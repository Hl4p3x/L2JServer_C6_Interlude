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
package org.l2jserver.gameserver.handler.itemhandlers;

import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.MercTicketManager;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

public class MercTicket implements IItemHandler
{
	private static final String[] MESSAGES =
	{
		"To arms!.",
		"I am ready to serve you my lord when the time comes.",
		"You summon me."
	};
	
	/**
	 * handler for using mercenary tickets. Things to do: 1) Check constraints: 1.a) Tickets may only be used in a castle 1.b) Only specific tickets may be used in each castle (different tickets for each castle) 1.c) only the owner of that castle may use them 1.d) tickets cannot be used during siege
	 * 1.e) Check if max number of tickets has been reached 1.f) Check if max number of tickets from this ticket's TYPE has been reached 2) If allowed, call the MercTicketManager to add the item and spawn in the world 3) Remove the item from the person's inventory
	 */
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		final int itemId = item.getItemId();
		final PlayerInstance player = (PlayerInstance) playable;
		final Castle castle = CastleManager.getInstance().getCastle(player);
		int castleId = -1;
		if (castle != null)
		{
			castleId = castle.getCastleId();
		}
		
		// add check that certain tickets can only be placed in certain castles
		if (MercTicketManager.getInstance().getTicketCastleId(itemId) != castleId)
		{
			switch (castleId)
			{
				case 1:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Gludio.");
					return;
				}
				case 2:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Dion.");
					return;
				}
				case 3:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Giran.");
					return;
				}
				case 4:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Oren.");
					return;
				}
				case 5:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Aden.");
					return;
				}
				case 6:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Heine.");
					return;
				}
				case 7:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Goddard.");
					return;
				}
				case 8:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Rune.");
					return;
				}
				case 9:
				{
					player.sendMessage("This Mercenary Ticket can only be used in Schuttgart.");
					return;
				}
				// player is not in a castle
				default:
				{
					player.sendMessage("Mercenary Tickets can only be used in a castle.");
					return;
				}
			}
		}
		
		if (!player.isCastleLord(castleId))
		{
			player.sendMessage("You are not the lord of this castle!");
			return;
		}
		
		if ((castle == null) || castle.getSiege().isInProgress())
		{
			player.sendMessage("You cannot hire mercenary while siege is in progress!");
			return;
		}
		
		if (MercTicketManager.getInstance().isAtCasleLimit(item.getItemId()))
		{
			player.sendMessage("You cannot hire any more mercenaries");
			return;
		}
		
		if (MercTicketManager.getInstance().isAtTypeLimit(item.getItemId()))
		{
			player.sendMessage("You cannot hire any more mercenaries of this type.  You may still hire other types of mercenaries");
			return;
		}
		
		final int npcId = MercTicketManager.getInstance().addTicket(item.getItemId(), player, MESSAGES);
		player.destroyItem("Consume", item.getObjectId(), 1, null, false); // Remove item from char's inventory
		player.sendMessage("Hired mercenary (" + itemId + "," + npcId + ") at coords:" + player.getX() + "," + player.getY() + "," + player.getZ() + " heading:" + player.getHeading());
	}
	
	// left in here for backward compatibility
	@Override
	public int[] getItemIds()
	{
		return MercTicketManager.getInstance().getItemIds();
	}
}
