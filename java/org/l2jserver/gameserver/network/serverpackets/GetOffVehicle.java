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

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Maktakien
 */
public class GetOffVehicle extends GameServerPacket
{
	private final PlayerInstance _player;
	private final int _boatId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public GetOffVehicle(PlayerInstance player, int boatId, int x, int y, int z)
	{
		_player = player;
		_boatId = boatId;
		_x = x;
		_y = y;
		_z = z;
		_player.setBoat(null);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5d);
		writeD(_player.getObjectId());
		writeD(_boatId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
