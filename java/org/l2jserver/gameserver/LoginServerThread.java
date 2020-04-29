/*
 * This file is part of the L2JServer project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jserver.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.crypt.NewCrypt;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.GameClient;
import org.l2jserver.gameserver.network.GameClient.GameClientState;
import org.l2jserver.gameserver.network.gameserverpackets.AuthRequest;
import org.l2jserver.gameserver.network.gameserverpackets.BlowFishKey;
import org.l2jserver.gameserver.network.gameserverpackets.ChangeAccessLevel;
import org.l2jserver.gameserver.network.gameserverpackets.GameServerBasePacket;
import org.l2jserver.gameserver.network.gameserverpackets.PlayerAuthRequest;
import org.l2jserver.gameserver.network.gameserverpackets.PlayerInGame;
import org.l2jserver.gameserver.network.gameserverpackets.PlayerLogout;
import org.l2jserver.gameserver.network.gameserverpackets.ServerStatus;
import org.l2jserver.gameserver.network.loginserverpackets.AuthResponse;
import org.l2jserver.gameserver.network.loginserverpackets.InitLS;
import org.l2jserver.gameserver.network.loginserverpackets.KickPlayer;
import org.l2jserver.gameserver.network.loginserverpackets.LoginServerFail;
import org.l2jserver.gameserver.network.loginserverpackets.PlayerAuthResponse;
import org.l2jserver.gameserver.network.serverpackets.AuthLoginFail;
import org.l2jserver.gameserver.network.serverpackets.CharSelectInfo;

public class LoginServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(LoginServerThread.class.getName());
	
	/** The LoginServerThread singleton */
	private static final int REVISION = 0x0102;
	private RSAPublicKey _publicKey;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;
	
	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #_blowfishKey}
	 */
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private byte[] _hexID;
	private final boolean _acceptAlternate;
	private int _requestID;
	private int _serverID;
	private final boolean _reserveHost;
	private int _maxPlayer;
	private final List<WaitingClient> _waitingClients;
	private final Map<String, GameClient> _accountsInGameServer;
	private int _status;
	private String _serverName;
	private final String _gameExternalHost;
	private final String _gameInternalHost;
	
	public LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_waitingClients = new ArrayList<>();
		_accountsInGameServer = new ConcurrentHashMap<>();
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	@Override
	public void run()
	{
		while (!_interrupted)
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				LOGGER.info("Connecting to login on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				if (_out != null)
				{
					synchronized (_out) // avoids tow threads writing in the mean time
					{
						_out = new BufferedOutputStream(_loginSocket.getOutputStream());
					}
				}
				else
				{
					_out = new BufferedOutputStream(_loginSocket.getOutputStream());
				}
				
				// init Blowfish
				_blowfishKey = generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!_interrupted)
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = (lengthHi * 256) + lengthLo;
					if (lengthHi < 0)
					{
						LOGGER.info("LoginServerThread: Login terminated the connection.");
						break;
					}
					
					final byte[] incoming = new byte[length];
					incoming[0] = (byte) lengthLo;
					incoming[1] = (byte) lengthHi;
					int receivedBytes = 0;
					int newBytes = 0;
					while ((newBytes != -1) && (receivedBytes < (length - 2)))
					{
						newBytes = _in.read(incoming, 2, length - 2);
						receivedBytes = receivedBytes + newBytes;
					}
					
					if (receivedBytes != (length - 2))
					{
						LOGGER.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					byte[] decrypt = new byte[length - 2];
					System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
					// decrypt if we have a key
					decrypt = _blowfish.decrypt(decrypt);
					checksumOk = NewCrypt.verifyChecksum(decrypt);
					if (!checksumOk)
					{
						LOGGER.warning("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					final int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
						case 0x00:
						{
							final InitLS init = new InitLS(decrypt);
							if (init.getRevision() != REVISION)
							{
								// TODO: revision mismatch
								LOGGER.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try
							{
								final KeyFactory kfac = KeyFactory.getInstance("RSA");
								final BigInteger modulus = new BigInteger(init.getRSAKey());
								final RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
							}
							catch (GeneralSecurityException e)
							{
								LOGGER.warning("Trouble while init the public key send by login");
								break;
							}
							// send the blowfish key through the rsa encryption
							final BlowFishKey bfk = new BlowFishKey(_blowfishKey, _publicKey);
							sendPacket(bfk);
							
							// now, only accept paket with the new encryption
							_blowfish = new NewCrypt(_blowfishKey);
							
							final AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer);
							sendPacket(ar);
							break;
						}
						case 0x01:
						{
							final LoginServerFail lsf = new LoginServerFail(decrypt);
							LOGGER.info("Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						}
						case 0x02:
						{
							final AuthResponse aresp = new AuthResponse(decrypt);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							LOGGER.info("Registered on login as Server " + _serverID + " : " + _serverName);
							final ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_CLOCK)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.OFF);
							}
							if (Config.SERVER_LIST_TESTSERVER)
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.OFF);
							}
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							sendPacket(st);
							if (World.getAllPlayersCount() > 0)
							{
								final List<String> playerList = new ArrayList<>();
								for (PlayerInstance player : World.getInstance().getAllPlayers())
								{
									playerList.add(player.getAccountName());
								}
								final PlayerInGame pig = new PlayerInGame(playerList);
								sendPacket(pig);
							}
							break;
						}
						case 0x03:
						{
							final PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							final String account = par.getAccount();
							WaitingClient wcToRemove = null;
							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									final PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									final CharSelectInfo cl = new CharSelectInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									LOGGER.warning("Session key is not correct. Closing connection for account " + wcToRemove.account + ".");
									wcToRemove.gameClient.getConnection().sendPacket(new AuthLoginFail(1));
									wcToRemove.gameClient.closeNow();
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						}
						case 0x04:
						{
							final KickPlayer kp = new KickPlayer(decrypt);
							doKickPlayer(kp.getAccount());
							break;
						}
					}
				}
			}
			catch (UnknownHostException e)
			{
				LOGGER.info("Deconnected from Login, Trying to reconnect:");
				LOGGER.info(e.toString());
			}
			catch (IOException e)
			{
				LOGGER.info("Deconnected from Login, Trying to reconnect..");
			}
			finally
			{
				if (_out != null)
				{
					synchronized (_out) // avoids tow threads writing in the mean time
					{
						try
						{
							_loginSocket.close();
						}
						catch (Exception e)
						{
						}
					}
				}
				else
				{
					try
					{
						_loginSocket.close();
					}
					catch (Exception e)
					{
					}
				}
			}
			
			try
			{
				Thread.sleep(5000); // 5 seconds
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(String acc, GameClient client, SessionKey key)
	{
		final WaitingClient wc = new WaitingClient(acc, client, key);
		
		synchronized (_waitingClients)
		{
			_waitingClients.add(wc);
		}
		
		final PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		
		try
		{
			sendPacket(par);
		}
		catch (IOException e)
		{
			LOGGER.warning("Error while sending player auth request");
		}
	}
	
	public void removeWaitingClient(GameClient client)
	{
		WaitingClient toRemove = null;
		
		synchronized (_waitingClients)
		{
			for (WaitingClient c : _waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
				}
			}
			
			if (toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}
	}
	
	public void sendLogout(String account)
	{
		if (account == null)
		{
			return;
		}
		final PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (IOException e)
		{
			LOGGER.warning("Error while sending logout packet to login: " + e.getMessage());
		}
	}
	
	public boolean addGameServerLogin(String account, GameClient client)
	{
		final GameClient savedClient = _accountsInGameServer.get(account);
		if (savedClient != null)
		{
			if (savedClient.isDetached())
			{
				_accountsInGameServer.put(account, client);
				return true;
			}
			
			savedClient.closeNow();
			_accountsInGameServer.remove(account);
			return false;
		}
		
		_accountsInGameServer.put(account, client);
		return true;
	}
	
	public void sendAccessLevel(String account, int level)
	{
		final ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (IOException e)
		{
		}
	}
	
	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(String account)
	{
		if (_accountsInGameServer.get(account) != null)
		{
			_accountsInGameServer.get(account).closeNow();
			getInstance().sendLogout(account);
		}
	}
	
	public static byte[] generateHex(int size)
	{
		final byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(GameServerBasePacket sl) throws IOException
	{
		if (_interrupted)
		{
			return;
		}
		
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		data = _blowfish.crypt(data);
		
		final int len = data.length + 2;
		if ((_out != null) && !_loginSocket.isClosed() && _loginSocket.isConnected())
		{
			synchronized (_out) // avoids tow threads writing in the mean time
			{
				_out.write(len & 0xff);
				_out.write((len >> 8) & 0xff);
				_out.write(data);
				_out.flush();
			}
		}
	}
	
	/**
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}
	
	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	/**
	 * @param id
	 * @param value
	 */
	public void sendServerStatus(int id, int value)
	{
		final ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
		}
	}
	
	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}
	
	/**
	 * @return
	 */
	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
	}
	
	/**
	 * @return
	 */
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}
	
	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return _serverName;
	}
	
	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_DOWN:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_FULL:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_GM_ONLY:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_GOOD:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			}
			case ServerStatus.STATUS_NORMAL:
			{
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Status does not exists:" + status);
			}
		}
	}
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		public int clientKey = -1;
		
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private class WaitingClient
	{
		public String account;
		public GameClient gameClient;
		public SessionKey session;
		
		public WaitingClient(String acc, GameClient client, SessionKey key)
		{
			account = acc;
			gameClient = client;
			session = key;
		}
	}
	
	private boolean _interrupted = false;
	
	@Override
	public void interrupt()
	{
		_interrupted = true;
		super.interrupt();
	}
	
	@Override
	public boolean isInterrupted()
	{
		return _interrupted;
	}
	
	/**
	 * Gets the single instance of LoginServerThread.
	 * @return single instance of LoginServerThread
	 */
	public static LoginServerThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginServerThread INSTANCE = new LoginServerThread();
	}
}
