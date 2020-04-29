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
package org.l2jserver.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.StringUtil;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.communitybbs.CommunityBoard;
import org.l2jserver.gameserver.datatables.sql.CharNameTable;
import org.l2jserver.gameserver.model.BlockList;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.FriendList;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class FriendsBBSManager extends BaseBBSManager
{
	private static final String FRIENDLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all friends from your Friends List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _friend;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";
	private static final String BLOCKLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all players from your Block List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _block;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";
	
	protected FriendsBBSManager()
	{
	}
	
	public static FriendsBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public void parseCmd(String command, PlayerInstance activeChar)
	{
		if (command.startsWith("_friendlist"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Friends List", command);
			showFriendsList(activeChar, false);
		}
		else if (command.startsWith("_blocklist"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Ignore List", command);
			showBlockList(activeChar, false);
		}
		else if (command.startsWith("_friend"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			final String action = st.nextToken();
			if (action.equals("select"))
			{
				activeChar.selectFriend((st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 0);
				showFriendsList(activeChar, false);
			}
			else if (action.equals("deselect"))
			{
				activeChar.deselectFriend((st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 0);
				showFriendsList(activeChar, false);
			}
			else if (action.equals("delall"))
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id = ? OR friend_id = ?");
					statement.setInt(1, activeChar.getObjectId());
					statement.setInt(2, activeChar.getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("could not delete friends objectid: " + e);
				}
				
				for (int friendId : activeChar.getFriendList())
				{
					final PlayerInstance player = World.getInstance().getPlayer(friendId);
					if (player != null)
					{
						player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
						player.getSelectedFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
						
						player.sendPacket(new FriendList(player)); // update friendList *heavy method*
					}
				}
				
				activeChar.getFriendList().clear();
				activeChar.getSelectedFriendList().clear();
				showFriendsList(activeChar, false);
				activeChar.sendMessage("You have cleared your friend list.");
				activeChar.sendPacket(new FriendList(activeChar));
			}
			else if (action.equals("delconfirm"))
			{
				showFriendsList(activeChar, true);
			}
			else if (action.equals("del"))
			{
				try (Connection con = DatabaseFactory.getConnection())
				{
					for (int friendId : activeChar.getSelectedFriendList())
					{
						final PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)");
						statement.setInt(1, activeChar.getObjectId());
						statement.setInt(2, friendId);
						statement.setInt(3, friendId);
						statement.setInt(4, activeChar.getObjectId());
						statement.execute();
						statement.close();
						
						final String name = CharNameTable.getInstance().getPlayerName(friendId);
						final PlayerInstance player = World.getInstance().getPlayer(friendId);
						if (player != null)
						{
							player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
							player.sendPacket(new FriendList(player)); // update friendList *heavy method*
						}
						
						// Player deleted from your friendlist
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(name));
						
						activeChar.getFriendList().remove(Integer.valueOf(friendId));
					}
				}
				catch (Exception e)
				{
					LOGGER.warning("could not delete friend objectid: " + e);
				}
				
				activeChar.getSelectedFriendList().clear();
				showFriendsList(activeChar, false);
				activeChar.sendPacket(new FriendList(activeChar)); // update friendList *heavy method*
			}
			else if (action.equals("mail"))
			{
				if (!activeChar.getSelectedFriendList().isEmpty())
				{
					showMailWrite(activeChar);
				}
			}
		}
		else if (command.startsWith("_block"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			final String action = st.nextToken();
			if (action.equals("select"))
			{
				activeChar.selectBlock((st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 0);
				showBlockList(activeChar, false);
			}
			else if (action.equals("deselect"))
			{
				activeChar.deselectBlock((st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 0);
				showBlockList(activeChar, false);
			}
			else if (action.equals("delall"))
			{
				final List<Integer> list = new ArrayList<>();
				list.addAll(activeChar.getBlockList().getBlockList());
				
				for (Integer blockId : list)
				{
					BlockList.removeFromBlockList(activeChar, blockId);
				}
				
				activeChar.getSelectedBlocksList().clear();
				showBlockList(activeChar, false);
			}
			else if (action.equals("delconfirm"))
			{
				showBlockList(activeChar, true);
			}
			else if (action.equals("del"))
			{
				for (Integer blockId : activeChar.getSelectedBlocksList())
				{
					BlockList.removeFromBlockList(activeChar, blockId);
				}
				
				activeChar.getSelectedBlocksList().clear();
				showBlockList(activeChar, false);
			}
		}
		else
		{
			super.parseCmd(command, activeChar);
		}
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, PlayerInstance activeChar)
	{
		if (ar1.equalsIgnoreCase("mail"))
		{
			MailBBSManager.getInstance().sendLetter(ar2, ar4, ar5, activeChar);
			showFriendsList(activeChar, false);
		}
		else
		{
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, activeChar);
		}
	}
	
	private void showFriendsList(PlayerInstance activeChar, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-list.htm");
		if (content == null)
		{
			return;
		}
		
		// Retrieve activeChar's friendlist and selected
		final List<Integer> list = activeChar.getFriendList();
		final List<Integer> slist = activeChar.getSelectedFriendList();
		final StringBuilder sb = new StringBuilder();
		
		// Friendlist
		for (Integer id : list)
		{
			if (slist.contains(id))
			{
				continue;
			}
			
			final String friendName = CharNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
			{
				continue;
			}
			
			final PlayerInstance friend = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _friend;select;", id, "\">[Select]</a>&nbsp;", friendName, " ", (((friend != null) && friend.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replace("%friendslist%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		
		// Selected friendlist
		for (Integer id : slist)
		{
			final String friendName = CharNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
			{
				continue;
			}
			
			final PlayerInstance friend = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _friend;deselect;", id, "\">[Deselect]</a>&nbsp;", friendName, " ", (((friend != null) && friend.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replace("%selectedFriendsList%", sb.toString());
		
		// Delete button.
		content = content.replace("%deleteMSG%", (delMsg) ? FRIENDLIST_DELETE_BUTTON : "");
		separateAndSend(content, activeChar);
	}
	
	private void showBlockList(PlayerInstance activeChar, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-blocklist.htm");
		if (content == null)
		{
			return;
		}
		
		// Retrieve activeChar's blocklist and selected
		final List<Integer> list = activeChar.getBlockList().getBlockList();
		final List<Integer> slist = activeChar.getSelectedBlocksList();
		final StringBuilder sb = new StringBuilder();
		
		// Blocklist
		for (Integer id : list)
		{
			if (slist.contains(id))
			{
				continue;
			}
			
			final String blockName = CharNameTable.getInstance().getPlayerName(id);
			if (blockName == null)
			{
				continue;
			}
			
			final PlayerInstance block = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _block;select;", id, "\">[Select]</a>&nbsp;", blockName, " ", (((block != null) && block.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replace("%blocklist%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		
		// Selected Blocklist
		for (Integer id : slist)
		{
			final String blockName = CharNameTable.getInstance().getPlayerName(id);
			if (blockName == null)
			{
				continue;
			}
			
			final PlayerInstance block = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _block;deselect;", id, "\">[Deselect]</a>&nbsp;", blockName, " ", (((block != null) && block.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replace("%selectedBlocksList%", sb.toString());
		
		// Delete button.
		content = content.replace("%deleteMSG%", (delMsg) ? BLOCKLIST_DELETE_BUTTON : "");
		separateAndSend(content, activeChar);
	}
	
	public static void showMailWrite(PlayerInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-mail.htm");
		if (content == null)
		{
			return;
		}
		
		final StringBuilder sb = new StringBuilder();
		for (int id : activeChar.getSelectedFriendList())
		{
			final String friendName = CharNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
			{
				continue;
			}
			
			if (sb.length() > 0)
			{
				sb.append(";");
			}
			
			sb.append(friendName);
		}
		
		content = content.replace("%list%", sb.toString());
		separateAndSend(content, activeChar);
	}
	
	@Override
	protected String getFolder()
	{
		return "friend/";
	}
	
	private static class SingletonHolder
	{
		protected static final FriendsBBSManager INSTANCE = new FriendsBBSManager();
	}
}