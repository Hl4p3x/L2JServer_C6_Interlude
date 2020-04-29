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

import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.clan.Clan;

/**
 * Format: (ch) d [dd].
 * @author -Wooden-
 */
public class PledgeSkillList extends GameServerPacket
{
	/** The _clan. */
	private final Clan _clan;
	
	/**
	 * Instantiates a new pledge skill list.
	 * @param clan the clan
	 */
	public PledgeSkillList(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected void writeImpl()
	{
		final Skill[] skills = _clan.getAllSkills();
		writeC(0xfe);
		writeH(0x39);
		writeD(skills.length);
		for (Skill sk : skills)
		{
			writeD(sk.getId());
			writeD(sk.getLevel());
		}
	}
}
