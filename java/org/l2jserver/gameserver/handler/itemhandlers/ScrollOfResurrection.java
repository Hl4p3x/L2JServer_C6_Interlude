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

import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class ScrollOfResurrection implements IItemHandler
{
	// All the items ids that this handler knows
	private static final int[] ITEM_IDS =
	{
		737,
		3936,
		3959,
		6387
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SITTING);
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("This Item Cannot Be Used On Olympiad Games.");
		}
		
		if (player.isMovementDisabled())
		{
			return;
		}
		
		final int itemId = item.getItemId();
		final boolean humanScroll = (itemId == 3936) || (itemId == 3959) || (itemId == 737);
		final boolean petScroll = (itemId == 6387) || (itemId == 737);
		
		// SoR Animation section
		final Creature target = (Creature) player.getTarget();
		if ((target != null) && target.isDead())
		{
			PlayerInstance targetPlayer = null;
			if (target instanceof PlayerInstance)
			{
				targetPlayer = (PlayerInstance) target;
			}
			
			PetInstance targetPet = null;
			if (target instanceof PetInstance)
			{
				targetPet = (PetInstance) target;
			}
			
			if ((targetPlayer != null) || (targetPet != null))
			{
				boolean condGood = true;
				
				// check target is not in a active siege zone
				Castle castle = null;
				if (targetPlayer != null)
				{
					castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
				}
				else if (targetPet != null)
				{
					castle = CastleManager.getInstance().getCastle(targetPet.getX(), targetPet.getY(), targetPet.getZ());
				}
				
				if ((castle != null) && castle.getSiege().isInProgress())
				{
					condGood = false;
					player.sendPacket(SystemMessageId.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
				}
				
				if (targetPet != null)
				{
					if (targetPet.getOwner() != player)
					{
						if (targetPet.getOwner().isReviveRequested())
						{
							if (targetPet.getOwner().isRevivingPet())
							{
								player.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
							}
							else
							{
								player.sendPacket(SystemMessageId.A_PET_CANNOT_BE_RESURRECTED_WHILE_IT_S_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
							}
							condGood = false;
						}
					}
					else if (!petScroll)
					{
						condGood = false;
						player.sendMessage("You do not have the correct scroll");
					}
				}
				else if (targetPlayer != null)
				{
					if (targetPlayer.isFestivalParticipant()) // Check to see if the current player target is in a festival.
					{
						condGood = false;
						player.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
					}
					if (targetPlayer.isReviveRequested())
					{
						if (targetPlayer.isRevivingPet())
						{
							player.sendPacket(SystemMessageId.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
						}
						else
						{
							player.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
						}
						condGood = false;
					}
					else if (!humanScroll)
					{
						condGood = false;
						player.sendMessage("You do not have the correct scroll");
					}
				}
				
				if (condGood)
				{
					int skillId = 0;
					final int skillLevel = 1;
					
					switch (itemId)
					{
						case 737:
						{
							skillId = 2014;
							break; // Scroll of Resurrection
						}
						case 3936:
						{
							skillId = 2049;
							break; // Blessed Scroll of Resurrection
						}
						case 3959:
						{
							skillId = 2062;
							break; // L2Day - Blessed Scroll of Resurrection
						}
						case 6387:
						{
							skillId = 2179;
							break; // Blessed Scroll of Resurrection: For Pets
						}
					}
					
					if (skillId != 0)
					{
						final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
						player.useMagic(skill, true, true);
						
						// Consume the scroll
						if (!player.destroyItem("Consume", item.getObjectId(), 1, null, false))
						{
							return;
						}
						
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
						sm.addItemName(itemId);
						player.sendPacket(sm);
					}
				}
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}