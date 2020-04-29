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
package quests.Q103_SpiritOfCraftsman;

import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q103_SpiritOfCraftsman extends Quest
{
	// Items
	private static final int KARROD_LETTER = 968;
	private static final int CECKTINON_VOUCHER_1 = 969;
	private static final int CECKTINON_VOUCHER_2 = 970;
	private static final int SOUL_CATCHER = 971;
	private static final int PRESERVING_OIL = 972;
	private static final int ZOMBIE_HEAD = 973;
	private static final int STEELBENDER_HEAD = 974;
	private static final int BONE_FRAGMENT = 1107;
	
	// Rewards
	private static final int SPIRITSHOT_NO_GRADE = 2509;
	private static final int SOULSHOT_NO_GRADE = 1835;
	private static final int BLOODSABER = 975;
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	private static final int LESSER_HEALING_POT = 1060;
	private static final int ECHO_BATTLE = 4412;
	private static final int ECHO_LOVE = 4413;
	private static final int ECHO_SOLITUDE = 4414;
	private static final int ECHO_FEAST = 4415;
	private static final int ECHO_CELEBRATION = 4416;
	
	// NPCs
	private static final int KARROD = 30307;
	private static final int CECKTINON = 30132;
	private static final int HARNE = 30144;
	
	public Q103_SpiritOfCraftsman()
	{
		super(103, "Spirit of Craftsman");
		
		registerQuestItems(KARROD_LETTER, CECKTINON_VOUCHER_1, CECKTINON_VOUCHER_2, BONE_FRAGMENT, SOUL_CATCHER, PRESERVING_OIL, ZOMBIE_HEAD, STEELBENDER_HEAD);
		
		addStartNpc(KARROD);
		addTalkId(KARROD, CECKTINON, HARNE);
		
		addKillId(20015, 20020, 20455, 20517, 20518);
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
		
		if (event.equals("30307-05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(KARROD_LETTER, 1);
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
				if (player.getRace() != Race.DARK_ELF)
				{
					htmltext = "30307-00.htm";
				}
				else if (player.getLevel() < 11)
				{
					htmltext = "30307-02.htm";
				}
				else
				{
					htmltext = "30307-03.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case KARROD:
						if (cond < 8)
						{
							htmltext = "30307-06.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30307-07.htm";
							st.takeItems(STEELBENDER_HEAD, 1);
							st.giveItems(BLOODSABER, 1);
							st.rewardItems(LESSER_HEALING_POT, 100);
							
							if (player.isMageClass())
							{
								st.giveItems(SPIRITSHOT_NO_GRADE, 500);
							}
							else
							{
								st.giveItems(SOULSHOT_NO_GRADE, 1000);
							}
							
							if (player.isNewbie())
							{
								st.showQuestionMark(26);
								if (player.isMageClass())
								{
									st.playTutorialVoice("tutorial_voice_027");
									st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000);
								}
								else
								{
									st.playTutorialVoice("tutorial_voice_026");
									st.giveItems(SOULSHOT_FOR_BEGINNERS, 7000);
								}
							}
							
							st.giveItems(ECHO_BATTLE, 10);
							st.giveItems(ECHO_LOVE, 10);
							st.giveItems(ECHO_SOLITUDE, 10);
							st.giveItems(ECHO_FEAST, 10);
							st.giveItems(ECHO_CELEBRATION, 10);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case CECKTINON:
						if (cond == 1)
						{
							htmltext = "30132-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(KARROD_LETTER, 1);
							st.giveItems(CECKTINON_VOUCHER_1, 1);
						}
						else if ((cond > 1) && (cond < 5))
						{
							htmltext = "30132-02.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30132-03.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SOUL_CATCHER, 1);
							st.giveItems(PRESERVING_OIL, 1);
						}
						else if (cond == 6)
						{
							htmltext = "30132-04.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30132-05.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ZOMBIE_HEAD, 1);
							st.giveItems(STEELBENDER_HEAD, 1);
						}
						else if (cond == 8)
						{
							htmltext = "30132-06.htm";
						}
						break;
					
					case HARNE:
						if (cond == 2)
						{
							htmltext = "30144-01.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CECKTINON_VOUCHER_1, 1);
							st.giveItems(CECKTINON_VOUCHER_2, 1);
						}
						else if (cond == 3)
						{
							htmltext = "30144-02.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30144-03.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CECKTINON_VOUCHER_2, 1);
							st.takeItems(BONE_FRAGMENT, 10);
							st.giveItems(SOUL_CATCHER, 1);
						}
						else if (cond == 5)
						{
							htmltext = "30144-04.htm";
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
			case 20517:
			case 20518:
			case 20455:
				if ((st.getInt("cond") == 3) && st.dropItems(BONE_FRAGMENT, 1, 10, 300000))
				{
					st.set("cond", "4");
				}
				break;
			
			case 20015:
			case 20020:
				if ((st.getInt("cond") == 6) && st.dropItems(ZOMBIE_HEAD, 1, 1, 300000))
				{
					st.set("cond", "7");
					st.takeItems(PRESERVING_OIL, 1);
				}
				break;
		}
		
		return null;
	}
}