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
package org.l2jserver.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SepulcherMonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.SepulcherNpcInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.Util;

/**
 * @author sandman TODO: Gatekeepers shouting some text when doors get opened..so far unknown in leaked C4 is this text: 1000502 [brushes hinders competitor's monster.] which is really ugly translation TODO: Victim should attack one npc, when u save this NPC debuff zones will not be activated and
 *         NPC will polymorph into some kind of Tammed Beast xD and shout: 1000503 [many thanks rescue.] which is again really ugly translation. When Victim kill this NPC, debuff zones will get activated with current core its impossible to make attack npc * npc i will try to search where is this
 *         prevented but still is unknown which npc u need to save to survive in next room without debuffs
 */
public class FourSepulchersManager extends GrandBossManager
{
	private static final String QUEST_ID = "Q620_FourGoblets";
	
	private static final int ENTRANCE_PASS = 7075;
	private static final int USED_PASS = 7261;
	private static final int CHAPEL_KEY = 7260;
	private static final int ANTIQUE_BROOCH = 7262;
	
	protected boolean _firstTimeRun;
	protected boolean _inEntryTime = false;
	protected boolean _inWarmUpTime = false;
	protected boolean _inAttackTime = false;
	protected boolean _inCoolDownTime = false;
	
	protected ScheduledFuture<?> _changeCoolDownTimeTask = null;
	protected ScheduledFuture<?> _changeEntryTimeTask = null;
	protected ScheduledFuture<?> _changeWarmUpTimeTask = null;
	protected ScheduledFuture<?> _changeAttackTimeTask = null;
	protected ScheduledFuture<?> _onPartyAnnihilatedTask = null;
	
	private final int[][] _startHallSpawn =
	{
		{
			181632,
			-85587,
			-7218
		},
		{
			179963,
			-88978,
			-7218
		},
		{
			173217,
			-86132,
			-7218
		},
		{
			175608,
			-82296,
			-7218
		}
	};
	
	private final int[][][] _shadowSpawnLoc =
	{
		{
			{
				25339,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25349,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25346,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25342,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25342,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25339,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25349,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25346,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25346,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25342,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25339,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25349,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25349,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25346,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25342,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25339,
				175591,
				-72744,
				-7215,
				49317
			}
		},
	};
	
	protected Map<Integer, Boolean> _archonSpawned = new HashMap<>();
	protected Map<Integer, Boolean> _hallInUse = new HashMap<>();
	protected Map<Integer, int[]> _startHallSpawns = new HashMap<>();
	protected Map<Integer, Integer> _hallGateKeepers = new HashMap<>();
	protected Map<Integer, Integer> _keyBoxNpc = new HashMap<>();
	protected Map<Integer, Integer> _victim = new HashMap<>();
	protected Map<Integer, PlayerInstance> _challengers = new HashMap<>();
	protected Map<Integer, Spawn> _executionerSpawns = new HashMap<>();
	protected Map<Integer, Spawn> _keyBoxSpawns = new HashMap<>();
	protected Map<Integer, Spawn> _mysteriousBoxSpawns = new HashMap<>();
	protected Map<Integer, Spawn> _shadowSpawns = new HashMap<>();
	protected Map<Integer, List<Spawn>> _dukeFinalMobs = new HashMap<>();
	protected Map<Integer, List<SepulcherMonsterInstance>> _dukeMobs = new HashMap<>();
	protected Map<Integer, List<Spawn>> _emperorsGraveNpcs = new HashMap<>();
	protected Map<Integer, List<Spawn>> _magicalMonsters = new HashMap<>();
	protected Map<Integer, List<Spawn>> _physicalMonsters = new HashMap<>();
	protected Map<Integer, List<SepulcherMonsterInstance>> _viscountMobs = new HashMap<>();
	
	protected List<Spawn> _physicalSpawns;
	protected List<Spawn> _magicalSpawns;
	protected List<Spawn> _managers;
	protected List<Spawn> _dukeFinalSpawns;
	protected List<Spawn> _emperorsGraveSpawns;
	protected List<NpcInstance> _allMobs = new ArrayList<>();
	
	protected long _attackTimeEnd = 0;
	protected long _coolDownTimeEnd = 0;
	protected long _entryTimeEnd = 0;
	protected long _warmUpTimeEnd = 0;
	
	protected byte _newCycleMin = 55;
	
	public static final FourSepulchersManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public FourSepulchersManager()
	{
		init();
	}
	
	private void init()
	{
		if (_changeCoolDownTimeTask != null)
		{
			_changeCoolDownTimeTask.cancel(true);
		}
		if (_changeEntryTimeTask != null)
		{
			_changeEntryTimeTask.cancel(true);
		}
		if (_changeWarmUpTimeTask != null)
		{
			_changeWarmUpTimeTask.cancel(true);
		}
		if (_changeAttackTimeTask != null)
		{
			_changeAttackTimeTask.cancel(true);
		}
		
		_changeCoolDownTimeTask = null;
		_changeEntryTimeTask = null;
		_changeWarmUpTimeTask = null;
		_changeAttackTimeTask = null;
		_inEntryTime = false;
		_inWarmUpTime = false;
		_inAttackTime = false;
		_inCoolDownTime = false;
		_firstTimeRun = true;
		initFixedInfo();
		loadMysteriousBox();
		initKeyBoxSpawns();
		loadPhysicalMonsters();
		loadMagicalMonsters();
		initLocationShadowSpawns();
		initExecutionerSpawns();
		loadDukeMonsters();
		loadEmperorsGraveMonsters();
		spawnManagers();
		timeSelector();
	}
	
	// phase select on server launch
	protected void timeSelector()
	{
		timeCalculator();
		final long currentTime = Calendar.getInstance().getTimeInMillis();
		// if current time >= time of entry beginning and if current time < time of entry beginning + time of entry end
		if ((currentTime >= _coolDownTimeEnd) && (currentTime < _entryTimeEnd)) // entry time check
		{
			clean();
			_changeEntryTimeTask = ThreadPool.schedule(new ChangeEntryTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Entry time");
		}
		else if ((currentTime >= _entryTimeEnd) && (currentTime < _warmUpTimeEnd)) // warmup time check
		{
			clean();
			_changeWarmUpTimeTask = ThreadPool.schedule(new ChangeWarmUpTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in WarmUp time");
		}
		else if ((currentTime >= _warmUpTimeEnd) && (currentTime < _attackTimeEnd)) // attack time check
		{
			clean();
			_changeAttackTimeTask = ThreadPool.schedule(new ChangeAttackTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Attack time");
		}
		else // cooldown time and without cleanup because it's already implemented
		{
			_changeCoolDownTimeTask = ThreadPool.schedule(new ChangeCoolDownTime(), 0);
			LOGGER.info("FourSepulchersManager: Beginning in Cooldown time");
		}
	}
	
	protected void timeCalculator()
	{
		final Calendar tmp = Calendar.getInstance();
		if (tmp.get(Calendar.MINUTE) < _newCycleMin)
		{
			tmp.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) - 1);
		}
		tmp.set(Calendar.MINUTE, _newCycleMin);
		_coolDownTimeEnd = tmp.getTimeInMillis();
		_entryTimeEnd = _coolDownTimeEnd + (Config.FS_TIME_ENTRY * 60000);
		_warmUpTimeEnd = _entryTimeEnd + (Config.FS_TIME_WARMUP * 60000);
		_attackTimeEnd = _warmUpTimeEnd + (Config.FS_TIME_ATTACK * 60000);
	}
	
	public void clean()
	{
		for (int i = 31921; i < 31925; i++)
		{
			final int[] location = _startHallSpawns.get(i);
			if ((location != null) && (location.length == 3))
			{
				final BossZone zone = GrandBossManager.getInstance().getZone(location[0], location[1], location[2]);
				if (zone != null)
				{
					zone.oustAllPlayers();
				}
			}
		}
		
		deleteAllMobs();
		closeAllDoors();
		
		_hallInUse.clear();
		_hallInUse.put(31921, false);
		_hallInUse.put(31922, false);
		_hallInUse.put(31923, false);
		_hallInUse.put(31924, false);
		
		if (_archonSpawned.size() != 0)
		{
			final Set<Integer> npcIdSet = _archonSpawned.keySet();
			for (int npcId : npcIdSet)
			{
				_archonSpawned.put(npcId, false);
			}
		}
	}
	
	protected void spawnManagers()
	{
		_managers = new ArrayList<>();
		int i = 31921;
		for (Spawn spawnDat; i <= 31924; i++)
		{
			if ((i < 31921) || (i > 31924))
			{
				continue;
			}
			final NpcTemplate template1 = NpcTable.getInstance().getTemplate(i);
			if (template1 == null)
			{
				continue;
			}
			try
			{
				spawnDat = new Spawn(template1);
				spawnDat.setAmount(1);
				spawnDat.setRespawnDelay(60);
				switch (i)
				{
					case 31921: // conquerors
					{
						spawnDat.setX(181061);
						spawnDat.setY(-85595);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-32584);
						break;
					}
					case 31922: // emperors
					{
						spawnDat.setX(179292);
						spawnDat.setY(-88981);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-33272);
						break;
					}
					case 31923: // sages
					{
						spawnDat.setX(173202);
						spawnDat.setY(-87004);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-16248);
						break;
					}
					case 31924: // judges
					{
						spawnDat.setX(175606);
						spawnDat.setY(-82853);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-16248);
						break;
					}
				}
				_managers.add(spawnDat);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
			catch (SecurityException e)
			{
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void initFixedInfo()
	{
		_startHallSpawns.put(31921, _startHallSpawn[0]);
		_startHallSpawns.put(31922, _startHallSpawn[1]);
		_startHallSpawns.put(31923, _startHallSpawn[2]);
		_startHallSpawns.put(31924, _startHallSpawn[3]);
		
		_hallInUse.put(31921, false);
		_hallInUse.put(31922, false);
		_hallInUse.put(31923, false);
		_hallInUse.put(31924, false);
		
		_hallGateKeepers.put(31925, 25150012);
		_hallGateKeepers.put(31926, 25150013);
		_hallGateKeepers.put(31927, 25150014);
		_hallGateKeepers.put(31928, 25150015);
		_hallGateKeepers.put(31929, 25150016);
		_hallGateKeepers.put(31930, 25150002);
		_hallGateKeepers.put(31931, 25150003);
		_hallGateKeepers.put(31932, 25150004);
		_hallGateKeepers.put(31933, 25150005);
		_hallGateKeepers.put(31934, 25150006);
		_hallGateKeepers.put(31935, 25150032);
		_hallGateKeepers.put(31936, 25150033);
		_hallGateKeepers.put(31937, 25150034);
		_hallGateKeepers.put(31938, 25150035);
		_hallGateKeepers.put(31939, 25150036);
		_hallGateKeepers.put(31940, 25150022);
		_hallGateKeepers.put(31941, 25150023);
		_hallGateKeepers.put(31942, 25150024);
		_hallGateKeepers.put(31943, 25150025);
		_hallGateKeepers.put(31944, 25150026);
		
		_keyBoxNpc.put(18120, 31455);
		_keyBoxNpc.put(18121, 31455);
		_keyBoxNpc.put(18122, 31455);
		_keyBoxNpc.put(18123, 31455);
		_keyBoxNpc.put(18124, 31456);
		_keyBoxNpc.put(18125, 31456);
		_keyBoxNpc.put(18126, 31456);
		_keyBoxNpc.put(18127, 31456);
		_keyBoxNpc.put(18128, 31457);
		_keyBoxNpc.put(18129, 31457);
		_keyBoxNpc.put(18130, 31457);
		_keyBoxNpc.put(18131, 31457);
		_keyBoxNpc.put(18149, 31458);
		_keyBoxNpc.put(18150, 31459);
		_keyBoxNpc.put(18151, 31459);
		_keyBoxNpc.put(18152, 31459);
		_keyBoxNpc.put(18153, 31459);
		_keyBoxNpc.put(18154, 31460);
		_keyBoxNpc.put(18155, 31460);
		_keyBoxNpc.put(18156, 31460);
		_keyBoxNpc.put(18157, 31460);
		_keyBoxNpc.put(18158, 31461);
		_keyBoxNpc.put(18159, 31461);
		_keyBoxNpc.put(18160, 31461);
		_keyBoxNpc.put(18161, 31461);
		_keyBoxNpc.put(18162, 31462);
		_keyBoxNpc.put(18163, 31462);
		_keyBoxNpc.put(18164, 31462);
		_keyBoxNpc.put(18165, 31462);
		_keyBoxNpc.put(18183, 31463);
		_keyBoxNpc.put(18184, 31464);
		_keyBoxNpc.put(18212, 31465);
		_keyBoxNpc.put(18213, 31465);
		_keyBoxNpc.put(18214, 31465);
		_keyBoxNpc.put(18215, 31465);
		_keyBoxNpc.put(18216, 31466);
		_keyBoxNpc.put(18217, 31466);
		_keyBoxNpc.put(18218, 31466);
		_keyBoxNpc.put(18219, 31466);
		
		_victim.put(18150, 18158);
		_victim.put(18151, 18159);
		_victim.put(18152, 18160);
		_victim.put(18153, 18161);
		_victim.put(18154, 18162);
		_victim.put(18155, 18163);
		_victim.put(18156, 18164);
		_victim.put(18157, 18165);
	}
	
	private void loadMysteriousBox()
	{
		_mysteriousBoxSpawns.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY id");
			statement.setInt(1, 0);
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
					final int keyNpcId = rset.getInt("key_npc_id");
					_mysteriousBoxSpawns.put(keyNpcId, spawnDat);
				}
				else
				{
					LOGGER.warning("FourSepulchersManager.LoadMysteriousBox: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.warning("FourSepulchersManager.LoadMysteriousBox: Spawn could not be initialized: " + e);
		}
	}
	
	private void initKeyBoxSpawns()
	{
		Spawn spawnDat;
		NpcTemplate template;
		for (Entry<Integer, Integer> entry : _keyBoxNpc.entrySet())
		{
			final int id = entry.getValue();
			try
			{
				template = NpcTable.getInstance().getTemplate(id);
				if (template != null)
				{
					spawnDat = new Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setX(0);
					spawnDat.setY(0);
					spawnDat.setZ(0);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(3600);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_keyBoxSpawns.put(entry.getKey(), spawnDat);
				}
				else
				{
					LOGGER.warning("FourSepulchersManager.InitKeyBoxSpawns: Data missing in NPC table for ID: " + id + ".");
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("FourSepulchersManager.InitKeyBoxSpawns: Spawn could not be initialized: " + e);
			}
		}
	}
	
	private void loadPhysicalMonsters()
	{
		_physicalMonsters.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 1);
			final ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 1);
				final ResultSet rset2 = statement2.executeQuery();
				Spawn spawnDat;
				NpcTemplate template1;
				_physicalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						_physicalSpawns.add(spawnDat);
					}
					else
					{
						LOGGER.warning("FourSepulchersManager.LoadPhysicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				_physicalMonsters.put(keyNpcId, _physicalSpawns);
			}
			
			rset1.close();
			statement1.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.warning("FourSepulchersManager.LoadPhysicalMonsters: Spawn could not be initialized: " + e);
		}
	}
	
	private void loadMagicalMonsters()
	{
		_magicalMonsters.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 2);
			final ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 2);
				final ResultSet rset2 = statement2.executeQuery();
				Spawn spawnDat;
				NpcTemplate template1;
				_magicalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						_magicalSpawns.add(spawnDat);
					}
					else
					{
						LOGGER.warning("FourSepulchersManager.LoadMagicalMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				_magicalMonsters.put(keyNpcId, _magicalSpawns);
			}
			
			rset1.close();
			statement1.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.warning("FourSepulchersManager.LoadMagicalMonsters: Spawn could not be initialized: " + e);
		}
	}
	
	private void loadDukeMonsters()
	{
		_dukeFinalMobs.clear();
		_archonSpawned.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 5);
			final ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 5);
				final ResultSet rset2 = statement2.executeQuery();
				Spawn spawnDat;
				NpcTemplate template1;
				_dukeFinalSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						_dukeFinalSpawns.add(spawnDat);
					}
					else
					{
						LOGGER.warning("FourSepulchersManager.LoadDukeMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				_dukeFinalMobs.put(keyNpcId, _dukeFinalSpawns);
				_archonSpawned.put(keyNpcId, false);
			}
			
			rset1.close();
			statement1.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.warning("FourSepulchersManager.LoadDukeMonsters: Spawn could not be initialized: " + e);
		}
	}
	
	private void loadEmperorsGraveMonsters()
	{
		_emperorsGraveNpcs.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement1 = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 6);
			final ResultSet rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				final int keyNpcId = rset1.getInt("key_npc_id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 6);
				final ResultSet rset2 = statement2.executeQuery();
				Spawn spawnDat;
				NpcTemplate template1;
				_emperorsGraveSpawns = new ArrayList<>();
				
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new Spawn(template1);
						spawnDat.setAmount(rset2.getInt("count"));
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						_emperorsGraveSpawns.add(spawnDat);
					}
					else
					{
						LOGGER.warning("FourSepulchersManager.LoadEmperorsGraveMonsters: Data missing in NPC table for ID: " + rset2.getInt("npc_templateid") + ".");
					}
				}
				
				rset2.close();
				statement2.close();
				_emperorsGraveNpcs.put(keyNpcId, _emperorsGraveSpawns);
			}
			
			rset1.close();
			statement1.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.warning("FourSepulchersManager.LoadEmperorsGraveMonsters: Spawn could not be initialized: " + e);
		}
	}
	
	protected void initLocationShadowSpawns()
	{
		final int locNo = Rnd.get(4);
		final int[] gateKeeper =
		{
			31929,
			31934,
			31939,
			31944
		};
		
		Spawn spawnDat;
		NpcTemplate template;
		_shadowSpawns.clear();
		
		for (int i = 0; i <= 3; i++)
		{
			template = NpcTable.getInstance().getTemplate(_shadowSpawnLoc[locNo][i][0]);
			if (template != null)
			{
				try
				{
					spawnDat = new Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setX(_shadowSpawnLoc[locNo][i][1]);
					spawnDat.setY(_shadowSpawnLoc[locNo][i][2]);
					spawnDat.setZ(_shadowSpawnLoc[locNo][i][3]);
					spawnDat.setHeading(_shadowSpawnLoc[locNo][i][4]);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					final int keyNpcId = gateKeeper[i];
					_shadowSpawns.put(keyNpcId, spawnDat);
				}
				catch (Exception e)
				{
					LOGGER.warning("initLocationShadowSpawns:" + e.getMessage());
				}
			}
			else
			{
				LOGGER.warning("FourSepulchersManager.InitLocationShadowSpawns: Data missing in NPC table for ID: " + _shadowSpawnLoc[locNo][i][0] + ".");
			}
		}
	}
	
	protected void initExecutionerSpawns()
	{
		Spawn spawnDat;
		NpcTemplate template;
		for (Entry<Integer, Integer> entry : _victim.entrySet())
		{
			final int id = entry.getValue();
			try
			{
				template = NpcTable.getInstance().getTemplate(id);
				if (template != null)
				{
					spawnDat = new Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setX(0);
					spawnDat.setY(0);
					spawnDat.setZ(0);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(3600);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_executionerSpawns.put(entry.getKey(), spawnDat);
				}
				else
				{
					LOGGER.warning("FourSepulchersManager.InitExecutionerSpawns: Data missing in NPC table for ID: " + id + ".");
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("FourSepulchersManager.InitExecutionerSpawns: Spawn could not be initialized: " + e);
			}
		}
	}
	
	public boolean isEntryTime()
	{
		return _inEntryTime;
	}
	
	public boolean isAttackTime()
	{
		return _inAttackTime;
	}
	
	public synchronized void tryEntry(NpcInstance npc, PlayerInstance player)
	{
		final int npcId = npc.getNpcId();
		switch (npcId)
		{
			// ID ok
			case 31921:
			case 31922:
			case 31923:
			case 31924:
			{
				break;
			}
			// ID not ok
			default:
			{
				if (!player.isGM())
				{
					LOGGER.warning("Player " + player.getName() + "(" + player.getObjectId() + ") tried to cheat in four sepulchers.");
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " tried to enter four sepulchers with invalid npc id.", Config.DEFAULT_PUNISH);
				}
				return;
			}
		}
		
		if (_hallInUse.get(npcId).booleanValue())
		{
			showHtmlFile(player, npcId + "-FULL.htm", npc, null);
			return;
		}
		
		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			if (!player.isInParty() || (player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT))
			{
				showHtmlFile(player, npcId + "-SP.htm", npc, null);
				return;
			}
			
			if (!player.getParty().isLeader(player))
			{
				showHtmlFile(player, npcId + "-NL.htm", npc, null);
				return;
			}
			
			for (PlayerInstance mem : player.getParty().getPartyMembers())
			{
				final QuestState qs = mem.getQuestState(QUEST_ID);
				if ((qs == null) || (!qs.isStarted() && !qs.isCompleted()))
				{
					showHtmlFile(player, npcId + "-NS.htm", npc, mem);
					return;
				}
				
				if (mem.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
				{
					showHtmlFile(player, npcId + "-SE.htm", npc, mem);
					return;
				}
				
				if (mem.getWeightPenalty() >= 3)
				{
					mem.sendPacket(SystemMessageId.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORY_S_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
					return;
				}
			}
		}
		else if ((Config.FS_PARTY_MEMBER_COUNT <= 1) && player.isInParty())
		{
			if (!player.getParty().isLeader(player))
			{
				showHtmlFile(player, npcId + "-NL.htm", npc, null);
				return;
			}
			for (PlayerInstance mem : player.getParty().getPartyMembers())
			{
				final QuestState qs = mem.getQuestState(QUEST_ID);
				if ((qs == null) || (!qs.isStarted() && !qs.isCompleted()))
				{
					showHtmlFile(player, npcId + "-NS.htm", npc, mem);
					return;
				}
				
				if (mem.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
				{
					showHtmlFile(player, npcId + "-SE.htm", npc, mem);
					return;
				}
				
				if (mem.getWeightPenalty() >= 3)
				{
					mem.sendPacket(SystemMessageId.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORY_S_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
					return;
				}
			}
		}
		else
		{
			final QuestState qs = player.getQuestState(QUEST_ID);
			if ((qs == null) || (!qs.isStarted() && !qs.isCompleted()))
			{
				showHtmlFile(player, npcId + "-NS.htm", npc, player);
				return;
			}
			
			if (player.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
			{
				showHtmlFile(player, npcId + "-SE.htm", npc, player);
				return;
			}
			
			if (player.getWeightPenalty() >= 3)
			{
				player.sendPacket(SystemMessageId.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORY_S_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				return;
			}
		}
		
		if (!_inEntryTime)
		{
			showHtmlFile(player, npcId + "-NE.htm", npc, null);
			return;
		}
		
		showHtmlFile(player, npcId + "-OK.htm", npc, null);
		entry(npcId, player);
	}
	
	private void entry(int npcId, PlayerInstance player)
	{
		final int[] location = _startHallSpawns.get(npcId);
		int driftX;
		int driftY;
		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			final List<PlayerInstance> members = new ArrayList<>();
			for (PlayerInstance mem : player.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
				{
					members.add(mem);
				}
			}
			
			for (PlayerInstance mem : members)
			{
				GrandBossManager.getInstance().getZone(location[0], location[1], location[2]).allowPlayerEntry(mem, 30);
				driftX = Rnd.get(-80, 80);
				driftY = Rnd.get(-80, 80);
				mem.teleToLocation(location[0] + driftX, location[1] + driftY, location[2]);
				mem.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, mem, true);
				if (mem.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				{
					mem.addItem("Quest", USED_PASS, 1, mem, true);
				}
				
				final ItemInstance hallsKey = mem.getInventory().getItemByItemId(CHAPEL_KEY);
				if (hallsKey != null)
				{
					mem.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), mem, true);
				}
			}
			
			_challengers.remove(npcId);
			_challengers.put(npcId, player);
			
			_hallInUse.remove(npcId);
			_hallInUse.put(npcId, true);
		}
		else if ((Config.FS_PARTY_MEMBER_COUNT <= 1) && player.isInParty())
		{
			final List<PlayerInstance> members = new ArrayList<>();
			for (PlayerInstance mem : player.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
				{
					members.add(mem);
				}
			}
			
			for (PlayerInstance mem : members)
			{
				GrandBossManager.getInstance().getZone(location[0], location[1], location[2]).allowPlayerEntry(mem, 30);
				driftX = Rnd.get(-80, 80);
				driftY = Rnd.get(-80, 80);
				mem.teleToLocation(location[0] + driftX, location[1] + driftY, location[2]);
				mem.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, mem, true);
				if (mem.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				{
					mem.addItem("Quest", USED_PASS, 1, mem, true);
				}
				
				final ItemInstance hallsKey = mem.getInventory().getItemByItemId(CHAPEL_KEY);
				if (hallsKey != null)
				{
					mem.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), mem, true);
				}
			}
			
			_challengers.remove(npcId);
			_challengers.put(npcId, player);
			
			_hallInUse.remove(npcId);
			_hallInUse.put(npcId, true);
		}
		else
		{
			GrandBossManager.getInstance().getZone(location[0], location[1], location[2]).allowPlayerEntry(player, 30);
			driftX = Rnd.get(-80, 80);
			driftY = Rnd.get(-80, 80);
			player.teleToLocation(location[0] + driftX, location[1] + driftY, location[2]);
			player.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, player, true);
			if (player.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
			{
				player.addItem("Quest", USED_PASS, 1, player, true);
			}
			
			final ItemInstance hallsKey = player.getInventory().getItemByItemId(CHAPEL_KEY);
			if (hallsKey != null)
			{
				player.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), player, true);
			}
			
			_challengers.remove(npcId);
			_challengers.put(npcId, player);
			
			_hallInUse.remove(npcId);
			_hallInUse.put(npcId, true);
		}
	}
	
	public void spawnMysteriousBox(int npcId)
	{
		if (!_inAttackTime)
		{
			return;
		}
		
		final Spawn spawnDat = _mysteriousBoxSpawns.get(npcId);
		if (spawnDat != null)
		{
			_allMobs.add(spawnDat.doSpawn());
			spawnDat.stopRespawn();
		}
	}
	
	public void spawnMonster(int npcId)
	{
		if (!_inAttackTime)
		{
			return;
		}
		
		List<Spawn> monsterList;
		final List<SepulcherMonsterInstance> mobs = new ArrayList<>();
		Spawn keyBoxMobSpawn;
		if (Rnd.nextBoolean())
		{
			monsterList = _physicalMonsters.get(npcId);
		}
		else
		{
			monsterList = _magicalMonsters.get(npcId);
		}
		
		if (monsterList != null)
		{
			boolean spawnKeyBoxMob = false;
			boolean spawnedKeyBoxMob = false;
			for (Spawn spawnDat : monsterList)
			{
				if (spawnedKeyBoxMob)
				{
					spawnKeyBoxMob = false;
				}
				else
				{
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
						{
							if (Rnd.get(48) == 0)
							{
								spawnKeyBoxMob = true;
							}
							break;
						}
						default:
						{
							spawnKeyBoxMob = false;
						}
					}
				}
				
				SepulcherMonsterInstance mob = null;
				if (spawnKeyBoxMob)
				{
					try
					{
						final NpcTemplate template = NpcTable.getInstance().getTemplate(18149);
						if (template != null)
						{
							keyBoxMobSpawn = new Spawn(template);
							keyBoxMobSpawn.setAmount(1);
							keyBoxMobSpawn.setX(spawnDat.getX());
							keyBoxMobSpawn.setY(spawnDat.getY());
							keyBoxMobSpawn.setZ(spawnDat.getZ());
							keyBoxMobSpawn.setHeading(spawnDat.getHeading());
							keyBoxMobSpawn.setRespawnDelay(3600);
							SpawnTable.getInstance().addNewSpawn(keyBoxMobSpawn, false);
							mob = (SepulcherMonsterInstance) keyBoxMobSpawn.doSpawn();
							keyBoxMobSpawn.stopRespawn();
						}
						else
						{
							LOGGER.warning("FourSepulchersManager.SpawnMonster: Data missing in NPC table for ID: 18149");
						}
					}
					catch (Exception e)
					{
						LOGGER.warning("FourSepulchersManager.SpawnMonster: Spawn could not be initialized: " + e);
					}
					
					spawnedKeyBoxMob = true;
				}
				else
				{
					mob = (SepulcherMonsterInstance) spawnDat.doSpawn();
					spawnDat.stopRespawn();
				}
				
				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
						case 31472:
						case 31477:
						case 31482:
						case 31487:
						{
							mobs.add(mob);
						}
					}
					_allMobs.add(mob);
				}
			}
			
			switch (npcId)
			{
				case 31469:
				case 31474:
				case 31479:
				case 31484:
				{
					_viscountMobs.put(npcId, mobs);
					break;
				}
				case 31472:
				case 31477:
				case 31482:
				case 31487:
				{
					_dukeMobs.put(npcId, mobs);
					break;
				}
			}
		}
	}
	
	public synchronized boolean isViscountMobsAnnihilated(int npcId)
	{
		final List<SepulcherMonsterInstance> mobs = _viscountMobs.get(npcId);
		if (mobs == null)
		{
			return true;
		}
		
		for (SepulcherMonsterInstance mob : mobs)
		{
			if (!mob.isDead())
			{
				return false;
			}
		}
		
		return true;
	}
	
	public synchronized boolean isDukeMobsAnnihilated(int npcId)
	{
		final List<SepulcherMonsterInstance> mobs = _dukeMobs.get(npcId);
		if (mobs == null)
		{
			return true;
		}
		
		for (SepulcherMonsterInstance mob : mobs)
		{
			if (!mob.isDead())
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void spawnKeyBox(NpcInstance activeChar)
	{
		if (!_inAttackTime)
		{
			return;
		}
		
		final Spawn spawnDat = _keyBoxSpawns.get(activeChar.getNpcId());
		if (spawnDat != null)
		{
			spawnDat.setAmount(1);
			spawnDat.setX(activeChar.getX());
			spawnDat.setY(activeChar.getY());
			spawnDat.setZ(activeChar.getZ());
			spawnDat.setHeading(activeChar.getHeading());
			spawnDat.setRespawnDelay(3600);
			_allMobs.add(spawnDat.doSpawn());
			spawnDat.stopRespawn();
		}
	}
	
	public void spawnExecutionerOfHalisha(NpcInstance activeChar)
	{
		if (!_inAttackTime)
		{
			return;
		}
		
		final Spawn spawnDat = _executionerSpawns.get(activeChar.getNpcId());
		if (spawnDat != null)
		{
			spawnDat.setAmount(1);
			spawnDat.setX(activeChar.getX());
			spawnDat.setY(activeChar.getY());
			spawnDat.setZ(activeChar.getZ());
			spawnDat.setHeading(activeChar.getHeading());
			spawnDat.setRespawnDelay(3600);
			_allMobs.add(spawnDat.doSpawn());
			spawnDat.stopRespawn();
		}
	}
	
	public void spawnArchonOfHalisha(int npcId)
	{
		if (!_inAttackTime)
		{
			return;
		}
		
		if (_archonSpawned.get(npcId))
		{
			return;
		}
		
		final List<Spawn> monsterList = _dukeFinalMobs.get(npcId);
		if (monsterList != null)
		{
			for (Spawn spawnDat : monsterList)
			{
				final SepulcherMonsterInstance mob = (SepulcherMonsterInstance) spawnDat.doSpawn();
				spawnDat.stopRespawn();
				
				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					_allMobs.add(mob);
				}
			}
			_archonSpawned.put(npcId, true);
		}
	}
	
	public void spawnEmperorsGraveNpc(int npcId)
	{
		if (!_inAttackTime)
		{
			return;
		}
		
		final List<Spawn> monsterList = _emperorsGraveNpcs.get(npcId);
		if (monsterList != null)
		{
			for (Spawn spawnDat : monsterList)
			{
				_allMobs.add(spawnDat.doSpawn());
				spawnDat.stopRespawn();
			}
		}
	}
	
	protected void locationShadowSpawns()
	{
		final int locNo = Rnd.get(4);
		final int[] gateKeeper =
		{
			31929,
			31934,
			31939,
			31944
		};
		
		Spawn spawnDat;
		for (int i = 0; i <= 3; i++)
		{
			final int keyNpcId = gateKeeper[i];
			spawnDat = _shadowSpawns.get(keyNpcId);
			spawnDat.setX(_shadowSpawnLoc[locNo][i][1]);
			spawnDat.setY(_shadowSpawnLoc[locNo][i][2]);
			spawnDat.setZ(_shadowSpawnLoc[locNo][i][3]);
			spawnDat.setHeading(_shadowSpawnLoc[locNo][i][4]);
			_shadowSpawns.put(keyNpcId, spawnDat);
		}
	}
	
	public void spawnShadow(int npcId)
	{
		if (!_inAttackTime)
		{
			return;
		}
		
		final Spawn spawnDat = _shadowSpawns.get(npcId);
		if (spawnDat != null)
		{
			final SepulcherMonsterInstance mob = (SepulcherMonsterInstance) spawnDat.doSpawn();
			spawnDat.stopRespawn();
			
			if (mob != null)
			{
				mob.mysteriousBoxId = npcId;
				_allMobs.add(mob);
			}
		}
	}
	
	public void deleteAllMobs()
	{
		for (NpcInstance mob : _allMobs)
		{
			try
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			catch (Exception e)
			{
				LOGGER.warning("deleteAllMobs: " + e.getMessage());
			}
		}
		_allMobs.clear();
	}
	
	protected void closeAllDoors()
	{
		for (int doorId : _hallGateKeepers.values())
		{
			final DoorInstance door = DoorData.getInstance().getDoor(doorId);
			try
			{
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					LOGGER.warning("Could not find door with id " + doorId);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Failed closing door " + doorId + " - " + e.getMessage());
			}
		}
	}
	
	protected byte minuteSelect(byte min)
	{
		if ((min % 5) != 0)// if doesn't divides on 5 fully
		{
			// mad table for selecting proper minutes...
			// may be there is a better way to do this
			switch (min)
			{
				case 6:
				case 7:
				{
					min = 5;
					break;
				}
				case 8:
				case 9:
				case 11:
				case 12:
				{
					min = 10;
					break;
				}
				case 13:
				case 14:
				case 16:
				case 17:
				{
					min = 15;
					break;
				}
				case 18:
				case 19:
				case 21:
				case 22:
				{
					min = 20;
					break;
				}
				case 23:
				case 24:
				case 26:
				case 27:
				{
					min = 25;
					break;
				}
				case 28:
				case 29:
				case 31:
				case 32:
				{
					min = 30;
					break;
				}
				case 33:
				case 34:
				case 36:
				case 37:
				{
					min = 35;
					break;
				}
				case 38:
				case 39:
				case 41:
				case 42:
				{
					min = 40;
					break;
				}
				case 43:
				case 44:
				case 46:
				case 47:
				{
					min = 45;
					break;
				}
				case 48:
				case 49:
				case 51:
				case 52:
				{
					min = 50;
					break;
				}
				case 53:
				case 54:
				case 56:
				case 57:
				{
					min = 55;
					break;
				}
			}
		}
		return min;
	}
	
	public void managerSay(byte min)
	{
		// for attack phase, sending message every 5 minutes
		if (_inAttackTime)
		{
			if (min < 5)
			{
				return; // do not shout when < 5 minutes
			}
			
			min = minuteSelect(min);
			String msg = min + " minute(s) have passed.";
			if (min == 90)
			{
				msg = "Game over. The teleport will appear momentarily";
			}
			
			for (Spawn temp : _managers)
			{
				if (temp == null)
				{
					LOGGER.warning("FourSepulchersManager: managerSay(): manager is null");
					continue;
				}
				if (!(temp.getLastSpawn() instanceof SepulcherNpcInstance))
				{
					LOGGER.warning("FourSepulchersManager: managerSay(): manager is not Sepulcher instance");
					continue;
				}
				// hall not used right now, so its manager will not tell you anything :)
				// if you don't need this - delete next two lines.
				if (!_hallInUse.get(temp.getNpcId()).booleanValue())
				{
					continue;
				}
				
				((SepulcherNpcInstance) temp.getLastSpawn()).sayInShout(msg);
			}
		}
		
		else if (_inEntryTime)
		{
			final String msg1 = "You may now enter the Sepulcher";
			final String msg2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter";
			for (Spawn temp : _managers)
			{
				if (temp == null)
				{
					LOGGER.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				if (!(temp.getLastSpawn() instanceof SepulcherNpcInstance))
				{
					LOGGER.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				((SepulcherNpcInstance) temp.getLastSpawn()).sayInShout(msg1);
				((SepulcherNpcInstance) temp.getLastSpawn()).sayInShout(msg2);
			}
		}
	}
	
	protected class ManagerSay implements Runnable
	{
		@Override
		public void run()
		{
			if (_inAttackTime)
			{
				final Calendar tmp = Calendar.getInstance();
				tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - _warmUpTimeEnd);
				if ((tmp.get(Calendar.MINUTE) + 5) < Config.FS_TIME_ATTACK)
				{
					managerSay((byte) tmp.get(Calendar.MINUTE)); // byte because minute cannot be more than 59
					ThreadPool.schedule(new ManagerSay(), 5 * 60000);
				}
				// attack time ending chat
				else if ((tmp.get(Calendar.MINUTE) + 5) >= Config.FS_TIME_ATTACK)
				{
					managerSay((byte) 90); // sending a unique id :D
				}
			}
			else if (_inEntryTime)
			{
				managerSay((byte) 0);
			}
		}
	}
	
	protected class ChangeEntryTime implements Runnable
	{
		@Override
		public void run()
		{
			_inEntryTime = true;
			_inWarmUpTime = false;
			_inAttackTime = false;
			_inCoolDownTime = false;
			long interval = 0;
			// if this is first launch - search time when entry time will be ended: counting difference between time when entry time ends and current time and then launching change time task
			if (_firstTimeRun)
			{
				interval = _entryTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_ENTRY * 60000; // else use stupid method
			}
			
			// launching saying process...
			ThreadPool.schedule(new ManagerSay(), 0);
			_changeWarmUpTimeTask = ThreadPool.schedule(new ChangeWarmUpTime(), interval);
			if (_changeEntryTimeTask != null)
			{
				_changeEntryTimeTask.cancel(true);
				_changeEntryTimeTask = null;
			}
		}
	}
	
	protected class ChangeWarmUpTime implements Runnable
	{
		@Override
		public void run()
		{
			_inEntryTime = true;
			_inWarmUpTime = false;
			_inAttackTime = false;
			_inCoolDownTime = false;
			long interval = 0;
			// searching time when warmup time will be ended: counting difference between time when warmup time ends and current time and then launching change time task
			if (_firstTimeRun)
			{
				interval = _warmUpTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_WARMUP * 60000;
			}
			_changeAttackTimeTask = ThreadPool.schedule(new ChangeAttackTime(), interval);
			if (_changeWarmUpTimeTask != null)
			{
				_changeWarmUpTimeTask.cancel(true);
				_changeWarmUpTimeTask = null;
			}
		}
	}
	
	protected class ChangeAttackTime implements Runnable
	{
		@Override
		public void run()
		{
			_inEntryTime = false;
			_inWarmUpTime = false;
			_inAttackTime = true;
			_inCoolDownTime = false;
			locationShadowSpawns();
			
			spawnMysteriousBox(31921);
			spawnMysteriousBox(31922);
			spawnMysteriousBox(31923);
			spawnMysteriousBox(31924);
			
			if (!_firstTimeRun)
			{
				_warmUpTimeEnd = Calendar.getInstance().getTimeInMillis();
			}
			
			long interval = 0;
			// say task
			if (_firstTimeRun)
			{
				for (double min = Calendar.getInstance().get(Calendar.MINUTE); min < _newCycleMin; min++)
				{
					// looking for next shout time....
					if ((min % 5) == 0) // check if min can be divided by 5
					{
						final Calendar inter = Calendar.getInstance();
						inter.set(Calendar.MINUTE, (int) min);
						ThreadPool.schedule(new ManagerSay(), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
						break;
					}
				}
			}
			else
			{
				ThreadPool.schedule(new ManagerSay(), 5 * 60400);
			}
			// searching time when attack time will be ended: counting difference between time when attack time ends and current time and then launching change time task
			if (_firstTimeRun)
			{
				interval = _attackTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_ATTACK * 60000;
			}
			_changeCoolDownTimeTask = ThreadPool.schedule(new ChangeCoolDownTime(), interval);
			if (_changeAttackTimeTask != null)
			{
				_changeAttackTimeTask.cancel(true);
				_changeAttackTimeTask = null;
			}
		}
	}
	
	protected class ChangeCoolDownTime implements Runnable
	{
		@Override
		public void run()
		{
			_inEntryTime = false;
			_inWarmUpTime = false;
			_inAttackTime = false;
			_inCoolDownTime = true;
			clean();
			
			final Calendar time = Calendar.getInstance();
			// one hour = 55th min to 55 min of next hour, so we check for this, also check for first launch
			if ((Calendar.getInstance().get(Calendar.MINUTE) > _newCycleMin) && !_firstTimeRun)
			{
				time.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) + 1);
			}
			time.set(Calendar.MINUTE, _newCycleMin);
			if (_firstTimeRun)
			{
				_firstTimeRun = false; // cooldown phase ends event hour, so it will be not first run
			}
			
			final long interval = time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			_changeEntryTimeTask = ThreadPool.schedule(new ChangeEntryTime(), interval);
			if (_changeCoolDownTimeTask != null)
			{
				_changeCoolDownTimeTask.cancel(true);
				_changeCoolDownTimeTask = null;
			}
		}
	}
	
	public Map<Integer, Integer> getHallGateKeepers()
	{
		return _hallGateKeepers;
	}
	
	public void showHtmlFile(PlayerInstance player, String file, NpcInstance npc, PlayerInstance member)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/SepulcherNpc/" + file);
		if (member != null)
		{
			html.replace("%member%", member.getName());
		}
		player.sendPacket(html);
	}
	
	private static class SingletonHolder
	{
		protected static final FourSepulchersManager INSTANCE = new FourSepulchersManager();
	}
}
