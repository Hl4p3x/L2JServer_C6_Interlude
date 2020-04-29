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
package quests.Q117_TheOceanOfDistantStars;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q117_TheOceanOfDistantStars extends Quest
{
	// NPCs
	private static final int ABEY = 32053;
	private static final int GHOST = 32054;
	private static final int ANCIENT_GHOST = 32055;
	private static final int OBI = 32052;
	private static final int BOX = 32076;
	
	// Items
	private static final int GREY_STAR = 8495;
	private static final int ENGRAVED_HAMMER = 8488;
	
	// Monsters
	private static final int BANDIT_WARRIOR = 22023;
	private static final int BANDIT_INSPECTOR = 22024;
	
	public Q117_TheOceanOfDistantStars()
	{
		super(117, "The Ocean of Distant Stars");
		
		registerQuestItems(GREY_STAR, ENGRAVED_HAMMER);
		
		addStartNpc(ABEY);
		addTalkId(ABEY, ANCIENT_GHOST, GHOST, OBI, BOX);
		addKillId(BANDIT_WARRIOR, BANDIT_INSPECTOR);
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
		
		if (event.equals("32053-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("32055-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32052-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32053-04.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32076-02.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(ENGRAVED_HAMMER, 1);
		}
		else if (event.equals("32053-06.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32052-04.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32052-06.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GREY_STAR, 1);
		}
		else if (event.equals("32055-04.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ENGRAVED_HAMMER, 1);
		}
		else if (event.equals("32054-03.htm"))
		{
			st.rewardExpAndSp(63591, 0);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() < 39) ? "32053-00.htm" : "32053-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ANCIENT_GHOST:
						if (cond == 1)
						{
							htmltext = "32055-01.htm";
						}
						else if ((cond > 1) && (cond < 9))
						{
							htmltext = "32055-02.htm";
						}
						else if (cond == 9)
						{
							htmltext = "32055-03.htm";
						}
						else if (cond > 9)
						{
							htmltext = "32055-05.htm";
						}
						break;
					
					case OBI:
						if (cond == 2)
						{
							htmltext = "32052-01.htm";
						}
						else if ((cond > 2) && (cond < 6))
						{
							htmltext = "32052-02.htm";
						}
						else if (cond == 6)
						{
							htmltext = "32052-03.htm";
						}
						else if (cond == 7)
						{
							htmltext = "32052-04.htm";
						}
						else if (cond == 8)
						{
							htmltext = "32052-05.htm";
						}
						else if (cond > 8)
						{
							htmltext = "32052-06.htm";
						}
						break;
					
					case ABEY:
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "32053-02.htm";
						}
						else if (cond == 3)
						{
							htmltext = "32053-03.htm";
						}
						else if (cond == 4)
						{
							htmltext = "32053-04.htm";
						}
						else if (cond == 5)
						{
							htmltext = "32053-05.htm";
						}
						else if (cond > 5)
						{
							htmltext = "32053-06.htm";
						}
						break;
					
					case BOX:
						if (cond == 4)
						{
							htmltext = "32076-01.htm";
						}
						else if (cond > 4)
						{
							htmltext = "32076-03.htm";
						}
						break;
					
					case GHOST:
						if (cond == 10)
						{
							htmltext = "32054-01.htm";
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
		final QuestState st = checkPlayerCondition(player, npc, "cond", "7");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(GREY_STAR, 1, 1, 200000))
		{
			st.set("cond", "8");
		}
		
		return null;
	}
}