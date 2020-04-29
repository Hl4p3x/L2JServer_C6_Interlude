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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlEvent;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.handler.SkillHandler;
import org.l2jserver.gameserver.instancemanager.DuelManager;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.model.skills.handlers.SkillDrain;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * The Class CubicInstance.
 */
public class CubicInstance
{
	protected static final Logger LOGGER = Logger.getLogger(CubicInstance.class.getName());
	
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
	public static final int SMART_CUBIC_ARCANALORD = 12;
	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;
	public static final int MAX_MAGIC_RANGE = 900;
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	
	protected PlayerInstance _owner;
	protected Creature _target;
	protected int _id;
	protected int _matk;
	protected int _activationtime;
	protected int _activationchance;
	private final boolean _givenByOther;
	protected List<Skill> _skills = new ArrayList<>();
	private Future<?> _disappearTask;
	private Future<?> _actionTask;
	
	/**
	 * Instantiates a new cubic instance.
	 * @param owner the owner
	 * @param id the id
	 * @param level the level
	 * @param mAtk the m atk
	 * @param activationtime the activationtime
	 * @param activationchance the activationchance
	 * @param totallifetime the totallifetime
	 * @param givenByOther the given by other
	 */
	public CubicInstance(PlayerInstance owner, int id, int level, int mAtk, int activationtime, int activationchance, int totallifetime, boolean givenByOther)
	{
		_owner = owner;
		_id = id;
		_matk = mAtk;
		_activationtime = activationtime * 1000;
		_activationchance = activationchance;
		_givenByOther = givenByOther;
		
		switch (_id)
		{
			case STORM_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			}
			case VAMPIRIC_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			}
			case LIFE_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4051, level));
				doAction();
				break;
			}
			case VIPER_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			}
			case POLTERGEIST_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4053, level));
				_skills.add(SkillTable.getInstance().getInfo(4054, level));
				_skills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			}
			case BINDING_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			}
			case AQUA_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			}
			case SPARK_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			}
			case ATTRACT_CUBIC:
			{
				_skills.add(SkillTable.getInstance().getInfo(5115, level));
				_skills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
			}
			case SMART_CUBIC_ARCANALORD:
			{
				// _skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4051, 7)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the cubic skills list
				break;
			}
			case SMART_CUBIC_ELEMENTALMASTER:
			{
				_skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4166, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the cubic skills list
				break;
			}
			case SMART_CUBIC_SPECTRALMASTER:
			{
				_skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4052, 6)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the cubic skills list
				break;
			}
			case SMART_CUBIC_EVATEMPLAR:
			{
				// _skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4053, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the cubic skills list
				break;
			}
			case SMART_CUBIC_SHILLIENTEMPLAR:
			{
				_skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(5115, 4)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the cubic skills list
				break;
			}
		}
		_disappearTask = ThreadPool.schedule(new Disappear(), totallifetime); // disappear
	}
	
	public void doAction()
	{
		if (_actionTask == null)
		{
			synchronized (this)
			{
				if (_actionTask == null)
				{
					switch (_id)
					{
						case AQUA_CUBIC:
						case BINDING_CUBIC:
						case SPARK_CUBIC:
						case STORM_CUBIC:
						case POLTERGEIST_CUBIC:
						case VAMPIRIC_CUBIC:
						case VIPER_CUBIC:
						case ATTRACT_CUBIC:
						case SMART_CUBIC_ARCANALORD:
						case SMART_CUBIC_ELEMENTALMASTER:
						case SMART_CUBIC_SPECTRALMASTER:
						case SMART_CUBIC_EVATEMPLAR:
						case SMART_CUBIC_SHILLIENTEMPLAR:
						{
							_actionTask = ThreadPool.scheduleAtFixedRate(new Action(_activationchance), 0, _activationtime);
							break;
						}
						case LIFE_CUBIC:
						{
							_actionTask = ThreadPool.scheduleAtFixedRate(new Heal(), 0, _activationtime);
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets the id.
	 * @return the id
	 */
	public int getId()
	{
		return _id;
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
	 * Gets the m critical hit.
	 * @param target the target
	 * @param skill the skill
	 * @return the m critical hit
	 */
	public int getMCriticalHit(Creature target, Skill skill)
	{
		// TODO: Temporary now mcrit for cubics is the baseMCritRate of its owner
		return _owner.getTemplate().getBaseMCritRate();
	}
	
	/**
	 * Gets the m atk.
	 * @return the m atk
	 */
	public int getMAtk()
	{
		return _matk;
	}
	
	/**
	 * Stop action.
	 */
	public void stopAction()
	{
		_target = null;
		if (_actionTask != null)
		{
			if (!_actionTask.isCancelled())
			{
				_actionTask.cancel(true);
			}
			_actionTask = null;
		}
	}
	
	/**
	 * Cancel disappear.
	 */
	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}
	
	/**
	 * this sets the enemy target for a cubic.
	 */
	public void getCubicTarget()
	{
		try
		{
			_target = null;
			final WorldObject ownerTarget = _owner.getTarget();
			if (ownerTarget == null)
			{
				return;
			}
			
			// Duel targeting
			if (_owner.isInDuel())
			{
				final PlayerInstance playerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
				final PlayerInstance playerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();
				if (DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
				{
					final Party partyA = playerA.getParty();
					final Party partyB = playerB.getParty();
					Party partyEnemy = null;
					if (partyA != null)
					{
						if (partyA.getPartyMembers().contains(_owner))
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = playerB;
							}
						}
						else
						{
							partyEnemy = partyA;
						}
					}
					else if (playerA == _owner)
					{
						if (partyB != null)
						{
							partyEnemy = partyB;
						}
						else
						{
							_target = playerB;
						}
					}
					else
					{
						_target = playerA;
					}
					if (((_target == playerA) || (_target == playerB)) && (_target == ownerTarget))
					{
						return;
					}
					if (partyEnemy != null)
					{
						if (partyEnemy.getPartyMembers().contains(ownerTarget))
						{
							_target = (Creature) ownerTarget;
						}
						return;
					}
				}
				if ((playerA != _owner) && (ownerTarget == playerA))
				{
					_target = playerA;
					return;
				}
				if ((playerB != _owner) && (ownerTarget == playerB))
				{
					_target = playerB;
					return;
				}
				_target = null;
				return;
			}
			
			// Olympiad targeting
			if (_owner.isInOlympiadMode())
			{
				if (_owner.isOlympiadStart())
				{
					final PlayerInstance[] players = Olympiad.getInstance().getPlayers(_owner.getOlympiadGameId());
					if (players != null)
					{
						if (_owner.getOlympiadSide() == 1)
						{
							if (ownerTarget == players[1])
							{
								_target = players[1];
							}
							else if ((players[1].getPet() != null) && (ownerTarget == players[1].getPet()))
							{
								_target = players[1].getPet();
							}
						}
						else if (ownerTarget == players[0])
						{
							_target = players[0];
						}
						else if ((players[0].getPet() != null) && (ownerTarget == players[0].getPet()))
						{
							_target = players[0].getPet();
						}
					}
				}
				return;
			}
			
			// test owners target if it is valid then use it
			if ((ownerTarget instanceof Creature) && (ownerTarget != _owner.getPet()) && (ownerTarget != _owner))
			{
				// target mob which has aggro on you or your summon
				if (ownerTarget instanceof Attackable)
				{
					if ((((Attackable) ownerTarget).getAggroList().get(_owner) != null) && !((Attackable) ownerTarget).isDead())
					{
						_target = (Creature) ownerTarget;
						return;
					}
					if ((_owner.getPet() != null) && (((Attackable) ownerTarget).getAggroList().get(_owner.getPet()) != null) && !((Attackable) ownerTarget).isDead())
					{
						_target = (Creature) ownerTarget;
						return;
					}
				}
				
				// get target in pvp or in siege
				PlayerInstance enemy = null;
				if (((_owner.getPvpFlag() > 0) && !_owner.isInsideZone(ZoneId.PEACE)) || _owner.isInsideZone(ZoneId.PVP))
				{
					if (!((Creature) ownerTarget).isDead() && (ownerTarget instanceof PlayerInstance))
					{
						enemy = (PlayerInstance) ownerTarget;
					}
					
					if (enemy != null)
					{
						boolean targetIt = true;
						if (_owner.getParty() != null)
						{
							if (_owner.getParty().getPartyMembers().contains(enemy))
							{
								targetIt = false;
							}
							else if ((_owner.getParty().getCommandChannel() != null) && _owner.getParty().getCommandChannel().getMembers().contains(enemy))
							{
								targetIt = false;
							}
						}
						if ((_owner.getClan() != null) && !_owner.isInsideZone(ZoneId.PVP))
						{
							if (_owner.getClan().isMember(enemy.getName()))
							{
								targetIt = false;
							}
							if ((_owner.getAllyId() > 0) && (enemy.getAllyId() > 0) && (_owner.getAllyId() == enemy.getAllyId()))
							{
								targetIt = false;
							}
						}
						if ((enemy.getPvpFlag() == 0) && !enemy.isInsideZone(ZoneId.PVP))
						{
							targetIt = false;
						}
						if (enemy.isInsideZone(ZoneId.PEACE))
						{
							targetIt = false;
						}
						if ((_owner.getSiegeState() > 0) && (_owner.getSiegeState() == enemy.getSiegeState()))
						{
							targetIt = false;
						}
						if (!enemy.isVisible())
						{
							targetIt = false;
						}
						
						if (targetIt)
						{
							_target = enemy;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
	}
	
	private class Action implements Runnable
	{
		private final int _chance;
		
		/**
		 * Instantiates a new action.
		 * @param chance the chance
		 */
		Action(int chance)
		{
			_chance = chance;
		}
		
		@Override
		public void run()
		{
			try
			{
				final PlayerInstance owner = _owner;
				if (owner == null)
				{
					stopAction();
					cancelDisappear();
					return;
				}
				
				if (owner.isDead() || !owner.isOnline())
				{
					stopAction();
					owner.delCubic(_id);
					owner.broadcastUserInfo();
					cancelDisappear();
					return;
				}
				
				if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(owner))
				{
					if (owner.getPet() != null)
					{
						if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(owner.getPet()))
						{
							stopAction();
							return;
						}
					}
					else
					{
						stopAction();
						return;
					}
				}
				
				// Smart Cubic debuff cancel is 100%
				boolean useCubicCure = false;
				Skill skill = null;
				if ((_id >= SMART_CUBIC_EVATEMPLAR) && (_id <= SMART_CUBIC_SPECTRALMASTER))
				{
					final Effect[] effects = owner.getAllEffects();
					for (Effect e : effects)
					{
						if ((e != null) && e.getSkill().isOffensive())
						{
							useCubicCure = true;
							e.exit(true);
						}
					}
				}
				
				if (useCubicCure)
				{
					// Smart Cubic debuff cancel is needed, no other skill is used in this
					// activation period
					owner.broadcastPacket(new MagicSkillUse(owner, owner, SKILL_CUBIC_CURE, 1, 0, 0));
				}
				else if (Rnd.get(100) < _chance)
				{
					skill = _skills.get(Rnd.get(_skills.size()));
					if (skill != null)
					{
						if (skill.getId() == SKILL_CUBIC_HEAL)
						{
							// friendly skill, so we look a target in owner's party
							cubicTargetForHeal();
						}
						else
						{
							// offensive skill, we look for an enemy target
							getCubicTarget();
							if ((_target == owner) || !isInCubicRange(owner, _target))
							{
								_target = null;
							}
						}
						
						final Creature target = _target;
						if ((target != null) && (!target.isDead()))
						{
							owner.broadcastPacket(new MagicSkillUse(owner, target, skill.getId(), skill.getLevel(), 0, 0));
							
							final SkillType type = skill.getSkillType();
							final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
							final Creature[] targets =
							{
								target
							};
							
							if ((type == SkillType.PARALYZE) || (type == SkillType.STUN) || (type == SkillType.ROOT) || (type == SkillType.AGGDAMAGE))
							{
								useCubicDisabler(type, CubicInstance.this, skill, targets);
							}
							else if (type == SkillType.MDAM)
							{
								useCubicMdam(CubicInstance.this, skill, targets);
							}
							else if ((type == SkillType.POISON) || (type == SkillType.DEBUFF) || (type == SkillType.DOT))
							{
								useCubicContinuous(CubicInstance.this, skill, targets);
							}
							else if (type == SkillType.DRAIN)
							{
								((SkillDrain) skill).useCubicSkill(CubicInstance.this, targets);
							}
							else
							{
								handler.useSkill(owner, skill, targets);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	/**
	 * Use cubic continuous.
	 * @param activeCubic the active cubic
	 * @param skill the skill
	 * @param targets the targets
	 */
	public void useCubicContinuous(CubicInstance activeCubic, Skill skill, WorldObject[] targets)
	{
		for (Creature target : (Creature[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			if (skill.isOffensive())
			{
				final boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill);
				if (!acted)
				{
					activeCubic.getOwner().sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
					continue;
				}
			}
			
			// if this is a debuff let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
			if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isInDuel() && (skill.getSkillType() == SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((PlayerInstance) target).getDuelId()))
			{
				final DuelManager dm = DuelManager.getInstance();
				for (Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
				{
					if (debuff != null)
					{
						dm.onBuff(((PlayerInstance) target), debuff);
					}
				}
			}
			else
			{
				skill.getEffects(activeCubic.getOwner(), target);
			}
		}
	}
	
	/**
	 * Use cubic mdam.
	 * @param activeCubic the active cubic
	 * @param skill the skill
	 * @param targets the targets
	 */
	public void useCubicMdam(CubicInstance activeCubic, Skill skill, WorldObject[] targets)
	{
		for (Creature target : (Creature[]) targets)
		{
			if (target == null)
			{
				continue;
			}
			
			if (target.isAlikeDead())
			{
				if (target instanceof PlayerInstance)
				{
					target.stopFakeDeath(null);
				}
				else
				{
					continue;
				}
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));
			final int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit);
			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);
				if (skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					if (target.getFirstEffect(skill) != null)
					{
						target.removeEffect(target.getFirstEffect(skill));
					}
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						skill.getEffects(activeCubic.getOwner(), target);
					}
				}
				
				target.reduceCurrentHp(damage, activeCubic.getOwner());
			}
		}
	}
	
	/**
	 * Use cubic disabler.
	 * @param type the type
	 * @param activeCubic the active cubic
	 * @param skill the skill
	 * @param targets the targets
	 */
	public void useCubicDisabler(SkillType type, CubicInstance activeCubic, Skill skill, WorldObject[] targets)
	{
		for (Creature target : (Creature[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			switch (type)
			{
				case STUN:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						// if this is a debuff let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
						if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isInDuel() && (skill.getSkillType() == SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((PlayerInstance) target).getDuelId()))
						{
							final DuelManager dm = DuelManager.getInstance();
							for (Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff(((PlayerInstance) target), debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic.getOwner(), target);
						}
					}
					break;
				}
				case PARALYZE: // use same as root for now
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						// if this is a debuff let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
						if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isInDuel() && (skill.getSkillType() == SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((PlayerInstance) target).getDuelId()))
						{
							final DuelManager dm = DuelManager.getInstance();
							for (Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff(((PlayerInstance) target), debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic.getOwner(), target);
						}
					}
					break;
				}
				case CANCEL:
				{
					final Effect[] effects = target.getAllEffects();
					if ((effects == null) || (effects.length == 0))
					{
						break;
					}
					final int maxNegatedEffects = 3;
					int count = 0;
					for (Effect e : effects)
					{
						// Do not remove raid curse skills
						if (e.getSkill().isOffensive() && (count < maxNegatedEffects) && (e.getSkill().getId() != 4215) && (e.getSkill().getId() != 4515) && (e.getSkill().getId() != 4082))
						{
							e.exit(true);
							if (count > -1)
							{
								count++;
							}
						}
					}
					break;
				}
				case ROOT:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						// if this is a debuff let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
						if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isInDuel() && (skill.getSkillType() == SkillType.DEBUFF) && (activeCubic.getOwner().getDuelId() == ((PlayerInstance) target).getDuelId()))
						{
							final DuelManager dm = DuelManager.getInstance();
							for (Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff(((PlayerInstance) target), debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic.getOwner(), target);
						}
					}
					break;
				}
				case AGGDAMAGE:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						if (target instanceof Attackable)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeCubic.getOwner(), (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
						}
						skill.getEffects(activeCubic.getOwner(), target);
					}
					break;
				}
			}
		}
	}
	
	/**
	 * returns true if the target is inside of the owner's max Cubic range.
	 * @param owner the owner
	 * @param target the target
	 * @return true, if is in cubic range
	 */
	public boolean isInCubicRange(Creature owner, Creature target)
	{
		if ((owner == null) || (target == null))
		{
			return false;
		}
		
		int x;
		int y;
		int z;
		// temporary range check until real behavior of cubics is known/coded
		final int range = MAX_MAGIC_RANGE;
		x = (owner.getX() - target.getX());
		y = (owner.getY() - target.getY());
		z = (owner.getZ() - target.getZ());
		return (((x * x) + (y * y) + (z * z)) <= (range * range));
	}
	
	/**
	 * this sets the friendly target for a cubic.
	 */
	public void cubicTargetForHeal()
	{
		Creature target = null;
		double percentleft = 100.0;
		Party party = _owner.getParty();
		
		// if owner is in a duel but not in a party duel, then it is the same as he does not have a party
		if (_owner.isInDuel() && !DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
		{
			party = null;
		}
		
		if ((party != null) && !_owner.isInOlympiadMode())
		{
			// Get all visible objects in a spheric area near the Creature
			// Get a list of Party Members
			final List<PlayerInstance> partyList = party.getPartyMembers();
			for (Creature partyMember : partyList)
			{
				// if party member not dead, check if he is in castrange of heal cubic and member is in cubic casting range, check if he need heal and if he have the lowest HP
				if (!partyMember.isDead() && isInCubicRange(_owner, partyMember) && (partyMember.getCurrentHp() < partyMember.getMaxHp()) && (percentleft > (partyMember.getCurrentHp() / partyMember.getMaxHp())))
				{
					percentleft = (partyMember.getCurrentHp() / partyMember.getMaxHp());
					target = partyMember;
				}
				if (partyMember.getPet() != null)
				{
					if (partyMember.getPet().isDead())
					{
						continue;
					}
					
					// if party member's pet not dead, check if it is in castrange of heal cubic
					if (!isInCubicRange(_owner, partyMember.getPet()))
					{
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have the lowest HP
					if ((partyMember.getPet().getCurrentHp() < partyMember.getPet().getMaxHp()) && (percentleft > (partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp())))
					{
						percentleft = (partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp());
						target = partyMember.getPet();
					}
				}
			}
		}
		else
		{
			if (_owner.getCurrentHp() < _owner.getMaxHp())
			{
				percentleft = (_owner.getCurrentHp() / _owner.getMaxHp());
				target = _owner;
			}
			if ((_owner.getPet() != null) && !_owner.getPet().isDead() && (_owner.getPet().getCurrentHp() < _owner.getPet().getMaxHp()) && (percentleft > (_owner.getPet().getCurrentHp() / _owner.getPet().getMaxHp())) && isInCubicRange(_owner, _owner.getPet()))
			{
				target = _owner.getPet();
			}
		}
		
		_target = target;
	}
	
	/**
	 * Given by other.
	 * @return true, if successful
	 */
	public boolean givenByOther()
	{
		return _givenByOther;
	}
	
	private class Heal implements Runnable
	{
		Heal()
		{
		}
		
		@Override
		public void run()
		{
			if (_owner.isDead() || !_owner.isOnline())
			{
				stopAction();
				_owner.delCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			try
			{
				Skill skill = null;
				for (Skill sk : _skills)
				{
					if (sk.getId() == SKILL_CUBIC_HEAL)
					{
						skill = sk;
						break;
					}
				}
				
				if (skill != null)
				{
					cubicTargetForHeal();
					final Creature target = _target;
					if ((target != null) && !target.isDead() && ((target.getMaxHp() - target.getCurrentHp()) > skill.getPower()))
					{
						final Creature[] targets =
						{
							target
						};
						final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
						if (handler != null)
						{
							handler.useSkill(_owner, skill, targets);
						}
						else
						{
							skill.useSkill(_owner, targets);
						}
						
						_owner.broadcastPacket(new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0));
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	private class Disappear implements Runnable
	{
		Disappear()
		{
		}
		
		@Override
		public void run()
		{
			stopAction();
			_owner.delCubic(_id);
			_owner.broadcastUserInfo();
		}
	}
}
