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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_stats",
		"admin_mcrit",
		"admin_addbufftest",
		"admin_skill_test",
		"admin_st",
		"admin_mp",
		"admin_oly_obs_mode",
		"admin_obs_mode"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.equals("admin_stats"))
		{
			for (String line : ThreadPool.getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		else if (command.equals("admin_mcrit"))
		{
			final Creature target = (Creature) activeChar.getTarget();
			BuilderUtil.sendSysMessage(activeChar, "Activechar Mcrit " + activeChar.getMCriticalHit(null, null));
			BuilderUtil.sendSysMessage(activeChar, "Activechar baseMCritRate " + activeChar.getTemplate().getBaseMCritRate());
			if (target != null)
			{
				BuilderUtil.sendSysMessage(activeChar, "Target Mcrit " + target.getMCriticalHit(null, null));
				BuilderUtil.sendSysMessage(activeChar, "Target baseMCritRate " + target.getTemplate().getBaseMCritRate());
			}
		}
		else if (command.equals("admin_addbufftest"))
		{
			final Creature target = (Creature) activeChar.getTarget();
			BuilderUtil.sendSysMessage(activeChar, "cast");
			
			final Skill skill = SkillTable.getInstance().getInfo(1085, 3);
			if (target != null)
			{
				BuilderUtil.sendSysMessage(activeChar, "target locked");
				for (int i = 0; i < 100;)
				{
					if (activeChar.isCastingNow())
					{
						continue;
					}
					
					BuilderUtil.sendSysMessage(activeChar, "Casting " + i);
					activeChar.useMagic(skill, false, false);
					i++;
				}
			}
		}
		else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				
				final int id = Integer.parseInt(st.nextToken());
				adminTestSkill(activeChar, id);
			}
			catch (NumberFormatException | NoSuchElementException e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Command format is //skill_test <ID>");
			}
		}
		else if (command.equals("admin_mp on"))
		{
			// .startPacketMonitor();
			BuilderUtil.sendSysMessage(activeChar, "command not working");
		}
		else if (command.equals("admin_mp off"))
		{
			// .stopPacketMonitor();
			BuilderUtil.sendSysMessage(activeChar, "command not working");
		}
		else if (command.equals("admin_mp dump"))
		{
			// .dumpPacketHistory();
			BuilderUtil.sendSysMessage(activeChar, "command not working");
		}
		else if (command.startsWith("admin_oly_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterOlympiadObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ(), -1);
			}
			else
			{
				activeChar.leaveOlympiadObserverMode();
			}
		}
		else if (command.startsWith("admin_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			else
			{
				activeChar.leaveObserverMode();
			}
		}
		return true;
	}
	
	private void adminTestSkill(PlayerInstance activeChar, int id)
	{
		Creature creature;
		final WorldObject target = activeChar.getTarget();
		if (!(target instanceof Creature))
		{
			creature = activeChar;
		}
		else
		{
			creature = (Creature) target;
		}
		
		creature.broadcastPacket(new MagicSkillUse(activeChar, creature, id, 1, 1, 1));
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}