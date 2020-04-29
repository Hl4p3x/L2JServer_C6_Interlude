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
package org.l2jserver.gameserver.model;

/**
 * @author Rayan RPG
 * @since 927
 */
public class NpcWalkerNode
{
	private int _moveX;
	private int _moveY;
	private int _moveZ;
	private int _delay;
	private boolean _running;
	private String _chatText;
	
	public void setRunning(boolean value)
	{
		_running = value;
	}
	
	public void setMoveX(int value)
	{
		_moveX = value;
	}
	
	public void setMoveY(int value)
	{
		_moveY = value;
	}
	
	public void setMoveZ(int value)
	{
		_moveZ = value;
	}
	
	public void setDelay(int value)
	{
		_delay = value;
	}
	
	public void setChatText(String value)
	{
		_chatText = value;
	}
	
	public int getMoveX()
	{
		return _moveX;
	}
	
	public int getMoveY()
	{
		return _moveY;
	}
	
	public int getMoveZ()
	{
		return _moveZ;
	}
	
	public int getDelay()
	{
		return _delay;
	}
	
	public boolean getRunning()
	{
		return _running;
	}
	
	public String getChatText()
	{
		return _chatText;
	}
}
