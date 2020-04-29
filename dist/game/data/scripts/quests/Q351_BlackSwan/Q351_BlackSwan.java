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
package quests.Q351_BlackSwan;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q351_BlackSwan extends Quest
{
	// NPCs
	private static final int GOSTA = 30916;
	private static final int IASON_HEINE = 30969;
	private static final int ROMAN = 30897;
	
	// Items
	private static final int ORDER_OF_GOSTA = 4296;
	private static final int LIZARD_FANG = 4297;
	private static final int BARREL_OF_LEAGUE = 4298;
	private static final int BILL_OF_IASON_HEINE = 4310;
	
	public Q351_BlackSwan()
	{
		super(351, "Black Swan");
		
		registerQuestItems(ORDER_OF_GOSTA, BARREL_OF_LEAGUE, LIZARD_FANG);
		
		addStartNpc(GOSTA);
		addTalkId(GOSTA, IASON_HEINE, ROMAN);
		
		addKillId(20784, 20785, 21639, 21640);
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
		
		if (event.equals("30916-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ORDER_OF_GOSTA, 1);
		}
		else if (event.equals("30969-02a.htm"))
		{
			final int lizardFangs = st.getQuestItemsCount(LIZARD_FANG);
			if (lizardFangs > 0)
			{
				htmltext = "30969-02.htm";
				
				st.takeItems(LIZARD_FANG, -1);
				st.rewardItems(57, lizardFangs * 20);
			}
		}
		else if (event.equals("30969-03a.htm"))
		{
			final int barrels = st.getQuestItemsCount(BARREL_OF_LEAGUE);
			if (barrels > 0)
			{
				htmltext = "30969-03.htm";
				
				st.takeItems(BARREL_OF_LEAGUE, -1);
				st.rewardItems(BILL_OF_IASON_HEINE, barrels);
				
				// Heine explains than player can speak with Roman in order to exchange bills for rewards.
				if (st.getInt("cond") == 1)
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
			}
		}
		else if (event.equals("30969-06.htm"))
		{
			// If no more quest items finish the quest for real, else send a "Return" type HTM.
			if (!st.hasQuestItems(BARREL_OF_LEAGUE, LIZARD_FANG))
			{
				htmltext = "30969-07.htm";
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
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
				htmltext = (player.getLevel() < 32) ? "30916-00.htm" : "30916-01.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case GOSTA:
						htmltext = "30916-04.htm";
						break;
					
					case IASON_HEINE:
						htmltext = "30969-01.htm";
						break;
					
					case ROMAN:
						htmltext = (st.hasQuestItems(BILL_OF_IASON_HEINE)) ? "30897-01.htm" : "30897-02.htm";
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
		
		final int random = Rnd.get(4);
		if (random < 3)
		{
			st.dropItemsAlways(LIZARD_FANG, (random < 2) ? 1 : 2, 0);
			st.dropItems(BARREL_OF_LEAGUE, 1, 0, 50000);
		}
		else
		{
			st.dropItems(BARREL_OF_LEAGUE, 1, 0, (npc.getNpcId() > 20785) ? 30000 : 40000);
		}
		
		return null;
	}
}