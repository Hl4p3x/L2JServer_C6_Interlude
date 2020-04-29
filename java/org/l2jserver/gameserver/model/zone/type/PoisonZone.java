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
package org.l2jserver.gameserver.model.zone.type;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.zone.ZoneType;

public class PoisonZone extends ZoneType
{
	protected final Logger LOGGER = Logger.getLogger(PoisonZone.class.getName());
	protected int _skillId;
	int _chance;
	private int _initialDelay;
	protected int _skillLvl;
	private int _reuse;
	boolean _enabled;
	String _target;
	private Future<?> _task;
	
	public PoisonZone(int id)
	{
		super(id);
		_skillId = 4070;
		_skillLvl = 1;
		_chance = 100;
		_initialDelay = 0;
		_reuse = 30000;
		_enabled = true;
		_target = "pc";
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "skillId":
			{
				_skillId = Integer.parseInt(value);
				break;
			}
			case "skillLvl":
			{
				_skillLvl = Integer.parseInt(value);
				break;
			}
			case "chance":
			{
				_chance = Integer.parseInt(value);
				break;
			}
			case "initialDelay":
			{
				_initialDelay = Integer.parseInt(value);
				break;
			}
			case "default_enabled":
			{
				_enabled = Boolean.parseBoolean(value);
				break;
			}
			case "target":
			{
				_target = value;
				break;
			}
			case "reuse":
			{
				_reuse = Integer.parseInt(value);
				break;
			}
			default:
			{
				super.setParameter(name, value);
				break;
			}
		}
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if ((((creature instanceof Playable) && _target.equalsIgnoreCase("pc")) || ((creature instanceof PlayerInstance) && _target.equalsIgnoreCase("pc_only")) || ((creature instanceof MonsterInstance) && _target.equalsIgnoreCase("npc"))) && (_task == null))
		{
			_task = ThreadPool.scheduleAtFixedRate(new ApplySkill(/* this */), _initialDelay, _reuse);
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (getCharactersInside().isEmpty() && (_task != null))
		{
			_task.cancel(true);
			_task = null;
		}
	}
	
	public Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
	}
	
	public String getTargetType()
	{
		return _target;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public void setZoneEnabled(boolean value)
	{
		_enabled = value;
	}
	
	class ApplySkill implements Runnable
	{
		@Override
		public void run()
		{
			if (_enabled)
			{
				for (Creature temp : getCharactersInside())
				{
					if ((temp != null) && !temp.isDead() && (((temp instanceof Playable) && _target.equalsIgnoreCase("pc")) || ((temp instanceof PlayerInstance) && _target.equalsIgnoreCase("pc_only")) || ((temp instanceof MonsterInstance) && _target.equalsIgnoreCase("npc"))) && (Rnd.get(100) < _chance))
					{
						final Skill skill = getSkill();
						if (skill == null)
						{
							LOGGER.warning("ATTENTION: error on zone with id " + getId());
							LOGGER.warning("Skill " + _skillId + "," + _skillLvl + " not present between skills");
						}
						else
						{
							skill.getEffects(temp, temp, false, false, false);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onDieInside(Creature l2character)
	{
	}
	
	@Override
	public void onReviveInside(Creature l2character)
	{
	}
}
