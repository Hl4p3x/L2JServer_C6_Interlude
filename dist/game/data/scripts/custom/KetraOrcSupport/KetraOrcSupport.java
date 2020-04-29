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
package custom.KetraOrcSupport;

import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.PlayerInventory;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.WareHouseWithdrawalList;
import org.l2jserver.gameserver.util.Util;

/**
 * This script supports :
 * <ul>
 * <li>Ketra Orc Village functions</li>
 * <li>Quests failures && alliance downgrade if you kill an allied mob.</li>
 * <li>Petrification effect in case an allied player helps a neutral or enemy.</li>
 * </ul>
 */
public class KetraOrcSupport extends Quest
{
	private static final int KADUN = 31370; // Hierarch
	private static final int WAHKAN = 31371; // Messenger
	private static final int ASEFA = 31372; // Soul Guide
	private static final int ATAN = 31373; // Grocer
	private static final int JAFF = 31374; // Warehouse Keeper
	private static final int JUMARA = 31375; // Trader
	private static final int KURFA = 31376; // Gate Keeper
	
	private static final int HORN = 7186;
	
	private static final int[] KETRAS =
	{
		21324,
		21325,
		21327,
		21328,
		21329,
		21331,
		21332,
		21334,
		21335,
		21336,
		21338,
		21339,
		21340,
		21342,
		21343,
		21344,
		21345,
		21346,
		21347,
		21348,
		21349
	};
	
	private static final int[][] BUFF =
	{
		{
			4359,
			2
		}, // Focus: Requires 2 Buffalo Horns
		{
			4360,
			2
		}, // Death Whisper: Requires 2 Buffalo Horns
		{
			4345,
			3
		}, // Might: Requires 3 Buffalo Horns
		{
			4355,
			3
		}, // Acumen: Requires 3 Buffalo Horns
		{
			4352,
			3
		}, // Berserker: Requires 3 Buffalo Horns
		{
			4354,
			3
		}, // Vampiric Rage: Requires 3 Buffalo Horns
		{
			4356,
			6
		}, // Empower: Requires 6 Buffalo Horns
		{
			4357,
			6
		}
		// Haste: Requires 6 Buffalo Horns
	};
	
	private static final Skill VARKA_KETRA_PETRIFICATION = SkillTable.getInstance().getInfo(4578, 1);
	
	/**
	 * Names of missions which will be automatically dropped if the alliance is broken.
	 */
	private static final String[] ketraMissions =
	{
		"Q605_AllianceWithKetraOrcs",
		"Q606_WarWithVarkaSilenos",
		"Q607_ProveYourCourage",
		"Q608_SlayTheEnemyCommander",
		"Q609_MagicalPowerOfWater_Part1",
		"Q610_MagicalPowerOfWater_Part2"
	};
	
	public KetraOrcSupport()
	{
		super(-1, "custom");
		
		addFirstTalkId(KADUN, WAHKAN, ASEFA, ATAN, JAFF, JUMARA, KURFA);
		addTalkId(ASEFA, JAFF, KURFA);
		addStartNpc(JAFF, KURFA);
		
		// Verify if the killer didn't kill an allied mob. Test his party aswell.
		addKillId(KETRAS);
		
		// Verify if an allied is healing/buff an enemy. Petrify him if it's the case.
		addSkillUseId(KETRAS);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (Util.isDigit(event))
		{
			final int[] buffInfo = BUFF[Integer.parseInt(event)];
			if (st.getQuestItemsCount(HORN) >= buffInfo[1])
			{
				htmltext = "31372-4.htm";
				st.takeItems(HORN, buffInfo[1]);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(buffInfo[0], 1));
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
			}
		}
		else if (event.equals("Withdraw"))
		{
			if (player.getWarehouse().getSize() == 0)
			{
				htmltext = "31374-0.htm";
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.setActiveWarehouse(player.getWarehouse());
				player.sendPacket(new WareHouseWithdrawalList(player, 1));
			}
		}
		else if (event.equals("Teleport"))
		{
			switch (player.getAllianceWithVarkaKetra())
			{
				case 4:
					htmltext = "31376-4.htm";
					break;
				case 5:
					htmltext = "31376-5.htm";
					break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		final int allianceLevel = player.getAllianceWithVarkaKetra();
		
		switch (npc.getNpcId())
		{
			case KADUN:
				if (allianceLevel > 0)
				{
					htmltext = "31370-friend.htm";
				}
				else
				{
					htmltext = "31370-no.htm";
				}
				break;
			
			case WAHKAN:
				if (allianceLevel > 0)
				{
					htmltext = "31371-friend.htm";
				}
				else
				{
					htmltext = "31371-no.htm";
				}
				break;
			
			case ASEFA:
				st.setState(State.STARTED);
				if (allianceLevel < 1)
				{
					htmltext = "31372-3.htm";
				}
				else if ((allianceLevel < 3) && (allianceLevel > 0))
				{
					htmltext = "31372-1.htm";
				}
				else if (allianceLevel > 2)
				{
					if (st.hasQuestItems(HORN))
					{
						htmltext = "31372-4.htm";
					}
					else
					{
						htmltext = "31372-2.htm";
					}
				}
				break;
			
			case ATAN:
				if (player.getKarma() >= 1)
				{
					htmltext = "31373-pk.htm";
				}
				else if (allianceLevel <= 0)
				{
					htmltext = "31373-no.htm";
				}
				else if ((allianceLevel == 1) || (allianceLevel == 2))
				{
					htmltext = "31373-1.htm";
				}
				else
				{
					htmltext = "31373-2.htm";
				}
				break;
			
			case JAFF:
				switch (allianceLevel)
				{
					case 1:
						htmltext = "31374-1.htm";
						break;
					case 2:
					case 3:
						htmltext = "31374-2.htm";
						break;
					default:
						if (allianceLevel <= 0)
						{
							htmltext = "31374-no.htm";
						}
						else if (player.getWarehouse().getSize() == 0)
						{
							htmltext = "31374-3.htm";
						}
						else
						{
							htmltext = "31374-4.htm";
						}
						break;
				}
				break;
			
			case JUMARA:
				switch (allianceLevel)
				{
					case 2:
						htmltext = "31375-1.htm";
						break;
					case 3:
					case 4:
						htmltext = "31375-2.htm";
						break;
					case 5:
						htmltext = "31375-3.htm";
						break;
					default:
						htmltext = "31375-no.htm";
						break;
				}
				break;
			
			case KURFA:
				if (allianceLevel <= 0)
				{
					htmltext = "31376-no.htm";
				}
				else if ((allianceLevel > 0) && (allianceLevel < 4))
				{
					htmltext = "31376-1.htm";
				}
				else if (allianceLevel == 4)
				{
					htmltext = "31376-2.htm";
				}
				else
				{
					htmltext = "31376-3.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final Party party = player.getParty();
		if (party != null)
		{
			for (PlayerInstance partyMember : party.getPartyMembers())
			{
				testKetraDemote(partyMember);
			}
		}
		else
		{
			testKetraDemote(player);
		}
		return null;
	}
	
	@Override
	public String onSkillUse(NpcInstance npc, PlayerInstance caster, Skill skill)
	{
		// Caster is an allied.
		if (caster.isAlliedWithKetra())
		{
			// Caster's skill is a positive effect ? Go further.
			switch (skill.getSkillType())
			{
				case BUFF:
				case HEAL:
				case HEAL_PERCENT:
				case HEAL_STATIC:
				case BALANCE_LIFE:
				case HOT:
					for (WorldObject target : skill.getTargetList(caster))
					{
						// Character isn't existing, or is current caster, we drop check.
						if ((target == null) || (target == caster))
						{
							continue;
						}
						
						// Target isn't a summon nor a player, we drop check.
						if (!(target instanceof Playable))
						{
							continue;
						}
						
						// Retrieve the player behind that target.
						final PlayerInstance player = target.getActingPlayer();
						
						// Character is dead.
						if (player.isDead())
						{
							continue;
						}
						
						// If player is neutral or enemy, go further.
						if (!(player.isAlliedWithKetra()))
						{
							// If the NPC got that player registered in aggro list, go further.
							if (((Attackable) npc).getAggroList().containsKey(player))
							{
								// Save current target for future use.
								final WorldObject oldTarget = npc.getTarget();
								
								// Curse the heretic or his pet.
								npc.setTarget((player.isPet() && (player.getPet() != null)) ? caster.getPet() : caster);
								npc.doCast(VARKA_KETRA_PETRIFICATION);
								
								// Revert to old target && drop the loop.
								npc.setTarget(oldTarget);
								break;
							}
						}
					}
					break;
			}
		}
		
		// Continue normal behavior.
		return null;
	}
	
	/**
	 * That method drops current alliance and retrograde badge.<br>
	 * If any Varka quest is in progress, it stops the quest (and drop all related qItems) :
	 * @param player The player to check.
	 */
	private void testKetraDemote(PlayerInstance player)
	{
		if (player.isAlliedWithKetra())
		{
			// Drop the alliance (old friends become aggro).
			player.setAllianceWithVarkaKetra(0);
			
			final PlayerInventory inventory = player.getInventory();
			
			// Drop by 1 the level of that alliance (symbolized by a quest item).
			for (int i = 7215; i >= 7211; i--)
			{
				final ItemInstance item = inventory.getItemByItemId(i);
				if (item != null)
				{
					// Destroy the badge.
					player.destroyItemByItemId("Quest", i, item.getCount(), player, true);
					
					// Badge lvl 1 ; no addition of badge of lower level.
					if (i != 7211)
					{
						player.addItem("Quest", i - 1, 1, player, true);
					}
					break;
				}
			}
			
			for (String mission : ketraMissions)
			{
				final QuestState pst = player.getQuestState(mission);
				if (pst != null)
				{
					pst.exitQuest(true);
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new KetraOrcSupport();
	}
}