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

import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.handler.SkillHandler;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author kombat
 */
public class ChanceSkillList extends ConcurrentHashMap<Skill, ChanceCondition>
{
	private Creature _owner;
	
	public ChanceSkillList(Creature owner)
	{
		super();
		_owner = owner;
	}
	
	public Creature getOwner()
	{
		return _owner;
	}
	
	public void setOwner(Creature owner)
	{
		_owner = owner;
	}
	
	public void onHit(Creature target, boolean ownerWasHit, boolean wasCrit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
			}
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_CRIT;
			}
		}
		
		onEvent(event, target);
	}
	
	public void onSkillHit(Creature target, boolean ownerWasHit, boolean wasMagic, boolean wasOffensive)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (wasOffensive)
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			event = ChanceCondition.EVT_CAST;
			event |= wasMagic ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= wasOffensive ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}
		
		onEvent(event, target);
	}
	
	public static boolean canTriggerByCast(Creature caster, Creature target, Skill trigger)
	{
		// crafting does not trigger any chance skills
		// possibly should be unhardcoded
		switch (trigger.getSkillType())
		{
			case COMMON_CRAFT:
			case DWARVEN_CRAFT:
			{
				return false;
			}
		}
		
		if (trigger.isToggle() || trigger.isPotion() || !trigger.isMagic())
		{
			return false; // No buffing with toggle skills or potions
		}
		
		if (trigger.getId() == 1320)
		{
			return false; // No buffing with Common
		}
		
		if (trigger.isOffensive() && !Formulas.calcMagicSuccess(caster, target, trigger))
		{
			return false; // Low grade skills won't trigger for high level targets
		}
		
		return true;
	}
	
	public void onEvent(int event, Creature target)
	{
		for (Entry<Skill, ChanceCondition> e : entrySet())
		{
			if ((e.getValue() != null) && e.getValue().trigger(event))
			{
				makeCast(e.getKey(), target);
			}
		}
	}
	
	private void makeCast(Skill skill, Creature target)
	{
		try
		{
			if (skill.calcWeaponDependancy(_owner))
			{
				if (skill.triggerAnotherSkill()) // should we use this skill or this skill is just referring to another one ...
				{
					skill = _owner.getSkills().get(skill.getTriggeredId());
					if (skill == null)
					{
						return;
					}
				}
				
				final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
				final WorldObject[] targets = skill.getTargetList(_owner, false, target);
				_owner.broadcastPacket(new MagicSkillLaunched(_owner, skill.getDisplayId(), skill.getLevel(), targets));
				_owner.broadcastPacket(new MagicSkillUse(_owner, (Creature) targets[0], skill.getDisplayId(), skill.getLevel(), 0, 0));
				
				// Launch the magic skill and calculate its effects
				if (handler != null)
				{
					handler.useSkill(_owner, skill, targets);
				}
				else
				{
					skill.useSkill(_owner, targets);
				}
			}
		}
		catch (Exception e)
		{
			// null
		}
	}
}
