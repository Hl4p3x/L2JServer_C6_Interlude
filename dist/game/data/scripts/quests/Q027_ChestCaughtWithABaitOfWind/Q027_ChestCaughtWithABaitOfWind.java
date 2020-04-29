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
package quests.Q027_ChestCaughtWithABaitOfWind;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q050_LanoscosSpecialBait.Q050_LanoscosSpecialBait;

public class Q027_ChestCaughtWithABaitOfWind extends Quest
{
	// NPCs
	private static final int LANOSCO = 31570;
	private static final int SHALING = 31442;
	
	// Items
	private static final int LARGE_BLUE_TREASURE_CHEST = 6500;
	private static final int STRANGE_BLUEPRINT = 7625;
	private static final int BLACK_PEARL_RING = 880;
	
	public Q027_ChestCaughtWithABaitOfWind()
	{
		super(27, "Chest caught with a bait of wind");
		
		registerQuestItems(STRANGE_BLUEPRINT);
		
		addStartNpc(LANOSCO);
		addTalkId(LANOSCO, SHALING);
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
		
		if (event.equals("31570-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31570-07.htm"))
		{
			if (st.hasQuestItems(LARGE_BLUE_TREASURE_CHEST))
			{
				st.set("cond", "2");
				st.takeItems(LARGE_BLUE_TREASURE_CHEST, 1);
				st.giveItems(STRANGE_BLUEPRINT, 1);
			}
			else
			{
				htmltext = "31570-08.htm";
			}
		}
		else if (event.equals("31434-02.htm"))
		{
			if (st.hasQuestItems(STRANGE_BLUEPRINT))
			{
				htmltext = "31434-02.htm";
				st.takeItems(STRANGE_BLUEPRINT, 1);
				st.giveItems(BLACK_PEARL_RING, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
			else
			{
				htmltext = "31434-03.htm";
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
				if (player.getLevel() < 27)
				{
					htmltext = "31570-02.htm";
				}
				else
				{
					final QuestState st2 = player.getQuestState(Q050_LanoscosSpecialBait.class.getSimpleName());
					if ((st2 != null) && st2.isCompleted())
					{
						htmltext = "31570-01.htm";
					}
					else
					{
						htmltext = "31570-03.htm";
					}
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case LANOSCO:
						if (cond == 1)
						{
							htmltext = (!st.hasQuestItems(LARGE_BLUE_TREASURE_CHEST)) ? "31570-06.htm" : "31570-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31570-09.htm";
						}
						break;
					
					case SHALING:
						if (cond == 2)
						{
							htmltext = "31434-01.htm";
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