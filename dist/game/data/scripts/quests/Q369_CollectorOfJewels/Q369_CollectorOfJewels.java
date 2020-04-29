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
package quests.Q369_CollectorOfJewels;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q369_CollectorOfJewels extends Quest
{
	// NPC
	private static final int NELL = 30376;
	
	// Items
	private static final int FLARE_SHARD = 5882;
	private static final int FREEZING_SHARD = 5883;
	
	// Reward
	private static final int ADENA = 57;
	
	// Droplist
	private static final Map<Integer, int[]> DROPLIST = new HashMap<>();
	static
	{
		DROPLIST.put(20609, new int[]
		{
			FLARE_SHARD,
			630000
		});
		DROPLIST.put(20612, new int[]
		{
			FLARE_SHARD,
			770000
		});
		DROPLIST.put(20749, new int[]
		{
			FLARE_SHARD,
			850000
		});
		DROPLIST.put(20616, new int[]
		{
			FREEZING_SHARD,
			600000
		});
		DROPLIST.put(20619, new int[]
		{
			FREEZING_SHARD,
			730000
		});
		DROPLIST.put(20747, new int[]
		{
			FREEZING_SHARD,
			850000
		});
	}
	
	public Q369_CollectorOfJewels()
	{
		super(369, "Collector of Jewels");
		
		registerQuestItems(FLARE_SHARD, FREEZING_SHARD);
		
		addStartNpc(NELL);
		addTalkId(NELL);
		
		for (int mob : DROPLIST.keySet())
		{
			addKillId(mob);
		}
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
		
		if (event.equals("30376-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30376-07.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equals("30376-08.htm"))
		{
			st.exitQuest(true);
			st.playSound(QuestState.SOUND_FINISH);
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
				htmltext = (player.getLevel() < 25) ? "30376-01.htm" : "30376-02.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				final int flare = st.getQuestItemsCount(FLARE_SHARD);
				final int freezing = st.getQuestItemsCount(FREEZING_SHARD);
				if (cond == 1)
				{
					htmltext = "30376-04.htm";
				}
				else if ((cond == 2) && (flare >= 50) && (freezing >= 50))
				{
					htmltext = "30376-05.htm";
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(FLARE_SHARD, -1);
					st.takeItems(FREEZING_SHARD, -1);
					st.rewardItems(ADENA, 12500);
				}
				else if (cond == 3)
				{
					htmltext = "30376-09.htm";
				}
				else if ((cond == 4) && (flare >= 200) && (freezing >= 200))
				{
					htmltext = "30376-10.htm";
					st.takeItems(FLARE_SHARD, -1);
					st.takeItems(FREEZING_SHARD, -1);
					st.rewardItems(ADENA, 63500);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
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
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		final int cond = st.getInt("cond");
		final int[] drop = DROPLIST.get(npc.getNpcId());
		if (cond == 1)
		{
			if (st.dropItems(drop[0], 1, 50, drop[1]) && (st.getQuestItemsCount((drop[0] == FLARE_SHARD) ? FREEZING_SHARD : FLARE_SHARD) >= 50))
			{
				st.set("cond", "2");
			}
		}
		else if ((cond == 3) && st.dropItems(drop[0], 1, 200, drop[1]) && (st.getQuestItemsCount((drop[0] == FLARE_SHARD) ? FREEZING_SHARD : FLARE_SHARD) >= 200))
		{
			st.set("cond", "4");
		}
		
		return null;
	}
}