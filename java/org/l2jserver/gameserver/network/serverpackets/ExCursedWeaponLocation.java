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

import java.util.List;

import org.l2jserver.gameserver.model.Location;

/**
 * Format: (ch) d[ddddd].
 * @author -Wooden-
 */
public class ExCursedWeaponLocation extends GameServerPacket
{
	private final List<CursedWeaponInfo> _cursedWeaponInfo;
	
	/**
	 * Instantiates a new ex cursed weapon location.
	 * @param cursedWeaponInfo the cursed weapon info
	 */
	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x46);
		
		if (!_cursedWeaponInfo.isEmpty())
		{
			writeD(_cursedWeaponInfo.size());
			for (CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w.id);
				writeD(w.activated);
				
				writeD(w.loc.getX());
				writeD(w.loc.getY());
				writeD(w.loc.getZ());
			}
		}
		else
		{
			writeD(0);
			writeD(0);
		}
	}
	
	/**
	 * The Class CursedWeaponInfo.
	 */
	public static class CursedWeaponInfo
	{
		/** The location. */
		public Location loc;
		
		/** The id. */
		public int id;
		
		/** The activated. */
		public int activated; // 0 - not activated ? 1 - activated
		
		/**
		 * Instantiates a new cursed weapon info.
		 * @param location the Location
		 * @param cwId the Id
		 * @param status the status
		 */
		public CursedWeaponInfo(Location location, int cwId, int status)
		{
			loc = location;
			id = cwId;
			activated = status;
		}
	}
}
