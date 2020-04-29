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
package org.l2jserver.gameserver.ai;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.FortSiegeGuardInstance;
import org.l2jserver.gameserver.model.actor.instance.SiegeGuardInstance;

/**
 * @author mkizub
 */
public class DoorAI extends CreatureAI
{
	public DoorAI(DoorInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	// rather stupid AI... well, it's for doors :D
	@Override
	protected void onIntentionIdle()
	{
		// null;
	}
	
	@Override
	protected void onIntentionActive()
	{
		// null;
	}
	
	@Override
	protected void onIntentionRest()
	{
		// null;
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionCast(Skill skill, WorldObject target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionMoveTo(Location destination)
	{
		// null;
	}
	
	@Override
	protected void onIntentionFollow(Creature target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionPickUp(WorldObject item)
	{
		// null;
	}
	
	@Override
	protected void onIntentionInteract(WorldObject object)
	{
		// null;
	}
	
	@Override
	public void onEvtThink()
	{
		// null;
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		final DoorInstance me = (DoorInstance) _actor;
		ThreadPool.execute(new onEventAttackedDoorTask(me, attacker));
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		// null;
	}
	
	@Override
	protected void onEvtStunned(Creature attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtSleeping(Creature attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtRooted(Creature attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		// null;
	}
	
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// null;
	}
	
	@Override
	protected void onEvtArrived()
	{
		// null;
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// null;
	}
	
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		// null;
	}
	
	@Override
	protected void onEvtForgetObject(WorldObject object)
	{
		// null;
	}
	
	@Override
	protected void onEvtCancel()
	{
		// null;
	}
	
	@Override
	protected void onEvtDead()
	{
		// null;
	}
	
	private class onEventAttackedDoorTask implements Runnable
	{
		private final DoorInstance _door;
		private final Creature _attacker;
		
		public onEventAttackedDoorTask(DoorInstance door, Creature attacker)
		{
			_door = door;
			_attacker = attacker;
		}
		
		@Override
		public void run()
		{
			_door.getKnownList().updateKnownObjects();
			
			for (SiegeGuardInstance guard : _door.getKnownSiegeGuards())
			{
				if ((guard != null) && (guard.getAI() != null) && _actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && (Math.abs(_attacker.getZ() - guard.getZ()) < 200))
				{
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, 15);
				}
			}
			for (FortSiegeGuardInstance guard : _door.getKnownFortSiegeGuards())
			{
				if ((guard != null) && (guard.getAI() != null) && _actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && (Math.abs(_attacker.getZ() - guard.getZ()) < 200))
				{
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, 15);
				}
			}
		}
	}
}
