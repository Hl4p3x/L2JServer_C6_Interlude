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

import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.BoatInstance;
import org.l2jserver.gameserver.model.actor.instance.DoorInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.StaticObjectInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.CharInfo;
import org.l2jserver.gameserver.network.serverpackets.DoorInfo;
import org.l2jserver.gameserver.network.serverpackets.DoorStatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.GetOnVehicle;
import org.l2jserver.gameserver.network.serverpackets.NpcInfo;
import org.l2jserver.gameserver.network.serverpackets.PetInfo;
import org.l2jserver.gameserver.network.serverpackets.PetItemList;
import org.l2jserver.gameserver.network.serverpackets.RelationChanged;
import org.l2jserver.gameserver.network.serverpackets.SpawnItem;
import org.l2jserver.gameserver.network.serverpackets.SpawnItemPoly;
import org.l2jserver.gameserver.network.serverpackets.StaticObject;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;
import org.l2jserver.gameserver.network.serverpackets.VehicleInfo;

public class RequestRecordInfo extends GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		player.getKnownList().updateKnownObjects();
		player.sendPacket(new UserInfo(player));
		for (WorldObject object : player.getKnownList().getKnownObjects().values())
		{
			if (object == null)
			{
				continue;
			}
			
			if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
			{
				player.sendPacket(new SpawnItemPoly(object));
			}
			else
			{
				if (object instanceof ItemInstance)
				{
					player.sendPacket(new SpawnItem((ItemInstance) object));
				}
				else if (object instanceof DoorInstance)
				{
					if (((DoorInstance) object).getCastle() != null)
					{
						player.sendPacket(new DoorInfo((DoorInstance) object, true));
					}
					else
					{
						player.sendPacket(new DoorInfo((DoorInstance) object, false));
					}
					player.sendPacket(new DoorStatusUpdate((DoorInstance) object));
				}
				else if (object instanceof BoatInstance)
				{
					if (!player.isInBoat() && (object != player.getBoat()))
					{
						player.sendPacket(new VehicleInfo((BoatInstance) object));
						((BoatInstance) object).sendVehicleDeparture(player);
					}
				}
				else if (object instanceof StaticObjectInstance)
				{
					player.sendPacket(new StaticObject((StaticObjectInstance) object));
				}
				else if (object instanceof NpcInstance)
				{
					player.sendPacket(new NpcInfo((NpcInstance) object, player));
				}
				else if (object instanceof Summon)
				{
					final Summon summon = (Summon) object;
					
					// Check if the PlayerInstance is the owner of the Pet
					if (player.equals(summon.getOwner()))
					{
						player.sendPacket(new PetInfo(summon));
						if (summon instanceof PetInstance)
						{
							player.sendPacket(new PetItemList((PetInstance) summon));
						}
					}
					else
					{
						player.sendPacket(new NpcInfo(summon, player));
					}
					
					// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
					summon.updateEffectIcons(true);
				}
				else if (object instanceof PlayerInstance)
				{
					final PlayerInstance otherPlayer = (PlayerInstance) object;
					if (otherPlayer.isInBoat())
					{
						otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getLocation());
						player.sendPacket(new CharInfo(otherPlayer));
						final int relation = otherPlayer.getRelation(player);
						if ((otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
						{
							player.sendPacket(new RelationChanged(otherPlayer, relation, player.isAutoAttackable(otherPlayer)));
						}
						player.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getBoatPosition().getX(), otherPlayer.getBoatPosition().getY(), otherPlayer.getBoatPosition().getZ()));
					}
					else
					{
						player.sendPacket(new CharInfo(otherPlayer));
						final int relation = otherPlayer.getRelation(player);
						if ((otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
						{
							player.sendPacket(new RelationChanged(otherPlayer, relation, player.isAutoAttackable(otherPlayer)));
						}
					}
				}
				
				if (object instanceof Creature)
				{
					// Update the state of the Creature object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the PlayerInstance
					final Creature obj = (Creature) object;
					obj.getAI().describeStateToPlayer(player);
				}
			}
		}
	}
}
