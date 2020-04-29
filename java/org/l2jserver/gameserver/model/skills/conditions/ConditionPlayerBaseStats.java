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
package org.l2jserver.gameserver.model.skills.conditions;

import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.Env;

/**
 * @author mkizub
 */
public class ConditionPlayerBaseStats extends Condition
{
	private final BaseStat _stat;
	private final int _value;
	
	public ConditionPlayerBaseStats(Creature creature, BaseStat stat, int value)
	{
		super();
		_stat = stat;
		_value = value;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof PlayerInstance))
		{
			return false;
		}
		final PlayerInstance player = (PlayerInstance) env.player;
		switch (_stat)
		{
			case Int:
			{
				return player.getINT() >= _value;
			}
			case Str:
			{
				return player.getSTR() >= _value;
			}
			case Con:
			{
				return player.getCON() >= _value;
			}
			case Dex:
			{
				return player.getDEX() >= _value;
			}
			case Men:
			{
				return player.getMEN() >= _value;
			}
			case Wit:
			{
				return player.getWIT() >= _value;
			}
		}
		return false;
	}
}

enum BaseStat
{
	Int,
	Str,
	Con,
	Dex,
	Men,
	Wit
}
