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
import java.sql.SQLException;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;

public class CharNameTable
{
	private static final Logger LOGGER = Logger.getLogger(CharNameTable.class.getName());
	
	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = true;
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, name);
			final ResultSet rset = statement.executeQuery();
			result = rset.next();
			
			statement.close();
			rset.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Could not check existing charname " + e);
		}
		return result;
	}
	
	public String getPlayerName(int objId)
	{
		String name = "";
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
			statement.setInt(1, objId);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				name = rset.getString(1);
			}
			
			statement.close();
			rset.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Could not check existing player name " + e);
		}
		
		return name;
	}
	
	public int getPlayerObjectId(String name)
	{
		int id = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, name);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				id = rset.getInt(1);
			}
			
			statement.close();
			rset.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Could not check existing player id " + e);
		}
		
		return id;
	}
	
	public int getPlayerAccessLevel(int objId)
	{
		int accessLevel = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT accesslevel FROM characters WHERE obj_Id=?");
			statement.setInt(1, objId);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				accessLevel = rset.getInt(1);
			}
			
			statement.close();
			rset.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Could not check existing player id " + e);
		}
		
		return accessLevel;
	}
	
	public int accountCharNumber(String account)
	{
		int number = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				number = rset.getInt(1);
			}
			
			statement.close();
			rset.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Could not check existing char number " + e);
		}
		
		return number;
	}
	
	public static CharNameTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CharNameTable INSTANCE = new CharNameTable();
	}
}
