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
package org.l2jserver.gameserver.handler.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author KidZor
 */

public class AdminScript implements IAdminCommandHandler
{
	private static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
	private static final Logger LOGGER = Logger.getLogger(AdminScript.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_load_script"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.startsWith("admin_load_script"))
		{
			File file;
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			final String line = st.nextToken();
			
			try
			{
				file = new File(SCRIPT_FOLDER, line);
				if (file.isFile())
				{
					// try
					// {
					// L2ScriptEngineManager.getInstance().executeScript(file);
					// }
					// catch (ScriptException e)
					// {
					// ScriptEngineManager.getInstance().reportScriptFileError(file, e);
					// }
				}
				else
				{
					LOGGER.warning("Failed loading: (" + file.getCanonicalPath() + " - Reason: doesnt exists or is not a file.");
				}
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
