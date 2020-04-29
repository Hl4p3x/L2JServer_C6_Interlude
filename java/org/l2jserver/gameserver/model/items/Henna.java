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

import org.l2jserver.commons.util.Util;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * A datatype used to retain Henna infos. Hennas are called "dye" ingame, and enhance {@link PlayerInstance} stats for a fee.<br>
 * You can draw up to 3 hennas (depending about your current class rank), but accumulated boni for a stat can't be higher than +5. There is no limit in reduction.
 */
public class Henna
{
	private final int _symbolId;
	private final int _dyeId;
	private final int _price;
	private final int _INT;
	private final int _STR;
	private final int _CON;
	private final int _MEN;
	private final int _DEX;
	private final int _WIT;
	private final int[] _classes;
	
	public Henna(StatSet set)
	{
		_symbolId = set.getInt("symbolId");
		_dyeId = set.getInt("dyeId");
		_price = set.getInt("price");
		_INT = set.getInt("INT");
		_STR = set.getInt("STR");
		_CON = set.getInt("CON");
		_MEN = set.getInt("MEN");
		_DEX = set.getInt("DEX");
		_WIT = set.getInt("WIT");
		_classes = set.getIntArray("classes", ";");
	}
	
	public int getSymbolId()
	{
		return _symbolId;
	}
	
	public int getDyeId()
	{
		return _dyeId;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public static final int getRequiredDyeAmount()
	{
		return 10;
	}
	
	public int getINT()
	{
		return _INT;
	}
	
	public int getSTR()
	{
		return _STR;
	}
	
	public int getCON()
	{
		return _CON;
	}
	
	public int getMEN()
	{
		return _MEN;
	}
	
	public int getDEX()
	{
		return _DEX;
	}
	
	public int getWIT()
	{
		return _WIT;
	}
	
	/**
	 * Seek if this {@link Henna} can be used by a {@link PlayerInstance}, based on his classId.
	 * @param player : The Player to check.
	 * @return true if this Henna owns the Player classId.
	 */
	public boolean canBeUsedBy(PlayerInstance player)
	{
		return Util.contains(_classes, player.getClassId().getId());
	}
}