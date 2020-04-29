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

import org.l2jserver.Config;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.TradeList;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @version $Revision: 1.7.2.2.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PrivateStoreListBuy extends GameServerPacket
{
	private final PlayerInstance _storePlayer;
	private final PlayerInstance _player;
	private int _playerAdena;
	private final TradeList.TradeItem[] _items;
	
	public PrivateStoreListBuy(PlayerInstance player, PlayerInstance storePlayer)
	{
		_storePlayer = storePlayer;
		_player = player;
		if (Config.SELL_BY_ITEM)
		{
			_player.sendPacket(new CreatureSay(0, ChatType.PARTYROOM_COMMANDER, "", "ATTENTION: Store System is not based on Adena, be careful!"));
			_playerAdena = _player.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			_playerAdena = _player.getAdena();
		}
		
		// _storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		// this items must be the items available into the _activeChar (seller) inventory
		_items = _storePlayer.getBuyList().getAvailableItems(_player.getInventory());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb8);
		writeD(_storePlayer.getObjectId());
		writeD(_playerAdena);
		
		writeD(_items.length);
		
		for (TradeList.TradeItem item : _items)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeH(item.getEnchant());
			// writeD(item.getCount()); //give max possible sell amount
			writeD(item.getCurCount());
			
			writeD(item.getItem().getReferencePrice());
			writeH(0);
			
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice()); // buyers price
			
			writeD(item.getCount()); // maximum possible tradecount
		}
	}
}