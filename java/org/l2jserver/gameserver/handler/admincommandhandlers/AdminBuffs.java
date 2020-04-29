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
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * @author ProGramMoS
 */

public class AdminBuffs implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_getbuffs",
		"admin_stopbuff",
		"admin_stopallbuffs",
		"admin_areacancel"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case "admin_getbuffs":
			{
				if (st.hasMoreTokens())
				{
					PlayerInstance player = null;
					final String playername = st.nextToken();
					player = World.getInstance().getPlayer(playername);
					if (player != null)
					{
						showBuffs(player, activeChar);
						return true;
					}
					BuilderUtil.sendSysMessage(activeChar, "The player " + playername + " is not online");
					return false;
				}
				else if ((activeChar.getTarget() != null) && (activeChar.getTarget() instanceof PlayerInstance))
				{
					showBuffs((PlayerInstance) activeChar.getTarget(), activeChar);
					return true;
				}
				else
				{
					return true;
				}
			}
			case "admin_stopbuff":
			{
				if (st.hasMoreTokens())
				{
					final String playername = st.nextToken();
					if (st.hasMoreTokens())
					{
						int skillId = 0;
						try
						{
							skillId = Integer.parseInt(st.nextToken());
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //stopbuff <playername> [skillId] (skillId must be a number)");
							return false;
						}
						if (skillId > 0)
						{
							removeBuff(activeChar, playername, skillId);
						}
						else
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //stopbuff <playername> [skillId] (skillId must be a number > 0)");
							return false;
						}
						return true;
					}
					BuilderUtil.sendSysMessage(activeChar, "Usage: //stopbuff <playername> [skillId]");
					return false;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //stopbuff <playername> [skillId]");
				return false;
			}
			case "admin_stopallbuffs":
			{
				if (st.hasMoreTokens())
				{
					final String playername = st.nextToken();
					if (playername != null)
					{
						removeAllBuffs(activeChar, playername);
						return true;
					}
					BuilderUtil.sendSysMessage(activeChar, "Usage: //stopallbuffs <playername>");
					return false;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //stopallbuffs <playername>");
				return false;
			}
			case "admin_areacancel":
			{
				if (st.hasMoreTokens())
				{
					final String val = st.nextToken();
					int radius = 0;
					try
					{
						radius = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //areacancel <radius> (integer value > 0)");
						return false;
					}
					if (radius > 0)
					{
						for (Creature knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if ((knownChar instanceof PlayerInstance) && !knownChar.equals(activeChar))
							{
								knownChar.stopAllEffects();
							}
						}
						BuilderUtil.sendSysMessage(activeChar, "All effects canceled within raidus " + radius);
						return true;
					}
					BuilderUtil.sendSysMessage(activeChar, "Usage: //areacancel <radius> (integer value > 0)");
					return false;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //areacancel <radius>");
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	public void showBuffs(PlayerInstance player, PlayerInstance activeChar)
	{
		final StringBuilder html = new StringBuilder();
		html.append("<html><center><font color=\"LEVEL\">Effects of " + player.getName() + "</font><center><br>");
		html.append("<table>");
		html.append("<tr><td width=200>Skill</td><td width=70>Action</td></tr>");
		
		final Effect[] effects = player.getAllEffects();
		for (Effect e : effects)
		{
			if (e != null)
			{
				html.append("<tr><td>" + e.getSkill().getName() + "</td><td><button value=\"Remove\" action=\"bypass -h admin_stopbuff " + player.getName() + " " + e.getSkill().getId() + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			}
		}
		
		html.append("</table><br>");
		html.append("<button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs " + player.getName() + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		html.append("</html>");
		
		final NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setHtml(html.toString());
		
		activeChar.sendPacket(ms);
	}
	
	private void removeBuff(PlayerInstance remover, String playername, int SkillId)
	{
		final PlayerInstance player = World.getInstance().getPlayer(playername);
		if ((player != null) && (SkillId > 0))
		{
			final Effect[] effects = player.getAllEffects();
			for (Effect e : effects)
			{
				if ((e != null) && (e.getSkill().getId() == SkillId))
				{
					e.exit(true);
					remover.sendMessage("Removed " + e.getSkill().getName() + " level " + e.getSkill().getLevel() + " from " + playername);
				}
			}
			showBuffs(player, remover);
		}
	}
	
	private void removeAllBuffs(PlayerInstance remover, String playername)
	{
		final PlayerInstance player = World.getInstance().getPlayer(playername);
		if (player != null)
		{
			player.stopAllEffects();
			remover.sendMessage("Removed all effects from " + playername);
			showBuffs(player, remover);
		}
		else
		{
			remover.sendMessage("Can not remove effects from " + playername + ". Player appears offline.");
		}
	}
}
