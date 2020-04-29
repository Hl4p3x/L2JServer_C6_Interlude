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
package quests.Q002_WhatWomenWant;

import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q002_WhatWomenWant extends Quest
{
	// NPCs
	private static final int ARUJIEN = 30223;
	private static final int MIRABEL = 30146;
	private static final int HERBIEL = 30150;
	private static final int GREENIS = 30157;
	
	// Items
	private static final int ARUJIEN_LETTER_1 = 1092;
	private static final int ARUJIEN_LETTER_2 = 1093;
	private static final int ARUJIEN_LETTER_3 = 1094;
	private static final int POETRY_BOOK = 689;
	private static final int GREENIS_LETTER = 693;
	
	// Rewards
	private static final int MYSTICS_EARRING = 113;
	
	public Q002_WhatWomenWant()
	{
		super(2, "What Women Want");
		
		registerQuestItems(ARUJIEN_LETTER_1, ARUJIEN_LETTER_2, ARUJIEN_LETTER_3, POETRY_BOOK, GREENIS_LETTER);
		
		addStartNpc(ARUJIEN);
		addTalkId(ARUJIEN, MIRABEL, HERBIEL, GREENIS);
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
		
		if (event.equals("30223-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ARUJIEN_LETTER_1, 1);
		}
		else if (event.equals("30223-08.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ARUJIEN_LETTER_3, 1);
			st.giveItems(POETRY_BOOK, 1);
		}
		else if (event.equals("30223-09.htm"))
		{
			st.takeItems(ARUJIEN_LETTER_3, 1);
			st.rewardItems(57, 450);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				if ((player.getRace() != Race.ELF) && (player.getRace() != Race.HUMAN))
				{
					htmltext = "30223-00.htm";
				}
				else if (player.getLevel() < 2)
				{
					htmltext = "30223-01.htm";
				}
				else
				{
					htmltext = "30223-02.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ARUJIEN:
						if (st.hasQuestItems(ARUJIEN_LETTER_1))
						{
							htmltext = "30223-05.htm";
						}
						else if (st.hasQuestItems(ARUJIEN_LETTER_3))
						{
							htmltext = "30223-07.htm";
						}
						else if (st.hasQuestItems(ARUJIEN_LETTER_2))
						{
							htmltext = "30223-06.htm";
						}
						else if (st.hasQuestItems(POETRY_BOOK))
						{
							htmltext = "30223-11.htm";
						}
						else if (st.hasQuestItems(GREENIS_LETTER))
						{
							htmltext = "30223-10.htm";
							st.takeItems(GREENIS_LETTER, 1);
							st.giveItems(MYSTICS_EARRING, 1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case MIRABEL:
						if (cond == 1)
						{
							htmltext = "30146-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ARUJIEN_LETTER_1, 1);
							st.giveItems(ARUJIEN_LETTER_2, 1);
						}
						else if (cond > 1)
						{
							htmltext = "30146-02.htm";
						}
						break;
					
					case HERBIEL:
						if (cond == 2)
						{
							htmltext = "30150-01.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ARUJIEN_LETTER_2, 1);
							st.giveItems(ARUJIEN_LETTER_3, 1);
						}
						else if (cond > 2)
						{
							htmltext = "30150-02.htm";
						}
						break;
					
					case GREENIS:
						if (cond < 4)
						{
							htmltext = "30157-01.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30157-02.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(POETRY_BOOK, 1);
							st.giveItems(GREENIS_LETTER, 1);
						}
						else if (cond == 5)
						{
							htmltext = "30157-03.htm";
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