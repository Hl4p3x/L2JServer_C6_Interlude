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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.zone.type.ClanHallZone;

/**
 * @author Steuf
 */
public class ClanHallManager
{
	private static final Logger LOGGER = Logger.getLogger(ClanHallManager.class.getName());
	
	private final Map<String, List<ClanHall>> _allClanHalls = new HashMap<>();
	private final Map<Integer, ClanHall> _clanHall = new HashMap<>();
	private final Map<Integer, ClanHall> _freeClanHall = new HashMap<>();
	private boolean _loaded = false;
	
	public boolean loaded()
	{
		return _loaded;
	}
	
	private ClanHallManager()
	{
		load();
	}
	
	private void load()
	{
		_allClanHalls.clear();
		_clanHall.clear();
		_freeClanHall.clear();
		
		LOGGER.info("Initializing ClanHallManager");
		try (Connection con = DatabaseFactory.getConnection())
		{
			int id;
			int ownerId;
			int lease;
			int grade = 0;
			String name;
			String desc;
			String location;
			long paidUntil = 0;
			boolean paid = false;
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			rs = statement.executeQuery();
			while (rs.next())
			{
				id = rs.getInt("id");
				name = rs.getString("name");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");
				desc = rs.getString("desc");
				location = rs.getString("location");
				paidUntil = rs.getLong("paidUntil");
				grade = rs.getInt("Grade");
				paid = rs.getBoolean("paid");
				
				final ClanHall ch = new ClanHall(id, name, ownerId, lease, desc, location, paidUntil, grade, paid);
				if (ownerId == 0)
				{
					_freeClanHall.put(id, ch);
				}
				else
				{
					final Clan clan = ClanTable.getInstance().getClan(ownerId);
					if (clan != null)
					{
						_clanHall.put(id, ch);
						clan.setHasHideout(id);
					}
					else
					{
						_freeClanHall.put(id, ch);
						ch.free();
						AuctionManager.getInstance().initNPC(id);
					}
				}
			}
			rs.close();
			statement.close();
			
			LOGGER.info("Loaded: " + _clanHall.size() + " clan halls");
			LOGGER.info("Loaded: " + _freeClanHall.size() + " free clan halls");
			_loaded = true;
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: ClanHallManager.load(): " + e.getMessage());
		}
	}
	
	/**
	 * @return Map with all free ClanHalls
	 */
	public Map<Integer, ClanHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}
	
	/**
	 * @return Map with all ClanHalls that have owner
	 */
	public Map<Integer, ClanHall> getClanHalls()
	{
		return _clanHall;
	}
	
	/**
	 * @param location
	 * @return Map with all ClanHalls which are in location
	 */
	public List<ClanHall> getClanHallsByLocation(String location)
	{
		if (!_allClanHalls.containsKey(location))
		{
			return null;
		}
		return _allClanHalls.get(location);
	}
	
	/**
	 * @param chId the clanHall id to check.
	 * @return true if the clanHall is free.
	 */
	public boolean isFree(int chId)
	{
		return _freeClanHall.containsKey(chId);
	}
	
	/**
	 * Free a ClanHall
	 * @param chId the id of clanHall to release.
	 */
	public synchronized void setFree(int chId)
	{
		_freeClanHall.put(chId, _clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	/**
	 * Set owner status for a clan hall.
	 * @param chId the clanHall id to make checks on.
	 * @param clan the new clan owner.
	 */
	public synchronized void setOwner(int chId, Clan clan)
	{
		if (!_clanHall.containsKey(chId))
		{
			_clanHall.put(chId, _freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}
		else
		{
			_clanHall.get(chId).free();
		}
		
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}
	
	/**
	 * @param clanHallId the id to use.
	 * @return a clanHall by its id.
	 */
	public ClanHall getClanHallById(int clanHallId)
	{
		if (_clanHall.containsKey(clanHallId))
		{
			return _clanHall.get(clanHallId);
		}
		if (_freeClanHall.containsKey(clanHallId))
		{
			return _freeClanHall.get(clanHallId);
		}
		return null;
	}
	
	public ClanHall getNearbyClanHall(int x, int y, int maxDist)
	{
		ClanHallZone zone = null;
		for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		for (Map.Entry<Integer, ClanHall> ch : _freeClanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		return null;
	}
	
	/**
	 * @param clan the clan to use.
	 * @return a clanHall by its owner.
	 */
	public ClanHall getClanHallByOwner(Clan clan)
	{
		for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
		{
			if (clan.getClanId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		return null;
	}
	
	public static ClanHallManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallManager INSTANCE = new ClanHallManager();
	}
}
