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
package quests.Q247_PossessorOfAPreciousSoul;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q247_PossessorOfAPreciousSoul extends Quest
{
	// NPCs
	private static final int CARADINE = 31740;
	private static final int LADY_OF_THE_LAKE = 31745;
	
	// Items
	private static final int CARADINE_LETTER = 7679;
	private static final int NOBLESS_TIARA = 7694;
	
	public Q247_PossessorOfAPreciousSoul()
	{
		super(247, "Possessor of a Precious Soul - 4");
		
		addStartNpc(CARADINE);
		addTalkId(CARADINE, LADY_OF_THE_LAKE);
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
		
		// Caradine
		if (event.equals("31740-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.takeItems(CARADINE_LETTER, 1);
		}
		else if (event.equals("31740-05.htm"))
		{
			st.set("cond", "2");
			player.teleToLocation(143209, 43968, -3038);
		}
		// Lady of the lake
		else if (event.equals("31745-05.htm"))
		{
			player.setNoble(true);
			st.giveItems(NOBLESS_TIARA, 1);
			st.rewardExpAndSp(93836, 0);
			player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				if (st.hasQuestItems(CARADINE_LETTER))
				{
					htmltext = (!player.isSubClassActive() || (player.getLevel() < 75)) ? "31740-02.htm" : "31740-01.htm";
				}
				break;
			
			case State.STARTED:
				if (!player.isSubClassActive())
				{
					break;
				}
				
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case CARADINE:
						if (cond == 1)
						{
							htmltext = "31740-04.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31740-06.htm";
						}
						break;
					
					case LADY_OF_THE_LAKE:
						if (cond == 2)
						{
							htmltext = (player.getLevel() < 75) ? "31745-06.htm" : "31745-01.htm";
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