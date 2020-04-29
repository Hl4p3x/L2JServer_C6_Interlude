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
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.xml.AdminData;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus - gmliston/gmlistoff = includes/excludes active character from /gmlist results - silence = toggles private messages acceptance mode - diet = toggles weight penalty mode -
 * tradeoff = toggles trade acceptance mode - reload = reloads specified component from multisell|skill|npc|htm|item|instancemanager - set/set_menu/set_mod = alters specified server setting - saveolymp = saves olympiad state manually - manualhero = cycles olympiad and calculate new heroes.
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2007/07/28 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminAdmin.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		"admin_diet",
		"admin_set",
		"admin_set_menu",
		"admin_set_mod",
		"admin_saveolymp",
		"admin_manualhero"
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
			case "admin_admin":
			case "admin_admin1":
			case "admin_admin2":
			case "admin_admin3":
			case "admin_admin4":
			{
				showMainPage(activeChar, command);
				return true;
			}
			case "admin_gmliston":
			{
				AdminData.getInstance().showGm(activeChar);
				BuilderUtil.sendSysMessage(activeChar, "Registerd into GM list.");
				return true;
			}
			case "admin_gmlistoff":
			{
				AdminData.getInstance().hideGm(activeChar);
				BuilderUtil.sendSysMessage(activeChar, "Removed from GM list.");
				return true;
			}
			case "admin_silence":
			{
				if (activeChar.isInRefusalMode()) // already in message refusal mode
				{
					activeChar.setInRefusalMode(false);
					activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
				}
				else
				{
					activeChar.setInRefusalMode(true);
					activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
				}
				return true;
			}
			case "admin_saveolymp":
			{
				Olympiad.getInstance().saveOlympiadStatus();
				BuilderUtil.sendSysMessage(activeChar, "Olympiad stuff saved!");
				return true;
			}
			case "admin_manualhero":
			{
				try
				{
					Olympiad.getInstance().manualSelectHeroes();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				BuilderUtil.sendSysMessage(activeChar, "Heroes formed!");
				return true;
			}
			case "admin_diet":
			{
				boolean noToken = false;
				if (st.hasMoreTokens())
				{
					if (st.nextToken().equalsIgnoreCase("on"))
					{
						activeChar.setDietMode(true);
						BuilderUtil.sendSysMessage(activeChar, "Diet mode on");
					}
					else if (st.nextToken().equalsIgnoreCase("off"))
					{
						activeChar.setDietMode(false);
						BuilderUtil.sendSysMessage(activeChar, "Diet mode off");
					}
				}
				else
				{
					noToken = true;
				}
				if (noToken)
				{
					if (activeChar.getDietMode())
					{
						activeChar.setDietMode(false);
						BuilderUtil.sendSysMessage(activeChar, "Diet mode off");
					}
					else
					{
						activeChar.setDietMode(true);
						BuilderUtil.sendSysMessage(activeChar, "Diet mode on");
					}
				}
				activeChar.refreshOverloaded();
				return true;
			}
			case "admin_set":
			{
				boolean noToken = false;
				final String[] cmd = st.nextToken().split("_");
				if ((cmd != null) && (cmd.length > 1))
				{
					if (st.hasMoreTokens())
					{
						final String[] parameter = st.nextToken().split("=");
						if (parameter.length > 1)
						{
							final String pName = parameter[0].trim();
							final String pValue = parameter[1].trim();
							if (Float.valueOf(pValue) == null)
							{
								BuilderUtil.sendSysMessage(activeChar, "Invalid parameter!");
								return false;
							}
							switch (pName)
							{
								case "RateXp":
								{
									Config.RATE_XP = Float.parseFloat(pValue);
									break;
								}
								case "RateSp":
								{
									Config.RATE_SP = Float.parseFloat(pValue);
									break;
								}
								case "RateDropSpoil":
								{
									Config.RATE_DROP_SPOIL = Float.parseFloat(pValue);
									break;
								}
							}
							BuilderUtil.sendSysMessage(activeChar, "Config parameter " + pName + " set to " + pValue);
						}
						else
						{
							noToken = true;
						}
					}
					
					if (cmd.length == 3)
					{
						if (cmd[2].equalsIgnoreCase("menu"))
						{
							AdminHelpPage.showHelpPage(activeChar, "settings.htm");
						}
						else if (cmd[2].equalsIgnoreCase("mod"))
						{
							AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
						}
					}
				}
				else
				{
					noToken = true;
				}
				
				if (noToken)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //set parameter=vaue");
					return false;
				}
				
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(PlayerInstance activeChar, String command)
	{
		int mode = 0;
		String filename = null;
		if ((command != null) && (command.length() > 11))
		{
			final String mode_s = command.substring(11);
			
			try
			{
				mode = Integer.parseInt(mode_s);
			}
			catch (NumberFormatException e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
		
		switch (mode)
		{
			case 1:
			{
				filename = "main";
				break;
			}
			case 2:
			{
				filename = "game";
				break;
			}
			case 3:
			{
				filename = "effects";
				break;
			}
			case 4:
			{
				filename = "server";
				break;
			}
			default:
			{
				filename = "main";
				break;
			}
		}
		
		AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm");
	}
}
