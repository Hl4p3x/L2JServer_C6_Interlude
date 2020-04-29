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
package org.l2jserver.gameserver.model.spawn;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.sql.TerritoryTable;
import org.l2jserver.gameserver.geoengine.GeoEngine;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.taskmanager.RespawnTaskManager;
import org.l2jserver.gameserver.util.Util;

/**
 * This class manages the spawn and respawn of a group of NpcInstance that are in the same are and have the same type. <b><u>Concept</u>:</b><br>
 * <br>
 * NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position. The heading of the NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<br>
 * <br>
 * @author Nightmare
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:32 $
 */
public class Spawn
{
	protected static final Logger LOGGER = Logger.getLogger(Spawn.class.getName());
	
	private NpcTemplate _template;
	private int _id;
	private int _location;
	private int _maximumCount;
	private int _currentCount;
	public int _scheduledCount;
	private int _locX;
	private int _locY;
	private int _locZ;
	private int _heading;
	private int _respawnDelay;
	private int _respawnMinDelay;
	private int _respawnMaxDelay;
	private Constructor<?> _constructor;
	private boolean _doRespawn;
	private int _instanceId = 0;
	private NpcInstance _lastSpawn;
	private static List<SpawnListener> _spawnListeners = new ArrayList<>();
	
	/**
	 * Constructor of Spawn.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Each Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...). All of those properties are stored in a different NpcTemplate for each type of Spawn. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of Spawn is created,
	 * server just create a link between the instance and the template. This link is stored in <b>_template</b><br>
	 * Each NpcInstance is linked to a Spawn that manages its spawn and respawn (delay, location...). This link is stored in <b>_spawn</b> of the NpcInstance<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the _template of the Spawn</li>
	 * <li>Calculate the implementationName used to generate the generic constructor of NpcInstance managed by this Spawn</li>
	 * <li>Create the generic constructor of NpcInstance managed by this Spawn</li><br>
	 * @param mobTemplate The NpcTemplate to link to this Spawn
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public Spawn(NpcTemplate mobTemplate) throws ClassNotFoundException, NoSuchMethodException
	{
		// Set the _template of the Spawn
		_template = mobTemplate;
		if (_template == null)
		{
			return;
		}
		
		// The Name of the NpcInstance type managed by this Spawn
		String implementationName = _template.getType(); // implementing class name
		if (mobTemplate.getNpcId() == 30995)
		{
			implementationName = "RaceManager";
		}
		
		if ((mobTemplate.getNpcId() >= 31046) && (mobTemplate.getNpcId() <= 31053))
		{
			implementationName = "SymbolMaker";
		}
		
		// Create the generic constructor of NpcInstance managed by this Spawn
		final Class<?>[] parameters =
		{
			int.class,
			NpcTemplate.class
		};
		_constructor = Class.forName("org.l2jserver.gameserver.model.actor.instance." + implementationName + "Instance").getConstructor(parameters);
	}
	
	/**
	 * @return the maximum number of NpcInstance that this Spawn can manage.
	 */
	public int getAmount()
	{
		return _maximumCount;
	}
	
	/**
	 * @return the Identifier of this L2spawn (used as key in the SpawnTable).
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the Identifier of the location area where NpcInstance can be spawned.
	 */
	public int getLocation()
	{
		return _location;
	}
	
	/**
	 * @return the X position of the spawn point.
	 */
	public int getX()
	{
		return _locX;
	}
	
	/**
	 * @return the Y position of the spawn point.
	 */
	public int getY()
	{
		return _locY;
	}
	
	/**
	 * @return the Z position of the spawn point.
	 */
	public int getZ()
	{
		return _locZ;
	}
	
	/**
	 * @return the Identifier of the NpcInstance manage by this L2spawn contained in the NpcTemplate.
	 */
	public int getNpcId()
	{
		if (_template == null)
		{
			return -1;
		}
		return _template.getNpcId();
	}
	
	/**
	 * @return the heading of NpcInstance when they are spawned.
	 */
	public int getHeading()
	{
		return _heading;
	}
	
	/**
	 * @return the delay between a NpcInstance remove and its re-spawn.
	 */
	public int getRespawnDelay()
	{
		return _respawnDelay;
	}
	
	/**
	 * @return Min RaidBoss Spawn delay.
	 */
	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}
	
	/**
	 * @return Max RaidBoss Spawn delay.
	 */
	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}
	
	/**
	 * Set the maximum number of NpcInstance that this Spawn can manage.
	 * @param amount
	 */
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}
	
	/**
	 * Set the Identifier of this L2spawn (used as key in the SpawnTable).
	 * @param id
	 */
	public void setId(int id)
	{
		_id = id;
	}
	
	/**
	 * Set the Identifier of the location area where NpcInstance can be spawned.
	 * @param location
	 */
	public void setLocation(int location)
	{
		_location = location;
	}
	
	/**
	 * Set Minimum Respawn Delay.
	 * @param date
	 */
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}
	
	/**
	 * Set Maximum Respawn Delay.
	 * @param date
	 */
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}
	
	/**
	 * Set the X position of the spawn point.
	 * @param locx
	 */
	public void setX(int locx)
	{
		_locX = locx;
	}
	
	/**
	 * Set the Y position of the spawn point.
	 * @param locy
	 */
	public void setY(int locy)
	{
		_locY = locy;
	}
	
	/**
	 * Set the Z position of the spawn point.
	 * @param locz
	 */
	public void setZ(int locz)
	{
		_locZ = locz;
	}
	
	/**
	 * Set the heading of NpcInstance when they are spawned.
	 * @param heading
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public void setLoc(int locx, int locy, int locz, int heading)
	{
		_locX = locx;
		_locY = locy;
		_locZ = locz;
		_heading = heading;
	}
	
	/**
	 * Decrease the current number of NpcInstance of this Spawn and if necessary create a SpawnTask to launch after the respawn Delay.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Decrease the current number of NpcInstance of this Spawn</li>
	 * <li>Check if respawn is possible to prevent multiple respawning caused by lag</li>
	 * <li>Update the current number of SpawnTask in progress or stand by of this Spawn</li>
	 * <li>Create a new SpawnTask to launch after the respawn Delay</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: A respawn is possible ONLY if _doRespawn=True and _scheduledCount + _currentCount < _maximumCount</b></font>
	 * @param oldNpc
	 */
	public void decreaseCount(NpcInstance oldNpc)
	{
		// Decrease the current number of NpcInstance of this Spawn
		_currentCount--;
		
		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (_doRespawn && ((_scheduledCount + _currentCount) < _maximumCount))
		{
			// Update the current number of SpawnTask in progress or stand by of this Spawn
			_scheduledCount++;
			
			// Schedule the next respawn.
			RespawnTaskManager.getInstance().add(oldNpc, System.currentTimeMillis() + _respawnDelay);
		}
	}
	
	/**
	 * Create the initial spawning and set _doRespawn to True.
	 * @return The number of NpcInstance that were spawned
	 */
	public int init()
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn();
		}
		_doRespawn = true;
		return _currentCount;
	}
	
	/**
	 * Set _doRespawn to False to stop respawn in this Spawn.
	 */
	public void stopRespawn()
	{
		_doRespawn = false;
	}
	
	/**
	 * Set _doRespawn to True to start or restart respawn in this Spawn.
	 */
	public void startRespawn()
	{
		_doRespawn = true;
	}
	
	/**
	 * Create the NpcInstance, add it to the world and launch its OnSpawn action.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position. The heading of the NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<br>
	 * <br>
	 * <b><u>Actions for an random spawn into location area</u> : <i>(if Locx=0 and Locy=0)</i></b><br>
	 * <li>Get NpcInstance Init parameters and its generate an Identifier</li>
	 * <li>Call the constructor of the NpcInstance</li>
	 * <li>Calculate the random position in the location area (if Locx=0 and Locy=0) or get its exact position from the Spawn</li>
	 * <li>Set the position of the NpcInstance</li>
	 * <li>Set the HP and MP of the NpcInstance to the max</li>
	 * <li>Set the heading of the NpcInstance (random heading if not defined : value=-1)</li>
	 * <li>Link the NpcInstance to this Spawn</li>
	 * <li>Init other values of the NpcInstance (ex : from its CreatureTemplate for INT, STR, DEX...) and add it in the world</li>
	 * <li>Lauch the action OnSpawn fo the NpcInstance</li>
	 * <li>Increase the current number of NpcInstance managed by this Spawn</li><br>
	 * @return
	 */
	public NpcInstance doSpawn()
	{
		NpcInstance npc = null;
		try
		{
			// Check if the Spawn is not a Net or Minion spawn
			if (_template.getType().equalsIgnoreCase("Pet") || _template.getType().equalsIgnoreCase("Minion"))
			{
				_currentCount++;
				
				return npc;
			}
			
			// Get NpcInstance Init parameters and its generate an Identifier
			final Object[] parameters =
			{
				IdFactory.getNextId(),
				_template
			};
			
			// Call the constructor of the NpcInstance
			// (can be a ArtefactInstance, FriendlyMobInstance, GuardInstance, MonsterInstance, SiegeGuardInstance, FeedableBeastInstance, TamedBeastInstance, FolkInstance)
			final Object tmp = _constructor.newInstance(parameters);
			
			// Must be done before object is spawned into visible world
			((WorldObject) tmp).setInstanceId(_instanceId);
			
			// Check if the Instance is a NpcInstance
			if (!(tmp instanceof NpcInstance))
			{
				return npc;
			}
			
			npc = (NpcInstance) tmp;
			return initializeNpcInstance(npc);
		}
		catch (Exception e)
		{
			LOGGER.warning("NPC " + _template.getNpcId() + " class not found " + e);
		}
		return npc;
	}
	
	/**
	 * @param npc
	 * @return
	 */
	private NpcInstance initializeNpcInstance(NpcInstance npc)
	{
		int newlocx;
		int newlocy;
		int newlocz;
		
		// If Locx=0 and Locy=0, the NpcInstance must be spawned in an area defined by location
		if ((_locX == 0) && (_locY == 0))
		{
			if (_location == 0)
			{
				return npc;
			}
			
			// Calculate the random position in the location area
			final int[] p = TerritoryTable.getInstance().getRandomPoint(getLocation());
			
			// Set the calculated position of the NpcInstance
			newlocx = p[0];
			newlocy = p[1];
			newlocz = p[3];
		}
		else
		{
			// The NpcInstance is spawned at the exact position (Lox, Locy, Locz)
			newlocx = _locX;
			newlocy = _locY;
			newlocz = _locZ;
		}
		
		// Do not correct z of flying NPCs.
		if (!npc.isFlying())
		{
			final int geoZ = GeoEngine.getInstance().getHeight(newlocx, newlocy, newlocz);
			// Do not correct Z distances greater than 300.
			if (Util.calculateDistance(newlocx, newlocy, newlocz, newlocx, newlocy, geoZ, true) < 300)
			{
				newlocz = geoZ;
			}
		}
		
		npc.stopAllEffects();
		
		// Set the HP and MP of the NpcInstance to the max
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
		
		// Clear script value.
		npc.setScriptValue(0);
		
		// Set the heading of the NpcInstance (random heading if not defined)
		if (_heading == -1)
		{
			npc.setHeading(Rnd.get(61794));
		}
		else
		{
			npc.setHeading(_heading);
		}
		
		// Reset decay info
		npc.setDecayed(false);
		
		// Link the NpcInstance to this Spawn
		npc.setSpawn(this);
		
		// Init other values of the NpcInstance (ex : from its CreatureTemplate for INT, STR, DEX...) and add it in the world as a visible object
		npc.spawnMe(newlocx, newlocy, newlocz);
		notifyNpcSpawned(npc);
		
		_lastSpawn = npc;
		for (Quest quest : npc.getTemplate().getEventQuests(EventType.ON_SPAWN))
		{
			quest.notifySpawn(npc);
		}
		
		// Increase the current number of NpcInstance managed by this Spawn
		_currentCount++;
		
		return npc;
	}
	
	public static void addSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.add(listener);
		}
	}
	
	public static void removeSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.remove(listener);
		}
	}
	
	public static void notifyNpcSpawned(NpcInstance npc)
	{
		synchronized (_spawnListeners)
		{
			for (SpawnListener listener : _spawnListeners)
			{
				listener.npcSpawned(npc);
			}
		}
	}
	
	/**
	 * @param i delay in seconds
	 */
	public void setRespawnDelay(int i)
	{
		if (i < 0)
		{
			LOGGER.warning("respawn delay is negative for spawnId:" + _id);
		}
		
		if (i < 10)
		{
			i = 10;
		}
		
		_respawnDelay = i * 1000;
	}
	
	public NpcInstance getLastSpawn()
	{
		return _lastSpawn;
	}
	
	public void respawnNpc(NpcInstance oldNpc)
	{
		if (_doRespawn)
		{
			// oldNpc.refreshId();
			initializeNpcInstance(oldNpc);
		}
	}
	
	public NpcTemplate getTemplate()
	{
		return _template;
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
}
