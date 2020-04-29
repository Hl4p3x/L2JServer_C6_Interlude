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
package org.l2jserver.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.engines.DocumentEngine;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.items.type.WeaponType;

public class SkillTable
{
	private final Map<Integer, Skill> _skills = new HashMap<>();
	private final boolean _initialized = true;
	
	private SkillTable()
	{
		reload();
	}
	
	public void reload()
	{
		_skills.clear();
		DocumentEngine.getInstance().loadAllSkills(_skills);
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	/**
	 * Provides the skill hash
	 * @param skill The Skill to be hashed
	 * @return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	/**
	 * Centralized method for easier change of the hashing sys
	 * @param skillId The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return (skillId * 256) + skillLevel;
	}
	
	public Skill getInfo(int skillId, int level)
	{
		return _skills.get(getSkillHashCode(skillId, level));
	}
	
	public int getMaxLevel(int magicId, int level)
	{
		Skill temp;
		while (level < 100)
		{
			level++;
			temp = _skills.get(getSkillHashCode(magicId, level));
			if (temp == null)
			{
				return level - 1;
			}
		}
		return level;
	}
	
	private static final WeaponType[] weaponDbMasks =
	{
		WeaponType.ETC,
		WeaponType.BOW,
		WeaponType.POLE,
		WeaponType.DUALFIST,
		WeaponType.DUAL,
		WeaponType.BLUNT,
		WeaponType.SWORD,
		WeaponType.DAGGER,
		WeaponType.BIGSWORD,
		WeaponType.ROD,
		WeaponType.BIGBLUNT
	};
	
	public int calcWeaponsAllowed(int mask)
	{
		if (mask == 0)
		{
			return 0;
		}
		
		int weaponsAllowed = 0;
		for (int i = 0; i < weaponDbMasks.length; i++)
		{
			if ((mask & (1 << i)) != 0)
			{
				weaponsAllowed |= weaponDbMasks[i].mask();
			}
		}
		
		return weaponsAllowed;
	}
	
	public static SkillTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTable INSTANCE = new SkillTable();
	}
}
