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

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.xml.MapRegionData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.handler.IVoicedCommandHandler;
import org.l2jserver.gameserver.handler.VoicedCommandHandler;
import org.l2jserver.gameserver.instancemanager.PetitionManager;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance.PunishLevel;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

public class Say2 extends GameClientPacket
{
	private static final Logger LOGGER_CHAT = Logger.getLogger("chat");
	
	private static final String[] WALKER_COMMAND_LIST =
	{
		"USESKILL",
		"USEITEM",
		"BUYITEM",
		"SELLITEM",
		"SAVEITEM",
		"LOADITEM",
		"MSG",
		"SET",
		"DELAY",
		"LABEL",
		"JMP",
		"CALL",
		"RETURN",
		"MOVETO",
		"NPCSEL",
		"NPCDLG",
		"DLGSEL",
		"CHARSTATUS",
		"POSOUTRANGE",
		"POSINRANGE",
		"GOHOME",
		"SAY",
		"EXIT",
		"PAUSE",
		"STRINDLG",
		"STRNOTINDLG",
		"CHANGEWAITTYPE",
		"FORCEATTACK",
		"ISMEMBER",
		"REQUESTJOINPARTY",
		"REQUESTOUTPARTY",
		"QUITPARTY",
		"MEMBERSTATUS",
		"CHARBUFFS",
		"ITEMCOUNT",
		"FOLLOWTELEPORT"
	};
	
	private String _text;
	private int _type;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = readD();
		_target = _type == ChatType.WHISPER.getClientId() ? readS() : null;
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			LOGGER.warning("[Say2.java] Active Character is null.");
			return;
		}
		
		ChatType chatType = ChatType.findByClientId(_type);
		if (chatType == null)
		{
			LOGGER.warning("Say2: Invalid type: " + _type + " Player : " + player.getName() + " text: " + _text);
			return;
		}
		
		if (player.isChatBanned() && !player.isGM() && (chatType != ChatType.CLAN) && (chatType != ChatType.ALLIANCE) && (chatType != ChatType.PARTY))
		{
			player.sendMessage("You may not chat while a chat ban is in effect.");
			return;
		}
		
		if (player.isInJail() && Config.JAIL_DISABLE_CHAT && ((chatType == ChatType.WHISPER) || (chatType == ChatType.SHOUT) || (chatType == ChatType.TRADE) || (chatType == ChatType.HERO_VOICE)))
		{
			player.sendMessage("You can not chat with players outside of the jail.");
			return;
		}
		
		if (!getClient().getFloodProtectors().getSayAction().tryPerformAction("Say2"))
		{
			player.sendMessage("You cannot speak too fast.");
			return;
		}
		
		if (player.isCursedWeaponEquiped() && ((chatType == ChatType.TRADE) || (chatType == ChatType.SHOUT)))
		{
			player.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
			return;
		}
		
		if ((chatType == ChatType.PETITION_PLAYER) && player.isGM())
		{
			chatType = ChatType.PETITION_GM;
		}
		
		if (_text.length() > Config.MAX_CHAT_LENGTH)
		{
			_text = _text.substring(0, Config.MAX_CHAT_LENGTH);
			// return;
		}
		
		if (Config.LOG_CHAT)
		{
			if (chatType == ChatType.WHISPER)
			{
				LOGGER_CHAT.info(chatType.name() + " [" + player + " to " + _target + "] " + _text);
			}
			else
			{
				LOGGER_CHAT.info(chatType.name() + " [" + player + "] " + _text);
			}
		}
		
		if (Config.L2WALKER_PROTECTION && (chatType == ChatType.WHISPER) && checkBot(_text))
		{
			Util.handleIllegalPlayerAction(player, "Client Emulator Detect: Player " + player.getName() + " using l2walker.", Config.DEFAULT_PUNISH);
			return;
		}
		_text = _text.replaceAll("\\\\n", "");
		
		// Say Filter implementation
		if (Config.USE_SAY_FILTER)
		{
			checkText(player);
		}
		
		final WorldObject saymode = player.getSayMode();
		if (saymode != null)
		{
			final String name = saymode.getName();
			final int actor = saymode.getObjectId();
			_type = 0;
			final Collection<WorldObject> list = saymode.getKnownList().getKnownObjects().values();
			final CreatureSay cs = new CreatureSay(actor, chatType, name, _text);
			for (WorldObject obj : list)
			{
				if (!(obj instanceof Creature))
				{
					continue;
				}
				final Creature chara = (Creature) obj;
				chara.sendPacket(cs);
			}
			return;
		}
		
		final CreatureSay cs = new CreatureSay(player.getObjectId(), chatType, player.getName(), _text);
		switch (chatType)
		{
			case WHISPER:
			{
				final PlayerInstance receiver = World.getInstance().getPlayer(_target);
				if (receiver == null)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_CURRENTLY_LOGGED_IN);
					sm.addString(_target);
					player.sendPacket(sm);
					return;
				}
				if (!receiver.getBlockList().isInBlockList(player) || player.isGM())
				{
					if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
					{
						player.sendMessage("Player is in jail.");
						return;
					}
					if (receiver.isChatBanned() && !player.isGM())
					{
						player.sendMessage("Player is chat banned.");
						return;
					}
					if (receiver.isInOfflineMode())
					{
						player.sendMessage("Player is in offline mode.");
						return;
					}
					if (!receiver.isInRefusalMode())
					{
						receiver.sendPacket(cs);
						player.sendPacket(new CreatureSay(player.getObjectId(), chatType, "->" + receiver.getName(), _text));
					}
					else
					{
						player.sendPacket(SystemMessageId.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					}
				}
				else if (receiver.getBlockList().isInBlockList(player))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST);
					sm.addString(_target);
					player.sendPacket(sm);
				}
				break;
			}
			case SHOUT:
			{
				// Flood protect Say
				if (!getClient().getFloodProtectors().getGlobalChat().tryPerformAction("global chat"))
				{
					return;
				}
				if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && player.isGM()))
				{
					if (Config.GLOBAL_CHAT_WITH_PVP)
					{
						if ((player.getPvpKills() < Config.GLOBAL_PVP_AMOUNT) && !player.isGM())
						{
							player.sendMessage("You must have at least " + Config.GLOBAL_PVP_AMOUNT + " pvp kills in order to speak in global chat");
							return;
						}
						final int region = MapRegionData.getInstance().getMapRegion(player.getX(), player.getY());
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							if (region == MapRegionData.getInstance().getMapRegion(plr.getX(), plr.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!plr.getBlockList().isInBlockList(player))
								{
									plr.sendPacket(cs);
								}
							}
						}
					}
					else
					{
						final int region = MapRegionData.getInstance().getMapRegion(player.getX(), player.getY());
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							if (region == MapRegionData.getInstance().getMapRegion(plr.getX(), plr.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!plr.getBlockList().isInBlockList(player))
								{
									plr.sendPacket(cs);
								}
							}
						}
					}
				}
				else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("GLOBAL"))
				{
					if (Config.GLOBAL_CHAT_WITH_PVP)
					{
						if ((player.getPvpKills() < Config.GLOBAL_PVP_AMOUNT) && !player.isGM())
						{
							player.sendMessage("You must have at least " + Config.GLOBAL_PVP_AMOUNT + " pvp kills in order to speak in global chat");
							return;
						}
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!plr.getBlockList().isInBlockList(player))
							{
								plr.sendPacket(cs);
							}
						}
					}
					else
					{
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!plr.getBlockList().isInBlockList(player))
							{
								plr.sendPacket(cs);
							}
						}
					}
				}
				break;
			}
			case TRADE:
			{
				if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("ON"))
				{
					if (Config.TRADE_CHAT_WITH_PVP)
					{
						if ((player.getPvpKills() <= Config.TRADE_PVP_AMOUNT) && !player.isGM())
						{
							player.sendMessage("You must have at least " + Config.TRADE_PVP_AMOUNT + "  pvp kills in order to speak in trade chat");
							return;
						}
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!plr.getBlockList().isInBlockList(player))
							{
								plr.sendPacket(cs);
							}
						}
					}
					else
					{
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!plr.getBlockList().isInBlockList(player))
							{
								plr.sendPacket(cs);
							}
						}
					}
				}
				else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
				{
					if (Config.TRADE_CHAT_WITH_PVP)
					{
						if ((player.getPvpKills() <= Config.TRADE_PVP_AMOUNT) && !player.isGM())
						{
							player.sendMessage("You must have at least " + Config.TRADE_PVP_AMOUNT + "  pvp kills in order to speak in trade chat");
							return;
						}
						final int region = MapRegionData.getInstance().getMapRegion(player.getX(), player.getY());
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							if (region == MapRegionData.getInstance().getMapRegion(plr.getX(), plr.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!plr.getBlockList().isInBlockList(player))
								{
									plr.sendPacket(cs);
								}
							}
						}
					}
					else if (Config.TRADE_CHAT_IS_NOOBLE)
					{
						if (!player.isNoble() && !player.isGM())
						{
							player.sendMessage("Only Nobless Players Can Use This Chat");
							return;
						}
						final int region = MapRegionData.getInstance().getMapRegion(player.getX(), player.getY());
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							if (region == MapRegionData.getInstance().getMapRegion(plr.getX(), plr.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!plr.getBlockList().isInBlockList(player))
								{
									plr.sendPacket(cs);
								}
							}
						}
					}
					else
					{
						final int region = MapRegionData.getInstance().getMapRegion(player.getX(), player.getY());
						for (PlayerInstance plr : World.getInstance().getAllPlayers())
						{
							if (region == MapRegionData.getInstance().getMapRegion(plr.getX(), plr.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!plr.getBlockList().isInBlockList(player))
								{
									plr.sendPacket(cs);
								}
							}
						}
					}
				}
				break;
			}
			case GENERAL:
			{
				if (_text.startsWith("."))
				{
					final StringTokenizer st = new StringTokenizer(_text);
					IVoicedCommandHandler vch;
					String command = "";
					String target = "";
					if (st.countTokens() > 1)
					{
						command = st.nextToken().substring(1);
						target = _text.substring(command.length() + 2);
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					else
					{
						command = _text.substring(1);
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					if (vch != null)
					{
						vch.useVoicedCommand(command, player, target);
						break;
					}
				}
				for (PlayerInstance plr : player.getKnownList().getKnownPlayers().values())
				{
					if ((plr != null) && player.isInsideRadius(plr, 1250, false, true))
					{
						// Like L2OFF if player is blocked can't read the message
						if (!plr.getBlockList().isInBlockList(player))
						{
							plr.sendPacket(cs);
						}
					}
				}
				player.sendPacket(cs);
				break;
			}
			case CLAN:
			{
				if (player.getClan() != null)
				{
					player.getClan().broadcastToOnlineMembers(cs);
				}
				break;
			}
			case ALLIANCE:
			{
				if (player.getClan() != null)
				{
					player.getClan().broadcastToOnlineAllyMembers(cs);
				}
				break;
			}
			case PARTY:
			{
				if (player.isInParty())
				{
					player.getParty().broadcastToPartyMembers(cs);
				}
				break;
			}
			case PETITION_PLAYER:
			case PETITION_GM:
			{
				if (!PetitionManager.getInstance().isPlayerInConsultation(player))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT);
					break;
				}
				PetitionManager.getInstance().sendActivePetitionMessage(player, _text);
				break;
			}
			case PARTYROOM_ALL:
			{
				if (player.isInParty() && player.getParty().isInCommandChannel() && player.getParty().isLeader(player))
				{
					player.getParty().getCommandChannel().broadcastCSToChannelMembers(cs, player);
				}
				break;
			}
			case PARTYROOM_COMMANDER:
			{
				if (player.isInParty() && player.getParty().isInCommandChannel() && player.getParty().getCommandChannel().getChannelLeader().equals(player))
				{
					player.getParty().getCommandChannel().broadcastCSToChannelMembers(cs, player);
				}
				break;
			}
			case HERO_VOICE:
			{
				if (player.isGM())
				{
					for (PlayerInstance plr : World.getInstance().getAllPlayers())
					{
						if (plr == null)
						{
							continue;
						}
						plr.sendPacket(cs);
					}
				}
				else if (player.isHero())
				{
					// Flood protect Hero Voice
					if (!getClient().getFloodProtectors().getHeroVoice().tryPerformAction("hero voice"))
					{
						return;
					}
					for (PlayerInstance plr : World.getInstance().getAllPlayers())
					{
						if (plr == null)
						{
							continue;
						}
						// Like L2OFF if player is blocked can't read the message
						if (!plr.getBlockList().isInBlockList(player))
						{
							plr.sendPacket(cs);
						}
					}
				}
				break;
			}
		}
	}
	
	private boolean checkBot(String text)
	{
		for (String botCommand : WALKER_COMMAND_LIST)
		{
			if (text.startsWith(botCommand))
			{
				return true;
			}
		}
		return false;
	}
	
	private void checkText(PlayerInstance player)
	{
		if (Config.USE_SAY_FILTER)
		{
			String filteredText = _text.toLowerCase();
			for (String pattern : Config.FILTER_LIST)
			{
				filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
			}
			
			if (!filteredText.equalsIgnoreCase(_text))
			{
				if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("chat"))
				{
					player.setPunishLevel(PunishLevel.CHAT, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
					player.sendMessage("Administrator banned you chat from " + Config.CHAT_FILTER_PUNISHMENT_PARAM1 + " minutes");
				}
				else if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("karma"))
				{
					player.setKarma(Config.CHAT_FILTER_PUNISHMENT_PARAM2);
					player.sendMessage("You have get " + Config.CHAT_FILTER_PUNISHMENT_PARAM2 + " karma for bad words");
				}
				else if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("jail"))
				{
					player.setPunishLevel(PunishLevel.JAIL, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
				}
				player.sendMessage("The word " + _text + " is not allowed!");
				_text = filteredText;
			}
		}
	}
}