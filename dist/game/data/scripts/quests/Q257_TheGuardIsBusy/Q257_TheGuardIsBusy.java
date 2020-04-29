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
package quests.Q257_TheGuardIsBusy;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q257_TheGuardIsBusy extends Quest
{
	// Items
	private static final int GLUDIO_LORD_MARK = 1084;
	private static final int ORC_AMULET = 752;
	private static final int ORC_NECKLACE = 1085;
	private static final int WEREWOLF_FANG = 1086;
	
	// Newbie Items
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q257_TheGuardIsBusy()
	{
		super(257, "The Guard is Busy");
		
		registerQuestItems(ORC_AMULET, ORC_NECKLACE, WEREWOLF_FANG, GLUDIO_LORD_MARK);
		
		addStartNpc(30039); // Gilbert
		addTalkId(30039);
		
		addKillId(20006, 20093, 20096, 20098, 20130, 20131, 20132, 20342, 20343);
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
		
		if (event.equals("30039-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(GLUDIO_LORD_MARK, 1);
		}
		else if (event.equals("30039-05.htm"))
		{
			st.takeItems(GLUDIO_LORD_MARK, 1);
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
				htmltext = (player.getLevel() < 6) ? "30039-01.htm" : "30039-02.htm";
				break;
			
			case State.STARTED:
				final int amulets = st.getQuestItemsCount(ORC_AMULET);
				final int necklaces = st.getQuestItemsCount(ORC_NECKLACE);
				final int fangs = st.getQuestItemsCount(WEREWOLF_FANG);
				
				if ((amulets + necklaces + fangs) == 0)
				{
					htmltext = "30039-04.htm";
				}
				else
				{
					htmltext = "30039-07.htm";
					
					st.takeItems(ORC_AMULET, -1);
					st.takeItems(ORC_NECKLACE, -1);
					st.takeItems(WEREWOLF_FANG, -1);
					
					int reward = (10 * amulets) + (20 * (necklaces + fangs));
					if ((amulets + necklaces + fangs) >= 10)
					{
						reward += 1000;
					}
					
					st.rewardItems(57, reward);
					
					if (player.isNewbie() && (st.getInt("Reward") == 0))
					{
						st.showQuestionMark(26);
						st.set("Reward", "1");
						
						if (player.isMageClass())
						{
							st.playTutorialVoice("tutorial_voice_027");
							st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000);
						}
						else
						{
							st.playTutorialVoice("tutorial_voice_026");
							st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
						}
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
		
		switch (npc.getNpcId())
		{
			case 20006:
			case 20130:
			case 20131:
				st.dropItems(ORC_AMULET, 1, 0, 500000);
				break;
			
			case 20093:
			case 20096:
			case 20098:
				st.dropItems(ORC_NECKLACE, 1, 0, 500000);
				break;
			
			case 20342:
				st.dropItems(WEREWOLF_FANG, 1, 0, 200000);
				break;
			
			case 20343:
				st.dropItems(WEREWOLF_FANG, 1, 0, 400000);
				break;
			
			case 20132:
				st.dropItems(WEREWOLF_FANG, 1, 0, 500000);
				break;
		}
		
		return null;
	}
}