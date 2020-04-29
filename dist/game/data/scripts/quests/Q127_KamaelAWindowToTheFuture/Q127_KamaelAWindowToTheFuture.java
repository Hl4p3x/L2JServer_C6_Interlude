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
package quests.Q127_KamaelAWindowToTheFuture;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.ExShowSlideshowKamael;

public class Q127_KamaelAWindowToTheFuture extends Quest
{
	// NPCs
	private static final int DOMINIC = 31350;
	private static final int KLAUS = 30187;
	private static final int ALDER = 32092;
	private static final int AKLAN = 31288;
	private static final int OLTLIN = 30862;
	private static final int JURIS = 30113;
	private static final int RODEMAI = 30756;
	
	// Items
	private static final int MARK_DOMINIC = 8939;
	private static final int MARK_HUMAN = 8940;
	private static final int MARK_DWARF = 8941;
	private static final int MARK_ORC = 8944;
	private static final int MARK_DELF = 8943;
	private static final int MARK_ELF = 8942;
	
	public Q127_KamaelAWindowToTheFuture()
	{
		super(127, "Kamael: A Window to the Future");
		
		registerQuestItems(MARK_DOMINIC, MARK_HUMAN, MARK_DWARF, MARK_ORC, MARK_DELF, MARK_ELF);
		
		addStartNpc(DOMINIC);
		addTalkId(DOMINIC, KLAUS, ALDER, AKLAN, OLTLIN, JURIS, RODEMAI);
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
		
		if (event.equals("31350-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(MARK_DOMINIC, 1);
		}
		else if (event.equals("31350-06.htm"))
		{
			st.takeItems(MARK_HUMAN, -1);
			st.takeItems(MARK_DWARF, -1);
			st.takeItems(MARK_ELF, -1);
			st.takeItems(MARK_DELF, -1);
			st.takeItems(MARK_ORC, -1);
			st.takeItems(MARK_DOMINIC, -1);
			st.rewardItems(57, 159100);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equals("30187-06.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equals("30187-08.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_HUMAN, 1);
		}
		else if (event.equals("32092-05.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_DWARF, 1);
		}
		else if (event.equals("31288-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_ORC, 1);
		}
		else if (event.equals("30862-04.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_DELF, 1);
		}
		else if (event.equals("30113-04.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_ELF, 1);
		}
		else if (event.equals("kamaelstory"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			player.sendPacket(ExShowSlideshowKamael.STATIC_PACKET);
			return null;
		}
		else if (event.equals("30756-05.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
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
		
		npc.getNpcId();
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = "31350-01.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case KLAUS:
						if (cond == 1)
						{
							htmltext = "30187-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30187-06.htm";
						}
						break;
					
					case ALDER:
						if (cond == 3)
						{
							htmltext = "32092-01.htm";
						}
						break;
					
					case AKLAN:
						if (cond == 4)
						{
							htmltext = "31288-01.htm";
						}
						break;
					
					case OLTLIN:
						if (cond == 5)
						{
							htmltext = "30862-01.htm";
						}
						break;
					
					case JURIS:
						if (cond == 6)
						{
							htmltext = "30113-01.htm";
						}
						break;
					
					case RODEMAI:
						if (cond == 7)
						{
							htmltext = "30756-01.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30756-04.htm";
						}
						break;
					
					case DOMINIC:
						if (cond == 9)
						{
							htmltext = "31350-05.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				return htmltext;
		}
		
		return htmltext;
	}
}