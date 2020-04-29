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
package org.l2jserver.gameserver.model.actor.instance;

import org.l2jserver.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.ExQuestInfo;
import org.l2jserver.gameserver.network.serverpackets.RadarControl;

/**
 * @version $Revision: $ $Date: $
 * @author LBaldi
 */
public class AdventurerInstance extends FolkInstance
{
	public AdventurerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.startsWith("npcfind_byid"))
		{
			try
			{
				final int bossId = Integer.parseInt(command.substring(12).trim());
				switch (RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId))
				{
					case ALIVE:
					case DEAD:
					{
						final Spawn spawn = RaidBossSpawnManager.getInstance().getSpawns().get(bossId);
						player.sendPacket(new RadarControl(0, 1, spawn.getX(), spawn.getY(), spawn.getZ()));
						break;
					}
					case UNDEFINED:
					{
						player.sendMessage("This Boss isn't in game.");
						break;
					}
				}
			}
			catch (NumberFormatException e)
			{
				LOGGER.warning("Invalid Bypass to Server command parameter.");
			}
		}
		else if (command.startsWith("raidInfo"))
		{
			final int bossLevel = Integer.parseInt(command.substring(9).trim());
			String filename = "data/html/adventurer_guildsman/raid_info/info.htm";
			if (bossLevel != 0)
			{
				filename = "data/html/adventurer_guildsman/raid_info/level" + bossLevel + ".htm";
			}
			
			showChatWindow(player, bossLevel, filename);
		}
		else if (command.equalsIgnoreCase("questlist"))
		{
			player.sendPacket(new ExQuestInfo());
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int value)
	{
		String pom = "";
		if (value == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + value;
		}
		return "data/html/adventurer_guildsman/" + pom + ".htm";
	}
	
	private void showChatWindow(PlayerInstance player, int bossLevel, String filename)
	{
		showChatWindow(player, filename);
	}
}
