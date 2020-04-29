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
package org.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;

public class SiegeClan
{
	private int _clanId = 0;
	private List<NpcInstance> _flag = new ArrayList<>();
	private int _numFlagsAdded = 0;
	private SiegeClanType _type;
	
	public enum SiegeClanType
	{
		OWNER,
		DEFENDER,
		ATTACKER,
		DEFENDER_PENDING
	}
	
	public SiegeClan(int clanId, SiegeClanType type)
	{
		_clanId = clanId;
		_type = type;
	}
	
	public int getNumFlags()
	{
		return _numFlagsAdded;
	}
	
	public void addFlag(NpcInstance flag)
	{
		_numFlagsAdded++;
		getFlag().add(flag);
	}
	
	public boolean removeFlag(NpcInstance flag)
	{
		if (flag == null)
		{
			return false;
		}
		
		final boolean ret = getFlag().remove(flag);
		
		// check if null objects or dups remain in the list.
		// for some reason, this might be happening sometimes...
		// delete false duplicates: if this flag got deleted, delete its copies too.
		if (ret)
		{
			getFlag().remove(flag);
		}
		
		// now delete nulls
		int n;
		boolean more = true;
		
		while (more)
		{
			more = false;
			n = getFlag().size();
			if (n > 0)
			{
				for (int i = 0; i < n; i++)
				{
					if (getFlag().get(i) == null)
					{
						getFlag().remove(i);
						more = true;
						break;
					}
				}
			}
		}
		
		_numFlagsAdded--; // remove flag count
		flag.deleteMe();
		return ret;
	}
	
	public void removeFlags()
	{
		for (NpcInstance flag : getFlag())
		{
			removeFlag(flag);
		}
	}
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public List<NpcInstance> getFlag()
	{
		if (_flag == null)
		{
			_flag = new ArrayList<>();
		}
		return _flag;
	}
	
	public SiegeClanType getType()
	{
		return _type;
	}
	
	public void setType(SiegeClanType setType)
	{
		_type = setType;
	}
}