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
package org.l2jserver.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.mmocore.MMOClient;
import org.l2jserver.commons.mmocore.MMOConnection;
import org.l2jserver.commons.mmocore.ReceivablePacket;
import org.l2jserver.gameserver.LoginServerThread;
import org.l2jserver.gameserver.LoginServerThread.SessionKey;
import org.l2jserver.gameserver.datatables.OfflineTradeTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.CharSelectInfoPackage;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.GameEvent;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.event.VIP;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.ServerClose;
import org.l2jserver.gameserver.util.EventData;
import org.l2jserver.gameserver.util.FloodProtectors;

public class GameClient extends MMOClient<MMOConnection<GameClient>> implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(GameClient.class.getName());
	
	/**
	 * CONNECTED - client has just connected AUTHED - client has authed but doesn't has character attached to it yet IN_GAME - client has selected a char and is in game
	 * @author KenM
	 */
	public enum GameClientState
	{
		CONNECTED,
		AUTHED,
		ENTERING,
		IN_GAME
	}
	
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);
	private final ReentrantLock _playerLock = new ReentrantLock();
	private final List<Integer> _charSlotMapping = new ArrayList<>();
	private final ReentrantLock _queueLock = new ReentrantLock();
	private final ArrayBlockingQueue<ReceivablePacket<GameClient>> _packetQueue;
	private final GameCrypt _crypt;
	private GameClientState _state;
	private String _accountName;
	private SessionKey _sessionId;
	private PlayerInstance _player;
	private ScheduledFuture<?> _cleanupTask = null;
	private volatile boolean _isDetached = false;
	private boolean _isAuthedGG;
	private int _protocolVersion;
	
	public GameClient(MMOConnection<GameClient> con)
	{
		super(con);
		_state = GameClientState.CONNECTED;
		_crypt = new GameCrypt();
		_packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);
	}
	
	public byte[] enableCrypt()
	{
		final byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState pState)
	{
		if (_state != pState)
		{
			_state = pState;
			_packetQueue.clear();
		}
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}
	
	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	public PlayerInstance getPlayer()
	{
		return _player;
	}
	
	public void setPlayer(PlayerInstance player)
	{
		_player = player;
		if (_player != null)
		{
			World.getInstance().storeObject(_player);
		}
	}
	
	public ReentrantLock getPlayerLock()
	{
		return _playerLock;
	}
	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}
	
	public void setGameGuardOk(boolean value)
	{
		_isAuthedGG = value;
	}
	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
	}
	
	public String getAccountName()
	{
		return _accountName;
	}
	
	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}
	
	public SessionKey getSessionId()
	{
		return _sessionId;
	}
	
	public void sendPacket(GameServerPacket gsp)
	{
		if (_isDetached)
		{
			return;
		}
		
		if (getConnection() != null)
		{
			getConnection().sendPacket(gsp);
			gsp.runImpl();
		}
	}
	
	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void setDetached(boolean value)
	{
		_isDetached = value;
	}
	
	/**
	 * Method to handle character deletion
	 * @param charslot
	 * @return a byte:
	 *         <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *         <li>0: character is not member of any clan, proceed with deletion
	 *         <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public byte markToDeleteChar(int charslot)
	{
		final int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
		{
			return -1;
		}
		
		byte answer = -1;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clanId from characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			final ResultSet rs = statement.executeQuery();
			rs.next();
			
			final int clanId = rs.getInt(1);
			answer = 0;
			if (clanId != 0)
			{
				final Clan clan = ClanTable.getInstance().getClan(clanId);
				if (clan == null)
				{
					answer = 0; // jeezes!
				}
				else if (clan.getLeaderId() == objid)
				{
					answer = 2;
				}
				else
				{
					answer = 1;
				}
			}
			
			// Setting delete time
			if (answer == 0)
			{
				if (Config.DELETE_DAYS == 0)
				{
					deleteCharByObjId(objid);
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_Id=?");
					statement.setLong(1, System.currentTimeMillis() + (Config.DELETE_DAYS * 86400000)); // 24*60*60*1000 = 86400000
					statement.setInt(2, objid);
					statement.execute();
					statement.close();
					rs.close();
				}
			}
			else
			{
				statement.close();
				rs.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on update delete time of char: " + e);
			answer = -1;
		}
		
		return answer;
	}
	
	public void markRestoredChar(int charslot)
	{
		// have to make sure active character must be nulled
		final int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on restoring char " + e);
		}
	}
	
	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			
			statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on deleting char: " + e);
		}
	}
	
	public PlayerInstance loadCharFromDisk(int charslot)
	{
		final int objId = getObjectIdForSlot(charslot);
		if (objId < 0)
		{
			return null;
		}
		
		PlayerInstance character = World.getInstance().getPlayer(objId);
		if (character != null)
		{
			// exploit prevention, should not happens in normal way
			LOGGER.warning("Attempt of double login: " + character.getName() + "(" + objId + ") " + _accountName);
			
			if (character.getClient() != null)
			{
				character.getClient().closeNow();
			}
			else
			{
				character.deleteMe();
				
				try
				{
					character.store();
				}
				catch (Exception e2)
				{
					LOGGER.warning("fixme:unhandled exception " + e2);
				}
			}
		}
		
		character = PlayerInstance.load(objId);
		return character;
	}
	
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();
		
		for (CharSelectInfoPackage c : chars)
		{
			final int objectId = c.getObjectId();
			_charSlotMapping.add(objectId);
		}
	}
	
	public void close(GameServerPacket gsp)
	{
		if (getConnection() != null)
		{
			getConnection().close(gsp);
		}
	}
	
	private int getObjectIdForSlot(int charslot)
	{
		if ((charslot < 0) || (charslot >= _charSlotMapping.size()))
		{
			LOGGER.warning(this + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		
		final Integer objectId = _charSlotMapping.get(charslot);
		return objectId.intValue();
	}
	
	@Override
	public void onForcedDisconnection()
	{
		// the force operation will allow to not save client position to prevent again criticals and stuck
		closeNow();
	}
	
	@Override
	public void onDisconnection()
	{
		// no long running tasks here, do it async
		try
		{
			ThreadPool.execute(new DisconnectTask());
		}
		catch (RejectedExecutionException e)
		{
			// server is closing
		}
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public void closeNow()
	{
		close(0);
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 * @param delay
	 */
	public void close(int delay)
	{
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
			{
				cancelCleanup();
			}
			_cleanupTask = ThreadPool.schedule(new CleanupTask(), delay); // delayed
		}
	}
	
	public String getIpAddress()
	{
		final InetAddress address = getConnection().getInetAddress();
		String ip;
		if (address == null)
		{
			ip = "N/A";
		}
		else
		{
			ip = address.getHostAddress();
		}
		return ip;
	}
	
	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			switch (_state)
			{
				case CONNECTED:
				{
					return "[IP: " + getIpAddress() + "]";
				}
				case AUTHED:
				{
					return "[Account: " + _accountName + " - IP: " + getIpAddress() + "]";
				}
				case ENTERING:
				case IN_GAME:
				{
					return "[Character: " + (_player == null ? "disconnected" : _player.getName()) + " - Account: " + _accountName + " - IP: " + getIpAddress() + "]";
				}
				default:
				{
					throw new IllegalStateException("Missing state on switch");
				}
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}
	
	protected class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// we are going to manually save the char below thus we can force the cancel
				
				final PlayerInstance player = _player;
				if (player != null) // this should only happen on connection loss
				{
					// we store all data from players who are disconnected while in an event in order to restore it in the next login
					if (player.atEvent)
					{
						final EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventKarma, player.eventPvpKills, player.eventPkKills, player.eventTitle, player.kills, player.eventSitForced);
						GameEvent.connectionLossData.put(player.getName(), data);
					}
					else if (player._inEventCTF)
					{
						CTF.onDisconnect(player);
					}
					else if (player._inEventDM)
					{
						DM.onDisconnect(player);
					}
					else if (player._inEventTvT)
					{
						TvT.onDisconnect(player);
					}
					else if (player._inEventVIP)
					{
						VIP.onDisconnect(player);
					}
					
					if (player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					}
					
					if (Olympiad.getInstance().isRegistered(player))
					{
						Olympiad.getInstance().unRegisterNoble(player);
					}
					
					// Decrease boxes number
					if (player._activeBoxes != -1)
					{
						player.decreaseBoxes();
					}
					
					// prevent closing again
					player.setClient(null);
					player.deleteMe();
					player.store(true);
				}
				
				setPlayer(null);
				setDetached(true);
			}
			catch (Exception e1)
			{
				LOGGER.warning("Error while cleanup client. " + e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	protected class DisconnectTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// we are going to manually save the char bellow thus we can force the cancel
				
				final PlayerInstance player = _player;
				if (player != null) // this should only happen on connection loss
				{
					// we store all data from players who are disconnected while in an event in order to restore it in the next login
					if (player.atEvent)
					{
						final EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventKarma, player.eventPvpKills, player.eventPkKills, player.eventTitle, player.kills, player.eventSitForced);
						GameEvent.connectionLossData.put(player.getName(), data);
					}
					else if (player._inEventCTF)
					{
						CTF.onDisconnect(player);
					}
					else if (player._inEventDM)
					{
						DM.onDisconnect(player);
					}
					else if (player._inEventTvT)
					{
						TvT.onDisconnect(player);
					}
					else if (player._inEventVIP)
					{
						VIP.onDisconnect(player);
					}
					
					if (player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					}
					
					if (Olympiad.getInstance().isRegistered(player))
					{
						Olympiad.getInstance().unRegisterNoble(player);
					}
					
					// Decrease boxes number
					if (player._activeBoxes != -1)
					{
						player.decreaseBoxes();
					}
					
					if (!player.isKicked() //
						&& !Olympiad.getInstance().isRegistered(player) //
						&& !player.isInOlympiadMode() //
						&& !player.isInFunEvent() //
						&& ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) //
							|| (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE)))
					{
						if (!Config.OFFLINE_MODE_IN_PEACE_ZONE || (Config.OFFLINE_MODE_IN_PEACE_ZONE && player.isInsideZone(ZoneId.PEACE)))
						{
							player.setOfflineMode(true);
							player.setOnlineStatus(false);
							player.leaveParty();
							player.store();
							
							if (Config.OFFLINE_MODE_SET_INVULNERABLE)
							{
								_player.setInvul(true);
							}
							if (Config.OFFLINE_SET_NAME_COLOR)
							{
								player._originalNameColorOffline = player.getAppearance().getNameColor();
								player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
								player.broadcastUserInfo();
							}
							
							if (player.getOfflineStartTime() == 0)
							{
								player.setOfflineStartTime(System.currentTimeMillis());
							}
							
							OfflineTradeTable.storeOffliner(player);
							World.OFFLINE_TRADE_COUNT++;
							return;
						}
					}
					
					// notify the world about our disconnect
					player.deleteMe();
					
					// store operation
					try
					{
						player.store();
					}
					catch (Exception e2)
					{
					}
				}
				
				setPlayer(null);
				setDetached(true);
			}
			catch (Exception e1)
			{
				LOGGER.warning("error while disconnecting client " + e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	public FloodProtectors getFloodProtectors()
	{
		return _floodProtectors;
	}
	
	private boolean cancelCleanup()
	{
		final Future<?> task = _cleanupTask;
		if (task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
	
	/**
	 * Returns false if client can receive packets. True if detached or queue overflow detected and queue still not empty.
	 * @return
	 */
	public boolean dropPacket()
	{
		return _isDetached;
	}
	
	/**
	 * Add packet to the queue and start worker thread if needed
	 * @param packet
	 */
	public void execute(ReceivablePacket<GameClient> packet)
	{
		if (!_packetQueue.offer(packet))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_queueLock.isLocked())
		{
			return;
		}
		
		try
		{
			ThreadPool.execute(this);
		}
		catch (RejectedExecutionException e)
		{
		}
	}
	
	@Override
	public void run()
	{
		if (!_queueLock.tryLock())
		{
			return;
		}
		
		try
		{
			while (true)
			{
				final ReceivablePacket<GameClient> packet = _packetQueue.poll();
				if (packet == null)
				{
					return;
				}
				
				if (_isDetached) // clear queue immediately after detach
				{
					_packetQueue.clear();
					return;
				}
				
				try
				{
					packet.run();
				}
				catch (Exception e)
				{
					LOGGER.warning("Exception during execution " + packet.getClass().getSimpleName() + ", client: " + this + "," + e.getMessage());
				}
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}
	
	public void setProtocolVersion(int version)
	{
		_protocolVersion = version;
	}
	
	public int getProtocolVersion()
	{
		return _protocolVersion;
	}
}
