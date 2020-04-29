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
package quests.Q344_1000YearsTheEndOfLamentation;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q344_1000YearsTheEndOfLamentation extends Quest
{
	// NPCs
	private static final int GILMORE = 30754;
	private static final int RODEMAI = 30756;
	private static final int ORVEN = 30857;
	private static final int KAIEN = 30623;
	private static final int GARVARENTZ = 30704;
	
	// Items
	private static final int ARTICLE_DEAD_HERO = 4269;
	private static final int OLD_KEY = 4270;
	private static final int OLD_HILT = 4271;
	private static final int OLD_TOTEM = 4272;
	private static final int CRUCIFIX = 4273;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(20236, 380000);
		CHANCES.put(20237, 490000);
		CHANCES.put(20238, 460000);
		CHANCES.put(20239, 490000);
		CHANCES.put(20240, 530000);
		CHANCES.put(20272, 380000);
		CHANCES.put(20273, 490000);
		CHANCES.put(20274, 460000);
		CHANCES.put(20275, 490000);
		CHANCES.put(20276, 530000);
	}
	
	public Q344_1000YearsTheEndOfLamentation()
	{
		super(344, "1000 years, the End of Lamentation");
		
		registerQuestItems(ARTICLE_DEAD_HERO, OLD_KEY, OLD_HILT, OLD_TOTEM, CRUCIFIX);
		
		addStartNpc(GILMORE);
		addTalkId(GILMORE, RODEMAI, ORVEN, GARVARENTZ, KAIEN);
		
		addKillId(20236, 20237, 20238, 20239, 20240, 20272, 20273, 20274, 20275, 20276);
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
		
		if (event.equals("30754-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30754-07.htm"))
		{
			if (st.get("success") != null)
			{
				st.set("cond", "1");
				st.unset("success");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equals("30754-08.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equals("30754-06.htm"))
		{
			if (!st.hasQuestItems(ARTICLE_DEAD_HERO))
			{
				htmltext = "30754-06a.htm";
			}
			else
			{
				final int amount = st.getQuestItemsCount(ARTICLE_DEAD_HERO);
				
				st.takeItems(ARTICLE_DEAD_HERO, -1);
				st.giveItems(57, amount * 60);
				
				// Special item, % based on actual number of qItems.
				if (Rnd.get(1000) < Math.min(10, Math.max(1, amount / 10)))
				{
					htmltext = "30754-10.htm";
				}
			}
		}
		else if (event.equals("30754-11.htm"))
		{
			final int random = Rnd.get(4);
			if (random < 1)
			{
				htmltext = "30754-12.htm";
				st.giveItems(OLD_KEY, 1);
			}
			else if (random < 2)
			{
				htmltext = "30754-13.htm";
				st.giveItems(OLD_HILT, 1);
			}
			else if (random < 3)
			{
				htmltext = "30754-14.htm";
				st.giveItems(OLD_TOTEM, 1);
			}
			else
			{
				st.giveItems(CRUCIFIX, 1);
			}
			
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
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
				htmltext = (player.getLevel() < 48) ? "30754-01.htm" : "30754-02.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case GILMORE:
						if (cond == 1)
						{
							htmltext = (st.hasQuestItems(ARTICLE_DEAD_HERO)) ? "30754-05.htm" : "30754-09.htm";
						}
						else if (cond == 2)
						{
							htmltext = (st.get("success") != null) ? "30754-16.htm" : "30754-15.htm";
						}
						break;
					
					default:
						if (cond == 2)
						{
							if (st.get("success") != null)
							{
								htmltext = npc.getNpcId() + "-02.htm";
							}
							else
							{
								rewards(st, npc.getNpcId());
								htmltext = npc.getNpcId() + "-01.htm";
							}
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	private void rewards(QuestState st, int npcId)
	{
		switch (npcId)
		{
			case ORVEN:
				if (st.hasQuestItems(CRUCIFIX))
				{
					st.set("success", "1");
					st.takeItems(CRUCIFIX, -1);
					
					final int chance = Rnd.get(100);
					if (chance < 80)
					{
						st.giveItems(1875, 19);
					}
					else if (chance < 95)
					{
						st.giveItems(952, 5);
					}
					else
					{
						st.giveItems(2437, 1);
					}
				}
				break;
			
			case GARVARENTZ:
				if (st.hasQuestItems(OLD_TOTEM))
				{
					st.set("success", "1");
					st.takeItems(OLD_TOTEM, -1);
					
					final int chance = Rnd.get(100);
					if (chance < 55)
					{
						st.giveItems(1882, 70);
					}
					else if (chance < 99)
					{
						st.giveItems(1881, 50);
					}
					else
					{
						st.giveItems(191, 1);
					}
				}
				break;
			
			case KAIEN:
				if (st.hasQuestItems(OLD_HILT))
				{
					st.set("success", "1");
					st.takeItems(OLD_HILT, -1);
					
					final int chance = Rnd.get(100);
					if (chance < 60)
					{
						st.giveItems(1874, 25);
					}
					else if (chance < 85)
					{
						st.giveItems(1887, 10);
					}
					else if (chance < 99)
					{
						st.giveItems(951, 1);
					}
					else
					{
						st.giveItems(133, 1);
					}
				}
				break;
			
			case RODEMAI:
				if (st.hasQuestItems(OLD_KEY))
				{
					st.set("success", "1");
					st.takeItems(OLD_KEY, -1);
					
					final int chance = Rnd.get(100);
					if (chance < 80)
					{
						st.giveItems(1879, 55);
					}
					else if (chance < 95)
					{
						st.giveItems(951, 1);
					}
					else
					{
						st.giveItems(885, 1);
					}
				}
				break;
		}
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		st.dropItems(ARTICLE_DEAD_HERO, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}