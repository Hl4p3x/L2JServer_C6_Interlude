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
package org.l2jserver.gameserver.model.actor.instance;

import java.util.Map;

import org.l2jserver.gameserver.ai.CreatureAI;
import org.l2jserver.gameserver.ai.NpcWalkerAI;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;

/**
 * This class manages some npcs can walk in the city.<br>
 * It inherits all methods from NpcInstance.<br>
 * <br>
 * @original author Rayan RPG
 * @since 819
 */
public class NpcWalkerInstance extends NpcInstance
{
	/**
	 * Constructor of NpcWalkerInstance (use Creature and NpcInstance constructor).
	 * @param objectId the object id
	 * @param template the template
	 */
	public NpcWalkerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setAI(new NpcWalkerAI(new NpcWalkerAIAccessor()));
	}
	
	/**
	 * AI can't be deattached, npc must move always with the same AI instance.
	 * @param newAI AI to set for this NpcWalkerInstance
	 */
	@Override
	public void setAI(CreatureAI newAI)
	{
		if (_ai == null)
		{
			super.setAI(newAI);
		}
	}
	
	@Override
	public void onSpawn()
	{
		((NpcWalkerAI) getAI()).setHomeX(getX());
		((NpcWalkerAI) getAI()).setHomeY(getY());
		((NpcWalkerAI) getAI()).setHomeZ(getZ());
	}
	
	/**
	 * Sends a chat to all _knowObjects.
	 * @param chat message to say
	 */
	public void broadcastChat(String chat)
	{
		final Map<Integer, PlayerInstance> knownPlayers = getKnownList().getKnownPlayers();
		if (knownPlayers == null)
		{
			return;
		}
		
		// we send message to known players only!
		if (!knownPlayers.isEmpty())
		{
			final CreatureSay cs = new CreatureSay(getObjectId(), ChatType.GENERAL, getName(), chat);
			
			// we interact and list players here
			for (PlayerInstance players : knownPlayers.values())
			{
				// finally send packet :D
				players.sendPacket(cs);
			}
		}
	}
	
	/**
	 * NPCs are immortal.
	 * @param i ignore it
	 * @param attacker ignore it
	 * @param awake ignore it
	 */
	@Override
	public void reduceCurrentHp(double i, Creature attacker, boolean awake)
	{
	}
	
	/**
	 * NPCs are immortal.
	 * @param killer ignore it
	 * @return false
	 */
	@Override
	public boolean doDie(Creature killer)
	{
		return false;
	}
	
	@Override
	public CreatureAI getAI()
	{
		return super.getAI();
	}
	
	/**
	 * The Class NpcWalkerAIAccessor.
	 */
	protected class NpcWalkerAIAccessor extends Creature.AIAccessor
	{
		/**
		 * AI can't be deattached.
		 */
		@Override
		public void detachAI()
		{
		}
	}
}
