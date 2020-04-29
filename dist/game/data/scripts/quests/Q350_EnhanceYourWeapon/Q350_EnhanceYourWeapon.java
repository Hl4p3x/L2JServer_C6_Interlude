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
package quests.Q350_EnhanceYourWeapon;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q350_EnhanceYourWeapon extends Quest
{
	// NPCs
	private static final int[] NPCS =
	{
		30115,
		30856,
		30194
	};
	// Items
	private static final int RED_SOUL_CRYSTAL0_ID = 4629;
	private static final int GREEN_SOUL_CRYSTAL0_ID = 4640;
	private static final int BLUE_SOUL_CRYSTAL0_ID = 4651;
	
	public Q350_EnhanceYourWeapon()
	{
		super(350, "Enhance Your Weapon");
		
		addStartNpc(NPCS);
		addTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30115-04.htm":
			case "30856-04.htm":
			case "30194-04.htm":
			{
				qs.set("cond", "1");
				qs.setState(State.STARTED);
				qs.playSound("ItemSound.quest_accept");
				break;
			}
			case "30115-09.htm":
			case "30856-09.htm":
			case "30194-09.htm":
			{
				qs.giveItems(RED_SOUL_CRYSTAL0_ID, 1);
				break;
			}
			case "30115-10.htm":
			case "30856-10.htm":
			case "30194-10.htm":
			{
				qs.giveItems(GREEN_SOUL_CRYSTAL0_ID, 1);
				break;
			}
			case "30115-11.htm":
			case "30856-11.htm":
			case "30194-11.htm":
			{
				qs.giveItems(BLUE_SOUL_CRYSTAL0_ID, 1);
				break;
			}
			case "exit.htm":
			{
				qs.exitQuest(true);
				break;
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
		final int id = qs.getState();
		if (id == State.CREATED)
		{
			qs.set("cond", "0");
		}
		if (qs.getInt("cond") == 0)
		{
			htmltext = npcId + "-01.htm";
		}
		else if (check(qs))
		{
			htmltext = npcId + "-03.htm";
		}
		else if ((qs.getQuestItemsCount(RED_SOUL_CRYSTAL0_ID) == 0) && (qs.getQuestItemsCount(GREEN_SOUL_CRYSTAL0_ID) == 0) && (qs.getQuestItemsCount(BLUE_SOUL_CRYSTAL0_ID) == 0))
		{
			htmltext = npcId + "-21.htm";
		}
		
		return htmltext;
	}
	
	private boolean check(QuestState qs)
	{
		for (int i = 4629; i <= 4665; i++)
		{
			if (qs.getQuestItemsCount(i) > 0)
			{
				return true;
			}
		}
		return false;
	}
}