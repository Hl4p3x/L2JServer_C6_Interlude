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
import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author evill33t
 */
public class Wedding
{
	protected static final Logger LOGGER = Logger.getLogger(Wedding.class.getName());
	
	private int _Id = 0;
	private int _player1Id = 0;
	private int _player2Id = 0;
	private boolean _maried = false;
	private Calendar _affiancedDate;
	private Calendar _weddingDate;
	private int _type = 0;
	
	public Wedding(int coupleId)
	{
		_Id = coupleId;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("Select * from mods_wedding where id = ?");
			statement.setInt(1, _Id);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_player1Id = rs.getInt("player1Id");
				_player2Id = rs.getInt("player2Id");
				_maried = rs.getBoolean("married");
				_affiancedDate = Calendar.getInstance();
				_affiancedDate.setTimeInMillis(rs.getLong("affianceDate"));
				
				_weddingDate = Calendar.getInstance();
				_weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
				
				_type = rs.getInt("coupleType");
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: Couple.load(): " + e.getMessage());
		}
	}
	
	public Wedding(PlayerInstance player1, PlayerInstance player2)
	{
		final int _tempPlayer1Id = player1.getObjectId();
		final int _tempPlayer2Id = player2.getObjectId();
		_player1Id = _tempPlayer1Id;
		_player2Id = _tempPlayer2Id;
		_affiancedDate = Calendar.getInstance();
		_affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		
		_weddingDate = Calendar.getInstance();
		_weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			_Id = IdFactory.getNextId();
			statement = con.prepareStatement("INSERT INTO mods_wedding (id, player1Id, player2Id, married, affianceDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)");
			statement.setInt(1, _Id);
			statement.setInt(2, _player1Id);
			statement.setInt(3, _player2Id);
			statement.setBoolean(4, false);
			statement.setLong(5, _affiancedDate.getTimeInMillis());
			statement.setLong(6, _weddingDate.getTimeInMillis());
			statement.execute();
			statement.close();
			
			_maried = true;
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
	}
	
	public void marry(int type)
	{
		_type = type;
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE mods_wedding set married = ?, weddingDate = ?, coupleType = ? where id = ?");
			statement.setBoolean(1, true);
			_weddingDate = Calendar.getInstance();
			statement.setLong(2, _weddingDate.getTimeInMillis());
			statement.setInt(3, _type);
			statement.setInt(4, _Id);
			statement.execute();
			statement.close();
			
			_maried = true;
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
	}
	
	public void divorce()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM mods_wedding WHERE id=?");
			statement.setInt(1, _Id);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: Couple.divorce(): " + e.getMessage());
		}
	}
	
	public int getId()
	{
		return _Id;
	}
	
	public int getPlayer1Id()
	{
		return _player1Id;
	}
	
	public int getPlayer2Id()
	{
		return _player2Id;
	}
	
	public boolean getMaried()
	{
		return _maried;
	}
	
	public Calendar getAffiancedDate()
	{
		return _affiancedDate;
	}
	
	public Calendar getWeddingDate()
	{
		return _weddingDate;
	}
	
	public int getType()
	{
		return _type;
	}
}
