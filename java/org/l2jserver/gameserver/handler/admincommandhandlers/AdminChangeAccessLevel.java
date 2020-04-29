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

import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * <b>This class handles Access Level Management commands:</b>
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_changelvl"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		handleChangeLevel(command, activeChar);
		return true;
	}
	
	/**
	 * @param command
	 * @param activeChar
	 */
	private void handleChangeLevel(String command, PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		final String[] parts = command.split(" ");
		if (parts.length == 2)
		{
			final int lvl = Integer.parseInt(parts[1]);
			if (activeChar.getTarget() instanceof PlayerInstance)
			{
				((PlayerInstance) activeChar.getTarget()).setAccessLevel(lvl);
				BuilderUtil.sendSysMessage(activeChar, "You have changed the access level of player " + activeChar.getTarget().getName() + " to " + lvl + " .");
			}
		}
		else if (parts.length == 3)
		{
			final int lvl = Integer.parseInt(parts[2]);
			final PlayerInstance player = World.getInstance().getPlayer(parts[1]);
			if (player != null)
			{
				player.setAccessLevel(lvl);
				BuilderUtil.sendSysMessage(activeChar, "You have changed the access level of player " + activeChar.getTarget().getName() + " to " + lvl + " .");
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
