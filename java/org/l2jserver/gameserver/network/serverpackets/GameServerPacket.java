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
package org.l2jserver.gameserver.network.serverpackets;

import java.util.logging.Logger;

import org.l2jserver.commons.mmocore.SendablePacket;
import org.l2jserver.gameserver.network.GameClient;

/**
 * @author KenM
 */
public abstract class GameServerPacket extends SendablePacket<GameClient>
{
	protected static final Logger LOGGER = Logger.getLogger(GameServerPacket.class.getName());
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (Exception e)
		{
			LOGGER.severe("Client: " + getClient() + " - Failed writing: " + getType() + " ; " + e.getMessage() + " " + e);
		}
	}
	
	public void runImpl()
	{
	}
	
	protected abstract void writeImpl();
	
	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}
}
