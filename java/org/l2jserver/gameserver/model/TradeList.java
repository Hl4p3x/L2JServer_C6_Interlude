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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.OfflineTradeTable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.EtcItemType;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

/**
 * @author Advi
 */
public class TradeList
{
	private static final Logger LOGGER = Logger.getLogger(TradeList.class.getName());
	
	public class TradeItem
	{
		private int _objectId;
		private final Item _item;
		private int _enchant;
		private int _count;
		private int _price;
		private int _curcount;
		
		/** Augmented Item */
		private final Augmentation _augmentation = null;
		
		public TradeItem(ItemInstance item, int count, int price)
		{
			_objectId = item.getObjectId();
			_item = item.getItem();
			_enchant = item.getEnchantLevel();
			_count = count;
			_price = price;
		}
		
		public TradeItem(Item item, int count, int price)
		{
			_objectId = 0;
			_item = item;
			_enchant = 0;
			_count = count;
			_price = price;
		}
		
		public TradeItem(TradeItem item, int count, int price)
		{
			_objectId = item.getObjectId();
			_item = item.getItem();
			_enchant = item.getEnchant();
			_count = count;
			_price = price;
		}
		
		public void setObjectId(int objectId)
		{
			_objectId = objectId;
		}
		
		public int getObjectId()
		{
			return _objectId;
		}
		
		public Item getItem()
		{
			return _item;
		}
		
		public void setEnchant(int enchant)
		{
			_enchant = enchant;
		}
		
		public int getEnchant()
		{
			return _enchant;
		}
		
		public void setCount(int count)
		{
			_count = count;
		}
		
		public int getCount()
		{
			return _count;
		}
		
		public void setPrice(int price)
		{
			_price = price;
		}
		
		public int getPrice()
		{
			return _price;
		}
		
		public void setCurCount(int count)
		{
			_curcount = count;
		}
		
		public int getCurCount()
		{
			return _curcount;
		}
		
		/**
		 * Returns whether this item is augmented or not
		 * @return true if augmented
		 */
		public boolean isAugmented()
		{
			return _augmentation != null;
		}
	}
	
	private final PlayerInstance _owner;
	private PlayerInstance _partner;
	private final List<TradeItem> _items;
	private String _title;
	private boolean _packaged;
	
	private boolean _confirmed = false;
	private boolean _locked = false;
	
	public TradeList(PlayerInstance owner)
	{
		_items = new ArrayList<>();
		_owner = owner;
	}
	
	public PlayerInstance getOwner()
	{
		return _owner;
	}
	
	public void setPartner(PlayerInstance partner)
	{
		_partner = partner;
	}
	
	public PlayerInstance getPartner()
	{
		return _partner;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isLocked()
	{
		return _locked;
	}
	
	public boolean isConfirmed()
	{
		return _confirmed;
	}
	
	public boolean isPackaged()
	{
		return _packaged;
	}
	
	public void setPackaged(boolean value)
	{
		_packaged = value;
	}
	
	/**
	 * Retrieves items from TradeList
	 * @return
	 */
	public TradeItem[] getItems()
	{
		return _items.toArray(new TradeItem[_items.size()]);
	}
	
	/**
	 * Returns the list of items in inventory available for transaction
	 * @param inventory
	 * @return ItemInstance : items in inventory
	 */
	public TradeItem[] getAvailableItems(PlayerInventory inventory)
	{
		final List<TradeItem> list = new ArrayList<>();
		for (TradeItem item : _items)
		{
			item = new TradeItem(item, item.getCount(), item.getPrice());
			list.add(inventory.adjustAvailableItem(item, list));
		}
		return list.toArray(new TradeItem[list.size()]);
	}
	
	/**
	 * Returns Item List size
	 * @return
	 */
	public int getItemCount()
	{
		return _items.size();
	}
	
	/**
	 * Adjust available item from Inventory by the one in this list
	 * @param item : ItemInstance to be adjusted
	 * @return TradeItem representing adjusted item
	 */
	public TradeItem adjustAvailableItem(ItemInstance item)
	{
		if (item.isStackable())
		{
			for (TradeItem exclItem : _items)
			{
				if ((exclItem.getItem().getItemId() == item.getItemId()) && (exclItem.getEnchant() == item.getEnchantLevel()))
				{
					if (item.getCount() <= exclItem.getCount())
					{
						return null;
					}
					return new TradeItem(item, item.getCount() - exclItem.getCount(), item.getReferencePrice());
				}
			}
		}
		return new TradeItem(item, item.getCount(), item.getReferencePrice());
	}
	
	/**
	 * Adjust ItemRequest by corresponding item in this list using its <b>ObjectId</b>
	 * @param item : ItemRequest to be adjusted
	 */
	public void adjustItemRequest(ItemRequest item)
	{
		for (TradeItem filtItem : _items)
		{
			if ((filtItem.getObjectId() == item.getObjectId()) && (filtItem.getEnchant() == item.getEnchant()))
			{
				if (filtItem.getCount() < item.getCount())
				{
					item.setCount(filtItem.getCount());
				}
				
				return;
			}
		}
		
		item.setCount(0);
	}
	
	/**
	 * Adjust ItemRequest by corresponding item in this list using its <b>ItemId</b>
	 * @param item : ItemRequest to be adjusted
	 */
	public void adjustItemRequestByItemId(ItemRequest item)
	{
		for (TradeItem filtItem : _items)
		{
			if ((filtItem.getItem().getItemId() == item.getItemId()) && (filtItem.getEnchant() == item.getEnchant()))
			{
				if (filtItem.getCount() < item.getCount())
				{
					item.setCount(filtItem.getCount());
				}
				
				return;
			}
		}
		
		item.setCount(0);
	}
	
	/**
	 * Add simplified item to TradeList
	 * @param objectId : int
	 * @param count : int
	 * @return
	 */
	public synchronized TradeItem addItem(int objectId, int count)
	{
		return addItem(objectId, count, 0);
	}
	
	/**
	 * Add item to TradeList
	 * @param objectId : int
	 * @param count : int
	 * @param price : int
	 * @return
	 */
	public synchronized TradeItem addItem(int objectId, int count, int price)
	{
		if (_locked)
		{
			Util.handleIllegalPlayerAction(_owner, "Player " + _owner.getName() + " Attempt to modify locked TradeList! ", Config.DEFAULT_PUNISH);
			LOGGER.warning(_owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		
		final WorldObject o = World.getInstance().findObject(objectId);
		if (!(o instanceof ItemInstance))
		{
			Util.handleIllegalPlayerAction(_owner, "Player " + _owner.getName() + " Attempt to add invalid item to TradeList! ", Config.DEFAULT_PUNISH);
			LOGGER.warning(_owner.getName() + ": Attempt to add invalid item to TradeList!");
			return null;
		}
		
		if (!_owner.validateItemManipulation(objectId, "Modify TradeList"))
		{
			Util.handleIllegalPlayerAction(_owner, "Player " + _owner.getName() + " Attempt to modify TradeList without valid conditions! ", Config.DEFAULT_PUNISH);
			LOGGER.warning(_owner.getName() + ": Attempt to modify TradeList without valid conditions!");
			return null;
		}
		
		final ItemInstance item = (ItemInstance) o;
		if (!item.isTradeable() || (item.getItemType() == EtcItemType.QUEST))
		{
			return null;
		}
		
		// GM items trade restriction (valid for trade and private sell)
		if ((_owner.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS))
		{
			return null;
		}
		
		if (count > item.getCount())
		{
			return null;
		}
		
		if (!item.isStackable() && (count > 1))
		{
			LOGGER.warning(_owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
			return null;
		}
		
		for (TradeItem checkitem : _items)
		{
			if (checkitem.getObjectId() == objectId)
			{
				return null;
			}
		}
		
		final TradeItem titem = new TradeItem(item, count, price);
		_items.add(titem);
		
		// If Player has already confirmed this trade, invalidate the confirmation
		invalidateConfirmation();
		
		return titem;
	}
	
	/**
	 * Add item to TradeList
	 * @param itemId : int
	 * @param count : int
	 * @param price : int
	 * @param enchant
	 * @return
	 */
	public synchronized TradeItem addItemByItemId(int itemId, int count, int price, int enchant)
	{
		if (_locked)
		{
			Util.handleIllegalPlayerAction(_owner, "Player " + _owner.getName() + " Attempt to modify locked TradeList! Banned ", Config.DEFAULT_PUNISH);
			LOGGER.warning(_owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		
		if (!_owner.validateItemManipulationByItemId(itemId, "Modify TradeList"))
		{
			Util.handleIllegalPlayerAction(_owner, "Player " + _owner.getName() + " Attempt to modify TradeList without valid conditions! ", Config.DEFAULT_PUNISH);
			LOGGER.warning(_owner.getName() + ": Attempt to modify TradeList without valid conditions!");
			return null;
		}
		
		final Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			Util.handleIllegalPlayerAction(_owner, "Player " + _owner.getName() + " Attempt to add invalid item to TradeList! Banned ", Config.DEFAULT_PUNISH);
			LOGGER.warning(_owner.getName() + ": Attempt to add invalid item to TradeList!");
			return null;
		}
		
		if (!item.isTradeable() || (item.getItemType() == EtcItemType.QUEST))
		{
			return null;
		}
		
		if (!item.isStackable() && (count > 1))
		{
			LOGGER.warning(_owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
			return null;
		}
		
		final TradeItem titem = new TradeItem(item, count, price);
		titem.setEnchant(enchant);
		_items.add(titem);
		
		// If Player has already confirmed this trade, invalidate the confirmation
		invalidateConfirmation();
		
		return titem;
	}
	
	/**
	 * Remove item from TradeList
	 * @param objectId : int
	 * @param itemId
	 * @param count : int
	 * @return
	 */
	public synchronized TradeItem removeItem(int objectId, int itemId, int count)
	{
		if (_locked)
		{
			Util.handleIllegalPlayerAction(_owner, "Player " + _owner.getName() + " Attempt to modify locked TradeList! Banned ", Config.DEFAULT_PUNISH);
			LOGGER.warning(_owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		
		for (TradeItem titem : _items)
		{
			if ((titem.getObjectId() == objectId) || (titem.getItem().getItemId() == itemId))
			{
				// If Partner has already confirmed this trade, invalidate the confirmation
				if (_partner != null)
				{
					final TradeList partnerList = _partner.getActiveTradeList();
					if (partnerList == null)
					{
						LOGGER.warning(_partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
						return null;
					}
					partnerList.invalidateConfirmation();
				}
				
				// Reduce item count or complete item
				if ((count != -1) && (titem.getCount() > count))
				{
					titem.setCount(titem.getCount() - count);
				}
				else
				{
					_items.remove(titem);
				}
				
				return titem;
			}
		}
		
		return null;
	}
	
	/**
	 * Update items in TradeList according their quantity in owner inventory
	 */
	public synchronized void updateItems()
	{
		for (TradeItem titem : _items)
		{
			final ItemInstance item = _owner.getInventory().getItemByObjectId(titem.getObjectId());
			if ((item == null) || (titem.getCount() < 1))
			{
				removeItem(titem.getObjectId(), -1, -1);
			}
			else if (item.getCount() < titem.getCount())
			{
				titem.setCount(item.getCount());
			}
		}
	}
	
	/**
	 * Lockes TradeList, no further changes are allowed
	 */
	public void lock()
	{
		_locked = true;
	}
	
	/**
	 * Clears item list
	 */
	public void clear()
	{
		_items.clear();
		_locked = false;
	}
	
	/**
	 * Confirms TradeList
	 * @return : boolean
	 */
	public boolean confirm()
	{
		if (_confirmed)
		{
			return true; // Already confirmed
		}
		
		// If Partner has already confirmed this trade, proceed exchange
		if (_partner != null)
		{
			final TradeList partnerList = _partner.getActiveTradeList();
			if (partnerList == null)
			{
				LOGGER.warning(_partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
				return false;
			}
			
			// Synchronization order to avoid deadlock
			TradeList sync1;
			TradeList sync2;
			if (getOwner().getObjectId() > partnerList.getOwner().getObjectId())
			{
				sync1 = partnerList;
				sync2 = this;
			}
			else
			{
				sync1 = this;
				sync2 = partnerList;
			}
			
			synchronized (sync1)
			{
				synchronized (sync2)
				{
					_confirmed = true;
					if (partnerList.isConfirmed())
					{
						partnerList.lock();
						lock();
						
						if (!partnerList.validate())
						{
							return false;
						}
						
						if (!validate())
						{
							return false;
						}
						
						doExchange(partnerList);
					}
					else
					{
						_partner.onTradeConfirm(_owner);
					}
				}
			}
		}
		else
		{
			_confirmed = true;
		}
		
		return _confirmed;
	}
	
	/**
	 * Cancels TradeList confirmation
	 */
	public void invalidateConfirmation()
	{
		_confirmed = false;
	}
	
	/**
	 * Validates TradeList with owner inventory
	 * @return
	 */
	private boolean validate()
	{
		if (_owner == null)
		{
			LOGGER.warning("Invalid owner of TradeList");
			return false;
		}
		
		final PlayerInstance player = (PlayerInstance) World.getInstance().findObject(_owner.getObjectId());
		if ((player == null) || (player.getInstanceLoginTime() != _owner.getInstanceLoginTime()))
		{
			LOGGER.warning("Invalid owner of TradeList");
			return false;
		}
		
		// Check for Item validity
		for (TradeItem titem : _items)
		{
			final ItemInstance item = _owner.checkItemManipulation(titem.getObjectId(), titem.getCount(), "transfer");
			if ((item == null) || (titem.getCount() < 1))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Transfers all TradeItems from inventory to partner
	 * @param partner
	 * @param ownerIU
	 * @param partnerIU
	 * @return
	 */
	private boolean TransferItems(PlayerInstance partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU)
	{
		for (TradeItem titem : _items)
		{
			final ItemInstance oldItem = _owner.getInventory().getItemByObjectId(titem.getObjectId());
			if (oldItem == null)
			{
				return false;
			}
			
			final ItemInstance newItem = _owner.getInventory().transferItem("Trade", titem.getObjectId(), titem.getCount(), partner.getInventory(), _owner, _partner);
			if (newItem == null)
			{
				return false;
			}
			
			// Add changes to inventory update packets
			if (ownerIU != null)
			{
				if ((oldItem.getCount() > 0) && (oldItem != newItem))
				{
					ownerIU.addModifiedItem(oldItem);
				}
				else
				{
					ownerIU.addRemovedItem(oldItem);
				}
			}
			
			if (partnerIU != null)
			{
				if (newItem.getCount() > titem.getCount())
				{
					partnerIU.addModifiedItem(newItem);
				}
				else
				{
					partnerIU.addNewItem(newItem);
				}
			}
		}
		return true;
	}
	
	/**
	 * Count items slots
	 * @param partner
	 * @return
	 */
	public int countItemsSlots(PlayerInstance partner)
	{
		int slots = 0;
		for (TradeItem item : _items)
		{
			if (item == null)
			{
				continue;
			}
			
			final Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if (template == null)
			{
				continue;
			}
			
			if (!template.isStackable())
			{
				slots += item.getCount();
			}
			else if (partner.getInventory().getItemByItemId(item.getItem().getItemId()) == null)
			{
				slots++;
			}
		}
		
		return slots;
	}
	
	/**
	 * Calc weight of items in tradeList
	 * @return
	 */
	public int calcItemsWeight()
	{
		int weight = 0;
		for (TradeItem item : _items)
		{
			if (item == null)
			{
				continue;
			}
			
			final Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if (template == null)
			{
				continue;
			}
			
			weight += item.getCount() * template.getWeight();
		}
		
		return weight;
	}
	
	/**
	 * Proceeds with trade
	 * @param partnerList
	 */
	private void doExchange(TradeList partnerList)
	{
		boolean success = false;
		// check weight and slots
		if (!getOwner().getInventory().validateWeight(partnerList.calcItemsWeight()) || !partnerList.getOwner().getInventory().validateWeight(calcItemsWeight()))
		{
			partnerList.getOwner().sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			_owner.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
		}
		else if (!getOwner().getInventory().validateCapacity(partnerList.countItemsSlots(getOwner())) || !partnerList.getOwner().getInventory().validateCapacity(countItemsSlots(partnerList.getOwner())))
		{
			partnerList.getOwner().sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			_owner.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
		}
		else
		{
			// Prepare inventory update packet
			final InventoryUpdate ownerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
			final InventoryUpdate partnerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
			
			// Transfer items
			partnerList.TransferItems(_owner, partnerIU, ownerIU);
			TransferItems(partnerList.getOwner(), ownerIU, partnerIU);
			
			// Send inventory update packet
			if (ownerIU != null)
			{
				_owner.sendPacket(ownerIU);
			}
			else
			{
				_owner.sendPacket(new ItemList(_owner, false));
			}
			
			if (partnerIU != null)
			{
				_partner.sendPacket(partnerIU);
			}
			else
			{
				_partner.sendPacket(new ItemList(_partner, false));
			}
			
			// Update current load as well
			StatusUpdate playerSU = new StatusUpdate(_owner.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, _owner.getCurrentLoad());
			_owner.sendPacket(playerSU);
			
			playerSU = new StatusUpdate(_partner.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, _partner.getCurrentLoad());
			_partner.sendPacket(playerSU);
			
			success = true;
		}
		// Finish the trade
		partnerList.getOwner().onTradeFinish(success);
		_owner.onTradeFinish(success);
	}
	
	/**
	 * Buy items from this PrivateStore list
	 * @param player
	 * @param items
	 * @param price
	 * @return : boolean true if success
	 */
	public synchronized boolean PrivateStoreBuy(PlayerInstance player, ItemRequest[] items, int price)
	{
		if (_locked)
		{
			return false;
		}
		
		if ((items == null) || (items.length == 0))
		{
			return false;
		}
		
		if (!validate())
		{
			lock();
			return false;
		}
		
		int slots = 0;
		int weight = 0;
		for (ItemRequest item : items)
		{
			if (item == null)
			{
				continue;
			}
			
			final Item template = ItemTable.getInstance().getTemplate(item.getItemId());
			if (template == null)
			{
				continue;
			}
			
			boolean found = false;
			for (TradeItem ti : _items)
			{
				if (ti.getObjectId() == item.getObjectId())
				{
					found = true;
					if (ti.getPrice() != item.getPrice())
					{
						return false;
					}
				}
			}
			
			// store is not selling that item...
			if (!found)
			{
				final String msg = "Requested Item is not available to buy... You are perfoming illegal operation, it has been segnalated";
				LOGGER.warning("ATTENTION: Player " + player.getName() + " has performed buy illegal operation..");
				player.sendMessage(msg);
				return false;
			}
			
			weight += item.getCount() * template.getWeight();
			if (!template.isStackable())
			{
				slots += item.getCount();
			}
			else if (player.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return false;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			return false;
		}
		
		final PlayerInventory ownerInventory = _owner.getInventory();
		final PlayerInventory playerInventory = player.getInventory();
		
		// Prepare inventory update packets
		final InventoryUpdate ownerIU = new InventoryUpdate();
		final InventoryUpdate playerIU = new InventoryUpdate();
		if (Config.SELL_BY_ITEM)
		{
			// Transfer Item
			if (price > playerInventory.getInventoryItemCount(Config.SELL_ITEM, -1))
			{
				// no useful lock to seller
				// lock();
				return false;
			}
			
			final ItemInstance item = playerInventory.getItemByItemId(Config.SELL_ITEM);
			if (item == null)
			{
				LOGGER.info("Buyer Medals are null");
				// no useful lock to seller
				// lock();
				return false;
			}
			
			// Check if requested item is available for manipulation
			final ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), price, "sell");
			if (oldItem == null)
			{
				LOGGER.info("Buyer old medals null");
				// no useful lock to seller
				// lock();
				return false;
			}
			
			// Proceed with item transfer
			final ItemInstance newItem = playerInventory.transferItem("PrivateStore", item.getObjectId(), price, ownerInventory, player, _owner);
			if (newItem == null)
			{
				LOGGER.info("Buyer new medals null");
				return false;
			}
			
			// Add changes to inventory update packets
			if ((oldItem.getCount() > 0) && (oldItem != newItem))
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			
			if (newItem.getCount() > item.getCount())
			{
				ownerIU.addModifiedItem(newItem);
			}
			else
			{
				ownerIU.addNewItem(newItem);
			}
			
			// Send messages about the transaction to both players
			final SystemMessage msg = SystemMessage.sendString("You obtained " + price + " " + item.getItemName());
			_owner.sendPacket(msg);
			
			final SystemMessage msg2 = SystemMessage.sendString("You spent " + price + " " + item.getItemName());
			player.sendPacket(msg2);
		}
		else
		{
			// Transfer adena
			if (price > playerInventory.getAdena())
			{
				lock();
				return false;
			}
			
			final ItemInstance adenaItem = playerInventory.getAdenaInstance();
			playerInventory.reduceAdena("PrivateStore", price, player, _owner);
			playerIU.addItem(adenaItem);
			ownerInventory.addAdena("PrivateStore", price, _owner, player);
			ownerIU.addItem(ownerInventory.getAdenaInstance());
		}
		
		// Transfer items
		for (ItemRequest item : items)
		{
			// Check if requested item is sill on the list and adjust its count
			adjustItemRequest(item);
			if (item.getCount() == 0)
			{
				continue;
			}
			
			// Check if requested item is available for manipulation
			final ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
			{
				lock();
				return false;
			}
			
			// Proceed with item transfer
			final ItemInstance newItem = ownerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), playerInventory, _owner, player);
			if (newItem == null)
			{
				return false;
			}
			
			removeItem(item.getObjectId(), -1, item.getCount());
			
			// Add changes to inventory update packets
			if ((oldItem.getCount() > 0) && (oldItem != newItem))
			{
				ownerIU.addModifiedItem(oldItem);
			}
			else
			{
				ownerIU.addRemovedItem(oldItem);
			}
			
			if (newItem.getCount() > item.getCount())
			{
				playerIU.addModifiedItem(newItem);
			}
			else
			{
				playerIU.addNewItem(newItem);
			}
			
			// Send messages about the transaction to both players
			if (newItem.isStackable())
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				_owner.sendPacket(msg);
				
				msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S3_S2_S_FROM_S1);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				player.sendPacket(msg);
			}
			else
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S2);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				_owner.sendPacket(msg);
				
				msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S2_FROM_S1);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				player.sendPacket(msg);
			}
		}
		
		// Send inventory update packet
		_owner.sendPacket(ownerIU);
		player.sendPacket(playerIU);
		
		if (_owner.isInOfflineMode())
		{
			OfflineTradeTable.storeOffliner(_owner);
		}
		
		return true;
	}
	
	/**
	 * Sell items to this PrivateStore list
	 * @param player
	 * @param items
	 * @param price
	 * @return : boolean true if success
	 */
	public synchronized boolean PrivateStoreSell(PlayerInstance player, ItemRequest[] items, int price)
	{
		if (_locked)
		{
			return false;
		}
		
		if ((items == null) || (items.length == 0))
		{
			return false;
		}
		
		final PlayerInventory ownerInventory = _owner.getInventory();
		final PlayerInventory playerInventory = player.getInventory();
		
		// Prepare inventory update packet
		final InventoryUpdate ownerIU = new InventoryUpdate();
		final InventoryUpdate playerIU = new InventoryUpdate();
		
		// we must check item are available before begining transaction, TODO: should we remove that check when transfering items as it's done here? (there might be synchro problems if player clicks fast if we remove it)
		// also check if augmented items are traded. If so, cancel it...
		for (ItemRequest item : items)
		{
			// Check if requested item is available for manipulation
			final ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
			{
				return false;
			}
			
			boolean found = false;
			for (TradeItem ti : _items)
			{
				if (ti.getItem().getItemId() == item.getItemId())
				{
					if (ti.getPrice() != item.getPrice())
					{
						return false;
					}
					
					if (ti.getEnchant() != item.getEnchant())
					{
						player.sendMessage("Incorect enchant level.");
						return false;
					}
					
					final WorldObject obj = World.getInstance().findObject(item.getObjectId());
					if (!(obj instanceof ItemInstance))
					{
						final String msgErr = "[RequestPrivateStoreSell] player " + _owner.getName() + " tried to sell null item in a private store (buy), ban this player!";
						Util.handleIllegalPlayerAction(_owner, msgErr, Config.DEFAULT_PUNISH);
						return false;
					}
					
					final ItemInstance itemInstance = (ItemInstance) obj;
					if (item.getEnchant() != itemInstance.getEnchantLevel())
					{
						final String msgErr = "[RequestPrivateStoreSell] player " + _owner.getName() + " tried to change enchant level in a private store (buy), ban this player!";
						Util.handleIllegalPlayerAction(_owner, msgErr, Config.DEFAULT_PUNISH);
						return false;
					}
					
					found = true;
					break;
				}
			}
			
			// store is not buying that item...
			if (!found)
			{
				final String msg = "Requested Item is not available to sell... You are perfoming illegal operation, it has been segnalated";
				LOGGER.warning("ATTENTION: Player " + player.getName() + " has performed sell illegal operation..");
				player.sendMessage(msg);
				return false;
			}
			
			if (oldItem.getAugmentation() != null)
			{
				final String msg = "Transaction failed. Augmented items may not be exchanged.";
				_owner.sendMessage(msg);
				player.sendMessage(msg);
				return false;
			}
		}
		
		// Transfer items
		for (ItemRequest item : items)
		{
			// Check if requested item is sill on the list and adjust its count
			adjustItemRequestByItemId(item);
			if (item.getCount() == 0)
			{
				continue;
			}
			
			// Check if requested item is available for manipulation
			final ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
			{
				return false;
			}
			
			// Check if requested item is correct
			if (oldItem.getItemId() != item.getItemId())
			{
				Util.handleIllegalPlayerAction(player, player + " is cheating with sell items", Config.DEFAULT_PUNISH);
				return false;
			}
			
			// Proceed with item transfer
			final ItemInstance newItem = playerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), ownerInventory, player, _owner);
			if (newItem == null)
			{
				return false;
			}
			
			removeItem(-1, item.getItemId(), item.getCount());
			
			// Add changes to inventory update packets
			if ((oldItem.getCount() > 0) && (oldItem != newItem))
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			
			if (newItem.getCount() > item.getCount())
			{
				ownerIU.addModifiedItem(newItem);
			}
			else
			{
				ownerIU.addNewItem(newItem);
			}
			
			// Send messages about the transaction to both players
			if (newItem.isStackable())
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S3_S2_S_FROM_S1);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				_owner.sendPacket(msg);
				
				msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				player.sendPacket(msg);
			}
			else
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_PURCHASED_S2_FROM_S1);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				_owner.sendPacket(msg);
				
				msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S2);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				player.sendPacket(msg);
			}
		}
		
		if (Config.SELL_BY_ITEM)
		{
			// Transfer Item
			if (price > ownerInventory.getInventoryItemCount(Config.SELL_ITEM, -1))
			{
				lock();
				return false;
			}
			
			final ItemInstance item = ownerInventory.getItemByItemId(Config.SELL_ITEM);
			if (item == null)
			{
				lock();
				return false;
			}
			
			// Check if requested item is available for manipulation
			final ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), price, "sell");
			if (oldItem == null)
			{
				lock();
				return false;
			}
			
			// Proceed with item transfer
			final ItemInstance newItem = ownerInventory.transferItem("PrivateStore", item.getObjectId(), price, playerInventory, _owner, player);
			if (newItem == null)
			{
				return false;
			}
			
			// Add changes to inventory update packets
			if ((oldItem.getCount() > 0) && (oldItem != newItem))
			{
				ownerIU.addModifiedItem(oldItem);
			}
			else
			{
				ownerIU.addRemovedItem(oldItem);
			}
			
			if (newItem.getCount() > item.getCount())
			{
				playerIU.addModifiedItem(newItem);
			}
			else
			{
				playerIU.addNewItem(newItem);
			}
			
			// Send messages about the transaction to both players
			final SystemMessage msg = SystemMessage.sendString("You obtained " + price + " " + item.getItemName());
			player.sendPacket(msg);
			
			final SystemMessage msg2 = SystemMessage.sendString("You spent " + price + " " + item.getItemName());
			_owner.sendPacket(msg2);
		}
		else
		{
			// Transfer adena
			if (price > ownerInventory.getAdena())
			{
				return false;
			}
			
			final ItemInstance adenaItem = ownerInventory.getAdenaInstance();
			ownerInventory.reduceAdena("PrivateStore", price, _owner, player);
			ownerIU.addItem(adenaItem);
			playerInventory.addAdena("PrivateStore", price, player, _owner);
			playerIU.addItem(playerInventory.getAdenaInstance());
		}
		
		// Send inventory update packet
		_owner.sendPacket(ownerIU);
		player.sendPacket(playerIU);
		
		if (_owner.isInOfflineMode())
		{
			OfflineTradeTable.storeOffliner(_owner);
		}
		
		return true;
	}
	
	/**
	 * @param objectId
	 * @return
	 */
	public TradeItem getItem(int objectId)
	{
		for (TradeItem item : _items)
		{
			if (item.getObjectId() == objectId)
			{
				return item;
			}
		}
		return null;
	}
}
