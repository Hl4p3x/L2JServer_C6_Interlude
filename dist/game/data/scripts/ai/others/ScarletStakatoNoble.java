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
package ai.others;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Mobius
 * @note Based on python script
 */
public class ScarletStakatoNoble extends Quest
{
	// NPCs
	private static final int SCARLET_STAKATO_NOBLE = 21378;
	private static final int SCARLET_STAKATO_NOBLE_B = 21652;
	
	private ScarletStakatoNoble()
	{
		super(-1, "ai/others");
		
		addKillId(SCARLET_STAKATO_NOBLE);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		if (Rnd.get(100) < 20)
		{
			addSpawn(SCARLET_STAKATO_NOBLE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			addSpawn(SCARLET_STAKATO_NOBLE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			addSpawn(SCARLET_STAKATO_NOBLE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			addSpawn(SCARLET_STAKATO_NOBLE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			addSpawn(SCARLET_STAKATO_NOBLE_B, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new ScarletStakatoNoble();
	}
}
