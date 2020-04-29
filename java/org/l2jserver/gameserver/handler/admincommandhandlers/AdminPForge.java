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

import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.AdminForgePacket;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles commands for GM to forge packets
 * @author Maktakien
 */
public class AdminPForge implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_forge",
		"admin_forge2",
		"admin_forge3"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.equals("admin_forge"))
		{
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_forge2"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				final String format = st.nextToken();
				showPage2(activeChar, format);
			}
			catch (Exception ex)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //forge2 format");
			}
		}
		else if (command.startsWith("admin_forge3"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				String format = st.nextToken();
				boolean broadcast = false;
				if (format.equalsIgnoreCase("broadcast"))
				{
					format = st.nextToken();
					broadcast = true;
				}
				
				final AdminForgePacket sp = new AdminForgePacket();
				for (int i = 0; i < format.length(); i++)
				{
					String val = st.nextToken();
					if (val.equalsIgnoreCase("$objid"))
					{
						val = String.valueOf(activeChar.getObjectId());
					}
					else if (val.equalsIgnoreCase("$tobjid"))
					{
						val = String.valueOf(activeChar.getTarget().getObjectId());
					}
					else if (val.equalsIgnoreCase("$bobjid"))
					{
						if (activeChar.getBoat() != null)
						{
							val = String.valueOf(activeChar.getBoat().getObjectId());
						}
					}
					else if (val.equalsIgnoreCase("$clarid"))
					{
						val = String.valueOf(((PlayerInstance) activeChar.getTarget()).getObjectId());
					}
					else if (val.equalsIgnoreCase("$allyid"))
					{
						val = String.valueOf(activeChar.getAllyId());
					}
					else if (val.equalsIgnoreCase("$tclanid"))
					{
						val = String.valueOf(((PlayerInstance) activeChar.getTarget()).getClanId());
					}
					else if (val.equalsIgnoreCase("$tallyid"))
					{
						val = String.valueOf(((PlayerInstance) activeChar.getTarget()).getAllyId());
					}
					else if (val.equalsIgnoreCase("$x"))
					{
						val = String.valueOf(activeChar.getX());
					}
					else if (val.equalsIgnoreCase("$y"))
					{
						val = String.valueOf(activeChar.getY());
					}
					else if (val.equalsIgnoreCase("$z"))
					{
						val = String.valueOf(activeChar.getZ());
					}
					else if (val.equalsIgnoreCase("$heading"))
					{
						val = String.valueOf(activeChar.getHeading());
					}
					else if (val.equalsIgnoreCase("$tx"))
					{
						val = String.valueOf(activeChar.getTarget().getX());
					}
					else if (val.equalsIgnoreCase("$ty"))
					{
						val = String.valueOf(activeChar.getTarget().getY());
					}
					else if (val.equalsIgnoreCase("$tz"))
					{
						val = String.valueOf(activeChar.getTarget().getZ());
					}
					else if (val.equalsIgnoreCase("$theading"))
					{
						val = String.valueOf(((PlayerInstance) activeChar.getTarget()).getHeading());
					}
					
					sp.addPart(format.getBytes()[i], val);
				}
				if (broadcast)
				{
					activeChar.broadcastPacket(sp);
				}
				else
				{
					activeChar.sendPacket(sp);
				}
				showPage3(activeChar, format, command);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return true;
	}
	
	private void showMainPage(PlayerInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "pforge1.htm");
	}
	
	private void showPage2(PlayerInstance activeChar, String format)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/pforge2.htm");
		adminReply.replace("%format%", format);
		StringBuilder replyMSG = new StringBuilder();
		for (int i = 0; i < format.length(); i++)
		{
			replyMSG.append(format.charAt(i) + " : <edit var=\"v" + i + "\" width=100><br1>");
		}
		
		adminReply.replace("%valueditors%", replyMSG.toString());
		replyMSG = new StringBuilder();
		for (int i = 0; i < format.length(); i++)
		{
			replyMSG.append(" \\$v" + i);
		}
		
		adminReply.replace("%send%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showPage3(PlayerInstance activeChar, String format, String command)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/pforge3.htm");
		adminReply.replace("%format%", format);
		adminReply.replace("%command%", command);
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
