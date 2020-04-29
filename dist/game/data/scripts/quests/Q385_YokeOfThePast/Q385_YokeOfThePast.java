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
package quests.Q385_YokeOfThePast;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q385_YokeOfThePast extends Quest
{
	// NPCs
	private static final int[] GATEKEEPER_ZIGGURAT =
	{
		31095,
		31096,
		31097,
		31098,
		31099,
		31100,
		31101,
		31102,
		31103,
		31104,
		31105,
		31106,
		31107,
		31108,
		31109,
		31110,
		31114,
		31115,
		31116,
		31117,
		31118,
		31119,
		31120,
		31121,
		31122,
		31123,
		31124,
		31125,
		31126
	};
	
	// Item
	private static final int ANCIENT_SCROLL = 5902;
	
	// Reward
	private static final int BLANK_SCROLL = 5965;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(21208, 70000);
		CHANCES.put(21209, 80000);
		CHANCES.put(21210, 110000);
		CHANCES.put(21211, 110000);
		CHANCES.put(21213, 140000);
		CHANCES.put(21214, 190000);
		CHANCES.put(21215, 190000);
		CHANCES.put(21217, 240000);
		CHANCES.put(21218, 300000);
		CHANCES.put(21219, 300000);
		CHANCES.put(21221, 370000);
		CHANCES.put(21222, 460000);
		CHANCES.put(21223, 450000);
		CHANCES.put(21224, 500000);
		CHANCES.put(21225, 540000);
		CHANCES.put(21226, 660000);
		CHANCES.put(21227, 640000);
		CHANCES.put(21228, 700000);
		CHANCES.put(21229, 750000);
		CHANCES.put(21230, 910000);
		CHANCES.put(21231, 860000);
		CHANCES.put(21236, 120000);
		CHANCES.put(21237, 140000);
		CHANCES.put(21238, 190000);
		CHANCES.put(21239, 190000);
		CHANCES.put(21240, 220000);
		CHANCES.put(21241, 240000);
		CHANCES.put(21242, 300000);
		CHANCES.put(21243, 300000);
		CHANCES.put(21244, 340000);
		CHANCES.put(21245, 370000);
		CHANCES.put(21246, 460000);
		CHANCES.put(21247, 450000);
		CHANCES.put(21248, 500000);
		CHANCES.put(21249, 540000);
		CHANCES.put(21250, 660000);
		CHANCES.put(21251, 640000);
		CHANCES.put(21252, 700000);
		CHANCES.put(21253, 750000);
		CHANCES.put(21254, 910000);
		CHANCES.put(21255, 860000);
	}
	
	public Q385_YokeOfThePast()
	{
		super(385, "Yoke of the Past");
		
		registerQuestItems(ANCIENT_SCROLL);
		
		addStartNpc(GATEKEEPER_ZIGGURAT);
		addTalkId(GATEKEEPER_ZIGGURAT);
		
		for (int npcId : CHANCES.keySet())
		{
			addKillId(npcId);
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
		
		if (event.equals("05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("10.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
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
				htmltext = (player.getLevel() < 20) ? "02.htm" : "01.htm";
				break;
			
			case State.STARTED:
				if (!st.hasQuestItems(ANCIENT_SCROLL))
				{
					htmltext = "08.htm";
				}
				else
				{
					htmltext = "09.htm";
					final int count = st.getQuestItemsCount(ANCIENT_SCROLL);
					st.takeItems(ANCIENT_SCROLL, -1);
					st.rewardItems(BLANK_SCROLL, count);
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
		
		partyMember.getQuestState(getName()).dropItems(ANCIENT_SCROLL, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}