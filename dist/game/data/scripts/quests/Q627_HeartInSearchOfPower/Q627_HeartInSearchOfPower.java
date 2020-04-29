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
package quests.Q627_HeartInSearchOfPower;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q627_HeartInSearchOfPower extends Quest
{
	// NPCs
	private static final int NECROMANCER = 31518;
	private static final int ENFEUX = 31519;
	
	// Items
	private static final int SEAL_OF_LIGHT = 7170;
	private static final int BEAD_OF_OBEDIENCE = 7171;
	private static final int GEM_OF_SAINTS = 7172;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(21520, 550000);
		CHANCES.put(21523, 584000);
		CHANCES.put(21524, 621000);
		CHANCES.put(21525, 621000);
		CHANCES.put(21526, 606000);
		CHANCES.put(21529, 625000);
		CHANCES.put(21530, 578000);
		CHANCES.put(21531, 690000);
		CHANCES.put(21532, 671000);
		CHANCES.put(21535, 693000);
		CHANCES.put(21536, 615000);
		CHANCES.put(21539, 762000);
		CHANCES.put(21540, 762000);
		CHANCES.put(21658, 690000);
	}
	
	// Rewards
	private static final Map<String, int[]> REWARDS = new HashMap<>();
	static
	{
		REWARDS.put("adena", new int[]
		{
			0,
			0,
			100000
		});
		REWARDS.put("asofe", new int[]
		{
			4043,
			13,
			6400
		});
		REWARDS.put("thon", new int[]
		{
			4044,
			13,
			6400
		});
		REWARDS.put("enria", new int[]
		{
			4042,
			6,
			13600
		});
		REWARDS.put("mold", new int[]
		{
			4041,
			3,
			17200
		});
	}
	
	public Q627_HeartInSearchOfPower()
	{
		super(627, "Heart in Search of Power");
		
		registerQuestItems(BEAD_OF_OBEDIENCE);
		
		addStartNpc(NECROMANCER);
		addTalkId(NECROMANCER, ENFEUX);
		
		for (int npcId : CHANCES.keySet())
		{
			addKillId(npcId);
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
		
		if (event.equals("31518-01.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31518-03.htm"))
		{
			if (st.getQuestItemsCount(BEAD_OF_OBEDIENCE) == 300)
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BEAD_OF_OBEDIENCE, -1);
				st.giveItems(SEAL_OF_LIGHT, 1);
			}
			else
			{
				htmltext = "31518-03a.htm";
				st.set("cond", "1");
				st.takeItems(BEAD_OF_OBEDIENCE, -1);
			}
		}
		else if (event.equals("31519-01.htm"))
		{
			if (st.getQuestItemsCount(SEAL_OF_LIGHT) == 1)
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(SEAL_OF_LIGHT, 1);
				st.giveItems(GEM_OF_SAINTS, 1);
			}
		}
		else if (REWARDS.containsKey(event))
		{
			if (st.getQuestItemsCount(GEM_OF_SAINTS) == 1)
			{
				htmltext = "31518-07.htm";
				st.takeItems(GEM_OF_SAINTS, 1);
				
				if (REWARDS.get(event)[0] > 0)
				{
					st.giveItems(REWARDS.get(event)[0], REWARDS.get(event)[1]);
				}
				st.rewardItems(57, REWARDS.get(event)[2]);
				
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31518-7.htm";
			}
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
				htmltext = (player.getLevel() < 60) ? "31518-00a.htm" : "31518-00.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case NECROMANCER:
						if (cond == 1)
						{
							htmltext = "31518-01a.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31518-02.htm";
						}
						else if (cond == 3)
						{
							htmltext = "31518-04.htm";
						}
						else if (cond == 4)
						{
							htmltext = "31518-05.htm";
						}
						break;
					
					case ENFEUX:
						if (cond == 3)
						{
							htmltext = "31519-00.htm";
						}
						else if (cond == 4)
						{
							htmltext = "31519-02.htm";
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
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(BEAD_OF_OBEDIENCE, 1, 300, CHANCES.get(npc.getNpcId())))
		{
			st.set("cond", "2");
		}
		
		return null;
	}
}