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
package org.l2jserver.tools.dbinstaller.util.mysql;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.l2jserver.commons.util.file.filter.SQLFilter;
import org.l2jserver.tools.dbinstaller.DBOutputInterface;

/**
 * @author mrTJO
 */
public class ScriptExecutor
{
	DBOutputInterface _frame;
	
	public ScriptExecutor(DBOutputInterface frame)
	{
		_frame = frame;
	}
	
	public void execSqlBatch(File dir)
	{
		execSqlBatch(dir, false);
	}
	
	public void execSqlBatch(File dir, boolean skipErrors)
	{
		final File[] files = dir.listFiles(new SQLFilter());
		if (files == null)
		{
			return;
		}
		Arrays.sort(files);
		_frame.setProgressIndeterminate(false);
		_frame.setProgressMaximum(files.length - 1);
		for (int i = 0; i < files.length; i++)
		{
			_frame.setProgressValue(i);
			execSqlFile(files[i], skipErrors);
		}
	}
	
	public void execSqlFile(File file)
	{
		execSqlFile(file, false);
	}
	
	public void execSqlFile(File file, boolean skipErrors)
	{
		_frame.appendToProgressArea("Installing " + file.getName());
		String line = "";
		final Connection con = _frame.getConnection();
		try (Statement stmt = con.createStatement();
			Scanner scn = new Scanner(file))
		{
			StringBuilder sb = new StringBuilder();
			while (scn.hasNextLine())
			{
				line = scn.nextLine();
				if (line.startsWith("--"))
				{
					continue;
				}
				else if (line.contains("--"))
				{
					line = line.split("--")[0];
				}
				
				line = line.trim();
				if (!line.isEmpty())
				{
					sb.append(line + System.getProperty("line.separator"));
				}
				
				if (line.endsWith(";"))
				{
					stmt.execute(sb.toString());
					sb = new StringBuilder();
				}
			}
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "File Not Found!: " + e.getMessage(), "Installer Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (SQLException e)
		{
			if (!skipErrors)
			{
				try
				{
					final Object[] options =
					{
						"Continue",
						"Abort"
					};
					
					if (JOptionPane.showOptionDialog(null, "MySQL Error: " + e.getMessage(), "Script Error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]) == 1)
					{
						System.exit(0);
					}
				}
				catch (HeadlessException h)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
