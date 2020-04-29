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

import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @version $Revision: 1.4.2.5.2.6 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharSelected extends GameServerPacket
{
	private final PlayerInstance _player;
	private final int _sessionId;
	
	/**
	 * @param player
	 * @param sessionId
	 */
	public CharSelected(PlayerInstance player, int sessionId)
	{
		_player = player;
		_sessionId = sessionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x15);
		
		writeS(_player.getName());
		writeD(_player.getObjectId()); // ??
		writeS(_player.getTitle());
		writeD(_sessionId);
		writeD(_player.getClanId());
		writeD(0x00); // ??
		writeD(_player.getAppearance().isFemale() ? 1 : 0);
		writeD(_player.getRace().ordinal());
		writeD(_player.getClassId().getId());
		writeD(0x01); // active ??
		writeD(_player.getX());
		writeD(_player.getY());
		writeD(_player.getZ());
		
		writeF(_player.getCurrentHp());
		writeF(_player.getCurrentMp());
		writeD(_player.getSp());
		writeQ(_player.getExp());
		writeD(_player.getLevel());
		writeD(_player.getKarma()); // thx evill33t
		writeD(0x0); // ?
		writeD(_player.getINT());
		writeD(_player.getSTR());
		writeD(_player.getCON());
		writeD(_player.getMEN());
		writeD(_player.getDEX());
		writeD(_player.getWIT());
		for (int i = 0; i < 30; i++)
		{
			writeD(0x00);
		}
		// writeD(0); //c3
		// writeD(0); //c3
		// writeD(0); //c3
		writeD(0x00); // c3 work
		writeD(0x00); // c3 work
		
		// extra info
		writeD(GameTimeController.getInstance().getGameTime()); // in-game time
		
		writeD(0x00); //
		
		writeD(0x00); // c3
		
		writeD(0x00); // c3 InspectorBin
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		
		writeD(0x00); // c3 InspectorBin for 528 client
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
	}
}