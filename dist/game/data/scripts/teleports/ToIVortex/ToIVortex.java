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
package teleports.ToIVortex;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;

public class ToIVortex extends Quest
{
	private static final int GREEN_STONE = 4401;
	private static final int BLUE_STONE = 4402;
	private static final int RED_STONE = 4403;
	
	public ToIVortex()
	{
		super(-1, "teleports");
		
		addStartNpc(30952, 30953, 30954);
		addTalkId(30952, 30953, 30954);
		addFirstTalkId(30952, 30953, 30954);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		if (event.equalsIgnoreCase("blue"))
		{
			if (st.hasQuestItems(BLUE_STONE))
			{
				st.takeItems(BLUE_STONE, 1);
				player.teleToLocation(114097, 19935, 935);
			}
			else
			{
				htmltext = "no-items.htm";
			}
		}
		else if (event.equalsIgnoreCase("green"))
		{
			if (st.hasQuestItems(GREEN_STONE))
			{
				st.takeItems(GREEN_STONE, 1);
				player.teleToLocation(110930, 15963, -4378);
			}
			else
			{
				htmltext = "no-items.htm";
			}
		}
		else if (event.equalsIgnoreCase("red"))
		{
			if (st.hasQuestItems(RED_STONE))
			{
				st.takeItems(RED_STONE, 1);
				player.teleToLocation(118558, 16659, 5987);
			}
			else
			{
				htmltext = "no-items.htm";
			}
		}
		st.exitQuest(true);
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, PlayerInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		return npc.getNpcId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new ToIVortex();
	}
}