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
package quests.Q367_ElectrifyingRecharge;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q367_ElectrifyingRecharge extends Quest
{
	// NPCs
	private static final int LORAIN = 30673;
	
	// Item
	private static final int LORAIN_LAMP = 5875;
	private static final int TITAN_LAMP_1 = 5876;
	private static final int TITAN_LAMP_2 = 5877;
	private static final int TITAN_LAMP_3 = 5878;
	private static final int TITAN_LAMP_4 = 5879;
	private static final int TITAN_LAMP_5 = 5880;
	
	// Reward
	private static final int[] REWARD =
	{
		4553,
		4554,
		4555,
		4556,
		4557,
		4558,
		4559,
		4560,
		4561,
		4562,
		4563,
		4564
	};
	
	// Mobs
	private static final int CATHEROK = 21035;
	
	public Q367_ElectrifyingRecharge()
	{
		super(367, "Electrifying Recharge!");
		
		registerQuestItems(LORAIN_LAMP, TITAN_LAMP_1, TITAN_LAMP_2, TITAN_LAMP_3, TITAN_LAMP_4, TITAN_LAMP_5);
		
		addStartNpc(LORAIN);
		addTalkId(LORAIN);
		
		addSpellFinishedId(CATHEROK);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equals("30673-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAIN_LAMP, 1);
		}
		else if (event.equals("30673-09.htm"))
		{
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAIN_LAMP, 1);
		}
		else if (event.equals("30673-08.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equals("30673-07.htm"))
		{
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAIN_LAMP, 1);
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
				htmltext = (player.getLevel() < 37) ? "30673-02.htm" : "30673-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.hasQuestItems(5880))
					{
						htmltext = "30673-05.htm";
						st.playSound(QuestState.SOUND_ACCEPT);
						st.takeItems(5880, 1);
						st.giveItems(LORAIN_LAMP, 1);
					}
					else if (st.hasQuestItems(5876))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5876, 1);
					}
					else if (st.hasQuestItems(5877))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5877, 1);
					}
					else if (st.hasQuestItems(5878))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5878, 1);
					}
					else
					{
						htmltext = "30673-03.htm";
					}
				}
				else if ((cond == 2) && st.hasQuestItems(5879))
				{
					htmltext = "30673-06.htm";
					st.takeItems(5879, 1);
					st.rewardItems(REWARD[Rnd.get(REWARD.length)], 1);
					st.playSound(QuestState.SOUND_FINISH);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onSpellFinished(NpcInstance npc, PlayerInstance player, Skill skill)
	{
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		if (skill.getId() == 4072)
		{
			if (st.hasQuestItems(LORAIN_LAMP))
			{
				final int randomItem = Rnd.get(5876, 5880);
				st.takeItems(LORAIN_LAMP, 1);
				st.giveItems(randomItem, 1);
				if (randomItem == 5879)
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
				{
					st.playSound(QuestState.SOUND_ITEMGET);
				}
			}
		}
		
		return null;
	}
}