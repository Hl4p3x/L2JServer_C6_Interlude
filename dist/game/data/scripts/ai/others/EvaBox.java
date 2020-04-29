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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Mobius
 * @note Based on python script
 */
public class EvaBox extends Quest
{
	// NPC
	private static final int BOX = 32342;
	// Skills
	private static final List<Integer> KISS_OF_EVA = new ArrayList<>();
	static
	{
		KISS_OF_EVA.add(1073);
		KISS_OF_EVA.add(3141);
		KISS_OF_EVA.add(3252);
	}
	// Items
	private static final int[] REWARDS =
	{
		9692,
		9693
	};
	
	private EvaBox()
	{
		super(-1, "ai/others");
		
		addKillId(BOX);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		for (Effect effect : killer.getAllEffects())
		{
			if (KISS_OF_EVA.contains(effect.getSkill().getId()))
			{
				final ItemInstance reward = ItemTable.getInstance().createItem("EvaBox", Rnd.get(REWARDS.length), 1, killer);
				reward.dropMe(npc, npc.getX(), npc.getY(), npc.getZ());
				break;
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new EvaBox();
	}
}
