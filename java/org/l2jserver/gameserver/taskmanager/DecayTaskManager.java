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
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;

/**
 * @author la2 Lets drink to code!
 */
public class DecayTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(DecayTaskManager.class.getName());
	
	protected Map<Creature, Long> _decayTasks = new ConcurrentHashMap<>();
	
	private DecayTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(new DecayScheduler(), 10000, 5000);
	}
	
	public void addDecayTask(Creature actor)
	{
		_decayTasks.put(actor, System.currentTimeMillis());
	}
	
	public void addDecayTask(Creature actor, int interval)
	{
		_decayTasks.put(actor, System.currentTimeMillis() + interval);
	}
	
	public void cancelDecayTask(Creature actor)
	{
		try
		{
			_decayTasks.remove(actor);
		}
		catch (NoSuchElementException e)
		{
		}
	}
	
	private class DecayScheduler implements Runnable
	{
		protected DecayScheduler()
		{
		}
		
		@Override
		public void run()
		{
			final Long current = System.currentTimeMillis();
			int delay;
			try
			{
				if (_decayTasks != null)
				{
					for (Entry<Creature, Long> entry : _decayTasks.entrySet())
					{
						final Creature actor = entry.getKey();
						if (actor instanceof RaidBossInstance)
						{
							delay = 30000;
						}
						else
						{
							delay = 8500;
						}
						if ((current - entry.getValue()) > delay)
						{
							actor.onDecay();
							_decayTasks.remove(actor);
						}
					}
				}
			}
			catch (Throwable e)
			{
				// TODO: Find out the reason for exception. Unless caught here, mob decay would stop.
				LOGGER.warning(e.toString());
			}
		}
	}
	
	@Override
	public String toString()
	{
		String ret = "============= DecayTask Manager Report ============\r\n";
		ret += "Tasks count: " + _decayTasks.size() + "\r\n";
		ret += "Tasks dump:\r\n";
		
		final Long current = System.currentTimeMillis();
		for (Creature actor : _decayTasks.keySet())
		{
			ret += "Class/Name: " + actor.getClass().getSimpleName() + "/" + actor.getName() + " decay timer: " + (current - _decayTasks.get(actor)) + "\r\n";
		}
		
		return ret;
	}
	
	public static DecayTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DecayTaskManager INSTANCE = new DecayTaskManager();
	}
}
