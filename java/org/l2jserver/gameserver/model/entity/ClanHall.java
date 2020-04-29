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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.instancemanager.AuctionManager;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.zone.type.ClanHallZone;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class ClanHall
{
	protected static final Logger LOGGER = Logger.getLogger(ClanHall.class.getName());
	
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;
	final int _clanHallId;
	private final List<DoorInstance> _doors = new ArrayList<>();
	private final List<StatSet> _doorDefault = new ArrayList<>();
	final String _name;
	private int _ownerId;
	private Clan _ownerClan;
	private final int _lease;
	private final String _desc;
	private final String _location;
	protected long _paidUntil;
	private ClanHallZone _zone;
	private final int _grade;
	protected final int _chRate = 604800000;
	protected boolean _isFree = true;
	private final Map<Integer, ClanHallFunction> _functions;
	protected boolean _paid;
	
	public class ClanHallFunction
	{
		final int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		final long _rate;
		long _endDate;
		protected boolean _inDebt;
		
		/**
		 * Instantiates a new clan hall function.
		 * @param type the type
		 * @param lvl the lvl
		 * @param lease the lease
		 * @param tempLease the temp lease
		 * @param rate the rate
		 * @param time the time
		 */
		public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask();
		}
		
		/**
		 * Gets the type.
		 * @return the type
		 */
		public int getType()
		{
			return _type;
		}
		
		/**
		 * Gets the lvl.
		 * @return the lvl
		 */
		public int getLvl()
		{
			return _lvl;
		}
		
		/**
		 * Gets the lease.
		 * @return the lease
		 */
		public int getLease()
		{
			return _fee;
		}
		
		/**
		 * Gets the rate.
		 * @return the rate
		 */
		public long getRate()
		{
			return _rate;
		}
		
		/**
		 * Gets the end time.
		 * @return the end time
		 */
		public long getEndTime()
		{
			return _endDate;
		}
		
		/**
		 * Sets the lvl.
		 * @param lvl the new lvl
		 */
		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}
		
		/**
		 * Sets the lease.
		 * @param lease the new lease
		 */
		public void setLease(int lease)
		{
			_fee = lease;
		}
		
		/**
		 * Sets the end time.
		 * @param time the new end time
		 */
		public void setEndTime(long time)
		{
			_endDate = time;
		}
		
		/**
		 * Initialize task.
		 */
		private void initializeTask()
		{
			if (_isFree)
			{
				return;
			}
			
			final long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
			{
				ThreadPool.schedule(new FunctionTask(), _endDate - currentTime);
			}
			else
			{
				ThreadPool.schedule(new FunctionTask(), 0);
			}
		}
		
		private class FunctionTask implements Runnable
		{
			@Override
			public void run()
			{
				try
				{
					if (_isFree)
					{
						return;
					}
					
					if (getOwnerClan().getWarehouse().getAdena() >= _fee)
					{
						int fee = _fee;
						boolean newfc = true;
						if ((_endDate == 0) || (_endDate == -1))
						{
							if (_endDate == -1)
							{
								newfc = false;
								fee = _tempFee;
							}
						}
						else
						{
							newfc = false;
						}
						
						setEndTime(System.currentTimeMillis() + _rate);
						dbSave(newfc);
						getOwnerClan().getWarehouse().destroyItemByItemId("CH_function_fee", 57, fee, null, null);
						ThreadPool.schedule(new FunctionTask(), _rate);
					}
					else
					{
						removeFunction(_type);
					}
				}
				catch (Throwable t)
				{
				}
			}
		}
		
		/**
		 * Db save.
		 * @param newFunction the new function
		 */
		public void dbSave(boolean newFunction)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				PreparedStatement statement;
				if (newFunction)
				{
					statement = con.prepareStatement("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
					statement.setInt(1, _clanHallId);
					statement.setInt(2, _type);
					statement.setInt(3, _lvl);
					statement.setInt(4, _fee);
					statement.setLong(5, _rate);
					statement.setLong(6, _endDate);
				}
				else
				{
					statement = con.prepareStatement("UPDATE clanhall_functions SET lvl=?, lease=?, endTime=? WHERE hall_id=? AND type=?");
					statement.setInt(1, _lvl);
					statement.setInt(2, _fee);
					statement.setLong(3, _endDate);
					statement.setInt(4, _clanHallId);
					statement.setInt(5, _type);
				}
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage());
			}
		}
	}
	
	/**
	 * Instantiates a new clan hall.
	 * @param clanHallId the clan hall id
	 * @param name the name
	 * @param ownerId the owner id
	 * @param lease the lease
	 * @param desc the desc
	 * @param location the location
	 * @param paidUntil the paid until
	 * @param grade the grade
	 * @param paid the paid
	 */
	public ClanHall(int clanHallId, String name, int ownerId, int lease, String desc, String location, long paidUntil, int grade, boolean paid)
	{
		_clanHallId = clanHallId;
		_name = name;
		_ownerId = ownerId;
		_lease = lease;
		_desc = desc;
		_location = location;
		_paidUntil = paidUntil;
		_grade = grade;
		_paid = paid;
		loadDoor();
		_functions = new HashMap<>();
		if (ownerId != 0)
		{
			_isFree = false;
			initialyzeTask(false);
			loadFunctions();
		}
	}
	
	/**
	 * Return if clanHall is paid or not.
	 * @return the paid
	 */
	public boolean getPaid()
	{
		return _paid;
	}
	
	/**
	 * Return Id Of Clan hall.
	 * @return the id
	 */
	public int getId()
	{
		return _clanHallId;
	}
	
	/**
	 * Return name.
	 * @return the name
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Return OwnerId.
	 * @return the owner id
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	/**
	 * Return lease.
	 * @return the lease
	 */
	public int getLease()
	{
		return _lease;
	}
	
	/**
	 * Return Desc.
	 * @return the desc
	 */
	public String getDesc()
	{
		return _desc;
	}
	
	/**
	 * Return Location.
	 * @return the location
	 */
	public String getLocation()
	{
		return _location;
	}
	
	/**
	 * Return PaidUntil.
	 * @return the paid until
	 */
	public long getPaidUntil()
	{
		return _paidUntil;
	}
	
	/**
	 * Return Grade.
	 * @return the grade
	 */
	public int getGrade()
	{
		return _grade;
	}
	
	/**
	 * Return all DoorInstance.
	 * @return the doors
	 */
	public List<DoorInstance> getDoors()
	{
		return _doors;
	}
	
	/**
	 * Return Door.
	 * @param doorId the door id
	 * @return the door
	 */
	public DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
		{
			return null;
		}
		
		for (int i = 0; i < getDoors().size(); i++)
		{
			final DoorInstance door = getDoors().get(i);
			if (door.getDoorId() == doorId)
			{
				return door;
			}
		}
		return null;
	}
	
	/**
	 * Return function with id.
	 * @param type the type
	 * @return the function
	 */
	public ClanHallFunction getFunction(int type)
	{
		return _functions.get(type);
	}
	
	/**
	 * Sets this clan halls zone.
	 * @param zone the new zone
	 */
	public void setZone(ClanHallZone zone)
	{
		_zone = zone;
	}
	
	/**
	 * Returns the zone of this clan hall.
	 * @return the zone
	 */
	public ClanHallZone getZone()
	{
		return _zone;
	}
	
	/**
	 * Free this clan hall.
	 */
	public void free()
	{
		_ownerId = 0;
		_isFree = true;
		for (Map.Entry<Integer, ClanHallFunction> fc : _functions.entrySet())
		{
			removeFunction(fc.getKey());
		}
		
		_functions.clear();
		_paidUntil = 0;
		_paid = false;
		updateDb();
	}
	
	/**
	 * Set owner if clan hall is free.
	 * @param clan the new owner
	 */
	public void setOwner(Clan clan)
	{
		// Verify that this ClanHall is Free and Clan isn't null
		if ((_ownerId > 0) || (clan == null))
		{
			return;
		}
		
		_ownerId = clan.getClanId();
		_isFree = false;
		_paidUntil = System.currentTimeMillis();
		initialyzeTask(true);
		
		// Annonce to Online member new ClanHall
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}
	
	/**
	 * Gets the owner clan.
	 * @return the owner clan
	 */
	public Clan getOwnerClan()
	{
		if (_ownerId == 0)
		{
			return null;
		}
		
		if (_ownerClan == null)
		{
			_ownerClan = ClanTable.getInstance().getClan(getOwnerId());
		}
		
		return _ownerClan;
	}
	
	/**
	 * Respawn all doors.
	 */
	public void spawnDoor()
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			DoorInstance door = getDoors().get(i);
			if (door.getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorData.createDoor(_doorDefault.get(i));
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (door.isOpen())
			{
				door.closeMe();
			}
			
			door.setCurrentHp(door.getMaxHp());
		}
	}
	
	/**
	 * Open or Close Door.
	 * @param player the player
	 * @param doorId the door id
	 * @param open the open
	 */
	public void openCloseDoor(PlayerInstance player, int doorId, boolean open)
	{
		if ((player != null) && (player.getClanId() == _ownerId))
		{
			openCloseDoor(doorId, open);
		}
	}
	
	/**
	 * Open close door.
	 * @param doorId the door id
	 * @param open the open
	 */
	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}
	
	/**
	 * Open close door.
	 * @param door the door
	 * @param open the open
	 */
	public void openCloseDoor(DoorInstance door, boolean open)
	{
		if (door != null)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
	}
	
	/**
	 * Open close doors.
	 * @param player the player
	 * @param open the open
	 */
	public void openCloseDoors(PlayerInstance player, boolean open)
	{
		if ((player != null) && (player.getClanId() == _ownerId))
		{
			openCloseDoors(open);
		}
	}
	
	/**
	 * Open close doors.
	 * @param open the open
	 */
	public void openCloseDoors(boolean open)
	{
		for (DoorInstance door : getDoors())
		{
			if (door != null)
			{
				if (open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
			}
		}
	}
	
	/**
	 * Banish Foreigner.
	 */
	public void banishForeigners()
	{
		_zone.banishForeigners(_ownerId);
	}
	
	/**
	 * Load All Functions.
	 */
	private void loadFunctions()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from clanhall_functions where hall_id = ?");
			statement.setInt(1, _clanHallId);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_functions.put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime")));
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: ClanHall.loadFunctions(): " + e.getMessage());
		}
	}
	
	/**
	 * Remove function In List and in DB.
	 * @param functionType the function type
	 */
	public void removeFunction(int functionType)
	{
		_functions.remove(functionType);
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
			statement.setInt(1, _clanHallId);
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage());
		}
	}
	
	/**
	 * Update Function.
	 * @param type the type
	 * @param lvl the lvl
	 * @param lease the lease
	 * @param rate the rate
	 * @param addNew the add new
	 * @return true, if successful
	 */
	public boolean updateFunctions(int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (addNew)
		{
			if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() < lease)
			{
				return false;
			}
			_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0));
		}
		else if ((lvl == 0) && (lease == 0))
		{
			removeFunction(type);
		}
		else
		{
			final int diffLease = lease - _functions.get(type).getLease();
			if (diffLease > 0)
			{
				if (ClanTable.getInstance().getClan(_ownerId).getWarehouse().getAdena() < diffLease)
				{
					return false;
				}
				
				_functions.remove(type);
				_functions.put(type, new ClanHallFunction(type, lvl, lease, diffLease, rate, -1));
			}
			else
			{
				_functions.get(type).setLease(lease);
				_functions.get(type).setLvl(lvl);
				_functions.get(type).dbSave(false);
			}
		}
		return true;
	}
	
	/**
	 * Update DB.
	 */
	public void updateDb()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
			statement.setInt(1, _ownerId);
			statement.setLong(2, _paidUntil);
			statement.setInt(3, _paid ? 1 : 0);
			statement.setInt(4, _clanHallId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Initialize Fee Task.
	 * @param forced the forced
	 */
	private void initialyzeTask(boolean forced)
	{
		final long currentTime = System.currentTimeMillis();
		if (_paidUntil > currentTime)
		{
			ThreadPool.schedule(new FeeTask(), _paidUntil - currentTime);
		}
		else if (!_paid && !forced)
		{
			if ((System.currentTimeMillis() + (1000 * 60 * 60 * 24)) <= (_paidUntil + _chRate))
			{
				ThreadPool.schedule(new FeeTask(), System.currentTimeMillis() + (1000 * 60 * 60 * 24));
			}
			else
			{
				ThreadPool.schedule(new FeeTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
			}
		}
		else
		{
			ThreadPool.schedule(new FeeTask(), 0);
		}
	}
	
	/**
	 * Fee Task.
	 */
	protected class FeeTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (_isFree)
				{
					return;
				}
				
				final Clan clan = ClanTable.getInstance().getClan(getOwnerId());
				if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
				{
					if (_paidUntil != 0)
					{
						while (_paidUntil < System.currentTimeMillis())
						{
							_paidUntil += _chRate;
						}
					}
					else
					{
						_paidUntil = System.currentTimeMillis() + _chRate;
					}
					
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
					ThreadPool.schedule(new FeeTask(), _paidUntil - System.currentTimeMillis());
					_paid = true;
					updateDb();
				}
				else
				{
					_paid = false;
					if (System.currentTimeMillis() > (_paidUntil + _chRate))
					{
						if (ClanHallManager.getInstance().loaded())
						{
							AuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
						}
						else
						{
							ThreadPool.schedule(new FeeTask(), 3000);
						}
					}
					else
					{
						updateDb();
						final SystemMessage sm = new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addNumber(getLease());
						clan.broadcastToOnlineMembers(sm);
						
						if ((System.currentTimeMillis() + (1000 * 60 * 60 * 24)) <= (_paidUntil + _chRate))
						{
							ThreadPool.schedule(new FeeTask(), System.currentTimeMillis() + (1000 * 60 * 60 * 24));
						}
						else
						{
							ThreadPool.schedule(new FeeTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
						}
					}
				}
			}
			catch (Exception t)
			{
				LOGGER.warning(t.toString());
			}
		}
	}
	
	/**
	 * Load door.
	 */
	private void loadDoor()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Select * from castle_door where castleId = ?");
			statement.setInt(1, _clanHallId);
			final ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				// Create set of the door default for use when respawning dead doors
				final StatSet set = new StatSet();
				set.set("name", rs.getString("name"));
				set.set("id", rs.getInt("id"));
				set.set("x", rs.getInt("x"));
				set.set("y", rs.getInt("y"));
				set.set("z", rs.getInt("z"));
				set.set("xMin", rs.getInt("range_xmin"));
				set.set("yMin", rs.getInt("range_ymin"));
				set.set("zMin", rs.getInt("range_zmin"));
				set.set("xMax", rs.getInt("range_xmax"));
				set.set("yMax", rs.getInt("range_ymax"));
				set.set("zMax", rs.getInt("range_zmax"));
				set.set("hp", rs.getInt("hp"));
				set.set("pDef", rs.getInt("pDef"));
				set.set("mDef", rs.getInt("mDef"));
				_doorDefault.add(set);
				
				final DoorInstance door = DoorData.createDoor(set);
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				_doors.add(door);
				DoorData.getInstance().putDoor(door);
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
}
