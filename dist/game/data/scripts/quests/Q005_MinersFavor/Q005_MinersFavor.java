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
package quests.Q005_MinersFavor;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q005_MinersFavor extends Quest
{
	// NPCs
	private static final int BOLTER = 30554;
	private static final int SHARI = 30517;
	private static final int GARITA = 30518;
	private static final int REED = 30520;
	private static final int BRUNON = 30526;
	
	// Items
	private static final int BOLTERS_LIST = 1547;
	private static final int MINING_BOOTS = 1548;
	private static final int MINERS_PICK = 1549;
	private static final int BOOMBOOM_POWDER = 1550;
	private static final int REDSTONE_BEER = 1551;
	private static final int BOLTERS_SMELLY_SOCKS = 1552;
	
	// Reward
	private static final int NECKLACE = 906;
	
	public Q005_MinersFavor()
	{
		super(5, "Miner's Favor");
		
		registerQuestItems(BOLTERS_LIST, MINING_BOOTS, MINERS_PICK, BOOMBOOM_POWDER, REDSTONE_BEER, BOLTERS_SMELLY_SOCKS);
		
		addStartNpc(BOLTER);
		addTalkId(BOLTER, SHARI, GARITA, REED, BRUNON);
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
		
		if (event.equals("30554-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(BOLTERS_LIST, 1);
			st.giveItems(BOLTERS_SMELLY_SOCKS, 1);
		}
		else if (event.equals("30526-02.htm"))
		{
			st.takeItems(BOLTERS_SMELLY_SOCKS, 1);
			st.giveItems(MINERS_PICK, 1);
			if (st.hasQuestItems(MINING_BOOTS, BOOMBOOM_POWDER, REDSTONE_BEER))
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
			{
				st.playSound(QuestState.SOUND_ITEMGET);
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
				htmltext = (player.getLevel() < 2) ? "30554-01.htm" : "30554-02.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case BOLTER:
						if (cond == 1)
						{
							htmltext = "30554-04.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30554-06.htm";
							st.takeItems(BOLTERS_LIST, 1);
							st.takeItems(BOOMBOOM_POWDER, 1);
							st.takeItems(MINERS_PICK, 1);
							st.takeItems(MINING_BOOTS, 1);
							st.takeItems(REDSTONE_BEER, 1);
							st.giveItems(NECKLACE, 1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case SHARI:
						if ((cond == 1) && !st.hasQuestItems(BOOMBOOM_POWDER))
						{
							htmltext = "30517-01.htm";
							st.giveItems(BOOMBOOM_POWDER, 1);
							if (st.hasQuestItems(MINING_BOOTS, MINERS_PICK, REDSTONE_BEER))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else
						{
							htmltext = "30517-02.htm";
						}
						break;
					
					case GARITA:
						if ((cond == 1) && !st.hasQuestItems(MINING_BOOTS))
						{
							htmltext = "30518-01.htm";
							st.giveItems(MINING_BOOTS, 1);
							if (st.hasQuestItems(MINERS_PICK, BOOMBOOM_POWDER, REDSTONE_BEER))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else
						{
							htmltext = "30518-02.htm";
						}
						break;
					
					case REED:
						if ((cond == 1) && !st.hasQuestItems(REDSTONE_BEER))
						{
							htmltext = "30520-01.htm";
							st.giveItems(REDSTONE_BEER, 1);
							if (st.hasQuestItems(MINING_BOOTS, MINERS_PICK, BOOMBOOM_POWDER))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else
						{
							htmltext = "30520-02.htm";
						}
						break;
					
					case BRUNON:
						if ((cond == 1) && !st.hasQuestItems(MINERS_PICK))
						{
							htmltext = "30526-01.htm";
						}
						else
						{
							htmltext = "30526-03.htm";
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