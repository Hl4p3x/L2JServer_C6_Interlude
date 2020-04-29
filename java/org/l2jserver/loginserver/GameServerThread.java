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
package org.l2jserver.loginserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.crypt.NewCrypt;
import org.l2jserver.loginserver.GameServerTable.GameServerInfo;
import org.l2jserver.loginserver.network.gameserverpackets.BlowFishKey;
import org.l2jserver.loginserver.network.gameserverpackets.ChangeAccessLevel;
import org.l2jserver.loginserver.network.gameserverpackets.GameServerAuth;
import org.l2jserver.loginserver.network.gameserverpackets.PlayerAuthRequest;
import org.l2jserver.loginserver.network.gameserverpackets.PlayerInGame;
import org.l2jserver.loginserver.network.gameserverpackets.PlayerLogout;
import org.l2jserver.loginserver.network.gameserverpackets.ServerStatus;
import org.l2jserver.loginserver.network.loginserverpackets.AuthResponse;
import org.l2jserver.loginserver.network.loginserverpackets.InitLS;
import org.l2jserver.loginserver.network.loginserverpackets.KickPlayer;
import org.l2jserver.loginserver.network.loginserverpackets.LoginServerFail;
import org.l2jserver.loginserver.network.loginserverpackets.PlayerAuthResponse;
import org.l2jserver.loginserver.network.serverpackets.ServerBasePacket;

/**
 * @author -Wooden-
 * @author KenM
 */
public class GameServerThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(GameServerThread.class.getName());
	private final Socket _connection;
	private InputStream _in;
	private OutputStream _out;
	private final RSAPublicKey _publicKey;
	private final RSAPrivateKey _privateKey;
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	
	private final String _connectionIp;
	
	private GameServerInfo _gsi;
	
	/** Authed Clients on a GameServer */
	private final Set<String> _accountsOnGameServer = new HashSet<>();
	
	private String _connectionIPAddress;
	
	@Override
	public void run()
	{
		boolean checkTime = true;
		final long time = System.currentTimeMillis();
		_connectionIPAddress = _connection.getInetAddress().getHostAddress();
		if (isBannedGameserverIP(_connectionIPAddress))
		{
			LOGGER.info("GameServerRegistration: IP Address " + _connectionIPAddress + " is on Banned IP list.");
			forceClose(LoginServerFail.REASON_IP_BANNED);
			// ensure no further processing for this connection
			return;
		}
		
		final InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
		try
		{
			sendPacket(startPacket);
			
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			while (true)
			{
				if (((time - System.currentTimeMillis()) > 10000) && checkTime)
				{
					_connection.close();
					break;
				}
				
				try
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = (lengthHi * 256) + lengthLo;
				}
				catch (IOException e)
				{
					lengthHi = -1;
					/*
					 * String serverName = getServerId() != -1 ? "[" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) : "(" + _connectionIPAddress + ")"; String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage(); LOGGER.info(msg);
					 * serverName = null; msg = null;
					 */
				}
				
				if ((lengthHi < 0) || _connection.isClosed())
				{
					LOGGER.info("LoginServerThread: Login terminated the connection.");
					break;
				}
				
				byte[] data = new byte[length - 2];
				int receivedBytes = 0;
				int newBytes = 0;
				
				while ((newBytes != -1) && (receivedBytes < (length - 2)))
				{
					newBytes = _in.read(data, 0, length - 2);
					receivedBytes = receivedBytes + newBytes;
				}
				
				if (receivedBytes != (length - 2))
				{
					LOGGER.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}
				
				// decrypt if we have a key
				data = _blowfish.decrypt(data);
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk)
				{
					LOGGER.warning("Incorrect packet checksum, closing connection (LS)");
					return;
				}
				
				final int packetType = data[0] & 0xff;
				switch (packetType)
				{
					case 00:
					{
						checkTime = false;
						onReceiveBlowfishKey(data);
						break;
					}
					case 01:
					{
						onGameServerAuth(data);
						break;
					}
					case 02:
					{
						onReceivePlayerInGame(data);
						break;
					}
					case 03:
					{
						onReceivePlayerLogOut(data);
						break;
					}
					case 04:
					{
						onReceiveChangeAccessLevel(data);
						break;
					}
					case 05:
					{
						onReceivePlayerAuthRequest(data);
						break;
					}
					case 06:
					{
						onReceiveServerStatus(data);
						break;
					}
					default:
					{
						LOGGER.warning("Unknown Opcode (" + Integer.toHexString(packetType).toUpperCase() + ") from GameServer, closing connection.");
						forceClose(LoginServerFail.NOT_AUTHED);
					}
				}
			}
		}
		catch (IOException e)
		{
			final String serverName = getServerId() != -1 ? "[" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) : "(" + _connectionIPAddress + ")";
			final String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
			LOGGER.info(msg);
		}
		finally
		{
			if (isAuthed())
			{
				_gsi.setDown();
				LOGGER.info("Server [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " is now set as disconnected");
			}
			
			LoginServer.getInstance().getGameServerListener().removeGameServer(this);
			LoginServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}
	
	private void onReceiveBlowfishKey(byte[] data)
	{
		final BlowFishKey bfk = new BlowFishKey(data, _privateKey);
		_blowfishKey = bfk.getKey();
		_blowfish = new NewCrypt(_blowfishKey);
	}
	
	private void onGameServerAuth(byte[] data) throws IOException
	{
		handleRegProcess(new GameServerAuth(data));
		if (isAuthed())
		{
			final AuthResponse ar = new AuthResponse(_gsi.getId());
			sendPacket(ar);
		}
	}
	
	private void onReceivePlayerInGame(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerInGame pig = new PlayerInGame(data);
			final List<String> newAccounts = pig.getAccounts();
			for (String account : newAccounts)
			{
				_accountsOnGameServer.add(account);
			}
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void onReceivePlayerLogOut(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerLogout plo = new PlayerLogout(data);
			_accountsOnGameServer.remove(plo.getAccount());
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void onReceiveChangeAccessLevel(byte[] data)
	{
		if (isAuthed())
		{
			final ChangeAccessLevel cal = new ChangeAccessLevel(data);
			LoginController.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
			LOGGER.info("Changed " + cal.getAccount() + " access level to " + cal.getLevel());
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void onReceivePlayerAuthRequest(byte[] data) throws IOException
	{
		if (isAuthed())
		{
			final PlayerAuthRequest par = new PlayerAuthRequest(data);
			PlayerAuthResponse authResponse;
			final SessionKey key = LoginController.getInstance().getKeyForAccount(par.getAccount());
			if ((key != null) && key.equals(par.getKey()))
			{
				LoginController.getInstance().removeAuthedLoginClient(par.getAccount());
				authResponse = new PlayerAuthResponse(par.getAccount(), true);
			}
			else
			{
				authResponse = new PlayerAuthResponse(par.getAccount(), false);
			}
			sendPacket(authResponse);
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void onReceiveServerStatus(byte[] data)
	{
		if (isAuthed())
		{
			new ServerStatus(data, getServerId()); // server status
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void handleRegProcess(GameServerAuth gameServerAuth)
	{
		final GameServerTable gameServerTable = GameServerTable.getInstance();
		final int id = gameServerAuth.getDesiredID();
		final byte[] hexId = gameServerAuth.getHexID();
		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if (gsi != null)
		{
			// does the hex id match?
			if (Arrays.equals(gsi.getHexId(), hexId))
			{
				// check to see if this GS is already connected
				synchronized (gsi)
				{
					if (gsi.isAuthed())
					{
						forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
					}
					else
					{
						attachGameServerInfo(gsi, gameServerAuth);
					}
				}
			}
			else // there is already a server registered with the desired id and different hex id
			// try to register this one with an alternative id
			if (Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
			{
				gsi = new GameServerInfo(id, hexId, this);
				if (gameServerTable.registerWithFirstAvaliableId(gsi))
				{
					attachGameServerInfo(gsi, gameServerAuth);
					gameServerTable.registerServerOnDB(gsi);
				}
				else
				{
					forceClose(LoginServerFail.REASON_NO_FREE_ID);
				}
			}
			else
			{
				// server id is already taken, and we cant get a new one for you
				forceClose(LoginServerFail.REASON_WRONG_HEXID);
			}
		}
		else // can we register on this id?
		if (Config.ACCEPT_NEW_GAMESERVER)
		{
			gsi = new GameServerInfo(id, hexId, this);
			if (gameServerTable.register(id, gsi))
			{
				attachGameServerInfo(gsi, gameServerAuth);
				gameServerTable.registerServerOnDB(gsi);
			}
			else
			{
				// some one took this ID meanwhile
				forceClose(LoginServerFail.REASON_ID_RESERVED);
			}
		}
		else
		{
			forceClose(LoginServerFail.REASON_WRONG_HEXID);
		}
	}
	
	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}
	
	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}
	
	/**
	 * Attachs a GameServerInfo to this Thread
	 * <li>Updates the GameServerInfo values based on GameServerAuth packet</li>
	 * <li><b>Sets the GameServerInfo as Authed</b></li><br>
	 * @param gsi The GameServerInfo to be attached.
	 * @param gameServerAuth The server info.
	 */
	private void attachGameServerInfo(GameServerInfo gsi, GameServerAuth gameServerAuth)
	{
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
	}
	
	private void forceClose(int reason)
	{
		final LoginServerFail lsf = new LoginServerFail(reason);
		
		try
		{
			sendPacket(lsf);
		}
		catch (IOException e)
		{
			LOGGER.warning("GameServerThread: Failed kicking banned server " + e);
		}
		
		try
		{
			_connection.close();
		}
		catch (IOException e)
		{
			LOGGER.warning("GameServerThread: Failed disconnecting banned server, server already disconnected " + e);
		}
	}
	
	/**
	 * @param ipAddress
	 * @return
	 */
	public static boolean isBannedGameserverIP(String ipAddress)
	{
		return false;
	}
	
	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch (IOException e)
		{
			LOGGER.warning(e.toString());
		}
		
		final KeyPair pair = GameServerTable.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(ServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		data = _blowfish.crypt(data);
		
		final int len = data.length + 2;
		synchronized (_out)
		{
			_out.write(len & 0xff);
			_out.write((len >> 8) & 0xff);
			_out.write(data);
			_out.flush();
		}
	}
	
	public void kickPlayer(String account)
	{
		final KickPlayer kp = new KickPlayer(account);
		try
		{
			sendPacket(kp);
		}
		catch (IOException e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * @param gameExternalHost
	 * @param gameInternalHost
	 */
	public void setGameHosts(String gameExternalHost, String gameInternalHost)
	{
		final String oldInternal = _gsi.getInternalHost();
		final String oldExternal = _gsi.getExternalHost();
		_gsi.setExternalHost(gameExternalHost);
		_gsi.setInternalIp(gameInternalHost);
		
		if (!gameExternalHost.equals("*"))
		{
			try
			{
				_gsi.setExternalIp(InetAddress.getByName(gameExternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				LOGGER.warning("Couldn't resolve hostname \"" + gameExternalHost + "\"");
			}
		}
		else
		{
			_gsi.setExternalIp(_connectionIp);
		}
		
		if (!gameInternalHost.equals("*"))
		{
			try
			{
				_gsi.setInternalIp(InetAddress.getByName(gameInternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				LOGGER.warning("Couldn't resolve hostname \"" + gameInternalHost + "\"");
			}
		}
		else
		{
			_gsi.setInternalIp(_connectionIp);
		}
		
		LOGGER.info("Updated Gameserver [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " IP's:");
		if ((oldInternal == null) || !oldInternal.equalsIgnoreCase(gameInternalHost))
		{
			LOGGER.info("InternalIP: " + gameInternalHost);
		}
		
		if ((oldExternal == null) || !oldExternal.equalsIgnoreCase(gameExternalHost))
		{
			LOGGER.info("ExternalIP: " + gameExternalHost);
		}
	}
	
	/**
	 * @return Returns the isAuthed.
	 */
	public boolean isAuthed()
	{
		if (_gsi == null)
		{
			return false;
		}
		return _gsi.isAuthed();
	}
	
	public void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}
	
	public GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}
	
	/**
	 * @return Returns the connectionIpAddress.
	 */
	public String getConnectionIpAddress()
	{
		return _connectionIPAddress;
	}
	
	private int getServerId()
	{
		if (_gsi != null)
		{
			return _gsi.getId();
		}
		return -1;
	}
}
