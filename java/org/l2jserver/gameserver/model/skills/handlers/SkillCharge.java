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
package org.l2jserver.gameserver.model.skills.handlers;

import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.effects.EffectCharge;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class SkillCharge extends Skill
{
	public SkillCharge(StatSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature creature, WorldObject target, boolean itemOrWeapon)
	{
		if (creature instanceof PlayerInstance)
		{
			final EffectCharge e = (EffectCharge) creature.getFirstEffect(this);
			if ((e != null) && (e.numCharges >= getNumCharges()))
			{
				creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY));
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addSkillName(getId());
				creature.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(creature, target, itemOrWeapon);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		// get the effect
		EffectCharge effect = null;
		if (caster instanceof PlayerInstance)
		{
			effect = ((PlayerInstance) caster).getChargeEffect();
		}
		else
		{
			effect = (EffectCharge) caster.getFirstEffect(this);
		}
		
		if (effect != null)
		{
			if (effect.numCharges < getNumCharges())
			{
				effect.numCharges++;
				if (caster instanceof PlayerInstance)
				{
					caster.sendPacket(new EtcStatusUpdate((PlayerInstance) caster));
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL);
					sm.addNumber(effect.numCharges);
					caster.sendPacket(sm);
				}
			}
			else
			{
				caster.sendPacket(new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_2));
			}
			return;
		}
		getEffects(caster, caster, false, false, false);
		
		// cast self effect if any
		getEffectsSelf(caster);
	}
}
