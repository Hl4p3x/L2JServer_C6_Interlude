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
package org.l2jserver.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.model.AccessLevel;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Loads administrator access levels and commands.
 * @author Mobius
 */
public class AdminData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(AdminData.class.getName());
	
	private final Map<Integer, AccessLevel> _accessLevels = new HashMap<>();
	private final Map<String, Integer> _adminCommandAccessRights = new HashMap<>();
	private final Map<PlayerInstance, Boolean> _gmList = new ConcurrentHashMap<>();
	
	protected AdminData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_accessLevels.clear();
		parseDatapackFile("config/AccessLevels.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _accessLevels.size() + " access levels.");
		_adminCommandAccessRights.clear();
		parseDatapackFile("config/AdminCommands.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _adminCommandAccessRights.size() + " access commands.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		StatSet set = null;
		String command = null;
		int accessLevel = 0;
		String name = null;
		int nameColor = 0;
		int titleColor = 0;
		boolean isGm = false;
		boolean allowPeaceAttack = false;
		boolean allowFixedRes = false;
		boolean allowTransaction = false;
		boolean allowAltG = false;
		boolean giveDamage = false;
		boolean takeAggro = false;
		boolean gainExp = false;
		boolean useNameColor = true;
		boolean useTitleColor = false;
		boolean canDisableGmStatus = true;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("access".equals(d.getNodeName()))
					{
						set = new StatSet(parseAttributes(d));
						accessLevel = set.getInt("level");
						name = set.getString("name");
						if (accessLevel < 0)
						{
							LOGGER.info(getClass().getSimpleName() + ": Access level with name " + name + " is using banned access level state(below 0). Ignoring it...");
							continue;
						}
						
						try
						{
							nameColor = Integer.decode("0x" + set.getString("nameColor"));
						}
						catch (NumberFormatException nfe)
						{
							LOGGER.warning(nfe.getMessage());
							
							try
							{
								nameColor = Integer.decode("0xFFFFFF");
							}
							catch (NumberFormatException nfe2)
							{
								LOGGER.warning(nfe.getMessage());
							}
						}
						
						try
						{
							titleColor = Integer.decode("0x" + set.getString("titleColor"));
						}
						catch (NumberFormatException nfe)
						{
							LOGGER.warning(nfe.getMessage());
							
							try
							{
								titleColor = Integer.decode("0x77FFFF");
							}
							catch (NumberFormatException nfe2)
							{
								LOGGER.warning(nfe.getMessage());
							}
						}
						
						isGm = set.getBoolean("isGm");
						allowPeaceAttack = set.getBoolean("allowPeaceAttack");
						allowFixedRes = set.getBoolean("allowFixedRes");
						allowTransaction = set.getBoolean("allowTransaction");
						allowAltG = set.getBoolean("allowAltg");
						giveDamage = set.getBoolean("giveDamage");
						takeAggro = set.getBoolean("takeAggro");
						gainExp = set.getBoolean("gainExp");
						useNameColor = set.getBoolean("useNameColor");
						useTitleColor = set.getBoolean("useTitleColor");
						canDisableGmStatus = set.getBoolean("canDisableGmStatus");
						_accessLevels.put(accessLevel, new AccessLevel(accessLevel, name, nameColor, titleColor, isGm, allowPeaceAttack, allowFixedRes, allowTransaction, allowAltG, giveDamage, takeAggro, gainExp, useNameColor, useTitleColor, canDisableGmStatus));
					}
					else if ("admin".equals(d.getNodeName()))
					{
						set = new StatSet(parseAttributes(d));
						command = set.getString("command");
						accessLevel = set.getInt("accessLevel");
						_adminCommandAccessRights.put(command, accessLevel);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the access level by characterAccessLevel
	 * @param accessLevelNum as int<br>
	 * @return AccessLevel: AccessLevel instance by char access level
	 */
	public AccessLevel getAccessLevel(int accessLevelNum)
	{
		AccessLevel accessLevel = null;
		synchronized (_accessLevels)
		{
			accessLevel = _accessLevels.get(accessLevelNum);
		}
		return accessLevel;
	}
	
	public void addBanAccessLevel(int accessLevel)
	{
		synchronized (_accessLevels)
		{
			if (accessLevel > -1)
			{
				return;
			}
			_accessLevels.put(accessLevel, new AccessLevel(accessLevel, "Banned", Integer.decode("0x000000"), Integer.decode("0x000000"), false, false, false, false, false, false, false, false, false, false, false));
		}
	}
	
	public int accessRightForCommand(String command)
	{
		int out = -1;
		if (_adminCommandAccessRights.containsKey(command))
		{
			out = _adminCommandAccessRights.get(command);
		}
		return out;
	}
	
	public boolean hasAccess(String adminCommand, AccessLevel accessLevel)
	{
		if (accessLevel.getLevel() <= 0)
		{
			return false;
		}
		
		if (!accessLevel.isGm())
		{
			return false;
		}
		
		String command = adminCommand;
		if (adminCommand.indexOf(' ') != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(' '));
		}
		
		int acar = 0;
		if (_adminCommandAccessRights.get(command) != null)
		{
			acar = _adminCommandAccessRights.get(command);
		}
		
		if (acar == 0)
		{
			LOGGER.warning("Admin Access Rights: No rights defined for admin command " + command + ".");
			return false;
		}
		
		return accessLevel.getLevel() >= acar;
	}
	
	public List<PlayerInstance> getAllGms(boolean includeHidden)
	{
		final List<PlayerInstance> tmpGmList = new ArrayList<>();
		for (Entry<PlayerInstance, Boolean> n : _gmList.entrySet())
		{
			if (includeHidden || !n.getValue())
			{
				tmpGmList.add(n.getKey());
			}
		}
		return tmpGmList;
	}
	
	public List<String> getAllGmNames(boolean includeHidden)
	{
		final List<String> tmpGmList = new ArrayList<>();
		for (Entry<PlayerInstance, Boolean> n : _gmList.entrySet())
		{
			if (!n.getValue())
			{
				tmpGmList.add(n.getKey().getName());
			}
			else if (includeHidden)
			{
				tmpGmList.add(n.getKey().getName() + " (invis)");
			}
		}
		return tmpGmList;
	}
	
	public void addGm(PlayerInstance player, boolean hidden)
	{
		_gmList.put(player, hidden);
	}
	
	public void deleteGm(PlayerInstance player)
	{
		_gmList.remove(player);
	}
	
	/**
	 * GM will be displayed on clients GM list.
	 * @param player the player
	 */
	public void showGm(PlayerInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, false);
		}
	}
	
	/**
	 * GM will no longer be displayed on clients GM list.
	 * @param player the player
	 */
	public void hideGm(PlayerInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, true);
		}
	}
	
	public boolean isGmOnline(boolean includeHidden)
	{
		for (boolean value : _gmList.values())
		{
			if (includeHidden || !value)
			{
				return true;
			}
		}
		return false;
	}
	
	public void sendListToPlayer(PlayerInstance player)
	{
		if (isGmOnline(player.isGM()))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.GM_LIST));
			for (String name : getAllGmNames(player.isGM()))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.GM_S1);
				sm.addString(name);
				player.sendPacket(sm);
			}
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessageId.THERE_ARE_NO_GMS_CURRENTLY_VISIBLE_IN_THE_PUBLIC_LIST_AS_THEY_MAY_BE_PERFORMING_OTHER_FUNCTIONS_AT_THE_MOMENT));
		}
	}
	
	public static void broadcastToGMs(GameServerPacket packet)
	{
		for (PlayerInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}
	
	public static void broadcastMessageToGMs(String message)
	{
		for (PlayerInstance gm : getInstance().getAllGms(true))
		{
			if (gm != null)
			{
				gm.sendPacket(SystemMessage.sendString(message));
			}
		}
	}
	
	public static AdminData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminData INSTANCE = new AdminData();
	}
}
