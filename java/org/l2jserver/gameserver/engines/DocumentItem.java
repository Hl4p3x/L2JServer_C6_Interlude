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
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.items.Armor;
import org.l2jserver.gameserver.model.items.EtcItem;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.type.ArmorType;
import org.l2jserver.gameserver.model.items.type.EtcItemType;
import org.l2jserver.gameserver.model.items.type.WeaponType;

/**
 * @author mkizub, JIV
 */
final class DocumentItem extends DocumentBase
{
	private ItemDataHolder _currentItem = null;
	private final List<Item> _itemsInFile = new ArrayList<>();
	
	private static final Map<String, Integer> _slots = new HashMap<>();
	static
	{
		_slots.put("chest", Item.SLOT_CHEST);
		_slots.put("fullarmor", Item.SLOT_FULL_ARMOR);
		_slots.put("head", Item.SLOT_HEAD);
		_slots.put("hair", Item.SLOT_HAIR);
		_slots.put("face", Item.SLOT_FACE);
		_slots.put("dhair", Item.SLOT_DHAIR);
		_slots.put("underwear", Item.SLOT_UNDERWEAR);
		_slots.put("back", Item.SLOT_BACK);
		_slots.put("neck", Item.SLOT_NECK);
		_slots.put("legs", Item.SLOT_LEGS);
		_slots.put("feet", Item.SLOT_FEET);
		_slots.put("gloves", Item.SLOT_GLOVES);
		_slots.put("chest,legs", Item.SLOT_CHEST | Item.SLOT_LEGS);
		_slots.put("rhand", Item.SLOT_R_HAND);
		_slots.put("lhand", Item.SLOT_L_HAND);
		_slots.put("lrhand", Item.SLOT_LR_HAND);
		_slots.put("rear,lear", Item.SLOT_R_EAR | Item.SLOT_L_EAR);
		_slots.put("rfinger,lfinger", Item.SLOT_R_FINGER | Item.SLOT_L_FINGER);
		_slots.put("none", Item.SLOT_NONE);
		_slots.put("wolf", Item.SLOT_WOLF); // for wolf
		_slots.put("hatchling", Item.SLOT_HATCHLING); // for hatchling
		_slots.put("strider", Item.SLOT_STRIDER); // for strider
		_slots.put("babypet", Item.SLOT_BABYPET); // for babypet
	}
	private static final Map<String, WeaponType> _weaponTypes = new HashMap<>();
	static
	{
		_weaponTypes.put("blunt", WeaponType.BLUNT);
		_weaponTypes.put("bow", WeaponType.BOW);
		_weaponTypes.put("dagger", WeaponType.DAGGER);
		_weaponTypes.put("dual", WeaponType.DUAL);
		_weaponTypes.put("dualfist", WeaponType.DUALFIST);
		_weaponTypes.put("etc", WeaponType.ETC);
		_weaponTypes.put("fist", WeaponType.FIST);
		_weaponTypes.put("none", WeaponType.NONE); // these are shields!
		_weaponTypes.put("pole", WeaponType.POLE);
		_weaponTypes.put("sword", WeaponType.SWORD);
		_weaponTypes.put("bigsword", WeaponType.BIGSWORD); // Two-Handed Swords
		_weaponTypes.put("pet", WeaponType.PET); // Pet Weapon
		_weaponTypes.put("rod", WeaponType.ROD); // Fishing Rods
		_weaponTypes.put("bigblunt", WeaponType.BIGBLUNT); // Two handed blunt
	}
	private static final Map<String, ArmorType> _armorTypes = new HashMap<>();
	static
	{
		_armorTypes.put("none", ArmorType.NONE);
		_armorTypes.put("light", ArmorType.LIGHT);
		_armorTypes.put("heavy", ArmorType.HEAVY);
		_armorTypes.put("magic", ArmorType.MAGIC);
		_armorTypes.put("pet", ArmorType.PET);
	}
	
	public DocumentItem(File file)
	{
		super(file);
	}
	
	@Override
	protected StatSet getStatSet()
	{
		return _currentItem.set;
	}
	
	@Override
	protected String getTableValue(String name)
	{
		return _tables.get(name)[_currentItem.currentLevel];
	}
	
	@Override
	protected String getTableValue(String name, int idx)
	{
		return _tables.get(name)[idx - 1];
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						try
						{
							_currentItem = new ItemDataHolder();
							parseItem(d);
							_itemsInFile.add(_currentItem.item);
							resetTable();
						}
						catch (Exception e)
						{
							LOGGER.log(Level.WARNING, "Cannot create item " + _currentItem.id, e);
						}
					}
				}
			}
		}
	}
	
	protected void parseItem(Node n)
	{
		final int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		final String className = n.getAttributes().getNamedItem("type").getNodeValue();
		final String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
		
		_currentItem.id = itemId;
		_currentItem.name = itemName;
		_currentItem.set = new StatSet();
		_currentItem.set.set("item_id", itemId);
		_currentItem.set.set("name", itemName);
		
		final Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("set".equals(n.getNodeName()))
			{
				if (_currentItem.item != null)
				{
					throw new IllegalStateException("Item created but set node found! Item " + itemId);
				}
				parseBeanSet(n, _currentItem.set, 1);
			}
		}
		
		if (className.equals("Weapon"))
		{
			int bodypart = _slots.get(_currentItem.set.getString("bodypart"));
			_currentItem.type = _weaponTypes.get(_currentItem.set.getString("weapon_type"));
			
			// lets see if this is a shield
			if (_currentItem.type == WeaponType.NONE)
			{
				_currentItem.set.set("type1", Item.TYPE1_SHIELD_ARMOR);
				_currentItem.set.set("type2", Item.TYPE2_SHIELD_ARMOR);
			}
			else
			{
				_currentItem.set.set("type1", Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				_currentItem.set.set("type2", Item.TYPE2_WEAPON);
			}
			
			if (_currentItem.type == WeaponType.PET)
			{
				_currentItem.set.set("type1", Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				
				switch (_currentItem.set.getString("bodypart"))
				{
					case "wolf":
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_WOLF);
						break;
					}
					case "hatchling":
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_HATCHLING);
						break;
					}
					case "babypet":
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_BABY);
						break;
					}
					default:
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_STRIDER);
						break;
					}
				}
				
				bodypart = Item.SLOT_R_HAND;
			}
			
			_currentItem.set.set("bodypart", bodypart);
		}
		else if (className.equals("Armor"))
		{
			_currentItem.type = _armorTypes.get(_currentItem.set.getString("armor_type"));
			
			int bodypart = _slots.get(_currentItem.set.getString("bodypart"));
			if ((bodypart == Item.SLOT_NECK) || (bodypart == Item.SLOT_HAIR) || (bodypart == Item.SLOT_FACE) || (bodypart == Item.SLOT_DHAIR) || ((bodypart & Item.SLOT_L_EAR) != 0) || ((bodypart & Item.SLOT_L_FINGER) != 0))
			{
				_currentItem.set.set("type1", Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				_currentItem.set.set("type2", Item.TYPE2_ACCESSORY);
			}
			else
			{
				_currentItem.set.set("type1", Item.TYPE1_SHIELD_ARMOR);
				_currentItem.set.set("type2", Item.TYPE2_SHIELD_ARMOR);
			}
			
			if (_currentItem.type == ArmorType.PET)
			{
				_currentItem.set.set("type1", Item.TYPE1_SHIELD_ARMOR);
				
				switch (_currentItem.set.getString("bodypart"))
				{
					case "wolf":
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_WOLF);
						break;
					}
					case "hatchling":
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_HATCHLING);
						break;
					}
					case "babypet":
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_BABY);
						break;
					}
					default:
					{
						_currentItem.set.set("type2", Item.TYPE2_PET_STRIDER);
						break;
					}
				}
				
				bodypart = Item.SLOT_CHEST;
			}
			
			_currentItem.set.set("bodypart", bodypart);
		}
		else
		{
			_currentItem.set.set("type1", Item.TYPE1_ITEM_QUESTITEM_ADENA);
			_currentItem.set.set("type2", Item.TYPE2_OTHER);
			
			final String itemType = _currentItem.set.getString("item_type");
			switch (itemType)
			{
				case "none":
				{
					_currentItem.type = EtcItemType.OTHER; // only for default
					break;
				}
				case "castle_guard":
				{
					_currentItem.type = EtcItemType.SCROLL; // dummy
					break;
				}
				case "pet_collar":
				{
					_currentItem.type = EtcItemType.PET_COLLAR;
					break;
				}
				case "potion":
				{
					_currentItem.type = EtcItemType.POTION;
					break;
				}
				case "recipe":
				{
					_currentItem.type = EtcItemType.RECEIPE;
					break;
				}
				case "scroll":
				{
					_currentItem.type = EtcItemType.SCROLL;
					break;
				}
				case "seed":
				{
					_currentItem.type = EtcItemType.SEED;
					break;
				}
				case "shot":
				{
					_currentItem.type = EtcItemType.SHOT;
					break;
				}
				case "spellbook":
				{
					_currentItem.type = EtcItemType.SPELLBOOK; // Spellbook, Amulet, Blueprint
					break;
				}
				case "herb":
				{
					_currentItem.type = EtcItemType.HERB;
					break;
				}
				case "arrow":
				{
					_currentItem.type = EtcItemType.ARROW;
					_currentItem.set.set("bodypart", Item.SLOT_L_HAND);
					break;
				}
				case "quest":
				{
					_currentItem.type = EtcItemType.QUEST;
					_currentItem.set.set("type2", Item.TYPE2_QUEST);
					break;
				}
				case "lure":
				{
					_currentItem.type = EtcItemType.OTHER;
					_currentItem.set.set("bodypart", Item.SLOT_L_HAND);
					break;
				}
				default:
				{
					_currentItem.type = EtcItemType.OTHER;
					break;
				}
			}
			
			final String consume = _currentItem.set.getString("consume_type");
			switch (consume)
			{
				case "asset":
				{
					_currentItem.type = EtcItemType.MONEY;
					_currentItem.set.set("stackable", true);
					_currentItem.set.set("type2", Item.TYPE2_MONEY);
					break;
				}
				case "stackable":
				{
					_currentItem.set.set("stackable", true);
					break;
				}
				default:
				{
					_currentItem.set.set("stackable", false);
					break;
				}
			}
		}
		
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("for".equals(n.getNodeName()))
			{
				makeItem();
				parseTemplate(n, _currentItem.item);
			}
		}
		
		// bah! in this point item doesn't have to be still created
		makeItem();
	}
	
	private void makeItem()
	{
		if (_currentItem.item != null)
		{
			return; // item is already created
		}
		
		if (_currentItem.type instanceof ArmorType)
		{
			_currentItem.item = new Armor((ArmorType) _currentItem.type, _currentItem.set);
		}
		else if (_currentItem.type instanceof WeaponType)
		{
			_currentItem.item = new Weapon((WeaponType) _currentItem.type, _currentItem.set);
		}
		else if (_currentItem.type instanceof EtcItemType)
		{
			_currentItem.item = new EtcItem((EtcItemType) _currentItem.type, _currentItem.set);
		}
		else
		{
			throw new Error("Unknown item type for " + _currentItem.set.getInt("item_id") + " " + _currentItem.type);
		}
	}
	
	public List<Item> getItemList()
	{
		return _itemsInFile;
	}
}
