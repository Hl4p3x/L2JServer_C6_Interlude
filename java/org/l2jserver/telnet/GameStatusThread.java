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
package org.l2jserver.telnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.LoginServerThread;
import org.l2jserver.gameserver.Shutdown;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.sql.TeleportLocationTable;
import org.l2jserver.gameserver.datatables.xml.AdminData;
import org.l2jserver.gameserver.datatables.xml.ZoneData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.instancemanager.DayNightSpawnManager;
import org.l2jserver.gameserver.instancemanager.QuestManager;
import org.l2jserver.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jserver.gameserver.model.Inventory;
import org.l2jserver.gameserver.model.TradeList;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.multisell.Multisell;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CharInfo;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;
import org.l2jserver.gameserver.taskmanager.DecayTaskManager;
import org.l2jserver.gameserver.util.GMAudit;

public class GameStatusThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(GameStatusThread.class.getName());
	
	private final Socket _cSocket;
	
	private final PrintWriter _print;
	private final BufferedReader _read;
	
	private final int _uptime;
	
	private void telnetOutput(int type, String text)
	{
		if (type == 5)
		{
			LOGGER.info("TELNET | " + text);
		}
	}
	
	private boolean isValidIP(Socket client)
	{
		boolean result = false;
		final InetAddress clientIP = client.getInetAddress();
		
		// convert IP to String, and compare with list
		final String clientStringIP = clientIP.getHostAddress();
		telnetOutput(1, "Connection from: " + clientStringIP);
		
		// read and loop thru list of IPs, compare with newIP
		InputStream telnetIS = null;
		try
		{
			final Properties telnetSettings = new Properties();
			telnetIS = new FileInputStream(new File(Config.TELNET_CONFIG_FILE));
			telnetSettings.load(telnetIS);
			
			final String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost,::1");
			
			// compare
			String ipToCompare = null;
			for (String ip : HostList.split(","))
			{
				if (!result)
				{
					ipToCompare = InetAddress.getByName(ip).getHostAddress();
					if (clientStringIP.equals(ipToCompare))
					{
						result = true;
					}
				}
			}
		}
		catch (IOException e)
		{
			telnetOutput(1, "Error: " + e);
		}
		finally
		{
			if (telnetIS != null)
			{
				try
				{
					telnetIS.close();
				}
				catch (Exception e)
				{
					LOGGER.warning(e.toString());
				}
			}
		}
		return result;
	}
	
	public GameStatusThread(Socket client, int uptime, String statusPW) throws IOException
	{
		setPriority(Thread.MAX_PRIORITY);
		_cSocket = client;
		_uptime = uptime;
		_print = new PrintWriter(_cSocket.getOutputStream());
		_read = new BufferedReader(new InputStreamReader(_cSocket.getInputStream()));
		if (isValidIP(client))
		{
			telnetOutput(1, client.getInetAddress().getHostAddress() + " accepted.");
			_print.println("Welcome To The L2J Telnet Session.");
			_print.println("Please Insert Your Password!");
			_print.print("Password: ");
			_print.flush();
			final String tmpLine = _read.readLine();
			if (tmpLine == null)
			{
				_print.println("Error.");
				_print.println("Disconnected...");
				_print.flush();
				_cSocket.close();
			}
			else if (tmpLine.compareTo(statusPW) != 0)
			{
				_print.println("Incorrect Password!");
				_print.println("Disconnected...");
				_print.flush();
				_cSocket.close();
			}
			else
			{
				_print.println("Password Correct!");
				_print.println("[L2J Game Server]");
				_print.print("");
				_print.flush();
			}
		}
		else
		{
			telnetOutput(5, "Connection attempt from " + client.getInetAddress().getHostAddress() + " rejected.");
			_cSocket.close();
		}
	}
	
	@Override
	public void run()
	{
		String usrCommand = "";
		try
		{
			while ((usrCommand.compareTo("quit") != 0) && (usrCommand.compareTo("exit") != 0))
			{
				usrCommand = _read.readLine();
				if (usrCommand == null)
				{
					_cSocket.close();
					break;
				}
				if (usrCommand.equals("help"))
				{
					_print.println("The following is a list of all available commands: ");
					_print.println("help                  - shows this help.");
					_print.println("status                - displays basic server statistics.");
					_print.println("gamestat privatestore - displays info about stores");
					_print.println("performance           - shows server performance statistics.");
					_print.println("forcegc               - forced garbage collection.");
					_print.println("purge                 - removes finished threads from thread pools.");
					_print.println("memusage              - displays memory amounts in JVM.");
					_print.println("announce <text>       - announces <text> in game.");
					_print.println("msg <nick> <text>     - Sends a whisper to char <nick> with <text>.");
					_print.println("gmchat <text>         - Sends a message to all GMs with <text>.");
					_print.println("gmlist                - lists all gms online.");
					_print.println("kick                  - kick player <name> from server.");
					_print.println("shutdown <time>       - shuts down server in <time> seconds.");
					_print.println("restart <time>        - restarts down server in <time> seconds.");
					_print.println("abort                 - aborts shutdown/restart.");
					_print.println("give <player> <itemid> <amount>");
					_print.println("enchant <player> <itemType> <enchant> (itemType: 1 - Helmet, 2 - Chest, 3 - Gloves, 4 - Feet, 5 - Legs, 6 - Right Hand, 7 - Left Hand, 8 - Left Ear, 9 - Right Ear , 10 - Left Finger, 11 - Right Finger, 12- Necklace, 13 - Underwear, 14 - Back, 15 - Belt, 0 - No Enchant)");
					_print.println("debug <cmd>           - executes the debug command (see 'help debug').");
					_print.println("reload <type>         - reload data");
					_print.println("jail <player> [time]");
					_print.println("unjail <player>");
					_print.println("quit                  - closes telnet session.");
				}
				else if (usrCommand.equals("help debug"))
				{
					_print.println("The following is a list of all available debug commands: ");
					_print.println("full                - Dumps complete debug information to an file (recommended)");
					_print.println("decay               - prints info about the DecayManager");
					_print.println("PacketTP            - prints info about the General Packet ThreadPool");
					_print.println("IOPacketTP          - prints info about the I/O Packet ThreadPool");
					_print.println("GeneralTP           - prints info about the General ThreadPool");
				}
				else if (usrCommand.equals("status"))
				{
					_print.print(getServerStatus());
					_print.flush();
				}
				else if (usrCommand.equals("forcegc"))
				{
					System.gc();
					final StringBuilder sb = new StringBuilder();
					sb.append("RAM Used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)); // 1024 * 1024 = 1048576
					_print.println(sb.toString());
				}
				else if (usrCommand.equals("performance"))
				{
					for (String line : ThreadPool.getStats())
					{
						_print.println(line);
					}
					_print.flush();
				}
				else if (usrCommand.equals("purge"))
				{
					ThreadPool.purge();
					_print.println("STATUS OF THREAD POOLS AFTER PURGE COMMAND:");
					_print.println("");
					for (String line : ThreadPool.getStats())
					{
						_print.println(line);
					}
					_print.flush();
				}
				else if (usrCommand.startsWith("memusage"))
				{
					final double max = Runtime.getRuntime().maxMemory() / 1024; // maxMemory is the upper
					// limit the jvm can use
					final double allocated = Runtime.getRuntime().totalMemory() / 1024; // totalMemory the
					// size of the
					// current
					// allocation pool
					final double nonAllocated = max - allocated; // non allocated memory till jvm limit
					final double cached = Runtime.getRuntime().freeMemory() / 1024; // freeMemory the
					// unused memory in
					// the allocation pool
					final double used = allocated - cached; // really used memory
					final double useable = max - used; // allocated, but non-used and non-allocated memory
					final DecimalFormat df = new DecimalFormat(" (0.0000'%')");
					final DecimalFormat df2 = new DecimalFormat(" # 'KB'");
					_print.println("+----"); // ...
					_print.println("| Allowed Memory:" + df2.format(max));
					_print.println("|    |= Allocated Memory:" + df2.format(allocated) + df.format((allocated / max) * 100));
					_print.println("|    |= Non-Allocated Memory:" + df2.format(nonAllocated) + df.format((nonAllocated / max) * 100));
					_print.println("| Allocated Memory:" + df2.format(allocated));
					_print.println("|    |= Used Memory:" + df2.format(used) + df.format((used / max) * 100));
					_print.println("|    |= Unused (cached) Memory:" + df2.format(cached) + df.format((cached / max) * 100));
					_print.println("| Useable Memory:" + df2.format(useable) + df.format((useable / max) * 100)); // ...
					_print.println("+----");
				}
				else if (usrCommand.startsWith("announce"))
				{
					try
					{
						usrCommand = usrCommand.substring(9);
						Announcements.getInstance().announceToAll(usrCommand);
						_print.println("Announcement Sent!");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text To Announce!");
					}
				}
				else if (usrCommand.startsWith("msg"))
				{
					try
					{
						final String val = usrCommand.substring(4);
						final StringTokenizer st = new StringTokenizer(val);
						final String name = st.nextToken();
						final String message = val.substring(name.length() + 1);
						final PlayerInstance reciever = World.getInstance().getPlayer(name);
						final CreatureSay cs = new CreatureSay(0, ChatType.WHISPER, "Telnet Priv", message);
						if (reciever != null)
						{
							reciever.sendPacket(cs);
							_print.println("Telnet Priv->" + name + ": " + message);
							_print.println("Message Sent!");
						}
						else
						{
							_print.println("Unable To Find Username: " + name);
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text!");
					}
				}
				else if (usrCommand.startsWith("gmchat"))
				{
					try
					{
						usrCommand = usrCommand.substring(7);
						final CreatureSay cs = new CreatureSay(0, ChatType.ALLIANCE, "Telnet GM Broadcast from " + _cSocket.getInetAddress().getHostAddress(), usrCommand);
						AdminData.broadcastToGMs(cs);
						_print.println("Your Message Has Been Sent To " + getOnlineGMs() + " GM(s).");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text To Announce!");
					}
				}
				else if (usrCommand.equals("gmlist"))
				{
					int igm = 0;
					String gmList = "";
					for (String player : AdminData.getInstance().getAllGmNames(true))
					{
						gmList = gmList + ", " + player;
						igm++;
					}
					_print.println("There are currently " + igm + " GM(s) online...");
					if (!gmList.isEmpty())
					{
						_print.println(gmList);
					}
				}
				else if (usrCommand.startsWith("kick"))
				{
					try
					{
						usrCommand = usrCommand.substring(5);
						final PlayerInstance player = World.getInstance().getPlayer(usrCommand);
						if (player != null)
						{
							player.sendMessage("You are kicked by gm");
							player.logout();
							_print.println("Player kicked");
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please enter player name to kick");
					}
				}
				else if (usrCommand.startsWith("shutdown"))
				{
					try
					{
						final int val = Integer.parseInt(usrCommand.substring(9));
						Shutdown.getInstance().startShutdown(null, val, false);
						_print.println("Server Will Shutdown In " + val + " Seconds!");
						_print.println("Type \"abort\" To Abort Shutdown!");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter * amount of seconds to shutdown!");
					}
					catch (Exception NumberFormatException)
					{
						_print.println("Numbers Only!");
					}
				}
				else if (usrCommand.startsWith("restart"))
				{
					try
					{
						final int val = Integer.parseInt(usrCommand.substring(8));
						Shutdown.getInstance().startShutdown(null, val, true);
						_print.println("Server Will Restart In " + val + " Seconds!");
						_print.println("Type \"abort\" To Abort Restart!");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter * amount of seconds to restart!");
					}
					catch (Exception NumberFormatException)
					{
						_print.println("Numbers Only!");
					}
				}
				else if (usrCommand.startsWith("abort"))
				{
					Shutdown.getInstance().abort(null);
					_print.println("OK! - Shutdown/Restart Aborted.");
				}
				else if (usrCommand.equals("quit"))
				{
					/* Do Nothing :p - Just here to save us from the "Command Not Understood" Text */
				}
				else if (usrCommand.startsWith("give"))
				{
					final StringTokenizer st = new StringTokenizer(usrCommand.substring(5));
					
					try
					{
						final PlayerInstance player = World.getInstance().getPlayer(st.nextToken());
						final int itemId = Integer.parseInt(st.nextToken());
						final int amount = Integer.parseInt(st.nextToken());
						if (player != null)
						{
							final ItemInstance item = player.getInventory().addItem("Status-Give", itemId, amount, null, null);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addItem(item);
							player.sendPacket(iu);
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
							sm.addItemName(itemId);
							sm.addNumber(amount);
							player.sendPacket(sm);
							_print.println("ok");
							GMAudit.auditGMAction("Telnet Admin", "Give Item", player.getName(), "item: " + itemId + " amount: " + amount);
						}
						else
						{
							_print.println("Player not found");
						}
					}
					catch (Exception e)
					{
					}
				}
				else if (usrCommand.startsWith("enchant"))
				{
					final StringTokenizer st = new StringTokenizer(usrCommand.substring(8), " ");
					int enchant = 0;
					int itemType = 0;
					
					try
					{
						final PlayerInstance player = World.getInstance().getPlayer(st.nextToken());
						itemType = Integer.parseInt(st.nextToken());
						enchant = Integer.parseInt(st.nextToken());
						
						switch (itemType)
						{
							case 1:
							{
								itemType = Inventory.PAPERDOLL_HEAD;
								break;
							}
							case 2:
							{
								itemType = Inventory.PAPERDOLL_CHEST;
								break;
							}
							case 3:
							{
								itemType = Inventory.PAPERDOLL_GLOVES;
								break;
							}
							case 4:
							{
								itemType = Inventory.PAPERDOLL_FEET;
								break;
							}
							case 5:
							{
								itemType = Inventory.PAPERDOLL_LEGS;
								break;
							}
							case 6:
							{
								itemType = Inventory.PAPERDOLL_RHAND;
								break;
							}
							case 7:
							{
								itemType = Inventory.PAPERDOLL_LHAND;
								break;
							}
							case 8:
							{
								itemType = Inventory.PAPERDOLL_LEAR;
								break;
							}
							case 9:
							{
								itemType = Inventory.PAPERDOLL_REAR;
								break;
							}
							case 10:
							{
								itemType = Inventory.PAPERDOLL_LFINGER;
								break;
							}
							case 11:
							{
								itemType = Inventory.PAPERDOLL_RFINGER;
								break;
							}
							case 12:
							{
								itemType = Inventory.PAPERDOLL_NECK;
								break;
							}
							case 13:
							{
								itemType = Inventory.PAPERDOLL_UNDER;
								break;
							}
							/*
							 * case 14: itemType = Inventory.PAPERDOLL_CLOAK; break; case 15: itemType = Inventory.PAPERDOLL_BELT; break;
							 */
							default:
							{
								itemType = 0;
							}
						}
						
						if (enchant > 65535)
						{
							enchant = 65535;
						}
						else if (enchant < 0)
						{
							enchant = 0;
						}
						
						boolean success = false;
						if ((player != null) && (itemType > 0))
						{
							success = setEnchant(player, enchant, itemType);
							if (success)
							{
								_print.println("Item enchanted successfully.");
							}
						}
						else if (!success)
						{
							_print.println("Item failed to enchant.");
						}
					}
					catch (Exception e)
					{
					}
				}
				else if (usrCommand.startsWith("jail"))
				{
					final StringTokenizer st = new StringTokenizer(usrCommand.substring(5));
					try
					{
						final String playerName = st.nextToken();
						final PlayerInstance playerObj = World.getInstance().getPlayer(playerName);
						int delay = 0;
						try
						{
							delay = Integer.parseInt(st.nextToken());
						}
						catch (NumberFormatException | NoSuchElementException nfe)
						{
						}
						// PlayerInstance playerObj = World.getInstance().getPlayer(player);
						if (playerObj != null)
						{
							playerObj.setPunishLevel(PlayerInstance.PunishLevel.JAIL, delay);
							_print.println("Character " + playerObj.getName() + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
						}
						else
						{
							jailOfflinePlayer(playerName, delay);
						}
					}
					catch (NoSuchElementException nsee)
					{
						_print.println("Specify a character name.");
					}
					catch (Exception e)
					{
					}
				}
				else if (usrCommand.startsWith("unjail"))
				{
					final StringTokenizer st = new StringTokenizer(usrCommand.substring(7));
					try
					{
						final String playerName = st.nextToken();
						final PlayerInstance playerObj = World.getInstance().getPlayer(playerName);
						if (playerObj != null)
						{
							playerObj.setPunishLevel(PlayerInstance.PunishLevel.NONE, 0);
							_print.println("Character " + playerObj.getName() + " removed from jail");
						}
						else
						{
							unjailOfflinePlayer(playerName);
						}
					}
					catch (NoSuchElementException nsee)
					{
						_print.println("Specify a character name.");
					}
					catch (Exception e)
					{
					}
				}
				else if (usrCommand.startsWith("debug") && (usrCommand.length() > 6))
				{
					final StringTokenizer st = new StringTokenizer(usrCommand.substring(6));
					final FileOutputStream fos = null;
					final OutputStreamWriter out = null;
					try
					{
						final String dbg = st.nextToken();
						if (dbg.equals("decay"))
						{
							_print.print(DecayTaskManager.getInstance().toString());
						}
						else if (dbg.equals("ai"))
						{
							/*
							 * _print.println("AITaskManagerStats"); for(String line : AITaskManager.getInstance().getStats()) { _print.println(line); }
							 */
						}
						else if (dbg.equals("aiflush"))
						{
							// AITaskManager.getInstance().flush();
						}
						else if (dbg.equals("full"))
						{
							debugAll();
						}
					}
					catch (Exception e)
					{
						LOGGER.warning(e.toString());
					}
					finally
					{
						if (out != null)
						{
							try
							{
								out.close();
							}
							catch (Exception e)
							{
								LOGGER.warning(e.toString());
							}
						}
						
						if (fos != null)
						{
							try
							{
								fos.close();
							}
							catch (Exception e)
							{
								LOGGER.warning(e.toString());
							}
						}
					}
				}
				else if (usrCommand.startsWith("reload"))
				{
					final StringTokenizer st = new StringTokenizer(usrCommand.substring(7));
					try
					{
						final String type = st.nextToken();
						if (type.equals("multisell"))
						{
							_print.print("Reloading multisell... ");
							Multisell.getInstance().reload();
							_print.println("done");
						}
						else if (type.equals("skill"))
						{
							_print.print("Reloading skills... ");
							SkillTable.getInstance().reload();
							_print.println("done");
						}
						else if (type.equals("npc"))
						{
							_print.print("Reloading npc templates... ");
							NpcTable.getInstance().reloadAllNpc();
							
							if (!Config.ALT_DEV_NO_QUESTS)
							{
								QuestManager.getInstance().reloadAllQuests();
							}
							
							_print.println("done");
						}
						else if (type.equals("html"))
						{
							_print.print("Reloading html cache... ");
							HtmCache.getInstance().reload();
							_print.println("done");
						}
						else if (type.equals("item"))
						{
							_print.print("Reloading item templates... ");
							ItemTable.getInstance().reload();
							_print.println("done");
						}
						else if (type.equals("zone"))
						{
							_print.print("Reloading zones... ");
							ZoneData.getInstance().reload();
							_print.println("done");
						}
						else if (type.equals("teleports"))
						{
							_print.print("Reloading telport location table... ");
							TeleportLocationTable.getInstance().load();
							_print.println("done");
						}
						else if (type.equals("spawns"))
						{
							_print.print("Reloading spawns... ");
							RaidBossSpawnManager.getInstance().cleanUp();
							DayNightSpawnManager.getInstance().cleanUp();
							World.getInstance().deleteVisibleNpcSpawns();
							NpcTable.getInstance().reloadAllNpc();
							SpawnTable.getInstance().reloadAll();
							RaidBossSpawnManager.getInstance().load();
							_print.println("done\n");
						}
					}
					catch (Exception e)
					{
					}
				}
				else if (usrCommand.startsWith("gamestat"))
				{
					final StringTokenizer st = new StringTokenizer(usrCommand.substring(9));
					try
					{
						final String type = st.nextToken();
						
						// name;type;x;y;itemId:enchant:price...
						if (type.equals("privatestore"))
						{
							final Collection<PlayerInstance> pls = World.getInstance().getAllPlayers();
							// synchronized (World.getInstance().getAllPlayers())
							{
								for (PlayerInstance player : pls)
								{
									if (player.getPrivateStoreType() == 0)
									{
										continue;
									}
									
									TradeList list = null;
									String content = "";
									if (player.getPrivateStoreType() == 1) // sell
									{
										list = player.getSellList();
										for (TradeList.TradeItem item : list.getItems())
										{
											content += item.getItem().getItemId() + ":" + item.getEnchant() + ":" + item.getPrice() + ":";
										}
										content = player.getName() + ";sell;" + player.getX() + ";" + player.getY() + ";" + content;
										_print.println(content);
									}
									else if (player.getPrivateStoreType() == 3) // buy
									{
										list = player.getBuyList();
										for (TradeList.TradeItem item : list.getItems())
										{
											content += item.getItem().getItemId() + ":" + item.getEnchant() + ":" + item.getPrice() + ":";
										}
										content = player.getName() + ";buy;" + player.getX() + ";" + player.getY() + ";" + content;
										_print.println(content);
									}
								}
							}
						}
					}
					catch (Exception e)
					{
					}
				}
				else if (usrCommand.length() == 0)
				{ /* Do Nothing Again - Same reason as the quit part */
				}
				_print.print("");
				_print.flush();
			}
			if (!_cSocket.isClosed())
			{
				_print.println("Bye Bye!");
				_print.flush();
				_cSocket.close();
			}
			telnetOutput(1, "Connection from " + _cSocket.getInetAddress().getHostAddress() + " was closed by client.");
		}
		catch (IOException e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	private boolean setEnchant(PlayerInstance player, int ench, int armorType)
	{
		// now we need to find the equipped weapon of the targeted character...
		int curEnchant = 0; // display purposes only
		ItemInstance itemInstance = null;
		
		// only attempt to enchant if there is a weapon equipped
		ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
		if ((parmorInstance != null) && (parmorInstance.getLocationSlot() == armorType))
		{
			itemInstance = parmorInstance;
		}
		else
		{
			// for bows/crossbows and double handed weapons
			parmorInstance = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if ((parmorInstance != null) && (parmorInstance.getLocationSlot() == Inventory.PAPERDOLL_RHAND))
			{
				itemInstance = parmorInstance;
			}
		}
		
		if (itemInstance != null)
		{
			curEnchant = itemInstance.getEnchantLevel();
			
			// set enchant value
			player.getInventory().unEquipItemInSlot(armorType);
			itemInstance.setEnchantLevel(ench);
			player.getInventory().equipItem(itemInstance);
			
			// send packets
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(itemInstance);
			player.sendPacket(iu);
			player.broadcastPacket(new CharInfo(player));
			player.sendPacket(new UserInfo(player));
			// player.broadcastPacket(new ExBrExtraUserInfo(player));
			
			// informations
			player.sendMessage("Changed enchantment of " + player.getName() + "'s " + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");
			player.sendMessage("Admin has changed the enchantment of your " + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");
			
			// LOGGER
			GMAudit.auditGMAction("TelnetAdministrator", "enchant", player.getName(), itemInstance.getItem().getName() + "(" + itemInstance.getObjectId() + ") from " + curEnchant + " to " + ench);
			return true;
		}
		return false;
	}
	
	private void jailOfflinePlayer(String name, int delay)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, PlayerInstance.PunishLevel.JAIL.value());
			statement.setLong(5, delay * 60000);
			statement.setString(6, name);
			statement.execute();
			final int count = statement.getUpdateCount();
			statement.close();
			
			if (count == 0)
			{
				_print.println("Character not found!");
			}
			else
			{
				_print.println("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
			}
		}
		catch (SQLException se)
		{
			_print.println("SQLException while jailing player");
		}
	}
	
	private void unjailOfflinePlayer(String name)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, punish_level=?, punish_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.execute();
			final int count = statement.getUpdateCount();
			statement.close();
			
			if (count == 0)
			{
				_print.println("Character not found!");
			}
			else
			{
				_print.println("Character " + name + " set free.");
			}
		}
		catch (SQLException se)
		{
			_print.println("SQLException while jailing player");
		}
	}
	
	private int getOnlineGMs()
	{
		return AdminData.getInstance().getAllGms(true).size();
	}
	
	private String getUptime(int time)
	{
		int uptime = (int) System.currentTimeMillis() - time;
		uptime = uptime / 1000;
		final int h = uptime / 3600;
		final int m = (uptime - (h * 3600)) / 60;
		final int s = ((uptime - (h * 3600)) - (m * 60));
		return h + "hrs " + m + "mins " + s + "secs";
	}
	
	private String gameTime()
	{
		final int t = GameTimeController.getInstance().getGameTime();
		final int h = t / 60;
		final int m = t % 60;
		final SimpleDateFormat format = new SimpleDateFormat("H:mm");
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		return format.format(cal.getTime());
	}
	
	public String getServerStatus()
	{
		int playerCount = 0;
		int objectCount = 0;
		final int max = LoginServerThread.getInstance().getMaxPlayer();
		playerCount = World.getAllPlayersCount();
		objectCount = World.getInstance().getAllVisibleObjectsCount();
		int itemCount = 0;
		int itemVoidCount = 0;
		int monsterCount = 0;
		int minionCount = 0;
		final int minionsGroupCount = 0;
		int npcCount = 0;
		int charCount = 0;
		int pcCount = 0;
		int detachedCount = 0;
		int doorCount = 0;
		int summonCount = 0;
		int aiCount = 0;
		for (WorldObject obj : World.getInstance().getAllVisibleObjects())
		{
			if (obj == null)
			{
				continue;
			}
			if ((obj instanceof Creature) && ((Creature) obj).hasAI())
			{
				aiCount++;
			}
			if (obj instanceof ItemInstance)
			{
				if (((ItemInstance) obj).getItemLocation() == ItemInstance.ItemLocation.VOID)
				{
					itemVoidCount++;
				}
				else
				{
					itemCount++;
				}
			}
			else if (obj instanceof MonsterInstance)
			{
				monsterCount++;
				if (((MonsterInstance) obj).hasMinions())
				{
					minionCount += ((MonsterInstance) obj).getSpawnedMinions().size(); /* .countSpawnedMinions(); */
					// minionsGroupCount += ((MonsterInstance) obj).getMinionList().lazyCountSpawnedMinionsGroups();
				}
			}
			else if (obj instanceof NpcInstance)
			{
				npcCount++;
			}
			else if (obj instanceof PlayerInstance)
			{
				pcCount++;
				if ((((PlayerInstance) obj).getClient() != null) && ((PlayerInstance) obj).getClient().isDetached())
				{
					detachedCount++;
				}
			}
			else if (obj instanceof Summon)
			{
				summonCount++;
			}
			else if (obj instanceof DoorInstance)
			{
				doorCount++;
			}
			else if (obj instanceof Creature)
			{
				charCount++;
			}
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("Server Status: ");
		sb.append("\r\n  --->  Player Count: " + playerCount + "/" + max);
		sb.append("\r\n  ---> Offline Count: " + detachedCount + "/" + playerCount);
		sb.append("\r\n  +-->  Object Count: " + objectCount);
		sb.append("\r\n  +-->      AI Count: " + aiCount);
		sb.append("\r\n  +.... Item(Void): " + itemVoidCount);
		sb.append("\r\n  +.......... Item: " + itemCount);
		sb.append("\r\n  +....... Monster: " + monsterCount);
		sb.append("\r\n  +......... Minions: " + minionCount);
		sb.append("\r\n  +.. Minions Groups: " + minionsGroupCount);
		sb.append("\r\n  +........... Npc: " + npcCount);
		sb.append("\r\n  +............ Player: " + pcCount);
		sb.append("\r\n  +........ Summon: " + summonCount);
		sb.append("\r\n  +.......... Door: " + doorCount);
		sb.append("\r\n  +.......... Creature: " + charCount);
		sb.append("\r\n  --->   Ingame Time: " + gameTime());
		sb.append("\r\n  ---> Server Uptime: " + getUptime(_uptime));
		sb.append("\r\n  --->      GM Count: " + getOnlineGMs());
		sb.append("\r\n  --->       Threads: " + Thread.activeCount());
		sb.append("\r\n  RAM Used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576)); // 1024 * 1024 = 1048576
		sb.append("\r\n");
		
		return sb.toString();
	}
	
	public void debugAll()
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		final StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(cal.getTime()));
		sb.append("\n\n");
		sb.append(getServerStatus());
		sb.append("\n\n");
		sb.append("\n## Java Platform Information ##");
		sb.append("\nJava Runtime Name: " + System.getProperty("java.runtime.name"));
		sb.append("\nJava Version: " + System.getProperty("java.version"));
		sb.append("\nJava Class Version: " + System.getProperty("java.class.version"));
		sb.append('\n');
		sb.append("\n## Virtual Machine Information ##");
		sb.append("\nVM Name: " + System.getProperty("java.vm.name"));
		sb.append("\nVM Version: " + System.getProperty("java.vm.version"));
		sb.append("\nVM Vendor: " + System.getProperty("java.vm.vendor"));
		sb.append("\nVM Info: " + System.getProperty("java.vm.info"));
		sb.append('\n');
		sb.append("\n## OS Information ##");
		sb.append("\nName: " + System.getProperty("os.name"));
		sb.append("\nArchiteture: " + System.getProperty("os.arch"));
		sb.append("\nVersion: " + System.getProperty("os.version"));
		sb.append('\n');
		sb.append("\n## Runtime Information ##");
		sb.append("\nCPU Count: " + Runtime.getRuntime().availableProcessors());
		sb.append("\nCurrent Free Heap Size: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " mb");
		sb.append("\nCurrent Heap Size: " + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + " mb");
		sb.append("\nMaximum Heap Size: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " mb");
		sb.append('\n');
		sb.append("\n## Class Path Information ##\n");
		final String cp = System.getProperty("java.class.path");
		final String[] libs = cp.split(File.pathSeparator);
		for (String lib : libs)
		{
			sb.append(lib);
			sb.append('\n');
		}
		
		sb.append('\n');
		
		checkForDeadlocks(sb);
		
		sb.append("\n\n## Thread Pool Manager Statistics ##\n");
		for (String line : ThreadPool.getStats())
		{
			sb.append(line);
			sb.append('\n');
		}
		
		int i = 0;
		File f = new File("./log/Debug-" + i + ".txt");
		while (f.exists())
		{
			i++;
			f = new File("./log/Debug-" + i + ".txt");
		}
		f.getParentFile().mkdirs();
		
		FileOutputStream fos = null;
		OutputStreamWriter out = null;
		try
		{
			fos = new FileOutputStream(f);
			out = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
			out.write(sb.toString());
			out.flush();
		}
		catch (FileNotFoundException e3)
		{
			LOGGER.warning(e3.toString());
		}
		catch (IOException e)
		{
			LOGGER.warning(e.toString());
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (Exception e)
				{
					LOGGER.warning(e.toString());
				}
			}
			
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (Exception e)
				{
					LOGGER.warning(e.toString());
				}
			}
		}
		
		_print.println("Debug output saved to log/" + f.getName());
		_print.flush();
	}
	
	private void checkForDeadlocks(StringBuilder sb)
	{
		final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		final long[] ids = findDeadlockedThreads(mbean);
		if ((ids != null) && (ids.length > 0))
		{
			final Thread[] threads = new Thread[ids.length];
			for (int i = 0; i < threads.length; i++)
			{
				threads[i] = findMatchingThread(mbean.getThreadInfo(ids[i]));
			}
			sb.append("Deadlocked Threads:\n");
			sb.append("-------------------\n");
			for (Thread thread : threads)
			{
				LOGGER.warning(thread.getName());
				for (StackTraceElement ste : thread.getStackTrace())
				{
					sb.append("\t" + ste);
					sb.append('\n');
				}
			}
		}
	}
	
	private long[] findDeadlockedThreads(ThreadMXBean mbean)
	{
		// JDK 1.5 only supports the findMonitorDeadlockedThreads()
		// method, so you need to comment out the following three lines
		if (mbean.isSynchronizerUsageSupported())
		{
			return mbean.findDeadlockedThreads();
		}
		return mbean.findMonitorDeadlockedThreads();
	}
	
	private Thread findMatchingThread(ThreadInfo inf)
	{
		for (Thread thread : Thread.getAllStackTraces().keySet())
		{
			if (thread.getId() == inf.getThreadId())
			{
				return thread;
			}
		}
		throw new IllegalStateException("Deadlocked Thread not found");
	}
}
