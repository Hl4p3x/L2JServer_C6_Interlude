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
package ai.bosses;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.l2jserver.Config;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Ice Fairy Sirra AI
 * @author Kerberos
 */
public class IceFairySirra extends Quest
{
	private static final int STEWARD = 32029;
	private static final int SILVER_HEMOCYTE = 8057;
	private static BossZone _freyasZone;
	private static PlayerInstance _player = null;
	protected Collection<NpcInstance> _allMobs = ConcurrentHashMap.newKeySet();
	protected Future<?> _onDeadEventTask = null;
	
	public IceFairySirra()
	{
		super(-1, "ai/bosses");
		final int[] mobs =
		{
			STEWARD,
			22100,
			22102,
			22104
		};
		
		for (int mob : mobs)
		{
			// TODO:
			addEventId(mob, EventType.QUEST_START);
			addEventId(mob, EventType.QUEST_TALK);
			addEventId(mob, EventType.NPC_FIRST_TALK);
		}
		
		init();
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, PlayerInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		String filename = "";
		if (npc.isBusy())
		{
			filename = getHtmlPath(10);
		}
		else
		{
			filename = getHtmlPath(0);
		}
		sendHtml(npc, player, filename);
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		if (event.equals("check_condition"))
		{
			if (npc.isBusy())
			{
				return super.onAdvEvent(event, npc, player);
			}
			
			String filename = "";
			if (player.isInParty() && (player.getParty().getPartyLeaderOID() == player.getObjectId()))
			{
				if (checkItems(player))
				{
					startQuestTimer("start", 100000, null, player);
					_player = player;
					destroyItems(player);
					player.getInventory().addItem("Scroll", 8379, 3, player, null);
					npc.setBusy(true);
					screenMessage(player, "Steward: Please wait a moment.", 100000);
					filename = getHtmlPath(3);
				}
				else
				{
					filename = getHtmlPath(2);
				}
			}
			else
			{
				filename = getHtmlPath(1);
			}
			sendHtml(npc, player, filename);
		}
		else if (event.equals("start"))
		{
			if (_freyasZone == null)
			{
				LOGGER.warning("IceFairySirraManager: Failed to load zone");
				cleanUp();
				return super.onAdvEvent(event, npc, player);
			}
			_freyasZone.setZoneEnabled(true);
			closeGates();
			doSpawns();
			startQuestTimer("Party_Port", 2000, null, player);
			startQuestTimer("End", 1802000, null, player);
		}
		else if (event.equals("Party_Port"))
		{
			teleportInside(player);
			screenMessage(player, "Steward: Please restore the Queen's appearance!", 10000);
			startQuestTimer("30MinutesRemaining", 300000, null, player);
		}
		else if (event.equals("30MinutesRemaining"))
		{
			screenMessage(player, "30 minute(s) are remaining.", 10000);
			startQuestTimer("20minutesremaining", 600000, null, player);
		}
		else if (event.equals("20MinutesRemaining"))
		{
			screenMessage(player, "20 minute(s) are remaining.", 10000);
			startQuestTimer("10minutesremaining", 600000, null, player);
		}
		else if (event.equals("10MinutesRemaining"))
		{
			screenMessage(player, "Steward: Waste no time! Please hurry!", 10000);
		}
		else if (event.equals("End"))
		{
			screenMessage(player, "Steward: Was it indeed too much to ask.", 10000);
			cleanUp();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public void init()
	{
		_freyasZone = GrandBossManager.getInstance().getZone(105546, -127892, -2768);
		if (_freyasZone == null)
		{
			LOGGER.warning("IceFairySirraManager: Failed to load zone");
			return;
		}
		_freyasZone.setZoneEnabled(false);
		final NpcInstance steward = findTemplate(STEWARD);
		if (steward != null)
		{
			steward.setBusy(false);
		}
		openGates();
	}
	
	public void cleanUp()
	{
		init();
		cancelQuestTimer("30MinutesRemaining", null, _player);
		cancelQuestTimer("20MinutesRemaining", null, _player);
		cancelQuestTimer("10MinutesRemaining", null, _player);
		cancelQuestTimer("End", null, _player);
		for (NpcInstance mob : _allMobs)
		{
			try
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			catch (Exception e)
			{
				LOGGER.warning("IceFairySirraManager: Failed deleting mob. " + e);
			}
		}
		_allMobs.clear();
	}
	
	public NpcInstance findTemplate(int npcId)
	{
		NpcInstance npc = null;
		for (Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if ((spawn != null) && (spawn.getNpcId() == npcId))
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}
	
	protected void openGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				final DoorInstance door = DoorData.getInstance().getDoor(i);
				if (door != null)
				{
					door.openMe();
				}
				else
				{
					LOGGER.warning("IceFairySirraManager: Attempted to open undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("IceFairySirraManager: Failed closing door " + e);
			}
		}
	}
	
	protected void closeGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				final DoorInstance door = DoorData.getInstance().getDoor(i);
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					LOGGER.warning("IceFairySirraManager: Attempted to close undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("IceFairySirraManager: Failed closing door " + e);
			}
		}
	}
	
	public boolean checkItems(PlayerInstance player)
	{
		if (player.getParty() != null)
		{
			for (PlayerInstance pc : player.getParty().getPartyMembers())
			{
				final ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				if ((i == null) || (i.getCount() < 10))
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	public void destroyItems(PlayerInstance player)
	{
		if (player.getParty() != null)
		{
			for (PlayerInstance pc : player.getParty().getPartyMembers())
			{
				final ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				pc.destroyItem("Hemocytes", i.getObjectId(), 10, null, false);
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void teleportInside(PlayerInstance player)
	{
		if (player.getParty() != null)
		{
			for (PlayerInstance pc : player.getParty().getPartyMembers())
			{
				pc.teleToLocation(113533, -126159, -3488, false);
				if (_freyasZone == null)
				{
					LOGGER.warning("IceFairySirraManager: Failed to load zone");
					cleanUp();
					return;
				}
				_freyasZone.allowPlayerEntry(pc, 2103);
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void screenMessage(PlayerInstance player, String text, int time)
	{
		if (player.getParty() != null)
		{
			for (PlayerInstance pc : player.getParty().getPartyMembers())
			{
				pc.sendPacket(new ExShowScreenMessage(text, time));
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void doSpawns()
	{
		final int[][] mobs =
		{
			{
				29060,
				105546,
				-127892,
				-2768
			},
			{
				29056,
				102779,
				-125920,
				-2840
			},
			{
				22100,
				111719,
				-126646,
				-2992
			},
			{
				22102,
				109509,
				-128946,
				-3216
			},
			{
				22104,
				109680,
				-125756,
				-3136
			}
		};
		Spawn spawnDat;
		NpcTemplate template;
		try
		{
			for (int i = 0; i < 5; i++)
			{
				template = NpcTable.getInstance().getTemplate(mobs[i][0]);
				if (template != null)
				{
					spawnDat = new Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setX(mobs[i][1]);
					spawnDat.setY(mobs[i][2]);
					spawnDat.setZ(mobs[i][3]);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(60);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_allMobs.add(spawnDat.doSpawn());
					spawnDat.stopRespawn();
				}
				else
				{
					LOGGER.warning("IceFairySirraManager: Data missing in NPC table for ID: " + mobs[i][0]);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("IceFairySirraManager: Spawns could not be initialized: " + e);
		}
	}
	
	public String getHtmlPath(int value)
	{
		String pom = "";
		pom = "32029-" + value;
		if (value == 0)
		{
			pom = "32029";
		}
		
		final String temp = "data/html/default/" + pom + ".htm";
		if (!Config.LAZY_CACHE)
		{
			// If not running lazy cache the file must be in the cache or it doesnt exist
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else if (HtmCache.getInstance().isLoadable(temp))
		{
			return temp;
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "data/html/npcdefault.htm";
	}
	
	public void sendHtml(NpcInstance npc, PlayerInstance player, String filename)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public static void main(String[] args)
	{
		new IceFairySirra();
	}
}
