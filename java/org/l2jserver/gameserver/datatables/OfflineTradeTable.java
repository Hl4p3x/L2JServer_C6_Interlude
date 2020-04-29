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
package org.l2jserver.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.LoginServerThread;
import org.l2jserver.gameserver.model.ManufactureItem;
import org.l2jserver.gameserver.model.ManufactureList;
import org.l2jserver.gameserver.model.TradeList.TradeItem;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.GameClient;
import org.l2jserver.gameserver.network.GameClient.GameClientState;

/**
 * @author Shyla
 */
public class OfflineTradeTable
{
	private static final Logger LOGGER = Logger.getLogger(OfflineTradeTable.class.getName());
	
	// SQL DEFINITIONS
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	private static final String SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`,`enchant`) VALUES (?,?,?,?,?)";
	private static final String DELETE_OFFLINE_TABLE_ALL_ITEMS = "delete from character_offline_trade_items where charId=?";
	private static final String DELETE_OFFLINE_TRADER = "DELETE FROM character_offline_trade where charId=?";
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items";
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade";
	private static final String LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE charId = ?";
	
	// called when server will go off, different from storeOffliner because
	// of store of normal sellers/buyers also if not in offline mode
	public static void storeOffliners()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement stm = con.prepareStatement(CLEAR_OFFLINE_TABLE);
			stm.execute();
			stm.close();
			stm = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS);
			stm.execute();
			stm.close();
			
			con.setAutoCommit(false); // avoid halfway done
			stm = con.prepareStatement(SAVE_OFFLINE_STATUS);
			final PreparedStatement stmItems = con.prepareStatement(SAVE_ITEMS);
			for (PlayerInstance pc : World.getInstance().getAllPlayers())
			{
				try
				{
					// without second check, server will store all guys that are in shop mode
					if ((pc.getPrivateStoreType() != PlayerInstance.STORE_PRIVATE_NONE)/* && (pc.isOffline()) */)
					{
						stm.setInt(1, pc.getObjectId()); // Char Id
						stm.setLong(2, pc.getOfflineStartTime());
						stm.setInt(3, pc.getPrivateStoreType()); // store type
						String title = null;
						
						switch (pc.getPrivateStoreType())
						{
							case PlayerInstance.STORE_PRIVATE_BUY:
							{
								if (!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getBuyList().getTitle();
								for (TradeItem i : pc.getBuyList().getItems())
								{
									stmItems.setInt(1, pc.getObjectId());
									stmItems.setInt(2, i.getItem().getItemId());
									stmItems.setLong(3, i.getCount());
									stmItems.setLong(4, i.getPrice());
									stmItems.setLong(5, i.getEnchant());
									stmItems.executeUpdate();
									stmItems.clearParameters();
								}
								break;
							}
							case PlayerInstance.STORE_PRIVATE_SELL:
							case PlayerInstance.STORE_PRIVATE_PACKAGE_SELL:
							{
								if (!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getSellList().getTitle();
								pc.getSellList().updateItems();
								for (TradeItem i : pc.getSellList().getItems())
								{
									stmItems.setInt(1, pc.getObjectId());
									stmItems.setInt(2, i.getObjectId());
									stmItems.setLong(3, i.getCount());
									stmItems.setLong(4, i.getPrice());
									stmItems.setLong(5, i.getEnchant());
									stmItems.executeUpdate();
									stmItems.clearParameters();
								}
								break;
							}
							case PlayerInstance.STORE_PRIVATE_MANUFACTURE:
							{
								if (!Config.OFFLINE_CRAFT_ENABLE)
								{
									continue;
								}
								title = pc.getCreateList().getStoreName();
								for (ManufactureItem i : pc.getCreateList().getList())
								{
									stmItems.setInt(1, pc.getObjectId());
									stmItems.setInt(2, i.getRecipeId());
									stmItems.setLong(3, 0);
									stmItems.setLong(4, i.getCost());
									stmItems.setLong(5, 0);
									stmItems.executeUpdate();
									stmItems.clearParameters();
								}
								break;
							}
							default:
							{
								// LOGGER.info( "OfflineTradersTable[storeTradeItems()]: Error while saving offline trader: " + pc.getObjectId() + ", store type: "+pc.getPrivateStoreType());
								// no save for this kind of shop
								continue;
							}
						}
						stm.setString(4, title);
						stm.executeUpdate();
						stm.clearParameters();
						con.commit(); // flush
					}
				}
				catch (Exception e)
				{
					LOGGER.warning("OfflineTradersTable[storeTradeItems()]: Error while saving offline trader: " + pc.getObjectId() + " " + e);
				}
			}
			stm.close();
			stmItems.close();
			LOGGER.info("Offline traders stored.");
		}
		catch (Exception e)
		{
			LOGGER.warning("OfflineTradersTable[storeTradeItems()]: Error while saving offline traders: " + e);
		}
	}
	
	public static void restoreOfflineTraders()
	{
		LOGGER.info("Loading offline traders...");
		int nTraders = 0;
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement stm = con.prepareStatement(LOAD_OFFLINE_STATUS);
			final ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				final long time = rs.getLong("time");
				if (Config.OFFLINE_MAX_DAYS > 0)
				{
					final Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS);
					if (cal.getTimeInMillis() <= System.currentTimeMillis())
					{
						LOGGER.info("Offline trader with id " + rs.getInt("charId") + " reached OfflineMaxDays, kicked.");
						continue;
					}
				}
				
				final int type = rs.getInt("type");
				if (type == PlayerInstance.STORE_PRIVATE_NONE)
				{
					continue;
				}
				
				PlayerInstance player = null;
				try
				{
					final GameClient client = new GameClient(null);
					player = PlayerInstance.load(rs.getInt("charId"));
					client.setPlayer(player);
					client.setAccountName(player.getAccountName());
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.setOfflineMode(true);
					player.setOnlineStatus(false);
					player.setOfflineStartTime(time);
					if (Config.OFFLINE_SLEEP_EFFECT)
					{
						player.startAbnormalEffect(Creature.ABNORMAL_EFFECT_SLEEP);
					}
					player.spawnMe(player.getX(), player.getY(), player.getZ());
					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
					final PreparedStatement stmItems = con.prepareStatement(LOAD_OFFLINE_ITEMS);
					stmItems.setInt(1, player.getObjectId());
					final ResultSet items = stmItems.executeQuery();
					
					switch (type)
					{
						case PlayerInstance.STORE_PRIVATE_BUY:
						{
							while (items.next())
							{
								player.getBuyList().addItemByItemId(items.getInt(2), items.getInt(3), items.getInt(4), items.getInt(5));
							}
							player.getBuyList().setTitle(rs.getString("title"));
							break;
						}
						case PlayerInstance.STORE_PRIVATE_SELL:
						case PlayerInstance.STORE_PRIVATE_PACKAGE_SELL:
						{
							while (items.next())
							{
								player.getSellList().addItem(items.getInt(2), items.getInt(3), items.getInt(4));
							}
							player.getSellList().setTitle(rs.getString("title"));
							player.getSellList().setPackaged(type == PlayerInstance.STORE_PRIVATE_PACKAGE_SELL);
							break;
						}
						case PlayerInstance.STORE_PRIVATE_MANUFACTURE:
						{
							final ManufactureList createList = new ManufactureList();
							while (items.next())
							{
								createList.add(new ManufactureItem(items.getInt(2), items.getInt(4)));
							}
							player.setCreateList(createList);
							player.getCreateList().setStoreName(rs.getString("title"));
							break;
						}
						default:
						{
							LOGGER.info("Offline trader " + player.getName() + " finished to sell his items");
						}
					}
					items.close();
					stmItems.close();
					
					player.sitDown();
					if (Config.OFFLINE_MODE_SET_INVULNERABLE)
					{
						player.setInvul(true);
					}
					if (Config.OFFLINE_SET_NAME_COLOR)
					{
						player._originalNameColorOffline = player.getAppearance().getNameColor();
						player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
					}
					player.setPrivateStoreType(type);
					player.setOnlineStatus(true);
					player.restoreEffects();
					player.broadcastUserInfo();
					nTraders++;
				}
				catch (Exception e)
				{
					LOGGER.warning("OfflineTradersTable[loadOffliners()]: Error loading trader: " + e);
					if (player != null)
					{
						player.logout();
					}
				}
			}
			rs.close();
			stm.close();
			World.OFFLINE_TRADE_COUNT = nTraders;
			LOGGER.info("Loaded " + nTraders + " offline traders.");
		}
		catch (Exception e)
		{
			LOGGER.warning("OfflineTradersTable[loadOffliners()]: Error while loading offline traders: " + e);
		}
	}
	
	public static void storeOffliner(PlayerInstance pc)
	{
		if ((pc.getPrivateStoreType() == PlayerInstance.STORE_PRIVATE_NONE) || (!pc.isInOfflineMode()))
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement stm = con.prepareStatement(DELETE_OFFLINE_TABLE_ALL_ITEMS);
			stm.setInt(1, pc.getObjectId());
			stm.execute();
			stm.clearParameters();
			stm.close();
			stm = con.prepareStatement(DELETE_OFFLINE_TRADER);
			stm.setInt(1, pc.getObjectId());
			stm.execute();
			stm.clearParameters();
			stm.close();
			
			con.setAutoCommit(false); // avoid halfway done
			stm = con.prepareStatement(SAVE_OFFLINE_STATUS);
			final PreparedStatement stmItems = con.prepareStatement(SAVE_ITEMS);
			boolean save = true;
			
			try
			{
				stm.setInt(1, pc.getObjectId()); // Char Id
				stm.setLong(2, pc.getOfflineStartTime());
				stm.setInt(3, pc.getPrivateStoreType()); // store type
				String title = null;
				
				switch (pc.getPrivateStoreType())
				{
					case PlayerInstance.STORE_PRIVATE_BUY:
					{
						if (!Config.OFFLINE_TRADE_ENABLE)
						{
							break;
						}
						title = pc.getBuyList().getTitle();
						for (TradeItem i : pc.getBuyList().getItems())
						{
							stmItems.setInt(1, pc.getObjectId());
							stmItems.setInt(2, i.getItem().getItemId());
							stmItems.setLong(3, i.getCount());
							stmItems.setLong(4, i.getPrice());
							stmItems.setLong(5, i.getEnchant());
							stmItems.executeUpdate();
							stmItems.clearParameters();
						}
						break;
					}
					case PlayerInstance.STORE_PRIVATE_SELL:
					case PlayerInstance.STORE_PRIVATE_PACKAGE_SELL:
					{
						if (!Config.OFFLINE_TRADE_ENABLE)
						{
							break;
						}
						title = pc.getSellList().getTitle();
						pc.getSellList().updateItems();
						for (TradeItem i : pc.getSellList().getItems())
						{
							stmItems.setInt(1, pc.getObjectId());
							stmItems.setInt(2, i.getObjectId());
							stmItems.setLong(3, i.getCount());
							stmItems.setLong(4, i.getPrice());
							stmItems.setLong(5, i.getEnchant());
							stmItems.executeUpdate();
							stmItems.clearParameters();
						}
						break;
					}
					case PlayerInstance.STORE_PRIVATE_MANUFACTURE:
					{
						if (!Config.OFFLINE_CRAFT_ENABLE)
						{
							break;
						}
						title = pc.getCreateList().getStoreName();
						for (ManufactureItem i : pc.getCreateList().getList())
						{
							stmItems.setInt(1, pc.getObjectId());
							stmItems.setInt(2, i.getRecipeId());
							stmItems.setLong(3, 0);
							stmItems.setLong(4, i.getCost());
							stmItems.setLong(5, 0);
							stmItems.executeUpdate();
							stmItems.clearParameters();
						}
						break;
					}
					default:
					{
						// LOGGER.info( "OfflineTradersTable[storeOffliner()]: Error while saving offline trader: " + pc.getObjectId() + ", store type: "+pc.getPrivateStoreType());
						// no save for this kind of shop
						save = false;
					}
				}
				
				if (save)
				{
					stm.setString(4, title);
					stm.executeUpdate();
					stm.clearParameters();
					con.commit(); // flush
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("OfflineTradersTable[storeOffliner()]: Error while saving offline trader: " + pc.getObjectId() + " " + e);
			}
			
			stm.close();
			stmItems.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("OfflineTradersTable[storeOffliner()]: Error while saving offline traders: " + e);
		}
	}
}
