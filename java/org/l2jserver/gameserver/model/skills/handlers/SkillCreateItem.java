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
package org.l2jserver.gameserver.model.skills.handlers;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;

/**
 * @author Nemesiss, Mobius
 */
public class SkillCreateItem extends Skill
{
	private final int _createItemId;
	private final int _createItemCount;
	private final int _createRandomCount;
	
	public SkillCreateItem(StatSet set)
	{
		super(set);
		_createItemId = set.getInt("createItemId", 0);
		_createItemCount = set.getInt("createItemCount", 0);
		_createRandomCount = set.getInt("createRandomCount", 0);
	}
	
	@Override
	public void useSkill(Creature creature, WorldObject[] targets)
	{
		if (creature.isAlikeDead() || (_createItemId == 0) || (_createItemCount == 0))
		{
			return;
		}
		
		if (creature.isPlayable())
		{
			creature.getActingPlayer().addItem("Create Item Skill", _createItemId, _createRandomCount > 1 ? _createItemCount * (Rnd.get(_createRandomCount) + 1) : _createItemCount, creature, true);
		}
	}
}
