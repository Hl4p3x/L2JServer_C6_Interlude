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
package quests.Q503_PursuitOfClanAmbition;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

/**
 * @author Jackass
 */
public class Q503_PursuitOfClanAmbition extends Quest
{
	// Items
	// First
	private static final int G_LET_MARTIEN = 3866;
	private static final int TH_WYRM_EGGS = 3842;
	private static final int DRAKE_EGGS = 3841;
	private static final int BL_WYRM_EGGS = 3840;
	private static final int MI_DRAKE_EGGS = 3839;
	private static final int BROOCH = 3843;
	private static final int BL_ANVIL_COIN = 3871;
	
	// Second part
	private static final int G_LET_BALTHAZAR = 3867;
	private static final int RECIPE_POWER_STONE = 3838;
	private static final int POWER_STONE = 3846;
	private static final int NEBULITE_CRYSTALS = 3844;
	private static final int BROKE_POWER_STONE = 3845;
	
	// Third part
	private static final int G_LET_RODEMAI = 3868;
	private static final int IMP_KEYS = 3847;
	private static final int SCEPTER_JUDGEMENT = 3869;
	
	// Final item
	private static final int PROOF_ASPIRATION = 3870;
	
	// NPCS
	private static final int MARTIEN = 30645;
	private static final int ATHREA = 30758;
	private static final int KALIS = 30759;
	private static final int GUSTAF = 30760;
	private static final int FRITZ = 30761;
	private static final int LUTZ = 30762;
	private static final int KURTZ = 30763;
	private static final int KUSTO = 30512;
	private static final int BALTHAZAR = 30764;
	private static final int RODEMAI = 30868;
	private static final int COFFER = 30765;
	private static final int CLEO = 30766;
	
	// Mobs
	private static final int THUNDER_WYRM = 20282;
	private static final int THUNDER_WYRM_TWO = 20243;
	private static final int DRAKE = 20137;
	private static final int DRAKE_TWO = 20285;
	private static final int BLITZ_WYRM = 27178;
	private static final int GIANT_SOLDIER = 20654;
	private static final int GIANT_SCOUT = 20656;
	private static final int GRAVE_GUARD = 20668;
	private static final int GRAVE_KEYMASTER = 27179;
	private static final int IMPERIAL_SLAVE = 27180;
	
	// Attack mob
	private static final int IMPERIAL_GRAVEKEEPER = 27181;
	
	// DROPLIST
	private static final int[][] DROPLIST =
	{
		// npcId, cond, MaxCount, chance, item1, item2 (giants), item3 (giants)
		// @formatter:off
		{THUNDER_WYRM, 2, 10, 200000, TH_WYRM_EGGS, 0, 0},
		{THUNDER_WYRM_TWO, 2, 10, 150000, TH_WYRM_EGGS, 0, 0},
		{DRAKE, 2, 10, 200000, DRAKE_EGGS, 0, 0},
		{DRAKE_TWO, 2, 10, 250000, DRAKE_EGGS, 0, 0},
		{BLITZ_WYRM, 2, 10, 1000000, BL_WYRM_EGGS, 0, 0},
		{GIANT_SOLDIER, 5, 10, 250000, NEBULITE_CRYSTALS, BROKE_POWER_STONE, POWER_STONE},
		{GIANT_SCOUT, 5, 10, 350000, NEBULITE_CRYSTALS, BROKE_POWER_STONE, POWER_STONE},
		{GRAVE_GUARD, 10, 0, 150000, 0, 0, 0},
		{GRAVE_KEYMASTER, 10, 6, 800000, IMP_KEYS, 0, 0},
		{IMPERIAL_GRAVEKEEPER, 10, 0, 0, 0, 0, 0}
		// @formatter:on
	};
	
	public Q503_PursuitOfClanAmbition()
	{
		super(503, "Pursuit of Clan Ambition!");
		
		registerQuestItems(MI_DRAKE_EGGS, BL_WYRM_EGGS, DRAKE_EGGS, TH_WYRM_EGGS, BROOCH, NEBULITE_CRYSTALS, BROKE_POWER_STONE, POWER_STONE, IMP_KEYS, G_LET_MARTIEN, G_LET_BALTHAZAR, G_LET_RODEMAI, SCEPTER_JUDGEMENT);
		
		addStartNpc(GUSTAF);
		addTalkId(MARTIEN, ATHREA, KALIS, GUSTAF, FRITZ, LUTZ, KURTZ, KUSTO, BALTHAZAR, RODEMAI, COFFER, CLEO);
		
		addKillId(THUNDER_WYRM_TWO, THUNDER_WYRM, DRAKE, DRAKE_TWO, BLITZ_WYRM, GIANT_SOLDIER, GIANT_SCOUT, GRAVE_GUARD, GRAVE_KEYMASTER, IMPERIAL_GRAVEKEEPER);
		addAttackId(IMPERIAL_GRAVEKEEPER);
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
		
		// Gustaf
		if (event.equalsIgnoreCase("30760-08.htm"))
		{
			st.setState(State.STARTED);
			st.giveItems(G_LET_MARTIEN, 1);
			st.set("cond", "1");
		}
		else if (event.equalsIgnoreCase("30760-12.htm"))
		{
			st.giveItems(G_LET_BALTHAZAR, 1);
			st.set("cond", "4");
		}
		else if (event.equalsIgnoreCase("30760-16.htm"))
		{
			st.giveItems(G_LET_RODEMAI, 1);
			st.set("cond", "7");
		}
		else if (event.equalsIgnoreCase("30760-20.htm"))
		{
			st.exitQuest(false);
			finishQuestToClan(player);
			st.giveItems(PROOF_ASPIRATION, 1);
			st.takeItems(SCEPTER_JUDGEMENT, -1);
			st.rewardExpAndSp(0, 250000);
		}
		else if (event.equalsIgnoreCase("30760-22.htm"))
		{
			st.set("cond", "1");
		}
		else if (event.equalsIgnoreCase("30760-23.htm"))
		{
			finishQuestToClan(player);
			st.exitQuest(false);
			st.takeItems(SCEPTER_JUDGEMENT, -1);
			st.giveItems(PROOF_ASPIRATION, 1);
			st.rewardExpAndSp(0, 250000);
		}
		// Martien
		else if (event.equalsIgnoreCase("30645-03.htm"))
		{
			setQuestToClanMembers(player);
			st.takeItems(G_LET_MARTIEN, -1);
			st.set("cond", "2");
			st.set("kurt", "0");
		}
		// Kurtz
		else if (event.equalsIgnoreCase("30763-02.htm"))
		{
			st.giveItems(MI_DRAKE_EGGS, 6);
			st.giveItems(BROOCH, 1);
			st.set("kurt", "1");
		}
		// Lutz
		else if (event.equalsIgnoreCase("30762-02.htm"))
		{
			st.giveItems(MI_DRAKE_EGGS, 4);
			st.giveItems(BL_WYRM_EGGS, 3);
			st.addSpawn(BLITZ_WYRM, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			st.addSpawn(BLITZ_WYRM, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			st.set("lutz", "1");
		}
		// Fritz
		else if (event.equalsIgnoreCase("30761-02.htm"))
		{
			st.giveItems(BL_WYRM_EGGS, 3);
			st.addSpawn(BLITZ_WYRM, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			st.addSpawn(BLITZ_WYRM, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			st.set("fritz", "1");
		}
		// Kusto
		else if (event.equalsIgnoreCase("30512-03.htm"))
		{
			st.takeItems(BROOCH, 1);
			st.giveItems(BL_ANVIL_COIN, 1);
			st.set("kurt", "2");
		}
		// Balthazar
		else if (event.equalsIgnoreCase("30764-03.htm"))
		{
			st.takeItems(G_LET_BALTHAZAR, -1);
			st.set("cond", "5");
		}
		else if (event.equalsIgnoreCase("30764-05.htm"))
		{
			st.takeItems(G_LET_BALTHAZAR, -1);
			st.set("cond", "5");
		}
		else if (event.equalsIgnoreCase("30764-06.htm"))
		{
			st.takeItems(BL_ANVIL_COIN, -1);
			st.giveItems(RECIPE_POWER_STONE, 1);
		}
		// Rodemai
		else if (event.equalsIgnoreCase("30868-04.htm"))
		{
			st.takeItems(G_LET_RODEMAI, -1);
			st.set("cond", "8");
		}
		else if (event.equalsIgnoreCase("30868-06a.htm"))
		{
			st.set("cond", "10");
		}
		else if (event.equalsIgnoreCase("30868-10.htm"))
		{
			st.set("cond", "12");
		}
		// Cleo
		else if (event.equalsIgnoreCase("30766-04.htm"))
		{
			st.set("cond", "9");
			npc.broadcastNpcSay("Blood and Honor");
			final NpcInstance sister1 = addSpawn(KALIS, 160665, 21209, -3710, npc.getHeading(), false, 180000);
			sister1.broadcastNpcSay("Ambition and Power");
			final NpcInstance sister2 = addSpawn(ATHREA, 160665, 21291, -3710, npc.getHeading(), false, 180000);
			sister2.broadcastNpcSay("War and Death");
		}
		// Coffer
		else if (event.equalsIgnoreCase("Open"))
		{
			if (st.getQuestItemsCount(IMP_KEYS) < 6)
			{
				htmltext = "30765-03a.htm";
			}
			else
			{
				htmltext = "30765-03.htm";
				st.set("cond", "11");
				st.takeItems(IMP_KEYS, 6);
				st.giveItems(SCEPTER_JUDGEMENT, 1);
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
				if (player.getClan() == null)
				{
					htmltext = "30760-01.htm";
					st.exitQuest(true);
				}
				else if (player.isClanLeader())
				{
					if (st.hasQuestItems(PROOF_ASPIRATION))
					{
						htmltext = "30760-03.htm";
						st.exitQuest(true);
					}
					else if (player.getClan().getLevel() != 4)
					{
						htmltext = "30760-02.htm";
						st.exitQuest(true);
					}
					else
					{
						htmltext = "30760-04.htm";
					}
				}
				else
				{
					htmltext = "30760-04t.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				int memberCond = 0;
				if (getClanLeaderQuestState(player, npc) != null)
				{
					memberCond = getClanLeaderQuestState(player, npc).getInt("cond");
				}
				
				switch (npc.getNpcId())
				{
					case GUSTAF:
						if (player.isClanLeader())
						{
							if (cond == 1)
							{
								htmltext = "30760-09.htm";
							}
							else if (cond == 2)
							{
								htmltext = "30760-10.htm";
							}
							else if (cond == 3)
							{
								htmltext = "30760-11.htm";
							}
							else if (cond == 4)
							{
								htmltext = "30760-13.htm";
							}
							else if (cond == 5)
							{
								htmltext = "30760-14.htm";
							}
							else if (cond == 6)
							{
								htmltext = "30760-15.htm";
							}
							else if (cond == 7)
							{
								htmltext = "30760-17.htm";
							}
							else if (cond == 12)
							{
								htmltext = "30760-19.htm";
							}
							else if (cond == 13)
							{
								htmltext = "30760-24.htm";
							}
							else
							{
								htmltext = "30760-18.htm";
							}
						}
						else
						{
							if (memberCond == 3)
							{
								htmltext = "30760-11t.htm";
							}
							else if (memberCond == 4)
							{
								htmltext = "30760-15t.htm";
							}
							else if (memberCond == 12)
							{
								htmltext = "30760-19t.htm";
							}
							else if (memberCond == 13)
							{
								htmltext = "30766-24t.htm";
							}
						}
						break;
					
					case MARTIEN:
						if (player.isClanLeader())
						{
							if (cond == 1)
							{
								htmltext = "30645-02.htm";
							}
							else if (cond == 2)
							{
								if ((st.getQuestItemsCount(MI_DRAKE_EGGS) > 9) && (st.getQuestItemsCount(BL_WYRM_EGGS) > 9) && (st.getQuestItemsCount(DRAKE_EGGS) > 9) && (st.getQuestItemsCount(TH_WYRM_EGGS) > 9))
								{
									htmltext = "30645-05.htm";
									st.set("cond", "3");
									st.takeItems(MI_DRAKE_EGGS, -1);
									st.takeItems(BL_WYRM_EGGS, -1);
									st.takeItems(DRAKE_EGGS, -1);
									st.takeItems(TH_WYRM_EGGS, -1);
								}
								else
								{
									htmltext = "30645-04.htm";
								}
							}
							else if (cond == 3)
							{
								htmltext = "30645-07.htm";
							}
							else
							{
								htmltext = "30645-08.htm";
							}
						}
						else
						{
							if ((memberCond == 1) || (memberCond == 2) || (memberCond == 3))
							{
								htmltext = "30645-01.htm";
							}
						}
						break;
					
					case LUTZ:
						if (player.isClanLeader() && (cond == 2))
						{
							if (st.getInt("lutz") == 1)
							{
								htmltext = "30762-03.htm";
							}
							else
							{
								htmltext = "30762-01.htm";
							}
						}
						break;
					
					case KURTZ:
						if (player.isClanLeader() && (cond == 2))
						{
							if (st.getInt("kurt") == 1)
							{
								htmltext = "30763-03.htm";
							}
							else
							{
								htmltext = "30763-01.htm";
							}
						}
						break;
					
					case FRITZ:
						if (player.isClanLeader() && (cond == 2))
						{
							if (st.getInt("fritz") == 1)
							{
								htmltext = "30761-03.htm";
							}
							else
							{
								htmltext = "30761-01.htm";
							}
						}
						break;
					
					case KUSTO:
						if (player.isClanLeader())
						{
							if (st.getQuestItemsCount(BROOCH) == 1)
							{
								if (st.getInt("kurt") == 0)
								{
									htmltext = "30512-01.htm";
								}
								else if (st.getInt("kurt") == 1)
								{
									htmltext = "30512-02.htm";
								}
								else
								{
									htmltext = "30512-04.htm";
								}
							}
						}
						else
						{
							if ((memberCond > 2) && (memberCond < 6))
							{
								htmltext = "30512-01a.htm";
							}
						}
						break;
					
					case BALTHAZAR:
						if (player.isClanLeader())
						{
							if (cond == 4)
							{
								if (st.getInt("kurt") == 2)
								{
									htmltext = "30764-04.htm";
								}
								else
								{
									htmltext = "30764-02.htm";
								}
							}
							else if (cond == 5)
							{
								if ((st.getQuestItemsCount(POWER_STONE) > 9) && (st.getQuestItemsCount(NEBULITE_CRYSTALS) > 9))
								{
									htmltext = "30764-08.htm";
									st.takeItems(POWER_STONE, -1);
									st.takeItems(NEBULITE_CRYSTALS, -1);
									st.takeItems(BROOCH, -1);
									st.set("cond", "6");
								}
								else
								{
									htmltext = "30764-07.htm";
								}
							}
							else if (cond == 6)
							{
								htmltext = "30764-09.htm";
							}
						}
						else
						{
							if (memberCond == 4)
							{
								htmltext = "30764-01.htm";
							}
						}
						break;
					
					case RODEMAI:
						if (player.isClanLeader())
						{
							if (cond == 7)
							{
								htmltext = "30868-02.htm";
							}
							else if (cond == 8)
							{
								htmltext = "30868-05.htm";
							}
							else if (cond == 9)
							{
								htmltext = "30868-06.htm";
							}
							else if (cond == 10)
							{
								htmltext = "30868-08.htm";
							}
							else if (cond == 11)
							{
								htmltext = "30868-09.htm";
							}
							else if (cond == 12)
							{
								htmltext = "30868-11.htm";
							}
						}
						else
						{
							if (memberCond == 7)
							{
								htmltext = "30868-01.htm";
							}
							else if ((memberCond == 9) || (memberCond == 10))
							{
								htmltext = "30868-07.htm";
							}
						}
						break;
					
					case CLEO:
						if (player.isClanLeader())
						{
							if (cond == 8)
							{
								htmltext = "30766-02.htm";
							}
							else if (cond == 9)
							{
								htmltext = "30766-05.htm";
							}
							else if (cond == 10)
							{
								htmltext = "30766-06.htm";
							}
							else if ((cond == 11) || (cond == 12) || (cond == 13))
							{
								htmltext = "30766-07.htm";
							}
						}
						else
						{
							if (memberCond == 8)
							{
								htmltext = "30766-01.htm";
							}
						}
						break;
					
					case COFFER:
						if (player.isClanLeader())
						{
							if (cond == 10)
							{
								htmltext = "30765-01.htm";
							}
						}
						else
						{
							if (memberCond == 10)
							{
								htmltext = "30765-02.htm";
							}
						}
						break;
					
					case KALIS:
						if (player.isClanLeader())
						{
							htmltext = "30759-01.htm";
						}
						break;
					
					case ATHREA:
						if (player.isClanLeader())
						{
							htmltext = "30758-01.htm";
						}
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		QuestState st = null;
		st = getClanLeaderQuestState(player, npc);
		if ((st == null) || !st.isStarted())
		{
			return null;
		}
		
		for (int[] element : DROPLIST)
		{
			if (element[0] == npc.getNpcId())
			{
				final int cond = element[1];
				final int maxCount = element[2];
				final int chance = element[3];
				final int item1 = element[4];
				final int item2 = element[5];
				final int item3 = element[6];
				
				if (st.getInt("cond") == cond)
				{
					if (item1 != 0)
					{
						st.dropItems(item1, 1, maxCount, chance);
					}
					else
					{
						if (element[0] == IMPERIAL_GRAVEKEEPER)
						{
							final NpcInstance coffer = addSpawn(COFFER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 180000);
							coffer.broadcastNpcSay("Curse of the gods on the one that defiles the property of the empire!");
						}
						else if ((element[0] == GRAVE_GUARD) && (st.getQuestItemsCount(IMP_KEYS) < 6) && (Rnd.get(50) < chance))
						{
							addSpawn(GRAVE_KEYMASTER, player.getX(), player.getY(), player.getZ(), player.getHeading(), true, 0);
						}
					}
					if ((item2 != 0) && (item3 != 0))
					{
						if (Rnd.get(4) == 0)
						{
							st.dropItems(item2, 1, maxCount, chance);
						}
						else
						{
							st.dropItems(item3, 1, maxCount, chance);
						}
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		if ((npc.getMaxHp() / 2) > npc.getCurrentHp())
		{
			if (Rnd.get(100) < 4)
			{
				addSpawn(IMPERIAL_SLAVE, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			}
			else
			{
				attacker.teleToLocation(185462, 20342, -3250);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}