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
package quests.Q411_PathToAnAssassin;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q411_PathToAnAssassin extends Quest
{
	// Items
	private static final int SHILEN_CALL = 1245;
	private static final int ARKENIA_LETTER = 1246;
	private static final int LEIKAN_NOTE = 1247;
	private static final int MOONSTONE_BEAST_MOLAR = 1248;
	private static final int SHILEN_TEARS = 1250;
	private static final int ARKENIA_RECOMMENDATION = 1251;
	private static final int IRON_HEART = 1252;
	
	// NPCs
	private static final int TRISKEL = 30416;
	private static final int ARKENIA = 30419;
	private static final int LEIKAN = 30382;
	
	public Q411_PathToAnAssassin()
	{
		super(411, "Path to an Assassin");
		
		registerQuestItems(SHILEN_CALL, ARKENIA_LETTER, LEIKAN_NOTE, MOONSTONE_BEAST_MOLAR, SHILEN_TEARS, ARKENIA_RECOMMENDATION);
		
		addStartNpc(TRISKEL);
		addTalkId(TRISKEL, ARKENIA, LEIKAN);
		
		addKillId(27036, 20369);
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
		
		if (event.equals("30416-05.htm"))
		{
			if (player.getClassId() != ClassId.DARK_FIGHTER)
			{
				htmltext = (player.getClassId() == ClassId.ASSASSIN) ? "30416-02a.htm" : "30416-02.htm";
			}
			else if (player.getLevel() < 19)
			{
				htmltext = "30416-03.htm";
			}
			else if (st.hasQuestItems(IRON_HEART))
			{
				htmltext = "30416-04.htm";
			}
			else
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.giveItems(SHILEN_CALL, 1);
			}
		}
		else if (event.equals("30419-05.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SHILEN_CALL, 1);
			st.giveItems(ARKENIA_LETTER, 1);
		}
		else if (event.equals("30382-03.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ARKENIA_LETTER, 1);
			st.giveItems(LEIKAN_NOTE, 1);
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
				htmltext = "30416-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case TRISKEL:
						if (cond == 1)
						{
							htmltext = "30416-11.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30416-07.htm";
						}
						else if ((cond == 3) || (cond == 4))
						{
							htmltext = "30416-08.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30416-09.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30416-10.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30416-06.htm";
							st.takeItems(ARKENIA_RECOMMENDATION, 1);
							st.giveItems(IRON_HEART, 1);
							st.rewardExpAndSp(3200, 3930);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case ARKENIA:
						if (cond == 1)
						{
							htmltext = "30419-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30419-07.htm";
						}
						else if ((cond == 3) || (cond == 4))
						{
							htmltext = "30419-10.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30419-11.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30419-08.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SHILEN_TEARS, -1);
							st.giveItems(ARKENIA_RECOMMENDATION, 1);
						}
						else if (cond == 7)
						{
							htmltext = "30419-09.htm";
						}
						break;
					
					case LEIKAN:
						if (cond == 2)
						{
							htmltext = "30382-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = (!st.hasQuestItems(MOONSTONE_BEAST_MOLAR)) ? "30382-05.htm" : "30382-06.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30382-07.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MOONSTONE_BEAST_MOLAR, -1);
							st.takeItems(LEIKAN_NOTE, -1);
						}
						else if (cond == 5)
						{
							htmltext = "30382-09.htm";
						}
						else if (cond > 5)
						{
							htmltext = "30382-08.htm";
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
		
		if (npc.getNpcId() == 20369)
		{
			if ((st.getInt("cond") == 3) && st.dropItemsAlways(MOONSTONE_BEAST_MOLAR, 1, 10))
			{
				st.set("cond", "4");
			}
		}
		else if (st.getInt("cond") == 5)
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SHILEN_TEARS, 1);
		}
		
		return null;
	}
}