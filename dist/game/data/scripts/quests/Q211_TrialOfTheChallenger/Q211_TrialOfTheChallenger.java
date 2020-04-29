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
package quests.Q211_TrialOfTheChallenger;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;

public class Q211_TrialOfTheChallenger extends Quest
{
	// Items
	private static final int LETTER_OF_KASH = 2628;
	private static final int WATCHER_EYE_1 = 2629;
	private static final int WATCHER_EYE_2 = 2630;
	private static final int SCROLL_OF_SHYSLASSYS = 2631;
	private static final int BROKEN_KEY = 2632;
	
	// Rewards
	private static final int ADENA = 57;
	private static final int ELVEN_NECKLACE_BEADS = 1904;
	private static final int WHITE_TUNIC_PATTERN = 1936;
	private static final int IRON_BOOTS_DESIGN = 1940;
	private static final int MANTICOR_SKIN_GAITERS_PATTERN = 1943;
	private static final int RIP_GAUNTLETS_PATTERN = 1946;
	private static final int TOME_OF_BLOOD_PAGE = 2030;
	private static final int MITHRIL_SCALE_GAITERS_MATERIAL = 2918;
	private static final int BRIGANDINE_GAUNTLETS_PATTERN = 2927;
	private static final int MARK_OF_CHALLENGER = 2627;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int FILAUR = 30535;
	private static final int KASH = 30644;
	private static final int MARTIEN = 30645;
	private static final int RALDO = 30646;
	private static final int CHEST_OF_SHYSLASSYS = 30647;
	
	// Monsters
	private static final int SHYSLASSYS = 27110;
	private static final int GORR = 27112;
	private static final int BARAHAM = 27113;
	private static final int SUCCUBUS_QUEEN = 27114;
	
	public Q211_TrialOfTheChallenger()
	{
		super(211, "Trial of the Challenger");
		
		registerQuestItems(LETTER_OF_KASH, WATCHER_EYE_1, WATCHER_EYE_2, SCROLL_OF_SHYSLASSYS, BROKEN_KEY);
		
		addStartNpc(KASH);
		addTalkId(FILAUR, KASH, MARTIEN, RALDO, CHEST_OF_SHYSLASSYS);
		
		addKillId(SHYSLASSYS, GORR, BARAHAM, SUCCUBUS_QUEEN);
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
		
		// KASH
		if (event.equals("30644-05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			
			if (!player.getVariables().getBoolean("secondClassChange35", false))
			{
				htmltext = "30644-05a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35.get(player.getClassId().getId()));
				player.getVariables().set("secondClassChange35", true);
			}
		}
		// MARTIEN
		else if (event.equals("30645-02.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_OF_KASH, 1);
		}
		// RALDO
		else if (event.equals("30646-04.htm") || event.equals("30646-06.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(WATCHER_EYE_2, 1);
		}
		// CHEST_OF_SHYSLASSYS
		else if (event.equals("30647-04.htm"))
		{
			if (st.hasQuestItems(BROKEN_KEY))
			{
				if (Rnd.get(10) < 2)
				{
					htmltext = "30647-03.htm";
					st.playSound(QuestState.SOUND_JACKPOT);
					st.takeItems(BROKEN_KEY, 1);
					final int chance = Rnd.get(100);
					if (chance > 90)
					{
						st.rewardItems(BRIGANDINE_GAUNTLETS_PATTERN, 1);
						st.rewardItems(IRON_BOOTS_DESIGN, 1);
						st.rewardItems(MANTICOR_SKIN_GAITERS_PATTERN, 1);
						st.rewardItems(MITHRIL_SCALE_GAITERS_MATERIAL, 1);
						st.rewardItems(RIP_GAUNTLETS_PATTERN, 1);
					}
					else if (chance > 70)
					{
						st.rewardItems(ELVEN_NECKLACE_BEADS, 1);
						st.rewardItems(TOME_OF_BLOOD_PAGE, 1);
					}
					else if (chance > 40)
					{
						st.rewardItems(WHITE_TUNIC_PATTERN, 1);
					}
					else
					{
						st.rewardItems(IRON_BOOTS_DESIGN, 1);
					}
				}
				else
				{
					htmltext = "30647-02.htm";
					st.takeItems(BROKEN_KEY, 1);
					st.rewardItems(ADENA, Rnd.get(1, 1000));
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
				if ((player.getClassId() != ClassId.WARRIOR) && (player.getClassId() != ClassId.ELVEN_KNIGHT) && (player.getClassId() != ClassId.PALUS_KNIGHT) && (player.getClassId() != ClassId.ORC_RAIDER) && (player.getClassId() != ClassId.ORC_MONK))
				{
					htmltext = "30644-02.htm";
				}
				else if (player.getLevel() < 35)
				{
					htmltext = "30644-01.htm";
				}
				else
				{
					htmltext = "30644-03.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case KASH:
						if (cond == 1)
						{
							htmltext = "30644-06.htm";
						}
						else if (cond == 2)
						{
							htmltext = "30644-07.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SCROLL_OF_SHYSLASSYS, 1);
							st.giveItems(LETTER_OF_KASH, 1);
						}
						else if (cond == 3)
						{
							htmltext = "30644-08.htm";
						}
						else if (cond > 3)
						{
							htmltext = "30644-09.htm";
						}
						break;
					
					case CHEST_OF_SHYSLASSYS:
						htmltext = "30647-01.htm";
						break;
					
					case MARTIEN:
						if (cond == 3)
						{
							htmltext = "30645-01.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30645-03.htm";
						}
						else if (cond == 5)
						{
							htmltext = "30645-04.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(WATCHER_EYE_1, 1);
						}
						else if (cond == 6)
						{
							htmltext = "30645-05.htm";
						}
						else if (cond == 7)
						{
							htmltext = "30645-07.htm";
						}
						else if (cond > 7)
						{
							htmltext = "30645-06.htm";
						}
						break;
					
					case RALDO:
						if (cond == 7)
						{
							htmltext = "30646-01.htm";
						}
						else if (cond == 8)
						{
							htmltext = "30646-06a.htm";
						}
						else if (cond == 10)
						{
							htmltext = "30646-07.htm";
							st.takeItems(BROKEN_KEY, 1);
							st.giveItems(MARK_OF_CHALLENGER, 1);
							st.rewardExpAndSp(72394, 11250);
							player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case FILAUR:
						if (cond == 8)
						{
							if (player.getLevel() >= 36)
							{
								htmltext = "30535-01.htm";
								st.set("cond", "9");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								htmltext = "30535-03.htm";
							}
						}
						else if (cond == 9)
						{
							htmltext = "30535-02.htm";
							st.addRadar(176560, -184969, -3729);
						}
						else if (cond == 10)
						{
							htmltext = "30535-04.htm";
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
			case SHYSLASSYS:
				if (st.getInt("cond") == 1)
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(BROKEN_KEY, 1);
					st.giveItems(SCROLL_OF_SHYSLASSYS, 1);
					addSpawn(CHEST_OF_SHYSLASSYS, npc, false, 200000);
				}
				break;
			
			case GORR:
				if ((st.getInt("cond") == 4) && st.dropItemsAlways(WATCHER_EYE_1, 1, 1))
				{
					st.set("cond", "5");
				}
				break;
			
			case BARAHAM:
				if ((st.getInt("cond") == 6) && st.dropItemsAlways(WATCHER_EYE_2, 1, 1))
				{
					st.set("cond", "7");
				}
				addSpawn(RALDO, npc, false, 100000);
				break;
			
			case SUCCUBUS_QUEEN:
				if (st.getInt("cond") == 9)
				{
					st.set("cond", "10");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				addSpawn(RALDO, npc, false, 100000);
				break;
		}
		
		return null;
	}
}