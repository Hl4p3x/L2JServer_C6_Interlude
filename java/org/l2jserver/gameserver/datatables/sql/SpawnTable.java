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
package org.l2jserver.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.instancemanager.DayNightSpawnManager;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.spawn.Spawn;

/**
 * @author Nightmare
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2005/03/27 15:29:18 $
 */
public class SpawnTable
{
	private static final Logger LOGGER = Logger.getLogger(SpawnTable.class.getName());
	
	private final Map<Integer, Spawn> _spawntable = new ConcurrentHashMap<>();
	private int _npcSpawnCount;
	private int _customSpawnCount;
	private int _highestId;
	
	private SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			fillSpawnTable();
		}
	}
	
	public Map<Integer, Spawn> getSpawnTable()
	{
		return _spawntable;
	}
	
	private void fillSpawnTable()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (Config.DELETE_GMSPAWN_ON_CUSTOM)
			{
				statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist where id NOT in ( select id from custom_notspawned where isCustom = false ) ORDER BY id");
			}
			else
			{
				statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist ORDER BY id");
			}
			
			final ResultSet rset = statement.executeQuery();
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					if (template1.getType().equalsIgnoreCase("SiegeGuard"))
					{
						// Don't spawn
					}
					else if (template1.getType().equalsIgnoreCase("RaidBoss"))
					{
						// Don't spawn raidboss
					}
					else if (template1.getType().equalsIgnoreCase("GrandBoss"))
					{
						// Don't spawn grandboss
					}
					else if (!Config.ALLOW_CLASS_MASTERS && template1.getType().equals("ClassMaster"))
					{
						// Dont' spawn class masters
					}
					else
					{
						spawnDat = new Spawn(template1);
						spawnDat.setId(rset.getInt("id"));
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setX(rset.getInt("locx"));
						spawnDat.setY(rset.getInt("locy"));
						spawnDat.setZ(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						
						final int loc_id = rset.getInt("loc_id");
						spawnDat.setLocation(loc_id);
						
						switch (rset.getInt("periodOfDay"))
						{
							case 0: // default
							{
								_npcSpawnCount += spawnDat.init();
								break;
							}
							case 1: // Day
							{
								DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								_npcSpawnCount++;
								break;
							}
							case 2: // Night
							{
								DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								_npcSpawnCount++;
								break;
							}
						}
						
						_spawntable.put(spawnDat.getId(), spawnDat);
						if (spawnDat.getId() > _highestId)
						{
							_highestId = spawnDat.getId();
						}
						if (spawnDat.getTemplate().getNpcId() == Olympiad.OLY_MANAGER)
						{
							Olympiad.olymanagers.add(spawnDat);
						}
					}
				}
				else
				{
					LOGGER.warning("SpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("SpawnTable: Spawn could not be initialized. " + e);
		}
		
		LOGGER.info("SpawnTable: Loaded " + _spawntable.size() + " Npc Spawn Locations. ");
		LOGGER.info("SpawnTable: Total number of NPCs in the world: " + _npcSpawnCount);
		
		// -------------------------------Custom Spawnlist----------------------------//
		if (Config.CUSTOM_SPAWNLIST_TABLE)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement;
				if (Config.DELETE_GMSPAWN_ON_CUSTOM)
				{
					statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM custom_spawnlist where id NOT in ( select id from custom_notspawned where isCustom = false ) ORDER BY id");
				}
				else
				{
					statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM custom_spawnlist ORDER BY id");
				}
				
				final ResultSet rset = statement.executeQuery();
				Spawn spawnDat;
				NpcTemplate template1;
				
				while (rset.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
					if (template1 != null)
					{
						if (template1.getType().equalsIgnoreCase("SiegeGuard"))
						{
							// Don't spawn
						}
						else if (template1.getType().equalsIgnoreCase("RaidBoss"))
						{
							// Don't spawn raidboss
						}
						else if (!Config.ALLOW_CLASS_MASTERS && template1.getType().equals("ClassMaster"))
						{
							// Dont' spawn class masters
						}
						else
						{
							spawnDat = new Spawn(template1);
							spawnDat.setId(rset.getInt("id"));
							spawnDat.setAmount(rset.getInt("count"));
							spawnDat.setX(rset.getInt("locx"));
							spawnDat.setY(rset.getInt("locy"));
							spawnDat.setZ(rset.getInt("locz"));
							spawnDat.setHeading(rset.getInt("heading"));
							spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
							
							final int loc_id = rset.getInt("loc_id");
							spawnDat.setLocation(loc_id);
							
							switch (rset.getInt("periodOfDay"))
							{
								case 0: // default
								{
									_customSpawnCount += spawnDat.init();
									break;
								}
								case 1: // Day
								{
									DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
									_customSpawnCount++;
									break;
								}
								case 2: // Night
								{
									DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
									_customSpawnCount++;
									break;
								}
							}
							
							_spawntable.put(spawnDat.getId(), spawnDat);
							if (spawnDat.getId() > _highestId)
							{
								_highestId = spawnDat.getId();
							}
						}
					}
					else
					{
						LOGGER.warning("CustomSpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
					}
				}
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("CustomSpawnTable: Spawn could not be initialized. " + e);
			}
			
			LOGGER.info("CustomSpawnTable: Loaded " + _customSpawnCount + " Npc Spawn Locations. ");
			LOGGER.info("CustomSpawnTable: Total number of NPCs in the world: " + _customSpawnCount);
		}
	}
	
	public Spawn getTemplate(int id)
	{
		return _spawntable.get(id);
	}
	
	public void addNewSpawn(Spawn spawn, boolean storeInDb)
	{
		_highestId++;
		spawn.setId(_highestId);
		_spawntable.put(_highestId, spawn);
		
		if (storeInDb)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("INSERT INTO " + (Config.SAVE_GMSPAWN_ON_CUSTOM ? "custom_spawnlist" : "spawnlist") + "(id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?,?)");
				statement.setInt(1, spawn.getId());
				statement.setInt(2, spawn.getAmount());
				statement.setInt(3, spawn.getNpcId());
				statement.setInt(4, spawn.getX());
				statement.setInt(5, spawn.getY());
				statement.setInt(6, spawn.getZ());
				statement.setInt(7, spawn.getHeading());
				statement.setInt(8, spawn.getRespawnDelay() / 1000);
				statement.setInt(9, spawn.getLocation());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("SpawnTable: Could not store spawn in the DB. " + e);
			}
		}
	}
	
	public void deleteSpawn(Spawn spawn, boolean updateDb)
	{
		if (_spawntable.remove(spawn.getId()) == null)
		{
			return;
		}
		
		if (updateDb)
		{
			if (Config.DELETE_GMSPAWN_ON_CUSTOM)
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("Replace into custom_notspawned VALUES (?,?)");
					statement.setInt(1, spawn.getId());
					statement.setBoolean(2, false);
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("SpawnTable: Spawn " + spawn.getId() + " could not be insert into DB. " + e);
				}
			}
			else
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("DELETE FROM " + (Config.SAVE_GMSPAWN_ON_CUSTOM ? "custom_spawnlist" : "spawnlist") + " WHERE id=?");
					statement.setInt(1, spawn.getId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("SpawnTable: Spawn " + spawn.getId() + " could not be removed from DB. " + e);
				}
			}
		}
	}
	
	// just wrapper
	public void reloadAll()
	{
		fillSpawnTable();
	}
	
	/**
	 * Get all the spawn of a NPC
	 * @param player
	 * @param npcId : ID of the NPC to find.
	 * @param teleportIndex
	 */
	public void findNPCInstances(PlayerInstance player, int npcId, int teleportIndex)
	{
		int index = 0;
		for (Spawn spawn : _spawntable.values())
		{
			if (npcId == spawn.getNpcId())
			{
				index++;
				
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
					{
						player.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ(), true);
					}
				}
				else
				{
					player.sendMessage(index + " - " + spawn.getTemplate().getName() + " (" + spawn.getId() + "): " + spawn.getX() + " " + spawn.getY() + " " + spawn.getZ());
				}
			}
		}
		
		if (index == 0)
		{
			player.sendMessage("No current spawns found.");
		}
	}
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnTable INSTANCE = new SpawnTable();
	}
}
