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
package teleports.RaceTrack;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class RaceTrack extends Quest
{
	private static final int RACE_MANAGER = 30995;
	
	private static final Map<Integer, Location> RETURN_LOCATIONS = new HashMap<>();
	static
	{
		RETURN_LOCATIONS.put(30320, new Location(-80826, 149775, -3043)); // RICHLIN
		RETURN_LOCATIONS.put(30256, new Location(-12672, 122776, -3116)); // BELLA
		RETURN_LOCATIONS.put(30059, new Location(15670, 142983, -2705)); // TRISHA
		RETURN_LOCATIONS.put(30080, new Location(83400, 147943, -3404)); // CLARISSA
		RETURN_LOCATIONS.put(30899, new Location(111409, 219364, -3545)); // FLAUEN
		RETURN_LOCATIONS.put(30177, new Location(82956, 53162, -1495)); // VALENTIA
		RETURN_LOCATIONS.put(30848, new Location(146331, 25762, -2018)); // ELISA
		RETURN_LOCATIONS.put(30233, new Location(116819, 76994, -2714)); // ESMERALDA
		RETURN_LOCATIONS.put(31320, new Location(43835, -47749, -792)); // ILYANA
		RETURN_LOCATIONS.put(31275, new Location(147930, -55281, -2728)); // TATIANA
		RETURN_LOCATIONS.put(31964, new Location(87386, -143246, -1293)); // BILIA
		RETURN_LOCATIONS.put(31210, new Location(12882, 181053, -3560)); // RACE TRACK GK
	}
	
	public RaceTrack()
	{
		super(-1, "teleports");
		
		addStartNpc(30320, 30256, 30059, 30080, 30899, 30177, 30848, 30233, 31320, 31275, 31964, 31210);
		addTalkId(RACE_MANAGER, 30320, 30256, 30059, 30080, 30899, 30177, 30848, 30233, 31320, 31275, 31964, 31210);
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (RETURN_LOCATIONS.containsKey(npc.getNpcId()))
		{
			player.teleToLocation(12661, 181687, -3560);
			st.setState(State.STARTED);
			st.set("id", Integer.toString(npc.getNpcId()));
		}
		else if (st.isStarted() && (npc.getNpcId() == RACE_MANAGER))
		{
			final Location loc = RETURN_LOCATIONS.get(st.getInt("id"));
			player.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
			st.exitQuest(true);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new RaceTrack();
	}
}