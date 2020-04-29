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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Siege;

public class SiegeManager
{
	private static final Logger LOGGER = Logger.getLogger(SiegeManager.class.getName());
	
	public static final SiegeManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private int _attackerMaxClans = 500; // Max number of clans
	private int _attackerRespawnDelay = 20000; // Time in ms. Changeable in siege.config
	private int _defenderMaxClans = 500; // Max number of clans
	private int _defenderRespawnDelay = 10000; // Time in ms. Changeable in siege.config
	
	// Siege settings
	private Map<Integer, List<SiegeSpawn>> _artefactSpawnList;
	private Map<Integer, List<SiegeSpawn>> _controlTowerSpawnList;
	
	private int _controlTowerLosePenalty = 20000; // Time in ms. Changeable in siege.config
	private int _flagMaxCount = 1; // Changeable in siege.config
	private int _siegeClanMinLevel = 4; // Changeable in siege.config
	private int _siegeLength = 120; // Time in minute. Changeable in siege.config
	
	private boolean _teleportToSiege = false;
	private boolean _teleportToSiegeTown = false;
	
	private SiegeManager()
	{
		load();
	}
	
	public void addSiegeSkills(PlayerInstance character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
	}
	
	/**
	 * Return true if character summon
	 * @param creature The Creature of the creature can summon
	 * @param isCheckOnly
	 * @return
	 */
	public boolean checkIfOkToSummon(Creature creature, boolean isCheckOnly)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return false;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		final Castle castle = CastleManager.getInstance().getCastle(player);
		String message = "";
		if ((castle == null) || (castle.getCastleId() <= 0))
		{
			message = "You must be on castle ground to summon this.";
		}
		else if (!castle.getSiege().isInProgress())
		{
			message = "You can only summon this during a siege.";
		}
		else if ((player.getClanId() != 0) && (castle.getSiege().getAttackerClan(player.getClanId()) == null))
		{
			message = "You can only summon this as a registered attacker.";
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly && !message.isEmpty())
		{
			player.sendMessage(message);
		}
		
		return false;
	}
	
	public boolean checkIsRegisteredInSiege(Clan clan)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (checkIsRegistered(clan, castle.getCastleId()) && (castle.getSiege() != null) && castle.getSiege().isInProgress())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return true if the clan is registered or owner of a castle
	 * @param clan The Clan of the player
	 * @param castleid
	 * @return
	 */
	public boolean checkIsRegistered(Clan clan, int castleid)
	{
		if (clan == null)
		{
			return false;
		}
		
		if (clan.getHasCastle() > 0)
		{
			return true;
		}
		
		boolean register = false;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleid);
			final ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				register = true;
				break;
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.info("Exception: checkIsRegistered(): " + e.getMessage());
		}
		return register;
	}
	
	public void removeSiegeSkills(PlayerInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}
	
	private final void load()
	{
		LOGGER.info("Initializing SiegeManager");
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(Config.SIEGE_CONFIG_FILE));
			final Properties siegeSettings = new Properties();
			siegeSettings.load(is);
			
			// Siege setting
			_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
			_attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "30000"));
			_controlTowerLosePenalty = Integer.decode(siegeSettings.getProperty("CTLossPenalty", "20000"));
			_defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
			_defenderRespawnDelay = Integer.decode(siegeSettings.getProperty("DefenderRespawn", "20000"));
			_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
			_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
			_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
			
			// Siege Teleports
			_teleportToSiege = Boolean.parseBoolean(siegeSettings.getProperty("AllowTeleportToSiege", "false"));
			_teleportToSiegeTown = Boolean.parseBoolean(siegeSettings.getProperty("AllowTeleportToSiegeTown", "false"));
			
			// Siege spawns settings
			_controlTowerSpawnList = new HashMap<>();
			_artefactSpawnList = new HashMap<>();
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				final List<SiegeSpawn> controlTowersSpawns = new ArrayList<>();
				for (int i = 1; i < 0xFF; i++)
				{
					final String spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + i, "");
					if (spawnParams.isEmpty())
					{
						break;
					}
					
					final StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
					
					try
					{
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int npc_id = Integer.parseInt(st.nextToken());
						final int hp = Integer.parseInt(st.nextToken());
						controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, npc_id, hp));
					}
					catch (Exception e)
					{
						LOGGER.warning("Error while loading control tower(s) for " + castle.getName() + " castle.");
					}
				}
				
				final List<SiegeSpawn> artefactSpawns = new ArrayList<>();
				for (int i = 1; i < 0xFF; i++)
				{
					final String spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + i, "");
					if (spawnParams.isEmpty())
					{
						break;
					}
					
					final StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
					
					try
					{
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int heading = Integer.parseInt(st.nextToken());
						final int npc_id = Integer.parseInt(st.nextToken());
						artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
					}
					catch (Exception e)
					{
						LOGGER.warning("Error while loading artefact(s) for " + castle.getName() + " castle.");
					}
				}
				
				_controlTowerSpawnList.put(castle.getCastleId(), controlTowersSpawns);
				_artefactSpawnList.put(castle.getCastleId(), artefactSpawns);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while loading siege data: " + e.getMessage());
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					LOGGER.warning("Error while loading siege data: " + e.getMessage());
				}
			}
		}
	}
	
	public List<SiegeSpawn> getArtefactSpawnList(int castleId)
	{
		if (_artefactSpawnList.containsKey(castleId))
		{
			return _artefactSpawnList.get(castleId);
		}
		return null;
	}
	
	public List<SiegeSpawn> getControlTowerSpawnList(int castleId)
	{
		if (_controlTowerSpawnList.containsKey(castleId))
		{
			return _controlTowerSpawnList.get(castleId);
		}
		return null;
	}
	
	public int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}
	
	public int getAttackerRespawnDelay()
	{
		return _attackerRespawnDelay;
	}
	
	public int getControlTowerLosePenalty()
	{
		return _controlTowerLosePenalty;
	}
	
	public int getDefenderMaxClans()
	{
		return _defenderMaxClans;
	}
	
	public int getDefenderRespawnDelay()
	{
		return _defenderRespawnDelay;
	}
	
	public int getFlagMaxCount()
	{
		return _flagMaxCount;
	}
	
	public Siege getSiege(WorldObject activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getSiege().checkIfInZone(x, y, z))
			{
				return castle.getSiege();
			}
		}
		return null;
	}
	
	public int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}
	
	public int getSiegeLength()
	{
		return _siegeLength;
	}
	
	public List<Siege> getSieges()
	{
		final List<Siege> sieges = new ArrayList<>();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			sieges.add(castle.getSiege());
		}
		return sieges;
	}
	
	/**
	 * @return the _teleportToSiege
	 */
	public boolean isTeleportToSiegeAllowed()
	{
		return _teleportToSiege;
	}
	
	/**
	 * @return the _teleportToSiegeTown
	 */
	public boolean isTeleportToSiegeTownAllowed()
	{
		return _teleportToSiegeTown;
	}
	
	public class SiegeSpawn
	{
		Location _location;
		private final int _npcId;
		private final int _heading;
		private final int _castleId;
		private int _hp;
		
		public SiegeSpawn(int castleId, int x, int y, int z, int heading, int npcId)
		{
			_castleId = castleId;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npcId;
		}
		
		public SiegeSpawn(int castleId, int x, int y, int z, int heading, int npcId, int hp)
		{
			_castleId = castleId;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npcId;
			_hp = hp;
		}
		
		public int getCastleId()
		{
			return _castleId;
		}
		
		public int getNpcId()
		{
			return _npcId;
		}
		
		public int getHeading()
		{
			return _heading;
		}
		
		public int getHp()
		{
			return _hp;
		}
		
		public Location getLocation()
		{
			return _location;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeManager INSTANCE = new SiegeManager();
	}
}
