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

import java.util.logging.Logger;

import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;

@SuppressWarnings("unused")
public class Action extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(Action.class.getName());
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _actionId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current PlayerInstance of the player
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.inObserverMode())
		{
			player.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final WorldObject obj;
		if (player.getTargetId() == _objectId)
		{
			obj = player.getTarget();
		}
		else
		{
			obj = World.getInstance().findObject(_objectId);
		}
		
		// If object requested does not exist
		// pressing e.g. pickup many times quickly would get you here
		if (obj == null)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't interact with objects in the other instances except from multiverse
		if ((obj.getInstanceId() != player.getInstanceId()) && (player.getInstanceId() != -1))
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Only GMs can directly interact with invisible characters
		if ((obj instanceof PlayerInstance) && (((PlayerInstance) obj).getAppearance().isInvisible()) && !player.isGM())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if (player.getPrivateStoreType() == 0/* && activeChar.getActiveRequester() == null */)
		{
			switch (_actionId)
			{
				case 0:
				{
					obj.onAction(player);
					break;
				}
				case 1:
				{
					if ((obj instanceof Creature) && ((Creature) obj).isAlikeDead())
					{
						obj.onAction(player);
					}
					else
					{
						obj.onActionShift(getClient());
					}
					break;
				}
				default:
				{
					// Invalid action detected (probably client cheating), LOGGER this
					LOGGER.warning("Character: " + player.getName() + " requested invalid action: " + _actionId);
					getClient().sendPacket(ActionFailed.STATIC_PACKET);
					break;
				}
			}
		}
		else
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET); // Actions prohibited when in trade
		}
	}
}