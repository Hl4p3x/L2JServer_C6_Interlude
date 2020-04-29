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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.model.Augmentation;
import org.l2jserver.gameserver.model.PlayerInventory;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Armor;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.multisell.MultiSellEntry;
import org.l2jserver.gameserver.model.multisell.MultiSellIngredient;
import org.l2jserver.gameserver.model.multisell.MultiSellListContainer;
import org.l2jserver.gameserver.model.multisell.Multisell;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * The Class MultiSellChoose.
 */
public class MultiSellChoose extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(MultiSellChoose.class.getName());
	private int _listId;
	private int _entryId;
	private int _amount;
	private int _enchantment;
	private int _transactionTax; // local handling of taxation
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readD();
		// _enchantment = readH(); // Commented this line because it did NOT work!
		_enchantment = _entryId % 100000;
		_entryId = _entryId / 100000;
		_transactionTax = 0; // Initialize tax amount to 0...
	}
	
	@Override
	public void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getMultiSell().tryPerformAction("multisell choose"))
		{
			player.setMultiSellId(-1);
			return;
		}
		
		if ((_amount < 1) || (_amount > 5000))
		{
			player.setMultiSellId(-1);
			return;
		}
		
		final NpcInstance merchant = player.getTarget() instanceof NpcInstance ? (NpcInstance) player.getTarget() : null;
		
		// Possible fix to Multisell Radius
		if ((merchant == null) || !player.isInsideRadius(merchant, NpcInstance.INTERACTION_DISTANCE, false, false))
		{
			player.setMultiSellId(-1);
			return;
		}
		
		final MultiSellListContainer list = Multisell.getInstance().getList(_listId);
		final int selectedList = player.getMultiSellId();
		if ((list == null) || (list.getListId() != _listId) || (selectedList != _listId))
		{
			player.setMultiSellId(-1);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setMultiSellId(-1);
			return;
		}
		
		for (MultiSellEntry entry : list.getEntries())
		{
			if (entry.getEntryId() == _entryId)
			{
				doExchange(player, entry, list.getApplyTaxes(), list.getMaintainEnchantment(), _enchantment);
				
				// dnt change multisell on exchange to avoid new window open need
				// player.setMultiSellId(-1);
				return;
			}
		}
	}
	
	private void doExchange(PlayerInstance player, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantment)
	{
		final PlayerInventory inv = player.getInventory();
		boolean maintainItemFound = false;
		
		// given the template entry and information about maintaining enchantment and applying taxes re-create the instance of
		// the entry that will be used for this exchange i.e. change the enchantment level of select ingredient/products and adena amount appropriately.
		final NpcInstance merchant = player.getTarget() instanceof NpcInstance ? (NpcInstance) player.getTarget() : null;
		final MultiSellEntry entry = prepareEntry(merchant, templateEntry, applyTaxes, maintainEnchantment, enchantment);
		
		// Generate a list of distinct ingredients and counts in order to check if the correct item-counts
		// are possessed by the player
		final List<MultiSellIngredient> ingredientsList = new ArrayList<>();
		boolean newIng = true;
		for (MultiSellIngredient e : entry.getIngredients())
		{
			newIng = true;
			
			// at this point, the template has already been modified so that enchantments are properly included
			// whenever they need to be applied. Uniqueness of items is thus judged by item id AND enchantment level
			for (MultiSellIngredient ex : ingredientsList)
			{
				// if the item was already added in the list, merely increment the count
				// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
				if ((ex.getItemId() == e.getItemId()) && (ex.getEnchantmentLevel() == e.getEnchantmentLevel()))
				{
					if (((double) ex.getItemCount() + e.getItemCount()) > Integer.MAX_VALUE)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
						ingredientsList.clear();
						return;
					}
					ex.setItemCount(ex.getItemCount() + e.getItemCount());
					newIng = false;
				}
			}
			if (newIng)
			{
				// If there is a maintainIngredient, then we do not need to check the enchantment parameter as the enchant level will be checked elsewhere
				if (maintainEnchantment)
				{
					maintainItemFound = true;
				}
				
				// if it's a new ingredient, just store its info directly (item id, count, enchantment)
				ingredientsList.add(new MultiSellIngredient(e));
			}
		}
		
		// If there is no maintainIngredient, then we must make sure that the enchantment is not kept from the client packet, as it may have been forged
		if (!maintainItemFound)
		{
			for (MultiSellIngredient product : entry.getProducts())
			{
				product.setEnchantmentLevel(0);
			}
		}
		
		// now check if the player has sufficient items in the inventory to cover the ingredients' expences
		for (MultiSellIngredient e : ingredientsList)
		{
			if ((e.getItemCount() * _amount) > Integer.MAX_VALUE)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				ingredientsList.clear();
				return;
			}
			
			if ((e.getItemId() != 65336) && (e.getItemId() != 65436))
			{
				// if this is not a list that maintains enchantment, check the count of all items that have the given id.
				// otherwise, check only the count of items with exactly the needed enchantment level
				if (inv.getInventoryItemCount(e.getItemId(), maintainEnchantment ? e.getEnchantmentLevel() : -1) < (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient() ? e.getItemCount() * _amount : e.getItemCount()))
				{
					player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
					ingredientsList.clear();
					return;
				}
			}
			else
			{
				if (e.getItemId() == 65336)
				{
					if (player.getClan() == null)
					{
						player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION);
						return;
					}
					
					if (!player.isClanLeader())
					{
						player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
						return;
					}
					
					if (player.getClan().getReputationScore() < (e.getItemCount() * _amount))
					{
						player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
						return;
					}
				}
				if ((e.getItemId() == 65436) && ((e.getItemCount() * _amount) > player.getPcBangScore()))
				{
					player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
					return;
				}
			}
		}
		
		ingredientsList.clear();
		final List<Augmentation> augmentation = new ArrayList<>();
		/** All ok, remove items and add final product */
		for (MultiSellIngredient e : entry.getIngredients())
		{
			if ((e.getItemId() != 65336) && (e.getItemId() != 65436))
			{
				for (MultiSellIngredient a : entry.getProducts())
				{
					if ((player.getInventoryLimit() < (inv.getSize() + _amount)) && !ItemTable.getInstance().createDummyItem(a.getItemId()).isStackable())
					{
						player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
						return;
					}
					if ((player.getInventoryLimit() < inv.getSize()) && ItemTable.getInstance().createDummyItem(a.getItemId()).isStackable())
					{
						player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
						return;
					}
				}
				ItemInstance itemToTake = inv.getItemByItemId(e.getItemId()); // initialize and initial guess for the item to take.
				
				// this is a cheat, transaction will be aborted and if any items already tanken will not be returned back to inventory!
				if (itemToTake == null)
				{
					LOGGER.warning("Character: " + player.getName() + " is trying to cheat in multisell, merchatnt id:" + (merchant != null ? merchant.getNpcId() : 0));
					return;
				}
				
				if (itemToTake.isWear())
				{
					LOGGER.warning("Character: " + player.getName() + " is trying to cheat in multisell with weared item");
					return;
				}
				
				if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient())
				{
					// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
					if (itemToTake.isStackable())
					{
						if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * _amount), player.getTarget(), true))
						{
							return;
						}
					}
					else // a) if enchantment is maintained, then get a list of items that exactly match this enchantment
					if (maintainEnchantment)
					{
						// loop through this list and remove (one by one) each item until the required amount is taken.
						final ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantmentLevel());
						for (int i = 0; i < (e.getItemCount() * _amount); i++)
						{
							if (inventoryContents[i].isAugmented())
							{
								augmentation.add(inventoryContents[i].getAugmentation());
							}
							
							if (inventoryContents[i].isEquipped() && inventoryContents[i].isAugmented())
							{
								inventoryContents[i].getAugmentation().removeBonus(player);
							}
							
							if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
							{
								return;
							}
						}
					}
					else
					// b) enchantment is not maintained. Get the instances with the LOWEST enchantment level
					{
						/*
						 * NOTE: There are 2 ways to achieve the above goal. 1) Get all items that have the correct itemId, loop through them until the lowest enchantment level is found. Repeat all this for the next item until proper count of items is reached. 2) Get all items that have the correct
						 * itemId, sort them once based on enchantment level, and get the range of items that is necessary. Method 1 is faster for a small number of items to be exchanged. Method 2 is faster for large amounts. EXPLANATION: Worst case scenario for algorithm 1 will make it run in a
						 * number of cycles given by: m*(2n-m+1)/2 where m is the number of items to be exchanged and n is the total number of inventory items that have a matching id. With algorithm 2 (sort), sorting takes n*LOGGER(n) time and the choice is done in a single cycle for case b (just
						 * grab the m first items) or in linear time for case a (find the beginning of items with correct enchantment, index x, and take all items from x to x+m). Basically, whenever m > LOGGER(n) we have: m*(2n-m+1)/2 = (2nm-m*m+m)/2 > (2nlogn-logn*logn+logn)/2 = nlog(n) -
						 * LOGGER(n*n) + LOGGER(n) = nlog(n) + LOGGER(n/n*n) = nlog(n) + LOGGER(1/n) = nlog(n) - LOGGER(n) = (n-1)LOGGER(n) So for m < LOGGER(n) then m*(2n-m+1)/2 > (n-1)LOGGER(n) and m*(2n-m+1)/2 > nlog(n) IDEALLY: In order to best optimize the performance, choose which algorithm to
						 * run, based on whether 2^m > n if ( (2<<(e.getItemCount() * _amount)) < inventoryContents.length ) // do Algorithm 1, no sorting else // do Algorithm 2, sorting CURRENT IMPLEMENTATION: In general, it is going to be very rare for a person to do a massive exchange of
						 * non-stackable items For this reason, we assume that algorithm 1 will always suffice and we keep things simple. If, in the future, it becomes necessary that we optimize, the above discussion should make it clear what optimization exactly is necessary (based on the comments
						 * under "IDEALLY").
						 */
						
						// choice 1. Small number of items exchanged. No sorting.
						for (int i = 1; i <= (e.getItemCount() * _amount); i++)
						{
							final ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());
							itemToTake = inventoryContents[0];
							// get item with the LOWEST enchantment level from the inventory...
							// +0 is lowest by default...
							if (itemToTake.getEnchantLevel() > 0)
							{
								for (ItemInstance inventoryContent : inventoryContents)
								{
									if (inventoryContent.getEnchantLevel() < itemToTake.getEnchantLevel())
									{
										itemToTake = inventoryContent;
										// nothing will have enchantment less than 0. If a zero-enchanted
										// item is found, just take it
										if (itemToTake.getEnchantLevel() == 0)
										{
											break;
										}
									}
								}
							}
							
							if (itemToTake.isEquipped() && itemToTake.isAugmented())
							{
								itemToTake.getAugmentation().removeBonus(player);
							}
							
							if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
							{
								return;
							}
						}
					}
				}
			}
			else if (e.getItemId() == 65336)
			{
				final int repCost = player.getClan().getReputationScore() - e.getItemCount();
				player.getClan().setReputationScore(repCost, true);
				player.sendPacket(new SystemMessage(SystemMessageId.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION_SCORE).addNumber(e.getItemCount()));
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
			}
			else
			{
				player.reducePcBangScore(e.getItemCount() * _amount);
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_USING_S1_POINT).addNumber(e.getItemCount()));
			}
		}
		// Generate the appropriate items
		for (MultiSellIngredient e : entry.getProducts())
		{
			if (ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable())
			{
				inv.addItem("Multisell[" + _listId + "]", e.getItemId(), (e.getItemCount() * _amount), player, player.getTarget());
			}
			else
			{
				ItemInstance product = null;
				for (int i = 0; i < (e.getItemCount() * _amount); i++)
				{
					product = inv.addItem("Multisell[" + _listId + "]", e.getItemId(), 1, player, player.getTarget());
					if (maintainEnchantment && (product != null))
					{
						if (i < augmentation.size())
						{
							product.setAugmentation(new Augmentation(product, augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill(), true));
						}
						product.setEnchantLevel(e.getEnchantmentLevel());
					}
				}
			}
			// Msg part
			SystemMessage sm;
			if ((e.getItemCount() * _amount) > 1)
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				sm.addItemName(e.getItemId());
				sm.addNumber(e.getItemCount() * _amount);
				player.sendPacket(sm);
			}
			else
			{
				if (maintainEnchantment && (e.getEnchantmentLevel() > 0))
				{
					sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_S2);
					sm.addNumber(e.getEnchantmentLevel());
					sm.addItemName(e.getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
					sm.addItemName(e.getItemId());
				}
				player.sendPacket(sm);
			}
		}
		player.sendPacket(new ItemList(player, false));
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.broadcastUserInfo();
		
		// Finally, give the tax to the castle...
		if ((merchant != null) && merchant.isInTown() && (merchant.getCastle().getOwnerId() > 0))
		{
			merchant.getCastle().addToTreasury(_transactionTax * _amount);
		}
	}
	
	// Regarding taxation, the following appears to be the case:
	// a) The count of aa remains unchanged (taxes do not affect aa directly).
	// b) 5/6 of the amount of aa is taxed by the normal tax rate.
	// c) the resulting taxes are added as normal adena value.
	// d) normal adena are taxed fully.
	// e) Items other than adena and ancient adena are not taxed even when the list is taxable.
	// example: If the template has an item worth 120aa, and the tax is 10%,
	// then from 120aa, take 5/6 so that is 100aa, apply the 10% tax in adena (10a)
	// so the final price will be 120aa and 10a!
	private MultiSellEntry prepareEntry(NpcInstance merchant, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
	{
		final MultiSellEntry newEntry = new MultiSellEntry();
		newEntry.setEntryId(templateEntry.getEntryId());
		int totalAdenaCount = 0;
		boolean hasIngredient = false;
		for (MultiSellIngredient ing : templateEntry.getIngredients())
		{
			// Load the ingredient from the template
			final MultiSellIngredient newIngredient = new MultiSellIngredient(ing);
			if ((newIngredient.getItemId() == 57) && newIngredient.isTaxIngredient())
			{
				double taxRate = 0.0;
				if (applyTaxes && (merchant != null) && merchant.isInTown())
				{
					taxRate = merchant.getCastle().getTaxRate();
				}
				
				_transactionTax = (int) Math.round(newIngredient.getItemCount() * taxRate);
				totalAdenaCount += _transactionTax;
				continue; // Do not yet add this adena amount to the list as non-taxIngredient adena might be entered later (order not guaranteed)
			}
			else if (ing.getItemId() == 57) // && !ing.isTaxIngredient()
			{
				totalAdenaCount += newIngredient.getItemCount();
				continue; // Do not yet add this adena amount to the list as taxIngredient adena might be entered later (order not guaranteed)
			}
			// If it is an armor/weapon, modify the enchantment level appropriately, if necessary
			else if (maintainEnchantment)
			{
				final Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				if ((tempItem instanceof Armor) || (tempItem instanceof Weapon))
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
					hasIngredient = true;
				}
			}
			
			// finally, add this ingredient to the entry
			newEntry.addIngredient(newIngredient);
		}
		// Next add the adena amount, if any
		if (totalAdenaCount > 0)
		{
			newEntry.addIngredient(new MultiSellIngredient(57, totalAdenaCount, false, false));
		}
		
		// Now modify the enchantment level of products, if necessary
		for (MultiSellIngredient ing : templateEntry.getProducts())
		{
			// Load the ingredient from the template
			final MultiSellIngredient newIngredient = new MultiSellIngredient(ing);
			if (maintainEnchantment && hasIngredient)
			{
				// If it is an armor/weapon, modify the enchantment level appropriately
				// (note, if maintain enchantment is "false" this modification will result to a +0)
				final Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				if ((tempItem instanceof Armor) || (tempItem instanceof Weapon))
				{
					if ((enchantLevel == 0) && maintainEnchantment)
					{
						enchantLevel = ing.getEnchantmentLevel();
					}
					newIngredient.setEnchantmentLevel(enchantLevel);
				}
			}
			newEntry.addProduct(newIngredient);
		}
		return newEntry;
	}
}