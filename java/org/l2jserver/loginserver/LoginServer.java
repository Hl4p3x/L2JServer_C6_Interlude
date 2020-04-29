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
package org.l2jserver.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseBackup;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.enums.ServerMode;
import org.l2jserver.commons.mmocore.SelectorConfig;
import org.l2jserver.commons.mmocore.SelectorThread;
import org.l2jserver.loginserver.network.gameserverpackets.ServerStatus;
import org.l2jserver.telnet.TelnetStatusThread;

/**
 * @author KenM
 */
public class LoginServer
{
	public Logger LOGGER = Logger.getLogger(LoginServer.class.getName());
	
	public static final int PROTOCOL_REV = 0x0102;
	private static LoginServer INSTANCE;
	private GameServerListener _gameServerListener;
	private SelectorThread<LoginClient> _selectorThread;
	private TelnetStatusThread _statusServer;
	private static int _loginStatus = ServerStatus.STATUS_NORMAL;
	
	private LoginServer() throws Exception
	{
		// Create log folder
		final File logFolder = new File(".", "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		
		try (InputStream is = new FileInputStream(new File("./log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		catch (IOException e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		// Load Config
		Config.load(ServerMode.LOGIN);
		
		// Prepare Database
		DatabaseFactory.init();
		
		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.severe("FATAL: Failed initializing LoginController. Reason: " + e);
			System.exit(1);
		}
		
		try
		{
			GameServerTable.getInstance();
		}
		catch (Exception e)
		{
			LOGGER.severe("Failed to load GameServerTable" + e);
			System.exit(1);
		}
		
		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch (UnknownHostException e1)
			{
				LOGGER.warning("WARNING: The LoginServer bind address is invalid, using all avaliable IPs " + e1);
			}
		}
		// Load telnet status
		if (Config.IS_TELNET_ENABLED)
		{
			try
			{
				_statusServer = new TelnetStatusThread();
				_statusServer.start();
			}
			catch (IOException e)
			{
				LOGGER.warning("Failed to start the Telnet Server. Reason: " + e.getMessage());
			}
		}
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		final LoginPacketHandler lph = new LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		}
		catch (IOException e)
		{
			LOGGER.severe("Failed to open Selector " + e);
			System.exit(1);
		}
		
		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			LOGGER.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException e)
		{
			LOGGER.severe("Failed to start the Game Server Listener" + e);
			System.exit(1);
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
			_selectorThread.start();
			LOGGER.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);
		}
		catch (IOException e)
		{
			LOGGER.warning("Failed to open server socket" + e);
			System.exit(1);
		}
		
		// load bannedIps
		Config.loadBanFile();
	}
	
	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	public void shutdown(boolean restart)
	{
		// Backup database.
		if (Config.BACKUP_DATABASE)
		{
			DatabaseBackup.performBackup();
		}
		
		LoginController.getInstance().shutdown();
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
	
	public int getStatus()
	{
		return _loginStatus;
	}
	
	public void setStatus(int status)
	{
		_loginStatus = status;
	}
	
	public static void main(String[] args) throws Exception
	{
		INSTANCE = new LoginServer();
	}
	
	public static LoginServer getInstance()
	{
		return INSTANCE;
	}
}
