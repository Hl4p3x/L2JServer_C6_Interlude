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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.geoengine.GeoEngine;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.network.serverpackets.Earthquake;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SpecialCamera;

/**
 * @author L2J_JP SANDMAN
 */
public class Antharas extends Quest
{
	protected static final Logger LOGGER = Logger.getLogger(Antharas.class.getName());
	
	// Config
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	protected static final boolean FWA_OLDANTHARAS = Config.ANTHARAS_OLD; // use antharas interlude with minions
	private static final boolean FWA_MOVEATRANDOM = true;
	private static final boolean FWA_DOSERVEREARTHQUAKE = true;
	private static final int FWA_LIMITOFWEAK = 45;
	private static final int FWA_LIMITOFNORMAL = 63;
	
	private static final int FWA_MAXMOBS = 10; // this includes Antharas itself
	private static final int FWA_INTERVALOFMOBSWEAK = 180000;
	private static final int FWA_INTERVALOFMOBSNORMAL = 150000;
	private static final int FWA_INTERVALOFMOBSSTRONG = 120000;
	private static final int FWA_PERCENTOFBEHEMOTH = 60;
	private static final int FWA_SELFDESTRUCTTIME = 15000;
	// Location of teleport cube.
	private static final int TELEPORT_CUBE = 31859;
	private static final int[] TELEPORT_CUBE_LOCATION =
	{
		177615,
		114941,
		-7709,
		0
	};
	
	protected Collection<Spawn> _teleportCubeSpawn = ConcurrentHashMap.newKeySet();
	protected Collection<NpcInstance> _teleportCube = ConcurrentHashMap.newKeySet();
	
	// Spawn data of monsters.
	protected Map<Integer, Spawn> _monsterSpawn = new ConcurrentHashMap<>();
	
	// Instance of monsters.
	protected Collection<NpcInstance> _monsters = ConcurrentHashMap.newKeySet();
	protected GrandBossInstance _antharas = null;
	
	// monstersId
	private static final int ANTHARASOLDID = 29019;
	private static final int ANTHARASWEAKID = 29066;
	private static final int ANTHARASNORMALID = 29067;
	private static final int ANTHARASSTRONGID = 29068;
	
	// Tasks.
	protected ScheduledFuture<?> _cubeSpawnTask = null;
	protected ScheduledFuture<?> _monsterSpawnTask = null;
	protected ScheduledFuture<?> _activityCheckTask = null;
	protected ScheduledFuture<?> _socialTask = null;
	protected ScheduledFuture<?> _mobiliseTask = null;
	protected ScheduledFuture<?> _mobsSpawnTask = null;
	protected ScheduledFuture<?> _selfDestructionTask = null;
	protected ScheduledFuture<?> _moveAtRandomTask = null;
	protected ScheduledFuture<?> _movieTask = null;
	
	// Antharas Status Tracking :
	private static final int DORMANT = 0; // Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final int WAITING = 1; // Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	// before he unleashes his attack. Entry is unlocked
	private static final int FIGHTING = 2; // Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final int DEAD = 3; // Antharas has been killed. Entry is locked
	
	protected static long _LastAction = 0;
	
	protected static BossZone _zone;
	
	// Boss: Antharas
	public Antharas()
	{
		super(-1, "ai/bosses");
		final int[] mob =
		{
			ANTHARASOLDID,
			ANTHARASWEAKID,
			ANTHARASNORMALID,
			ANTHARASSTRONGID,
			29069,
			29070,
			29071,
			29072,
			29073,
			29074,
			29075,
			29076
		};
		registerMobs(mob);
		init();
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		if (event.equals("setAntharasSpawnTask"))
		{
			setAntharasSpawnTask();
		}
		return null;
	}
	
	// Initialize
	private void init()
	{
		// Setting spawn data of monsters.
		try
		{
			_zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
			NpcTemplate template1;
			Spawn tempSpawn;
			
			// Old Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASOLDID);
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29019, tempSpawn);
			
			// Weak Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASWEAKID);
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29066, tempSpawn);
			
			// Normal Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASNORMALID);
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29067, tempSpawn);
			
			// Strong Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASSTRONGID);
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29068, tempSpawn);
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		
		// Setting spawn data of teleport cube.
		try
		{
			final NpcTemplate cube = NpcTable.getInstance().getTemplate(TELEPORT_CUBE);
			Spawn spawnDat;
			spawnDat = new Spawn(cube);
			spawnDat.setAmount(1);
			spawnDat.setX(TELEPORT_CUBE_LOCATION[0]);
			spawnDat.setY(TELEPORT_CUBE_LOCATION[1]);
			spawnDat.setZ(TELEPORT_CUBE_LOCATION[2]);
			spawnDat.setHeading(TELEPORT_CUBE_LOCATION[3]);
			spawnDat.setRespawnDelay(60);
			spawnDat.setLocation(0);
			SpawnTable.getInstance().addNewSpawn(spawnDat, false);
			_teleportCubeSpawn.add(spawnDat);
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		
		Integer status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
		if (FWA_OLDANTHARAS || (status == WAITING))
		{
			final StatSet info = GrandBossManager.getInstance().getStatSet(ANTHARASOLDID);
			final Long respawnTime = info.getLong("respawn_time");
			if ((status == DEAD) && (respawnTime <= System.currentTimeMillis()))
			{
				// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
				// also, the status needs to be changed to DORMANT
				GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
				status = DORMANT;
			}
			else if (status == FIGHTING)
			{
				final int loc_x = info.getInt("loc_x");
				final int loc_y = info.getInt("loc_y");
				final int loc_z = info.getInt("loc_z");
				final int heading = info.getInt("heading");
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				_antharas = (GrandBossInstance) addSpawn(ANTHARASOLDID, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPool.scheduleAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (status == DEAD)
			{
				ThreadPool.schedule(new UnlockAntharas(ANTHARASOLDID), respawnTime - System.currentTimeMillis());
			}
			else if (status == DORMANT)
			{
				// Here status is 0 on Database, dont do nothing
			}
			else
			{
				setAntharasSpawnTask();
			}
		}
		else
		{
			final Integer statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			final Integer statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			final Integer statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);
			int antharasId = 0;
			if ((statusWeak == FIGHTING) || (statusWeak == DEAD))
			{
				antharasId = ANTHARASWEAKID;
				status = statusWeak;
			}
			else if ((statusNormal == FIGHTING) || (statusNormal == DEAD))
			{
				antharasId = ANTHARASNORMALID;
				status = statusNormal;
			}
			else if ((statusStrong == FIGHTING) || (statusStrong == DEAD))
			{
				antharasId = ANTHARASSTRONGID;
				status = statusStrong;
			}
			if ((antharasId != 0) && (status == FIGHTING))
			{
				final StatSet info = GrandBossManager.getInstance().getStatSet(antharasId);
				final int loc_x = info.getInt("loc_x");
				final int loc_y = info.getInt("loc_y");
				final int loc_z = info.getInt("loc_z");
				final int heading = info.getInt("heading");
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				_antharas = (GrandBossInstance) addSpawn(antharasId, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPool.scheduleAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if ((antharasId != 0) && (status == DEAD))
			{
				final StatSet info = GrandBossManager.getInstance().getStatSet(antharasId);
				final Long respawnTime = info.getLong("respawn_time");
				if (respawnTime <= System.currentTimeMillis())
				{
					// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
					// also, the status needs to be changed to DORMANT
					GrandBossManager.getInstance().setBossStatus(antharasId, DORMANT);
					status = DORMANT;
				}
				else
				{
					ThreadPool.schedule(new UnlockAntharas(antharasId), respawnTime - System.currentTimeMillis());
				}
			}
		}
	}
	
	// Do spawn teleport cube.
	public void spawnCube()
	{
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		
		for (Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
	}
	
	// Setting Antharas spawn task.
	public void setAntharasSpawnTask()
	{
		if (_monsterSpawnTask == null)
		{
			synchronized (this)
			{
				if (_monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, WAITING);
					_monsterSpawnTask = ThreadPool.schedule(new AntharasSpawn(1), 60000 * Config.ANTHARAS_WAIT_TIME);
				}
			}
		}
	}
	
	protected void startMinionSpawns(int antharasId)
	{
		int intervalOfMobs;
		
		// Interval of minions is decided by the type of Antharas
		// that invaded the lair.
		switch (antharasId)
		{
			case ANTHARASWEAKID:
			{
				intervalOfMobs = FWA_INTERVALOFMOBSWEAK;
				break;
			}
			case ANTHARASNORMALID:
			{
				intervalOfMobs = FWA_INTERVALOFMOBSNORMAL;
				break;
			}
			default:
			{
				intervalOfMobs = FWA_INTERVALOFMOBSSTRONG;
				break;
			}
		}
		
		// Spawn mobs.
		_mobsSpawnTask = ThreadPool.scheduleAtFixedRate(new MobsSpawn(), intervalOfMobs, intervalOfMobs);
	}
	
	// Do spawn Antharas.
	private class AntharasSpawn implements Runnable
	{
		private int _taskId = 0;
		private Collection<Creature> _players;
		
		AntharasSpawn(int taskId)
		{
			_taskId = taskId;
			if (_zone.getCharactersInside() != null)
			{
				_players = _zone.getCharactersInside();
			}
		}
		
		@Override
		public void run()
		{
			int npcId;
			Spawn antharasSpawn = null;
			
			switch (_taskId)
			{
				case 1: // Spawn.
				{
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					_monsterSpawnTask.cancel(false);
					_monsterSpawnTask = null;
					if (FWA_OLDANTHARAS)
					{
						npcId = 29019; // old
					}
					else if ((_players == null) || (_players.size() <= FWA_LIMITOFWEAK))
					{
						npcId = 29066; // weak
					}
					else if (_players.size() > FWA_LIMITOFNORMAL)
					{
						npcId = 29068; // strong
					}
					else
					{
						npcId = 29067; // normal
					}
					// Do spawn.
					antharasSpawn = _monsterSpawn.get(npcId);
					_antharas = (GrandBossInstance) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(_antharas);
					_monsters.add(_antharas);
					_antharas.setImmobilized(true);
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
					GrandBossManager.getInstance().setBossStatus(npcId, FIGHTING);
					_LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					_activityCheckTask = ThreadPool.scheduleAtFixedRate(new CheckActivity(), 60000, 60000);
					// Setting 1st time of minions spawn task.
					if (!FWA_OLDANTHARAS)
					{
						startMinionSpawns(npcId);
					}
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(2), 16);
					break;
				}
				case 2:
				{
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, -19, 0, 20000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(3), 3000);
					break;
				}
				case 3:
				{
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, 0, 6000, 20000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(4), 10000);
					break;
				}
				case 4:
				{
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 3700, 0, -3, 0, 10000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(5), 200);
					break;
				}
				case 5:
				{
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 22000, 30000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(6), 10800);
					break;
				}
				case 6:
				{
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 300, 7000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(7), 1900);
					break;
				}
				case 7:
				{
					_antharas.abortCast();
					_mobiliseTask = ThreadPool.schedule(new SetMobilised(_antharas), 16);
					// Move at random.
					if (FWA_MOVEATRANDOM)
					{
						final Location pos = new Location(Rnd.get(175000, 178500), Rnd.get(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPool.schedule(new MoveAtRandom(_antharas, pos), 500);
					}
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					break;
				}
			}
		}
	}
	
	protected void broadcastPacket(GameServerPacket mov)
	{
		if (_zone != null)
		{
			for (Creature creatures : _zone.getCharactersInside())
			{
				if (creatures instanceof PlayerInstance)
				{
					creatures.sendPacket(mov);
				}
			}
		}
	}
	
	// Do spawn Behemoth or Bomber.
	private class MobsSpawn implements Runnable
	{
		@Override
		public void run()
		{
			NpcTemplate template1;
			Spawn tempSpawn;
			final boolean isBehemoth = Rnd.get(100) < FWA_PERCENTOFBEHEMOTH;
			try
			{
				final int mobNumber = (isBehemoth ? 2 : 3);
				// Set spawn.
				for (int i = 0; i < mobNumber; i++)
				{
					if (_monsters.size() >= FWA_MAXMOBS)
					{
						break;
					}
					int npcId;
					if (isBehemoth)
					{
						npcId = 29069;
					}
					else
					{
						npcId = Rnd.get(29070, 29076);
					}
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new Spawn(template1);
					// allocates it at random in the lair of Antharas.
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = ((_antharas.getX() - x) * (_antharas.getX() - x)) + ((_antharas.getY() - y) * (_antharas.getY() - y));
					while ((tried++ < 25) && notFound)
					{
						final int rx = Rnd.get(175000, 179900);
						final int ry = Rnd.get(112400, 116000);
						final int rdt = ((_antharas.getX() - rx) * (_antharas.getX() - rx)) + ((_antharas.getY() - ry) * (_antharas.getY() - ry));
						final Location randomLocation = new Location(rx, ry, -7704);
						if (GeoEngine.getInstance().canSeeTarget(_antharas, randomLocation) && (rdt < dt))
						{
							x = rx;
							y = ry;
							dt = rdt;
							if (rdt <= 900000)
							{
								notFound = false;
							}
						}
					}
					tempSpawn.setX(x);
					tempSpawn.setY(y);
					tempSpawn.setZ(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
					SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
					// Do spawn.
					_monsters.add(tempSpawn.doSpawn());
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	@Override
	public String onAggroRangeEnter(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case 29070:
			case 29071:
			case 29072:
			case 29073:
			case 29074:
			case 29075:
			case 29076:
			{
				if ((_selfDestructionTask == null) && !npc.isDead())
				{
					_selfDestructionTask = ThreadPool.schedule(new SelfDestructionOfBomber(npc), FWA_SELFDESTRUCTTIME);
				}
				break;
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	// Do self destruction.
	private class SelfDestructionOfBomber implements Runnable
	{
		private final NpcInstance _bomber;
		
		public SelfDestructionOfBomber(NpcInstance bomber)
		{
			_bomber = bomber;
		}
		
		@Override
		public void run()
		{
			Skill skill = null;
			switch (_bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				{
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				}
				case 29076:
				{
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
				}
			}
			
			_bomber.doCast(skill);
			
			if (_selfDestructionTask != null)
			{
				_selfDestructionTask.cancel(false);
				_selfDestructionTask = null;
			}
		}
	}
	
	@Override
	public String onSpellFinished(NpcInstance npc, PlayerInstance player, Skill skill)
	{
		if (npc.isInvul())
		{
			return null;
		}
		else if ((skill != null) && ((skill.getId() == 5097) || (skill.getId() == 5094)))
		{
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				case 29076:
				{
					npc.doDie(npc);
					break;
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	// At end of activity time.
	protected class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			final Long temp = (System.currentTimeMillis() - _LastAction);
			if (temp > (Config.ANTHARAS_DESPAWN_TIME * 60000))
			{
				GrandBossManager.getInstance().setBossStatus(_antharas.getNpcId(), DORMANT);
				setUnspawn();
			}
		}
	}
	
	// Clean Antharas's lair.
	public void setUnspawn()
	{
		// Eliminate players.
		_zone.oustAllPlayers();
		
		// Not executed tasks is canceled.
		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		if (_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if (_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}
		
		// Delete monsters.
		for (NpcInstance mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();
		
		// Delete teleport cube.
		for (NpcInstance cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();
	}
	
	// Do spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		private final int _type;
		
		CubeSpawn(int type)
		{
			_type = type;
		}
		
		@Override
		public void run()
		{
			if (_type == 0)
			{
				spawnCube();
				_cubeSpawnTask = ThreadPool.schedule(new CubeSpawn(1), 1800000);
			}
			else
			{
				setUnspawn();
			}
		}
	}
	
	// UnLock Antharas.
	private static class UnlockAntharas implements Runnable
	{
		private final int _bossId;
		
		public UnlockAntharas(int bossId)
		{
			_bossId = bossId;
		}
		
		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(_bossId, DORMANT);
			if (FWA_DOSERVEREARTHQUAKE)
			{
				for (PlayerInstance p : World.getInstance().getAllPlayers())
				{
					p.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
				}
			}
		}
	}
	
	// Action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private final GrandBossInstance _boss;
		
		public SetMobilised(GrandBossInstance boss)
		{
			_boss = boss;
		}
		
		@Override
		public void run()
		{
			_boss.setImmobilized(false);
			
			// When it is possible to act, a social action is canceled.
			if (_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}
	
	// Move at random on after Antharas appears.
	private static class MoveAtRandom implements Runnable
	{
		private final NpcInstance _npc;
		private final Location _pos;
		
		public MoveAtRandom(NpcInstance npc, Location pos)
		{
			_npc = npc;
			_pos = pos;
		}
		
		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		if ((npc.getNpcId() == 29019) || (npc.getNpcId() == 29066) || (npc.getNpcId() == 29067) || (npc.getNpcId() == 29068))
		{
			_LastAction = System.currentTimeMillis();
			if (!FWA_OLDANTHARAS && (_mobsSpawnTask == null))
			{
				startMinionSpawns(npc.getNpcId());
			}
		}
		else if ((npc.getNpcId() > 29069) && (npc.getNpcId() < 29077) && (npc.getCurrentHp() <= damage))
		{
			Skill skill = null;
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				{
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				}
				case 29076:
				{
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
				}
			}
			
			npc.doCast(skill);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		if ((npc.getNpcId() == 29019) || (npc.getNpcId() == 29066) || (npc.getNpcId() == 29067) || (npc.getNpcId() == 29068))
		{
			npc.broadcastPacket(new PlaySound(1, "BS01_D", npc));
			_cubeSpawnTask = ThreadPool.schedule(new CubeSpawn(0), 10000);
			GrandBossManager.getInstance().setBossStatus(npc.getNpcId(), DEAD);
			final long respawnTime = (Config.ANTHARAS_RESP_FIRST + Rnd.get(Config.ANTHARAS_RESP_SECOND)) * 3600000;
			ThreadPool.schedule(new UnlockAntharas(npc.getNpcId()), respawnTime);
			// also save the respawn time so that the info is maintained past reboots
			final StatSet info = GrandBossManager.getInstance().getStatSet(npc.getNpcId());
			info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
			GrandBossManager.getInstance().setStatSet(npc.getNpcId(), info);
		}
		else if (npc.getNpcId() == 29069)
		{
			final int countHPHerb = Rnd.get(6, 18);
			final int countMPHerb = Rnd.get(6, 18);
			for (int i = 0; i < countHPHerb; i++)
			{
				((MonsterInstance) npc).DropItem(killer, 8602, 1);
			}
			for (int i = 0; i < countMPHerb; i++)
			{
				((MonsterInstance) npc).DropItem(killer, 8605, 1);
			}
		}
		if (_monsters.contains(npc))
		{
			_monsters.remove(npc);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Antharas();
	}
}