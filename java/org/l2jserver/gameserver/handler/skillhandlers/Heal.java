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
import org.l2jserver.gameserver.handler.SkillHandler;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Heal implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.HEAL,
		SkillType.HEAL_PERCENT,
		SkillType.HEAL_STATIC
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		PlayerInstance player = null;
		if (creature instanceof PlayerInstance)
		{
			player = (PlayerInstance) creature;
		}
		
		final boolean bss = creature.checkBss();
		final boolean sps = creature.checkSps();
		
		// check for other effects
		try
		{
			final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);
			if (handler != null)
			{
				handler.useSkill(creature, skill, targets);
			}
		}
		catch (Exception e)
		{
		}
		
		Creature target = null;
		for (WorldObject target2 : targets)
		{
			target = (Creature) target2;
			if ((target == null) || target.isDead() || target.isInvul())
			{
				continue;
			}
			
			// Avoid players heal inside Baium lair from outside
			if (((GrandBossManager.getInstance().getZone(player) == null) && (GrandBossManager.getInstance().getZone(target) != null)) || ((GrandBossManager.getInstance().getZone(target) == null) && (GrandBossManager.getInstance().getZone(creature) != null)))
			{
				continue;
			}
			
			// We should not heal walls and door
			if (target instanceof DoorInstance)
			{
				continue;
			}
			
			// We should not heal siege flags
			if ((target instanceof NpcInstance) && (((NpcInstance) target).getNpcId() == 35062))
			{
				creature.getActingPlayer().sendMessage("You cannot heal siege flags!");
				continue;
			}
			
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != creature)
			{
				if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isCursedWeaponEquiped())
				{
					continue;
				}
				else if ((player != null) && player.isCursedWeaponEquiped())
				{
					continue;
				}
			}
			
			// Fixed about Infinity Rod skill on Raid Boss and BigBoss
			if ((skill.getId() == 3598) && ((target instanceof RaidBossInstance) || (target instanceof GrandBossInstance)))
			{
				continue;
			}
			
			double hp = skill.getPower();
			if (skill.getSkillType() == SkillType.HEAL_PERCENT)
			{
				hp = (target.getMaxHp() * hp) / 100.0;
			}
			else if (bss)
			{
				hp *= 1.5;
			}
			else if (sps)
			{
				hp *= 1.3;
			}
			
			if (skill.getSkillType() == SkillType.HEAL_STATIC)
			{
				hp = skill.getPower();
			}
			else if (skill.getSkillType() != SkillType.HEAL_PERCENT)
			{
				hp *= target.calcStat(Stat.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			}
			
			target.setCurrentHp(hp + target.getCurrentHp());
			target.setLastHealAmount((int) hp);
			final StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
			
			if (target instanceof PlayerInstance)
			{
				if (skill.getId() == 4051)
				{
					target.sendPacket(new SystemMessage(SystemMessageId.REJUVENATING_HP));
				}
				else if ((creature instanceof PlayerInstance) && (creature != target))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_HAS_BEEN_RESTORED_BY_S1);
					sm.addString(creature.getName());
					sm.addNumber((int) hp);
					target.sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_HAS_BEEN_RESTORED);
					sm.addNumber((int) hp);
					target.sendPacket(sm);
				}
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}