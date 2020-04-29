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
package org.l2jserver.gameserver.network.clientpackets;

import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;

@SuppressWarnings("unused")
public class AttackRequest extends GameClientPacket
{
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _attackId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC(); // 0 for simple click - 1 for shift-click
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((System.currentTimeMillis() - player.getLastAttackPacket()) < 500)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		player.setLastAttackPacket();
		
		// avoid using expensive operations if not needed
		final WorldObject target;
		if (player.getTargetId() == _objectId)
		{
			target = player.getTarget();
		}
		else
		{
			target = World.getInstance().findObject(_objectId);
		}
		
		if (target == null)
		{
			return;
		}
		
		// Like L2OFF
		if (player.isAttackingNow() && player.isMoving())
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't attack objects in the other instances except from multiverse
		if ((target.getInstanceId() != player.getInstanceId()) && (player.getInstanceId() != -1))
		{
			return;
		}
		
		// Only GMs can directly attack invisible characters
		if ((target instanceof PlayerInstance) && ((PlayerInstance) target).getAppearance().isInvisible() && !player.isGM())
		{
			return;
		}
		
		// During teleport phase, players cant do any attack
		if ((TvT.isTeleport() && player._inEventTvT) || (CTF.isTeleport() && player._inEventCTF) || (DM.isTeleport() && player._inEventDM))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// No attacks to same team in Event
		if (TvT.isStarted())
		{
			if (target instanceof PlayerInstance)
			{
				if ((player._inEventTvT && ((PlayerInstance) target)._inEventTvT) && player._teamNameTvT.equals(((PlayerInstance) target)._teamNameTvT))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (target instanceof SummonInstance)
			{
				if ((player._inEventTvT && ((SummonInstance) target).getOwner()._inEventTvT) && player._teamNameTvT.equals(((SummonInstance) target).getOwner()._teamNameTvT))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// No attacks to same team in Event
		if (CTF.isStarted())
		{
			if (target instanceof PlayerInstance)
			{
				if ((player._inEventCTF && ((PlayerInstance) target)._inEventCTF) && player._teamNameCTF.equals(((PlayerInstance) target)._teamNameCTF))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (target instanceof SummonInstance)
			{
				if ((player._inEventCTF && ((SummonInstance) target).getOwner()._inEventCTF) && player._teamNameCTF.equals(((SummonInstance) target).getOwner()._teamNameCTF))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (player.getTarget() != target)
		{
			target.onAction(player);
		}
		else if ((target.getObjectId() != player.getObjectId()) && (player.getPrivateStoreType() == 0)
		/* && activeChar.getActiveRequester() ==null */)
		{
			target.onForcedAttack(player);
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}