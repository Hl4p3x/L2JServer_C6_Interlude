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
package quests.Q602_ShadowOfLight;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q602_ShadowOfLight extends Quest
{
	private static final int EYE_OF_DARKNESS = 7189;
	
	private static final int[][] REWARDS =
	{
		{
			6699,
			40000,
			120000,
			20000,
			20
		},
		{
			6698,
			60000,
			110000,
			15000,
			40
		},
		{
			6700,
			40000,
			150000,
			10000,
			50
		},
		{
			0,
			100000,
			140000,
			11250,
			100
		}
	};
	
	public Q602_ShadowOfLight()
	{
		super(602, "Shadow of Light");
		
		registerQuestItems(EYE_OF_DARKNESS);
		
		addStartNpc(31683); // Eye of Argos
		addTalkId(31683);
		
		addKillId(21299, 21304);
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
		
		if (event.equals("31683-02.htm"))
		{
			if (player.getLevel() < 68)
			{
				htmltext = "31683-02a.htm";
			}
			else
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
		}
		else if (event.equals("31683-05.htm"))
		{
			st.takeItems(EYE_OF_DARKNESS, -1);
			
			final int random = Rnd.get(100);
			for (int[] element : REWARDS)
			{
				if (random < element[4])
				{
					st.rewardItems(57, element[1]);
					
					if (element[0] != 0)
					{
						st.giveItems(element[0], 3);
					}
					
					st.rewardExpAndSp(element[2], element[3]);
					break;
				}
			}
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = "31683-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "31683-03.htm";
				}
				else if (cond == 2)
				{
					htmltext = "31683-04.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMember(player, npc, "cond", "1");
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(EYE_OF_DARKNESS, 1, 100, (npc.getNpcId() == 21299) ? 450000 : 500000))
		{
			st.set("cond", "2");
		}
		
		return null;
	}
}