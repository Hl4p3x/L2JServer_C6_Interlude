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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;

/**
 * Core AI
 * @author qwerty, Mobius
 */
public class Core extends Quest
{
	// NPCs
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	private static final int SUSCEPTOR = 29011;
	// Spawns
	private static final Map<Integer, Location> MINNION_SPAWNS = new HashMap<>();
	static
	{
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(17191, 109298, -6488));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(17564, 109548, -6488));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(17855, 109552, -6488));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(18280, 109202, -6488));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(18784, 109253, -6488));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(18059, 108314, -6488));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(17300, 108444, -6488));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(17148, 110071, -6648));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(18318, 110077, -6648));
		MINNION_SPAWNS.put(DEATH_KNIGHT, new Location(17726, 110391, -6648));
		MINNION_SPAWNS.put(DOOM_WRAITH, new Location(17113, 110970, -6648));
		MINNION_SPAWNS.put(DOOM_WRAITH, new Location(17496, 110880, -6648));
		MINNION_SPAWNS.put(DOOM_WRAITH, new Location(18061, 110990, -6648));
		MINNION_SPAWNS.put(DOOM_WRAITH, new Location(18384, 110698, -6648));
		MINNION_SPAWNS.put(DOOM_WRAITH, new Location(17993, 111458, -6584));
		MINNION_SPAWNS.put(SUSCEPTOR, new Location(17297, 111470, -6584));
		MINNION_SPAWNS.put(SUSCEPTOR, new Location(17893, 110198, -6648));
		MINNION_SPAWNS.put(SUSCEPTOR, new Location(17706, 109423, -6488));
		MINNION_SPAWNS.put(SUSCEPTOR, new Location(17849, 109388, -6480));
	}
	// Misc
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	private static boolean _firstAttacked;
	
	private static final Collection<Attackable> _minions = ConcurrentHashMap.newKeySet();
	
	public Core()
	{
		super(-1, "ai/bosses");
		
		final int[] mobs =
		{
			CORE,
			DEATH_KNIGHT,
			DOOM_WRAITH,
			SUSCEPTOR
		};
		
		for (int mob : mobs)
		{
			addEventId(mob, EventType.ON_KILL);
			addEventId(mob, EventType.ON_ATTACK);
		}
		
		_firstAttacked = false;
		final StatSet info = GrandBossManager.getInstance().getStatSet(CORE);
		if (GrandBossManager.getInstance().getBossStatus(CORE) == DEAD)
		{
			// Load the unlock date and time for Core from DB.
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			// If Core is locked until a certain time, mark it so and start the unlock timer the unlock time has not yet expired.
			if (temp > 0)
			{
				startQuestTimer("core_unlock", temp, null, null);
			}
			else
			{
				// The time has already expired while the server was offline. Immediately spawn Core.
				final GrandBossInstance core = (GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Raid boss " + core.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			if (GlobalVariablesManager.getInstance().getBoolean("CoreAttacked", false))
			{
				_firstAttacked = true;
			}
			final GrandBossInstance core = (GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
			if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
			{
				Announcements.getInstance().announceToAll("Raid boss " + core.getName() + " spawned in world.");
			}
			spawnBoss(core);
		}
	}
	
	@Override
	public void saveGlobalData()
	{
		GlobalVariablesManager.getInstance().set("CoreAttacked", _firstAttacked);
	}
	
	public void spawnBoss(GrandBossInstance npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
		// Spawn minions
		Attackable mob;
		Location spawnLocation;
		for (Entry<Integer, Location> spawn : MINNION_SPAWNS.entrySet())
		{
			spawnLocation = spawn.getValue();
			mob = (Attackable) addSpawn(spawn.getKey(), spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), Rnd.get(61794), false, 0);
			_minions.add(mob);
		}
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final Integer status = GrandBossManager.getInstance().getBossStatus(CORE);
		if (event.equals("core_unlock"))
		{
			final GrandBossInstance core = (GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
			if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
			{
				Announcements.getInstance().announceToAll("Raid boss " + core.getName() + " spawned in world.");
			}
			GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (status == null)
		{
			LOGGER.warning("GrandBoss with Id " + CORE + " has not valid status into GrandBossManager.");
		}
		else if (event.equals("spawn_minion") && (status == ALIVE))
		{
			_minions.add((Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0));
		}
		else if (event.equals("despawn_minions"))
		{
			for (Attackable mob : _minions)
			{
				if (mob != null)
				{
					mob.decayMe();
				}
			}
			_minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == CORE)
		{
			if (_firstAttacked)
			{
				if (Rnd.get(100) == 0)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "Removing intruders."));
				}
			}
			else
			{
				_firstAttacked = true;
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "A non-permitted target has been discovered."));
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "Starting intruder removal system."));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		final String name = npc.getName();
		if (npcId == CORE)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, name, "A fatal error has occurred."));
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, name, "System is being shut down..."));
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, name, "......"));
			_firstAttacked = false;
			addSpawn(31842, 16502, 110165, -6394, 0, false, 900000);
			addSpawn(31842, 18948, 110166, -6397, 0, false, 900000);
			GrandBossManager.getInstance().setBossStatus(CORE, DEAD);
			// Calculate Min and Max respawn times randomly.
			final long respawnTime = (Config.CORE_RESP_FIRST + Rnd.get(Config.CORE_RESP_SECOND)) * 3600000;
			startQuestTimer("core_unlock", respawnTime, null, null);
			// Also save the respawn time so that the info is maintained past reboots.
			final StatSet info = GrandBossManager.getInstance().getStatSet(CORE);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(CORE, info);
			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if ((GrandBossManager.getInstance().getBossStatus(CORE) == ALIVE) && _minions.contains(npc))
		{
			_minions.remove(npc);
			startQuestTimer("spawn_minion", Config.CORE_RESP_MINION * 1000, npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Core();
	}
}
