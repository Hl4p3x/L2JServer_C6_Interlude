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

import org.l2jserver.gameserver.model.Skill;

/**
 * @author BiTi
 */
public class HeroSkillTable
{
	private static final Integer[] HERO_SKILL_IDS = new Integer[]
	{
		395,
		396,
		1374,
		1375,
		1376
	};
	private static Skill[] HERO_SKILLS;
	
	private HeroSkillTable()
	{
		HERO_SKILLS = new Skill[5];
		HERO_SKILLS[0] = SkillTable.getInstance().getInfo(395, 1);
		HERO_SKILLS[1] = SkillTable.getInstance().getInfo(396, 1);
		HERO_SKILLS[2] = SkillTable.getInstance().getInfo(1374, 1);
		HERO_SKILLS[3] = SkillTable.getInstance().getInfo(1375, 1);
		HERO_SKILLS[4] = SkillTable.getInstance().getInfo(1376, 1);
	}
	
	public static Skill[] getHeroSkills()
	{
		return HERO_SKILLS;
	}
	
	public static boolean isHeroSkill(int skillid)
	{
		for (int id : HERO_SKILL_IDS)
		{
			if (id == skillid)
			{
				return true;
			}
		}
		return false;
	}
	
	public static HeroSkillTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HeroSkillTable INSTANCE = new HeroSkillTable();
	}
}
