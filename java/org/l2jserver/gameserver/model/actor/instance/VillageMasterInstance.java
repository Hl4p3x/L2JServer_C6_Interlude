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

import java.util.Iterator;
import java.util.Set;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.sql.SkillTreeTable;
import org.l2jserver.gameserver.datatables.xml.PlayerTemplateData;
import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.FortManager;
import org.l2jserver.gameserver.instancemanager.FortSiegeManager;
import org.l2jserver.gameserver.instancemanager.SiegeManager;
import org.l2jserver.gameserver.model.PledgeSkillLearn;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.base.ClassType;
import org.l2jserver.gameserver.model.base.PlayerClass;
import org.l2jserver.gameserver.model.base.SubClass;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.clan.Clan.SubPledge;
import org.l2jserver.gameserver.model.clan.ClanMember;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.Fort;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.AquireSkillList;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;
import org.l2jserver.gameserver.util.Util;

public class VillageMasterInstance extends FolkInstance
{
	/**
	 * Instantiates a new village master instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public VillageMasterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		final String[] commandStr = command.split(" ");
		final String actualCommand = commandStr[0]; // Get actual command
		String cmdParams = "";
		String cmdParams2 = "";
		if (commandStr.length >= 2)
		{
			cmdParams = commandStr[1];
		}
		if (commandStr.length >= 3)
		{
			cmdParams2 = commandStr[2];
		}
		
		if (player.isAio() && !Config.ALLOW_AIO_USE_CM)
		{
			player.sendMessage("Aio Buffers Can't Speak To Village Masters.");
			return;
		}
		
		// Fix exploit stuck subclass and skills
		if (player.isLearningSkill() || player.isLocked())
		{
			return;
		}
		
		if (actualCommand.equalsIgnoreCase("create_clan"))
		{
			if (cmdParams.equals(""))
			{
				return;
			}
			
			ClanTable.getInstance().createClan(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("create_academy"))
		{
			if (cmdParams.equals(""))
			{
				return;
			}
			
			createSubPledge(player, cmdParams, null, Clan.SUBUNIT_ACADEMY, 5);
		}
		else if (actualCommand.equalsIgnoreCase("rename_pledge"))
		{
			if (cmdParams.isEmpty() || cmdParams2.isEmpty())
			{
				return;
			}
			
			renameSubPledge(player, Integer.parseInt(cmdParams), cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("create_royal"))
		{
			if (cmdParams.equals(""))
			{
				return;
			}
			
			createSubPledge(player, cmdParams, cmdParams2, Clan.SUBUNIT_ROYAL1, 6);
		}
		else if (actualCommand.equalsIgnoreCase("create_knight"))
		{
			if (cmdParams.equals(""))
			{
				return;
			}
			
			createSubPledge(player, cmdParams, cmdParams2, Clan.SUBUNIT_KNIGHT1, 7);
		}
		else if (actualCommand.equalsIgnoreCase("assign_subpl_leader"))
		{
			if (cmdParams.equals(""))
			{
				return;
			}
			
			assignSubPledgeLeader(player, cmdParams, cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("create_ally"))
		{
			if (cmdParams.equals(""))
			{
				return;
			}
			
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
				return;
			}
			player.getClan().createAlly(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_ALLIANCE_LEADERS);
				return;
			}
			player.getClan().dissolveAlly(player);
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
		{
			dissolveClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("change_clan_leader"))
		{
			if (cmdParams.equals(""))
			{
				return;
			}
			
			changeClanLeader(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("recover_clan"))
		{
			recoverClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			player.getClan().levelUpClan(player);
		}
		else if (actualCommand.equalsIgnoreCase("learn_clan_skills"))
		{
			showPledgeSkillList(player);
		}
		else if (command.startsWith("Subclass"))
		{
			final int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
			
			// Subclasses may not be changed while a skill is in use.
			if (player.isCastingNow() || player.isAllSkillsDisabled())
			{
				player.sendPacket(SystemMessageId.SUB_CLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
				return;
			}
			
			if (player.isInCombat())
			{
				player.sendMessage("You can't change Subclass when you are in combact.");
				return;
			}
			
			if (player.isCursedWeaponEquiped())
			{
				player.sendMessage("You can`t change Subclass while Cursed weapon equiped!");
				return;
			}
			
			final StringBuilder content = new StringBuilder("<html><body>");
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			Set<PlayerClass> subsAvailable;
			int paramOne = 0;
			int paramTwo = 0;
			
			try
			{
				int endIndex = command.length();
				if (command.length() > 13)
				{
					endIndex = 13;
					paramTwo = Integer.parseInt(command.substring(13).trim());
				}
				
				if (endIndex > 11)
				{
					paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
				}
			}
			catch (Exception NumberFormatException)
			{
			}
			
			switch (cmdChoice)
			{
				case 1: // Add Subclass - Initial
				{
					// Avoid giving player an option to add a new sub class, if they have three already.
					if (player.getTotalSubClasses() == Config.ALLOWED_SUBCLASS)
					{
						player.sendMessage("You can now only change one of your current sub classes.");
						return;
					}
					subsAvailable = getAvailableSubClasses(player);
					if ((subsAvailable != null) && !subsAvailable.isEmpty())
					{
						content.append("Add Subclass:<br>Which sub class do you wish to add?<br>");
						for (PlayerClass subClass : subsAvailable)
						{
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 " + subClass.ordinal() + "\" msg=\"1268;" + formatClassForDisplay(subClass) + "\">" + formatClassForDisplay(subClass) + "</a><br>");
						}
					}
					else
					{
						player.sendMessage("There are no sub classes available at this time.");
						return;
					}
					break;
				}
				case 2: // Change Class - Initial
				{
					content.append("Change Subclass:<br>");
					final int baseClassId = player.getBaseClass();
					if (player.getSubClasses().isEmpty())
					{
						content.append("You can't change sub classes when you don't have a sub class to begin with.<br><a action=\"bypass -h npc_" + getObjectId() + "_Subclass 1\">Add subclass.</a>");
					}
					else
					{
						content.append("Which class would you like to switch to?<br>");
						if (baseClassId == player.getActiveClass())
						{
							content.append(PlayerTemplateData.getInstance().getClassNameById(baseClassId) + "&nbsp;<font color=\"LEVEL\">(Base Class)</font><br><br>");
						}
						else
						{
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" + PlayerTemplateData.getInstance().getClassNameById(baseClassId) + "</a>&nbsp;<font color=\"LEVEL\">(Base Class)</font><br><br>");
						}
						for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
						{
							final SubClass subClass = subList.next();
							final int subClassId = subClass.getClassId();
							if (subClassId == player.getActiveClass())
							{
								content.append(PlayerTemplateData.getInstance().getClassNameById(subClassId) + "<br>");
							}
							else
							{
								content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClass.getClassIndex() + "\">" + PlayerTemplateData.getInstance().getClassNameById(subClassId) + "</a><br>");
							}
						}
					}
					break;
				}
				case 3: // Change/Cancel Subclass - Initial
				{
					content.append("Change Subclass:<br>Which of the following sub classes would you like to change?<br>");
					int classIndex = 1;
					for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
					{
						final SubClass subClass = subList.next();
						content.append("Sub-class " + classIndex + "<br1>");
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + subClass.getClassIndex() + "\">" + PlayerTemplateData.getInstance().getClassNameById(subClass.getClassId()) + "</a><br>");
						classIndex++;
					}
					content.append("<br>If you change a sub class, you'll start at level 40 after the 2nd class transfer.");
					break;
				}
				case 4: // Add Subclass - Action (Subclass 4 x[x])
				{
					// If the character is less than level 75 on any of their previously chosen classes then disallow them to change to their most recently added sub-class choice.
					// Fix exploit stuck subclass and skills
					if (player.isLearningSkill() || player.isLocked())
					{
						return;
					}
					player.setLocked(true);
					boolean allowAddition = true;
					// Subclass exploit fix during add subclass
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("add subclass"))
					{
						LOGGER.warning("Player " + player.getName() + " has performed a subclass change too fast");
						player.setLocked(false);
						return;
					}
					// You can't add Subclass when you are registered in Events (TVT, CTF, DM)
					if (player._inEventTvT || player._inEventCTF || player._inEventDM)
					{
						player.sendMessage("You can't add a subclass while in an event.");
						player.setLocked(false);
						return;
					}
					// Check player level
					if (player.getLevel() < 75)
					{
						player.sendMessage("You may not add a new sub class before you are level 75 on your previous class.");
						allowAddition = false;
					}
					// You can't add Subclass when you are registered in Olympiad
					if (Olympiad.getInstance().isRegisteredInComp(player) || (player.getOlympiadGameId() > 0))
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT);
						player.setLocked(false);
						return;
					}
					if (allowAddition && !player.getSubClasses().isEmpty())
					{
						for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
						{
							final SubClass subClass = subList.next();
							if (subClass.getLevel() < 75)
							{
								player.sendMessage("You may not add a new sub class before you are level 75 on your previous sub class.");
								allowAddition = false;
								break;
							}
						}
					}
					// If quest checking is enabled, verify if the character has completed the Mimir's Elixir (Path to Subclass) and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items.
					// If they both exist, remove both unique items and continue with adding the sub-class.
					if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
					{
						QuestState qs = player.getQuestState("Q235_MimirsElixir");
						if ((qs == null) || !qs.isCompleted())
						{
							player.sendMessage("You must have completed the Mimir's Elixir quest to continue adding your sub class.");
							player.setLocked(false);
							return;
						}
						qs = player.getQuestState("Q234_FatesWhisper");
						if ((qs == null) || !qs.isCompleted())
						{
							player.sendMessage("You must have completed the Fate's Whisper quest to continue adding your sub class.");
							player.setLocked(false);
							return;
						}
					}
					if (allowAddition)
					{
						final String className = PlayerTemplateData.getInstance().getClassNameById(paramOne);
						if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1))
						{
							player.sendMessage("The sub class could not be added.");
							player.setLocked(false);
							return;
						}
						player.setActiveClass(player.getTotalSubClasses());
						// Check player skills
						if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
						{
							player.checkAllowedSkills();
						}
						content.append("Add Subclass:<br>The sub class of <font color=\"LEVEL\">" + className + "</font> has been added.");
						player.sendPacket(SystemMessageId.CONGRATULATIONS_YOU_VE_COMPLETED_A_CLASS_TRANSFER); // Transfer to new class.
					}
					else
					{
						html.setFile("data/html/villagemaster/SubClass_Fail.htm");
					}
					player.setLocked(false);
					break;
				}
				case 5: // Change Class - Action
				{
					// If the character is less than level 75 on any of their previously chosen classes then disallow them to change to their most recently added sub-class choice. Note: paramOne = classIndex
					// Fix exploit stuck subclass and skills
					if (player.isLearningSkill() || player.isLocked())
					{
						return;
					}
					player.setLocked(true);
					// Subclass exploit fix during change subclass
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change subclass"))
					{
						LOGGER.warning("Player " + player.getName() + " has performed a subclass change too fast");
						player.setLocked(false);
						return;
					}
					// You can't change Subclass when you are registered in Events (TVT, CTF, DM)
					if (player._inEventTvT || player._inEventCTF || player._inEventDM)
					{
						player.sendMessage("You can't change subclass while in an event.");
						player.setLocked(false);
						return;
					}
					// You can't change Subclass when you are registered in Olympiad
					if (Olympiad.getInstance().isRegisteredInComp(player) || (player.getOlympiadGameId() > 0))
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT);
						player.setLocked(false);
						return;
					}
					player.setActiveClass(paramOne);
					content.append("Change Subclass:<br>Your active sub class is now a <font color=\"LEVEL\">" + PlayerTemplateData.getInstance().getClassNameById(player.getActiveClass()) + "</font>.");
					player.sendPacket(SystemMessageId.THE_TRANSFER_OF_SUB_CLASS_HAS_BEEN_COMPLETED); // Transfer completed.
					// check player skills
					// Player skills are already checked during setActiveClass
					player.setLocked(false);
					break;
				}
				case 6: // Change/Cancel Subclass - Choice
				{
					content.append("Please choose a sub class to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br><font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");
					subsAvailable = getAvailableSubClasses(player);
					if ((subsAvailable != null) && !subsAvailable.isEmpty())
					{
						for (PlayerClass subClass : subsAvailable)
						{
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 7 " + paramOne + " " + subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
						}
					}
					else
					{
						player.sendMessage("There are no sub classes available at this time.");
						return;
					}
					break;
				}
				case 7: // Cancel/Change Subclass - Action
				{
					// Warning: the information about this subclass will be removed from the subclass list even if false!
					// Fix exploit stuck subclass and skills
					if (player.isLearningSkill() || player.isLocked())
					{
						return;
					}
					player.setLocked(true);
					// Subclass exploit fix during delete subclass
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("delete subclass"))
					{
						LOGGER.warning("Player " + player.getName() + " has performed a subclass change too fast");
						player.setLocked(false);
						return;
					}
					// You can't delete Subclass when you are registered in Events (TVT, CTF, DM)
					if (player._inEventTvT || player._inEventCTF || player._inEventDM)
					{
						player.sendMessage("You can't delete a subclass while in an event.");
						player.setLocked(false);
						return;
					}
					// You can't delete Subclass when you are registered in Olympiad
					if (Olympiad.getInstance().isRegisteredInComp(player) || (player.getOlympiadGameId() > 0))
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT);
						player.setLocked(false);
						return;
					}
					if (player.modifySubClass(paramOne, paramTwo))
					{
						player.setActiveClass(paramOne);
						content.append("Change Subclass:<br>Your sub class has been changed to <font color=\"LEVEL\">" + PlayerTemplateData.getInstance().getClassNameById(paramTwo) + "</font>.");
						player.sendPacket(SystemMessageId.THE_NEW_SUB_CLASS_HAS_BEEN_ADDED); // Subclass added.
						// Player skills are already checked during setActiveClass
					}
					else
					{
						// This isn't good! modifySubClass() removed subclass from memory we must update _classIndex! Else IndexOutOfBoundsException can turn up some place down the line along with other seemingly unrelated problems.
						player.setActiveClass(0); // Also updates _classIndex plus switching _classid to baseclass.
						// Check player skills
						if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
						{
							player.checkAllowedSkills();
						}
						player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
						player.setLocked(false);
						return;
					}
					player.setLocked(false);
					break;
				}
			}
			content.append("</body></html>");
			
			// If the content is greater than for a basic blank page, then assume no external HTML file was assigned.
			if (content.length() > 26)
			{
				html.setHtml(content.toString());
			}
			
			player.sendPacket(html);
		}
		else
		{
			// this class dont know any other commands, let forward the command to the parent class
			super.onBypassFeedback(player, command);
		}
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
		return "data/html/villagemaster/" + pom + ".htm";
	}
	
	/**
	 * Dissolve clan.
	 * @param player the player
	 * @param clanId the clan id
	 */
	public void dissolveClan(PlayerInstance player, int clanId)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Clan clan = player.getClan();
		if (clan.getAllyId() != 0)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE);
			return;
		}
		
		if (clan.isAtWar() != 0)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR);
			return;
		}
		
		if ((clan.getHasCastle() != 0) || (clan.getHasHideout() != 0) || (clan.getHasFort() != 0))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_OWNING_A_CLAN_HALL_OR_CASTLE);
			return;
		}
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId()))
			{
				player.sendPacket(SystemMessageId.UNABLE_TO_DISPERSE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE);
				return;
			}
		}
		
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (FortSiegeManager.getInstance().checkIsRegistered(clan, fort.getFortId()))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE);
				return;
			}
		}
		
		if (player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE);
			return;
		}
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
			return;
		}
		
		clan.setDissolvingExpiryTime(System.currentTimeMillis() + (Config.ALT_CLAN_DISSOLVE_DAYS * 86400000)); // 24*60*60*1000 = 86400000
		clan.updateClanInDB();
		
		ClanTable.getInstance().scheduleRemoveClan(clan.getClanId());
		
		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false);
	}
	
	/**
	 * Recover clan.
	 * @param player the player
	 * @param clanId the clan id
	 */
	public void recoverClan(PlayerInstance player, int clanId)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		final Clan clan = player.getClan();
		clan.setDissolvingExpiryTime(0);
		clan.updateClanInDB();
	}
	
	/**
	 * Change clan leader.
	 * @param player the player
	 * @param target the target
	 */
	public void changeClanLeader(PlayerInstance player, String target)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (player.isFlying())
		{
			player.sendMessage("Get off the Wyvern first.");
			return;
		}
		
		if (player.isMounted())
		{
			player.sendMessage("Get off the Pet first.");
			return;
		}
		
		if (player.getName().equalsIgnoreCase(target))
		{
			return;
		}
		
		final Clan clan = player.getClan();
		final ClanMember member = clan.getClanMember(target);
		if (member == null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOES_NOT_EXIST);
			sm.addString(target);
			player.sendPacket(sm);
			return;
		}
		
		if (!member.isOnline())
		{
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
			return;
		}
		
		if (SiegeManager.getInstance().checkIsRegisteredInSiege(clan) || FortSiegeManager.getInstance().checkIsRegisteredInSiege(clan))
		{
			player.sendMessage("Cannot change clan leader while registered in Siege");
			return;
		}
		
		// Set old name/title color for old clan leader
		if (Config.CLAN_LEADER_COLOR_ENABLED && (clan.getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL))
		{
			if (Config.CLAN_LEADER_COLORED == 1)
			{
				player.getAppearance().setNameColor(0x000000);
			}
			else
			{
				player.getAppearance().setTitleColor(0xFFFF77);
			}
		}
		
		clan.setNewLeader(member, player);
	}
	
	/**
	 * Creates the sub pledge.
	 * @param player the player
	 * @param clanName the clan name
	 * @param leaderName the leader name
	 * @param pledgeType the pledge type
	 * @param minClanLvl the min clan lvl
	 */
	public void createSubPledge(PlayerInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Clan clan = player.getClan();
		if (clan.getLevel() < minClanLvl)
		{
			if (pledgeType == Clan.SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.TO_ESTABLISH_A_CLAN_ACADEMY_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET);
			}
			return;
		}
		
		if (!Util.isAlphaNumeric(clanName) || (2 > clanName.length()))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
			return;
		}
		
		if (clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_S_LENGTH_IS_INCORRECT);
			return;
		}
		for (Clan tempClan : ClanTable.getInstance().getClans())
		{
			if (tempClan.getSubPledge(clanName) != null)
			{
				if (pledgeType == Clan.SUBUNIT_ACADEMY)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
					sm.addString(clanName);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
				}
				return;
			}
		}
		
		if ((pledgeType != Clan.SUBUNIT_ACADEMY) && ((clan.getClanMember(leaderName) == null) || (clan.getClanMember(leaderName).getPledgeType() != 0)))
		{
			if (pledgeType >= Clan.SUBUNIT_KNIGHT1)
			{
				player.sendPacket(SystemMessageId.THE_CAPTAIN_OF_THE_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
			}
			else if (pledgeType >= Clan.SUBUNIT_ROYAL1)
			{
				player.sendPacket(SystemMessageId.THE_CAPTAIN_OF_THE_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			}
			return;
		}
		
		if (clan.createSubPledge(player, pledgeType, leaderName, clanName) == null)
		{
			return;
		}
		
		SystemMessage sm;
		if (pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			sm = new SystemMessage(SystemMessageId.CONGRATULATIONS_THE_S1_S_CLAN_ACADEMY_HAS_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if (pledgeType >= Clan.SUBUNIT_KNIGHT1)
		{
			sm = new SystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if (pledgeType >= Clan.SUBUNIT_ROYAL1)
		{
			sm = new SystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.YOUR_CLAN_HAS_BEEN_CREATED);
		}
		
		player.sendPacket(sm);
		if (pledgeType != Clan.SUBUNIT_ACADEMY)
		{
			final ClanMember leaderSubPledge = clan.getClanMember(leaderName);
			if (leaderSubPledge.getPlayerInstance() == null)
			{
				return;
			}
			
			leaderSubPledge.getPlayerInstance().setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderSubPledge.getPlayerInstance()));
			leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
		}
	}
	
	/**
	 * Rename sub pledge.
	 * @param player the player
	 * @param pledgeType the pledge type
	 * @param pledgeName the pledge name
	 */
	private void renameSubPledge(PlayerInstance player, int pledgeType, String pledgeName)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Clan clan = player.getClan();
		final SubPledge subPledge = player.getClan().getSubPledge(pledgeType);
		if (subPledge == null)
		{
			player.sendMessage("Pledge don't exists.");
			return;
		}
		if (!Util.isAlphaNumeric(pledgeName) || (2 > pledgeName.length()))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
			return;
		}
		if (pledgeName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_S_LENGTH_IS_INCORRECT);
			return;
		}
		
		subPledge.setName(pledgeName);
		clan.updateSubPledgeInDB(subPledge.getId());
		clan.broadcastClanStatus();
		player.sendMessage("Pledge name changed.");
	}
	
	/**
	 * Assign sub pledge leader.
	 * @param player the player
	 * @param clanName the clan name
	 * @param leaderName the leader name
	 */
	public void assignSubPledgeLeader(PlayerInstance player, String clanName, String leaderName)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (leaderName.length() > 16)
		{
			player.sendPacket(SystemMessageId.YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGTH_PLEASE_TRY_AGAIN);
			return;
		}
		
		if (player.getName().equals(leaderName))
		{
			player.sendPacket(SystemMessageId.THE_CAPTAIN_OF_THE_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			return;
		}
		
		final Clan clan = player.getClan();
		final SubPledge subPledge = player.getClan().getSubPledge(clanName);
		if (null == subPledge)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
			return;
		}
		
		if (subPledge.getId() == Clan.SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
			return;
		}
		
		if ((clan.getClanMember(leaderName) == null) || (clan.getClanMember(leaderName).getPledgeType() != 0))
		{
			if (subPledge.getId() >= Clan.SUBUNIT_KNIGHT1)
			{
				player.sendPacket(SystemMessageId.THE_CAPTAIN_OF_THE_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
			}
			else if (subPledge.getId() >= Clan.SUBUNIT_ROYAL1)
			{
				player.sendPacket(SystemMessageId.THE_CAPTAIN_OF_THE_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			}
			return;
		}
		
		subPledge.setLeaderName(leaderName);
		clan.updateSubPledgeInDB(subPledge.getId());
		final ClanMember leaderSubPledge = clan.getClanMember(leaderName);
		if (leaderSubPledge.getPlayerInstance() != null)
		{
			leaderSubPledge.getPlayerInstance().setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderSubPledge.getPlayerInstance()));
			leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
			clan.broadcastClanStatus();
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_SELECTED_AS_THE_CAPTAIN_OF_S2);
			sm.addString(leaderName);
			sm.addString(clanName);
			clan.broadcastToOnlineMembers(sm);
		}
	}
	
	/**
	 * Gets the available sub classes.
	 * @param player the player
	 * @return the available sub classes
	 */
	private final Set<PlayerClass> getAvailableSubClasses(PlayerInstance player)
	{
		int charClassId = player.getBaseClass();
		if (charClassId >= 88)
		{
			charClassId = player.getClassId().getParent().ordinal();
		}
		
		final Race npcRace = getVillageMasterRace();
		final ClassType npcTeachType = getVillageMasterTeachType();
		final PlayerClass currClass = PlayerClass.values()[charClassId];
		
		/**
		 * If the race of your main class is Elf or Dark Elf, you may not select each class as a subclass to the other class, and you may not select Overlord and Warsmith class as a subclass. You may not select a similar class as the subclass. The occupations classified as similar classes are as
		 * follows: Treasure Hunter, Plainswalker and Abyss Walker Hawkeye, Silver Ranger and Phantom Ranger Paladin, Dark Avenger, Temple Knight and Shillien Knight Warlocks, Elemental Summoner and Phantom Summoner Elder and Shillien Elder Swordsinger and Bladedancer Sorcerer, Spellsinger and
		 * Spellhowler
		 */
		final Set<PlayerClass> availSubs = currClass.getAvailableSubclasses(player);
		if (availSubs != null)
		{
			for (PlayerClass availSub : availSubs)
			{
				for (SubClass subClass : player.getSubClasses().values())
				{
					if (subClass.getClassId() == availSub.ordinal())
					{
						availSubs.remove(availSub);
					}
				}
				for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
				{
					final SubClass prevSubClass = subList.next();
					int subClassId = prevSubClass.getClassId();
					if (subClassId >= 88)
					{
						subClassId = ClassId.getClassId(subClassId).getParent().getId();
					}
					
					if ((availSub.ordinal() == subClassId) || (availSub.ordinal() == player.getBaseClass()))
					{
						availSubs.remove(PlayerClass.values()[availSub.ordinal()]);
					}
				}
				
				if ((npcRace == Race.HUMAN) || (npcRace == Race.ELF))
				{
					// If the master is human or light elf, ensure that fighter-type masters only teach fighter classes, and priest-type masters only teach priest classes etc.
					if (!availSub.isOfType(npcTeachType))
					{
						availSubs.remove(availSub);
					}
					else if (!availSub.isOfRace(Race.HUMAN) && !availSub.isOfRace(Race.ELF))
					{
						availSubs.remove(availSub);
					}
				}
				else if ((npcRace != Race.HUMAN) && (npcRace != Race.ELF) && !availSub.isOfRace(npcRace)) // If the master is not human and not light elf, then remove any classes not of the same race as the master.
				{
					availSubs.remove(availSub);
				}
			}
		}
		return availSubs;
	}
	
	/**
	 * this displays PledgeSkillList to the player.
	 * @param player the player
	 */
	public void showPledgeSkillList(PlayerInstance player)
	{
		if (player.getClan() == null)
		{
			return;
		}
		
		final PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);
		final AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Clan);
		int counts = 0;
		for (PledgeSkillLearn s : skills)
		{
			final int cost = s.getRepCost();
			counts++;
			
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}
		
		if (counts == 0)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(1);
			if (player.getClan().getLevel() < 8)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addNumber(player.getClan().getLevel() + 1);
				player.sendPacket(sm);
			}
			else
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<html><body>");
				sb.append("You've learned all skills available for your Clan.<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Format class for display.
	 * @param className the class name
	 * @return the string
	 */
	private final String formatClassForDisplay(PlayerClass className)
	{
		String classNameStr = className.toString();
		final char[] charArray = classNameStr.toCharArray();
		for (int i = 1; i < charArray.length; i++)
		{
			if (Character.isUpperCase(charArray[i]))
			{
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);
			}
		}
		return classNameStr;
	}
	
	/**
	 * Gets the village master race.
	 * @return the village master race
	 */
	private final Race getVillageMasterRace()
	{
		final String npcClass = getTemplate().getStatSet().getString("jClass").toLowerCase();
		if (npcClass.contains("human"))
		{
			return Race.HUMAN;
		}
		
		if (npcClass.contains("darkelf"))
		{
			return Race.DARK_ELF;
		}
		
		if (npcClass.contains("elf"))
		{
			return Race.ELF;
		}
		
		if (npcClass.contains("orc"))
		{
			return Race.ORC;
		}
		
		return Race.DWARF;
	}
	
	/**
	 * Gets the village master teach type.
	 * @return the village master teach type
	 */
	private final ClassType getVillageMasterTeachType()
	{
		final String npcClass = getTemplate().getStatSet().getString("jClass");
		if (npcClass.contains("sanctuary") || npcClass.contains("clergyman"))
		{
			return ClassType.Priest;
		}
		
		if (npcClass.contains("mageguild") || npcClass.contains("patriarch"))
		{
			return ClassType.Mystic;
		}
		
		return ClassType.Fighter;
	}
	
	/**
	 * Iter sub classes.
	 * @param player the player
	 * @return the iterator
	 */
	private Iterator<SubClass> iterSubClasses(PlayerInstance player)
	{
		return player.getSubClasses().values().iterator();
	}
}
