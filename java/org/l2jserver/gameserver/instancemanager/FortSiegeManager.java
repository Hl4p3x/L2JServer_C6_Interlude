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
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.model.entity.siege.FortSiege;

public class FortSiegeManager
{
	private static final Logger LOGGER = Logger.getLogger(FortSiegeManager.class.getName());
	
	public static final FortSiegeManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public FortSiegeManager()
	{
		load();
	}
	
	private int _attackerMaxClans = 500; // Max number of clans
	private int _attackerRespawnDelay = 20000; // Time in ms. Changeable in siege.config
	private int _defenderMaxClans = 500; // Max number of clans
	private int _defenderRespawnDelay = 10000; // Time in ms. Changeable in siege.config
	
	// Fort Siege settings
	private Map<Integer, List<SiegeSpawn>> _commanderSpawnList;
	private Map<Integer, List<SiegeSpawn>> _flagList;
	
	private int _controlTowerLosePenalty = 20000; // Time in ms. Changeable in siege.config
	private int _flagMaxCount = 1; // Changeable in siege.config
	private int _siegeClanMinLevel = 4; // Changeable in siege.config
	private int _siegeLength = 120; // Time in minute. Changeable in siege.config
	private List<FortSiege> _sieges;
	
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
		final Fort fort = FortManager.getInstance().getFort(player);
		String message = "";
		if ((fort == null) || (fort.getFortId() <= 0))
		{
			message = "You must be on fort ground to summon this.";
		}
		else if (!fort.getSiege().isInProgress())
		{
			message = "You can only summon this during a siege.";
		}
		else if ((player.getClanId() != 0) && (fort.getSiege().getAttackerClan(player.getClanId()) == null))
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
	
	/**
	 * Return true if the clan is registered or owner of a fort
	 * @param clan The Clan of the player
	 * @param fortid
	 * @return
	 */
	public boolean checkIsRegistered(Clan clan, int fortid)
	{
		if (clan == null)
		{
			return false;
		}
		
		if (clan.getHasFort() > 0)
		{
			return true;
		}
		
		boolean register = false;
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, fortid);
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
			LOGGER.warning("Exception: checkIsRegistered(): " + e.getMessage());
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
		LOGGER.info("Initializing FortSiegeManager");
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(Config.FORTSIEGE_CONFIG_FILE));
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
			
			// Siege spawns settings
			_commanderSpawnList = new HashMap<>();
			_flagList = new HashMap<>();
			for (Fort fort : FortManager.getInstance().getForts())
			{
				final List<SiegeSpawn> commanderSpawns = new ArrayList<>();
				final List<SiegeSpawn> flagSpawns = new ArrayList<>();
				for (int i = 1; i < 5; i++)
				{
					final String _spawnParams = siegeSettings.getProperty(fort.getName() + "Commander" + i, "");
					if (_spawnParams.length() == 0)
					{
						break;
					}
					
					final StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					
					try
					{
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int heading = Integer.parseInt(st.nextToken());
						final int npc_id = Integer.parseInt(st.nextToken());
						commanderSpawns.add(new SiegeSpawn(fort.getFortId(), x, y, z, heading, npc_id));
					}
					catch (Exception e)
					{
						LOGGER.warning("Error while loading commander(s) for " + fort.getName() + " fort.");
					}
				}
				
				_commanderSpawnList.put(fort.getFortId(), commanderSpawns);
				
				for (int i = 1; i < 4; i++)
				{
					final String _spawnParams = siegeSettings.getProperty(fort.getName() + "Flag" + i, "");
					if (_spawnParams.length() == 0)
					{
						break;
					}
					
					final StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					
					try
					{
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int flag_id = Integer.parseInt(st.nextToken());
						flagSpawns.add(new SiegeSpawn(fort.getFortId(), x, y, z, 0, flag_id));
					}
					catch (Exception e)
					{
						LOGGER.warning("Error while loading flag(s) for " + fort.getName() + " fort.");
					}
				}
				_flagList.put(fort.getFortId(), flagSpawns);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while loading fortsiege data. " + e.getMessage());
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
					LOGGER.warning("Error while loading fortsiege data. " + e.getMessage());
				}
			}
		}
	}
	
	public List<SiegeSpawn> getCommanderSpawnList(int fortId)
	{
		if (_commanderSpawnList.containsKey(fortId))
		{
			return _commanderSpawnList.get(fortId);
		}
		return null;
	}
	
	public List<SiegeSpawn> getFlagList(int fortId)
	{
		if (_flagList.containsKey(fortId))
		{
			return _flagList.get(fortId);
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
	
	public FortSiege getSiege(WorldObject activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public FortSiege getSiege(int x, int y, int z)
	{
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort.getSiege().checkIfInZone(x, y, z))
			{
				return fort.getSiege();
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
	
	public List<FortSiege> getSieges()
	{
		if (_sieges == null)
		{
			_sieges = new ArrayList<>();
		}
		return _sieges;
	}
	
	public void addSiege(FortSiege fortSiege)
	{
		if (_sieges == null)
		{
			_sieges = new ArrayList<>();
		}
		_sieges.add(fortSiege);
	}
	
	public void removeSiege(FortSiege fortSiege)
	{
		if (_sieges == null)
		{
			_sieges = new ArrayList<>();
		}
		_sieges.remove(fortSiege);
	}
	
	public boolean isCombat(int itemId)
	{
		return itemId == 9819;
	}
	
	public class SiegeSpawn
	{
		Location _location;
		private final int _npcId;
		private final int _heading;
		private final int _fortId;
		private int _hp;
		
		public SiegeSpawn(int fortId, int x, int y, int z, int heading, int npcId)
		{
			_fortId = fortId;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npcId;
		}
		
		public SiegeSpawn(int fortId, int x, int y, int z, int heading, int npcId, int hp)
		{
			_fortId = fortId;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npcId;
			_hp = hp;
		}
		
		public int getFortId()
		{
			return _fortId;
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
	
	public boolean checkIsRegisteredInSiege(Clan clan)
	{
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (checkIsRegistered(clan, fort.getFortId()) && (fort.getSiege() != null) && fort.getSiege().isInProgress())
			{
				return true;
			}
		}
		return false;
	}
	
	private static class SingletonHolder
	{
		protected static final FortSiegeManager INSTANCE = new FortSiegeManager();
	}
}
