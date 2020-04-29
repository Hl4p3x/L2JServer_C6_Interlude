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
package org.l2jserver.gameserver.model.actor.stat;

import org.l2jserver.gameserver.datatables.sql.PetDataTable;
import org.l2jserver.gameserver.datatables.xml.ExperienceData;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.PetInfo;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class PetStat extends SummonStat
{
	public PetStat(PetInstance activeChar)
	{
		super(activeChar);
	}
	
	public boolean addExp(int value)
	{
		if (!super.addExp(value))
		{
			return false;
		}
		
		getActiveChar().broadcastPacket(new PetInfo(getActiveChar()));
		// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them.
		getActiveChar().updateEffectIcons(true);
		
		return true;
	}
	
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_PET_GAINED_S1_EXPERIENCE_POINTS);
		sm.addNumber((int) addToExp);
		getActiveChar().getOwner().sendPacket(sm);
		
		return true;
	}
	
	@Override
	public boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (ExperienceData.getInstance().getMaxLevel() - 1))
		{
			return false;
		}
		
		final boolean levelIncreased = super.addLevel(value);
		
		// Sync up exp with current level
		if ((getExp() > getExpForLevel(getLevel() + 1)) || (getExp() < getExpForLevel(getLevel())))
		{
			setExp(ExperienceData.getInstance().getExpForLevel(getLevel()));
		}
		
		if (levelIncreased)
		{
			getActiveChar().getOwner().sendMessage("Your pet has increased it's level.");
		}
		
		final StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().broadcastPacket(su);
		
		// Send a Server->Client packet PetInfo to the PlayerInstance
		getActiveChar().getOwner().sendPacket(new PetInfo(getActiveChar()));
		// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
		getActiveChar().updateEffectIcons(true);
		
		if (getActiveChar().getControlItem() != null)
		{
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
		}
		
		return levelIncreased;
	}
	
	@Override
	public long getExpForLevel(int level)
	{
		return PetDataTable.getInstance().getPetData(getActiveChar().getNpcId(), level).getPetMaxExp();
	}
	
	@Override
	public PetInstance getActiveChar()
	{
		return (PetInstance) super.getActiveChar();
	}
	
	public int getFeedBattle()
	{
		return getActiveChar().getPetData().getPetFeedBattle();
	}
	
	public int getFeedNormal()
	{
		return getActiveChar().getPetData().getPetFeedNormal();
	}
	
	@Override
	public void setLevel(int value)
	{
		getActiveChar().stopFeed();
		super.setLevel(value);
		
		getActiveChar().setPetData(PetDataTable.getInstance().getPetData(getActiveChar().getTemplate().getNpcId(), getLevel()));
		getActiveChar().startFeed(false);
		
		if (getActiveChar().getControlItem() != null)
		{
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
		}
	}
	
	public int getMaxFeed()
	{
		return getActiveChar().getPetData().getPetMaxFeed();
	}
	
	@Override
	public int getMaxHp()
	{
		return (int) calcStat(Stat.MAX_HP, getActiveChar().getPetData().getPetMaxHP(), null, null);
	}
	
	@Override
	public int getMaxMp()
	{
		return (int) calcStat(Stat.MAX_MP, getActiveChar().getPetData().getPetMaxMP(), null, null);
	}
	
	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		double attack = getActiveChar().getPetData().getPetMAtk();
		final Stat stat = skill == null ? null : skill.getStat();
		if (stat != null)
		{
			switch (stat)
			{
				case AGGRESSION:
				{
					attack += getActiveChar().getTemplate().getBaseAggression();
					break;
				}
				case BLEED:
				{
					attack += getActiveChar().getTemplate().getBaseBleed();
					break;
				}
				case POISON:
				{
					attack += getActiveChar().getTemplate().getBasePoison();
					break;
				}
				case STUN:
				{
					attack += getActiveChar().getTemplate().getBaseStun();
					break;
				}
				case ROOT:
				{
					attack += getActiveChar().getTemplate().getBaseRoot();
					break;
				}
				case MOVEMENT:
				{
					attack += getActiveChar().getTemplate().getBaseMovement();
					break;
				}
				case CONFUSION:
				{
					attack += getActiveChar().getTemplate().getBaseConfusion();
					break;
				}
				case SLEEP:
				{
					attack += getActiveChar().getTemplate().getBaseSleep();
					break;
				}
				case FIRE:
				{
					attack += getActiveChar().getTemplate().getBaseFire();
					break;
				}
				case WIND:
				{
					attack += getActiveChar().getTemplate().getBaseWind();
					break;
				}
				case WATER:
				{
					attack += getActiveChar().getTemplate().getBaseWater();
					break;
				}
				case EARTH:
				{
					attack += getActiveChar().getTemplate().getBaseEarth();
					break;
				}
				case HOLY:
				{
					attack += getActiveChar().getTemplate().getBaseHoly();
					break;
				}
				case DARK:
				{
					attack += getActiveChar().getTemplate().getBaseDark();
					break;
				}
			}
		}
		
		if (skill != null)
		{
			attack += skill.getPower();
		}
		
		return (int) calcStat(Stat.MAGIC_ATTACK, attack, target, skill);
	}
	
	@Override
	public int getMDef(Creature target, Skill skill)
	{
		final double defence = getActiveChar().getPetData().getPetMDef();
		return (int) calcStat(Stat.MAGIC_DEFENCE, defence, target, skill);
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stat.POWER_ATTACK, getActiveChar().getPetData().getPetPAtk(), target, null);
	}
	
	@Override
	public int getPDef(Creature target)
	{
		return (int) calcStat(Stat.POWER_DEFENCE, getActiveChar().getPetData().getPetPDef(), target, null);
	}
	
	@Override
	public int getAccuracy()
	{
		return (int) calcStat(Stat.ACCURACY_COMBAT, getActiveChar().getPetData().getPetAccuracy(), null, null);
	}
	
	@Override
	public int getCriticalHit(Creature target, Skill skill)
	{
		return (int) calcStat(Stat.CRITICAL_RATE, getActiveChar().getPetData().getPetCritical(), target, null);
	}
	
	@Override
	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stat.EVASION_RATE, getActiveChar().getPetData().getPetEvasion(), target, null);
	}
	
	@Override
	public int getRunSpeed()
	{
		return (int) calcStat(Stat.RUN_SPEED, getActiveChar().getPetData().getPetSpeed(), null, null);
	}
	
	@Override
	public int getPAtkSpd()
	{
		return (int) calcStat(Stat.POWER_ATTACK_SPEED, getActiveChar().getPetData().getPetAtkSpeed(), null, null);
	}
	
	@Override
	public int getMAtkSpd()
	{
		return (int) calcStat(Stat.MAGIC_ATTACK_SPEED, getActiveChar().getPetData().getPetCastSpeed(), null, null);
	}
}
