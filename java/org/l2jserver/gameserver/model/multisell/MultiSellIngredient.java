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

/**
 * @author programmos
 */
public class MultiSellIngredient
{
	private int _itemId;
	private int _itemCount;
	private int _enchantmentLevel;
	private boolean _isTaxIngredient;
	private boolean _mantainIngredient;
	
	public MultiSellIngredient(int itemId, int itemCount, boolean isTaxIngredient, boolean mantainIngredient)
	{
		this(itemId, itemCount, 0, isTaxIngredient, mantainIngredient);
	}
	
	public MultiSellIngredient(int itemId, int itemCount, int enchantmentLevel, boolean isTaxIngredient, boolean mantainIngredient)
	{
		setItemId(itemId);
		setItemCount(itemCount);
		setEnchantmentLevel(enchantmentLevel);
		setTaxIngredient(isTaxIngredient);
		setMantainIngredient(mantainIngredient);
	}
	
	public MultiSellIngredient(MultiSellIngredient e)
	{
		_itemId = e.getItemId();
		_itemCount = e.getItemCount();
		_enchantmentLevel = e.getEnchantmentLevel();
		_isTaxIngredient = e.isTaxIngredient();
		_mantainIngredient = e.getMantainIngredient();
	}
	
	/**
	 * @param itemId The itemId to set.
	 */
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	/**
	 * @return Returns the itemId.
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * @param itemCount The itemCount to set.
	 */
	public void setItemCount(int itemCount)
	{
		_itemCount = itemCount;
	}
	
	/**
	 * @return Returns the itemCount.
	 */
	public int getItemCount()
	{
		return _itemCount;
	}
	
	/**
	 * @param enchantmentLevel
	 */
	public void setEnchantmentLevel(int enchantmentLevel)
	{
		_enchantmentLevel = enchantmentLevel;
	}
	
	/**
	 * @return Returns the itemCount.
	 */
	public int getEnchantmentLevel()
	{
		return _enchantmentLevel;
	}
	
	public void setTaxIngredient(boolean isTaxIngredient)
	{
		_isTaxIngredient = isTaxIngredient;
	}
	
	public boolean isTaxIngredient()
	{
		return _isTaxIngredient;
	}
	
	public void setMantainIngredient(boolean mantainIngredient)
	{
		_mantainIngredient = mantainIngredient;
	}
	
	public boolean getMantainIngredient()
	{
		return _mantainIngredient;
	}
}
