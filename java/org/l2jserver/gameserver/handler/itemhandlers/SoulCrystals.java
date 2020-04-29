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
package org.l2jserver.gameserver.handler.itemhandlers;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class SoulCrystals implements IItemHandler
{
	// First line is for Red Soul Crystals, second is Green and third is Blue Soul Crystals, ordered by ascending level, from 0 to 13.
	private static final int[] ITEM_IDS =
	{
		4629,
		4630,
		4631,
		4632,
		4633,
		4634,
		4635,
		4636,
		4637,
		4638,
		4639,
		5577,
		5580,
		5908,
		4640,
		4641,
		4642,
		4643,
		4644,
		4645,
		4646,
		4647,
		4648,
		4649,
		4650,
		5578,
		5581,
		5911,
		4651,
		4652,
		4653,
		4654,
		4655,
		4656,
		4657,
		4658,
		4659,
		4660,
		4661,
		5579,
		5582,
		5914
	};
	
	// Our main method, where everything goes on
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		final WorldObject target = player.getTarget();
		if (!(target instanceof MonsterInstance))
		{
			// Send a System Message to the caster
			player.sendPacket(new SystemMessage(SystemMessageId.INVALID_TARGET));
			
			// Send a Server->Client packet ActionFailed to the PlayerInstance
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			return;
		}
		
		if (player.isParalyzed())
		{
			player.sendMessage("You Cannot Use This While You Are Paralyzed");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// u can use soul crystal only when target hp goes below 50%
		if (((MonsterInstance) target).getCurrentHp() > (((MonsterInstance) target).getMaxHp() / 2.0))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int crystalId = item.getItemId();
		
		// Soul Crystal Casting section
		final Skill skill = SkillTable.getInstance().getInfo(2096, 1);
		player.useMagic(skill, false, true);
		// End Soul Crystal Casting section
		
		// Continue execution later
		final CrystalFinalizer cf = new CrystalFinalizer(player, target, crystalId);
		ThreadPool.schedule(cf, skill.getHitTime());
	}
	
	static class CrystalFinalizer implements Runnable
	{
		private final PlayerInstance _player;
		private final Attackable _target;
		private final int _crystalId;
		
		CrystalFinalizer(PlayerInstance player, WorldObject target, int crystalId)
		{
			_player = player;
			_target = (Attackable) target;
			_crystalId = crystalId;
		}
		
		@Override
		public void run()
		{
			if (_player.isDead() || _target.isDead())
			{
				return;
			}
			_player.enableAllSkills();
			try
			{
				_target.addAbsorber(_player, _crystalId);
				_player.setTarget(_target);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
