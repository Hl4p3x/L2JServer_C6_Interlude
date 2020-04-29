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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.datatables.xml.ManorSeedData;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.CastleManorManager;
import org.l2jserver.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jserver.gameserver.instancemanager.CastleManorManager.SeedProduction;
import org.l2jserver.gameserver.instancemanager.CrownManager;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.model.zone.type.CastleTeleportZone;
import org.l2jserver.gameserver.model.zone.type.CastleZone;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;

public class Castle
{
	protected static final Logger LOGGER = Logger.getLogger(Castle.class.getName());
	
	private List<CropProcure> _procure = new ArrayList<>();
	private List<SeedProduction> _production = new ArrayList<>();
	private List<CropProcure> _procureNext = new ArrayList<>();
	private List<SeedProduction> _productionNext = new ArrayList<>();
	private boolean _isNextPeriodApproved = false;
	
	private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
	private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
	private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
	
	private int _castleId = 0;
	private final List<DoorInstance> _doors = new ArrayList<>();
	private final List<StatSet> _doorDefault = new ArrayList<>();
	private String _name = "";
	private int _ownerId = 0;
	private Siege _siege = null;
	private Calendar _siegeDate;
	private int _siegeDayOfWeek = 7; // Default to saturday
	private int _siegeHourOfDay = 20; // Default to 8 pm server time
	private int _taxPercent = 0;
	private double _taxRate = 0;
	private int _treasury = 0;
	private CastleZone _zone;
	private CastleTeleportZone _teleZone;
	private Clan _formerOwner = null;
	private int _nbArtifact = 1;
	private final int[] _gate =
	{
		Integer.MIN_VALUE,
		0,
		0
	};
	private final Map<Integer, Integer> _engrave = new HashMap<>();
	
	public Castle(int castleId)
	{
		_castleId = castleId;
		if ((_castleId == 7) || (castleId == 9))
		{
			_nbArtifact = 2;
		}
		load();
		loadDoor();
	}
	
	public void Engrave(Clan clan, int objId)
	{
		_engrave.put(objId, clan.getClanId());
		
		if (_engrave.size() == _nbArtifact)
		{
			boolean rst = true;
			for (int id : _engrave.values())
			{
				if (id != clan.getClanId())
				{
					rst = false;
				}
			}
			
			if (rst)
			{
				_engrave.clear();
				setOwner(clan);
			}
			else
			{
				getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
			}
		}
		else
		{
			getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
		}
	}
	
	/**
	 * Add amount to castle instance's treasury (warehouse).
	 * @param amount
	 */
	public void addToTreasury(int amount)
	{
		if (_ownerId <= 0)
		{
			return;
		}
		
		if (_name.equalsIgnoreCase("Schuttgart") || _name.equalsIgnoreCase("Goddard"))
		{
			final Castle rune = CastleManager.getInstance().getCastle("rune");
			if (rune != null)
			{
				final int runeTax = (int) (amount * rune.getTaxRate());
				if (rune.getOwnerId() > 0)
				{
					rune.addToTreasury(runeTax);
				}
				
				amount -= runeTax;
			}
		}
		if (!_name.equalsIgnoreCase("aden") && !_name.equalsIgnoreCase("Rune") && !_name.equalsIgnoreCase("Schuttgart") && !_name.equalsIgnoreCase("Goddard")) // If current castle instance is not Aden, Rune, Goddard or Schuttgart.
		{
			final Castle aden = CastleManager.getInstance().getCastle("aden");
			if (aden != null)
			{
				final int adenTax = (int) (amount * aden.getTaxRate()); // Find out what Aden gets from the current castle instance's income
				if (aden.getOwnerId() > 0)
				{
					aden.addToTreasury(adenTax); // Only bother to really add the tax to the treasury if not npc owned
				}
				
				amount -= adenTax; // Subtract Aden's income from current castle instance's income
			}
		}
		
		addToTreasuryNoTax(amount);
	}
	
	/**
	 * Add amount to castle instance's treasury (warehouse), no tax paying.
	 * @param amount
	 * @return
	 */
	public boolean addToTreasuryNoTax(int amount)
	{
		if (_ownerId <= 0)
		{
			return false;
		}
		
		if (amount < 0)
		{
			amount *= -1;
			if (_treasury < amount)
			{
				return false;
			}
			
			_treasury -= amount;
		}
		else if (((long) _treasury + amount) > Integer.MAX_VALUE)
		{
			_treasury = Integer.MAX_VALUE;
		}
		else
		{
			_treasury += amount;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Update castle set treasury = ? where id = ?");
			statement.setInt(1, _treasury);
			statement.setInt(2, _castleId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
		return true;
	}
	
	/**
	 * Move non clan members off castle area and to nearest town.
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
	 * Sets this castles zone
	 * @param zone
	 */
	public void setZone(CastleZone zone)
	{
		_zone = zone;
	}
	
	public CastleZone getZone()
	{
		return _zone;
	}
	
	public void setTeleZone(CastleTeleportZone zone)
	{
		_teleZone = zone;
	}
	
	public CastleTeleportZone getTeleZone()
	{
		return _teleZone;
	}
	
	/**
	 * Get the objects distance to this castle
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
	
	// This method is used to begin removing all castle upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
	}
	
	// This method updates the castle tax rate
	public void setOwner(Clan clan)
	{
		// Remove old owner
		if ((_ownerId > 0) && ((clan == null) || (clan.getClanId() != _ownerId)))
		{
			final Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
			if (oldOwner != null)
			{
				if (_formerOwner == null)
				{
					_formerOwner = oldOwner;
					if (Config.REMOVE_CASTLE_CIRCLETS)
					{
						CastleManager.getInstance().removeCirclet(_formerOwner, getCastleId());
					}
				}
				oldOwner.setHasCastle(0); // Unset has castle flag for old owner
				Announcements.getInstance().announceToAll(oldOwner.getName() + " has lost " + getName() + " castle!");
				
				// remove crowns
				CrownManager.getInstance().checkCrowns(oldOwner);
			}
		}
		
		updateOwnerInDB(clan); // Update in database
		if (getSiege().isInProgress())
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}
		
		updateClansReputation();
	}
	
	public void removeOwner(Clan clan)
	{
		if (clan != null)
		{
			_formerOwner = clan;
			if (Config.REMOVE_CASTLE_CIRCLETS)
			{
				CastleManager.getInstance().removeCirclet(_formerOwner, getCastleId());
			}
			
			clan.setHasCastle(0);
			
			Announcements.getInstance().announceToAll(clan.getName() + " has lost " + getName() + " castle");
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}
		
		updateOwnerInDB(null);
		
		if (getSiege().isInProgress())
		{
			getSiege().midVictory();
		}
		
		updateClansReputation();
	}
	
	// This method updates the castle tax rate
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
		
		setTaxPercent(taxPercent);
		player.sendMessage(_name + " castle tax changed to " + taxPercent + "%.");
	}
	
	public void setTaxPercent(int taxPercent)
	{
		_taxPercent = taxPercent;
		_taxRate = _taxPercent / 100.0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Update castle set taxPercent = ? where id = ?");
			statement.setInt(1, taxPercent);
			statement.setInt(2, _castleId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Respawn all doors on castle grounds.
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	/**
	 * Respawn all doors on castle grounds
	 * @param isDoorWeak
	 */
	public void spawnDoor(boolean isDoorWeak)
	{
		for (int i = 0; i < _doors.size(); i++)
		{
			DoorInstance door = _doors.get(i);
			if (door.getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorData.createDoor(_doorDefault.get(i));
				if (isDoorWeak)
				{
					door.setCurrentHpDirect(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHpDirect(door.getMaxHp());
				}
				
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				_doors.set(i, door);
			}
			else if (door.isOpen())
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
			door.setCurrentHpDirect(door.getMaxHp() + hp);
			saveDoorUpgrade(doorId, hp, pDef, mDef);
		}
	}
	
	private void load()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from castle where id = ?");
			statement.setInt(1, _castleId);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_name = rs.getString("name");
				// _OwnerId = rs.getInt("ownerId");
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
				
				_taxPercent = rs.getInt("taxPercent");
				_treasury = rs.getInt("treasury");
			}
			
			rs.close();
			statement.close();
			statement = null;
			rs = null;
			_taxRate = _taxPercent / 100.0;
			statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?");
			statement.setInt(1, _castleId);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_ownerId = rs.getInt("clan_id");
			}
			
			if (_ownerId > 0)
			{
				final Clan clan = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
				ThreadPool.schedule(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	// This method loads castle door data from database
	private void loadDoor()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Select * from castle_door where castleId = ?");
			statement.setInt(1, _castleId);
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
	
	// This method loads castle door upgrade data from database
	private void loadDoorUpgrade()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Select * from castle_doorupgrade where doorId in (Select Id from castle_door where castleId = ?)");
			statement.setInt(1, _castleId);
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
			LOGGER.warning(e.toString());
		}
	}
	
	private void removeDoorUpgrade()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("delete from castle_doorupgrade where doorId in (select id from castle_door where castleId=?)");
			statement.setInt(1, _castleId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
			statement.setInt(1, doorId);
			statement.setInt(2, hp);
			statement.setInt(3, pDef);
			statement.setInt(4, mDef);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
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
			
			// ============================================================================
			// NEED TO REMOVE HAS CASTLE FLAG FROM CLAN_DATA
			// SHOULD BE CHECKED FROM CASTLE TABLE
			statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
			statement.setInt(1, _castleId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
			statement.setInt(1, _castleId);
			statement.setInt(2, _ownerId);
			statement.execute();
			statement.close();
			// ============================================================================
			
			// Announce to clan memebers
			if (clan != null)
			{
				clan.setHasCastle(_castleId); // Set has castle flag for new owner
				Announcements.getInstance().announceToAll(clan.getName() + " has taken " + getName() + " castle!");
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory"));
				// give crowns
				CrownManager.getInstance().checkCrowns(clan);
				
				ThreadPool.schedule(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	public int getCastleId()
	{
		return _castleId;
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
	
	public String getName()
	{
		return _name;
	}
	
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	public Siege getSiege()
	{
		if (_siege == null)
		{
			_siege = new Siege(new Castle[]
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
	
	public int getSiegeDayOfWeek()
	{
		return _siegeDayOfWeek;
	}
	
	public int getSiegeHourOfDay()
	{
		return _siegeHourOfDay;
	}
	
	public int getTaxPercent()
	{
		return _taxPercent;
	}
	
	public double getTaxRate()
	{
		return _taxRate;
	}
	
	public int getTreasury()
	{
		return _treasury;
	}
	
	public List<SeedProduction> getSeedProduction(int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? _production : _productionNext;
	}
	
	public List<CropProcure> getCropProcure(int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? _procure : _procureNext;
	}
	
	public void setSeedProduction(List<SeedProduction> seed, int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			_production = seed;
		}
		else
		{
			_productionNext = seed;
		}
	}
	
	public void setCropProcure(List<CropProcure> crop, int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			_procure = crop;
		}
		else
		{
			_procureNext = crop;
		}
	}
	
	public synchronized SeedProduction getSeed(int seedId, int period)
	{
		for (SeedProduction seed : getSeedProduction(period))
		{
			if (seed.getId() == seedId)
			{
				return seed;
			}
		}
		return null;
	}
	
	public synchronized CropProcure getCrop(int cropId, int period)
	{
		for (CropProcure crop : getCropProcure(period))
		{
			if (crop.getId() == cropId)
			{
				return crop;
			}
		}
		return null;
	}
	
	public int getManorCost(int period)
	{
		List<CropProcure> procure;
		List<SeedProduction> production;
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			procure = _procure;
			production = _production;
		}
		else
		{
			procure = _procureNext;
			production = _productionNext;
		}
		
		int total = 0;
		if (production != null)
		{
			for (SeedProduction seed : production)
			{
				total += ManorSeedData.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
			}
		}
		
		if (procure != null)
		{
			for (CropProcure crop : procure)
			{
				total += crop.getPrice() * crop.getStartAmount();
			}
		}
		
		return total;
	}
	
	// save manor production data
	public void saveSeedData()
	{
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION);
			statement.setInt(1, _castleId);
			statement.execute();
			statement.close();
			
			if (_production != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String[] values = new String[_production.size()];
				for (SeedProduction s : _production)
				{
					values[count] = "(" + _castleId + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_CURRENT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
			
			if (_productionNext != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String[] values = new String[_productionNext.size()];
				for (SeedProduction s : _productionNext)
				{
					values[count] = "(" + _castleId + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_NEXT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.info("Error adding seed production data for castle " + _name + ": " + e.getMessage());
		}
	}
	
	// save manor production data for specified period
	public void saveSeedData(int period)
	{
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION_PERIOD);
			statement.setInt(1, _castleId);
			statement.setInt(2, period);
			statement.execute();
			statement.close();
			
			List<SeedProduction> prod = null;
			prod = getSeedProduction(period);
			if (prod != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String[] values = new String[prod.size()];
				for (SeedProduction s : prod)
				{
					values[count] = "(" + _castleId + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
					count++;
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.info("Error adding seed production data for castle " + _name + ": " + e.getMessage());
		}
	}
	
	// save crop procure data
	public void saveCropData()
	{
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE);
			statement.setInt(1, _castleId);
			statement.execute();
			statement.close();
			
			if (_procure != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String[] values = new String[_procure.size()];
				for (CropProcure cp : _procure)
				{
					values[count] = "(" + _castleId + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + CastleManorManager.PERIOD_CURRENT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
			
			if (_procureNext != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String[] values = new String[_procureNext.size()];
				for (CropProcure cp : _procureNext)
				{
					values[count] = "(" + _castleId + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + CastleManorManager.PERIOD_NEXT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.info("Error adding crop data for castle " + _name + ": " + e.getMessage());
		}
	}
	
	// save crop procure data for specified period
	public void saveCropData(int period)
	{
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE_PERIOD);
			statement.setInt(1, _castleId);
			statement.setInt(2, period);
			statement.execute();
			statement.close();
			
			List<CropProcure> proc = null;
			proc = getCropProcure(period);
			if (proc != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String[] values = new String[proc.size()];
				for (CropProcure cp : proc)
				{
					values[count] = "(" + _castleId + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.info("Error adding crop data for castle " + _name + ": " + e.getMessage());
		}
	}
	
	public void updateCrop(int cropId, int amount, int period)
	{
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(CASTLE_UPDATE_CROP);
			statement.setInt(1, amount);
			statement.setInt(2, cropId);
			statement.setInt(3, _castleId);
			statement.setInt(4, period);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.info("Error adding crop data for castle " + _name + ": " + e.getMessage());
		}
	}
	
	public void updateSeed(int seedId, int amount, int period)
	{
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement(CASTLE_UPDATE_SEED);
			statement.setInt(1, amount);
			statement.setInt(2, seedId);
			statement.setInt(3, _castleId);
			statement.setInt(4, period);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.info("Error adding seed production data for castle " + _name + ": " + e.getMessage());
		}
	}
	
	public boolean isNextPeriodApproved()
	{
		return _isNextPeriodApproved;
	}
	
	public void setNextPeriodApproved(boolean value)
	{
		_isNextPeriodApproved = value;
	}
	
	public void updateClansReputation()
	{
		if (_formerOwner != null)
		{
			if (_formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
			{
				final int maxreward = Math.max(0, _formerOwner.getReputationScore());
				_formerOwner.setReputationScore(_formerOwner.getReputationScore() - 1000, true);
				
				final Clan owner = ClanTable.getInstance().getClan(getOwnerId());
				if (owner != null)
				{
					owner.setReputationScore(owner.getReputationScore() + Math.min(1000, maxreward), true);
					owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
				}
			}
			else
			{
				_formerOwner.setReputationScore(_formerOwner.getReputationScore() + 500, true);
			}
			
			_formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_formerOwner));
		}
		else
		{
			final Clan owner = ClanTable.getInstance().getClan(getOwnerId());
			if (owner != null)
			{
				owner.setReputationScore(owner.getReputationScore() + 1000, true);
				owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
			}
		}
	}
	
	public void createClanGate(int x, int y, int z)
	{
		_gate[0] = x;
		_gate[1] = y;
		_gate[2] = z;
	}
	
	/** Optimized as much as possible. */
	public void destroyClanGate()
	{
		_gate[0] = Integer.MIN_VALUE;
	}
	
	/**
	 * This method must always be called before using gate coordinate retrieval methods! Optimized as much as possible.
	 * @return is a Clan Gate available
	 */
	
	public boolean isGateOpen()
	{
		return _gate[0] != Integer.MIN_VALUE;
	}
	
	public int getGateX()
	{
		return _gate[0];
	}
	
	public int getGateY()
	{
		return _gate[1];
	}
	
	public int getGateZ()
	{
		return _gate[2];
	}
	
	public void oustAllPlayers()
	{
		_teleZone.oustAllPlayers();
	}
	
	/**
	 * @return
	 */
	public boolean isSiegeInProgress()
	{
		if (_siege != null)
		{
			return _siege.isInProgress();
		}
		return false;
	}
}
