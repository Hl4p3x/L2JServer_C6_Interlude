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
package org.l2jserver.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.knownlist.MonsterKnownList;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.util.MinionList;

/**
 * This class manages all Monsters. MonsterInstance:<br>
 * <li>MinionInstance</li>
 * <li>RaidBossInstance</li>
 * <li>GrandBossInstance</li>
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class MonsterInstance extends Attackable
{
	protected final MinionList _minionList;
	protected ScheduledFuture<?> _minionMaintainTask = null;
	
	/**
	 * Constructor of MonsterInstance (use Creature and NpcInstance constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to set the _template of the MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public MonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		_minionList = new MinionList(this);
	}
	
	@Override
	public MonsterKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof MonsterKnownList))
		{
			setKnownList(new MonsterKnownList(this));
		}
		return (MonsterKnownList) super.getKnownList();
	}
	
	public void returnHome()
	{
		ThreadPool.schedule(() ->
		{
			final Spawn mobSpawn = getSpawn();
			if (!isInCombat() && !isAlikeDead() && !isDead() && (mobSpawn != null) && !isInsideRadius(mobSpawn.getX(), mobSpawn.getY(), Config.MAX_DRIFT_RANGE, false))
			{
				teleToLocation(mobSpawn.getX(), mobSpawn.getY(), mobSpawn.getZ(), false);
			}
		}, Config.MONSTER_RETURN_DELAY * 1000);
	}
	
	/**
	 * Return True if the attacker is not another MonsterInstance.
	 * @param attacker the attacker
	 * @return true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker instanceof MonsterInstance)
		{
			return false;
		}
		return !isEventMob;
	}
	
	/**
	 * Return True if the MonsterInstance is Agressive (aggroRange > 0).
	 * @return true, if is aggressive
	 */
	@Override
	public boolean isAggressive()
	{
		return (getTemplate().getAggroRange() > 0) && !isEventMob;
	}
	
	@Override
	public void onSpawn()
	{
		if (getTemplate().getMinionData() != null)
		{
			try
			{
				for (MinionInstance minion : _minionList.getSpawnedMinions())
				{
					if (minion == null)
					{
						continue;
					}
					_minionList.getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				_minionList.clearRespawnList();
				
				manageMinions();
			}
			catch (NullPointerException e)
			{
			}
			
			switch (getTemplate().getNpcId())
			{
				case 12372: // baium
				{
					broadcastPacket(new SocialAction(getObjectId(), 2));
				}
			}
		}
		
		super.onSpawn();
	}
	
	/**
	 * Spawn all minions at a regular interval.
	 */
	protected void manageMinions()
	{
		_minionMaintainTask = ThreadPool.schedule(_minionList::spawnMinions, 1000);
	}
	
	public void callMinions()
	{
		if (_minionList.hasMinions())
		{
			for (MinionInstance minion : _minionList.getSpawnedMinions())
			{
				// Get actual coords of the minion and check to see if it's too far away from this MonsterInstance
				if (!isInsideRadius(minion, 200, false, false))
				{
					// Get the coords of the master to use as a base to move the minion to
					final int masterX = getX();
					final int masterY = getY();
					final int masterZ = getZ();
					
					// Calculate a new random coord for the minion based on the master's coord
					int minionX = (masterX + Rnd.get(401)) - 200;
					int minionY = (masterY + Rnd.get(401)) - 200;
					final int minionZ = masterZ;
					while (((minionX != (masterX + 30)) && (minionX != (masterX - 30))) || ((minionY != (masterY + 30)) && (minionY != (masterY - 30))))
					{
						minionX = (masterX + Rnd.get(401)) - 200;
						minionY = (masterY + Rnd.get(401)) - 200;
					}
					
					// Move the minion to the new coords
					if (!minion.isInCombat() && !minion.isDead() && !minion.isMovementDisabled())
					{
						minion.moveToLocation(minionX, minionY, minionZ, 0);
					}
				}
			}
		}
	}
	
	/**
	 * Call minions to assist.
	 * @param attacker the attacker
	 */
	public void callMinionsToAssist(Creature attacker)
	{
		if (_minionList.hasMinions())
		{
			final List<MinionInstance> spawnedMinions = _minionList.getSpawnedMinions();
			if ((spawnedMinions != null) && !spawnedMinions.isEmpty())
			{
				final Iterator<MinionInstance> itr = spawnedMinions.iterator();
				MinionInstance minion;
				while (itr.hasNext())
				{
					minion = itr.next();
					// Trigger the aggro condition of the minion
					if ((minion != null) && !minion.isDead())
					{
						if (this instanceof RaidBossInstance)
						{
							minion.addDamage(attacker, 100);
						}
						else
						{
							minion.addDamage(attacker, 1);
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (_minionMaintainTask != null)
		{
			_minionMaintainTask.cancel(true); // doesn't do it?
		}
		
		if (this instanceof RaidBossInstance)
		{
			deleteSpawnedMinions();
		}
		return true;
	}
	
	/**
	 * Gets the spawned minions.
	 * @return the spawned minions
	 */
	public List<MinionInstance> getSpawnedMinions()
	{
		return _minionList.getSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions instances.
	 * @return the total spawned minions instances
	 */
	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList.countSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions groups.
	 * @return the total spawned minions groups
	 */
	public int getTotalSpawnedMinionsGroups()
	{
		return _minionList.lazyCountSpawnedMinionsGroups();
	}
	
	/**
	 * Notify minion died.
	 * @param minion the minion
	 */
	public void notifyMinionDied(MinionInstance minion)
	{
		_minionList.moveMinionToRespawnList(minion);
	}
	
	/**
	 * Notify minion spawned.
	 * @param minion the minion
	 */
	public void notifyMinionSpawned(MinionInstance minion)
	{
		_minionList.addSpawnedMinion(minion);
	}
	
	/**
	 * Checks for minions.
	 * @return true, if successful
	 */
	public boolean hasMinions()
	{
		return _minionList.hasMinions();
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (!(attacker instanceof MonsterInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_minionList.hasMinions())
		{
			if (_minionMaintainTask != null)
			{
				_minionMaintainTask.cancel(true);
			}
			
			deleteSpawnedMinions();
		}
		super.deleteMe();
	}
	
	/**
	 * Delete spawned minions.
	 */
	public void deleteSpawnedMinions()
	{
		for (MinionInstance minion : _minionList.getSpawnedMinions())
		{
			if (minion == null)
			{
				continue;
			}
			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
			_minionList.getSpawnedMinions().remove(minion);
		}
		_minionList.clearRespawnList();
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
}
