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
import org.l2jserver.gameserver.model.skills.effects.EffectCharge;
import org.l2jserver.gameserver.model.skills.handlers.SkillCharge;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class EnergyStone implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5589
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		PlayerInstance player;
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
		if (item.getItemId() != 5589)
		{
			return;
		}
		if (player.isAllSkillsDisabled())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SITTING);
			return;
		}
		
		final SkillCharge skill = getChargeSkill(player);
		if (skill == null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addItemName(5589);
			player.sendPacket(sm);
			return;
		}
		
		final SystemMessage sm1 = new SystemMessage(SystemMessageId.USE_S1);
		sm1.addItemName(5589);
		player.sendPacket(sm1);
		
		final EffectCharge effect = player.getChargeEffect();
		if (effect == null)
		{
			final Skill dummy = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
			if (dummy != null)
			{
				dummy.getEffects(player, player);
				player.sendPacket(new MagicSkillUse(playable, player, skill.getId(), 1, 1, 0));
				player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
			}
			return;
		}
		
		if (effect.numCharges < 2)
		{
			effect.addNumCharges(1);
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL);
			sm.addNumber(effect.getLevel());
			player.sendPacket(sm);
		}
		else if (effect.numCharges == 2)
		{
			player.sendPacket(SystemMessageId.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY);
		}
		
		final MagicSkillUse msu = new MagicSkillUse(playable, player, skill.getId(), 1, 1, 0);
		player.sendPacket(msu);
		player.broadcastPacket(msu);
		player.sendPacket(new EtcStatusUpdate(player));
		player.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}
	
	private SkillCharge getChargeSkill(PlayerInstance player)
	{
		for (Skill s : player.getAllSkills())
		{
			if ((s.getId() == 50) || (s.getId() == 8))
			{
				return (SkillCharge) s;
			}
		}
		return null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}