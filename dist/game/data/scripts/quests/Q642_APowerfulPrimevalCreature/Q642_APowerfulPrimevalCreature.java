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
package quests.Q642_APowerfulPrimevalCreature;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q642_APowerfulPrimevalCreature extends Quest
{
	// Items
	private static final int DINOSAUR_TISSUE = 8774;
	private static final int DINOSAUR_EGG = 8775;
	
	private static final int ANCIENT_EGG = 18344;
	
	// Rewards
	private static final int[] REWARDS =
	{
		8690,
		8692,
		8694,
		8696,
		8698,
		8700,
		8702,
		8704,
		8706,
		8708,
		8710
	};
	
	public Q642_APowerfulPrimevalCreature()
	{
		super(642, "A Powerful Primeval Creature");
		
		registerQuestItems(DINOSAUR_TISSUE, DINOSAUR_EGG);
		
		addStartNpc(32105); // Dinn
		addTalkId(32105);
		
		// Dinosaurs + egg
		addKillId(22196, 22197, 22198, 22199, 22200, 22201, 22202, 22203, 22204, 22205, 22218, 22219, 22220, 22223, 22224, 22225, ANCIENT_EGG);
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
		
		if (event.equals("32105-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("32105-08.htm"))
		{
			if ((st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150) && st.hasQuestItems(DINOSAUR_EGG))
			{
				htmltext = "32105-06.htm";
			}
		}
		else if (event.equals("32105-07.htm"))
		{
			final int tissues = st.getQuestItemsCount(DINOSAUR_TISSUE);
			if (tissues > 0)
			{
				st.takeItems(DINOSAUR_TISSUE, -1);
				st.rewardItems(57, tissues * 5000);
			}
			else
			{
				htmltext = "32105-08.htm";
			}
		}
		else if (event.contains("event_"))
		{
			if ((st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150) && st.hasQuestItems(DINOSAUR_EGG))
			{
				htmltext = "32105-07.htm";
				st.takeItems(DINOSAUR_TISSUE, 150);
				st.takeItems(DINOSAUR_EGG, 1);
				st.rewardItems(57, 44000);
				st.giveItems(REWARDS[Integer.parseInt(event.split("_")[1])], 1);
			}
			else
			{
				htmltext = "32105-08.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() < 75) ? "32105-00.htm" : "32105-01.htm";
				break;
			
			case State.STARTED:
				htmltext = (!st.hasQuestItems(DINOSAUR_TISSUE)) ? "32105-08.htm" : "32105-05.htm";
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
		
		if (npc.getNpcId() == ANCIENT_EGG)
		{
			if (Rnd.get(100) < 1)
			{
				st.giveItems(DINOSAUR_EGG, 1);
				if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150)
				{
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
				{
					st.playSound(QuestState.SOUND_ITEMGET);
				}
			}
		}
		else if (Rnd.get(100) < 33)
		{
			st.rewardItems(DINOSAUR_TISSUE, 1);
			if ((st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150) && st.hasQuestItems(DINOSAUR_EGG))
			{
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
			{
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		
		return null;
	}
}