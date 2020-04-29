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

import java.util.List;

/**
 * @author -Nemesiss-
 */
public class ExtractableItem
{
	private final int _itemId;
	private final List<ExtractableProductItem> _products;
	
	public ExtractableItem(int itemid, List<ExtractableProductItem> products)
	{
		_itemId = itemid;
		_products = products;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public List<ExtractableProductItem> getProductItems()
	{
		return _products;
	}
}
