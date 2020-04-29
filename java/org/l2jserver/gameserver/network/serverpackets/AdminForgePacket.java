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
package org.l2jserver.gameserver.network.serverpackets;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is made to create packets with any format
 * @author Maktakien
 */
public class AdminForgePacket extends GameServerPacket
{
	private final List<Part> _parts = new ArrayList<>();
	
	private class Part
	{
		public byte b;
		public String str;
		
		public Part(byte bb, String string)
		{
			b = bb;
			str = string;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		for (Part p : _parts)
		{
			generate(p.b, p.str);
		}
	}
	
	/**
	 * @param b
	 * @param string
	 * @return
	 */
	public boolean generate(byte b, String string)
	{
		if ((b == 'C') || (b == 'c'))
		{
			writeC(Integer.decode(string));
			return true;
		}
		else if ((b == 'D') || (b == 'd'))
		{
			writeD(Integer.decode(string));
			return true;
		}
		else if ((b == 'H') || (b == 'h'))
		{
			writeH(Integer.decode(string));
			return true;
		}
		else if ((b == 'F') || (b == 'f'))
		{
			writeF(Double.parseDouble(string));
			return true;
		}
		else if ((b == 'S') || (b == 's'))
		{
			writeS(string);
			return true;
		}
		else if ((b == 'B') || (b == 'b') || (b == 'X') || (b == 'x'))
		{
			writeB(new BigInteger(string).toByteArray());
			return true;
		}
		return false;
	}
	
	public void addPart(byte b, String string)
	{
		_parts.add(new Part(b, string));
	}
}
