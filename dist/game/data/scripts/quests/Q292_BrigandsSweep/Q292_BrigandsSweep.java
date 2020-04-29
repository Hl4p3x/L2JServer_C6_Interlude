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
package quests.Q292_BrigandsSweep;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q292_BrigandsSweep extends Quest
{
	// NPCs
	private static final int SPIRON = 30532;
	private static final int BALANKI = 30533;
	
	// Items
	private static final int GOBLIN_NECKLACE = 1483;
	private static final int GOBLIN_PENDANT = 1484;
	private static final int GOBLIN_LORD_PENDANT = 1485;
	private static final int SUSPICIOUS_MEMO = 1486;
	private static final int SUSPICIOUS_CONTRACT = 1487;
	
	// Monsters
	private static final int GOBLIN_BRIGAND = 20322;
	private static final int GOBLIN_BRIGAND_LEADER = 20323;
	private static final int GOBLIN_BRIGAND_LIEUTENANT = 20324;
	private static final int GOBLIN_SNOOPER = 20327;
	private static final int GOBLIN_LORD = 20528;
	
	public Q292_BrigandsSweep()
	{
		super(292, "Brigands Sweep");
		
		registerQuestItems(GOBLIN_NECKLACE, GOBLIN_PENDANT, GOBLIN_LORD_PENDANT, SUSPICIOUS_MEMO, SUSPICIOUS_CONTRACT);
		
		addStartNpc(SPIRON);
		addTalkId(SPIRON, BALANKI);
		
		addKillId(GOBLIN_BRIGAND, GOBLIN_BRIGAND_LEADER, GOBLIN_BRIGAND_LIEUTENANT, GOBLIN_SNOOPER, GOBLIN_LORD);
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
		
		if (event.equals("30532-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30532-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (player.getRace() != Race.DWARF)
				{
					htmltext = "30532-00.htm";
				}
				else if (player.getLevel() < 5)
				{
					htmltext = "30532-01.htm";
				}
				else
				{
					htmltext = "30532-02.htm";
				}
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case SPIRON:
						final int goblinNecklaces = st.getQuestItemsCount(GOBLIN_NECKLACE);
						final int goblinPendants = st.getQuestItemsCount(GOBLIN_PENDANT);
						final int goblinLordPendants = st.getQuestItemsCount(GOBLIN_LORD_PENDANT);
						final int suspiciousMemos = st.getQuestItemsCount(SUSPICIOUS_MEMO);
						final int countAll = goblinNecklaces + goblinPendants + goblinLordPendants;
						final boolean hasContract = st.hasQuestItems(SUSPICIOUS_CONTRACT);
						if (countAll == 0)
						{
							htmltext = "30532-04.htm";
						}
						else
						{
							if (hasContract)
							{
								htmltext = "30532-10.htm";
							}
							else if (suspiciousMemos > 0)
							{
								if (suspiciousMemos > 1)
								{
									htmltext = "30532-09.htm";
								}
								else
								{
									htmltext = "30532-08.htm";
								}
							}
							else
							{
								htmltext = "30532-05.htm";
							}
							
							st.takeItems(GOBLIN_NECKLACE, -1);
							st.takeItems(GOBLIN_PENDANT, -1);
							st.takeItems(GOBLIN_LORD_PENDANT, -1);
							if (hasContract)
							{
								st.set("cond", "1");
								st.takeItems(SUSPICIOUS_CONTRACT, -1);
							}
							
							st.rewardItems(57, ((12 * goblinNecklaces) + (36 * goblinPendants) + (33 * goblinLordPendants) + (countAll >= 10 ? 1000 : 0) + ((hasContract) ? 1120 : 0)));
						}
						break;
					
					case BALANKI:
						if (!st.hasQuestItems(SUSPICIOUS_CONTRACT))
						{
							htmltext = "30533-01.htm";
						}
						else
						{
							htmltext = "30533-02.htm";
							st.set("cond", "1");
							st.takeItems(SUSPICIOUS_CONTRACT, -1);
							st.rewardItems(57, 1500);
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
		
		final int chance = Rnd.get(10);
		if (chance > 5)
		{
			switch (npc.getNpcId())
			{
				case GOBLIN_BRIGAND:
				case GOBLIN_SNOOPER:
				case GOBLIN_BRIGAND_LIEUTENANT:
					st.dropItemsAlways(GOBLIN_NECKLACE, 1, 0);
					break;
				
				case GOBLIN_BRIGAND_LEADER:
					st.dropItemsAlways(GOBLIN_PENDANT, 1, 0);
					break;
				
				case GOBLIN_LORD:
					st.dropItemsAlways(GOBLIN_LORD_PENDANT, 1, 0);
					break;
			}
		}
		else if ((chance > 4) && (st.getInt("cond") == 1) && st.dropItemsAlways(SUSPICIOUS_MEMO, 1, 3))
		{
			st.set("cond", "2");
			st.takeItems(SUSPICIOUS_MEMO, -1);
			st.giveItems(SUSPICIOUS_CONTRACT, 1);
		}
		
		return null;
	}
}