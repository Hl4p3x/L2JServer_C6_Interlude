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

import org.l2jserver.Config;
import org.l2jserver.gameserver.handler.IVoicedCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

public class StatsCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"stat",
		"stats"
	};
	
	private enum CommandEnum
	{
		stat,
		stats
	}
	
	@Override
	public boolean useVoicedCommand(String command, PlayerInstance activeChar, String target)
	{
		final CommandEnum comm = CommandEnum.valueOf(command);
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case stat:
			{
				if (!Config.ALLOW_DETAILED_STATS_VIEW)
				{
					return false;
				}
				if (activeChar.getTarget() == null)
				{
					activeChar.sendMessage("You have no one targeted.");
					return false;
				}
				if (activeChar.getTarget() == activeChar)
				{
					activeChar.sendMessage("You cannot request your stats.");
					return false;
				}
				if (!(activeChar.getTarget() instanceof PlayerInstance))
				{
					activeChar.sendMessage("You can only get the info of a player.");
					return false;
				}
				final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				final PlayerInstance targetp = (PlayerInstance) activeChar.getTarget();
				final StringBuilder replyMSG = new StringBuilder("<html><body><center>");
				replyMSG.append("<br><br><font color=\"00FF00\">=========>>" + targetp.getName() + "<<=========</font><br>");
				replyMSG.append("<font color=\"FF0000\">Level: " + targetp.getLevel() + "</font><br>");
				if (targetp.getClan() != null)
				{
					replyMSG.append("<font color=\"FF0000\">Clan: " + targetp.getClan().getName() + "</font><br>");
					replyMSG.append("<font color=\"FF0000\">Alliance: " + targetp.getClan().getAllyName() + "</font><br>");
				}
				else
				{
					replyMSG.append("<font color=\"FF0000\">Alliance: None</font><br>");
					replyMSG.append("<font color=\"FF0000\">Clan: None</font><br>");
				}
				replyMSG.append("<font color=\"FF0000\">Adena: " + targetp.getAdena() + "</font><br>");
				if (targetp.getInventory().getItemByItemId(6393) == null)
				{
					replyMSG.append("<font color=\"FF0000\">Medals : 0</font><br>");
				}
				else
				{
					replyMSG.append("<font color=\"FF0000\">Medals : " + targetp.getInventory().getItemByItemId(6393).getCount() + "</font><br>");
				}
				if (targetp.getInventory().getItemByItemId(3470) == null)
				{
					replyMSG.append("<font color=\"FF0000\">Gold Bars : 0</font><br>");
				}
				else
				{
					replyMSG.append("<font color=\"FF0000\">Gold Bars : " + targetp.getInventory().getItemByItemId(3470).getCount() + "</font><br>");
				}
				replyMSG.append("<font color=\"FF0000\">PvP Kills: " + targetp.getPvpKills() + "</font><br>");
				replyMSG.append("<font color=\"FF0000\">PvP Flags: " + targetp.getPvpFlag() + "</font><br>");
				replyMSG.append("<font color=\"FF0000\">PK Kills: " + targetp.getPkKills() + "</font><br>");
				replyMSG.append("<font color=\"FF0000\">HP, CP, MP: " + targetp.getMaxHp() + ", " + targetp.getMaxCp() + ", " + targetp.getMaxMp() + "</font><br>");
				if (targetp.getActiveWeaponInstance() == null)
				{
					replyMSG.append("<font color=\"FF0000\">No Weapon!</font><br>");
				}
				else
				{
					replyMSG.append("<font color=\"FF0000\">Wep Enchant: " + targetp.getActiveWeaponInstance().getEnchantLevel() + "</font><br>");
				}
				replyMSG.append("<font color=\"00FF00\">=========>>" + targetp.getName() + "<<=========</font><br>");
				replyMSG.append("</center></body></html>");
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
				return true;
			}
			case stats:
			{
				if (!Config.ALLOW_SIMPLE_STATS_VIEW)
				{
					return false;
				}
				if (activeChar.getTarget() == null)
				{
					activeChar.sendMessage("You have no one targeted.");
					return false;
				}
				if (activeChar.getTarget() == activeChar)
				{
					activeChar.sendMessage("You cannot request your stats.");
					return false;
				}
				if (!(activeChar.getTarget() instanceof PlayerInstance))
				{
					activeChar.sendMessage("You can only get the info of a player.");
					return false;
				}
				final PlayerInstance targetp = (PlayerInstance) activeChar.getTarget();
				if (targetp != null)
				{
					final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
					final StringBuilder replyMSG = new StringBuilder("<html><body>");
					replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br>");
					replyMSG.append("<br>Statistics for player <font color=\"LEVEL\">" + targetp.getName() + "</font><br>");
					replyMSG.append("Total kills <font color=\"FF0000\">" + targetp.kills.size() + "</font><br>");
					replyMSG.append("<br>Detailed list:<br>");
					for (String kill : targetp.kills)
					{
						replyMSG.append("<font color=\"FF0000\">" + kill + "</font><br>");
					}
					replyMSG.append("</body></html>");
					adminReply.setHtml(replyMSG.toString());
					activeChar.sendPacket(adminReply);
					return true;
				}
				return false;
			}
			default:
			{
				return false;
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
