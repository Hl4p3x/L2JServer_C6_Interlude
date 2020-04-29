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
package quests.Q419_GetAPet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q419_GetAPet extends Quest
{
	// Items
	private static final int ANIMAL_LOVER_LIST = 3417;
	private static final int ANIMAL_SLAYER_LIST_1 = 3418;
	private static final int ANIMAL_SLAYER_LIST_2 = 3419;
	private static final int ANIMAL_SLAYER_LIST_3 = 3420;
	private static final int ANIMAL_SLAYER_LIST_4 = 3421;
	private static final int ANIMAL_SLAYER_LIST_5 = 3422;
	private static final int BLOODY_FANG = 3423;
	private static final int BLOODY_CLAW = 3424;
	private static final int BLOODY_NAIL = 3425;
	private static final int BLOODY_KASHA_FANG = 3426;
	private static final int BLOODY_TARANTULA_NAIL = 3427;
	
	// Reward
	private static final int WOLF_COLLAR = 2375;
	
	// NPCs
	private static final int MARTIN = 30731;
	private static final int BELLA = 30256;
	private static final int METTY = 30072;
	private static final int ELLIE = 30091;
	
	// Droplist
	private static final Map<Integer, int[]> DROPLIST = new HashMap<>();
	static
	{
		DROPLIST.put(20103, new int[]
		{
			BLOODY_FANG,
			600000
		});
		DROPLIST.put(20106, new int[]
		{
			BLOODY_FANG,
			750000
		});
		DROPLIST.put(20108, new int[]
		{
			BLOODY_FANG,
			1000000
		});
		DROPLIST.put(20460, new int[]
		{
			BLOODY_CLAW,
			600000
		});
		DROPLIST.put(20308, new int[]
		{
			BLOODY_CLAW,
			750000
		});
		DROPLIST.put(20466, new int[]
		{
			BLOODY_CLAW,
			1000000
		});
		DROPLIST.put(20025, new int[]
		{
			BLOODY_NAIL,
			600000
		});
		DROPLIST.put(20105, new int[]
		{
			BLOODY_NAIL,
			750000
		});
		DROPLIST.put(20034, new int[]
		{
			BLOODY_NAIL,
			1000000
		});
		DROPLIST.put(20474, new int[]
		{
			BLOODY_KASHA_FANG,
			600000
		});
		DROPLIST.put(20476, new int[]
		{
			BLOODY_KASHA_FANG,
			750000
		});
		DROPLIST.put(20478, new int[]
		{
			BLOODY_KASHA_FANG,
			1000000
		});
		DROPLIST.put(20403, new int[]
		{
			BLOODY_TARANTULA_NAIL,
			750000
		});
		DROPLIST.put(20508, new int[]
		{
			BLOODY_TARANTULA_NAIL,
			1000000
		});
	}
	
	public Q419_GetAPet()
	{
		super(419, "Get a Pet");
		
		registerQuestItems(ANIMAL_LOVER_LIST, ANIMAL_SLAYER_LIST_1, ANIMAL_SLAYER_LIST_2, ANIMAL_SLAYER_LIST_3, ANIMAL_SLAYER_LIST_4, ANIMAL_SLAYER_LIST_5, BLOODY_FANG, BLOODY_CLAW, BLOODY_NAIL, BLOODY_KASHA_FANG, BLOODY_TARANTULA_NAIL);
		
		addStartNpc(MARTIN);
		addTalkId(MARTIN, BELLA, ELLIE, METTY);
		
		for (int npcId : DROPLIST.keySet())
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
		
		if (event.equals("task"))
		{
			final int race = player.getRace().ordinal();
			htmltext = "30731-0" + (race + 4) + ".htm";
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ANIMAL_SLAYER_LIST_1 + race, 1);
		}
		else if (event.equals("30731-12.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ANIMAL_SLAYER_LIST_1, 1);
			st.takeItems(ANIMAL_SLAYER_LIST_2, 1);
			st.takeItems(ANIMAL_SLAYER_LIST_3, 1);
			st.takeItems(ANIMAL_SLAYER_LIST_4, 1);
			st.takeItems(ANIMAL_SLAYER_LIST_5, 1);
			st.takeItems(BLOODY_FANG, -1);
			st.takeItems(BLOODY_CLAW, -1);
			st.takeItems(BLOODY_NAIL, -1);
			st.takeItems(BLOODY_KASHA_FANG, -1);
			st.takeItems(BLOODY_TARANTULA_NAIL, -1);
			st.giveItems(ANIMAL_LOVER_LIST, 1);
		}
		else if (event.equals("30256-03.htm"))
		{
			st.set("progress", String.valueOf(st.getInt("progress") | 1));
			if (st.getInt("progress") == 7)
			{
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equals("30072-02.htm"))
		{
			st.set("progress", String.valueOf(st.getInt("progress") | 2));
			if (st.getInt("progress") == 7)
			{
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equals("30091-02.htm"))
		{
			st.set("progress", String.valueOf(st.getInt("progress") | 4));
			if (st.getInt("progress") == 7)
			{
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equals("test"))
		{
			st.set("answers", "0");
			st.set("quiz", "20 21 22 23 24 25 26 27 28 29 30 31 32 33");
			return checkQuestions(st);
		}
		else if (event.equals("wrong"))
		{
			st.set("wrong", String.valueOf(st.getInt("wrong") + 1));
			return checkQuestions(st);
		}
		else if (event.equals("right"))
		{
			st.set("correct", String.valueOf(st.getInt("correct") + 1));
			return checkQuestions(st);
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
				htmltext = (player.getLevel() < 15) ? "30731-01.htm" : "30731-02.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case MARTIN:
						if (st.hasAtLeastOneQuestItem(ANIMAL_SLAYER_LIST_1, ANIMAL_SLAYER_LIST_2, ANIMAL_SLAYER_LIST_3, ANIMAL_SLAYER_LIST_4, ANIMAL_SLAYER_LIST_5))
						{
							final int proofs = st.getQuestItemsCount(BLOODY_FANG) + st.getQuestItemsCount(BLOODY_CLAW) + st.getQuestItemsCount(BLOODY_NAIL) + st.getQuestItemsCount(BLOODY_KASHA_FANG) + st.getQuestItemsCount(BLOODY_TARANTULA_NAIL);
							if (proofs == 0)
							{
								htmltext = "30731-09.htm";
							}
							else if (proofs < 50)
							{
								htmltext = "30731-10.htm";
							}
							else
							{
								htmltext = "30731-11.htm";
							}
						}
						else if (st.getInt("progress") == 7)
						{
							htmltext = "30731-13.htm";
						}
						else
						{
							htmltext = "30731-16.htm";
						}
						break;
					
					case BELLA:
						htmltext = "30256-01.htm";
						break;
					
					case METTY:
						htmltext = "30072-01.htm";
						break;
					
					case ELLIE:
						htmltext = "30091-01.htm";
						break;
				}
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
		
		final int[] drop = DROPLIST.get(npc.getNpcId());
		if (st.hasQuestItems(drop[0] - 5))
		{
			st.dropItems(drop[0], 1, 50, drop[1]);
		}
		
		return null;
	}
	
	private static String checkQuestions(QuestState st)
	{
		final int answers = st.getInt("correct") + (st.getInt("wrong"));
		if (answers < 10)
		{
			final String[] questions = st.getString("quiz").split(" ");
			final int index = Rnd.get(questions.length - 1);
			final String question = questions[index];
			if (questions.length > (10 - answers))
			{
				questions[index] = questions[questions.length - 1];
				st.set("quiz", String.join(" ", Arrays.copyOf(questions, questions.length - 1)));
			}
			return "30731-" + question + ".htm";
		}
		
		if (st.getInt("wrong") > 0)
		{
			st.unset("progress");
			st.unset("answers");
			st.unset("quiz");
			st.unset("wrong");
			st.unset("correct");
			return "30731-14.htm";
		}
		
		st.takeItems(ANIMAL_LOVER_LIST, 1);
		st.giveItems(WOLF_COLLAR, 1);
		st.playSound(QuestState.SOUND_FINISH);
		st.exitQuest(true);
		
		return "30731-15.htm";
	}
}