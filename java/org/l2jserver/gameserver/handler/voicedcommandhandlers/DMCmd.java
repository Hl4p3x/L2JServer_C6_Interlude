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

import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.handler.IVoicedCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.event.DM;

public class DMCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"dmjoin",
		"dmleave",
		"dminfo"
	};
	
	@Override
	public boolean useVoicedCommand(String command, PlayerInstance activeChar, String target)
	{
		if (command.startsWith("dmjoin"))
		{
			JoinDM(activeChar);
		}
		else if (command.startsWith("dmleave"))
		{
			LeaveDM(activeChar);
		}
		
		else if (command.startsWith("dminfo"))
		{
			DMinfo(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public boolean JoinDM(PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!DM.isJoining())
		{
			activeChar.sendMessage("There is no DeathMatch Event in progress.");
			return false;
		}
		else if (DM.isJoining() && activeChar._inEventDM)
		{
			activeChar.sendMessage("You are already registered.");
			return false;
		}
		else if (activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you are holding a Cursed Weapon.");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you are in Olympiad.");
			return false;
		}
		else if (activeChar.getLevel() < DM.getMinlvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too low.");
			return false;
		}
		else if (activeChar.getLevel() > DM.getMaxlvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too high.");
			return false;
		}
		else if (activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you have Karma.");
			return false;
		}
		else if (DM.isTeleport() || DM.hasStarted())
		{
			activeChar.sendMessage("DeathMatch Event registration period is over. You can't register now.");
			return false;
		}
		else
		{
			activeChar.sendMessage("Your participation in the DeathMatch event has been approved.");
			DM.addPlayer(activeChar);
			return true;
		}
	}
	
	public boolean LeaveDM(PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!DM.isJoining())
		{
			activeChar.sendMessage("There is no DeathMatch Event in progress.");
			return false;
		}
		else if ((DM.isTeleport() || DM.hasStarted()) && activeChar._inEventDM)
		{
			activeChar.sendMessage("You can not leave now because DeathMatch event has started.");
			return false;
		}
		else if (DM.isJoining() && !activeChar._inEventDM)
		{
			activeChar.sendMessage("You aren't registered in the DeathMatch Event.");
			return false;
		}
		else
		{
			DM.removePlayer(activeChar);
			return true;
		}
	}
	
	public boolean DMinfo(PlayerInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!DM.isJoining())
		{
			activeChar.sendMessage("There is no DeathMatch Event in progress.");
			return false;
		}
		else if (DM.isTeleport() || DM.hasStarted())
		{
			activeChar.sendMessage("I can't provide you this info. Command available only in joining period.");
			return false;
		}
		else
		{
			if (DM._players.size() == 1)
			{
				activeChar.sendMessage("There is " + DM._players.size() + " player participating in this event.");
				activeChar.sendMessage("Reward: " + DM.getRewardAmount() + " " + ItemTable.getInstance().getTemplate(DM.getRewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + DM.getMinlvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + DM.getMaxlvl() + ".");
			}
			else
			{
				activeChar.sendMessage("There are " + DM._players.size() + " players participating in this event.");
				activeChar.sendMessage("Reward: " + DM.getRewardAmount() + " " + ItemTable.getInstance().getTemplate(DM.getRewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + DM.getMinlvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + DM.getMaxlvl() + ".");
			}
			return true;
		}
	}
}