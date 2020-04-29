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
package ai.others.NewbieHelper;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

import ai.others.Tutorial.Tutorial;

public class NewbieHelper extends Quest
{
	// Quest Items
	// Human
	private static final int RECOMMENDATION_01 = 1067;
	private static final int RECOMMENDATION_02 = 1068;
	// Elf
	private static final int LEAF_OF_MOTHERTREE = 1069;
	// Dark Elf
	private static final int BLOOD_OF_JUNDIN = 1070;
	// Dwarf
	private static final int LICENSE_OF_MINER = 1498;
	// Orc
	private static final int VOUCHER_OF_FLAME = 1496;
	
	// Items Reward
	private static final int SOULSHOT_NOVICE = 5789;
	private static final int SPIRITSHOT_NOVICE = 5790;
	private static final int BLUE_GEM = 6353;
	private static final int TOKEN = 8542;
	private static final int SCROLL = 8594;
	
	private static final Map<String, Event> _events = new HashMap<>();
	static
	{
		_events.put("30008_02", new Event("30008-03.htm", -84058, 243239, -3730, RECOMMENDATION_01, 0x00, SOULSHOT_NOVICE, 200, 0x00, 0, 0));
		_events.put("30017_02", new Event("30017-03.htm", -84058, 243239, -3730, RECOMMENDATION_02, 0x0a, SPIRITSHOT_NOVICE, 100, 0x00, 0, 0));
		_events.put("30129_02", new Event("30129-03.htm", 12116, 16666, -4610, BLOOD_OF_JUNDIN, 0x26, SPIRITSHOT_NOVICE, 100, 0x1f, SOULSHOT_NOVICE, 200));
		_events.put("30370_02", new Event("30370-03.htm", 45491, 48359, -3086, LEAF_OF_MOTHERTREE, 0x19, SPIRITSHOT_NOVICE, 100, 0x12, SOULSHOT_NOVICE, 200));
		_events.put("30528_02", new Event("30528-03.htm", 115642, -178046, -941, LICENSE_OF_MINER, 0x35, SOULSHOT_NOVICE, 200, 0x00, 0, 0));
		_events.put("30573_02", new Event("30573-03.htm", -45067, -113549, -235, VOUCHER_OF_FLAME, 0x31, SOULSHOT_NOVICE, 200, 0x2c, SOULSHOT_NOVICE, 200));
	}
	
	// @formatter:off
	private static final Map<Integer, Talk> _talks = new HashMap<>();
	static
	{
		// Grand Master - Roien - Human
		_talks.put(30008, new Talk(0, new String[]{"30008-01.htm", "30008-02.htm", "30008-04.htm"}, 0, 0));
		_talks.put(30009, new Talk(0, new String[]{"newbiehelper_fig_01.htm", "30009-03.htm", "", "30009-04.htm"}, 1, RECOMMENDATION_01));
		// Grand Master - Gallint - Human
		_talks.put(30017, new Talk(0, new String[]{"30017-01.htm", "30017-02.htm", "30017-04.htm"}, 0, 0));
		_talks.put(30019, new Talk(0, new String[]{"newbiehelper_fig_01.htm", "", "30019-03a.htm", "30019-04.htm"}, 1, RECOMMENDATION_02));
		// Hierarch - Dark Elf
		_talks.put(30129, new Talk(2, new String[]{"30129-01.htm", "30129-02.htm", "30129-04.htm"}, 0, 0));
		_talks.put(30131, new Talk(2, new String[]{"newbiehelper_fig_01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm"}, 1, BLOOD_OF_JUNDIN));
		// Nerupa - Elf
		_talks.put(30370, new Talk(1, new String[]{"30370-01.htm", "30370-02.htm", "30370-04.htm"}, 0, 0));
		_talks.put(30400, new Talk(1, new String[]{"newbiehelper_fig_01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
		// Foreman - Dwarf
		_talks.put(30528, new Talk(4, new String[]{"30528-01.htm", "30528-02.htm", "30528-04.htm"}, 0, 0));
		_talks.put(30530, new Talk(4, new String[]{"newbiehelper_fig_01.htm", "30530-03.htm", "", "30530-04.htm"}, 1, LICENSE_OF_MINER));
		// Flame Guardian - Orc
		_talks.put(30573, new Talk(3, new String[]{"30573-01.htm", "30573-02.htm", "30573-04.htm"}, 0, 0));
		_talks.put(30575, new Talk(3, new String[]{"newbiehelper_fig_01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm"}, 1, VOUCHER_OF_FLAME));
	}
	// @formatter:on
	
	public NewbieHelper()
	{
		super(-1, "ai/others");
		
		addStartNpc(30009, 30019, 30131, 30400, 30530, 30575);
		
		addTalkId(30009, 30019, 30131, 30400, 30530, 30575, 30008, 30017, 30129, 30370, 30528, 30573);
		
		addFirstTalkId(new int[]
		{
			30009, // Newbie Helper - Human
			30019, // Newbie Helper - Human
			30131, // Newbie Helper - Dark Elf
			30400, // Newbie Helper - Elf
			30530, // Newbie Helper - Dwarf
			30575, // Newbie Helper - Orc
			
			30598, // Newbie Guide
			30599, // Newbie Guide
			30600, // Newbie Guide
			30601, // Newbie Guide
			30602, // Newbie Guide
			
			30008, // Grand Master - Roien - Human
			30017, // Grand Master - Gallint - Human
			30129, // Hierarch - Dark Elf
			30370, // Nerupa - Elf
			30528, // Foreman - Dwarf
			30573 // Flame Guardian - Orc
		});
		
		addKillId(18342);
	}
	
	private static class Talk
	{
		int _raceId;
		String[] _htmlfiles;
		int _npcType;
		int _item;
		
		public Talk(int raceId, String[] htmlfiles, int npcTyp, int item)
		{
			_raceId = raceId;
			_htmlfiles = htmlfiles;
			_npcType = npcTyp;
			_item = item;
		}
	}
	
	private static class Event
	{
		String _htmlfile;
		int _radarX;
		int _radarY;
		int _radarZ;
		int _item;
		int _classId1;
		int _gift1;
		int _count1;
		int _classId2;
		int _gift2;
		int _count2;
		
		public Event(String htmlfile, int x, int y, int z, int item, int classId1, int gift1, int count1, int classId2, int gift2, int count2)
		{
			_htmlfile = htmlfile;
			_radarX = x;
			_radarY = y;
			_radarZ = z;
			_item = item;
			_classId1 = classId1;
			_gift1 = gift1;
			_count1 = count1;
			_classId2 = classId2;
			_gift2 = gift2;
			_count2 = count2;
		}
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final QuestState qs1 = player.getQuestState(getName());
		final QuestState qs2 = player.getQuestState(Tutorial.class.getSimpleName());
		if ((qs1 == null) || (qs2 == null))
		{
			return null;
		}
		
		String htmltext = event;
		player = qs1.getPlayer();
		
		final int ex = qs2.getInt("Ex");
		final int classId = qs1.getPlayer().getClassId().getId();
		if (event.equalsIgnoreCase("TimerEx_NewbieHelper"))
		{
			if (ex == 0)
			{
				qs1.playTutorialVoice(player.isMageClass() ? "tutorial_voice_009b" : "tutorial_voice_009a");
				qs2.set("Ex", "1");
			}
			else if (ex == 3)
			{
				qs1.playTutorialVoice("tutorial_voice_010a");
				qs2.set("Ex", "4");
			}
			return null;
		}
		else if (event.equalsIgnoreCase("TimerEx_GrandMaster"))
		{
			if (ex >= 4)
			{
				qs1.showQuestionMark(7);
				qs1.playSound("ItemSound.quest_tutorial");
				qs1.playTutorialVoice("tutorial_voice_025");
			}
			return null;
		}
		else
		{
			final Event ev = _events.get(event);
			if (ev != null)
			{
				if (ev._radarX != 0)
				{
					qs1.addRadar(ev._radarX, ev._radarY, ev._radarZ);
				}
				htmltext = ev._htmlfile;
				if ((qs1.getQuestItemsCount(ev._item) == 1) && (qs1.getInt("onlyone") == 0))
				{
					qs1.rewardExpAndSp(0, 50);
					startQuestTimer("TimerEx_GrandMaster", 60000, null, player, false);
					qs1.takeItems(ev._item, 1);
					if (ex <= 3)
					{
						qs2.set("Ex", "4");
					}
					if (classId == ev._classId1)
					{
						qs1.giveItems(ev._gift1, ev._count1);
						qs1.playTutorialVoice(ev._gift1 == SPIRITSHOT_NOVICE ? "tutorial_voice_027" : "tutorial_voice_026");
					}
					else if ((classId == ev._classId2) && (ev._gift2 != 0))
					{
						qs1.giveItems(ev._gift2, ev._count2);
						qs1.playTutorialVoice("tutorial_voice_026");
					}
					qs1.unset("step");
					qs1.set("onlyone", "1");
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = "";
		QuestState qs1 = player.getQuestState(getName());
		final QuestState qs2 = player.getQuestState(Tutorial.class.getSimpleName());
		if (qs1 == null)
		{
			qs1 = newQuestState(player);
		}
		
		if ((qs2 == null) || Config.DISABLE_TUTORIAL)
		{
			npc.showChatWindow(player);
			return null;
		}
		
		final int npcId = npc.getNpcId();
		final int level = player.getLevel();
		final boolean isMage = player.isMageClass();
		final boolean isOrcMage = player.getClassId().getId() == 49;
		int npcType = 0;
		int raceId = 0;
		int item = 0;
		String[] htmlfiles = {};
		final Talk talk = _talks.get(npcId);
		try
		{
			if (talk != null)
			{
				raceId = talk._raceId;
				htmlfiles = talk._htmlfiles;
				npcType = talk._npcType;
				item = talk._item;
			}
			if (((level >= 10) || (qs1.getInt("onlyone") == 1)) && (npcType == 1))
			{
				htmltext = "newbiehelper_03.htm";
			}
			else if ((qs1.getInt("onlyone") == 0) && (level < 10))
			{
				if (player.getRace().ordinal() == raceId)
				{
					htmltext = htmlfiles[0];
					if (npcType == 1)
					{
						if ((qs1.getInt("step") == 0) && (qs2.get("Ex") == null))
						{
							qs2.set("Ex", "0");
							qs1.set("step", "1");
							startQuestTimer("TimerEx_NewbieHelper", 30000, null, player, false);
							htmltext = !isMage ? "newbiehelper_fig_01.htm" : isOrcMage ? "newbiehelper_mage_01a.htm" : "newbiehelper_mage_01.htm";
							qs1.setState(State.STARTED);
						}
						else if ((qs1.getInt("step") == 1) && (qs2.getInt("Ex") <= 2) && (qs1.getQuestItemsCount(item) == 0))
						{
							if (qs1.hasAtLeastOneQuestItem(BLUE_GEM))
							{
								qs1.takeItems(BLUE_GEM, -1);
								qs1.giveItems(item, 1);
								qs1.set("step", "2");
								qs2.set("ucMemo", "3");
								qs2.set("Ex", "3");
								startQuestTimer("TimerEx_NewbieHelper", 30000, null, player, false);
								if (isMage && !isOrcMage)
								{
									qs1.playTutorialVoice("tutorial_voice_027");
									qs1.giveItems(SPIRITSHOT_NOVICE, 100);
									htmltext = htmlfiles[2];
									if (htmltext.equalsIgnoreCase(""))
									{
										htmltext = "<html><body>I am sorry.  I only help warriors.  Please go to another Newbie Helper who may assist you.</body></html>";
									}
								}
								else
								{
									qs1.playTutorialVoice("tutorial_voice_026");
									qs1.giveItems(SOULSHOT_NOVICE, 200);
									htmltext = htmlfiles[1];
									if (htmltext.equalsIgnoreCase(""))
									{
										htmltext = "<html><body>I am sorry.  I only help mystics.  Please go to another Newbie Helper who may assist you.</body></html>";
									}
								}
							}
							else
							{
								htmltext = !isMage ? "newbiehelper_fig_02.htm" : isOrcMage ? "newbiehelper_mage_02a.htm" : "newbiehelper_mage_02.htm";
							}
						}
						else if (qs1.getInt("step") == 2)
						{
							htmltext = htmlfiles[3];
						}
					}
					else if (npcType == 0)
					{
						final int step = qs1.getInt("step");
						if (step == 1)
						{
							htmltext = htmlfiles[0];
						}
						else if (step == 2)
						{
							htmltext = htmlfiles[1];
						}
						else if (step == 3)
						{
							htmltext = htmlfiles[2];
						}
					}
				}
			}
			else if ((npcId >= 30598) && (npcId <= 30602))
			{
				if ((qs2.getInt("reward") == 0) && (qs1.getInt("onlyone") == 1))
				{
					qs1.playTutorialVoice(isMage && !isOrcMage ? "tutorial_voice_027" : "tutorial_voice_026");
					qs1.giveItems(isMage && !isOrcMage ? SPIRITSHOT_NOVICE : SOULSHOT_NOVICE, isMage && !isOrcMage ? 100 : 200);
					qs1.giveItems(TOKEN, 12);
					if (Rnd.get(100) < 50)
					{
						qs1.giveItems(SCROLL, 2);
					}
					qs2.set("reward", "1");
					qs1.setState(State.COMPLETED);
				}
				npc.showChatWindow(player);
				return null;
			}
			else if ((npcType == 0) && (qs1.getState() == State.COMPLETED))
			{
				htmltext = "" + npcId + "-04.htm";
			}
			if ((htmltext == null) || htmltext.equalsIgnoreCase(""))
			{
				npc.showChatWindow(player);
				return null;
			}
		}
		catch (Exception e)
		{
			// Ignore.
		}
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState qs1 = player.getQuestState(getName());
		final QuestState qs2 = player.getQuestState(Tutorial.class.getSimpleName());
		if ((qs1 == null) || (qs2 == null))
		{
			return null;
		}
		
		final int ex = qs2.getInt("Ex");
		if (ex <= 1)
		{
			qs1.setState(State.STARTED);
			qs2.playTutorialVoice("tutorial_voice_011");
			qs2.showQuestionMark(3);
			qs2.set("Ex", "2");
		}
		else if ((ex <= 2) && (qs1.getState() == State.STARTED) && (qs2.getInt("Gemstone") == 0) && (Rnd.get(100) < 50))
		{
			((MonsterInstance) npc).DropItem(player, BLUE_GEM, 1);
			qs1.playSound("ItemSound.quest_tutorial");
			qs1.set("step", "1");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new NewbieHelper();
	}
}