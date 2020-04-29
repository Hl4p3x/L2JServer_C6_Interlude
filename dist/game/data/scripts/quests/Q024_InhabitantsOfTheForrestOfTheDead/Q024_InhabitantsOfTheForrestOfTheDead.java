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
package quests.Q024_InhabitantsOfTheForrestOfTheDead;

import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;

import quests.Q023_LidiasHeart.Q023_LidiasHeart;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q024_InhabitantsOfTheForrestOfTheDead extends Quest
{
	// NPCs
	private static final int DORIAN = 31389;
	private static final int WIZARD = 31522;
	private static final int TOMBSTONE = 31531;
	private static final int MAID_OF_LIDIA = 31532;
	// Items
	private static final int LETTER = 7065;
	private static final int HAIRPIN = 7148;
	private static final int TOTEM = 7151;
	private static final int FLOWER = 7152;
	private static final int SILVER_CROSS = 7153;
	private static final int BROKEN_SILVER_CROSS = 7154;
	private static final int SUSPICIOUS_TOTEM = 7156;
	
	public Q024_InhabitantsOfTheForrestOfTheDead()
	{
		super(24, "Inhabitants of the Forest of the Dead");
		
		addStartNpc(DORIAN);
		addTalkId(DORIAN, TOMBSTONE, MAID_OF_LIDIA, WIZARD);
		registerQuestItems(FLOWER, SILVER_CROSS, BROKEN_SILVER_CROSS, LETTER, HAIRPIN, TOTEM);
		addAggroRangeEnterId(25332);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "31389-02.htm":
			{
				qs.giveItems(FLOWER, 1);
				qs.set("cond", "1");
				qs.playSound("ItemSound.quest_accept");
				qs.setState(State.STARTED);
				break;
			}
			case "31389-11.htm":
			{
				qs.set("cond", "3");
				qs.playSound("ItemSound.quest_middle");
				qs.giveItems(SILVER_CROSS, 1);
				break;
			}
			case "31389-16.htm":
			{
				qs.playSound("InterfaceSound.charstat_open_01");
				break;
			}
			case "31389-17.htm":
			{
				qs.takeItems(BROKEN_SILVER_CROSS, -1);
				qs.giveItems(HAIRPIN, 1);
				qs.set("cond", "5");
				break;
			}
			case "31522-03.htm":
			{
				qs.takeItems(TOTEM, -1);
				break;
			}
			case "31522-07.htm":
			{
				qs.set("cond", "11");
				break;
			}
			case "31522-19.htm":
			{
				qs.giveItems(SUSPICIOUS_TOTEM, 1);
				qs.rewardExpAndSp(242105, 22529);
				qs.exitQuest(true);
				qs.playSound("ItemSound.quest_finish");
				break;
			}
			case "31531-02.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "2");
				qs.takeItems(FLOWER, -1);
				break;
			}
			case "31532-04.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.giveItems(LETTER, 1);
				qs.set("cond", "6");
				break;
			}
			case "31532-06.htm":
			{
				qs.takeItems(HAIRPIN, -1);
				qs.takeItems(LETTER, -1);
				break;
			}
			case "31532-16.htm":
			{
				qs.playSound("ItemSound.quest_middle");
				qs.set("cond", "9");
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		final int npcId = npc.getNpcId();
		final int state = qs.getState();
		if (state == State.COMPLETED)
		{
			if (npcId == WIZARD)
			{
				htmltext = "31522-20.htm";
			}
			else
			{
				htmltext = getAlreadyCompletedMsg();
			}
		}
		final int cond = qs.getInt("cond");
		if (npcId == DORIAN)
		{
			if (state == State.CREATED)
			{
				final QuestState qs2 = player.getQuestState(Q023_LidiasHeart.class.getSimpleName());
				if (qs2 != null)
				{
					if ((qs2.getState() == State.COMPLETED) && (player.getLevel() >= 65))
					{
						htmltext = "31389-01.htm";
					}
					else
					{
						htmltext = "31389-00.htm";
					}
				}
				else
				{
					htmltext = "31389-00.htm";
				}
			}
			else if (cond == 1)
			{
				htmltext = "31389-03.htm";
			}
			else if (cond == 2)
			{
				htmltext = "31389-04.htm";
			}
			else if (cond == 3)
			{
				htmltext = "31389-12.htm";
			}
			else if (cond == 4)
			{
				htmltext = "31389-13.htm";
			}
			else if (cond == 5)
			{
				htmltext = "31389-18.htm";
			}
		}
		else if (npcId == TOMBSTONE)
		{
			if (cond == 1)
			{
				qs.playSound("AmdSound.d_wind_loot_02");
				htmltext = "31531-01.htm";
			}
			else if (cond == 2)
			{
				htmltext = "31531-03.htm";
			}
		}
		else if (npcId == MAID_OF_LIDIA)
		{
			if (cond == 5)
			{
				htmltext = "31532-01.htm";
			}
			else if (cond == 6)
			{
				if ((qs.getQuestItemsCount(LETTER) > 0) && (qs.getQuestItemsCount(HAIRPIN) > 0))
				{
					htmltext = "31532-05.htm";
				}
				else
				{
					htmltext = "31532-07.htm";
				}
			}
			else if (cond == 9)
			{
				htmltext = "31532-16.htm";
			}
		}
		else if (npcId == WIZARD)
		{
			if (cond == 10)
			{
				htmltext = "31522-01.htm";
			}
			else if (cond == 11)
			{
				htmltext = "31522-08.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAggroRangeEnter(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		if (isPet)
		{
			npc.getAttackByList().remove(player.getPet());
		}
		else
		{
			npc.getAttackByList().remove(player);
			final QuestState qs = player.getQuestState(getName());
			if ((qs != null) && (qs.getQuestItemsCount(SILVER_CROSS) > 0))
			{
				qs.takeItems(SILVER_CROSS, -1);
				qs.giveItems(BROKEN_SILVER_CROSS, 1);
				qs.set("cond", "4");
				for (PlayerInstance nearby : npc.getKnownList().getKnownPlayers().values())
				{
					nearby.sendPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "That sign!"));
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}