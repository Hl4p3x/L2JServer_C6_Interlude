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
package quests.Q381_LetsBecomeARoyalMember;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q381_LetsBecomeARoyalMember extends Quest
{
	// NPCs
	private static final int SORINT = 30232;
	private static final int SANDRA = 30090;
	
	// Items
	private static final int KAIL_COIN = 5899;
	private static final int COIN_ALBUM = 5900;
	private static final int GOLDEN_CLOVER_COIN = 7569;
	private static final int COIN_COLLECTOR_MEMBERSHIP = 3813;
	
	// Reward
	private static final int ROYAL_MEMBERSHIP = 5898;
	
	public Q381_LetsBecomeARoyalMember()
	{
		super(381, "Let's Become a Royal Member!");
		
		registerQuestItems(KAIL_COIN, GOLDEN_CLOVER_COIN);
		
		addStartNpc(SORINT);
		addTalkId(SORINT, SANDRA);
		
		addKillId(21018, 27316);
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
		
		if (event.equals("30090-02.htm"))
		{
			st.set("aCond", "1"); // Alternative cond used for Sandra.
		}
		else if (event.equals("30232-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = ((player.getLevel() < 55) || !st.hasQuestItems(COIN_COLLECTOR_MEMBERSHIP)) ? "30232-02.htm" : "30232-01.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case SORINT:
						if (!st.hasQuestItems(KAIL_COIN))
						{
							htmltext = "30232-04.htm";
						}
						else if (!st.hasQuestItems(COIN_ALBUM))
						{
							htmltext = "30232-05.htm";
						}
						else
						{
							htmltext = "30232-06.htm";
							st.takeItems(KAIL_COIN, -1);
							st.takeItems(COIN_ALBUM, -1);
							st.giveItems(ROYAL_MEMBERSHIP, 1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case SANDRA:
						if (!st.hasQuestItems(COIN_ALBUM))
						{
							if (st.getInt("aCond") == 0)
							{
								htmltext = "30090-01.htm";
							}
							else
							{
								if (!st.hasQuestItems(GOLDEN_CLOVER_COIN))
								{
									htmltext = "30090-03.htm";
								}
								else
								{
									htmltext = "30090-04.htm";
									st.takeItems(GOLDEN_CLOVER_COIN, -1);
									st.giveItems(COIN_ALBUM, 1);
								}
							}
						}
						else
						{
							htmltext = "30090-05.htm";
						}
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
		
		if (npc.getNpcId() == 21018)
		{
			st.dropItems(KAIL_COIN, 1, 1, 50000);
		}
		else if (st.getInt("aCond") == 1)
		{
			st.dropItemsAlways(GOLDEN_CLOVER_COIN, 1, 1);
		}
		
		return null;
	}
}