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
import java.sql.ResultSet;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * @author Blaze
 */
public class AdminWalker implements IAdminCommandHandler
{
	private static int _npcid = 0;
	private static int _point = 1;
	private static String _text = "";
	private static int _mode = 1;
	private static int _routeid = 0;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_walker_setmessage",
		"admin_walker_menu",
		"admin_walker_setnpc",
		"admin_walker_setpoint",
		"admin_walker_setmode",
		"admin_walker_addpoint"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		try
		{
			final String[] parts = command.split(" ");
			if (command.startsWith("admin_walker_menu"))
			{
				mainMenu(activeChar);
			}
			else if (command.startsWith("admin_walker_setnpc "))
			{
				try
				{
					_npcid = Integer.parseInt(parts[1]);
					
					try (Connection con = DatabaseFactory.getConnection())
					{
						PreparedStatement statement = con.prepareStatement("SELECT `route_id` FROM `walker_routes` WHERE `npc_id` = " + _npcid + ";");
						final ResultSet rset = statement.executeQuery();
						if (rset.next())
						{
							BuilderUtil.sendSysMessage(activeChar, "Such NPC already was, we add routes");
							_routeid = rset.getInt("route_id");
						}
						else
						{
							statement = con.prepareStatement("SELECT MAX(`route_id`) AS max FROM `walker_routes`;");
							final ResultSet rset1 = statement.executeQuery();
							if (rset1.next())
							{
								_routeid = rset1.getInt("max") + 1;
							}
							
							rset1.close();
						}
						
						statement.close();
					}
					catch (Exception e)
					{
					}
					
					_point = 1;
				}
				catch (NumberFormatException e)
				{
					BuilderUtil.sendSysMessage(activeChar, "The incorrect identifier");
				}
				
				mainMenu(activeChar);
			}
			else if (command.startsWith("admin_walker_addpoint"))
			{
				addMenu(activeChar);
			}
			else if (command.startsWith("admin_walker_setmode"))
			{
				if (_mode == 1)
				{
					_mode = 0;
				}
				else
				{
					_mode = 1;
				}
				
				addMenu(activeChar);
			}
			else if (command.startsWith("admin_walker_setmessage"))
			{
				_text = command.substring(24);
				addMenu(activeChar);
			}
			else if (command.startsWith("admin_walker_setpoint"))
			{
				setPoint(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				_point++;
				addMenu(activeChar);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void setPoint(int x, int y, int z)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (_text.isEmpty())
			{
				statement = con.prepareStatement("INSERT INTO `lineage`.`walker_routes` (`route_id` ,`npc_id` ,`move_point` ,`chatText` ,`move_x` ,`move_y` ,`move_z` ,`delay` ,`running` )VALUES (?, ?, ?, NULL, ?, ?, ?, ?, ?);");
				statement.setInt(1, _routeid);
				statement.setInt(2, _npcid);
				statement.setInt(3, _point);
				statement.setInt(4, x);
				statement.setInt(5, y);
				statement.setInt(6, z);
				statement.setInt(7, 0);
				statement.setInt(8, _mode);
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO `lineage`.`walker_routes` (`route_id` ,`npc_id` ,`move_point` ,`chatText` ,`move_x` ,`move_y` ,`move_z` ,`delay` ,`running` )VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
				statement.setInt(1, _routeid);
				statement.setInt(2, _npcid);
				statement.setInt(3, _point);
				statement.setString(4, _text);
				statement.setInt(5, x);
				statement.setInt(6, y);
				statement.setInt(7, z);
				statement.setInt(8, 0);
				statement.setInt(9, _mode);
			}
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	private void mainMenu(PlayerInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><font color=\"009900\">Editing Walkers</font><br>");
		sb.append("<br>");
		sb.append("Is chosen NPCID: " + _npcid + "<br>");
		sb.append("Number of the current point: " + _point + "<br>");
		sb.append("<edit var=\"id\" width=80 height=15><br>");
		sb.append("<button value=\"New NPC\" action=\"bypass -h admin_walker_setnpc $id\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("<button value=\"To establish a point\" action=\"bypass -h admin_walker_addpoint width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private void addMenu(PlayerInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><font color=\"009900\">Editing Walkers</font><br>");
		sb.append("<br>");
		sb.append("Is chosen NPCID: " + _npcid + "<br>");
		sb.append("Number of the current point: " + _point + "<br>");
		sb.append("Number of the current route: " + _routeid + "<br>");
		if (_mode == 1)
		{
			sb.append("Mode: Run<br>");
		}
		else
		{
			sb.append("Mode: Step<br>");
		}
		
		if (_text.isEmpty())
		{
			sb.append("The phrase is not established<br>");
		}
		else
		{
			sb.append("The phrase: " + _text + "<br>");
		}
		
		sb.append("<edit var=\"id\" width=80 height=15><br>");
		sb.append("<button value=\"To establish a phrase\" action=\"bypass -h admin_walker_setmessage $id width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("<button value=\"To add a point\" action=\"bypass -h admin_walker_setpoint width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("<button value=\"To change a mode\" action=\"bypass -h admin_walker_setmode width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("<button value=\"The main menu\" action=\"bypass -h admin_walker_menu width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
}
