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
package org.l2jserver.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.EnchantResult;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.IllegalPlayerAction;
import org.l2jserver.gameserver.util.Util;

public class RequestEnchantItem extends GameClientPacket
{
	protected static final Logger LOGGER = Logger.getLogger(RequestEnchantItem.class.getName());
	
	private static final int[] CRYSTAL_SCROLLS =
	{
		731,
		732,
		949,
		950,
		953,
		954,
		957,
		958,
		961,
		962
	};
	
	private static final int[] NORMAL_WEAPON_SCROLLS =
	{
		729,
		947,
		951,
		955,
		959
	};
	
	private static final int[] BLESSED_WEAPON_SCROLLS =
	{
		6569,
		6571,
		6573,
		6575,
		6577
	};
	
	private static final int[] CRYSTAL_WEAPON_SCROLLS =
	{
		731,
		949,
		953,
		957,
		961
	};
	
	private static final int[] NORMAL_ARMOR_SCROLLS =
	{
		730,
		948,
		952,
		956,
		960
	};
	
	private static final int[] BLESSED_ARMOR_SCROLLS =
	{
		6570,
		6572,
		6574,
		6576,
		6578
	};
	
	private static final int[] CRYSTAL_ARMOR_SCROLLS =
	{
		732,
		950,
		954,
		958,
		962
	};
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if ((player == null) || (_objectId == 0))
		{
			return;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.cancelActiveTrade();
			player.sendMessage("Your trade canceled");
			return;
		}
		
		// Fix enchant transactions
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.setActiveEnchantItem(null);
			return;
		}
		
		if (!player.isOnline())
		{
			player.setActiveEnchantItem(null);
			return;
		}
		
		final ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		ItemInstance scroll = player.getActiveEnchantItem();
		player.setActiveEnchantItem(null);
		
		if ((item == null) || (scroll == null))
		{
			player.setActiveEnchantItem(null);
			return;
		}
		
		// can't enchant rods and shadow items
		if ((item.getItem().getItemType() == WeaponType.ROD) || item.isShadowItem())
		{
			player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.setActiveEnchantItem(null);
			return;
		}
		
		if (!Config.ENCHANT_HERO_WEAPON && (item.getItemId() >= 6611) && (item.getItemId() <= 6621))
		{
			player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.setActiveEnchantItem(null);
			return;
		}
		
		/*
		 * if(!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_ENCHANT)) { activeChar.setActiveEnchantItem(null); activeChar.sendMessage("Enchant failed"); return; }
		 */
		
		if (item.isWear())
		{
			player.setActiveEnchantItem(null);
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to enchant a weared Item", IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		final int itemType2 = item.getItem().getType2();
		boolean enchantItem = false;
		boolean blessedScroll = false;
		boolean crystalScroll = false;
		int crystalId = 0;
		
		/** pretty code ;D */
		switch (item.getItem().getCrystalType())
		{
			case Item.CRYSTAL_A:
			{
				crystalId = 1461;
				switch (scroll.getItemId())
				{
					case 729:
					case 731:
					case 6569:
					{
						if (itemType2 == Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					}
					case 730:
					case 732:
					case 6570:
					{
						if ((itemType2 == Item.TYPE2_SHIELD_ARMOR) || (itemType2 == Item.TYPE2_ACCESSORY))
						{
							enchantItem = true;
						}
						break;
					}
				}
				break;
			}
			case Item.CRYSTAL_B:
			{
				crystalId = 1460;
				switch (scroll.getItemId())
				{
					case 947:
					case 949:
					case 6571:
					{
						if (itemType2 == Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					}
					case 948:
					case 950:
					case 6572:
					{
						if ((itemType2 == Item.TYPE2_SHIELD_ARMOR) || (itemType2 == Item.TYPE2_ACCESSORY))
						{
							enchantItem = true;
						}
						break;
					}
				}
				break;
			}
			case Item.CRYSTAL_C:
			{
				crystalId = 1459;
				switch (scroll.getItemId())
				{
					case 951:
					case 953:
					case 6573:
					{
						if (itemType2 == Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					}
					case 952:
					case 954:
					case 6574:
					{
						if ((itemType2 == Item.TYPE2_SHIELD_ARMOR) || (itemType2 == Item.TYPE2_ACCESSORY))
						{
							enchantItem = true;
						}
						break;
					}
				}
				break;
			}
			case Item.CRYSTAL_D:
			{
				crystalId = 1458;
				switch (scroll.getItemId())
				{
					case 955:
					case 957:
					case 6575:
					{
						if (itemType2 == Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					}
					case 956:
					case 958:
					case 6576:
					{
						if ((itemType2 == Item.TYPE2_SHIELD_ARMOR) || (itemType2 == Item.TYPE2_ACCESSORY))
						{
							enchantItem = true;
						}
						break;
					}
				}
				break;
			}
			case Item.CRYSTAL_S:
			{
				crystalId = 1462;
				switch (scroll.getItemId())
				{
					case 959:
					case 961:
					case 6577:
					{
						if (itemType2 == Item.TYPE2_WEAPON)
						{
							enchantItem = true;
						}
						break;
					}
					case 960:
					case 962:
					case 6578:
					{
						if ((itemType2 == Item.TYPE2_SHIELD_ARMOR) || (itemType2 == Item.TYPE2_ACCESSORY))
						{
							enchantItem = true;
						}
						break;
					}
				}
				break;
			}
		}
		
		if (!enchantItem)
		{
			player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}
		
		// Get the scroll type - Yesod
		if ((scroll.getItemId() >= 6569) && (scroll.getItemId() <= 6578))
		{
			blessedScroll = true;
		}
		else
		{
			for (int crystalscroll : CRYSTAL_SCROLLS)
			{
				if (scroll.getItemId() == crystalscroll)
				{
					crystalScroll = true;
					break;
				}
			}
		}
		
		// SystemMessage sm = SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		// player.sendPacket(sm);
		SystemMessage sm;
		int chance = 0;
		int maxEnchantLevel = 0;
		int minEnchantLevel = 0;
		if (item.getItem().getType2() == Item.TYPE2_WEAPON)
		{
			if (blessedScroll)
			{
				for (int blessedweaponscroll : BLESSED_WEAPON_SCROLLS)
				{
					if (scroll.getItemId() == blessedweaponscroll)
					{
						if (item.getEnchantLevel() >= Config.BLESS_WEAPON_ENCHANT_LEVEL.size()) // the hash has size equals to
																								// max enchant, so if the actual
																								// enchant level is equal or more then max
																								// then the enchant rate is equal to last
																								// enchant rate
						{
							chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(Config.BLESS_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_WEAPON_MAX;
						break;
					}
				}
			}
			else if (crystalScroll)
			{
				for (int crystalweaponscroll : CRYSTAL_WEAPON_SCROLLS)
				{
					if (scroll.getItemId() == crystalweaponscroll)
					{
						if (item.getEnchantLevel() >= Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
						maxEnchantLevel = Config.CRYSTAL_ENCHANT_MAX;
						break;
						
					}
				}
			}
			else
			{ // normal scrolls
				for (int normalweaponscroll : NORMAL_WEAPON_SCROLLS)
				{
					if (scroll.getItemId() == normalweaponscroll)
					{
						if (item.getEnchantLevel() >= Config.NORMAL_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(Config.NORMAL_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_WEAPON_MAX;
						break;
					}
				}
			}
		}
		else if (item.getItem().getType2() == Item.TYPE2_SHIELD_ARMOR)
		{
			if (blessedScroll)
			{
				for (int blessedarmorscroll : BLESSED_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == blessedarmorscroll)
					{
						if (item.getEnchantLevel() >= Config.BLESS_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(Config.BLESS_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_ARMOR_MAX;
						break;
					}
				}
			}
			else if (crystalScroll)
			{
				for (int crystalarmorscroll : CRYSTAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == crystalarmorscroll)
					{
						if (item.getEnchantLevel() >= Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
						maxEnchantLevel = Config.CRYSTAL_ENCHANT_MAX;
						break;
					}
				}
			}
			else
			{ // normal scrolls
				for (int normalarmorscroll : NORMAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == normalarmorscroll)
					{
						if (item.getEnchantLevel() >= Config.NORMAL_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(Config.NORMAL_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_ARMOR_MAX;
						break;
					}
				}
			}
		}
		else if (item.getItem().getType2() == Item.TYPE2_ACCESSORY)
		{
			if (blessedScroll)
			{
				for (int blessedjewelscroll : BLESSED_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == blessedjewelscroll)
					{
						if (item.getEnchantLevel() >= Config.BLESS_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(Config.BLESS_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_JEWELRY_MAX;
						break;
					}
				}
			}
			else if (crystalScroll)
			{
				for (int crystaljewelscroll : CRYSTAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == crystaljewelscroll)
					{
						if (item.getEnchantLevel() >= Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						minEnchantLevel = Config.CRYSTAL_ENCHANT_MIN;
						maxEnchantLevel = Config.CRYSTAL_ENCHANT_MAX;
						break;
					}
				}
			}
			else
			{
				for (int normaljewelscroll : NORMAL_ARMOR_SCROLLS)
				{
					if (scroll.getItemId() == normaljewelscroll)
					{
						if (item.getEnchantLevel() >= Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_JEWELRY_MAX;
						break;
					}
				}
			}
		}
		
		if (((maxEnchantLevel != 0) && (item.getEnchantLevel() >= maxEnchantLevel)) || ((item.getEnchantLevel()) < minEnchantLevel))
		{
			player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}
		
		if (Config.SCROLL_STACKABLE)
		{
			scroll = player.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, player, item);
		}
		else
		{
			scroll = player.getInventory().destroyItem("Enchant", scroll, player, item);
		}
		
		if (scroll == null)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to enchant with a scroll he doesnt have", Config.DEFAULT_PUNISH);
			return;
		}
		
		if ((item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX) || ((item.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR) && (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)))
		{
			chance = 100;
		}
		
		int rndValue = Rnd.get(100);
		if (Config.ENABLE_DWARF_ENCHANT_BONUS && (player.getRace() == Race.DWARF) && (player.getLevel() >= Config.DWARF_ENCHANT_MIN_LEVEL))
		{
			rndValue -= Config.DWARF_ENCHANT_BONUS;
		}
		
		synchronized (item)
		{
			if (rndValue < chance)
			{
				if (item.getOwnerId() != player.getObjectId())
				{
					player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
					return;
				}
				
				if ((item.getItemLocation() != ItemInstance.ItemLocation.INVENTORY) && (item.getItemLocation() != ItemInstance.ItemLocation.PAPERDOLL))
				{
					player.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
					return;
				}
				
				if (item.getEnchantLevel() == 0)
				{
					sm = new SystemMessage(SystemMessageId.YOUR_S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED);
					sm.addItemName(item.getItemId());
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.YOUR_S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
					player.sendPacket(sm);
				}
				
				item.setEnchantLevel(item.getEnchantLevel() + Config.CUSTOM_ENCHANT_VALUE);
				item.updateDatabase();
			}
			else
			{
				if (crystalScroll)
				{
					sm = SystemMessage.sendString("Failed in Crystal Enchant. The enchant value of the item become " + Config.CRYSTAL_ENCHANT_MIN);
					player.sendPacket(sm);
				}
				else if (blessedScroll)
				{
					sm = new SystemMessage(SystemMessageId.FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);
					player.sendPacket(sm);
				}
				else if (item.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.THE_ENCHANTMENT_HAS_FAILED_YOUR_S1_S2_HAS_BEEN_CRYSTALLIZED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
					player.sendPacket(sm);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.THE_ENCHANTMENT_HAS_FAILED_YOUR_S1_HAS_BEEN_CRYSTALLIZED);
					sm.addItemName(item.getItemId());
					player.sendPacket(sm);
				}
				
				if (!blessedScroll && !crystalScroll)
				{
					if (item.getEnchantLevel() > 0)
					{
						sm = new SystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
						sm.addNumber(item.getEnchantLevel());
						sm.addItemName(item.getItemId());
						player.sendPacket(sm);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DISARMED);
						sm.addItemName(item.getItemId());
						player.sendPacket(sm);
					}
					
					if (item.isEquipped())
					{
						if (item.isAugmented())
						{
							item.getAugmentation().removeBonus(player);
						}
						
						final ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
						final InventoryUpdate iu = new InventoryUpdate();
						for (ItemInstance element : unequiped)
						{
							iu.addModifiedItem(element);
						}
						player.sendPacket(iu);
						
						player.broadcastUserInfo();
					}
					
					int count = item.getCrystalCount() - ((item.getItem().getCrystalCount() + 1) / 2);
					if (count < 1)
					{
						count = 1;
					}
					
					final ItemInstance destroyItem = player.getInventory().destroyItem("Enchant", item, player, null);
					if (destroyItem == null)
					{
						return;
					}
					
					final ItemInstance crystals = player.getInventory().addItem("Enchant", crystalId, count, player, destroyItem);
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
					sm.addItemName(crystals.getItemId());
					sm.addNumber(count);
					player.sendPacket(sm);
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						final InventoryUpdate iu = new InventoryUpdate();
						if (destroyItem.getCount() == 0)
						{
							iu.addRemovedItem(destroyItem);
						}
						else
						{
							iu.addModifiedItem(destroyItem);
						}
						iu.addItem(crystals);
						
						player.sendPacket(iu);
					}
					else
					{
						player.sendPacket(new ItemList(player, true));
					}
					
					final StatusUpdate su = new StatusUpdate(player.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					
					player.broadcastUserInfo();
					
					final World world = World.getInstance();
					world.removeObject(destroyItem);
				}
				else if (blessedScroll)
				{
					item.setEnchantLevel(Config.BREAK_ENCHANT);
					item.updateDatabase();
				}
				else if (crystalScroll)
				{
					item.setEnchantLevel(Config.CRYSTAL_ENCHANT_MIN);
					item.updateDatabase();
				}
			}
		}
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.sendPacket(new EnchantResult(item.getEnchantLevel())); // FIXME i'm really not sure about this...
		player.sendPacket(new ItemList(player, false)); // TODO update only the enchanted item
		player.broadcastUserInfo();
	}
}
