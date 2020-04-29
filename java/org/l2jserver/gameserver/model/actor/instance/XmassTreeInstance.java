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
package org.l2jserver.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author Drunkard Zabb0x Lets drink2code!
 */
public class XmassTreeInstance extends NpcInstance
{
	private final ScheduledFuture<?> _aiTask;
	
	class XmassAI implements Runnable
	{
		private final XmassTreeInstance _caster;
		
		protected XmassAI(XmassTreeInstance caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
			for (PlayerInstance player : getKnownList().getKnownPlayers().values())
			{
				final int i = Rnd.get(3);
				handleCast(player, (4262 + i));
			}
		}
		
		private boolean handleCast(PlayerInstance player, int skillId)
		{
			final Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
			if (player.getFirstEffect(skill) == null)
			{
				setTarget(player);
				doCast(skill);
				
				broadcastPacket(new MagicSkillUse(_caster, player, skill.getId(), 1, skill.getHitTime(), 0));
				return true;
			}
			return false;
		}
	}
	
	public XmassTreeInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_aiTask = ThreadPool.scheduleAtFixedRate(new XmassAI(this), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		
		super.deleteMe();
	}
	
	@Override
	public int getDistanceToWatchObject(WorldObject object)
	{
		return 900;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
}
