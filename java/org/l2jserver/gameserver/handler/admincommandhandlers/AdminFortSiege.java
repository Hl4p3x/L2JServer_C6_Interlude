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
package org.l2jserver.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.instancemanager.FortManager;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles all siege commands: Todo: change the class name, and neaten it up
 * @author programmos
 */
public class AdminFortSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fortsiege",
		"admin_add_fortattacker",
		"admin_add_fortdefender",
		"admin_add_fortguard",
		"admin_list_fortsiege_clans",
		"admin_clear_fortsiege_list",
		"admin_move_fortdefenders",
		"admin_spawn_fortdoors",
		"admin_endfortsiege",
		"admin_startfortsiege",
		"admin_setfort",
		"admin_removefort"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command
		
		// Get fort
		Fort fort = null;
		if (st.hasMoreTokens())
		{
			fort = FortManager.getInstance().getFort(st.nextToken());
		}
		
		// No fort specified
		if ((fort == null) || (fort.getFortId() < 0))
		{
			showFortSelectPage(activeChar);
		}
		else
		{
			final WorldObject target = activeChar.getTarget();
			PlayerInstance player = null;
			if (target instanceof PlayerInstance)
			{
				player = (PlayerInstance) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_fortattacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
				}
				else
				{
					fort.getSiege().registerAttacker(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_add_fortdefender"))
			{
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
				}
				else
				{
					fort.getSiege().registerDefender(player, true);
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				fort.getSiege().listRegisterClan(activeChar);
				
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_fortdefenders"))
			{
				BuilderUtil.sendSysMessage(activeChar, "Not implemented yet.");
			}
			else if (command.equalsIgnoreCase("admin_setfort"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
				}
				else
				{
					fort.setOwner(player.getClan());
				}
			}
			else if (command.equalsIgnoreCase("admin_removefort"))
			{
				final Clan clan = ClanTable.getInstance().getClan(fort.getOwnerId());
				if (clan != null)
				{
					fort.removeOwner(clan);
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Unable to remove fort");
				}
			}
			else if (command.equalsIgnoreCase("admin_spawn_fortdoors"))
			{
				fort.spawnDoor();
			}
			else if (command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}
			
			showFortSiegePage(activeChar, fort.getName());
		}
		
		return true;
	}
	
	private void showFortSelectPage(PlayerInstance activeChar)
	{
		int i = 0;
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/forts.htm");
		final StringBuilder cList = new StringBuilder();
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort != null)
			{
				final String name = fort.getName();
				cList.append("<td fixwidth=90><a action=\"bypass -h admin_fortsiege " + name + "\">" + name + "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		
		adminReply.replace("%forts%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showFortSiegePage(PlayerInstance activeChar, String fortName)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/fort.htm");
		adminReply.replace("%fortName%", fortName);
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
