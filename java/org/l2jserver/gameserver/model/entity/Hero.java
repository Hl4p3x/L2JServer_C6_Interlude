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
package org.l2jserver.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * @author godson
 */
public class Hero
{
	private static final Logger LOGGER = Logger.getLogger(Hero.class.getName());
	
	private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
	private static final String GET_ALL_HEROES = "SELECT * FROM heroes";
	private static final String UPDATE_ALL = "UPDATE heroes SET played = 0";
	private static final String INSERT_HERO = "INSERT INTO heroes VALUES (?,?,?,?,?)";
	private static final String UPDATE_HERO = "UPDATE heroes SET count = ?, played = ? WHERE charId = ?";
	private static final String GET_CLAN_ALLY = "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid  WHERE characters.obj_Id = ?";
	private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)";
	private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) AND owner_id NOT IN (SELECT obj_id FROM characters WHERE accesslevel > 0)";
	private static final List<Integer> _heroItems = Arrays.asList(6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621);
	private static Map<Integer, StatSet> _heroes;
	private static Map<Integer, StatSet> _completeHeroes;
	
	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";
	
	public Hero()
	{
		init();
	}
	
	private void init()
	{
		_heroes = new HashMap<>();
		_completeHeroes = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = null;
			PreparedStatement statement2 = null;
			ResultSet rset = null;
			ResultSet rset2 = null;
			statement = con.prepareStatement(GET_HEROES);
			rset = statement.executeQuery();
			while (rset.next())
			{
				final StatSet hero = new StatSet();
				final int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
				hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				statement2 = con.prepareStatement(GET_CLAN_ALLY);
				statement2.setInt(1, charId);
				rset2 = statement2.executeQuery();
				if (rset2.next())
				{
					final int clanId = rset2.getInt("clanid");
					final int allyId = rset2.getInt("allyId");
					String clanName = "";
					String allyName = "";
					int clanCrest = 0;
					int allyCrest = 0;
					if (clanId > 0)
					{
						clanName = ClanTable.getInstance().getClan(clanId).getName();
						clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
						if (allyId > 0)
						{
							allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
							allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
						}
					}
					hero.set(CLAN_CREST, clanCrest);
					hero.set(CLAN_NAME, clanName);
					hero.set(ALLY_CREST, allyCrest);
					hero.set(ALLY_NAME, allyName);
				}
				rset2.close();
				statement2.close();
				_heroes.put(charId, hero);
			}
			rset.close();
			statement.close();
			statement = con.prepareStatement(GET_ALL_HEROES);
			rset = statement.executeQuery();
			while (rset.next())
			{
				final StatSet hero = new StatSet();
				final int charId = rset.getInt(Olympiad.CHAR_ID);
				final String charName = rset.getString(Olympiad.CHAR_NAME);
				hero.set(Olympiad.CHAR_NAME, charName);
				hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				statement2 = con.prepareStatement(GET_CLAN_ALLY);
				statement2.setInt(1, charId);
				rset2 = statement2.executeQuery();
				if (rset2.next())
				{
					final int clanId = rset2.getInt("clanid");
					final int allyId = rset2.getInt("allyId");
					String clanName = "";
					String allyName = "";
					int clanCrest = 0;
					int allyCrest = 0;
					if (clanId > 0)
					{
						final Clan clan = ClanTable.getInstance().getClan(clanId);
						if (clan != null)
						{
							clanName = clan.getName();
							clanCrest = clan.getCrestId();
							if (allyId > 0)
							{
								allyName = clan.getAllyName();
								allyCrest = clan.getAllyCrestId();
							}
						}
						else
						{
							LOGGER.warning("Hero System: Player " + charName + " has clan id " + clanId + " that is not present inside clanTable..");
						}
					}
					hero.set(CLAN_CREST, clanCrest);
					hero.set(CLAN_NAME, clanName);
					hero.set(ALLY_CREST, allyCrest);
					hero.set(ALLY_NAME, allyName);
				}
				rset2.close();
				statement2.close();
				_completeHeroes.put(charId, hero);
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("Hero System: Couldnt load Heroes");
		}
		LOGGER.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
		LOGGER.info("Hero System: Loaded " + _completeHeroes.size() + " all time Heroes.");
	}
	
	public void putHero(PlayerInstance player, boolean isComplete)
	{
		try
		{
			final StatSet newHero = new StatSet();
			newHero.set(Olympiad.CHAR_NAME, player.getName());
			newHero.set(Olympiad.CLASS_ID, player.getClassId().getId());
			newHero.set(COUNT, 1);
			newHero.set(PLAYED, 1);
			_heroes.put(player.getObjectId(), newHero);
			if (isComplete)
			{
				_completeHeroes.put(player.getObjectId(), newHero);
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public void deleteHero(PlayerInstance player, boolean isComplete)
	{
		final int objId = player.getObjectId();
		if (_heroes.containsKey(objId))
		{
			_heroes.remove(objId);
		}
		if (isComplete && _completeHeroes.containsKey(objId))
		{
			_completeHeroes.remove(objId);
		}
	}
	
	public Map<Integer, StatSet> getHeroes()
	{
		return _heroes;
	}
	
	public synchronized void computeNewHeroes(List<StatSet> newHeroes)
	{
		updateHeroes(true);
		ItemInstance[] items;
		InventoryUpdate iu;
		if (_heroes.size() != 0)
		{
			for (StatSet hero : _heroes.values())
			{
				final String name = hero.getString(Olympiad.CHAR_NAME);
				final PlayerInstance player = World.getInstance().getPlayer(name);
				if (player == null)
				{
					continue;
				}
				try
				{
					player.setHero(false);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(Item.SLOT_LR_HAND);
					iu = new InventoryUpdate();
					for (ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(Item.SLOT_R_HAND);
					iu = new InventoryUpdate();
					for (ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(Item.SLOT_HAIR);
					iu = new InventoryUpdate();
					for (ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(Item.SLOT_FACE);
					iu = new InventoryUpdate();
					for (ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					items = player.getInventory().unEquipItemInBodySlotAndRecord(Item.SLOT_DHAIR);
					iu = new InventoryUpdate();
					for (ItemInstance item : items)
					{
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					for (ItemInstance item : player.getInventory().getAvailableItems(false))
					{
						if (item == null)
						{
							continue;
						}
						if (!_heroItems.contains(item.getItemId()))
						{
							continue;
						}
						player.destroyItem("Hero", item, null, true);
						iu = new InventoryUpdate();
						iu.addRemovedItem(item);
						player.sendPacket(iu);
					}
					player.sendPacket(new UserInfo(player));
					player.broadcastUserInfo();
				}
				catch (NullPointerException e)
				{
				}
			}
		}
		if (newHeroes.isEmpty())
		{
			_heroes.clear();
			return;
		}
		final Map<Integer, StatSet> heroes = new HashMap<>();
		for (StatSet hero : newHeroes)
		{
			final int charId = hero.getInt(Olympiad.CHAR_ID);
			if ((_completeHeroes != null) && _completeHeroes.containsKey(charId))
			{
				final StatSet oldHero = _completeHeroes.get(charId);
				final int count = oldHero.getInt(COUNT);
				oldHero.set(COUNT, count + 1);
				oldHero.set(PLAYED, 1);
				heroes.put(charId, oldHero);
			}
			else
			{
				final StatSet newHero = new StatSet();
				newHero.set(Olympiad.CHAR_NAME, hero.getString(Olympiad.CHAR_NAME));
				newHero.set(Olympiad.CLASS_ID, hero.getInt(Olympiad.CLASS_ID));
				newHero.set(COUNT, 1);
				newHero.set(PLAYED, 1);
				heroes.put(charId, newHero);
			}
		}
		deleteItemsInDb();
		_heroes.clear();
		_heroes.putAll(heroes);
		heroes.clear();
		updateHeroes(false);
		for (StatSet hero : _heroes.values())
		{
			final String name = hero.getString(Olympiad.CHAR_NAME);
			final PlayerInstance player = World.getInstance().getPlayer(name);
			if (player != null)
			{
				player.setHero(true);
				final Clan clan = player.getClan();
				if (clan != null)
				{
					clan.setReputationScore(clan.getReputationScore() + 1000, true);
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
					final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_NAMED_A_HERO_2S_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE);
					sm.addString(name);
					sm.addNumber(1000);
					clan.broadcastToOnlineMembers(sm);
				}
				player.sendPacket(new UserInfo(player));
				player.broadcastUserInfo();
			}
			else
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					PreparedStatement statement = null;
					ResultSet rset = null;
					statement = con.prepareStatement(GET_CLAN_NAME);
					statement.setString(1, name);
					rset = statement.executeQuery();
					if (rset.next())
					{
						final String clanName = rset.getString("clan_name");
						if (clanName != null)
						{
							final Clan clan = ClanTable.getInstance().getClanByName(clanName);
							if (clan != null)
							{
								clan.setReputationScore(clan.getReputationScore() + 1000, true);
								clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
								final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_NAMED_A_HERO_2S_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE);
								sm.addString(name);
								sm.addNumber(1000);
								clan.broadcastToOnlineMembers(sm);
							}
						}
					}
					rset.close();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("could not get clan name of " + name + ": " + e);
				}
			}
		}
	}
	
	public void updateHeroes(boolean setDefault)
	{
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset2 = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (setDefault)
			{
				statement = con.prepareStatement(UPDATE_ALL);
				statement.execute();
				statement.close();
			}
			else
			{
				for (Entry<Integer, StatSet> entry : _heroes.entrySet())
				{
					final Integer heroId = entry.getKey();
					final StatSet hero = entry.getValue();
					if ((_completeHeroes == null) || !_completeHeroes.containsKey(heroId))
					{
						statement = con.prepareStatement(INSERT_HERO);
						statement.setInt(1, heroId);
						statement.setString(2, hero.getString(Olympiad.CHAR_NAME));
						statement.setInt(3, hero.getInt(Olympiad.CLASS_ID));
						statement.setInt(4, hero.getInt(COUNT));
						statement.setInt(5, hero.getInt(PLAYED));
						statement.execute();
						statement2 = con.prepareStatement(GET_CLAN_ALLY);
						statement2.setInt(1, heroId);
						rset2 = statement2.executeQuery();
						if (rset2.next())
						{
							final int clanId = rset2.getInt("clanid");
							final int allyId = rset2.getInt("allyId");
							String clanName = "";
							String allyName = "";
							int clanCrest = 0;
							int allyCrest = 0;
							if (clanId > 0)
							{
								clanName = ClanTable.getInstance().getClan(clanId).getName();
								clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
								if (allyId > 0)
								{
									allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
									allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
								}
							}
							hero.set(CLAN_CREST, clanCrest);
							hero.set(CLAN_NAME, clanName);
							hero.set(ALLY_CREST, allyCrest);
							hero.set(ALLY_NAME, allyName);
						}
						rset2.close();
						statement2.close();
						// _heroes.remove(heroId);
						_heroes.put(heroId, hero);
						_completeHeroes.put(heroId, hero);
					}
					else
					{
						statement = con.prepareStatement(UPDATE_HERO);
						statement.setInt(1, hero.getInt(COUNT));
						statement.setInt(2, hero.getInt(PLAYED));
						statement.setInt(3, heroId);
						statement.execute();
					}
					statement.close();
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("Hero System: Couldnt update Heroes");
		}
	}
	
	public List<Integer> getHeroItems()
	{
		return _heroItems;
	}
	
	private void deleteItemsInDb()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(DELETE_ITEMS);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	public static Hero getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Hero INSTANCE = new Hero();
	}
}