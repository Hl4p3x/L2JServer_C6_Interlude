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
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;

public class CustomPotions implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		9720,
		9721,
		9722,
		9723,
		9724,
		9725,
		9726,
		9727,
		9728,
		9729,
		9730,
		9731,
	};
	
	@Override
	public synchronized void useItem(Playable playable, ItemInstance item)
	{
		PlayerInstance player;
		boolean res = false;
		if (playable instanceof PlayerInstance)
		{
			player = (PlayerInstance) playable;
		}
		else if (playable instanceof PetInstance)
		{
			player = ((PetInstance) playable).getOwner();
		}
		else
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		if (player.isAllSkillsDisabled())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int itemId = item.getItemId();
		if ((itemId >= 9720) && (itemId <= 9731))
		{
			res = usePotion(player, itemId, 1);
		}
		
		if (res)
		{
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	public boolean usePotion(PlayerInstance player, int magicId, int level)
	{
		final Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
			player.doCast(skill);
			if (((!player.isSitting() && !player.isParalyzed() && !player.isFakeDeath()) || skill.isPotion()))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
