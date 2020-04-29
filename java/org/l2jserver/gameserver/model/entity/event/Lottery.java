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
package org.l2jserver.gameserver.model.entity.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Lottery
{
	protected static final Logger LOGGER = Logger.getLogger(Lottery.class.getName());
	
	public static final long SECOND = 1000;
	public static final long MINUTE = 60000;
	
	private static final String INSERT_LOTTERY = "INSERT INTO lottery(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
	private static final String UPDATE_PRICE = "UPDATE lottery SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
	private static final String UPDATE_LOTTERY = "UPDATE lottery SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
	private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM lottery WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
	private static final String SELECT_LOTTERY_ITEM = "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?";
	private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM lottery WHERE id = 1 and idnr = ?";
	
	protected int _number;
	protected int _prize;
	protected boolean _isSellingTickets;
	protected boolean _isStarted;
	protected long _enddate;
	
	private Lottery()
	{
		_number = 1;
		_prize = Config.ALT_LOTTERY_PRIZE;
		_isSellingTickets = false;
		_isStarted = false;
		_enddate = System.currentTimeMillis();
		if (Config.ALLOW_LOTTERY)
		{
			new startLottery().run();
		}
	}
	
	public int getId()
	{
		return _number;
	}
	
	public int getPrize()
	{
		return _prize;
	}
	
	public long getEndDate()
	{
		return _enddate;
	}
	
	public void increasePrize(int count)
	{
		_prize += count;
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement(UPDATE_PRICE);
			statement.setInt(1, _prize);
			statement.setInt(2, _prize);
			statement.setInt(3, _number);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Lottery: Could not increase current lottery prize: " + e);
		}
	}
	
	public boolean isSellableTickets()
	{
		return _isSellingTickets;
	}
	
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	private class startLottery implements Runnable
	{
		protected startLottery()
		{
		}
		
		@Override
		public void run()
		{
			PreparedStatement statement;
			try (Connection con = DatabaseFactory.getConnection())
			{
				statement = con.prepareStatement(SELECT_LAST_LOTTERY);
				final ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					_number = rset.getInt("idnr");
					if (rset.getInt("finished") == 1)
					{
						_number++;
						_prize = rset.getInt("newprize");
					}
					else
					{
						_prize = rset.getInt("prize");
						_enddate = rset.getLong("enddate");
						if (_enddate <= (System.currentTimeMillis() + (2 * MINUTE)))
						{
							new finishLottery().run();
							rset.close();
							statement.close();
							con.close();
							return;
						}
						
						if (_enddate > System.currentTimeMillis())
						{
							_isStarted = true;
							ThreadPool.schedule(new finishLottery(), _enddate - System.currentTimeMillis());
							if (_enddate > (System.currentTimeMillis() + (12 * MINUTE)))
							{
								_isSellingTickets = true;
								ThreadPool.schedule(new stopSellingTickets(), _enddate - System.currentTimeMillis() - (10 * MINUTE));
							}
							rset.close();
							statement.close();
							con.close();
							return;
						}
					}
				}
				rset.close();
				statement.close();
			}
			catch (SQLException e)
			{
				LOGGER.warning("Lottery: Could not restore lottery data: " + e);
			}
			
			_isSellingTickets = true;
			_isStarted = true;
			Announcements.getInstance().announceToAll("Lottery tickets are now available for Lucky Lottery #" + getId() + ".");
			final Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(_enddate);
			finishtime.set(Calendar.MINUTE, 0);
			finishtime.set(Calendar.SECOND, 0);
			if (finishtime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			{
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
				_enddate += 604800000;
			}
			else
			{
				finishtime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
			}
			
			ThreadPool.schedule(new stopSellingTickets(), _enddate - System.currentTimeMillis() - (10 * MINUTE));
			ThreadPool.schedule(new finishLottery(), _enddate - System.currentTimeMillis());
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				statement = con.prepareStatement(INSERT_LOTTERY);
				statement.setInt(1, 1);
				statement.setInt(2, _number);
				statement.setLong(3, _enddate);
				statement.setInt(4, _prize);
				statement.setInt(5, _prize);
				statement.execute();
				statement.close();
			}
			catch (SQLException e)
			{
				LOGGER.warning("Lottery: Could not store new lottery data: " + e);
			}
		}
	}
	
	private class stopSellingTickets implements Runnable
	{
		protected stopSellingTickets()
		{
		}
		
		@Override
		public void run()
		{
			_isSellingTickets = false;
			Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED));
		}
	}
	
	private class finishLottery implements Runnable
	{
		protected finishLottery()
		{
		}
		
		@Override
		public void run()
		{
			final int[] luckynums = new int[5];
			int luckynum = 0;
			for (int i = 0; i < 5; i++)
			{
				boolean found = true;
				
				while (found)
				{
					luckynum = Rnd.get(20) + 1;
					found = false;
					for (int j = 0; j < i; j++)
					{
						if (luckynums[j] == luckynum)
						{
							found = true;
						}
					}
				}
				
				luckynums[i] = luckynum;
			}
			
			int enchant = 0;
			int type2 = 0;
			for (int i = 0; i < 5; i++)
			{
				if (luckynums[i] < 17)
				{
					enchant += Math.pow(2, luckynums[i] - 1);
				}
				else
				{
					type2 += Math.pow(2, luckynums[i] - 17);
				}
			}
			
			int count1 = 0;
			int count2 = 0;
			int count3 = 0;
			int count4 = 0;
			PreparedStatement statement;
			try (Connection con = DatabaseFactory.getConnection())
			{
				statement = con.prepareStatement(SELECT_LOTTERY_ITEM);
				statement.setInt(1, _number);
				final ResultSet rset = statement.executeQuery();
				
				while (rset.next())
				{
					int curenchant = rset.getInt("enchant_level") & enchant;
					int curtype2 = rset.getInt("custom_type2") & type2;
					if ((curenchant == 0) && (curtype2 == 0))
					{
						continue;
					}
					
					int count = 0;
					for (int i = 1; i <= 16; i++)
					{
						final int val = curenchant / 2;
						if (val != ((double) curenchant / 2))
						{
							count++;
						}
						
						final int val2 = curtype2 / 2;
						if (val2 != ((double) curtype2 / 2))
						{
							count++;
						}
						
						curenchant = val;
						curtype2 = val2;
					}
					
					if (count == 5)
					{
						count1++;
					}
					else if (count == 4)
					{
						count2++;
					}
					else if (count == 3)
					{
						count3++;
					}
					else if (count > 0)
					{
						count4++;
					}
				}
				rset.close();
				statement.close();
			}
			catch (SQLException e)
			{
				LOGGER.warning("Lottery: Could restore lottery data: " + e);
			}
			
			final int prize4 = count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
			int prize1 = 0;
			int prize2 = 0;
			int prize3 = 0;
			if (count1 > 0)
			{
				prize1 = (int) (((_prize - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE) / count1);
			}
			
			if (count2 > 0)
			{
				prize2 = (int) (((_prize - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE) / count2);
			}
			
			if (count3 > 0)
			{
				prize3 = (int) (((_prize - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE) / count3);
			}
			
			final int newprize = _prize - (prize1 + prize2 + prize3 + prize4);
			SystemMessage sm;
			if (count1 > 0)
			{
				// There are winners.
				sm = new SystemMessage(SystemMessageId.THE_PRIZE_AMOUNT_FOR_THE_WINNER_OF_LOTTERY_S1_IS_S2_ADENA_WE_HAVE_S3_FIRST_PRIZE_WINNERS);
				sm.addNumber(_number);
				sm.addNumber(_prize);
				sm.addNumber(count1);
				Announcements.getInstance().announceToAll(sm);
			}
			else
			{
				// There are no winners.
				sm = new SystemMessage(SystemMessageId.THE_PRIZE_AMOUNT_FOR_LUCKY_LOTTERY_S1_IS_S2_ADENA_THERE_WAS_NO_FIRST_PRIZE_WINNER_IN_THIS_DRAWING_THEREFORE_THE_JACKPOT_WILL_BE_ADDED_TO_THE_NEXT_DRAWING);
				sm.addNumber(_number);
				sm.addNumber(_prize);
				Announcements.getInstance().announceToAll(sm);
			}
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				statement = con.prepareStatement(UPDATE_LOTTERY);
				statement.setInt(1, _prize);
				statement.setInt(2, newprize);
				statement.setInt(3, enchant);
				statement.setInt(4, type2);
				statement.setInt(5, prize1);
				statement.setInt(6, prize2);
				statement.setInt(7, prize3);
				statement.setInt(8, _number);
				statement.execute();
				statement.close();
			}
			catch (SQLException e)
			{
				LOGGER.warning("Lottery: Could not store finished lottery data: " + e);
			}
			
			ThreadPool.schedule(new startLottery(), MINUTE);
			_number++;
			
			_isStarted = false;
		}
	}
	
	public int[] decodeNumbers(int enchant, int type2)
	{
		final int[] res = new int[5];
		int id = 0;
		int nr = 1;
		
		while (enchant > 0)
		{
			final int val = enchant / 2;
			if (val != ((double) enchant / 2))
			{
				res[id] = nr;
				id++;
			}
			enchant /= 2;
			nr++;
		}
		
		nr = 17;
		
		while (type2 > 0)
		{
			final int val = type2 / 2;
			if (val != ((double) type2 / 2))
			{
				res[id] = nr;
				id++;
			}
			type2 /= 2;
			nr++;
		}
		
		return res;
	}
	
	public int[] checkTicket(ItemInstance item)
	{
		return checkTicket(item.getCustomType1(), item.getEnchantLevel(), item.getCustomType2());
	}
	
	public int[] checkTicket(int id, int enchant, int type2)
	{
		final int[] res =
		{
			0,
			0
		};
		
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(SELECT_LOTTERY_TICKET);
			statement.setInt(1, id);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				int curenchant = rset.getInt("number1") & enchant;
				int curtype2 = rset.getInt("number2") & type2;
				if ((curenchant == 0) && (curtype2 == 0))
				{
					rset.close();
					statement.close();
					con.close();
					return res;
				}
				
				int count = 0;
				for (int i = 1; i <= 16; i++)
				{
					final int val = curenchant / 2;
					if (val != ((double) curenchant / 2))
					{
						count++;
					}
					final int val2 = curtype2 / 2;
					if (val2 != ((double) curtype2 / 2))
					{
						count++;
					}
					curenchant = val;
					curtype2 = val2;
				}
				
				switch (count)
				{
					case 0:
					{
						break;
					}
					case 5:
					{
						res[0] = 1;
						res[1] = rset.getInt("prize1");
						break;
					}
					case 4:
					{
						res[0] = 2;
						res[1] = rset.getInt("prize2");
						break;
					}
					case 3:
					{
						res[0] = 3;
						res[1] = rset.getInt("prize3");
						break;
					}
					default:
					{
						res[0] = 4;
						res[1] = 200;
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Lottery: Could not check lottery ticket #" + id + ": " + e);
		}
		
		return res;
	}
	
	public static Lottery getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Lottery INSTANCE = new Lottery();
	}
}
