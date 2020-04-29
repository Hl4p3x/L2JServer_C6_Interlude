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
package quests.Q336_CoinsOfMagic;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

/**
 * Adapted from FirstTeam Interlude
 */
public class Q336_CoinsOfMagic extends Quest
{
	private static final int COIN_DIAGRAM = 3811;
	private static final int KALDIS_COIN = 3812;
	private static final int MEMBERSHIP_1 = 3813;
	private static final int MEMBERSHIP_2 = 3814;
	private static final int MEMBERSHIP_3 = 3815;
	private static final int BLOOD_MEDUSA = 3472;
	private static final int BLOOD_WEREWOLF = 3473;
	private static final int BLOOD_BASILISK = 3474;
	private static final int BLOOD_DREVANUL = 3475;
	private static final int BLOOD_SUCCUBUS = 3476;
	private static final int GOLD_WYVERN = 3482;
	private static final int GOLD_KNIGHT = 3483;
	private static final int GOLD_GIANT = 3484;
	private static final int GOLD_DRAKE = 3485;
	private static final int GOLD_WYRM = 3486;
	private static final int SILVER_UNICORN = 3490;
	private static final int SILVER_FAIRY = 3491;
	private static final int SILVER_DRYAD = 3492;
	private static final int SILVER_GOLEM = 3494;
	private static final int SILVER_UNDINE = 3495;
	private static final int[] BASIC_COINS =
	{
		BLOOD_MEDUSA,
		GOLD_WYVERN,
		SILVER_UNICORN
	};
	private static final int SORINT = 30232;
	private static final int BERNARD = 30702;
	private static final int PAGE = 30696;
	private static final int HAGGER = 30183;
	private static final int STAN = 30200;
	private static final int RALFORD = 30165;
	private static final int FERRIS = 30847;
	private static final int COLLOB = 30092;
	private static final int PANO = 30078;
	private static final int DUNING = 30688;
	private static final int LORAIN = 30673;
	private static final int TIMAK_ORC_ARCHER = 20584;
	private static final int TIMAK_ORC_SOLDIER = 20585;
	private static final int TIMAK_ORC_SHAMAN = 20587;
	private static final int LAKIN = 20604;
	private static final int TORTURED_UNDEAD = 20678;
	private static final int HATAR_HANISHEE = 20663;
	private static final int SHACKLE = 20235;
	private static final int TIMAK_ORC = 20583;
	private static final int HEADLESS_KNIGHT = 20146;
	private static final int ROYAL_CAVE_SERVANT = 20240;
	private static final int MALRUK_SUCCUBUS_TUREN = 20245;
	private static final int FORMOR = 20568;
	private static final int FORMOR_ELDER = 20569;
	private static final int VANOR_SILENOS_SHAMAN = 20685;
	private static final int TARLK_BUGBEAR_HIGH_WARRIOR = 20572;
	private static final int OEL_MAHUM = 20161;
	private static final int OEL_MAHUM_WARRIOR = 20575;
	private static final int HARIT_LIZARDMAN_MATRIARCH = 20645;
	private static final int HARIT_LIZARDMAN_SHAMAN = 20644;
	private static final int GRAVE_LICH = 21003;
	private static final int DOOM_SERVANT = 21006;
	private static final int DOOM_ARCHER = 21008;
	private static final int DOOM_KNIGHT = 20674;
	private static final int KOOKABURRA2 = 21276;
	private static final int KOOKABURRA3 = 21275;
	private static final int KOOKABURRA4 = 21274;
	private static final int ANTELOPE2 = 21278;
	private static final int ANTELOPE3 = 21279;
	private static final int ANTELOPE4 = 21280;
	private static final int BANDERSNATCH2 = 21282;
	private static final int BANDERSNATCH3 = 21284;
	private static final int BANDERSNATCH4 = 21283;
	private static final int BUFFALO2 = 21287;
	private static final int BUFFALO3 = 21288;
	private static final int BUFFALO4 = 21286;
	private static final int CLAWS_OF_SPLENDOR = 21521;
	private static final int WISDOM_OF_SPLENDOR = 21526;
	private static final int PUNISHMENT_OF_SPLENDOR = 21531;
	private static final int WAILING_OF_SPLENDOR = 21539;
	private static final int HUNGERED_CORPSE = 20954;
	private static final int BLOODY_GHOST = 20960;
	private static final int NIHIL_INVADER = 20957;
	private static final int DARK_GUARD = 20959;
	// @formatter:off
	private static final int[][] PROMOTE =
	{
		new int[0],
		new int[0], {SILVER_DRYAD, BLOOD_BASILISK, BLOOD_SUCCUBUS, SILVER_UNDINE, GOLD_GIANT, GOLD_WYRM}, {BLOOD_WEREWOLF, GOLD_DRAKE, SILVER_FAIRY, BLOOD_DREVANUL, GOLD_KNIGHT, SILVER_GOLEM}
	};
	private static final int[][] EXCHANGE_LEVEL =
	{
		{PAGE, 3}, {LORAIN, 3}, {HAGGER, 3}, {RALFORD, 2}, {STAN, 2}, {DUNING, 2}, {FERRIS, 1}, {COLLOB, 1}, {PANO, 1}
	};
	private static final int[][] DROPLIST =
	{
		{TIMAK_ORC_ARCHER, BLOOD_MEDUSA}, {TIMAK_ORC_SOLDIER, BLOOD_MEDUSA}, {TIMAK_ORC_SHAMAN, 3472}, {LAKIN, 3472}, {TORTURED_UNDEAD, 3472}, {HATAR_HANISHEE, 3472}, {TIMAK_ORC, GOLD_WYVERN}, {SHACKLE, GOLD_WYVERN}, {HEADLESS_KNIGHT, GOLD_WYVERN},
		{ROYAL_CAVE_SERVANT, GOLD_WYVERN}, {MALRUK_SUCCUBUS_TUREN, GOLD_WYVERN}, {FORMOR, SILVER_UNICORN}, {FORMOR_ELDER, SILVER_UNICORN}, {VANOR_SILENOS_SHAMAN, SILVER_UNICORN}, {TARLK_BUGBEAR_HIGH_WARRIOR, SILVER_UNICORN}, {OEL_MAHUM, SILVER_UNICORN}, {OEL_MAHUM_WARRIOR, SILVER_UNICORN}
	};
	private static final int[] MONSTERS =
	{
		GRAVE_LICH, DOOM_SERVANT, DOOM_ARCHER, DOOM_KNIGHT, KOOKABURRA2, KOOKABURRA3, KOOKABURRA4, ANTELOPE2, ANTELOPE3, ANTELOPE4, BANDERSNATCH2, BANDERSNATCH3, BANDERSNATCH4, BUFFALO2, BUFFALO3, BUFFALO4, CLAWS_OF_SPLENDOR, WISDOM_OF_SPLENDOR, PUNISHMENT_OF_SPLENDOR, WAILING_OF_SPLENDOR, HUNGERED_CORPSE, BLOODY_GHOST, NIHIL_INVADER, DARK_GUARD
	};
	// @formatter:on
	
	public Q336_CoinsOfMagic()
	{
		super(336, "Coins of Magic");
		addStartNpc(SORINT);
		addTalkId(SORINT, BERNARD, PAGE, HAGGER, STAN, RALFORD, FERRIS, COLLOB, PANO, DUNING, LORAIN);
		for (int[] mob : DROPLIST)
		{
			addKillId(mob[0]);
		}
		addKillId(MONSTERS);
		addKillId(HARIT_LIZARDMAN_MATRIARCH);
		addKillId(HARIT_LIZARDMAN_SHAMAN);
		registerQuestItems(COIN_DIAGRAM, KALDIS_COIN, MEMBERSHIP_1, MEMBERSHIP_2, MEMBERSHIP_3);
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
		
		final int cond = st.getInt("cond");
		if ("30702-06.htm".equalsIgnoreCase(event))
		{
			if (cond < 7)
			{
				st.set("cond", "7");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if ("30232-22.htm".equalsIgnoreCase(event))
		{
			if (cond < 6)
			{
				st.set("cond", "6");
			}
		}
		else if ("30232-23.htm".equalsIgnoreCase(event))
		{
			if (cond < 5)
			{
				st.set("cond", "5");
			}
		}
		else if ("30702-02.htm".equalsIgnoreCase(event))
		{
			st.set("cond", "2");
		}
		else if ("30232-05.htm".equalsIgnoreCase(event))
		{
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(COIN_DIAGRAM, 1);
			st.set("cond", "1");
		}
		else if ("30232-04.htm".equalsIgnoreCase(event) || "30232-18a.htm".equalsIgnoreCase(event))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_giveup");
		}
		else if ("raise".equalsIgnoreCase(event))
		{
			htmltext = promote(st);
		}
		return htmltext;
	}
	
	private String promote(QuestState st)
	{
		final int grade = st.getInt("grade");
		String html;
		if (grade == 1)
		{
			html = "30232-15.htm";
		}
		else
		{
			int h = 0;
			for (int i : PROMOTE[grade])
			{
				if (st.getQuestItemsCount(i) > 0)
				{
					++h;
				}
			}
			if (h == 6)
			{
				for (int i : PROMOTE[grade])
				{
					st.takeItems(i, 1);
				}
				html = "30232-" + (19 - grade) + ".htm";
				st.takeItems(KALDIS_COIN + grade, -1);
				st.giveItems(COIN_DIAGRAM + grade, 1);
				st.set("grade", "" + (grade - 1));
				if (grade == 3)
				{
					st.set("cond", "9");
				}
				else if (grade == 2)
				{
					st.set("cond", "11");
				}
				st.playSound("ItemSound.quest_fanfare_middle");
			}
			else
			{
				html = "30232-" + (16 - grade) + ".htm";
				if (grade == 3)
				{
					st.set("cond", "8");
				}
				else if (grade == 2)
				{
					st.set("cond", "9");
				}
			}
		}
		return html;
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
		
		final int npcId = npc.getNpcId();
		final int id = st.getState();
		final int grade = st.getInt("grade");
		switch (npcId)
		{
			case SORINT:
			{
				if (id == State.CREATED)
				{
					if (st.getPlayer().getLevel() < 40)
					{
						htmltext = "30232-01.htm";
						st.exitQuest(true);
					}
					else
					{
						htmltext = "30232-02.htm";
					}
				}
				else if (st.getQuestItemsCount(COIN_DIAGRAM) > 0)
				{
					if (st.getQuestItemsCount(KALDIS_COIN) > 0)
					{
						st.takeItems(KALDIS_COIN, -1);
						st.takeItems(COIN_DIAGRAM, -1);
						st.giveItems(MEMBERSHIP_3, 1);
						st.set("grade", "3");
						st.set("cond", "4");
						st.playSound("ItemSound.quest_fanfare_middle");
						htmltext = "30232-07.htm";
					}
					else
					{
						htmltext = "30232-06.htm";
					}
				}
				else if (grade == 3)
				{
					htmltext = "30232-12.htm";
				}
				else if (grade == 2)
				{
					htmltext = "30232-11.htm";
				}
				else if (grade == 1)
				{
					htmltext = "30232-10.htm";
				}
				break;
			}
			case BERNARD:
			{
				if ((st.getQuestItemsCount(COIN_DIAGRAM) > 0) && (grade == 0))
				{
					htmltext = "30702-01.htm";
				}
				else if (grade == 3)
				{
					htmltext = "30702-05.htm";
				}
				break;
			}
			default:
			{
				for (int[] e : EXCHANGE_LEVEL)
				{
					if ((npcId == e[0]) && (grade <= e[1]))
					{
						htmltext = npcId + "-01.htm";
					}
				}
				break;
			}
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
		
		final int cond = st.getInt("cond");
		final int grade = st.getInt("grade");
		final int chance = (npc.getLevel() + (grade * 3)) - 20;
		final int npcId = npc.getNpcId();
		if ((npcId == HARIT_LIZARDMAN_MATRIARCH) || (npcId == HARIT_LIZARDMAN_SHAMAN))
		{
			if ((cond == 2) && (Rnd.get(100) < (10.0 * npc.getTemplate().getBaseHpConsumeRate())))
			{
				st.giveItems(KALDIS_COIN, 1);
				st.set("cond", "3");
			}
			return null;
		}
		for (int[] e : DROPLIST)
		{
			if (e[0] == npcId)
			{
				if (Rnd.get(100) < chance)
				{
					st.giveItems(e[1], 1);
				}
				return null;
			}
		}
		for (int u : MONSTERS)
		{
			if (u == npcId)
			{
				if (Rnd.get(100) < (chance * npc.getTemplate().getBaseHpConsumeRate()))
				{
					st.giveItems(BASIC_COINS[Rnd.get(BASIC_COINS.length)], 1);
				}
				return null;
			}
		}
		return null;
	}
}
