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

import org.l2jserver.gameserver.handler.IVoicedCommandHandler;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.network.serverpackets.Ride;

public class CastleCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"open doors",
		"close doors",
		"ride wyvern"
	};
	
	@Override
	public boolean useVoicedCommand(String command, PlayerInstance activeChar, String target)
	{
		if (command.startsWith("open doors") && target.equals("castle") && activeChar.isClanLeader())
		{
			final DoorInstance door = (DoorInstance) activeChar.getTarget();
			final Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
			if ((door == null) || (castle == null))
			{
				return false;
			}
			
			if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
			{
				door.openMe();
			}
		}
		else if (command.startsWith("close doors") && target.equals("castle") && activeChar.isClanLeader())
		{
			final DoorInstance door = (DoorInstance) activeChar.getTarget();
			final Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
			if ((door == null) || (castle == null))
			{
				return false;
			}
			
			if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
			{
				door.closeMe();
			}
		}
		else if (command.startsWith("ride wyvern") && target.equals("castle") && (activeChar.getClan().getHasCastle() > 0) && activeChar.isClanLeader())
		{
			if (!activeChar.disarmWeapons())
			{
				return false;
			}
			
			final Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, 12621);
			activeChar.sendPacket(mount);
			activeChar.broadcastPacket(mount);
			activeChar.setMountType(mount.getMountType());
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
