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
package org.l2jserver.gameserver.handler.skillhandlers;

import org.l2jserver.gameserver.handler.ISkillHandler;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.event.VIP;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

public class SummonFriend implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SUMMON_FRIEND
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance activePlayer = (PlayerInstance) creature;
		if (!PlayerInstance.checkSummonerStatus(activePlayer))
		{
			return;
		}
		
		if (activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		if (activePlayer._inEvent)
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventCTF && CTF.isStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventDM && DM.hasStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventTvT && TvT.isStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventVIP && VIP._started)
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		
		// Checks summoner not in siege zone
		if (creature.isInsideZone(ZoneId.SIEGE))
		{
			((PlayerInstance) creature).sendMessage("You cannot summon in siege zone.");
			return;
		}
		
		// Checks summoner not in arenas, siege zones, jail
		if (activePlayer.isInsideZone(ZoneId.PVP))
		{
			activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_DURING_COMBAT);
			return;
		}
		
		if ((GrandBossManager.getInstance().getZone(activePlayer) != null) && !activePlayer.isGM())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return;
		}
		
		try
		{
			for (WorldObject target1 : targets)
			{
				if (!(target1 instanceof Creature))
				{
					continue;
				}
				
				final Creature target = (Creature) target1;
				if (creature == target)
				{
					continue;
				}
				
				if (target instanceof PlayerInstance)
				{
					final PlayerInstance targetChar = (PlayerInstance) target;
					if (!PlayerInstance.checkSummonTargetStatus(targetChar, activePlayer))
					{
						continue;
					}
					
					if (targetChar.isAlikeDead())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if (targetChar._inEvent)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventCTF)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventDM)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventTvT)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventVIP)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					
					if (targetChar.isInStoreMode())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					// Target cannot be in combat (or dead, but that's checked by TARGET_PARTY)
					if (targetChar.isRooted() || targetChar.isInCombat())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if ((GrandBossManager.getInstance().getZone(targetChar) != null) && !targetChar.isGM())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					// Check for the the target's festival status
					if (targetChar.isInOlympiadMode())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD));
						continue;
					}
					
					// Check for the the target's festival status
					if (targetChar.isFestivalParticipant())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					// Check for the target's jail status, arenas and siege zones
					if (targetChar.isInsideZone(ZoneId.PVP))
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					// Requires a Summoning Crystal
					/* if (targetChar.getInventory().getItemByItemId(8615) == null) */
					if ((targetChar.getInventory().getItemByItemId(8615) == null) && (skill.getId() != 1429)) // KidZor
					{
						((PlayerInstance) creature).sendMessage("Your target cannot be summoned while he hasn't got a Summoning Crystal");
						targetChar.sendMessage("You cannot be summoned while you haven't got a Summoning Crystal");
						continue;
					}
					
					if (!Util.checkIfInRange(0, creature, target, false))
					{
						// Check already summon
						if (!targetChar.teleportRequest((PlayerInstance) creature, skill))
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ALREADY_BEEN_SUMMONED);
							sm.addString(target.getName());
							creature.sendPacket(sm);
							continue;
						}
						
						// Summon friend
						if (skill.getId() == 1403)
						{
							// Send message
							final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
							confirm.addString(creature.getName());
							confirm.addZoneName(creature.getX(), creature.getY(), creature.getZ());
							confirm.addTime(30000);
							confirm.addRequesterId(creature.getObjectId());
							targetChar.sendPacket(confirm);
						}
						else
						{
							PlayerInstance.teleToTarget(targetChar, (PlayerInstance) creature, skill);
							targetChar.teleportRequest(null, null);
						}
					}
				}
			}
		}
		catch (Throwable e)
		{
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}