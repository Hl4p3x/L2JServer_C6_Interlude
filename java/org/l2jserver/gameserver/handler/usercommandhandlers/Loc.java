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
package org.l2jserver.gameserver.handler.usercommandhandlers;

import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.handler.IUserCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Loc implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	@Override
	public boolean useUserCommand(int id, PlayerInstance player)
	{
		final int _nearestTown = MapRegionData.getInstance().getClosestTownNumber(player);
		SystemMessageId msg;
		
		switch (_nearestTown)
		{
			case 0:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_TALKING_ISLAND_VILLAGE;
				break;
			}
			case 1:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_ELVEN_VILLAGE;
				break;
			}
			case 2:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_DARK_ELF_VILLAGE;
				break;
			}
			case 3:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_ORC_VILLAGE;
				break;
			}
			case 4:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_DWARVEN_VILLAGE;
				break;
			}
			case 5:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GLUDIO;
				break;
			}
			case 6:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_GLUDIN_VILLAGE;
				break;
			}
			case 7:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_DION;
				break;
			}
			case 8:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GIRAN;
				break;
			}
			case 9:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_OREN;
				break;
			}
			case 10:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_ADEN_CASTLE_TOWN;
				break;
			}
			case 11:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_HUNTERS_VILLAGE;
				break;
			}
			case 12:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_GIRAN_HARBOR;
				break;
			}
			case 13:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_HEINE;
				break;
			}
			case 14:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_RUNE_VILLAGE;
				break;
			}
			case 15:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GODDARD;
				break;
			}
			case 16:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_SCHUTTGART;
				break;
			}
			case 17:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_FLORAN_VILLAGE;
				break;
			}
			case 18:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_THE_PRIMEVAL_ISLE;
				break;
			}
			default:
			{
				msg = SystemMessageId.CURRENT_LOCATION_S1_S2_S3_NEAR_ADEN_CASTLE_TOWN;
			}
		}
		
		final SystemMessage sm = new SystemMessage(msg);
		sm.addNumber(player.getX());
		sm.addNumber(player.getY());
		sm.addNumber(player.getZ());
		player.sendPacket(sm);
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
