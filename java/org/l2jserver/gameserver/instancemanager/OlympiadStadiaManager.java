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
import java.util.logging.Logger;

import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.zone.type.OlympiadStadiumZone;

public class OlympiadStadiaManager
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadStadiaManager.class.getName());
	
	private List<OlympiadStadiumZone> _olympiadStadias;
	
	public void addStadium(OlympiadStadiumZone arena)
	{
		if (_olympiadStadias == null)
		{
			_olympiadStadias = new ArrayList<>();
		}
		
		_olympiadStadias.add(arena);
	}
	
	public OlympiadStadiumZone getStadium(Creature creature)
	{
		for (OlympiadStadiumZone temp : _olympiadStadias)
		{
			if (temp.isCharacterInZone(creature))
			{
				return temp;
			}
		}
		return null;
	}
	
	public OlympiadStadiumZone getStadiumByLoc(int x, int y, int z)
	{
		if (_olympiadStadias != null)
		{
			for (OlympiadStadiumZone temp : _olympiadStadias)
			{
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public static OlympiadStadiaManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final OlympiadStadiaManager INSTANCE = new OlympiadStadiaManager();
	}
}
