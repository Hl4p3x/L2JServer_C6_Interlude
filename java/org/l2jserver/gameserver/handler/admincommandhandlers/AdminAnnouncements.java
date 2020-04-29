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

import org.l2jserver.Config;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.handler.AutoAnnouncementHandler;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.util.Broadcast;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - announce text = announces text to all players - list_announcements = show menu - reload_announcements = reloads announcements from txt file - announce_announcements = announce all stored announcements to all players - add_announcement text = adds
 * text to startup announcements - del_announcement id = deletes announcement with respective id
 * @version $Revision: 1.4.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminAnnouncements implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_list_announcements",
		"admin_reload_announcements",
		"admin_announce_announcements",
		"admin_add_announcement",
		"admin_del_announcement",
		"admin_announce",
		"admin_critannounce",
		"admin_announce_menu",
		"admin_list_autoannouncements",
		"admin_add_autoannouncement",
		"admin_del_autoannouncement",
		"admin_autoannounce"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		String text = "";
		int index = 0;
		
		switch (comm)
		{
			case "admin_list_announcements":
			{
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			}
			case "admin_reload_announcements":
			{
				Announcements.getInstance().loadAnnouncements();
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			}
			case "admin_announce_menu":
			{
				if (st.hasMoreTokens())
				{
					text = command.replace(comm + " ", "");
					// text = st.nextToken();
				}
				if (!text.equals(""))
				{
					Announcements.getInstance().announceToAll(text);
				}
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			}
			case "admin_announce_announcements":
			{
				for (PlayerInstance player : World.getInstance().getAllPlayers())
				{
					Announcements.getInstance().showAnnouncements(player);
				}
				Announcements.getInstance().listAnnouncements(activeChar);
				return true;
			}
			case "admin_add_announcement":
			{
				if (st.hasMoreTokens())
				{
					text = command.replace(comm + " ", "");
				}
				if (!text.equals(""))
				{
					Announcements.getInstance().addAnnouncement(text);
					Announcements.getInstance().listAnnouncements(activeChar);
					return true;
				}
				BuilderUtil.sendSysMessage(activeChar, "You cannot announce Empty message");
				return false;
			}
			case "admin_del_announcement":
			{
				if (st.hasMoreTokens())
				{
					final String index_s = st.nextToken();
					try
					{
						index = Integer.parseInt(index_s);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //del_announcement <index> (number >=0)");
					}
				}
				if (index >= 0)
				{
					Announcements.getInstance().delAnnouncement(index);
					Announcements.getInstance().listAnnouncements(activeChar);
					return true;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //del_announcement <index> (number >=0)");
				return false;
			}
			case "admin_announce":
			{
				// Call method from another class
				if (Config.GM_ANNOUNCER_NAME)
				{
					command = command + " [ " + activeChar.getName() + " ]";
				}
				Announcements.getInstance().handleAnnounce(command, 15);
				return true;
			}
			case "admin_critannounce":
			{
				String text1 = command.substring(19);
				if (Config.GM_CRITANNOUNCER_NAME && (text1.length() > 0))
				{
					text1 = activeChar.getName() + ": " + text1;
				}
				Broadcast.toAllOnlinePlayers(new CreatureSay(activeChar.getObjectId(), ChatType.CRITICAL_ANNOUNCE, "", text1));
				return true;
			}
			case "admin_list_autoannouncements":
			{
				AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
				return true;
			}
			case "admin_add_autoannouncement":
			{
				if (st.hasMoreTokens())
				{
					int delay = 0;
					try
					{
						delay = Integer.parseInt(st.nextToken().trim());
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
						return false;
					}
					if (st.hasMoreTokens())
					{
						String autoAnnounce = st.nextToken();
						if (delay > 30)
						{
							while (st.hasMoreTokens())
							{
								autoAnnounce = autoAnnounce + " " + st.nextToken();
							}
							AutoAnnouncementHandler.getInstance().registerAnnouncment(autoAnnounce, delay);
							AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
						return false;
					}
					BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
					return false;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //add_autoannouncement <delay> (Seconds > 30) <Announcements>");
				return false;
			}
			case "admin_del_autoannouncement":
			{
				if (st.hasMoreTokens())
				{
					try
					{
						index = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //del_autoannouncement <index> (number >= 0)");
						return false;
					}
					if (index >= 0)
					{
						AutoAnnouncementHandler.getInstance().removeAnnouncement(index);
						AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //del_autoannouncement <index> (number >= 0)");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //del_autoannouncement <index> (number >= 0)");
					return false;
				}
				return false;
			}
			case "admin_autoannounce":
			{
				AutoAnnouncementHandler.getInstance().listAutoAnnouncements(activeChar);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}