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
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.instancemanager.FortManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.type.ArenaZone;
import org.l2jserver.gameserver.model.zone.type.ClanHallZone;
import org.l2jserver.gameserver.model.zone.type.TownZone;

/**
 * @author Mobius
 */
public class MapRegionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(MapRegionData.class.getName());
	
	public static final Location FLORAN_VILLAGE_LOCATION = new Location(17817, 170079, -3530);
	public static final Location JAIL_LOCATION = new Location(-114356, -249645, -2984);
	private static final Location EXIT_MONSTER_RACE_LOCATION = new Location(12661, 181687, -3560);
	private static final int[][] REGIONS = new int[19][21];
	
	protected MapRegionData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/MapRegions.xml");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			int id = 0;
			final StatSet set = new StatSet();
			final Node n = doc.getFirstChild();
			for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("map".equalsIgnoreCase(node.getNodeName()))
				{
					final NamedNodeMap attrs = node.getAttributes();
					for (int i = 0; i < attrs.getLength(); i++)
					{
						final Node attr = attrs.item(i);
						set.set(attr.getNodeName(), attr.getNodeValue());
					}
					
					id = set.getInt("id");
					REGIONS[0][id] = set.getInt("region1");
					REGIONS[1][id] = set.getInt("region2");
					REGIONS[2][id] = set.getInt("region3");
					REGIONS[3][id] = set.getInt("region4");
					REGIONS[4][id] = set.getInt("region5");
					REGIONS[5][id] = set.getInt("region6");
					REGIONS[6][id] = set.getInt("region7");
					REGIONS[7][id] = set.getInt("region8");
					REGIONS[8][id] = set.getInt("region9");
					REGIONS[9][id] = set.getInt("region10");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Error while reading map region data: " + e);
		}
	}
	
	public int getMapRegion(int posX, int posY)
	{
		return REGIONS[getMapRegionX(posX)][getMapRegionY(posY)];
	}
	
	public int getMapRegionX(int posX)
	{
		// +4 to shift coords to center
		return (posX >> 15) + 4;
	}
	
	public int getMapRegionY(int posY)
	{
		// +10 to shift coords to center
		return (posY >> 15) + 10;
	}
	
	public Location getTeleToLocation(Creature creature, TeleportWhereType teleportWhere)
	{
		// The character isn't a player, bypass all checks and retrieve a random spawn location on closest town.
		if (!(creature instanceof PlayerInstance))
		{
			return getClosestTown(creature.getX(), creature.getY()).getSpawnLoc();
		}
		
		final PlayerInstance player = creature.getActingPlayer();
		
		// If in Monster Derby Track
		if (player.isInsideZone(ZoneId.MONSTER_TRACK))
		{
			return EXIT_MONSTER_RACE_LOCATION;
		}
		
		Castle castle = null;
		Fort fort = null;
		ClanHall clanhall = null;
		if (player.getClan() != null)
		{
			// If teleport to clan hall
			if (teleportWhere == TeleportWhereType.CLANHALL)
			{
				clanhall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
				if (clanhall != null)
				{
					final ClanHallZone zone = clanhall.getZone();
					if (zone != null)
					{
						return zone.getSpawnLoc();
					}
				}
			}
			
			// If teleport to castle
			if (teleportWhere == TeleportWhereType.CASTLE)
			{
				castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
			}
			
			// If teleport to fort
			if (teleportWhere == TeleportWhereType.FORTRESS)
			{
				fort = FortManager.getInstance().getFortByOwner(player.getClan());
			}
			
			// Check if player is on castle or fortress ground
			if (castle == null)
			{
				castle = CastleManager.getInstance().getCastle(player);
			}
			
			if (fort == null)
			{
				fort = FortManager.getInstance().getFort(player);
			}
			
			if ((castle != null) && (castle.getCastleId() > 0))
			{
				// If Teleporting to castle or if is on caslte with siege and player's clan is defender
				if ((teleportWhere == TeleportWhereType.CASTLE) || ((teleportWhere == TeleportWhereType.CASTLE) && castle.getSiege().isInProgress() && (castle.getSiege().getDefenderClan(player.getClan()) != null)))
				{
					return castle.getZone().getSpawnLoc();
				}
				
				if ((teleportWhere == TeleportWhereType.SIEGEFLAG) && castle.getSiege().isInProgress())
				{
					// Check if player's clan is attacker
					final List<NpcInstance> flags = castle.getSiege().getFlag(player.getClan());
					if ((flags != null) && !flags.isEmpty())
					{
						// Spawn to flag - Need more work to get player to the nearest flag
						final NpcInstance flag = flags.get(0);
						return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
				}
			}
			else if ((fort != null) && (fort.getFortId() > 0))
			{
				// Teleporting to castle or fortress is on castle with siege and player's clan is defender
				if ((teleportWhere == TeleportWhereType.FORTRESS) || ((teleportWhere == TeleportWhereType.FORTRESS) && fort.getSiege().isInProgress() && (fort.getSiege().getDefenderClan(player.getClan()) != null)))
				{
					return fort.getZone().getSpawnLoc();
				}
				
				if ((teleportWhere == TeleportWhereType.SIEGEFLAG) && fort.getSiege().isInProgress())
				{
					// Check if player's clan is attacker
					final List<NpcInstance> flags = fort.getSiege().getFlag(player.getClan());
					if ((flags != null) && !flags.isEmpty())
					{
						// Spawn to flag
						final NpcInstance flag = flags.get(0);
						return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
				}
			}
		}
		
		// Karma player lands out of city.
		if (player.getKarma() > 0)
		{
			return getClosestTown(player).getChaoticSpawnLoc();
		}
		
		// Check if player is in arena.
		final ArenaZone arena = ZoneData.getInstance().getZone(player, ArenaZone.class);
		if (arena != null)
		{
			return arena.getSpawnLoc();
		}
		
		// Retrieve a random spawn location of the nearest town.
		return getClosestTown(player).getSpawnLoc();
	}
	
	public int getAreaCastle(Creature creature)
	{
		switch (getClosestTownNumber(creature))
		{
			case 0: // Talking Island Village
			{
				return 1;
			}
			case 1: // Elven Village
			{
				return 4;
			}
			case 2: // Dark Elven Village
			{
				return 4;
			}
			case 3: // Orc Village
			{
				return 9;
			}
			case 4: // Dwarven Village
			{
				return 9;
			}
			case 5: // Town of Gludio
			{
				return 1;
			}
			case 6: // Gludin Village
			{
				return 1;
			}
			case 7: // Town of Dion
			{
				return 2;
			}
			case 8: // Town of Giran
			{
				return 3;
			}
			case 9: // Town of Oren
			{
				return 4;
			}
			case 10: // Town of Aden
			{
				return 5;
			}
			case 11: // Hunters Village
			{
				return 5;
			}
			case 12: // Giran Harbor
			{
				return 3;
			}
			case 13: // Heine
			{
				return 6;
			}
			case 14: // Rune Township
			{
				return 8;
			}
			case 15: // Town of Goddard
			{
				return 7;
			}
			case 16: // Town of Shuttgart
			{
				return 9;
			}
			case 17: // Ivory Tower
			{
				return 4;
			}
			case 18: // Primeval Isle Wharf
			{
				return 8;
			}
			default: // Town of Aden
			{
				return 5;
			}
		}
	}
	
	public int getClosestTownNumber(Creature creature)
	{
		return getMapRegion(creature.getX(), creature.getY());
	}
	
	public String getClosestTownName(Creature creature)
	{
		switch (getMapRegion(creature.getX(), creature.getY()))
		{
			case 0:
			{
				return "Talking Island Village";
			}
			case 1:
			{
				return "Elven Village";
			}
			case 2:
			{
				return "Dark Elven Village";
			}
			case 3:
			{
				return "Orc Village";
			}
			case 4:
			{
				return "Dwarven Village";
			}
			case 5:
			{
				return "Town of Gludio";
			}
			case 6:
			{
				return "Gludin Village";
			}
			case 7:
			{
				return "Town of Dion";
			}
			case 8:
			{
				return "Town of Giran";
			}
			case 9:
			{
				return "Town of Oren";
			}
			case 10:
			{
				return "Town of Aden";
			}
			case 11:
			{
				return "Hunters Village";
			}
			case 12:
			{
				return "Giran Harbor";
			}
			case 13:
			{
				return "Heine";
			}
			case 14:
			{
				return "Rune Township";
			}
			case 15:
			{
				return "Town of Goddard";
			}
			case 16:
			{
				return "Town of Shuttgart";
			}
			case 18:
			{
				return "Primeval Isle";
			}
			default:
			{
				return "Town of Aden";
			}
		}
	}
	
	/**
	 * A specific method, used ONLY by players. There's a Race condition.
	 * @param player : The player used to find race, x and y.
	 * @return the closest TownZone based on a X/Y location.
	 */
	private TownZone getClosestTown(PlayerInstance player)
	{
		switch (getMapRegion(player.getX(), player.getY()))
		{
			case 0: // TI
			{
				return getTown(2);
			}
			case 1: // Elven
			{
				return getTown((player.getTemplate().getRace() == Race.DARK_ELF) ? 1 : 3);
			}
			case 2: // DE
			{
				return getTown((player.getTemplate().getRace() == Race.ELF) ? 3 : 1);
			}
			case 3: // Orc
			{
				return getTown(4);
			}
			case 4: // Dwarven
			{
				return getTown(6);
			}
			case 5: // Gludio
			{
				return getTown(7);
			}
			case 6: // Gludin
			{
				return getTown(5);
			}
			case 7: // Dion
			{
				return getTown(8);
			}
			case 8: // Giran
			case 12: // Giran Harbor
			{
				return getTown(9);
			}
			case 9: // Oren
			{
				return getTown(10);
			}
			case 10: // Aden
			{
				return getTown(12);
			}
			case 11: // HV
			{
				return getTown(11);
			}
			case 13: // Heine
			{
				return getTown(15);
			}
			case 14: // Rune
			{
				return getTown(14);
			}
			case 15: // Goddard
			{
				return getTown(13);
			}
			case 16: // Schuttgart
			{
				return getTown(17);
			}
			case 17: // Floran
			{
				return getTown(16);
			}
			case 18: // Primeval Isle
			{
				return getTown(19);
			}
		}
		return getTown(16); // Default to floran
	}
	
	/**
	 * @param x : The current character's X location.
	 * @param y : The current character's Y location.
	 * @return the closest L2TownZone based on a X/Y location.
	 */
	private TownZone getClosestTown(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // TI
			{
				return getTown(2);
			}
			case 1: // Elven
			{
				return getTown(3);
			}
			case 2: // DE
			{
				return getTown(1);
			}
			case 3: // Orc
			{
				return getTown(4);
			}
			case 4: // Dwarven
			{
				return getTown(6);
			}
			case 5: // Gludio
			{
				return getTown(7);
			}
			case 6: // Gludin
			{
				return getTown(5);
			}
			case 7: // Dion
			{
				return getTown(8);
			}
			case 8: // Giran
			case 12: // Giran Harbor
			{
				return getTown(9);
			}
			case 9: // Oren
			{
				return getTown(10);
			}
			case 10: // Aden
			{
				return getTown(12);
			}
			case 11: // HV
			{
				return getTown(11);
			}
			case 13: // Heine
			{
				return getTown(15);
			}
			case 14: // Rune
			{
				return getTown(14);
			}
			case 15: // Goddard
			{
				return getTown(13);
			}
			case 16: // Schuttgart
			{
				return getTown(17);
			}
			case 17: // Floran
			{
				return getTown(16);
			}
			case 18: // Primeval Isle
			{
				return getTown(19);
			}
		}
		return getTown(16); // Default to floran
	}
	
	/**
	 * @param x : The current character's X location.
	 * @param y : The current character's Y location.
	 * @return the closest region based on a X/Y location.
	 */
	public int getClosestLocation(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // TI
			{
				return 1;
			}
			case 1: // Elven
			{
				return 4;
			}
			case 2: // DE
			{
				return 3;
			}
			case 3: // Orc
			case 4: // Dwarven
			case 16: // Schuttgart
			{
				return 9;
			}
			case 5: // Gludio
			case 6: // Gludin
			{
				return 2;
			}
			case 7: // Dion
			{
				return 5;
			}
			case 8: // Giran
			case 12: // Giran Harbor
			{
				return 6;
			}
			case 9: // Oren
			{
				return 10;
			}
			case 10: // Aden
			{
				return 13;
			}
			case 11: // HV
			{
				return 11;
			}
			case 13: // Heine
			{
				return 12;
			}
			case 14: // Rune
			{
				return 14;
			}
			case 15: // Goddard
			{
				return 15;
			}
		}
		return 0;
	}
	
	/**
	 * @param townId the townId to match.
	 * @return a TownZone based on the overall list of TownZone, matching the townId.
	 */
	public final TownZone getTown(int townId)
	{
		for (TownZone temp : ZoneData.getInstance().getAllZones(TownZone.class))
		{
			if (temp.getTownId() == townId)
			{
				return temp;
			}
		}
		return null;
	}
	
	public static MapRegionData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MapRegionData INSTANCE = new MapRegionData();
	}
}
