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

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.SpecialCamera;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Benom extends Quest
{
	// NPCs
	private static final int BENOM = 29054;
	private static final int BENOM_TELEPORT = 13101;
	// Locations
	private static final Location[] WALK_ROUTES =
	{
		new Location(12565, -49739, -547),
		new Location(11242, -49689, -33),
		new Location(10751, -49702, 83),
		new Location(10824, -50808, 316),
		new Location(9084, -50786, 972),
		new Location(9095, -49787, 1252),
		new Location(8371, -49711, 1252),
		new Location(8423, -48545, 1252),
		new Location(9105, -48474, 1252),
		new Location(9085, -47488, 972),
		new Location(10858, -47527, 316),
		new Location(10842, -48626, 75),
		new Location(12171, -48464, -547),
		new Location(13565, -49145, -535),
		new Location(15653, -49159, -1059),
		new Location(15423, -48402, -839),
		new Location(15066, -47438, -419),
		new Location(13990, -46843, -292),
		new Location(13685, -47371, -163),
		new Location(13384, -47470, -163),
		new Location(14609, -48608, 346),
		new Location(13878, -47449, 747),
		new Location(12894, -49109, 980),
		new Location(10135, -49150, 996),
		new Location(12894, -49109, 980),
		new Location(13738, -50894, 747),
		new Location(14579, -49698, 347),
		new Location(12896, -51135, -166),
		new Location(12971, -52046, -292),
		new Location(15140, -50781, -442),
		new Location(15328, -50406, -603),
		new Location(15594, -49192, -1059),
		new Location(13175, -49153, -537)
	};
	// Misc
	private static final int[] WALK_TIMES =
	{
		18000,
		17000,
		4500,
		16000,
		22000,
		14000,
		10500,
		14000,
		9500,
		12500,
		20500,
		14500,
		17000,
		20000,
		22000,
		11000,
		11000,
		20000,
		8000,
		5500,
		20000,
		18000,
		25000,
		28000,
		25000,
		25000,
		25000,
		25000,
		10000,
		24000,
		7000,
		12000,
		20000
	};
	private static final String[] TALK =
	{
		"You should have finished me when you had the chance!!!",
		"I will crush all of you!!!",
		"I am not finished here, come face me!!!",
		"You cowards!!! I will torture each and everyone of you!!!"
	};
	private static final int ALIVE = 0;
	private static final int DEAD = 1;
	private static int _benomWalkRouteStep = 0;
	private static int _benomIsSpawned = 0;
	private static NpcInstance _benomInstance;
	private static NpcInstance _teleportInstance;
	
	public Benom()
	{
		super(-1, "ai/bosses");
		
		addStartNpc(BENOM_TELEPORT);
		addTalkId(BENOM_TELEPORT);
		addFirstTalkId(BENOM_TELEPORT);
		addAttackId(BENOM);
		addKillId(BENOM);
		
		final int castleOwner = CastleManager.getInstance().getCastleById(8).getOwnerId();
		final long siegeDate = CastleManager.getInstance().getCastleById(8).getSiegeDate().getTimeInMillis();
		long benomTeleporterSpawn = (siegeDate - System.currentTimeMillis()) - 86400000;
		final long benomRaidRoomSpawn = (siegeDate - System.currentTimeMillis()) - 86400000;
		long benomRaidSiegeSpawn = (siegeDate - System.currentTimeMillis());
		if (benomTeleporterSpawn < 0)
		{
			benomTeleporterSpawn = 1;
		}
		if (benomRaidSiegeSpawn < 0)
		{
			benomRaidSiegeSpawn = 1;
		}
		if (castleOwner > 0)
		{
			if (benomTeleporterSpawn >= 1)
			{
				startQuestTimer("BenomTeleSpawn", benomTeleporterSpawn, null, null);
			}
			if ((siegeDate - System.currentTimeMillis()) > 0)
			{
				startQuestTimer("BenomRaidRoomSpawn", benomRaidRoomSpawn, null, null);
			}
			startQuestTimer("BenomRaidSiegeSpawn", benomRaidSiegeSpawn, null, null);
		}
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		switch (event)
		{
			case "BenomTeleSpawn":
			{
				_teleportInstance = addSpawn(BENOM_TELEPORT, 11013, -49629, -547, 13400, false, 0);
				break;
			}
			case "BenomRaidRoomSpawn":
			{
				if ((_benomIsSpawned == 0) && (GrandBossManager.getInstance().getBossStatus(BENOM) == 0))
				{
					_benomInstance = addSpawn(BENOM, 12047, -49211, -3009, 0, false, 0);
					_benomIsSpawned = 1;
				}
				break;
			}
			case "BenomRaidSiegeSpawn":
			{
				if (GrandBossManager.getInstance().getBossStatus(BENOM) == 0)
				{
					if (_benomIsSpawned == 0)
					{
						_benomInstance = addSpawn(BENOM, 11025, -49152, -537, 0, false, 0);
						_benomIsSpawned = 1;
					}
					else if (_benomIsSpawned == 1)
					{
						_benomInstance.teleToLocation(11025, -49152, -537);
					}
					startQuestTimer("BenomSpawnEffect", 100, null, null);
					startQuestTimer("BenomBossDespawn", 5400000, null, null);
					_teleportInstance.deleteMe();
				}
				break;
			}
			case "BenomSpawnEffect":
			{
				_benomInstance.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				_benomInstance.broadcastPacket(new SpecialCamera(_benomInstance.getObjectId(), 200, 0, 150, 0, 5000));
				_benomInstance.broadcastPacket(new SocialAction(_benomInstance.getObjectId(), 3));
				startQuestTimer("BenomWalk", 5000, _benomInstance, null);
				_benomWalkRouteStep = 0;
				break;
			}
			case "Attacking":
			{
				final Collection<PlayerInstance> knownPlayers = npc.getKnownList().getKnownPlayers().values();
				if (!knownPlayers.isEmpty())
				{
					final PlayerInstance target = knownPlayers.stream().findAny().get();
					((MonsterInstance) npc).addDamageHate(target, 0, 999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					startQuestTimer("Attacking", 2000, npc, player);
				}
				else
				{
					startQuestTimer("BenomWalkFinish", 2000, npc, null);
				}
				break;
			}
			case "BenomWalkFinish":
			{
				if (npc.getCastle().getSiege().isInProgress())
				{
					cancelQuestTimer("Attacking", npc, player);
					npc.teleToLocation(WALK_ROUTES[_benomWalkRouteStep], false);
					npc.setWalking();
					_benomWalkRouteStep = 0;
					startQuestTimer("BenomWalk", 2200, npc, null);
				}
				break;
			}
			case "BenomWalk":
			{
				if (_benomWalkRouteStep == 33)
				{
					_benomWalkRouteStep = 0;
					startQuestTimer("BenomWalk", 100, npc, null);
				}
				else
				{
					startQuestTimer("Talk", 100, npc, null);
					if (_benomWalkRouteStep == 14)
					{
						startQuestTimer("DoorOpen", 15000, null, null);
						startQuestTimer("DoorClose", 23000, null, null);
					}
					if (_benomWalkRouteStep == 32)
					{
						startQuestTimer("DoorOpen", 500, null, null);
						startQuestTimer("DoorClose", 4000, null, null);
					}
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, WALK_ROUTES[_benomWalkRouteStep]);
					startQuestTimer("BenomWalk", WALK_TIMES[_benomWalkRouteStep], npc, null);
					_benomWalkRouteStep = _benomWalkRouteStep + 1;
				}
				break;
			}
			case "DoorOpen":
			{
				DoorData.getInstance().getDoor(20160005).openMe();
				break;
			}
			case "DoorClose":
			{
				DoorData.getInstance().getDoor(20160005).closeMe();
				break;
			}
			case "Talk":
			{
				if (Rnd.get(100) < 40)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, "Benom", TALK[Rnd.get(4)]));
				}
				break;
			}
			case "BenomBossDespawn":
			{
				GrandBossManager.getInstance().setBossStatus(BENOM, ALIVE);
				_benomIsSpawned = 0;
				_benomInstance.deleteMe();
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, PlayerInstance player)
	{
		final int castleOwner = CastleManager.getInstance().getCastleById(8).getOwnerId();
		final int clanId = player.getClanId();
		if ((castleOwner > 0) && (clanId > 0))
		{
			if (castleOwner == clanId)
			{
				final int x = 12558 + (Rnd.get(200) - 100);
				final int y = -49279 + (Rnd.get(200) - 100);
				player.teleToLocation(x, y, -3007);
				return null;
			}
			return "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>";
		}
		return "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>";
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		cancelQuestTimer("BenomWalk", npc, null);
		cancelQuestTimer("BenomWalkFinish", npc, null);
		startQuestTimer("Attacking", 100, npc, attacker);
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		GrandBossManager.getInstance().setBossStatus(BENOM, DEAD);
		cancelQuestTimer("BenomWalk", npc, null);
		cancelQuestTimer("BenomWalkFinish", npc, null);
		cancelQuestTimer("BenomBossDespawn", npc, null);
		cancelQuestTimer("Talk", npc, null);
		cancelQuestTimer("Attacking", npc, null);
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Benom();
	}
}
