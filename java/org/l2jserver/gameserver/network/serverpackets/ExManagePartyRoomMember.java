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

import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.partymatching.PartyMatchRoom;

/**
 * @author Gnacik Mode : 0 - add 1 - modify 2 - quit
 */
public class ExManagePartyRoomMember extends GameServerPacket
{
	private final PlayerInstance _player;
	private final PartyMatchRoom _room;
	private final int _mode;
	
	public ExManagePartyRoomMember(PlayerInstance player, PartyMatchRoom room, int mode)
	{
		_player = player;
		_room = room;
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x10);
		writeD(_mode);
		writeD(_player.getObjectId());
		writeS(_player.getName());
		writeD(_player.getActiveClass());
		writeD(_player.getLevel());
		writeD(MapRegionData.getInstance().getClosestLocation(_player.getX(), _player.getY()));
		if (_room.getOwner().equals(_player))
		{
			writeD(1);
		}
		else if ((_room.getOwner().isInParty() && _player.isInParty()) && (_room.getOwner().getParty().getPartyLeaderOID() == _player.getParty().getPartyLeaderOID()))
		{
			writeD(2);
		}
		else
		{
			writeD(0);
		}
	}
}