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

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.datatables.xml.ZoneData;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.type.TownZone;

public class AdminTownWar implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_townwar_start",
		"admin_townwar_end"
	};
	private WorldObject _activeObject;
	
	public WorldObject getActiveObject()
	{
		return _activeObject;
	}
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.startsWith("admin_townwar_start")) // townwar_start
		{
			startTW(activeChar);
		}
		if (command.startsWith("admin_townwar_end")) // townwar_end
		{
			endTW(activeChar);
		}
		return true;
	}
	
	private void startTW(PlayerInstance activeChar)
	{
		// All Towns will become War Zones
		if (Config.TW_ALL_TOWNS)
		{
			for (TownZone zone : ZoneData.getInstance().getAllZones(TownZone.class))
			{
				zone.setParameter("noPeace", "true");
			}
		}
		
		// A Town will become War Zone
		if (!Config.TW_ALL_TOWNS && (Config.TW_TOWN_ID != 18) && (Config.TW_TOWN_ID != 21) && (Config.TW_TOWN_ID != 22))
		{
			MapRegionData.getInstance().getTown(Config.TW_TOWN_ID).setParameter("noPeace", "true");
		}
		
		int x;
		int y;
		int z;
		TownZone town;
		for (PlayerInstance onlinePlayer : World.getInstance().getAllPlayers())
		{
			if (onlinePlayer.isOnline())
			{
				x = onlinePlayer.getX();
				y = onlinePlayer.getY();
				z = onlinePlayer.getZ();
				town = ZoneData.getInstance().getZone(x, y, z, TownZone.class);
				if (town != null)
				{
					if ((town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS)
					{
						onlinePlayer.setInsideZone(ZoneId.PVP, false);
						onlinePlayer.revalidateZone(true);
					}
					else if (Config.TW_ALL_TOWNS)
					{
						onlinePlayer.setInsideZone(ZoneId.PVP, false);
						onlinePlayer.revalidateZone(true);
					}
				}
				onlinePlayer.setInTownWar(true);
			}
		}
		
		// Announce for all towns
		if (Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().criticalAnnounceToAll("Town War Event!");
			Announcements.getInstance().criticalAnnounceToAll("All towns have been set to war zone by " + activeChar.getName() + ".");
		}
		
		// Announce for one town
		if (!Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().criticalAnnounceToAll("Town War Event!");
			Announcements.getInstance().criticalAnnounceToAll(MapRegionData.getInstance().getTown(Config.TW_TOWN_ID).getName() + " has been set to war zone by " + activeChar.getName() + ".");
		}
	}
	
	private void endTW(PlayerInstance activeChar)
	{
		// All Towns will become Peace Zones
		if (Config.TW_ALL_TOWNS)
		{
			MapRegionData.getInstance().getTown(1).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(2).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(3).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(4).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(5).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(6).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(7).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(8).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(9).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(10).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(11).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(12).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(13).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(14).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(15).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(16).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(17).setParameter("noPeace", "false");
			MapRegionData.getInstance().getTown(19).setParameter("noPeace", "false");
		}
		
		// A Town will become Peace Zone
		if (!Config.TW_ALL_TOWNS && (Config.TW_TOWN_ID != 18) && (Config.TW_TOWN_ID != 21) && (Config.TW_TOWN_ID != 22))
		{
			MapRegionData.getInstance().getTown(Config.TW_TOWN_ID).setParameter("noPeace", "false");
		}
		
		int x;
		int y;
		int z;
		TownZone town;
		for (PlayerInstance onlinePlayer : World.getInstance().getAllPlayers())
		{
			if (onlinePlayer.isOnline())
			{
				x = onlinePlayer.getX();
				y = onlinePlayer.getY();
				z = onlinePlayer.getZ();
				town = ZoneData.getInstance().getZone(x, y, z, TownZone.class);
				if (town != null)
				{
					if ((town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS)
					{
						onlinePlayer.setInsideZone(ZoneId.PVP, true);
						onlinePlayer.revalidateZone(true);
					}
					else if (Config.TW_ALL_TOWNS)
					{
						onlinePlayer.setInsideZone(ZoneId.PVP, true);
						onlinePlayer.revalidateZone(true);
					}
				}
				onlinePlayer.setInTownWar(false);
			}
		}
		
		// Announce for all towns
		if (Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().criticalAnnounceToAll("All towns have been set back to normal by " + activeChar.getName() + ".");
		}
		
		// Announce for one town
		if (!Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().criticalAnnounceToAll(MapRegionData.getInstance().getTown(Config.TW_TOWN_ID).getName() + " has been set back to normal by " + activeChar.getName() + ".");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}