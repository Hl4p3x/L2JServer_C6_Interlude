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
package org.l2jserver.gameserver.network.serverpackets;

import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

/**
 * Sdh(h dddhh [dhhh] d) Sdh ddddd ddddd ddddd ddddd
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2007/11/26 16:10:05 $
 */
public class GMViewWarehouseWithdrawList extends GameServerPacket
{
	private final ItemInstance[] _items;
	private final String _playerName;
	private final PlayerInstance _player;
	private final int _money;
	
	public GMViewWarehouseWithdrawList(PlayerInstance player)
	{
		_player = player;
		_items = _player.getWarehouse().getItems();
		_playerName = _player.getName();
		_money = _player.getAdena();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x95);
		writeS(_playerName);
		writeD(_money);
		writeH(_items.length);
		
		for (ItemInstance item : _items)
		{
			writeH(item.getItem().getType1());
			
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			
			switch (item.getItem().getType2())
			{
				case Item.TYPE2_WEAPON:
				{
					writeD(item.getItem().getBodyPart());
					writeH(item.getEnchantLevel());
					writeH(((Weapon) item.getItem()).getSoulShotCount());
					writeH(((Weapon) item.getItem()).getSpiritShotCount());
					break;
				}
				case Item.TYPE2_SHIELD_ARMOR:
				case Item.TYPE2_ACCESSORY:
				case Item.TYPE2_PET_WOLF:
				case Item.TYPE2_PET_HATCHLING:
				case Item.TYPE2_PET_STRIDER:
				case Item.TYPE2_PET_BABY:
				{
					writeD(item.getItem().getBodyPart());
					writeH(item.getEnchantLevel());
					writeH(0x00);
					writeH(0x00);
					break;
				}
			}
			
			writeD(item.getObjectId());
			
			switch (item.getItem().getType2())
			{
				case Item.TYPE2_WEAPON:
				{
					if (item.isAugmented())
					{
						writeD(0x0000FFFF & item.getAugmentation().getAugmentationId());
						writeD(item.getAugmentation().getAugmentationId() >> 16);
					}
					else
					{
						writeD(0);
						writeD(0);
					}
					break;
				}
				case Item.TYPE2_SHIELD_ARMOR:
				case Item.TYPE2_ACCESSORY:
				case Item.TYPE2_PET_WOLF:
				case Item.TYPE2_PET_HATCHLING:
				case Item.TYPE2_PET_STRIDER:
				case Item.TYPE2_PET_BABY:
				{
					writeD(0);
					writeD(0);
				}
			}
		}
	}
}
