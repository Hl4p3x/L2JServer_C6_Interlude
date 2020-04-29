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
package quests.Q166_MassOfDarkness;

import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q166_MassOfDarkness extends Quest
{
	// NPCs
	private static final int UNDRIAS = 30130;
	private static final int IRIA = 30135;
	private static final int DORANKUS = 30139;
	private static final int TRUDY = 30143;
	
	// Items
	private static final int UNDRIAS_LETTER = 1088;
	private static final int CEREMONIAL_DAGGER = 1089;
	private static final int DREVIANT_WINE = 1090;
	private static final int GARMIEL_SCRIPTURE = 1091;
	
	public Q166_MassOfDarkness()
	{
		super(166, "Mass of Darkness");
		
		registerQuestItems(UNDRIAS_LETTER, CEREMONIAL_DAGGER, DREVIANT_WINE, GARMIEL_SCRIPTURE);
		
		addStartNpc(UNDRIAS);
		addTalkId(UNDRIAS, IRIA, DORANKUS, TRUDY);
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
		
		if (event.equals("30130-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(UNDRIAS_LETTER, 1);
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
				if (player.getRace() != Race.DARK_ELF)
				{
					htmltext = "30130-00.htm";
				}
				else if (player.getLevel() < 2)
				{
					htmltext = "30130-02.htm";
				}
				else
				{
					htmltext = "30130-03.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case UNDRIAS:
						if (cond == 1)
						{
							htmltext = "30130-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30130-06.htm";
							st.takeItems(CEREMONIAL_DAGGER, 1);
							st.takeItems(DREVIANT_WINE, 1);
							st.takeItems(GARMIEL_SCRIPTURE, 1);
							st.takeItems(UNDRIAS_LETTER, 1);
							st.rewardItems(57, 500);
							st.rewardExpAndSp(500, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case IRIA:
						if ((cond == 1) && !st.hasQuestItems(CEREMONIAL_DAGGER))
						{
							htmltext = "30135-01.htm";
							st.giveItems(CEREMONIAL_DAGGER, 1);
							if (st.hasQuestItems(DREVIANT_WINE, GARMIEL_SCRIPTURE))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (cond == 2)
						{
							htmltext = "30135-02.htm";
						}
						break;
					
					case DORANKUS:
						if ((cond == 1) && !st.hasQuestItems(DREVIANT_WINE))
						{
							htmltext = "30139-01.htm";
							st.giveItems(DREVIANT_WINE, 1);
							if (st.hasQuestItems(CEREMONIAL_DAGGER, GARMIEL_SCRIPTURE))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (cond == 2)
						{
							htmltext = "30139-02.htm";
						}
						break;
					
					case TRUDY:
						if ((cond == 1) && !st.hasQuestItems(GARMIEL_SCRIPTURE))
						{
							htmltext = "30143-01.htm";
							st.giveItems(GARMIEL_SCRIPTURE, 1);
							if (st.hasQuestItems(CEREMONIAL_DAGGER, DREVIANT_WINE))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (cond == 2)
						{
							htmltext = "30143-02.htm";
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