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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.gameserver.instancemanager.ArenaManager;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.instancemanager.OlympiadStadiaManager;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.WorldRegion;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.zone.ZoneRespawn;
import org.l2jserver.gameserver.model.zone.ZoneType;
import org.l2jserver.gameserver.model.zone.form.ZoneCuboid;
import org.l2jserver.gameserver.model.zone.form.ZoneCylinder;
import org.l2jserver.gameserver.model.zone.form.ZoneNPoly;
import org.l2jserver.gameserver.model.zone.type.ArenaZone;
import org.l2jserver.gameserver.model.zone.type.BigheadZone;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.model.zone.type.CastleTeleportZone;
import org.l2jserver.gameserver.model.zone.type.CastleZone;
import org.l2jserver.gameserver.model.zone.type.ClanHallZone;
import org.l2jserver.gameserver.model.zone.type.DamageZone;
import org.l2jserver.gameserver.model.zone.type.DerbyTrackZone;
import org.l2jserver.gameserver.model.zone.type.EffectZone;
import org.l2jserver.gameserver.model.zone.type.FishingZone;
import org.l2jserver.gameserver.model.zone.type.FortZone;
import org.l2jserver.gameserver.model.zone.type.HqZone;
import org.l2jserver.gameserver.model.zone.type.JailZone;
import org.l2jserver.gameserver.model.zone.type.MotherTreeZone;
import org.l2jserver.gameserver.model.zone.type.NoLandingZone;
import org.l2jserver.gameserver.model.zone.type.NoRestartZone;
import org.l2jserver.gameserver.model.zone.type.NoStoreZone;
import org.l2jserver.gameserver.model.zone.type.NoSummonFriendZone;
import org.l2jserver.gameserver.model.zone.type.OlympiadStadiumZone;
import org.l2jserver.gameserver.model.zone.type.PeaceZone;
import org.l2jserver.gameserver.model.zone.type.PoisonZone;
import org.l2jserver.gameserver.model.zone.type.ScriptZone;
import org.l2jserver.gameserver.model.zone.type.SwampZone;
import org.l2jserver.gameserver.model.zone.type.TownZone;
import org.l2jserver.gameserver.model.zone.type.WaterZone;

public class ZoneData
{
	private static final Logger LOGGER = Logger.getLogger(ZoneData.class.getName());
	
	private final Map<Class<? extends ZoneType>, Map<Integer, ? extends ZoneType>> _classZones = new HashMap<>();
	private final Map<Integer, ItemInstance> _debugItems = new ConcurrentHashMap<>();
	
	private int _lastDynamicId = 300000;
	
	protected ZoneData()
	{
		LOGGER.info(getClass().getSimpleName() + ": Loading zones...");
		load();
	}
	
	public void reload()
	{
		// remove zones from world
		int count = 0;
		for (WorldRegion[] worldRegion : World.getInstance().getAllWorldRegions())
		{
			for (WorldRegion region : worldRegion)
			{
				for (ZoneType zone : region.getZones())
				{
					region.removeZone(zone);
					count++;
				}
			}
		}
		
		LOGGER.info(getClass().getSimpleName() + ": Removed zones in " + count + " regions.");
		
		// clear
		_classZones.clear();
		clearDebugItems();
		
		// load all zones
		load();
		
		// revalidate objects in zones
		for (WorldObject o : World.getInstance().getAllVisibleObjects())
		{
			if (o instanceof Creature)
			{
				((Creature) o).revalidateZone(true);
			}
		}
	}
	
	private final void load()
	{
		// Get the world regions
		WorldRegion[][] worldRegions = World.getInstance().getAllWorldRegions();
		
		// Load the zone xml
		try
		{
			final File mainDir = new File("data/zones");
			if (!mainDir.isDirectory())
			{
				LOGGER.warning(getClass().getSimpleName() + ": Main directory " + mainDir.getAbsolutePath() + " hasn't been found.");
				return;
			}
			
			for (final File file : mainDir.listFiles())
			{
				if (file.isFile() && file.getName().endsWith(".xml"))
				{
					loadFileZone(file, worldRegions);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Error while loading zones.", e);
			return;
		}
		
		// get size
		int size = 0;
		for (Map<Integer, ? extends ZoneType> map : _classZones.values())
		{
			size += map.size();
		}
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _classZones.size() + " zones classes and total " + size + " zones.");
	}
	
	private void loadFileZone(final File f, WorldRegion[][] worldRegions) throws Exception
	{
		Document doc = null;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		try
		{
			doc = factory.newDocumentBuilder().parse(f);
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not parse zone file: " + e);
			return;
		}
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				NamedNodeMap attrs = n.getAttributes();
				Node attribute = attrs.getNamedItem("enabled");
				if ((attribute != null) && !Boolean.parseBoolean(attribute.getNodeValue()))
				{
					continue;
				}
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("zone".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap nnmd = d.getAttributes();
						
						// Generate dynamically zone's ID.
						int zoneId = _lastDynamicId++;
						
						// Dynamic id is replaced by handwritten id if existing.
						attribute = nnmd.getNamedItem("id");
						if (attribute != null)
						{
							zoneId = Integer.parseInt(attribute.getNodeValue());
						}
						
						final String zoneType = nnmd.getNamedItem("type").getNodeValue();
						final String zoneShape = nnmd.getNamedItem("shape").getNodeValue();
						final int minZ = Integer.parseInt(nnmd.getNamedItem("minZ").getNodeValue());
						final int maxZ = Integer.parseInt(nnmd.getNamedItem("maxZ").getNodeValue());
						
						// Create the zone
						ZoneType temp = null;
						
						switch (zoneType)
						{
							case "FishingZone":
							{
								temp = new FishingZone(zoneId);
								break;
							}
							case "ClanHallZone":
							{
								temp = new ClanHallZone(zoneId);
								break;
							}
							case "PeaceZone":
							{
								temp = new PeaceZone(zoneId);
								break;
							}
							case "TownZone":
							{
								temp = new TownZone(zoneId);
								break;
							}
							case "OlympiadStadiumZone":
							{
								temp = new OlympiadStadiumZone(zoneId);
								break;
							}
							case "CastleZone":
							{
								temp = new CastleZone(zoneId);
								break;
							}
							case "FortZone":
							{
								temp = new FortZone(zoneId);
								break;
							}
							case "DamageZone":
							{
								temp = new DamageZone(zoneId);
								break;
							}
							case "ArenaZone":
							{
								temp = new ArenaZone(zoneId);
								break;
							}
							case "MotherTreeZone":
							{
								temp = new MotherTreeZone(zoneId);
								break;
							}
							case "BigheadZone":
							{
								temp = new BigheadZone(zoneId);
								break;
							}
							case "NoLandingZone":
							{
								temp = new NoLandingZone(zoneId);
								break;
							}
							case "NoRestartZone":
							{
								temp = new NoRestartZone(zoneId);
								break;
							}
							case "NoStoreZone":
							{
								temp = new NoStoreZone(zoneId);
								break;
							}
							case "NoSummonFriendZone":
							{
								temp = new NoSummonFriendZone(zoneId);
								break;
							}
							case "JailZone":
							{
								temp = new JailZone(zoneId);
								break;
							}
							case "DerbyTrackZone":
							{
								temp = new DerbyTrackZone(zoneId);
								break;
							}
							case "WaterZone":
							{
								temp = new WaterZone(zoneId);
								break;
							}
							case "HqZone":
							{
								temp = new HqZone(zoneId);
								break;
							}
							case "BossZone":
							{
								temp = new BossZone(zoneId, Integer.parseInt(nnmd.getNamedItem("bossId").getNodeValue()));
								break;
							}
							case "EffectZone":
							{
								temp = new EffectZone(zoneId);
								break;
							}
							case "PoisonZone":
							{
								temp = new PoisonZone(zoneId);
								break;
							}
							case "ScriptZone":
							{
								temp = new ScriptZone(zoneId);
								break;
							}
							case "CastleTeleportZone":
							{
								temp = new CastleTeleportZone(zoneId);
								break;
							}
							case "SwampZone":
							{
								temp = new SwampZone(zoneId);
								break;
							}
						}
						
						// Check for unknown type
						if (temp == null)
						{
							LOGGER.warning("ZoneData: No such zone type: " + zoneType);
							continue;
						}
						
						// Get the zone shape from file if any
						try
						{
							List<int[]> rs = new ArrayList<>();
							
							// loading from XML first
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("node".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									int[] point = new int[2];
									point[0] = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
									point[1] = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
									rs.add(point);
								}
							}
							
							int[][] coords = rs.toArray(new int[rs.size()][]);
							if ((coords == null) || (coords.length == 0))
							{
								LOGGER.warning(getClass().getSimpleName() + ": missing data for zone: " + zoneId + " on file: " + f.getName());
								continue;
							}
							
							// Create this zone. Parsing for cuboids is a bit different than for other polygons cuboids need exactly 2 points to be defined.
							// Other polygons need at least 3 (one per vertex)
							if (zoneShape.equalsIgnoreCase("Cuboid"))
							{
								if (coords.length == 2)
								{
									temp.setZone(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
								}
								else
								{
									LOGGER.warning(getClass().getSimpleName() + ": Missing cuboid vertex in data for zone: " + zoneId + " in file: " + f.getName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("NPoly"))
							{
								// nPoly needs to have at least 3 vertices
								if (coords.length > 2)
								{
									final int[] aX = new int[coords.length];
									final int[] aY = new int[coords.length];
									for (int i = 0; i < coords.length; i++)
									{
										aX[i] = coords[i][0];
										aY[i] = coords[i][1];
									}
									temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
								}
								else
								{
									LOGGER.warning(getClass().getSimpleName() + ": Bad data for zone: " + zoneId + " in file: " + f.getName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("Cylinder"))
							{
								// A Cylinder zone requires a center point at x,y and a radius
								attrs = d.getAttributes();
								final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
								if ((coords.length == 1) && (zoneRad > 0))
								{
									temp.setZone(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
								}
								else
								{
									LOGGER.warning(getClass().getSimpleName() + ": Bad data for zone: " + zoneId + " in file: " + f.getName());
									continue;
								}
							}
							else
							{
								LOGGER.warning(getClass().getSimpleName() + ": Unknown shape: " + zoneShape + " in file: " + f.getName());
								continue;
							}
						}
						catch (Exception e)
						{
							LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Failed to load zone " + zoneId + " coordinates: " + e.getMessage(), e);
						}
						
						// Check for additional parameters
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("stat".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								String name = attrs.getNamedItem("name").getNodeValue();
								String val = attrs.getNamedItem("val").getNodeValue();
								temp.setParameter(name, val);
							}
							else if ("spawn".equalsIgnoreCase(cd.getNodeName()) && (temp instanceof ZoneRespawn))
							{
								attrs = cd.getAttributes();
								int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
								int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
								int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
								Node val = attrs.getNamedItem("isChaotic");
								if ((val != null) && Boolean.parseBoolean(val.getNodeValue()))
								{
									((ZoneRespawn) temp).addChaoticSpawn(spawnX, spawnY, spawnZ);
								}
								else
								{
									((ZoneRespawn) temp).addSpawn(spawnX, spawnY, spawnZ);
								}
							}
						}
						
						addZone(zoneId, temp);
						
						// Register the zone into any world region it intersects with...
						for (int x = 0; x < worldRegions.length; x++)
						{
							for (int y = 0; y < worldRegions[x].length; y++)
							{
								if (temp.getZone().intersectsRectangle((x - World.OFFSET_X) << World.SHIFT_BY, ((x + 1) - World.OFFSET_X) << World.SHIFT_BY, (y - World.OFFSET_Y) << World.SHIFT_BY, ((y + 1) - World.OFFSET_Y) << World.SHIFT_BY))
								{
									worldRegions[x][y].addZone(temp);
								}
							}
						}
						
						// Special managers for arenas, towns...
						if (temp instanceof ArenaZone)
						{
							ArenaManager.getInstance().addArena((ArenaZone) temp);
						}
						else if (temp instanceof OlympiadStadiumZone)
						{
							OlympiadStadiaManager.getInstance().addStadium((OlympiadStadiumZone) temp);
						}
						else if (temp instanceof BossZone)
						{
							GrandBossManager.getInstance().addZone((BossZone) temp);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Add new zone
	 * @param id
	 * @param <T>
	 * @param zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> void addZone(Integer id, T zone)
	{
		// _zones.put(id, zone);
		Map<Integer, T> map = (Map<Integer, T>) _classZones.get(zone.getClass());
		if (map == null)
		{
			map = new HashMap<>();
			map.put(id, zone);
			_classZones.put(zone.getClass(), map);
		}
		else
		{
			map.put(id, zone);
		}
	}
	
	/**
	 * Return all zones by class type
	 * @param <T>
	 * @param zoneType Zone class
	 * @return Collection of zones
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> Collection<T> getAllZones(Class<T> zoneType)
	{
		return (Collection<T>) _classZones.get(zoneType).values();
	}
	
	/**
	 * Get zone by ID
	 * @param id
	 * @return
	 * @see #getZoneById(int, Class)
	 */
	public ZoneType getZoneById(int id)
	{
		for (Map<Integer, ? extends ZoneType> map : _classZones.values())
		{
			if (map.containsKey(id))
			{
				return map.get(id);
			}
		}
		return null;
	}
	
	/**
	 * Get zone by ID and zone class
	 * @param <T>
	 * @param id
	 * @param zoneType
	 * @return zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZoneById(int id, Class<T> zoneType)
	{
		return (T) _classZones.get(zoneType).get(id);
	}
	
	/**
	 * Returns all zones from where the object is located
	 * @param object
	 * @return zones
	 */
	public List<ZoneType> getZones(WorldObject object)
	{
		return getZones(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Returns zone from where the object is located by type
	 * @param <T>
	 * @param object
	 * @param type
	 * @return zone
	 */
	public <T extends ZoneType> T getZone(WorldObject object, Class<T> type)
	{
		if (object == null)
		{
			return null;
		}
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	/**
	 * Returns all zones from given coordinates
	 * @param x
	 * @param y
	 * @param z
	 * @return zones
	 */
	public List<ZoneType> getZones(int x, int y, int z)
	{
		final List<ZoneType> temp = new ArrayList<>();
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z))
			{
				temp.add(zone);
			}
		}
		return temp;
	}
	
	/**
	 * Returns zone from given coordinates
	 * @param <T>
	 * @param x
	 * @param y
	 * @param z
	 * @param type
	 * @return zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZone(int x, int y, int z, Class<T> type)
	{
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
			{
				return (T) zone;
			}
		}
		return null;
	}
	
	/**
	 * Add an item on debug list. Used to visualize zones.
	 * @param item : The item to add.
	 */
	public void addDebugItem(ItemInstance item)
	{
		_debugItems.put(item.getObjectId(), item);
	}
	
	/**
	 * Remove all debug items from the world.
	 */
	public void clearDebugItems()
	{
		for (ItemInstance item : _debugItems.values())
		{
			item.decayMe();
		}
		
		_debugItems.clear();
	}
	
	public static final ZoneData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneData INSTANCE = new ZoneData();
	}
}