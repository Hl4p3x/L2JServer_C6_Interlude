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
import java.sql.SQLException;
import java.util.StringTokenizer;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.LoginServerThread;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ServerClose;
import org.l2jserver.gameserver.util.BuilderUtil;
import org.l2jserver.gameserver.util.GMAudit;

/**
 * This class handles following admin commands: - ban_acc <account_name> = changes account access level to -100 and logs him off. If no account is specified target's account is used. - ban_char <char_name> = changes a characters access level to -100 and logs him off. If no character is specified
 * target is used. - ban_chat <char_name> <duration> = chat bans a character for the specified duration. If no name is specified the target is chat banned indefinitely. - unban_acc <account_name> = changes account access level to 0. - unban_char <char_name> = changes specified characters access
 * level to 0. - unban_chat <char_name> = lifts chat ban from specified player. If no player name is specified current target is used. - jail charname [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified. - unjail charname = Unjails player, teleport him to
 * Floran.
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ban", // returns ban commands
		"admin_ban_acc",
		"admin_ban_char",
		"admin_banchat",
		"admin_unban", // returns unban commands
		"admin_unban_acc",
		"admin_unban_char",
		"admin_unbanchat",
		"admin_jail",
		"admin_unjail"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";
		int duration = -1;
		PlayerInstance targetPlayer = null;
		if (st.hasMoreTokens())
		{
			player = st.nextToken();
			targetPlayer = World.getInstance().getPlayer(player);
			if (st.hasMoreTokens())
			{
				try
				{
					duration = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
					BuilderUtil.sendSysMessage(activeChar, "Invalid number format used: " + nfe);
					return false;
				}
			}
		}
		else if ((activeChar.getTarget() != null) && (activeChar.getTarget() instanceof PlayerInstance))
		{
			targetPlayer = (PlayerInstance) activeChar.getTarget();
		}
		
		if ((targetPlayer != null) && targetPlayer.equals(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_ON_YOURSELF);
			return false;
		}
		
		if (command.startsWith("admin_ban ") || command.equalsIgnoreCase("admin_ban"))
		{
			BuilderUtil.sendSysMessage(activeChar, "Available ban commands: //ban_acc, //ban_char, //ban_chat");
			return false;
		}
		else if (command.startsWith("admin_ban_acc"))
		{
			// May need to check usage in admin_ban_menu as well.
			if ((targetPlayer == null) && player.equals(""))
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //ban_acc <account_name> (if none, target char's account gets banned)");
				return false;
			}
			else if (targetPlayer == null)
			{
				LoginServerThread.getInstance().sendAccessLevel(player, -100);
				BuilderUtil.sendSysMessage(activeChar, "Ban request sent for account " + player);
				auditAction(command, activeChar, player);
			}
			else
			{
				targetPlayer.setPunishLevel(PlayerInstance.PunishLevel.ACC, 0);
				BuilderUtil.sendSysMessage(activeChar, "Account " + targetPlayer.getAccountName() + " banned.");
				auditAction(command, activeChar, targetPlayer.getAccountName());
			}
		}
		else if (command.startsWith("admin_ban_char"))
		{
			if ((targetPlayer == null) && player.equals(""))
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //ban_char <char_name> (if none, target char is banned)");
				return false;
			}
			auditAction(command, activeChar, (targetPlayer == null ? player : targetPlayer.getName()));
			return changeCharAccessLevel(targetPlayer, player, activeChar, -100);
		}
		else if (command.startsWith("admin_banchat"))
		{
			if ((targetPlayer == null) && player.equals(""))
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //banchat <char_name> [penalty_minutes]");
				return false;
			}
			if (targetPlayer != null)
			{
				if (targetPlayer.getPunishLevel().value() > 0)
				{
					activeChar.sendMessage(targetPlayer.getName() + " is already jailed or banned.");
					return false;
				}
				String banLengthStr = "";
				targetPlayer.setPunishLevel(PlayerInstance.PunishLevel.CHAT, duration);
				if (duration > 0)
				{
					banLengthStr = " for " + duration + " minutes";
				}
				activeChar.sendMessage(targetPlayer.getName() + " is now chat banned" + banLengthStr + ".");
				auditAction(command, activeChar, targetPlayer.getName());
			}
			else
			{
				banChatOfflinePlayer(activeChar, player, duration, true);
				auditAction(command, activeChar, player);
			}
		}
		else if (command.startsWith("admin_unbanchat"))
		{
			if ((targetPlayer == null) && player.equals(""))
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //unbanchat <char_name>");
				return false;
			}
			if (targetPlayer != null)
			{
				if (targetPlayer.isChatBanned())
				{
					targetPlayer.setPunishLevel(PlayerInstance.PunishLevel.NONE, 0);
					activeChar.sendMessage(targetPlayer.getName() + "'s chat ban has now been lifted.");
					auditAction(command, activeChar, targetPlayer.getName());
				}
				else
				{
					activeChar.sendMessage(targetPlayer.getName() + " is not currently chat banned.");
				}
			}
			else
			{
				banChatOfflinePlayer(activeChar, player, 0, false);
				auditAction(command, activeChar, player);
			}
		}
		else if (command.startsWith("admin_unban ") || command.equalsIgnoreCase("admin_unban"))
		{
			BuilderUtil.sendSysMessage(activeChar, "Available unban commands: //unban_acc, //unban_char, //unban_chat");
			return false;
		}
		else if (command.startsWith("admin_unban_acc"))
		{
			// Need to check admin_unban_menu command as well in AdminMenu.java handler.
			if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else if (!player.equals(""))
			{
				LoginServerThread.getInstance().sendAccessLevel(player, 0);
				BuilderUtil.sendSysMessage(activeChar, "Unban request sent for account " + player);
				auditAction(command, activeChar, player);
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //unban_acc <account_name>");
				return false;
			}
		}
		else if (command.startsWith("admin_unban_char"))
		{
			if ((targetPlayer == null) && player.equals(""))
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //unban_char <char_name>");
				return false;
			}
			else if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else
			{
				auditAction(command, activeChar, player);
				return changeCharAccessLevel(null, player, activeChar, 0);
			}
		}
		else if (command.startsWith("admin_jail"))
		{
			if ((targetPlayer == null) && player.equals(""))
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //jail <charname> [penalty_minutes] (if no name is given, selected target is jailed indefinitely)");
				return false;
			}
			if (targetPlayer != null)
			{
				targetPlayer.setPunishLevel(PlayerInstance.PunishLevel.JAIL, duration);
				BuilderUtil.sendSysMessage(activeChar, "Character " + targetPlayer.getName() + " jailed for " + (duration > 0 ? duration + " minutes." : "ever!"));
				auditAction(command, activeChar, targetPlayer.getName());
				if (targetPlayer.getParty() != null)
				{
					targetPlayer.getParty().removePartyMember(targetPlayer);
				}
			}
			else
			{
				jailOfflinePlayer(activeChar, player, duration);
				auditAction(command, activeChar, player);
			}
		}
		else if (command.startsWith("admin_unjail"))
		{
			if ((targetPlayer == null) && player.equals(""))
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //unjail <charname> (If no name is given target is used)");
				return false;
			}
			else if (targetPlayer != null)
			{
				targetPlayer.setPunishLevel(PlayerInstance.PunishLevel.NONE, 0);
				BuilderUtil.sendSysMessage(activeChar, "Character " + targetPlayer.getName() + " removed from jail");
				auditAction(command, activeChar, targetPlayer.getName());
			}
			else
			{
				unjailOfflinePlayer(activeChar, player);
				auditAction(command, activeChar, player);
			}
		}
		return true;
	}
	
	private void auditAction(String fullCommand, PlayerInstance activeChar, String target)
	{
		if (!Config.GMAUDIT)
		{
			return;
		}
		
		final String[] command = fullCommand.split(" ");
		GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", command[0], (target.equals("") ? "no-target" : target), (command.length > 2 ? command[2] : ""));
	}
	
	private void banChatOfflinePlayer(PlayerInstance activeChar, String name, int delay, boolean ban)
	{
		int level = 0;
		long value = 0;
		if (ban)
		{
			level = PlayerInstance.PunishLevel.CHAT.value();
			value = (delay > 0 ? delay * 60000 : 60000);
		}
		else
		{
			level = PlayerInstance.PunishLevel.NONE.value();
			value = 0;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, level);
			statement.setLong(2, value);
			statement.setString(3, name);
			statement.execute();
			final int count = statement.getUpdateCount();
			statement.close();
			
			if (count == 0)
			{
				BuilderUtil.sendSysMessage(activeChar, "Character not found!");
			}
			else if (ban)
			{
				BuilderUtil.sendSysMessage(activeChar, "Character " + name + " chat-banned for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Character " + name + "'s chat-banned lifted");
			}
		}
		catch (SQLException se)
		{
			BuilderUtil.sendSysMessage(activeChar, "SQLException while chat-banning player");
		}
	}
	
	private void jailOfflinePlayer(PlayerInstance activeChar, String name, int delay)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, PlayerInstance.PunishLevel.JAIL.value());
			statement.setLong(5, (delay > 0 ? delay * 60000 : 0));
			statement.setString(6, name);
			statement.execute();
			final int count = statement.getUpdateCount();
			statement.close();
			
			if (count == 0)
			{
				BuilderUtil.sendSysMessage(activeChar, "Character not found!");
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
		}
		catch (SQLException se)
		{
			BuilderUtil.sendSysMessage(activeChar, "SQLException while jailing player");
		}
	}
	
	private void unjailOfflinePlayer(PlayerInstance activeChar, String name)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.execute();
			final int count = statement.getUpdateCount();
			statement.close();
			if (count == 0)
			{
				BuilderUtil.sendSysMessage(activeChar, "Character not found!");
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Character " + name + " removed from jail");
			}
		}
		catch (SQLException se)
		{
			BuilderUtil.sendSysMessage(activeChar, "SQLException while jailing player");
		}
	}
	
	private boolean changeCharAccessLevel(PlayerInstance targetPlayer, String player, PlayerInstance activeChar, int lvl)
	{
		boolean output = false;
		if (targetPlayer != null)
		{
			targetPlayer.setAccessLevel(lvl);
			targetPlayer.sendMessage("Your character has been banned. Contact the administrator for more informations.");
			
			try
			{
				// Save player status
				targetPlayer.store();
				
				// Player Disconnect like L2OFF, no client crash.
				if (targetPlayer.getClient() != null)
				{
					targetPlayer.getClient().sendPacket(ServerClose.STATIC_PACKET);
					targetPlayer.getClient().setPlayer(null);
					targetPlayer.setClient(null);
				}
			}
			catch (Throwable t)
			{
			}
			
			targetPlayer.deleteMe();
			
			BuilderUtil.sendSysMessage(activeChar, "The character " + targetPlayer.getName() + " has now been banned.");
			output = true;
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");
				statement.setInt(1, lvl);
				statement.setString(2, player);
				statement.execute();
				final int count = statement.getUpdateCount();
				statement.close();
				if (count == 0)
				{
					BuilderUtil.sendSysMessage(activeChar, "Character not found or access level unaltered.");
				}
				else
				{
					activeChar.sendMessage(player + " now has an access level of " + lvl);
					output = true;
				}
			}
			catch (SQLException se)
			{
				BuilderUtil.sendSysMessage(activeChar, "SQLException while changing character's access level");
			}
		}
		return output;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}