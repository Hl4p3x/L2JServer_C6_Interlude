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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.Config;

public class BruteProtector
{
	private static final Logger LOGGER = Logger.getLogger(BruteProtector.class.getName());
	private static final Map<String, ArrayList<Integer>> _clients = new HashMap<>();
	
	public static boolean canLogin(String ip)
	{
		if (!_clients.containsKey(ip))
		{
			_clients.put(ip, new ArrayList<Integer>());
			_clients.get(ip).add((int) (System.currentTimeMillis() / 1000));
			return true;
		}
		
		_clients.get(ip).add((int) (System.currentTimeMillis() / 1000));
		
		/*
		 * I am not quite sure because we can have a number of NATed clients with single IP if (currentAttemptTime - lastAttemptTime <= 2) // Time between last login attempt and current less or equal than 2 seconds return false;
		 */
		if (_clients.get(ip).size() < Config.BRUT_LOGON_ATTEMPTS)
		{
			return true;
		}
		
		// Calculating average time difference between attempts
		int lastTime = 0;
		int avg = 0;
		for (int i : _clients.get(ip))
		{
			if (lastTime == 0)
			{
				lastTime = i;
				continue;
			}
			avg += i - lastTime;
			lastTime = i;
		}
		avg = avg / (_clients.get(ip).size() - 1);
		
		// Minimum average time difference (in seconds) between attempts
		if (avg < Config.BRUT_AVG_TIME)
		{
			LOGGER.warning("IP " + ip + " has " + avg + " seconds between login attempts. Possible BruteForce.");
			// Deleting 2 first elements because if ban will disappear user should have a possibility to logon
			synchronized (_clients.get(ip))
			{
				_clients.get(ip).remove(0);
				_clients.get(ip).remove(0);
			}
			
			return false; // IP have to be banned
		}
		
		synchronized (_clients.get(ip))
		{
			_clients.get(ip).remove(0);
		}
		
		return true;
	}
}
