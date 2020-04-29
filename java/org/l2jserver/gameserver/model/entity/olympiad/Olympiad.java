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
package org.l2jserver.gameserver.model.entity.olympiad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.instancemanager.OlympiadStadiaManager;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.entity.Hero;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Olympiad
{
	protected static final Logger LOGGER = Logger.getLogger(Olympiad.class.getName());
	
	private static Map<Integer, StatSet> _nobles;
	private static Map<Integer, StatSet> _oldnobles;
	protected static List<StatSet> _heroesToBe;
	private static List<PlayerInstance> _nonClassBasedRegisters;
	private static Map<Integer, List<PlayerInstance>> _classBasedRegisters;
	public static final int OLY_MANAGER = 31688;
	public static List<Spawn> olymanagers = new ArrayList<>();
	
	private static final String OLYMPIAD_DATA_FILE = "config/olympiad.cfg";
	public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.charId";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles (`charId`,`class_id`,`char_name`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`,`competitions_drawn`) VALUES (?,?,?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE charId = ?";
	private static final String OLYMPIAD_UPDATE_OLD_NOBLES = "UPDATE olympiad_nobles_eom SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE charId = ?";
	private static final String OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.charId, characters.char_name FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.charId AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= 9 AND olympiad_nobles.competitions_won >= 1 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_won/olympiad_nobles.competitions_done DESC LIMIT " + Config.ALT_OLY_NUMBER_HEROS_EACH_CLASS;
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.obj_Id = olympiad_nobles_eom.charId AND olympiad_nobles_eom.class_id = ? AND olympiad_nobles_eom.competitions_done >= 9 ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT = "SELECT characters.char_name from olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.charId AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= 9 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC LIMIT 10";
	private static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
	private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
	private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT * FROM olympiad_nobles";
	private static final String OLYMPIAD_LOAD_OLD_NOBLES = "SELECT olympiad_nobles_eom.charId, olympiad_nobles_eom.class_id, characters.char_name, olympiad_nobles_eom.olympiad_points, olympiad_nobles_eom.competitions_done, olympiad_nobles_eom.competitions_won, olympiad_nobles_eom.competitions_lost, olympiad_nobles_eom.competitions_drawn FROM olympiad_nobles_eom, characters WHERE characters.obj_Id = olympiad_nobles_eom.charId";
	
	private static final int[] HERO_IDS =
	{
		88,
		89,
		90,
		91,
		92,
		93,
		94,
		95,
		96,
		97,
		98,
		99,
		100,
		101,
		102,
		103,
		104,
		105,
		106,
		107,
		108,
		109,
		110,
		111,
		112,
		113,
		114,
		115,
		116,
		117,
		118
	};
	
	private static final int COMP_START = Config.ALT_OLY_START_TIME; // 6PM
	private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
	private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 6 hours
	protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD; // 1 week
	protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD; // 24 hours
	
	private static final int DEFAULT_POINTS = 18;
	protected static final int WEEKLY_POINTS = 3;
	
	public static final String CHAR_ID = "charId";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	public static final String COMP_DRAWN = "competitions_drawn";
	
	protected long _olympiadEnd;
	protected long _validationEnd;
	
	/**
	 * The current period of the olympiad.<br>
	 * <b>0 -</b> Competition period<br>
	 * <b>1 -</b> Validation Period
	 */
	protected int _period;
	protected long _nextWeeklyChange;
	protected int _currentCycle;
	private long _compEnd;
	private Calendar _compStart;
	protected static boolean _inCompPeriod;
	protected static boolean _compStarted = false;
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _scheduledValdationTask;
	
	protected enum COMP_TYPE
	{
		CLASSED,
		NON_CLASSED
	}
	
	public Olympiad()
	{
		load();
		
		if (_period == 0)
		{
			init();
		}
	}
	
	public static Integer getStadiumCount()
	{
		return OlympiadManager.STADIUMS.length;
	}
	
	private void load()
	{
		_nobles = new ConcurrentHashMap<>();
		_oldnobles = new HashMap<>();
		
		final Properties olympiadProperties = new Properties();
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File("./" + OLYMPIAD_DATA_FILE));
			olympiadProperties.load(is);
		}
		catch (Exception e)
		{
			LOGGER.warning(OLYMPIAD_DATA_FILE + " cannot be loaded... It will be created on next save or server shutdown..");
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (Exception e)
			{
			}
		}
		
		_currentCycle = Integer.parseInt(olympiadProperties.getProperty("CurrentCycle", "1"));
		_period = Integer.parseInt(olympiadProperties.getProperty("Period", "0"));
		_olympiadEnd = Long.parseLong(olympiadProperties.getProperty("OlympiadEnd", "0"));
		_validationEnd = Long.parseLong(olympiadProperties.getProperty("ValdationEnd", "0"));
		_nextWeeklyChange = Long.parseLong(olympiadProperties.getProperty("NextWeeklyChange", "0"));
		
		switch (_period)
		{
			case 0:
			{
				if ((_olympiadEnd == 0) || (_olympiadEnd < Calendar.getInstance().getTimeInMillis()))
				{
					setNewOlympiadEnd();
				}
				else
				{
					scheduleWeeklyChange();
				}
				break;
			}
			case 1:
			{
				if (_validationEnd > Calendar.getInstance().getTimeInMillis())
				{
					_scheduledValdationTask = ThreadPool.schedule(new ValidationEndTask(), getMillisToValidationEnd());
				}
				else
				{
					_currentCycle++;
					_period = 0;
					deleteNobles();
					setNewOlympiadEnd();
				}
				break;
			}
			default:
			{
				LOGGER.warning("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final StatSet statData = new StatSet();
				final int charId = rset.getInt(CHAR_ID);
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set("to_save", false);
				_nobles.put(charId, statData);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Olympiad System: Error loading noblesse data from database: " + e);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_OLD_NOBLES);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final StatSet statData = new StatSet();
				final int charId = rset.getInt(CHAR_ID);
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set("to_save", false);
				_oldnobles.put(charId, statData);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Olympiad System: Error loading noblesse data from database: " + e);
		}
		
		synchronized (this)
		{
			LOGGER.info("Olympiad System: Loading Olympiad System....");
			if (_period == 0)
			{
				LOGGER.info("Olympiad System: Currently in Olympiad Period");
			}
			else
			{
				LOGGER.info("Olympiad System: Currently in Validation Period");
			}
			
			long milliToEnd;
			if (_period == 0)
			{
				milliToEnd = getMillisToOlympiadEnd();
			}
			else
			{
				milliToEnd = getMillisToValidationEnd();
			}
			
			LOGGER.info("Olympiad System: " + Math.round(milliToEnd / 60000) + " minutes until period ends");
			if (_period == 0)
			{
				milliToEnd = getMillisToWeekChange();
				
				LOGGER.info("Olympiad System: Next weekly change is in " + Math.round(milliToEnd / 60000) + " minutes");
			}
		}
		
		LOGGER.info("Olympiad System: Loaded " + _nobles.size() + " Nobles");
	}
	
	protected final void init()
	{
		if (_period == 1)
		{
			return;
		}
		
		_nonClassBasedRegisters = new ArrayList<>();
		_classBasedRegisters = new HashMap<>();
		_compStart = Calendar.getInstance();
		if (Config.ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS)
		{
			final int currentDay = _compStart.get(Calendar.DAY_OF_WEEK);
			boolean dayFound = false;
			int dayCounter = 0;
			for (int i = currentDay; i < 8; i++)
			{
				if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
				{
					dayFound = true;
					break;
				}
				dayCounter++;
			}
			if (!dayFound)
			{
				for (int i = 1; i < 8; i++)
				{
					if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
					{
						break;
					}
					dayCounter++;
				}
			}
			if (dayCounter > 0)
			{
				_compStart.add(Calendar.DAY_OF_MONTH, dayCounter);
			}
		}
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		
		_scheduledOlympiadEnd = ThreadPool.schedule(new OlympiadEndTask(), getMillisToOlympiadEnd());
		updateCompStatus();
	}
	
	protected class OlympiadEndTask implements Runnable
	{
		@Override
		public void run()
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.PERIOD_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_NOW_ENDED);
			sm.addNumber(_currentCycle);
			
			Announcements.getInstance().announceToAll(sm);
			Announcements.getInstance().announceToAll("Olympiad Validation Period has began");
			if (_scheduledWeeklyTask != null)
			{
				_scheduledWeeklyTask.cancel(true);
			}
			
			saveNobleData();
			
			_period = 1;
			sortHerosToBe();
			giveHeroBonus();
			Hero.getInstance().computeNewHeroes(_heroesToBe);
			
			saveOlympiadStatus();
			updateMonthlyData();
			
			final Calendar validationEnd = Calendar.getInstance();
			_validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;
			_scheduledValdationTask = ThreadPool.schedule(new ValidationEndTask(), getMillisToValidationEnd());
		}
	}
	
	protected class ValidationEndTask implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
			_period = 0;
			_currentCycle++;
			deleteNobles();
			setNewOlympiadEnd();
			init();
		}
	}
	
	public boolean registerNoble(PlayerInstance noble, boolean classBased)
	{
		SystemMessage sm;
		if (!_inCompPeriod)
		{
			sm = new SystemMessage(SystemMessageId.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!noble.isNoble())
		{
			sm = new SystemMessage(SystemMessageId.ONLY_NOBLESSE_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			noble.sendPacket(sm);
			return false;
		}
		
		/** Begin Olympiad Restrictions */
		if (noble.getBaseClass() != noble.getClassId().getId())
		{
			sm = new SystemMessage(SystemMessageId.YOU_CANNOT_PARTICIPATE_IN_THE_GRAND_OLYMPIAD_GAMES_WITH_A_CHARACTER_IN_THEIR_SUBCLASS);
			noble.sendPacket(sm);
			return false;
		}
		
		if (noble.isCursedWeaponEquiped())
		{
			sm = new SystemMessage(SystemMessageId.IF_YOU_POSSESS_S1_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addItemName(noble.getCursedWeaponEquipedId());
			noble.sendPacket(sm);
			return false;
		}
		
		if ((noble.getInventoryLimit() * 0.8) <= noble.getInventory().getSize())
		{
			sm = new SystemMessage(SystemMessageId.YOU_CAN_T_JOIN_A_GRAND_OLYMPIAD_GAME_MATCH_WITH_THAT_MUCH_STUFF_ON_YOU_REDUCE_YOUR_WEIGHT_TO_BELOW_80_PERCENT_FULL_AND_REQUEST_TO_JOIN_AGAIN);
			noble.sendPacket(sm);
			return false;
		}
		
		if (getMillisToCompEnd() < 600000)
		{
			sm = new SystemMessage(SystemMessageId.THE_REQUEST_TO_PARTICIPATE_IN_THE_GAME_CANNOT_BE_MADE_STARTING_FROM_10_MINUTES_BEFORE_THE_END_OF_THE_GAME);
			noble.sendPacket(sm);
			return false;
		}
		
		// To avoid possible bug during Olympiad, observer char can't join
		if (noble.inObserverMode())
		{
			noble.sendMessage("You can't participate to Olympiad. You are in Observer Mode, try to restart!");
			return false;
		}
		
		// Olympiad dualbox protection
		if ((noble._activeBoxes > 1) && !Config.ALLOW_DUALBOX_OLY)
		{
			final List<String> playerBoxes = noble._activeBoxeCharacters;
			if ((playerBoxes != null) && (playerBoxes.size() > 1))
			{
				for (String character_name : playerBoxes)
				{
					final PlayerInstance player = World.getInstance().getPlayer(character_name);
					if ((player != null) && ((player.getOlympiadGameId() > 0) || player.isInOlympiadMode() || getInstance().isRegistered(player)))
					{
						noble.sendMessage("You are already participating in Olympiad with another char!");
						return false;
					}
				}
			}
		}
		
		/** End Olympiad Restrictions */
		if (_classBasedRegisters.containsKey(noble.getClassId().getId()))
		{
			final List<PlayerInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			for (PlayerInstance participant : classed)
			{
				if (participant.getObjectId() == noble.getObjectId())
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_JOINED_THE_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH);
					noble.sendPacket(sm);
					return false;
				}
			}
		}
		
		if (isRegisteredInComp(noble))
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_JOINED_THE_WAITING_LIST_FOR_A_NON_CLASS_SPECIFIC_MATCH);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!_nobles.containsKey(noble.getObjectId()))
		{
			final StatSet statDat = new StatSet();
			statDat.set(CLASS_ID, noble.getClassId().getId());
			statDat.set(CHAR_NAME, noble.getName());
			statDat.set(POINTS, DEFAULT_POINTS);
			statDat.set(COMP_DONE, 0);
			statDat.set(COMP_WON, 0);
			statDat.set(COMP_LOST, 0);
			statDat.set(COMP_DRAWN, 0);
			statDat.set("to_save", true);
			_nobles.put(noble.getObjectId(), statDat);
		}
		
		if (classBased && (getNoblePoints(noble.getObjectId()) < 3))
		{
			noble.sendMessage("Cant register when you have less than 3 points");
			return false;
		}
		if (!classBased && (getNoblePoints(noble.getObjectId()) < 5))
		{
			noble.sendMessage("Cant register when you have less than 5 points");
			return false;
		}
		
		if (classBased)
		{
			if (_classBasedRegisters.containsKey(noble.getClassId().getId()))
			{
				final List<PlayerInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
				classed.add(noble);
				
				_classBasedRegisters.remove(noble.getClassId().getId());
				_classBasedRegisters.put(noble.getClassId().getId(), classed);
			}
			else
			{
				final List<PlayerInstance> classed = new ArrayList<>();
				classed.add(noble);
				
				_classBasedRegisters.put(noble.getClassId().getId(), classed);
			}
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH);
			noble.sendPacket(sm);
		}
		else
		{
			_nonClassBasedRegisters.add(noble);
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST_FOR_A_NON_CLASS_SPECIFIC_MATCH);
			noble.sendPacket(sm);
		}
		
		return true;
	}
	
	protected static int getNobleCount()
	{
		return _nobles.size();
	}
	
	protected static StatSet getNobleStats(int playerId)
	{
		return _nobles.get(playerId);
	}
	
	protected static synchronized void updateNobleStats(int playerId, StatSet stats)
	{
		_nobles.remove(playerId);
		_nobles.put(playerId, stats);
	}
	
	protected static synchronized void updateOldNobleStats(int playerId, StatSet stats)
	{
		_oldnobles.remove(playerId);
		_oldnobles.put(playerId, stats);
		getInstance().saveOldNobleData(playerId);
	}
	
	protected static List<PlayerInstance> getRegisteredNonClassBased()
	{
		return _nonClassBasedRegisters;
	}
	
	protected static Map<Integer, List<PlayerInstance>> getRegisteredClassBased()
	{
		return _classBasedRegisters;
	}
	
	protected static List<Integer> hasEnoughRegisteredClassed()
	{
		final List<Integer> result = new ArrayList<>();
		for (Integer classList : getRegisteredClassBased().keySet())
		{
			if (getRegisteredClassBased().get(classList).size() >= Config.ALT_OLY_CLASSED)
			{
				result.add(classList);
			}
		}
		
		if (!result.isEmpty())
		{
			return result;
		}
		return null;
	}
	
	protected static boolean hasEnoughRegisteredNonClassed()
	{
		return getRegisteredNonClassBased().size() >= Config.ALT_OLY_NONCLASSED;
	}
	
	protected static void clearRegistered()
	{
		_nonClassBasedRegisters.clear();
		_classBasedRegisters.clear();
	}
	
	public boolean isRegistered(PlayerInstance noble)
	{
		boolean result = false;
		if ((_nonClassBasedRegisters != null) && _nonClassBasedRegisters.contains(noble))
		{
			result = true;
		}
		else if ((_classBasedRegisters != null) && _classBasedRegisters.containsKey(noble.getClassId().getId()))
		{
			final List<PlayerInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			if ((classed != null) && classed.contains(noble))
			{
				result = true;
			}
		}
		return result;
	}
	
	public boolean unRegisterNoble(PlayerInstance noble)
	{
		SystemMessage sm;
		if (!_inCompPeriod)
		{
			sm = new SystemMessage(SystemMessageId.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!noble.isNoble())
		{
			sm = new SystemMessage(SystemMessageId.ONLY_NOBLESSE_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!isRegistered(noble))
		{
			sm = new SystemMessage(SystemMessageId.YOU_ARE_NOT_CURRENTLY_REGISTERED_ON_ANY_GRAND_OLYMPIAD_GAMES_WAITING_LIST);
			noble.sendPacket(sm);
			return false;
		}
		
		for (OlympiadGame game : OlympiadManager.getInstance().getOlympiadGames().values())
		{
			if (game == null)
			{
				continue;
			}
			
			if (((game._playerOne != null) && (game._playerOne.getObjectId() == noble.getObjectId())) || ((game._playerTwo != null) && (game._playerTwo.getObjectId() == noble.getObjectId())))
			{
				noble.sendMessage("Can't deregister whilst you are already selected for a game");
				return false;
			}
		}
		
		if (_nonClassBasedRegisters.contains(noble))
		{
			_nonClassBasedRegisters.remove(noble);
		}
		else
		{
			final List<PlayerInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			classed.remove(noble);
			
			_classBasedRegisters.remove(noble.getClassId().getId());
			_classBasedRegisters.put(noble.getClassId().getId(), classed);
		}
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST);
		noble.sendPacket(sm);
		
		return true;
	}
	
	public void removeDisconnectedCompetitor(PlayerInstance player)
	{
		if (OlympiadManager.getInstance().getOlympiadGame(player.getOlympiadGameId()) != null)
		{
			OlympiadManager.getInstance().getOlympiadGame(player.getOlympiadGameId()).handleDisconnect(player);
		}
		
		final List<PlayerInstance> classed = _classBasedRegisters.get(player.getClassId().getId());
		if (_nonClassBasedRegisters.contains(player))
		{
			_nonClassBasedRegisters.remove(player);
		}
		else if ((classed != null) && classed.contains(player))
		{
			classed.remove(player);
			
			_classBasedRegisters.remove(player.getClassId().getId());
			_classBasedRegisters.put(player.getClassId().getId(), classed);
		}
	}
	
	public void notifyCompetitorDamage(PlayerInstance player, int damage, int gameId)
	{
		if (OlympiadManager.getInstance().getOlympiadGames().get(gameId) != null)
		{
			OlympiadManager.getInstance().getOlympiadGames().get(gameId).addDamage(player, damage);
		}
	}
	
	private void updateCompStatus()
	{
		synchronized (this)
		{
			final long milliToStart = getMillisToCompBegin();
			final double numSecs = (milliToStart / 1000) % 60;
			double countDown = ((milliToStart / 1000) - numSecs) / 60;
			final int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			final int numHours = (int) Math.floor(countDown % 24);
			final int numDays = (int) Math.floor((countDown - numHours) / 24);
			
			LOGGER.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
			
			LOGGER.info("Olympiad System: Event starts/started : " + _compStart.getTime());
		}
		
		_scheduledCompStart = ThreadPool.schedule(() ->
		{
			if (isOlympiadEnd())
			{
				return;
			}
			
			_inCompPeriod = true;
			final OlympiadManager om = OlympiadManager.getInstance();
			Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.SHARPEN_YOUR_SWORDS_TIGHTEN_THE_STITCHINGS_IN_YOUR_ARMOR_AND_MAKE_HASTE_TO_A_GRAND_OLYMPIAD_MANAGER_BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_TAKING_PLACE));
			LOGGER.info("Olympiad System: Olympiad Game Started");
			
			final Thread olyCycle = new Thread(om);
			olyCycle.start();
			
			final long regEnd = getMillisToCompEnd() - 600000;
			if (regEnd > 0)
			{
				ThreadPool.schedule(() -> Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.THE_GRAND_OLYMPIAD_REGISTRATION_PERIOD_HAS_ENDED)), regEnd);
			}
			
			_scheduledCompEnd = ThreadPool.schedule(() ->
			{
				if (isOlympiadEnd())
				{
					return;
				}
				_inCompPeriod = false;
				Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.MUCH_CARNAGE_HAS_BEEN_LEFT_FOR_THE_CLEANUP_CREW_OF_THE_OLYMPIAD_STADIUM_BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_OVER));
				LOGGER.info("Olympiad System: Olympiad Game Ended");
				
				while (OlympiadGame._battleStarted)
				{
					try
					{
						// wait 1 minutes for end of pendings games
						Thread.sleep(60000);
					}
					catch (InterruptedException e)
					{
					}
				}
				saveOlympiadStatus();
				
				init();
			}, getMillisToCompEnd());
		}, getMillisToCompBegin());
	}
	
	private long getMillisToOlympiadEnd()
	{
		return (_olympiadEnd - Calendar.getInstance().getTimeInMillis());
	}
	
	public void manualSelectHeroes()
	{
		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		
		_scheduledOlympiadEnd = ThreadPool.schedule(new OlympiadEndTask(), 0);
	}
	
	protected long getMillisToValidationEnd()
	{
		if (_validationEnd > Calendar.getInstance().getTimeInMillis())
		{
			return (_validationEnd - Calendar.getInstance().getTimeInMillis());
		}
		return 10;
	}
	
	public boolean isOlympiadEnd()
	{
		return (_period != 0);
	}
	
	protected void setNewOlympiadEnd()
	{
		if (Config.ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS)
		{
			setNewOlympiadEndCustom();
			return;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.PERIOD_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_STARTED);
		sm.addNumber(_currentCycle);
		
		Announcements.getInstance().announceToAll(sm);
		
		final Calendar currentTime = Calendar.getInstance();
		currentTime.add(Calendar.MONTH, 1);
		currentTime.set(Calendar.DAY_OF_MONTH, 1);
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 12);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		_olympiadEnd = currentTime.getTimeInMillis();
		
		final Calendar nextChange = Calendar.getInstance();
		_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		scheduleWeeklyChange();
	}
	
	public boolean inCompPeriod()
	{
		return _inCompPeriod;
	}
	
	private long getMillisToCompBegin()
	{
		if ((_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_compEnd > Calendar.getInstance().getTimeInMillis()))
		{
			return 10;
		}
		
		if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
		{
			return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
		}
		
		return setNewCompBegin();
	}
	
	private long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		int currentDay = _compStart.get(Calendar.DAY_OF_WEEK);
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		
		// Today's competitions ended, start checking from next day.
		if (currentDay == _compStart.get(Calendar.DAY_OF_WEEK))
		{
			if (currentDay == Calendar.SATURDAY)
			{
				currentDay = Calendar.SUNDAY;
			}
			else
			{
				currentDay++;
			}
		}
		
		if (Config.ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS)
		{
			boolean dayFound = false;
			int dayCounter = 0;
			for (int i = currentDay; i < 8; i++)
			{
				if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
				{
					dayFound = true;
					break;
				}
				dayCounter++;
			}
			if (!dayFound)
			{
				for (int i = 1; i < 8; i++)
				{
					if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
					{
						break;
					}
					dayCounter++;
				}
			}
			if (dayCounter > 0)
			{
				_compStart.add(Calendar.DAY_OF_MONTH, dayCounter);
			}
		}
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		
		LOGGER.info("Olympiad System: New Schedule @ " + _compStart.getTime());
		return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	protected long getMillisToCompEnd()
	{
		return (_compEnd - Calendar.getInstance().getTimeInMillis());
	}
	
	private long getMillisToWeekChange()
	{
		if (_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
		{
			return (_nextWeeklyChange - Calendar.getInstance().getTimeInMillis());
		}
		return 10;
	}
	
	private void scheduleWeeklyChange()
	{
		if (Config.ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS)
		{
			schedulePointsRestoreCustom();
			return;
		}
		
		_scheduledWeeklyTask = ThreadPool.scheduleAtFixedRate(() ->
		{
			addWeeklyPoints();
			LOGGER.info("Olympiad System: Added weekly points to nobles");
			
			final Calendar nextChange = Calendar.getInstance();
			_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		}, getMillisToWeekChange(), WEEKLY_PERIOD);
	}
	
	protected synchronized void addWeeklyPoints()
	{
		if (_period == 1)
		{
			return;
		}
		
		for (Entry<Integer, StatSet> entry : _nobles.entrySet())
		{
			final Integer nobleId = entry.getKey();
			final StatSet nobleInfo = entry.getValue();
			int currentPoints = nobleInfo.getInt(POINTS);
			currentPoints += WEEKLY_POINTS;
			nobleInfo.set(POINTS, currentPoints);
			updateNobleStats(nobleId, nobleInfo);
		}
	}
	
	public Map<Integer, String> getMatchList()
	{
		return OlympiadManager.getInstance().getAllTitles();
	}
	
	// returns the players for the given olympiad game Id
	public PlayerInstance[] getPlayers(int id)
	{
		if (OlympiadManager.getInstance().getOlympiadGame(id) == null)
		{
			return null;
		}
		return OlympiadManager.getInstance().getOlympiadGame(id).getPlayers();
	}
	
	public int getCurrentCycle()
	{
		return _currentCycle;
	}
	
	public static void addSpectator(int id, PlayerInstance spectator, boolean storeCoords)
	{
		if (getInstance().isRegisteredInComp(spectator))
		{
			spectator.sendPacket(SystemMessageId.YOU_MAY_NOT_OBSERVE_A_GRAND_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST);
			return;
		}
		if (spectator.isRegisteredInFunEvent())
		{
			spectator.sendMessage("You are already registered to an Event");
			return;
		}
		
		OlympiadManager.STADIUMS[id].addSpectator(id, spectator, storeCoords);
		if (OlympiadManager.getInstance().getOlympiadGame(id) != null)
		{
			OlympiadManager.getInstance().getOlympiadGame(id).sendPlayersStatus(spectator);
		}
	}
	
	public static int getSpectatorArena(PlayerInstance player)
	{
		for (int i = 0; i < OlympiadManager.STADIUMS.length; i++)
		{
			if (OlympiadManager.STADIUMS[i].getSpectators().contains(player))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static void removeSpectator(int id, PlayerInstance spectator)
	{
		OlympiadManager.STADIUMS[id].removeSpectator(spectator);
	}
	
	public List<PlayerInstance> getSpectators(int id)
	{
		if (OlympiadManager.getInstance().getOlympiadGame(id) == null)
		{
			return null;
		}
		return OlympiadManager.STADIUMS[id].getSpectators();
	}
	
	public Map<Integer, OlympiadGame> getOlympiadGames()
	{
		return OlympiadManager.getInstance().getOlympiadGames();
	}
	
	public boolean playerInStadia(PlayerInstance player)
	{
		return (OlympiadStadiaManager.getInstance().getStadium(player) != null);
	}
	
	public int[] getWaitingList()
	{
		final int[] array = new int[2];
		if (!_inCompPeriod)
		{
			return null;
		}
		
		int classCount = 0;
		if (!_classBasedRegisters.isEmpty())
		{
			for (List<PlayerInstance> classed : _classBasedRegisters.values())
			{
				classCount += classed.size();
			}
		}
		
		array[0] = classCount;
		array[1] = _nonClassBasedRegisters.size();
		return array;
	}
	
	/**
	 * Save noblesse data to database
	 */
	protected synchronized void saveNobleData()
	{
		Connection con = null;
		if ((_nobles == null) || _nobles.isEmpty())
		{
			return;
		}
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement statement;
			for (Entry<Integer, StatSet> entry : _nobles.entrySet())
			{
				final Integer nobleId = entry.getKey();
				final StatSet nobleInfo = entry.getValue();
				if (nobleInfo == null)
				{
					continue;
				}
				
				final int charId = nobleId;
				final int classId = nobleInfo.getInt(CLASS_ID);
				final String charName = nobleInfo.getString(CHAR_NAME);
				final int points = nobleInfo.getInt(POINTS);
				final int compDone = nobleInfo.getInt(COMP_DONE);
				final int compWon = nobleInfo.getInt(COMP_WON);
				final int compLost = nobleInfo.getInt(COMP_LOST);
				final int compDrawn = nobleInfo.getInt(COMP_DRAWN);
				final boolean toSave = nobleInfo.getBoolean("to_save");
				if (toSave)
				{
					statement = con.prepareStatement(OLYMPIAD_SAVE_NOBLES);
					statement.setInt(1, charId);
					statement.setInt(2, classId);
					statement.setString(3, charName);
					statement.setInt(4, points);
					statement.setInt(5, compDone);
					statement.setInt(6, compWon);
					statement.setInt(7, compLost);
					statement.setInt(8, compDrawn);
					statement.execute();
					statement.close();
					
					nobleInfo.set("to_save", false);
					updateNobleStats(nobleId, nobleInfo);
				}
				else
				{
					statement = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES);
					statement.setInt(1, points);
					statement.setInt(2, compDone);
					statement.setInt(3, compWon);
					statement.setInt(4, compLost);
					statement.setInt(5, compDrawn);
					statement.setInt(6, charId);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Failed to save noblesse data to database: " + e);
		}
	}
	
	/**
	 * Save noblesse data to database
	 * @param nobleId
	 */
	protected synchronized void saveOldNobleData(int nobleId)
	{
		PreparedStatement statement = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			final StatSet nobleInfo = _oldnobles.get(nobleId);
			if (nobleInfo == null)
			{
				return;
			}
			
			final int charId = nobleId;
			final int points = nobleInfo.getInt(POINTS);
			final int compDone = nobleInfo.getInt(COMP_DONE);
			final int compWon = nobleInfo.getInt(COMP_WON);
			final int compLost = nobleInfo.getInt(COMP_LOST);
			final int compDrawn = nobleInfo.getInt(COMP_DRAWN);
			statement = con.prepareStatement(OLYMPIAD_UPDATE_OLD_NOBLES);
			statement.setInt(1, points);
			statement.setInt(2, compDone);
			statement.setInt(3, compWon);
			statement.setInt(4, compLost);
			statement.setInt(5, compDrawn);
			statement.setInt(6, charId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Failed to save old noblesse data to database: " + e);
		}
	}
	
	/**
	 * Save olympiad.cfg file with current olympiad status and update noblesse table in database
	 */
	public void saveOlympiadStatus()
	{
		saveNobleData();
		
		final Properties olympiadProperties = new Properties();
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(new File("./" + OLYMPIAD_DATA_FILE));
			olympiadProperties.setProperty("CurrentCycle", String.valueOf(_currentCycle));
			olympiadProperties.setProperty("Period", String.valueOf(_period));
			olympiadProperties.setProperty("OlympiadEnd", String.valueOf(_olympiadEnd));
			olympiadProperties.setProperty("ValdationEnd", String.valueOf(_validationEnd));
			olympiadProperties.setProperty("NextWeeklyChange", String.valueOf(_nextWeeklyChange));
			
			final GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
			gc.clear();
			gc.setTimeInMillis(_nextWeeklyChange);
			
			olympiadProperties.setProperty("NextWeeklyChange_DateFormat", DateFormat.getDateTimeInstance().format(gc.getTime()));
			gc.clear();
			gc.setTimeInMillis(_olympiadEnd);
			
			olympiadProperties.setProperty("OlympiadEnd_DateFormat", DateFormat.getDateTimeInstance().format(gc.getTime()));
			olympiadProperties.store(fos, "Olympiad Properties");
		}
		catch (Exception e)
		{
			LOGGER.warning("Olympiad System: Unable to save olympiad properties to file: " + e);
		}
		finally
		{
			try
			{
				if (fos != null)
				{
					fos.close();
				}
			}
			catch (Exception e)
			{
			}
		}
	}
	
	protected void updateMonthlyData()
	{
		PreparedStatement statement = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(OLYMPIAD_MONTH_CLEAR);
			statement.execute();
			statement.close();
			statement = con.prepareStatement(OLYMPIAD_MONTH_CREATE);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Failed to update monthly noblese data: " + e);
		}
	}
	
	protected void sortHerosToBe()
	{
		if (_period != 1)
		{
			return;
		}
		
		if (_nobles != null)
		{
			for (Entry<Integer, StatSet> entry : _nobles.entrySet())
			{
				final Integer nobleId = entry.getKey();
				final StatSet nobleInfo = entry.getValue();
				if (nobleInfo == null)
				{
					continue;
				}
				
				final int charId = nobleId;
				final int classId = nobleInfo.getInt(CLASS_ID);
				final String charName = nobleInfo.getString(CHAR_NAME);
				final int points = nobleInfo.getInt(POINTS);
				final int compDone = nobleInfo.getInt(COMP_DONE);
				logResult(charName, "", Double.valueOf(charId), Double.valueOf(classId), compDone, points, "noble-charId-classId-compdone-points", 0, "");
			}
		}
		
		_heroesToBe = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rset = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			StatSet hero;
			for (int HERO_ID : HERO_IDS)
			{
				statement = con.prepareStatement(OLYMPIAD_GET_HEROS);
				statement.setInt(1, HERO_ID);
				rset = statement.executeQuery();
				
				while (rset.next())
				{
					hero = new StatSet();
					hero.set(CLASS_ID, HERO_ID);
					hero.set(CHAR_ID, rset.getInt(CHAR_ID));
					hero.set(CHAR_NAME, rset.getString(CHAR_NAME));
					logResult(hero.getString(CHAR_NAME), "", hero.getDouble(CHAR_ID), hero.getDouble(CLASS_ID), 0, 0, "awarded hero", 0, "");
					_heroesToBe.add(hero);
				}
				statement.close();
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Couldnt load heros from DB : " + e.getMessage());
		}
	}
	
	public List<String> getClassLeaderBoard(int classId)
	{
		final List<String> names = new ArrayList<>();
		PreparedStatement statement = null;
		ResultSet rset = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (Config.ALT_OLY_SHOW_MONTHLY_WINNERS)
			{
				statement = con.prepareStatement(GET_EACH_CLASS_LEADER);
			}
			else
			{
				statement = con.prepareStatement(GET_EACH_CLASS_LEADER_CURRENT);
			}
			statement.setInt(1, classId);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				names.add(rset.getString(CHAR_NAME));
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Couldnt load olympiad leaders from DB");
		}
		
		return names;
	}
	
	protected void giveHeroBonus()
	{
		if (_heroesToBe.isEmpty())
		{
			return;
		}
		
		for (StatSet hero : _heroesToBe)
		{
			final int charId = hero.getInt(CHAR_ID);
			final StatSet noble = _nobles.get(charId);
			int currentPoints = noble.getInt(POINTS);
			currentPoints += Config.ALT_OLY_HERO_POINTS;
			noble.set(POINTS, currentPoints);
			updateNobleStats(charId, noble);
		}
	}
	
	public int getNoblessePasses(int objId)
	{
		if (_period == 1)
		{
			if (_nobles.isEmpty())
			{
				return 0;
			}
			
			final StatSet noble = _nobles.get(objId);
			if (noble == null)
			{
				return 0;
			}
			int points = noble.getInt(POINTS);
			if (points <= Config.ALT_OLY_MIN_POINT_FOR_EXCH)
			{
				return 0;
			}
			
			noble.set(POINTS, 0);
			updateNobleStats(objId, noble);
			points *= Config.ALT_OLY_GP_PER_POINT;
			return points;
		}
		
		if (_oldnobles.isEmpty())
		{
			return 0;
		}
		
		final StatSet noble = _oldnobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		int points = noble.getInt(POINTS);
		if (points <= Config.ALT_OLY_MIN_POINT_FOR_EXCH)
		{
			return 0;
		}
		
		noble.set(POINTS, 0);
		updateOldNobleStats(objId, noble);
		points *= Config.ALT_OLY_GP_PER_POINT;
		return points;
	}
	
	public boolean isRegisteredInComp(PlayerInstance player)
	{
		boolean result = isRegistered(player);
		if (_inCompPeriod)
		{
			for (OlympiadGame game : OlympiadManager.getInstance().getOlympiadGames().values())
			{
				if (((game._playerOne != null) && (game._playerOne.getObjectId() == player.getObjectId())) || ((game._playerTwo != null) && (game._playerTwo.getObjectId() == player.getObjectId())))
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	public int getNoblePoints(int objId)
	{
		if (_nobles.isEmpty())
		{
			return 0;
		}
		
		final StatSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		
		return noble.getInt(POINTS);
	}
	
	public int getCompetitionDone(int objId)
	{
		if (_nobles.isEmpty())
		{
			return 0;
		}
		
		final StatSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		
		return noble.getInt(COMP_DONE);
	}
	
	public int getCompetitionWon(int objId)
	{
		if (_nobles.isEmpty())
		{
			return 0;
		}
		
		final StatSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		
		return noble.getInt(COMP_WON);
	}
	
	public int getCompetitionLost(int objId)
	{
		if (_nobles.isEmpty())
		{
			return 0;
		}
		
		final StatSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		
		return noble.getInt(COMP_LOST);
	}
	
	protected void deleteNobles()
	{
		PreparedStatement statement = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(OLYMPIAD_DELETE_ALL);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Couldnt delete nobles from DB");
		}
		
		_oldnobles.clear();
		_oldnobles = _nobles;
		_nobles = new ConcurrentHashMap<>();
	}
	
	/**
	 * Logs result of Olympiad to a csv file.
	 * @param playerOne
	 * @param playerTwo
	 * @param p1hp
	 * @param p2hp
	 * @param p1dmg
	 * @param p2dmg
	 * @param result
	 * @param points
	 * @param classed
	 */
	public static synchronized void logResult(String playerOne, String playerTwo, Double p1hp, Double p2hp, int p1dmg, int p2dmg, String result, int points, String classed)
	{
		if (!Config.ALT_OLY_LOG_FIGHTS)
		{
			return;
		}
		
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
		final String date = formatter.format(new Date());
		FileWriter save = null;
		try
		{
			final File file = new File("log/olympiad.csv");
			boolean writeHead = false;
			if (!file.exists())
			{
				writeHead = true;
			}
			
			save = new FileWriter(file, true);
			if (writeHead)
			{
				final String header = "Date,Player1,Player2,Player1 HP,Player2 HP,Player1 Damage,Player2 Damage,Result,Points,Classed\r\n";
				save.write(header);
			}
			
			final String out = date + "," + playerOne + "," + playerTwo + "," + p1hp + "," + p2hp + "," + p1dmg + "," + p2dmg + "," + result + "," + points + "," + classed + "\r\n";
			save.write(out);
		}
		catch (IOException e)
		{
			LOGGER.warning("Olympiad System: Olympiad LOGGER could not be saved: " + e);
		}
		finally
		{
			try
			{
				if (save != null)
				{
					save.close();
				}
			}
			catch (Exception e)
			{
			}
		}
	}
	
	public static void sendMatchList(PlayerInstance player)
	{
		final NpcHtmlMessage message = new NpcHtmlMessage(0);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<center><br>Grand Olympiad Game View<table width=270 border=0 bgcolor=\"000000\">");
		replyMSG.append("<tr><td fixwidth=30>NO.</td><td fixwidth=60>Status</td><td>Player1 / Player2</td></tr>");
		
		final Map<Integer, String> matches = getInstance().getMatchList();
		for (int i = 0; i < getStadiumCount(); i++)
		{
			final int arenaID = i + 1;
			String players = "&nbsp;";
			String state = "Initial State";
			if (matches.containsKey(i))
			{
				state = "In Progress";
				players = matches.get(i);
			}
			replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h OlympiadArenaChange " + i + "\">" + arenaID + "</a></td><td fixwidth=60>" + state + "</td><td>" + players + "</td></tr>");
		}
		replyMSG.append("</table></center></body></html>");
		
		message.setHtml(replyMSG.toString());
		player.sendPacket(message);
	}
	
	public static void bypassChangeArena(String command, PlayerInstance player)
	{
		final String[] commands = command.split(" ");
		final int id = Integer.parseInt(commands[1]);
		final int arena = getSpectatorArena(player);
		if (arena >= 0)
		{
			removeSpectator(arena, player);
		}
		addSpectator(id, player, false);
	}
	
	protected void setNewOlympiadEndCustom()
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.PERIOD_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_STARTED);
		sm.addNumber(_currentCycle);
		
		Announcements.getInstance().announceToAll(sm);
		
		final Calendar currentTime = Calendar.getInstance();
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 12);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		
		final Calendar nextChange = Calendar.getInstance();
		
		switch (Config.ALT_OLY_PERIOD)
		{
			case DAY:
			{
				currentTime.add(Calendar.DAY_OF_MONTH, Config.ALT_OLY_PERIOD_MULTIPLIER);
				currentTime.add(Calendar.DAY_OF_MONTH, -1); // last day is for validation
				if (Config.ALT_OLY_PERIOD_MULTIPLIER >= 14)
				{
					_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
				}
				else if (Config.ALT_OLY_PERIOD_MULTIPLIER >= 7)
				{
					_nextWeeklyChange = nextChange.getTimeInMillis() + (WEEKLY_PERIOD / 2);
				}
				else
				{
					// nothing to do, too low period
				}
			}
				break;
			case WEEK:
			{
				currentTime.add(Calendar.WEEK_OF_MONTH, Config.ALT_OLY_PERIOD_MULTIPLIER);
				currentTime.add(Calendar.DAY_OF_MONTH, -1); // last day is for validation
				if (Config.ALT_OLY_PERIOD_MULTIPLIER > 1)
				{
					_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
				}
				else
				{
					_nextWeeklyChange = nextChange.getTimeInMillis() + (WEEKLY_PERIOD / 2);
				}
			}
				break;
			case MONTH:
			{
				currentTime.add(Calendar.MONTH, Config.ALT_OLY_PERIOD_MULTIPLIER);
				currentTime.add(Calendar.DAY_OF_MONTH, -1); // last day is for validation
				_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
			}
				break;
		}
		
		_olympiadEnd = currentTime.getTimeInMillis();
		scheduleWeeklyChange();
	}
	
	private void schedulePointsRestoreCustom()
	{
		long finalChangePeriod = WEEKLY_PERIOD;
		
		switch (Config.ALT_OLY_PERIOD)
		{
			case DAY:
			{
				if (Config.ALT_OLY_PERIOD_MULTIPLIER < 10)
				{
					finalChangePeriod = WEEKLY_PERIOD / 2;
				}
				break;
			}
			case WEEK:
			{
				if (Config.ALT_OLY_PERIOD_MULTIPLIER == 1)
				{
					finalChangePeriod = WEEKLY_PERIOD / 2;
				}
				break;
			}
		}
		
		_scheduledWeeklyTask = ThreadPool.scheduleAtFixedRate(new OlympiadPointsRestoreTask(finalChangePeriod), getMillisToWeekChange(), finalChangePeriod);
	}
	
	class OlympiadPointsRestoreTask implements Runnable
	{
		private final long restoreTime;
		
		public OlympiadPointsRestoreTask(long restoreTime)
		{
			this.restoreTime = restoreTime;
		}
		
		@Override
		public void run()
		{
			addWeeklyPoints();
			LOGGER.info("Olympiad System: Added points to nobles");
			
			final Calendar nextChange = Calendar.getInstance();
			_nextWeeklyChange = nextChange.getTimeInMillis() + restoreTime;
		}
	}
	
	public static Olympiad getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Olympiad INSTANCE = new Olympiad();
	}
}
