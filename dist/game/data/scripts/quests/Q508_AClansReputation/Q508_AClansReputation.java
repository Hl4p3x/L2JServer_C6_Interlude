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
package quests.Q508_AClansReputation;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

public class Q508_AClansReputation extends Quest
{
	// NPC
	private static final int SIR_ERIC_RODEMAI = 30868;
	
	// Items
	private static final int NUCLEUS_OF_FLAMESTONE_GIANT = 8494;
	private static final int THEMIS_SCALE = 8277;
	private static final int NUCLEUS_OF_HEKATON_PRIME = 8279;
	private static final int TIPHON_SHARD = 8280;
	private static final int GLAKIS_NUCLEUS = 8281;
	private static final int RAHHAS_FANG = 8282;
	
	// Raidbosses
	private static final int FLAMESTONE_GIANT = 25524;
	private static final int PALIBATI_QUEEN_THEMIS = 25252;
	private static final int HEKATON_PRIME = 25140;
	private static final int GARGOYLE_LORD_TIPHON = 25255;
	private static final int LAST_LESSER_GIANT_GLAKI = 25245;
	private static final int RAHHA = 25051;
	
	// Reward list (itemId, minClanPoints, maxClanPoints)
	private static final int[][] REWARD_LIST =
	{
		{
			PALIBATI_QUEEN_THEMIS,
			THEMIS_SCALE,
			65,
			100
		},
		{
			HEKATON_PRIME,
			NUCLEUS_OF_HEKATON_PRIME,
			40,
			75
		},
		{
			GARGOYLE_LORD_TIPHON,
			TIPHON_SHARD,
			30,
			65
		},
		{
			LAST_LESSER_GIANT_GLAKI,
			GLAKIS_NUCLEUS,
			105,
			140
		},
		{
			RAHHA,
			RAHHAS_FANG,
			40,
			75
		},
		{
			FLAMESTONE_GIANT,
			NUCLEUS_OF_FLAMESTONE_GIANT,
			60,
			95
		}
	};
	
	// Radar
	private static final int[][] radar =
	{
		{
			192346,
			21528,
			-3648
		},
		{
			191979,
			54902,
			-7658
		},
		{
			170038,
			-26236,
			-3824
		},
		{
			171762,
			55028,
			-5992
		},
		{
			117232,
			-9476,
			-3320
		},
		{
			144218,
			-5816,
			-4722
		}
	};
	
	public Q508_AClansReputation()
	{
		super(508, "A Clan's Reputation");
		
		registerQuestItems(THEMIS_SCALE, NUCLEUS_OF_HEKATON_PRIME, TIPHON_SHARD, GLAKIS_NUCLEUS, RAHHAS_FANG, NUCLEUS_OF_FLAMESTONE_GIANT);
		
		addStartNpc(SIR_ERIC_RODEMAI);
		addTalkId(SIR_ERIC_RODEMAI);
		
		addKillId(FLAMESTONE_GIANT, PALIBATI_QUEEN_THEMIS, HEKATON_PRIME, GARGOYLE_LORD_TIPHON, LAST_LESSER_GIANT_GLAKI, RAHHA);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (Util.isDigit(event))
		{
			final int evt = Integer.parseInt(event);
			st.set("raid", event);
			htmltext = "30868-" + event + ".htm";
			
			final int x = radar[evt - 1][0];
			final int y = radar[evt - 1][1];
			final int z = radar[evt - 1][2];
			if ((x + y + z) > 0)
			{
				st.addRadar(x, y, z);
			}
			
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30868-7.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		final Clan clan = player.getClan();
		
		switch (st.getState())
		{
			case State.CREATED:
				if (!player.isClanLeader())
				{
					st.exitQuest(true);
					htmltext = "30868-0a.htm";
				}
				else if (clan.getLevel() < 5)
				{
					st.exitQuest(true);
					htmltext = "30868-0b.htm";
				}
				else
				{
					htmltext = "30868-0c.htm";
				}
				break;
			
			case State.STARTED:
				final int raid = st.getInt("raid");
				if (st.getInt("cond") == 1)
				{
					final int item = REWARD_LIST[raid - 1][1];
					final int count = st.getQuestItemsCount(item);
					final int reward = Rnd.get(REWARD_LIST[raid - 1][2], REWARD_LIST[raid - 1][3]);
					if (count == 0)
					{
						htmltext = "30868-" + raid + "a.htm";
					}
					else if (count == 1)
					{
						htmltext = "30868-" + raid + "b.htm";
						st.takeItems(item, 1);
						clan.setReputationScore(clan.getReputationScore() + reward, true);
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE).addNumber(reward));
						clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		// Retrieve the qS of the clan leader.
		final QuestState st = getClanLeaderQuestState(player, npc);
		if ((st == null) || !st.isStarted())
		{
			return null;
		}
		
		// Reward only if quest is setup on good index.
		final int raid = st.getInt("raid");
		if (REWARD_LIST[raid - 1][0] == npc.getNpcId())
		{
			final int item = REWARD_LIST[raid - 1][1];
			if (!st.hasQuestItems(item))
			{
				st.giveItems(item, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		
		return null;
	}
}