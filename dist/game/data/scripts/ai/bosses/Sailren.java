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

import org.l2jserver.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.SpecialCamera;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Sailren extends Quest
{
	// NPCs
	private static final int STATUE = 32109;
	private static final int SAILREN = 29065;
	private static final int VELO = 22196;
	private static final int PTERO = 22199;
	private static final int TREX = 22215;
	private static final int STONE = 8784;
	// Misc
	private static NpcInstance _vlcInstance;
	private static NpcInstance _ptrInstance;
	private static NpcInstance _trxInstance;
	private static NpcInstance _slrnInstance;
	
	private Sailren()
	{
		super(-1, "ai/bosses");
		
		addStartNpc(STATUE);
		addTalkId(STATUE);
		addKillId(VELO, PTERO, TREX, SAILREN);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		switch (event)
		{
			case "start":
			{
				_vlcInstance = addSpawn(VELO, 27845, -5567, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, _vlcInstance, player);
				cancelQuestTimer("start", npc, null);
				break;
			}
			case "round2":
			{
				_ptrInstance = addSpawn(PTERO, 27838, -5578, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, _ptrInstance, player);
				cancelQuestTimer("round2", npc, null);
				break;
			}
			case "round3":
			{
				_trxInstance = addSpawn(TREX, 27838, -5578, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, _trxInstance, player);
				cancelQuestTimer("round3", npc, null);
				break;
			}
			case "sailren":
			{
				_slrnInstance = addSpawn(SAILREN, 27489, -6223, -1982, 45000, false, 0);
				startQuestTimer("camera", 2000, _slrnInstance, player);
				startQuestTimer("vkrovatku", 1200000, _slrnInstance, null);
				cancelQuestTimer("round4", npc, null);
				break;
			}
			case "camera":
			{
				player.broadcastPacket(new SpecialCamera(npc.getObjectId(), 400, -75, 3, -150, 5000));
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
				break;
			}
			case "open":
			{
				GlobalVariablesManager.getInstance().remove("SailrenClose");
				cancelQuestTimer("open", npc, null);
				break;
			}
			case "vkrovatku":
			{
				npc.deleteMe();
				GlobalVariablesManager.getInstance().remove("SailrenClose");
				cancelQuestTimer("open", npc, null);
				cancelQuestTimer("vkrovatku", npc, null);
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		if (player.getInventory().getItemByItemId(STONE) != null)
		{
			if (!GlobalVariablesManager.getInstance().hasVariable("SailrenClose"))
			{
				final Party party = player.getParty();
				if (party != null)
				{
					player.destroyItemByItemId("Sailren", STONE, 1, player, true);
					GlobalVariablesManager.getInstance().set("SailrenClose", true);
					final BossZone zone = GrandBossManager.getInstance().getZone(27244, -7026, -1974);
					for (PlayerInstance member : party.getPartyMembers())
					{
						if (zone != null)
						{
							zone.allowPlayerEntry(member, 3600);
						}
						member.teleToLocation(27244, -7026, -1974);
					}
					startQuestTimer("start", 30000, npc, player);
					startQuestTimer("open", 1800000, npc, null);
				}
				else
				{
					return "<html><body><font color=LEVEL>Only with party...</font></body></html>";
				}
			}
			else
			{
				return "<html><body><font color=LEVEL>Some one else is inside...</font></body></html>";
			}
		}
		else
		{
			return "<html><body>You need quest item: <font color=LEVEL>Gazkh...</font></body></html>";
		}
		return null;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		if (npc == _vlcInstance)
		{
			startQuestTimer("round2", 30000, npc, killer);
		}
		else if (npc == _ptrInstance)
		{
			startQuestTimer("round3", 60000, npc, killer);
		}
		else if (npc == _trxInstance)
		{
			startQuestTimer("sailren", 180000, npc, killer);
		}
		else if (npc == _slrnInstance)
		{
			GlobalVariablesManager.getInstance().remove("SailrenClose");
			cancelQuestTimer("open", npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Sailren();
	}
}
