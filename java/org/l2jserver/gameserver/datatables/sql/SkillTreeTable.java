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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.xml.PlayerTemplateData;
import org.l2jserver.gameserver.model.EnchantSkillLearn;
import org.l2jserver.gameserver.model.PledgeSkillLearn;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.SkillLearn;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.skills.holders.ISkillsHolder;
import org.l2jserver.gameserver.model.skills.holders.PlayerSkillHolder;

/**
 * @version $Revision: 1.13.2.2.2.8 $ $Date: 2005/04/06 16:13:25 $
 */
public class SkillTreeTable
{
	private static final Logger LOGGER = Logger.getLogger(SkillTreeTable.class.getName());
	
	private final Map<ClassId, Map<Integer, SkillLearn>> _skillTrees = new EnumMap<>(ClassId.class);
	private final List<SkillLearn> _fishingSkillTrees = new ArrayList<>();
	private final List<SkillLearn> _expandDwarfCraftSkillTrees = new ArrayList<>();
	private final List<PledgeSkillLearn> _pledgeSkillTrees = new ArrayList<>();
	private final List<EnchantSkillLearn> _enchantSkillTrees = new ArrayList<>();
	
	private SkillTreeTable()
	{
		load();
	}
	
	public void load()
	{
		_skillTrees.clear();
		_fishingSkillTrees.clear();
		_expandDwarfCraftSkillTrees.clear();
		_pledgeSkillTrees.clear();
		_enchantSkillTrees.clear();
		
		ClassId classId = null;
		ClassId parentClassId;
		int count = 0;
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (PlayerTemplate playerTemplate : PlayerTemplateData.getInstance().getAllTemplates())
			{
				final Map<Integer, SkillLearn> map = new HashMap<>();
				classId = playerTemplate.getClassId();
				parentClassId = classId.getParent();
				
				final PreparedStatement statement = con.prepareStatement("SELECT * FROM skill_trees where class_id=? ORDER BY skill_id, level");
				statement.setInt(1, classId.getId());
				final ResultSet skilltree = statement.executeQuery();
				if (parentClassId != null)
				{
					map.putAll(_skillTrees.get(parentClassId));
					count -= map.size();
				}
				
				int prevSkillId = -1;
				while (skilltree.next())
				{
					final int id = skilltree.getInt("skill_id");
					final int lvl = skilltree.getInt("level");
					final String name = skilltree.getString("name");
					final int minLvl = skilltree.getInt("min_level");
					final int cost = skilltree.getInt("sp");
					if (prevSkillId != id)
					{
						prevSkillId = id;
					}
					
					map.put(SkillTable.getSkillHashCode(id, lvl), new SkillLearn(id, lvl, minLvl, name, cost, 0, 0));
				}
				
				_skillTrees.put(classId, map);
				count += map.size();
				skilltree.close();
				statement.close();
			}
		}
		catch (Exception e)
		{
			if (classId != null)
			{
				LOGGER.warning("Error while creating skill tree (Class ID " + classId.getId() + "):  " + e);
			}
		}
		LOGGER.info("SkillTreeTable: Loaded " + count + " skills.");
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM fishing_skill_trees ORDER BY skill_id, level");
			final ResultSet skilltree = statement.executeQuery();
			int prevSkillId = -1;
			
			while (skilltree.next())
			{
				final int id = skilltree.getInt("skill_id");
				final int lvl = skilltree.getInt("level");
				final String name = skilltree.getString("name");
				final int minLvl = skilltree.getInt("min_level");
				final int cost = skilltree.getInt("sp");
				final int costId = skilltree.getInt("costid");
				final int costCount = skilltree.getInt("cost");
				final int isDwarven = skilltree.getInt("isfordwarf");
				if (prevSkillId != id)
				{
					prevSkillId = id;
				}
				
				final SkillLearn skill = new SkillLearn(id, lvl, minLvl, name, cost, costId, costCount);
				if (isDwarven == 0)
				{
					_fishingSkillTrees.add(skill);
				}
				else
				{
					_expandDwarfCraftSkillTrees.add(skill);
				}
			}
			
			skilltree.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating fishing skill table " + e);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM enchant_skill_trees ORDER BY skill_id, level");
			final ResultSet skilltree = statement.executeQuery();
			int prevSkillId = -1;
			
			while (skilltree.next())
			{
				final int id = skilltree.getInt("skill_id");
				final int lvl = skilltree.getInt("level");
				final String name = skilltree.getString("name");
				final int baseLvl = skilltree.getInt("base_lvl");
				final int minSkillLvl = skilltree.getInt("min_skill_lvl");
				final int sp = skilltree.getInt("sp");
				final int exp = skilltree.getInt("exp");
				final byte rate76 = skilltree.getByte("success_rate76");
				final byte rate77 = skilltree.getByte("success_rate77");
				final byte rate78 = skilltree.getByte("success_rate78");
				final byte rate79 = skilltree.getByte("success_rate79");
				final byte rate80 = skilltree.getByte("success_rate80");
				if (prevSkillId != id)
				{
					prevSkillId = id;
				}
				
				_enchantSkillTrees.add(new EnchantSkillLearn(id, lvl, minSkillLvl, baseLvl, name, sp, exp, rate76, rate77, rate78, rate79, rate80));
			}
			
			skilltree.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating enchant skill table " + e);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM pledge_skill_trees ORDER BY skill_id, level");
			final ResultSet skilltree = statement.executeQuery();
			int prevSkillId = -1;
			
			while (skilltree.next())
			{
				final int id = skilltree.getInt("skill_id");
				final int lvl = skilltree.getInt("level");
				final String name = skilltree.getString("name");
				final int baseLvl = skilltree.getInt("clan_lvl");
				final int sp = skilltree.getInt("repCost");
				final int itemId = skilltree.getInt("itemId");
				if (prevSkillId != id)
				{
					prevSkillId = id;
				}
				
				_pledgeSkillTrees.add(new PledgeSkillLearn(id, lvl, baseLvl, name, sp, itemId));
			}
			
			skilltree.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating fishing skill table " + e);
		}
		
		LOGGER.info("FishingSkillTreeTable: Loaded " + _fishingSkillTrees.size() + " general skills.");
		LOGGER.info("FishingSkillTreeTable: Loaded " + _expandDwarfCraftSkillTrees.size() + " dwarven skills.");
		LOGGER.info("EnchantSkillTreeTable: Loaded " + _enchantSkillTrees.size() + " enchant skills.");
		LOGGER.info("PledgeSkillTreeTable: Loaded " + _pledgeSkillTrees.size() + " pledge skills.");
	}
	
	/**
	 * Return the minimum level needed to have this Expertise.
	 * @param grade The grade level searched
	 * @return
	 */
	public int getExpertiseLevel(int grade)
	{
		if (grade <= 0)
		{
			return 0;
		}
		
		// since expertise comes at same level for all classes we use paladin for now
		final Map<Integer, SkillLearn> learnMap = _skillTrees.get(ClassId.PALADIN);
		final int skillHashCode = SkillTable.getSkillHashCode(239, grade);
		if (learnMap.containsKey(skillHashCode))
		{
			return learnMap.get(skillHashCode).getMinLevel();
		}
		
		return 0;
	}
	
	/**
	 * Each class receives new skill on certain levels, this methods allow the retrieval of the minimum character level of given class required to learn a given skill
	 * @param skillId The iD of the skill
	 * @param classId The classId of the character
	 * @param skillLvl The SkillLvl
	 * @return The min level
	 */
	public int getMinSkillLevel(int skillId, ClassId classId, int skillLvl)
	{
		final Map<Integer, SkillLearn> map = _skillTrees.get(classId);
		final int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);
		if (map.containsKey(skillHashCode))
		{
			return map.get(skillHashCode).getMinLevel();
		}
		return 0;
	}
	
	public int getMinSkillLevel(int skillId, int skillLvl)
	{
		final int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);
		
		// Look on all classes for this skill (takes the first one found)
		for (Map<Integer, SkillLearn> map : _skillTrees.values())
		{
			// checks if the current class has this skill
			if (map.containsKey(skillHashCode))
			{
				return map.get(skillHashCode).getMinLevel();
			}
		}
		return 0;
	}
	
	public SkillLearn[] getAvailableSkills(PlayerInstance player, ClassId classId)
	{
		final List<SkillLearn> result = getAvailableSkills(player, classId, player);
		return result.toArray(new SkillLearn[result.size()]);
	}
	
	/**
	 * Gets the available skills.
	 * @param player the learning skill player.
	 * @param classId the learning skill class ID.
	 * @param holder
	 * @return all available skills for a given {@code player}, {@code classId}, {@code includeByFs} and {@code includeAutoGet}.
	 */
	private List<SkillLearn> getAvailableSkills(PlayerInstance player, ClassId classId, ISkillsHolder holder)
	{
		final List<SkillLearn> result = new ArrayList<>();
		final Collection<SkillLearn> skills = _skillTrees.get(classId).values();
		if (skills.isEmpty())
		{
			LOGGER.warning(getClass().getSimpleName() + ": Skilltree for class " + classId + " is not defined!");
			return result;
		}
		
		for (SkillLearn skill : skills)
		{
			if (skill.getMinLevel() <= player.getLevel())
			{
				final Skill oldSkill = holder.getKnownSkill(skill.getId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	public SkillLearn[] getAvailableSkills(PlayerInstance player)
	{
		final List<SkillLearn> result = new ArrayList<>();
		final List<SkillLearn> skills = new ArrayList<>();
		skills.addAll(_fishingSkillTrees);
		
		if (player.hasDwarvenCraft() && (_expandDwarfCraftSkillTrees != null))
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}
		
		final Skill[] oldSkills = player.getAllSkills();
		for (SkillLearn temp : skills)
		{
			if (temp.getMinLevel() <= player.getLevel())
			{
				boolean knownSkill = false;
				for (int j = 0; (j < oldSkills.length) && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if (oldSkills[j].getLevel() == (temp.getLevel() - 1))
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && (temp.getLevel() == 1))
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new SkillLearn[result.size()]);
	}
	
	public EnchantSkillLearn[] getAvailableEnchantSkills(PlayerInstance player)
	{
		final List<EnchantSkillLearn> result = new ArrayList<>();
		final List<EnchantSkillLearn> skills = new ArrayList<>();
		skills.addAll(_enchantSkillTrees);
		
		final Skill[] oldSkills = player.getAllSkills();
		if (player.getLevel() < 76)
		{
			return result.toArray(new EnchantSkillLearn[result.size()]);
		}
		
		for (EnchantSkillLearn skillLearn : skills)
		{
			boolean isKnownSkill = false;
			for (Skill skill : oldSkills)
			{
				if (isKnownSkill)
				{
					continue;
				}
				if (skill.getId() == skillLearn.getId())
				{
					isKnownSkill = true;
					if (skill.getLevel() == skillLearn.getMinSkillLevel())
					{
						// this is the next level of a skill that we know
						result.add(skillLearn);
					}
				}
			}
		}
		return result.toArray(new EnchantSkillLearn[result.size()]);
	}
	
	public PledgeSkillLearn[] getAvailablePledgeSkills(PlayerInstance player)
	{
		final List<PledgeSkillLearn> result = new ArrayList<>();
		final List<PledgeSkillLearn> skills = _pledgeSkillTrees;
		if (skills == null)
		{
			LOGGER.warning("No clan skills defined!");
			return new PledgeSkillLearn[0];
		}
		
		final Skill[] oldSkills = player.getClan().getAllSkills();
		for (PledgeSkillLearn temp : skills)
		{
			if (temp.getBaseLevel() <= player.getClan().getLevel())
			{
				boolean knownSkill = false;
				for (int j = 0; (j < oldSkills.length) && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if (oldSkills[j].getLevel() == (temp.getLevel() - 1))
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && (temp.getLevel() == 1))
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new PledgeSkillLearn[result.size()]);
	}
	
	/**
	 * Returns all allowed skills for a given class.
	 * @param classId
	 * @return all allowed skills for a given class.
	 */
	public Collection<SkillLearn> getAllowedSkills(ClassId classId)
	{
		return _skillTrees.get(classId).values();
	}
	
	public int getMinLevelForNewSkill(PlayerInstance player, ClassId classId)
	{
		int minLevel = 0;
		final Collection<SkillLearn> skills = _skillTrees.get(classId).values();
		for (SkillLearn temp : skills)
		{
			if ((temp.getMinLevel() > player.getLevel()) && (temp.getSpCost() != 0) && ((minLevel == 0) || (temp.getMinLevel() < minLevel)))
			{
				minLevel = temp.getMinLevel();
			}
		}
		return minLevel;
	}
	
	public int getMinLevelForNewSkill(PlayerInstance player)
	{
		int minLevel = 0;
		final List<SkillLearn> skills = new ArrayList<>();
		skills.addAll(_fishingSkillTrees);
		
		if (player.hasDwarvenCraft() && (_expandDwarfCraftSkillTrees != null))
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}
		
		for (SkillLearn s : skills)
		{
			if ((s.getMinLevel() > player.getLevel()) && ((minLevel == 0) || (s.getMinLevel() < minLevel)))
			{
				minLevel = s.getMinLevel();
			}
		}
		
		return minLevel;
	}
	
	public int getSkillCost(PlayerInstance player, Skill skill)
	{
		int skillCost = 100000000;
		final ClassId classId = player.getSkillLearningClassId();
		final int skillHashCode = SkillTable.getSkillHashCode(skill);
		if (_skillTrees.get(classId).containsKey(skillHashCode))
		{
			final SkillLearn skillLearn = _skillTrees.get(classId).get(skillHashCode);
			if (skillLearn.getMinLevel() <= player.getLevel())
			{
				skillCost = skillLearn.getSpCost();
				if (!player.getClassId().equalsOrChildOf(classId))
				{
					if (skill.getCrossLearnAdd() < 0)
					{
						return skillCost;
					}
					
					skillCost += skill.getCrossLearnAdd();
					skillCost *= skill.getCrossLearnMul();
				}
				
				if ((classId.getRace() != player.getRace()) && !player.isSubClassActive())
				{
					skillCost *= skill.getCrossLearnRace();
				}
				
				if (classId.isMage() != player.getClassId().isMage())
				{
					skillCost *= skill.getCrossLearnProf();
				}
			}
		}
		
		return skillCost;
	}
	
	public int getSkillSpCost(PlayerInstance player, Skill skill)
	{
		int skillCost = 100000000;
		final EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);
		for (EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}
			
			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}
			
			if (76 > player.getLevel())
			{
				continue;
			}
			
			skillCost = enchantSkillLearn.getSpCost();
		}
		return skillCost;
	}
	
	public int getSkillExpCost(PlayerInstance player, Skill skill)
	{
		int skillCost = 100000000;
		final EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);
		for (EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}
			
			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}
			
			if (76 > player.getLevel())
			{
				continue;
			}
			
			skillCost = enchantSkillLearn.getExp();
		}
		
		return skillCost;
	}
	
	public byte getSkillRate(PlayerInstance player, Skill skill)
	{
		final EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);
		for (EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}
			
			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}
			
			return enchantSkillLearn.getRate(player);
		}
		
		return 0;
	}
	
	/**
	 * @param player
	 * @param classId
	 * @return
	 */
	public Collection<Skill> getAllAvailableSkills(PlayerInstance player, ClassId classId)
	{
		// Get available skills
		int unLearnable = 0;
		final PlayerSkillHolder holder = new PlayerSkillHolder(player.getSkills());
		List<SkillLearn> learnable = getAvailableSkills(player, classId, holder);
		while (learnable.size() > unLearnable)
		{
			for (SkillLearn s : learnable)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if ((sk == null) || ((sk.getId() == Skill.SKILL_DIVINE_INSPIRATION) && !Config.AUTO_LEARN_DIVINE_INSPIRATION && !player.isGM()))
				{
					unLearnable++;
					continue;
				}
				
				holder.addSkill(sk);
			}
			
			// Get new available skills, some skills depend of previous skills to be available.
			learnable = getAvailableSkills(player, classId, holder);
		}
		return holder.getSkills().values();
	}
	
	public static SkillTreeTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTreeTable INSTANCE = new SkillTreeTable();
	}
}