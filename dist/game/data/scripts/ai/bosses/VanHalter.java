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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.SpecialCamera;

/**
 * @author L2J_JP SANDMAN
 **/
public class VanHalter extends Quest
{
	private static final Logger LOGGER = Logger.getLogger(VanHalter.class.getName());
	
	// List of intruders.
	protected Map<Integer, List<PlayerInstance>> _bleedingPlayers = new HashMap<>();
	
	// Spawn data of monsters.
	protected Map<Integer, Spawn> _monsterSpawn = new ConcurrentHashMap<>();
	protected Collection<Spawn> _royalGuardSpawn = ConcurrentHashMap.newKeySet();
	protected Collection<Spawn> _royalGuardCaptainSpawn = ConcurrentHashMap.newKeySet();
	protected Collection<Spawn> _royalGuardHelperSpawn = ConcurrentHashMap.newKeySet();
	protected Collection<Spawn> _triolRevelationSpawn = ConcurrentHashMap.newKeySet();
	protected Collection<Spawn> _triolRevelationAlive = ConcurrentHashMap.newKeySet();
	protected Collection<Spawn> _guardOfAltarSpawn = ConcurrentHashMap.newKeySet();
	protected Map<Integer, Spawn> _cameraMarkerSpawn = new ConcurrentHashMap<>();
	protected Spawn _ritualOfferingSpawn = null;
	protected Spawn _ritualSacrificeSpawn = null;
	protected Spawn _vanHalterSpawn = null;
	
	// Instance of monsters.
	protected Collection<NpcInstance> _monsters = ConcurrentHashMap.newKeySet();
	protected Collection<NpcInstance> _royalGuard = ConcurrentHashMap.newKeySet();
	protected Collection<NpcInstance> _royalGuardCaptain = ConcurrentHashMap.newKeySet();
	protected Collection<NpcInstance> _royalGuardHepler = ConcurrentHashMap.newKeySet();
	protected Collection<NpcInstance> _triolRevelation = ConcurrentHashMap.newKeySet();
	protected Collection<NpcInstance> _guardOfAltar = ConcurrentHashMap.newKeySet();
	protected Map<Integer, NpcInstance> _cameraMarker = new ConcurrentHashMap<>();
	protected Collection<DoorInstance> _doorOfAltar = ConcurrentHashMap.newKeySet();
	protected Collection<DoorInstance> _doorOfSacrifice = ConcurrentHashMap.newKeySet();
	protected NpcInstance _ritualOffering = null;
	protected NpcInstance _ritualSacrifice = null;
	protected RaidBossInstance _vanHalter = null;
	
	// Task
	protected ScheduledFuture<?> _movieTask = null;
	protected ScheduledFuture<?> _closeDoorOfAltarTask = null;
	protected ScheduledFuture<?> _openDoorOfAltarTask = null;
	protected ScheduledFuture<?> _lockUpDoorOfAltarTask = null;
	protected ScheduledFuture<?> _callRoyalGuardHelperTask = null;
	protected ScheduledFuture<?> _timeUpTask = null;
	protected ScheduledFuture<?> _intervalTask = null;
	protected ScheduledFuture<?> _halterEscapeTask = null;
	protected ScheduledFuture<?> _setBleedTask = null;
	
	// State of High Priestess van Halter
	boolean _isLocked = false;
	boolean _isHalterSpawned = false;
	boolean _isSacrificeSpawned = false;
	boolean _isCaptainSpawned = false;
	boolean _isHelperCalled = false;
	
	// VanHalter Status Tracking :
	private static final byte INTERVAL = 0;
	private static final byte NOTSPAWN = 1;
	private static final byte ALIVE = 2;
	
	// Initialize
	public VanHalter()
	{
		super(-1, "ai/bosses");
		
		final int[] mobs =
		{
			29062,
			22188,
			32058,
			32059,
			32060,
			32061,
			32062,
			32063,
			32064,
			32065,
			32066
		};
		
		addEventId(29062, EventType.ON_ATTACK);
		for (int mob : mobs)
		{
			addEventId(mob, EventType.ON_KILL);
		}
		
		// GrandBossManager.getInstance().addBoss(29062);
		// Clear flag.
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;
		
		// Setting door state.
		_doorOfAltar.add(DoorData.getInstance().getDoor(19160014));
		_doorOfAltar.add(DoorData.getInstance().getDoor(19160015));
		openDoorOfAltar(true);
		_doorOfSacrifice.add(DoorData.getInstance().getDoor(19160016));
		_doorOfSacrifice.add(DoorData.getInstance().getDoor(19160017));
		closeDoorOfSacrifice();
		
		// Load spawn data of monsters.
		loadRoyalGuard();
		loadTriolRevelation();
		loadRoyalGuardCaptain();
		loadRoyalGuardHelper();
		loadGuardOfAltar();
		loadVanHalter();
		loadRitualOffering();
		loadRitualSacrifice();
		
		// Spawn monsters.
		spawnRoyalGuard();
		spawnTriolRevelation();
		spawnVanHalter();
		spawnRitualOffering();
		
		// Setting spawn data of Dummy camera marker.
		_cameraMarkerSpawn.clear();
		try
		{
			final NpcTemplate template1 = NpcTable.getInstance().getTemplate(13014); // Dummy npc
			Spawn tempSpawn;
			
			// Dummy camera marker.
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-10449);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(1, tempSpawn);
			
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-10051);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(2, tempSpawn);
			
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-9741);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(3, tempSpawn);
			
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-9394);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(4, tempSpawn);
			
			tempSpawn = new Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55197);
			tempSpawn.setZ(-8739);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(5, tempSpawn);
		}
		catch (Exception e)
		{
			LOGGER.warning("VanHalterManager : " + e.getMessage() + " :" + e);
		}
		
		// Set time up.
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPool.schedule(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER);
		
		// Set bleeding to palyers.
		if (_setBleedTask != null)
		{
			_setBleedTask.cancel(false);
		}
		_setBleedTask = ThreadPool.schedule(new Bleeding(), 2000);
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(29062);
		if (status == INTERVAL)
		{
			enterInterval();
		}
		else
		{
			GrandBossManager.getInstance().setBossStatus(29062, NOTSPAWN);
		}
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		if ((npc.getNpcId() == 29062) && (((int) (npc.getStatus().getCurrentHp() / npc.getMaxHp()) * 100) <= 20))
		{
			callRoyalGuardHelper();
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if ((npcId == 32058) || (npcId == 32059) || (npcId == 32060) || (npcId == 32061) || (npcId == 32062) || (npcId == 32063) || (npcId == 32064) || (npcId == 32065) || (npcId == 32066))
		{
			removeBleeding(npcId);
		}
		checkTriolRevelationDestroy();
		if (npcId == 22188)
		{
			checkRoyalGuardCaptainDestroy();
		}
		if (npcId == 29062)
		{
			enterInterval();
		}
		return super.onKill(npc, killer, isPet);
	}
	
	// Load Royal Guard.
	protected void loadRoyalGuard()
	{
		_royalGuardSpawn.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 22175);
			statement.setInt(2, 22176);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRoyalGuard()
	{
		if (!_royalGuard.isEmpty())
		{
			deleteRoyalGuard();
		}
		
		for (Spawn rgs : _royalGuardSpawn)
		{
			rgs.startRespawn();
			_royalGuard.add(rgs.doSpawn());
		}
	}
	
	protected void deleteRoyalGuard()
	{
		for (NpcInstance rg : _royalGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}
		
		_royalGuard.clear();
	}
	
	// Load Triol's Revelation.
	protected void loadTriolRevelation()
	{
		_triolRevelationSpawn.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 32058);
			statement.setInt(2, 32068);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_triolRevelationSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadTriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadTriolRevelation: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnTriolRevelation()
	{
		if (!_triolRevelation.isEmpty())
		{
			deleteTriolRevelation();
		}
		
		for (Spawn trs : _triolRevelationSpawn)
		{
			trs.startRespawn();
			_triolRevelation.add(trs.doSpawn());
			if ((trs.getNpcId() != 32067) && (trs.getNpcId() != 32068))
			{
				_triolRevelationAlive.add(trs);
			}
		}
	}
	
	protected void deleteTriolRevelation()
	{
		for (NpcInstance tr : _triolRevelation)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_triolRevelation.clear();
		_bleedingPlayers.clear();
	}
	
	// Load Royal Guard Captain.
	protected void loadRoyalGuardCaptain()
	{
		_royalGuardCaptainSpawn.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22188);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardCaptainSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRoyalGuardCaptain()
	{
		if (!_royalGuardCaptain.isEmpty())
		{
			deleteRoyalGuardCaptain();
		}
		
		for (Spawn trs : _royalGuardCaptainSpawn)
		{
			trs.startRespawn();
			_royalGuardCaptain.add(trs.doSpawn());
		}
		_isCaptainSpawned = true;
	}
	
	protected void deleteRoyalGuardCaptain()
	{
		for (NpcInstance tr : _royalGuardCaptain)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_royalGuardCaptain.clear();
	}
	
	// Load Royal Guard Helper.
	protected void loadRoyalGuardHelper()
	{
		_royalGuardHelperSpawn.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22191);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardHelperSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRoyalGuardHepler()
	{
		for (Spawn trs : _royalGuardHelperSpawn)
		{
			trs.startRespawn();
			_royalGuardHepler.add(trs.doSpawn());
		}
	}
	
	protected void deleteRoyalGuardHepler()
	{
		for (NpcInstance tr : _royalGuardHepler)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_royalGuardHepler.clear();
	}
	
	// Load Guard Of Altar
	protected void loadGuardOfAltar()
	{
		_guardOfAltarSpawn.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32051);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_guardOfAltarSpawn.add(spawnDat);
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnGuardOfAltar()
	{
		if (!_guardOfAltar.isEmpty())
		{
			deleteGuardOfAltar();
		}
		
		for (Spawn trs : _guardOfAltarSpawn)
		{
			trs.startRespawn();
			_guardOfAltar.add(trs.doSpawn());
		}
	}
	
	protected void deleteGuardOfAltar()
	{
		for (NpcInstance tr : _guardOfAltar)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		
		_guardOfAltar.clear();
	}
	
	// Load High Priestess van Halter.
	protected void loadVanHalter()
	{
		_vanHalterSpawn = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 29062);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_vanHalterSpawn = spawnDat;
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnVanHalter()
	{
		_vanHalter = (RaidBossInstance) _vanHalterSpawn.doSpawn();
		// _vanHalter.setImmobilized(true);
		_vanHalter.setInvul(true);
		_isHalterSpawned = true;
	}
	
	protected void deleteVanHalter()
	{
		// _vanHalter.setImmobilized(false);
		_vanHalter.setInvul(false);
		_vanHalter.getSpawn().stopRespawn();
		_vanHalter.deleteMe();
	}
	
	// Load Ritual Offering.
	protected void loadRitualOffering()
	{
		_ritualOfferingSpawn = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32038);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_ritualOfferingSpawn = spawnDat;
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRitualOffering()
	{
		_ritualOffering = _ritualOfferingSpawn.doSpawn();
		// _ritualOffering.setImmobilized(true);
		_ritualOffering.setInvul(true);
		_ritualOffering.setParalyzed(true);
	}
	
	protected void deleteRitualOffering()
	{
		// _ritualOffering.setImmobilized(false);
		_ritualOffering.setInvul(false);
		_ritualOffering.setParalyzed(false);
		_ritualOffering.getSpawn().stopRespawn();
		_ritualOffering.deleteMe();
	}
	
	// Load Ritual Sacrifice.
	protected void loadRitualSacrifice()
	{
		_ritualSacrificeSpawn = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22195);
			final ResultSet rset = statement.executeQuery();
			
			Spawn spawnDat;
			NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_ritualSacrificeSpawn = spawnDat;
				}
				else
				{
					LOGGER.warning("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// Problem with initializing spawn, go to next one
			LOGGER.warning("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRitualSacrifice()
	{
		_ritualSacrifice = _ritualSacrificeSpawn.doSpawn();
		// _ritualSacrifice.setImmobilized(true);
		_ritualSacrifice.setInvul(true);
		_isSacrificeSpawned = true;
	}
	
	protected void deleteRitualSacrifice()
	{
		if (!_isSacrificeSpawned)
		{
			return;
		}
		
		_ritualSacrifice.getSpawn().stopRespawn();
		_ritualSacrifice.deleteMe();
		_isSacrificeSpawned = false;
	}
	
	protected void spawnCameraMarker()
	{
		_cameraMarker.clear();
		for (int i = 1; i <= _cameraMarkerSpawn.size(); i++)
		{
			_cameraMarker.put(i, _cameraMarkerSpawn.get(i).doSpawn());
			_cameraMarker.get(i).getSpawn().stopRespawn();
			_cameraMarker.get(i).setImmobilized(true);
		}
	}
	
	protected void deleteCameraMarker()
	{
		if (_cameraMarker.isEmpty())
		{
			return;
		}
		
		for (int i = 1; i <= _cameraMarker.size(); i++)
		{
			_cameraMarker.get(i).deleteMe();
		}
		_cameraMarker.clear();
	}
	
	protected class LockUpDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			closeDoorOfAltar(false);
			_isLocked = true;
			_lockUpDoorOfAltarTask = null;
		}
	}
	
	protected void openDoorOfAltar(boolean loop)
	{
		for (DoorInstance door : _doorOfAltar)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage() + " :" + e);
			}
		}
		
		if (loop)
		{
			_isLocked = false;
			if (_closeDoorOfAltarTask != null)
			{
				_closeDoorOfAltarTask.cancel(false);
			}
			_closeDoorOfAltarTask = null;
			_closeDoorOfAltarTask = ThreadPool.schedule(new CloseDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER);
		}
		else
		{
			if (_closeDoorOfAltarTask != null)
			{
				_closeDoorOfAltarTask.cancel(false);
			}
			_closeDoorOfAltarTask = null;
		}
	}
	
	protected class OpenDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			openDoorOfAltar(true);
		}
	}
	
	protected void closeDoorOfAltar(boolean loop)
	{
		for (DoorInstance door : _doorOfAltar)
		{
			door.closeMe();
		}
		
		if (loop)
		{
			if (_openDoorOfAltarTask != null)
			{
				_openDoorOfAltarTask.cancel(false);
			}
			_openDoorOfAltarTask = null;
			_openDoorOfAltarTask = ThreadPool.schedule(new OpenDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER);
		}
		else
		{
			if (_openDoorOfAltarTask != null)
			{
				_openDoorOfAltarTask.cancel(false);
			}
			_openDoorOfAltarTask = null;
		}
	}
	
	protected class CloseDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			closeDoorOfAltar(true);
		}
	}
	
	protected void openDoorOfSacrifice()
	{
		for (DoorInstance door : _doorOfSacrifice)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage() + " :" + e);
			}
		}
	}
	
	protected void closeDoorOfSacrifice()
	{
		for (DoorInstance door : _doorOfSacrifice)
		{
			try
			{
				door.closeMe();
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage() + " :" + e);
			}
		}
	}
	
	// event
	public void checkTriolRevelationDestroy()
	{
		if (_isCaptainSpawned)
		{
			return;
		}
		
		boolean isTriolRevelationDestroyed = true;
		for (Spawn tra : _triolRevelationAlive)
		{
			if (!tra.getLastSpawn().isDead())
			{
				isTriolRevelationDestroyed = false;
			}
		}
		
		if (isTriolRevelationDestroyed)
		{
			spawnRoyalGuardCaptain();
		}
	}
	
	public void checkRoyalGuardCaptainDestroy()
	{
		if (!_isHalterSpawned)
		{
			return;
		}
		
		deleteRoyalGuard();
		deleteRoyalGuardCaptain();
		spawnGuardOfAltar();
		openDoorOfSacrifice();
		
		// _vanHalter.setImmobilized(true);
		_vanHalter.setInvul(true);
		spawnCameraMarker();
		
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;
		_movieTask = ThreadPool.schedule(new Movie(1), Config.HPH_APPTIMEOFHALTER);
	}
	
	// Start fight against High Priestess van Halter.
	protected void combatBeginning()
	{
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPool.schedule(new TimeUp(), Config.HPH_FIGHTTIMEOFHALTER);
		
		final Map<Integer, PlayerInstance> targets = new HashMap<>();
		int i = 0;
		for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
		{
			i++;
			targets.put(i, pc);
		}
		
		_vanHalter.reduceCurrentHp(1, targets.get(Rnd.get(1, i)));
	}
	
	// Call Royal Guard Helper and escape from player.
	public void callRoyalGuardHelper()
	{
		if (!_isHelperCalled)
		{
			_isHelperCalled = true;
			_halterEscapeTask = ThreadPool.schedule(new HalterEscape(), 500);
			_callRoyalGuardHelperTask = ThreadPool.schedule(new CallRoyalGuardHelper(), 1000);
		}
	}
	
	protected class CallRoyalGuardHelper implements Runnable
	{
		@Override
		public void run()
		{
			spawnRoyalGuardHepler();
			
			if ((_royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT) && !_vanHalter.isDead())
			{
				if (_callRoyalGuardHelperTask != null)
				{
					_callRoyalGuardHelperTask.cancel(false);
				}
				_callRoyalGuardHelperTask = ThreadPool.schedule(new CallRoyalGuardHelper(), Config.HPH_CALLROYALGUARDHELPERINTERVAL);
			}
			else
			{
				if (_callRoyalGuardHelperTask != null)
				{
					_callRoyalGuardHelperTask.cancel(false);
				}
				_callRoyalGuardHelperTask = null;
			}
		}
	}
	
	protected class HalterEscape implements Runnable
	{
		@Override
		public void run()
		{
			if ((_royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT) && !_vanHalter.isDead())
			{
				if (_vanHalter.isAfraid())
				{
					_vanHalter.stopEffects(Effect.EffectType.FEAR);
					_vanHalter.setAfraid(false);
					_vanHalter.updateAbnormalEffect();
				}
				else
				{
					_vanHalter.startFear();
					if (_vanHalter.getZ() >= -10476)
					{
						final Location pos = new Location(-16397, -53308, -10448, 0);
						if ((_vanHalter.getX() == pos.getX()) && (_vanHalter.getY() == pos.getY()))
						{
							_vanHalter.stopEffects(Effect.EffectType.FEAR);
							_vanHalter.setAfraid(false);
							_vanHalter.updateAbnormalEffect();
						}
						else
						{
							_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
						}
					}
					else if (_vanHalter.getX() >= -16397)
					{
						final Location pos = new Location(-15548, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
					else
					{
						final Location pos = new Location(-17248, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
				}
				if (_halterEscapeTask != null)
				{
					_halterEscapeTask.cancel(false);
				}
				_halterEscapeTask = ThreadPool.schedule(new HalterEscape(), 5000);
			}
			else
			{
				_vanHalter.stopEffects(Effect.EffectType.FEAR);
				_vanHalter.setAfraid(false);
				_vanHalter.updateAbnormalEffect();
				if (_halterEscapeTask != null)
				{
					_halterEscapeTask.cancel(false);
				}
				_halterEscapeTask = null;
			}
		}
	}
	
	// Check bleeding player.
	protected void addBleeding()
	{
		final Skill bleed = SkillTable.getInstance().getInfo(4615, 12);
		for (NpcInstance tr : _triolRevelation)
		{
			if (!tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).iterator().hasNext() || tr.isDead())
			{
				continue;
			}
			
			final List<PlayerInstance> bpc = new ArrayList<>();
			for (PlayerInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc, false, false, false);
					tr.broadcastPacket(new MagicSkillUse(tr, pc, bleed.getId(), 12, 1, 1));
				}
				
				bpc.add(pc);
			}
			_bleedingPlayers.remove(tr.getNpcId());
			_bleedingPlayers.put(tr.getNpcId(), bpc);
		}
	}
	
	public void removeBleeding(int npcId)
	{
		if (_bleedingPlayers.get(npcId) == null)
		{
			return;
		}
		for (PlayerInstance pc : _bleedingPlayers.get(npcId))
		{
			if (pc.getFirstEffect(Effect.EffectType.DMG_OVER_TIME) != null)
			{
				pc.stopEffects(Effect.EffectType.DMG_OVER_TIME);
			}
		}
		_bleedingPlayers.remove(npcId);
	}
	
	protected class Bleeding implements Runnable
	{
		@Override
		public void run()
		{
			addBleeding();
			
			if (_setBleedTask != null)
			{
				_setBleedTask.cancel(false);
			}
			_setBleedTask = ThreadPool.schedule(new Bleeding(), 2000);
		}
	}
	
	// High Priestess van Halter dead or time up.
	public void enterInterval()
	{
		// Cancel all task
		if (_callRoyalGuardHelperTask != null)
		{
			_callRoyalGuardHelperTask.cancel(false);
		}
		_callRoyalGuardHelperTask = null;
		
		if (_closeDoorOfAltarTask != null)
		{
			_closeDoorOfAltarTask.cancel(false);
		}
		_closeDoorOfAltarTask = null;
		
		if (_halterEscapeTask != null)
		{
			_halterEscapeTask.cancel(false);
		}
		_halterEscapeTask = null;
		
		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}
		_intervalTask = null;
		
		if (_lockUpDoorOfAltarTask != null)
		{
			_lockUpDoorOfAltarTask.cancel(false);
		}
		_lockUpDoorOfAltarTask = null;
		
		if (_movieTask != null)
		{
			_movieTask.cancel(false);
		}
		_movieTask = null;
		
		if (_openDoorOfAltarTask != null)
		{
			_openDoorOfAltarTask.cancel(false);
		}
		_openDoorOfAltarTask = null;
		
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;
		
		// Delete monsters
		if (_vanHalter.isDead())
		{
			_vanHalter.getSpawn().stopRespawn();
		}
		else
		{
			deleteVanHalter();
		}
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualOffering();
		deleteRitualSacrifice();
		deleteGuardOfAltar();
		
		// Set interval end.
		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(29062);
		if (status != INTERVAL)
		{
			final long interval = Rnd.get(Config.HPH_FIXINTERVALOFHALTER, Config.HPH_FIXINTERVALOFHALTER + Config.HPH_RANDOMINTERVALOFHALTER)/* * 3600000 */;
			final StatSet info = GrandBossManager.getInstance().getStatSet(29062);
			info.set("respawn_time", (System.currentTimeMillis() + interval));
			GrandBossManager.getInstance().setStatSet(29062, info);
			GrandBossManager.getInstance().setBossStatus(29062, INTERVAL);
		}
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(29062);
		final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
		_intervalTask = ThreadPool.schedule(new Interval(), temp);
	}
	
	// Interval.
	protected class Interval implements Runnable
	{
		@Override
		public void run()
		{
			setupAltar();
		}
	}
	
	// Interval end.
	public void setupAltar()
	{
		// Cancel all task
		if (_callRoyalGuardHelperTask != null)
		{
			_callRoyalGuardHelperTask.cancel(false);
		}
		_callRoyalGuardHelperTask = null;
		
		if (_closeDoorOfAltarTask != null)
		{
			_closeDoorOfAltarTask.cancel(false);
		}
		_closeDoorOfAltarTask = null;
		
		if (_halterEscapeTask != null)
		{
			_halterEscapeTask.cancel(false);
		}
		_halterEscapeTask = null;
		
		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}
		_intervalTask = null;
		
		if (_lockUpDoorOfAltarTask != null)
		{
			_lockUpDoorOfAltarTask.cancel(false);
		}
		_lockUpDoorOfAltarTask = null;
		
		if (_movieTask != null)
		{
			_movieTask.cancel(false);
		}
		_movieTask = null;
		
		if (_openDoorOfAltarTask != null)
		{
			_openDoorOfAltarTask.cancel(false);
		}
		_openDoorOfAltarTask = null;
		
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;
		
		// Delete all monsters
		deleteVanHalter();
		deleteTriolRevelation();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualSacrifice();
		deleteRitualOffering();
		deleteGuardOfAltar();
		deleteCameraMarker();
		
		// Clear flag.
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;
		
		// Set door state
		closeDoorOfSacrifice();
		openDoorOfAltar(true);
		
		// Respawn monsters.
		spawnTriolRevelation();
		spawnRoyalGuard();
		spawnRitualOffering();
		spawnVanHalter();
		
		GrandBossManager.getInstance().setBossStatus(29062, NOTSPAWN);
		
		// Set time up.
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPool.schedule(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER);
	}
	
	// Time up.
	protected class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			enterInterval();
		}
	}
	
	// Appearance movie.
	private class Movie implements Runnable
	{
		private static final int DISTANCE = 6502500;
		
		private final int _taskId;
		
		public Movie(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void run()
		{
			_vanHalter.setHeading(16384);
			_vanHalter.setTarget(_ritualOffering);
			
			switch (_taskId)
			{
				case 1:
				{
					GrandBossManager.getInstance().setBossStatus(29062, ALIVE);
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= DISTANCE)
						{
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 50, 90, 0, 0, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(2), 16);
					break;
				}
				case 2:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= DISTANCE)
						{
							_cameraMarker.get(5).broadcastPacket(new SpecialCamera(_cameraMarker.get(5).getObjectId(), 1842, 100, -3, 0, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(3), 1);
					break;
				}
				case 3:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= DISTANCE)
						{
							_cameraMarker.get(5).broadcastPacket(new SpecialCamera(_cameraMarker.get(5).getObjectId(), 1861, 97, -10, 1500, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(4), 1500);
					break;
				}
				case 4:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= DISTANCE)
						{
							_cameraMarker.get(4).broadcastPacket(new SpecialCamera(_cameraMarker.get(4).getObjectId(), 1876, 97, 12, 0, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(5), 1);
					break;
				}
				case 5:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= DISTANCE)
						{
							_cameraMarker.get(4).broadcastPacket(new SpecialCamera(_cameraMarker.get(4).getObjectId(), 1839, 94, 0, 1500, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(6), 1500);
					break;
				}
				case 6:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= DISTANCE)
						{
							_cameraMarker.get(3).broadcastPacket(new SpecialCamera(_cameraMarker.get(3).getObjectId(), 1872, 94, 15, 0, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(7), 1);
					break;
				}
				case 7:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= DISTANCE)
						{
							_cameraMarker.get(3).broadcastPacket(new SpecialCamera(_cameraMarker.get(3).getObjectId(), 1839, 92, 0, 1500, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(8), 1500);
					break;
				}
				case 8:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= DISTANCE)
						{
							_cameraMarker.get(2).broadcastPacket(new SpecialCamera(_cameraMarker.get(2).getObjectId(), 1872, 92, 15, 0, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(9), 1);
					break;
				}
				case 9:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= DISTANCE)
						{
							_cameraMarker.get(2).broadcastPacket(new SpecialCamera(_cameraMarker.get(2).getObjectId(), 1839, 90, 5, 1500, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(10), 1500);
					break;
				}
				case 10:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= DISTANCE)
						{
							_cameraMarker.get(1).broadcastPacket(new SpecialCamera(_cameraMarker.get(1).getObjectId(), 1872, 90, 5, 0, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(11), 1);
					break;
				}
				case 11:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= DISTANCE)
						{
							_cameraMarker.get(1).broadcastPacket(new SpecialCamera(_cameraMarker.get(1).getObjectId(), 2002, 90, 2, 1500, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(12), 2000);
					break;
				}
				case 12:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= DISTANCE)
						{
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 50, 90, 10, 0, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(13), 1000);
					break;
				}
				case 13:
				{
					// High Priestess van Halter uses the skill to kill Ritual Offering.
					final Skill skill = SkillTable.getInstance().getInfo(1168, 7);
					_ritualOffering.setInvul(false);
					_vanHalter.setTarget(_ritualOffering);
					// _vanHalter.setImmobilized(false);
					_vanHalter.doCast(skill);
					// _vanHalter.setImmobilized(true);
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(14), 4700);
					break;
				}
				case 14:
				{
					_ritualOffering.setInvul(false);
					_ritualOffering.reduceCurrentHp(_ritualOffering.getMaxHp() + 1, _vanHalter);
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(15), 4300);
					break;
				}
				case 15:
				{
					spawnRitualSacrifice();
					deleteRitualOffering();
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= DISTANCE)
						{
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 100, 90, 15, 1500, 15000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(16), 2000);
					break;
				}
				case 16:
				{
					// Set camera.
					for (PlayerInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= DISTANCE)
						{
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 5200, 90, -10, 9500, 6000));
						}
					}
					// Set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(17), 6000);
					break;
				}
				case 17:
				{
					deleteRitualSacrifice();
					deleteCameraMarker();
					// _vanHalter.setImmobilized(false);
					_vanHalter.setInvul(false);
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPool.schedule(new Movie(18), 1000);
					break;
				}
				case 18:
				{
					combatBeginning();
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new VanHalter();
	}
}
