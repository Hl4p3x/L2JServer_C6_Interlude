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
package org.l2jserver.commons.util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ClassMasterSettings
{
	private final Map<Integer, Map<Integer, Integer>> _claimItems;
	private final Map<Integer, Map<Integer, Integer>> _rewardItems;
	private final Map<Integer, Boolean> _allowedClassChange;
	
	public ClassMasterSettings(String configLine)
	{
		_claimItems = new HashMap<>();
		_rewardItems = new HashMap<>();
		_allowedClassChange = new HashMap<>();
		if (configLine != null)
		{
			parseConfigLine(configLine.trim());
		}
	}
	
	private void parseConfigLine(String configLine)
	{
		final StringTokenizer st = new StringTokenizer(configLine, ";");
		while (st.hasMoreTokens())
		{
			final int job = Integer.parseInt(st.nextToken());
			_allowedClassChange.put(job, true);
			Map<Integer, Integer> items = new HashMap<>();
			
			if (st.hasMoreTokens())
			{
				final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				while (st2.hasMoreTokens())
				{
					final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					final int itemId = Integer.parseInt(st3.nextToken());
					final int quantity = Integer.parseInt(st3.nextToken());
					items.put(itemId, quantity);
				}
			}
			_claimItems.put(job, items);
			
			items = new HashMap<>();
			if (st.hasMoreTokens())
			{
				final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				while (st2.hasMoreTokens())
				{
					final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					final int itemId = Integer.parseInt(st3.nextToken());
					final int quantity = Integer.parseInt(st3.nextToken());
					items.put(itemId, quantity);
				}
			}
			_rewardItems.put(job, items);
		}
	}
	
	public boolean isAllowed(int job)
	{
		if (_allowedClassChange == null)
		{
			return false;
		}
		if (_allowedClassChange.containsKey(job))
		{
			return _allowedClassChange.get(job);
		}
		return false;
	}
	
	public Map<Integer, Integer> getRewardItems(int job)
	{
		if (_rewardItems.containsKey(job))
		{
			return _rewardItems.get(job);
		}
		return null;
	}
	
	public Map<Integer, Integer> getRequireItems(int job)
	{
		if (_claimItems.containsKey(job))
		{
			return _claimItems.get(job);
		}
		return null;
	}
}