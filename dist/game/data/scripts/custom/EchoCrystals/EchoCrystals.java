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
package custom.EchoCrystals;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.holders.ScoreDataHolder;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.util.Util;

/**
 * @authors DrLecter (python), Plim (java)
 * @notes Formerly based on Elektra's script
 */
public class EchoCrystals extends Quest
{
	private static final int ADENA = 57;
	private static final int COST = 200;
	
	private static final Map<Integer, ScoreDataHolder> SCORES = new HashMap<>();
	static
	{
		SCORES.put(4410, new ScoreDataHolder(4411, "01", "02", "03"));
		SCORES.put(4409, new ScoreDataHolder(4412, "04", "05", "06"));
		SCORES.put(4408, new ScoreDataHolder(4413, "07", "08", "09"));
		SCORES.put(4420, new ScoreDataHolder(4414, "10", "11", "12"));
		SCORES.put(4421, new ScoreDataHolder(4415, "13", "14", "15"));
		SCORES.put(4419, new ScoreDataHolder(4417, "16", "05", "06"));
		SCORES.put(4418, new ScoreDataHolder(4416, "17", "05", "06"));
	}
	
	public EchoCrystals()
	{
		super(-1, "custom");
		
		addStartNpc(31042, 31043);
		addTalkId(31042, 31043);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		if ((st != null) && Util.isDigit(event))
		{
			final int score = Integer.parseInt(event);
			if (SCORES.containsKey(score))
			{
				final int crystal = SCORES.get(score).getCrystalId();
				final String ok = SCORES.get(score).getOkMsg();
				final String noadena = SCORES.get(score).getNoAdenaMsg();
				final String noscore = SCORES.get(score).getNoScoreMsg();
				if (st.getQuestItemsCount(score) == 0)
				{
					htmltext = npc.getNpcId() + "-" + noscore + ".htm";
				}
				else if (st.getQuestItemsCount(ADENA) < COST)
				{
					htmltext = npc.getNpcId() + "-" + noadena + ".htm";
				}
				else
				{
					st.takeItems(ADENA, COST);
					st.giveItems(crystal, 1);
					htmltext = npc.getNpcId() + "-" + ok + ".htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		return "1.htm";
	}
	
	public static void main(String[] args)
	{
		new EchoCrystals();
	}
}