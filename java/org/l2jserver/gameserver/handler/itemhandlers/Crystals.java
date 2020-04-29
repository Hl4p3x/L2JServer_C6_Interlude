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

import java.util.logging.Logger;

import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;

public class Crystals implements IItemHandler
{
	protected static final Logger LOGGER = Logger.getLogger(Crystals.class.getName());
	
	private static final int[] ITEM_IDS =
	{
		7906,
		7907,
		7908,
		7909,
		7910,
		7911,
		7912,
		7913,
		7914,
		7915,
		7916,
		7917
	};
	
	@Override
	public synchronized void useItem(Playable playable, ItemInstance item)
	{
		PlayerInstance player;
		// boolean res = false;
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
		Skill skill = null;
		
		switch (itemId)
		{
			case 7906:
			{
				skill = SkillTable.getInstance().getInfo(2248, 1);
				break;
			}
			case 7907:
			{
				skill = SkillTable.getInstance().getInfo(2249, 1);
				break;
			}
			case 7908:
			{
				skill = SkillTable.getInstance().getInfo(2250, 1);
				break;
			}
			case 7909:
			{
				skill = SkillTable.getInstance().getInfo(2251, 1);
				break;
			}
			case 7910:
			{
				skill = SkillTable.getInstance().getInfo(2252, 1);
				break;
			}
			case 7911:
			{
				skill = SkillTable.getInstance().getInfo(2253, 1);
				break;
			}
			case 7912:
			{
				skill = SkillTable.getInstance().getInfo(2254, 1);
				break;
			}
			case 7913:
			{
				skill = SkillTable.getInstance().getInfo(2255, 1);
				break;
			}
			case 7914:
			{
				skill = SkillTable.getInstance().getInfo(2256, 1);
				break;
			}
			case 7915:
			{
				skill = SkillTable.getInstance().getInfo(2257, 1);
				break;
			}
			case 7916:
			{
				skill = SkillTable.getInstance().getInfo(2258, 1);
				break;
			}
			case 7917:
			{
				skill = SkillTable.getInstance().getInfo(2259, 1);
				break;
			}
		}
		if (skill != null)
		{
			player.doCast(skill);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}