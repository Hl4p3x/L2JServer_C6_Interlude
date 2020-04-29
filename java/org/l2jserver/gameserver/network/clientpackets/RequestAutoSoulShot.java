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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class RequestAutoSoulShot extends GameClientPacket
{
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;
	
	private static final List<Integer> SHOT_IDS = new ArrayList<>();
	static
	{
		SHOT_IDS.add(5789);
		SHOT_IDS.add(1835);
		SHOT_IDS.add(1463);
		SHOT_IDS.add(1464);
		SHOT_IDS.add(1465);
		SHOT_IDS.add(1466);
		SHOT_IDS.add(1467);
		SHOT_IDS.add(5790);
		SHOT_IDS.add(2509);
		SHOT_IDS.add(2510);
		SHOT_IDS.add(2511);
		SHOT_IDS.add(2512);
		SHOT_IDS.add(2513);
		SHOT_IDS.add(2514);
		SHOT_IDS.add(3947);
		SHOT_IDS.add(3948);
		SHOT_IDS.add(3949);
		SHOT_IDS.add(3950);
		SHOT_IDS.add(3951);
		SHOT_IDS.add(3952);
	}
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Like L2OFF you can't use soulshots while sitting
		if (player.isSitting() && SHOT_IDS.contains(_itemId))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_CANNOT_BE_ACTIVATED);
			sm.addItemName(_itemId);
			player.sendPacket(sm);
			return;
		}
		
		if ((player.getPrivateStoreType() == 0) && (player.getActiveRequester() == null) && !player.isDead())
		{
			final ItemInstance item = player.getInventory().getItemByItemId(_itemId);
			if (item != null)
			{
				if (_type == 1)
				{
					// Fishingshots are not automatic on retail
					if ((_itemId < 6535) || (_itemId > 6540))
					{
						player.addAutoSoulShot(_itemId);
						
						// Attempt to charge first shot on activation
						if ((_itemId == 6645) || (_itemId == 6646) || (_itemId == 6647))
						{
							// Like L2OFF you can active automatic SS only if you have a pet
							if (player.getPet() != null)
							{
								// player.addAutoSoulShot(_itemId);
								// ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
								// player.sendPacket(atk);
								
								// start the auto soulshot use
								final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
								sm.addString(item.getItemName());
								player.sendPacket(sm);
								
								player.rechargeAutoSoulShot(true, true, true);
							}
							else
							{
								final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATIC_USE_FUNCTION);
								sm.addString(item.getItemName());
								player.sendPacket(sm);
								return;
							}
						}
						else
						{
							if ((player.getActiveWeaponItem() != player.getFistsWeaponItem()) && (item.getItem().getCrystalType() == player.getActiveWeaponItem().getCrystalType()))
							{
								if ((_itemId >= 3947) && (_itemId <= 3952) && player.isInOlympiadMode())
								{
									final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
									sm.addString(item.getItemName());
									player.sendPacket(sm);
								}
								else
								{
									// player.addAutoSoulShot(_itemId);
									
									// start the auto soulshot use
									final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
									sm.addString(item.getItemName());
									player.sendPacket(sm);
								}
							}
							else if (((_itemId >= 2509) && (_itemId <= 2514)) || ((_itemId >= 3947) && (_itemId <= 3952)) || (_itemId == 5790))
							{
								player.sendPacket(SystemMessageId.THE_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE);
							}
							else
							{
								player.sendPacket(SystemMessageId.THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON);
							}
							
							player.rechargeAutoSoulShot(true, true, false);
						}
					}
				}
				else if (_type == 0)
				{
					player.removeAutoSoulShot(_itemId);
					// ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
					// player.sendPacket(atk);
					
					// cancel the auto soulshot use
					final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
					sm.addString(item.getItemName());
					player.sendPacket(sm);
				}
				
				player.sendPacket(new ExAutoSoulShot(_itemId, _type));
			}
		}
	}
}
