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
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Inventory;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.Radar;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.entity.event.manager.EventTask;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.RadarControl;
import org.l2jserver.gameserver.network.serverpackets.Ride;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class CTF implements EventTask
{
	protected static final Logger LOGGER = Logger.getLogger(CTF.class.getName());
	
	protected static String _eventName = "";
	protected static String _eventDesc = "";
	protected static String _joiningLocationName = "";
	private static Spawn _npcSpawn;
	protected static boolean _joining = false;
	protected static boolean _teleport = false;
	protected static boolean _started = false;
	protected static boolean _aborted = false;
	protected static boolean _sitForced = false;
	protected static boolean _inProgress = false;
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
	protected static long _intervalBetweenMatches = 0;
	private String startEventTime;
	protected static boolean _teamEvent = true; // TODO to be integrated
	public static List<PlayerInstance> _players = new ArrayList<>();
	private static String _topTeam = "";
	public static List<PlayerInstance> _playersShuffle = new ArrayList<>();
	public static List<String> _teams = new ArrayList<>();
	public static List<String> _savePlayers = new ArrayList<>();
	public static List<String> _savePlayerTeams = new ArrayList<>();
	public static List<Integer> _teamPlayersCount = new ArrayList<>();
	public static List<Integer> _teamColors = new ArrayList<>();
	public static List<Integer> _teamsX = new ArrayList<>();
	public static List<Integer> _teamsY = new ArrayList<>();
	public static List<Integer> _teamsZ = new ArrayList<>();
	public static List<Integer> _teamPointsCount = new ArrayList<>();
	public static int _topScore = 0;
	public static int _eventCenterX = 0;
	public static int _eventCenterY = 0;
	public static int _eventCenterZ = 0;
	public static int _eventOffset = 0;
	private static int _FlagNPC = 35062;
	private static int _FLAG_IN_HAND_ITEM_ID = 6718;
	public static List<Integer> _flagIds = new ArrayList<>();
	public static List<Integer> _flagsX = new ArrayList<>();
	public static List<Integer> _flagsY = new ArrayList<>();
	public static List<Integer> _flagsZ = new ArrayList<>();
	public static List<Spawn> _flagSpawns = new ArrayList<>();
	public static List<Spawn> _throneSpawns = new ArrayList<>();
	public static List<Boolean> _flagsTaken = new ArrayList<>();
	
	/**
	 * Instantiates a new cTF.
	 */
	private CTF()
	{
	}
	
	/**
	 * Gets the new instance.
	 * @return the new instance
	 */
	public static CTF getNewInstance()
	{
		return new CTF();
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
			CTF._eventName = eventName;
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
			CTF._eventDesc = eventDesc;
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
			CTF._joiningLocationName = joiningLocationName;
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
			CTF._npcId = npcId;
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
			CTF._rewardId = rewardId;
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
			CTF._rewardAmount = rewardAmount;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _minlvl.
	 * @return the _minlvl
	 */
	public static int getMinLvl()
	{
		return _minlvl;
	}
	
	/**
	 * Set_minlvl.
	 * @param minlvl the _minlvl to set
	 * @return true, if successful
	 */
	public static boolean setMinLvl(int minlvl)
	{
		if (!_inProgress)
		{
			CTF._minlvl = minlvl;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _maxlvl.
	 * @return the _maxlvl
	 */
	public static int getMaxLvl()
	{
		return _maxlvl;
	}
	
	/**
	 * Set_maxlvl.
	 * @param maxlvl the _maxlvl to set
	 * @return true, if successful
	 */
	public static boolean setMaxLvl(int maxlvl)
	{
		if (!_inProgress)
		{
			CTF._maxlvl = maxlvl;
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
			CTF._joinTime = joinTime;
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
			CTF._eventTime = eventTime;
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
			CTF._minPlayers = minPlayers;
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
			CTF._maxPlayers = maxPlayers;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the _interval between matches.
	 * @return the _intervalBetweenMatches
	 */
	public static long getIntervalBetweenMatches()
	{
		return _intervalBetweenMatches;
	}
	
	/**
	 * Set_interval between matches.
	 * @param intervalBetweenMatches the _intervalBetweenMatches to set
	 * @return true, if successful
	 */
	public static boolean setIntervalBetweenMatches(long intervalBetweenMatches)
	{
		if (!_inProgress)
		{
			CTF._intervalBetweenMatches = intervalBetweenMatches;
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
	public static boolean isStarted()
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
		return (_teams.size() >= 2) && !_teamsX.contains(0) && !_teamsY.contains(0) && !_teamsZ.contains(0);
	}
	
	/**
	 * Check start join player info.
	 * @return true, if successful
	 */
	private static boolean checkStartJoinPlayerInfo()
	{
		// TODO be integrated
		return true;
	}
	
	/**
	 * Check auto event start join ok.
	 * @return true, if successful
	 */
	protected static boolean checkAutoEventStartJoinOk()
	{
		return (_joinTime != 0) && (_eventTime != 0);
	}
	
	/**
	 * Check optional event start join ok.
	 * @return true, if successful
	 */
	private static boolean checkOptionalEventStartJoinOk()
	{
		try
		{
			if (_flagsX.contains(0) || _flagsY.contains(0) || _flagsZ.contains(0) || _flagIds.contains(0))
			{
				return false;
			}
			if ((_flagsX.size() < _teams.size()) || (_flagsY.size() < _teams.size()) || (_flagsZ.size() < _teams.size()) || (_flagIds.size() < _teams.size()))
			{
				return false;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return false;
		}
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
			_npcSpawn.getLastSpawn()._isEventMobCTF = true;
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
		if (Config.CTF_ANNOUNCE_REWARD && (ItemTable.getInstance().getTemplate(_rewardId) != null))
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		}
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Recruiting levels: " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + ".");
		if (Config.CTF_COMMAND)
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Commands .ctfjoin .ctfleave .ctfinfo!");
		}
		
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
		
		if (_teamEvent)
		{
			if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
			{
				shuffleTeams();
			}
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
			{
				Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
				if (Config.CTF_STATS_LOGGER)
				{
					LOGGER.info(_eventName + ":Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
				}
				
				return false;
			}
		}
		else if (!checkMinPlayers(_players.size()))
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _players.size());
			if (Config.CTF_STATS_LOGGER)
			{
				LOGGER.info(_eventName + ":Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _players.size());
			}
			return false;
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
						if (Config.CTF_ON_START_UNSUMMON_PET && (player.getPet() != null))
						{
							final Summon summon = player.getPet();
							for (Effect e1 : summon.getAllEffects())
							{
								if (e1 != null)
								{
									e1.exit(true);
								}
							}
							
							if (summon instanceof PetInstance)
							{
								summon.unSummon(player);
							}
						}
						
						if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
						{
							for (Effect e2 : player.getAllEffects())
							{
								if (e2 != null)
								{
									e2.exit(true);
								}
							}
						}
						
						// Remove player from his party
						if (player.getParty() != null)
						{
							final Party party = player.getParty();
							party.removePartyMember(player);
						}
						
						if (_teamEvent)
						{
							final int offset = Config.CTF_SPAWN_OFFSET;
							player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(offset), _teamsY.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(offset), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
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
		spawnAllFlags();
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
		
		afterStartOperations();
		
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Started. Go Capture the Flags!");
		_started = true;
		return true;
	}
	
	/**
	 * After start operations.
	 */
	private static void afterStartOperations()
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player != null)
				{
					player._teamNameHaveFlagCTF = null;
					player._haveFlagCTF = false;
				}
			}
		}
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
			
			if (_topScore != 0)
			{
				playKneelAnimation(_topTeam);
				
				if (Config.CTF_ANNOUNCE_TEAM_STATS)
				{
					Announcements.getInstance().criticalAnnounceToAll(_eventName + " Team Statistics:");
					for (String team : _teams)
					{
						final int _flags_ = teamPointsCount(team);
						Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Team: " + team + " - Flags taken: " + _flags_);
					}
				}
				
				if (_topTeam != null)
				{
					Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Team " + _topTeam + " wins the match, with " + _topScore + " flags taken!");
				}
				else
				{
					Announcements.getInstance().criticalAnnounceToAll(_eventName + ": The event finished with a TIE: " + _topScore + " flags taken by each team!");
				}
				rewardTeam(_topTeam);
				
				if (Config.CTF_STATS_LOGGER)
				{
					LOGGER.info("**** " + _eventName + " ****");
					LOGGER.info(_eventName + " Team Statistics:");
					for (String team : _teams)
					{
						final int _flags_ = teamPointsCount(team);
						LOGGER.info("Team: " + team + " - Flags taken: " + _flags_);
					}
					
					LOGGER.info(_eventName + ": Team " + _topTeam + " wins the match, with " + _topScore + " flags taken!");
				}
			}
			else
			{
				Announcements.getInstance().criticalAnnounceToAll(_eventName + ": The event finished with a TIE: No team wins the match(nobody took flags)!");
				if (Config.CTF_STATS_LOGGER)
				{
					LOGGER.info(_eventName + ": No team win the match(nobody took flags).");
				}
				
				rewardTeam(_topTeam);
			}
		}
		else
		{
			processTopPlayer();
		}
		
		teleportFinish();
	}
	
	/**
	 * After finish operations.
	 */
	private static void afterFinishOperations()
	{
		unspawnAllFlags();
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
			cleanCTF();
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
		unspawnAllFlags();
	}
	
	/**
	 * Teleport finish.
	 */
	public static void teleportFinish()
	{
		sit();
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Teleport back to participation NPC in 20 seconds!");
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
			cleanCTF();
		}, 20000);
	}
	
	protected static class AutoEventTask implements Runnable
	{
		@Override
		public void run()
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
						
						if (!_started && !_aborted)
						{ // if is not already started and it's not aborted
							
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
	}
	
	/**
	 * Auto event.
	 */
	public static void autoEvent()
	{
		ThreadPool.execute(new AutoEventTask());
	}
	
	// start without restart
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
	protected static void waiter(long interval)
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
						// fallthrou?
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
			if ((_playersShuffle == null) || _playersShuffle.isEmpty())
			{
				return;
			}
			
			for (PlayerInstance player : _playersShuffle)
			{
				if (player == null)
				{
					_playersShuffle.remove(player);
				}
				else if (!player.isOnline() || player.isInJail() || player.isInOfflineMode())
				{
					removePlayer(player);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
	}
	
	/**
	 * Start event ok.
	 * @return true, if successful
	 */
	private static boolean startEventOk()
	{
		if (_joining || !_teleport || _started)
		{
			return false;
		}
		
		if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			if (_teamPlayersCount.contains(0))
			{
				return false;
			}
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
		{
			final List<PlayerInstance> playersShuffleTemp = new ArrayList<>();
			int loopCount = 0;
			loopCount = _playersShuffle.size();
			for (int i = 0; i < loopCount; i++)
			{
				playersShuffleTemp.add(_playersShuffle.get(i));
			}
			
			_playersShuffle = playersShuffleTemp;
			playersShuffleTemp.clear();
		}
		return true;
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
	 * @param teamName the team name
	 * @param eventPlayer the event player
	 * @return true, if successful
	 */
	private static boolean addPlayerOk(String teamName, PlayerInstance eventPlayer)
	{
		if (eventPlayer.isAio() && !Config.ALLOW_AIO_IN_EVENTS)
		{
			eventPlayer.sendMessage("AIO charactes are not allowed to participate in events :/");
		}
		if (checkShufflePlayers(eventPlayer) || eventPlayer._inEventCTF)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		
		if (eventPlayer._inEventTvT || eventPlayer._inEventDM)
		{
			eventPlayer.sendMessage("You already participated in another event!");
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
					if ((player != null) && player._inEventCTF)
					{
						eventPlayer.sendMessage("You already participated in event with another char!");
						return false;
					}
				}
			}
		}
		
		synchronized (_players)
		{
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
			
			if (_players.contains(eventPlayer))
			{
				eventPlayer.sendMessage("You already participated in the event!");
				return false;
			}
		}
		
		if (_savePlayers.contains(eventPlayer.getName()))
		{
			eventPlayer.sendMessage("You already participated in another event!");
			return false;
		}
		
		if (Config.CTF_EVEN_TEAMS.equals("NO"))
		{
			return true;
		}
		else if (Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			boolean allTeamsEqual = true;
			int countBefore = -1;
			for (int playersCount : _teamPlayersCount)
			{
				if (countBefore == -1)
				{
					countBefore = playersCount;
				}
				
				if (countBefore != playersCount)
				{
					allTeamsEqual = false;
					break;
				}
				
				countBefore = playersCount;
			}
			
			if (allTeamsEqual)
			{
				return true;
			}
			
			countBefore = Integer.MAX_VALUE;
			for (int teamPlayerCount : _teamPlayersCount)
			{
				if (teamPlayerCount < countBefore)
				{
					countBefore = teamPlayerCount;
				}
			}
			
			final List<String> joinableTeams = new ArrayList<>();
			for (String team : _teams)
			{
				if (teamPlayersCount(team) == countBefore)
				{
					joinableTeams.add(team);
				}
			}
			
			if (joinableTeams.contains(teamName))
			{
				return true;
			}
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
		{
			return true;
		}
		
		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
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
				player._originalNameColorCTF = player.getAppearance().getNameColor();
				player._originalKarmaCTF = player.getKarma();
				player._originalTitleCTF = player.getTitle();
				player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameCTF)));
				player.setKarma(0);
				if (Config.CTF_AURA && (_teams.size() >= 2))
				{
					player.setTeam(_teams.indexOf(player._teamNameCTF) + 1);
				}
				
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
		LOGGER.info("##########################");
		LOGGER.info("# _teams(List<String>) #");
		LOGGER.info("##########################");
		
		for (String team : _teams)
		{
			LOGGER.info(team + " Flags Taken :" + _teamPointsCount.get(_teams.indexOf(team)));
		}
		
		if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
		{
			LOGGER.info("");
			LOGGER.info("#########################################");
			LOGGER.info("# _playersShuffle(List<PlayerInstance>) #");
			LOGGER.info("#########################################");
			
			for (PlayerInstance player : _playersShuffle)
			{
				if (player != null)
				{
					LOGGER.info("Name: " + player.getName());
				}
			}
		}
		
		LOGGER.info("");
		LOGGER.info("##################################");
		LOGGER.info("# _players(List<PlayerInstance>) #");
		LOGGER.info("##################################");
		
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player != null)
				{
					LOGGER.info("Name: " + player.getName() + "   Team: " + player._teamNameCTF + "  Flags :" + player._countCTFflags);
				}
			}
		}
		
		LOGGER.info("");
		LOGGER.info("#####################################################################");
		LOGGER.info("# _savePlayers(List<String>) and _savePlayerTeams(List<String>) #");
		LOGGER.info("#####################################################################");
		
		for (String player : _savePlayers)
		{
			LOGGER.info("Name: " + player + "	Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));
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
		LOGGER.info("**********==CTF==************");
		LOGGER.info("CTF._teamPointsCount:" + _teamPointsCount);
		LOGGER.info("CTF._flagIds:" + _flagIds);
		LOGGER.info("CTF._flagSpawns:" + _flagSpawns);
		LOGGER.info("CTF._throneSpawns:" + _throneSpawns);
		LOGGER.info("CTF._flagsTaken:" + _flagsTaken);
		LOGGER.info("CTF._flagsX:" + _flagsX);
		LOGGER.info("CTF._flagsY:" + _flagsY);
		LOGGER.info("CTF._flagsZ:" + _flagsZ);
		LOGGER.info("************EOF**************\n");
		LOGGER.info("");
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
			_players = new ArrayList<>();
		}
		
		_topTeam = "";
		_teams = new ArrayList<>();
		_savePlayerTeams = new ArrayList<>();
		_playersShuffle = new ArrayList<>();
		_teamPlayersCount = new ArrayList<>();
		_teamPointsCount = new ArrayList<>();
		_teamColors = new ArrayList<>();
		_teamsX = new ArrayList<>();
		_teamsY = new ArrayList<>();
		_teamsZ = new ArrayList<>();
		_throneSpawns = new ArrayList<>();
		_flagSpawns = new ArrayList<>();
		_flagsTaken = new ArrayList<>();
		_flagIds = new ArrayList<>();
		_flagsX = new ArrayList<>();
		_flagsY = new ArrayList<>();
		_flagsZ = new ArrayList<>();
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
		_topScore = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_joinTime = 0;
		_eventTime = 0;
		_minPlayers = 0;
		_maxPlayers = 0;
		_intervalBetweenMatches = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from ctf");
			rs = statement.executeQuery();
			int teams = 0;
			
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
				teams = rs.getInt("teamsCount");
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_minPlayers = rs.getInt("minPlayers");
				_maxPlayers = rs.getInt("maxPlayers");
				_intervalBetweenMatches = rs.getLong("delayForNextEvent");
			}
			statement.close();
			
			int index = -1;
			if (teams > 0)
			{
				index = 0;
			}
			while ((index < teams) && (index > -1))
			{
				statement = con.prepareStatement("Select * from ctf_teams where teamId = ?");
				statement.setInt(1, index);
				rs = statement.executeQuery();
				while (rs.next())
				{
					_teams.add(rs.getString("teamName"));
					_teamPlayersCount.add(0);
					_teamPointsCount.add(0);
					_teamColors.add(0);
					_teamsX.add(0);
					_teamsY.add(0);
					_teamsZ.add(0);
					_teamsX.set(index, rs.getInt("teamX"));
					_teamsY.set(index, rs.getInt("teamY"));
					_teamsZ.set(index, rs.getInt("teamZ"));
					_teamColors.set(index, rs.getInt("teamColor"));
					_flagsX.add(0);
					_flagsY.add(0);
					_flagsZ.add(0);
					_flagsX.set(index, rs.getInt("flagX"));
					_flagsY.set(index, rs.getInt("flagY"));
					_flagsZ.set(index, rs.getInt("flagZ"));
					_flagSpawns.add(null);
					_flagIds.add(_FlagNPC);
					_flagsTaken.add(false);
				}
				index++;
				statement.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: loadData(): " + e.getMessage());
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
			statement = con.prepareStatement("Delete from ctf");
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("INSERT INTO ctf (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers,delayForNextEvent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
			statement.setInt(13, _teams.size());
			statement.setInt(14, _joinTime);
			statement.setInt(15, _eventTime);
			statement.setInt(16, _minPlayers);
			statement.setInt(17, _maxPlayers);
			statement.setLong(18, _intervalBetweenMatches);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("Delete from ctf_teams");
			statement.execute();
			statement.close();
			
			for (String teamName : _teams)
			{
				final int index = _teams.indexOf(teamName);
				if (index == -1)
				{
					return;
				}
				statement = con.prepareStatement("INSERT INTO ctf_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor, flagX, flagY, flagZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, index);
				statement.setString(2, teamName);
				statement.setInt(3, _teamsX.get(index));
				statement.setInt(4, _teamsY.get(index));
				statement.setInt(5, _teamsZ.get(index));
				statement.setInt(6, _teamColors.get(index));
				statement.setInt(7, _flagsX.get(index));
				statement.setInt(8, _flagsY.get(index));
				statement.setInt(9, _flagsZ.get(index));
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: saveData(): " + e.getMessage());
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
			if (!_started && !_joining)
			{
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			}
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
			{
				if (!_started)
				{
					replyMSG.append("Currently participated: <font color=\"00FF00\">" + _playersShuffle.size() + ".</font><br>");
					replyMSG.append("Max players: <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			else if (eventPlayer.isCursedWeaponEquiped() && !Config.CTF_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			}
			else if (!_started && _joining && (eventPlayer.getLevel() >= _minlvl) && (eventPlayer.getLevel() <= _maxlvl))
			{
				synchronized (_players)
				{
					if (_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
					{
						if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
						{
							replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer._teamNameCTF + "</font><br><br>");
						}
						else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
						{
							replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
						}
						
						replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + _playersShuffle.size() + "</font></center><br>");
						replyMSG.append("<center><font color=\"3366CC\">Wait till event start or remove your participation!</font><center>");
						replyMSG.append("<center><button value=\"Remove\" action=\"bypass -h npc_" + objectId + "_ctf_player_leave\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
					}
					else
					{
						replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
						replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + _minlvl + "</font></center></td><br>");
						replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font></center></td><br><br>");
						replyMSG.append("<center><font color=\"3366CC\">Teams:</font></center><br>");
						if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
						{
							replyMSG.append("<center><table border=\"0\">");
							for (String team : _teams)
							{
								replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
								replyMSG.append("<center><td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join " + team + "\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td></tr>");
							}
							replyMSG.append("</table></center>");
						}
						else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
						{
							replyMSG.append("<center>");
							
							for (String team : _teams)
							{
								replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font> &nbsp;</td>");
							}
							
							replyMSG.append("</center><br>");
							
							replyMSG.append("<center><button value=\"Join Event\" action=\"bypass -h npc_" + objectId + "_ctf_player_join eventShuffle\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
							replyMSG.append("<center><font color=\"3366CC\">Teams will be reandomly generated!</font></center><br>");
							replyMSG.append("<center>Joined Players:</font> <font color=\"LEVEL\">" + _playersShuffle.size() + "</center></font><br>");
							replyMSG.append("<center>Reward: <font color=\"LEVEL\">" + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName() + "</center></font>");
						}
					}
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
	 * @param teamName the team name
	 */
	public static void addPlayer(PlayerInstance player, String teamName)
	{
		if (!addPlayerOk(teamName, player))
		{
			return;
		}
		
		synchronized (_players)
		{
			if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
			{
				player._teamNameCTF = teamName;
				_players.add(player);
				setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
			}
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
			{
				_playersShuffle.add(player);
			}
		}
		
		player._inEventCTF = true;
		player._countCTFflags = 0;
		player.sendMessage(_eventName + ": You successfully registered for the event.");
	}
	
	/**
	 * Removes the player.
	 * @param player the player
	 */
	public static void removePlayer(PlayerInstance player)
	{
		if (player._inEventCTF)
		{
			if (!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorCTF);
				player.setTitle(player._originalTitleCTF);
				player.setKarma(player._originalKarmaCTF);
				if (Config.CTF_AURA && (_teams.size() >= 2))
				{
					player.setTeam(0); // clear aura :P
				}
				player.broadcastUserInfo();
			}
			
			// after remove, all event data must be cleaned in player
			player._originalNameColorCTF = 0;
			player._originalTitleCTF = null;
			player._originalKarmaCTF = 0;
			player._teamNameCTF = "";
			player._countCTFflags = 0;
			player._inEventCTF = false;
			
			synchronized (_players)
			{
				if ((Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
				{
					setTeamPlayersCount(player._teamNameCTF, teamPlayersCount(player._teamNameCTF) - 1);
					_players.remove(player);
				}
				else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (!_playersShuffle.isEmpty() && _playersShuffle.contains(player)))
				{
					_playersShuffle.remove(player);
				}
			}
			
			player.sendMessage("Your participation in the CTF event has been removed.");
		}
	}
	
	/**
	 * Clean ctf.
	 */
	public static void cleanCTF()
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player != null)
				{
					cleanEventPlayer(player);
					
					removePlayer(player);
					if (_savePlayers.contains(player.getName()))
					{
						_savePlayers.remove(player.getName());
					}
					player._inEventCTF = false;
				}
			}
		}
		
		if ((_playersShuffle != null) && !_playersShuffle.isEmpty())
		{
			for (PlayerInstance player : _playersShuffle)
			{
				if (player != null)
				{
					player._inEventCTF = false;
				}
			}
		}
		
		_topScore = 0;
		_topTeam = "";
		synchronized (_players)
		{
			_players = new ArrayList<>();
		}
		
		_playersShuffle = new ArrayList<>();
		_savePlayers = new ArrayList<>();
		_savePlayerTeams = new ArrayList<>();
		_teamPointsCount = new ArrayList<>();
		_teamPlayersCount = new ArrayList<>();
		cleanLocalEventInfo();
		
		_inProgress = false;
		loadData();
	}
	
	/**
	 * Clean local event info.
	 */
	private static void cleanLocalEventInfo()
	{
		_flagSpawns = new ArrayList<>();
		_flagsTaken = new ArrayList<>();
	}
	
	/**
	 * Clean event player.
	 * @param player the player
	 */
	private static void cleanEventPlayer(PlayerInstance player)
	{
		if (player._haveFlagCTF)
		{
			removeFlagFromPlayer(player);
		}
		else
		{
			player.getInventory().destroyItemByItemId("", _FLAG_IN_HAND_ITEM_ID, 1, player, null);
		}
		player._haveFlagCTF = false;
	}
	
	/**
	 * Adds the disconnected player.
	 * @param player the player
	 */
	public static synchronized void addDisconnectedPlayer(PlayerInstance player)
	{
		if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started)) || (Config.CTF_EVEN_TEAMS.equals("NO") || (Config.CTF_EVEN_TEAMS.equals("BALANCE") && (_teleport || _started))))
		{
			if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
			{
				player.stopAllEffects();
			}
			
			player._teamNameCTF = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			
			synchronized (_players)
			{
				for (PlayerInstance p : _players)
				{
					// check by name incase player got new objectId
					if ((p != null) && p.getName().equals(player.getName()))
					{
						player._originalNameColorCTF = player.getAppearance().getNameColor();
						player._originalTitleCTF = player.getTitle();
						player._originalKarmaCTF = player.getKarma();
						player._inEventCTF = true;
						player._countCTFflags = p._countCTFflags;
						_players.remove(p); // removing old object id from vector
						_players.add(player); // adding new objectId to vector
						break;
					}
				}
			}
			
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameCTF)));
			player.setKarma(0);
			if (Config.CTF_AURA && (_teams.size() >= 2))
			{
				player.setTeam(_teams.indexOf(player._teamNameCTF) + 1);
			}
			player.broadcastUserInfo();
			
			final int offset = Config.CTF_SPAWN_OFFSET;
			player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(offset), _teamsY.get(_teams.indexOf(player._teamNameCTF)) + Rnd.get(offset), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
			afterAddDisconnectedPlayerOperations(player);
		}
	}
	
	/**
	 * After add disconnected player operations.
	 * @param player the player
	 */
	private static void afterAddDisconnectedPlayerOperations(PlayerInstance player)
	{
		player._teamNameHaveFlagCTF = null;
		player._haveFlagCTF = false;
		checkRestoreFlags();
	}
	
	/**
	 * Shuffle teams.
	 */
	public static void shuffleTeams()
	{
		int teamCount = 0;
		int playersCount = 0;
		
		synchronized (_players)
		{
			for (;;)
			{
				if (_playersShuffle.isEmpty())
				{
					break;
				}
				
				final int playerToAddIndex = Rnd.get(_playersShuffle.size());
				PlayerInstance player = null;
				player = _playersShuffle.get(playerToAddIndex);
				_players.add(player);
				_players.get(playersCount)._teamNameCTF = _teams.get(teamCount);
				_savePlayers.add(_players.get(playersCount).getName());
				_savePlayerTeams.add(_teams.get(teamCount));
				playersCount++;
				
				if (teamCount == (_teams.size() - 1))
				{
					teamCount = 0;
				}
				else
				{
					teamCount++;
				}
				
				_playersShuffle.remove(playerToAddIndex);
			}
		}
	}
	
	/**
	 * Play kneel animation.
	 * @param teamName the team name
	 */
	public static void playKneelAnimation(String teamName)
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if (player != null)
				{
					if (!player._teamNameCTF.equals(teamName))
					{
						player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
					}
					else if (player._teamNameCTF.equals(teamName))
					{
						player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
					}
				}
			}
		}
	}
	
	/**
	 * Reward team.
	 * @param teamName the team name
	 */
	public static void rewardTeam(String teamName)
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if ((player != null) && player.isOnline() && (player._inEventCTF))
				{
					if ((teamName != null) && (player._teamNameCTF.equals(teamName)))
					{
						player.addItem(_eventName + " Event: " + _eventName, _rewardId, _rewardAmount, player, true);
						
						final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
						final StringBuilder replyMSG = new StringBuilder("");
						replyMSG.append("<html><body>");
						replyMSG.append("<font color=\"FFFF00\">Your team wins the event. Look in your inventory for the reward.</font>");
						replyMSG.append("</body></html>");
						
						nhm.setHtml(replyMSG.toString());
						player.sendPacket(nhm);
						
						// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (teamName == null)
					{
						int minusReward = 0;
						if (_topScore != 0)
						{
							minusReward = _rewardAmount / 2;
						}
						else
						{
							// nobody took flags
							minusReward = _rewardAmount / 4;
						}
						
						player.addItem(_eventName + " Event: " + _eventName, _rewardId, minusReward, player, true);
						
						final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
						final StringBuilder replyMSG = new StringBuilder("");
						replyMSG.append("<html><body>");
						replyMSG.append("<font color=\"FFFF00\">Your team had a tie in the event. Look in your inventory for the reward.</font>");
						replyMSG.append("</body></html>");
						
						nhm.setHtml(replyMSG.toString());
						player.sendPacket(nhm);
						
						// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
			}
		}
	}
	
	/**
	 * Process top player.
	 */
	private static void processTopPlayer()
	{
		//
	}
	
	/**
	 * Process top team.
	 */
	private static void processTopTeam()
	{
		_topTeam = null;
		for (String team : _teams)
		{
			if ((teamPointsCount(team) == _topScore) && (_topScore > 0))
			{
				_topTeam = null;
			}
			
			if (teamPointsCount(team) > _topScore)
			{
				_topTeam = team;
				_topScore = teamPointsCount(team);
			}
		}
	}
	
	/**
	 * Adds the team.
	 * @param teamName the team name
	 */
	public static void addTeam(String teamName)
	{
		if (_inProgress)
		{
			return;
		}
		
		if (teamName.equals(" "))
		{
			return;
		}
		
		_teams.add(teamName);
		_teamPlayersCount.add(0);
		_teamPointsCount.add(0);
		_teamColors.add(0);
		_teamsX.add(0);
		_teamsY.add(0);
		_teamsZ.add(0);
		
		addTeamEventOperations(teamName);
	}
	
	/**
	 * Adds the team event operations.
	 * @param teamName the team name
	 */
	private static void addTeamEventOperations(String teamName)
	{
		addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, 0, 0, 0);
	}
	
	/**
	 * Removes the team.
	 * @param teamName the team name
	 */
	public static void removeTeam(String teamName)
	{
		if (_inProgress || _teams.isEmpty())
		{
			return;
		}
		
		if (teamPlayersCount(teamName) > 0)
		{
			return;
		}
		
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		
		_teamsZ.remove(index);
		_teamsY.remove(index);
		_teamsX.remove(index);
		_teamColors.remove(index);
		_teamPointsCount.remove(index);
		_teamPlayersCount.remove(index);
		_teams.remove(index);
		
		removeTeamEventItems(teamName);
	}
	
	/**
	 * Removes the team event items.
	 * @param teamName the team name
	 */
	private static void removeTeamEventItems(String teamName)
	{
		final int index = _teams.indexOf(teamName);
		_flagSpawns.remove(index);
		_flagsTaken.remove(index);
		_flagIds.remove(index);
		_flagsX.remove(index);
		_flagsY.remove(index);
		_flagsZ.remove(index);
	}
	
	/**
	 * Sets the team pos.
	 * @param teamName the team name
	 * @param player the player
	 */
	public static void setTeamPos(String teamName, PlayerInstance player)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		
		_teamsX.set(index, player.getX());
		_teamsY.set(index, player.getY());
		_teamsZ.set(index, player.getZ());
	}
	
	/**
	 * Sets the team pos.
	 * @param teamName the team name
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public static void setTeamPos(String teamName, int x, int y, int z)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		
		_teamsX.set(index, x);
		_teamsY.set(index, y);
		_teamsZ.set(index, z);
	}
	
	/**
	 * Sets the team color.
	 * @param teamName the team name
	 * @param color the color
	 */
	public static void setTeamColor(String teamName, int color)
	{
		if (_inProgress)
		{
			return;
		}
		
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		
		_teamColors.set(index, color);
	}
	
	/**
	 * Team players count.
	 * @param teamName the team name
	 * @return the int
	 */
	public static int teamPlayersCount(String teamName)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return -1;
		}
		return _teamPlayersCount.get(index);
	}
	
	/**
	 * Sets the team players count.
	 * @param teamName the team name
	 * @param teamPlayersCount the team players count
	 */
	public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		
		_teamPlayersCount.set(index, teamPlayersCount);
	}
	
	/**
	 * Check shuffle players.
	 * @param eventPlayer the event player
	 * @return true, if successful
	 */
	public static boolean checkShufflePlayers(PlayerInstance eventPlayer)
	{
		try
		{
			for (PlayerInstance player : _playersShuffle)
			{
				if ((player == null) || !player.isOnline())
				{
					_playersShuffle.remove(player);
					eventPlayer._inEventCTF = false;
				}
				else if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer._inEventCTF = true;
					eventPlayer._countCTFflags = 0;
					return true;
				}
				// This 1 is incase player got new objectid after DC or reconnect
				else if (player.getName().equals(eventPlayer.getName()))
				{
					_playersShuffle.remove(player);
					_playersShuffle.add(eventPlayer);
					eventPlayer._inEventCTF = true;
					eventPlayer._countCTFflags = 0;
					return true;
				}
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}
	
	/**
	 * just an announcer to send termination messages.
	 */
	public static void sendFinalMessages()
	{
		if (!_started && !_aborted)
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Thank you For Participating At, " + _eventName + " Event.");
		}
	}
	
	/**
	 * returns the interval between each event.
	 * @return the interval between matches
	 */
	public static int getIntervalBetweenMatchs()
	{
		final long actualTime = System.currentTimeMillis();
		final long totalTime = actualTime + _intervalBetweenMatches;
		final long interval = totalTime - actualTime;
		final int seconds = (int) (interval / 1000);
		return seconds / 60;
	}
	
	@Override
	public void run()
	{
		LOGGER.info(_eventName + ": Event notification start");
		eventOnceStart();
	}
	
	@Override
	public String getEventIdentifier()
	{
		return _eventName;
	}
	
	@Override
	public String getEventStartTime()
	{
		return startEventTime;
	}
	
	/**
	 * Sets the event start time.
	 * @param newTime the new event start time
	 */
	public void setEventStartTime(String newTime)
	{
		startEventTime = newTime;
	}
	
	/**
	 * On disconnect.
	 * @param player the player
	 */
	public static void onDisconnect(PlayerInstance player)
	{
		if (player._inEventCTF)
		{
			removePlayer(player);
			player.teleToLocation(_npcX, _npcY, _npcZ);
		}
	}
	
	/**
	 * Team points count.
	 * @param teamName the team name
	 * @return the int
	 */
	public static int teamPointsCount(String teamName)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return -1;
		}
		return _teamPointsCount.get(index);
	}
	
	/**
	 * Sets the team points count.
	 * @param teamName the team name
	 * @param teamPointCount the team point count
	 */
	public static void setTeamPointsCount(String teamName, int teamPointCount)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		
		_teamPointsCount.set(index, teamPointCount);
	}
	
	/**
	 * Gets the _event offset.
	 * @return the _eventOffset
	 */
	public static int getEventOffset()
	{
		return _eventOffset;
	}
	
	/**
	 * Set_event offset.
	 * @param eventOffset the _eventOffset to set
	 * @return true, if successful
	 */
	public static boolean setEventOffset(int eventOffset)
	{
		if (!_inProgress)
		{
			CTF._eventOffset = eventOffset;
			return true;
		}
		return false;
	}
	
	/**
	 * Show flag html.
	 * @param eventPlayer the event player
	 * @param objectId the object id
	 * @param teamName the team name
	 */
	public static void showFlagHtml(PlayerInstance eventPlayer, String objectId, String teamName)
	{
		if (eventPlayer == null)
		{
			return;
		}
		
		try
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuilder replyMSG = new StringBuilder("<html><head><body><center>");
			replyMSG.append("CTF Flag<br><br>");
			replyMSG.append("<font color=\"00FF00\">" + teamName + "'s Flag</font><br1>");
			if ((eventPlayer._teamNameCTF != null) && eventPlayer._teamNameCTF.equals(teamName))
			{
				replyMSG.append("<font color=\"LEVEL\">This is your Flag</font><br1>");
			}
			else
			{
				replyMSG.append("<font color=\"LEVEL\">Enemy Flag!</font><br1>");
			}
			if (_started)
			{
				processInFlagRange(eventPlayer);
			}
			else
			{
				replyMSG.append("CTF match is not in progress yet.<br>Wait for a GM to start the event<br>");
			}
			replyMSG.append("</center></body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			LOGGER.info("CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception: " + e.getStackTrace());
		}
	}
	
	/**
	 * Check restore flags.
	 */
	public static void checkRestoreFlags()
	{
		final List<Integer> teamsTakenFlag = new ArrayList<>();
		try
		{
			synchronized (_players)
			{
				for (PlayerInstance player : _players)
				{
					if (player != null)
					{
						// logged off with a flag in his hands
						if (!player.isOnline() && player._haveFlagCTF)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + player.getName() + " logged off with a CTF flag!");
							player._haveFlagCTF = false;
							if ((_teams.indexOf(player._teamNameHaveFlagCTF) >= 0) && _flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF)))
							{
								_flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), false);
								spawnFlag(player._teamNameHaveFlagCTF);
								Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + player._teamNameHaveFlagCTF + " flag now returned to place.");
							}
							removeFlagFromPlayer(player);
							player._teamNameHaveFlagCTF = null;
							return;
						}
						else if (player._haveFlagCTF)
						{
							teamsTakenFlag.add(_teams.indexOf(player._teamNameHaveFlagCTF));
						}
					}
				}
			}
			
			// Go over the list of ALL teams
			for (String team : _teams)
			{
				if (team == null)
				{
					continue;
				}
				final int index = _teams.indexOf(team);
				if (!teamsTakenFlag.contains(index) && _flagsTaken.get(index))
				{
					_flagsTaken.set(index, false);
					spawnFlag(team);
					Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + team + " flag returned due to player error.");
				}
			}
			// Check if a player ran away from the event holding a flag:
			synchronized (_players)
			{
				for (PlayerInstance player : _players)
				{
					if ((player != null) && player._haveFlagCTF && isOutsideCTFArea(player))
					{
						Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + player.getName() + " escaped from the event holding a flag!");
						player._haveFlagCTF = false;
						if ((_teams.indexOf(player._teamNameHaveFlagCTF) >= 0) && _flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF)))
						{
							_flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), false);
							spawnFlag(player._teamNameHaveFlagCTF);
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + player._teamNameHaveFlagCTF + " flag now returned to place.");
						}
						removeFlagFromPlayer(player);
						player._teamNameHaveFlagCTF = null;
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
						player.sendMessage("You have been returned to your team spawn");
						return;
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.info("CTF.restoreFlags() Error:" + e);
		}
	}
	
	/**
	 * Adds the flag to player.
	 * @param player the player
	 */
	public static void addFlagToPlayer(PlayerInstance player)
	{
		// Remove items from the player hands (right, left, both)
		// This is NOT a BUG, I don't want them to see the icon they have 8D
		ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
			if (wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LRHAND);
			}
		}
		else
		{
			player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_RHAND);
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if (wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LHAND);
			}
		}
		// Add the flag in his hands
		player.getInventory().equipItem(ItemTable.getInstance().createItem("", _FLAG_IN_HAND_ITEM_ID, 1, player, null));
		player.broadcastPacket(new SocialAction(player.getObjectId(), 16)); // Amazing glow
		player._haveFlagCTF = true;
		player.broadcastUserInfo();
		player.sendPacket(new CreatureSay(player.getObjectId(), ChatType.PARTYROOM_COMMANDER, ":", "You got it! Run back! ::"));
	}
	
	/**
	 * Removes the flag from player.
	 * @param player the player
	 */
	public static void removeFlagFromPlayer(PlayerInstance player)
	{
		final ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		player._haveFlagCTF = false;
		if (wpn != null)
		{
			final ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			player.getInventory().destroyItemByItemId("", _FLAG_IN_HAND_ITEM_ID, 1, player, null);
			final InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			player.sendPacket(iu);
			player.sendPacket(new ItemList(player, true)); // Get your weapon back now ...
			player.abortAttack();
			player.broadcastUserInfo();
		}
		else
		{
			player.getInventory().destroyItemByItemId("", _FLAG_IN_HAND_ITEM_ID, 1, player, null);
			player.sendPacket(new ItemList(player, true)); // Get your weapon back now ...
			player.abortAttack();
			player.broadcastUserInfo();
		}
	}
	
	/**
	 * Sets the team flag.
	 * @param teamName the team name
	 * @param player the player
	 */
	public static void setTeamFlag(String teamName, PlayerInstance player)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, player.getX(), player.getY(), player.getZ());
	}
	
	/**
	 * Spawn all flags.
	 */
	public static void spawnAllFlags()
	{
		while (_flagSpawns.size() < _teams.size())
		{
			_flagSpawns.add(null);
		}
		while (_throneSpawns.size() < _teams.size())
		{
			_throneSpawns.add(null);
		}
		for (String team : _teams)
		{
			final int index = _teams.indexOf(team);
			final NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));
			final NpcTemplate throne = NpcTable.getInstance().getTemplate(32027);
			try
			{
				// Spawn throne
				_throneSpawns.set(index, new Spawn(throne));
				_throneSpawns.get(index).setX(_flagsX.get(index));
				_throneSpawns.get(index).setY(_flagsY.get(index));
				_throneSpawns.get(index).setZ(_flagsZ.get(index) - 10);
				_throneSpawns.get(index).setAmount(1);
				_throneSpawns.get(index).setHeading(0);
				_throneSpawns.get(index).setRespawnDelay(1);
				SpawnTable.getInstance().addNewSpawn(_throneSpawns.get(index), false);
				_throneSpawns.get(index).init();
				_throneSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
				_throneSpawns.get(index).getLastSpawn().decayMe();
				_throneSpawns.get(index).getLastSpawn().spawnMe(_throneSpawns.get(index).getLastSpawn().getX(), _throneSpawns.get(index).getLastSpawn().getY(), _throneSpawns.get(index).getLastSpawn().getZ());
				_throneSpawns.get(index).getLastSpawn().setTitle(team + " Throne");
				_throneSpawns.get(index).getLastSpawn().broadcastPacket(new MagicSkillUse(_throneSpawns.get(index).getLastSpawn(), _throneSpawns.get(index).getLastSpawn(), 1036, 1, 5500, 1));
				_throneSpawns.get(index).getLastSpawn()._isCTF_throneSpawn = true;
				// Spawn flag
				_flagSpawns.set(index, new Spawn(tmpl));
				_flagSpawns.get(index).setX(_flagsX.get(index));
				_flagSpawns.get(index).setY(_flagsY.get(index));
				_flagSpawns.get(index).setZ(_flagsZ.get(index));
				_flagSpawns.get(index).setAmount(1);
				_flagSpawns.get(index).setHeading(0);
				_flagSpawns.get(index).setRespawnDelay(1);
				SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);
				_flagSpawns.get(index).init();
				_flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
				_flagSpawns.get(index).getLastSpawn().setTitle(team + "'s Flag");
				_flagSpawns.get(index).getLastSpawn()._CTF_FlagTeamName = team;
				_flagSpawns.get(index).getLastSpawn().decayMe();
				_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
				_flagSpawns.get(index).getLastSpawn()._isCTF_Flag = true;
				calculateOutSideOfCTF(); // Sets event boundaries so players don't run with the flag.
			}
			catch (Exception e)
			{
				LOGGER.info("CTF Engine[spawnAllFlags()]: exception: " + e);
			}
		}
	}
	
	/**
	 * Unspawn all flags.
	 */
	public static void unspawnAllFlags()
	{
		try
		{
			if ((_throneSpawns == null) || (_flagSpawns == null) || (_teams == null))
			{
				return;
			}
			for (String team : _teams)
			{
				final int index = _teams.indexOf(team);
				if (_throneSpawns.get(index) != null)
				{
					_throneSpawns.get(index).getLastSpawn().deleteMe();
					_throneSpawns.get(index).stopRespawn();
					SpawnTable.getInstance().deleteSpawn(_throneSpawns.get(index), true);
				}
				if (_flagSpawns.get(index) != null)
				{
					_flagSpawns.get(index).getLastSpawn().deleteMe();
					_flagSpawns.get(index).stopRespawn();
					SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
				}
			}
			_throneSpawns.clear();
		}
		catch (Exception e)
		{
			LOGGER.info("CTF Engine[unspawnAllFlags()]: exception: " + e);
		}
	}
	
	/**
	 * Unspawn flag.
	 * @param teamName the team name
	 */
	private static void unspawnFlag(String teamName)
	{
		final int index = _teams.indexOf(teamName);
		_flagSpawns.get(index).getLastSpawn().deleteMe();
		_flagSpawns.get(index).stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
	}
	
	/**
	 * Spawn flag.
	 * @param teamName the team name
	 */
	public static void spawnFlag(String teamName)
	{
		final int index = _teams.indexOf(teamName);
		final NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));
		
		try
		{
			_flagSpawns.set(index, new Spawn(tmpl));
			_flagSpawns.get(index).setX(_flagsX.get(index));
			_flagSpawns.get(index).setY(_flagsY.get(index));
			_flagSpawns.get(index).setZ(_flagsZ.get(index));
			_flagSpawns.get(index).setAmount(1);
			_flagSpawns.get(index).setHeading(0);
			_flagSpawns.get(index).setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);
			_flagSpawns.get(index).init();
			_flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
			_flagSpawns.get(index).getLastSpawn().setTitle(teamName + "'s Flag");
			_flagSpawns.get(index).getLastSpawn()._CTF_FlagTeamName = teamName;
			_flagSpawns.get(index).getLastSpawn()._isCTF_Flag = true;
			_flagSpawns.get(index).getLastSpawn().decayMe();
			_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
		}
		catch (Exception e)
		{
			LOGGER.info("CTF Engine[spawnFlag(" + teamName + ")]: exception: " + e);
		}
	}
	
	/**
	 * In range of flag.
	 * @param player the _player
	 * @param flagIndex the flag index
	 * @param offset the offset
	 * @return true, if successful
	 */
	public static boolean inRangeOfFlag(PlayerInstance player, int flagIndex, int offset)
	{
		return (player.getX() > (_flagsX.get(flagIndex) - offset)) && (player.getX() < (_flagsX.get(flagIndex) + offset)) && (player.getY() > (_flagsY.get(flagIndex) - offset)) && (player.getY() < (_flagsY.get(flagIndex) + offset)) && (player.getZ() > (_flagsZ.get(flagIndex) - offset)) && (player.getZ() < (_flagsZ.get(flagIndex) + offset));
	}
	
	/**
	 * Process in flag range.
	 * @param player the _player
	 */
	public static void processInFlagRange(PlayerInstance player)
	{
		try
		{
			checkRestoreFlags();
			for (String team : _teams)
			{
				if (team.equals(player._teamNameCTF))
				{
					final int indexOwn = _teams.indexOf(player._teamNameCTF);
					// If player is near his team flag holding the enemy flag
					if (inRangeOfFlag(player, indexOwn, 100) && !_flagsTaken.get(indexOwn) && player._haveFlagCTF)
					{
						final int indexEnemy = _teams.indexOf(player._teamNameHaveFlagCTF);
						// Return enemy flag to place
						_flagsTaken.set(indexEnemy, false);
						spawnFlag(player._teamNameHaveFlagCTF);
						// Remove the flag from this player
						player.broadcastPacket(new SocialAction(player.getObjectId(), 16)); // Amazing glow
						player.broadcastUserInfo();
						player.broadcastPacket(new SocialAction(player.getObjectId(), 3)); // Victory
						player.broadcastUserInfo();
						removeFlagFromPlayer(player);
						_teamPointsCount.set(indexOwn, teamPointsCount(team) + 1);
						Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + player.getName() + " scores for " + player._teamNameCTF + ".");
					}
				}
				else
				{
					final int indexEnemy = _teams.indexOf(team);
					// If the player is near a enemy flag
					if (inRangeOfFlag(player, indexEnemy, 100) && !_flagsTaken.get(indexEnemy) && !player._haveFlagCTF && !player.isDead())
					{
						_flagsTaken.set(indexEnemy, true);
						unspawnFlag(team);
						player._teamNameHaveFlagCTF = team;
						addFlagToPlayer(player);
						player.broadcastUserInfo();
						player._haveFlagCTF = true;
						Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + team + " flag taken by " + player.getName() + "...");
						pointTeamTo(player, team);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Point team to.
	 * @param hasFlag the has flag
	 * @param ourFlag the our flag
	 */
	public static void pointTeamTo(PlayerInstance hasFlag, String ourFlag)
	{
		try
		{
			synchronized (_players)
			{
				for (PlayerInstance player : _players)
				{
					if ((player != null) && player.isOnline() && player._teamNameCTF.equals(ourFlag))
					{
						player.sendMessage(hasFlag.getName() + " took your flag!");
						if (player._haveFlagCTF)
						{
							player.sendMessage("You can not return the flag to headquarters, until your flag is returned to it's place.");
							player.sendPacket(new RadarControl(1, 1, player.getX(), player.getY(), player.getZ()));
						}
						else
						{
							player.sendPacket(new RadarControl(0, 1, hasFlag.getX(), hasFlag.getY(), hasFlag.getZ()));
							final Radar rdr = new Radar(player);
							final Radar.RadarOnPlayer radar = rdr.new RadarOnPlayer(hasFlag, player);
							ThreadPool.schedule(radar, 10000 + Rnd.get(30000));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Adds the or set.
	 * @param listSize the list size
	 * @param flagSpawn the flag spawn
	 * @param flagsTaken the flags taken
	 * @param flagId the flag id
	 * @param flagX the flag x
	 * @param flagY the flag y
	 * @param flagZ the flag z
	 */
	private static void addOrSet(int listSize, Spawn flagSpawn, boolean flagsTaken, int flagId, int flagX, int flagY, int flagZ)
	{
		while (_flagsX.size() <= listSize)
		{
			_flagSpawns.add(null);
			_flagsTaken.add(false);
			_flagIds.add(_FlagNPC);
			_flagsX.add(0);
			_flagsY.add(0);
			_flagsZ.add(0);
		}
		_flagSpawns.set(listSize, flagSpawn);
		_flagsTaken.set(listSize, flagsTaken);
		_flagIds.set(listSize, flagId);
		_flagsX.set(listSize, flagX);
		_flagsY.set(listSize, flagY);
		_flagsZ.set(listSize, flagZ);
	}
	
	/**
	 * Used to calculate the event CTF area, so that players don't run off with the flag. Essential, since a player may take the flag just so other teams can't score points. This function is Only called upon ONE time on BEGINING OF EACH EVENT right after we spawn the flags.
	 */
	private static void calculateOutSideOfCTF()
	{
		if ((_teams == null) || (_flagSpawns == null) || (_teamsX == null) || (_teamsY == null) || (_teamsZ == null))
		{
			return;
		}
		final int division = _teams.size() * 2;
		int pos = 0;
		final int[] locX = new int[division];
		final int[] locY = new int[division];
		final int[] locZ = new int[division];
		// Get all coordinates inorder to create a polygon:
		for (Spawn flag : _flagSpawns)
		{
			if (flag == null)
			{
				continue;
			}
			
			locX[pos] = flag.getX();
			locY[pos] = flag.getY();
			locZ[pos] = flag.getZ();
			pos++;
			if (pos > (division / 2))
			{
				break;
			}
		}
		for (int x = 0; x < _teams.size(); x++)
		{
			locX[pos] = _teamsX.get(x);
			locY[pos] = _teamsY.get(x);
			locZ[pos] = _teamsZ.get(x);
			pos++;
			if (pos > division)
			{
				break;
			}
		}
		// Find the polygon center, note that it's not the mathematical center of the polygon,
		// Rather than a point which centers all coordinates:
		int centerX = 0;
		int centerY = 0;
		int centerZ = 0;
		for (int x = 0; x < pos; x++)
		{
			centerX += (locX[x] / division);
			centerY += (locY[x] / division);
			centerZ += (locZ[x] / division);
		}
		// Now let's find the furthest distance from the "center" to the egg shaped sphere
		// Surrounding the polygon, size x1.5 (for maximum logical area to wander...):
		int maxX = 0;
		int maxY = 0;
		int maxZ = 0;
		for (int x = 0; x < pos; x++)
		{
			if (maxX < (2 * Math.abs(centerX - locX[x])))
			{
				maxX = (2 * Math.abs(centerX - locX[x]));
			}
			if (maxY < (2 * Math.abs(centerY - locY[x])))
			{
				maxY = (2 * Math.abs(centerY - locY[x]));
			}
			if (maxZ < (2 * Math.abs(centerZ - locZ[x])))
			{
				maxZ = (2 * Math.abs(centerZ - locZ[x]));
			}
		}
		
		// CenterX,centerY,centerZ are the coordinates of the "event center".
		// So let's save those coordinates to check on the players:
		_eventCenterX = centerX;
		_eventCenterY = centerY;
		_eventCenterZ = centerZ;
		_eventOffset = maxX;
		if (_eventOffset < maxY)
		{
			_eventOffset = maxY;
		}
		if (_eventOffset < maxZ)
		{
			_eventOffset = maxZ;
		}
	}
	
	/**
	 * Checks if is outside ctf area.
	 * @param player the player
	 * @return true, if is outside ctf area
	 */
	public static boolean isOutsideCTFArea(PlayerInstance player)
	{
		return (player == null) || !player.isOnline() || (player.getX() <= (_eventCenterX - _eventOffset)) || (player.getX() >= (_eventCenterX + _eventOffset)) || (player.getY() <= (_eventCenterY - _eventOffset)) || (player.getY() >= (_eventCenterY + _eventOffset)) || (player.getZ() <= (_eventCenterZ - _eventOffset)) || (player.getZ() >= (_eventCenterZ + _eventOffset));
	}
}