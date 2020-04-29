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
package org.l2jserver.gameserver.model.zone.type;

import org.l2jserver.Config;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.ZoneRespawn;

/**
 * A Town zone
 * @author durgus
 */
public class TownZone extends ZoneRespawn
{
	private String _townName;
	private int _townId;
	private int _redirectTownId;
	private int _taxById;
	private boolean _noPeace;
	
	public TownZone(int id)
	{
		super(id);
		
		_taxById = 0;
		
		// Default to Giran
		_redirectTownId = 9;
		
		// Default peace zone
		_noPeace = false;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_townName = value;
		}
		else if (name.equals("townId"))
		{
			_townId = Integer.parseInt(value);
		}
		else if (name.equals("redirectTownId"))
		{
			_redirectTownId = Integer.parseInt(value);
		}
		else if (name.equals("taxById"))
		{
			_taxById = Integer.parseInt(value);
		}
		else if (name.equals("noPeace"))
		{
			_noPeace = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		// PVP possible during siege, now for siege participants only
		// Could also check if this town is in siege, or if any siege is going on
		if ((creature instanceof PlayerInstance) && (((PlayerInstance) creature).getSiegeState() != 0) && (Config.ZONE_TOWN == 1))
		{
			return;
		}
		
		if (!_noPeace && (Config.ZONE_TOWN != 2))
		{
			creature.setInsideZone(ZoneId.PEACE, true);
		}
		
		creature.setInsideZone(ZoneId.TOWN, true);
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (!_noPeace)
		{
			creature.setInsideZone(ZoneId.PEACE, false);
		}
		
		creature.setInsideZone(ZoneId.TOWN, false);
	}
	
	@Override
	protected void onDieInside(Creature creature)
	{
	}
	
	@Override
	protected void onReviveInside(Creature creature)
	{
	}
	
	/**
	 * Returns this town zones name
	 * @return
	 */
	public String getName()
	{
		return _townName;
	}
	
	/**
	 * Returns this zones town id (if any)
	 * @return
	 */
	public int getTownId()
	{
		return _townId;
	}
	
	/**
	 * Gets the id for this town zones redir town
	 * @return
	 */
	@Deprecated
	public int getRedirectTownId()
	{
		return _redirectTownId;
	}
	
	/**
	 * Returns this town zones castle id
	 * @return
	 */
	public int getTaxById()
	{
		return _taxById;
	}
}
