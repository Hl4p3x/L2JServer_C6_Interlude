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
package org.l2jserver.gameserver.model.skills.funcs;

import org.l2jserver.Config;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.skills.Env;
import org.l2jserver.gameserver.model.skills.Stat;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stat pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
	}
	
	@Override
	public void calc(Env env)
	{
		if ((cond != null) && !cond.test(env))
		{
			return;
		}
		final ItemInstance item = (ItemInstance) funcOwner;
		final int cristall = item.getItem().getCrystalType();
		final Enum<?> itemType = item.getItemType();
		if (cristall == Item.CRYSTAL_NONE)
		{
			return;
		}
		int enchant = item.getEnchantLevel();
		int overenchant = 0;
		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}
		
		if ((env.player != null) && (env.player instanceof PlayerInstance))
		{
			final PlayerInstance player = (PlayerInstance) env.player;
			if (player.isInOlympiadMode() && (Config.ALT_OLY_ENCHANT_LIMIT >= 0) && ((enchant + overenchant) > Config.ALT_OLY_ENCHANT_LIMIT))
			{
				if (Config.ALT_OLY_ENCHANT_LIMIT > 3)
				{
					overenchant = Config.ALT_OLY_ENCHANT_LIMIT - 3;
				}
				else
				{
					overenchant = 0;
					enchant = Config.ALT_OLY_ENCHANT_LIMIT;
				}
			}
		}
		
		if ((stat == Stat.MAGIC_DEFENCE) || (stat == Stat.POWER_DEFENCE))
		{
			env.value += enchant + (3 * overenchant);
			return;
		}
		
		if (stat == Stat.MAGIC_ATTACK)
		{
			switch (item.getItem().getCrystalType())
			{
				case Item.CRYSTAL_S:
				{
					env.value += (4 * enchant) + (8 * overenchant);
					break;
				}
				case Item.CRYSTAL_A:
				{
					env.value += (3 * enchant) + (6 * overenchant);
					break;
				}
				case Item.CRYSTAL_B:
				{
					env.value += (3 * enchant) + (6 * overenchant);
					break;
				}
				case Item.CRYSTAL_C:
				{
					env.value += (3 * enchant) + (6 * overenchant);
					break;
				}
				case Item.CRYSTAL_D:
				{
					env.value += (2 * enchant) + (4 * overenchant);
					break;
				}
			}
			return;
		}
		
		switch (item.getItem().getCrystalType())
		{
			case Item.CRYSTAL_A:
			{
				if (itemType == WeaponType.BOW)
				{
					env.value += (8 * enchant) + (16 * overenchant);
				}
				else if ((itemType == WeaponType.DUALFIST) || (itemType == WeaponType.DUAL) || ((itemType == WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
				{
					env.value += (5 * enchant) + (10 * overenchant);
				}
				else
				{
					env.value += (4 * enchant) + (8 * overenchant);
				}
				break;
			}
			case Item.CRYSTAL_B:
			{
				if (itemType == WeaponType.BOW)
				{
					env.value += (6 * enchant) + (12 * overenchant);
				}
				else if ((itemType == WeaponType.DUALFIST) || (itemType == WeaponType.DUAL) || ((itemType == WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
				{
					env.value += (4 * enchant) + (8 * overenchant);
				}
				else
				{
					env.value += (3 * enchant) + (6 * overenchant);
				}
				break;
			}
			case Item.CRYSTAL_C:
			{
				if (itemType == WeaponType.BOW)
				{
					env.value += (6 * enchant) + (12 * overenchant);
				}
				else if ((itemType == WeaponType.DUALFIST) || (itemType == WeaponType.DUAL) || ((itemType == WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
				{
					env.value += (4 * enchant) + (8 * overenchant);
				}
				else
				{
					env.value += (3 * enchant) + (6 * overenchant);
				}
				break;
			}
			case Item.CRYSTAL_D:
			{
				if (itemType == WeaponType.BOW)
				{
					env.value += (4 * enchant) + (8 * overenchant);
				}
				else
				{
					env.value += (2 * enchant) + (4 * overenchant);
				}
				break;
			}
			case Item.CRYSTAL_S:
			{
				if (itemType == WeaponType.BOW)
				{
					env.value += (10 * enchant) + (20 * overenchant);
				}
				else if ((itemType == WeaponType.DUALFIST) || (itemType == WeaponType.DUAL) || ((itemType == WeaponType.SWORD) && (item.getItem().getBodyPart() == 16384)))
				{
					env.value += (4 * enchant) + (12 * overenchant);
				}
				else
				{
					env.value += (4 * enchant) + (10 * overenchant);
				}
				break;
			}
		}
		return;
	}
}
