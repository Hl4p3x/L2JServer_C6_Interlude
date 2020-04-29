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
package org.l2jserver.gameserver.datatables.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jserver.gameserver.model.entity.ClanHall;

/**
 * @author Mobius
 */
public class DoorData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(DoorData.class.getName());
	
	private static final Map<Integer, DoorInstance> DOORS = new HashMap<>();
	
	@Override
	public void load()
	{
		parseDatapackFile("data/Doors.xml");
		LOGGER.info("DoorData: Loaded " + DOORS.size() + " doors.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			final StatSet set = new StatSet();
			final Node n = doc.getFirstChild();
			for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if (!"door".equalsIgnoreCase(node.getNodeName()))
				{
					continue;
				}
				
				final NamedNodeMap attrs = node.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					final Node attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				
				final DoorInstance door = createDoor(set);
				DOORS.put(door.getDoorId(), door);
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				final ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 500);
				if (clanhall != null)
				{
					clanhall.getDoors().add(door);
					door.setClanHall(clanhall);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("DoorData: Error while reading door data: " + e);
		}
	}
	
	public static DoorInstance createDoor(StatSet set)
	{
		final String name = set.getString("name");
		final int id = set.getInt("id");
		final int x = set.getInt("x");
		final int y = set.getInt("y");
		final int z = set.getInt("z");
		final int xMin = set.getInt("xMin");
		final int yMin = set.getInt("yMin");
		final int zMin = set.getInt("zMin");
		final int xMax = set.getInt("xMax");
		final int yMax = set.getInt("yMax");
		final int zMax = set.getInt("zMax");
		final int hp = set.getInt("hp");
		final int pDef = set.getInt("pDef");
		final int mDef = set.getInt("mDef");
		final boolean unlockable = set.getBoolean("unlockable", false);
		final boolean isOpen = set.getBoolean("isOpen", false);
		if (xMin > xMax)
		{
			LOGGER.warning("Error in door data, ID:" + id);
		}
		
		if (yMin > yMax)
		{
			LOGGER.warning("Error in door data, ID:" + id);
		}
		
		if (zMin > zMax)
		{
			LOGGER.warning("Error in door data, ID:" + id);
		}
		
		int collisionRadius; // (max) radius for movement checks
		if ((xMax - xMin) > (yMax - yMin))
		{
			collisionRadius = yMax - yMin;
		}
		else
		{
			collisionRadius = xMax - xMin;
		}
		
		final StatSet npcDat = new StatSet();
		npcDat.set("npcId", id);
		npcDat.set("level", 0);
		npcDat.set("jClass", "door");
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);
		
		// npcDat.set("name", "");
		npcDat.set("collision_radius", collisionRadius);
		npcDat.set("collision_height", zMax - zMin);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("name", name);
		npcDat.set("baseHpMax", hp);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", pDef);
		npcDat.set("baseMDef", mDef);
		
		final CreatureTemplate template = new CreatureTemplate(npcDat);
		final DoorInstance door = new DoorInstance(IdFactory.getNextId(), template, id, name, unlockable);
		door.setRange(xMin, yMin, zMin, xMax, yMax, zMax);
		try
		{
			door.setMapRegion(MapRegionData.getInstance().getMapRegion(x, y));
		}
		catch (Exception e)
		{
			LOGGER.warning("Error in door data, ID:" + id + " " + e);
		}
		door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
		door.setOpen(isOpen);
		door.setXYZInvisible(x, y, z);
		return door;
	}
	
	public DoorInstance getDoor(Integer id)
	{
		return DOORS.get(id);
	}
	
	public void putDoor(DoorInstance door)
	{
		DOORS.put(door.getDoorId(), door);
	}
	
	public Collection<DoorInstance> getDoors()
	{
		return DOORS.values();
	}
	
	/**
	 * Performs a check and sets up a scheduled task for those doors that require auto opening/closing.
	 */
	public void checkAutoOpen()
	{
		for (DoorInstance doorInst : DOORS.values())
		{
			// Garden of Eva (every 7 minutes)
			if (doorInst.getDoorName().startsWith("goe"))
			{
				doorInst.setAutoActionDelay(420000);
			}
			// Tower of Insolence (every 5 minutes)
			else if (doorInst.getDoorName().startsWith("aden_tower"))
			{
				doorInst.setAutoActionDelay(300000);
			}
			// Cruma Tower (every 20 minutes)
			else if (doorInst.getDoorName().startsWith("cruma"))
			{
				doorInst.setAutoActionDelay(1200000);
			}
		}
	}
	
	public boolean checkIfDoorsBetween(Location start, Location end)
	{
		return checkIfDoorsBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
	}
	
	public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz)
	{
		int region;
		try
		{
			region = MapRegionData.getInstance().getMapRegion(x, y);
		}
		catch (Exception e)
		{
			return false;
		}
		
		for (DoorInstance doorInst : DOORS.values())
		{
			if (doorInst.getMapRegion() != region)
			{
				continue;
			}
			if (doorInst.getXMax() == 0)
			{
				continue;
			}
			
			// line segment goes through box
			// heavy approximation disabling some shooting angles especially near 2-piece doors
			// but most calculations should stop short
			// phase 1, x
			if (((x <= doorInst.getXMax()) && (tx >= doorInst.getXMin())) || ((tx <= doorInst.getXMax()) && (x >= doorInst.getXMin())))
			{
				// phase 2, y
				if (((y <= doorInst.getYMax()) && (ty >= doorInst.getYMin())) || ((ty <= doorInst.getYMax()) && (y >= doorInst.getYMin())))
				{
					// phase 3, basically only z remains but now we calculate it with another formula (by rage)
					// in some cases the direct line check (only) in the beginning isn't sufficient,
					// when char z changes a lot along the path
					if ((doorInst.getStatus().getCurrentHp() > 0) && !doorInst.isOpen())
					{
						final int px1 = doorInst.getXMin();
						final int py1 = doorInst.getYMin();
						final int pz1 = doorInst.getZMin();
						final int px2 = doorInst.getXMax();
						final int py2 = doorInst.getYMax();
						final int pz2 = doorInst.getZMax();
						final int l = tx - x;
						final int m = ty - y;
						final int n = tz - z;
						final int dk = ((doorInst.getA() * l) + (doorInst.getB() * m) + (doorInst.getC() * n));
						if (dk == 0)
						{
							continue; // Parallel
						}
						
						final float p = (float) ((doorInst.getA() * x) + (doorInst.getB() * y) + (doorInst.getC() * z) + doorInst.getD()) / dk;
						final int fx = (int) (x - (l * p));
						final int fy = (int) (y - (m * p));
						final int fz = (int) (z - (n * p));
						if (((Math.min(x, tx) <= fx) && (fx <= Math.max(x, tx))) && ((Math.min(y, ty) <= fy) && (fy <= Math.max(y, ty))) && ((Math.min(z, tz) <= fz) && (fz <= Math.max(z, tz))))
						{
							if ((((fx >= px1) && (fx <= px2)) || ((fx >= px2) && (fx <= px1))) && (((fy >= py1) && (fy <= py2)) || ((fy >= py2) && (fy <= py1))) && (((fz >= pz1) && (fz <= pz2)) || ((fz >= pz2) && (fz <= pz1))))
							{
								return true; // Door between
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public static DoorData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DoorData INSTANCE = new DoorData();
	}
}
