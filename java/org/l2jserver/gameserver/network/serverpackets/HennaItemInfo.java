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

public class HennaItemInfo extends GameServerPacket
{
	private final PlayerInstance _player;
	private final Henna _henna;
	
	public HennaItemInfo(Henna henna, PlayerInstance player)
	{
		_henna = henna;
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe3);
		writeD(_henna.getSymbolId()); // symbol Id
		writeD(_henna.getDyeId()); // item id of dye
		writeD(Henna.getRequiredDyeAmount()); // total amount of dye required
		writeD(_henna.getPrice()); // total amount of adenas required to draw symbol
		writeD(1); // able to draw or not 0 is false and 1 is true
		writeD(_player.getAdena());
		
		writeD(_player.getINT()); // current INT
		writeC(_player.getINT() + _henna.getINT()); // equip INT
		writeD(_player.getSTR()); // current STR
		writeC(_player.getSTR() + _henna.getSTR()); // equip STR
		writeD(_player.getCON()); // current CON
		writeC(_player.getCON() + _henna.getCON()); // equip CON
		writeD(_player.getMEN()); // current MEM
		writeC(_player.getMEN() + _henna.getMEN()); // equip MEM
		writeD(_player.getDEX()); // current DEX
		writeC(_player.getDEX() + _henna.getDEX()); // equip DEX
		writeD(_player.getWIT()); // current WIT
		writeC(_player.getWIT() + _henna.getWIT()); // equip WIT
	}
}
