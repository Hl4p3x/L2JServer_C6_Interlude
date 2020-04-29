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

import org.l2jserver.gameserver.model.ShortCut;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.ShortCutRegister;

public class RequestShortCutReg extends GameClientPacket
{
	private int _type;
	private int _id;
	private int _slot;
	private int _page;
	@SuppressWarnings("unused")
	private int _unk;
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		final int slot = readD();
		_id = readD();
		_unk = readD();
		_slot = slot % 12;
		_page = slot / 12;
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		switch (_type)
		{
			case 0x01: // item
			case 0x03: // action
			case 0x04: // macro
			case 0x05: // recipe
			{
				final ShortCut sc = new ShortCut(_slot, _page, _type, _id, -1);
				sendPacket(new ShortCutRegister(sc));
				player.registerShortCut(sc);
				break;
			}
			case 0x02: // skill
			{
				final int level = player.getSkillLevel(_id);
				if (level > 0)
				{
					final ShortCut sc = new ShortCut(_slot, _page, _type, _id, level);
					sendPacket(new ShortCutRegister(sc));
					player.registerShortCut(sc);
				}
				break;
			}
		}
	}
}