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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.l2jserver.gameserver.TradeController;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.CastleManorManager;
import org.l2jserver.gameserver.model.PlayerInventory;
import org.l2jserver.gameserver.model.StoreTradeList;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.BuyList;
import org.l2jserver.gameserver.network.serverpackets.ExShowCropInfo;
import org.l2jserver.gameserver.network.serverpackets.ExShowCropSetting;
import org.l2jserver.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import org.l2jserver.gameserver.network.serverpackets.ExShowSeedInfo;
import org.l2jserver.gameserver.network.serverpackets.ExShowSeedSetting;
import org.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.ValidateLocation;
import org.l2jserver.gameserver.util.Util;

/**
 * Castle Chamberlains implementation used for: - tax rate control - regional manor system control - castle treasure control - ...
 */
public class CastleChamberlainInstance extends FolkInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public CastleChamberlainInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		player.setLastFolkNPC(this);
		
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
		// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC().getObjectId() != getObjectId())
		{
			return;
		}
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		final int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
		{
			return;
		}
		
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			return;
		}
		else if (condition == COND_OWNER)
		{
			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}
			
			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_DISMISS) == Clan.CP_CS_DISMISS)
				{
					getCastle().banishForeigners(); // Move non-clan members off castle area
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list_siege_clans"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_MANAGE_SIEGE) == Clan.CP_CS_MANAGE_SIEGE)
				{
					getCastle().getSiege().listRegisterClan(player); // List current register clan
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("receive_report"))
			{
				if (player.isClanLeader())
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-report.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					final Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
					html.replace("%clanname%", clan.getName());
					html.replace("%clanleadername%", clan.getLeaderName());
					html.replace("%castlename%", getCastle().getName());
					{
						final int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
						switch (currentPeriod)
						{
							case SevenSigns.PERIOD_COMP_RECRUITING:
							{
								html.replace("%ss_event%", "Quest Event Initialization");
								break;
							}
							case SevenSigns.PERIOD_COMPETITION:
							{
								html.replace("%ss_event%", "Competition (Quest Event)");
								break;
							}
							case SevenSigns.PERIOD_COMP_RESULTS:
							{
								html.replace("%ss_event%", "Quest Event Results");
								break;
							}
							case SevenSigns.PERIOD_SEAL_VALIDATION:
							{
								html.replace("%ss_event%", "Seal Validation");
								break;
							}
						}
					}
					{
						final int sealOwner1 = SevenSigns.getInstance().getSealOwner(1);
						switch (sealOwner1)
						{
							case SevenSigns.CABAL_NULL:
							{
								html.replace("%ss_avarice%", "Not in Possession");
								break;
							}
							case SevenSigns.CABAL_DAWN:
							{
								html.replace("%ss_avarice%", "Lords of Dawn");
								break;
							}
							case SevenSigns.CABAL_DUSK:
							{
								html.replace("%ss_avarice%", "Revolutionaries of Dusk");
								break;
							}
						}
					}
					{
						final int sealOwner2 = SevenSigns.getInstance().getSealOwner(2);
						switch (sealOwner2)
						{
							case SevenSigns.CABAL_NULL:
							{
								html.replace("%ss_gnosis%", "Not in Possession");
								break;
							}
							case SevenSigns.CABAL_DAWN:
							{
								html.replace("%ss_gnosis%", "Lords of Dawn");
								break;
							}
							case SevenSigns.CABAL_DUSK:
							{
								html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
								break;
							}
						}
					}
					{
						final int sealOwner3 = SevenSigns.getInstance().getSealOwner(3);
						switch (sealOwner3)
						{
							case SevenSigns.CABAL_NULL:
							{
								html.replace("%ss_strife%", "Not in Possession");
								break;
							}
							case SevenSigns.CABAL_DAWN:
							{
								html.replace("%ss_strife%", "Lords of Dawn");
								break;
							}
							case SevenSigns.CABAL_DUSK:
							{
								html.replace("%ss_strife%", "Revolutionaries of Dusk");
								break;
							}
						}
					}
					player.sendPacket(html);
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("items"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_USE_FUNCTIONS) == Clan.CP_CS_USE_FUNCTIONS)
				{
					if (val.equals(""))
					{
						return;
					}
					
					player.tempInvetoryDisable();
					
					int buy;
					{
						final int castleId = getCastle().getCastleId();
						final int circlet = CastleManager.getInstance().getCircletByCastleId(castleId);
						final PlayerInventory s = player.getInventory();
						if (s.getItemByItemId(circlet) == null)
						{
							buy = Integer.parseInt(val + "1");
						}
						else
						{
							buy = Integer.parseInt(val + "2");
						}
					}
					final StoreTradeList list = TradeController.getInstance().getBuyList(buy);
					if ((list != null) && list.getNpcId().equals(String.valueOf(getNpcId())))
					{
						player.sendPacket(new BuyList(list, player.getAdena(), 0));
					}
					else
					{
						LOGGER.warning("player: " + player.getName() + " attempting to buy from chamberlain that don't have buylist!");
						LOGGER.warning("buylist id:" + buy);
					}
					
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
			}
			else if (actualCommand.equalsIgnoreCase("manage_vault"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_TAXES) == Clan.CP_CS_TAXES)
				{
					String filename = "data/html/chamberlain/chamberlain-vault.htm";
					int amount = 0;
					if (val.equalsIgnoreCase("deposit"))
					{
						try
						{
							amount = Integer.parseInt(st.nextToken());
						}
						catch (NoSuchElementException e)
						{
						}
						if ((amount > 0) && (((long) getCastle().getTreasury() + amount) < Integer.MAX_VALUE))
						{
							if (player.reduceAdena("Castle", amount, this, true))
							{
								getCastle().addToTreasuryNoTax(amount);
							}
							else
							{
								sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
							}
						}
					}
					else if (val.equalsIgnoreCase("withdraw"))
					{
						try
						{
							amount = Integer.parseInt(st.nextToken());
						}
						catch (NoSuchElementException e)
						{
						}
						if (amount > 0)
						{
							if (getCastle().getTreasury() < amount)
							{
								filename = "data/html/chamberlain/chamberlain-vault-no.htm";
							}
							else if (getCastle().addToTreasuryNoTax(-1 * amount))
							{
								player.addAdena("Castle", amount, this, true);
							}
						}
					}
					
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					html.replace("%tax_income%", Util.formatAdena(getCastle().getTreasury()));
					html.replace("%withdraw_amount%", Util.formatAdena(amount));
					player.sendPacket(html);
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manor"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_MANOR_ADMIN) == Clan.CP_CS_MANOR_ADMIN)
				{
					String filename = "";
					if (CastleManorManager.getInstance().isDisabled())
					{
						filename = "data/html/npcdefault.htm";
					}
					else
					{
						final int cmd = Integer.parseInt(val);
						switch (cmd)
						{
							case 0:
							{
								filename = "data/html/chamberlain/manor/manor.htm";
								break;
							}
							// TODO: correct in html's to 1
							case 4:
							{
								filename = "data/html/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
								break;
							}
							default:
							{
								filename = "data/html/chamberlain/chamberlain-no.htm";
								break;
							}
						}
					}
					if (filename.length() != 0)
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile(filename);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%npcname%", getName());
						player.sendPacket(html);
					}
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
				return;
			}
			else if (command.startsWith("manor_menu_select"))
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_MANOR_ADMIN) == Clan.CP_CS_MANOR_ADMIN)
				{
					if (CastleManorManager.getInstance().isUnderMaintenance())
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
						player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
						return;
					}
					
					final String params = command.substring(command.indexOf('?') + 1);
					final StringTokenizer str = new StringTokenizer(params, "&");
					final int ask = Integer.parseInt(str.nextToken().split("=")[1]);
					final int state = Integer.parseInt(str.nextToken().split("=")[1]);
					final int time = Integer.parseInt(str.nextToken().split("=")[1]);
					int castleId;
					if (state == -1)
					{
						castleId = getCastle().getCastleId();
					}
					else
					{
						// info for requested manor
						castleId = state;
					}
					
					switch (ask)
					{
						case 3: // Current seeds (Manor info)
						{
							if ((time == 1) && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
							{
								player.sendPacket(new ExShowSeedInfo(castleId, null));
							}
							else
							{
								player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
							}
							break;
						}
						case 4: // Current crops (Manor info)
						{
							if ((time == 1) && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
							{
								player.sendPacket(new ExShowCropInfo(castleId, null));
							}
							else
							{
								player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
							}
							break;
						}
						case 5: // Basic info (Manor info)
						{
							player.sendPacket(new ExShowManorDefaultInfo());
							break;
						}
						case 7: // Edit seed setup
						{
							if (getCastle().isNextPeriodApproved())
							{
								player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_A_M_AND_8_P_M);
							}
							else
							{
								player.sendPacket(new ExShowSeedSetting(getCastle().getCastleId()));
							}
							break;
						}
						case 8: // Edit crop setup
						{
							if (getCastle().isNextPeriodApproved())
							{
								player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_A_M_AND_8_P_M);
							}
							else
							{
								player.sendPacket(new ExShowCropSetting(getCastle().getCastleId()));
							}
							break;
						}
					}
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
			}
			else if (actualCommand.equalsIgnoreCase("operate_door")) // door control
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_OPEN_DOOR) == Clan.CP_CS_OPEN_DOOR)
				{
					if (!val.isEmpty())
					{
						final boolean open = Integer.parseInt(val) == 1;
						while (st.hasMoreTokens())
						{
							getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
						}
					}
					
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/" + getTemplate().getNpcId() + "-d.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-noprivs.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("tax_set")) // tax rates control
			{
				if ((player.getClanPrivileges() & Clan.CP_CS_TAXES) == Clan.CP_CS_TAXES)
				{
					if (!val.isEmpty())
					{
						getCastle().setTaxPercent(player, Integer.parseInt(val));
					}
					
					final StringBuilder msg = new StringBuilder("<html><body>");
					msg.append(getName() + ":<br>");
					msg.append("Current tax rate: " + getCastle().getTaxPercent() + "%<br>");
					msg.append("<table>");
					msg.append("<tr>");
					msg.append("<td>Change tax rate to:</td>");
					msg.append("<td><edit var=\"value\" width=40><br>");
					msg.append("<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15></td>");
					msg.append("</tr>");
					msg.append("</table>");
					msg.append("</center>");
					msg.append("</body></html>");
					
					sendHtmlMessage(player, msg.toString());
				}
				else
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/chamberlain/chamberlain-tax.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%tax%", String.valueOf(getCastle().getTaxPercent()));
					player.sendPacket(html);
				}
				return;
			}
		}
		super.onBypassFeedback(player, command);
	}
	
	private void sendHtmlMessage(PlayerInstance player, String htmlMessage)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setHtml(htmlMessage);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void showMessageWindow(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/chamberlain/chamberlain-no.htm";
		
		final int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/chamberlain/chamberlain-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER)
			{
				filename = "data/html/chamberlain/chamberlain.htm"; // Owner message window
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	protected int validateCondition(PlayerInstance player)
	{
		if ((getCastle() != null) && (getCastle().getCastleId() > 0) && (player.getClan() != null))
		{
			if (getCastle().getSiege().isInProgress())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			else if (getCastle().getOwnerId() == player.getClanId())
			{
				return COND_OWNER; // Owner
			}
		}
		return COND_ALL_FALSE;
	}
}
