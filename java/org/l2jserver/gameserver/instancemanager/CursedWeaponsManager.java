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
package org.l2jserver.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.CursedWeapon;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.CommanderInstance;
import org.l2jserver.gameserver.model.actor.instance.FestivalMonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.FortSiegeGuardInstance;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RiftInvaderInstance;
import org.l2jserver.gameserver.model.actor.instance.SiegeGuardInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Micht
 */
public class CursedWeaponsManager
{
	private static final Logger LOGGER = Logger.getLogger(CursedWeaponsManager.class.getName());
	
	private static final Map<Integer, CursedWeapon> _cursedWeapons = new HashMap<>();
	
	public static final CursedWeaponsManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public CursedWeaponsManager()
	{
		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			return;
		}
		
		load();
		restore();
		controlPlayers();
		
		LOGGER.info("Loaded: " + _cursedWeapons.size() + " cursed weapon(s).");
	}
	
	public void reload()
	{
		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			return;
		}
		
		_cursedWeapons.clear();
		
		load();
		restore();
		controlPlayers();
		
		LOGGER.info("Reloaded: " + _cursedWeapons.size() + " cursed weapon(s).");
	}
	
	private final void load()
	{
		LOGGER.info("Initializing CursedWeaponsManager.");
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			final File file = new File(Config.DATAPACK_ROOT + "/data/CursedWeapons.xml");
			if (!file.exists())
			{
				return;
			}
			
			final Document doc = factory.newDocumentBuilder().parse(file);
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							final int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
							final String name = attrs.getNamedItem("name").getNodeValue();
							final CursedWeapon cw = new CursedWeapon(id, skillId, name);
							int val;
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDropRate(val);
								}
								else if ("duration".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDuration(val);
								}
								else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDurationLost(val);
								}
								else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDisapearChance(val);
								}
								else if ("stageKills".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setStageKills(val);
								}
							}
							
							_cursedWeapons.put(id, cw);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error parsing cursed weapons file. " + e);
		}
	}
	
	private final void restore()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT itemId, playerId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
			final ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				final int itemId = rset.getInt("itemId");
				final int playerId = rset.getInt("playerId");
				final int playerKarma = rset.getInt("playerKarma");
				final int playerPkKills = rset.getInt("playerPkKills");
				final int nbKills = rset.getInt("nbKills");
				final long endTime = rset.getLong("endTime");
				final CursedWeapon cw = _cursedWeapons.get(itemId);
				cw.setPlayerId(playerId);
				cw.setPlayerKarma(playerKarma);
				cw.setPlayerPkKills(playerPkKills);
				cw.setNbKills(nbKills);
				cw.setEndTime(endTime);
				cw.reActivate();
				
				// clean up the cursed weapons table.
				removeFromDb(itemId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore CursedWeapons data: " + e);
		}
	}
	
	private final void controlPlayers()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = null;
			ResultSet rset = null;
			
			// TODO: See comments below...
			// This entire for loop should NOT be necessary, since it is already handled by
			// CursedWeapon.endOfLife(). However, if we indeed *need* to duplicate it for safety,
			// then we'd better make sure that it FULLY cleans up inactive cursed weapons!
			// Undesired effects result otherwise, such as player with no zariche but with karma
			// or a lost-child entry in the cursed weapons table, without a corresponding one in items...
			for (CursedWeapon cw : _cursedWeapons.values())
			{
				if (cw.isActivated())
				{
					continue;
				}
				
				// Do an item check to be sure that the cursed weapon isn't hold by someone
				final int itemId = cw.getItemId();
				try
				{
					statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
					statement.setInt(1, itemId);
					rset = statement.executeQuery();
					if (rset.next())
					{
						// A player has the cursed weapon in his inventory ...
						final int playerId = rset.getInt("owner_id");
						LOGGER.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
						
						// Delete the item
						statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
						statement.setInt(1, playerId);
						statement.setInt(2, itemId);
						if (statement.executeUpdate() != 1)
						{
							LOGGER.warning("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
						}
						statement.close();
						
						// Restore the player's old karma and pk count
						statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
						statement.setInt(1, cw.getPlayerKarma());
						statement.setInt(2, cw.getPlayerPkKills());
						statement.setInt(3, playerId);
						if (statement.executeUpdate() != 1)
						{
							LOGGER.warning("Error while updating karma & pkkills for userId " + cw.getPlayerId());
						}
					}
					rset.close();
					statement.close();
				}
				catch (SQLException sqlE)
				{
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not check CursedWeapons data: " + e.getMessage());
		}
	}
	
	public synchronized void checkDrop(Attackable attackable, PlayerInstance player)
	{
		if ((attackable instanceof SiegeGuardInstance) || (attackable instanceof RiftInvaderInstance) || (attackable instanceof FestivalMonsterInstance) || (attackable instanceof GrandBossInstance) || (attackable instanceof FortSiegeGuardInstance) || (attackable instanceof CommanderInstance))
		{
			return;
		}
		
		if (player.isCursedWeaponEquiped())
		{
			return;
		}
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActive())
			{
				continue;
			}
			
			if (cw.checkDrop(attackable, player))
			{
				break;
			}
		}
	}
	
	public void activate(PlayerInstance player, ItemInstance item)
	{
		final CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if (player.isCursedWeaponEquiped()) // cannot own 2 cursed swords
		{
			final CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquipedId());
			/*
			 * TODO: give the bonus level in a more appropriate manner. The following code adds "_stageKills" levels. This will also show in the char status. I do not have enough info to know if the bonus should be shown in the pk count, or if it should be a full "_stageKills" bonus or just the
			 * remaining from the current count till the of the current stage... This code is a TEMP fix, so that the cursed weapon's bonus level can be observed with as little change in the code as possible, until proper info arises.
			 */
			cw2.setNbKills(cw2.getStageKills() - 1);
			cw2.increaseKills();
			
			// erase the newly obtained cursed weapon
			cw.setPlayer(player); // NECESSARY in order to find which inventory the weapon is in!
			cw.endOfLife(); // expire the weapon and clean up.
		}
		else
		{
			cw.activate(player, item);
		}
	}
	
	public void drop(int itemId, Creature killer)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		cw.dropIt(killer);
	}
	
	public void increaseKills(int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		cw.increaseKills();
	}
	
	public int getLevel(int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		return cw.getLevel();
	}
	
	public static void announce(SystemMessage sm)
	{
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			player.sendPacket(sm);
		}
	}
	
	public void checkPlayer(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActive() && (player.getObjectId() == cw.getPlayerId()))
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveSkill();
				player.setCursedWeaponEquipedId(cw.getItemId());
				
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_S2_MINUTE_S_OF_USAGE_TIME_REMAINING);
				sm.addString(cw.getName());
				// sm.addItemName(cw.getItemId());
				sm.addNumber((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000));
				player.sendPacket(sm);
				
				sm = new SystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
				sm.addZoneName(player.getX(), player.getY(), player.getZ()); // Region Name
				sm.addItemName(cw.getItemId());
				announce(sm);
				
			}
		}
	}
	
	public static void removeFromDb(int itemId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, itemId);
			statement.executeUpdate();
			
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("CursedWeaponsManager: Failed to remove data. " + e);
		}
	}
	
	public void saveData()
	{
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			cw.saveData();
		}
	}
	
	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}
	
	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}
	
	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}
	
	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}
	
	public void givePassive(int itemId)
	{
		try
		{
			_cursedWeapons.get(itemId).giveSkill();
		}
		catch (Exception e)
		{
		}
	}
	
	private static class SingletonHolder
	{
		protected static final CursedWeaponsManager INSTANCE = new CursedWeaponsManager();
	}
}
