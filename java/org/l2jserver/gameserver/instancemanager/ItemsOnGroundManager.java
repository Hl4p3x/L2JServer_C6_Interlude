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
package org.l2jserver.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.ItemsAutoDestroy;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.EtcItemType;

/**
 * This class manage all items on ground
 * @version $Revision: $ $Date: $
 * @author DiezelMax - original ideea
 * @author Enforcer - actual build
 */
public class ItemsOnGroundManager
{
	static final Logger LOGGER = Logger.getLogger(ItemsOnGroundManager.class.getName());
	protected List<ItemInstance> _items = new ArrayList<>();
	
	private ItemsOnGroundManager()
	{
		// If SaveDroppedItem is false, may want to delete all items previously stored to avoid add old items on reactivate
		if (!Config.SAVE_DROPPED_ITEM)
		{
			if (Config.CLEAR_DROPPED_ITEM_TABLE)
			{
				emptyTable();
			}
			
			return;
		}
		
		LOGGER.info("Initializing ItemsOnGroundManager");
		_items.clear();
		load();
		
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
		{
			ThreadPool.scheduleAtFixedRate(new StoreInDb(), Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
		}
	}
	
	public static final ItemsOnGroundManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private void load()
	{
		// if DestroyPlayerDroppedItem was previously false, items curently protected will be added to ItemsAutoDestroy
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				String str = null;
				if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "update itemsonground set drop_time=? where drop_time=-1 and equipable=0";
				}
				else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "update itemsonground set drop_time=? where drop_time=-1";
				}
				
				final PreparedStatement statement = con.prepareStatement(str);
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("error while updating table ItemsOnGround " + e);
			}
		}
		
		// Add items to world
		try (Connection con = DatabaseFactory.getConnection())
		{
			final Statement s = con.createStatement();
			ResultSet result;
			int count = 0;
			result = s.executeQuery("select object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable from itemsonground");
			while (result.next())
			{
				final ItemInstance item = new ItemInstance(result.getInt(1), result.getInt(2));
				World.getInstance().storeObject(item);
				if (item.isStackable() && (result.getInt(3) > 1))
				{
					item.setCount(result.getInt(3));
				}
				
				if (result.getInt(4) > 0)
				{
					item.setEnchantLevel(result.getInt(4));
				}
				
				item.getPosition().setWorldPosition(result.getInt(5), result.getInt(6), result.getInt(7));
				item.getPosition().setWorldRegion(World.getInstance().getRegion(item.getLocation()));
				item.getPosition().getWorldRegion().addVisibleObject(item);
				item.setDropTime(result.getLong(8));
				item.setProtected(result.getLong(8) == -1);
				item.setVisible(true);
				World.getInstance().addVisibleObject(item, item.getPosition().getWorldRegion(), null);
				_items.add(item);
				count++;
				// add to ItemsAutoDestroy only items not protected
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()) && (result.getLong(8) > -1) && (((Config.AUTODESTROY_ITEM_AFTER > 0) && (item.getItemType() != EtcItemType.HERB)) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (item.getItemType() == EtcItemType.HERB))))
				{
					ItemsAutoDestroy.getInstance().addItem(item);
				}
			}
			
			result.close();
			s.close();
			
			if (count > 0)
			{
				LOGGER.info("ItemsOnGroundManager: restored " + count + " items.");
			}
			else
			{
				LOGGER.info("Initializing ItemsOnGroundManager.");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("error while loading ItemsOnGround " + e);
		}
		
		if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
		{
			emptyTable();
		}
	}
	
	public void save(ItemInstance item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		_items.add(item);
	}
	
	public void removeObject(WorldObject item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		_items.remove(item);
	}
	
	public void saveInDb()
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		ThreadPool.execute(new StoreInDb());
	}
	
	public void cleanUp()
	{
		_items.clear();
	}
	
	public void emptyTable()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement del = con.prepareStatement("delete from itemsonground");
			del.execute();
			del.close();
		}
		catch (Exception e1)
		{
			LOGGER.warning("Error while cleaning table ItemsOnGround: " + e1);
		}
	}
	
	protected class StoreInDb implements Runnable
	{
		@Override
		public void run()
		{
			emptyTable();
			
			if (_items.isEmpty())
			{
				return;
			}
			
			for (ItemInstance item : _items)
			{
				if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					continue; // Cursed Items not saved to ground, prevent double save
				}
				
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("insert into itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) values(?,?,?,?,?,?,?,?,?)");
					statement.setInt(1, item.getObjectId());
					statement.setInt(2, item.getItemId());
					statement.setInt(3, item.getCount());
					statement.setInt(4, item.getEnchantLevel());
					statement.setInt(5, item.getX());
					statement.setInt(6, item.getY());
					statement.setInt(7, item.getZ());
					if (item.isProtected())
					{
						statement.setLong(8, -1); // item will be protected
					}
					else
					{
						statement.setLong(8, item.getDropTime()); // item will be added to ItemsAutoDestroy
					}
					if (item.isEquipable())
					{
						statement.setLong(9, 1); // set equipable
					}
					else
					{
						statement.setLong(9, 0);
					}
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("Error while inserting into table ItemsOnGround: " + e);
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsOnGroundManager INSTANCE = new ItemsOnGroundManager();
	}
}
