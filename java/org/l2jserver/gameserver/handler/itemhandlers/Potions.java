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
package org.l2jserver.gameserver.handler.itemhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Effect.EffectType;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.event.VIP;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Potions implements IItemHandler
{
	protected static final Logger LOGGER = Logger.getLogger(Potions.class.getName());
	private int _herbstask = 0;
	
	private static Map<Integer, PotionsSkills> potions = new HashMap<>();
	
	private static void loadPotions()
	{
		for (PotionsSkills potionSkill : PotionsSkills.values())
		{
			potions.put(potionSkill.potionId, potionSkill);
		}
	}
	
	public static PotionsSkills getSkillsForPotion(Integer potionId)
	{
		if (potions.isEmpty())
		{
			loadPotions();
		}
		return potions.get(potionId);
	}
	
	public static List<Integer> getPotionsForSkill(Integer skillId, Integer skillLevel)
	{
		if (potions.isEmpty())
		{
			loadPotions();
		}
		
		final List<Integer> outputPotions = new ArrayList<>();
		for (Entry<Integer, PotionsSkills> entry : potions.entrySet())
		{
			Map<Integer, Integer> itemSkills = null;
			if (entry.getValue() != null)
			{
				itemSkills = entry.getValue().skills;
				if ((itemSkills.get(skillId) != null) && (itemSkills.get(skillId).equals(skillLevel)))
				{
					outputPotions.add(entry.getKey());
				}
			}
		}
		
		return outputPotions;
	}
	
	/** Task for Herbs */
	private class HerbTask implements Runnable
	{
		private final PlayerInstance _player;
		private final int _magicId;
		private final int _level;
		
		HerbTask(PlayerInstance activeChar, int magicId, int level)
		{
			_player = activeChar;
			_magicId = magicId;
			_level = level;
		}
		
		@Override
		public void run()
		{
			try
			{
				usePotion(_player, _magicId, _level);
			}
			catch (Throwable t)
			{
				LOGGER.warning(t.getMessage());
			}
		}
	}
	
	private static final int[] ITEM_IDS =
	{
		65,
		725,
		726,
		727,
		728,
		733,
		734,
		735,
		1060,
		1061,
		1062,
		1073,
		1374,
		1375,
		1539,
		1540,
		4667,
		4679,
		4680,
		5283,
		5591,
		5592,
		6035,
		6036,
		6652,
		6653,
		6654,
		6655,
		8193,
		8194,
		8195,
		8196,
		8197,
		8198,
		8199,
		8200,
		8201,
		8202,
		8600,
		8601,
		8602,
		8603,
		8604,
		8605,
		8606,
		8607,
		8608,
		8609,
		8610,
		8611,
		8612,
		8613,
		8614,
		// elixir of life
		8622,
		8623,
		8624,
		8625,
		8626,
		8627,
		// elixir of Strength
		8628,
		8629,
		8630,
		8631,
		8632,
		8633,
		// elixir of cp
		8634,
		8635,
		8636,
		8637,
		8638,
		8639,
		// primavel potions
		8786,
		8787
	};
	
	@Override
	public synchronized void useItem(Playable playable, ItemInstance item)
	{
		if (playable instanceof PlayerInstance)
		{
			PlayerInstance activeChar;
			activeChar = (PlayerInstance) playable;
			if (activeChar._inEventTvT && TvT.isStarted() && !Config.TVT_ALLOW_POTIONS)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar._inEventDM && DM.hasStarted() && !Config.DM_ALLOW_POTIONS)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar._inEventCTF && CTF.isStarted() && !Config.CTF_ALLOW_POTIONS)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar._inEventVIP && VIP._started)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar.isInOlympiadMode())
			{
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
				return;
			}
			
			if (!Config.ALLOW_POTS_IN_PVP && (activeChar.isInDuel() || (activeChar.getPvpFlag() != 0)))
			{
				activeChar.sendMessage("You Cannot Use Potions In PvP!");
				return;
			}
			
			final int itemId = item.getItemId();
			switch (itemId)
			{
				// MANA POTIONS
				case 726: // mana drug, xml: 2003
				{
					if (!isEffectReplaceable(playable, EffectType.MANA_HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2003, 1);
					break;
				}
				case 728: // mana_potion, xml: 2005
				{
					usePotion(activeChar, 2005, 1);
					break;
				}
				// HEALING AND SPEED POTIONS
				case 65: // red_potion, xml: 2001
				{
					usePotion(activeChar, 2001, 1);
					break;
				}
				case 725: // healing_drug, xml: 2002
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2002, 1);
					break;
				}
				case 727: // _healing_potion, xml: 2032
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2032, 1);
					break;
				}
				case 733: // endeavor_potion
				{
					usePotion(activeChar, 2010, 1);
					break;
				}
				case 734: // quick_step_potion, xml: 2011
				{
					usePotion(activeChar, 2011, 1);
					break;
				}
				case 735: // swift_attack_potion, xml: 2012
				{
					usePotion(activeChar, 2012, 1);
					break;
				}
				case 1060: // lesser_healing_potion,
				case 1073: // beginner's potion, xml: 2031
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2031, 1);
					break;
				}
				case 1061: // healing_potion, xml: 2032
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2032, 1);
					break;
				}
				case 1062: // haste_potion, xml: 2011
				{
					usePotion(activeChar, 2011, 1);
					break;
				}
				case 1374: // adv_quick_step_potion, xml: 2034
				{
					usePotion(activeChar, 2034, 1);
					break;
				}
				case 1375: // adv_swift_attack_potion, xml: 2035
				{
					usePotion(activeChar, 2035, 1);
					break;
				}
				case 1539: // greater_healing_potion, xml: 2037
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2037, 1);
					break;
				}
				case 1540: // quick_healing_potion, xml: 2038
				{
					usePotion(activeChar, 2038, 1);
					break;
				}
				case 4667: // potion_of_critical_escape
				{
					usePotion(activeChar, 2074, 1);
					break;
				}
				case 4679: // bless of eva
				{
					usePotion(activeChar, 2076, 1);
					break;
				}
				case 4680: // rsk_damage_shield_potion
				{
					usePotion(activeChar, 2077, 1);
					break;
				}
				case 5283: // Rice Cake, xml: 2136
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2136, 1);
					break;
				}
				case 5591: // CP
				{
					usePotion(activeChar, 2166, 1);
					break;
				}
				case 5592: // Greater CP
				{
					usePotion(activeChar, 2166, 2);
					break;
				}
				case 6035: // Magic Haste Potion, xml: 2169
				{
					usePotion(activeChar, 2169, 1);
					break;
				}
				case 6036: // Greater Magic Haste Potion, xml: 2169
				{
					usePotion(activeChar, 2169, 2);
					break;
				}
				// ELIXIR
				case 8622:
				case 8623:
				case 8624:
				case 8625:
				case 8626:
				case 8627:
				{
					// elixir of Life
					if (!isEffectReplaceable(activeChar, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					if (((itemId == 8622) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8623) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8624) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8625) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8626) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8627) && (activeChar.getExpertiseIndex() == 5)))
					{
						usePotion(activeChar, 2287, (activeChar.getExpertiseIndex() + 1));
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE_THIS_ITEM_CANNOT_BE_USED); // INCOMPATIBLE_ITEM_GRADE
						sm.addItemName(itemId);
						activeChar.sendPacket(sm);
					}
					break;
				}
				case 8628:
				case 8629:
				case 8630:
				case 8631:
				case 8632:
				case 8633:
				{
					// elixir of Strength
					if (!isEffectReplaceable(activeChar, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					if (((itemId == 8628) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8629) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8630) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8631) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8632) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8633) && (activeChar.getExpertiseIndex() == 5)))
					{
						usePotion(activeChar, 2288, (activeChar.getExpertiseIndex() + 1));
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE_THIS_ITEM_CANNOT_BE_USED); // INCOMPATIBLE_ITEM_GRADE
						sm.addItemName(itemId);
						activeChar.sendPacket(sm);
					}
					break;
				}
				case 8634:
				case 8635:
				case 8636:
				case 8637:
				case 8638:
				case 8639:
				{
					// elixir of cp
					if (!isEffectReplaceable(activeChar, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					if (((itemId == 8634) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8635) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8636) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8637) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8638) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8639) && (activeChar.getExpertiseIndex() == 5)))
					{
						usePotion(activeChar, 2289, (activeChar.getExpertiseIndex() + 1));
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE_THIS_ITEM_CANNOT_BE_USED); // INCOMPATIBLE_ITEM_GRADE
						sm.addItemName(itemId);
						activeChar.sendPacket(sm);
					}
					break;
				}
				// VALAKAS AMULETS
				case 6652: // Amulet Protection of Valakas
				{
					usePotion(activeChar, 2231, 1);
					break;
				}
				case 6653: // Amulet Flames of Valakas
				{
					usePotion(activeChar, 2233, 1);
					break;
				}
				case 6654: // Amulet Flames of Valakas
				{
					usePotion(activeChar, 2233, 1);
					break;
				}
				case 6655: // Amulet Slay Valakas
				{
					usePotion(activeChar, 2232, 1);
					break;
				}
				// HERBS
				case 8600: // Herb of Life
				{
					usePotion(activeChar, 2278, 1);
					break;
				}
				case 8601: // Greater Herb of Life
				{
					usePotion(activeChar, 2278, 2);
					break;
				}
				case 8602: // Superior Herb of Life
				{
					usePotion(activeChar, 2278, 3);
					break;
				}
				case 8603: // Herb of Mana
				{
					usePotion(activeChar, 2279, 1);
					break;
				}
				case 8604: // Greater Herb of Mane
				{
					usePotion(activeChar, 2279, 2);
					break;
				}
				case 8605: // Superior Herb of Mane
				{
					usePotion(activeChar, 2279, 3);
					break;
				}
				case 8606: // Herb of Strength
				{
					usePotion(activeChar, 2280, 1);
					break;
				}
				case 8607: // Herb of Magic
				{
					usePotion(activeChar, 2281, 1);
					break;
				}
				case 8608: // Herb of Atk. Spd.
				{
					usePotion(activeChar, 2282, 1);
					break;
				}
				case 8609: // Herb of Casting Spd.
				{
					usePotion(activeChar, 2283, 1);
					break;
				}
				case 8610: // Herb of Critical Attack
				{
					usePotion(activeChar, 2284, 1);
					break;
				}
				case 8611: // Herb of Speed
				{
					usePotion(activeChar, 2285, 1);
					break;
				}
				case 8612: // Herb of Warrior
				{
					usePotion(activeChar, 2280, 1);
					usePotion(activeChar, 2282, 1);
					usePotion(activeChar, 2284, 1);
					break;
				}
				case 8613: // Herb of Mystic
				{
					usePotion(activeChar, 2281, 1);
					usePotion(activeChar, 2283, 1);
					break;
				}
				case 8614: // Herb of Warrior
				{
					usePotion(activeChar, 2278, 3);
					usePotion(activeChar, 2279, 3);
					break;
				}
				// FISHERMAN POTIONS
				case 8193: // Fisherman's Potion - Green
				{
					if (playable.getSkillLevel(1315) <= 3)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 1);
					break;
				}
				case 8194: // Fisherman's Potion - Jade
				{
					if (playable.getSkillLevel(1315) <= 6)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 2);
					break;
				}
				case 8195: // Fisherman's Potion - Blue
				{
					if (playable.getSkillLevel(1315) <= 9)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 3);
					break;
				}
				case 8196: // Fisherman's Potion - Yellow
				{
					if (playable.getSkillLevel(1315) <= 12)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 4);
					break;
				}
				case 8197: // Fisherman's Potion - Orange
				{
					if (playable.getSkillLevel(1315) <= 15)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 5);
					break;
				}
				case 8198: // Fisherman's Potion - Purple
				{
					if (playable.getSkillLevel(1315) <= 18)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 6);
					break;
				}
				case 8199: // Fisherman's Potion - Red
				{
					if (playable.getSkillLevel(1315) <= 21)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 7);
					break;
				}
				case 8200: // Fisherman's Potion - White
				{
					if (playable.getSkillLevel(1315) <= 24)
					{
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						return;
					}
					usePotion(activeChar, 2274, 8);
					break;
				}
				case 8201: // Fisherman's Potion - Black
				{
					usePotion(activeChar, 2274, 9);
					break;
				}
				case 8202: // Fishing Potion
				{
					usePotion(activeChar, 2275, 1);
					break;
				}
				case 8786:
				{
					usePotion(activeChar, 2305, 1);
					break;
				}
				case 8787:
				{
					usePotion(activeChar, 2305, 1);
					break;
				}
			}
		}
		else if (playable instanceof PetInstance)
		{
			PetInstance activeChar;
			activeChar = ((PetInstance) playable);
			final int itemId = item.getItemId();
			switch (itemId)
			{
				// MANA POTIONS
				case 728: // mana_potion, xml: 2005
				{
					usePotion(activeChar, 2005, 1);
					break;
				}
				// HEALING AND SPEED POTIONS
				case 1060: // lesser_healing_potion,
				case 1073: // beginner's potion, xml: 2031
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2031, 1);
					break;
				}
				case 1061: // healing_potion, xml: 2032
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2032, 1);
					break;
				}
				case 1062: // haste_potion, xml: 2011
				{
					usePotion(activeChar, 2011, 1);
					break;
				}
				case 1539:
				{
					if (!isEffectReplaceable(playable, EffectType.HEAL_OVER_TIME, itemId))
					{
						return;
					}
					usePotion(activeChar, 2037, 1);
					break;
				}
				case 6035: // Magic Haste Potion, xml: 2169
				{
					usePotion(activeChar, 2169, 1);
					break;
				}
				default:
				{
					activeChar.getOwner().sendMessage("You can't use " + ItemTable.getInstance().getTemplate(itemId).getName() + " on summon.");
				}
			}
		}
	}
	
	private boolean isEffectReplaceable(Playable activeChar, Enum<EffectType> effectType, int itemId)
	{
		final Effect[] effects = activeChar.getAllEffects();
		if (effects == null)
		{
			return true;
		}
		
		for (Effect e : effects)
		{
			if ((e.getEffectType() == effectType) && e.getSkill().isPotion())
			{
				// One can reuse pots after 2/3 of their duration is over.
				// It would be faster to check if its > 10 but that would screw custom pot durations...
				if (e.getTaskTime() > ((e.getSkill().getBuffDuration() * 67) / 100000))
				{
					return true;
				}
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE);
				sm.addItemName(itemId);
				activeChar.sendPacket(sm);
				
				return false;
			}
		}
		return true;
	}
	
	public boolean usePotion(Playable player, int magicId, int level)
	{
		if (player instanceof PlayerInstance)
		{
			final PlayerInstance activeChar = player.getActingPlayer();
			if (activeChar.isCastingNow() && (magicId > 2277) && (magicId < 2285))
			{
				_herbstask += 100;
				ThreadPool.schedule(new HerbTask(activeChar, magicId, level), _herbstask);
			}
			else
			{
				if ((magicId > 2277) && (magicId < 2285) && (_herbstask >= 100))
				{
					_herbstask -= 100;
				}
				final Skill skill = SkillTable.getInstance().getInfo(magicId, level);
				if (skill != null)
				{
					// Return false if potion is in reuse
					// so is not destroyed from inventory
					if (activeChar.isSkillDisabled(skill))
					{
						if ((skill.getId() != 2166))
						{
							final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE);
							sm.addSkillName(skill.getId(), skill.getLevel());
							activeChar.sendPacket(sm);
						}
						// Cp potion message
						else if (skill.getId() == 2166)
						{
							if (skill.getLevel() == 2)
							{
								activeChar.sendMessage("Greater CP Potion is not available at this time: being prepared for reuse.");
							}
							else if (skill.getLevel() == 1)
							{
								activeChar.sendMessage("CP Potion is not available at this time: being prepared for reuse.");
							}
						}
						return false;
					}
					
					activeChar.doCast(skill);
					
					// only for Heal potions
					if ((magicId == 2031) || (magicId == 2032) || (magicId == 2037))
					{
						activeChar.shortBuffStatusUpdate(magicId, level, 15);
					}
					
					if ((!activeChar.isSitting() || skill.isPotion()))
					{
						return true;
					}
				}
			}
		}
		else if (player instanceof PetInstance)
		{
			final PetInstance activeChar = (PetInstance) player;
			final Skill skill = SkillTable.getInstance().getInfo(magicId, level);
			if (skill != null)
			{
				// Return false if potion is in reuse so is not destroyed from inventory
				if (activeChar.isSkillDisabled(skill))
				{
					if ((skill.getId() != 2166))
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE);
						sm.addSkillName(skill.getId(), skill.getLevel());
						activeChar.sendPacket(sm);
					}
					return false;
				}
				
				activeChar.doCast(skill);
				
				if (skill.isPotion())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	public static void deletePotionItem(Playable playable, Integer skillId, Integer skillLevel)
	{
		if (!(playable instanceof PlayerInstance) && !(playable instanceof Summon))
		{
			return;
		}
		
		final List<Integer> possiblePotions = getPotionsForSkill(skillId, skillLevel);
		if (!possiblePotions.isEmpty())
		{
			for (Integer potion : possiblePotions)
			{
				if ((potion >= 8600) && (potion <= 8614)) // herbs are directly destroyed
				{
					continue;
				}
				
				if (playable instanceof PlayerInstance)
				{
					final PlayerInstance activeChar = (PlayerInstance) playable;
					if (activeChar.getInventory().getInventoryItemCount(potion, 0) > 0)
					{
						final ItemInstance item = activeChar.getInventory().getItemByItemId(potion);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
					}
				}
				else if (playable instanceof Summon)
				{
					final Summon activeChar = (Summon) playable;
					if (activeChar.getInventory().getInventoryItemCount(potion, 0) > 0)
					{
						final ItemInstance item = activeChar.getInventory().getItemByItemId(potion);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
					}
				}
			}
		}
		else
		{
			LOGGER.warning("Attention: Can't destroy potion for skill " + skillId + " level " + skillLevel);
		}
	}
	
	enum PotionsSkills
	{
		mana_drug(726, 2003, 1),
		mana_potion(728, 2005, 1),
		red_potion(65, 2001, 1),
		healing_drug(725, 2002, 1),
		healing_potion_ring(727, 2032, 1),
		quick_step_potion(734, 2011, 1),
		swift_attack_potion(735, 2012, 1),
		lesser_healing_potion(1060, 2031, 1),
		beginner_s_potion(1073, 2031, 1),
		healing_potion(1061, 2032, 1),
		haste_potion(1062, 2011, 1),
		adv_quick_step_potion(1374, 2034, 1),
		adv_swift_attack_potion(1375, 2035, 1),
		greater_healing_potion(1539, 2037, 1),
		quick_healing_potion(1540, 2038, 1),
		bless_of_eva(4679, 2076, 1),
		endeavor_potion(733, 2010, 1),
		potion_of_critical_escape(4667, 2074, 1),
		rsk_damage_shield_potion(4680, 2077, 1),
		Rice_Cake(5283, 2136, 1),
		CP(5591, 2166, 1),
		Greater_CP(5592, 2166, 2),
		Magic_Haste_Potion(6035, 2169, 1),
		Greater_Magic_Haste_Potion(6036, 2169, 2),
		elixir_of_Life_nog(8622, 2287, 1),
		elixir_of_Life_d(8623, 2287, 2),
		elixir_of_Life_c(8624, 2287, 3),
		elixir_of_Life_b(8625, 2287, 4),
		elixir_of_Life_a(8626, 2287, 5),
		elixir_of_Life_s(8627, 2287, 6),
		elixir_of_Strength_nog(8628, 2288, 1),
		elixir_of_Strength_d(8629, 2288, 2),
		elixir_of_Strength_c(8630, 2288, 3),
		elixir_of_Strength_b(8631, 2288, 4),
		elixir_of_Strength_a(8632, 2288, 5),
		elixir_of_Strength_s(8633, 2288, 6),
		elixir_of_cp_nog(8634, 2289, 1),
		elixir_of_cp_d(8635, 2289, 2),
		elixir_of_cp_c(8636, 2289, 3),
		elixir_of_cp_b(8637, 2289, 4),
		elixir_of_cp_a(8638, 2289, 5),
		elixir_of_cp_s(8639, 2289, 6),
		Amulet_Protection_of_Valakas(6652, 2231, 1),
		Amulet_Flames_of_Valakas_1(6653, 2233, 1),
		Amulet_Flames_of_Valakas_2(6654, 2233, 1),
		Amulet_Slay_Valakas(6655, 2232, 1),
		Herb_of_Life(8600, 2278, 1),
		Greater_Herb_of_Life(8601, 2278, 2),
		Superior_Herb_of_Life(8602, 2278, 3),
		Herb_of_Mana(8603, 2279, 1),
		Greater_Herb_of_Mane(8604, 2279, 2),
		Superior_Herb_of_Mane(8605, 2279, 3),
		Herb_of_Strength(8606, 2280, 1),
		Herb_of_Magic(8607, 2281, 1),
		Herb_of_Atk_Spd(8608, 2282, 1),
		Herb_of_Casting_Spd(8609, 2283, 1),
		Herb_of_Critical_Attack(8610, 2284, 1),
		Herb_of_Speed(8611, 2285, 1),
		Herb_of_Warrior(8612, new Integer[]
		{
			2280,
			2282,
			2284
		}, new Integer[]
		{
			1,
			1,
			1
		}),
		Herb_of_Mystic(8613, new Integer[]
		{
			2281,
			2283
		}, new Integer[]
		{
			1,
			1
		}),
		Herb_of_Recovery(8614, new Integer[]
		{
			2278,
			2279
		}, new Integer[]
		{
			3,
			3
		}),
		
		Fisherman_s_Potion_Green(8193, 2274, 1),
		Fisherman_s_Potion_Jade(8194, 2274, 2),
		Fisherman_s_Potion_Blue(8195, 2274, 3),
		Fisherman_s_Potion_Yellow(8196, 2274, 4),
		Fisherman_s_Potion_Orange(8197, 2274, 5),
		Fisherman_s_Potion_Purple(8198, 2274, 6),
		Fisherman_s_Potion_Red(8199, 2274, 7),
		Fisherman_s_Potion_White(8200, 2274, 8),
		Fisherman_s_Potion_Black(8201, 2274, 9),
		Fishing_Potion(8202, 2275, 1),
		Primeval_Potion(8786, 2305, 1),
		Primeval_Potion1(8787, 2305, 1);
		
		public Integer potionId;
		public Map<Integer, Integer> skills = new HashMap<>();
		
		private PotionsSkills(int potionItem, int skillIdentifier, int skillLevel)
		{
			skills.put(skillIdentifier, skillLevel);
			potionId = potionItem;
		}
		
		private PotionsSkills(int potionItem, Integer[] skillIdentifiers, Integer[] skillLevels)
		{
			for (int i = 0; i < skillIdentifiers.length; i++)
			{
				skills.put(skillIdentifiers[i], skillLevels[i]); // each skill of a particular potion can have just 1 level, not more
			}
			potionId = potionItem;
		}
	}
}