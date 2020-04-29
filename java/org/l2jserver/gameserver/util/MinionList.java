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
package org.l2jserver.gameserver.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.MinionData;
import org.l2jserver.gameserver.model.actor.instance.MinionInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class MinionList
{
	/** List containing the current spawned minions for this MonsterInstance */
	private final List<MinionInstance> minionReferences;
	protected Map<Long, Integer> _respawnTasks = new ConcurrentHashMap<>();
	private final MonsterInstance master;
	
	public MinionList(MonsterInstance pMaster)
	{
		minionReferences = new ArrayList<>();
		master = pMaster;
	}
	
	public int countSpawnedMinions()
	{
		synchronized (minionReferences)
		{
			return minionReferences.size();
		}
	}
	
	public int countSpawnedMinionsById(int minionId)
	{
		int count = 0;
		synchronized (minionReferences)
		{
			for (MinionInstance minion : minionReferences)
			{
				if (minion.getNpcId() == minionId)
				{
					count++;
				}
			}
		}
		return count;
	}
	
	public boolean hasMinions()
	{
		return !minionReferences.isEmpty();
	}
	
	public List<MinionInstance> getSpawnedMinions()
	{
		return minionReferences;
	}
	
	public void addSpawnedMinion(MinionInstance minion)
	{
		synchronized (minionReferences)
		{
			minionReferences.add(minion);
		}
	}
	
	public int lazyCountSpawnedMinionsGroups()
	{
		final Set<Integer> seenGroups = new HashSet<>();
		for (MinionInstance minion : minionReferences)
		{
			seenGroups.add(minion.getNpcId());
		}
		return seenGroups.size();
	}
	
	public void removeSpawnedMinion(MinionInstance minion)
	{
		synchronized (minionReferences)
		{
			minionReferences.remove(minion);
		}
	}
	
	public void moveMinionToRespawnList(MinionInstance minion)
	{
		final Long current = System.currentTimeMillis();
		synchronized (minionReferences)
		{
			minionReferences.remove(minion);
			if (_respawnTasks.get(current) == null)
			{
				_respawnTasks.put(current, minion.getNpcId());
			}
			else
			{
				// nice AoE
				for (int i = 1; i < 30; i++)
				{
					if (_respawnTasks.get(current + i) == null)
					{
						_respawnTasks.put(current + i, minion.getNpcId());
						break;
					}
				}
			}
		}
	}
	
	public void clearRespawnList()
	{
		_respawnTasks.clear();
	}
	
	/**
	 * Manage respawning of minions for this RaidBoss.
	 */
	public void maintainMinions()
	{
		if ((master == null) || master.isAlikeDead())
		{
			return;
		}
		
		final Long current = System.currentTimeMillis();
		if (_respawnTasks != null)
		{
			for (Entry<Long, Integer> entry : _respawnTasks.entrySet())
			{
				final long deathTime = entry.getKey();
				final double delay = Config.RAID_MINION_RESPAWN_TIMER;
				if ((current - deathTime) > delay)
				{
					spawnSingleMinion(entry.getValue());
					_respawnTasks.remove(deathTime);
				}
			}
		}
	}
	
	/**
	 * Manage the spawn of all Minions of this RaidBoss.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the Minion data of all Minions that must be spawn</li>
	 * <li>For each Minion type, spawn the amount of Minion needed</li>
	 */
	public void spawnMinions()
	{
		if ((master == null) || master.isAlikeDead())
		{
			return;
		}
		
		final List<MinionData> minions = master.getTemplate().getMinionData();
		
		synchronized (minionReferences)
		{
			int minionCount;
			int minionId;
			int minionsToSpawn;
			for (MinionData minion : minions)
			{
				minionCount = minion.getAmount();
				minionId = minion.getMinionId();
				minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
				for (int i = 0; i < minionsToSpawn; i++)
				{
					spawnSingleMinion(minionId);
				}
			}
		}
	}
	
	/**
	 * Init a Minion and add it in the world as a visible object.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the template of the Minion to spawn</li>
	 * <li>Create and Init the Minion and generate its Identifier</li>
	 * <li>Set the Minion HP, MP and Heading</li>
	 * <li>Set the Minion leader to this RaidBoss</li>
	 * <li>Init the position of the Minion and add it in the world as a visible object</li><br>
	 * @param minionid The I2NpcTemplate Identifier of the Minion to spawn
	 */
	public void spawnSingleMinion(int minionid)
	{
		// Get the template of the Minion to spawn
		final NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionid);
		
		// Create and Init the Minion and generate its Identifier
		final MinionInstance monster = new MinionInstance(IdFactory.getNextId(), minionTemplate);
		
		// Set the Minion HP, MP and Heading
		monster.setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
		monster.setHeading(master.getHeading());
		
		// Set the Minion leader to this RaidBoss
		monster.setLeader(master);
		
		// Init the position of the Minion and add it in the world as a visible object
		int spawnConstant;
		final int randSpawnLim = 170;
		int randPlusMin = 1;
		spawnConstant = Rnd.get(randSpawnLim);
		randPlusMin = Rnd.get(2);
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		
		final int newX = master.getX() + spawnConstant;
		spawnConstant = Rnd.get(randSpawnLim);
		randPlusMin = Rnd.get(2);
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		
		final int newY = master.getY() + spawnConstant;
		monster.spawnMe(newX, newY, master.getZ());
		
		// Assist master
		if (!master.getAggroList().isEmpty())
		{
			monster.getAggroList().putAll(master.getAggroList());
			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, monster.getAggroList().keySet().stream().findFirst().get());
		}
	}
}
