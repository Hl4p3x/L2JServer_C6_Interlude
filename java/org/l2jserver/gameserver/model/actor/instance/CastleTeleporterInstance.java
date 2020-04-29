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

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * The Class CastleTeleporterInstance.
 * @author Kerberos
 */
public class CastleTeleporterInstance extends NpcInstance
{
	public static final Logger LOGGER = Logger.getLogger(CastleTeleporterInstance.class.getName());
	
	private boolean _currentTask = false;
	
	/**
	 * Instantiates a new castle teleporter instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public CastleTeleporterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		if (actualCommand.equalsIgnoreCase("tele"))
		{
			int delay;
			if (!_currentTask)
			{
				if (getCastle().getSiege().isInProgress() && (getCastle().getSiege().getControlTowerCount() == 0))
				{
					delay = 480000;
				}
				else
				{
					delay = 30000;
				}
				
				setTask(true);
				ThreadPool.schedule(new oustAllPlayers(), delay);
			}
			
			final String filename = "data/html/castleteleporter/MassGK-1.htm";
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			player.sendPacket(html);
			return;
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(PlayerInstance player)
	{
		String filename;
		if (!_currentTask)
		{
			if (getCastle().getSiege().isInProgress() && (getCastle().getSiege().getControlTowerCount() == 0))
			{
				filename = "data/html/castleteleporter/MassGK-2.htm";
			}
			else
			{
				filename = "data/html/castleteleporter/MassGK.htm";
			}
		}
		else
		{
			filename = "data/html/castleteleporter/MassGK-1.htm";
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	void oustAllPlayers()
	{
		getCastle().oustAllPlayers();
	}
	
	class oustAllPlayers implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				oustAllPlayers();
				setTask(false);
			}
			catch (NullPointerException e)
			{
				LOGGER.warning("" + e.getMessage());
			}
		}
	}
	
	public boolean getTask()
	{
		return _currentTask;
	}
	
	public void setTask(boolean value)
	{
		_currentTask = value;
	}
}