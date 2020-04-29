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

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.GameClient.GameClientState;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.CharSelected;

@SuppressWarnings("unused")
public class CharacterSelected extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(CharacterSelected.class.getName());
	
	private int _charSlot;
	private int _unk1; // new in C4
	private int _unk2;
	private int _unk3;
	private int _unk4;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// if there is a playback.dat file in the current directory, it will be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to be a [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		// playLogFile(getConnection()); // try to play LOGGER file
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
		{
			return;
		}
		
		// we should always be abble to acquire the lock but if we cant lock then nothing should be done (ie repeated packet)
		if (getClient().getPlayerLock().tryLock())
		{
			try
			{
				// should always be null but if not then this is repeated packet and nothing should be done here
				if (getClient().getPlayer() == null)
				{
					// Load up character from disk
					final PlayerInstance cha = getClient().loadCharFromDisk(_charSlot);
					if (cha == null)
					{
						LOGGER.warning(getType() + ": Character could not be loaded (slot:" + _charSlot + ")");
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					if (cha.getAccessLevel().getLevel() < 0)
					{
						cha.deleteMe();
						return;
					}
					
					cha.setClient(getClient());
					getClient().setPlayer(cha);
					getClient().setState(GameClientState.ENTERING);
					sendPacket(new CharSelected(cha, getClient().getSessionId().playOkID1));
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(e.toString());
			}
			finally
			{
				getClient().getPlayerLock().unlock();
			}
		}
	}
}