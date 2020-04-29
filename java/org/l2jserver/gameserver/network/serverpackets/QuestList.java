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

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.QuestState;

public class QuestList extends GameServerPacket
{
	private final Collection<QuestState> _questStates;
	
	public QuestList(PlayerInstance player)
	{
		_questStates = player.getAllQuestStates();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x80);
		writeH(_questStates.size());
		for (QuestState qs : _questStates)
		{
			writeD(qs.getQuest().getQuestId());
			
			final int states = qs.getInt("__compltdStateFlags");
			if (states != 0)
			{
				writeD(states);
			}
			else
			{
				writeD(qs.getInt("cond"));
			}
		}
	}
}
