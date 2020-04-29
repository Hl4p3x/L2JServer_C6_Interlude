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
package org.l2jserver.gameserver.model.skills.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.conditions.Condition;
import org.l2jserver.gameserver.model.skills.funcs.FuncTemplate;
import org.l2jserver.gameserver.model.skills.funcs.Lambda;

/**
 * @author mkizub
 */
public class EffectTemplate
{
	static Logger LOGGER = Logger.getLogger(EffectTemplate.class.getName());
	
	private final Class<?> _func;
	private final Constructor<?> _constructor;
	
	public Condition attachCond;
	public Condition applayCond;
	public Lambda lambda;
	public int counter;
	public int period; // in seconds
	public int abnormalEffect;
	public FuncTemplate[] funcTemplates;
	public boolean showIcon;
	
	public String stackType;
	public float stackOrder;
	public double effectPower; // to thandle chance
	public SkillType effectType; // to handle resistences etc...
	
	public EffectTemplate(Condition pAttachCond, Condition pApplayCond, String func, Lambda pLambda, int pCounter, int pPeriod, int pAbnormalEffect, String pStackType, float pStackOrder, int pShowIcon, SkillType eType, double ePower)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		lambda = pLambda;
		counter = pCounter;
		period = pPeriod;
		abnormalEffect = pAbnormalEffect;
		stackType = pStackType;
		stackOrder = pStackOrder;
		showIcon = pShowIcon == 0;
		effectType = eType;
		effectPower = ePower;
		
		try
		{
			_func = Class.forName("org.l2jserver.gameserver.model.skills.effects.Effect" + func);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			_constructor = _func.getConstructor(Env.class, EffectTemplate.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Effect getEffect(Env env)
	{
		if ((attachCond != null) && !attachCond.test(env))
		{
			return null;
		}
		try
		{
			return (Effect) _constructor.newInstance(env, this);
		}
		catch (IllegalAccessException e)
		{
			LOGGER.warning(e.toString());
			return null;
		}
		catch (InstantiationException e)
		{
			LOGGER.warning(e.toString());
			return null;
		}
		catch (InvocationTargetException e)
		{
			LOGGER.warning("Error creating new instance of Class " + _func + " Exception was: " + e);
			return null;
		}
	}
	
	public void attach(FuncTemplate f)
	{
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
}