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
package org.l2jserver.gameserver.model.entity.event.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;

/**
 * @author Shyla
 */
public class EventsGlobalTask implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(EventsGlobalTask.class.getName());
	
	private static EventsGlobalTask instance;
	
	private boolean destroy = false;
	
	private final Map<String, List<EventTask>> timeToTasks = new ConcurrentHashMap<>(); // time is in hh:mm
	private final Map<String, List<EventTask>> eventIdToTasks = new ConcurrentHashMap<>();
	
	private EventsGlobalTask()
	{
		ThreadPool.schedule(this, 5000);
	}
	
	public static EventsGlobalTask getInstance()
	{
		if (instance == null)
		{
			instance = new EventsGlobalTask();
		}
		return instance;
	}
	
	public void registerNewEventTask(EventTask event)
	{
		if ((event == null) || (event.getEventIdentifier() == null) || event.getEventIdentifier().equals("") || (event.getEventStartTime() == null) || event.getEventStartTime().equals(""))
		{
			LOGGER.warning("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		List<EventTask> savedTasksForTime = timeToTasks.get(event.getEventStartTime());
		List<EventTask> savedTasksForId = eventIdToTasks.get(event.getEventIdentifier());
		if (savedTasksForTime != null)
		{
			if (!savedTasksForTime.contains(event))
			{
				savedTasksForTime.add(event);
			}
		}
		else
		{
			savedTasksForTime = new ArrayList<>();
			savedTasksForTime.add(event);
		}
		
		timeToTasks.put(event.getEventStartTime(), savedTasksForTime);
		
		if (savedTasksForId != null)
		{
			if (!savedTasksForId.contains(event))
			{
				savedTasksForId.add(event);
			}
		}
		else
		{
			savedTasksForId = new ArrayList<>();
			savedTasksForId.add(event);
		}
		
		eventIdToTasks.put(event.getEventIdentifier(), savedTasksForId);
	}
	
	public void clearEventTasksByEventName(String eventId)
	{
		if (eventId == null)
		{
			LOGGER.warning("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		if (eventId.equalsIgnoreCase("all"))
		{
			timeToTasks.clear();
			eventIdToTasks.clear();
		}
		else
		{
			final List<EventTask> oldTasksForId = eventIdToTasks.get(eventId);
			if (oldTasksForId != null)
			{
				for (EventTask actual : oldTasksForId)
				{
					final List<EventTask> oldTasksForTime = timeToTasks.get(actual.getEventStartTime());
					if (oldTasksForTime != null)
					{
						oldTasksForTime.remove(actual);
						timeToTasks.put(actual.getEventStartTime(), oldTasksForTime);
					}
				}
				eventIdToTasks.remove(eventId);
			}
		}
	}
	
	public void deleteEventTask(EventTask event)
	{
		if ((event == null) || (event.getEventIdentifier() == null) || event.getEventIdentifier().equals("") || (event.getEventStartTime() == null) || event.getEventStartTime().equals(""))
		{
			LOGGER.warning("registerNewEventTask: eventTask must be not null as its identifier and startTime ");
			return;
		}
		
		if (timeToTasks.size() < 0)
		{
			return;
		}
		
		final List<EventTask> oldTasksForId = eventIdToTasks.get(event.getEventIdentifier());
		final List<EventTask> oldTasksForTime = timeToTasks.get(event.getEventStartTime());
		if (oldTasksForId != null)
		{
			oldTasksForId.remove(event);
			eventIdToTasks.put(event.getEventIdentifier(), oldTasksForId);
		}
		
		if (oldTasksForTime != null)
		{
			oldTasksForTime.remove(event);
			timeToTasks.put(event.getEventStartTime(), oldTasksForTime);
		}
	}
	
	private void checkRegisteredEvents()
	{
		if (timeToTasks.size() < 0)
		{
			return;
		}
		
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int min = calendar.get(Calendar.MINUTE);
		String hourStr = "";
		String minStr = "";
		if (hour < 10)
		{
			hourStr = "0" + hour;
		}
		else
		{
			hourStr = "" + hour;
		}
		
		if (min < 10)
		{
			minStr = "0" + min;
		}
		else
		{
			minStr = "" + min;
		}
		
		final String currentTime = hourStr + ":" + minStr;
		final List<EventTask> registeredEventsAtCurrentTime = timeToTasks.get(currentTime);
		if (registeredEventsAtCurrentTime != null)
		{
			for (EventTask actualEvent : registeredEventsAtCurrentTime)
			{
				ThreadPool.schedule(actualEvent, 5000);
			}
		}
	}
	
	public void destroyLocalInstance()
	{
		destroy = true;
		instance = null;
	}
	
	@Override
	public void run()
	{
		while (!destroy)
		{
			// start time checker
			checkRegisteredEvents();
			
			try
			{
				Thread.sleep(60000); // 1 minute
			}
			catch (InterruptedException e)
			{
			}
		}
	}
}
