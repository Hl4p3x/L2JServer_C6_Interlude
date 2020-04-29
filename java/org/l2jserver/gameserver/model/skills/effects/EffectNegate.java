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

import java.util.logging.Logger;

import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.skills.Env;

/**
 * @author Gnat
 */
public class EffectNegate extends Effect
{
	protected static final Logger LOGGER = Logger.getLogger(EffectNegate.class.getName());
	
	public EffectNegate(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.NEGATE;
	}
	
	@Override
	public void onStart()
	{
		final Skill skill = getSkill();
		if (skill.getNegateId() != 0)
		{
			getEffected().stopSkillEffects(skill.getNegateId());
		}
		
		for (String negateSkillType : skill.getNegateSkillTypes())
		{
			SkillType type = null;
			try
			{
				type = SkillType.valueOf(negateSkillType);
			}
			catch (Exception e)
			{
			}
			
			if (type != null)
			{
				getEffected().stopSkillEffects(type, skill.getPower());
			}
		}
		
		for (String negateEffectType : skill.getNegateEffectTypes())
		{
			EffectType type = null;
			try
			{
				type = EffectType.valueOf(negateEffectType);
			}
			catch (Exception e)
			{
			}
			
			if (type != null)
			{
				getEffected().stopEffects(type);
			}
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
