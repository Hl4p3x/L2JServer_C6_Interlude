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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.event.GameEvent;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.CharInfo;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.Revive;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * This class handles following admin commands: - admin = shows menu
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminEventEngine implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_event",
		"admin_event_new",
		"admin_event_choose",
		"admin_event_store",
		"admin_event_set",
		"admin_event_change_teams_number",
		"admin_event_announce",
		"admin_event_panel",
		"admin_event_control_begin",
		"admin_event_control_teleport",
		"admin_add",
		"admin_event_see",
		"admin_event_del",
		"admin_delete_buffer",
		"admin_event_control_sit",
		"admin_event_name",
		"admin_event_control_kill",
		"admin_event_control_res",
		"admin_event_control_poly",
		"admin_event_control_unpoly",
		"admin_event_control_prize",
		"admin_event_control_chatban",
		"admin_event_control_finish"
	};
	
	private static String tempBuffer = "";
	private static String tempName = "";
	private static String tempName2 = "";
	private static boolean npcsDeleted = false;
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.equals("admin_event"))
		{
			showMainPage(activeChar);
		}
		else if (command.equals("admin_event_new"))
		{
			showNewEventPage(activeChar);
		}
		else if (command.startsWith("admin_add"))
		{
			tempBuffer += command.substring(10);
			showNewEventPage(activeChar);
		}
		else if (command.startsWith("admin_event_see"))
		{
			final String eventName = command.substring(16);
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
				replyMSG.append("</body></html>");
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			catch (Exception e)
			{
			}
			finally
			{
				if (inbr != null)
				{
					try
					{
						inbr.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				if (isr != null)
				{
					try
					{
						isr.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				if (in != null)
				{
					try
					{
						in.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				if (buff != null)
				{
					try
					{
						buff.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else if (command.startsWith("admin_event_del"))
		{
			final String eventName = command.substring(16);
			final File file = new File("data/events/" + eventName);
			file.delete();
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_event_name"))
		{
			tempName += command.substring(17);
			showNewEventPage(activeChar);
		}
		else if (command.equalsIgnoreCase("admin_delete_buffer"))
		{
			try
			{
				tempBuffer += tempBuffer.substring(0, tempBuffer.length() - 10);
				showNewEventPage(activeChar);
			}
			catch (Exception e)
			{
				tempBuffer = "";
			}
		}
		else if (command.startsWith("admin_event_store"))
		{
			FileOutputStream file = null;
			PrintStream p = null;
			try
			{
				file = new FileOutputStream("data/events/" + tempName);
				p = new PrintStream(file);
				p.println(activeChar.getName());
				p.println(tempBuffer);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (p != null)
				{
					p.close();
				}
				
				if (file != null)
				{
					try
					{
						file.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			tempBuffer = "";
			tempName = "";
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_event_set"))
		{
			GameEvent.eventName = command.substring(16);
			showEventParameters(activeChar, 2);
		}
		else if (command.startsWith("admin_event_change_teams_number"))
		{
			showEventParameters(activeChar, Integer.parseInt(command.substring(32)));
		}
		else if (command.startsWith("admin_event_panel"))
		{
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_begin"))
		{
			try
			{
				GameEvent.active = true;
				GameEvent.players.clear();
				GameEvent.connectionLossData.clear();
				
				for (int j = 0; j < GameEvent.teamsNumber; j++)
				{
					final LinkedList<String> link = new LinkedList<>();
					GameEvent.players.put(j + 1, link);
				}
				
				int i = 0;
				
				while (!GameEvent.participatingPlayers.isEmpty())
				{
					final String target = getMaxLeveledPlayer();
					if (!target.equals(""))
					{
						GameEvent.players.get(i + 1).add(target);
						i = (i + 1) % GameEvent.teamsNumber;
					}
				}
				
				destroyEventNpcs();
				npcsDeleted = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_teleport"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(29), "-");
			
			while (st.hasMoreElements())
			{
				teleportTeam(activeChar, Integer.parseInt(st.nextToken()));
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_sit"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(24), "-");
			
			while (st.hasMoreElements())
			{
				sitTeam(Integer.parseInt(st.nextToken()));
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_kill"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(25), "-");
			
			while (st.hasMoreElements())
			{
				killTeam(activeChar, Integer.parseInt(st.nextToken()));
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_res"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(24), "-");
			
			while (st.hasMoreElements())
			{
				resTeam(Integer.parseInt(st.nextToken()));
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_poly"))
		{
			final StringTokenizer st0 = new StringTokenizer(command.substring(25));
			final StringTokenizer st = new StringTokenizer(st0.nextToken(), "-");
			final String id = st0.nextToken();
			
			while (st.hasMoreElements())
			{
				polyTeam(Integer.parseInt(st.nextToken()), id);
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_unpoly"))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(27), "-");
			
			while (st.hasMoreElements())
			{
				unpolyTeam(Integer.parseInt(st.nextToken()));
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_prize"))
		{
			final StringTokenizer st0 = new StringTokenizer(command.substring(26));
			final StringTokenizer st = new StringTokenizer(st0.nextToken(), "-");
			String n = st0.nextToken();
			
			final StringTokenizer st1 = new StringTokenizer(n, "*");
			n = st1.nextToken();
			String type = "";
			if (st1.hasMoreElements())
			{
				type = st1.nextToken();
			}
			
			final String id = st0.nextToken();
			
			while (st.hasMoreElements())
			{
				regardTeam(activeChar, Integer.parseInt(st.nextToken()), Integer.parseInt(n), Integer.parseInt(id), type);
			}
			
			showEventControl(activeChar);
		}
		else if (command.startsWith("admin_event_control_finish"))
		{
			for (int i = 0; i < GameEvent.teamsNumber; i++)
			{
				telePlayersBack(i + 1);
			}
			
			GameEvent.eventName = "";
			GameEvent.teamsNumber = 0;
			GameEvent.names.clear();
			GameEvent.participatingPlayers.clear();
			GameEvent.players.clear();
			GameEvent.id = 12760;
			GameEvent.npcs.clear();
			GameEvent.active = false;
			npcsDeleted = false;
		}
		else if (command.startsWith("admin_event_announce"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(21));
			GameEvent.id = Integer.parseInt(st.nextToken());
			GameEvent.teamsNumber = Integer.parseInt(st.nextToken());
			String temp = " ";
			String temp2 = "";
			
			while (st.hasMoreElements())
			{
				temp += st.nextToken() + " ";
			}
			
			st = new StringTokenizer(temp, "-");
			Integer i = 1;
			
			while (st.hasMoreElements())
			{
				temp2 = st.nextToken();
				if (!temp2.equals(" "))
				{
					GameEvent.names.put(i, temp2.substring(1, temp2.length() - 1));
					i++;
				}
			}
			
			GameEvent.participatingPlayers.clear();
			
			muestraNpcConInfoAPlayers(activeChar, GameEvent.id);
			
			final PlaySound snd = new PlaySound(1, "B03_F");
			activeChar.sendPacket(snd);
			activeChar.broadcastPacket(snd);
			
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE</font></center><br>");
			replyMSG.append("<center>The event <font color=\"LEVEL\">" + GameEvent.eventName + "</font> has been announced, now you can type //event_panel to see the event panel control</center><br>");
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	String showStoredEvents()
	{
		final File dir = new File("data/events");
		final String[] files = dir.list();
		String result = "";
		if (files == null)
		{
			result = "No 'data/events' directory!";
			return result;
		}
		
		for (String file2 : files)
		{
			final File file = new File("data/events/" + file2);
			result += "<font color=\"LEVEL\">" + file.getName() + " </font><br><button value=\"select\" action=\"bypass -h admin_event_set " + file.getName() + "\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"ver\" action=\"bypass -h admin_event_see " + file.getName() + "\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"delete\" action=\"bypass -h admin_event_del " + file.getName() + "\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><br>";
		}
		
		return result;
	}
	
	public void showMainPage(PlayerInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br>");
		replyMSG.append("<br><center><button value=\"Create NEW event \" action=\"bypass -h admin_event_new\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<center><br>Stored Events<br></center>");
		replyMSG.append(showStoredEvents());
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void showNewEventPage(PlayerInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br>");
		replyMSG.append("<br><center>Event's Title<br><font color=\"LEVEL\">");
		if (tempName.equals(""))
		{
			replyMSG.append("Use //event_name text to insert a new title");
		}
		else
		{
			replyMSG.append(tempName);
		}
		
		replyMSG.append("</font></center><br><br>Event's description<br>");
		if (tempBuffer.equals(""))
		{
			replyMSG.append("Use //add text o //delete_buffer to modify this text field");
		}
		else
		{
			replyMSG.append(tempBuffer);
		}
		
		if ((!tempName.equals("") || !tempBuffer.equals("")))
		{
			replyMSG.append("<br><button value=\"Crear\" action=\"bypass -h admin_event_store\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		}
		
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public void showEventParameters(PlayerInstance activeChar, int teamnumbers)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br>");
		replyMSG.append("<center><font color=\"LEVEL\">" + GameEvent.eventName + "</font></center><br>");
		replyMSG.append("<br><center><button value=\"Change number of teams to\" action=\"bypass -h admin_event_change_teams_number $event_teams_number\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"> <edit var=\"event_teams_number\" width=100 height=20><br><br>");
		replyMSG.append("<font color=\"LEVEL\">Team's Names</font><br>");
		for (int i = 0; i < teamnumbers; i++)
		{
			replyMSG.append((i + 1) + ".- <edit var=\"event_teams_name" + (i + 1) + "\" width=100 height=20><br>");
		}
		
		replyMSG.append("<br><br>Announcer NPC id<edit var=\"event_npcid\" width=100 height=20><br><br><button value=\"Announce Event!!\" action=\"bypass -h admin_event_announce $event_npcid " + teamnumbers + " ");
		for (int i = 0; i < teamnumbers; i++)
		{
			replyMSG.append("$event_teams_name" + (i + 1) + " - ");
		}
		
		replyMSG.append("\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	void muestraNpcConInfoAPlayers(PlayerInstance activeChar, int id)
	{
		GameEvent.npcs.clear();
		final LinkedList<PlayerInstance> temp = new LinkedList<>();
		temp.clear();
		
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			if (!temp.contains(player))
			{
				GameEvent.spawn(player, id);
				temp.add(player);
			}
			for (PlayerInstance playertemp : player.getKnownList().getKnownPlayers().values())
			{
				if ((Math.abs(playertemp.getX() - player.getX()) < 500) && (Math.abs(playertemp.getY() - player.getY()) < 500) && (Math.abs(playertemp.getZ() - player.getZ()) < 500))
				{
					temp.add(playertemp);
				}
			}
		}
		
		GameEvent.announceAllPlayers(activeChar.getName() + " wants to make an event !!! (you'll find a npc with the details around)");
	}
	
	void showEventControl(PlayerInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br><font color=\"LEVEL\">" + GameEvent.eventName + "</font><br><br><table width=200>");
		replyMSG.append("<tr><td>Apply this command to teams number </td><td><edit var=\"team_number\" width=100 height=15></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		
		if (!npcsDeleted)
		{
			replyMSG.append("<tr><td><button value=\"Start\" action=\"bypass -h admin_event_control_begin\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><font color=\"LEVEL\">Destroys all event npcs so no more people can't participate now on</font></td></tr>");
		}
		
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"Teleport\" action=\"bypass -h admin_event_control_teleport $team_number\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><font color=\"LEVEL\">Teleports the specified team to your position</font></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"Sit\" action=\"bypass -h admin_event_control_sit $team_number\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><font color=\"LEVEL\">Sits/Stands up the team</font></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"Kill\" action=\"bypass -h admin_event_control_kill $team_number\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><font color=\"LEVEL\">Finish with the life of all the players in the selected team</font></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"Resurrect\" action=\"bypass -h admin_event_control_res $team_number\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><font color=\"LEVEL\">Resurrect Team's members</font></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"Polymorph\" action=\"bypass -h admin_event_control_poly $team_number $poly_id\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><edit var=\"poly_id\" width=100 height=15><font color=\"LEVEL\">Polymorphs the team into the NPC with the id specified</font></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"UnPolymorph\" action=\"bypass -h admin_event_control_unpoly $team_number\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><font color=\"LEVEL\">Unpolymorph the team</font></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"Give Item\" action=\"bypass -h admin_event_control_prize $team_number $n $id\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"> number <edit var=\"n\" width=100 height=15> item id <edit var=\"id\" width=100 height=15></td><td><font color=\"LEVEL\">Give the specified item id to every single member of the team, you can put 5*level, 5*kills or 5 in the number field for example</font></td></tr>");
		replyMSG.append("<tr><td>&nbsp;</td></tr>");
		replyMSG.append("<tr><td><button value=\"End\" action=\"bypass -h admin_event_control_finish\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td><font color=\"LEVEL\">Will finish the event teleporting back all the players</font></td></tr>");
		replyMSG.append("</table></body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	String getMaxLeveledPlayer()
	{
		final Iterator<String> it = GameEvent.participatingPlayers.iterator();
		PlayerInstance pc = null;
		int max = 0;
		String name = "";
		
		while (it.hasNext())
		{
			try
			{
				tempName2 = it.next();
				pc = World.getInstance().getPlayer(tempName2);
				if (max < pc.getLevel())
				{
					max = pc.getLevel();
					name = pc.getName();
				}
			}
			catch (Exception e)
			{
				try
				{
					GameEvent.participatingPlayers.remove(tempName2);
				}
				catch (Exception e2)
				{
				}
			}
		}
		
		GameEvent.participatingPlayers.remove(name);
		
		return name;
	}
	
	void destroyEventNpcs()
	{
		NpcInstance npc;
		while (!GameEvent.npcs.isEmpty())
		{
			try
			{
				npc = (NpcInstance) World.getInstance().findObject(Integer.parseInt(GameEvent.npcs.get(0)));
				final Spawn spawn = npc.getSpawn();
				if (spawn != null)
				{
					spawn.stopRespawn();
					SpawnTable.getInstance().deleteSpawn(spawn, true);
				}
				
				npc.deleteMe();
				GameEvent.npcs.remove(0);
			}
			catch (Exception e)
			{
				GameEvent.npcs.remove(0);
			}
		}
	}
	
	void teleportTeam(PlayerInstance activeChar, int team)
	{
		final LinkedList<String> linked = GameEvent.players.get(team);
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance pc = World.getInstance().getPlayer(it.next());
				pc.setTitle(GameEvent.names.get(team));
				pc.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	void sitTeam(int team)
	{
		final LinkedList<String> linked = GameEvent.players.get(team);
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance pc = World.getInstance().getPlayer(it.next());
				pc.eventSitForced = !pc.eventSitForced;
				if (pc.eventSitForced)
				{
					pc.sitDown();
				}
				else
				{
					pc.standUp();
				}
			}
			catch (Exception e)
			{
			}
		}
	}
	
	void killTeam(PlayerInstance activeChar, int team)
	{
		final LinkedList<String> linked = GameEvent.players.get(team);
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance target = World.getInstance().getPlayer(it.next());
				target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	void resTeam(int team)
	{
		final LinkedList<String> linked = GameEvent.players.get(team);
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance character = World.getInstance().getPlayer(it.next());
				character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
				character.setCurrentCp(character.getMaxCp());
				
				final Revive revive = new Revive(character);
				final SocialAction sa = new SocialAction(character.getObjectId(), 15);
				character.broadcastPacket(sa);
				character.sendPacket(sa);
				character.sendPacket(revive);
				character.broadcastPacket(revive);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	void polyTeam(int team, String id)
	{
		final LinkedList<String> linked = GameEvent.players.get(team);
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance target = World.getInstance().getPlayer(it.next());
				target.getPoly().setPolyInfo("npc", id);
				target.teleToLocation(target.getX(), target.getY(), target.getZ(), true);
				target.broadcastPacket(new CharInfo(target));
				target.sendPacket(new UserInfo(target));
			}
			catch (Exception e)
			{
			}
		}
	}
	
	void unpolyTeam(int team)
	{
		final LinkedList<String> linked = GameEvent.players.get(team);
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance target = World.getInstance().getPlayer(it.next());
				target.getPoly().setPolyInfo(null, "1");
				target.decayMe();
				target.spawnMe(target.getX(), target.getY(), target.getZ());
				target.broadcastPacket(new CharInfo(target));
				target.sendPacket(new UserInfo(target));
			}
			catch (Exception e)
			{
			}
		}
	}
	
	private void createItem(PlayerInstance activeChar, PlayerInstance player, int id, int num)
	{
		player.getInventory().addItem("Event", id, num, player, activeChar);
		player.sendPacket(new ItemList(player, true));
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("CONGRATULATIONS, you should have a present in your inventory");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		player.sendPacket(adminReply);
	}
	
	void regardTeam(PlayerInstance activeChar, int team, int n, int id, String type)
	{
		final LinkedList<String> linked = GameEvent.players.get(team);
		int temp = n;
		
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance target = World.getInstance().getPlayer(it.next());
				if (type.equalsIgnoreCase("level"))
				{
					temp = n * target.getLevel();
				}
				else if (type.equalsIgnoreCase("kills"))
				{
					temp = n * target.kills.size();
				}
				else
				{
					temp = n;
				}
				createItem(activeChar, target, id, temp);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	void telePlayersBack(int team)
	{
		resTeam(team);
		unpolyTeam(team);
		
		final LinkedList<String> linked = GameEvent.players.get(team);
		final Iterator<String> it = linked.iterator();
		
		while (it.hasNext())
		{
			try
			{
				final PlayerInstance target = World.getInstance().getPlayer(it.next());
				target.setTitle(target.eventTitle);
				target.setKarma(target.eventKarma);
				target.setPvpKills(target.eventPvpKills);
				target.setPkKills(target.eventPkKills);
				target.teleToLocation(target.eventX, target.eventY, target.eventZ, true);
				target.kills.clear();
				target.eventSitForced = false;
				target.atEvent = false;
			}
			catch (Exception e)
			{
			}
		}
	}
}
