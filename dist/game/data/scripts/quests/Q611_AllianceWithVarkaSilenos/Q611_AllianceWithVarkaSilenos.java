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
package quests.Q611_AllianceWithVarkaSilenos;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q612_WarWithKetraOrcs.Q612_WarWithKetraOrcs;

/**
 * This quest supports both Q611 && Q612 onKill sections.
 */
public class Q611_AllianceWithVarkaSilenos extends Quest
{
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(21324, 500000);
		CHANCES.put(21325, 500000);
		CHANCES.put(21327, 509000);
		CHANCES.put(21328, 521000);
		CHANCES.put(21329, 519000);
		CHANCES.put(21331, 500000);
		CHANCES.put(21332, 500000);
		CHANCES.put(21334, 509000);
		CHANCES.put(21335, 518000);
		CHANCES.put(21336, 518000);
		CHANCES.put(21338, 527000);
		CHANCES.put(21339, 500000);
		CHANCES.put(21340, 500000);
		CHANCES.put(21342, 508000);
		CHANCES.put(21343, 628000);
		CHANCES.put(21344, 604000);
		CHANCES.put(21345, 627000);
		CHANCES.put(21346, 604000);
		CHANCES.put(21347, 649000);
		CHANCES.put(21348, 626000);
		CHANCES.put(21349, 626000);
	}
	
	private static final Map<Integer, Integer> CHANCES_MOLAR = new HashMap<>();
	static
	{
		CHANCES_MOLAR.put(21324, 500000);
		CHANCES_MOLAR.put(21327, 510000);
		CHANCES_MOLAR.put(21328, 522000);
		CHANCES_MOLAR.put(21329, 519000);
		CHANCES_MOLAR.put(21331, 529000);
		CHANCES_MOLAR.put(21332, 529000);
		CHANCES_MOLAR.put(21334, 539000);
		CHANCES_MOLAR.put(21336, 548000);
		CHANCES_MOLAR.put(21338, 558000);
		CHANCES_MOLAR.put(21339, 568000);
		CHANCES_MOLAR.put(21340, 568000);
		CHANCES_MOLAR.put(21342, 578000);
		CHANCES_MOLAR.put(21343, 664000);
		CHANCES_MOLAR.put(21345, 713000);
		CHANCES_MOLAR.put(21347, 738000);
	}
	
	// Quest Items
	private static final int KETRA_BADGE_SOLDIER = 7226;
	private static final int KETRA_BADGE_OFFICER = 7227;
	private static final int KETRA_BADGE_CAPTAIN = 7228;
	
	private static final int VARKA_ALLIANCE_1 = 7221;
	private static final int VARKA_ALLIANCE_2 = 7222;
	private static final int VARKA_ALLIANCE_3 = 7223;
	private static final int VARKA_ALLIANCE_4 = 7224;
	private static final int VARKA_ALLIANCE_5 = 7225;
	
	private static final int VALOR_FEATHER = 7229;
	private static final int WISDOM_FEATHER = 7230;
	
	private static final int MOLAR_OF_KETRA_ORC = 7234;
	
	public Q611_AllianceWithVarkaSilenos()
	{
		super(611, "Alliance with Varka Silenos");
		
		registerQuestItems(KETRA_BADGE_SOLDIER, KETRA_BADGE_OFFICER, KETRA_BADGE_CAPTAIN);
		
		addStartNpc(31378); // Naran Ashanuk
		addTalkId(31378);
		
		for (int mobs : CHANCES.keySet())
		{
			addKillId(mobs);
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
		
		if (event.equals("31378-03a.htm"))
		{
			if (player.isAlliedWithKetra())
			{
				htmltext = "31378-02a.htm";
			}
			else
			{
				st.setState(State.STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
				for (int i = VARKA_ALLIANCE_1; i <= VARKA_ALLIANCE_5; i++)
				{
					if (st.hasQuestItems(i))
					{
						st.set("cond", String.valueOf(i - 7219));
						player.setAllianceWithVarkaKetra(7220 - i);
						return "31378-0" + (i - 7217) + ".htm";
					}
				}
				st.set("cond", "1");
			}
		}
		// Stage 1
		else if (event.equals("31378-10-1.htm"))
		{
			if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 100)
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(KETRA_BADGE_SOLDIER, -1);
				st.giveItems(VARKA_ALLIANCE_1, 1);
				player.setAllianceWithVarkaKetra(-1);
			}
			else
			{
				htmltext = "31378-03b.htm";
			}
		}
		// Stage 2
		else if (event.equals("31378-10-2.htm"))
		{
			if ((st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 200) && (st.getQuestItemsCount(KETRA_BADGE_OFFICER) >= 100))
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(KETRA_BADGE_SOLDIER, -1);
				st.takeItems(KETRA_BADGE_OFFICER, -1);
				st.takeItems(VARKA_ALLIANCE_1, -1);
				st.giveItems(VARKA_ALLIANCE_2, 1);
				player.setAllianceWithVarkaKetra(-2);
			}
			else
			{
				htmltext = "31378-12.htm";
			}
		}
		// Stage 3
		else if (event.equals("31378-10-3.htm"))
		{
			if ((st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 300) && (st.getQuestItemsCount(KETRA_BADGE_OFFICER) >= 200) && (st.getQuestItemsCount(KETRA_BADGE_CAPTAIN) >= 100))
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(KETRA_BADGE_SOLDIER, -1);
				st.takeItems(KETRA_BADGE_OFFICER, -1);
				st.takeItems(KETRA_BADGE_CAPTAIN, -1);
				st.takeItems(VARKA_ALLIANCE_2, -1);
				st.giveItems(VARKA_ALLIANCE_3, 1);
				player.setAllianceWithVarkaKetra(-3);
			}
			else
			{
				htmltext = "31378-15.htm";
			}
		}
		// Stage 4
		else if (event.equals("31378-10-4.htm"))
		{
			if ((st.getQuestItemsCount(KETRA_BADGE_SOLDIER) >= 300) && (st.getQuestItemsCount(KETRA_BADGE_OFFICER) >= 300) && (st.getQuestItemsCount(KETRA_BADGE_CAPTAIN) >= 200) && (st.getQuestItemsCount(VALOR_FEATHER) >= 1))
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(KETRA_BADGE_SOLDIER, -1);
				st.takeItems(KETRA_BADGE_OFFICER, -1);
				st.takeItems(KETRA_BADGE_CAPTAIN, -1);
				st.takeItems(VALOR_FEATHER, -1);
				st.takeItems(VARKA_ALLIANCE_3, -1);
				st.giveItems(VARKA_ALLIANCE_4, 1);
				player.setAllianceWithVarkaKetra(-4);
			}
			else
			{
				htmltext = "31378-21.htm";
			}
		}
		// Leave quest
		else if (event.equals("31378-20.htm"))
		{
			st.takeItems(VARKA_ALLIANCE_1, -1);
			st.takeItems(VARKA_ALLIANCE_2, -1);
			st.takeItems(VARKA_ALLIANCE_3, -1);
			st.takeItems(VARKA_ALLIANCE_4, -1);
			st.takeItems(VARKA_ALLIANCE_5, -1);
			st.takeItems(VALOR_FEATHER, -1);
			st.takeItems(WISDOM_FEATHER, -1);
			player.setAllianceWithVarkaKetra(0);
			st.exitQuest(true);
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
				if (player.getLevel() >= 74)
				{
					htmltext = "31378-01.htm";
				}
				else
				{
					htmltext = "31378-02b.htm";
					st.exitQuest(true);
					player.setAllianceWithVarkaKetra(0);
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 100)
					{
						htmltext = "31378-03b.htm";
					}
					else
					{
						htmltext = "31378-09.htm";
					}
				}
				else if (cond == 2)
				{
					if ((st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 200) || (st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 100))
					{
						htmltext = "31378-12.htm";
					}
					else
					{
						htmltext = "31378-13.htm";
					}
				}
				else if (cond == 3)
				{
					if ((st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 300) || (st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 200) || (st.getQuestItemsCount(KETRA_BADGE_CAPTAIN) < 100))
					{
						htmltext = "31378-15.htm";
					}
					else
					{
						htmltext = "31378-16.htm";
					}
				}
				else if (cond == 4)
				{
					if ((st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 300) || (st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 300) || (st.getQuestItemsCount(KETRA_BADGE_CAPTAIN) < 200) || !st.hasQuestItems(VALOR_FEATHER))
					{
						htmltext = "31378-21.htm";
					}
					else
					{
						htmltext = "31378-22.htm";
					}
				}
				else if (cond == 5)
				{
					if ((st.getQuestItemsCount(KETRA_BADGE_SOLDIER) < 400) || (st.getQuestItemsCount(KETRA_BADGE_OFFICER) < 400) || (st.getQuestItemsCount(KETRA_BADGE_CAPTAIN) < 200) || !st.hasQuestItems(WISDOM_FEATHER))
					{
						htmltext = "31378-17.htm";
					}
					else
					{
						htmltext = "31378-10-5.htm";
						st.set("cond", "6");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(KETRA_BADGE_SOLDIER, 400);
						st.takeItems(KETRA_BADGE_OFFICER, 400);
						st.takeItems(KETRA_BADGE_CAPTAIN, 200);
						st.takeItems(WISDOM_FEATHER, -1);
						st.takeItems(VARKA_ALLIANCE_4, -1);
						st.giveItems(VARKA_ALLIANCE_5, 1);
						player.setAllianceWithVarkaKetra(-5);
					}
				}
				else if (cond == 6)
				{
					htmltext = "31378-08.htm";
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
		
		final int npcId = npc.getNpcId();
		
		// Support for Q612.
		QuestState st = partyMember.getQuestState(Q612_WarWithKetraOrcs.class.getSimpleName());
		if ((st != null) && Rnd.nextBoolean() && CHANCES_MOLAR.containsKey(npcId))
		{
			st.dropItems(MOLAR_OF_KETRA_ORC, 1, 0, CHANCES_MOLAR.get(npcId));
			return null;
		}
		
		st = partyMember.getQuestState(getName());
		
		final int cond = st.getInt("cond");
		if (cond == 6)
		{
			return null;
		}
		
		switch (npcId)
		{
			case 21324:
			case 21325:
			case 21327:
			case 21328:
			case 21329:
				if (cond == 1)
				{
					st.dropItems(KETRA_BADGE_SOLDIER, 1, 100, CHANCES.get(npcId));
				}
				else if (cond == 2)
				{
					st.dropItems(KETRA_BADGE_SOLDIER, 1, 200, CHANCES.get(npcId));
				}
				else if ((cond == 3) || (cond == 4))
				{
					st.dropItems(KETRA_BADGE_SOLDIER, 1, 300, CHANCES.get(npcId));
				}
				else if (cond == 5)
				{
					st.dropItems(KETRA_BADGE_SOLDIER, 1, 400, CHANCES.get(npcId));
				}
				break;
			
			case 21331:
			case 21332:
			case 21334:
			case 21335:
			case 21336:
			case 21338:
			case 21343:
			case 21344:
				if (cond == 2)
				{
					st.dropItems(KETRA_BADGE_OFFICER, 1, 100, CHANCES.get(npcId));
				}
				else if (cond == 3)
				{
					st.dropItems(KETRA_BADGE_OFFICER, 1, 200, CHANCES.get(npcId));
				}
				else if (cond == 4)
				{
					st.dropItems(KETRA_BADGE_OFFICER, 1, 300, CHANCES.get(npcId));
				}
				else if (cond == 5)
				{
					st.dropItems(KETRA_BADGE_OFFICER, 1, 400, CHANCES.get(npcId));
				}
				break;
			
			case 21339:
			case 21340:
			case 21342:
			case 21345:
			case 21346:
			case 21347:
			case 21348:
			case 21349:
				if (cond == 3)
				{
					st.dropItems(KETRA_BADGE_CAPTAIN, 1, 100, CHANCES.get(npcId));
				}
				else if ((cond == 4) || (cond == 5))
				{
					st.dropItems(KETRA_BADGE_CAPTAIN, 1, 200, CHANCES.get(npcId));
				}
				break;
		}
		
		return null;
	}
}