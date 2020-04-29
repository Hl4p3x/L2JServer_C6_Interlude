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
package org.l2jserver.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jserver.commons.mmocore.ReceivablePacket;
import org.l2jserver.gameserver.network.GameClient;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;

/**
 * Packets received by the game server from clients
 * @author KenM
 */
public abstract class GameClientPacket extends ReceivablePacket<GameClient>
{
	protected static final Logger LOGGER = Logger.getLogger(GameClientPacket.class.getName());
	
	@Override
	protected boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (Exception e)
		{
			LOGGER.severe("Client: " + getClient() + " - Failed reading: " + getType() + " ; " + e.getMessage() + " " + e);
		}
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public void run()
	{
		try
		{
			runImpl();
			if ((this instanceof MoveBackwardToLocation) || (this instanceof AttackRequest) || (this instanceof RequestMagicSkillUse))
			{
				if (getClient().getPlayer() != null)
				{
					getClient().getPlayer().onActionRequest(); // Removes onSpawn Protection
				}
			}
		}
		catch (Throwable t)
		{
			LOGGER.severe("Client: " + getClient() + " - Failed reading: " + getType() + " ; " + t.getMessage() + " " + t);
			if (this instanceof EnterWorld)
			{
				getClient().closeNow();
			}
		}
	}
	
	protected abstract void runImpl();
	
	/**
	 * Sends a game server packet to the client.
	 * @param gsp the game server packet
	 */
	protected final void sendPacket(GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}
}