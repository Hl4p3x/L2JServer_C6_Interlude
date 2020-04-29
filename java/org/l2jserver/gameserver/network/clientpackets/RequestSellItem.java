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

import org.l2jserver.Config;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.FishermanInstance;
import org.l2jserver.gameserver.model.actor.instance.MerchantInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class RequestSellItem extends GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items; // count*3
	
	/**
	 * packet type id 0x1e sample 1e 00 00 00 00 // list id 02 00 00 00 // number of items 71 72 00 10 // object id ea 05 00 00 // item id 01 00 00 00 // item count 76 4b 00 10 // object id 2e 0a 00 00 // item id 01 00 00 00 // item count format: cdd (ddd)
	 */
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if ((_count <= 0) || ((_count * 12) > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
			return;
		}
		
		_items = new int[_count * 3];
		for (int i = 0; i < _count; i++)
		{
			final int objectId = readD();
			_items[(i * 3) + 0] = objectId;
			final int itemId = readD();
			_items[(i * 3) + 1] = itemId;
			final long cnt = readD();
			if ((cnt > Integer.MAX_VALUE) || (cnt <= 0))
			{
				_count = 0;
				return;
			}
			_items[(i * 3) + 2] = (int) cnt;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			player.sendMessage("You buying too fast.");
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (player.getKarma() > 0))
		{
			return;
		}
		
		final WorldObject target = player.getTarget();
		if (!player.isGM() && ((target == null) // No target (ie GM Shop)
			|| !(target instanceof MerchantInstance) // Target not a merchant and not mercmanager
			|| !player.isInsideRadius(target, NpcInstance.INTERACTION_DISTANCE, false, false)))
		{
			return; // Distance is too far
		}
		
		String htmlFolder = "";
		NpcInstance merchant = null;
		if (target instanceof MerchantInstance)
		{
			htmlFolder = "merchant";
			merchant = (NpcInstance) target;
		}
		else if (target instanceof FishermanInstance)
		{
			htmlFolder = "fisherman";
			merchant = (NpcInstance) target;
		}
		else
		{
			return;
		}
		
		if ((_listId > 1000000) && (merchant.getTemplate().getNpcId() != (_listId - 1000000)))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		long totalPrice = 0;
		// Proceed the sell
		for (int i = 0; i < _count; i++)
		{
			final int objectId = _items[(i * 3) + 0];
			final int count = _items[(i * 3) + 2];
			
			// Check count
			if ((count <= 0) || (count > Integer.MAX_VALUE))
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			ItemInstance item = player.checkItemManipulation(objectId, count, "sell");
			
			// Check Item
			if ((item == null) || !item.getItem().isSellable())
			{
				continue;
			}
			
			final long price = item.getReferencePrice() / 2;
			totalPrice += price * count;
			
			// Fix exploit during Sell
			if (((Integer.MAX_VALUE / count) < price) || (totalPrice > Integer.MAX_VALUE))
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			// Check totalPrice
			if (totalPrice <= 0)
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			player.getInventory().destroyItem("Sell", objectId, count, player, null);
		}
		
		if (!Config.MERCHANT_ZERO_SELL_PRICE)
		{
			player.addAdena("Sell", (int) totalPrice, merchant, false);
		}
		
		final String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");
		if (html != null)
		{
			final NpcHtmlMessage soldMsg = new NpcHtmlMessage(merchant.getObjectId());
			soldMsg.setHtml(html.replace("%objectId%", String.valueOf(merchant.getObjectId())));
			player.sendPacket(soldMsg);
		}
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}
}
