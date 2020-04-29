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
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.Ride;
import org.l2jserver.gameserver.network.serverpackets.ValidateLocation;

public class WyvernManagerInstance extends CastleChamberlainInstance
{
	protected static final int COND_CLAN_OWNER = 3;
	private int _clanHallId = -1;
	
	/**
	 * Instantiates a new wyvern manager instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public WyvernManagerInstance(int objectId, NpcTemplate template)
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
			if (player.getPet() == null)
			{
				if (player.isMounted())
				{
					player.sendMessage("You already have a pet or are mounted.");
				}
				else
				{
					player.sendMessage("Summon your strider first.");
				}
			}
			else if ((player.getPet().getNpcId() == 12526) || (player.getPet().getNpcId() == 12527) || (player.getPet().getNpcId() == 12528))
			{
				if ((player.getInventory().getItemByItemId(1460) != null) && (player.getInventory().getItemByItemId(1460).getCount() >= 10))
				{
					if (player.getPet().getLevel() < 55)
					{
						player.sendMessage("Your strider has not reached the required level.");
					}
					else
					{
						if (!player.disarmWeapons())
						{
							return;
						}
						player.getPet().unSummon(player);
						player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
						final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
						player.sendPacket(mount);
						player.broadcastPacket(mount);
						player.setMountType(mount.getMountType());
						player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
						player.sendMessage("The wyvern has been summoned successfully!");
					}
				}
				else
				{
					player.sendMessage("You need 10 Crystals: B Grade.");
				}
			}
			else
			{
				player.sendMessage("Unsummon your pet.");
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
			
			// Send a Server->Client packet MyTargetSelected to the PlayerInstance player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
			// Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
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
	
	/**
	 * Show message window.
	 * @param player the player
	 */
	private void showMessageWindow(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
		if (getClanHall() != null)
		{
			filename = "data/html/wyvernmanager/wyvernmanager-clan-no.htm";
		}
		final int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_OWNER)
			{
				filename = "data/html/wyvernmanager/wyvernmanager.htm"; // Owner message window
			}
			else if (condition == COND_CLAN_OWNER)
			{
				filename = "data/html/wyvernmanager/wyvernmanager-clan.htm";
			}
		}
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	/**
	 * Return the PledgeHall this NpcInstance belongs to.
	 * @return the clan hall
	 */
	public ClanHall getClanHall()
	{
		if (_clanHallId < 0)
		{
			final ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
			if (temp != null)
			{
				_clanHallId = temp.getId();
			}
			
			if (_clanHallId < 0)
			{
				return null;
			}
		}
		return ClanHallManager.getInstance().getClanHallById(_clanHallId);
	}
	
	@Override
	protected int validateCondition(PlayerInstance player)
	{
		if ((getClanHall() != null) && (player.getClan() != null))
		{
			if ((getClanHall().getOwnerId() == player.getClanId()) && player.isClanLeader())
			{
				return COND_CLAN_OWNER; // Owner of the clanhall
			}
		}
		else if ((super.getCastle() != null) && (super.getCastle().getCastleId() > 0) && (player.getClan() != null))
		{
			// Checks if player is in Sieage Zone, he can't use wyvern!!
			if (super.isInsideZone(ZoneId.SIEGE) || super.getCastle().getSiege().isInProgress())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			else if ((super.getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				&& player.isClanLeader())
			{
				return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
