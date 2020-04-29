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
package org.l2jserver.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Kerberos
 */
public class RaidBossPointsManager
{
	private static final Logger LOGGER = Logger.getLogger(RaidBossPointsManager.class.getName());
	protected static Map<Integer, Map<Integer, Integer>> _list;
	
	private static final Comparator<Entry<Integer, Integer>> _comparator = (entry, entry1) -> entry.getValue().equals(entry1.getValue()) ? 0 : entry.getValue() < entry1.getValue() ? 1 : -1;
	
	public static void init()
	{
		_list = new HashMap<>();
		final List<Integer> chars = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `character_raid_points`");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				chars.add(rset.getInt("charId"));
			}
			rset.close();
			statement.close();
			for (int charId : chars)
			{
				final Map<Integer, Integer> values = new HashMap<>();
				statement = con.prepareStatement("SELECT * FROM `character_raid_points` WHERE `charId`=?");
				statement.setInt(1, charId);
				rset = statement.executeQuery();
				while (rset.next())
				{
					values.put(rset.getInt("boss_id"), rset.getInt("points"));
				}
				rset.close();
				statement.close();
				_list.put(charId, values);
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("RaidPointsManager: Could not load raid points.");
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
	}
	
	public static void updatePointsInDB(PlayerInstance player, int raidId, int points)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, raidId);
			statement.setInt(3, points);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("RaidPointsManager: Could not update char raid points: " + e);
		}
	}
	
	public static void addPoints(PlayerInstance player, int bossId, int points)
	{
		final int ownerId = player.getObjectId();
		Map<Integer, Integer> tmpPoint;
		if (_list == null)
		{
			_list = new HashMap<>();
		}
		tmpPoint = _list.get(ownerId);
		if ((tmpPoint == null) || tmpPoint.isEmpty())
		{
			tmpPoint = new HashMap<>();
			tmpPoint.put(bossId, points);
			updatePointsInDB(player, bossId, points);
		}
		else
		{
			final int currentPoins = tmpPoint.containsKey(bossId) ? tmpPoint.get(bossId).intValue() : 0;
			tmpPoint.remove(bossId);
			tmpPoint.put(bossId, currentPoins == 0 ? points : currentPoins + points);
			updatePointsInDB(player, bossId, currentPoins == 0 ? points : currentPoins + points);
		}
		_list.remove(ownerId);
		_list.put(ownerId, tmpPoint);
	}
	
	public static final int getPointsByOwnerId(int ownerId)
	{
		Map<Integer, Integer> tmpPoint;
		if (_list == null)
		{
			_list = new HashMap<>();
		}
		tmpPoint = _list.get(ownerId);
		int totalPoints = 0;
		if ((tmpPoint == null) || tmpPoint.isEmpty())
		{
			return 0;
		}
		
		for (int points : tmpPoint.values())
		{
			totalPoints += points;
		}
		return totalPoints;
	}
	
	public static final Map<Integer, Integer> getList(PlayerInstance player)
	{
		return _list.get(player.getObjectId());
	}
	
	public static void cleanUp()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE from character_raid_points WHERE charId > 0");
			statement.executeUpdate();
			statement.close();
			_list.clear();
			_list = new HashMap<>();
		}
		catch (Exception e)
		{
			LOGGER.warning("RaidPointsManager: Could not clean raid points: " + e);
		}
	}
	
	public static final int calculateRanking(int playerObjId)
	{
		final Map<Integer, Integer> tmpRanking = new HashMap<>();
		final Map<Integer, Integer> tmpPoints = new HashMap<>();
		int totalPoints;
		for (int ownerId : _list.keySet())
		{
			totalPoints = getPointsByOwnerId(ownerId);
			if (totalPoints != 0)
			{
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		final ArrayList<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());
		Collections.sort(list, _comparator);
		int ranking = 1;
		for (Entry<Integer, Integer> entry : list)
		{
			tmpRanking.put(entry.getKey(), ranking++);
		}
		
		if (tmpRanking.containsKey(playerObjId))
		{
			return tmpRanking.get(playerObjId);
		}
		return 0;
	}
	
	public static Map<Integer, Integer> getRankList()
	{
		final Map<Integer, Integer> tmpRanking = new HashMap<>();
		final Map<Integer, Integer> tmpPoints = new HashMap<>();
		int totalPoints;
		for (int ownerId : _list.keySet())
		{
			totalPoints = getPointsByOwnerId(ownerId);
			if (totalPoints != 0)
			{
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		final ArrayList<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());
		Collections.sort(list, _comparator);
		int ranking = 1;
		for (Entry<Integer, Integer> entry : list)
		{
			tmpRanking.put(entry.getKey(), ranking++);
		}
		return tmpRanking;
	}
}