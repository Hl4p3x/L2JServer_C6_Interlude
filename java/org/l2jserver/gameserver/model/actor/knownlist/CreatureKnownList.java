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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.util.Util;

public class CreatureKnownList extends WorldObjectKnownList
{
	private Map<Integer, PlayerInstance> _knownPlayers;
	private Map<Integer, Integer> _knownRelations;
	
	public CreatureKnownList(Creature creature)
	{
		super(creature);
	}
	
	@Override
	public boolean addKnownObject(WorldObject object)
	{
		return addKnownObject(object, null);
	}
	
	@Override
	public boolean addKnownObject(WorldObject object, Creature dropper)
	{
		if (!super.addKnownObject(object, dropper))
		{
			return false;
		}
		
		if (object instanceof PlayerInstance)
		{
			getKnownPlayers().put(object.getObjectId(), (PlayerInstance) object);
			getKnownRelations().put(object.getObjectId(), -1);
		}
		return true;
	}
	
	/**
	 * @param player The PlayerInstance to search in _knownPlayer
	 * @return True if the PlayerInstance is in _knownPlayer of the Creature.
	 */
	public boolean knowsThePlayer(PlayerInstance player)
	{
		return (getActiveChar() == player) || getKnownPlayers().containsKey(player.getObjectId());
	}
	
	/**
	 * Remove all WorldObject from _knownObjects and _knownPlayer of the Creature then cancel Attak or Cast and notify AI.
	 */
	@Override
	public void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		getKnownPlayers().clear();
		getKnownRelations().clear();
		
		// Set _target of the Creature to null
		// Cancel Attack or Cast
		getActiveChar().setTarget(null);
		
		// Cancel AI Task
		if (getActiveChar().hasAI())
		{
			getActiveChar().setAI(null);
		}
	}
	
	@Override
	public boolean removeKnownObject(WorldObject object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		if (object instanceof PlayerInstance)
		{
			getKnownPlayers().remove(object.getObjectId());
			getKnownRelations().remove(object.getObjectId());
		}
		// If object is targeted by the Creature, cancel Attack or Cast
		if (object == getActiveChar().getTarget())
		{
			getActiveChar().setTarget(null);
		}
		
		return true;
	}
	
	public Creature getActiveChar()
	{
		return (Creature) super.getActiveObject();
	}
	
	@Override
	public int getDistanceToForgetObject(WorldObject object)
	{
		return 0;
	}
	
	@Override
	public int getDistanceToWatchObject(WorldObject object)
	{
		return 0;
	}
	
	public Collection<Creature> getKnownCharacters()
	{
		final List<Creature> result = new ArrayList<>();
		for (WorldObject obj : getKnownObjects().values())
		{
			if (obj instanceof Creature)
			{
				result.add((Creature) obj);
			}
		}
		return result;
	}
	
	public Collection<Creature> getKnownCharactersInRadius(long radius)
	{
		final List<Creature> result = new ArrayList<>();
		for (WorldObject obj : getKnownObjects().values())
		{
			if (obj instanceof PlayerInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((PlayerInstance) obj);
				}
			}
			else if (obj instanceof MonsterInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((MonsterInstance) obj);
				}
			}
			else if (obj instanceof NpcInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((NpcInstance) obj);
				}
			}
		}
		return result;
	}
	
	public Map<Integer, PlayerInstance> getKnownPlayers()
	{
		if (_knownPlayers == null)
		{
			_knownPlayers = new ConcurrentHashMap<>();
		}
		return _knownPlayers;
	}
	
	public Map<Integer, Integer> getKnownRelations()
	{
		if (_knownRelations == null)
		{
			_knownRelations = new ConcurrentHashMap<>();
		}
		return _knownRelations;
	}
	
	public Collection<PlayerInstance> getKnownPlayersInRadius(long radius)
	{
		final List<PlayerInstance> result = new ArrayList<>();
		for (PlayerInstance player : getKnownPlayers().values())
		{
			if (Util.checkIfInRange((int) radius, getActiveChar(), player, true))
			{
				result.add(player);
			}
		}
		return result;
	}
}
