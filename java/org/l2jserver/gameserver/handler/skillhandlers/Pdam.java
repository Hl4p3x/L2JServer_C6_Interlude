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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.skills.BaseStat;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.model.skills.effects.EffectCharge;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Pdam implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.PDAM,
		SkillType.FATALCOUNTER
		/* , SkillType.CHARGEDAM */
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		if (creature.isAlikeDead())
		{
			return;
		}
		
		int damage = 0;
		
		// Calculate targets based on vegeance
		final List<WorldObject> result = new ArrayList<>();
		for (WorldObject wo : targets)
		{
			result.add(wo);
			
			final Creature target = (Creature) wo;
			if (target.vengeanceSkill(skill))
			{
				result.add(creature);
			}
		}
		
		final boolean bss = creature.checkBss();
		final boolean sps = creature.checkSps();
		final boolean ss = creature.checkSs();
		for (WorldObject target2 : result)
		{
			if (target2 == null)
			{
				continue;
			}
			
			final Creature target = (Creature) target2;
			final Formulas f = Formulas.getInstance();
			final ItemInstance weapon = creature.getActiveWeaponInstance();
			if ((creature instanceof PlayerInstance) && (target instanceof PlayerInstance) && target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				continue;
			}
			
			// Calculate skill evasion
			if (Formulas.calcPhysicalSkillEvasion(target, skill))
			{
				creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED));
				continue;
			}
			
			final boolean dual = creature.isUsingDualWeapon();
			final boolean shld = Formulas.calcShldUse(creature, target);
			// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
			boolean crit = false;
			if (skill.getBaseCritRate() > 0)
			{
				crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStat.STR.calcBonus(creature));
			}
			
			boolean soul = false;
			if (weapon != null)
			{
				soul = (ss && (weapon.getItemType() != WeaponType.DAGGER));
			}
			
			if (!crit && ((skill.getCondition() & Skill.COND_CRIT) != 0))
			{
				damage = 0;
			}
			else
			{
				damage = (int) Formulas.calcPhysDam(creature, target, skill, shld, false, dual, soul);
			}
			
			if (crit)
			{
				damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
			}
			
			if (damage > 0)
			{
				if (target != creature)
				{
					creature.sendDamageMessage(target, damage, false, crit, false);
				}
				else
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.S1_HIT_YOU_FOR_S2_DAMAGE);
					smsg.addString(target.getName());
					smsg.addNumber(damage);
					creature.sendPacket(smsg);
				}
				
				if (!target.isInvul() && skill.hasEffects())
				{
					if (target.reflectSkill(skill))
					{
						creature.stopSkillEffects(skill.getId());
						
						skill.getEffects(null, creature, ss, sps, bss);
						final SystemMessage sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
						sm.addSkillName(skill.getId());
						creature.sendPacket(sm);
					}
					else if (f.calcSkillSuccess(creature, target, skill, soul, false, false)) // activate attacked effects, if any
					{
						// Like L2OFF must remove the first effect if the second effect lands
						skill.getEffects(creature, target, ss, sps, bss);
						final SystemMessage sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
						sm.addSkillName(skill.getId());
						target.sendPacket(sm);
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getDisplayId());
						creature.sendPacket(sm);
					}
				}
				
				// Success of lethal effect
				final int chance = Rnd.get(1000);
				if ((target != creature) && !target.isRaid() && (chance < skill.getLethalChance1()) && !(target instanceof DoorInstance) && (!(target instanceof NpcInstance) || (((NpcInstance) target).getNpcId() != 35062)))
				{
					// 1st lethal effect activate (cp to 1 or if target is npc then hp to 50%)
					if ((skill.getLethalChance2() > 0) && (chance >= skill.getLethalChance2()))
					{
						if (target instanceof PlayerInstance)
						{
							final PlayerInstance player = (PlayerInstance) target;
							if (!player.isInvul())
							{
								player.setCurrentCp(1); // Set CP to 1
								player.reduceCurrentHp(damage, creature);
							}
						}
						else if (target instanceof MonsterInstance) // If is a monster remove first damage and after 50% of current hp
						{
							target.reduceCurrentHp(damage, creature);
							target.reduceCurrentHp(target.getCurrentHp() / 2, creature);
						}
						// Half Kill!
						creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
					else // 2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
					{
						// If is a monster damage is (CurrentHp - 1) so HP = 1
						if (target instanceof NpcInstance)
						{
							target.reduceCurrentHp(target.getCurrentHp() - 1, creature);
						}
						else if (target instanceof PlayerInstance) // If is a active player set his HP and CP to 1
						{
							final PlayerInstance player = (PlayerInstance) target;
							if (!player.isInvul())
							{
								player.setCurrentHp(1);
								player.setCurrentCp(1);
							}
						}
						// Lethal Strike was succefful!
						creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL));
					}
				}
				else if (skill.getDmgDirectlyToHP() || !(creature instanceof Playable)) // Make damage directly to HP
				{
					if (target instanceof PlayerInstance)
					{
						final PlayerInstance player = (PlayerInstance) target;
						if (!player.isInvul())
						{
							if (damage >= player.getCurrentHp())
							{
								if (player.isInDuel())
								{
									player.setCurrentHp(1);
								}
								else
								{
									player.setCurrentHp(0);
									if (player.isInOlympiadMode())
									{
										player.abortAttack();
										player.abortCast();
										player.getStatus().stopHpMpRegeneration();
									}
									else
									{
										player.doDie(creature);
									}
								}
							}
							else
							{
								player.setCurrentHp(player.getCurrentHp() - damage);
							}
						}
						final SystemMessage smsg = new SystemMessage(SystemMessageId.S1_HIT_YOU_FOR_S2_DAMAGE);
						smsg.addString(creature.getName());
						smsg.addNumber(damage);
						player.sendPacket(smsg);
					}
					else
					{
						target.reduceCurrentHp(damage, creature);
					}
				}
				else if ((creature instanceof PlayerInstance) && (target instanceof PlayerInstance) && !target.isInvul()) // only players can reduce CPs each other
				{
					final PlayerInstance player = (PlayerInstance) target;
					double hpDamage = 0;
					if (damage >= player.getCurrentCp())
					{
						final double cur_cp = player.getCurrentCp();
						hpDamage = damage - cur_cp;
						player.setCurrentCp(1);
					}
					else
					{
						final double cur_cp = player.getCurrentCp();
						player.setCurrentCp(cur_cp - damage);
					}
					
					if (hpDamage > 0)
					{
						player.reduceCurrentHp(damage, creature);
					}
				}
				else
				{
					target.reduceCurrentHp(damage, creature);
				}
			}
			else // No - damage
			{
				creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED));
			}
			
			if ((skill.getId() == 345) || (skill.getId() == 346)) // Sonic Rage or Raging Force
			{
				final EffectCharge effect = (EffectCharge) creature.getFirstEffect(Effect.EffectType.CHARGE);
				if (effect != null)
				{
					int effectcharge = effect.getLevel();
					if (effectcharge < 7)
					{
						effectcharge++;
						effect.addNumCharges(1);
						
						creature.sendPacket(new EtcStatusUpdate((PlayerInstance) creature));
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL);
						sm.addNumber(effectcharge);
						creature.sendPacket(sm);
					}
					else
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY));
					}
				}
				else if (skill.getId() == 345) // Sonic Rage
				{
					final Skill dummy = SkillTable.getInstance().getInfo(8, 7); // Lv7 Sonic Focus
					dummy.getEffects(creature, creature, ss, sps, bss);
				}
				else if (skill.getId() == 346) // Raging Force
				{
					final Skill dummy = SkillTable.getInstance().getInfo(50, 7); // Lv7 Focused Force
					dummy.getEffects(creature, creature, ss, sps, bss);
				}
			}
			// self Effect :]
			final Effect effect = creature.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				// Replace old effect with new one.
				effect.exit(false);
			}
			skill.getEffectsSelf(creature);
		}
		
		if (skill.isMagic())
		{
			if (bss)
			{
				creature.removeBss();
			}
			else if (sps)
			{
				creature.removeSps();
			}
		}
		else
		{
			creature.removeSs();
		}
		
		if (skill.isSuicideAttack() && !creature.isInvul())
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