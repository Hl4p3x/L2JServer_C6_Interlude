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
package quests.Q325_GrimCollector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.holders.ItemHolder;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q325_GrimCollector extends Quest
{
	// Items
	private static final int ANATOMY_DIAGRAM = 1349;
	private static final int ZOMBIE_HEAD = 1350;
	private static final int ZOMBIE_HEART = 1351;
	private static final int ZOMBIE_LIVER = 1352;
	private static final int SKULL = 1353;
	private static final int RIB_BONE = 1354;
	private static final int SPINE = 1355;
	private static final int ARM_BONE = 1356;
	private static final int THIGH_BONE = 1357;
	private static final int COMPLETE_SKELETON = 1358;
	
	// NPCs
	private static final int CURTIS = 30336;
	private static final int VARSAK = 30342;
	private static final int SAMED = 30434;
	
	private static final Map<Integer, List<ItemHolder>> DROPLIST = new HashMap<>();
	static
	{
		DROPLIST.put(20026, Arrays.asList(new ItemHolder(ZOMBIE_HEAD, 30), new ItemHolder(ZOMBIE_HEART, 50), new ItemHolder(ZOMBIE_LIVER, 75)));
		DROPLIST.put(20029, Arrays.asList(new ItemHolder(ZOMBIE_HEAD, 30), new ItemHolder(ZOMBIE_HEART, 52), new ItemHolder(ZOMBIE_LIVER, 75)));
		DROPLIST.put(20035, Arrays.asList(new ItemHolder(SKULL, 5), new ItemHolder(RIB_BONE, 15), new ItemHolder(SPINE, 29), new ItemHolder(THIGH_BONE, 79)));
		DROPLIST.put(20042, Arrays.asList(new ItemHolder(SKULL, 6), new ItemHolder(RIB_BONE, 19), new ItemHolder(ARM_BONE, 69), new ItemHolder(THIGH_BONE, 86)));
		DROPLIST.put(20045, Arrays.asList(new ItemHolder(SKULL, 9), new ItemHolder(SPINE, 59), new ItemHolder(ARM_BONE, 77), new ItemHolder(THIGH_BONE, 97)));
		DROPLIST.put(20051, Arrays.asList(new ItemHolder(SKULL, 9), new ItemHolder(RIB_BONE, 59), new ItemHolder(SPINE, 79), new ItemHolder(ARM_BONE, 100)));
		DROPLIST.put(20457, Arrays.asList(new ItemHolder(ZOMBIE_HEAD, 40), new ItemHolder(ZOMBIE_HEART, 60), new ItemHolder(ZOMBIE_LIVER, 80)));
		DROPLIST.put(20458, Arrays.asList(new ItemHolder(ZOMBIE_HEAD, 40), new ItemHolder(ZOMBIE_HEART, 70), new ItemHolder(ZOMBIE_LIVER, 100)));
		DROPLIST.put(20514, Arrays.asList(new ItemHolder(SKULL, 6), new ItemHolder(RIB_BONE, 21), new ItemHolder(SPINE, 30), new ItemHolder(ARM_BONE, 31), new ItemHolder(THIGH_BONE, 64)));
		DROPLIST.put(20515, Arrays.asList(new ItemHolder(SKULL, 5), new ItemHolder(RIB_BONE, 20), new ItemHolder(SPINE, 31), new ItemHolder(ARM_BONE, 33), new ItemHolder(THIGH_BONE, 69)));
	}
	
	public Q325_GrimCollector()
	{
		super(325, "Grim Collector");
		
		registerQuestItems(ZOMBIE_HEAD, ZOMBIE_HEART, ZOMBIE_LIVER, SKULL, RIB_BONE, SPINE, ARM_BONE, THIGH_BONE, COMPLETE_SKELETON, ANATOMY_DIAGRAM);
		
		addStartNpc(CURTIS);
		addTalkId(CURTIS, VARSAK, SAMED);
		
		for (int npcId : DROPLIST.keySet())
		{
			addKillId(npcId);
		}
	}
	
	private static int getNumberOfPieces(QuestState st)
	{
		return st.getQuestItemsCount(ZOMBIE_HEAD) + st.getQuestItemsCount(SPINE) + st.getQuestItemsCount(ARM_BONE) + st.getQuestItemsCount(ZOMBIE_HEART) + st.getQuestItemsCount(ZOMBIE_LIVER) + st.getQuestItemsCount(SKULL) + st.getQuestItemsCount(RIB_BONE) + st.getQuestItemsCount(THIGH_BONE) + st.getQuestItemsCount(COMPLETE_SKELETON);
	}
	
	private void payback(QuestState st)
	{
		final int count = getNumberOfPieces(st);
		if (count > 0)
		{
			int reward = (30 * st.getQuestItemsCount(ZOMBIE_HEAD)) + (20 * st.getQuestItemsCount(ZOMBIE_HEART)) + (20 * st.getQuestItemsCount(ZOMBIE_LIVER)) + (100 * st.getQuestItemsCount(SKULL)) + (40 * st.getQuestItemsCount(RIB_BONE)) + (14 * st.getQuestItemsCount(SPINE)) + (14 * st.getQuestItemsCount(ARM_BONE)) + (14 * st.getQuestItemsCount(THIGH_BONE)) + (341 * st.getQuestItemsCount(COMPLETE_SKELETON));
			if (count > 10)
			{
				reward += 1629;
			}
			
			if (st.hasQuestItems(COMPLETE_SKELETON))
			{
				reward += 543;
			}
			
			st.takeItems(ZOMBIE_HEAD, -1);
			st.takeItems(ZOMBIE_HEART, -1);
			st.takeItems(ZOMBIE_LIVER, -1);
			st.takeItems(SKULL, -1);
			st.takeItems(RIB_BONE, -1);
			st.takeItems(SPINE, -1);
			st.takeItems(ARM_BONE, -1);
			st.takeItems(THIGH_BONE, -1);
			st.takeItems(COMPLETE_SKELETON, -1);
			
			st.rewardItems(57, reward);
		}
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
		
		if (event.equals("30336-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30434-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(ANATOMY_DIAGRAM, 1);
		}
		else if (event.equals("30434-06.htm"))
		{
			st.takeItems(ANATOMY_DIAGRAM, -1);
			payback(st);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equals("30434-07.htm"))
		{
			payback(st);
		}
		else if (event.equals("30434-09.htm"))
		{
			final int skeletons = st.getQuestItemsCount(COMPLETE_SKELETON);
			if (skeletons > 0)
			{
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(COMPLETE_SKELETON, -1);
				st.rewardItems(57, 543 + (341 * skeletons));
			}
		}
		else if (event.equals("30342-03.htm"))
		{
			if (!st.hasQuestItems(SPINE, ARM_BONE, SKULL, RIB_BONE, THIGH_BONE))
			{
				htmltext = "30342-02.htm";
			}
			else
			{
				st.takeItems(SPINE, 1);
				st.takeItems(SKULL, 1);
				st.takeItems(ARM_BONE, 1);
				st.takeItems(RIB_BONE, 1);
				st.takeItems(THIGH_BONE, 1);
				
				if (Rnd.get(10) < 9)
				{
					st.giveItems(COMPLETE_SKELETON, 1);
				}
				else
				{
					htmltext = "30342-04.htm";
				}
			}
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
				htmltext = (player.getLevel() < 15) ? "30336-01.htm" : "30336-02.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case CURTIS:
						htmltext = (!st.hasQuestItems(ANATOMY_DIAGRAM)) ? "30336-04.htm" : "30336-05.htm";
						break;
					
					case SAMED:
						if (!st.hasQuestItems(ANATOMY_DIAGRAM))
						{
							htmltext = "30434-01.htm";
						}
						else
						{
							if (getNumberOfPieces(st) == 0)
							{
								htmltext = "30434-04.htm";
							}
							else
							{
								htmltext = (!st.hasQuestItems(COMPLETE_SKELETON)) ? "30434-05.htm" : "30434-08.htm";
							}
						}
						break;
					
					case VARSAK:
						htmltext = "30342-01.htm";
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
		
		if (st.hasQuestItems(ANATOMY_DIAGRAM))
		{
			final int chance = Rnd.get(100);
			for (ItemHolder drop : DROPLIST.get(npc.getNpcId()))
			{
				if (chance < drop.getCount())
				{
					st.dropItemsAlways(drop.getId(), 1, 0);
					break;
				}
			}
		}
		
		return null;
	}
}