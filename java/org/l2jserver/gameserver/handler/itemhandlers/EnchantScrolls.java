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
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ChooseInventoryItem;

public class EnchantScrolls implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		729,
		730,
		731,
		732,
		6569,
		6570, // a grade
		947,
		948,
		949,
		950,
		6571,
		6572, // b grade
		951,
		952,
		953,
		954,
		6573,
		6574, // c grade
		955,
		956,
		957,
		958,
		6575,
		6576, // d grade
		959,
		960,
		961,
		962,
		6577,
		6578, // s grade
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			return;
		}
		
		player.setActiveEnchantItem(item);
		player.sendPacket(SystemMessageId.SELECT_ITEM_TO_ENCHANT);
		player.sendPacket(new ChooseInventoryItem(item.getItemId()));
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
