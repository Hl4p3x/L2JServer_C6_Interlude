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
package quests.Q123_TheLeaderAndTheFollower;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import quests.Q118_ToLeadAndBeLed.Q118_ToLeadAndBeLed;

public class Q123_TheLeaderAndTheFollower extends Quest
{
	// NPC
	private static final int NEWYEAR = 31961;
	
	// Mobs
	private static final int BRUIN_LIZARDMAN = 27321;
	private static final int PICOT_ARENEID = 27322;
	
	// Items
	private static final int BRUIN_LIZARDMAN_BLOOD = 8549;
	private static final int PICOT_ARANEID_LEG = 8550;
	private static final int CRYSTAL_D = 1458;
	
	// Rewards
	private static final int CLAN_OATH_HELM = 7850;
	private static final int CLAN_OATH_ARMOR = 7851;
	private static final int CLAN_OATH_GAUNTLETS = 7852;
	private static final int CLAN_OATH_SABATON = 7853;
	private static final int CLAN_OATH_BRIGANDINE = 7854;
	private static final int CLAN_OATH_LEATHER_GLOVES = 7855;
	private static final int CLAN_OATH_BOOTS = 7856;
	private static final int CLAN_OATH_AKETON = 7857;
	private static final int CLAN_OATH_PADDED_GLOVES = 7858;
	private static final int CLAN_OATH_SANDALS = 7859;
	
	public Q123_TheLeaderAndTheFollower()
	{
		super(123, "The Leader and the Follower");
		
		registerQuestItems(BRUIN_LIZARDMAN_BLOOD, PICOT_ARANEID_LEG);
		
		addStartNpc(NEWYEAR);
		addTalkId(NEWYEAR);
		
		addKillId(BRUIN_LIZARDMAN, PICOT_ARENEID);
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
		
		if (event.equals("31961-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.set("state", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31961-05d.htm"))
		{
			if (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) > 9)
			{
				st.set("cond", "3");
				st.set("state", "2");
				st.set("stateEx", "1");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BRUIN_LIZARDMAN_BLOOD, -1);
			}
		}
		else if (event.equals("31961-05e.htm"))
		{
			if (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) > 9)
			{
				st.set("cond", "4");
				st.set("state", "2");
				st.set("stateEx", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BRUIN_LIZARDMAN_BLOOD, -1);
			}
		}
		else if (event.equals("31961-05f.htm"))
		{
			if (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) > 9)
			{
				st.set("cond", "5");
				st.set("state", "2");
				st.set("stateEx", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BRUIN_LIZARDMAN_BLOOD, -1);
			}
		}
		else if (event.equals("31961-10.htm"))
		{
			final PlayerInstance academic = getApprentice(player);
			if (academic != null)
			{
				final QuestState st2 = academic.getQuestState(getName());
				if ((st2 != null) && (st2.getInt("state") == 2))
				{
					final int stateEx = st2.getInt("stateEx");
					if (stateEx == 1)
					{
						if (st.getQuestItemsCount(CRYSTAL_D) > 921)
						{
							st.takeItems(CRYSTAL_D, 922);
							st2.set("cond", "6");
							st2.set("state", "3");
							st2.playSound(QuestState.SOUND_MIDDLE);
						}
						else
						{
							htmltext = "31961-11.htm";
						}
					}
					else
					{
						if (st.getQuestItemsCount(CRYSTAL_D) > 770)
						{
							st.takeItems(CRYSTAL_D, 771);
							st2.set("cond", "6");
							st2.set("state", "3");
							st2.playSound(QuestState.SOUND_MIDDLE);
						}
						else
						{
							htmltext = "31961-11a.htm";
						}
					}
				}
			}
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
				if (player.getSponsor() > 0)
				{
					final QuestState st2 = player.getQuestState(Q118_ToLeadAndBeLed.class.getSimpleName());
					if (st2 != null)
					{
						htmltext = (st2.isCompleted()) ? "31961-02a.htm" : "31961-02b.htm";
					}
					else
					{
						htmltext = (player.getLevel() > 18) ? "31961-01.htm" : "31961-02.htm";
					}
				}
				else if (player.getApprentice() > 0)
				{
					final PlayerInstance academic = getApprentice(player);
					if (academic != null)
					{
						final QuestState st3 = academic.getQuestState(getName());
						if (st3 != null)
						{
							final int state = st3.getInt("state");
							if (state == 2)
							{
								htmltext = "31961-08.htm";
							}
							else if (state == 3)
							{
								htmltext = "31961-12.htm";
							}
							else
							{
								htmltext = "31961-14.htm";
							}
						}
					}
					else
					{
						htmltext = "31961-09.htm";
					}
				}
				break;
			
			case State.STARTED:
				final int state = st.getInt("state");
				if (state == 1)
				{
					htmltext = (st.getQuestItemsCount(BRUIN_LIZARDMAN_BLOOD) < 10) ? "31961-04.htm" : "31961-05.htm";
				}
				else if (state == 2)
				{
					final int stateEx = st.getInt("stateEx");
					if (player.getSponsor() == 0)
					{
						if (stateEx == 1)
						{
							htmltext = "31961-06a.htm";
						}
						else if (stateEx == 2)
						{
							htmltext = "31961-06b.htm";
						}
						else if (stateEx == 3)
						{
							htmltext = "31961-06c.htm";
						}
					}
					else
					{
						if (!getSponsor(player))
						{
							if (stateEx == 1)
							{
								htmltext = "31961-06.htm";
							}
							else if (stateEx == 2)
							{
								htmltext = "31961-06d.htm";
							}
							else if (stateEx == 3)
							{
								htmltext = "31961-06e.htm";
							}
						}
						else
						{
							htmltext = "31961-07.htm";
						}
					}
				}
				else if (state == 3)
				{
					st.set("cond", "7");
					st.set("state", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
					htmltext = "31961-15.htm";
				}
				else if (state == 4)
				{
					if (st.getQuestItemsCount(PICOT_ARANEID_LEG) > 7)
					{
						htmltext = "31961-17.htm";
						
						st.takeItems(PICOT_ARANEID_LEG, -1);
						st.giveItems(CLAN_OATH_HELM, 1);
						
						switch (st.getInt("stateEx"))
						{
							case 1:
								st.giveItems(CLAN_OATH_ARMOR, 1);
								st.giveItems(CLAN_OATH_GAUNTLETS, 1);
								st.giveItems(CLAN_OATH_SABATON, 1);
								break;
							
							case 2:
								st.giveItems(CLAN_OATH_BRIGANDINE, 1);
								st.giveItems(CLAN_OATH_LEATHER_GLOVES, 1);
								st.giveItems(CLAN_OATH_BOOTS, 1);
								break;
							
							case 3:
								st.giveItems(CLAN_OATH_AKETON, 1);
								st.giveItems(CLAN_OATH_PADDED_GLOVES, 1);
								st.giveItems(CLAN_OATH_SANDALS, 1);
								break;
						}
						
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(false);
					}
					else
					{
						htmltext = "31961-16.htm";
					}
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
		
		if (player.getSponsor() == 0)
		{
			st.exitQuest(true);
			return null;
		}
		
		final int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case BRUIN_LIZARDMAN:
				if ((cond == 1) && st.dropItems(BRUIN_LIZARDMAN_BLOOD, 1, 10, 700000))
				{
					st.set("cond", "2");
				}
				break;
			
			case PICOT_ARENEID:
				if ((cond == 7) && getSponsor(player) && st.dropItems(PICOT_ARANEID_LEG, 1, 8, 700000))
				{
					st.set("cond", "8");
				}
				break;
		}
		
		return null;
	}
}