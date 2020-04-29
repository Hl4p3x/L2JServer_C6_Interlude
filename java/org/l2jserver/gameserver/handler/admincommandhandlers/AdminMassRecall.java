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

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - recallparty - recallclan - recallally
 * @author Yamaneko
 */
public class AdminMassRecall implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_recallclan",
		"admin_recallparty",
		"admin_recallally"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.startsWith("admin_recallclan"))
		{
			try
			{
				final String val = command.substring(17).trim();
				final Clan clan = ClanTable.getInstance().getClanByName(val);
				if (clan == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "This clan doesn't exists.");
					return true;
				}
				
				final PlayerInstance[] m = clan.getOnlineMembers();
				for (PlayerInstance element : m)
				{
					Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Error in recallclan command.");
			}
		}
		else if (command.startsWith("admin_recallally"))
		{
			try
			{
				final String val = command.substring(17).trim();
				final Clan clan = ClanTable.getInstance().getClanByName(val);
				if (clan == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "This clan doesn't exists.");
					return true;
				}
				
				final int ally = clan.getAllyId();
				if (ally == 0)
				{
					final PlayerInstance[] m = clan.getOnlineMembers();
					for (PlayerInstance element : m)
					{
						Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
					}
				}
				else
				{
					for (Clan aclan : ClanTable.getInstance().getClans())
					{
						if (aclan.getAllyId() == ally)
						{
							final PlayerInstance[] m = aclan.getOnlineMembers();
							for (PlayerInstance element : m)
							{
								Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Error in recallally command.");
			}
		}
		else if (command.startsWith("admin_recallparty"))
		{
			try
			{
				final String val = command.substring(18).trim();
				final PlayerInstance player = World.getInstance().getPlayer(val);
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Target error.");
					return true;
				}
				
				if (!player.isInParty())
				{
					BuilderUtil.sendSysMessage(activeChar, "Player is not in party.");
					return true;
				}
				
				final Party p = player.getParty();
				for (PlayerInstance ppl : p.getPartyMembers())
				{
					Teleport(ppl, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Error in recallparty command.");
			}
		}
		return true;
	}
	
	private void Teleport(PlayerInstance player, int x, int y, int z, String message)
	{
		player.sendMessage(message);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(x, y, z, true);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}
