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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.ai.CreatureAI;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.ai.DoorAI;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.FortManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Territory;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.knownlist.DoorKnownList;
import org.l2jserver.gameserver.model.actor.stat.DoorStat;
import org.l2jserver.gameserver.model.actor.status.DoorStatus;
import org.l2jserver.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.GameClient;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jserver.gameserver.network.serverpackets.DoorStatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.ValidateLocation;

public class DoorInstance extends Creature
{
	protected static final Logger LOGGER = Logger.getLogger(DoorInstance.class.getName());
	
	private int _castleIndex = -2;
	private int _mapRegion = -1;
	private int _fortIndex = -2;
	private int _rangeXMin = 0;
	private int _rangeYMin = 0;
	private int _rangeZMin = 0;
	private int _rangeXMax = 0;
	private int _rangeYMax = 0;
	private int _rangeZMax = 0;
	private int _A = 0;
	private int _B = 0;
	private int _C = 0;
	private int _D = 0;
	protected final int _doorId;
	protected final String _name;
	boolean _open;
	private final boolean _unlockable;
	private ClanHall _clanHall;
	protected int _autoActionDelay = -1;
	private ScheduledFuture<?> _autoActionTask;
	public Territory pos;
	
	/**
	 * This class may be created only by Creature and only for AI.
	 */
	public class AIAccessor extends Creature.AIAccessor
	{
		@Override
		public DoorInstance getActor()
		{
			return DoorInstance.this;
		}
		
		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}
		
		@Override
		public void moveTo(int x, int y, int z)
		{
		}
		
		@Override
		public void stopMove(Location pos)
		{
		}
		
		@Override
		public void doAttack(Creature target)
		{
		}
		
		@Override
		public void doCast(Skill skill)
		{
		}
	}
	
	@Override
	public CreatureAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new DoorAI(new AIAccessor());
				}
			}
		}
		return _ai;
	}
	
	@Override
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	class CloseTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				onClose();
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	/**
	 * Manages the auto open and closing of a door.
	 */
	class AutoOpenClose implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!_open)
				{
					openMe();
				}
				else
				{
					closeMe();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
			}
		}
	}
	
	/**
	 * Instantiates a new door instance.
	 * @param objectId the object id
	 * @param template the template
	 * @param doorId the door id
	 * @param name the name
	 * @param unlockable the unlockable
	 */
	public DoorInstance(int objectId, CreatureTemplate template, int doorId, String name, boolean unlockable)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
		pos = new Territory();
	}
	
	@Override
	public DoorKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof DoorKnownList))
		{
			setKnownList(new DoorKnownList(this));
		}
		return (DoorKnownList) super.getKnownList();
	}
	
	@Override
	public DoorStat getStat()
	{
		if (!(super.getStat() instanceof DoorStat))
		{
			setStat(new DoorStat(this));
		}
		return (DoorStat) super.getStat();
	}
	
	@Override
	public DoorStatus getStatus()
	{
		if (!(super.getStatus() instanceof DoorStatus))
		{
			setStatus(new DoorStatus(this));
		}
		return (DoorStatus) super.getStatus();
	}
	
	/**
	 * Checks if is unlockable.
	 * @return true, if is unlockable
	 */
	public boolean isUnlockable()
	{
		return _unlockable;
	}
	
	@Override
	public int getLevel()
	{
		return 1;
	}
	
	/**
	 * Gets the door id.
	 * @return Returns the doorId.
	 */
	public int getDoorId()
	{
		return _doorId;
	}
	
	/**
	 * @return Returns if the door is open.
	 */
	public boolean isOpen()
	{
		return _open;
	}
	
	/**
	 * @param open The door open status.
	 */
	public void setOpen(boolean open)
	{
		_open = open;
	}
	
	/**
	 * Sets the delay in milliseconds for automatic opening/closing of this door instance.<br>
	 * <b>Note:</b> A value of -1 cancels the auto open/close task.
	 * @param actionDelay the new auto action delay
	 */
	public void setAutoActionDelay(int actionDelay)
	{
		if (_autoActionDelay == actionDelay)
		{
			return;
		}
		
		if (actionDelay > -1)
		{
			final AutoOpenClose ao = new AutoOpenClose();
			ThreadPool.scheduleAtFixedRate(ao, actionDelay, actionDelay);
		}
		else if (_autoActionTask != null)
		{
			_autoActionTask.cancel(false);
		}
		
		_autoActionDelay = actionDelay;
	}
	
	/**
	 * Gets the damage.
	 * @return the damage
	 */
	public int getDamage()
	{
		final int dmg = 6 - (int) Math.ceil((getCurrentHp() / getMaxHp()) * 6);
		if (dmg > 6)
		{
			return 6;
		}
		if (dmg < 0)
		{
			return 0;
		}
		return dmg;
	}
	
	/**
	 * Gets the castle.
	 * @return the castle
	 */
	public Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			_castleIndex = CastleManager.getInstance().getCastleIndex(this);
		}
		
		if (_castleIndex < 0)
		{
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}
	
	/**
	 * Gets the fort.
	 * @return the fort
	 */
	public Fort getFort()
	{
		if (_fortIndex < 0)
		{
			_fortIndex = FortManager.getInstance().getFortIndex(this);
		}
		
		if (_fortIndex < 0)
		{
			return null;
		}
		
		return FortManager.getInstance().getForts().get(_fortIndex);
	}
	
	/**
	 * Sets the clan hall.
	 * @param clanhall the new clan hall
	 */
	public void setClanHall(ClanHall clanhall)
	{
		_clanHall = clanhall;
	}
	
	/**
	 * Gets the clan hall.
	 * @return the clan hall
	 */
	public ClanHall getClanHall()
	{
		return _clanHall;
	}
	
	/**
	 * Checks if is enemy of.
	 * @param creature the cha
	 * @return true, if is enemy of
	 */
	public boolean isEnemyOf(Creature creature)
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (_unlockable)
		{
			return true;
		}
		
		// Doors can't be attacked by NPCs
		if (!(attacker instanceof Playable))
		{
			return false;
		}
		
		// Attackable during siege by attacker only
		PlayerInstance player = null;
		if (attacker instanceof PlayerInstance)
		{
			player = (PlayerInstance) attacker;
		}
		else if (attacker instanceof SummonInstance)
		{
			player = ((SummonInstance) attacker).getOwner();
		}
		else if (attacker instanceof PetInstance)
		{
			player = ((PetInstance) attacker).getOwner();
		}
		
		if (player == null)
		{
			return false;
		}
		
		final Clan clan = player.getClan();
		final boolean isCastle = (getCastle() != null) && (getCastle().getCastleId() > 0) && getCastle().getSiege().isInProgress() && getCastle().getSiege().checkIsAttacker(clan);
		final boolean isFort = (getFort() != null) && (getFort().getFortId() > 0) && getFort().getSiege().isInProgress() && getFort().getSiege().checkIsAttacker(clan);
		if (isFort)
		{
			if ((clan != null) && (clan == getFort().getOwnerClan()))
			{
				return false;
			}
		}
		else if (isCastle)
		{
			if ((clan != null) && (clan.getClanId() == getCastle().getOwnerId()))
			{
				return false;
			}
		}
		return isCastle || isFort || DevastatedCastle.getInstance().isInProgress();
	}
	
	/**
	 * Checks if is attackable.
	 * @param attacker the attacker
	 * @return true, if is attackable
	 */
	public boolean isAttackable(Creature attacker)
	{
		return isAutoAttackable(attacker);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		// No effects.
	}
	
	/**
	 * Gets the distance to watch object.
	 * @param object the object
	 * @return the distance to watch object
	 */
	public int getDistanceToWatchObject(WorldObject object)
	{
		if (!(object instanceof PlayerInstance))
		{
			return 0;
		}
		return 2000;
	}
	
	/**
	 * Return the distance after which the object must be remove from _knownObject according to the type of the object.<br>
	 * <br>
	 * <b><u>Values</u>:</b><br>
	 * <li>object is a PlayerInstance : 4000</li>
	 * <li>object is not a PlayerInstance : 0</li><br>
	 * @param object the object
	 * @return the distance to forget object
	 */
	public int getDistanceToForgetObject(WorldObject object)
	{
		if (!(object instanceof PlayerInstance))
		{
			return 0;
		}
		return 4000;
	}
	
	/**
	 * Return null.
	 * @return the active weapon instance
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		// Check if the PlayerInstance already target the NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the PlayerInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			player.sendPacket(new DoorStatusUpdate(this));
			
			// Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isAutoAttackable(player))
		{
			if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
		}
		else if ((player.getClan() != null) && (_clanHall != null) && (player.getClanId() == _clanHall.getOwnerId()))
		{
			if (!isInsideRadius(player, NpcInstance.INTERACTION_DISTANCE, false, false))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Like L2OFF Clanhall's doors get request to be closed/opened
				player.gatesRequest(this);
				if (!_open)
				{
					player.sendPacket(new ConfirmDlg(1140));
				}
				else
				{
					player.sendPacket(new ConfirmDlg(1141));
				}
			}
		}
		// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onActionShift(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isGM())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
			if (isAutoAttackable(player))
			{
				player.sendPacket(new DoorStatusUpdate(this));
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final StringBuilder html1 = new StringBuilder("<html><body><center><font color=\"LEVEL\">Door Information</font></center>");
			html1.append("<table border=0><tr>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + _doorId + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + _doorId + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			html1.append("</tr></table><br><table border=0 width=270>");
			html1.append("<tr><td width=90>Instance Type:</td><td width=180>" + getClass().getSimpleName() + "</td></tr>");
			html1.append("<tr><td>Current HP:</td><td>" + getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>Max HP:</td><td>" + getMaxHp() + "</td></tr>");
			html1.append("<tr><td>Object ID:</td><td>" + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Door ID:</td><td>" + _doorId + "</td></tr>");
			html1.append("</table></body></html>");
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else
		{
			// ATTACK the mob without moving?
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
			if (isAutoAttackable(player))
			{
				player.sendPacket(new DoorStatusUpdate(this));
			}
			
			final NpcHtmlMessage reply = new NpcHtmlMessage(5);
			final StringBuilder replyMsg = new StringBuilder("<html><body>You cannot use this action.");
			replyMsg.append("</body></html>");
			reply.setHtml(replyMsg.toString());
			player.sendPacket(reply);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if ((knownPlayers == null) || knownPlayers.isEmpty())
		{
			return;
		}
		
		final DoorStatusUpdate su = new DoorStatusUpdate(this);
		for (PlayerInstance player : knownPlayers)
		{
			player.sendPacket(su);
		}
	}
	
	public void onOpen()
	{
		ThreadPool.schedule(new CloseTask(), 60000);
	}
	
	public void onClose()
	{
		closeMe();
	}
	
	public void closeMe()
	{
		synchronized (this)
		{
			if (!_open)
			{
				return;
			}
			
			setOpen(false);
		}
		
		broadcastStatusUpdate();
	}
	
	public void openMe()
	{
		synchronized (this)
		{
			if (_open)
			{
				return;
			}
			setOpen(true);
		}
		
		broadcastStatusUpdate();
	}
	
	@Override
	public String toString()
	{
		return "door " + _doorId;
	}
	
	/**
	 * Gets the door name.
	 * @return the door name
	 */
	public String getDoorName()
	{
		return _name;
	}
	
	/**
	 * Gets the x min.
	 * @return the x min
	 */
	public int getXMin()
	{
		return _rangeXMin;
	}
	
	/**
	 * Gets the y min.
	 * @return the y min
	 */
	public int getYMin()
	{
		return _rangeYMin;
	}
	
	/**
	 * Gets the z min.
	 * @return the z min
	 */
	public int getZMin()
	{
		return _rangeZMin;
	}
	
	/**
	 * Gets the x max.
	 * @return the x max
	 */
	public int getXMax()
	{
		return _rangeXMax;
	}
	
	/**
	 * Gets the y max.
	 * @return the y max
	 */
	public int getYMax()
	{
		return _rangeYMax;
	}
	
	/**
	 * Gets the z max.
	 * @return the z max
	 */
	public int getZMax()
	{
		return _rangeZMax;
	}
	
	/**
	 * Sets the range.
	 * @param xMin the x min
	 * @param yMin the y min
	 * @param zMin the z min
	 * @param xMax the x max
	 * @param yMax the y max
	 * @param zMax the z max
	 */
	public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
	{
		_rangeXMin = xMin;
		_rangeYMin = yMin;
		_rangeZMin = zMin;
		_rangeXMax = xMax;
		_rangeYMax = yMax;
		_rangeZMax = zMax;
		_A = (_rangeYMax * (_rangeZMax - _rangeZMin)) + (_rangeYMin * (_rangeZMin - _rangeZMax));
		_B = (_rangeZMin * (_rangeXMax - _rangeXMin)) + (_rangeZMax * (_rangeXMin - _rangeXMax));
		_C = (_rangeXMin * (_rangeYMax - _rangeYMin)) + (_rangeXMin * (_rangeYMin - _rangeYMax));
		_D = -1 * ((_rangeXMin * ((_rangeYMax * _rangeZMax) - (_rangeYMin * _rangeZMax))) + (_rangeXMax * ((_rangeYMin * _rangeZMin) - (_rangeYMin * _rangeZMax))) + (_rangeXMin * ((_rangeYMin * _rangeZMax) - (_rangeYMax * _rangeZMin))));
	}
	
	/**
	 * Gets the a.
	 * @return the a
	 */
	public int getA()
	{
		return _A;
	}
	
	/**
	 * Gets the b.
	 * @return the b
	 */
	public int getB()
	{
		return _B;
	}
	
	/**
	 * Gets the c.
	 * @return the c
	 */
	public int getC()
	{
		return _C;
	}
	
	/**
	 * Gets the d.
	 * @return the d
	 */
	public int getD()
	{
		return _D;
	}
	
	/**
	 * Gets the map region.
	 * @return the map region
	 */
	public int getMapRegion()
	{
		return _mapRegion;
	}
	
	/**
	 * Sets the map region.
	 * @param region the new map region
	 */
	public void setMapRegion(int region)
	{
		_mapRegion = region;
	}
	
	/**
	 * Gets the known siege guards.
	 * @return the known siege guards
	 */
	public Collection<SiegeGuardInstance> getKnownSiegeGuards()
	{
		final List<SiegeGuardInstance> result = new ArrayList<>();
		for (WorldObject obj : getKnownList().getKnownObjects().values())
		{
			if (obj instanceof SiegeGuardInstance)
			{
				result.add((SiegeGuardInstance) obj);
			}
		}
		return result;
	}
	
	/**
	 * Gets the known fort siege guards.
	 * @return the known fort siege guards
	 */
	public Collection<FortSiegeGuardInstance> getKnownFortSiegeGuards()
	{
		final List<FortSiegeGuardInstance> result = new ArrayList<>();
		final Collection<WorldObject> objs = getKnownList().getKnownObjects().values();
		{
			for (WorldObject obj : objs)
			{
				if (obj instanceof FortSiegeGuardInstance)
				{
					result.add((FortSiegeGuardInstance) obj);
				}
			}
		}
		return result;
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake)
	{
		if (isAutoAttackable(attacker) || ((attacker instanceof PlayerInstance) && ((PlayerInstance) attacker).isGM()))
		{
			super.reduceCurrentHp(damage, attacker, awake);
		}
		else
		{
			super.reduceCurrentHp(0, attacker, awake);
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		final boolean isFort = ((getFort() != null) && (getFort().getFortId() > 0) && getFort().getSiege().isInProgress());
		final boolean isCastle = ((getCastle() != null) && (getCastle().getCastleId() > 0) && getCastle().getSiege().isInProgress());
		if (isFort || isCastle)
		{
			broadcastPacket(SystemMessage.sendString("The castle gate has been broken down."));
		}
		return true;
	}
	
	@Override
	public boolean isDoor()
	{
		return true;
	}
}
