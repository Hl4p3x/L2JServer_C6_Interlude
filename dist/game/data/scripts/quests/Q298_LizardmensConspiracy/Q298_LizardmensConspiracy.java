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
package quests.Q298_LizardmensConspiracy;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q298_LizardmensConspiracy extends Quest
{
	// NPCs
	private static final int PRAGA = 30333;
	private static final int ROHMER = 30344;
	
	// Items
	private static final int PATROL_REPORT = 7182;
	private static final int WHITE_GEM = 7183;
	private static final int RED_GEM = 7184;
	
	public Q298_LizardmensConspiracy()
	{
		super(298, "Lizardmen's Conspiracy");
		
		registerQuestItems(PATROL_REPORT, WHITE_GEM, RED_GEM);
		
		addStartNpc(PRAGA);
		addTalkId(PRAGA, ROHMER);
		
		addKillId(20926, 20927, 20922, 20923, 20924);
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
		
		if (event.equals("30333-1.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(PATROL_REPORT, 1);
		}
		else if (event.equals("30344-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PATROL_REPORT, 1);
		}
		else if (event.equals("30344-4.htm"))
		{
			if (st.getInt("cond") == 3)
			{
				htmltext = "30344-3.htm";
				st.takeItems(WHITE_GEM, -1);
				st.takeItems(RED_GEM, -1);
				st.rewardExpAndSp(0, 42000);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
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
				htmltext = (player.getLevel() < 25) ? "30333-0b.htm" : "30333-0a.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case PRAGA:
						htmltext = "30333-2.htm";
						break;
					
					case ROHMER:
						if (st.getInt("cond") == 1)
						{
							htmltext = (st.hasQuestItems(PATROL_REPORT)) ? "30344-0.htm" : "30344-0a.htm";
						}
						else
						{
							htmltext = "30344-2.htm";
						}
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMember(player, npc, "2");
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case 20922:
				if (st.dropItems(WHITE_GEM, 1, 50, 400000) && (st.getQuestItemsCount(RED_GEM) >= 50))
				{
					st.set("cond", "3");
				}
				break;
			
			case 20923:
				if (st.dropItems(WHITE_GEM, 1, 50, 450000) && (st.getQuestItemsCount(RED_GEM) >= 50))
				{
					st.set("cond", "3");
				}
				break;
			
			case 20924:
				if (st.dropItems(WHITE_GEM, 1, 50, 350000) && (st.getQuestItemsCount(RED_GEM) >= 50))
				{
					st.set("cond", "3");
				}
				break;
			
			case 20926:
			case 20927:
				if (st.dropItems(RED_GEM, 1, 50, 400000) && (st.getQuestItemsCount(WHITE_GEM) >= 50))
				{
					st.set("cond", "3");
				}
				break;
		}
		
		return null;
	}
}