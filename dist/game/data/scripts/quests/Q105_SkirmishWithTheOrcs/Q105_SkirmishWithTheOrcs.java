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
package quests.Q105_SkirmishWithTheOrcs;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q105_SkirmishWithTheOrcs extends Quest
{
	// Item
	private static final int KENDELL_ORDER_1 = 1836;
	private static final int KENDELL_ORDER_2 = 1837;
	private static final int KENDELL_ORDER_3 = 1838;
	private static final int KENDELL_ORDER_4 = 1839;
	private static final int KENDELL_ORDER_5 = 1840;
	private static final int KENDELL_ORDER_6 = 1841;
	private static final int KENDELL_ORDER_7 = 1842;
	private static final int KENDELL_ORDER_8 = 1843;
	private static final int KABOO_CHIEF_TORC_1 = 1844;
	private static final int KABOO_CHIEF_TORC_2 = 1845;
	
	// Monster
	private static final int KABOO_CHIEF_UOPH = 27059;
	private static final int KABOO_CHIEF_KRACHA = 27060;
	private static final int KABOO_CHIEF_BATOH = 27061;
	private static final int KABOO_CHIEF_TANUKIA = 27062;
	private static final int KABOO_CHIEF_TUREL = 27064;
	private static final int KABOO_CHIEF_ROKO = 27065;
	private static final int KABOO_CHIEF_KAMUT = 27067;
	private static final int KABOO_CHIEF_MURTIKA = 27068;
	
	// Rewards
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	private static final int RED_SUNSET_STAFF = 754;
	private static final int RED_SUNSET_SWORD = 981;
	private static final int ECHO_BATTLE = 4412;
	private static final int ECHO_LOVE = 4413;
	private static final int ECHO_SOLITUDE = 4414;
	private static final int ECHO_FEAST = 4415;
	private static final int ECHO_CELEBRATION = 4416;
	
	public Q105_SkirmishWithTheOrcs()
	{
		super(105, "Skirmish with the Orcs");
		
		registerQuestItems(KENDELL_ORDER_1, KENDELL_ORDER_2, KENDELL_ORDER_3, KENDELL_ORDER_4, KENDELL_ORDER_5, KENDELL_ORDER_6, KENDELL_ORDER_7, KENDELL_ORDER_8, KABOO_CHIEF_TORC_1, KABOO_CHIEF_TORC_2);
		
		addStartNpc(30218); // Kendell
		addTalkId(30218);
		
		addKillId(KABOO_CHIEF_UOPH, KABOO_CHIEF_KRACHA, KABOO_CHIEF_BATOH, KABOO_CHIEF_TANUKIA, KABOO_CHIEF_TUREL, KABOO_CHIEF_ROKO, KABOO_CHIEF_KAMUT, KABOO_CHIEF_MURTIKA);
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
		
		if (event.equals("30218-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(Rnd.get(1836, 1839), 1); // Kendell's orders 1 to 4.
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
				if (player.getRace() != Race.ELF)
				{
					htmltext = "30218-00.htm";
				}
				else if (player.getLevel() < 10)
				{
					htmltext = "30218-01.htm";
				}
				else
				{
					htmltext = "30218-02.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "30218-05.htm";
				}
				else if (cond == 2)
				{
					htmltext = "30218-06.htm";
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(KABOO_CHIEF_TORC_1, 1);
					st.takeItems(KENDELL_ORDER_1, 1);
					st.takeItems(KENDELL_ORDER_2, 1);
					st.takeItems(KENDELL_ORDER_3, 1);
					st.takeItems(KENDELL_ORDER_4, 1);
					st.giveItems(Rnd.get(1840, 1843), 1); // Kendell's orders 5 to 8.
				}
				else if (cond == 3)
				{
					htmltext = "30218-07.htm";
				}
				else if (cond == 4)
				{
					htmltext = "30218-08.htm";
					st.takeItems(KABOO_CHIEF_TORC_2, 1);
					st.takeItems(KENDELL_ORDER_5, 1);
					st.takeItems(KENDELL_ORDER_6, 1);
					st.takeItems(KENDELL_ORDER_7, 1);
					st.takeItems(KENDELL_ORDER_8, 1);
					
					if (player.isMageClass())
					{
						st.giveItems(RED_SUNSET_STAFF, 1);
					}
					else
					{
						st.giveItems(RED_SUNSET_SWORD, 1);
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
			case KABOO_CHIEF_UOPH:
			case KABOO_CHIEF_KRACHA:
			case KABOO_CHIEF_BATOH:
			case KABOO_CHIEF_TANUKIA:
				if ((st.getInt("cond") == 1) && st.hasQuestItems(npc.getNpcId() - 25223)) // npcId - 25223 = itemId to verify.
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(KABOO_CHIEF_TORC_1, 1);
				}
				break;
			
			case KABOO_CHIEF_TUREL:
			case KABOO_CHIEF_ROKO:
				if ((st.getInt("cond") == 3) && st.hasQuestItems(npc.getNpcId() - 25224)) // npcId - 25224 = itemId to verify.
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(KABOO_CHIEF_TORC_2, 1);
				}
				break;
			
			case KABOO_CHIEF_KAMUT:
			case KABOO_CHIEF_MURTIKA:
				if ((st.getInt("cond") == 3) && st.hasQuestItems(npc.getNpcId() - 25225)) // npcId - 25225 = itemId to verify.
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(KABOO_CHIEF_TORC_2, 1);
				}
				break;
		}
		
		return null;
	}
}