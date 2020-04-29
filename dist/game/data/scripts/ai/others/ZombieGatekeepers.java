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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.quest.Quest;

public class ZombieGatekeepers extends Quest
{
	public ZombieGatekeepers()
	{
		super(-1, "ai");
		addAttackId(22136);
		addAggroRangeEnterId(22136);
	}
	
	private final Map<Integer, List<Creature>> _attackersList = new ConcurrentHashMap<>();
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final int npcObjId = npc.getObjectId();
		final Creature target = isPet ? attacker.getPet() : attacker;
		if (_attackersList.get(npcObjId) == null)
		{
			final List<Creature> player = new ArrayList<>();
			player.add(target);
			_attackersList.put(npcObjId, player);
		}
		else if (!_attackersList.get(npcObjId).contains(target))
		{
			_attackersList.get(npcObjId).add(target);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onAggroRangeEnter(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final int npcObjId = npc.getObjectId();
		final Creature target = isPet ? player.getPet() : player;
		final ItemInstance visitorsMark = player.getInventory().getItemByItemId(8064);
		final ItemInstance fadedVisitorsMark = player.getInventory().getItemByItemId(8065);
		final ItemInstance pagansMark = player.getInventory().getItemByItemId(8067);
		final long mark1 = visitorsMark == null ? 0 : visitorsMark.getCount();
		final long mark2 = fadedVisitorsMark == null ? 0 : fadedVisitorsMark.getCount();
		final long mark3 = pagansMark == null ? 0 : pagansMark.getCount();
		if ((mark1 == 0) && (mark2 == 0) && (mark3 == 0))
		{
			((Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		else if ((_attackersList.get(npcObjId) == null) || !_attackersList.get(npcObjId).contains(target))
		{
			((Attackable) npc).getAggroList().remove(target);
		}
		else
		{
			((Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		final int npcObjId = npc.getObjectId();
		if (_attackersList.get(npcObjId) != null)
		{
			_attackersList.get(npcObjId).clear();
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new ZombieGatekeepers();
	}
}