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

import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.effects.EffectBattleForce;
import org.l2jserver.gameserver.model.skills.effects.EffectSpellForce;

/**
 * @author kombat
 */
public class ConditionForceBuff extends Condition
{
	private static int BATTLE_FORCE = 5104;
	private static int SPELL_FORCE = 5105;
	
	private final int _battleForces;
	private final int _spellForces;
	
	public ConditionForceBuff(int[] forces)
	{
		_battleForces = forces[0];
		_spellForces = forces[1];
	}
	
	public ConditionForceBuff(int battle, int spell)
	{
		_battleForces = battle;
		_spellForces = spell;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final int neededBattle = _battleForces;
		if (neededBattle > 0)
		{
			final Effect battleForce = env.player.getFirstEffect(BATTLE_FORCE);
			if (!(battleForce instanceof EffectBattleForce) || (((EffectBattleForce) battleForce).forces < neededBattle))
			{
				return false;
			}
		}
		final int neededSpell = _spellForces;
		if (neededSpell > 0)
		{
			final Effect spellForce = env.player.getFirstEffect(SPELL_FORCE);
			if (!(spellForce instanceof EffectSpellForce) || (((EffectSpellForce) spellForce).forces < neededSpell))
			{
				return false;
			}
		}
		return true;
	}
}
