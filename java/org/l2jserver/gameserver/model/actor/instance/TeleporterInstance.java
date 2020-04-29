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

import java.util.StringTokenizer;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.sql.TeleportLocationTable;
import org.l2jserver.gameserver.datatables.xml.ZoneData;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.instancemanager.SiegeManager;
import org.l2jserver.gameserver.model.TeleportLocation;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.model.zone.type.TownZone;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * The Class TeleporterInstance.
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class TeleporterInstance extends FolkInstance
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;
	
	/**
	 * Instantiates a new teleporter instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public TeleporterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (Olympiad.getInstance().isRegisteredInComp(player))
		{
			player.sendMessage("You are not allowed to use a teleport while registered in olympiad game.");
			return;
		}
		
		if (player.isAio() && !Config.ALLOW_AIO_USE_GK)
		{
			player.sendMessage("Aio Buffers Can't Use Teleports");
			return;
		}
		
		final int condition = validateCondition(player);
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		if (actualCommand.equalsIgnoreCase("goto"))
		{
			final int npcId = getTemplate().getNpcId();
			
			switch (npcId)
			{
				case 31095: //
				case 31096: //
				case 31097: //
				case 31098: // Enter Necropolises
				case 31099: //
				case 31100: //
				case 31101: //
				case 31102: //
				case 31114: //
				case 31115: //
				case 31116: // Enter Catacombs
				case 31117: //
				case 31118: //
				case 31119: //
				{
					player.setIn7sDungeon(true);
					break;
				}
				case 31103: //
				case 31104: //
				case 31105: //
				case 31106: // Exit Necropolises
				case 31107: //
				case 31108: //
				case 31109: //
				case 31110: //
				case 31120: //
				case 31121: //
				case 31122: // Exit Catacombs
				case 31123: //
				case 31124: //
				case 31125: //
				{
					player.setIn7sDungeon(false);
					break;
				}
			}
			
			if (st.countTokens() <= 0)
			{
				return;
			}
			
			final int whereTo = Integer.parseInt(st.nextToken());
			if (condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if (condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0; // NOTE: Replace 0 with highest level when privilege level is implemented
				if (st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}
				
				if (10 >= minPrivilegeLevel)
				{
					doTeleport(player, whereTo);
				}
				else
				{
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				}
				
				return;
			}
		}
		
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int value)
	{
		String pom = "";
		if (value == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + value;
		}
		return "data/html/teleporter/" + pom + ".htm";
	}
	
	@Override
	public void showChatWindow(PlayerInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		
		final int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/teleporter/castleteleporter-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER)
			{
				filename = getHtmlPath(getNpcId(), 0); // Owner message window
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	/**
	 * Do teleport.
	 * @param player the player
	 * @param value the value
	 */
	private void doTeleport(PlayerInstance player, int value)
	{
		final TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(value);
		if (list != null)
		{
			// you cannot teleport to village that is in siege
			if (!SiegeManager.getInstance().isTeleportToSiegeAllowed() && (SiegeManager.getInstance().getSiege(list.getX(), list.getY(), list.getZ()) != null) && !player.isNoble())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
			else if (!SiegeManager.getInstance().isTeleportToSiegeTownAllowed() && (ZoneData.getInstance().getZone(list.getX(), list.getY(), list.getZ(), TownZone.class) != null) && CastleManager.getInstance().findNearestCastle(list.getX(), list.getY()).getSiege().isInProgress() && !player.isNoble())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
			else if (!player.isGM() && !Config.FLAGED_PLAYER_CAN_USE_GK && (player.getPvpFlag() > 0))
			{
				player.sendMessage("Don't run from PvP! You will be able to use the teleporter only after your flag is gone.");
				return;
			}
			else if (player.isAio() && !Config.ALLOW_AIO_USE_GK)
			{
				player.sendMessage("Aio Buffers are not allowed to use GateKeepers.");
				return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (player.getKarma() > 0)) // karma
			{
				player.sendMessage("Go away, you're not welcome here.");
				return;
			}
			else if (list.isForNoble() && !player.isNoble())
			{
				final String filename = "data/html/teleporter/nobleteleporter-no.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			else if (player.isAlikeDead())
			{
				player.sendMessage("You can't use teleport when you are dead.");
				return;
			}
			else if (player.isSitting())
			{
				player.sendMessage("You can't use teleport when you are sitting.");
				return;
			}
			else if ((list.getTeleId() == 9982) && (list.getTeleId() == 9983) && (list.getTeleId() == 9984) && (getNpcId() == 30483) && (player.getLevel() >= Config.CRUMA_TOWER_LEVEL_RESTRICT))
			{
				// Chars level XX can't enter in Cruma Tower. Retail: level 56 and above
				final int maxlvl = Config.CRUMA_TOWER_LEVEL_RESTRICT;
				final String filename = "data/html/teleporter/30483-biglvl.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%allowedmaxlvl%", "" + maxlvl + "");
				player.sendPacket(html);
				return;
			}
			// Lilith and Anakim have BossZone, so players must be allowed to enter
			else if (list.getTeleId() == 450)
			{
				final BossZone zone = GrandBossManager.getInstance().getZone(list.getX(), list.getY(), list.getZ());
				zone.allowPlayerEntry(player, 300);
				player.teleToLocation(list.getX(), list.getY(), list.getZ(), true);
			}
			else if (!list.isForNoble() && (Config.ALT_GAME_FREE_TELEPORT || player.reduceAdena("Teleport", list.getPrice(), this, true)))
			{
				player.teleToLocation(list.getX(), list.getY(), list.getZ(), true);
			}
			else if (list.isForNoble() && (Config.ALT_GAME_FREE_TELEPORT || player.destroyItemByItemId("Noble Teleport", 6651, list.getPrice(), this, true)))
			{
				player.teleToLocation(list.getX(), list.getY(), list.getZ(), true);
			}
		}
		else
		{
			LOGGER.warning("No teleport destination with id:" + value);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Validate condition.
	 * @param player the player
	 * @return the int
	 */
	private int validateCondition(PlayerInstance player)
	{
		if (CastleManager.getInstance().getCastleIndex(this) < 0)
		{
			return COND_REGULAR; // Regular access
		}
		else if (getCastle().getSiege().isInProgress())
		{
			return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
		}
		else if (player.getClan() != null) // Teleporter is on castle ground and player is in a clan
		{
			if (getCastle().getOwnerId() == player.getClanId())
			{
				return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
