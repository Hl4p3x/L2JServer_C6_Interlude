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
package quests.Q259_RanchersPlea;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q259_RanchersPlea extends Quest
{
	// NPCs
	private static final int EDMOND = 30497;
	private static final int MARIUS = 30405;
	
	// Monsters
	private static final int GIANT_SPIDER = 20103;
	private static final int TALON_SPIDER = 20106;
	private static final int BLADE_SPIDER = 20108;
	
	// Items
	private static final int GIANT_SPIDER_SKIN = 1495;
	
	// Rewards
	private static final int ADENA = 57;
	private static final int HEALING_POTION = 1061;
	private static final int WOODEN_ARROW = 17;
	
	public Q259_RanchersPlea()
	{
		super(259, "Rancher's Plea");
		
		registerQuestItems(GIANT_SPIDER_SKIN);
		
		addStartNpc(EDMOND);
		addTalkId(EDMOND, MARIUS);
		
		addKillId(GIANT_SPIDER, TALON_SPIDER, BLADE_SPIDER);
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
		
		if (event.equals("30497-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30497-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equals("30405-04.htm"))
		{
			if (st.getQuestItemsCount(GIANT_SPIDER_SKIN) >= 10)
			{
				st.takeItems(GIANT_SPIDER_SKIN, 10);
				st.rewardItems(HEALING_POTION, 1);
			}
			else
			{
				htmltext = "<html><body>Incorrect item count</body></html>";
			}
		}
		else if (event.equals("30405-05.htm"))
		{
			if (st.getQuestItemsCount(GIANT_SPIDER_SKIN) >= 10)
			{
				st.takeItems(GIANT_SPIDER_SKIN, 10);
				st.rewardItems(WOODEN_ARROW, 50);
			}
			else
			{
				htmltext = "<html><body>Incorrect item count</body></html>";
			}
		}
		else if (event.equals("30405-07.htm"))
		{
			if (st.getQuestItemsCount(GIANT_SPIDER_SKIN) >= 10)
			{
				htmltext = "30405-06.htm";
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
				htmltext = (player.getLevel() < 15) ? "30497-01.htm" : "30497-02.htm";
				break;
			
			case State.STARTED:
				final int count = st.getQuestItemsCount(GIANT_SPIDER_SKIN);
				switch (npc.getNpcId())
				{
					case EDMOND:
						if (count == 0)
						{
							htmltext = "30497-04.htm";
						}
						else
						{
							htmltext = "30497-05.htm";
							st.takeItems(GIANT_SPIDER_SKIN, -1);
							st.rewardItems(ADENA, ((count >= 10) ? 250 : 0) + (count * 25));
						}
						break;
					
					case MARIUS:
						htmltext = (count < 10) ? "30405-01.htm" : "30405-02.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		st.dropItemsAlways(GIANT_SPIDER_SKIN, 1, 0);
		
		return null;
	}
}