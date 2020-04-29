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
package quests.Q023_LidiasHeart;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q022_TragedyInVonHellmannForest.Q022_TragedyInVonHellmannForest;

public class Q023_LidiasHeart extends Quest
{
	// NPCs
	private static final int INNOCENTIN = 31328;
	private static final int BROKEN_BOOKSHELF = 31526;
	private static final int GHOST_OF_VON_HELLMANN = 31524;
	private static final int TOMBSTONE = 31523;
	private static final int VIOLET = 31386;
	private static final int BOX = 31530;
	
	// NPC instance
	private NpcInstance _ghost = null;
	
	// Items
	private static final int FOREST_OF_DEADMAN_MAP = 7063;
	private static final int SILVER_KEY = 7149;
	private static final int LIDIA_HAIRPIN = 7148;
	private static final int LIDIA_DIARY = 7064;
	private static final int SILVER_SPEAR = 7150;
	
	public Q023_LidiasHeart()
	{
		super(23, "Lidia's Heart");
		
		registerQuestItems(FOREST_OF_DEADMAN_MAP, SILVER_KEY, LIDIA_DIARY, SILVER_SPEAR);
		
		addStartNpc(INNOCENTIN);
		addTalkId(INNOCENTIN, BROKEN_BOOKSHELF, GHOST_OF_VON_HELLMANN, VIOLET, BOX, TOMBSTONE);
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
		
		if (event.equals("31328-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(FOREST_OF_DEADMAN_MAP, 1);
			st.giveItems(SILVER_KEY, 1);
		}
		else if (event.equals("31328-06.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31526-05.htm"))
		{
			if (!st.hasQuestItems(LIDIA_HAIRPIN))
			{
				st.giveItems(LIDIA_HAIRPIN, 1);
				if (st.hasQuestItems(LIDIA_DIARY))
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
				{
					st.playSound(QuestState.SOUND_ITEMGET);
				}
			}
		}
		else if (event.equals("31526-11.htm"))
		{
			if (!st.hasQuestItems(LIDIA_DIARY))
			{
				st.giveItems(LIDIA_DIARY, 1);
				if (st.hasQuestItems(LIDIA_HAIRPIN))
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
				{
					st.playSound(QuestState.SOUND_ITEMGET);
				}
			}
		}
		else if (event.equals("31328-11.htm"))
		{
			if (st.getInt("cond") < 5)
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equals("31328-19.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31524-04.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LIDIA_DIARY, 1);
		}
		else if (event.equals("31523-02.htm"))
		{
			if (_ghost == null)
			{
				_ghost = addSpawn(31524, 51432, -54570, -3136, 0, false, 60000);
				_ghost.broadcastNpcSay("Who awoke me?");
				startQuestTimer("ghost_cleanup", 58000, null, player, false);
			}
		}
		else if (event.equals("31523-05.htm"))
		{
			// Don't launch twice the same task...
			if (getQuestTimer("tomb_digger", null, player) == null)
			{
				startQuestTimer("tomb_digger", 10000, null, player, false);
			}
		}
		else if (event.equals("tomb_digger"))
		{
			htmltext = "31523-06.htm";
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SILVER_KEY, 1);
		}
		else if (event.equals("31530-02.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SILVER_KEY, 1);
			st.giveItems(SILVER_SPEAR, 1);
		}
		else if (event.equals("ghost_cleanup"))
		{
			_ghost = null;
			return null;
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
				final QuestState st2 = player.getQuestState(Q022_TragedyInVonHellmannForest.class.getSimpleName());
				if ((st2 != null) && st2.isCompleted())
				{
					if (player.getLevel() >= 64)
					{
						htmltext = "31328-01.htm";
					}
					else
					{
						htmltext = "31328-00a.htm";
					}
				}
				else
				{
					htmltext = "31328-00.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case INNOCENTIN:
						if (cond == 1)
						{
							htmltext = "31328-03.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31328-07.htm";
						}
						else if (cond == 4)
						{
							htmltext = "31328-08.htm";
						}
						else if (cond > 5)
						{
							htmltext = "31328-21.htm";
						}
						break;
					
					case BROKEN_BOOKSHELF:
						if (cond == 2)
						{
							htmltext = "31526-00.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 3)
						{
							if (!st.hasQuestItems(LIDIA_DIARY))
							{
								htmltext = (!st.hasQuestItems(LIDIA_HAIRPIN)) ? "31526-02.htm" : "31526-06.htm";
							}
							else if (!st.hasQuestItems(LIDIA_HAIRPIN))
							{
								htmltext = "31526-12.htm";
							}
						}
						else if (cond > 3)
						{
							htmltext = "31526-13.htm";
						}
						break;
					
					case GHOST_OF_VON_HELLMANN:
						if (cond == 6)
						{
							htmltext = "31524-01.htm";
						}
						else if (cond > 6)
						{
							htmltext = "31524-05.htm";
						}
						break;
					
					case TOMBSTONE:
						if (cond == 6)
						{
							htmltext = (_ghost == null) ? "31523-01.htm" : "31523-03.htm";
						}
						else if (cond == 7)
						{
							htmltext = "31523-04.htm";
						}
						else if (cond > 7)
						{
							htmltext = "31523-06.htm";
						}
						break;
					
					case VIOLET:
						if (cond == 8)
						{
							htmltext = "31386-01.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 9)
						{
							htmltext = "31386-02.htm";
						}
						else if (cond == 10)
						{
							if (st.hasQuestItems(SILVER_SPEAR))
							{
								htmltext = "31386-03.htm";
								st.takeItems(SILVER_SPEAR, 1);
								st.rewardItems(57, 100000);
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(false);
							}
							else
							{
								htmltext = "31386-02.htm";
								st.set("cond", "9");
							}
						}
						break;
					
					case BOX:
						if (cond == 9)
						{
							htmltext = "31530-01.htm";
						}
						else if (cond == 10)
						{
							htmltext = "31530-03.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				if (npc.getNpcId() == VIOLET)
				{
					htmltext = "31386-04.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg();
				}
				break;
		}
		
		return htmltext;
	}
}