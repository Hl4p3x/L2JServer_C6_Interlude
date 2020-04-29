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
package org.l2jserver.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.model.StoreTradeList;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

/**
 * This class manages buylists from database
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeListTable
{
	private static final Logger LOGGER = Logger.getLogger(TradeListTable.class.getName());
	
	private int _nextListId;
	private final Map<Integer, StoreTradeList> _lists = new HashMap<>();
	
	/** Task launching the function for restore count of Item (Clan Hall) */
	private class RestoreCount implements Runnable
	{
		private final int _timer;
		
		public RestoreCount(int time)
		{
			_timer = time;
		}
		
		@Override
		public void run()
		{
			restoreCount(_timer);
			dataTimerSave(_timer);
			ThreadPool.schedule(new RestoreCount(_timer), _timer * 60 * 60 * 1000);
		}
	}
	
	private TradeListTable()
	{
		load();
	}
	
	private void load(boolean custom)
	{
		_lists.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement1 = con.prepareStatement("SELECT shop_id,npc_id FROM " + (custom ? "custom_merchant_shopids" : "merchant_shopids"));
			final ResultSet rset1 = statement1.executeQuery();
			
			while (rset1.next())
			{
				final PreparedStatement statement = con.prepareStatement("SELECT item_id, price, shop_id, order, count, time, currentCount FROM " + (custom ? "custom_merchant_buylists" : "merchant_buylists") + " WHERE shop_id=? ORDER BY order ASC");
				statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
				final ResultSet rset = statement.executeQuery();
				final StoreTradeList buylist = new StoreTradeList(rset1.getInt("shop_id"));
				buylist.setNpcId(rset1.getString("npc_id"));
				int itemId = 0;
				int itemCount = 0;
				int price = 0;
				if (!buylist.isGm() && (NpcTable.getInstance().getTemplate(rset1.getInt("npc_id")) == null))
				{
					LOGGER.warning("TradeListTable: Merchant id " + rset1.getString("npc_id") + " with buylist " + buylist.getListId() + " does not exist.");
				}
				
				try
				{
					while (rset.next())
					{
						itemId = rset.getInt("item_id");
						price = rset.getInt("price");
						final int count = rset.getInt("count");
						final int currentCount = rset.getInt("currentCount");
						final int time = rset.getInt("time");
						final ItemInstance buyItem = ItemTable.getInstance().createDummyItem(itemId);
						if (buyItem == null)
						{
							continue;
						}
						
						itemCount++;
						
						if (count > -1)
						{
							buyItem.setCountDecrease(true);
						}
						buyItem.setPriceToSell(price);
						buyItem.setTime(time);
						buyItem.setInitCount(count);
						
						if (currentCount > -1)
						{
							buyItem.setCount(currentCount);
						}
						else
						{
							buyItem.setCount(count);
						}
						
						buylist.addItem(buyItem);
						
						if (!buylist.isGm() && (buyItem.getReferencePrice() > price))
						{
							LOGGER.warning("TradeListTable: Reference price of item " + itemId + " in buylist " + buylist.getListId() + " higher then sell price.");
						}
					}
				}
				catch (Exception e)
				{
					LOGGER.warning("TradeListTable: Problem with buylist " + buylist.getListId() + ". " + e);
				}
				
				if (itemCount > 0)
				{
					_lists.put(buylist.getListId(), buylist);
					_nextListId = Math.max(_nextListId, buylist.getListId() + 1);
				}
				else
				{
					LOGGER.warning("TradeListTable: Empty buylist " + buylist.getListId() + ".");
				}
				
				statement.close();
				rset.close();
			}
			rset1.close();
			statement1.close();
			
			LOGGER.info("TradeListTable: Loaded " + _lists.size() + " Buylists.");
			
			try
			{
				int time = 0;
				long savetimer = 0;
				final long currentMillis = System.currentTimeMillis();
				final PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM " + (custom ? "custom_merchant_buylists" : "merchant_buylists") + " WHERE time <> 0 ORDER BY time");
				final ResultSet rset2 = statement2.executeQuery();
				
				while (rset2.next())
				{
					time = rset2.getInt("time");
					savetimer = rset2.getLong("savetimer");
					if ((savetimer - currentMillis) > 0)
					{
						ThreadPool.schedule(new RestoreCount(time), savetimer - System.currentTimeMillis());
					}
					else
					{
						ThreadPool.schedule(new RestoreCount(time), 0);
					}
				}
				
				rset2.close();
				statement2.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("TradeController: Could not restore Timer for Item count. " + e);
			}
		}
		catch (Exception e)
		{
			// problem with initializing buylists, go to next one
			LOGGER.warning("TradeListTable: Buylists could not be initialized. " + e);
		}
	}
	
	public void load()
	{
		load(false); // not custom
		load(true); // custom
	}
	
	public void reloadAll()
	{
		_lists.clear();
		
		load();
	}
	
	public StoreTradeList getBuyList(int listId)
	{
		if (_lists.containsKey(listId))
		{
			return _lists.get(listId);
		}
		return null;
	}
	
	protected void restoreCount(int time)
	{
		for (StoreTradeList list : _lists.values())
		{
			list.restoreCount(time);
		}
	}
	
	protected void dataTimerSave(int time)
	{
		final long timerSave = System.currentTimeMillis() + (time * 3600000); // 60*60*1000
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE merchant_buylists SET savetimer =? WHERE time =?");
			statement.setLong(1, timerSave);
			statement.setInt(2, time);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("TradeController: Could not update Timer save in Buylist. " + e);
		}
	}
	
	public void dataCountStore()
	{
		int listId;
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			for (StoreTradeList list : _lists.values())
			{
				if (list == null)
				{
					continue;
				}
				
				listId = list.getListId();
				for (ItemInstance Item : list.getItems())
				{
					if (Item.getCount() < Item.getInitCount()) // needed?
					{
						statement = con.prepareStatement("UPDATE merchant_buylists SET currentCount=? WHERE item_id=? AND shop_id=?");
						statement.setInt(1, Item.getCount());
						statement.setInt(2, Item.getItemId());
						statement.setInt(3, listId);
						statement.executeUpdate();
						statement.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("TradeController: Could not store Count Item. " + e);
		}
	}
	
	public static TradeListTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TradeListTable INSTANCE = new TradeListTable();
	}
}
