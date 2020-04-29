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
package org.l2jserver.gameserver.taskmanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.CubicInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.AutoAttackStop;

/**
 * Attack stance task manager.
 * @author Luca Baldi
 */
public class AttackStanceTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(AttackStanceTaskManager.class.getName());
	
	protected static final Map<Creature, Long> _attackStanceTasks = new ConcurrentHashMap<>();
	
	public static final long COMBAT_TIME = 15000;
	
	private AttackStanceTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	public void addAttackStanceTask(Creature actor)
	{
		if (actor instanceof Summon)
		{
			final Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		if (actor instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) actor;
			for (CubicInstance cubic : player.getCubics().values())
			{
				if (cubic.getId() != CubicInstance.LIFE_CUBIC)
				{
					cubic.doAction();
				}
			}
		}
		_attackStanceTasks.put(actor, System.currentTimeMillis());
	}
	
	public void removeAttackStanceTask(Creature actor)
	{
		if (actor instanceof Summon)
		{
			final Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		_attackStanceTasks.remove(actor);
	}
	
	public boolean hasAttackStanceTask(Creature actor)
	{
		if (actor instanceof Summon)
		{
			final Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		return _attackStanceTasks.containsKey(actor);
	}
	
	private class FightModeScheduler implements Runnable
	{
		protected FightModeScheduler()
		{
		}
		
		@Override
		public void run()
		{
			final Long current = System.currentTimeMillis();
			try
			{
				if (_attackStanceTasks != null)
				{
					synchronized (this)
					{
						for (Entry<Creature, Long> entry : _attackStanceTasks.entrySet())
						{
							final Creature actor = entry.getKey();
							if ((current - entry.getValue()) > COMBAT_TIME)
							{
								actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
								if ((actor instanceof PlayerInstance) && (((PlayerInstance) actor).getPet() != null))
								{
									((PlayerInstance) actor).getPet().broadcastPacket(new AutoAttackStop(((PlayerInstance) actor).getPet().getObjectId()));
								}
								actor.getAI().setAutoAttacking(false);
								_attackStanceTasks.remove(actor);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				// TODO: Find out the reason for exception. Unless caught here, players remain in attack positions.
				LOGGER.warning("Error in FightModeScheduler: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Gets the single instance of AttackStanceTaskManager.
	 * @return single instance of AttackStanceTaskManager
	 */
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager INSTANCE = new AttackStanceTaskManager();
	}
}
