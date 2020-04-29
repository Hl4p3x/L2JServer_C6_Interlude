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
package quests.Q605_AllianceWithKetraOrcs;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q606_WarWithVarkaSilenos.Q606_WarWithVarkaSilenos;

/**
 * This quest supports both Q605 && Q606 onKill sections.
 */
public class Q605_AllianceWithKetraOrcs extends Quest
{
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(21350, 500000);
		CHANCES.put(21351, 500000);
		CHANCES.put(21353, 509000);
		CHANCES.put(21354, 521000);
		CHANCES.put(21355, 519000);
		CHANCES.put(21357, 500000);
		CHANCES.put(21358, 500000);
		CHANCES.put(21360, 509000);
		CHANCES.put(21361, 518000);
		CHANCES.put(21362, 518000);
		CHANCES.put(21364, 527000);
		CHANCES.put(21365, 500000);
		CHANCES.put(21366, 500000);
		CHANCES.put(21368, 508000);
		CHANCES.put(21369, 628000);
		CHANCES.put(21370, 604000);
		CHANCES.put(21371, 627000);
		CHANCES.put(21372, 604000);
		CHANCES.put(21373, 649000);
		CHANCES.put(21374, 626000);
		CHANCES.put(21375, 626000);
	}
	
	private static final Map<Integer, Integer> CHANCES_MANE = new HashMap<>();
	static
	{
		CHANCES_MANE.put(21350, 500000);
		CHANCES_MANE.put(21353, 510000);
		CHANCES_MANE.put(21354, 522000);
		CHANCES_MANE.put(21355, 519000);
		CHANCES_MANE.put(21357, 529000);
		CHANCES_MANE.put(21358, 529000);
		CHANCES_MANE.put(21360, 539000);
		CHANCES_MANE.put(21362, 548000);
		CHANCES_MANE.put(21364, 558000);
		CHANCES_MANE.put(21365, 568000);
		CHANCES_MANE.put(21366, 568000);
		CHANCES_MANE.put(21368, 568000);
		CHANCES_MANE.put(21369, 664000);
		CHANCES_MANE.put(21371, 713000);
		CHANCES_MANE.put(21373, 738000);
	}
	
	// Quest Items
	private static final int VARKA_BADGE_SOLDIER = 7216;
	private static final int VARKA_BADGE_OFFICER = 7217;
	private static final int VARKA_BADGE_CAPTAIN = 7218;
	
	private static final int KETRA_ALLIANCE_1 = 7211;
	private static final int KETRA_ALLIANCE_2 = 7212;
	private static final int KETRA_ALLIANCE_3 = 7213;
	private static final int KETRA_ALLIANCE_4 = 7214;
	private static final int KETRA_ALLIANCE_5 = 7215;
	
	private static final int TOTEM_OF_VALOR = 7219;
	private static final int TOTEM_OF_WISDOM = 7220;
	
	private static final int VARKA_MANE = 7233;
	
	public Q605_AllianceWithKetraOrcs()
	{
		super(605, "Alliance with Ketra Orcs");
		
		registerQuestItems(VARKA_BADGE_SOLDIER, VARKA_BADGE_OFFICER, VARKA_BADGE_CAPTAIN);
		
		addStartNpc(31371); // Wahkan
		addTalkId(31371);
		
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
		
		if (event.equals("31371-03a.htm"))
		{
			if (player.isAlliedWithVarka())
			{
				htmltext = "31371-02a.htm";
			}
			else
			{
				st.setState(State.STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
				for (int i = KETRA_ALLIANCE_1; i <= KETRA_ALLIANCE_5; i++)
				{
					if (st.hasQuestItems(i))
					{
						st.set("cond", String.valueOf(i - 7209));
						player.setAllianceWithVarkaKetra(i - 7210);
						return "31371-0" + (i - 7207) + ".htm";
					}
				}
				st.set("cond", "1");
			}
		}
		// Stage 1
		else if (event.equals("31371-10-1.htm"))
		{
			if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 100)
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(VARKA_BADGE_SOLDIER, -1);
				st.giveItems(KETRA_ALLIANCE_1, 1);
				player.setAllianceWithVarkaKetra(1);
			}
			else
			{
				htmltext = "31371-03b.htm";
			}
		}
		// Stage 2
		else if (event.equals("31371-10-2.htm"))
		{
			if ((st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 200) && (st.getQuestItemsCount(VARKA_BADGE_OFFICER) >= 100))
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(VARKA_BADGE_SOLDIER, -1);
				st.takeItems(VARKA_BADGE_OFFICER, -1);
				st.takeItems(KETRA_ALLIANCE_1, -1);
				st.giveItems(KETRA_ALLIANCE_2, 1);
				player.setAllianceWithVarkaKetra(2);
			}
			else
			{
				htmltext = "31371-12.htm";
			}
		}
		// Stage 3
		else if (event.equals("31371-10-3.htm"))
		{
			if ((st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 300) && (st.getQuestItemsCount(VARKA_BADGE_OFFICER) >= 200) && (st.getQuestItemsCount(VARKA_BADGE_CAPTAIN) >= 100))
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(VARKA_BADGE_SOLDIER, -1);
				st.takeItems(VARKA_BADGE_OFFICER, -1);
				st.takeItems(VARKA_BADGE_CAPTAIN, -1);
				st.takeItems(KETRA_ALLIANCE_2, -1);
				st.giveItems(KETRA_ALLIANCE_3, 1);
				player.setAllianceWithVarkaKetra(3);
			}
			else
			{
				htmltext = "31371-15.htm";
			}
		}
		// Stage 4
		else if (event.equals("31371-10-4.htm"))
		{
			if ((st.getQuestItemsCount(VARKA_BADGE_SOLDIER) >= 300) && (st.getQuestItemsCount(VARKA_BADGE_OFFICER) >= 300) && (st.getQuestItemsCount(VARKA_BADGE_CAPTAIN) >= 200) && (st.getQuestItemsCount(TOTEM_OF_VALOR) >= 1))
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(VARKA_BADGE_SOLDIER, -1);
				st.takeItems(VARKA_BADGE_OFFICER, -1);
				st.takeItems(VARKA_BADGE_CAPTAIN, -1);
				st.takeItems(TOTEM_OF_VALOR, -1);
				st.takeItems(KETRA_ALLIANCE_3, -1);
				st.giveItems(KETRA_ALLIANCE_4, 1);
				player.setAllianceWithVarkaKetra(4);
			}
			else
			{
				htmltext = "31371-21.htm";
			}
		}
		// Leave quest
		else if (event.equals("31371-20.htm"))
		{
			st.takeItems(KETRA_ALLIANCE_1, -1);
			st.takeItems(KETRA_ALLIANCE_2, -1);
			st.takeItems(KETRA_ALLIANCE_3, -1);
			st.takeItems(KETRA_ALLIANCE_4, -1);
			st.takeItems(KETRA_ALLIANCE_5, -1);
			st.takeItems(TOTEM_OF_VALOR, -1);
			st.takeItems(TOTEM_OF_WISDOM, -1);
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
					htmltext = "31371-01.htm";
				}
				else
				{
					htmltext = "31371-02b.htm";
					st.exitQuest(true);
					player.setAllianceWithVarkaKetra(0);
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 100)
					{
						htmltext = "31371-03b.htm";
					}
					else
					{
						htmltext = "31371-09.htm";
					}
				}
				else if (cond == 2)
				{
					if ((st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 200) || (st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 100))
					{
						htmltext = "31371-12.htm";
					}
					else
					{
						htmltext = "31371-13.htm";
					}
				}
				else if (cond == 3)
				{
					if ((st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 300) || (st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 200) || (st.getQuestItemsCount(VARKA_BADGE_CAPTAIN) < 100))
					{
						htmltext = "31371-15.htm";
					}
					else
					{
						htmltext = "31371-16.htm";
					}
				}
				else if (cond == 4)
				{
					if ((st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 300) || (st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 300) || (st.getQuestItemsCount(VARKA_BADGE_CAPTAIN) < 200) || !st.hasQuestItems(TOTEM_OF_VALOR))
					{
						htmltext = "31371-21.htm";
					}
					else
					{
						htmltext = "31371-22.htm";
					}
				}
				else if (cond == 5)
				{
					if ((st.getQuestItemsCount(VARKA_BADGE_SOLDIER) < 400) || (st.getQuestItemsCount(VARKA_BADGE_OFFICER) < 400) || (st.getQuestItemsCount(VARKA_BADGE_CAPTAIN) < 200) || !st.hasQuestItems(TOTEM_OF_WISDOM))
					{
						htmltext = "31371-17.htm";
					}
					else
					{
						htmltext = "31371-10-5.htm";
						st.set("cond", "6");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(VARKA_BADGE_SOLDIER, 400);
						st.takeItems(VARKA_BADGE_OFFICER, 400);
						st.takeItems(VARKA_BADGE_CAPTAIN, 200);
						st.takeItems(TOTEM_OF_WISDOM, -1);
						st.takeItems(KETRA_ALLIANCE_4, -1);
						st.giveItems(KETRA_ALLIANCE_5, 1);
						player.setAllianceWithVarkaKetra(5);
					}
				}
				else if (cond == 6)
				{
					htmltext = "31371-08.htm";
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
		
		// Support for Q606.
		QuestState st = partyMember.getQuestState(Q606_WarWithVarkaSilenos.class.getSimpleName());
		if ((st != null) && Rnd.nextBoolean() && CHANCES_MANE.containsKey(npcId))
		{
			st.dropItems(VARKA_MANE, 1, 0, CHANCES_MANE.get(npcId));
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
			case 21350:
			case 21351:
			case 21353:
			case 21354:
			case 21355:
				if (cond == 1)
				{
					st.dropItems(VARKA_BADGE_SOLDIER, 1, 100, CHANCES.get(npcId));
				}
				else if (cond == 2)
				{
					st.dropItems(VARKA_BADGE_SOLDIER, 1, 200, CHANCES.get(npcId));
				}
				else if ((cond == 3) || (cond == 4))
				{
					st.dropItems(VARKA_BADGE_SOLDIER, 1, 300, CHANCES.get(npcId));
				}
				else if (cond == 5)
				{
					st.dropItems(VARKA_BADGE_SOLDIER, 1, 400, CHANCES.get(npcId));
				}
				break;
			
			case 21357:
			case 21358:
			case 21360:
			case 21361:
			case 21362:
			case 21364:
			case 21369:
			case 21370:
				if (cond == 2)
				{
					st.dropItems(VARKA_BADGE_OFFICER, 1, 100, CHANCES.get(npcId));
				}
				else if (cond == 3)
				{
					st.dropItems(VARKA_BADGE_OFFICER, 1, 200, CHANCES.get(npcId));
				}
				else if (cond == 4)
				{
					st.dropItems(VARKA_BADGE_OFFICER, 1, 300, CHANCES.get(npcId));
				}
				else if (cond == 5)
				{
					st.dropItems(VARKA_BADGE_OFFICER, 1, 400, CHANCES.get(npcId));
				}
				break;
			
			case 21365:
			case 21366:
			case 21368:
			case 21371:
			case 21372:
			case 21373:
			case 21374:
			case 21375:
				if (cond == 3)
				{
					st.dropItems(VARKA_BADGE_CAPTAIN, 1, 100, CHANCES.get(npcId));
				}
				else if ((cond == 4) || (cond == 5))
				{
					st.dropItems(VARKA_BADGE_CAPTAIN, 1, 200, CHANCES.get(npcId));
				}
				break;
		}
		
		return null;
	}
}