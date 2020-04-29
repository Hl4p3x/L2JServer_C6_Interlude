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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class OlympiadLogger
{
	private static final Logger LOGGER = Logger.getLogger(OlympiadLogger.class.getName());
	
	public static void add(String text, String cat)
	{
		final String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
		
		new File("log/game").mkdirs();
		final File file = new File("log/game/" + (cat != null ? cat : "_all") + ".txt");
		FileWriter save = null;
		try
		{
			save = new FileWriter(file, true);
			final String out = "[" + date + "] '---': " + text + "\n"; // "+char_name()+"
			save.write(out);
			save.flush();
		}
		catch (IOException e)
		{
			LOGGER.warning("saving chat LOGGER failed: " + e);
			e.printStackTrace();
		}
		finally
		{
			if (save != null)
			{
				try
				{
					save.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (cat != null)
		{
			add(text, null);
		}
	}
}