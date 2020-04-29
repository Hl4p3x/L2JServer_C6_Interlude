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
package org.l2jserver.gameserver.model.actor;

import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.knownlist.PlayableKnownList;
import org.l2jserver.gameserver.model.actor.stat.PlayableStat;
import org.l2jserver.gameserver.model.actor.status.PlayableStatus;
import org.l2jserver.gameserver.model.actor.templates.CreatureTemplate;

/**
 * This class represents all Playable characters in the world.<br>
 * Playable:
 * <ul>
 * <li>PlayerInstance</li>
 * <li>Summon</li>
 * </ul>
 */
public abstract class Playable extends Creature
{
	private boolean _isNoblesseBlessed = false; // for Noblesse Blessing skill, restores buffs after death
	private boolean _getCharmOfLuck = false; // Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	private boolean _isPhoenixBlessed = false; // for Soul of The Phoenix or Salvation buffs
	private boolean _ProtectionBlessing = false;
	
	/**
	 * Constructor of PlayableInstance (use Creature constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to create an empty _skills slot and link copy basic Calculator set to this PlayableInstance</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template The CreatureTemplate to apply to the PlayableInstance
	 */
	public Playable(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
	}
	
	@Override
	public PlayableKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof PlayableKnownList))
		{
			setKnownList(new PlayableKnownList(this));
		}
		return (PlayableKnownList) super.getKnownList();
	}
	
	@Override
	public PlayableStat getStat()
	{
		if (!(super.getStat() instanceof PlayableStat))
		{
			setStat(new PlayableStat(this));
		}
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		if (!(super.getStatus() instanceof PlayableStatus))
		{
			setStatus(new PlayableStatus(this));
		}
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (killer != null)
		{
			PlayerInstance player = null;
			if (killer instanceof PlayerInstance)
			{
				player = (PlayerInstance) killer;
			}
			else if (killer instanceof Summon)
			{
				player = ((Summon) killer).getOwner();
			}
			
			if (player != null)
			{
				player.onKillUpdatePvPKarma(this);
			}
		}
		return true;
	}
	
	/**
	 * Check if pvp.
	 * @param target the target
	 * @return true, if successful
	 */
	public boolean checkIfPvP(Creature target)
	{
		if (target == null)
		{
			return false; // Target is null
		}
		if (target == this)
		{
			return false; // Target is self
		}
		if (!(target instanceof Playable))
		{
			return false; // Target is not a PlayableInstance
		}
		
		PlayerInstance player = null;
		if (this instanceof PlayerInstance)
		{
			player = (PlayerInstance) this;
		}
		else if (this instanceof Summon)
		{
			player = ((Summon) this).getOwner();
		}
		
		if (player == null)
		{
			return false; // Active player is null
		}
		if (player.getKarma() != 0)
		{
			return false; // Active player has karma
		}
		
		PlayerInstance targetPlayer = null;
		if (target instanceof PlayerInstance)
		{
			targetPlayer = (PlayerInstance) target;
		}
		else if (target instanceof Summon)
		{
			targetPlayer = ((Summon) target).getOwner();
		}
		
		if (targetPlayer == null)
		{
			return false; // Target player is null
		}
		if (targetPlayer == this)
		{
			return false; // Target player is self
		}
		if (targetPlayer.getKarma() != 0)
		{
			return false; // Target player has karma
		}
		if (targetPlayer.getPvpFlag() == 0)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Return True.
	 * @return true, if is attackable
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	private Effect _lastNoblessEffect = null;
	
	// Support for Noblesse Blessing skill, where buffs are retained after resurrect
	/**
	 * Checks if is noblesse blessed.
	 * @return true, if is noblesse blessed
	 */
	public boolean isNoblesseBlessed()
	{
		return _isNoblesseBlessed;
	}
	
	/**
	 * Sets the checks if is noblesse blessed.
	 * @param value the new checks if is noblesse blessed
	 */
	public void setNoblesseBlessed(boolean value)
	{
		_isNoblesseBlessed = value;
	}
	
	/**
	 * Start noblesse blessing.
	 * @param effect
	 */
	public void startNoblesseBlessing(Effect effect)
	{
		_lastNoblessEffect = effect;
		setNoblesseBlessed(true);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop noblesse blessing.
	 * @param effect the effect
	 */
	public void stopNoblesseBlessing(Effect effect)
	{
		// to avoid multiple buffs effects removal
		if ((effect != null) && (_lastNoblessEffect != effect))
		{
			return;
		}
		
		if (effect == null)
		{
			stopEffects(Effect.EffectType.NOBLESSE_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}
		
		setNoblesseBlessed(false);
		updateAbnormalEffect();
		_lastNoblessEffect = null;
	}
	
	private Effect _lastProtectionBlessingEffect = null;
	
	// for Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	/**
	 * Gets the protection blessing.
	 * @return the protection blessing
	 */
	public boolean getProtectionBlessing()
	{
		return _ProtectionBlessing;
	}
	
	/**
	 * Sets the protection blessing.
	 * @param value the new protection blessing
	 */
	public void setProtectionBlessing(boolean value)
	{
		_ProtectionBlessing = value;
	}
	
	/**
	 * Start protection blessing.
	 * @param effect
	 */
	public void startProtectionBlessing(Effect effect)
	{
		_lastProtectionBlessingEffect = effect;
		setProtectionBlessing(true);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop protection blessing.
	 * @param effect the effect
	 */
	public void stopProtectionBlessing(Effect effect)
	{
		if ((effect != null) && (_lastProtectionBlessingEffect != effect))
		{
			return;
		}
		
		if (effect == null)
		{
			stopEffects(Effect.EffectType.PROTECTION_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}
		
		setProtectionBlessing(false);
		updateAbnormalEffect();
		_lastProtectionBlessingEffect = null;
	}
	
	private Effect _lastPhoenixBlessedEffect = null;
	
	// Support for Soul of the Phoenix and Salvation skills
	/**
	 * Checks if is phoenix blessed.
	 * @return true, if is phoenix blessed
	 */
	public boolean isPhoenixBlessed()
	{
		return _isPhoenixBlessed;
	}
	
	/**
	 * Sets the checks if is phoenix blessed.
	 * @param value the new checks if is phoenix blessed
	 */
	public void setPhoenixBlessed(boolean value)
	{
		_isPhoenixBlessed = value;
	}
	
	/**
	 * Start phoenix blessing.
	 * @param effect
	 */
	public void startPhoenixBlessing(Effect effect)
	{
		_lastPhoenixBlessedEffect = effect;
		setPhoenixBlessed(true);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop phoenix blessing.
	 * @param effect the effect
	 */
	public void stopPhoenixBlessing(Effect effect)
	{
		if ((effect != null) && (_lastPhoenixBlessedEffect != effect))
		{
			return;
		}
		
		if (effect == null)
		{
			stopEffects(Effect.EffectType.PHOENIX_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}
		
		setPhoenixBlessed(false);
		updateAbnormalEffect();
		_lastPhoenixBlessedEffect = null;
	}
	
	/**
	 * Destroy item by item id.
	 * @param process the process
	 * @param itemId the item id
	 * @param count the count
	 * @param reference the reference
	 * @param sendMessage the send message
	 * @return true, if successful
	 */
	public abstract boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage);
	
	/**
	 * Destroy item.
	 * @param process the process
	 * @param objectId the object id
	 * @param count the count
	 * @param reference the reference
	 * @param sendMessage the send message
	 * @return true, if successful
	 */
	public abstract boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage);
	
	// Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	private Effect _lastCharmOfLuckEffect = null;
	
	/**
	 * Gets the charm of luck.
	 * @return the charm of luck
	 */
	public boolean getCharmOfLuck()
	{
		return _getCharmOfLuck;
	}
	
	/**
	 * Sets the charm of luck.
	 * @param value the new charm of luck
	 */
	public void setCharmOfLuck(boolean value)
	{
		_getCharmOfLuck = value;
	}
	
	/**
	 * Start charm of luck.
	 * @param effect
	 */
	public void startCharmOfLuck(Effect effect)
	{
		setCharmOfLuck(true);
		updateAbnormalEffect();
		_lastCharmOfLuckEffect = effect;
	}
	
	/**
	 * Stop charm of luck.
	 * @param effect the effect
	 */
	public void stopCharmOfLuck(Effect effect)
	{
		if ((effect != null) && (_lastCharmOfLuckEffect != effect))
		{
			return;
		}
		
		if (effect == null)
		{
			stopEffects(Effect.EffectType.CHARM_OF_LUCK);
		}
		else
		{
			removeEffect(effect);
		}
		
		setCharmOfLuck(false);
		updateAbnormalEffect();
		_lastCharmOfLuckEffect = null;
	}
	
	/**
	 * Checks if is in fun event.
	 * @return true, if is in fun event
	 */
	public boolean isInFunEvent()
	{
		final PlayerInstance player = getActingPlayer();
		return (player != null) && player.isInFunEvent();
	}
	
	/**
	 * Gets the acting player.
	 * @return the acting player
	 */
	@Override
	public PlayerInstance getActingPlayer()
	{
		if (this instanceof PlayerInstance)
		{
			return (PlayerInstance) this;
		}
		return null;
	}
	
	@Override
	public boolean isPlayable()
	{
		return true;
	}
}
