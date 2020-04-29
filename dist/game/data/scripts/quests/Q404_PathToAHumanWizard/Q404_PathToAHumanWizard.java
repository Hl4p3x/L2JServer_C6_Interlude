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
package quests.Q404_PathToAHumanWizard;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q404_PathToAHumanWizard extends Quest
{
	// Items
	private static final int MAP_OF_LUSTER = 1280;
	private static final int KEY_OF_FLAME = 1281;
	private static final int FLAME_EARING = 1282;
	private static final int BROKEN_BRONZE_MIRROR = 1283;
	private static final int WIND_FEATHER = 1284;
	private static final int WIND_BANGEL = 1285;
	private static final int RAMA_DIARY = 1286;
	private static final int SPARKLE_PEBBLE = 1287;
	private static final int WATER_NECKLACE = 1288;
	private static final int RUST_GOLD_COIN = 1289;
	private static final int RED_SOIL = 1290;
	private static final int EARTH_RING = 1291;
	private static final int BEAD_OF_SEASON = 1292;
	
	// NPCs
	private static final int PARINA = 30391;
	private static final int EARTH_SNAKE = 30409;
	private static final int WASTELAND_LIZARDMAN = 30410;
	private static final int FLAME_SALAMANDER = 30411;
	private static final int WIND_SYLPH = 30412;
	private static final int WATER_UNDINE = 30413;
	
	public Q404_PathToAHumanWizard()
	{
		super(404, "Path to a Human Wizard");
		
		registerQuestItems(MAP_OF_LUSTER, KEY_OF_FLAME, FLAME_EARING, BROKEN_BRONZE_MIRROR, WIND_FEATHER, WIND_BANGEL, RAMA_DIARY, SPARKLE_PEBBLE, WATER_NECKLACE, RUST_GOLD_COIN, RED_SOIL, EARTH_RING);
		
		addStartNpc(PARINA);
		addTalkId(PARINA, EARTH_SNAKE, WASTELAND_LIZARDMAN, FLAME_SALAMANDER, WIND_SYLPH, WATER_UNDINE);
		
		addKillId(20021, 20359, 27030);
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
		
		if (event.equals("30391-08.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30410-03.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BROKEN_BRONZE_MIRROR, 1);
			st.giveItems(WIND_FEATHER, 1);
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
		
		final int cond = st.getInt("cond");
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getClassId() != ClassId.MAGE)
				{
					htmltext = (player.getClassId() == ClassId.WIZARD) ? "30391-02a.htm" : "30391-01.htm";
				}
				else if (player.getLevel() < 19)
				{
					htmltext = "30391-02.htm";
				}
				else if (st.hasQuestItems(BEAD_OF_SEASON))
				{
					htmltext = "30391-03.htm";
				}
				else
				{
					htmltext = "30391-04.htm";
				}
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case PARINA:
						if (cond < 13)
						{
							htmltext = "30391-05.htm";
						}
						else if (cond == 13)
						{
							htmltext = "30391-06.htm";
							st.takeItems(EARTH_RING, 1);
							st.takeItems(FLAME_EARING, 1);
							st.takeItems(WATER_NECKLACE, 1);
							st.takeItems(WIND_BANGEL, 1);
							st.giveItems(BEAD_OF_SEASON, 1);
							st.rewardExpAndSp(3200, 2020);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case FLAME_SALAMANDER:
						if (cond == 1)
						{
							htmltext = "30411-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(MAP_OF_LUSTER, 1);
						}
						else if (cond == 2)
						{
							htmltext = "30411-02.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30411-03.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(KEY_OF_FLAME, 1);
							st.takeItems(MAP_OF_LUSTER, 1);
							st.giveItems(FLAME_EARING, 1);
						}
						else if (cond > 3)
						{
							htmltext = "30411-04.htm";
						}
						break;
					
					case WIND_SYLPH:
						if (cond == 4)
						{
							htmltext = "30412-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(BROKEN_BRONZE_MIRROR, 1);
						}
						else if (cond == 5)
						{
							htmltext = "30412-02.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30412-03.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(WIND_FEATHER, 1);
							st.giveItems(WIND_BANGEL, 1);
						}
						else if (cond > 6)
						{
							htmltext = "30412-04.htm";
						}
						break;
					
					case WASTELAND_LIZARDMAN:
						if (cond == 5)
						{
							htmltext = "30410-01.htm";
						}
						else if (cond > 5)
						{
							htmltext = "30410-04.htm";
						}
						break;
					
					case WATER_UNDINE:
						if (cond == 7)
						{
							htmltext = "30413-01.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(RAMA_DIARY, 1);
						}
						else if (cond == 8)
						{
							htmltext = "30413-02.htm";
						}
						else if (cond == 9)
						{
							htmltext = "30413-03.htm";
							st.set("cond", "10");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(RAMA_DIARY, 1);
							st.takeItems(SPARKLE_PEBBLE, -1);
							st.giveItems(WATER_NECKLACE, 1);
						}
						else if (cond > 9)
						{
							htmltext = "30413-04.htm";
						}
						break;
					
					case EARTH_SNAKE:
						if (cond == 10)
						{
							htmltext = "30409-01.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(RUST_GOLD_COIN, 1);
						}
						else if (cond == 11)
						{
							htmltext = "30409-02.htm";
						}
						else if (cond == 12)
						{
							htmltext = "30409-03.htm";
							st.set("cond", "13");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(RED_SOIL, 1);
							st.takeItems(RUST_GOLD_COIN, 1);
							st.giveItems(EARTH_RING, 1);
						}
						else if (cond > 12)
						{
							htmltext = "30409-04.htm";
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
		
		switch (npc.getNpcId())
		{
			case 20359: // Ratman Warrior
				if ((st.getInt("cond") == 2) && st.dropItems(KEY_OF_FLAME, 1, 1, 800000))
				{
					st.set("cond", "3");
				}
				break;
			
			case 27030: // Water Seer
				if ((st.getInt("cond") == 8) && st.dropItems(SPARKLE_PEBBLE, 1, 2, 800000))
				{
					st.set("cond", "9");
				}
				break;
			
			case 20021: // Red Bear
				if ((st.getInt("cond") == 11) && st.dropItems(RED_SOIL, 1, 1, 200000))
				{
					st.set("cond", "12");
				}
				break;
		}
		
		return null;
	}
}