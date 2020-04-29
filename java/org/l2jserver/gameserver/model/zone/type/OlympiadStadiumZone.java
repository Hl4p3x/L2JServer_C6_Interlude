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

import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.ZoneType;
import org.l2jserver.gameserver.network.SystemMessageId;

/**
 * An olympiad stadium
 * @author durgus
 */
public class OlympiadStadiumZone extends ZoneType
{
	private int _stadiumId;
	
	public OlympiadStadiumZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("stadiumId"))
		{
			_stadiumId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	public void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, true);
		creature.setInsideZone(ZoneId.NO_RESTART, true);
		creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = creature.getActingPlayer();
			if ((player.getOlympiadGameId() + 1) == _stadiumId)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			}
			else if (!player.isGM())
			{
				player.teleToLocation(TeleportWhereType.TOWN);
			}
		}
	}
	
	@Override
	public void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, false);
		creature.setInsideZone(ZoneId.NO_RESTART, false);
		creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		
		if (creature instanceof PlayerInstance)
		{
			creature.getActingPlayer().sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
		}
	}
	
	@Override
	public void onDieInside(Creature creature)
	{
	}
	
	@Override
	public void onReviveInside(Creature creature)
	{
	}
	
	/**
	 * Returns this zones stadium id (if any)
	 * @return
	 */
	public int getStadiumId()
	{
		return _stadiumId;
	}
}