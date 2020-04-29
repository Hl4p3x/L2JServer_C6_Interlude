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

import org.l2jserver.gameserver.ai.AttackableAI;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldRegion;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;

/**
 * This class manages all Minions. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class MinionInstance extends MonsterInstance
{
	private MonsterInstance _master;
	
	/**
	 * Constructor of MinionInstance (use Creature and NpcInstance constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to set the _template of the MinionInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the MinionInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public MinionInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/**
	 * Return True if the Creature is minion of RaidBoss.
	 * @return true, if is raid
	 */
	@Override
	public boolean isRaid()
	{
		return _master instanceof RaidBossInstance;
	}
	
	/**
	 * Return the master of this MinionInstance.
	 * @return the leader
	 */
	public MonsterInstance getLeader()
	{
		return _master;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Notify Leader that Minion has Spawned
		_master.notifyMinionSpawned(this);
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		final WorldRegion region = World.getInstance().getRegion(getX(), getY());
		if ((region != null) && !region.isActive())
		{
			((AttackableAI) getAI()).stopAITask();
		}
	}
	
	/**
	 * Set the master of this MinionInstance.
	 * @param leader The Creature that leads this MinionInstance
	 */
	public void setLeader(MonsterInstance leader)
	{
		_master = leader;
	}
	
	/**
	 * Manages the doDie event for this MinionInstance.
	 * @param killer The Creature that killed this MinionInstance.<br>
	 * @return true, if successful
	 */
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		_master.notifyMinionDied(this);
		return true;
	}
	
	@Override
	public boolean isMinion()
	{
		return true;
	}
}
