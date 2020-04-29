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
package org.l2jserver.gameserver.handler;

import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

public interface IItemHandler
{
	/**
	 * Launch task associated to the item.
	 * @param playable : PlayableInstance designating the player
	 * @param item : ItemInstance designating the item to use
	 */
	void useItem(Playable playable, ItemInstance item);
	
	/**
	 * Returns the list of item IDs corresponding to the type of item.<br>
	 * <b><i>Use :</i></u><br>
	 * This method is called at initialization to register all the item IDs automatically
	 * @return int[] designating all itemIds for a type of item.
	 */
	int[] getItemIds();
}
