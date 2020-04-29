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

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.CommanderInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author programmos
 */
public class CommanderKnownList extends AttackableKnownList
{
	public CommanderKnownList(CommanderInstance activeChar)
	{
		super(activeChar);
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
		
		if (getActiveChar().getHomeX() == 0)
		{
			getActiveChar().getHomeLocation();
		}
		
		// Check if siege is in progress
		if ((getActiveChar().getFort() != null) && getActiveChar().getFort().getSiege().isInProgress())
		{
			PlayerInstance player = null;
			if (object instanceof PlayerInstance)
			{
				player = (PlayerInstance) object;
			}
			else if (object instanceof Summon)
			{
				player = ((Summon) object).getOwner();
			}
			
			// Check if player is not the defender
			if ((player != null) && ((player.getClan() == null) || (getActiveChar().getFort().getSiege().getAttackerClan(player.getClan()) != null)) && (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		
		return true;
	}
	
	@Override
	public CommanderInstance getActiveChar()
	{
		return (CommanderInstance) super.getActiveChar();
	}
}
