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
package org.l2jserver.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.model.zone.type.FortZone;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;

/**
 * @author programmos
 */
public class Fort
{
	protected static final Logger LOGGER = Logger.getLogger(Fort.class.getName());
	
	private int _fortId = 0;
	private final List<DoorInstance> _doors = new ArrayList<>();
	private final List<StatSet> _doorDefault = new ArrayList<>();
	private String _name = "";
	private int _ownerId = 0;
	private Clan _fortOwner = null;
	private FortSiege _siege = null;
	private Calendar _siegeDate;
	private int _siegeDayOfWeek = 7; // Default to saturday
	private int _siegeHourOfDay = 20; // Default to 8 pm server time
	private FortZone _zone;
	private Clan _formerOwner = null;
	
	public Fort(int fortId)
	{
		_fortId = fortId;
		load();
		loadDoor();
	}
	
	public void EndOfSiege(Clan clan)
	{
		ThreadPool.schedule(new endFortressSiege(this, clan), 1000);
	}
	
	public void Engrave(Clan clan, int objId)
	{
		getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to raise the flag.", true);
		setOwner(clan);
	}
	
	/**
	 * Add amount to fort instance's treasury (warehouse).
	 * @param amount
	 */
	public void addToTreasury(int amount)
	{
		// TODO: Implement?
	}
	
	/**
	 * Add amount to fort instance's treasury (warehouse), no tax paying.
	 * @param amount
	 * @return
	 */
	public boolean addToTreasuryNoTax(int amount)
	{
		return true;
	}
	
	/**
	 * Move non clan members off fort area and to nearest town.
	 */
	public void banishForeigners()
	{
		_zone.banishForeigners(_ownerId);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Sets this forts zone
	 * @param zone
	 */
	public void setZone(FortZone zone)
	{
		_zone = zone;
	}
	
	public FortZone getZone()
	{
		return _zone;
	}
	
	/**
	 * Get the objects distance to this fort
	 * @param obj
	 * @return
	 */
	public double getDistance(WorldObject obj)
	{
		return _zone.getDistanceToZone(obj);
	}
	
	public void closeDoor(PlayerInstance player, int doorId)
	{
		openCloseDoor(player, doorId, false);
	}
	
	public void openDoor(PlayerInstance player, int doorId)
	{
		openCloseDoor(player, doorId, true);
	}
	
	public void openCloseDoor(PlayerInstance player, int doorId, boolean open)
	{
		if (player.getClanId() != _ownerId)
		{
			return;
		}
		
		final DoorInstance door = getDoor(doorId);
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
	
	// This method is used to begin removing all fort upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
	}
	
	// This method updates the fort tax rate
	public void setOwner(Clan clan)
	{
		// Remove old owner
		if ((_ownerId > 0) && ((clan == null) || (clan.getClanId() != _ownerId)))
		{
			// Try to find clan instance
			final Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());
			if (oldOwner != null)
			{
				if (_formerOwner == null)
				{
					_formerOwner = oldOwner;
				}
				
				// Unset has fort flag for old owner
				oldOwner.setHasFort(0);
				Announcements.getInstance().announceToAll(oldOwner.getName() + " has lost " + getName() + " fortress!");
			}
		}
		
		updateOwnerInDB(clan); // Update in database
		if (getSiege().isInProgress())
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}
		
		updateClansReputation();
		
		_fortOwner = clan;
	}
	
	public void removeOwner(Clan clan)
	{
		if (clan != null)
		{
			_formerOwner = clan;
			clan.setHasFort(0);
			Announcements.getInstance().announceToAll(clan.getName() + " has lost " + getName() + " fort");
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}
		
		updateOwnerInDB(null);
		
		if (getSiege().isInProgress())
		{
			getSiege().midVictory();
		}
		
		updateClansReputation();
	}
	
	// This method updates the fort tax rate
	public void setTaxPercent(PlayerInstance player, int taxPercent)
	{
		int maxTax;
		
		switch (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_DAWN:
			{
				maxTax = 25;
				break;
			}
			case SevenSigns.CABAL_DUSK:
			{
				maxTax = 5;
				break;
			}
			default: // no owner
			{
				maxTax = 15;
			}
		}
		
		if ((taxPercent < 0) || (taxPercent > maxTax))
		{
			player.sendMessage("Tax value must be between 0 and " + maxTax + ".");
			return;
		}
		
		player.sendMessage(_name + " fort tax changed to " + taxPercent + "%.");
	}
	
	/**
	 * Respawn all doors on fort grounds.
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	/**
	 * Respawn all doors on fort grounds
	 * @param isDoorWeak
	 */
	public void spawnDoor(boolean isDoorWeak)
	{
		for (int i = 0; i < _doors.size(); i++)
		{
			DoorInstance door = _doors.get(i);
			if (door.getCurrentHp() >= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorData.createDoor(_doorDefault.get(i));
				if (isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHp(door.getMaxHp());
				}
				
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				_doors.set(i, door);
			}
			else if (!door.isOpen())
			{
				door.closeMe();
			}
		}
		
		loadDoorUpgrade(); // Check for any upgrade the doors may have
	}
	
	// This method upgrade door
	public void upgradeDoor(int doorId, int hp, int pDef, int mDef)
	{
		final DoorInstance door = getDoor(doorId);
		if (door == null)
		{
			return;
		}
		
		if (door.getDoorId() == doorId)
		{
			door.setCurrentHp(door.getMaxHp() + hp);
			saveDoorUpgrade(doorId, hp, pDef, mDef);
		}
	}
	
	// This method loads fort
	private void load()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from fort where id = ?");
			statement.setInt(1, _fortId);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_name = rs.getString("name");
				_siegeDate = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				
				_siegeDayOfWeek = rs.getInt("siegeDayOfWeek");
				if ((_siegeDayOfWeek < 1) || (_siegeDayOfWeek > 7))
				{
					_siegeDayOfWeek = 7;
				}
				
				_siegeHourOfDay = rs.getInt("siegeHourOfDay");
				if ((_siegeHourOfDay < 0) || (_siegeHourOfDay > 23))
				{
					_siegeHourOfDay = 20;
				}
				
				_ownerId = rs.getInt("owner");
			}
			
			rs.close();
			statement.close();
			
			if (_ownerId > 0)
			{
				final Clan clan = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
				if (clan != null)
				{
					clan.setHasFort(_fortId);
					_fortOwner = clan;
				}
			}
			else
			{
				_fortOwner = null;
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: loadFortData(): " + e);
		}
	}
	
	// This method loads fort door data from database
	private void loadDoor()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Select * from fort_door where fortId = ?");
			statement.setInt(1, _fortId);
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
			LOGGER.warning("Exception: loadFortDoor(): " + e);
		}
	}
	
	// This method loads fort door upgrade data from database
	private void loadDoorUpgrade()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Select * from fort_doorupgrade where doorId in (Select Id from fort_door where fortId = ?)");
			statement.setInt(1, _fortId);
			final ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: loadFortDoorUpgrade(): " + e);
		}
	}
	
	private void removeDoorUpgrade()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("delete from fort_doorupgrade where doorId in (select id from fort_door where fortId=?)");
			statement.setInt(1, _fortId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: removeDoorUpgrade(): " + e);
		}
	}
	
	private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO fort_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
			statement.setInt(1, doorId);
			statement.setInt(2, hp);
			statement.setInt(3, pDef);
			statement.setInt(4, mDef);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e);
		}
	}
	
	private void updateOwnerInDB(Clan clan)
	{
		if (clan != null)
		{
			_ownerId = clan.getClanId(); // Update owner id property
		}
		else
		{
			_ownerId = 0; // Remove owner
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE fort SET owner=? where id = ?");
			statement.setInt(1, _ownerId);
			statement.setInt(2, _fortId);
			statement.execute();
			statement.close();
			
			// Announce to clan memebers
			if (clan != null)
			{
				clan.setHasFort(_fortId); // Set has fort flag for new owner
				Announcements.getInstance().announceToAll(clan.getName() + " has taken " + getName() + " fort!");
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory"));
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: updateOwnerInDB(Pledge clan): " + e.getMessage());
		}
	}
	
	public int getFortId()
	{
		return _fortId;
	}
	
	public Clan getOwnerClan()
	{
		return _fortOwner;
	}
	
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	public DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
		{
			return null;
		}
		
		for (int i = 0; i < _doors.size(); i++)
		{
			final DoorInstance door = _doors.get(i);
			if (door.getDoorId() == doorId)
			{
				return door;
			}
		}
		return null;
	}
	
	public List<DoorInstance> getDoors()
	{
		return _doors;
	}
	
	public FortSiege getSiege()
	{
		if (_siege == null)
		{
			_siege = new FortSiege(new Fort[]
			{
				this
			});
		}
		return _siege;
	}
	
	public Calendar getSiegeDate()
	{
		return _siegeDate;
	}
	
	public void setSiegeDate(Calendar siegeDate)
	{
		_siegeDate = siegeDate;
	}
	
	public int getSiegeDayOfWeek()
	{
		return _siegeDayOfWeek;
	}
	
	public int getSiegeHourOfDay()
	{
		return _siegeHourOfDay;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void updateClansReputation()
	{
		if (_formerOwner != null)
		{
			if (_formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
			{
				final int maxreward = Math.max(0, _formerOwner.getReputationScore());
				final Clan owner = ClanTable.getInstance().getClan(getOwnerId());
				if (owner != null)
				{
					owner.setReputationScore(owner.getReputationScore() + Math.min(500, maxreward), true);
					owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
				}
			}
			else
			{
				_formerOwner.setReputationScore(_formerOwner.getReputationScore() + 250, true);
			}
			
			_formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_formerOwner));
		}
		else
		{
			final Clan owner = ClanTable.getInstance().getClan(getOwnerId());
			if (owner != null)
			{
				owner.setReputationScore(owner.getReputationScore() + 500, true);
				owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
			}
		}
	}
	
	private class endFortressSiege implements Runnable
	{
		private final Fort _f;
		private final Clan _clan;
		
		public endFortressSiege(Fort f, Clan clan)
		{
			_f = f;
			_clan = clan;
		}
		
		@Override
		public void run()
		{
			_f.Engrave(_clan, 0);
		}
	}
}
