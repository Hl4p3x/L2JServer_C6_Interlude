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

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.BuilderUtil;
import org.l2jserver.gameserver.util.Util;

/**
 * A retail-like implementation of //gmspeed builder command.
 * @author lord_rex
 */
public class AdminGmSpeed implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gmspeed",
	};
	
	private static final int SUPER_HASTE_ID = 7029;
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance player)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final String cmd = st.nextToken();
		if (cmd.equals("admin_gmspeed"))
		{
			if (!st.hasMoreTokens())
			{
				BuilderUtil.sendSysMessage(player, "//gmspeed [0...10]");
				return false;
			}
			final String token = st.nextToken();
			
			// Rollback feature for old custom way, in order to make everyone happy.
			if (Config.USE_SUPER_HASTE_AS_GM_SPEED)
			{
				try
				{
					final int val = Integer.parseInt(token);
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
				return true;
			}
			
			if (!Util.isFloat(token))
			{
				BuilderUtil.sendSysMessage(player, "//gmspeed [0...10]");
				return false;
			}
			float runSpeedBoost = Float.parseFloat(token);
			if ((runSpeedBoost < 0) || (runSpeedBoost > 10))
			{
				// Custom limit according to SDW's request - real retail limit is unknown.
				BuilderUtil.sendSysMessage(player, "//gmspeed [0...10]");
				return false;
			}
			
			final PlayerInstance targetCharacter;
			final WorldObject target = player.getTarget();
			if ((target != null) && target.isPlayer())
			{
				targetCharacter = (PlayerInstance) target;
			}
			else
			{
				// If there is no target, let's use the command executer.
				targetCharacter = player;
			}
			
			targetCharacter.getStat().setGmSpeedMultiplier(runSpeedBoost > 1 ? runSpeedBoost : 1);
			targetCharacter.broadcastUserInfo();
			
			if (runSpeedBoost < 1)
			{
				runSpeedBoost = 1;
			}
			BuilderUtil.sendSysMessage(player, "[" + targetCharacter.getName() + "] speed is [" + (runSpeedBoost * 100) + "0]% fast.");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
