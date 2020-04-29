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

import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * The classical custom L2J implementation of the old //gmspeed GM command.
 * @author lord_rex (No, it wasn't me at all. Eclipse added my name there.)
 */
public class AdminSuperHaste implements IAdminCommandHandler
{
	public static final String[] ADMIN_COMMANDS =
	{
		"admin_superhaste",
		"admin_superhaste_menu",
		"admin_speed",
		"admin_speed_menu",
	};
	
	private static final int SUPER_HASTE_ID = 7029;
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance player)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final String cmd = st.nextToken();
		switch (cmd)
		{
			case "admin_superhaste":
			case "admin_speed":
			{
				try
				{
					final int val = Integer.parseInt(st.nextToken());
					final boolean sendMessage = player.getFirstEffect(SUPER_HASTE_ID) != null;
					player.stopSkillEffects(SUPER_HASTE_ID);
					
					if ((val == 0) && sendMessage)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(SUPER_HASTE_ID));
					}
					else if ((val >= 1) && (val <= 4))
					{
						final Skill gmSpeedSkill = SkillTable.getInstance().getInfo(SUPER_HASTE_ID, val);
						player.doCast(gmSpeedSkill);
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Use //gmspeed value (0=off...4=max).");
				}
				finally
				{
					player.updateEffectIcons();
				}
				break;
			}
			case "admin_superhaste_menu":
			case "admin_speed_menu":
			{
				AdminHelpPage.showHelpPage(player, "gm_menu.htm");
				break;
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
