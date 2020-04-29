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
package quests.Q372_LegacyOfInsolence;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;

public class Q372_LegacyOfInsolence extends Quest
{
	// NPCs
	private static final int WALDERAL = 30844;
	private static final int PATRIN = 30929;
	private static final int HOLLY = 30839;
	private static final int CLAUDIA = 31001;
	private static final int DESMOND = 30855;
	
	// Monsters
	private static final int[][] MONSTERS_DROPS =
	{
		{ // npcId
			20817,
			20821,
			20825,
			20829,
			21069,
			21063
		}, // parchment (red, blue, black, white)
		{
			5966,
			5966,
			5966,
			5967,
			5968,
			5969
		}, // rate
		{
			300000,
			400000,
			460000,
			400000,
			250000,
			250000
		}
	};
	
	// Items
	private static final int[][] SCROLLS =
	{
		// Walderal => 13 blueprints => parts, recipes.
		{
			5989,
			6001
		},
		// Holly -> 5x Imperial Genealogy -> Dark Crystal parts/Adena
		{
			5984,
			5988
		},
		// Patrin -> 5x Ancient Epic -> Tallum parts/Adena
		{
			5979,
			5983
		},
		// Claudia -> 7x Revelation of the Seals -> Nightmare parts/Adena
		{
			5972,
			5978
		},
		// Desmond -> 7x Revelation of the Seals -> Majestic parts/Adena
		{
			5972,
			5978
		}
	};
	
	// Rewards matrice.
	private static final int[][][] REWARDS_MATRICE =
	{
		// Walderal DC choice
		{
			{
				13,
				5496
			},
			{
				26,
				5508
			},
			{
				40,
				5525
			},
			{
				58,
				5368
			},
			{
				76,
				5392
			},
			{
				100,
				5426
			}
		},
		// Walderal Tallum choice
		{
			{
				13,
				5497
			},
			{
				26,
				5509
			},
			{
				40,
				5526
			},
			{
				58,
				5370
			},
			{
				76,
				5394
			},
			{
				100,
				5428
			}
		},
		// Walderal NM choice
		{
			{
				20,
				5502
			},
			{
				40,
				5514
			},
			{
				58,
				5527
			},
			{
				73,
				5380
			},
			{
				87,
				5404
			},
			{
				100,
				5430
			}
		},
		// Walderal Maja choice
		{
			{
				20,
				5503
			},
			{
				40,
				5515
			},
			{
				58,
				5528
			},
			{
				73,
				5382
			},
			{
				87,
				5406
			},
			{
				100,
				5432
			}
		},
		// Holly DC parts + adenas.
		{
			{
				33,
				5496
			},
			{
				66,
				5508
			},
			{
				89,
				5525
			},
			{
				100,
				57
			}
		},
		// Patrin Tallum parts + adenas.
		{
			{
				33,
				5497
			},
			{
				66,
				5509
			},
			{
				89,
				5526
			},
			{
				100,
				57
			}
		},
		// Claudia NM parts + adenas.
		{
			{
				35,
				5502
			},
			{
				70,
				5514
			},
			{
				87,
				5527
			},
			{
				100,
				57
			}
		},
		// Desmond Maja choice
		{
			{
				35,
				5503
			},
			{
				70,
				5515
			},
			{
				87,
				5528
			},
			{
				100,
				57
			}
		}
	};
	
	public Q372_LegacyOfInsolence()
	{
		super(372, "Legacy of Insolence");
		
		addStartNpc(WALDERAL);
		addTalkId(WALDERAL, PATRIN, HOLLY, CLAUDIA, DESMOND);
		
		addKillId(MONSTERS_DROPS[0]);
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
		
		if (event.equals("30844-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equals("30844-05b.htm"))
		{
			if (st.getInt("cond") == 1)
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equals("30844-07.htm"))
		{
			for (int blueprint = 5989; blueprint <= 6001; blueprint++)
			{
				if (!st.hasQuestItems(blueprint))
				{
					htmltext = "30844-06.htm";
					break;
				}
			}
		}
		else if (event.startsWith("30844-07-"))
		{
			checkAndRewardItems(st, 0, Integer.parseInt(event.substring(9, 10)), WALDERAL);
		}
		else if (event.equals("30844-09.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 59) ? "30844-01.htm" : "30844-02.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case WALDERAL:
						htmltext = "30844-05.htm";
						break;
					
					case HOLLY:
						htmltext = checkAndRewardItems(st, 1, 4, HOLLY);
						break;
					
					case PATRIN:
						htmltext = checkAndRewardItems(st, 2, 5, PATRIN);
						break;
					
					case CLAUDIA:
						htmltext = checkAndRewardItems(st, 3, 6, CLAUDIA);
						break;
					
					case DESMOND:
						htmltext = checkAndRewardItems(st, 4, 7, DESMOND);
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final PlayerInstance partyMember = getRandomPartyMemberState(player, npc, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		final int npcId = npc.getNpcId();
		for (int i = 0; i < MONSTERS_DROPS[0].length; i++)
		{
			if (MONSTERS_DROPS[0][i] == npcId)
			{
				partyMember.getQuestState(getName()).dropItems(MONSTERS_DROPS[1][i], 1, 0, MONSTERS_DROPS[2][i]);
				break;
			}
		}
		return null;
	}
	
	private static String checkAndRewardItems(QuestState st, int itemType, int rewardType, int npcId)
	{
		// Retrieve array with items to check.
		final int[] itemsToCheck = SCROLLS[itemType];
		
		// Check set of items.
		for (int item = itemsToCheck[0]; item <= itemsToCheck[1]; item++)
		{
			if (!st.hasQuestItems(item))
			{
				return npcId + ((npcId == WALDERAL) ? "-07a.htm" : "-01.htm");
			}
		}
		
		// Remove set of items.
		for (int item = itemsToCheck[0]; item <= itemsToCheck[1]; item++)
		{
			st.takeItems(item, 1);
		}
		
		// Retrieve array with rewards.
		final int[][] rewards = REWARDS_MATRICE[rewardType];
		final int chance = Rnd.get(100);
		for (int[] reward : rewards)
		{
			if (chance < reward[0])
			{
				st.rewardItems(reward[1], 1);
				return npcId + "-02.htm";
			}
		}
		
		return npcId + ((npcId == WALDERAL) ? "-07a.htm" : "-01.htm");
	}
}