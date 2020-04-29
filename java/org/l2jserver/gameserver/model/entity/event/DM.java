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
package org.l2jserver.gameserver.model.entity.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.entity.event.manager.EventTask;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.Ride;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;

public class DM implements EventTask
{
	protected static final Logger LOGGER = Logger.getLogger(DM.class.getName());
	
	private static String _eventName = "";
	private static String _eventDesc = "";
	private static String _joiningLocationName = "";
	private static Spawn _npcSpawn;
	private static boolean _joining = false;
	private static boolean _teleport = false;
	private static boolean _started = false;
	private static boolean _aborted = false;
	private static boolean _sitForced = false;
	private static boolean _inProgress = false;
	protected static int _npcId = 0;
	protected static int _npcX = 0;
	protected static int _npcY = 0;
	protected static int _npcZ = 0;
	protected static int _npcHeading = 0;
	protected static int _rewardId = 0;
	protected static int _rewardAmount = 0;
	protected static int _minlvl = 0;
	protected static int _maxlvl = 0;
	protected static int _joinTime = 0;
	protected static int _eventTime = 0;
	protected static int _minPlayers = 0;
	protected static int _maxPlayers = 0;
	protected static int _topKills = 0;
	protected static int _playerColors = 0;
	protected static int _playerX = 0;
	protected static int _playerY = 0;
	protected static int _playerZ = 0;
	private static long _intervalBetweenMatches = 0;
	private String startEventTime;
	protected static boolean _teamEvent = false; // TODO to be integrated
	public static List<PlayerInstance> _players = new ArrayList<>();
	public static List<PlayerInstance> _topPlayers = new ArrayList<>();
	public static List<String> _savePlayers = new ArrayList<>();
	
	/**
	 * Instantiates a new dM.
	 */
	private DM()
	{
	}
	
	/**
	 * Gets the new instance.
	 * @return the new instance
	 */
	public static DM getNewInstance()
	{
		return new DM();
	}
	
	/**
	 * Gets the _event name.
	 * @return the _eventName
	 */
	public static String getEventName()
	{
		return _eventName;
	}
	
	/**
	 * Set_event name.
	 * @param eventName the _eventName to set
	 * @return true, if successful
	 */
	public static boolean setEventName(String eventName)
	{
		if (!_inProgress)
		{
			DM._eventName = eventName;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _event desc.
	 * @return the _eventDesc
	 */
	public static String getEventDesc()
	{
		return _eventDesc;
	}
	
	/**
	 * Set_event desc.
	 * @param eventDesc the _eventDesc to set
	 * @return true, if successful
	 */
	public static boolean setEventDesc(String eventDesc)
	{
		if (!_inProgress)
		{
			DM._eventDesc = eventDesc;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _joining location name.
	 * @return the _joiningLocationName
	 */
	public static String getJoiningLocationName()
	{
		return _joiningLocationName;
	}
	
	/**
	 * Set_joining location name.
	 * @param joiningLocationName the _joiningLocationName to set
	 * @return true, if successful
	 */
	public static boolean setJoiningLocationName(String joiningLocationName)
	{
		if (!_inProgress)
		{
			DM._joiningLocationName = joiningLocationName;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _npc id.
	 * @return the _npcId
	 */
	public static int getNpcId()
	{
		return _npcId;
	}
	
	/**
	 * Set_npc id.
	 * @param npcId the _npcId to set
	 * @return true, if successful
	 */
	public static boolean setNpcId(int npcId)
	{
		if (!_inProgress)
		{
			DM._npcId = npcId;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _npc location.
	 * @return the _npc location
	 */
	public static Location getNpcLocation()
	{
		return new Location(_npcX, _npcY, _npcZ, _npcHeading);
	}
	
	/**
	 * Gets the _reward id.
	 * @return the _rewardId
	 */
	public static int getRewardId()
	{
		return _rewardId;
	}
	
	/**
	 * Set_reward id.
	 * @param rewardId the _rewardId to set
	 * @return true, if successful
	 */
	public static boolean setRewardId(int rewardId)
	{
		if (!_inProgress)
		{
			DM._rewardId = rewardId;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _reward amount.
	 * @return the _rewardAmount
	 */
	public static int getRewardAmount()
	{
		return _rewardAmount;
	}
	
	/**
	 * Set_reward amount.
	 * @param rewardAmount the _rewardAmount to set
	 * @return true, if successful
	 */
	public static boolean setRewardAmount(int rewardAmount)
	{
		if (!_inProgress)
		{
			DM._rewardAmount = rewardAmount;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _minlvl.
	 * @return the _minlvl
	 */
	public static int getMinlvl()
	{
		return _minlvl;
	}
	
	/**
	 * Set_minlvl.
	 * @param minlvl the _minlvl to set
	 * @return true, if successful
	 */
	public static boolean setMinlvl(int minlvl)
	{
		if (!_inProgress)
		{
			DM._minlvl = minlvl;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _maxlvl.
	 * @return the _maxlvl
	 */
	public static int getMaxlvl()
	{
		return _maxlvl;
	}
	
	/**
	 * Set_maxlvl.
	 * @param maxlvl the _maxlvl to set
	 * @return true, if successful
	 */
	public static boolean setMaxlvl(int maxlvl)
	{
		if (!_inProgress)
		{
			DM._maxlvl = maxlvl;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _join time.
	 * @return the _joinTime
	 */
	public static int getJoinTime()
	{
		return _joinTime;
	}
	
	/**
	 * Set_join time.
	 * @param joinTime the _joinTime to set
	 * @return true, if successful
	 */
	public static boolean setJoinTime(int joinTime)
	{
		if (!_inProgress)
		{
			DM._joinTime = joinTime;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _event time.
	 * @return the _eventTime
	 */
	public static int getEventTime()
	{
		return _eventTime;
	}
	
	/**
	 * Set_event time.
	 * @param eventTime the _eventTime to set
	 * @return true, if successful
	 */
	public static boolean setEventTime(int eventTime)
	{
		if (!_inProgress)
		{
			DM._eventTime = eventTime;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _min players.
	 * @return the _minPlayers
	 */
	public static int getMinPlayers()
	{
		return _minPlayers;
	}
	
	/**
	 * Set_min players.
	 * @param minPlayers the _minPlayers to set
	 * @return true, if successful
	 */
	public static boolean setMinPlayers(int minPlayers)
	{
		if (!_inProgress)
		{
			DM._minPlayers = minPlayers;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _max players.
	 * @return the _maxPlayers
	 */
	public static int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	/**
	 * Set_max players.
	 * @param maxPlayers the _maxPlayers to set
	 * @return true, if successful
	 */
	public static boolean setMaxPlayers(int maxPlayers)
	{
		if (!_inProgress)
		{
			DM._maxPlayers = maxPlayers;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _interval between matches.
	 * @return the _intervalBetweenMatchs
	 */
	public static long getIntervalBetweenMatches()
	{
		return _intervalBetweenMatches;
	}
	
	/**
	 * Set_interval between matches.
	 * @param intervalBetweenMatches the _intervalBetweenMatchs to set
	 * @return true, if successful
	 */
	public static boolean setIntervalBetweenMatches(long intervalBetweenMatches)
	{
		if (!_inProgress)
		{
			DM._intervalBetweenMatches = intervalBetweenMatches;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the start event time.
	 * @return the startEventTime
	 */
	public String getStartEventTime()
	{
		return startEventTime;
	}
	
	/**
	 * Sets the start event time.
	 * @param startEventTime the startEventTime to set
	 * @return true, if successful
	 */
	public boolean setStartEventTime(String startEventTime)
	{
		if (!_inProgress)
		{
			this.startEventTime = startEventTime;
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is _joining.
	 * @return the _joining
	 */
	public static boolean isJoining()
	{
		return _joining;
	}
	
	/**
	 * Checks if is _teleport.
	 * @return the _teleport
	 */
	public static boolean isTeleport()
	{
		return _teleport;
	}
	
	/**
	 * Checks if is _started.
	 * @return the _started
	 */
	public static boolean hasStarted()
	{
		return _started;
	}
	
	/**
	 * Checks if is _aborted.
	 * @return the _aborted
	 */
	public static boolean isAborted()
	{
		return _aborted;
	}
	
	/**
	 * Checks if is _sit forced.
	 * @return the _sitForced
	 */
	public static boolean isSitForced()
	{
		return _sitForced;
	}
	
	/**
	 * Checks if is _in progress.
	 * @return the _inProgress
	 */
	public static boolean isInProgress()
	{
		return _inProgress;
	}
	
	/**
	 * Check max level.
	 * @param maxlvl the maxlvl
	 * @return true, if successful
	 */
	public static boolean checkMaxLevel(int maxlvl)
	{
		return _minlvl < maxlvl;
	}
	
	/**
	 * Check min level.
	 * @param minlvl the minlvl
	 * @return true, if successful
	 */
	public static boolean checkMinLevel(int minlvl)
	{
		return _maxlvl > minlvl;
	}
	
	/**
	 * returns true if participated players is higher or equal then minimum needed players.
	 * @param players the players
	 * @return true, if successful
	 */
	public static boolean checkMinPlayers(int players)
	{
		return _minPlayers > players;
	}
	
	/**
	 * returns true if max players is higher or equal then participated players.
	 * @param players the players
	 * @return true, if successful
	 */
	public static boolean checkMaxPlayers(int players)
	{
		return _maxPlayers <= players;
	}
	
	/**
	 * Check start join ok.
	 * @return true, if successful
	 */
	public static boolean checkStartJoinOk()
	{
		if (_started || _teleport || _joining || _eventName.equals("") || _joiningLocationName.equals("") || _eventDesc.equals("") || (_npcId == 0) || (_npcX == 0) || (_npcY == 0) || (_npcZ == 0) || (_rewardId == 0) || (_rewardAmount == 0))
		{
			return false;
		}
		
		if (_teamEvent)
		{
			if (!checkStartJoinTeamInfo())
			{
				return false;
			}
		}
		else if (!checkStartJoinPlayerInfo())
		{
			return false;
		}
		
		if (!Config.ALLOW_EVENTS_DURING_OLY && Olympiad.getInstance().inCompPeriod())
		{
			return false;
		}
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if ((castle != null) && (castle.getSiege() != null) && castle.getSiege().isInProgress())
			{
				return false;
			}
		}
		
		if (!checkOptionalEventStartJoinOk())
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check start join team info.
	 * @return true, if successful
	 */
	private static boolean checkStartJoinTeamInfo()
	{
		// TODO be integrated
		return true;
	}
	
	/**
	 * Check start join player info.
	 * @return true, if successful
	 */
	private static boolean checkStartJoinPlayerInfo()
	{
		return (_playerX != 0) && (_playerY != 0) && (_playerZ != 0) && (_playerColors != 0);
	}
	
	/**
	 * Check auto event start join ok.
	 * @return true, if successful
	 */
	private static boolean checkAutoEventStartJoinOk()
	{
		return (_joinTime != 0) && (_eventTime != 0);
	}
	
	/**
	 * Check optional event start join ok.
	 * @return true, if successful
	 */
	private static boolean checkOptionalEventStartJoinOk()
	{
		// TODO be integrated
		return true;
	}
	
	/**
	 * Sets the npc pos.
	 * @param player the new npc pos
	 */
	public static void setNpcPos(PlayerInstance player)
	{
		_npcX = player.getX();
		_npcY = player.getY();
		_npcZ = player.getZ();
		_npcHeading = player.getHeading();
	}
	
	/**
	 * Spawn event npc.
	 */
	private static void spawnEventNpc()
	{
		final NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);
		
		try
		{
			_npcSpawn = new Spawn(tmpl);
			_npcSpawn.setX(_npcX);
			_npcSpawn.setY(_npcY);
			_npcSpawn.setZ(_npcZ);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(_npcHeading);
			_npcSpawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(_eventName);
			_npcSpawn.getLastSpawn()._isEventMobDM = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUse(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			LOGGER.warning(_eventName + " Engine[spawnEventNpc(exception: " + e.getMessage());
		}
	}
	
	/**
	 * Unspawn event npc.
	 */
	private static void unspawnEventNpc()
	{
		if ((_npcSpawn == null) || (_npcSpawn.getLastSpawn() == null))
		{
			return;
		}
		
		_npcSpawn.getLastSpawn().deleteMe();
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
	}
	
	/**
	 * Start join.
	 * @return true, if successful
	 */
	public static boolean startJoin()
	{
		if (!checkStartJoinOk())
		{
			return false;
		}
		
		_inProgress = true;
		_joining = true;
		spawnEventNpc();
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Event " + _eventName + "!");
		if (Config.DM_ANNOUNCE_REWARD && (ItemTable.getInstance().getTemplate(_rewardId) != null))
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		}
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Recruiting levels: " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName);
		if (Config.DM_COMMAND)
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Commands .dmjoin .dmleave .dminfo");
		}
		
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": FULL BUFF Event: be ready with your buffs, they won't be deleted!!!");
		return true;
	}
	
	/**
	 * Start teleport.
	 * @return true, if successful
	 */
	public static boolean startTeleport()
	{
		if (!_joining || _started || _teleport)
		{
			return false;
		}
		
		removeOfflinePlayers();
		
		if (!_teamEvent)
		{
			synchronized (_players)
			{
				final int size = _players.size();
				if (!checkMinPlayers(size))
				{
					Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + size);
					if (Config.DM_STATS_LOGGER)
					{
						LOGGER.info(_eventName + ":Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + size);
					}
					
					return false;
				}
			}
		}
		
		_joining = false;
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Teleport to team spot in 20 seconds!");
		setUserData();
		ThreadPool.schedule(() ->
		{
			sit();
			afterTeleportOperations();
			
			synchronized (_players)
			{
				for (PlayerInstance player : _players)
				{
					if (player != null)
					{
						// Remove Summon's buffs
						if (Config.DM_ON_START_UNSUMMON_PET && (player.getPet() != null))
						{
							final Summon summon = player.getPet();
							summon.stopAllEffects();
							
							if (summon instanceof PetInstance)
							{
								summon.unSummon(player);
							}
						}
						
						if (Config.DM_ON_START_REMOVE_ALL_EFFECTS)
						{
							player.stopAllEffects();
						}
						
						// Remove player from his party
						if (player.getParty() != null)
						{
							final Party party = player.getParty();
							party.removePartyMember(player);
						}
						
						if (!_teamEvent)
						{
							final int offset = Config.DM_SPAWN_OFFSET;
							player.teleToLocation(_playerX + Rnd.get(offset), _playerY + Rnd.get(offset), _playerZ);
						}
					}
				}
			}
		}, 20000);
		_teleport = true;
		return true;
	}
	
	/**
	 * After teleport operations.
	 */
	protected static void afterTeleportOperations()
	{
	}
	
	/**
	 * Start event.
	 * @return true, if successful
	 */
	public static boolean startEvent()
	{
		if (!startEventOk())
		{
			return false;
		}
		
		_teleport = false;
		sit();
		removeParties();
		
		afterStartOperations();
		
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Started. Go to kill your enemies!");
		_started = true;
		return true;
	}
	
	/**
	 * Removes the parties.
	 */
	private static void removeParties()
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player.getParty() != null)
				{
					final Party party = player.getParty();
					party.removePartyMember(player);
				}
			}
		}
	}
	
	/**
	 * After start operations.
	 */
	private static void afterStartOperations()
	{
	}
	
	/**
	 * Restarts Event checks if event was aborted. and if true cancels restart task
	 */
	public static synchronized void restartEvent()
	{
		LOGGER.info(_eventName + ": Event has been restarted...");
		_joining = false;
		_started = false;
		_inProgress = false;
		_aborted = false;
		final long delay = _intervalBetweenMatches;
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": joining period will be avaible again in " + _intervalBetweenMatches + " minute(s)!");
		waiter(delay);
		
		try
		{
			if (!_aborted)
			{
				autoEvent(); // start a new event
			}
			else
			{
				Announcements.getInstance().criticalAnnounceToAll(_eventName + ": next event aborted!");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(_eventName + ": Error While Trying to restart Event... " + e);
		}
	}
	
	/**
	 * Finish event.
	 */
	public static void finishEvent()
	{
		if (!finishEventOk())
		{
			return;
		}
		
		_started = false;
		_aborted = false;
		unspawnEventNpc();
		
		afterFinishOperations();
		
		if (_teamEvent)
		{
			processTopTeam();
		}
		else
		{
			processTopPlayer();
			
			if (_topKills != 0)
			{
				String winners = "";
				for (PlayerInstance winner : _topPlayers)
				{
					winners = winners + " " + winner.getName();
				}
				Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + winners + " win the match! " + _topKills + " kills.");
				rewardPlayer();
				
				if (Config.DM_STATS_LOGGER)
				{
					LOGGER.info("**** " + _eventName + " ****");
					LOGGER.info(_eventName + ": " + winners + " win the match! " + _topKills + " kills.");
				}
			}
			else
			{
				Announcements.getInstance().criticalAnnounceToAll(_eventName + ": No players win the match(nobody killed).");
				if (Config.DM_STATS_LOGGER)
				{
					LOGGER.info(_eventName + ": No players win the match(nobody killed).");
				}
			}
		}
		
		teleportFinish();
	}
	
	/**
	 * After finish operations.
	 */
	private static void afterFinishOperations()
	{
	}
	
	/**
	 * Abort event.
	 */
	public static void abortEvent()
	{
		if (!_joining && !_teleport && !_started)
		{
			return;
		}
		
		if (_joining && !_teleport && !_started)
		{
			unspawnEventNpc();
			cleanDM();
			_joining = false;
			_inProgress = false;
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Match aborted!");
			return;
		}
		_joining = false;
		_teleport = false;
		_started = false;
		_aborted = true;
		unspawnEventNpc();
		
		afterFinish();
		
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Match aborted!");
		teleportFinish();
	}
	
	/**
	 * After finish.
	 */
	private static void afterFinish()
	{
	}
	
	/**
	 * Teleport finish.
	 */
	public static void teleportFinish()
	{
		sit();
		
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Teleport back to participation NPC in 20 seconds!");
		removeUserData();
		ThreadPool.schedule(() ->
		{
			synchronized (_players)
			{
				for (PlayerInstance player : _players)
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.teleToLocation(_npcX, _npcY, _npcZ, false);
						}
						else
						{
							try (Connection con = DatabaseFactory.getConnection())
							{
								final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
								statement.setInt(1, _npcX);
								statement.setInt(2, _npcY);
								statement.setInt(3, _npcZ);
								statement.setString(4, player.getName());
								statement.execute();
								statement.close();
							}
							catch (Exception e)
							{
								LOGGER.warning(e.getMessage());
							}
						}
					}
				}
			}
			
			sit();
			cleanDM();
		}, 20000);
	}
	
	/**
	 * Auto event.
	 */
	public static void autoEvent()
	{
		LOGGER.info("Starting " + _eventName + "!");
		LOGGER.info("Matchs Are Restarted At Every: " + getIntervalBetweenMatchs() + " Minutes.");
		if (checkAutoEventStartJoinOk() && startJoin() && !_aborted)
		{
			if (_joinTime > 0)
			{
				waiter(_joinTime * 60 * 1000); // minutes for join event
			}
			else if (_joinTime <= 0)
			{
				LOGGER.info(_eventName + ": join time <=0 aborting event.");
				abortEvent();
				return;
			}
			if (startTeleport() && !_aborted)
			{
				waiter(30 * 1000); // 30 sec wait time untill start fight after teleported
				if (startEvent() && !_aborted)
				{
					LOGGER.warning(_eventName + ": waiting.....minutes for event time " + _eventTime);
					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();
					
					LOGGER.info(_eventName + ": waiting... delay for final messages ");
					waiter(60000); // just a give a delay delay for final messages
					sendFinalMessages();
					
					if (!_started && !_aborted) // if is not already started and it's not aborted
					{
						LOGGER.info(_eventName + ": waiting.....delay for restart event  " + _intervalBetweenMatches + " minutes.");
						waiter(60000); // just a give a delay to next restart
						
						try
						{
							if (!_aborted)
							{
								restartEvent();
							}
						}
						catch (Exception e)
						{
							LOGGER.warning("Error while tying to Restart Event " + e);
						}
					}
				}
			}
			else if (!_aborted)
			{
				abortEvent();
				restartEvent();
			}
		}
	}
	
	/**
	 * Event once start.
	 */
	public static void eventOnceStart()
	{
		if (startJoin() && !_aborted)
		{
			if (_joinTime > 0)
			{
				waiter(_joinTime * 60 * 1000); // minutes for join event
			}
			else if (_joinTime <= 0)
			{
				abortEvent();
				return;
			}
			if (startTeleport() && !_aborted)
			{
				waiter(1 * 60 * 1000); // 1 min wait time untill start fight after teleported
				if (startEvent() && !_aborted)
				{
					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();
				}
			}
			else if (!_aborted)
			{
				abortEvent();
			}
		}
	}
	
	/**
	 * Waiter.
	 * @param interval the interval
	 */
	private static void waiter(long interval)
	{
		final long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (((startWaiterTime + interval) > System.currentTimeMillis()) && !_aborted)
		{
			seconds--; // Here because we don't want to see two time announce at the same time
			if (_joining || _started || _teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
					{
						removeOfflinePlayers();
						if (_joining)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + (seconds / 60 / 60) + " hour(s) till registration close!");
						}
						else if (_started)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + (seconds / 60 / 60) + " hour(s) till event finish!");
						}
						break;
					}
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
					{
						if (_joining)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + "!");
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + (seconds / 60) + " minute(s) till registration close!");
						}
						else if (_started)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + (seconds / 60) + " minute(s) till event finish!");
						}
						break;
					}
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
					{
						removeOfflinePlayers();
						// fallthrough?
					}
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
					{
						if (_joining)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + seconds + " second(s) till registration close!");
						}
						else if (_teleport)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + seconds + " seconds(s) till start fight!");
						}
						else if (_started)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + seconds + " second(s) till event finish!");
						}
						break;
					}
				}
			}
			
			final long startOneSecondWaiterStartTime = System.currentTimeMillis();
			
			// Only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while ((startOneSecondWaiterStartTime + 1000) > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}
	
	/**
	 * Sit.
	 */
	public static void sit()
	{
		if (_sitForced)
		{
			_sitForced = false;
		}
		else
		{
			_sitForced = true;
		}
		
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player != null)
				{
					if (_sitForced)
					{
						player.stopMove(null, false);
						player.abortAttack();
						player.abortCast();
						
						if (!player.isSitting())
						{
							player.sitDown();
						}
					}
					else if (player.isSitting())
					{
						player.standUp();
					}
				}
			}
		}
	}
	
	/**
	 * Removes the offline players.
	 */
	public static void removeOfflinePlayers()
	{
		try
		{
			synchronized (_players)
			{
				if ((_players == null) || _players.isEmpty())
				{
					return;
				}
				
				final List<PlayerInstance> toBeRemoved = new ArrayList<>();
				for (PlayerInstance player : _players)
				{
					if (player == null)
					{
						continue;
					}
					
					if ((player._inEventDM && !player.isOnline()) || player.isInJail() || player.isInOfflineMode())
					{
						if (!_joining)
						{
							player.getAppearance().setNameColor(player._originalNameColorDM);
							player.setTitle(player._originalTitleDM);
							player.setKarma(player._originalKarmaDM);
							
							player.broadcastUserInfo();
						}
						
						// after remove, all event data must be cleaned in player
						player._originalNameColorDM = 0;
						player._originalTitleDM = null;
						player._originalKarmaDM = 0;
						player._countDMkills = 0;
						player._inEventDM = false;
						toBeRemoved.add(player);
						
						player.sendMessage("Your participation in the DeathMatch event has been removed.");
					}
				}
				_players.removeAll(toBeRemoved);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Start event ok.
	 * @return true, if successful
	 */
	private static boolean startEventOk()
	{
		return !_joining && _teleport && !_started;
	}
	
	/**
	 * Finish event ok.
	 * @return true, if successful
	 */
	private static boolean finishEventOk()
	{
		return _started;
	}
	
	/**
	 * Adds the player ok.
	 * @param eventPlayer the event player
	 * @return true, if successful
	 */
	private static boolean addPlayerOk(PlayerInstance eventPlayer)
	{
		if (eventPlayer.isAio() && !Config.ALLOW_AIO_IN_EVENTS)
		{
			eventPlayer.sendMessage("AIO charactes are not allowed to participate in events :/");
		}
		if (eventPlayer._inEventDM)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		
		if (eventPlayer._inEventTvT || eventPlayer._inEventCTF)
		{
			eventPlayer.sendMessage("You already participated to another event!");
			return false;
		}
		
		if (Olympiad.getInstance().isRegistered(eventPlayer) || eventPlayer.isInOlympiadMode())
		{
			eventPlayer.sendMessage("You already participated in Olympiad!");
			return false;
		}
		
		if ((eventPlayer._activeBoxes > 1) && !Config.ALLOW_DUALBOX_EVENT)
		{
			final List<String> playerBoxes = eventPlayer._activeBoxeCharacters;
			if ((playerBoxes != null) && (playerBoxes.size() > 1))
			{
				for (String characterName : playerBoxes)
				{
					final PlayerInstance player = World.getInstance().getPlayer(characterName);
					if ((player != null) && player._inEventDM)
					{
						eventPlayer.sendMessage("You already participated in event with another char!");
						return false;
					}
				}
			}
		}
		
		if (!Config.DM_ALLOW_HEALER_CLASSES && ((eventPlayer.getClassId() == ClassId.CARDINAL) || (eventPlayer.getClassId() == ClassId.EVA_SAINT) || (eventPlayer.getClassId() == ClassId.SHILLIEN_SAINT)))
		{
			eventPlayer.sendMessage("You cant join with Healer Class!");
			return false;
		}
		
		synchronized (_players)
		{
			if (_players.contains(eventPlayer))
			{
				eventPlayer.sendMessage("You already participated in the event!");
				return false;
			}
			
			for (PlayerInstance player : _players)
			{
				if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer.sendMessage("You already participated in the event!");
					return false;
				}
				else if (player.getName().equalsIgnoreCase(eventPlayer.getName()))
				{
					eventPlayer.sendMessage("You already participated in the event!");
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Sets the user data.
	 */
	public static void setUserData()
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				player._originalNameColorDM = player.getAppearance().getNameColor();
				player._originalKarmaDM = player.getKarma();
				player._originalTitleDM = player.getTitle();
				player.getAppearance().setNameColor(_playerColors);
				player.setKarma(0);
				player.setTitle("Kills: " + player._countDMkills);
				if (player.isMounted() && player.setMountType(0))
				{
					if (player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					}
					
					player.broadcastPacket(new Ride(player.getObjectId(), Ride.ACTION_DISMOUNT, 0));
					player.setMountObjectID(0);
				}
				player.broadcastUserInfo();
			}
		}
	}
	
	/**
	 * Dump data.
	 */
	public static void dumpData()
	{
		LOGGER.info("");
		LOGGER.info("");
		
		if (!_joining && !_teleport && !_started)
		{
			LOGGER.info("<<---------------------------------->>");
			LOGGER.info(">> " + _eventName + " Engine infos dump (INACTIVE) <<");
			LOGGER.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (_joining && !_teleport && !_started)
		{
			LOGGER.info("<<--------------------------------->>");
			LOGGER.info(">> " + _eventName + " Engine infos dump (JOINING) <<");
			LOGGER.info("<<--^----^^-----^----^^------^----->>");
		}
		else if (!_joining && _teleport && !_started)
		{
			LOGGER.info("<<---------------------------------->>");
			LOGGER.info(">> " + _eventName + " Engine infos dump (TELEPORT) <<");
			LOGGER.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (!_joining && !_teleport && _started)
		{
			LOGGER.info("<<--------------------------------->>");
			LOGGER.info(">> " + _eventName + " Engine infos dump (STARTED) <<");
			LOGGER.info("<<--^----^^-----^----^^------^----->>");
		}
		
		LOGGER.info("Name: " + _eventName);
		LOGGER.info("Desc: " + _eventDesc);
		LOGGER.info("Join location: " + _joiningLocationName);
		LOGGER.info("Min lvl: " + _minlvl);
		LOGGER.info("Max lvl: " + _maxlvl);
		
		LOGGER.info("");
		LOGGER.info("##################################");
		LOGGER.info("# _players(List<PlayerInstance>) #");
		LOGGER.info("##################################");
		
		synchronized (_players)
		{
			LOGGER.info("Total Players : " + _players.size());
			for (PlayerInstance player : _players)
			{
				if (player != null)
				{
					LOGGER.info("Name: " + player.getName() + " kills :" + player._countDMkills);
				}
			}
		}
		
		LOGGER.info("");
		LOGGER.info("################################");
		LOGGER.info("# _savePlayers(List<String>) #");
		LOGGER.info("################################");
		
		for (String player : _savePlayers)
		{
			LOGGER.info("Name: " + player);
		}
		
		LOGGER.info("");
		LOGGER.info("");
		
		dumpLocalEventInfo();
	}
	
	/**
	 * Dump local event info.
	 */
	private static void dumpLocalEventInfo()
	{
	}
	
	/**
	 * Load data.
	 */
	public static void loadData()
	{
		_eventName = "";
		_eventDesc = "";
		_joiningLocationName = "";
		_savePlayers = new ArrayList<>();
		
		synchronized (_players)
		{
			_players.clear();
		}
		
		_topPlayers = new ArrayList<>();
		_npcSpawn = null;
		_joining = false;
		_teleport = false;
		_started = false;
		_sitForced = false;
		_aborted = false;
		_inProgress = false;
		_npcId = 0;
		_npcX = 0;
		_npcY = 0;
		_npcZ = 0;
		_npcHeading = 0;
		_rewardId = 0;
		_rewardAmount = 0;
		_topKills = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_joinTime = 0;
		_eventTime = 0;
		_minPlayers = 0;
		_maxPlayers = 0;
		_intervalBetweenMatches = 0;
		_playerColors = 0;
		_playerX = 0;
		_playerY = 0;
		_playerZ = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from dm");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_eventName = rs.getString("eventName");
				_eventDesc = rs.getString("eventDesc");
				_joiningLocationName = rs.getString("joiningLocation");
				_minlvl = rs.getInt("minlvl");
				_maxlvl = rs.getInt("maxlvl");
				_npcId = rs.getInt("npcId");
				_npcX = rs.getInt("npcX");
				_npcY = rs.getInt("npcY");
				_npcZ = rs.getInt("npcZ");
				_npcHeading = rs.getInt("npcHeading");
				_rewardId = rs.getInt("rewardId");
				_rewardAmount = rs.getInt("rewardAmount");
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_minPlayers = rs.getInt("minPlayers");
				_maxPlayers = rs.getInt("maxPlayers");
				_playerColors = rs.getInt("color");
				_playerX = rs.getInt("playerX");
				_playerY = rs.getInt("playerY");
				_playerZ = rs.getInt("playerZ");
				_intervalBetweenMatches = rs.getInt("delayForNextEvent");
			}
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: DM.loadData(): " + e.getMessage());
		}
	}
	
	/**
	 * Save data.
	 */
	public static void saveData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("Delete from dm");
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("INSERT INTO dm (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, joinTime, eventTime, minPlayers, maxPlayers, color, playerX, playerY, playerZ, delayForNextEvent ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, _eventName);
			statement.setString(2, _eventDesc);
			statement.setString(3, _joiningLocationName);
			statement.setInt(4, _minlvl);
			statement.setInt(5, _maxlvl);
			statement.setInt(6, _npcId);
			statement.setInt(7, _npcX);
			statement.setInt(8, _npcY);
			statement.setInt(9, _npcZ);
			statement.setInt(10, _npcHeading);
			statement.setInt(11, _rewardId);
			statement.setInt(12, _rewardAmount);
			statement.setInt(13, _joinTime);
			statement.setInt(14, _eventTime);
			statement.setInt(15, _minPlayers);
			statement.setInt(16, _maxPlayers);
			statement.setInt(17, _playerColors);
			statement.setInt(18, _playerX);
			statement.setInt(19, _playerY);
			statement.setInt(20, _playerZ);
			statement.setLong(21, _intervalBetweenMatches);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: DM.saveData(): " + e.getMessage());
		}
	}
	
	/**
	 * Show event html.
	 * @param eventPlayer the event player
	 * @param objectId the object id
	 */
	public static void showEventHtml(PlayerInstance eventPlayer, String objectId)
	{
		try
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuilder replyMSG = new StringBuilder("<html><title>" + _eventName + "</title><body>");
			replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br1>");
			replyMSG.append("<center><font color=\"3366CC\">Current event:</font></center><br1>");
			replyMSG.append("<center>Name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font></center><br1>");
			replyMSG.append("<center>Description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font></center><br><br>");
			replyMSG.append("<center>Event Type:&nbsp;<font color=\"00FF00\"> Full Buff Event!!! </font></center><br><br>");
			
			synchronized (_players)
			{
				if (!_started && !_joining)
				{
					replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
				}
				else if (!checkMaxPlayers(_players.size()))
				{
					if (!_started)
					{
						replyMSG.append("Currently participated: <font color=\"00FF00\">" + _players.size() + ".</font><br>");
						replyMSG.append("Max players: <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
						replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
					}
				}
				else if (eventPlayer.isCursedWeaponEquiped() && !Config.DM_JOIN_CURSED)
				{
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
				}
				else if (!_started && _joining && (eventPlayer.getLevel() >= _minlvl) && (eventPlayer.getLevel() <= _maxlvl))
				{
					if (_players.contains(eventPlayer))
					{
						replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
						replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + _players.size() + "</font></center><br>");
						replyMSG.append("<table border=\"0\"><tr>");
						replyMSG.append("<td width=\"200\">Wait till event start or</td>");
						replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_dmevent_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
						replyMSG.append("<td width=\"100\">your participation!</td>");
						replyMSG.append("</tr></table>");
					}
					else
					{
						replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + _players.size() + "</font></center><br>");
						replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
						replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + _minlvl + "</font></center></td><br>");
						replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font></center></td><br><br>");
						replyMSG.append("<center><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_dmevent_player_join\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center><br>");
					}
				}
				else if (_started && !_joining)
				{
					replyMSG.append("<center>" + _eventName + " match is in progress.</center>");
				}
				else if ((eventPlayer.getLevel() < _minlvl) || (eventPlayer.getLevel() > _maxlvl))
				{
					replyMSG.append("Your lvl: <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
					replyMSG.append("Min lvl: <font color=\"00FF00\">" + _minlvl + "</font><br>");
					replyMSG.append("Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
			
			// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
			eventPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
		catch (Exception e)
		{
			LOGGER.warning(_eventName + " Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
	
	/**
	 * Adds the player.
	 * @param player the player
	 */
	public static void addPlayer(PlayerInstance player)
	{
		if (!addPlayerOk(player))
		{
			return;
		}
		
		synchronized (_players)
		{
			_players.add(player);
		}
		
		player._inEventDM = true;
		player._countDMkills = 0;
		_savePlayers.add(player.getName());
		player.sendMessage("DM: You successfully registered for the DeathMatch event.");
	}
	
	/**
	 * Removes the player.
	 * @param player the player
	 */
	public static void removePlayer(PlayerInstance player)
	{
		if ((player != null) && player._inEventDM)
		{
			if (!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorDM);
				player.setTitle(player._originalTitleDM);
				player.setKarma(player._originalKarmaDM);
				
				player.broadcastUserInfo();
			}
			
			// after remove, all event data must be cleaned in player
			player._originalNameColorDM = 0;
			player._originalTitleDM = null;
			player._originalKarmaDM = 0;
			player._countDMkills = 0;
			player._inEventDM = false;
			
			synchronized (_players)
			{
				_players.remove(player);
			}
			
			player.sendMessage("Your participation in the DeathMatch event has been removed.");
		}
	}
	
	/**
	 * Clean dm.
	 */
	public static void cleanDM()
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player != null)
				{
					cleanEventPlayer(player);
					
					if (player._inEventDM)
					{
						if (!_joining)
						{
							player.getAppearance().setNameColor(player._originalNameColorDM);
							player.setTitle(player._originalTitleDM);
							player.setKarma(player._originalKarmaDM);
							
							player.broadcastUserInfo();
						}
						
						// after remove, all event data must be cleaned in player
						player._originalNameColorDM = 0;
						player._originalTitleDM = null;
						player._originalKarmaDM = 0;
						player._countDMkills = 0;
						player._inEventDM = false;
						player.sendMessage("Your participation in the DeathMatch event has been removed.");
					}
					
					if (_savePlayers.contains(player.getName()))
					{
						_savePlayers.remove(player.getName());
					}
					player._inEventDM = false;
				}
			}
			
			_players.clear();
		}
		
		_topKills = 0;
		_savePlayers = new ArrayList<>();
		_topPlayers = new ArrayList<>();
		cleanLocalEventInfo();
		
		_inProgress = false;
		loadData();
	}
	
	/**
	 * Clean local event info.
	 */
	private static void cleanLocalEventInfo()
	{
		// nothing
	}
	
	/**
	 * Clean event player.
	 * @param player the player
	 */
	private static void cleanEventPlayer(PlayerInstance player)
	{
		// nothing
	}
	
	/**
	 * Adds the disconnected player.
	 * @param player the player
	 */
	public static void addDisconnectedPlayer(PlayerInstance player)
	{
		synchronized (_players)
		{
			if (!_players.contains(player) && _savePlayers.contains(player.getName()))
			{
				if (Config.DM_ON_START_REMOVE_ALL_EFFECTS)
				{
					player.stopAllEffects();
				}
				
				_players.add(player);
				
				player._originalNameColorDM = player.getAppearance().getNameColor();
				player._originalTitleDM = player.getTitle();
				player._originalKarmaDM = player.getKarma();
				player._inEventDM = true;
				player._countDMkills = 0;
				if (_teleport || _started)
				{
					player.setTitle("Kills: " + player._countDMkills);
					player.getAppearance().setNameColor(_playerColors);
					player.setKarma(0);
					player.broadcastUserInfo();
					player.teleToLocation(_playerX + Rnd.get(Config.DM_SPAWN_OFFSET), _playerY + Rnd.get(Config.DM_SPAWN_OFFSET), _playerZ);
				}
			}
		}
	}
	
	/**
	 * Gets the _player colors.
	 * @return the _playerColors
	 */
	public static int getPlayerColors()
	{
		return _playerColors;
	}
	
	/**
	 * Set_player colors.
	 * @param playerColors the _playerColors to set
	 * @return true, if successful
	 */
	public static boolean setPlayerColors(int playerColors)
	{
		if (!_inProgress)
		{
			DM._playerColors = playerColors;
			return true;
		}
		return false;
	}
	
	/**
	 * Reward player.
	 */
	public static void rewardPlayer()
	{
		for (PlayerInstance _topPlayer : _topPlayers)
		{
			_topPlayer.addItem("DM Event: " + _eventName, _rewardId, _rewardAmount, _topPlayer, true);
			
			final StatusUpdate su = new StatusUpdate(_topPlayer.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, _topPlayer.getCurrentLoad());
			_topPlayer.sendPacket(su);
			
			final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
			final StringBuilder replyMSG = new StringBuilder("");
			replyMSG.append("<html><body>You won the event. Look in your inventory for the reward.</body></html>");
			nhm.setHtml(replyMSG.toString());
			_topPlayer.sendPacket(nhm);
			
			// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
			_topPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Process top player.
	 */
	private static void processTopPlayer()
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player._countDMkills > _topKills)
				{
					_topPlayers.clear();
					_topPlayers.add(player);
					_topKills = player._countDMkills;
				}
				else if (player._countDMkills == _topKills)
				{
					if (!_topPlayers.contains(player))
					{
						_topPlayers.add(player);
					}
				}
			}
		}
	}
	
	/**
	 * Process top team.
	 */
	private static void processTopTeam()
	{
	}
	
	/**
	 * Gets the _players spawn location.
	 * @return the _players spawn location
	 */
	public static Location getPlayersSpawnLocation()
	{
		return new Location(_playerX + Rnd.get(Config.DM_SPAWN_OFFSET), _playerY + Rnd.get(Config.DM_SPAWN_OFFSET), _playerZ, 0);
	}
	
	/**
	 * Sets the players pos.
	 * @param player the new players pos
	 */
	public static void setPlayersPos(PlayerInstance player)
	{
		_playerX = player.getX();
		_playerY = player.getY();
		_playerZ = player.getZ();
	}
	
	/**
	 * Removes the user data.
	 */
	public static void removeUserData()
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				player.getAppearance().setNameColor(player._originalNameColorDM);
				player.setTitle(player._originalTitleDM);
				player.setKarma(player._originalKarmaDM);
				player._inEventDM = false;
				player._countDMkills = 0;
				player.broadcastUserInfo();
			}
		}
	}
	
	/**
	 * just an announcer to send termination messages.
	 */
	public static void sendFinalMessages()
	{
		if (!_started && !_aborted)
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Thank you For participating!");
		}
	}
	
	/**
	 * returns the interval between each event.
	 * @return the interval between matchs
	 */
	public static int getIntervalBetweenMatchs()
	{
		final long actualTime = System.currentTimeMillis();
		final long totalTime = actualTime + _intervalBetweenMatches;
		final long interval = totalTime - actualTime;
		final int seconds = (int) (interval / 1000);
		return seconds / 60;
	}
	
	/**
	 * Sets the event start time.
	 * @param newTime the new event start time
	 */
	public void setEventStartTime(String newTime)
	{
		startEventTime = newTime;
	}
	
	@Override
	public String getEventIdentifier()
	{
		return _eventName;
	}
	
	@Override
	public void run()
	{
		LOGGER.info("DM: Event notification start");
		eventOnceStart();
	}
	
	@Override
	public String getEventStartTime()
	{
		return startEventTime;
	}
	
	/**
	 * On disconnect.
	 * @param player the player
	 */
	public static void onDisconnect(PlayerInstance player)
	{
		if (player._inEventDM)
		{
			removePlayer(player);
			player.teleToLocation(_npcX, _npcY, _npcZ);
		}
	}
}