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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.spawn.Spawn;

public class FortressOfResistance
{
	private static final Logger LOGGER = Logger.getLogger(FortressOfResistance.class.getName());
	
	private final Map<Integer, DamageInfo> _clansDamageInfo;
	
	private static int START_DAY = 1;
	private static int HOUR = Config.PARTISAN_HOUR;
	private static int MINUTES = Config.PARTISAN_MINUTES;
	
	private static final int BOSS_ID = 35368;
	private static final int MESSENGER_ID = 35382;
	
	private ScheduledFuture<?> _nurka;
	private ScheduledFuture<?> _announce;
	
	private final Calendar _capturetime = Calendar.getInstance();
	
	protected class DamageInfo
	{
		public Clan _clan;
		public long _damage;
	}
	
	private FortressOfResistance()
	{
		if (Config.PARTISAN_DAY == 1)
		{
			START_DAY = Calendar.MONDAY;
		}
		else if (Config.PARTISAN_DAY == 2)
		{
			START_DAY = Calendar.TUESDAY;
		}
		else if (Config.PARTISAN_DAY == 3)
		{
			START_DAY = Calendar.WEDNESDAY;
		}
		else if (Config.PARTISAN_DAY == 4)
		{
			START_DAY = Calendar.THURSDAY;
		}
		else if (Config.PARTISAN_DAY == 5)
		{
			START_DAY = Calendar.FRIDAY;
		}
		else if (Config.PARTISAN_DAY == 6)
		{
			START_DAY = Calendar.SATURDAY;
		}
		else if (Config.PARTISAN_DAY == 7)
		{
			START_DAY = Calendar.SUNDAY;
		}
		else
		{
			START_DAY = Calendar.FRIDAY;
		}
		
		if ((HOUR < 0) || (HOUR > 23))
		{
			HOUR = 21;
		}
		if ((MINUTES < 0) || (MINUTES > 59))
		{
			MINUTES = 0;
		}
		
		_clansDamageInfo = new HashMap<>();
		
		synchronized (this)
		{
			setCalendarForNextCaprture();
			final long milliToCapture = getMilliToCapture();
			final RunMessengerSpawn rms = new RunMessengerSpawn();
			ThreadPool.schedule(rms, milliToCapture);
			
			final long total_millis = System.currentTimeMillis() + milliToCapture;
			final GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
			cal.setTimeInMillis(total_millis);
			final String next_ch_siege_date = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(cal.getTimeInMillis());
			LOGGER.info("Fortress of Resistanse: siege will start the " + next_ch_siege_date);
		}
	}
	
	private void setCalendarForNextCaprture()
	{
		int daysToChange = getDaysToCapture();
		if (daysToChange == 7)
		{
			if (_capturetime.get(Calendar.HOUR_OF_DAY) < HOUR)
			{
				daysToChange = 0;
			}
			else if ((_capturetime.get(Calendar.HOUR_OF_DAY) == HOUR) && (_capturetime.get(Calendar.MINUTE) < MINUTES))
			{
				daysToChange = 0;
			}
		}
		
		if (daysToChange > 0)
		{
			_capturetime.add(Calendar.DATE, daysToChange);
		}
		
		_capturetime.set(Calendar.HOUR_OF_DAY, HOUR);
		_capturetime.set(Calendar.MINUTE, MINUTES);
	}
	
	private int getDaysToCapture()
	{
		final int numDays = _capturetime.get(Calendar.DAY_OF_WEEK) - START_DAY;
		if (numDays < 0)
		{
			return 0 - numDays;
		}
		return 7 - numDays;
	}
	
	private long getMilliToCapture()
	{
		final long currTimeMillis = System.currentTimeMillis();
		final long captureTimeMillis = _capturetime.getTimeInMillis();
		return captureTimeMillis - currTimeMillis;
	}
	
	protected class RunMessengerSpawn implements Runnable
	{
		@Override
		public void run()
		{
			MessengerSpawn();
		}
	}
	
	public void MessengerSpawn()
	{
		if (!ClanHallManager.getInstance().isFree(21))
		{
			ClanHallManager.getInstance().setFree(21);
		}
		
		Announce("Capture registration of Partisan Hideout has begun!");
		Announce("Now its open for 1 hours!");
		NpcInstance result = null;
		try
		{
			final NpcTemplate template = NpcTable.getInstance().getTemplate(MESSENGER_ID);
			final Spawn spawn = new Spawn(template);
			spawn.setX(50335);
			spawn.setY(111275);
			spawn.setZ(-1970);
			spawn.stopRespawn();
			result = spawn.doSpawn();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
		final RunBossSpawn rbs = new RunBossSpawn();
		ThreadPool.schedule(rbs, 3600000); // 60 * 60 * 1000
		LOGGER.info("Fortress of Resistanse: Messenger spawned!");
		ThreadPool.schedule(new DeSpawnTimer(result), 3600000); // 60 * 60 * 1000
	}
	
	protected class RunBossSpawn implements Runnable
	{
		@Override
		public void run()
		{
			BossSpawn();
		}
	}
	
	public void BossSpawn()
	{
		if (!_clansDamageInfo.isEmpty())
		{
			_clansDamageInfo.clear();
		}
		
		NpcInstance result = null;
		try
		{
			final NpcTemplate template = NpcTable.getInstance().getTemplate(BOSS_ID);
			final Spawn spawn = new Spawn(template);
			spawn.setX(44525);
			spawn.setY(108867);
			spawn.setZ(-2020);
			spawn.stopRespawn();
			result = spawn.doSpawn();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
		
		LOGGER.info("Fortress of Resistanse: Boss spawned!");
		Announce("Capture of Partisan Hideout has begun!");
		Announce("You have one hour to kill Nurka!");
		_nurka = ThreadPool.schedule(new DeSpawnTimer(result), 3600000); // 60 * 60 * 1000
		_announce = ThreadPool.schedule(new AnnounceInfo("No one can`t kill Nurka! Partisan Hideout set free until next week!"), 3600000);
	}
	
	protected class DeSpawnTimer implements Runnable
	{
		NpcInstance _npc = null;
		
		public DeSpawnTimer(NpcInstance npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			_npc.onDecay();
		}
	}
	
	public boolean Conditions(PlayerInstance player)
	{
		return (player != null) && (player.getClan() != null) && player.isClanLeader() && (player.getClan().getAuctionBiddedAt() <= 0) && (ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) == null) && (player.getClan().getLevel() > 2);
	}
	
	protected class AnnounceInfo implements Runnable
	{
		String _message;
		
		public AnnounceInfo(String message)
		{
			_message = message;
		}
		
		@Override
		public void run()
		{
			Announce(_message);
		}
	}
	
	public void Announce(String message)
	{
		Announcements.getInstance().announceToAll(message);
	}
	
	public void CaptureFinish()
	{
		Clan clanIdMaxDamage = null;
		long tempMaxDamage = 0;
		for (DamageInfo damageInfo : _clansDamageInfo.values())
		{
			if ((damageInfo != null) && (damageInfo._damage > tempMaxDamage))
			{
				tempMaxDamage = damageInfo._damage;
				clanIdMaxDamage = damageInfo._clan;
			}
		}
		if (clanIdMaxDamage != null)
		{
			ClanHallManager.getInstance().setOwner(21, clanIdMaxDamage);
			clanIdMaxDamage.setReputationScore(clanIdMaxDamage.getReputationScore() + 600, true);
			update();
			
			Announce("Capture of Partisan Hideout is over.");
			Announce("Now its belong to: '" + clanIdMaxDamage.getName() + "' until next capture.");
		}
		else
		{
			Announce("Capture of Partisan Hideout is over.");
			Announce("No one can`t capture Partisan Hideout.");
		}
		
		_nurka.cancel(true);
		_announce.cancel(true);
	}
	
	public void addSiegeDamage(Clan clan, long damage)
	{
		DamageInfo clanDamage = _clansDamageInfo.get(clan.getClanId());
		if (clanDamage != null)
		{
			clanDamage._damage += damage;
		}
		else
		{
			clanDamage = new DamageInfo();
			clanDamage._clan = clan;
			clanDamage._damage += damage;
			_clansDamageInfo.put(clan.getClanId(), clanDamage);
		}
	}
	
	private void update()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE clanhall SET paidUntil=?, paid=? WHERE id=?");
			statement.setLong(1, System.currentTimeMillis() + 59760000);
			statement.setInt(2, 1);
			statement.setInt(3, 21);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	public static FortressOfResistance getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FortressOfResistance INSTANCE = new FortressOfResistance();
	}
}
