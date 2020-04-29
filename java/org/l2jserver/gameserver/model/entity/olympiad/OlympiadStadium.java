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
package org.l2jserver.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author GodKratos
 */
class OlympiadStadium
{
	private boolean _freeToUse = true;
	private final int[] _coords = new int[3];
	private final List<PlayerInstance> _spectators;
	
	public boolean isFreeToUse()
	{
		return _freeToUse;
	}
	
	public void setStadiaBusy()
	{
		_freeToUse = false;
	}
	
	public void setStadiaFree()
	{
		_freeToUse = true;
		clearSpectators();
	}
	
	public int[] getCoordinates()
	{
		return _coords;
	}
	
	public OlympiadStadium(int x, int y, int z)
	{
		_coords[0] = x;
		_coords[1] = y;
		_coords[2] = z;
		_spectators = new ArrayList<>();
	}
	
	protected void addSpectator(int id, PlayerInstance spec, boolean storeCoords)
	{
		spec.enterOlympiadObserverMode(_coords[0], _coords[1], _coords[2], id, storeCoords);
		_spectators.add(spec);
	}
	
	protected List<PlayerInstance> getSpectators()
	{
		return _spectators;
	}
	
	protected void removeSpectator(PlayerInstance spec)
	{
		if ((_spectators != null) && _spectators.contains(spec))
		{
			_spectators.remove(spec);
		}
	}
	
	private void clearSpectators()
	{
		_spectators.clear();
	}
}
