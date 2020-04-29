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
package org.l2jserver.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.handler.ICustomByPassHandler;
import org.l2jserver.gameserver.handler.IVoicedCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * <b><u>User Character .repair voicecommand - SL2 L2JEmu</u></b><br>
 * <br>
 * <u>NOTICE:</u> Voice command .repair that when used, allows player to try to repair any of characters on his account, by setting spawn to Floran, removing all shortcuts and moving everything equipped to that char warehouse.<br>
 * <br>
 * (solving client crashes on character entering world)<br>
 * <br>
 * @author szponiasty
 * @version $Revision: 0.17.2.95.2.9 $ $Date: 2010/03/03 9:07:11 $
 */
public class Repair implements IVoicedCommandHandler, ICustomByPassHandler
{
	static final Logger LOGGER = Logger.getLogger(Repair.class.getName());
	
	private static final String[] _voicedCommands =
	{
		"repair",
	};
	
	@Override
	public boolean useVoicedCommand(String command, PlayerInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.startsWith("repair"))
		{
			final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair.htm");
			final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
			activeChar.sendPacket(npcHtmlMessage);
			return true;
		}
		return false;
	}
	
	private String getCharList(PlayerInstance activeChar)
	{
		String result = "";
		final String repCharAcc = activeChar.getAccountName();
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?");
			statement.setString(1, repCharAcc);
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (activeChar.getName().compareTo(rset.getString(1)) != 0)
				{
					result += rset.getString(1) + ";";
				}
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean checkAcc(PlayerInstance activeChar, String repairChar)
	{
		boolean result = false;
		String repCharAcc = "";
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				repCharAcc = rset.getString(1);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (activeChar.getAccountName().compareTo(repCharAcc) == 0)
		{
			result = true;
		}
		return result;
	}
	
	private boolean checkPunish(PlayerInstance activeChar, String repairChar)
	{
		boolean result = false;
		int accessLevel = 0;
		int repCharJail = 0;
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT accesslevel,punish_level FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				accessLevel = rset.getInt(1);
				repCharJail = rset.getInt(2);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if ((repCharJail == 1) || (accessLevel < 0))
		{
			result = true;
		}
		return result;
	}
	
	private boolean checkKarma(PlayerInstance activeChar, String repairChar)
	{
		boolean result = false;
		int repCharKarma = 0;
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT karma FROM characters WHERE char_name=?");
			statement.setString(1, repairChar);
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				repCharKarma = rset.getInt(1);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (repCharKarma > 0)
		{
			result = true;
		}
		return result;
	}
	
	private boolean checkChar(PlayerInstance activeChar, String repairChar)
	{
		boolean result = false;
		if (activeChar.getName().compareTo(repairChar) == 0)
		{
			result = true;
		}
		return result;
	}
	
	private void repairBadCharacter(String charName)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, charName);
			final ResultSet rset = statement.executeQuery();
			int objId = 0;
			if (rset.next())
			{
				objId = rset.getInt(1);
			}
			rset.close();
			statement.close();
			if (objId == 0)
			{
				con.close();
				return;
			}
			statement = con.prepareStatement("UPDATE characters SET x=17867, y=170259, z=-3503 WHERE obj_Id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=? AND loc=\"PAPERDOLL\"");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("GameServer: could not repair character:" + e);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
	private static final String[] _BYPASSCMD =
	{
		"repair",
		"repair_close_win"
	};
	
	private enum CommandEnum
	{
		repair,
		repair_close_win
	}
	
	@Override
	public String[] getByPassCommands()
	{
		return _BYPASSCMD;
	}
	
	@Override
	public void handleCommand(String command, PlayerInstance activeChar, String repairChar)
	{
		final CommandEnum comm = CommandEnum.valueOf(command);
		if (comm == null)
		{
			return;
		}
		
		switch (comm)
		{
			case repair:
			{
				if ((repairChar == null) || repairChar.equals(""))
				{
					return;
				}
				if (checkAcc(activeChar, repairChar))
				{
					if (checkChar(activeChar, repairChar))
					{
						final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-self.htm");
						final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
					else if (checkPunish(activeChar, repairChar))
					{
						final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-jail.htm");
						final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
					else if (checkKarma(activeChar, repairChar))
					{
						activeChar.sendMessage("Selected Char has Karma,Cannot be repaired!");
						return;
					}
					else
					{
						repairBadCharacter(repairChar);
						final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-done.htm");
						final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
						npcHtmlMessage.setHtml(htmContent);
						activeChar.sendPacket(npcHtmlMessage);
						return;
					}
				}
				final String htmContent = HtmCache.getInstance().getHtm("data/html/mods/repair/repair-error.htm");
				final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
				activeChar.sendPacket(npcHtmlMessage);
				return;
			}
			case repair_close_win:
			{
				// Do nothing.
				return;
			}
		}
	}
}
