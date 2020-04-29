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
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Mobius
 * @note Based on python script
 */
public class RetreatOnAttack extends Quest
{
	// NPCs
	private static final int EPLY = 20432;
	private static final int OL_MAHUM_GUARD = 20058;
	// Misc
	private static final String[] OL_MAHUM_GUARD_TEXT =
	{
		"I'll be back!",
		"You are stronger than expected..."
	};
	
	private RetreatOnAttack()
	{
		super(-1, "ai/others");
		
		addAttackId(EPLY, OL_MAHUM_GUARD);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		if (event.equals("Retreat") && (npc != null) && (player != null))
		{
			npc.setAfraid(false);
			((Attackable) npc).addDamageHate(player, 0, 100);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return null;
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if ((npcId == EPLY) || ((npc.getStatus().getCurrentHp() <= ((npc.getMaxHp() * 50) / 100)) && (Rnd.get(100) < 10)))
		{
			if (npcId == OL_MAHUM_GUARD)
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), OL_MAHUM_GUARD_TEXT[Rnd.get(OL_MAHUM_GUARD_TEXT.length)]));
			}
			int posX = npc.getX();
			int posY = npc.getY();
			final int posZ = npc.getZ();
			int signX = -500;
			int signY = -500;
			if (npc.getX() > attacker.getX())
			{
				signX = 500;
			}
			if (npc.getY() > attacker.getY())
			{
				signY = 500;
			}
			posX = posX + signX;
			posY = posY + signY;
			npc.setAfraid(true);
			npc.setRunning();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ));
			startQuestTimer("Retreat", 10000, npc, attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new RetreatOnAttack();
	}
}
