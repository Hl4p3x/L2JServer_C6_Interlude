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
package org.l2jserver.gameserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.xml.RecipeData;
import org.l2jserver.gameserver.model.Inventory;
import org.l2jserver.gameserver.model.ManufactureItem;
import org.l2jserver.gameserver.model.RecipeList;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RecipeInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.RecipeBookItemList;
import org.l2jserver.gameserver.network.serverpackets.RecipeItemMakeInfo;
import org.l2jserver.gameserver.network.serverpackets.RecipeShopItemInfo;
import org.l2jserver.gameserver.network.serverpackets.SetupGauge;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

public class RecipeController
{
	protected static final Logger LOGGER = Logger.getLogger(RecipeController.class.getName());
	
	private static RecipeController INSTANCE;
	protected static final Map<PlayerInstance, RecipeItemMaker> _activeMakers = Collections.synchronizedMap(new WeakHashMap<PlayerInstance, RecipeItemMaker>());
	
	public static RecipeController getInstance()
	{
		return INSTANCE == null ? INSTANCE = new RecipeController() : INSTANCE;
	}
	
	public synchronized void requestBookOpen(PlayerInstance player, boolean isDwarvenCraft)
	{
		RecipeItemMaker maker = null;
		if (Config.ALT_GAME_CREATION)
		{
			maker = _activeMakers.get(player);
		}
		
		if (maker == null)
		{
			final RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, player.getMaxMp());
			response.addRecipes(isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook());
			player.sendPacket(response);
			return;
		}
		
		player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING));
	}
	
	public synchronized void requestMakeItemAbort(PlayerInstance player)
	{
		_activeMakers.remove(player); // TODO: anything else here?
	}
	
	public synchronized void requestManufactureItem(PlayerInstance manufacturer, int recipeListId, PlayerInstance player)
	{
		final RecipeList recipeList = getValidRecipeList(player, recipeListId);
		if (recipeList == null)
		{
			return;
		}
		
		final List<RecipeList> dwarfRecipes = Arrays.asList(manufacturer.getDwarvenRecipeBook());
		final List<RecipeList> commonRecipes = Arrays.asList(manufacturer.getCommonRecipeBook());
		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		RecipeItemMaker maker;
		if (Config.ALT_GAME_CREATION && ((maker = _activeMakers.get(manufacturer)) != null)) // check if busy
		{
			player.sendMessage("Manufacturer is busy, please try later.");
			return;
		}
		
		maker = new RecipeItemMaker(manufacturer, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(manufacturer, maker);
				ThreadPool.schedule(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
	}
	
	public synchronized void requestMakeItem(PlayerInstance player, int recipeListId)
	{
		if (player.isInDuel())
		{
			player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return;
		}
		
		final RecipeList recipeList = getValidRecipeList(player, recipeListId);
		if (recipeList == null)
		{
			return;
		}
		
		final List<RecipeList> dwarfRecipes = Arrays.asList(player.getDwarvenRecipeBook());
		final List<RecipeList> commonRecipes = Arrays.asList(player.getCommonRecipeBook());
		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		RecipeItemMaker maker;
		
		// check if already busy (possible in alt mode only)
		if (Config.ALT_GAME_CREATION && ((maker = _activeMakers.get(player)) != null))
		{
			player.sendMessage("You are busy creating " + ItemTable.getInstance().getTemplate(recipeList.getItemId()).getName());
			return;
		}
		
		maker = new RecipeItemMaker(player, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(player, maker);
				ThreadPool.schedule(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
	}
	
	private class RecipeItemMaker implements Runnable
	{
		protected boolean _isValid;
		protected List<TempItem> _items = null;
		protected final RecipeList _recipeList;
		protected final PlayerInstance _player; // "crafter"
		protected final PlayerInstance _target; // "customer"
		protected final Skill _skill;
		protected final int _skillId;
		protected final int _skillLevel;
		protected double _creationPasses;
		protected double _manaRequired;
		protected int _price;
		protected int _totalItems;
		protected int _delay;
		
		public RecipeItemMaker(PlayerInstance pPlayer, RecipeList pRecipeList, PlayerInstance pTarget)
		{
			_player = pPlayer;
			_target = pTarget;
			_recipeList = pRecipeList;
			_isValid = false;
			_skillId = _recipeList.isDwarvenRecipe() ? Skill.SKILL_CREATE_DWARVEN : Skill.SKILL_CREATE_COMMON;
			_skillLevel = _player.getSkillLevel(_skillId);
			_skill = _player.getKnownSkill(_skillId);
			_player.setCrafting(true);
			
			if (_player.isAlikeDead())
			{
				_player.sendMessage("Dead people don't craft.");
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isAlikeDead())
			{
				_target.sendMessage("Dead customers can't use manufacture.");
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isProcessingTransaction())
			{
				_target.sendMessage("You are busy.");
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_player.isProcessingTransaction())
			{
				if (_player != _target)
				{
					_target.sendMessage("Manufacturer " + _player.getName() + " is busy.");
				}
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			// validate recipe list
			if ((_recipeList == null) || (_recipeList.getRecipes().length == 0))
			{
				_player.sendMessage("No such recipe");
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			_manaRequired = _recipeList.getMpCost();
			
			// validate skill level
			if (_recipeList.getLevel() > _skillLevel)
			{
				_player.sendMessage("Need skill level " + _recipeList.getLevel());
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			// check that customer can afford to pay for creation services
			if (_player != _target)
			{
				for (ManufactureItem temp : _player.getCreateList().getList())
				{
					if (temp.getRecipeId() == _recipeList.getId()) // find recipe for item we want manufactured
					{
						_price = temp.getCost();
						if (_target.getAdena() < _price) // check price
						{
							_target.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
							abort();
							return;
						}
						break;
					}
				}
			}
			
			_items = listItems(false);
			// make temporary items
			if (_items == null)
			{
				abort();
				return;
			}
			
			// calculate reference price
			for (TempItem i : _items)
			{
				_totalItems += i.getQuantity();
			}
			// initial mana check requires MP as written on recipe
			if (_player.getCurrentMp() < _manaRequired)
			{
				_target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
				abort();
				return;
			}
			
			// determine number of creation passes needed
			// can "equip" skillLevel items each pass
			_creationPasses = (_totalItems / _skillLevel) + ((_totalItems % _skillLevel) != 0 ? 1 : 0);
			if (Config.ALT_GAME_CREATION && (_creationPasses != 0))
			{
				_manaRequired /= _creationPasses; // checks to validateMp() will only need portion of mp for one pass
			}
			
			updateMakeInfo(true);
			updateCurMp();
			updateCurLoad();
			
			_player.setCrafting(false);
			_isValid = true;
		}
		
		@Override
		public void run()
		{
			if (!Config.IS_CRAFTING_ENABLED)
			{
				_target.sendMessage("Item creation is currently disabled.");
				abort();
				return;
			}
			
			if ((_player == null) || (_target == null))
			{
				LOGGER.warning("Player or target == null (disconnected?), aborting" + _target + _player);
				abort();
				return;
			}
			
			if (!_player.isOnline() || !_target.isOnline())
			{
				LOGGER.warning("Player or target is not online, aborting " + _target + _player);
				abort();
				return;
			}
			
			if (Config.ALT_GAME_CREATION && (_activeMakers.get(_player) == null))
			{
				if (_target != _player)
				{
					_target.sendMessage("Manufacture aborted.");
					_player.sendMessage("Manufacture aborted.");
				}
				else
				{
					_player.sendMessage("Item creation aborted.");
				}
				
				abort();
				return;
			}
			
			if (Config.ALT_GAME_CREATION && !_items.isEmpty())
			{
				// check mana
				if (!validateMp())
				{
					return;
				}
				// use some mp
				_player.reduceCurrentMp(_manaRequired);
				// update craft window mp bar
				updateCurMp();
				
				// grab (equip) some more items with a nice msg to player
				grabSomeItems();
				
				// if still not empty, schedule another pass
				if (!_items.isEmpty())
				{
					// divided by RATE_CONSUMABLES_COST to remove craft time increase on higher consumables rates
					_delay = (int) ((Config.ALT_GAME_CREATION_SPEED * _player.getMReuseRate(_skill) * GameTimeController.TICKS_PER_SECOND) / Config.RATE_CONSUMABLE_COST) * GameTimeController.MILLIS_IN_TICK;
					
					// FIXME: please fix this packet to show crafting animation (somebody)
					_player.broadcastPacket(new MagicSkillUse(_player, _skillId, _skillLevel, _delay, 0));
					_player.sendPacket(new SetupGauge(0, _delay));
					ThreadPool.schedule(this, 100 + _delay);
				}
				else
				{
					// for alt mode, sleep delay msec before finishing
					_player.sendPacket(new SetupGauge(0, _delay));
					
					try
					{
						Thread.sleep(_delay);
					}
					catch (Exception e)
					{
						// Ignore.
					}
					finally
					{
						finishCrafting();
					}
				}
			}
			// for old craft mode just finish
			else
			{
				finishCrafting();
			}
		}
		
		private void finishCrafting()
		{
			if (!Config.ALT_GAME_CREATION)
			{
				_player.reduceCurrentMp(_manaRequired);
			}
			
			// first take adena for manufacture
			if ((_target != _player) && (_price > 0)) // customer must pay for services
			{
				// attempt to pay for item
				final ItemInstance adenatransfer = _target.transferItem("PayManufacture", _target.getInventory().getAdenaInstance().getObjectId(), _price, _player.getInventory(), _player);
				if (adenatransfer == null)
				{
					_target.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					abort();
					return;
				}
			}
			
			_items = listItems(true);
			// this line actually takes materials from inventory
			if (_items == null)
			{
				// handle possible cheaters here
				// (they click craft then try to get rid of items in order to get free craft)
			}
			else if (Rnd.get(100) < _recipeList.getSuccessRate())
			{
				rewardPlayer(); // and immediately puts created item in its place
				updateMakeInfo(true);
			}
			else
			{
				_player.sendMessage("Item(s) failed to create");
				if (_target != _player)
				{
					_target.sendMessage("Item(s) failed to create");
				}
				
				updateMakeInfo(false);
			}
			// update load and mana bar of craft window
			updateCurMp();
			updateCurLoad();
			_activeMakers.remove(_player);
			_player.setCrafting(false);
			_target.sendPacket(new ItemList(_target, false));
		}
		
		private void updateMakeInfo(boolean success)
		{
			if (_target == _player)
			{
				_target.sendPacket(new RecipeItemMakeInfo(_recipeList.getId(), _target, success));
			}
			else
			{
				_target.sendPacket(new RecipeShopItemInfo(_player, _recipeList.getId()));
			}
		}
		
		private void updateCurLoad()
		{
			final StatusUpdate su = new StatusUpdate(_target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, _target.getCurrentLoad());
			_target.sendPacket(su);
		}
		
		private void updateCurMp()
		{
			final StatusUpdate su = new StatusUpdate(_target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_MP, (int) _target.getCurrentMp());
			_target.sendPacket(su);
		}
		
		private void grabSomeItems()
		{
			int numItems = _skillLevel;
			
			while ((numItems > 0) && !_items.isEmpty())
			{
				final TempItem item = _items.get(0);
				int count = item.getQuantity();
				if (count >= numItems)
				{
					count = numItems;
				}
				
				item.setQuantity(item.getQuantity() - count);
				if (item.getQuantity() <= 0)
				{
					_items.remove(0);
				}
				else
				{
					_items.set(0, item);
				}
				
				numItems -= count;
				if (_target == _player)
				{
					// you equipped ...
					final SystemMessage sm = new SystemMessage(SystemMessageId.EQUIPPED_S1_S2);
					sm.addNumber(count);
					sm.addItemName(item.getItemId());
					_player.sendPacket(sm);
				}
				else
				{
					_target.sendMessage("Manufacturer " + _player.getName() + " used " + count + " " + item.getItemName());
				}
			}
		}
		
		private boolean validateMp()
		{
			if (_player.getCurrentMp() < _manaRequired)
			{
				// rest (wait for MP)
				if (Config.ALT_GAME_CREATION)
				{
					_player.sendPacket(new SetupGauge(0, _delay));
					ThreadPool.schedule(this, 100 + _delay);
				}
				// no rest - report no mana
				else
				{
					_target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					abort();
				}
				return false;
			}
			return true;
		}
		
		private List<TempItem> listItems(boolean remove)
		{
			final RecipeInstance[] recipes = _recipeList.getRecipes();
			final Inventory inv = _target.getInventory();
			final List<TempItem> materials = new ArrayList<>();
			for (RecipeInstance recipe : recipes)
			{
				final int quantity = _recipeList.isConsumable() ? (int) (recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) : recipe.getQuantity();
				if (quantity > 0)
				{
					final ItemInstance item = inv.getItemByItemId(recipe.getItemId());
					
					// check materials
					if ((item == null) || (item.getCount() < quantity))
					{
						_target.sendMessage("You dont have the right elements for making this item" + (_recipeList.isConsumable() && (Config.RATE_CONSUMABLE_COST != 1) ? ".\nDue to server rates you need " + Config.RATE_CONSUMABLE_COST + "x more material than listed in recipe" : ""));
						abort();
						return null;
					}
					
					// make new temporary object, just for counting puroses
					
					final TempItem temp = new TempItem(item, quantity);
					materials.add(temp);
				}
			}
			
			if (remove)
			{
				for (TempItem tmp : materials)
				{
					inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), _target, _player);
				}
			}
			return materials;
		}
		
		private void abort()
		{
			updateMakeInfo(false);
			_player.setCrafting(false);
			_activeMakers.remove(_player);
		}
		
		/**
		 * FIXME: This class should be in some other file, but I don't know where Class explanation: For item counting or checking purposes. When you don't want to modify inventory class contains itemId, quantity, ownerId, referencePrice, but not objectId
		 */
		private class TempItem
		{
			// no object id stored, this will be only "list" of items with it's owner
			private final int _itemId;
			private int _quantity;
			private final String _itemName;
			
			/**
			 * @param item
			 * @param quantity of that item
			 */
			public TempItem(ItemInstance item, int quantity)
			{
				super();
				_itemId = item.getItemId();
				_quantity = quantity;
				_itemName = item.getItem().getName();
			}
			
			/**
			 * @return Returns the quantity.
			 */
			public int getQuantity()
			{
				return _quantity;
			}
			
			/**
			 * @param quantity The quantity to set.
			 */
			public void setQuantity(int quantity)
			{
				_quantity = quantity;
			}
			
			/**
			 * @return Returns the itemId.
			 */
			public int getItemId()
			{
				return _itemId;
			}
			
			/**
			 * @return Returns the itemName.
			 */
			public String getItemName()
			{
				return _itemName;
			}
		}
		
		private void rewardPlayer()
		{
			final int itemId = _recipeList.getItemId();
			final int itemCount = _recipeList.getCount();
			final ItemInstance createdItem = _target.getInventory().addItem("Manufacture", itemId, itemCount, _target, _player);
			
			// inform customer of earned item
			SystemMessage sm = null;
			if (itemCount > 1)
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				sm.addItemName(itemId);
				sm.addNumber(itemCount);
				_target.sendPacket(sm);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
				sm.addItemName(itemId);
				_target.sendPacket(sm);
			}
			
			if (_target != _player)
			{
				// inform manufacturer of earned profit
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_ADENA);
				sm.addNumber(_price);
				_player.sendPacket(sm);
			}
			
			if (Config.ALT_GAME_CREATION)
			{
				final int recipeLevel = _recipeList.getLevel();
				int exp = createdItem.getReferencePrice() * itemCount;
				// one variation
				
				// exp -= materialsRefPrice;
				// mat. ref. price is not accurate so other method is better
				if (exp < 0)
				{
					exp = 0;
				}
				
				// another variation
				exp /= recipeLevel;
				for (int i = _skillLevel; i > recipeLevel; i--)
				{
					exp /= 4;
				}
				
				final int sp = exp / 10;
				
				// Added multiplication of Creation speed with XP/SP gain
				// slower crafting -> more XP, faster crafting -> less XP
				// you can use ALT_GAME_CREATION_XP_RATE/SP to
				// modify XP/SP gained (default = 1)
				_player.addExpAndSp((int) _player.calcStat(Stat.EXPSP_RATE, exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null), (int) _player.calcStat(Stat.EXPSP_RATE, sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
			}
			updateMakeInfo(true); // success
		}
	}
	
	private RecipeList getValidRecipeList(PlayerInstance player, int id)
	{
		final RecipeList recipeList = RecipeData.getInstance().getRecipe(id);
		if ((recipeList == null) || (recipeList.getRecipes().length == 0))
		{
			player.sendMessage("No recipe for: " + id);
			player.setCrafting(false);
			return null;
		}
		return recipeList;
	}
}
