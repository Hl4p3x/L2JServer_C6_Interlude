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

import org.l2jserver.Config;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.Calculator;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.model.zone.ZoneId;

public class CreatureStat
{
	private final Creature _creature;
	private long _exp = 0;
	private int _sp = 0;
	private int _level = 1;
	/** Speed multiplier set by admin gmspeed command */
	private float _gmSpeedMultiplier = 1;
	
	/**
	 * Instantiates a new char stat.
	 * @param creature the creature
	 */
	public CreatureStat(Creature creature)
	{
		_creature = creature;
	}
	
	/**
	 * Calculate the new value of the state with modifiers that will be applied on the targeted Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * A Creature owns a table of Calculators called <b>_calculators</b>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) :<br>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br>
	 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <b>_order</b>. Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in
	 * the value property of an Env class instance.
	 * @param stat The stat to calculate the new value with modifiers
	 * @param init The initial value of the stat before applying modifiers
	 * @param target The Creature whose properties will be used in the calculation (ex : CON, INT...)
	 * @param skill The Skill whose properties will be used in the calculation (ex : Level...)
	 * @return the double
	 */
	public double calcStat(Stat stat, double init, Creature target, Skill skill)
	{
		if (_creature == null)
		{
			return init;
		}
		
		final int id = stat.ordinal();
		final Calculator c = _creature.getCalculators()[id];
		
		// If no Func object found, no modifier is applied
		if ((c == null) || (c.size() == 0))
		{
			return init;
		}
		
		// Create and init an Env object to pass parameters to the Calculator
		final Env env = new Env();
		env.player = _creature;
		env.target = target;
		env.skill = skill;
		env.value = init;
		env.baseValue = init;
		
		// Launch the calculation
		c.calc(env);
		// avoid some troubles with negative stats (some stats should never be negative)
		if (env.value <= 0)
		{
			switch (stat)
			{
				case MAX_HP:
				case MAX_MP:
				case MAX_CP:
				case MAGIC_DEFENCE:
				case POWER_DEFENCE:
				case POWER_ATTACK:
				case MAGIC_ATTACK:
				case POWER_ATTACK_SPEED:
				case MAGIC_ATTACK_SPEED:
				case SHIELD_DEFENCE:
				case STAT_CON:
				case STAT_DEX:
				case STAT_INT:
				case STAT_MEN:
				case STAT_STR:
				case STAT_WIT:
				{
					env.value = 1;
				}
			}
		}
		
		return env.value;
	}
	
	/**
	 * Return the Accuracy (base+modifier) of the Creature in function of the Weapon Expertise Penalty.
	 * @return the accuracy
	 */
	public int getAccuracy()
	{
		if (_creature == null)
		{
			return 0;
		}
		return (int) (calcStat(Stat.ACCURACY_COMBAT, 0, null, null) / _creature.getWeaponExpertisePenalty());
	}
	
	/**
	 * Gets the active char.
	 * @return the active char
	 */
	public Creature getActiveChar()
	{
		return _creature;
	}
	
	/**
	 * Return the Attack Speed multiplier (base+modifier) of the Creature to get proper animations.
	 * @return the attack speed multiplier
	 */
	public float getAttackSpeedMultiplier()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (float) ((1.1 * getPAtkSpd()) / _creature.getTemplate().getBasePAtkSpd());
	}
	
	/**
	 * Return the CON of the Creature (base+modifier).
	 * @return the CON
	 */
	public int getCON()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.STAT_CON, _creature.getTemplate().getBaseCON(), null, null);
	}
	
	/**
	 * Return the Critical Damage rate (base+modifier) of the Creature.
	 * @param target the target
	 * @param init the init
	 * @return the critical dmg
	 */
	public double getCriticalDmg(Creature target, double init)
	{
		return calcStat(Stat.CRITICAL_DAMAGE, init, target, null);
	}
	
	/**
	 * Return the Critical Hit rate (base+modifier) of the Creature.
	 * @param target the target
	 * @param skill the skill
	 * @return the critical hit
	 */
	public int getCriticalHit(Creature target, Skill skill)
	{
		if (_creature == null)
		{
			return 1;
		}
		
		int criticalHit = (int) ((calcStat(Stat.CRITICAL_RATE, _creature.getTemplate().getBaseCritRate(), target, skill) * 10.0) + 0.5);
		criticalHit /= 10;
		
		// Set a cap of Critical Hit at 500
		if (criticalHit > Config.MAX_PCRIT_RATE)
		{
			criticalHit = Config.MAX_PCRIT_RATE;
		}
		
		return criticalHit;
	}
	
	/**
	 * Return the DEX of the Creature (base+modifier).
	 * @return the DEX
	 */
	public int getDEX()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.STAT_DEX, _creature.getTemplate().getBaseDEX(), null, null);
	}
	
	/**
	 * Return the Attack Evasion rate (base+modifier) of the Creature.
	 * @param target the target
	 * @return the evasion rate
	 */
	public int getEvasionRate(Creature target)
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) (calcStat(Stat.EVASION_RATE, 0, target, null) / _creature.getArmourExpertisePenalty());
	}
	
	/**
	 * Gets the exp.
	 * @return the exp
	 */
	public long getExp()
	{
		return _exp;
	}
	
	/**
	 * Sets the exp.
	 * @param value the new exp
	 */
	public void setExp(long value)
	{
		_exp = value;
	}
	
	/**
	 * Return the INT of the Creature (base+modifier).
	 * @return the INT
	 */
	public int getINT()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.STAT_INT, _creature.getTemplate().getBaseINT(), null, null);
	}
	
	/**
	 * Gets the level.
	 * @return the level
	 */
	public int getLevel()
	{
		return _level;
	}
	
	/**
	 * Sets the level.
	 * @param value the new level
	 */
	public void setLevel(int value)
	{
		_level = value;
	}
	
	/**
	 * Return the Magical Attack range (base+modifier) of the Creature.
	 * @param skill the skill
	 * @return the magical attack range
	 */
	public int getMagicalAttackRange(Skill skill)
	{
		if (skill != null)
		{
			return (int) calcStat(Stat.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		
		if (_creature == null)
		{
			return 1;
		}
		
		return _creature.getTemplate().getBaseAtkRange();
	}
	
	/**
	 * Gets the max cp.
	 * @return the max cp
	 */
	public int getMaxCp()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.MAX_CP, _creature.getTemplate().getBaseCpMax(), null, null);
	}
	
	/**
	 * Gets the max hp.
	 * @return the max hp
	 */
	public int getMaxHp()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.MAX_HP, _creature.getTemplate().getBaseHpMax(), null, null);
	}
	
	/**
	 * Gets the max mp.
	 * @return the max mp
	 */
	public int getMaxMp()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.MAX_MP, _creature.getTemplate().getBaseMpMax(), null, null);
	}
	
	/**
	 * Return the MAtk (base+modifier) of the Creature for a skill used in function of abnormal effects in progress.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Calculate Magic damage</li>
	 * @param target The Creature targeted by the skill
	 * @param skill The Skill used against the target
	 * @return the m atk
	 */
	public int getMAtk(Creature target, Skill skill)
	{
		if (_creature == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _creature.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}
		
		double attack = _creature.getTemplate().getBaseMAtk() * bonusAtk;
		
		// Get the skill type to calculate its effect in function of base stats of the Creature target.
		final Stat stat = skill == null ? null : skill.getStat();
		if (stat != null)
		{
			switch (stat)
			{
				case AGGRESSION:
				{
					attack += _creature.getTemplate().getBaseAggression();
					break;
				}
				case BLEED:
				{
					attack += _creature.getTemplate().getBaseBleed();
					break;
				}
				case POISON:
				{
					attack += _creature.getTemplate().getBasePoison();
					break;
				}
				case STUN:
				{
					attack += _creature.getTemplate().getBaseStun();
					break;
				}
				case ROOT:
				{
					attack += _creature.getTemplate().getBaseRoot();
					break;
				}
				case MOVEMENT:
				{
					attack += _creature.getTemplate().getBaseMovement();
					break;
				}
				case CONFUSION:
				{
					attack += _creature.getTemplate().getBaseConfusion();
					break;
				}
				case SLEEP:
				{
					attack += _creature.getTemplate().getBaseSleep();
					break;
				}
				case FIRE:
				{
					attack += _creature.getTemplate().getBaseFire();
					break;
				}
				case WIND:
				{
					attack += _creature.getTemplate().getBaseWind();
					break;
				}
				case WATER:
				{
					attack += _creature.getTemplate().getBaseWater();
					break;
				}
				case EARTH:
				{
					attack += _creature.getTemplate().getBaseEarth();
					break;
				}
				case HOLY:
				{
					attack += _creature.getTemplate().getBaseHoly();
					break;
				}
				case DARK:
				{
					attack += _creature.getTemplate().getBaseDark();
					break;
				}
			}
		}
		
		// Add the power of the skill to the attack effect
		if (skill != null)
		{
			attack += skill.getPower();
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stat.MAGIC_ATTACK, attack, target, skill);
	}
	
	/**
	 * Return the MAtk Speed (base+modifier) of the Creature in function of the Armour Expertise Penalty.
	 * @return the m atk spd
	 */
	public int getMAtkSpd()
	{
		if (_creature == null)
		{
			return 1;
		}
		
		float bonusSpdAtk = 1;
		if (Config.CHAMPION_ENABLE && _creature.isChampion())
		{
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		}
		
		double val = calcStat(Stat.MAGIC_ATTACK_SPEED, _creature.getTemplate().getBaseMAtkSpd() * bonusSpdAtk, null, null);
		val /= _creature.getArmourExpertisePenalty();
		if ((val > Config.MAX_MATK_SPEED) && (_creature instanceof PlayerInstance))
		{
			val = Config.MAX_MATK_SPEED;
		}
		
		return (int) val;
	}
	
	/**
	 * Return the Magic Critical Hit rate (base+modifier) of the Creature.
	 * @param target the target
	 * @param skill the skill
	 * @return the m critical hit
	 */
	public int getMCriticalHit(Creature target, Skill skill)
	{
		if (_creature == null)
		{
			return 1;
		}
		
		double mrate = calcStat(Stat.MCRITICAL_RATE, (_creature.getTemplate().getBaseMCritRate()) * Config.MCRIT_RATE_MUL, target, skill);
		if (mrate > Config.MAX_MCRIT_RATE)
		{
			mrate = Config.MAX_MCRIT_RATE;
		}
		
		return (int) mrate;
	}
	
	/**
	 * Return the MDef (base+modifier) of the Creature against a skill in function of abnormal effects in progress.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Calculate Magic damage</li>
	 * @param target The Creature targeted by the skill
	 * @param skill The Skill used against the target
	 * @return the m def
	 */
	public int getMDef(Creature target, Skill skill)
	{
		if (_creature == null)
		{
			return 1;
		}
		
		// Get the base MDef of the Creature
		double defence = _creature.getTemplate().getBaseMDef();
		
		// Calculate modifier for Raid Bosses
		if (_creature.isRaid())
		{
			defence *= Config.RAID_M_DEFENCE_MULTIPLIER;
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stat.MAGIC_DEFENCE, defence, target, skill);
	}
	
	/**
	 * Return the MEN of the Creature (base+modifier).
	 * @return the MEN
	 */
	public int getMEN()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.STAT_MEN, _creature.getTemplate().getBaseMEN(), null, null);
	}
	
	/**
	 * Gets the movement speed multiplier.
	 * @return the movement speed multiplier
	 */
	public float getMovementSpeedMultiplier()
	{
		if (_creature == null)
		{
			return 1;
		}
		return Math.max(1, getRunSpeed() / _creature.getTemplate().getBaseRunSpd());
	}
	
	public void setGmSpeedMultiplier(float multipier)
	{
		_gmSpeedMultiplier = multipier;
	}
	
	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the Creature in function of the movement type.
	 * @return the move speed
	 */
	public float getMoveSpeed()
	{
		if (_creature == null)
		{
			return 1;
		}
		
		if (_creature.isRunning())
		{
			return getRunSpeed();
		}
		
		return getWalkSpeed();
	}
	
	/**
	 * Return the MReuse rate (base+modifier) of the Creature.
	 * @param skill the skill
	 * @return the m reuse rate
	 */
	public double getMReuseRate(Skill skill)
	{
		if (_creature == null)
		{
			return 1;
		}
		return calcStat(Stat.MAGIC_REUSE_RATE, _creature.getTemplate().getBaseMReuseRate(), null, skill);
	}
	
	/**
	 * Return the PReuse rate (base+modifier) of the Creature.
	 * @param skill the skill
	 * @return the p reuse rate
	 */
	public double getPReuseRate(Skill skill)
	{
		if (_creature == null)
		{
			return 1;
		}
		return calcStat(Stat.P_REUSE, _creature.getTemplate().getBaseMReuseRate(), null, skill);
	}
	
	/**
	 * Return the PAtk (base+modifier) of the Creature.
	 * @param target the target
	 * @return the p atk
	 */
	public int getPAtk(Creature target)
	{
		if (_creature == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _creature.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}
		
		return (int) calcStat(Stat.POWER_ATTACK, _creature.getTemplate().getBasePAtk() * bonusAtk, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against animals.
	 * @param target the target
	 * @return the p atk animals
	 */
	public double getPAtkAnimals(Creature target)
	{
		return calcStat(Stat.PATK_ANIMALS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against dragons.
	 * @param target the target
	 * @return the p atk dragons
	 */
	public double getPAtkDragons(Creature target)
	{
		return calcStat(Stat.PATK_DRAGONS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against angels.
	 * @param target the target
	 * @return the p atk angels
	 */
	public double getPAtkAngels(Creature target)
	{
		return calcStat(Stat.PATK_ANGELS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against insects.
	 * @param target the target
	 * @return the p atk insects
	 */
	public double getPAtkInsects(Creature target)
	{
		return calcStat(Stat.PATK_INSECTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against monsters.
	 * @param target the target
	 * @return the p atk monsters
	 */
	public double getPAtkMonsters(Creature target)
	{
		return calcStat(Stat.PATK_MONSTERS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against plants.
	 * @param target the target
	 * @return the p atk plants
	 */
	public double getPAtkPlants(Creature target)
	{
		return calcStat(Stat.PATK_PLANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Speed (base+modifier) of the Creature in function of the Armour Expertise Penalty.
	 * @return the p atk spd
	 */
	public int getPAtkSpd()
	{
		if (_creature == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _creature.isChampion())
		{
			bonusAtk = Config.CHAMPION_SPD_ATK;
		}
		
		double val = calcStat(Stat.POWER_ATTACK_SPEED, _creature.getTemplate().getBasePAtkSpd() * bonusAtk, null, null);
		val /= _creature.getArmourExpertisePenalty();
		if ((val > Config.MAX_PATK_SPEED) && (_creature instanceof PlayerInstance))
		{
			val = Config.MAX_PATK_SPEED;
		}
		
		return (int) val;
	}
	
	/**
	 * Return the PAtk Modifier against undead.
	 * @param target the target
	 * @return the p atk undead
	 */
	public double getPAtkUndead(Creature target)
	{
		return calcStat(Stat.PATK_UNDEAD, 1, target, null);
	}
	
	/**
	 * Gets the p def undead.
	 * @param target the target
	 * @return the p def undead
	 */
	public double getPDefUndead(Creature target)
	{
		return calcStat(Stat.PDEF_UNDEAD, 1, target, null);
	}
	
	/**
	 * Gets the p def plants.
	 * @param target the target
	 * @return the p def plants
	 */
	public double getPDefPlants(Creature target)
	{
		return calcStat(Stat.PDEF_PLANTS, 1, target, null);
	}
	
	/**
	 * Gets the p def insects.
	 * @param target the target
	 * @return the p def insects
	 */
	public double getPDefInsects(Creature target)
	{
		return calcStat(Stat.PDEF_INSECTS, 1, target, null);
	}
	
	/**
	 * Gets the p def animals.
	 * @param target the target
	 * @return the p def animals
	 */
	public double getPDefAnimals(Creature target)
	{
		return calcStat(Stat.PDEF_ANIMALS, 1, target, null);
	}
	
	/**
	 * Gets the p def monsters.
	 * @param target the target
	 * @return the p def monsters
	 */
	public double getPDefMonsters(Creature target)
	{
		return calcStat(Stat.PDEF_MONSTERS, 1, target, null);
	}
	
	/**
	 * Gets the p def dragons.
	 * @param target the target
	 * @return the p def dragons
	 */
	public double getPDefDragons(Creature target)
	{
		return calcStat(Stat.PDEF_DRAGONS, 1, target, null);
	}
	
	/**
	 * Gets the p def angels.
	 * @param target the target
	 * @return the p def angels
	 */
	public double getPDefAngels(Creature target)
	{
		return calcStat(Stat.PDEF_ANGELS, 1, target, null);
	}
	
	/**
	 * Return the PDef (base+modifier) of the Creature.
	 * @param target the target
	 * @return the p def
	 */
	public int getPDef(Creature target)
	{
		if (_creature == null)
		{
			return 1;
		}
		
		// Get the base PDef of the Creature
		double defence = _creature.getTemplate().getBasePDef();
		
		// Calculate modifier for Raid Bosses
		if (_creature.isRaid())
		{
			defence *= Config.RAID_P_DEFENCE_MULTIPLIER;
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stat.POWER_DEFENCE, defence, target, null);
	}
	
	/**
	 * Return the Physical Attack range (base+modifier) of the Creature.
	 * @return the physical attack range
	 */
	public int getPhysicalAttackRange()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.POWER_ATTACK_RANGE, _creature.getTemplate().getBaseAtkRange(), null, null);
	}
	
	/**
	 * Return the Skill/Spell reuse modifier.
	 * @param target the target
	 * @return the reuse modifier
	 */
	public double getReuseModifier(Creature target)
	{
		return calcStat(Stat.ATK_REUSE, 1, target, null);
	}
	
	/**
	 * Return the RunSpeed (base+modifier) of the Creature in function of the Armour Expertise Penalty.
	 * @return the run speed
	 */
	public int getRunSpeed()
	{
		if (_creature == null)
		{
			return 1;
		}
		
		// err we should be adding TO the persons run speed not making it a constant
		int val = (int) calcStat(Stat.RUN_SPEED, _creature.getTemplate().getBaseRunSpd(), null, null) + Config.RUN_SPD_BOOST;
		if (_creature.isInsideZone(ZoneId.WATER))
		{
			val /= 2;
		}
		
		if (_creature.isFlying())
		{
			val += Config.WYVERN_SPEED;
			return (int) (val * _gmSpeedMultiplier);
		}
		
		if (_creature.isRiding())
		{
			val += Config.STRIDER_SPEED;
			return (int) (val * _gmSpeedMultiplier);
		}
		
		val /= _creature.getArmourExpertisePenalty();
		if ((val > Config.MAX_RUN_SPEED) && !(_creature.isPlayer() && !_creature.getActingPlayer().isGM()))
		{
			val = Config.MAX_RUN_SPEED;
		}
		
		return (int) (val * _gmSpeedMultiplier);
	}
	
	/**
	 * Return the ShieldDef rate (base+modifier) of the Creature.
	 * @return the shld def
	 */
	public int getShldDef()
	{
		return (int) calcStat(Stat.SHIELD_DEFENCE, 0, null, null);
	}
	
	/**
	 * Gets the sp.
	 * @return the sp
	 */
	public int getSp()
	{
		return _sp;
	}
	
	/**
	 * Sets the sp.
	 * @param value the new sp
	 */
	public void setSp(int value)
	{
		_sp = value;
	}
	
	/**
	 * Return the STR of the Creature (base+modifier).
	 * @return the STR
	 */
	public int getSTR()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.STAT_STR, _creature.getTemplate().getBaseSTR(), null, null);
	}
	
	/**
	 * Return the WalkSpeed (base+modifier) of the Creature.
	 * @return the walk speed
	 */
	public int getWalkSpeed()
	{
		if (_creature == null)
		{
			return 1;
		}
		
		if (_creature instanceof PlayerInstance)
		{
			return (getRunSpeed() * 70) / 100;
		}
		return (int) calcStat(Stat.WALK_SPEED, _creature.getTemplate().getBaseWalkSpd(), null, null);
	}
	
	/**
	 * Return the WIT of the Creature (base+modifier).
	 * @return the WIT
	 */
	public int getWIT()
	{
		if (_creature == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.STAT_WIT, _creature.getTemplate().getBaseWIT(), null, null);
	}
	
	/**
	 * Return the mpConsume.
	 * @param skill the skill
	 * @return the mp consume
	 */
	public int getMpConsume(Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		
		int mpconsume = skill.getMpConsume();
		if (skill.isDance() && (_creature != null) && (_creature.getDanceCount() > 0))
		{
			mpconsume += _creature.getDanceCount() * skill.getNextDanceMpCost();
		}
		
		return (int) calcStat(Stat.MP_CONSUME, mpconsume, null, skill);
	}
	
	/**
	 * Return the mpInitialConsume.
	 * @param skill the skill
	 * @return the mp initial consume
	 */
	public int getMpInitialConsume(Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		return (int) calcStat(Stat.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
	}
	
	/**
	 * Return the PDef Modifier against giants.
	 * @param target the target
	 * @return the p def giants
	 */
	public double getPDefGiants(Creature target)
	{
		return calcStat(Stat.PDEF_GIANTS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against giants.
	 * @param target the target
	 * @return the p def magic creatures
	 */
	public double getPDefMagicCreatures(Creature target)
	{
		return calcStat(Stat.PDEF_MCREATURES, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against giants.
	 * @param target the target
	 * @return the p atk giants
	 */
	public double getPAtkGiants(Creature target)
	{
		return calcStat(Stat.PATK_GIANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against magic creatures.
	 * @param target the target
	 * @return the p atk magic creatures
	 */
	public double getPAtkMagicCreatures(Creature target)
	{
		return calcStat(Stat.PATK_MCREATURES, 1, target, null);
	}
}
