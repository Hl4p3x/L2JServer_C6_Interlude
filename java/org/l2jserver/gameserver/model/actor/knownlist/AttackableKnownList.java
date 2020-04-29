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

import java.util.Collection;

import org.l2jserver.gameserver.ai.CreatureAI;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.FolkInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

public class AttackableKnownList extends NpcKnownList
{
	public AttackableKnownList(Attackable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean removeKnownObject(WorldObject object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		// Remove the WorldObject from the _aggrolist of the Attackable
		if (object instanceof Creature)
		{
			getActiveChar().getAggroList().remove(object);
		}
		
		// Set the Attackable Intention to AI_INTENTION_IDLE
		final Collection<PlayerInstance> known = getKnownPlayers().values();
		
		// FIXME: This is a temporary solution
		final CreatureAI ai = getActiveChar().getAI();
		if ((ai != null) && ((known == null) || known.isEmpty()))
		{
			ai.setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		return true;
	}
	
	@Override
	public Attackable getActiveChar()
	{
		return (Attackable) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(WorldObject object)
	{
		if ((getActiveChar().getAggroList() != null) && (getActiveChar().getAggroList().get(object) != null))
		{
			return 3000;
		}
		return Math.min(2200, 2 * getDistanceToWatchObject(object));
	}
	
	@Override
	public int getDistanceToWatchObject(WorldObject object)
	{
		if ((object instanceof FolkInstance) || !(object instanceof Creature))
		{
			return 0;
		}
		
		if (object instanceof Playable)
		{
			return 1500;
		}
		
		if (getActiveChar().getAggroRange() > getActiveChar().getFactionRange())
		{
			return getActiveChar().getAggroRange();
		}
		
		if (getActiveChar().getFactionRange() > 200)
		{
			return getActiveChar().getFactionRange();
		}
		
		return 200;
	}
}
