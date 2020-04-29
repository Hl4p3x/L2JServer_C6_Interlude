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
package quests.Q335_TheSongOfTheHunter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

/**
 * Adapted from FirstTeam Interlude
 */
public class Q335_TheSongOfTheHunter extends Quest
{
	private static final int GREY = 30744;
	private static final int TOR = 30745;
	private static final int CYBELLIN = 30746;
	private static final int BREKA_ORC_WARRIOR = 20271;
	private static final int WINDSUS = 20553;
	private static final int TARLK_BUGBEAR_WARRIOR = 20571;
	private static final int BREKA_OVERLORD_HAKA = 27140;
	private static final int BREKA_OVERLORD_JAKA = 27141;
	private static final int BREKA_OVERLORD_MARKA = 27142;
	private static final int WINDSUS_ALEPH = 27143;
	private static final int TARLK_RAIDER_ATHU = 27144;
	private static final int TARLK_RAIDER_LANKA = 27145;
	private static final int TARLK_RAIDER_TRISKA = 27146;
	private static final int TARLK_RAIDER_MOTURA = 27147;
	private static final int TARLK_RAIDER_KALATH = 27148;
	private static final int CYBELLINS_DAGGER = 3471;
	private static final int CIRCLE_HUNTER_LICENSE1 = 3692;
	private static final int CIRCLE_HUNTER_LICENSE2 = 3693;
	private static final int LAUREL_LEAF_PIN = 3694;
	private static final int TEST_INSTRUCTIONS1 = 3695;
	private static final int TEST_INSTRUCTIONS2 = 3696;
	private static final int CYBELLINS_REQUEST = 3697;
	private static final int GUARDIAN_BASILISK_SCALE = 3709;
	private static final int KARUT_WEED = 3710;
	private static final int HAKAS_HEAD = 3711;
	private static final int JAKAS_HEAD = 3712;
	private static final int MARKAS_HEAD = 3713;
	private static final int WINDSUS_ALEPH_SKIN = 3714;
	private static final int INDIGO_RUNESTONE = 3715;
	private static final int SPORESEA_SEED = 3716;
	private static final int TIMAK_ORC_TOTEM = 3717;
	private static final int TRISALIM_SILK = 3718;
	private static final int AMBROSIUS_FRUIT = 3719;
	private static final int BALEFIRE_CRYSTAL = 3720;
	private static final int IMPERIAL_ARROWHEAD = 3721;
	private static final int ATHUS_HEAD = 3722;
	private static final int LANKAS_HEAD = 3723;
	private static final int TRISKAS_HEAD = 3724;
	private static final int MOTURAS_HEAD = 3725;
	private static final int KALATHS_HEAD = 3726;
	// @formatter:off
	private static final int[] Q_BLOOD_CRYSTAL =
	{
		3708, 3698, 3699, 3700, 3701, 3702, 3703, 3704, 3705, 3706, 3707
	};
	private static final int[] Q_BLOOD_CRYSTAL_LIZARDMEN =
	{
		20578, 20579, 20580, 20581, 20582, 20641, 20642, 20643, 20644, 20645
	};
	private static final int[][][] ITEMS_1ST_CIRCLE =
	{
		{{GUARDIAN_BASILISK_SCALE}, {40}, {20550, 75}},
		{{KARUT_WEED}, {20}, {20581, 50}},
		{{HAKAS_HEAD, JAKAS_HEAD, MARKAS_HEAD}, {3}},
		{{WINDSUS_ALEPH_SKIN}, {1}, {WINDSUS_ALEPH, 100}},
		{{INDIGO_RUNESTONE}, {20}, {20563, 50}, {20565, 50}},
		{{SPORESEA_SEED}, {30}, {20555, 70}}
	};
	private static final int[][][] ITEMS_2ND_CIRCLE =
	{
		{{TIMAK_ORC_TOTEM}, {20}, {20586, 50}},
		{{TRISALIM_SILK}, {20}, {20560, 50}, {20561, 50}},
		{{AMBROSIUS_FRUIT}, {30}, {20591, 75}, {20597, 75}},
		{{BALEFIRE_CRYSTAL}, {20}, {20675, 50}},
		{{IMPERIAL_ARROWHEAD}, {20}, {20660, 50}},
		{{ATHUS_HEAD, LANKAS_HEAD, TRISKAS_HEAD, MOTURAS_HEAD, KALATHS_HEAD}, {5}}
	};
	// @formatter:on
	private static final Request[] REQUESTS1 =
	{
		new Request(3727, 3769, 40, 2090, "C: 40 Totems of Kadesh").addDrop(20578, 80).addDrop(20579, 83),
		new Request(3728, 3770, 50, 6340, "C: 50 Jade Necklaces of Timak").addDrop(20586, 89).addDrop(20588, 100),
		new Request(3729, 3771, 50, 9480, "C: 50 Enchanted Golem Shards").addDrop(20565, 100),
		new Request(3730, 3772, 30, 9110, "C: 30 Pieces Monster Eye Meat").addDrop(20556, 50),
		new Request(3731, 3773, 40, 8690, "C: 40 Eggs of Dire Wyrm").addDrop(20557, 80),
		new Request(3732, 3774, 100, 9480, "C: 100 Claws of Guardian Basilisk").addDrop(20550, 150),
		new Request(3733, 3775, 50, 11280, "C: 50 Revenant Chains").addDrop(20552, 100),
		new Request(3734, 3776, 30, 9640, "C: 30 Windsus Tusks").addDrop(WINDSUS, 50),
		new Request(3735, 3777, 100, 9180, "C: 100 Skulls of Grandis").addDrop(20554, 200),
		new Request(3736, 3778, 50, 5160, "C: 50 Taik Obsidian Amulets").addDrop(20631, 100).addDrop(20632, 93),
		new Request(3737, 3779, 30, 3140, "C: 30 Heads of Karul Bugbear").addDrop(20600, 50),
		new Request(3738, 3780, 40, 3160, "C: 40 Ivory Charms of Tamlin").addDrop(20601, 62).addDrop(20602, 80),
		new Request(3739, 3781, 1, 6370, "B: Situation Preparation - Leto Chief").addSpawn(20582, 27157, 10).addDrop(27157, 100),
		new Request(3740, 3782, 50, 19080, "B: 50 Enchanted Gargoyle Horns").addDrop(20567, 50),
		new Request(3741, 3783, 50, 17730, "B: 50 Coiled Serpent Totems").addDrop(20269, 93).addDrop(BREKA_ORC_WARRIOR, 100),
		new Request(3742, 3784, 1, 5790, "B: Situation Preparation - Sorcerer Catch of Leto").addSpawn(20581, 27156, 10).addDrop(27156, 100),
		new Request(3743, 3785, 1, 8560, "B: Situation Preparation - Timak Raider Kaikee").addSpawn(20586, 27158, 10).addDrop(27158, 100),
		new Request(3744, 3786, 30, 8320, "B: 30 Kronbe Venom Sacs").addDrop(20603, 50),
		new Request(3745, 3787, 30, 30310, "A: 30 Eva's Charm").addDrop(20562, 50),
		new Request(3746, 3788, 1, 27540, "A: Titan's Tablet").addSpawn(20554, 27160, 10).addDrop(27160, 100),
		new Request(3747, 3789, 1, 20560, "A: Book of Shunaiman").addSpawn(20600, 27164, 10).addDrop(27164, 100)
	};
	private static final Request[] REQUESTS2 =
	{
		new Request(3748, 3790, 40, 6850, "C: 40 Rotting Tree Spores").addDrop(20558, 67),
		new Request(3749, 3791, 40, 7250, "C: 40 Trisalim Venom Sacs").addDrop(20560, 66).addDrop(20561, 75),
		new Request(3750, 3792, 50, 7160, "C: 50 Totems of Taik Orc").addDrop(20633, 53).addDrop(20634, 99),
		new Request(3751, 3793, 40, 6580, "C: 40 Harit Barbed Necklaces").addDrop(20641, 88).addDrop(20642, 88).addDrop(20643, 91),
		new Request(3752, 3794, 20, 10100, "C: 20 Coins of Ancient Empire").addDrop(20661, 50).addSpawn(20661, 27149, 5).addDrop(20662, 52).addSpawn(20662, 27149, 5).addDrop(27149, 300),
		new Request(3753, 3795, 30, 13000, "C: 30 Skins of Farkran").addDrop(20667, 90),
		new Request(3754, 3796, 40, 7660, "C: 40 Tempest Shards").addDrop(20589, 49).addSpawn(20589, 27149, 5).addDrop(27149, 500),
		new Request(3755, 3797, 40, 7660, "C: 40 Tsunami Shards").addDrop(20590, 51).addSpawn(20590, 27149, 5).addDrop(27149, 500),
		new Request(3756, 3798, 40, 11260, "C: 40 Manes of Pan Ruem").addDrop(20592, 80).addDrop(20598, 100),
		new Request(3757, 3799, 40, 7660, "C: 40 Hamadryad Shard").addDrop(20594, 64).addSpawn(20594, 27149, 5).addDrop(27149, 500),
		new Request(3758, 3800, 30, 8810, "C: 30 Manes of Vanor Silenos").addDrop(20682, 70).addDrop(20683, 85).addDrop(20684, 90),
		new Request(3759, 3801, 30, 7350, "C: 30 Totems of Tarlk Bugbears").addDrop(TARLK_BUGBEAR_WARRIOR, 63),
		new Request(3760, 3802, 1, 8760, "B: Situation Preparation - Overlord Okun of Timak").addSpawn(20588, 27159, 10).addDrop(27159, 100),
		new Request(3761, 3803, 1, 9380, "B: Situation Preparation - Overlord Kakran of Taik").addSpawn(20634, 27161, 10).addDrop(27161, 100),
		new Request(3762, 3804, 40, 17820, "B: 40 Narcissus Soulstones").addDrop(20639, 86).addSpawn(20639, 27149, 5).addDrop(27149, 500),
		new Request(3763, 3805, 20, 17540, "B: 20 Eyes of Deprived").addDrop(20664, 77),
		new Request(3764, 3806, 20, 14160, "B: 20 Unicorn Horns").addDrop(20593, 68).addDrop(20599, 86),
		new Request(3765, 3807, 1, 15960, "B: Golden Mane of Silenos").addSpawn(20686, 27163, 10).addDrop(27163, 100),
		new Request(3766, 3808, 20, 39100, "A: 20 Skulls of Executed Person").addDrop(20659, 73),
		new Request(3767, 3809, 1, 39550, "A: Bust of Travis").addSpawn(20662, 27162, 10).addDrop(27162, 100),
		new Request(3768, 3810, 10, 41200, "A: 10 Swords of Cadmus").addDrop(20676, 64)
	};
	
	public Q335_TheSongOfTheHunter()
	{
		super(335, "Song of the Hunter");
		
		addStartNpc(GREY);
		addTalkId(GREY, CYBELLIN, TOR);
		addKillId(BREKA_OVERLORD_HAKA);
		addKillId(BREKA_OVERLORD_JAKA);
		addKillId(BREKA_OVERLORD_MARKA);
		addKillId(TARLK_RAIDER_ATHU);
		addKillId(TARLK_RAIDER_LANKA);
		addKillId(TARLK_RAIDER_TRISKA);
		addKillId(TARLK_RAIDER_MOTURA);
		addKillId(TARLK_RAIDER_KALATH);
		addKillId(Q_BLOOD_CRYSTAL_LIZARDMEN);
		final List<Integer> questItems = new ArrayList<>();
		for (int[][] ItemsCond : ITEMS_1ST_CIRCLE)
		{
			for (int i : ItemsCond[0])
			{
				questItems.add(i);
			}
			for (int i = 2; i < ItemsCond.length; ++i)
			{
				addKillId(ItemsCond[i][0]);
			}
		}
		for (int[][] ItemsCond : ITEMS_2ND_CIRCLE)
		{
			for (int i : ItemsCond[0])
			{
				questItems.add(i);
			}
			for (int i = 2; i < ItemsCond.length; ++i)
			{
				addKillId(ItemsCond[i][0]);
			}
		}
		for (Request r : REQUESTS1)
		{
			questItems.add(r.request_id);
			questItems.add(r.request_item);
			for (int id : r.droplist.keySet())
			{
				addKillId(id);
			}
			for (int id : r.spawnlist.keySet())
			{
				addKillId(id);
			}
		}
		for (Request r : REQUESTS2)
		{
			questItems.add(r.request_id);
			questItems.add(r.request_item);
			for (int id : r.droplist.keySet())
			{
				addKillId(id);
			}
			for (int id : r.spawnlist.keySet())
			{
				addKillId(id);
			}
		}
		questItems.add(CIRCLE_HUNTER_LICENSE1);
		questItems.add(CIRCLE_HUNTER_LICENSE2);
		questItems.add(LAUREL_LEAF_PIN);
		questItems.add(TEST_INSTRUCTIONS1);
		questItems.add(TEST_INSTRUCTIONS2);
		questItems.add(CYBELLINS_REQUEST);
		questItems.add(CYBELLINS_DAGGER);
		for (int i : Q_BLOOD_CRYSTAL)
		{
			questItems.add(i);
		}
		
		registerQuestItems(questItems.stream().mapToInt(i -> i).toArray());
	}
	
	private static int CalcItemsConds(QuestState st, int[][][] itemConds)
	{
		int result = 0;
		for (int[][] itemCond : itemConds)
		{
			int count = 0;
			for (int i : itemCond[0])
			{
				count += st.getQuestItemsCount(i);
			}
			if (count >= itemCond[1][0])
			{
				++result;
			}
		}
		return result;
	}
	
	private void DelItemsConds(QuestState st, int[][][] itemConds)
	{
		for (int[][] itemCond : itemConds)
		{
			for (int i : itemCond[0])
			{
				st.takeItems(i, -1);
			}
		}
	}
	
	private static int Get_Blood_Crystal_Level(QuestState st)
	{
		for (int i = Q_BLOOD_CRYSTAL.length - 1; i >= 0; --i)
		{
			if (st.getQuestItemsCount(Q_BLOOD_CRYSTAL[i]) > 0)
			{
				return i;
			}
		}
		return -1;
	}
	
	private static boolean Blood_Crystal2Adena(QuestState st, int bloodCrystalLevel)
	{
		if (bloodCrystalLevel < 2)
		{
			return false;
		}
		for (int i : Q_BLOOD_CRYSTAL)
		{
			st.takeItems(i, -1);
		}
		st.giveItems(57, (3400 * (int) Math.pow(2.0, bloodCrystalLevel - 2)));
		return true;
	}
	
	private void GenList(QuestState st)
	{
		// final int grade_c = 12;
		// final int grade_b = 6;
		// final int grade_a = 3;
		if ((st.getString("list") == null) || st.getString("list").isEmpty())
		{
			final long Laurel_Leaf_Pin_count = st.getQuestItemsCount(LAUREL_LEAF_PIN);
			final int[] list = new int[5];
			if (Laurel_Leaf_Pin_count < 4L)
			{
				if ((Laurel_Leaf_Pin_count == 0) || (Rnd.get(100) < 80))
				{
					for (int i = 0; i < 5; ++i)
					{
						list[i] = Rnd.get(12);
					}
				}
				else
				{
					list[0] = 12 + Rnd.get(6);
					list[1] = Rnd.get(12);
					list[2] = Rnd.get(6);
					list[3] = 6 + Rnd.get(6);
					list[4] = Rnd.get(12);
				}
			}
			else if (Rnd.get(100) < 20)
			{
				list[0] = 12 + Rnd.get(6);
				list[1] = Rnd.get(100) < 5 ? (18 + Rnd.get(3)) : Rnd.get(12);
				list[2] = Rnd.get(6);
				list[3] = 6 + Rnd.get(6);
				list[4] = Rnd.get(12);
			}
			else
			{
				list[0] = Rnd.get(12);
				list[1] = Rnd.get(100) < 5 ? (18 + Rnd.get(3)) : Rnd.get(12);
				list[2] = Rnd.get(6);
				list[3] = 6 + Rnd.get(6);
				list[4] = Rnd.get(12);
			}
			boolean sortFlag;
			do
			{
				sortFlag = false;
				for (int j = 1; j < list.length; ++j)
				{
					if (list[j] < list[j - 1])
					{
						final int tmp = list[j];
						list[j] = list[j - 1];
						list[j - 1] = tmp;
						sortFlag = true;
					}
				}
			}
			while (sortFlag);
			int packedlist = 0;
			try
			{
				packedlist = packInt(list, 5);
			}
			catch (Exception e)
			{
				// Ignore.
			}
			st.set("list", String.valueOf(packedlist));
		}
	}
	
	private static String FormatList(QuestState st, Request[] requests)
	{
		String result = "<html><head><body>Guild Member Tor:<br>%reply%<br>%reply%<br>%reply%<br>%reply%<br>%reply%<br></body></html>";
		final int[] listpacked = unpackInt(st.getInt("list"), 5);
		for (int i = 0; i <= 5; ++i)
		{
			final String s = "<a action=\"bypass -h Quest Q335_TheSongOfTheHunter 30745-request-" + requests[listpacked[i]].request_id + "\">" + requests[listpacked[i]].text + "</a>";
			result = result.replaceFirst("%reply%", s);
		}
		return result;
	}
	
	private static Request GetCurrentRequest(QuestState st, Request[] requests)
	{
		for (Request r : requests)
		{
			if (st.getQuestItemsCount(r.request_id) > 0)
			{
				return r;
			}
		}
		return null;
	}
	
	private static boolean isValidRequest(int id)
	{
		for (Request r : REQUESTS1)
		{
			if (r.request_id == id)
			{
				return true;
			}
		}
		for (Request r : REQUESTS2)
		{
			if (r.request_id == id)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return event;
		}
		
		final int state = st.getState();
		if ("30744_03.htm".equalsIgnoreCase(event) && (state == 1))
		{
			if (st.getQuestItemsCount(TEST_INSTRUCTIONS1) == 0)
			{
				st.giveItems(TEST_INSTRUCTIONS1, 1);
			}
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if ("30744_09.htm".equalsIgnoreCase(event) && (state == 2))
		{
			if (GetCurrentRequest(st, REQUESTS1) != null)
			{
				return "30744_09a.htm";
			}
			if (st.getQuestItemsCount(TEST_INSTRUCTIONS2) == 0)
			{
				st.playSound("ItemSound.quest_middle");
				st.giveItems(TEST_INSTRUCTIONS2, 1);
			}
		}
		else if ("30744_16.htm".equalsIgnoreCase(event) && (state == 2))
		{
			if (st.getQuestItemsCount(LAUREL_LEAF_PIN) >= 20)
			{
				st.giveItems(57, 20000);
				event = "30744_17.htm";
			}
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if ("30746_03.htm".equalsIgnoreCase(event) && (state == 2))
		{
			if ((st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE1) == 0) && (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE2) == 0))
			{
				return null;
			}
			if (st.getQuestItemsCount(CYBELLINS_DAGGER) == 0)
			{
				st.giveItems(CYBELLINS_DAGGER, 1);
			}
			if (st.getQuestItemsCount(CYBELLINS_REQUEST) == 0)
			{
				st.giveItems(CYBELLINS_REQUEST, 1);
			}
			for (int i : Q_BLOOD_CRYSTAL)
			{
				st.takeItems(i, -1);
			}
			st.playSound("ItemSound.quest_middle");
			st.giveItems(Q_BLOOD_CRYSTAL[1], 1);
		}
		else if ("30746_06.htm".equalsIgnoreCase(event) && (state == 2))
		{
			if (!Blood_Crystal2Adena(st, Get_Blood_Crystal_Level(st)))
			{
				return null;
			}
		}
		else if ("30746_10.htm".equalsIgnoreCase(event) && (state == 2))
		{
			st.takeItems(CYBELLINS_DAGGER, -1);
			st.takeItems(CYBELLINS_REQUEST, -1);
			for (int i : Q_BLOOD_CRYSTAL)
			{
				st.takeItems(i, -1);
			}
		}
		else if ("30745_02.htm".equalsIgnoreCase(event) && (state == 2))
		{
			if (st.getQuestItemsCount(TEST_INSTRUCTIONS2) > 0)
			{
				return "30745_03.htm";
			}
		}
		else if ("30745_05b.htm".equalsIgnoreCase(event) && (state == 2))
		{
			if (st.getQuestItemsCount(LAUREL_LEAF_PIN) > 0)
			{
				st.takeItems(LAUREL_LEAF_PIN, 1);
			}
			for (Request r : REQUESTS1)
			{
				st.takeItems(r.request_id, -1);
				st.takeItems(r.request_item, -1);
			}
			for (Request r : REQUESTS2)
			{
				st.takeItems(r.request_id, -1);
				st.takeItems(r.request_item, -1);
			}
		}
		else
		{
			if ("30745-list1".equalsIgnoreCase(event) && (state == 2))
			{
				GenList(st);
				return FormatList(st, REQUESTS1);
			}
			if ("30745-list2".equalsIgnoreCase(event) && (state == 2))
			{
				GenList(st);
				return FormatList(st, REQUESTS2);
			}
			if (event.startsWith("30745-request-") && (state == 2))
			{
				event = event.replaceFirst("30745-request-", "");
				int requestId;
				try
				{
					requestId = Integer.parseInt(event);
				}
				catch (Exception e)
				{
					return null;
				}
				if (!isValidRequest(requestId))
				{
					return null;
				}
				st.giveItems(requestId, 1);
				return "30745-" + requestId + ".htm";
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		final String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		final int _state = st.getState();
		final int npcId = npc.getNpcId();
		if (_state == 1)
		{
			if (npcId != GREY)
			{
				return getNoQuestMsg();
			}
			if (st.getPlayer().getLevel() < 35)
			{
				st.exitQuest(true);
				return "30744_01.htm";
			}
			st.set("cond", "0");
			st.unset("list");
			return "30744_02.htm";
		}
		if (_state != 2)
		{
			return getNoQuestMsg();
		}
		if (npcId == GREY)
		{
			if (st.getQuestItemsCount(TEST_INSTRUCTIONS1) > 0)
			{
				if (CalcItemsConds(st, ITEMS_1ST_CIRCLE) < 3)
				{
					return "30744_05.htm";
				}
				DelItemsConds(st, ITEMS_1ST_CIRCLE);
				st.takeItems(TEST_INSTRUCTIONS1, -1);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(CIRCLE_HUNTER_LICENSE1, 1);
				st.set("cond", "2");
				return "30744_06.htm";
			}
			if (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE1) > 0)
			{
				if (st.getPlayer().getLevel() < 45)
				{
					return "30744_07.htm";
				}
				if (st.getQuestItemsCount(TEST_INSTRUCTIONS2) == 0)
				{
					return "30744_08.htm";
				}
			}
			if (st.getQuestItemsCount(TEST_INSTRUCTIONS2) > 0)
			{
				if (CalcItemsConds(st, ITEMS_2ND_CIRCLE) < 3)
				{
					return "30744_11.htm";
				}
				DelItemsConds(st, ITEMS_2ND_CIRCLE);
				st.takeItems(TEST_INSTRUCTIONS2, -1);
				st.takeItems(CIRCLE_HUNTER_LICENSE1, -1);
				st.playSound("ItemSound.quest_middle");
				st.giveItems(CIRCLE_HUNTER_LICENSE2, 1);
				st.set("cond", "3");
				return "30744_12.htm";
			}
			else if (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE2) > 0)
			{
				return "30744_14.htm";
			}
		}
		if (npcId == CYBELLIN)
		{
			if ((st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE1) == 0) && (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE2) == 0))
			{
				return "30746_01.htm";
			}
			if (st.getQuestItemsCount(CYBELLINS_REQUEST) == 0)
			{
				return "30746_02.htm";
			}
			final int Blood_Crystal_Level = Get_Blood_Crystal_Level(st);
			if (Blood_Crystal_Level == -1)
			{
				return "30746_08.htm";
			}
			if (Blood_Crystal_Level == 0)
			{
				return "30746_09.htm";
			}
			if (Blood_Crystal_Level == 1)
			{
				return "30746_04.htm";
			}
			if ((Blood_Crystal_Level > 1) && (Blood_Crystal_Level < 10))
			{
				return "30746_05.htm";
			}
			if ((Blood_Crystal_Level == 10) && Blood_Crystal2Adena(st, Blood_Crystal_Level))
			{
				return "30746_05a.htm";
			}
		}
		if (npcId == TOR)
		{
			if ((st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE1) == 0) && (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE2) == 0))
			{
				return "30745_01a.htm";
			}
			if (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE1) > 0)
			{
				final Request request = GetCurrentRequest(st, REQUESTS1);
				if (request != null)
				{
					return request.Complete(st) ? "30745_06a.htm" : "30745_05.htm";
				}
				if (st.getPlayer().getLevel() < 45)
				{
					return "30745_01b.htm";
				}
				return (st.getQuestItemsCount(TEST_INSTRUCTIONS2) > 0) ? "30745_03.htm" : "30745_03a.htm";
			}
			else if (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE2) > 0)
			{
				final Request request = GetCurrentRequest(st, REQUESTS2);
				if (request == null)
				{
					return "30745_03b.htm";
				}
				return request.Complete(st) ? "30745_06b.htm" : "30745_05.htm";
			}
		}
		return getNoQuestMsg();
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, State.STARTED);
		if (st == null)
		{
			return null;
		}
		
		if (st.getState() != 2)
		{
			return null;
		}
		final int npcId = npc.getNpcId();
		int[][][] itemsCircle = null;
		if (st.getQuestItemsCount(TEST_INSTRUCTIONS1) > 0)
		{
			itemsCircle = ITEMS_1ST_CIRCLE;
		}
		else if (st.getQuestItemsCount(TEST_INSTRUCTIONS2) > 0)
		{
			itemsCircle = ITEMS_2ND_CIRCLE;
		}
		if (itemsCircle != null)
		{
			for (int[][] itemCond : itemsCircle)
			{
				for (int i = 2; i < itemCond.length; ++i)
				{
					if ((npcId == itemCond[i][0]) && (Rnd.get(100) < itemCond[i][1]) && (st.getQuestItemsCount(itemCond[0][0]) < itemCond[1][0]))
					{
						st.giveItems(itemCond[0][0], 1);
					}
				}
			}
			if (st.getQuestItemsCount(TEST_INSTRUCTIONS1) > 0)
			{
				final long hakasHeadCount = st.getQuestItemsCount(HAKAS_HEAD);
				final long jakasHeadCount = st.getQuestItemsCount(JAKAS_HEAD);
				final long markasHeadCount = st.getQuestItemsCount(MARKAS_HEAD);
				if (npcId == BREKA_ORC_WARRIOR)
				{
					if ((hakasHeadCount == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(BREKA_OVERLORD_HAKA, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
					else if ((jakasHeadCount == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(BREKA_OVERLORD_JAKA, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
					else if ((markasHeadCount == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(BREKA_OVERLORD_MARKA, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
				}
				else if (npcId == BREKA_OVERLORD_HAKA)
				{
					if (hakasHeadCount == 0)
					{
						st.giveItems(HAKAS_HEAD, 1);
					}
				}
				else if (npcId == BREKA_OVERLORD_JAKA)
				{
					if (jakasHeadCount == 0)
					{
						st.giveItems(JAKAS_HEAD, 1);
					}
				}
				else if (npcId == BREKA_OVERLORD_MARKA)
				{
					if (markasHeadCount == 0)
					{
						st.giveItems(MARKAS_HEAD, 1);
					}
				}
				else if ((npcId == WINDSUS) && (st.getQuestItemsCount(WINDSUS_ALEPH_SKIN) == 0) && (Rnd.get(100) < 10))
				{
					st.addSpawn(WINDSUS_ALEPH, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
				}
			}
			else if (st.getQuestItemsCount(TEST_INSTRUCTIONS2) > 0)
			{
				final long Athus_Head_count = st.getQuestItemsCount(ATHUS_HEAD);
				final long Lankas_Head_count = st.getQuestItemsCount(LANKAS_HEAD);
				final long Triskas_Head_count = st.getQuestItemsCount(TRISKAS_HEAD);
				final long Moturas_Head_count = st.getQuestItemsCount(MOTURAS_HEAD);
				final long Kalaths_Head_count = st.getQuestItemsCount(KALATHS_HEAD);
				if (npcId == TARLK_BUGBEAR_WARRIOR)
				{
					if ((Athus_Head_count == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(TARLK_RAIDER_ATHU, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
					else if ((Lankas_Head_count == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(TARLK_RAIDER_LANKA, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
					else if ((Triskas_Head_count == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(TARLK_RAIDER_TRISKA, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
					else if ((Moturas_Head_count == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(TARLK_RAIDER_MOTURA, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
					else if ((Kalaths_Head_count == 0) && (Rnd.get(100) < 10))
					{
						st.addSpawn(TARLK_RAIDER_KALATH, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					}
				}
				else if (npcId == TARLK_RAIDER_ATHU)
				{
					if (Athus_Head_count == 0)
					{
						st.giveItems(ATHUS_HEAD, 1);
					}
				}
				else if (npcId == TARLK_RAIDER_LANKA)
				{
					if (Lankas_Head_count == 0)
					{
						st.giveItems(LANKAS_HEAD, 1);
					}
				}
				else if (npcId == TARLK_RAIDER_TRISKA)
				{
					if (Triskas_Head_count == 0)
					{
						st.giveItems(TRISKAS_HEAD, 1);
					}
				}
				else if (npcId == TARLK_RAIDER_MOTURA)
				{
					if (Moturas_Head_count == 0)
					{
						st.giveItems(MOTURAS_HEAD, 1);
					}
				}
				else if ((npcId == TARLK_RAIDER_KALATH) && (Kalaths_Head_count == 0))
				{
					st.giveItems(KALATHS_HEAD, 1);
				}
			}
		}
		if ((st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE1) > 0) || (st.getQuestItemsCount(CIRCLE_HUNTER_LICENSE2) > 0))
		{
			if ((st.getQuestItemsCount(CYBELLINS_REQUEST) > 0) && (st.getPlayer().getActiveWeaponItem() != null) && (st.getPlayer().getActiveWeaponItem().getItemId() == 3471))
			{
				final int Blood_Crystal_Level = Get_Blood_Crystal_Level(st);
				if ((Blood_Crystal_Level > 0) && (Blood_Crystal_Level < 10))
				{
					for (int lizardmen_id : Q_BLOOD_CRYSTAL_LIZARDMEN)
					{
						if (npcId == lizardmen_id)
						{
							if (Rnd.get(100) < 50)
							{
								st.takeItems(Q_BLOOD_CRYSTAL[Blood_Crystal_Level], -1);
								st.playSound((Blood_Crystal_Level < 6) ? "ItemSound.quest_middle" : "ItemSound.quest_jackpot");
								st.giveItems(Q_BLOOD_CRYSTAL[Blood_Crystal_Level + 1], 1);
							}
							else
							{
								for (int i : Q_BLOOD_CRYSTAL)
								{
									st.takeItems(i, -1);
								}
								st.giveItems(Q_BLOOD_CRYSTAL[0], 1);
							}
						}
					}
				}
			}
			Request request = GetCurrentRequest(st, REQUESTS1);
			if (request == null)
			{
				request = GetCurrentRequest(st, REQUESTS2);
			}
			if (request != null)
			{
				if (request.droplist.containsKey(npcId) && (Rnd.get(100) < request.droplist.get(npcId)) && (st.getQuestItemsCount(request.request_item) < request.request_count))
				{
					st.giveItems(request.request_item, 1);
				}
				if (request.spawnlist.containsKey(npcId) && (st.getQuestItemsCount(request.request_item) < request.request_count))
				{
					final int[] spawnChance = request.spawnlist.get(npcId);
					if (Rnd.get(100) < spawnChance[1])
					{
						st.addSpawn(spawnChance[0], npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
						if (spawnChance[0] == 27149)
						{
							npc.broadcastNpcSay("Show me the pretty sparkling things! They're all mine!");
						}
					}
				}
			}
		}
		if (((npcId == 27160) || (npcId == 27162) || (npcId == 27164)) && (Rnd.get(100) < 50))
		{
			npc.broadcastNpcSay("We'll take the property of the ancient empire!");
			st.addSpawn(27150, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
			st.addSpawn(27150, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
		}
		return null;
	}
	
	public static class Request
	{
		public int request_id;
		public int request_item;
		public int request_count;
		public int reward_adena;
		public String text;
		public Map<Integer, Integer> droplist;
		public Map<Integer, int[]> spawnlist;
		
		public Request(int requestid, int requestitem, int requestcount, int rewardadena, String txt)
		{
			droplist = new HashMap<>();
			spawnlist = new HashMap<>();
			request_id = requestid;
			request_item = requestitem;
			request_count = requestcount;
			reward_adena = rewardadena;
			text = txt;
		}
		
		public Request addDrop(int killMobId, int chance)
		{
			droplist.put(killMobId, chance);
			return this;
		}
		
		public Request addSpawn(int killMobId, int spawnMobId, int chance)
		{
			try
			{
				spawnlist.put(killMobId, new int[]
				{
					spawnMobId,
					chance
				});
			}
			catch (Exception e)
			{
				// Ignore.
			}
			return this;
		}
		
		public boolean Complete(QuestState st)
		{
			if (st.getQuestItemsCount(request_item) < request_count)
			{
				return false;
			}
			st.takeItems(request_id, -1);
			st.takeItems(request_item, -1);
			st.playSound("ItemSound.quest_middle");
			st.giveItems(LAUREL_LEAF_PIN, 1);
			st.giveItems(57, reward_adena);
			st.unset("list");
			return true;
		}
	}
	
	private static int packInt(int[] a, int bits) throws Exception
	{
		final int m = 32 / bits;
		if (a.length > m)
		{
			throw new Exception("Overflow");
		}
		int result = 0;
		final int mval = (int) Math.pow(2.0, bits);
		for (int i = 0; i < m; ++i)
		{
			result <<= bits;
			int next;
			if (a.length > i)
			{
				next = a[i];
				if ((next >= mval) || (next < 0))
				{
					throw new Exception("Overload, value is out of range");
				}
			}
			else
			{
				next = 0;
			}
			result += next;
		}
		return result;
	}
	
	private static int[] unpackInt(int a, int bits)
	{
		final int m = 32 / bits;
		final int mval = (int) Math.pow(2.0, bits);
		final int[] result = new int[m];
		for (int i = m; i > 0; --i)
		{
			final int next = a;
			a >>= bits;
			result[i - 1] = next - (a * mval);
		}
		return result;
	}
}
