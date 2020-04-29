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

import org.l2jserver.Config;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.skills.BaseStat;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

/**
 * @author Steuf-Shyla
 */
public class Blow implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.BLOW
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
		final boolean ss = creature.checkSs();
		Formulas.getInstance();
		
		for (Creature target : (Creature[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// Check firstly if target dodges skill
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			byte successChance = 0;
			if (skill.getName().equals("Backstab"))
			{
				if (creature.isBehindTarget())
				{
					successChance = (byte) Config.BACKSTAB_ATTACK_BEHIND;
				}
				else if (creature.isFrontTarget())
				{
					successChance = (byte) Config.BACKSTAB_ATTACK_FRONT;
				}
				else
				{
					successChance = (byte) Config.BACKSTAB_ATTACK_SIDE;
				}
			}
			else if (creature.isBehindTarget())
			{
				successChance = (byte) Config.BLOW_ATTACK_BEHIND;
			}
			else if (creature.isFrontTarget())
			{
				successChance = (byte) Config.BLOW_ATTACK_FRONT;
			}
			else
			{
				successChance = (byte) Config.BLOW_ATTACK_SIDE;
			}
			
			boolean success = true;
			if ((skill.getCondition() & Skill.COND_CRIT) != 0)
			{
				success = (success && Formulas.getInstance().calcBlow(creature, target, successChance));
			}
			
			if (!skillIsEvaded && success)
			{
				if (skill.hasEffects())
				{
					target.stopSkillEffects(skill.getId());
					if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss))
					{
						skill.getEffects(creature, target, ss, sps, bss);
						final SystemMessage sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
						sm.addSkillName(skill);
						target.sendPacket(sm);
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
						sm.addSkillName(skill);
						creature.sendPacket(sm);
						return;
					}
				}
				
				final ItemInstance weapon = creature.getActiveWeaponInstance();
				boolean soul = false;
				if (weapon != null)
				{
					soul = (ss && (weapon.getItemType() == WeaponType.DAGGER));
				}
				
				final boolean shld = Formulas.calcShldUse(creature, target);
				
				// Critical hit
				boolean crit = false;
				
				// Critical damage condition is applied for sure if there is skill critical condition
				if ((skill.getCondition() & Skill.COND_CRIT) != 0)
				{
					crit = true; // if there is not critical condition, calculate critical chance
				}
				else if (Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStat.DEX.calcBonus(creature)))
				{
					crit = true;
				}
				
				double damage = Formulas.calcBlowDamage(creature, target, skill, shld, crit, soul);
				if (skill.getDmgDirectlyToHP() && (target instanceof PlayerInstance))
				{
					// no vegeange implementation
					final Creature[] ts =
					{
						target,
						creature
					};
					
					for (Creature targ : ts)
					{
						final PlayerInstance player = (PlayerInstance) targ;
						if (!player.isInvul())
						{
							// Check and calculate transfered damage
							final Summon summon = player.getPet();
							if ((summon instanceof SummonInstance) && Util.checkIfInRange(900, player, summon, true))
							{
								int tDmg = ((int) damage * (int) player.getStat().calcStat(Stat.TRANSFER_DAMAGE_PERCENT, 0, null, null)) / 100;
								
								// Only transfer dmg up to current HP, it should not be killed
								if (summon.getCurrentHp() < tDmg)
								{
									tDmg = (int) summon.getCurrentHp() - 1;
								}
								if (tDmg > 0)
								{
									summon.reduceCurrentHp(tDmg, creature);
									damage -= tDmg;
								}
							}
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
										// player.setDead(true);
										player.setIsPendingRevive(true);
										if (player.getPet() != null)
										{
											player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
										}
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
						smsg.addNumber((int) damage);
						player.sendPacket(smsg);
						
						// stop if no vengeance, so only target will be effected
						if (!player.vengeanceSkill(skill))
						{
							break;
						}
					}
				}
				else
				{
					target.reduceCurrentHp(damage, creature);
					
					// vengeance reflected damage
					if (target.vengeanceSkill(skill))
					{
						creature.reduceCurrentHp(damage, target);
					}
				}
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				if (creature instanceof PlayerInstance)
				{
					final PlayerInstance activePlayer = (PlayerInstance) creature;
					activePlayer.sendDamageMessage(target, (int) damage, false, true, false);
					if (activePlayer.isInOlympiadMode() && (target instanceof PlayerInstance) && ((PlayerInstance) target).isInOlympiadMode() && (((PlayerInstance) target).getOlympiadGameId() == activePlayer.getOlympiadGameId()))
					{
						Olympiad.getInstance().notifyCompetitorDamage(activePlayer, (int) damage, activePlayer.getOlympiadGameId());
					}
				}
				
				// Possibility of a lethal strike
				Formulas.calcLethalHit(creature, target, skill);
				creature.sendPacket(new PlaySound("skillsound.critical_hit_02"));
			}
			else
			{
				if (skillIsEvaded && (target instanceof PlayerInstance))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_AVOIDED_S1_S_ATTACK);
					sm.addString(creature.getName());
					((PlayerInstance) target).sendPacket(sm);
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
				sm.addSkillName(skill);
				creature.sendPacket(sm);
				return;
			}
			
			// Self Effect
			if (skill.hasSelfEffects())
			{
				final Effect effect = creature.getFirstEffect(skill.getId());
				if ((effect != null) && effect.isSelfEffect())
				{
					effect.exit(false);
				}
				skill.getEffectsSelf(creature);
			}
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
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}