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
package org.l2jserver.gameserver.model.holders;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.gameserver.model.actor.instance.BoatInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.VehicleDeparture;

/**
 * @author Maktakien
 */
public class BoatPathHolder
{
	protected static final Logger LOGGER = Logger.getLogger(BoatPathHolder.class.getName());
	
	private final List<BoatPoint> _path;
	private final int _max;
	public int ticketId;
	public int ntx;
	public int nty;
	public int ntz;
	public String npc;
	public String sysmess10;
	public String sysmess5;
	public String sysmess1;
	public String sysmessb;
	public String sysmess0;
	
	public static class BoatPoint
	{
		public int speed1;
		public int speed2;
		public int x;
		public int y;
		public int z;
		public int time;
	}
	
	public BoatPathHolder(int pathId, int pTicketId, int pNtx, int pNty, int pNtz, String pNpc, String pSysmess10, String pSysmess5, String pSysmess1, String pSysmess0, String pSysmessb, List<BoatPoint> path)
	{
		ticketId = pTicketId;
		ntx = pNtx;
		nty = pNty;
		ntz = pNtz;
		npc = pNpc;
		sysmess10 = pSysmess10;
		sysmess5 = pSysmess5;
		sysmess1 = pSysmess1;
		sysmessb = pSysmessb;
		sysmess0 = pSysmess0;
		_path = path;
		_max = _path.size();
	}
	
	public int state(int state, BoatInstance boat)
	{
		if (state < _max)
		{
			final BoatPoint path = _path.get(state);
			final double dx = boat.getX() - path.x;
			final double dy = boat.getY() - path.y;
			final double distance = Math.sqrt((dx * dx) + (dy * dy));
			final double cos = dx / distance;
			final double sin = dy / distance;
			boat.getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
			boat.vd = new VehicleDeparture(boat, path.speed1, path.speed2, path.x, path.y, path.z);
			boat.boatSpeed = path.speed1;
			boat.moveToLocation(path.x, path.y, path.z, path.speed1);
			final Collection<PlayerInstance> knownPlayers = boat.getKnownList().getKnownPlayers().values();
			if ((knownPlayers == null) || knownPlayers.isEmpty())
			{
				return path.time;
			}
			for (PlayerInstance player : knownPlayers)
			{
				player.sendPacket(boat.vd);
			}
			if (path.time == 0)
			{
				path.time = 1;
			}
			return path.time;
		}
		return 0;
	}
}
