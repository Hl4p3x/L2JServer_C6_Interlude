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

import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.NpcInfo;
import org.l2jserver.gameserver.network.serverpackets.StopMove;

/**
 * While a tamed beast behaves a lot like a pet (ingame) and does have an owner, in all other aspects, it acts like a mob. In addition, it can be fed in order to increase its duration.<br>
 * This class handles the running tasks, AI, and feed of the mob. The (mostly optional) AI on feeding the spawn is handled by the datapack ai script
 */
public class TamedBeastInstance extends FeedableBeastInstance
{
	private static final int MAX_DISTANCE_FROM_HOME = 30000;
	private static final int MAX_DISTANCE_FROM_OWNER = 2000;
	private static final int MAX_DURATION = 1200000; // 20 minutes
	private static final int DURATION_CHECK_INTERVAL = 60000; // 1 minute
	private static final int DURATION_INCREASE_INTERVAL = 20000; // 20 secs (gained upon feeding)
	private static final int BUFF_INTERVAL = 5000; // 5 seconds
	private int _foodSkillId;
	private int _remainingTime = MAX_DURATION;
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	private PlayerInstance _owner;
	private Future<?> _buffTask = null;
	private Future<?> _durationCheckTask = null;
	
	/**
	 * Instantiates a new tamed beast instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public TamedBeastInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setHome(this);
	}
	
	/**
	 * Instantiates a new tamed beast instance.
	 * @param objectId the object id
	 * @param template the template
	 * @param owner the owner
	 * @param foodSkillId the food skill id
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public TamedBeastInstance(int objectId, NpcTemplate template, PlayerInstance owner, int foodSkillId, int x, int y, int z)
	{
		super(objectId, template);
		
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setOwner(owner);
		setFoodType(foodSkillId);
		setHome(x, y, z);
		spawnMe(x, y, z);
	}
	
	/**
	 * On receive food.
	 */
	public void onReceiveFood()
	{
		// Eating food extends the duration by 20secs, to a max of 20minutes
		_remainingTime += DURATION_INCREASE_INTERVAL;
		if (_remainingTime > MAX_DURATION)
		{
			_remainingTime = MAX_DURATION;
		}
	}
	
	/**
	 * Gets the home.
	 * @return the home
	 */
	public Location getHome()
	{
		return new Location(_homeX, _homeY, _homeZ);
	}
	
	/**
	 * Sets the home.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setHome(int x, int y, int z)
	{
		_homeX = x;
		_homeY = y;
		_homeZ = z;
	}
	
	/**
	 * Sets the home.
	 * @param c the new home
	 */
	public void setHome(Creature c)
	{
		setHome(c.getX(), c.getY(), c.getZ());
	}
	
	/**
	 * Gets the remaining time.
	 * @return the remaining time
	 */
	public int getRemainingTime()
	{
		return _remainingTime;
	}
	
	/**
	 * Sets the remaining time.
	 * @param duration the new remaining time
	 */
	public void setRemainingTime(int duration)
	{
		_remainingTime = duration;
	}
	
	/**
	 * Gets the food type.
	 * @return the food type
	 */
	public int getFoodType()
	{
		return _foodSkillId;
	}
	
	/**
	 * Sets the food type.
	 * @param foodItemId the new food type
	 */
	public void setFoodType(int foodItemId)
	{
		if (foodItemId > 0)
		{
			_foodSkillId = foodItemId;
			
			// start the duration checks
			// start the buff tasks
			if (_durationCheckTask != null)
			{
				_durationCheckTask.cancel(true);
			}
			
			_durationCheckTask = ThreadPool.scheduleAtFixedRate(new CheckDuration(this), DURATION_CHECK_INTERVAL, DURATION_CHECK_INTERVAL);
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		getAI().stopFollow();
		cleanTasks();
		
		return true;
	}
	
	/**
	 * Clean tasks.
	 */
	private synchronized void cleanTasks()
	{
		if (_buffTask != null)
		{
			_buffTask.cancel(true);
			_buffTask = null;
		}
		
		if (_durationCheckTask != null)
		{
			_durationCheckTask.cancel(true);
			_durationCheckTask = null;
		}
		
		// clean up variables
		if (_owner != null)
		{
			_owner.setTrainedBeast(null);
			_owner = null;
		}
		
		_foodSkillId = 0;
		_remainingTime = 0;
	}
	
	/**
	 * Gets the owner.
	 * @return the owner
	 */
	public PlayerInstance getOwner()
	{
		return _owner;
	}
	
	/**
	 * Sets the owner.
	 * @param owner the new owner
	 */
	public void setOwner(PlayerInstance owner)
	{
		if (owner != null)
		{
			_owner = owner;
			setTitle(owner.getName());
			// broadcast the new title
			broadcastPacket(new NpcInfo(this, owner));
			owner.setTrainedBeast(this);
			
			// always and automatically follow the owner.
			getAI().startFollow(_owner, 100);
			
			// instead of calculating this value each time, let's get this now and pass it on
			int totalBuffsAvailable = 0;
			for (Skill skill : getTemplate().getSkills().values())
			{
				// if the skill is a buff, check if the owner has it already [ owner.getEffect(L2Skill skill) ]
				if (skill.getSkillType() == Skill.SkillType.BUFF)
				{
					totalBuffsAvailable++;
				}
			}
			
			// start the buff tasks
			if (_buffTask != null)
			{
				_buffTask.cancel(true);
			}
			_buffTask = ThreadPool.scheduleAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), BUFF_INTERVAL, BUFF_INTERVAL);
		}
		else
		{
			doDespawn(); // despawn if no owner
		}
	}
	
	/**
	 * Checks if is too far from home.
	 * @return true, if is too far from home
	 */
	public boolean isTooFarFromHome()
	{
		return !isInsideRadius(_homeX, _homeY, _homeZ, MAX_DISTANCE_FROM_HOME, true, true);
	}
	
	/**
	 * Do despawn.
	 */
	public void doDespawn()
	{
		// stop running tasks
		getAI().stopFollow();
		stopHpMpRegeneration();
		setTarget(null);
		cleanTasks();
		onDecay();
	}
	
	// notification triggered by the owner when the owner is attacked.
	// tamed mobs will heal/recharge or debuff the enemy according to their skills
	/**
	 * On owner got attacked.
	 * @param attacker the attacker
	 */
	public void onOwnerGotAttacked(Creature attacker)
	{
		// check if the owner is no longer around...if so, despawn
		if ((_owner == null) || !_owner.isOnline())
		{
			doDespawn();
			return;
		}
		
		// if the owner is too far away, stop anything else and immediately run towards the owner.
		if (!_owner.isInsideRadius(this, MAX_DISTANCE_FROM_OWNER, true, true))
		{
			getAI().startFollow(_owner);
			return;
		}
		
		// if the owner is dead, do nothing...
		if (_owner.isDead())
		{
			return;
		}
		
		// if the tamed beast is currently in the middle of casting, let it complete its skill...
		if (isCastingNow())
		{
			return;
		}
		
		final float HPRatio = (float) _owner.getCurrentHp() / _owner.getMaxHp();
		
		// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
		// use of more than one debuff at this moment is acceptable
		if (HPRatio >= 0.8)
		{
			for (Skill skill : getTemplate().getSkills().values())
			{
				// if the skill is a debuff, check if the attacker has it already [ attacker.getEffect(L2Skill skill) ]
				if ((skill.getSkillType() == Skill.SkillType.DEBUFF) && (Rnd.get(3) < 1) && (attacker.getFirstEffect(skill) != null))
				{
					sitCastAndFollow(skill, attacker);
				}
			}
		}
		// for HP levels between 80% and 50%, do not react to attack events (so that MP can regenerate a bit)
		// for lower HP ranges, heal or recharge the owner with 1 skill use per attack.
		else if (HPRatio < 0.5)
		{
			int chance = 1;
			if (HPRatio < 0.25)
			{
				chance = 2;
			}
			
			// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
			for (Skill skill : getTemplate().getSkills().values())
			{
				// if the skill is a buff, check if the owner has it already [ owner.getEffect(L2Skill skill) ]
				if ((Rnd.get(5) < chance) && ((skill.getSkillType() == Skill.SkillType.HEAL) || (skill.getSkillType() == Skill.SkillType.HOT) || (skill.getSkillType() == Skill.SkillType.BALANCE_LIFE) || (skill.getSkillType() == Skill.SkillType.HEAL_PERCENT) || (skill.getSkillType() == Skill.SkillType.HEAL_STATIC) || (skill.getSkillType() == Skill.SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == Skill.SkillType.COMBATPOINTPERCENTHEAL) || (skill.getSkillType() == Skill.SkillType.CPHOT) || (skill.getSkillType() == Skill.SkillType.MANAHEAL) || (skill.getSkillType() == Skill.SkillType.MANA_BY_LEVEL) || (skill.getSkillType() == Skill.SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == Skill.SkillType.MANARECHARGE) || (skill.getSkillType() == Skill.SkillType.MPHOT)))
				{
					sitCastAndFollow(skill, _owner);
					return;
				}
			}
		}
	}
	
	/**
	 * Prepare and cast a skill: First smoothly prepare the beast for casting, by abandoning other actions Next, call super.doCast(skill) in order to actually cast the spell Finally, return to auto-following the owner.
	 * @param skill the skill
	 * @param target the target
	 * @see org.l2jserver.gameserver.model.actor.Creature#doCast(org.l2jserver.gameserver.model.Skill)
	 */
	protected void sitCastAndFollow(Skill skill, Creature target)
	{
		stopMove(null);
		broadcastPacket(new StopMove(this));
		getAI().setIntention(AI_INTENTION_IDLE);
		
		setTarget(target);
		doCast(skill);
		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
	}
	
	/**
	 * The Class CheckDuration.
	 */
	private class CheckDuration implements Runnable
	{
		/** The _tamed beast. */
		private final TamedBeastInstance _tamedBeast;
		
		/**
		 * Instantiates a new check duration.
		 * @param tamedBeast the tamed beast
		 */
		CheckDuration(TamedBeastInstance tamedBeast)
		{
			_tamedBeast = tamedBeast;
		}
		
		@Override
		public void run()
		{
			final int foodTypeSkillId = _tamedBeast.getFoodType();
			final PlayerInstance owner = _tamedBeast.getOwner();
			_tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - DURATION_CHECK_INTERVAL);
			
			// I tried to avoid this as much as possible...but it seems I can't avoid hardcoding
			// ids further, except by carrying an additional variable just for these two lines...
			// Find which food item needs to be consumed.
			ItemInstance item = null;
			if (foodTypeSkillId == 2188)
			{
				item = owner.getInventory().getItemByItemId(6643);
			}
			else if (foodTypeSkillId == 2189)
			{
				item = owner.getInventory().getItemByItemId(6644);
			}
			
			// if the owner has enough food, call the item handler (use the food and triffer all necessary actions)
			if ((item != null) && (item.getCount() >= 1))
			{
				final WorldObject oldTarget = owner.getTarget();
				owner.setTarget(_tamedBeast);
				final WorldObject[] targets =
				{
					_tamedBeast
				};
				
				// emulate a call to the owner using food, but bypass all checks for range, etc
				// this also causes a call to the AI tasks handling feeding, which may call onReceiveFood as required.
				owner.callSkill(SkillTable.getInstance().getInfo(foodTypeSkillId, 1), targets);
				owner.setTarget(oldTarget);
			}
			else if (_tamedBeast.getRemainingTime() < (MAX_DURATION - 300000)) // if the owner has no food, the beast immediately despawns, except when it was only newly spawned. Newly spawned beasts can last up to 5 minutes
			{
				_tamedBeast.setRemainingTime(-1);
			}
			
			if (_tamedBeast.getRemainingTime() <= 0)
			{
				_tamedBeast.doDespawn();
			}
		}
	}
	
	private class CheckOwnerBuffs implements Runnable
	{
		private final TamedBeastInstance _tamedBeast;
		private final int _numBuffs;
		
		CheckOwnerBuffs(TamedBeastInstance tamedBeast, int numBuffs)
		{
			_tamedBeast = tamedBeast;
			_numBuffs = numBuffs;
		}
		
		@Override
		public void run()
		{
			final PlayerInstance owner = _tamedBeast.getOwner();
			
			// check if the owner is no longer around...if so, despawn
			if ((owner == null) || !owner.isOnline())
			{
				doDespawn();
				return;
			}
			
			// if the owner is too far away, stop anything else and immediately run towards the owner.
			if (!isInsideRadius(owner, MAX_DISTANCE_FROM_OWNER, true, true))
			{
				getAI().startFollow(owner);
				return;
			}
			
			// if the owner is dead, do nothing...
			if (owner.isDead())
			{
				return;
			}
			
			// if the tamed beast is currently casting a spell, do not interfere (do not attempt to cast anything new yet).
			if (isCastingNow())
			{
				return;
			}
			
			int totalBuffsOnOwner = 0;
			int i = 0;
			final int rand = Rnd.get(_numBuffs);
			Skill buffToGive = null;
			
			// get this npc's skills: getSkills()
			for (Skill skill : _tamedBeast.getTemplate().getSkills().values())
			{
				// if the skill is a buff, check if the owner has it already [ owner.getEffect(L2Skill skill) ]
				if (skill.getSkillType() == Skill.SkillType.BUFF)
				{
					if (i == rand)
					{
						buffToGive = skill;
					}
					i++;
					if (owner.getFirstEffect(skill) != null)
					{
						totalBuffsOnOwner++;
					}
				}
			}
			// if the owner has less than 60% of this beast's available buff, cast a random buff
			if (((_numBuffs * 2) / 3) > totalBuffsOnOwner)
			{
				_tamedBeast.sitCastAndFollow(buffToGive, owner);
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _tamedBeast.getOwner());
		}
	}
}
