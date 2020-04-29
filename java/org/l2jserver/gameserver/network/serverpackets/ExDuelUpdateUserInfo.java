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
 * Format: ch Sddddddddd.
 * @author KenM
 */
public class ExDuelUpdateUserInfo extends GameServerPacket
{
	/** The _active char. */
	private final PlayerInstance _player;
	
	/**
	 * Instantiates a new ex duel update user info.
	 * @param player the cha
	 */
	public ExDuelUpdateUserInfo(PlayerInstance player)
	{
		_player = player;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4f);
		writeS(_player.getName());
		writeD(_player.getObjectId());
		writeD(_player.getClassId().getId());
		writeD(_player.getLevel());
		writeD((int) _player.getCurrentHp());
		writeD(_player.getMaxHp());
		writeD((int) _player.getCurrentMp());
		writeD(_player.getMaxMp());
		writeD((int) _player.getCurrentCp());
		writeD(_player.getMaxCp());
	}
}
