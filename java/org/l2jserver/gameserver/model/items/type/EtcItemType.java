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
package org.l2jserver.gameserver.model.items.type;

/**
 * Description of EtcItem Type
 */
public enum EtcItemType
{
	ARROW(0, "Arrow"),
	MATERIAL(1, "Material"),
	PET_COLLAR(2, "PetCollar"),
	POTION(3, "Potion"),
	RECEIPE(4, "Receipe"),
	SCROLL(5, "Scroll"),
	QUEST(6, "Quest"),
	MONEY(7, "Money"),
	OTHER(8, "Other"),
	SPELLBOOK(9, "Spellbook"),
	SEED(10, "Seed"),
	SHOT(11, "Shot"),
	HERB(12, "Herb");
	
	final int _id;
	final String _name;
	
	/**
	 * Constructor of the EtcItemType.
	 * @param id : int designating the ID of the EtcItemType
	 * @param name : String designating the name of the EtcItemType
	 */
	EtcItemType(int id, String name)
	{
		_id = id;
		_name = name;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	public int mask()
	{
		return 1 << (_id + 21);
	}
	
	/**
	 * Returns the name of the EtcItemType
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _name;
	}
}
