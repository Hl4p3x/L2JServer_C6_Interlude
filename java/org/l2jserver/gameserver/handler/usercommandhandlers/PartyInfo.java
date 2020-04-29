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
package org.l2jserver.gameserver.handler.usercommandhandlers;

import org.l2jserver.gameserver.handler.IUserCommandHandler;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Support for /partyinfo command Added by Tempy - 28 Jul 05
 */
public class PartyInfo implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		81
	};
	
	@Override
	public boolean useUserCommand(int id, PlayerInstance player)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		if (!player.isInParty())
		{
			return false;
		}
		
		final Party playerParty = player.getParty();
		final int memberCount = playerParty.getMemberCount();
		final int lootDistribution = playerParty.getLootDistribution();
		final String partyLeader = playerParty.getPartyMembers().get(0).getName();
		player.sendPacket(SystemMessageId.PARTY_INFORMATION);
		
		switch (lootDistribution)
		{
			case Party.ITEM_LOOTER:
			{
				player.sendPacket(SystemMessageId.LOOTING_METHOD_FINDERS_KEEPERS);
				break;
			}
			case Party.ITEM_ORDER:
			{
				player.sendPacket(SystemMessageId.LOOTING_METHOD_BY_TURN);
				break;
			}
			case Party.ITEM_ORDER_SPOIL:
			{
				player.sendPacket(SystemMessageId.LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL);
				break;
			}
			case Party.ITEM_RANDOM:
			{
				player.sendPacket(SystemMessageId.LOOTING_METHOD_RANDOM);
				break;
			}
			case Party.ITEM_RANDOM_SPOIL:
			{
				player.sendPacket(SystemMessageId.LOOTING_METHOD_RANDOM_INCLUDING_SPOIL);
				break;
			}
		}
		player.sendPacket(new SystemMessage(SystemMessageId.PARTY_LEADER_S1).addString(partyLeader));
		player.sendMessage("Members: " + memberCount + "/9");
		player.sendPacket(SystemMessageId.EMPTY_3);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
