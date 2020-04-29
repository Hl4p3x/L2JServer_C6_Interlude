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
package quests.Q405_PathToACleric;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q405_PathToACleric extends Quest
{
	// Items
	private static final int LETTER_OF_ORDER_1 = 1191;
	private static final int LETTER_OF_ORDER_2 = 1192;
	private static final int LIONEL_BOOK = 1193;
	private static final int BOOK_OF_VIVYAN = 1194;
	private static final int BOOK_OF_SIMPLON = 1195;
	private static final int BOOK_OF_PRAGA = 1196;
	private static final int CERTIFICATE_OF_GALLINT = 1197;
	private static final int PENDANT_OF_MOTHER = 1198;
	private static final int NECKLACE_OF_MOTHER = 1199;
	private static final int LIONEL_COVENANT = 1200;
	
	// NPCs
	private static final int GALLINT = 30017;
	private static final int ZIGAUNT = 30022;
	private static final int VIVYAN = 30030;
	private static final int PRAGA = 30333;
	private static final int SIMPLON = 30253;
	private static final int LIONEL = 30408;
	
	// Reward
	private static final int MARK_OF_FATE = 1201;
	
	public Q405_PathToACleric()
	{
		super(405, "Path to a Cleric");
		
		registerQuestItems(LETTER_OF_ORDER_1, BOOK_OF_SIMPLON, BOOK_OF_PRAGA, BOOK_OF_VIVYAN, NECKLACE_OF_MOTHER, PENDANT_OF_MOTHER, LETTER_OF_ORDER_2, LIONEL_BOOK, CERTIFICATE_OF_GALLINT, LIONEL_COVENANT);
		
		addStartNpc(ZIGAUNT);
		addTalkId(ZIGAUNT, SIMPLON, PRAGA, VIVYAN, LIONEL, GALLINT);
		
		addKillId(20029, 20026);
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
		
		if (event.equals("30022-05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LETTER_OF_ORDER_1, 1);
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
				if (player.getClassId() != ClassId.MAGE)
				{
					htmltext = (player.getClassId() == ClassId.CLERIC) ? "30022-02a.htm" : "30022-02.htm";
				}
				else if (player.getLevel() < 19)
				{
					htmltext = "30022-03.htm";
				}
				else if (st.hasQuestItems(MARK_OF_FATE))
				{
					htmltext = "30022-04.htm";
				}
				else
				{
					htmltext = "30022-01.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ZIGAUNT:
						if (cond == 1)
						{
							htmltext = "30022-06.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30022-08.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BOOK_OF_PRAGA, 1);
							st.takeItems(BOOK_OF_VIVYAN, 1);
							st.takeItems(BOOK_OF_SIMPLON, 3);
							st.takeItems(LETTER_OF_ORDER_1, 1);
							st.giveItems(LETTER_OF_ORDER_2, 1);
						}
						else if ((cond > 2) && (cond < 6))
						{
							htmltext = "30022-07.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30022-09.htm";
							st.takeItems(LETTER_OF_ORDER_2, 1);
							st.takeItems(LIONEL_COVENANT, 1);
							st.giveItems(MARK_OF_FATE, 1);
							st.rewardExpAndSp(3200, 5610);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case SIMPLON:
						if ((cond == 1) && !st.hasQuestItems(BOOK_OF_SIMPLON))
						{
							htmltext = "30253-01.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.giveItems(BOOK_OF_SIMPLON, 3);
						}
						else if ((cond > 1) || st.hasQuestItems(BOOK_OF_SIMPLON))
						{
							htmltext = "30253-02.htm";
						}
						break;
					
					case PRAGA:
						if (cond == 1)
						{
							if (!st.hasQuestItems(BOOK_OF_PRAGA) && !st.hasQuestItems(NECKLACE_OF_MOTHER) && st.hasQuestItems(BOOK_OF_SIMPLON))
							{
								htmltext = "30333-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(NECKLACE_OF_MOTHER, 1);
							}
							else if (!st.hasQuestItems(PENDANT_OF_MOTHER))
							{
								htmltext = "30333-02.htm";
							}
							else if (st.hasQuestItems(PENDANT_OF_MOTHER))
							{
								htmltext = "30333-03.htm";
								st.takeItems(NECKLACE_OF_MOTHER, 1);
								st.takeItems(PENDANT_OF_MOTHER, 1);
								st.giveItems(BOOK_OF_PRAGA, 1);
								
								if (st.hasQuestItems(BOOK_OF_VIVYAN))
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
						else if ((cond > 1) || (st.hasQuestItems(BOOK_OF_PRAGA)))
						{
							htmltext = "30333-04.htm";
						}
						break;
					
					case VIVYAN:
						if ((cond == 1) && !st.hasQuestItems(BOOK_OF_VIVYAN) && st.hasQuestItems(BOOK_OF_SIMPLON))
						{
							htmltext = "30030-01.htm";
							st.giveItems(BOOK_OF_VIVYAN, 1);
							
							if (st.hasQuestItems(BOOK_OF_PRAGA))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if ((cond > 1) || st.hasQuestItems(BOOK_OF_VIVYAN))
						{
							htmltext = "30030-02.htm";
						}
						break;
					
					case LIONEL:
						if (cond < 3)
						{
							htmltext = "30408-02.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30408-01.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(LIONEL_BOOK, 1);
						}
						else if (cond == 4)
						{
							htmltext = "30408-03.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30408-04.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CERTIFICATE_OF_GALLINT, 1);
							st.giveItems(LIONEL_COVENANT, 1);
						}
						else if (cond == 6)
						{
							htmltext = "30408-05.htm";
						}
						break;
					
					case GALLINT:
						if (cond == 4)
						{
							htmltext = "30017-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LIONEL_BOOK, 1);
							st.giveItems(CERTIFICATE_OF_GALLINT, 1);
						}
						else if (cond > 4)
						{
							htmltext = "30017-02.htm";
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
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		if (st.hasQuestItems(NECKLACE_OF_MOTHER) && !st.hasQuestItems(PENDANT_OF_MOTHER))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(PENDANT_OF_MOTHER, 1);
		}
		
		return null;
	}
}