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

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.zone.type.DerbyTrackZone;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.DeleteObject;
import org.l2jserver.gameserver.network.serverpackets.MonRaceInfo;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Broadcast;

public class MonsterRace
{
	protected static final Logger LOGGER = Logger.getLogger(MonsterRace.class.getName());
	
	public static enum RaceState
	{
		ACCEPTING_BETS,
		WAITING,
		STARTING_RACE,
		RACE_END
	}
	
	protected static final PlaySound SOUND_1 = new PlaySound(1, "S_Race");
	protected static final PlaySound SOUND_2 = new PlaySound("ItemSound2.race_start");
	
	protected static final int[][] CODES =
	{
		{
			-1,
			0
		},
		{
			0,
			15322
		},
		{
			13765,
			-1
		}
	};
	
	protected final List<Integer> _npcTemplates = new ArrayList<>(); // List holding npc templates, shuffled on a new race.
	protected final List<HistoryInfo> _history = new ArrayList<>(); // List holding old race records.
	protected final Map<Integer, Long> _betsPerLane = new ConcurrentHashMap<>(); // Map holding all bets for each lane ; values setted to 0 after every race.
	protected final List<Double> _odds = new ArrayList<>(); // List holding sorted odds per lane ; cleared at new odds calculation.
	
	protected int _raceNumber = 1;
	protected int _finalCountdown = 0;
	protected RaceState _state = RaceState.RACE_END;
	
	protected MonRaceInfo _packet;
	
	private Constructor<?> _constructor;
	private final NpcInstance[] _monsters = new NpcInstance[8];
	private int[][] _speeds = new int[8][20];
	private final int[] _first = new int[2];
	private final int[] _second = new int[2];
	
	protected MonsterRace()
	{
		if (!Config.ALLOW_RACE)
		{
			return;
		}
		
		// Feed _history with previous race results.
		loadHistory();
		
		// Feed _betsPerLane with stored informations on bets.
		loadBets();
		
		// Feed _npcTemplates, we will only have to shuffle it when needed.
		for (int i = 31003; i < 31027; i++)
		{
			_npcTemplates.add(i);
		}
		
		ThreadPool.scheduleAtFixedRate(new Announcement(), 0, 1000);
	}
	
	public static class HistoryInfo
	{
		private final int _raceId;
		private int _first;
		private int _second;
		private double _oddRate;
		
		public HistoryInfo(int raceId, int first, int second, double oddRate)
		{
			_raceId = raceId;
			_first = first;
			_second = second;
			_oddRate = oddRate;
		}
		
		public int getRaceId()
		{
			return _raceId;
		}
		
		public int getFirst()
		{
			return _first;
		}
		
		public int getSecond()
		{
			return _second;
		}
		
		public double getOddRate()
		{
			return _oddRate;
		}
		
		public void setFirst(int first)
		{
			_first = first;
		}
		
		public void setSecond(int second)
		{
			_second = second;
		}
		
		public void setOddRate(double oddRate)
		{
			_oddRate = oddRate;
		}
	}
	
	private class Announcement implements Runnable
	{
		public Announcement()
		{
		}
		
		@Override
		public void run()
		{
			if (_finalCountdown > 1200)
			{
				_finalCountdown = 0;
			}
			
			switch (_finalCountdown)
			{
				case 0:
				{
					newRace();
					newSpeeds();
					
					_state = RaceState.ACCEPTING_BETS;
					_packet = new MonRaceInfo(CODES[0][0], CODES[0][1], getMonsters(), getSpeeds());
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, _packet, SystemMessage.getSystemMessage(SystemMessageId.TICKETS_ARE_NOW_AVAILABLE_FOR_MONSTER_RACE_S1).addNumber(_raceNumber));
					break;
				}
				case 30: // 30 sec
				case 60: // 1 min
				case 90: // 1 min 30 sec
				case 120: // 2 min
				case 150: // 2 min 30
				case 180: // 3 min
				case 210: // 3 min 30
				case 240: // 4 min
				case 270: // 4 min 30 sec
				case 330: // 5 min 30 sec
				case 360: // 6 min
				case 390: // 6 min 30 sec
				case 420: // 7 min
				case 450: // 7 min 30
				case 480: // 8 min
				case 510: // 8 min 30
				case 540: // 9 min
				case 570: // 9 min 30 sec
				case 630: // 10 min 30 sec
				case 660: // 11 min
				case 690: // 11 min 30 sec
				case 720: // 12 min
				case 750: // 12 min 30
				case 780: // 13 min
				case 810: // 13 min 30
				case 870: // 14 min 30 sec
				{
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1).addNumber(_raceNumber));
					break;
				}
				case 300: // 5 min
				{
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_END_IN_S1_MINUTE_S).addNumber(10));
					break;
				}
				case 600: // 10 min
				{
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_END_IN_S1_MINUTE_S).addNumber(5));
					break;
				}
				case 840: // 14 min
				{
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_END_IN_S1_MINUTE_S).addNumber(1));
					break;
				}
				case 900: // 15 min
				{
					_state = RaceState.WAITING;
					calculateOdds();
					
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1).addNumber(_raceNumber), SystemMessage.getSystemMessage(SystemMessageId.TICKETS_SALES_ARE_CLOSED_FOR_MONSTER_RACE_S1_ODDS_ARE_POSTED).addNumber(_raceNumber));
					break;
				}
				case 960: // 16 min
				case 1020: // 17 min
				{
					final int minutes = (_finalCountdown == 960) ? 2 : 1;
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSTER_RACE_S2_WILL_BEGIN_IN_S1_MINUTE_S).addNumber(minutes).addNumber(_raceNumber));
					break;
				}
				case 1050: // 17 min 30 sec
				{
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSTER_RACE_S1_WILL_BEGIN_IN_30_SECONDS));
					break;
				}
				case 1070: // 17 min 50 sec
				{
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSTER_RACE_S1_IS_ABOUT_TO_BEGIN_COUNTDOWN_IN_FIVE_SECONDS));
					break;
				}
				case 1075: // 17 min 55 sec
				case 1076: // 17 min 56 sec
				case 1077: // 17 min 57 sec
				case 1078: // 17 min 58 sec
				case 1079: // 17 min 59 sec
				{
					final int seconds = 1080 - _finalCountdown;
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.THE_RACE_WILL_BEGIN_IN_S1_SECOND_S).addNumber(seconds));
					break;
				}
				case 1080: // 18 min
				{
					_state = RaceState.STARTING_RACE;
					_packet = new MonRaceInfo(CODES[1][0], CODES[1][1], getMonsters(), getSpeeds());
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.THEY_RE_OFF), SOUND_1, SOUND_2, _packet);
					break;
				}
				case 1085: // 18 min 5 sec
				{
					_packet = new MonRaceInfo(CODES[2][0], CODES[2][1], getMonsters(), getSpeeds());
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, _packet);
					break;
				}
				case 1115: // 18 min 35 sec
				{
					_state = RaceState.RACE_END;
					
					// Populate history info with data, stores it in database.
					final HistoryInfo info = _history.get(_history.size() - 1);
					info.setFirst(getFirstPlace());
					info.setSecond(getSecondPlace());
					info.setOddRate(_odds.get(getFirstPlace() - 1));
					saveHistory(info);
					clearBets();
					
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.FIRST_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S1_SECOND_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S2).addNumber(getFirstPlace()).addNumber(getSecondPlace()), SystemMessage.getSystemMessage(SystemMessageId.MONSTER_RACE_S1_IS_FINISHED).addNumber(_raceNumber));
					_raceNumber++;
					break;
				}
				case 1140: // 19 min
				{
					Broadcast.toAllPlayersInZoneType(DerbyTrackZone.class, new DeleteObject(getMonsters()[0]), new DeleteObject(getMonsters()[1]), new DeleteObject(getMonsters()[2]), new DeleteObject(getMonsters()[3]), new DeleteObject(getMonsters()[4]), new DeleteObject(getMonsters()[5]), new DeleteObject(getMonsters()[6]), new DeleteObject(getMonsters()[7]));
					break;
				}
			}
			_finalCountdown += 1;
		}
	}
	
	public void newRace()
	{
		// Edit _history.
		_history.add(new HistoryInfo(_raceNumber, 0, 0, 0));
		
		// Randomize _npcTemplates.
		Collections.shuffle(_npcTemplates);
		
		// Setup 8 new creatures ; pickup the first 8 from _npcTemplates.
		for (int i = 0; i < 8; i++)
		{
			try
			{
				final NpcTemplate template = NpcTable.getInstance().getTemplate(_npcTemplates.get(i));
				_constructor = Class.forName("org.l2jserver.gameserver.model.actor.instance." + template.getType() + "Instance").getConstructors()[0];
				final int objectId = IdFactory.getNextId();
				_monsters[i] = (NpcInstance) _constructor.newInstance(objectId, template);
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "", e);
			}
		}
	}
	
	public void newSpeeds()
	{
		_speeds = new int[8][20];
		int total = 0;
		_first[1] = 0;
		_second[1] = 0;
		for (int i = 0; i < 8; i++)
		{
			total = 0;
			for (int j = 0; j < 20; j++)
			{
				if (j == 19)
				{
					_speeds[i][j] = 100;
				}
				else
				{
					_speeds[i][j] = Rnd.get(60) + 65;
				}
				total += _speeds[i][j];
			}
			
			if (total >= _first[1])
			{
				_second[0] = _first[0];
				_second[1] = _first[1];
				_first[0] = 8 - i;
				_first[1] = total;
			}
			else if (total >= _second[1])
			{
				_second[0] = 8 - i;
				_second[1] = total;
			}
		}
	}
	
	/**
	 * Load past races informations, feeding _history arrayList.<br>
	 * Also sets _raceNumber, based on latest HistoryInfo loaded.
	 */
	protected void loadHistory()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM mdt_history");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				_history.add(new HistoryInfo(rset.getInt("race_id"), rset.getInt("first"), rset.getInt("second"), rset.getDouble("odd_rate")));
				_raceNumber++;
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "MonsterRace: Can't load history: " + e.getMessage(), e);
		}
		LOGGER.info("MonsterRace: loaded " + _history.size() + " records, currently on race #" + _raceNumber);
	}
	
	/**
	 * Save an history record into database.
	 * @param history The infos to store.
	 */
	protected void saveHistory(HistoryInfo history)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO mdt_history (race_id, first, second, odd_rate) VALUES (?,?,?,?)");
			statement.setInt(1, history.getRaceId());
			statement.setInt(2, history.getFirst());
			statement.setInt(3, history.getSecond());
			statement.setDouble(4, history.getOddRate());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "MonsterRace: Can't save history: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Load current bets per lane ; initialize the map keys.
	 */
	protected void loadBets()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM mdt_bets");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				setBetOnLane(rset.getInt("lane_id"), rset.getLong("bet"), false);
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "MonsterRace: Can't load bets: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Save the current lane bet into database.
	 * @param lane : The lane to affect.
	 * @param sum : The sum to set.
	 */
	protected void saveBet(int lane, long sum)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("REPLACE INTO mdt_bets (lane_id, bet) VALUES (?,?)");
			statement.setInt(1, lane);
			statement.setLong(2, sum);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "MonsterRace: Can't save bet: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Clear all lanes bets, either on database or Map.
	 */
	protected void clearBets()
	{
		for (int key : _betsPerLane.keySet())
		{
			_betsPerLane.put(key, 0L);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE mdt_bets SET bet = 0");
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "MonsterRace: Can't clear bets: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Setup lane bet, based on previous value (if any).
	 * @param lane : The lane to edit.
	 * @param amount : The amount to add.
	 * @param saveOnDb : Should it be saved on db or not.
	 */
	public void setBetOnLane(int lane, long amount, boolean saveOnDb)
	{
		final long sum = (_betsPerLane.containsKey(lane)) ? _betsPerLane.get(lane) + amount : amount;
		_betsPerLane.put(lane, sum);
		
		if (saveOnDb)
		{
			saveBet(lane, sum);
		}
	}
	
	/**
	 * Calculate odds for every lane, based on others lanes.
	 */
	protected void calculateOdds()
	{
		// Clear previous List holding old odds.
		_odds.clear();
		
		// Sort bets lanes per lane.
		final Map<Integer, Long> sortedLanes = new TreeMap<>(_betsPerLane);
		
		// Pass a first loop in order to calculate total sum of all lanes.
		long sumOfAllLanes = 0;
		for (long amount : sortedLanes.values())
		{
			sumOfAllLanes += amount;
		}
		
		// As we get the sum, we can now calculate the odd rate of each lane.
		for (long amount : sortedLanes.values())
		{
			_odds.add((amount == 0) ? 0D : Math.max(1.25, (sumOfAllLanes * 0.7) / amount));
		}
	}
	
	public NpcInstance[] getMonsters()
	{
		return _monsters;
	}
	
	public int[][] getSpeeds()
	{
		return _speeds;
	}
	
	public int getFirstPlace()
	{
		return _first[0];
	}
	
	public int getSecondPlace()
	{
		return _second[0];
	}
	
	public MonRaceInfo getRacePacket()
	{
		return _packet;
	}
	
	public RaceState getCurrentRaceState()
	{
		return _state;
	}
	
	public int getRaceNumber()
	{
		return _raceNumber;
	}
	
	public List<HistoryInfo> getHistory()
	{
		return _history;
	}
	
	public List<Double> getOdds()
	{
		return _odds;
	}
	
	public static MonsterRace getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MonsterRace INSTANCE = new MonsterRace();
	}
}