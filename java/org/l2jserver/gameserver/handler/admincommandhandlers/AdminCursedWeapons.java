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

import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jserver.gameserver.model.CursedWeapon;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands:<br>
 * - cw_info = displays cursed weapon status.<br>
 * - cw_remove = removes a cursed weapon from the world, item id or name must be provided.<br>
 * - cw_add = adds a cursed weapon into the world, item id or name must be provided, the target will be the wielder.<br>
 * - cw_goto = teleports GM to the specified cursed weapon.<br>
 * - cw_reload = reloads instance manager.
 * @author ProGramMoS, Zoey76
 */
public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static final CursedWeaponsManager cursedWeaponsManager = CursedWeaponsManager.getInstance();
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cw_info",
		"admin_cw_remove",
		"admin_cw_goto",
		"admin_cw_reload",
		"admin_cw_add",
		"admin_cw_info_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.equalsIgnoreCase("admin_cw_info"))
		{
			BuilderUtil.sendSysMessage(activeChar, "====== Cursed Weapons: ======");
			for (CursedWeapon cw : cursedWeaponsManager.getCursedWeapons())
			{
				BuilderUtil.sendSysMessage(activeChar, "> " + cw.getName() + " (" + cw.getItemId() + ")");
				if (cw.isActivated())
				{
					final PlayerInstance pl = cw.getPlayer();
					BuilderUtil.sendSysMessage(activeChar, "  Player holding: " + (pl == null ? "null" : pl.getName()));
					BuilderUtil.sendSysMessage(activeChar, "    Player karma: " + cw.getPlayerKarma());
					BuilderUtil.sendSysMessage(activeChar, "    Time Remaining: " + (cw.getTimeLeft() / 60000) + " min.");
					BuilderUtil.sendSysMessage(activeChar, "    Kills : " + cw.getNbKills());
				}
				else if (cw.isDropped())
				{
					BuilderUtil.sendSysMessage(activeChar, "  Lying on the ground.");
					BuilderUtil.sendSysMessage(activeChar, "    Time Remaining: " + (cw.getTimeLeft() / 60000) + " min.");
					BuilderUtil.sendSysMessage(activeChar, "    Kills : " + cw.getNbKills());
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "  Don't exist in the world.");
				}
				
				activeChar.sendPacket(SystemMessageId.EMPTY_3);
			}
		}
		else if (command.equalsIgnoreCase("admin_cw_info_menu"))
		{
			final StringBuilder replyMSG = new StringBuilder();
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile("data/html/admin/cwinfo.htm");
			
			for (CursedWeapon cw : cursedWeaponsManager.getCursedWeapons())
			{
				final int itemId = cw.getItemId();
				replyMSG.append("<table width=270><tr><td>Name:</td><td>" + cw.getName() + "</td></tr>");
				if (cw.isActivated())
				{
					final PlayerInstance pl = cw.getPlayer();
					replyMSG.append("<tr><td>Weilder:</td><td>" + (pl == null ? "null" : pl.getName()) + "</td></tr>");
					replyMSG.append("<tr><td>Karma:</td><td>" + cw.getPlayerKarma() + "</td></tr>");
					replyMSG.append("<tr><td>Kills:</td><td>" + cw.getPlayerPkKills() + "/" + cw.getNbKills() + "</td></tr>");
					replyMSG.append("<tr><td>Time remaining:</td><td>" + (cw.getTimeLeft() / 60000) + " min.</td></tr>");
					replyMSG.append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove " + itemId + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
					replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto " + itemId + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				}
				else if (cw.isDropped())
				{
					replyMSG.append("<tr><td>Position:</td><td>Lying on the ground</td></tr>");
					replyMSG.append("<tr><td>Time remaining:</td><td>" + (cw.getTimeLeft() / 60000) + " min.</td></tr>");
					replyMSG.append("<tr><td>Kills:</td><td>" + cw.getNbKills() + "</td></tr>");
					replyMSG.append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove " + itemId + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
					replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto " + itemId + "\" width=73 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				}
				else
				{
					replyMSG.append("<tr><td>Position:</td><td>Doesn't exist.</td></tr>");
					replyMSG.append("<tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add " + itemId + "\" width=99 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>");
				}
				
				replyMSG.append("</table>");
				replyMSG.append("<br>");
			}
			
			adminReply.replace("%cwinfo%", replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		else if (command.equalsIgnoreCase("admin_cw_reload"))
		{
			cursedWeaponsManager.reload();
		}
		else if (command.startsWith("admin_cw_remove"))
		{
			if (!st.hasMoreElements())
			{
				BuilderUtil.sendSysMessage(activeChar, "Not enough parameters!");
				return false;
			}
			
			String parameter = st.nextToken();
			int id = 0;
			if (parameter.matches("[0-9]*"))
			{
				id = Integer.parseInt(parameter);
			}
			else
			{
				parameter = parameter.replace('_', ' ');
				for (CursedWeapon cwp : cursedWeaponsManager.getCursedWeapons())
				{
					if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
					{
						id = cwp.getItemId();
						break;
					}
				}
			}
			
			if (cursedWeaponsManager.isCursed(id))
			{
				cursedWeaponsManager.getCursedWeapon(id).endOfLife();
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong Cursed Weapon Id!");
			}
		}
		else if (command.startsWith("admin_cw_goto"))
		{
			if (!st.hasMoreElements())
			{
				BuilderUtil.sendSysMessage(activeChar, "Not enough parameters!");
				return false;
			}
			
			String parameter = st.nextToken();
			int id = 0;
			if (parameter.matches("[0-9]*"))
			{
				id = Integer.parseInt(parameter);
			}
			else
			{
				parameter = parameter.replace('_', ' ');
				for (CursedWeapon cwp : cursedWeaponsManager.getCursedWeapons())
				{
					if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
					{
						id = cwp.getItemId();
						break;
					}
				}
			}
			
			if (cursedWeaponsManager.isCursed(id))
			{
				cursedWeaponsManager.getCursedWeapon(id).goTo(activeChar);
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong Cursed Weapon Id!");
			}
		}
		else if (command.startsWith("admin_cw_add"))
		{
			if (!st.hasMoreElements())
			{
				BuilderUtil.sendSysMessage(activeChar, "Not enough parameters!");
				return false;
			}
			
			String parameter = st.nextToken();
			int id = 0;
			if (parameter.matches("[0-9]*"))
			{
				id = Integer.parseInt(parameter);
			}
			else
			{
				parameter = parameter.replace('_', ' ');
				for (CursedWeapon cwp : cursedWeaponsManager.getCursedWeapons())
				{
					if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
					{
						id = cwp.getItemId();
						break;
					}
				}
			}
			
			if (cursedWeaponsManager.isCursed(id))
			{
				final CursedWeapon cursedWeapon = cursedWeaponsManager.getCursedWeapon(id);
				if (cursedWeapon.isActive())
				{
					BuilderUtil.sendSysMessage(activeChar, "This Cursed Weapon is already active!");
				}
				else
				{
					// end time is equal to dropped one
					final long endTime = System.currentTimeMillis() + (cursedWeapon.getDuration() * 60000);
					cursedWeapon.setEndTime(endTime);
					
					final WorldObject target = activeChar.getTarget();
					if ((target != null) && (target instanceof PlayerInstance))
					{
						((PlayerInstance) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
					}
					else
					{
						activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
					}
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong Cursed Weapon Id!");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
