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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.TeleportLocation;

/**
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class TeleportLocationTable
{
	private static final Logger LOGGER = Logger.getLogger(TeleportLocationTable.class.getName());
	
	private final Map<Integer, TeleportLocation> _teleports = new HashMap<>();
	
	private TeleportLocationTable()
	{
		load();
	}
	
	public void load()
	{
		_teleports.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
			final ResultSet rset = statement.executeQuery();
			TeleportLocation teleport;
			
			while (rset.next())
			{
				teleport = new TeleportLocation();
				teleport.setTeleId(rset.getInt("id"));
				teleport.setX(rset.getInt("loc_x"));
				teleport.setY(rset.getInt("loc_y"));
				teleport.setZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				teleport.setForNoble(rset.getInt("fornoble") == 1);
				_teleports.put(teleport.getTeleId(), teleport);
			}
			
			statement.close();
			rset.close();
			
			LOGGER.info("TeleportLocationTable: Loaded " + _teleports.size() + " Teleport Location Templates");
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating teleport table " + e);
		}
		
		if (Config.CUSTOM_TELEPORT_TABLE)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM custom_teleport");
				final ResultSet rset = statement.executeQuery();
				TeleportLocation teleport;
				int cTeleCount = _teleports.size();
				
				while (rset.next())
				{
					teleport = new TeleportLocation();
					teleport.setTeleId(rset.getInt("id"));
					teleport.setX(rset.getInt("loc_x"));
					teleport.setY(rset.getInt("loc_y"));
					teleport.setZ(rset.getInt("loc_z"));
					teleport.setPrice(rset.getInt("price"));
					teleport.setForNoble(rset.getInt("fornoble") == 1);
					_teleports.put(teleport.getTeleId(), teleport);
				}
				
				statement.close();
				rset.close();
				
				cTeleCount = _teleports.size() - cTeleCount;
				if (cTeleCount > 0)
				{
					LOGGER.info("TeleportLocationTable: Loaded " + cTeleCount + " Custom Teleport Location Templates.");
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Error while creating custom teleport table " + e);
			}
		}
	}
	
	public TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
	
	public static TeleportLocationTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleportLocationTable INSTANCE = new TeleportLocationTable();
	}
}
