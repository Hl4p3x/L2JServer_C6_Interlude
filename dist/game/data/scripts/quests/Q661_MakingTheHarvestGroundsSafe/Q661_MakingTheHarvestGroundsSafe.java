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
package quests.Q661_MakingTheHarvestGroundsSafe;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q661_MakingTheHarvestGroundsSafe extends Quest
{
	// NPC
	private static final int NORMAN = 30210;
	
	// Items
	private static final int STING_OF_GIANT_POISON_BEE = 8283;
	private static final int CLOUDY_GEM = 8284;
	private static final int TALON_OF_YOUNG_ARANEID = 8285;
	
	// Reward
	private static final int ADENA = 57;
	
	// Monsters
	private static final int GIANT_POISON_BEE = 21095;
	private static final int CLOUDY_BEAST = 21096;
	private static final int YOUNG_ARANEID = 21097;
	
	public Q661_MakingTheHarvestGroundsSafe()
	{
		super(661, "Making the Harvest Grounds Safe");
		
		registerQuestItems(STING_OF_GIANT_POISON_BEE, CLOUDY_GEM, TALON_OF_YOUNG_ARANEID);
		
		addStartNpc(NORMAN);
		addTalkId(NORMAN);
		
		addKillId(GIANT_POISON_BEE, CLOUDY_BEAST, YOUNG_ARANEID);
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
		
		if (event.equals("30210-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30210-04.htm"))
		{
			final int item1 = st.getQuestItemsCount(STING_OF_GIANT_POISON_BEE);
			final int item2 = st.getQuestItemsCount(CLOUDY_GEM);
			final int item3 = st.getQuestItemsCount(TALON_OF_YOUNG_ARANEID);
			int sum = 0;
			sum = (item1 * 57) + (item2 * 56) + (item3 * 60);
			if ((item1 + item2 + item3) >= 10)
			{
				sum += 2871;
			}
			
			st.takeItems(STING_OF_GIANT_POISON_BEE, item1);
			st.takeItems(CLOUDY_GEM, item2);
			st.takeItems(TALON_OF_YOUNG_ARANEID, item3);
			st.rewardItems(ADENA, sum);
		}
		else if (event.equals("30210-06.htm"))
		{
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 21) ? "30210-01a.htm" : "30210-01.htm";
				break;
			
			case State.STARTED:
				htmltext = (st.hasAtLeastOneQuestItem(STING_OF_GIANT_POISON_BEE, CLOUDY_GEM, TALON_OF_YOUNG_ARANEID)) ? "30210-03.htm" : "30210-05.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		st.dropItems(npc.getNpcId() - 12812, 1, 0, 500000);
		
		return null;
	}
}