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
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.FortManager;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.Skill.SkillType;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.ArtefactInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.util.Util;

/**
 * @author _drunk_
 */
public class TakeCastle implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.TAKECASTLE
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId()))
		{
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(player);
		final Fort fort = FortManager.getInstance().getFort(player);
		if ((castle != null) && (fort == null))
		{
			if (!checkIfOkToCastSealOfRule(player, castle, true))
			{
				return;
			}
		}
		else if ((fort != null) && (castle == null))
		{
			if (!checkIfOkToCastFlagDisplay(player, fort, true))
			{
				return;
			}
		}
		
		if ((castle == null) && (fort == null))
		{
			return;
		}
		
		try
		{
			if ((castle != null) && (targets[0] instanceof ArtefactInstance))
			{
				castle.Engrave(player.getClan(), targets[0].getObjectId());
			}
			else if (fort != null)
			{
				fort.EndOfSiege(player.getClan());
			}
		}
		catch (Exception e)
		{
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
	/**
	 * Return true if character clan place a flag
	 * @param creature The Creature of the creature placing the flag
	 * @param isCheckOnly
	 * @return
	 */
	public static boolean checkIfOkToCastSealOfRule(Creature creature, boolean isCheckOnly)
	{
		final Castle castle = CastleManager.getInstance().getCastle(creature);
		final Fort fort = FortManager.getInstance().getFort(creature);
		if ((fort != null) && (castle == null))
		{
			return checkIfOkToCastFlagDisplay(creature, fort, isCheckOnly);
		}
		return checkIfOkToCastSealOfRule(creature, castle, isCheckOnly);
	}
	
	public static boolean checkIfOkToCastSealOfRule(Creature creature, Castle castle, boolean isCheckOnly)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return false;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		String message = "";
		if ((castle == null) || (castle.getCastleId() <= 0))
		{
			message = "You must be on castle ground to use this skill.";
		}
		else if ((player.getTarget() == null) || !player.getTarget().isArtefact())
		{
			message = "You can only use this skill on an artifact.";
		}
		else if (!castle.getSiege().isInProgress())
		{
			message = "You can only use this skill during a siege.";
		}
		else if (!Util.checkIfInRange(200, player, player.getTarget(), true) || (Math.abs(player.getZ() - player.getTarget().getZ()) > 40))
		{
			message = "You are not in range of the artifact.";
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
		{
			message = "You must be an attacker to use this skill.";
		}
		else
		{
			if (!isCheckOnly)
			{
				castle.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to engrave the ruler.", true);
			}
			return true;
		}
		
		if (!isCheckOnly && !message.isEmpty())
		{
			player.sendMessage(message);
		}
		
		return false;
	}
	
	public static boolean checkIfOkToCastFlagDisplay(Creature creature, boolean isCheckOnly)
	{
		return checkIfOkToCastFlagDisplay(creature, FortManager.getInstance().getFort(creature), isCheckOnly);
	}
	
	public static boolean checkIfOkToCastFlagDisplay(Creature creature, Fort fort, boolean isCheckOnly)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return false;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		String message = "";
		if ((fort == null) || (fort.getFortId() <= 0))
		{
			message = "You must be on fort ground to use this skill.";
		}
		else if ((player.getTarget() == null) && !player.getTarget().isArtefact())
		{
			message = "You can only use this skill on an flagpole.";
		}
		else if (!fort.getSiege().isInProgress())
		{
			message = "You can only use this skill during a siege.";
		}
		else if (!Util.checkIfInRange(200, player, player.getTarget(), true))
		{
			message = "You are not in range of the flagpole.";
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			message = "You must be an attacker to use this skill.";
		}
		else
		{
			if (!isCheckOnly)
			{
				fort.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to raise flag.", true);
			}
			return true;
		}
		
		if (!isCheckOnly && !message.isEmpty())
		{
			player.sendMessage(message);
		}
		
		return false;
	}
}
