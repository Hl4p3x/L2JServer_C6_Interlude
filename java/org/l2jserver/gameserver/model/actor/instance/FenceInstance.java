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
package org.l2jserver.gameserver.model.actor.instance;

import org.l2jserver.gameserver.datatables.xml.FenceData;
import org.l2jserver.gameserver.enums.FenceState;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.network.serverpackets.DeleteObject;
import org.l2jserver.gameserver.network.serverpackets.ExColosseumFenceInfo;

/**
 * @author HoridoJoho / FBIagent
 */
public class FenceInstance extends WorldObject
{
	private final int _xMin;
	private final int _xMax;
	private final int _yMin;
	private final int _yMax;
	
	private final int _width;
	private final int _length;
	
	private FenceState _state;
	private int[] _heightFences;
	
	public FenceInstance(int x, int y, String name, int width, int length, int height, FenceState state)
	{
		super(IdFactory.getNextId());
		
		_xMin = x - (width / 2);
		_xMax = x + (width / 2);
		_yMin = y - (length / 2);
		_yMax = y + (length / 2);
		super.setName(name);
		_width = width;
		_length = length;
		_state = state;
		if (height > 1)
		{
			_heightFences = new int[height - 1];
			for (int i = 0; i < _heightFences.length; i++)
			{
				_heightFences[i] = IdFactory.getNextId();
			}
		}
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	public void sendInfo(PlayerInstance player)
	{
		player.sendPacket(new ExColosseumFenceInfo(this));
		if (_heightFences != null)
		{
			for (int objId : _heightFences)
			{
				player.sendPacket(new ExColosseumFenceInfo(objId, getX(), getY(), getZ(), _width, _length, _state));
			}
		}
	}
	
	public boolean deleteMe()
	{
		if (_heightFences != null)
		{
			final DeleteObject[] deleteObjects = new DeleteObject[_heightFences.length];
			for (int i = 0; i < _heightFences.length; i++)
			{
				deleteObjects[i] = new DeleteObject(_heightFences[i]);
			}
			
			getKnownList().getKnownObjects().values().stream().filter(PlayerInstance.class::isInstance).map(PlayerInstance.class::cast).forEach(player ->
			{
				for (DeleteObject deleteObject : deleteObjects)
				{
					player.sendPacket(deleteObject);
				}
			});
		}
		
		decayMe();
		
		FenceData.getInstance().removeFence(this);
		return false;
	}
	
	public FenceState getState()
	{
		return _state;
	}
	
	public void setState(FenceState type)
	{
		_state = type;
		getKnownList().getKnownObjects().values().stream().filter(PlayerInstance.class::isInstance).map(PlayerInstance.class::cast).forEach(this::sendInfo);
	}
	
	public int getWidth()
	{
		return _width;
	}
	
	public int getLength()
	{
		return _length;
	}
	
	public int getXMin()
	{
		return _xMin;
	}
	
	public int getYMin()
	{
		return _yMin;
	}
	
	public int getXMax()
	{
		return _xMax;
	}
	
	public int getYMax()
	{
		return _yMax;
	}
}