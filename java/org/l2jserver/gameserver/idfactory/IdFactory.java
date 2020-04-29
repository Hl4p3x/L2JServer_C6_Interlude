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
package org.l2jserver.gameserver.idfactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.util.PrimeFinder;

/**
 * @author Mobius (reworked from L2J version)
 */
public abstract class IdFactory
{
	private static final Logger LOGGER = Logger.getLogger(IdFactory.class.getName());
	
	protected static final String[] ID_CHECKS =
	{
		"SELECT owner_id    FROM items                 WHERE object_id >= ?   AND object_id < ?",
		"SELECT object_id   FROM items                 WHERE object_id >= ?   AND object_id < ?",
		"SELECT char_id     FROM character_quests      WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_id     FROM character_friends     WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_id     FROM character_friends     WHERE friend_id >= ?   AND friend_id < ?",
		"SELECT char_obj_id FROM character_hennas      WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_id     FROM character_recipebook  WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_obj_id FROM character_shortcuts   WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_macroses    WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_skills      WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_skills_save WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_subclasses  WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT obj_Id      FROM characters            WHERE obj_Id >= ?      AND obj_Id < ?",
		"SELECT clanid      FROM characters            WHERE clanid >= ?      AND clanid < ?",
		"SELECT clan_id     FROM clan_data             WHERE clan_id >= ?     AND clan_id < ?",
		"SELECT clan_id     FROM siege_clans           WHERE clan_id >= ?     AND clan_id < ?",
		"SELECT ally_id     FROM clan_data             WHERE ally_id >= ?     AND ally_id < ?",
		"SELECT leader_id   FROM clan_data             WHERE leader_id >= ?   AND leader_id < ?",
		"SELECT item_obj_id FROM pets                  WHERE item_obj_id >= ? AND item_obj_id < ?",
		"SELECT object_id   FROM itemsonground        WHERE object_id >= ?   AND object_id < ?"
	};
	//@formatter:off
	private static final String[][] ID_EXTRACTS =
	{
		{"characters","obj_Id"},
		{"items","object_id"},
		{"clan_data","clan_id"},
		{"itemsonground","object_id"}
	};
	//@formatter:on
	private static final String[] TIMESTAMPS_CLEAN =
	{
		"DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?"
	};
	public static final int FIRST_OID = 0x10000000;
	public static final int LAST_OID = 0x7FFFFFFF;
	public static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;
	
	private static BitSet _freeIds;
	private static AtomicInteger _freeIdCount;
	private static AtomicInteger _nextFreeId;
	private static boolean _initialized;
	
	public static void init()
	{
		// Update characters online status.
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("UPDATE characters SET online = 0");
			LOGGER.info("Updated characters online status.");
		}
		catch (Exception e)
		{
			LOGGER.warning("IdFactory: Could not update characters online status: " + e);
		}
		
		// Cleanup database.
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			final long cleanupStart = System.currentTimeMillis();
			int cleanCount = 0;
			
			// Characters
			cleanCount += statement.executeUpdate("DELETE FROM character_friends WHERE character_friends.char_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_quests WHERE character_quests.char_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.char_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_skills WHERE character_skills.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.playerId NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM heroes WHERE heroes.charId NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.charId NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM seven_signs WHERE seven_signs.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
			
			// Auction
			cleanCount += statement.executeUpdate("DELETE FROM auction WHERE auction.id IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			cleanCount += statement.executeUpdate("DELETE FROM auction_bid WHERE auctionId IN (SELECT id FROM clanhall WHERE ownerId <> 0)");
			
			// Clan
			statement.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT obj_Id FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clanhall_functions WHERE clanhall_functions.hall_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			statement.executeUpdate("UPDATE castle SET taxpercent=0 WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data);");
			
			// Forums
			cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
			cleanCount += statement.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");
			cleanCount += statement.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
			
			// Update needed items after cleaning has taken place.
			cleanCount += statement.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT obj_Id FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data);");
			statement.executeUpdate("UPDATE characters SET clanid=0 WHERE characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
			
			LOGGER.info("IdFactory: Cleaned " + cleanCount + " elements from database in " + ((System.currentTimeMillis() - cleanupStart) / 1000) + " seconds.");
		}
		catch (Exception e)
		{
			LOGGER.warning("IdFactory: Could not clean up database: " + e);
		}
		
		// Cleanup timestamps.
		try (Connection con = DatabaseFactory.getConnection())
		{
			int cleanCount = 0;
			for (String line : TIMESTAMPS_CLEAN)
			{
				try (PreparedStatement statement = con.prepareStatement(line))
				{
					statement.setLong(1, System.currentTimeMillis());
					cleanCount += statement.executeUpdate();
				}
			}
			LOGGER.info("IdFactory: Cleaned " + cleanCount + " expired timestamps from database.");
		}
		catch (Exception e)
		{
			LOGGER.warning("IdFactory: Could not clean expired timestamps from database. " + e);
		}
		
		// Initialize.
		try
		{
			_freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			_freeIds.clear();
			_freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
			
			// Collect already used ids.
			final List<Integer> usedIds = new ArrayList<>();
			try (Connection con = DatabaseFactory.getConnection();
				Statement statement = con.createStatement())
			{
				String extractUsedObjectIdsQuery = "";
				for (String[] tblClmn : ID_EXTRACTS)
				{
					extractUsedObjectIdsQuery += "SELECT " + tblClmn[1] + " FROM " + tblClmn[0] + " UNION ";
				}
				extractUsedObjectIdsQuery = extractUsedObjectIdsQuery.substring(0, extractUsedObjectIdsQuery.length() - 7); // Remove the last " UNION "
				try (ResultSet result = statement.executeQuery(extractUsedObjectIdsQuery))
				{
					while (result.next())
					{
						usedIds.add(result.getInt(1));
					}
				}
			}
			Collections.sort(usedIds);
			
			// Register used ids.
			for (int usedObjectId : usedIds)
			{
				final int objectId = usedObjectId - FIRST_OID;
				if (objectId < 0)
				{
					LOGGER.warning("IdFactory: Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					continue;
				}
				_freeIds.set(usedObjectId - FIRST_OID);
				_freeIdCount.decrementAndGet();
			}
			
			_nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
			_initialized = true;
		}
		catch (Exception e)
		{
			_initialized = false;
			LOGGER.severe("IdFactory: Could not be initialized properly: " + e.getMessage());
		}
		
		// Schedule increase capacity task.
		ThreadPool.scheduleAtFixedRate(() ->
		{
			synchronized (_nextFreeId)
			{
				if (PrimeFinder.nextPrime((usedIdCount() * 11) / 10) > _freeIds.size())
				{
					increaseBitSetCapacity();
				}
			}
		}, 30000, 30000);
		
		LOGGER.info("IdFactory: " + _freeIds.size() + " id's available.");
	}
	
	public synchronized static void releaseId(int objectId)
	{
		synchronized (_nextFreeId)
		{
			if ((objectId - FIRST_OID) > -1)
			{
				_freeIds.clear(objectId - FIRST_OID);
				_freeIdCount.incrementAndGet();
			}
			else
			{
				LOGGER.warning("IdFactory: Release objectID " + objectId + " failed (< " + FIRST_OID + ")");
			}
		}
	}
	
	public synchronized static int getNextId()
	{
		synchronized (_nextFreeId)
		{
			final int newId = _nextFreeId.get();
			_freeIds.set(newId);
			_freeIdCount.decrementAndGet();
			
			final int nextFree = _freeIds.nextClearBit(newId) < 0 ? _freeIds.nextClearBit(0) : _freeIds.nextClearBit(newId);
			if (nextFree < 0)
			{
				if (_freeIds.size() >= FREE_OBJECT_ID_SIZE)
				{
					throw new NullPointerException("IdFactory: Ran out of valid ids.");
				}
				increaseBitSetCapacity();
			}
			_nextFreeId.set(nextFree);
			
			return newId + FIRST_OID;
		}
	}
	
	private static void increaseBitSetCapacity()
	{
		final BitSet newBitSet = new BitSet(PrimeFinder.nextPrime((usedIdCount() * 11) / 10));
		newBitSet.or(_freeIds);
		_freeIds = newBitSet;
	}
	
	private static int usedIdCount()
	{
		return _freeIdCount.get() - FIRST_OID;
	}
	
	public static int size()
	{
		return _freeIdCount.get();
	}
	
	public static boolean hasInitialized()
	{
		return _initialized;
	}
}
