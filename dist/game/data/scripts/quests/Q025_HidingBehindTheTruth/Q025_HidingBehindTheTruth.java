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
package quests.Q025_HidingBehindTheTruth;

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;

import quests.Q024_InhabitantsOfTheForrestOfTheDead.Q024_InhabitantsOfTheForrestOfTheDead;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q025_HidingBehindTheTruth extends Quest
{
	// NPCs
	private static final int AGRIPEL = 31348;
	private static final int BENEDICT = 31349;
	private static final int WIZARD = 31522;
	private static final int TOMBSTONE = 31531;
	private static final int LIDIA = 31532;
	private static final int BOOKSHELF = 31533;
	private static final int BOOKSHELF2 = 31534;
	private static final int BOOKSHELF3 = 31535;
	private static final int COFFIN = 31536;
	private static final int TRIOL = 27218;
	// Items
	private static final int CONTRACT = 7066;
	private static final int DRESS = 7155;
	private static final int SUSPICIOUS_TOTEM = 7156;
	private static final int GEMSTONE_KEY = 7157;
	private static final int TOTEM_DOLL = 7158;
	
	public Q025_HidingBehindTheTruth()
	{
		super(25, "Hiding Behind the Truth");
		
		addStartNpc(BENEDICT);
		addTalkId(AGRIPEL, BENEDICT, BOOKSHELF, BOOKSHELF2, BOOKSHELF3, WIZARD, LIDIA, TOMBSTONE, COFFIN);
		addKillId(TRIOL);
		registerQuestItems(SUSPICIOUS_TOTEM, GEMSTONE_KEY, TOTEM_DOLL, DRESS);
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
			case "31349-02.htm":
			{
				qs.playSound("ItemSound.quest_accept");
				qs.set("cond", "1");
				qs.setState(State.STARTED);
				break;
			}
			case "31349-03.htm":
			{
				if (qs.getQuestItemsCount(SUSPICIOUS_TOTEM) > 0)
				{
					htmltext = "31349-05.htm";
				}
				else
				{
					qs.playSound("ItemSound.quest_middle");
					qs.set("cond", "2");
				}
				break;
			}
			case "31349-10.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "4");
				break;
			}
			case "31348-02.htm":
			{
				qs.takeItems(SUSPICIOUS_TOTEM, -1);
				break;
			}
			case "31348-07.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "5");
				qs.giveItems(GEMSTONE_KEY, 1);
				break;
			}
			case "31522-04.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "6");
				break;
			}
			case "31535-03.htm":
			{
				if (qs.getInt("step") == 0)
				{
					qs.set("step", "1");
					final NpcInstance triol = qs.addSpawn(TRIOL, 59712, -47568, -2712, 300000);
					triol.broadcastPacket(new CreatureSay(triol.getObjectId(), ChatType.GENERAL, triol.getName(), "That box was sealed by my master. Don't touch it!"));
					triol.setRunning();
					((Attackable) triol).addDamageHate(player, 0, 999);
					triol.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					qs.playSound("ItemSound.quest_middle");
					qs.set("cond", "7");
				}
				else if (qs.getInt("step") == 2)
				{
					htmltext = "31535-04.htm";
				}
				break;
			}
			case "31535-05.htm":
			{
				qs.giveItems(CONTRACT, 1);
				qs.takeItems(GEMSTONE_KEY, -1);
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "9");
				break;
			}
			case "31532-02.htm":
			{
				qs.takeItems(CONTRACT, -1);
				break;
			}
			case "31532-06.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "11");
				break;
			}
			case "31531-02.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "12");
				qs.addSpawn(COFFIN, 60104, -35820, -664, 20000);
				break;
			}
			case "31532-18.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "15");
				break;
			}
			case "31522-12.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "16");
			}
				break;
			case "31348-10.htm":
			{
				qs.takeItems(TOTEM_DOLL, -1);
				break;
			}
			case "31348-15.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "17");
				break;
			}
			case "31348-16.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "18");
				break;
			}
			case "31532-20.htm":
			{
				qs.giveItems(905, 2);
				qs.giveItems(874, 1);
				qs.takeItems(7063, -1);
				qs.rewardExpAndSp(572277, 53750);
				qs.unset("cond");
				qs.exitQuest(true);
				qs.playSound("ItemSound.quest_finish");
				break;
			}
			case "31522-15.htm":
			{
				qs.giveItems(936, 1);
				qs.giveItems(874, 1);
				qs.takeItems(7063, -1);
				qs.rewardExpAndSp(572277, 53750);
				qs.unset("cond");
				qs.exitQuest(true);
				qs.playSound("ItemSound.quest_finish");
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
		
		final int npcId = npc.getNpcId();
		final int id = qs.getState();
		final int cond = qs.getInt("cond");
		if (id == State.COMPLETED)
		{
			htmltext = getAlreadyCompletedMsg();
		}
		else if (id == State.CREATED)
		{
			if (npcId == BENEDICT)
			{
				final QuestState qs2 = player.getQuestState(Q024_InhabitantsOfTheForrestOfTheDead.class.getSimpleName());
				if (qs2 != null)
				{
					if ((qs2.getState() == State.COMPLETED) && (player.getLevel() >= 66))
					{
						htmltext = "31349-01.htm";
					}
					else
					{
						htmltext = "31349-00.htm";
					}
				}
			}
		}
		else if (id == State.STARTED)
		{
			if (npcId == BENEDICT)
			{
				if (cond == 1)
				{
					htmltext = "31349-02.htm";
				}
				else if ((cond == 2) || (cond == 3))
				{
					htmltext = "31349-04.htm";
				}
				else if (cond == 4)
				{
					htmltext = "31349-10.htm";
				}
			}
			else if (npcId == WIZARD)
			{
				if (cond == 2)
				{
					htmltext = "31522-01.htm";
					qs.playSound("ItemSound.quest_middle");
					qs.set("cond", "3");
					qs.giveItems(SUSPICIOUS_TOTEM, 1);
				}
				else if (cond == 3)
				{
					htmltext = "31522-02.htm";
				}
				else if (cond == 5)
				{
					htmltext = "31522-03.htm";
				}
				else if (cond == 6)
				{
					htmltext = "31522-04.htm";
				}
				else if (cond == 9)
				{
					htmltext = "31522-05.htm";
					qs.playSound("ItemSound.quest_middle");
					qs.set("cond", "10");
				}
				else if (cond == 10)
				{
					htmltext = "31522-05.htm";
				}
				else if (cond == 15)
				{
					htmltext = "31522-06.htm";
				}
				else if (cond == 16)
				{
					htmltext = "31522-13.htm";
				}
				else if (cond == 17)
				{
					htmltext = "31522-16.htm";
				}
				else if (cond == 18)
				{
					htmltext = "31522-14.htm";
				}
			}
			else if (npcId == AGRIPEL)
			{
				if (cond == 4)
				{
					htmltext = "31348-01.htm";
				}
				else if (cond == 5)
				{
					htmltext = "31348-08.htm";
				}
				else if (cond == 16)
				{
					htmltext = "31348-09.htm";
				}
				else if (cond == 17)
				{
					htmltext = "31348-17.htm";
				}
				else if (cond == 18)
				{
					htmltext = "31348-18.htm";
				}
			}
			else if (npcId == BOOKSHELF)
			{
				if (cond == 6)
				{
					htmltext = "31533-01.htm";
				}
			}
			else if (npcId == BOOKSHELF2)
			{
				if (cond == 6)
				{
					htmltext = "31534-01.htm";
				}
			}
			else if (npcId == BOOKSHELF3)
			{
				if ((cond >= 6) && (cond <= 8))
				{
					htmltext = "31535-01.htm";
				}
				else if (cond == 9)
				{
					htmltext = "31535-06.htm";
				}
			}
			else if (npcId == LIDIA)
			{
				if (cond == 10)
				{
					htmltext = "31532-01.htm";
				}
				else if ((cond == 11) || (cond == 12))
				{
					htmltext = "31532-06.htm";
				}
				else if (cond == 13)
				{
					htmltext = "31532-07.htm";
					qs.set("cond", "14");
					qs.takeItems(DRESS, -1);
				}
				else if (cond == 14)
				{
					htmltext = "31532-08.htm";
				}
				else if (cond == 15)
				{
					htmltext = "31532-18.htm";
				}
				else if (cond == 17)
				{
					htmltext = "31532-19.htm";
				}
				else if (cond == 18)
				{
					htmltext = "31532-21.htm";
				}
			}
			else if (npcId == TOMBSTONE)
			{
				if ((cond == 11) || (cond == 12))
				{
					htmltext = "31531-01.htm";
				}
				else if (cond == 13)
				{
					htmltext = "31531-03.htm";
				}
			}
			else if (npcId == COFFIN)
			{
				if (cond == 12)
				{
					htmltext = "31536-01.htm";
					qs.giveItems(DRESS, 1);
					qs.playSound("ItemSound.quest_middle");
					qs.set("cond", "13");
					npc.deleteMe();
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return null;
		}
		
		if ((qs.getState() == State.STARTED) && (qs.getInt("cond") == 7))
		{
			qs.playSound("ItemSound.quest_itemget");
			qs.set("cond", "8");
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "You've ended my immortal life! You've protected by the feudal lord, aren't you?"));
			qs.giveItems(TOTEM_DOLL, 1);
			qs.set("step", "2");
		}
		
		return null;
	}
}