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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.gameserver.model.DropCategory;
import org.l2jserver.gameserver.model.DropData;
import org.l2jserver.gameserver.model.MinionData;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.skills.Stat;

/**
 * This cl contains all generic data of a Spawn object.<br>
 * <br>
 * <b><u>Data</u>:</b><br>
 * <li>npcId, type, name, sex</li>
 * <li>rewardExp, rewardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand, armor</li>
 * <li>isUndead</li>
 * <li>_drops</li>
 * <li>_minions</li>
 * <li>_teachInfo</li>
 * <li>_skills</li>
 * <li>_questsStart</li><br>
 * <br>
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public class NpcTemplate extends CreatureTemplate
{
	protected static final Logger LOGGER = Logger.getLogger(NpcTemplate.class.getName());
	
	private final int _npcId;
	private final int _idTemplate;
	private final String _type;
	private final String _name;
	private final boolean _serverSideName;
	private final String _title;
	private final boolean _serverSideTitle;
	private final String _sex;
	private final byte _level;
	private final int _rewardExp;
	private final int _rewardSp;
	private final int _aggroRange;
	private final int _rhand;
	private final int _lhand;
	private final int _armor;
	private final int _absorbLevel;
	private final AbsorbCrystalType _absorbType;
	private String _factionId;
	private final int _factionRange;
	private Race _race;
	
	private final boolean _custom;
	
	public enum AbsorbCrystalType
	{
		LAST_HIT,
		FULL_PARTY,
		PARTY_ONE_RANDOM
	}
	
	public enum Race
	{
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE,
		UNKNOWN
	}
	
	private final StatSet _npcStatSet;
	
	/** The table containing all Item that can be dropped by NpcInstance using this NpcTemplate */
	private final List<DropCategory> _categories = new ArrayList<>();
	
	/** The table containing all Minions that must be spawn with the NpcInstance using this NpcTemplate */
	private final List<MinionData> _minions = new ArrayList<>();
	
	private final List<ClassId> _teachInfo = new ArrayList<>();
	private final Map<Integer, Skill> _skills = new HashMap<>();
	private final Map<Stat, Double> _vulnerabilities = new EnumMap<>(Stat.class);
	// contains a list of quests for each event type (questStart, questAttack, questKill, etc)
	private final Map<EventType, List<Quest>> _questEvents = new EnumMap<>(EventType.class);
	
	/**
	 * Constructor of Creature.
	 * @param set The StatSet object to transfer data to the method
	 * @param custom
	 */
	public NpcTemplate(StatSet set, boolean custom)
	{
		super(set);
		_npcId = set.getInt("npcId");
		_idTemplate = set.getInt("idTemplate");
		_type = set.getString("type");
		_name = set.getString("name");
		_serverSideName = set.getBoolean("serverSideName");
		_title = set.getString("title");
		_serverSideTitle = set.getBoolean("serverSideTitle");
		_sex = set.getString("sex");
		_level = set.getByte("level");
		_rewardExp = set.getInt("rewardExp");
		_rewardSp = set.getInt("rewardSp");
		_aggroRange = set.getInt("aggroRange");
		_rhand = set.getInt("rhand");
		_lhand = set.getInt("lhand");
		_armor = set.getInt("armor");
		_absorbLevel = set.getInt("absorb_level", 0);
		_absorbType = AbsorbCrystalType.valueOf(set.getString("absorb_type"));
		final String f = set.getString("factionId", null);
		if (f == null)
		{
			setFactionId(null);
		}
		else
		{
			setFactionId(f.intern());
		}
		_factionRange = set.getInt("factionRange", 0);
		_npcStatSet = set;
		_custom = custom;
	}
	
	public void addTeachInfo(ClassId classId)
	{
		_teachInfo.add(classId);
	}
	
	public List<ClassId> getTeachInfo()
	{
		return _teachInfo;
	}
	
	public boolean canTeach(ClassId classId)
	{
		// If the player is on a third class, fetch the class teacher information for its parent class.
		if (classId.getId() >= 88)
		{
			return _teachInfo.contains(classId.getParent());
		}
		return _teachInfo.contains(classId);
	}
	
	// add a drop to a given category. If the category does not exist, create it.
	public void addDropData(DropData drop, int categoryType)
	{
		if (!drop.isQuestDrop())
		{
			boolean catExists = false;
			for (DropCategory cat : _categories)
			{
				// if the category exists, add the drop to this category.
				if (cat.getCategoryType() == categoryType)
				{
					cat.addDropData(drop, _type.equalsIgnoreCase("RaidBoss") || _type.equalsIgnoreCase("GrandBoss"));
					catExists = true;
					break;
				}
			}
			// if the category doesn't exit, create it and add the drop
			if (!catExists)
			{
				final DropCategory cat = new DropCategory(categoryType);
				cat.addDropData(drop, _type.equalsIgnoreCase("RaidBoss") || _type.equalsIgnoreCase("GrandBoss"));
				_categories.add(cat);
			}
		}
	}
	
	public void addRaidData(MinionData minion)
	{
		_minions.add(minion);
	}
	
	public void addSkill(Skill skill)
	{
		_skills.put(skill.getId(), skill);
	}
	
	public void addVulnerability(Stat id, double vuln)
	{
		_vulnerabilities.put(id, vuln);
	}
	
	public String getType()
	{
		return _type;
	}
	
	public double getVulnerability(Stat id)
	{
		if (_vulnerabilities.get(id) == null)
		{
			return 1;
		}
		return _vulnerabilities.get(id);
	}
	
	public double removeVulnerability(Stat id)
	{
		return _vulnerabilities.remove(id);
	}
	
	/**
	 * Return the list of all possible UNCATEGORIZED drops of this NpcTemplate.
	 * @return
	 */
	public List<DropCategory> getDropData()
	{
		return _categories;
	}
	
	/**
	 * Return the list of all possible item drops of this NpcTemplate.<br>
	 * (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)<br>
	 * @return
	 */
	public List<DropData> getAllDropData()
	{
		final List<DropData> lst = new ArrayList<>();
		for (DropCategory tmp : _categories)
		{
			lst.addAll(tmp.getAllDrops());
		}
		return lst;
	}
	
	/**
	 * Empty all possible drops of this NpcTemplate.
	 */
	public synchronized void clearAllDropData()
	{
		while (!_categories.isEmpty())
		{
			_categories.get(0).clearAllDrops();
			_categories.remove(0);
		}
		_categories.clear();
	}
	
	/**
	 * Return the list of all Minions that must be spawn with the NpcInstance using this NpcTemplate.
	 * @return
	 */
	public List<MinionData> getMinionData()
	{
		return _minions;
	}
	
	public Map<Integer, Skill> getSkills()
	{
		return _skills;
	}
	
	public void addQuestEvent(EventType eventType, Quest q)
	{
		if (_questEvents.get(eventType) == null)
		{
			final List<Quest> quests = new ArrayList<>();
			quests.add(q);
			_questEvents.put(eventType, quests);
		}
		else
		{
			final List<Quest> quests = _questEvents.get(eventType);
			
			// If only one registration per npc is allowed for this event type then only register this NPC if not already registered for the specified event.
			// If a quest allows multiple registrations, then register regardless of count.
			// In all cases, check if this new registration is replacing an older copy of the SAME quest.
			if (!eventType.isMultipleRegistrationAllowed())
			{
				if (quests.get(0).getName().equals(q.getName()))
				{
					quests.set(0, q);
				}
				else
				{
					LOGGER.warning("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + eventType + "\" for NPC \"" + _name + "\" and quest \"" + q.getName() + "\".");
				}
			}
			else
			{
				// Loop through the existing quests and copy them to the new list. While doing so, also check if this new quest happens to be just a replacement for a previously loaded quest.
				// If so, just save the updated reference and do NOT use the new list. Else, add the new quest to the end of the new list.
				for (Quest quest : quests)
				{
					if (quest.getName().equals(q.getName()))
					{
						quest = q;
						return;
					}
				}
				quests.add(q);
				// _questEvents.put(eventType, quests);
			}
		}
	}
	
	public List<Quest> getEventQuests(EventType eventType)
	{
		if (_questEvents.get(eventType) == null)
		{
			return Collections.emptyList();
		}
		return _questEvents.get(eventType);
	}
	
	public StatSet getStatSet()
	{
		return _npcStatSet;
	}
	
	public void setRace(int raceId)
	{
		switch (raceId)
		{
			case 1:
			{
				_race = Race.UNDEAD;
				break;
			}
			case 2:
			{
				_race = Race.MAGICCREATURE;
				break;
			}
			case 3:
			{
				_race = Race.BEAST;
				break;
			}
			case 4:
			{
				_race = Race.ANIMAL;
				break;
			}
			case 5:
			{
				_race = Race.PLANT;
				break;
			}
			case 6:
			{
				_race = Race.HUMANOID;
				break;
			}
			case 7:
			{
				_race = Race.SPIRIT;
				break;
			}
			case 8:
			{
				_race = Race.ANGEL;
				break;
			}
			case 9:
			{
				_race = Race.DEMON;
				break;
			}
			case 10:
			{
				_race = Race.DRAGON;
				break;
			}
			case 11:
			{
				_race = Race.GIANT;
				break;
			}
			case 12:
			{
				_race = Race.BUG;
				break;
			}
			case 13:
			{
				_race = Race.FAIRIE;
				break;
			}
			case 14:
			{
				_race = Race.HUMAN;
				break;
			}
			case 15:
			{
				_race = Race.ELVE;
				break;
			}
			case 16:
			{
				_race = Race.DARKELVE;
				break;
			}
			case 17:
			{
				_race = Race.ORC;
				break;
			}
			case 18:
			{
				_race = Race.DWARVE;
				break;
			}
			case 19:
			{
				_race = Race.OTHER;
				break;
			}
			case 20:
			{
				_race = Race.NONLIVING;
				break;
			}
			case 21:
			{
				_race = Race.SIEGEWEAPON;
				break;
			}
			case 22:
			{
				_race = Race.DEFENDINGARMY;
				break;
			}
			case 23:
			{
				_race = Race.MERCENARIE;
				break;
			}
			default:
			{
				_race = Race.UNKNOWN;
				break;
			}
		}
	}
	
	public Race getRace()
	{
		if (_race == null)
		{
			_race = Race.UNKNOWN;
		}
		return _race;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getIdTemplate()
	{
		return _idTemplate;
	}
	
	public boolean isServerSideName()
	{
		return _serverSideName;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isServerSideTitle()
	{
		return _serverSideTitle;
	}
	
	public String getSex()
	{
		return _sex;
	}
	
	public int getRewardExp()
	{
		return _rewardExp;
	}
	
	public int getRewardSp()
	{
		return _rewardSp;
	}
	
	public int getAggroRange()
	{
		return _aggroRange;
	}
	
	public int getRhand()
	{
		return _rhand;
	}
	
	public int getLhand()
	{
		return _lhand;
	}
	
	public int getArmor()
	{
		return _armor;
	}
	
	public int getAbsorbLevel()
	{
		return _absorbLevel;
	}
	
	public AbsorbCrystalType getAbsorbType()
	{
		return _absorbType;
	}
	
	public String getFactionId()
	{
		return _factionId;
	}
	
	public void setFactionId(String id)
	{
		_factionId = id;
	}
	
	public int getFactionRange()
	{
		return _factionRange;
	}
	
	public boolean isCustom()
	{
		return _custom;
	}
}
