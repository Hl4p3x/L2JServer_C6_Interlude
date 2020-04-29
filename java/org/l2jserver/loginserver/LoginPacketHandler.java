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

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.l2jserver.commons.mmocore.IPacketHandler;
import org.l2jserver.commons.mmocore.ReceivablePacket;
import org.l2jserver.loginserver.LoginClient.LoginClientState;
import org.l2jserver.loginserver.network.clientpackets.AuthGameGuard;
import org.l2jserver.loginserver.network.clientpackets.RequestAuthLogin;
import org.l2jserver.loginserver.network.clientpackets.RequestServerList;
import org.l2jserver.loginserver.network.clientpackets.RequestServerLogin;

/**
 * Handler for packets received by Login Server
 * @author KenM
 */

public class LoginPacketHandler implements IPacketHandler<LoginClient>
{
	private static final Logger LOGGER = Logger.getLogger(LoginPacketHandler.class.getName());
	
	@Override
	public ReceivablePacket<LoginClient> handlePacket(ByteBuffer buf, LoginClient client)
	{
		final int opcode = buf.get() & 0xFF;
		ReceivablePacket<LoginClient> packet = null;
		final LoginClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
			{
				if (opcode == 0x07)
				{
					packet = new AuthGameGuard();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			}
			case AUTHED_GG:
			{
				if (opcode == 0x00)
				{
					packet = new RequestAuthLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			}
			case AUTHED_LOGIN:
			{
				if (opcode == 0x05)
				{
					packet = new RequestServerList();
				}
				else if (opcode == 0x02)
				{
					packet = new RequestServerLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			}
		}
		return packet;
	}
	
	private void debugOpcode(int opcode, LoginClientState state)
	{
		LOGGER.info("Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}