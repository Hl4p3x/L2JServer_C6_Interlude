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

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

public class EnchantSkillLearn
{
	// these two build the primary key
	private final int id;
	private final int level;
	// not needed, just for easier debug
	private final String name;
	private final int spCost;
	private final int baseLvl;
	private final int minSkillLevel;
	private final int exp;
	private final byte rate76;
	private final byte rate77;
	private final byte rate78;
	private final byte rate79;
	private final byte rate80;
	
	public EnchantSkillLearn(int id, int level, int minSkillLevel, int baseLvl, String name, int spCost, int exp, byte rate76, byte rate77, byte rate78, byte rate79, byte rate80)
	{
		this.id = id;
		this.level = level;
		this.baseLvl = baseLvl;
		this.minSkillLevel = minSkillLevel;
		this.name = name.intern();
		this.spCost = spCost;
		this.exp = exp;
		this.rate76 = rate76;
		this.rate77 = rate77;
		this.rate78 = rate78;
		this.rate79 = rate79;
		this.rate80 = rate80;
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return level;
	}
	
	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return baseLvl;
	}
	
	/**
	 * @return Returns the minSkillLevel.
	 */
	public int getMinSkillLevel()
	{
		return minSkillLevel;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return Returns the spCost.
	 */
	public int getSpCost()
	{
		return spCost;
	}
	
	public int getExp()
	{
		return exp;
	}
	
	public byte getRate(PlayerInstance ply)
	{
		byte result;
		switch (ply.getLevel())
		{
			case 76:
			{
				result = rate76;
				break;
			}
			case 77:
			{
				result = rate77;
				break;
			}
			case 78:
			{
				result = rate78;
				break;
			}
			case 79:
			{
				result = rate79;
				break;
			}
			case 80:
			{
				result = rate80;
				break;
			}
			default:
			{
				result = rate80;
				break;
			}
		}
		return result;
	}
}
