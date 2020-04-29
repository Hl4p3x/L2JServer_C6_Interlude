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
package teleports.GrandBossTeleporters;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.instancemanager.QuestManager;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.zone.type.BossZone;

/**
 * @author Mobius
 * @note Based on python script
 */
public class GrandBossTeleporters extends Quest
{
	// NPCs
	private static final int[] NPCs =
	{
		13001, // Heart of Warding : Teleport into Lair of Antharas
		31859, // Teleportation Cubic : Teleport out of Lair of Antharas
		31384, // Gatekeeper of Fire Dragon : Opening some doors
		31385, // Heart of Volcano : Teleport into Lair of Valakas
		31540, // Watcher of Valakas Klein : Teleport into Hall of Flames
		31686, // Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
		31687, // Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
		31759, // Teleportation Cubic : Teleport out of Lair of Valakas
	};
	// Misc.
	private static int playerCount = 0;
	
	private GrandBossTeleporters()
	{
		super(-1, "teleports");
		
		addStartNpc(NPCs);
		addTalkId(NPCs);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (event.equals("31540"))
		{
			if (st.getQuestItemsCount(7267) > 0)
			{
				st.takeItems(7267, 1);
				player.teleToLocation(183813, -115157, -3303);
				st.set("allowEnter", "1");
				htmltext = null;
			}
			else
			{
				htmltext = "31540-06.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final int npcId = npc.getNpcId();
		String htmltext = null;
		if (npcId == 13001) // heart of warding
		{
			if (antharasAI() != null)
			{
				final int status = GrandBossManager.getInstance().getBossStatus(29019);
				final int statusW = GrandBossManager.getInstance().getBossStatus(29066);
				final int statusN = GrandBossManager.getInstance().getBossStatus(29067);
				final int statusS = GrandBossManager.getInstance().getBossStatus(29068);
				if ((status == 2) || (statusW == 2) || (statusN == 2) || (statusS == 2))
				{
					htmltext = "13001-02.htm";
				}
				else if ((status == 3) || (statusW == 3) || (statusN == 3) || (statusS == 3))
				{
					htmltext = "13001-01.htm";
				}
				else if ((status == 0) || (status == 1)) // If entrance to see Antharas is unlocked (he is Dormant or Waiting)
				{
					final QuestState st = player.getQuestState(getName());
					if (st.getQuestItemsCount(3865) > 0)
					{
						st.takeItems(3865, 1);
						final BossZone zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
						if (zone != null)
						{
							zone.allowPlayerEntry(player, 30);
						}
						final int x = 179700 + Rnd.get(700);
						final int y = 113800 + Rnd.get(2100);
						player.teleToLocation(x, y, -7709);
						if (status == 0)
						{
							antharasAI().startQuestTimer("setAntharasSpawnTask", 1000, npc, player);
						}
					}
					else
					{
						htmltext = "13001-03.htm";
					}
				}
			}
		}
		else if (npcId == 31859) // antharas teleport cube
		{
			final int x = 79800 + Rnd.get(600);
			final int y = 151200 + Rnd.get(1100);
			player.teleToLocation(x, y, -3534);
		}
		else if (npcId == 31385) // heart of volcano
		{
			htmltext = "31385-01.htm";
			if (valakasAI() != null)
			{
				final int status = GrandBossManager.getInstance().getBossStatus(29028);
				if ((status == 0) || (status == 1)) // If entrance to see Valakas is unlocked (he is Dormant or Waiting)
				{
					final QuestState st = player.getQuestState(getName());
					if (playerCount >= 200)
					{
						htmltext = "31385-03.htm";
					}
					else if (st.getInt("allowEnter") == 1)
					{
						st.unset("allowEnter");
						final BossZone zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
						if (zone != null)
						{
							zone.allowPlayerEntry(player, 30);
						}
						final int x = 204328 + Rnd.get(600);
						final int y = -111874 + Rnd.get(600);
						player.teleToLocation(x, y, 70);
						playerCount++;
						if (status == 0)
						{
							final GrandBossInstance valakas = GrandBossManager.getInstance().getBoss(29028);
							valakasAI().startQuestTimer("lock_entry_and_spawn_valakas", 60000 * Config.VALAKAS_WAIT_TIME, valakas, null);
							GrandBossManager.getInstance().setBossStatus(29028, 1);
						}
					}
					else // player cheated, wasn't ported via npc Klein
					{
						htmltext = "31385-04.htm";
					}
				}
				else if (status == 2)
				{
					htmltext = "31385-02.htm";
				}
				else
				{
					htmltext = "31385-01.htm";
				}
			}
			else
			{
				htmltext = "31385-01.htm";
			}
		}
		else if (npcId == 31384) // Gatekeeper of Fire Dragon
		{
			DoorData.getInstance().getDoor(24210004).openMe();
		}
		else if (npcId == 31686) // Gatekeeper of Fire Dragon
		{
			DoorData.getInstance().getDoor(24210005).openMe();
		}
		else if (npcId == 31687) // Gatekeeper of Fire Dragon
		{
			DoorData.getInstance().getDoor(24210006).openMe();
		}
		else if (npcId == 31540) // Watcher of Valakas Klein
		{
			if (playerCount < 50)
			{
				htmltext = "31540-01.htm";
			}
			else if (playerCount < 100)
			{
				htmltext = "31540-02.htm";
			}
			else if (playerCount < 150)
			{
				htmltext = "31540-03.htm";
			}
			else if (playerCount < 200)
			{
				htmltext = "31540-04.htm";
			}
			else
			{
				htmltext = "31540-05.htm";
			}
		}
		else if (npcId == 31759) // valakas teleport cube
		{
			final int x = 150037 + Rnd.get(500);
			final int y = -57720 + Rnd.get(500);
			player.teleToLocation(x, y, -2976);
		}
		return htmltext;
	}
	
	private Quest antharasAI()
	{
		return QuestManager.getInstance().getQuest("Antharas");
	}
	
	private Quest valakasAI()
	{
		return QuestManager.getInstance().getQuest("Valakas");
	}
	
	public static void main(String[] args)
	{
		new GrandBossTeleporters();
	}
}
