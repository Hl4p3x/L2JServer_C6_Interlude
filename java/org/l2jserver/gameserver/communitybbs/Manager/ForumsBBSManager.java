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
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.communitybbs.BB.Forum;

public class ForumsBBSManager extends BaseBBSManager
{
	private final Collection<Forum> _table;
	private int _lastid = 1;
	
	public static ForumsBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	protected ForumsBBSManager()
	{
		_table = ConcurrentHashMap.newKeySet();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			final ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				addForum(new Forum(result.getInt("forum_id"), null));
			}
			
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on Forum (root): " + e.getMessage());
		}
	}
	
	public void initRoot()
	{
		for (Forum f : _table)
		{
			f.vload();
		}
		
		LOGGER.info("Loaded " + _table.size() + " forums. Last forum id used: " + _lastid);
	}
	
	public void addForum(Forum ff)
	{
		if (ff == null)
		{
			return;
		}
		
		_table.add(ff);
		
		if (ff.getID() > _lastid)
		{
			_lastid = ff.getID();
		}
	}
	
	public Forum getForumByName(String name)
	{
		for (Forum f : _table)
		{
			if (f.getName().equals(name))
			{
				return f;
			}
		}
		return null;
	}
	
	public Forum createNewForum(String name, Forum parent, int type, int perm, int oid)
	{
		final Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertIntoDb();
		
		return forum;
	}
	
	public int getANewID()
	{
		return ++_lastid;
	}
	
	public Forum getForumByID(int id)
	{
		for (Forum f : _table)
		{
			if (f.getID() == id)
			{
				return f;
			}
		}
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final ForumsBBSManager INSTANCE = new ForumsBBSManager();
	}
}