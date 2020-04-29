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
package org.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.effects.EffectTemplate;
import org.l2jserver.gameserver.model.skills.funcs.Func;
import org.l2jserver.gameserver.model.skills.funcs.FuncTemplate;
import org.l2jserver.gameserver.model.skills.funcs.Lambda;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import org.l2jserver.gameserver.network.serverpackets.MagicEffectIcons;
import org.l2jserver.gameserver.network.serverpackets.PartySpelled;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public abstract class Effect
{
	static final Logger LOGGER = Logger.getLogger(Effect.class.getName());
	
	public enum EffectState
	{
		CREATED,
		ACTING,
		FINISHING
	}
	
	public enum EffectType
	{
		BUFF,
		DEBUFF,
		CHARGE,
		DMG_OVER_TIME,
		HEAL_OVER_TIME,
		COMBAT_POINT_HEAL_OVER_TIME,
		MANA_DMG_OVER_TIME,
		MANA_HEAL_OVER_TIME,
		MP_CONSUME_PER_LEVEL,
		RELAXING,
		STUN,
		ROOT,
		SLEEP,
		HATE,
		FAKE_DEATH,
		CONFUSION,
		CONFUSE_MOB_ONLY,
		MUTE,
		IMMOBILEUNTILATTACKED,
		FEAR,
		SALVATION,
		SILENT_MOVE,
		SIGNET_EFFECT,
		SIGNET_GROUND,
		SEED,
		PARALYZE,
		STUN_SELF,
		PSYCHICAL_MUTE,
		REMOVE_TARGET,
		TARGET_ME,
		SILENCE_MAGIC_PHYSICAL,
		BETRAY,
		NOBLESSE_BLESSING,
		PHOENIX_BLESSING,
		PETRIFICATION,
		BLUFF,
		BATTLE_FORCE,
		SPELL_FORCE,
		CHARM_OF_LUCK,
		INVINCIBLE,
		PROTECTION_BLESSING,
		INTERRUPT,
		MEDITATION,
		BLOW,
		FUSION,
		CANCEL,
		BLOCK_BUFF,
		BLOCK_DEBUFF,
		PREVENT_BUFF,
		CLAN_GATE,
		NEGATE
	}
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	
	// member _effector is the instance of Creature that cast/used the spell/skill that is causing this effect. Do not confuse with the instance of Creature that is being affected by this effect.
	private final Creature _effector;
	
	// member _effected is the instance of Creature that was affected by this effect. Do not confuse with the instance of Creature that catsed/used this effect.
	protected final Creature _effected;
	
	// the skill that was used.
	public Skill _skill;
	
	// the value of an update
	private final Lambda _lambda;
	
	// the current state
	private EffectState _state;
	
	// period, seconds
	private final int _period;
	private int _periodStartTicks;
	int _periodfirsttime;
	
	// function templates
	private final FuncTemplate[] _funcTemplates;
	
	// initial count
	protected int _totalCount;
	// counter
	private int _count;
	
	// abnormal effect mask
	private final int _abnormalEffect;
	
	public boolean preventExitUpdate;
	
	private boolean _cancelEffect = false;
	
	public class EffectTask implements Runnable
	{
		protected final int _delay;
		protected final int _rate;
		
		EffectTask(int pDelay, int pRate)
		{
			_delay = pDelay;
			_rate = pRate;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_periodfirsttime == 0)
				{
					setPeriodStartTicks(GameTimeController.getGameTicks());
				}
				else
				{
					setPeriodfirsttime(0);
				}
				scheduleEffect();
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	private ScheduledFuture<?> _currentFuture;
	private EffectTask _currentTask;
	
	/** The Identifier of the stack group */
	private final String _stackType;
	
	/** The position of the effect in the stack group */
	private final float _stackOrder;
	
	private final EffectTemplate _template;
	
	private boolean _inUse = false;
	
	protected Effect(Env env, EffectTemplate template)
	{
		_template = template;
		_state = EffectState.CREATED;
		_skill = env.skill;
		// _item = env._item == null ? null : env._item.getItem();
		_effected = env.target;
		_effector = env.player;
		_lambda = template.lambda;
		_funcTemplates = template.funcTemplates;
		_count = template.counter;
		_totalCount = _count;
		int temp = template.period;
		if (env.skillMastery)
		{
			temp *= 2;
		}
		_period = temp;
		_abnormalEffect = template.abnormalEffect;
		_stackType = template.stackType;
		_stackOrder = template.stackOrder;
		_periodStartTicks = GameTimeController.getGameTicks();
		_periodfirsttime = 0;
		scheduleEffect();
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getTotalCount()
	{
		return _totalCount;
	}
	
	public void setCount(int newcount)
	{
		_count = newcount;
	}
	
	public void setFirstTime(int newfirsttime)
	{
		if (_currentFuture != null)
		{
			_periodStartTicks = GameTimeController.getGameTicks() - (newfirsttime * GameTimeController.TICKS_PER_SECOND);
			_currentFuture.cancel(false);
			_currentFuture = null;
			_currentTask = null;
			_periodfirsttime = newfirsttime;
			final int duration = _period - _periodfirsttime;
			_currentTask = new EffectTask(duration * 1000, -1);
			_currentFuture = ThreadPool.schedule(_currentTask, duration * 1000);
		}
	}
	
	public int getPeriod()
	{
		return _period;
	}
	
	public int getTime()
	{
		return (GameTimeController.getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	/**
	 * Returns the elapsed time of the task.
	 * @return Time in seconds.
	 */
	public int getTaskTime()
	{
		if (_count == _totalCount)
		{
			return 0;
		}
		return (Math.abs((_count - _totalCount) + 1) * _period) + getTime() + 1;
	}
	
	public boolean getInUse()
	{
		return _inUse;
	}
	
	public void setInUse(boolean inUse)
	{
		_inUse = inUse;
	}
	
	public String getStackType()
	{
		return _stackType;
	}
	
	public float getStackOrder()
	{
		return _stackOrder;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public Creature getEffector()
	{
		return _effector;
	}
	
	public Creature getEffected()
	{
		return _effected;
	}
	
	public boolean isSelfEffect()
	{
		return _skill._effectTemplatesSelf != null;
	}
	
	public boolean isHerbEffect()
	{
		return _skill.getName().contains("Herb");
	}
	
	public double calc()
	{
		final Env env = new Env();
		env.player = _effector;
		env.target = _effected;
		env.skill = _skill;
		return _lambda.calc(env);
	}
	
	private synchronized void startEffectTask(int duration)
	{
		stopEffectTask();
		_currentTask = new EffectTask(duration, -1);
		_currentFuture = ThreadPool.schedule(_currentTask, duration);
		if (_state == EffectState.ACTING)
		{
			// To avoid possible NPE caused by player crash
			if (_effected != null)
			{
				_effected.addEffect(this);
			}
			else
			{
				LOGGER.warning("Effected is null for skill " + _skill.getId() + " on effect " + getEffectType());
			}
		}
	}
	
	private synchronized void startEffectTaskAtFixedRate(int delay, int rate)
	{
		stopEffectTask();
		_currentTask = new EffectTask(delay, rate);
		_currentFuture = ThreadPool.scheduleAtFixedRate(_currentTask, delay, rate);
		if (_state == EffectState.ACTING)
		{
			_effected.addEffect(this);
		}
	}
	
	/**
	 * Stop the Effect task and send Server->Client update packet.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Cancel the effect in the the abnormal effect map of the Creature</li>
	 * <li>Stop the task of the Effect, remove it and update client magic icone</li>
	 */
	public void exit()
	{
		exit(false, false);
	}
	
	public void exit(boolean cancelEffect)
	{
		exit(false, cancelEffect);
	}
	
	public void exit(boolean preventUpdate, boolean cancelEffect)
	{
		preventExitUpdate = preventUpdate;
		_state = EffectState.FINISHING;
		_cancelEffect = cancelEffect;
		scheduleEffect();
	}
	
	/**
	 * Stop the task of the Effect, remove it and update client magic icone.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Cancel the task</li>
	 * <li>Stop and remove Effect from Creature and update client magic icone</li>
	 */
	public synchronized void stopEffectTask()
	{
		// Cancel the task
		if (_currentFuture != null)
		{
			if (!_currentFuture.isCancelled())
			{
				_currentFuture.cancel(false);
			}
			
			_currentFuture = null;
			_currentTask = null;
			
			// To avoid possible NPE caused by player crash
			if (_effected != null)
			{
				_effected.removeEffect(this);
			}
			else
			{
				LOGGER.warning("Effected is null for skill " + _skill.getId() + " on effect " + getEffectType());
			}
		}
	}
	
	/**
	 * @return effect type
	 */
	public abstract EffectType getEffectType();
	
	/** Notify started */
	public void onStart()
	{
		if (_abnormalEffect != 0)
		{
			_effected.startAbnormalEffect(_abnormalEffect);
		}
	}
	
	/**
	 * Cancel the effect in the the abnormal effect map of the effected Creature.
	 */
	public void onExit()
	{
		if (_abnormalEffect != 0)
		{
			_effected.stopAbnormalEffect(_abnormalEffect);
		}
	}
	
	/**
	 * Return true for continuation of this effect
	 * @return
	 */
	public abstract boolean onActionTime();
	
	public void rescheduleEffect()
	{
		if (_state != EffectState.ACTING)
		{
			scheduleEffect();
		}
		else
		{
			if (_count > 1)
			{
				startEffectTaskAtFixedRate(5, _period * 1000);
				return;
			}
			if (_period > 0)
			{
				startEffectTask(_period * 1000);
			}
		}
	}
	
	public void scheduleEffect()
	{
		if (_state == EffectState.CREATED)
		{
			_state = EffectState.ACTING;
			
			onStart();
			
			if (_skill.isPvpSkill() && (_effected != null) && (_effected instanceof PlayerInstance) && _template.showIcon)
			{
				final SystemMessage smsg = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
				smsg.addString(_skill.getName());
				_effected.sendPacket(smsg);
			}
			
			if (_count > 1)
			{
				startEffectTaskAtFixedRate(5, _period * 1000);
				return;
			}
			if (_period > 0)
			{
				startEffectTask(_period * 1000);
				return;
			}
		}
		
		if (_state == EffectState.ACTING)
		{
			if (_count-- > 0)
			{
				if (_inUse)
				{ // effect has to be in use
					if (onActionTime())
					{
						return; // false causes effect to finish right away
					}
				}
				else if (_count > 0)
				{
					return;
				}
			}
			_state = EffectState.FINISHING;
		}
		
		if (_state == EffectState.FINISHING)
		{
			// Cancel the effect in the the abnormal effect map of the Creature
			onExit();
			
			// If the time left is equal to zero, send the message
			if ((_effected instanceof PlayerInstance) && _template.showIcon && !_effected.isDead())
			{
				// Like L2OFF message S1_HAS_BEEN_ABORTED for toggle skills
				if (_skill.isToggle())
				{
					final SystemMessage smsg3 = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
					smsg3.addString(_skill.getName());
					_effected.sendPacket(smsg3);
				}
				else if (_cancelEffect)
				{
					final SystemMessage smsg3 = new SystemMessage(SystemMessageId.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED);
					smsg3.addString(_skill.getName());
					_effected.sendPacket(smsg3);
				}
				else if (_count == 0)
				{
					final SystemMessage smsg3 = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
					smsg3.addString(_skill.getName());
					_effected.sendPacket(smsg3);
				}
			}
			
			// Stop the task of the Effect, remove it and update client magic icon.
			stopEffectTask();
		}
	}
	
	public Func[] getStatFuncs()
	{
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		final List<Func> funcs = new ArrayList<>();
		for (FuncTemplate t : _funcTemplates)
		{
			final Env env = new Env();
			env.player = _effector;
			env.target = _effected;
			env.skill = _skill;
			final Func f = t.getFunc(env, this); // effect is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public void addIcon(MagicEffectIcons mi)
	{
		final EffectTask task = _currentTask;
		final ScheduledFuture<?> future = _currentFuture;
		if ((task == null) || (future == null))
		{
			return;
		}
		
		if ((_state == EffectState.FINISHING) || (_state == EffectState.CREATED))
		{
			return;
		}
		
		if (!_template.showIcon)
		{
			return;
		}
		
		if (task._rate > 0)
		{
			if (_skill.isPotion())
			{
				mi.addEffect(_skill.getId(), _skill.getLevel(), _skill.getBuffDuration() - (getTaskTime() * 1000), false);
			}
			else if (!_skill.isToggle())
			{
				mi.addEffect(_skill.getId(), _skill.getLevel(), (_count * _period) * 1000, _skill.isDebuff());
			}
			else
			{
				mi.addEffect(_skill.getId(), _skill.getLevel(), -1, true);
			}
		}
		else
		{
			mi.addEffect(_skill.getId(), _skill.getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS) + 1000, _skill.getSkillType() == SkillType.DEBUFF);
		}
	}
	
	public void addPartySpelledIcon(PartySpelled ps)
	{
		final EffectTask task = _currentTask;
		final ScheduledFuture<?> future = _currentFuture;
		if ((task == null) || (future == null))
		{
			return;
		}
		
		if ((_state == EffectState.FINISHING) || (_state == EffectState.CREATED))
		{
			return;
		}
		
		ps.addPartySpelledEffect(_skill.getId(), _skill.getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
	}
	
	public void addOlympiadSpelledIcon(ExOlympiadSpelledInfo os)
	{
		final EffectTask task = _currentTask;
		final ScheduledFuture<?> future = _currentFuture;
		if ((task == null) || (future == null))
		{
			return;
		}
		
		if ((_state == EffectState.FINISHING) || (_state == EffectState.CREATED))
		{
			return;
		}
		
		os.addEffect(_skill.getId(), _skill.getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
	}
	
	public int getLevel()
	{
		return _skill.getLevel();
	}
	
	public int getPeriodfirsttime()
	{
		return _periodfirsttime;
	}
	
	public void setPeriodfirsttime(int periodfirsttime)
	{
		_periodfirsttime = periodfirsttime;
	}
	
	public int getPeriodStartTicks()
	{
		return _periodStartTicks;
	}
	
	public void setPeriodStartTicks(int periodStartTicks)
	{
		_periodStartTicks = periodStartTicks;
	}
	
	public boolean getShowIcon()
	{
		return _template.showIcon;
	}
	
	public EffectState getState()
	{
		return _state;
	}
}
