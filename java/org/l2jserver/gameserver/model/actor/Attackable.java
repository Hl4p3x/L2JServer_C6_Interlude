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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ItemsAutoDestroy;
import org.l2jserver.gameserver.ai.AttackableAI;
import org.l2jserver.gameserver.ai.CreatureAI;
import org.l2jserver.gameserver.ai.CtrlEvent;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.ai.FortSiegeGuardAI;
import org.l2jserver.gameserver.ai.SiegeGuardAI;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.xml.ManorSeedData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jserver.gameserver.model.CommandChannel;
import org.l2jserver.gameserver.model.DropCategory;
import org.l2jserver.gameserver.model.DropData;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.FolkInstance;
import org.l2jserver.gameserver.model.actor.instance.FortSiegeGuardInstance;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.MinionInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jserver.gameserver.model.actor.instance.SiegeGuardInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.actor.knownlist.AttackableKnownList;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.base.SoulCrystal;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.EtcItemType;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.script.EventDroplist;
import org.l2jserver.gameserver.script.EventDroplist.DateDrop;
import org.l2jserver.gameserver.util.Util;

/**
 * This class manages all NPC that can be attacked.<br>
 * <br>
 * Attackable:<br>
 * <li>ArtefactInstance</li>
 * <li>FriendlyMobInstance</li>
 * <li>MonsterInstance</li>
 * <li>SiegeGuardInstance</li>
 * @version $Revision: 1.24.2.3.2.16 $ $Date: 2009/04/13 02:11:03 $
 */
public class Attackable extends NpcInstance
{
	/**
	 * This class contains all AggroInfo of the Attackable against the attacker Creature.<br>
	 * <br>
	 * <b><u>Data</u>:</b><br>
	 * <li>attacker : The attaker Creature concerned by this AggroInfo of this Attackable</li>
	 * <li>hate : Hate level of this Attackable against the attaker Creature (hate = damage)</li>
	 * <li>damage : Number of damages that the attaker Creature gave to this Attackable</li>
	 */
	public class AggroInfo
	{
		/** The attaker Creature concerned by this AggroInfo of this Attackable */
		protected Creature _attacker;
		
		/** Hate level of this Attackable against the attaker Creature (hate = damage) */
		protected int _hate;
		
		/** Number of damages that the attaker Creature gave to this Attackable */
		protected int _damage;
		
		/**
		 * Constructor of AggroInfo.
		 * @param pAttacker
		 */
		AggroInfo(Creature pAttacker)
		{
			_attacker = pAttacker;
		}
		
		/**
		 * Verify is object is equal to this AggroInfo.
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			
			if (obj instanceof AggroInfo)
			{
				return ((AggroInfo) obj)._attacker == _attacker;
			}
			
			return false;
		}
		
		/**
		 * Return the Identifier of the attacker Creature.
		 */
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	/**
	 * This class contains all RewardInfo of the Attackable against the any attacker Creature, based on amount of damage done.<br>
	 * <br>
	 * <b><u>Data</u>:</b><br>
	 * <li>attacker : The attacker Creature concerned by this RewardInfo of this Attackable</li>
	 * <li>dmg : Total amount of damage done by the attacker to this Attackable (summon + own)</li>
	 */
	protected final class RewardInfo
	{
		protected Creature _attacker;
		protected long _dmg = 0;
		
		public RewardInfo(Creature pAttacker, long pDmg)
		{
			_attacker = pAttacker;
			_dmg = pDmg;
		}
		
		public void addDamage(long pDmg)
		{
			_dmg += pDmg;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			
			if (obj instanceof RewardInfo)
			{
				return ((RewardInfo) obj)._attacker == _attacker;
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	/**
	 * This class contains all AbsorberInfo of the Attackable against the absorber Creature.<br>
	 * <br>
	 * <b><u>Data</u>:</b><br>
	 * <li>absorber : The attacker Creature concerned by this AbsorberInfo of this Attackable</li>
	 */
	public class AbsorberInfo
	{
		/** The attacker Creature concerned by this AbsorberInfo of this Attackable */
		protected PlayerInstance _absorber;
		protected int _crystalId;
		protected double _absorbedHP;
		
		/**
		 * Constructor of AbsorberInfo.
		 * @param attacker
		 * @param pCrystalId
		 * @param pAbsorbedHP
		 */
		AbsorberInfo(PlayerInstance attacker, int pCrystalId, double pAbsorbedHP)
		{
			_absorber = attacker;
			_crystalId = pCrystalId;
			_absorbedHP = pAbsorbedHP;
		}
		
		/**
		 * Verify is object is equal to this AbsorberInfo.
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			
			if (obj instanceof AbsorberInfo)
			{
				return ((AbsorberInfo) obj)._absorber == _absorber;
			}
			
			return false;
		}
		
		/**
		 * Return the Identifier of the absorber Creature.
		 */
		@Override
		public int hashCode()
		{
			return _absorber.getObjectId();
		}
	}
	
	/**
	 * This class is used to create item reward lists instead of creating item instances.
	 */
	public class RewardItem
	{
		protected int _itemId;
		protected int _count;
		
		public RewardItem(int itemId, int count)
		{
			_itemId = itemId;
			_count = count;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public int getCount()
		{
			return _count;
		}
	}
	
	/**
	 * The table containing all autoAttackable Creature in its Aggro Range and Creature that attacked the Attackable This Map is Thread Safe, but Removing Object While Interating Over It Will Result NPE
	 */
	private final Map<Creature, AggroInfo> _aggroList = new ConcurrentHashMap<>();
	
	/**
	 * Use this to Remove Object from this Map This Should be Synchronized While Interacting over This Map - ie u cant Interacting and removing object at once
	 * @return
	 */
	public Map<Creature, AggroInfo> getAggroList()
	{
		return _aggroList;
	}
	
	private boolean _isReturningToSpawnPoint = false;
	
	public boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}
	
	public void setReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}
	
	private boolean _canReturnToSpawnPoint = true;
	
	public boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}
	
	public void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}
	
	/** Table containing all Items that a Dwarf can Sweep on this Attackable */
	private RewardItem[] _sweepItems;
	
	/** crops */
	private RewardItem[] _harvestItems;
	private boolean _seeded;
	private int _seedType = 0;
	private PlayerInstance _seeder = null;
	
	/** True if an over-hit enabled skill has successfully landed on the Attackable */
	private boolean _overhit;
	
	/** Stores the extra (over-hit) damage done to the Attackable when the attacker uses an over-hit enabled skill */
	private double _overhitDamage;
	
	/** Stores the attacker who used the over-hit enabled skill on the Attackable */
	private Creature _overhitAttacker;
	
	/** First CommandChannel who attacked the Attackable and meet the requirements **/
	private CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0;
	
	/** True if a Soul Crystal was successfuly used on the Attackable */
	private boolean _absorbed;
	
	/** The table containing all PlayerInstance that successfuly absorbed the soul of this Attackable */
	private final Map<PlayerInstance, AbsorberInfo> _absorbersList = new ConcurrentHashMap<>();
	
	/** Have this Attackable to reward Exp and SP on Die? **/
	private boolean _mustGiveExpSp;
	
	public Creature _mostHated;
	
	/**
	 * Constructor of Attackable (use Creature and NpcInstance constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to set the _template of the Attackable (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the Attackable</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template
	 */
	public Attackable(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		_mustGiveExpSp = true;
	}
	
	@Override
	public AttackableKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof AttackableKnownList))
		{
			setKnownList(new AttackableKnownList(this));
		}
		return (AttackableKnownList) super.getKnownList();
	}
	
	/**
	 * Return the Creature AI of the Attackable and if its null create a new one.
	 */
	@Override
	public CreatureAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new AttackableAI(new AIAccessor());
				}
			}
		}
		return _ai;
	}
	
	/**
	 * Not used.
	 * @param target
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public boolean getCondition2(Creature target)
	{
		if ((target instanceof FolkInstance) || (target instanceof DoorInstance))
		{
			return false;
		}
		
		if (target.isAlikeDead() || !isInsideRadius(target, getAggroRange(), false, false) || (Math.abs(getZ() - target.getZ()) > 100))
		{
			return false;
		}
		
		return !target.isInvul();
	}
	
	/**
	 * Reduce the current HP of the Attackable.
	 * @param damage The HP decrease value
	 * @param attacker The Creature who attacks
	 */
	@Override
	public void reduceCurrentHp(double damage, Creature attacker)
	{
		reduceCurrentHp(damage, attacker, true);
	}
	
	/**
	 * Reduce the current HP of the Attackable, update its _aggroList and launch the doDie Task if necessary.
	 * @param attacker The Creature who attacks
	 * @param awake The awake state (If True : stop sleeping)
	 */
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake)
	{
		if (isRaid() && (attacker != null) && (attacker.getParty() != null) && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if (_firstCommandChannelAttacked == null) // looting right isn't set
			{
				synchronized (this)
				{
					if (_firstCommandChannelAttacked == null)
					{
						_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (_firstCommandChannelAttacked != null)
						{
							_commandChannelTimer = new CommandChannelTimer(this);
							_commandChannelLastAttack = System.currentTimeMillis();
							ThreadPool.schedule(_commandChannelTimer, 10000); // check for last attack
							_firstCommandChannelAttacked.broadcastToChannelMembers(new CreatureSay(0, ChatType.PARTYROOM_ALL, "", "You have looting rights!")); // TODO: retail msg
						}
					}
				}
			}
			else if (attacker.getParty().getCommandChannel().equals(_firstCommandChannelAttacked)) // is in same channel
			{
				_commandChannelLastAttack = System.currentTimeMillis(); // update last attack time
			}
		}
		
		if (isEventMob)
		{
			return;
		}
		
		// Add damage and hate to the attacker AggroInfo of the Attackable _aggroList
		if (attacker != null)
		{
			addDamage(attacker, (int) damage);
		}
		
		// If this Attackable is a MonsterInstance and it has spawned minions, call its minions to battle
		if (this instanceof MonsterInstance)
		{
			MonsterInstance master = (MonsterInstance) this;
			if (this instanceof MinionInstance)
			{
				master = ((MinionInstance) this).getLeader();
				if (!master.isInCombat() && !master.isDead())
				{
					master.addDamage(attacker, 1);
				}
			}
			
			if (master.hasMinions())
			{
				master.callMinionsToAssist(attacker);
			}
		}
		
		// Reduce the current HP of the Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake);
	}
	
	public synchronized void setMustRewardExpSp(boolean value)
	{
		_mustGiveExpSp = value;
	}
	
	public synchronized boolean getMustRewardExpSP()
	{
		return _mustGiveExpSp;
	}
	
	/**
	 * Kill the Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Distribute Exp and SP rewards to PlayerInstance (including Summon owner) that hit the Attackable and to their Party members</li>
	 * <li>Notify the Quest Engine of the Attackable death if necessary</li>
	 * <li>Kill the NpcInstance (the corpse disappeared after 7 seconds)</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T GIVE rewards to PetInstance</b></font>
	 * @param killer The Creature that has killed the Attackable
	 */
	@Override
	public boolean doDie(Creature killer)
	{
		// Kill the NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer))
		{
			return false;
		}
		
		// Enhance soul crystals of the attacker if this Attackable had its soul absorbed
		try
		{
			if (killer instanceof PlayerInstance)
			{
				levelSoulCrystals(killer);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		
		// Notify the Quest Engine of the Attackable death if necessary
		try
		{
			if ((killer instanceof PlayerInstance) || (killer instanceof Summon))
			{
				final PlayerInstance player = killer instanceof PlayerInstance ? (PlayerInstance) killer : ((Summon) killer).getOwner();
				
				// only 1 randomly choosen quest of all quests registered to this character can be applied
				for (Quest quest : getTemplate().getEventQuests(EventType.ON_KILL))
				{
					quest.notifyKill(this, player, killer instanceof Summon);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		
		setChampion(false);
		if (Config.CHAMPION_ENABLE)
		{
			// Set champion on next spawn.
			if (!(this instanceof GrandBossInstance) && !(this instanceof RaidBossInstance) && (this instanceof MonsterInstance) /* && !getTemplate().isQuestMonster */ && (Config.CHAMPION_FREQUENCY > 0) && (getLevel() >= Config.CHAMP_MIN_LVL) && (getLevel() <= Config.CHAMP_MAX_LVL) && (Rnd.get(100) < Config.CHAMPION_FREQUENCY))
			{
				setChampion(true);
			}
		}
		
		return true;
	}
	
	class OnKillNotifyTask implements Runnable
	{
		private final Attackable _attackable;
		private final Quest _quest;
		private final PlayerInstance _killer;
		private final boolean _isPet;
		
		public OnKillNotifyTask(Attackable attackable, Quest quest, PlayerInstance killer, boolean isPet)
		{
			_attackable = attackable;
			_quest = quest;
			_killer = killer;
			_isPet = isPet;
		}
		
		@Override
		public void run()
		{
			_quest.notifyKill(_attackable, _killer, _isPet);
		}
	}
	
	/**
	 * Distribute Exp and SP rewards to PlayerInstance (including Summon owner) that hit the Attackable and to their Party members.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the PlayerInstance owner of the SummonInstance (if necessary) and Party in progress</li>
	 * <li>Calculate the Experience and SP rewards in function of the level difference</li>
	 * <li>Add Exp and SP rewards to PlayerInstance (including Summon penalty) and to Party members in the known area of the last attacker</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T GIVE rewards to PetInstance</b></font>
	 * @param lastAttacker The Creature that has killed the Attackable
	 */
	@Override
	protected void calculateRewards(Creature lastAttacker)
	{
		// Creates an empty list of rewards
		final Map<Creature, RewardInfo> rewards = new ConcurrentHashMap<>();
		
		try
		{
			if (_aggroList.isEmpty())
			{
				return;
			}
			
			PlayerInstance maxDealer = null;
			long maxDamage = 0;
			long damage;
			Creature attacker;
			Creature ddealer;
			
			// While Interacting over This Map Removing Object is Not Allowed
			synchronized (_aggroList)
			{
				// Go through the _aggroList of the Attackable
				for (AggroInfo info : _aggroList.values())
				{
					if (info == null)
					{
						continue;
					}
					
					// Get the Creature corresponding to this attacker
					attacker = info._attacker;
					
					// Get damages done by this attacker
					damage = info._damage;
					
					// Prevent unwanted behavior
					if (damage > 1)
					{
						if ((attacker instanceof SummonInstance) || ((attacker instanceof PetInstance) && (((PetInstance) attacker).getPetData().getOwnerExpTaken() > 0)))
						{
							ddealer = ((Summon) attacker).getOwner();
						}
						else
						{
							ddealer = info._attacker;
						}
						
						// Check if ddealer isn't too far from this (killed monster)
						if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true))
						{
							continue;
						}
						
						// Calculate real damages (Summoners should get own damage plus summon's damage)
						RewardInfo reward = rewards.get(ddealer);
						if (reward == null)
						{
							reward = new RewardInfo(ddealer, damage);
						}
						else
						{
							reward.addDamage(damage);
						}
						
						rewards.put(ddealer, reward);
						
						if ((ddealer instanceof Playable) && (((Playable) ddealer).getActingPlayer() != null) && (reward._dmg > maxDamage))
						{
							maxDealer = ((Playable) ddealer).getActingPlayer();
							maxDamage = reward._dmg;
						}
					}
				}
			}
			
			// Manage Base, Quests and Sweep drops of the Attackable
			doItemDrop((maxDealer != null) && maxDealer.isOnline() ? maxDealer : lastAttacker);
			
			// Manage drop of Special Events created by GM for a defined period
			doEventDrop((maxDealer != null) && maxDealer.isOnline() ? maxDealer : lastAttacker);
			if (!_mustGiveExpSp)
			{
				return;
			}
			
			if (!rewards.isEmpty())
			{
				Party attackerParty;
				long exp;
				long partyDmg;
				int levelDiff;
				int partyLvl;
				int sp;
				float partyMul;
				float penalty;
				RewardInfo reward2;
				int[] tmp;
				for (RewardInfo reward : rewards.values())
				{
					if (reward == null)
					{
						continue;
					}
					
					// Penalty applied to the attacker's XP
					penalty = 0;
					
					// Attacker to be rewarded
					attacker = reward._attacker;
					
					// Total amount of damage done
					damage = reward._dmg;
					
					// If the attacker is a Pet, get the party of the owner
					if (attacker instanceof PetInstance)
					{
						attackerParty = ((PetInstance) attacker).getParty();
					}
					else if (attacker instanceof PlayerInstance)
					{
						attackerParty = ((PlayerInstance) attacker).getParty();
					}
					else
					{
						return;
					}
					
					// If this attacker is a PlayerInstance with a summoned SummonInstance, get Exp Penalty applied for the current summoned SummonInstance
					if ((attacker instanceof PlayerInstance) && (((PlayerInstance) attacker).getPet() instanceof SummonInstance))
					{
						penalty = ((SummonInstance) ((PlayerInstance) attacker).getPet()).getExpPenalty();
					}
					
					// We must avoid "over damage", if any
					if (damage > getMaxHp())
					{
						damage = getMaxHp();
					}
					
					// If there's NO party in progress
					if (attackerParty == null)
					{
						// Calculate Exp and SP rewards
						if (attacker.getKnownList().knowsObject(this))
						{
							// Calculate the difference of level between this attacker (PlayerInstance or SummonInstance owner) and the Attackable
							// mob = 24, atk = 10, diff = -14 (full xp)
							// mob = 24, atk = 28, diff = 4 (some xp)
							// mob = 24, atk = 50, diff = 26 (no xp)
							levelDiff = attacker.getLevel() - getLevel();
							tmp = calculateExpAndSp(levelDiff, damage);
							exp = tmp[0];
							exp *= 1 - penalty;
							sp = tmp[1];
							if (Config.CHAMPION_ENABLE && isChampion())
							{
								exp *= Config.CHAMPION_REWARDS;
								sp *= Config.CHAMPION_REWARDS;
							}
							
							// Check for an over-hit enabled strike and Donator options
							if (attacker instanceof PlayerInstance)
							{
								final PlayerInstance player = (PlayerInstance) attacker;
								if (_overhit && (attacker == _overhitAttacker))
								{
									player.sendPacket(SystemMessageId.OVER_HIT);
									exp += calculateOverhitExp(exp);
								}
								if (player.isDonator())
								{
									exp = (long) (exp * Config.DONATOR_XPSP_RATE);
									sp = (int) (sp * Config.DONATOR_XPSP_RATE);
								}
							}
							
							// Distribute the Exp and SP between the PlayerInstance and its Summon
							if (!attacker.isDead())
							{
								attacker.addExpAndSp(Math.round(attacker.calcStat(Stat.EXPSP_RATE, exp, null, null)), (int) attacker.calcStat(Stat.EXPSP_RATE, sp, null, null));
							}
						}
					}
					else
					{
						// share with party members
						partyDmg = 0;
						partyMul = 1.f;
						partyLvl = 0;
						
						// Get all Creature that can be rewarded in the party
						final List<Playable> rewardedMembers = new ArrayList<>();
						
						// Go through all PlayerInstance in the party
						List<PlayerInstance> groupMembers;
						if (attackerParty.isInCommandChannel())
						{
							groupMembers = attackerParty.getCommandChannel().getMembers();
						}
						else
						{
							groupMembers = attackerParty.getPartyMembers();
						}
						
						for (PlayerInstance pl : groupMembers)
						{
							if ((pl == null) || pl.isDead())
							{
								continue;
							}
							
							// Get the RewardInfo of this PlayerInstance from Attackable rewards
							reward2 = rewards.get(pl);
							
							// If the PlayerInstance is in the Attackable rewards add its damages to party damages
							if (reward2 != null)
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									partyDmg += reward2._dmg; // Add PlayerInstance damages to party damages
									rewardedMembers.add(pl);
									
									if (pl.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = pl.getLevel();
										}
									}
								}
								
								rewards.remove(pl); // Remove the PlayerInstance from the Attackable rewards
							}
							else // Add PlayerInstance of the party (that have attacked or not) to members that can be rewarded and in range of the monster.
							if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
							{
								rewardedMembers.add(pl);
								
								if (pl.getLevel() > partyLvl)
								{
									if (attackerParty.isInCommandChannel())
									{
										partyLvl = attackerParty.getCommandChannel().getLevel();
									}
									else
									{
										partyLvl = pl.getLevel();
									}
								}
							}
							
							final Playable summon = pl.getPet();
							if (summon instanceof PetInstance)
							{
								reward2 = rewards.get(summon);
								if (reward2 != null) // Pets are only added if they have done damage
								{
									if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true))
									{
										partyDmg += reward2._dmg; // Add summon damages to party damages
										rewardedMembers.add(summon);
										
										if (summon.getLevel() > partyLvl)
										{
											partyLvl = summon.getLevel();
										}
									}
									
									rewards.remove(summon); // Remove the summon from the Attackable rewards
								}
							}
						}
						
						// If the party didn't killed this Attackable alone
						if (partyDmg < getMaxHp())
						{
							partyMul = (float) partyDmg / getMaxHp();
						}
						
						// Avoid "over damage"
						if (partyDmg > getMaxHp())
						{
							partyDmg = getMaxHp();
						}
						
						// Calculate the level difference between Party and Attackable
						levelDiff = partyLvl - getLevel();
						
						// Calculate Exp and SP rewards
						tmp = calculateExpAndSp(levelDiff, partyDmg);
						exp = tmp[0];
						sp = tmp[1];
						if (Config.CHAMPION_ENABLE && isChampion())
						{
							exp *= Config.CHAMPION_REWARDS;
							sp *= Config.CHAMPION_REWARDS;
						}
						
						exp *= partyMul;
						sp *= partyMul;
						
						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						if (attacker instanceof PlayerInstance)
						{
							final PlayerInstance player = (PlayerInstance) attacker;
							if (_overhit && (attacker == _overhitAttacker))
							{
								player.sendPacket(SystemMessageId.OVER_HIT);
								exp += calculateOverhitExp(exp);
							}
						}
						
						// Distribute Experience and SP rewards to PlayerInstance Party members in the known area of the last attacker
						if (partyDmg > 0)
						{
							attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl);
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
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the Attackable _aggroList.
	 * @param attacker The Creature that gave damages to this Attackable
	 * @param damage The number of damages given by the attacker Creature
	 */
	public void addDamage(Creature attacker, int damage)
	{
		addDamageHate(attacker, damage, damage);
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the Attackable _aggroList.
	 * @param attacker The Creature that gave damages to this Attackable
	 * @param damage The number of damages given by the attacker Creature
	 * @param aggro The hate (=damage) given by the attacker Creature
	 */
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		// Get the AggroInfo of the attacker Creature from the _aggroList of the Attackable
		AggroInfo ai = _aggroList.get(attacker);
		if (ai == null)
		{
			ai = new AggroInfo(attacker);
			ai._damage = 0;
			ai._hate = 0;
			_aggroList.put(attacker, ai);
		}
		
		// If aggro is negative, its comming from SEE_SPELL, buffs use constant 150
		if (aggro < 0)
		{
			ai._hate -= (aggro * 150) / (getLevel() + 7);
			aggro = -aggro;
		}
		// if damage == 0 -> this is case of adding only to aggro list, dont apply formula on it
		else if (damage == 0)
		{
			ai._hate += aggro;
			// else its damage that must be added using constant 100
		}
		else
		{
			ai._hate += (aggro * 100) / (getLevel() + 7);
		}
		
		// Add new damage and aggro (=damage) to the AggroInfo object
		ai._damage += damage;
		
		// Set the intention to the Attackable to AI_INTENTION_ACTIVE
		if ((getAI() != null) && (aggro > 0) && (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		
		// Notify the Attackable AI with EVT_ATTACKED
		if (damage > 0)
		{
			if (getAI() != null)
			{
				getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
			}
			
			try
			{
				if ((attacker instanceof PlayerInstance) || (attacker instanceof Summon))
				{
					final PlayerInstance player = attacker instanceof PlayerInstance ? (PlayerInstance) attacker : ((Summon) attacker).getOwner();
					for (Quest quest : getTemplate().getEventQuests(EventType.ON_ATTACK))
					{
						quest.notifyAttack(this, player, damage, attacker instanceof Summon);
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	public void reduceHate(Creature target, int amount)
	{
		if (getAI() instanceof SiegeGuardAI)
		{
			// TODO: this just prevents error until siege guards are handled properly
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return;
		}
		if (target == null) // whole aggrolist
		{
			final Creature mostHated = getMostHated();
			if (mostHated == null) // makes target passive for a moment more
			{
				((AttackableAI) getAI()).setGlobalAggro(-25);
				return;
			}
			for (AggroInfo ai : _aggroList.values())
			{
				if (ai == null)
				{
					return;
				}
				ai._hate -= amount;
			}
			
			amount = getHating(mostHated);
			if (amount <= 0)
			{
				((AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			
			return;
		}
		
		final AggroInfo ai = _aggroList.get(target);
		if (ai == null)
		{
			return;
		}
		
		ai._hate -= amount;
		if ((ai._hate <= 0) && (getMostHated() == null))
		{
			((AttackableAI) getAI()).setGlobalAggro(-25);
			clearAggroList();
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			setWalking();
		}
	}
	
	/**
	 * Clears _aggroList hate of the Creature without removing from the list.
	 * @param target
	 */
	public void stopHating(Creature target)
	{
		if (target == null)
		{
			return;
		}
		
		final AggroInfo ai = _aggroList.get(target);
		if (ai == null)
		{
			return;
		}
		
		ai._hate = 0;
	}
	
	/**
	 * Return the most hated Creature of the Attackable _aggroList.<br>
	 * @return
	 */
	public Creature getMostHated()
	{
		if (_aggroList.isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		Creature mostHated = null;
		int maxHate = 0;
		
		// While iterating over This Map Removing Object is Not Allowed
		synchronized (_aggroList)
		{
			// Go through the aggroList of the Attackable
			for (AggroInfo ai : _aggroList.values())
			{
				if (ai == null)
				{
					continue;
				}
				
				if (ai._attacker.isAlikeDead() || !getKnownList().knowsObject(ai._attacker) || !ai._attacker.isVisible() || ((ai._attacker instanceof PlayerInstance) && !((PlayerInstance) ai._attacker).isOnline() && !((PlayerInstance) ai._attacker).isInOfflineMode()))
				{
					ai._hate = 0;
				}
				
				if (ai._hate > maxHate)
				{
					mostHated = ai._attacker;
					maxHate = ai._hate;
				}
			}
		}
		
		if (mostHated != null)
		{
			_mostHated = mostHated;
		}
		
		return mostHated;
	}
	
	/**
	 * Return the hate level of the Attackable against this Creature contained in _aggroList.
	 * @param target The Creature whose hate level must be returned
	 * @return
	 */
	public int getHating(Creature target)
	{
		if (_aggroList.isEmpty())
		{
			return 0;
		}
		
		if (target == null)
		{
			return 0;
		}
		
		final AggroInfo ai = _aggroList.get(target);
		if (ai == null)
		{
			return 0;
		}
		
		if ((ai._attacker instanceof PlayerInstance) && (((PlayerInstance) ai._attacker).getAppearance().isInvisible() || ((PlayerInstance) ai._attacker).isSpawnProtected() || ((PlayerInstance) ai._attacker).isTeleportProtected() || ai._attacker.isInvul()))
		{
			// Remove Object Should Use This Method and Can be Blocked While iterating
			_aggroList.remove(target);
			return 0;
		}
		
		if (!ai._attacker.isVisible())
		{
			_aggroList.remove(target);
			return 0;
		}
		
		if (ai._attacker.isAlikeDead())
		{
			ai._hate = 0;
			return 0;
		}
		
		return ai._hate;
	}
	
	/**
	 * Calculates quantity of items for specific drop according to current situation
	 * @param drop The DropData count is being calculated for
	 * @param lastAttacker The PlayerInstance that has killed the Attackable
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @param isSweep
	 * @return
	 */
	private RewardItem calculateRewardItem(PlayerInstance lastAttacker, DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		if ((Config.HIGH_RATE_SERVER_DROPS && !drop.isQuestDrop() && (drop.getItemId() != 57)))
		{
			// it's not adena-->check if drop has an epic jewel ant queen,orfen,core,frintezza,baium,antharas,valakas,zaken,stones
			if ((drop.getItemId() == 6660) || (drop.getItemId() == 6661) || (drop.getItemId() == 6662) || (drop.getItemId() == 8191) || (drop.getItemId() == 6658) || (drop.getItemId() == 6656) || (drop.getItemId() == 6657) || (drop.getItemId() == 6659) || ((drop.getItemId() >= 6360) && (drop.getItemId() <= 6362)) || ((drop.getItemId() >= 8723) && (drop.getItemId() <= 8762)))
			{
				// if epic jewel, seal stones or life stone, continue
			}
			else
			{
				return null;
			}
		}
		
		float dropChance = 0;
		
		switch (drop.getItemId())
		{
			case 6662: // core ring
			{
				if (Config.CORE_RING_CHANCE > 0)
				{
					dropChance = (10000 * Config.CORE_RING_CHANCE);
				}
				else
				{
					dropChance = drop.getChance();
				}
				break;
			}
			case 6661: // orfen earring
			{
				if (Config.ORFEN_EARRING_CHANCE > 0)
				{
					dropChance = (10000 * Config.ORFEN_EARRING_CHANCE);
				}
				else
				{
					dropChance = drop.getChance();
				}
				break;
			}
			case 6659: // zaken earring
			{
				if (Config.ZAKEN_EARRING_CHANCE > 0)
				{
					dropChance = (10000 * Config.ZAKEN_EARRING_CHANCE);
				}
				else
				{
					dropChance = drop.getChance();
				}
				break;
			}
			case 6660: // aq ring
			{
				if (Config.QA_RING_CHANCE > 0)
				{
					dropChance = (10000 * Config.QA_RING_CHANCE);
				}
				else
				{
					dropChance = drop.getChance();
				}
				break;
			}
			default:
			{
				dropChance = drop.getChance();
			}
		}
		
		int deepBlueDrop = 1;
		if (Config.DEEPBLUE_DROP_RULES && (levelModifier > 0))
		{
			// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
			// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
			deepBlueDrop = 3;
			if (drop.getItemId() == 57)
			{
				deepBlueDrop *= isRaid() ? 1 : Config.RATE_DROP_ITEMS;
			}
		}
		
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if (Config.DEEPBLUE_DROP_RULES)
		{
			dropChance = (drop.getChance() - ((drop.getChance() * levelModifier) / 100)) / deepBlueDrop;
		}
		
		// Applies Drop rates
		if (drop.getItemId() == 57)
		{
			if (this instanceof RaidBossInstance)
			{
				dropChance *= Config.ADENA_RAID;
			}
			else if (this instanceof GrandBossInstance)
			{
				dropChance *= Config.ADENA_BOSS;
			}
			else if (this instanceof MinionInstance)
			{
				dropChance *= Config.ADENA_MINION;
			}
			else
			{
				dropChance *= Config.RATE_DROP_ADENA;
				if (lastAttacker.isDonator())
				{
					dropChance *= Config.DONATOR_ADENA_RATE;
				}
			}
		}
		else if (isSweep)
		{
			if (this instanceof RaidBossInstance)
			{
				dropChance *= Config.SPOIL_RAID;
			}
			else if (this instanceof GrandBossInstance)
			{
				dropChance *= Config.SPOIL_BOSS;
			}
			else if (this instanceof MinionInstance)
			{
				dropChance *= Config.SPOIL_MINION;
			}
			else
			{
				dropChance *= Config.RATE_DROP_SPOIL;
				if (lastAttacker.isDonator())
				{
					dropChance *= Config.DONATOR_SPOIL_RATE;
				}
			}
		}
		else if (this instanceof RaidBossInstance)
		{
			dropChance *= Config.ITEMS_RAID;
		}
		else if (this instanceof GrandBossInstance)
		{
			dropChance *= Config.ITEMS_BOSS;
		}
		else if (this instanceof MinionInstance)
		{
			dropChance *= Config.ITEMS_MINION;
		}
		else
		{
			dropChance *= Config.RATE_DROP_ITEMS;
			if (lastAttacker.isDonator())
			{
				dropChance *= Config.DONATOR_DROP_RATE;
			}
		}
		
		if (Config.CHAMPION_ENABLE && isChampion())
		{
			dropChance *= Config.CHAMPION_REWARDS;
		}
		
		// Round drop chance
		dropChance = Math.round(dropChance);
		
		// Set our limits for chance of drop
		if (dropChance < 1)
		{
			dropChance = 1;
		}
		
		// Get min and max Item quantity that can be dropped in one time
		final int minCount = drop.getMinDrop();
		final int maxCount = drop.getMaxDrop();
		int itemCount = 0;
		
		// Count and chance adjustment for high rate servers
		if ((dropChance > DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
		{
			final int multiplier = (int) dropChance / DropData.MAX_CHANCE;
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount * multiplier, maxCount * multiplier);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount * multiplier;
			}
			else
			{
				itemCount += multiplier;
			}
			
			dropChance = dropChance % DropData.MAX_CHANCE;
		}
		
		// Check if the Item must be dropped
		final int random = Rnd.get(DropData.MAX_CHANCE);
		
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			
			// Prepare for next iteration if dropChance > DropData.MAX_CHANCE
			dropChance -= DropData.MAX_CHANCE;
		}
		if (Config.CHAMPION_ENABLE && isChampion() && ((drop.getItemId() == 57) || ((drop.getItemId() >= 6360) && (drop.getItemId() <= 6362))))
		{
			itemCount *= Config.CHAMPION_ADENAS_REWARDS;
		}
		
		if ((drop.getItemId() >= 6360) && (drop.getItemId() <= 6362))
		{
			itemCount *= Config.RATE_DROP_SEAL_STONES;
		}
		
		if (itemCount > 0)
		{
			return new RewardItem(drop.getItemId(), itemCount);
		}
		
		return null;
	}
	
	/**
	 * Calculates quantity of items for specific drop CATEGORY according to current situation<br>
	 * Only a max of ONE item from a category is allowed to be dropped.
	 * @param lastAttacker The PlayerInstance that has killed the Attackable
	 * @param categoryDrops
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @return
	 */
	private RewardItem calculateCategorizedRewardItem(PlayerInstance lastAttacker, DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
		{
			return null;
		}
		
		if ((Config.HIGH_RATE_SERVER_DROPS && (categoryDrops.getCategoryType() != 0)))
		{
			// it's not adena-->check if drop is quest or is an epic jewel
			boolean doDrop = false;
			for (DropData dd : categoryDrops.getAllDrops())
			{
				// quest_drop,ant queen,orfen,core,frintezza,baium,antharas,valakas,zaken, seal Stones, life stones
				if (dd.isQuestDrop() || (dd.getItemId() == 6660) || (dd.getItemId() == 6661) || (dd.getItemId() == 6662) || (dd.getItemId() == 8191) || (dd.getItemId() == 6658) || (dd.getItemId() == 6656) || (dd.getItemId() == 6657) || (dd.getItemId() == 6659) || ((dd.getItemId() >= 6360) && (dd.getItemId() <= 6362)) || ((dd.getItemId() >= 8723) && (dd.getItemId() <= 8762)))
				{
					// if epic jewel, return just 1 from raid
					doDrop = true;
				}
			}
			
			if (!doDrop)
			{
				return null;
			}
		}
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		final int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		int deepBlueDrop = 1;
		if (Config.DEEPBLUE_DROP_RULES && (levelModifier > 0))
		{
			// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
			// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
			deepBlueDrop = 3;
		}
		
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if (Config.DEEPBLUE_DROP_RULES)
		{
			categoryDropChance = (categoryDropChance - ((categoryDropChance * levelModifier) / 100)) / deepBlueDrop;
		}
		
		// Applies Drop rates
		if (this instanceof RaidBossInstance)
		{
			categoryDropChance *= Config.ITEMS_RAID;
		}
		else if (this instanceof GrandBossInstance)
		{
			categoryDropChance *= Config.ITEMS_BOSS;
		}
		else if (this instanceof MinionInstance)
		{
			categoryDropChance *= Config.ITEMS_MINION;
		}
		else
		{
			categoryDropChance *= Config.RATE_DROP_ITEMS;
		}
		
		if (Config.CHAMPION_ENABLE && isChampion())
		{
			categoryDropChance *= Config.CHAMPION_REWARDS;
		}
		
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(DropData.MAX_CHANCE) < categoryDropChance)
		{
			final DropData drop = categoryDrops.dropOne(isRaid());
			if (drop == null)
			{
				return null;
			}
			
			// Now decide the quantity to drop based on the rates and penalties. To get this value
			// simply divide the modified categoryDropChance by the base category chance. This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.
			//
			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again. If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure. So the chance will be adjusted to 100%
			// if smaller.
			long dropChance = 0;
			
			switch (drop.getItemId())
			{
				case 6662: // core ring
				{
					if (Config.CORE_RING_CHANCE > 0)
					{
						dropChance = (10000 * Config.CORE_RING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
					break;
				}
				case 6661: // orfen earring
				{
					if (Config.ORFEN_EARRING_CHANCE > 0)
					{
						dropChance = (10000 * Config.ORFEN_EARRING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
					break;
				}
				case 6659: // zaken earring
				{
					if (Config.ZAKEN_EARRING_CHANCE > 0)
					{
						dropChance = (10000 * Config.ZAKEN_EARRING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
					break;
				}
				case 6660: // aq ring
				{
					if (Config.QA_RING_CHANCE > 0)
					{
						dropChance = (10000 * Config.QA_RING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
					break;
				}
				default:
				{
					dropChance = drop.getChance();
				}
			}
			
			if (drop.getItemId() == 57)
			{
				if (this instanceof RaidBossInstance)
				{
					dropChance *= Config.ADENA_RAID;
				}
				else if (this instanceof GrandBossInstance)
				{
					dropChance *= Config.ADENA_BOSS;
				}
				else if (this instanceof MinionInstance)
				{
					dropChance *= Config.ADENA_MINION;
				}
				else
				{
					dropChance *= Config.RATE_DROP_ADENA;
					if (lastAttacker.isDonator())
					{
						dropChance *= Config.DONATOR_ADENA_RATE;
					}
				}
			}
			else if (this instanceof RaidBossInstance)
			{
				dropChance *= Config.ITEMS_RAID;
			}
			else if (this instanceof GrandBossInstance)
			{
				dropChance *= Config.ITEMS_BOSS;
			}
			else if (this instanceof MinionInstance)
			{
				dropChance *= Config.ITEMS_MINION;
			}
			else
			{
				dropChance *= Config.RATE_DROP_ITEMS;
				if (lastAttacker.isDonator())
				{
					dropChance *= Config.DONATOR_DROP_RATE;
				}
			}
			
			if (Config.CHAMPION_ENABLE && isChampion())
			{
				dropChance *= Config.CHAMPION_REWARDS;
			}
			
			if (dropChance < DropData.MAX_CHANCE)
			{
				dropChance = DropData.MAX_CHANCE;
			}
			
			// Get min and max Item quantity that can be dropped in one time
			final int min = drop.getMinDrop();
			final int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Count and chance adjustment for high rate servers
			if ((dropChance > DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
			{
				final long multiplier = dropChance / DropData.MAX_CHANCE;
				if (min < max)
				{
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				}
				else if (min == max)
				{
					itemCount += min * multiplier;
				}
				else
				{
					itemCount += multiplier;
				}
				
				dropChance = dropChance % DropData.MAX_CHANCE;
			}
			
			// Check if the Item must be dropped
			final int random = Rnd.get(DropData.MAX_CHANCE);
			
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
				{
					itemCount += Rnd.get(min, max);
				}
				else if (min == max)
				{
					itemCount += min;
				}
				else
				{
					itemCount++;
				}
				
				// Prepare for next iteration if dropChance > DropData.MAX_CHANCE
				dropChance -= DropData.MAX_CHANCE;
			}
			if (Config.CHAMPION_ENABLE && isChampion() && ((drop.getItemId() == 57) || ((drop.getItemId() >= 6360) && (drop.getItemId() <= 6362))))
			{
				itemCount *= Config.CHAMPION_ADENAS_REWARDS;
			}
			
			if ((drop.getItemId() >= 6360) && (drop.getItemId() <= 6362))
			{
				itemCount *= Config.RATE_DROP_SEAL_STONES;
			}
			
			if (itemCount > 0)
			{
				return new RewardItem(drop.getItemId(), itemCount);
			}
		}
		return null;
	}
	
	/**
	 * Calculates the level modifier for drop
	 * @param lastAttacker The PlayerInstance that has killed the Attackable
	 * @return
	 */
	private int calculateLevelModifierForDrop(PlayerInstance lastAttacker)
	{
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int highestLevel = lastAttacker.getLevel();
			
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			if ((getAttackByList() != null) && !getAttackByList().isEmpty())
			{
				for (Creature atkChar : getAttackByList())
				{
					if ((atkChar != null) && (atkChar.getLevel() > highestLevel))
					{
						highestLevel = atkChar.getLevel();
					}
				}
			}
			
			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if ((highestLevel - 9) >= getLevel())
			{
				return (highestLevel - (getLevel() + 8)) * 9;
			}
		}
		
		return 0;
	}
	
	public void doItemDrop(Creature lastAttacker)
	{
		doItemDrop(getTemplate(), lastAttacker);
	}
	
	/**
	 * Manage Base, Quests and Special Events drops of Attackable (called by calculateRewards).<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * During a Special Event all Attackable can drop extra Items. Those extra Items are defined in the table <b>allNpcDateDrops</b> of the EventDroplist. Each Special Event has a start and end date to stop to drop extra Items automaticaly.<br>
	 * <br>
	 * <b><u>Actions</u> : </b><br>
	 * <li>Manage drop of Special Events created by GM for a defined period</li>
	 * <li>Get all possible drops of this Attackable from NpcTemplate and add it Quest drops</li>
	 * <li>For each possible drops (base + quests), calculate which one must be dropped (random)</li>
	 * <li>Get each Item quantity dropped (random)</li>
	 * <li>Create this or these ItemInstance corresponding to each Item Identifier dropped</li>
	 * <li>If the autoLoot mode is actif and if the Creature that has killed the Attackable is a PlayerInstance, give this or these Item(s) to the PlayerInstance that has killed the Attackable</li>
	 * <li>If the autoLoot mode isn't actif or if the Creature that has killed the Attackable is not a PlayerInstance, add this or these Item(s) in the world as a visible object at the position where mob was last</li><br>
	 * @param npcTemplate
	 * @param lastAttacker The Creature that has killed the Attackable
	 */
	public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker)
	{
		PlayerInstance player = null;
		if (lastAttacker instanceof PlayerInstance)
		{
			player = (PlayerInstance) lastAttacker;
		}
		else if (lastAttacker instanceof Summon)
		{
			player = ((Summon) lastAttacker).getOwner();
		}
		
		// Don't drop anything if the last attacker or owner isn't PlayerInstance
		if (player == null)
		{
			return;
		}
		
		final int levelModifier = calculateLevelModifierForDrop(player); // level modifier in %'s (will be subtracted from drop chance)
		
		// Check the drop of a cursed weapon
		if ((levelModifier == 0) && (player.getLevel() > 20))
		{
			CursedWeaponsManager.getInstance().checkDrop(this, player);
		}
		
		// now throw all categorized drops and handle spoil.
		for (DropCategory cat : npcTemplate.getDropData())
		{
			RewardItem item = null;
			if (cat.isSweep())
			{
				// according to sh1ny, seeded mobs CAN be spoiled and swept.
				if (isSpoil()/* && !_seeded */)
				{
					final List<RewardItem> sweepList = new ArrayList<>();
					for (DropData drop : cat.getAllDrops())
					{
						item = calculateRewardItem(player, drop, levelModifier, true);
						if (item == null)
						{
							continue;
						}
						
						sweepList.add(item);
					}
					
					// Set the table _sweepItems of this Attackable
					if (!sweepList.isEmpty())
					{
						_sweepItems = sweepList.toArray(new RewardItem[sweepList.size()]);
					}
				}
			}
			else
			{
				if (_seeded)
				{
					final DropData drop = cat.dropSeedAllowedDropsOnly();
					if (drop == null)
					{
						continue;
					}
					
					item = calculateRewardItem(player, drop, levelModifier, false);
				}
				else
				{
					item = calculateCategorizedRewardItem(player, cat, levelModifier);
				}
				
				if (item != null)
				{
					// Check if the autoLoot mode is active
					if (Config.AUTO_LOOT)
					{
						final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
						if (itemTemplate == null)
						{
							LOGGER.info("ERROR: Item id to autoloot " + item.getItemId() + " has not template into items/armor/weapon tables.. It cannot be dropped..");
						}
						else if (!player.getInventory().validateCapacity(itemTemplate) || (!Config.AUTO_LOOT_BOSS && (this instanceof RaidBossInstance)) || (!Config.AUTO_LOOT_BOSS && (this instanceof GrandBossInstance)))
						{
							DropItem(player, item);
						}
						else
						{
							player.doAutoLoot(this, item); // Give this or these Item(s) to the PlayerInstance that has killed the Attackable
						}
					}
					else
					{
						DropItem(player, item); // drop the item on the ground
					}
					
					// Broadcast message if RaidBoss was defeated
					if ((this instanceof RaidBossInstance) || (this instanceof GrandBossInstance))
					{
						SystemMessage sm;
						sm = new SystemMessage(SystemMessageId.S1_DIED_AND_DROPPED_S3_S2);
						sm.addString(getName());
						sm.addItemName(item.getItemId());
						sm.addNumber(item.getCount());
						broadcastPacket(sm);
					}
				}
			}
		}
		
		// Apply Special Item drop with rnd qty for champions
		if (Config.CHAMPION_ENABLE && isChampion() && (player.getLevel() <= (getLevel() + 3)) && (Config.CHAMPION_REWARD > 0) && (Rnd.get(100) < Config.CHAMPION_REWARD))
		{
			int champqty = Rnd.get(Config.CHAMPION_REWARD_QTY);
			champqty++; // quantity should actually vary between 1 and whatever admin specified as max, inclusive.
			
			// Give this or these Item(s) to the PlayerInstance that has killed the Attackable
			final RewardItem item = new RewardItem(Config.CHAMPION_REWARD_ID, champqty);
			if (Config.AUTO_LOOT)
			{
				final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
				if (!player.getInventory().validateCapacity(itemTemplate))
				{
					DropItem(player, item);
				}
				else
				{
					player.addItem("ChampionLoot", item.getItemId(), item.getCount(), this, true);
				}
			}
			else
			{
				DropItem(player, item);
			}
		}
		
		// Instant Item Drop :>
		final double rateHp = getStat().calcStat(Stat.MAX_HP, 1, this, null);
		if ((rateHp < 2) && npcTemplate.getType().contentEquals("Monster")) // only Monster with <= 1x HP can drop herbs
		{
			boolean hp = false;
			boolean mp = false;
			boolean spec = false;
			
			// ptk - patk type enhance
			int random = Rnd.get(1000); // note *10
			if ((random < Config.RATE_DROP_SPECIAL_HERBS) && !spec) // && !_spec useless yet
			{
				final RewardItem item = new RewardItem(8612, 1); // Herb of Warrior
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
				{
					final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
					if (!player.getInventory().validateCapacity(itemTemplate))
					{
						DropItem(player, item);
					}
					else
					{
						player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
					}
				}
				else
				{
					DropItem(player, item);
				}
				spec = true;
			}
			else
			{
				for (int i = 0; i < 3; i++)
				{
					random = Rnd.get(100);
					if (random < Config.RATE_DROP_COMMON_HERBS)
					{
						RewardItem item = null;
						if (i == 0)
						{
							item = new RewardItem(8606, 1); // Herb of Power
						}
						if (i == 1)
						{
							item = new RewardItem(8608, 1); // Herb of Atk. Spd.
						}
						if (i == 2)
						{
							item = new RewardItem(8610, 1); // Herb of Critical Attack
						}
						
						if (item == null)
						{
							break;
						}
						
						if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						{
							final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
							if (!player.getInventory().validateCapacity(itemTemplate))
							{
								DropItem(player, item);
							}
							else
							{
								player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
							}
						}
						else
						{
							DropItem(player, item);
						}
						break;
					}
				}
			}
			
			// mtk - matk type enhance
			random = Rnd.get(1000); // note *10
			if ((random < Config.RATE_DROP_SPECIAL_HERBS) && !spec)
			{
				final RewardItem item = new RewardItem(8613, 1); // Herb of Mystic
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
				{
					final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
					if (!player.getInventory().validateCapacity(itemTemplate))
					{
						DropItem(player, item);
					}
					else
					{
						player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
					}
				}
				else
				{
					DropItem(player, item);
				}
				spec = true;
			}
			else
			{
				for (int i = 0; i < 2; i++)
				{
					random = Rnd.get(100);
					if (random < Config.RATE_DROP_COMMON_HERBS)
					{
						RewardItem item = null;
						if (i == 0)
						{
							item = new RewardItem(8607, 1); // Herb of Magic
						}
						if (i == 1)
						{
							item = new RewardItem(8609, 1); // Herb of Casting Speed
						}
						if (item == null)
						{
							break;
						}
						if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						{
							final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
							if (!player.getInventory().validateCapacity(itemTemplate))
							{
								DropItem(player, item);
							}
							else
							{
								player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
							}
						}
						else
						{
							DropItem(player, item);
						}
						break;
					}
				}
			}
			
			// hp + mp type
			random = Rnd.get(1000); // note *10
			if ((random < Config.RATE_DROP_SPECIAL_HERBS) && !spec)
			{
				final RewardItem item = new RewardItem(8614, 1); // Herb of Recovery
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
				{
					final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
					if (!player.getInventory().validateCapacity(itemTemplate))
					{
						DropItem(player, item);
					}
					else
					{
						player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
					}
				}
				else
				{
					DropItem(player, item);
				}
				
				mp = true;
				hp = true;
				spec = true;
			}
			
			// hp - restore hp type
			if (!hp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_MP_HP_HERBS)
				{
					final RewardItem item = new RewardItem(8600, 1); // Herb of Life
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					{
						final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
						if (!player.getInventory().validateCapacity(itemTemplate))
						{
							DropItem(player, item);
						}
						else
						{
							player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
						}
					}
					else
					{
						DropItem(player, item);
					}
					hp = true;
				}
			}
			if (!hp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_GREATER_HERBS)
				{
					final RewardItem item = new RewardItem(8601, 1); // Greater Herb of Life
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					{
						final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
						if (!player.getInventory().validateCapacity(itemTemplate))
						{
							DropItem(player, item);
						}
						else
						{
							player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
						}
					}
					else
					{
						DropItem(player, item);
					}
					hp = true;
				}
			}
			if (!hp)
			{
				random = Rnd.get(1000); // note *10
				if (random < Config.RATE_DROP_SUPERIOR_HERBS)
				{
					final RewardItem item = new RewardItem(8602, 1); // Superior Herb of Life
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					{
						final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
						if (!player.getInventory().validateCapacity(itemTemplate))
						{
							DropItem(player, item);
						}
						else
						{
							player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
						}
					}
					else
					{
						DropItem(player, item);
					}
				}
			}
			
			// mp - restore mp type
			if (!mp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_MP_HP_HERBS)
				{
					final RewardItem item = new RewardItem(8603, 1); // Herb of Mana
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					{
						final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
						if (!player.getInventory().validateCapacity(itemTemplate))
						{
							DropItem(player, item);
						}
						else
						{
							player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
						}
					}
					else
					{
						DropItem(player, item);
					}
					mp = true;
				}
			}
			if (!mp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_GREATER_HERBS)
				{
					final RewardItem item = new RewardItem(8604, 1); // Greater Herb of Mana
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					{
						final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
						if (!player.getInventory().validateCapacity(itemTemplate))
						{
							DropItem(player, item);
						}
						else
						{
							player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
						}
					}
					else
					{
						DropItem(player, item);
					}
					mp = true;
				}
			}
			if (!mp)
			{
				random = Rnd.get(1000); // note *10
				if (random < Config.RATE_DROP_SUPERIOR_HERBS)
				{
					final RewardItem item = new RewardItem(8605, 1); // Superior Herb of Mana
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					{
						final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
						if (!player.getInventory().validateCapacity(itemTemplate))
						{
							DropItem(player, item);
						}
						else
						{
							player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
						}
					}
					else
					{
						DropItem(player, item);
					}
				}
			}
			
			// speed enhance type
			random = Rnd.get(100);
			if (random < Config.RATE_DROP_COMMON_HERBS)
			{
				final RewardItem item = new RewardItem(8611, 1); // Herb of Speed
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
				{
					final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
					if (!player.getInventory().validateCapacity(itemTemplate))
					{
						DropItem(player, item);
					}
					else
					{
						player.addItem("AutoLoot", item.getItemId(), item.getCount(), this, true);
					}
				}
				else
				{
					DropItem(player, item);
				}
			}
		}
	}
	
	/**
	 * Manage Special Events drops created by GM for a defined period.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * During a Special Event all Attackable can drop extra Items. Those extra Items are defined in the table <b>allNpcDateDrops</b> of the EventDroplist. Each Special Event has a start and end date to stop to drop extra Items automaticaly.<br>
	 * <br>
	 * <b><u>Actions</u> : <i>If an extra drop must be generated</i></b><br>
	 * <li>Get an Item Identifier (random) from the DateDrop Item table of this Event</li>
	 * <li>Get the Item quantity dropped (random)</li>
	 * <li>Create this or these ItemInstance corresponding to this Item Identifier</li>
	 * <li>If the autoLoot mode is actif and if the Creature that has killed the Attackable is a PlayerInstance, give this or these Item(s) to the PlayerInstance that has killed the Attackable</li>
	 * <li>If the autoLoot mode isn't actif or if the Creature that has killed the Attackable is not a PlayerInstance, add this or these Item(s) in the world as a visible object at the position where mob was last</li><br>
	 * @param lastAttacker The Creature that has killed the Attackable
	 */
	public void doEventDrop(Creature lastAttacker)
	{
		PlayerInstance player = null;
		if (lastAttacker instanceof PlayerInstance)
		{
			player = (PlayerInstance) lastAttacker;
		}
		else if (lastAttacker instanceof Summon)
		{
			player = ((Summon) lastAttacker).getOwner();
		}
		
		if (player == null)
		{
			return; // Don't drop anything if the last attacker or owner isn't PlayerInstance
		}
		
		if ((player.getLevel() - getLevel()) > 9)
		{
			return;
		}
		
		// Go through DateDrop of EventDroplist allNpcDateDrops within the date range
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
		{
			if (Rnd.get(DropData.MAX_CHANCE) < drop.chance)
			{
				final RewardItem item = new RewardItem(drop.items[Rnd.get(drop.items.length)], Rnd.get(drop.min, drop.max));
				if (Config.AUTO_LOOT)
				{
					final Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
					if (!player.getInventory().validateCapacity(itemTemplate))
					{
						DropItem(player, item);
					}
					else
					{
						player.doAutoLoot(this, item); // Give this or these Item(s) to the PlayerInstance that has killed the Attackable
					}
				}
				else
				{
					DropItem(player, item); // drop the item on the ground
				}
			}
		}
	}
	
	/**
	 * Drop reward item.
	 * @param mainDamageDealer
	 * @param item
	 * @return
	 */
	public ItemInstance DropItem(PlayerInstance mainDamageDealer, RewardItem item)
	{
		final int randDropLim = 70;
		ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			// Randomize drop position
			final int newX = (getX() + Rnd.get((randDropLim * 2) + 1)) - randDropLim;
			final int newY = (getY() + Rnd.get((randDropLim * 2) + 1)) - randDropLim;
			final int newZ = Math.max(getZ(), mainDamageDealer.getZ()) + 20; // TODO: temp hack, do something nicer when we have geodata
			
			// Init the dropped ItemInstance and add it in the world as a visible object at the position where mob was last
			ditem = ItemTable.getInstance().createItem("Loot", item.getItemId(), item.getCount(), mainDamageDealer, this);
			ditem.getDropProtection().protect(mainDamageDealer);
			ditem.dropMe(this, newX, newY, newZ);
			
			// Add drop to auto destroy item task
			if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()) && (((Config.AUTODESTROY_ITEM_AFTER > 0) && (ditem.getItemType() != EtcItemType.HERB)) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (ditem.getItemType() == EtcItemType.HERB))))
			{
				ItemsAutoDestroy.getInstance().addItem(ditem);
			}
			
			ditem.setProtected(false);
			
			// If stackable, end loop as entire count is included in 1 instance of item
			if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
			{
				break;
			}
		}
		return ditem;
	}
	
	public ItemInstance DropItem(PlayerInstance lastAttacker, int itemId, int itemCount)
	{
		return DropItem(lastAttacker, new RewardItem(itemId, itemCount));
	}
	
	/**
	 * Return the active weapon of this Attackable (= null).<br>
	 * @return
	 */
	public ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	/**
	 * Return True if the _aggroList of this Attackable is Empty.<br>
	 * @return
	 */
	public boolean noTarget()
	{
		return _aggroList.isEmpty();
	}
	
	/**
	 * Return True if the _aggroList of this Attackable contains the Creature.
	 * @param creature The Creature searched in the _aggroList of the Attackable
	 * @return
	 */
	public boolean containsTarget(Creature creature)
	{
		return _aggroList.containsKey(creature);
	}
	
	/**
	 * Clear the _aggroList of the Attackable.
	 */
	public void clearAggroList()
	{
		_aggroList.clear();
	}
	
	/**
	 * Return True if a Dwarf use Sweep on the Attackable and if item can be spoiled.<br>
	 * @return
	 */
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}
	
	/**
	 * Return table containing all ItemInstance that can be spoiled.<br>
	 * @return
	 */
	public synchronized RewardItem[] takeSweep()
	{
		final RewardItem[] sweep = _sweepItems;
		_sweepItems = null;
		return sweep;
	}
	
	/**
	 * Return table containing all ItemInstance that can be harvested.<br>
	 * @return
	 */
	public synchronized RewardItem[] takeHarvest()
	{
		final RewardItem[] harvest = _harvestItems;
		_harvestItems = null;
		return harvest;
	}
	
	/**
	 * Set the over-hit flag on the Attackable.
	 * @param status The status of the over-hit flag
	 */
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}
	
	/**
	 * Set the over-hit values like the attacker who did the strike and the ammount of damage done by the skill.
	 * @param attacker The Creature who hit on the Attackable using the over-hit enabled skill
	 * @param damage The ammount of damage done by the over-hit enabled skill on the Attackable
	 */
	public void setOverhitValues(Creature attacker, double damage)
	{
		// Calculate the over-hit damage
		// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
		final double overhitDmg = (getCurrentHp() - damage) * -1;
		if (overhitDmg < 0)
		{
			// we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
			// let's just clear all the over-hit related values
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}
	
	/**
	 * Return the Creature who hit on the Attackable using an over-hit enabled skill.
	 * @return Creature attacker
	 */
	public Creature getOverhitAttacker()
	{
		return _overhitAttacker;
	}
	
	/**
	 * Return the ammount of damage done on the Attackable using an over-hit enabled skill.
	 * @return double damage
	 */
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}
	
	/**
	 * Return True if the Attackable was hit by an over-hit enabled skill.<br>
	 * @return
	 */
	public boolean isOverhit()
	{
		return _overhit;
	}
	
	/**
	 * Activate the absorbed soul condition on the Attackable.
	 */
	public void absorbSoul()
	{
		_absorbed = true;
	}
	
	/**
	 * Return True if the Attackable had his soul absorbed.<br>
	 * @return
	 */
	public boolean isAbsorbed()
	{
		return _absorbed;
	}
	
	/**
	 * Adds an attacker that successfully absorbed the soul of this Attackable into the _absorbersList.
	 * @param attacker - a valid PlayerInstance
	 * @param crystalId
	 */
	public void addAbsorber(PlayerInstance attacker, int crystalId)
	{
		// This just works for targets like MonsterInstance
		if (!(this instanceof MonsterInstance))
		{
			return;
		}
		
		// The attacker must not be null
		if (attacker == null)
		{
			return;
		}
		
		// This Attackable must be of one type in the _absorbingMOBS_levelXX tables.
		// OBS: This is done so to avoid triggering the absorbed conditions for mobs that can't be absorbed.
		if (getAbsorbLevel() == 0)
		{
			return;
		}
		
		// If we have no _absorbersList initiated, do it
		AbsorberInfo ai = _absorbersList.get(attacker);
		
		// If the Creature attacker isn't already in the _absorbersList of this Attackable, add it
		if (ai == null)
		{
			ai = new AbsorberInfo(attacker, crystalId, getCurrentHp());
			_absorbersList.put(attacker, ai);
		}
		else
		{
			ai._absorber = attacker;
			ai._crystalId = crystalId;
			ai._absorbedHP = getCurrentHp();
		}
		
		// Set this Attackable as absorbed
		absorbSoul();
	}
	
	/**
	 * Calculate the leveling chance of Soul Crystals based on the attacker that killed this Attackable
	 * @param attacker The player that last killed this Attackable $ Rewrite 06.12.06 - Yesod
	 */
	private void levelSoulCrystals(Creature attacker)
	{
		// Only PlayerInstance can absorb a soul
		if (!(attacker instanceof PlayerInstance) && !(attacker instanceof Summon))
		{
			resetAbsorbList();
			return;
		}
		
		final int maxAbsorbLevel = getAbsorbLevel();
		int minAbsorbLevel = 0;
		
		// If this is not a valid Attackable, clears the _absorbersList and just return
		if (maxAbsorbLevel == 0)
		{
			resetAbsorbList();
			return;
		}
		
		// All boss mobs with maxAbsorbLevel 13 have minAbsorbLevel of 12 else 10
		if (maxAbsorbLevel > 10)
		{
			minAbsorbLevel = maxAbsorbLevel > 12 ? 12 : 10;
		}
		
		// Init some useful vars
		boolean isSuccess = true;
		boolean doLevelup = true;
		final boolean isBossMob = maxAbsorbLevel > 10;
		final NpcTemplate.AbsorbCrystalType absorbType = getTemplate().getAbsorbType();
		final PlayerInstance killer = attacker instanceof Summon ? ((Summon) attacker).getOwner() : (PlayerInstance) attacker;
		
		// If this mob is a boss, then skip some checkings
		if (!isBossMob)
		{
			// Fail if this Attackable isn't absorbed or there's no one in its _absorbersList
			if (!_absorbed /* || _absorbersList == null */)
			{
				resetAbsorbList();
				return;
			}
			
			// Fail if the killer isn't in the _absorbersList of this Attackable and mob is not boss
			final AbsorberInfo ai = _absorbersList.get(killer);
			if ((ai == null) || (ai._absorber.getObjectId() != killer.getObjectId()))
			{
				isSuccess = false;
			}
			
			// Check if the soul crystal was used when HP of this Attackable wasn't higher than half of it
			if ((ai != null) && (ai._absorbedHP > (getMaxHp() / 2.0)))
			{
				isSuccess = false;
			}
			
			if (!isSuccess)
			{
				resetAbsorbList();
				return;
			}
		}
		
		// ********
		String[] crystalNFO = null;
		String crystalNME = "";
		
		final int dice = Rnd.get(100);
		int crystalQTY = 0;
		int crystalLVL = 0;
		int crystalOLD = 0;
		int crystalNEW = 0;
		
		// ********
		// Now we have four choices:
		// 1- The Monster level is too low for the crystal. Nothing happens.
		// 2- Everything is correct, but it failed. Nothing happens. (57.5%)
		// 3- Everything is correct, but it failed. The crystal scatters. A sound event is played. (10%)
		// 4- Everything is correct, the crystal level up. A sound event is played. (32.5%)
		List<PlayerInstance> players = new ArrayList<>();
		if ((absorbType == NpcTemplate.AbsorbCrystalType.FULL_PARTY) && killer.isInParty())
		{
			players = killer.getParty().getPartyMembers();
		}
		else if ((absorbType == NpcTemplate.AbsorbCrystalType.PARTY_ONE_RANDOM) && killer.isInParty())
		{
			// This is a naive method for selecting a random member. It gets any random party member and
			// then checks if the member has a valid crystal. It does not select the random party member
			// among those who have crystals, only. However, this might actually be correct (same as retail).
			players.add(killer.getParty().getPartyMembers().get(Rnd.get(killer.getParty().getMemberCount())));
		}
		else
		{
			players.add(killer);
		}
		
		for (PlayerInstance player : players)
		{
			if (player == null)
			{
				continue;
			}
			
			crystalQTY = 0;
			
			final ItemInstance[] inv = player.getInventory().getItems();
			for (ItemInstance item : inv)
			{
				final int itemId = item.getItemId();
				for (int id : SoulCrystal.SOUL_CRYSTAL_TABLE)
				{
					// Find any of the 39 possible crystals.
					if (id == itemId)
					{
						crystalQTY++;
						// Keep count but make sure the player has no more than 1 crystal
						if (crystalQTY > 1)
						{
							isSuccess = false;
							break;
						}
						
						// Validate if the crystal has already leveled
						if ((id != SoulCrystal.RED_NEW_CRYSTAL) && (id != SoulCrystal.GRN_NEW_CYRSTAL) && (id != SoulCrystal.BLU_NEW_CRYSTAL))
						{
							try
							{
								if (item.getItem().getName().contains("Grade"))
								{
									// Split the name of the crystal into 'name' & 'level'
									crystalNFO = item.getItem().getName().trim().replace(" Grade ", "-").split("-");
									// Set Level to 13
									crystalLVL = 13;
									// Get Name
									crystalNME = crystalNFO[0].toLowerCase();
								}
								else
								{
									// Split the name of the crystal into 'name' & 'level'
									crystalNFO = item.getItem().getName().trim().replace(" Stage ", "").split("-");
									// Get Level
									crystalLVL = Integer.parseInt(crystalNFO[1].trim());
									// Get Name
									crystalNME = crystalNFO[0].toLowerCase();
								}
								// Allocate current and levelup ids' for higher level crystals
								if (crystalLVL > 9)
								{
									for (int[] element : SoulCrystal.HIGH_SOUL_CONVERT)
									{
										// Get the next stage above 10 using array.
										if (id == element[0])
										{
											crystalNEW = element[1];
											break;
										}
									}
								}
								else
								{
									crystalNEW = id + 1;
								}
							}
							catch (NumberFormatException nfe)
							{
								LOGGER.warning("An attempt to identify a soul crystal failed, verify the names have not changed in etcitem table. " + nfe);
								player.sendMessage("There has been an error handling your soul crystal. Please notify your server admin.");
								isSuccess = false;
								break;
							}
							catch (Exception e)
							{
								isSuccess = false;
								break;
							}
						}
						else
						{
							crystalNME = item.getItem().getName().toLowerCase().trim();
							crystalNEW = id + 1;
						}
						
						// Done
						crystalOLD = id;
						break;
					}
				}
				
				if (!isSuccess)
				{
					break;
				}
			}
			
			// If the crystal level is way too high for this mob, say that we can't increase it
			if ((crystalLVL < minAbsorbLevel) || (crystalLVL >= maxAbsorbLevel))
			{
				doLevelup = false;
			}
			
			// The player doesn't have any crystals with him get to the next player.
			if ((crystalQTY < 1) || (crystalQTY > 1) || !isSuccess || !doLevelup)
			{
				// Too many crystals in inventory.
				if (crystalQTY > 1)
				{
					player.sendPacket(SystemMessageId.THE_SOUL_CRYSTALS_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL);
				}
				// The soul crystal stage of the player is way too high
				// Like L2OFF message must not appear if char hasn't crystal on inventory
				else if (!doLevelup && (crystalQTY > 0))
				{
					player.sendPacket(SystemMessageId.THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_A_SOUL);
				}
				
				crystalQTY = 0;
				continue;
			}
			
			/*
			 * TODO: Confirm boss chance for crystal level up and for crystal breaking. It is known that bosses with FULL_PARTY crystal level ups have 100% success rate, but this is not the case for the other bosses (one-random or last-hit). While not confirmed, it is most reasonable that crystals
			 * leveled up at bosses will never break. Also, the chance to level up is guessed as around 70% if not higher.
			 */
			final int chanceLevelUp = isBossMob ? 70 : SoulCrystal.LEVEL_CHANCE;
			
			// If succeeds or it is a full party absorb, level up the crystal.
			if (((absorbType == NpcTemplate.AbsorbCrystalType.FULL_PARTY) && doLevelup) || (dice <= chanceLevelUp))
			{
				// Give staged crystal
				exchangeCrystal(player, crystalOLD, crystalNEW, false);
			}
			
			// If true and not a last-hit mob, break the crystal.
			else if (!isBossMob && (dice >= (100.0 - SoulCrystal.BREAK_CHANCE)))
			{
				// Remove current crystal an give a broken open.
				if (crystalNME.startsWith("red"))
				{
					exchangeCrystal(player, crystalOLD, SoulCrystal.RED_BROKEN_CRYSTAL, true);
				}
				else if (crystalNME.startsWith("gre"))
				{
					exchangeCrystal(player, crystalOLD, SoulCrystal.GRN_BROKEN_CYRSTAL, true);
				}
				else if (crystalNME.startsWith("blu"))
				{
					exchangeCrystal(player, crystalOLD, SoulCrystal.BLU_BROKEN_CRYSTAL, true);
				}
				
				resetAbsorbList();
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_A_SOUL);
			}
		}
	}
	
	private void exchangeCrystal(PlayerInstance player, int takeid, int giveid, boolean broke)
	{
		ItemInstance item = player.getInventory().destroyItemByItemId("SoulCrystal", takeid, 1, player, this);
		if (item != null)
		{
			// Prepare inventory update packet
			final InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addRemovedItem(item);
			
			// Add new crystal to the killer's inventory
			item = player.getInventory().addItem("SoulCrystal", giveid, 1, player, this);
			playerIU.addItem(item);
			
			// Send a sound event and text message to the player
			if (broke)
			{
				player.sendPacket(SystemMessageId.THE_SOUL_CRYSTAL_BROKE_BECAUSE_IT_WAS_NOT_ABLE_TO_ENDURE_THE_SOUL_ENERGY);
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL);
			}
			
			// Send system message
			final SystemMessage sms = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
			sms.addItemName(giveid);
			player.sendPacket(sms);
			
			// Send inventory update packet
			player.sendPacket(playerIU);
		}
	}
	
	private void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}
	
	/**
	 * Calculate the Experience and SP to distribute to attacker (PlayerInstance, SummonInstance or Party) of the Attackable.
	 * @param diff The difference of level between attacker (PlayerInstance, SummonInstance or Party) and the Attackable
	 * @param damage The damages given by the attacker (PlayerInstance, SummonInstance or Party)
	 * @return
	 */
	private int[] calculateExpAndSp(int diff, long damage)
	{
		double xp;
		double sp;
		if (diff < -5)
		{
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}
		
		xp = (getExpReward() * damage) / getMaxHp();
		if (Config.ALT_GAME_EXPONENT_XP != 0)
		{
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}
		
		sp = (getSpReward() * damage) / getMaxHp();
		if (Config.ALT_GAME_EXPONENT_SP != 0)
		{
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		
		if ((Config.ALT_GAME_EXPONENT_XP == 0) && (Config.ALT_GAME_EXPONENT_SP == 0))
		{
			if (diff > 5) // formula revised May 07
			{
				final double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}
			
			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0)
			{
				sp = 0;
			}
		}
		
		return new int[]
		{
			(int) xp,
			(int) sp
		};
	}
	
	public long calculateOverhitExp(long normalExp)
	{
		// Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the Attackable
		double overhitPercentage = (_overhitDamage * 100) / getMaxHp();
		
		// Over-hit damage percentages are limited to 25% max
		if (overhitPercentage > 25)
		{
			overhitPercentage = 25;
		}
		
		// Get the overhit exp bonus according to the above over-hit damage percentage
		// (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and so on...)
		final double overhitExp = (overhitPercentage / 100) * normalExp;
		
		// Return the rounded ammount of exp points to be added to the player's normal exp reward
		final long bonusOverhit = Math.round(overhitExp);
		return bonusOverhit;
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		// Clear mob spoil,seed
		setSpoil(false);
		
		// Clear all aggro char from list
		clearAggroList();
		
		// Clear Harvester Rewrard List
		_harvestItems = null;
		
		// Clear mod Seeded stat
		setSeeded(false);
		
		_sweepItems = null;
		resetAbsorbList();
		
		setWalking();
		
		// Check the region where this mob is, do not activate the AI if region is inactive.
		if (hasAI())
		{
			// Set the intention of the Attackable to AI_INTENTION_ACTIVE
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			// Check the region where this mob is, do not activate the AI if region is inactive.
			if (!isInActiveRegion())
			{
				if (this instanceof SiegeGuardInstance)
				{
					((SiegeGuardAI) getAI()).stopAITask();
				}
				else if (this instanceof FortSiegeGuardInstance)
				{
					((FortSiegeGuardAI) getAI()).stopAITask();
				}
				else
				{
					((AttackableAI) getAI()).stopAITask();
				}
			}
		}
	}
	
	/**
	 * Sets state of the mob to seeded. Parameters needed to be set before.
	 */
	public void setSeeded()
	{
		if ((_seedType != 0) && (_seeder != null))
		{
			setSeeded(_seedType);
		}
	}
	
	/**
	 * Sets the seed parameters, but not the seed state
	 * @param id - id of the seed
	 * @param seeder - player who is sowind the seed
	 */
	public void setSeeded(int id, PlayerInstance seeder)
	{
		if (!_seeded)
		{
			_seedType = id;
			_seeder = seeder;
		}
	}
	
	public void setSeeded(int id)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;
		for (int skillId : getTemplate().getSkills().keySet())
		{
			switch (skillId)
			{
				case 4303: // Strong type x2
				{
					count *= 2;
					break;
				}
				case 4304: // Strong type x3
				{
					count *= 3;
					break;
				}
				case 4305: // Strong type x4
				{
					count *= 4;
					break;
				}
				case 4306: // Strong type x5
				{
					count *= 5;
					break;
				}
				case 4307: // Strong type x6
				{
					count *= 6;
					break;
				}
				case 4308: // Strong type x7
				{
					count *= 7;
					break;
				}
				case 4309: // Strong type x8
				{
					count *= 8;
					break;
				}
				case 4310: // Strong type x9
				{
					count *= 9;
					break;
				}
			}
		}
		
		final int diff = getLevel() - (ManorSeedData.getInstance().getSeedLevel(_seedType) - 5);
		
		// hi-lvl mobs bonus
		if (diff > 0)
		{
			count += diff;
		}
		
		final List<RewardItem> harvested = new ArrayList<>();
		harvested.add(new RewardItem(ManorSeedData.getInstance().getCropType(_seedType), (int) (count * Config.RATE_DROP_MANOR)));
		_harvestItems = harvested.toArray(new RewardItem[harvested.size()]);
	}
	
	public void setSeeded(boolean seeded)
	{
		_seeded = seeded;
	}
	
	public PlayerInstance getSeeder()
	{
		return _seeder;
	}
	
	public int getSeedType()
	{
		return _seedType;
	}
	
	public boolean isSeeded()
	{
		return _seeded;
	}
	
	private int getAbsorbLevel()
	{
		return getTemplate().getAbsorbLevel();
	}
	
	/**
	 * Check if the server allows Random Animation.<br>
	 * This is located here because Monster and FriendlyMob both extend this class. The other non-pc instances extend either NpcInstance or MonsterInstance.
	 */
	@Override
	public boolean hasRandomAnimation()
	{
		return (Config.MAX_MONSTER_ANIMATION > 0) && !(this instanceof GrandBossInstance);
	}
	
	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}
	
	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}
	
	public CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}
	
	public void setFirstCommandChannelAttacked(CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}
	
	public long getCommandChannelLastAttack()
	{
		return _commandChannelLastAttack;
	}
	
	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		_commandChannelLastAttack = channelLastAttack;
	}
	
	private static class CommandChannelTimer implements Runnable
	{
		private final Attackable _monster;
		
		public CommandChannelTimer(Attackable monster)
		{
			_monster = monster;
		}
		
		@Override
		public void run()
		{
			if ((System.currentTimeMillis() - _monster.getCommandChannelLastAttack()) > 900000)
			{
				_monster.setCommandChannelTimer(null);
				_monster.setFirstCommandChannelAttacked(null);
				_monster.setCommandChannelLastAttack(0);
			}
			else
			{
				ThreadPool.schedule(this, 10000); // 10sec
			}
		}
	}
}
