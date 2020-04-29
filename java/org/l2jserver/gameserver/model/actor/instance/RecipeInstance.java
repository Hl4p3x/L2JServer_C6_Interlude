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

/**
 * This class describes a RecipeList component (1 line of the recipe : Item-Quantity needed).
 */
public class RecipeInstance
{
	private final int _itemId;
	private final int _quantity;
	
	/**
	 * Constructor of RecipeInstance (create a new line in a RecipeList).
	 * @param itemId the item id
	 * @param quantity the quantity
	 */
	public RecipeInstance(int itemId, int quantity)
	{
		_itemId = itemId;
		_quantity = quantity;
	}
	
	/**
	 * Return the Identifier of the RecipeInstance Item needed.
	 * @return the item id
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Return the Item quantity needed of the RecipeInstance.
	 * @return the quantity
	 */
	public int getQuantity()
	{
		return _quantity;
	}
}
