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
package org.l2jserver.gameserver.model.holders;

import org.l2jserver.gameserver.model.StatSet;

/**
 * This class represents a Newbie Helper Buff Author: Ayor
 */
public class HelperBuffHolder
{
	/** Min level that the player must achieve to obtain this buff from Newbie Helper */
	private int _lowerLevel;
	
	/** Max level that the player mustn't exceed if it want to obtain this buff from Newbie Helper */
	private int _upperLevel;
	
	/** Identifier of the skill (buff) that the Newbie Helper must cast */
	private int _skillID;
	
	/** Level of the skill (buff) that the Newbie Helper must cast */
	private int _skillLevel;
	
	/**
	 * If True only Magus class will obtain this Buff<br>
	 * If False only Fighter class will obtain this Buff
	 */
	private boolean _isMagicClass;
	
	/**
	 * Constructor of HelperBuff.
	 * @param set
	 */
	public HelperBuffHolder(StatSet set)
	{
		_lowerLevel = set.getInt("lowerLevel");
		_upperLevel = set.getInt("upperLevel");
		_skillID = set.getInt("skillID");
		_skillLevel = set.getInt("skillLevel");
		if ("false".equals(set.getString("isMagicClass")))
		{
			_isMagicClass = false;
		}
		else
		{
			_isMagicClass = true;
		}
	}
	
	/**
	 * Returns the lower level that the PlayerInstance must achieve in order to obtain this buff
	 * @return int
	 */
	public int getLowerLevel()
	{
		return _lowerLevel;
	}
	
	/**
	 * Sets the lower level that the PlayerInstance must achieve in order to obtain this buff
	 * @param lowerLevel
	 */
	public void setLowerLevel(int lowerLevel)
	{
		_lowerLevel = lowerLevel;
	}
	
	/**
	 * Returns the upper level that the PlayerInstance mustn't exceed in order to obtain this buff
	 * @return int
	 */
	public int getUpperLevel()
	{
		return _upperLevel;
	}
	
	/**
	 * Sets the upper level that the PlayerInstance mustn't exceed in order to obtain this buff
	 * @param upperLevel
	 */
	public void setUpperLevel(int upperLevel)
	{
		_upperLevel = upperLevel;
	}
	
	/**
	 * Returns the ID of the buff that the PlayerInstance will receive
	 * @return int
	 */
	public int getSkillID()
	{
		return _skillID;
	}
	
	/**
	 * Sets the ID of the buff that the PlayerInstance will receive
	 * @param skillID
	 */
	public void setSkillID(int skillID)
	{
		_skillID = skillID;
	}
	
	/**
	 * Returns the Level of the buff that the PlayerInstance will receive
	 * @return int
	 */
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	/**
	 * Sets the Level of the buff that the PlayerInstance will receive
	 * @param skillLevel
	 */
	public void setSkillLevel(int skillLevel)
	{
		_skillLevel = skillLevel;
	}
	
	/**
	 * Returns if this Buff can be cast on a fighter or a mystic
	 * @return boolean : False if it's a fighter class Buff
	 */
	public boolean isMagicClassBuff()
	{
		return _isMagicClass;
	}
	
	/**
	 * Sets if this Buff can be cast on a fighter or a mystic
	 * @param isMagicClass
	 */
	public void setMagicClass(boolean isMagicClass)
	{
		_isMagicClass = isMagicClass;
	}
}
