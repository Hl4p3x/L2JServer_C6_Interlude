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

import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.actor.instance.ArtefactInstance;
import org.l2jserver.gameserver.model.actor.instance.ControlTowerInstance;
import org.l2jserver.gameserver.model.actor.instance.EffectPointInstance;
import org.l2jserver.gameserver.model.actor.instance.FolkInstance;
import org.l2jserver.gameserver.model.actor.instance.SiegeFlagInstance;
import org.l2jserver.gameserver.model.actor.instance.SiegeSummonInstance;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.network.serverpackets.BeginRotation;
import org.l2jserver.gameserver.network.serverpackets.StopRotation;
import org.l2jserver.gameserver.network.serverpackets.ValidateLocation;

/**
 * @author programmos, sword developers Implementation of the Bluff Effect
 */
public class EffectBluff extends Effect
{
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BLUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onStart()
	{
		if (getEffected().isDead() || getEffected().isAfraid())
		{
			return;
		}
		
		if ((getEffected() instanceof FolkInstance) || (getEffected() instanceof ControlTowerInstance) || (getEffected() instanceof ArtefactInstance) || (getEffected() instanceof EffectPointInstance) || (getEffected() instanceof SiegeFlagInstance) || (getEffected() instanceof SiegeSummonInstance))
		{
			return;
		}
		
		super.onStart();
		
		// break target
		getEffected().setTarget(null);
		// stop cast
		getEffected().breakCast();
		// stop attacking
		getEffected().breakAttack();
		// stop follow
		getEffected().getAI().stopFollow();
		// stop auto attack
		getEffected().getAI().clientStopAutoAttack();
		
		getEffected().broadcastPacket(new BeginRotation(getEffected(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		// sometimes rotation didn't showed correctly ??
		getEffected().sendPacket(new ValidateLocation(getEffector()));
		getEffector().sendPacket(new ValidateLocation(getEffected()));
		onActionTime();
	}
}
