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
package quests.Q125_TheNameOfEvil_1;

import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.util.Util;

import quests.Q124_MeetingTheElroki.Q124_MeetingTheElroki;

public class Q125_TheNameOfEvil_1 extends Quest
{
	private static final int MUSHIKA = 32114;
	private static final int KARAKAWEI = 32117;
	private static final int ULU_KAIMU = 32119;
	private static final int BALU_KAIMU = 32120;
	private static final int CHUTA_KAIMU = 32121;
	
	private static final int ORNITHOMIMUS_CLAW = 8779;
	private static final int DEINONYCHUS_BONE = 8780;
	private static final int EPITAPH_OF_WISDOM = 8781;
	private static final int GAZKH_FRAGMENT = 8782;
	
	private static final int[] ORNITHOMIMUS =
	{
		22200,
		22201,
		22202,
		22219,
		22224,
		22742,
		22744
	};
	
	private static final int[] DEINONYCHUS =
	{
		16067,
		22203,
		22204,
		22205,
		22220,
		22225,
		22743,
		22745
	};
	
	public Q125_TheNameOfEvil_1()
	{
		super(125, "The Name of Evil - 1");
		
		registerQuestItems(ORNITHOMIMUS_CLAW, DEINONYCHUS_BONE, EPITAPH_OF_WISDOM, GAZKH_FRAGMENT);
		
		addStartNpc(MUSHIKA);
		addTalkId(MUSHIKA, KARAKAWEI, ULU_KAIMU, BALU_KAIMU, CHUTA_KAIMU);
		
		for (int i : ORNITHOMIMUS)
		{
			addKillId(i);
		}
		
		for (int i : DEINONYCHUS)
		{
			addKillId(i);
		}
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
		
		if (event.equals("32114-05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("32114-09.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(GAZKH_FRAGMENT, 1);
		}
		else if (event.equals("32117-08.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32117-14.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32119-14.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32120-15.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("32121-16.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GAZKH_FRAGMENT, -1);
			st.giveItems(EPITAPH_OF_WISDOM, 1);
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
				final QuestState first = player.getQuestState(Q124_MeetingTheElroki.class.getSimpleName());
				if ((first != null) && first.isCompleted() && (player.getLevel() >= 76))
				{
					htmltext = "32114-01.htm";
				}
				else
				{
					htmltext = "32114-00.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MUSHIKA:
						if (cond == 1)
						{
							htmltext = "32114-07.htm";
						}
						else if (cond == 2)
						{
							htmltext = "32114-10.htm";
						}
						else if ((cond > 2) && (cond < 8))
						{
							htmltext = "32114-11.htm";
						}
						else if (cond == 8)
						{
							htmltext = "32114-12.htm";
							st.takeItems(EPITAPH_OF_WISDOM, -1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case KARAKAWEI:
						if (cond == 2)
						{
							htmltext = "32117-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "32117-09.htm";
						}
						else if (cond == 4)
						{
							if ((st.getQuestItemsCount(ORNITHOMIMUS_CLAW) >= 2) && (st.getQuestItemsCount(DEINONYCHUS_BONE) >= 2))
							{
								htmltext = "32117-10.htm";
								st.takeItems(ORNITHOMIMUS_CLAW, -1);
								st.takeItems(DEINONYCHUS_BONE, -1);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								htmltext = "32117-09.htm";
								st.set("cond", "3");
							}
						}
						else if (cond == 5)
						{
							htmltext = "32117-15.htm";
						}
						else if ((cond == 6) || (cond == 7))
						{
							htmltext = "32117-16.htm";
						}
						else if (cond == 8)
						{
							htmltext = "32117-17.htm";
						}
						break;
					
					case ULU_KAIMU:
						if (cond == 5)
						{
							npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
							htmltext = "32119-01.htm";
						}
						else if (cond == 6)
						{
							htmltext = "32119-14.htm";
						}
						break;
					
					case BALU_KAIMU:
						if (cond == 6)
						{
							npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
							htmltext = "32120-01.htm";
						}
						else if (cond == 7)
						{
							htmltext = "32120-16.htm";
						}
						break;
					
					case CHUTA_KAIMU:
						if (cond == 7)
						{
							npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
							htmltext = "32121-01.htm";
						}
						else if (cond == 8)
						{
							htmltext = "32121-17.htm";
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
		final QuestState st = checkPlayerCondition(player, npc, "cond", "3");
		if (st == null)
		{
			return null;
		}
		
		final int npcId = npc.getNpcId();
		if (Util.contains(ORNITHOMIMUS, npcId))
		{
			if (st.dropItems(ORNITHOMIMUS_CLAW, 1, 2, 50000) && (st.getQuestItemsCount(DEINONYCHUS_BONE) == 2))
			{
				st.set("cond", "4");
			}
		}
		else if (Util.contains(DEINONYCHUS, npcId))
		{
			if (st.dropItems(DEINONYCHUS_BONE, 1, 2, 50000) && (st.getQuestItemsCount(ORNITHOMIMUS_CLAW) == 2))
			{
				st.set("cond", "4");
			}
		}
		return null;
	}
}