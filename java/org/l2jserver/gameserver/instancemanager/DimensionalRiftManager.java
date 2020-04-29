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
package org.l2jserver.gameserver.instancemanager;

import java.awt.Polygon;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.DimensionalRift;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.util.Util;

/**
 * Thanks to Fortress and balancer.ru - kombat
 */
public class DimensionalRiftManager
{
	protected static final Logger LOGGER = Logger.getLogger(DimensionalRiftManager.class.getName());
	
	private final Map<Byte, Map<Byte, DimensionalRiftRoom>> _rooms = new HashMap<>();
	private static final short DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;
	private static final int MAX_PARTY_PER_AREA = 3;
	
	private DimensionalRiftManager()
	{
		loadRooms();
		loadSpawns();
	}
	
	public DimensionalRiftRoom getRoom(byte type, byte room)
	{
		return _rooms.get(type) == null ? null : _rooms.get(type).get(room);
	}
	
	public boolean isAreaAvailable(byte area)
	{
		final Map<Byte, DimensionalRiftRoom> tmap = _rooms.get(area);
		if (tmap == null)
		{
			return false;
		}
		int used = 0;
		for (DimensionalRiftRoom room : tmap.values())
		{
			if (room.isUsed())
			{
				used++;
			}
		}
		return used <= MAX_PARTY_PER_AREA;
	}
	
	public boolean isRoomAvailable(byte area, byte room)
	{
		if ((_rooms.get(area) == null) || (_rooms.get(area).get(room) == null))
		{
			return false;
		}
		return !_rooms.get(area).get(room).isUsed();
	}
	
	private void loadRooms()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement s = con.prepareStatement("SELECT * FROM dimensional_rift");
			final ResultSet rs = s.executeQuery();
			
			while (rs.next())
			{
				// 0 waiting room, 1 recruit, 2 soldier, 3 officer, 4 captain , 5 commander, 6 hero
				final byte type = rs.getByte("type");
				final byte room_id = rs.getByte("room_id");
				
				// coords related
				final int xMin = rs.getInt("xMin");
				final int xMax = rs.getInt("xMax");
				final int yMin = rs.getInt("yMin");
				final int yMax = rs.getInt("yMax");
				final int z1 = rs.getInt("zMin");
				final int z2 = rs.getInt("zMax");
				final int xT = rs.getInt("xT");
				final int yT = rs.getInt("yT");
				final int zT = rs.getInt("zT");
				final boolean isBossRoom = rs.getByte("boss") > 0;
				if (!_rooms.containsKey(type))
				{
					_rooms.put(type, new HashMap<Byte, DimensionalRiftRoom>());
				}
				
				_rooms.get(type).put(room_id, new DimensionalRiftRoom(type, room_id, xMin, xMax, yMin, yMax, z1, z2, xT, yT, zT, isBossRoom));
			}
			
			s.close();
			rs.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Can't load Dimension Rift zones. " + e);
		}
		
		final int typeSize = _rooms.keySet().size();
		int roomSize = 0;
		for (Map<Byte, DimensionalRiftRoom> room : _rooms.values())
		{
			roomSize += room.keySet().size();
		}
		
		LOGGER.info("DimensionalRiftManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
	}
	
	public void loadSpawns()
	{
		int total = 0;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			final File file = new File(Config.DATAPACK_ROOT + "/data/DimensionalRift.xml");
			if (!file.exists())
			{
				throw new IOException();
			}
			
			final Document doc = factory.newDocumentBuilder().parse(file);
			NamedNodeMap attrs;
			byte type;
			byte roomId;
			int mobId;
			int x;
			int y;
			int z;
			int delay;
			int count;
			Spawn spawnDat;
			NpcTemplate template;
			for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
			{
				if ("rift".equalsIgnoreCase(rift.getNodeName()))
				{
					for (Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
					{
						if ("area".equalsIgnoreCase(area.getNodeName()))
						{
							attrs = area.getAttributes();
							type = Byte.parseByte(attrs.getNamedItem("type").getNodeValue());
							for (Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
							{
								if ("room".equalsIgnoreCase(room.getNodeName()))
								{
									attrs = room.getAttributes();
									roomId = Byte.parseByte(attrs.getNamedItem("id").getNodeValue());
									for (Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
									{
										if ("spawn".equalsIgnoreCase(spawn.getNodeName()))
										{
											attrs = spawn.getAttributes();
											mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
											delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
											count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
											template = NpcTable.getInstance().getTemplate(mobId);
											if (template == null)
											{
												LOGGER.warning("Template " + mobId + " not found!");
											}
											if (!_rooms.containsKey(type))
											{
												LOGGER.warning("Type " + type + " not found!");
											}
											else if (!_rooms.get(type).containsKey(roomId))
											{
												LOGGER.warning("Room " + roomId + " in Type " + type + " not found!");
											}
											
											for (int i = 0; i < count; i++)
											{
												final DimensionalRiftRoom riftRoom = _rooms.get(type).get(roomId);
												x = riftRoom.getRandomX();
												y = riftRoom.getRandomY();
												z = riftRoom.getTeleportCoords()[2];
												if ((template != null) && _rooms.containsKey(type) && _rooms.get(type).containsKey(roomId))
												{
													spawnDat = new Spawn(template);
													spawnDat.setAmount(1);
													spawnDat.setX(x);
													spawnDat.setY(y);
													spawnDat.setZ(z);
													spawnDat.setHeading(-1);
													spawnDat.setRespawnDelay(delay);
													SpawnTable.getInstance().addNewSpawn(spawnDat, false);
													_rooms.get(type).get(roomId).getSpawns().add(spawnDat);
													total++;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error on loading dimensional rift spawns: " + e);
		}
		LOGGER.info("DimensionalRiftManager: Loaded " + total + " dimensional rift spawns.");
	}
	
	public void reload()
	{
		for (Map<Byte, DimensionalRiftRoom> rooms : _rooms.values())
		{
			for (DimensionalRiftRoom room : rooms.values())
			{
				room.getSpawns().clear();
			}
			rooms.clear();
		}
		_rooms.clear();
		loadRooms();
		loadSpawns();
	}
	
	public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone)
	{
		if (ignorePeaceZone)
		{
			return _rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z);
		}
		return _rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z) && !_rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public boolean checkIfInPeaceZone(int x, int y, int z)
	{
		return _rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public void teleportToWaitingRoom(PlayerInstance player)
	{
		final int[] coords = getRoom((byte) 0, (byte) 0).getTeleportCoords();
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	public void start(PlayerInstance player, byte type, NpcInstance npc)
	{
		boolean canPass = true;
		if (!player.isInParty())
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
			return;
		}
		
		if (player.getParty().getPartyLeaderOID() != player.getObjectId())
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (player.getParty().isInDimensionalRift())
		{
			handleCheat(player, npc);
			return;
		}
		
		if (!isAreaAvailable(type))
		{
			player.sendMessage("This rift area is full. Try later.");
			return;
		}
		
		if (player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/SmallParty.htm");
			html.replace("%npc_name%", npc.getName());
			html.replace("%count%", String.valueOf(Config.RIFT_MIN_PARTY_SIZE));
			player.sendPacket(html);
			return;
		}
		
		for (PlayerInstance p : player.getParty().getPartyMembers())
		{
			if (!checkIfInPeaceZone(p.getX(), p.getY(), p.getZ()))
			{
				canPass = false;
			}
		}
		
		if (!canPass)
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
			return;
		}
		
		ItemInstance i;
		for (PlayerInstance p : player.getParty().getPartyMembers())
		{
			i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			if (i == null)
			{
				canPass = false;
				break;
			}
			
			if ((i.getCount() > 0) && (i.getCount() < getNeededItems(type)))
			{
				canPass = false;
			}
		}
		
		if (!canPass)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/NoFragments.htm");
			html.replace("%npc_name%", npc.getName());
			html.replace("%count%", String.valueOf(getNeededItems(type)));
			player.sendPacket(html);
			return;
		}
		
		for (PlayerInstance p : player.getParty().getPartyMembers())
		{
			i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			p.destroyItem("RiftEntrance", i.getObjectId(), getNeededItems(type), null, false);
		}
		
		byte room;
		do
		{
			room = (byte) Rnd.get(1, 9);
		}
		while (!isRoomAvailable(type, room));
		new DimensionalRift(player.getParty(), type, room);
	}
	
	public void killRift(DimensionalRift d)
	{
		if (d.getTeleportTimerTask() != null)
		{
			d.getTeleportTimerTask().cancel();
		}
		d.setTeleportTimerTask(null);
		
		if (d.getTeleportTimer() != null)
		{
			d.getTeleportTimer().cancel();
		}
		d.setTeleportTimer(null);
		
		if (d.getSpawnTimerTask() != null)
		{
			d.getSpawnTimerTask().cancel();
		}
		d.setSpawnTimerTask(null);
		
		if (d.getSpawnTimer() != null)
		{
			d.getSpawnTimer().cancel();
		}
		d.setSpawnTimer(null);
	}
	
	public class DimensionalRiftRoom
	{
		protected final byte _type;
		protected final byte _room;
		private final int _xMin;
		private final int _xMax;
		private final int _yMin;
		private final int _yMax;
		private final int _zMin;
		private final int _zMax;
		private final int[] _teleportCoords;
		private final Shape _s;
		private final boolean _isBossRoom;
		private final List<Spawn> _roomSpawns;
		protected final List<NpcInstance> _roomMobs;
		private boolean _isUsed = false;
		
		public DimensionalRiftRoom(byte type, byte room, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, int xT, int yT, int zT, boolean isBossRoom)
		{
			_type = type;
			_room = room;
			_xMin = xMin + 128;
			_xMax = xMax - 128;
			_yMin = yMin + 128;
			_yMax = yMax - 128;
			_zMin = zMin;
			_zMax = zMax;
			_teleportCoords = new int[]
			{
				xT,
				yT,
				zT
			};
			_isBossRoom = isBossRoom;
			_roomSpawns = new ArrayList<>();
			_roomMobs = new ArrayList<>();
			_s = new Polygon(new int[]
			{
				xMin,
				xMax,
				xMax,
				xMin
			}, new int[]
			{
				yMin,
				yMin,
				yMax,
				yMax
			}, 4);
		}
		
		public int getRandomX()
		{
			return Rnd.get(_xMin, _xMax);
		}
		
		public int getRandomY()
		{
			return Rnd.get(_yMin, _yMax);
		}
		
		public int[] getTeleportCoords()
		{
			return _teleportCoords;
		}
		
		public boolean checkIfInZone(int x, int y, int z)
		{
			return _s.contains(x, y) && (z >= _zMin) && (z <= _zMax);
		}
		
		public boolean isBossRoom()
		{
			return _isBossRoom;
		}
		
		public List<Spawn> getSpawns()
		{
			return _roomSpawns;
		}
		
		public void spawn()
		{
			for (Spawn spawn : _roomSpawns)
			{
				spawn.doSpawn();
				if ((spawn.getNpcId() < 25333) && (spawn.getNpcId() > 25338))
				{
					spawn.startRespawn();
				}
			}
		}
		
		public void unspawn()
		{
			for (Spawn spawn : _roomSpawns)
			{
				spawn.stopRespawn();
				if (spawn.getLastSpawn() != null)
				{
					spawn.getLastSpawn().deleteMe();
				}
			}
			_isUsed = false;
		}
		
		public void setUsed()
		{
			_isUsed = true;
		}
		
		public boolean isUsed()
		{
			return _isUsed;
		}
	}
	
	private int getNeededItems(byte type)
	{
		switch (type)
		{
			case 1:
			{
				return Config.RIFT_ENTER_COST_RECRUIT;
			}
			case 2:
			{
				return Config.RIFT_ENTER_COST_SOLDIER;
			}
			case 3:
			{
				return Config.RIFT_ENTER_COST_OFFICER;
			}
			case 4:
			{
				return Config.RIFT_ENTER_COST_CAPTAIN;
			}
			case 5:
			{
				return Config.RIFT_ENTER_COST_COMMANDER;
			}
			case 6:
			{
				return Config.RIFT_ENTER_COST_HERO;
			}
			default:
			{
				return 999999;
			}
		}
	}
	
	public void showHtmlFile(PlayerInstance player, String file, NpcInstance npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(file);
		html.replace("%npc_name%", npc.getName());
		player.sendPacket(html);
	}
	
	public void handleCheat(PlayerInstance player, NpcInstance npc)
	{
		showHtmlFile(player, "data/html/seven_signs/rift/Cheater.htm", npc);
		if (!player.isGM())
		{
			LOGGER.warning("Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " tried to cheat in dimensional rift.", Config.DEFAULT_PUNISH);
		}
	}
	
	public static DimensionalRiftManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DimensionalRiftManager INSTANCE = new DimensionalRiftManager();
	}
}
