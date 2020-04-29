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
package quests.Q374_WhisperOfDreams_Part1;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q374_WhisperOfDreams_Part1 extends Quest
{
	// NPCs
	private static final int MANAKIA = 30515;
	private static final int TORAI = 30557;
	
	// Monsters
	private static final int CAVE_BEAST = 20620;
	private static final int DEATH_WAVE = 20621;
	
	// Items
	private static final int CAVE_BEAST_TOOTH = 5884;
	private static final int DEATH_WAVE_LIGHT = 5885;
	private static final int SEALED_MYSTERIOUS_STONE = 5886;
	private static final int MYSTERIOUS_STONE = 5887;
	
	// Rewards
	private static final int[][] REWARDS =
	{
		{
			5486,
			3,
			2950
		}, // Dark Crystal, 3x, 2950 adena
		{
			5487,
			2,
			18050
		}, // Nightmare, 2x, 18050 adena
		{
			5488,
			2,
			18050
		}, // Majestic, 2x, 18050 adena
		{
			5485,
			4,
			10450
		}, // Tallum Tunic, 4, 10450 adena
		{
			5489,
			6,
			15550
		}
		// Tallum Stockings, 6, 15550 adena
	};
	
	public Q374_WhisperOfDreams_Part1()
	{
		super(374, "Whisper of Dreams, Part 1");
		
		registerQuestItems(DEATH_WAVE_LIGHT, CAVE_BEAST_TOOTH, SEALED_MYSTERIOUS_STONE, MYSTERIOUS_STONE);
		
		addStartNpc(MANAKIA);
		addTalkId(MANAKIA, TORAI);
		
		addKillId(CAVE_BEAST, DEATH_WAVE);
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
		
		// Manakia
		if (event.equals("30515-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.set("condStone", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.startsWith("30515-06-"))
		{
			if ((st.getQuestItemsCount(CAVE_BEAST_TOOTH) >= 65) && (st.getQuestItemsCount(DEATH_WAVE_LIGHT) >= 65))
			{
				htmltext = "30515-06.htm";
				st.playSound(QuestState.SOUND_MIDDLE);
				
				final int[] reward = REWARDS[Integer.parseInt(event.substring(9, 10))];
				
				st.takeItems(CAVE_BEAST_TOOTH, -1);
				st.takeItems(DEATH_WAVE_LIGHT, -1);
				
				st.rewardItems(57, reward[2]);
				st.giveItems(reward[0], reward[1]);
			}
			else
			{
				htmltext = "30515-07.htm";
			}
		}
		else if (event.equals("30515-08.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		// Torai
		else if (event.equals("30557-02.htm"))
		{
			if ((st.getInt("cond") == 2) && st.hasQuestItems(SEALED_MYSTERIOUS_STONE))
			{
				st.set("cond", "3");
				st.takeItems(SEALED_MYSTERIOUS_STONE, -1);
				st.giveItems(MYSTERIOUS_STONE, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
			{
				htmltext = "30557-03.htm";
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
				htmltext = (player.getLevel() < 56) ? "30515-01.htm" : "30515-02.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MANAKIA:
						if (!(st.hasQuestItems(SEALED_MYSTERIOUS_STONE)))
						{
							if ((st.getQuestItemsCount(CAVE_BEAST_TOOTH) >= 65) && (st.getQuestItemsCount(DEATH_WAVE_LIGHT) >= 65))
							{
								htmltext = "30515-05.htm";
							}
							else
							{
								htmltext = "30515-04.htm";
							}
						}
						else
						{
							if (cond == 1)
							{
								htmltext = "30515-09.htm";
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								htmltext = "30515-10.htm";
							}
						}
						break;
					
					case TORAI:
						if ((cond == 2) && st.hasQuestItems(SEALED_MYSTERIOUS_STONE))
						{
							htmltext = "30557-01.htm";
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
		// Drop tooth or light to anyone.
		PlayerInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		st.dropItems((npc.getNpcId() == CAVE_BEAST) ? CAVE_BEAST_TOOTH : DEATH_WAVE_LIGHT, 1, 65, 500000);
		
		// Drop sealed mysterious stone to party member who still need it.
		partyMember = getRandomPartyMember(player, npc, "condStone", "1");
		if (partyMember == null)
		{
			return null;
		}
		
		st = partyMember.getQuestState(getName());
		
		if (st.dropItems(SEALED_MYSTERIOUS_STONE, 1, 1, 1000))
		{
			st.unset("condStone");
		}
		
		return null;
	}
}