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

import org.l2jserver.gameserver.GameTimeController;

/**
 * A datatype used to retain a fish information.
 */
public class Fish
{
	private final int _id;
	private final int _level;
	private final int _hp;
	private final int _hpRegen;
	private final int _type;
	private final int _group;
	private final int _guts;
	private final int _gutsCheckTime;
	private final int _waitTime;
	private final int _combatTime;
	
	public Fish(StatSet set)
	{
		_id = set.getInt("id");
		_level = set.getInt("level");
		_hp = set.getInt("hp");
		_hpRegen = set.getInt("hpRegen");
		_type = set.getInt("type");
		_group = set.getInt("group");
		_guts = set.getInt("guts");
		_gutsCheckTime = set.getInt("gutsCheckTime");
		_waitTime = set.getInt("waitTime");
		_combatTime = set.getInt("combatTime");
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getHp()
	{
		return _hp;
	}
	
	public int getHpRegen()
	{
		return _hpRegen;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getType(boolean isLureNight)
	{
		if (!GameTimeController.getInstance().isNowNight() && isLureNight)
		{
			return -1;
		}
		return _type;
	}
	
	public int getGroup()
	{
		return _group;
	}
	
	public int getGuts()
	{
		return _guts;
	}
	
	public int getGutsCheckTime()
	{
		return _gutsCheckTime;
	}
	
	public int getWaitTime()
	{
		return _waitTime;
	}
	
	public int getCombatTime()
	{
		return _combatTime;
	}
}