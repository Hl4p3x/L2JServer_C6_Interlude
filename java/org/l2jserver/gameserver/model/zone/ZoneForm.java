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
package org.l2jserver.gameserver.model.zone;

import java.awt.geom.Line2D;

import org.l2jserver.gameserver.datatables.xml.ZoneData;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

/**
 * Abstract base class for any zone form
 * @author durgus
 */
public abstract class ZoneForm
{
	protected static final int STEP = 50;
	
	public abstract boolean isInsideZone(int x, int y, int z);
	
	public abstract boolean intersectsRectangle(int x1, int x2, int y1, int y2);
	
	public abstract double getDistanceToZone(int x, int y);
	
	public abstract int getLowZ(); // Support for the ability to extract the z coordinates of zones.
	
	public abstract int getHighZ(); // New fishing patch makes use of that to get the Z for the hook
	
	public abstract void visualizeZone(int id, int z);
	
	// landing coordinates.
	
	protected boolean lineSegmentsIntersect(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2)
	{
		return Line2D.linesIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2);
	}
	
	protected boolean lineIntersectsLine(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2)
	{
		final int s1 = sameSide(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2);
		final int s2 = sameSide(bx1, by1, bx2, by2, ax1, ay1, ax2, ay1);
		return (s1 <= 0) && (s2 <= 0);
	}
	
	protected int sameSide(double x0, double y0, double x1, double y1, double px0, double py0, double px1, double py1)
	{
		int sameSide = 0;
		
		final double dx = x1 - x0;
		final double dy = y1 - y0;
		final double dx1 = px0 - x0;
		final double dy1 = py0 - y0;
		final double dx2 = px1 - x1;
		final double dy2 = py1 - y1;
		
		// Cross product of the vector from the endpoint of the line to the point
		final double c1 = (dx * dy1) - (dy * dx1);
		final double c2 = (dx * dy2) - (dy * dx2);
		if ((c1 != 0) && (c2 != 0))
		{
			sameSide = (c1 < 0) != (c2 < 0) ? -1 : 1;
		}
		else if ((dx == 0) && (dx1 == 0) && (dx2 == 0))
		{
			sameSide = !isBetween(y0, y1, py0) && !isBetween(y0, y1, py1) ? 1 : 0;
		}
		else if ((dy == 0) && (dy1 == 0) && (dy2 == 0))
		{
			sameSide = !isBetween(x0, x1, px0) && !isBetween(x0, x1, px1) ? 1 : 0;
		}
		return sameSide;
	}
	
	protected boolean isBetween(double a, double b, double c)
	{
		return b > a ? (c >= a) && (c <= b) : (c >= b) && (c <= a);
	}
	
	protected static final void dropDebugItem(int id, int x, int y, int z)
	{
		final ItemInstance item = new ItemInstance(IdFactory.getNextId(), 57);
		item.setCount(id);
		item.spawnMe(x, y, z + 5);
		ZoneData.getInstance().addDebugItem(item);
	}
}
