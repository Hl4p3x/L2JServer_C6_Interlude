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
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.WeaponType;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author _tomciaaa_
 */
public class StrSiegeAssault implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.STRSIEGEASSAULT
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		if (!creature.isRiding())
		{
			return;
		}
		
		if (!(player.getTarget() instanceof DoorInstance))
		{
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(player);
		final Fort fort = FortManager.getInstance().getFort(player);
		if ((castle == null) && (fort == null))
		{
			return;
		}
		
		if (castle != null)
		{
			if (!checkIfOkToUseStriderSiegeAssault(player, castle, true))
			{
				return;
			}
		}
		else if (!checkIfOkToUseStriderSiegeAssault(player, fort, true))
		{
			return;
		}
		
		try
		{
			final ItemInstance itemToTake = player.getInventory().getItemByItemId(skill.getItemConsumeId());
			if (!player.destroyItem("Consume", itemToTake.getObjectId(), skill.getItemConsume(), null, true))
			{
				return;
			}
			
			// damage calculation
			int damage = 0;
			for (WorldObject target2 : targets)
			{
				if (target2 == null)
				{
					continue;
				}
				
				final Creature target = (Creature) target2;
				final ItemInstance weapon = creature.getActiveWeaponInstance();
				if ((creature instanceof PlayerInstance) && (target instanceof PlayerInstance) && target.isAlikeDead() && target.isFakeDeath())
				{
					target.stopFakeDeath(null);
				}
				else if (target.isAlikeDead())
				{
					continue;
				}
				
				final boolean dual = creature.isUsingDualWeapon();
				final boolean shld = Formulas.calcShldUse(creature, target);
				final boolean crit = Formulas.calcCrit(creature.getCriticalHit(target, skill));
				final boolean soul = ((weapon != null) && (weapon.getChargedSoulshot() == ItemInstance.CHARGED_SOULSHOT) && (weapon.getItemType() != WeaponType.DAGGER));
				if (!crit && ((skill.getCondition() & Skill.COND_CRIT) != 0))
				{
					damage = 0;
				}
				else
				{
					damage = (int) Formulas.calcPhysDam(creature, target, skill, shld, crit, dual, soul);
				}
				
				if (damage > 0)
				{
					target.reduceCurrentHp(damage, creature);
					if (soul && (weapon != null))
					{
						weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);
					}
					
					creature.sendDamageMessage(target, damage, false, false, false);
				}
				else
				{
					creature.sendPacket(SystemMessage.sendString(skill.getName() + " failed."));
				}
			}
		}
		catch (Exception e)
		{
			player.sendMessage("Error using siege assault:" + e);
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
	 * @param isCheckOnly if false, it will send a notification to the player telling him why it failed
	 * @return
	 */
	public static boolean checkIfOkToUseStriderSiegeAssault(Creature creature, boolean isCheckOnly)
	{
		final Castle castle = CastleManager.getInstance().getCastle(creature);
		final Fort fort = FortManager.getInstance().getFort(creature);
		if ((castle == null) && (fort == null))
		{
			return false;
		}
		
		if (castle != null)
		{
			return checkIfOkToUseStriderSiegeAssault(creature, castle, isCheckOnly);
		}
		return checkIfOkToUseStriderSiegeAssault(creature, fort, isCheckOnly);
	}
	
	public static boolean checkIfOkToUseStriderSiegeAssault(Creature creature, Castle castle, boolean isCheckOnly)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return false;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		String message = "";
		if ((castle == null) || (castle.getCastleId() <= 0))
		{
			message = "You must be on castle ground to use strider siege assault";
		}
		else if (!castle.getSiege().isInProgress())
		{
			message = "You can only use strider siege assault during a siege.";
		}
		else if (!(player.getTarget() instanceof DoorInstance))
		{
			message = "You can only use strider siege assault on doors and walls.";
		}
		else if (!creature.isRiding())
		{
			message = "You can only use strider siege assault when on strider.";
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly && !message.isEmpty())
		{
			player.sendMessage(message);
		}
		
		return false;
	}
	
	public static boolean checkIfOkToUseStriderSiegeAssault(Creature creature, Fort fort, boolean isCheckOnly)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return false;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		String message = "";
		if ((fort == null) || (fort.getFortId() <= 0))
		{
			message = "You must be on fort ground to use strider siege assault.";
		}
		else if (!fort.getSiege().isInProgress())
		{
			message = "You can only use strider siege assault during a siege.";
		}
		else if (!(player.getTarget() instanceof DoorInstance))
		{
			message = "You can only use strider siege assault on doors and walls.";
		}
		else if (!creature.isRiding())
		{
			message = "You can only use strider siege assault when on strider.";
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly && !message.isEmpty())
		{
			player.sendMessage(message);
		}
		
		return false;
	}
}
