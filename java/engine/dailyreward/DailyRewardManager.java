package engine.dailyreward;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;

/**
 * @author An4rch / Hl4p3x
 */
public class DailyRewardManager
{
	private static int dailyRewardId = Config.DAILY_REWARD_ITEM_ID;
	private final static Logger LOG = LoggerFactory.getLogger(DailyRewardManager.class);
	
	protected DailyRewardManager()
	{
		loadSystemThread();
		LOG.info("Daily Reward Manager: Loaded");
	}
	
	protected static void loadSystemThread()
	{
		long spawnMillis = 0;
		
		Calendar c = Calendar.getInstance();
		
		String[] time = "24:00".split(":");
		c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
		c.set(Calendar.MINUTE, Integer.parseInt(time[1]));
		c.set(Calendar.SECOND, 0);
		spawnMillis = c.getTimeInMillis() - System.currentTimeMillis();
		
		ThreadPool.schedule(() ->
		{
			clearDBTable();
			loadSystemThread();
			System.out.println("[Daily Reward] Table cleaned and restart the thread.");
		}, spawnMillis);
	}
	
	protected static void clearDBTable()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM reward_manager");
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void initialAllEngines(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		if (player.getLevel() < Config.MIN_LEVEL_TO_DAILYREWARD)
		{
			player.sendMessage("You need to be at least level " + Config.MIN_LEVEL_TO_DAILYREWARD + " to use receive daily reward.");
			return;
		}
		
		if (checkIfIPorHWIDExistInDB(player, "ip"))
		{
			if (DailyRewardManager.checkForLatestHWIDReward(player, "ip")) // Rewarded
			{
				if (DailyRewardManager.checkForLatestHWIDReward(player, "ip"))
				{
					player.sendMessage("DAILY REWARD: Sorry, " + player.getName() + "! Join again in " + Cd(player, "ip", false) + " to get your daily reward again.");
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				}
				return;
			}
			openDailyRewardHtml(player);
		}
		else // If not exist in Database
		{
			openDailyRewardHtml(player);
		}
	}
	
	public void claimDailyReward(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		if (player.getLevel() < Config.MIN_LEVEL_TO_DAILYREWARD)
		{
			player.sendMessage("You need to be at least level " + Config.MIN_LEVEL_TO_DAILYREWARD + " to use receive daily reward.");
			return;
		}
		
		if (checkIfIPorHWIDExistInDB(player, "ip"))
		{
			if (DailyRewardManager.checkForLatestHWIDReward(player, "ip")) // Rewarded
			{
				if (DailyRewardManager.checkForLatestHWIDReward(player, "ip"))
				{
					player.sendMessage("DAILY REWARD: Dear, " + player.getName() + "! Join again in " + Cd(player, "ip", false) + " to get your reward.");
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
				}
				return;
			}
			if (checkIfIPorHWIDExistInDB(player, "ip"))
			{
				updateLastReward(player, "ip");
			}
			
			giveReward(player);
		}
		else
		{
			insertNewParentOfPlayerIPHWID(player);
			giveReward(player);
		}
	}
	
	private static void openDailyRewardHtml(PlayerInstance player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		htm.setFile("data/html/engine/dailyreward/index.htm");
		htm.replace("", "");
		player.sendPacket(htm);
	}
	
	/**
	 * @param player
	 */
	private static void giveReward(PlayerInstance player)
	{
		player.addItem("HWIDRewardManager", dailyRewardId, Config.DAILY_REWARD_AMOUNT, null, true);
		player.sendPacket(new PlaySound("ItemSound3.siege_victory"));
	}
	
	/**
	 * TODO Need implement for method detection (HWID(HDD|MAC|CPU))
	 * @param player
	 * @param mode
	 * @return
	 */
	@SuppressWarnings("unused")
	private static boolean checkForLatestHWIDReward(PlayerInstance player, String mode)
	{
		if ((Config.REWARD_PER_TIME_MODE_RESTRICTION == 1) && mode.equals("ip"))
		{
			return false;
		}
		
		return Long.parseLong(Cd(player, mode, true)) > System.currentTimeMillis();
	}
	
	private static void updateLastReward(PlayerInstance player, String mode)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE reward_manager SET expire_time=? WHERE " + mode + "=?");
			statement.setLong(1, System.currentTimeMillis());
			statement.setString(2, (mode.equals("ip") ? player.getIP() : player.getHWID()));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static String Cd(PlayerInstance player, String mode, boolean returnInTimestamp)
	{
		long CdMs = 0;
		long voteDelay = 1440 * 60000L;
		
		PreparedStatement statement = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement("SELECT expire_time FROM reward_manager WHERE " + mode + "=?");
			statement.setString(1, (mode.equals("ip") ? player.getIP() : player.getHWID()));
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				CdMs = rset.getLong("expire_time");
			}
			
			if ((CdMs + voteDelay) < System.currentTimeMillis())
			{
				CdMs = System.currentTimeMillis() - voteDelay;
			}
			
			rset.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
				{
					statement.close();
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
		
		if (returnInTimestamp)
		{
			return String.valueOf(CdMs + voteDelay);
		}
		
		Date resultdate = new Date(CdMs + voteDelay);
		return sdf.format(resultdate);
	}
	
	private static boolean checkIfIPorHWIDExistInDB(PlayerInstance player, String mode)
	{
		boolean flag = false;
		PreparedStatement statement = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement("SELECT * FROM reward_manager WHERE " + mode + "=?");
			statement.setString(1, (mode.equals("ip") ? player.getIP() : player.getHWID()));
			
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
			{
				flag = true;
			}
			
			rset.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
				{
					statement.close();
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
		return flag;
	}
	
	private static void insertNewParentOfPlayerIPHWID(PlayerInstance player)
	{
		PreparedStatement statement = null;
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement("INSERT INTO reward_manager (ip,hwid,expire_time) VALUES (?,?,?)");
			statement.setString(1, player.getIP());
			statement.setString(2, player.getHWID());
			statement.setLong(3, System.currentTimeMillis());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (statement != null)
				{
					statement.close();
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static DailyRewardManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DailyRewardManager _instance = new DailyRewardManager();
	}
}