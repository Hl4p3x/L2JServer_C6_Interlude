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
package quests.Q639_GuardiansOfTheHolyGrail;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q639_GuardiansOfTheHolyGrail extends Quest
{
	// NPCs
	private static final int DOMINIC = 31350;
	private static final int GREMORY = 32008;
	private static final int HOLY_GRAIL = 32028;
	
	// Items
	private static final int SCRIPTURE = 8069;
	private static final int WATER_BOTTLE = 8070;
	private static final int HOLY_WATER_BOTTLE = 8071;
	
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(22122, 760000);
		CHANCES.put(22123, 750000);
		CHANCES.put(22124, 590000);
		CHANCES.put(22125, 580000);
		CHANCES.put(22126, 590000);
		CHANCES.put(22127, 580000);
		CHANCES.put(22128, 170000);
		CHANCES.put(22129, 590000);
		CHANCES.put(22130, 850000);
		CHANCES.put(22131, 920000);
		CHANCES.put(22132, 580000);
		CHANCES.put(22133, 930000);
		CHANCES.put(22134, 230000);
		CHANCES.put(22135, 580000);
	}
	
	public Q639_GuardiansOfTheHolyGrail()
	{
		super(639, "Guardians of the Holy Grail");
		
		registerQuestItems(SCRIPTURE, WATER_BOTTLE, HOLY_WATER_BOTTLE);
		
		addStartNpc(DOMINIC);
		addTalkId(DOMINIC, GREMORY, HOLY_GRAIL);
		
		for (int id : CHANCES.keySet())
		{
			addKillId(id);
		}
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
		
		// DOMINIC
		if (event.equals("31350-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31350-08.htm"))
		{
			final int count = st.getQuestItemsCount(SCRIPTURE);
			st.takeItems(SCRIPTURE, -1);
			st.rewardItems(57, (1625 * count) + ((count >= 10) ? 33940 : 0));
		}
		else if (event.equals("31350-09.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		// GREMORY
		else if (event.equals("32008-05.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(WATER_BOTTLE, 1);
		}
		else if (event.equals("32008-09.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(HOLY_WATER_BOTTLE, 1);
		}
		else if (event.equals("32008-12.htm"))
		{
			if (st.getQuestItemsCount(SCRIPTURE) >= 4000)
			{
				htmltext = "32008-11.htm";
				st.takeItems(SCRIPTURE, 4000);
				st.rewardItems(959, 1);
			}
		}
		else if (event.equals("32008-14.htm"))
		{
			if (st.getQuestItemsCount(SCRIPTURE) >= 400)
			{
				htmltext = "32008-13.htm";
				st.takeItems(SCRIPTURE, 400);
				st.rewardItems(960, 1);
			}
		}
		// HOLY GRAIL
		else if (event.equals("32028-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(WATER_BOTTLE, 1);
			st.giveItems(HOLY_WATER_BOTTLE, 1);
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
				htmltext = (player.getLevel() < 73) ? "31350-02.htm" : "31350-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case DOMINIC:
						htmltext = (st.hasQuestItems(SCRIPTURE)) ? "31350-05.htm" : "31350-06.htm";
						break;
					
					case GREMORY:
						if (cond == 1)
						{
							htmltext = "32008-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "32008-06.htm";
						}
						else if (cond == 3)
						{
							htmltext = "32008-08.htm";
						}
						else if (cond == 4)
						{
							htmltext = "32008-10.htm";
						}
						break;
					
					case HOLY_GRAIL:
						if (cond == 2)
						{
							htmltext = "32028-01.htm";
						}
						else if (cond > 2)
						{
							htmltext = "32028-03.htm";
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
		final PlayerInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		partyMember.getQuestState(getName()).dropItems(SCRIPTURE, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}