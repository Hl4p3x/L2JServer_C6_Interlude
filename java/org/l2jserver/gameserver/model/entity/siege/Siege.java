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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.instancemanager.MercTicketManager;
import org.l2jserver.gameserver.instancemanager.SiegeGuardManager;
import org.l2jserver.gameserver.instancemanager.SiegeManager;
import org.l2jserver.gameserver.instancemanager.SiegeManager.SiegeSpawn;
import org.l2jserver.gameserver.model.SiegeClan;
import org.l2jserver.gameserver.model.SiegeClan.SiegeClanType;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.ArtefactInstance;
import org.l2jserver.gameserver.model.actor.instance.ControlTowerInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.RelationChanged;
import org.l2jserver.gameserver.network.serverpackets.SiegeInfo;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * The Class Siege.
 */
public class Siege
{
	// ==========================================================================================
	// Message to add/check
	// id=17 msg=[Castle siege has begun.] c3_attr1=[SystemMsg_k.17]
	// id=18 msg=[Castle siege is over.] c3_attr1=[SystemMsg_k.18]
	// id=288 msg=[The castle gate has been broken down.]
	// id=291 msg=[Clan $s1 is victorious over $s2's castle siege!]
	// id=292 msg=[$s1 has announced the castle siege time.]
	// - id=293 msg=[The registration term for $s1 has ended.]
	// - id=358 msg=[$s1 hour(s) until castle siege conclusion.]
	// - id=359 msg=[$s1 minute(s) until castle siege conclusion.]
	// - id=360 msg=[Castle siege $s1 second(s) left!]
	// id=640 msg=[You have failed to refuse castle defense aid.]
	// id=641 msg=[You have failed to approve castle defense aid.]
	// id=644 msg=[You are not yet registered for the castle siege.]
	// - id=645 msg=[Only clans with Level 4 and higher may register for a castle siege.]
	// id=646 msg=[You do not have the authority to modify the castle defender list.]
	// - id=688 msg=[The clan that owns the castle is automatically registered on the defending side.]
	// id=689 msg=[A clan that owns a castle cannot participate in another siege.]
	// id=690 msg=[You cannot register on the attacking side because you are part of an alliance with the clan that owns the castle.]
	// id=718 msg=[The castle gates cannot be opened and closed during a siege.]
	// - id=295 msg=[$s1's siege was canceled because there were no clans that participated.]
	// id=659 msg=[This is not the time for siege registration and so registrations cannot be accepted or rejected.]
	// - id=660 msg=[This is not the time for siege registration and so registration and cancellation cannot be done.]
	// id=663 msg=[The siege time has been declared for $s. It is not possible to change the time after a siege time has been declared. Do you want to continue?]
	// id=667 msg=[You are registering on the attacking side of the $s1 siege. Do you want to continue?]
	// id=668 msg=[You are registering on the defending side of the $s1 siege. Do you want to continue?]
	// id=669 msg=[You are canceling your application to participate in the $s1 siege battle. Do you want to continue?]
	// id=707 msg=[You cannot teleport to a village that is in a siege.]
	// - id=711 msg=[The siege of $s1 has started.]
	// - id=712 msg=[The siege of $s1 has finished.]
	// id=844 msg=[The siege to conquer $s1 has begun.]
	// - id=845 msg=[The deadline to register for the siege of $s1 has passed.]
	// - id=846 msg=[The siege of $s1 has been canceled due to lack of interest.]
	// - id=856 msg=[The siege of $s1 has ended in a draw.]
	// id=285 msg=[Clan $s1 has succeeded in engraving the ruler!]
	// - id=287 msg=[The opponent clan has begun to engrave the ruler.]
	
	protected static final Logger LOGGER = Logger.getLogger(Siege.class.getName());
	private final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	
	public enum TeleportWhoType
	{
		All,
		Attacker,
		DefenderNotOwner,
		Owner,
		Spectator
	}
	
	private int _controlTowerCount;
	private int _controlTowerMaxCount;
	
	/**
	 * Gets the control tower count.
	 * @return the control tower count
	 */
	public int getControlTowerCount()
	{
		return _controlTowerCount;
	}
	
	/**
	 * The Class ScheduleEndSiegeTask.
	 */
	public class ScheduleEndSiegeTask implements Runnable
	{
		/** The _castle inst. */
		private final Castle _castleInst;
		
		/**
		 * Instantiates a new schedule end siege task.
		 * @param pCastle the castle
		 */
		public ScheduleEndSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
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
					// Prepare task for 1 hr left.
					ThreadPool.schedule(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 3600000);
				}
				else if ((timeRemaining <= 3600000) && (timeRemaining > 600000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					
					// Prepare task for 10 minute left.
					ThreadPool.schedule(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 600000);
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					
					// Prepare task for 5 minute left.
					ThreadPool.schedule(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 300000);
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					
					// Prepare task for 10 seconds count down
					ThreadPool.schedule(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 10000);
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					announceToPlayer(getCastle().getName() + " siege " + (timeRemaining / 1000) + " second(s) left!", true);
					
					// Prepare task for second count down
					ThreadPool.schedule(new ScheduleEndSiegeTask(_castleInst), timeRemaining);
				}
				else
				{
					_castleInst.getSiege().endSiege();
				}
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Castle _castleInst;
		
		/**
		 * Instantiates a new schedule start siege task.
		 * @param pCastle the castle
		 */
		public ScheduleStartSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
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
					ThreadPool.schedule(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 86400000);
				}
				else if ((timeRemaining <= 86400000) && (timeRemaining > 13600000))
				{
					announceToPlayer("The registration term for " + getCastle().getName() + " has ended.", false);
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					
					// Prepare task for 1 hr left before siege start.
					ThreadPool.schedule(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 13600000);
				}
				else if ((timeRemaining <= 13600000) && (timeRemaining > 600000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					
					// Prepare task for 10 minute left.
					ThreadPool.schedule(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 600000);
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					
					// Prepare task for 5 minute left.
					ThreadPool.schedule(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 300000);
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					announceToPlayer((timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					
					// Prepare task for 10 seconds count down
					ThreadPool.schedule(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 10000);
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					announceToPlayer(getCastle().getName() + " siege " + (timeRemaining / 1000) + " second(s) to start!", false);
					
					// Prepare task for second count down
					ThreadPool.schedule(new ScheduleStartSiegeTask(_castleInst), timeRemaining);
				}
				else
				{
					_castleInst.getSiege().startSiege();
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
	private List<ArtefactInstance> _artifacts = new ArrayList<>();
	private List<ControlTowerInstance> _controlTowers = new ArrayList<>();
	private final Castle[] _castle;
	boolean _isInProgress = false;
	private boolean _isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	protected boolean _isRegistrationOver = false;
	protected Calendar _siegeEndDate;
	private SiegeGuardManager _siegeGuardManager;
	protected Calendar _siegeRegistrationEndDate;
	
	/**
	 * Instantiates a new siege.
	 * @param castle the castle
	 */
	public Siege(Castle[] castle)
	{
		_castle = castle;
		_siegeGuardManager = new SiegeGuardManager(getCastle());
		startAutoTask();
	}
	
	/**
	 * When siege ends
	 */
	public void endSiege()
	{
		if (_isInProgress)
		{
			announceToPlayer("The siege of " + getCastle().getName() + " has finished!", false);
			final PlaySound sound = new PlaySound("systemmsg_e.18");
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				player.sendPacket(sound);
			}
			
			LOGGER.info("[SIEGE] The siege of " + getCastle().getName() + " has finished! " + fmt.format(new Date(System.currentTimeMillis())));
			if (getCastle().getOwnerId() <= 0)
			{
				announceToPlayer("The siege of " + getCastle().getName() + " has ended in a draw.", false);
				
				LOGGER.info("[SIEGE] The siege of " + getCastle().getName() + " has ended in a draw. " + fmt.format(new Date(System.currentTimeMillis())));
			}
			
			// Removes all flags. Note: Remove flag before teleporting players
			removeFlags();
			
			// Teleport to the second closest town
			teleportPlayer(TeleportWhoType.Attacker, TeleportWhereType.TOWN);
			
			// Teleport to the second closest town
			teleportPlayer(TeleportWhoType.DefenderNotOwner, TeleportWhereType.TOWN);
			
			// Teleport to the second closest town
			teleportPlayer(TeleportWhoType.Spectator, TeleportWhereType.TOWN);
			
			// Flag so that siege instance can be started
			_isInProgress = false;
			updatePlayerSiegeStateFlags(true);
			
			// Save castle specific data
			saveCastleSiege();
			
			// Clear siege clan from db
			clearSiegeClan();
			
			// Remove artifact from this castle
			removeArtifact();
			
			// Remove all control tower from this castle
			removeControlTower();
			
			// Remove all spawned siege guard from this castle
			_siegeGuardManager.unspawnSiegeGuard();
			
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();
			}
			
			// Respawn door to castle
			getCastle().spawnDoor();
			getCastle().getZone().updateZoneStatusForCharactersInside();
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
	 * When control of castle changed during siege
	 */
	public void midVictory()
	{
		if (_isInProgress) // Siege still in progress
		{
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs(); // Remove all merc entry from db
			}
			
			if ((getDefenderClans().isEmpty()) && // If defender doesn't exist (Pc vs Npc)
				(getAttackerClans().size() == 1)) // Only 1 attacker
			{
				final SiegeClan scNewOwner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(scNewOwner);
				addDefender(scNewOwner, SiegeClanType.OWNER);
				endSiege();
				
				return;
			}
			
			if (getCastle().getOwnerId() > 0)
			{
				final int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				// If defender doesn't exist (Pc vs Npc) and only an alliance attacks
				// The player's clan is in an alliance
				if (getDefenderClans().isEmpty() && (allyId != 0))
				{
					boolean allinsamealliance = true;
					for (SiegeClan sc : getAttackerClans())
					{
						if ((sc != null) && (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId))
						{
							allinsamealliance = false;
						}
					}
					if (allinsamealliance)
					{
						final SiegeClan scNewOwner = getAttackerClan(getCastle().getOwnerId());
						removeAttacker(scNewOwner);
						addDefender(scNewOwner, SiegeClanType.OWNER);
						endSiege();
						return;
					}
				}
				
				for (SiegeClan sc : getDefenderClans())
				{
					if (sc != null)
					{
						removeDefender(sc);
						addAttacker(sc);
					}
				}
				
				final SiegeClan scNewOwner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(scNewOwner);
				addDefender(scNewOwner, SiegeClanType.OWNER);
				
				// The player's clan is in an alliance
				if (allyId != 0)
				{
					final Clan[] clanList = ClanTable.getInstance().getClans();
					for (Clan clan : clanList)
					{
						if (clan.getAllyId() == allyId)
						{
							final SiegeClan sc = getAttackerClan(clan.getClanId());
							if (sc != null)
							{
								removeAttacker(sc);
								addDefender(sc, SiegeClanType.DEFENDER);
							}
						}
					}
				}
				
				// Teleport to the second closest town
				teleportPlayer(TeleportWhoType.Attacker, TeleportWhereType.SIEGEFLAG);
				
				// Teleport to the second closest town
				teleportPlayer(TeleportWhoType.Spectator, TeleportWhereType.TOWN);
				
				// Removes defenders' flags
				removeDefenderFlags();
				
				// Remove all castle upgrade
				getCastle().removeUpgrade();
				
				// Respawn door to castle but make them weaker (50% hp)
				getCastle().spawnDoor(true);
				
				// Remove all control tower from this castle
				removeControlTower();
				
				// Each new siege midvictory CT are completely respawned.
				_controlTowerCount = 0;
				_controlTowerMaxCount = 0;
				spawnControlTower(getCastle().getCastleId());
				updatePlayerSiegeStateFlags(false);
			}
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
				if (getCastle().getOwnerId() <= 0)
				{
					sm = new SystemMessage(SystemMessageId.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED);
				}
				
				sm.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(sm);
				return;
			}
			
			// Atk is now atk
			_isNormalSide = true;
			
			// Flag so that same siege instance cannot be started again
			_isInProgress = true;
			
			// Load siege clan from db
			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			
			// Teleport to the closest town
			teleportPlayer(TeleportWhoType.Attacker, TeleportWhereType.TOWN);
			_controlTowerCount = 0;
			_controlTowerMaxCount = 0;
			
			// Spawn artifact
			spawnArtifact(getCastle().getCastleId());
			
			// Spawn control tower
			spawnControlTower(getCastle().getCastleId());
			
			// Spawn door
			getCastle().spawnDoor();
			
			// Spawn siege guard
			spawnSiegeGuard();
			
			// remove the tickets from the ground
			MercTicketManager.getInstance().deleteTickets(getCastle().getCastleId());
			
			// Reset respawn delay
			_defenderRespawnDelayPenalty = 0;
			getCastle().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, SiegeManager.getInstance().getSiegeLength());
			
			// Prepare auto end task
			ThreadPool.schedule(new ScheduleEndSiegeTask(getCastle()), 1000);
			announceToPlayer("The siege of " + getCastle().getName() + " has started!", false);
			final PlaySound sound = new PlaySound("systemmsg_e.17");
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				player.sendPacket(sound);
			}
			
			LOGGER.info("[SIEGE] The siege of " + getCastle().getName() + " has started! " + fmt.format(new Date(System.currentTimeMillis())));
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
			getCastle().getZone().announceToPlayers(message);
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
	 * Return true if object is inside the zone.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _isInProgress && getCastle().checkIfInZone(x, y, z); // Castle zone during siege
	}
	
	/**
	 * Return true if clan is attacker.
	 * @param clan The Clan of the player
	 * @return true, if successful
	 */
	public boolean checkIsAttacker(Clan clan)
	{
		return getAttackerClan(clan) != null;
	}
	
	/**
	 * Return true if clan is defender.
	 * @param clan The Clan of the player
	 * @return true, if successful
	 */
	public boolean checkIsDefender(Clan clan)
	{
		return getDefenderClan(clan) != null;
	}
	
	/**
	 * Return true if clan is defender waiting approval.
	 * @param clan The Clan of the player
	 * @return true, if successful
	 */
	public boolean checkIsDefenderWaiting(Clan clan)
	{
		return getDefenderWaitingClan(clan) != null;
	}
	
	/**
	 * Clear all registered siege clans from database for castle.
	 */
	public void clearSiegeClan()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();
			
			if (getCastle().getOwnerId() > 0)
			{
				final PreparedStatement statement2 = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
				statement2.setInt(1, getCastle().getOwnerId());
				statement2.execute();
				statement2.close();
			}
			
			getAttackerClans().clear();
			getDefenderClans().clear();
			_defenderWaitingClans.clear();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Clear all siege clans waiting for approval from database for castle.
	 */
	public void clearSiegeWaitingClan()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();
			
			_defenderWaitingClans.clear();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	/**
	 * Return list of PlayerInstance registered as attacker in the zone.
	 * @return the attackers in zone
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
			if (clan.getClanId() == getCastle().getOwnerId())
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
		return getCastle().getZone().getAllPlayers();
	}
	
	/**
	 * Return list of PlayerInstance owning the castle in the zone.
	 * @return the owners in zone
	 */
	public List<PlayerInstance> getOwnersInZone()
	{
		final List<PlayerInstance> players = new ArrayList<>();
		Clan clan;
		for (SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() != getCastle().getOwnerId())
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
		// Add respawn penalty to defenders for each control tower lose
		_defenderRespawnDelayPenalty += SiegeManager.getInstance().getControlTowerLosePenalty();
		_controlTowerCount--;
		
		if (_controlTowerCount < 0)
		{
			_controlTowerCount = 0;
		}
		
		if ((_controlTowerMaxCount > 0) && (SiegeManager.getInstance().getControlTowerLosePenalty() > 0))
		{
			_defenderRespawnDelayPenalty = ((_controlTowerMaxCount - _controlTowerCount) / _controlTowerCount) * SiegeManager.getInstance().getControlTowerLosePenalty();
		}
		else
		{
			_defenderRespawnDelayPenalty = 0;
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
		player.sendPacket(new SiegeInfo(getCastle()));
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
		if (getCastle().getOwnerId() != 0)
		{
			allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		}
		
		if ((allyId != 0) && (player.getClan().getAllyId() == allyId) && !force)
		{
			player.sendMessage("You cannot register as an attacker because your alliance owns the castle");
			return;
		}
		
		if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 1, false);
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
		if (getCastle().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
		}
		else if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan(), 2, false);
		}
	}
	
	/**
	 * Remove clan from siege.
	 * @param clanId The int of player's clan id
	 */
	public void removeSiegeClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, clanId);
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
		if ((clan == null) || (clan.getHasCastle() == getCastle().getCastleId()) || !SiegeManager.getInstance().checkIsRegistered(clan, getCastle().getCastleId()))
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
	public void startAutoTask()
	{
		correctSiegeDateTime();
		
		LOGGER.info("Siege of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());
		loadSiegeClan();
		
		// Schedule registration end
		_siegeRegistrationEndDate = Calendar.getInstance();
		_siegeRegistrationEndDate.setTimeInMillis(getCastle().getSiegeDate().getTimeInMillis());
		_siegeRegistrationEndDate.add(Calendar.DAY_OF_MONTH, -1);
		
		// Schedule siege auto start
		ThreadPool.schedule(new ScheduleStartSiegeTask(getCastle()), 1000);
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
				players = getCastle().getZone().getAllPlayers();
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
		// Add registered attacker to attacker list
		getAttackerClans().add(new SiegeClan(clanId, SiegeClanType.ATTACKER));
	}
	
	/**
	 * Add clan as defender.
	 * @param clanId The int of clan's id
	 */
	private void addDefender(int clanId)
	{
		// Add registered defender to defender list
		getDefenderClans().add(new SiegeClan(clanId, SiegeClanType.DEFENDER));
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
		// Add registered defender to defender list
		_defenderWaitingClans.add(new SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING));
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
			player.sendMessage("The deadline to register for the siege of " + getCastle().getName() + " has passed.");
		}
		else if (_isInProgress)
		{
			player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
		}
		else if ((player.getClan() == null) || (player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel()))
		{
			player.sendMessage("Only clans with Level " + SiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a castle siege.");
		}
		else if (player.getClan().getHasCastle() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a castle.");
		}
		else if (player.getClan().getHasFort() > 0)
		{
			player.sendMessage("You cannot register because your clan already own a fort.");
		}
		else if (player.getClan().getClanId() == getCastle().getOwnerId())
		{
			player.sendPacket(SystemMessageId.THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
		}
		else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getCastleId()))
		{
			player.sendMessage("You are already registered in a Siege.");
		}
		else if (checkIfAlreadyRegisteredForAnotherSiege(player.getClan()))
		{
			player.sendMessage("You are already registered in another Siege.");
		}
		else
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if the clan has already registered to a siege for the same day.
	 * @param clan The Clan of the player trying to register
	 * @return true, if successful
	 */
	private boolean checkIfAlreadyRegisteredForAnotherSiege(Clan clan)
	{
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (siege == this)
			{
				continue;
			}
			if (siege.checkIsAttacker(clan))
			{
				return true;
			}
			if (siege.checkIsDefender(clan))
			{
				return true;
			}
			if (siege.checkIsDefenderWaiting(clan))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return the correct siege date as Calendar.
	 */
	private void correctSiegeDateTime()
	{
		boolean corrected = false;
		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Since siege has past reschedule it to the next one (14 days)
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}
		
		if (getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != getCastle().getSiegeDayOfWeek())
		{
			corrected = true;
			getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, getCastle().getSiegeDayOfWeek());
		}
		
		if (getCastle().getSiegeDate().get(Calendar.HOUR_OF_DAY) != getCastle().getSiegeHourOfDay())
		{
			corrected = true;
			getCastle().getSiegeDate().set(Calendar.HOUR_OF_DAY, getCastle().getSiegeHourOfDay());
		}
		
		getCastle().getSiegeDate().set(Calendar.MINUTE, 0);
		if (corrected)
		{
			saveSiegeDate();
		}
	}
	
	/** Load siege clans. */
	private void loadSiegeClan()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			_defenderWaitingClans.clear();
			
			// Add castle owner as defender (add owner first so that they are on the top of the defender list)
			if (getCastle().getOwnerId() > 0)
			{
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			}
			
			PreparedStatement statement = null;
			ResultSet rs = null;
			statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
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
			
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.info("Exception: loadSiegeClan(): " + e);
		}
	}
	
	/** Remove artifacts spawned. */
	private void removeArtifact()
	{
		if (_artifacts != null)
		{
			// Remove all instance of artifact for this castle
			for (ArtefactInstance art : _artifacts)
			{
				if (art != null)
				{
					art.decayMe();
				}
			}
			_artifacts = null;
		}
	}
	
	/** Remove all control tower spawned. */
	private void removeControlTower()
	{
		if (_controlTowers != null)
		{
			// Remove all instance of control tower for this castle
			for (ControlTowerInstance ct : _controlTowers)
			{
				if (ct != null)
				{
					ct.decayMe();
				}
			}
			
			_controlTowers = null;
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
	
	/** Remove flags from defenders. */
	private void removeDefenderFlags()
	{
		for (SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}
	
	/** Save castle siege related to database. */
	private void saveCastleSiege()
	{
		setNextSiegeDate(); // Set the next set date for 2 weeks from now
		saveSiegeDate(); // Save the new date
		startAutoTask(); // Prepare auto start siege and end registration
	}
	
	/** Save siege date to database. */
	private void saveSiegeDate()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Update castle set siegeDate = ? where id = ?");
			statement.setLong(1, getCastle().getSiegeDate().getTimeInMillis());
			statement.setInt(2, getCastle().getCastleId());
			statement.execute();
			
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.info("Exception: saveSiegeDate(): " + e);
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
		if (clan.getHasCastle() > 0)
		{
			return;
		}
		
		if ((typeId == 0) || (typeId == 2) || (typeId == -1))
		{
			if ((getDefenderClans().size() + getDefenderWaitingClans().size()) >= SiegeManager.getInstance().getDefenderMaxClans())
			{
				return;
			}
		}
		else if (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans())
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (!isUpdateRegistration)
			{
				statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) values (?,?,?,0)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, typeId);
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("Update siege_clans set type = ? where castle_id = ? and clan_id = ?");
				statement.setInt(1, typeId);
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, clan.getClanId());
				statement.execute();
				statement.close();
			}
			
			if ((typeId == 0) || (typeId == -1))
			{
				addDefender(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to defend " + getCastle().getName(), false);
			}
			else if (typeId == 1)
			{
				addAttacker(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to attack " + getCastle().getName(), false);
			}
			else if (typeId == 2)
			{
				addDefenderWaiting(clan.getClanId());
				announceToPlayer(clan.getName() + " has requested to defend " + getCastle().getName(), false);
			}
		}
		catch (Exception e)
		{
			LOGGER.info("Exception: saveSiegeClan(Pledge clan, int typeId, boolean isUpdateRegistration): " + e);
		}
	}
	
	/** Set the date for the next siege. */
	private void setNextSiegeDate()
	{
		while (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Set next siege date if siege has passed
			// Schedule to happen in 14 days
			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 14);
		}
		
		// Allow registration for next siege
		_isRegistrationOver = false;
	}
	
	/**
	 * Spawn artifact.
	 * @param id the id
	 */
	private void spawnArtifact(int id)
	{
		// Set artefact array size if one does not exist
		if (_artifacts == null)
		{
			_artifacts = new ArrayList<>();
		}
		
		for (SiegeSpawn _sp : SiegeManager.getInstance().getArtefactSpawnList(id))
		{
			ArtefactInstance art;
			art = new ArtefactInstance(IdFactory.getNextId(), NpcTable.getInstance().getTemplate(_sp.getNpcId()));
			art.setCurrentHpMp(art.getMaxHp(), art.getMaxMp());
			art.setHeading(_sp.getLocation().getHeading());
			art.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 50);
			_artifacts.add(art);
		}
	}
	
	/**
	 * Spawn control tower.
	 * @param id the id
	 */
	private void spawnControlTower(int id)
	{
		// Set control tower array size if one does not exist
		if (_controlTowers == null)
		{
			_controlTowers = new ArrayList<>();
		}
		
		for (SiegeSpawn _sp : SiegeManager.getInstance().getControlTowerSpawnList(id))
		{
			ControlTowerInstance ct;
			
			final NpcTemplate template = NpcTable.getInstance().getTemplate(_sp.getNpcId());
			template.getStatSet().set("baseHpMax", _sp.getHp());
			ct = new ControlTowerInstance(IdFactory.getNextId(), template);
			ct.setCurrentHpMp(ct.getMaxHp(), ct.getMaxMp());
			ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);
			_controlTowerCount++;
			_controlTowerMaxCount++;
			_controlTowers.add(ct);
		}
	}
	
	/**
	 * Spawn siege guard.
	 */
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
		
		// Register guard to the closest Control Tower
		// When CT dies, so do all the guards that it controls
		if (!getSiegeGuardManager().getSiegeGuardSpawn().isEmpty() && !_controlTowers.isEmpty())
		{
			ControlTowerInstance closestCt;
			double distance;
			double x;
			double y;
			double z;
			double distanceClosest = 0;
			for (Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
			{
				if (spawn == null)
				{
					continue;
				}
				
				closestCt = null;
				distanceClosest = 0;
				for (ControlTowerInstance ct : _controlTowers)
				{
					if (ct == null)
					{
						continue;
					}
					
					x = spawn.getX() - ct.getX();
					y = spawn.getY() - ct.getY();
					z = spawn.getZ() - ct.getZ();
					distance = (x * x) + (y * y) + (z * z);
					if ((closestCt == null) || (distance < distanceClosest))
					{
						closestCt = ct;
						distanceClosest = distance;
					}
				}
				
				if (closestCt != null)
				{
					closestCt.registerGuard(spawn);
				}
			}
		}
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
		return SiegeManager.getInstance().getAttackerRespawnDelay();
	}
	
	/**
	 * Gets the castle.
	 * @return the castle
	 */
	public Castle getCastle()
	{
		if ((_castle == null) || (_castle.length <= 0))
		{
			return null;
		}
		return _castle[0];
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
		return SiegeManager.getInstance().getDefenderRespawnDelay() + _defenderRespawnDelayPenalty;
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
		return getCastle().getSiegeDate();
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
	public SiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			_siegeGuardManager = new SiegeGuardManager(getCastle());
		}
		return _siegeGuardManager;
	}
}
