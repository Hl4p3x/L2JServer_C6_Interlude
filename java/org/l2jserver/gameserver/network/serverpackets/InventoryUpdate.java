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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.model.ItemInfo;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

/**
 * 37 // Packet Identifier<br>
 * 01 00 // Number of ItemInfo Trame of the Packet<br>
 * <br>
 * 03 00 // Update type : 01-add, 02-modify, 03-remove<br>
 * 04 00 // Item Type 1 : 00-weapon/ring/earring/necklace, 01-armor/shield, 04-item/questitem/adena<br>
 * c6 37 50 40 // ObjectId<br>
 * cd 09 00 00 // ItemId<br>
 * 05 00 00 00 // Quantity<br>
 * 05 00 // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item<br>
 * 00 00 // Filler (always 0)<br>
 * 00 00 // Equipped : 00-No, 01-yes<br>
 * 00 00 // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand<br>
 * 00 00 // Enchant level (pet level shown in control item)<br>
 * 00 00 // Pet name exists or not shown in control item<br>
 * <br>
 * <br>
 * format h (hh dddhhhh hh) revision 377<br>
 * format h (hh dddhhhd hh) revision 415<br>
 * <br>
 * @version $Revision: 1.3.2.2.2.4 $ $Date: 2005/03/27 15:29:39 $ Rebuild 23.2.2006 by Advi
 */
public class InventoryUpdate extends GameServerPacket
{
	private final List<ItemInfo> _items;
	
	public InventoryUpdate()
	{
		_items = new ArrayList<>();
	}
	
	public InventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
	}
	
	public void addItem(ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item));
		}
	}
	
	public void addNewItem(ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item, 1));
		}
	}
	
	public void addModifiedItem(ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item, 2));
		}
	}
	
	public void addRemovedItem(ItemInstance item)
	{
		if (item != null)
		{
			_items.add(new ItemInfo(item, 3));
		}
	}
	
	public void addItems(List<ItemInstance> items)
	{
		if (items != null)
		{
			for (ItemInstance item : items)
			{
				if (item != null)
				{
					_items.add(new ItemInfo(item));
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x27);
		writeH(_items.size());
		for (ItemInfo item : _items)
		{
			writeH(item.getChange()); // Update type : 01-add, 02-modify,
			// 03-remove
			writeH(item.getItem().getType1()); // Item Type 1 :
			// 00-weapon/ring/earring/necklace,
			// 01-armor/shield,
			// 04-item/questitem/adena
			writeD(item.getObjectId()); // ObjectId
			writeD(item.getItem().getItemId()); // ItemId
			writeD(item.getCount()); // Quantity
			writeH(item.getItem().getType2()); // Item Type 2 : 00-weapon,
			// 01-shield/armor,
			// 02-ring/earring/necklace,
			// 03-questitem, 04-adena,
			// 05-item
			writeH(item.getCustomType1()); // Filler (always 0)
			writeH(item.getEquipped()); // Equipped : 00-No, 01-yes
			writeD(item.getItem().getBodyPart()); // Slot : 0006-lr.ear,
			// 0008-neck,
			// 0030-lr.finger,
			// 0040-head, 0100-l.hand,
			// 0200-gloves, 0400-chest,
			// 0800-pants, 1000-feet,
			// 4000-r.hand, 8000-r.hand
			writeH(item.getEnchant()); // Enchant level (pet level shown in
			// control item)
			writeH(item.getCustomType2()); // Pet name exists or not shown
			// in
			// control item
			writeD(item.getAugemtationBonus());
			writeD(item.getMana());
		}
	}
}