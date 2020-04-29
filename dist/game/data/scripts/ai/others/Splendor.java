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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Splendor extends Quest
{
	// NPCs
	private static final Map<Integer, List<Integer>> NPCS = new HashMap<>();
	static
	{
		// Npc, [NewNpc,% for chance by shot,ModeSpawn]
		// Modespawn 1=> delete and spawn the new npc
		// Modespawn 2=> just add 1 spawn
		// if Quest_Drop = 5 => 25% by shot to change mob
		NPCS.put(21521, Arrays.asList(21522, 5, 1)); // Claw of Splendor
		NPCS.put(21524, Arrays.asList(21525, 5, 1)); // Blade of Splendor
		NPCS.put(21527, Arrays.asList(21528, 5, 1)); // Anger of Splendor
		NPCS.put(21537, Arrays.asList(21538, 5, 1)); // Fang of Splendor
		NPCS.put(21539, Arrays.asList(21540, 100, 2)); // Wailing of Splendor
	}
	
	private Splendor()
	{
		super(-1, "ai/others");
		
		for (int npcId : NPCS.keySet())
		{
			addAttackId(npcId);
		}
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final List<Integer> npcData = NPCS.get(npc.getNpcId());
		if (Rnd.get(100) < (npcData.get(1) * Config.RATE_DROP_QUEST))
		{
			if (npcData.get(2) == 1)
			{
				npc.deleteMe();
				final MonsterInstance newNpc = (MonsterInstance) addSpawn(npcData.get(0), npc);
				newNpc.addDamageHate(attacker, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
			else if (npc.isScriptValue(1))
			{
				return super.onAttack(npc, attacker, damage, isPet);
			}
			else if (npcData.get(2) == 2)
			{
				npc.setScriptValue(1);
				final MonsterInstance newNpc = (MonsterInstance) addSpawn(npcData.get(0), npc);
				newNpc.addDamageHate(attacker, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new Splendor();
	}
}
