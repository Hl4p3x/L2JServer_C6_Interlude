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
package org.l2jserver.gameserver.model.items.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.geoengine.GeoEngine;
import org.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jserver.gameserver.model.Augmentation;
import org.l2jserver.gameserver.model.DropProtection;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.knownlist.NullKnownList;
import org.l2jserver.gameserver.model.items.Armor;
import org.l2jserver.gameserver.model.items.EtcItem;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.skills.funcs.Func;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.IllegalPlayerAction;
import org.l2jserver.gameserver.util.Util;

/**
 * This class manages items.
 */
public class ItemInstance extends WorldObject
{
	private static final Logger LOGGER = Logger.getLogger(ItemInstance.class.getName());
	private static final Logger _logItems = Logger.getLogger("item");
	
	private final DropProtection _dropProtection = new DropProtection();
	
	/**
	 * Enumeration of locations for item.
	 */
	public enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		FREIGHT
	}
	
	private int _ownerId;
	private int _count;
	private int _initCount;
	private int _time;
	private boolean _decrease = false;
	private final int _itemId;
	private final Item _item;
	private ItemLocation _loc;
	private int _locData;
	private int _enchantLevel;
	private int _priceSell;
	private int _priceBuy;
	private boolean _wear;
	private Augmentation _augmentation = null;
	private int _mana = -1;
	private boolean _consumingMana = false;
	private static final int MANA_CONSUMPTION_RATE = 60000;
	private int _type1;
	private int _type2;
	private long _dropTime;
	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SOULSHOT = 2; // Does it real;y exist? ;-)
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
	private int _chargedSoulshot = CHARGED_NONE;
	private int _chargedSpiritshot = CHARGED_NONE;
	private boolean _chargedFishtshot = false;
	private boolean _protected;
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 3; // 1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	private ScheduledFuture<?> itemLootShedule = null;
	
	/**
	 * Constructor of the ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public ItemInstance(int objectId, int itemId)
	{
		this(objectId, ItemTable.getInstance().getTemplate(itemId));
	}
	
	/**
	 * Constructor of the ItemInstance from the objetId and the description of the item given by the Item.
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : Item containing informations of the item
	 */
	public ItemInstance(int objectId, Item item)
	{
		super(objectId);
		
		if (item == null)
		{
			throw new IllegalArgumentException();
		}
		
		super.setKnownList(new NullKnownList(this));
		
		_itemId = item.getItemId();
		_item = item;
		_count = 1;
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
	}
	
	/**
	 * Sets the ownerID of the item.
	 * @param process : String Identifier of process triggering this action
	 * @param ownerId : int designating the ID of the owner
	 * @param creator : PlayerInstance Player requesting the item creation
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(String process, int ownerId, PlayerInstance creator, WorldObject reference)
	{
		setOwnerId(ownerId);
	}
	
	/**
	 * Sets the ownerID of the item.
	 * @param ownerId : int designating the ID of the owner
	 */
	public void setOwnerId(int ownerId)
	{
		if (ownerId == _ownerId)
		{
			return;
		}
		
		_ownerId = ownerId;
		_storedInDb = false;
	}
	
	/**
	 * Returns the ownerID of the item.
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	/**
	 * Sets the location of the item.
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}
	
	/**
	 * Sets the location of the item.<br>
	 * <u><i>Remark :</i></u> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc : ItemLocation (enumeration)
	 * @param locData : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int locData)
	{
		if ((loc == _loc) && (locData == _locData))
		{
			return;
		}
		_loc = loc;
		_locData = locData;
		_storedInDb = false;
	}
	
	/**
	 * Gets the item location.
	 * @return the item location
	 */
	public ItemLocation getItemLocation()
	{
		return _loc;
	}
	
	public boolean isPotion()
	{
		return _item.isPotion();
	}
	
	/**
	 * Returns the quantity of item.
	 * @return int
	 */
	public int getCount()
	{
		return _count;
	}
	
	/**
	 * Sets the quantity of the item.<br>
	 * <u><i>Remark :</i></u> If loc and loc_data different from database, say datas not up-to-date
	 * @param process : String Identifier of process triggering this action
	 * @param count : int
	 * @param creator : PlayerInstance Player requesting the item creation
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(String process, int count, PlayerInstance creator, WorldObject reference)
	{
		if (count == 0)
		{
			return;
		}
		
		if ((count > 0) && (_count > (Integer.MAX_VALUE - count)))
		{
			_count = Integer.MAX_VALUE;
		}
		else
		{
			_count += count;
		}
		
		if (_count < 0)
		{
			_count = 0;
		}
		
		_storedInDb = false;
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				this,
				creator,
				reference
			});
			_logItems.log(record);
		}
	}
	
	// No logging (function designed for shots only)
	/**
	 * Change count without trace.
	 * @param process the process
	 * @param count the count
	 * @param creator the creator
	 * @param reference the reference
	 */
	public void changeCountWithoutTrace(String process, int count, PlayerInstance creator, WorldObject reference)
	{
		if (count == 0)
		{
			return;
		}
		if ((count > 0) && (_count > (Integer.MAX_VALUE - count)))
		{
			_count = Integer.MAX_VALUE;
		}
		else
		{
			_count += count;
		}
		if (_count < 0)
		{
			_count = 0;
		}
		
		_storedInDb = false;
	}
	
	/**
	 * Sets the quantity of the item.<br>
	 * <u><i>Remark :</i></u> If loc and loc_data different from database, say datas not up-to-date
	 * @param count : int
	 */
	public void setCount(int count)
	{
		if (_count == count)
		{
			return;
		}
		
		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}
	
	/**
	 * Returns if item is equipable.
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return ((_item.getBodyPart() != 0) && !(_item instanceof EtcItem));
	}
	
	/**
	 * Returns if item is equipped.
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP);
	}
	
	/**
	 * Returns the slot where the item is stored.
	 * @return int
	 */
	public int getEquipSlot()
	{
		return _locData;
	}
	
	/**
	 * Returns the characteristics of the item.
	 * @return Item
	 */
	public Item getItem()
	{
		return _item;
	}
	
	/**
	 * Gets the custom type1.
	 * @return the custom type1
	 */
	public int getCustomType1()
	{
		return _type1;
	}
	
	/**
	 * Gets the custom type2.
	 * @return the custom type2
	 */
	public int getCustomType2()
	{
		return _type2;
	}
	
	/**
	 * Sets the custom type1.
	 * @param newtype the new custom type1
	 */
	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}
	
	/**
	 * Sets the custom type2.
	 * @param newtype the new custom type2
	 */
	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}
	
	/**
	 * Sets the drop time.
	 * @param time the new drop time
	 */
	public void setDropTime(long time)
	{
		_dropTime = time;
	}
	
	/**
	 * Gets the drop time.
	 * @return the drop time
	 */
	public long getDropTime()
	{
		return _dropTime;
	}
	
	/**
	 * Checks if is cupid bow.
	 * @return true, if is cupid bow
	 */
	public boolean isCupidBow()
	{
		return (_itemId == 9140) || (_itemId == 9141);
	}
	
	/**
	 * Checks if is wear.
	 * @return true, if is wear
	 */
	public boolean isWear()
	{
		return _wear;
	}
	
	/**
	 * Sets the wear.
	 * @param newwear the new wear
	 */
	public void setWear(boolean newwear)
	{
		_wear = newwear;
	}
	
	/**
	 * Returns the type of item.
	 * @return Enum
	 */
	public Enum<?> getItemType()
	{
		return _item.getItemType();
	}
	
	/**
	 * Returns the ID of the item.
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization.
	 * @return int
	 */
	public int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}
	
	/**
	 * Returns the reference price of the item.
	 * @return int
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}
	
	/**
	 * Returns the name of the item.
	 * @return String
	 */
	public String getItemName()
	{
		return _item.getName();
	}
	
	/**
	 * Returns the price of the item for selling.
	 * @return int
	 */
	public int getPriceToSell()
	{
		return _item.isConsumable() ? (int) (_priceSell * Config.RATE_CONSUMABLE_COST) : _priceSell;
	}
	
	/**
	 * Sets the price of the item for selling <u><i>Remark :</i></u> If loc and loc_data different from database, say datas not up-to-date.
	 * @param price : int designating the price
	 */
	public void setPriceToSell(int price)
	{
		_priceSell = price;
		_storedInDb = false;
	}
	
	/**
	 * Returns the last change of the item.
	 * @return int
	 */
	public int getLastChange()
	{
		return _lastChange;
	}
	
	/**
	 * Sets the last change of the item.
	 * @param lastChange : int
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}
	
	/**
	 * Returns if item is stackable.
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	/**
	 * Returns if item is dropable.
	 * @return boolean
	 */
	public boolean isDropable()
	{
		return !isAugmented() && _item.isDropable();
	}
	
	/**
	 * Returns if item is destroyable.
	 * @return boolean
	 */
	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}
	
	/**
	 * Returns if item is tradeable.
	 * @return boolean
	 */
	public boolean isTradeable()
	{
		return !isAugmented() && _item.isTradeable();
	}
	
	/**
	 * Returns if item is consumable.
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}
	
	/**
	 * Returns if item is available for manipulation.
	 * @param player the player
	 * @param allowAdena the allow adena
	 * @param allowEquipped
	 * @return boolean
	 */
	public boolean isAvailable(PlayerInstance player, boolean allowAdena, boolean allowEquipped)
	{
		return (!isEquipped() || allowEquipped) && (_item.getType2() != Item.TYPE2_QUEST) && ((_item.getType2() != Item.TYPE2_MONEY) || (_item.getType1() != Item.TYPE1_SHIELD_ARMOR)) // TODO: what does this mean?
			&& ((player.getPet() == null) || (getObjectId() != player.getPet().getControlItemId())) // Not Control item of currently summoned pet
			&& (player.getActiveEnchantItem() != this) && (allowAdena || (_itemId != 57)) && ((player.getCurrentSkill() == null) || (player.getCurrentSkill().getSkill().getItemConsumeId() != _itemId)) && isTradeable();
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		// this causes the validate position handler to do the pickup if the location is reached.
		// mercenary tickets can only be picked up by the castle owner and GMs.
		if ((!player.isGM()) && (((_itemId >= 3960) && (_itemId <= 4021) && player.isInParty()) || ((_itemId >= 3960) && (_itemId <= 3969) && !player.isCastleLord(1)) || ((_itemId >= 3973) && (_itemId <= 3982) && !player.isCastleLord(2)) || ((_itemId >= 3986) && (_itemId <= 3995) && !player.isCastleLord(3)) || ((_itemId >= 3999) && (_itemId <= 4008) && !player.isCastleLord(4)) || ((_itemId >= 4012) && (_itemId <= 4021) && !player.isCastleLord(5)) || ((_itemId >= 5205) && (_itemId <= 5214) && !player.isCastleLord(6)) || ((_itemId >= 6779) && (_itemId <= 6788) && !player.isCastleLord(7)) || ((_itemId >= 7973) && (_itemId <= 7982) && !player.isCastleLord(8)) || ((_itemId >= 7918) && (_itemId <= 7927) && !player.isCastleLord(9))))
		{
			if (player.isInParty())
			{
				player.sendMessage("You cannot pickup mercenaries while in a party.");
			}
			else
			{
				player.sendMessage("Only the castle lord can pickup mercenaries.");
			}
			
			player.setTarget(this);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (player.getFreight().getItemByObjectId(getObjectId()) != null)
		{
			player.setTarget(this);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to pickup Freight Items", IllegalPlayerAction.PUNISH_KICK);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
		}
	}
	
	/**
	 * Returns the level of enchantment of the item.
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	/**
	 * Sets the level of enchantment of the item.
	 * @param enchantLevel the new enchant level
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
		{
			return;
		}
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}
	
	/**
	 * Returns the physical defense of the item.
	 * @return int
	 */
	public int getPDef()
	{
		if (_item instanceof Armor)
		{
			return ((Armor) _item).getPDef();
		}
		return 0;
	}
	
	/**
	 * Returns whether this item is augmented or not.
	 * @return true if augmented
	 */
	public boolean isAugmented()
	{
		return _augmentation != null;
	}
	
	/**
	 * Returns the augmentation object for this item.
	 * @return augmentation
	 */
	public Augmentation getAugmentation()
	{
		return _augmentation;
	}
	
	/**
	 * Sets a new augmentation.
	 * @param augmentation the augmentation
	 * @return return true if sucessfull
	 */
	public boolean setAugmentation(Augmentation augmentation)
	{
		// there shall be no previous augmentation.
		if (_augmentation != null)
		{
			return false;
		}
		_augmentation = augmentation;
		return true;
	}
	
	/**
	 * Remove the augmentation.
	 */
	public void removeAugmentation()
	{
		if (_augmentation == null)
		{
			return;
		}
		_augmentation.deleteAugmentationData();
		_augmentation = null;
	}
	
	/**
	 * Used to decrease mana (mana means life time for shadow items).
	 */
	public class ScheduleConsumeManaTask implements Runnable
	{
		private final ItemInstance _shadowItem;
		
		/**
		 * Instantiates a new schedule consume mana task.
		 * @param item the item
		 */
		public ScheduleConsumeManaTask(ItemInstance item)
		{
			_shadowItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				// decrease mana
				if (_shadowItem != null)
				{
					_shadowItem.decreaseMana(true);
				}
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	/**
	 * Returns true if this item is a shadow item Shadow items have a limited life-time.
	 * @return true, if is shadow item
	 */
	public boolean isShadowItem()
	{
		return _mana >= 0;
	}
	
	/**
	 * Sets the mana for this shadow item <b>NOTE</b>: does not send an inventory update packet.
	 * @param mana the new mana
	 */
	public void setMana(int mana)
	{
		_mana = mana;
	}
	
	/**
	 * Returns the remaining mana of this shadow item.
	 * @return lifeTime
	 */
	public int getMana()
	{
		return _mana;
	}
	
	/**
	 * Decreases the mana of this shadow item, sends a inventory update schedules a new consumption task if non is running optionally one could force a new task.
	 * @param resetConsumingMana the reset consuming mana
	 */
	public void decreaseMana(boolean resetConsumingMana)
	{
		if (!isShadowItem())
		{
			return;
		}
		
		if (_mana > 0)
		{
			_mana--;
		}
		
		if (_storedInDb)
		{
			_storedInDb = false;
		}
		if (resetConsumingMana)
		{
			_consumingMana = false;
		}
		
		final PlayerInstance player = (PlayerInstance) World.getInstance().findObject(getOwnerId());
		if (player != null)
		{
			SystemMessage sm;
			switch (_mana)
			{
				case 10:
				{
					sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_10);
					sm.addString(_item.getName());
					player.sendPacket(sm);
					break;
				}
				case 5:
				{
					sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_5);
					sm.addString(_item.getName());
					player.sendPacket(sm);
					break;
				}
				case 1:
				{
					sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_1_IT_WILL_DISAPPEAR_SOON);
					sm.addString(_item.getName());
					player.sendPacket(sm);
					break;
				}
			}
			
			if (_mana == 0) // The life time has expired
			{
				sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED);
				sm.addString(_item.getName());
				player.sendPacket(sm);
				
				// unequip
				if (isEquipped())
				{
					final ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getEquipSlot());
					final InventoryUpdate iu = new InventoryUpdate();
					
					for (ItemInstance element : unequiped)
					{
						player.checkSSMatch(null, element);
						iu.addModifiedItem(element);
					}
					
					player.sendPacket(iu);
				}
				
				if (_loc != ItemLocation.WAREHOUSE)
				{
					// destroy
					player.getInventory().destroyItem("ItemInstance", this, player, null);
					
					// send update
					final InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);
					
					final StatusUpdate su = new StatusUpdate(player.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
				}
				else
				{
					player.getWarehouse().destroyItem("ItemInstance", this, player, null);
				}
				
				// delete from world
				World.getInstance().removeObject(this);
			}
			else
			{
				// Reschedule if still equipped
				if (!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				
				if (_loc != ItemLocation.WAREHOUSE)
				{
					final InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}
	
	/**
	 * Schedule consume mana task.
	 */
	private void scheduleConsumeManaTask()
	{
		_consumingMana = true;
		ThreadPool.schedule(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}
	
	/**
	 * Returns false cause item can't be attacked.
	 * @param attacker the attacker
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	/**
	 * Returns the type of charge with SoulShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}
	
	/**
	 * Returns the type of charge with SpiritShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}
	
	/**
	 * Gets the charged fishshot.
	 * @return the charged fishshot
	 */
	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}
	
	/**
	 * Sets the type of charge with SoulShot of the item.
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}
	
	/**
	 * Sets the type of charge with SpiritShot of the item.
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}
	
	/**
	 * Sets the charged fishshot.
	 * @param type the new charged fishshot
	 */
	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}
	
	/**
	 * This function basically returns a set of functions from Item/Armor/Weapon, but may add additional functions, if this particular item instance is enhanched for a particular player.
	 * @param creature : Creature designating the player
	 * @return Func[]
	 */
	public Func[] getStatFuncs(Creature creature)
	{
		return _item.getStatFuncs(this, creature);
	}
	
	/**
	 * Updates database.<br>
	 * <u><i>Concept:</i></u><br>
	 * <b>IF</b> the item exists in database :
	 * <ul>
	 * <li><b>IF</b> the item has no owner, or has no location, or has a null quantity : remove item from database</li>
	 * <li><b>ELSE</b> : update item in database</li>
	 * </ul>
	 * <b> Otherwise</b> :
	 * <ul>
	 * <li><b>IF</b> the item hasn't a null quantity, and has a correct location, and has a correct owner : insert item in database</li>
	 * </ul>
	 */
	public void updateDatabase()
	{
		if (_wear)
		{
			return;
		}
		
		if (_existsInDb)
		{
			if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || ((_count == 0) && (_loc != ItemLocation.LEASE)))
			{
				removeFromDb();
			}
			else
			{
				updateInDb();
			}
		}
		else
		{
			if ((_count == 0) && (_loc != ItemLocation.LEASE))
			{
				return;
			}
			
			if ((_loc == ItemLocation.VOID) || (_ownerId == 0))
			{
				return;
			}
			
			insertIntoDb();
		}
	}
	
	/**
	 * Returns a ItemInstance stored in database from its objectID.
	 * @param objectId : int designating the objectID of the item
	 * @return ItemInstance
	 */
	public static ItemInstance restoreFromDb(int objectId)
	{
		ItemInstance inst = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2, mana_left FROM items WHERE object_id = ?");
			statement.setInt(1, objectId);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				final int owner_id = rs.getInt("owner_id");
				final int item_id = rs.getInt("item_id");
				final int count = rs.getInt("count");
				final ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));
				final int loc_data = rs.getInt("loc_data");
				final int enchant_level = rs.getInt("enchant_level");
				final int custom_type1 = rs.getInt("custom_type1");
				final int custom_type2 = rs.getInt("custom_type2");
				final int price_sell = rs.getInt("price_sell");
				final int price_buy = rs.getInt("price_buy");
				final int manaLeft = rs.getInt("mana_left");
				final Item item = ItemTable.getInstance().getTemplate(item_id);
				if (item == null)
				{
					LOGGER.warning("Item item_id=" + item_id + " not known, object_id=" + objectId);
					rs.close();
					statement.close();
					con.close();
					return null;
				}
				
				inst = new ItemInstance(objectId, item);
				inst._existsInDb = true;
				inst._storedInDb = true;
				inst._ownerId = owner_id;
				inst._count = count;
				inst._enchantLevel = enchant_level;
				inst._type1 = custom_type1;
				inst._type2 = custom_type2;
				inst._loc = loc;
				inst._locData = loc_data;
				inst._priceSell = price_sell;
				inst._priceBuy = price_buy;
				
				// Setup life time for shadow weapons
				inst._mana = manaLeft;
				
				// consume 1 mana
				if ((inst._mana > 0) && (inst.getItemLocation() == ItemLocation.PAPERDOLL))
				{
					inst.decreaseMana(false);
				}
				
				// if mana left is 0 delete this item
				if (inst._mana == 0)
				{
					inst.removeFromDb();
					rs.close();
					statement.close();
					con.close();
					return null;
				}
				else if ((inst._mana > 0) && (inst.getItemLocation() == ItemLocation.PAPERDOLL))
				{
					inst.scheduleConsumeManaTask();
				}
			}
			else
			{
				LOGGER.warning("Item object_id=" + objectId + " not found");
				rs.close();
				statement.close();
				con.close();
				return null;
			}
			
			rs.close();
			statement.close();
			
			// load augmentation
			statement = con.prepareStatement("SELECT attributes,skill,level FROM augmentations WHERE item_id=?");
			statement.setInt(1, objectId);
			rs = statement.executeQuery();
			if (rs.next())
			{
				inst._augmentation = new Augmentation(inst, rs.getInt("attributes"), rs.getInt("skill"), rs.getInt("level"), false);
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore item " + objectId + " from DB " + e);
		}
		
		return inst;
	}
	
	/**
	 * Init a dropped ItemInstance and add it in the world as a visible object.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the x,y,z position of the ItemInstance dropped and update its _worldregion</li>
	 * <li>Add the ItemInstance dropped to _visibleObjects of its WorldRegion</li>
	 * <li>Add the ItemInstance dropped in the world as a <b>visible</b> object</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T ADD the object to _allObjects of World </b></font><br>
	 * <br>
	 * <b><u>Assert</u>:</b><br>
	 * <li>_worldRegion == null <i>(WorldObject is invisible at the beginning)</i></li><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Drop item</li>
	 * <li>Call Pet</li><br>
	 * @param dropper the dropper
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void dropMe(Creature dropper, int x, int y, int z)
	{
		if (Config.PATHFINDING && (dropper != null))
		{
			final Location dropDest = GeoEngine.getInstance().canMoveToTargetLoc(dropper.getX(), dropper.getY(), dropper.getZ(), x, y, z, dropper.getInstanceId());
			if ((dropDest != null) && (dropDest.getX() != 0) && (dropDest.getY() != 0))
			{
				x = dropDest.getX();
				y = dropDest.getY();
				z = dropDest.getZ();
			}
		}
		
		synchronized (this)
		{
			// Set the x,y,z position of the ItemInstance dropped and update its _worldregion
			setVisible(true);
			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(World.getInstance().getRegion(getPosition().getWorldPosition()));
			
			// Add the ItemInstance dropped to _visibleObjects of its WorldRegion
			getPosition().getWorldRegion().addVisibleObject(this);
		}
		
		setDropTime(System.currentTimeMillis());
		
		// this can synchronize on others instancies, so it's out of synchronized, to avoid deadlocks
		// Add the ItemInstance dropped in the world as a visible object
		World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), dropper);
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().save(this);
		}
	}
	
	/**
	 * Update the database with values of the item.
	 */
	private void updateInDb()
	{
		if (_wear)
		{
			return;
		}
		
		if (_storedInDb)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=?,mana_left=? WHERE object_id = ?");
			statement.setInt(1, _ownerId);
			statement.setInt(2, _count);
			statement.setString(3, _loc.name());
			statement.setInt(4, _locData);
			statement.setInt(5, _enchantLevel);
			statement.setInt(6, _priceSell);
			statement.setInt(7, _priceBuy);
			statement.setInt(8, _type1);
			statement.setInt(9, _type2);
			statement.setInt(10, _mana);
			statement.setInt(11, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not update item " + getObjectId() + " in DB: Reason: " + e);
		}
	}
	
	/**
	 * Insert the item in database.
	 */
	private void insertIntoDb()
	{
		if (_wear)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,price_sell,price_buy,object_id,custom_type1,custom_type2,mana_left) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _ownerId);
			statement.setInt(2, _itemId);
			statement.setInt(3, _count);
			statement.setString(4, _loc.name());
			statement.setInt(5, _locData);
			statement.setInt(6, _enchantLevel);
			statement.setInt(7, _priceSell);
			statement.setInt(8, _priceBuy);
			statement.setInt(9, getObjectId());
			statement.setInt(10, _type1);
			statement.setInt(11, _type2);
			statement.setInt(12, _mana);
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();
		}
		catch (Exception e)
		{
			updateInDb();
		}
	}
	
	/**
	 * Delete item from database.
	 */
	private void removeFromDb()
	{
		if (_wear)
		{
			return;
		}
		
		// delete augmentation data
		if (isAugmented())
		{
			_augmentation.deleteAugmentationData();
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not delete item " + getObjectId() + " in DB: " + e);
		}
	}
	
	/**
	 * Returns the item in String format.
	 * @return String
	 */
	@Override
	public String toString()
	{
		return "" + _item;
	}
	
	/**
	 * Reset owner timer.
	 */
	public void resetOwnerTimer()
	{
		if (itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
	}
	
	/**
	 * Sets the item loot shedule.
	 * @param sf the new item loot shedule
	 */
	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}
	
	/**
	 * Gets the item loot shedule.
	 * @return the item loot shedule
	 */
	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}
	
	/**
	 * Sets the protected.
	 * @param isProtected the new protected
	 */
	public void setProtected(boolean isProtected)
	{
		_protected = isProtected;
	}
	
	/**
	 * Checks if is protected.
	 * @return true, if is protected
	 */
	public boolean isProtected()
	{
		return _protected;
	}
	
	/**
	 * Checks if is night lure.
	 * @return true, if is night lure
	 */
	public boolean isNightLure()
	{
		return ((_itemId >= 8505) && (_itemId <= 8513)) || (_itemId == 8485);
	}
	
	/**
	 * Sets the count decrease.
	 * @param decrease the new count decrease
	 */
	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}
	
	/**
	 * Gets the count decrease.
	 * @return the count decrease
	 */
	public boolean getCountDecrease()
	{
		return _decrease;
	}
	
	/**
	 * Sets the inits the count.
	 * @param initCount the new inits the count
	 */
	public void setInitCount(int initCount)
	{
		_initCount = initCount;
	}
	
	/**
	 * Gets the inits the count.
	 * @return the inits the count
	 */
	public int getInitCount()
	{
		return _initCount;
	}
	
	/**
	 * Restore init count.
	 */
	public void restoreInitCount()
	{
		if (_decrease)
		{
			_count = _initCount;
		}
	}
	
	/**
	 * Sets the time.
	 * @param time the new time
	 */
	public void setTime(int time)
	{
		if (time > 0)
		{
			_time = time;
		}
		else
		{
			_time = 0;
		}
	}
	
	/**
	 * Gets the time.
	 * @return the time
	 */
	public int getTime()
	{
		return _time;
	}
	
	/**
	 * Returns the slot where the item is stored.
	 * @return int
	 */
	public int getLocationSlot()
	{
		return _locData;
	}
	
	/**
	 * Gets the drop protection.
	 * @return the drop protection
	 */
	public DropProtection getDropProtection()
	{
		return _dropProtection;
	}
	
	/**
	 * Checks if is varka ketra ally quest item.
	 * @return true, if is varka ketra ally quest item
	 */
	public boolean isVarkaKetraAllyQuestItem()
	{
		return ((_itemId >= 7211) && (_itemId <= 7215)) || ((_itemId >= 7221) && (_itemId <= 7225));
	}
	
	public boolean isOlyRestrictedItem()
	{
		return (Config.LIST_OLY_RESTRICTED_ITEMS.contains(_itemId));
	}
	
	public boolean isHeroItem()
	{
		return (((_itemId >= 6611) && (_itemId <= 6621)) || ((_itemId >= 9388) && (_itemId <= 9390)) || (_itemId == 6842));
	}
	
	public boolean checkOlympCondition()
	{
		return !isHeroItem() && !isOlyRestrictedItem() && !_wear && (Config.ALT_OLY_AUGMENT_ALLOW || !isAugmented());
	}
	
	/**
	 * Returns true if item is a Weapon/Shield
	 * @return boolean
	 */
	public boolean isWeapon()
	{
		return (_item instanceof Weapon);
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}
}
