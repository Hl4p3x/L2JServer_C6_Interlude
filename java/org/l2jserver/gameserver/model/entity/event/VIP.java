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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.PlayerInventory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class VIP
{
	private static final Logger LOGGER = Logger.getLogger(VIP.class.getName());
	
	public static String _teamName = "";
	public static String _joinArea = "";
	public static int _time = 0;
	public static int _winners = 0;
	public static int _vipReward = 0;
	public static int _vipRewardAmount = 0;
	public static int _notVipReward = 0;
	public static int _notVipRewardAmount = 0;
	public static int _theVipReward = 0;
	public static int _theVipRewardAmount = 0;
	public static int _endNPC = 0;
	public static int _joinNPC = 0;
	public static int _delay = 0;
	public static int _endX = 0;
	public static int _endY = 0;
	public static int _endZ = 0;
	public static int _startX = 0;
	public static int _startY = 0;
	public static int _startZ = 0;
	public static int _joinX = 0;
	public static int _joinY = 0;
	public static int _joinZ = 0;
	public static int _team = 0;
	public static boolean _started = false;
	public static boolean _joining = false;
	public static boolean _inProgress = true;
	public static boolean _sitForced = false;
	public static Spawn _endSpawn;
	public static Spawn _joinSpawn;
	public static List<PlayerInstance> _playersVIP = new ArrayList<>();
	public static List<PlayerInstance> _playersNotVIP = new ArrayList<>();
	
	public static void setTeam(String team, PlayerInstance player)
	{
		if (team.equalsIgnoreCase("Human"))
		{
			_team = 1;
			_teamName = "Human";
		}
		else if (team.equalsIgnoreCase("Elf"))
		{
			_team = 2;
			_teamName = "Elf";
		}
		else if (team.equalsIgnoreCase("Dark"))
		{
			_team = 3;
			_teamName = "Dark Elf";
		}
		else if (team.equalsIgnoreCase("Orc"))
		{
			_team = 4;
			_teamName = "Orc";
		}
		else if (team.equalsIgnoreCase("Dwarf"))
		{
			_team = 5;
			_teamName = "Dwarf";
		}
		else
		{
			player.sendMessage("Invalid Team Name: //vip_setteam <human/elf/dark/orc/dwarf>");
			return;
		}
		setLoc();
	}
	
	public static void setRandomTeam(PlayerInstance player)
	{
		final int random = Rnd.get(5) + 1; // (0 - 4) + 1
		LOGGER.info("Random number generated in setRandomTeam(): " + random);
		
		switch (random)
		{
			case 1:
			{
				_team = 1;
				_teamName = "Human";
				setLoc();
				break;
			}
			case 2:
			{
				_team = 2;
				_teamName = "Elf";
				setLoc();
				break;
			}
			case 3:
			{
				_team = 3;
				_teamName = "Dark";
				setLoc();
				break;
			}
			case 4:
			{
				_team = 4;
				_teamName = "Orc";
				setLoc();
				break;
			}
			case 5:
			{
				_team = 5;
				_teamName = "Dwarf";
				setLoc();
				break;
			}
			default:
			{
				break;
			}
		}
	}
	
	public static void setLoc()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT endx,endy,endz FROM VIPinfo WHERE teamID = " + _team);
			final ResultSet rset = statement.executeQuery();
			rset.next();
			
			_endX = rset.getInt("endx");
			_endY = rset.getInt("endy");
			_endZ = rset.getInt("endz");
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.info("Could not check End LOC for team" + _team + " got: " + e.getMessage());
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT startx,starty,startz FROM VIPinfo WHERE teamID = " + _team);
			final ResultSet rset = statement.executeQuery();
			rset.next();
			
			_startX = rset.getInt("startx");
			_startY = rset.getInt("starty");
			_startZ = rset.getInt("startz");
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.info("Could not check Start LOC for team" + _team + " got: " + e.getMessage());
		}
	}
	
	public static void endNPC(int npcId, PlayerInstance player)
	{
		if (_team == 0)
		{
			player.sendMessage("Please select a team first");
			return;
		}
		
		final NpcTemplate npctmp = NpcTable.getInstance().getTemplate(npcId);
		_endNPC = npcId;
		
		try
		{
			_endSpawn = new Spawn(npctmp);
			_endSpawn.setX(_endX);
			_endSpawn.setY(_endY);
			_endSpawn.setZ(_endZ);
			_endSpawn.setAmount(1);
			_endSpawn.setHeading(player.getHeading());
			_endSpawn.setRespawnDelay(1);
		}
		catch (Exception e)
		{
			player.sendMessage("VIP Engine[endNPC(" + player.getName() + ")]: exception: " + e.getMessage());
		}
	}
	
	public static void joinNPC(int npcId, PlayerInstance player)
	{
		if (_joinX == 0)
		{
			player.sendMessage("Please set a join x,y,z first");
			return;
		}
		
		final NpcTemplate npctmp = NpcTable.getInstance().getTemplate(npcId);
		_joinNPC = npcId;
		
		try
		{
			_joinSpawn = new Spawn(npctmp);
			_joinSpawn.setX(_joinX);
			_joinSpawn.setY(_joinY);
			_joinSpawn.setZ(_joinZ);
			_joinSpawn.setAmount(1);
			_joinSpawn.setHeading(player.getHeading());
			_joinSpawn.setRespawnDelay(1);
		}
		catch (Exception e)
		{
			player.sendMessage("VIP Engine[joinNPC(" + player.getName() + ")]: exception: " + e.getMessage());
		}
	}
	
	public static void spawnEndNPC()
	{
		try
		{
			SpawnTable.getInstance().addNewSpawn(_endSpawn, false);
			_endSpawn.init();
			_endSpawn.getLastSpawn().setCurrentHp(999999999);
			_endSpawn.getLastSpawn().setTitle("VIP Event Manager");
			_endSpawn.getLastSpawn().isAggressive();
			_endSpawn.getLastSpawn().decayMe();
			_endSpawn.getLastSpawn().spawnMe(_endSpawn.getLastSpawn().getX(), _endSpawn.getLastSpawn().getY(), _endSpawn.getLastSpawn().getZ());
			_endSpawn.getLastSpawn().broadcastPacket(new MagicSkillUse(_endSpawn.getLastSpawn(), _endSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			LOGGER.info("VIP Engine[spawnEndNPC()]: exception: " + e.getMessage());
		}
	}
	
	public static void spawnJoinNPC()
	{
		try
		{
			SpawnTable.getInstance().addNewSpawn(_joinSpawn, false);
			_joinSpawn.init();
			_joinSpawn.getLastSpawn().setCurrentHp(999999999);
			_joinSpawn.getLastSpawn().setTitle("VIP Event Manager");
			_joinSpawn.getLastSpawn().isAggressive();
			_joinSpawn.getLastSpawn().decayMe();
			_joinSpawn.getLastSpawn().spawnMe(_joinSpawn.getLastSpawn().getX(), _joinSpawn.getLastSpawn().getY(), _joinSpawn.getLastSpawn().getZ());
			_joinSpawn.getLastSpawn().broadcastPacket(new MagicSkillUse(_joinSpawn.getLastSpawn(), _joinSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			LOGGER.info("VIP Engine[spawnJoinNPC()]: exception: " + e.getMessage());
		}
	}
	
	public static String getNPCName(int id, PlayerInstance player)
	{
		if (id == 0)
		{
			return "";
		}
		
		final NpcTemplate npctmp = NpcTable.getInstance().getTemplate(id);
		return npctmp.getName();
	}
	
	public static String getItemName(int id, PlayerInstance player)
	{
		if (id == 0)
		{
			return "";
		}
		final Item itemtmp = ItemTable.getInstance().getTemplate(id);
		return itemtmp.getName();
	}
	
	public static void setJoinLOC(String x, String y, String z)
	{
		_joinX = Integer.parseInt(x);
		_joinY = Integer.parseInt(y);
		_joinZ = Integer.parseInt(z);
	}
	
	public static void startJoin(PlayerInstance player)
	{
		if ((_time == 0) || (_team == 0) || (_endNPC == 0) || (_delay == 0))
		{
			player.sendMessage("Cannot initiate join status of event, not all the values are filled in");
			return;
		}
		
		if (_joining)
		{
			player.sendMessage("Players are already allowed to join the event");
			return;
		}
		
		if (_started)
		{
			player.sendMessage("Event already started. Please wait for it to finish or finish it manually");
			return;
		}
		
		_inProgress = true;
		_joining = true;
		Announcements.getInstance().criticalAnnounceToAll("Vip event has started.Use .vipjoin to join or .vipleave to leave.");
		spawnJoinNPC();
		
		ThreadPool.schedule(() ->
		{
			_joining = false;
			_started = true;
			startEvent();
		}, _delay);
	}
	
	public static void startEvent()
	{
		Announcements.getInstance().criticalAnnounceToAll("Registration for the VIP event involving " + _teamName + " has ended.");
		Announcements.getInstance().criticalAnnounceToAll("Players will be teleported to their locations in 20 seconds.");
		ThreadPool.schedule(() ->
		{
			teleportPlayers();
			chooseVIP();
			setUserData();
			Announcements.getInstance().criticalAnnounceToAll("Players have been teleported for the VIP event.");
			Announcements.getInstance().criticalAnnounceToAll("VIP event will start in 20 seconds.");
			spawnEndNPC();
			
			ThreadPool.schedule(() ->
			{
				Announcements.getInstance().criticalAnnounceToAll("VIP event has started. " + _teamName + "'s VIP must get to the starter city and talk with " + getNPCName(_endNPC, null) + ". The opposing team must kill the VIP. All players except the VIP will respawn at their current locations.");
				Announcements.getInstance().criticalAnnounceToAll("VIP event will end if the " + _teamName + " team makes it to their town or when " + (_time / 1000 / 60) + " mins have elapsed.");
				VIP.sit();
				
				ThreadPool.schedule(VIP::endEventTime, _time);
			}, 20000);
		}, 20000);
	}
	
	public static void vipDied()
	{
		if (!_started)
		{
			LOGGER.info("Could not finish the event. Event not started or event ended prematurly.");
			return;
		}
		
		_started = false;
		unspawnEventNpcs();
		Announcements.getInstance().criticalAnnounceToAll("The VIP has died. The opposing team has won.");
		rewardNotVIP();
		teleportFinish();
	}
	
	public static void endEventTime()
	{
		if (!_started)
		{
			LOGGER.info("Could not finish the event. Event not started or event ended prematurly (VIP died)");
			return;
		}
		
		_started = false;
		unspawnEventNpcs();
		Announcements.getInstance().criticalAnnounceToAll("The time has run out and the " + _teamName + "'s have not made it to their goal. Everybody on the opposing team wins.");
		rewardNotVIP();
		teleportFinish();
	}
	
	public static void unspawnEventNpcs()
	{
		if (_endSpawn != null)
		{
			_endSpawn.getLastSpawn().deleteMe();
			_endSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(_endSpawn, true);
		}
		
		if (_joinSpawn != null)
		{
			_joinSpawn.getLastSpawn().deleteMe();
			_joinSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(_joinSpawn, true);
		}
	}
	
	public static void showEndHTML(PlayerInstance eventPlayer, String objectId)
	{
		try
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("VIP (End NPC)<br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("Team:&nbsp;<font color=\"FFFFFF\">" + _teamName + "</font><br><br>");
			if (!_started)
			{
				replyMSG.append("<center>Please wait until the admin/gm starts the joining period.</center>");
			}
			else if (eventPlayer._isTheVIP)
			{
				replyMSG.append("You have made it to the end. All you have to do is hit the finish button to reward yourself and your team. Congrats!<br>");
				replyMSG.append("<center>");
				replyMSG.append("<button value=\"Finish\" action=\"bypass -h npc_" + objectId + "_vip_finishVIP\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				replyMSG.append("</center>");
			}
			else
			{
				replyMSG.append("I am the character the VIP has to reach in order to win the event.<br>");
			}
			
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			LOGGER.info("VIP(showJoinHTML(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
	
	public static void vipWin(PlayerInstance player)
	{
		if (!_started)
		{
			LOGGER.info("Could not finish the event. Event not started or event ended prematurly");
			return;
		}
		
		_started = false;
		unspawnEventNpcs();
		Announcements.getInstance().criticalAnnounceToAll("The VIP has made it to the goal. " + _teamName + " has won. Everybody on that team wins.");
		rewardVIP();
		teleportFinish();
	}
	
	public static void rewardNotVIP()
	{
		for (PlayerInstance player : _playersNotVIP)
		{
			if (player != null)
			{
				final PlayerInventory inv = player.getInventory();
				if (ItemTable.getInstance().createDummyItem(_notVipReward).isStackable())
				{
					inv.addItem("VIP Event: ", _notVipReward, _notVipRewardAmount, player, null);
				}
				else
				{
					for (int i = 0; i <= (_notVipRewardAmount - 1); i++)
					{
						inv.addItem("VIP Event: ", _notVipReward, 1, player, null);
					}
				}
				
				SystemMessage sm;
				if (_notVipRewardAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
					sm.addItemName(_notVipReward);
					sm.addNumber(_notVipRewardAmount);
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
					sm.addItemName(_notVipReward);
					player.sendPacket(sm);
				}
				
				final StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				
				final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
				final StringBuilder replyMSG = new StringBuilder("");
				replyMSG.append("<html><body>Your team has won the event. Your inventory now contains your reward.</body></html>");
				nhm.setHtml(replyMSG.toString());
				player.sendPacket(nhm);
			}
		}
	}
	
	public static void rewardVIP()
	{
		for (PlayerInstance player : _playersVIP)
		{
			if ((player != null) && !player._isTheVIP)
			{
				final PlayerInventory inv = player.getInventory();
				if (ItemTable.getInstance().createDummyItem(_vipReward).isStackable())
				{
					inv.addItem("VIP Event: ", _vipReward, _vipRewardAmount, player, null);
				}
				else
				{
					for (int i = 0; i <= (_vipRewardAmount - 1); i++)
					{
						inv.addItem("VIP Event: ", _vipReward, 1, player, null);
					}
				}
				
				SystemMessage sm;
				if (_vipRewardAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
					sm.addItemName(_vipReward);
					sm.addNumber(_vipRewardAmount);
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
					sm.addItemName(_vipReward);
					player.sendPacket(sm);
				}
				
				final StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				
				final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
				final StringBuilder replyMSG = new StringBuilder("");
				replyMSG.append("<html><body>Your team has won the event. Your inventory now contains your reward.</body></html>");
				nhm.setHtml(replyMSG.toString());
				player.sendPacket(nhm);
			}
			else if ((player != null) && player._isTheVIP)
			{
				final PlayerInventory inv = player.getInventory();
				if (ItemTable.getInstance().createDummyItem(_theVipReward).isStackable())
				{
					inv.addItem("VIP Event: ", _theVipReward, _theVipRewardAmount, player, null);
				}
				else
				{
					for (int i = 0; i <= (_theVipRewardAmount - 1); i++)
					{
						inv.addItem("VIP Event: ", _theVipReward, 1, player, null);
					}
				}
				
				SystemMessage sm;
				if (_theVipRewardAmount > 1)
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
					sm.addItemName(_theVipReward);
					sm.addNumber(_theVipRewardAmount);
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
					sm.addItemName(_theVipReward);
					player.sendPacket(sm);
				}
				
				final StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				
				final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
				final StringBuilder replyMSG = new StringBuilder("");
				replyMSG.append("<html><body>You team have won the event. Your inventory now contains your reward.</body></html>");
				nhm.setHtml(replyMSG.toString());
				player.sendPacket(nhm);
			}
		}
	}
	
	public static void teleportFinish()
	{
		Announcements.getInstance().criticalAnnounceToAll("Teleporting VIP players back to the Registration area in 20 seconds.");
		ThreadPool.schedule(() ->
		{
			for (PlayerInstance player1 : _playersVIP)
			{
				if (player1 != null)
				{
					player1.teleToLocation(_joinX, _joinY, _joinZ);
				}
			}
			
			for (PlayerInstance player2 : _playersNotVIP)
			{
				if (player2 != null)
				{
					player2.teleToLocation(_joinX, _joinY, _joinZ);
				}
			}
			
			VIP.clean();
		}, 20000);
	}
	
	public static void clean()
	{
		_time = _winners = _endNPC = _joinNPC = _delay = _endX = _endY = _endZ = _startX = _startY = _startZ = _joinX = _joinY = _joinZ = _team = 0;
		_vipReward = _vipRewardAmount = _notVipReward = _notVipRewardAmount = _theVipReward = _theVipRewardAmount = 0;
		_started = _joining = _sitForced = false;
		_inProgress = false;
		_teamName = _joinArea = "";
		for (PlayerInstance player : _playersVIP)
		{
			player.getAppearance().setNameColor(player._originalNameColourVIP);
			player.setKarma(player._originalKarmaVIP);
			player.broadcastUserInfo();
			player._inEventVIP = false;
			player._isTheVIP = false;
			player._isNotVIP = false;
			player._isVIP = false;
		}
		
		for (PlayerInstance player : _playersNotVIP)
		{
			player.getAppearance().setNameColor(player._originalNameColourVIP);
			player.setKarma(player._originalKarmaVIP);
			player.broadcastUserInfo();
			player._inEventVIP = false;
			player._isTheVIP = false;
			player._isNotVIP = false;
			player._isVIP = false;
		}
		
		_playersVIP = new ArrayList<>();
		_playersNotVIP = new ArrayList<>();
	}
	
	public static void chooseVIP()
	{
		final int size = _playersVIP.size();
		LOGGER.info("Size of players on VIP: " + size);
		
		final int random = Rnd.get(size);
		LOGGER.info("Random number chosen in VIP: " + random);
		
		final PlayerInstance vip = _playersVIP.get(random);
		vip._isTheVIP = true;
	}
	
	public static void teleportPlayers()
	{
		sit();
		
		for (PlayerInstance player : _playersVIP)
		{
			if (player != null)
			{
				player.teleToLocation(_startX, _startY, _startZ);
			}
		}
		for (PlayerInstance player : _playersNotVIP)
		{
			if (player != null)
			{
				player.teleToLocation(_endX, _endY, _endZ);
			}
		}
	}
	
	public static void sit()
	{
		if (_sitForced)
		{
			_sitForced = false;
		}
		else
		{
			_sitForced = true;
		}
		
		for (PlayerInstance player : _playersVIP)
		{
			if (player != null)
			{
				if (_sitForced)
				{
					player.stopMove(null, false);
					player.abortAttack();
					player.abortCast();
					
					if (!player.isSitting())
					{
						player.sitDown();
					}
				}
				else if (player.isSitting())
				{
					player.standUp();
				}
			}
		}
		
		for (PlayerInstance player : _playersNotVIP)
		{
			if (player != null)
			{
				if (_sitForced)
				{
					player.stopMove(null, false);
					player.abortAttack();
					player.abortCast();
					
					if (!player.isSitting())
					{
						player.sitDown();
					}
				}
				else if (player.isSitting())
				{
					player.standUp();
				}
			}
		}
	}
	
	public static void setUserData()
	{
		for (PlayerInstance player : _playersVIP)
		{
			if (player._isTheVIP)
			{
				player.getAppearance().setNameColor(255, 255, 0);
			}
			else
			{
				player.getAppearance().setNameColor(255, 0, 0);
			}
			
			player.setKarma(0);
			player.broadcastUserInfo();
		}
		for (PlayerInstance player : _playersNotVIP)
		{
			player.getAppearance().setNameColor(0, 255, 0);
			player.setKarma(0);
			player.broadcastUserInfo();
		}
	}
	
	public static void showJoinHTML(PlayerInstance eventPlayer, String objectId)
	{
		try
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("VIP (Join NPC)<br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("	... Team:&nbsp;<font color=\"FFFFFF\">" + _teamName + "</font><br><br>");
			if (!_joining && !_started)
			{
				replyMSG.append("<center>Please wait until the admin/gm starts the joining period.</center>");
			}
			else if (_joining && !_started)
			{
				// Joining period
				if (_playersVIP.contains(eventPlayer) || _playersNotVIP.contains(eventPlayer))
				{
					replyMSG.append("You are already on a team<br><br>");
				}
				else
				{
					replyMSG.append("You want to participate in the event?<br><br>");
					if ((eventPlayer.getRace() == Race.HUMAN) && (_team == 1))
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if ((eventPlayer.getRace() == Race.ELF) && (_team == 2))
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if ((eventPlayer.getRace() == Race.DARK_ELF) && (_team == 3))
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if ((eventPlayer.getRace() == Race.ORC) && (_team == 4))
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else if ((eventPlayer.getRace() == Race.DWARF) && (_team == 5))
					{
						replyMSG.append("It seems you are on the VIP race! Be prepared to protect the VIP when it is decided<br1>");
						replyMSG.append("The VIP will be decided on when the event starts. It's completely random.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
					else
					{
						replyMSG.append("It seems you are not on the part of the VIP race.<br>");
						replyMSG.append("When the event starts you will be teleported to the " + _teamName + " town<br1>");
						replyMSG.append("Be sure to cooperate with your team to destroy the VIP.<br1>");
						replyMSG.append("The VIP will be announced when the event starts.<br>");
						replyMSG.append("<center>");
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_vip_joinNotVIPTeam\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						replyMSG.append("</center>");
					}
				}
			}
			else if (_started)
			{
				replyMSG.append("<center>The event is already taking place. Please sign up for the next event.</center>");
			}
			
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			LOGGER.info("VIP(showJoinHTML(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
	
	public static void addPlayerVIP(PlayerInstance player)
	{
		player._isVIP = true;
		_playersVIP.add(player);
		player._originalNameColourVIP = player.getAppearance().getNameColor();
		player._originalKarmaVIP = player.getKarma();
		player._inEventVIP = true;
	}
	
	public static void addPlayerNotVIP(PlayerInstance player)
	{
		player._isNotVIP = true;
		_playersNotVIP.add(player);
		player._originalNameColourVIP = player.getAppearance().getNameColor();
		player._originalKarmaVIP = player.getKarma();
		player._inEventVIP = true;
	}
	
	public static void onDisconnect(PlayerInstance player)
	{
		if (player._inEventTvT)
		{
			player.getAppearance().setNameColor(player._originalNameColourVIP);
			player.setKarma(player._originalKarmaVIP);
			player.broadcastUserInfo();
			player._inEventVIP = false;
			player._isTheVIP = false;
			player._isNotVIP = false;
			player._isVIP = false;
			player.teleToLocation(_startX, _startY, _startZ);
		}
	}
}