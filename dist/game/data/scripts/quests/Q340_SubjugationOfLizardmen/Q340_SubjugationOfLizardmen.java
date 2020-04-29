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
package quests.Q340_SubjugationOfLizardmen;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q340_SubjugationOfLizardmen extends Quest
{
	// NPCs
	private static final int WEISZ = 30385;
	private static final int ADONIUS = 30375;
	private static final int LEVIAN = 30037;
	private static final int CHEST = 30989;
	
	// Items
	private static final int CARGO = 4255;
	private static final int HOLY = 4256;
	private static final int ROSARY = 4257;
	private static final int TOTEM = 4258;
	
	public Q340_SubjugationOfLizardmen()
	{
		super(340, "Subjugation of Lizardmen");
		
		registerQuestItems(CARGO, HOLY, ROSARY, TOTEM);
		
		addStartNpc(WEISZ);
		addTalkId(WEISZ, ADONIUS, LEVIAN, CHEST);
		
		addKillId(20008, 20010, 20014, 20024, 20027, 20030, 25146);
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
		
		if (event.equals("30385-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30385-07.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CARGO, -1);
		}
		else if (event.equals("30385-09.htm"))
		{
			st.takeItems(CARGO, -1);
			st.rewardItems(57, 4090);
		}
		else if (event.equals("30385-10.htm"))
		{
			st.takeItems(CARGO, -1);
			st.rewardItems(57, 4090);
			st.exitQuest(true);
		}
		else if (event.equals("30375-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30037-02.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30989-02.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(TOTEM, 1);
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
				htmltext = (player.getLevel() < 17) ? "30385-01.htm" : "30385-02.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case WEISZ:
						if (cond == 1)
						{
							htmltext = (st.getQuestItemsCount(CARGO) < 30) ? "30385-05.htm" : "30385-06.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30385-11.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30385-13.htm";
							st.rewardItems(57, 14700);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case ADONIUS:
						if (cond == 2)
						{
							htmltext = "30375-01.htm";
						}
						else if (cond == 3)
						{
							if (st.hasQuestItems(ROSARY, HOLY))
							{
								htmltext = "30375-04.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(HOLY, -1);
								st.takeItems(ROSARY, -1);
							}
							else
							{
								htmltext = "30375-03.htm";
							}
						}
						else if (cond == 4)
						{
							htmltext = "30375-05.htm";
						}
						break;
					
					case LEVIAN:
						if (cond == 4)
						{
							htmltext = "30037-01.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30037-03.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30037-04.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TOTEM, -1);
						}
						else if (cond == 7)
						{
							htmltext = "30037-05.htm";
						}
						break;
					
					case CHEST:
						if (cond == 5)
						{
							htmltext = "30989-01.htm";
						}
						else
						{
							htmltext = "30989-03.htm";
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
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case 20008:
				if (st.getInt("cond") == 1)
				{
					st.dropItems(CARGO, 1, 30, 500000);
				}
				break;
			
			case 20010:
				if (st.getInt("cond") == 1)
				{
					st.dropItems(CARGO, 1, 30, 520000);
				}
				break;
			
			case 20014:
				if (st.getInt("cond") == 1)
				{
					st.dropItems(CARGO, 1, 30, 550000);
				}
				break;
			
			case 20024:
			case 20027:
			case 20030:
				if ((st.getInt("cond") == 3) && st.dropItems(HOLY, 1, 1, 100000))
				{
					st.dropItems(ROSARY, 1, 1, 100000);
				}
				break;
			
			case 25146:
				addSpawn(CHEST, npc, false, 30000);
				break;
		}
		return null;
	}
}