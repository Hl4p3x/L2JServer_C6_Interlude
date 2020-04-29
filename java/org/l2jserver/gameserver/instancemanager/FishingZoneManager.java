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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.model.zone.type.FishingZone;
import org.l2jserver.gameserver.model.zone.type.WaterZone;

public class FishingZoneManager
{
	private List<FishingZone> _fishingZones;
	private List<WaterZone> _waterZones;
	
	private FishingZoneManager()
	{
	}
	
	public void addFishingZone(FishingZone fishingZone)
	{
		if (_fishingZones == null)
		{
			_fishingZones = new ArrayList<>();
		}
		
		_fishingZones.add(fishingZone);
	}
	
	public void addWaterZone(WaterZone waterZone)
	{
		if (_waterZones == null)
		{
			_waterZones = new ArrayList<>();
		}
		
		_waterZones.add(waterZone);
	}
	
	/*
	 * isInsideFishingZone() - This function was modified to check the coordinates without caring for Z. This allows for the player to fish off bridges, into the water, or from other similar high places. One should be able to cast the line from up into the water, not only fishing whith one's feet
	 * wet. :) TODO: Consider in the future, limiting the maximum height one can be above water, if we start getting "orbital fishing" players... xD
	 */
	public FishingZone isInsideFishingZone(int x, int y, int z)
	{
		for (FishingZone temp : _fishingZones)
		{
			if (temp.isInsideZone(x, y, temp.getWaterZ() - 10))
			{
				return temp;
			}
		}
		return null;
	}
	
	public WaterZone isInsideWaterZone(int x, int y, int z)
	{
		for (WaterZone temp : _waterZones)
		{
			if (temp.isInsideZone(x, y, temp.getWaterZ()))
			{
				return temp;
			}
		}
		return null;
	}
	
	public static FishingZoneManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FishingZoneManager INSTANCE = new FishingZoneManager();
	}
}
