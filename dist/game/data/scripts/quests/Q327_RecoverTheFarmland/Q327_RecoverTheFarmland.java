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
package quests.Q327_RecoverTheFarmland;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q327_RecoverTheFarmland extends Quest
{
	// Items
	private static final int LEIKAN_LETTER = 5012;
	private static final int TUREK_DOGTAG = 1846;
	private static final int TUREK_MEDALLION = 1847;
	private static final int CLAY_URN_FRAGMENT = 1848;
	private static final int BRASS_TRINKET_PIECE = 1849;
	private static final int BRONZE_MIRROR_PIECE = 1850;
	private static final int JADE_NECKLACE_BEAD = 1851;
	private static final int ANCIENT_CLAY_URN = 1852;
	private static final int ANCIENT_BRASS_TIARA = 1853;
	private static final int ANCIENT_BRONZE_MIRROR = 1854;
	private static final int ANCIENT_JADE_NECKLACE = 1855;
	
	// Rewards
	private static final int ADENA = 57;
	private static final int SOULSHOT_D = 1463;
	private static final int SPIRITSHOT_D = 2510;
	private static final int HEALING_POTION = 1061;
	private static final int HASTE_POTION = 734;
	private static final int POTION_OF_ALACRITY = 735;
	private static final int SCROLL_OF_ESCAPE = 736;
	private static final int SCROLL_OF_RESURRECTION = 737;
	
	// NPCs
	private static final int LEIKAN = 30382;
	private static final int PIOTUR = 30597;
	private static final int IRIS = 30034;
	private static final int ASHA = 30313;
	private static final int NESTLE = 30314;
	
	// Monsters
	private static final int TUREK_ORC_WARLORD = 20495;
	private static final int TUREK_ORC_ARCHER = 20496;
	private static final int TUREK_ORC_SKIRMISHER = 20497;
	private static final int TUREK_ORC_SUPPLIER = 20498;
	private static final int TUREK_ORC_FOOTMAN = 20499;
	private static final int TUREK_ORC_SENTINEL = 20500;
	private static final int TUREK_ORC_SHAMAN = 20501;
	
	// Chances
	private static final int[][] DROPLIST =
	{
		{
			TUREK_ORC_ARCHER,
			140000,
			TUREK_DOGTAG
		},
		{
			TUREK_ORC_SKIRMISHER,
			70000,
			TUREK_DOGTAG
		},
		{
			TUREK_ORC_SUPPLIER,
			120000,
			TUREK_DOGTAG
		},
		{
			TUREK_ORC_FOOTMAN,
			100000,
			TUREK_DOGTAG
		},
		{
			TUREK_ORC_SENTINEL,
			80000,
			TUREK_DOGTAG
		},
		{
			TUREK_ORC_SHAMAN,
			90000,
			TUREK_MEDALLION
		},
		{
			TUREK_ORC_WARLORD,
			180000,
			TUREK_MEDALLION
		}
	};
	
	// Exp
	private static final Map<Integer, Integer> EXP_REWARD = new HashMap<>();
	static
	{
		EXP_REWARD.put(ANCIENT_CLAY_URN, 2766);
		EXP_REWARD.put(ANCIENT_BRASS_TIARA, 3227);
		EXP_REWARD.put(ANCIENT_BRONZE_MIRROR, 3227);
		EXP_REWARD.put(ANCIENT_JADE_NECKLACE, 3919);
	}
	
	public Q327_RecoverTheFarmland()
	{
		super(327, "Recover the Farmland");
		
		registerQuestItems(LEIKAN_LETTER);
		
		addStartNpc(LEIKAN, PIOTUR);
		addTalkId(LEIKAN, PIOTUR, IRIS, ASHA, NESTLE);
		
		addKillId(TUREK_ORC_WARLORD, TUREK_ORC_ARCHER, TUREK_ORC_SKIRMISHER, TUREK_ORC_SUPPLIER, TUREK_ORC_FOOTMAN, TUREK_ORC_SENTINEL, TUREK_ORC_SHAMAN);
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
		
		// Piotur
		if (event.equals("30597-03.htm") && (st.getInt("cond") < 1))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30597-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		// Leikan
		else if (event.equals("30382-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LEIKAN_LETTER, 1);
		}
		// Asha
		else if (event.equals("30313-02.htm"))
		{
			if (st.getQuestItemsCount(CLAY_URN_FRAGMENT) >= 5)
			{
				st.takeItems(CLAY_URN_FRAGMENT, 5);
				if (Rnd.get(6) < 5)
				{
					htmltext = "30313-03.htm";
					st.rewardItems(ANCIENT_CLAY_URN, 1);
				}
				else
				{
					htmltext = "30313-10.htm";
				}
			}
		}
		else if (event.equals("30313-04.htm"))
		{
			if (st.getQuestItemsCount(BRASS_TRINKET_PIECE) >= 5)
			{
				st.takeItems(BRASS_TRINKET_PIECE, 5);
				if (Rnd.get(7) < 6)
				{
					htmltext = "30313-05.htm";
					st.rewardItems(ANCIENT_BRASS_TIARA, 1);
				}
				else
				{
					htmltext = "30313-10.htm";
				}
			}
		}
		else if (event.equals("30313-06.htm"))
		{
			if (st.getQuestItemsCount(BRONZE_MIRROR_PIECE) >= 5)
			{
				st.takeItems(BRONZE_MIRROR_PIECE, 5);
				if (Rnd.get(7) < 6)
				{
					htmltext = "30313-07.htm";
					st.rewardItems(ANCIENT_BRONZE_MIRROR, 1);
				}
				else
				{
					htmltext = "30313-10.htm";
				}
			}
		}
		else if (event.equals("30313-08.htm"))
		{
			if (st.getQuestItemsCount(JADE_NECKLACE_BEAD) >= 5)
			{
				st.takeItems(JADE_NECKLACE_BEAD, 5);
				if (Rnd.get(8) < 7)
				{
					htmltext = "30313-09.htm";
					st.rewardItems(ANCIENT_JADE_NECKLACE, 1);
				}
				else
				{
					htmltext = "30313-10.htm";
				}
			}
		}
		// Iris
		else if (event.equals("30034-03.htm"))
		{
			final int n = st.getQuestItemsCount(CLAY_URN_FRAGMENT);
			if (n == 0)
			{
				htmltext = "30034-02.htm";
			}
			else
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(CLAY_URN_FRAGMENT, n);
				st.rewardExpAndSp(n * 307, 0);
			}
		}
		else if (event.equals("30034-04.htm"))
		{
			final int n = st.getQuestItemsCount(BRASS_TRINKET_PIECE);
			if (n == 0)
			{
				htmltext = "30034-02.htm";
			}
			else
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(BRASS_TRINKET_PIECE, n);
				st.rewardExpAndSp(n * 368, 0);
			}
		}
		else if (event.equals("30034-05.htm"))
		{
			final int n = st.getQuestItemsCount(BRONZE_MIRROR_PIECE);
			if (n == 0)
			{
				htmltext = "30034-02.htm";
			}
			else
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(BRONZE_MIRROR_PIECE, n);
				st.rewardExpAndSp(n * 368, 0);
			}
		}
		else if (event.equals("30034-06.htm"))
		{
			final int n = st.getQuestItemsCount(JADE_NECKLACE_BEAD);
			if (n == 0)
			{
				htmltext = "30034-02.htm";
			}
			else
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(JADE_NECKLACE_BEAD, n);
				st.rewardExpAndSp(n * 430, 0);
			}
		}
		else if (event.equals("30034-07.htm"))
		{
			boolean isRewarded = false;
			for (int i = 1852; i < 1856; i++)
			{
				final int n = st.getQuestItemsCount(i);
				if (n > 0)
				{
					st.takeItems(i, n);
					st.rewardExpAndSp(n * EXP_REWARD.get(i), 0);
					isRewarded = true;
				}
			}
			if (!isRewarded)
			{
				htmltext = "30034-02.htm";
			}
			else
			{
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		// Nestle
		else if (event.equals("30314-03.htm"))
		{
			if (!st.hasQuestItems(ANCIENT_CLAY_URN))
			{
				htmltext = "30314-07.htm";
			}
			else
			{
				st.takeItems(ANCIENT_CLAY_URN, 1);
				st.rewardItems(SOULSHOT_D, 70 + Rnd.get(41));
			}
		}
		else if (event.equals("30314-04.htm"))
		{
			if (!st.hasQuestItems(ANCIENT_BRASS_TIARA))
			{
				htmltext = "30314-07.htm";
			}
			else
			{
				st.takeItems(ANCIENT_BRASS_TIARA, 1);
				final int rnd = Rnd.get(100);
				if (rnd < 40)
				{
					st.rewardItems(HEALING_POTION, 1);
				}
				else if (rnd < 84)
				{
					st.rewardItems(HASTE_POTION, 1);
				}
				else
				{
					st.rewardItems(POTION_OF_ALACRITY, 1);
				}
			}
		}
		else if (event.equals("30314-05.htm"))
		{
			if (!st.hasQuestItems(ANCIENT_BRONZE_MIRROR))
			{
				htmltext = "30314-07.htm";
			}
			else
			{
				st.takeItems(ANCIENT_BRONZE_MIRROR, 1);
				st.rewardItems((Rnd.get(100) < 59) ? SCROLL_OF_ESCAPE : SCROLL_OF_RESURRECTION, 1);
			}
		}
		else if (event.equals("30314-06.htm"))
		{
			if (!st.hasQuestItems(ANCIENT_JADE_NECKLACE))
			{
				htmltext = "30314-07.htm";
			}
			else
			{
				st.takeItems(ANCIENT_JADE_NECKLACE, 1);
				st.rewardItems(SPIRITSHOT_D, 50 + Rnd.get(41));
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
				htmltext = npc.getNpcId() + ((player.getLevel() < 25) ? "-01.htm" : "-02.htm");
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case PIOTUR:
						if (!st.hasQuestItems(LEIKAN_LETTER))
						{
							if (st.hasAtLeastOneQuestItem(TUREK_DOGTAG, TUREK_MEDALLION))
							{
								htmltext = "30597-05.htm";
								
								if (cond < 4)
								{
									st.set("cond", "4");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								
								final int dogtag = st.getQuestItemsCount(TUREK_DOGTAG);
								final int medallion = st.getQuestItemsCount(TUREK_MEDALLION);
								st.takeItems(TUREK_DOGTAG, -1);
								st.takeItems(TUREK_MEDALLION, -1);
								st.rewardItems(ADENA, (dogtag * 40) + (medallion * 50) + (((dogtag + medallion) >= 10) ? 619 : 0));
							}
							else
							{
								htmltext = "30597-04.htm";
							}
						}
						else
						{
							htmltext = "30597-03a.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LEIKAN_LETTER, 1);
						}
						break;
					
					case LEIKAN:
						if (cond == 2)
						{
							htmltext = "30382-04.htm";
						}
						else if ((cond == 3) || (cond == 4))
						{
							htmltext = "30382-05.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 5)
						{
							htmltext = "30382-05.htm";
						}
						break;
					
					default:
						htmltext = npc.getNpcId() + "-01.htm";
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
		
		for (int[] npcData : DROPLIST)
		{
			if (npcData[0] == npc.getNpcId())
			{
				st.dropItemsAlways(npcData[2], 1, -1);
				st.dropItems(Rnd.get(1848, 1851), 1, 0, npcData[1]);
				break;
			}
		}
		
		return null;
	}
}