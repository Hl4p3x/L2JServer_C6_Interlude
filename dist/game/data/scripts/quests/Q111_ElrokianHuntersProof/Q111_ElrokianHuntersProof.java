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
package quests.Q111_ElrokianHuntersProof;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q111_ElrokianHuntersProof extends Quest
{
	// NPCs
	private static final int MARQUEZ = 32113;
	private static final int MUSHIKA = 32114;
	private static final int ASAMAH = 32115;
	private static final int KIRIKASHIN = 32116;
	
	// Items
	private static final int FRAGMENT = 8768;
	private static final int EXPEDITION_LETTER = 8769;
	private static final int CLAW = 8770;
	private static final int BONE = 8771;
	private static final int SKIN = 8772;
	private static final int PRACTICE_TRAP = 8773;
	
	public Q111_ElrokianHuntersProof()
	{
		super(111, "Elrokian Hunter's Proof");
		
		registerQuestItems(FRAGMENT, EXPEDITION_LETTER, CLAW, BONE, SKIN, PRACTICE_TRAP);
		
		addStartNpc(MARQUEZ);
		addTalkId(MARQUEZ, MUSHIKA, ASAMAH, KIRIKASHIN);
		
		addKillId(22196, 22197, 22198, 22218, 22200, 22201, 22202, 22219, 22208, 22209, 22210, 22221, 22203, 22204, 22205, 22220);
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
		
		if (event.equals("32113-002.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("32115-002.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32113-009.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32113-018.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(FRAGMENT, -1);
			st.giveItems(EXPEDITION_LETTER, 1);
		}
		else if (event.equals("32116-003.htm"))
		{
			st.set("cond", "7");
			st.playSound("EtcSound.elcroki_song_full");
		}
		else if (event.equals("32116-005.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32115-004.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32115-006.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32116-007.htm"))
		{
			st.takeItems(PRACTICE_TRAP, 1);
			st.giveItems(8763, 1);
			st.giveItems(8764, 100);
			st.rewardItems(57, 1022636);
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
				htmltext = (player.getLevel() < 75) ? "32113-000.htm" : "32113-001.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MARQUEZ:
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "32113-002.htm";
						}
						else if (cond == 3)
						{
							htmltext = "32113-003.htm";
						}
						else if (cond == 4)
						{
							htmltext = "32113-009.htm";
						}
						else if (cond == 5)
						{
							htmltext = "32113-010.htm";
						}
						break;
					
					case MUSHIKA:
						if (cond == 1)
						{
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						htmltext = "32114-001.htm";
						break;
					
					case ASAMAH:
						if (cond == 2)
						{
							htmltext = "32115-001.htm";
						}
						else if (cond == 3)
						{
							htmltext = "32115-002.htm";
						}
						else if (cond == 8)
						{
							htmltext = "32115-003.htm";
						}
						else if (cond == 9)
						{
							htmltext = "32115-004.htm";
						}
						else if (cond == 10)
						{
							htmltext = "32115-006.htm";
						}
						else if (cond == 11)
						{
							htmltext = "32115-007.htm";
							st.set("cond", "12");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BONE, -1);
							st.takeItems(CLAW, -1);
							st.takeItems(SKIN, -1);
							st.giveItems(PRACTICE_TRAP, 1);
						}
						break;
					
					case KIRIKASHIN:
						if (cond < 6)
						{
							htmltext = "32116-008.htm";
						}
						else if (cond == 6)
						{
							htmltext = "32116-001.htm";
							st.takeItems(EXPEDITION_LETTER, 1);
						}
						else if (cond == 7)
						{
							htmltext = "32116-004.htm";
						}
						else if (cond == 12)
						{
							htmltext = "32116-006.htm";
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
		final PlayerInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case 22196:
			case 22197:
			case 22198:
			case 22218:
				if ((st.getInt("cond") == 4) && st.dropItems(FRAGMENT, 1, 50, 250000))
				{
					st.set("cond", "5");
				}
				break;
			
			case 22200:
			case 22201:
			case 22202:
			case 22219:
				if ((st.getInt("cond") == 10) && st.dropItems(CLAW, 1, 10, 650000) && (st.getQuestItemsCount(BONE) >= 10) && (st.getQuestItemsCount(SKIN) >= 10))
				{
					st.set("cond", "11");
				}
				break;
			
			case 22208:
			case 22209:
			case 22210:
			case 22221:
				if ((st.getInt("cond") == 10) && st.dropItems(SKIN, 1, 10, 650000) && (st.getQuestItemsCount(CLAW) >= 10) && (st.getQuestItemsCount(BONE) >= 10))
				{
					st.set("cond", "11");
				}
				break;
			
			case 22203:
			case 22204:
			case 22205:
			case 22220:
				if ((st.getInt("cond") == 10) && st.dropItems(BONE, 1, 10, 650000) && (st.getQuestItemsCount(CLAW) >= 10) && (st.getQuestItemsCount(SKIN) >= 10))
				{
					st.set("cond", "11");
				}
				break;
		}
		
		return null;
	}
}