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
package org.l2jserver.gameserver.model.entity.event;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.EventData;

public class GameEvent
{
	protected static final Logger LOGGER = Logger.getLogger(GameEvent.class.getName());
	
	public static final Map<Integer, LinkedList<String>> players = new ConcurrentHashMap<>();
	public static final Map<String, EventData> connectionLossData = new ConcurrentHashMap<>();
	public static final Map<Integer, String> names = new ConcurrentHashMap<>();
	public static final List<String> participatingPlayers = new LinkedList<>();
	public static final List<String> npcs = new LinkedList<>();
	public static String eventName = "";
	public static int teamsNumber = 0;
	public static boolean active = false;
	public static int id = 12760;
	
	public static int getTeamOfPlayer(String name)
	{
		for (int i = 1; i <= players.size(); i++)
		{
			final LinkedList<String> temp = players.get(i);
			final Iterator<String> it = temp.iterator();
			
			while (it.hasNext())
			{
				if (it.next().equals(name))
				{
					return i;
				}
			}
		}
		return 0;
	}
	
	public static String[] getTopNKillers(int n)
	{
		// this will return top N players sorted by kills, first element in the array will be the one with more kills
		final String[] killers = new String[n];
		String playerTemp = "";
		int kills = 0;
		
		final LinkedList<String> killersTemp = new LinkedList<>();
		for (int k = 0; k < n; k++)
		{
			kills = 0;
			for (int i = 1; i <= teamsNumber; i++)
			{
				final LinkedList<String> temp = players.get(i);
				final Iterator<String> it = temp.iterator();
				
				while (it.hasNext())
				{
					try
					{
						final PlayerInstance player = World.getInstance().getPlayer(it.next());
						if (!killersTemp.contains(player.getName()) && (player.kills.size() > kills))
						{
							kills = player.kills.size();
							playerTemp = player.getName();
						}
					}
					catch (Exception e)
					{
					}
				}
			}
			
			killersTemp.add(playerTemp);
		}
		
		for (int i = 0; i < n; i++)
		{
			kills = 0;
			final Iterator<String> it = killersTemp.iterator();
			
			while (it.hasNext())
			{
				try
				{
					final PlayerInstance player = World.getInstance().getPlayer(it.next());
					if (player.kills.size() > kills)
					{
						kills = player.kills.size();
						playerTemp = player.getName();
					}
				}
				catch (Exception e)
				{
				}
			}
			
			killers[i] = playerTemp;
			killersTemp.remove(playerTemp);
		}
		
		return killers;
	}
	
	public static void showEventHtml(PlayerInstance player, String objectid)
	{
		FileInputStream fis = null;
		BufferedInputStream buff = null;
		DataInputStream in = null;
		InputStreamReader isr = null;
		BufferedReader inbr = null;
		
		try
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			fis = new FileInputStream("data/events/" + eventName);
			buff = new BufferedInputStream(fis);
			in = new DataInputStream(buff);
			isr = new InputStreamReader(in);
			inbr = new BufferedReader(isr);
			
			final StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("<center><font color=\"LEVEL\">" + eventName + "</font><font color=\"FF0000\"> bY " + inbr.readLine() + "</font></center><br>");
			replyMSG.append("<br>" + inbr.readLine());
			if (participatingPlayers.contains(player.getName()))
			{
				replyMSG.append("<br><center>You are already in the event players list !!</center></body></html>");
			}
			else
			{
				replyMSG.append("<br><center><button value=\"Participate !! \" action=\"bypass -h npc_" + objectid + "_event_participate\" width=90 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
			}
			
			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		finally
		{
			if (inbr != null)
			{
				try
				{
					inbr.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning(e1.toString());
				}
			}
			
			if (isr != null)
			{
				try
				{
					isr.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning(e1.toString());
				}
			}
			
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning(e1.toString());
				}
			}
			
			if (buff != null)
			{
				try
				{
					buff.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning(e1.toString());
				}
			}
			
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning(e1.toString());
				}
			}
		}
	}
	
	public static void spawn(PlayerInstance target, int npcid)
	{
		final NpcTemplate template1 = NpcTable.getInstance().getTemplate(npcid);
		
		try
		{
			final Spawn spawn = new Spawn(template1);
			spawn.setX(target.getX() + 50);
			spawn.setY(target.getY() + 50);
			spawn.setZ(target.getZ());
			spawn.setAmount(1);
			spawn.setHeading(target.getHeading());
			spawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			spawn.init();
			spawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			spawn.getLastSpawn().setName("event inscriptor");
			spawn.getLastSpawn().setTitle(eventName);
			spawn.getLastSpawn().isEventMob = true;
			spawn.getLastSpawn().isAggressive();
			spawn.getLastSpawn().decayMe();
			spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(), spawn.getLastSpawn().getY(), spawn.getLastSpawn().getZ());
			spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
			npcs.add(String.valueOf(spawn.getLastSpawn().getObjectId()));
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
	}
	
	public static void announceAllPlayers(String text)
	{
		final CreatureSay cs = new CreatureSay(0, ChatType.ANNOUNCEMENT, "", text);
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
	}
	
	public static boolean isOnEvent(PlayerInstance player)
	{
		for (int k = 0; k < teamsNumber; k++)
		{
			final Iterator<String> it = players.get(k + 1).iterator();
			boolean temp = false;
			
			while (it.hasNext())
			{
				temp = player.getName().equalsIgnoreCase(it.next());
				if (temp)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static void inscribePlayer(PlayerInstance player)
	{
		try
		{
			participatingPlayers.add(player.getName());
			player.eventKarma = player.getKarma();
			player.eventX = player.getX();
			player.eventY = player.getY();
			player.eventZ = player.getZ();
			player.eventPkKills = player.getPkKills();
			player.eventPvpKills = player.getPvpKills();
			player.eventTitle = player.getTitle();
			player.kills.clear();
			player.atEvent = true;
		}
		catch (Exception e)
		{
			LOGGER.warning("error when signing in the event:" + e.getMessage());
		}
	}
	
	public static void restoreChar(PlayerInstance player)
	{
		try
		{
			player.eventX = connectionLossData.get(player.getName()).eventX;
			player.eventY = connectionLossData.get(player.getName()).eventY;
			player.eventZ = connectionLossData.get(player.getName()).eventZ;
			player.eventKarma = connectionLossData.get(player.getName()).eventKarma;
			player.eventPvpKills = connectionLossData.get(player.getName()).eventPvpKills;
			player.eventPkKills = connectionLossData.get(player.getName()).eventPkKills;
			player.eventTitle = connectionLossData.get(player.getName()).eventTitle;
			player.kills = connectionLossData.get(player.getName()).kills;
			player.eventSitForced = connectionLossData.get(player.getName()).eventSitForced;
			player.atEvent = true;
		}
		catch (Exception e)
		{
		}
	}
	
	public static void restoreAndTeleChar(PlayerInstance target)
	{
		try
		{
			restoreChar(target);
			target.setTitle(target.eventTitle);
			target.setKarma(target.eventKarma);
			target.setPvpKills(target.eventPvpKills);
			target.setPkKills(target.eventPkKills);
			target.teleToLocation(target.eventX, target.eventY, target.eventZ);
			target.kills.clear();
			target.eventSitForced = false;
			target.atEvent = false;
		}
		catch (Exception e)
		{
		}
	}
}
