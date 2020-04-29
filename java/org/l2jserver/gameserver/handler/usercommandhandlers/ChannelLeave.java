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
import org.l2jserver.gameserver.model.CommandChannel;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Chris
 */
public class ChannelLeave implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		96
	};
	
	@Override
	public boolean useUserCommand(int id, PlayerInstance player)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		if (player == null)
		{
			return false;
		}
		
		if (player.isInParty())
		{
			if (player.getParty().isLeader(player) && player.getParty().isInCommandChannel())
			{
				final CommandChannel channel = player.getParty().getCommandChannel();
				final Party party = player.getParty();
				channel.removeParty(party);
				
				party.getLeader().sendPacket(SystemMessageId.YOU_HAVE_QUIT_THE_COMMAND_CHANNEL);
				channel.broadcastToChannelMembers(new SystemMessage(SystemMessageId.S1_S_PARTY_HAS_LEFT_THE_COMMAND_CHANNEL).addString(party.getLeader().getName()));
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
