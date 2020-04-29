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
package custom.MissQueen;

import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class MissQueen extends Quest
{
	// Rewards
	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;
	
	// Miss Queen locations
	private static final Location[] LOCATIONS =
	{
		// new Location(116224, -181728, -1378, 0), // Dwarven Village
		new Location(114885, -178092, -832, 0), // Dwarven Village
		new Location(45472, 49312, -3072, 53000), // Elven Village
		// new Location(47648, 51296, -2994, 38500), // Elven Village
		// new Location(11340, 15972, -4582, 14000), // Dark Elf Village
		new Location(10968, 17540, -4572, 55000), // Dark Elf Village
		new Location(-14048, 123184, -3120, 32000), // Gludio Village
		new Location(-44979, -113508, -199, 32000), // Orc Village
		new Location(-84119, 243254, -3730, 8000), // Talking Island
		// new Location(-84336, 242156, -3730, 24500), // Talking Island
		new Location(-82032, 150160, -3127, 16500) // Gludin Village
	};
	
	public MissQueen()
	{
		super(-1, "custom");
		
		// Spawn the 11 NPCs.
		for (Location loc : LOCATIONS)
		{
			addSpawn(31760, loc, false, 0);
		}
		
		addStartNpc(31760);
		addTalkId(31760);
		addFirstTalkId(31760);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (event.equals("newbie_coupon"))
		{
			if ((player.getClassId().level() == 0) && (player.getLevel() >= 6) && (player.getLevel() <= 25) && (player.getPkKills() <= 0))
			{
				if (st.getInt("reward_1") == 1)
				{
					htmltext = "31760-01.htm";
				}
				else
				{
					st.setState(State.STARTED);
					htmltext = "31760-02.htm";
					st.set("reward_1", "1");
					st.giveItems(COUPON_ONE, 1);
				}
			}
			else
			{
				htmltext = "31760-03.htm";
			}
		}
		else if (event.equals("traveller_coupon"))
		{
			if ((player.getClassId().level() == 1) && (player.getLevel() >= 6) && (player.getLevel() <= 25) && (player.getPkKills() <= 0))
			{
				if (st.getInt("reward_2") == 1)
				{
					htmltext = "31760-04.htm";
				}
				else
				{
					st.setState(State.STARTED);
					htmltext = "31760-05.htm";
					st.set("reward_2", "1");
					st.giveItems(COUPON_TWO, 1);
				}
			}
			else
			{
				htmltext = "31760-06.htm";
			}
		}
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
		return "31760.htm";
	}
	
	public static void main(String[] args)
	{
		new MissQueen();
	}
}