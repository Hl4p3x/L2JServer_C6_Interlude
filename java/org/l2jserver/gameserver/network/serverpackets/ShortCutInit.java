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

import org.l2jserver.gameserver.model.ShortCut;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * ShortCutInit format d *(1dddd)/(2ddddd)/(3dddd)
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit extends GameServerPacket
{
	private ShortCut[] _shortCuts;
	private PlayerInstance _player;
	
	public ShortCutInit(PlayerInstance player)
	{
		_player = player;
		if (_player == null)
		{
			return;
		}
		
		_shortCuts = _player.getAllShortCuts();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);
		
		for (ShortCut sc : _shortCuts)
		{
			writeD(sc.getType());
			writeD(sc.getSlot() + (sc.getPage() * 12));
			
			switch (sc.getType())
			{
				case ShortCut.TYPE_ITEM: // 1
				{
					writeD(sc.getId());
					writeD(0x01);
					writeD(-1);
					writeD(0x00);
					writeD(0x00);
					writeH(0x00);
					writeH(0x00);
					break;
				}
				case ShortCut.TYPE_SKILL: // 2
				{
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeC(0x00); // C5
					writeD(0x01); // C6
					break;
				}
				case ShortCut.TYPE_ACTION: // 3
				{
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				}
				case ShortCut.TYPE_MACRO: // 4
				{
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				}
				case ShortCut.TYPE_RECIPE: // 5
				{
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				}
				default:
				{
					writeD(sc.getId());
					writeD(0x01); // C6
				}
			}
		}
	}
}
