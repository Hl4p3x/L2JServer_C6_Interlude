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
package org.l2jserver.gameserver.model.skills.funcs;

import org.l2jserver.gameserver.model.skills.Env;

/**
 * @author mkizub
 */
public class LambdaStats extends Lambda
{
	public enum StatType
	{
		PLAYER_LEVEL,
		TARGET_LEVEL,
		PLAYER_MAX_HP,
		PLAYER_MAX_MP
	}
	
	private final StatType _stat;
	
	public LambdaStats(StatType stat)
	{
		_stat = stat;
	}
	
	@Override
	public double calc(Env env)
	{
		switch (_stat)
		{
			case PLAYER_LEVEL:
			{
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getLevel();
			}
			case TARGET_LEVEL:
			{
				if (env.target == null)
				{
					return 1;
				}
				return env.target.getLevel();
			}
			case PLAYER_MAX_HP:
			{
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getMaxHp();
			}
			case PLAYER_MAX_MP:
			{
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getMaxMp();
			}
		}
		return 0;
	}
}
