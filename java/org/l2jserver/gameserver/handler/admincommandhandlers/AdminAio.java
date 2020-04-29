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
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.xml.AdminData;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * Give / Take Status Aio to Player Changes name color and title color if enabled Uses: setaio [<player_name>] [<time_duration in days>] removeaio [<player_name>] If <player_name> is not specified, the current target player is used.
 * @author KhayrusS
 */
public class AdminAio implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminAio.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_setaio",
		"admin_removeaio"
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
		
		switch (comm)
		{
			case "admin_setaio":
			{
				boolean noToken = false;
				if (st.hasMoreTokens())
				{ // char_name not specified
					final String char_name = st.nextToken();
					final PlayerInstance player = World.getInstance().getPlayer(char_name);
					if (player != null)
					{
						if (st.hasMoreTokens()) // time
						{
							final String time = st.nextToken();
							try
							{
								final int value = Integer.parseInt(time);
								if (value > 0)
								{
									doAio(activeChar, player, char_name, time);
									if (player.isAio())
									{
										return true;
									}
								}
								else
								{
									BuilderUtil.sendSysMessage(activeChar, "Time must be bigger then 0!");
									return false;
								}
							}
							catch (NumberFormatException e)
							{
								BuilderUtil.sendSysMessage(activeChar, "Time must be a number!");
								return false;
							}
						}
						else
						{
							noToken = true;
						}
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Player must be online to set AIO status");
						noToken = true;
					}
				}
				else
				{
					noToken = true;
				}
				if (noToken)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setaio <char_name> [time](in days)");
					return false;
				}
				break;
			}
			case "admin_removeaio":
			{
				boolean noToken = false;
				if (st.hasMoreTokens())
				{ // char_name
					final String char_name = st.nextToken();
					final PlayerInstance player = World.getInstance().getPlayer(char_name);
					if (player != null)
					{
						removeAio(activeChar, player, char_name);
						if (!player.isAio())
						{
							return true;
						}
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Player must be online to remove AIO status");
						noToken = true;
					}
				}
				else
				{
					noToken = true;
				}
				if (noToken)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //removeaio <char_name>");
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void doAio(PlayerInstance activeChar, PlayerInstance player, String playerName, String time)
	{
		final int days = Integer.parseInt(time);
		if (player == null)
		{
			BuilderUtil.sendSysMessage(activeChar, "not found char" + playerName);
			return;
		}
		
		if (days > 0)
		{
			player.setAio(true);
			player.setEndTime("aio", days);
			player.getStat().addExp(player.getStat().getExpForLevel(81));
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("UPDATE characters SET aio=1, aio_end=? WHERE obj_id=?");
				statement.setLong(1, player.getAioEndTime());
				statement.setInt(2, player.getObjectId());
				statement.execute();
				statement.close();
				
				if (Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
				{
					player.getAppearance().setNameColor(Config.AIO_NCOLOR);
				}
				
				if (Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
				{
					player.getAppearance().setTitleColor(Config.AIO_TCOLOR);
				}
				
				player.rewardAioSkills();
				player.broadcastUserInfo();
				player.sendPacket(new EtcStatusUpdate(player));
				player.sendSkillList();
				AdminData.broadcastMessageToGMs("GM " + activeChar.getName() + " set Aio stat for player " + playerName + " for " + time + " day(s)");
				player.sendMessage("You are now an Aio, Congratulations!");
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				LOGGER.warning("Could not set Aio stats to char: " + e);
			}
		}
		else
		{
			removeAio(activeChar, player, playerName);
		}
	}
	
	public void removeAio(PlayerInstance activeChar, PlayerInstance player, String playerName)
	{
		player.setAio(false);
		player.setAioEndTime(0);
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET Aio=0, Aio_end=0 WHERE obj_id=?");
			statement.setInt(1, player.getObjectId());
			statement.execute();
			statement.close();
			
			player.lostAioSkills();
			player.getAppearance().setNameColor(0xFFFFFF);
			player.getAppearance().setTitleColor(0xFFFFFF);
			player.broadcastUserInfo();
			player.sendPacket(new EtcStatusUpdate(player));
			player.sendSkillList();
			AdminData.broadcastMessageToGMs("GM " + activeChar.getName() + " remove Aio stat of player " + playerName);
			player.sendMessage("Now You are not an Aio..");
			player.broadcastUserInfo();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not remove Aio stats of char: " + e);
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}