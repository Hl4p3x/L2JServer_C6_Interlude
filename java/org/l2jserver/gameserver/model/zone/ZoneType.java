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
package org.l2jserver.gameserver.model.zone;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;

/**
 * Abstract base class for any zone type Handles basic operations
 * @author durgus
 */
public abstract class ZoneType
{
	private final int _id;
	protected ZoneForm _zone;
	private final Map<Integer, Creature> _characterList = new ConcurrentHashMap<>();
	
	/** Parameters to affect specific characters */
	private boolean _checkAffected;
	
	private int _minLvl;
	private int _maxLvl;
	private int[] _race;
	private int[] _class;
	private char _classType;
	
	protected ZoneType(int id)
	{
		_id = id;
		_checkAffected = false;
		_minLvl = 0;
		_maxLvl = 0xFF;
		_classType = 0;
		_race = null;
		_class = null;
	}
	
	public int getId()
	{
		return _id;
	}
	
	/**
	 * Setup new parameters for this zone
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		_checkAffected = true;
		
		// Minimum level
		switch (name)
		{
			case "affectedLvlMin":
			{
				_minLvl = Integer.parseInt(value);
				break;
			}
			// Maximum level
			case "affectedLvlMax":
			{
				_maxLvl = Integer.parseInt(value);
				break;
			}
			// Affected Races
			case "affectedRace":
			{
				// Create a new array holding the affected race
				if (_race == null)
				{
					_race = new int[1];
					_race[0] = Integer.parseInt(value);
				}
				else
				{
					final int[] temp = new int[_race.length + 1];
					int i = 0;
					for (; i < _race.length; i++)
					{
						temp[i] = _race[i];
					}
					temp[i] = Integer.parseInt(value);
					_race = temp;
				}
				break;
			}
			// Affected classes
			case "affectedClassId":
			{
				// Create a new array holding the affected classIds
				if (_class == null)
				{
					_class = new int[1];
					_class[0] = Integer.parseInt(value);
				}
				else
				{
					final int[] temp = new int[_class.length + 1];
					int i = 0;
					for (; i < _class.length; i++)
					{
						temp[i] = _class[i];
					}
					temp[i] = Integer.parseInt(value);
					_class = temp;
				}
				break;
			}
			// Affected class type
			case "affectedClassType":
			{
				if (value.equals("Fighter"))
				{
					_classType = 1;
				}
				else
				{
					_classType = 2;
				}
				break;
			}
		}
	}
	
	/**
	 * Checks if the given character is affected by this zone
	 * @param creature
	 * @return
	 */
	private boolean isAffected(Creature creature)
	{
		// Check lvl
		if ((creature.getLevel() < _minLvl) || (creature.getLevel() > _maxLvl))
		{
			return false;
		}
		
		if (creature instanceof PlayerInstance)
		{
			// Check class type
			if (_classType != 0)
			{
				if (((PlayerInstance) creature).isMageClass())
				{
					if (_classType == 1)
					{
						return false;
					}
				}
				else if (_classType == 2)
				{
					return false;
				}
			}
			
			// Check race
			if (_race != null)
			{
				boolean ok = false;
				for (int element : _race)
				{
					if (((PlayerInstance) creature).getRace().ordinal() == element)
					{
						ok = true;
						break;
					}
				}
				
				if (!ok)
				{
					return false;
				}
			}
			
			// Check class
			if (_class != null)
			{
				boolean ok = false;
				for (int clas : _class)
				{
					if (((PlayerInstance) creature).getClassId().ordinal() == clas)
					{
						ok = true;
						break;
					}
				}
				
				if (!ok)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Set the zone for this ZoneType Instance
	 * @param zone
	 */
	public void setZone(ZoneForm zone)
	{
		_zone = zone;
	}
	
	/**
	 * Returns this zones zone form
	 * @return
	 */
	public ZoneForm getZone()
	{
		return _zone;
	}
	
	/**
	 * Checks if the given coordinates are within the zone
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Checks if the given object is inside the zone.
	 * @param object
	 * @return
	 */
	public boolean isInsideZone(WorldObject object)
	{
		return _zone.isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public double getDistanceToZone(int x, int y)
	{
		return _zone.getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(WorldObject object)
	{
		return _zone.getDistanceToZone(object.getX(), object.getY());
	}
	
	public void revalidateInZone(Creature creature)
	{
		// If the character can't be affected by this zone return
		if (_checkAffected && !isAffected(creature))
		{
			return;
		}
		
		// If the object is inside the zone...
		if (_zone.isInsideZone(creature.getX(), creature.getY(), creature.getZ()))
		{
			// Was the character not yet inside this zone?
			if (!_characterList.containsKey(creature.getObjectId()))
			{
				_characterList.put(creature.getObjectId(), creature);
				onEnter(creature);
			}
		}
		// Was the character inside this zone?
		else if (_characterList.containsKey(creature.getObjectId()))
		{
			_characterList.remove(creature.getObjectId());
			onExit(creature);
		}
	}
	
	/**
	 * Force fully removes a character from the zone Should use during teleport / logoff
	 * @param creature
	 */
	public void removeCharacter(Creature creature)
	{
		if (_characterList.containsKey(creature.getObjectId()))
		{
			_characterList.remove(creature.getObjectId());
			onExit(creature);
		}
	}
	
	/**
	 * Will scan the zones char list for the character
	 * @param creature
	 * @return
	 */
	public boolean isCharacterInZone(Creature creature)
	{
		// re validate zone is not always performed, so better both checks
		if (creature != null)
		{
			return _characterList.containsKey(creature.getObjectId()) || isInsideZone(creature.getX(), creature.getY(), creature.getZ());
		}
		return false;
	}
	
	protected abstract void onEnter(Creature creature);
	
	protected abstract void onExit(Creature creature);
	
	protected abstract void onDieInside(Creature creature);
	
	protected abstract void onReviveInside(Creature creature);
	
	/**
	 * Broadcasts packet to all players inside the zone
	 * @param packet
	 */
	public void broadcastPacket(GameServerPacket packet)
	{
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (Creature creature : _characterList.values())
		{
			if (creature instanceof PlayerInstance)
			{
				creature.sendPacket(packet);
			}
		}
	}
	
	public Collection<Creature> getCharactersInside()
	{
		return _characterList.values();
	}
	
	public void visualizeZone(int z)
	{
		getZone().visualizeZone(_id, z);
	}
}
