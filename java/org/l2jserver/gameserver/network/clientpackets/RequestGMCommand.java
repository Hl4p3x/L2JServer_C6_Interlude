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

import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.GMViewCharacterInfo;
import org.l2jserver.gameserver.network.serverpackets.GMViewHennaInfo;
import org.l2jserver.gameserver.network.serverpackets.GMViewItemList;
import org.l2jserver.gameserver.network.serverpackets.GMViewPledgeInfo;
import org.l2jserver.gameserver.network.serverpackets.GMViewQuestList;
import org.l2jserver.gameserver.network.serverpackets.GMViewSkillInfo;
import org.l2jserver.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public class RequestGMCommand extends GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestGMCommand.class.getName());
	
	private String _targetName;
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		// _unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = World.getInstance().getPlayer(_targetName);
		
		// prevent non GM or low level GMs from vieweing player stuff
		if ((player == null) || !getClient().getPlayer().getAccessLevel().allowAltG())
		{
			return;
		}
		
		switch (_command)
		{
			case 1: // player status
			{
				sendPacket(new GMViewCharacterInfo(player));
				sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 2: // player clan
			{
				if (player.getClan() != null)
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3: // player skills
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				sendPacket(new GMViewQuestList(player));
				break;
			}
			case 5: // player inventory
			{
				sendPacket(new GMViewItemList(player));
				sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 6: // player warehouse
			{
				// GM warehouse view to be implemented
				sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}
		}
	}
}
