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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class QueenAnt extends Quest
{
	private static final int QUEEN = 29001;
	private static final int LARVA = 29002;
	private static final int NURSE = 29003;
	private static final int GUARD = 29004;
	private static final int ROYAL = 29005;
	
	// QUEEN Status Tracking :
	private static final int LIVE = 0; // Queen Ant is spawned.
	private static final int DEAD = 1; // Queen Ant has been killed.
	
	@SuppressWarnings("unused")
	private static BossZone _zone;
	private MonsterInstance _larva = null;
	private MonsterInstance _queen = null;
	private final List<MonsterInstance> _minions = new CopyOnWriteArrayList<>();
	private final List<MonsterInstance> _nurses = new CopyOnWriteArrayList<>();
	
	enum Event
	{
		QUEEN_SPAWN, /* CHECK_QA_ZONE, */
		CHECK_MINIONS_ZONE,
		CHECK_NURSE_ALIVE,
		ACTION,
		DESPAWN_MINIONS,
		SPAWN_ROYAL,
		NURSES_SPAWN,
		RESPAWN_ROYAL,
		RESPAWN_NURSE,
		LARVA_DESPAWN,
		HEAL
	}
	
	public QueenAnt()
	{
		super(-1, "ai/bosses");
		
		final int[] mobs =
		{
			QUEEN,
			LARVA,
			NURSE,
			GUARD,
			ROYAL
		};
		for (int mob : mobs)
		{
			addEventId(mob, EventType.ON_KILL);
			addEventId(mob, EventType.ON_ATTACK);
		}
		
		_zone = GrandBossManager.getInstance().getZone(-21610, 181594, -5734);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(QUEEN);
		final Integer status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		
		switch (status)
		{
			case DEAD:
			{
				final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
				if (temp > 0)
				{
					startQuestTimer("QUEEN_SPAWN", temp, null, null);
				}
				else
				{
					final GrandBossInstance queen = (GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
					if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
					{
						Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
					}
					GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
					GrandBossManager.getInstance().addBoss(queen);
					spawnBoss(queen);
				}
			}
				break;
			case LIVE:
			{
				/*
				 * int loc_x = info.getInteger("loc_x"); int loc_y = info.getInteger("loc_y"); int loc_z = info.getInteger("loc_z"); int heading = info.getInteger("heading");
				 */
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				final GrandBossInstance queen = (GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().addBoss(queen);
				queen.setCurrentHpMp(hp, mp);
				spawnBoss(queen);
			}
				break;
			default:
			{
				final GrandBossInstance queen = (GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
				GrandBossManager.getInstance().addBoss(queen);
				spawnBoss(queen);
			}
		}
	}
	
	private void spawnBoss(GrandBossInstance npc)
	{
		startQuestTimer("ACTION", 10000, npc, null, true);
		npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
		startQuestTimer("SPAWN_ROYAL", 1000, npc, null);
		startQuestTimer("NURSES_SPAWN", 1000, npc, null);
		startQuestTimer("CHECK_MINIONS_ZONE", 30000, npc, null, true);
		startQuestTimer("HEAL", 1000, null, null, true);
		_queen = npc;
		_larva = (MonsterInstance) addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0);
		_larva.setUnkillable(true);
		_larva.setImmobilized(true);
		_larva.setAttackDisabled(true);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final Event eventEnum = Event.valueOf(event);
		
		switch (eventEnum)
		{
			case QUEEN_SPAWN:
			{
				final GrandBossInstance queen = (GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + queen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
				GrandBossManager.getInstance().addBoss(queen);
				spawnBoss(queen);
				break;
			}
			case LARVA_DESPAWN:
			{
				_larva.decayMe();
				break;
			}
			case NURSES_SPAWN:
			{
				final int radius = 400;
				for (int i = 0; i < 6; i++)
				{
					final int x = (int) (radius * Math.cos(i * 1.407)); // 1.407~2pi/6
					final int y = (int) (radius * Math.sin(i * 1.407));
					_nurses.add((MonsterInstance) addSpawn(NURSE, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
					_nurses.get(i).setAttackDisabled(true);
				}
				break;
			}
			case SPAWN_ROYAL:
			{
				final int radius = 400;
				for (int i = 0; i < 8; i++)
				{
					final int x = (int) (radius * Math.cos(i * .7854)); // .7854~2pi/8
					final int y = (int) (radius * Math.sin(i * .7854));
					_minions.add((MonsterInstance) addSpawn(ROYAL, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
				}
				break;
			}
			case RESPAWN_ROYAL:
			{
				_minions.add((MonsterInstance) addSpawn(ROYAL, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));
				break;
			}
			case RESPAWN_NURSE:
			{
				_nurses.add((MonsterInstance) addSpawn(NURSE, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));
				break;
			}
			case DESPAWN_MINIONS:
			{
				for (int i = 0; i < _minions.size(); i++)
				{
					final Attackable mob = _minions.get(i);
					if (mob != null)
					{
						mob.decayMe();
					}
				}
				for (int k = 0; k < _nurses.size(); k++)
				{
					final MonsterInstance nurse = _nurses.get(k);
					if (nurse != null)
					{
						nurse.decayMe();
					}
				}
				_nurses.clear();
				_minions.clear();
				break;
			}
			case CHECK_MINIONS_ZONE:
			{
				for (int i = 0; i < _minions.size(); i++)
				{
					final Attackable mob = _minions.get(i);
					if ((mob != null) && !mob.isInsideRadius(npc.getX(), npc.getY(), 700, false))/* !_Zone.isInsideZone(mob)) */
					{
						mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					}
				}
				break;
			}
			case CHECK_NURSE_ALIVE:
			{
				int deadNurses = 0;
				for (MonsterInstance nurse : _nurses)
				{
					if (nurse.isDead())
					{
						deadNurses++;
					}
				}
				if (deadNurses == _nurses.size())
				{
					startQuestTimer("RESPAWN_NURSE", Config.QA_RESP_NURSE * 1000, npc, null);
				}
				break;
			}
			case ACTION:
			{
				if (Rnd.get(3) == 0)
				{
					if (Rnd.nextBoolean())
					{
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
					}
					else
					{
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
					}
				}
				break;
			}
			case HEAL:
			{
				boolean notCasting;
				final boolean larvaNeedHeal = (_larva != null) && (_larva.getCurrentHp() < _larva.getMaxHp());
				final boolean queenNeedHeal = (_queen != null) && (_queen.getCurrentHp() < _queen.getMaxHp());
				boolean nurseNeedHeal = false;
				for (MonsterInstance nurse : _nurses)
				{
					nurseNeedHeal = (nurse != null) && (nurse.getCurrentHp() < nurse.getMaxHp());
					if ((nurse == null) || nurse.isDead() || nurse.isCastingNow())
					{
						continue;
					}
					notCasting = nurse.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST;
					if (larvaNeedHeal)
					{
						if ((nurse.getTarget() != _larva) || notCasting)
						{
							getIntoPosition(nurse, _larva);
							nurse.setTarget(_larva);
							nurse.doCast(SkillTable.getInstance().getInfo(4020, 1));
							nurse.doCast(SkillTable.getInstance().getInfo(4024, 1));
						}
						continue;
					}
					if (queenNeedHeal)
					{
						if ((nurse.getTarget() != _queen) || notCasting)
						{
							getIntoPosition(nurse, _queen);
							nurse.setTarget(_queen);
							nurse.doCast(SkillTable.getInstance().getInfo(4020, 1));
						}
						continue;
					}
					if (nurseNeedHeal && ((nurse.getTarget() != nurse) || notCasting))
					{
						for (int k = 0; k < _nurses.size(); k++)
						{
							getIntoPosition(_nurses.get(k), nurse);
							_nurses.get(k).setTarget(nurse);
							_nurses.get(k).doCast(SkillTable.getInstance().getInfo(4020, 1));
						}
					}
					if (notCasting && (nurse.getTarget() != null))
					{
						nurse.setTarget(null);
					}
				}
				break;
			}
			default:
			{
				LOGGER.info("QUEEN: Not defined event: " + event + "!");
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if (npcId == NURSE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		final Integer status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		if (npcId == QUEEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
			GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD);
			// time is 36hour +/- 17hour
			final long respawnTime = (Config.QA_RESP_FIRST + Rnd.get(Config.QA_RESP_SECOND)) * 3600000;
			startQuestTimer("QUEEN_SPAWN", respawnTime, null, null);
			startQuestTimer("LARVA_DESPAWN", 4 * 60 * 60 * 1000, null, null);
			cancelQuestTimer("ACTION", npc, null);
			cancelQuestTimer("SPAWN_ROYAL", npc, null);
			cancelQuestTimer("CHECK_MINIONS_ZONE", npc, null);
			cancelQuestTimer("CHECK_NURSE_ALIVE", npc, null);
			cancelQuestTimer("HEAL", null, null);
			// cancelQuestTimer("CHECK_QA_ZONE", npc, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatSet info = GrandBossManager.getInstance().getStatSet(QUEEN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(QUEEN, info);
			startQuestTimer("DESPAWN_MINIONS", 10000, null, null);
		}
		else if ((status == LIVE) && ((npcId == ROYAL) || (npcId == NURSE)))
		{
			npc.decayMe();
			if (_minions.contains(npc))
			{
				_minions.remove(npc);
			}
			else
			{
				_nurses.remove(npc);
			}
			
			if (npcId == ROYAL)
			{
				startQuestTimer("RESPAWN_ROYAL", (Config.QA_RESP_ROYAL + Rnd.get(40)) * 1000, npc, null);
			}
			else // if (npcId == NURSE)
			{
				startQuestTimer("CHECK_NURSE_ALIVE", 1000, npc, null);
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public void getIntoPosition(MonsterInstance nurse, MonsterInstance caller)
	{
		if (!nurse.isInsideRadius(caller, 300, false, false))
		{
			nurse.getAI().moveToPawn(caller, 300);
		}
	}
	
	public static void main(String[] args)
	{
		new QueenAnt();
	}
}