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
package quests.Q655_AGrandPlanForTamingWildBeasts;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q655_AGrandPlanForTamingWildBeasts extends Quest
{
	// NPCs
	private static final int MESSENGER = 35627;
	// Items
	private static final int CRYSTAL_PURITY = 8084;
	private static final int LICENSE = 8293;
	
	public Q655_AGrandPlanForTamingWildBeasts()
	{
		super(655, "A Grand Plan for Taming Wild Beasts");
		
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		if (event.equals("a2.htm"))
		{
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equals("a4.htm"))
		{
			if (qs.getQuestItemsCount(CRYSTAL_PURITY) == 10)
			{
				qs.takeItems(CRYSTAL_PURITY, -10);
				qs.giveItems(LICENSE, 1);
				qs.set("cond", "3");
			}
			else
			{
				htmltext = "a5.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		final int npcId = npc.getNpcId();
		final int cond = qs.getInt("cond");
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return "a6.htm";
		}
		if (clan.getLevel() < 4)
		{
			return "a6.htm";
		}
		if (clan.getLeaderName() != player.getName())
		{
			return "a6.htm";
		}
		if (npcId == MESSENGER)
		{
			if (cond == 0)
			{
				htmltext = "a1.htm";
			}
			else if (cond > 1)
			{
				htmltext = "a3.htm";
			}
		}
		else
		{
			htmltext = null;
			npc.showChatWindow(player, 3);
		}
		
		return htmltext;
	}
}