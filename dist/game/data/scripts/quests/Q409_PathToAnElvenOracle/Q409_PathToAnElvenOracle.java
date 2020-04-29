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
package quests.Q409_PathToAnElvenOracle;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q409_PathToAnElvenOracle extends Quest
{
	// Items
	private static final int CRYSTAL_MEDALLION = 1231;
	private static final int SWINDLER_MONEY = 1232;
	private static final int ALLANA_DIARY = 1233;
	private static final int LIZARD_CAPTAIN_ORDER = 1234;
	private static final int LEAF_OF_ORACLE = 1235;
	private static final int HALF_OF_DIARY = 1236;
	private static final int TAMIL_NECKLACE = 1275;
	
	// NPCs
	private static final int MANUEL = 30293;
	private static final int ALLANA = 30424;
	private static final int PERRIN = 30428;
	
	public Q409_PathToAnElvenOracle()
	{
		super(409, "Path to an Elven Oracle");
		
		registerQuestItems(CRYSTAL_MEDALLION, SWINDLER_MONEY, ALLANA_DIARY, LIZARD_CAPTAIN_ORDER, HALF_OF_DIARY, TAMIL_NECKLACE);
		
		addStartNpc(MANUEL);
		addTalkId(MANUEL, ALLANA, PERRIN);
		
		addKillId(27032, 27033, 27034, 27035);
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
		
		if (event.equals("30293-05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(CRYSTAL_MEDALLION, 1);
		}
		else if (event.equals("spawn_lizards"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			addSpawn(27032, -92319, 154235, -3284, 2000, false, 0);
			addSpawn(27033, -92361, 154190, -3284, 2000, false, 0);
			addSpawn(27034, -92375, 154278, -3278, 2000, false, 0);
			return null;
		}
		else if (event.equals("30428-06.htm"))
		{
			addSpawn(27035, -93194, 147587, -2672, 2000, false, 0);
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
				if (player.getClassId() != ClassId.ELVEN_MAGE)
				{
					htmltext = (player.getClassId() == ClassId.ORACLE) ? "30293-02a.htm" : "30293-02.htm";
				}
				else if (player.getLevel() < 19)
				{
					htmltext = "30293-03.htm";
				}
				else if (st.hasQuestItems(LEAF_OF_ORACLE))
				{
					htmltext = "30293-04.htm";
				}
				else
				{
					htmltext = "30293-01.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MANUEL:
						if (cond == 1)
						{
							htmltext = "30293-06.htm";
						}
						else if ((cond == 2) || (cond == 3))
						{
							htmltext = "30293-09.htm";
						}
						else if ((cond > 3) && (cond < 7))
						{
							htmltext = "30293-07.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30293-08.htm";
							st.takeItems(ALLANA_DIARY, 1);
							st.takeItems(CRYSTAL_MEDALLION, 1);
							st.takeItems(LIZARD_CAPTAIN_ORDER, 1);
							st.takeItems(SWINDLER_MONEY, 1);
							st.giveItems(LEAF_OF_ORACLE, 1);
							st.rewardExpAndSp(3200, 1130);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case ALLANA:
						if (cond == 1)
						{
							htmltext = "30424-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30424-02.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(HALF_OF_DIARY, 1);
						}
						else if (cond == 4)
						{
							htmltext = "30424-03.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30424-06.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30424-04.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HALF_OF_DIARY, -1);
							st.giveItems(ALLANA_DIARY, 1);
						}
						else if (cond == 7)
						{
							htmltext = "30424-05.htm";
						}
						break;
					
					case PERRIN:
						if (cond == 4)
						{
							htmltext = "30428-01.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30428-04.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TAMIL_NECKLACE, -1);
							st.giveItems(SWINDLER_MONEY, 1);
						}
						else if (cond > 5)
						{
							htmltext = "30428-05.htm";
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
		
		if (npc.getNpcId() == 27035)
		{
			if (st.getInt("cond") == 4)
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(TAMIL_NECKLACE, 1);
			}
		}
		else if (st.getInt("cond") == 2)
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(LIZARD_CAPTAIN_ORDER, 1);
		}
		
		return null;
	}
}