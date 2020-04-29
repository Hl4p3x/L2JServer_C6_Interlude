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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * @author Mobius
 */
public class AdminOnline implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_online"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.equalsIgnoreCase("admin_online"))
		{
			final List<String> ips = new ArrayList<>();
			int total = 0;
			int online = 0;
			int offline = 0;
			int peace = 0;
			int notPeace = 0;
			// int instanced = 0;
			int combat = 0;
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				if (player.getClient() != null)
				{
					final String ip = player.getClient().getIpAddress();
					if ((ip != null) && !ips.contains(ip))
					{
						ips.add(ip);
					}
				}
				
				total++;
				
				if (player.isInOfflineMode())
				{
					offline++;
				}
				else if (player.isOnline())
				{
					online++;
				}
				
				if (player.isInsideZone(ZoneId.PEACE))
				{
					peace++;
				}
				else
				{
					notPeace++;
				}
				
				// if (player.getInstanceId() > 0)
				// {
				// instanced++;
				// }
				if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || (player.getPvpFlag() > 0) || player.isInsideZone(ZoneId.PVP) || player.isInsideZone(ZoneId.SIEGE))
				{
					combat++;
				}
			}
			
			BuilderUtil.sendSysMessage(activeChar, "Online Player Report");
			BuilderUtil.sendSysMessage(activeChar, "Total count: " + total);
			BuilderUtil.sendSysMessage(activeChar, "Total online: " + online);
			BuilderUtil.sendSysMessage(activeChar, "Total offline: " + offline);
			BuilderUtil.sendSysMessage(activeChar, "Max connected: " + World.MAX_CONNECTED_COUNT);
			BuilderUtil.sendSysMessage(activeChar, "Unique IPs: " + ips.size());
			BuilderUtil.sendSysMessage(activeChar, "In peace zone: " + peace);
			BuilderUtil.sendSysMessage(activeChar, "Not in peace zone: " + notPeace);
			// BuilderUtil.sendSysMessage(activeChar, "In instances: " + instanced);
			BuilderUtil.sendSysMessage(activeChar, "In combat: " + combat);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}