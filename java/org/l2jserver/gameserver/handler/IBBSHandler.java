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
package org.l2jserver.gameserver.handler;

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Azagthtot
 */
public interface IBBSHandler
{
	/**
	 * @return as String [] _bbs)
	 */
	String[] getBBSCommands();
	
	/**
	 * @param command as String - _bbs
	 * @param player as PlayerInstance - Alt+B
	 * @param params as String -
	 */
	void handleCommand(String command, PlayerInstance player, String params);
}
