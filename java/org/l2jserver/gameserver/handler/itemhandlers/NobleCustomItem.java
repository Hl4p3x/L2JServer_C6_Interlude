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

import org.l2jserver.Config;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class NobleCustomItem implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		Config.NOOBLE_CUSTOM_ITEM_ID
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (Config.NOBLE_CUSTOM_ITEMS)
		{
			if (!(playable instanceof PlayerInstance))
			{
				return;
			}
			
			final PlayerInstance player = (PlayerInstance) playable;
			if (player.isInOlympiadMode())
			{
				player.sendMessage("This Item Cannot Be Used On Olympiad Games.");
			}
			
			if (player.isNoble())
			{
				player.sendMessage("You Are Already A Noblesse!.");
			}
			else
			{
				player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
				player.setNoble(true);
				player.sendMessage("You Are Now a Noble,You Are Granted With Noblesse Status , And Noblesse Skills.");
				player.broadcastUserInfo();
				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
				player.getInventory().addItem("Tiara", 7694, 1, player, null);
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
