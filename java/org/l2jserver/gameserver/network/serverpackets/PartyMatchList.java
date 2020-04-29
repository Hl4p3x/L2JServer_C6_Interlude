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
package org.l2jserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jserver.gameserver.model.partymatching.PartyMatchRoomList;

public class PartyMatchList extends GameServerPacket
{
	private final PlayerInstance _player;
	private final int _loc;
	private final int _lim;
	private final List<PartyMatchRoom> _rooms;
	
	public PartyMatchList(PlayerInstance player, int auto, int location, int limit)
	{
		_player = player;
		_loc = location;
		_lim = limit;
		_rooms = new ArrayList<>();
	}
	
	@Override
	protected final void writeImpl()
	{
		if (getClient().getPlayer() == null)
		{
			return;
		}
		
		for (PartyMatchRoom room : PartyMatchRoomList.getInstance().getRooms())
		{
			if ((room.getMembers() < 1) || (room.getOwner() == null) || !room.getOwner().isOnline() || (room.getOwner().getPartyRoom() != room.getId()))
			{
				PartyMatchRoomList.getInstance().deleteRoom(room.getId());
				continue;
			}
			
			if ((_loc > 0) && (_loc != room.getLocation()))
			{
				continue;
			}
			
			if ((_lim == 0) && ((_player.getLevel() < room.getMinLvl()) || (_player.getLevel() > room.getMaxLvl())))
			{
				continue;
			}
			
			_rooms.add(room);
		}
		
		int count = 0;
		final int size = _rooms.size();
		writeC(0x96);
		if (size > 0)
		{
			writeD(1);
		}
		else
		{
			writeD(0);
		}
		
		writeD(_rooms.size());
		while (size > count)
		{
			writeD(_rooms.get(count).getId());
			writeS(_rooms.get(count).getTitle());
			writeD(_rooms.get(count).getLocation());
			writeD(_rooms.get(count).getMinLvl());
			writeD(_rooms.get(count).getMaxLvl());
			writeD(_rooms.get(count).getMembers());
			writeD(_rooms.get(count).getMaxMembers());
			writeS(_rooms.get(count).getOwner().getName());
			count++;
		}
	}
}