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

import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Mobius
 * @note Based on python script
 */
public class AncientEgg extends Quest
{
	// NPC
	private static final int EGG = 18344; // Ancient Egg
	// Skill
	private static final int SIGNAL = 5088; // Signal
	
	private AncientEgg()
	{
		super(-1, "ai");
		addAttackId(EGG);
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		attacker.setTarget(attacker);
		attacker.doCast(SkillTable.getInstance().getInfo(SIGNAL, 1));
		return null;
	}
	
	public static void main(String[] args)
	{
		new AncientEgg();
	}
}
