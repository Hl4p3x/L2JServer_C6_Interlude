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
package org.l2jserver.gameserver.model.actor.status;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.instancemanager.DuelManager;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.actor.stat.CreatureStat;
import org.l2jserver.gameserver.model.entity.Duel;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;

public class CreatureStatus
{
	protected static final Logger LOGGER = Logger.getLogger(CreatureStatus.class.getName());
	
	final Creature _creature;
	double _currentCp = 0; // Current CP of the Creature
	double _currentHp = 0; // Current HP of the Creature
	double _currentMp = 0; // Current MP of the Creature
	private Set<Creature> _StatusListener;
	private Future<?> _regTask;
	private byte _flagsRegenActive = 0;
	private static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;
	
	/**
	 * Instantiates a new char status.
	 * @param creature the creature
	 */
	public CreatureStatus(Creature creature)
	{
		_creature = creature;
	}
	
	/**
	 * Add the object to the list of Creature that must be informed of HP/MP updates of this Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Each Creature owns a list called <b>_statusListener</b> that contains all PlayerInstance to inform of HP/MP updates. Players who must be informed are players that target this Creature. When a RegenTask is in progress sever just need to go through this list to send Server->Client packet
	 * StatusUpdate.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Target a PC or NPC</li><br>
	 * @param object Creature to add to the listener
	 */
	public void addStatusListener(Creature object)
	{
		if (object == _creature)
		{
			return;
		}
		
		synchronized (getStatusListener())
		{
			getStatusListener().add(object);
		}
	}
	
	/**
	 * Reduce cp.
	 * @param value the value
	 */
	public void reduceCp(int value)
	{
		if (_currentCp > value)
		{
			setCurrentCp(_currentCp - value);
		}
		else
		{
			setCurrentCp(0);
		}
	}
	
	/**
	 * Reduce the current HP of the Creature and launch the doDie Task if necessary.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>Attackable : Update the attacker AggroInfo of the Attackable _aggroList</li><br>
	 * @param value the value
	 * @param attacker The Creature who attacks
	 */
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	/**
	 * Reduce hp.
	 * @param value the value
	 * @param attacker the attacker
	 * @param awake the awake
	 */
	public void reduceHp(double value, Creature attacker, boolean awake)
	{
		if (_creature.isInvul())
		{
			return;
		}
		
		if (_creature instanceof PlayerInstance)
		{
			if (((PlayerInstance) _creature).isInDuel())
			{
				// the duel is finishing - players do not recive damage
				if (((PlayerInstance) _creature).getDuelState() == Duel.DUELSTATE_DEAD)
				{
					return;
				}
				else if (((PlayerInstance) _creature).getDuelState() == Duel.DUELSTATE_WINNER)
				{
					return;
				}
				
				// cancel duel if player got hit by another player, that is not part of the duel or a monster
				if (!(attacker instanceof SummonInstance) && (!(attacker instanceof PlayerInstance) || (((PlayerInstance) attacker).getDuelId() != ((PlayerInstance) _creature).getDuelId())))
				{
					((PlayerInstance) _creature).setDuelState(Duel.DUELSTATE_INTERRUPTED);
				}
			}
			if (_creature.isDead() && !_creature.isFakeDeath())
			{
				return; // Disabled == null check so skills like Body to Mind work again untill another solution is found
			}
		}
		else
		{
			if (_creature.isDead())
			{
				return; // Disabled == null check so skills like Body to Mind work again untill another solution is found
			}
			
			if ((attacker instanceof PlayerInstance) && ((PlayerInstance) attacker).isInDuel() && (!(_creature instanceof SummonInstance) || (((SummonInstance) _creature).getOwner().getDuelId() != ((PlayerInstance) attacker).getDuelId()))) // Duelling player attacks mob
			{
				((PlayerInstance) attacker).setDuelState(Duel.DUELSTATE_INTERRUPTED);
			}
		}
		
		if (awake && _creature.isSleeping())
		{
			_creature.stopSleeping(null);
		}
		
		if (awake && _creature.isImmobileUntilAttacked())
		{
			_creature.stopImmobileUntilAttacked(null);
		}
		
		if (_creature.isStunned() && (Rnd.get(10) == 0))
		{
			_creature.stopStunning(null);
		}
		
		// Add attackers to npc's attacker list
		if (_creature instanceof NpcInstance)
		{
			_creature.addAttackerToAttackByList(attacker);
		}
		
		if (value > 0) // Reduce Hp if any
		{
			// If we're dealing with an Attackable Instance and the attacker hit it with an over-hit enabled skill, set the over-hit values.
			// Anything else, clear the over-hit flag
			if (_creature instanceof Attackable)
			{
				if (((Attackable) _creature).isOverhit())
				{
					((Attackable) _creature).setOverhitValues(attacker, value);
				}
				else
				{
					((Attackable) _creature).overhitEnabled(false);
				}
			}
			
			value = _currentHp - value; // Get diff of Hp vs value
			if (value <= 0)
			{
				// is the dieing one a duelist? if so change his duel state to dead
				if ((_creature instanceof PlayerInstance) && ((PlayerInstance) _creature).isInDuel())
				{
					_creature.disableAllSkills();
					stopHpMpRegeneration();
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendPacket(ActionFailed.STATIC_PACKET);
					
					// let the DuelManager know of his defeat
					DuelManager.getInstance().onPlayerDefeat((PlayerInstance) getActiveChar());
					value = 1;
				}
				else
				{
					// Set value to 0 if Hp < 0
					value = 0;
				}
			}
			setCurrentHp(value); // Set Hp
		}
		else if (_creature instanceof Attackable) // If we're dealing with an Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag.
		{
			((Attackable) _creature).overhitEnabled(false);
		}
		
		if (_creature.isDead())
		{
			_creature.abortAttack();
			_creature.abortCast();
			
			if ((_creature instanceof PlayerInstance) && ((PlayerInstance) _creature).isInOlympiadMode())
			{
				stopHpMpRegeneration();
				return;
			}
			
			// Start the doDie process
			_creature.doDie(attacker);
			
			// now reset currentHp to zero
			setCurrentHp(0);
		}
		else // If we're dealing with an Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
		if (_creature instanceof Attackable)
		{
			((Attackable) _creature).overhitEnabled(false);
		}
	}
	
	/**
	 * Reduce mp.
	 * @param value the value
	 */
	public void reduceMp(double value)
	{
		value = _currentMp - value;
		if (value < 0)
		{
			value = 0;
		}
		
		setCurrentMp(value);
	}
	
	/**
	 * Remove the object from the list of Creature that must be informed of HP/MP updates of this Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Each Creature owns a list called <b>_statusListener</b> that contains all PlayerInstance to inform of HP/MP updates. Players who must be informed are players that target this Creature. When a RegenTask is in progress sever just need to go through this list to send Server->Client packet
	 * StatusUpdate.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Untarget a PC or NPC</li><br>
	 * @param object Creature to add to the listener
	 */
	public void removeStatusListener(Creature object)
	{
		synchronized (getStatusListener())
		{
			getStatusListener().remove(object);
		}
	}
	
	/**
	 * Start the HP/MP/CP Regeneration task.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Calculate the regen task period</li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority</li>
	 */
	public synchronized void startHpMpRegeneration()
	{
		if ((_regTask == null) && !_creature.isDead())
		{
			// Get the Regeneration periode
			final int period = Formulas.getRegeneratePeriod(_creature);
			
			// Create the HP/MP/CP Regeneration task
			_regTask = ThreadPool.scheduleAtFixedRate(new RegenTask(), period, period);
		}
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the RegenActive flag to False</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 */
	public synchronized void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			// Stop the HP/MP/CP Regeneration task
			_regTask.cancel(false);
			_regTask = null;
			
			// Set the RegenActive flag to false
			_flagsRegenActive = 0;
		}
	}
	
	/**
	 * Gets the active char.
	 * @return the active char
	 */
	public Creature getActiveChar()
	{
		return _creature;
	}
	
	/**
	 * Gets the current cp.
	 * @return the current cp
	 */
	public double getCurrentCp()
	{
		return _currentCp;
	}
	
	/**
	 * Sets the current cp direct.
	 * @param newCp the new current cp direct
	 */
	public void setCurrentCpDirect(double newCp)
	{
		setCurrentCp(newCp, true, true);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new current cp
	 */
	public void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true, false);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new cp
	 * @param broadcastPacket the broadcast packet
	 */
	public void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		setCurrentCp(newCp, broadcastPacket, false);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new cp
	 * @param broadcastPacket the broadcast packet
	 * @param direct the direct
	 */
	public void setCurrentCp(double newCp, boolean broadcastPacket, boolean direct)
	{
		synchronized (this)
		{
			// Get the Max CP of the Creature
			final int maxCp = _creature.getStat().getMaxCp();
			if (newCp < 0)
			{
				newCp = 0;
			}
			
			if ((newCp >= maxCp) && !direct)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform
		if (broadcastPacket)
		{
			_creature.broadcastStatusUpdate();
		}
	}
	
	/**
	 * Gets the current hp.
	 * @return the current hp
	 */
	public double getCurrentHp()
	{
		return _currentHp;
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new current hp
	 */
	public void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}
	
	/**
	 * Sets the current hp direct.
	 * @param newHp the new current hp direct
	 */
	public void setCurrentHpDirect(double newHp)
	{
		setCurrentHp(newHp, true, true);
	}
	
	/**
	 * Sets the current mp direct.
	 * @param newMp the new current mp direct
	 */
	public void setCurrentMpDirect(double newMp)
	{
		setCurrentMp(newMp, true, true);
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new hp
	 * @param broadcastPacket the broadcast packet
	 */
	public void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		setCurrentHp(newHp, true, false);
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new hp
	 * @param broadcastPacket the broadcast packet
	 * @param direct the direct
	 */
	public void setCurrentHp(double newHp, boolean broadcastPacket, boolean direct)
	{
		synchronized (this)
		{
			// Get the Max HP of the Creature
			final double maxHp = _creature.getStat().getMaxHp();
			if ((newHp >= maxHp) && !direct)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;
				_creature.setKilledAlready(false);
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;
				if (!_creature.isDead())
				{
					_creature.setKilledAlready(false);
				}
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform
		if (broadcastPacket)
		{
			_creature.broadcastStatusUpdate();
		}
	}
	
	/**
	 * Sets the current hp mp.
	 * @param newHp the new hp
	 * @param newMp the new mp
	 */
	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp, false, false);
		setCurrentMp(newMp, true, false); // send the StatusUpdate only once
	}
	
	/**
	 * Gets the current mp.
	 * @return the current mp
	 */
	public double getCurrentMp()
	{
		return _currentMp;
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new current mp
	 */
	public void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new mp
	 * @param broadcastPacket the broadcast packet
	 */
	public void setCurrentMp(double newMp, boolean broadcastPacket)
	{
		setCurrentMp(newMp, broadcastPacket, false);
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new mp
	 * @param broadcastPacket the broadcast packet
	 * @param direct the direct
	 */
	public void setCurrentMp(double newMp, boolean broadcastPacket, boolean direct)
	{
		synchronized (this)
		{
			// Get the Max MP of the Creature
			final int maxMp = _creature.getStat().getMaxMp();
			if ((newMp >= maxMp) && !direct)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform
		if (broadcastPacket)
		{
			_creature.broadcastStatusUpdate();
		}
	}
	
	/**
	 * Return the list of Creature that must be informed of HP/MP updates of this Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Each Creature owns a list called <b>_statusListener</b> that contains all PlayerInstance to inform of HP/MP updates. Players who must be informed are players that target this Creature. When a RegenTask is in progress sever just need to go through this list to send Server->Client packet
	 * StatusUpdate.
	 * @return The list of Creature to inform or null if empty
	 */
	public Set<Creature> getStatusListener()
	{
		if (_StatusListener == null)
		{
			_StatusListener = new CopyOnWriteArraySet<>();
		}
		return _StatusListener;
	}
	
	/**
	 * Task of HP/MP/CP regeneration.
	 */
	class RegenTask implements Runnable
	{
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				final CreatureStat charstat = _creature.getStat();
				
				// Modify the current CP of the Creature and broadcast Server->Client packet StatusUpdate
				if (_currentCp < charstat.getMaxCp())
				{
					setCurrentCp(_currentCp + Formulas.calcCpRegen(_creature), false);
				}
				
				// Modify the current HP of the Creature and broadcast Server->Client packet StatusUpdate
				if (_currentHp < charstat.getMaxHp())
				{
					setCurrentHp(_currentHp + Formulas.calcHpRegen(_creature), false);
				}
				
				// Modify the current MP of the Creature and broadcast Server->Client packet StatusUpdate
				if (_currentMp < charstat.getMaxMp())
				{
					setCurrentMp(_currentMp + Formulas.calcMpRegen(_creature), false);
				}
				
				if (!_creature.isInActiveRegion())
				{
					// no broadcast necessary for characters that are in inactive regions.
					// stop regeneration for characters who are filled up and in an inactive region.
					if ((_currentCp == charstat.getMaxCp()) && (_currentHp == charstat.getMaxHp()) && (_currentMp == charstat.getMaxMp()))
					{
						stopHpMpRegeneration();
					}
				}
				else
				{
					_creature.broadcastStatusUpdate(); // send the StatusUpdate packet
				}
			}
			catch (Throwable e)
			{
				LOGGER.warning("RegenTask failed for " + _creature.getName() + " " + e);
			}
		}
	}
}
