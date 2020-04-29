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
package quests.Q039_RedEyedInvaders;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q039_RedEyedInvaders extends Quest
{
	// NPCs
	private static final int BABENCO = 30334;
	private static final int BATHIS = 30332;
	
	// Mobs
	private static final int MAILLE_LIZARDMAN = 20919;
	private static final int MAILLE_LIZARDMAN_SCOUT = 20920;
	private static final int MAILLE_LIZARDMAN_GUARD = 20921;
	private static final int ARANEID = 20925;
	
	// Items
	private static final int BLACK_BONE_NECKLACE = 7178;
	private static final int RED_BONE_NECKLACE = 7179;
	private static final int INCENSE_POUCH = 7180;
	private static final int GEM_OF_MAILLE = 7181;
	
	// First droplist
	private static final Map<Integer, int[]> FIRST_DP = new HashMap<>();
	static
	{
		FIRST_DP.put(MAILLE_LIZARDMAN_GUARD, new int[]
		{
			RED_BONE_NECKLACE,
			BLACK_BONE_NECKLACE
		});
		FIRST_DP.put(MAILLE_LIZARDMAN, new int[]
		{
			BLACK_BONE_NECKLACE,
			RED_BONE_NECKLACE
		});
		FIRST_DP.put(MAILLE_LIZARDMAN_SCOUT, new int[]
		{
			BLACK_BONE_NECKLACE,
			RED_BONE_NECKLACE
		});
	}
	
	// Second droplist
	private static final Map<Integer, int[]> SECOND_DP = new HashMap<>();
	static
	{
		SECOND_DP.put(ARANEID, new int[]
		{
			GEM_OF_MAILLE,
			INCENSE_POUCH,
			500000
		});
		SECOND_DP.put(MAILLE_LIZARDMAN_GUARD, new int[]
		{
			INCENSE_POUCH,
			GEM_OF_MAILLE,
			300000
		});
		SECOND_DP.put(MAILLE_LIZARDMAN_SCOUT, new int[]
		{
			INCENSE_POUCH,
			GEM_OF_MAILLE,
			250000
		});
	}
	
	// Rewards
	private static final int GREEN_COLORED_LURE_HG = 6521;
	private static final int BABY_DUCK_RODE = 6529;
	private static final int FISHING_SHOT_NG = 6535;
	
	public Q039_RedEyedInvaders()
	{
		super(39, "Red-Eyed Invaders");
		
		registerQuestItems(BLACK_BONE_NECKLACE, RED_BONE_NECKLACE, INCENSE_POUCH, GEM_OF_MAILLE);
		
		addStartNpc(BABENCO);
		addTalkId(BABENCO, BATHIS);
		
		addKillId(MAILLE_LIZARDMAN, MAILLE_LIZARDMAN_SCOUT, MAILLE_LIZARDMAN_GUARD, ARANEID);
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
		
		if (event.equals("30334-1.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30332-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30332-3.htm"))
		{
			st.set("cond", "4");
			st.takeItems(BLACK_BONE_NECKLACE, -1);
			st.takeItems(RED_BONE_NECKLACE, -1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30332-5.htm"))
		{
			st.takeItems(INCENSE_POUCH, -1);
			st.takeItems(GEM_OF_MAILLE, -1);
			st.giveItems(GREEN_COLORED_LURE_HG, 60);
			st.giveItems(BABY_DUCK_RODE, 1);
			st.giveItems(FISHING_SHOT_NG, 500);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 20) ? "30334-2.htm" : "30334-0.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case BABENCO:
						htmltext = "30334-3.htm";
						break;
					
					case BATHIS:
						if (cond == 1)
						{
							htmltext = "30332-0.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30332-2a.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30332-2.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30332-3a.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30332-4.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		PlayerInstance partyMember = getRandomPartyMember(player, npc, "2");
		if ((partyMember != null) && (npcId != ARANEID))
		{
			final QuestState st = partyMember.getQuestState(getName());
			if (st == null)
			{
				return null;
			}
			
			final int[] list = FIRST_DP.get(npcId);
			if (st.dropItems(list[0], 1, 100, 500000) && (st.getQuestItemsCount(list[1]) == 100))
			{
				st.set("cond", "3");
			}
		}
		else
		{
			partyMember = getRandomPartyMember(player, npc, "4");
			if ((partyMember != null) && (npcId != MAILLE_LIZARDMAN))
			{
				final QuestState st = partyMember.getQuestState(getName());
				final int[] list = SECOND_DP.get(npcId);
				if (st.dropItems(list[0], 1, 30, list[2]) && (st.getQuestItemsCount(list[1]) == 30))
				{
					st.set("cond", "5");
				}
			}
		}
		
		return null;
	}
}