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
package quests.Q331_ArrowOfVengeance;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q331_ArrowOfVengeance extends Quest
{
	// Items
	private static final int HARPY_FEATHER = 1452;
	private static final int MEDUSA_VENOM = 1453;
	private static final int WYRM_TOOTH = 1454;
	
	public Q331_ArrowOfVengeance()
	{
		super(331, "Arrow of Vengeance");
		
		registerQuestItems(HARPY_FEATHER, MEDUSA_VENOM, WYRM_TOOTH);
		
		addStartNpc(30125); // Belton
		addTalkId(30125);
		
		addKillId(20145, 20158, 20176);
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
		
		if (event.equals("30125-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30125-06.htm"))
		{
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
				htmltext = (player.getLevel() < 32) ? "30125-01.htm" : "30125-02.htm";
				break;
			
			case State.STARTED:
				final int harpyFeather = st.getQuestItemsCount(HARPY_FEATHER);
				final int medusaVenom = st.getQuestItemsCount(MEDUSA_VENOM);
				final int wyrmTooth = st.getQuestItemsCount(WYRM_TOOTH);
				
				if ((harpyFeather + medusaVenom + wyrmTooth) > 0)
				{
					htmltext = "30125-05.htm";
					st.takeItems(HARPY_FEATHER, -1);
					st.takeItems(MEDUSA_VENOM, -1);
					st.takeItems(WYRM_TOOTH, -1);
					
					int reward = (harpyFeather * 78) + (medusaVenom * 88) + (wyrmTooth * 92);
					if ((harpyFeather + medusaVenom + wyrmTooth) > 10)
					{
						reward += 3100;
					}
					
					st.rewardItems(57, reward);
				}
				else
				{
					htmltext = "30125-04.htm";
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
		
		switch (npc.getNpcId())
		{
			case 20145:
				st.dropItems(HARPY_FEATHER, 1, 0, 500000);
				break;
			
			case 20158:
				st.dropItems(MEDUSA_VENOM, 1, 0, 500000);
				break;
			
			case 20176:
				st.dropItems(WYRM_TOOTH, 1, 0, 500000);
				break;
		}
		
		return null;
	}
}