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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.commons.util.StringUtil;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.ZoneType;
import org.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;

/**
 * another type of damage zone with skills
 * @author kerberos
 */
public class EffectZone extends ZoneType
{
	public static final Logger LOGGER = Logger.getLogger(EffectZone.class.getName());
	
	int _chance;
	private int _initialDelay;
	private int _reuse;
	boolean _enabled;
	private boolean _isShowDangerIcon;
	private Future<?> _task;
	protected Map<Integer, Integer> _skills;
	
	public EffectZone(int id)
	{
		super(id);
		_chance = 100;
		_initialDelay = 0;
		_reuse = 30000;
		_enabled = true;
		_isShowDangerIcon = true;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
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
			case "defaultStatus":
			{
				_enabled = Boolean.parseBoolean(value);
				break;
			}
			case "reuse":
			{
				_reuse = Integer.parseInt(value);
				break;
			}
			case "skillIdLvl":
			{
				final String[] propertySplit = value.split(";");
				_skills = new ConcurrentHashMap<>(propertySplit.length);
				for (String skill : propertySplit)
				{
					final String[] skillSplit = skill.split("-");
					if (skillSplit.length != 2)
					{
						LOGGER.warning(StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skill, "\""));
					}
					else
					{
						try
						{
							_skills.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								LOGGER.warning(StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skillSplit[0], "\"", skillSplit[1]));
							}
						}
					}
				}
				break;
			}
			case "showDangerIcon":
			{
				_isShowDangerIcon = Boolean.parseBoolean(value);
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
		if ((_skills != null) && (_task == null))
		{
			synchronized (this)
			{
				if (_task == null)
				{
					_task = ThreadPool.scheduleAtFixedRate(new ApplySkill(), _initialDelay, _reuse);
				}
			}
		}
		
		if ((creature instanceof PlayerInstance) && _isShowDangerIcon)
		{
			creature.setInsideZone(ZoneId.DANGER_AREA, true);
			creature.sendPacket(new EtcStatusUpdate((PlayerInstance) creature));
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if ((creature instanceof PlayerInstance) && _isShowDangerIcon)
		{
			creature.setInsideZone(ZoneId.DANGER_AREA, false);
			if (!creature.isInsideZone(ZoneId.DANGER_AREA))
			{
				creature.sendPacket(new EtcStatusUpdate((PlayerInstance) creature));
			}
		}
		
		if (getCharactersInside().isEmpty() && (_task != null))
		{
			_task.cancel(true);
			_task = null;
		}
	}
	
	protected Skill getSkill(int skillId, int skillLvl)
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public void addSkill(int skillId, int skillLvL)
	{
		if (skillLvL < 1) // remove skill
		{
			removeSkill(skillId);
			return;
		}
		
		if (_skills == null)
		{
			synchronized (this)
			{
				if (_skills == null)
				{
					_skills = new ConcurrentHashMap<>(3);
				}
			}
		}
		_skills.put(skillId, skillLvL);
	}
	
	public void removeSkill(int skillId)
	{
		if (_skills != null)
		{
			_skills.remove(skillId);
		}
	}
	
	public void clearSkills()
	{
		if (_skills != null)
		{
			_skills.clear();
		}
	}
	
	public int getSkillLevel(int skillId)
	{
		if ((_skills == null) || !_skills.containsKey(skillId))
		{
			return 0;
		}
		return _skills.get(skillId);
	}
	
	public void setZoneEnabled(boolean value)
	{
		_enabled = value;
	}
	
	class ApplySkill implements Runnable
	{
		ApplySkill()
		{
			if (_skills == null)
			{
				throw new IllegalStateException("No skills defined.");
			}
		}
		
		@Override
		public void run()
		{
			if (_enabled)
			{
				for (Creature temp : getCharactersInside())
				{
					if ((temp != null) && !temp.isDead())
					{
						if (!(temp instanceof Playable))
						{
							continue;
						}
						
						if (Rnd.get(100) < _chance)
						{
							for (Entry<Integer, Integer> e : _skills.entrySet())
							{
								final Skill skill = getSkill(e.getKey(), e.getValue());
								if (skill == null)
								{
									LOGGER.warning("ATTENTION: Skill " + e.getKey() + " cannot be loaded.. Verify Skill definition into data/stats/skill folder...");
									continue;
								}
								
								if (skill.checkCondition(temp, temp, false))
								{
									if (temp.getFirstEffect(e.getKey()) == null)
									{
										skill.getEffects(temp, temp);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onDieInside(Creature creature)
	{
	}
	
	@Override
	public void onReviveInside(Creature creature)
	{
	}
}