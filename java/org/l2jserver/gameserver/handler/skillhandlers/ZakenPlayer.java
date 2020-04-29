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
package org.l2jserver.gameserver.handler.skillhandlers;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;

public class ZakenPlayer implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.ZAKENPLAYER
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		try
		{
			for (WorldObject target1 : targets)
			{
				if (!(target1 instanceof Creature))
				{
					continue;
				}
				final Creature target = (Creature) target1;
				final int ch = (Rnd.get(14) + 1);
				if (ch == 1)
				{
					target.teleToLocation(55299, 219120, -2952, true);
				}
				else if (ch == 2)
				{
					target.teleToLocation(56363, 218043, -2952, true);
				}
				else if (ch == 3)
				{
					target.teleToLocation(54245, 220162, -2952, true);
				}
				else if (ch == 4)
				{
					target.teleToLocation(56289, 220126, -2952, true);
				}
				else if (ch == 5)
				{
					target.teleToLocation(55299, 219120, -3224, true);
				}
				else if (ch == 6)
				{
					target.teleToLocation(56363, 218043, -3224, true);
				}
				else if (ch == 7)
				{
					target.teleToLocation(54245, 220162, -3224, true);
				}
				else if (ch == 8)
				{
					target.teleToLocation(56289, 220126, -3224, true);
				}
				else if (ch == 9)
				{
					target.teleToLocation(55299, 219120, -3496, true);
				}
				else if (ch == 10)
				{
					target.teleToLocation(56363, 218043, -3496, true);
				}
				else if (ch == 11)
				{
					target.teleToLocation(54245, 220162, -3496, true);
				}
				else if (ch == 12)
				{
					target.teleToLocation(56289, 220126, -3496, true);
				}
				else
				{
					target.teleToLocation(53930, 217760, -2944, true);
				}
			}
		}
		catch (Throwable e)
		{
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}