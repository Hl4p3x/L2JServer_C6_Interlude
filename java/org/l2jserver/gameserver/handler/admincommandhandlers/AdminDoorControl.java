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

import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands:<br>
 * - open1 = open coloseum door 24190001<br>
 * - open2 = open coloseum door 24190002<br>
 * - open3 = open coloseum door 24190003<br>
 * - open4 = open coloseum door 24190004<br>
 * - openall = open all coloseum door<br>
 * - close1 = close coloseum door 24190001<br>
 * - close2 = close coloseum door 24190002<br>
 * - close3 = close coloseum door 24190003<br>
 * - close4 = close coloseum door 24190004<br>
 * - closeall = close all coloseum door<br>
 * <br>
 * - open = open selected door<br>
 * - close = close selected door<br>
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final DoorData doorTable = DoorData.getInstance();
		WorldObject target2 = null;
		if (command.startsWith("admin_close "))
		{
			try
			{
				final int doorId = Integer.parseInt(command.substring(12));
				if (doorTable.getDoor(doorId) != null)
				{
					doorTable.getDoor(doorId).closeMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).closeMe();
						}
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong ID door.");
				return false;
			}
		}
		else if (command.equals("admin_close"))
		{
			target2 = activeChar.getTarget();
			if (target2 instanceof DoorInstance)
			{
				((DoorInstance) target2).closeMe();
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Incorrect target.");
			}
		}
		else if (command.startsWith("admin_open "))
		{
			try
			{
				final int doorId = Integer.parseInt(command.substring(11));
				if (doorTable.getDoor(doorId) != null)
				{
					doorTable.getDoor(doorId).openMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).openMe();
						}
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong ID door.");
				return false;
			}
		}
		else if (command.equals("admin_open"))
		{
			target2 = activeChar.getTarget();
			if (target2 instanceof DoorInstance)
			{
				((DoorInstance) target2).openMe();
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Incorrect target.");
			}
		}
		// need optimize cycle
		// set limits on the ID doors that do not cycle to close doors
		else if (command.equals("admin_closeall"))
		{
			try
			{
				for (DoorInstance door : doorTable.getDoors())
				{
					door.closeMe();
				}
				
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
			}
			catch (Exception e)
			{
				return false;
			}
		}
		else if (command.equals("admin_openall"))
		{
			// need optimize cycle
			// set limits on the PH door to do a cycle of opening doors.
			try
			{
				for (DoorInstance door : doorTable.getDoors())
				{
					door.openMe();
				}
				
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
			}
			catch (Exception e)
			{
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
}
