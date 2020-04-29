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
package org.l2jserver.tools.dbinstaller;

import java.io.File;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.l2jserver.tools.dbinstaller.util.mysql.DBDumper;
import org.l2jserver.tools.dbinstaller.util.mysql.ScriptExecutor;

/**
 * @author mrTJO
 */
public class RunTasks extends Thread
{
	DBOutputInterface _frame;
	String _db;
	String _sqlDir;
	
	public RunTasks(DBOutputInterface frame, String db, String sqlDir)
	{
		_frame = frame;
		_db = db;
		_sqlDir = sqlDir;
	}
	
	@Override
	public void run()
	{
		new DBDumper(_frame, _db);
		final ScriptExecutor exec = new ScriptExecutor(_frame);
		_frame.appendToProgressArea("Installing Database Content...");
		exec.execSqlBatch(new File(_sqlDir));
		_frame.appendToProgressArea("Database Installation Complete!");
		
		try
		{
			_frame.getConnection().close();
		}
		catch (SQLException e)
		{
			JOptionPane.showMessageDialog(null, "Cannot close MySQL Connection: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
		}
		
		_frame.showMessage("Done!", "Database Installation Complete!", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
}
