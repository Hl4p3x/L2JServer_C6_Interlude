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

import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.zone.type.ArenaZone;

public class ArenaManager
{
	private List<ArenaZone> _arenas;
	
	private ArenaManager()
	{
	}
	
	public void addArena(ArenaZone arena)
	{
		if (_arenas == null)
		{
			_arenas = new ArrayList<>();
		}
		
		_arenas.add(arena);
	}
	
	public ArenaZone getArena(Creature creature)
	{
		if (_arenas != null)
		{
			for (ArenaZone temp : _arenas)
			{
				if (temp.isCharacterInZone(creature))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public ArenaZone getArena(int x, int y, int z)
	{
		if (_arenas != null)
		{
			for (ArenaZone temp : _arenas)
			{
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public static ArenaManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ArenaManager INSTANCE = new ArenaManager();
	}
}
