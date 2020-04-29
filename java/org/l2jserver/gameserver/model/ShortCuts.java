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
package org.l2jserver.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.EtcItemType;
import org.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jserver.gameserver.network.serverpackets.ShortCutInit;

/**
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:33 $
 */
public class ShortCuts
{
	private static final Logger LOGGER = Logger.getLogger(ShortCuts.class.getName());
	
	private final PlayerInstance _owner;
	private final Map<Integer, ShortCut> _shortCuts = new TreeMap<>();
	
	public ShortCuts(PlayerInstance owner)
	{
		_owner = owner;
	}
	
	public ShortCut[] getAllShortCuts()
	{
		return _shortCuts.values().toArray(new ShortCut[_shortCuts.values().size()]);
	}
	
	public ShortCut getShortCut(int slot, int page)
	{
		final ShortCut sc = _shortCuts.get(slot + (page * 12));
		// verify shortcut
		if ((sc != null) && (sc.getType() == ShortCut.TYPE_ITEM) && (_owner.getInventory().getItemByObjectId(sc.getId()) == null))
		{
			deleteShortCut(sc.getSlot(), sc.getPage());
		}
		return sc;
	}
	
	public synchronized void registerShortCut(ShortCut shortcut)
	{
		final ShortCut oldShortCut = _shortCuts.put(shortcut.getSlot() + (12 * shortcut.getPage()), shortcut);
		registerShortCutInDb(shortcut, oldShortCut);
	}
	
	private void registerShortCutInDb(ShortCut shortcut, ShortCut oldShortCut)
	{
		if (oldShortCut != null)
		{
			deleteShortCutFromDb(oldShortCut);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not store character shortcut: " + e);
		}
	}
	
	/**
	 * @param slot
	 * @param page
	 */
	public synchronized void deleteShortCut(int slot, int page)
	{
		final ShortCut old = _shortCuts.remove(slot + (page * 12));
		
		if ((old == null) || (_owner == null))
		{
			return;
		}
		
		deleteShortCutFromDb(old);
		
		if (old.getType() == ShortCut.TYPE_ITEM)
		{
			final ItemInstance item = _owner.getInventory().getItemByObjectId(old.getId());
			
			if ((item != null) && (item.getItemType() == EtcItemType.SHOT))
			{
				_owner.removeAutoSoulShot(item.getItemId());
				_owner.sendPacket(new ExAutoSoulShot(item.getItemId(), 0));
			}
		}
		
		_owner.sendPacket(new ShortCutInit(_owner));
		
		for (int shotId : _owner.getAutoSoulShot().values())
		{
			_owner.sendPacket(new ExAutoSoulShot(shotId, 1));
		}
	}
	
	public synchronized void deleteShortCutByObjectId(int objectId)
	{
		ShortCut toRemove = null;
		
		for (ShortCut shortcut : _shortCuts.values())
		{
			if ((shortcut.getType() == ShortCut.TYPE_ITEM) && (shortcut.getId() == objectId))
			{
				toRemove = shortcut;
				break;
			}
		}
		
		if (toRemove != null)
		{
			deleteShortCut(toRemove.getSlot(), toRemove.getPage());
		}
	}
	
	/**
	 * @param shortcut
	 */
	private void deleteShortCutFromDb(ShortCut shortcut)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not delete character shortcut: " + e);
		}
	}
	
	public void restore()
	{
		_shortCuts.clear();
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, _owner.getClassIndex());
			
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int slot = rset.getInt("slot");
				final int page = rset.getInt("page");
				final int type = rset.getInt("type");
				final int id = rset.getInt("shortcut_id");
				final int level = rset.getInt("level");
				
				final ShortCut sc = new ShortCut(slot, page, type, id, level);
				_shortCuts.put(slot + (page * 12), sc);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore character shortcuts: " + e);
		}
		
		// verify shortcuts
		for (ShortCut sc : getAllShortCuts())
		{
			if ((sc.getType() == ShortCut.TYPE_ITEM) && (_owner.getInventory().getItemByObjectId(sc.getId()) == null))
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
	}
}
