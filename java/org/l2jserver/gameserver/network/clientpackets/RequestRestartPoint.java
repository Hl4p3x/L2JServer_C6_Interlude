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
package org.l2jserver.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.instancemanager.FortManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.SiegeClan;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.network.serverpackets.Revive;
import org.l2jserver.gameserver.util.IllegalPlayerAction;
import org.l2jserver.gameserver.util.Util;

/**
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestartPoint extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestRestartPoint.class.getName());
	
	protected int _requestedPointType;
	protected boolean _continuation;
	
	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isFakeDeath())
		{
			player.stopFakeDeath(null);
			player.broadcastPacket(new Revive(player));
			return;
		}
		else if (!player.isAlikeDead())
		{
			LOGGER.warning("Living player [" + player.getName() + "] called RestartPointPacket! Ban this player!");
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(player.getX(), player.getY(), player.getZ());
		if ((castle != null) && castle.getSiege().isInProgress() && (player.getClan() != null) && castle.getSiege().checkIsAttacker(player.getClan()))
		{
			// Schedule respawn delay for attacker
			ThreadPool.schedule(new DeathTask(player), castle.getSiege().getAttackerRespawnDelay());
			player.sendMessage("You will be re-spawned in " + (castle.getSiege().getAttackerRespawnDelay() / 1000) + " seconds");
			return;
		}
		
		// Run immediately (no need to schedule)
		new DeathTask(player).run();
	}
	
	class DeathTask implements Runnable
	{
		PlayerInstance _player;
		
		DeathTask(PlayerInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if ((_player._inEventTvT && TvT.isStarted()) || (_player._inEventDM && DM.hasStarted()) || (_player._inEventCTF && CTF.isStarted()))
			{
				_player.sendMessage("You cannot restart while participating in an event!");
				return;
			}
			
			try
			{
				Location loc = null;
				Castle castle = null;
				Fort fort = null;
				if (_player.isInJail())
				{
					_requestedPointType = 27;
				}
				else if (_player.isFestivalParticipant())
				{
					_requestedPointType = 4;
				}
				
				if (_player.isPhoenixBlessed())
				{
					_player.stopPhoenixBlessing(null);
				}
				
				switch (_requestedPointType)
				{
					case 1: // to clanhall
					{
						if (_player.getClan() != null)
						{
							if (_player.getClan().getHasHideout() == 0)
							{
								// cheater
								_player.sendMessage("You may not use this respawn point!");
								Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
								return;
							}
							loc = MapRegionData.getInstance().getTeleToLocation(_player, TeleportWhereType.CLANHALL);
							if ((ClanHallManager.getInstance().getClanHallByOwner(_player.getClan()) != null) && (ClanHallManager.getInstance().getClanHallByOwner(_player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null))
							{
								_player.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(_player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
							}
							break;
						}
						loc = MapRegionData.getInstance().getTeleToLocation(_player, TeleportWhereType.TOWN);
						break;
					}
					case 2: // to castle
					{
						Boolean isInDefense = false;
						castle = CastleManager.getInstance().getCastle(_player);
						fort = FortManager.getInstance().getFort(_player);
						TeleportWhereType teleportWhere = TeleportWhereType.TOWN;
						if ((castle != null) && castle.getSiege().isInProgress() && castle.getSiege().checkIsDefender(_player.getClan()))
						{
							isInDefense = true;
						}
						if ((fort != null) && fort.getSiege().isInProgress() && fort.getSiege().checkIsDefender(_player.getClan()))
						{
							isInDefense = true;
						}
						if ((_player.getClan().getHasCastle() == 0) && (_player.getClan().getHasFort() == 0) && !isInDefense)
						{
							// cheater
							_player.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						if (CastleManager.getInstance().getCastleByOwner(_player.getClan()) != null)
						{
							teleportWhere = TeleportWhereType.CASTLE;
						}
						else if (FortManager.getInstance().getFortByOwner(_player.getClan()) != null)
						{
							teleportWhere = TeleportWhereType.FORTRESS;
						}
						loc = MapRegionData.getInstance().getTeleToLocation(_player, teleportWhere);
						break;
					}
					case 3: // to siege HQ
					{
						SiegeClan siegeClan = null;
						castle = CastleManager.getInstance().getCastle(_player);
						fort = FortManager.getInstance().getFort(_player);
						if ((castle != null) && castle.getSiege().isInProgress())
						{
							siegeClan = castle.getSiege().getAttackerClan(_player.getClan());
						}
						else if ((fort != null) && fort.getSiege().isInProgress())
						{
							siegeClan = fort.getSiege().getAttackerClan(_player.getClan());
						}
						if ((siegeClan == null) || siegeClan.getFlag().isEmpty())
						{
							// cheater
							_player.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = MapRegionData.getInstance().getTeleToLocation(_player, TeleportWhereType.SIEGEFLAG);
						break;
					}
					case 4: // Fixed or Player is a festival participant
					{
						if (!_player.isGM() && !_player.isFestivalParticipant())
						{
							// cheater
							_player.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = new Location(_player.getX(), _player.getY(), _player.getZ()); // spawn them where they died
						break;
					}
					case 27: // to jail
					{
						if (!_player.isInJail())
						{
							return;
						}
						loc = MapRegionData.JAIL_LOCATION;
						break;
					}
					default:
					{
						if ((_player.getKarma() > 0) && Config.ALT_KARMA_TELEPORT_TO_FLORAN)
						{
							loc = MapRegionData.FLORAN_VILLAGE_LOCATION; // Floran Village
							break;
						}
						loc = MapRegionData.getInstance().getTeleToLocation(_player, TeleportWhereType.TOWN);
						break;
					}
				}
				
				// Stand up and teleport, proof dvp video.
				_player.setIn7sDungeon(false);
				_player.setIsPendingRevive(true);
				_player.teleToLocation(loc, true);
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.toString());
			}
		}
	}
}