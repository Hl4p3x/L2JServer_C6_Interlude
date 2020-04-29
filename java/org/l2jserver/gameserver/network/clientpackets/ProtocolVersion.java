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

import org.l2jserver.Config;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.KeyPacket;

public class ProtocolVersion extends GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(ProtocolVersion.class.getName());
	private int _version;
	
	@Override
	protected void readImpl()
	{
		_version = readH();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_version == 65534) || (_version == -2)) // Ping
		{
			getClient().close((GameServerPacket) null);
		}
		else if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION))
		{
			LOGGER.info("Client: " + getClient() + " -> Protocol Revision: " + _version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is " + Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
			LOGGER.warning("Wrong Protocol Version " + _version);
			getClient().close((GameServerPacket) null);
		}
		else
		{
			getClient().setProtocolVersion(_version);
			getClient().sendPacket(new KeyPacket(getClient().enableCrypt()));
		}
	}
}