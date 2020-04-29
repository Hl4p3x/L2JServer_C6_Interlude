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
import org.l2jserver.gameserver.model.items.Henna;

public class HennaInfo extends GameServerPacket
{
	private final PlayerInstance _player;
	private final Henna[] _hennas = new Henna[3];
	private int _count;
	
	public HennaInfo(PlayerInstance player)
	{
		_player = player;
		_count = 0;
		for (int i = 0; i < 3; i++)
		{
			Henna henna = _player.getHenna(i + 1);
			if (henna != null)
			{
				_hennas[_count++] = henna;
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe4);
		
		writeC(_player.getHennaStatINT()); // equip INT
		writeC(_player.getHennaStatSTR()); // equip STR
		writeC(_player.getHennaStatCON()); // equip CON
		writeC(_player.getHennaStatMEN()); // equip MEM
		writeC(_player.getHennaStatDEX()); // equip DEX
		writeC(_player.getHennaStatWIT()); // equip WIT
		
		// Henna slots
		int classId = _player.getClassId().level();
		if (classId == 1)
		{
			writeD(2);
		}
		else if (classId > 1)
		{
			writeD(3);
		}
		else
		{
			writeD(0);
		}
		
		writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].canBeUsedBy(_player) ? _hennas[i].getSymbolId() : 0);
		}
	}
}
