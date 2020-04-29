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

import java.awt.Polygon;
import java.util.logging.Logger;

import org.l2jserver.commons.util.Rnd;

public class Territory
{
	private static final Logger LOGGER = Logger.getLogger(Territory.class.getName());
	
	protected class Point
	{
		protected int _x;
		protected int _y;
		protected int _zmin;
		protected int _zmax;
		protected int _proc;
		
		Point(int x, int y, int zmin, int zmax, int proc)
		{
			_x = x;
			_y = y;
			_zmin = zmin;
			_zmax = zmax;
			_proc = proc;
		}
	}
	
	private Point[] _points;
	// private String _terr;
	private int _xMin;
	private int _xMax;
	private int _yMin;
	private int _yMax;
	private int _zMin;
	private int _zMax;
	private int _procMax;
	private final Polygon poly;
	
	public Territory(/* String string */)
	{
		poly = new Polygon();
		_points = new Point[0];
		// _terr = string;
		_xMin = 999999;
		_xMax = -999999;
		_yMin = 999999;
		_yMax = -999999;
		_zMin = 999999;
		_zMax = -999999;
		_procMax = 0;
	}
	
	public void add(int x, int y, int zmin, int zmax, int proc)
	{
		final Point[] newPoints = new Point[_points.length + 1];
		System.arraycopy(_points, 0, newPoints, 0, _points.length);
		newPoints[_points.length] = new Point(x, y, zmin, zmax, proc);
		_points = newPoints;
		poly.addPoint(x, y);
		if (x < _xMin)
		{
			_xMin = x;
		}
		
		if (y < _yMin)
		{
			_yMin = y;
		}
		
		if (x > _xMax)
		{
			_xMax = x;
		}
		
		if (y > _yMax)
		{
			_yMax = y;
		}
		
		if (zmin < _zMin)
		{
			_zMin = zmin;
		}
		
		if (zmax > _zMax)
		{
			_zMax = zmax;
		}
		
		_procMax += proc;
	}
	
	public void print()
	{
		for (Point p : _points)
		{
			LOGGER.info("(" + p._x + "," + p._y + ")");
		}
	}
	
	public boolean isIntersect(int x, int y, Point p1, Point p2)
	{
		final double dy1 = p1._y - y;
		final double dy2 = p2._y - y;
		if (Math.signum(dy1) == Math.signum(dy2))
		{
			return false;
		}
		
		final double dx1 = p1._x - x;
		final double dx2 = p2._x - x;
		if ((dx1 >= 0) && (dx2 >= 0))
		{
			return true;
		}
		
		if ((dx1 < 0) && (dx2 < 0))
		{
			return false;
		}
		
		final double dx0 = (dy1 * (p1._x - p2._x)) / (p1._y - p2._y);
		return dx0 <= dx1;
	}
	
	public boolean isInside(int x, int y)
	{
		return poly.contains(x, y);
	}
	
	public int[] getRandomPoint()
	{
		int i;
		final int[] p = new int[4];
		if (_procMax > 0)
		{
			int pos = 0;
			final int rnd = Rnd.get(_procMax);
			for (i = 0; i < _points.length; i++)
			{
				final Point p1 = _points[i];
				pos += p1._proc;
				if (rnd <= pos)
				{
					p[0] = p1._x;
					p[1] = p1._y;
					p[2] = p1._zmin;
					p[3] = p1._zmax;
					return p;
				}
			}
		}
		for (i = 0; i < 100; i++)
		{
			p[0] = Rnd.get(_xMin, _xMax);
			p[1] = Rnd.get(_yMin, _yMax);
			if (isInside(p[0], p[1]))
			{
				double curdistance = 0;
				p[2] = _zMin + 100;
				p[3] = _zMax;
				for (i = 0; i < _points.length; i++)
				{
					final Point p1 = _points[i];
					final double dx = p1._x - p[0];
					final double dy = p1._y - p[1];
					final double distance = Math.sqrt((dx * dx) + (dy * dy));
					if ((curdistance == 0) || (distance < curdistance))
					{
						curdistance = distance;
						p[2] = p1._zmin + 100;
					}
				}
				return p;
			}
		}
		LOGGER.warning("Can't make point for " + this);
		return p;
	}
	
	public int getProcMax()
	{
		return _procMax;
	}
	
	public int getYmin()
	{
		return _yMin;
	}
	
	public int getXmax()
	{
		return _xMax;
	}
	
	public int getXmin()
	{
		return _xMin;
	}
	
	public int getYmax()
	{
		return _yMax;
	}
	
	public int getZmin()
	{
		return _zMin;
	}
	
	public int getZmax()
	{
		return _zMax;
	}
}
