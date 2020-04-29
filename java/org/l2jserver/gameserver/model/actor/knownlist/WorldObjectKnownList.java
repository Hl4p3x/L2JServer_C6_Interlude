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
package org.l2jserver.gameserver.model.actor.knownlist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.BoatInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.util.Util;

public class WorldObjectKnownList
{
	private final WorldObject _activeObject;
	private final Map<Integer, WorldObject> _knownObjects = new ConcurrentHashMap<>();
	
	public WorldObjectKnownList(WorldObject activeObject)
	{
		_activeObject = activeObject;
	}
	
	public boolean addKnownObject(WorldObject object)
	{
		return addKnownObject(object, null);
	}
	
	public boolean addKnownObject(WorldObject object, Creature dropper)
	{
		if ((object == null) || (object == _activeObject))
		{
			return false;
		}
		
		// Check if already know object
		if (knowsObject(object))
		{
			if (!object.isVisible())
			{
				removeKnownObject(object);
			}
			return false;
		}
		
		// Check if object is not inside distance to watch object
		if (!Util.checkIfInRange(getDistanceToWatchObject(object), _activeObject, object, true))
		{
			return false;
		}
		
		return getKnownObjects().put(object.getObjectId(), object) == null;
	}
	
	public boolean knowsObject(WorldObject object)
	{
		if (object == null)
		{
			return false;
		}
		return (_activeObject == object) || getKnownObjects().containsKey(object.getObjectId());
	}
	
	/** Remove all WorldObject from _knownObjects */
	public void removeAllKnownObjects()
	{
		getKnownObjects().clear();
	}
	
	public boolean removeKnownObject(WorldObject object)
	{
		if (object == null)
		{
			return false;
		}
		return getKnownObjects().remove(object.getObjectId()) != null;
	}
	
	/**
	 * Update the _knownObject and _knowPlayers of the Creature and of its already known WorldObject.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove invisible and too far WorldObject from _knowObject and if necessary from _knownPlayers of the Creature</li>
	 * <li>Add visible WorldObject near the Creature to _knowObject and if necessary to _knownPlayers of the Creature</li>
	 * <li>Add Creature to _knowObject and if necessary to _knownPlayers of WorldObject alreday known by the Creature</li>
	 */
	public synchronized void updateKnownObjects()
	{
		// Only bother updating knownobjects for Creature; don't for WorldObject
		if (_activeObject instanceof Creature)
		{
			findCloseObjects();
			forgetObjects();
		}
	}
	
	private final void findCloseObjects()
	{
		if (_activeObject == null)
		{
			return;
		}
		
		if (_activeObject.isPlayable())
		{
			// Go through all visible WorldObject near the Creature
			for (WorldObject object : World.getInstance().getVisibleObjects(_activeObject))
			{
				if (object == null)
				{
					continue;
				}
				
				// Try to add object to active object's known objects
				// PlayableInstance sees everything
				addKnownObject(object);
				
				// Try to add active object to object's known objects
				// Only if object is a Creature and active object is a PlayableInstance
				if (object instanceof Creature)
				{
					object.getKnownList().addKnownObject(_activeObject);
				}
			}
		}
		else
		{
			// Go through all visible WorldObject near the Creature
			for (WorldObject playable : World.getInstance().getVisiblePlayers(_activeObject))
			{
				if (playable == null)
				{
					return;
				}
				
				// Try to add object to active object's known objects
				// Creature only needs to see visible PlayerInstance and PlayableInstance, when moving. Other l2characters are currently only known from initial spawn area.
				// Possibly look into getDistanceToForgetObject values before modifying this approach...
				addKnownObject(playable);
			}
		}
	}
	
	public void forgetObjects()
	{
		// Go through knownObjects
		for (WorldObject object : getKnownObjects().values())
		{
			if (object == null)
			{
				continue;
			}
			
			// Remove all invisible objects
			// Remove all too far objects
			if (!object.isVisible() || !Util.checkIfInRange(getDistanceToForgetObject(object), _activeObject, object, true))
			{
				if ((object instanceof BoatInstance) && (_activeObject instanceof PlayerInstance))
				{
					if (((BoatInstance) object).getVehicleDeparture() == null)
					{
						continue;
					}
					
					if (((PlayerInstance) _activeObject).isInBoat())
					{
						if (((PlayerInstance) _activeObject).getBoat() != object)
						{
							removeKnownObject(object);
						}
					}
					else
					{
						removeKnownObject(object);
					}
				}
				else
				{
					removeKnownObject(object);
				}
			}
		}
	}
	
	public WorldObject getActiveObject()
	{
		return _activeObject;
	}
	
	public int getDistanceToForgetObject(WorldObject object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(WorldObject object)
	{
		return 0;
	}
	
	/**
	 * @return the _knownObjects containing all WorldObject known by the Creature.
	 */
	public Map<Integer, WorldObject> getKnownObjects()
	{
		return _knownObjects;
	}
}
