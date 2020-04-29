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

import java.util.logging.Logger;

import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

public class ObjectPosition
{
	private static final Logger LOGGER = Logger.getLogger(ObjectPosition.class.getName());
	
	private final WorldObject _activeObject;
	private int _heading = 0;
	private Location _worldPosition;
	private WorldRegion _worldRegion; // Object localization : Used for items/chars that are seen in the world
	private Boolean _changingRegion = false;
	
	/**
	 * Instantiates a new object position.
	 * @param activeObject the active object
	 */
	public ObjectPosition(WorldObject activeObject)
	{
		_activeObject = activeObject;
		setWorldRegion(World.getInstance().getRegion(getWorldPosition()));
	}
	
	/**
	 * Set the x,y,z position of the WorldObject and if necessary modify its _worldRegion.<br>
	 * <br>
	 * <b><u>Assert</u>:</b><br>
	 * <li>_worldRegion != null</li><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Update position during and after movement, or after teleport</li><br>
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setXYZ(int x, int y, int z)
	{
		setWorldPosition(x, y, z);
		
		try
		{
			if (World.getInstance().getRegion(getWorldPosition()) != getWorldRegion())
			{
				updateWorldRegion();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Object Id at bad coords: (x: " + getWorldPosition().getX() + ", y: " + getWorldPosition().getY() + ", z: " + getWorldPosition().getZ() + ").");
			if (_activeObject instanceof PlayerInstance)
			{
				((PlayerInstance) _activeObject).teleToLocation(0, 0, 0, false);
				((PlayerInstance) _activeObject).sendMessage("Error with your coords, Please ask a GM for help!");
			}
			else if (_activeObject instanceof Creature)
			{
				_activeObject.decayMe();
			}
		}
	}
	
	/**
	 * Set the x,y,z position of the WorldObject and make it invisible.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * A WorldObject is invisble if <b>_hidden</b>=true or <b>_worldregion</b>==null<br>
	 * <br>
	 * <b><u>Assert</u>:</b><br>
	 * <li>_worldregion==null <i>(WorldObject is invisible)</i></li><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Create a Door</li>
	 * <li>Restore PlayerInstance</li><br>
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setXYZInvisible(int x, int y, int z)
	{
		if (x > World.MAP_MAX_X)
		{
			x = World.MAP_MAX_X - 5000;
		}
		
		if (x < World.MAP_MIN_X)
		{
			x = World.MAP_MIN_X + 5000;
		}
		
		if (y > World.MAP_MAX_Y)
		{
			y = World.MAP_MAX_Y - 5000;
		}
		
		if (y < World.MAP_MIN_Y)
		{
			y = World.MAP_MIN_Y + 5000;
		}
		
		setWorldPosition(x, y, z);
		_activeObject.setVisible(false);
	}
	
	/**
	 * checks if current object changed its region, if so, update referencies.
	 */
	public void updateWorldRegion()
	{
		if (!_activeObject.isVisible())
		{
			return;
		}
		
		final WorldRegion newRegion = World.getInstance().getRegion(getWorldPosition());
		if (newRegion != getWorldRegion())
		{
			getWorldRegion().removeVisibleObject(_activeObject);
			
			setWorldRegion(newRegion);
			
			// Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its WorldRegion
			getWorldRegion().addVisibleObject(_activeObject);
		}
	}
	
	/**
	 * Gets the active object.
	 * @return the active object
	 */
	public WorldObject getActiveObject()
	{
		return _activeObject;
	}
	
	/**
	 * Gets the heading.
	 * @return the heading
	 */
	public int getHeading()
	{
		return _heading;
	}
	
	/**
	 * Sets the heading.
	 * @param value the new heading
	 */
	public void setHeading(int value)
	{
		_heading = value;
	}
	
	/**
	 * Return the x position of the WorldObject.
	 * @return the x
	 */
	public int getX()
	{
		return getWorldPosition().getX();
	}
	
	/**
	 * Sets the x.
	 * @param value the new x
	 */
	public void setX(int value)
	{
		getWorldPosition().setX(value);
	}
	
	/**
	 * Return the y position of the WorldObject.
	 * @return the y
	 */
	public int getY()
	{
		return getWorldPosition().getY();
	}
	
	/**
	 * Sets the y.
	 * @param value the new y
	 */
	public void setY(int value)
	{
		getWorldPosition().setY(value);
	}
	
	/**
	 * Return the z position of the WorldObject.
	 * @return the z
	 */
	public int getZ()
	{
		return getWorldPosition().getZ();
	}
	
	/**
	 * Sets the z.
	 * @param value the new z
	 */
	public void setZ(int value)
	{
		getWorldPosition().setZ(value);
	}
	
	/**
	 * Gets the world position.
	 * @return the world position
	 */
	public Location getWorldPosition()
	{
		if (_worldPosition == null)
		{
			_worldPosition = new Location(0, 0, 0);
		}
		return _worldPosition;
	}
	
	/**
	 * Sets the world position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setWorldPosition(int x, int y, int z)
	{
		getWorldPosition().setXYZ(x, y, z);
	}
	
	/**
	 * Sets the world position.
	 * @param location the new world position
	 */
	public void setWorldPosition(Location location)
	{
		setWorldPosition(location.getX(), location.getY(), location.getZ());
	}
	
	/**
	 * Gets the world region.
	 * @return the world region
	 */
	public WorldRegion getWorldRegion()
	{
		synchronized (_changingRegion)
		{
			_changingRegion = false;
			return _worldRegion;
		}
	}
	
	/**
	 * Sets the world region.
	 * @param value the new world region
	 */
	public void setWorldRegion(WorldRegion value)
	{
		synchronized (_changingRegion)
		{
			_changingRegion = true;
			_worldRegion = value;
		}
	}
}
