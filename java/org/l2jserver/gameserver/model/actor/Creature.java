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

import static org.l2jserver.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.ai.AttackableAI;
import org.l2jserver.gameserver.ai.CreatureAI;
import org.l2jserver.gameserver.ai.CtrlEvent;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.HeroSkillTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.datatables.xml.ZoneData;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.geoengine.GeoEngine;
import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.handler.SkillHandler;
import org.l2jserver.gameserver.handler.itemhandlers.Potions;
import org.l2jserver.gameserver.instancemanager.DimensionalRiftManager;
import org.l2jserver.gameserver.instancemanager.DuelManager;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jserver.gameserver.model.ChanceSkillList;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.ForceBuff;
import org.l2jserver.gameserver.model.Inventory;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.ObjectPosition;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillTargetType;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.WorldRegion;
import org.l2jserver.gameserver.model.actor.instance.BoatInstance;
import org.l2jserver.gameserver.model.actor.instance.ControlTowerInstance;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.EffectPointInstance;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.GuardInstance;
import org.l2jserver.gameserver.model.actor.instance.MinionInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcWalkerInstance;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance.SkillDat;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jserver.gameserver.model.actor.instance.RiftInvaderInstance;
import org.l2jserver.gameserver.model.actor.instance.SiegeFlagInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.actor.knownlist.CreatureKnownList;
import org.l2jserver.gameserver.model.actor.stat.CreatureStat;
import org.l2jserver.gameserver.model.actor.status.CreatureStatus;
import org.l2jserver.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.Duel;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.GameEvent;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.event.VIP;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.skills.Calculator;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.model.skills.effects.EffectCharge;
import org.l2jserver.gameserver.model.skills.funcs.Func;
import org.l2jserver.gameserver.model.skills.holders.ISkillsHolder;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.model.zone.type.TownZone;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.Attack;
import org.l2jserver.gameserver.network.serverpackets.BeginRotation;
import org.l2jserver.gameserver.network.serverpackets.ChangeMoveType;
import org.l2jserver.gameserver.network.serverpackets.ChangeWaitType;
import org.l2jserver.gameserver.network.serverpackets.CharInfo;
import org.l2jserver.gameserver.network.serverpackets.CharMoveToLocation;
import org.l2jserver.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.MagicEffectIcons;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillCanceld;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jserver.gameserver.network.serverpackets.NpcInfo;
import org.l2jserver.gameserver.network.serverpackets.PartySpelled;
import org.l2jserver.gameserver.network.serverpackets.PetInfo;
import org.l2jserver.gameserver.network.serverpackets.RelationChanged;
import org.l2jserver.gameserver.network.serverpackets.Revive;
import org.l2jserver.gameserver.network.serverpackets.SetupGauge;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.StopMove;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.TargetUnselected;
import org.l2jserver.gameserver.network.serverpackets.TeleportToLocation;
import org.l2jserver.gameserver.network.serverpackets.ValidateLocation;
import org.l2jserver.gameserver.network.serverpackets.ValidateLocationInVehicle;
import org.l2jserver.gameserver.util.Util;

/**
 * Mother class of all character objects of the world (PC, NPC...)<br>
 * Creature:<br>
 * <ul>
 * <li>DoorInstance</li>
 * <li>NpcInstance</li>
 * <li>PlayableInstance</li><br>
 * <b>Concept of CreatureTemplate:</b><br>
 * Each Creature owns generic and static properties (ex : all Keltir have the same number of HP...).<br>
 * All of those properties are stored in a different template for each type of Creature.<br>
 * When a new instance of Creature is spawned, server just create a link between the instance and the template.<br>
 * @version $Revision: 1.5.5 $ $Date: 2009/05/12 19:45:27 $
 */
public abstract class Creature extends WorldObject implements ISkillsHolder
{
	protected static final Logger LOGGER = Logger.getLogger(Creature.class.getName());
	
	private long attackStance;
	private List<Creature> _attackByList;
	private Skill _lastSkillCast;
	private Skill _lastPotionCast;
	private boolean _isBuffProtected = false; // Protect From Debuffs
	private boolean _isAfraid = false; // Flee in a random direction
	private boolean _isConfused = false; // Attack anyone randomly
	private boolean _isFakeDeath = false; // Fake death
	private boolean _isFlying = false; // Is flying Wyvern?
	private boolean _isFallsdown = false; // Falls down
	private boolean _isMuted = false; // Cannot use magic
	private boolean _isPhysicalMuted = false; // Cannot use psychical skills
	private boolean _isKilledAlready = false;
	private int _isImmobilized = 0;
	private boolean _isOverloaded = false; // the char is carrying too much
	private boolean _isParalyzed = false;
	private boolean _isRiding = false; // Is Riding strider?
	private boolean _isPendingRevive = false;
	private boolean _isRooted = false; // Cannot move until root timed out
	private boolean _isRunning = false;
	private boolean _isImmobileUntilAttacked = false; // Is in immobile until attacked.
	private boolean _isSleeping = false; // Cannot move/attack until sleep timed out or monster is attacked
	private boolean _isStunned = false; // Cannot move/attack until stun timed out
	private boolean _isBetrayed = false; // Betrayed by own summon
	private boolean _isBlockBuff = false; // Got blocked buff bar
	private boolean _isBlockDebuff = false; // Got blocked debuff bar
	protected boolean _isTeleporting = false;
	protected boolean _isInvul = false;
	protected boolean _isUnkillable = false;
	protected boolean _isAttackDisabled = false;
	private int _lastHealAmount = 0;
	private CreatureStat _stat;
	private CreatureStatus _status;
	private CreatureTemplate _template; // The link on the CreatureTemplate object containing generic and static properties of this Creature type (ex : Max HP, Speed...)
	private String _title;
	private String _aiClass = "default";
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	private boolean _champion = false;
	private Calculator[] _calculators;
	private final Map<Integer, Skill> _skills;
	protected final Map<Integer, Skill> _triggeredSkills;
	protected ChanceSkillList _chanceSkills;
	protected ForceBuff _forceBuff;
	private boolean _blocked;
	private boolean _meditated;
	private final byte[] _zones = new byte[ZoneId.getZoneCount()];
	private boolean _advanceFlag = false;
	private int _advanceMultiplier = 1;
	private byte _startingRotationCounter = 4;
	
	/**
	 * Check if the character is in the given zone Id.
	 * @param zone the zone Id to check
	 * @return {code true} if the character is in that zone
	 */
	public boolean isInsideZone(ZoneId zone)
	{
		return _zones[zone.ordinal()] > 0;
	}
	
	public void setInsideZone(ZoneId zone, boolean state)
	{
		synchronized (_zones)
		{
			if (state)
			{
				_zones[zone.ordinal()]++;
			}
			else if (_zones[zone.ordinal()] > 0)
			{
				_zones[zone.ordinal()]--;
			}
		}
	}
	
	/**
	 * Constructor of Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Each Creature owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of Creature. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of Creature
	 * is spawned, server just create a link between the instance and the template This link is stored in <b>_template</b><br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the _template of the Creature</li>
	 * <li>Set _overloaded to false (the charcater can take more items)</li>
	 * <li>If Creature is a NPCInstance, copy skills from template to object</li>
	 * <li>If Creature is a NPCInstance, link _calculators to NPC_STD_CALCULATOR</li>
	 * <li>If Creature is NOT a NPCInstance, create an empty _skills slot</li>
	 * <li>If Creature is a PlayerInstance or Summon, copy basic Calculator set to object</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template The CreatureTemplate to apply to the object
	 */
	public Creature(int objectId, CreatureTemplate template)
	{
		super(objectId);
		getKnownList();
		
		// Set its template to the new Creature
		_template = template;
		_triggeredSkills = new HashMap<>();
		if ((template != null) && (this instanceof NpcInstance))
		{
			// Copy the Standard Calcultors of the NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;
			
			// Copy the skills of the NPCInstance from its template to the Creature Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy to avoid that a spell affecting a NPCInstance, affects others NPCInstance of the same type too.
			_skills = ((NpcTemplate) template).getSkills();
			for (Map.Entry<Integer, Skill> skill : _skills.entrySet())
			{
				addStatFuncs(skill.getValue().getStatFuncs(null, this));
			}
			
			if (!Config.NPC_ATTACKABLE || (!(this instanceof Attackable) && !(this instanceof ControlTowerInstance) && !(this instanceof SiegeFlagInstance) && !(this instanceof EffectPointInstance)))
			{
				setInvul(true);
			}
		}
		else // not NpcInstance
		{
			// Initialize the Map _skills to null
			_skills = new ConcurrentHashMap<>();
			
			// If Creature is a PlayerInstance or a Summon, create the basic calculator set
			_calculators = new Calculator[Stat.NUM_STATS];
			Formulas.getInstance().addFuncsToNewCharacter(this);
			
			if (!(this instanceof Attackable) && !isAttackable() && !(this instanceof DoorInstance))
			{
				setInvul(true);
			}
		}
	}
	
	/**
	 * Inits the char status update values.
	 */
	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getStat().getMaxHp() / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateIncCheck = getStat().getMaxHp();
		_hpUpdateDecCheck = getStat().getMaxHp() - _hpUpdateInterval;
	}
	
	/**
	 * Remove the Creature from the world when the decay task is launched.<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T REMOVE the object from _allObjects of World </b></font><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packets to players</b></font>
	 */
	public void onDecay()
	{
		final WorldRegion reg = getWorldRegion();
		if (reg != null)
		{
			reg.removeFromZones(this);
		}
		
		decayMe();
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone();
	}
	
	public void onTeleported()
	{
		if (!_isTeleporting)
		{
			return;
		}
		
		final ObjectPosition pos = getPosition();
		if (pos != null)
		{
			spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		}
		
		setTeleporting(false);
		
		if (_isPendingRevive)
		{
			doRevive();
		}
		
		final Summon pet = getPet();
		
		// Modify the position of the pet if necessary
		if ((pet != null) && (pos != null))
		{
			pet.setFollowStatus(false);
			pet.teleToLocation(pos.getX() + Rnd.get(-100, 100), pos.getY() + Rnd.get(-100, 100), pos.getZ(), false);
			pet.setFollowStatus(true);
		}
	}
	
	/**
	 * Add Creature instance that is attacking to the attacker list.
	 * @param creature The Creature that attcks this one
	 */
	public void addAttackerToAttackByList(Creature creature)
	{
		if ((creature == null) || (creature == this) || (getAttackByList() == null) || getAttackByList().contains(creature))
		{
			return;
		}
		
		getAttackByList().add(creature);
	}
	
	/**
	 * Checks if is starting rotation allowed.
	 * @return true, if is starting rotation allowed
	 */
	public synchronized boolean isStartingRotationAllowed()
	{
		// This function is called too often from movement arrow
		_startingRotationCounter--;
		if (_startingRotationCounter < 0)
		{
			_startingRotationCounter = 4;
		}
		
		if (_startingRotationCounter == 4)
		{
			return true;
		}
		return false;
	}
	
	public void broadcastPacket(GameServerPacket mov)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		
		// don't broadcast anytime the rotating packet
		if ((mov instanceof BeginRotation) && !isStartingRotationAllowed())
		{
			return;
		}
		
		for (PlayerInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player != null)
			{
				if ((this instanceof PlayerInstance) && !player.isGM() && (((PlayerInstance) this).getAppearance().isInvisible() || ((PlayerInstance) this).inObserverMode()))
				{
					return;
				}
				
				try
				{
					player.sendPacket(mov);
					
					if ((mov instanceof CharInfo) && (this instanceof PlayerInstance))
					{
						final int relation = ((PlayerInstance) this).getRelation(player);
						if ((getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
						{
							player.sendPacket(new RelationChanged((PlayerInstance) this, relation, player.isAutoAttackable(this)));
						}
					}
				}
				catch (NullPointerException e)
				{
					LOGGER.warning(e.toString());
				}
			}
		}
	}
	
	/**
	 * Send a packet to the Creature AND to all PlayerInstance in the radius (max knownlist radius) from the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * PlayerInstance in the detection area of the Creature are identified in <b>_knownPlayers</b>. In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet
	 * @param mov the mov
	 * @param radiusInKnownlist the radius in knownlist
	 */
	public void broadcastPacket(GameServerPacket mov, int radiusInKnownlist)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		
		for (PlayerInstance player : getKnownList().getKnownPlayers().values())
		{
			try
			{
				if (!isInsideRadius(player, radiusInKnownlist, false, false))
				{
					continue;
				}
				
				player.sendPacket(mov);
				
				if ((mov instanceof CharInfo) && (this instanceof PlayerInstance))
				{
					final int relation = ((PlayerInstance) this).getRelation(player);
					if ((getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
					{
						player.sendPacket(new RelationChanged((PlayerInstance) this, relation, player.isAutoAttackable(this)));
					}
				}
			}
			catch (NullPointerException e)
			{
				LOGGER.warning(e.toString());
			}
		}
	}
	
	/**
	 * Need hp update.
	 * @param barPixels the bar pixels
	 * @return true if hp update should be done, false if not
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		final double currentHp = getStatus().getCurrentHp();
		if ((currentHp <= 1.0) || (getStat().getMaxHp() < barPixels))
		{
			return true;
		}
		
		if ((currentHp <= _hpUpdateDecCheck) || (currentHp >= _hpUpdateIncCheck))
		{
			if (currentHp == getStat().getMaxHp())
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all Creature called _statusListener that must be informed of HP/MP updates of this Creature</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND CP information</b></font><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance : Send current HP,MP and CP to the PlayerInstance and only current HP, MP and Level to all other PlayerInstance of the Party</li>
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
		{
			return;
		}
		
		if (!needHpUpdate(352))
		{
			return;
		}
		
		// Create the Server->Client packet StatusUpdate with current HP and MP
		StatusUpdate su = null;
		if (Config.FORCE_COMPLETE_STATUS_UPDATE && (this instanceof PlayerInstance))
		{
			su = new StatusUpdate((PlayerInstance) this);
		}
		else
		{
			su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
		}
		
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		for (Creature temp : getStatus().getStatusListener())
		{
			if (temp != null)
			{
				temp.sendPacket(su);
			}
		}
	}
	
	/**
	 * Not Implemented.<br>
	 * <br>
	 * <b><u>Overridden in</u>:</b><br>
	 * <li>PlayerInstance</li><br>
	 * @param mov the mov
	 */
	public void sendPacket(GameServerPacket mov)
	{
		// default implementation
	}
	
	/**
	 * Send message.
	 * @param message the message
	 */
	public void sendMessage(String message)
	{
		// default implementation
	}
	
	/** The _in town war. */
	private boolean _inTownWar;
	
	/**
	 * Checks if is in town war.
	 * @return true, if is in town war
	 */
	public boolean isinTownWar()
	{
		return _inTownWar;
	}
	
	/**
	 * Sets the in town war.
	 * @param value the new in town war
	 */
	public void setInTownWar(boolean value)
	{
		_inTownWar = value;
	}
	
	/**
	 * Teleport a Creature and its pet if necessary.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Stop the movement of the Creature</li>
	 * <li>Set the x,y,z position of the WorldObject and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the Creature AND to all PlayerInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><br>
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param allowRandomOffset the allow random offset
	 */
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if (Config.TW_DISABLE_GK)
		{
			final TownZone town = ZoneData.getInstance().getZone(getX(), getY(), getZ(), TownZone.class);
			if ((town != null) && _inTownWar)
			{
				if ((town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS)
				{
					return;
				}
				else if (Config.TW_ALL_TOWNS)
				{
					return;
				}
			}
		}
		
		// Stop movement
		stopMove(null, false);
		abortAttack();
		abortCast();
		
		setTeleporting(true);
		setTarget(null);
		
		// Remove from world regions zones
		final WorldRegion region = getWorldRegion();
		if (region != null)
		{
			region.removeFromZones(this);
		}
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
			y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
		}
		
		z += 5;
		
		// Send a Server->Client packet TeleportToLocationt to the Creature AND to all PlayerInstance in the _KnownPlayers of the Creature
		broadcastPacket(new TeleportToLocation(this, x, y, z));
		
		// remove the object from its old location
		decayMe();
		
		// Set the x,y,z position of the WorldObject and if necessary modify its _worldRegion
		getPosition().setXYZ(x, y, z);
		if (!(this instanceof PlayerInstance))
		{
			onTeleported();
		}
		
		revalidateZone(true);
	}
	
	protected byte _zoneValidateCounter = 4;
	
	/**
	 * Revalidate zone.
	 * @param force the force
	 */
	public void revalidateZone(boolean force)
	{
		final WorldRegion region = getWorldRegion();
		if (region == null)
		{
			return;
		}
		
		// This function is called too often from movement code
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}
		region.revalidateZones(this);
	}
	
	/**
	 * Tele to location.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, false);
	}
	
	/**
	 * Tele to location.
	 * @param loc the loc
	 * @param allowRandomOffset the allow random offset
	 */
	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		
		if ((this instanceof PlayerInstance) && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)) // true -> ignore waiting room :)
		{
			final PlayerInstance player = (PlayerInstance) this;
			player.sendMessage("You have been sent to the waiting room.");
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			
			final int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoords();
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
		}
		
		teleToLocation(x, y, z, allowRandomOffset);
	}
	
	/**
	 * Tele to location.
	 * @param teleportWhere the teleport where
	 */
	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionData.getInstance().getTeleToLocation(this, teleportWhere), true);
	}
	
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the active weapon (always equipped in the right hand)</li>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the PlayerInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use</li>
	 * <li>Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)</li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the Creature AND to all PlayerInstance in the _KnownPlayers of the Creature</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><br>
	 * @param target The Creature targeted
	 */
	protected void doAttack(Creature target)
	{
		if (target == null)
		{
			return;
		}
		
		if (isAlikeDead())
		{
			// If PlayerInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((this instanceof NpcInstance) && target.isAlikeDead())
		{
			// If PlayerInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((this instanceof PlayerInstance) && target.isDead() && !target.isFakeDeath())
		{
			// If PlayerInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!getKnownList().knowsObject(target))
		{
			// If PlayerInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((this instanceof PlayerInstance) && isDead())
		{
			// If PlayerInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((target instanceof PlayerInstance) && (((PlayerInstance) target).getDuelState() == Duel.DUELSTATE_DEAD))
		{
			// If PlayerInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((target instanceof DoorInstance) && !((DoorInstance) target).isAttackable(this))
		{
			return;
		}
		
		if (isAttackingDisabled())
		{
			return;
		}
		
		if (this instanceof PlayerInstance)
		{
			if (((PlayerInstance) this).inObserverMode())
			{
				sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (target instanceof PlayerInstance)
			{
				if (((PlayerInstance) target).isCursedWeaponEquiped() && (((PlayerInstance) this).getLevel() <= Config.MAX_LEVEL_NEWBIE))
				{
					((PlayerInstance) this).sendMessage("Can't attack a cursed player when under level 21.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (((PlayerInstance) this).isCursedWeaponEquiped() && (((PlayerInstance) target).getLevel() <= Config.MAX_LEVEL_NEWBIE))
				{
					((PlayerInstance) this).sendMessage("Can't attack a newbie player using a cursed weapon.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			if (getObjectId() == target.getObjectId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if ((target instanceof NpcInstance) && Config.DISABLE_ATTACK_NPC_TYPE)
			{
				final String mobtype = ((NpcInstance) target).getTemplate().getType();
				if (!Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
				{
					((PlayerInstance) this).sendMessage("Npc Type " + mobtype + " has Protection - No Attack Allowed!");
					((PlayerInstance) this).sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Get the active weapon instance (always equipped in the right hand)
		final ItemInstance weaponInst = getActiveWeaponInstance();
		
		// Get the active weapon item corresponding to the active weapon instance (always equipped in the right hand)
		final Weapon weaponItem = getActiveWeaponItem();
		if ((weaponItem != null) && (weaponItem.getItemType() == WeaponType.ROD))
		{
			// You can't make an attack with a fishing pole.
			((PlayerInstance) this).sendPacket(SystemMessageId.YOU_LOOK_ODDLY_AT_THE_FISHING_POLE_IN_DISBELIEF_AND_REALIZE_THAT_YOU_CAN_T_ATTACK_ANYTHING_WITH_THIS);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((target instanceof GrandBossInstance) && (((GrandBossInstance) target).getNpcId() == 29022) && (Math.abs(_clientZ - target.getZ()) > 200))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANNOT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// GeoData Los Check here (or dz > 1000)
		if (!GeoEngine.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANNOT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check for a bow
		if ((weaponItem != null) && (weaponItem.getItemType() == WeaponType.BOW))
		{
			// Equip arrows needed in left hand and send a Server->Client packet ItemList to the PlayerInstance then return True
			if (!checkAndEquipArrows())
			{
				// Cancel the action because the PlayerInstance have no arrow
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_RUN_OUT_OF_ARROWS));
				return;
			}
			
			// Check for arrows and MP
			if (this instanceof PlayerInstance)
			{
				// Checking if target has moved to peace zone - only for player-bow attacks at the moment
				// Other melee is checked in movement code and for offensive spells a check is done every time
				if (target.isInsidePeaceZone((PlayerInstance) this))
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Verify if the bow can be use
				if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
					// Verify if PlayerInstance owns enough MP
					final int saMpConsume = (int) getStat().calcStat(Stat.MP_CONSUME, 0, null, null);
					final int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;
					if (getStatus().getCurrentMp() < mpConsume)
					{
						// If PlayerInstance doesn't have enough MP, stop the attack
						ThreadPool.schedule(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					// If PlayerInstance have enough MP, the bow consummes it
					getStatus().reduceMp(mpConsume);
					
					// Set the period of bow non re-use
					_disableBowAttackEndTime = (5 * GameTimeController.TICKS_PER_SECOND) + GameTimeController.getGameTicks();
				}
				else
				{
					// Cancel the action because the bow can't be re-use at this moment
					ThreadPool.schedule(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (this instanceof NpcInstance)
			{
				if (_disableBowAttackEndTime > GameTimeController.getGameTicks())
				{
					return;
				}
			}
		}
		
		// Add the PlayerInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);
		
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getStatus().getCurrentCp() - 10);
		}
		
		// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if (this instanceof PlayerInstance)
		{
			((PlayerInstance) this).rechargeAutoSoulShot(true, false, false);
		}
		else if (this instanceof Summon)
		{
			((Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
		}
		
		// Verify if soulshots are charged.
		boolean wasSSCharged;
		if ((this instanceof Summon) && !(this instanceof PetInstance))
		{
			wasSSCharged = ((Summon) this).getChargedSoulShot() != ItemInstance.CHARGED_NONE;
		}
		else
		{
			wasSSCharged = (weaponInst != null) && (weaponInst.getChargedSoulshot() != ItemInstance.CHARGED_NONE);
		}
		
		// Mobius: Do not move when attack is launched.
		if (isMoving())
		{
			stopMove(getPosition().getWorldPosition());
		}
		
		// Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		final int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		final int timeToHit = timeAtk / 2;
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += (timeAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;
		int ssGrade = 0;
		if (weaponItem != null)
		{
			ssGrade = weaponItem.getCrystalType();
		}
		
		// Create a Server->Client packet Attack
		final Attack attack = new Attack(this, wasSSCharged, ssGrade);
		boolean hitted;
		
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		
		// Heading calculation on every attack
		setHeading(Util.calculateHeadingFrom(getX(), getY(), target.getX(), target.getY()));
		
		// Get the Attack Reuse Delay of the Weapon
		final int reuse = calculateReuseTime(target, weaponItem);
		
		// Select the type of attack to start
		if (weaponItem == null)
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, timeToHit);
		}
		else if (isUsingDualWeapon())
		{
			hitted = doAttackHitByDual(attack, target, timeToHit);
		}
		else
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		
		// Flag the attacker if it's a PlayerInstance outside a PvP area
		PlayerInstance player = null;
		if (this instanceof PlayerInstance)
		{
			player = (PlayerInstance) this;
		}
		else if (this instanceof Summon)
		{
			player = ((Summon) this).getOwner();
		}
		
		if (player != null)
		{
			player.updatePvPStatus(target);
		}
		
		// Check if hit isn't missed
		if (!hitted)
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_MISSED));
			// Abort the attack of the Creature and send Server->Client ActionFailed packet
			abortAttack();
		}
		else
		{
			// If we didn't miss the hit, discharge the shoulshots, if any
			if ((this instanceof Summon) && !(this instanceof PetInstance))
			{
				((Summon) this).setChargedSoulShot(ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst != null)
			{
				weaponInst.setChargedSoulshot(ItemInstance.CHARGED_NONE);
			}
			
			if (player != null)
			{
				if (player.isCursedWeaponEquiped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
					{
						target.setCurrentCp(0);
					}
				}
				else if (player.isHero() && (target instanceof PlayerInstance) && ((PlayerInstance) target).isCursedWeaponEquiped())
				{
					// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
					target.setCurrentCp(0);
				}
			}
		}
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the Creature AND to all PlayerInstance in the _KnownPlayers of the Creature
		if (attack.hasHits())
		{
			broadcastPacket(attack);
		}
		
		// Like L2OFF mobs id 27181 can teleport players near cabrio
		if ((this instanceof MonsterInstance) && (((MonsterInstance) this).getNpcId() == 27181))
		{
			final int rndNum = Rnd.get(100);
			final PlayerInstance gettarget = (PlayerInstance) _target;
			if ((rndNum < 5) && (gettarget != null))
			{
				gettarget.teleToLocation(179768, 6364, -2734);
			}
		}
		
		// Like L2OFF if target is not auto attackable you give only one hit
		if ((this instanceof PlayerInstance) && (target instanceof PlayerInstance) && !target.isAutoAttackable(this))
		{
			((PlayerInstance) this).getAI().clientStopAutoAttack();
			((PlayerInstance) this).getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, this);
		}
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPool.schedule(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}
	
	/**
	 * Launch a Bow attack.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>Consumme arrows</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>If the Creature is a PlayerInstance, Send a Server->Client packet SetupGauge</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack</li><br>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param reuse the reuse
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, Creature target, int sAtk, int reuse)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consumme arrows
		reduceArrowCount();
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}
		
		// Check if the Creature is a PlayerInstance
		if (this instanceof PlayerInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessageId.YOU_CAREFULLY_NOCK_AN_ARROW));
			
			// Send a Server->Client packet SetupGauge
			sendPacket(new SetupGauge(SetupGauge.RED, sAtk + reuse));
		}
		
		// Create a new hit task with Medium priority
		ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = ((sAtk + reuse) / GameTimeController.MILLIS_IN_TICK) + GameTimeController.getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Calculate if hits are missed or not</li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient</li>
	 * <li>If hits aren't missed, calculate if hit is critical</li>
	 * <li>If hits aren't missed, calculate physical damages</li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack</li><br>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk the s atk
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		final boolean miss2 = Formulas.calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
			damage2 /= 2;
		}
		
		// Create a new hit task with Medium priority for hit 1
		ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);
		
		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPool.schedule(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);
		
		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		
		// Return true if hit 1 or hit 2 isn't missed
		return !miss1 || !miss2;
	}
	
	/**
	 * Launch a Pole attack.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get all visible objects in a spheric area near the Creature to obtain possible targets</li>
	 * <li>If possible target is the Creature targeted, launch a simple attack against it</li>
	 * <li>If possible target isn't the Creature targeted but is attakable, launch a simple attack against it</li><br>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param sAtk the s atk
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, int sAtk)
	{
		boolean hitted = false;
		double angleChar;
		double angleTarget;
		final int maxRadius = (int) getStat().calcStat(Stat.POWER_ATTACK_RANGE, 66, null, null);
		final int maxAngleDiff = (int) getStat().calcStat(Stat.POWER_ATTACK_ANGLE, 120, null, null);
		if (_target == null)
		{
			return false;
		}
		
		angleTarget = Util.calculateAngleFrom(this, _target);
		setHeading((int) ((angleTarget / 9.0) * 1610.0));
		angleChar = Util.convertHeadingToDegree(_heading);
		double attackpercent = 85;
		final int attackcountmax = (int) getStat().calcStat(Stat.ATTACK_COUNT_MAX, 3, null, null);
		int attackcount = 0;
		if (angleChar <= 0)
		{
			angleChar += 360;
		}
		
		Creature target;
		for (WorldObject obj : getKnownList().getKnownObjects().values())
		{
			if (obj instanceof Creature)
			{
				if ((obj instanceof PetInstance) && (this instanceof PlayerInstance) && (((PetInstance) obj).getOwner() == (PlayerInstance) this))
				{
					continue;
				}
				
				if (!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}
				
				if (Math.abs(obj.getZ() - getZ()) > Config.DIFFERENT_Z_CHANGE_OBJECT)
				{
					continue;
				}
				
				angleTarget = Util.calculateAngleFrom(this, obj);
				if ((Math.abs(angleChar - angleTarget) > maxAngleDiff) && (Math.abs((angleChar + 360) - angleTarget) > maxAngleDiff) && (Math.abs(angleChar - (angleTarget + 360)) > maxAngleDiff))
				{
					continue;
				}
				
				target = (Creature) obj;
				if (!target.isAlikeDead())
				{
					attackcount += 1;
					if ((attackcount <= attackcountmax) && ((target == getAI().getAttackTarget()) || target.isAutoAttackable(this)))
					{
						hitted |= doAttackHitSimple(attack, target, attackpercent, sAtk);
						attackpercent /= 1.15;
						
						// Flag player if the target is another player
						if ((this instanceof PlayerInstance) && (obj instanceof PlayerInstance))
						{
							((PlayerInstance) this).updatePvPStatus(target);
						}
					}
				}
			}
		}
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack</li><br>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk the s atk
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, Creature target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	/**
	 * Do attack hit simple.
	 * @param attack the attack
	 * @param target the target
	 * @param attackpercent the attackpercent
	 * @param sAtk the s atk
	 * @return true, if successful
	 */
	private boolean doAttackHitSimple(Attack attack, Creature target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
			if (attackpercent != 100)
			{
				damage1 = (int) ((damage1 * attackpercent) / 100);
			}
		}
		
		// Create a new hit task with Medium priority
		ThreadPool.schedule(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted...</li>
	 * <li>Get the list of all targets (ex : area effects) and define the Creature targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUse (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message</li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><br>
	 * @param skill The Skill to use
	 */
	public void doCast(Skill skill)
	{
		final Creature creature = this;
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Check if the skill is a magic spell and if the Creature is not muted
		if (skill.isMagic() && _isMuted && !skill.isPotion())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Should not be able to charge yourself.
		if ((skill.getSkillType() == SkillType.CHARGEDAM) && (_target != null) && (_target == this))
		{
			if (isPlayer())
			{
				getActingPlayer().sendPacket(SystemMessageId.INVALID_TARGET);
			}
			return;
		}
		
		// Check if the skill is psychical and if the Creature is not psychical_muted
		if (!skill.isMagic() && _isPhysicalMuted && !skill.isPotion())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Can't use Hero and resurrect skills during Olympiad
		if ((creature instanceof PlayerInstance) && ((PlayerInstance) creature).isInOlympiadMode() && (skill.isHeroSkill() || (skill.getSkillType() == SkillType.RESURRECT)))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_GAMES_MATCH));
			return;
		}
		
		// Like L2OFF you can't use skills when you are attacking now
		if ((creature instanceof PlayerInstance) && !skill.isPotion())
		{
			final ItemInstance rhand = ((PlayerInstance) this).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (((rhand != null) && (rhand.getItemType() == WeaponType.BOW)) && isAttackingNow())
			{
				return;
			}
		}
		
		// prevent casting signets to peace zone
		if ((skill.getSkillType() == SkillType.SIGNET) || (skill.getSkillType() == SkillType.SIGNET_CASTTIME))
		{
			final WorldRegion region = getWorldRegion();
			if (region == null)
			{
				return;
			}
			boolean canCast = true;
			if ((skill.getTargetType() == SkillTargetType.TARGET_GROUND) && (this instanceof PlayerInstance))
			{
				final Location wp = ((PlayerInstance) this).getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
				{
					canCast = false;
				}
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
			{
				canCast = false;
			}
			if (!canCast)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addSkillName(skill.getId());
				sendPacket(sm);
				return;
			}
		}
		
		// Recharge AutoSoulShot
		if (skill.useSoulShot())
		{
			if (creature instanceof PlayerInstance)
			{
				((PlayerInstance) creature).rechargeAutoSoulShot(true, false, false);
			}
			else if (this instanceof Summon)
			{
				((Summon) creature).getOwner().rechargeAutoSoulShot(true, false, true);
			}
		}
		if (skill.useSpiritShot())
		{
			if (creature instanceof PlayerInstance)
			{
				((PlayerInstance) creature).rechargeAutoSoulShot(false, true, false);
			}
			else if (this instanceof Summon)
			{
				((Summon) creature).getOwner().rechargeAutoSoulShot(false, true, true);
			}
		}
		
		// Get all possible targets of the skill in a table in function of the skill target type
		final WorldObject[] targets = skill.getTargetList(creature);
		// Set the target of the skill in function of Skill Type and Target Type
		Creature target = null;
		if ((skill.getTargetType() == SkillTargetType.TARGET_AURA) || (skill.getTargetType() == SkillTargetType.TARGET_GROUND) || skill.isPotion())
		{
			target = this;
		}
		else if ((targets == null) || (targets.length == 0))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		else if (((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.HEAL) || (skill.getSkillType() == SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == SkillType.COMBATPOINTPERCENTHEAL) || (skill.getSkillType() == SkillType.MANAHEAL) || (skill.getSkillType() == SkillType.REFLECT) || (skill.getSkillType() == SkillType.SEED) || (skill.getTargetType() == SkillTargetType.TARGET_SELF) || (skill.getTargetType() == SkillTargetType.TARGET_PET) || (skill.getTargetType() == SkillTargetType.TARGET_PARTY) || (skill.getTargetType() == SkillTargetType.TARGET_CLAN) || (skill.getTargetType() == SkillTargetType.TARGET_ALLY)) && !skill.isPotion())
		{
			target = (Creature) targets[0];
		}
		else
		{
			target = (Creature) _target;
		}
		
		if (target == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Player can't heal rb config
		if (!Config.PLAYERS_CAN_HEAL_RB && (creature instanceof PlayerInstance) && !((PlayerInstance) creature).isGM() && ((target instanceof RaidBossInstance) || (target instanceof GrandBossInstance)) && ((skill.getSkillType() == SkillType.HEAL) || (skill.getSkillType() == SkillType.HEAL_PERCENT)))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((creature instanceof PlayerInstance) && (target instanceof NpcInstance) && Config.DISABLE_ATTACK_NPC_TYPE)
		{
			final String mobtype = ((NpcInstance) target).getTemplate().getType();
			if (!Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
			{
				((PlayerInstance) creature).sendMessage("Npc Type " + mobtype + " has Protection - No Attack Allowed!");
				((PlayerInstance) creature).sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (skill.isPotion())
		{
			setLastPotionCast(skill);
		}
		else
		{
			setLastSkillCast(skill);
		}
		
		// Get the Identifier of the skill
		final int magicId = skill.getId();
		
		// Get the Display Identifier for a skill that client can't display
		final int displayId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		if (level < 1)
		{
			level = 1;
		}
		
		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		final boolean effectWhileCasting = skill.hasEffectWhileCasting();
		final boolean forceBuff = (skill.getSkillType() == SkillType.FORCE_BUFF) && (target instanceof PlayerInstance);
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting && !forceBuff && !skill.isStaticHitTime())
		{
			hitTime = Formulas.getInstance().calcMAtkSpd(creature, skill, hitTime);
			if (coolTime > 0)
			{
				coolTime = Formulas.getInstance().calcMAtkSpd(creature, skill, coolTime);
			}
		}
		
		// Calculate altered Cast Speed due to BSpS/SpS only for Magic skills
		if ((checkBss() || checkSps()) && !skill.isStaticHitTime() && !skill.isPotion() && skill.isMagic())
		{
			// Only takes 70% of the time to cast a BSpS/SpS cast
			hitTime = (int) (0.70 * hitTime);
			coolTime = (int) (0.70 * coolTime);
		}
		
		if (skill.isPotion())
		{
			// Set the _castEndTime and _castInterruptTim. +10 ticks for lag situations, will be reseted in onMagicFinalizer
			_castPotionEndTime = 10 + GameTimeController.getGameTicks() + ((coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK);
			_castPotionInterruptTime = -2 + GameTimeController.getGameTicks() + (hitTime / GameTimeController.MILLIS_IN_TICK);
		}
		else
		{
			// Set the _castEndTime and _castInterruptTim. +10 ticks for lag situations, will be reseted in onMagicFinalizer
			_castEndTime = 10 + GameTimeController.getGameTicks() + ((coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK);
			_castInterruptTime = -2 + GameTimeController.getGameTicks() + (hitTime / GameTimeController.MILLIS_IN_TICK);
		}
		
		// Init the reuse time of the skill
		int reuseDelay = skill.getReuseDelay();
		if ((creature instanceof PlayerInstance) && Formulas.getInstance().calcSkillMastery(creature))
		{
			reuseDelay = 0;
		}
		else if (!skill.isStaticReuse() && !skill.isPotion())
		{
			if (skill.isMagic())
			{
				reuseDelay *= getStat().getMReuseRate(skill);
			}
			else
			{
				reuseDelay *= getStat().getPReuseRate(skill);
			}
			
			reuseDelay *= 333.0 / (skill.isMagic() ? getStat().getMAtkSpd() : getStat().getPAtkSpd());
		}
		
		// To turn local player in target direction
		setHeading(Util.calculateHeadingFrom(getX(), getY(), target.getX(), target.getY()));
		
		// Like L2OFF after a skill the player must stop the movement, unless it is toggle or potion.
		if (!skill.isToggle() && !skill.isPotion() && (this instanceof PlayerInstance))
		{
			stopMove(null);
		}
		
		// Start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			callSkill(skill, targets);
		}
		
		if (!skill.isToggle())
		{
			// Send a Server->Client packet MagicSkillUse with target, displayId, level, skillTime, reuseDelay
			// to the Creature AND to all PlayerInstance in the _KnownPlayers of the Creature
			broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay));
		}
		
		// Send a system message USE_S1 to the Creature
		if ((creature instanceof PlayerInstance) && (magicId != 1312))
		{
			if (skill.isPotion())
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
				if (magicId == 2005)
				{
					sm.addItemName(728);
				}
				else if (magicId == 2003)
				{
					sm.addItemName(726);
				}
				else if ((magicId == 2166) && (skill.getLevel() == 2))
				{
					sm.addItemName(5592);
				}
				else if ((magicId == 2166) && (skill.getLevel() == 1))
				{
					sm.addItemName(5591);
				}
				else
				{
					sm.addSkillName(magicId, skill.getLevel());
				}
				sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
				if (magicId == 2005)
				{
					sm.addItemName(728);
				}
				else if (magicId == 2003)
				{
					sm.addItemName(726);
				}
				else if ((magicId == 2166) && (skill.getLevel() == 2))
				{
					sm.addItemName(5592);
				}
				else if ((magicId == 2166) && (skill.getLevel() == 1))
				{
					sm.addItemName(5591);
				}
				else
				{
					sm.addSkillName(magicId, skill.getLevel());
				}
				
				// Skill 2046 is used only for animation on pets
				if (magicId != 2046)
				{
					sendPacket(sm);
				}
			}
		}
		
		// Skill reuse check
		if ((reuseDelay > 30000) && isPlayer())
		{
			getActingPlayer().addTimestamp(skill, reuseDelay);
		}
		
		// Check if this skill consume mp on start casting
		final int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			final StatusUpdate su = new StatusUpdate(getObjectId());
			if (skill.isDance())
			{
				getStatus().reduceMp(calcStat(Stat.DANCE_MP_CONSUME_RATE, initmpcons, null, null));
			}
			else if (skill.isMagic())
			{
				getStatus().reduceMp(calcStat(Stat.MAGICAL_MP_CONSUME_RATE, initmpcons, null, null));
			}
			else
			{
				getStatus().reduceMp(calcStat(Stat.PHYSICAL_MP_CONSUME_RATE, initmpcons, null, null));
			}
			
			su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
			sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			disableSkill(skill, reuseDelay);
		}
		
		// For force buff skills, start the effect as long as the player is casting.
		if (forceBuff)
		{
			startForceBuff(target, skill);
		}
		
		// launch the magic in hitTime milliseconds
		if (hitTime > 210)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if ((creature instanceof PlayerInstance) && !forceBuff)
			{
				sendPacket(new SetupGauge(SetupGauge.BLUE, hitTime));
			}
			
			// Disable all skills during the casting
			if (!skill.isPotion())
			{ // for particular potion is the timestamp to disable particular skill
				disableAllSkills();
				
				if (_skillCast != null) // delete previous skill cast
				{
					_skillCast.cancel(true);
					_skillCast = null;
				}
			}
			
			// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
			// For client animation reasons (party buffs especially) 200 ms before!
			if ((_forceBuff != null) || effectWhileCasting)
			{
				if (skill.isPotion())
				{
					_potionCast = ThreadPool.schedule(new MagicUseTask(targets, skill, coolTime, 2), hitTime);
				}
				else
				{
					_skillCast = ThreadPool.schedule(new MagicUseTask(targets, skill, coolTime, 2), hitTime);
				}
			}
			else if (skill.isPotion())
			{
				_potionCast = ThreadPool.schedule(new MagicUseTask(targets, skill, coolTime, 1), hitTime - 200);
			}
			else
			{
				_skillCast = ThreadPool.schedule(new MagicUseTask(targets, skill, coolTime, 1), hitTime - 200);
			}
		}
		else
		{
			onMagicLaunchedTimer(targets, skill, coolTime, true);
		}
	}
	
	/**
	 * Starts a force buff on target.
	 * @param target the target
	 * @param skill the skill
	 */
	public void startForceBuff(Creature target, Skill skill)
	{
		if (skill.getSkillType() != SkillType.FORCE_BUFF)
		{
			return;
		}
		
		if (_forceBuff == null)
		{
			_forceBuff = new ForceBuff(this, target, skill);
		}
	}
	
	/**
	 * Kill the Creature.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the Creature</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform</li>
	 * <li>Notify Creature AI</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>NpcInstance : Create a DecayTask to remove the corpse of the NpcInstance after 7 seconds</li>
	 * <li>Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine</li>
	 * <li>PlayerInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop</li><br>
	 * @param killer The Creature who killed it
	 * @return true, if successful
	 */
	public boolean doDie(Creature killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (_isKilledAlready)
			{
				return false;
			}
			
			setKilledAlready(true);
		}
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop fear to avoid possible bug with char position after death
		if (_isAfraid)
		{
			stopFear(null);
		}
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the Creature,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if ((this instanceof Playable) && ((Playable) this).isPhoenixBlessed())
		{
			if (((Playable) this).isNoblesseBlessed())
			{
				((Playable) this).stopNoblesseBlessing(null);
			}
			if (((Playable) this).getCharmOfLuck())
			{
				((Playable) this).stopCharmOfLuck(null);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed PlayableInstance
		else if ((this instanceof Playable) && ((Playable) this).isNoblesseBlessed())
		{
			((Playable) this).stopNoblesseBlessing(null);
			if (((Playable) this).getCharmOfLuck())
			{
				((Playable) this).stopCharmOfLuck(null);
			}
		}
		else if (this instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) this;
			
			// to avoid Event Remove buffs on die
			if (player._inEventDM && DM.hasStarted())
			{
				if (Config.DM_REMOVE_BUFFS_ON_DIE)
				{
					stopAllEffects();
				}
			}
			else if (player._inEventTvT && TvT.isStarted())
			{
				if (Config.TVT_REMOVE_BUFFS_ON_DIE)
				{
					stopAllEffects();
				}
			}
			else if (player._inEventCTF && CTF.isStarted())
			{
				if (Config.CTF_REMOVE_BUFFS_ON_DIE)
				{
					stopAllEffects();
				}
			}
			else if (Config.LEAVE_BUFFS_ON_DIE) // this means that the player is not in event
			{
				stopAllEffects();
			}
		}
		else
		// this means all other characters, including Summons
		{
			stopAllEffects();
		}
		
		// if killer is the same then the most damager/hated
		Creature mostHated = null;
		if (this instanceof Attackable)
		{
			mostHated = ((Attackable) this)._mostHated;
		}
		
		if ((mostHated != null) && isInsideRadius(mostHated, 200, false, false))
		{
			calculateRewards(mostHated);
		}
		else
		{
			calculateRewards(killer);
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform
		broadcastStatusUpdate();
		
		// Notify Creature AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this);
		}
		
		// Notify Quest of character's death
		for (QuestState qs : getNotifyQuestOfDeath())
		{
			qs.getQuest().notifyDeath((killer == null ? this : killer), this, qs);
		}
		
		getNotifyQuestOfDeath().clear();
		
		getAttackByList().clear();
		
		// If character is PhoenixBlessed a resurrection popup will show up
		if ((this instanceof Playable) && ((Playable) this).isPhoenixBlessed())
		{
			((PlayerInstance) this).reviveRequest(((PlayerInstance) this), null, false);
		}
		
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
		
		return true;
	}
	
	/**
	 * Calculate rewards.
	 * @param killer the killer
	 */
	protected void calculateRewards(Creature killer)
	{
	}
	
	/** Sets HP, MP and CP and revives the Creature. */
	public void doRevive()
	{
		if (!_isTeleporting)
		{
			setIsPendingRevive(false);
			
			if ((this instanceof Playable) && ((Playable) this).isPhoenixBlessed())
			{
				((Playable) this).stopPhoenixBlessing(null);
				
				// Like L2OFF Soul of The Phoenix and Salvation restore all hp,cp,mp.
				_status.setCurrentCp(getStat().getMaxCp());
				_status.setCurrentHp(getStat().getMaxHp());
				_status.setCurrentMp(getStat().getMaxMp());
			}
			else
			{
				_status.setCurrentCp(getStat().getMaxCp() * Config.RESPAWN_RESTORE_CP);
				_status.setCurrentHp(getStat().getMaxHp() * Config.RESPAWN_RESTORE_HP);
			}
		}
		// Start broadcast status
		broadcastPacket(new Revive(this));
		if (getWorldRegion() != null)
		{
			getWorldRegion().onRevive(this);
		}
		else
		{
			setIsPendingRevive(true);
		}
	}
	
	/**
	 * Revives the Creature using skill.
	 * @param revivePower the revive power
	 */
	public void doRevive(double revivePower)
	{
		doRevive();
	}
	
	/**
	 * Check if the active Skill can be casted.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Check if the Creature can cast (ex : not sleeping...)</li>
	 * <li>Check if the target is correct</li>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><br>
	 * @param skill The Skill to use
	 */
	protected void useMagic(Skill skill)
	{
		if ((skill == null) || isDead())
		{
			return;
		}
		
		// Check if the Creature can cast
		if (!skill.isPotion() && isAllSkillsDisabled())
		{
			// must be checked by caller
			return;
		}
		
		// Ignore the passive skill request. why does the client send it anyway ??
		if (skill.isPassive() || skill.isChance())
		{
			return;
		}
		
		// Get the target for the skill
		WorldObject target = null;
		
		switch (skill.getTargetType())
		{
			case TARGET_AURA: // AURA, SELF should be cast even if no target has been found
			case TARGET_SELF:
			{
				target = this;
				break;
			}
			default:
			{
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
			}
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	/**
	 * Gets this creature's AI.
	 * @return the AI
	 */
	public CreatureAI getAI()
	{
		CreatureAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				ai = _ai;
				if (ai == null)
				{
					_ai = ai = new CreatureAI(new AIAccessor());
				}
			}
		}
		return ai;
	}
	
	/**
	 * Sets the aI.
	 * @param newAI the new aI
	 */
	public void setAI(CreatureAI newAI)
	{
		final CreatureAI oldAI = _ai;
		if ((oldAI != null) && (oldAI != newAI) && (oldAI instanceof AttackableAI))
		{
			((AttackableAI) oldAI).stopAITask();
		}
		_ai = newAI;
	}
	
	/**
	 * Return True if the Creature has a CreatureAI.
	 * @return true, if successful
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	/**
	 * Return a list of Creature that attacked.
	 * @return the attack by list
	 */
	public List<Creature> getAttackByList()
	{
		if (_attackByList == null)
		{
			_attackByList = new ArrayList<>();
		}
		return _attackByList;
	}
	
	/**
	 * Gets the last skill cast.
	 * @return the last skill cast
	 */
	public Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	/**
	 * Sets the last skill cast.
	 * @param skill the new last skill cast
	 */
	public void setLastSkillCast(Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	/**
	 * Gets the last potion cast.
	 * @return the last potion cast
	 */
	public Skill getLastPotionCast()
	{
		return _lastPotionCast;
	}
	
	/**
	 * Sets the last potion cast.
	 * @param skill the new last potion cast
	 */
	public void setLastPotionCast(Skill skill)
	{
		_lastPotionCast = skill;
	}
	
	/**
	 * Checks if is afraid.
	 * @return true, if is afraid
	 */
	public boolean isAfraid()
	{
		return _isAfraid;
	}
	
	/**
	 * Sets the checks if is afraid.
	 * @param value the new checks if is afraid
	 */
	public void setAfraid(boolean value)
	{
		_isAfraid = value;
	}
	
	/**
	 * Return True if the Creature is dead or use fake death.
	 * @return true, if is alike dead
	 */
	public boolean isAlikeDead()
	{
		return _isFakeDeath || (getStatus().getCurrentHp() <= 0.01);
	}
	
	/**
	 * Return True if the Creature can't use its skills (ex : stun, sleep...).
	 * @return true, if is all skills disabled
	 */
	public boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || _isImmobileUntilAttacked || _isStunned || _isSleeping || _isParalyzed;
	}
	
	/**
	 * Return True if the Creature can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse).
	 * @return true, if is attacking disabled
	 */
	public boolean isAttackingDisabled()
	{
		return _isImmobileUntilAttacked || _isStunned || _isSleeping || _isFallsdown || (_attackEndTime > GameTimeController.getGameTicks()) || _isFakeDeath || _isParalyzed || _isAttackDisabled;
	}
	
	/**
	 * Gets the calculators.
	 * @return the calculators
	 */
	public Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	/**
	 * Checks if is confused.
	 * @return true, if is confused
	 */
	public boolean isConfused()
	{
		return _isConfused;
	}
	
	/**
	 * Sets the checks if is confused.
	 * @param value the new checks if is confused
	 */
	public void setConfused(boolean value)
	{
		_isConfused = value;
	}
	
	/**
	 * Return True if the Creature is dead.
	 * @return true, if is dead
	 */
	public boolean isDead()
	{
		return !_isFakeDeath && (getStatus().getCurrentHp() < 0.5);
	}
	
	/**
	 * Checks if is fake death.
	 * @return true, if is fake death
	 */
	public boolean isFakeDeath()
	{
		return _isFakeDeath;
	}
	
	/**
	 * Sets the checks if is fake death.
	 * @param value the new checks if is fake death
	 */
	public void setFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}
	
	/**
	 * Return True if the Creature is flying.
	 * @return true, if is flying
	 */
	public boolean isFlying()
	{
		return _isFlying;
	}
	
	/**
	 * Set the Creature flying mode to True.
	 * @param mode the new checks if is flying
	 */
	public void setFlying(boolean mode)
	{
		_isFlying = mode;
	}
	
	/**
	 * Checks if is falling.
	 * @return true, if is falling
	 */
	public boolean isFalling()
	{
		return _isFallsdown;
	}
	
	/**
	 * Sets the checks if is falling.
	 * @param value the new checks if is falling
	 */
	public void setFalling(boolean value)
	{
		_isFallsdown = value;
	}
	
	/**
	 * Checks if is immobilized.
	 * @return true, if is immobilized
	 */
	public boolean isImmobilized()
	{
		return _isImmobilized > 0;
	}
	
	/**
	 * Sets the checks if is immobilized.
	 * @param value the new checks if is immobilized
	 */
	public void setImmobilized(boolean value)
	{
		// Stop this if he is moving
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		if (value)
		{
			_isImmobilized++;
		}
		else
		{
			_isImmobilized--;
		}
	}
	
	/**
	 * Checks if is block buff.
	 * @return the _isBlockBuff
	 */
	public boolean isBlockBuff()
	{
		return _isBlockBuff;
	}
	
	/**
	 * Sets the block buff.
	 * @param blockBuff the _isBlockBuff to set
	 */
	public void setBlockBuff(boolean blockBuff)
	{
		_isBlockBuff = blockBuff;
	}
	
	/**
	 * Checks if is block debuff.
	 * @return the _isBlockDebuff
	 */
	public boolean isBlockDebuff()
	{
		return _isBlockDebuff;
	}
	
	/**
	 * Sets the block debuff.
	 * @param blockDebuff the _isBlockDebuff to set
	 */
	public void setBlockDebuff(boolean blockDebuff)
	{
		_isBlockDebuff = blockDebuff;
	}
	
	/**
	 * Checks if is killed already.
	 * @return true, if is killed already
	 */
	public boolean isKilledAlready()
	{
		return _isKilledAlready;
	}
	
	/**
	 * Sets the checks if is killed already.
	 * @param value the new checks if is killed already
	 */
	public void setKilledAlready(boolean value)
	{
		_isKilledAlready = value;
	}
	
	/**
	 * Checks if is muted.
	 * @return true, if is muted
	 */
	public boolean isMuted()
	{
		return _isMuted;
	}
	
	/**
	 * Sets the checks if is muted.
	 * @param value the new checks if is muted
	 */
	public void setMuted(boolean value)
	{
		_isMuted = value;
	}
	
	/**
	 * Checks if is physical muted.
	 * @return true, if is physical muted
	 */
	public boolean isPhysicalMuted()
	{
		return _isPhysicalMuted;
	}
	
	/**
	 * Sets the checks if is physical muted.
	 * @param value the new checks if is physical muted
	 */
	public void setPhysicalMuted(boolean value)
	{
		_isPhysicalMuted = value;
	}
	
	/**
	 * Return True if the Creature can't move (stun, root, sleep, overload, paralyzed).
	 * @return true, if is movement disabled
	 */
	public boolean isMovementDisabled()
	{
		return _isImmobileUntilAttacked || _isStunned || _isRooted || _isSleeping || _isOverloaded || _isParalyzed || isImmobilized() || _isFakeDeath || _isFallsdown;
	}
	
	/**
	 * Return True if the Creature can be controlled by the player (confused, afraid).
	 * @return true, if is out of control
	 */
	public boolean isOutOfControl()
	{
		return _isConfused || _isAfraid || _blocked;
	}
	
	/**
	 * Checks if is overloaded.
	 * @return true, if is overloaded
	 */
	public boolean isOverloaded()
	{
		return _isOverloaded;
	}
	
	/**
	 * Set the overloaded status of the Creature is overloaded (if True, the PlayerInstance can't take more item).
	 * @param value the new checks if is overloaded
	 */
	public void setOverloaded(boolean value)
	{
		_isOverloaded = value;
	}
	
	/**
	 * Checks if is paralyzed.
	 * @return true, if is paralyzed
	 */
	public boolean isParalyzed()
	{
		return _isParalyzed;
	}
	
	/**
	 * Sets the checks if is paralyzed.
	 * @param value the new checks if is paralyzed
	 */
	public void setParalyzed(boolean value)
	{
		if (_petrified)
		{
			return;
		}
		_isParalyzed = value;
	}
	
	/**
	 * Checks if is pending revive.
	 * @return true, if is pending revive
	 */
	public boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}
	
	/**
	 * Sets the checks if is pending revive.
	 * @param value the new checks if is pending revive
	 */
	public void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}
	
	/**
	 * Return the Summon of the Creature.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 * @return the pet
	 */
	public Summon getPet()
	{
		return null;
	}
	
	/**
	 * Return True if the Creature is ridding.
	 * @return true, if is riding
	 */
	public boolean isRiding()
	{
		return _isRiding;
	}
	
	/**
	 * Set the Creature riding mode to True.
	 * @param mode the new checks if is riding
	 */
	public void setRiding(boolean mode)
	{
		_isRiding = mode;
	}
	
	/**
	 * Checks if is rooted.
	 * @return true, if is rooted
	 */
	public boolean isRooted()
	{
		return _isRooted;
	}
	
	/**
	 * Sets the checks if is rooted.
	 * @param value the new checks if is rooted
	 */
	public void setRooted(boolean value)
	{
		_isRooted = value;
	}
	
	/**
	 * Return True if the Creature is running.
	 * @return true, if is running
	 */
	public boolean isRunning()
	{
		return _isRunning;
	}
	
	/**
	 * Sets the checks if is running.
	 * @param value the new checks if is running
	 */
	public void setRunning(boolean value)
	{
		_isRunning = value;
		broadcastPacket(new ChangeMoveType(this));
	}
	
	/**
	 * Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others PlayerInstance.
	 */
	public void setRunning()
	{
		if (!_isRunning)
		{
			setRunning(true);
		}
	}
	
	/**
	 * Checks if is immobile until attacked.
	 * @return true, if is immobile until attacked
	 */
	public boolean isImmobileUntilAttacked()
	{
		return _isImmobileUntilAttacked;
	}
	
	/**
	 * Sets the checks if is immobile until attacked.
	 * @param value the new checks if is immobile until attacked
	 */
	public void setImmobileUntilAttacked(boolean value)
	{
		_isImmobileUntilAttacked = value;
	}
	
	/**
	 * Checks if is sleeping.
	 * @return true, if is sleeping
	 */
	public boolean isSleeping()
	{
		return _isSleeping;
	}
	
	/**
	 * Sets the checks if is sleeping.
	 * @param value the new checks if is sleeping
	 */
	public void setSleeping(boolean value)
	{
		_isSleeping = value;
	}
	
	/**
	 * Checks if is stunned.
	 * @return true, if is stunned
	 */
	public boolean isStunned()
	{
		return _isStunned;
	}
	
	/**
	 * Sets the checks if is stunned.
	 * @param value the new checks if is stunned
	 */
	public void setStunned(boolean value)
	{
		_isStunned = value;
	}
	
	/**
	 * Checks if is betrayed.
	 * @return true, if is betrayed
	 */
	public boolean isBetrayed()
	{
		return _isBetrayed;
	}
	
	/**
	 * Sets the checks if is betrayed.
	 * @param value the new checks if is betrayed
	 */
	public void setBetrayed(boolean value)
	{
		_isBetrayed = value;
	}
	
	/**
	 * Checks if is teleporting.
	 * @return true, if is teleporting
	 */
	public boolean isTeleporting()
	{
		return _isTeleporting;
	}
	
	/**
	 * Sets the checks if is teleporting.
	 * @param value the new checks if is teleporting
	 */
	public void setTeleporting(boolean value)
	{
		_isTeleporting = value;
	}
	
	/**
	 * Sets the checks if is invul.
	 * @param value the new checks if is invul
	 */
	public void setInvul(boolean value)
	{
		if (_petrified)
		{
			return;
		}
		
		_isInvul = value;
	}
	
	/**
	 * Checks if is invul.
	 * @return true, if is invul
	 */
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting;
	}
	
	/**
	 * Checks if is undead.
	 * @return true, if is undead
	 */
	public boolean isUndead()
	{
		return _template.isUndead();
	}
	
	@Override
	public CreatureKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof CreatureKnownList))
		{
			setKnownList(new CreatureKnownList(this));
		}
		return (CreatureKnownList) super.getKnownList();
	}
	
	/**
	 * Gets the stat.
	 * @return the stat
	 */
	public CreatureStat getStat()
	{
		if (_stat == null)
		{
			_stat = new CreatureStat(this);
		}
		return _stat;
	}
	
	/**
	 * Sets the stat.
	 * @param value the new stat
	 */
	public void setStat(CreatureStat value)
	{
		_stat = value;
	}
	
	/**
	 * Gets the status.
	 * @return the status
	 */
	public CreatureStatus getStatus()
	{
		if (_status == null)
		{
			_status = new CreatureStatus(this);
		}
		return _status;
	}
	
	/**
	 * Sets the status.
	 * @param value the new status
	 */
	public void setStatus(CreatureStatus value)
	{
		_status = value;
	}
	
	/**
	 * Gets the template.
	 * @return the template
	 */
	public CreatureTemplate getTemplate()
	{
		return _template;
	}
	
	/**
	 * Set the template of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Each Creature owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of Creature. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of Creature
	 * is spawned, server just create a link between the instance and the template This link is stored in <b>_template</b><br>
	 * <br>
	 * <b><u>Assert</u>:</b><br>
	 * <li>this instanceof Creature</li><br>
	 * <BR
	 * @param template the new template
	 */
	protected final synchronized void setTemplate(CreatureTemplate template)
	{
		_template = template;
	}
	
	/**
	 * Return the Title of the Creature.
	 * @return the title
	 */
	public String getTitle()
	{
		if (_title == null)
		{
			return "";
		}
		return _title;
	}
	
	/**
	 * Set the Title of the Creature.
	 * @param value the new title
	 */
	public void setTitle(String value)
	{
		if (value == null)
		{
			value = "";
		}
		
		if ((this instanceof PlayerInstance) && (value.length() > 16))
		{
			value = value.substring(0, 15);
		}
		
		_title = value; // public void setTitle(String value) { _title = value; }
	}
	
	/**
	 * Set the Creature movement type to walk and send Server->Client packet ChangeMoveType to all others PlayerInstance.
	 */
	public void setWalking()
	{
		if (_isRunning)
		{
			setRunning(false);
		}
	}
	
	/**
	 * Task lauching the function enableSkill().
	 */
	class EnableSkill implements Runnable
	{
		Creature _creature;
		Skill _skill;
		
		/**
		 * Instantiates a new enable skill.
		 * @param creature
		 * @param skill the skill
		 */
		public EnableSkill(Creature creature, Skill skill)
		{
			_creature = creature;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_creature != null)
				{
					_creature.enableSkill(_skill);
				}
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	/**
	 * Task lauching the function onHitTimer().<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a PlayerInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are PlayerInstance</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li>
	 */
	class HitTask implements Runnable
	{
		Creature _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;
		
		/**
		 * Instantiates a new hit task.
		 * @param target the target
		 * @param damage the damage
		 * @param crit the crit
		 * @param miss the miss
		 * @param soulshot the soulshot
		 * @param shld the shld
		 */
		public HitTask(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}
		
		@Override
		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (Throwable e)
			{
				LOGGER.warning("fixme:hit task unhandled exception " + e);
			}
		}
	}
	
	/**
	 * Task lauching the magic skill phases.
	 */
	class MagicUseTask implements Runnable
	{
		WorldObject[] _targets;
		Skill _skill;
		int _coolTime;
		int _phase;
		
		/**
		 * Instantiates a new magic use task.
		 * @param targets the targets
		 * @param skill the skill
		 * @param coolTime the cool time
		 * @param phase the phase
		 */
		public MagicUseTask(WorldObject[] targets, Skill skill, int coolTime, int phase)
		{
			_targets = targets;
			_skill = skill;
			_coolTime = coolTime;
			_phase = phase;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (_phase)
				{
					case 1:
					{
						onMagicLaunchedTimer(_targets, _skill, _coolTime, false);
						break;
					}
					case 2:
					{
						onMagicHitTimer(_targets, _skill, _coolTime, false);
						break;
					}
					case 3:
					{
						onMagicFinalizer(_targets, _skill);
						break;
					}
					default:
					{
						break;
					}
				}
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.getMessage());
				enableAllSkills();
			}
		}
	}
	
	/**
	 * Task lauching the function useMagic().
	 */
	class QueuedMagicUseTask implements Runnable
	{
		PlayerInstance _currPlayer;
		Skill _queuedSkill;
		boolean _isCtrlPressed;
		boolean _isShiftPressed;
		
		/**
		 * Instantiates a new queued magic use task.
		 * @param currPlayer the curr player
		 * @param queuedSkill the queued skill
		 * @param isCtrlPressed the is ctrl pressed
		 * @param isShiftPressed the is shift pressed
		 */
		public QueuedMagicUseTask(PlayerInstance currPlayer, Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}
		
		@Override
		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	/**
	 * Task of AI notification.
	 */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		
		/**
		 * Instantiates a new notify ai task.
		 * @param evt the evt
		 */
		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}
		
		@Override
		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (Throwable t)
			{
				LOGGER.warning(t.getMessage());
			}
		}
	}
	
	/**
	 * Task lauching the function stopPvPFlag().
	 */
	class PvPFlag implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (System.currentTimeMillis() > _pvpFlagLasts)
				{
					stopPvPFlag();
				}
				else if (System.currentTimeMillis() > (_pvpFlagLasts - 5000))
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("error in pvp flag task: " + e);
			}
		}
	}
	
	/** Map 32 bits (0x0000) containing all abnormal effect in progress. */
	private int _AbnormalEffects;
	
	/**
	 * FastTable containing all active skills effects in progress of a Creature.
	 */
	private final List<Effect> _effects = new ArrayList<>();
	
	/** The table containing the List of all stacked effect in progress for each Stack group Identifier. */
	protected Map<String, List<Effect>> _stackedEffects = new HashMap<>();
	
	public static final int ABNORMAL_EFFECT_BLEEDING = 0x000001;
	public static final int ABNORMAL_EFFECT_POISON = 0x000002;
	public static final int ABNORMAL_EFFECT_REDCIRCLE = 0x000004;
	public static final int ABNORMAL_EFFECT_ICE = 0x000008;
	public static final int ABNORMAL_EFFECT_WIND = 0x0000010;
	public static final int ABNORMAL_EFFECT_FEAR = 0x0000020;
	public static final int ABNORMAL_EFFECT_STUN = 0x000040;
	public static final int ABNORMAL_EFFECT_SLEEP = 0x000080;
	public static final int ABNORMAL_EFFECT_MUTED = 0x000100;
	public static final int ABNORMAL_EFFECT_ROOT = 0x000200;
	public static final int ABNORMAL_EFFECT_HOLD_1 = 0x000400;
	public static final int ABNORMAL_EFFECT_HOLD_2 = 0x000800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 0x001000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD = 0x002000;
	public static final int ABNORMAL_EFFECT_FLAME = 0x004000;
	public static final int ABNORMAL_EFFECT_UNKNOWN_16 = 0x008000;
	public static final int ABNORMAL_EFFECT_GROW = 0x010000;
	public static final int ABNORMAL_EFFECT_FLOATING_ROOT = 0x020000;
	public static final int ABNORMAL_EFFECT_DANCE_STUNNED = 0x040000;
	public static final int ABNORMAL_EFFECT_FIREROOT_STUN = 0x080000;
	public static final int ABNORMAL_EFFECT_STEALTH = 0x100000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_1 = 0x200000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_2 = 0x400000;
	public static final int ABNORMAL_EFFECT_MAGIC_CIRCLE = 0x800000;
	public static final int ABNORMAL_EFFECT_CONFUSED = 0x0020;
	public static final int ABNORMAL_EFFECT_AFRAID = 0x0010;
	
	/**
	 * Launch and add Effect (including Stack Group management) to Creature and update client magic icone.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,Effect) <b>_effects</b>. The Integer key of _effects is the Skill Identifier that has created the Effect.<br>
	 * Several same effect can't be used on a Creature at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a Creature, only the more efficient (identified by its priority order) will be preserve.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Add the Effect to the Creature _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the Creature (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the Creature (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icones on player client</li><br>
	 * @param newEffect the new effect
	 */
	public synchronized void addEffect(Effect newEffect)
	{
		if (newEffect == null)
		{
			return;
		}
		
		final Effect[] effects = getAllEffects();
		
		// Make sure there's no same effect previously
		for (Effect effect : effects)
		{
			if (effect == null)
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if ((effect.getSkill().getId() == newEffect.getSkill().getId()) && (effect.getEffectType() == newEffect.getEffectType()) && (effect.getStackType().equals(newEffect.getStackType())))
			{
				if (this instanceof PlayerInstance)
				{
					final PlayerInstance player = (PlayerInstance) this;
					if (player.isInDuel())
					{
						DuelManager.getInstance().getDuel(player.getDuelId()).onBuffStop(player, effect);
					}
				}
				
				if (((newEffect.getSkill().getSkillType() == SkillType.BUFF) || (newEffect.getEffectType() == Effect.EffectType.BUFF) || (newEffect.getEffectType() == Effect.EffectType.HEAL_OVER_TIME)) && (newEffect.getStackOrder() >= effect.getStackOrder()))
				{
					effect.exit(false);
				}
				else
				{
					newEffect.stopEffectTask();
					return;
				}
			}
		}
		
		final Skill tempskill = newEffect.getSkill();
		
		// Remove first Buff if number of buffs > BUFFS_MAX_AMOUNT
		if ((getBuffCount() >= getMaxBuffCount()) && !doesStack(tempskill) && ((tempskill.getSkillType() == SkillType.BUFF) || (tempskill.getSkillType() == SkillType.REFLECT) || (tempskill.getSkillType() == SkillType.HEAL_PERCENT) || (tempskill.getSkillType() == SkillType.MANAHEAL_PERCENT)) && ((tempskill.getId() <= 4360) || (tempskill.getId() >= 4367)) && ((tempskill.getId() <= 4550) || (tempskill.getId() >= 4555)))
		{
			if (newEffect.isHerbEffect())
			{
				newEffect.exit(false);
				return;
			}
			removeFirstBuff(tempskill.getId());
		}
		
		// Remove first DeBuff if number of debuffs > DEBUFFS_MAX_AMOUNT
		if ((getDeBuffCount() >= Config.DEBUFFS_MAX_AMOUNT) && !doesStack(tempskill) && tempskill.isDebuff())
		{
			removeFirstDeBuff(tempskill.getId());
		}
		
		synchronized (_effects)
		{
			// Add the Effect to all effect in progress on the Creature
			if (!newEffect.getSkill().isToggle())
			{
				int pos = 0;
				for (int i = 0; i < _effects.size(); i++)
				{
					if (_effects.get(i) == null)
					{
						_effects.remove(i);
						i--;
						continue;
					}
					
					if (_effects.get(i) != null)
					{
						final int skillid = _effects.get(i).getSkill().getId();
						if (!_effects.get(i).getSkill().isToggle() && ((skillid <= 4360) || (skillid >= 4367)))
						{
							pos++;
						}
					}
					else
					{
						break;
					}
				}
				_effects.add(pos, newEffect);
			}
			else
			{
				_effects.add(newEffect);
			}
		}
		
		// Check if a stack group is defined for this effect
		if (newEffect.getStackType().equals("none"))
		{
			// Set this Effect to In Use
			newEffect.setInUse(true);
			
			// Add Funcs of this effect to the Calculator set of the Creature
			addStatFuncs(newEffect.getStatFuncs());
			
			// Update active skills in progress icones on player client
			updateEffectIcons();
			return;
		}
		
		// Get the list of all stacked effects corresponding to the stack type of the Effect to add
		List<Effect> stackQueue = _stackedEffects.get(newEffect.getStackType());
		if (stackQueue == null)
		{
			stackQueue = new ArrayList<>();
		}
		
		// Get the first stacked effect of the Stack group selected
		if (!stackQueue.isEmpty() && _effects.contains(stackQueue.get(0)))
		{
			// Remove all Func objects corresponding to this stacked effect from the Calculator set of the Creature
			removeStatsOwner(stackQueue.get(0));
			
			// Set the Effect to Not In Use
			stackQueue.get(0).setInUse(false);
		}
		
		// Add the new effect to the stack group selected at its position
		stackQueue = effectQueueInsert(newEffect, stackQueue);
		if (stackQueue == null)
		{
			return;
		}
		
		// Update the Stack Group table _stackedEffects of the Creature
		_stackedEffects.put(newEffect.getStackType(), stackQueue);
		
		// Get the first stacked effect of the Stack group selected
		if (_effects.contains(stackQueue.get(0)))
		{
			// Set this Effect to In Use
			stackQueue.get(0).setInUse(true);
			
			// Add all Func objects corresponding to this stacked effect to the Calculator set of the Creature
			addStatFuncs(stackQueue.get(0).getStatFuncs());
		}
		
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
	}
	
	/**
	 * Insert an effect at the specified position in a Stack Group.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Several same effect can't be used on a Creature at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a Creature, only the more efficient (identified by its priority order) will be preserve.
	 * @param newStackedEffect the new stacked effect
	 * @param stackQueue The Stack Group in wich the effect must be added
	 * @return the list
	 */
	private List<Effect> effectQueueInsert(Effect newStackedEffect, List<Effect> stackQueue)
	{
		// Create an Iterator to go through the list of stacked effects in progress on the Creature
		final Iterator<Effect> queueIterator = stackQueue.iterator();
		int i = 0;
		while (queueIterator.hasNext())
		{
			final Effect cur = queueIterator.next();
			if (newStackedEffect.getStackOrder() < cur.getStackOrder())
			{
				i++;
			}
			else
			{
				break;
			}
		}
		
		// Add the new effect to the Stack list in function of its position in the Stack group
		stackQueue.add(i, newStackedEffect);
		
		// skill.exit() could be used, if the users don't wish to see "effect removed" always when a timer goes off, even if the buff isn't active any more (has been replaced). but then check e.g. npc hold and raid petrify.
		if (Config.EFFECT_CANCELING && !newStackedEffect.isHerbEffect() && (stackQueue.size() > 1))
		{
			synchronized (_effects)
			{
				_effects.remove(stackQueue.get(1));
			}
			
			stackQueue.remove(1);
		}
		
		return stackQueue;
	}
	
	/**
	 * Stop and remove Effect (including Stack Group management) from Creature and update client magic icone.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,Effect) <b>_effects</b>. The Integer key of _effects is the Skill Identifier that has created the Effect.<br>
	 * Several same effect can't be used on a Creature at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a Creature, only the more efficient (identified by its priority order) will be preserve.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove Func added by this effect from the Creature Calculator (Stop Effect)</li>
	 * <li>If the Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the Effect from _effects of the Creature</li>
	 * <li>Update active skills in progress icones on player client</li><br>
	 * @param effect the effect
	 */
	public void removeEffect(Effect effect)
	{
		if (effect == null)
		{
			return;
		}
		
		if (effect.getStackType().equals("none"))
		{
			// Remove Func added by this effect from the Creature Calculator
			removeStatsOwner(effect);
		}
		else
		{
			if (_stackedEffects == null)
			{
				return;
			}
			
			// Get the list of all stacked effects corresponding to the stack type of the Effect to add
			final List<Effect> stackQueue = _stackedEffects.get(effect.getStackType());
			if ((stackQueue == null) || stackQueue.isEmpty())
			{
				return;
			}
			
			// Get the Identifier of the first stacked effect of the Stack group selected
			final Effect frontEffect = stackQueue.get(0);
			
			// Remove the effect from the Stack Group
			final boolean removed = stackQueue.remove(effect);
			if (removed)
			{
				// Check if the first stacked effect was the effect to remove
				if (frontEffect == effect)
				{
					// Remove all its Func objects from the Creature calculator set
					removeStatsOwner(effect);
					
					// Check if there's another effect in the Stack Group and add its list of Funcs to the Calculator set of the Creature
					if (!stackQueue.isEmpty() && _effects.contains(stackQueue.get(0)))
					{
						// Add its list of Funcs to the Calculator set of the Creature
						addStatFuncs(stackQueue.get(0).getStatFuncs());
						// Set the effect to In Use
						stackQueue.get(0).setInUse(true);
					}
				}
				if (stackQueue.isEmpty())
				{
					_stackedEffects.remove(effect.getStackType());
				}
				else
				{
					// Update the Stack Group table _stackedEffects of the Creature
					_stackedEffects.put(effect.getStackType(), stackQueue);
				}
			}
		}
		
		synchronized (_effects)
		{
			// Remove the active skill L2effect from _effects of the Creature
			_effects.remove(effect);
		}
		
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
	}
	
	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.
	 * @param mask the mask
	 */
	public void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	/**
	 * immobile start.
	 */
	public void startImmobileUntilAttacked()
	{
		setImmobileUntilAttacked(true);
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Confused flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public void startConfused()
	{
		setConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Fake Death flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public void startFakeDeath()
	{
		setFalling(true);
		setFakeDeath(true);
		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	/**
	 * Active the abnormal effect Fear flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public void startFear()
	{
		setAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFFRAID);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Muted flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public void startMuted()
	{
		setMuted(true);
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public void startPsychicalMuted()
	{
		setPhysicalMuted(true);
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Root flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public void startRooted()
	{
		setRooted(true);
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Sleep flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public void startSleeping()
	{
		setSleeping(true);
		/* Aborts any attacks/casts if sleeped */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Launch a Stun Abnormal Effect on the Creature.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this Creature</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the PlayerInstance attacker</li>
	 */
	public void startStunning()
	{
		if (_isStunned)
		{
			return;
		}
		
		setStunned(true);
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		getAI().stopFollow(); // Like L2OFF char stop to follow if sticked to another one
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Start betray.
	 */
	public void startBetray()
	{
		setBetrayed(true);
		getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop betray.
	 */
	public void stopBetray()
	{
		stopEffects(Effect.EffectType.BETRAY);
		setBetrayed(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Modify the abnormal effect map according to the mask.
	 * @param mask the mask
	 */
	public void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Stop all active skills effects in progress on the Creature.
	 */
	public void stopAllEffects()
	{
		final Effect[] effects = getAllEffects();
		for (Effect effect : effects)
		{
			if (effect != null)
			{
				effect.exit(true);
			}
			else
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
			}
		}
		
		if (this instanceof PlayerInstance)
		{
			((PlayerInstance) this).updateAndBroadcastStatus(2);
		}
	}
	
	/**
	 * Stop immobilization until attacked abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) immobilization until attacked abnormal Effect from Creature and update client magic icon</li>
	 * <li>Set the abnormal effect flag _muted to False</li>
	 * <li>Notify the Creature AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><br>
	 * @param effect the effect
	 */
	public void stopImmobileUntilAttacked(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.IMMOBILEUNTILATTACKED);
		}
		else
		{
			removeEffect(effect);
			stopSkillEffects(effect.getSkill().getNegateId());
		}
		
		setImmobileUntilAttacked(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Confused abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal Effect from Creature and update client magic icone</li>
	 * <li>Set the abnormal effect flag _confused to False</li>
	 * <li>Notify the Creature AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><br>
	 * @param effect the effect
	 */
	public void stopConfused(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.CONFUSION);
		}
		else
		{
			removeEffect(effect);
		}
		
		setConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop and remove the Effects corresponding to the Skill Identifier and update client magic icone.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,Effect) <b>_effects</b>. The Integer key of _effects is the Skill Identifier that has created the Effect.
	 * @param skillId the skill id
	 */
	public void stopSkillEffects(int skillId)
	{
		final Effect[] effects = getAllEffects();
		for (Effect effect : effects)
		{
			if ((effect == null) || (effect.getSkill() == null))
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill().getId() == skillId)
			{
				effect.exit(true);
			}
		}
	}
	
	/**
	 * Stop and remove all Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the Creature and update client magic icone.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,Effect) <b>_effects</b>. The Integer key of _effects is the Skill Identifier that has created the Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove Func added by this effect from the Creature Calculator (Stop Effect)</li>
	 * <li>Remove the Effect from _effects of the Creature</li>
	 * <li>Update active skills in progress icones on player client</li><br>
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public void stopEffects(Effect.EffectType type)
	{
		final Effect[] effects = getAllEffects();
		for (Effect effect : effects)
		{
			if (effect == null)
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if (effect.getEffectType() == type)
			{
				effect.exit(true);
			}
		}
	}
	
	/**
	 * Stop and remove the Effects corresponding to the SkillType and update client magic icon.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,Effect) <b>_effects</b>. The Integer key of _effects is the Skill Identifier that has created the Effect.
	 * @param skillType The SkillType of the Effect to remove from _effects
	 * @param power the power
	 */
	public void stopSkillEffects(SkillType skillType, double power)
	{
		final Effect[] effects = getAllEffects();
		for (Effect effect : effects)
		{
			if ((effect == null) || (effect.getSkill() == null))
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if ((effect.getSkill().getSkillType() == skillType) && ((power == 0) || (effect.getSkill().getPower() <= power)))
			{
				effect.exit(true);
			}
		}
	}
	
	/**
	 * Stop skill effects.
	 * @param skillType the skill type
	 */
	public void stopSkillEffects(SkillType skillType)
	{
		stopSkillEffects(skillType, -1);
	}
	
	/**
	 * Stop a specified/all Fake Death abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal Effect from Creature and update client magic icone</li>
	 * <li>Set the abnormal effect flag _fake_death to False</li>
	 * <li>Notify the Creature AI</li><br>
	 * @param effect the effect
	 */
	public void stopFakeDeath(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.FAKE_DEATH);
		}
		else
		{
			removeEffect(effect);
		}
		
		setFakeDeath(false);
		setFalling(false);
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (this instanceof PlayerInstance)
		{
			((PlayerInstance) this).setRecentFakeDeath(true);
		}
		
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH));
		broadcastPacket(new Revive(this));
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	/**
	 * Stop a specified/all Fear abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal Effect from Creature and update client magic icone</li>
	 * <li>Set the abnormal effect flag _affraid to False</li>
	 * <li>Notify the Creature AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><br>
	 * @param effect the effect
	 */
	public void stopFear(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.FEAR);
		}
		else
		{
			removeEffect(effect);
		}
		
		setAfraid(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Muted abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal Effect from Creature and update client magic icone</li>
	 * <li>Set the abnormal effect flag _muted to False</li>
	 * <li>Notify the Creature AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><br>
	 * @param effect the effect
	 */
	public void stopMuted(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.MUTE);
		}
		else
		{
			removeEffect(effect);
		}
		
		setMuted(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop psychical muted.
	 * @param effect the effect
	 */
	public void stopPsychicalMuted(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.PSYCHICAL_MUTE);
		}
		else
		{
			removeEffect(effect);
		}
		
		setPhysicalMuted(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Root abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) Root abnormal Effect from Creature and update client magic icone</li>
	 * <li>Set the abnormal effect flag _rooted to False</li>
	 * <li>Notify the Creature AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><br>
	 * @param effect the effect
	 */
	public void stopRooting(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.ROOT);
		}
		else
		{
			removeEffect(effect);
		}
		
		setRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Sleep abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal Effect from Creature and update client magic icone</li>
	 * <li>Set the abnormal effect flag _sleeping to False</li>
	 * <li>Notify the Creature AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><br>
	 * @param effect the effect
	 */
	public void stopSleeping(Effect effect)
	{
		if (effect == null)
		{
			stopEffects(Effect.EffectType.SLEEP);
		}
		else
		{
			removeEffect(effect);
		}
		
		setSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Stun abnormal Effect.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal Effect from Creature and update client magic icone</li>
	 * <li>Set the abnormal effect flag _stuned to False</li>
	 * <li>Notify the Creature AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><br>
	 * @param effect the effect
	 */
	public void stopStunning(Effect effect)
	{
		if (!_isStunned)
		{
			return;
		}
		
		if (effect == null)
		{
			stopEffects(Effect.EffectType.STUN);
		}
		else
		{
			removeEffect(effect);
		}
		
		setStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Not Implemented.<br>
	 * <br>
	 * <b><u>Overridden in</u>:</b><br>
	 * <li>NPCInstance</li>
	 * <li>PlayerInstance</li>
	 * <li>Summon</li>
	 * <li>DoorInstance</li>
	 */
	public abstract void updateAbnormalEffect();
	
	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icones on client.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icone on the client.<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method ONLY UPDATE the client of the player and not clients of all players in the party.</b></font>
	 */
	public void updateEffectIcons()
	{
		updateEffectIcons(false);
	}
	
	/**
	 * Update effect icons.
	 * @param partyOnly the party only
	 */
	public void updateEffectIcons(boolean partyOnly)
	{
		// Create a PlayerInstance of this if needed
		PlayerInstance player = null;
		if (this instanceof PlayerInstance)
		{
			player = (PlayerInstance) this;
		}
		
		// Create a Summon of this if needed
		Summon summon = null;
		if (this instanceof Summon)
		{
			summon = (Summon) this;
			player = summon.getOwner();
			summon.getOwner().sendPacket(new PetInfo(summon));
		}
		
		// Create the main packet if needed
		MagicEffectIcons mi = null;
		if (!partyOnly)
		{
			mi = new MagicEffectIcons();
		}
		
		// Create the party packet if needed
		PartySpelled ps = null;
		if (summon != null)
		{
			ps = new PartySpelled(summon);
		}
		else if ((player != null) && player.isInParty())
		{
			ps = new PartySpelled(player);
		}
		
		// Create the olympiad spectator packet if needed
		ExOlympiadSpelledInfo os = null;
		if ((player != null) && player.isInOlympiadMode())
		{
			os = new ExOlympiadSpelledInfo(player);
		}
		
		if ((mi == null) && (ps == null) && (os == null))
		{
			return; // nothing to do (should not happen)
		}
		
		// Go through all effects if any
		synchronized (_effects)
		{
			for (int i = 0; i < _effects.size(); i++)
			{
				if ((_effects.get(i) == null) || (_effects.get(i).getSkill() == null))
				{
					_effects.remove(i);
					i--;
					continue;
				}
				
				if ((_effects.get(i).getEffectType() == Effect.EffectType.CHARGE) && (player != null))
				{
					// handled by EtcStatusUpdate
					continue;
				}
				
				if (_effects.get(i).getInUse())
				{
					if (mi != null)
					{
						_effects.get(i).addIcon(mi);
					}
					// Like L2OFF toggle and healing potions must not be showed on party buff list
					if ((ps != null) && !_effects.get(i).getSkill().isToggle() && (_effects.get(i).getSkill().getId() != 2031) && (_effects.get(i).getSkill().getId() != 2037) && (_effects.get(i).getSkill().getId() != 2032))
					{
						_effects.get(i).addPartySpelledIcon(ps);
					}
					if (os != null)
					{
						_effects.get(i).addOlympiadSpelledIcon(os);
					}
				}
			}
		}
		
		// Send the packets if needed
		if (mi != null)
		{
			sendPacket(mi);
		}
		
		if ((ps != null) && (player != null))
		{
			// summon info only needs to go to the owner, not to the whole party
			// player info: if in party, send to all party members except one's self.
			// if not in party, send to self.
			if (player.isInParty() && (summon == null))
			{
				player.getParty().broadcastToPartyMembers(player, ps);
			}
			else
			{
				player.sendPacket(ps);
			}
		}
		
		if ((os != null) && (player != null) && (Olympiad.getInstance().getSpectators(player.getOlympiadGameId()) != null))
		{
			for (PlayerInstance spectator : Olympiad.getInstance().getSpectators(player.getOlympiadGameId()))
			{
				if (spectator == null)
				{
					continue;
				}
				spectator.sendPacket(os);
			}
		}
	}
	
	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...). The map is calculated by applying a BINARY OR operation on each effect.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li>
	 * @return the abnormal effect
	 */
	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (_isStunned)
		{
			ae |= ABNORMAL_EFFECT_STUN;
		}
		if (_isRooted)
		{
			ae |= ABNORMAL_EFFECT_ROOT;
		}
		if (_isSleeping)
		{
			ae |= ABNORMAL_EFFECT_SLEEP;
		}
		if (_isConfused)
		{
			ae |= ABNORMAL_EFFECT_CONFUSED;
		}
		if (_isMuted)
		{
			ae |= ABNORMAL_EFFECT_MUTED;
		}
		if (_isAfraid)
		{
			ae |= ABNORMAL_EFFECT_AFRAID;
		}
		if (_isPhysicalMuted)
		{
			ae |= ABNORMAL_EFFECT_MUTED;
		}
		return ae;
	}
	
	/**
	 * Return all active skills effects in progress on the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in <b>_effects</b>. The Integer key of _effects is the Skill Identifier that has created the effect.
	 * @return A table containing all active skills effect in progress on the Creature
	 */
	public Effect[] getAllEffects()
	{
		synchronized (_effects)
		{
			return _effects.toArray(new Effect[_effects.size()]);
		}
	}
	
	/**
	 * Return Effect in progress on the Creature corresponding to the Skill Identifier.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in <b>_effects</b>.
	 * @param index The Skill Identifier of the Effect to return from the _effects
	 * @return The Effect corresponding to the Skill Identifier
	 */
	public Effect getFirstEffect(int index)
	{
		final Effect[] effects = getAllEffects();
		Effect effNotInUse = null;
		for (Effect effect : effects)
		{
			if (effect == null)
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill().getId() == index)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
		}
		
		return effNotInUse;
	}
	
	/**
	 * Gets the first effect.
	 * @param type the type
	 * @return the first effect
	 */
	public Effect getFirstEffect(SkillType type)
	{
		final Effect[] effects = getAllEffects();
		Effect effNotInUse = null;
		for (Effect effect : effects)
		{
			if (effect == null)
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill().getSkillType() == type)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
		}
		
		return effNotInUse;
	}
	
	/**
	 * Return the first Effect in progress on the Creature created by the Skill.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in <b>_effects</b>.
	 * @param skill The Skill whose effect must be returned
	 * @return The first Effect created by the Skill
	 */
	public Effect getFirstEffect(Skill skill)
	{
		final Effect[] effects = getAllEffects();
		Effect effNotInUse = null;
		for (Effect effect : effects)
		{
			if (effect == null)
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill() == skill)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
		}
		
		return effNotInUse;
	}
	
	/**
	 * Return the first Effect in progress on the Creature corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,Effect) <b>_effects</b>. The Integer key of _effects is the Skill Identifier that has created the Effect.
	 * @param tp The Effect Type of skills whose effect must be returned
	 * @return The first Effect corresponding to the Effect Type
	 */
	public Effect getFirstEffect(Effect.EffectType tp)
	{
		final Effect[] effects = getAllEffects();
		Effect effNotInUse = null;
		for (Effect effect : effects)
		{
			if (effect == null)
			{
				synchronized (_effects)
				{
					_effects.remove(effect);
				}
				continue;
			}
			
			if (effect.getEffectType() == tp)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
		}
		
		return effNotInUse;
	}
	
	/**
	 * Gets the charge effect.
	 * @return the charge effect
	 */
	public EffectCharge getChargeEffect()
	{
		final Effect effect = getFirstEffect(SkillType.CHARGE);
		if (effect != null)
		{
			return (EffectCharge) effect;
		}
		return null;
	}
	
	/**
	 * This class permit to the Creature AI to obtain informations and uses Creature method.
	 */
	public class AIAccessor
	{
		/**
		 * Return the Creature managed by this Accessor AI.
		 * @return the actor
		 */
		public Creature getActor()
		{
			return Creature.this;
		}
		
		/**
		 * Accessor to Creature moveToLocation() method with an interaction area.
		 * @param x the x
		 * @param y the y
		 * @param z the z
		 * @param offset the offset
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}
		
		/**
		 * Accessor to Creature moveToLocation() method without interaction area.
		 * @param x the x
		 * @param y the y
		 * @param z the z
		 */
		public void moveTo(int x, int y, int z)
		{
			moveToLocation(x, y, z, 0);
		}
		
		/**
		 * Accessor to Creature stopMove() method.
		 * @param pos the pos
		 */
		public void stopMove(Location pos)
		{
			Creature.this.stopMove(pos);
		}
		
		/**
		 * Accessor to Creature doAttack() method.
		 * @param target the target
		 */
		public void doAttack(Creature target)
		{
			Creature.this.doAttack(target);
		}
		
		/**
		 * Accessor to Creature doCast() method.
		 * @param skill the skill
		 */
		public void doCast(Skill skill)
		{
			Creature.this.doCast(skill);
		}
		
		/**
		 * Create a NotifyAITask.
		 * @param evt the evt
		 * @return the notify ai task
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}
		
		/**
		 * Cancel the AI.
		 */
		public void detachAI()
		{
			_ai = null;
		}
	}
	
	/**
	 * This class group all movement data.<br>
	 * <br>
	 * <b><u>Data</u>:</b><br>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : number of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need to recalculate position
		
		public int _moveStartTime;
		public int _moveTimestamp;
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate;
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<Location> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
	
	protected List<Skill> _disabledSkills = new CopyOnWriteArrayList<>();
	private boolean _allSkillsDisabled;
	protected MoveData _move;
	private boolean _cursorKeyMovement = false;
	private int _heading;
	private WorldObject _target;
	private int _castEndTime;
	private int _castInterruptTime;
	private int _castPotionEndTime;
	@SuppressWarnings("unused")
	private int _castPotionInterruptTime;
	int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;
	
	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE. */
	private static final Calculator[] NPC_STD_CALCULATOR;
	static
	{
		NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();
	}
	
	protected CreatureAI _ai;
	protected Future<?> _skillCast;
	protected Future<?> _potionCast;
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	
	/** List of all QuestState instance that needs to be notified of this character's death. */
	private List<QuestState> _NotifyQuestOfDeathList = new ArrayList<>();
	
	/**
	 * Add QuestState instance that is to be notified of character's death.
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if ((qs == null) || _NotifyQuestOfDeathList.contains(qs))
		{
			return;
		}
		
		_NotifyQuestOfDeathList.add(qs);
	}
	
	/**
	 * Return a list of Creature that attacked.
	 * @return the notify quest of death
	 */
	public List<QuestState> getNotifyQuestOfDeath()
	{
		if (_NotifyQuestOfDeathList == null)
		{
			_NotifyQuestOfDeathList = new ArrayList<>();
		}
		return _NotifyQuestOfDeathList;
	}
	
	/**
	 * Add a Func to the Calculator set of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * A Creature owns a table of Calculators called <b>_calculators</b>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * NPCInstances who don't have skills share the same Calculator set called <b>NPC_STD_CALCULATOR</b>.<br>
	 * That's why, if a NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li>
	 * <li>Add the Func object to _calculators</li><br>
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public synchronized void addStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		// Check if Calculator set is linked to the standard Calculator set of NPC
		if (_calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			_calculators = new Calculator[Stat.NUM_STATS];
			for (int i = 0; i < Stat.NUM_STATS; i++)
			{
				if (NPC_STD_CALCULATOR[i] != null)
				{
					_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
				}
			}
		}
		
		// Select the Calculator of the affected state in the Calculator set
		final int stat = f.stat.ordinal();
		if (_calculators[stat] == null)
		{
			_calculators[stat] = new Calculator();
		}
		
		// Add the Func to the calculator corresponding to the state
		_calculators[stat].addFunc(f);
	}
	
	/**
	 * Add a list of Funcs to the Calculator set of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * A Creature owns a table of Calculators called <b>_calculators</b>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method is ONLY for PlayerInstance</b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Equip an item from inventory</li>
	 * <li>Learn a new passive skill</li>
	 * <li>Use an active skill</li><br>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public synchronized void addStatFuncs(Func[] funcs)
	{
		final List<Stat> modifiedStats = new ArrayList<>();
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	/**
	 * Remove a Func from the Calculator set of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * A Creature owns a table of Calculators called <b>_calculators</b>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * NPCInstances who don't have skills share the same Calculator set called <b>NPC_STD_CALCULATOR</b>.<br>
	 * That's why, if a NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove the Func object from _calculators</li>
	 * <li>If Creature is a NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><br>
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public synchronized void removeStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		// Select the Calculator of the affected state in the Calculator set
		final int stat = f.stat.ordinal();
		if (_calculators[stat] == null)
		{
			return;
		}
		
		// Remove the Func object from the Calculator
		_calculators[stat].removeFunc(f);
		
		if (_calculators[stat].size() == 0)
		{
			_calculators[stat] = null;
		}
		
		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof NpcInstance)
		{
			int i = 0;
			for (; i < Stat.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
				{
					break;
				}
			}
			
			if (i >= Stat.NUM_STATS)
			{
				_calculators = NPC_STD_CALCULATOR;
			}
		}
	}
	
	/**
	 * Remove a list of Funcs from the Calculator set of the PlayerInstance.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * A Creature owns a table of Calculators called <b>_calculators</b>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method is ONLY for PlayerInstance</b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li><br>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public synchronized void removeStatFuncs(Func[] funcs)
	{
		final List<Stat> modifiedStats = new ArrayList<>();
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			removeStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * A Creature owns a table of Calculators called <b>_calculators</b>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * NPCInstances who don't have skills share the same Calculator set called <b>NPC_STD_CALCULATOR</b>.<br>
	 * That's why, if a NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove all Func objects of the selected owner from _calculators</li>
	 * <li>If Creature is a NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li><br>
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public void removeStatsOwner(Object owner)
	{
		List<Stat> modifiedStats = null;
		int i = 0;
		// Go through the Calculator set
		synchronized (_calculators)
		{
			for (Calculator calc : _calculators)
			{
				if (calc != null)
				{
					// Delete all Func objects of the selected owner
					if (modifiedStats != null)
					{
						modifiedStats.addAll(calc.removeOwner(owner));
					}
					else
					{
						modifiedStats = calc.removeOwner(owner);
					}
					
					if (calc.size() == 0)
					{
						_calculators[i] = null;
					}
				}
				i++;
			}
			
			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (this instanceof NpcInstance)
			{
				i = 0;
				for (; i < Stat.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}
				
				if (i >= Stat.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
			
			if ((owner instanceof Effect) && !((Effect) owner).preventExitUpdate)
			{
				broadcastModifiedStats(modifiedStats);
			}
		}
	}
	
	/**
	 * Broadcast modified stats.
	 * @param stats the stats
	 */
	public void broadcastModifiedStats(List<Stat> stats)
	{
		if ((stats == null) || stats.isEmpty())
		{
			return;
		}
		
		boolean broadcastFull = false;
		boolean otherStats = false;
		StatusUpdate su = null;
		for (Stat stat : stats)
		{
			if (stat == Stat.POWER_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(getObjectId());
				}
				
				su.addAttribute(StatusUpdate.ATK_SPD, getStat().getPAtkSpd());
			}
			else if (stat == Stat.MAGIC_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(getObjectId());
				}
				
				su.addAttribute(StatusUpdate.CAST_SPD, getStat().getMAtkSpd());
			}
			else if (stat == Stat.MAX_CP)
			{
				if (this instanceof PlayerInstance)
				{
					if (su == null)
					{
						su = new StatusUpdate(getObjectId());
					}
					
					su.addAttribute(StatusUpdate.MAX_CP, getStat().getMaxCp());
				}
			}
			else if (stat == Stat.RUN_SPEED)
			{
				broadcastFull = true;
			}
			else
			{
				otherStats = true;
			}
		}
		
		if (this instanceof PlayerInstance)
		{
			if (broadcastFull)
			{
				((PlayerInstance) this).updateAndBroadcastStatus(2);
			}
			else if (otherStats)
			{
				((PlayerInstance) this).updateAndBroadcastStatus(1);
				if (su != null)
				{
					for (PlayerInstance player : getKnownList().getKnownPlayers().values())
					{
						try
						{
							player.sendPacket(su);
						}
						catch (NullPointerException e)
						{
							LOGGER.warning(e.toString());
						}
					}
				}
			}
			else if (su != null)
			{
				broadcastPacket(su);
			}
		}
		else if (this instanceof NpcInstance)
		{
			if (broadcastFull && (getKnownList() != null) && (getKnownList().getKnownPlayers() != null))
			{
				for (PlayerInstance player : getKnownList().getKnownPlayers().values())
				{
					if (player != null)
					{
						player.sendPacket(new NpcInfo((NpcInstance) this, player));
					}
				}
			}
			else if (su != null)
			{
				broadcastPacket(su);
			}
		}
		else if (this instanceof Summon)
		{
			if (broadcastFull)
			{
				for (PlayerInstance player : getKnownList().getKnownPlayers().values())
				{
					if (player != null)
					{
						player.sendPacket(new NpcInfo((Summon) this, player));
					}
				}
			}
			else if (su != null)
			{
				broadcastPacket(su);
			}
		}
		else if (su != null)
		{
			broadcastPacket(su);
		}
	}
	
	/**
	 * Return the orientation of the Creature.
	 * @return the heading
	 */
	public int getHeading()
	{
		return _heading;
	}
	
	/**
	 * Set the orientation of the Creature.
	 * @param heading the new heading
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	/**
	 * Return the X destination of the Creature or the X position if not in movement.
	 * @return the client x
	 */
	public int getClientX()
	{
		return _clientX;
	}
	
	/**
	 * Gets the client y.
	 * @return the client y
	 */
	public int getClientY()
	{
		return _clientY;
	}
	
	/**
	 * Gets the client z.
	 * @return the client z
	 */
	public int getClientZ()
	{
		return _clientZ;
	}
	
	/**
	 * Gets the client heading.
	 * @return the client heading
	 */
	public int getClientHeading()
	{
		return _clientHeading;
	}
	
	/**
	 * Sets the client x.
	 * @param value the new client x
	 */
	public void setClientX(int value)
	{
		_clientX = value;
	}
	
	/**
	 * Sets the client y.
	 * @param value the new client y
	 */
	public void setClientY(int value)
	{
		_clientY = value;
	}
	
	/**
	 * Sets the client z.
	 * @param value the new client z
	 */
	public void setClientZ(int value)
	{
		_clientZ = value;
	}
	
	/**
	 * Sets the client heading.
	 * @param value the new client heading
	 */
	public void setClientHeading(int value)
	{
		_clientHeading = value;
	}
	
	/**
	 * Gets the xdestination.
	 * @return the xdestination
	 */
	public int getXdestination()
	{
		final MoveData m = _move;
		if (m != null)
		{
			return m._xDestination;
		}
		return getX();
	}
	
	/**
	 * Return the Y destination of the Creature or the Y position if not in movement.
	 * @return the ydestination
	 */
	public int getYdestination()
	{
		final MoveData m = _move;
		if (m != null)
		{
			return m._yDestination;
		}
		return getY();
	}
	
	/**
	 * Return the Z destination of the Creature or the Z position if not in movement.
	 * @return the zdestination
	 */
	public int getZdestination()
	{
		final MoveData m = _move;
		if (m != null)
		{
			return m._zDestination;
		}
		return getZ();
	}
	
	/**
	 * Return True if the Creature is in combat.
	 * @return true, if is in combat
	 */
	public boolean isInCombat()
	{
		return ((getAI().getAttackTarget() != null) || getAI().isAutoAttacking());
	}
	
	/**
	 * Return True if the Creature is moving.
	 * @return true, if is moving
	 */
	public boolean isMoving()
	{
		return _move != null;
	}
	
	/**
	 * Return True if the Creature is traveling a calculated path.
	 * @return true, if is on geodata path
	 */
	public boolean isOnGeodataPath()
	{
		final MoveData m = _move;
		if (m == null)
		{
			return false;
		}
		if (m.onGeodataPathIndex == -1)
		{
			return false;
		}
		if (m.onGeodataPathIndex >= (m.geoPath.size() - 1))
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Return True if the Creature is casting.
	 * @return true, if is casting now
	 */
	public boolean isCastingNow()
	{
		final Effect mog = getFirstEffect(Effect.EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			return true;
		}
		return _castEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the Creature is casting.
	 * @return true, if is casting potion now
	 */
	public boolean isCastingPotionNow()
	{
		return _castPotionEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the cast of the Creature can be aborted.
	 * @return true, if successful
	 */
	public boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the Creature is attacking.
	 * @return true, if is attacking now
	 */
	public boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the Creature has aborted its attack.
	 * @return true, if is attack aborted
	 */
	public boolean isAttackAborted()
	{
		return _attacking <= 0;
	}
	
	/**
	 * Abort the attack of the Creature and send Server->Client ActionFailed packet.
	 */
	public void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Returns body part (paperdoll slot) we are targeting right now.
	 * @return the attacking body part
	 */
	public int getAttackingBodyPart()
	{
		return _attacking;
	}
	
	/**
	 * Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.
	 */
	public void abortCast()
	{
		abortCast(false);
	}
	
	/**
	 * Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.
	 * @param force the force
	 */
	public void abortCast(boolean force)
	{
		if (isCastingNow() || force)
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}
			
			if (_forceBuff != null)
			{
				_forceBuff.onCastAbort();
			}
			
			final Effect mog = getFirstEffect(Effect.EffectType.SIGNET_GROUND);
			if (mog != null)
			{
				mog.exit(true);
			}
			
			// cancels the skill hit scheduled task
			enableAllSkills(); // re-enables the skills
			if (this instanceof PlayerInstance)
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			}
			
			broadcastPacket(new MagicSkillCanceld(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
		}
	}
	
	/**
	 * Update the position of the Creature during a movement and return True if the movement is finished.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <b>_move</b> of the Creature. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<br>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the Creature position on the server. Note, that the current server position can differe from the current client position even if each movement is straight foward. That's why,
	 * client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server. But, it's always the server position that is used in range calculation.<br>
	 * At the end of the estimated movement time, the Creature position is automatically set to the destination position even if the movement is not finished.<br>
	 * <font color=#FF0000><b><u>Caution</u>: The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet. But x and y positions must be calculated to avoid that players try to modify their movement speed.</b></font>
	 * @param gameTicks number of ticks since the server start
	 * @return True if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		final MoveData m = _move;
		if (m == null)
		{
			return true;
		}
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		// Check if this is the first update
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}
		
		// Check if the position has already been calculated
		if (m._moveTimestamp == gameTicks)
		{
			return false;
		}
		
		final int xPrev = getX();
		final int yPrev = getY();
		final int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations
		double dx;
		double dy;
		double dz;
		double distFraction;
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		if (Config.COORD_SYNCHRONIZE == 1)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else // otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		
		// Z coordinate will follow client values
		dz = m._zDestination - zPrev;
		float speed;
		if (this instanceof BoatInstance)
		{
			speed = ((BoatInstance) this).boatSpeed;
		}
		else
		{
			speed = getStat().getMoveSpeed();
		}
		
		if (isPlayer())
		{
			final double distance = Math.hypot(dx, dy);
			if (_cursorKeyMovement // In case of cursor movement, avoid moving through obstacles.
				|| (distance > 3000)) // Stop movement when player has clicked far away and intersected with an obstacle.
			{
				final double angle = Util.convertHeadingToDegree(getHeading());
				final double radian = Math.toRadians(angle);
				final double course = Math.toRadians(180);
				final double frontDistance = 10 * (_stat.getMoveSpeed() / 100);
				final int x1 = (int) (Math.cos(Math.PI + radian + course) * frontDistance);
				final int y1 = (int) (Math.sin(Math.PI + radian + course) * frontDistance);
				final int x = xPrev + x1;
				final int y = yPrev + y1;
				if (!GeoEngine.getInstance().canMoveToTarget(xPrev, yPrev, zPrev, x, y, zPrev, getInstanceId()))
				{
					_move.onGeodataPathIndex = -1;
					stopMove(getActingPlayer().getLastServerPosition());
					return false;
				}
			}
			// Prevent player moving on ledges.
			if ((dz > 180) && (distance < 300))
			{
				_move.onGeodataPathIndex = -1;
				stopMove(getActingPlayer().getLastServerPosition());
				return false;
			}
		}
		
		final double distPassed = (speed * (gameTicks - m._moveTimestamp)) / GameTimeController.TICKS_PER_SECOND;
		if ((((dx * dx) + (dy * dy)) < 10000) && ((dz * dz) > 2500)) // close enough, allows error between client and server geodata if it cannot be avoided
		{
			distFraction = distPassed / Math.sqrt((dx * dx) + (dy * dy));
		}
		else
		{
			distFraction = distPassed / Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		}
		
		if (distFraction > 1)
		{
			// Set the position of the Creature to the destination
			super.setXYZ(m._xDestination, m._yDestination, m._zDestination);
			if (this instanceof BoatInstance)
			{
				((BoatInstance) this).updatePeopleInTheBoat(m._xDestination, m._yDestination, m._zDestination);
			}
			else
			{
				revalidateZone();
			}
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;
			
			// Set the position of the Creature to estimated after parcial move
			super.setXYZ((int) m._xAccurate, (int) m._yAccurate, zPrev + (int) ((dz * distFraction) + 0.5));
			if (this instanceof BoatInstance)
			{
				((BoatInstance) this).updatePeopleInTheBoat((int) m._xAccurate, (int) m._yAccurate, zPrev + (int) ((dz * distFraction) + 0.5));
			}
			else
			{
				revalidateZone();
			}
		}
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		return distFraction > 1;
	}
	
	/**
	 * Revalidate zone.
	 */
	public void revalidateZone()
	{
		if (getWorldRegion() == null)
		{
			return;
		}
		
		getWorldRegion().revalidateZones(this);
	}
	
	/**
	 * Stop movement of the Creature (Called by AI Accessor only).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Delete movement data of the Creature</li>
	 * <li>Set the current position (x,y,z), its current WorldRegion if necessary and its heading</li>
	 * <li>Remove the WorldObject object from _gmList** of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding WorldRegion Creatures</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T send Server->Client packet StopMove/StopRotation </b></font>
	 * @param pos the pos
	 */
	public void stopMove(Location pos)
	{
		stopMove(pos, true);
	}
	
	/**
	 * @param pos the pos
	 * @param updateKnownObjects the update known objects
	 */
	public void stopMove(Location pos, boolean updateKnownObjects)
	{
		// Delete movement data of the Creature
		_move = null;
		_cursorKeyMovement = false;
		
		// Set AI_INTENTION_IDLE
		if ((this instanceof PlayerInstance) && (getAI() != null))
		{
			((PlayerInstance) this).getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		// Set the current position (x,y,z), its current WorldRegion if necessary and its heading
		// All data are contained in a CharPosition object
		if (pos != null)
		{
			getPosition().setXYZ(pos.getX(), pos.getY(), pos.getZ());
			setHeading(pos.getHeading());
			
			if (this instanceof PlayerInstance)
			{
				((PlayerInstance) this).revalidateZone(true);
				if (((PlayerInstance) this).isInBoat())
				{
					broadcastPacket(new ValidateLocationInVehicle(this));
				}
			}
		}
		
		broadcastPacket(new StopMove(this));
		if (updateKnownObjects)
		{
			getKnownList().updateKnownObjects();
		}
	}
	
	/**
	 * Target a WorldObject (add the target to the Creature _target, _knownObject and Creature to _KnownObject of the WorldObject).<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * The WorldObject (including Creature) targeted is identified in <b>_target</b> of the Creature<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the _target of Creature to WorldObject</li>
	 * <li>If necessary, add WorldObject to _knownObject of the Creature</li>
	 * <li>If necessary, add Creature to _KnownObject of the WorldObject</li>
	 * <li>If object==null, cancel Attak or Cast</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance : Remove the PlayerInstance from the old target _statusListener and add it to the new target if it was a Creature</li><br>
	 * @param object L2object to target
	 */
	public void setTarget(WorldObject object)
	{
		if ((object != null) && !object.isVisible())
		{
			object = null;
		}
		
		if ((object != null) && (object != _target))
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		
		// If object==null, Cancel Attack or Cast
		if ((object == null) && (_target != null))
		{
			final TargetUnselected my = new TargetUnselected(this);
			
			// No need to broadcast the packet to all players
			if (this instanceof PlayerInstance)
			{
				// Send packet just to me and to party, not to any other that does not use the information
				if (!isInParty())
				{
					sendPacket(my);
				}
				else
				{
					getParty().broadcastToPartyMembers(my);
				}
			}
			else
			{
				sendPacket(new TargetUnselected(this));
			}
		}
		
		_target = object;
	}
	
	/**
	 * Return the identifier of the WorldObject targeted or -1.
	 * @return the target id
	 */
	public int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}
		return -1;
	}
	
	/**
	 * Return the WorldObject targeted or null.
	 * @return the target
	 */
	public WorldObject getTarget()
	{
		return _target;
	}
	
	/**
	 * Calculate movement data for a move to location action and add the Creature to movingObjects of GameTimeController (only called by AI Accessor).<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <b>_move</b> of the Creature. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<br>
	 * All Creature in movement are identified in <b>movingObjects</b> of GameTimeController that will call the updatePosition method of those Creature each 0.1s.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get current position of the Creature</li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset</li>
	 * <li>Create and Init a MoveData object</li>
	 * <li>Set the Creature _move object to MoveData object</li>
	 * <li>Add the Creature to movingObjects of the GameTimeController</li>
	 * <li>Create a task to notify the AI that Creature arrives at a check point of the movement</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(WorldObject), onIntentionInteract(WorldObject)</li>
	 * <li>FollowTask</li><br>
	 * @param x The X position of the destination
	 * @param y The Y position of the destination
	 * @param z The Y position of the destination
	 * @param offset The size of the interaction area of the Creature targeted
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Block movement during Event start
		if (this instanceof PlayerInstance)
		{
			if (GameEvent.active && ((PlayerInstance) this).eventSitForced)
			{
				((PlayerInstance) this).sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
				((PlayerInstance) this).getClient().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if ((TvT.isSitForced() && ((PlayerInstance) this)._inEventTvT) || (CTF.isSitForced() && ((PlayerInstance) this)._inEventCTF) || (DM.isSitForced() && ((PlayerInstance) this)._inEventDM))
			{
				((PlayerInstance) this).sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
				((PlayerInstance) this).getClient().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (VIP._sitForced && ((PlayerInstance) this)._inEventVIP)
			{
				((PlayerInstance) this).sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
				((PlayerInstance) this).sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Fix archer bug with movement/hittask
		if ((this instanceof PlayerInstance) && isAttackingNow())
		{
			final ItemInstance rhand = ((PlayerInstance) this).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (((rhand != null) && (rhand.getItemType() == WeaponType.BOW)))
			{
				return;
			}
		}
		
		// Get the Move Speed of the Creature
		final float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			return;
		}
		
		// Get current position of the Creature
		final int curX = getX();
		final int curY = getY();
		final int curZ = getZ();
		
		// Mobius: Fix for teleporting on top of catacomb entrances.
		final boolean isInWater = isInsideZone(ZoneId.WATER);
		if (isInWater && (z > curZ))
		{
			z = Math.max(z - 500, curZ);
		}
		
		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.hypot(dx, dy);
		
		final boolean verticalMovementOnly = _isFlying && (distance == 0) && (dz != 0);
		if (verticalMovementOnly)
		{
			distance = Math.abs(dz);
		}
		
		// Make water move short and use no geodata checks for swimming chars distance in a click can easily be over 3000.
		if (isInWater && (distance > 700))
		{
			final double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distance = Math.hypot(dx, dy);
		}
		
		// @formatter:off
		// Define movement angles needed
		// ^
		// |    X (x,y)
		// |   /
		// |  / distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		// @formatter:on
		
		double cos;
		double sin;
		
		// Check if a movement offset is defined or no distance to go through
		if ((offset > 0) || (distance < 1))
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			if (offset < 5)
			{
				offset = 5;
			}
			
			// If no distance to go through, the movement is canceled
			if ((distance < 1) || ((distance - offset) <= 0))
			{
				// Notify the AI that the Creature is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				return;
			}
			
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
			distance -= (offset - 5); // due to rounding error, we have to move a bit closer to be in range
			
			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}
		
		// Create and Init a MoveData object
		final MoveData m = new MoveData();
		
		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.disregardingGeodata = false;
		if (!_isFlying && !isInWater && !(this instanceof BoatInstance) && !(this instanceof NpcWalkerInstance) && !_cursorKeyMovement)
		{
			final boolean isInBoat = (this instanceof PlayerInstance) && ((PlayerInstance) this).isInBoat();
			if (isInBoat)
			{
				m.disregardingGeodata = true;
			}
			
			// Movement checks.
			if (Config.PATHFINDING)
			{
				final double originalDistance = distance;
				final int originalX = x;
				final int originalY = y;
				final int originalZ = z;
				final int gtx = (originalX - World.MAP_MIN_X) >> 4;
				final int gty = (originalY - World.MAP_MIN_Y) >> 4;
				if (isOnGeodataPath())
				{
					try
					{
						if ((gtx == _move.geoPathGtx) && (gty == _move.geoPathGty))
						{
							return;
						}
						_move.onGeodataPathIndex = -1; // Set not on geodata path.
					}
					catch (NullPointerException e)
					{
						// nothing
					}
				}
				
				// Temporary fix for character outside world region errors (should not happen)
				if ((curX < World.MAP_MIN_X) || (curX > World.MAP_MAX_X) || (curY < World.MAP_MIN_Y) || (curY > World.MAP_MAX_Y))
				{
					LOGGER.warning("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (this instanceof PlayerInstance)
					{
						((PlayerInstance) this).deleteMe();
					}
					else if (this instanceof Summon)
					{
						return;
					}
					else
					{
						onDecay();
					}
					return;
				}
				
				if (!isInBoat // Not in vehicle.
					&& !(isPlayer() && (distance > 3000)) // Should be able to click far away and move.
					&& !(isMonster() && (Math.abs(dz) > 100)) // Monsters can move on ledges.
					&& !(((curZ - z) > 300) && (distance < 300))) // Prohibit correcting destination if character wants to fall.
				{
					// location different if destination wasn't reached (or just z coord is different)
					final Location destiny = GeoEngine.getInstance().canMoveToTargetLoc(curX, curY, curZ, x, y, z, getInstanceId());
					x = destiny.getX();
					y = destiny.getY();
					dx = x - curX;
					dy = y - curY;
					dz = z - curZ;
					distance = verticalMovementOnly ? Math.pow(dz, 2) : Math.hypot(dx, dy);
				}
				
				// Pathfinding checks.
				if (((originalDistance - distance) > 30) && !_isAfraid && !isInBoat)
				{
					// Path calculation -- overrides previous movement check
					m.geoPath = GeoEngine.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, getInstanceId());
					if ((m.geoPath == null) || (m.geoPath.size() < 2)) // No path found
					{
						m.disregardingGeodata = true;
						
						// Mobius: Verify destination. Prevents wall collision issues.
						final Location newDestination = GeoEngine.getInstance().canMoveToTargetLoc(curX, curY, curZ, originalX, originalY, originalZ, getInstanceId());
						x = newDestination.getX();
						y = newDestination.getY();
						z = newDestination.getZ();
					}
					else
					{
						m.onGeodataPathIndex = 0; // on first segment
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;
						m.geoPathAccurateTx = originalX;
						m.geoPathAccurateTy = originalY;
						x = m.geoPath.get(m.onGeodataPathIndex).getX();
						y = m.geoPath.get(m.onGeodataPathIndex).getY();
						z = m.geoPath.get(m.onGeodataPathIndex).getZ();
						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.pow(dz, 2) : Math.hypot(dx, dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			
			// If no distance to go through, the movement is cancelled
			if ((distance < 1) && (Config.PATHFINDING || (this instanceof Playable) || _isAfraid || (this instanceof RiftInvaderInstance)))
			{
				if (this instanceof Summon)
				{
					// Do not break following owner.
					if (getAI().getFollowTarget() != getActingPlayer())
					{
						((Summon) this).setFollowStatus(false);
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					}
				}
				else
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
				return;
			}
		}
		
		// Apply Z distance for flying or swimming for correct timing calculations
		if ((_isFlying || isInWater) && !verticalMovementOnly)
		{
			distance = Math.hypot(distance, dz);
		}
		
		// Calculate the number of ticks between the current position and the destination
		// One tick added for rounding reasons
		final int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		
		// Calculate and set the heading of the Creature
		m._heading = 0; // initial value for coordinate sync
		
		// Does not break heading on vertical movements
		if (!verticalMovementOnly)
		{
			setHeading(Util.calculateHeadingFrom(cos, sin));
		}
		
		m._moveStartTime = GameTimeController.getGameTicks();
		
		// Set the Creature _move object to MoveData object
		_move = m;
		
		// Add the Creature to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that Creature arrives at a check point of the movement
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPool.schedule(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive to destination by GameTimeController
	}
	
	/**
	 * Move to next route point.
	 * @return true, if successful
	 */
	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		
		// Get the Move Speed of the Creature
		final float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		
		final MoveData md = _move;
		if (md == null)
		{
			return false;
		}
		
		// Create and Init a MoveData object
		final MoveData m = new MoveData();
		
		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;
		if (md.onGeodataPathIndex == (md.geoPath.size() - 2))
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		
		final double distance = Math.hypot(m._xDestination - super.getX(), m._yDestination - super.getY());
		// Calculate and set the heading of the Creature
		if (distance != 0)
		{
			setHeading(Util.calculateHeadingFrom(getX(), getY(), m._xDestination, m._yDestination));
		}
		
		// Calculate the number of ticks between the current position and the destination
		// One tick added for rounding reasons
		final int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		m._heading = 0; // initial value for coordinate sync
		m._moveStartTime = GameTimeController.getGameTicks();
		
		// Set the Creature _move object to MoveData object
		_move = m;
		
		// Add the Creature to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that Creature arrives at a check point of the movement
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPool.schedule(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive to destination by GameTimeController
		
		// Send a Server->Client packet CharMoveToLocation to the actor and all PlayerInstance in its _knownPlayers
		broadcastPacket(new CharMoveToLocation(this));
		return true;
	}
	
	/**
	 * Validate movement heading.
	 * @param heading the heading
	 * @return true, if successful
	 */
	public boolean validateMovementHeading(int heading)
	{
		final MoveData m = _move;
		if (m == null)
		{
			return true;
		}
		
		boolean result = true;
		if (m._heading != heading)
		{
			result = (m._heading == 0); // initial value or false
			m._heading = heading;
		}
		
		return result;
	}
	
	/**
	 * Return the distance between the current position of the Creature and the target (x,y).
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public double getDistance(int x, int y)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	/**
	 * Return the distance between the current position of the Creature and the target (x,y).
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z the z
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public double getDistance(int x, int y, int z)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	/**
	 * Return the squared distance between the current position of the Creature and the given object.
	 * @param object WorldObject
	 * @return the squared distance
	 */
	public double getDistanceSq(WorldObject object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Return the squared distance between the current position of the Creature and the given x, y, z.
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @return the squared distance
	 */
	public double getDistanceSq(int x, int y, int z)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		return (dx * dx) + (dy * dy) + (dz * dz);
	}
	
	/**
	 * Return the squared plan distance between the current position of the Creature and the given object.<br>
	 * (check only x and y, not z)
	 * @param object WorldObject
	 * @return the squared plan distance
	 */
	public double getPlanDistanceSq(WorldObject object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}
	
	/**
	 * Return the squared plan distance between the current position of the Creature and the given x, y, z.<br>
	 * (check only x and y, not z)
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	public double getPlanDistanceSq(int x, int y)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		return (dx * dx) + (dy * dy);
	}
	
	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!
	 * @param object the target
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	public boolean isInsideRadius(WorldObject object, int radius, boolean checkZ, boolean strictCheck)
	{
		if (object != null)
		{
			return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
		}
		return false;
	}
	
	/**
	 * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param radius the radius around the target
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	public boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given radius around the given point.
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	public boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		if (strictCheck)
		{
			if (checkZ)
			{
				return ((dx * dx) + (dy * dy) + (dz * dz)) < (radius * radius);
			}
			return ((dx * dx) + (dy * dy)) < (radius * radius);
		}
		if (checkZ)
		{
			return ((dx * dx) + (dy * dy) + (dz * dz)) <= (radius * radius);
		}
		return ((dx * dx) + (dy * dy)) <= (radius * radius);
	}
	
	/**
	 * Return the Weapon Expertise Penalty of the Creature.
	 * @return the weapon expertise penalty
	 */
	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}
	
	/**
	 * Return the Armour Expertise Penalty of the Creature.
	 * @return the armour expertise penalty
	 */
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}
	
	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}
	
	/**
	 * Retun True if arrows are available.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 * @return true, if successful
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	/**
	 * Add Exp and Sp to the Creature.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 * <li>PetInstance</li><br>
	 * @param addToExp the add to exp
	 * @param addToSp the add to sp
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * Return the active weapon instance (always equipped in the right hand).<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 * @return the active weapon instance
	 */
	public abstract ItemInstance getActiveWeaponInstance();
	
	/**
	 * Return the active weapon item (always equipped in the right hand).<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 * @return the active weapon item
	 */
	public abstract Weapon getActiveWeaponItem();
	
	/**
	 * Return the secondary weapon instance (always equipped in the left hand).<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 * @return the secondary weapon instance
	 */
	public abstract ItemInstance getSecondaryWeaponInstance();
	
	/**
	 * Return the secondary weapon item (always equipped in the left hand).<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 * @return the secondary weapon item
	 */
	public abstract Weapon getSecondaryWeaponItem();
	
	/**
	 * Manage hit process (called by Hit Task).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a PlayerInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are PlayerInstance</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li><br>
	 * @param target The Creature targeted
	 * @param damage number of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 */
	protected void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a PlayerInstance)
		if ((target == null) || isAlikeDead() || ((this instanceof NpcInstance) && ((NpcInstance) this).isEventMob))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if (((this instanceof NpcInstance) && target.isAlikeDead()) || target.isDead() || (!getKnownList().knowsObject(target) && !(this instanceof DoorInstance)))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (miss)
		{
			if (target instanceof PlayerInstance)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_AVOIDED_S1_S_ATTACK);
				if (this instanceof Summon)
				{
					final int mobId = ((Summon) this).getTemplate().getNpcId();
					sm.addNpcName(mobId);
				}
				else
				{
					sm.addString(getName());
				}
				((PlayerInstance) target).sendPacket(sm);
			}
		}
		
		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are PlayerInstance
		if (!isAttackAborted())
		{
			if (Config.ALLOW_RAID_BOSS_PETRIFIED && ((this instanceof PlayerInstance) || (this instanceof Summon))) // Check if option is True Or False.
			{
				boolean toBeCursed = false;
				
				// check on BossZone raid lvl
				if (!(target instanceof Playable) && !(target instanceof SummonInstance))
				{
					// this must work just on mobs/raids
					if ((target.isRaid() && (getLevel() > (target.getLevel() + 8))) || (!(target instanceof PlayerInstance) && ((target.getTarget() != null) && (target.getTarget() instanceof RaidBossInstance) && (getLevel() > (((RaidBossInstance) target.getTarget()).getLevel() + 8)))) || (!(target instanceof PlayerInstance) && ((target.getTarget() != null) && (target.getTarget() instanceof GrandBossInstance) && (getLevel() > (((GrandBossInstance) target.getTarget()).getLevel() + 8)))))
					{
						toBeCursed = true;
					}
					
					// advanced check too if not already cursed
					if (!toBeCursed)
					{
						int bossId = -1;
						NpcTemplate bossTemplate = null;
						final BossZone bossZone = GrandBossManager.getInstance().getZone(this);
						if (bossZone != null)
						{
							bossId = bossZone.getBossId();
						}
						
						if (bossId != -1)
						{
							bossTemplate = NpcTable.getInstance().getTemplate(bossId);
							if ((bossTemplate != null) && (getLevel() > (bossTemplate.getLevel() + 8)))
							{
								MonsterInstance bossInstance = null;
								if (bossTemplate.getType().equals("RaidBoss"))
								{
									if (RaidBossSpawnManager.getInstance().getStatSet(bossId) != null)
									{
										bossInstance = RaidBossSpawnManager.getInstance().getBoss(bossId);
									}
								}
								else if (bossTemplate.getType().equals("GrandBoss"))
								{
									if (GrandBossManager.getInstance().getStatSet(bossId) != null)
									{
										bossInstance = GrandBossManager.getInstance().getBoss(bossId);
									}
								}
								
								// max allowed rage into take cursed is 3000
								if ((bossInstance != null) && bossInstance.isInsideRadius(this, 3000, false, false))
								{
									toBeCursed = true;
								}
							}
						}
					}
				}
				
				if (toBeCursed)
				{
					final Skill skill = SkillTable.getInstance().getInfo(4515, 1);
					if (skill != null)
					{
						abortAttack();
						abortCast();
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(target, this, false, false, false);
						if (this instanceof Summon)
						{
							final Summon src = ((Summon) this);
							if (src.getOwner() != null)
							{
								src.getOwner().abortAttack();
								src.getOwner().abortCast();
								src.getOwner().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								skill.getEffects(target, src.getOwner(), false, false, false);
							}
						}
					}
					else
					{
						LOGGER.warning("Skill 4515 at level 1 is missing in DP.");
					}
					
					if (target instanceof MinionInstance)
					{
						((MinionInstance) target).getLeader().stopHating(this);
						
						final List<MinionInstance> spawnedMinions = ((MinionInstance) target).getLeader().getSpawnedMinions();
						if ((spawnedMinions != null) && !spawnedMinions.isEmpty())
						{
							final Iterator<MinionInstance> itr = spawnedMinions.iterator();
							MinionInstance minion;
							while (itr.hasNext())
							{
								minion = itr.next();
								if (((MinionInstance) target).getLeader().getMostHated() == null)
								{
									((AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.setWalking();
								}
								if ((minion != null) && !minion.isDead())
								{
									((AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.addDamage(((MinionInstance) target).getLeader().getMostHated(), 100);
								}
							}
						}
					}
					else
					{
						((Attackable) target).stopHating(this);
						final List<MinionInstance> spawnedMinions = ((MonsterInstance) target).getSpawnedMinions();
						if ((spawnedMinions != null) && !spawnedMinions.isEmpty())
						{
							final Iterator<MinionInstance> itr = spawnedMinions.iterator();
							MinionInstance minion;
							while (itr.hasNext())
							{
								minion = itr.next();
								if (((Attackable) target).getMostHated() == null)
								{
									((AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.setWalking();
								}
								if ((minion != null) && !minion.isDead())
								{
									((AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.addDamage(((Attackable) target).getMostHated(), 100);
								}
							}
						}
					}
					
					damage = 0; // prevents messing up drop calculation
				}
			}
			
			sendDamageMessage(target, damage, false, crit, miss);
			
			// If Creature target is a PlayerInstance, send a system message
			if (target instanceof PlayerInstance)
			{
				final PlayerInstance enemy = (PlayerInstance) target;
				
				// Check if shield is efficient
				if (shld)
				{
					enemy.sendPacket(SystemMessageId.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
				}
			}
			else if (target instanceof Summon)
			{
				final Summon activeSummon = (Summon) target;
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_PET_RECEIVED_S2_DAMAGE_CAUSED_BY_S1);
				sm.addString(getName());
				sm.addNumber(damage);
				activeSummon.getOwner().sendPacket(sm);
			}
			
			if (!miss && (damage > 0))
			{
				final Weapon weapon = getActiveWeaponItem();
				final boolean isBow = (weapon != null) && weapon.getItemType().toString().equalsIgnoreCase("Bow");
				if (!isBow) // Do not reflect or absorb if weapon is of type bow
				{
					// Absorb HP from the damage inflicted
					final double absorbPercent = getStat().calcStat(Stat.ABSORB_DAMAGE_PERCENT, 0, null, null);
					if (absorbPercent > 0)
					{
						final int maxCanAbsorb = (int) (getStat().getMaxHp() - getStatus().getCurrentHp());
						int absorbDamage = (int) ((absorbPercent / 100.) * damage);
						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						}
						
						if (absorbDamage > 0)
						{
							setCurrentHp(getStatus().getCurrentHp() + absorbDamage);
						}
					}
					
					// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
					final double reflectPercent = target.getStat().calcStat(Stat.REFLECT_DAMAGE_PERCENT, 0, null, null);
					if (reflectPercent > 0)
					{
						int reflectedDamage = (int) ((reflectPercent / 100.) * damage);
						damage -= reflectedDamage;
						if (reflectedDamage > target.getMaxHp())
						{
							reflectedDamage = target.getMaxHp();
						}
						
						getStatus().reduceHp(reflectedDamage, target, true);
					}
				}
				
				target.reduceCurrentHp(damage, this);
				
				// Notify AI with EVT_ATTACKED
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				// Maybe launch chance skills on us
				if (_chanceSkills != null)
				{
					_chanceSkills.onHit(target, false, crit);
				}
				
				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onHit(this, true, crit);
				}
			}
			
			// Launch weapon Special ability effect if available
			final Weapon activeWeapon = getActiveWeaponItem();
			if (activeWeapon != null)
			{
				activeWeapon.getSkillEffects(this, target, crit);
			}
			return;
		}
		
		getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}
	
	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature.
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the Creature and send Server->Client ActionFailed packet
			abortAttack();
			
			if (this instanceof PlayerInstance)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED));
			}
		}
	}
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature.
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && (_lastSkillCast != null) && _lastSkillCast.isMagic())
		{
			// Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (this instanceof PlayerInstance)
			{
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.YOUR_CASTING_HAS_BEEN_INTERRUPTED));
			}
		}
	}
	
	/**
	 * Reduce the arrow number of the Creature.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li>
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothing
	}
	
	/**
	 * Manage Forced attack (shift + select target).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>If Creature or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed</li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed</li>
	 * <li>If Creature is a ArtefactInstance, send a Server->Client packet ActionFailed</li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK</li><br>
	 * @param player The PlayerInstance to attack
	 */
	@Override
	public void onForcedAttack(PlayerInstance player)
	{
		if (!(player.getTarget() instanceof Creature))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isInsidePeaceZone(player))
		{
			// If Creature or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode() && (player.getTarget() != null) && (player.getTarget() instanceof Playable))
		{
			PlayerInstance target;
			if (player.getTarget() instanceof Summon)
			{
				target = ((Summon) player.getTarget()).getOwner();
			}
			else
			{
				target = (PlayerInstance) player.getTarget();
			}
			
			if (target.isInOlympiadMode() && !player.isOlympiadStart() && (player.getOlympiadGameId() == target.getOlympiadGameId()))
			{
				// if PlayerInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (player.isConfused() || player.isBlocked())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// GeoData Los Check or dz > 1000
		if (!GeoEngine.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessageId.CANNOT_SEE_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(AI_INTENTION_ATTACK, this);
	}
	
	/**
	 * Return True if inside peace zone.
	 * @param attacker the attacker
	 * @return true, if is inside peace zone
	 */
	public boolean isInsidePeaceZone(PlayerInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}
	
	/**
	 * Checks if is inside peace zone.
	 * @param attacker the attacker
	 * @param target the target
	 * @return true, if is inside peace zone
	 */
	public static boolean isInsidePeaceZone(WorldObject attacker, WorldObject target)
	{
		if (target == null)
		{
			return false;
		}
		
		if ((target instanceof NpcInstance) && Config.DISABLE_ATTACK_NPC_TYPE)
		{
			final String mobtype = ((NpcInstance) target).getTemplate().getType();
			if (Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
			{
				return false;
			}
		}
		
		// Attack Monster on Peace Zone like L2OFF.
		if ((target instanceof MonsterInstance) || ((attacker instanceof MonsterInstance) && Config.ALT_MOB_AGRO_IN_PEACEZONE))
		{
			return false;
		}
		
		// Attack Guard on Peace Zone like L2OFF.
		if ((target instanceof GuardInstance) || (attacker instanceof GuardInstance))
		{
			return false;
		}
		// Attack NPC on Peace Zone like L2OFF.
		if ((target instanceof NpcInstance) || (attacker instanceof NpcInstance))
		{
			return false;
		}
		
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if ((target instanceof PlayerInstance) && (((PlayerInstance) target).getKarma() > 0))
			{
				return false;
			}
			
			if ((target instanceof Summon) && (((Summon) target).getOwner().getKarma() > 0))
			{
				return false;
			}
			
			if ((attacker instanceof PlayerInstance) && (((PlayerInstance) attacker).getKarma() > 0))
			{
				if ((target instanceof PlayerInstance) && (((PlayerInstance) target).getPvpFlag() > 0))
				{
					return false;
				}
				
				if ((target instanceof Summon) && (((Summon) target).getOwner().getPvpFlag() > 0))
				{
					return false;
				}
			}
			
			if ((attacker instanceof Summon) && (((Summon) attacker).getOwner().getKarma() > 0))
			{
				if ((target instanceof PlayerInstance) && (((PlayerInstance) target).getPvpFlag() > 0))
				{
					return false;
				}
				
				if ((target instanceof Summon) && (((Summon) target).getOwner().getPvpFlag() > 0))
				{
					return false;
				}
			}
		}
		
		// Right now only PlayerInstance has up-to-date zone status...
		PlayerInstance src = null;
		PlayerInstance dst = null;
		if ((attacker instanceof Playable) && (target instanceof Playable))
		{
			if (attacker instanceof PlayerInstance)
			{
				src = (PlayerInstance) attacker;
			}
			else if (attacker instanceof Summon)
			{
				src = ((Summon) attacker).getOwner();
			}
			
			if (target instanceof PlayerInstance)
			{
				dst = (PlayerInstance) target;
			}
			else if (target instanceof Summon)
			{
				dst = ((Summon) target).getOwner();
			}
		}
		
		if ((src != null) && src.getAccessLevel().allowPeaceAttack())
		{
			return false;
		}
		
		// checks on event status
		if ((src != null) && (dst != null))
		{
			// Attacker and target can fight in olympiad with peace zone
			if (src.isInOlympiadMode() && src.isOlympiadStart() && dst.isInOlympiadMode() && dst.isOlympiadStart())
			{
				return false;
			}
			
			if (dst.isInFunEvent() && src.isInFunEvent())
			{
				if (src.isInStartedTVTEvent() && dst.isInStartedTVTEvent())
				{
					return false;
				}
				else if (src.isInStartedDMEvent() && dst.isInStartedDMEvent())
				{
					return false;
				}
				else if (src.isInStartedCTFEvent() && dst.isInStartedCTFEvent())
				{
					return false;
				}
				else if (src.isInStartedVIPEvent() && dst.isInStartedVIPEvent())
				{
					return false;
				}
			}
		}
		
		if ((attacker instanceof Creature) && ((Creature) attacker).isInsideZone(ZoneId.PEACE))
		{
			return true;
		}
		
		if ((target instanceof Creature) && ((Creature) target).isInsideZone(ZoneId.PEACE))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * return true if this character is inside an active grid.
	 * @return the boolean
	 */
	public Boolean isInActiveRegion()
	{
		try
		{
			final WorldRegion region = World.getInstance().getRegion(getX(), getY());
			return (region != null) && region.isActive();
		}
		catch (Exception e)
		{
			if (this instanceof PlayerInstance)
			{
				LOGGER.warning("Player " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
				((PlayerInstance) this).sendMessage("Error with your coordinates! Please reboot your game fully!");
				((PlayerInstance) this).teleToLocation(80753, 145481, -3532, false); // Near Giran luxury shop
			}
			else
			{
				LOGGER.warning("Object " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
				decayMe();
			}
			return false;
		}
	}
	
	/**
	 * Return True if the Creature has a Party in progress.
	 * @return true, if is in party
	 */
	public boolean isInParty()
	{
		return false;
	}
	
	/**
	 * Return the Party object of the Creature.
	 * @return the party
	 */
	public Party getParty()
	{
		return null;
	}
	
	/**
	 * Return the Attack Speed of the Creature (delay (in milliseconds) before next attack).
	 * @param target the target
	 * @param weapon the weapon
	 * @return the int
	 */
	public int calculateTimeBetweenAttacks(Creature target, Weapon weapon)
	{
		double atkSpd = 0;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
				{
					atkSpd = getStat().getPAtkSpd();
					return (int) ((1500 * 345) / atkSpd);
				}
				case DAGGER:
				{
					atkSpd = getStat().getPAtkSpd();
					break;
				}
				default:
				{
					atkSpd = getStat().getPAtkSpd();
				}
			}
		}
		else
		{
			atkSpd = getStat().getPAtkSpd();
		}
		return Formulas.getInstance().calcPAtkSpd(this, target, atkSpd);
	}
	
	/**
	 * Calculate reuse time.
	 * @param target the target
	 * @param weapon the weapon
	 * @return the int
	 */
	public int calculateReuseTime(Creature target, Weapon weapon)
	{
		if (weapon == null)
		{
			return 0;
		}
		
		int reuse = weapon.getAttackReuseDelay();
		
		// only bows should continue for now
		if (reuse == 0)
		{
			return 0;
		}
		
		reuse *= getStat().getReuseModifier(target);
		
		final double atkSpd = getStat().getPAtkSpd();
		
		switch (weapon.getItemType())
		{
			case BOW:
			{
				return (int) ((reuse * 345) / atkSpd);
			}
			default:
			{
				return (int) ((reuse * 312) / atkSpd);
			}
		}
	}
	
	/**
	 * Return True if the Creature use a dual weapon.
	 * @return true, if is using dual weapon
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	/**
	 * Add a skill to the Creature _skills and its Func objects to the calculator set of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All skills own by a Creature are identified in <b>_skills</b><br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li>
	 * <li>If an old skill has been replaced, remove all its Func objects of Creature calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the Creature</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance : Save update in the character_skills table of the database</li><br>
	 * @param newSkill The Skill to add to the Creature
	 * @return The Skill replaced or null if just added a new Skill
	 */
	@Override
	public Skill addSkill(Skill newSkill)
	{
		Skill oldSkill = null;
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			
			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{
				// if skill came with another one, we should delete the other one too.
				if (oldSkill.triggerAnotherSkill())
				{
					_triggeredSkills.remove(oldSkill.getTriggeredId());
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}
			
			// Add Func objects of newSkill to the calculator set of the Creature
			addStatFuncs(newSkill.getStatFuncs(null, this));
			if ((oldSkill != null) && (_chanceSkills != null))
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (newSkill.isChance())
			{
				addChanceSkill(newSkill);
			}
			
			if (newSkill.isChance() && newSkill.triggerAnotherSkill())
			{
				final Skill triggeredSkill = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(triggeredSkill);
			}
			
			if (newSkill.triggerAnotherSkill())
			{
				_triggeredSkills.put(newSkill.getTriggeredId(), SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel()));
			}
		}
		
		return oldSkill;
	}
	
	/**
	 * Adds the chance skill.
	 * @param skill the skill
	 */
	public void addChanceSkill(Skill skill)
	{
		synchronized (this)
		{
			if (_chanceSkills == null)
			{
				_chanceSkills = new ChanceSkillList(this);
			}
			
			_chanceSkills.put(skill, skill.getChanceCondition());
		}
	}
	
	/**
	 * Removes the chance skill.
	 * @param id the id
	 */
	public void removeChanceSkill(int id)
	{
		synchronized (this)
		{
			for (Skill skill : _chanceSkills.keySet())
			{
				if (skill.getId() == id)
				{
					_chanceSkills.remove(skill);
				}
			}
			
			if (_chanceSkills.size() == 0)
			{
				_chanceSkills = null;
			}
		}
	}
	
	/**
	 * Remove a skill from the Creature and its Func objects from calculator set of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All skills own by a Creature are identified in <b>_skills</b><br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove the skill from the Creature _skills</li>
	 * <li>Remove all its Func objects from the Creature calculator set</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance : Save update in the character_skills table of the database</li><br>
	 * @param skill The Skill to remove from the Creature
	 * @return The Skill removed
	 */
	public synchronized Skill removeSkill(Skill skill)
	{
		if (skill == null)
		{
			return null;
		}
		return removeSkill(skill.getId());
	}
	
	/**
	 * Removes the skill.
	 * @param skillId the skill id
	 * @return the skill
	 */
	public Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}
	
	/**
	 * Removes the skill.
	 * @param skillId the skill id
	 * @param cancelEffect the cancel effect
	 * @return the skill
	 */
	public Skill removeSkill(int skillId, boolean cancelEffect)
	{
		// Remove the skill from the Creature _skills
		final Skill oldSkill = _skills.remove(skillId);
		// Remove all its Func objects from the Creature calculator set
		if (oldSkill != null)
		{
			// this is just a fail-safe againts buggers and GM dummies...
			if (oldSkill.triggerAnotherSkill())
			{
				removeSkill(oldSkill.getTriggeredId(), true);
				_triggeredSkills.remove(oldSkill.getTriggeredId());
			}
			
			// Stop casting if this skill is used right now
			if ((_lastSkillCast != null) && isCastingNow() && (oldSkill.getId() == _lastSkillCast.getId()))
			{
				abortCast();
			}
			
			if ((cancelEffect || oldSkill.isToggle()) && (getFirstEffect(oldSkill) == null))
			{
				removeStatsOwner(oldSkill);
				stopSkillEffects(oldSkill.getId());
			}
			
			if (oldSkill.isChance() && (_chanceSkills != null))
			{
				removeChanceSkill(oldSkill.getId());
			}
			removeStatsOwner(oldSkill);
		}
		return oldSkill;
	}
	
	/**
	 * Return all skills own by the Creature in a table of Skill.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All skills own by a Creature are identified in <b>_skills</b> the Creature
	 * @return the all skills
	 */
	public Skill[] getAllSkills()
	{
		return _skills.values().toArray(new Skill[_skills.values().size()]);
	}
	
	/**
	 * @return the map containing this character skills.
	 */
	@Override
	public Map<Integer, Skill> getSkills()
	{
		return _skills;
	}
	
	/**
	 * Gets the chance skills.
	 * @return the chance skills
	 */
	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}
	
	/**
	 * Return the level of a skill owned by the Creature.
	 * @param skillId The identifier of the Skill whose level must be returned
	 * @return The level of the Skill identified by skillId
	 */
	@Override
	public int getSkillLevel(int skillId)
	{
		final Skill skill = _skills.get(skillId);
		return (skill == null) ? 0 : skill.getLevel();
	}
	
	/**
	 * Return True if the skill is known by the Creature.
	 * @param skillId The identifier of the Skill to check the knowledge
	 * @return the known skill
	 */
	@Override
	public Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	/**
	 * Return the number of skills of type(Buff, Debuff, HEAL_PERCENT, MANAHEAL_PERCENT) affecting this Creature.
	 * @return The number of Buffs affecting this Creature
	 */
	public int getBuffCount()
	{
		final Effect[] effects = getAllEffects();
		int numBuffs = 0;
		for (Effect e : effects)
		{
			if (e == null)
			{
				synchronized (_effects)
				{
					_effects.remove(e);
				}
				continue;
			}
			
			if (((e.getSkill().getSkillType() == SkillType.BUFF) || (e.getSkill().getId() == 1416) || (e.getSkill().getSkillType() == SkillType.REFLECT) || (e.getSkill().getSkillType() == SkillType.HEAL_PERCENT) || (e.getSkill().getSkillType() == SkillType.MANAHEAL_PERCENT)) && ((e.getSkill().getId() <= 4360) || (e.getSkill().getId() >= 4367))) // 7s
			{
				numBuffs++;
			}
		}
		
		return numBuffs;
	}
	
	/**
	 * Return the number of skills of type(Debuff, poison, slow, etc.) affecting this Creature.
	 * @return The number of debuff affecting this Creature
	 */
	public int getDeBuffCount()
	{
		final Effect[] effects = getAllEffects();
		int numDeBuffs = 0;
		for (Effect e : effects)
		{
			if (e == null)
			{
				synchronized (_effects)
				{
					_effects.remove(e);
				}
				continue;
			}
			
			// Check for all debuff skills
			if (e.getSkill().isDebuff())
			{
				numDeBuffs++;
			}
		}
		
		return numDeBuffs;
	}
	
	/**
	 * Gets the max buff count.
	 * @return the max buff count
	 */
	public int getMaxBuffCount()
	{
		return Config.BUFFS_MAX_AMOUNT + getSkillLevel(Skill.SKILL_DIVINE_INSPIRATION);
	}
	
	/**
	 * Removes the first Buff of this Creature.
	 * @param preferSkill If != 0 the given skill Id will be removed instead of first
	 */
	public void removeFirstBuff(int preferSkill)
	{
		final Effect[] effects = getAllEffects();
		Effect removeMe = null;
		for (Effect e : effects)
		{
			if (e == null)
			{
				synchronized (_effects)
				{
					_effects.remove(e);
				}
				continue;
			}
			
			if (((e.getSkill().getSkillType() == SkillType.BUFF) || (e.getSkill().getSkillType() == SkillType.REFLECT) || (e.getSkill().getSkillType() == SkillType.HEAL_PERCENT) || (e.getSkill().getSkillType() == SkillType.MANAHEAL_PERCENT)) && ((e.getSkill().getId() <= 4360) || (e.getSkill().getId() >= 4367)))
			{
				if (preferSkill == 0)
				{
					removeMe = e;
					break;
				}
				else if (e.getSkill().getId() == preferSkill)
				{
					removeMe = e;
					break;
				}
				else if (removeMe == null)
				{
					removeMe = e;
				}
			}
		}
		
		if (removeMe != null)
		{
			removeMe.exit(true);
		}
	}
	
	/**
	 * Removes the first DeBuff of this Creature.
	 * @param preferSkill If != 0 the given skill Id will be removed instead of first
	 */
	public void removeFirstDeBuff(int preferSkill)
	{
		final Effect[] effects = getAllEffects();
		Effect removeMe = null;
		for (Effect e : effects)
		{
			if (e == null)
			{
				synchronized (_effects)
				{
					_effects.remove(e);
				}
				continue;
			}
			
			if (e.getSkill().isDebuff())
			{
				if (preferSkill == 0)
				{
					removeMe = e;
					break;
				}
				else if (e.getSkill().getId() == preferSkill)
				{
					removeMe = e;
					break;
				}
				else if (removeMe == null)
				{
					removeMe = e;
				}
			}
		}
		
		if (removeMe != null)
		{
			removeMe.exit(true);
		}
	}
	
	/**
	 * Gets the dance count.
	 * @return the dance count
	 */
	public int getDanceCount()
	{
		int danceCount = 0;
		
		final Effect[] effects = getAllEffects();
		for (Effect e : effects)
		{
			if (e == null)
			{
				synchronized (_effects)
				{
					_effects.remove(e);
				}
				continue;
			}
			
			if (e.getSkill().isDance() && e.getInUse())
			{
				danceCount++;
			}
		}
		
		return danceCount;
	}
	
	/**
	 * Checks if the given skill stacks with an existing one.
	 * @param checkSkill the skill to be checked
	 * @return Returns whether or not this skill will stack
	 */
	public boolean doesStack(Skill checkSkill)
	{
		if (_effects.isEmpty() || (checkSkill.getEffectTemplates() == null) || (checkSkill.getEffectTemplates().length < 1) || (checkSkill.getEffectTemplates()[0].stackType == null))
		{
			return false;
		}
		
		final String stackType = checkSkill.getEffectTemplates()[0].stackType;
		if (stackType.equals("none"))
		{
			return false;
		}
		
		final Effect[] effects = getAllEffects();
		for (Effect e : effects)
		{
			if (e == null)
			{
				synchronized (_effects)
				{
					_effects.remove(e);
				}
				continue;
			}
			
			if ((e.getStackType() != null) && e.getStackType().equals(stackType))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all PlayerInstance of Creature _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the PlayerInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the Creature with EVT_FINISH_CASTING</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: A magic skill casting MUST BE in progress</b></font>
	 * @param targets the targets
	 * @param skill The Skill to use
	 * @param coolTime the cool time
	 * @param instant the instant
	 */
	public void onMagicLaunchedTimer(WorldObject[] targets, Skill skill, int coolTime, boolean instant)
	{
		if ((skill == null) || (((targets == null) || (targets.length <= 0)) && (skill.getTargetType() != SkillTargetType.TARGET_AURA)))
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if ((skill.getCastRange() < 0) && (skill.getSkillRadius() > 80))
		{
			escapeRange = skill.getSkillRadius();
		}
		
		WorldObject[] finalTargets = null;
		int skipped = 0;
		if (escapeRange > 0)
		{
			final List<Creature> targetList = new ArrayList<>();
			for (int i = 0; (targets != null) && (i < targets.length); i++)
			{
				if (targets[i] instanceof Creature)
				{
					if (!Util.checkIfInRange(escapeRange, this, targets[i], true))
					{
						continue;
					}
					
					// Check if the target is behind a wall
					if ((skill.getSkillRadius() > 0) && skill.isOffensive() && Config.PATHFINDING && !GeoEngine.getInstance().canSeeTarget(this, targets[i]))
					{
						skipped++;
						continue;
					}
					
					if (skill.isOffensive())
					{
						if (this instanceof PlayerInstance)
						{
							if (((Creature) targets[i]).isInsidePeaceZone((PlayerInstance) this))
							{
								continue;
							}
						}
						else if (isInsidePeaceZone(this, targets[i]))
						{
							continue;
						}
					}
					targetList.add((Creature) targets[i]);
				}
			}
			if (targetList.isEmpty() && (skill.getTargetType() != SkillTargetType.TARGET_AURA))
			{
				if (this instanceof PlayerInstance)
				{
					for (int i = 0; i < skipped; i++)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SEE_TARGET));
					}
				}
				
				abortCast();
				return;
			}
			finalTargets = targetList.toArray(new Creature[targetList.size()]);
		}
		else
		{
			finalTargets = targets;
		}
		
		// if the skill is not a potion and player is not casting now
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if (!skill.isPotion() && (!isCastingNow() || isAlikeDead()))
		{
			_skillCast = null;
			enableAllSkills();
			
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			_castEndTime = 0;
			_castInterruptTime = 0;
			return;
		}
		
		// Get the display identifier of the skill
		final int magicId = skill.getDisplayId();
		
		// Get the level of the skill
		final int level = getSkillLevel(skill.getId());
		
		// Send a Server->Client packet MagicSkillLaunched to the Creature AND to all PlayerInstance in the _KnownPlayers of the Creature
		if (!skill.isPotion())
		{
			broadcastPacket(new MagicSkillLaunched(this, magicId, level, finalTargets));
		}
		
		if (instant)
		{
			onMagicHitTimer(finalTargets, skill, coolTime, true);
		}
		else if (skill.isPotion())
		{
			_potionCast = ThreadPool.schedule(new MagicUseTask(finalTargets, skill, coolTime, 2), 200);
		}
		else
		{
			_skillCast = ThreadPool.schedule(new MagicUseTask(finalTargets, skill, coolTime, 2), 200);
		}
	}
	
	/**
	 * On magic hit timer.
	 * @param targets the targets
	 * @param skill the skill
	 * @param coolTime the cool time
	 * @param instant the instant
	 */
	public void onMagicHitTimer(WorldObject[] targets, Skill skill, int coolTime, boolean instant)
	{
		if ((skill == null) || (((targets == null) || (targets.length <= 0)) && (skill.getTargetType() != SkillTargetType.TARGET_AURA)))
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			return;
		}
		
		if (_forceBuff != null)
		{
			_skillCast = null;
			enableAllSkills();
			
			_forceBuff.onCastAbort();
			
			return;
		}
		
		final Effect mog = getFirstEffect(Effect.EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			_skillCast = null;
			enableAllSkills();
			
			// close skill if it's not SIGNET_CASTTIME
			if (mog.getSkill().getSkillType() != SkillType.SIGNET_CASTTIME)
			{
				mog.exit(true);
			}
			
			final WorldObject target = targets == null ? null : targets[0];
			if (target != null)
			{
				notifyQuestEventSkillFinished(skill, target);
			}
			return;
		}
		
		final WorldObject[] targets2 = targets;
		try
		{
			if ((targets2 != null) && (targets2.length != 0))
			{
				// Go through targets table
				for (WorldObject target2 : targets2)
				{
					if (target2 == null)
					{
						continue;
					}
					
					if (target2 instanceof Playable)
					{
						final Creature target = (Creature) target2;
						
						// If the skill is type STEALTH(ex: Dance of Shadow)
						if (skill.isAbnormalEffectByName(ABNORMAL_EFFECT_STEALTH))
						{
							final Effect silentMove = target.getFirstEffect(Effect.EffectType.SILENT_MOVE);
							if (silentMove != null)
							{
								silentMove.exit(true);
							}
						}
						
						if ((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.SEED))
						{
							final SystemMessage smsg = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
							smsg.addString(skill.getName());
							target.sendPacket(smsg);
						}
						
						if ((this instanceof PlayerInstance) && (target instanceof Summon))
						{
							((Summon) target).getOwner().sendPacket(new PetInfo((Summon) target));
							sendPacket(new NpcInfo((Summon) target, this));
							
							// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
							((Summon) target).updateEffectIcons(true);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
		
		try
		{
			final StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;
			
			// Consume MP of the Creature and Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform
			final double mpConsume = getStat().getMpConsume(skill);
			if (mpConsume > 0)
			{
				if (skill.isDance())
				{
					getStatus().reduceMp(calcStat(Stat.DANCE_MP_CONSUME_RATE, mpConsume, null, null));
				}
				else if (skill.isMagic())
				{
					getStatus().reduceMp(calcStat(Stat.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null));
				}
				else
				{
					getStatus().reduceMp(calcStat(Stat.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null));
				}
				
				su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
				isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;
				consumeHp = calcStat(Stat.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if ((consumeHp + 1) >= getStatus().getCurrentHp())
				{
					consumeHp = getStatus().getCurrentHp() - 1.0;
				}
				
				getStatus().reduceHp(consumeHp, this);
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				isSendStatus = true;
			}
			
			// Send a Server->Client packet StatusUpdate with MP modification to the PlayerInstance
			if (isSendStatus)
			{
				sendPacket(su);
			}
			
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the Creature
			if (skill.getItemConsume() > 0)
			{
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
			}
			
			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets);
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
		
		if (instant || (coolTime == 0))
		{
			onMagicFinalizer(targets, skill);
		}
		else if (skill.isPotion())
		{
			_potionCast = ThreadPool.schedule(new MagicUseTask(targets, skill, coolTime, 3), coolTime);
		}
		else
		{
			_skillCast = ThreadPool.schedule(new MagicUseTask(targets, skill, coolTime, 3), coolTime);
		}
	}
	
	/**
	 * On magic finalizer.
	 * @param targets the targets
	 * @param skill the skill
	 */
	public void onMagicFinalizer(WorldObject[] targets, Skill skill)
	{
		if (skill.isPotion())
		{
			_potionCast = null;
			_castPotionEndTime = 0;
			_castPotionInterruptTime = 0;
		}
		else
		{
			_skillCast = null;
			_castEndTime = 0;
			_castInterruptTime = 0;
			enableAllSkills();
			
			// if the skill has changed the character's state to something other than STATE_CASTING
			// then just leave it that way, otherwise switch back to STATE_IDLE.
			if ((skill.getId() != 345) && (skill.getId() != 346))
			{
				// Like L2OFF while use a skill and next interntion == null the char stop auto attack
				if (((getAI().getNextIntention() == null) && ((skill.getSkillType() == SkillType.PDAM) && (skill.getCastRange() < 400))) || (skill.getSkillType() == SkillType.BLOW) || (skill.getSkillType() == SkillType.DRAIN_SOUL) || (skill.getSkillType() == SkillType.SOW) || (skill.getSkillType() == SkillType.SPOIL))
				{
					if (this instanceof PlayerInstance)
					{
						final PlayerInstance currPlayer = (PlayerInstance) this;
						final SkillDat skilldat = currPlayer.getCurrentSkill();
						// Like L2OFF if the skill is BLOW the player doesn't auto attack
						// If on XML skill nextActionAttack = true the char auto attack
						// If CTRL is pressed the autoattack is aborted (like L2OFF)
						if ((skilldat != null) && !skilldat.isCtrlPressed() && skill.nextActionIsAttack() && (_target != null) && (_target instanceof Creature))
						{
							getAI().setIntention(AI_INTENTION_ATTACK, _target);
						}
					}
					else if (skill.nextActionIsAttack() && (_target != null) && (_target instanceof Creature))
					{
						getAI().setIntention(AI_INTENTION_ATTACK, _target);
					}
					else if ((skill.isOffensive()) && (skill.getSkillType() != SkillType.UNLOCK) && (skill.getSkillType() != SkillType.BLOW) && (skill.getSkillType() != SkillType.DELUXE_KEY_UNLOCK) && (skill.getId() != 345) && (skill.getId() != 346))
					{
						getAI().setIntention(AI_INTENTION_ATTACK, _target);
						getAI().clientStartAutoAttack();
					}
				}
				if (this instanceof PlayerInstance)
				{
					final PlayerInstance currPlayer = (PlayerInstance) this;
					final SkillDat skilldat = currPlayer.getCurrentSkill();
					if ((skilldat != null) && !skilldat.isCtrlPressed() && (skill.isOffensive()) && (skill.getSkillType() != SkillType.UNLOCK) && (skill.getSkillType() != SkillType.BLOW) && (skill.getSkillType() != SkillType.DELUXE_KEY_UNLOCK) && (skill.getId() != 345) && (skill.getId() != 346))
					{
						if (!skill.isMagic() && skill.nextActionIsAttack())
						{
							getAI().setIntention(AI_INTENTION_ATTACK, _target);
						}
						
						getAI().clientStartAutoAttack();
					}
				}
				else if ((skill.isOffensive()) && (skill.getSkillType() != SkillType.UNLOCK) && (skill.getSkillType() != SkillType.BLOW) && (skill.getSkillType() != SkillType.DELUXE_KEY_UNLOCK) && (skill.getId() != 345) && (skill.getId() != 346))
				{
					if (!skill.isMagic())
					{
						getAI().setIntention(AI_INTENTION_ATTACK, _target);
					}
					
					getAI().clientStartAutoAttack();
				}
			}
			else
			{
				getAI().clientStopAutoAttack();
			}
			
			// Notify the AI of the Creature with EVT_FINISH_CASTING
			getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
			
			notifyQuestEventSkillFinished(skill, _target);
			
			// If character is a player, then wipe their current cast state and check if a skill is queued. If there is a queued skill, launch it and wipe the queue.
			if (this instanceof PlayerInstance)
			{
				final PlayerInstance currPlayer = (PlayerInstance) this;
				final SkillDat queuedSkill = currPlayer.getQueuedSkill();
				currPlayer.setCurrentSkill(null, false, false);
				if (queuedSkill != null)
				{
					currPlayer.setQueuedSkill(null, false, false);
					
					// DON'T USE : Recursive call to useMagic() method
					// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
					ThreadPool.execute(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
				}
				
				final Weapon activeWeapon = getActiveWeaponItem();
				// Launch weapon Special ability skill effect if available
				if (activeWeapon != null)
				{
					try
					{
						if ((targets != null) && (targets.length > 0))
						{
							for (WorldObject target : targets)
							{
								if ((target instanceof Creature) && !((Creature) target).isDead() && activeWeapon.getSkillEffects(this, (Creature) target, skill))
								{
									sendPacket(SystemMessage.sendString("Target affected by weapon special ability!"));
								}
							}
						}
					}
					catch (Exception e)
					{
						LOGGER.warning(e.toString());
					}
				}
			}
		}
	}
	
	/**
	 * Notify quest event skill finished.
	 * @param skill the skill
	 * @param target the target
	 */
	private void notifyQuestEventSkillFinished(Skill skill, WorldObject target)
	{
		if ((this instanceof NpcInstance) && ((target instanceof PlayerInstance) || (target instanceof Summon)))
		{
			final PlayerInstance player = target instanceof PlayerInstance ? (PlayerInstance) target : ((Summon) target).getOwner();
			for (Quest quest : ((NpcTemplate) _template).getEventQuests(EventType.ON_SPELL_FINISHED))
			{
				quest.notifySpellFinished(((NpcInstance) this), player, skill);
			}
		}
	}
	
	/**
	 * Reduce the item number of the Creature.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance</li><br>
	 * @param itemConsumeId the item consume id
	 * @param itemCount the item count
	 */
	public void consumeItem(int itemConsumeId, int itemCount)
	{
	}
	
	/**
	 * Enable a skill (remove it from _disabledSkills of the Creature).<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All skills disabled are identified by their skillId in <b>_disabledSkills</b> of the Creature
	 * @param skill
	 */
	public void enableSkill(Skill skill)
	{
		_disabledSkills.remove(skill);
		if (isPlayer())
		{
			getActingPlayer().removeTimestamp(skill);
		}
	}
	
	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill the skill thats going to be disabled
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(Skill skill, long delay)
	{
		if (skill == null)
		{
			return;
		}
		
		if (!_disabledSkills.contains(skill))
		{
			_disabledSkills.add(skill);
		}
		
		if (delay > 10)
		{
			ThreadPool.schedule(new EnableSkill(this, skill), delay);
		}
	}
	
	/**
	 * Check if a skill is disabled.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All skills disabled are identified by their skillId in <b>_disabledSkills</b> of the Creature
	 * @param skill the skill to know if its disabled
	 * @return true, if is skill disabled
	 */
	public boolean isSkillDisabled(Skill skill)
	{
		if (isAllSkillsDisabled() && !skill.isPotion())
		{
			return true;
		}
		
		if (isPlayer())
		{
			final PlayerInstance activeChar = getActingPlayer();
			if (((skill.getSkillType() == SkillType.FISHING) || (skill.getSkillType() == SkillType.REELING) || (skill.getSkillType() == SkillType.PUMPING)) && !activeChar.isFishing() && ((activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getItemType() != WeaponType.ROD)))
			{
				if (skill.getSkillType() == SkillType.PUMPING)
				{
					// Pumping skill is available only while fishing
					activeChar.sendPacket(SystemMessageId.YOU_MAY_ONLY_USE_THE_PUMPING_SKILL_WHILE_YOU_ARE_FISHING);
				}
				else if (skill.getSkillType() == SkillType.REELING)
				{
					// Reeling skill is available only while fishing
					activeChar.sendPacket(SystemMessageId.YOU_MAY_ONLY_USE_THE_REELING_SKILL_WHILE_YOU_ARE_FISHING);
				}
				else if (skill.getSkillType() == SkillType.FISHING)
				{
					// Player hasn't fishing pole equiped
					activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_FISHING_POLE_EQUIPPED);
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addString(skill.getName());
				activeChar.sendPacket(sm);
				return true;
			}
			
			if (((skill.getSkillType() == SkillType.FISHING) || (skill.getSkillType() == SkillType.REELING) || (skill.getSkillType() == SkillType.PUMPING)) && (activeChar.getActiveWeaponItem() == null))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addString(skill.getName());
				activeChar.sendPacket(sm);
				return true;
			}
			
			if (((skill.getSkillType() == SkillType.REELING) || (skill.getSkillType() == SkillType.PUMPING)) && !activeChar.isFishing() && ((activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getItemType() == WeaponType.ROD)))
			{
				if (skill.getSkillType() == SkillType.PUMPING)
				{
					// Pumping skill is available only while fishing
					activeChar.sendPacket(SystemMessageId.YOU_MAY_ONLY_USE_THE_PUMPING_SKILL_WHILE_YOU_ARE_FISHING);
				}
				else if (skill.getSkillType() == SkillType.REELING)
				{
					// Reeling skill is available only while fishing
					activeChar.sendPacket(SystemMessageId.YOU_MAY_ONLY_USE_THE_REELING_SKILL_WHILE_YOU_ARE_FISHING);
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addString(skill.getName());
				activeChar.sendPacket(sm);
				return true;
			}
			
			if (activeChar.isHero() && HeroSkillTable.isHeroSkill(skill.getId()) && activeChar.isInOlympiadMode() && activeChar.isOlympiadStart())
			{
				activeChar.sendMessage("You can't use Hero skills during Olympiad match.");
				return true;
			}
		}
		
		return _disabledSkills.contains(skill);
	}
	
	/**
	 * Disable all skills (set _allSkillsDisabled to True).
	 */
	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}
	
	/**
	 * Enable all skills (set _allSkillsDisabled to False).
	 */
	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}
	
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.
	 * @param skill The Skill to use
	 * @param targets The table of WorldObject targets
	 */
	public void callSkill(Skill skill, WorldObject[] targets)
	{
		try
		{
			if (skill.isToggle() && (getFirstEffect(skill.getId()) != null))
			{
				return;
			}
			
			if ((targets == null) || (targets.length == 0))
			{
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
			
			// Do initial checkings for skills and set pvp flag/draw aggro when needed
			for (WorldObject target : targets)
			{
				if (target instanceof Creature)
				{
					// Set some values inside target's instance for later use
					final Creature creature = (Creature) target;
					if ((skill.getEffectType() == SkillType.BUFF) && creature.isBlockBuff())
					{
						continue;
					}
					
					if (target instanceof Creature)
					{
						final Creature targ = (Creature) target;
						if (ChanceSkillList.canTriggerByCast(this, targ, skill))
						{
							// Maybe launch chance skills on us
							if (_chanceSkills != null)
							{
								_chanceSkills.onSkillHit(targ, false, skill.isMagic(), skill.isOffensive());
							}
							// Maybe launch chance skills on target
							if (targ.getChanceSkills() != null)
							{
								targ.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
							}
						}
					}
					
					if (Config.ALLOW_RAID_BOSS_PETRIFIED && ((this instanceof PlayerInstance) || (this instanceof Summon))) // Check if option is True Or False.
					{
						boolean toBeCursed = false;
						
						// check on BossZone raid lvl
						if (!(creature.getTarget() instanceof Playable) && !(creature.getTarget() instanceof SummonInstance))
						{
							// this must work just on mobs/raids
							if ((creature.isRaid() && (getLevel() > (creature.getLevel() + 8))) || (!(creature instanceof PlayerInstance) && ((creature.getTarget() instanceof RaidBossInstance) && (getLevel() > (((RaidBossInstance) creature.getTarget()).getLevel() + 8)))) || (!(creature instanceof PlayerInstance) && ((creature.getTarget() instanceof GrandBossInstance) && (getLevel() > (((GrandBossInstance) creature.getTarget()).getLevel() + 8)))))
							{
								toBeCursed = true;
							}
							
							// advanced check too if not already cursed
							if (!toBeCursed)
							{
								int bossId = -1;
								NpcTemplate bossTemplate = null;
								final BossZone bossZone = GrandBossManager.getInstance().getZone(this);
								if (bossZone != null)
								{
									bossId = bossZone.getBossId();
								}
								
								if (bossId != -1)
								{
									bossTemplate = NpcTable.getInstance().getTemplate(bossId);
									if ((bossTemplate != null) && (getLevel() > (bossTemplate.getLevel() + 8)))
									{
										MonsterInstance bossInstance = null;
										if (bossTemplate.getType().equals("RaidBoss"))
										{
											if (RaidBossSpawnManager.getInstance().getStatSet(bossId) != null)
											{
												bossInstance = RaidBossSpawnManager.getInstance().getBoss(bossId);
											}
										}
										else if (bossTemplate.getType().equals("GrandBoss"))
										{
											if (GrandBossManager.getInstance().getStatSet(bossId) != null)
											{
												bossInstance = GrandBossManager.getInstance().getBoss(bossId);
											}
										}
										
										// max allowed rage into take cursed is 3000
										if ((bossInstance != null/* && alive */) && bossInstance.isInsideRadius(this, 3000, false, false))
										{
											toBeCursed = true;
										}
									}
								}
							}
						}
						
						if (toBeCursed)
						{
							if (skill.isMagic())
							{
								final Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
								if (tempSkill != null)
								{
									abortAttack();
									abortCast();
									getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
									tempSkill.getEffects(creature, this, false, false, false);
									if (this instanceof Summon)
									{
										final Summon src = ((Summon) this);
										if (src.getOwner() != null)
										{
											src.getOwner().abortAttack();
											src.getOwner().abortCast();
											src.getOwner().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
											tempSkill.getEffects(creature, src.getOwner(), false, false, false);
										}
									}
								}
								else
								{
									LOGGER.warning("Skill 4215 at level 1 is missing in DP.");
								}
							}
							else
							{
								final Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
								if (tempSkill != null)
								{
									tempSkill.getEffects(creature, this, false, false, false);
								}
								else
								{
									LOGGER.warning("Skill 4515 at level 1 is missing in DP.");
								}
								
								if (creature instanceof MinionInstance)
								{
									((MinionInstance) creature).getLeader().stopHating(this);
									final List<MinionInstance> spawnedMinions = ((MonsterInstance) creature).getSpawnedMinions();
									if ((spawnedMinions != null) && !spawnedMinions.isEmpty())
									{
										final Iterator<MinionInstance> itr = spawnedMinions.iterator();
										MinionInstance minion;
										while (itr.hasNext())
										{
											minion = itr.next();
											if (((Attackable) creature).getMostHated() == null)
											{
												((AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.setWalking();
											}
											if ((minion != null) && !minion.isDead())
											{
												((AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.addDamage(((Attackable) creature).getMostHated(), 100);
											}
										}
									}
								}
								else
								{
									((Attackable) creature).stopHating(this);
									final List<MinionInstance> spawnedMinions = ((MonsterInstance) creature).getSpawnedMinions();
									if ((spawnedMinions != null) && !spawnedMinions.isEmpty())
									{
										final Iterator<MinionInstance> itr = spawnedMinions.iterator();
										MinionInstance minion;
										while (itr.hasNext())
										{
											minion = itr.next();
											if (((Attackable) creature).getMostHated() == null)
											{
												((AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.setWalking();
											}
											if ((minion != null) && !minion.isDead())
											{
												((AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.addDamage(((Attackable) creature).getMostHated(), 100);
											}
										}
									}
								}
							}
							return;
						}
					}
					
					PlayerInstance activeChar = null;
					if (this instanceof PlayerInstance)
					{
						activeChar = (PlayerInstance) this;
					}
					else if (this instanceof Summon)
					{
						activeChar = ((Summon) this).getOwner();
					}
					
					if (activeChar != null)
					{
						if (skill.isOffensive())
						{
							if ((creature instanceof PlayerInstance) || (creature instanceof Summon))
							{
								// Signets are a special case, casted on target_self but don't harm self
								if ((skill.getSkillType() != SkillType.SIGNET) && (skill.getSkillType() != SkillType.SIGNET_CASTTIME))
								{
									creature.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
									activeChar.updatePvPStatus(creature);
								}
							}
							else if (creature instanceof Attackable)
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
									{
										break;
									}
									default:
									{
										((Creature) target).addAttackerToAttackByList(this);
										int hitTime = Formulas.getInstance().calcMAtkSpd(activeChar, skill, skill.getHitTime());
										if ((checkBss() || checkSps()) && !skill.isStaticHitTime() && !skill.isPotion() && skill.isMagic())
										{
											hitTime = (int) (0.70 * hitTime);
										}
										ThreadPool.schedule(new notifyAiTaskDelayed(CtrlEvent.EVT_ATTACKED, this, target), hitTime);
										break;
									}
								}
							}
						}
						else if (creature instanceof PlayerInstance)
						{
							// Casting non offensive skill on player with pvp flag set or with karma
							if (!creature.equals(this) && ((((PlayerInstance) creature).getPvpFlag() > 0) || (((PlayerInstance) creature).getKarma() > 0)))
							{
								activeChar.updatePvPStatus();
							}
						}
						else if ((creature instanceof Attackable) && (skill.getSkillType() != SkillType.SUMMON) && (skill.getSkillType() != SkillType.BEAST_FEED) && (skill.getSkillType() != SkillType.UNLOCK) && (skill.getSkillType() != SkillType.DELUXE_KEY_UNLOCK))
						{
							activeChar.updatePvPStatus(this);
						}
					}
				}
				if (target instanceof MonsterInstance)
				{
					if (!skill.isOffensive() && (skill.getSkillType() != SkillType.UNLOCK) && (skill.getSkillType() != SkillType.SUMMON) && (skill.getSkillType() != SkillType.DELUXE_KEY_UNLOCK) && (skill.getSkillType() != SkillType.BEAST_FEED))
					{
						PlayerInstance activeChar = null;
						if (this instanceof PlayerInstance)
						{
							activeChar = (PlayerInstance) this;
							activeChar.updatePvPStatus(activeChar);
						}
						else if (this instanceof Summon)
						{
							activeChar = ((Summon) this).getOwner();
						}
					}
				}
			}
			
			ISkillHandler handler = null;
			// Check if the skill effects are already in progress on the Creature
			if (skill.isToggle() && (getFirstEffect(skill.getId()) != null))
			{
				handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
				if (handler != null)
				{
					handler.useSkill(this, skill, targets);
				}
				else
				{
					skill.useSkill(this, targets);
				}
				return;
			}
			
			// Check if over-hit is possible
			if (skill.isOverhit())
			{
				// Set the "over-hit enabled" flag on each of the possible targets
				for (WorldObject target : targets)
				{
					final Creature creature = (Creature) target;
					if (creature instanceof Attackable)
					{
						((Attackable) creature).overhitEnabled(true);
					}
				}
			}
			
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			
			// Launch the magic skill and calculate its effects
			if (handler != null)
			{
				handler.useSkill(this, skill, targets);
			}
			else
			{
				skill.useSkill(this, targets);
			}
			
			// if the skill is a potion, must delete the potion item
			if (skill.isPotion() && (this instanceof Playable))
			{
				Potions.deletePotionItem((Playable) this, skill.getId(), skill.getLevel());
			}
			
			if ((this instanceof PlayerInstance) || (this instanceof Summon))
			{
				final PlayerInstance caster = this instanceof PlayerInstance ? (PlayerInstance) this : ((Summon) this).getOwner();
				for (WorldObject target : targets)
				{
					if (target instanceof NpcInstance)
					{
						final NpcInstance npc = (NpcInstance) target;
						for (Quest quest : npc.getTemplate().getEventQuests(EventType.ON_SKILL_USE))
						{
							quest.notifySkillUse(npc, caster, skill);
						}
					}
				}
				
				if (skill.getAggroPoints() > 0)
				{
					for (WorldObject spMob : caster.getKnownList().getKnownObjects().values())
					{
						if (spMob instanceof NpcInstance)
						{
							final NpcInstance npcMob = (NpcInstance) spMob;
							if (npcMob.isInsideRadius(caster, 1000, true, true) && npcMob.hasAI() && (npcMob.getAI().getIntention() == AI_INTENTION_ATTACK))
							{
								final WorldObject npcTarget = npcMob.getTarget();
								for (WorldObject target : targets)
								{
									if ((npcTarget == target) || (npcMob == target))
									{
										npcMob.seeSpell(caster, target, skill);
									}
								}
							}
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
	 * See spell.
	 * @param caster the caster
	 * @param target the target
	 * @param skill the skill
	 */
	public void seeSpell(PlayerInstance caster, WorldObject target, Skill skill)
	{
		if (this instanceof Attackable)
		{
			((Attackable) this).addDamageHate(caster, 0, -skill.getAggroPoints());
		}
	}
	
	/**
	 * Return True if the Creature is behind the target and can't be seen.
	 * @param target the target
	 * @return true, if is behind
	 */
	public boolean isBehind(WorldObject target)
	{
		double angleChar; //
		double angleTarget;
		double angleDiff;
		final double maxAngleDiff = 40;
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof Creature)
		{
			((Creature) target).sendPacket(new ValidateLocation(this));
			sendPacket(new ValidateLocation(((Creature) target)));
			
			final Creature target1 = (Creature) target;
			angleChar = Util.calculateAngleFrom(target1, this);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= (-360 + maxAngleDiff))
			{
				angleDiff += 360;
			}
			
			if (angleDiff >= (360 - maxAngleDiff))
			{
				angleDiff -= 360;
			}
			
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if is behind target.
	 * @return true, if is behind target
	 */
	public boolean isBehindTarget()
	{
		return isBehind(_target);
	}
	
	/**
	 * Returns true if target is in front of Creature (shield def etc).
	 * @param target the target
	 * @param maxAngle the max angle
	 * @return true, if is facing
	 */
	public boolean isFacing(WorldObject target, int maxAngle)
	{
		double angleChar;
		double angleTarget;
		double angleDiff;
		double maxAngleDiff;
		if (target == null)
		{
			return false;
		}
		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(_heading);
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		if (Math.abs(angleDiff) <= maxAngleDiff)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Return True if the Creature is behind the target and can't be seen.
	 * @param target the target
	 * @return true, if is front
	 */
	public boolean isFront(WorldObject target)
	{
		double angleChar;
		double angleTarget;
		double angleDiff;
		final double maxAngleDiff = 40;
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof Creature)
		{
			((Creature) target).sendPacket(new ValidateLocation(this));
			sendPacket(new ValidateLocation(((Creature) target)));
			
			final Creature target1 = (Creature) target;
			angleChar = Util.calculateAngleFrom(target1, this);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= (-180 + maxAngleDiff))
			{
				angleDiff += 180;
			}
			
			if (angleDiff >= (180 - maxAngleDiff))
			{
				angleDiff -= 180;
			}
			
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				return !isBehind(_target);
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if is front target.
	 * @return true, if is front target
	 */
	public boolean isFrontTarget()
	{
		return isFront(_target);
	}
	
	/**
	 * Return True if the Creature is side the target and can't be seen.
	 * @param target the target
	 * @return true, if is side
	 */
	public boolean isSide(WorldObject target)
	{
		if (target == null)
		{
			return false;
		}
		
		if ((target instanceof Creature) && (isBehind(_target) || isFront(_target)))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if is side target.
	 * @return true, if is side target
	 */
	public boolean isSideTarget()
	{
		return isSide(_target);
	}
	
	/**
	 * Return 1.
	 * @return the level mod
	 */
	public double getLevelMod()
	{
		return 1;
	}
	
	/**
	 * Sets the skill cast.
	 * @param newSkillCast the new skill cast
	 */
	public void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	
	/**
	 * Sets the skill cast end time.
	 * @param newSkillCastEndTime the new skill cast end time
	 */
	public void setSkillCastEndTime(int newSkillCastEndTime)
	{
		_castEndTime = newSkillCastEndTime;
		// for interrupt -12 ticks; first removing the extra second and then -200 ms
		_castInterruptTime = newSkillCastEndTime - 12;
	}
	
	/** The _ pvp reg task. */
	private Future<?> _PvPRegTask;
	
	/** The _pvp flag lasts. */
	long _pvpFlagLasts;
	
	/**
	 * Sets the pvp flag lasts.
	 * @param time the new pvp flag lasts
	 */
	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}
	
	/**
	 * Gets the pvp flag lasts.
	 * @return the pvp flag lasts
	 */
	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}
	
	/**
	 * Start pvp flag.
	 */
	public void startPvPFlag()
	{
		updatePvPFlag(1);
		
		_PvPRegTask = ThreadPool.scheduleAtFixedRate(new PvPFlag(), 1000, 1000);
	}
	
	/**
	 * Stop pvp reg task.
	 */
	public void stopPvpRegTask()
	{
		if (_PvPRegTask != null)
		{
			_PvPRegTask.cancel(true);
		}
	}
	
	/**
	 * Stop pvp flag.
	 */
	public void stopPvPFlag()
	{
		stopPvpRegTask();
		
		updatePvPFlag(0);
		
		_PvPRegTask = null;
	}
	
	/**
	 * Update pvp flag.
	 * @param value the value
	 */
	public void updatePvPFlag(int value)
	{
	}
	
	/**
	 * Return a Random Damage in function of the weapon.
	 * @param target the target
	 * @return the random damage
	 */
	public int getRandomDamage(Creature target)
	{
		final Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
		{
			return 5 + (int) Math.sqrt(getLevel());
		}
		return weaponItem.getRandomDamage();
	}
	
	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}
	
	/**
	 * Gets the attack end time.
	 * @return the attack end time
	 */
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	/**
	 * Not Implemented.
	 * @return the level
	 */
	public abstract int getLevel();
	
	/**
	 * Calc stat.
	 * @param stat the stat
	 * @param init the init
	 * @param target the target
	 * @param skill the skill
	 * @return the double
	 */
	public double calcStat(Stat stat, double init, Creature target, Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	// Property - Public
	/**
	 * Gets the accuracy.
	 * @return the accuracy
	 */
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	/**
	 * Gets the attack speed multiplier.
	 * @return the attack speed multiplier
	 */
	public float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}
	
	/**
	 * Gets the cON.
	 * @return the cON
	 */
	public int getCON()
	{
		return getStat().getCON();
	}
	
	/**
	 * Gets the dEX.
	 * @return the dEX
	 */
	public int getDEX()
	{
		return getStat().getDEX();
	}
	
	/**
	 * Gets the critical dmg.
	 * @param target the target
	 * @param init the init
	 * @return the critical dmg
	 */
	public double getCriticalDmg(Creature target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}
	
	/**
	 * Gets the critical hit.
	 * @param target the target
	 * @param skill the skill
	 * @return the critical hit
	 */
	public int getCriticalHit(Creature target, Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	/**
	 * Gets the evasion rate.
	 * @param target the target
	 * @return the evasion rate
	 */
	public int getEvasionRate(Creature target)
	{
		return getStat().getEvasionRate(target);
	}
	
	/**
	 * Gets the iNT.
	 * @return the iNT
	 */
	public int getINT()
	{
		return getStat().getINT();
	}
	
	/**
	 * Gets the magical attack range.
	 * @param skill the skill
	 * @return the magical attack range
	 */
	public int getMagicalAttackRange(Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}
	
	/**
	 * Gets the max cp.
	 * @return the max cp
	 */
	public int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	/**
	 * Gets the m atk.
	 * @param target the target
	 * @param skill the skill
	 * @return the m atk
	 */
	public int getMAtk(Creature target, Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	/**
	 * Gets the m atk spd.
	 * @return the m atk spd
	 */
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	/**
	 * Gets the max mp.
	 * @return the max mp
	 */
	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	/**
	 * Gets the max hp.
	 * @return the max hp
	 */
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	/**
	 * Gets the m critical hit.
	 * @param target the target
	 * @param skill the skill
	 * @return the m critical hit
	 */
	public int getMCriticalHit(Creature target, Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	/**
	 * Gets the m def.
	 * @param target the target
	 * @param skill the skill
	 * @return the m def
	 */
	public int getMDef(Creature target, Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	/**
	 * Gets the mEN.
	 * @return the mEN
	 */
	public int getMEN()
	{
		return getStat().getMEN();
	}
	
	/**
	 * Gets the m reuse rate.
	 * @param skill the skill
	 * @return the m reuse rate
	 */
	public double getMReuseRate(Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}
	
	/**
	 * Gets the movement speed multiplier.
	 * @return the movement speed multiplier
	 */
	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}
	
	/**
	 * Gets the p atk.
	 * @param target the target
	 * @return the p atk
	 */
	public int getPAtk(Creature target)
	{
		return getStat().getPAtk(target);
	}
	
	/**
	 * Gets the p atk animals.
	 * @param target the target
	 * @return the p atk animals
	 */
	public double getPAtkAnimals(Creature target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	/**
	 * Gets the p atk dragons.
	 * @param target the target
	 * @return the p atk dragons
	 */
	public double getPAtkDragons(Creature target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	/**
	 * Gets the p atk angels.
	 * @param target the target
	 * @return the p atk angels
	 */
	public double getPAtkAngels(Creature target)
	{
		return getStat().getPAtkAngels(target);
	}
	
	/**
	 * Gets the p atk insects.
	 * @param target the target
	 * @return the p atk insects
	 */
	public double getPAtkInsects(Creature target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	/**
	 * Gets the p atk monsters.
	 * @param target the target
	 * @return the p atk monsters
	 */
	public double getPAtkMonsters(Creature target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	/**
	 * Gets the p atk plants.
	 * @param target the target
	 * @return the p atk plants
	 */
	public double getPAtkPlants(Creature target)
	{
		return getStat().getPAtkPlants(target);
	}
	
	/**
	 * Gets the p atk spd.
	 * @return the p atk spd
	 */
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	/**
	 * Gets the p atk undead.
	 * @param target the target
	 * @return the p atk undead
	 */
	public double getPAtkUndead(Creature target)
	{
		return getStat().getPAtkUndead(target);
	}
	
	/**
	 * Gets the p def undead.
	 * @param target the target
	 * @return the p def undead
	 */
	public double getPDefUndead(Creature target)
	{
		return getStat().getPDefUndead(target);
	}
	
	/**
	 * Gets the p def plants.
	 * @param target the target
	 * @return the p def plants
	 */
	public double getPDefPlants(Creature target)
	{
		return getStat().getPDefPlants(target);
	}
	
	/**
	 * Gets the p def insects.
	 * @param target the target
	 * @return the p def insects
	 */
	public double getPDefInsects(Creature target)
	{
		return getStat().getPDefInsects(target);
	}
	
	/**
	 * Gets the p def animals.
	 * @param target the target
	 * @return the p def animals
	 */
	public double getPDefAnimals(Creature target)
	{
		return getStat().getPDefAnimals(target);
	}
	
	/**
	 * Gets the p def monsters.
	 * @param target the target
	 * @return the p def monsters
	 */
	public double getPDefMonsters(Creature target)
	{
		return getStat().getPDefMonsters(target);
	}
	
	/**
	 * Gets the p def dragons.
	 * @param target the target
	 * @return the p def dragons
	 */
	public double getPDefDragons(Creature target)
	{
		return getStat().getPDefDragons(target);
	}
	
	/**
	 * Gets the p def angels.
	 * @param target the target
	 * @return the p def angels
	 */
	public double getPDefAngels(Creature target)
	{
		return getStat().getPDefAngels(target);
	}
	
	/**
	 * Gets the p def.
	 * @param target the target
	 * @return the p def
	 */
	public int getPDef(Creature target)
	{
		return getStat().getPDef(target);
	}
	
	/**
	 * Gets the p atk giants.
	 * @param target the target
	 * @return the p atk giants
	 */
	public double getPAtkGiants(Creature target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	/**
	 * Gets the p atk magic creatures.
	 * @param target the target
	 * @return the p atk magic creatures
	 */
	public double getPAtkMagicCreatures(Creature target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}
	
	/**
	 * Gets the p def giants.
	 * @param target the target
	 * @return the p def giants
	 */
	public double getPDefGiants(Creature target)
	{
		return getStat().getPDefGiants(target);
	}
	
	/**
	 * Gets the p def magic creatures.
	 * @param target the target
	 * @return the p def magic creatures
	 */
	public double getPDefMagicCreatures(Creature target)
	{
		return getStat().getPDefMagicCreatures(target);
	}
	
	/**
	 * Gets the physical attack range.
	 * @return the physical attack range
	 */
	public int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}
	
	/**
	 * Gets the run speed.
	 * @return the run speed
	 */
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	/**
	 * Gets the shld def.
	 * @return the shld def
	 */
	public int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	/**
	 * Gets the sTR.
	 * @return the sTR
	 */
	public int getSTR()
	{
		return getStat().getSTR();
	}
	
	/**
	 * Gets the walk speed.
	 * @return the walk speed
	 */
	public int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}
	
	/**
	 * Gets the wIT.
	 * @return the wIT
	 */
	public int getWIT()
	{
		return getStat().getWIT();
	}
	
	/**
	 * Adds the status listener.
	 * @param object the object
	 */
	public void addStatusListener(Creature object)
	{
		getStatus().addStatusListener(object);
	}
	
	/**
	 * Reduce current hp.
	 * @param i the i
	 * @param attacker the attacker
	 */
	public void reduceCurrentHp(double i, Creature attacker)
	{
		reduceCurrentHp(i, attacker, true);
	}
	
	/**
	 * Reduce current hp.
	 * @param i the i
	 * @param attacker the attacker
	 * @param awake the awake
	 */
	public void reduceCurrentHp(double i, Creature attacker, boolean awake)
	{
		if ((this instanceof NpcInstance) && Config.INVUL_NPC_LIST.contains(((NpcInstance) this).getNpcId()))
		{
			return;
		}
		
		if (Config.CHAMPION_ENABLE && _champion && (Config.CHAMPION_HP != 0))
		{
			getStatus().reduceHp(i / Config.CHAMPION_HP, attacker, awake);
		}
		else if (_advanceFlag)
		{
			getStatus().reduceHp(i / _advanceMultiplier, attacker, awake);
		}
		else if (_isUnkillable)
		{
			final double hpToReduce = getStatus().getCurrentHp() - 1;
			if (i > getStatus().getCurrentHp())
			{
				getStatus().reduceHp(hpToReduce, attacker, awake);
			}
			else
			{
				getStatus().reduceHp(i, attacker, awake);
			}
		}
		else
		{
			getStatus().reduceHp(i, attacker, awake);
		}
	}
	
	private long _nextReducingHPByOverTime = -1;
	
	public void reduceCurrentHpByDamOverTime(double i, Creature attacker, boolean awake, int period)
	{
		if (_nextReducingHPByOverTime > System.currentTimeMillis())
		{
			return;
		}
		
		_nextReducingHPByOverTime = System.currentTimeMillis() + (period * 1000);
		reduceCurrentHp(i, attacker, awake);
	}
	
	private long _nextReducingMPByOverTime = -1;
	
	public void reduceCurrentMpByDamOverTime(double i, int period)
	{
		if (_nextReducingMPByOverTime > System.currentTimeMillis())
		{
			return;
		}
		
		_nextReducingMPByOverTime = System.currentTimeMillis() + (period * 1000);
		reduceCurrentMp(i);
	}
	
	/**
	 * Reduce current mp.
	 * @param i the i
	 */
	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}
	
	/**
	 * Removes the status listener.
	 * @param object the object
	 */
	public void removeStatusListener(Creature object)
	{
		getStatus().removeStatusListener(object);
	}
	
	/**
	 * Stop hp mp regeneration.
	 */
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	/**
	 * Gets the current cp.
	 * @return the current cp
	 */
	public double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new current cp
	 */
	public void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new current cp
	 */
	public void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	/**
	 * Gets the current hp.
	 * @return the current hp
	 */
	public double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new current hp
	 */
	public void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	/**
	 * Sets the current hp direct.
	 * @param newHp the new current hp direct
	 */
	public void setCurrentHpDirect(double newHp)
	{
		getStatus().setCurrentHpDirect(newHp);
	}
	
	/**
	 * Sets the current cp direct.
	 * @param newCp the new current cp direct
	 */
	public void setCurrentCpDirect(double newCp)
	{
		getStatus().setCurrentCpDirect(newCp);
	}
	
	/**
	 * Sets the current mp direct.
	 * @param newMp the new current mp direct
	 */
	public void setCurrentMpDirect(double newMp)
	{
		getStatus().setCurrentMpDirect(newMp);
	}
	
	/**
	 * Sets the current hp mp.
	 * @param newHp the new hp
	 * @param newMp the new mp
	 */
	public void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	/**
	 * Gets the current mp.
	 * @return the current mp
	 */
	public double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new current mp
	 */
	public void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new current mp
	 */
	public void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	/**
	 * Sets the ai class.
	 * @param aiClass the new ai class
	 */
	public void setAiClass(String aiClass)
	{
		_aiClass = aiClass;
	}
	
	/**
	 * Gets the ai class.
	 * @return the ai class
	 */
	public String getAiClass()
	{
		return _aiClass;
	}
	
	/**
	 * Sets the champion.
	 * @param champ the new champion
	 */
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	/**
	 * Checks if is champion.
	 * @return true, if is champion
	 */
	public boolean isChampion()
	{
		return _champion;
	}
	
	/**
	 * Gets the last heal amount.
	 * @return the last heal amount
	 */
	public int getLastHealAmount()
	{
		return _lastHealAmount;
	}
	
	/**
	 * Sets the last heal amount.
	 * @param hp the new last heal amount
	 */
	public void setLastHealAmount(int hp)
	{
		_lastHealAmount = hp;
	}
	
	/**
	 * @return the _advanceFlag
	 */
	public boolean isAdvanceFlag()
	{
		return _advanceFlag;
	}
	
	/**
	 * @param advanceFlag
	 */
	public void setAdvanceFlag(boolean advanceFlag)
	{
		_advanceFlag = advanceFlag;
	}
	
	/**
	 * @param advanceMultiplier
	 */
	public void setAdvanceMultiplier(int advanceMultiplier)
	{
		_advanceMultiplier = advanceMultiplier;
	}
	
	/**
	 * Check if character reflected skill.
	 * @param skill the skill
	 * @return true, if successful
	 */
	public boolean reflectSkill(Skill skill)
	{
		return Rnd.get(100) < calcStat(skill.isMagic() ? Stat.REFLECT_SKILL_MAGIC : Stat.REFLECT_SKILL_PHYSIC, 0, null, null);
	}
	
	/**
	 * Vengeance skill.
	 * @param skill the skill
	 * @return true, if successful
	 */
	public boolean vengeanceSkill(Skill skill)
	{
		if (!skill.isMagic() && (skill.getCastRange() <= 40))
		{
			final double venganceChance = calcStat(Stat.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0, null, skill);
			if (venganceChance > Rnd.get(100))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Send system message about damage.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>PlayerInstance
	 * <li>SummonInstance
	 * <li>PetInstance</li><br>
	 * @param target the target
	 * @param damage the damage
	 * @param mcrit the mcrit
	 * @param pcrit the pcrit
	 * @param miss the miss
	 */
	public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}
	
	/**
	 * Gets the force buff.
	 * @return the force buff
	 */
	public ForceBuff getForceBuff()
	{
		return _forceBuff;
	}
	
	/**
	 * Sets the force buff.
	 * @param fb the new force buff
	 */
	public void setForceBuff(ForceBuff fb)
	{
		_forceBuff = fb;
	}
	
	/**
	 * Checks if is fear immune.
	 * @return true, if is fear immune
	 */
	public boolean isFearImmune()
	{
		return false;
	}
	
	/**
	 * Restore hpmp.
	 */
	public void restoreHPMP()
	{
		getStatus().setCurrentHpMp(getStat().getMaxHp(), getStat().getMaxMp());
	}
	
	/**
	 * Restore cp.
	 */
	public void restoreCP()
	{
		getStatus().setCurrentCp(getStat().getMaxCp());
	}
	
	/**
	 * Block.
	 */
	public void block()
	{
		_blocked = true;
	}
	
	/**
	 * Unblock.
	 */
	public void unblock()
	{
		_blocked = false;
	}
	
	/**
	 * Checks if is blocked.
	 * @return true, if is blocked
	 */
	public boolean isBlocked()
	{
		return _blocked;
	}
	
	/**
	 * Checks if is meditated.
	 * @return true, if is meditated
	 */
	public boolean isMeditated()
	{
		return _meditated;
	}
	
	/**
	 * Sets the meditated.
	 * @param meditated the new meditated
	 */
	public void setMeditated(boolean meditated)
	{
		_meditated = meditated;
	}
	
	/**
	 * Update attack stance.
	 */
	public void updateAttackStance()
	{
		attackStance = System.currentTimeMillis();
	}
	
	/**
	 * Gets the attack stance.
	 * @return the attack stance
	 */
	public long getAttackStance()
	{
		return attackStance;
	}
	
	/** The _petrified. */
	private boolean _petrified = false;
	
	/**
	 * Checks if is petrified.
	 * @return the petrified
	 */
	public boolean isPetrified()
	{
		return _petrified;
	}
	
	/**
	 * Sets the petrified.
	 * @param petrified the petrified to set
	 */
	public void setPetrified(boolean petrified)
	{
		if (petrified)
		{
			setParalyzed(petrified);
			setInvul(petrified);
			_petrified = petrified;
		}
		else
		{
			_petrified = petrified;
			setParalyzed(petrified);
			setInvul(petrified);
		}
	}
	
	/**
	 * Check bss.
	 * @return true, if successful
	 */
	public boolean checkBss()
	{
		boolean bss = false;
		final ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof Summon)
		{
			final Summon activeSummon = (Summon) this;
			if (activeSummon.getChargedSpiritShot() == ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
			}
		}
		return bss;
	}
	
	/**
	 * Removes the bss.
	 */
	public synchronized void removeBss()
	{
		final ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				weaponInst.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof Summon)
		{
			final Summon activeSummon = (Summon) this;
			if (activeSummon.getChargedSpiritShot() == ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				activeSummon.setChargedSpiritShot(ItemInstance.CHARGED_NONE);
			}
		}
		reloadShots(true);
	}
	
	/**
	 * Check sps.
	 * @return true, if successful
	 */
	public boolean checkSps()
	{
		boolean ss = false;
		final ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof Summon)
		{
			final Summon activeSummon = (Summon) this;
			if (activeSummon.getChargedSpiritShot() == ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
			}
		}
		return ss;
	}
	
	/**
	 * Removes the sps.
	 */
	public synchronized void removeSps()
	{
		final ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == ItemInstance.CHARGED_SPIRITSHOT)
			{
				weaponInst.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof Summon)
		{
			final Summon activeSummon = (Summon) this;
			if (activeSummon.getChargedSpiritShot() == ItemInstance.CHARGED_SPIRITSHOT)
			{
				activeSummon.setChargedSpiritShot(ItemInstance.CHARGED_NONE);
			}
		}
		reloadShots(true);
	}
	
	/**
	 * Check ss.
	 * @return true, if successful
	 */
	public boolean checkSs()
	{
		boolean ss = false;
		final ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSoulshot() == ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof Summon)
		{
			final Summon activeSummon = (Summon) this;
			if (activeSummon.getChargedSoulShot() == ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
			}
		}
		return ss;
	}
	
	/**
	 * Removes the ss.
	 */
	public void removeSs()
	{
		final ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSoulshot() == ItemInstance.CHARGED_SOULSHOT)
			{
				weaponInst.setChargedSoulshot(ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof Summon)
		{
			final Summon activeSummon = (Summon) this;
			if (activeSummon.getChargedSoulShot() == ItemInstance.CHARGED_SOULSHOT)
			{
				activeSummon.setChargedSoulShot(ItemInstance.CHARGED_NONE);
			}
		}
		reloadShots(false);
	}
	
	/**
	 * Return a multiplier based on weapon random damage<br>
	 * .
	 * @return the random damage multiplier
	 */
	public double getRandomDamageMultiplier()
	{
		final Weapon activeWeapon = getActiveWeaponItem();
		int random;
		if (activeWeapon != null)
		{
			random = activeWeapon.getRandomDamage();
		}
		else
		{
			random = 5 + (int) Math.sqrt(getLevel());
		}
		return (1 + ((double) Rnd.get(0 - random, random) / 100));
	}
	
	/**
	 * Sets the checks if is buff protected.
	 * @param value the new checks if is buff protected
	 */
	public void setBuffProtected(boolean value)
	{
		_isBuffProtected = value;
	}
	
	/**
	 * Checks if is buff protected.
	 * @return true, if is buff protected
	 */
	public boolean isBuffProtected()
	{
		return _isBuffProtected;
	}
	
	/**
	 * Gets the _triggered skills.
	 * @return the _triggeredSkills
	 */
	public Map<Integer, Skill> getTriggeredSkills()
	{
		return _triggeredSkills;
	}
	
	/**
	 * Set target of Attackable and update it.
	 * @author: Nefer
	 * @param trasformedNpc
	 */
	public void setTargetTrasformedNpc(Attackable trasformedNpc)
	{
		if (trasformedNpc == null)
		{
			return;
		}
		
		// Set the target of the PlayerInstance player
		setTarget(trasformedNpc);
		
		// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
		// The player.getLevel() - getLevel() permit to display the correct color in the select window
		sendPacket(new MyTargetSelected(trasformedNpc.getObjectId(), getLevel() - trasformedNpc.getLevel()));
		
		// Send a Server->Client packet StatusUpdate of the NpcInstance to the PlayerInstance to update its HP bar
		final StatusUpdate su = new StatusUpdate(trasformedNpc.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) trasformedNpc.getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_HP, trasformedNpc.getMaxHp());
		sendPacket(su);
	}
	
	/**
	 * @return if the object can be killed
	 */
	public boolean isUnkillable()
	{
		return _isUnkillable;
	}
	
	/**
	 * @param value the _isKillable to set
	 */
	public void setUnkillable(boolean value)
	{
		_isUnkillable = value;
	}
	
	/**
	 * @return the _isAttackDisabled
	 */
	public boolean isAttackDisabled()
	{
		return _isAttackDisabled;
	}
	
	/**
	 * @param value the _isAttackDisabled to set
	 */
	public void setAttackDisabled(boolean value)
	{
		_isAttackDisabled = value;
	}
	
	/**
	 * AI not. Task
	 */
	static class notifyAiTaskDelayed implements Runnable
	{
		CtrlEvent event;
		Object object;
		WorldObject tgt;
		
		notifyAiTaskDelayed(CtrlEvent evt, Object obj, WorldObject target)
		{
			event = evt;
			object = obj;
			tgt = target;
		}
		
		@Override
		public void run()
		{
			((Creature) tgt).getAI().notifyEvent(event, object);
		}
	}
	
	public synchronized void reloadShots(boolean isMagic)
	{
		if (this instanceof PlayerInstance)
		{
			((PlayerInstance) this).rechargeAutoSoulShot(!isMagic, isMagic, false);
		}
		else if (this instanceof Summon)
		{
			((Summon) this).getOwner().rechargeAutoSoulShot(!isMagic, isMagic, true);
		}
	}
	
	public void setCursorKeyMovement(boolean value)
	{
		_cursorKeyMovement = value;
	}
	
	@Override
	public boolean isCreature()
	{
		return true;
	}
}
