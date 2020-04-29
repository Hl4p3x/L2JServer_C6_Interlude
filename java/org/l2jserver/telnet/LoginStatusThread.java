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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.loginserver.GameServerTable;
import org.l2jserver.loginserver.LoginController;
import org.l2jserver.loginserver.LoginServer;

public class LoginStatusThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(LoginStatusThread.class.getName());
	
	private final Socket _cSocket;
	
	private final PrintWriter _print;
	private final BufferedReader _read;
	
	private boolean _redirectLogger;
	
	private void telnetOutput(int type, String text)
	{
		if (type == 1)
		{
			LOGGER.info("TELNET | " + text);
		}
		else if (type == 2)
		{
			System.out.print("TELNET | " + text);
		}
		else if (type == 3)
		{
			System.out.print(text);
		}
		else if (type == 4)
		{
			LOGGER.info(text);
		}
		else
		{
			LOGGER.info("TELNET | " + text);
		}
	}
	
	private boolean isValidIP(Socket client)
	{
		boolean result = false;
		final InetAddress clientIP = client.getInetAddress();
		
		// convert IP to String, and compare with list
		final String clientStringIP = clientIP.getHostAddress();
		telnetOutput(1, "Connection from: " + clientStringIP);
		
		// read and loop thru list of IPs, compare with newIP
		InputStream telnetIS = null;
		try
		{
			final Properties telnetSettings = new Properties();
			telnetIS = new FileInputStream(new File(Config.TELNET_CONFIG_FILE));
			telnetSettings.load(telnetIS);
			
			final String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost,::1");
			
			// compare
			String ipToCompare = null;
			for (String ip : HostList.split(","))
			{
				if (!result)
				{
					ipToCompare = InetAddress.getByName(ip).getHostAddress();
					if (clientStringIP.equals(ipToCompare))
					{
						result = true;
					}
				}
			}
		}
		catch (IOException e)
		{
			telnetOutput(1, "Error: " + e);
		}
		finally
		{
			if (telnetIS != null)
			{
				try
				{
					telnetIS.close();
				}
				catch (Exception e)
				{
					LOGGER.warning(e.toString());
				}
			}
		}
		return result;
	}
	
	public LoginStatusThread(Socket client, int uptime, String statusPW) throws IOException
	{
		_cSocket = client;
		_print = new PrintWriter(_cSocket.getOutputStream());
		_read = new BufferedReader(new InputStreamReader(_cSocket.getInputStream()));
		if (isValidIP(client))
		{
			telnetOutput(1, client.getInetAddress().getHostAddress() + " accepted.");
			_print.println("Welcome To The L2J Telnet Session.");
			_print.println("Please Insert Your Password!");
			_print.print("Password: ");
			_print.flush();
			final String tmpLine = _read.readLine();
			if (tmpLine == null)
			{
				_print.println("Error.");
				_print.println("Disconnected...");
				_print.flush();
				_cSocket.close();
			}
			else if (tmpLine.compareTo(statusPW) != 0)
			{
				_print.println("Incorrect Password!");
				_print.println("Disconnected...");
				_print.flush();
				_cSocket.close();
			}
			else
			{
				_print.println("Password Correct!");
				_print.println("[L2J Login Server]");
				_print.print("");
				_print.flush();
			}
		}
		else
		{
			telnetOutput(5, "Connection attempt from " + client.getInetAddress().getHostAddress() + " rejected.");
			_cSocket.close();
		}
	}
	
	@Override
	public void run()
	{
		String usrCommand = "";
		try
		{
			while ((usrCommand.compareTo("quit") != 0) && (usrCommand.compareTo("exit") != 0))
			{
				usrCommand = _read.readLine();
				if (usrCommand == null)
				{
					_cSocket.close();
					break;
				}
				if (usrCommand.equals("help"))
				{
					_print.println("The following is a list of all available commands: ");
					_print.println("help                - shows this help.");
					_print.println("status              - displays basic server statistics.");
					_print.println("unblock <ip>        - removes <ip> from banlist.");
					_print.println("shutdown			- shuts down server.");
					_print.println("restart				- restarts the server.");
					_print.println("RedirectLogger		- Telnet will give you some info about server in real time.");
					_print.println("quit                - closes telnet session.");
					_print.println("");
				}
				else if (usrCommand.equals("status"))
				{
					// TODO enhance the output
					_print.println("Registered Server Count: " + GameServerTable.getInstance().getRegisteredGameServers().size());
				}
				else if (usrCommand.startsWith("unblock"))
				{
					try
					{
						usrCommand = usrCommand.substring(8);
						if (LoginController.getInstance().removeBanForAddress(usrCommand))
						{
							LOGGER.warning("IP removed via TELNET by host: " + _cSocket.getInetAddress().getHostAddress());
							_print.println("The IP " + usrCommand + " has been removed from the hack protection list!");
						}
						else
						{
							_print.println("IP not found in hack protection list...");
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter the IP to Unblock!");
					}
				}
				else if (usrCommand.startsWith("shutdown"))
				{
					LoginServer.getInstance().shutdown(false);
					_print.println("Bye Bye!");
					_print.flush();
					_cSocket.close();
				}
				else if (usrCommand.startsWith("restart"))
				{
					LoginServer.getInstance().shutdown(true);
					_print.println("Bye Bye!");
					_print.flush();
					_cSocket.close();
				}
				else if (usrCommand.equals("RedirectLogger"))
				{
					_redirectLogger = true;
				}
				else if (usrCommand.equals("quit"))
				{ /* Do Nothing :p - Just here to save us from the "Command Not Understood" Text */
				}
				else if (usrCommand.length() == 0)
				{ /* Do Nothing Again - Same reason as the quit part */
				}
				else
				{
					_print.println("Invalid Command");
				}
				_print.print("");
				_print.flush();
			}
			if (!_cSocket.isClosed())
			{
				_print.println("Bye Bye!");
				_print.flush();
				_cSocket.close();
			}
			telnetOutput(1, "Connection from " + _cSocket.getInetAddress().getHostAddress() + " was closed by client.");
		}
		catch (IOException e)
		{
			LOGGER.warning(e.toString());
		}
	}
	
	public void printToTelnet(String msg)
	{
		synchronized (_print)
		{
			_print.println(msg);
			_print.flush();
		}
	}
	
	/**
	 * @return Returns the redirectLogger.
	 */
	public boolean isRedirectLogger()
	{
		return _redirectLogger;
	}
}
