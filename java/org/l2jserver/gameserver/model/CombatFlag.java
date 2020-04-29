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
package org.l2jserver.gameserver.model;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author programmos, scoria dev
 */
public class CombatFlag
{
	protected PlayerInstance _player = null;
	public int playerId = 0;
	private ItemInstance _item = null;
	
	private final Location _location;
	public ItemInstance itemInstance;
	
	private final int _itemId;
	
	public CombatFlag(int x, int y, int z, int heading, int itemId)
	{
		_location = new Location(x, y, z, heading);
		_itemId = itemId;
	}
	
	public synchronized void spawnMe()
	{
		ItemInstance i;
		
		// Init the dropped ItemInstance and add it in the world as a visible object at the position where mob was last
		i = ItemTable.getInstance().createItem("Combat", _itemId, 1, null, null);
		i.spawnMe(_location.getX(), _location.getY(), _location.getZ());
		itemInstance = i;
	}
	
	public synchronized void unSpawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		
		if (itemInstance != null)
		{
			itemInstance.decayMe();
		}
	}
	
	public void activate(PlayerInstance player, ItemInstance item)
	{
		// if the player is mounted, attempt to unmount first. Only allow picking up the combat flag if dismount is successful.
		if (player.isMounted() && !player.dismount())
		{
			// TODO: correct this custom message.
			player.sendMessage("You may not pick up this item while riding in this territory");
			return;
		}
		
		// Player holding it data
		_player = player;
		playerId = _player.getObjectId();
		itemInstance = null;
		
		// Add skill
		giveSkill();
		
		// Equip with the weapon
		_item = item;
		_player.getInventory().equipItemAndRecord(_item);
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EQUIPPED_YOUR_S1);
		sm.addItemName(_item.getItemId());
		_player.sendPacket(sm);
		
		// Refresh inventory
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendPacket(iu);
		}
		else
		{
			_player.sendPacket(new ItemList(_player, false));
		}
		
		// Refresh player stats
		_player.broadcastUserInfo();
	}
	
	public void dropIt()
	{
		// Reset player stats
		removeSkill();
		_player.destroyItem("DieDrop", _item, null, false);
		_item = null;
		_player.broadcastUserInfo();
		_player = null;
		playerId = 0;
	}
	
	public void giveSkill()
	{
		_player.addSkill(SkillTable.getInstance().getInfo(3318, 1), false);
		_player.addSkill(SkillTable.getInstance().getInfo(3358, 1), false);
		_player.sendSkillList();
	}
	
	public void removeSkill()
	{
		_player.removeSkill(SkillTable.getInstance().getInfo(3318, 1), false);
		_player.removeSkill(SkillTable.getInstance().getInfo(3358, 1), false);
		_player.sendSkillList();
	}
}
