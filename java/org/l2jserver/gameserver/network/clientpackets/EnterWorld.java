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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.l2jserver.Config;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.communitybbs.Manager.MailBBSManager;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.xml.AdminData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.ClanHallManager;
import org.l2jserver.gameserver.instancemanager.CoupleManager;
import org.l2jserver.gameserver.instancemanager.CrownManager;
import org.l2jserver.gameserver.instancemanager.DimensionalRiftManager;
import org.l2jserver.gameserver.instancemanager.FortSiegeManager;
import org.l2jserver.gameserver.instancemanager.PetitionManager;
import org.l2jserver.gameserver.instancemanager.SiegeManager;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Inventory;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.ClassMasterInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.Announcements;
import org.l2jserver.gameserver.model.entity.ClanHall;
import org.l2jserver.gameserver.model.entity.Hero;
import org.l2jserver.gameserver.model.entity.Rebirth;
import org.l2jserver.gameserver.model.entity.Wedding;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.GameEvent;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.entity.siege.FortSiege;
import org.l2jserver.gameserver.model.entity.siege.Siege;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.GameClient.GameClientState;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ClientSetTime;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.Die;
import org.l2jserver.gameserver.network.serverpackets.Earthquake;
import org.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.ExMailArrived;
import org.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jserver.gameserver.network.serverpackets.ExStorageMaxCount;
import org.l2jserver.gameserver.network.serverpackets.FriendList;
import org.l2jserver.gameserver.network.serverpackets.HennaInfo;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jserver.gameserver.network.serverpackets.PledgeSkillList;
import org.l2jserver.gameserver.network.serverpackets.PledgeStatusChanged;
import org.l2jserver.gameserver.network.serverpackets.QuestList;
import org.l2jserver.gameserver.network.serverpackets.ShortCutInit;
import org.l2jserver.gameserver.network.serverpackets.SignsSky;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;
import org.l2jserver.gameserver.util.BuilderUtil;
import org.l2jserver.gameserver.util.Util;

/**
 * Enter World Packet Handler
 */
public class EnterWorld extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(EnterWorld.class.getName());
	
	private final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");
	
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			LOGGER.warning("EnterWorld failed! player is null...");
			getClient().closeNow();
			return;
		}
		
		getClient().setState(GameClientState.IN_GAME);
		
		// Set lock at login
		player.setLocked(true);
		
		// Register in flood protector
		// FloodProtector.getInstance().registerNewPlayer(player.getObjectId());
		if (!player.isGM() && !player.isDonator() && Config.CHECK_NAME_ON_LOGIN && ((player.getName().length() < 3) || (player.getName().length() > 16) || !Util.isAlphaNumeric(player.getName()) || !isValidName(player.getName())))
		{
			LOGGER.warning("Charname: " + player.getName() + " is invalid. EnterWorld failed.");
			getClient().closeNow();
			return;
		}
		
		// Set online status
		player.setOnlineStatus(true);
		
		player.setRunning(); // running is default
		player.standUp(); // standing is default
		player.broadcastKarma(); // include UserInfo
		
		// Engage and notify Partner
		if (Config.ALLOW_WEDDING)
		{
			engage(player);
			notifyPartner(player);
		}
		
		enterGM(player);
		
		Quest.playerEnter(player);
		player.sendPacket(new QuestList(player));
		if (Config.ENABLE_COMMUNITY_BOARD)
		{
			// Unread mails make a popup appears.
			if (MailBBSManager.getInstance().checkUnreadMail(player) > 0)
			{
				player.sendPacket(SystemMessageId.YOU_VE_GOT_MAIL);
				player.sendPacket(new PlaySound("systemmsg_e.1233"));
				player.sendPacket(ExMailArrived.STATIC_PACKET);
			}
			
			// Clan notice, if active.
			if ((player.getClan() != null) && player.getClan().isNoticeEnabled())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/clan_notice.htm");
				html.replace("%clan_name%", player.getClan().getName());
				html.replace("%notice_text%", player.getClan().getNotice().replaceAll("\r\n", "<br>").replaceAll("action", "").replace("bypass", ""));
				sendPacket(html);
			}
		}
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			player.setProtection(true);
		}
		
		player.spawnMe(player.getX(), player.getY(), player.getZ());
		// Mobius: Set player heading.
		// player.sendPacket(new StopRotation(player, player.getHeading(), 10000000));
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			sendPacket(new SignsSky());
		}
		
		// Buff and Status icons
		if (Config.STORE_SKILL_COOLTIME)
		{
			player.restoreEffects();
		}
		
		player.sendPacket(new EtcStatusUpdate(player));
		
		final Effect[] effects = player.getAllEffects();
		if (effects != null)
		{
			for (Effect e : effects)
			{
				if (e.getEffectType() == Effect.EffectType.HEAL_OVER_TIME)
				{
					player.stopEffects(Effect.EffectType.HEAL_OVER_TIME);
					player.removeEffect(e);
				}
				if (e.getEffectType() == Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					player.stopEffects(Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
					player.removeEffect(e);
				}
			}
		}
		
		// Apply augmentation boni for equipped items
		for (ItemInstance temp : player.getInventory().getAugmentedItems())
		{
			if ((temp != null) && temp.isEquipped())
			{
				temp.getAugmentation().applyBonus(player);
			}
		}
		
		// Remove Demonic Weapon if character is not Cursed Weapon Equipped
		if ((player.getInventory().getItemByItemId(8190) != null) && !player.isCursedWeaponEquipped())
		{
			player.destroyItem("Zariche", player.getInventory().getItemByItemId(8190), null, true);
		}
		if ((player.getInventory().getItemByItemId(8689) != null) && !player.isCursedWeaponEquipped())
		{
			player.destroyItem("Akamanah", player.getInventory().getItemByItemId(8689), null, true);
		}
		
		// Apply death penalty
		player.restoreDeathPenaltyBuffLevel();
		
		if (GameEvent.active && GameEvent.connectionLossData.containsKey(player.getName()) && GameEvent.isOnEvent(player))
		{
			GameEvent.restoreChar(player);
		}
		else if (GameEvent.connectionLossData.containsKey(player.getName()))
		{
			GameEvent.restoreAndTeleChar(player);
		}
		
		// SECURE FIX - Anti Overenchant Cheat!!
		if (Config.MAX_ITEM_ENCHANT_KICK > 0)
		{
			for (ItemInstance i : player.getInventory().getItems())
			{
				if (!player.isGM() && i.isEquipable() && (i.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK))
				{
					// Delete Item Over enchanted
					player.getInventory().destroyItem(null, i, player, null);
					// Message to Player
					player.sendMessage("[Server]: You have over enchanted items you will be kicked from server!");
					player.sendMessage("[Server]: Respect our server rules.");
					// Message with screen
					sendPacket(new ExShowScreenMessage(" You have an over enchanted item, you will be kicked from server! ", 6000));
					// Punishment e LOGGER in audit
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has Overenchanted  item! Kicked! ", Config.DEFAULT_PUNISH);
					// Logger in console
					LOGGER.info("#### ATTENTION ####");
					LOGGER.info(i + " item has been removed from " + player);
				}
			}
		}
		
		// Restores custom status
		player.restoreCustomStatus();
		
		ColorSystem(player);
		
		// Expand Skill
		player.sendPacket(new ExStorageMaxCount(player));
		player.getMacroses().sendUpdate();
		
		// Send packets info
		sendPacket(new ClientSetTime()); // SetClientTime
		sendPacket(new UserInfo(player));
		sendPacket(new HennaInfo(player));
		sendPacket(new FriendList(player));
		sendPacket(new ItemList(player, false));
		sendPacket(new ShortCutInit(player));
		
		// Reload inventory to give SA skill
		player.getInventory().reloadEquippedItems();
		
		// Welcome to Lineage II
		player.sendPacket(SystemMessageId.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(player);
		Announcements.getInstance().showAnnouncements(player);
		
		loadTutorial(player);
		
		// Check for crowns
		CrownManager.getInstance().checkCrowns(player);
		
		// Check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
		{
			player.checkAllowedSkills();
		}
		
		PetitionManager.getInstance().checkPetitionMessages(player);
		
		// Send user info again .. just like the real client
		// sendPacket(ui);
		if ((player.getClanId() != 0) && (player.getClan() != null))
		{
			sendPacket(new PledgeShowMemberListAll(player.getClan(), player));
			sendPacket(new PledgeStatusChanged(player.getClan()));
		}
		
		if (player.isAlikeDead())
		{
			sendPacket(new Die(player)); // No broadcast needed since the player will already spawn dead to others
		}
		
		if (Config.ALLOW_WATER)
		{
			player.checkWaterState();
		}
		
		if ((Hero.getInstance().getHeroes() != null) && Hero.getInstance().getHeroes().containsKey(player.getObjectId()))
		{
			player.setHero(true);
		}
		
		setPledgeClass(player);
		
		notifyClanMembers(player);
		notifySponsorOrApprentice(player);
		
		player.setTarget(player);
		
		player.onPlayerEnter();
		
		if (Config.PCB_ENABLE)
		{
			player.showPcBangWindow();
		}
		
		if (Config.ANNOUNCE_CASTLE_LORDS)
		{
			notifyCastleOwner(player);
		}
		
		if (Olympiad.getInstance().playerInStadia(player))
		{
			player.teleToLocation(TeleportWhereType.TOWN);
			player.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium");
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
		}
		
		if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS);
		}
		
		if (player.getClan() != null)
		{
			player.sendPacket(new PledgeSkillList(player.getClan()));
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(player.getClan()))
				{
					player.setSiegeState((byte) 1);
					break;
				}
				else if (siege.checkIsDefender(player.getClan()))
				{
					player.setSiegeState((byte) 2);
					break;
				}
			}
			
			for (FortSiege fortsiege : FortSiegeManager.getInstance().getSieges())
			{
				if (!fortsiege.isInProgress())
				{
					continue;
				}
				
				if (fortsiege.checkIsAttacker(player.getClan()))
				{
					player.setSiegeState((byte) 1);
					break;
				}
				else if (fortsiege.checkIsDefender(player.getClan()))
				{
					player.setSiegeState((byte) 2);
					break;
				}
			}
			
			// Add message at connexion if clanHall not paid. Possibly this is custom...
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
			if ((clanHall != null) && !clanHall.getPaid())
			{
				player.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
			}
		}
		
		if (!player.isGM() && (player.getSiegeState() < 2) && player.isInsideZone(ZoneId.SIEGE))
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			player.teleToLocation(TeleportWhereType.TOWN);
			player.sendMessage("You have been teleported to the nearest town due to you being in siege zone");
		}
		
		// Rebirth's skills must be actived only on main class
		if (Config.REBIRTH_ENABLE && !player.isSubClassActive())
		{
			Rebirth.getInstance().grantRebirthSkills(player);
		}
		
		if (TvT._savePlayers.contains(player.getName()))
		{
			TvT.addDisconnectedPlayer(player);
		}
		
		if (CTF._savePlayers.contains(player.getName()))
		{
			CTF.addDisconnectedPlayer(player);
		}
		
		if (DM._savePlayers.contains(player.getName()))
		{
			DM.addDisconnectedPlayer(player);
		}
		
		// Means that it's not ok multiBox situation, so logout
		if (!player.checkMultiBox())
		{
			player.sendMessage("I'm sorry, but multibox is not allowed here.");
			player.logout();
		}
		
		Hellows(player);
		
		if (Config.ALLOW_CLASS_MASTERS && Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			final ClassMasterInstance master = ClassMasterInstance.getInstance();
			if (master != null)
			{
				final int lvlnow = player.getClassId().level();
				if ((player.getLevel() >= 20) && (lvlnow == 0))
				{
					ClassMasterInstance.getInstance().onAction(player);
				}
				else if ((player.getLevel() >= 40) && (lvlnow == 1))
				{
					ClassMasterInstance.getInstance().onAction(player);
				}
				else if ((player.getLevel() >= 76) && (lvlnow == 2))
				{
					ClassMasterInstance.getInstance().onAction(player);
				}
			}
			else
			{
				LOGGER.info("Attention: Remote ClassMaster is Enabled, but not inserted into DataBase. Remember to install 31288 Custom_Npc...");
			}
		}
		
		// Apply night/day bonus on skill Shadow Sense
		if (player.getRace().ordinal() == 2)
		{
			final Skill skill = SkillTable.getInstance().getInfo(294, 1);
			if ((skill != null) && (player.getSkillLevel(294) == 1))
			{
				if (GameTimeController.getInstance().isNowNight())
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT);
					sm.addSkillName(294);
					sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR);
					sm.addSkillName(294);
					sendPacket(sm);
				}
			}
		}
		
		// Elrokian Trap like L2OFF
		final ItemInstance rhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if ((rhand != null) && (rhand.getItemId() == 8763))
		{
			player.addSkill(SkillTable.getInstance().getInfo(3626, 1));
			player.addSkill(SkillTable.getInstance().getInfo(3627, 1));
			player.addSkill(SkillTable.getInstance().getInfo(3628, 1));
		}
		else
		{
			player.removeSkill(3626, true);
			player.removeSkill(3627, true);
			player.removeSkill(3628, true);
		}
		
		// Send all skills to char
		player.sendSkillList();
		
		// Close lock at login
		player.setLocked(false);
	}
	
	private boolean isValidName(String text)
	{
		boolean result = true;
		final String test = text;
		Pattern pattern;
		
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			LOGGER.warning("ERROR " + getType() + ": Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		
		return result;
	}
	
	private void enterGM(PlayerInstance player)
	{
		if (player.isGM())
		{
			gmStartupProcess:
			{
				if (Config.GM_STARTUP_BUILDER_HIDE && AdminData.getInstance().hasAccess("admin_hide", player.getAccessLevel()))
				{
					player.setInRefusalMode(true);
					player.setInvul(true);
					player.getAppearance().setInvisible();
					
					BuilderUtil.sendSysMessage(player, "hide is default for builder.");
					BuilderUtil.sendSysMessage(player, "FriendAddOff is default for builder.");
					BuilderUtil.sendSysMessage(player, "whisperoff is default for builder.");
					
					// It isn't recommend to use the below custom L2J GMStartup functions together with retail-like GMStartupBuilderHide, so breaking the process at that stage.
					break gmStartupProcess;
				}
				
				if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", player.getAccessLevel()))
				{
					player.setInvul(true);
				}
				
				if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", player.getAccessLevel()))
				{
					player.getAppearance().setInvisible();
				}
				
				if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
				{
					player.setInRefusalMode(true);
				}
				
				if (Config.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmliston", player.getAccessLevel()))
				{
					AdminData.getInstance().addGm(player, false);
				}
				else
				{
					AdminData.getInstance().addGm(player, true);
				}
			}
			
			if (Config.GM_SPECIAL_EFFECT)
			{
				player.broadcastPacket(new Earthquake(player.getX(), player.getY(), player.getZ(), 50, 4));
			}
			
			if (Config.SHOW_GM_LOGIN)
			{
				Announcements.getInstance().announceToAll("GM " + player.getName() + " has logged on.");
			}
		}
	}
	
	private void Hellows(PlayerInstance player)
	{
		if (Config.ALT_SERVER_NAME_ENABLED)
		{
			player.sendMessage("Welcome to " + Config.ALT_Server_Name);
		}
		
		if (Config.ONLINE_PLAYERS_ON_LOGIN)
		{
			player.sendMessage("There are " + World.getInstance().getAllPlayers().size() + " players online.");
		}
		
		if (player.getFirstLog() && Config.NEW_PLAYER_EFFECT)
		{
			final Skill skill = SkillTable.getInstance().getInfo(2025, 1);
			if (skill != null)
			{
				final MagicSkillUse msu = new MagicSkillUse(player, player, 2025, 1, 1, 0);
				player.sendPacket(msu);
				player.broadcastPacket(msu);
				player.useMagic(skill, false, false);
			}
			player.setFirstLog(false);
			player.updateFirstLog();
		}
		
		if (Config.WELCOME_HTM && isValidName(player.getName()))
		{
			final String Welcome_Path = "data/html/welcome.htm";
			final File mainText = new File(Config.DATAPACK_ROOT, Welcome_Path);
			if (mainText.exists())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Welcome_Path);
				html.replace("%name%", player.getName());
				sendPacket(html);
			}
		}
		
		if (Config.PM_MESSAGE_ON_START)
		{
			player.sendPacket(new CreatureSay(2, ChatType.HERO_VOICE, Config.PM_TEXT1, Config.PM_SERVER_NAME));
			player.sendPacket(new CreatureSay(15, ChatType.PARTYROOM_COMMANDER, player.getName(), Config.PM_TEXT2));
		}
		
		if (Config.SERVER_TIME_ON_START)
		{
			player.sendMessage("SVR time is " + fmt.format(new Date(System.currentTimeMillis())));
		}
	}
	
	private void ColorSystem(PlayerInstance player)
	{
		// Color System checks - Start
		// Check if the custom PvP and PK color systems are enabled and if so check the character's counters
		// and apply any color changes that must be done. Thankz Kidzor
		/** KidZor: Ammount 1 **/
		if ((player.getPvpKills() >= Config.PVP_AMOUNT1) && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			player.updatePvPColor(player.getPvpKills());
		}
		if ((player.getPkKills() >= Config.PK_AMOUNT1) && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			player.updatePkColor(player.getPkKills());
		}
		
		/** KidZor: Ammount 2 **/
		if ((player.getPvpKills() >= Config.PVP_AMOUNT2) && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			player.updatePvPColor(player.getPvpKills());
		}
		if ((player.getPkKills() >= Config.PK_AMOUNT2) && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			player.updatePkColor(player.getPkKills());
		}
		
		/** KidZor: Ammount 3 **/
		if ((player.getPvpKills() >= Config.PVP_AMOUNT3) && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			player.updatePvPColor(player.getPvpKills());
		}
		if ((player.getPkKills() >= Config.PK_AMOUNT3) && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			player.updatePkColor(player.getPkKills());
		}
		
		/** KidZor: Ammount 4 **/
		if ((player.getPvpKills() >= Config.PVP_AMOUNT4) && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			player.updatePvPColor(player.getPvpKills());
		}
		if ((player.getPkKills() >= Config.PK_AMOUNT4) && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			player.updatePkColor(player.getPkKills());
		}
		
		/** KidZor: Ammount 5 **/
		if ((player.getPvpKills() >= Config.PVP_AMOUNT5) && Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			player.updatePvPColor(player.getPvpKills());
		}
		if ((player.getPkKills() >= Config.PK_AMOUNT5) && Config.PK_COLOR_SYSTEM_ENABLED)
		{
			player.updatePkColor(player.getPkKills());
			// Color System checks - End
		}
		
		// Apply color settings to clan leader when entering
		if ((player.getClan() != null) && player.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && (player.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL))
		{
			if (Config.CLAN_LEADER_COLORED == 1)
			{
				player.getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
			}
			else
			{
				player.getAppearance().setTitleColor(Config.CLAN_LEADER_COLOR);
			}
		}
		
		if (Config.ALLOW_AIO_NCOLOR && player.isAio())
		{
			player.getAppearance().setNameColor(Config.AIO_NCOLOR);
		}
		
		if (Config.ALLOW_AIO_TCOLOR && player.isAio())
		{
			player.getAppearance().setTitleColor(Config.AIO_TCOLOR);
		}
		
		if (player.isAio())
		{
			onEnterAio(player);
		}
		
		player.updateNameTitleColor();
		
		sendPacket(new UserInfo(player));
		sendPacket(new HennaInfo(player));
		sendPacket(new FriendList(player));
		sendPacket(new ItemList(player, false));
		sendPacket(new ShortCutInit(player));
		player.broadcastUserInfo();
		player.sendPacket(new EtcStatusUpdate(player));
	}
	
	private void onEnterAio(PlayerInstance player)
	{
		final long now = Calendar.getInstance().getTimeInMillis();
		final long endDay = player.getAioEndTime();
		if (now > endDay)
		{
			player.setAio(false);
			player.setAioEndTime(0);
			player.lostAioSkills();
			player.sendMessage("[Aio System]: Removed your Aio stats... period ends.");
		}
		else
		{
			final Date dt = new Date(endDay);
			final long daysleft = (endDay - now) / 86400000;
			if (daysleft > 30)
			{
				player.sendMessage("[Aio System]: Aio period ends in " + df.format(dt) + ". enjoy the Game.");
			}
			else if (daysleft > 0)
			{
				player.sendMessage("[Aio System]: Left " + (int) daysleft + " for Aio period ends.");
			}
			else if (daysleft < 1)
			{
				final long hour = (endDay - now) / 3600000;
				player.sendMessage("[Aio System]: Left " + (int) hour + " hours to Aio period ends.");
			}
		}
	}
	
	private void engage(PlayerInstance player)
	{
		final int _chaid = player.getObjectId();
		for (Wedding cl : CoupleManager.getInstance().getCouples())
		{
			if ((cl.getPlayer1Id() == _chaid) || (cl.getPlayer2Id() == _chaid))
			{
				if (cl.getMaried())
				{
					player.setMarried(true);
					player.setmarriedType(cl.getType());
				}
				
				player.setCoupleId(cl.getId());
				
				if (cl.getPlayer1Id() == _chaid)
				{
					player.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					player.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}
	
	private void notifyPartner(PlayerInstance player)
	{
		if (player.getPartnerId() != 0)
		{
			PlayerInstance partner = null;
			if (World.getInstance().findObject(player.getPartnerId()) instanceof PlayerInstance)
			{
				partner = (PlayerInstance) World.getInstance().findObject(player.getPartnerId());
			}
			
			if (partner != null)
			{
				partner.sendMessage("Your partner has logged in");
			}
		}
	}
	
	private void notifyClanMembers(PlayerInstance player)
	{
		final Clan clan = player.getClan();
		if (clan != null)
		{
			clan.getClanMember(player.getObjectId()).setPlayerInstance(player);
			clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addString(player.getName()), player);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(player), player);
		}
	}
	
	private void notifySponsorOrApprentice(PlayerInstance player)
	{
		if (player.getSponsor() != 0)
		{
			final PlayerInstance sponsor = (PlayerInstance) World.getInstance().findObject(player.getSponsor());
			if (sponsor != null)
			{
				sponsor.sendPacket(new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addString(player.getName()));
			}
		}
		else if (player.getApprentice() != 0)
		{
			final PlayerInstance apprentice = (PlayerInstance) World.getInstance().findObject(player.getApprentice());
			if (apprentice != null)
			{
				apprentice.sendPacket(new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addString(player.getName()));
			}
		}
	}
	
	private void loadTutorial(PlayerInstance player)
	{
		final QuestState qs = player.getQuestState("Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}
	
	private void setPledgeClass(PlayerInstance player)
	{
		int pledgeClass = 0;
		if (player.getClan() != null)
		{
			pledgeClass = player.getClan().getClanMember(player.getObjectId()).calculatePledgeClass(player);
		}
		
		if (player.isNoble() && (pledgeClass < 5))
		{
			pledgeClass = 5;
		}
		
		if (player.isHero())
		{
			pledgeClass = 8;
		}
		
		player.setPledgeClass(pledgeClass);
	}
	
	private void notifyCastleOwner(PlayerInstance player)
	{
		final Clan clan = player.getClan();
		if ((clan != null) && (clan.getHasCastle() > 0))
		{
			final Castle castle = CastleManager.getInstance().getCastleById(clan.getHasCastle());
			if ((castle != null) && (player.getObjectId() == clan.getLeaderId()))
			{
				Announcements.getInstance().announceToAll("Lord " + player.getName() + " Ruler Of " + castle.getName() + " Castle is now Online!");
			}
		}
	}
}