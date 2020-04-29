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

import org.l2jserver.gameserver.handler.IUserCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Support for /olympiadstat command Added by kamy
 */
public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		109
	};
	
	@Override
	public boolean useUserCommand(int id, PlayerInstance player)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_CURRENT_RECORD_FOR_THIS_GRAND_OLYMPIAD_IS_S1_MATCH_ES_S2_WIN_S_AND_S3_DEFEAT_S_YOU_HAVE_EARNED_S4_OLYMPIAD_POINT_S);
		sm.addNumber(Olympiad.getInstance().getCompetitionDone(player.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getCompetitionWon(player.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getCompetitionLost(player.getObjectId()));
		sm.addNumber(Olympiad.getInstance().getNoblePoints(player.getObjectId()));
		player.sendPacket(sm);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
