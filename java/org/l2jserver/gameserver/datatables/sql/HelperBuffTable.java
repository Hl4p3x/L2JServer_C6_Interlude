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
package org.l2jserver.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.holders.HelperBuffHolder;

/**
 * This class represents the Newbie Helper Buff list. Author: Ayor
 */
public class HelperBuffTable
{
	private static final Logger LOGGER = Logger.getLogger(HelperBuffTable.class.getName());
	
	public List<HelperBuffHolder> helperBuff = new ArrayList<>();
	private final boolean _initialized = true;
	private int _magicClassLowestLevel = 100;
	private int _physicClassLowestLevel = 100;
	private int _magicClassHighestLevel = 1;
	private int _physicClassHighestLevel = 1;
	
	public void load()
	{
		helperBuff.clear();
		restoreHelperBuffData();
	}
	
	/**
	 * Create and Load the Newbie Helper Buff list from SQL Table helper_buff_list
	 */
	private HelperBuffTable()
	{
		load();
	}
	
	/**
	 * Read and Load the Newbie Helper Buff list from SQL Table helper_buff_list
	 */
	private void restoreHelperBuffData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM helper_buff_list");
			final ResultSet helperbuffdata = statement.executeQuery();
			fillHelperBuffTable(helperbuffdata);
			helperbuffdata.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Table helper_buff_list not found: Update your database " + e);
		}
	}
	
	/**
	 * Load the Newbie Helper Buff list from SQL Table helper_buff_list
	 * @param helperBuffData
	 * @throws Exception
	 */
	private void fillHelperBuffTable(ResultSet helperBuffData) throws Exception
	{
		while (helperBuffData.next())
		{
			final StatSet helperBuffDat = new StatSet();
			final int id = helperBuffData.getInt("id");
			helperBuffDat.set("id", id);
			helperBuffDat.set("skillID", helperBuffData.getInt("skill_id"));
			helperBuffDat.set("skillLevel", helperBuffData.getInt("skill_level"));
			helperBuffDat.set("lowerLevel", helperBuffData.getInt("lower_level"));
			helperBuffDat.set("upperLevel", helperBuffData.getInt("upper_level"));
			helperBuffDat.set("isMagicClass", helperBuffData.getString("is_magic_class"));
			
			// Calulate the range level in wich player must be to obtain buff from Newbie Helper
			if ("false".equals(helperBuffData.getString("is_magic_class")))
			{
				if (helperBuffData.getInt("lower_level") < _physicClassLowestLevel)
				{
					_physicClassLowestLevel = helperBuffData.getInt("lower_level");
				}
				
				if (helperBuffData.getInt("upper_level") > _physicClassHighestLevel)
				{
					_physicClassHighestLevel = helperBuffData.getInt("upper_level");
				}
			}
			else
			{
				if (helperBuffData.getInt("lower_level") < _magicClassLowestLevel)
				{
					_magicClassLowestLevel = helperBuffData.getInt("lower_level");
				}
				
				if (helperBuffData.getInt("upper_level") > _magicClassHighestLevel)
				{
					_magicClassHighestLevel = helperBuffData.getInt("upper_level");
				}
			}
			
			// Add this Helper Buff to the Helper Buff List
			final HelperBuffHolder template = new HelperBuffHolder(helperBuffDat);
			helperBuff.add(template);
		}
		
		LOGGER.info("HelperBuffTable: Loaded " + helperBuff.size() + " templates.");
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	public HelperBuffHolder getHelperBuffTableItem(int id)
	{
		return helperBuff.get(id);
	}
	
	/**
	 * @return the Helper Buff List
	 */
	public List<HelperBuffHolder> getHelperBuffTable()
	{
		return helperBuff;
	}
	
	/**
	 * @return Returns the magicClassHighestLevel.
	 */
	public int getMagicClassHighestLevel()
	{
		return _magicClassHighestLevel;
	}
	
	/**
	 * @param magicClassHighestLevel The magicClassHighestLevel to set.
	 */
	public void setMagicClassHighestLevel(int magicClassHighestLevel)
	{
		_magicClassHighestLevel = magicClassHighestLevel;
	}
	
	/**
	 * @return Returns the magicClassLowestLevel.
	 */
	public int getMagicClassLowestLevel()
	{
		return _magicClassLowestLevel;
	}
	
	/**
	 * @param magicClassLowestLevel The magicClassLowestLevel to set.
	 */
	public void setMagicClassLowestLevel(int magicClassLowestLevel)
	{
		_magicClassLowestLevel = magicClassLowestLevel;
	}
	
	/**
	 * @return Returns the physicClassHighestLevel.
	 */
	public int getPhysicClassHighestLevel()
	{
		return _physicClassHighestLevel;
	}
	
	/**
	 * @param physicClassHighestLevel The physicClassHighestLevel to set.
	 */
	public void setPhysicClassHighestLevel(int physicClassHighestLevel)
	{
		_physicClassHighestLevel = physicClassHighestLevel;
	}
	
	/**
	 * @return Returns the physicClassLowestLevel.
	 */
	public int getPhysicClassLowestLevel()
	{
		return _physicClassLowestLevel;
	}
	
	/**
	 * @param physicClassLowestLevel The physicClassLowestLevel to set.
	 */
	public void setPhysicClassLowestLevel(int physicClassLowestLevel)
	{
		_physicClassLowestLevel = physicClassLowestLevel;
	}
	
	public static HelperBuffTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HelperBuffTable INSTANCE = new HelperBuffTable();
	}
}
