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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.enums.RaidBossStatus;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author godson
 */
public class DayNightSpawnManager
{
	private static final Logger LOGGER = Logger.getLogger(DayNightSpawnManager.class.getName());
	
	private final List<Spawn> _dayCreatures = new ArrayList<>();
	private final List<Spawn> _nightCreatures = new ArrayList<>();
	private final Map<Spawn, RaidBossInstance> _bosses = new ConcurrentHashMap<>();
	
	private DayNightSpawnManager()
	{
	}
	
	public void addDayCreature(Spawn spawnDat)
	{
		if (_dayCreatures.contains(spawnDat))
		{
			LOGGER.warning("DayNightSpawnManager: Spawn already added into day map");
			return;
		}
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(Spawn spawnDat)
	{
		if (_nightCreatures.contains(spawnDat))
		{
			LOGGER.warning("DayNightSpawnManager: Spawn already added into night map");
			return;
		}
		_nightCreatures.add(spawnDat);
	}
	
	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}
	
	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}
	
	/**
	 * Manage Spawn/Respawn
	 * @param unSpawnCreatures List with spawns must be unspawned
	 * @param spawnCreatures List with spawns must be spawned
	 * @param unspawnLogInfo String for log info for unspawned NpcInstance
	 * @param spawnLogInfo String for log info for spawned NpcInstance
	 */
	private void spawnCreatures(List<Spawn> unSpawnCreatures, List<Spawn> spawnCreatures, String unspawnLogInfo, String spawnLogInfo)
	{
		try
		{
			if (!unSpawnCreatures.isEmpty())
			{
				int i = 0;
				for (Spawn spawn : unSpawnCreatures)
				{
					if (spawn == null)
					{
						continue;
					}
					
					spawn.stopRespawn();
					final NpcInstance last = spawn.getLastSpawn();
					if (last != null)
					{
						last.deleteMe();
						i++;
					}
				}
				LOGGER.info("DayNightSpawnManager: Removed " + i + " " + unspawnLogInfo + " creatures");
			}
			
			int i = 0;
			for (Spawn spawnDat : spawnCreatures)
			{
				if (spawnDat == null)
				{
					continue;
				}
				spawnDat.startRespawn();
				spawnDat.doSpawn();
				i++;
			}
			
			LOGGER.info("DayNightSpawnManager: Spawned " + i + " " + spawnLogInfo + " creatures");
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while spawning creatures: " + e.getMessage());
		}
	}
	
	private void changeMode(int mode)
	{
		if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty() && _bosses.isEmpty())
		{
			return;
		}
		
		switch (mode)
		{
			case 0:
			{
				spawnDayCreatures();
				specialNightBoss(0);
				ShadowSenseMsg(0);
				break;
			}
			case 1:
			{
				spawnNightCreatures();
				specialNightBoss(1);
				ShadowSenseMsg(1);
				break;
			}
			default:
			{
				LOGGER.warning("DayNightSpawnManager: Wrong mode sent");
				break;
			}
		}
	}
	
	public void notifyChangeMode()
	{
		try
		{
			if (GameTimeController.getInstance().isNowNight())
			{
				changeMode(1);
			}
			else
			{
				changeMode(0);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while notifyChangeMode(): " + e.getMessage());
		}
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(int mode)
	{
		try
		{
			for (Entry<Spawn, RaidBossInstance> entry : _bosses.entrySet())
			{
				RaidBossInstance boss = entry.getValue();
				if ((boss == null) && (mode == 1))
				{
					final Spawn spawn = entry.getKey();
					boss = (RaidBossInstance) spawn.doSpawn();
					RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
					_bosses.put(spawn, boss);
					continue;
				}
				
				if ((boss == null) && (mode == 0))
				{
					continue;
				}
				
				if ((boss != null) && (boss.getNpcId() == 25328) && boss.getRaidStatus().equals(RaidBossStatus.ALIVE))
				{
					handleHellmans(boss, mode);
				}
				return;
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while specialNoghtBoss(): " + e.getMessage());
		}
	}
	
	private void handleHellmans(RaidBossInstance boss, int mode)
	{
		switch (mode)
		{
			case 0:
			{
				boss.deleteMe();
				LOGGER.warning(getClass().getSimpleName() + ": Deleting Hellman raidboss");
				break;
			}
			case 1:
			{
				if (!boss.isVisible())
				{
					boss.spawnMe();
				}
				LOGGER.warning(getClass().getSimpleName() + ": Spawning Hellman raidboss");
				break;
			}
		}
	}
	
	private void ShadowSenseMsg(int mode)
	{
		final Skill skill = SkillTable.getInstance().getInfo(294, 1);
		if (skill == null)
		{
			return;
		}
		
		final SystemMessageId msg = (mode == 1 ? SystemMessageId.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT : SystemMessageId.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR);
		final Collection<PlayerInstance> pls = World.getInstance().getAllPlayers();
		for (PlayerInstance onlinePlayer : pls)
		{
			if ((onlinePlayer.getRace().ordinal() == 2) && (onlinePlayer.getSkillLevel(294) > 0))
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(msg);
				sm.addSkillName(294);
				onlinePlayer.sendPacket(sm);
			}
		}
	}
	
	public RaidBossInstance handleBoss(Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
		{
			return _bosses.get(spawnDat);
		}
		
		if (GameTimeController.getInstance().isNowNight())
		{
			final RaidBossInstance raidboss = (RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			
			return raidboss;
		}
		return null;
	}
	
	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager INSTANCE = new DayNightSpawnManager();
	}
}
