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
package quests.Q641_AttackSailren;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

import quests.Q126_TheNameOfEvil_2.Q126_TheNameOfEvil_2;

public class Q641_AttackSailren extends Quest
{
	// NPCs
	private static final int STATUE = 32109;
	
	// Quest Item
	private static final int GAZKH_FRAGMENT = 8782;
	private static final int GAZKH = 8784;
	
	public Q641_AttackSailren()
	{
		super(641, "Attack Sailren!");
		
		registerQuestItems(GAZKH_FRAGMENT);
		
		addStartNpc(STATUE);
		addTalkId(STATUE);
		
		addKillId(22196, 22197, 22198, 22199, 22218, 22223);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (event.equals("32109-5.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("32109-8.htm"))
		{
			if (st.getQuestItemsCount(GAZKH_FRAGMENT) >= 30)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, player, 5089, 1, 3000, 0));
				st.takeItems(GAZKH_FRAGMENT, -1);
				st.giveItems(GAZKH, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "32109-6.htm";
				st.set("cond", "1");
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
				if (player.getLevel() < 77)
				{
					htmltext = "32109-3.htm";
				}
				else
				{
					final QuestState st2 = player.getQuestState(Q126_TheNameOfEvil_2.class.getSimpleName());
					htmltext = ((st2 != null) && st2.isCompleted()) ? "32109-1.htm" : "32109-2.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "32109-5.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32109-7.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMember(player, npc, "cond", "1");
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(GAZKH_FRAGMENT, 1, 30, 50000))
		{
			st.set("cond", "2");
		}
		
		return null;
	}
}