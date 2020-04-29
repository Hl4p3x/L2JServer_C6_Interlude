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
package org.l2jserver.gameserver.model.items;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.handler.SkillHandler;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.conditions.ConditionGameChance;
import org.l2jserver.gameserver.model.skills.funcs.Func;
import org.l2jserver.gameserver.model.skills.funcs.FuncTemplate;

/**
 * This class is dedicated to the management of weapons.
 * @version $Revision: 1.4.2.3.2.5 $ $Date: 2005/04/02 15:57:51 $
 */
public class Weapon extends Item
{
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _pDam;
	private final int _rndDam;
	private final int _critical;
	private final double _hitModifier;
	private final int _avoidModifier;
	private final int _shieldDef;
	private final double _shieldDefRate;
	private final int _atkSpeed;
	private final int _atkReuse;
	private final int _mpConsume;
	private final int _mDam;
	private Skill _itemSkill = null; // for passive skill
	private Skill _enchant4Skill = null; // skill that activates when item is enchanted +4 (for duals)
	
	// Attached skills for Special Abilities
	protected Skill[] _skillsOnCast;
	protected Skill[] _skillsOnCrit;
	
	/**
	 * Constructor for Weapon.<br>
	 * <u><i>Variables filled :</i></u><br>
	 * <li>_soulShotCount & _spiritShotCount</li>
	 * <li>_pDam & _mDam & _rndDam</li>
	 * <li>_critical</li>
	 * <li>_hitModifier</li>
	 * <li>_avoidModifier</li>
	 * <li>_shieldDes & _shieldDefRate</li>
	 * <li>_atkSpeed & _AtkReuse</li>
	 * <li>_mpConsume</li><br>
	 * @param type : ArmorType designating the type of armor
	 * @param set : StatSet designating the set of couples (key,value) caracterizing the armor
	 * @see Item constructor
	 */
	public Weapon(WeaponType type, StatSet set)
	{
		super(type, set);
		_soulShotCount = set.getInt("soulshots", 0);
		_spiritShotCount = set.getInt("spiritshots", 0);
		_pDam = set.getInt("p_dam", 0);
		_rndDam = set.getInt("rnd_dam", 0);
		_critical = set.getInt("critical", 0);
		_hitModifier = set.getDouble("hit_modify", 0);
		_avoidModifier = set.getInt("avoid_modify", 0);
		_shieldDef = set.getInt("shield_def", 0);
		_shieldDefRate = set.getDouble("shield_def_rate", 0);
		_atkSpeed = set.getInt("atk_speed", 0);
		_atkReuse = set.getInt("atk_reuse", type == WeaponType.BOW ? 1500 : 0);
		_mpConsume = set.getInt("mp_consume", 0);
		_mDam = set.getInt("m_dam", 0);
		
		int sId = set.getInt("item_skill_id", 0);
		int sLv = set.getInt("item_skill_lvl", 0);
		if ((sId > 0) && (sLv > 0))
		{
			_itemSkill = SkillTable.getInstance().getInfo(sId, sLv);
		}
		
		sId = set.getInt("enchant4_skill_id", 0);
		sLv = set.getInt("enchant4_skill_lvl", 0);
		if ((sId > 0) && (sLv > 0))
		{
			_enchant4Skill = SkillTable.getInstance().getInfo(sId, sLv);
		}
		
		sId = set.getInt("onCast_skill_id", 0);
		sLv = set.getInt("onCast_skill_lvl", 0);
		int sCh = set.getInt("onCast_skill_chance", 0);
		if ((sId > 0) && (sLv > 0) && (sCh > 0))
		{
			final Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
			skill.attach(new ConditionGameChance(sCh), true);
			attachOnCast(skill);
		}
		
		sId = set.getInt("onCrit_skill_id", 0);
		sLv = set.getInt("onCrit_skill_lvl", 0);
		sCh = set.getInt("onCrit_skill_chance", 0);
		if ((sId > 0) && (sLv > 0) && (sCh > 0))
		{
			final Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
			skill.attach(new ConditionGameChance(sCh), true);
			attachOnCrit(skill);
		}
	}
	
	/**
	 * Returns the type of Weapon
	 * @return WeaponType
	 */
	@Override
	public WeaponType getItemType()
	{
		return (WeaponType) super._type;
	}
	
	/**
	 * Returns the ID of the Etc item after applying the mask.
	 * @return int : ID of the Weapon
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * Returns the quantity of SoulShot used.
	 * @return int
	 */
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	
	/**
	 * Returns the quatity of SpiritShot used.
	 * @return int
	 */
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	
	/**
	 * Returns the physical damage.
	 * @return int
	 */
	public int getPDamage()
	{
		return _pDam;
	}
	
	/**
	 * Returns the random damage inflicted by the weapon
	 * @return int
	 */
	public int getRandomDamage()
	{
		return _rndDam;
	}
	
	/**
	 * Returns the attack speed of the weapon
	 * @return int
	 */
	public int getAttackSpeed()
	{
		return _atkSpeed;
	}
	
	/**
	 * Return the Attack Reuse Delay of the Weapon.
	 * @return int
	 */
	public int getAttackReuseDelay()
	{
		return _atkReuse;
	}
	
	/**
	 * Returns the avoid modifier of the weapon
	 * @return int
	 */
	public int getAvoidModifier()
	{
		return _avoidModifier;
	}
	
	/**
	 * Returns the rate of critical hit
	 * @return int
	 */
	public int getCritical()
	{
		return _critical;
	}
	
	/**
	 * Returns the hit modifier of the weapon
	 * @return double
	 */
	public double getHitModifier()
	{
		return _hitModifier;
	}
	
	/**
	 * Returns the magical damage inflicted by the weapon
	 * @return int
	 */
	public int getMDamage()
	{
		return _mDam;
	}
	
	/**
	 * Returns the MP consumption with the weapon
	 * @return int
	 */
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	/**
	 * Returns the shield defense of the weapon
	 * @return int
	 */
	public int getShieldDef()
	{
		return _shieldDef;
	}
	
	/**
	 * Returns the rate of shield defense of the weapon
	 * @return double
	 */
	public double getShieldDefRate()
	{
		return _shieldDefRate;
	}
	
	/**
	 * Returns passive skill linked to that weapon
	 * @return
	 */
	public Skill getSkill()
	{
		return _itemSkill;
	}
	
	/**
	 * Returns skill that player get when has equipped weapon +4 or more (for duals SA)
	 * @return
	 */
	public Skill getEnchant4Skill()
	{
		return _enchant4Skill;
	}
	
	/**
	 * Returns array of Func objects containing the list of functions used by the weapon
	 * @param instance : ItemInstance pointing out the weapon
	 * @param creature : Creature pointing out the player
	 * @return Func[] : array of functions
	 */
	@Override
	public Func[] getStatFuncs(ItemInstance instance, Creature creature)
	{
		final List<Func> funcs = new ArrayList<>();
		if (_funcTemplates != null)
		{
			for (FuncTemplate t : _funcTemplates)
			{
				final Env env = new Env();
				env.player = creature;
				env.item = instance;
				final Func f = t.getFunc(env, instance);
				if (f != null)
				{
					funcs.add(f);
				}
			}
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	/**
	 * Returns effects of skills associated with the item to be triggered onHit.
	 * @param caster : Creature pointing out the caster
	 * @param target : Creature pointing out the target
	 * @param crit : boolean tells whether the hit was critical
	 * @return Effect[] : array of effects generated by the skill
	 */
	public Effect[] getSkillEffects(Creature caster, Creature target, boolean crit)
	{
		if ((_skillsOnCrit == null) || !crit)
		{
			return _emptyEffectSet;
		}
		final List<Effect> effects = new ArrayList<>();
		for (Skill skill : _skillsOnCrit)
		{
			if (target.isRaid() && ((skill.getSkillType() == SkillType.CONFUSION) || (skill.getSkillType() == SkillType.MUTE) || (skill.getSkillType() == SkillType.PARALYZE) || (skill.getSkillType() == SkillType.ROOT)))
			{
				continue; // These skills should not work on RaidBoss
			}
			
			if (!skill.checkCondition(caster, target, true))
			{
				continue; // Skill condition not met
			}
			
			if (target.getFirstEffect(skill.getId()) != null)
			{
				target.getFirstEffect(skill.getId()).exit(false);
			}
			for (Effect e : skill.getEffects(caster, target, false, false, false))
			{
				effects.add(e);
			}
		}
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		return effects.toArray(new Effect[effects.size()]);
	}
	
	/**
	 * Returns effects of skills associated with the item to be triggered onCast.
	 * @param caster : Creature pointing out the caster
	 * @param target : Creature pointing out the target
	 * @param trigger : Skill pointing out the skill triggering this action
	 * @return Effect[] : array of effects generated by the skill
	 */
	public boolean getSkillEffects(Creature caster, Creature target, Skill trigger)
	{
		boolean output = false;
		
		if (_skillsOnCast == null)
		{
			return output;
		}
		
		for (Skill skill : _skillsOnCast)
		{
			if (trigger.isOffensive() != skill.isOffensive())
			{
				continue; // Trigger only same type of skill
			}
			
			if ((trigger.getId() >= 1320) && (trigger.getId() <= 1322))
			{
				continue; // No buff with Common and Dwarven Craft
			}
			
			if (trigger.isPotion())
			{
				continue; // No buff with potions
			}
			
			if (target.isRaid() && ((skill.getSkillType() == SkillType.CONFUSION) || (skill.getSkillType() == SkillType.MUTE) || (skill.getSkillType() == SkillType.PARALYZE) || (skill.getSkillType() == SkillType.ROOT)))
			{
				continue; // These skills should not work on RaidBoss
			}
			
			if (trigger.isToggle()/* && skill.getSkillType() == SkillType.BUFF */)
			{
				continue; // No buffing with toggle skills
			}
			
			if (!skill.checkCondition(caster, target, true)) // check skill condition and chance
			{
				continue; // Skill condition not met
			}
			
			try
			{
				// Get the skill handler corresponding to the skill type
				final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
				final Creature[] targets = new Creature[1];
				targets[0] = target;
				
				// Launch the magic skill and calculate its effects
				if (handler != null)
				{
					handler.useSkill(caster, skill, targets);
				}
				else
				{
					skill.useSkill(caster, targets);
				}
				
				if ((caster instanceof PlayerInstance) && (target instanceof NpcInstance))
				{
					for (Quest quest : ((NpcInstance) target).getTemplate().getEventQuests(EventType.ON_SKILL_USE))
					{
						quest.notifySkillUse((NpcInstance) target, (PlayerInstance) caster, skill);
					}
				}
				
				output = true;
			}
			catch (IOException e)
			{
			}
		}
		
		return output;
	}
	
	/**
	 * Add the Skill skill to the list of skills generated by the item triggered by critical hit
	 * @param skill : Skill
	 */
	public void attachOnCrit(Skill skill)
	{
		if (_skillsOnCrit == null)
		{
			_skillsOnCrit = new Skill[]
			{
				skill
			};
		}
		else
		{
			final int len = _skillsOnCrit.length;
			final Skill[] tmp = new Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest, number of components to be copied)
			System.arraycopy(_skillsOnCrit, 0, tmp, 0, len);
			tmp[len] = skill;
			_skillsOnCrit = tmp;
		}
	}
	
	/**
	 * Add the Skill skill to the list of skills generated by the item triggered by casting spell
	 * @param skill : Skill
	 */
	public void attachOnCast(Skill skill)
	{
		if (_skillsOnCast == null)
		{
			_skillsOnCast = new Skill[]
			{
				skill
			};
		}
		else
		{
			final int len = _skillsOnCast.length;
			final Skill[] tmp = new Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest, number of components to be copied)
			System.arraycopy(_skillsOnCast, 0, tmp, 0, len);
			tmp[len] = skill;
			_skillsOnCast = tmp;
		}
	}
}
