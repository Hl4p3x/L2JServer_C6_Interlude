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

import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.effects.EffectSeed;

/**
 * @author Advi
 */
public class ConditionElementSeed extends Condition
{
	private static int[] seedSkills =
	{
		1285,
		1286,
		1287
	};
	private final int[] _requiredSeeds;
	
	public ConditionElementSeed(int[] seeds)
	{
		_requiredSeeds = seeds;
	}
	
	ConditionElementSeed(int fire, int water, int wind, int various, int any)
	{
		_requiredSeeds = new int[5];
		_requiredSeeds[0] = fire;
		_requiredSeeds[1] = water;
		_requiredSeeds[2] = wind;
		_requiredSeeds[3] = various;
		_requiredSeeds[4] = any;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final int[] seeds = new int[3];
		for (int i = 0; i < seeds.length; i++)
		{
			seeds[i] = env.player.getFirstEffect(seedSkills[i]) instanceof EffectSeed ? ((EffectSeed) env.player.getFirstEffect(seedSkills[i])).getPower() : 0;
			if (seeds[i] >= _requiredSeeds[i])
			{
				seeds[i] -= _requiredSeeds[i];
			}
			else
			{
				return false;
			}
		}
		
		if (_requiredSeeds[3] > 0)
		{
			int count = 0;
			for (int i = 0; (i < seeds.length) && (count < _requiredSeeds[3]); i++)
			{
				if (seeds[i] > 0)
				{
					seeds[i]--;
					count++;
				}
			}
			if (count < _requiredSeeds[3])
			{
				return false;
			}
		}
		
		if (_requiredSeeds[4] > 0)
		{
			int count = 0;
			for (int i = 0; (i < seeds.length) && (count < _requiredSeeds[4]); i++)
			{
				count += seeds[i];
			}
			if (count < _requiredSeeds[4])
			{
				return false;
			}
		}
		
		return true;
	}
}
