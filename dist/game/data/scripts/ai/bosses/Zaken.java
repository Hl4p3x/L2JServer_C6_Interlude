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

import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;

/**
 * Zaken AI
 */
public class Zaken extends Quest
{
	protected static final Logger LOGGER = Logger.getLogger(Zaken.class.getName());
	
	private static final int ZAKEN = 29022;
	private static final int DOLL_BLADER_B = 29023;
	private static final int VALE_MASTER_B = 29024;
	private static final int PIRATES_ZOMBIE_CAPTAIN_B = 29026;
	private static final int PIRATES_ZOMBIE_B = 29027;
	private static final int[] X_COORDS =
	{
		53950,
		55980,
		54950,
		55970,
		53930,
		55970,
		55980,
		54960,
		53950,
		53930,
		55970,
		55980,
		54960,
		53950,
		53930
	};
	private static final int[] Y_COORDS =
	{
		219860,
		219820,
		218790,
		217770,
		217760,
		217770,
		219920,
		218790,
		219860,
		217760,
		217770,
		219920,
		218790,
		219860,
		217760
	};
	private static final int[] Z_COORDS =
	{
		-3488,
		-3488,
		-3488,
		-3488,
		-3488,
		-3216,
		-3216,
		-3216,
		-3216,
		-3216,
		-2944,
		-2944,
		-2944,
		-2944,
		-2944
	};
	// Zaken status tracking
	private static final byte ALIVE = 0; // Zaken is spawned.
	private static final byte DEAD = 1; // Zaken has been killed.
	// Misc
	private static BossZone _Zone;
	private int _1001 = 0; // used for first cancel of QuestTimer "1001"
	private int _ai0 = 0; // used for zaken coords updater
	private int _ai1 = 0; // used for X coord tracking for non-random teleporting in zaken's self teleport skill
	private int _ai2 = 0; // used for Y coord tracking for non-random teleporting in zaken's self teleport skill
	private int _ai3 = 0; // used for Z coord tracking for non-random teleporting in zaken's self teleport skill
	private int _ai4 = 0; // used for spawning minions cycles
	private int _quest0 = 0; // used for teleporting progress
	private int _quest1 = 0; // used for most hated players progress
	private int _quest2 = 0; // used for zaken HP check for teleport
	private PlayerInstance c_quest0 = null; // 1st player used for area teleport
	private PlayerInstance c_quest1 = null; // 2nd player used for area teleport
	private PlayerInstance c_quest2 = null; // 3rd player used for area teleport
	private PlayerInstance c_quest3 = null; // 4th player used for area teleport
	private PlayerInstance c_quest4 = null; // 5th player used for area teleport
	
	public Zaken()
	{
		super(-1, "ai/bosses");
		
		// Zaken doors handling
		ThreadPool.scheduleAtFixedRate(() ->
		{
			try
			{
				if (getTimeHour() == 0)
				{
					LOGGER.info("Zaken door id 21240006 opened, game time 00.00.");
					DoorData.getInstance().getDoor(21240006).openMe();
					ThreadPool.schedule(() ->
					{
						try
						{
							LOGGER.info("Zaken door id 21240006 closed.");
							DoorData.getInstance().getDoor(21240006).closeMe();
						}
						catch (Throwable e)
						{
							LOGGER.warning("Cannot close door ID: 21240006 " + e);
						}
					}, 300000L);
				}
			}
			catch (Throwable e)
			{
				LOGGER.warning("Cannot open door ID: 21240006 " + e);
			}
		}, 2000L, 600000L);
		
		addEventId(ZAKEN, EventType.ON_KILL);
		addEventId(ZAKEN, EventType.ON_ATTACK);
		
		_Zone = GrandBossManager.getInstance().getZone(55312, 219168, -3223);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(ZAKEN);
		final Integer status = GrandBossManager.getInstance().getBossStatus(ZAKEN);
		if (status == DEAD)
		{
			// load the unlock date and time for zaken from DB
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			// if zaken is locked until a certain time, mark it so and start the unlock timer
			// the unlock time has not yet expired.
			if (temp > 0)
			{
				startQuestTimer("zaken_unlock", temp, null, null);
			}
			else
			{
				// the time has already expired while the server was offline. Immediately spawn zaken.
				final GrandBossInstance zaken = (GrandBossInstance) addSpawn(ZAKEN, 55312, 219168, -3223, 0, false, 0);
				GrandBossManager.getInstance().setBossStatus(ZAKEN, ALIVE);
				spawnBoss(zaken);
			}
		}
		else
		{
			final int loc_x = info.getInt("loc_x");
			final int loc_y = info.getInt("loc_y");
			final int loc_z = info.getInt("loc_z");
			final int heading = info.getInt("heading");
			final int hp = info.getInt("currentHP");
			final int mp = info.getInt("currentMP");
			final GrandBossInstance zaken = (GrandBossInstance) addSpawn(ZAKEN, loc_x, loc_y, loc_z, heading, false, 0);
			zaken.setCurrentHpMp(hp, mp);
			spawnBoss(zaken);
		}
	}
	
	public void spawnBoss(GrandBossInstance npc)
	{
		if (npc == null)
		{
			LOGGER.warning("Zaken AI failed to load, missing Zaken in grandboss_data.sql");
			return;
		}
		GrandBossManager.getInstance().addBoss(npc);
		
		npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
		_ai0 = 0;
		_ai1 = npc.getX();
		_ai2 = npc.getY();
		_ai3 = npc.getZ();
		_quest0 = 0;
		_quest1 = 0;
		_quest2 = 3;
		if (_Zone == null)
		{
			LOGGER.warning("Zaken AI failed to load, missing zone for Zaken");
			return;
		}
		if (_Zone.isInsideZone(npc))
		{
			_ai4 = 1;
			startQuestTimer("1003", 1700, null, null);
		}
		_1001 = 1;
		startQuestTimer("1001", 1000, npc, null); // buffs,random teleports
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final Integer status = GrandBossManager.getInstance().getBossStatus(ZAKEN);
		if (((status == DEAD) && (event == null)) || !event.equals("zaken_unlock"))
		{
			return super.onAdvEvent(event, npc, player);
		}
		
		if (event.equals("1001"))
		{
			if (_1001 == 1)
			{
				_1001 = 0;
				cancelQuestTimer("1001", npc, null);
			}
			int sk4223 = 0;
			int sk4227 = 0;
			final Effect[] effects = npc.getAllEffects();
			if ((effects != null) && (effects.length != 0))
			{
				for (Effect e : effects)
				{
					if (e.getSkill().getId() == 4227)
					{
						sk4227 = 1;
					}
					if (e.getSkill().getId() == 4223)
					{
						sk4223 = 1;
					}
				}
			}
			if (getTimeHour() < 5)
			{
				if (sk4223 == 1) // use night face if zaken have day face
				{
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getInfo(4224, 1));
					_ai1 = npc.getX();
					_ai2 = npc.getY();
					_ai3 = npc.getZ();
				}
				if (sk4227 == 0) // use zaken regeneration
				{
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getInfo(4227, 1));
				}
				if ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK) && (_ai0 == 0))
				{
					int i0 = 0;
					int i1 = 1;
					if (((Attackable) npc).getMostHated() != null)
					{
						if ((((((Attackable) npc).getMostHated().getX() - _ai1) * (((Attackable) npc).getMostHated().getX() - _ai1)) + ((((Attackable) npc).getMostHated().getY() - _ai2) * (((Attackable) npc).getMostHated().getY() - _ai2))) > (1500 * 1500))
						{
							i0 = 1;
						}
						else
						{
							i0 = 0;
						}
						if (i0 == 0)
						{
							i1 = 0;
						}
						if (_quest0 > 0)
						{
							if (c_quest0 == null)
							{
								i0 = 0;
							}
							else if ((((c_quest0.getX() - _ai1) * (c_quest0.getX() - _ai1)) + ((c_quest0.getY() - _ai2) * (c_quest0.getY() - _ai2))) > (1500 * 1500))
							{
								i0 = 1;
							}
							else
							{
								i0 = 0;
							}
							if (i0 == 0)
							{
								i1 = 0;
							}
						}
						if (_quest0 > 1)
						{
							if (c_quest1 == null)
							{
								i0 = 0;
							}
							else if ((((c_quest1.getX() - _ai1) * (c_quest1.getX() - _ai1)) + ((c_quest1.getY() - _ai2) * (c_quest1.getY() - _ai2))) > (1500 * 1500))
							{
								i0 = 1;
							}
							else
							{
								i0 = 0;
							}
							if (i0 == 0)
							{
								i1 = 0;
							}
						}
						if (_quest0 > 2)
						{
							if (c_quest2 == null)
							{
								i0 = 0;
							}
							else if ((((c_quest2.getX() - _ai1) * (c_quest2.getX() - _ai1)) + ((c_quest2.getY() - _ai2) * (c_quest2.getY() - _ai2))) > (1500 * 1500))
							{
								i0 = 1;
							}
							else
							{
								i0 = 0;
							}
							if (i0 == 0)
							{
								i1 = 0;
							}
						}
						if (_quest0 > 3)
						{
							if (c_quest3 == null)
							{
								i0 = 0;
							}
							else if ((((c_quest3.getX() - _ai1) * (c_quest3.getX() - _ai1)) + ((c_quest3.getY() - _ai2) * (c_quest3.getY() - _ai2))) > (1500 * 1500))
							{
								i0 = 1;
							}
							else
							{
								i0 = 0;
							}
							if (i0 == 0)
							{
								i1 = 0;
							}
						}
						if (_quest0 > 4)
						{
							if (c_quest4 == null)
							{
								i0 = 0;
							}
							else if ((((c_quest4.getX() - _ai1) * (c_quest4.getX() - _ai1)) + ((c_quest4.getY() - _ai2) * (c_quest4.getY() - _ai2))) > (1500 * 1500))
							{
								i0 = 1;
							}
							else
							{
								i0 = 0;
							}
							if (i0 == 0)
							{
								i1 = 0;
							}
						}
						if (i1 == 1)
						{
							_quest0 = 0;
							final int i2 = Rnd.get(15);
							_ai1 = X_COORDS[i2] + Rnd.get(650);
							_ai2 = Y_COORDS[i2] + Rnd.get(650);
							_ai3 = Z_COORDS[i2];
							npc.setTarget(npc);
							npc.doCast(SkillTable.getInstance().getInfo(4222, 1));
						}
					}
				}
				if ((Rnd.get(20) < 1) && (_ai0 == 0))
				{
					_ai1 = npc.getX();
					_ai2 = npc.getY();
					_ai3 = npc.getZ();
				}
				Creature cAi0 = null;
				if ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK) && (_quest1 == 0))
				{
					if (((Attackable) npc).getMostHated() != null)
					{
						cAi0 = ((Attackable) npc).getMostHated();
						_quest1 = 1;
					}
				}
				else if ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK) && (_quest1 != 0) && (((Attackable) npc).getMostHated() != null))
				{
					if (cAi0 == ((Attackable) npc).getMostHated())
					{
						_quest1 = (_quest1 + 1);
					}
					else
					{
						_quest1 = 1;
						cAi0 = ((Attackable) npc).getMostHated();
					}
				}
				if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					_quest1 = 0;
				}
				if (_quest1 > 5)
				{
					((Attackable) npc).stopHating(cAi0);
					final Creature nextTarget = ((Attackable) npc).getMostHated();
					if (nextTarget != null)
					{
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, nextTarget);
					}
					_quest1 = 0;
				}
			}
			else if (sk4223 == 0) // use day face if not night time
			{
				npc.setTarget(npc);
				npc.doCast(SkillTable.getInstance().getInfo(4223, 1));
				_quest2 = 3;
			}
			if (sk4227 == 1) // when switching to day time, cancel zaken night regen
			{
				npc.setTarget(npc);
				npc.doCast(SkillTable.getInstance().getInfo(4242, 1));
			}
			if (Rnd.get(40) < 1)
			{
				final int i2 = Rnd.get(15);
				_ai1 = X_COORDS[i2] + Rnd.get(650);
				_ai2 = Y_COORDS[i2] + Rnd.get(650);
				_ai3 = Z_COORDS[i2];
				npc.setTarget(npc);
				npc.doCast(SkillTable.getInstance().getInfo(4222, 1));
			}
			startQuestTimer("1001", 30000, npc, null);
		}
		if (event.equals("1002"))
		{
			_quest0 = 0;
			npc.doCast(SkillTable.getInstance().getInfo(4222, 1));
			_ai0 = 0;
		}
		if (event.equals("1003"))
		{
			if (_ai4 == 1)
			{
				final int rr = Rnd.get(15);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, X_COORDS[rr] + Rnd.get(650), Y_COORDS[rr] + Rnd.get(650), Z_COORDS[rr], Rnd.get(65536), false, 0);
				_ai4 = 2;
			}
			else if (_ai4 == 2)
			{
				final int rr = Rnd.get(15);
				addSpawn(DOLL_BLADER_B, X_COORDS[rr] + Rnd.get(650), Y_COORDS[rr] + Rnd.get(650), Z_COORDS[rr], Rnd.get(65536), false, 0);
				_ai4 = 3;
			}
			else if (_ai4 == 3)
			{
				addSpawn(VALE_MASTER_B, X_COORDS[Rnd.get(15)] + Rnd.get(650), Y_COORDS[Rnd.get(15)] + Rnd.get(650), Z_COORDS[Rnd.get(15)], Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, X_COORDS[Rnd.get(15)] + Rnd.get(650), Y_COORDS[Rnd.get(15)] + Rnd.get(650), Z_COORDS[Rnd.get(15)], Rnd.get(65536), false, 0);
				_ai4 = 4;
			}
			else if (_ai4 == 4)
			{
				addSpawn(PIRATES_ZOMBIE_B, X_COORDS[Rnd.get(15)] + Rnd.get(650), Y_COORDS[Rnd.get(15)] + Rnd.get(650), Z_COORDS[Rnd.get(15)], Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, X_COORDS[Rnd.get(15)] + Rnd.get(650), Y_COORDS[Rnd.get(15)] + Rnd.get(650), Z_COORDS[Rnd.get(15)], Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, X_COORDS[Rnd.get(15)] + Rnd.get(650), Y_COORDS[Rnd.get(15)] + Rnd.get(650), Z_COORDS[Rnd.get(15)], Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, X_COORDS[Rnd.get(15)] + Rnd.get(650), Y_COORDS[Rnd.get(15)] + Rnd.get(650), Z_COORDS[Rnd.get(15)], Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, X_COORDS[Rnd.get(15)] + Rnd.get(650), Y_COORDS[Rnd.get(15)] + Rnd.get(650), Z_COORDS[Rnd.get(15)], Rnd.get(65536), false, 0);
				_ai4 = 5;
			}
			else if (_ai4 == 5)
			{
				addSpawn(DOLL_BLADER_B, 52675, 219371, -3290, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 52687, 219596, -3368, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 52672, 219740, -3418, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 52857, 219992, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 52959, 219997, -3488, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 53381, 220151, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 54236, 220948, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54885, 220144, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55264, 219860, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 55399, 220263, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55679, 220129, -3488, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 56276, 220783, -3488, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 57173, 220234, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 56267, 218826, -3488, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 56294, 219482, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 56094, 219113, -3488, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 56364, 218967, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 57113, 218079, -3488, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 56186, 217153, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55440, 218081, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 55202, 217940, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55225, 218236, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54973, 218075, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 53412, 218077, -3488, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54226, 218797, -3488, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54394, 219067, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54139, 219253, -3488, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 54262, 219480, -3488, Rnd.get(65536), false, 0);
				_ai4 = 6;
			}
			else if (_ai4 == 6)
			{
				addSpawn(PIRATES_ZOMBIE_B, 53412, 218077, -3488, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54413, 217132, -3488, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 54841, 217132, -3488, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 55372, 217128, -3343, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 55893, 217122, -3488, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 56282, 217237, -3216, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 56963, 218080, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 56267, 218826, -3216, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 56294, 219482, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 56094, 219113, -3216, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 56364, 218967, -3216, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 56276, 220783, -3216, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 57173, 220234, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54885, 220144, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55264, 219860, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 55399, 220263, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55679, 220129, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 54236, 220948, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 54464, 219095, -3216, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54226, 218797, -3216, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54394, 219067, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54139, 219253, -3216, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 54262, 219480, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 53412, 218077, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55440, 218081, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 55202, 217940, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55225, 218236, -3216, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54973, 218075, -3216, Rnd.get(65536), false, 0);
				_ai4 = 7;
			}
			else if (_ai4 == 7)
			{
				addSpawn(PIRATES_ZOMBIE_B, 54228, 217504, -3216, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54181, 217168, -3216, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 54714, 217123, -3168, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 55298, 217127, -3073, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 55787, 217130, -2993, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 56284, 217216, -2944, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 56963, 218080, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 56267, 218826, -2944, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 56294, 219482, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 56094, 219113, -2944, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 56364, 218967, -2944, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 56276, 220783, -2944, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 57173, 220234, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54885, 220144, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55264, 219860, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 55399, 220263, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55679, 220129, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 54236, 220948, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 54464, 219095, -2944, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54226, 218797, -2944, Rnd.get(65536), false, 0);
				addSpawn(VALE_MASTER_B, 54394, 219067, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54139, 219253, -2944, Rnd.get(65536), false, 0);
				addSpawn(DOLL_BLADER_B, 54262, 219480, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 53412, 218077, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 54280, 217200, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55440, 218081, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_CAPTAIN_B, 55202, 217940, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 55225, 218236, -2944, Rnd.get(65536), false, 0);
				addSpawn(PIRATES_ZOMBIE_B, 54973, 218075, -2944, Rnd.get(65536), false, 0);
				_ai4 = 8;
				cancelQuestTimer("1003", null, null);
			}
		}
		else if (event.equals("zaken_unlock"))
		{
			final GrandBossInstance zaken = (GrandBossInstance) addSpawn(ZAKEN, 55312, 219168, -3223, 0, false, 0);
			GrandBossManager.getInstance().setBossStatus(ZAKEN, ALIVE);
			spawnBoss(zaken);
		}
		else if (event.equals("CreateOnePrivateEx"))
		{
			addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFactionCall(NpcInstance npc, NpcInstance caller, PlayerInstance attacker, boolean isPet)
	{
		if ((caller == null) || (npc == null))
		{
			return super.onFactionCall(npc, caller, attacker, isPet);
		}
		
		final int npcId = npc.getNpcId();
		final int callerId = caller.getNpcId();
		if ((getTimeHour() < 5) && (callerId != ZAKEN) && (npcId == ZAKEN))
		{
			if ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) && (_ai0 == 0) && (caller.getCurrentHp() < (0.9 * caller.getMaxHp())) && (Rnd.get(450) < 1))
			{
				_ai0 = 1;
				_ai1 = caller.getX();
				_ai2 = caller.getY();
				_ai3 = caller.getZ();
				startQuestTimer("1002", 300, caller, null);
			}
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public String onSpellFinished(NpcInstance npc, PlayerInstance player, Skill skill)
	{
		if (npc.getNpcId() == ZAKEN)
		{
			final int skillId = skill.getId();
			if (skillId == 4222)
			{
				npc.teleToLocation(_ai1, _ai2, _ai3);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
			else if (skillId == 4216)
			{
				final int i1 = Rnd.get(15);
				player.teleToLocation(X_COORDS[i1] + Rnd.get(650), Y_COORDS[i1] + Rnd.get(650), Z_COORDS[i1]);
				((Attackable) npc).stopHating(player);
				final Creature nextTarget = ((Attackable) npc).getMostHated();
				if (nextTarget != null)
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, nextTarget);
				}
			}
			else if (skillId == 4217)
			{
				int i0 = 0;
				int i1 = Rnd.get(15);
				player.teleToLocation(X_COORDS[i1] + Rnd.get(650), Y_COORDS[i1] + Rnd.get(650), Z_COORDS[i1]);
				((Attackable) npc).stopHating(player);
				if ((c_quest0 != null) && (_quest0 > 0) && (c_quest0 != player) && (c_quest0.getZ() > (player.getZ() - 100)) && (c_quest0.getZ() < (player.getZ() + 100)))
				{
					if ((((c_quest0.getX() - player.getX()) * (c_quest0.getX() - player.getX())) + ((c_quest0.getY() - player.getY()) * (c_quest0.getY() - player.getY()))) > (250 * 250))
					{
						i0 = 1;
					}
					else
					{
						i0 = 0;
					}
					if (i0 == 0)
					{
						i1 = Rnd.get(15);
						c_quest0.teleToLocation(X_COORDS[i1] + Rnd.get(650), Y_COORDS[i1] + Rnd.get(650), Z_COORDS[i1]);
						((Attackable) npc).stopHating(c_quest0);
					}
				}
				if ((c_quest1 != null) && (_quest0 > 1) && (c_quest1 != player) && (c_quest1.getZ() > (player.getZ() - 100)) && (c_quest1.getZ() < (player.getZ() + 100)))
				{
					if ((((c_quest1.getX() - player.getX()) * (c_quest1.getX() - player.getX())) + ((c_quest1.getY() - player.getY()) * (c_quest1.getY() - player.getY()))) > (250 * 250))
					{
						i0 = 1;
					}
					else
					{
						i0 = 0;
					}
					if (i0 == 0)
					{
						i1 = Rnd.get(15);
						c_quest1.teleToLocation(X_COORDS[i1] + Rnd.get(650), Y_COORDS[i1] + Rnd.get(650), Z_COORDS[i1]);
						((Attackable) npc).stopHating(c_quest1);
					}
				}
				if ((c_quest2 != null) && (_quest0 > 2) && (c_quest2 != player) && (c_quest2.getZ() > (player.getZ() - 100)) && (c_quest2.getZ() < (player.getZ() + 100)))
				{
					if ((((c_quest2.getX() - player.getX()) * (c_quest2.getX() - player.getX())) + ((c_quest2.getY() - player.getY()) * (c_quest2.getY() - player.getY()))) > (250 * 250))
					{
						i0 = 1;
					}
					else
					{
						i0 = 0;
					}
					if (i0 == 0)
					{
						i1 = Rnd.get(15);
						c_quest2.teleToLocation(X_COORDS[i1] + Rnd.get(650), Y_COORDS[i1] + Rnd.get(650), Z_COORDS[i1]);
						((Attackable) npc).stopHating(c_quest2);
					}
				}
				if ((c_quest3 != null) && (_quest0 > 3) && (c_quest3 != player) && (c_quest3.getZ() > (player.getZ() - 100)) && (c_quest3.getZ() < (player.getZ() + 100)))
				{
					if ((((c_quest3.getX() - player.getX()) * (c_quest3.getX() - player.getX())) + ((c_quest3.getY() - player.getY()) * (c_quest3.getY() - player.getY()))) > (250 * 250))
					{
						i0 = 1;
					}
					else
					{
						i0 = 0;
					}
					if (i0 == 0)
					{
						i1 = Rnd.get(15);
						c_quest3.teleToLocation(X_COORDS[i1] + Rnd.get(650), Y_COORDS[i1] + Rnd.get(650), Z_COORDS[i1]);
						((Attackable) npc).stopHating(c_quest3);
					}
				}
				if ((c_quest4 != null) && (_quest0 > 4) && (c_quest4 != player) && (c_quest4.getZ() > (player.getZ() - 100)) && (c_quest4.getZ() < (player.getZ() + 100)))
				{
					if ((((c_quest4.getX() - player.getX()) * (c_quest4.getX() - player.getX())) + ((c_quest4.getY() - player.getY()) * (c_quest4.getY() - player.getY()))) > (250 * 250))
					{
						i0 = 1;
					}
					else
					{
						i0 = 0;
					}
					if (i0 == 0)
					{
						i1 = Rnd.get(15);
						c_quest4.teleToLocation(X_COORDS[i1] + Rnd.get(650), Y_COORDS[i1] + Rnd.get(650), Z_COORDS[i1]);
						((Attackable) npc).stopHating(c_quest4);
					}
				}
				final Creature nextTarget = ((Attackable) npc).getMostHated();
				if (nextTarget != null)
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, nextTarget);
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == ZAKEN)
		{
			if (attacker.getMountType() == 1)
			{
				int sk4258 = 0;
				final Effect[] effects = attacker.getAllEffects();
				if ((effects != null) && (effects.length != 0))
				{
					for (Effect e : effects)
					{
						if (e.getSkill().getId() == 4258)
						{
							sk4258 = 1;
						}
					}
				}
				if (sk4258 == 0)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4258, 1));
				}
			}
			final Creature originalAttacker = isPet ? attacker.getPet() : attacker;
			final int hate = (int) (((damage / npc.getMaxHp()) / 0.05) * 20000);
			((Attackable) npc).addDamageHate(originalAttacker, 0, hate);
			if (Rnd.get(10) < 1)
			{
				final int i0 = Rnd.get((15 * 15));
				if (i0 < 1)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4216, 1));
				}
				else if (i0 < 2)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4217, 1));
				}
				else if (i0 < 4)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4219, 1));
				}
				else if (i0 < 8)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4218, 1));
				}
				else if (i0 < 15)
				{
					for (Creature creature : npc.getKnownList().getKnownCharactersInRadius(100))
					{
						if (creature != attacker)
						{
							continue;
						}
						if (attacker != ((Attackable) npc).getMostHated())
						{
							npc.setTarget(attacker);
							npc.doCast(SkillTable.getInstance().getInfo(4221, 1));
						}
					}
				}
				if (Rnd.nextBoolean() && (attacker == ((Attackable) npc).getMostHated()))
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4220, 1));
				}
			}
			if ((getTimeHour() >= 5) && (npc.getCurrentHp() < ((npc.getMaxHp() * _quest2) / 4.0)))
			{
				_quest2 = (_quest2 - 1);
				final int i2 = Rnd.get(15);
				_ai1 = X_COORDS[i2] + Rnd.get(650);
				_ai2 = Y_COORDS[i2] + Rnd.get(650);
				_ai3 = Z_COORDS[i2];
				npc.setTarget(npc);
				npc.doCast(SkillTable.getInstance().getInfo(4222, 1));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		final Integer status = GrandBossManager.getInstance().getBossStatus(ZAKEN);
		if (npc.getNpcId() == ZAKEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
			GrandBossManager.getInstance().setBossStatus(ZAKEN, DEAD);
			// time is 36hour +/- 17hour
			final long respawnTime = (Config.ZAKEN_RESP_FIRST + Rnd.get(Config.ZAKEN_RESP_SECOND)) * 3600000;
			startQuestTimer("zaken_unlock", respawnTime, null, null);
			cancelQuestTimer("1001", npc, null);
			cancelQuestTimer("1003", npc, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatSet info = GrandBossManager.getInstance().getStatSet(ZAKEN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(ZAKEN, info);
		}
		else if (status == ALIVE)
		{
			startQuestTimer("CreateOnePrivateEx", ((30 + Rnd.get(60)) * 1000), npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onAggroRangeEnter(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		if (npc.getNpcId() == ZAKEN)
		{
			if (_Zone.isInsideZone(npc))
			{
				final Creature target = isPet ? player.getPet() : player;
				((Attackable) npc).addDamageHate(target, 1, 200);
			}
			if ((player.getZ() > (npc.getZ() - 100)) && (player.getZ() < (npc.getZ() + 100)))
			{
				if ((_quest0 < 5) && (Rnd.get(3) < 1))
				{
					if (_quest0 == 0)
					{
						c_quest0 = player;
					}
					else if (_quest0 == 1)
					{
						c_quest1 = player;
					}
					else if (_quest0 == 2)
					{
						c_quest2 = player;
					}
					else if (_quest0 == 3)
					{
						c_quest3 = player;
					}
					else if (_quest0 == 4)
					{
						c_quest4 = player;
					}
					_quest0++;
				}
				if (Rnd.get(15) < 1)
				{
					final int i0 = Rnd.get((15 * 15));
					if (i0 < 1)
					{
						npc.setTarget(player);
						npc.doCast(SkillTable.getInstance().getInfo(4216, 1));
					}
					else if (i0 < 2)
					{
						npc.setTarget(player);
						npc.doCast(SkillTable.getInstance().getInfo(4217, 1));
					}
					else if (i0 < 4)
					{
						npc.setTarget(player);
						npc.doCast(SkillTable.getInstance().getInfo(4219, 1));
					}
					else if (i0 < 8)
					{
						npc.setTarget(player);
						npc.doCast(SkillTable.getInstance().getInfo(4218, 1));
					}
					else if (i0 < 15)
					{
						for (Creature creature : npc.getKnownList().getKnownCharactersInRadius(100))
						{
							if (creature != player)
							{
								continue;
							}
							if (player != ((Attackable) npc).getMostHated())
							{
								npc.setTarget(player);
								npc.doCast(SkillTable.getInstance().getInfo(4221, 1));
							}
						}
					}
					if (Rnd.nextBoolean() && (player == ((Attackable) npc).getMostHated()))
					{
						npc.setTarget(player);
						npc.doCast(SkillTable.getInstance().getInfo(4220, 1));
					}
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public int getTimeHour()
	{
		return (GameTimeController.getInstance().getGameTime() / 60) % 24;
	}
	
	public static void main(String[] args)
	{
		new Zaken();
	}
}