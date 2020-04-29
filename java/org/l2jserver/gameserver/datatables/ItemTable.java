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
package org.l2jserver.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.PetDataTable;
import org.l2jserver.gameserver.engines.DocumentEngine;
import org.l2jserver.gameserver.engines.ItemDataHolder;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jserver.gameserver.model.items.Armor;
import org.l2jserver.gameserver.model.items.EtcItem;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance.ItemLocation;

/**
 * @version $Revision: 1.9.2.6.2.9 $ $Date: 2005/04/02 15:57:34 $
 */
public class ItemTable
{
	private static final Logger LOGGER = Logger.getLogger(ItemTable.class.getName());
	private static final Logger _logItems = Logger.getLogger("item");
	
	private Item[] _allTemplates;
	private final Map<Integer, EtcItem> _etcItems;
	private final Map<Integer, Armor> _armors;
	private final Map<Integer, Weapon> _weapons;
	
	private static final Map<String, Integer> _crystalTypes = new HashMap<>();
	static
	{
		_crystalTypes.put("s", Item.CRYSTAL_S);
		_crystalTypes.put("a", Item.CRYSTAL_A);
		_crystalTypes.put("b", Item.CRYSTAL_B);
		_crystalTypes.put("c", Item.CRYSTAL_C);
		_crystalTypes.put("d", Item.CRYSTAL_D);
		_crystalTypes.put("none", Item.CRYSTAL_NONE);
	}
	
	/**
	 * Returns a new object Item
	 * @return
	 */
	public ItemDataHolder newItem()
	{
		return new ItemDataHolder();
	}
	
	/**
	 * Constructor.
	 */
	private ItemTable()
	{
		_etcItems = new HashMap<>();
		_armors = new HashMap<>();
		_weapons = new HashMap<>();
		load();
	}
	
	private void load()
	{
		int highest = 0;
		_armors.clear();
		_etcItems.clear();
		_weapons.clear();
		for (Item item : DocumentEngine.getInstance().loadItems())
		{
			if (highest < item.getItemId())
			{
				highest = item.getItemId();
			}
			if (item instanceof EtcItem)
			{
				_etcItems.put(item.getItemId(), (EtcItem) item);
			}
			else if (item instanceof Armor)
			{
				_armors.put(item.getItemId(), (Armor) item);
			}
			else
			{
				_weapons.put(item.getItemId(), (Weapon) item);
			}
		}
		buildFastLookupTable(highest);
	}
	
	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 * @param size
	 */
	private void buildFastLookupTable(int size)
	{
		// Create a FastLookUp Table called _allTemplates of size : value of the highest item ID
		LOGGER.info("Highest item id used: " + size);
		_allTemplates = new Item[size + 1];
		
		// Insert armor item in Fast Look Up Table
		for (Armor item : _armors.values())
		{
			_allTemplates[item.getItemId()] = item;
		}
		
		// Insert weapon item in Fast Look Up Table
		for (Weapon item : _weapons.values())
		{
			_allTemplates[item.getItemId()] = item;
		}
		
		// Insert etcItem item in Fast Look Up Table
		for (EtcItem item : _etcItems.values())
		{
			_allTemplates[item.getItemId()] = item;
		}
	}
	
	/**
	 * Returns the item corresponding to the item ID
	 * @param id : int designating the item
	 * @return Item
	 */
	public Item getTemplate(int id)
	{
		if (id >= _allTemplates.length)
		{
			return null;
		}
		return _allTemplates[id];
	}
	
	/**
	 * Create the ItemInstance corresponding to the Item Identifier and quantitiy add logs the activity.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Create and Init the ItemInstance corresponding to the Item Identifier and quantity</li>
	 * <li>Add the ItemInstance object to _allObjects of L2world</li>
	 * <li>Logs Item creation according to LOGGER settings</li><br>
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be created
	 * @param count : int Quantity of items to be created for stackable items
	 * @param actor : PlayerInstance Player requesting the item creation
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the new item
	 */
	public ItemInstance createItem(String process, int itemId, int count, PlayerInstance actor, WorldObject reference)
	{
		// Create and Init the ItemInstance corresponding to the Item Identifier
		final ItemInstance item = new ItemInstance(IdFactory.getNextId(), itemId);
		
		// create loot schedule also if autoloot is enabled
		if (process.equalsIgnoreCase("loot")/* && !Config.AUTO_LOOT */)
		{
			ScheduledFuture<?> itemLootShedule;
			long delay = 0;
			// if in CommandChannel and was killing a World/RaidBoss
			if ((reference instanceof GrandBossInstance) || (reference instanceof RaidBossInstance))
			{
				if ((((Attackable) reference).getFirstCommandChannelAttacked() != null) && ((Attackable) reference).getFirstCommandChannelAttacked().meetRaidWarCondition(reference))
				{
					item.setOwnerId(((Attackable) reference).getFirstCommandChannelAttacked().getChannelLeader().getObjectId());
					delay = 300000;
				}
				else
				{
					delay = 15000;
					item.setOwnerId(actor.getObjectId());
				}
			}
			else
			{
				item.setOwnerId(actor.getObjectId());
				delay = 15000;
			}
			itemLootShedule = ThreadPool.schedule(new resetOwner(item), delay);
			item.setItemLootShedule(itemLootShedule);
		}
		
		// Add the ItemInstance object to _allObjects of L2world
		World.getInstance().storeObject(item);
		
		// Set Item parameters
		if (item.isStackable() && (count > 1))
		{
			item.setCount(count);
		}
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				item,
				actor,
				reference
			});
			_logItems.log(record);
		}
		return item;
	}
	
	public ItemInstance createItem(String process, int itemId, int count, PlayerInstance actor)
	{
		return createItem(process, itemId, count, actor, null);
	}
	
	/**
	 * Returns a dummy (fr = factice) item.<br>
	 * <u><i>Concept :</i></u><br>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 * @param itemId : int designating the item
	 * @return ItemInstance designating the dummy item created
	 */
	public ItemInstance createDummyItem(int itemId)
	{
		final Item item = getTemplate(itemId);
		if (item == null)
		{
			return null;
		}
		
		ItemInstance temp = new ItemInstance(0, item);
		
		try
		{
			temp = new ItemInstance(0, itemId);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// this can happen if the item templates were not initialized
		}
		
		if (temp.getItem() == null)
		{
			LOGGER.warning("ItemTable: Item Template missing for Id: " + itemId);
		}
		
		return temp;
	}
	
	/**
	 * Destroys the ItemInstance.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Sets ItemInstance parameters to be unusable</li>
	 * <li>Removes the ItemInstance object to _allObjects of L2world</li>
	 * <li>Logs Item delettion according to LOGGER settings</li><br>
	 * @param process : String Identifier of process triggering this action
	 * @param item
	 * @param actor : PlayerInstance Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void destroyItem(String process, ItemInstance item, PlayerInstance actor, WorldObject reference)
	{
		synchronized (item)
		{
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(ItemInstance.REMOVED);
			
			World.getInstance().removeObject(item);
			IdFactory.releaseId(item.getObjectId());
			
			// if it's a pet control item, delete the pet as well
			if (PetDataTable.isPetItem(item.getItemId()))
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
					statement.setInt(1, item.getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("Could not delete pet objectid " + e);
				}
			}
		}
	}
	
	public void reload()
	{
		load();
	}
	
	protected class resetOwner implements Runnable
	{
		ItemInstance _item;
		
		public resetOwner(ItemInstance item)
		{
			_item = item;
		}
		
		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}
	
	public Set<Integer> getAllArmorsId()
	{
		return _armors.keySet();
	}
	
	public Set<Integer> getAllWeaponsId()
	{
		return _weapons.keySet();
	}
	
	public int getArraySize()
	{
		return _allTemplates.length;
	}
	
	/**
	 * Returns instance of ItemTable
	 * @return ItemTable
	 */
	public static ItemTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemTable INSTANCE = new ItemTable();
	}
}
