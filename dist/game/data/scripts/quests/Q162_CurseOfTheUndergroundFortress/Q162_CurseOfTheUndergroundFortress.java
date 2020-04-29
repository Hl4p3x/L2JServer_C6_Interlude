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
package quests.Q162_CurseOfTheUndergroundFortress;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q162_CurseOfTheUndergroundFortress extends Quest
{
	// Monsters
	private static final int SHADE_HORROR = 20033;
	private static final int DARK_TERROR = 20345;
	private static final int MIST_TERROR = 20371;
	private static final int DUNGEON_SKELETON_ARCHER = 20463;
	private static final int DUNGEON_SKELETON = 20464;
	private static final int DREAD_SOLDIER = 20504;
	
	// Items
	private static final int BONE_FRAGMENT = 1158;
	private static final int ELF_SKULL = 1159;
	
	// Rewards
	private static final int BONE_SHIELD = 625;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	static
	{
		CHANCES.put(SHADE_HORROR, 250000);
		CHANCES.put(DARK_TERROR, 260000);
		CHANCES.put(MIST_TERROR, 230000);
		CHANCES.put(DUNGEON_SKELETON_ARCHER, 250000);
		CHANCES.put(DUNGEON_SKELETON, 230000);
		CHANCES.put(DREAD_SOLDIER, 260000);
	}
	
	public Q162_CurseOfTheUndergroundFortress()
	{
		super(162, "Curse of the Underground Fortress");
		
		registerQuestItems(BONE_FRAGMENT, ELF_SKULL);
		
		addStartNpc(30147); // Unoren
		addTalkId(30147);
		
		addKillId(SHADE_HORROR, DARK_TERROR, MIST_TERROR, DUNGEON_SKELETON_ARCHER, DUNGEON_SKELETON, DREAD_SOLDIER);
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
		
		if (event.equals("30147-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				if (player.getRace() == Race.DARK_ELF)
				{
					htmltext = "30147-00.htm";
				}
				else if (player.getLevel() < 12)
				{
					htmltext = "30147-01.htm";
				}
				else
				{
					htmltext = "30147-02.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "30147-05.htm";
				}
				else if (cond == 2)
				{
					htmltext = "30147-06.htm";
					st.takeItems(ELF_SKULL, -1);
					st.takeItems(BONE_FRAGMENT, -1);
					st.giveItems(BONE_SHIELD, 1);
					st.rewardItems(57, 24000);
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
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case DUNGEON_SKELETON:
			case DUNGEON_SKELETON_ARCHER:
			case DREAD_SOLDIER:
				if (st.dropItems(BONE_FRAGMENT, 1, 10, CHANCES.get(npcId)) && (st.getQuestItemsCount(ELF_SKULL) >= 3))
				{
					st.set("cond", "2");
				}
				break;
			
			case SHADE_HORROR:
			case DARK_TERROR:
			case MIST_TERROR:
				if (st.dropItems(ELF_SKULL, 1, 3, CHANCES.get(npcId)) && (st.getQuestItemsCount(BONE_FRAGMENT) >= 10))
				{
					st.set("cond", "2");
				}
				break;
		}
		
		return null;
	}
}