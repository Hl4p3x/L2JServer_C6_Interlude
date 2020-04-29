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
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * Itemhhandler for Character Appearance Change Potions
 * @author Tempy
 */
public class CharChangePotions implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5235,
		5236,
		5237, // Face
		5238,
		5239,
		5240,
		5241, // Hair Color
		5242,
		5243,
		5244,
		5245,
		5246,
		5247,
		5248, // Hair Style
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		final int itemId = item.getItemId();
		PlayerInstance player;
		if (playable instanceof PlayerInstance)
		{
			player = (PlayerInstance) playable;
		}
		else if (playable instanceof PetInstance)
		{
			player = ((PetInstance) playable).getOwner();
		}
		else
		{
			return;
		}
		
		if (player.isAllSkillsDisabled())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		switch (itemId)
		{
			case 5235:
			{
				player.getAppearance().setFace(0);
				break;
			}
			case 5236:
			{
				player.getAppearance().setFace(1);
				break;
			}
			case 5237:
			{
				player.getAppearance().setFace(2);
				break;
			}
			case 5238:
			{
				player.getAppearance().setHairColor(0);
				break;
			}
			case 5239:
			{
				player.getAppearance().setHairColor(1);
				break;
			}
			case 5240:
			{
				player.getAppearance().setHairColor(2);
				break;
			}
			case 5241:
			{
				player.getAppearance().setHairColor(3);
				break;
			}
			case 5242:
			{
				player.getAppearance().setHairStyle(0);
				break;
			}
			case 5243:
			{
				player.getAppearance().setHairStyle(1);
				break;
			}
			case 5244:
			{
				player.getAppearance().setHairStyle(2);
				break;
			}
			case 5245:
			{
				player.getAppearance().setHairStyle(3);
				break;
			}
			case 5246:
			{
				player.getAppearance().setHairStyle(4);
				break;
			}
			case 5247:
			{
				player.getAppearance().setHairStyle(5);
				break;
			}
			case 5248:
			{
				player.getAppearance().setHairStyle(6);
				break;
			}
		}
		
		// Create a summon effect!
		player.broadcastPacket(new MagicSkillUse(playable, player, 2003, 1, 1, 0));
		
		// Update the changed stat for the character in the DB.
		player.store();
		
		// Remove the item from inventory.
		player.destroyItem("Consume", item.getObjectId(), 1, null, false);
		
		// Broadcast the changes to the char and all those nearby.
		player.broadcastPacket(new UserInfo(player));
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
