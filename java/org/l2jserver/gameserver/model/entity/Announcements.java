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
package org.l2jserver.gameserver.model.entity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.script.DateRange;

/**
 * @author ProGramMoS
 * @version 1.6
 */
public class Announcements
{
	private static final Logger LOGGER = Logger.getLogger(Announcements.class.getName());
	
	private final List<String> _announcements = new ArrayList<>();
	private final List<List<Object>> _eventAnnouncements = new ArrayList<>();
	
	public Announcements()
	{
		loadAnnouncements();
	}
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		final File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			LOGGER.warning("data/announcements.txt doesn't exist");
		}
	}
	
	public void showAnnouncements(PlayerInstance player)
	{
		for (String _announcement : _announcements)
		{
			player.sendPacket(new CreatureSay(0, ChatType.ANNOUNCEMENT, player.getName(), _announcement.replace("%name%", player.getName())));
		}
		
		for (List<Object> entry : _eventAnnouncements)
		{
			final DateRange validDateRange = (DateRange) entry.get(0);
			final String[] msg = (String[]) entry.get(1);
			final Date currentDate = new Date();
			if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				for (String element : msg)
				{
					sm.addString(element);
				}
				player.sendPacket(sm);
			}
		}
	}
	
	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		final List<Object> entry = new ArrayList<>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
	}
	
	public void listAnnouncements(PlayerInstance player)
	{
		final String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		final StringBuilder replyMSG = new StringBuilder("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>" + _announcements.get(i) + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}
		
		adminReply.replace("%announces%", replyMSG.toString());
		player.sendPacket(adminReply);
	}
	
	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}
	
	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}
	
	private void readFromDisk(File file)
	{
		LineNumberReader lnr = null;
		FileReader reader = null;
		try
		{
			int i = 0;
			String line = null;
			reader = new FileReader(file);
			lnr = new LineNumberReader(reader);
			
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					final String announcement = st.nextToken();
					_announcements.add(announcement);
					
					i++;
				}
			}
			LOGGER.info("Announcements: Loaded " + i + " Announcements.");
		}
		catch (IOException e1)
		{
			LOGGER.warning("Error reading announcements " + e1);
		}
		finally
		{
			if (lnr != null)
			{
				try
				{
					lnr.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning(e1.toString());
				}
			}
			
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning(e1.toString());
				}
			}
		}
	}
	
	private void saveToDisk()
	{
		final File file = new File("data/announcements.txt");
		FileWriter save = null;
		
		try
		{
			save = new FileWriter(file);
			for (String _announcement : _announcements)
			{
				save.write(_announcement);
				save.write("\r\n");
			}
			save.flush();
		}
		catch (IOException e)
		{
			LOGGER.warning("saving the announcements file has failed: " + e);
		}
		finally
		{
			if (save != null)
			{
				try
				{
					save.close();
				}
				catch (IOException e)
				{
					LOGGER.warning(e.toString());
				}
			}
		}
	}
	
	public void announceToAll(String text)
	{
		final CreatureSay cs = new CreatureSay(0, ChatType.ANNOUNCEMENT, "", text);
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
	}
	
	public void criticalAnnounceToAll(String text)
	{
		final CreatureSay cs = new CreatureSay(0, ChatType.CRITICAL_ANNOUNCE, null, text);
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(cs);
			}
		}
	}
	
	public void announceToAll(SystemMessage sm)
	{
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}
	
	// Method for handling announcements from admin
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			final String text = command.substring(lengthToTrim);
			getInstance().announceToAll(text);
		}
		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
		}
	}
	
	public static Announcements getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Announcements INSTANCE = new Announcements();
	}
}
