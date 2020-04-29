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
package org.l2jserver.gameserver.model.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.l2jserver.gameserver.model.skills.funcs.Func;

/**
 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematic function:<br>
 * <br>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br>
 * <br>
 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <b>_order</b>. Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in the
 * value property of an Env class instance.<br>
 * <br>
 * Method addFunc and removeFunc permit to add and remove a Func object from a Calculator.
 */
public class Calculator
{
	/** Empty Func table definition */
	private static final Func[] _emptyFuncs = new Func[0];
	
	/** Table of Func object */
	private Func[] _functions;
	
	/**
	 * Constructor of Calculator (Init value : emptyFuncs).
	 */
	public Calculator()
	{
		_functions = _emptyFuncs;
	}
	
	/**
	 * Constructor of Calculator (Init value : Calculator c).
	 * @param c
	 */
	public Calculator(Calculator c)
	{
		_functions = c._functions;
	}
	
	/**
	 * Check if 2 calculators are equals.
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static boolean equalsCals(Calculator c1, Calculator c2)
	{
		if (c1 == c2)
		{
			return true;
		}
		
		if ((c1 == null) || (c2 == null))
		{
			return false;
		}
		
		final Func[] funcs1 = c1._functions;
		final Func[] funcs2 = c2._functions;
		if (funcs1 == funcs2)
		{
			return true;
		}
		
		if (funcs1.length != funcs2.length)
		{
			return false;
		}
		
		if (funcs1.length == 0)
		{
			return true;
		}
		
		for (int i = 0; i < funcs1.length; i++)
		{
			if (funcs1[i] != funcs2[i])
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return the number of Funcs in the Calculator.
	 */
	public int size()
	{
		return _functions.length;
	}
	
	/**
	 * Add a Func to the Calculator.
	 * @param f
	 */
	public synchronized void addFunc(Func f)
	{
		final Func[] funcs = _functions;
		final Func[] tmp = new Func[funcs.length + 1];
		final int order = f.order;
		int i;
		for (i = 0; (i < funcs.length) && (order >= funcs[i].order); i++)
		{
			tmp[i] = funcs[i];
		}
		
		tmp[i] = f;
		for (; i < funcs.length; i++)
		{
			tmp[i + 1] = funcs[i];
		}
		
		_functions = tmp;
	}
	
	/**
	 * Remove a Func from the Calculator.
	 * @param f
	 */
	public synchronized void removeFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		final ArrayList<Func> tmp = new ArrayList<>();
		tmp.addAll(Arrays.asList(_functions));
		
		if (tmp.contains(f))
		{
			tmp.remove(f);
		}
		
		_functions = tmp.toArray(new Func[tmp.size()]);
	}
	
	/**
	 * Remove each Func with the specified owner of the Calculator.
	 * @param owner
	 * @return
	 */
	public synchronized List<Stat> removeOwner(Object owner)
	{
		final Func[] funcs = _functions;
		final List<Stat> modifiedStats = new ArrayList<>();
		for (Func func : funcs)
		{
			if (func.funcOwner == owner)
			{
				modifiedStats.add(func.stat);
				removeFunc(func);
			}
		}
		return modifiedStats;
	}
	
	/**
	 * Run each Func of the Calculator.
	 * @param env
	 */
	public void calc(Env env)
	{
		final Func[] funcs = _functions;
		for (Func func : funcs)
		{
			func.calc(env);
		}
	}
}
