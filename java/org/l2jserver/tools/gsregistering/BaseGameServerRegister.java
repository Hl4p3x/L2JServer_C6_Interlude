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
package org.l2jserver.tools.gsregistering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.enums.ServerMode;
import org.l2jserver.commons.util.Util;
import org.l2jserver.loginserver.GameServerTable;

/**
 * The Class BaseGameServerRegister.
 * @author KenM
 */
public abstract class BaseGameServerRegister
{
	private boolean _loaded = false;
	
	/**
	 * The main method.
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{
		final GameServerRegister cmdUi = new GameServerRegister();
		try
		{
			cmdUi.consoleUI();
		}
		catch (IOException e)
		{
			cmdUi.showError("I/O exception trying to get input from keyboard.", e);
		}
	}
	
	/**
	 * Load.
	 */
	public void load()
	{
		Config.load(ServerMode.LOGIN);
		DatabaseFactory.init();
		GameServerTable.getInstance();
		_loaded = true;
	}
	
	/**
	 * Checks if is loaded.
	 * @return true, if is loaded
	 */
	public boolean isLoaded()
	{
		return _loaded;
	}
	
	/**
	 * Show the error.
	 * @param msg the msg.
	 * @param t the t.
	 */
	public abstract void showError(String msg, Throwable t);
	
	/**
	 * Unregister the game server.
	 * @param id the game server id.
	 * @throws SQLException the SQL exception.
	 */
	public static void unregisterGameServer(int id) throws SQLException
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM gameservers WHERE server_id = ?"))
		{
			ps.setInt(1, id);
			ps.executeUpdate();
		}
		GameServerTable.getInstance().getRegisteredGameServers().remove(id);
	}
	
	/**
	 * Unregister all game servers.
	 * @throws SQLException the SQL exception
	 */
	public static void unregisterAllGameServers() throws SQLException
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement())
		{
			s.executeUpdate("DELETE FROM gameservers");
		}
		GameServerTable.getInstance().getRegisteredGameServers().clear();
	}
	
	/**
	 * Register a game server.
	 * @param id the id of the game server.
	 * @param outDir the out dir.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void registerGameServer(int id, String outDir) throws IOException
	{
		final byte[] hexId = Util.generateHex(16);
		GameServerTable.getInstance().registerServerOnDB(hexId, id, "");
		
		final Properties hexSetting = new Properties();
		final File file = new File(outDir, "hexid.txt");
		// Create a new empty file only if it doesn't exist
		file.createNewFile();
		try (OutputStream out = new FileOutputStream(file))
		{
			hexSetting.setProperty("ServerID", String.valueOf(id));
			hexSetting.setProperty("HexID", new BigInteger(hexId).toString(16));
			hexSetting.store(out, "The HexId to Auth into LoginServer");
		}
	}
	
	/**
	 * Register first available.
	 * @param outDir the out dir
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int registerFirstAvailable(String outDir) throws IOException
	{
		for (Entry<Integer, String> e : GameServerTable.getInstance().getServerNames().entrySet())
		{
			if (!GameServerTable.getInstance().hasRegisteredGameServerOnId(e.getKey()))
			{
				registerGameServer(e.getKey(), outDir);
				return e.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * The Class BaseTask.
	 */
	protected abstract static class BaseTask implements Runnable
	{
		protected ResourceBundle _bundle;
		
		/**
		 * Sets the bundle.
		 * @param bundle The bundle to set.
		 */
		public void setBundle(ResourceBundle bundle)
		{
			_bundle = bundle;
		}
		
		/**
		 * Gets the bundle.
		 * @return Returns the bundle.
		 */
		public ResourceBundle getBundle()
		{
			return _bundle;
		}
		
		/**
		 * Show the error.
		 * @param msg the msg
		 * @param t the t
		 */
		public void showError(String msg, Throwable t)
		{
			String title;
			if (_bundle != null)
			{
				title = _bundle.getString("error");
				msg += Config.EOL + _bundle.getString("reason") + ' ' + t.getLocalizedMessage();
			}
			else
			{
				title = "Error";
				msg += Config.EOL + "Cause: " + t.getLocalizedMessage();
			}
			System.out.println(title + ": " + msg);
		}
	}
	
	/**
	 * The Class UnregisterAllTask.
	 */
	protected static class UnregisterAllTask extends BaseTask
	{
		@Override
		public void run()
		{
			try
			{
				unregisterAllGameServers();
			}
			catch (SQLException e)
			{
				showError(_bundle.getString("sqlErrorUnregisterAll"), e);
			}
		}
	}
}
