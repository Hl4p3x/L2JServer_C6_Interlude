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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.Announcements;

/**
 * Auto Spawn Handler Allows spawning of a NPC object based on a timer. (From the official idea used for the Merchant and Blacksmith of Mammon) General Usage: - Call registerSpawn() with the parameters listed below. int npcId int[][] spawnPoints or specify NULL to add points later. int initialDelay
 * (If < 0 = default value) int respawnDelay (If < 0 = default value) int despawnDelay (If < 0 = default value or if = 0, function disabled) spawnPoints is a standard two-dimensional int array containing X,Y and Z coordinates. The default respawn/despawn delays are currently every hour (as for
 * Mammon on official servers). - The resulting AutoSpawnInstance object represents the newly added spawn index. - The internal methods of this object can be used to adjust random spawning, for instance a call to setRandomSpawn(1, true); would set the spawn at index 1 to be randomly rather than
 * sequentially-based. - Also they can be used to specify the number of NPC instances to spawn using setSpawnCount(), and broadcast a message to all users using setBroadcast(). Random Spawning = OFF by default Broadcasting = OFF by default
 * @author Tempy
 */
public class AutoSpawn
{
	protected static final Logger LOGGER = Logger.getLogger(AutoSpawn.class.getName());
	
	private static final int DEFAULT_INITIAL_SPAWN = 30000; // 30 seconds after registration
	private static final int DEFAULT_RESPAWN = 3600000; // 1 hour in millisecs
	private static final int DEFAULT_DESPAWN = 3600000; // 1 hour in millisecs
	
	protected Map<Integer, AutoSpawnInstance> _registeredSpawns;
	protected Map<Integer, ScheduledFuture<?>> _runningSpawns;
	
	protected boolean _activeState = true;
	
	private AutoSpawn()
	{
		_registeredSpawns = new HashMap<>();
		_runningSpawns = new HashMap<>();
		restoreSpawnData();
	}
	
	public int size()
	{
		synchronized (_registeredSpawns)
		{
			return _registeredSpawns.size();
		}
	}
	
	private void restoreSpawnData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = null;
			PreparedStatement statement2 = null;
			ResultSet rs = null;
			ResultSet rs2 = null;
			
			// Restore spawn group data, then the location data.
			statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				// Register random spawn group, set various options on the created spawn instance.
				final AutoSpawnInstance spawnInst = registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));
				spawnInst.setSpawnCount(rs.getInt("count"));
				spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
				
				// Restore the spawn locations for this spawn group/instance.
				statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");
				statement2.setInt(1, rs.getInt("groupId"));
				rs2 = statement2.executeQuery();
				
				while (rs2.next())
				{
					// Add each location to the spawn group/instance.
					spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
				}
				
				statement2.close();
				rs2.close();
			}
			
			statement.close();
			rs.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("AutoSpawnHandler: Could not restore spawn data: " + e);
		}
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks it as active. Returns a AutoSpawnInstance containing info about the spawn.
	 * @param npcId
	 * @param spawnPoints
	 * @param initialDelay (If < 0 = default value)
	 * @param respawnDelay (If < 0 = default value)
	 * @param despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if (initialDelay < 0)
		{
			initialDelay = DEFAULT_INITIAL_SPAWN;
		}
		
		if (respawnDelay < 0)
		{
			respawnDelay = DEFAULT_RESPAWN;
		}
		
		if (despawnDelay < 0)
		{
			despawnDelay = DEFAULT_DESPAWN;
		}
		
		final AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);
		if (spawnPoints != null)
		{
			for (int[] spawnPoint : spawnPoints)
			{
				newSpawn.addSpawnLocation(spawnPoint);
			}
		}
		
		final int newId = IdFactory.getNextId();
		newSpawn._objectId = newId;
		
		synchronized (_registeredSpawns)
		{
			_registeredSpawns.put(newId, newSpawn);
		}
		
		setSpawnActive(newSpawn, true);
		return newSpawn;
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks it as active. Returns a AutoSpawnInstance containing info about the spawn.<br>
	 * <b>Warning:</b> Spawn locations must be specified separately using addSpawnLocation().
	 * @param npcId
	 * @param initialDelay (If < 0 = default value)
	 * @param respawnDelay (If < 0 = default value)
	 * @param despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return spawnInst
	 */
	public AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay)
	{
		return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}
	
	/**
	 * Remove a registered spawn from the list, specified by the given spawn instance.
	 * @param spawnInst
	 * @return removedSuccessfully
	 */
	public boolean removeSpawn(AutoSpawnInstance spawnInst)
	{
		synchronized (_registeredSpawns)
		{
			if (!_registeredSpawns.containsValue(spawnInst))
			{
				return false;
			}
			
			// Try to remove from the list of registered spawns if it exists.
			_registeredSpawns.remove(spawnInst.getNpcId());
			
			synchronized (_runningSpawns)
			{
				// Cancel the currently associated running scheduled task.
				final ScheduledFuture<?> respawnTask = _runningSpawns.remove(spawnInst._objectId);
				
				try
				{
					respawnTask.cancel(false);
					
				}
				catch (Exception e)
				{
					LOGGER.warning("AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + e);
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Remove a registered spawn from the list, specified by the given spawn object ID.
	 * @param objectId
	 */
	public void removeSpawn(int objectId)
	{
		AutoSpawnInstance spawn = null;
		synchronized (_registeredSpawns)
		{
			spawn = _registeredSpawns.get(objectId);
		}
		removeSpawn(spawn);
	}
	
	/**
	 * Sets the active state of the specified spawn.
	 * @param spawnInst
	 * @param isActive
	 */
	public void setSpawnActive(AutoSpawnInstance spawnInst, boolean isActive)
	{
		if (spawnInst == null)
		{
			return;
		}
		
		final int objectId = spawnInst._objectId;
		if (isSpawnRegistered(objectId))
		{
			ScheduledFuture<?> spawnTask = null;
			if (isActive)
			{
				final AutoSpawner rs = new AutoSpawner(objectId);
				if (spawnInst._desDelay > 0)
				{
					spawnTask = ThreadPool.scheduleAtFixedRate(rs, spawnInst._initDelay, spawnInst._resDelay);
				}
				else
				{
					spawnTask = ThreadPool.schedule(rs, spawnInst._initDelay);
				}
				
				synchronized (_runningSpawns)
				{
					_runningSpawns.put(objectId, spawnTask);
				}
			}
			else
			{
				final AutoDespawner rd = new AutoDespawner(objectId);
				
				synchronized (_runningSpawns)
				{
					spawnTask = _runningSpawns.remove(objectId);
				}
				
				if (spawnTask != null)
				{
					spawnTask.cancel(false);
				}
				
				ThreadPool.schedule(rd, 0);
			}
			
			spawnInst.setSpawnActive(isActive);
		}
	}
	
	/**
	 * Sets the active state of all auto spawn instances to that specified, and cancels the scheduled spawn task if necessary.
	 * @param isActive
	 */
	public void setAllActive(boolean isActive)
	{
		if (_activeState == isActive)
		{
			return;
		}
		
		Collection<AutoSpawnInstance> instances;
		synchronized (_registeredSpawns)
		{
			instances = _registeredSpawns.values();
		}
		
		for (AutoSpawnInstance spawnInst : instances)
		{
			setSpawnActive(spawnInst, isActive);
		}
		
		_activeState = isActive;
	}
	
	/**
	 * Returns the number of milliseconds until the next occurrence of the given spawn.
	 * @param spawnInst
	 * @return
	 */
	public long getTimeToNextSpawn(AutoSpawnInstance spawnInst)
	{
		if (spawnInst == null)
		{
			return -1;
		}
		final int objectId = spawnInst.getObjectId();
		
		synchronized (_runningSpawns)
		{
			final ScheduledFuture<?> task = _runningSpawns.get(objectId);
			if (task != null)
			{
				return task.getDelay(TimeUnit.MILLISECONDS);
			}
		}
		return -1;
	}
	
	/**
	 * Attempts to return the AutoSpawnInstance associated with the given NPC or Object ID type.<br>
	 * Note: If isObjectId == false, returns first instance for the specified NPC ID.
	 * @param id
	 * @param isObjectId
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId)
	{
		if (isObjectId)
		{
			return _registeredSpawns.get(id);
		}
		
		Collection<AutoSpawnInstance> instances;
		synchronized (_registeredSpawns)
		{
			instances = _registeredSpawns.values();
		}
		
		for (AutoSpawnInstance spawnInst : instances)
		{
			if (spawnInst.getNpcId() == id)
			{
				return spawnInst;
			}
		}
		return null;
	}
	
	public Map<Integer, AutoSpawnInstance> getAutoSpawnInstances(int npcId)
	{
		final Map<Integer, AutoSpawnInstance> spawnInstList = new HashMap<>();
		Collection<AutoSpawnInstance> instances;
		synchronized (_registeredSpawns)
		{
			instances = _registeredSpawns.values();
		}
		
		for (AutoSpawnInstance spawnInst : instances)
		{
			if (spawnInst.getNpcId() == npcId)
			{
				spawnInstList.put(spawnInst.getObjectId(), spawnInst);
			}
		}
		
		return spawnInstList;
	}
	
	/**
	 * Tests if the specified object ID is assigned to an auto spawn.
	 * @param objectId
	 * @return boolean isAssigned
	 */
	public boolean isSpawnRegistered(int objectId)
	{
		synchronized (_registeredSpawns)
		{
			return _registeredSpawns.containsKey(objectId);
		}
	}
	
	/**
	 * Tests if the specified spawn instance is assigned to an auto spawn.
	 * @param spawnInst
	 * @return boolean isAssigned
	 */
	public boolean isSpawnRegistered(AutoSpawnInstance spawnInst)
	{
		synchronized (_registeredSpawns)
		{
			return _registeredSpawns.containsValue(spawnInst);
		}
	}
	
	/**
	 * AutoSpawner Class<br>
	 * This handles the main spawn task for an auto spawn instance, and initializes a despawner if required.
	 * @author Tempy
	 */
	private class AutoSpawner implements Runnable
	{
		private final int _objectId;
		
		protected AutoSpawner(int objectId)
		{
			_objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				AutoSpawnInstance spawnInst = null;
				
				synchronized (_registeredSpawns)
				{
					// Retrieve the required spawn instance for this spawn task.
					spawnInst = _registeredSpawns.get(_objectId);
				}
				
				// If the spawn is not scheduled to be active, cancel the spawn task.
				if (!spawnInst.isSpawnActive())
				{
					return;
				}
				
				final Location[] locationList = spawnInst.getLocationList();
				
				// If there are no set co-ordinates, cancel the spawn task.
				if (locationList.length == 0)
				{
					LOGGER.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + _objectId + ").");
					return;
				}
				
				final int locationCount = locationList.length;
				int locationIndex = Rnd.get(locationCount);
				
				// If random spawning is disabled, the spawn at the next set of co-ordinates after the last. If the index is greater than the number of possible spawns, reset the counter to zero.
				if (!spawnInst.isRandomSpawn())
				{
					locationIndex = spawnInst._lastLocIndex;
					locationIndex++;
					
					if (locationIndex == locationCount)
					{
						locationIndex = 0;
					}
					
					spawnInst._lastLocIndex = locationIndex;
				}
				
				// Set the X, Y and Z co-ordinates, where this spawn will take place.
				final int x = locationList[locationIndex].getX();
				final int y = locationList[locationIndex].getY();
				final int z = locationList[locationIndex].getZ();
				final int heading = locationList[locationIndex].getHeading();
				
				// Fetch the template for this NPC ID and create a new spawn.
				final NpcTemplate npcTemp = NpcTable.getInstance().getTemplate(spawnInst.getNpcId());
				if (npcTemp == null)
				{
					LOGGER.warning("Couldnt find NPC id" + spawnInst.getNpcId() + " Try to update your DP");
					return;
				}
				
				final Spawn newSpawn = new Spawn(npcTemp);
				newSpawn.setX(x);
				newSpawn.setY(y);
				newSpawn.setZ(z);
				
				if (heading != -1)
				{
					newSpawn.setHeading(heading);
				}
				
				newSpawn.setAmount(spawnInst.getSpawnCount());
				
				if (spawnInst._desDelay == 0)
				{
					newSpawn.setRespawnDelay(spawnInst._resDelay);
				}
				
				// Add the new spawn information to the spawn table, but do not store it.
				SpawnTable.getInstance().addNewSpawn(newSpawn, false);
				NpcInstance npcInst = null;
				if (spawnInst._spawnCount == 1)
				{
					npcInst = newSpawn.doSpawn();
					npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
					spawnInst.addNpcInstance(npcInst);
				}
				else
				{
					for (int i = 0; i < spawnInst._spawnCount; i++)
					{
						npcInst = newSpawn.doSpawn();
						
						// To prevent spawning of more than one NPC in the exact same spot, move it slightly by a small random offset.
						npcInst.setXYZ(npcInst.getX() + Rnd.get(50), npcInst.getY() + Rnd.get(50), npcInst.getZ());
						
						// Add the NPC instance to the list of managed instances.
						spawnInst.addNpcInstance(npcInst);
					}
				}
				
				final String nearestTown = MapRegionData.getInstance().getClosestTownName(npcInst);
				
				// Announce to all players that the spawn has taken place, with the nearest town location.
				if (spawnInst.isBroadcasting() && (npcInst != null))
				{
					Announcements.getInstance().announceToAll("The " + npcInst.getName() + " has spawned near " + nearestTown + "!");
				}
				
				// If there is no despawn time, do not create a despawn task.
				if (spawnInst.getDespawnDelay() > 0)
				{
					final AutoDespawner rd = new AutoDespawner(_objectId);
					ThreadPool.schedule(rd, spawnInst.getDespawnDelay() - 1000);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): " + e);
			}
		}
	}
	
	/**
	 * AutoDespawner Class<br>
	 * Simply used as a secondary class for despawning an auto spawn instance.
	 * @author Tempy
	 */
	private class AutoDespawner implements Runnable
	{
		private final int _objectId;
		
		protected AutoDespawner(int objectId)
		{
			_objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				AutoSpawnInstance spawnInst = null;
				synchronized (_registeredSpawns)
				{
					spawnInst = _registeredSpawns.get(_objectId);
				}
				
				if (spawnInst == null)
				{
					LOGGER.info("AutoSpawnHandler: No spawn registered for object ID = " + _objectId + ".");
					return;
				}
				
				final NpcInstance[] npcs = spawnInst.getNPCInstanceList();
				if (npcs == null)
				{
					LOGGER.info("AutoSpawnHandler: No spawn registered");
					return;
				}
				
				for (NpcInstance npc : npcs)
				{
					if (npc == null)
					{
						continue;
					}
					
					npc.deleteMe();
					spawnInst.removeNpcInstance(npc);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + _objectId + "): " + e);
			}
		}
	}
	
	/**
	 * AutoSpawnInstance Class<br>
	 * Stores information about a registered auto spawn.
	 * @author Tempy
	 */
	public class AutoSpawnInstance
	{
		protected int _objectId;
		
		protected int _spawnIndex;
		
		protected int _npcId;
		
		protected int _initDelay;
		
		protected int _resDelay;
		
		protected int _desDelay;
		
		protected int _spawnCount = 1;
		
		protected int _lastLocIndex = -1;
		
		private final List<NpcInstance> _npcList = new ArrayList<>();
		
		private final List<Location> _locList = new ArrayList<>();
		
		private boolean _spawnActive;
		
		private boolean _randomSpawn = false;
		
		private boolean _broadcastAnnouncement = false;
		
		protected AutoSpawnInstance(int npcId, int initDelay, int respawnDelay, int despawnDelay)
		{
			_npcId = npcId;
			_initDelay = initDelay;
			_resDelay = respawnDelay;
			_desDelay = despawnDelay;
		}
		
		protected void setSpawnActive(boolean activeValue)
		{
			_spawnActive = activeValue;
		}
		
		protected boolean addNpcInstance(NpcInstance npcInst)
		{
			return _npcList.add(npcInst);
		}
		
		protected boolean removeNpcInstance(NpcInstance npcInst)
		{
			return _npcList.remove(npcInst);
		}
		
		public int getObjectId()
		{
			return _objectId;
		}
		
		public int getInitialDelay()
		{
			return _initDelay;
		}
		
		public int getRespawnDelay()
		{
			return _resDelay;
		}
		
		public int getDespawnDelay()
		{
			return _desDelay;
		}
		
		public int getNpcId()
		{
			return _npcId;
		}
		
		public int getSpawnCount()
		{
			return _spawnCount;
		}
		
		public Location[] getLocationList()
		{
			return _locList.toArray(new Location[_locList.size()]);
		}
		
		public NpcInstance[] getNPCInstanceList()
		{
			NpcInstance[] ret;
			
			synchronized (_npcList)
			{
				ret = new NpcInstance[_npcList.size()];
				_npcList.toArray(ret);
			}
			return ret;
		}
		
		public Spawn[] getSpawns()
		{
			final List<Spawn> npcSpawns = new ArrayList<>();
			for (NpcInstance npcInst : _npcList)
			{
				npcSpawns.add(npcInst.getSpawn());
			}
			return npcSpawns.toArray(new Spawn[npcSpawns.size()]);
		}
		
		public void setSpawnCount(int spawnCount)
		{
			_spawnCount = spawnCount;
		}
		
		public void setRandomSpawn(boolean random)
		{
			_randomSpawn = random;
		}
		
		public void setBroadcast(boolean broadcast)
		{
			_broadcastAnnouncement = broadcast;
		}
		
		public boolean isSpawnActive()
		{
			return _spawnActive;
		}
		
		public boolean isRandomSpawn()
		{
			return _randomSpawn;
		}
		
		public boolean isBroadcasting()
		{
			return _broadcastAnnouncement;
		}
		
		public boolean addSpawnLocation(int x, int y, int z, int heading)
		{
			return _locList.add(new Location(x, y, z, heading));
		}
		
		public boolean addSpawnLocation(int[] spawnLoc)
		{
			if (spawnLoc.length != 3)
			{
				return false;
			}
			return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
		}
		
		public Location removeSpawnLocation(int locIndex)
		{
			try
			{
				return _locList.remove(locIndex);
			}
			catch (IndexOutOfBoundsException e)
			{
				return null;
			}
		}
	}
	
	public static AutoSpawn getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSpawn INSTANCE = new AutoSpawn();
	}
}
