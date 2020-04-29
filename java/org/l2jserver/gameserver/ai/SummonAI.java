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

import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;

import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Creature.AIAccessor;
import org.l2jserver.gameserver.model.actor.Summon;

public class SummonAI extends CreatureAI
{
	private boolean _thinking; // to prevent recursive thinking
	
	public SummonAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		final Summon summon = (Summon) _actor;
		if (summon.getFollowStatus())
		{
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}
	
	private void thinkAttack()
	{
		final Summon summon = (Summon) _actor;
		WorldObject target = null;
		target = summon.getTarget();
		
		// Like L2OFF if the target is dead the summon must go back to his owner
		if ((target != null) && ((Creature) target).isDead())
		{
			summon.setFollowStatus(true);
		}
		
		if (checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange()))
		{
			return;
		}
		
		clientStopMoving(null);
		_accessor.doAttack(getAttackTarget());
	}
	
	private void thinkCast()
	{
		final Summon summon = (Summon) _actor;
		final Creature target = getCastTarget();
		if (checkTargetLost(target))
		{
			setCastTarget(null);
		}
		
		final Skill skill = getSkill();
		if (maybeMoveToPawn(target, _actor.getMagicalAttackRange(skill)))
		{
			return;
		}
		
		clientStopMoving(null);
		summon.setFollowStatus(false);
		setIntention(AI_INTENTION_IDLE);
		_accessor.doCast(skill);
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled())
		{
			return;
		}
		
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
		((Summon.AIAccessor) _accessor).doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled())
		{
			return;
		}
		
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	public void onEvtThink()
	{
		if (_thinking || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		_thinking = true;
		
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if (getIntention() == AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if (getIntention() == AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if (getIntention() == AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		super.onEvtFinishCasting();
		
		final Summon summon = (Summon) _actor;
		WorldObject target = null;
		target = summon.getTarget();
		if (target == null)
		{
			return;
		}
		
		if (summon.getAI().getIntention() != AI_INTENTION_ATTACK)
		{
			summon.setFollowStatus(true);
		}
		else if (((Creature) target).isDead())
		{
			summon.setFollowStatus(true);
		}
	}
}