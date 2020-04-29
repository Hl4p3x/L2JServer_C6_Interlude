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
package org.l2jserver.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.cache.CrestCache;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;

public class RequestSetAllyCrest extends GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestSetAllyCrest.class.getName());
	
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if ((_length < 0) || (_length > 192))
		{
			return;
		}
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_length < 0)
		{
			player.sendMessage("File transfer error.");
			return;
		}
		
		if (_length > 192)
		{
			player.sendMessage("The crest file size was too big (max 192 bytes).");
			return;
		}
		
		if (player.getAllyId() != 0)
		{
			final Clan leaderclan = ClanTable.getInstance().getClan(player.getAllyId());
			if ((player.getClanId() != leaderclan.getClanId()) || !player.isClanLeader())
			{
				return;
			}
			
			final CrestCache crestCache = CrestCache.getInstance();
			final int newId = IdFactory.getNextId();
			if (!crestCache.saveAllyCrest(newId, _data))
			{
				LOGGER.warning("Error loading crest of ally:" + leaderclan.getAllyName());
				return;
			}
			
			if (leaderclan.getAllyCrestId() != 0)
			{
				crestCache.removeAllyCrest(leaderclan.getAllyCrestId());
			}
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?");
				statement.setInt(1, newId);
				statement.setInt(2, leaderclan.getAllyId());
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				LOGGER.warning("could not update the ally crest id:" + e.getMessage());
			}
			
			for (Clan clan : ClanTable.getInstance().getClans())
			{
				if (clan.getAllyId() == player.getAllyId())
				{
					clan.setAllyCrestId(newId);
					for (PlayerInstance member : clan.getOnlineMembers())
					{
						member.broadcastUserInfo();
					}
				}
			}
		}
	}
}
