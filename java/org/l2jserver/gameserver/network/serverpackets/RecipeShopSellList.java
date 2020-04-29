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
package org.l2jserver.gameserver.network.serverpackets;

import org.l2jserver.gameserver.model.ManufactureItem;
import org.l2jserver.gameserver.model.ManufactureList;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

public class RecipeShopSellList extends GameServerPacket
{
	private final PlayerInstance _buyer;
	private final PlayerInstance _player;
	
	public RecipeShopSellList(PlayerInstance buyer, PlayerInstance player)
	{
		_buyer = buyer;
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		final ManufactureList createList = _player.getCreateList();
		if (createList != null)
		{
			writeC(0xd9);
			writeD(_player.getObjectId());
			writeD((int) _player.getCurrentMp()); // Creator's MP
			writeD(_player.getMaxMp()); // Creator's MP
			writeD(_buyer.getAdena()); // Buyer Adena
			writeD(createList.size());
			
			for (ManufactureItem item : createList.getList())
			{
				writeD(item.getRecipeId());
				writeD(0x00); // unknown
				writeD(item.getCost());
			}
		}
	}
}
