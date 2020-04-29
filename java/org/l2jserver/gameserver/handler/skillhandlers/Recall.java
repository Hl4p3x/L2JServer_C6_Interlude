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

import org.l2jserver.gameserver.enums.TeleportWhereType;
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
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.RECALL
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		try
		{
			if (creature instanceof PlayerInstance)
			{
				final PlayerInstance instance = (PlayerInstance) creature;
				if (instance.isInOlympiadMode())
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH));
					return;
				}
				
				// Checks summoner not in siege zone
				if (creature.isInsideZone(ZoneId.SIEGE))
				{
					((PlayerInstance) creature).sendMessage("You cannot summon in siege zone.");
					return;
				}
				
				if (creature.isInsideZone(ZoneId.PVP))
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_DURING_COMBAT));
					return;
				}
				
				if ((GrandBossManager.getInstance().getZone(instance) != null) && !instance.isGM())
				{
					instance.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
					return;
				}
			}
			
			for (WorldObject target1 : targets)
			{
				if (!(target1 instanceof Creature))
				{
					continue;
				}
				
				final Creature target = (Creature) target1;
				if (target instanceof PlayerInstance)
				{
					final PlayerInstance targetChar = (PlayerInstance) target;
					if (targetChar.isFestivalParticipant())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't use escape skill in a festival."));
						continue;
					}
					
					if ((targetChar._inEventCTF && CTF.isStarted()) || (targetChar._inEventTvT && TvT.isStarted()) || (targetChar._inEventDM && DM.hasStarted()) || (targetChar._inEventVIP && VIP._started))
					{
						targetChar.sendMessage("You can't use escape skill in Event.");
						continue;
					}
					
					if (targetChar.isInJail())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't escape from jail."));
						continue;
					}
					
					if (targetChar.isInDuel())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't use escape skills during a duel."));
						continue;
					}
					
					if (targetChar.isAlikeDead())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if (targetChar.isInStoreMode())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if ((GrandBossManager.getInstance().getZone(targetChar) != null) && !targetChar.isGM())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					if (targetChar.isInOlympiadMode())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD));
						continue;
					}
					
					if (targetChar.isInsideZone(ZoneId.PVP))
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
				}
				
				target.teleToLocation(TeleportWhereType.TOWN);
			}
			
			if (skill.isMagic() && skill.useSpiritShot())
			{
				if (creature.checkBss())
				{
					creature.removeBss();
				}
				if (creature.checkSps())
				{
					creature.removeSps();
				}
			}
			else if (skill.useSoulShot())
			{
				if (creature.checkSs())
				{
					creature.removeSs();
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