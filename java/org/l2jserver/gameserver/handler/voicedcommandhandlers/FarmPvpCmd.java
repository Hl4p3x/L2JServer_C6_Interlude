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
package org.l2jserver.gameserver.handler.voicedcommandhandlers;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.handler.IVoicedCommandHandler;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.network.serverpackets.SetupGauge;

public class FarmPvpCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"farm1",
		"farm2",
		"pvp1",
		"pvp2"
	};
	
	@Override
	public boolean useVoicedCommand(String command, PlayerInstance activeChar, String target)
	{
		int placex;
		int placey;
		int placez;
		String message;
		if (command.equalsIgnoreCase("farm1") && Config.ALLOW_FARM1_COMMAND)
		{
			placex = Config.FARM1_X;
			placey = Config.FARM1_Y;
			placez = Config.FARM1_Z;
			message = Config.FARM1_CUSTOM_MESSAGE;
		}
		else if (command.equalsIgnoreCase("farm2") && Config.ALLOW_FARM2_COMMAND)
		{
			placex = Config.FARM2_X;
			placey = Config.FARM2_Y;
			placez = Config.FARM2_Z;
			message = Config.FARM2_CUSTOM_MESSAGE;
		}
		else if (command.equalsIgnoreCase("pvp1") && Config.ALLOW_PVP1_COMMAND)
		{
			placex = Config.PVP1_X;
			placey = Config.PVP1_Y;
			placez = Config.PVP1_Z;
			message = Config.PVP1_CUSTOM_MESSAGE;
		}
		else if (command.equalsIgnoreCase("pvp2") && Config.ALLOW_PVP2_COMMAND)
		{
			placex = Config.PVP2_X;
			placey = Config.PVP2_Y;
			placez = Config.PVP2_Z;
			message = Config.PVP2_CUSTOM_MESSAGE;
		}
		else
		{
			return false;
		}
		
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("Sorry, you are in Jail!");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("Sorry, you are in the Olympiad now.");
			return false;
		}
		else if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("Sorry, you are in an event.");
			return false;
		}
		else if (activeChar.isInDuel())
		{
			activeChar.sendMessage("Sorry, you are in a duel!");
			return false;
		}
		else if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("Sorry, you are in the observation mode.");
			return false;
		}
		else if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("Sorry, you are in a festival.");
			return false;
		}
		else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (activeChar.getKarma() > 0))
		{
			activeChar.sendMessage("Sorry, you are PK");
			return false;
		}
		else if (Olympiad.getInstance().isRegistered(activeChar))
		{
			activeChar.sendMessage("Sorry, you can't use this command while registered in Olympiad");
			return false;
		}
		activeChar.sendPacket(new SetupGauge(SetupGauge.BLUE, 15000));
		activeChar.setImmobilized(true);
		
		ThreadPool.schedule(new teleportTask(activeChar, placex, placey, placez, message), 15000);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	class teleportTask implements Runnable
	{
		private final PlayerInstance _player;
		private final int _x;
		private final int _y;
		private final int _z;
		private final String _message;
		
		teleportTask(PlayerInstance activeChar, int x, int y, int z, String message)
		{
			_player = activeChar;
			_x = x;
			_y = y;
			_z = z;
			_message = message;
		}
		
		@Override
		public void run()
		{
			if (_player == null)
			{
				return;
			}
			
			_player.teleToLocation(_x, _y, _z);
			_player.sendMessage(_message);
			_player.setImmobilized(false);
		}
	}
}
