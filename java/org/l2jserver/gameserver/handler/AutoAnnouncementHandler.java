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
package org.l2jserver.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Auto Announcment Handler Automatically send announcment at a set time interval.
 * @author chief
 */
public class AutoAnnouncementHandler
{
	protected static final Logger LOGGER = Logger.getLogger(AutoAnnouncementHandler.class.getName());
	
	private static final long DEFAULT_ANNOUNCEMENT_DELAY = 180000; // 3 mins by default
	protected Map<Integer, AutoAnnouncementInstance> _registeredAnnouncements;
	
	protected AutoAnnouncementHandler()
	{
		_registeredAnnouncements = new HashMap<>();
		restoreAnnouncementData();
	}
	
	private void restoreAnnouncementData()
	{
		int numLoaded = 0;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement("SELECT * FROM auto_announcements ORDER BY id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				numLoaded++;
				registerGlobalAnnouncement(rs.getInt("id"), rs.getString("announcement"), rs.getLong("delay"));
			}
			
			statement.close();
			rs.close();
			
			LOGGER.info("GameServer: Loaded " + numLoaded + " Auto Announcements.");
		}
		catch (Exception e)
		{
			LOGGER.info("Problem with AutoAnnouncementHandler: " + e.getMessage());
		}
	}
	
	/**
	 * @param player
	 */
	public void listAutoAnnouncements(PlayerInstance player)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40></td>");
		replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br>");
		replyMSG.append("<td width=180><center>Auto Announcement Menu</center></td>");
		replyMSG.append("<td width=40></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Add new auto announcement:</center>");
		replyMSG.append("<center><multiedit var=\"new_autoannouncement\" width=240 height=30></center><br>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Delay: <edit var=\"delay\" width=70></center>");
		replyMSG.append("<center>Note: Time in Seconds 60s = 1 min.</center>");
		replyMSG.append("<center>Note2: Minimum Time is 30 Seconds.</center>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_autoannouncement $delay $new_autoannouncement\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td>");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("<br>");
		
		for (AutoAnnouncementInstance announcementInst : getInstance().values())
		{
			replyMSG.append("<table width=260><tr><td width=220>[" + announcementInst.getDefaultDelay() + "s] " + announcementInst.getDefaultTexts() + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_autoannouncement " + announcementInst.getDefaultId() + "\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		player.sendPacket(adminReply);
	}
	
	/**
	 * @return
	 */
	public int size()
	{
		return _registeredAnnouncements.size();
	}
	
	/**
	 * Registers a globally active autoannouncement.<br>
	 * Returns the associated auto announcement instance.
	 * @param id
	 * @param announcementTexts
	 * @param announcementDelay announcementDelay (-1 = default delay)
	 * @return AutoAnnouncementInstance announcementInst
	 */
	public AutoAnnouncementInstance registerGlobalAnnouncement(int id, String announcementTexts, long announcementDelay)
	{
		return registerAnnouncement(id, announcementTexts, announcementDelay);
	}
	
	/**
	 * Registers a NON globally-active auto announcement<br>
	 * Returns the associated auto chat instance.
	 * @param id
	 * @param announcementTexts
	 * @param announcementDelay announcementDelay (-1 = default delay)
	 * @return AutoAnnouncementInstance announcementInst
	 */
	public AutoAnnouncementInstance registerAnnouncment(int id, String announcementTexts, long announcementDelay)
	{
		return registerAnnouncement(id, announcementTexts, announcementDelay);
	}
	
	public AutoAnnouncementInstance registerAnnouncment(String announcementTexts, long announcementDelay)
	{
		final int nextId = nextAutoAnnouncmentId();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO auto_announcements (id,announcement,delay) VALUES (?,?,?)");
			statement.setInt(1, nextId);
			statement.setString(2, announcementTexts);
			statement.setLong(3, announcementDelay);
			statement.executeUpdate();
			
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("System: Could Not Insert Auto Announcment into DataBase: Reason: Duplicate Id");
		}
		return registerAnnouncement(nextId, announcementTexts, announcementDelay);
	}
	
	public int nextAutoAnnouncmentId()
	{
		int nextId = 0;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			statement = con.prepareStatement("SELECT id FROM auto_announcements ORDER BY id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				if (rs.getInt("id") > nextId)
				{
					nextId = rs.getInt("id");
				}
			}
			
			statement.close();
			rs.close();
			
			nextId++;
		}
		catch (Exception e)
		{
			LOGGER.info("Problem with AutoAnnouncementHandler: " + e.getMessage());
		}
		return nextId;
	}
	
	/**
	 * @param id
	 * @param announcementTexts
	 * @param chatDelay
	 * @return
	 */
	private final AutoAnnouncementInstance registerAnnouncement(int id, String announcementTexts, long chatDelay)
	{
		AutoAnnouncementInstance announcementInst = null;
		if (chatDelay < 0)
		{
			chatDelay = DEFAULT_ANNOUNCEMENT_DELAY;
		}
		
		if (_registeredAnnouncements.containsKey(id))
		{
			announcementInst = _registeredAnnouncements.get(id);
		}
		else
		{
			announcementInst = new AutoAnnouncementInstance(id, announcementTexts, chatDelay);
		}
		
		_registeredAnnouncements.put(id, announcementInst);
		
		return announcementInst;
	}
	
	/**
	 * @return
	 */
	public Collection<AutoAnnouncementInstance> values()
	{
		return _registeredAnnouncements.values();
	}
	
	/**
	 * Removes and cancels ALL auto announcement for the given announcement id.
	 * @param id
	 * @return boolean removedSuccessfully
	 */
	public boolean removeAnnouncement(int id)
	{
		final AutoAnnouncementInstance announcementInst = _registeredAnnouncements.get(id);
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM auto_announcements WHERE id=?");
			statement.setInt(1, announcementInst.getDefaultId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not Delete Auto Announcement in Database, Reason: " + e);
		}
		return removeAnnouncement(announcementInst);
	}
	
	/**
	 * Removes and cancels ALL auto announcement for the given announcement instance.
	 * @param announcementInst
	 * @return boolean removedSuccessfully
	 */
	public boolean removeAnnouncement(AutoAnnouncementInstance announcementInst)
	{
		if (announcementInst == null)
		{
			return false;
		}
		
		_registeredAnnouncements.remove(announcementInst.getDefaultId());
		announcementInst.setActive(false);
		
		return true;
	}
	
	/**
	 * Returns the associated auto announcement instance either by the given announcement ID or object ID.
	 * @param id
	 * @return AutoAnnouncementInstance announcementInst
	 */
	public AutoAnnouncementInstance getAutoAnnouncementInstance(int id)
	{
		return _registeredAnnouncements.get(id);
	}
	
	/**
	 * Sets the active state of all auto announcement instances to that specified, and cancels the scheduled chat task if necessary.
	 * @param isActive
	 */
	public void setAutoAnnouncementActive(boolean isActive)
	{
		for (AutoAnnouncementInstance announcementInst : _registeredAnnouncements.values())
		{
			announcementInst.setActive(isActive);
		}
	}
	
	/**
	 * Auto Announcement Instance
	 */
	public class AutoAnnouncementInstance
	{
		private long _defaultDelay = DEFAULT_ANNOUNCEMENT_DELAY;
		private String _defaultTexts;
		private boolean _defaultRandom = false;
		private final Integer _defaultId;
		
		private boolean _isActive;
		
		public ScheduledFuture<?> _chatTask;
		
		/**
		 * @param id
		 * @param announcementTexts
		 * @param announcementDelay
		 */
		protected AutoAnnouncementInstance(int id, String announcementTexts, long announcementDelay)
		{
			_defaultId = id;
			_defaultTexts = announcementTexts;
			_defaultDelay = announcementDelay * 1000;
			setActive(true);
		}
		
		/**
		 * @return
		 */
		public boolean isActive()
		{
			return _isActive;
		}
		
		/**
		 * @return
		 */
		public boolean isDefaultRandom()
		{
			return _defaultRandom;
		}
		
		/**
		 * @return
		 */
		public long getDefaultDelay()
		{
			return _defaultDelay;
		}
		
		/**
		 * @return
		 */
		public String getDefaultTexts()
		{
			return _defaultTexts;
		}
		
		/**
		 * @return
		 */
		public Integer getDefaultId()
		{
			return _defaultId;
		}
		
		/**
		 * @param delayValue
		 */
		public void setDefaultChatDelay(long delayValue)
		{
			_defaultDelay = delayValue;
		}
		
		/**
		 * @param textsValue
		 */
		public void setDefaultChatTexts(String textsValue)
		{
			_defaultTexts = textsValue;
		}
		
		/**
		 * @param randValue
		 */
		public void setDefaultRandom(boolean randValue)
		{
			_defaultRandom = randValue;
		}
		
		/**
		 * @param activeValue
		 */
		public void setActive(boolean activeValue)
		{
			if (_isActive == activeValue)
			{
				return;
			}
			
			_isActive = activeValue;
			if (_isActive)
			{
				final AutoAnnouncementRunner acr = new AutoAnnouncementRunner(_defaultId);
				_chatTask = ThreadPool.scheduleAtFixedRate(acr, _defaultDelay, _defaultDelay);
			}
			else
			{
				_chatTask.cancel(false);
			}
		}
		
		/**
		 * Auto Announcement Runner<br>
		 * <br>
		 * Represents the auto announcement scheduled task for each announcement instance.
		 * @author chief
		 */
		private class AutoAnnouncementRunner implements Runnable
		{
			protected int id;
			
			protected AutoAnnouncementRunner(int pId)
			{
				id = pId;
			}
			
			@Override
			public synchronized void run()
			{
				final AutoAnnouncementInstance announcementInst = _registeredAnnouncements.get(id);
				String text;
				text = announcementInst.getDefaultTexts();
				if (text == null)
				{
					return;
				}
				
				Announcements.getInstance().announceToAll(text);
			}
		}
	}
	
	public static AutoAnnouncementHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoAnnouncementHandler INSTANCE = new AutoAnnouncementHandler();
	}
}
