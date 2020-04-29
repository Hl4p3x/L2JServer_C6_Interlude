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
package quests.Q154_SacrificeToTheSea;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q154_SacrificeToTheSea extends Quest
{
	// NPCs
	private static final int ROCKSWELL = 30312;
	private static final int CRISTEL = 30051;
	private static final int ROLFE = 30055;
	
	// Items
	private static final int FOX_FUR = 1032;
	private static final int FOX_FUR_YARN = 1033;
	private static final int MAIDEN_DOLL = 1034;
	
	// Reward
	private static final int EARING = 113;
	
	public Q154_SacrificeToTheSea()
	{
		super(154, "Sacrifice to the Sea");
		
		registerQuestItems(FOX_FUR, FOX_FUR_YARN, MAIDEN_DOLL);
		
		addStartNpc(ROCKSWELL);
		addTalkId(ROCKSWELL, CRISTEL, ROLFE);
		
		addKillId(20481, 20544, 20545); // Following Keltirs can be found near Talking Island.
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
		
		if (event.equals("30312-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = (player.getLevel() < 2) ? "30312-02.htm" : "30312-03.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ROCKSWELL:
						if (cond == 1)
						{
							htmltext = "30312-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30312-08.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30312-06.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30312-07.htm";
							st.takeItems(MAIDEN_DOLL, -1);
							st.giveItems(EARING, 1);
							st.rewardExpAndSp(100, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case CRISTEL:
						if (cond == 1)
						{
							htmltext = (st.hasQuestItems(FOX_FUR)) ? "30051-01.htm" : "30051-01a.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30051-02.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(FOX_FUR, -1);
							st.giveItems(FOX_FUR_YARN, 1);
						}
						else if (cond == 3)
						{
							htmltext = "30051-03.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30051-04.htm";
						}
						break;
					
					case ROLFE:
						if (cond < 3)
						{
							htmltext = "30055-03.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30055-01.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(FOX_FUR_YARN, 1);
							st.giveItems(MAIDEN_DOLL, 1);
						}
						else if (cond == 4)
						{
							htmltext = "30055-02.htm";
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
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(FOX_FUR, 1, 10, 400000))
		{
			st.set("cond", "2");
		}
		
		return null;
	}
}