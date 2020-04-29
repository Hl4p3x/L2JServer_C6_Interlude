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

import org.l2jserver.gameserver.model.BlockList;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.AskJoinParty;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class RequestJoinParty extends GameClientPacket
{
	private String _name;
	private int _itemDistribution;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance requestor = getClient().getPlayer();
		final PlayerInstance target = World.getInstance().getPlayer(_name);
		if (requestor == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getPartyInvitation().tryPerformAction("PartyInvitation"))
		{
			requestor.sendMessage("You Cannot Invite into Party So Fast!");
			return;
		}
		
		if (target == null)
		{
			requestor.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}
		
		if ((requestor._inEventDM && (DM.isTeleport() || DM.hasStarted())) || (target._inEventDM && (DM.isTeleport() || DM.hasStarted())))
		{
			requestor.sendMessage("You can't invite that player in party!");
			return;
		}
		
		if ((requestor._inEventTvT && !target._inEventTvT && (TvT.isStarted() || TvT.isTeleport())) || (!requestor._inEventTvT && target._inEventTvT && (TvT.isStarted() || TvT.isTeleport())) || (requestor._inEventCTF && !target._inEventCTF && (CTF.isStarted() || CTF.isTeleport())) || (!requestor._inEventCTF && target._inEventCTF && (CTF.isStarted() || CTF.isTeleport())))
		{
			requestor.sendMessage("You can't invite that player in party: you or your target are in Event");
			return;
		}
		
		if (target.isInParty())
		{
			final SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}
		
		if (target == requestor)
		{
			requestor.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (target.isCursedWeaponEquiped() || requestor.isCursedWeaponEquiped())
		{
			requestor.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (target.isGM() && target.getAppearance().isInvisible())
		{
			requestor.sendMessage("You can't invite GM in invisible mode.");
			return;
		}
		
		if (target.isInJail() || requestor.isInJail())
		{
			final SystemMessage sm = SystemMessage.sendString("Player is in Jail.");
			requestor.sendPacket(sm);
			return;
		}
		
		if (BlockList.isBlocked(target, requestor))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST).addString(target.getName()));
			return;
		}
		
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
		{
			return;
		}
		
		if (target.isInDuel() || requestor.isInDuel())
		{
			return;
		}
		
		if (!requestor.isInParty()) // Asker has no party
		{
			createNewParty(target, requestor);
		}
		else if (requestor.getParty().isInDimensionalRift())
		{
			requestor.sendMessage("You can't invite a player when in Dimensional Rift.");
		}
		else
		{
			addTargetToParty(target, requestor);
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(PlayerInstance target, PlayerInstance requestor)
	{
		SystemMessage msg;
		
		// summary of ppl already in party and ppl that get invitation
		if (requestor.getParty().getMemberCount() >= 9)
		{
			requestor.sendPacket(SystemMessageId.THE_PARTY_IS_FULL);
			return;
		}
		
		if (!requestor.getParty().isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
			return;
		}
		
		if (requestor.getParty().getPendingInvitation() && !requestor.getParty().isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().setPendingInvitation(true);
			
			msg = new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_S1_TO_YOUR_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(PlayerInstance target, PlayerInstance requestor)
	{
		SystemMessage msg;
		if (!target.isProcessingRequest())
		{
			requestor.setParty(new Party(requestor, _itemDistribution));
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().setPendingInvitation(true);
			
			msg = new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_S1_TO_YOUR_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
	}
}