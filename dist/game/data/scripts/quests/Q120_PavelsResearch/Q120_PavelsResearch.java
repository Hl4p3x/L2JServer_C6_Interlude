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
package quests.Q120_PavelsResearch;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

import quests.Q114_ResurrectionOfAnOldManager.Q114_ResurrectionOfAnOldManager;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q120_PavelsResearch extends Quest
{
	// NPCs
	private static final int YUMI = 32041;
	private static final int WEATHER1 = 32042; // north
	private static final int WEATHER2 = 32043; // east
	private static final int WEATHER3 = 32044; // west
	private static final int BOOKSHELF = 32045;
	private static final int STONES = 32046;
	private static final int WENDY = 32047;
	// Items
	private static final int EAR_BINDING = 854;
	private static final int REPORT = 8058;
	private static final int REPORT2 = 8059;
	private static final int ENIGMA = 8060;
	private static final int FLOWER = 8290;
	private static final int HEART = 8291;
	private static final int NECKLACE = 8292;
	
	public Q120_PavelsResearch()
	{
		super(120, "Pavel's Research");
		
		addStartNpc(STONES);
		addTalkId(BOOKSHELF, STONES, WEATHER1, WEATHER2, WEATHER3, WENDY, YUMI);
		registerQuestItems(FLOWER, REPORT, REPORT2, ENIGMA, HEART, NECKLACE);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "32041-03.htm":
			{
				qs.set("cond", "3");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32041-04.htm":
			{
				qs.set("cond", "4");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32041-12.htm":
			{
				qs.set("cond", "8");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32041-16.htm":
			{
				qs.set("cond", "16");
				qs.giveItems(ENIGMA, 1);
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32041-22.htm":
			{
				qs.set("cond", "17");
				qs.takeItems(ENIGMA, 1);
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32041-32.htm":
			{
				qs.takeItems(NECKLACE, 1);
				qs.giveItems(EAR_BINDING, 1);
				qs.exitQuest(true);
				qs.playSound("ItemSound.quest_finish");
				break;
			}
			case "32042-06.htm":
			{
				if (qs.getInt("cond") == 10)
				{
					if ((qs.getInt("talk") + qs.getInt("talk1")) == 2)
					{
						qs.set("cond", "11");
						qs.set("talk", "0");
						qs.set("talk1", "0");
						qs.playSound("ItemSound.quest_middle");
					}
					else
					{
						htmltext = "32042-03.htm";
					}
				}
				break;
			}
			case "32042-10.htm":
			{
				if ((qs.getInt("talk") + qs.getInt("talk1") + qs.getInt("talk2")) == 3)
				{
					htmltext = "32042-14.htm";
				}
				break;
			}
			case "32042-11.htm":
			{
				if (qs.getInt("talk") == 0)
				{
					qs.set("talk", "1");
				}
				break;
			}
			case "32042-12.htm":
			{
				if (qs.getInt("talk1") == 0)
				{
					qs.set("talk1", "1");
				}
				break;
			}
			case "32042-13.htm":
			{
				if (qs.getInt("talk2") == 0)
				{
					qs.set("talk2", "1");
				}
				break;
			}
			case "32042-15.htm":
			{
				qs.set("cond", "12");
				qs.set("talk", "0");
				qs.set("talk1", "0");
				qs.set("talk2", "0");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32043-06.htm":
			{
				if (qs.getInt("cond") == 17)
				{
					if ((qs.getInt("talk") + qs.getInt("talk1")) == 2)
					{
						qs.set("cond", "18");
						qs.set("talk", "0");
						qs.set("talk1", "0");
						qs.playSound("ItemSound.quest_middle");
					}
					else
					{
						htmltext = "32043-03.htm";
					}
				}
				break;
			}
			case "32043-15.htm":
			{
				if ((qs.getInt("talk") + qs.getInt("talk1")) == 2)
				{
					htmltext = "32043-29.htm";
				}
				break;
			}
			case "32043-18.htm":
			{
				if (qs.getInt("talk") == 1)
				{
					htmltext = "32043-21.htm";
				}
				break;
			}
			case "32043-20.htm":
			{
				qs.set("talk", "1");
				qs.playSound("AmbSound.ed_drone_02");
				break;
			}
			case "32043-28.htm":
			{
				qs.set("talk1", "1");
				break;
			}
			case "32043-30.htm":
			{
				qs.set("cond", "19");
				qs.set("talk", "0");
				qs.set("talk1", "0");
				break;
			}
			case "32044-06.htm":
			{
				if (qs.getInt("cond") == 20)
				{
					if ((qs.getInt("talk") + qs.getInt("talk1")) == 2)
					{
						qs.set("cond", "21");
						qs.set("talk", "0");
						qs.set("talk1", "0");
						qs.playSound("ItemSound.quest_middle");
					}
					else
					{
						htmltext = "32044-03.htm";
					}
				}
				break;
			}
			case "32044-08.htm":
			{
				if ((qs.getInt("talk") + qs.getInt("talk1")) == 2)
				{
					htmltext = "32044-11.htm";
				}
				break;
			}
			case "32044-09.htm":
			{
				if (qs.getInt("talk") == 0)
				{
					qs.set("talk", "1");
				}
				break;
			}
			case "32044-10.htm":
			{
				if (qs.getInt("talk1") == 0)
				{
					qs.set("talk1", "1");
				}
				break;
			}
			case "32044-17.htm":
			{
				qs.set("cond", "22");
				qs.set("talk", "0");
				qs.set("talk1", "0");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32045-02.htm":
			{
				qs.set("cond", "15");
				qs.playSound("ItemSound.quest_middle");
				qs.giveItems(REPORT, 1);
				npc.broadcastPacket(new MagicSkillUse(npc, qs.getPlayer(), 5073, 5, 1500, 0));
				break;
			}
			case "32046-04.htm":
			case "32046-05.htm":
			{
				qs.exitQuest(true);
				break;
			}
			case "32046-06.htm":
			{
				if (qs.getPlayer().getLevel() >= 50)
				{
					qs.setState(State.STARTED);
					qs.playSound("ItemSound.quest_accept");
					qs.set("cond", "1");
				}
				else
				{
					htmltext = "32046-00.htm";
					qs.exitQuest(true);
				}
				break;
			}
			case "32046-08.htm":
			{
				qs.set("cond", "2");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32046-12.htm":
			{
				qs.set("cond", "6");
				qs.playSound("ItemSound.quest_middle");
				qs.giveItems(FLOWER, 1);
				break;
			}
			case "32046-22.htm":
			{
				qs.set("cond", "10");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32046-29.htm":
			{
				qs.set("cond", "13");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32046-35.htm":
			{
				qs.set("cond", "20");
				qs.playSound("ItemSou;nd.quest_middle");
				break;
			}
			case "32046-38.htm":
			{
				qs.set("cond", "23");
				qs.playSound("ItemSound.quest_middle");
				qs.giveItems(HEART, 1);
				break;
			}
			case "32047-06.htm":
			{
				qs.set("cond", "5");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32047-10.htm":
			{
				qs.set("cond", "7");
				qs.playSound("ItemSound.quest_middle");
				qs.takeItems(FLOWER, 1);
				break;
			}
			case "32047-15.htm":
			{
				qs.set("cond", "9");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32047-18.htm":
			{
				qs.set("cond", "14");
				qs.playSound("ItemSound.quest_middle");
				break;
			}
			case "32047-26.htm":
			{
				qs.set("cond", "24");
				qs.playSound("ItemSound.quest_middle");
				qs.takeItems(HEART, 1);
				break;
			}
			case "32047-32.htm":
			{
				qs.set("cond", "25");
				qs.playSound("ItemSound.quest_middle");
				qs.giveItems(NECKLACE, 1);
				break;
			}
			case "w1_1":
			{
				qs.set("talk", "1");
				htmltext = "32042-04.htm";
				break;
			}
			case "w1_2":
			{
				qs.set("talk1", "1");
				htmltext = "32042-05.htm";
				break;
			}
			case "w2_1":
			{
				qs.set("talk", "1");
				htmltext = "32043-04.htm";
				break;
			}
			case "w2_2":
			{
				qs.set("talk1", "1");
				htmltext = "32043-05.htm";
				break;
			}
			case "w3_1":
			{
				qs.set("talk", "1");
				htmltext = "32044-04.htm";
				break;
			}
			case "w3_2":
			{
				qs.set("talk1", "1");
				htmltext = "32044-05.htm";
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		final int state = qs.getState();
		final int npcId = npc.getNpcId();
		final int cond = qs.getInt("cond");
		if (state == State.COMPLETED)
		{
			htmltext = getAlreadyCompletedMsg();
		}
		else if (npcId == STONES)
		{
			if (state == State.CREATED)
			{
				final QuestState qs2 = player.getQuestState(Q114_ResurrectionOfAnOldManager.class.getSimpleName());
				if (qs2 != null)
				{
					if ((player.getLevel() >= 49) && (qs2.getState() == State.COMPLETED))
					{
						htmltext = "32046-01.htm";
					}
					else
					{
						htmltext = "32046-00.htm";
						qs.exitQuest(true);
					}
				}
				else
				{
					htmltext = "32046-00.htm";
					qs.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "32046-06.htm";
			}
			else if (cond == 2)
			{
				htmltext = "32046-09.htm";
			}
			else if (cond == 5)
			{
				htmltext = "32046-10.htm";
			}
			else if (cond == 6)
			{
				htmltext = "32046-13.htm";
			}
			else if (cond == 9)
			{
				htmltext = "32046-14.htm";
			}
			else if (cond == 10)
			{
				htmltext = "32046-23.htm";
			}
			else if (cond == 12)
			{
				htmltext = "32046-26.htm";
			}
			else if (cond == 13)
			{
				htmltext = "32046-30.htm";
			}
			else if (cond == 19)
			{
				htmltext = "32046-31.htm";
			}
			else if (cond == 20)
			{
				htmltext = "32046-36.htm";
			}
			else if (cond == 22)
			{
				htmltext = "32046-37.htm";
			}
			else if (cond == 23)
			{
				htmltext = "32046-39.htm";
			}
		}
		else if (npcId == WENDY)
		{
			if ((cond >= 2) && (cond <= 4))
			{
				htmltext = "32047-01.htm";
			}
			else if (cond == 5)
			{
				htmltext = "32047-07.htm";
			}
			else if (cond == 6)
			{
				htmltext = "32047-08.htm";
			}
			else if (cond == 7)
			{
				htmltext = "32047-11.htm";
			}
			else if (cond == 8)
			{
				htmltext = "32047-12.htm";
			}
			else if (cond == 9)
			{
				htmltext = "32047-15.htm";
			}
			else if (cond == 13)
			{
				htmltext = "32047-16.htm";
			}
			else if (cond == 14)
			{
				htmltext = "32047-19.htm";
			}
			else if (cond == 15)
			{
				htmltext = "32047-20.htm";
			}
			else if (cond == 23)
			{
				htmltext = "32047-21.htm";
			}
			else if (cond == 24)
			{
				htmltext = "32047-26.htm";
			}
			else if (cond == 25)
			{
				htmltext = "32047-33.htm";
			}
		}
		else if (npcId == YUMI)
		{
			if (cond == 2)
			{
				htmltext = "32041-01.htm";
			}
			else if (cond == 3)
			{
				htmltext = "32041-05.htm";
			}
			else if (cond == 4)
			{
				htmltext = "32041-06.htm";
			}
			else if (cond == 7)
			{
				htmltext = "32041-07.htm";
			}
			else if (cond == 8)
			{
				htmltext = "32041-13.htm";
			}
			else if (cond == 15)
			{
				htmltext = "32041-14.htm";
			}
			else if (cond == 16)
			{
				if (qs.getQuestItemsCount(REPORT2) == 0)
				{
					htmltext = "32041-17.htm";
				}
				else
				{
					htmltext = "32041-18.htm";
				}
			}
			else if (cond == 17)
			{
				htmltext = "32041-22.htm";
			}
			else if (cond == 25)
			{
				htmltext = "32041-26.htm";
			}
		}
		else if (npcId == WEATHER1)
		{
			if (cond == 10)
			{
				htmltext = "32042-01.htm";
			}
			else if (cond == 11)
			{
				if ((qs.getInt("talk") + qs.getInt("talk1") + qs.getInt("talk2")) == 3)
				{
					htmltext = "32042-14.htm";
				}
				else
				{
					htmltext = "32042-06.htm";
				}
			}
			else if (cond == 12)
			{
				htmltext = "32042-15.htm";
			}
		}
		else if (npcId == WEATHER2)
		{
			if (cond == 17)
			{
				htmltext = "32043-01.htm";
			}
			else if (cond == 18)
			{
				if ((qs.getInt("talk") + qs.getInt("talk1")) == 2)
				{
					htmltext = "32043-29.htm";
				}
				else
				{
					htmltext = "32043-06.htm";
				}
			}
			else if (cond == 19)
			{
				htmltext = "32043-30.htm";
			}
		}
		else if (npcId == WEATHER3)
		{
			if (cond == 20)
			{
				htmltext = "32044-01.htm";
			}
			else if (cond == 21)
			{
				htmltext = "32044-06.htm";
			}
			else if (cond == 22)
			{
				htmltext = "32044-18.htm";
			}
		}
		else if (npcId == BOOKSHELF)
		{
			if (cond == 14)
			{
				htmltext = "32045-01.htm";
			}
			else if (cond == 15)
			{
				htmltext = "32045-03.htm";
			}
		}
		return htmltext;
	}
}