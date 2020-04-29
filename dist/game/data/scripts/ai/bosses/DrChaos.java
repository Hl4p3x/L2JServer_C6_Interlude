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

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.SpecialCamera;

/**
 * @author Mobius
 * @note Based on python script
 */
public class DrChaos extends Quest
{
	// NPCs
	private static final int STRANGE_MACHINE = 32032;
	private static final int DR_CHAOS = 32033;
	private static final int CHAOS_GOLEM = 25512;
	// Misc
	private static int _golemSpawned = 0;
	private static int _chaosSpawned = 1;
	
	private DrChaos()
	{
		super(-1, "ai/bosses");
		
		addFirstTalkId(DR_CHAOS);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		switch (event)
		{
			case "1":
			{
				final NpcInstance strangeMachine = findTemplate(STRANGE_MACHINE);
				if (strangeMachine != null)
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, strangeMachine);
					strangeMachine.broadcastPacket(new SpecialCamera(strangeMachine.getObjectId(), 1, -200, 15, 10000, 20000));
				}
				else
				{
					LOGGER.warning("Dr Chaos AI: problem finding Strange Machine (npcid = " + STRANGE_MACHINE + "). Error: not spawned!");
				}
				startQuestTimer("2", 2000, npc, player);
				startQuestTimer("3", 10000, npc, player);
				if (_chaosSpawned == 0)
				{
					addSpawn(DR_CHAOS, 96471, -111425, -3334, 0, false, 0);
					_chaosSpawned = 1;
				}
				startQuestTimer("2", 2000, npc, player);
				startQuestTimer("3", 10000, npc, player);
				break;
			}
			case "2":
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
				break;
			}
			case "3":
			{
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -150, 10, 3000, 20000));
				startQuestTimer("4", 2500, npc, player);
				break;
			}
			case "4":
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(96055, -110759, -3312));
				startQuestTimer("5", 2000, npc, player);
				break;
			}
			case "5":
			{
				player.teleToLocation(94832, -112624, -3304);
				npc.teleToLocation(-113091, -243942, -15536);
				if (_golemSpawned == 0)
				{
					final NpcInstance golem = addSpawn(CHAOS_GOLEM, 94640, -112496, -3336, 0, false, 0);
					_golemSpawned = 1;
					startQuestTimer("6", 1000, golem, player);
					player.sendPacket(new PlaySound(1, "Rm03_A"));
				}
				break;
			}
			case "6":
			{
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 30, -200, 20, 6000, 8000));
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, PlayerInstance player)
	{
		player.setTarget(null);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(96323, -110914, -3328));
		startQuestTimer("1", 3000, npc, player);
		return null;
	}
	
	private NpcInstance findTemplate(int npcId)
	{
		NpcInstance npcInstance = null;
		for (Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if ((spawn != null) && (spawn.getNpcId() == npcId))
			{
				npcInstance = spawn.getLastSpawn();
				break;
			}
		}
		return npcInstance;
	}
	
	public static void main(String[] args)
	{
		new DrChaos();
	}
}
