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
package quests.Q102_SeaOfSporesFever;

import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q102_SeaOfSporesFever extends Quest
{
	// Items
	private static final int ALBERIUS_LETTER = 964;
	private static final int EVERGREEN_AMULET = 965;
	private static final int DRYAD_TEARS = 966;
	private static final int ALBERIUS_LIST = 746;
	private static final int COBENDELL_MEDICINE_1 = 1130;
	private static final int COBENDELL_MEDICINE_2 = 1131;
	private static final int COBENDELL_MEDICINE_3 = 1132;
	private static final int COBENDELL_MEDICINE_4 = 1133;
	private static final int COBENDELL_MEDICINE_5 = 1134;
	
	// Rewards
	private static final int SPIRITSHOT_NO_GRADE = 2509;
	private static final int SOULSHOT_NO_GRADE = 1835;
	private static final int SWORD_OF_SENTINEL = 743;
	private static final int STAFF_OF_SENTINEL = 744;
	private static final int LESSER_HEALING_POT = 1060;
	private static final int ECHO_BATTLE = 4412;
	private static final int ECHO_LOVE = 4413;
	private static final int ECHO_SOLITUDE = 4414;
	private static final int ECHO_FEAST = 4415;
	private static final int ECHO_CELEBRATION = 4416;
	
	// NPCs
	private static final int ALBERIUS = 30284;
	private static final int COBENDELL = 30156;
	private static final int BERROS = 30217;
	private static final int VELTRESS = 30219;
	private static final int RAYEN = 30221;
	private static final int GARTRANDELL = 30285;
	
	public Q102_SeaOfSporesFever()
	{
		super(102, "Sea of Spores Fever");
		
		registerQuestItems(ALBERIUS_LETTER, EVERGREEN_AMULET, DRYAD_TEARS, COBENDELL_MEDICINE_1, COBENDELL_MEDICINE_2, COBENDELL_MEDICINE_3, COBENDELL_MEDICINE_4, COBENDELL_MEDICINE_5, ALBERIUS_LIST);
		
		addStartNpc(ALBERIUS);
		addTalkId(ALBERIUS, COBENDELL, BERROS, RAYEN, GARTRANDELL, VELTRESS);
		
		addKillId(20013, 20019);
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
		
		if (event.equals("30284-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ALBERIUS_LETTER, 1);
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
				if (player.getRace() != Race.ELF)
				{
					htmltext = "30284-00.htm";
				}
				else if (player.getLevel() < 12)
				{
					htmltext = "30284-08.htm";
				}
				else
				{
					htmltext = "30284-07.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ALBERIUS:
						if (cond == 1)
						{
							htmltext = "30284-03.htm";
						}
						else if ((cond == 2) || (cond == 3))
						{
							htmltext = "30284-09.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30284-04.htm";
							st.set("cond", "5");
							st.set("medicines", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(COBENDELL_MEDICINE_1, 1);
							st.giveItems(ALBERIUS_LIST, 1);
						}
						else if (cond == 5)
						{
							htmltext = "30284-05.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30284-06.htm";
							st.takeItems(ALBERIUS_LIST, 1);
							
							if (player.isMageClass())
							{
								st.giveItems(STAFF_OF_SENTINEL, 1);
								st.rewardItems(SPIRITSHOT_NO_GRADE, 500);
							}
							else
							{
								st.giveItems(SWORD_OF_SENTINEL, 1);
								st.rewardItems(SOULSHOT_NO_GRADE, 1000);
							}
							
							st.giveItems(LESSER_HEALING_POT, 100);
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
					
					case COBENDELL:
						if (cond == 1)
						{
							htmltext = "30156-03.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ALBERIUS_LETTER, 1);
							st.giveItems(EVERGREEN_AMULET, 1);
						}
						else if (cond == 2)
						{
							htmltext = "30156-04.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30156-07.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30156-05.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(DRYAD_TEARS, -1);
							st.takeItems(EVERGREEN_AMULET, 1);
							st.giveItems(COBENDELL_MEDICINE_1, 1);
							st.giveItems(COBENDELL_MEDICINE_2, 1);
							st.giveItems(COBENDELL_MEDICINE_3, 1);
							st.giveItems(COBENDELL_MEDICINE_4, 1);
							st.giveItems(COBENDELL_MEDICINE_5, 1);
						}
						else if (cond == 4)
						{
							htmltext = "30156-06.htm";
						}
						break;
					
					case BERROS:
						if (cond == 5)
						{
							htmltext = "30217-01.htm";
							checkItem(st, COBENDELL_MEDICINE_2);
						}
						break;
					
					case VELTRESS:
						if (cond == 5)
						{
							htmltext = "30219-01.htm";
							checkItem(st, COBENDELL_MEDICINE_3);
						}
						break;
					
					case RAYEN:
						if (cond == 5)
						{
							htmltext = "30221-01.htm";
							checkItem(st, COBENDELL_MEDICINE_4);
						}
						break;
					
					case GARTRANDELL:
						if (cond == 5)
						{
							htmltext = "30285-01.htm";
							checkItem(st, COBENDELL_MEDICINE_5);
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
		final QuestState st = checkPlayerCondition(player, npc, "cond", "2");
		if (st == null)
		{
			return null;
		}
		
		if (st.dropItems(DRYAD_TEARS, 1, 10, 300000))
		{
			st.set("cond", "3");
		}
		
		return null;
	}
	
	private void checkItem(QuestState st, int itemId)
	{
		if (st.hasQuestItems(itemId))
		{
			st.takeItems(itemId, 1);
			
			final int medicinesLeft = st.getInt("medicines") - 1;
			if (medicinesLeft == 0)
			{
				st.set("cond", "6");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
			{
				st.set("medicines", String.valueOf(medicinesLeft));
			}
		}
	}
}