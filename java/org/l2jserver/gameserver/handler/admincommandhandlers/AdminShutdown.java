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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.l2jserver.Config;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.Shutdown;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - server_shutdown [sec] = shows menu or shuts down server in sec seconds
 * @version $Revision: 1.5.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShutdown implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_server_shutdown",
		"admin_server_restart",
		"admin_server_abort"
	};
	
	private enum CommandEnum
	{
		admin_server_shutdown,
		admin_server_restart,
		admin_server_abort
	}
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_server_shutdown:
			{
				if (st.hasMoreTokens())
				{
					final String secs = st.nextToken();
					try
					{
						final int val = Integer.parseInt(secs);
						if (val >= 0)
						{
							serverShutdown(activeChar, val, false);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Negative Value is not allowed");
						return false;
					}
					catch (StringIndexOutOfBoundsException e)
					{
						sendHtmlForm(activeChar);
						return false;
					}
				}
				sendHtmlForm(activeChar);
				return false;
			}
			case admin_server_restart:
			{
				if (st.hasMoreTokens())
				{
					final String secs = st.nextToken();
					try
					{
						final int val = Integer.parseInt(secs);
						if (val >= 0)
						{
							serverShutdown(activeChar, val, true);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Negative Value is not allowed");
						return false;
					}
					catch (StringIndexOutOfBoundsException e)
					{
						sendHtmlForm(activeChar);
						return false;
					}
				}
				sendHtmlForm(activeChar);
				return false;
			}
			case admin_server_abort:
			{
				serverAbort(activeChar);
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
	
	private void sendHtmlForm(PlayerInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final int t = GameTimeController.getInstance().getGameTime();
		final int h = t / 60;
		final int m = t % 60;
		final SimpleDateFormat format = new SimpleDateFormat("h:mm");
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		adminReply.setFile("data/html/admin/shutdown.htm");
		adminReply.replace("%count%", String.valueOf(World.getAllPlayersCount()));
		adminReply.replace("%used%", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		adminReply.replace("%xp%", String.valueOf(Config.RATE_XP));
		adminReply.replace("%sp%", String.valueOf(Config.RATE_SP));
		adminReply.replace("%adena%", String.valueOf(Config.RATE_DROP_ADENA));
		adminReply.replace("%drop%", String.valueOf(Config.RATE_DROP_ITEMS));
		adminReply.replace("%time%", format.format(cal.getTime()));
		activeChar.sendPacket(adminReply);
	}
	
	private void serverShutdown(PlayerInstance activeChar, int seconds, boolean restart)
	{
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}
	
	private void serverAbort(PlayerInstance activeChar)
	{
		Shutdown.getInstance().abort(activeChar);
	}
}
