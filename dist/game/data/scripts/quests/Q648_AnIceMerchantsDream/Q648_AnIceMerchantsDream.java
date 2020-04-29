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
package quests.Q648_AnIceMerchantsDream;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q115_TheOtherSideOfTruth.Q115_TheOtherSideOfTruth;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q648_AnIceMerchantsDream extends Quest
{
	// NPCs
	private static final int RAFFORTY = 32020;
	private static final int ICE_SHELF = 32023;
	private static final int[] MONSTERS =
	{
		22080,
		22081,
		22082,
		22083,
		22084,
		22085,
		22086,
		22087,
		22088,
		22089,
		22090,
		22091,
		22092,
		22093,
		22094,
		22095,
		22096,
		22097,
		22098,
		22099
	};
	// Items
	private static final int HEMOCYTE = 8057;
	private static final int SILVER_ICE = 8077;
	private static final int BLACK_ICE = 8078;
	
	public Q648_AnIceMerchantsDream()
	{
		super(648, "An Ice Merchant's Dream");
		
		addStartNpc(RAFFORTY);
		addStartNpc(ICE_SHELF);
		addTalkId(RAFFORTY, ICE_SHELF);
		addKillId(MONSTERS);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "32020-02.htm":
			{
				qs.setState(State.STARTED);
				qs.playSound("ItemSound.quest_accept");
				qs.set("cond", "1");
				break;
			}
			case "32020-07.htm":
			{
				final int silver = qs.getQuestItemsCount(SILVER_ICE);
				final int black = qs.getQuestItemsCount(BLACK_ICE);
				final int r1 = silver * 300;
				final int r2 = black * 1200;
				qs.giveItems(57, r1 + r2);
				qs.takeItems(SILVER_ICE, silver);
				qs.takeItems(BLACK_ICE, black);
				break;
			}
			case "32020-09.htm":
			{
				qs.exitQuest(true);
				qs.playSound("ItemSound.quest_finish");
				break;
			}
			case "32023-04.htm":
			{
				qs.playSound("ItemSound2.broken_key");
				qs.takeItems(SILVER_ICE, 1);
				break;
			}
			case "32023-05.htm":
			{
				if (Rnd.get(100) <= 25)
				{
					qs.giveItems(BLACK_ICE, 1);
					qs.playSound("ItemSound3.sys_enchant_sucess");
				}
				else
				{
					htmltext = "32023-06.htm";
					qs.playSound("ItemSound3.sys_enchant_failed");
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		final int npcId = npc.getNpcId();
		final int id = qs.getState();
		final int cond = qs.getInt("cond");
		final int silver = qs.getQuestItemsCount(SILVER_ICE);
		final int black = qs.getQuestItemsCount(BLACK_ICE);
		if (npcId == RAFFORTY)
		{
			if (id == State.CREATED)
			{
				if (player.getLevel() >= 53)
				{
					htmltext = "32020-01.htm";
				}
				else
				{
					htmltext = "32020-00.htm";
					qs.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				if ((silver > 0) || (black > 0))
				{
					final QuestState st2 = player.getQuestState(Q115_TheOtherSideOfTruth.class.getSimpleName());
					htmltext = "32020-05.htm";
					if ((st2 != null) && (st2.getState() == State.COMPLETED))
					{
						htmltext = "32020-10.htm";
						qs.playSound("ItemSound.quest_middle");
						qs.set("cond", "2");
					}
				}
				else
				{
					htmltext = "32020-04.htm";
				}
			}
			else if (cond == 2)
			{
				if ((silver > 0) || (black > 0))
				{
					htmltext = "32020-10.htm";
				}
				else
				{
					htmltext = "32020-04a.htm";
				}
			}
		}
		else if (npcId == ICE_SHELF)
		{
			if (id == State.CREATED)
			{
				htmltext = "32023-00.htm";
			}
			else
			{
				if (silver > 0)
				{
					htmltext = "32023-02.htm";
				}
				else
				{
					htmltext = "32023-01.htm";
				}
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState qs = partyMember.getQuestState(getName());
		if (qs != null)
		{
			int chance = (int) ((npc.getNpcId() - 22050) * Config.RATE_DROP_QUEST);
			chance /= 100;
			int numItems = chance;
			int random = Rnd.get(100);
			if (random <= chance)
			{
				numItems += 1;
			}
			if (numItems != 0)
			{
				qs.giveItems(SILVER_ICE, numItems);
				qs.playSound("ItemSound.quest_itemget");
			}
			
			final int cond = qs.getInt("cond");
			random = Rnd.get(100);
			if ((cond == 2) && (random <= 10))
			{
				qs.giveItems(HEMOCYTE, 1);
				qs.playSound("ItemSound.quest_itemget");
			}
		}
		
		return null;
	}
}