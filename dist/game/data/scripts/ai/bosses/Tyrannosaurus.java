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
package ai.bosses;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Mobius
 */
public class Tyrannosaurus extends Quest
{
	// NPCs
	private static final int[] TREX =
	{
		22215, // Tyrannosaurus
		22216, // Tyrannosaurus
		22217, // Tyrannosaurus
	};
	// Locations
	private static final Location[] SPAWNS =
	{
		new Location(19506, -15772, -3080, 49220),
		new Location(22253, -17062, -2976, 47449),
		new Location(23348, -20888, -2672, 0),
		new Location(25047, -18477, -2712, 0),
		new Location(27331, -16669, -2664, 21125),
		new Location(27714, -14692, -2552, 0),
		new Location(26555, -11574, -2464, 28153),
		new Location(21295, -11123, -2784, 41953),
		new Location(19605, -11234, -2816, 57601),
		new Location(19220, -11806, -2776, 0),
		new Location(26740, -16596, -2688, 13790)
	};
	
	public Tyrannosaurus()
	{
		super(-1, "ai/bosses");
		addKillId(TREX);
		addSpawn(TREX[Rnd.get(TREX.length)], SPAWNS[Rnd.get(SPAWNS.length)], false, 0);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		ThreadPool.schedule(() -> addSpawn(TREX[Rnd.get(TREX.length)], SPAWNS[Rnd.get(SPAWNS.length)], false, 0), 1800000);
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Tyrannosaurus();
	}
}
