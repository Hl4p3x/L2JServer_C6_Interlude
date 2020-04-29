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

import java.util.logging.Logger;

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.clan.ClanMember;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class RequestGiveNickName extends GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestGiveNickName.class.getName());
	
	private String _target;
	private String _title;
	
	@Override
	protected void readImpl()
	{
		_target = readS();
		_title = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Noblesse can bestow a title to themselves
		if (player.isNoble() && _target.matches(player.getName()))
		{
			player.setTitle(_title);
			player.sendPacket(new SystemMessage(SystemMessageId.YOUR_TITLE_HAS_BEEN_CHANGED));
			player.broadcastTitleInfo();
		}
		// Can the player change/give a title?
		else if ((player.getClanPrivileges() & Clan.CP_CL_GIVE_TITLE) == Clan.CP_CL_GIVE_TITLE)
		{
			if (player.getClan().getLevel() < 3)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.A_PLAYER_CAN_ONLY_BE_GRANTED_A_TITLE_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE));
				return;
			}
			
			final ClanMember member1 = player.getClan().getClanMember(_target);
			if (member1 != null)
			{
				final PlayerInstance member = member1.getPlayerInstance();
				if (member != null)
				{
					// is target from the same clan?
					member.setTitle(_title);
					member.sendPacket(new SystemMessage(SystemMessageId.YOUR_TITLE_HAS_BEEN_CHANGED));
					member.broadcastTitleInfo();
				}
				else
				{
					player.sendMessage("Target needs to be online to get a title.");
				}
			}
			else
			{
				player.sendMessage("Target does not belong to your clan.");
			}
		}
	}
}
