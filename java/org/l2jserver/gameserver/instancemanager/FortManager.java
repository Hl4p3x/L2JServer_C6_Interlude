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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.siege.Fort;

/**
 * @author programmos, scoria dev
 */
public class FortManager
{
	protected static final Logger LOGGER = Logger.getLogger(FortManager.class.getName());
	
	private static final List<Fort> _forts = new CopyOnWriteArrayList<>();
	
	public FortManager()
	{
		LOGGER.info("Initializing FortManager");
		_forts.clear();
		load();
	}
	
	public int findNearestFortIndex(WorldObject obj)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double closestDistance = 99999999;
			double distance;
			Fort fort;
			for (int i = 0; i < _forts.size(); i++)
			{
				fort = _forts.get(i);
				if (fort == null)
				{
					continue;
				}
				distance = fort.getDistance(obj);
				if (closestDistance > distance)
				{
					closestDistance = distance;
					index = i;
				}
			}
		}
		return index;
	}
	
	private final void load()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select id from fort order by id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_forts.add(new Fort(rs.getInt("id")));
			}
			
			rs.close();
			statement.close();
			
			LOGGER.info("Loaded: " + _forts.size() + " fortress");
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: loadFortData(): " + e.getMessage());
		}
	}
	
	public Fort getFortById(int fortId)
	{
		for (Fort f : _forts)
		{
			if (f.getFortId() == fortId)
			{
				return f;
			}
		}
		return null;
	}
	
	public Fort getFortByOwner(Clan clan)
	{
		for (Fort f : _forts)
		{
			if (f.getOwnerId() == clan.getClanId())
			{
				return f;
			}
		}
		return null;
	}
	
	public Fort getFort(String name)
	{
		for (Fort f : _forts)
		{
			if (f.getName().equalsIgnoreCase(name.trim()))
			{
				return f;
			}
		}
		return null;
	}
	
	public Fort getFort(int x, int y, int z)
	{
		for (Fort f : _forts)
		{
			if (f.checkIfInZone(x, y, z))
			{
				return f;
			}
		}
		return null;
	}
	
	public Fort getFort(WorldObject activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public int getFortIndex(int fortId)
	{
		Fort fort;
		for (int i = 0; i < _forts.size(); i++)
		{
			fort = _forts.get(i);
			if ((fort != null) && (fort.getFortId() == fortId))
			{
				return i;
			}
		}
		return -1;
	}
	
	public int getFortIndex(WorldObject activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for (int i = 0; i < _forts.size(); i++)
		{
			fort = _forts.get(i);
			if ((fort != null) && fort.checkIfInZone(x, y, z))
			{
				return i;
			}
		}
		return -1;
	}
	
	public List<Fort> getForts()
	{
		return _forts;
	}
	
	public static final FortManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FortManager INSTANCE = new FortManager();
	}
}
