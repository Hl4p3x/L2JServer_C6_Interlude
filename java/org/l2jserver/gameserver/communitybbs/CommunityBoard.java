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
package org.l2jserver.gameserver.communitybbs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.Config;
import org.l2jserver.gameserver.communitybbs.Manager.BaseBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.ClanBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.FavoriteBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.FriendsBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.MailBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.PostBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.RegionBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.TopBBSManager;
import org.l2jserver.gameserver.communitybbs.Manager.TopicBBSManager;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.GameClient;
import org.l2jserver.gameserver.network.SystemMessageId;

public class CommunityBoard
{
	/** The bypasses used by the players. */
	private final Map<Integer, String> _bypasses = new ConcurrentHashMap<>();
	
	protected CommunityBoard()
	{
	}
	
	public static CommunityBoard getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public void handleCommands(GameClient client, String command)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!Config.ENABLE_COMMUNITY_BOARD)
		{
			player.sendPacket(SystemMessageId.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}
		
		if (command.startsWith("_bbshome"))
		{
			TopBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_bbsloc"))
		{
			RegionBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_bbsclan"))
		{
			ClanBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_bbsmemo"))
		{
			TopicBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_bbsmail") || command.equals("_maillist_0_1_0_"))
		{
			MailBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_friend") || command.startsWith("_block"))
		{
			FriendsBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_bbstopics"))
		{
			TopicBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_bbsposts"))
		{
			PostBBSManager.getInstance().parseCmd(command, player);
		}
		else if (command.startsWith("_bbsgetfav") || command.startsWith("bbs_add_fav") || command.startsWith("_bbsdelfav_"))
		{
			FavoriteBBSManager.getInstance().parseCmd(command, player);
		}
		else
		{
			BaseBBSManager.separateAndSend("<html><body><br><br><center>The command: " + command + " isn't implemented.</center></body></html>", player);
		}
	}
	
	public void handleWriteCommands(GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!Config.ENABLE_COMMUNITY_BOARD)
		{
			player.sendPacket(SystemMessageId.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}
		
		if (url.equals("Topic"))
		{
			TopicBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
		}
		else if (url.equals("Post"))
		{
			PostBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
		}
		else if (url.equals("_bbsloc"))
		{
			RegionBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
		}
		else if (url.equals("_bbsclan"))
		{
			ClanBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
		}
		else if (url.equals("Mail"))
		{
			MailBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
		}
		else if (url.equals("_friend"))
		{
			FriendsBBSManager.getInstance().parseWrite(arg1, arg2, arg3, arg4, arg5, player);
		}
		else
		{
			BaseBBSManager.separateAndSend("<html><body><br><br><center>The command: " + url + " isn't implemented.</center></body></html>", player);
		}
	}
	
	/**
	 * Sets the last bypass used by the player.
	 * @param player the player
	 * @param title the title
	 * @param bypass the bypass
	 */
	public void addBypass(PlayerInstance player, String title, String bypass)
	{
		_bypasses.put(player.getObjectId(), title + "&" + bypass);
	}
	
	/**
	 * Removes the last bypass used by the player.
	 * @param player the player
	 * @return the last bypass used
	 */
	public String removeBypass(PlayerInstance player)
	{
		return _bypasses.remove(player.getObjectId());
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoard INSTANCE = new CommunityBoard();
	}
}