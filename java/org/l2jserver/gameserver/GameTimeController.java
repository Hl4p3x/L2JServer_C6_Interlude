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
package org.l2jserver.gameserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.ai.CtrlEvent;
import org.l2jserver.gameserver.instancemanager.DayNightSpawnManager;
import org.l2jserver.gameserver.model.actor.Creature;

/**
 * @version $Revision: 1.1.4.8 $ $Date: 2005/04/06 16:13:24 $
 */
public class GameTimeController
{
	static final Logger LOGGER = Logger.getLogger(GameTimeController.class.getName());
	
	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	
	private static GameTimeController INSTANCE = new GameTimeController();
	
	protected static int _gameTicks;
	protected static long _gameStartTime;
	protected static boolean _isNight = false;
	
	private static List<Creature> _movingObjects = new ArrayList<>();
	
	protected static TimerThread _timer;
	private final ScheduledFuture<?> _timerWatcher;
	
	/**
	 * one ingame day is 240 real minutes
	 * @return
	 */
	public static GameTimeController getInstance()
	{
		return INSTANCE;
	}
	
	private GameTimeController()
	{
		_gameStartTime = System.currentTimeMillis() - 3600000; // offset so that the server starts a day begin
		_gameTicks = 3600000 / MILLIS_IN_TICK; // offset so that the server starts a day begin
		
		_timer = new TimerThread();
		_timer.start();
		
		_timerWatcher = ThreadPool.scheduleAtFixedRate(new TimerWatcher(), 0, 1000);
		ThreadPool.scheduleAtFixedRate(new BroadcastSunState(), 0, 600000);
	}
	
	public boolean isNowNight()
	{
		return _isNight;
	}
	
	public int getGameTime()
	{
		return _gameTicks / (TICKS_PER_SECOND * 10);
	}
	
	public static int getGameTicks()
	{
		return _gameTicks;
	}
	
	/**
	 * Add a Creature to movingObjects of GameTimeController.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All Creature in movement are identified in <b>movingObjects</b> of GameTimeController.
	 * @param creature The Creature to add to movingObjects of GameTimeController
	 */
	public synchronized void registerMovingObject(Creature creature)
	{
		if (creature == null)
		{
			return;
		}
		
		if (!_movingObjects.contains(creature))
		{
			_movingObjects.add(creature);
		}
	}
	
	/**
	 * Move all Creatures contained in movingObjects of GameTimeController.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All Creature in movement are identified in <b>movingObjects</b> of GameTimeController.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Update the position of each Creature</li>
	 * <li>If movement is finished, the Creature is removed from movingObjects</li>
	 * <li>Create a task to update the _knownObject and _knowPlayers of each Creature that finished its movement and of their already known WorldObject then notify AI with EVT_ARRIVED</li>
	 */
	protected synchronized void moveObjects()
	{
		// Get all Creature from the ArrayList movingObjects and put them into a table
		final Creature[] chars = _movingObjects.toArray(new Creature[_movingObjects.size()]);
		
		// Create an ArrayList to contain all Creature that are arrived to destination
		List<Creature> ended = null;
		
		// Go throw the table containing Creature in movement
		for (Creature creature : chars)
		{
			// Update the position of the Creature and return True if the movement is finished
			final boolean end = creature.updatePosition(_gameTicks);
			
			// If movement is finished, the Creature is removed from movingObjects and added to the ArrayList ended
			if (end)
			{
				_movingObjects.remove(creature);
				if (ended == null)
				{
					ended = new ArrayList<>();
				}
				
				ended.add(creature);
			}
		}
		
		// Create a task to update the _knownObject and _knowPlayers of each Creature that finished its movement and of their already known WorldObject
		// then notify AI with EVT_ARRIVED
		// TODO: maybe a general TP is needed for that kinda stuff (all knownlist updates should be done in a TP anyway).
		if (ended != null)
		{
			ThreadPool.execute(new MovingObjectArrived(ended));
		}
	}
	
	public void stopTimer()
	{
		_timerWatcher.cancel(true);
		_timer.interrupt();
	}
	
	class TimerThread extends Thread
	{
		protected Exception _error;
		
		public TimerThread()
		{
			super("GameTimeController");
			setDaemon(true);
			setPriority(MAX_PRIORITY);
		}
		
		@Override
		public void run()
		{
			for (;;)
			{
				final int _oldTicks = _gameTicks; // save old ticks value to avoid moving objects 2x in same tick
				long runtime = System.currentTimeMillis() - _gameStartTime; // from server boot to now
				
				_gameTicks = (int) (runtime / MILLIS_IN_TICK); // new ticks value (ticks now)
				
				if (_oldTicks != _gameTicks)
				{
					moveObjects(); // XXX: if this makes objects go slower, remove it
					// but I think it can't make that effect. is it better to call moveObjects() twice in same
					// tick to make-up for missed tick ? or is it better to ignore missed tick ?
					// (will happen very rarely but it will happen ... on garbage collection definitely)
				}
				
				runtime = System.currentTimeMillis() - _gameStartTime - runtime;
				
				// calculate sleep time... time needed to next tick minus time it takes to call moveObjects()
				final int sleepTime = (1 + MILLIS_IN_TICK) - ((int) runtime % MILLIS_IN_TICK);
				
				// LOGGER.finest("TICK: "+_gameTicks);
				
				try
				{
					sleep(sleepTime); // hope other threads will have much more cpu time available now
				}
				catch (InterruptedException e)
				{
					// nothing
				}
				// SelectorThread most of all
			}
		}
	}
	
	class TimerWatcher implements Runnable
	{
		@Override
		public void run()
		{
			if (!_timer.isAlive())
			{
				final String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
				LOGGER.warning(time + " TimerThread stop with following error. restart it.");
				if (_timer._error != null)
				{
					_timer._error.printStackTrace();
				}
				
				_timer = new TimerThread();
				_timer.start();
			}
		}
	}
	
	/**
	 * Update the _knownObject and _knowPlayers of each Creature that finished its movement and of their already known WorldObject then notify AI with EVT_ARRIVED.
	 */
	class MovingObjectArrived implements Runnable
	{
		private final List<Creature> _ended;
		
		MovingObjectArrived(List<Creature> ended)
		{
			_ended = ended;
		}
		
		@Override
		public void run()
		{
			for (Creature creature : _ended)
			{
				try
				{
					creature.getKnownList().updateKnownObjects();
					creature.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				}
				catch (NullPointerException e)
				{
				}
			}
		}
	}
	
	class BroadcastSunState implements Runnable
	{
		@Override
		public void run()
		{
			final int h = (getGameTime() / 60) % 24; // Time in hour
			final boolean tempIsNight = h < 6;
			
			// If diff day/night state
			if (tempIsNight != _isNight)
			{
				// Set current day/night varible to value of temp varible
				_isNight = tempIsNight;
				DayNightSpawnManager.getInstance().notifyChangeMode();
			}
		}
	}
}
