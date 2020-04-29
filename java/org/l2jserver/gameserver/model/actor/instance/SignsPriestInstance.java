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
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Dawn/Dusk Seven Signs Priest Instance
 * @author Tempy
 */
public class SignsPriestInstance extends FolkInstance
{
	public SignsPriestInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.startsWith("SevenSignsDesc"))
		{
			final int val = Integer.parseInt(command.substring(15));
			showChatWindow(player, val, null, true);
		}
		else if (command.startsWith("SevenSigns"))
		{
			SystemMessage sm;
			InventoryUpdate iu;
			StatusUpdate su;
			String path;
			int cabal = SevenSigns.CABAL_NULL;
			int stoneType = 0;
			final ItemInstance ancientAdena = player.getInventory().getItemByItemId(SevenSigns.ANCIENT_ADENA_ID);
			final int ancientAdenaAmount = ancientAdena == null ? 0 : ancientAdena.getCount();
			int val = Integer.parseInt(command.substring(11, 12).trim());
			if (command.length() > 12)
			{
				val = Integer.parseInt(command.substring(11, 13).trim());
			}
			
			if (command.length() > 13)
			{
				try
				{
					cabal = Integer.parseInt(command.substring(14, 15).trim());
				}
				catch (Exception e)
				{
					try
					{
						cabal = Integer.parseInt(command.substring(13, 14).trim());
					}
					catch (Exception e2)
					{
						try
						{
							final StringTokenizer st = new StringTokenizer(command.trim());
							st.nextToken();
							cabal = Integer.parseInt(st.nextToken());
						}
						catch (Exception e3)
						{
							LOGGER.warning("Failed to retrieve cabal from bypass command. NpcId: " + getNpcId() + "; Command: " + command);
						}
					}
				}
			}
			
			switch (val)
			{
				case 2: // Purchase Record of the Seven Signs
				{
					if (!player.getInventory().validateCapacity(1))
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM);
						break;
					}
					
					if (!player.reduceAdena("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_COST, this, false))
					{
						final String filename = "data/html/seven_signs/noadena.htm";
						final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile(filename);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
						break;
					}
					player.addItem("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_ID, 1, player, true);
					// Update current load as well
					su = new StatusUpdate(player.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					sendPacket(su);
					break;
				}
				case 3: // Join Cabal Intro 1
				case 8: // Festival of Darkness Intro - SevenSigns x [0]1
				case 10: // Teleport Locations List
				{
					showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
					break;
				}
				case 4: // Join a Cabal - SevenSigns 4 [0]1 x
				{
					final int newSeal = Integer.parseInt(command.substring(15));
					final int oldCabal = SevenSigns.getInstance().getPlayerCabal(player);
					if (oldCabal != SevenSigns.CABAL_NULL)
					{
						player.sendMessage("You are already a member of the " + SevenSigns.getCabalName(cabal) + ".");
						return;
					}
					if (player.getClassId().level() == 0)
					{
						player.sendMessage("You must have already completed your first class transfer.");
						break;
					}
					else if (player.getClassId().level() >= 2)
					{
						if (Config.ALT_GAME_REQUIRE_CASTLE_DAWN)
						{
							if (getPlayerAllyHasCastle(player) && (cabal == SevenSigns.CABAL_DUSK))
							{
								player.sendMessage("You must not be a member of a castle-owning clan to join the Revolutionaries of Dusk.");
								return;
							}
							
							if (!getPlayerAllyHasCastle(player))
							{
								if (cabal == SevenSigns.CABAL_DAWN)
								{
									player.sendMessage("You must be a member of a castle-owning clan to join the Lords Of Dawn.");
									return;
								}
							}
							else if (cabal == SevenSigns.CABAL_DAWN)
							{
								boolean allowJoinDawn = false;
								if (player.destroyItemByItemId("SevenSigns", SevenSigns.CERTIFICATE_OF_APPROVAL_ID, 1, this, false))
								{
									sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
									sm.addNumber(1);
									sm.addItemName(SevenSigns.CERTIFICATE_OF_APPROVAL_ID);
									player.sendPacket(sm);
									allowJoinDawn = true;
								}
								else if (player.reduceAdena("SevenSigns", SevenSigns.ADENA_JOIN_DAWN_COST, this, false))
								{
									sm = new SystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED);
									sm.addNumber(SevenSigns.ADENA_JOIN_DAWN_COST);
									player.sendPacket(sm);
									allowJoinDawn = true;
								}
								if (!allowJoinDawn)
								{
									player.sendMessage("You must be a member of a castle-owning clan, have a Certificate of Lord's Approval, or pay 50000 adena to join the Lords of Dawn.");
									return;
								}
							}
						}
					}
					SevenSigns.getInstance().setPlayerInfo(player, cabal, newSeal);
					// Joined Dawn
					if (cabal == SevenSigns.CABAL_DAWN)
					{
						player.sendPacket(SystemMessageId.YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN);
					}
					else
					{
						player.sendPacket(SystemMessageId.YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK); // Joined Dusk
					}
					// Show a confirmation message to the user, indicating which seal they chose.
					switch (newSeal)
					{
						case SevenSigns.SEAL_AVARICE:
						{
							player.sendPacket(SystemMessageId.YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD);
							break;
						}
						case SevenSigns.SEAL_GNOSIS:
						{
							player.sendPacket(SystemMessageId.YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD);
							break;
						}
						case SevenSigns.SEAL_STRIFE:
						{
							player.sendPacket(SystemMessageId.YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD);
							break;
						}
					}
					
					showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
					break;
				}
				case 6: // Contribute Seal Stones - SevenSigns 6 x
				{
					stoneType = Integer.parseInt(command.substring(13));
					final ItemInstance redStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
					final int redStoneCount = redStones == null ? 0 : redStones.getCount();
					final ItemInstance greenStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
					final int greenStoneCount = greenStones == null ? 0 : greenStones.getCount();
					final ItemInstance blueStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
					final int blueStoneCount = blueStones == null ? 0 : blueStones.getCount();
					int contribScore = SevenSigns.getInstance().getPlayerContribScore(player);
					boolean stonesFound = false;
					if (contribScore == Config.ALT_MAXIMUM_PLAYER_CONTRIB)
					{
						player.sendPacket(SystemMessageId.CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE);
						break;
					}
					int redContribCount = 0;
					int greenContribCount = 0;
					int blueContribCount = 0;
					switch (stoneType)
					{
						case 1:
						{
							blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / SevenSigns.BLUE_CONTRIB_POINTS;
							if (blueContribCount > blueStoneCount)
							{
								blueContribCount = blueStoneCount;
							}
							break;
						}
						case 2:
						{
							greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / SevenSigns.GREEN_CONTRIB_POINTS;
							if (greenContribCount > greenStoneCount)
							{
								greenContribCount = greenStoneCount;
							}
							break;
						}
						case 3:
						{
							redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / SevenSigns.RED_CONTRIB_POINTS;
							if (redContribCount > redStoneCount)
							{
								redContribCount = redStoneCount;
							}
							break;
						}
						case 4:
						{
							int tempContribScore = contribScore;
							redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.RED_CONTRIB_POINTS;
							if (redContribCount > redStoneCount)
							{
								redContribCount = redStoneCount;
							}
							tempContribScore += redContribCount * SevenSigns.RED_CONTRIB_POINTS;
							greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.GREEN_CONTRIB_POINTS;
							if (greenContribCount > greenStoneCount)
							{
								greenContribCount = greenStoneCount;
							}
							tempContribScore += greenContribCount * SevenSigns.GREEN_CONTRIB_POINTS;
							blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.BLUE_CONTRIB_POINTS;
							if (blueContribCount > blueStoneCount)
							{
								blueContribCount = blueStoneCount;
							}
							break;
						}
					}
					if ((redContribCount > 0) && player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_RED_ID, redContribCount, this, false))
					{
						stonesFound = true;
					}
					if ((greenContribCount > 0) && player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_GREEN_ID, greenContribCount, this, false))
					{
						stonesFound = true;
					}
					if ((blueContribCount > 0) && player.destroyItemByItemId("SevenSigns", SevenSigns.SEAL_STONE_BLUE_ID, blueContribCount, this, false))
					{
						stonesFound = true;
					}
					
					if (!stonesFound)
					{
						player.sendMessage("You do not have any seal stones of that type.");
						break;
					}
					
					contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player, blueContribCount, greenContribCount, redContribCount);
					sm = new SystemMessage(SystemMessageId.YOUR_CONTRIBUTION_SCORE_IS_INCREASED_BY_S1);
					sm.addNumber(contribScore);
					player.sendPacket(sm);
					
					showChatWindow(player, 6, null, false);
					break;
				}
				case 7: // Exchange Ancient Adena for Adena - SevenSigns 7 xxxxxxx
				{
					int ancientAdenaConvert = 0;
					try
					{
						ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
					}
					catch (NumberFormatException e)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					catch (StringIndexOutOfBoundsException e)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					if (ancientAdenaConvert < 1)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm");
						break;
					}
					if (ancientAdenaAmount < ancientAdenaConvert)
					{
						showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_4.htm");
						break;
					}
					player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true);
					player.addAdena("SevenSigns", ancientAdenaConvert, this, true);
					iu = new InventoryUpdate();
					iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
					iu.addModifiedItem(player.getInventory().getAdenaInstance());
					player.sendPacket(iu);
					break;
				}
				case 9: // Receive Contribution Rewards
				{
					final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
					final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
					if (SevenSigns.getInstance().isSealValidationPeriod() && (playerCabal == winningCabal))
					{
						final int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player, true);
						if (ancientAdenaReward < 3)
						{
							showChatWindow(player, 9, "b", false);
							break;
						}
						player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
						// Send inventory update packet
						iu = new InventoryUpdate();
						iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
						sendPacket(iu);
						// Update current load as well
						su = new StatusUpdate(player.getObjectId());
						su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
						sendPacket(su);
						showChatWindow(player, 9, "a", false);
					}
					break;
				}
				case 11: // Teleport to Hunting Grounds
				{
					try
					{
						final String portInfo = command.substring(14).trim();
						final StringTokenizer st = new StringTokenizer(portInfo);
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int ancientAdenaCost = Integer.parseInt(st.nextToken());
						if ((ancientAdenaCost > 0) && !player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true))
						{
							break;
						}
						player.teleToLocation(x, y, z, true);
					}
					catch (Exception e)
					{
						LOGGER.warning("SevenSigns: Error occurred while teleporting player: " + e);
					}
					break;
				}
				case 17: // Exchange Seal Stones for Ancient Adena (Type Choice) - SevenSigns 17 x
				{
					stoneType = Integer.parseInt(command.substring(14));
					int stoneId = 0;
					int stoneCount = 0;
					int stoneValue = 0;
					String stoneColor = null;
					String content;
					switch (stoneType)
					{
						case 1:
						{
							stoneColor = "blue";
							stoneId = SevenSigns.SEAL_STONE_BLUE_ID;
							stoneValue = SevenSigns.SEAL_STONE_BLUE_VALUE;
							break;
						}
						case 2:
						{
							stoneColor = "green";
							stoneId = SevenSigns.SEAL_STONE_GREEN_ID;
							stoneValue = SevenSigns.SEAL_STONE_GREEN_VALUE;
							break;
						}
						case 3:
						{
							stoneColor = "red";
							stoneId = SevenSigns.SEAL_STONE_RED_ID;
							stoneValue = SevenSigns.SEAL_STONE_RED_VALUE;
							break;
						}
					}
					
					final ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
					if (stoneInstance != null)
					{
						stoneCount = stoneInstance.getCount();
					}
					
					path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm";
					content = HtmCache.getInstance().getHtm(path);
					if (content != null)
					{
						content = content.replace("%stoneColor%", stoneColor);
						content = content.replace("%stoneValue%", String.valueOf(stoneValue));
						content = content.replace("%stoneCount%", String.valueOf(stoneCount));
						content = content.replace("%stoneItemId%", String.valueOf(stoneId));
						content = content.replace("%objectId%", String.valueOf(getObjectId()));
						
						final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setHtml(content);
						player.sendPacket(html);
					}
					else
					{
						LOGGER.warning("Problem with HTML text " + SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm: " + path);
					}
					break;
				}
				case 18: // Exchange Seal Stones for Ancient Adena - SevenSigns 18 xxxx xxxxxx
				{
					final int convertStoneId = Integer.parseInt(command.substring(14, 18));
					int convertCount = 0;
					try
					{
						convertCount = Integer.parseInt(command.substring(19).trim());
					}
					catch (Exception NumberFormatException)
					{
						player.sendMessage("You must enter an integer amount.");
						break;
					}
					final ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
					if (convertItem == null)
					{
						player.sendMessage("You do not have any seal stones of that type.");
						break;
					}
					final int totalCount = convertItem.getCount();
					int ancientAdenaReward = 0;
					if ((convertCount <= totalCount) && (convertCount > 0))
					{
						switch (convertStoneId)
						{
							case SevenSigns.SEAL_STONE_BLUE_ID:
							{
								ancientAdenaReward = SevenSigns.calcAncientAdenaReward(convertCount, 0, 0);
								break;
							}
							case SevenSigns.SEAL_STONE_GREEN_ID:
							{
								ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, convertCount, 0);
								break;
							}
							case SevenSigns.SEAL_STONE_RED_ID:
							{
								ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, 0, convertCount);
								break;
							}
						}
						
						if (player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true))
						{
							player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
							
							// Send inventory update packet
							iu = new InventoryUpdate();
							iu.addModifiedItem(player.getInventory().getAncientAdenaInstance());
							iu.addModifiedItem(convertItem);
							sendPacket(iu);
							
							// Update current load as well
							su = new StatusUpdate(player.getObjectId());
							su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
							sendPacket(su);
						}
					}
					else
					{
						player.sendMessage("You do not have that many seal stones.");
					}
					break;
				}
				case 19: // Seal Information (for when joining a cabal)
				{
					final int chosenSeal = Integer.parseInt(command.substring(16));
					final String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);
					showChatWindow(player, val, fileSuffix, false);
					break;
				}
				case 20: // Seal Status (for when joining a cabal)
				{
					final StringBuilder contentBuffer = new StringBuilder("<html><body><font color=\"LEVEL\">[ Seal Status ]</font><br>");
					for (int i = 1; i < 4; i++)
					{
						final int sealOwner = SevenSigns.getInstance().getSealOwner(i);
						if (sealOwner != SevenSigns.CABAL_NULL)
						{
							contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": " + SevenSigns.getCabalName(sealOwner) + "]<br>");
						}
						else
						{
							contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": Nothingness]<br>");
						}
					}
					contentBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_SevenSigns 3 " + cabal + "\">Go back.</a></body></html>");
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml(contentBuffer.toString());
					player.sendPacket(html);
					break;
				}
				default:
				{
					// 1 = Purchase Record Intro
					// 5 = Contrib Seal Stones Intro
					// 16 = Choose Type of Seal Stones to Convert
					showChatWindow(player, val, null, false);
					break;
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private final boolean getPlayerAllyHasCastle(PlayerInstance player)
	{
		final Clan playerClan = player.getClan();
		
		// The player is not in a clan, so return false.
		if (playerClan == null)
		{
			return false;
		}
		
		// If castle ownage check is clan-based rather than ally-based, check if the player's clan has a castle and return the result.
		if (!Config.ALT_GAME_REQUIRE_CLAN_CASTLE)
		{
			final int allyId = playerClan.getAllyId();
			
			// The player's clan is not in an alliance, so return false.
			if (allyId != 0)
			{
				// Check if another clan in the same alliance owns a castle, by traversing the list of clans and act accordingly.
				final Clan[] clanList = ClanTable.getInstance().getClans();
				for (Clan clan : clanList)
				{
					if ((clan.getAllyId() == allyId) && (clan.getHasCastle() > 0))
					{
						return true;
					}
				}
			}
		}
		
		return playerClan.getHasCastle() > 0;
	}
	
	private void showChatWindow(PlayerInstance player, int value, String suffix, boolean isDescription)
	{
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		filename += isDescription ? "desc_" + value : "signs_" + value;
		filename += suffix != null ? "_" + suffix + ".htm" : ".htm";
		showChatWindow(player, filename);
	}
}
