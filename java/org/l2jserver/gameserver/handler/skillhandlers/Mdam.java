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
package org.l2jserver.gameserver.handler.skillhandlers;

import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Mdam implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.MDAM,
		SkillType.DEATHLINK
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		if (creature.isAlikeDead())
		{
			return;
		}
		
		final boolean bss = creature.checkBss();
		final boolean sps = creature.checkSps();
		for (WorldObject target2 : targets)
		{
			if (target2 == null)
			{
				continue;
			}
			
			final Creature target = (Creature) target2;
			if ((creature instanceof PlayerInstance) && (target instanceof PlayerInstance) && target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				if ((skill.getTargetType() == Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB) && (target instanceof NpcInstance))
				{
					((NpcInstance) target).endDecayTask();
				}
				continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(creature.getMCriticalHit(target, skill));
			final int damage = (int) Formulas.calcMagicDam(creature, target, skill, sps, bss, mcrit);
			
			// Why are we trying to reduce the current target HP here?
			// Why not inside the below "if" condition, after the effects processing as it should be?
			// It doesn't seem to make sense for me. I'm moving this line inside the "if" condition, right after the effects processing...
			// [changed by nexus - 2006-08-15]
			// target.reduceCurrentHp(damage, activeChar);
			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				creature.sendDamageMessage(target, damage, mcrit, false, false);
				if (skill.hasEffects())
				{
					if (target.reflectSkill(skill))
					{
						creature.stopSkillEffects(skill.getId());
						skill.getEffects(null, creature, false, sps, bss);
						final SystemMessage sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
						sm.addSkillName(skill.getId());
						creature.sendPacket(sm);
					}
					else if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, false, sps, bss)) // activate attacked effects, if any
					{
						// Like L2OFF must remove the first effect only if the second effect is successful
						target.stopSkillEffects(skill.getId());
						skill.getEffects(creature, target, false, sps, bss);
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getDisplayId());
						creature.sendPacket(sm);
					}
				}
				
				target.reduceCurrentHp(damage, creature);
			}
		}
		
		if (bss)
		{
			creature.removeBss();
		}
		else if (sps)
		{
			creature.removeSps();
		}
		
		// self Effect :]
		final Effect effect = creature.getFirstEffect(skill.getId());
		if ((effect != null) && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit(false);
		}
		skill.getEffectsSelf(creature);
		
		if (skill.isSuicideAttack())
		{
			creature.doDie(null);
			creature.setCurrentHp(0);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
