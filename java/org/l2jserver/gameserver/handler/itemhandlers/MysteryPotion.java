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
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @version $Revision: 1.1.6.4 $ $Date: 2005/04/06 18:25:18 $
 */

public class MysteryPotion implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5234
	};
	private static final int BIGHEAD_EFFECT = 0x2000;
	private static final int MYSTERY_POTION_SKILL = 2103;
	private static final int EFFECT_DURATION = 1200000; // 20 mins
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		
		// Use a summon skill effect for fun ;)
		final MagicSkillUse msu = new MagicSkillUse(playable, playable, 2103, 1, 0, 0);
		player.sendPacket(msu);
		player.broadcastPacket(msu);
		
		player.startAbnormalEffect(BIGHEAD_EFFECT);
		player.destroyItem("Consume", item.getObjectId(), 1, null, false);
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
		sm.addSkillName(MYSTERY_POTION_SKILL);
		player.sendPacket(sm);
		
		final MysteryPotionStop mp = new MysteryPotionStop(playable);
		ThreadPool.schedule(mp, EFFECT_DURATION);
	}
	
	public class MysteryPotionStop implements Runnable
	{
		private final Playable _playable;
		
		public MysteryPotionStop(Playable playable)
		{
			_playable = playable;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (!(_playable instanceof PlayerInstance))
				{
					return;
				}
				
				((PlayerInstance) _playable).stopAbnormalEffect(BIGHEAD_EFFECT);
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
