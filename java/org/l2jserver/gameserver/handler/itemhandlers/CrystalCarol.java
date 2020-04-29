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
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

public class CrystalCarol implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5562,
		5563,
		5564,
		5565,
		5566,
		5583,
		5584,
		5585,
		5586,
		5587,
		4411,
		4412,
		4413,
		4414,
		4415,
		4416,
		4417,
		5010,
		6903,
		7061,
		7062,
		8555
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		final int itemId = item.getItemId();
		if (itemId == 5562) // crystal_carol_01
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2140, 1, 1, 0));
		}
		else if (itemId == 5563) // crystal_carol_02
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2141, 1, 1, 0));
		}
		else if (itemId == 5564) // crystal_carol_03
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2142, 1, 1, 0));
		}
		else if (itemId == 5565) // crystal_carol_04
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2143, 1, 1, 0));
		}
		else if (itemId == 5566) // crystal_carol_05
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2144, 1, 1, 0));
		}
		else if (itemId == 5583) // crystal_carol_06
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2145, 1, 1, 0));
		}
		else if (itemId == 5584) // crystal_carol_07
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2146, 1, 1, 0));
		}
		else if (itemId == 5585) // crystal_carol_08
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2147, 1, 1, 0));
		}
		else if (itemId == 5586) // crystal_carol_09
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2148, 1, 1, 0));
		}
		else if (itemId == 5587) // crystal_carol_10
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2149, 1, 1, 0));
		}
		else if (itemId == 4411) // crystal_journey
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2069, 1, 1, 0));
		}
		else if (itemId == 4412) // crystal_battle
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2068, 1, 1, 0));
		}
		else if (itemId == 4413) // crystal_love
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2070, 1, 1, 0));
		}
		else if (itemId == 4414) // crystal_solitude
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2072, 1, 1, 0));
		}
		else if (itemId == 4415) // crystal_festival
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2071, 1, 1, 0));
		}
		else if (itemId == 4416) // crystal_celebration
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2073, 1, 1, 0));
		}
		else if (itemId == 4417) // crystal_comedy
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2067, 1, 1, 0));
		}
		else if (itemId == 5010) // crystal_victory
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2066, 1, 1, 0));
		}
		else if (itemId == 6903) // music_box_m
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2187, 1, 1, 0));
		}
		else if (itemId == 7061) // crystal_birthday
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2073, 1, 1, 0));
		}
		else if (itemId == 7062) // crystal_wedding
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2230, 1, 1, 0));
		}
		else if (itemId == 8555) // VVKorea
		{
			player.broadcastPacket(new MagicSkillUse(playable, player, 2272, 1, 1, 0));
		}
		player.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
