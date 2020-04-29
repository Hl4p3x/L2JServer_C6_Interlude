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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.commons.util.StringUtil;
import org.l2jserver.gameserver.communitybbs.CommunityBoard;
import org.l2jserver.gameserver.communitybbs.BB.Forum;
import org.l2jserver.gameserver.communitybbs.BB.Post;
import org.l2jserver.gameserver.communitybbs.BB.Topic;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

public class TopicBBSManager extends BaseBBSManager
{
	private final List<Topic> _table;
	private final Map<Forum, Integer> _maxId;
	
	public static TopicBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	protected TopicBBSManager()
	{
		_table = new ArrayList<>();
		_maxId = new ConcurrentHashMap<>();
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, PlayerInstance player)
	{
		if (ar1.equals("crea"))
		{
			final Forum f = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if (f == null)
			{
				separateAndSend("<html><body><br><br><center>The forum named '" + ar2 + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			f.vload();
			final Topic t = new Topic(Topic.ConstructorType.CREATE, getInstance().getMaxID(f) + 1, Integer.parseInt(ar2), ar5, Calendar.getInstance().getTimeInMillis(), player.getName(), player.getObjectId(), Topic.MEMO, 0);
			f.addTopic(t);
			getInstance().setMaxID(t.getID(), f);
			
			final Post p = new Post(player.getName(), player.getObjectId(), Calendar.getInstance().getTimeInMillis(), t.getID(), f.getID(), ar4);
			PostBBSManager.getInstance().addPostByTopic(p, t);
			parseCmd("_bbsmemo", player);
		}
		else if (ar1.equals("del"))
		{
			final Forum f = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if (f == null)
			{
				separateAndSend("<html><body><br><br><center>The forum named '" + ar2 + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Topic t = f.getTopic(Integer.parseInt(ar3));
			if (t == null)
			{
				separateAndSend("<html><body><br><br><center>The topic named '" + ar3 + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Post p = PostBBSManager.getInstance().getPostByTopic(t);
			if (p != null)
			{
				p.deleteMe(t);
			}
			
			t.deleteMe(f);
			parseCmd("_bbsmemo", player);
		}
		else
		{
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, player);
		}
	}
	
	@Override
	public void parseCmd(String command, PlayerInstance player)
	{
		if (command.equals("_bbsmemo"))
		{
			CommunityBoard.getInstance().addBypass(player, "Memo Command", command);
			showTopics(player.getMemo(), player, 1, player.getMemo().getID());
		}
		else if (command.startsWith("_bbstopics;read"))
		{
			CommunityBoard.getInstance().addBypass(player, "Topics Command", command);
			
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int idf = Integer.parseInt(st.nextToken());
			String index = null;
			if (st.hasMoreTokens())
			{
				index = st.nextToken();
			}
			
			int ind = 0;
			if (index == null)
			{
				ind = 1;
			}
			else
			{
				ind = Integer.parseInt(index);
			}
			
			showTopics(ForumsBBSManager.getInstance().getForumByID(idf), player, ind, idf);
		}
		else if (command.startsWith("_bbstopics;crea"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int idf = Integer.parseInt(st.nextToken());
			showNewTopic(ForumsBBSManager.getInstance().getForumByID(idf), player, idf);
		}
		else if (command.startsWith("_bbstopics;del"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			
			final int idf = Integer.parseInt(st.nextToken());
			final int idt = Integer.parseInt(st.nextToken());
			final Forum f = ForumsBBSManager.getInstance().getForumByID(idf);
			if (f == null)
			{
				separateAndSend("<html><body><br><br><center>The forum named '" + idf + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Topic t = f.getTopic(idt);
			if (t == null)
			{
				separateAndSend("<html><body><br><br><center>The topic named '" + idt + "' doesn't exist.</center></body></html>", player);
				return;
			}
			
			final Post p = PostBBSManager.getInstance().getPostByTopic(t);
			if (p != null)
			{
				p.deleteMe(t);
			}
			
			t.deleteMe(f);
			parseCmd("_bbsmemo", player);
		}
		else
		{
			super.parseCmd(command, player);
		}
	}
	
	public void addTopic(Topic tt)
	{
		_table.add(tt);
	}
	
	public void delTopic(Topic topic)
	{
		_table.remove(topic);
	}
	
	public void setMaxID(int id, Forum f)
	{
		_maxId.put(f, id);
	}
	
	public int getMaxID(Forum f)
	{
		final Integer i = _maxId.get(f);
		if (i == null)
		{
			return 0;
		}
		return i;
	}
	
	public Topic getTopicByID(int idf)
	{
		for (Topic t : _table)
		{
			if (t.getID() == idf)
			{
				return t;
			}
		}
		return null;
	}
	
	private void showNewTopic(Forum forum, PlayerInstance player, int idf)
	{
		if (forum == null)
		{
			separateAndSend("<html><body><br><br><center>The forum named '" + idf + "' doesn't exist.</center></body></html>", player);
			return;
		}
		
		if (forum.getType() == Forum.MEMO)
		{
			showMemoNewTopics(forum, player);
		}
		else
		{
			separateAndSend("<html><body><br><br><center>The forum named '" + forum.getName() + "' doesn't exist.</center></body></html>", player);
		}
	}
	
	private void showMemoNewTopics(Forum forum, PlayerInstance player)
	{
		final String html = "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0><tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&$413;</td><td FIXWIDTH=540><edit var = \"Title\" width=540 height=13></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td><td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&nbsp;</td><td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Topic crea " + forum.getID() + " Title Content Title\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td><td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td><td align=center FIXWIDTH=400>&nbsp;</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table></center></body></html>";
		send1001(html, player);
		send1002(player);
	}
	
	private void showTopics(Forum forum, PlayerInstance player, int index, int idf)
	{
		if (forum == null)
		{
			separateAndSend("<html><body><br><br><center>The forum named '" + idf + "' doesn't exist.</center></body></html>", player);
			return;
		}
		
		if (forum.getType() == Forum.MEMO)
		{
			showMemoTopics(forum, player, index);
		}
		else
		{
			separateAndSend("<html><body><br><br><center>The forum named '" + forum.getName() + "' doesn't exist.</center></body></html>", player);
		}
	}
	
	private void showMemoTopics(Forum forum, PlayerInstance player, int index)
	{
		forum.vload();
		final StringBuilder sb = new StringBuilder("<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415 align=center>&$413;</td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>&$418;</td></tr></table>");
		final DateFormat dateFormat = DateFormat.getInstance();
		for (int i = 0, j = getMaxID(forum) + 1; i < (12 * index); j--)
		{
			if (j < 0)
			{
				break;
			}
			
			final Topic t = forum.getTopic(j);
			if ((t != null) && (i++ >= (12 * (index - 1))))
			{
				StringUtil.append(sb, "<table border=0 cellspacing=0 cellpadding=5 WIDTH=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415><a action=\"bypass _bbsposts;read;", forum.getID(), ";", t.getID(), "\">", t.getName(), "</a></td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>", dateFormat.format(new Date(t.getDate())), "</td></tr></table><img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
			}
		}
		
		sb.append("<br><table width=610 cellspace=0 cellpadding=0><tr><td width=50><button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td><td width=510 align=center><table border=0><tr>");
		if (index == 1)
		{
			sb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			StringUtil.append(sb, "<td><button action=\"bypass _bbstopics;read;", forum.getID(), ";", index - 1, "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		
		int nbp;
		nbp = forum.getTopicSize() / 8;
		if ((nbp * 8) != ClanTable.getInstance().getClans().length)
		{
			nbp++;
		}
		
		for (int i = 1; i <= nbp; i++)
		{
			if (i == index)
			{
				StringUtil.append(sb, "<td> ", i, " </td>");
			}
			else
			{
				StringUtil.append(sb, "<td><a action=\"bypass _bbstopics;read;", forum.getID(), ";", i, "\"> ", i, " </a></td>");
			}
		}
		
		if (index == nbp)
		{
			sb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			StringUtil.append(sb, "<td><button action=\"bypass _bbstopics;read;", forum.getID(), ";", index + 1, "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		
		StringUtil.append(sb, "</tr></table></td><td align=right><button value = \"&$421;\" action=\"bypass _bbstopics;crea;", forum.getID(), "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td></td><td align=center><table border=0><tr><td></td><td><edit var = \"Search\" width=130 height=11></td><td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table></td></tr></table><br><br><br></center></body></html>");
		separateAndSend(sb.toString(), player);
	}
	
	private static class SingletonHolder
	{
		protected static final TopicBBSManager INSTANCE = new TopicBBSManager();
	}
}