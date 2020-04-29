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
package quests.Q017_LightAndDarkness;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q017_LightAndDarkness extends Quest
{
	// Items
	private static final int BLOOD_OF_SAINT = 7168;
	
	// NPCs
	private static final int HIERARCH = 31517;
	private static final int SAINT_ALTAR_1 = 31508;
	private static final int SAINT_ALTAR_2 = 31509;
	private static final int SAINT_ALTAR_3 = 31510;
	private static final int SAINT_ALTAR_4 = 31511;
	
	public Q017_LightAndDarkness()
	{
		super(17, "Light and Darkness");
		
		registerQuestItems(BLOOD_OF_SAINT);
		
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH, SAINT_ALTAR_1, SAINT_ALTAR_2, SAINT_ALTAR_3, SAINT_ALTAR_4);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("31517-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(BLOOD_OF_SAINT, 4);
		}
		else if (event.equals("31508-02.htm"))
		{
			if (st.hasQuestItems(BLOOD_OF_SAINT))
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BLOOD_OF_SAINT, 1);
			}
			else
			{
				htmltext = "31508-03.htm";
			}
		}
		else if (event.equals("31509-02.htm"))
		{
			if (st.hasQuestItems(BLOOD_OF_SAINT))
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BLOOD_OF_SAINT, 1);
			}
			else
			{
				htmltext = "31509-03.htm";
			}
		}
		else if (event.equals("31510-02.htm"))
		{
			if (st.hasQuestItems(BLOOD_OF_SAINT))
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BLOOD_OF_SAINT, 1);
			}
			else
			{
				htmltext = "31510-03.htm";
			}
		}
		else if (event.equals("31511-02.htm"))
		{
			if (st.hasQuestItems(BLOOD_OF_SAINT))
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BLOOD_OF_SAINT, 1);
			}
			else
			{
				htmltext = "31511-03.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() < 61) ? "31517-03.htm" : "31517-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case HIERARCH:
						if (cond == 5)
						{
							htmltext = "31517-07.htm";
							st.rewardExpAndSp(105527, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						else
						{
							if (st.hasQuestItems(BLOOD_OF_SAINT))
							{
								htmltext = "31517-05.htm";
							}
							else
							{
								htmltext = "31517-06.htm";
								st.exitQuest(true);
							}
						}
						break;
					
					case SAINT_ALTAR_1:
						if (cond == 1)
						{
							htmltext = "31508-01.htm";
						}
						else if (cond > 1)
						{
							htmltext = "31508-04.htm";
						}
						break;
					
					case SAINT_ALTAR_2:
						if (cond == 2)
						{
							htmltext = "31509-01.htm";
						}
						else if (cond > 2)
						{
							htmltext = "31509-04.htm";
						}
						break;
					
					case SAINT_ALTAR_3:
						if (cond == 3)
						{
							htmltext = "31510-01.htm";
						}
						else if (cond > 3)
						{
							htmltext = "31510-04.htm";
						}
						break;
					
					case SAINT_ALTAR_4:
						if (cond == 4)
						{
							htmltext = "31511-01.htm";
						}
						else if (cond > 4)
						{
							htmltext = "31511-04.htm";
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