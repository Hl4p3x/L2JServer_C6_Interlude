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
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.BabyPetInstance;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Broadcast;

/**
 * Beast SpiritShot Handler
 * @author Tempy
 */
public class BeastSpiritShot implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		6646,
		6647
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (playable == null)
		{
			return;
		}
		
		PlayerInstance activeOwner = null;
		if (playable instanceof Summon)
		{
			activeOwner = ((Summon) playable).getOwner();
			activeOwner.sendPacket(SystemMessageId.THIS_PET_CANNOT_USE_THIS_ITEM);
			return;
		}
		else if (playable instanceof PlayerInstance)
		{
			activeOwner = (PlayerInstance) playable;
		}
		
		if (activeOwner == null)
		{
			return;
		}
		
		final Summon activePet = activeOwner.getPet();
		if (activePet == null)
		{
			activeOwner.sendPacket(SystemMessageId.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return;
		}
		
		if (activePet.isDead())
		{
			activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET_OR_SERVITOR_SAD_ISN_T_IT);
			return;
		}
		
		final int itemId = item.getItemId();
		final boolean isBlessed = itemId == 6647;
		int shotConsumption = 1;
		ItemInstance weaponInst = null;
		Weapon weaponItem = null;
		if ((activePet instanceof PetInstance) && !(activePet instanceof BabyPetInstance))
		{
			weaponInst = ((PetInstance) activePet).getActiveWeaponInstance();
			weaponItem = ((PetInstance) activePet).getActiveWeaponItem();
			if (weaponInst == null)
			{
				activeOwner.sendPacket(SystemMessageId.YOU_MAY_NOT_USE_SPIRITSHOTS);
				return;
			}
			
			if (weaponInst.getChargedSpiritshot() != ItemInstance.CHARGED_NONE)
			{
				// SpiritShots are already active.
				return;
			}
			
			final int shotCount = item.getCount();
			shotConsumption = weaponItem.getSpiritShotCount();
			if (shotConsumption == 0)
			{
				activeOwner.sendPacket(SystemMessageId.YOU_MAY_NOT_USE_SPIRITSHOTS);
				return;
			}
			
			if ((shotCount <= shotConsumption))
			{
				// Not enough SpiritShots to use.
				activeOwner.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR);
				return;
			}
			
			if (isBlessed)
			{
				weaponInst.setChargedSpiritshot(ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			}
			else
			{
				weaponInst.setChargedSpiritshot(ItemInstance.CHARGED_SPIRITSHOT);
			}
		}
		else
		{
			if (activePet.getChargedSpiritShot() != ItemInstance.CHARGED_NONE)
			{
				return;
			}
			
			if (isBlessed)
			{
				activePet.setChargedSpiritShot(ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			}
			else
			{
				activePet.setChargedSpiritShot(ItemInstance.CHARGED_SPIRITSHOT);
			}
		}
		
		if (!Config.DONT_DESTROY_SS && !activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
		{
			if (activeOwner.getAutoSoulShot().containsKey(itemId))
			{
				activeOwner.removeAutoSoulShot(itemId);
				activeOwner.sendPacket(new ExAutoSoulShot(itemId, 0));
				final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
				sm.addString(item.getItem().getName());
				activeOwner.sendPacket(sm);
				
				return;
			}
			
			activeOwner.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOTS_FOR_THAT);
			return;
		}
		
		// Pet uses the power of spirit.
		activeOwner.sendPacket(SystemMessageId.PET_USES_THE_POWER_OF_SPIRIT);
		Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, isBlessed ? 2009 : 2008, 1, 0, 0), 360000/* 600 */);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
