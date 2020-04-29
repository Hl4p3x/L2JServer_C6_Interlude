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
package quests.Q601_WatchingEyes;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q601_WatchingEyes extends Quest
{
	// Items
	private static final int PROOF_OF_AVENGER = 7188;
	
	// Rewards
	private static final int[][] REWARDS =
	{
		{
			6699,
			90000,
			20
		},
		{
			6698,
			80000,
			40
		},
		{
			6700,
			40000,
			50
		},
		{
			0,
			230000,
			100
		}
	};
	
	public Q601_WatchingEyes()
	{
		super(601, "Watching Eyes");
		
		registerQuestItems(PROOF_OF_AVENGER);
		
		addStartNpc(31683); // Eye of Argos
		addTalkId(31683);
		
		addKillId(21306, 21308, 21309, 21310, 21311);
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
		
		if (event.equals("31683-03.htm"))
		{
			if (player.getLevel() < 71)
			{
				htmltext = "31683-02.htm";
			}
			else
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
		}
		else if (event.equals("31683-07.htm"))
		{
			st.takeItems(PROOF_OF_AVENGER, -1);
			
			final int random = Rnd.get(100);
			for (int[] element : REWARDS)
			{
				if (random < element[2])
				{
					st.rewardItems(57, element[1]);
					if (element[0] != 0)
					{
						st.giveItems(element[0], 5);
						st.rewardExpAndSp(120000, 10000);
					}
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
					htmltext = (st.hasQuestItems(PROOF_OF_AVENGER)) ? "31683-05.htm" : "31683-04.htm";
				}
				else if (cond == 2)
				{
					htmltext = "31683-06.htm";
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
		
		if (st.dropItems(PROOF_OF_AVENGER, 1, 100, 500000))
		{
			st.set("cond", "2");
		}
		
		return null;
	}
}