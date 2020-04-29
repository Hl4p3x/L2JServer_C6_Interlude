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
package quests.Q115_TheOtherSideOfTruth;

import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q115_TheOtherSideOfTruth extends Quest
{
	// NPCs
	private static final int MISA = 32018;
	private static final int SUSPICIOUS = 32019;
	private static final int RAFFORTY = 32020;
	private static final int SCULPTURE1 = 32021;
	private static final int KIERRE = 32022;
	private static final int SCULPTURE2 = 32077;
	private static final int SCULPTURE3 = 32078;
	private static final int SCULPTURE4 = 32079;
	// Items
	private static final int LETTER = 8079;
	private static final int LETTER2 = 8080;
	private static final int TABLET = 8081;
	private static final int REPORT = 8082;
	
	public Q115_TheOtherSideOfTruth()
	{
		super(115, "The Other Side of Truth");
		
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, MISA, SCULPTURE1, SCULPTURE2, SCULPTURE3, SCULPTURE4, KIERRE);
		registerQuestItems(LETTER, LETTER2, TABLET, REPORT);
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
			case "32018-04.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "7");
				qs.takeItems(LETTER2, 1);
				break;
			}
			case "32020-02.htm":
			{
				qs.setState(State.STARTED);
				qs.playSound("ItemSound.quest_accept");
				qs.set("cond", "1");
				break;
			}
			case "32020-05.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "3");
				qs.takeItems(LETTER, 1);
				break;
			}
			case "32020-06.htm":
			case "32020-08a.htm":
			{
				qs.exitQuest(true);
				qs.playSound("ItemSound.quest_finish");
				break;
			}
			case "32020-08.htm":
			case "32020-07a.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "4");
				break;
			}
			case "32020-12.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "5");
				break;
			}
			case "32020-16.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "10");
				qs.takeItems(REPORT, 1);
				break;
			}
			case "32020-18.htm":
			{
				if (qs.getQuestItemsCount(TABLET) == 0)
				{
					qs.playSound("ItemSound.quest_middle");
					qs.set("cond", "11");
					htmltext = "32020-19.htm";
				}
				else
				{
					qs.exitQuest(false);
					qs.playSound("ItemSound.quest_finish");
					qs.giveItems(57, 115673);
					qs.rewardExpAndSp(493595, 40442);
				}
				break;
			}
			case "32020-19.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "11");
				break;
			}
			case "32022-02.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "9");
				final NpcInstance man = qs.addSpawn(SUSPICIOUS, 104562, -107598, -3688, 0, false, 4000);
				man.broadcastPacket(new CreatureSay(man.getObjectId(), ChatType.GENERAL, man.getName(), "We meet again."));
				startQuestTimer("2", 3700, man, player);
				qs.giveItems(REPORT, 1);
				break;
			}
			case "Sculpture-04.htm":
			{
				qs.set("talk", "1");
				htmltext = "Sculpture-05.htm";
				qs.set("" + npc.getNpcId(), "1");
				break;
			}
			case "Sculpture-04a.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "8");
				final NpcInstance man = qs.addSpawn(SUSPICIOUS, 117890, -126478, -2584, 0, false, 4000);
				man.broadcastPacket(new CreatureSay(man.getObjectId(), ChatType.GENERAL, man.getName(), "This looks like the right place..."));
				startQuestTimer("1", 3700, man, player);
				htmltext = "Sculpture-04.htm";
				if ((qs.getInt("" + SCULPTURE1) == 0) && (qs.getInt("" + SCULPTURE2) == 0))
				{
					qs.giveItems(TABLET, 1);
				}
				break;
			}
			case "Sculpture-05.htm":
			{
				qs.set("" + npc.getNpcId(), "1");
				break;
			}
			case "1":
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "I see someone. Is this fate?"));
				break;
			}
			case "2":
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "Don't bother trying to find out more about me. Follow your own destiny."));
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState qs = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
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
		else if (npcId == RAFFORTY)
		{
			if (state == State.CREATED)
			{
				if (qs.getPlayer().getLevel() >= 53)
				{
					htmltext = "32020-01.htm";
				}
				else
				{
					htmltext = "32020-00.htm";
					qs.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "32020-03.htm";
			}
			else if (cond == 2)
			{
				htmltext = "32020-04.htm";
			}
			else if (cond == 3)
			{
				htmltext = "32020-05.htm";
			}
			else if (cond == 4)
			{
				htmltext = "32020-11.htm";
			}
			else if (cond == 5)
			{
				htmltext = "32020-13.htm";
				qs.playSound("ItemSound.quest_middle");
				qs.giveItems(LETTER2, 1);
				qs.set("cond", "6");
			}
			else if (cond == 6)
			{
				htmltext = "32020-14.htm";
			}
			else if (cond == 9)
			{
				htmltext = "32020-15.htm";
			}
			else if (cond == 10)
			{
				htmltext = "32020-17.htm";
			}
			else if (cond == 11)
			{
				htmltext = "32020-20.htm";
			}
			else if (cond == 12)
			{
				htmltext = "32020-18.htm";
				qs.exitQuest(false);
				qs.playSound("ItemSound.quest_finish");
				qs.giveItems(57, 60044);
			}
		}
		else if (npcId == MISA)
		{
			if (cond == 1)
			{
				htmltext = "32018-01.htm";
				qs.giveItems(LETTER, 1);
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "2");
			}
			else if (cond == 2)
			{
				htmltext = "32018-02.htm";
			}
			else if (cond == 6)
			{
				htmltext = "32018-03.htm";
			}
			else if (cond == 7)
			{
				htmltext = "32018-05.htm";
			}
		}
		else if (npcId == SCULPTURE1)
		{
			if (cond == 7)
			{
				if (qs.getInt("" + npcId) == 1)
				{
					htmltext = "Sculpture-02.htm";
				}
				else if (qs.getInt("talk") == 1)
				{
					htmltext = "Sculpture-06.htm";
				}
				else
				{
					htmltext = "Sculpture-03.htm";
				}
			}
			else if (cond == 8)
			{
				htmltext = "Sculpture-04.htm";
			}
			else if (cond == 11)
			{
				qs.giveItems(TABLET, 1);
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "12");
				htmltext = "Sculpture-07.htm";
			}
			else if (cond == 12)
			{
				htmltext = "Sculpture-08.htm";
			}
		}
		else if (npcId == SCULPTURE2)
		{
			if (cond == 7)
			{
				if (qs.getInt("" + npcId) == 1)
				{
					htmltext = "Sculpture-02.htm";
				}
				else if (qs.getInt("talk") == 1)
				{
					htmltext = "Sculpture-06.htm";
				}
				else
				{
					htmltext = "Sculpture-03.htm";
				}
			}
			else if (cond == 8)
			{
				htmltext = "Sculpture-04.htm";
			}
			else if (cond == 11)
			{
				qs.giveItems(TABLET, 1);
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "12");
				htmltext = "Sculpture-07.htm";
			}
			else if (cond == 12)
			{
				htmltext = "Sculpture-08.htm";
			}
		}
		else if (npcId == SCULPTURE3)
		{
			if (cond == 7)
			{
				if (qs.getInt("" + npcId) == 1)
				{
					htmltext = "Sculpture-02.htm";
				}
				else
				{
					htmltext = "Sculpture-01.htm";
					qs.set("" + npcId, "1");
				}
			}
			else if (cond == 8)
			{
				htmltext = "Sculpture-04.htm";
			}
			else if (cond == 11)
			{
				qs.giveItems(TABLET, 1);
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "12");
				htmltext = "Sculpture-07.htm";
			}
			else if (cond == 12)
			{
				htmltext = "Sculpture-08.htm";
			}
		}
		else if (npcId == SCULPTURE4)
		{
			if (cond == 7)
			{
				if (qs.getInt("" + npcId) == 1)
				{
					htmltext = "Sculpture-02.htm";
				}
				else
				{
					htmltext = "Sculpture-01.htm";
					qs.set("" + npcId, "1");
				}
			}
			else if (cond == 8)
			{
				htmltext = "Sculpture-04.htm";
			}
			else if (cond == 11)
			{
				qs.giveItems(TABLET, 1);
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "12");
				htmltext = "Sculpture-07.htm";
			}
			else if (cond == 12)
			{
				htmltext = "Sculpture-08.htm";
			}
		}
		else if (npcId == KIERRE)
		{
			if (cond == 8)
			{
				htmltext = "32022-01.htm";
			}
			else if (cond == 9)
			{
				htmltext = "32022-03.htm";
			}
		}
		
		return htmltext;
	}
}