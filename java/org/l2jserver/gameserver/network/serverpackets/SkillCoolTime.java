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

import java.util.Collection;

import org.l2jserver.gameserver.model.Timestamp;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * Skill Cool Time server packet implementation.
 * @author KenM, Zoey76, Mobius
 */
public class SkillCoolTime extends GameServerPacket
{
	private final long _currentTime;
	public Collection<Timestamp> _reuseTimestamps;
	
	public SkillCoolTime(PlayerInstance player)
	{
		_currentTime = System.currentTimeMillis();
		_reuseTimestamps = player.getReuseTimeStamps();
	}
	
	@Override
	protected final void writeImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		writeC(0xc1);
		writeD(_reuseTimestamps.size());
		for (Timestamp ts : _reuseTimestamps)
		{
			writeD(ts.getSkillId());
			writeD(ts.getSkillLevel());
			writeD((int) ts.getReuse() / 1000);
			writeD((int) Math.max(ts.getStamp() - _currentTime, 0) / 1000);
		}
	}
}