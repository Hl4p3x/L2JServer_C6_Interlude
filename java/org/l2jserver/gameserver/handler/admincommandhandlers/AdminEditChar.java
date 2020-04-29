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
package org.l2jserver.gameserver.handler.admincommandhandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.HeroSkillTable;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.Hero;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jserver.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jserver.gameserver.network.serverpackets.SetSummonRemainTime;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.BuilderUtil;
import org.l2jserver.gameserver.util.Util;

public class AdminEditChar implements IAdminCommandHandler
{
	protected static final Logger LOGGER = Logger.getLogger(AdminEditChar.class.getName());
	
	private static String[] ADMIN_COMMANDS =
	{
		"admin_changename", // changes char name
		"admin_setname", // changes char name
		"admin_edit_character",
		"admin_current_player",
		"admin_nokarma",
		"admin_setkarma",
		"admin_character_list", // same as character_info, kept for compatibility purposes
		"admin_character_info", // given a player name, displays an information window
		"admin_show_characters",
		"admin_find_character",
		"admin_find_dualbox",
		"admin_find_ip", // find all the player connections from a given IPv4 number
		"admin_find_account", // list all the characters from an account (useful for GMs w/o DB access)
		"admin_save_modifications", // consider it deprecated...
		"admin_rec",
		"admin_setclass",
		"admin_settitle",
		"admin_setsex",
		"admin_setcolor",
		"admin_fullfood",
		"admin_remclanwait",
		"admin_setcp",
		"admin_sethp",
		"admin_setmp",
		"admin_setchar_cp",
		"admin_setchar_hp",
		"admin_setchar_mp",
		"admin_sethero"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case "admin_changename":
			case "admin_setname":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //changename|setname <new_name_for_target>");
					return false;
				}
				final WorldObject target = activeChar.getTarget();
				PlayerInstance player = null;
				String oldName = null;
				if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
					oldName = player.getName();
					World.getInstance().removeFromAllPlayers(player);
					player.setName(val);
					player.store();
					World.getInstance().addToAllPlayers(player);
					player.sendMessage("Your name has been changed by a GM.");
					player.broadcastUserInfo();
					if (player.isInParty())
					{
						// Delete party window for other party members
						player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
						for (PlayerInstance member : player.getParty().getPartyMembers())
						{
							// And re-add
							if (member != player)
							{
								member.sendPacket(new PartySmallWindowAll(player, player.getParty()));
							}
						}
					}
					if (player.getClan() != null)
					{
						player.getClan().updateClanMember(player);
						player.getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(player));
						player.sendPacket(new PledgeShowMemberListAll(player.getClan(), player));
					}
				}
				else if (target instanceof NpcInstance)
				{
					final NpcInstance npc = (NpcInstance) target;
					oldName = npc.getName();
					npc.setName(val);
					npc.updateAbnormalEffect();
				}
				if (oldName == null)
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return false;
				}
				BuilderUtil.sendSysMessage(activeChar, "Name changed from " + oldName + " to " + val);
				return true;
			} // changes char name
			case "admin_edit_character":
			{
				editCharacter(activeChar);
				return true;
			}
			case "admin_current_player":
			{
				showCharacterInfo(activeChar, null);
				return true;
			}
			case "admin_nokarma":
			{
				setTargetKarma(activeChar, 0);
				return true;
			}
			case "admin_setkarma":
			{
				int karma = 0;
				if (st.hasMoreTokens())
				{
					final String val = st.nextToken();
					try
					{
						karma = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //setkarma new_karma_for_target(number)");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setkarma new_karma_for_target");
					return false;
				}
				setTargetKarma(activeChar, karma);
				return true;
			}
			case "admin_character_list":
			case "admin_character_info":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //character_info <player_name>");
					return false;
				}
				final PlayerInstance target = World.getInstance().getPlayer(val);
				if (target != null)
				{
					showCharacterInfo(activeChar, target);
					return true;
				}
				activeChar.sendPacket(SystemMessageId.THAT_CHARACTER_DOES_NOT_EXIST);
				return false;
			}
			case "admin_show_characters":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						final int page = Integer.parseInt(val);
						listCharacters(activeChar, page);
						return true;
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //show_characters <page_number>");
						listCharacters(activeChar, 0);
						return false;
					}
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //show_characters <page_number>");
				listCharacters(activeChar, 0);
				return false;
			}
			case "admin_find_character":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //find_character <player_name>");
					listCharacters(activeChar, 0);
					return false;
				}
				findCharacter(activeChar, val);
				return true;
			}
			case "admin_find_dualbox":
			{
				String val = "";
				int boxes = 2;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						boxes = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //find_dualbox <boxes_number>(default 2)");
						listCharacters(activeChar, 0);
						return false;
					}
				}
				findMultibox(activeChar, boxes);
				return true;
			}
			case "admin_find_ip":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //find_ip <ip>");
					listCharacters(activeChar, 0);
					return false;
				}
				try
				{
					findCharactersPerIp(activeChar, val);
				}
				catch (IllegalArgumentException e)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //find_ip <ip>");
					listCharacters(activeChar, 0);
					return false;
				}
				return true;
			}
			case "admin_find_account":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //find_account <account_name>");
					listCharacters(activeChar, 0);
					return false;
				}
				findCharactersPerAccount(activeChar, val);
				return true;
			}
			case "admin_save_modifications":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //save_modifications <mods>");
					listCharacters(activeChar, 0);
					return false;
				}
				adminModifyCharacter(activeChar, val);
				return true;
			}
			case "admin_rec":
			{
				String val = "";
				int value = 1;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						value = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //rec <value>(default 1)");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //rec <value>(default 1)");
					return false;
				}
				final WorldObject target = activeChar.getTarget();
				PlayerInstance player = null;
				if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Select player before. Usage: //rec <value>(default 1)");
					listCharacters(activeChar, 0);
					return false;
				}
				player.setRecomHave(player.getRecomHave() + value);
				final SystemMessage sm = new SystemMessage(SystemMessageId.GM_S1);
				sm.addString("You have been recommended by a GM");
				player.sendPacket(sm);
				player.broadcastUserInfo();
				return true;
			}
			case "admin_setclass":
			{
				String val = "";
				int classidval = 0;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						classidval = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //setclass <value>(default 1)");
						return false;
					}
				}
				else
				{
					AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
					return false;
				}
				final WorldObject target = activeChar.getTarget();
				PlayerInstance player = null;
				if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
				}
				else
				{
					return false;
				}
				boolean valid = false;
				for (ClassId classid : ClassId.values())
				{
					if (classidval == classid.getId())
					{
						valid = true;
					}
				}
				if (valid && (player.getClassId().getId() != classidval))
				{
					player.setClassId(classidval);
					final ClassId classId = ClassId.getClassId(classidval);
					if (!player.isSubClassActive())
					{
						player.setBaseClass(classId);
					}
					final String newclass = player.getTemplate().getClassName();
					player.store();
					if (player != activeChar)
					{
						player.sendMessage("A GM changed your class to " + newclass);
					}
					player.broadcastUserInfo();
					activeChar.sendMessage(player.getName() + " changed to " + newclass);
					return true;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //setclass <valid_new_classid>");
				return false;
			}
			case "admin_settitle":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						if (val.equals(""))
						{
							val = st.nextToken();
						}
						else
						{
							val += " " + st.nextToken();
						}
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //settitle <new_title>");
					return false;
				}
				final WorldObject target = activeChar.getTarget();
				PlayerInstance player = null;
				NpcInstance npc = null;
				if (target == null)
				{
					player = activeChar;
				}
				else if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
				}
				else if (target instanceof NpcInstance)
				{
					npc = (NpcInstance) target;
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Select your target before the command");
					return false;
				}
				if (player != null)
				{
					player.setTitle(val);
					if (player != activeChar)
					{
						player.sendMessage("Your title has been changed by a GM");
					}
					player.broadcastTitleInfo();
				}
				else if (npc != null)
				{
					npc.setTitle(val);
					npc.updateAbnormalEffect();
				}
				return true;
			}
			case "admin_setsex":
			{
				final WorldObject target = activeChar.getTarget();
				PlayerInstance player = null;
				if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Select player before command");
					return false;
				}
				if (player.getAppearance().isFemale())
				{
					player.getAppearance().setMale();
				}
				else
				{
					player.getAppearance().setFemale();
				}
				PlayerInstance.setSexDB(player, 1);
				player.sendMessage("Your gender has been changed by a GM");
				player.decayMe();
				player.spawnMe(player.getX(), player.getY(), player.getZ());
				player.broadcastUserInfo();
				return true;
			}
			case "admin_setcolor":
			{
				String val = "";
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setcolor <new_color>");
					return false;
				}
				final WorldObject target = activeChar.getTarget();
				if (target == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "You have to select a player!");
					return false;
				}
				if (!(target instanceof PlayerInstance))
				{
					BuilderUtil.sendSysMessage(activeChar, "Your target is not a player!");
					return false;
				}
				final PlayerInstance player = (PlayerInstance) target;
				player.getAppearance().setNameColor(Integer.decode("0x" + val));
				player.sendMessage("Your name color has been changed by a GM");
				player.broadcastUserInfo();
				return true;
			}
			case "admin_fullfood":
			{
				final WorldObject target = activeChar.getTarget();
				if (target instanceof PetInstance)
				{
					final PetInstance targetPet = (PetInstance) target;
					targetPet.setCurrentFed(targetPet.getMaxFed());
					targetPet.getOwner().sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					return false;
				}
				return true;
			}
			case "admin_remclanwait":
			{
				final WorldObject target = activeChar.getTarget();
				PlayerInstance player = null;
				if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "You have to select a player!");
					return false;
				}
				if (player.getClan() == null)
				{
					player.setClanJoinExpiryTime(0);
					player.sendMessage("A GM Has reset your clan wait time, You may now join another clan.");
					BuilderUtil.sendSysMessage(activeChar, "You have reset " + player.getName() + "'s wait time to join another clan.");
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Sorry, but " + player.getName() + " must not be in a clan. Player must leave clan before the wait limit can be reset.");
					return false;
				}
				return true;
			}
			case "admin_setcp":
			{
				String val = "";
				int value = 0;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						value = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Value must be an integer");
						BuilderUtil.sendSysMessage(activeChar, "Usage: //setcp <new_value>");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setcp <new_value>");
					return false;
				}
				activeChar.getStatus().setCurrentCp(value);
				return true;
			}
			case "admin_sethp":
			{
				String val = "";
				int value = 0;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						value = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Value must be an integer");
						BuilderUtil.sendSysMessage(activeChar, "Usage: //sethp <new_value>");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //sethp <new_value>");
					return false;
				}
				activeChar.getStatus().setCurrentHp(value);
				return true;
			}
			case "admin_setmp":
			{
				String val = "";
				int value = 0;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						value = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Value must be an integer");
						BuilderUtil.sendSysMessage(activeChar, "Usage: //setmp <new_value>");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setmp <new_value>");
					return false;
				}
				activeChar.getStatus().setCurrentMp(value);
				return true;
			}
			case "admin_setchar_cp":
			{
				String val = "";
				int value = 0;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						value = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Value must be an integer");
						BuilderUtil.sendSysMessage(activeChar, "Usage: //setchar_cp <new_value>");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setchar_cp <new_value>");
					return false;
				}
				if (activeChar.getTarget() instanceof PlayerInstance)
				{
					final PlayerInstance pc = (PlayerInstance) activeChar.getTarget();
					pc.getStatus().setCurrentCp(value);
				}
				else if (activeChar.getTarget() instanceof PetInstance)
				{
					final PetInstance pet = (PetInstance) activeChar.getTarget();
					pet.getStatus().setCurrentCp(value);
				}
				else
				{
					activeChar.getStatus().setCurrentCp(value);
				}
				return true;
			}
			case "admin_setchar_hp":
			{
				String val = "";
				int value = 0;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						value = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Value must be an integer");
						BuilderUtil.sendSysMessage(activeChar, "Usage: //setchar_hp <new_value>");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setchar_hp <new_value>");
					return false;
				}
				if (activeChar.getTarget() instanceof PlayerInstance)
				{
					final PlayerInstance pc = (PlayerInstance) activeChar.getTarget();
					pc.getStatus().setCurrentHp(value);
				}
				else if (activeChar.getTarget() instanceof PetInstance)
				{
					final PetInstance pet = (PetInstance) activeChar.getTarget();
					pet.getStatus().setCurrentHp(value);
				}
				else
				{
					activeChar.getStatus().setCurrentHp(value);
				}
				return true;
			}
			case "admin_setchar_mp":
			{
				String val = "";
				int value = 0;
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
					try
					{
						value = Integer.parseInt(val);
					}
					catch (NumberFormatException e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Value must be an integer");
						BuilderUtil.sendSysMessage(activeChar, "Usage: //setchar_mp <new_value>");
						return false;
					}
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setchar_mp <new_value>");
					return false;
				}
				if (activeChar.getTarget() instanceof PlayerInstance)
				{
					final PlayerInstance pc = (PlayerInstance) activeChar.getTarget();
					pc.getStatus().setCurrentMp(value);
				}
				else if (activeChar.getTarget() instanceof PetInstance)
				{
					final PetInstance pet = (PetInstance) activeChar.getTarget();
					pet.getStatus().setCurrentMp(value);
				}
				else
				{
					activeChar.getStatus().setCurrentMp(value);
				}
				return true;
			}
			case "admin_sethero":
			{
				try
				{
					PlayerInstance target;
					if ((activeChar.getTarget() != null) && (activeChar.getTarget() instanceof PlayerInstance))
					{
						target = (PlayerInstance) activeChar.getTarget(); // Target - player
					}
					else
					{
						target = activeChar; // No target
					}
					if (target != null)
					{
						final String[] tokens = command.split(" ");
						boolean param = true;
						boolean save = false;
						if (tokens.length == 2)
						{
							param = Boolean.parseBoolean(tokens[1]);
						}
						else if (tokens.length == 3)
						{
							param = Boolean.parseBoolean(tokens[1]);
							save = Boolean.parseBoolean(tokens[2]);
						}
						else if (tokens.length > 3)
						{
							throw new Exception("too many tokens");
						}
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, target);
						target.setTarget(target);
						target.setHero(param);
						if (!target.isHero())
						{
							for (Skill skill : HeroSkillTable.getHeroSkills())
							{
								target.removeSkill(skill, false);
							}
							Hero.getInstance().deleteHero(target, false);
						}
						else
						{
							target.broadcastPacket(new SocialAction(target.getObjectId(), 16));
							for (Skill skill : HeroSkillTable.getHeroSkills())
							{
								target.addSkill(skill);
							}
							Hero.getInstance().putHero(target, false);
						}
						target.broadcastStatusUpdate();
						target.broadcastUserInfo();
						if (save)
						{
							Olympiad.getInstance().saveOlympiadStatus();
						}
					}
				}
				catch (Exception e)
				{
					BuilderUtil.sendSysMessage(activeChar, "Example: //sethero <trigger> <save>");
				}
			}
		}
		
		return false;
	}
	
	private void listCharacters(PlayerInstance activeChar, int page)
	{
		final List<PlayerInstance> onlinePlayersList = new ArrayList<>();
		for (PlayerInstance actualPlayer : World.getInstance().getAllPlayers())
		{
			if ((actualPlayer != null) && actualPlayer.isOnline() && !actualPlayer.isInOfflineMode())
			{
				onlinePlayersList.add(actualPlayer);
			}
			else if (actualPlayer == null)
			{
				LOGGER.warning("listCharacters: found player null into World Instance..");
			}
		}
		
		final PlayerInstance[] players = onlinePlayersList.toArray(new PlayerInstance[onlinePlayersList.size()]);
		final int MaxCharactersPerPage = 20;
		int maxPages = players.length / MaxCharactersPerPage;
		if (players.length > (MaxCharactersPerPage * maxPages))
		{
			maxPages++;
		}
		
		// Check if number of users changed
		if (page > maxPages)
		{
			page = maxPages;
		}
		
		final int CharactersStart = MaxCharactersPerPage * page;
		int charactersEnd = players.length;
		if ((charactersEnd - CharactersStart) > MaxCharactersPerPage)
		{
			charactersEnd = CharactersStart + MaxCharactersPerPage;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charlist.htm");
		StringBuilder replyMSG = new StringBuilder();
		for (int x = 0; x < maxPages; x++)
		{
			final int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
		}
		
		adminReply.replace("%pages%", replyMSG.toString());
		replyMSG = new StringBuilder();
		for (int i = CharactersStart; i < charactersEnd; i++)
		{
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_info " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().getClassName() + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
		}
		
		adminReply.replace("%players%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public static void gatherCharacterInfo(PlayerInstance activeChar, PlayerInstance player, String filename)
	{
		String ip = "N/A";
		String account = "N/A";
		if (player.getClient() != null)
		{
			account = player.getClient().getAccountName();
			ip = player.getClient().getIpAddress();
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		final Clan playerClan = ClanTable.getInstance().getClan(player.getClanId());
		if (playerClan != null)
		{
			adminReply.replace("%clan%", playerClan.getName());
		}
		else
		{
			adminReply.replace("%clan%", "no Clan");
		}
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().getClassName());
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%x%", String.valueOf(player.getX()));
		adminReply.replace("%y%", String.valueOf(player.getY()));
		adminReply.replace("%z%", String.valueOf(player.getZ()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getKarma()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo(((float) player.getCurrentLoad() / player.getMaxLoad()) * 100, 2)));
		adminReply.replace("%patk%", String.valueOf(player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf(player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%access%", String.valueOf(player.getAccessLevel().getLevel()));
		adminReply.replace("%account%", account);
		adminReply.replace("%ip%", ip);
		adminReply.replace("%protocol%", String.valueOf(player.getClient() != null ? player.getClient().getProtocolVersion() : "NULL"));
		activeChar.sendPacket(adminReply);
	}
	
	private void setTargetKarma(PlayerInstance activeChar, int newKarma)
	{
		// function to change karma of selected char
		final WorldObject target = activeChar.getTarget();
		PlayerInstance player = null;
		if (target instanceof PlayerInstance)
		{
			player = (PlayerInstance) target;
		}
		else
		{
			return;
		}
		
		if (newKarma >= 0)
		{
			// for display
			final int oldKarma = player.getKarma();
			
			// update karma
			player.setKarma(newKarma);
			
			final StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.KARMA, newKarma);
			player.sendPacket(su);
			
			// Common character information
			final SystemMessage sm = new SystemMessage(SystemMessageId.GM_S1);
			sm.addString("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
			player.sendPacket(sm);
			
			// Admin information
			if (player != activeChar)
			{
				BuilderUtil.sendSysMessage(activeChar, "Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
			}
		}
		else
		{
			// tell admin of mistake
			BuilderUtil.sendSysMessage(activeChar, "You must enter a value for karma greater than or equal to 0.");
		}
	}
	
	private void adminModifyCharacter(PlayerInstance activeChar, String modifications)
	{
		final WorldObject target = activeChar.getTarget();
		if (!(target instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) target;
		final StringTokenizer st = new StringTokenizer(modifications);
		if (st.countTokens() != 6)
		{
			editCharacter(player);
			return;
		}
		
		final String hp = st.nextToken();
		final String mp = st.nextToken();
		final String cp = st.nextToken();
		final String pvpflag = st.nextToken();
		final String pvpkills = st.nextToken();
		final String pkkills = st.nextToken();
		final int hpval = Integer.parseInt(hp);
		final int mpval = Integer.parseInt(mp);
		final int cpval = Integer.parseInt(cp);
		final int pvpflagval = Integer.parseInt(pvpflag);
		final int pvpkillsval = Integer.parseInt(pvpkills);
		final int pkkillsval = Integer.parseInt(pkkills);
		
		// Common character information
		player.sendMessage("Admin has changed your stats.  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP Flag: " + pvpflagval + " PvP/PK " + pvpkillsval + "/" + pkkillsval);
		player.getStatus().setCurrentHp(hpval);
		player.getStatus().setCurrentMp(mpval);
		player.getStatus().setCurrentCp(cpval);
		player.setPvpFlag(pvpflagval);
		player.setPvpKills(pvpkillsval);
		player.setPkKills(pkkillsval);
		
		// Save the changed parameters to the database.
		player.store();
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, hpval);
		su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
		su.addAttribute(StatusUpdate.CUR_MP, mpval);
		su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
		su.addAttribute(StatusUpdate.CUR_CP, cpval);
		su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
		player.sendPacket(su);
		
		// Admin information
		player.sendMessage("Changed stats of " + player.getName() + ".  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval);
		showCharacterInfo(activeChar, null); // Back to start
		player.broadcastUserInfo();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.decayMe();
		player.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	private void editCharacter(PlayerInstance activeChar)
	{
		final WorldObject target = activeChar.getTarget();
		if (!(target instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) target;
		gatherCharacterInfo(activeChar, player, "charedit.htm");
	}
	
	private void findCharacter(PlayerInstance activeChar, String characterToFind)
	{
		int charactersFound = 0;
		String name;
		final Collection<PlayerInstance> allPlayers = World.getInstance().getAllPlayers();
		final PlayerInstance[] players = allPlayers.toArray(new PlayerInstance[allPlayers.size()]);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charfind.htm");
		StringBuilder replyMSG = new StringBuilder();
		for (PlayerInstance player : players)
		{
			name = player.getName();
			if (name.toLowerCase().contains(characterToFind.toLowerCase()))
			{
				charactersFound = charactersFound + 1;
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().getClassName() + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			}
			
			if (charactersFound > 20)
			{
				break;
			}
		}
		
		adminReply.replace("%results%", replyMSG.toString());
		replyMSG = new StringBuilder();
		if (charactersFound == 0)
		{
			replyMSG.append("s. Please try again.");
		}
		else if (charactersFound > 20)
		{
			adminReply.replace("%number%", " more than 20");
			replyMSG.append("s.<br>Please refine your search to see all of the results.");
		}
		else if (charactersFound == 1)
		{
			replyMSG.append('.');
		}
		else
		{
			replyMSG.append("s.");
		}
		
		adminReply.replace("%number%", String.valueOf(charactersFound));
		adminReply.replace("%end%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void findMultibox(PlayerInstance activeChar, int multibox)
	{
		final Collection<PlayerInstance> allPlayers = World.getInstance().getAllPlayers();
		final PlayerInstance[] players = allPlayers.toArray(new PlayerInstance[allPlayers.size()]);
		final Map<String, List<PlayerInstance>> ipMap = new HashMap<>();
		String ip = "0.0.0.0";
		
		final Map<String, Integer> dualboxIPs = new HashMap<>();
		for (PlayerInstance player : players)
		{
			if ((player.getClient() == null) || (player.getClient().getConnection() == null) || (player.getClient().getConnection().getInetAddress() == null) || (player.getClient().getConnection().getInetAddress().getHostAddress() == null))
			{
				continue;
			}
			
			ip = player.getClient().getConnection().getInetAddress().getHostAddress();
			if (ipMap.get(ip) == null)
			{
				ipMap.put(ip, new ArrayList<PlayerInstance>());
			}
			ipMap.get(ip).add(player);
			
			if (ipMap.get(ip).size() >= multibox)
			{
				final Integer count = dualboxIPs.get(ip);
				if (count == null)
				{
					dualboxIPs.put(ip, 0);
				}
				else
				{
					dualboxIPs.put(ip, count + 1);
				}
			}
		}
		
		final List<String> keys = new ArrayList<>(dualboxIPs.keySet());
		Collections.sort(keys, (left, right) -> dualboxIPs.get(left).compareTo(dualboxIPs.get(right)));
		Collections.reverse(keys);
		
		final StringBuilder results = new StringBuilder();
		for (String dualboxIP : keys)
		{
			results.append("<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + "</a><br1>");
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param ipAdress
	 */
	private void findCharactersPerIp(PlayerInstance activeChar, String ipAdress)
	{
		if (!ipAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
		{
			throw new IllegalArgumentException("Malformed IPv4 number");
		}
		
		final Collection<PlayerInstance> allPlayers = World.getInstance().getAllPlayers();
		final PlayerInstance[] players = allPlayers.toArray(new PlayerInstance[allPlayers.size()]);
		int charactersFound = 0;
		String name;
		String ip = "0.0.0.0";
		StringBuilder replyMSG = new StringBuilder();
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/ipfind.htm");
		
		for (PlayerInstance player : players)
		{
			if ((player.getClient() == null) || (player.getClient().getConnection() == null) || (player.getClient().getConnection().getInetAddress() == null) || (player.getClient().getConnection().getInetAddress().getHostAddress() == null))
			{
				continue;
			}
			
			ip = player.getClient().getConnection().getInetAddress().getHostAddress();
			if (ip.equals(ipAdress))
			{
				name = player.getName();
				charactersFound = charactersFound + 1;
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().getClassName() + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			}
			
			if (charactersFound > 20)
			{
				break;
			}
		}
		
		adminReply.replace("%results%", replyMSG.toString());
		replyMSG = new StringBuilder();
		if (charactersFound == 0)
		{
			replyMSG.append("s. Maybe they got d/c? :)");
		}
		else if (charactersFound > 20)
		{
			adminReply.replace("%number%", " more than " + charactersFound);
			replyMSG.append("s.<br>In order to avoid you a client crash I won't <br1>display results beyond the 20th character.");
		}
		else if (charactersFound == 1)
		{
			replyMSG.append('.');
		}
		else
		{
			replyMSG.append("s.");
		}
		
		adminReply.replace("%ip%", ip);
		adminReply.replace("%number%", String.valueOf(charactersFound));
		adminReply.replace("%end%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param characterName
	 */
	private void findCharactersPerAccount(PlayerInstance activeChar, String characterName)
	{
		if (characterName.matches(Config.CNAME_TEMPLATE))
		{
			String account = null;
			Map<Integer, String> chars;
			final PlayerInstance player = World.getInstance().getPlayer(characterName);
			if (player == null)
			{
				throw new IllegalArgumentException("Player doesn't exist");
			}
			
			chars = player.getAccountChars();
			account = player.getAccountName();
			
			final StringBuilder replyMSG = new StringBuilder();
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile("data/html/admin/accountinfo.htm");
			
			for (String charname : chars.values())
			{
				replyMSG.append(charname + "<br1>");
			}
			
			adminReply.replace("%characters%", replyMSG.toString());
			adminReply.replace("%account%", account);
			adminReply.replace("%player%", characterName);
			activeChar.sendPacket(adminReply);
		}
		else
		{
			throw new IllegalArgumentException("Malformed character name");
		}
	}
	
	private void showCharacterInfo(PlayerInstance activeChar, PlayerInstance player)
	{
		if (player == null)
		{
			final WorldObject target = activeChar.getTarget();
			if (target instanceof PlayerInstance)
			{
				player = (PlayerInstance) target;
			}
			else
			{
				return;
			}
		}
		else
		{
			activeChar.setTarget(player);
		}
		
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}