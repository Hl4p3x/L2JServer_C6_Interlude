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
package quests.Q021_HiddenTruth;

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q021_HiddenTruth extends Quest
{
	// NPCs
	private static final int MYSTERIOUS_WIZARD = 31522;
	private static final int TOMBSTONE = 31523;
	private static final int VON_HELLMAN_DUKE = 31524;
	private static final int VON_HELLMAN_PAGE = 31525;
	private static final int BROKEN_BOOKSHELF = 31526;
	private static final int AGRIPEL = 31348;
	private static final int DOMINIC = 31350;
	private static final int BENEDICT = 31349;
	private static final int INNOCENTIN = 31328;
	
	// Items
	private static final int CROSS_OF_EINHASAD = 7140;
	private static final int CROSS_OF_EINHASAD_NEXT_QUEST = 7141;
	
	private static final Location[] PAGE_LOCS =
	{
		new Location(51992, -54424, -3160),
		new Location(52328, -53400, -3160),
		new Location(51928, -51656, -3096)
	};
	
	private NpcInstance _duke;
	private NpcInstance _page;
	
	public Q021_HiddenTruth()
	{
		super(21, "Hidden Truth");
		
		registerQuestItems(CROSS_OF_EINHASAD);
		
		addStartNpc(MYSTERIOUS_WIZARD);
		addTalkId(MYSTERIOUS_WIZARD, TOMBSTONE, VON_HELLMAN_DUKE, VON_HELLMAN_PAGE, BROKEN_BOOKSHELF, AGRIPEL, DOMINIC, BENEDICT, INNOCENTIN);
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
		
		if (event.equals("31522-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("31523-03.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			spawnTheDuke(player);
		}
		else if (event.equals("31524-06.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			spawnThePage(player);
		}
		else if (event.equals("31526-08.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equals("31526-14.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CROSS_OF_EINHASAD, 1);
		}
		else if (event.equals("1"))
		{
			_page.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, PAGE_LOCS[0]);
			_page.broadcastNpcSay("Follow me...");
			startQuestTimer("2", 5000, _page, player, false);
			return null;
		}
		else if (event.equals("2"))
		{
			_page.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, PAGE_LOCS[1]);
			startQuestTimer("3", 12000, _page, player, false);
			return null;
		}
		else if (event.equals("3"))
		{
			_page.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, PAGE_LOCS[2]);
			startQuestTimer("4", 18000, _page, player, false);
			return null;
		}
		else if (event.equals("4"))
		{
			st.set("end_walk", "1");
			_page.broadcastNpcSay("Please check this bookcase, " + player.getName() + ".");
			startQuestTimer("5", 47000, _page, player, false);
			return null;
		}
		else if (event.equals("5"))
		{
			_page.broadcastNpcSay("I'm confused! Maybe it's time to go back.");
			return null;
		}
		else if (event.equals("31328-05.htm"))
		{
			if (st.hasQuestItems(CROSS_OF_EINHASAD))
			{
				st.takeItems(CROSS_OF_EINHASAD, 1);
				st.giveItems(CROSS_OF_EINHASAD_NEXT_QUEST, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		else if (event.equals("dukeDespawn"))
		{
			_duke.deleteMe();
			_duke = null;
			return null;
		}
		else if (event.equals("pageDespawn"))
		{
			_page.deleteMe();
			_page = null;
			return null;
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
				htmltext = (player.getLevel() < 63) ? "31522-03.htm" : "31522-01.htm";
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MYSTERIOUS_WIZARD:
						htmltext = "31522-05.htm";
						break;
					
					case TOMBSTONE:
						if (cond == 1)
						{
							htmltext = "31523-01.htm";
						}
						else if ((cond == 2) || (cond == 3))
						{
							htmltext = "31523-04.htm";
							spawnTheDuke(player);
						}
						else if (cond > 3)
						{
							htmltext = "31523-04.htm";
						}
						break;
					
					case VON_HELLMAN_DUKE:
						if (cond == 2)
						{
							htmltext = "31524-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "31524-07.htm";
							spawnThePage(player);
						}
						else if (cond > 3)
						{
							htmltext = "31524-07a.htm";
						}
						break;
					
					case VON_HELLMAN_PAGE:
						if (cond == 3)
						{
							if (st.getInt("end_walk") == 1)
							{
								htmltext = "31525-02.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								htmltext = "31525-01.htm";
							}
						}
						else if (cond == 4)
						{
							htmltext = "31525-02.htm";
						}
						break;
					
					case BROKEN_BOOKSHELF:
						if (((cond == 3) && (st.getInt("end_walk") == 1)) || (cond == 4))
						{
							htmltext = "31526-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							
							if (_page != null)
							{
								cancelQuestTimer("5", _page, player);
								cancelQuestTimer("pageDespawn", _page, player);
								_page.deleteMe();
								_page = null;
							}
							
							if (_duke != null)
							{
								cancelQuestTimer("dukeDespawn", _duke, player);
								_duke.deleteMe();
								_duke = null;
							}
						}
						else if (cond == 5)
						{
							htmltext = "31526-10.htm";
						}
						else if (cond > 5)
						{
							htmltext = "31526-15.htm";
						}
						break;
					
					case AGRIPEL:
					case BENEDICT:
					case DOMINIC:
						if (((cond == 6) || (cond == 7)) && st.hasQuestItems(CROSS_OF_EINHASAD))
						{
							final int npcId = npc.getNpcId();
							
							// For cond 6, make checks until cond 7 is activated.
							if (cond == 6)
							{
								int npcId1 = 0;
								int npcId2 = 0;
								if (npcId == AGRIPEL)
								{
									npcId1 = BENEDICT;
									npcId2 = DOMINIC;
								}
								else if (npcId == BENEDICT)
								{
									npcId1 = AGRIPEL;
									npcId2 = DOMINIC;
								}
								else if (npcId == DOMINIC)
								{
									npcId1 = AGRIPEL;
									npcId2 = BENEDICT;
								}
								
								if ((st.getInt(String.valueOf(npcId1)) == 1) && (st.getInt(String.valueOf(npcId2)) == 1))
								{
									st.set("cond", "7");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
								{
									st.set(String.valueOf(npcId), "1");
								}
							}
							
							htmltext = npcId + "-01.htm";
						}
						break;
					
					case INNOCENTIN:
						if ((cond == 7) && st.hasQuestItems(CROSS_OF_EINHASAD))
						{
							htmltext = "31328-01.htm";
						}
						break;
				}
				break;
			
			case State.COMPLETED:
				if (npc.getNpcId() == INNOCENTIN)
				{
					htmltext = "31328-06.htm";
				}
				else
				{
					htmltext = getAlreadyCompletedMsg();
				}
				break;
		}
		
		return htmltext;
	}
	
	private void spawnTheDuke(PlayerInstance player)
	{
		if (_duke == null)
		{
			_duke = addSpawn(VON_HELLMAN_DUKE, 51432, -54570, -3136, 0, false, 0);
			_duke.broadcastNpcSay("Who awoke me?");
			startQuestTimer("dukeDespawn", 300000, _duke, player, false);
		}
	}
	
	private void spawnThePage(PlayerInstance player)
	{
		if (_page == null)
		{
			_page = addSpawn(VON_HELLMAN_PAGE, 51608, -54520, -3168, 0, false, 0);
			_page.broadcastNpcSay("My master has instructed me to be your guide, " + player.getName() + ".");
			startQuestTimer("1", 4000, _page, player, false);
			startQuestTimer("pageDespawn", 90000, _page, player, false);
		}
	}
}