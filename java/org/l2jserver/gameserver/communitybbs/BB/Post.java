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
package org.l2jserver.gameserver.communitybbs.BB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.communitybbs.Manager.PostBBSManager;

/**
 * @author Maktakien
 */
public class Post
{
	private static final Logger LOGGER = Logger.getLogger(Post.class.getName());
	
	public class CPost
	{
		public int postId;
		public String postOwner;
		public int postOwnerId;
		public long postDate;
		public int postTopicId;
		public int postForumId;
		public String postTxt;
	}
	
	private final List<CPost> _post;
	
	/**
	 * @param postOwner
	 * @param postOwnerId
	 * @param date
	 * @param tid
	 * @param postForumId
	 * @param txt
	 */
	public Post(String postOwner, int postOwnerId, long date, int tid, int postForumId, String txt)
	{
		_post = new ArrayList<>();
		final CPost cp = new CPost();
		cp.postId = 0;
		cp.postOwner = postOwner;
		cp.postOwnerId = postOwnerId;
		cp.postDate = date;
		cp.postTopicId = tid;
		cp.postForumId = postForumId;
		cp.postTxt = txt;
		_post.add(cp);
		insertindb(cp);
	}
	
	public void insertindb(CPost cp)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)");
			statement.setInt(1, cp.postId);
			statement.setString(2, cp.postOwner);
			statement.setInt(3, cp.postOwnerId);
			statement.setLong(4, cp.postDate);
			statement.setInt(5, cp.postTopicId);
			statement.setInt(6, cp.postForumId);
			statement.setString(7, cp.postTxt);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while saving new Post to db " + e);
		}
	}
	
	public Post(Topic t)
	{
		_post = new ArrayList<>();
		load(t);
	}
	
	public CPost getCPost(int id)
	{
		int i = 0;
		for (CPost cp : _post)
		{
			if (i++ == id)
			{
				return cp;
			}
		}
		return null;
	}
	
	public void deleteMe(Topic t)
	{
		PostBBSManager.getInstance().delPostByTopic(t);
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while deleting post: " + e.getMessage());
		}
	}
	
	private void load(Topic t)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			final ResultSet result = statement.executeQuery();
			while (result.next())
			{
				final CPost cp = new CPost();
				cp.postId = result.getInt("post_id");
				cp.postOwner = result.getString("post_owner_name");
				cp.postOwnerId = result.getInt("post_ownerid");
				cp.postDate = result.getLong("post_date");
				cp.postTopicId = result.getInt("post_topic_id");
				cp.postForumId = result.getInt("post_forum_id");
				cp.postTxt = result.getString("post_txt");
				_post.add(cp);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on Post " + t.getForumID() + "/" + t.getID() + " : " + e);
		}
	}
	
	public void updateText(int i)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final CPost cp = getCPost(i);
			final PreparedStatement statement = con.prepareStatement("UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");
			statement.setString(1, cp.postTxt);
			statement.setInt(2, cp.postId);
			statement.setInt(3, cp.postTopicId);
			statement.setInt(4, cp.postForumId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while saving new Post to db " + e);
		}
	}
}