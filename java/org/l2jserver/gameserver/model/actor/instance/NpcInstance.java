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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.MobGroupTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.sql.HelperBuffTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.xml.ZoneData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.CustomNpcInstanceManager;
import org.l2jserver.gameserver.instancemanager.DimensionalRiftManager;
import org.l2jserver.gameserver.instancemanager.FortManager;
import org.l2jserver.gameserver.instancemanager.QuestManager;
import org.l2jserver.gameserver.model.DropCategory;
import org.l2jserver.gameserver.model.DropData;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.knownlist.NpcKnownList;
import org.l2jserver.gameserver.model.actor.stat.NpcStat;
import org.l2jserver.gameserver.model.actor.status.NpcStatus;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.GameEvent;
import org.l2jserver.gameserver.model.entity.event.Lottery;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.event.VIP;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSignsFestival;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.model.holders.HelperBuffHolder;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.multisell.Multisell;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.model.zone.type.TownZone;
import org.l2jserver.gameserver.network.GameClient;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import org.l2jserver.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.NpcInfo;
import org.l2jserver.gameserver.network.serverpackets.RadarControl;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.ValidateLocation;
import org.l2jserver.gameserver.taskmanager.DecayTaskManager;

/**
 * This class represents a Non-Player-Creature in the world. It can be a monster or a friendly creature. It also uses a template to fetch some static values. The templates are hardcoded in the client, so we can rely on them.<br>
 * <br>
 * Creature:<br>
 * <li>Attackable</li>
 * <li>FolkInstance</li>
 * @version $Revision: 1.32.2.7.2.24 $ $Date: 2009/04/13 09:17:09 $
 */
public class NpcInstance extends Creature
{
	protected static final Logger LOGGER = Logger.getLogger(NpcInstance.class.getName());
	
	public static final int INTERACTION_DISTANCE = 150;
	private CustomNpcInstance _customNpcInstance;
	private Spawn _spawn;
	private boolean _isBusy = false;
	private String _busyMessage = "";
	volatile boolean _isDecayed = false;
	private boolean _isSpoil = false;
	private int _castleIndex = -2;
	private int _fortIndex = -2;
	public boolean isEventMob = false;
	public boolean _isEventMobTvT = false;
	public boolean _isEventVIPNPC = false;
	public boolean _isEventVIPNPCEnd = false;
	public boolean _isEventMobDM = false;
	public boolean _isEventMobCTF = false;
	public boolean _isCTF_throneSpawn = false;
	public boolean _isCTF_Flag = false;
	private boolean _isInTown = false;
	public String _CTF_FlagTeamName;
	private int _isSpoiledBy = 0;
	private long _lastSocialBroadcast = 0;
	private static final int MINIMUM_SOCIAL_INTERVAL = 6000;
	private int _currentLHandId; // normally this shouldn't change from the template, but there exist exceptions
	private int _currentRHandId; // normally this shouldn't change from the template, but there exist exceptions
	private int _currentCollisionHeight; // used for npc grow effect skills
	private int _currentCollisionRadius; // used for npc grow effect skills
	private volatile int _scriptValue = 0;
	
	/**
	 * Send a packet SocialAction to all PlayerInstance in the _KnownPlayers of the NpcInstance and create a new RandomAnimation Task.
	 * @param animationId
	 */
	public void onRandomAnimation(int animationId)
	{
		// Send a packet SocialAction to all PlayerInstance in the _KnownPlayers of the NpcInstance
		final long now = System.currentTimeMillis();
		if ((now - _lastSocialBroadcast) > MINIMUM_SOCIAL_INTERVAL)
		{
			_lastSocialBroadcast = now;
			broadcastPacket(new SocialAction(getObjectId(), animationId));
		}
	}
	
	/**
	 * Check if the server allows Random Animation.
	 * @return true, if successful
	 */
	public boolean hasRandomAnimation()
	{
		return Config.MAX_NPC_ANIMATION > 0;
	}
	
	public class destroyTemporalNPC implements Runnable
	{
		private final Spawn _oldSpawn;
		
		public destroyTemporalNPC(Spawn spawn)
		{
			_oldSpawn = spawn;
		}
		
		@Override
		public void run()
		{
			try
			{
				_oldSpawn.getLastSpawn().deleteMe();
				_oldSpawn.stopRespawn();
				SpawnTable.getInstance().deleteSpawn(_oldSpawn, false);
			}
			catch (Throwable t)
			{
				LOGGER.warning(t.toString());
			}
		}
	}
	
	public class destroyTemporalSummon implements Runnable
	{
		Summon _summon;
		PlayerInstance _player;
		
		public destroyTemporalSummon(Summon summon, PlayerInstance player)
		{
			_summon = summon;
			_player = player;
		}
		
		@Override
		public void run()
		{
			_summon.unSummon(_player);
		}
	}
	
	/**
	 * Constructor of NpcInstance (use Creature constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to set the _template of the Creature (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the Creature</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template The NpcTemplate to apply to the NPC
	 */
	public NpcInstance(int objectId, NpcTemplate template)
	{
		// Call the Creature constructor to set the _template of the Creature, copy skills from template to object and link _calculators to NPC_STD_CALCULATOR
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		initCharStatusUpdateValues();
		
		// initialize the "current" equipment
		_currentLHandId = getTemplate().getLhand();
		_currentRHandId = getTemplate().getRhand();
		// initialize the "current" collisions
		_currentCollisionHeight = getTemplate().getCollisionHeight();
		_currentCollisionRadius = getTemplate().getCollisionRadius();
		
		// Set the name of the Creature
		setName(template.getName());
	}
	
	/**
	 * Receive the stored int value for this {@link NpcInstance}.
	 * @return stored script value
	 */
	public int getScriptValue()
	{
		return _scriptValue;
	}
	
	/**
	 * Sets the script value related with this {@link NpcInstance}.
	 * @param value value to store
	 */
	public void setScriptValue(int value)
	{
		_scriptValue = value;
	}
	
	/**
	 * @param value value to store
	 * @return {@code true} if stored script value equals given value, {@code false} otherwise
	 */
	public boolean isScriptValue(int value)
	{
		return _scriptValue == value;
	}
	
	@Override
	public NpcKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof NpcKnownList))
		{
			setKnownList(new NpcKnownList(this));
		}
		return (NpcKnownList) super.getKnownList();
	}
	
	@Override
	public NpcStat getStat()
	{
		if (!(super.getStat() instanceof NpcStat))
		{
			setStat(new NpcStat(this));
		}
		return (NpcStat) super.getStat();
	}
	
	@Override
	public NpcStatus getStatus()
	{
		if (!(super.getStatus() instanceof NpcStatus))
		{
			setStatus(new NpcStatus(this));
		}
		return (NpcStatus) super.getStatus();
	}
	
	/**
	 * Return the NpcTemplate of the NpcInstance.
	 * @return the template
	 */
	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}
	
	/**
	 * Return the generic Identifier of this NpcInstance contained in the NpcTemplate.
	 * @return the npc id
	 */
	public int getNpcId()
	{
		return getTemplate().getNpcId();
	}
	
	@Override
	public boolean isAttackable()
	{
		return Config.NPC_ATTACKABLE || (this instanceof Attackable);
	}
	
	/**
	 * Return the faction Identifier of this NpcInstance contained in the NpcTemplate.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * If a NPC belows to a Faction, other NPC of the faction inside the Faction range will help it if it's attacked
	 * @return the faction id
	 */
	public String getFactionId()
	{
		return getTemplate().getFactionId();
	}
	
	/**
	 * Return the Level of this NpcInstance contained in the NpcTemplate.
	 * @return the level
	 */
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}
	
	/**
	 * Return True if the NpcInstance is agressive (ex : MonsterInstance in function of aggroRange).
	 * @return true, if is aggressive
	 */
	public boolean isAggressive()
	{
		return false;
	}
	
	/**
	 * Return the Aggro Range of this NpcInstance contained in the NpcTemplate.
	 * @return the aggro range
	 */
	public int getAggroRange()
	{
		return getTemplate().getAggroRange();
	}
	
	/**
	 * Return the Faction Range of this NpcInstance contained in the NpcTemplate.
	 * @return the faction range
	 */
	public int getFactionRange()
	{
		return getTemplate().getFactionRange();
	}
	
	/**
	 * Return True if this NpcInstance is undead in function of the NpcTemplate.
	 * @return true, if is undead
	 */
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}
	
	/**
	 * Send a packet NpcInfo with state of abnormal effect to all PlayerInstance in the _KnownPlayers of the NpcInstance.
	 */
	@Override
	public void updateAbnormalEffect()
	{
		// Send a Server->Client packet NpcInfo with state of abnormal effect to all PlayerInstance in the _KnownPlayers of the NpcInstance
		for (PlayerInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player != null)
			{
				player.sendPacket(new NpcInfo(this, player));
			}
		}
	}
	
	/**
	 * Return the distance under which the object must be add to _knownObject in function of the object type.<br>
	 * <br>
	 * <b><u>Values</u>:</b><br>
	 * <li>object is a FolkInstance : 0 (don't remember it)</li>
	 * <li>object is a Creature : 0 (don't remember it)</li>
	 * <li>object is a PlayableInstance : 1500</li>
	 * <li>others : 500</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>Attackable</li><br>
	 * @param object The Object to add to _knownObject
	 * @return the distance to watch object
	 */
	public int getDistanceToWatchObject(WorldObject object)
	{
		if (object instanceof FestivalGuideInstance)
		{
			return 10000;
		}
		
		if ((object instanceof FolkInstance) || !(object instanceof Creature))
		{
			return 0;
		}
		
		if (object instanceof Playable)
		{
			return 1500;
		}
		
		return 500;
	}
	
	/**
	 * Return the distance after which the object must be remove from _knownObject in function of the object type.<br>
	 * <br>
	 * <b><u>Values</u>:</b><br>
	 * <li>object is not a Creature : 0 (don't remember it)</li>
	 * <li>object is a FolkInstance : 0 (don't remember it)</li>
	 * <li>object is a PlayableInstance : 3000</li>
	 * <li>others : 1000</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>Attackable</li><br>
	 * @param object The Object to remove from _knownObject
	 * @return the distance to forget object
	 */
	public int getDistanceToForgetObject(WorldObject object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	/**
	 * Return False.<br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>MonsterInstance : Check if the attacker is not another MonsterInstance</li>
	 * <li>PlayerInstance</li><br>
	 * @param attacker the attacker
	 * @return true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	/**
	 * Return the Identifier of the item in the left hand of this NpcInstance contained in the NpcTemplate.
	 * @return the left hand item
	 */
	public int getLeftHandItem()
	{
		return _currentLHandId;
	}
	
	/**
	 * Return the Identifier of the item in the right hand of this NpcInstance contained in the NpcTemplate.
	 * @return the right hand item
	 */
	public int getRightHandItem()
	{
		return _currentRHandId;
	}
	
	/**
	 * Return True if this NpcInstance has drops that can be sweeped.
	 * @return true, if is spoil
	 */
	public boolean isSpoil()
	{
		return _isSpoil;
	}
	
	/**
	 * Set the spoil state of this NpcInstance.
	 * @param isSpoil the new spoil
	 */
	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}
	
	/**
	 * Gets the checks if is spoiled by.
	 * @return the checks if is spoiled by
	 */
	public int getSpoiledBy()
	{
		return _isSpoiledBy;
	}
	
	/**
	 * Sets the checks if is spoiled by.
	 * @param value the new checks if is spoiled by
	 */
	public void setSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}
	
	/**
	 * Return the busy status of this NpcInstance.
	 * @return true, if is busy
	 */
	public boolean isBusy()
	{
		return _isBusy;
	}
	
	/**
	 * Set the busy status of this NpcInstance.
	 * @param isBusy the new busy
	 */
	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}
	
	/**
	 * Return the busy message of this NpcInstance.
	 * @return the busy message
	 */
	public String getBusyMessage()
	{
		return _busyMessage;
	}
	
	/**
	 * Set the busy message of this NpcInstance.
	 * @param message the new busy message
	 */
	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}
	
	/**
	 * Can target.
	 * @param player the player
	 * @return true, if successful
	 */
	protected boolean canTarget(PlayerInstance player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		return true;
	}
	
	/**
	 * Can interact.
	 * @param player the player
	 * @return true, if successful
	 */
	protected boolean canInteract(PlayerInstance player)
	{
		if (player.isCastingNow())
		{
			return false;
		}
		else if (player.isDead() || player.isFakeDeath())
		{
			return false;
		}
		else if (player.isSitting())
		{
			return false;
		}
		else if (player.isInStoreMode())
		{
			return false;
		}
		else if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			return false;
		}
		else if (player.getInstanceId() != getInstanceId())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Manage actions when a player click on the NpcInstance.<br>
	 * <br>
	 * <b><u>Actions on first click on the NpcInstance (Select it)</u>:</b><br>
	 * <li>Set the NpcInstance as target of the PlayerInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the PlayerInstance player (display the select window)</li>
	 * <li>If NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the PlayerInstance in order to update NpcInstance HP bar</li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client</li><br>
	 * <br>
	 * <b><u>Actions on second click on the NpcInstance (Attack it/Intercat with it)</u>:</b><br>
	 * <li>Send a Server->Client packet MyTargetSelected to the PlayerInstance player (display the select window)</li>
	 * <li>If NpcInstance is autoAttackable, notify the PlayerInstance AI with AI_INTENTION_ATTACK (after a height verification)</li>
	 * <li>If NpcInstance is NOT autoAttackable, notify the PlayerInstance AI with AI_INTENTION_INTERACT (after a distance verification) and show message</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid that client wait an other packet</b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Client packet : Action, AttackRequest</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>ArtefactInstance : Manage only fisrt click to select Artefact</li>
	 * <li>GuardInstance :</li><br>
	 * @param player The PlayerInstance that start an action on the NpcInstance
	 */
	@Override
	public void onAction(PlayerInstance player)
	{
		if (!canTarget(player) || ((System.currentTimeMillis() - player.getTimerToAttack()) < Config.CLICK_TASK))
		{
			return;
		}
		
		// Check if the PlayerInstance already target the NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the PlayerInstance player
			player.setTarget(this);
			
			// Remove player spawn protection
			player.onActionRequest();
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
				// The player.getLevel() - getLevel() permit to display the correct color in the select window
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
				
				// Send a Server->Client packet StatusUpdate of the NpcInstance to the PlayerInstance to update its HP bar
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			else
			{
				// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			}
			
			player.setTimerToAttack(System.currentTimeMillis());
			// Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new ValidateLocation(this));
			
			// Check if the player is attackable (without a forced attack) and isn't dead
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				// Check the height difference
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					// Set the PlayerInstance Intention to AI_INTENTION_ATTACK
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					// player.startAttack(this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else if (!isAutoAttackable(player))
			{
				// Calculate the distance between the PlayerInstance and the NpcInstance
				if (!canInteract(player))
				{
					// Notify the PlayerInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Like L2OFF if char is dead, is sitting, is in trade or is in fakedeath can't interact with npc
					if (player.isSitting() || player.isDead() || player.isFakeDeath() || (player.getActiveTradeList() != null))
					{
						return;
					}
					
					// Send a Server->Client packet SocialAction to the all PlayerInstance on the _knownPlayer of the NpcInstance to display a social action of the NpcInstance on their client
					broadcastPacket(new SocialAction(getObjectId(), Rnd.get(8)));
					// Open a chat window on client with the text of the NpcInstance
					if (isEventMob)
					{
						GameEvent.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (_isEventMobTvT)
					{
						TvT.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (_isEventMobDM)
					{
						DM.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (_isEventMobCTF)
					{
						CTF.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (_isCTF_Flag && player._inEventCTF)
					{
						CTF.showFlagHtml(player, String.valueOf(getObjectId()), _CTF_FlagTeamName);
					}
					else if (_isCTF_throneSpawn)
					{
						CTF.checkRestoreFlags();
					}
					else if (_isEventVIPNPC)
					{
						VIP.showJoinHTML(player, String.valueOf(getObjectId()));
					}
					else if (_isEventVIPNPCEnd)
					{
						VIP.showEndHTML(player, String.valueOf(getObjectId()));
					}
					else
					{
						final List<Quest> questList = getTemplate().getEventQuests(EventType.NPC_FIRST_TALK);
						if (questList.size() == 1)
						{
							questList.get(0).notifyFirstTalk(this, player);
						}
						else
						{
							showChatWindow(player, 0);
						}
					}
					// Like L2OFF player must rotate to the Npc
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
				
				// to avoid player stuck
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	/**
	 * Manage and Display the GM console to modify the NpcInstance (GM only).<br>
	 * <br>
	 * <b><u>Actions (If the PlayerInstance is a GM only)</u>:</b><br>
	 * <li>Set the NpcInstance as target of the PlayerInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the PlayerInstance player (display the select window)</li>
	 * <li>If NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the PlayerInstance in order to update NpcInstance HP bar</li>
	 * <li>Send a Server->Client NpcHtmlMessage() containing the GM console about this NpcInstance</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid that client wait an other packet</b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Client packet : Action</li><br>
	 * @param client The thread that manage the player that pessed Shift and click on the NpcInstance
	 */
	@Override
	public void onActionShift(GameClient client)
	{
		// Get the PlayerInstance corresponding to the thread
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Weapon currentWeapon = player.getActiveWeaponItem();
		
		// Check if the PlayerInstance is a GM
		if (player.isGM())
		{
			// Set the target of the PlayerInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
			// The player.getLevel() - getLevel() permit to display the correct color in the select window
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet StatusUpdate of the NpcInstance to the PlayerInstance to update its HP bar
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			
			// Send a Server->Client NpcHtmlMessage() containing the GM console about this NpcInstance
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			final StringBuilder html1 = new StringBuilder("<html><body><center><font color=\"LEVEL\">NPC Information</font></center>");
			final String className = getClass().getSimpleName();
			html1.append("<center><table><tr>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr>");
			html1.append("<td><button value=\"Skills\" action=\"bypass -h admin_show_skilllist_npc " + getTemplate().getNpcId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			html1.append("<td><button value=\"Droplist\" action=\"bypass -h admin_show_droplist " + getTemplate().getNpcId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			html1.append("<tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().getNpcId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"><br1></td></tr>");
			html1.append("</table></center><br>");
			
			html1.append("Instance Type: " + className + "<br1>Faction: " + getTemplate().getFactionId() + "<br1>Location ID: " + (_spawn != null ? _spawn.getLocation() : 0) + "<br1>");
			if (this instanceof ControllableMobInstance)
			{
				html1.append("Mob Group: " + MobGroupTable.getInstance().getGroupForMob((ControllableMobInstance) this).getGroupId() + "<br>");
			}
			else
			{
				html1.append("Respawn Time: " + (_spawn != null ? (_spawn.getRespawnDelay() / 1000) + "  Seconds<br>" : "?  Seconds<br>"));
			}
			
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Object ID</td><td>" + getObjectId() + "</td><td>NPC ID</td><td>" + getTemplate().getNpcId() + "</td></tr>");
			html1.append("<tr><td>Castle</td><td>" + getCastle().getCastleId() + "</td><td>Coords</td><td>" + getX() + "," + getY() + "," + getZ() + "</td></tr>");
			html1.append("<tr><td>Level</td><td>" + getTemplate().getLevel() + "</td><td>Aggro</td><td>" + (this instanceof Attackable ? ((Attackable) this).getAggroRange() : 0) + "</td></tr>");
			html1.append("</table><br>");
			
			html1.append("<font color=\"LEVEL\">Combat</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Current HP</td><td>" + getCurrentHp() + "</td><td>Current MP</td><td>" + getCurrentMp() + "</td></tr>");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stat.MAX_HP, 1, this, null)) + "*" + getStat().calcStat(Stat.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("</table><br>");
			
			html1.append("<font color=\"LEVEL\">Basic Stats</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
			html1.append("</table><br>");
			
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else if (Config.ALT_GAME_VIEWNPC)
		{
			// Set the target of the PlayerInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
			// The player.getLevel() - getLevel() permit to display the correct color in the select window
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet StatusUpdate of the NpcInstance to the PlayerInstance to update its HP bar
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			final StringBuilder html1 = new StringBuilder("<html><body>");
			html1.append("<br><center><font color=\"LEVEL\">[Combat Stats]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stat.MAX_HP, 1, this, null)) + "*" + (int) getStat().calcStat(Stat.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("<tr><td>Race</td><td>" + getTemplate().getRace() + "</td><td></td><td></td></tr>");
			html1.append("</table>");
			
			html1.append("<br><center><font color=\"LEVEL\">[Basic Stats]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
			html1.append("</table>");
			
			html1.append("<br><center><font color=\"LEVEL\">[Drop Info]</font><br>");
			html1.append("Rates legend: <font color=\"ff0000\">50%+</font> <font color=\"00ff00\">30%+</font> <font color=\"0000ff\">less than 30%</font>");
			html1.append("<table border=0 width=\"100%\">");
			for (DropCategory cat : getTemplate().getDropData())
			{
				final List<DropData> drops = cat.getAllDrops();
				if (drops != null)
				{
					for (DropData drop : drops)
					{
						if ((drop == null) || (ItemTable.getInstance().getTemplate(drop.getItemId()) == null))
						{
							continue;
						}
						
						final String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();
						if (drop.getChance() >= 600000)
						{
							html1.append("<tr><td><font color=\"ff0000\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
						}
						else if (drop.getChance() >= 300000)
						{
							html1.append("<tr><td><font color=\"00ff00\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
						}
						else
						{
							html1.append("<tr><td><font color=\"0000ff\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
						}
					}
				}
			}
			
			html1.append("</table>");
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else // Like L2OFF set the target of the PlayerInstance player
		{
			// Check if the PlayerInstance already target the NpcInstance
			if (this != player.getTarget())
			{
				// Set the target of the PlayerInstance player
				player.setTarget(this);
				
				// Check if the player is attackable (without a forced attack)
				if (isAutoAttackable(player))
				{
					// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
					// The player.getLevel() - getLevel() permit to display the correct color in the select window
					player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
					
					// Send a Server->Client packet StatusUpdate of the NpcInstance to the PlayerInstance to update its HP bar
					final StatusUpdate su = new StatusUpdate(getObjectId());
					su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
					su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
					player.sendPacket(su);
				}
				else
				{
					// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
					player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				}
				
				player.setTimerToAttack(System.currentTimeMillis());
				player.sendPacket(new ValidateLocation(this));
			}
			else
			{
				player.sendPacket(new ValidateLocation(this));
				// Check if the player is attackable (without a forced attack) and isn't dead
				if (isAutoAttackable(player) && !isAlikeDead())
				{
					// Check the height difference
					if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
					{
						// Like L2OFF player must not move with shift pressed
						// Only archer can hit from long
						if (!canInteract(player) && ((currentWeapon != null) && (currentWeapon.getItemType() != WeaponType.BOW)))
						{
							// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else if (!canInteract(player) && (currentWeapon == null))
						{
							// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							// Set the PlayerInstance Intention to AI_INTENTION_ATTACK
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						}
					}
					else
					{
						// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
				else if (!isAutoAttackable(player))
				{
					// Like L2OFF player must not move with shift pressed
					// Only archer can hit from long
					if (!canInteract(player) && ((currentWeapon != null) && (currentWeapon.getItemType() != WeaponType.BOW)))
					{
						// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (!canInteract(player) && (currentWeapon == null))
					{
						// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						// Like L2OFF if char is dead, is sitting, is in trade or is in fakedeath can't interact with npc
						if (player.isSitting() || player.isDead() || player.isFakeDeath() || (player.getActiveTradeList() != null))
						{
							return;
						}
						
						// Send a Server->Client packet SocialAction to the all PlayerInstance on the _knownPlayer of the NpcInstance to display a social action of the NpcInstance on their client
						broadcastPacket(new SocialAction(getObjectId(), Rnd.get(8)));
						// Open a chat window on client with the text of the NpcInstance
						if (isEventMob)
						{
							GameEvent.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (_isEventMobTvT)
						{
							TvT.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (_isEventMobDM)
						{
							DM.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (_isEventMobCTF)
						{
							CTF.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (_isCTF_Flag && player._inEventCTF)
						{
							CTF.showFlagHtml(player, String.valueOf(getObjectId()), _CTF_FlagTeamName);
						}
						else if (_isCTF_throneSpawn)
						{
							CTF.checkRestoreFlags();
						}
						else if (_isEventVIPNPC)
						{
							VIP.showJoinHTML(player, String.valueOf(getObjectId()));
						}
						else if (_isEventVIPNPCEnd)
						{
							VIP.showEndHTML(player, String.valueOf(getObjectId()));
						}
						else
						{
							final List<Quest> questList = getTemplate().getEventQuests(EventType.NPC_FIRST_TALK);
							if (questList.size() == 1)
							{
								questList.get(0).notifyFirstTalk(this, player);
							}
							else
							{
								showChatWindow(player, 0);
							}
						}
					}
					
					// to avoid player stuck
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			
			// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Return the Castle this NpcInstance belongs to.
	 * @return the castle
	 */
	public Castle getCastle()
	{
		// Get castle this NPC belongs to (excluding Attackable)
		if (_castleIndex < 0)
		{
			final TownZone town = ZoneData.getInstance().getZone(getX(), getY(), getZ(), TownZone.class);
			if (town != null)
			{
				_castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
			}
			
			if (_castleIndex < 0)
			{
				_castleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
			}
			else
			{
				_isInTown = true; // Npc was spawned in town
			}
		}
		
		if (_castleIndex < 0)
		{
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}
	
	/**
	 * Return the Fort this NpcInstance belongs to.
	 * @return the fort
	 */
	public Fort getFort()
	{
		// Get Fort this NPC belongs to (excluding Attackable)
		if (_fortIndex < 0)
		{
			final Fort fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
			if (fort != null)
			{
				_fortIndex = FortManager.getInstance().getFortIndex(fort.getFortId());
			}
			if (_fortIndex < 0)
			{
				_fortIndex = FortManager.getInstance().findNearestFortIndex(this);
			}
		}
		if (_fortIndex < 0)
		{
			return null;
		}
		return FortManager.getInstance().getForts().get(_fortIndex);
	}
	
	/**
	 * Gets the checks if is in town.
	 * @return the checks if is in town
	 */
	public boolean isInTown()
	{
		if (_castleIndex < 0)
		{
			getCastle();
		}
		return _isInTown;
	}
	
	/**
	 * Open a quest or chat window on client with the text of the NpcInstance in function of the command.<br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Client packet : RequestBypassToServer</li><br>
	 * @param player the player
	 * @param command The command string received from client
	 */
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (_isBusy && (_busyMessage.length() > 0))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("/data/html/npcbusy.htm");
			html.replace("%busymessage%", _busyMessage);
			html.replace("%npcname%", getName());
			html.replace("%playername%", player.getName());
			player.sendPacket(html);
		}
		else if (command.equalsIgnoreCase("TerritoryStatus"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			{
				if (getCastle().getOwnerId() > 0)
				{
					html.setFile("/data/html/territorystatus.htm");
					final Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
					html.replace("%clanname%", clan.getName());
					html.replace("%clanleadername%", clan.getLeaderName());
				}
				else
				{
					html.setFile("/data/html/territorynoclan.htm");
				}
			}
			html.replace("%castlename%", getCastle().getName());
			html.replace("%taxpercent%", "" + getCastle().getTaxPercent());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			{
				if (getCastle().getCastleId() > 6)
				{
					html.replace("%territory%", "The Kingdom of Elmore");
				}
				else
				{
					html.replace("%territory%", "The Kingdom of Aden");
				}
			}
			player.sendPacket(html);
		}
		else if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			if (quest.length() == 0)
			{
				showQuestWindow(player);
			}
			else
			{
				showQuestWindow(player, quest);
			}
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("Link"))
		{
			final String path = command.substring(5).trim();
			if (path.indexOf("..") != -1)
			{
				return;
			}
			final String filename = "/data/html/" + path;
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (command.startsWith("NobleTeleport"))
		{
			if (!player.isNoble())
			{
				final String filename = "/data/html/teleporter/nobleteleporter-no.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("Loto"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
			}
			if (val == 0)
			{
				// new loto ticket
				for (int i = 0; i < 5; i++)
				{
					player.setLoto(i, 0);
				}
			}
			showLotoWindow(player, val);
		}
		else if (command.startsWith("CPRecovery"))
		{
			makeCPRecovery(player);
		}
		else if (command.startsWith("SupportMagic"))
		{
			makeSupportMagic(player);
		}
		else if (command.startsWith("GiveBlessing"))
		{
			giveBlessingSupport(player);
		}
		else if (command.startsWith("multisell"))
		{
			Multisell.getInstance().SeparateAndSend(Integer.parseInt(command.substring(9).trim()), player, false, getCastle().getTaxRate());
		}
		else if (command.startsWith("exc_multisell"))
		{
			Multisell.getInstance().SeparateAndSend(Integer.parseInt(command.substring(13).trim()), player, true, getCastle().getTaxRate());
		}
		else if (command.startsWith("Augment"))
		{
			final int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
			switch (cmdChoice)
			{
				case 1:
				{
					player.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
					player.sendPacket(new ExShowVariationMakeWindow());
					break;
				}
				case 2:
				{
					player.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
					player.sendPacket(new ExShowVariationCancelWindow());
					break;
				}
			}
		}
		else if (command.startsWith("npcfind_byid"))
		{
			try
			{
				final Spawn spawn = SpawnTable.getInstance().getTemplate(Integer.parseInt(command.substring(12).trim()));
				if (spawn != null)
				{
					player.sendPacket(new RadarControl(0, 1, spawn.getX(), spawn.getY(), spawn.getZ()));
				}
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("newbie_give_coupon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if ((player.getLevel() > 25) || (player.getLevel() < 6) || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-3.htm");
					player.sendPacket(html);
				}
				else if (player.getCoupon(0))
				{
					html.setFile("data/html/adventurers_guide/31760-1.htm");
					player.sendPacket(html);
				}
				else
				{
					player.getInventory().addItem("Weapon Coupon", 7832, 1, player, this);
					player.addCoupon(1);
					html.setFile("data/html/adventurers_guide/31760-2.htm");
					player.sendPacket(html);
				}
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("newbie_give_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if ((player.getLevel() > 25) || (player.getLevel() < 6) || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-3.htm");
					player.sendPacket(html);
				}
				else
				{
					Multisell.getInstance().SeparateAndSend(10010, player, false, getCastle().getTaxRate());
				}
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("newbie_return_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if ((player.getLevel() > 25) || (player.getLevel() < 6) || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-3.htm");
					player.sendPacket(html);
				}
				else
				{
					Multisell.getInstance().SeparateAndSend(10011, player, false, getCastle().getTaxRate());
				}
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("traveller_give_coupon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if ((player.getLevel() > 25) || (player.getClassId().level() != 1) || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-6.htm");
					player.sendPacket(html);
				}
				else if (player.getCoupon(1))
				{
					html.setFile("data/html/adventurers_guide/31760-4.htm");
					player.sendPacket(html);
				}
				else
				{
					player.getInventory().addItem("Weapon Coupon", 7833, 1, player, this);
					player.addCoupon(2);
					html.setFile("data/html/adventurers_guide/31760-5.htm");
					player.sendPacket(html);
				}
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("traveller_give_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if ((player.getLevel() > 25) || (player.getClassId().level() != 1) || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-6.htm");
					player.sendPacket(html);
				}
				else
				{
					Multisell.getInstance().SeparateAndSend(10012, player, false, getCastle().getTaxRate());
				}
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("traveller_return_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if ((player.getLevel() > 25) || (player.getClassId().level() != 1) || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-6.htm");
					player.sendPacket(html);
				}
				else
				{
					Multisell.getInstance().SeparateAndSend(10013, player, false, getCastle().getTaxRate());
				}
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("EnterRift"))
		{
			try
			{
				final Byte b1 = Byte.parseByte(command.substring(10)); // Selected Area: Recruit, Soldier etc
				DimensionalRiftManager.getInstance().start(player, b1, this);
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("ChangeRiftRoom"))
		{
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().manualTeleport(player, this);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(player, this);
			}
		}
		else if (command.startsWith("ExitRift"))
		{
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().manualExitRift(player, this);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(player, this);
			}
		}
	}
	
	/**
	 * Return null (regular NPCs don't have weapons instancies).
	 * @return the active weapon instance
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons
		return null;
	}
	
	/**
	 * Return the weapon item equipped in the right hand of the NpcInstance or null.
	 * @return the active weapon item
	 */
	@Override
	public Weapon getActiveWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the NpcInstance
		final int weaponId = getTemplate().getRhand();
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equipped in the right hand of the NpcInstance
		final Item item = ItemTable.getInstance().getTemplate(getTemplate().getRhand());
		if (!(item instanceof Weapon))
		{
			return null;
		}
		
		return (Weapon) item;
	}
	
	/**
	 * Give blessing support.
	 * @param player the player
	 */
	public void giveBlessingSupport(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		// Blessing of protection - author eX1steam.
		// Prevent a cursed weapon weilder of being buffed - I think no need of that becouse karma check > 0
		if (player.isCursedWeaponEquiped())
		{
			return;
		}
		
		final int playerLevel = player.getLevel();
		// If the player is too high level, display a message and return
		if ((playerLevel > 39) || (player.getClassId().level() >= 2))
		{
			final String content = "<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br1>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer.</font></body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		final Skill skill = SkillTable.getInstance().getInfo(5182, 1);
		broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skill.getLevel(), 0, 0));
		skill.getEffects(this, player);
	}
	
	/**
	 * Return null (regular NPCs don't have weapons instancies).
	 * @return the secondary weapon instance
	 */
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons
		return null;
	}
	
	/**
	 * Return the weapon item equipped in the left hand of the NpcInstance or null.
	 * @return the secondary weapon item
	 */
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the NpcInstance
		final int weaponId = getTemplate().getLhand();
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equipped in the right hand of the NpcInstance
		final Item item = ItemTable.getInstance().getTemplate(getTemplate().getLhand());
		if (!(item instanceof Weapon))
		{
			return null;
		}
		
		return (Weapon) item;
	}
	
	/**
	 * Send a Server->Client packet NpcHtmlMessage to the PlayerInstance in order to display the message of the NpcInstance.
	 * @param player The PlayerInstance who talks with the NpcInstance
	 * @param content The text of the NpcMessage
	 */
	public void insertObjectIdAndShowChatWindow(PlayerInstance player, String content)
	{
		// Send a Server->Client packet NpcHtmlMessage to the PlayerInstance in order to display the message of the NpcInstance
		content = content.replace("%objectId%", String.valueOf(getObjectId()));
		final NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}
	
	/**
	 * Return the pathfile of the selected HTML file in function of the npcId and of the page number.<br>
	 * <br>
	 * <b><u>Format of the pathfile</u>:</b><br>
	 * <li>if the file exists on the server (page number = 0) : <b>data/html/default/12006.htm</b> (npcId-page number)</li>
	 * <li>if the file exists on the server (page number > 0) : <b>data/html/default/12006-1.htm</b> (npcId-page number)</li>
	 * <li>if the file doesn't exist on the server : <b>data/html/npcdefault.htm</b> (message : "I have nothing to say to you")</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>GuardInstance : Set the pathfile to data/html/guard/12006-1.htm (npcId-page number)</li><br>
	 * @param npcId The Identifier of the NpcInstance whose text must be display
	 * @param value The number of the page to display
	 * @return the html path
	 */
	public String getHtmlPath(int npcId, int value)
	{
		String pom = "";
		if (value == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + value;
		}
		
		final String temp = "data/html/default/" + pom + ".htm";
		if (!Config.LAZY_CACHE)
		{
			// If not running lazy cache the file must be in the cache or it doesnt exist
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else if (HtmCache.getInstance().isLoadable(temp))
		{
			return temp;
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "data/html/npcdefault.htm";
	}
	
	/**
	 * Open a choose quest window on client with all quests available of the NpcInstance.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the PlayerInstance</li><br>
	 * @param player The PlayerInstance that talk with the NpcInstance
	 * @param quests The table containing quests of the NpcInstance
	 */
	private void showQuestChooseWindow(PlayerInstance player, Quest[] quests)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Talk about:</title><br>");
		String state;
		for (Quest q : quests)
		{
			if (q == null)
			{
				continue;
			}
			
			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[");
			state = "";
			final QuestState qs = player.getQuestState(q.getName());
			if (qs != null)
			{
				if (qs.isStarted() && (qs.getInt("cond") > 0))
				{
					state = " (In Progress)";
				}
				else if (qs.isCompleted())
				{
					state = " (Done)";
				}
			}
			sb.append(q.getDescr()).append(state).append("]</a><br>");
		}
		
		sb.append("</body></html>");
		
		// Send a Server->Client packet NpcHtmlMessage to the PlayerInstance in order to display the message of the NpcInstance
		insertObjectIdAndShowChatWindow(player, sb.toString());
	}
	
	/**
	 * Open a quest window on client with the text of the NpcInstance.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the text of the quest state in the folder data/scripts/quests/questId/stateId.htm</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the PlayerInstance</li>
	 * <li>Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet</li><br>
	 * @param player The PlayerInstance that talk with the NpcInstance
	 * @param questId The Identifier of the quest to display the message
	 */
	public void showQuestWindow(PlayerInstance player, String questId)
	{
		String content = null;
		Quest q = null;
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			q = QuestManager.getInstance().getQuest(questId);
		}
		
		// Get the state of the selected quest
		QuestState qs = player.getQuestState(questId);
		if (q == null)
		{
			// No quests found
			content = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		}
		else
		{
			if ((player.getWeightPenalty() >= 3) && (q.getQuestId() >= 1) && (q.getQuestId() < 1000))
			{
				player.sendPacket(SystemMessageId.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORY_S_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				return;
			}
			
			if (qs == null)
			{
				if ((q.getQuestId() >= 1) && (q.getQuestId() < 20000))
				{
					final List<Quest> questList = player.getAllActiveQuests();
					if (questList.size() >= 25) // if too many ongoing quests, don't show window and send message
					{
						player.sendMessage("You have too many quests, cannot register");
						return;
					}
				}
				// Check for start point
				for (Quest temp : getTemplate().getEventQuests(EventType.QUEST_START))
				{
					if (temp == q)
					{
						qs = q.newQuestState(player);
						break;
					}
				}
			}
		}
		
		if (qs != null)
		{
			// If the quest is already started, no need to show a window
			if (!qs.getQuest().notifyTalk(this, qs))
			{
				return;
			}
			
			questId = qs.getQuest().getName();
			final String stateId = State.getStateName(qs.getState());
			final String path = Config.DATAPACK_ROOT + "/data/scripts/quests/" + questId + "/" + stateId + ".htm";
			content = HtmCache.getInstance().getHtm(path);
		}
		
		// Send a Server->Client packet NpcHtmlMessage to the PlayerInstance in order to display the message of the Npc
		if (content != null)
		{
			insertObjectIdAndShowChatWindow(player, content);
		}
		
		// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.
	 * @param player The PlayerInstance that talk with the NpcInstance
	 */
	public void showQuestWindow(PlayerInstance player)
	{
		// collect awaiting quests and start points
		final List<Quest> options = new ArrayList<>();
		final List<QuestState> awaits = player.getQuestsForTalk(getTemplate().getNpcId());
		final List<Quest> starts = getTemplate().getEventQuests(EventType.QUEST_START);
		
		// Quests are limited between 1 and 999 because those are the quests that are supported by the client.
		// By limiting them there, we are allowed to create custom quests at higher IDs without interfering
		if (awaits != null)
		{
			for (QuestState qs : awaits)
			{
				if (!options.contains(qs.getQuest()) && (qs.getQuest().getQuestId() > 0) && (qs.getQuest().getQuestId() < 1000))
				{
					options.add(qs.getQuest());
				}
			}
		}
		
		if (starts != null)
		{
			for (Quest quest : starts)
			{
				if (!options.contains(quest) && (quest.getQuestId() > 0) && (quest.getQuestId() < 1000))
				{
					options.add(quest);
				}
			}
		}
		
		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if (options.size() > 1)
		{
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, "");
		}
	}
	
	/**
	 * Make the NPC speaks to his current knownlist.
	 * @param message The String message to send.
	 */
	public void broadcastNpcSay(String message)
	{
		broadcastPacket(new CreatureSay(getObjectId(), ChatType.GENERAL, getName(), message));
	}
	
	/**
	 * Open a Loto window on client with the text of the NpcInstance.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the PlayerInstance</li>
	 * <li>Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet</li><br>
	 * @param player The PlayerInstance that talk with the NpcInstance
	 * @param value The number of the page of the NpcInstance to display
	 */
	// 0 - first buy lottery ticket window
	// 1-20 - buttons
	// 21 - second buy lottery ticket window
	// 22 - selected ticket with 5 numbers
	// 23 - current lottery jackpot
	// 24 - Previous winning numbers/Prize claim
	// >24 - check lottery ticket by item object id
	public void showLotoWindow(PlayerInstance player, int value)
	{
		final int npcId = getTemplate().getNpcId();
		String filename;
		SystemMessage sm;
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		if (value == 0) // 0 - first buy lottery ticket window
		{
			filename = getHtmlPath(npcId, 1);
			html.setFile(filename);
		}
		else if ((value >= 1) && (value <= 21)) // 1-20 - buttons, 21 - second buy lottery ticket window
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
				return;
			}
			
			filename = getHtmlPath(npcId, 5);
			html.setFile(filename);
			
			int count = 0;
			int found = 0;
			// counting buttons and unsetting button if found
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == value)
				{
					// unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if (player.getLoto(i) > 0)
				{
					count++;
				}
			}
			
			// if not rearched limit 5 and not unseted value
			if ((count < 5) && (found == 0) && (value <= 20))
			{
				for (int i = 0; i < 5; i++)
				{
					if (player.getLoto(i) == 0)
					{
						player.setLoto(i, value);
						break;
					}
				}
			}
			
			// setting pusshed buttons
			count = 0;
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if (player.getLoto(i) < 10)
					{
						button = "0" + button;
					}
					final String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					final String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}
			}
			
			if (count == 5)
			{
				final String search = "0\">Return";
				final String replace = "22\">The winner selected the numbers above.";
				html.replace(search, replace);
			}
		}
		else if (value == 22) // 22 - selected ticket with 5 numbers
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
				return;
			}
			
			final int price = Config.ALT_LOTTERY_TICKET_PRICE;
			final int lotonumber = Lottery.getInstance().getId();
			int enchant = 0;
			int type2 = 0;
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == 0)
				{
					return;
				}
				
				if (player.getLoto(i) < 17)
				{
					enchant += Math.pow(2, player.getLoto(i) - 1);
				}
				else
				{
					type2 += Math.pow(2, player.getLoto(i) - 17);
				}
			}
			if (player.getAdena() < price)
			{
				sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				player.sendPacket(sm);
				return;
			}
			if (!player.reduceAdena("Loto", price, this, true))
			{
				return;
			}
			Lottery.getInstance().increasePrize(price);
			
			sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_S2);
			sm.addNumber(lotonumber);
			sm.addItemName(4442);
			player.sendPacket(sm);
			
			final ItemInstance item = new ItemInstance(IdFactory.getNextId(), 4442);
			item.setCount(1);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem("Loto", item, player, this);
			
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			final ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
			iu.addModifiedItem(adenaupdate);
			player.sendPacket(iu);
			
			filename = getHtmlPath(npcId, 3);
			html.setFile(filename);
		}
		else if (value == 23) // 23 - current lottery jackpot
		{
			filename = getHtmlPath(npcId, 3);
			html.setFile(filename);
		}
		else if (value == 24) // 24 - Previous winning numbers/Prize claim
		{
			filename = getHtmlPath(npcId, 4);
			html.setFile(filename);
			
			final int lotonumber = Lottery.getInstance().getId();
			String message = "";
			for (ItemInstance item : player.getInventory().getItems())
			{
				if (item == null)
				{
					continue;
				}
				if ((item.getItemId() == 4442) && (item.getCustomType1() < lotonumber))
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					final int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for (int i = 0; i < 5; i++)
					{
						message += numbers[i] + " ";
					}
					final int[] check = Lottery.getInstance().checkTicket(item);
					if (check[0] > 0)
					{
						switch (check[0])
						{
							case 1:
							{
								message += "- 1st Prize";
								break;
							}
							case 2:
							{
								message += "- 2nd Prize";
								break;
							}
							case 3:
							{
								message += "- 3th Prize";
								break;
							}
							case 4:
							{
								message += "- 4th Prize";
								break;
							}
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}
			if (message == "")
			{
				message += "There is no winning lottery ticket...<br>";
			}
			html.replace("%result%", message);
		}
		else if (value > 24) // >24 - check lottery ticket by item object id
		{
			final int lotonumber = Lottery.getInstance().getId();
			final ItemInstance item = player.getInventory().getItemByObjectId(value);
			if ((item == null) || (item.getItemId() != 4442) || (item.getCustomType1() >= lotonumber))
			{
				return;
			}
			final int[] check = Lottery.getInstance().checkTicket(item);
			sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
			sm.addItemName(4442);
			player.sendPacket(sm);
			
			final int adena = check[1];
			if (adena > 0)
			{
				player.addAdena("Loto", adena, this, true);
			}
			player.destroyItem("Loto", item, this, false);
			return;
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%race%", "" + Lottery.getInstance().getId());
		html.replace("%adena%", "" + Lottery.getInstance().getPrize());
		html.replace("%ticket_price%", "" + Config.ALT_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", "" + (Config.ALT_LOTTERY_5_NUMBER_RATE * 100));
		html.replace("%prize4%", "" + (Config.ALT_LOTTERY_4_NUMBER_RATE * 100));
		html.replace("%prize3%", "" + (Config.ALT_LOTTERY_3_NUMBER_RATE * 100));
		html.replace("%prize2%", "" + Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", "" + DateFormat.getDateInstance().format(Lottery.getInstance().getEndDate()));
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Make cp recovery.
	 * @param player the player
	 */
	public void makeCPRecovery(PlayerInstance player)
	{
		if ((getTemplate().getNpcId() != 31225) && (getTemplate().getNpcId() != 31226))
		{
			return;
		}
		if (player.isCursedWeaponEquiped())
		{
			player.sendMessage("Go away, you're not welcome here.");
			return;
		}
		
		final int neededmoney = 100;
		SystemMessage sm;
		if (!player.reduceAdena("RestoreCP", neededmoney, player.getLastFolkNPC(), true))
		{
			return;
		}
		
		// Skill's animation
		final Skill skill = SkillTable.getInstance().getInfo(4380, 1);
		if (skill != null)
		{
			broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skill.getLevel(), 0, 0));
			skill.getEffects(this, player);
		}
		
		player.setCurrentCp(player.getMaxCp());
		// cp restored
		sm = new SystemMessage(SystemMessageId.S1_CPS_HAVE_BEEN_RESTORED);
		sm.addString(player.getName());
		player.sendPacket(sm);
	}
	
	/**
	 * Add Newbie helper buffs to Player according to its level.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the range level in wich player must be to obtain buff</li>
	 * <li>If player level is out of range, display a message and return</li>
	 * <li>According to player level cast buff</li><br>
	 * <font color=#FF0000><b> Newbie Helper Buff list is define in sql table helper_buff_list</b></font>
	 * @param player The PlayerInstance that talk with the NpcInstance if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_USEITEM)) return;
	 */
	public void makeSupportMagic(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		// Prevent a cursed weapon weilder of being buffed
		if (player.isCursedWeaponEquiped())
		{
			return;
		}
		
		final int playerLevel = player.getLevel();
		int lowestLevel = 0;
		int higestLevel = 0;
		
		// Calculate the min and max level between wich the player must be to obtain buff
		if (player.isMageClass())
		{
			lowestLevel = HelperBuffTable.getInstance().getMagicClassLowestLevel();
			higestLevel = HelperBuffTable.getInstance().getMagicClassHighestLevel();
		}
		else
		{
			lowestLevel = HelperBuffTable.getInstance().getPhysicClassLowestLevel();
			higestLevel = HelperBuffTable.getInstance().getPhysicClassHighestLevel();
		}
		
		// If the player is too high level, display a message and return
		if ((playerLevel > higestLevel) || !player.isNewbie())
		{
			final String content = "<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level " + higestLevel + " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		// If the player is too low level, display a message and return
		if (playerLevel < lowestLevel)
		{
			final String content = "<html><body>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		Skill skill = null;
		// Go through the Helper Buff list define in sql table helper_buff_list and cast skill
		for (HelperBuffHolder helperBuffItem : HelperBuffTable.getInstance().getHelperBuffTable())
		{
			if ((helperBuffItem.isMagicClassBuff() == player.isMageClass()) && (playerLevel >= helperBuffItem.getLowerLevel()) && (playerLevel <= helperBuffItem.getUpperLevel()))
			{
				skill = SkillTable.getInstance().getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
				if (skill.getSkillType() == SkillType.SUMMON)
				{
					player.doCast(skill);
				}
				else
				{
					broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skill.getLevel(), 0, 0));
					skill.getEffects(this, player);
				}
			}
		}
	}
	
	/**
	 * Show chat window.
	 * @param player the player
	 */
	public void showChatWindow(PlayerInstance player)
	{
		showChatWindow(player, 0);
	}
	
	/**
	 * Returns true if html exists.
	 * @param player the player
	 * @param type the type
	 * @return boolean
	 */
	private boolean showPkDenyChatWindow(PlayerInstance player, String type)
	{
		final String html = HtmCache.getInstance().getHtm("data/html/" + type + "/" + getNpcId() + "-pk.htm");
		if (html != null)
		{
			final NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		return false;
	}
	
	/**
	 * Open a chat window on client with the text of the NpcInstance.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the PlayerInstance</li>
	 * <li>Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet</li><br>
	 * @param player The PlayerInstance that talk with the NpcInstance
	 * @param value The number of the page of the NpcInstance to display
	 */
	public void showChatWindow(PlayerInstance player, int value)
	{
		// Like L2OFF if char is dead, is sitting, is in trade or is in fakedeath can't speak with npcs
		if (player.isSitting() || player.isDead() || player.isFakeDeath() || (player.getActiveTradeList() != null))
		{
			return;
		}
		
		if (player.getKarma() > 0)
		{
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (this instanceof MerchantInstance))
			{
				if (showPkDenyChatWindow(player, "merchant"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (this instanceof TeleporterInstance))
			{
				if (showPkDenyChatWindow(player, "teleporter"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (this instanceof WarehouseInstance))
			{
				if (showPkDenyChatWindow(player, "warehouse"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (this instanceof FishermanInstance))
			{
				if (showPkDenyChatWindow(player, "fisherman"))
				{
					return;
				}
			}
		}
		
		if ((getTemplate().getType() == "Auctioneer") && (value == 0))
		{
			return;
		}
		
		final int npcId = getTemplate().getNpcId();
		
		/* For use with Seven Signs implementation */
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		final int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		final boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		switch (npcId)
		{
			case 31078:
			case 31079:
			case 31080:
			case 31081:
			case 31082: // Dawn Priests
			case 31083:
			case 31084:
			case 31168:
			case 31692:
			case 31694:
			case 31997:
			{
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DAWN:
					{
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dawn_priest_2c.htm";
								}
								else
								{
									filename += "dawn_priest_2a.htm";
								}
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1b.htm";
						}
						break;
					}
					case SevenSigns.CABAL_DUSK:
					{
						if (isSealValidationPeriod)
						{
							filename += "dawn_priest_3b.htm";
						}
						else
						{
							filename += "dawn_priest_3a.htm";
						}
						break;
					}
					default:
					{
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								filename += "dawn_priest_4.htm";
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1a.htm";
						}
						break;
					}
				}
				break;
			}
			case 31085:
			case 31086:
			case 31087:
			case 31088: // Dusk Priest
			case 31089:
			case 31090:
			case 31091:
			case 31169:
			case 31693:
			case 31695:
			case 31998:
			{
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DUSK:
					{
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dusk_priest_2c.htm";
								}
								else
								{
									filename += "dusk_priest_2a.htm";
								}
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1b.htm";
						}
						break;
					}
					case SevenSigns.CABAL_DAWN:
					{
						if (isSealValidationPeriod)
						{
							filename += "dusk_priest_3b.htm";
						}
						else
						{
							filename += "dusk_priest_3a.htm";
						}
						break;
					}
					default:
					{
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								filename += "dusk_priest_4.htm";
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1a.htm";
						}
						break;
					}
				}
				break;
			}
			case 31095: //
			case 31096: //
			case 31097: //
			case 31098: // Enter Necropolises
			case 31099: //
			case 31100: //
			case 31101: //
			case 31102: //
			{
				if (isSealValidationPeriod)
				{
					if (Config.ALT_REQUIRE_WIN_7S)
					{
						if ((playerCabal != compWinner) || (sealAvariceOwner != compWinner))
						{
							switch (compWinner)
							{
								case SevenSigns.CABAL_DAWN:
								{
									player.sendPacket(SystemMessageId.ONLY_A_LORD_OF_DAWN_MAY_USE_THIS);
									filename += "necro_no.htm";
									break;
								}
								case SevenSigns.CABAL_DUSK:
								{
									player.sendPacket(SystemMessageId.ONLY_A_REVOLUTIONARY_OF_DUSK_MAY_USE_THIS);
									filename += "necro_no.htm";
									break;
								}
								case SevenSigns.CABAL_NULL:
								{
									filename = getHtmlPath(npcId, value); // do the default!
									break;
								}
							}
						}
						else
						{
							filename = getHtmlPath(npcId, value); // do the default!
						}
					}
					else
					{
						filename = getHtmlPath(npcId, value); // do the default!
					}
				}
				else if (playerCabal == SevenSigns.CABAL_NULL)
				{
					filename += "necro_no.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, value); // do the default!
				}
				break;
			}
			case 31114: //
			case 31115: //
			case 31116: // Enter Catacombs
			case 31117: //
			case 31118: //
			case 31119: //
			{
				if (isSealValidationPeriod)
				{
					if (Config.ALT_REQUIRE_WIN_7S)
					{
						if ((playerCabal != compWinner) || (sealGnosisOwner != compWinner))
						{
							switch (compWinner)
							{
								case SevenSigns.CABAL_DAWN:
								{
									player.sendPacket(SystemMessageId.ONLY_A_LORD_OF_DAWN_MAY_USE_THIS);
									filename += "cata_no.htm";
									break;
								}
								case SevenSigns.CABAL_DUSK:
								{
									player.sendPacket(SystemMessageId.ONLY_A_REVOLUTIONARY_OF_DUSK_MAY_USE_THIS);
									filename += "cata_no.htm";
									break;
								}
								case SevenSigns.CABAL_NULL:
								{
									filename = getHtmlPath(npcId, value); // do the default!
									break;
								}
							}
						}
						else
						{
							filename = getHtmlPath(npcId, value); // do the default!
						}
					}
					else
					{
						filename = getHtmlPath(npcId, value); // do the default!
					}
				}
				else if (playerCabal == SevenSigns.CABAL_NULL)
				{
					filename += "cata_no.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, value); // do the default!
				}
				break;
			}
			case 31111: // Gatekeeper Spirit (Disciples)
			{
				if ((playerCabal == sealAvariceOwner) && (playerCabal == compWinner))
				{
					switch (sealAvariceOwner)
					{
						case SevenSigns.CABAL_DAWN:
						{
							filename += "spirit_dawn.htm";
							break;
						}
						case SevenSigns.CABAL_DUSK:
						{
							filename += "spirit_dusk.htm";
							break;
						}
						case SevenSigns.CABAL_NULL:
						{
							filename += "spirit_null.htm";
							break;
						}
					}
				}
				else
				{
					filename += "spirit_null.htm";
				}
				break;
			}
			case 31112: // Gatekeeper Spirit (Disciples)
			{
				filename += "spirit_exit.htm";
				break;
			}
			case 31127: //
			case 31128: //
			case 31129: // Dawn Festival Guides
			case 31130: //
			case 31131: //
			{
				filename += "festival/dawn_guide.htm";
				break;
			}
			case 31137: //
			case 31138: //
			case 31139: // Dusk Festival Guides
			case 31140: //
			case 31141: //
			{
				filename += "festival/dusk_guide.htm";
				break;
			}
			case 31092: // Black Marketeer of Mammon
			{
				filename += "blkmrkt_1.htm";
				break;
			}
			case 31113: // Merchant of Mammon
			{
				if (Config.ALT_REQUIRE_WIN_7S)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
						{
							if ((playerCabal != compWinner) || (playerCabal != sealAvariceOwner))
							{
								player.sendPacket(SystemMessageId.ONLY_A_LORD_OF_DAWN_MAY_USE_THIS);
								return;
							}
							break;
						}
						case SevenSigns.CABAL_DUSK:
						{
							if ((playerCabal != compWinner) || (playerCabal != sealAvariceOwner))
							{
								player.sendPacket(SystemMessageId.ONLY_A_REVOLUTIONARY_OF_DUSK_MAY_USE_THIS);
								return;
							}
							break;
						}
					}
				}
				filename += "mammmerch_1.htm";
				break;
			}
			case 31126: // Blacksmith of Mammon
			{
				if (Config.ALT_REQUIRE_WIN_7S)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
						{
							if ((playerCabal != compWinner) || (playerCabal != sealGnosisOwner))
							{
								player.sendPacket(SystemMessageId.ONLY_A_LORD_OF_DAWN_MAY_USE_THIS);
								return;
							}
							break;
						}
						case SevenSigns.CABAL_DUSK:
						{
							if ((playerCabal != compWinner) || (playerCabal != sealGnosisOwner))
							{
								player.sendPacket(SystemMessageId.ONLY_A_REVOLUTIONARY_OF_DUSK_MAY_USE_THIS);
								return;
							}
							break;
						}
					}
				}
				filename += "mammblack_1.htm";
				break;
			}
			case 31132:
			case 31133:
			case 31134:
			case 31135:
			case 31136: // Festival Witches
			case 31142:
			case 31143:
			case 31144:
			case 31145:
			case 31146:
			{
				filename += "festival/festival_witch.htm";
				break;
			}
			case 31688:
			{
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, value);
				}
				break;
			}
			// fixed Monument of Heroes HTML for noble characters like L2OFF
			case 31690:
			{
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, value);
				}
				break;
			}
			case 31769:
			case 31770:
			case 31771:
			case 31772:
			{
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, value);
				}
				break;
			}
			default:
			{
				if ((npcId >= 31865) && (npcId <= 31918))
				{
					filename += "rift/GuardianOfBorder.htm";
					break;
				}
				if (((npcId >= 31093) && (npcId <= 31094)) || ((npcId >= 31172) && (npcId <= 31201)) || ((npcId >= 31239) && (npcId <= 31254)))
				{
					return;
				}
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = getHtmlPath(npcId, value);
				break;
			}
		}
		
		if (this instanceof CastleTeleporterInstance)
		{
			((CastleTeleporterInstance) this).showChatWindow(player);
			return;
		}
		
		// Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the PlayerInstance
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		
		if ((this instanceof MerchantInstance) && Config.LIST_PET_RENT_NPC.contains(npcId))
		{
			html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
		}
		html.replace("%npcname%", getName());
		html.replace("%playername%", player.getName());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStart());
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Open a chat window on client with the text specified by the given file name and path,<br>
	 * relative to the datapack root.<br>
	 * Added by Tempy
	 * @param player The PlayerInstance that talk with the NpcInstance
	 * @param filename The filename that contains the text to send
	 */
	public void showChatWindow(PlayerInstance player, String filename)
	{
		// Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the PlayerInstance
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Return the Exp Reward of this NpcInstance contained in the NpcTemplate (modified by RATE_XP).
	 * @return the exp reward
	 */
	public int getExpReward()
	{
		final double rateXp = getStat().calcStat(Stat.MAX_HP, 1, this, null);
		return (int) (getTemplate().getRewardExp() * rateXp * Config.RATE_XP);
	}
	
	/**
	 * Return the SP Reward of this NpcInstance contained in the NpcTemplate (modified by RATE_SP).
	 * @return the sp reward
	 */
	public int getSpReward()
	{
		final double rateSp = getStat().calcStat(Stat.MAX_HP, 1, this, null);
		return (int) (getTemplate().getRewardSp() * rateSp * Config.RATE_SP);
	}
	
	/**
	 * Kill the NpcInstance (the corpse disappeared after 7 seconds).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Create a DecayTask to remove the corpse of the NpcInstance after 7 seconds</li>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the Creature</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other PlayerInstance to inform</li>
	 * <li>Notify Creature AI</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>Attackable</li><br>
	 * @param killer The Creature who killed it
	 * @return true, if successful
	 */
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		// Normally this wouldn't really be needed, but for those few exceptions, we do need to reset the weapons back to the initial templated weapon.
		_currentLHandId = getTemplate().getLhand();
		_currentRHandId = getTemplate().getRhand();
		_currentCollisionHeight = getTemplate().getCollisionHeight();
		_currentCollisionRadius = getTemplate().getCollisionRadius();
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	/**
	 * Set the spawn of the NpcInstance.
	 * @param spawn The Spawn that manage the NpcInstance
	 */
	public void setSpawn(Spawn spawn)
	{
		_spawn = spawn;
		// Does this Npc morph into a PlayerInstance?
		if ((_spawn != null) && CustomNpcInstanceManager.getInstance().isCustomNpcInstance(_spawn.getId(), getNpcId()))
		{
			new CustomNpcInstance(this);
		}
	}
	
	/**
	 * Remove the NpcInstance from the world and update its spawn object (for a complete removal use the deleteMe method).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove the NpcInstance from the world when the decay task is launched</li>
	 * <li>Decrease its spawn counter</li>
	 * <li>Manage Siege task (killFlag, killCT)</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T REMOVE the object from _allObjects of World </b></font><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packets to players</b></font>
	 */
	@Override
	public void onDecay()
	{
		if (_isDecayed)
		{
			return;
		}
		setDecayed(true);
		
		// Manage Life Control Tower
		if (this instanceof ControlTowerInstance)
		{
			((ControlTowerInstance) this).onDeath();
		}
		
		// Remove the NpcInstance from the world when the decay task is launched
		super.onDecay();
		
		// Decrease its spawn counter
		if (_spawn != null)
		{
			_spawn.decreaseCount(this);
		}
		
		// Clear script value
		_scriptValue = 0;
	}
	
	/**
	 * Remove PROPERLY the NpcInstance from the world.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove the NpcInstance from the world and update its spawn object</li>
	 * <li>Remove all WorldObject from _knownObjects and _knownPlayer of the NpcInstance then cancel Attak or Cast and notify AI</li>
	 * <li>Remove WorldObject object from _allObjects of World</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packets to players</b></font>
	 */
	public void deleteMe()
	{
		if (getWorldRegion() != null)
		{
			getWorldRegion().removeFromZones(this);
			// FIXME this is just a temp hack, we should find a better solution | for what ?
		}
		
		try
		{
			decayMe();
		}
		catch (Throwable t)
		{
			LOGGER.warning(t.getMessage());
		}
		
		// Remove all WorldObject from _knownObjects and _knownPlayer of the Creature then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Throwable t)
		{
			LOGGER.warning(t.getMessage());
		}
		
		// Remove WorldObject object from _allObjects of World
		World.getInstance().removeObject(this);
	}
	
	/**
	 * Return the Spawn object that manage this NpcInstance.
	 * @return the spawn
	 */
	public Spawn getSpawn()
	{
		return _spawn;
	}
	
	@Override
	public String toString()
	{
		return getTemplate().getName();
	}
	
	/**
	 * Checks if is decayed.
	 * @return true, if is decayed
	 */
	public boolean isDecayed()
	{
		return _isDecayed;
	}
	
	/**
	 * Sets the decayed.
	 * @param decayed the new decayed
	 */
	public void setDecayed(boolean decayed)
	{
		_isDecayed = decayed;
	}
	
	/**
	 * End decay task.
	 */
	public void endDecayTask()
	{
		if (!_isDecayed)
		{
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}
	
	/**
	 * Sets the l hand id.
	 * @param newWeaponId the new l hand id
	 */
	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}
	
	/**
	 * Sets the r hand id.
	 * @param newWeaponId the new r hand id
	 */
	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}
	
	/**
	 * Sets the collision height.
	 * @param height the new collision height
	 */
	public void setCollisionHeight(int height)
	{
		_currentCollisionHeight = height;
	}
	
	/**
	 * Sets the collision radius.
	 * @param radius the new collision radius
	 */
	public void setCollisionRadius(int radius)
	{
		_currentCollisionRadius = radius;
	}
	
	/**
	 * Gets the collision height.
	 * @return the collision height
	 */
	public int getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	/**
	 * Gets the collision radius.
	 * @return the collision radius
	 */
	public int getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	/**
	 * Gets the custom npc instance.
	 * @return the custom npc instance
	 */
	public CustomNpcInstance getCustomNpcInstance()
	{
		return _customNpcInstance;
	}
	
	/**
	 * Sets the custom npc instance.
	 * @param arg the new custom npc instance
	 */
	public void setCustomNpcInstance(CustomNpcInstance arg)
	{
		_customNpcInstance = arg;
	}
	
	@Override
	public boolean isNpc()
	{
		return true;
	}
}
