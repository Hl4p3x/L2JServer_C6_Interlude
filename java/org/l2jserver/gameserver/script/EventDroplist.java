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
package org.l2jserver.gameserver.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class manage drop of Special Events created by GM for a defined period. During a Special Event all Attackable can drop extra Items. Those extra Items are defined in the table <b>allNpcDateDrops</b>. Each Special Event has a start and end date to stop to drop extra Items automaticaly.
 */
public class EventDroplist
{
	/** The table containing all DataDrop object */
	private final List<DateDrop> _allNpcDateDrops;
	
	public class DateDrop
	{
		/** Start and end date of the Event */
		public DateRange dateRange;
		
		/** The table containing Item identifier that can be dropped as extra Items during the Event */
		public int[] items;
		
		/** The min number of Item dropped in one time during this Event */
		public int min;
		
		/** The max number of Item dropped in one time during this Event */
		public int max;
		
		/** The rate of drop for this Event */
		public int chance;
	}
	
	/**
	 * Constructor of EventDroplist.
	 */
	private EventDroplist()
	{
		_allNpcDateDrops = new ArrayList<>();
	}
	
	/**
	 * Create and Init a new DateDrop then add it to the allNpcDateDrops of EventDroplist .
	 * @param items The table containing all item identifier of this DateDrop
	 * @param count The table containing min and max value of this DateDrop
	 * @param chance The chance to obtain this drop
	 * @param range The DateRange object to add to this DateDrop
	 */
	public void addGlobalDrop(int[] items, int[] count, int chance, DateRange range)
	{
		final DateDrop date = new DateDrop();
		date.dateRange = range;
		date.items = items;
		date.min = count[0];
		date.max = count[1];
		date.chance = chance;
		_allNpcDateDrops.add(date);
	}
	
	/**
	 * @return all DateDrop of EventDroplist allNpcDateDrops within the date range.
	 */
	public List<DateDrop> getAllDrops()
	{
		final List<DateDrop> list = new ArrayList<>();
		for (DateDrop drop : _allNpcDateDrops)
		{
			final Date currentDate = new Date();
			if (drop.dateRange.isWithinRange(currentDate))
			{
				list.add(drop);
			}
		}
		return list;
	}
	
	public static EventDroplist getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final EventDroplist INSTANCE = new EventDroplist();
	}
}
