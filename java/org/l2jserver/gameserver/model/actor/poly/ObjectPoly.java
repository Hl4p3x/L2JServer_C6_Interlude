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
package org.l2jserver.gameserver.model.actor.poly;

import org.l2jserver.gameserver.model.WorldObject;

public class ObjectPoly
{
	private final WorldObject _activeObject;
	private int _polyId;
	private String _polyType;
	
	public ObjectPoly(WorldObject activeObject)
	{
		_activeObject = activeObject;
	}
	
	public void setPolyInfo(String polyType, String polyId)
	{
		setPolyId(Integer.parseInt(polyId));
		setPolyType(polyType);
	}
	
	public WorldObject getActiveObject()
	{
		return _activeObject;
	}
	
	public boolean isMorphed()
	{
		return _polyType != null;
	}
	
	public int getPolyId()
	{
		return _polyId;
	}
	
	public void setPolyId(int value)
	{
		_polyId = value;
	}
	
	public String getPolyType()
	{
		return _polyType;
	}
	
	public void setPolyType(String value)
	{
		_polyType = value;
	}
}
