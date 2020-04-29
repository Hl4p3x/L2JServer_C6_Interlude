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
package quests.Q413_PathToAShillienOracle;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q413_PathToAShillienOracle extends Quest
{
	// Items
	private static final int SIDRA_LETTER = 1262;
	private static final int BLANK_SHEET = 1263;
	private static final int BLOODY_RUNE = 1264;
	private static final int GARMIEL_BOOK = 1265;
	private static final int PRAYER_OF_ADONIUS = 1266;
	private static final int PENITENT_MARK = 1267;
	private static final int ASHEN_BONES = 1268;
	private static final int ANDARIEL_BOOK = 1269;
	private static final int ORB_OF_ABYSS = 1270;
	
	// NPCs
	private static final int SIDRA = 30330;
	private static final int ADONIUS = 30375;
	private static final int TALBOT = 30377;
	
	public Q413_PathToAShillienOracle()
	{
		super(413, "Path to a Shillien Oracle");
		
		registerQuestItems(SIDRA_LETTER, BLANK_SHEET, BLOODY_RUNE, GARMIEL_BOOK, PRAYER_OF_ADONIUS, PENITENT_MARK, ASHEN_BONES, ANDARIEL_BOOK);
		
		addStartNpc(SIDRA);
		addTalkId(SIDRA, ADONIUS, TALBOT);
		
		addKillId(20776, 20457, 20458, 20514, 20515);
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
		
		if (event.equals("30330-05.htm"))
		{
			if (player.getClassId() != ClassId.DARK_MAGE)
			{
				htmltext = (player.getClassId() == ClassId.SHILLIEN_ORACLE) ? "30330-02a.htm" : "30330-03.htm";
			}
			else if (player.getLevel() < 19)
			{
				htmltext = "30330-02.htm";
			}
			else if (st.hasQuestItems(ORB_OF_ABYSS))
			{
				htmltext = "30330-04.htm";
			}
		}
		else if (event.equals("30330-06.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(SIDRA_LETTER, 1);
		}
		else if (event.equals("30377-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SIDRA_LETTER, 1);
			st.giveItems(BLANK_SHEET, 5);
		}
		else if (event.equals("30375-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PRAYER_OF_ADONIUS, 1);
			st.giveItems(PENITENT_MARK, 1);
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
				htmltext = "30330-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case SIDRA:
						if (cond == 1)
						{
							htmltext = "30330-07.htm";
						}
						else if ((cond > 1) && (cond < 4))
						{
							htmltext = "30330-08.htm";
						}
						else if ((cond > 3) && (cond < 7))
						{
							htmltext = "30330-09.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30330-10.htm";
							st.takeItems(ANDARIEL_BOOK, 1);
							st.takeItems(GARMIEL_BOOK, 1);
							st.giveItems(ORB_OF_ABYSS, 1);
							st.rewardExpAndSp(3200, 3120);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case TALBOT:
						if (cond == 1)
						{
							htmltext = "30377-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = (st.hasQuestItems(BLOODY_RUNE)) ? "30377-04.htm" : "30377-03.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30377-05.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BLOODY_RUNE, -1);
							st.giveItems(GARMIEL_BOOK, 1);
							st.giveItems(PRAYER_OF_ADONIUS, 1);
						}
						else if ((cond > 3) && (cond < 7))
						{
							htmltext = "30377-06.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30377-07.htm";
						}
						break;
					
					case ADONIUS:
						if (cond == 4)
						{
							htmltext = "30375-01.htm";
						}
						else if (cond == 5)
						{
							htmltext = (st.hasQuestItems(ASHEN_BONES)) ? "30375-05.htm" : "30375-06.htm";
						}
						else if (cond == 6)
						{
							htmltext = "30375-07.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ASHEN_BONES, -1);
							st.takeItems(PENITENT_MARK, -1);
							st.giveItems(ANDARIEL_BOOK, 1);
						}
						else if (cond == 7)
						{
							htmltext = "30375-08.htm";
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
		
		if (npc.getNpcId() == 20776)
		{
			if (st.getInt("cond") == 2)
			{
				st.takeItems(BLANK_SHEET, 1);
				if (st.dropItemsAlways(BLOODY_RUNE, 1, 5))
				{
					st.set("cond", "3");
				}
			}
		}
		else if ((st.getInt("cond") == 5) && st.dropItemsAlways(ASHEN_BONES, 1, 10))
		{
			st.set("cond", "6");
		}
		
		return null;
	}
}