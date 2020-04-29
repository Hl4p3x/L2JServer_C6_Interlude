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
package org.l2jserver.gameserver.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.type.ArmorType;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.model.skills.conditions.Condition;
import org.l2jserver.gameserver.model.skills.conditions.ConditionElementSeed;
import org.l2jserver.gameserver.model.skills.conditions.ConditionForceBuff;
import org.l2jserver.gameserver.model.skills.conditions.ConditionGameChance;
import org.l2jserver.gameserver.model.skills.conditions.ConditionGameTime;
import org.l2jserver.gameserver.model.skills.conditions.ConditionGameTime.CheckGameTime;
import org.l2jserver.gameserver.model.skills.conditions.ConditionLogicAnd;
import org.l2jserver.gameserver.model.skills.conditions.ConditionLogicNot;
import org.l2jserver.gameserver.model.skills.conditions.ConditionLogicOr;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerClassIdRestriction;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerHp;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerHpPercentage;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerLevel;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerMp;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerRace;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerState;
import org.l2jserver.gameserver.model.skills.conditions.ConditionPlayerState.CheckPlayerState;
import org.l2jserver.gameserver.model.skills.conditions.ConditionSkillStats;
import org.l2jserver.gameserver.model.skills.conditions.ConditionSlotItemId;
import org.l2jserver.gameserver.model.skills.conditions.ConditionTargetAggro;
import org.l2jserver.gameserver.model.skills.conditions.ConditionTargetClassIdRestriction;
import org.l2jserver.gameserver.model.skills.conditions.ConditionTargetLevel;
import org.l2jserver.gameserver.model.skills.conditions.ConditionTargetRaceId;
import org.l2jserver.gameserver.model.skills.conditions.ConditionTargetUsesWeaponKind;
import org.l2jserver.gameserver.model.skills.conditions.ConditionUsingItemType;
import org.l2jserver.gameserver.model.skills.conditions.ConditionUsingSkill;
import org.l2jserver.gameserver.model.skills.conditions.ConditionWithSkill;
import org.l2jserver.gameserver.model.skills.effects.EffectTemplate;
import org.l2jserver.gameserver.model.skills.funcs.FuncTemplate;
import org.l2jserver.gameserver.model.skills.funcs.Lambda;
import org.l2jserver.gameserver.model.skills.funcs.LambdaCalc;
import org.l2jserver.gameserver.model.skills.funcs.LambdaConst;
import org.l2jserver.gameserver.model.skills.funcs.LambdaStats;

/**
 * @author mkizub
 */
public abstract class DocumentBase
{
	static Logger LOGGER = Logger.getLogger(DocumentBase.class.getName());
	
	private final File _file;
	protected Map<String, String[]> _tables;
	
	DocumentBase(File pFile)
	{
		_file = pFile;
		_tables = new HashMap<>();
	}
	
	Document parse()
	{
		Document doc;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(_file);
		}
		catch (Exception e)
		{
			LOGGER.warning("Error loading file " + _file + " " + e);
			return null;
		}
		
		try
		{
			parseDocument(doc);
		}
		catch (Exception e)
		{
			LOGGER.warning("Error in file " + _file + " " + e);
			return null;
		}
		return doc;
	}
	
	protected abstract void parseDocument(Document doc);
	
	protected abstract StatSet getStatSet();
	
	protected abstract String getTableValue(String name);
	
	protected abstract String getTableValue(String name, int idx);
	
	protected void resetTable()
	{
		_tables = new HashMap<>();
	}
	
	protected void setTable(String name, String[] table)
	{
		_tables.put(name, table);
	}
	
	protected void parseTemplate(Node n, Object template)
	{
		Condition condition = null;
		n = n.getFirstChild();
		if (n == null)
		{
			return;
		}
		
		if ("cond".equalsIgnoreCase(n.getNodeName()))
		{
			condition = parseCondition(n.getFirstChild(), template);
			final Node msg = n.getAttributes().getNamedItem("msg");
			if ((condition != null) && (msg != null))
			{
				condition.setMessage(msg.getNodeValue());
			}
			n = n.getNextSibling();
		}
		
		for (; n != null; n = n.getNextSibling())
		{
			if ("add".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Add", condition);
			}
			else if ("sub".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Sub", condition);
			}
			else if ("mul".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Mul", condition);
			}
			else if ("basemul".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "BaseMul", condition);
			}
			else if ("div".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Div", condition);
			}
			else if ("set".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Set", condition);
			}
			else if ("enchant".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Enchant", condition);
			}
			else if ("skill".equalsIgnoreCase(n.getNodeName()))
			{
				attachSkill(n, template, condition);
			}
			else if ("effect".equalsIgnoreCase(n.getNodeName()))
			{
				if (template instanceof EffectTemplate)
				{
					throw new RuntimeException("Nested effects");
				}
				
				attachEffect(n, template, condition);
			}
		}
	}
	
	protected void attachFunc(Node n, Object template, String name, Condition attachCond)
	{
		final Stat stat = Stat.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		final String order = n.getAttributes().getNamedItem("order").getNodeValue();
		final Lambda lambda = getLambda(n, template);
		final int ord = Integer.decode(getValue(order, template));
		final Condition applayCond = parseCondition(n.getFirstChild(), template);
		final FuncTemplate ft = new FuncTemplate(attachCond, applayCond, name, stat, ord, lambda);
		if (template instanceof Item)
		{
			((Item) template).attach(ft);
		}
		else if (template instanceof Skill)
		{
			((Skill) template).attach(ft);
		}
		else if (template instanceof EffectTemplate)
		{
			((EffectTemplate) template).attach(ft);
		}
	}
	
	protected void attachLambdaFunc(Node n, Object template, LambdaCalc calc)
	{
		String name = n.getNodeName();
		final StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
		name = sb.toString();
		final Lambda lambda = getLambda(n, template);
		final FuncTemplate ft = new FuncTemplate(null, null, name, null, calc.funcs.length, lambda);
		calc.addFunc(ft.getFunc(new Env(), calc));
	}
	
	protected void attachEffect(Node n, Object template, Condition attachCond)
	{
		final NamedNodeMap attrs = n.getAttributes();
		final String name = attrs.getNamedItem("name").getNodeValue();
		int time;
		int count = 1;
		int showIcon = 0;
		if (attrs.getNamedItem("noicon") != null)
		{
			showIcon = Integer.decode(getValue(attrs.getNamedItem("noicon").getNodeValue(), template));
		}
		if (attrs.getNamedItem("count") != null)
		{
			count = Integer.decode(getValue(attrs.getNamedItem("count").getNodeValue(), template));
		}
		
		if (attrs.getNamedItem("time") != null)
		{
			time = Integer.decode(getValue(attrs.getNamedItem("time").getNodeValue(), template));
			if (Config.ENABLE_MODIFY_SKILL_DURATION && Config.SKILL_DURATION_LIST.containsKey(((Skill) template).getId()))
			{
				if (((Skill) template).getLevel() < 100)
				{
					time = Config.SKILL_DURATION_LIST.get(((Skill) template).getId());
				}
				else if ((((Skill) template).getLevel() >= 100) && (((Skill) template).getLevel() < 140))
				{
					time += Config.SKILL_DURATION_LIST.get(((Skill) template).getId());
				}
				else if (((Skill) template).getLevel() > 140)
				{
					time = Config.SKILL_DURATION_LIST.get(((Skill) template).getId());
				}
			}
		}
		else
		{
			time = ((Skill) template).getBuffDuration() / 1000 / count;
		}
		
		boolean self = false;
		if ((attrs.getNamedItem("self") != null) && (Integer.decode(getValue(attrs.getNamedItem("self").getNodeValue(), template)) == 1))
		{
			self = true;
		}
		
		final Lambda lambda = getLambda(n, template);
		final Condition applayCond = parseCondition(n.getFirstChild(), template);
		int abnormal = 0;
		if (attrs.getNamedItem("abnormal") != null)
		{
			final String abn = attrs.getNamedItem("abnormal").getNodeValue();
			if (abn.equals("poison"))
			{
				abnormal = Creature.ABNORMAL_EFFECT_POISON;
			}
			else if (abn.equals("bleeding"))
			{
				abnormal = Creature.ABNORMAL_EFFECT_BLEEDING;
			}
			else if (abn.equals("flame"))
			{
				abnormal = Creature.ABNORMAL_EFFECT_FLAME;
			}
			else if (abn.equals("bighead"))
			{
				abnormal = Creature.ABNORMAL_EFFECT_BIG_HEAD;
			}
			else if (abn.equals("stealth"))
			{
				abnormal = Creature.ABNORMAL_EFFECT_STEALTH;
			}
			else if (abn.equals("float"))
			{
				abnormal = Creature.ABNORMAL_EFFECT_FLOATING_ROOT;
			}
		}
		
		float stackOrder = 0;
		String stackType = "none";
		if (attrs.getNamedItem("stackType") != null)
		{
			stackType = attrs.getNamedItem("stackType").getNodeValue();
		}
		
		if (attrs.getNamedItem("stackOrder") != null)
		{
			stackOrder = Float.parseFloat(getValue(attrs.getNamedItem("stackOrder").getNodeValue(), template));
		}
		
		double effectPower = -1;
		if (attrs.getNamedItem("effectPower") != null)
		{
			effectPower = Double.parseDouble(getValue(attrs.getNamedItem("effectPower").getNodeValue(), template));
		}
		
		SkillType type = null;
		if (attrs.getNamedItem("effectType") != null)
		{
			final String typeName = getValue(attrs.getNamedItem("effectType").getNodeValue(), template);
			
			try
			{
				type = Enum.valueOf(SkillType.class, typeName);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Not skilltype found for: " + typeName);
			}
		}
		
		final EffectTemplate lt = new EffectTemplate(attachCond, applayCond, name, lambda, count, time, abnormal, stackType, stackOrder, showIcon, type, effectPower);
		parseTemplate(n, lt);
		if (template instanceof Item)
		{
			((Item) template).attach(lt);
		}
		else if ((template instanceof Skill) && !self)
		{
			((Skill) template).attach(lt);
		}
		else if ((template instanceof Skill) && self)
		{
			((Skill) template).attachSelf(lt);
		}
	}
	
	protected void attachSkill(Node n, Object template, Condition attachCond)
	{
		final NamedNodeMap attrs = n.getAttributes();
		int id = 0;
		int lvl = 1;
		if (attrs.getNamedItem("id") != null)
		{
			id = Integer.decode(getValue(attrs.getNamedItem("id").getNodeValue(), template));
		}
		
		if (attrs.getNamedItem("lvl") != null)
		{
			lvl = Integer.decode(getValue(attrs.getNamedItem("lvl").getNodeValue(), template));
		}
		
		final Skill skill = SkillTable.getInstance().getInfo(id, lvl);
		if (attrs.getNamedItem("chance") != null)
		{
			if ((template instanceof Weapon) || (template instanceof Item))
			{
				skill.attach(new ConditionGameChance(Integer.decode(getValue(attrs.getNamedItem("chance").getNodeValue(), template))), true);
			}
			else
			{
				skill.attach(new ConditionGameChance(Integer.decode(getValue(attrs.getNamedItem("chance").getNodeValue(), template))), false);
			}
		}
		
		if (template instanceof Weapon)
		{
			if ((attrs.getNamedItem("onUse") != null) || ((attrs.getNamedItem("onCrit") == null) && (attrs.getNamedItem("onCast") == null)))
			{
				((Weapon) template).attach(skill); // Attach as skill triggered on use
			}
			
			if (attrs.getNamedItem("onCrit") != null)
			{
				((Weapon) template).attachOnCrit(skill); // Attach as skill triggered on critical hit
			}
			
			if (attrs.getNamedItem("onCast") != null)
			{
				((Weapon) template).attachOnCast(skill); // Attach as skill triggered on cast
			}
		}
		else if (template instanceof Item)
		{
			((Item) template).attach(skill); // Attach as skill triggered on use
		}
	}
	
	protected Condition parseCondition(Node n, Object template)
	{
		while ((n != null) && (n.getNodeType() != Node.ELEMENT_NODE))
		{
			n = n.getNextSibling();
		}
		if (n == null)
		{
			return null;
		}
		
		if ("and".equalsIgnoreCase(n.getNodeName()))
		{
			return parseLogicAnd(n, template);
		}
		
		if ("or".equalsIgnoreCase(n.getNodeName()))
		{
			return parseLogicOr(n, template);
		}
		
		if ("not".equalsIgnoreCase(n.getNodeName()))
		{
			return parseLogicNot(n, template);
		}
		
		if ("player".equalsIgnoreCase(n.getNodeName()))
		{
			return parsePlayerCondition(n);
		}
		
		if ("target".equalsIgnoreCase(n.getNodeName()))
		{
			return parseTargetCondition(n, template);
		}
		
		if ("skill".equalsIgnoreCase(n.getNodeName()))
		{
			return parseSkillCondition(n);
		}
		
		if ("using".equalsIgnoreCase(n.getNodeName()))
		{
			return parseUsingCondition(n);
		}
		
		if ("game".equalsIgnoreCase(n.getNodeName()))
		{
			return parseGameCondition(n);
		}
		
		return null;
	}
	
	protected Condition parseLogicAnd(Node n, Object template)
	{
		final ConditionLogicAnd cond = new ConditionLogicAnd();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				cond.add(parseCondition(n, template));
			}
		}
		
		if ((cond.conditions == null) || (cond.conditions.length == 0))
		{
			LOGGER.warning("Empty <and> condition in " + _file);
		}
		
		return cond;
	}
	
	protected Condition parseLogicOr(Node n, Object template)
	{
		final ConditionLogicOr cond = new ConditionLogicOr();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				cond.add(parseCondition(n, template));
			}
		}
		
		if ((cond.conditions == null) || (cond.conditions.length == 0))
		{
			LOGGER.warning("Empty <or> condition in " + _file);
		}
		
		return cond;
	}
	
	protected Condition parseLogicNot(Node n, Object template)
	{
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				return new ConditionLogicNot(parseCondition(n, template));
			}
		}
		
		LOGGER.warning("Empty <not> condition in " + _file);
		return null;
	}
	
	protected Condition parsePlayerCondition(Node n)
	{
		Condition cond = null;
		final int[] elementSeeds = new int[5];
		final int[] forces = new int[2];
		final NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			final Node a = attrs.item(i);
			if ("race".equalsIgnoreCase(a.getNodeName()))
			{
				final Race race = Race.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerRace(race));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				final int lvl = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
			}
			else if ("resting".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RESTING, val));
			}
			else if ("flying".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.FLYING, val));
			}
			else if ("moving".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.MOVING, val));
			}
			else if ("running".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RUNNING, val));
			}
			else if ("behind".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.BEHIND, val));
			}
			else if ("front".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.FRONT, val));
			}
			else if ("side".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.SIDE, val));
			}
			else if ("hp".equalsIgnoreCase(a.getNodeName()))
			{
				final int hp = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHp(hp));
			}
			else if ("hprate".equalsIgnoreCase(a.getNodeName()))
			{
				final double rate = Double.parseDouble(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHpPercentage(rate));
			}
			else if ("mp".equalsIgnoreCase(a.getNodeName()))
			{
				final int hp = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerMp(hp));
			}
			else if ("seed_fire".equalsIgnoreCase(a.getNodeName()))
			{
				elementSeeds[0] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_water".equalsIgnoreCase(a.getNodeName()))
			{
				elementSeeds[1] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_wind".equalsIgnoreCase(a.getNodeName()))
			{
				elementSeeds[2] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_various".equalsIgnoreCase(a.getNodeName()))
			{
				elementSeeds[3] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("seed_any".equalsIgnoreCase(a.getNodeName()))
			{
				elementSeeds[4] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("battle_force".equalsIgnoreCase(a.getNodeName()))
			{
				forces[0] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("spell_force".equalsIgnoreCase(a.getNodeName()))
			{
				forces[1] = Integer.decode(getValue(a.getNodeValue(), null));
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
			{
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				final ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					final String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerClassIdRestriction(array));
			}
		}
		
		// Elemental seed condition processing
		for (int elementSeed : elementSeeds)
		{
			if (elementSeed > 0)
			{
				cond = joinAnd(cond, new ConditionElementSeed(elementSeeds));
				break;
			}
		}
		
		if ((forces[0] + forces[1]) > 0)
		{
			cond = joinAnd(cond, new ConditionForceBuff(forces));
		}
		
		if (cond == null)
		{
			LOGGER.warning("Unrecognized <player> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseTargetCondition(Node n, Object template)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			final Node a = attrs.item(i);
			if ("aggro".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetAggro(val));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				final int lvl = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionTargetLevel(lvl));
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
			{
				final List<Integer> array = new ArrayList<>();
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					final String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
			}
			else if ("race_id".equalsIgnoreCase(a.getNodeName()))
			{
				final List<Integer> array = new ArrayList<>();
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					final String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetRaceId(array));
			}
			else if ("pvp".equalsIgnoreCase(a.getNodeName()))
			{
				final List<Integer> array = new ArrayList<>();
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					final String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetRaceId(array));
			}
			else if ("using".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					final String item = st.nextToken().trim();
					for (WeaponType wt : WeaponType.values())
					{
						if (wt.toString().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					
					for (ArmorType at : ArmorType.values())
					{
						if (at.toString().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
				}
				cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
			}
		}
		if (cond == null)
		{
			LOGGER.warning("Unrecognized <target> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseSkillCondition(Node n)
	{
		final NamedNodeMap attrs = n.getAttributes();
		final Stat stat = Stat.valueOfXml(attrs.getNamedItem("stat").getNodeValue());
		return new ConditionSkillStats(stat);
	}
	
	protected Condition parseUsingCondition(Node n)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			final Node a = attrs.item(i);
			if ("kind".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					final String item = st.nextToken().trim();
					for (WeaponType wt : WeaponType.values())
					{
						if (wt.toString().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					
					for (ArmorType at : ArmorType.values())
					{
						if (at.toString().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
				}
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if ("skill".equalsIgnoreCase(a.getNodeName()))
			{
				final int id = Integer.parseInt(a.getNodeValue());
				cond = joinAnd(cond, new ConditionUsingSkill(id));
			}
			else if ("slotitem".equalsIgnoreCase(a.getNodeName()))
			{
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
				final int id = Integer.parseInt(st.nextToken().trim());
				final int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if (st.hasMoreTokens())
				{
					enchant = Integer.parseInt(st.nextToken().trim());
				}
				
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
			}
		}
		if (cond == null)
		{
			LOGGER.warning("Unrecognized <using> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseGameCondition(Node n)
	{
		Condition cond = null;
		final NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			final Node a = attrs.item(i);
			if ("skill".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionWithSkill(val));
			}
			
			if ("night".equalsIgnoreCase(a.getNodeName()))
			{
				final boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
			}
			
			if ("chance".equalsIgnoreCase(a.getNodeName()))
			{
				final int val = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionGameChance(val));
			}
		}
		
		if (cond == null)
		{
			LOGGER.warning("Unrecognized <game> condition in " + _file);
		}
		return cond;
	}
	
	protected void parseTable(Node n)
	{
		final NamedNodeMap attrs = n.getAttributes();
		final String name = attrs.getNamedItem("name").getNodeValue();
		if (name.charAt(0) != '#')
		{
			throw new IllegalArgumentException("Table name must start with #");
		}
		
		final StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
		final List<String> array = new ArrayList<>();
		while (data.hasMoreTokens())
		{
			array.add(data.nextToken());
		}
		final String[] res = new String[array.size()];
		int i = 0;
		for (String str : array)
		{
			res[i++] = str;
		}
		setTable(name, res);
	}
	
	protected void parseBeanSet(Node n, StatSet set, Integer level)
	{
		final String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		final String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
		final char ch = value.length() == 0 ? ' ' : value.charAt(0);
		if ((ch == '#') || (ch == '-') || Character.isDigit(ch))
		{
			set.set(name, getValue(value, level));
		}
		else
		{
			set.set(name, value);
		}
	}
	
	protected Lambda getLambda(Node n, Object template)
	{
		final Node nval = n.getAttributes().getNamedItem("val");
		if (nval != null)
		{
			final String val = nval.getNodeValue();
			if (val.charAt(0) == '#')
			{
				return new LambdaConst(Double.parseDouble(getTableValue(val)));
			}
			else if (val.charAt(0) == '$')
			{
				if (val.equalsIgnoreCase("$player_level"))
				{
					return new LambdaStats(LambdaStats.StatType.PLAYER_LEVEL);
				}
				
				if (val.equalsIgnoreCase("$target_level"))
				{
					return new LambdaStats(LambdaStats.StatType.TARGET_LEVEL);
				}
				
				if (val.equalsIgnoreCase("$player_max_hp"))
				{
					return new LambdaStats(LambdaStats.StatType.PLAYER_MAX_HP);
				}
				
				if (val.equalsIgnoreCase("$player_max_mp"))
				{
					return new LambdaStats(LambdaStats.StatType.PLAYER_MAX_MP);
				}
				
				// try to find value out of item fields
				final StatSet set = getStatSet();
				final String field = set.getString(val.substring(1));
				if (field != null)
				{
					return new LambdaConst(Double.parseDouble(getValue(field, template)));
				}
				// failed
				throw new IllegalArgumentException("Unknown value " + val);
			}
			else
			{
				return new LambdaConst(Double.parseDouble(val));
			}
		}
		final LambdaCalc calc = new LambdaCalc();
		n = n.getFirstChild();
		while ((n != null) && (n.getNodeType() != Node.ELEMENT_NODE))
		{
			n = n.getNextSibling();
		}
		
		if ((n == null) || !"val".equals(n.getNodeName()))
		{
			throw new IllegalArgumentException("Value not specified");
		}
		
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}
			attachLambdaFunc(n, template, calc);
		}
		return calc;
	}
	
	protected String getValue(String value, Object template)
	{
		// is it a table?
		if (value.charAt(0) == '#')
		{
			if (template instanceof Skill)
			{
				return getTableValue(value);
			}
			else if (template instanceof Integer)
			{
				return getTableValue(value, ((Integer) template).intValue());
			}
			else
			{
				throw new IllegalStateException();
			}
		}
		return value;
	}
	
	protected Condition joinAnd(Condition cond, Condition c)
	{
		if (cond == null)
		{
			return c;
		}
		if (cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		final ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}
}
