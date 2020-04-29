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

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.Env;

/**
 * @author decad
 */
final class EffectBetray extends Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BETRAY;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		if ((getEffected() != null) && (getEffector() instanceof PlayerInstance) && (getEffected() instanceof Summon))
		{
			PlayerInstance targetOwner = null;
			targetOwner = ((Summon) getEffected()).getOwner();
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetOwner);
			targetOwner.setBetrayed(true);
			onActionTime();
		}
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		if ((getEffected() != null) && (getEffector() instanceof PlayerInstance) && (getEffected() instanceof Summon))
		{
			PlayerInstance targetOwner = null;
			targetOwner = ((Summon) getEffected()).getOwner();
			targetOwner.setBetrayed(false);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		PlayerInstance targetOwner = null;
		targetOwner = ((Summon) getEffected()).getOwner();
		targetOwner.setBetrayed(true);
		return false;
	}
}
