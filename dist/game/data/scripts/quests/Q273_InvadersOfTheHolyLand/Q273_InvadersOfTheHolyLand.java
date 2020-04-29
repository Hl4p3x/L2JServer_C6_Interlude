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
package quests.Q273_InvadersOfTheHolyLand;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q273_InvadersOfTheHolyLand extends Quest
{
	// Items
	private static final int BLACK_SOULSTONE = 1475;
	private static final int RED_SOULSTONE = 1476;
	
	// Reward
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q273_InvadersOfTheHolyLand()
	{
		super(273, "Invaders of the Holy Land");
		
		registerQuestItems(BLACK_SOULSTONE, RED_SOULSTONE);
		
		addStartNpc(30566); // Varkees
		addTalkId(30566);
		
		addKillId(20311, 20312, 20313);
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
		
		if (event.equals("30566-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30566-07.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (player.getRace() != Race.ORC)
				{
					htmltext = "30566-00.htm";
				}
				else if (player.getLevel() < 6)
				{
					htmltext = "30566-01.htm";
				}
				else
				{
					htmltext = "30566-02.htm";
				}
				break;
			
			case State.STARTED:
				final int red = st.getQuestItemsCount(RED_SOULSTONE);
				final int black = st.getQuestItemsCount(BLACK_SOULSTONE);
				if ((red + black) == 0)
				{
					htmltext = "30566-04.htm";
				}
				else
				{
					if (red == 0)
					{
						htmltext = "30566-05.htm";
					}
					else
					{
						htmltext = "30566-06.htm";
					}
					
					final int reward = (black * 3) + (red * 10) + ((black >= 10) ? ((red >= 1) ? 1800 : 1500) : 0);
					st.takeItems(BLACK_SOULSTONE, -1);
					st.takeItems(RED_SOULSTONE, -1);
					st.rewardItems(57, reward);
					if (player.isNewbie() && (st.getInt("Reward") == 0))
					{
						st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
						st.playTutorialVoice("tutorial_voice_026");
						st.set("Reward", "1");
					}
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
		
		final int npcId = npc.getNpcId();
		int probability = 77;
		if (npcId == 20311)
		{
			probability = 90;
		}
		else if (npcId == 20312)
		{
			probability = 87;
		}
		
		if (Rnd.get(100) <= probability)
		{
			st.dropItemsAlways(BLACK_SOULSTONE, 1, 0);
		}
		else
		{
			st.dropItemsAlways(RED_SOULSTONE, 1, 0);
		}
		
		return null;
	}
}