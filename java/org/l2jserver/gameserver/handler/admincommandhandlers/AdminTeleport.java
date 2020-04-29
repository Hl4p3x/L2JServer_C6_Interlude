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
import java.util.logging.Logger;

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - show_moves - show_teleport - teleport_to_character - move_to - teleport_character
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminTeleport implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminTeleport.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_moves",
		"admin_show_moves_other",
		"admin_show_teleport",
		"admin_teleport_to_character",
		"admin_teleportto",
		"admin_move_to",
		"admin_teleport_character",
		"admin_recall",
		"admin_walk",
		"admin_recall_npc",
		"admin_gonorth",
		"admin_gosouth",
		"admin_goeast",
		"admin_gowest",
		"admin_goup",
		"admin_godown",
		"admin_instant_move",
		"admin_sendhome",
		"admin_tele",
		"admin_teleto",
		"admin_recall_party",
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		// Alt+g window (instant move)
		if (command.equals("admin_instant_move"))
		{
			BuilderUtil.sendSysMessage(activeChar, "Instant move ready. Click where you want to go.");
			activeChar.setTeleMode(1);
		}
		// Send player to town (alt+g)
		else if (command.startsWith("admin_sendhome"))
		{
			try
			{
				final String[] param = command.split(" ");
				if (param.length != 2)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //sendhome <playername>");
					return false;
				}
				final String targetName = param[1];
				final PlayerInstance player = World.getInstance().getPlayer(targetName);
				if (player != null)
				{
					final Location loc = MapRegionData.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
					player.setInstanceId(0);
					player.teleToLocation(loc, true);
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "User is not online.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		
		switch (comm)
		{
			case "admin_show_moves":
			{
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
				return true;
			}
			case "admin_show_moves_other":
			{
				AdminHelpPage.showHelpPage(activeChar, "tele/other.html");
				return true;
			}
			case "admin_show_teleport":
			{
				showTeleportCharWindow(activeChar);
				return true;
			}
			case "admin_teleport_to_character":
			{
				teleportToCharacter(activeChar, activeChar.getTarget());
				return true;
			}
			case "admin_teleportto":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //teleportto <char_name>");
					return false;
				}
				final PlayerInstance player = World.getInstance().getPlayer(val);
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "ATTENTION: char_name must be valid character");
					BuilderUtil.sendSysMessage(activeChar, "Usage: //teleportto <char_name>");
					return false;
				}
				teleportToCharacter(activeChar, player);
				return true;
			}
			case "admin_recall_party":
			{
				if (activeChar.isGM() && (activeChar.getAccessLevel().getLevel() != 100))
				{
					return false;
				}
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //recall_party <party_leader_name>");
					return false;
				}
				if (val.equals(""))
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //recall_party <party_leader_name>");
					return false;
				}
				final PlayerInstance player = World.getInstance().getPlayer(val);
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "ATTENTION: party_leader_name must be valid character");
					BuilderUtil.sendSysMessage(activeChar, "//recall_party <party_leader_name>");
					return false;
				}
				if (player.getParty() == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "The player must have a party");
					BuilderUtil.sendSysMessage(activeChar, "//recall_party <party_leader_name>");
					return false;
				}
				if (!player.getParty().isLeader(player))
				{
					BuilderUtil.sendSysMessage(activeChar, "The player must be the party_leader");
					BuilderUtil.sendSysMessage(activeChar, "//recall_party <party_leader_name>");
					return false;
				}
				for (PlayerInstance partyMember : player.getParty().getPartyMembers())
				{
					partyMember.sendMessage("You party teleported by Admin.");
					teleportTo(partyMember, activeChar.getX(), activeChar.getY(), activeChar.getZ());
				}
				return true;
			}
			case "admin_move_to":
			{
				int x = 0;
				int y = 0;
				int z = 0;
				if (st.countTokens() == 3)
				{
					try
					{
						final String x_str = st.nextToken();
						final String y_str = st.nextToken();
						final String z_str = st.nextToken();
						x = Integer.parseInt(x_str);
						y = Integer.parseInt(y_str);
						z = Integer.parseInt(z_str);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //move_to <coordinates>");
						AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
						return false;
					}
				}
				else
				{
					AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
					return false;
				}
				if ((x == 0) && (y == 0))
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //move_to <valid_coordinates>");
					AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
					return false;
				}
				teleportTo(activeChar, x, y, z);
				return true;
			}
			case "admin_teleport_character":
			{
				int x = 0;
				int y = 0;
				int z = 0;
				if (st.countTokens() == 3)
				{
					try
					{
						final String x_str = st.nextToken();
						final String y_str = st.nextToken();
						final String z_str = st.nextToken();
						x = Integer.parseInt(x_str);
						y = Integer.parseInt(y_str);
						z = Integer.parseInt(z_str);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //teleport_character <coordinates>");
						showTeleportCharWindow(activeChar);
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //teleport_character <coordinates>");
					showTeleportCharWindow(activeChar);
					return false;
				}
				if ((x == 0) && (y == 0))
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //teleport_character <valid_coordinates>");
					showTeleportCharWindow(activeChar);
					return false;
				}
				WorldObject target = null;
				PlayerInstance player = null;
				target = activeChar.getTarget();
				if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
				}
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Select valid player");
					BuilderUtil.sendSysMessage(activeChar, "Usage: //teleport_character <valid_coordinates>");
					showTeleportCharWindow(activeChar);
					return false;
				}
				teleportTo(player, x, y, z);
				return true;
			}
			case "admin_recall":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //recall <char_name>");
					return false;
				}
				if (val.equals(""))
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //recall <char_name>");
					return false;
				}
				final PlayerInstance player = World.getInstance().getPlayer(val);
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "ATTENTION: char_name must be valid character");
					BuilderUtil.sendSysMessage(activeChar, "Usage: //recall <char_name>");
					return false;
				}
				teleportTo(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
				return true;
			}
			case "admin_walk":
			{
				int x = 0;
				int y = 0;
				int z = 0;
				if (st.countTokens() == 3)
				{
					try
					{
						final String x_str = st.nextToken();
						final String y_str = st.nextToken();
						final String z_str = st.nextToken();
						x = Integer.parseInt(x_str);
						y = Integer.parseInt(y_str);
						z = Integer.parseInt(z_str);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //walk <coordinates>");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //walk <coordinates>");
					return false;
				}
				if ((x == 0) && (y == 0))
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //walk <valid_coordinates>");
					return false;
				}
				final Location pos = new Location(x, y, z, 0);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
				return true;
			}
			case "admin_recall_npc":
			{
				recallNPC(activeChar);
				break;
			}
			case "admin_gonorth":
			case "admin_gosouth":
			case "admin_goeast":
			case "admin_gowest":
			case "admin_goup":
			case "admin_godown":
			{
				int intVal = 150;
				int x = activeChar.getX();
				int y = activeChar.getY();
				int z = activeChar.getZ();
				try
				{
					final String val = command.substring(8);
					st = new StringTokenizer(val);
					final String dir = st.nextToken();
					if (st.hasMoreTokens())
					{
						intVal = Integer.parseInt(st.nextToken());
					}
					switch (dir)
					{
						case "east":
						{
							x += intVal;
							break;
						}
						case "west":
						{
							x -= intVal;
							break;
						}
						case "north":
						{
							y -= intVal;
							break;
						}
						case "south":
						{
							y += intVal;
							break;
						}
						case "up":
						{
							z += intVal;
							break;
						}
						case "down":
						{
							z -= intVal;
							break;
						}
					}
					
					activeChar.teleToLocation(x, y, z, false);
					showTeleportWindow(activeChar);
					
					return true;
				}
				catch (Exception e)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //go<north|south|east|west|up|down> [offset] (default 150)");
					return false;
				}
			}
			case "admin_tele":
			{
				showTeleportWindow(activeChar);
				break;
			}
			case "admin_teleto":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				switch (val)
				{
					case "":
					{
						activeChar.setTeleMode(1);
						break;
					}
					case "r":
					{
						BuilderUtil.sendSysMessage(activeChar, "Instant move ready. Click where you want to go.");
						activeChar.setTeleMode(2);
						break;
					}
					case "end":
					{
						activeChar.setTeleMode(0);
						break;
					}
					default:
					{
						BuilderUtil.sendSysMessage(activeChar, "Defined mode not allowed..");
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void teleportTo(PlayerInstance activeChar, int x, int y, int z)
	{
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.teleToLocation(x, y, z, false);
		BuilderUtil.sendSysMessage(activeChar, "You have been teleported to " + x + " " + y + " " + z);
	}
	
	private void showTeleportWindow(PlayerInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "move.htm");
	}
	
	private void showTeleportCharWindow(PlayerInstance activeChar)
	{
		final WorldObject target = activeChar.getTarget();
		PlayerInstance player = null;
		if (target instanceof PlayerInstance)
		{
			player = (PlayerInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Teleport Character</title>");
		replyMSG.append("<body>");
		replyMSG.append("The character you will teleport is " + player.getName() + ".");
		replyMSG.append("<br>");
		replyMSG.append("Co-ordinate x");
		replyMSG.append("<edit var=\"char_cord_x\" width=110>");
		replyMSG.append("Co-ordinate y");
		replyMSG.append("<edit var=\"char_cord_y\" width=110>");
		replyMSG.append("Co-ordinate z");
		replyMSG.append("<edit var=\"char_cord_z\" width=110>");
		replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void teleportToCharacter(PlayerInstance activeChar, WorldObject target)
	{
		PlayerInstance player = null;
		if (target instanceof PlayerInstance)
		{
			player = (PlayerInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_ON_YOURSELF);
		}
		else
		{
			final int x = player.getX();
			final int y = player.getY();
			final int z = player.getZ();
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, true);
			BuilderUtil.sendSysMessage(activeChar, "You have teleported to character " + player.getName() + ".");
		}
	}
	
	private void recallNPC(PlayerInstance activeChar)
	{
		final WorldObject obj = activeChar.getTarget();
		if (obj instanceof NpcInstance)
		{
			final NpcInstance target = (NpcInstance) obj;
			final int monsterTemplate = target.getTemplate().getNpcId();
			final NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
			if (template1 == null)
			{
				BuilderUtil.sendSysMessage(activeChar, "Incorrect monster template.");
				LOGGER.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
				return;
			}
			
			Spawn spawn = target.getSpawn();
			if (spawn == null)
			{
				BuilderUtil.sendSysMessage(activeChar, "Incorrect monster spawn.");
				LOGGER.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
				return;
			}
			
			if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()) || GrandBossManager.getInstance().isDefined(spawn.getNpcId()))
			{
				BuilderUtil.sendSysMessage(activeChar, "You cannot recall a boss instance.");
				return;
			}
			
			final int respawnTime = spawn.getRespawnDelay() / 1000;
			target.deleteMe();
			spawn.stopRespawn();
			
			SpawnTable.getInstance().deleteSpawn(spawn, true);
			
			try
			{
				spawn = new Spawn(template1);
				spawn.setX(activeChar.getX());
				spawn.setY(activeChar.getY());
				spawn.setZ(activeChar.getZ());
				spawn.setAmount(1);
				spawn.setHeading(activeChar.getHeading());
				spawn.setRespawnDelay(respawnTime);
				SpawnTable.getInstance().addNewSpawn(spawn, true);
				spawn.init();
				
				BuilderUtil.sendSysMessage(activeChar, "Created " + template1.getName() + " on " + target.getObjectId() + ".");
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Target is not in game.");
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
		}
	}
}
