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
package quests.Q035_FindGlitteringJewelry;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q037_MakeFormalWear.Q037_MakeFormalWear;

public class Q035_FindGlitteringJewelry extends Quest
{
	// NPCs
	private static final int ELLIE = 30091;
	private static final int FELTON = 30879;
	
	// Items
	private static final int ROUGH_JEWEL = 7162;
	private static final int ORIHARUKON = 1893;
	private static final int SILVER_NUGGET = 1873;
	private static final int THONS = 4044;
	
	// Reward
	private static final int JEWEL_BOX = 7077;
	
	public Q035_FindGlitteringJewelry()
	{
		super(35, "Find Glittering Jewelry");
		
		registerQuestItems(ROUGH_JEWEL);
		
		addStartNpc(ELLIE);
		addTalkId(ELLIE, FELTON);
		
		addKillId(20135); // Alligator
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
		
		if (event.equals("30091-1.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30879-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30091-3.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ROUGH_JEWEL, 10);
		}
		else if (event.equals("30091-5.htm"))
		{
			if ((st.getQuestItemsCount(ORIHARUKON) >= 5) && (st.getQuestItemsCount(SILVER_NUGGET) >= 500) && (st.getQuestItemsCount(THONS) >= 150))
			{
				st.takeItems(ORIHARUKON, 5);
				st.takeItems(SILVER_NUGGET, 500);
				st.takeItems(THONS, 150);
				st.giveItems(JEWEL_BOX, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
			else
			{
				htmltext = "30091-4a.htm";
			}
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 60)
				{
					final QuestState fwear = player.getQuestState(Q037_MakeFormalWear.class.getSimpleName());
					if ((fwear != null) && (fwear.getInt("cond") == 6))
					{
						htmltext = "30091-0.htm";
					}
					else
					{
						htmltext = "30091-0a.htm";
					}
				}
				else
				{
					htmltext = "30091-0b.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ELLIE:
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "30091-1a.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30091-2.htm";
						}
						else if (cond == 4)
						{
							htmltext = ((st.getQuestItemsCount(ORIHARUKON) >= 5) && (st.getQuestItemsCount(SILVER_NUGGET) >= 500) && (st.getQuestItemsCount(THONS) >= 150)) ? "30091-4.htm" : "30091-4a.htm";
						}
						break;
					
					case FELTON:
						if (cond == 1)
						{
							htmltext = "30879-0.htm";
						}
						else if (cond > 1)
						{
							htmltext = "30879-1a.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerCondition(player, npc, "cond", "2");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(ROUGH_JEWEL, 1, 10, 500000))
		{
			st.set("cond", "3");
		}
		
		return null;
	}
}