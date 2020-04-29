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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.xml.AdminData;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.util.BuilderUtil;

public class AdminNoble implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_setnoble"
	};
	
	protected static final Logger LOGGER = Logger.getLogger(AdminNoble.class.getName());
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.startsWith("admin_setnoble"))
		{
			final WorldObject target = activeChar.getTarget();
			if (target instanceof PlayerInstance)
			{
				final PlayerInstance targetPlayer = (PlayerInstance) target;
				final boolean newNoble = !targetPlayer.isNoble();
				if (newNoble)
				{
					targetPlayer.setNoble(true);
					targetPlayer.sendMessage("You are now a noblesse.");
					updateDatabase(targetPlayer, true);
					sendMessages(true, targetPlayer, activeChar, true);
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
				}
				else
				{
					targetPlayer.setNoble(false);
					targetPlayer.sendMessage("You are no longer a noblesse.");
					updateDatabase(targetPlayer, false);
					sendMessages(false, targetPlayer, activeChar, true);
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Impossible to set a non Player Target as noble.");
				LOGGER.info("GM: " + activeChar.getName() + " is trying to set a non Player Target as noble.");
				return false;
			}
		}
		
		return true;
	}
	
	private void sendMessages(boolean forNewNoble, PlayerInstance player, PlayerInstance gm, boolean notifyGmList)
	{
		if (forNewNoble)
		{
			player.sendMessage(gm.getName() + " has granted Noble Status from you!");
			gm.sendMessage("You've granted Noble Status from " + player.getName());
			if (notifyGmList)
			{
				AdminData.broadcastMessageToGMs("Warn: " + gm.getName() + " has set " + player.getName() + " as Noble !");
			}
		}
		else
		{
			player.sendMessage(gm.getName() + " has revoked Noble Status for you!");
			gm.sendMessage("You've revoked Noble Status for " + player.getName());
			if (notifyGmList)
			{
				AdminData.broadcastMessageToGMs("Warn: " + gm.getName() + " has removed Noble Status of player" + player.getName());
			}
		}
	}
	
	private void updateDatabase(PlayerInstance player, boolean newNoble)
	{
		if (player == null)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement stmt = con.prepareStatement(newNoble ? INSERT_DATA : DEL_DATA);
			if (newNoble)
			{
				stmt.setInt(1, player.getObjectId());
				stmt.setString(2, player.getName());
				stmt.setInt(3, player.isHero() ? 1 : 0);
				stmt.setInt(4, 1);
				stmt.setInt(5, player.isDonator() ? 1 : 0);
				stmt.execute();
				stmt.close();
			}
			else // deletes from database
			{
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
				stmt.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error: could not update database: " + e);
		}
	}
	
	String INSERT_DATA = "REPLACE INTO characters_custom_data (obj_Id, char_name, hero, noble, donator) VALUES (?,?,?,?,?)";
	String DEL_DATA = "UPDATE characters_custom_data SET noble = 0 WHERE obj_Id=?";
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
