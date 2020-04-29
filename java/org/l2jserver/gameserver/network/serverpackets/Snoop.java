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

import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * CDSDDSS -> (0xd5)(objId)(name)(0x00)(type)(speaker)(name)
 */
public class Snoop extends GameServerPacket
{
	private final PlayerInstance _snooped;
	private final ChatType _type;
	private final String _speaker;
	private final String _msg;
	
	public Snoop(PlayerInstance snooped, ChatType _chatType, String speaker, String msg)
	{
		_snooped = snooped;
		_type = _chatType;
		_speaker = speaker;
		_msg = msg;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xd5);
		writeD(_snooped.getObjectId());
		writeS(_snooped.getName());
		writeD(0); // ??
		writeD(_type.getClientId());
		writeS(_speaker);
		writeS(_msg);
	}
}