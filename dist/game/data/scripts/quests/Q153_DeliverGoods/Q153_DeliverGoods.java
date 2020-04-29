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
package quests.Q153_DeliverGoods;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q153_DeliverGoods extends Quest
{
	// NPCs
	private static final int JACKSON = 30002;
	private static final int SILVIA = 30003;
	private static final int ARNOLD = 30041;
	private static final int RANT = 30054;
	
	// Items
	private static final int DELIVERY_LIST = 1012;
	private static final int HEAVY_WOOD_BOX = 1013;
	private static final int CLOTH_BUNDLE = 1014;
	private static final int CLAY_POT = 1015;
	private static final int JACKSON_RECEIPT = 1016;
	private static final int SILVIA_RECEIPT = 1017;
	private static final int RANT_RECEIPT = 1018;
	
	// Rewards
	private static final int SOULSHOT_NO_GRADE = 1835;
	private static final int RING_OF_KNOWLEDGE = 875;
	
	public Q153_DeliverGoods()
	{
		super(153, "Deliver Goods");
		
		registerQuestItems(DELIVERY_LIST, HEAVY_WOOD_BOX, CLOTH_BUNDLE, CLAY_POT, JACKSON_RECEIPT, SILVIA_RECEIPT, RANT_RECEIPT);
		
		addStartNpc(ARNOLD);
		addTalkId(JACKSON, SILVIA, ARNOLD, RANT);
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
		
		if (event.equals("30041-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(DELIVERY_LIST, 1);
			st.giveItems(CLAY_POT, 1);
			st.giveItems(CLOTH_BUNDLE, 1);
			st.giveItems(HEAVY_WOOD_BOX, 1);
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
				htmltext = (player.getLevel() < 2) ? "30041-00.htm" : "30041-01.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case ARNOLD:
						if (st.getInt("cond") == 1)
						{
							htmltext = "30041-03.htm";
						}
						else if (st.getInt("cond") == 2)
						{
							htmltext = "30041-04.htm";
							st.takeItems(DELIVERY_LIST, 1);
							st.takeItems(JACKSON_RECEIPT, 1);
							st.takeItems(SILVIA_RECEIPT, 1);
							st.takeItems(RANT_RECEIPT, 1);
							st.giveItems(RING_OF_KNOWLEDGE, 1);
							st.giveItems(RING_OF_KNOWLEDGE, 1);
							st.rewardExpAndSp(600, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case JACKSON:
						if (st.hasQuestItems(HEAVY_WOOD_BOX))
						{
							htmltext = "30002-01.htm";
							st.takeItems(HEAVY_WOOD_BOX, 1);
							st.giveItems(JACKSON_RECEIPT, 1);
							if (st.hasQuestItems(SILVIA_RECEIPT, RANT_RECEIPT))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else
						{
							htmltext = "30002-02.htm";
						}
						break;
					
					case SILVIA:
						if (st.hasQuestItems(CLOTH_BUNDLE))
						{
							htmltext = "30003-01.htm";
							st.takeItems(CLOTH_BUNDLE, 1);
							st.giveItems(SILVIA_RECEIPT, 1);
							st.giveItems(SOULSHOT_NO_GRADE, 3);
							if (st.hasQuestItems(JACKSON_RECEIPT, RANT_RECEIPT))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else
						{
							htmltext = "30003-02.htm";
						}
						break;
					
					case RANT:
						if (st.hasQuestItems(CLAY_POT))
						{
							htmltext = "30054-01.htm";
							st.takeItems(CLAY_POT, 1);
							st.giveItems(RANT_RECEIPT, 1);
							if (st.hasQuestItems(JACKSON_RECEIPT, SILVIA_RECEIPT))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else
						{
							htmltext = "30054-02.htm";
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
}