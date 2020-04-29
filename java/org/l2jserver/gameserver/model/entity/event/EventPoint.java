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

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

public class EventPoint
{
	private final PlayerInstance _player;
	private Integer _points = 0;
	
	public EventPoint(PlayerInstance player)
	{
		_player = player;
		loadFromDB();
	}
	
	public PlayerInstance getActiveChar()
	{
		return _player;
	}
	
	public void savePoints()
	{
		saveToDb();
	}
	
	private void loadFromDB()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement st = con.prepareStatement("Select * From char_points where charId = ?");
			st.setInt(1, _player.getObjectId());
			final ResultSet rst = st.executeQuery();
			
			while (rst.next())
			{
				_points = rst.getInt("points");
			}
			
			rst.close();
			st.close();
		}
		catch (Exception ex)
		{
		}
	}
	
	private void saveToDb()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement st = con.prepareStatement("Update char_points Set points = ? Where charId = ?");
			st.setInt(1, _points);
			st.setInt(2, _player.getObjectId());
			st.execute();
			st.close();
		}
		catch (Exception ex)
		{
		}
	}
	
	public Integer getPoints()
	{
		return _points;
	}
	
	public void setPoints(Integer points)
	{
		_points = points;
	}
	
	public void addPoints(Integer points)
	{
		_points += points;
	}
	
	public void removePoints(Integer points)
	{
		// Don not know, do the calc or return. It's up to you.
		if ((_points - points) < 0)
		{
			return;
		}
		
		_points -= points;
	}
	
	public boolean canSpend(Integer value)
	{
		return (_points - value) >= 0;
	}
}
