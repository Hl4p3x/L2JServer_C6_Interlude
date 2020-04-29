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
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.ZoneRespawn;
import org.l2jserver.gameserver.network.serverpackets.ClanHallDecoration;

/**
 * A clan hall zone
 * @author durgus
 */
public class ClanHallZone extends ZoneRespawn
{
	private int _clanHallId;
	
	public ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "clanHallId":
			{
				_clanHallId = Integer.parseInt(value);
				// Register self to the correct clan hall
				ClanHallManager.getInstance().getClanHallById(_clanHallId).setZone(this);
				break;
			}
			default:
			{
				super.setParameter(name, value);
				break;
			}
		}
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if (creature instanceof PlayerInstance)
		{
			// Set as in clan hall
			creature.setInsideZone(ZoneId.CLAN_HALL, true);
			
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallById(_clanHallId);
			if (clanHall == null)
			{
				return;
			}
			
			// Send decoration packet
			((PlayerInstance) creature).sendPacket(new ClanHallDecoration(clanHall));
			
			// Send a message
			if ((clanHall.getOwnerId() != 0) && (clanHall.getOwnerId() == ((PlayerInstance) creature).getClanId()))
			{
				((PlayerInstance) creature).sendMessage("You have entered your clan hall.");
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (creature instanceof PlayerInstance)
		{
			// Unset clanhall zone
			creature.setInsideZone(ZoneId.CLAN_HALL, false);
			
			// Send a message
			if ((((PlayerInstance) creature).getClanId() != 0) && (ClanHallManager.getInstance().getClanHallById(_clanHallId).getOwnerId() == ((PlayerInstance) creature).getClanId()))
			{
				((PlayerInstance) creature).sendMessage("You have left your clan hall.");
			}
		}
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
	 * Removes all foreigners from the clan hall
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		for (Creature temp : getCharactersInside())
		{
			if (!(temp instanceof PlayerInstance))
			{
				continue;
			}
			
			if (((PlayerInstance) temp).getClanId() == owningClanId)
			{
				continue;
			}
			
			((PlayerInstance) temp).teleToLocation(TeleportWhereType.TOWN);
		}
	}
}
