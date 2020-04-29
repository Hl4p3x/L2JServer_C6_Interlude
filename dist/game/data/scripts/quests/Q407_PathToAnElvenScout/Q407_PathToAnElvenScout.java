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
package quests.Q407_PathToAnElvenScout;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q407_PathToAnElvenScout extends Quest
{
	// Items
	private static final int REISA_LETTER = 1207;
	private static final int PRIAS_TORN_LETTER_1 = 1208;
	private static final int PRIAS_TORN_LETTER_2 = 1209;
	private static final int PRIAS_TORN_LETTER_3 = 1210;
	private static final int PRIAS_TORN_LETTER_4 = 1211;
	private static final int MORETTI_HERB = 1212;
	private static final int MORETTI_LETTER = 1214;
	private static final int PRIAS_LETTER = 1215;
	private static final int HONORARY_GUARD = 1216;
	private static final int REISA_RECOMMENDATION = 1217;
	private static final int RUSTED_KEY = 1293;
	
	// NPCs
	private static final int REISA = 30328;
	private static final int BABENCO = 30334;
	private static final int MORETTI = 30337;
	private static final int PRIAS = 30426;
	
	public Q407_PathToAnElvenScout()
	{
		super(407, "Path to an Elven Scout");
		
		registerQuestItems(REISA_LETTER, PRIAS_TORN_LETTER_1, PRIAS_TORN_LETTER_2, PRIAS_TORN_LETTER_3, PRIAS_TORN_LETTER_4, MORETTI_HERB, MORETTI_LETTER, PRIAS_LETTER, HONORARY_GUARD, RUSTED_KEY);
		
		addStartNpc(REISA);
		addTalkId(REISA, MORETTI, BABENCO, PRIAS);
		
		addKillId(20053, 27031);
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
		
		if (event.equals("30328-05.htm"))
		{
			if (player.getClassId() != ClassId.ELVEN_FIGHTER)
			{
				htmltext = (player.getClassId() == ClassId.ELVEN_SCOUT) ? "30328-02a.htm" : "30328-02.htm";
			}
			else if (player.getLevel() < 19)
			{
				htmltext = "30328-03.htm";
			}
			else if (st.hasQuestItems(REISA_RECOMMENDATION))
			{
				htmltext = "30328-04.htm";
			}
			else
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.giveItems(REISA_LETTER, 1);
			}
		}
		else if (event.equals("30337-03.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(REISA_LETTER, -1);
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
				htmltext = "30328-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case REISA:
						if (cond == 1)
						{
							htmltext = "30328-06.htm";
						}
						else if ((cond > 1) && (cond < 8))
						{
							htmltext = "30328-08.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30328-07.htm";
							st.takeItems(HONORARY_GUARD, -1);
							st.giveItems(REISA_RECOMMENDATION, 1);
							st.rewardExpAndSp(3200, 1000);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case MORETTI:
						if (cond == 1)
						{
							htmltext = "30337-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = (!st.hasQuestItems(PRIAS_TORN_LETTER_1)) ? "30337-04.htm" : "30337-05.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30337-06.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(PRIAS_TORN_LETTER_1, -1);
							st.takeItems(PRIAS_TORN_LETTER_2, -1);
							st.takeItems(PRIAS_TORN_LETTER_3, -1);
							st.takeItems(PRIAS_TORN_LETTER_4, -1);
							st.giveItems(MORETTI_HERB, 1);
							st.giveItems(MORETTI_LETTER, 1);
						}
						else if ((cond > 3) && (cond < 7))
						{
							htmltext = "30337-09.htm";
						}
						else if ((cond == 7) && st.hasQuestItems(PRIAS_LETTER))
						{
							htmltext = "30337-07.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(PRIAS_LETTER, -1);
							st.giveItems(HONORARY_GUARD, 1);
						}
						else if (cond == 8)
						{
							htmltext = "30337-08.htm";
						}
						break;
					
					case BABENCO:
						if (cond == 2)
						{
							htmltext = "30334-01.htm";
						}
						break;
					
					case PRIAS:
						if (cond == 4)
						{
							htmltext = "30426-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 5)
						{
							htmltext = "30426-01.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30426-02.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(RUSTED_KEY, -1);
							st.takeItems(MORETTI_HERB, -1);
							st.takeItems(MORETTI_LETTER, -1);
							st.giveItems(PRIAS_LETTER, 1);
						}
						else if (cond == 7)
						{
							htmltext = "30426-04.htm";
						}
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
		
		final int cond = st.getInt("cond");
		if (npc.getNpcId() == 20053)
		{
			if (cond == 2)
			{
				if (!st.hasQuestItems(PRIAS_TORN_LETTER_1))
				{
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(PRIAS_TORN_LETTER_1, 1);
				}
				else if (!st.hasQuestItems(PRIAS_TORN_LETTER_2))
				{
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(PRIAS_TORN_LETTER_2, 1);
				}
				else if (!st.hasQuestItems(PRIAS_TORN_LETTER_3))
				{
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(PRIAS_TORN_LETTER_3, 1);
				}
				else if (!st.hasQuestItems(PRIAS_TORN_LETTER_4))
				{
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(PRIAS_TORN_LETTER_4, 1);
				}
			}
		}
		else if (((cond == 4) || (cond == 5)) && st.dropItems(RUSTED_KEY, 1, 1, 600000))
		{
			st.set("cond", "6");
		}
		
		return null;
	}
}