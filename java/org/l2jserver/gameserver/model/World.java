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
package org.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @version $Revision: 1.21.2.5.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public class World
{
	private static final Logger LOGGER = Logger.getLogger(World.class.getName());
	
	public static volatile int MAX_CONNECTED_COUNT = 0;
	public static volatile int OFFLINE_TRADE_COUNT = 0;
	
	public static final int SHIFT_BY = 12;
	
	// Geodata min/max tiles
	public static final int TILE_X_MIN = 16;
	public static final int TILE_X_MAX = 26;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_Y_MAX = 25;
	
	// Map dimensions
	public static final int TILE_SIZE = 32768;
	public static final int MAP_MIN_X = (TILE_X_MIN - 20) * TILE_SIZE;
	public static final int MAP_MAX_X = (TILE_X_MAX - 19) * TILE_SIZE;
	public static final int MAP_MIN_Y = (TILE_Y_MIN - 18) * TILE_SIZE;
	public static final int MAP_MAX_Y = (TILE_Y_MAX - 17) * TILE_SIZE;
	
	/** calculated offset used so top left region is 0,0. */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	
	/** The Constant OFFSET_Y. */
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	
	/** number of regions. */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	
	/** The Constant REGIONS_Y. */
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	/** HashMap(String Player name, PlayerInstance) containing all the players in game. */
	private static Map<String, PlayerInstance> _allPlayers = new ConcurrentHashMap<>();
	
	/** WorldObjectHashMap(WorldObject) containing all visible objects. */
	private static final Map<Integer, WorldObject> _allObjects = new ConcurrentHashMap<>();
	
	/** List with the pets instances and their owner id. */
	private static final Map<Integer, PetInstance> _petsInstance = new ConcurrentHashMap<>();
	
	/** The _world regions. */
	private WorldRegion[][] _worldRegions;
	
	private World()
	{
		initRegions();
	}
	
	/**
	 * Add WorldObject object in _allObjects.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Withdraw an item from the warehouse, create an item</li>
	 * <li>Spawn a Creature (PC, NPC, Pet)</li><br>
	 * @param object the object
	 */
	public void storeObject(WorldObject object)
	{
		_allObjects.putIfAbsent(object.getObjectId(), object);
	}
	
	/**
	 * Remove WorldObject object from _allObjects of World.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Delete item from inventory, tranfer Item from inventory to warehouse</li>
	 * <li>Crystallize item</li>
	 * <li>Remove NPC/PC/Pet from the world</li><br>
	 * @param object WorldObject to remove from _allObjects of World
	 */
	public void removeObject(WorldObject object)
	{
		_allObjects.remove(object.getObjectId());
	}
	
	/**
	 * Removes the objects.
	 * @param list the list
	 */
	public void removeObjects(List<WorldObject> list)
	{
		for (WorldObject o : list)
		{
			_allObjects.remove(o.getObjectId());
		}
	}
	
	/**
	 * Removes the objects.
	 * @param objects the objects
	 */
	public void removeObjects(WorldObject[] objects)
	{
		for (WorldObject o : objects)
		{
			_allObjects.remove(o.getObjectId());
		}
	}
	
	/**
	 * Return the WorldObject object that belongs to an ID or null if no object found.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li><br>
	 * @param oID Identifier of the WorldObject
	 * @return the object
	 */
	public WorldObject findObject(int oID)
	{
		return _allObjects.get(oID);
	}
	
	/**
	 * Added by Tempy - 08 Aug 05 Allows easy retrevial of all visible objects in world. -- do not use that fucntion, its unsafe!
	 * @return the all visible objects
	 */
	public Collection<WorldObject> getAllVisibleObjects()
	{
		return _allObjects.values();
	}
	
	/**
	 * Get the count of all visible objects in world.
	 * @return count off all World objects
	 */
	public int getAllVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	/**
	 * Return a collection containing all players in game.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Read-only, please! </b></font>
	 * @return the all players
	 */
	public Collection<PlayerInstance> getAllPlayers()
	{
		return _allPlayers.values();
	}
	
	/**
	 * Return how many players are online.
	 * @return number of online players.
	 */
	public static Integer getAllPlayersCount()
	{
		return _allPlayers.size();
	}
	
	/**
	 * Return the player instance corresponding to the given name.
	 * @param name Name of the player to get Instance
	 * @return the player
	 */
	public PlayerInstance getPlayer(String name)
	{
		return _allPlayers.get(name.toLowerCase());
	}
	
	/**
	 * Gets the player.
	 * @param playerObjId the player obj id
	 * @return the player
	 */
	public PlayerInstance getPlayer(int playerObjId)
	{
		for (PlayerInstance actual : _allPlayers.values())
		{
			if (actual.getObjectId() == playerObjId)
			{
				return actual;
			}
		}
		return null;
	}
	
	/**
	 * Return a collection containing all pets in game.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Read-only, please! </b></font>
	 * @return the all pets
	 */
	public Collection<PetInstance> getAllPets()
	{
		return _petsInstance.values();
	}
	
	/**
	 * Return the pet instance from the given ownerId.
	 * @param ownerId ID of the owner
	 * @return the pet
	 */
	public PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}
	
	/**
	 * Add the given pet instance from the given ownerId.
	 * @param ownerId ID of the owner
	 * @param pet PetInstance of the pet
	 * @return the pet instance
	 */
	public PetInstance addPet(int ownerId, PetInstance pet)
	{
		return _petsInstance.put(ownerId, pet);
	}
	
	/**
	 * Remove the given pet instance.
	 * @param ownerId ID of the owner
	 */
	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}
	
	/**
	 * Remove the given pet instance.
	 * @param pet the pet to remove
	 */
	public void removePet(PetInstance pet)
	{
		_petsInstance.values().remove(pet);
	}
	
	/**
	 * Add a WorldObject in the world.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * WorldObject (including PlayerInstance) are identified in <b>_visibleObjects</b> of his current WorldRegion and in <b>_knownObjects</b> of other surrounding Creatures<br>
	 * PlayerInstance are identified in <b>_allPlayers</b> of World, in <b>_allPlayers</b> of his current WorldRegion and in <b>_knownPlayer</b> of other surrounding Creatures<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Add the WorldObject object in _allPlayers* of World</li>
	 * <li>Add the WorldObject object in _gmList** of GmListTable</li>
	 * <li>Add object in _knownObjects and _knownPlayer* of all surrounding WorldRegion Creatures</li>
	 * <li>If object is a Creature, add all surrounding WorldObject in its _knownObjects and all surrounding PlayerInstance in its _knownPlayer</li><br>
	 * <i>* only if object is a PlayerInstance</i><br>
	 * <i>** only if object is a GM PlayerInstance</i><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T ADD the object in _visibleObjects and _allPlayers* of WorldRegion (need synchronisation)</b></font><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T ADD the object to _allObjects and _allPlayers* of World (need synchronisation)</b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Drop an Item</li>
	 * <li>Spawn a Creature</li>
	 * <li>Apply Death Penalty of a PlayerInstance</li><br>
	 * @param object L2object to add in the world
	 * @param newRegion the new region
	 * @param dropper Creature who has dropped the object (if necessary)
	 */
	public void addVisibleObject(WorldObject object, WorldRegion newRegion, Creature dropper)
	{
		if (object instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) object;
			final PlayerInstance tmp = _allPlayers.get(player.getName().toLowerCase());
			if ((tmp != null) && (tmp != player)) // just kick the player previous instance
			{
				tmp.store(); // Store character and items
				tmp.logout();
				
				if (tmp.getClient() != null)
				{
					tmp.getClient().setPlayer(null); // prevent deleteMe from being called a second time on disconnection
				}
			}
			
			if (!newRegion.isActive())
			{
				return;
			}
			
			// Go through the visible objects contained in the circular area
			for (WorldObject wo : getVisibleObjects(object, 2000))
			{
				if (wo == null)
				{
					continue;
				}
				
				// Add the object in WorldObjectHashSet(WorldObject) _knownObjects of the visible Creature according to conditions :
				// - Creature is visible
				// - object is not already known
				// - object is in the watch distance
				// If WorldObject is a PlayerInstance, add WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the visible Creature
				wo.getKnownList().addKnownObject(object);
				
				// Add the visible WorldObject in WorldObjectHashSet(WorldObject) _knownObjects of the object according to conditions
				// If visible WorldObject is a PlayerInstance, add visible WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the object
				object.getKnownList().addKnownObject(wo);
			}
			
			if (!player.isTeleporting() && (tmp != null))
			{
				LOGGER.warning("Teleporting: Duplicate character!? Closing both characters (" + player.getName() + ")");
				player.closeNetConnection();
				tmp.closeNetConnection();
				return;
			}
			
			synchronized (_allPlayers)
			{
				_allPlayers.put(player.getName().toLowerCase(), player);
			}
		}
		
		// Go through the visible objects contained in the circular area
		for (WorldObject wo : getVisibleObjects(object, 2000))
		{
			// Add the object in WorldObjectHashSet(WorldObject) _knownObjects of the visible Creature according to conditions :
			// - Creature is visible
			// - object is not already known
			// - object is in the watch distance
			// If WorldObject is a PlayerInstance, add WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the visible Creature
			wo.getKnownList().addKnownObject(object, dropper);
			
			// Add the visible WorldObject in WorldObjectHashSet(WorldObject) _knownObjects of the object according to conditions
			// If visible WorldObject is a PlayerInstance, add visible WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the object
			object.getKnownList().addKnownObject(wo, dropper);
		}
	}
	
	/**
	 * Add the PlayerInstance to _allPlayers of World.
	 * @param player the cha
	 */
	public void addToAllPlayers(PlayerInstance player)
	{
		_allPlayers.put(player.getName().toLowerCase(), player);
	}
	
	/**
	 * Remove the PlayerInstance from _allPlayers of World.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Remove a player fom the visible objects</li><br>
	 * @param player the cha
	 */
	public void removeFromAllPlayers(PlayerInstance player)
	{
		if ((player != null) && !player.isTeleporting())
		{
			_allPlayers.remove(player.getName().toLowerCase());
		}
	}
	
	/**
	 * Remove a WorldObject from the world.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * WorldObject (including PlayerInstance) are identified in <b>_visibleObjects</b> of his current WorldRegion and in <b>_knownObjects</b> of other surrounding Creatures<br>
	 * PlayerInstance are identified in <b>_allPlayers</b> of World, in <b>_allPlayers</b> of his current WorldRegion and in <b>_knownPlayer</b> of other surrounding Creatures<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove the WorldObject object from _allPlayers* of World</li>
	 * <li>Remove the WorldObject object from _visibleObjects and _allPlayers* of WorldRegion</li>
	 * <li>Remove the WorldObject object from _gmList** of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding WorldRegion Creatures</li>
	 * <li>If object is a Creature, remove all WorldObject from its _knownObjects and all PlayerInstance from its _knownPlayer</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T REMOVE the object from _allObjects of World</b></font><br>
	 * <i>* only if object is a PlayerInstance</i><br>
	 * <i>** only if object is a GM PlayerInstance</i><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Pickup an Item</li>
	 * <li>Decay a Creature</li><br>
	 * @param object L2object to remove from the world
	 * @param oldRegion the old region
	 */
	public void removeVisibleObject(WorldObject object, WorldRegion oldRegion)
	{
		if ((object == null) || (oldRegion == null))
		{
			return;
		}
		
		// Remove the object from the WorldObjectHashSet(WorldObject) _visibleObjects of WorldRegion
		// If object is a PlayerInstance, remove it from the WorldObjectHashSet(PlayerInstance) _allPlayers of this WorldRegion
		oldRegion.removeVisibleObject(object);
		
		// Go through all surrounding WorldRegion Creatures
		for (WorldRegion worldRegion : oldRegion.getSurroundingRegions())
		{
			for (WorldObject wo : worldRegion.getVisibleObjects())
			{
				// Remove the WorldObject from the WorldObjectHashSet(WorldObject) _knownObjects of the surrounding WorldRegion Creatures
				// If object is a PlayerInstance, remove the WorldObject from the WorldObjectHashSet(PlayerInstance) _knownPlayer of the surrounding WorldRegion Creatures
				// If object is targeted by one of the surrounding WorldRegion Creatures, cancel ATTACK and cast
				if ((wo != null) && (wo.getKnownList() != null))
				{
					wo.getKnownList().removeKnownObject(object);
				}
				
				// Remove surrounding WorldRegion Creatures from the WorldObjectHashSet(WorldObject) _KnownObjects of object
				// If surrounding WorldRegion Creatures is a PlayerInstance, remove it from the WorldObjectHashSet(PlayerInstance) _knownPlayer of object
				//
				if (object.getKnownList() != null)
				{
					object.getKnownList().removeKnownObject(wo);
				}
			}
		}
		
		// If object is a Creature :
		// Remove all WorldObject from WorldObjectHashSet(WorldObject) containing all WorldObject detected by the Creature
		// Remove all PlayerInstance from WorldObjectHashSet(PlayerInstance) containing all player ingame detected by the Creature
		object.getKnownList().removeAllKnownObjects();
		
		// If selected WorldObject is a NcIntance, remove it from WorldObjectHashSet(PlayerInstance) _allPlayers of World
		if (object instanceof PlayerInstance)
		{
			if (object.getActingPlayer().isInOfflineMode())
			{
				OFFLINE_TRADE_COUNT--;
			}
			
			if (!((PlayerInstance) object).isTeleporting())
			{
				removeFromAllPlayers((PlayerInstance) object);
			}
		}
	}
	
	/**
	 * Return all visible objects of the WorldRegion object's and of its surrounding WorldRegion.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All visible object are identified in <b>_visibleObjects</b> of their current WorldRegion<br>
	 * All surrounding WorldRegion are identified in <b>_surroundingRegions</b> of the selected WorldRegion in order to scan a large area around a WorldObject<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Find Close Objects for Creature</li><br>
	 * @param object L2object that determine the current WorldRegion
	 * @return the visible objects
	 */
	public List<WorldObject> getVisibleObjects(WorldObject object)
	{
		if (object == null)
		{
			return null;
		}
		
		final WorldRegion region = object.getWorldRegion();
		if (region == null)
		{
			return null;
		}
		
		// Create a list in order to contain all visible WorldObject
		final List<WorldObject> result = new ArrayList<>();
		
		// Go through the list of region
		for (WorldRegion worldRegion : region.getSurroundingRegions())
		{
			for (WorldObject wo : worldRegion.getVisibleObjects())
			{
				if (wo == null)
				{
					continue;
				}
				
				if (wo.equals(object))
				{
					continue; // skip our own character
				}
				
				if (!wo.isVisible())
				{
					continue; // skip dying objects
				}
				
				result.add(wo);
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the WorldRegions in the circular area (radius) centered on the object.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All visible object are identified in <b>_visibleObjects</b> of their current WorldRegion<br>
	 * All surrounding WorldRegion are identified in <b>_surroundingRegions</b> of the selected WorldRegion in order to scan a large area around a WorldObject<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Define the aggrolist of monster</li>
	 * <li>Define visible objects of a WorldObject</li>
	 * <li>Skill : Confusion...</li>
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the circular area
	 * @return the visible objects
	 */
	public List<WorldObject> getVisibleObjects(WorldObject object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final WorldRegion region = object.getWorldRegion();
		if (region == null)
		{
			return new ArrayList<>();
		}
		
		final int x = object.getX();
		final int y = object.getY();
		final int sqRadius = radius * radius;
		
		// Create a list in order to contain all visible WorldObject
		final List<WorldObject> result = new ArrayList<>();
		
		// Go through the list of region
		for (WorldRegion worldRegion : region.getSurroundingRegions())
		{
			// Go through visible objects of the selected region
			for (WorldObject wo : worldRegion.getVisibleObjects())
			{
				if (wo == null)
				{
					continue;
				}
				
				if (wo.equals(object))
				{
					continue; // skip our own character
				}
				
				final int x1 = wo.getX();
				final int y1 = wo.getY();
				final double dx = x1 - x;
				final double dy = y1 - y;
				
				// If the visible object is inside the circular area add the object to the list result
				if (((dx * dx) + (dy * dy)) < sqRadius)
				{
					result.add(wo);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the WorldRegions in the spheric area (radius) centered on the object.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All visible object are identified in <b>_visibleObjects</b> of their current WorldRegion<br>
	 * All surrounding WorldRegion are identified in <b>_surroundingRegions</b> of the selected WorldRegion in order to scan a large area around a WorldObject<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Define the target list of a skill</li>
	 * <li>Define the target list of a polearme attack</li><br>
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the spheric area
	 * @return the visible objects3 d
	 */
	public List<WorldObject> getVisibleObjects3D(WorldObject object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final int x = object.getX();
		final int y = object.getY();
		final int z = object.getZ();
		final int sqRadius = radius * radius;
		
		// Create a list in order to contain all visible WorldObject
		final List<WorldObject> result = new ArrayList<>();
		
		// Go through visible object of the selected region
		for (WorldRegion worldRegion : object.getWorldRegion().getSurroundingRegions())
		{
			for (WorldObject wo : worldRegion.getVisibleObjects())
			{
				if (wo == null)
				{
					continue;
				}
				
				if (wo.equals(object))
				{
					continue; // skip our own character
				}
				
				final int x1 = wo.getX();
				final int y1 = wo.getY();
				final int z1 = wo.getZ();
				final long dx = x1 - x;
				final long dy = y1 - y;
				final long dz = z1 - z;
				if (((dx * dx) + (dy * dy) + (dz * dz)) < sqRadius)
				{
					result.add(wo);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible players of the WorldRegion object's and of its surrounding WorldRegion.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All visible object are identified in <b>_visibleObjects</b> of their current WorldRegion<br>
	 * All surrounding WorldRegion are identified in <b>_surroundingRegions</b> of the selected WorldRegion in order to scan a large area around a WorldObject<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Find Close Objects for Creature</li><br>
	 * @param object WorldObject that determine the current WorldRegion
	 * @return the visible playable
	 */
	public List<PlayerInstance> getVisiblePlayers(WorldObject object)
	{
		final WorldRegion region = object.getWorldRegion();
		if (region == null)
		{
			return null;
		}
		
		// Create a list in order to contain all visible WorldObject
		final List<PlayerInstance> result = new ArrayList<>();
		
		// Go through the list of region
		for (WorldRegion worldRegion : region.getSurroundingRegions())
		{
			// Go through visible object of the selected region
			for (PlayerInstance playable : worldRegion.getAllPlayers())
			{
				if (playable == null)
				{
					continue;
				}
				
				if (playable.equals(object))
				{
					continue; // skip our own character
				}
				
				if (!playable.isVisible())
				{
					continue; // skip dying objects
				}
				
				result.add(playable);
			}
		}
		
		return result;
	}
	
	/**
	 * Calculate the current WorldRegions of the object according to its position (x,y).<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Set position of a new WorldObject (drop, spawn...)</li>
	 * <li>Update position of a WorldObject after a mouvement</li><br>
	 * @param location the point
	 * @return the region
	 */
	public WorldRegion getRegion(Location location)
	{
		return _worldRegions[(location.getX() >> SHIFT_BY) + OFFSET_X][(location.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Gets the region.
	 * @param x the x
	 * @param y the y
	 * @return the region
	 */
	public WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Returns the whole 2d array containing the world regions used by ZoneData.java to setup zones inside the world regions
	 * @return the all world regions
	 */
	public WorldRegion[][] getAllWorldRegions()
	{
		return _worldRegions;
	}
	
	/**
	 * Init each WorldRegion and their surrounding table.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All surrounding WorldRegion are identified in <b>_surroundingRegions</b> of the selected WorldRegion in order to scan a large area around a WorldObject<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Constructor of World</li>
	 */
	private void initRegions()
	{
		LOGGER.info("World: Setting up World Regions");
		_worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				_worldRegions[x][y] = new WorldRegion(x, y);
			}
		}
		
		// Set surrounding regions.
		for (int rx = 0; rx <= REGIONS_X; rx++)
		{
			for (int ry = 0; ry <= REGIONS_Y; ry++)
			{
				final List<WorldRegion> surroundingRegions = new ArrayList<>();
				for (int sx = rx - 1; sx <= (rx + 1); sx++)
				{
					for (int sy = ry - 1; sy <= (ry + 1); sy++)
					{
						if (((sx >= 0) && (sx < REGIONS_X) && (sy >= 0) && (sy < REGIONS_Y)))
						{
							surroundingRegions.add(_worldRegions[sx][sy]);
						}
					}
				}
				WorldRegion[] regionArray = new WorldRegion[surroundingRegions.size()];
				regionArray = surroundingRegions.toArray(regionArray);
				_worldRegions[rx][ry].setSurroundingRegions(regionArray);
			}
		}
		
		LOGGER.info("World: (" + REGIONS_X + "x" + REGIONS_Y + ") World Region Grid set up.");
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public synchronized void deleteVisibleNpcSpawns()
	{
		LOGGER.info("Deleting all visible NPCs.");
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}
		LOGGER.info("All visible NPCs deleted.");
	}
	
	/**
	 * Gets the account players.
	 * @param account the account_name
	 * @return the account players
	 */
	public List<PlayerInstance> getAccountPlayers(String account)
	{
		final List<PlayerInstance> result = new ArrayList<>();
		for (PlayerInstance actual : _allPlayers.values())
		{
			if (actual.getAccountName().equals(account))
			{
				result.add(actual);
			}
		}
		return result;
	}
	
	public static World getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final World INSTANCE = new World();
	}
}
