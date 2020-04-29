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
package quests.Q034_InSearchOfCloth;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q037_MakeFormalWear.Q037_MakeFormalWear;

public class Q034_InSearchOfCloth extends Quest
{
	// NPCs
	private static final int RADIA = 30088;
	private static final int RALFORD = 30165;
	private static final int VARAN = 30294;
	
	// Monsters
	private static final int TRISALIM_SPIDER = 20560;
	private static final int TRISALIM_TARANTULA = 20561;
	
	// Items
	private static final int SPINNERET = 7528;
	private static final int SUEDE = 1866;
	private static final int THREAD = 1868;
	private static final int SPIDERSILK = 7161;
	
	// Rewards
	private static final int MYSTERIOUS_CLOTH = 7076;
	
	public Q034_InSearchOfCloth()
	{
		super(34, "In Search of Cloth");
		
		registerQuestItems(SPINNERET, SPIDERSILK);
		
		addStartNpc(RADIA);
		addTalkId(RADIA, RALFORD, VARAN);
		
		addKillId(TRISALIM_SPIDER, TRISALIM_TARANTULA);
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
		
		if (event.equals("30088-1.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30294-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30088-3.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30165-1.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30165-3.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SPINNERET, 10);
			st.giveItems(SPIDERSILK, 1);
		}
		else if (event.equals("30088-5.htm"))
		{
			if ((st.getQuestItemsCount(SUEDE) >= 3000) && (st.getQuestItemsCount(THREAD) >= 5000) && st.hasQuestItems(SPIDERSILK))
			{
				st.takeItems(SPIDERSILK, 1);
				st.takeItems(SUEDE, 3000);
				st.takeItems(THREAD, 5000);
				st.giveItems(MYSTERIOUS_CLOTH, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
			else
			{
				htmltext = "30088-4a.htm";
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
						htmltext = "30088-0.htm";
					}
					else
					{
						htmltext = "30088-0a.htm";
					}
				}
				else
				{
					htmltext = "30088-0b.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case RADIA:
						if (cond == 1)
						{
							htmltext = "30088-1a.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30088-2.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30088-3a.htm";
						}
						else if (cond == 6)
						{
							if ((st.getQuestItemsCount(SUEDE) < 3000) || (st.getQuestItemsCount(THREAD) < 5000) || !st.hasQuestItems(SPIDERSILK))
							{
								htmltext = "30088-4a.htm";
							}
							else
							{
								htmltext = "30088-4.htm";
							}
						}
						break;
					
					case VARAN:
						if (cond == 1)
						{
							htmltext = "30294-0.htm";
						}
						else if (cond > 1)
						{
							htmltext = "30294-1a.htm";
						}
						break;
					
					case RALFORD:
						if (cond == 3)
						{
							htmltext = "30165-0.htm";
						}
						else if ((cond == 4) && (st.getQuestItemsCount(SPINNERET) < 10))
						{
							htmltext = "30165-1a.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30165-2.htm";
						}
						else if (cond > 5)
						{
							htmltext = "30165-3a.htm";
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
		final QuestState st = checkPlayerCondition(player, npc, "cond", "4");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(SPINNERET, 1, 10, 500000))
		{
			st.set("cond", "5");
		}
		
		return null;
	}
}