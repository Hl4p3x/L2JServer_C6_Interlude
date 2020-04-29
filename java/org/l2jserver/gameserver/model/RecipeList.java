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

import org.l2jserver.gameserver.model.actor.instance.RecipeInstance;

/**
 * This class describes a Recipe used by Dwarf to craft Item. All RecipeList are made of RecipeInstance (1 line of the recipe : Item-Quantity needed).
 */
public class RecipeList
{
	/** The table containing all RecipeInstance (1 line of the recipe : Item-Quantity needed) of the RecipeList */
	private RecipeInstance[] _recipes;
	
	/** The recipe id */
	private final int _id;
	
	/** The crafting level needed to use this RecipeList */
	private final int _level;
	
	/** The item id of the recipe. */
	private final int _recipeId;
	
	/** The name of the RecipeList */
	private final String _recipeName;
	
	/** The crafting success rate when using the RecipeList */
	private final int _successRate;
	
	/** The crafting MP cost of this RecipeList */
	private final int _mpCost;
	
	/** The Identifier of the Item crafted with this RecipeList */
	private final int _itemId;
	
	/** The quantity of Item crafted when using this RecipeList */
	private final int _count;
	
	/** If this a common or a dwarven recipe */
	private final boolean _isDwarvenRecipe;
	
	/**
	 * Constructor of RecipeList (create a new Recipe).
	 * @param id
	 * @param level
	 * @param recipeId
	 * @param recipeName
	 * @param successRate
	 * @param mpCost
	 * @param itemId
	 * @param count
	 * @param isDwarvenRecipe
	 */
	public RecipeList(int id, int level, int recipeId, String recipeName, int successRate, int mpCost, int itemId, int count, boolean isDwarvenRecipe)
	{
		_id = id;
		_recipes = new RecipeInstance[0];
		_level = level;
		_recipeId = recipeId;
		_recipeName = recipeName;
		_successRate = successRate;
		_mpCost = mpCost;
		_itemId = itemId;
		_count = count;
		_isDwarvenRecipe = isDwarvenRecipe;
	}
	
	/**
	 * Add a RecipeInstance to the RecipeList (add a line Item-Quantity needed to the Recipe).
	 * @param recipe
	 */
	public void addRecipe(RecipeInstance recipe)
	{
		final int len = _recipes.length;
		final RecipeInstance[] tmp = new RecipeInstance[len + 1];
		System.arraycopy(_recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipes = tmp;
	}
	
	/**
	 * @return the recipe id of the recipe.
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the crafting level needed to use this RecipeList.
	 */
	public int getLevel()
	{
		return _level;
	}
	
	/**
	 * @return the item id of the recipe.
	 */
	public int getRecipeId()
	{
		return _recipeId;
	}
	
	/**
	 * @return the name of the RecipeList.
	 */
	public String getRecipeName()
	{
		return _recipeName;
	}
	
	/**
	 * @return the crafting success rate when using the RecipeList.
	 */
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	/**
	 * @return the crafting MP cost of this RecipeList.
	 */
	public int getMpCost()
	{
		return _mpCost;
	}
	
	/**
	 * @return true if the Item crafted with this RecipeList is consumable (shot, arrow,...).
	 */
	public boolean isConsumable()
	{
		return ((_itemId >= 1463) && (_itemId <= 1467)) || ((_itemId >= 2509) && (_itemId <= 2514)) || ((_itemId >= 3947) && (_itemId <= 3952)) || ((_itemId >= 1341) && (_itemId <= 1345));
	}
	
	/**
	 * @return the Identifier of the Item crafted with this RecipeList.
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * @return the quantity of Item crafted when using this RecipeList.
	 */
	public int getCount()
	{
		return _count;
	}
	
	/**
	 * @return true if this a Dwarven recipe or false if its a Common recipe
	 */
	public boolean isDwarvenRecipe()
	{
		return _isDwarvenRecipe;
	}
	
	/**
	 * @return the table containing all RecipeInstance (1 line of the recipe : Item-Quantity needed) of the RecipeList.
	 */
	public RecipeInstance[] getRecipes()
	{
		return _recipes;
	}
}
