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
package org.l2jserver.gameserver.model.entity.siege.clanhalls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.DecoInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.entity.siege.ClanHallSiege;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.zone.type.ClanHallZone;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.taskmanager.ExclusiveTask;

public class WildBeastFarmSiege extends ClanHallSiege
{
	protected static final Logger LOGGER = Logger.getLogger(WildBeastFarmSiege.class.getName());
	
	boolean _registrationPeriod = false;
	private int _clanCounter = 0;
	protected Map<Integer, clanPlayersInfo> _clansInfo = new HashMap<>();
	public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(63);
	protected clanPlayersInfo _ownerClanInfo = new clanPlayersInfo();
	protected boolean _finalStage = false;
	protected ScheduledFuture<?> _midTimer;
	
	private WildBeastFarmSiege()
	{
		LOGGER.info("SiegeManager of Wild Beasts Farm");
		final long siegeDate = restoreSiegeDate(63);
		final Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 63, 22);
		// Schedule siege auto start
		_startSiegeTask.schedule(1000);
	}
	
	public void startSiege()
	{
		setRegistrationPeriod(false);
		if (_clansInfo.size() == 0)
		{
			endSiege(false);
			return;
		}
		if ((_clansInfo.size() == 1) && (clanhall.getOwnerClan() == null))
		{
			endSiege(false);
			return;
		}
		if ((_clansInfo.size() == 1) && (clanhall.getOwnerClan() != null))
		{
			Clan clan = null;
			for (clanPlayersInfo a : _clansInfo.values())
			{
				clan = ClanTable.getInstance().getClanByName(a._clanName);
			}
			setInProgress(true);
			startSecondStep(clan);
			anonce("Take place at the siege of his headquarters.", 1);
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 30);
			_endSiegeTask.schedule(1000);
			return;
		}
		setInProgress(true);
		spawnFlags();
		gateControl(1);
		anonce("Take place at the siege of his headquarters.", 1);
		ThreadPool.schedule(new startFirstStep(), 5 * 60000);
		_midTimer = ThreadPool.schedule(new midSiegeStep(), 25 * 60000);
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 60);
		_endSiegeTask.schedule(1000);
	}
	
	public void startSecondStep(Clan winner)
	{
		final List<String> winPlayers = getInstance().getRegisteredPlayers(winner);
		unSpawnAll();
		_clansInfo.clear();
		final clanPlayersInfo regPlayers = new clanPlayersInfo();
		regPlayers._clanName = winner.getName();
		regPlayers._players = winPlayers;
		_clansInfo.put(winner.getClanId(), regPlayers);
		_clansInfo.put(clanhall.getOwnerClan().getClanId(), _ownerClanInfo);
		spawnFlags();
		gateControl(1);
		_finalStage = true;
		anonce("Take place at the siege of his headquarters.", 1);
		ThreadPool.schedule(new startFirstStep(), 5 * 60000);
	}
	
	public void endSiege(boolean par)
	{
		_mobControlTask.cancel();
		_finalStage = false;
		if (par)
		{
			final Clan winner = checkHaveWinner();
			if (winner != null)
			{
				ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
				anonce("Attention! Clan hall, farm beasts was conquered by the clan " + winner.getName(), 2);
			}
			else
			{
				anonce("Attention! Clan hall, farm wild animals did not get new owner", 2);
			}
		}
		setInProgress(false);
		unSpawnAll();
		_clansInfo.clear();
		_clanCounter = 0;
		teleportPlayers();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(), 63, 22);
		_startSiegeTask.schedule(1000);
	}
	
	public void unSpawnAll()
	{
		for (String clanName : getRegisteredClans())
		{
			final Clan clan = ClanTable.getInstance().getClanByName(clanName);
			final MonsterInstance mob = getQuestMob(clan);
			final DecoInstance flag = getSiegeFlag(clan);
			if (mob != null)
			{
				mob.deleteMe();
			}
			if (flag != null)
			{
				flag.deleteMe();
			}
		}
	}
	
	public void gateControl(int value)
	{
		if (value == 1)
		{
			DoorData.getInstance().getDoor(21150003).openMe();
			DoorData.getInstance().getDoor(21150004).openMe();
			DoorData.getInstance().getDoor(21150001).closeMe();
			DoorData.getInstance().getDoor(21150002).closeMe();
		}
		else if (value == 2)
		{
			DoorData.getInstance().getDoor(21150001).closeMe();
			DoorData.getInstance().getDoor(21150002).closeMe();
			DoorData.getInstance().getDoor(21150003).closeMe();
			DoorData.getInstance().getDoor(21150004).closeMe();
		}
	}
	
	public void teleportPlayers()
	{
		final ClanHallZone zone = clanhall.getZone();
		for (Creature creature : zone.getCharactersInside())
		{
			if (creature instanceof PlayerInstance)
			{
				final Clan clan = ((PlayerInstance) creature).getClan();
				if (!isPlayerRegister(clan, creature.getName()))
				{
					creature.teleToLocation(53468, -94092, -1634);
				}
			}
		}
	}
	
	public Clan checkHaveWinner()
	{
		Clan res = null;
		int questMobCount = 0;
		for (String clanName : getRegisteredClans())
		{
			final Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (getQuestMob(clan) != null)
			{
				res = clan;
				questMobCount++;
			}
		}
		if (questMobCount > 1)
		{
			return null;
		}
		return res;
	}
	
	protected class midSiegeStep implements Runnable
	{
		@Override
		public void run()
		{
			_mobControlTask.cancel();
			final Clan winner = checkHaveWinner();
			if (winner != null)
			{
				if (clanhall.getOwnerClan() == null)
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
					anonce("Attention! Hall clan Fkrma wild animals was conquered by the clan " + winner.getName(), 2);
					endSiege(false);
				}
				else
				{
					startSecondStep(winner);
				}
			}
			else
			{
				endSiege(true);
			}
		}
	}
	
	protected class startFirstStep implements Runnable
	{
		@Override
		public void run()
		{
			teleportPlayers();
			gateControl(2);
			int mobCounter = 1;
			for (String clanName : getRegisteredClans())
			{
				NpcTemplate template;
				final Clan clan = ClanTable.getInstance().getClanByName(clanName);
				template = NpcTable.getInstance().getTemplate(35617 + mobCounter);
				final MonsterInstance questMob = new MonsterInstance(IdFactory.getNextId(), template);
				questMob.setHeading(100);
				questMob.getStatus().setCurrentHpMp(questMob.getMaxHp(), questMob.getMaxMp());
				if (mobCounter == 1)
				{
					questMob.spawnMe(57069, -91797, -1360);
				}
				else if (mobCounter == 2)
				{
					questMob.spawnMe(58838, -92232, -1354);
				}
				else if (mobCounter == 3)
				{
					questMob.spawnMe(57327, -93373, -1365);
				}
				else if (mobCounter == 4)
				{
					questMob.spawnMe(57820, -91740, -1354);
				}
				else if (mobCounter == 5)
				{
					questMob.spawnMe(58728, -93487, -1360);
				}
				final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._mob = questMob;
				mobCounter++;
			}
			_mobControlTask.schedule(3000);
			anonce("The battle began. Kill the enemy NPC", 1);
		}
	}
	
	public void spawnFlags()
	{
		int flagCounter = 1;
		for (String clanName : getRegisteredClans())
		{
			NpcTemplate template;
			final Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (clan == clanhall.getOwnerClan())
			{
				template = NpcTable.getInstance().getTemplate(35422);
			}
			else
			{
				template = NpcTable.getInstance().getTemplate(35422 + flagCounter);
			}
			final DecoInstance flag = new DecoInstance(IdFactory.getNextId(), template);
			flag.setTitle(clan.getName());
			flag.setHeading(100);
			flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			if (clan == clanhall.getOwnerClan())
			{
				flag.spawnMe(58782, -93180, -1354);
				final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._flag = flag;
				continue;
			}
			
			if (flagCounter == 1)
			{
				flag.spawnMe(56769, -92097, -1360);
			}
			else if (flagCounter == 2)
			{
				flag.spawnMe(59138, -92532, -1354);
			}
			else if (flagCounter == 3)
			{
				flag.spawnMe(57027, -93673, -1365);
			}
			else if (flagCounter == 4)
			{
				flag.spawnMe(58120, -91440, -1354);
			}
			else if (flagCounter == 5)
			{
				flag.spawnMe(58428, -93787, -1360);
			}
			final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
			regPlayers._flag = flag;
			flagCounter++;
		}
	}
	
	public void setRegistrationPeriod(boolean par)
	{
		_registrationPeriod = par;
	}
	
	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}
	
	public boolean isPlayerRegister(Clan playerClan, String playerName)
	{
		if (playerClan == null)
		{
			return false;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		return (regPlayers != null) && regPlayers._players.contains(playerName);
	}
	
	public boolean isClanOnSiege(Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
		{
			return true;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		return regPlayers != null;
	}
	
	public synchronized int registerClanOnSiege(PlayerInstance player, Clan playerClan)
	{
		if (_clanCounter == 5)
		{
			return 2;
		}
		final ItemInstance item = player.getInventory().getItemByItemId(8293);
		if ((item != null) && player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false))
		{
			_clanCounter++;
			clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
			if (regPlayers == null)
			{
				regPlayers = new clanPlayersInfo();
				regPlayers._clanName = playerClan.getName();
				_clansInfo.put(playerClan.getClanId(), regPlayers);
			}
		}
		else
		{
			return 1;
		}
		return 0;
	}
	
	public boolean unRegisterClan(Clan playerClan)
	{
		if (_clansInfo.remove(playerClan.getClanId()) != null)
		{
			_clanCounter--;
			return true;
		}
		return false;
	}
	
	public List<String> getRegisteredClans()
	{
		final List<String> clans = new ArrayList<>();
		for (clanPlayersInfo a : _clansInfo.values())
		{
			clans.add(a._clanName);
		}
		return clans;
	}
	
	public List<String> getRegisteredPlayers(Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
		{
			return _ownerClanInfo._players;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
		{
			return regPlayers._players;
		}
		return null;
	}
	
	public DecoInstance getSiegeFlag(Clan playerClan)
	{
		final clanPlayersInfo clanInfo = _clansInfo.get(playerClan.getClanId());
		if (clanInfo != null)
		{
			return clanInfo._flag;
		}
		return null;
	}
	
	public MonsterInstance getQuestMob(Clan clan)
	{
		final clanPlayersInfo clanInfo = _clansInfo.get(clan.getClanId());
		if (clanInfo != null)
		{
			return clanInfo._mob;
		}
		return null;
	}
	
	public int getPlayersCount(String playerClan)
	{
		for (clanPlayersInfo a : _clansInfo.values())
		{
			if (a._clanName.equalsIgnoreCase(playerClan))
			{
				return a._players.size();
			}
		}
		return 0;
	}
	
	public void addPlayer(Clan playerClan, String playerName)
	{
		if ((playerClan == clanhall.getOwnerClan()) && (_ownerClanInfo._players.size() < 18) && !_ownerClanInfo._players.contains(playerName))
		{
			_ownerClanInfo._players.add(playerName);
			return;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if ((regPlayers != null) && (regPlayers._players.size() < 18) && !regPlayers._players.contains(playerName))
		{
			regPlayers._players.add(playerName);
		}
	}
	
	public void removePlayer(Clan playerClan, String playerName)
	{
		if ((playerClan == clanhall.getOwnerClan()) && _ownerClanInfo._players.contains(playerName))
		{
			_ownerClanInfo._players.remove(playerName);
			return;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if ((regPlayers != null) && regPlayers._players.contains(playerName))
		{
			regPlayers._players.remove(playerName);
		}
	}
	
	private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (isInProgress())
			{
				cancel();
				return;
			}
			final Calendar siegeStart = Calendar.getInstance();
			siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
			final long registerTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			siegeStart.add(Calendar.HOUR, 1);
			final long siegeTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			long remaining = registerTimeRemaining;
			if ((registerTimeRemaining <= 0) && !_registrationPeriod)
			{
				if (clanhall.getOwnerClan() != null)
				{
					_ownerClanInfo._clanName = clanhall.getOwnerClan().getName();
				}
				else
				{
					_ownerClanInfo._clanName = "";
				}
				setRegistrationPeriod(true);
				anonce("Attention! The period of registration at the siege clan hall, farm wild animals.", 2);
				remaining = siegeTimeRemaining;
			}
			if (siegeTimeRemaining <= 0)
			{
				startSiege();
				cancel();
				return;
			}
			schedule(remaining);
		}
	};
	
	public void anonce(String text, int type)
	{
		if (type == 1)
		{
			final CreatureSay cs = new CreatureSay(0, ChatType.SHOUT, "Journal", text);
			for (String clanName : getRegisteredClans())
			{
				final Clan clan = ClanTable.getInstance().getClanByName(clanName);
				for (String playerName : getRegisteredPlayers(clan))
				{
					final PlayerInstance cha = World.getInstance().getPlayer(playerName);
					if (cha != null)
					{
						cha.sendPacket(cs);
					}
				}
			}
		}
		else
		{
			final CreatureSay cs = new CreatureSay(0, ChatType.SHOUT, "Journal", text);
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				if (player.getInstanceId() == 0)
				{
					player.sendPacket(cs);
				}
			}
		}
	}
	
	protected final ExclusiveTask _endSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (!isInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(true);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};
	protected final ExclusiveTask _mobControlTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			int mobCount = 0;
			for (clanPlayersInfo cl : _clansInfo.values())
			{
				if (cl._mob.isDead())
				{
					final Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
					unRegisterClan(clan);
				}
				else
				{
					mobCount++;
				}
			}
			teleportPlayers();
			if (mobCount < 2)
			{
				if (_finalStage)
				{
					_siegeEndDate = Calendar.getInstance();
					_endSiegeTask.cancel();
					_endSiegeTask.schedule(5000);
				}
				else
				{
					_midTimer.cancel(false);
					ThreadPool.schedule(new midSiegeStep(), 5000);
				}
			}
			else
			{
				schedule(3000);
			}
		}
	};
	
	protected class clanPlayersInfo
	{
		public String _clanName;
		public DecoInstance _flag = null;
		public MonsterInstance _mob = null;
		public List<String> _players = new ArrayList<>();
	}
	
	public static WildBeastFarmSiege getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final WildBeastFarmSiege INSTANCE = new WildBeastFarmSiege();
	}
}
