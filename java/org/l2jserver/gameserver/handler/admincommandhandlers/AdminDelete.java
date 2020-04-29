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

import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.equals("admin_delete"))
		{
			final WorldObject obj = activeChar.getTarget();
			if (obj instanceof NpcInstance)
			{
				final NpcInstance target = (NpcInstance) obj;
				target.deleteMe();
				
				final Spawn spawn = target.getSpawn();
				if (spawn != null)
				{
					if (GrandBossManager.getInstance().isDefined(spawn.getNpcId()))
					{
						BuilderUtil.sendSysMessage(activeChar, "You cannot delete a grandboss.");
						return true;
					}
					
					spawn.stopRespawn();
					
					if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
					{
						RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
					}
					else
					{
						SpawnTable.getInstance().deleteSpawn(spawn, true);
					}
				}
				
				BuilderUtil.sendSysMessage(activeChar, "Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
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
