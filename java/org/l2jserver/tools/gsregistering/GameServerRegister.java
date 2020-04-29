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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.l2jserver.Config;
import org.l2jserver.loginserver.GameServerTable;

public class GameServerRegister extends BaseGameServerRegister
{
	private LineNumberReader _in;
	
	public static void main(String[] args)
	{
		// Backwards compatibility, redirect to the new one
		BaseGameServerRegister.main(args);
	}
	
	public GameServerRegister()
	{
		super();
		load();
		
		if (GameServerTable.getInstance().getServerNames().isEmpty())
		{
			System.out.println("No available names for GameServer, verify servername.xml file exists in the LoginServer folder.");
			System.exit(1);
		}
	}
	
	public void consoleUI() throws IOException
	{
		_in = new LineNumberReader(new InputStreamReader(System.in));
		boolean choiceOk = false;
		String choice;
		
		while (true)
		{
			hr();
			System.out.println("GSRegister");
			System.out.println(Config.EOL);
			System.out.println("1 - Register GameServer");
			System.out.println("2 - List GameServers Names and IDs");
			System.out.println("3 - Remove GameServer");
			System.out.println("4 - Remove ALL GameServers");
			System.out.println("5 - Exit");
			
			do
			{
				System.out.print("Choice: ");
				choice = _in.readLine();
				try
				{
					final int choiceNumber = Integer.parseInt(choice);
					choiceOk = true;
					
					switch (choiceNumber)
					{
						case 1:
						{
							registerNewGS();
							break;
						}
						case 2:
						{
							listGSNames();
							break;
						}
						case 3:
						{
							unregisterSingleGS();
							break;
						}
						case 4:
						{
							unregisterAllGS();
							break;
						}
						case 5:
						{
							System.exit(0);
							break;
						}
						default:
						{
							System.out.printf("Invalid Choice: %s" + Config.EOL, choice);
							choiceOk = false;
						}
					}
				}
				catch (NumberFormatException nfe)
				{
					System.out.printf("Invalid Choice: %s" + Config.EOL, choice);
				}
			}
			while (!choiceOk);
		}
	}
	
	/**
	 * 
	 */
	private void hr()
	{
		System.out.println("_____________________________________________________" + Config.EOL);
	}
	
	/**
	 * 
	 */
	private void listGSNames()
	{
		int idMaxLen = 0;
		int nameMaxLen = 0;
		for (Entry<Integer, String> e : GameServerTable.getInstance().getServerNames().entrySet())
		{
			if (e.getKey().toString().length() > idMaxLen)
			{
				idMaxLen = e.getKey().toString().length();
			}
			if (e.getValue().length() > nameMaxLen)
			{
				nameMaxLen = e.getValue().length();
			}
		}
		idMaxLen += 2;
		nameMaxLen += 2;
		
		String id;
		boolean inUse;
		final String gsInUse = "In Use";
		final String gsFree = "Free";
		final int gsStatusMaxLen = Math.max(gsInUse.length(), gsFree.length()) + 2;
		for (Entry<Integer, String> e : GameServerTable.getInstance().getServerNames().entrySet())
		{
			id = e.getKey().toString();
			System.out.print(id);
			
			for (int i = id.length(); i < idMaxLen; i++)
			{
				System.out.print(' ');
			}
			System.out.print("| ");
			
			System.out.print(e.getValue());
			
			for (int i = e.getValue().length(); i < nameMaxLen; i++)
			{
				System.out.print(' ');
			}
			System.out.print("| ");
			
			inUse = GameServerTable.getInstance().hasRegisteredGameServerOnId(e.getKey());
			final String inUseStr = inUse ? gsInUse : gsFree;
			System.out.print(inUseStr);
			
			for (int i = inUseStr.length(); i < gsStatusMaxLen; i++)
			{
				System.out.print(' ');
			}
			System.out.println('|');
		}
	}
	
	/**
	 * @throws IOException
	 */
	private void unregisterAllGS() throws IOException
	{
		if (yesNoQuestion("Are you sure you want to remove ALL GameServers?"))
		{
			try
			{
				BaseGameServerRegister.unregisterAllGameServers();
				System.out.println("All GameServers were successfully removed.");
			}
			catch (SQLException e)
			{
				showError("An SQL error occurred while trying to remove ALL GameServers.", e);
			}
		}
	}
	
	private boolean yesNoQuestion(String question) throws IOException
	{
		do
		{
			hr();
			System.out.println(question);
			System.out.println("1 - Yes");
			System.out.println("2 - No");
			System.out.print("Choice: ");
			String choice;
			choice = _in.readLine();
			if (choice != null)
			{
				if (choice.equals("1"))
				{
					return true;
				}
				else if (choice.equals("2"))
				{
					return false;
				}
				else
				{
					System.out.printf("Invalid Choice: %s" + Config.EOL, choice);
				}
			}
		}
		while (true);
	}
	
	/**
	 * @throws IOException
	 */
	private void unregisterSingleGS() throws IOException
	{
		String line;
		int id = Integer.MIN_VALUE;
		
		do
		{
			System.out.print("Enter desired ID: ");
			line = _in.readLine();
			try
			{
				id = Integer.parseInt(line);
			}
			catch (NumberFormatException e)
			{
				System.out.printf("Invalid Choice: %s" + Config.EOL, line);
			}
		}
		while (id == Integer.MIN_VALUE);
		
		final String name = GameServerTable.getInstance().getServerNameById(id);
		if (name == null)
		{
			System.out.printf("No name for ID: %d" + Config.EOL, id);
		}
		else if (GameServerTable.getInstance().hasRegisteredGameServerOnId(id))
		{
			System.out.printf("Are you sure you want to remove GameServer %d - %s?" + Config.EOL, id, name);
			try
			{
				BaseGameServerRegister.unregisterGameServer(id);
				System.out.printf("GameServer ID: %d was successfully removed from LoginServer." + Config.EOL, id);
			}
			catch (SQLException e)
			{
				showError("An SQL error occurred while trying to remove the GameServer.", e);
			}
		}
		else
		{
			System.out.printf("No GameServer is registered on ID: %d" + Config.EOL, id);
		}
	}
	
	private void registerNewGS() throws IOException
	{
		String line;
		int id = Integer.MIN_VALUE;
		
		do
		{
			System.out.println("Enter desired ID:");
			line = _in.readLine();
			try
			{
				id = Integer.parseInt(line);
			}
			catch (NumberFormatException e)
			{
				System.out.printf("Invalid Choice: %s" + Config.EOL, line);
			}
		}
		while (id == Integer.MIN_VALUE);
		
		if (GameServerTable.getInstance().getServerNameById(id) == null)
		{
			System.out.printf("No name for ID: %d" + Config.EOL, id);
		}
		else if (GameServerTable.getInstance().hasRegisteredGameServerOnId(id))
		{
			System.out.println("This ID is not available.");
		}
		else
		{
			try
			{
				BaseGameServerRegister.registerGameServer(id, ".");
			}
			catch (IOException e)
			{
				showError("An error saving the hexid file occurred while trying to register the GameServer.", e);
			}
		}
	}
	
	@Override
	public void showError(String msg, Throwable t)
	{
		msg += Config.EOL + "Reason: " + t.getLocalizedMessage();
		System.out.println("Error: " + msg);
	}
}