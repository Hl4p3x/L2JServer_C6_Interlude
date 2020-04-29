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
package quests.Q386_StolenDignity;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

/**
 * Adapted from FirstTeam Interlude
 */
public class Q386_StolenDignity extends Quest
{
	private static final int ROMP = 30843;
	private static final int STOLEN_INFERIUM_ORE = 6363;
	private static final int REQUIRED_STOLEN_INFERIUM_ORE = 100;
	private static final Map<Integer, Integer> DROP_CHANCES = new HashMap<>();
	static
	{
		DROP_CHANCES.put(20670, 14);
		DROP_CHANCES.put(20671, 14);
		DROP_CHANCES.put(20954, 11);
		DROP_CHANCES.put(20956, 13);
		DROP_CHANCES.put(20958, 13);
		DROP_CHANCES.put(20959, 13);
		DROP_CHANCES.put(20960, 11);
		DROP_CHANCES.put(20964, 13);
		DROP_CHANCES.put(20969, 19);
		DROP_CHANCES.put(20967, 18);
		DROP_CHANCES.put(20970, 18);
		DROP_CHANCES.put(20971, 18);
		DROP_CHANCES.put(20974, 28);
		DROP_CHANCES.put(20975, 28);
		DROP_CHANCES.put(21001, 14);
		DROP_CHANCES.put(21003, 18);
		DROP_CHANCES.put(21005, 14);
		DROP_CHANCES.put(21020, 16);
		DROP_CHANCES.put(21021, 15);
		DROP_CHANCES.put(21259, 15);
		DROP_CHANCES.put(21089, 13);
		DROP_CHANCES.put(21108, 19);
		DROP_CHANCES.put(21110, 18);
		DROP_CHANCES.put(21113, 25);
		DROP_CHANCES.put(21114, 23);
		DROP_CHANCES.put(21116, 25);
	}
	private static final Map<Integer, Bingo> BINGOS = new HashMap<>();
	// @formatter:off
	private static final int[][] REWARDS_WIN =
	{
		{5529, 10}, {5532, 10}, {5533, 10}, {5534, 10}, {5535, 10}, {5536, 10}, {5537, 10}, {5538, 10}, {5539, 10}, {5541, 10}, {5542, 10},
		{5543, 10}, {5544, 10}, {5545, 10}, {5546, 10}, {5547, 10}, {5548, 10}, {8331, 10}, {8341, 10}, {8342, 10}, {8346, 10}, {8349, 10},
		{8712, 10}, {8713, 10}, {8714, 10}, {8715, 10}, {8716, 10}, {8717, 10}, {8718, 10}, {8719, 10}, {8720, 10}, {8721, 10}, {8722, 10}
	};
	private static final int[][] REWARDS_LOSE =
	{
		{5529, 4}, {5532, 4}, {5533, 4}, {5534, 4}, {5535, 4}, {5536, 4}, {5537, 4}, {5538, 4}, {5539, 4}, {5541, 4}, {5542, 4},
		{5543, 4}, {5544, 4}, {5545, 4}, {5546, 4}, {5547, 4}, {5548, 4}, {8331, 4}, {8341, 4}, {8342, 4}, {8346, 4}, {8349, 4},
		{8712, 4}, {8713, 4}, {8714, 4}, {8715, 4}, {8716, 4}, {8717, 4}, {8718, 4}, {8719, 4}, {8720, 4}, {8721, 4}, {8722, 4}
	};
	// @formatter:on
	
	public Q386_StolenDignity()
	{
		super(386, "Stolen Dignity");
		
		addStartNpc(ROMP);
		addTalkId(ROMP);
		for (int killId : DROP_CHANCES.keySet())
		{
			addKillId(killId);
		}
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return event;
		}
		
		if ("warehouse_keeper_romp_q0386_05.htm".equalsIgnoreCase(event))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if ("warehouse_keeper_romp_q0386_08.htm".equalsIgnoreCase(event))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if ("game".equalsIgnoreCase(event))
		{
			if (st.getQuestItemsCount(STOLEN_INFERIUM_ORE) < REQUIRED_STOLEN_INFERIUM_ORE)
			{
				return "warehouse_keeper_romp_q0386_11.htm";
			}
			st.takeItems(STOLEN_INFERIUM_ORE, REQUIRED_STOLEN_INFERIUM_ORE);
			final int char_obj_id = st.getPlayer().getObjectId();
			if (BINGOS.containsKey(char_obj_id))
			{
				BINGOS.remove(char_obj_id);
			}
			final Bingo bingo = new Bingo(st);
			BINGOS.put(char_obj_id, bingo);
			return bingo.getDialog("");
		}
		else if (event.contains("choice-"))
		{
			final int char_obj_id = st.getPlayer().getObjectId();
			if (!BINGOS.containsKey(char_obj_id))
			{
				return null;
			}
			final Bingo bingo = BINGOS.get(char_obj_id);
			return bingo.Select(event.replaceFirst("choice-", ""));
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (st.getState() != 1)
		{
			return (st.getQuestItemsCount(STOLEN_INFERIUM_ORE) < REQUIRED_STOLEN_INFERIUM_ORE) ? "warehouse_keeper_romp_q0386_06.htm" : "warehouse_keeper_romp_q0386_07.htm";
		}
		if (st.getPlayer().getLevel() < 58)
		{
			st.exitQuest(true);
			return "warehouse_keeper_romp_q0386_04.htm";
		}
		return "warehouse_keeper_romp_q0386_01.htm";
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		final Integer chance = DROP_CHANCES.get(npc.getNpcId());
		if ((chance != null) && (Rnd.get(100) < chance))
		{
			st.giveItems(STOLEN_INFERIUM_ORE, 1);
		}
		return null;
	}
	
	public static class Bingo extends ai.others.Bingo
	{
		protected static final String MSG_BEGIN = "I've arranged the numbers 1 through 9 on the grid. Don't peek!<br>Let me have the " + REQUIRED_STOLEN_INFERIUM_ORE + " Infernium Ores. Too many players try to run away without paying when it becomes obvious that they're losing...<br>OK, select six numbers between 1 and 9. Choose the %choicenum% number.";
		protected static final String MSG_AGAIN = "You've already chosen that number. Make your %choicenum% choice again.";
		protected static final String MSG_ZERO_LINES = "Wow! How unlucky can you get? Your choices are highlighted in red below. As you can see, your choices didn't make a single line! Losing this badly is actually quite rare!<br>You look so sad, I feel bad for you... Wait here...<br>.<br>.<br>.<br>Take this... I hope it will bring you better luck in the future.";
		protected static final String MSG_THREE_LINES = "Excellent! As you can see, you've formed three lines! Congratulations! As promised, I'll give you some unclaimed merchandise from the warehouse. Wait here...<br>.<br>.<br>.<br>Whew, it's dusty! OK, here you go. Do you like it?";
		protected static final String MSG_LOSE = "Oh, too bad. Your choices didn't form three lines. You should try again... Your choices are highlighted in red.";
		private static final String TEMPLATE_CHOICE = "<a action=\"bypass -h Quest Q386_StolenDignity choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ";
		
		private final QuestState _qs;
		
		public Bingo(QuestState qs)
		{
			super(TEMPLATE_CHOICE);
			_qs = qs;
		}
		
		@Override
		protected String getFinal()
		{
			final String result = super.getFinal();
			if (lines == 3)
			{
				reward(REWARDS_WIN);
			}
			else if (lines == 0)
			{
				reward(REWARDS_LOSE);
			}
			BINGOS.remove(_qs.getPlayer().getObjectId());
			return result;
		}
		
		private void reward(int[][] rew)
		{
			final int[] r = rew[Rnd.get(rew.length)];
			_qs.giveItems(r[0], r[1]);
		}
	}
}
