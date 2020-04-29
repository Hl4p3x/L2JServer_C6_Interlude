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
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.AutoAttackStart;
import org.l2jserver.gameserver.network.serverpackets.AutoAttackStop;
import org.l2jserver.gameserver.network.serverpackets.CharMoveToLocation;
import org.l2jserver.gameserver.network.serverpackets.Die;
import org.l2jserver.gameserver.network.serverpackets.MoveToLocationInVehicle;
import org.l2jserver.gameserver.network.serverpackets.MoveToPawn;
import org.l2jserver.gameserver.network.serverpackets.StopMove;
import org.l2jserver.gameserver.network.serverpackets.StopRotation;
import org.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2jserver.gameserver.taskmanager.CreatureFollowTaskManager;

/**
 * Mother class of all objects AI in the world.<br>
 * <br>
 * AbastractAI:<br>
 * <li>CreatureAI</li>
 */
abstract class AbstractAI implements Ctrl
{
	/** The creature that this AI manages */
	protected final Creature _actor;
	
	/** An accessor for private methods of the actor */
	protected final Creature.AIAccessor _accessor;
	
	/** Current long-term intention */
	private CtrlIntention _intention = AI_INTENTION_IDLE;
	/** Current long-term intention parameter */
	private Object _intentionArg0 = null;
	/** Current long-term intention parameter */
	private Object _intentionArg1 = null;
	
	/** Flags about client's state, in order to know which messages to send */
	protected boolean _clientMoving;
	/** Flags about client's state, in order to know which messages to send */
	protected boolean _clientAutoAttacking;
	/** Flags about client's state, in order to know which messages to send */
	protected int _clientMovingToPawnOffset;
	
	/** Different targets this AI maintains */
	private WorldObject _target;
	private Creature _castTarget;
	private Creature _attackTarget;
	private Creature _followTarget;
	
	/** Diferent internal state flags */
	private int _moveToPawnTimeout;
	
	/**
	 * Constructor of AbstractAI.
	 * @param accessor The AI accessor of the Creature
	 */
	protected AbstractAI(Creature.AIAccessor accessor)
	{
		_accessor = accessor;
		
		// Get the Creature managed by this Accessor AI
		_actor = accessor.getActor();
	}
	
	/**
	 * Return the Creature managed by this Accessor AI.
	 */
	@Override
	public Creature getActor()
	{
		return _actor;
	}
	
	/**
	 * Set the Intention of this AbstractAI.<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method is USED by AI classes</b></font><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <b>AttackableAI</b> : Create an AI Task executed every 1s (if necessary)<br>
	 * <b>L2PlayerAI</b> : Stores the current AI intention parameters to later restore it if necessary
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	public synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention = intention;
		_intentionArg0 = arg0;
		_intentionArg1 = arg1;
	}
	
	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Stop the FOLLOW mode if necessary</b></font>
	 * @param intention The new Intention to set to the AI
	 */
	@Override
	public void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}
	
	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Stop the FOLLOW mode if necessary</b></font>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention (optional target)
	 */
	@Override
	public void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}
	
	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Stop the FOLLOW mode if necessary</b></font>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention (optional target)
	 * @param arg1 The second parameter of the Intention (optional target)
	 */
	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (!_actor.isVisible() || !_actor.hasAI())
		{
			return;
		}
		
		// Stop the follow mode if necessary
		if ((intention != AI_INTENTION_FOLLOW) && (intention != AI_INTENTION_ATTACK))
		{
			stopFollow();
		}
		
		// Launch the onIntention method of the CreatureAI corresponding to the new Intention
		switch (intention)
		{
			case AI_INTENTION_IDLE:
			{
				onIntentionIdle();
				break;
			}
			case AI_INTENTION_ACTIVE:
			{
				onIntentionActive();
				break;
			}
			case AI_INTENTION_REST:
			{
				onIntentionRest();
				break;
			}
			case AI_INTENTION_ATTACK:
			{
				onIntentionAttack((Creature) arg0);
				break;
			}
			case AI_INTENTION_CAST:
			{
				onIntentionCast((Skill) arg0, (WorldObject) arg1);
				break;
			}
			case AI_INTENTION_MOVE_TO:
			{
				onIntentionMoveTo((Location) arg0);
				break;
			}
			case AI_INTENTION_MOVE_TO_IN_A_BOAT:
			{
				onIntentionMoveToInABoat((Location) arg0, (Location) arg1);
				break;
			}
			case AI_INTENTION_FOLLOW:
			{
				onIntentionFollow((Creature) arg0);
				break;
			}
			case AI_INTENTION_PICK_UP:
			{
				onIntentionPickUp((WorldObject) arg0);
				break;
			}
			case AI_INTENTION_INTERACT:
			{
				onIntentionInteract((WorldObject) arg0);
				break;
			}
		}
	}
	
	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<br>
	 * <font color=#FF0000><b><u>Caution</u>: The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</b></font>
	 * @param evt The event whose the AI must be notified
	 */
	@Override
	public void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}
	
	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<br>
	 * <font color=#FF0000><b><u>Caution</u>: The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</b></font>
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 */
	@Override
	public void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}
	
	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<br>
	 * <font color=#FF0000><b><u>Caution</u>: The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</b></font>
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	@Override
	public void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if (!_actor.isVisible() || !_actor.hasAI() || ((_actor instanceof PlayerInstance) && !((PlayerInstance) _actor).isOnline()) || ((_actor instanceof PlayerInstance) && ((PlayerInstance) _actor).isInOfflineMode()))
		{
			return;
		}
		
		switch (evt)
		{
			case EVT_THINK:
			{
				onEvtThink();
				break;
			}
			case EVT_ATTACKED:
			{
				onEvtAttacked((Creature) arg0);
				break;
			}
			case EVT_AGGRESSION:
			{
				onEvtAggression((Creature) arg0, ((Number) arg1).intValue());
				break;
			}
			case EVT_STUNNED:
			{
				onEvtStunned((Creature) arg0);
				break;
			}
			case EVT_SLEEPING:
			{
				onEvtSleeping((Creature) arg0);
				break;
			}
			case EVT_ROOTED:
			{
				onEvtRooted((Creature) arg0);
				break;
			}
			case EVT_CONFUSED:
			{
				onEvtConfused((Creature) arg0);
				break;
			}
			case EVT_MUTED:
			{
				onEvtMuted((Creature) arg0);
				break;
			}
			case EVT_READY_TO_ACT:
			{
				onEvtReadyToAct();
				break;
			}
			case EVT_USER_CMD:
			{
				onEvtUserCmd(arg0, arg1);
				break;
			}
			case EVT_ARRIVED:
			{
				onEvtArrived();
				break;
			}
			case EVT_ARRIVED_REVALIDATE:
			{
				onEvtArrivedRevalidate();
				break;
			}
			case EVT_ARRIVED_BLOCKED:
			{
				onEvtArrivedBlocked((Location) arg0);
				break;
			}
			case EVT_FORGET_OBJECT:
			{
				onEvtForgetObject((WorldObject) arg0);
				break;
			}
			case EVT_CANCEL:
			{
				onEvtCancel();
				break;
			}
			case EVT_DEAD:
			{
				onEvtDead();
				break;
			}
			case EVT_FAKE_DEATH:
			{
				onEvtFakeDeath();
				break;
			}
			case EVT_FINISH_CASTING:
			{
				onEvtFinishCasting();
				break;
			}
		}
	}
	
	protected abstract void onIntentionIdle();
	
	protected abstract void onIntentionActive();
	
	protected abstract void onIntentionRest();
	
	protected abstract void onIntentionAttack(Creature target);
	
	protected abstract void onIntentionCast(Skill skill, WorldObject target);
	
	protected abstract void onIntentionMoveTo(Location destination);
	
	protected abstract void onIntentionMoveToInABoat(Location destination, Location origin);
	
	protected abstract void onIntentionFollow(Creature target);
	
	protected abstract void onIntentionPickUp(WorldObject item);
	
	protected abstract void onIntentionInteract(WorldObject object);
	
	protected abstract void onEvtThink();
	
	protected abstract void onEvtAttacked(Creature attacker);
	
	protected abstract void onEvtAggression(Creature target, int aggro);
	
	protected abstract void onEvtStunned(Creature attacker);
	
	protected abstract void onEvtSleeping(Creature attacker);
	
	protected abstract void onEvtRooted(Creature attacker);
	
	protected abstract void onEvtConfused(Creature attacker);
	
	protected abstract void onEvtMuted(Creature attacker);
	
	protected abstract void onEvtReadyToAct();
	
	protected abstract void onEvtUserCmd(Object arg0, Object arg1);
	
	protected abstract void onEvtArrived();
	
	protected abstract void onEvtArrivedRevalidate();
	
	protected abstract void onEvtArrivedBlocked(Location location);
	
	protected abstract void onEvtForgetObject(WorldObject object);
	
	protected abstract void onEvtCancel();
	
	protected abstract void onEvtDead();
	
	protected abstract void onEvtFakeDeath();
	
	protected abstract void onEvtFinishCasting();
	
	/**
	 * Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 */
	protected void clientActionFailed()
	{
		if (_actor instanceof PlayerInstance)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn <i>(broadcast)</i>.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 * @param pawn
	 * @param offset
	 */
	public void moveToPawn(WorldObject pawn, int offset)
	{
		// Check if actor can move
		if (!_actor.isMovementDisabled())
		{
			if (offset < 10)
			{
				offset = 10;
			}
			
			// prevent possible extra calls to this function (there is none?),
			// also don't send movetopawn packets too often
			boolean sendPacket = true;
			if (_clientMoving && (getTarget() == pawn))
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (GameTimeController.getGameTicks() < _moveToPawnTimeout)
					{
						return;
					}
					
					sendPacket = false;
				}
				else if (_actor.isOnGeodataPath())
				{
					// minimum time to calculate new route is 2 seconds
					if (GameTimeController.getGameTicks() < (_moveToPawnTimeout + 10))
					{
						return;
					}
				}
			}
			
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			
			setTarget(pawn);
			
			_moveToPawnTimeout = GameTimeController.getGameTicks();
			_moveToPawnTimeout += /* 1000 */ 200 / GameTimeController.MILLIS_IN_TICK;
			
			if ((pawn == null) || (_accessor == null))
			{
				return;
			}
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			// Mobius: Solves moving to wrong Z when not using geodata,
			// but probably is not accurate and you should use geodata.
			// _accessor.moveTo(pawn.getX(), pawn.getY(), _actor.getZ(), offset);
			
			if (!_actor.isMoving())
			{
				_actor.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Send a Server->Client packet MoveToPawn/CharMoveToLocation to the actor and all PlayerInstance in its _knownPlayers
			if (pawn instanceof Creature)
			{
				if (_actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new CharMoveToLocation(_actor));
					_clientMovingToPawnOffset = 0;
				}
				else if (sendPacket)
				{
					_actor.broadcastPacket(new MoveToPawn(_actor, (Creature) pawn, offset));
				}
			}
			else
			{
				_actor.broadcastPacket(new CharMoveToLocation(_actor));
			}
		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation <i>(broadcast)</i>.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 * @param x
	 * @param y
	 * @param z
	 */
	public void moveTo(int x, int y, int z)
	{
		// Check if actor can move
		if (!_actor.isMovementDisabled())
		{
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(x, y, z);
			
			// Send a Server->Client packet CharMoveToLocation to the actor and all PlayerInstance in its _knownPlayers
			_actor.broadcastPacket(new CharMoveToLocation(_actor));
		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	protected void moveToInABoat(Location destination, Location origin)
	{
		// Chek if actor can move
		if (!_actor.isMovementDisabled())
		{
			// Send a Server->Client packet CharMoveToLocation to the actor and all PlayerInstance in its _knownPlayers
			// CharMoveToLocation msg = new CharMoveToLocation(_actor);
			if (((PlayerInstance) _actor).getBoat() != null)
			{
				_actor.broadcastPacket(new MoveToLocationInVehicle(_actor, destination, origin));
			}
		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation <i>(broadcast)</i>.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 * @param pos
	 */
	protected void clientStopMoving(Location pos)
	{
		// Stop movement of the Creature
		if (_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}
		
		_clientMovingToPawnOffset = 0;
		
		if (_clientMoving || (pos != null))
		{
			_clientMoving = false;
			
			// Send a Server->Client packet StopMove to the actor and all PlayerInstance in its _knownPlayers
			_actor.broadcastPacket(new StopMove(_actor));
			
			if (pos != null)
			{
				// Send a Server->Client packet StopRotation to the actor and all PlayerInstance in its _knownPlayers
				final StopRotation sr = new StopRotation(_actor, pos.getHeading(), 0);
				_actor.sendPacket(sr);
				_actor.broadcastPacket(sr);
			}
		}
	}
	
	// Client has already arrived to target, no need to force StopMove packet
	protected void clientStoppedMoving()
	{
		if (_clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			_clientMovingToPawnOffset = 0;
			_actor.broadcastPacket(new StopMove(_actor));
		}
		_clientMoving = false;
	}
	
	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <i>(broadcast)</i>.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 */
	public void clientStartAutoAttack()
	{
		if ((((_actor instanceof NpcInstance) && !(_actor instanceof Attackable)) && !(_actor instanceof Playable)))
		{
			return;
		}
		
		if (_actor instanceof Summon)
		{
			final Summon summon = (Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}
		if (!_clientAutoAttacking)
		{
			if ((_actor instanceof PlayerInstance) && (((PlayerInstance) _actor).getPet() != null))
			{
				((PlayerInstance) _actor).getPet().broadcastPacket(new AutoAttackStart(((PlayerInstance) _actor).getPet().getObjectId()));
			}
			// Send a Server->Client packet AutoAttackStart to the actor and all PlayerInstance in its _knownPlayers
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}
	
	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <i>(broadcast)</i>.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 */
	public void clientStopAutoAttack()
	{
		if (_actor instanceof Summon)
		{
			final Summon summon = (Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		
		final boolean isAutoAttacking = _clientAutoAttacking;
		
		if (_actor instanceof PlayerInstance)
		{
			if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor) && isAutoAttacking)
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
			}
		}
		else if (isAutoAttacking)
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			setAutoAttacking(false);
		}
	}
	
	/**
	 * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die <i>(broadcast)</i>.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 */
	protected void clientNotifyDead()
	{
		// Send a Server->Client packet Die to the actor and all PlayerInstance in its _knownPlayers
		_actor.broadcastPacket(new Die(_actor));
		
		// Init AI
		setIntention(AI_INTENTION_IDLE);
		setTarget(null);
		setAttackTarget(null);
		setCastTarget(null);
		
		// Cancel the follow task if necessary
		stopFollow();
	}
	
	/**
	 * Update the state of this actor client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the PlayerInstance player.<br>
	 * <font color=#FF0000><b><u>Caution</u>: Low level function, used by AI subclasses</b></font>
	 * @param player The PlayerIstance to notify with state of this Creature
	 */
	public void describeStateToPlayer(PlayerInstance player)
	{
		if (_clientMoving)
		{
			final Creature follow = getFollowTarget();
			if ((_clientMovingToPawnOffset != 0) && (follow != null))
			{
				// Send a Server->Client packet MoveToPawn to the actor and all PlayerInstance in its _knownPlayers
				player.sendPacket(new MoveToPawn(_actor, follow, _clientMovingToPawnOffset));
			}
			else
			{
				// Send a Server->Client packet CharMoveToLocation to the actor and all PlayerInstance in its _knownPlayers
				player.sendPacket(new CharMoveToLocation(_actor));
			}
		}
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 1s.
	 * @param target The Creature to follow
	 */
	public synchronized void startFollow(Creature target)
	{
		startFollow(target, -1);
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.
	 * @param target The Creature to follow
	 * @param range
	 */
	public synchronized void startFollow(Creature target, int range)
	{
		stopFollow();
		_followTarget = target;
		if (range == -1)
		{
			CreatureFollowTaskManager.getInstance().addNormalFollow(_actor, range);
		}
		else
		{
			CreatureFollowTaskManager.getInstance().addAttackFollow(_actor, range);
		}
	}
	
	/**
	 * Stop an AI Follow Task.
	 */
	public synchronized void stopFollow()
	{
		CreatureFollowTaskManager.getInstance().remove(_actor);
		_followTarget = null;
	}
	
	public synchronized Creature getFollowTarget()
	{
		return _followTarget;
	}
	
	protected synchronized WorldObject getTarget()
	{
		return _target;
	}
	
	protected synchronized void setTarget(WorldObject target)
	{
		_target = target;
	}
	
	protected synchronized void setCastTarget(Creature target)
	{
		_castTarget = target;
	}
	
	/**
	 * @return the current cast target.
	 */
	public synchronized Creature getCastTarget()
	{
		return _castTarget;
	}
	
	protected synchronized void setAttackTarget(Creature target)
	{
		_attackTarget = target;
	}
	
	/**
	 * Return current attack target.
	 */
	@Override
	public synchronized Creature getAttackTarget()
	{
		return _attackTarget;
	}
	
	public synchronized boolean isAutoAttacking()
	{
		return _clientAutoAttacking;
	}
	
	public synchronized void setAutoAttacking(boolean isAutoAttacking)
	{
		_clientAutoAttacking = isAutoAttacking;
	}
	
	/**
	 * @return the _intentionArg0
	 */
	public synchronized Object getIntentionArg0()
	{
		return _intentionArg0;
	}
	
	/**
	 * @param intentionArg0 the _intentionArg0 to set
	 */
	public synchronized void setIntentionArg0(Object intentionArg0)
	{
		_intentionArg0 = intentionArg0;
	}
	
	/**
	 * @return the _intentionArg1
	 */
	public synchronized Object getIntentionArg1()
	{
		return _intentionArg1;
	}
	
	/**
	 * @param intentionArg1 the _intentionArg1 to set
	 */
	public synchronized void setIntentionArg1(Object intentionArg1)
	{
		_intentionArg1 = intentionArg1;
	}
	
	/**
	 * Return the current Intention.
	 */
	@Override
	public synchronized CtrlIntention getIntention()
	{
		return _intention;
	}
}
