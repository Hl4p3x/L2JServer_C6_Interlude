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

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class RequestUnEquipItem extends GameClientPacket
{
	private int _slot;
	
	/**
	 * packet type id 0x11 format: cd
	 */
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player._haveFlagCTF)
		{
			player.sendMessage("You can't unequip a CTF flag.");
			return;
		}
		
		final ItemInstance item = player.getInventory().getPaperdollItemByItemId(_slot);
		if ((item != null) && item.isWear())
		{
			// Wear-items are not to be unequipped
			return;
		}
		
		// Prevent of unequiping a cursed weapon
		if ((_slot == Item.SLOT_LR_HAND) && player.isCursedWeaponEquiped())
		{
			// Message ?
			return;
		}
		
		// Prevent player from unequipping items in special conditions
		if (player.isStunned() || player.isConfused() || player.isParalyzed() || player.isSleeping() || player.isAlikeDead())
		{
			player.sendMessage("Your status does not allow you to do that.");
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			return;
		}
		
		if (player.isMoving() && player.isAttackingNow() && ((_slot == Item.SLOT_LR_HAND) || (_slot == Item.SLOT_L_HAND) || (_slot == Item.SLOT_R_HAND)))
		{
			final WorldObject target = player.getTarget();
			player.setTarget(null);
			player.stopMove(null);
			player.setTarget(target);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK);
		}
		
		// Remove augmentation bonus
		if ((item != null) && item.isAugmented())
		{
			item.getAugmentation().removeBonus(player);
		}
		
		final ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		
		// show the update in the inventory
		final InventoryUpdate iu = new InventoryUpdate();
		for (ItemInstance element : unequiped)
		{
			player.checkSSMatch(null, element);
			iu.addModifiedItem(element);
		}
		player.sendPacket(iu);
		
		player.broadcastUserInfo();
		
		// this can be 0 if the user pressed the right mouse button twice very fast
		if (unequiped.length > 0)
		{
			SystemMessage sm = null;
			if (unequiped[0].getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
				sm.addNumber(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0].getItemId());
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DISARMED);
				sm.addItemName(unequiped[0].getItemId());
			}
			player.sendPacket(sm);
		}
	}
}
