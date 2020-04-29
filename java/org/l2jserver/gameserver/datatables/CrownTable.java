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
package org.l2jserver.gameserver.datatables;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has just one simple function to return the item id of a crown regarding to castleid
 * @author evill33t
 */
public class CrownTable
{
	private static List<Integer> _crownList = new ArrayList<>();
	
	public static List<Integer> getCrownList()
	{
		if (_crownList.isEmpty())
		{
			_crownList.add(6841); // Crown of the lord
			_crownList.add(6834); // Innadril
			_crownList.add(6835); // Dion
			_crownList.add(6836); // Goddard
			_crownList.add(6837); // Oren
			_crownList.add(6838); // Gludio
			_crownList.add(6839); // Giran
			_crownList.add(6840); // Aden
			_crownList.add(8182); // Rune
			_crownList.add(8183); // Schuttgart
		}
		return _crownList;
	}
	
	public static int getCrownId(int castleId)
	{
		int crownId = 0;
		switch (castleId)
		{
			// Gludio
			case 1:
			{
				crownId = 6838;
				break;
			}
			// Dion
			case 2:
			{
				crownId = 6835;
				break;
			}
			// Giran
			case 3:
			{
				crownId = 6839;
				break;
			}
			// Oren
			case 4:
			{
				crownId = 6837;
				break;
			}
			// Aden
			case 5:
			{
				crownId = 6840;
				break;
			}
			// Innadril
			case 6:
			{
				crownId = 6834;
				break;
			}
			// Goddard
			case 7:
			{
				crownId = 6836;
				break;
			}
			// Rune
			case 8:
			{
				crownId = 8182;
				break;
			}
			// Schuttgart
			case 9:
			{
				crownId = 8183;
				break;
			}
			default:
			{
				crownId = 0;
				break;
			}
		}
		return crownId;
	}
}
