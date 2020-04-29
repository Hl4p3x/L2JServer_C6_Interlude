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
package org.l2jserver.gameserver.model;

/**
 * Simple class containing all necessary information to maintain<br>
 * valid time stamps and reuse for skills and items reuse upon re-login.<br>
 * <b>Filter this carefully as it becomes redundant to store reuse for small delays.</b>
 * @author Yesod, Zoey76
 */
public class Timestamp
{
	private final Skill _skill;
	private final long _reuse;
	private volatile long _stamp;
	
	public Timestamp(Skill skill, long reuse)
	{
		_skill = skill;
		_reuse = reuse;
		_stamp = System.currentTimeMillis() + _reuse;
	}
	
	public Timestamp(Skill skill, long reuse, long stamp)
	{
		_skill = skill;
		_reuse = reuse;
		_stamp = stamp;
	}
	
	/**
	 * Gets the time stamp.
	 * @return the time stamp, either the system time where this time stamp was created or the custom time assigned
	 */
	public long getStamp()
	{
		return _stamp;
	}
	
	/**
	 * Gets the skill.
	 * @return the skill
	 */
	public Skill getSkill()
	{
		return _skill;
	}
	
	/**
	 * Gets the skill ID.
	 * @return the skill ID
	 */
	public int getSkillId()
	{
		return _skill.getId();
	}
	
	/**
	 * Gets the skill level.
	 * @return the skill level
	 */
	public int getSkillLevel()
	{
		return _skill.getLevel();
	}
	
	/**
	 * Gets the reuse.
	 * @return the reuse
	 */
	public long getReuse()
	{
		return _reuse;
	}
	
	/**
	 * Gets the remaining time.
	 * @return the remaining time for this time stamp to expire
	 */
	public long getRemaining()
	{
		if (_stamp == 0)
		{
			return 0;
		}
		final long remainingTime = Math.max(_stamp - System.currentTimeMillis(), 0);
		if (remainingTime == 0)
		{
			_stamp = 0;
		}
		return remainingTime;
	}
	
	/**
	 * Verifies if the reuse delay has passed.
	 * @return {@code true} if this time stamp has expired, {@code false} otherwise
	 */
	public boolean hasNotPassed()
	{
		if (_stamp == 0)
		{
			return false;
		}
		final boolean hasNotPassed = System.currentTimeMillis() < _stamp;
		if (!hasNotPassed)
		{
			_stamp = 0;
		}
		return hasNotPassed;
	}
}
