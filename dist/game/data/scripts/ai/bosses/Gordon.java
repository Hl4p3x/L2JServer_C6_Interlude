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

import java.util.Collection;

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.spawn.Spawn;

/**
 * Gordon AI
 * @author TOFIZ
 * @version $Revision: 1.1 $ $Date: 2008/08/21 $
 */
public class Gordon extends Quest
{
	private static final int GORDON = 29095;
	private static int _npcMoveX = 0;
	private static int _npcMoveY = 0;
	private static int _isWalkTo = 0;
	private static int _npcBlock = 0;
	
	// @formatter:off
	private static final int[][] WALKS =
	{
		{141569, -45908, -2387},
		{142494, -45456, -2397},
		{142922, -44561, -2395},
		{143672, -44130, -2398},
		{144557, -43378, -2325},
		{145839, -43267, -2301},
		{147044, -43601, -2307},
		{148140, -43206, -2303},
		{148815, -43434, -2328},
		{149862, -44151, -2558},
		{151037, -44197, -2708},
		{152555, -42756, -2836},
		{154808, -39546, -3236},
		{155333, -39962, -3272},
		{156531, -41240, -3470},
		{156863, -43232, -3707},
		{156783, -44198, -3764},
		{158169, -45163, -3541},
		{158952, -45479, -3473},
		{160039, -46514, -3634},
		{160244, -47429, -3656},
		{159155, -48109, -3665},
		{159558, -51027, -3523},
		{159396, -53362, -3244},
		{160872, -56556, -2789},
		{160857, -59072, -2613},
		{160410, -59888, -2647},
		{158770, -60173, -2673},
		{156368, -59557, -2638},
		{155188, -59868, -2642},
		{154118, -60591, -2731},
		{153571, -61567, -2821},
		{153457, -62819, -2886},
		{152939, -63778, -3003},
		{151816, -64209, -3120},
		{147655, -64826, -3433},
		{145422, -64576, -3369},
		{144097, -64320, -3404},
		{140780, -61618, -3096},
		{139688, -61450, -3062},
		{138267, -61743, -3056},
		{138613, -58491, -3465},
		{138139, -57252, -3517},
		{139555, -56044, -3310},
		{139107, -54537, -3240},
		{139279, -53781, -3091},
		{139810, -52687, -2866},
		{139657, -52041, -2793},
		{139215, -51355, -2698},
		{139334, -50514, -2594},
		{139817, -49715, -2449},
		{139824, -48976, -2263},
		{140130, -47578, -2213},
		{140483, -46339, -2382},
		{141569, -45908, -2387}
	};
	// @formatter:on
	
	private static boolean _isAttacked = false;
	private static boolean _isSpawned = false;
	
	public Gordon()
	{
		super(-1, "ai/bosses");
		
		addEventId(GORDON, EventType.ON_KILL);
		addEventId(GORDON, EventType.ON_ATTACK);
		addEventId(GORDON, EventType.ON_SPAWN);
		
		// wait 2 minutes after Start AI
		startQuestTimer("check_ai", 120000, null, null, true);
		
		_isSpawned = false;
		_isAttacked = false;
		_isWalkTo = 1;
		_npcMoveX = 0;
		_npcMoveY = 0;
		_npcBlock = 0;
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		int x = WALKS[_isWalkTo - 1][0];
		int y = WALKS[_isWalkTo - 1][1];
		int z = WALKS[_isWalkTo - 1][2];
		
		switch (event)
		{
			case "time_isAttacked":
			{
				_isAttacked = false;
				if (npc.getNpcId() == GORDON)
				{
					npc.setWalking();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, z, 0));
				}
				break;
			}
			case "check_ai":
			{
				cancelQuestTimer("check_ai", null, null);
				if (!_isSpawned)
				{
					final NpcInstance gordon = findTemplate(GORDON);
					if (gordon != null)
					{
						_isSpawned = true;
						startQuestTimer("Start", 1000, gordon, null, true);
						return super.onAdvEvent(event, npc, player);
					}
				}
				break;
			}
			case "Start":
			{
				// startQuestTimer("Start", 1000, npc, null);
				if ((npc != null) && _isSpawned)
				{
					// check if player have Cursed Weapon and in radius
					if (npc.getNpcId() == GORDON)
					{
						final Collection<PlayerInstance> chars = npc.getKnownList().getKnownPlayers().values();
						if ((chars != null) && !chars.isEmpty())
						{
							for (PlayerInstance pc : chars)
							{
								if (pc.isCursedWeaponEquipped() && pc.isInsideRadius(npc, 5000, false, false))
								{
									npc.setRunning();
									((Attackable) npc).addDamageHate(pc, 0, 9999);
									npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, pc);
									_isAttacked = true;
									cancelQuestTimer("time_isAttacked", null, null);
									startQuestTimer("time_isAttacked", 180000, npc, null);
									return super.onAdvEvent(event, npc, player);
								}
							}
						}
					}
					// end check
					if (_isAttacked)
					{
						return super.onAdvEvent(event, npc, player);
					}
					
					if ((npc.getNpcId() == GORDON) && ((npc.getX() - 50) <= x) && ((npc.getX() + 50) >= x) && ((npc.getY() - 50) <= y) && ((npc.getY() + 50) >= y))
					{
						_isWalkTo++;
						if (_isWalkTo > 55)
						{
							_isWalkTo = 1;
						}
						x = WALKS[_isWalkTo - 1][0];
						y = WALKS[_isWalkTo - 1][1];
						z = WALKS[_isWalkTo - 1][2];
						npc.setWalking();
						// TODO: find better way to prevent teleporting to the home location
						npc.getSpawn().setX(x);
						npc.getSpawn().setY(y);
						npc.getSpawn().setZ(z);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, z, 0));
					}
					// Test for unblock Npc
					if ((npc.getX() != _npcMoveX) && (npc.getY() != _npcMoveY))
					{
						_npcMoveX = npc.getX();
						_npcMoveY = npc.getY();
						_npcBlock = 0;
					}
					else if (npc.getNpcId() == GORDON)
					{
						_npcBlock++;
						if (_npcBlock > 2)
						{
							npc.teleToLocation(x, y, z);
							return super.onAdvEvent(event, npc, player);
						}
						if (_npcBlock > 0)
						{
							// TODO: find better way to prevent teleporting to the home location
							npc.getSpawn().setX(x);
							npc.getSpawn().setY(y);
							npc.getSpawn().setZ(z);
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, z, 0));
						}
					}
					// End Test unblock Npc
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(NpcInstance npc)
	{
		if ((npc.getNpcId() == GORDON) && (_npcBlock == 0))
		{
			_isSpawned = true;
			_isWalkTo = 1;
			startQuestTimer("Start", 1000, npc, null);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance player, int damage, boolean isPet)
	{
		if (npc.getNpcId() == GORDON)
		{
			_isAttacked = true;
			cancelQuestTimer("time_isAttacked", null, null);
			startQuestTimer("time_isAttacked", 180000, npc, null);
			if (player != null)
			{
				npc.setRunning();
				((Attackable) npc).addDamageHate(player, 0, 100);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == GORDON)
		{
			cancelQuestTimer("Start", null, null);
			cancelQuestTimer("time_isAttacked", null, null);
			_isSpawned = false;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public NpcInstance findTemplate(int npcId)
	{
		NpcInstance npc = null;
		for (Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if ((spawn != null) && (spawn.getNpcId() == npcId))
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}
	
	public static void main(String[] args)
	{
		new Gordon();
	}
}
