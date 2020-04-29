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
package org.l2jserver.gameserver.model.actor.templates;

import org.l2jserver.gameserver.model.StatSet;

public class CreatureTemplate
{
	// BaseStats
	private final int _baseSTR;
	private final int _baseCON;
	private final int _baseDEX;
	private final int _baseINT;
	private final int _baseWIT;
	private final int _baseMEN;
	private final float _baseHpMax;
	private final float _baseCpMax;
	private final float _baseMpMax;
	
	/** HP Regen base */
	private final float _baseHpReg;
	
	/** MP Regen base */
	private final float _baseMpReg;
	
	private final int _basePAtk;
	private final int _baseMAtk;
	private final int _basePDef;
	private final int _baseMDef;
	private final int _basePAtkSpd;
	private final int _baseMAtkSpd;
	private final float _baseMReuseRate;
	private final int _baseShldDef;
	private final int _baseAtkRange;
	private final int _baseShldRate;
	private final int _baseCritRate;
	private final int _baseMCritRate;
	private final int _baseWalkSpd;
	private final int _baseRunSpd;
	
	// SpecialStats
	private final int _baseBreath;
	private final int _baseAggression;
	private final int _baseBleed;
	private final int _basePoison;
	private final int _baseStun;
	private final int _baseRoot;
	private final int _baseMovement;
	private final int _baseConfusion;
	private final int _baseSleep;
	private final int _baseFire;
	private final int _baseWind;
	private final int _baseWater;
	private final int _baseEarth;
	private final int _baseHoly;
	private final int _baseDark;
	private final double _baseAggressionVuln;
	private final double _baseBleedVuln;
	private final double _basePoisonVuln;
	private final double _baseStunVuln;
	private final double _baseRootVuln;
	private final double _baseMovementVuln;
	private final double _baseConfusionVuln;
	private final double _baseSleepVuln;
	private final double _baseFireVuln;
	private final double _baseWindVuln;
	private final double _baseWaterVuln;
	private final double _baseEarthVuln;
	private final double _baseHolyVuln;
	private final double _baseDarkVuln;
	private final double _baseCritVuln;
	
	private final boolean _isUndead;
	
	// C4 Stats
	private final int _baseMpConsumeRate;
	private final int _baseHpConsumeRate;
	
	private final int _collisionRadius;
	private final int _collisionHeight;
	
	public CreatureTemplate(StatSet set)
	{
		// Base stats
		_baseSTR = set.getInt("baseSTR");
		_baseCON = set.getInt("baseCON");
		_baseDEX = set.getInt("baseDEX");
		_baseINT = set.getInt("baseINT");
		_baseWIT = set.getInt("baseWIT");
		_baseMEN = set.getInt("baseMEN");
		_baseHpMax = set.getFloat("baseHpMax");
		_baseCpMax = set.getFloat("baseCpMax");
		_baseMpMax = set.getFloat("baseMpMax");
		_baseHpReg = set.getFloat("baseHpReg");
		_baseMpReg = set.getFloat("baseMpReg");
		_basePAtk = set.getInt("basePAtk");
		_baseMAtk = set.getInt("baseMAtk");
		_basePDef = set.getInt("basePDef");
		_baseMDef = set.getInt("baseMDef");
		_basePAtkSpd = set.getInt("basePAtkSpd");
		_baseMAtkSpd = set.getInt("baseMAtkSpd");
		_baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		_baseShldDef = set.getInt("baseShldDef");
		_baseAtkRange = set.getInt("baseAtkRange");
		_baseShldRate = set.getInt("baseShldRate");
		_baseCritRate = set.getInt("baseCritRate");
		_baseMCritRate = set.getInt("baseMCritRate", 5);
		_baseWalkSpd = set.getInt("baseWalkSpd");
		_baseRunSpd = set.getInt("baseRunSpd");
		
		// SpecialStats
		_baseBreath = set.getInt("baseBreath", 100);
		_baseAggression = set.getInt("baseAggression", 0);
		_baseBleed = set.getInt("baseBleed", 0);
		_basePoison = set.getInt("basePoison", 0);
		_baseStun = set.getInt("baseStun", 0);
		_baseRoot = set.getInt("baseRoot", 0);
		_baseMovement = set.getInt("baseMovement", 0);
		_baseConfusion = set.getInt("baseConfusion", 0);
		_baseSleep = set.getInt("baseSleep", 0);
		_baseFire = set.getInt("baseFire", 0);
		_baseWind = set.getInt("baseWind", 0);
		_baseWater = set.getInt("baseWater", 0);
		_baseEarth = set.getInt("baseEarth", 0);
		_baseHoly = set.getInt("baseHoly", 0);
		_baseDark = set.getInt("baseDark", 0);
		_baseAggressionVuln = set.getInt("baseAaggressionVuln", 1);
		_baseBleedVuln = set.getInt("baseBleedVuln", 1);
		_basePoisonVuln = set.getInt("basePoisonVuln", 1);
		_baseStunVuln = set.getInt("baseStunVuln", 1);
		_baseRootVuln = set.getInt("baseRootVuln", 1);
		_baseMovementVuln = set.getInt("baseMovementVuln", 1);
		_baseConfusionVuln = set.getInt("baseConfusionVuln", 1);
		_baseSleepVuln = set.getInt("baseSleepVuln", 1);
		_baseFireVuln = set.getInt("baseFireVuln", 1);
		_baseWindVuln = set.getInt("baseWindVuln", 1);
		_baseWaterVuln = set.getInt("baseWaterVuln", 1);
		_baseEarthVuln = set.getInt("baseEarthVuln", 1);
		_baseHolyVuln = set.getInt("baseHolyVuln", 1);
		_baseDarkVuln = set.getInt("baseDarkVuln", 1);
		_baseCritVuln = set.getInt("baseCritVuln", 1);
		_isUndead = set.getInt("isUndead", 0) == 1;
		
		// C4 Stats
		_baseMpConsumeRate = set.getInt("baseMpConsumeRate", 0);
		_baseHpConsumeRate = set.getInt("baseHpConsumeRate", 0);
		
		// Geometry
		_collisionRadius = (int) set.getFloat("collision_radius"); // TODO: Support float.
		_collisionHeight = (int) set.getFloat("collision_height"); // TODO: Support float.
	}
	
	public int getBaseSTR()
	{
		return _baseSTR;
	}
	
	public int getBaseCON()
	{
		return _baseCON;
	}
	
	public int getBaseDEX()
	{
		return _baseDEX;
	}
	
	public int getBaseINT()
	{
		return _baseINT;
	}
	
	public int getBaseWIT()
	{
		return _baseWIT;
	}
	
	public int getBaseMEN()
	{
		return _baseMEN;
	}
	
	public float getBaseHpMax()
	{
		return _baseHpMax;
	}
	
	public float getBaseCpMax()
	{
		return _baseCpMax;
	}
	
	public float getBaseMpMax()
	{
		return _baseMpMax;
	}
	
	public float getBaseHpReg()
	{
		return _baseHpReg;
	}
	
	public float getBaseMpReg()
	{
		return _baseMpReg;
	}
	
	public int getBasePAtk()
	{
		return _basePAtk;
	}
	
	public int getBaseMAtk()
	{
		return _baseMAtk;
	}
	
	public int getBasePDef()
	{
		return _basePDef;
	}
	
	public int getBaseMDef()
	{
		return _baseMDef;
	}
	
	public int getBasePAtkSpd()
	{
		return _basePAtkSpd;
	}
	
	public int getBaseMAtkSpd()
	{
		return _baseMAtkSpd;
	}
	
	public float getBaseMReuseRate()
	{
		return _baseMReuseRate;
	}
	
	public int getBaseShldDef()
	{
		return _baseShldDef;
	}
	
	public int getBaseAtkRange()
	{
		return _baseAtkRange;
	}
	
	public int getBaseShldRate()
	{
		return _baseShldRate;
	}
	
	public int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public int getBaseMCritRate()
	{
		return _baseMCritRate;
	}
	
	public int getBaseWalkSpd()
	{
		return _baseWalkSpd;
	}
	
	public int getBaseRunSpd()
	{
		return _baseRunSpd;
	}
	
	public int getBaseBreath()
	{
		return _baseBreath;
	}
	
	public int getBaseAggression()
	{
		return _baseAggression;
	}
	
	public int getBaseBleed()
	{
		return _baseBleed;
	}
	
	public int getBasePoison()
	{
		return _basePoison;
	}
	
	public int getBaseStun()
	{
		return _baseStun;
	}
	
	public int getBaseRoot()
	{
		return _baseRoot;
	}
	
	public int getBaseMovement()
	{
		return _baseMovement;
	}
	
	public int getBaseConfusion()
	{
		return _baseConfusion;
	}
	
	public int getBaseSleep()
	{
		return _baseSleep;
	}
	
	public int getBaseFire()
	{
		return _baseFire;
	}
	
	public int getBaseWind()
	{
		return _baseWind;
	}
	
	public int getBaseWater()
	{
		return _baseWater;
	}
	
	public int getBaseEarth()
	{
		return _baseEarth;
	}
	
	public int getBaseHoly()
	{
		return _baseHoly;
	}
	
	public int getBaseDark()
	{
		return _baseDark;
	}
	
	public double getBaseAggressionVuln()
	{
		return _baseAggressionVuln;
	}
	
	public double getBaseBleedVuln()
	{
		return _baseBleedVuln;
	}
	
	public double getBasePoisonVuln()
	{
		return _basePoisonVuln;
	}
	
	public double getBaseStunVuln()
	{
		return _baseStunVuln;
	}
	
	public double getBaseRootVuln()
	{
		return _baseRootVuln;
	}
	
	public double getBaseMovementVuln()
	{
		return _baseMovementVuln;
	}
	
	public double getBaseConfusionVuln()
	{
		return _baseConfusionVuln;
	}
	
	public double getBaseSleepVuln()
	{
		return _baseSleepVuln;
	}
	
	public double getBaseFireVuln()
	{
		return _baseFireVuln;
	}
	
	public double getBaseWindVuln()
	{
		return _baseWindVuln;
	}
	
	public double getBaseWaterVuln()
	{
		return _baseWaterVuln;
	}
	
	public double getBaseEarthVuln()
	{
		return _baseEarthVuln;
	}
	
	public double getBaseHolyVuln()
	{
		return _baseHolyVuln;
	}
	
	public double getBaseDarkVuln()
	{
		return _baseDarkVuln;
	}
	
	public double getBaseCritVuln()
	{
		return _baseCritVuln;
	}
	
	public boolean isUndead()
	{
		return _isUndead;
	}
	
	public int getBaseMpConsumeRate()
	{
		return _baseMpConsumeRate;
	}
	
	public int getBaseHpConsumeRate()
	{
		return _baseHpConsumeRate;
	}
	
	public int getCollisionRadius()
	{
		return _collisionRadius;
	}
	
	public int getCollisionHeight()
	{
		return _collisionHeight;
	}
}
