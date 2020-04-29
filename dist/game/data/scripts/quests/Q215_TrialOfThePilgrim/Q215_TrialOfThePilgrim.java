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
package quests.Q215_TrialOfThePilgrim;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q215_TrialOfThePilgrim extends Quest
{
	// Items
	private static final int BOOK_OF_SAGE = 2722;
	private static final int VOUCHER_OF_TRIAL = 2723;
	private static final int SPIRIT_OF_FLAME = 2724;
	private static final int ESSENCE_OF_FLAME = 2725;
	private static final int BOOK_OF_GERALD = 2726;
	private static final int GRAY_BADGE = 2727;
	private static final int PICTURE_OF_NAHIR = 2728;
	private static final int HAIR_OF_NAHIR = 2729;
	private static final int STATUE_OF_EINHASAD = 2730;
	private static final int BOOK_OF_DARKNESS = 2731;
	private static final int DEBRIS_OF_WILLOW = 2732;
	private static final int TAG_OF_RUMOR = 2733;
	
	// Rewards
	private static final int MARK_OF_PILGRIM = 2721;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int SANTIAGO = 30648;
	private static final int TANAPI = 30571;
	private static final int ANCESTOR_MARTANKUS = 30649;
	private static final int GAURI_TWINKLEROCK = 30550;
	private static final int DORF = 30651;
	private static final int GERALD = 30650;
	private static final int PRIMOS = 30117;
	private static final int PETRON = 30036;
	private static final int ANDELLIA = 30362;
	private static final int URUHA = 30652;
	private static final int CASIAN = 30612;
	
	// Monsters
	private static final int LAVA_SALAMANDER = 27116;
	private static final int NAHIR = 27117;
	private static final int BLACK_WILLOW = 27118;
	
	public Q215_TrialOfThePilgrim()
	{
		super(215, "Trial of the Pilgrim");
		
		registerQuestItems(BOOK_OF_SAGE, VOUCHER_OF_TRIAL, SPIRIT_OF_FLAME, ESSENCE_OF_FLAME, BOOK_OF_GERALD, GRAY_BADGE, PICTURE_OF_NAHIR, HAIR_OF_NAHIR, STATUE_OF_EINHASAD, BOOK_OF_DARKNESS, DEBRIS_OF_WILLOW, TAG_OF_RUMOR);
		
		addStartNpc(SANTIAGO);
		addTalkId(SANTIAGO, TANAPI, ANCESTOR_MARTANKUS, GAURI_TWINKLEROCK, DORF, GERALD, PRIMOS, PETRON, ANDELLIA, URUHA, CASIAN);
		
		addKillId(LAVA_SALAMANDER, NAHIR, BLACK_WILLOW);
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
		
		if (event.equals("30648-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(VOUCHER_OF_TRIAL, 1);
			
			if (!player.getVariables().getBoolean("secondClassChange35", false))
			{
				htmltext = "30648-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35.get(player.getClassId().getId()));
				player.getVariables().set("secondClassChange35", true);
			}
		}
		else if (event.equals("30649-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ESSENCE_OF_FLAME, 1);
			st.giveItems(SPIRIT_OF_FLAME, 1);
		}
		else if (event.equals("30650-02.htm"))
		{
			if (st.getQuestItemsCount(57) >= 100000)
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(57, 100000);
				st.giveItems(BOOK_OF_GERALD, 1);
			}
			else
			{
				htmltext = "30650-03.htm";
			}
		}
		else if (event.equals("30652-02.htm"))
		{
			st.set("cond", "15");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DEBRIS_OF_WILLOW, 1);
			st.giveItems(BOOK_OF_DARKNESS, 1);
		}
		else if (event.equals("30362-04.htm"))
		{
			st.set("cond", "16");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("30362-05.htm"))
		{
			st.set("cond", "16");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BOOK_OF_DARKNESS, 1);
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
				if ((player.getClassId() != ClassId.CLERIC) && (player.getClassId() != ClassId.ORACLE) && (player.getClassId() != ClassId.SHILLIEN_ORACLE) && (player.getClassId() != ClassId.ORC_SHAMAN))
				{
					htmltext = "30648-02.htm";
				}
				else if (player.getLevel() < 35)
				{
					htmltext = "30648-01.htm";
				}
				else
				{
					htmltext = "30648-03.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case SANTIAGO:
						if (cond < 17)
						{
							htmltext = "30648-09.htm";
						}
						else if (cond == 17)
						{
							htmltext = "30648-10.htm";
							st.takeItems(BOOK_OF_SAGE, 1);
							st.giveItems(MARK_OF_PILGRIM, 1);
							st.rewardExpAndSp(77382, 16000);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case TANAPI:
						if (cond == 1)
						{
							htmltext = "30571-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(VOUCHER_OF_TRIAL, 1);
						}
						else if (cond < 5)
						{
							htmltext = "30571-02.htm";
						}
						else if (cond >= 5)
						{
							htmltext = "30571-03.htm";
							
							if (cond == 5)
							{
								st.set("cond", "6");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						break;
					
					case ANCESTOR_MARTANKUS:
						if (cond == 2)
						{
							htmltext = "30649-01.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 3)
						{
							htmltext = "30649-02.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30649-03.htm";
						}
						break;
					
					case GAURI_TWINKLEROCK:
						if (cond == 6)
						{
							htmltext = "30550-01.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(TAG_OF_RUMOR, 1);
						}
						else if (cond > 6)
						{
							htmltext = "30550-02.htm";
						}
						break;
					
					case DORF:
						if (cond == 7)
						{
							htmltext = (!st.hasQuestItems(BOOK_OF_GERALD)) ? "30651-01.htm" : "30651-02.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TAG_OF_RUMOR, 1);
							st.giveItems(GRAY_BADGE, 1);
						}
						else if (cond > 7)
						{
							htmltext = "30651-03.htm";
						}
						break;
					
					case GERALD:
						if ((cond == 7) && !st.hasQuestItems(BOOK_OF_GERALD))
						{
							htmltext = "30650-01.htm";
						}
						else if ((cond == 8) && st.hasQuestItems(BOOK_OF_GERALD))
						{
							htmltext = "30650-04.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.takeItems(BOOK_OF_GERALD, 1);
							st.giveItems(57, 100000);
						}
						break;
					
					case PRIMOS:
						if (cond == 8)
						{
							htmltext = "30117-01.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond > 8)
						{
							htmltext = "30117-02.htm";
						}
						break;
					
					case PETRON:
						if (cond == 9)
						{
							htmltext = "30036-01.htm";
							st.set("cond", "10");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(PICTURE_OF_NAHIR, 1);
						}
						else if (cond == 10)
						{
							htmltext = "30036-02.htm";
						}
						else if (cond == 11)
						{
							htmltext = "30036-03.htm";
							st.set("cond", "12");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HAIR_OF_NAHIR, 1);
							st.takeItems(PICTURE_OF_NAHIR, 1);
							st.giveItems(STATUE_OF_EINHASAD, 1);
						}
						else if (cond > 11)
						{
							htmltext = "30036-04.htm";
						}
						break;
					
					case ANDELLIA:
						if (cond == 12)
						{
							if (player.getLevel() < 36)
							{
								htmltext = "30362-01a.htm";
							}
							else
							{
								htmltext = "30362-01.htm";
								st.set("cond", "13");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 13)
						{
							htmltext = (Rnd.nextBoolean()) ? "30362-02.htm" : "30362-02a.htm";
						}
						else if (cond == 14)
						{
							htmltext = "30362-07.htm";
						}
						else if (cond == 15)
						{
							htmltext = "30362-03.htm";
						}
						else if (cond == 16)
						{
							htmltext = "30362-06.htm";
						}
						break;
					
					case URUHA:
						if (cond == 14)
						{
							htmltext = "30652-01.htm";
						}
						else if (cond == 15)
						{
							htmltext = "30652-03.htm";
						}
						break;
					
					case CASIAN:
						if (cond == 16)
						{
							htmltext = "30612-01.htm";
							st.set("cond", "17");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BOOK_OF_DARKNESS, 1);
							st.takeItems(GRAY_BADGE, 1);
							st.takeItems(SPIRIT_OF_FLAME, 1);
							st.takeItems(STATUE_OF_EINHASAD, 1);
							st.giveItems(BOOK_OF_SAGE, 1);
						}
						else if (cond == 17)
						{
							htmltext = "30612-02.htm";
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
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getNpcId())
		{
			case LAVA_SALAMANDER:
				if ((st.getInt("cond") == 3) && st.dropItems(ESSENCE_OF_FLAME, 1, 1, 200000))
				{
					st.set("cond", "4");
				}
				break;
			
			case NAHIR:
				if ((st.getInt("cond") == 10) && st.dropItems(HAIR_OF_NAHIR, 1, 1, 200000))
				{
					st.set("cond", "11");
				}
				break;
			
			case BLACK_WILLOW:
				if ((st.getInt("cond") == 13) && st.dropItems(DEBRIS_OF_WILLOW, 1, 1, 200000))
				{
					st.set("cond", "14");
				}
				break;
		}
		
		return null;
	}
}