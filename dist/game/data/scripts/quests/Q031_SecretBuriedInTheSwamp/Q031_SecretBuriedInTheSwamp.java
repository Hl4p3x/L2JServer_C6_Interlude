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
package quests.Q031_SecretBuriedInTheSwamp;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q031_SecretBuriedInTheSwamp extends Quest
{
	// Item
	private static final int KRORIN_JOURNAL = 7252;
	
	// NPCs
	private static final int ABERCROMBIE = 31555;
	private static final int FORGOTTEN_MONUMENT_1 = 31661;
	private static final int FORGOTTEN_MONUMENT_2 = 31662;
	private static final int FORGOTTEN_MONUMENT_3 = 31663;
	private static final int FORGOTTEN_MONUMENT_4 = 31664;
	private static final int CORPSE_OF_DWARF = 31665;
	
	public Q031_SecretBuriedInTheSwamp()
	{
		super(31, "Secret Buried in the Swamp");
		
		registerQuestItems(KRORIN_JOURNAL);
		
		addStartNpc(ABERCROMBIE);
		addTalkId(ABERCROMBIE, CORPSE_OF_DWARF, FORGOTTEN_MONUMENT_1, FORGOTTEN_MONUMENT_2, FORGOTTEN_MONUMENT_3, FORGOTTEN_MONUMENT_4);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("31555-01.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31665-01.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(KRORIN_JOURNAL, 1);
		}
		else if (event.equals("31555-04.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31661-01.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31662-01.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31663-01.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31664-01.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31555-07.htm"))
		{
			st.takeItems(KRORIN_JOURNAL, 1);
			st.rewardItems(57, 40000);
			st.rewardExpAndSp(130000, 0);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() < 66) ? "31555-00a.htm" : "31555-00.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ABERCROMBIE:
						if (cond == 1)
						{
							htmltext = "31555-02.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31555-03.htm";
						}
						else if ((cond > 2) && (cond < 7))
						{
							htmltext = "31555-05.htm";
						}
						else if (cond == 7)
						{
							htmltext = "31555-06.htm";
						}
						break;
					
					case CORPSE_OF_DWARF:
						if (cond == 1)
						{
							htmltext = "31665-00.htm";
						}
						else if (cond > 1)
						{
							htmltext = "31665-02.htm";
						}
						break;
					
					case FORGOTTEN_MONUMENT_1:
						if (cond == 3)
						{
							htmltext = "31661-00.htm";
						}
						else if (cond > 3)
						{
							htmltext = "31661-02.htm";
						}
						break;
					
					case FORGOTTEN_MONUMENT_2:
						if (cond == 4)
						{
							htmltext = "31662-00.htm";
						}
						else if (cond > 4)
						{
							htmltext = "31662-02.htm";
						}
						break;
					
					case FORGOTTEN_MONUMENT_3:
						if (cond == 5)
						{
							htmltext = "31663-00.htm";
						}
						else if (cond > 5)
						{
							htmltext = "31663-02.htm";
						}
						break;
					
					case FORGOTTEN_MONUMENT_4:
						if (cond == 6)
						{
							htmltext = "31664-00.htm";
						}
						else if (cond > 6)
						{
							htmltext = "31664-02.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}