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
package org.l2jserver.gameserver.model;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.model.actor.Creature;

public class Potion extends WorldObject
{
	protected static final Logger LOGGER = Logger.getLogger(Potion.class.getName());
	
	private Future<?> _potionhpRegTask;
	private Future<?> _potionmpRegTask;
	protected int _milliseconds;
	protected double _effect;
	protected int _duration;
	private int _potion;
	protected Object _mpLock = new Object();
	protected Object _hpLock = new Object();
	
	class PotionHpHealing implements Runnable
	{
		Creature _creature;
		
		public PotionHpHealing(Creature creature)
		{
			_creature = creature;
		}
		
		@Override
		public void run()
		{
			try
			{
				synchronized (_hpLock)
				{
					double nowHp = _creature.getCurrentHp();
					if (_duration == 0)
					{
						stopPotionHpRegeneration();
					}
					if (_duration != 0)
					{
						nowHp += _effect;
						_creature.setCurrentHp(nowHp);
						_duration = _duration - (_milliseconds / 1000);
						setCurrentHpPotion2();
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Error in hp potion task:" + e);
			}
		}
	}
	
	public Potion(int objectId)
	{
		super(objectId);
	}
	
	public void stopPotionHpRegeneration()
	{
		if (_potionhpRegTask != null)
		{
			_potionhpRegTask.cancel(false);
		}
		
		_potionhpRegTask = null;
	}
	
	public void setCurrentHpPotion2()
	{
		if (_duration == 0)
		{
			stopPotionHpRegeneration();
		}
	}
	
	public void setCurrentHpPotion1(Creature creature, int item)
	{
		_potion = item;
		
		switch (_potion)
		{
			case 1540:
			{
				double nowHp = creature.getCurrentHp();
				nowHp += 435;
				if (nowHp >= creature.getMaxHp())
				{
					nowHp = creature.getMaxHp();
				}
				creature.setCurrentHp(nowHp);
				break;
			}
			case 728:
			{
				double nowMp = creature.getMaxMp();
				nowMp += 435;
				if (nowMp >= creature.getMaxMp())
				{
					nowMp = creature.getMaxMp();
				}
				creature.setCurrentMp(nowMp);
				break;
			}
			case 726:
			{
				_milliseconds = 500;
				_duration = 15;
				_effect = 1.5;
				startPotionMpRegeneration(creature);
				break;
			}
		}
	}
	
	class PotionMpHealing implements Runnable
	{
		Creature _instance;
		
		public PotionMpHealing(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			try
			{
				synchronized (_mpLock)
				{
					double nowMp = _instance.getCurrentMp();
					if (_duration == 0)
					{
						stopPotionMpRegeneration();
					}
					
					if (_duration != 0)
					{
						nowMp += _effect;
						_instance.setCurrentMp(nowMp);
						_duration = _duration - (_milliseconds / 1000);
						setCurrentMpPotion2();
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("error in mp potion task:" + e);
			}
		}
	}
	
	private void startPotionMpRegeneration(Creature creature)
	{
		_potionmpRegTask = ThreadPool.scheduleAtFixedRate(new PotionMpHealing(creature), 1000, _milliseconds);
	}
	
	public void stopPotionMpRegeneration()
	{
		if (_potionmpRegTask != null)
		{
			_potionmpRegTask.cancel(false);
		}
		
		_potionmpRegTask = null;
	}
	
	public void setCurrentMpPotion2()
	{
		if (_duration == 0)
		{
			stopPotionMpRegeneration();
		}
	}
	
	public void setCurrentMpPotion1(Creature creature, int item)
	{
		_potion = item;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
}
