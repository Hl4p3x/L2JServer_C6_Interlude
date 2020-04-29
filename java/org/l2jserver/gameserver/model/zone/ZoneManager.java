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
package org.l2jserver.gameserver.model.zone;

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.model.actor.Creature;

/**
 * This class manages all zones for a given world region
 * @author durgus
 */
public class ZoneManager
{
	private final List<ZoneType> _zones;
	
	/**
	 * The Constructor creates an initial zone list use registerNewZone() / unregisterZone() to change the zone list
	 */
	public ZoneManager()
	{
		_zones = new ArrayList<>();
	}
	
	/**
	 * Register a new zone object into the manager
	 * @param zone
	 */
	public void registerNewZone(ZoneType zone)
	{
		_zones.add(zone);
	}
	
	/**
	 * Unregister a given zone from the manager (e.g. dynamic zones)
	 * @param zone
	 */
	public void unregisterZone(ZoneType zone)
	{
		_zones.remove(zone);
	}
	
	public void revalidateZones(Creature creature)
	{
		for (ZoneType e : _zones)
		{
			if (e != null)
			{
				e.revalidateInZone(creature);
			}
		}
	}
	
	public void removeCharacter(Creature creature)
	{
		for (ZoneType e : _zones)
		{
			if (e != null)
			{
				e.removeCharacter(creature);
			}
		}
	}
	
	public void onDeath(Creature creature)
	{
		for (ZoneType e : _zones)
		{
			if (e != null)
			{
				e.onDieInside(creature);
			}
		}
	}
	
	public void onRevive(Creature creature)
	{
		for (ZoneType e : _zones)
		{
			if (e != null)
			{
				e.onReviveInside(creature);
			}
		}
	}
	
	public List<ZoneType> getZones()
	{
		return _zones;
	}
}
