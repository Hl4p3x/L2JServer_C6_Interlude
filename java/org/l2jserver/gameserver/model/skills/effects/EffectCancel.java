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

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

final class EffectCancel extends Effect
{
	public EffectCancel(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CANCEL;
	}
	
	@Override
	public void onStart()
	{
		final int landrate = (int) getEffector().calcStat(Stat.CANCEL_VULN, 90, getEffected(), null);
		if (Rnd.get(100) < landrate)
		{
			final Effect[] effects = getEffected().getAllEffects();
			int maxdisp = (int) getSkill().getNegatePower();
			if (maxdisp == 0)
			{
				maxdisp = 5;
			}
			for (Effect e : effects)
			{
				switch (e.getEffectType())
				{
					case SIGNET_GROUND:
					case SIGNET_EFFECT:
					{
						continue;
					}
				}
				
				if ((e.getSkill().getId() != 4082) && (e.getSkill().getId() != 4215) && (e.getSkill().getId() != 5182) && (e.getSkill().getId() != 4515) && (e.getSkill().getId() != 110) && (e.getSkill().getId() != 111) && (e.getSkill().getId() != 1323) && (e.getSkill().getId() != 1325) && (e.getSkill().getSkillType() == SkillType.BUFF))
				{
					// TODO Fix cancel debuffs
					if (e.getSkill().getSkillType() != SkillType.DEBUFF)
					{
						int rate = 100;
						final int level = e.getLevel();
						if (level > 0)
						{
							rate = 150 / (1 + level);
						}
						
						if (rate > 95)
						{
							rate = 95;
						}
						else if (rate < 5)
						{
							rate = 5;
						}
						
						if (Rnd.get(100) < rate)
						{
							e.exit(true);
							maxdisp--;
							if (maxdisp == 0)
							{
								break;
							}
						}
					}
				}
			}
		}
		else if (getEffector() instanceof PlayerInstance)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
			sm.addString(getEffected().getName());
			sm.addSkillName(getSkill().getDisplayId());
			getEffector().sendPacket(sm);
		}
	}
	
	@Override
	public void onExit()
	{
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
