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
package org.l2jserver.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.instancemanager.FortSiegeGuardManager;
import org.l2jserver.gameserver.instancemanager.FortSiegeManager;
import org.l2jserver.gameserver.instancemanager.FortSiegeManager.SiegeSpawn;
import org.l2jserver.gameserver.instancemanager.MercTicketManager;
import org.l2jserver.gameserver.model.SiegeClan;
import org.l2jserver.gameserver.model.SiegeClan.SiegeClanType;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.ArtefactInstance;
import org.l2jserver.gameserver.model.actor.instance.CommanderInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.FortressSiegeInfo;
import org.l2jserver.gameserver.network.serverpackets.RelationChanged;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * The Class FortSiege.
 * @author programmos
 */
public class FortSiege
{
	protected static final Logger LOGGER = Logger.getLogger(FortSiege.class.getName());
	
	public enum TeleportWhoType
	{
		All,
		Attacker,
		DefenderNotOwner,
		Owner,
		Spectator
	}
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		/** The _fort inst. */
		private final Fort _fortInst;
		
		/**
		 * Instantiates a new schedule end siege task.
		 * @param pFort the fort
		 */
		public ScheduleEndSiegeTask(Fort pFort)
		{
			_fortInst = pFort;
		}
		
		@Override
		public void run()
		{
			if (!_isInProgress)
			{
				return;
			}
			
			try
			{
				final long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 3600000)
				{
					ThreadPool.schedule(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 3600000); // Prepare task for 1 hr left.
				}
				else if ((timeRemaining <= 3600000) && (timeRemaining > 600000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					ThreadPool.schedule(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					
					// Prepare task for 5 minute left.
					ThreadPool.schedule(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 300000);
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege conclusion.", true);
					
					// Prepare task for 10 seconds count down
					ThreadPool.schedule(new ScheduleEndSiegeTask(_fortInst), timeRemaining - 10000);
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					announceToPlayer(getFort().getName() + " siege " + (timeRemaining / 1000) + " second(s) left!", true);
					
					// Prepare task for second count down
					ThreadPool.schedule(new ScheduleEndSiegeTask(_fortInst), timeRemaining);
				}
				else
				{
					_fortInst.getSiege().endSiege();
				}
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Fort _fortInst;
		
		/**
		 * Instantiates a new schedule start siege task.
		 * @param pFort the fort
		 */
		public ScheduleStartSiegeTask(Fort pFort)
		{
			_fortInst = pFort;
		}
		
		@Override
		public void run()
		{
			if (_isInProgress)
			{
				return;
			}
			
			try
			{
				final long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 86400000)
				{
					// Prepare task for 24 before siege start to end registration
					ThreadPool.schedule(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 86400000);
				}
				else if ((timeRemaining <= 86400000) && (timeRemaining > 13600000))
				{
					// Prepare task for 1 hr left before siege start.
					ThreadPool.schedule(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 13600000);
				}
				else if ((timeRemaining <= 13600000) && (timeRemaining > 600000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege begin.", false);
					
					// Prepare task for 10 minute left.
					ThreadPool.schedule(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 600000);
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					announceToPlayer("The registration term for " + getFort().getName() + " has ended.", false);
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					
					// Prepare task for 5 minute left.
					ThreadPool.schedule(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 300000);
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getFort().getName() + " siege begin.", false);
					
					// Prepare task for 10 seconds count down
					ThreadPool.schedule(new ScheduleStartSiegeTask(_fortInst), timeRemaining - 10000);
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					announceToPlayer(getFort().getName() + " siege " + (timeRemaining / 1000) + " second(s) to start!", false);
					
					// Prepare task for second count down
					ThreadPool.schedule(new ScheduleStartSiegeTask(_fortInst), timeRemaining);
				}
				else
				{
					_fortInst.getSiege().startSiege();
				}
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	private final List<SiegeClan> _attackerClans = new ArrayList<>(); // SiegeClan
	private final List<SiegeClan> _defenderClans = new ArrayList<>(); // SiegeClan
	private final List<SiegeClan> _defenderWaitingClans = new ArrayList<>(); // SiegeClan
	private int _defenderRespawnDelayPenalty;
	private List<CommanderInstance> _commanders = new ArrayList<>();
	private List<ArtefactInstance> _combatflag = new ArrayList<>();
	private final Fort[] _fort;
	boolean _isInProgress = false;
	private boolean _isScheduled = false;
	private boolean _isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	protected boolean _isRegistrationOver = false;
	protected Calendar _siegeEndDate;
	private FortSiegeGuardManager _siegeGuardManager;
	protected Calendar _siegeRegistrationEndDate;
	
	/**
	 * Instantiates a new fort siege.
	 * @param fort the fort
	 */
	public FortSiege(Fort[] fort)
	{
		_fort = fort;
		checkAutoTask();
	}
	
	/**
	 * When siege ends
	 */
	public void endSiege()
	{
		if (_isInProgress)
		{
			announceToPlayer("The siege of " + getFort().getName() + " has finished!", false);
			if (getFort().getOwnerId() <= 0)
			{
				announceToPlayer("The siege of " + getFort().getName() + " has ended in a draw.", false);
			}
			
			// Removes all flags. Note: Remove flag before teleporting players
			removeFlags();
			unSpawnFlags();
			
			// Teleport to the second closest town
			teleportPlayer(TeleportWhoType.Attacker, TeleportWhereType.TOWN);
			
			// Teleport to the second closest town
			teleportPlayer(TeleportWhoType.DefenderNotOwner, TeleportWhereType.TOWN);
			
			// Teleport to the second closest town
			teleportPlayer(TeleportWhoType.Spectator, TeleportWhereType.TOWN);
			
			// Flag so that siege instance can be started
			_isInProgress = false;
			updatePlayerSiegeStateFlags(true);
			
			// Save fort specific data
			saveFortSiege();
			
			// Clear siege clan from db
			clearSiegeClan();
			
			// Remove commander from this fort
			removeCommander();
			
			_siegeGuardManager.unspawnSiegeGuard(); // Remove all spawned siege guard from this fort
			if (getFort().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();
			}
			
			// Respawn door to fort
			getFort().spawnDoor();
			getFort().getZone().updateZoneStatusForCharactersInside();
		}
	}
	
	/**
	 * Removes the defender.
	 * @param sc the sc
	 */
	private void removeDefender(SiegeClan sc)
	{
		if (sc != null)
		{
			getDefenderClans().remove(sc);
		}
	}
	
	/**
	 * Removes the attacker.
	 * @param sc the sc
	 */
	private void removeAttacker(SiegeClan sc)
	{
		if (sc != null)
		{
			getAttackerClans().remove(sc);
		}
	}
	
	/**
	 * Adds the defender.
	 * @param sc the sc
	 * @param type the type
	 */
	private void addDefender(SiegeClan sc, SiegeClanType type)
	{
		if (sc == null)
		{
			return;
		}
		
		sc.setType(type);
		getDefenderClans().add(sc);
	}
	
	/**
	 * Adds the attacker.
	 * @param sc the sc
	 */
	private void addAttacker(SiegeClan sc)
	{
		if (sc == null)
		{
			return;
		}
		
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}
	
	/**
	 * When control of fort changed during siege
	 */
	public void midVictory()
	{
		if (_isInProgress) // Siege still in progress
		{
			// defenders to attacker
			for (SiegeClan sc : getDefenderClans())
			{
				if (sc != null)
				{
					removeDefender(sc);
					addAttacker(sc);
				}
			}
			
			// owner as defender
			final SiegeClan scNewOwner = getAttackerClan(getFort().getOwnerId());
			removeAttacker(scNewOwner);
			addDefender(scNewOwner, SiegeClanType.OWNER);
			endSiege();
		}
	}
	
	/**
	 * When siege starts
	 */
	public void startSiege()
	{
		if (!_isInProgress)
		{
			if (getAttackerClans().isEmpty())
			{
				SystemMessage sm;
				if (getFort().getOwnerId() <= 0)
				{
					sm = new SystemMessage(SystemMessageId.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED);
				}
				
				sm.addString(getFort().getName());
				Announcements.getInstance().announceToAll(sm);
				return;
			}
			
			_isNormalSide = true; // Atk is now atk
			_isInProgress = true; // Flag so that same siege instance cannot be started again
			_isScheduled = false;
			
			// Load siege clan from db
			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			
			// Teleport to the closest town
			teleportPlayer(TeleportWhoType.Attacker, TeleportWhereType.TOWN);
			
			// Spawn commander
			spawnCommander(getFort().getFortId());
			
			// Spawn door
			getFort().spawnDoor();
			
			// Spawn siege guard
			spawnSiegeGuard();
			
			// remove the tickets from the ground
			MercTicketManager.getInstance().deleteTickets(getFort().getFortId());
			
			// Reset respawn delay
			_defenderRespawnDelayPenalty = 0;
			getFort().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, FortSiegeManager.getInstance().getSiegeLength());
			ThreadPool.schedule(new ScheduleEndSiegeTask(getFort()), 1000); // Prepare auto end task
			announceToPlayer("The siege of " + getFort().getName() + " has started!", false);
			saveFortSiege();
			FortSiegeManager.getInstance().addSiege(this);
		}
	}
	
	/**
	 * Announce to player.
	 * @param message The String of the message to send to player
	 * @param inAreaOnly The boolean flag to show message to players in area only.
	 */
	public void announceToPlayer(String message, boolean inAreaOnly)
	{
		if (inAreaOnly)
		{
			getFort().getZone().announceToPlayers(message);
			return;
		}
		
		// Get all players
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendMessage(message);
		}
	}
	
	/**
	 * Update player siege state flags.
	 * @param clear the clear
	 */
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		Clan clan;
		for (SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (PlayerInstance member : clan.getOnlineMembers())
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 1);
				}
				
				member.sendPacket(new UserInfo(member));
				for (PlayerInstance player : member.getKnownList().getKnownPlayers().values())
				{
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
				}
			}
		}
		
		for (SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (PlayerInstance member : clan.getOnlineMembers())
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
				}
				else
				{
					member.setSiegeState((byte) 2);
				}
				
				member.sendPacket(new UserInfo(member));
				for (PlayerInstance player : member.getKnownList().getKnownPlayers().values())
				{
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
				}
			}
		}
	}
	
	/**
	 * Approve clan as defender for siege.
	 * @param clanId The int of player's clan id
	 */
	public void approveSiegeDefenderClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		
		saveSiegeClan(ClanTable.getInstance().getClan(clanId), 0, true);
		loadSiegeClan();
	}
	
	/**
	 * Check if in zone.
	 * @param object the object
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(WorldObject object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Check if in zone.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _isInProgress && getFort().checkIfInZone(x, y, z); // Fort zone during siege
	}
	
	/**
	 * Check is attacker.
	 * @param clan The Clan of the player
	 * @return true if clan is attacker
	 */
	public boolean checkIsAttacker(Clan clan)
	{
		return getAttackerClan(clan) != null;
	}
	
	/**
	 * Check is defender.
	 * @param clan The Clan of the player
	 * @return true if clan is defender
	 */
	public boolean checkIsDefender(Clan clan)
	{
		return getDefenderClan(clan) != null;
	}
	
	/**
	 * Check is defender waiting.
	 * @param clan The Clan of the player
	 * @return true if clan is defender waiting approval
	 */
	public boolean checkIsDefenderWaiting(Clan clan)
	{
		return getDefenderWaitingClan(clan) != null;
	}
	
	/**
	 * Clear all registered siege clans from database for fort.
	 */
	public void clearSiegeClan()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			statement.close();
			
			if (getFort().getOwnerId() > 0)
			{
				final PreparedStatement statement2 = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?");
				statement2.setInt(1, getFort().getOwnerId());
				statement2.execute();
				statement2.close();
			}
			
			getAttackerClans().clear();
			getDefenderClans().clear();
			_defenderWaitingClans.clear();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: clearSiegeClan(): " + e);
		}
	}
	
	/** Set the date for the next siege. */
	private void clearSiegeDate()
	{
		getFort().getSiegeDate().setTimeInMillis(0);
		_isRegistrationOver = false; // Allow registration for next siege
	}
	
	/**
	 * Clear all siege clans waiting for approval from database for fort.
	 */
	public void clearSiegeWaitingClan()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and type = 2");
			statement.setInt(1, getFort().getFortId());
			statement.execute();
			statement.close();
			
			_defenderWaitingClans.clear();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: clearSiegeWaitingClan(): " + e);
		}
	}
	
	/**
	 * Gets the attackers in zone.
	 * @return list of PlayerInstance registered as attacker in the zone.
	 */
	public List<PlayerInstance> getAttackersInZone()
	{
		final List<PlayerInstance> players = new ArrayList<>();
		Clan clan;
		for (SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (PlayerInstance player : clan.getOnlineMembers())
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * Return list of PlayerInstance registered as defender but not owner in the zone.
	 * @return the defenders but not owners in zone
	 */
	public List<PlayerInstance> getDefendersButNotOwnersInZone()
	{
		final List<PlayerInstance> players = new ArrayList<>();
		Clan clan;
		for (SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() == getFort().getOwnerId())
			{
				continue;
			}
			
			for (PlayerInstance player : clan.getOnlineMembers())
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
		return players;
	}
	
	/**
	 * Return list of PlayerInstance in the zone.
	 * @return the players in zone
	 */
	public List<PlayerInstance> getPlayersInZone()
	{
		return getFort().getZone().getAllPlayers();
	}
	
	/**
	 * Return list of PlayerInstance owning the fort in the zone.
	 * @return the owners in zone
	 */
	public List<PlayerInstance> getOwnersInZone()
	{
		final List<PlayerInstance> players = new ArrayList<>();
		Clan clan;
		for (SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() != getFort().getOwnerId())
			{
				continue;
			}
			
			for (PlayerInstance player : clan.getOnlineMembers())
			{
				if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
				{
					players.add(player);
				}
			}
		}
		
		return players;
	}
	
	/**
	 * Return list of PlayerInstance not registered as attacker or defender in the zone.
	 * @return the spectators in zone
	 */
	public List<PlayerInstance> getSpectatorsInZone()
	{
		final List<PlayerInstance> players = new ArrayList<>();
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			// quick check from player states, which don't include siege number however
			if (!player.isInsideZone(ZoneId.SIEGE) || (player.getSiegeState() != 0))
			{
				continue;
			}
			
			if (checkIfInZone(player.getX(), player.getY(), player.getZ()))
			{
				players.add(player);
			}
		}
		
		return players;
	}
	
	/**
	 * Control Tower was skilled.
	 * @param ct the ct
	 */
	public void killedCT(NpcInstance ct)
	{
		_defenderRespawnDelayPenalty += FortSiegeManager.getInstance().getControlTowerLosePenalty(); // Add respawn penalty to defenders for each control tower lose
	}
	
	/**
	 * Commanderr was skilled.
	 * @param ct the ct
	 */
	public void killedCommander(CommanderInstance ct)
	{
		if (_commanders != null)
		{
			_commanders.remove(ct);
			if (_commanders.isEmpty())
			{
				spawnFlag(getFort().getFortId());
			}
		}
	}
	
	/**
	 * Remove the flag that was killed.
	 * @param flag the flag
	 */
	public void killedFlag(NpcInstance flag)
	{
		if (flag == null)
		{
			return;
		}
		
		for (int i = 0; i < getAttackerClans().size(); i++)
		{
			if (getAttackerClan(i).removeFlag(flag))
			{
				return;
			}
		}
	}
	
	/**
	 * Display list of registered clans.
	 * @param player the player
	 */
	public void listRegisterClan(PlayerInstance player)
	{
		player.sendPacket(new FortressSiegeInfo(getFort()));
	}
	
	/**
	 * Register clan as attacker.
	 * @param player The PlayerInstance of the player trying to register
	 */
	public void registerAttacker(PlayerInstance player)
	{
		registerAttacker(player, false);
	}
	
	/**
	 * Register attacker.
	 * @param player the player
	 * @param force the force
	 */
	public void registerAttacker(PlayerInstance player, boolean force)
	{
		if (player.getClan() == null)
		{
			return;
		}
		
		int allyId = 0;
		if (getFort().getOwnerId() != 0)
		{
			allyId = ClanTable.getInstance().getClan(getFort().getOwnerId()).getAllyId();
		}
		
		if ((allyId != 0) && (player.getClan().getAllyId() == allyId) && !force)
		{
			player.sendMessage("You cannot register as an attacker because your alliance owns the fort");
			return;
		}
		
		if ((player.getInventory().getItemByItemId(57) != null) && (player.getInventory().getItemByItemId(57).getCount() < 250000))
		{
			player.sendMessage("You do not have enough adena.");
			return;
		}
		
		if (force || checkIfCanRegister(player))
		{
			player.getInventory().destroyItemByItemId("Siege", 57, 250000, player, player.getTarget());
			player.getInventory().updateDatabase();
			
			saveSiegeClan(player.getClan(), 1, false); // Save to database
			
			// if the first registering we start the timer
			if (getAttackerClans().size() == 1)
			{
				startAutoTask(true);
			}
		}
	}
	
	/**
	 * Register clan as defender.
	 * @param player The PlayerInstance of the player trying to register
	 */
	public void registerDefender(PlayerInstance player)
	{
		registerDefender(player, false);
	}
	
	/**
	 * Register defender.
	 * @param player the player
	 * @param force the force
	 */
	public void registerDefender(PlayerInstance player, boolean force)
	{
		if (getFort().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getFort().getName() + " is owned by NPC.");
		}
		else if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 2, false); // Save to database
		}
	}
	
	/**
	 * Remove clan from siege.
	 * @param clanId The int of player's clan id
	 */
	public void removeSiegeClan(int clanId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (clanId != 0)
			{
				statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=? and clan_id=?");
			}
			else
			{
				statement = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?");
			}
			
			statement.setInt(1, getFort().getFortId());
			if (clanId != 0)
			{
				statement.setInt(2, clanId);
			}
			
			statement.execute();
			statement.close();
			
			loadSiegeClan();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Remove clan from siege.
	 * @param clan the clan
	 */
	public void removeSiegeClan(Clan clan)
	{
		if ((clan == null) || (clan.getHasFort() == getFort().getFortId()) || !FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getFortId()))
		{
			return;
		}
		
		removeSiegeClan(clan.getClanId());
	}
	
	/**
	 * Remove clan from siege.
	 * @param player The PlayerInstance of player/clan being removed
	 */
	public void removeSiegeClan(PlayerInstance player)
	{
		removeSiegeClan(player.getClan());
	}
	
	/**
	 * Start the auto tasks
	 */
	public void checkAutoTask()
	{
		if (getFort().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			clearSiegeDate();
			saveSiegeDate();
			removeSiegeClan(0); // remove all clans
			return;
		}
		
		startAutoTask(false);
	}
	
	/**
	 * Start the auto tasks.
	 * @param setTime the set time
	 */
	public void startAutoTask(boolean setTime)
	{
		if (setTime)
		{
			setSiegeDateTime();
		}
		
		LOGGER.info("Siege of " + getFort().getName() + ": " + getFort().getSiegeDate().getTime());
		setScheduled(true);
		loadSiegeClan();
		
		// Schedule registration end
		_siegeRegistrationEndDate = Calendar.getInstance();
		_siegeRegistrationEndDate.setTimeInMillis(getFort().getSiegeDate().getTimeInMillis());
		_siegeRegistrationEndDate.add(Calendar.MINUTE, -10);
		
		// Schedule siege auto start
		ThreadPool.schedule(new ScheduleStartSiegeTask(getFort()), 1000);
	}
	
	/**
	 * Teleport players.
	 * @param teleportWho the teleport who
	 * @param teleportWhere the teleport where
	 */
	public void teleportPlayer(TeleportWhoType teleportWho, TeleportWhereType teleportWhere)
	{
		List<PlayerInstance> players;
		switch (teleportWho)
		{
			case Owner:
			{
				players = getOwnersInZone();
				break;
			}
			case Attacker:
			{
				players = getAttackersInZone();
				break;
			}
			case DefenderNotOwner:
			{
				players = getDefendersButNotOwnersInZone();
				break;
			}
			case Spectator:
			{
				players = getSpectatorsInZone();
				break;
			}
			default:
			{
				players = getFort().getZone().getAllPlayers();
			}
		}
		
		for (PlayerInstance player : players)
		{
			if (player.isGM() || player.isInJail())
			{
				continue;
			}
			
			player.teleToLocation(teleportWhere);
		}
	}
	
	/**
	 * Add clan as attacker.
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}
	
	/**
	 * Add clan as defender.
	 * @param clanId The int of clan's id
	 */
	private void addDefender(int clanId)
	{
		getDefenderClans().add(new SiegeClan(clanId, SiegeClanType.DEFENDER)); // Add registered defender to defender list
	}
	
	/**
	 * <p>
	 * Add clan as defender with the specified type
	 * </p>
	 * .
	 * @param clanId The int of clan's id
	 * @param type the type of the clan
	 */
	private void addDefender(int clanId, SiegeClanType type)
	{
		getDefenderClans().add(new SiegeClan(clanId, type));
	}
	
	/**
	 * Add clan as defender waiting approval.
	 * @param clanId The int of clan's id
	 */
	private void addDefenderWaiting(int clanId)
	{
		_defenderWaitingClans.add(new SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING)); // Add registered defender to defender list
	}
	
	/**
	 * Return true if the player can register.
	 * @param player The PlayerInstance of the player trying to register
	 * @return true, if successful
	 */
	private boolean checkIfCanRegister(PlayerInstance player)
	{
		if (_isRegistrationOver)
		{
			player.sendMessage("The deadline to register for the siege of " + getFort().getName() + " has passed.");
		}
		else if (_isInProgress)
		{
			player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
		}
		else if ((player.getClan() == null) || (player.getClan().getLevel() < FortSiegeManager.getInstance().getSiegeClanMinLevel()))
		{
			player.sendMessage("Only clans with Level " + FortSiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a fort siege.");
		}
		else if (player.getClan().getHasFort() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a fort.");
		}
		else if (player.getClan().getHasCastle() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a castle.");
		}
		else if (player.getClan().getClanId() == getFort().getOwnerId())
		{
			player.sendPacket(SystemMessageId.THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
		}
		else if (FortSiegeManager.getInstance().checkIsRegistered(player.getClan(), getFort().getFortId()))
		{
			player.sendMessage("You are already registered in a Siege.");
		}
		else
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the siege date time.
	 */
	private void setSiegeDateTime()
	{
		final Calendar newDate = Calendar.getInstance();
		newDate.add(Calendar.MINUTE, 60);
		getFort().setSiegeDate(newDate);
		saveSiegeDate();
	}
	
	/** Load siege clans. */
	private void loadSiegeClan()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			_defenderWaitingClans.clear();
			
			// Add fort owner as defender (add owner first so that they are on the top of the defender list)
			if (getFort().getOwnerId() > 0)
			{
				addDefender(getFort().getOwnerId(), SiegeClanType.OWNER);
			}
			
			PreparedStatement statement = null;
			ResultSet rs = null;
			statement = con.prepareStatement("SELECT clan_id,type FROM fortsiege_clans where fort_id=?");
			statement.setInt(1, getFort().getFortId());
			rs = statement.executeQuery();
			int typeId;
			
			while (rs.next())
			{
				typeId = rs.getInt("type");
				if (typeId == 0)
				{
					addDefender(rs.getInt("clan_id"));
				}
				else if (typeId == 1)
				{
					addAttacker(rs.getInt("clan_id"));
				}
				else if (typeId == 2)
				{
					addDefenderWaiting(rs.getInt("clan_id"));
				}
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: loadSiegeClan(): " + e);
		}
	}
	
	/** Remove artifacts spawned. */
	private void removeCommander()
	{
		if (_commanders != null)
		{
			// Remove all instance of artifact for this fort
			for (CommanderInstance commander : _commanders)
			{
				if (commander != null)
				{
					commander.decayMe();
				}
			}
			_commanders = null;
		}
	}
	
	/** Remove all flags. */
	private void removeFlags()
	{
		for (SiegeClan sc : getAttackerClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
		for (SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}
	
	/** Save fort siege related to database. */
	private void saveFortSiege()
	{
		clearSiegeDate(); // clear siege date
		saveSiegeDate(); // Save the new date
		setScheduled(false);
	}
	
	/** Save siege date to database. */
	private void saveSiegeDate()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Update fort set siegeDate = ? where id = ?");
			statement.setLong(1, getFort().getSiegeDate().getTimeInMillis());
			statement.setInt(2, getFort().getFortId());
			statement.execute();
			
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: saveSiegeDate(): " + e);
		}
	}
	
	/**
	 * Save registration to database.
	 * @param clan The Clan of player
	 * @param typeId -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 * @param isUpdateRegistration the is update registration
	 */
	private void saveSiegeClan(Clan clan, int typeId, boolean isUpdateRegistration)
	{
		if (clan.getHasFort() > 0)
		{
			return;
		}
		
		if ((typeId == 0) || (typeId == 2) || (typeId == -1))
		{
			if ((getDefenderClans().size() + getDefenderWaitingClans().size()) >= FortSiegeManager.getInstance().getDefenderMaxClans())
			{
				return;
			}
		}
		else if (getAttackerClans().size() >= FortSiegeManager.getInstance().getAttackerMaxClans())
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (!isUpdateRegistration)
			{
				statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id,type,fort_owner) values (?,?,?,0)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, getFort().getFortId());
				statement.setInt(3, typeId);
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("Update fortsiege_clans set type = ? where fort_id = ? and clan_id = ?");
				statement.setInt(1, typeId);
				statement.setInt(2, getFort().getFortId());
				statement.setInt(3, clan.getClanId());
				statement.execute();
				statement.close();
			}
			
			if ((typeId == 0) || (typeId == -1))
			{
				addDefender(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to defend " + getFort().getName(), false);
			}
			else if (typeId == 1)
			{
				addAttacker(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to attack " + getFort().getName(), false);
			}
			else if (typeId == 2)
			{
				addDefenderWaiting(clan.getClanId());
				announceToPlayer(clan.getName() + " has requested to defend " + getFort().getName(), false);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: saveSiegeClan(Pledge clan, int typeId, boolean isUpdateRegistration): " + e);
		}
	}
	
	/**
	 * Spawn artifact.
	 * @param id the id
	 */
	private void spawnCommander(int id)
	{
		// Set commanders array size if one does not exist
		if (_commanders == null)
		{
			_commanders = new ArrayList<>();
		}
		
		for (SiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(id))
		{
			CommanderInstance commander;
			commander = new CommanderInstance(IdFactory.getNextId(), NpcTable.getInstance().getTemplate(_sp.getNpcId()));
			commander.setCurrentHpMp(commander.getMaxHp(), commander.getMaxMp());
			commander.setHeading(_sp.getLocation().getHeading());
			commander.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 50);
			_commanders.add(commander);
		}
	}
	
	/**
	 * Spawn flag.
	 * @param id the id
	 */
	private void spawnFlag(int id)
	{
		if (_combatflag == null)
		{
			_combatflag = new ArrayList<>();
		}
		
		for (SiegeSpawn _sp : FortSiegeManager.getInstance().getFlagList(id))
		{
			ArtefactInstance combatflag;
			combatflag = new ArtefactInstance(IdFactory.getNextId(), NpcTable.getInstance().getTemplate(_sp.getNpcId()));
			combatflag.setCurrentHpMp(combatflag.getMaxHp(), combatflag.getMaxMp());
			combatflag.setHeading(_sp.getLocation().getHeading());
			combatflag.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 10);
			_combatflag.add(combatflag);
		}
	}
	
	/**
	 * Un spawn flags.
	 */
	private void unSpawnFlags()
	{
		if (_combatflag != null)
		{
			// Remove all instance of artifact for this fort
			for (ArtefactInstance _sp : _combatflag)
			{
				if (_sp != null)
				{
					_sp.decayMe();
				}
			}
			_combatflag = null;
		}
	}
	
	/**
	 * Spawn siege guard.
	 */
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
	}
	
	/**
	 * Gets the attacker clan.
	 * @param clan the clan
	 * @return the attacker clan
	 */
	public SiegeClan getAttackerClan(Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getAttackerClan(clan.getClanId());
	}
	
	/**
	 * Gets the attacker clan.
	 * @param clanId the clan id
	 * @return the attacker clan
	 */
	public SiegeClan getAttackerClan(int clanId)
	{
		for (SiegeClan sc : getAttackerClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	/**
	 * Gets the attacker clans.
	 * @return the attacker clans
	 */
	public List<SiegeClan> getAttackerClans()
	{
		if (_isNormalSide)
		{
			return _attackerClans;
		}
		return _defenderClans;
	}
	
	/**
	 * Gets the attacker respawn delay.
	 * @return the attacker respawn delay
	 */
	public int getAttackerRespawnDelay()
	{
		return FortSiegeManager.getInstance().getAttackerRespawnDelay();
	}
	
	/**
	 * Gets the fort.
	 * @return the fort
	 */
	public Fort getFort()
	{
		if ((_fort == null) || (_fort.length <= 0))
		{
			return null;
		}
		return _fort[0];
	}
	
	/**
	 * Gets the defender clan.
	 * @param clan the clan
	 * @return the defender clan
	 */
	public SiegeClan getDefenderClan(Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderClan(clan.getClanId());
	}
	
	/**
	 * Gets the defender clan.
	 * @param clanId the clan id
	 * @return the defender clan
	 */
	public SiegeClan getDefenderClan(int clanId)
	{
		for (SiegeClan sc : getDefenderClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	/**
	 * Gets the defender clans.
	 * @return the defender clans
	 */
	public List<SiegeClan> getDefenderClans()
	{
		if (_isNormalSide)
		{
			return _defenderClans;
		}
		return _attackerClans;
	}
	
	/**
	 * Gets the defender waiting clan.
	 * @param clan the clan
	 * @return the defender waiting clan
	 */
	public SiegeClan getDefenderWaitingClan(Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderWaitingClan(clan.getClanId());
	}
	
	/**
	 * Gets the defender waiting clan.
	 * @param clanId the clan id
	 * @return the defender waiting clan
	 */
	public SiegeClan getDefenderWaitingClan(int clanId)
	{
		for (SiegeClan sc : _defenderWaitingClans)
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	/**
	 * Gets the defender waiting clans.
	 * @return the defender waiting clans
	 */
	public List<SiegeClan> getDefenderWaitingClans()
	{
		return _defenderWaitingClans;
	}
	
	/**
	 * Gets the defender respawn delay.
	 * @return the defender respawn delay
	 */
	public int getDefenderRespawnDelay()
	{
		return FortSiegeManager.getInstance().getDefenderRespawnDelay() + _defenderRespawnDelayPenalty;
	}
	
	/**
	 * Gets the checks if is in progress.
	 * @return the checks if is in progress
	 */
	public boolean isInProgress()
	{
		return _isInProgress;
	}
	
	/**
	 * Gets the checks if is scheduled.
	 * @return the checks if is scheduled
	 */
	public boolean isScheduled()
	{
		return _isScheduled;
	}
	
	/**
	 * Sets the checks if is scheduled.
	 * @param isScheduled the new checks if is scheduled
	 */
	public void setScheduled(boolean isScheduled)
	{
		_isScheduled = isScheduled;
	}
	
	/**
	 * Gets the checks if is registration over.
	 * @return the checks if is registration over
	 */
	public boolean isRegistrationOver()
	{
		return _isRegistrationOver;
	}
	
	/**
	 * Gets the siege date.
	 * @return the siege date
	 */
	public Calendar getSiegeDate()
	{
		return getFort().getSiegeDate();
	}
	
	/**
	 * Gets the flag.
	 * @param clan the clan
	 * @return the flag
	 */
	public List<NpcInstance> getFlag(Clan clan)
	{
		if (clan != null)
		{
			final SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
			{
				return sc.getFlag();
			}
		}
		return null;
	}
	
	/**
	 * Gets the siege guard manager.
	 * @return the siege guard manager
	 */
	public FortSiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			_siegeGuardManager = new FortSiegeGuardManager(getFort());
		}
		return _siegeGuardManager;
	}
}
