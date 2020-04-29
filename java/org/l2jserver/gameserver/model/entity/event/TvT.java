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
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.entity.event.manager.EventTask;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.Ride;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class TvT implements EventTask
{
	protected static final Logger LOGGER = Logger.getLogger(TvT.class.getName());
	
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
	private static boolean _teamEvent = true; // TODO to be integrated
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
	public static int _topKills = 0;
	
	/**
	 * Instantiates a new tvt.
	 */
	private TvT()
	{
	}
	
	/**
	 * Gets the new instance.
	 * @return the new instance
	 */
	public static TvT getNewInstance()
	{
		return new TvT();
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
			TvT._eventName = eventName;
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
			TvT._eventDesc = eventDesc;
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
			TvT._joiningLocationName = joiningLocationName;
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
			TvT._npcId = npcId;
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
			TvT._rewardId = rewardId;
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
			TvT._rewardAmount = rewardAmount;
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
			TvT._minlvl = minlvl;
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
			TvT._maxlvl = maxlvl;
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
			TvT._joinTime = joinTime;
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
			TvT._eventTime = eventTime;
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
			TvT._minPlayers = minPlayers;
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
			TvT._maxPlayers = maxPlayers;
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
			TvT._intervalBetweenMatches = intervalBetweenMatches;
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
			_npcSpawn.getLastSpawn()._isEventMobTvT = true;
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
		if (Config.TVT_ANNOUNCE_REWARD && (ItemTable.getInstance().getTemplate(_rewardId) != null))
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getName());
		}
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Recruiting levels: " + _minlvl + " to " + _maxlvl);
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Joinable in " + _joiningLocationName + ".");
		if (Config.TVT_COMMAND)
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Commands .tvtjoin .tvtleave .tvtinfo");
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
		
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			shuffleTeams();
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			if (Config.CTF_STATS_LOGGER)
			{
				LOGGER.info(_eventName + ":Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			}
			
			return false;
		}
		
		_joining = false;
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Teleport to team spot in 20 seconds!");
		setUserData();
		ThreadPool.schedule(() ->
		{
			sit();
			
			synchronized (_players)
			{
				for (PlayerInstance player : _players)
				{
					if (player != null)
					{
						// Remove Summon's buffs
						if (Config.TVT_ON_START_UNSUMMON_PET && (player.getPet() != null))
						{
							final Summon summon = player.getPet();
							summon.stopAllEffects();
							
							if (summon instanceof PetInstance)
							{
								summon.unSummon(player);
							}
						}
						
						if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
						{
							player.stopAllEffects();
						}
						
						// Remove player from his party
						if (player.getParty() != null)
						{
							final Party party = player.getParty();
							party.removePartyMember(player);
						}
						
						player.teleToLocation((_teamsX.get(_teams.indexOf(player._teamNameTvT)) + Rnd.get(201)) - 100, (_teamsY.get(_teams.indexOf(player._teamNameTvT)) + Rnd.get(201)) - 100, _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
					}
				}
			}
		}, 20000);
		_teleport = true;
		return true;
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
		
		if (Config.TVT_CLOSE_FORT_DOORS)
		{
			closeFortDoors();
		}
		
		if (Config.TVT_CLOSE_ADEN_COLOSSEUM_DOORS)
		{
			closeAdenColosseumDoors();
		}
		
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Started. Go to kill your enemies!");
		_started = true;
		return true;
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
			synchronized (_players)
			{
				final PlayerInstance bestKiller = findBestKiller(_players);
				final PlayerInstance looser = findLooser(_players);
				if (_topKills != 0)
				{
					playKneelAnimation(_topTeam);
					
					if (Config.TVT_ANNOUNCE_TEAM_STATS)
					{
						Announcements.getInstance().criticalAnnounceToAll(_eventName + " Team Statistics:");
						for (String team : _teams)
						{
							final int _kills = teamKillsCount(team);
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Team: " + team + " - Kills: " + _kills);
						}
						
						if (bestKiller != null)
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Top killer: " + bestKiller.getName() + " - Kills: " + bestKiller._countTvTkills);
						}
						if ((looser != null) && (!looser.equals(bestKiller)))
						{
							Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Top looser: " + looser.getName() + " - Dies: " + looser._countTvTdies);
						}
					}
					
					if (_topTeam != null)
					{
						Announcements.getInstance().criticalAnnounceToAll(_eventName + ": " + _topTeam + "'s win the match! " + _topKills + " kills.");
					}
					else
					{
						Announcements.getInstance().criticalAnnounceToAll(_eventName + ": The event finished with a TIE: " + _topKills + " kills by each team!");
					}
					rewardTeam(_topTeam, bestKiller, looser);
					if (Config.TVT_STATS_LOGGER)
					{
						LOGGER.info("**** " + _eventName + " ****");
						LOGGER.info(_eventName + " Team Statistics:");
						for (String team : _teams)
						{
							final int _kills = teamKillsCount(team);
							LOGGER.info("Team: " + team + " - Kills: " + _kills);
						}
						
						if (bestKiller != null)
						{
							LOGGER.info("Top killer: " + bestKiller.getName() + " - Kills: " + bestKiller._countTvTkills);
						}
						if ((looser != null) && (!looser.equals(bestKiller)))
						{
							LOGGER.info("Top looser: " + looser.getName() + " - Dies: " + looser._countTvTdies);
						}
						
						LOGGER.info(_eventName + ": " + _topTeam + "'s win the match! " + _topKills + " kills.");
					}
				}
				else
				{
					Announcements.getInstance().criticalAnnounceToAll(_eventName + ": The event finished with a TIE: No team wins the match(nobody killed)!");
					if (Config.TVT_STATS_LOGGER)
					{
						LOGGER.info(_eventName + ": No team win the match(nobody killed).");
					}
					
					rewardTeam(_topTeam, bestKiller, looser);
				}
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
		if (Config.TVT_OPEN_FORT_DOORS)
		{
			openFortDoors();
		}
		
		if (Config.TVT_OPEN_ADEN_COLOSSEUM_DOORS)
		{
			openAdenColosseumDoors();
		}
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
			cleanTvT();
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
		Announcements.getInstance().criticalAnnounceToAll(_eventName + ": Match aborted!");
		teleportFinish();
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
			cleanTvT();
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
					waiter(30 * 1000); // 30 sec wait time until start fight after teleported
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
	}
	
	/**
	 * Auto event.
	 */
	public static void autoEvent()
	{
		ThreadPool.execute(new AutoEventTask());
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
				catch (InterruptedException e)
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
		
		if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			if (_teamPlayersCount.contains(0))
			{
				return false;
			}
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
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
		if (checkShufflePlayers(eventPlayer) || eventPlayer._inEventTvT)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		
		if (eventPlayer._inEventCTF || eventPlayer._inEventDM)
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
				for (String character_name : playerBoxes)
				{
					final PlayerInstance player = World.getInstance().getPlayer(character_name);
					if ((player != null) && player._inEventTvT)
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
		
		if (Config.TVT_EVEN_TEAMS.equals("NO"))
		{
			return true;
		}
		else if (Config.TVT_EVEN_TEAMS.equals("BALANCE"))
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
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
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
				player._originalNameColorTvT = player.getAppearance().getNameColor();
				player._originalKarmaTvT = player.getKarma();
				player._originalTitleTvT = player.getTitle();
				player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
				player.setKarma(0);
				player.setTitle("Kills: " + player._countTvTkills);
				if (Config.TVT_AURA && (_teams.size() >= 2))
				{
					player.setTeam(_teams.indexOf(player._teamNameTvT) + 1);
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
			LOGGER.info(team + " Kills Done :" + _teamPointsCount.get(_teams.indexOf(team)));
		}
		
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
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
					LOGGER.info("Name: " + player.getName() + "   Team: " + player._teamNameTvT + "  Kills Done:" + player._countTvTkills);
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
		_players = new ArrayList<>();
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
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from tvt");
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
				statement = con.prepareStatement("Select * from tvt_teams where teamId = ?");
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
			statement = con.prepareStatement("Delete from tvt");
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("INSERT INTO tvt (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers,delayForNextEvent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)");
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
			
			statement = con.prepareStatement("Delete from tvt_teams");
			statement.execute();
			statement.close();
			
			for (String teamName : _teams)
			{
				final int index = _teams.indexOf(teamName);
				if (index == -1)
				{
					con.close();
					return;
				}
				statement = con.prepareStatement("INSERT INTO tvt_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor) VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, index);
				statement.setString(2, teamName);
				statement.setInt(3, _teamsX.get(index));
				statement.setInt(4, _teamsY.get(index));
				statement.setInt(5, _teamsZ.get(index));
				statement.setInt(6, _teamColors.get(index));
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
			else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
			{
				if (!_started)
				{
					replyMSG.append("Currently participated: <font color=\"00FF00\">" + _playersShuffle.size() + ".</font><br>");
					replyMSG.append("Max players: <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			else if (eventPlayer.isCursedWeaponEquiped() && !Config.TVT_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			}
			else if (!_started && _joining && (eventPlayer.getLevel() >= _minlvl) && (eventPlayer.getLevel() <= _maxlvl))
			{
				synchronized (_players)
				{
					if (_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
					{
						if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
						{
							replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer._teamNameTvT + "</font><br><br>");
						}
						else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
						{
							replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
						}
						
						replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + _playersShuffle.size() + "</font></center><br>");
						replyMSG.append("<center><font color=\"3366CC\">Wait till event start or remove your participation!</font><center>");
						replyMSG.append("<center><button value=\"Remove\" action=\"bypass -h npc_" + objectId + "_tvt_player_leave\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
					}
					else
					{
						replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
						replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + _minlvl + "</font></center></td><br>");
						replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + _maxlvl + "</font></center></td><br><br>");
						replyMSG.append("<center><font color=\"3366CC\">Teams:</font></center><br>");
						if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
						{
							replyMSG.append("<center><table border=\"0\">");
							for (String team : _teams)
							{
								replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
								replyMSG.append("<center><td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join " + team + "\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td></tr>");
							}
							replyMSG.append("</table></center>");
						}
						else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
						{
							replyMSG.append("<center>");
							
							for (String team : _teams)
							{
								replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font> &nbsp;</td>");
							}
							
							replyMSG.append("</center><br>");
							
							replyMSG.append("<center><button value=\"Join Event\" action=\"bypass -h npc_" + objectId + "_tvt_player_join eventShuffle\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
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
			if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
			{
				player._teamNameTvT = teamName;
				_players.add(player);
				setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
			}
			else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
			{
				_playersShuffle.add(player);
			}
		}
		
		player._inEventTvT = true;
		player._countTvTkills = 0;
		player.sendMessage(_eventName + ": You successfully registered for the event.");
	}
	
	/**
	 * Removes the player.
	 * @param player the player
	 */
	public static void removePlayer(PlayerInstance player)
	{
		if (player._inEventTvT)
		{
			if (!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorTvT);
				player.setTitle(player._originalTitleTvT);
				player.setKarma(player._originalKarmaTvT);
				if (Config.TVT_AURA && (_teams.size() >= 2))
				{
					player.setTeam(0); // clear aura :P
				}
				player.broadcastUserInfo();
			}
			
			// after remove, all event data must be cleaned in player
			player._originalNameColorTvT = 0;
			player._originalTitleTvT = null;
			player._originalKarmaTvT = 0;
			player._teamNameTvT = "";
			player._countTvTkills = 0;
			player._inEventTvT = false;
			
			synchronized (_players)
			{
				if ((Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
				{
					setTeamPlayersCount(player._teamNameTvT, teamPlayersCount(player._teamNameTvT) - 1);
					_players.remove(player);
				}
				else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (!_playersShuffle.isEmpty() && _playersShuffle.contains(player)))
				{
					_playersShuffle.remove(player);
				}
			}
			
			player.sendMessage("Your participation in the TvT event has been removed.");
		}
	}
	
	/**
	 * Clean tvt.
	 */
	public static void cleanTvT()
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
					player._inEventTvT = false;
				}
			}
		}
		
		if ((_playersShuffle != null) && !_playersShuffle.isEmpty())
		{
			for (PlayerInstance player : _playersShuffle)
			{
				if (player != null)
				{
					player._inEventTvT = false;
				}
			}
		}
		
		_topKills = 0;
		_topTeam = "";
		_players = new ArrayList<>();
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
	public static synchronized void addDisconnectedPlayer(PlayerInstance player)
	{
		if ((Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started)) || (Config.TVT_EVEN_TEAMS.equals("NO") || (Config.TVT_EVEN_TEAMS.equals("BALANCE") && (_teleport || _started))))
		{
			if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
			{
				player.stopAllEffects();
			}
			
			player._teamNameTvT = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			
			synchronized (_players)
			{
				for (PlayerInstance p : _players)
				{
					if (p == null)
					{
						continue;
					}
					
					// check by name incase player got new objectId
					if (p.getName().equals(player.getName()))
					{
						player._originalNameColorTvT = player.getAppearance().getNameColor();
						player._originalTitleTvT = player.getTitle();
						player._originalKarmaTvT = player.getKarma();
						player._inEventTvT = true;
						player._countTvTkills = p._countTvTkills;
						_players.remove(p); // removing old object id from vector
						_players.add(player); // adding new objectId to vector
						break;
					}
				}
			}
			
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
			player.setKarma(0);
			if (Config.TVT_AURA && (_teams.size() >= 2))
			{
				player.setTeam(_teams.indexOf(player._teamNameTvT) + 1);
			}
			player.broadcastUserInfo();
			
			player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
			afterAddDisconnectedPlayerOperations(player);
		}
	}
	
	/**
	 * After add disconnected player operations.
	 * @param player the player
	 */
	private static void afterAddDisconnectedPlayerOperations(PlayerInstance player)
	{
		// nothing
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
				_players.get(playersCount)._teamNameTvT = _teams.get(teamCount);
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
					if (!player._teamNameTvT.equals(teamName))
					{
						player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
					}
					else if (player._teamNameTvT.equals(teamName))
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
	 * @param bestKiller the best killer
	 * @param looser the looser
	 */
	public static void rewardTeam(String teamName, PlayerInstance bestKiller, PlayerInstance looser)
	{
		synchronized (_players)
		{
			for (PlayerInstance player : _players)
			{
				if ((player != null) && player.isOnline() && (player._inEventTvT) && (!player.equals(looser)) && ((player._countTvTkills > 0) || Config.TVT_PRICE_NO_KILLS))
				{
					if ((bestKiller != null) && (bestKiller.equals(player)))
					{
						player.addItem(_eventName + " Event: " + _eventName, _rewardId, _rewardAmount, player, true);
						player.addItem(_eventName + " Event: " + _eventName, Config.TVT_TOP_KILLER_REWARD, Config.TVT_TOP_KILLER_QTY, player, true);
					}
					else if ((teamName != null) && (player._teamNameTvT.equals(teamName)))
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
						if (_topKills != 0)
						{
							minusReward = _rewardAmount / 2;
						}
						else
						{
							// nobody killed
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
		// nothing
	}
	
	/**
	 * Process top team.
	 */
	private static void processTopTeam()
	{
		_topTeam = null;
		for (String team : _teams)
		{
			if ((teamKillsCount(team) == _topKills) && (_topKills > 0))
			{
				_topTeam = null;
			}
			
			if (teamKillsCount(team) > _topKills)
			{
				_topTeam = team;
				_topKills = teamKillsCount(team);
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
		// nothing
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
		_teams.indexOf(teamName);
		
		//
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
					eventPlayer._inEventTvT = false;
				}
				else if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
					return true;
				}
				// This 1 is incase player got new objectid after DC or reconnect
				else if (player.getName().equals(eventPlayer.getName()))
				{
					_playersShuffle.remove(player);
					_playersShuffle.add(eventPlayer);
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
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
		if (player._inEventTvT)
		{
			removePlayer(player);
			player.teleToLocation(_npcX, _npcY, _npcZ);
		}
	}
	
	/**
	 * Team kills count.
	 * @param teamName the team name
	 * @return the int
	 */
	public static int teamKillsCount(String teamName)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return -1;
		}
		return _teamPointsCount.get(index);
	}
	
	/**
	 * Sets the team kills count.
	 * @param teamName the team name
	 * @param teamKillsCount the team kills count
	 */
	public static void setTeamKillsCount(String teamName, int teamKillsCount)
	{
		final int index = _teams.indexOf(teamName);
		if (index == -1)
		{
			return;
		}
		
		_teamPointsCount.set(index, teamKillsCount);
	}
	
	/**
	 * Kick player from tvt.
	 * @param playerToKick the player to kick
	 */
	public static void kickPlayerFromTvt(PlayerInstance playerToKick)
	{
		if (playerToKick == null)
		{
			return;
		}
		
		synchronized (_players)
		{
			if (_joining)
			{
				_playersShuffle.remove(playerToKick);
				_players.remove(playerToKick);
				playerToKick._inEventTvT = false;
				playerToKick._teamNameTvT = "";
				playerToKick._countTvTkills = 0;
			}
		}
		
		if (_started || _teleport)
		{
			_playersShuffle.remove(playerToKick);
			removePlayer(playerToKick);
			if (playerToKick.isOnline())
			{
				playerToKick.getAppearance().setNameColor(playerToKick._originalNameColorTvT);
				playerToKick.setKarma(playerToKick._originalKarmaTvT);
				playerToKick.setTitle(playerToKick._originalTitleTvT);
				playerToKick.broadcastUserInfo();
				playerToKick.sendMessage("You have been kicked from the TvT.");
				playerToKick.teleToLocation(_npcX, _npcY, _npcZ, false);
				playerToKick.teleToLocation((_npcX + Rnd.get(201)) - 100, (_npcY + Rnd.get(201)) - 100, _npcZ, false);
			}
		}
	}
	
	/**
	 * Find best killer.
	 * @param players the players
	 * @return the pc instance
	 */
	public static PlayerInstance findBestKiller(List<PlayerInstance> players)
	{
		if (players == null)
		{
			return null;
		}
		PlayerInstance bestKiller = null;
		for (PlayerInstance player : players)
		{
			if ((bestKiller == null) || (bestKiller._countTvTkills < player._countTvTkills))
			{
				bestKiller = player;
			}
		}
		return bestKiller;
	}
	
	/**
	 * Find looser.
	 * @param players the players
	 * @return the pc instance
	 */
	public static PlayerInstance findLooser(List<PlayerInstance> players)
	{
		if (players == null)
		{
			return null;
		}
		PlayerInstance looser = null;
		for (PlayerInstance player : players)
		{
			if ((looser == null) || (looser._countTvTdies < player._countTvTdies))
			{
				looser = player;
			}
		}
		return looser;
	}
	
	/**
	 * The Class TvTTeam.
	 */
	public static class TvTTeam
	{
		/** The kill count. */
		private int killCount = -1;
		
		/** The name. */
		private String name = null;
		
		/**
		 * Instantiates a new tvt team.
		 * @param name the name
		 * @param killCount the kill count
		 */
		TvTTeam(String name, int killCount)
		{
			this.killCount = killCount;
			this.name = name;
		}
		
		/**
		 * Gets the kill count.
		 * @return the kill count
		 */
		public int getKillCount()
		{
			return killCount;
		}
		
		/**
		 * Sets the kill count.
		 * @param killCount the new kill count
		 */
		public void setKillCount(int killCount)
		{
			this.killCount = killCount;
		}
		
		/**
		 * Gets the name.
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Sets the name.
		 * @param name the new name
		 */
		public void setName(String name)
		{
			this.name = name;
		}
	}
	
	/**
	 * Close fort doors.
	 */
	private static void closeFortDoors()
	{
		DoorData.getInstance().getDoor(23170004).closeMe();
		DoorData.getInstance().getDoor(23170005).closeMe();
		DoorData.getInstance().getDoor(23170002).closeMe();
		DoorData.getInstance().getDoor(23170003).closeMe();
		DoorData.getInstance().getDoor(23170006).closeMe();
		DoorData.getInstance().getDoor(23170007).closeMe();
		DoorData.getInstance().getDoor(23170008).closeMe();
		DoorData.getInstance().getDoor(23170009).closeMe();
		DoorData.getInstance().getDoor(23170010).closeMe();
		DoorData.getInstance().getDoor(23170011).closeMe();
		
		try
		{
			Thread.sleep(20);
		}
		catch (InterruptedException ie)
		{
			LOGGER.warning("Error, " + ie.getMessage());
		}
	}
	
	/**
	 * Open fort doors.
	 */
	private static void openFortDoors()
	{
		DoorData.getInstance().getDoor(23170004).openMe();
		DoorData.getInstance().getDoor(23170005).openMe();
		DoorData.getInstance().getDoor(23170002).openMe();
		DoorData.getInstance().getDoor(23170003).openMe();
		DoorData.getInstance().getDoor(23170006).openMe();
		DoorData.getInstance().getDoor(23170007).openMe();
		DoorData.getInstance().getDoor(23170008).openMe();
		DoorData.getInstance().getDoor(23170009).openMe();
		DoorData.getInstance().getDoor(23170010).openMe();
		DoorData.getInstance().getDoor(23170011).openMe();
	}
	
	/**
	 * Close aden colosseum doors.
	 */
	private static void closeAdenColosseumDoors()
	{
		DoorData.getInstance().getDoor(24190002).closeMe();
		DoorData.getInstance().getDoor(24190003).closeMe();
		
		try
		{
			Thread.sleep(20);
		}
		catch (InterruptedException ie)
		{
			LOGGER.warning("Error, " + ie.getMessage());
		}
	}
	
	/**
	 * Open aden colosseum doors.
	 */
	private static void openAdenColosseumDoors()
	{
		DoorData.getInstance().getDoor(24190002).openMe();
		DoorData.getInstance().getDoor(24190003).openMe();
	}
}