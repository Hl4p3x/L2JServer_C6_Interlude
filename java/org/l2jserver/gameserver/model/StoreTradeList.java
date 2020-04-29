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

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

/**
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:33 $
 */
public class StoreTradeList
{
	private static final Logger LOGGER = Logger.getLogger(StoreTradeList.class.getName());
	
	private final List<ItemInstance> _items;
	private final int _listId;
	private boolean _confirmed;
	private boolean _gm;
	private String _buystorename;
	private String _sellstorename;
	private String _npcId;
	
	public StoreTradeList(int listId)
	{
		_items = new ArrayList<>();
		_listId = listId;
		_confirmed = false;
	}
	
	public void setNpcId(String id)
	{
		_npcId = id;
		if (id.equals("gm"))
		{
			_gm = true;
		}
		else
		{
			_gm = false;
		}
	}
	
	public String getNpcId()
	{
		return _npcId;
	}
	
	public void addItem(ItemInstance item)
	{
		_items.add(item);
	}
	
	public void replaceItem(int itemID, int price)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final ItemInstance item = _items.get(i);
			if (item.getItemId() == itemID)
			{
				if (price < (item.getReferencePrice() / 2))
				{
					LOGGER.warning("TradeList " + _listId + " itemId  " + itemID + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
					price = item.getReferencePrice();
				}
				item.setPriceToSell(price);
			}
		}
	}
	
	public synchronized boolean decreaseCount(int itemID, int count)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final ItemInstance item = _items.get(i);
			if (item.getItemId() == itemID)
			{
				if (item.getCount() < count)
				{
					return false;
				}
				item.setCount(item.getCount() - count);
			}
		}
		return true;
	}
	
	public void restoreCount(int time)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final ItemInstance item = _items.get(i);
			if (item.getCountDecrease() && (item.getTime() == time))
			{
				item.restoreInitCount();
			}
		}
	}
	
	public void removeItem(int itemId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final ItemInstance item = _items.get(i);
			if (item.getItemId() == itemId)
			{
				_items.remove(item);
			}
		}
	}
	
	/**
	 * @return Returns the listId.
	 */
	public int getListId()
	{
		return _listId;
	}
	
	public void setSellStoreName(String name)
	{
		_sellstorename = name;
	}
	
	public String getSellStoreName()
	{
		return _sellstorename;
	}
	
	public void setBuyStoreName(String name)
	{
		_buystorename = name;
	}
	
	public String getBuyStoreName()
	{
		return _buystorename;
	}
	
	/**
	 * @return Returns the items.
	 */
	public List<ItemInstance> getItems()
	{
		return _items;
	}
	
	public List<ItemInstance> getItems(int start, int end)
	{
		return _items.subList(start, end);
	}
	
	public int getPriceForItemId(int itemId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final ItemInstance item = _items.get(i);
			if (item.getItemId() == itemId)
			{
				return item.getPriceToSell();
			}
		}
		return -1;
	}
	
	public boolean countDecrease(int itemId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final ItemInstance item = _items.get(i);
			if (item.getItemId() == itemId)
			{
				return item.getCountDecrease();
			}
		}
		return false;
	}
	
	public boolean containsItemId(int itemId)
	{
		for (ItemInstance item : _items)
		{
			if (item.getItemId() == itemId)
			{
				return true;
			}
		}
		return false;
	}
	
	public ItemInstance getItem(int objectId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final ItemInstance item = _items.get(i);
			if (item.getObjectId() == objectId)
			{
				return item;
			}
		}
		return null;
	}
	
	public synchronized void setConfirmedTrade(boolean confirmed)
	{
		_confirmed = confirmed;
	}
	
	public synchronized boolean hasConfirmed()
	{
		return _confirmed;
	}
	
	public void removeItem(int objId, int count)
	{
		ItemInstance temp;
		for (int y = 0; y < _items.size(); y++)
		{
			temp = _items.get(y);
			if (temp.getObjectId() == objId)
			{
				if (count == temp.getCount())
				{
					_items.remove(temp);
				}
				break;
			}
		}
	}
	
	public boolean contains(int objId)
	{
		boolean bool = false;
		ItemInstance temp;
		for (int y = 0; y < _items.size(); y++)
		{
			temp = _items.get(y);
			if (temp.getObjectId() == objId)
			{
				bool = true;
				break;
			}
		}
		return bool;
	}
	
	public void updateBuyList(PlayerInstance player, List<TradeItem> list)
	{
		TradeItem temp;
		int count;
		final Inventory playersInv = player.getInventory();
		ItemInstance temp2;
		count = 0;
		
		while (count != list.size())
		{
			temp = list.get(count);
			temp2 = playersInv.getItemByItemId(temp.getItemId());
			if (temp2 == null)
			{
				list.remove(count);
				count = count - 1;
			}
			else if (temp.getCount() == 0)
			{
				list.remove(count);
				count = count - 1;
			}
			count++;
		}
	}
	
	public void updateSellList(PlayerInstance player, List<TradeItem> list)
	{
		final Inventory playersInv = player.getInventory();
		TradeItem temp;
		ItemInstance temp2;
		int count = 0;
		while (count != list.size())
		{
			temp = list.get(count);
			temp2 = playersInv.getItemByObjectId(temp.getObjectId());
			if (temp2 == null)
			{
				list.remove(count);
				count = count - 1;
			}
			else if (temp2.getCount() < temp.getCount())
			{
				temp.setCount(temp2.getCount());
			}
			count++;
		}
	}
	
	public boolean isGm()
	{
		return _gm;
	}
}
