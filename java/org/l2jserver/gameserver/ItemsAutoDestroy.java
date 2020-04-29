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
package org.l2jserver.gameserver;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.EtcItemType;

public class ItemsAutoDestroy
{
	protected static final Logger LOGGER = Logger.getLogger(ItemsAutoDestroy.class.getName());
	
	private final Collection<ItemInstance> _items = ConcurrentHashMap.newKeySet();
	
	protected ItemsAutoDestroy()
	{
		ThreadPool.scheduleAtFixedRate(this::removeItems, 5000, 5000);
	}
	
	public synchronized void addItem(ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}
	
	private synchronized void removeItems()
	{
		if (_items.isEmpty())
		{
			return;
		}
		
		final long curtime = System.currentTimeMillis();
		for (ItemInstance item : _items)
		{
			if ((item == null) || (item.getDropTime() == 0) || (item.getItemLocation() != ItemInstance.ItemLocation.VOID))
			{
				_items.remove(item);
			}
			else if (item.getItemType() == EtcItemType.HERB)
			{
				if ((curtime - item.getDropTime()) > Config.HERB_AUTO_DESTROY_TIME)
				{
					World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					World.getInstance().removeObject(item);
					_items.remove(item);
					
					if (Config.SAVE_DROPPED_ITEM)
					{
						ItemsOnGroundManager.getInstance().removeObject(item);
					}
				}
			}
			else if ((curtime - item.getDropTime()) > (Config.AUTODESTROY_ITEM_AFTER * 1000))
			{
				World.getInstance().removeVisibleObject(item, item.getWorldRegion());
				World.getInstance().removeObject(item);
				_items.remove(item);
				
				if (Config.SAVE_DROPPED_ITEM)
				{
					ItemsOnGroundManager.getInstance().removeObject(item);
				}
			}
		}
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroy INSTANCE = new ItemsAutoDestroy();
	}
}
