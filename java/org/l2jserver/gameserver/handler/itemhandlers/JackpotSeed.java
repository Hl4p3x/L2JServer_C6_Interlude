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

import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.GourdInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;

public class JackpotSeed implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6389, // small seed
		6390, // large seed
	};
	
	private static final int[] NPC_IDS =
	{
		12774, // Young Pumpkin
		12777, // Large Young Pumpkin
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		final PlayerInstance player = (PlayerInstance) playable;
		NpcTemplate template1 = null;
		final int itemId = item.getItemId();
		for (int i = 0; i < ITEM_IDS.length; i++)
		{
			if (ITEM_IDS[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(NPC_IDS[i]);
				break;
			}
		}
		
		if (template1 == null)
		{
			return;
		}
		
		try
		{
			final Spawn spawn = new Spawn(template1);
			spawn.setId(IdFactory.getNextId());
			spawn.setX(player.getX());
			spawn.setY(player.getY());
			spawn.setZ(player.getZ());
			final GourdInstance gourd = (GourdInstance) spawn.doSpawn();
			World.getInstance().storeObject(gourd);
			gourd.setOwner(player.getName());
			player.destroyItem("Consume", item.getObjectId(), 1, null, false);
			player.sendMessage("Created " + template1.getName() + " at x: " + spawn.getX() + " y: " + spawn.getY() + " z: " + spawn.getZ());
		}
		catch (Exception e)
		{
			player.sendMessage("Target is not ingame.");
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
