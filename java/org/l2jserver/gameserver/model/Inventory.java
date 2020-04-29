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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.xml.ArmorSetData;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Armor;
import org.l2jserver.gameserver.model.items.EtcItem;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance.ItemLocation;
import org.l2jserver.gameserver.model.items.type.EtcItemType;
import org.l2jserver.gameserver.model.items.type.WeaponType;

/**
 * This class manages inventory
 * @version $Revision: 1.13.2.9.2.12 $ $Date: 2005/03/29 23:15:15 $ rewritten 23.2.2006 by Advi
 */
public abstract class Inventory extends ItemContainer
{
	public interface PaperdollListener
	{
		void notifyEquiped(int slot, ItemInstance inst);
		
		void notifyUnequiped(int slot, ItemInstance inst);
	}
	
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_LEAR = 1;
	public static final int PAPERDOLL_REAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_LFINGER = 4;
	public static final int PAPERDOLL_RFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_LRHAND = 14;
	public static final int PAPERDOLL_FACE = 15;
	public static final int PAPERDOLL_HAIR = 16;
	public static final int PAPERDOLL_DHAIR = 17;
	
	public static final int PAPERDOLL_TOTALSLOTS = 18;
	
	// Speed percentage mods
	public static final double MAX_ARMOR_WEIGHT = 12000;
	
	private final ItemInstance[] _paperdoll;
	private final List<PaperdollListener> _paperdollListeners;
	
	// protected to be accessed from child classes only
	protected int _totalWeight;
	
	// used to quickly check for using of items of special type
	private int _wearedMask;
	
	final class FormalWearListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, ItemInstance item)
		{
			if (!(getOwner() instanceof PlayerInstance))
			{
				return;
			}
			
			final PlayerInstance owner = (PlayerInstance) getOwner();
			if (item.getItemId() == 6408)
			{
				owner.setWearingFormalWear(false);
			}
		}
		
		@Override
		public void notifyEquiped(int slot, ItemInstance item)
		{
			if (!(getOwner() instanceof PlayerInstance))
			{
				return;
			}
			
			final PlayerInstance owner = (PlayerInstance) getOwner();
			
			// If player equip Formal Wear unequip weapons and abort cast/attack
			if (item.getItemId() == 6408)
			{
				owner.setWearingFormalWear(true);
				if (owner.isCastingNow())
				{
					owner.abortCast();
				}
				if (owner.isAttackingNow())
				{
					owner.abortAttack();
				}
				unEquipItemInSlot(PAPERDOLL_RHAND);
				unEquipItemInSlot(PAPERDOLL_LHAND);
				unEquipItemInSlot(PAPERDOLL_LRHAND);
			}
			// else if (!owner.isWearingFormalWear())
			// {
			// return;
			// }
		}
	}
	
	/**
	 * Recorder of alterations in inventory
	 */
	public static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory _inventory;
		private final List<ItemInstance> _changed;
		
		/**
		 * Constructor of the ChangeRecorder
		 * @param inventory
		 */
		ChangeRecorder(Inventory inventory)
		{
			_inventory = inventory;
			_changed = new ArrayList<>();
			_inventory.addPaperdollListener(this);
		}
		
		/**
		 * Add alteration in inventory when item equiped
		 */
		@Override
		public void notifyEquiped(int slot, ItemInstance item)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}
		
		/**
		 * Add alteration in inventory when item unequiped
		 */
		@Override
		public void notifyUnequiped(int slot, ItemInstance item)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}
		
		/**
		 * Returns alterations in inventory
		 * @return ItemInstance[] : array of alterated items
		 */
		public ItemInstance[] getChangedItems()
		{
			return _changed.toArray(new ItemInstance[_changed.size()]);
		}
	}
	
	final class BowListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, ItemInstance item)
		{
			if (slot != PAPERDOLL_LRHAND)
			{
				return;
			}
			
			if (item.getItemType() == WeaponType.BOW)
			{
				final ItemInstance arrow = getPaperdollItem(PAPERDOLL_LHAND);
				if (arrow != null)
				{
					setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				
				PlayerInstance player = null;
				Skill skill = null;
				if ((item.getItemId() == 9140) && (getOwner() instanceof PlayerInstance))
				{
					player = (PlayerInstance) getOwner();
					skill = SkillTable.getInstance().getInfo(3261, 1);
					player.removeSkill(skill);
					player.sendSkillList();
				}
			}
		}
		
		@Override
		public void notifyEquiped(int slot, ItemInstance item)
		{
			if (slot != PAPERDOLL_LRHAND)
			{
				return;
			}
			
			if (item.getItemType() == WeaponType.BOW)
			{
				final ItemInstance arrow = findArrowForBow(item.getItem());
				if (arrow != null)
				{
					setPaperdollItem(PAPERDOLL_LHAND, arrow);
					// InventoryUpdate();
				}
				
				PlayerInstance player = null;
				Skill skill = null;
				if ((item.getItemId() == 9140) && (getOwner() instanceof PlayerInstance))
				{
					player = (PlayerInstance) getOwner();
					skill = SkillTable.getInstance().getInfo(3261, 1);
					player.addSkill(skill, false);
					player.sendSkillList();
				}
			}
		}
	}
	
	final class StatsListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, ItemInstance item)
		{
			if (slot == PAPERDOLL_LRHAND)
			{
				return;
			}
			
			getOwner().removeStatsOwner(item);
		}
		
		@Override
		public void notifyEquiped(int slot, ItemInstance item)
		{
			if (slot == PAPERDOLL_LRHAND)
			{
				return;
			}
			
			getOwner().addStatFuncs(item.getStatFuncs(getOwner()));
		}
	}
	
	final class ItemPassiveSkillsListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, ItemInstance item)
		{
			PlayerInstance player;
			if (getOwner() instanceof PlayerInstance)
			{
				player = (PlayerInstance) getOwner();
			}
			else
			{
				return;
			}
			
			Skill passiveSkill = null;
			Skill enchant4Skill = null;
			
			final Item it = item.getItem();
			if (it instanceof Weapon)
			{
				passiveSkill = ((Weapon) it).getSkill();
				enchant4Skill = ((Weapon) it).getEnchant4Skill();
			}
			else if (it instanceof Armor)
			{
				passiveSkill = ((Armor) it).getSkill();
			}
			
			if (!player.isItemEquippedByItemId(item.getItemId()))
			{
				if (passiveSkill != null)
				{
					player.removeSkill(passiveSkill, false);
					player.sendSkillList();
				}
				
				if (enchant4Skill != null)
				{
					player.removeSkill(enchant4Skill, false);
					player.sendSkillList();
				}
			}
		}
		
		@Override
		public void notifyEquiped(int slot, ItemInstance item)
		{
			PlayerInstance player;
			if (getOwner() instanceof PlayerInstance)
			{
				player = (PlayerInstance) getOwner();
			}
			else
			{
				return;
			}
			
			Skill passiveSkill = null;
			Skill enchant4Skill = null;
			
			final Item it = item.getItem();
			if (it instanceof Weapon)
			{
				// Check for Penality
				player.refreshExpertisePenalty();
				player.refreshMasteryWeapPenality();
				// If player get penality he will not recive SA bonus like retail
				if (player.getExpertisePenalty() == 0)
				{
					// Passive skills from Weapon (SA)
					passiveSkill = ((Weapon) it).getSkill();
				}
				
				if (item.getEnchantLevel() >= 4)
				{
					enchant4Skill = ((Weapon) it).getEnchant4Skill();
				}
			}
			else if (it instanceof Armor)
			{
				// Check for Penality
				player.refreshExpertisePenalty();
				player.refreshMasteryPenality();
				// Passive skills from Armor
				passiveSkill = ((Armor) it).getSkill();
			}
			
			if ((passiveSkill != null) && (!passiveSkill.isSingleEffect() || (player.getInventory().checkHowManyEquipped(item.getItemId()) == 1)))
			{
				player.addSkill(passiveSkill, false);
				player.sendSkillList();
			}
			
			if ((enchant4Skill != null) && (!enchant4Skill.isSingleEffect() || (player.getInventory().checkHowManyEquipped(item.getItemId()) == 1)))
			{
				player.addSkill(enchant4Skill, false);
				player.sendSkillList();
			}
		}
	}
	
	final class ArmorSetListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, ItemInstance item)
		{
			if (!(getOwner() instanceof PlayerInstance))
			{
				return;
			}
			
			final PlayerInstance player = (PlayerInstance) getOwner();
			
			// checks if player worns chest item
			final ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
			if (chestItem == null)
			{
				return;
			}
			
			// checks if there is armorset for chest item that player worns
			final ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
			if (armorSet == null)
			{
				return;
			}
			
			// checks if equipped item is part of set
			if (armorSet.containItem(slot, item.getItemId()))
			{
				if (armorSet.containAll(player))
				{
					final Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
					if (skill != null)
					{
						player.addSkill(skill, false);
						player.sendSkillList();
					}
					else
					{
						LOGGER.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getSkillId() + ".");
					}
					
					if (armorSet.containShield(player)) // has shield from set
					{
						final Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
						if (skills != null)
						{
							player.addSkill(skills, false);
							player.sendSkillList();
						}
						else
						{
							LOGGER.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
						}
					}
					
					if (armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
					{
						final int skillId = armorSet.getEnchant6skillId();
						if (skillId > 0)
						{
							final Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
							if (skille != null)
							{
								player.addSkill(skille, false);
								player.sendSkillList();
							}
							else
							{
								LOGGER.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getEnchant6skillId() + ".");
							}
						}
					}
				}
			}
			else if (armorSet.containShield(item.getItemId()))
			{
				if (armorSet.containAll(player))
				{
					final Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
					if (skills != null)
					{
						player.addSkill(skills, false);
						player.sendSkillList();
					}
					else
					{
						LOGGER.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
					}
				}
			}
		}
		
		@Override
		public void notifyUnequiped(int slot, ItemInstance item)
		{
			if (!(getOwner() instanceof PlayerInstance))
			{
				return;
			}
			
			final PlayerInstance player = (PlayerInstance) getOwner();
			boolean remove = false;
			int removeSkillId1 = 0; // set skill
			int removeSkillId2 = 0; // shield skill
			int removeSkillId3 = 0; // enchant +6 skill
			if (slot == PAPERDOLL_CHEST)
			{
				final ArmorSet armorSet = ArmorSetData.getInstance().getSet(item.getItemId());
				if (armorSet == null)
				{
					return;
				}
				
				remove = true;
				removeSkillId1 = armorSet.getSkillId();
				removeSkillId2 = armorSet.getShieldSkillId();
				removeSkillId3 = armorSet.getEnchant6skillId();
			}
			else
			{
				final ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
				if (chestItem == null)
				{
					return;
				}
				
				final ArmorSet armorSet = ArmorSetData.getInstance().getSet(chestItem.getItemId());
				if (armorSet == null)
				{
					return;
				}
				
				if (armorSet.containItem(slot, item.getItemId())) // removed part of set
				{
					remove = true;
					removeSkillId1 = armorSet.getSkillId();
					removeSkillId2 = armorSet.getShieldSkillId();
					removeSkillId3 = armorSet.getEnchant6skillId();
				}
				else if (armorSet.containShield(item.getItemId())) // removed shield
				{
					remove = true;
					removeSkillId2 = armorSet.getShieldSkillId();
				}
			}
			
			if (remove)
			{
				if (removeSkillId1 != 0)
				{
					final Skill skill = SkillTable.getInstance().getInfo(removeSkillId1, 1);
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						LOGGER.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId1 + ".");
					}
				}
				
				if (removeSkillId2 != 0)
				{
					final Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						LOGGER.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId2 + ".");
					}
				}
				
				if (removeSkillId3 != 0)
				{
					final Skill skill = SkillTable.getInstance().getInfo(removeSkillId3, 1);
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						LOGGER.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId3 + ".");
					}
				}
				player.sendSkillList();
			}
		}
	}
	
	/**
	 * Constructor of the inventory
	 */
	protected Inventory()
	{
		_paperdoll = new ItemInstance[0x12];
		_paperdollListeners = new ArrayList<>();
		addPaperdollListener(new ArmorSetListener());
		addPaperdollListener(new BowListener());
		addPaperdollListener(new ItemPassiveSkillsListener());
		addPaperdollListener(new StatsListener());
		addPaperdollListener(new FormalWearListener());
	}
	
	protected abstract ItemLocation getEquipLocation();
	
	/**
	 * Returns the instance of new ChangeRecorder
	 * @return ChangeRecorder
	 */
	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}
	
	/**
	 * Drop item from inventory and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param item : ItemInstance to be dropped
	 * @param actor : PlayerInstance Player requesting the item drop
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public ItemInstance dropItem(String process, ItemInstance item, PlayerInstance actor, WorldObject reference)
	{
		synchronized (item)
		{
			if (!_items.contains(item))
			{
				return null;
			}
			
			removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(ItemInstance.REMOVED);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	/**
	 * Drop item from inventory by using its <b>objectID</b> and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : int Quantity of items to be dropped
	 * @param actor : PlayerInstance Player requesting the item drop
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public ItemInstance dropItem(String process, int objectId, int count, PlayerInstance actor, WorldObject reference)
	{
		ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		
		// Adjust item quantity and create new instance to drop
		if (item.getCount() > count)
		{
			item.changeCount(process, -count, actor, reference);
			item.setLastChange(ItemInstance.MODIFIED);
			item.updateDatabase();
			
			item = ItemTable.getInstance().createItem(process, item.getItemId(), count, actor, reference);
			item.updateDatabase();
			refreshWeight();
			
			return item;
		}
		// Directly drop entire item
		return dropItem(process, item, actor, reference);
	}
	
	/**
	 * Adds item to inventory for further adjustments and Equip it if necessary (itemlocation defined)
	 * @param item : ItemInstance to be added from inventory
	 */
	@Override
	protected void addItem(ItemInstance item)
	{
		super.addItem(item);
		
		if (item.isEquipped())
		{
			equipItem(item);
		}
	}
	
	/**
	 * Removes item from inventory for further adjustments.
	 * @param item : ItemInstance to be removed from inventory
	 */
	@Override
	protected void removeItem(ItemInstance item)
	{
		for (int i = 0; i < _paperdoll.length; i++)
		{
			if (_paperdoll[i] == item)
			{
				unEquipItemInSlot(i);
			}
		}
		
		super.removeItem(item);
	}
	
	/**
	 * Returns the item in the paperdoll slot
	 * @param slot
	 * @return ItemInstance
	 */
	public ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}
	
	/**
	 * Returns the item in the paperdoll Item slot
	 * @param slot
	 * @return ItemInstance
	 */
	public ItemInstance getPaperdollItemByItemId(int slot)
	{
		switch (slot)
		{
			case 0x01:
			{
				return _paperdoll[0];
			}
			case 0x04:
			{
				return _paperdoll[1];
			}
			case 0x02:
			{
				return _paperdoll[2];
			}
			case 0x08:
			{
				return _paperdoll[3];
			}
			case 0x20:
			{
				return _paperdoll[4];
			}
			case 0x10:
			{
				return _paperdoll[5];
			}
			case 0x40:
			{
				return _paperdoll[6];
			}
			case 0x80:
			{
				return _paperdoll[7];
			}
			case 0x0100:
			{
				return _paperdoll[8];
			}
			case 0x0200:
			{
				return _paperdoll[9];
			}
			case 0x0400:
			{
				return _paperdoll[10];
			}
			case 0x0800:
			{
				return _paperdoll[11];
			}
			case 0x1000:
			{
				return _paperdoll[12];
			}
			case 0x2000:
			{
				return _paperdoll[13];
			}
			case 0x4000:
			{
				return _paperdoll[14];
			}
			case 0x040000:
			{
				return _paperdoll[15];
			}
			case 0x010000:
			{
				return _paperdoll[16];
			}
			case 0x080000:
			{
				return _paperdoll[17];
			}
		}
		return null;
	}
	
	/**
	 * Returns the ID of the item in the paperdol slot
	 * @param slot : int designating the slot
	 * @return int designating the ID of the item
	 */
	public int getPaperdollItemId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			return item.getItemId();
		}
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_DHAIR];
			if (item != null)
			{
				return item.getItemId();
			}
		}
		return 0;
	}
	
	public int getPaperdollAugmentationId(int slot)
	{
		final ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			if (item.getAugmentation() != null)
			{
				return item.getAugmentation().getAugmentationId();
			}
			return 0;
		}
		return 0;
	}
	
	/**
	 * Returns the objectID associated to the item in the paperdoll slot
	 * @param slot : int pointing out the slot
	 * @return int designating the objectID
	 */
	public int getPaperdollObjectId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			return item.getObjectId();
		}
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_DHAIR];
			if (item != null)
			{
				return item.getObjectId();
			}
		}
		return 0;
	}
	
	/**
	 * Adds new inventory's paperdoll listener
	 * @param listener
	 */
	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.add(listener);
	}
	
	/**
	 * Removes a paperdoll listener
	 * @param listener to be deleted
	 */
	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}
	
	/**
	 * Equips an item in the given slot of the paperdoll. <u><i>Remark :</i></u> The item <b>HAS TO BE</b> already in the inventory
	 * @param slot : int pointing out the slot of the paperdoll
	 * @param item : ItemInstance pointing out the item to add in slot
	 * @return ItemInstance designating the item placed in the slot before
	 */
	public ItemInstance setPaperdollItem(int slot, ItemInstance item)
	{
		final ItemInstance old = _paperdoll[slot];
		if (old != item)
		{
			if (old != null)
			{
				_paperdoll[slot] = null;
				// Put old item from paperdoll slot to base location
				old.setLocation(getBaseLocation());
				old.setLastChange(ItemInstance.MODIFIED);
				
				// Get the mask for paperdoll
				int mask = 0;
				for (int i = 0; i < PAPERDOLL_LRHAND; i++)
				{
					final ItemInstance pi = _paperdoll[i];
					if (pi != null)
					{
						mask |= pi.getItem().getItemMask();
					}
				}
				
				_wearedMask = mask;
				
				// Notify all paperdoll listener in order to unequip old item in slot
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}
					
					listener.notifyUnequiped(slot, old);
				}
				
				if (old.isAugmented() && (getOwner() != null) && (getOwner() instanceof PlayerInstance))
				{
					old.getAugmentation().removeBonus((PlayerInstance) getOwner());
				}
				
				old.updateDatabase();
			}
			
			// Add new item in slot of paperdoll
			if (item != null)
			{
				_paperdoll[slot] = item;
				item.setLocation(getEquipLocation(), slot);
				item.setLastChange(ItemInstance.MODIFIED);
				_wearedMask |= item.getItem().getItemMask();
				for (PaperdollListener listener : _paperdollListeners)
				{
					listener.notifyEquiped(slot, item);
				}
				
				item.updateDatabase();
			}
		}
		return old;
	}
	
	/**
	 * Return the mask of weared item
	 * @return int
	 */
	public int getWearedMask()
	{
		return _wearedMask;
	}
	
	public int getSlotFromItem(ItemInstance item)
	{
		int slot = -1;
		final int location = item.getEquipSlot();
		
		switch (location)
		{
			case PAPERDOLL_UNDER:
			{
				slot = Item.SLOT_UNDERWEAR;
				break;
			}
			case PAPERDOLL_LEAR:
			{
				slot = Item.SLOT_L_EAR;
				break;
			}
			case PAPERDOLL_REAR:
			{
				slot = Item.SLOT_R_EAR;
				break;
			}
			case PAPERDOLL_NECK:
			{
				slot = Item.SLOT_NECK;
				break;
			}
			case PAPERDOLL_RFINGER:
			{
				slot = Item.SLOT_R_FINGER;
				break;
			}
			case PAPERDOLL_LFINGER:
			{
				slot = Item.SLOT_L_FINGER;
				break;
			}
			case PAPERDOLL_HAIR:
			{
				slot = Item.SLOT_HAIR;
				break;
			}
			case PAPERDOLL_FACE:
			{
				slot = Item.SLOT_FACE;
				break;
			}
			case PAPERDOLL_DHAIR:
			{
				slot = Item.SLOT_DHAIR;
				break;
			}
			case PAPERDOLL_HEAD:
			{
				slot = Item.SLOT_HEAD;
				break;
			}
			case PAPERDOLL_RHAND:
			{
				slot = Item.SLOT_R_HAND;
				break;
			}
			case PAPERDOLL_LHAND:
			{
				slot = Item.SLOT_L_HAND;
				break;
			}
			case PAPERDOLL_GLOVES:
			{
				slot = Item.SLOT_GLOVES;
				break;
			}
			case PAPERDOLL_CHEST:
			{
				slot = Item.SLOT_CHEST;
				break;
			}
			case PAPERDOLL_LEGS:
			{
				slot = Item.SLOT_LEGS;
				break;
			}
			case PAPERDOLL_BACK:
			{
				slot = Item.SLOT_BACK;
				break;
			}
			case PAPERDOLL_FEET:
			{
				slot = Item.SLOT_FEET;
				break;
			}
			case PAPERDOLL_LRHAND:
			{
				slot = Item.SLOT_LR_HAND;
				break;
			}
		}
		return slot;
	}
	
	/**
	 * Unequips item in body slot and returns alterations.
	 * @param slot : int designating the slot of the paperdoll
	 * @return ItemInstance[] : list of changes
	 */
	public ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
	{
		final ChangeRecorder recorder = newRecorder();
		
		try
		{
			unEquipItemInBodySlot(slot);
			
			if (getOwner() instanceof PlayerInstance)
			{
				((PlayerInstance) getOwner()).refreshExpertisePenalty();
				((PlayerInstance) getOwner()).refreshMasteryPenality();
				((PlayerInstance) getOwner()).refreshMasteryWeapPenality();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	/**
	 * Sets item in slot of the paperdoll to null value
	 * @param pdollSlot : int designating the slot
	 * @return ItemInstance designating the item in slot before change
	 */
	public ItemInstance unEquipItemInSlot(int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}
	
	/**
	 * Unepquips item in slot and returns alterations
	 * @param slot : int designating the slot
	 * @return ItemInstance[] : list of items altered
	 */
	public ItemInstance[] unEquipItemInSlotAndRecord(int slot)
	{
		final ChangeRecorder recorder = newRecorder();
		
		try
		{
			unEquipItemInSlot(slot);
			
			if (getOwner() instanceof PlayerInstance)
			{
				((PlayerInstance) getOwner()).refreshExpertisePenalty();
				((PlayerInstance) getOwner()).refreshMasteryPenality();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	/**
	 * Unequips item in slot (i.e. equips with default value)
	 * @param slot : int designating the slot
	 */
	private void unEquipItemInBodySlot(int slot)
	{
		int pdollSlot = -1;
		switch (slot)
		{
			case Item.SLOT_L_EAR:
			{
				pdollSlot = PAPERDOLL_LEAR;
				break;
			}
			case Item.SLOT_R_EAR:
			{
				pdollSlot = PAPERDOLL_REAR;
				break;
			}
			case Item.SLOT_NECK:
			{
				pdollSlot = PAPERDOLL_NECK;
				break;
			}
			case Item.SLOT_R_FINGER:
			{
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			}
			case Item.SLOT_L_FINGER:
			{
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			}
			case Item.SLOT_HAIR:
			{
				pdollSlot = PAPERDOLL_HAIR;
				break;
			}
			case Item.SLOT_FACE:
			{
				pdollSlot = PAPERDOLL_FACE;
				break;
			}
			case Item.SLOT_DHAIR:
			{
				setPaperdollItem(PAPERDOLL_HAIR, null);
				setPaperdollItem(PAPERDOLL_FACE, null); // this should be the same as in DHAIR
				pdollSlot = PAPERDOLL_DHAIR;
				break;
			}
			case Item.SLOT_HEAD:
			{
				pdollSlot = PAPERDOLL_HEAD;
				break;
			}
			case Item.SLOT_R_HAND:
			{
				pdollSlot = PAPERDOLL_RHAND;
				break;
			}
			case Item.SLOT_L_HAND:
			{
				pdollSlot = PAPERDOLL_LHAND;
				break;
			}
			case Item.SLOT_GLOVES:
			{
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			}
			case Item.SLOT_CHEST:
			{
				pdollSlot = PAPERDOLL_CHEST;
				break;
			}
			case Item.SLOT_FULL_ARMOR:
			{
				pdollSlot = PAPERDOLL_CHEST;
				break;
			}
			case Item.SLOT_LEGS:
			{
				pdollSlot = PAPERDOLL_LEGS;
				break;
			}
			case Item.SLOT_BACK:
			{
				pdollSlot = PAPERDOLL_BACK;
				break;
			}
			case Item.SLOT_FEET:
			{
				pdollSlot = PAPERDOLL_FEET;
				break;
			}
			case Item.SLOT_UNDERWEAR:
			{
				pdollSlot = PAPERDOLL_UNDER;
				break;
			}
			case Item.SLOT_LR_HAND:
			{
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null); // this should be the same as in LRHAND
				pdollSlot = PAPERDOLL_LRHAND;
				break;
			}
		}
		if (pdollSlot >= 0)
		{
			setPaperdollItem(pdollSlot, null);
		}
	}
	
	/**
	 * Equips item and returns list of alterations
	 * @param item : ItemInstance corresponding to the item
	 * @return ItemInstance[] : list of alterations
	 */
	public ItemInstance[] equipItemAndRecord(ItemInstance item)
	{
		final ChangeRecorder recorder = newRecorder();
		
		try
		{
			equipItem(item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	/**
	 * Equips item in slot of paperdoll.
	 * @param item : ItemInstance designating the item and slot used.
	 */
	public synchronized void equipItem(ItemInstance item)
	{
		if ((getOwner() instanceof PlayerInstance) && (((PlayerInstance) getOwner()).getPrivateStoreType() != 0))
		{
			return;
		}
		
		if (getOwner() instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) getOwner();
			
			// Like L2OFF weapon hero and crown aren't removed after restart
			if (!player.isGM() && !player.isHero())
			{
				final int itemId = item.getItemId();
				if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842))
				{
					return;
				}
			}
		}
		
		final int targetSlot = item.getItem().getBodyPart();
		switch (targetSlot)
		{
			case Item.SLOT_LR_HAND:
			{
				if (setPaperdollItem(PAPERDOLL_LHAND, null) != null)
				{
					// exchange 2h for 2h
					setPaperdollItem(PAPERDOLL_RHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				setPaperdollItem(PAPERDOLL_RHAND, item);
				setPaperdollItem(PAPERDOLL_LRHAND, item);
				break;
			}
			case Item.SLOT_L_HAND:
			{
				if (!(item.getItem() instanceof EtcItem) || (item.getItem().getItemType() != EtcItemType.ARROW))
				{
					final ItemInstance old1 = setPaperdollItem(PAPERDOLL_LRHAND, null);
					if (old1 != null)
					{
						setPaperdollItem(PAPERDOLL_RHAND, null);
					}
				}
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_LHAND, item);
				break;
			}
			case Item.SLOT_R_HAND:
			{
				if (_paperdoll[PAPERDOLL_LRHAND] != null)
				{
					setPaperdollItem(PAPERDOLL_LRHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case Item.SLOT_L_EAR:
			case Item.SLOT_R_EAR:
			case Item.SLOT_L_EAR | Item.SLOT_R_EAR:
			{
				if (_paperdoll[PAPERDOLL_LEAR] == null)
				{
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				else if (_paperdoll[PAPERDOLL_REAR] == null)
				{
					setPaperdollItem(PAPERDOLL_REAR, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LEAR, null);
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				break;
			}
			case Item.SLOT_L_FINGER:
			case Item.SLOT_R_FINGER:
			case Item.SLOT_L_FINGER | Item.SLOT_R_FINGER:
			{
				if (_paperdoll[PAPERDOLL_LFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				else if (_paperdoll[PAPERDOLL_RFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LFINGER, null);
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				break;
			}
			case Item.SLOT_NECK:
			{
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			}
			case Item.SLOT_FULL_ARMOR:
			{
				setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			}
			case Item.SLOT_CHEST:
			{
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			}
			case Item.SLOT_LEGS:
			{
				// handle full armor
				final ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if ((chest != null) && (chest.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR))
				{
					setPaperdollItem(PAPERDOLL_CHEST, null);
				}
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_LEGS, item);
				break;
			}
			case Item.SLOT_FEET:
			{
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			}
			case Item.SLOT_GLOVES:
			{
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			}
			case Item.SLOT_HEAD:
			{
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			}
			case Item.SLOT_HAIR:
			{
				if (setPaperdollItem(PAPERDOLL_DHAIR, null) != null)
				{
					setPaperdollItem(PAPERDOLL_DHAIR, null);
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
				}
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			}
			case Item.SLOT_FACE:
			{
				if (setPaperdollItem(PAPERDOLL_DHAIR, null) != null)
				{
					setPaperdollItem(PAPERDOLL_DHAIR, null);
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				setPaperdollItem(PAPERDOLL_FACE, item);
				break;
			}
			case Item.SLOT_DHAIR:
			{
				if (setPaperdollItem(PAPERDOLL_HAIR, null) != null)
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				setPaperdollItem(PAPERDOLL_DHAIR, item);
				break;
			}
			case Item.SLOT_UNDERWEAR:
			{
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			}
			case Item.SLOT_BACK:
			{
				setPaperdollItem(PAPERDOLL_BACK, item);
				break;
			}
			default:
			{
				LOGGER.warning("unknown body slot:" + targetSlot);
			}
		}
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	protected void refreshWeight()
	{
		int weight = 0;
		for (ItemInstance item : _items)
		{
			if ((item != null) && (item.getItem() != null))
			{
				weight += item.getItem().getWeight() * item.getCount();
			}
		}
		
		_totalWeight = weight;
	}
	
	/**
	 * Returns the totalWeight.
	 * @return int
	 */
	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	/**
	 * Return the ItemInstance of the arrows needed for this bow.
	 * @param bow : Item designating the bow
	 * @return ItemInstance pointing out arrows for bow
	 */
	public ItemInstance findArrowForBow(Item bow)
	{
		if (bow == null)
		{
			return null;
		}
		
		// Check if char has the bow equiped
		if (bow.getItemType() != WeaponType.BOW)
		{
			return null;
		}
		
		int arrowsId = 0;
		
		switch (bow.getCrystalType())
		{
			default: // broken weapon.csv ??
			case Item.CRYSTAL_NONE:
			{
				arrowsId = 17;
				break; // Wooden arrow
			}
			case Item.CRYSTAL_D:
			{
				arrowsId = 1341;
				break; // Bone arrow
			}
			case Item.CRYSTAL_C:
			{
				arrowsId = 1342;
				break; // Fine steel arrow
			}
			case Item.CRYSTAL_B:
			{
				arrowsId = 1343;
				break; // Silver arrow
			}
			case Item.CRYSTAL_A:
			{
				arrowsId = 1344;
				break; // Mithril arrow
			}
			case Item.CRYSTAL_S:
			{
				arrowsId = 1345;
				break; // Shining arrow
			}
		}
		// Get the ItemInstance corresponding to the item identifier and return it
		return getItemByItemId(arrowsId);
	}
	
	/**
	 * Get back items in inventory from database
	 */
	@Override
	public void restore()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY object_id DESC");
			statement.setInt(1, getOwner().getObjectId());
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());
			final ResultSet inv = statement.executeQuery();
			ItemInstance item;
			
			while (inv.next())
			{
				final int objectId = inv.getInt(1);
				item = ItemInstance.restoreFromDb(objectId);
				if (item == null)
				{
					continue;
				}
				
				if (getOwner() instanceof PlayerInstance)
				{
					final PlayerInstance player = (PlayerInstance) getOwner();
					if (!player.isGM() && !player.isHero())
					{
						final int itemId = item.getItemId();
						if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842))
						{
							item.setLocation(ItemLocation.INVENTORY);
						}
					}
				}
				
				World.getInstance().storeObject(item);
				
				// If stackable item is found in inventory just add to current quantity
				if (item.isStackable() && (getItemByItemId(item.getItemId()) != null))
				{
					addItem("Restore", item, null, getOwner());
				}
				else
				{
					addItem(item);
				}
			}
			
			inv.close();
			statement.close();
			refreshWeight();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore inventory : " + e);
		}
	}
	
	/**
	 * Re-notify to paperdoll listeners every equipped item
	 */
	public void reloadEquippedItems()
	{
		ItemInstance item;
		int slot;
		for (ItemInstance element : _paperdoll)
		{
			item = element;
			if (item == null)
			{
				continue;
			}
			
			slot = item.getEquipSlot();
			for (PaperdollListener listener : _paperdollListeners)
			{
				if (listener == null)
				{
					continue;
				}
				
				listener.notifyUnequiped(slot, item);
				listener.notifyEquiped(slot, item);
			}
		}
	}
}
