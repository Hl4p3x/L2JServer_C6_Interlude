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
package org.l2jserver.telnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.enums.ServerMode;
import org.l2jserver.commons.util.Rnd;

public class TelnetStatusThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(TelnetStatusThread.class.getName());
	
	private final ServerSocket statusServerSocket;
	
	private final int _uptime;
	private final int _statusPort;
	private String _statusPw;
	private final List<LoginStatusThread> _loginStatus;
	
	@Override
	public void run()
	{
		setPriority(Thread.MAX_PRIORITY);
		
		while (true)
		{
			try
			{
				final Socket connection = statusServerSocket.accept();
				if (Config.SERVER_MODE == ServerMode.GAME)
				{
					final GameStatusThread gst = new GameStatusThread(connection, _uptime, _statusPw);
					if (!connection.isClosed())
					{
						ThreadPool.execute(gst);
					}
				}
				else if (Config.SERVER_MODE == ServerMode.LOGIN)
				{
					final LoginStatusThread lst = new LoginStatusThread(connection, _uptime, _statusPw);
					if (!connection.isClosed())
					{
						ThreadPool.execute(lst);
						_loginStatus.add(lst);
					}
				}
				if (isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch (IOException io)
					{
						LOGGER.warning(io.toString());
					}
					break;
				}
			}
			catch (IOException e)
			{
				if (isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch (IOException io)
					{
						LOGGER.warning(io.toString());
					}
					break;
				}
			}
		}
	}
	
	public TelnetStatusThread() throws IOException
	{
		super("Status");
		final Properties telnetSettings = new Properties();
		final InputStream is = new FileInputStream(new File(Config.TELNET_CONFIG_FILE));
		telnetSettings.load(is);
		is.close();
		
		_statusPort = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
		_statusPw = telnetSettings.getProperty("StatusPW");
		if ((Config.SERVER_MODE == ServerMode.GAME) || (Config.SERVER_MODE == ServerMode.LOGIN))
		{
			if (_statusPw == null)
			{
				LOGGER.info("Server's Telnet Function Has No Password Defined!");
				LOGGER.info("A Password Has Been Automaticly Created!");
				_statusPw = rndPW(10);
				LOGGER.info("Password Has Been Set To: " + _statusPw);
			}
			LOGGER.info("Telnet StatusServer started successfully, listening on Port: " + _statusPort);
		}
		
		statusServerSocket = new ServerSocket(_statusPort);
		_uptime = (int) System.currentTimeMillis();
		_loginStatus = new ArrayList<>();
	}
	
	private String rndPW(int length)
	{
		final String lowerChar = "qwertyuiopasdfghjklzxcvbnm";
		final String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
		final String digits = "1234567890";
		final StringBuilder password = new StringBuilder(length);
		for (int i = 0; i < length; i++)
		{
			final int charSet = Rnd.get(3);
			switch (charSet)
			{
				case 0:
				{
					password.append(lowerChar.charAt(Rnd.get(lowerChar.length() - 1)));
					break;
				}
				case 1:
				{
					password.append(upperChar.charAt(Rnd.get(upperChar.length() - 1)));
					break;
				}
				case 2:
				{
					password.append(digits.charAt(Rnd.get(digits.length() - 1)));
					break;
				}
			}
		}
		return password.toString();
	}
	
	public void sendMessageToTelnets(String msg)
	{
		final List<LoginStatusThread> lsToRemove = new ArrayList<>();
		for (LoginStatusThread ls : _loginStatus)
		{
			if (ls.isInterrupted())
			{
				lsToRemove.add(ls);
			}
			else
			{
				ls.printToTelnet(msg);
			}
		}
	}
}
