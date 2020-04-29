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
package quests.Q636_TheTruthBeyondTheGate;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q636_TheTruthBeyondTheGate extends Quest
{
	// NPCs
	private static final int ELIYAH = 31329;
	private static final int FLAURON = 32010;
	// Item
	private static final int MARK = 8064;
	
	public Q636_TheTruthBeyondTheGate()
	{
		super(636, "Truth Beyond the Gate");
		
		addStartNpc(ELIYAH);
		addTalkId(ELIYAH, FLAURON);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		if (event.equals("31329-04.htm"))
		{
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equals("32010-02.htm"))
		{
			qs.playSound("ItemSound.quest_finish");
			qs.giveItems(MARK, 1);
			qs.unset("cond");
			qs.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		final int npcId = npc.getNpcId();
		final int id = qs.getState();
		final int cond = qs.getInt("cond");
		if ((cond == 0) && (id == State.CREATED))
		{
			if (npcId == ELIYAH)
			{
				if (player.getLevel() > 72)
				{
					htmltext = "31329-02.htm";
				}
				else
				{
					htmltext = "31329-01.htm";
					qs.exitQuest(true);
				}
			}
		}
		else if (id == State.STARTED)
		{
			if (npcId == ELIYAH)
			{
				htmltext = "31329-05.htm";
			}
			else if (npcId == FLAURON)
			{
				if (cond == 1)
				{
					htmltext = "32010-01.htm";
					qs.set("cond", "2");
				}
				else
				{
					htmltext = "32010-03.htm";
				}
			}
		}
		
		return htmltext;
	}
}