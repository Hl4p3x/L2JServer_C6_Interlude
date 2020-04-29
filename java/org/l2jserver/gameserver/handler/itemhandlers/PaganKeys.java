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

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

/**
 * @author chris
 */
public class PaganKeys implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		8273,
		8274,
		8275
	};
	public static final int INTERACTION_DISTANCE = 100;
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		final int itemId = item.getItemId();
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		final WorldObject target = player.getTarget();
		if (!(target instanceof DoorInstance))
		{
			player.sendPacket(SystemMessageId.INVALID_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final DoorInstance door = (DoorInstance) target;
		if (!player.isInsideRadius(door, INTERACTION_DISTANCE, false, false))
		{
			player.sendMessage("Too far.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((player.getAbnormalEffect() > 0) || player.isInCombat())
		{
			player.sendMessage("You cannot use the key now.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int openChance = 35;
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return;
		}
		
		switch (itemId)
		{
			case 8273: // AnteroomKey
			{
				if (door.getDoorName().startsWith("Anteroom"))
				{
					if ((openChance > 0) && (Rnd.get(100) < openChance))
					{
						player.sendMessage("You opened Anterooms Door.");
						door.openMe();
						door.onOpen(); // Closes the door after 60sec
						player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
					}
					else
					{
						player.sendMessage("You failed to open Anterooms Door.");
						player.broadcastPacket(new SocialAction(player.getObjectId(), 13));
						player.sendPacket(new PlaySound("interfacesound.system_close_01"));
					}
				}
				else
				{
					player.sendMessage("Incorrect Door.");
				}
				break;
			}
			case 8274: // Chapelkey, Capel Door has a Gatekeeper?? I use this key for Altar Entrance and Chapel_Door
			{
				if (door.getDoorName().startsWith("Altar_Entrance") || door.getDoorName().startsWith("Chapel_Door"))
				{
					if ((openChance > 0) && (Rnd.get(100) < openChance))
					{
						player.sendMessage("You opened Altar Entrance.");
						door.openMe();
						door.onOpen();
						player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
					}
					else
					{
						player.sendMessage("You failed to open Altar Entrance.");
						player.broadcastPacket(new SocialAction(player.getObjectId(), 13));
						player.sendPacket(new PlaySound("interfacesound.system_close_01"));
					}
				}
				else
				{
					player.sendMessage("Incorrect Door.");
				}
				break;
			}
			case 8275: // Key of Darkness
			{
				if (door.getDoorName().startsWith("Door_of_Darkness"))
				{
					if ((openChance > 0) && (Rnd.get(100) < openChance))
					{
						player.sendMessage("You opened Door of Darkness.");
						door.openMe();
						door.onOpen();
						player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
					}
					else
					{
						player.sendMessage("You failed to open Door of Darkness.");
						player.broadcastPacket(new SocialAction(player.getObjectId(), 13));
						player.sendPacket(new PlaySound("interfacesound.system_close_01"));
					}
				}
				else
				{
					player.sendMessage("Incorrect Door.");
				}
				break;
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}