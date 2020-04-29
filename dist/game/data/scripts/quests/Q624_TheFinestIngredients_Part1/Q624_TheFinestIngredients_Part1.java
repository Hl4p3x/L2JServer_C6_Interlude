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
package quests.Q624_TheFinestIngredients_Part1;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q624_TheFinestIngredients_Part1 extends Quest
{
	// Mobs
	private static final int NEPENTHES = 21319;
	private static final int ATROX = 21321;
	private static final int ATROXSPAWN = 21317;
	private static final int BANDERSNATCH = 21314;
	
	// Items
	private static final int TRUNK_OF_NEPENTHES = 7202;
	private static final int FOOT_OF_BANDERSNATCHLING = 7203;
	private static final int SECRET_SPICE = 7204;
	
	// Rewards
	private static final int ICE_CRYSTAL = 7080;
	private static final int SOY_SAUCE_JAR = 7205;
	
	public Q624_TheFinestIngredients_Part1()
	{
		super(624, "The Finest Ingredients - Part 1");
		
		registerQuestItems(TRUNK_OF_NEPENTHES, FOOT_OF_BANDERSNATCHLING, SECRET_SPICE);
		
		addStartNpc(31521); // Jeremy
		addTalkId(31521);
		
		addKillId(NEPENTHES, ATROX, ATROXSPAWN, BANDERSNATCH);
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
		
		if (event.equals("31521-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31521-05.htm"))
		{
			if ((st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50) && (st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50) && (st.getQuestItemsCount(SECRET_SPICE) >= 50))
			{
				st.takeItems(TRUNK_OF_NEPENTHES, -1);
				st.takeItems(FOOT_OF_BANDERSNATCHLING, -1);
				st.takeItems(SECRET_SPICE, -1);
				st.giveItems(ICE_CRYSTAL, 1);
				st.giveItems(SOY_SAUCE_JAR, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				st.set("cond", "1");
				htmltext = "31521-07.htm";
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
				htmltext = (player.getLevel() < 73) ? "31521-03.htm" : "31521-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "31521-06.htm";
				}
				else if (cond == 2)
				{
					if ((st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50) && (st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50) && (st.getQuestItemsCount(SECRET_SPICE) >= 50))
					{
						htmltext = "31521-04.htm";
					}
					else
					{
						htmltext = "31521-07.htm";
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMember(player, npc, "1");
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case NEPENTHES:
				if (st.dropItemsAlways(TRUNK_OF_NEPENTHES, 1, 50) && (st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50) && (st.getQuestItemsCount(SECRET_SPICE) >= 50))
				{
					st.set("cond", "2");
				}
				break;
			
			case ATROX:
			case ATROXSPAWN:
				if (st.dropItemsAlways(SECRET_SPICE, 1, 50) && (st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50) && (st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50))
				{
					st.set("cond", "2");
				}
				break;
			
			case BANDERSNATCH:
				if (st.dropItemsAlways(FOOT_OF_BANDERSNATCHLING, 1, 50) && (st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50) && (st.getQuestItemsCount(SECRET_SPICE) >= 50))
				{
					st.set("cond", "2");
				}
				break;
		}
		
		return null;
	}
}