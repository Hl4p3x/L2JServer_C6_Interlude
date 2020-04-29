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

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.xml.AdminData;
import org.l2jserver.gameserver.handler.AdminCommandHandler;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.util.GMAudit;

/**
 * This class handles all GM commands triggered by //command
 */
public class SendBypassBuildCmd extends GameClientPacket
{
	protected static final Logger LOGGER = Logger.getLogger(SendBypassBuildCmd.class.getName());
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;
	
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = "admin_" + readS().trim();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if ((player == null) || !player.isGM())
		{
			return;
		}
		
		// Check if handler exists.
		final IAdminCommandHandler handler = AdminCommandHandler.getInstance().getAdminCommandHandler(_command);
		if (handler == null)
		{
			player.sendMessage("The command " + _command + " does not exist!");
			LOGGER.warning("No handler registered for admin command '" + _command + "'.");
			return;
		}
		
		// Check access level and notify requester if not allowed to use this command.
		if (!AdminData.getInstance().hasAccess(_command, player.getAccessLevel()))
		{
			player.sendMessage("You don't have the access right to use this command!");
			LOGGER.warning("Character " + player.getName() + " tried to use admin command " + _command + ", but doesn't have access to it!");
			return;
		}
		
		// Log is GM audit is enabled.
		if (Config.GMAUDIT)
		{
			GMAudit.auditGMAction(player.getName() + " [" + player.getObjectId() + "]", _command, (player.getTarget() != null ? player.getTarget().getName() : "no-target"));
		}
		
		// Run the command.
		handler.useAdminCommand(_command, player);
	}
}
