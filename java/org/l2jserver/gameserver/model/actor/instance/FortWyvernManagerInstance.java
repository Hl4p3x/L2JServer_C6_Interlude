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
package org.l2jserver.gameserver.model.actor.instance;

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.Ride;

/**
 * @author Scoria, Qwerty
 */

public class FortWyvernManagerInstance extends NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public FortWyvernManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.startsWith("RideWyvern"))
		{
			if (!player.isClanLeader())
			{
				player.sendMessage("Only clan leaders are allowed.");
				return;
			}
			
			int petItemId = 0;
			ItemInstance petItem = null;
			if (player.getPet() == null)
			{
				if (player.isMounted())
				{
					petItem = player.getInventory().getItemByObjectId(player.getMountObjectID());
					if (petItem != null)
					{
						petItemId = petItem.getItemId();
					}
				}
			}
			else
			{
				petItemId = player.getPet().getControlItemId();
			}
			
			if ((petItemId == 0) || !player.isMounted())
			{
				player.sendMessage("Ride your strider first...");
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				return;
			}
			else if (player.isMounted() && (petItem != null) && (petItem.getEnchantLevel() < 55))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				return;
			}
			
			// Wyvern requires Config.MANAGER_CRYSTAL_COUNT crystal for ride...
			if ((player.getInventory().getItemByItemId(1460) != null) && (player.getInventory().getItemByItemId(1460).getCount() >= 10))
			{
				if (!player.disarmWeapons())
				{
					return;
				}
				
				if (player.isMounted())
				{
					player.dismount();
				}
				
				if (player.getPet() != null)
				{
					player.getPet().unSummon(player);
				}
				
				player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
				
				final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
				player.sendPacket(mount);
				player.broadcastPacket(mount);
				player.setMountType(mount.getMountType());
				
				player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
				player.sendMessage("The Wyvern has been summoned successfully!");
			}
			else
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				player.sendMessage("You need 10 Crystals: B Grade.");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the PlayerInstance already target the NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the PlayerInstance player
			player.setTarget(this);
		}
		else if (!canInteract(player)) // Calculate the distance between the PlayerInstance and the NpcInstance
		{
			// Notify the PlayerInstance AI with AI_INTENTION_INTERACT
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showMessageWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showMessageWindow(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/fortress/wyvernmanager-no.htm";
		
		final int condition = validateCondition(player);
		if ((condition > COND_ALL_FALSE) && (condition == COND_OWNER))
		{
			filename = "data/html/fortress/wyvernmanager.htm";
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%count%", String.valueOf(10));
		player.sendPacket(html);
	}
	
	protected int validateCondition(PlayerInstance player)
	{
		if ((getFort() != null) && (getFort().getFortId() > 0) && (player.getClan() != null))
		{
			if (getFort().getSiege().isInProgress())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			else if ((getFort().getOwnerId() == player.getClanId()) && player.isClanLeader())
			{
				return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
