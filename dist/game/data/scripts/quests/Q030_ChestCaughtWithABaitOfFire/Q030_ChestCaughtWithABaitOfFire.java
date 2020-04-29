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
package quests.Q030_ChestCaughtWithABaitOfFire;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q053_LinnaeusSpecialBait.Q053_LinnaeusSpecialBait;

public class Q030_ChestCaughtWithABaitOfFire extends Quest
{
	// NPCs
	private static final int LINNAEUS = 31577;
	private static final int RUKAL = 30629;
	
	// Items
	private static final int RED_TREASURE_BOX = 6511;
	private static final int MUSICAL_SCORE = 7628;
	private static final int NECKLACE_OF_PROTECTION = 916;
	
	public Q030_ChestCaughtWithABaitOfFire()
	{
		super(30, "Chest caught with a bait of fire");
		
		registerQuestItems(MUSICAL_SCORE);
		
		addStartNpc(LINNAEUS);
		addTalkId(LINNAEUS, RUKAL);
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
		
		if (event.equals("31577-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31577-07.htm"))
		{
			if (st.hasQuestItems(RED_TREASURE_BOX))
			{
				st.set("cond", "2");
				st.takeItems(RED_TREASURE_BOX, 1);
				st.giveItems(MUSICAL_SCORE, 1);
			}
			else
			{
				htmltext = "31577-08.htm";
			}
		}
		else if (event.equals("30629-02.htm"))
		{
			if (st.hasQuestItems(MUSICAL_SCORE))
			{
				htmltext = "30629-02.htm";
				st.takeItems(MUSICAL_SCORE, 1);
				st.giveItems(NECKLACE_OF_PROTECTION, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
			else
			{
				htmltext = "30629-03.htm";
			}
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
				if (player.getLevel() < 60)
				{
					htmltext = "31577-02.htm";
				}
				else
				{
					final QuestState st2 = player.getQuestState(Q053_LinnaeusSpecialBait.class.getSimpleName());
					if ((st2 != null) && st2.isCompleted())
					{
						htmltext = "31577-01.htm";
					}
					else
					{
						htmltext = "31577-03.htm";
					}
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case LINNAEUS:
						if (cond == 1)
						{
							htmltext = (!st.hasQuestItems(RED_TREASURE_BOX)) ? "31577-06.htm" : "31577-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31577-09.htm";
						}
						break;
					
					case RUKAL:
						if (cond == 2)
						{
							htmltext = "30629-01.htm";
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