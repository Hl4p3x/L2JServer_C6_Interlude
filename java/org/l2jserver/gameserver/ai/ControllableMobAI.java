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

import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.MobGroupTable;
import org.l2jserver.gameserver.model.MobGroup;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Creature.AIAccessor;
import org.l2jserver.gameserver.model.actor.instance.ControllableMobInstance;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.FolkInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.util.Util;

/**
 * @author littlecrow AI for controllable mobs
 */
public class ControllableMobAI extends AttackableAI
{
	public static final int AI_IDLE = 1;
	public static final int AI_NORMAL = 2;
	public static final int AI_FORCEATTACK = 3;
	public static final int AI_FOLLOW = 4;
	public static final int AI_CAST = 5;
	public static final int AI_ATTACK_GROUP = 6;
	
	private int _alternateAI;
	
	private boolean _isThinking; // to prevent thinking recursively
	private boolean _isNotMoving;
	
	private Creature _forcedTarget;
	private MobGroup _targetGroup;
	
	protected void thinkFollow()
	{
		final Attackable me = (Attackable) _actor;
		if (!Util.checkIfInRange(MobGroupTable.FOLLOW_RANGE, me, getForcedTarget(), true))
		{
			final int signX = Rnd.nextBoolean() ? -1 : 1;
			final int signY = Rnd.nextBoolean() ? -1 : 1;
			final int randX = Rnd.get(MobGroupTable.FOLLOW_RANGE);
			final int randY = Rnd.get(MobGroupTable.FOLLOW_RANGE);
			moveTo(getForcedTarget().getX() + (signX * randX), getForcedTarget().getY() + (signY * randY), getForcedTarget().getZ());
		}
	}
	
	@Override
	public void onEvtThink()
	{
		if (_isThinking || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		setThinking(true);
		
		try
		{
			switch (_alternateAI)
			{
				case AI_IDLE:
				{
					if (getIntention() != AI_INTENTION_ACTIVE)
					{
						setIntention(AI_INTENTION_ACTIVE);
					}
					break;
				}
				case AI_FOLLOW:
				{
					thinkFollow();
					break;
				}
				case AI_CAST:
				{
					thinkCast();
					break;
				}
				case AI_FORCEATTACK:
				{
					thinkForceAttack();
					break;
				}
				case AI_ATTACK_GROUP:
				{
					thinkAttackGroup();
					break;
				}
				default:
				{
					if (getIntention() == AI_INTENTION_ACTIVE)
					{
						thinkActive();
					}
					else if (getIntention() == AI_INTENTION_ATTACK)
					{
						thinkAttack();
					}
					break;
				}
			}
		}
		finally
		{
			setThinking(false);
		}
	}
	
	protected void thinkCast()
	{
		if ((getAttackTarget() == null) || getAttackTarget().isAlikeDead())
		{
			setAttackTarget(findNextRndTarget());
			clientStopMoving(null);
		}
		
		if (getAttackTarget() == null)
		{
			return;
		}
		
		((Attackable) _actor).setTarget(getAttackTarget());
		if (!_actor.isMuted())
		{
			// check distant skills
			int maxRange = 0;
			for (Skill sk : _actor.getAllSkills())
			{
				if (Util.checkIfInRange(sk.getCastRange(), _actor, getAttackTarget(), true) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_accessor.doCast(sk);
					return;
				}
				maxRange = Math.max(maxRange, sk.getCastRange());
			}
			
			if (!_isNotMoving)
			{
				moveToPawn(getAttackTarget(), maxRange);
			}
		}
	}
	
	protected void thinkAttackGroup()
	{
		final Creature target = getForcedTarget();
		if ((target == null) || target.isAlikeDead())
		{
			// try to get next group target
			setForcedTarget(findNextGroupTarget());
			clientStopMoving(null);
		}
		
		if (target == null)
		{
			return;
		}
		
		_actor.setTarget(target);
		// as a response, we put the target in a forced attack mode
		final ControllableMobInstance theTarget = (ControllableMobInstance) target;
		final ControllableMobAI ctrlAi = (ControllableMobAI) theTarget.getAI();
		ctrlAi.forceAttack(_actor);
		
		final Skill[] skills = _actor.getAllSkills();
		final double dist2 = _actor.getPlanDistanceSq(target.getX(), target.getY());
		final int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius();
		int maxRange = range;
		if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
		{
			// check distant skills
			for (Skill sk : skills)
			{
				final int castRange = sk.getCastRange();
				if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_accessor.doCast(sk);
					return;
				}
				maxRange = Math.max(maxRange, castRange);
			}
			
			if (!_isNotMoving)
			{
				moveToPawn(target, range);
			}
			return;
		}
		_accessor.doAttack(target);
	}
	
	protected void thinkForceAttack()
	{
		if ((getForcedTarget() == null) || getForcedTarget().isAlikeDead())
		{
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			setAlternateAI(AI_IDLE);
		}
		
		_actor.setTarget(getForcedTarget());
		final Skill[] skills = _actor.getAllSkills();
		final double dist2 = _actor.getPlanDistanceSq(getForcedTarget().getX(), getForcedTarget().getY());
		final int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + getForcedTarget().getTemplate().getCollisionRadius();
		int maxRange = range;
		if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
		{
			// check distant skills
			for (Skill sk : skills)
			{
				final int castRange = sk.getCastRange();
				if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_accessor.doCast(sk);
					return;
				}
				maxRange = Math.max(maxRange, castRange);
			}
			
			if (!_isNotMoving)
			{
				moveToPawn(getForcedTarget(), _actor.getPhysicalAttackRange()/* range */);
			}
			return;
		}
		_accessor.doAttack(getForcedTarget());
	}
	
	protected void thinkAttack()
	{
		if ((getAttackTarget() == null) || getAttackTarget().isAlikeDead())
		{
			if (getAttackTarget() != null)
			{
				// stop hating
				final Attackable npc = (Attackable) _actor;
				npc.stopHating(getAttackTarget());
			}
			
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
		{
			// notify aggression
			if (((NpcInstance) _actor).getFactionId() != null)
			{
				for (WorldObject obj : _actor.getKnownList().getKnownObjects().values())
				{
					if (!(obj instanceof NpcInstance))
					{
						continue;
					}
					
					final NpcInstance npc = (NpcInstance) obj;
					final String factionId = ((NpcInstance) _actor).getFactionId();
					if (!factionId.equalsIgnoreCase(npc.getFactionId()))
					{
						continue;
					}
					
					if (_actor.isInsideRadius(npc, npc.getFactionRange(), false, true) && (Math.abs(getAttackTarget().getZ() - npc.getZ()) < 200))
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
					}
				}
			}
			
			_actor.setTarget(getAttackTarget());
			final Skill[] skills = _actor.getAllSkills();
			final double dist2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
			final int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
			int maxRange = range;
			if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
			{
				// check distant skills
				for (Skill sk : skills)
				{
					final int castRange = sk.getCastRange();
					if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
					{
						_accessor.doCast(sk);
						return;
					}
					maxRange = Math.max(maxRange, castRange);
				}
				moveToPawn(getAttackTarget(), range);
				return;
			}
			
			// Force mobs to attack anybody if confused.
			Creature hated;
			if (_actor.isConfused())
			{
				hated = findNextRndTarget();
			}
			else
			{
				hated = getAttackTarget();
			}
			
			if (hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE);
				return;
			}
			
			if (hated != getAttackTarget())
			{
				setAttackTarget(hated);
			}
			
			if (!_actor.isMuted() && (skills.length > 0) && (Rnd.get(5) == 3))
			{
				for (Skill sk : skills)
				{
					final int castRange = sk.getCastRange();
					if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)))
					{
						_accessor.doCast(sk);
						return;
					}
				}
			}
			_accessor.doAttack(getAttackTarget());
		}
	}
	
	private void thinkActive()
	{
		setAttackTarget(findNextRndTarget());
		Creature hated;
		if (_actor.isConfused())
		{
			hated = findNextRndTarget();
		}
		else
		{
			hated = getAttackTarget();
		}
		
		if (hated != null)
		{
			_actor.setRunning();
			setIntention(AI_INTENTION_ATTACK, hated);
		}
	}
	
	private boolean autoAttackCondition(Creature target)
	{
		if ((target == null) || !(_actor instanceof Attackable))
		{
			return false;
		}
		
		final Attackable me = (Attackable) _actor;
		if ((target instanceof FolkInstance) || (target instanceof DoorInstance))
		{
			return false;
		}
		
		if (target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || (Math.abs(_actor.getZ() - target.getZ()) > 100))
		{
			return false;
		}
		
		// Check if the target isn't invulnerable
		if (target.isInvul())
		{
			return false;
		}
		
		// Check if the target is a PlayerInstance and if the target isn't in silent move mode
		if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isSilentMoving())
		{
			return false;
		}
		
		if (target instanceof NpcInstance)
		{
			return false;
		}
		
		return me.isAggressive();
	}
	
	private Creature findNextRndTarget()
	{
		final int aggroRange = ((Attackable) _actor).getAggroRange();
		final Attackable npc = (Attackable) _actor;
		int npcX;
		int npcY;
		int targetX;
		int targetY;
		double dy;
		double dx;
		final double dblAggroRange = aggroRange * aggroRange;
		final List<Creature> potentialTarget = new ArrayList<>();
		for (WorldObject obj : npc.getKnownList().getKnownObjects().values())
		{
			if (!(obj instanceof Creature))
			{
				continue;
			}
			
			npcX = npc.getX();
			npcY = npc.getY();
			targetX = obj.getX();
			targetY = obj.getY();
			dx = npcX - targetX;
			dy = npcY - targetY;
			if (((dx * dx) + (dy * dy)) > dblAggroRange)
			{
				continue;
			}
			
			final Creature target = (Creature) obj;
			if (autoAttackCondition(target))
			{
				potentialTarget.add(target);
			}
		}
		
		if (potentialTarget.isEmpty())
		{
			return null;
		}
		
		// we choose a random target
		final int choice = Rnd.get(potentialTarget.size());
		return potentialTarget.get(choice);
	}
	
	private ControllableMobInstance findNextGroupTarget()
	{
		return getGroupTarget().getRandomMob();
	}
	
	public ControllableMobAI(AIAccessor accessor)
	{
		super(accessor);
		setAlternateAI(AI_IDLE);
	}
	
	public int getAlternateAI()
	{
		return _alternateAI;
	}
	
	public void setAlternateAI(int alternateAI)
	{
		_alternateAI = alternateAI;
	}
	
	public void forceAttack(Creature target)
	{
		setAlternateAI(AI_FORCEATTACK);
		setForcedTarget(target);
	}
	
	public void forceAttackGroup(MobGroup group)
	{
		setForcedTarget(null);
		setGroupTarget(group);
		setAlternateAI(AI_ATTACK_GROUP);
	}
	
	public void stop()
	{
		setAlternateAI(AI_IDLE);
		clientStopMoving(null);
	}
	
	public void move(int x, int y, int z)
	{
		moveTo(x, y, z);
	}
	
	public void follow(Creature target)
	{
		setAlternateAI(AI_FOLLOW);
		setForcedTarget(target);
	}
	
	public boolean isThinking()
	{
		return _isThinking;
	}
	
	public boolean isNotMoving()
	{
		return _isNotMoving;
	}
	
	public void setNotMoving(boolean isNotMoving)
	{
		_isNotMoving = isNotMoving;
	}
	
	public void setThinking(boolean isThinking)
	{
		_isThinking = isThinking;
	}
	
	private synchronized Creature getForcedTarget()
	{
		return _forcedTarget;
	}
	
	private synchronized MobGroup getGroupTarget()
	{
		return _targetGroup;
	}
	
	private synchronized void setForcedTarget(Creature forcedTarget)
	{
		_forcedTarget = forcedTarget;
	}
	
	private synchronized void setGroupTarget(MobGroup targetGroup)
	{
		_targetGroup = targetGroup;
	}
}
