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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.Config;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.EtcItemType;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.effects.EffectTemplate;
import org.l2jserver.gameserver.model.skills.funcs.Func;
import org.l2jserver.gameserver.model.skills.funcs.FuncTemplate;

/**
 * This class contains all informations concerning the item (weapon, armor, etc).<br>
 * Mother class of :
 * <li>Armor</li>
 * <li>EtcItem</li>
 * <li>Weapon</li>
 * @version $Revision: 1.7.2.2.2.5 $ $Date: 2005/04/06 18:25:18 $
 */
public abstract class Item
{
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	public static final int TYPE2_PET_WOLF = 6;
	public static final int TYPE2_PET_HATCHLING = 7;
	public static final int TYPE2_PET_STRIDER = 8;
	public static final int TYPE2_PET_BABY = 9;
	
	public static final int SLOT_NONE = 0x0000;
	public static final int SLOT_UNDERWEAR = 0x0001;
	public static final int SLOT_R_EAR = 0x0002;
	public static final int SLOT_L_EAR = 0x0004;
	public static final int SLOT_NECK = 0x0008;
	public static final int SLOT_R_FINGER = 0x0010;
	public static final int SLOT_L_FINGER = 0x0020;
	public static final int SLOT_HEAD = 0x0040;
	public static final int SLOT_R_HAND = 0x0080;
	public static final int SLOT_L_HAND = 0x0100;
	public static final int SLOT_GLOVES = 0x0200;
	public static final int SLOT_CHEST = 0x0400;
	public static final int SLOT_LEGS = 0x0800;
	public static final int SLOT_FEET = 0x1000;
	public static final int SLOT_BACK = 0x2000;
	public static final int SLOT_LR_HAND = 0x4000;
	public static final int SLOT_FULL_ARMOR = 0x8000;
	public static final int SLOT_HAIR = 0x010000;
	public static final int SLOT_WOLF = 0x020000;
	public static final int SLOT_HATCHLING = 0x100000;
	public static final int SLOT_STRIDER = 0x200000;
	public static final int SLOT_BABYPET = 0x400000;
	public static final int SLOT_FACE = 0x040000;
	public static final int SLOT_DHAIR = 0x080000;
	
	public static final int CRYSTAL_NONE = 0x00;
	public static final int CRYSTAL_D = 0x01;
	public static final int CRYSTAL_C = 0x02;
	public static final int CRYSTAL_B = 0x03;
	public static final int CRYSTAL_A = 0x04;
	public static final int CRYSTAL_S = 0x05;
	
	private static final int[] crystalItemId =
	{
		0,
		1458,
		1459,
		1460,
		1461,
		1462
	};
	private static final int[] crystalEnchantBonusArmor =
	{
		0,
		11,
		6,
		11,
		19,
		25
	};
	private static final int[] crystalEnchantBonusWeapon =
	{
		0,
		90,
		45,
		67,
		144,
		250
	};
	
	private final int _itemId;
	private final String _name;
	private final int _type1; // needed for item list (inventory)
	private final int _type2; // different lists for armor, weapon, etc
	private final int _weight;
	private final boolean _crystallizable;
	private final boolean _stackable;
	private final int _crystalType; // default to none-grade
	private final int _duration;
	private final int _bodyPart;
	private final int _referencePrice;
	private final int _crystalCount;
	private final boolean _sellable;
	private final boolean _dropable;
	private final boolean _destroyable;
	private final boolean _tradeable;
	
	protected final Enum<?> _type;
	
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;
	protected Skill[] _skills;
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	protected static final Effect[] _emptyEffectSet = new Effect[0];
	
	/**
	 * Constructor of the Item that fill class variables.<br>
	 * <u><i>Variables filled :</i></u><br>
	 * <li>type</li>
	 * <li>_itemId</li>
	 * <li>_name</li>
	 * <li>_type1 & _type2</li>
	 * <li>_weight</li>
	 * <li>_crystallizable</li>
	 * <li>_stackable</li>
	 * <li>_crystalType & _crystlaCount</li>
	 * <li>_duration</li>
	 * <li>_bodypart</li>
	 * <li>_referencePrice</li>
	 * <li>_sellable</li><br>
	 * @param type : Enum designating the type of the item
	 * @param set : StatSet corresponding to a set of couples (key,value) for description of the item
	 */
	protected Item(Enum<?> type, StatSet set)
	{
		_type = type;
		_itemId = set.getInt("item_id");
		_name = set.getString("name");
		_type1 = set.getInt("type1"); // needed for item list (inventory)
		_type2 = set.getInt("type2"); // different lists for armor, weapon, etc
		_weight = set.getInt("weight", 0);
		_crystallizable = set.getBoolean("crystallizable", false);
		_stackable = set.getBoolean("stackable", false);
		
		switch (set.getString("crystal_type", ""))
		{
			case "d":
			{
				_crystalType = CRYSTAL_D;
				break;
			}
			case "c":
			{
				_crystalType = CRYSTAL_C;
				break;
			}
			case "b":
			{
				_crystalType = CRYSTAL_B;
				break;
			}
			case "a":
			{
				_crystalType = CRYSTAL_A;
				break;
			}
			case "s":
			{
				_crystalType = CRYSTAL_S;
				break;
			}
			default:
			{
				_crystalType = CRYSTAL_NONE;
				break;
			}
		}
		
		_duration = set.getInt("duration", -1);
		_bodyPart = set.getInt("bodypart", SLOT_NONE);
		_referencePrice = set.getInt("price", 0);
		_crystalCount = set.getInt("crystal_count", 0);
		_sellable = set.getBoolean("sellable", true);
		_dropable = set.getBoolean("dropable", true);
		_destroyable = set.getBoolean("destroyable", true);
		_tradeable = set.getBoolean("tradeable", true);
	}
	
	/**
	 * Returns the itemType.
	 * @return Enum
	 */
	public Enum<?> getItemType()
	{
		return _type;
	}
	
	/**
	 * Returns the duration of the item
	 * @return int
	 */
	public int getDuration()
	{
		return _duration;
	}
	
	/**
	 * Returns the ID of the iden
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	public abstract int getItemMask();
	
	/**
	 * Returns the type 2 of the item
	 * @return int
	 */
	public int getType2()
	{
		return _type2;
	}
	
	/**
	 * Returns the weight of the item
	 * @return int
	 */
	public int getWeight()
	{
		return _weight;
	}
	
	/**
	 * Returns if the item is crystallizable
	 * @return boolean
	 */
	public boolean isCrystallizable()
	{
		return _crystallizable;
	}
	
	/**
	 * Return the type of crystal if item is crystallizable
	 * @return int
	 */
	public int getCrystalType()
	{
		return _crystalType;
	}
	
	/**
	 * Return the type of crystal if item is crystallizable
	 * @return int
	 */
	public int getCrystalItemId()
	{
		return crystalItemId[_crystalType];
	}
	
	/**
	 * Returns the grade of the item.<br>
	 * <u><i>Concept :</i></u><br>
	 * In fact, this fucntion returns the type of crystal of the item.
	 * @return int
	 */
	public int getItemGrade()
	{
		return _crystalType;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization
	 * @return int
	 */
	public int getCrystalCount()
	{
		return _crystalCount;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization on specific enchant level
	 * @param enchantLevel
	 * @return int
	 */
	public int getCrystalCount(int enchantLevel)
	{
		if (enchantLevel > 3)
		{
			switch (_type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
				{
					return _crystalCount + (crystalEnchantBonusArmor[_crystalType] * ((3 * enchantLevel) - 6));
				}
				case TYPE2_WEAPON:
				{
					return _crystalCount + (crystalEnchantBonusWeapon[_crystalType] * ((2 * enchantLevel) - 3));
				}
				default:
				{
					return _crystalCount;
				}
			}
		}
		else if (enchantLevel > 0)
		{
			switch (_type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
				{
					return _crystalCount + (crystalEnchantBonusArmor[_crystalType] * enchantLevel);
				}
				case TYPE2_WEAPON:
				{
					return _crystalCount + (crystalEnchantBonusWeapon[_crystalType] * enchantLevel);
				}
				default:
				{
					return _crystalCount;
				}
			}
		}
		else
		{
			return _crystalCount;
		}
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Return the part of the body used with the item.
	 * @return int
	 */
	public int getBodyPart()
	{
		return _bodyPart;
	}
	
	/**
	 * Returns the type 1 of the item
	 * @return int
	 */
	public int getType1()
	{
		return _type1;
	}
	
	/**
	 * Returns if the item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _stackable;
	}
	
	/**
	 * Returns if the item is consumable
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return false;
	}
	
	/**
	 * Returns the price of reference of the item
	 * @return int
	 */
	public int getReferencePrice()
	{
		return isConsumable() ? (int) (_referencePrice * Config.RATE_CONSUMABLE_COST) : _referencePrice;
	}
	
	/**
	 * Returns if the item can be sold
	 * @return boolean
	 */
	public boolean isSellable()
	{
		return _sellable;
	}
	
	/**
	 * Returns if the item can dropped
	 * @return boolean
	 */
	public boolean isDropable()
	{
		return _dropable;
	}
	
	/**
	 * Returns if the item can destroy
	 * @return boolean
	 */
	public boolean isDestroyable()
	{
		return _destroyable;
	}
	
	/**
	 * Returns if the item can add to trade
	 * @return boolean
	 */
	public boolean isTradeable()
	{
		return _tradeable;
	}
	
	public boolean isPotion()
	{
		return (_type == EtcItemType.POTION);
	}
	
	/**
	 * Returns if item is for hatchling
	 * @return boolean
	 */
	public boolean isForHatchling()
	{
		return _type2 == TYPE2_PET_HATCHLING;
	}
	
	/**
	 * Returns if item is for strider
	 * @return boolean
	 */
	public boolean isForStrider()
	{
		return _type2 == TYPE2_PET_STRIDER;
	}
	
	/**
	 * Returns if item is for wolf
	 * @return boolean
	 */
	public boolean isForWolf()
	{
		return _type2 == TYPE2_PET_WOLF;
	}
	
	/**
	 * Returns if item is for wolf
	 * @return boolean
	 */
	public boolean isForBabyPet()
	{
		return _type2 == TYPE2_PET_BABY;
	}
	
	/**
	 * Returns array of Func objects containing the list of functions used by the item
	 * @param instance : ItemInstance pointing out the item
	 * @param creature : Creature pointing out the player
	 * @return Func[] : array of functions
	 */
	public Func[] getStatFuncs(ItemInstance instance, Creature creature)
	{
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		final List<Func> funcs = new ArrayList<>();
		for (FuncTemplate t : _funcTemplates)
		{
			final Env env = new Env();
			env.player = creature;
			env.target = creature;
			env.item = instance;
			final Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	/**
	 * Returns the effects associated with the item.
	 * @param instance : ItemInstance pointing out the item
	 * @param creature : Creature pointing out the player
	 * @return Effect[] : array of effects generated by the item
	 */
	public Effect[] getEffects(ItemInstance instance, Creature creature)
	{
		if (_effectTemplates == null)
		{
			return _emptyEffectSet;
		}
		final List<Effect> effects = new ArrayList<>();
		for (EffectTemplate et : _effectTemplates)
		{
			final Env env = new Env();
			env.player = creature;
			env.target = creature;
			env.item = instance;
			final Effect e = et.getEffect(env);
			if (e != null)
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
	 * Returns effects of skills associated with the item.
	 * @param caster : Creature pointing out the caster
	 * @param target : Creature pointing out the target
	 * @return Effect[] : array of effects generated by the skill
	 */
	public Effect[] getSkillEffects(Creature caster, Creature target)
	{
		if (_skills == null)
		{
			return _emptyEffectSet;
		}
		final List<Effect> effects = new ArrayList<>();
		for (Skill skill : _skills)
		{
			if (!skill.checkCondition(caster, target, true))
			{
				continue; // Skill condition not met
			}
			
			if (target.getFirstEffect(skill.getId()) != null)
			{
				target.removeEffect(target.getFirstEffect(skill.getId()));
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
	 * Add the FuncTemplate f to the list of functions used with the item
	 * @param f : FuncTemplate to add
	 */
	public void attach(FuncTemplate f)
	{
		// If _functTemplates is empty, create it and add the FuncTemplate f in it
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = _funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest, number of components to be copied)
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	/**
	 * Add the EffectTemplate effect to the list of effects generated by the item
	 * @param effect : EffectTemplate
	 */
	public void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			final int len = _effectTemplates.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}
	
	/**
	 * Add the Skill skill to the list of skills generated by the item
	 * @param skill : Skill
	 */
	public void attach(Skill skill)
	{
		if (_skills == null)
		{
			_skills = new Skill[]
			{
				skill
			};
		}
		else
		{
			final int len = _skills.length;
			final Skill[] tmp = new Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_skills, 0, tmp, 0, len);
			tmp[len] = skill;
			_skills = tmp;
		}
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _name;
	}
}
