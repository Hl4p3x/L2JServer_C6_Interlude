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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;

public class SummonMinions extends Quest
{
	private static int HasSpawned;
	private static Set<Integer> myTrackingSet = new CopyOnWriteArraySet<>(); // Used to track instances of npcs
	private final Map<Integer, List<PlayerInstance>> _attackersList = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer[]> MINIONS = new HashMap<>();
	static
	{
		MINIONS.put(20767, new Integer[]
		{
			20768,
			20769,
			20770
		}); // Timak Orc Troop
		// MINIONS.put(22030, new Integer[]{22045, 22047, 22048}); //Ragna Orc Shaman
		// MINIONS.put(22032, new Integer[]{22036}); //Ragna Orc Warrior - summons shaman but not 22030 ><
		// MINIONS.put(22038, new Integer[]{22037}); //Ragna Orc Hero
		MINIONS.put(21524, new Integer[]
		{
			21525
		}); // Blade of Splendor
		MINIONS.put(21531, new Integer[]
		{
			21658
		}); // Punishment of Splendor
		MINIONS.put(21539, new Integer[]
		{
			21540
		}); // Wailing of Splendor
		MINIONS.put(22257, new Integer[]
		{
			18364,
			18364
		}); // Island Guardian
		MINIONS.put(22258, new Integer[]
		{
			18364,
			18364
		}); // White Sand Mirage
		MINIONS.put(22259, new Integer[]
		{
			18364,
			18364
		}); // Muddy Coral
		MINIONS.put(22260, new Integer[]
		{
			18364,
			18364
		}); // Kleopora
		MINIONS.put(22261, new Integer[]
		{
			18365,
			18365
		}); // Seychelles
		MINIONS.put(22262, new Integer[]
		{
			18365,
			18365
		}); // Naiad
		MINIONS.put(22263, new Integer[]
		{
			18365,
			18365
		}); // Sonneratia
		MINIONS.put(22264, new Integer[]
		{
			18366,
			18366
		}); // Castalia
		MINIONS.put(22265, new Integer[]
		{
			18366,
			18366
		}); // Chrysocolla
		MINIONS.put(22266, new Integer[]
		{
			18366,
			18366
		}); // Pythia
	}
	
	public SummonMinions()
	{
		super(-1, "ai");
		final int[] mobs =
		{
			20767,
			21524,
			21531,
			21539,
			22257,
			22258,
			22259,
			22260,
			22261,
			22262,
			22263,
			22264,
			22265,
			22266
		};
		
		for (int mob : mobs)
		{
			addEventId(mob, EventType.ON_KILL);
			addEventId(mob, EventType.ON_ATTACK);
		}
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		final int npcObjId = npc.getObjectId();
		if (MINIONS.containsKey(npcId))
		{
			if (!myTrackingSet.contains(npcObjId)) // this allows to handle multiple instances of npc
			{
				myTrackingSet.add(npcObjId);
				HasSpawned = npcObjId;
			}
			if (HasSpawned == npcObjId)
			{
				if ((npcId == 22030) || (npcId == 22032) || (npcId == 22038)) // mobs that summon minions only on certain hp
				{
					if (npc.getStatus().getCurrentHp() < (npc.getMaxHp() / 2))
					{
						HasSpawned = 0;
						if (Rnd.get(100) < 33) // mobs that summon minions only on certain chance
						{
							final Integer[] minions = MINIONS.get(npcId);
							for (Integer minion : minions)
							{
								final Attackable newNpc = (Attackable) addSpawn(minion, (npc.getX() + Rnd.get(-150, 150)), (npc.getY() + Rnd.get(-150, 150)), npc.getZ(), 0, false, 0);
								newNpc.setRunning();
								newNpc.addDamageHate(attacker, 0, 999);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
							}
						}
					}
				}
				else if ((npcId == 22257) || (npcId == 22258) || (npcId == 22259) || (npcId == 22260) || (npcId == 22261) || (npcId == 22262) || (npcId == 22263) || (npcId == 22264) || (npcId == 22265) || (npcId == 22266))
				{
					if (isPet)
					{
						attacker = attacker.getPet().getOwner();
					}
					if (attacker.getParty() != null)
					{
						for (PlayerInstance member : attacker.getParty().getPartyMembers())
						{
							if (_attackersList.get(npcObjId) == null)
							{
								final List<PlayerInstance> player = new ArrayList<>();
								player.add(member);
								_attackersList.put(npcObjId, player);
							}
							else if (!_attackersList.get(npcObjId).contains(member))
							{
								_attackersList.get(npcObjId).add(member);
							}
						}
					}
					else if (_attackersList.get(npcObjId) == null)
					{
						final List<PlayerInstance> player = new ArrayList<>();
						player.add(attacker);
						_attackersList.put(npcObjId, player);
					}
					else if (!_attackersList.get(npcObjId).contains(attacker))
					{
						_attackersList.get(npcObjId).add(attacker);
					}
					if (((attacker.getParty() != null) && (attacker.getParty().getMemberCount() > 2)) || (_attackersList.get(npcObjId).size() > 2)) // Just to make sure..
					{
						HasSpawned = 0;
						final Integer[] minions = MINIONS.get(npcId);
						for (Integer minion : minions)
						{
							final Attackable newNpc = (Attackable) addSpawn(minion, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
							newNpc.setRunning();
							newNpc.addDamageHate(attacker, 0, 999);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
						}
					}
				}
				else
				// mobs without special conditions
				{
					HasSpawned = 0;
					final Integer[] minions = MINIONS.get(npcId);
					if (npcId != 20767)
					{
						for (Integer minion : minions)
						{
							final Attackable newNpc = (Attackable) addSpawn(minion, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0);
							newNpc.setRunning();
							newNpc.addDamageHate(attacker, 0, 999);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
						}
					}
					else
					{
						for (Integer minion : minions)
						{
							addSpawn(minion, (npc.getX() + Rnd.get(-100, 100)), (npc.getY() + Rnd.get(-100, 100)), npc.getZ(), 0, false, 0);
						}
					}
					if (npcId == 20767)
					{
						npc.broadcastPacket(new CreatureSay(npcObjId, ChatType.GENERAL, npc.getName(), "Come out, you children of darkness!"));
					}
				}
			}
		}
		if (_attackersList.get(npcObjId) != null)
		{
			_attackersList.get(npcObjId).clear();
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		final int npcObjId = npc.getObjectId();
		if (MINIONS.containsKey(npcId))
		{
			myTrackingSet.remove(npcObjId);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new SummonMinions();
	}
}
