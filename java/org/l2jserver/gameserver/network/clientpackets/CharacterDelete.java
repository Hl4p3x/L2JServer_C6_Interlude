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

import org.l2jserver.gameserver.network.serverpackets.CharDeleteFail;
import org.l2jserver.gameserver.network.serverpackets.CharDeleteOk;
import org.l2jserver.gameserver.network.serverpackets.CharSelectInfo;

/**
 * @author eX1steam
 */
public class CharacterDelete extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(CharacterDelete.class.getName());
	private int _charSlot;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterDelete"))
		{
			return;
		}
		
		try
		{
			final byte answer = getClient().markToDeleteChar(_charSlot);
			switch (answer)
			{
				default:
				case -1: // Error
				{
					break;
				}
				case 0: // Success!
				{
					sendPacket(new CharDeleteOk());
					break;
				}
				case 1:
				{
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				}
				case 2:
				{
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("ERROR " + getType() + ": " + e);
		}
		
		final CharSelectInfo cl = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}
