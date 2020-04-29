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
package org.l2jserver.gameserver.model.items;

import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.items.type.EtcItemType;

/**
 * This class is dedicated to the management of EtcItem.
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:30:10 $
 */
public class EtcItem extends Item
{
	/**
	 * Constructor for EtcItem.
	 * @see Item constructor
	 * @param type : EtcItemType designating the type of object Etc
	 * @param set : StatSet designating the set of couples (key,value) for description of the Etc
	 */
	public EtcItem(EtcItemType type, StatSet set)
	{
		super(type, set);
	}
	
	/**
	 * Returns the type of Etc Item
	 * @return EtcItemType
	 */
	@Override
	public EtcItemType getItemType()
	{
		return (EtcItemType) super._type;
	}
	
	/**
	 * Returns if the item is consumable
	 * @return boolean
	 */
	@Override
	public boolean isConsumable()
	{
		return (getItemType() == EtcItemType.SHOT) || (getItemType() == EtcItemType.POTION); // || (type == EtcItemType.SCROLL));
	}
	
	/**
	 * Returns the ID of the Etc item after applying the mask.
	 * @return int : ID of the EtcItem
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
}
