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
public class LambdaCalc extends Lambda
{
	public Func[] funcs;
	
	public LambdaCalc()
	{
		funcs = new Func[0];
	}
	
	@Override
	public double calc(Env env)
	{
		final double saveValue = env.value;
		try
		{
			env.value = 0;
			for (Func f : funcs)
			{
				f.calc(env);
			}
			return env.value;
		}
		finally
		{
			env.value = saveValue;
		}
	}
	
	public void addFunc(Func f)
	{
		final int len = funcs.length;
		final Func[] tmp = new Func[len + 1];
		for (int i = 0; i < len; i++)
		{
			tmp[i] = funcs[i];
		}
		tmp[len] = f;
		funcs = tmp;
	}
}
