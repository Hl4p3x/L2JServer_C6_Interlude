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
package org.l2jserver.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.model.ItemRequest;
import org.l2jserver.gameserver.model.TradeList;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

public class RequestPrivateStoreSell extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestPrivateStoreSell.class.getName());
	
	private int _storePlayerId;
	private int _count;
	private int _price;
	private ItemRequest[] _items;
	
	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		_count = readD();
		// count*20 is the size of a for iteration of each item
		if ((_count < 0) || ((_count * 20) > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
		}
		_items = new ItemRequest[_count];
		long priceTotal = 0;
		for (int i = 0; i < _count; i++)
		{
			final int objectId = readD();
			final int itemId = readD();
			final int enchant = readH();
			readH(); // TODO analyse this
			final long count = readD();
			final int price = readD();
			if ((count > Integer.MAX_VALUE) || (count < 0))
			{
				final String msgErr = "[RequestPrivateStoreSell] player " + getClient().getPlayer().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getPlayer(), msgErr, Config.DEFAULT_PUNISH);
				_count = 0;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, enchant, (int) count, price);
			priceTotal += price * count;
		}
		
		if ((priceTotal < 0) || (priceTotal > Integer.MAX_VALUE))
		{
			final String msgErr = "[RequestPrivateStoreSell] player " + getClient().getPlayer().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getPlayer(), msgErr, Config.DEFAULT_PUNISH);
			_count = 0;
			return;
		}
		
		_price = (int) priceTotal;
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("privatestoresell"))
		{
			player.sendMessage("You selling items too fast");
			return;
		}
		
		final WorldObject object = World.getInstance().findObject(_storePlayerId);
		if (!(object instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance storePlayer = (PlayerInstance) object;
		if (storePlayer.getPrivateStoreType() != PlayerInstance.STORE_PRIVATE_BUY)
		{
			return;
		}
		
		final TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
		{
			return;
		}
		
		// Check if player didn't choose any items
		if ((_items == null) || (_items.length == 0))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(PlayerInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (Config.SELL_BY_ITEM)
		{
			if (storePlayer.getItemCount(Config.SELL_ITEM, -1) < _price)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessage.sendString("You have not enough items to buy, canceling PrivateBuy"));
				storePlayer.setPrivateStoreType(PlayerInstance.STORE_PRIVATE_NONE);
				storePlayer.broadcastUserInfo();
				return;
			}
		}
		else if (storePlayer.getAdena() < _price)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			storePlayer.sendMessage("You have not enough adena, canceling PrivateBuy.");
			storePlayer.setPrivateStoreType(PlayerInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
		}
		
		if (!storeList.PrivateStoreSell(player, _items, _price))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			Util.handleIllegalPlayerAction(getClient().getPlayer(), "Player " + getClient().getPlayer().getName() + " provided invalid list or request! ", Config.DEFAULT_PUNISH);
			LOGGER.warning("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(PlayerInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
	}
}