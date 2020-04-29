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
package quests.Q275_DarkWingedSpies;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q275_DarkWingedSpies extends Quest
{
	// Monsters
	private static final int DARKWING_BAT = 20316;
	private static final int VARANGKA_TRACKER = 27043;
	
	// Items
	private static final int DARKWING_BAT_FANG = 1478;
	private static final int VARANGKA_PARASITE = 1479;
	
	public Q275_DarkWingedSpies()
	{
		super(275, "Dark Winged Spies");
		
		registerQuestItems(DARKWING_BAT_FANG, VARANGKA_PARASITE);
		
		addStartNpc(30567); // Tantus
		addTalkId(30567);
		
		addKillId(DARKWING_BAT, VARANGKA_TRACKER);
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
		
		if (event.equals("30567-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				if (player.getRace() != Race.ORC)
				{
					htmltext = "30567-00.htm";
				}
				else if (player.getLevel() < 11)
				{
					htmltext = "30567-01.htm";
				}
				else
				{
					htmltext = "30567-02.htm";
				}
				break;
			
			case State.STARTED:
				if (st.getInt("cond") == 1)
				{
					htmltext = "30567-04.htm";
				}
				else
				{
					htmltext = "30567-05.htm";
					st.takeItems(DARKWING_BAT_FANG, -1);
					st.takeItems(VARANGKA_PARASITE, -1);
					st.rewardItems(57, 4200);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
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
			case DARKWING_BAT:
				if (st.dropItemsAlways(DARKWING_BAT_FANG, 1, 70))
				{
					st.set("cond", "2");
				}
				else if ((Rnd.get(100) < 10) && (st.getQuestItemsCount(DARKWING_BAT_FANG) > 10) && (st.getQuestItemsCount(DARKWING_BAT_FANG) < 66))
				{
					// Spawn of Varangka Tracker on the npc position.
					addSpawn(VARANGKA_TRACKER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
					st.giveItems(VARANGKA_PARASITE, 1);
				}
				break;
			
			case VARANGKA_TRACKER:
				if (st.hasQuestItems(VARANGKA_PARASITE))
				{
					st.takeItems(VARANGKA_PARASITE, -1);
					if (st.dropItemsAlways(DARKWING_BAT_FANG, 5, 70))
					{
						st.set("cond", "2");
					}
				}
				break;
		}
		
		return null;
	}
}