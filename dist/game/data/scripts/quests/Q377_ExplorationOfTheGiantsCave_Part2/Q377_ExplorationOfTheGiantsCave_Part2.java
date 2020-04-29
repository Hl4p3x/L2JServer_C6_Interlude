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
package quests.Q377_ExplorationOfTheGiantsCave_Part2;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q377_ExplorationOfTheGiantsCave_Part2 extends Quest
{
	// Items
	private static final int ANCIENT_BOOK = 5955;
	private static final int DICTIONARY_INTERMEDIATE = 5892;
	
	private static final int[][] BOOKS =
	{
		// science & technology -> majestic leather, leather armor of nightmare
		{
			5945,
			5946,
			5947,
			5948,
			5949
		},
		// culture -> armor of nightmare, majestic plate
		{
			5950,
			5951,
			5952,
			5953,
			5954
		}
	};
	
	// Rewards
	private static final int[][] RECIPES =
	{
		// science & technology -> majestic leather, leather armor of nightmare
		{
			5338,
			5336
		},
		// culture -> armor of nightmare, majestic plate
		{
			5420,
			5422
		}
	};
	
	public Q377_ExplorationOfTheGiantsCave_Part2()
	{
		super(377, "Exploration of Giants Cave, Part 2");
		
		addStartNpc(31147); // Sobling
		addTalkId(31147);
		
		addKillId(20654, 20656, 20657, 20658);
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
		
		if (event.equals("31147-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31147-04.htm"))
		{
			htmltext = checkItems(st);
		}
		else if (event.equals("31147-07.htm"))
		{
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
				htmltext = ((player.getLevel() < 57) || !st.hasQuestItems(DICTIONARY_INTERMEDIATE)) ? "31147-01.htm" : "31147-02.htm";
				break;
			
			case State.STARTED:
				htmltext = checkItems(st);
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		partyMember.getQuestState(getName()).dropItems(ANCIENT_BOOK, 1, 0, 18000);
		
		return null;
	}
	
	private static String checkItems(QuestState st)
	{
		for (int type = 0; type < BOOKS.length; type++)
		{
			boolean complete = true;
			for (int book : BOOKS[type])
			{
				if (!st.hasQuestItems(book))
				{
					complete = false;
				}
			}
			
			if (complete)
			{
				for (int book : BOOKS[type])
				{
					st.takeItems(book, 1);
				}
				
				st.giveItems(RECIPES[type][Rnd.get(RECIPES[type].length)], 1);
				return "31147-04.htm";
			}
		}
		return "31147-05.htm";
	}
}