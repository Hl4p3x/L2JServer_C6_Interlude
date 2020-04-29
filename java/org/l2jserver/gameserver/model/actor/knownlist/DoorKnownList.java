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
package org.l2jserver.gameserver.model.actor.knownlist;

import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.FortSiegeGuardInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SiegeGuardInstance;

public class DoorKnownList extends CreatureKnownList
{
	public DoorKnownList(DoorInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public DoorInstance getActiveChar()
	{
		return (DoorInstance) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(WorldObject object)
	{
		if ((object instanceof SiegeGuardInstance) || (object instanceof FortSiegeGuardInstance))
		{
			return 800;
		}
		
		if (!(object instanceof PlayerInstance))
		{
			return 0;
		}
		
		return 4000;
	}
	
	@Override
	public int getDistanceToWatchObject(WorldObject object)
	{
		if ((object instanceof SiegeGuardInstance) || (object instanceof FortSiegeGuardInstance))
		{
			return 600;
		}
		
		if (!(object instanceof PlayerInstance))
		{
			return 0;
		}
		
		return 2000;
	}
}
