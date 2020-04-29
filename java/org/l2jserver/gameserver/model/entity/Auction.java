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
package org.l2jserver.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.instancemanager.AuctionManager;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;

public class Auction
{
	protected static final Logger LOGGER = Logger.getLogger(Auction.class.getName());
	
	public static final long MAX_ADENA = 99900000000L;
	private static final int ADENA_ID = 57;
	private int _id = 0;
	private long _endDate;
	private int _highestBidderId = 0;
	private String _highestBidderName = "";
	private int _highestBidderMaxBid = 0;
	private int _itemId = 0;
	private String _itemName = "";
	private int _itemObjectId = 0;
	private final int _itemQuantity = 0;
	private String _itemType = "";
	private int _sellerId = 0;
	private String _sellerClanName = "";
	private String _sellerName = "";
	private int _currentBid = 0;
	private int _startingBid = 0;
	private final Map<Integer, Bidder> _bidders = new HashMap<>();
	
	private static final String[] ItemTypeName =
	{
		"ClanHall"
	};
	
	public enum ItemTypeEnum
	{
		ClanHall
	}
	
	public class Bidder
	{
		private final String _name;
		private final String _clanName;
		private int _bid;
		private final Calendar _timeBid;
		
		/**
		 * Instantiates a new bidder.
		 * @param name the name
		 * @param clanName the clan name
		 * @param bid the bid
		 * @param timeBid the time bid
		 */
		public Bidder(String name, String clanName, int bid, long timeBid)
		{
			_name = name;
			_clanName = clanName;
			_bid = bid;
			_timeBid = Calendar.getInstance();
			_timeBid.setTimeInMillis(timeBid);
		}
		
		/**
		 * Gets the name.
		 * @return the name
		 */
		public String getName()
		{
			return _name;
		}
		
		/**
		 * Gets the clan name.
		 * @return the clan name
		 */
		public String getClanName()
		{
			return _clanName;
		}
		
		/**
		 * Gets the bid.
		 * @return the bid
		 */
		public int getBid()
		{
			return _bid;
		}
		
		/**
		 * Gets the time bid.
		 * @return the time bid
		 */
		public Calendar getTimeBid()
		{
			return _timeBid;
		}
		
		/**
		 * Sets the time bid.
		 * @param timeBid the new time bid
		 */
		public void setTimeBid(long timeBid)
		{
			_timeBid.setTimeInMillis(timeBid);
		}
		
		/**
		 * Sets the bid.
		 * @param bid the new bid
		 */
		public void setBid(int bid)
		{
			_bid = bid;
		}
	}
	
	public class AutoEndTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				endAuction();
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	public Auction(int auctionId)
	{
		_id = auctionId;
		load();
		startAutoTask();
	}
	
	/**
	 * Instantiates a new auction.
	 * @param itemId the item id
	 * @param clan the clan
	 * @param delay the delay
	 * @param bid the bid
	 * @param name the name
	 */
	public Auction(int itemId, Clan clan, long delay, int bid, String name)
	{
		_id = itemId;
		_endDate = System.currentTimeMillis() + delay;
		_itemId = itemId;
		_itemName = name;
		_itemType = "ClanHall";
		_sellerId = clan.getLeaderId();
		_sellerName = clan.getLeaderName();
		_sellerClanName = clan.getName();
		_startingBid = bid;
	}
	
	/**
	 * Load auctions.
	 */
	private void load()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from auction where id = ?");
			statement.setInt(1, _id);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_currentBid = rs.getInt("currentBid");
				_endDate = rs.getLong("endDate");
				_itemId = rs.getInt("itemId");
				_itemName = rs.getString("itemName");
				_itemObjectId = rs.getInt("itemObjectId");
				_itemType = rs.getString("itemType");
				_sellerId = rs.getInt("sellerId");
				_sellerClanName = rs.getString("sellerClanName");
				_sellerName = rs.getString("sellerName");
				_startingBid = rs.getInt("startingBid");
			}
			
			rs.close();
			statement.close();
			loadBid();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	private void loadBid()
	{
		_highestBidderId = 0;
		_highestBidderName = "";
		_highestBidderMaxBid = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC");
			statement.setInt(1, _id);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				if (rs.isFirst())
				{
					_highestBidderId = rs.getInt("bidderId");
					_highestBidderName = rs.getString("bidderName");
					_highestBidderMaxBid = rs.getInt("maxBid");
				}
				_bidders.put(rs.getInt("bidderId"), new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid")));
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Task Manage.
	 */
	private void startAutoTask()
	{
		final long currentTime = System.currentTimeMillis();
		long taskDelay = 0;
		if (_endDate <= currentTime)
		{
			_endDate = currentTime + (7 * 24 * 60 * 60 * 1000);
			saveAuctionDate();
		}
		else
		{
			taskDelay = _endDate - currentTime;
		}
		
		ThreadPool.schedule(new AutoEndTask(), taskDelay);
	}
	
	/**
	 * Gets the item type name.
	 * @param value the value
	 * @return the item type name
	 */
	public static String getItemTypeName(ItemTypeEnum value)
	{
		return ItemTypeName[value.ordinal()];
	}
	
	/**
	 * Save Auction Data End.
	 */
	private void saveAuctionDate()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Update auction set endDate = ? where id = ?");
			statement.setLong(1, _endDate);
			statement.setInt(2, _id);
			statement.execute();
			
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: saveAuctionDate(): " + e.getMessage());
		}
	}
	
	/**
	 * Set a bid.
	 * @param bidder the bidder
	 * @param bid the bid
	 */
	public synchronized void setBid(PlayerInstance bidder, int bid)
	{
		int requiredAdena = bid;
		if (_highestBidderName.equals(bidder.getClan().getLeaderName()))
		{
			requiredAdena = bid - _highestBidderMaxBid;
		}
		
		if ((((_highestBidderId > 0) && (bid > _highestBidderMaxBid)) || ((_highestBidderId == 0) && (bid >= _startingBid))) && takeItem(bidder, requiredAdena))
		{
			updateInDB(bidder, bid);
			bidder.getClan().setAuctionBiddedAt(_id, true);
			return;
		}
		if ((bid < _startingBid) || (bid <= _highestBidderMaxBid))
		{
			bidder.sendMessage("Bid Price must be higher");
		}
	}
	
	/**
	 * Return Item in WHC.
	 * @param clan the clan
	 * @param quantity the quantity
	 * @param penalty the penalty
	 */
	private void returnItem(String clan, int quantity, boolean penalty)
	{
		if (penalty)
		{
			quantity *= 0.9; // take 10% tax fee if needed
		}
		
		// avoid overflow on return
		final long limit = MAX_ADENA - ClanTable.getInstance().getClanByName(clan).getWarehouse().getAdena();
		quantity = (int) Math.min(quantity, limit);
		ClanTable.getInstance().getClanByName(clan).getWarehouse().addItem("Outbidded", ADENA_ID, quantity, null, null);
	}
	
	/**
	 * Take Item in WHC.
	 * @param bidder the bidder
	 * @param quantity the quantity
	 * @return true, if successful
	 */
	private boolean takeItem(PlayerInstance bidder, int quantity)
	{
		if ((bidder.getClan() != null) && (bidder.getClan().getWarehouse().getAdena() >= quantity))
		{
			bidder.getClan().getWarehouse().destroyItemByItemId("Buy", ADENA_ID, quantity, bidder, bidder);
			return true;
		}
		bidder.sendMessage("You do not have enough adena");
		return false;
	}
	
	/**
	 * Update auction in DB.
	 * @param bidder the bidder
	 * @param bid the bid
	 */
	private void updateInDB(PlayerInstance bidder, int bid)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (_bidders.get(bidder.getClanId()) != null)
			{
				statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?");
				statement.setInt(1, bidder.getClanId());
				statement.setString(2, bidder.getClan().getLeaderName());
				statement.setInt(3, bid);
				statement.setLong(4, System.currentTimeMillis());
				statement.setInt(5, _id);
				statement.setInt(6, bidder.getClanId());
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, IdFactory.getNextId());
				statement.setInt(2, _id);
				statement.setInt(3, bidder.getClanId());
				statement.setString(4, bidder.getName());
				statement.setInt(5, bid);
				statement.setString(6, bidder.getClan().getName());
				statement.setLong(7, System.currentTimeMillis());
				statement.execute();
				statement.close();
				
				if (World.getInstance().getPlayer(_highestBidderName) != null)
				{
					World.getInstance().getPlayer(_highestBidderName).sendMessage("You have been out bidded");
				}
			}
			_highestBidderId = bidder.getClanId();
			_highestBidderMaxBid = bid;
			_highestBidderName = bidder.getClan().getLeaderName();
			if (_bidders.get(_highestBidderId) == null)
			{
				_bidders.put(_highestBidderId, new Bidder(_highestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis()));
			}
			else
			{
				_bidders.get(_highestBidderId).setBid(bid);
				_bidders.get(_highestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
			}
			
			bidder.sendMessage("You have bidded successfully");
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: Auction.updateInDB(PlayerInstance bidder, int bid): " + e.getMessage());
		}
	}
	
	/**
	 * Remove bids.
	 */
	private void removeBids()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?");
			statement.setInt(1, _id);
			statement.execute();
			
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: Auction.deleteFromDB(): " + e.getMessage());
		}
		
		for (Bidder b : _bidders.values())
		{
			if (ClanTable.getInstance().getClanByName(b.getClanName()).getHasHideout() == 0)
			{
				returnItem(b.getClanName(), b.getBid(), true); // 10 % tax
			}
			else if (World.getInstance().getPlayer(b.getName()) != null)
			{
				World.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
			}
			ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
		}
		_bidders.clear();
	}
	
	/**
	 * Remove auctions.
	 */
	public void deleteAuctionFromDB()
	{
		AuctionManager.getInstance().getAuctions().remove(this);
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?");
			statement.setInt(1, _itemId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: Auction.deleteFromDB(): " + e.getMessage());
		}
	}
	
	/**
	 * End of auction.
	 */
	public void endAuction()
	{
		if (ClanHallManager.getInstance().loaded())
		{
			if ((_highestBidderId == 0) && (_sellerId == 0))
			{
				startAutoTask();
				return;
			}
			
			if ((_highestBidderId == 0) && (_sellerId > 0))
			{
				/**
				 * If seller haven't sell ClanHall, auction removed, THIS MUST BE CONFIRMED
				 */
				final int aucId = AuctionManager.getInstance().getAuctionIndex(_id);
				AuctionManager.getInstance().getAuctions().remove(aucId);
				
				return;
			}
			
			if (_sellerId > 0)
			{
				returnItem(_sellerClanName, _highestBidderMaxBid, true);
				returnItem(_sellerClanName, ClanHallManager.getInstance().getClanHallById(_itemId).getLease(), false);
			}
			
			deleteAuctionFromDB();
			final Clan clan = ClanTable.getInstance().getClanByName(_bidders.get(_highestBidderId).getClanName());
			_bidders.remove(_highestBidderId);
			clan.setAuctionBiddedAt(0, true);
			removeBids();
			ClanHallManager.getInstance().setOwner(_itemId, clan);
		}
		else
		{
			/** Task waiting ClanHallManager is loaded every 3s */
			ThreadPool.schedule(new AutoEndTask(), 3000);
		}
	}
	
	/**
	 * Cancel bid.
	 * @param bidder the bidder
	 */
	public synchronized void cancelBid(int bidder)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?");
			statement.setInt(1, _id);
			statement.setInt(2, bidder);
			statement.execute();
			
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: Auction.cancelBid(String bidder): " + e.getMessage());
		}
		
		returnItem(_bidders.get(bidder).getClanName(), _bidders.get(bidder).getBid(), true);
		ClanTable.getInstance().getClanByName(_bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
		_bidders.clear();
		loadBid();
	}
	
	/**
	 * Cancel auction.
	 */
	public void cancelAuction()
	{
		deleteAuctionFromDB();
		removeBids();
	}
	
	/**
	 * Confirm an auction.
	 */
	public void confirmAuction()
	{
		AuctionManager.getInstance().getAuctions().add(this);
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemName, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _id);
			statement.setInt(2, _sellerId);
			statement.setString(3, _sellerName);
			statement.setString(4, _sellerClanName);
			statement.setString(5, _itemType);
			statement.setInt(6, _itemId);
			statement.setInt(7, _itemObjectId);
			statement.setString(8, _itemName);
			statement.setInt(9, _itemQuantity);
			statement.setInt(10, _startingBid);
			statement.setInt(11, _currentBid);
			statement.setLong(12, _endDate);
			statement.execute();
			statement.close();
			loadBid();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: Auction.load(): " + e.getMessage());
		}
	}
	
	/**
	 * Get var auction.
	 * @return the id
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * Gets the current bid.
	 * @return the current bid
	 */
	public int getCurrentBid()
	{
		return _currentBid;
	}
	
	/**
	 * Gets the end date.
	 * @return the end date
	 */
	public long getEndDate()
	{
		return _endDate;
	}
	
	/**
	 * Gets the highest bidder id.
	 * @return the highest bidder id
	 */
	public int getHighestBidderId()
	{
		return _highestBidderId;
	}
	
	/**
	 * Gets the highest bidder name.
	 * @return the highest bidder name
	 */
	public String getHighestBidderName()
	{
		return _highestBidderName;
	}
	
	/**
	 * Gets the highest bidder max bid.
	 * @return the highest bidder max bid
	 */
	public int getHighestBidderMaxBid()
	{
		return _highestBidderMaxBid;
	}
	
	/**
	 * Gets the item id.
	 * @return the item id
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Gets the item name.
	 * @return the item name
	 */
	public String getItemName()
	{
		return _itemName;
	}
	
	/**
	 * Gets the item object id.
	 * @return the item object id
	 */
	public int getItemObjectId()
	{
		return _itemObjectId;
	}
	
	/**
	 * Gets the item quantity.
	 * @return the item quantity
	 */
	public int getItemQuantity()
	{
		return _itemQuantity;
	}
	
	/**
	 * Gets the item type.
	 * @return the item type
	 */
	public String getItemType()
	{
		return _itemType;
	}
	
	/**
	 * Gets the seller id.
	 * @return the seller id
	 */
	public int getSellerId()
	{
		return _sellerId;
	}
	
	/**
	 * Gets the seller name.
	 * @return the seller name
	 */
	public String getSellerName()
	{
		return _sellerName;
	}
	
	/**
	 * Gets the seller clan name.
	 * @return the seller clan name
	 */
	public String getSellerClanName()
	{
		return _sellerClanName;
	}
	
	/**
	 * Gets the starting bid.
	 * @return the starting bid
	 */
	public int getStartingBid()
	{
		return _startingBid;
	}
	
	/**
	 * Gets the bidders.
	 * @return the bidders
	 */
	public Map<Integer, Bidder> getBidders()
	{
		return _bidders;
	}
}
