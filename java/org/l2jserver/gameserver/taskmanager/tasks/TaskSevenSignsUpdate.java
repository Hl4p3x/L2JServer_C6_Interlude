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
package org.l2jserver.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSignsFestival;
import org.l2jserver.gameserver.taskmanager.Task;
import org.l2jserver.gameserver.taskmanager.TaskManager;
import org.l2jserver.gameserver.taskmanager.TaskManager.ExecutedTask;
import org.l2jserver.gameserver.taskmanager.TaskTypes;

/**
 * Updates all data for the Seven Signs and Festival of Darkness engines, when time is elapsed.
 * @author Tempy
 */
public class TaskSevenSignsUpdate extends Task
{
	private static final Logger LOGGER = Logger.getLogger(TaskSevenSignsUpdate.class.getName());
	public static final String NAME = "sevensignsupdate";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try
		{
			SevenSigns.getInstance().saveSevenSignsData(null, true);
			if (!SevenSigns.getInstance().isSealValidationPeriod())
			{
				SevenSignsFestival.getInstance().saveFestivalData(false);
			}
			
			LOGGER.info("[GlobalTask] SevenSigns save launched.");
		}
		catch (Exception e)
		{
			LOGGER.warning("SevenSigns: Failed to save Seven Signs configuration: " + e);
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "1800000", "");
	}
}