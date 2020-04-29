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
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.clan.ClanMember;

/**
 * @author -Wooden-
 */
public class PledgeShowMemberListUpdate extends GameServerPacket
{
	private final PlayerInstance _player;
	private final int _pledgeType;
	private int _hasSponsor;
	private final String _name;
	private final int _level;
	private final int _classId;
	private final int _objectId;
	private final boolean _isOnline;
	
	public PledgeShowMemberListUpdate(PlayerInstance player)
	{
		_player = player;
		_pledgeType = player.getPledgeType();
		if (_pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _player.getSponsor() != 0 ? 1 : 0;
		}
		else if (_player.isOnline())
		{
			_hasSponsor = _player.isClanLeader() ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
		_name = _player.getName();
		_level = _player.getLevel();
		_classId = _player.getClassId().getId();
		_objectId = _player.getObjectId();
		_isOnline = _player.isOnline();
	}
	
	public PledgeShowMemberListUpdate(ClanMember player)
	{
		_player = player.getPlayerInstance();
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId();
		_objectId = player.getObjectId();
		_isOnline = _player.isOnline();
		_pledgeType = player.getPledgeType();
		if (_pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _player.getSponsor() != 0 ? 1 : 0;
		}
		else if (player.isOnline())
		{
			_hasSponsor = _player.isClanLeader() ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(0);
		writeD(_objectId);
		writeD(_isOnline ? 1 : 0); // 1=online 0=offline
		writeD(_pledgeType);
		writeD(_hasSponsor);
	}
}
