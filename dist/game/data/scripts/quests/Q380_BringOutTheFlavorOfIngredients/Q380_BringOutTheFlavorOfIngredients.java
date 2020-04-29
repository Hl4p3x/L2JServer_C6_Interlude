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
package quests.Q380_BringOutTheFlavorOfIngredients;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q380_BringOutTheFlavorOfIngredients extends Quest
{
	// Monsters
	private static final int DIRE_WOLF = 20205;
	private static final int KADIF_WEREWOLF = 20206;
	private static final int GIANT_MIST_LEECH = 20225;
	
	// Items
	private static final int RITRON_FRUIT = 5895;
	private static final int MOON_FACE_FLOWER = 5896;
	private static final int LEECH_FLUIDS = 5897;
	private static final int ANTIDOTE = 1831;
	
	// Rewards
	private static final int RITRON_JELLY = 5960;
	private static final int JELLY_RECIPE = 5959;
	
	public Q380_BringOutTheFlavorOfIngredients()
	{
		super(380, "Bring Out the Flavor of Ingredients!");
		
		registerQuestItems(RITRON_FRUIT, MOON_FACE_FLOWER, LEECH_FLUIDS);
		
		addStartNpc(30069); // Rollant
		addTalkId(30069);
		
		addKillId(DIRE_WOLF, KADIF_WEREWOLF, GIANT_MIST_LEECH);
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
		
		if (event.equals("30069-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30069-12.htm"))
		{
			st.giveItems(JELLY_RECIPE, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 24) ? "30069-00.htm" : "30069-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "30069-06.htm";
				}
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(ANTIDOTE) >= 2)
					{
						htmltext = "30069-07.htm";
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(RITRON_FRUIT, -1);
						st.takeItems(MOON_FACE_FLOWER, -1);
						st.takeItems(LEECH_FLUIDS, -1);
						st.takeItems(ANTIDOTE, 2);
					}
					else
					{
						htmltext = "30069-06.htm";
					}
				}
				else if (cond == 3)
				{
					htmltext = "30069-08.htm";
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else if (cond == 4)
				{
					htmltext = "30069-09.htm";
					st.set("cond", "5");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else if (cond == 5)
				{
					htmltext = "30069-10.htm";
					st.set("cond", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else if (cond == 6)
				{
					st.giveItems(RITRON_JELLY, 1);
					if (Rnd.get(100) < 55)
					{
						htmltext = "30069-11.htm";
					}
					else
					{
						htmltext = "30069-13.htm";
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case DIRE_WOLF:
				if (st.dropItems(RITRON_FRUIT, 1, 4, 100000) && (st.getQuestItemsCount(MOON_FACE_FLOWER) == 20) && (st.getQuestItemsCount(LEECH_FLUIDS) == 10))
				{
					st.set("cond", "2");
				}
				break;
			
			case KADIF_WEREWOLF:
				if (st.dropItems(MOON_FACE_FLOWER, 1, 20, 500000) && (st.getQuestItemsCount(RITRON_FRUIT) == 4) && (st.getQuestItemsCount(LEECH_FLUIDS) == 10))
				{
					st.set("cond", "2");
				}
				break;
			
			case GIANT_MIST_LEECH:
				if (st.dropItems(LEECH_FLUIDS, 1, 10, 500000) && (st.getQuestItemsCount(RITRON_FRUIT) == 4) && (st.getQuestItemsCount(MOON_FACE_FLOWER) == 20))
				{
					st.set("cond", "2");
				}
				break;
		}
		
		return null;
	}
}