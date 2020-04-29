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
package org.l2jserver.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.Skill;

/**
 * @author l2jserver
 */
public class SkillSpellbookTable
{
	private static final Logger LOGGER = Logger.getLogger(SkillSpellbookTable.class.getName());
	
	private static Map<Integer, Integer> skillSpellbooks;
	
	private SkillSpellbookTable()
	{
		skillSpellbooks = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT skill_id, item_id FROM skill_spellbooks");
			final ResultSet spbooks = statement.executeQuery();
			
			while (spbooks.next())
			{
				skillSpellbooks.put(spbooks.getInt("skill_id"), spbooks.getInt("item_id"));
			}
			
			spbooks.close();
			statement.close();
			
			LOGGER.info("SkillSpellbookTable: Loaded " + skillSpellbooks.size() + " spellbooks");
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while loading spellbook data " + e);
		}
	}
	
	public int getBookForSkill(int skillId, int level)
	{
		if ((skillId == Skill.SKILL_DIVINE_INSPIRATION) && (level != -1))
		{
			switch (level)
			{
				case 1:
				{
					return 8618; // Ancient Book - Divine Inspiration (Modern Language Version)
				}
				case 2:
				{
					return 8619; // Ancient Book - Divine Inspiration (Original Language Version)
				}
				case 3:
				{
					return 8620; // Ancient Book - Divine Inspiration (Manuscript)
				}
				case 4:
				{
					return 8621; // Ancient Book - Divine Inspiration (Original Version)
				}
				default:
				{
					return -1;
				}
			}
		}
		
		if (!skillSpellbooks.containsKey(skillId))
		{
			return -1;
		}
		
		return skillSpellbooks.get(skillId);
	}
	
	public int getBookForSkill(Skill skill)
	{
		return getBookForSkill(skill.getId(), -1);
	}
	
	public int getBookForSkill(Skill skill, int level)
	{
		return getBookForSkill(skill.getId(), level);
	}
	
	public static SkillSpellbookTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillSpellbookTable INSTANCE = new SkillSpellbookTable();
	}
}
