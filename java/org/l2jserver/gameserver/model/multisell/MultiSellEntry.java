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
package org.l2jserver.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author programmos
 */
public class MultiSellEntry
{
	private int _entryId;
	
	private final List<MultiSellIngredient> _products = new ArrayList<>();
	private final List<MultiSellIngredient> _ingredients = new ArrayList<>();
	
	/**
	 * @param entryId The entryId to set.
	 */
	public void setEntryId(int entryId)
	{
		_entryId = entryId;
	}
	
	/**
	 * @return Returns the entryId.
	 */
	public int getEntryId()
	{
		return _entryId;
	}
	
	/**
	 * @param product The product to add.
	 */
	public void addProduct(MultiSellIngredient product)
	{
		_products.add(product);
	}
	
	/**
	 * @return Returns the products.
	 */
	public List<MultiSellIngredient> getProducts()
	{
		return _products;
	}
	
	/**
	 * @param ingredient The ingredients to set.
	 */
	public void addIngredient(MultiSellIngredient ingredient)
	{
		_ingredients.add(ingredient);
	}
	
	/**
	 * @return Returns the ingredients.
	 */
	public List<MultiSellIngredient> getIngredients()
	{
		return _ingredients;
	}
}
