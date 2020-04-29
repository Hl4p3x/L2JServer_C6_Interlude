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
package org.l2jserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jserver.commons.enums.ServerMode;
import org.l2jserver.commons.util.ClassMasterSettings;
import org.l2jserver.commons.util.PropertiesParser;
import org.l2jserver.commons.util.StringUtil;
import org.l2jserver.gameserver.model.entity.olympiad.OlympiadPeriod;
import org.l2jserver.gameserver.util.FloodProtectorConfig;
import org.l2jserver.loginserver.LoginController;

public class Config
{
	private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
	
	// --------------------------------------------------
	// Files
	// --------------------------------------------------
	
	// standard
	private static final String FILTER_FILE = "./config/chatfilter.txt";
	private static final String HEXID_FILE = "./config/hexid.txt";
	// main
	private static final String ACCESS_CONFIG_FILE = "./config/main/Access.ini";
	private static final String CHARACTER_CONFIG_FILE = "./config/main/Character.ini";
	private static final String CLANHALL_CONFIG_FILE = "./config/main/Clanhall.ini";
	public static final String CLASS_DAMAGE_CONFIG_FILE = "./config/main/ClassDamage.ini";
	private static final String CONQUERABLE_CLANHALL_CONFIG_FILE = "./config/main/ConquerableClanHalls.ini";
	private static final String CRAFTING_CONFIG_FILE = "./config/main/Crafting.ini";
	private static final String ENCHANT_CONFIG_FILE = "./config/main/Enchant.ini";
	public static final String FORTSIEGE_CONFIG_FILE = "./config/main/Fort.ini";
	private static final String GENERAL_CONFIG_FILE = "./config/main/General.ini";
	private static final String GEOENGINE_CONFIG_FILE = "./config/main/GeoEngine.ini";
	private static final String OLYMP_CONFIG_FILE = "./config/main/Olympiad.ini";
	private static final String PHYSICS_CONFIG_FILE = "./config/main/Physics.ini";
	private static final String PVP_CONFIG_FILE = "./config/main/PvP.ini";
	private static final String RAIDBOSS_CONFIG_FILE = "./config/main/RaidBoss.ini";
	private static final String RATES_CONFIG_FILE = "./config/main/Rates.ini";
	private static final String SERVER_CONFIG_FILE = "./config/main/Server.ini";
	private static final String SEVENSIGNS_CONFIG_FILE = "./config/main/SevenSigns.ini";
	public static final String SIEGE_CONFIG_FILE = "./config/main/Siege.ini";
	// protected
	private static final String DAEMONS_CONFIG_FILE = "./config/protected/Daemons.ini";
	private static final String PROTECT_FLOOD_CONFIG_FILE = "./config/protected/Flood.ini";
	private static final String PROTECT_OTHER_CONFIG_FILE = "./config/protected/Other.ini";
	public static final String TELNET_CONFIG_FILE = "./config/protected/Telnet.ini";
	// events
	private static final String EVENT_CTF_CONFIG_FILE = "./config/events/CtF.ini";
	private static final String EVENT_DM_CONFIG_FILE = "./config/events/DM.ini";
	private static final String EVENT_PC_BANG_POINT_CONFIG_FILE = "./config/events/PcBang.ini";
	private static final String EVENT_TVT_CONFIG_FILE = "./config/events/TvT.ini";
	private static final String EVENT_TW_CONFIG_FILE = "./config/events/TW.ini";
	// custom
	private static final String BANK_CONFIG_FILE = "./config/custom/Bank.ini";
	private static final String CHAMPION_CONFIG_FILE = "./config/custom/Champion.ini";
	private static final String MERCHANT_ZERO_SELL_PRICE_CONFIG_FILE = "./config/custom/MerchantZeroSellPrice.ini";
	private static final String OFFLINE_CONFIG_FILE = "./config/custom/Offline.ini";
	private static final String OTHER_CONFIG_FILE = "./config/custom/Other.ini";
	private static final String SCHEME_BUFFER_CONFIG_FILE = "./config/custom/SchemeBuffer.ini";
	private static final String EVENT_REBIRTH_CONFIG_FILE = "./config/custom/Rebirth.ini";
	private static final String EVENT_WEDDING_CONFIG_FILE = "./config/custom/Wedding.ini";
	private static final String L2JSERVER_CUSTOM = "./config/custom/L2JServer.ini";
	// login
	private static final String LOGIN_CONFIG_FILE = "./config/main/LoginServer.ini";
	// others
	private static final String BANNED_IP_FILE = "./config/others/banned_ip.cfg";
	public static final String SERVER_NAME_FILE = "./config/others/servername.xml";
	// legacy
	private static final String LEGACY_BANNED_IP = "./config/banned_ip.cfg";
	
	// --------------------------------------------------
	// Constants
	// --------------------------------------------------
	public static final String EOL = System.lineSeparator();
	
	public static ServerMode SERVER_MODE = ServerMode.NONE;
	
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean SHOW_GM_LOGIN;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_SPECIAL_EFFECT;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_BUILDER_HIDE;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_CRITANNOUNCER_NAME;
	public static boolean GM_DEBUG_HTML_PATHS;
	public static boolean USE_SUPER_HASTE_AS_GM_SPEED;
	
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean TRADE_CHAT_WITH_PVP;
	public static int TRADE_PVP_AMOUNT;
	public static boolean GLOBAL_CHAT_WITH_PVP;
	public static int GLOBAL_PVP_AMOUNT;
	public static int BRUT_AVG_TIME;
	public static int BRUT_LOGON_ATTEMPTS;
	public static int BRUT_BAN_IP_TIME;
	public static int MAX_CHAT_LENGTH;
	public static boolean TRADE_CHAT_IS_NOOBLE;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static int DELETE_DAYS;
	public static int MAX_DRIFT_RANGE;
	public static boolean AGGRO_DISTANCE_CHECK_ENABLED;
	public static int AGGRO_DISTANCE_CHECK_RANGE;
	public static boolean AGGRO_DISTANCE_CHECK_RAIDS;
	public static boolean AGGRO_DISTANCE_CHECK_INSTANCES;
	public static boolean AGGRO_DISTANCE_CHECK_RESTORE_LIFE;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_MANOR;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static String PROTECTED_ITEMS;
	public static List<Integer> LIST_PROTECTED_ITEMS = new ArrayList<>();
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_FREIGHT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_NPC_WALKERS;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean ENABLE_COMMUNITY_BOARD;
	public static String BBS_DEFAULT;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_NPC_AGGRESSION;
	public static int ZONE_TOWN;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	
	public static int CHAR_DATA_STORE_INTERVAL;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static boolean BYPASS_VALIDATION;
	public static boolean HIGH_RATE_SERVER_DROPS;
	public static boolean FORCE_COMPLETE_STATUS_UPDATE;
	
	public static int PORT_GAME;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static boolean BACKUP_DATABASE;
	public static String MYSQL_BIN_PATH;
	public static String BACKUP_PATH;
	public static int BACKUP_DAYS;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	
	public static boolean IS_TELNET_ENABLED;
	
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static int WYVERN_SPEED;
	public static int STRIDER_SPEED;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static String NONDROPPABLE_ITEMS;
	public static List<Integer> LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
	public static String PET_RENT_NPC;
	public static List<Integer> LIST_PET_RENT_NPC = new ArrayList<>();
	public static boolean ENABLE_AIO_SYSTEM;
	public static Map<Integer, Integer> AIO_SKILLS;
	public static boolean ALLOW_AIO_NCOLOR;
	public static int AIO_NCOLOR;
	public static boolean ALLOW_AIO_TCOLOR;
	public static int AIO_TCOLOR;
	public static boolean ALLOW_AIO_USE_GK;
	public static boolean ALLOW_AIO_USE_CM;
	public static boolean ALLOW_AIO_IN_EVENTS;
	public static int CHAT_FILTER_PUNISHMENT_PARAM1;
	public static int CHAT_FILTER_PUNISHMENT_PARAM2;
	public static int CHAT_FILTER_PUNISHMENT_PARAM3;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static String CHAT_FILTER_PUNISHMENT;
	public static List<String> FILTER_LIST = new ArrayList<>();
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static boolean ALLOW_QUAKE_SYSTEM;
	public static boolean ENABLE_ANTI_PVP_FARM_MSG;
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_QUESTS_REWARD;
	public static float RATE_DROP_ADENA;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_SEAL_STONES;
	public static float RATE_DROP_SPOIL;
	public static float RATE_DROP_MANOR;
	public static float RATE_DROP_QUEST;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_MP_HP_HERBS;
	public static float RATE_DROP_GREATER_HERBS;
	public static float RATE_DROP_SUPERIOR_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static float ADENA_BOSS;
	public static float ADENA_RAID;
	public static float ADENA_MINION;
	public static float ITEMS_BOSS;
	public static float ITEMS_RAID;
	public static float ITEMS_MINION;
	public static float SPOIL_BOSS;
	public static float SPOIL_RAID;
	public static float SPOIL_MINION;
	
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static float ALT_GAME_SKILL_HIT_RATE;
	public static boolean ALT_GAME_VIEWNPC;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static boolean ALT_PRIVILEGES_SECURE_CHECK;
	public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static int ALT_LOTTERY_PRIZE;
	public static int ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static boolean ALT_KARMA_TELEPORT_TO_FLORAN;
	public static boolean DONT_DESTROY_SS;
	public static int STANDARD_RESPAWN_DELAY;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static boolean EXPERTISE_PENALTY;
	public static boolean MASTERY_PENALTY;
	public static int LEVEL_TO_GET_PENALTY;
	public static boolean MASTERY_WEAPON_PENALTY;
	public static int LEVEL_TO_GET_WEAPON_PENALTY;
	public static int ACTIVE_AUGMENTS_START_REUSE_TIME;
	public static boolean NPC_ATTACKABLE;
	public static List<Integer> INVUL_NPC_LIST;
	public static boolean DISABLE_ATTACK_NPC_TYPE;
	public static String ALLOWED_NPC_TYPES;
	public static List<String> LIST_ALLOWED_NPC_TYPES = new ArrayList<>();
	public static boolean SELL_BY_ITEM;
	public static int SELL_ITEM;
	public static String DISABLE_BOW_CLASSES_STRING;
	public static List<Integer> DISABLE_BOW_CLASSES = new ArrayList<>();
	public static boolean ALT_MOBS_STATS_BONUS;
	public static boolean ALT_PETS_STATS_BONUS;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_P_DEFENCE_MULTIPLIER;
	public static double RAID_M_DEFENCE_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static long CLICK_TASK;
	public static boolean ANNOUNCE_CASTLE_LORDS;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALLOW_GUARDS;
	
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_REQUIRE_WIN_7S;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	
	public static int DEVASTATED_DAY;
	public static int DEVASTATED_HOUR;
	public static int DEVASTATED_MINUTES;
	public static int PARTISAN_DAY;
	public static int PARTISAN_HOUR;
	public static int PARTISAN_MINUTES;
	
	public static boolean CHAMPION_ENABLE;
	public static int CHAMPION_FREQUENCY;
	public static int CHAMP_MIN_LVL;
	public static int CHAMP_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static int CHAMPION_ADENAS_REWARDS;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	public static String CHAMP_TITLE;
	
	public static boolean MERCHANT_ZERO_SELL_PRICE;
	
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_DURATION;
	public static int WEDDING_NAME_COLOR_NORMAL;
	public static int WEDDING_NAME_COLOR_GEY;
	public static int WEDDING_NAME_COLOR_LESBO;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	public static boolean GIVE_CUPID_BOW;
	public static boolean ANNOUNCE_WEDDING;
	
	public static final int REWARD_PER_TIME_MODE_RESTRICTION = 2;
	public static int DAILY_REWARD_ITEM_ID;
	public static boolean ENABLE_DAILY;
	public static int MIN_LEVEL_TO_DAILYREWARD;
	public static int DAILY_REWARD_AMOUNT;
	
	public static String TVT_EVEN_TEAMS;
	public static boolean TVT_ALLOW_INTERFERENCE;
	public static boolean TVT_ALLOW_POTIONS;
	public static boolean TVT_ALLOW_SUMMON;
	public static boolean TVT_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean TVT_ON_START_UNSUMMON_PET;
	public static boolean TVT_REVIVE_RECOVERY;
	public static boolean TVT_ANNOUNCE_TEAM_STATS;
	public static boolean TVT_ANNOUNCE_REWARD;
	public static boolean TVT_PRICE_NO_KILLS;
	public static boolean TVT_JOIN_CURSED;
	public static boolean TVT_COMMAND;
	public static long TVT_REVIVE_DELAY;
	public static boolean TVT_OPEN_FORT_DOORS;
	public static boolean TVT_CLOSE_FORT_DOORS;
	public static boolean TVT_OPEN_ADEN_COLOSSEUM_DOORS;
	public static boolean TVT_CLOSE_ADEN_COLOSSEUM_DOORS;
	public static int TVT_TOP_KILLER_REWARD;
	public static int TVT_TOP_KILLER_QTY;
	public static boolean TVT_AURA;
	public static boolean TVT_STATS_LOGGER;
	public static boolean TVT_REMOVE_BUFFS_ON_DIE;
	
	public static int TW_TOWN_ID;
	public static boolean TW_ALL_TOWNS;
	public static int TW_ITEM_ID;
	public static int TW_ITEM_AMOUNT;
	public static boolean TW_ALLOW_KARMA;
	public static boolean TW_DISABLE_GK;
	public static boolean TW_RESS_ON_DIE;
	
	public static boolean REBIRTH_ENABLE;
	public static String[] REBIRTH_ITEM_PRICE;
	public static String[] REBIRTH_MAGE_SKILL;
	public static String[] REBIRTH_FIGHTER_SKILL;
	public static int REBIRTH_MIN_LEVEL;
	public static int REBIRTH_MAX;
	public static int REBIRTH_RETURN_TO_LEVEL;
	
	public static boolean PCB_ENABLE;
	public static int PCB_MIN_LEVEL;
	public static int PCB_POINT_MIN;
	public static int PCB_POINT_MAX;
	public static int PCB_CHANCE_DUAL_POINT;
	public static int PCB_INTERVAL;
	
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_DEV_NO_SCRIPT;
	public static boolean ALT_DEV_NO_RB;
	public static boolean SERVER_LIST_TESTSERVER;
	public static boolean SERVER_GMONLY;
	public static boolean GMAUDIT;
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	
	public static boolean LAZY_CACHE;
	public static boolean CHECK_HTML_ENCODING;
	
	public static boolean IS_CRAFTING_ENABLED;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	
	public static int BUFFER_MAX_SCHEMES;
	public static int BUFFER_STATIC_BUFF_COST;
	
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_SET_INVULNERABLE;
	public static boolean OFFLINE_COMMAND1;
	public static boolean OFFLINE_COMMAND2;
	public static boolean OFFLINE_LOGOUT;
	public static boolean OFFLINE_SLEEP_EFFECT;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	
	public static boolean DM_ALLOW_INTERFERENCE;
	public static boolean DM_ALLOW_POTIONS;
	public static boolean DM_ALLOW_SUMMON;
	public static boolean DM_JOIN_CURSED;
	public static boolean DM_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean DM_ON_START_UNSUMMON_PET;
	public static long DM_REVIVE_DELAY;
	public static boolean DM_COMMAND;
	public static boolean DM_ENABLE_KILL_REWARD;
	public static int DM_KILL_REWARD_ID;
	public static int DM_KILL_REWARD_AMOUNT;
	public static boolean DM_ANNOUNCE_REWARD;
	public static boolean DM_REVIVE_RECOVERY;
	public static int DM_SPAWN_OFFSET;
	public static boolean DM_STATS_LOGGER;
	public static boolean DM_ALLOW_HEALER_CLASSES;
	public static boolean DM_REMOVE_BUFFS_ON_DIE;
	
	public static String CTF_EVEN_TEAMS;
	public static boolean CTF_ALLOW_INTERFERENCE;
	public static boolean CTF_ALLOW_POTIONS;
	public static boolean CTF_ALLOW_SUMMON;
	public static boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean CTF_ON_START_UNSUMMON_PET;
	public static boolean CTF_ANNOUNCE_TEAM_STATS;
	public static boolean CTF_ANNOUNCE_REWARD;
	public static boolean CTF_JOIN_CURSED;
	public static boolean CTF_REVIVE_RECOVERY;
	public static boolean CTF_COMMAND;
	public static boolean CTF_AURA;
	public static boolean CTF_STATS_LOGGER;
	public static int CTF_SPAWN_OFFSET;
	public static boolean CTF_REMOVE_BUFFS_ON_DIE;
	
	public static boolean ONLINE_PLAYERS_ON_LOGIN;
	public static boolean SUBSTUCK_SKILLS;
	public static boolean ALT_SERVER_NAME_ENABLED;
	public static boolean ANNOUNCE_TO_ALL_SPAWN_RB;
	public static boolean ANNOUNCE_TRY_BANNED_ACCOUNT;
	public static String ALT_Server_Name;
	public static boolean DONATOR_NAME_COLOR_ENABLED;
	public static int DONATOR_NAME_COLOR;
	public static int DONATOR_TITLE_COLOR;
	public static float DONATOR_XPSP_RATE;
	public static float DONATOR_ADENA_RATE;
	public static float DONATOR_DROP_RATE;
	public static float DONATOR_SPOIL_RATE;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean DELETE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE = true;
	public static boolean CUSTOM_TELEPORT_TABLE = true;
	public static boolean CUSTOM_DROPLIST_TABLE = true;
	public static boolean CUSTOM_MERCHANT_TABLES = true;
	public static boolean ALLOW_SIMPLE_STATS_VIEW;
	public static boolean ALLOW_DETAILED_STATS_VIEW;
	public static boolean ALLOW_ONLINE_VIEW;
	public static boolean WELCOME_HTM;
	public static String ALLOWED_SKILLS;
	public static List<Integer> ALLOWED_SKILLS_LIST = new ArrayList<>();
	public static boolean PROTECTOR_PLAYER_PK;
	public static boolean PROTECTOR_PLAYER_PVP;
	public static int PROTECTOR_RADIUS_ACTION;
	public static int PROTECTOR_SKILLID;
	public static int PROTECTOR_SKILLLEVEL;
	public static int PROTECTOR_SKILLTIME;
	public static String PROTECTOR_MESSAGE;
	public static boolean CASTLE_SHIELD;
	public static boolean CLANHALL_SHIELD;
	public static boolean APELLA_ARMORS;
	public static boolean OATH_ARMORS;
	public static boolean CASTLE_CROWN;
	public static boolean CASTLE_CIRCLETS;
	public static boolean KEEP_SUBCLASS_SKILLS;
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	public static boolean NOBLE_CUSTOM_ITEMS;
	public static boolean HERO_CUSTOM_ITEMS;
	public static boolean ALLOW_CREATE_LVL;
	public static int CHAR_CREATE_LVL;
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	public static boolean ALLOW_HERO_SUBSKILL;
	public static int HERO_COUNT;
	public static int CRUMA_TOWER_LEVEL_RESTRICT;
	public static boolean ALLOW_RAID_BOSS_PETRIFIED;
	public static int ALT_PLAYER_PROTECTION_LEVEL;
	public static boolean ALLOW_LOW_LEVEL_TRADE;
	public static boolean USE_CHAT_FILTER;
	public static int MONSTER_RETURN_DELAY;
	public static boolean SCROLL_STACKABLE;
	public static boolean ALLOW_CHAR_KILL_PROTECT;
	public static int CLAN_LEADER_COLOR;
	public static int CLAN_LEADER_COLOR_CLAN_LEVEL;
	public static boolean CLAN_LEADER_COLOR_ENABLED;
	public static int CLAN_LEADER_COLORED;
	public static boolean SAVE_RAIDBOSS_STATUS_INTO_DB;
	public static boolean DISABLE_WEIGHT_PENALTY;
	public static int DIFFERENT_Z_CHANGE_OBJECT;
	public static int DIFFERENT_Z_NEW_MOVIE;
	public static int HERO_CUSTOM_ITEM_ID;
	public static int NOOBLE_CUSTOM_ITEM_ID;
	public static int HERO_CUSTOM_DAY;
	public static boolean ALLOW_FARM1_COMMAND;
	public static boolean ALLOW_FARM2_COMMAND;
	public static boolean ALLOW_PVP1_COMMAND;
	public static boolean ALLOW_PVP2_COMMAND;
	public static int FARM1_X;
	public static int FARM1_Y;
	public static int FARM1_Z;
	public static int PVP1_X;
	public static int PVP1_Y;
	public static int PVP1_Z;
	public static int FARM2_X;
	public static int FARM2_Y;
	public static int FARM2_Z;
	public static int PVP2_X;
	public static int PVP2_Y;
	public static int PVP2_Z;
	public static String FARM1_CUSTOM_MESSAGE;
	public static String FARM2_CUSTOM_MESSAGE;
	public static String PVP1_CUSTOM_MESSAGE;
	public static String PVP2_CUSTOM_MESSAGE;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean PM_MESSAGE_ON_START;
	public static boolean SERVER_TIME_ON_START;
	public static String PM_SERVER_NAME;
	public static String PM_TEXT1;
	public static String PM_TEXT2;
	public static boolean NEW_PLAYER_EFFECT;
	
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	public static boolean PVP_COLOR_SYSTEM_ENABLED;
	public static int PVP_AMOUNT1;
	public static int PVP_AMOUNT2;
	public static int PVP_AMOUNT3;
	public static int PVP_AMOUNT4;
	public static int PVP_AMOUNT5;
	public static int NAME_COLOR_FOR_PVP_AMOUNT1;
	public static int NAME_COLOR_FOR_PVP_AMOUNT2;
	public static int NAME_COLOR_FOR_PVP_AMOUNT3;
	public static int NAME_COLOR_FOR_PVP_AMOUNT4;
	public static int NAME_COLOR_FOR_PVP_AMOUNT5;
	public static boolean PK_COLOR_SYSTEM_ENABLED;
	public static int PK_AMOUNT1;
	public static int PK_AMOUNT2;
	public static int PK_AMOUNT3;
	public static int PK_AMOUNT4;
	public static int PK_AMOUNT5;
	public static int TITLE_COLOR_FOR_PK_AMOUNT1;
	public static int TITLE_COLOR_FOR_PK_AMOUNT2;
	public static int TITLE_COLOR_FOR_PK_AMOUNT3;
	public static int TITLE_COLOR_FOR_PK_AMOUNT4;
	public static int TITLE_COLOR_FOR_PK_AMOUNT5;
	public static boolean PVP_REWARD_ENABLED;
	public static int PVP_REWARD_ID;
	public static int PVP_REWARD_AMOUNT;
	public static boolean PK_REWARD_ENABLED;
	public static int PK_REWARD_ID;
	public static int PK_REWARD_AMOUNT;
	public static int REWARD_PROTECT;
	public static boolean ENABLE_PK_INFO;
	public static boolean FLAGED_PLAYER_USE_BUFFER;
	public static boolean FLAGED_PLAYER_CAN_USE_GK;
	public static boolean PVPEXPSP_SYSTEM;
	public static int ADD_EXP;
	public static int ADD_SP;
	public static boolean ALLOW_POTS_IN_PVP;
	public static boolean ALLOW_SOE_IN_PVP;
	public static boolean ANNOUNCE_PVP_KILL;
	public static boolean ANNOUNCE_PK_KILL;
	public static boolean ANNOUNCE_ALL_KILL;
	public static int DUEL_SPAWN_X;
	public static int DUEL_SPAWN_Y;
	public static int DUEL_SPAWN_Z;
	public static boolean PVP_PK_TITLE;
	public static String PVP_TITLE_PREFIX;
	public static String PK_TITLE_PREFIX;
	public static boolean WAR_LEGEND_AURA;
	public static int KILLS_TO_GET_WAR_LEGEND_AURA;
	public static boolean ANTI_FARM_ENABLED;
	public static boolean ANTI_FARM_CLAN_ALLY_ENABLED;
	public static boolean ANTI_FARM_LVL_DIFF_ENABLED;
	public static int ANTI_FARM_MAX_LVL_DIFF;
	public static boolean ANTI_FARM_PDEF_DIFF_ENABLED;
	public static int ANTI_FARM_MAX_PDEF_DIFF;
	public static boolean ANTI_FARM_PATK_DIFF_ENABLED;
	public static int ANTI_FARM_MAX_PATK_DIFF;
	public static boolean ANTI_FARM_PARTY_ENABLED;
	public static boolean ANTI_FARM_IP_ENABLED;
	public static boolean ANTI_FARM_SUMMON;
	
	public static int ALT_OLY_NUMBER_HEROS_EACH_CLASS;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static List<Integer> LIST_OLY_RESTRICTED_SKILLS = new ArrayList<>();
	public static boolean ALT_OLY_AUGMENT_ALLOW;
	public static int ALT_OLY_TELEPORT_COUNTDOWN;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_MIN_POINT_FOR_EXCH;
	public static int ALT_OLY_HERO_POINTS;
	public static String ALT_OLY_RESTRICTED_ITEMS;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>();
	public static boolean ALLOW_EVENTS_DURING_OLY;
	public static boolean ALT_OLY_RECHARGE_SKILLS;
	public static int ALT_OLY_COMP_RITEM;
	public static boolean REMOVE_CUBIC_OLYMPIAD;
	public static boolean ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS;
	public static OlympiadPeriod ALT_OLY_PERIOD;
	public static int ALT_OLY_PERIOD_MULTIPLIER;
	public static List<Integer> ALT_OLY_COMPETITION_DAYS;
	
	public static Map<Integer, Integer> NORMAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> BLESS_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> NORMAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> BLESS_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> NORMAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> BLESS_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int ENCHANT_WEAPON_MAX;
	public static int ENCHANT_ARMOR_MAX;
	public static int ENCHANT_JEWELRY_MAX;
	public static int CRYSTAL_ENCHANT_MAX;
	public static int CRYSTAL_ENCHANT_MIN;
	public static boolean ENABLE_DWARF_ENCHANT_BONUS;
	public static int DWARF_ENCHANT_MIN_LEVEL;
	public static int DWARF_ENCHANT_BONUS;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static boolean DELETE_AUGM_PASSIVE_ON_CHANGE;
	public static boolean DELETE_AUGM_ACTIVE_ON_CHANGE;
	public static boolean ENCHANT_HERO_WEAPON;
	public static int SOUL_CRYSTAL_BREAK_CHANCE;
	public static int SOUL_CRYSTAL_LEVEL_CHANCE;
	public static int SOUL_CRYSTAL_MAX_LEVEL;
	public static int CUSTOM_ENCHANT_VALUE;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int BREAK_ENCHANT;
	public static int GM_OVER_ENCHANT;
	public static int MAX_ITEM_ENCHANT_KICK;
	
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_UNKNOWN_PACKETS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_PARTY_INVITATION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SAY_ACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MOVE_ACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GENERIC_ACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MACRO;
	public static FloodProtectorConfig FLOOD_PROTECTOR_POTION;
	
	public static boolean CHECK_SKILLS_ON_ENTER;
	public static boolean CHECK_NAME_ON_LOGIN;
	public static boolean L2WALKER_PROTECTION;
	public static boolean PROTECTED_ENCHANT;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean ONLY_GM_TELEPORT_FREE;
	public static boolean ALLOW_DUALBOX;
	public static int ALLOWED_BOXES;
	public static boolean ALLOW_DUALBOX_OLY;
	public static boolean ALLOW_DUALBOX_EVENT;
	
	public static int BLOW_ATTACK_FRONT;
	public static int BLOW_ATTACK_SIDE;
	public static int BLOW_ATTACK_BEHIND;
	public static int BACKSTAB_ATTACK_FRONT;
	public static int BACKSTAB_ATTACK_SIDE;
	public static int BACKSTAB_ATTACK_BEHIND;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static float MCRIT_RATE_MUL;
	public static int RUN_SPD_BOOST;
	public static int MAX_RUN_SPEED;
	public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
	public static float ALT_DAGGER_DMG_VS_HEAVY;
	public static float ALT_DAGGER_DMG_VS_ROBE;
	public static float ALT_DAGGER_DMG_VS_LIGHT;
	public static boolean ALLOW_RAID_LETHAL;
	
	public static boolean ALLOW_LETHAL_PROTECTION_MOBS;
	public static String LETHAL_PROTECTED_MOBS;
	public static List<Integer> LIST_LETHAL_PROTECTED_MOBS = new ArrayList<>();
	public static float MAGIC_CRITICAL_POWER;
	public static float STUN_CHANCE_MODIFIER;
	public static float BLEED_CHANCE_MODIFIER;
	public static float POISON_CHANCE_MODIFIER;
	public static float PARALYZE_CHANCE_MODIFIER;
	public static float ROOT_CHANCE_MODIFIER;
	public static float SLEEP_CHANCE_MODIFIER;
	public static float FEAR_CHANCE_MODIFIER;
	public static float CONFUSION_CHANCE_MODIFIER;
	public static float DEBUFF_CHANCE_MODIFIER;
	public static float BUFF_CHANCE_MODIFIER;
	public static boolean SEND_SKILLS_CHANCE_TO_PLAYERS;
	public static boolean REMOVE_WEAPON_SUBCLASS;
	public static boolean REMOVE_CHEST_SUBCLASS;
	public static boolean REMOVE_LEG_SUBCLASS;
	public static boolean ENABLE_CLASS_DAMAGE_SETTINGS;
	public static boolean ENABLE_CLASS_DAMAGE_SETTINGS_IN_OLY;
	public static boolean ENABLE_CLASS_DAMAGE_LOGGER;
	public static boolean LEAVE_BUFFS_ON_DIE;
	public static boolean ALT_RAIDS_STATS_BONUS;
	
	public static String GEODATA_PATH;
	public static int COORD_SYNCHRONIZE;
	public static int PART_OF_CHARACTER_HEIGHT;
	public static int MAX_OBSTACLE_HEIGHT;
	public static boolean PATHFINDING;
	public static String PATHFIND_BUFFERS;
	public static int BASE_WEIGHT;
	public static int DIAGONAL_WEIGHT;
	public static int HEURISTIC_WEIGHT;
	public static int OBSTACLE_MULTIPLIER;
	public static int MAX_ITERATIONS;
	public static boolean FALL_DAMAGE;
	public static boolean ALLOW_WATER;
	
	public static int RBLOCKRAGE;
	public static boolean PLAYERS_CAN_HEAL_RB;
	public static HashMap<Integer, Integer> RBS_SPECIFIC_LOCK_RAGE;
	public static boolean ALLOW_DIRECT_TP_TO_BOSS_ROOM;
	public static boolean ANTHARAS_OLD;
	public static int ANTHARAS_CLOSE;
	public static int ANTHARAS_DESPAWN_TIME;
	public static int ANTHARAS_RESP_FIRST;
	public static int ANTHARAS_RESP_SECOND;
	public static int ANTHARAS_WAIT_TIME;
	public static float ANTHARAS_POWER_MULTIPLIER;
	public static int BAIUM_SLEEP;
	public static int BAIUM_RESP_FIRST;
	public static int BAIUM_RESP_SECOND;
	public static float BAIUM_POWER_MULTIPLIER;
	public static int CORE_RESP_MINION;
	public static int CORE_RESP_FIRST;
	public static int CORE_RESP_SECOND;
	public static int CORE_LEVEL;
	public static int CORE_RING_CHANCE;
	public static float CORE_POWER_MULTIPLIER;
	public static int QA_RESP_NURSE;
	public static int QA_RESP_ROYAL;
	public static int QA_RESP_FIRST;
	public static int QA_RESP_SECOND;
	public static int QA_LEVEL;
	public static int QA_RING_CHANCE;
	public static float QA_POWER_MULTIPLIER;
	public static float LEVEL_DIFF_MULTIPLIER_MINION;
	public static int HPH_FIXINTERVALOFHALTER;
	public static int HPH_RANDOMINTERVALOFHALTER;
	public static int HPH_APPTIMEOFHALTER;
	public static int HPH_ACTIVITYTIMEOFHALTER;
	public static int HPH_FIGHTTIMEOFHALTER;
	public static int HPH_CALLROYALGUARDHELPERCOUNT;
	public static int HPH_CALLROYALGUARDHELPERINTERVAL;
	public static int HPH_INTERVALOFDOOROFALTER;
	public static int HPH_TIMEOFLOCKUPDOOROFALTAR;
	public static int ZAKEN_RESP_FIRST;
	public static int ZAKEN_RESP_SECOND;
	public static int ZAKEN_LEVEL;
	public static int ZAKEN_EARRING_CHANCE;
	public static float ZAKEN_POWER_MULTIPLIER;
	public static int ORFEN_RESP_FIRST;
	public static int ORFEN_RESP_SECOND;
	public static int ORFEN_LEVEL;
	public static int ORFEN_EARRING_CHANCE;
	public static float ORFEN_POWER_MULTIPLIER;
	public static int VALAKAS_RESP_FIRST;
	public static int VALAKAS_RESP_SECOND;
	public static int VALAKAS_WAIT_TIME;
	public static int VALAKAS_DESPAWN_TIME;
	public static float VALAKAS_POWER_MULTIPLIER;
	public static int FRINTEZZA_RESP_FIRST;
	public static int FRINTEZZA_RESP_SECOND;
	public static float FRINTEZZA_POWER_MULTIPLIER;
	public static boolean BYPASS_FRINTEZZA_PARTIES_CHECK;
	public static int FRINTEZZA_MIN_PARTIES;
	public static int FRINTEZZA_MAX_PARTIES;
	public static String RAID_INFO_IDS;
	public static List<Integer> RAID_INFO_IDS_LIST = new ArrayList<>();
	
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_BOSS;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean SP_BOOK_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static int ALLOWED_SUBCLASS;
	public static byte BASE_SUBCLASS_LEVEL;
	public static byte MAX_SUBCLASS_LEVEL;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE;
	public static int ALT_PARTY_RANGE;
	public static double ALT_WEIGHT_LIMIT;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALT_GAME_MOB_ATTACK_AI;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_GAME_FREIGHTS;
	public static int ALT_GAME_FREIGHT_PRICE;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static boolean ALT_GAME_TIREDNESS;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static boolean ALT_RECOMMEND;
	public static int ALT_RECOMMENDATIONS_NUMBER;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static int MAX_LEVEL_NEWBIE;
	public static int MAX_LEVEL_NEWBIE_STATUS;
	public static boolean DISABLE_TUTORIAL;
	public static int STARTING_ADENA;
	public static int STARTING_AA;
	public static boolean CUSTOM_STARTER_ITEMS_ENABLED;
	public static List<int[]> STARTING_CUSTOM_ITEMS_F = new ArrayList<>();
	public static List<int[]> STARTING_CUSTOM_ITEMS_M = new ArrayList<>();
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int MAX_ITEM_IN_PACKET;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean ENABLE_KEYBOARD_MOVEMENT;
	public static int UNSTUCK_INTERVAL;
	public static int PLAYER_SPAWN_PROTECTION;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean DEEPBLUE_DROP_RULES;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean RESPAWN_RANDOM_ENABLED;
	public static int RESPAWN_RANDOM_MAX_OFFSET;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static int DEATH_PENALTY_CHANCE;
	public static boolean EFFECT_CANCELING;
	public static boolean STORE_SKILL_COOLTIME;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte DEBUFFS_MAX_AMOUNT;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static boolean ALLOW_CLASS_MASTERS;
	public static boolean CLASS_MASTER_STRIDER_UPDATE;
	public static boolean ALLOW_CLASS_MASTERS_FIRST_CLASS;
	public static boolean ALLOW_CLASS_MASTERS_SECOND_CLASS;
	public static boolean ALLOW_CLASS_MASTERS_THIRD_CLASS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_REMOTE_CLASS_MASTERS;
	
	public static long DEADLOCKCHECK_INTIAL_TIME;
	public static long DEADLOCKCHECK_DELAY_TIME;
	
	public static ArrayList<String> QUESTION_LIST = new ArrayList<>();
	
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;
	public static int REQUEST_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static File DATAPACK_ROOT;
	public static File SCRIPT_ROOT;
	public static int MAXIMUM_ONLINE_USERS;
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;
	public static int SCHEDULED_THREAD_POOL_COUNT;
	public static int INSTANT_THREAD_POOL_COUNT;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static String ALLY_NAME_TEMPLATE;
	
	public static int IP_UPDATE_TIME;
	public static boolean SHOW_LICENCE;
	public static boolean FORCE_GGAUTH;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static String NETWORK_IP_LIST;
	public static long SESSION_TTL;
	public static int MAX_LOGINSESSIONS;
	
	/** MMO settings */
	public static final int MMO_SELECTOR_SLEEP_TIME = 20; // default 20
	public static final int MMO_MAX_SEND_PER_PASS = 80; // default 80
	public static final int MMO_MAX_READ_PER_PASS = 80; // default 80
	public static final int MMO_HELPER_BUFFER_COUNT = 20; // default 20
	
	/** Client Packets Queue settings */
	public static final int CLIENT_PACKET_QUEUE_SIZE = 14; // default MMO_MAX_READ_PER_PASS + 2
	public static final int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = 13; // default MMO_MAX_READ_PER_PASS + 1
	public static final int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = 160; // default 160
	public static final int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = 5; // default 5
	public static final int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = 80; // default 80
	public static final int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = 2; // default 2
	public static final int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = 1; // default 1
	public static final int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = 1; // default 1
	public static final int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = 5; // default 5
	
	/** Packet handler settings */
	public static final boolean PACKET_HANDLER_DEBUG = false;
	
	public static void loadAccessConfig()
	{
		final PropertiesParser accessConfig = new PropertiesParser(ACCESS_CONFIG_FILE);
		EVERYBODY_HAS_ADMIN_RIGHTS = accessConfig.getBoolean("EverybodyHasAdminRights", false);
		GM_STARTUP_AUTO_LIST = accessConfig.getBoolean("GMStartupAutoList", true);
		GM_HERO_AURA = accessConfig.getBoolean("GMHeroAura", false);
		GM_STARTUP_BUILDER_HIDE = accessConfig.getBoolean("GMStartupBuilderHide", true);
		GM_STARTUP_INVULNERABLE = accessConfig.getBoolean("GMStartupInvulnerable", true);
		GM_ANNOUNCER_NAME = accessConfig.getBoolean("AnnounceGmName", false);
		GM_CRITANNOUNCER_NAME = accessConfig.getBoolean("CritAnnounceName", false);
		SHOW_GM_LOGIN = accessConfig.getBoolean("ShowGMLogin", false);
		GM_STARTUP_INVISIBLE = accessConfig.getBoolean("GMStartupInvisible", true);
		GM_SPECIAL_EFFECT = accessConfig.getBoolean("GmLoginSpecialEffect", false);
		GM_STARTUP_SILENCE = accessConfig.getBoolean("GMStartupSilence", true);
		GM_DEBUG_HTML_PATHS = accessConfig.getBoolean("GMDebugHtmlPaths", true);
		USE_SUPER_HASTE_AS_GM_SPEED = accessConfig.getBoolean("UseSuperHasteAsGMSpeed", false);
	}
	
	public static void loadServerConfig()
	{
		final PropertiesParser serverConfig = new PropertiesParser(SERVER_CONFIG_FILE);
		GAMESERVER_HOSTNAME = serverConfig.getString("GameserverHostname", "");
		PORT_GAME = serverConfig.getInt("GameserverPort", 7777);
		EXTERNAL_HOSTNAME = serverConfig.getString("ExternalHostname", "*");
		INTERNAL_HOSTNAME = serverConfig.getString("InternalHostname", "*");
		GAME_SERVER_LOGIN_PORT = serverConfig.getInt("LoginPort", 9014);
		GAME_SERVER_LOGIN_HOST = serverConfig.getString("LoginHost", "127.0.0.1");
		DATABASE_DRIVER = serverConfig.getString("Driver", "org.mariadb.jdbc.Driver");
		DATABASE_URL = serverConfig.getString("URL", "jdbc:mariadb://localhost/");
		DATABASE_LOGIN = serverConfig.getString("Login", "root");
		DATABASE_PASSWORD = serverConfig.getString("Password", "");
		DATABASE_MAX_CONNECTIONS = serverConfig.getInt("MaximumDbConnections", 10);
		BACKUP_DATABASE = serverConfig.getBoolean("BackupDatabase", false);
		MYSQL_BIN_PATH = serverConfig.getString("MySqlBinLocation", "C:/xampp/mysql/bin/");
		BACKUP_PATH = serverConfig.getString("BackupPath", "../backup/");
		BACKUP_DAYS = serverConfig.getInt("BackupDays", 30);
		REQUEST_ID = serverConfig.getInt("RequestServerID", 0);
		ACCEPT_ALTERNATE_ID = serverConfig.getBoolean("AcceptAlternateID", true);
		try
		{
			DATAPACK_ROOT = new File(serverConfig.getString("DatapackRoot", ".")).getCanonicalFile();
			SCRIPT_ROOT = new File(serverConfig.getString("ScriptRoot", "./data/scripts").replaceAll("\\\\", "/")).getCanonicalFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		MAXIMUM_ONLINE_USERS = serverConfig.getInt("MaximumOnlineUsers", 100);
		SERVER_LIST_BRACKET = serverConfig.getBoolean("ServerListBrackets", false);
		SERVER_LIST_CLOCK = serverConfig.getBoolean("ServerListClock", false);
		MIN_PROTOCOL_REVISION = serverConfig.getInt("MinProtocolRevision", 660);
		MAX_PROTOCOL_REVISION = serverConfig.getInt("MaxProtocolRevision", 665);
		if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
		{
			throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
		}
		SCHEDULED_THREAD_POOL_COUNT = serverConfig.getInt("ScheduledThreadPoolCount", 40);
		INSTANT_THREAD_POOL_COUNT = serverConfig.getInt("InstantThreadPoolCount", 20);
		CNAME_TEMPLATE = serverConfig.getString("CnameTemplate", ".*");
		PET_NAME_TEMPLATE = serverConfig.getString("PetNameTemplate", ".*");
		CLAN_NAME_TEMPLATE = serverConfig.getString("ClanNameTemplate", ".*");
		ALLY_NAME_TEMPLATE = serverConfig.getString("AllyNameTemplate", ".*");
	}
	
	public static void loadTelnetConfig()
	{
		final PropertiesParser telnetConfig = new PropertiesParser(TELNET_CONFIG_FILE);
		IS_TELNET_ENABLED = telnetConfig.getBoolean("EnableTelnet", false);
	}
	
	public static void loadRatesConfig()
	{
		final PropertiesParser ratesConfig = new PropertiesParser(RATES_CONFIG_FILE);
		RATE_XP = ratesConfig.getFloat("RateXp", 1f);
		RATE_SP = ratesConfig.getFloat("RateSp", 1f);
		RATE_PARTY_XP = ratesConfig.getFloat("RatePartyXp", 1f);
		RATE_PARTY_SP = ratesConfig.getFloat("RatePartySp", 1f);
		RATE_QUESTS_REWARD = ratesConfig.getFloat("RateQuestsReward", 1f);
		RATE_DROP_ADENA = ratesConfig.getFloat("RateDropAdena", 1f);
		RATE_CONSUMABLE_COST = ratesConfig.getFloat("RateConsumableCost", 1f);
		RATE_DROP_ITEMS = ratesConfig.getFloat("RateDropItems", 1f);
		RATE_DROP_SEAL_STONES = ratesConfig.getFloat("RateDropSealStones", 1f);
		RATE_DROP_SPOIL = ratesConfig.getFloat("RateDropSpoil", 1f);
		RATE_DROP_MANOR = ratesConfig.getFloat("RateDropManor", 1f);
		RATE_DROP_QUEST = ratesConfig.getFloat("RateDropQuest", 1f);
		RATE_KARMA_EXP_LOST = ratesConfig.getFloat("RateKarmaExpLost", 1f);
		RATE_SIEGE_GUARDS_PRICE = ratesConfig.getFloat("RateSiegeGuardsPrice", 1f);
		RATE_DROP_COMMON_HERBS = ratesConfig.getFloat("RateCommonHerbs", 15f);
		RATE_DROP_MP_HP_HERBS = ratesConfig.getFloat("RateHpMpHerbs", 10f);
		RATE_DROP_GREATER_HERBS = ratesConfig.getFloat("RateGreaterHerbs", 4f);
		RATE_DROP_SUPERIOR_HERBS = ratesConfig.getFloat("RateSuperiorHerbs", 0.80f) * 10;
		RATE_DROP_SPECIAL_HERBS = ratesConfig.getFloat("RateSpecialHerbs", 0.20f) * 10;
		PLAYER_DROP_LIMIT = ratesConfig.getInt("PlayerDropLimit", 3);
		PLAYER_RATE_DROP = ratesConfig.getInt("PlayerRateDrop", 5);
		PLAYER_RATE_DROP_ITEM = ratesConfig.getInt("PlayerRateDropItem", 70);
		PLAYER_RATE_DROP_EQUIP = ratesConfig.getInt("PlayerRateDropEquip", 25);
		PLAYER_RATE_DROP_EQUIP_WEAPON = ratesConfig.getInt("PlayerRateDropEquipWeapon", 5);
		PET_XP_RATE = ratesConfig.getFloat("PetXpRate", 1f);
		PET_FOOD_RATE = ratesConfig.getInt("PetFoodRate", 1);
		SINEATER_XP_RATE = ratesConfig.getFloat("SinEaterXpRate", 1f);
		KARMA_DROP_LIMIT = ratesConfig.getInt("KarmaDropLimit", 10);
		KARMA_RATE_DROP = ratesConfig.getInt("KarmaRateDrop", 70);
		KARMA_RATE_DROP_ITEM = ratesConfig.getInt("KarmaRateDropItem", 50);
		KARMA_RATE_DROP_EQUIP = ratesConfig.getInt("KarmaRateDropEquip", 40);
		KARMA_RATE_DROP_EQUIP_WEAPON = ratesConfig.getInt("KarmaRateDropEquipWeapon", 10);
		ADENA_BOSS = ratesConfig.getFloat("AdenaBoss", 1f);
		ADENA_RAID = ratesConfig.getFloat("AdenaRaid", 1f);
		ADENA_MINION = ratesConfig.getFloat("AdenaMinion", 1f);
		ITEMS_BOSS = ratesConfig.getFloat("ItemsBoss", 1f);
		ITEMS_RAID = ratesConfig.getFloat("ItemsRaid", 1f);
		ITEMS_MINION = ratesConfig.getFloat("ItemsMinion", 1f);
		SPOIL_BOSS = ratesConfig.getFloat("SpoilBoss", 1f);
		SPOIL_RAID = ratesConfig.getFloat("SpoilRaid", 1f);
		SPOIL_MINION = ratesConfig.getFloat("SpoilMinion", 1f);
	}
	
	public static void loadGeneralConfig()
	{
		final PropertiesParser generalConfig = new PropertiesParser(GENERAL_CONFIG_FILE);
		SERVER_LIST_TESTSERVER = generalConfig.getBoolean("TestServer", false);
		SERVER_GMONLY = generalConfig.getBoolean("ServerGMOnly", false);
		ALT_DEV_NO_QUESTS = generalConfig.getBoolean("AltDevNoQuests", false);
		ALT_DEV_NO_SPAWNS = generalConfig.getBoolean("AltDevNoSpawns", false);
		ALT_DEV_NO_SCRIPT = generalConfig.getBoolean("AltDevNoScript", false);
		ALT_DEV_NO_RB = generalConfig.getBoolean("AltDevNoRB", false);
		GMAUDIT = generalConfig.getBoolean("GMAudit", false);
		LOG_CHAT = generalConfig.getBoolean("LogChat", false);
		LOG_ITEMS = generalConfig.getBoolean("LogItems", false);
		LAZY_CACHE = generalConfig.getBoolean("LazyCache", false);
		CHECK_HTML_ENCODING = generalConfig.getBoolean("CheckHtmlEncoding", true);
		REMOVE_CASTLE_CIRCLETS = generalConfig.getBoolean("RemoveCastleCirclets", true);
		ALT_GAME_VIEWNPC = generalConfig.getBoolean("AltGameViewNpc", false);
		ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = generalConfig.getBoolean("AltNewCharAlwaysIsNewbie", false);
		ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = generalConfig.getBoolean("AltMembersCanWithdrawFromClanWH", false);
		ALT_MAX_NUM_OF_CLANS_IN_ALLY = generalConfig.getInt("AltMaxNumOfClansInAlly", 3);
		ALT_CLAN_MEMBERS_FOR_WAR = generalConfig.getInt("AltClanMembersForWar", 15);
		ALT_CLAN_JOIN_DAYS = generalConfig.getInt("DaysBeforeJoinAClan", 5);
		ALT_CLAN_CREATE_DAYS = generalConfig.getInt("DaysBeforeCreateAClan", 10);
		ALT_CLAN_DISSOLVE_DAYS = generalConfig.getInt("DaysToPassToDissolveAClan", 7);
		ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = generalConfig.getInt("DaysBeforeJoinAllyWhenLeaved", 1);
		ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = generalConfig.getInt("DaysBeforeJoinAllyWhenDismissed", 1);
		ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = generalConfig.getInt("DaysBeforeAcceptNewClanWhenDismissed", 1);
		ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = generalConfig.getInt("DaysBeforeCreateNewAllyWhenDissolved", 10);
		ALT_MANOR_REFRESH_TIME = generalConfig.getInt("AltManorRefreshTime", 20);
		ALT_MANOR_REFRESH_MIN = generalConfig.getInt("AltManorRefreshMin", 0);
		ALT_MANOR_APPROVE_TIME = generalConfig.getInt("AltManorApproveTime", 6);
		ALT_MANOR_APPROVE_MIN = generalConfig.getInt("AltManorApproveMin", 0);
		ALT_MANOR_MAINTENANCE_PERIOD = generalConfig.getInt("AltManorMaintenancePeriod", 360000);
		ALT_MANOR_SAVE_ALL_ACTIONS = generalConfig.getBoolean("AltManorSaveAllActions", false);
		ALT_MANOR_SAVE_PERIOD_RATE = generalConfig.getInt("AltManorSavePeriodRate", 2);
		ALT_LOTTERY_PRIZE = generalConfig.getInt("AltLotteryPrize", 50000);
		ALT_LOTTERY_TICKET_PRICE = generalConfig.getInt("AltLotteryTicketPrice", 2000);
		ALT_LOTTERY_5_NUMBER_RATE = generalConfig.getFloat("AltLottery5NumberRate", 0.6f);
		ALT_LOTTERY_4_NUMBER_RATE = generalConfig.getFloat("AltLottery4NumberRate", 0.2f);
		ALT_LOTTERY_3_NUMBER_RATE = generalConfig.getFloat("AltLottery3NumberRate", 0.2f);
		ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = generalConfig.getInt("AltLottery2and1NumberPrize", 200);
		ALT_FISH_CHAMPIONSHIP_ENABLED = generalConfig.getBoolean("AltFishChampionshipEnabled", true);
		ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = generalConfig.getInt("AltFishChampionshipRewardItemId", 57);
		ALT_FISH_CHAMPIONSHIP_REWARD_1 = generalConfig.getInt("AltFishChampionshipReward1", 800000);
		ALT_FISH_CHAMPIONSHIP_REWARD_2 = generalConfig.getInt("AltFishChampionshipReward2", 500000);
		ALT_FISH_CHAMPIONSHIP_REWARD_3 = generalConfig.getInt("AltFishChampionshipReward3", 300000);
		ALT_FISH_CHAMPIONSHIP_REWARD_4 = generalConfig.getInt("AltFishChampionshipReward4", 200000);
		ALT_FISH_CHAMPIONSHIP_REWARD_5 = generalConfig.getInt("AltFishChampionshipReward5", 100000);
		RIFT_MIN_PARTY_SIZE = generalConfig.getInt("RiftMinPartySize", 5);
		RIFT_MAX_JUMPS = generalConfig.getInt("MaxRiftJumps", 4);
		RIFT_SPAWN_DELAY = generalConfig.getInt("RiftSpawnDelay", 10000);
		RIFT_AUTO_JUMPS_TIME_MIN = generalConfig.getInt("AutoJumpsDelayMin", 480);
		RIFT_AUTO_JUMPS_TIME_MAX = generalConfig.getInt("AutoJumpsDelayMax", 600);
		RIFT_ENTER_COST_RECRUIT = generalConfig.getInt("RecruitCost", 18);
		RIFT_ENTER_COST_SOLDIER = generalConfig.getInt("SoldierCost", 21);
		RIFT_ENTER_COST_OFFICER = generalConfig.getInt("OfficerCost", 24);
		RIFT_ENTER_COST_CAPTAIN = generalConfig.getInt("CaptainCost", 27);
		RIFT_ENTER_COST_COMMANDER = generalConfig.getInt("CommanderCost", 30);
		RIFT_ENTER_COST_HERO = generalConfig.getInt("HeroCost", 33);
		RIFT_BOSS_ROOM_TIME_MUTIPLY = generalConfig.getFloat("BossRoomTimeMultiply", 1.5f);
		DONT_DESTROY_SS = generalConfig.getBoolean("DontDestroySS", false);
		STANDARD_RESPAWN_DELAY = generalConfig.getInt("StandardRespawnDelay", 180);
		RAID_RANKING_1ST = generalConfig.getInt("1stRaidRankingPoints", 1250);
		RAID_RANKING_2ND = generalConfig.getInt("2ndRaidRankingPoints", 900);
		RAID_RANKING_3RD = generalConfig.getInt("3rdRaidRankingPoints", 700);
		RAID_RANKING_4TH = generalConfig.getInt("4thRaidRankingPoints", 600);
		RAID_RANKING_5TH = generalConfig.getInt("5thRaidRankingPoints", 450);
		RAID_RANKING_6TH = generalConfig.getInt("6thRaidRankingPoints", 350);
		RAID_RANKING_7TH = generalConfig.getInt("7thRaidRankingPoints", 300);
		RAID_RANKING_8TH = generalConfig.getInt("8thRaidRankingPoints", 200);
		RAID_RANKING_9TH = generalConfig.getInt("9thRaidRankingPoints", 150);
		RAID_RANKING_10TH = generalConfig.getInt("10thRaidRankingPoints", 100);
		RAID_RANKING_UP_TO_50TH = generalConfig.getInt("UpTo50thRaidRankingPoints", 25);
		RAID_RANKING_UP_TO_100TH = generalConfig.getInt("UpTo100thRaidRankingPoints", 12);
		EXPERTISE_PENALTY = generalConfig.getBoolean("ExpertisePenalty", true);
		MASTERY_PENALTY = generalConfig.getBoolean("MasteryPenalty", false);
		LEVEL_TO_GET_PENALTY = generalConfig.getInt("LevelToGetPenalty", 20);
		MASTERY_WEAPON_PENALTY = generalConfig.getBoolean("MasteryWeaponPenality", false);
		LEVEL_TO_GET_WEAPON_PENALTY = generalConfig.getInt("LevelToGetWeaponPenalty", 20);
		ACTIVE_AUGMENTS_START_REUSE_TIME = generalConfig.getInt("AugmStartReuseTime", 0);
		INVUL_NPC_LIST = new ArrayList<>();
		final String t = generalConfig.getString("InvulNpcList", "30001-32132,35092-35103,35142-35146,35176-35187,35218-35232,35261-35278,35308-35319,35352-35367,35382-35407,35417-35427,35433-35469,35497-35513,35544-35587,35600-35617,35623-35628,35638-35640,35644,35645,50007,70010,99999");
		String as[];
		final int k = (as = t.split(",")).length;
		for (int j = 0; j < k; j++)
		{
			final String t2 = as[j];
			if (t2.contains("-"))
			{
				final int a1 = Integer.parseInt(t2.split("-")[0]);
				final int a2 = Integer.parseInt(t2.split("-")[1]);
				for (int i = a1; i <= a2; i++)
				{
					INVUL_NPC_LIST.add(Integer.valueOf(i));
				}
			}
			else
			{
				INVUL_NPC_LIST.add(Integer.valueOf(Integer.parseInt(t2)));
			}
		}
		DISABLE_ATTACK_NPC_TYPE = generalConfig.getBoolean("DisableAttackToNpcs", false);
		ALLOWED_NPC_TYPES = generalConfig.getString("AllowedNPCTypes", "");
		LIST_ALLOWED_NPC_TYPES = new ArrayList<>();
		for (String npc_type : ALLOWED_NPC_TYPES.split(","))
		{
			LIST_ALLOWED_NPC_TYPES.add(npc_type);
		}
		NPC_ATTACKABLE = generalConfig.getBoolean("NpcAttackable", false);
		SELL_BY_ITEM = generalConfig.getBoolean("SellByItem", false);
		SELL_ITEM = generalConfig.getInt("SellItem", 57);
		ALT_MOBS_STATS_BONUS = generalConfig.getBoolean("AltMobsStatsBonus", true);
		ALT_PETS_STATS_BONUS = generalConfig.getBoolean("AltPetsStatsBonus", true);
		RAID_HP_REGEN_MULTIPLIER = generalConfig.getDouble("RaidHpRegenMultiplier", 100) / 100;
		RAID_MP_REGEN_MULTIPLIER = generalConfig.getDouble("RaidMpRegenMultiplier", 100) / 100;
		RAID_P_DEFENCE_MULTIPLIER = generalConfig.getDouble("RaidPhysicalDefenceMultiplier", 100) / 100;
		RAID_M_DEFENCE_MULTIPLIER = generalConfig.getDouble("RaidMagicalDefenceMultiplier", 100) / 100;
		RAID_MINION_RESPAWN_TIMER = generalConfig.getDouble("RaidMinionRespawnTime", 300000);
		RAID_MIN_RESPAWN_MULTIPLIER = generalConfig.getFloat("RaidMinRespawnMultiplier", 1f);
		RAID_MAX_RESPAWN_MULTIPLIER = generalConfig.getFloat("RaidMaxRespawnMultiplier", 1f);
		CLICK_TASK = generalConfig.getInt("ClickTaskDelay", 50);
		WYVERN_SPEED = generalConfig.getInt("WyvernSpeed", 100);
		STRIDER_SPEED = generalConfig.getInt("StriderSpeed", 80);
		ALLOW_WYVERN_UPGRADER = generalConfig.getBoolean("AllowWyvernUpgrader", false);
		ENABLE_AIO_SYSTEM = generalConfig.getBoolean("EnableAioSystem", true);
		ALLOW_AIO_NCOLOR = generalConfig.getBoolean("AllowAioNameColor", true);
		AIO_NCOLOR = Integer.decode("0x" + generalConfig.getString("AioNameColor", "88AA88"));
		ALLOW_AIO_TCOLOR = generalConfig.getBoolean("AllowAioTitleColor", true);
		AIO_TCOLOR = Integer.decode("0x" + generalConfig.getString("AioTitleColor", "88AA88"));
		ALLOW_AIO_USE_GK = generalConfig.getBoolean("AllowAioUseGk", false);
		ALLOW_AIO_USE_CM = generalConfig.getBoolean("AllowAioUseClassMaster", false);
		ALLOW_AIO_IN_EVENTS = generalConfig.getBoolean("AllowAioInEvents", false);
		if (ENABLE_AIO_SYSTEM)
		{
			final String[] AioSkillsSplit = generalConfig.getString("AioSkills", "").split(";");
			AIO_SKILLS = new HashMap<>(AioSkillsSplit.length);
			for (String skill : AioSkillsSplit)
			{
				final String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
				{
					LOGGER.info("[Aio System]: invalid config property in " + GENERAL_CONFIG_FILE + " -> AioSkills \"" + skill + "\"");
				}
				else
				{
					try
					{
						AIO_SKILLS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.equals(""))
						{
							LOGGER.info("[Aio System]: invalid config property in " + GENERAL_CONFIG_FILE + " -> AioSkills \"" + skillSplit[0] + "\"" + skillSplit[1]);
						}
					}
				}
			}
		}
		PET_RENT_NPC = generalConfig.getString("ListPetRentNpc", "30827");
		LIST_PET_RENT_NPC = new ArrayList<>();
		for (String id : PET_RENT_NPC.split(","))
		{
			LIST_PET_RENT_NPC.add(Integer.parseInt(id));
		}
		JAIL_IS_PVP = generalConfig.getBoolean("JailIsPvp", true);
		JAIL_DISABLE_CHAT = generalConfig.getBoolean("JailDisableChat", true);
		USE_SAY_FILTER = generalConfig.getBoolean("UseChatFilter", false);
		CHAT_FILTER_CHARS = generalConfig.getString("ChatFilterChars", "[I love L2jServer]");
		CHAT_FILTER_PUNISHMENT = generalConfig.getString("ChatFilterPunishment", "off");
		CHAT_FILTER_PUNISHMENT_PARAM1 = generalConfig.getInt("ChatFilterPunishmentParam1", 1);
		CHAT_FILTER_PUNISHMENT_PARAM2 = generalConfig.getInt("ChatFilterPunishmentParam2", 1000);
		FS_TIME_ATTACK = generalConfig.getInt("TimeOfAttack", 50);
		FS_TIME_COOLDOWN = generalConfig.getInt("TimeOfCoolDown", 5);
		FS_TIME_ENTRY = generalConfig.getInt("TimeOfEntry", 3);
		FS_TIME_WARMUP = generalConfig.getInt("TimeOfWarmUp", 2);
		FS_PARTY_MEMBER_COUNT = generalConfig.getInt("NumberOfNecessaryPartyMembers", 4);
		if (FS_TIME_ATTACK <= 0)
		{
			FS_TIME_ATTACK = 50;
		}
		if (FS_TIME_COOLDOWN <= 0)
		{
			FS_TIME_COOLDOWN = 5;
		}
		if (FS_TIME_ENTRY <= 0)
		{
			FS_TIME_ENTRY = 3;
		}
		if (FS_TIME_WARMUP <= 0)
		{
			FS_TIME_WARMUP = 2;
		}
		if (FS_PARTY_MEMBER_COUNT <= 0)
		{
			FS_PARTY_MEMBER_COUNT = 4;
		}
		ALLOW_QUAKE_SYSTEM = generalConfig.getBoolean("AllowQuakeSystem", false);
		ENABLE_ANTI_PVP_FARM_MSG = generalConfig.getBoolean("EnableAntiPvpFarmMsg", false);
		ANNOUNCE_CASTLE_LORDS = generalConfig.getBoolean("AnnounceCastleLords", false);
		ANNOUNCE_MAMMON_SPAWN = generalConfig.getBoolean("AnnounceMammonSpawn", true);
		ALLOW_GUARDS = generalConfig.getBoolean("AllowGuards", false);
		AUTODESTROY_ITEM_AFTER = generalConfig.getInt("AutoDestroyDroppedItemAfter", 0);
		HERB_AUTO_DESTROY_TIME = generalConfig.getInt("AutoDestroyHerbTime", 15) * 1000;
		PROTECTED_ITEMS = generalConfig.getString("ListOfProtectedItems", "");
		LIST_PROTECTED_ITEMS = new ArrayList<>();
		for (String id : PROTECTED_ITEMS.split(","))
		{
			LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
		}
		DESTROY_DROPPED_PLAYER_ITEM = generalConfig.getBoolean("DestroyPlayerDroppedItem", false);
		DESTROY_EQUIPABLE_PLAYER_ITEM = generalConfig.getBoolean("DestroyEquipableItem", false);
		SAVE_DROPPED_ITEM = generalConfig.getBoolean("SaveDroppedItem", false);
		EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = generalConfig.getBoolean("EmptyDroppedItemTableAfterLoad", false);
		SAVE_DROPPED_ITEM_INTERVAL = generalConfig.getInt("SaveDroppedItemInterval", 0) * 60000;
		CLEAR_DROPPED_ITEM_TABLE = generalConfig.getBoolean("ClearDroppedItemTable", false);
		PRECISE_DROP_CALCULATION = generalConfig.getBoolean("PreciseDropCalculation", true);
		MULTIPLE_ITEM_DROP = generalConfig.getBoolean("MultipleItemDrop", true);
		ALLOW_WAREHOUSE = generalConfig.getBoolean("AllowWarehouse", true);
		WAREHOUSE_CACHE = generalConfig.getBoolean("WarehouseCache", false);
		WAREHOUSE_CACHE_TIME = generalConfig.getInt("WarehouseCacheTime", 15);
		ALLOW_FREIGHT = generalConfig.getBoolean("AllowFreight", true);
		ALLOW_WEAR = generalConfig.getBoolean("AllowWear", false);
		WEAR_DELAY = generalConfig.getInt("WearDelay", 5);
		WEAR_PRICE = generalConfig.getInt("WearPrice", 10);
		ALLOW_LOTTERY = generalConfig.getBoolean("AllowLottery", false);
		ALLOW_RACE = generalConfig.getBoolean("AllowRace", false);
		ALLOW_RENTPET = generalConfig.getBoolean("AllowRentPet", false);
		ALLOW_DISCARDITEM = generalConfig.getBoolean("AllowDiscardItem", true);
		ALLOWFISHING = generalConfig.getBoolean("AllowFishing", false);
		ALLOW_MANOR = generalConfig.getBoolean("AllowManor", false);
		ALLOW_BOAT = generalConfig.getBoolean("AllowBoat", false);
		ALLOW_NPC_WALKERS = generalConfig.getBoolean("AllowNpcWalkers", true);
		ALLOW_CURSED_WEAPONS = generalConfig.getBoolean("AllowCursedWeapons", false);
		DEFAULT_GLOBAL_CHAT = generalConfig.getString("GlobalChat", "ON");
		DEFAULT_TRADE_CHAT = generalConfig.getString("TradeChat", "ON");
		MAX_CHAT_LENGTH = generalConfig.getInt("MaxChatLength", 100);
		TRADE_CHAT_IS_NOOBLE = generalConfig.getBoolean("TradeChatIsNooble", false);
		TRADE_CHAT_WITH_PVP = generalConfig.getBoolean("TradeChatWithPvP", false);
		TRADE_PVP_AMOUNT = generalConfig.getInt("TradePvPAmount", 800);
		GLOBAL_CHAT_WITH_PVP = generalConfig.getBoolean("GlobalChatWithPvP", false);
		GLOBAL_PVP_AMOUNT = generalConfig.getInt("GlobalPvPAmount", 1500);
		ENABLE_COMMUNITY_BOARD = generalConfig.getBoolean("EnableCommunityBoard", true);
		BBS_DEFAULT = generalConfig.getString("BBSDefault", "_bbshome");
		ZONE_TOWN = generalConfig.getInt("ZoneTown", 0);
		MAX_DRIFT_RANGE = generalConfig.getInt("MaxDriftRange", 300);
		AGGRO_DISTANCE_CHECK_ENABLED = generalConfig.getBoolean("AggroDistanceCheckEnabled", false);
		AGGRO_DISTANCE_CHECK_RANGE = generalConfig.getInt("AggroDistanceCheckRange", 1500);
		AGGRO_DISTANCE_CHECK_RAIDS = generalConfig.getBoolean("AggroDistanceCheckRaids", false);
		AGGRO_DISTANCE_CHECK_INSTANCES = generalConfig.getBoolean("AggroDistanceCheckInstances", false);
		AGGRO_DISTANCE_CHECK_RESTORE_LIFE = generalConfig.getBoolean("AggroDistanceCheckRestoreLife", true);
		MIN_NPC_ANIMATION = generalConfig.getInt("MinNpcAnimation", 5);
		MAX_NPC_ANIMATION = generalConfig.getInt("MaxNpcAnimation", 60);
		MIN_MONSTER_ANIMATION = generalConfig.getInt("MinMonsterAnimation", 5);
		MAX_MONSTER_ANIMATION = generalConfig.getInt("MaxMonsterAnimation", 60);
		SHOW_NPC_LVL = generalConfig.getBoolean("ShowNpcLevel", false);
		SHOW_NPC_AGGRESSION = generalConfig.getBoolean("ShowNpcAggression", false);
		FORCE_INVENTORY_UPDATE = generalConfig.getBoolean("ForceInventoryUpdate", false);
		FORCE_COMPLETE_STATUS_UPDATE = generalConfig.getBoolean("ForceCompletePlayerStatusUpdate", true);
		CHAR_DATA_STORE_INTERVAL = generalConfig.getInt("CharacterDataStoreInterval", 15) * 60 * 1000;
		UPDATE_ITEMS_ON_CHAR_STORE = generalConfig.getBoolean("UpdateItemsOnCharStore", false);
		AUTODELETE_INVALID_QUEST_DATA = generalConfig.getBoolean("AutoDeleteInvalidQuestData", false);
		DELETE_DAYS = generalConfig.getInt("DeleteCharAfterDays", 7);
		DEFAULT_PUNISH = generalConfig.getInt("DefaultPunish", 2);
		DEFAULT_PUNISH_PARAM = generalConfig.getInt("DefaultPunishParam", 0);
		GRIDS_ALWAYS_ON = generalConfig.getBoolean("GridsAlwaysOn", false);
		GRID_NEIGHBOR_TURNON_TIME = generalConfig.getInt("GridNeighborTurnOnTime", 30);
		GRID_NEIGHBOR_TURNOFF_TIME = generalConfig.getInt("GridNeighborTurnOffTime", 300);
		HIGH_RATE_SERVER_DROPS = generalConfig.getBoolean("HighRateServerDrops", false);
	}
	
	public static void load7sConfig()
	{
		final PropertiesParser sevenSignsConfig = new PropertiesParser(SEVENSIGNS_CONFIG_FILE);
		ALT_GAME_REQUIRE_CASTLE_DAWN = sevenSignsConfig.getBoolean("AltRequireCastleForDawn", false);
		ALT_GAME_REQUIRE_CLAN_CASTLE = sevenSignsConfig.getBoolean("AltRequireClanCastle", false);
		ALT_REQUIRE_WIN_7S = sevenSignsConfig.getBoolean("AltRequireWin7s", true);
		ALT_FESTIVAL_MIN_PLAYER = sevenSignsConfig.getInt("AltFestivalMinPlayer", 5);
		ALT_MAXIMUM_PLAYER_CONTRIB = sevenSignsConfig.getInt("AltMaxPlayerContrib", 1000000);
		ALT_FESTIVAL_MANAGER_START = sevenSignsConfig.getLong("AltFestivalManagerStart", 120000);
		ALT_FESTIVAL_LENGTH = sevenSignsConfig.getLong("AltFestivalLength", 1080000);
		ALT_FESTIVAL_CYCLE_LENGTH = sevenSignsConfig.getLong("AltFestivalCycleLength", 2280000);
		ALT_FESTIVAL_FIRST_SPAWN = sevenSignsConfig.getLong("AltFestivalFirstSpawn", 120000);
		ALT_FESTIVAL_FIRST_SWARM = sevenSignsConfig.getLong("AltFestivalFirstSwarm", 300000);
		ALT_FESTIVAL_SECOND_SPAWN = sevenSignsConfig.getLong("AltFestivalSecondSpawn", 540000);
		ALT_FESTIVAL_SECOND_SWARM = sevenSignsConfig.getLong("AltFestivalSecondSwarm", 720000);
		ALT_FESTIVAL_CHEST_SPAWN = sevenSignsConfig.getLong("AltFestivalChestSpawn", 900000);
	}
	
	public static void loadCHConfig()
	{
		final PropertiesParser clanhallConfig = new PropertiesParser(CLANHALL_CONFIG_FILE);
		CH_TELE_FEE_RATIO = clanhallConfig.getLong("ClanHallTeleportFunctionFeeRation", 86400000);
		CH_TELE1_FEE = clanhallConfig.getInt("ClanHallTeleportFunctionFeeLvl1", 86400000);
		CH_TELE2_FEE = clanhallConfig.getInt("ClanHallTeleportFunctionFeeLvl2", 86400000);
		CH_SUPPORT_FEE_RATIO = clanhallConfig.getLong("ClanHallSupportFunctionFeeRation", 86400000);
		CH_SUPPORT1_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl1", 86400000);
		CH_SUPPORT2_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl2", 86400000);
		CH_SUPPORT3_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl3", 86400000);
		CH_SUPPORT4_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl4", 86400000);
		CH_SUPPORT5_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl5", 86400000);
		CH_SUPPORT6_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl6", 86400000);
		CH_SUPPORT7_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl7", 86400000);
		CH_SUPPORT8_FEE = clanhallConfig.getInt("ClanHallSupportFeeLvl8", 86400000);
		CH_MPREG_FEE_RATIO = clanhallConfig.getLong("ClanHallMpRegenerationFunctionFeeRation", 86400000);
		CH_MPREG1_FEE = clanhallConfig.getInt("ClanHallMpRegenerationFeeLvl1", 86400000);
		CH_MPREG2_FEE = clanhallConfig.getInt("ClanHallMpRegenerationFeeLvl2", 86400000);
		CH_MPREG3_FEE = clanhallConfig.getInt("ClanHallMpRegenerationFeeLvl3", 86400000);
		CH_MPREG4_FEE = clanhallConfig.getInt("ClanHallMpRegenerationFeeLvl4", 86400000);
		CH_MPREG5_FEE = clanhallConfig.getInt("ClanHallMpRegenerationFeeLvl5", 86400000);
		CH_HPREG_FEE_RATIO = clanhallConfig.getLong("ClanHallHpRegenerationFunctionFeeRation", 86400000);
		CH_HPREG1_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl1", 86400000);
		CH_HPREG2_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl2", 86400000);
		CH_HPREG3_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl3", 86400000);
		CH_HPREG4_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl4", 86400000);
		CH_HPREG5_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl5", 86400000);
		CH_HPREG6_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl6", 86400000);
		CH_HPREG7_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl7", 86400000);
		CH_HPREG8_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl8", 86400000);
		CH_HPREG9_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl9", 86400000);
		CH_HPREG10_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl10", 86400000);
		CH_HPREG11_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl11", 86400000);
		CH_HPREG12_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl12", 86400000);
		CH_HPREG13_FEE = clanhallConfig.getInt("ClanHallHpRegenerationFeeLvl13", 86400000);
		CH_EXPREG_FEE_RATIO = clanhallConfig.getLong("ClanHallExpRegenerationFunctionFeeRation", 86400000);
		CH_EXPREG1_FEE = clanhallConfig.getInt("ClanHallExpRegenerationFeeLvl1", 86400000);
		CH_EXPREG2_FEE = clanhallConfig.getInt("ClanHallExpRegenerationFeeLvl2", 86400000);
		CH_EXPREG3_FEE = clanhallConfig.getInt("ClanHallExpRegenerationFeeLvl3", 86400000);
		CH_EXPREG4_FEE = clanhallConfig.getInt("ClanHallExpRegenerationFeeLvl4", 86400000);
		CH_EXPREG5_FEE = clanhallConfig.getInt("ClanHallExpRegenerationFeeLvl5", 86400000);
		CH_EXPREG6_FEE = clanhallConfig.getInt("ClanHallExpRegenerationFeeLvl6", 86400000);
		CH_EXPREG7_FEE = clanhallConfig.getInt("ClanHallExpRegenerationFeeLvl7", 86400000);
		CH_ITEM_FEE_RATIO = clanhallConfig.getLong("ClanHallItemCreationFunctionFeeRation", 86400000);
		CH_ITEM1_FEE = clanhallConfig.getInt("ClanHallItemCreationFunctionFeeLvl1", 86400000);
		CH_ITEM2_FEE = clanhallConfig.getInt("ClanHallItemCreationFunctionFeeLvl2", 86400000);
		CH_ITEM3_FEE = clanhallConfig.getInt("ClanHallItemCreationFunctionFeeLvl3", 86400000);
		CH_CURTAIN_FEE_RATIO = clanhallConfig.getLong("ClanHallCurtainFunctionFeeRation", 86400000);
		CH_CURTAIN1_FEE = clanhallConfig.getInt("ClanHallCurtainFunctionFeeLvl1", 86400000);
		CH_CURTAIN2_FEE = clanhallConfig.getInt("ClanHallCurtainFunctionFeeLvl2", 86400000);
		CH_FRONT_FEE_RATIO = clanhallConfig.getLong("ClanHallFrontPlatformFunctionFeeRation", 86400000);
		CH_FRONT1_FEE = clanhallConfig.getInt("ClanHallFrontPlatformFunctionFeeLvl1", 86400000);
		CH_FRONT2_FEE = clanhallConfig.getInt("ClanHallFrontPlatformFunctionFeeLvl2", 86400000);
	}
	
	public static void loadElitCHConfig()
	{
		final PropertiesParser conquerableConfig = new PropertiesParser(CONQUERABLE_CLANHALL_CONFIG_FILE);
		DEVASTATED_DAY = conquerableConfig.getInt("DevastatedDay", 1);
		DEVASTATED_HOUR = conquerableConfig.getInt("DevastatedHour", 18);
		DEVASTATED_MINUTES = conquerableConfig.getInt("DevastatedMinutes", 0);
		PARTISAN_DAY = conquerableConfig.getInt("PartisanDay", 5);
		PARTISAN_HOUR = conquerableConfig.getInt("PartisanHour", 21);
		PARTISAN_MINUTES = conquerableConfig.getInt("PartisanMinutes", 0);
	}
	
	public static void loadChampionConfig()
	{
		final PropertiesParser championConfig = new PropertiesParser(CHAMPION_CONFIG_FILE);
		CHAMPION_ENABLE = championConfig.getBoolean("ChampionEnable", false);
		CHAMPION_FREQUENCY = championConfig.getInt("ChampionFrequency", 0);
		CHAMP_MIN_LVL = championConfig.getInt("ChampionMinLevel", 20);
		CHAMP_MAX_LVL = championConfig.getInt("ChampionMaxLevel", 60);
		CHAMPION_HP = championConfig.getInt("ChampionHp", 7);
		CHAMPION_HP_REGEN = championConfig.getFloat("ChampionHpRegen", 1f);
		CHAMPION_REWARDS = championConfig.getInt("ChampionRewards", 8);
		CHAMPION_ADENAS_REWARDS = championConfig.getInt("ChampionAdenasRewards", 1);
		CHAMPION_ATK = championConfig.getFloat("ChampionAtk", 1f);
		CHAMPION_SPD_ATK = championConfig.getFloat("ChampionSpdAtk", 1f);
		CHAMPION_REWARD = championConfig.getInt("ChampionRewardItem", 0);
		CHAMPION_REWARD_ID = championConfig.getInt("ChampionRewardItemID", 6393);
		CHAMPION_REWARD_QTY = championConfig.getInt("ChampionRewardItemQty", 1);
		CHAMP_TITLE = championConfig.getString("ChampionTitle", "Champion");
	}
	
	public static void loadMerchantZeroPriceConfig()
	{
		final PropertiesParser merchantZeroSellPriceConfig = new PropertiesParser(MERCHANT_ZERO_SELL_PRICE_CONFIG_FILE);
		MERCHANT_ZERO_SELL_PRICE = merchantZeroSellPriceConfig.getBoolean("MerchantZeroSellPrice", false);
	}
	
	public static void loadWeddingConfig()
	{
		final PropertiesParser weddingConfig = new PropertiesParser(EVENT_WEDDING_CONFIG_FILE);
		ALLOW_WEDDING = weddingConfig.getBoolean("AllowWedding", false);
		WEDDING_PRICE = weddingConfig.getInt("WeddingPrice", 250000000);
		WEDDING_PUNISH_INFIDELITY = weddingConfig.getBoolean("WeddingPunishInfidelity", true);
		WEDDING_TELEPORT = weddingConfig.getBoolean("WeddingTeleport", true);
		WEDDING_TELEPORT_PRICE = weddingConfig.getInt("WeddingTeleportPrice", 50000);
		WEDDING_TELEPORT_DURATION = weddingConfig.getInt("WeddingTeleportDuration", 60);
		WEDDING_NAME_COLOR_NORMAL = Integer.decode("0x" + weddingConfig.getString("WeddingNameCollorN", "FFFFFF"));
		WEDDING_NAME_COLOR_GEY = Integer.decode("0x" + weddingConfig.getString("WeddingNameCollorB", "FFFFFF"));
		WEDDING_NAME_COLOR_LESBO = Integer.decode("0x" + weddingConfig.getString("WeddingNameCollorL", "FFFFFF"));
		WEDDING_SAMESEX = weddingConfig.getBoolean("WeddingAllowSameSex", false);
		WEDDING_FORMALWEAR = weddingConfig.getBoolean("WeddingFormalWear", true);
		WEDDING_DIVORCE_COSTS = weddingConfig.getInt("WeddingDivorceCosts", 20);
		GIVE_CUPID_BOW = weddingConfig.getBoolean("WeddingGiveBow", false);
		ANNOUNCE_WEDDING = weddingConfig.getBoolean("AnnounceWedding", true);
	}
	
	public static void loadL2jServerConfig()
	{
		final PropertiesParser L2jServer = new PropertiesParser(L2JSERVER_CUSTOM);
		ENABLE_DAILY = L2jServer.getBoolean("EnableDailySystem", false);
		DAILY_REWARD_ITEM_ID = L2jServer.getInt("DailySystemItemId", 57);
		DAILY_REWARD_AMOUNT = L2jServer.getInt("DailyRewardAmount", 1500);
		MIN_LEVEL_TO_DAILYREWARD = L2jServer.getInt("DailyMinLevelRequired", 20);
	}
	
	public static void loadTVTConfig()
	{
		final PropertiesParser tvtConfig = new PropertiesParser(EVENT_TVT_CONFIG_FILE);
		TVT_EVEN_TEAMS = tvtConfig.getString("TvTEvenTeams", "BALANCE");
		TVT_ALLOW_INTERFERENCE = tvtConfig.getBoolean("TvTAllowInterference", false);
		TVT_ALLOW_POTIONS = tvtConfig.getBoolean("TvTAllowPotions", false);
		TVT_ALLOW_SUMMON = tvtConfig.getBoolean("TvTAllowSummon", false);
		TVT_ON_START_REMOVE_ALL_EFFECTS = tvtConfig.getBoolean("TvTOnStartRemoveAllEffects", true);
		TVT_ON_START_UNSUMMON_PET = tvtConfig.getBoolean("TvTOnStartUnsummonPet", true);
		TVT_REVIVE_RECOVERY = tvtConfig.getBoolean("TvTReviveRecovery", false);
		TVT_ANNOUNCE_TEAM_STATS = tvtConfig.getBoolean("TvTAnnounceTeamStats", false);
		TVT_ANNOUNCE_REWARD = tvtConfig.getBoolean("TvTAnnounceReward", false);
		TVT_PRICE_NO_KILLS = tvtConfig.getBoolean("TvTPriceNoKills", false);
		TVT_JOIN_CURSED = tvtConfig.getBoolean("TvTJoinWithCursedWeapon", true);
		TVT_COMMAND = tvtConfig.getBoolean("TvTCommand", true);
		TVT_REVIVE_DELAY = tvtConfig.getLong("TvTReviveDelay", 20000);
		if (TVT_REVIVE_DELAY < 1000)
		{
			TVT_REVIVE_DELAY = 1000; // can't be set less then 1 second
		}
		TVT_OPEN_FORT_DOORS = tvtConfig.getBoolean("TvTOpenFortDoors", false);
		TVT_CLOSE_FORT_DOORS = tvtConfig.getBoolean("TvTCloseFortDoors", false);
		TVT_OPEN_ADEN_COLOSSEUM_DOORS = tvtConfig.getBoolean("TvTOpenAdenColosseumDoors", false);
		TVT_CLOSE_ADEN_COLOSSEUM_DOORS = tvtConfig.getBoolean("TvTCloseAdenColosseumDoors", false);
		TVT_TOP_KILLER_REWARD = tvtConfig.getInt("TvTTopKillerRewardId", 5575);
		TVT_TOP_KILLER_QTY = tvtConfig.getInt("TvTTopKillerRewardQty", 2000000);
		TVT_AURA = tvtConfig.getBoolean("TvTAura", false);
		TVT_STATS_LOGGER = tvtConfig.getBoolean("TvTStatsLogger", true);
		TVT_REMOVE_BUFFS_ON_DIE = tvtConfig.getBoolean("TvTRemoveBuffsOnPlayerDie", false);
	}
	
	public static void loadTWConfig()
	{
		final PropertiesParser twConfig = new PropertiesParser(EVENT_TW_CONFIG_FILE);
		TW_TOWN_ID = twConfig.getInt("TWTownId", 9);
		TW_ALL_TOWNS = twConfig.getBoolean("TWAllTowns", false);
		TW_ITEM_ID = twConfig.getInt("TownWarItemId", 57);
		TW_ITEM_AMOUNT = twConfig.getInt("TownWarItemAmount", 5000);
		TW_ALLOW_KARMA = twConfig.getBoolean("AllowKarma", false);
		TW_DISABLE_GK = twConfig.getBoolean("DisableGK", true);
		TW_RESS_ON_DIE = twConfig.getBoolean("SendRessOnDeath", false);
	}
	
	public static void loadRebirthConfig()
	{
		final PropertiesParser rebirthConfig = new PropertiesParser(EVENT_REBIRTH_CONFIG_FILE);
		REBIRTH_ENABLE = rebirthConfig.getBoolean("REBIRTH_ENABLE", false);
		REBIRTH_MIN_LEVEL = rebirthConfig.getInt("REBIRTH_MIN_LEVEL", 80);
		REBIRTH_MAX = rebirthConfig.getInt("REBIRTH_MAX", 3);
		REBIRTH_RETURN_TO_LEVEL = rebirthConfig.getInt("REBIRTH_RETURN_TO_LEVEL", 1);
		REBIRTH_ITEM_PRICE = rebirthConfig.getString("REBIRTH_ITEM_PRICE", "").split(";");
		REBIRTH_MAGE_SKILL = rebirthConfig.getString("REBIRTH_MAGE_SKILL", "").split(";");
		REBIRTH_FIGHTER_SKILL = rebirthConfig.getString("REBIRTH_FIGHTER_SKILL", "").split(";");
	}
	
	public static void loadPCBPointConfig()
	{
		final PropertiesParser pcBangConfig = new PropertiesParser(EVENT_PC_BANG_POINT_CONFIG_FILE);
		PCB_ENABLE = pcBangConfig.getBoolean("PcBangPointEnable", true);
		PCB_MIN_LEVEL = pcBangConfig.getInt("PcBangPointMinLevel", 20);
		PCB_POINT_MIN = pcBangConfig.getInt("PcBangPointMinCount", 20);
		PCB_POINT_MAX = pcBangConfig.getInt("PcBangPointMaxCount", 1000000);
		if (PCB_POINT_MAX < 1)
		{
			PCB_POINT_MAX = Integer.MAX_VALUE;
		}
		PCB_CHANCE_DUAL_POINT = pcBangConfig.getInt("PcBangPointDualChance", 20);
		PCB_INTERVAL = pcBangConfig.getInt("PcBangPointTimeStamp", 900);
	}
	
	public static void loadCraftConfig()
	{
		final PropertiesParser craftConfig = new PropertiesParser(CRAFTING_CONFIG_FILE);
		DWARF_RECIPE_LIMIT = craftConfig.getInt("DwarfRecipeLimit", 50);
		COMMON_RECIPE_LIMIT = craftConfig.getInt("CommonRecipeLimit", 50);
		IS_CRAFTING_ENABLED = craftConfig.getBoolean("CraftingEnabled", true);
		ALT_GAME_CREATION = craftConfig.getBoolean("AltGameCreation", false);
		ALT_GAME_CREATION_SPEED = craftConfig.getDouble("AltGameCreationSpeed", 1);
		ALT_GAME_CREATION_XP_RATE = craftConfig.getDouble("AltGameCreationRateXp", 1);
		ALT_GAME_CREATION_SP_RATE = craftConfig.getDouble("AltGameCreationRateSp", 1);
		ALT_BLACKSMITH_USE_RECIPES = craftConfig.getBoolean("AltBlacksmithUseRecipes", true);
	}
	
	public static void loadBankingConfig()
	{
		final PropertiesParser bankConfig = new PropertiesParser(BANK_CONFIG_FILE);
		BANKING_SYSTEM_ENABLED = bankConfig.getBoolean("BankingEnabled", false);
		BANKING_SYSTEM_GOLDBARS = bankConfig.getInt("BankingGoldbarCount", 1);
		BANKING_SYSTEM_ADENA = bankConfig.getInt("BankingAdenaCount", 500000000);
	}
	
	public static void loadBufferConfig()
	{
		final PropertiesParser shemeBufferConfig = new PropertiesParser(SCHEME_BUFFER_CONFIG_FILE);
		BUFFER_MAX_SCHEMES = shemeBufferConfig.getInt("BufferMaxSchemesPerChar", 4);
		BUFFER_STATIC_BUFF_COST = shemeBufferConfig.getInt("BufferStaticCostPerBuff", -1);
	}
	
	public static void loadOfflineConfig()
	{
		final PropertiesParser offlineConfig = new PropertiesParser(OFFLINE_CONFIG_FILE);
		OFFLINE_TRADE_ENABLE = offlineConfig.getBoolean("OfflineTradeEnable", false);
		OFFLINE_CRAFT_ENABLE = offlineConfig.getBoolean("OfflineCraftEnable", false);
		OFFLINE_SET_NAME_COLOR = offlineConfig.getBoolean("OfflineNameColorEnable", false);
		OFFLINE_NAME_COLOR = Integer.decode("0x" + offlineConfig.getString("OfflineNameColor", "ff00ff"));
		OFFLINE_MODE_IN_PEACE_ZONE = offlineConfig.getBoolean("OfflineModeInPeaceZone", false);
		OFFLINE_MODE_SET_INVULNERABLE = offlineConfig.getBoolean("OfflineModeSetInvulnerable", false);
		OFFLINE_COMMAND1 = offlineConfig.getBoolean("OfflineCommand1", true);
		OFFLINE_COMMAND2 = offlineConfig.getBoolean("OfflineCommand2", false);
		OFFLINE_LOGOUT = offlineConfig.getBoolean("OfflineLogout", false);
		OFFLINE_SLEEP_EFFECT = offlineConfig.getBoolean("OfflineSleepEffect", true);
		RESTORE_OFFLINERS = offlineConfig.getBoolean("RestoreOffliners", false);
		OFFLINE_MAX_DAYS = offlineConfig.getInt("OfflineMaxDays", 10);
		OFFLINE_DISCONNECT_FINISHED = offlineConfig.getBoolean("OfflineDisconnectFinished", true);
	}
	
	public static void loadDMConfig()
	{
		final PropertiesParser dmConfig = new PropertiesParser(EVENT_DM_CONFIG_FILE);
		DM_ALLOW_INTERFERENCE = dmConfig.getBoolean("DMAllowInterference", false);
		DM_ALLOW_POTIONS = dmConfig.getBoolean("DMAllowPotions", false);
		DM_ALLOW_SUMMON = dmConfig.getBoolean("DMAllowSummon", false);
		DM_JOIN_CURSED = dmConfig.getBoolean("DMJoinWithCursedWeapon", false);
		DM_ON_START_REMOVE_ALL_EFFECTS = dmConfig.getBoolean("DMOnStartRemoveAllEffects", true);
		DM_ON_START_UNSUMMON_PET = dmConfig.getBoolean("DMOnStartUnsummonPet", true);
		DM_REVIVE_DELAY = dmConfig.getLong("DMReviveDelay", 20000);
		if (DM_REVIVE_DELAY < 1000)
		{
			DM_REVIVE_DELAY = 1000; // can't be set less then 1 second
		}
		DM_REVIVE_RECOVERY = dmConfig.getBoolean("DMReviveRecovery", false);
		DM_COMMAND = dmConfig.getBoolean("DMCommand", false);
		DM_ENABLE_KILL_REWARD = dmConfig.getBoolean("DMEnableKillReward", false);
		DM_KILL_REWARD_ID = dmConfig.getInt("DMKillRewardID", 6392);
		DM_KILL_REWARD_AMOUNT = dmConfig.getInt("DMKillRewardAmount", 1);
		DM_ANNOUNCE_REWARD = dmConfig.getBoolean("DMAnnounceReward", false);
		DM_SPAWN_OFFSET = dmConfig.getInt("DMSpawnOffset", 100);
		DM_STATS_LOGGER = dmConfig.getBoolean("DMStatsLogger", true);
		DM_ALLOW_HEALER_CLASSES = dmConfig.getBoolean("DMAllowedHealerClasses", true);
		DM_REMOVE_BUFFS_ON_DIE = dmConfig.getBoolean("DMRemoveBuffsOnPlayerDie", false);
	}
	
	public static void loadCTFConfig()
	{
		final PropertiesParser ctfConfig = new PropertiesParser(EVENT_CTF_CONFIG_FILE);
		CTF_EVEN_TEAMS = ctfConfig.getString("CTFEvenTeams", "BALANCE");
		CTF_ALLOW_INTERFERENCE = ctfConfig.getBoolean("CTFAllowInterference", false);
		CTF_ALLOW_POTIONS = ctfConfig.getBoolean("CTFAllowPotions", false);
		CTF_ALLOW_SUMMON = ctfConfig.getBoolean("CTFAllowSummon", false);
		CTF_ON_START_REMOVE_ALL_EFFECTS = ctfConfig.getBoolean("CTFOnStartRemoveAllEffects", true);
		CTF_ON_START_UNSUMMON_PET = ctfConfig.getBoolean("CTFOnStartUnsummonPet", true);
		CTF_ANNOUNCE_TEAM_STATS = ctfConfig.getBoolean("CTFAnnounceTeamStats", false);
		CTF_ANNOUNCE_REWARD = ctfConfig.getBoolean("CTFAnnounceReward", false);
		CTF_JOIN_CURSED = ctfConfig.getBoolean("CTFJoinWithCursedWeapon", true);
		CTF_REVIVE_RECOVERY = ctfConfig.getBoolean("CTFReviveRecovery", false);
		CTF_COMMAND = ctfConfig.getBoolean("CTFCommand", true);
		CTF_AURA = ctfConfig.getBoolean("CTFAura", true);
		CTF_STATS_LOGGER = ctfConfig.getBoolean("CTFStatsLogger", true);
		CTF_SPAWN_OFFSET = ctfConfig.getInt("CTFSpawnOffset", 100);
		CTF_REMOVE_BUFFS_ON_DIE = ctfConfig.getBoolean("CTFRemoveBuffsOnPlayerDie", false);
	}
	
	public static void loadCustomServerConfig()
	{
		final PropertiesParser customServerConfig = new PropertiesParser(OTHER_CONFIG_FILE);
		CUSTOM_SPAWNLIST_TABLE = customServerConfig.getBoolean("CustomSpawnlistTable", true);
		SAVE_GMSPAWN_ON_CUSTOM = customServerConfig.getBoolean("SaveGmSpawnOnCustom", true);
		DELETE_GMSPAWN_ON_CUSTOM = customServerConfig.getBoolean("DeleteGmSpawnOnCustom", true);
		ONLINE_PLAYERS_ON_LOGIN = customServerConfig.getBoolean("OnlineOnLogin", false);
		PROTECTOR_PLAYER_PK = customServerConfig.getBoolean("ProtectorPlayerPK", false);
		PROTECTOR_PLAYER_PVP = customServerConfig.getBoolean("ProtectorPlayerPVP", false);
		PROTECTOR_RADIUS_ACTION = customServerConfig.getInt("ProtectorRadiusAction", 500);
		PROTECTOR_SKILLID = customServerConfig.getInt("ProtectorSkillId", 1069);
		PROTECTOR_SKILLLEVEL = customServerConfig.getInt("ProtectorSkillLevel", 42);
		PROTECTOR_SKILLTIME = customServerConfig.getInt("ProtectorSkillTime", 800);
		PROTECTOR_MESSAGE = customServerConfig.getString("ProtectorMessage", "Protector, not spawnkilling here, go read the rules !!!");
		DONATOR_NAME_COLOR_ENABLED = customServerConfig.getBoolean("DonatorNameColorEnabled", false);
		DONATOR_NAME_COLOR = Integer.decode("0x" + customServerConfig.getString("DonatorColorName", "00FFFF"));
		DONATOR_TITLE_COLOR = Integer.decode("0x" + customServerConfig.getString("DonatorTitleColor", "00FF00"));
		DONATOR_XPSP_RATE = customServerConfig.getFloat("DonatorXpSpRate", 1.5f);
		DONATOR_ADENA_RATE = customServerConfig.getFloat("DonatorAdenaRate", 1.5f);
		DONATOR_DROP_RATE = customServerConfig.getFloat("DonatorDropRate", 1.5f);
		DONATOR_SPOIL_RATE = customServerConfig.getFloat("DonatorSpoilRate", 1.5f);
		WELCOME_HTM = customServerConfig.getBoolean("WelcomeHtm", false);
		ALT_SERVER_NAME_ENABLED = customServerConfig.getBoolean("ServerNameEnabled", false);
		ANNOUNCE_TO_ALL_SPAWN_RB = customServerConfig.getBoolean("AnnounceToAllSpawnRb", false);
		ANNOUNCE_TRY_BANNED_ACCOUNT = customServerConfig.getBoolean("AnnounceTryBannedAccount", false);
		ALT_Server_Name = customServerConfig.getString("ServerName", "");
		DIFFERENT_Z_CHANGE_OBJECT = customServerConfig.getInt("DifferentZchangeObject", 650);
		DIFFERENT_Z_NEW_MOVIE = customServerConfig.getInt("DifferentZnewmovie", 1000);
		ALLOW_SIMPLE_STATS_VIEW = customServerConfig.getBoolean("AllowSimpleStatsView", false);
		ALLOW_DETAILED_STATS_VIEW = customServerConfig.getBoolean("AllowDetailedStatsView", false);
		ALLOW_ONLINE_VIEW = customServerConfig.getBoolean("AllowOnlineView", false);
		KEEP_SUBCLASS_SKILLS = customServerConfig.getBoolean("KeepSubClassSkills", false);
		ALLOWED_SKILLS = customServerConfig.getString("AllowedSkills", "541,542,543,544,545,546,547,548,549,550,551,552,553,554,555,556,557,558,617,618,619");
		ALLOWED_SKILLS_LIST = new ArrayList<>();
		for (String id : ALLOWED_SKILLS.trim().split(","))
		{
			ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
		}
		CASTLE_SHIELD = customServerConfig.getBoolean("CastleShieldRestriction", true);
		CLANHALL_SHIELD = customServerConfig.getBoolean("ClanHallShieldRestriction", true);
		APELLA_ARMORS = customServerConfig.getBoolean("ApellaArmorsRestriction", true);
		OATH_ARMORS = customServerConfig.getBoolean("OathArmorsRestriction", true);
		CASTLE_CROWN = customServerConfig.getBoolean("CastleLordsCrownRestriction", true);
		CASTLE_CIRCLETS = customServerConfig.getBoolean("CastleCircletsRestriction", true);
		CHAR_TITLE = customServerConfig.getBoolean("CharTitle", false);
		ADD_CHAR_TITLE = customServerConfig.getString("CharAddTitle", "Welcome");
		NOBLE_CUSTOM_ITEMS = customServerConfig.getBoolean("EnableNobleCustomItem", true);
		NOOBLE_CUSTOM_ITEM_ID = customServerConfig.getInt("NoobleCustomItemId", 6673);
		HERO_CUSTOM_ITEMS = customServerConfig.getBoolean("EnableHeroCustomItem", true);
		HERO_CUSTOM_ITEM_ID = customServerConfig.getInt("HeroCustomItemId", 3481);
		HERO_CUSTOM_DAY = customServerConfig.getInt("HeroCustomDay", 0);
		ALLOW_CREATE_LVL = customServerConfig.getBoolean("CustomStartingLvl", false);
		CHAR_CREATE_LVL = customServerConfig.getInt("CharLvl", 80);
		SPAWN_CHAR = customServerConfig.getBoolean("CustomSpawn", false);
		SPAWN_X = customServerConfig.getInt("SpawnX", 50821);
		SPAWN_Y = customServerConfig.getInt("SpawnY", 186527);
		SPAWN_Z = customServerConfig.getInt("SpawnZ", -3625);
		ALLOW_LOW_LEVEL_TRADE = customServerConfig.getBoolean("AllowLowLevelTrade", true);
		ALLOW_HERO_SUBSKILL = customServerConfig.getBoolean("CustomHeroSubSkill", false);
		HERO_COUNT = customServerConfig.getInt("HeroCount", 1);
		CRUMA_TOWER_LEVEL_RESTRICT = customServerConfig.getInt("CrumaTowerLevelRestrict", 56);
		ALLOW_RAID_BOSS_PETRIFIED = customServerConfig.getBoolean("AllowRaidBossPetrified", true);
		ALT_PLAYER_PROTECTION_LEVEL = customServerConfig.getInt("AltPlayerProtectionLevel", 0);
		MONSTER_RETURN_DELAY = customServerConfig.getInt("MonsterReturnDelay", 1200);
		SCROLL_STACKABLE = customServerConfig.getBoolean("ScrollStackable", false);
		ALLOW_CHAR_KILL_PROTECT = customServerConfig.getBoolean("AllowLowLvlProtect", false);
		CLAN_LEADER_COLOR_ENABLED = customServerConfig.getBoolean("ClanLeaderNameColorEnabled", true);
		CLAN_LEADER_COLORED = customServerConfig.getInt("ClanLeaderColored", 1);
		CLAN_LEADER_COLOR = Integer.decode("0x" + customServerConfig.getString("ClanLeaderColor", "00FFFF"));
		CLAN_LEADER_COLOR_CLAN_LEVEL = customServerConfig.getInt("ClanLeaderColorAtClanLevel", 1);
		SAVE_RAIDBOSS_STATUS_INTO_DB = customServerConfig.getBoolean("SaveRBStatusIntoDB", false);
		DISABLE_WEIGHT_PENALTY = customServerConfig.getBoolean("DisableWeightPenalty", false);
		ALLOW_FARM1_COMMAND = customServerConfig.getBoolean("AllowFarm1Command", false);
		ALLOW_FARM2_COMMAND = customServerConfig.getBoolean("AllowFarm2Command", false);
		ALLOW_PVP1_COMMAND = customServerConfig.getBoolean("AllowPvP1Command", false);
		ALLOW_PVP2_COMMAND = customServerConfig.getBoolean("AllowPvP2Command", false);
		FARM1_X = customServerConfig.getInt("farm1_X", 81304);
		FARM1_Y = customServerConfig.getInt("farm1_Y", 14589);
		FARM1_Z = customServerConfig.getInt("farm1_Z", -3469);
		PVP1_X = customServerConfig.getInt("pvp1_X", 81304);
		PVP1_Y = customServerConfig.getInt("pvp1_Y", 14589);
		PVP1_Z = customServerConfig.getInt("pvp1_Z", -3469);
		FARM2_X = customServerConfig.getInt("farm2_X", 81304);
		FARM2_Y = customServerConfig.getInt("farm2_Y", 14589);
		FARM2_Z = customServerConfig.getInt("farm2_Z", -3469);
		PVP2_X = customServerConfig.getInt("pvp2_X", 81304);
		PVP2_Y = customServerConfig.getInt("pvp2_Y", 14589);
		PVP2_Z = customServerConfig.getInt("pvp2_Z", -3469);
		FARM1_CUSTOM_MESSAGE = customServerConfig.getString("Farm1CustomMeesage", "You have been teleported to Farm Zone 1!");
		FARM2_CUSTOM_MESSAGE = customServerConfig.getString("Farm2CustomMeesage", "You have been teleported to Farm Zone 2!");
		PVP1_CUSTOM_MESSAGE = customServerConfig.getString("PvP1CustomMeesage", "You have been teleported to PvP Zone 1!");
		PVP2_CUSTOM_MESSAGE = customServerConfig.getString("PvP2CustomMeesage", "You have been teleported to PvP Zone 2!");
		GM_TRADE_RESTRICTED_ITEMS = customServerConfig.getBoolean("GMTradeRestrictedItems", false);
		GM_RESTART_FIGHTING = customServerConfig.getBoolean("GMRestartFighting", false);
		PM_MESSAGE_ON_START = customServerConfig.getBoolean("PMWelcomeShow", false);
		SERVER_TIME_ON_START = customServerConfig.getBoolean("ShowServerTimeOnStart", false);
		PM_SERVER_NAME = customServerConfig.getString("PMServerName", "Server");
		PM_TEXT1 = customServerConfig.getString("PMText1", "Have Fun and Nice Stay on");
		PM_TEXT2 = customServerConfig.getString("PMText2", "Vote for us every 24h");
		NEW_PLAYER_EFFECT = customServerConfig.getBoolean("NewPlayerEffect", true);
	}
	
	public static void loadPvpConfig()
	{
		final PropertiesParser pvpConfig = new PropertiesParser(PVP_CONFIG_FILE);
		KARMA_MIN_KARMA = pvpConfig.getInt("MinKarma", 240);
		KARMA_MAX_KARMA = pvpConfig.getInt("MaxKarma", 10000);
		KARMA_XP_DIVIDER = pvpConfig.getInt("XPDivider", 260);
		KARMA_LOST_BASE = pvpConfig.getInt("BaseKarmaLost", 0);
		KARMA_DROP_GM = pvpConfig.getBoolean("CanGMDropEquipment", false);
		KARMA_AWARD_PK_KILL = pvpConfig.getBoolean("AwardPKKillPVPPoint", true);
		KARMA_PK_LIMIT = pvpConfig.getInt("MinimumPKRequiredToDrop", 5);
		KARMA_NONDROPPABLE_PET_ITEMS = pvpConfig.getString("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
		KARMA_NONDROPPABLE_ITEMS = pvpConfig.getString("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");
		KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
		for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(","))
		{
			KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
		}
		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
		for (String id : KARMA_NONDROPPABLE_ITEMS.split(","))
		{
			KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
		}
		PVP_NORMAL_TIME = pvpConfig.getInt("PvPVsNormalTime", 15000);
		PVP_PVP_TIME = pvpConfig.getInt("PvPVsPvPTime", 30000);
		ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = pvpConfig.getBoolean("AltKarmaPlayerCanBeKilledInPeaceZone", false);
		ALT_GAME_KARMA_PLAYER_CAN_SHOP = pvpConfig.getBoolean("AltKarmaPlayerCanShop", true);
		ALT_GAME_KARMA_PLAYER_CAN_USE_GK = pvpConfig.getBoolean("AltKarmaPlayerCanUseGK", false);
		ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = pvpConfig.getBoolean("AltKarmaPlayerCanTeleport", true);
		ALT_GAME_KARMA_PLAYER_CAN_TRADE = pvpConfig.getBoolean("AltKarmaPlayerCanTrade", true);
		ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = pvpConfig.getBoolean("AltKarmaPlayerCanUseWareHouse", true);
		ALT_KARMA_TELEPORT_TO_FLORAN = pvpConfig.getBoolean("AltKarmaTeleportToFloran", false);
		PVP_REWARD_ENABLED = pvpConfig.getBoolean("PvpRewardEnabled", false);
		PVP_REWARD_ID = pvpConfig.getInt("PvpRewardItemId", 6392);
		PVP_REWARD_AMOUNT = pvpConfig.getInt("PvpRewardAmmount", 1);
		PK_REWARD_ENABLED = pvpConfig.getBoolean("PKRewardEnabled", false);
		PK_REWARD_ID = pvpConfig.getInt("PKRewardItemId", 6392);
		PK_REWARD_AMOUNT = pvpConfig.getInt("PKRewardAmmount", 1);
		REWARD_PROTECT = pvpConfig.getInt("RewardProtect", 1);
		PVP_COLOR_SYSTEM_ENABLED = pvpConfig.getBoolean("EnablePvPColorSystem", false);
		PVP_AMOUNT1 = pvpConfig.getInt("PvpAmount1", 500);
		PVP_AMOUNT2 = pvpConfig.getInt("PvpAmount2", 1000);
		PVP_AMOUNT3 = pvpConfig.getInt("PvpAmount3", 1500);
		PVP_AMOUNT4 = pvpConfig.getInt("PvpAmount4", 2500);
		PVP_AMOUNT5 = pvpConfig.getInt("PvpAmount5", 5000);
		NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + pvpConfig.getString("ColorForAmount1", "00FF00"));
		NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + pvpConfig.getString("ColorForAmount2", "00FF00"));
		NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + pvpConfig.getString("ColorForAmount3", "00FF00"));
		NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + pvpConfig.getString("ColorForAmount4", "00FF00"));
		NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + pvpConfig.getString("ColorForAmount5", "00FF00"));
		PK_COLOR_SYSTEM_ENABLED = pvpConfig.getBoolean("EnablePkColorSystem", false);
		PK_AMOUNT1 = pvpConfig.getInt("PkAmount1", 500);
		PK_AMOUNT2 = pvpConfig.getInt("PkAmount2", 1000);
		PK_AMOUNT3 = pvpConfig.getInt("PkAmount3", 1500);
		PK_AMOUNT4 = pvpConfig.getInt("PkAmount4", 2500);
		PK_AMOUNT5 = pvpConfig.getInt("PkAmount5", 5000);
		TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + pvpConfig.getString("TitleForAmount1", "00FF00"));
		TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + pvpConfig.getString("TitleForAmount2", "00FF00"));
		TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + pvpConfig.getString("TitleForAmount3", "00FF00"));
		TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + pvpConfig.getString("TitleForAmount4", "00FF00"));
		TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + pvpConfig.getString("TitleForAmount5", "00FF00"));
		FLAGED_PLAYER_USE_BUFFER = pvpConfig.getBoolean("AltKarmaFlagPlayerCanUseBuffer", false);
		FLAGED_PLAYER_CAN_USE_GK = pvpConfig.getBoolean("FlaggedPlayerCanUseGK", false);
		PVPEXPSP_SYSTEM = pvpConfig.getBoolean("AllowAddExpSpAtPvP", false);
		ADD_EXP = pvpConfig.getInt("AddExpAtPvp", 0);
		ADD_SP = pvpConfig.getInt("AddSpAtPvp", 0);
		ALLOW_SOE_IN_PVP = pvpConfig.getBoolean("AllowSoEInPvP", true);
		ALLOW_POTS_IN_PVP = pvpConfig.getBoolean("AllowPotsInPvP", true);
		ENABLE_PK_INFO = pvpConfig.getBoolean("EnablePkInfo", false);
		ANNOUNCE_ALL_KILL = pvpConfig.getBoolean("AnnounceAllKill", false);
		ANNOUNCE_PVP_KILL = pvpConfig.getBoolean("AnnouncePvPKill", false);
		ANNOUNCE_PK_KILL = pvpConfig.getBoolean("AnnouncePkKill", false);
		DUEL_SPAWN_X = pvpConfig.getInt("DuelSpawnX", -102495);
		DUEL_SPAWN_Y = pvpConfig.getInt("DuelSpawnY", -209023);
		DUEL_SPAWN_Z = pvpConfig.getInt("DuelSpawnZ", -3326);
		PVP_PK_TITLE = pvpConfig.getBoolean("PvpPkTitle", false);
		PVP_TITLE_PREFIX = pvpConfig.getString("PvPTitlePrefix", " ");
		PK_TITLE_PREFIX = pvpConfig.getString("PkTitlePrefix", " | ");
		WAR_LEGEND_AURA = pvpConfig.getBoolean("WarLegendAura", false);
		KILLS_TO_GET_WAR_LEGEND_AURA = pvpConfig.getInt("KillsToGetWarLegendAura", 30);
		ANTI_FARM_ENABLED = pvpConfig.getBoolean("AntiFarmEnabled", false);
		ANTI_FARM_CLAN_ALLY_ENABLED = pvpConfig.getBoolean("AntiFarmClanAlly", false);
		ANTI_FARM_LVL_DIFF_ENABLED = pvpConfig.getBoolean("AntiFarmLvlDiff", false);
		ANTI_FARM_MAX_LVL_DIFF = pvpConfig.getInt("AntiFarmMaxLvlDiff", 40);
		ANTI_FARM_PDEF_DIFF_ENABLED = pvpConfig.getBoolean("AntiFarmPdefDiff", false);
		ANTI_FARM_MAX_PDEF_DIFF = pvpConfig.getInt("AntiFarmMaxPdefDiff", 300);
		ANTI_FARM_PATK_DIFF_ENABLED = pvpConfig.getBoolean("AntiFarmPatkDiff", false);
		ANTI_FARM_MAX_PATK_DIFF = pvpConfig.getInt("AntiFarmMaxPatkDiff", 300);
		ANTI_FARM_PARTY_ENABLED = pvpConfig.getBoolean("AntiFarmParty", false);
		ANTI_FARM_IP_ENABLED = pvpConfig.getBoolean("AntiFarmIP", false);
		ANTI_FARM_SUMMON = pvpConfig.getBoolean("AntiFarmSummon", false);
	}
	
	public static void loadOlympConfig()
	{
		final PropertiesParser olympiadConfig = new PropertiesParser(OLYMP_CONFIG_FILE);
		ALT_OLY_START_TIME = olympiadConfig.getInt("AltOlyStartTime", 18);
		ALT_OLY_MIN = olympiadConfig.getInt("AltOlyMin", 0);
		ALT_OLY_CPERIOD = olympiadConfig.getLong("AltOlyCPeriod", 21600000);
		ALT_OLY_BATTLE = olympiadConfig.getLong("AltOlyBattle", 360000);
		ALT_OLY_WPERIOD = olympiadConfig.getLong("AltOlyWPeriod", 604800000);
		ALT_OLY_VPERIOD = olympiadConfig.getLong("AltOlyVPeriod", 86400000);
		ALT_OLY_CLASSED = olympiadConfig.getInt("AltOlyClassedParticipants", 5);
		ALT_OLY_NONCLASSED = olympiadConfig.getInt("AltOlyNonClassedParticipants", 9);
		ALT_OLY_BATTLE_REWARD_ITEM = olympiadConfig.getInt("AltOlyBattleRewItem", 6651);
		ALT_OLY_CLASSED_RITEM_C = olympiadConfig.getInt("AltOlyClassedRewItemCount", 50);
		ALT_OLY_NONCLASSED_RITEM_C = olympiadConfig.getInt("AltOlyNonClassedRewItemCount", 30);
		ALT_OLY_COMP_RITEM = olympiadConfig.getInt("AltOlyCompRewItem", 6651);
		ALT_OLY_GP_PER_POINT = olympiadConfig.getInt("AltOlyGPPerPoint", 1000);
		ALT_OLY_MIN_POINT_FOR_EXCH = olympiadConfig.getInt("AltOlyMinPointForExchange", 50);
		ALT_OLY_HERO_POINTS = olympiadConfig.getInt("AltOlyHeroPoints", 100);
		ALT_OLY_RESTRICTED_ITEMS = olympiadConfig.getString("AltOlyRestrictedItems", "0");
		LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>();
		for (String id : ALT_OLY_RESTRICTED_ITEMS.split(","))
		{
			LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
		}
		ALLOW_EVENTS_DURING_OLY = olympiadConfig.getBoolean("AllowEventsDuringOly", false);
		ALT_OLY_RECHARGE_SKILLS = olympiadConfig.getBoolean("AltOlyRechargeSkills", false);
		REMOVE_CUBIC_OLYMPIAD = olympiadConfig.getBoolean("RemoveCubicOlympiad", false);
		ALT_OLY_NUMBER_HEROS_EACH_CLASS = olympiadConfig.getInt("AltOlyNumberHerosEachClass", 1);
		ALT_OLY_LOG_FIGHTS = olympiadConfig.getBoolean("AlyOlyLogFights", false);
		ALT_OLY_SHOW_MONTHLY_WINNERS = olympiadConfig.getBoolean("AltOlyShowMonthlyWinners", true);
		ALT_OLY_ANNOUNCE_GAMES = olympiadConfig.getBoolean("AltOlyAnnounceGames", true);
		LIST_OLY_RESTRICTED_SKILLS = new ArrayList<>();
		for (String id : olympiadConfig.getString("AltOlyRestrictedSkills", "0").split(","))
		{
			LIST_OLY_RESTRICTED_SKILLS.add(Integer.parseInt(id));
		}
		ALT_OLY_AUGMENT_ALLOW = olympiadConfig.getBoolean("AltOlyAugmentAllow", true);
		ALT_OLY_TELEPORT_COUNTDOWN = olympiadConfig.getInt("AltOlyTeleportCountDown", 120);
		ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS = olympiadConfig.getBoolean("AltOlyUseCustomPeriodSettings", false);
		ALT_OLY_PERIOD = OlympiadPeriod.valueOf(olympiadConfig.getString("AltOlyPeriod", "MONTH"));
		ALT_OLY_PERIOD_MULTIPLIER = olympiadConfig.getInt("AltOlyPeriodMultiplier", 1);
		ALT_OLY_COMPETITION_DAYS = new ArrayList<>();
		for (String s : olympiadConfig.getString("AltOlyCompetitionDays", "1,2,3,4,5,6,7").split(","))
		{
			ALT_OLY_COMPETITION_DAYS.add(Integer.parseInt(s));
		}
	}
	
	public static void loadEnchantConfig()
	{
		final PropertiesParser enchantConfig = new PropertiesParser(ENCHANT_CONFIG_FILE);
		String[] propertySplit = enchantConfig.getString("NormalWeaponEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					NORMAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("BlessWeaponEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					BLESS_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("CrystalWeaponEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					CRYSTAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("NormalArmorEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					NORMAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("BlessArmorEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					BLESS_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("CrystalArmorEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					CRYSTAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("NormalJewelryEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					NORMAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("BlessJewelryEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					BLESS_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		propertySplit = enchantConfig.getString("CrystalJewelryEnchantLevel", "").split(";");
		for (String readData : propertySplit)
		{
			final String[] writeData = readData.split(",");
			if (writeData.length != 2)
			{
				LOGGER.info("invalid config property");
			}
			else
			{
				try
				{
					CRYSTAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
				}
				catch (NumberFormatException nfe)
				{
					if (!readData.equals(""))
					{
						LOGGER.info("invalid config property");
					}
				}
			}
		}
		ENCHANT_SAFE_MAX = enchantConfig.getInt("EnchantSafeMax", 3);
		ENCHANT_SAFE_MAX_FULL = enchantConfig.getInt("EnchantSafeMaxFull", 4);
		ENCHANT_WEAPON_MAX = enchantConfig.getInt("EnchantWeaponMax", 25);
		ENCHANT_ARMOR_MAX = enchantConfig.getInt("EnchantArmorMax", 25);
		ENCHANT_JEWELRY_MAX = enchantConfig.getInt("EnchantJewelryMax", 25);
		CRYSTAL_ENCHANT_MIN = enchantConfig.getInt("CrystalEnchantMin", 20);
		CRYSTAL_ENCHANT_MAX = enchantConfig.getInt("CrystalEnchantMax", 0);
		ENABLE_DWARF_ENCHANT_BONUS = enchantConfig.getBoolean("EnableDwarfEnchantBonus", false);
		DWARF_ENCHANT_MIN_LEVEL = enchantConfig.getInt("DwarfEnchantMinLevel", 80);
		DWARF_ENCHANT_BONUS = enchantConfig.getInt("DwarfEnchantBonus", 15);
		AUGMENTATION_NG_SKILL_CHANCE = enchantConfig.getInt("AugmentationNGSkillChance", 15);
		AUGMENTATION_MID_SKILL_CHANCE = enchantConfig.getInt("AugmentationMidSkillChance", 30);
		AUGMENTATION_HIGH_SKILL_CHANCE = enchantConfig.getInt("AugmentationHighSkillChance", 45);
		AUGMENTATION_TOP_SKILL_CHANCE = enchantConfig.getInt("AugmentationTopSkillChance", 60);
		AUGMENTATION_BASESTAT_CHANCE = enchantConfig.getInt("AugmentationBaseStatChance", 1);
		AUGMENTATION_NG_GLOW_CHANCE = enchantConfig.getInt("AugmentationNGGlowChance", 0);
		AUGMENTATION_MID_GLOW_CHANCE = enchantConfig.getInt("AugmentationMidGlowChance", 40);
		AUGMENTATION_HIGH_GLOW_CHANCE = enchantConfig.getInt("AugmentationHighGlowChance", 70);
		AUGMENTATION_TOP_GLOW_CHANCE = enchantConfig.getInt("AugmentationTopGlowChance", 100);
		DELETE_AUGM_PASSIVE_ON_CHANGE = enchantConfig.getBoolean("DeleteAgmentPassiveEffectOnChangeWep", true);
		DELETE_AUGM_ACTIVE_ON_CHANGE = enchantConfig.getBoolean("DeleteAgmentActiveEffectOnChangeWep", true);
		ENCHANT_HERO_WEAPON = enchantConfig.getBoolean("EnableEnchantHeroWeapons", false);
		SOUL_CRYSTAL_BREAK_CHANCE = enchantConfig.getInt("SoulCrystalBreakChance", 10);
		SOUL_CRYSTAL_LEVEL_CHANCE = enchantConfig.getInt("SoulCrystalLevelChance", 32);
		SOUL_CRYSTAL_MAX_LEVEL = enchantConfig.getInt("SoulCrystalMaxLevel", 13);
		CUSTOM_ENCHANT_VALUE = enchantConfig.getInt("CustomEnchantValue", 1);
		ALT_OLY_ENCHANT_LIMIT = enchantConfig.getInt("AltOlyMaxEnchant", -1);
		BREAK_ENCHANT = enchantConfig.getInt("BreakEnchant", 0);
		MAX_ITEM_ENCHANT_KICK = enchantConfig.getInt("EnchantKick", 0);
		GM_OVER_ENCHANT = enchantConfig.getInt("GMOverEnchant", 0);
	}
	
	public static void loadFloodConfig()
	{
		FLOOD_PROTECTOR_USE_ITEM = new FloodProtectorConfig("UseItemFloodProtector");
		FLOOD_PROTECTOR_ROLL_DICE = new FloodProtectorConfig("RollDiceFloodProtector");
		FLOOD_PROTECTOR_FIREWORK = new FloodProtectorConfig("FireworkFloodProtector");
		FLOOD_PROTECTOR_ITEM_PET_SUMMON = new FloodProtectorConfig("ItemPetSummonFloodProtector");
		FLOOD_PROTECTOR_HERO_VOICE = new FloodProtectorConfig("HeroVoiceFloodProtector");
		FLOOD_PROTECTOR_GLOBAL_CHAT = new FloodProtectorConfig("GlobalChatFloodProtector");
		FLOOD_PROTECTOR_SUBCLASS = new FloodProtectorConfig("SubclassFloodProtector");
		FLOOD_PROTECTOR_DROP_ITEM = new FloodProtectorConfig("DropItemFloodProtector");
		FLOOD_PROTECTOR_SERVER_BYPASS = new FloodProtectorConfig("ServerBypassFloodProtector");
		FLOOD_PROTECTOR_MULTISELL = new FloodProtectorConfig("MultiSellFloodProtector");
		FLOOD_PROTECTOR_TRANSACTION = new FloodProtectorConfig("TransactionFloodProtector");
		FLOOD_PROTECTOR_MANUFACTURE = new FloodProtectorConfig("ManufactureFloodProtector");
		FLOOD_PROTECTOR_MANOR = new FloodProtectorConfig("ManorFloodProtector");
		FLOOD_PROTECTOR_CHARACTER_SELECT = new FloodProtectorConfig("CharacterSelectFloodProtector");
		FLOOD_PROTECTOR_UNKNOWN_PACKETS = new FloodProtectorConfig("UnknownPacketsFloodProtector");
		FLOOD_PROTECTOR_PARTY_INVITATION = new FloodProtectorConfig("PartyInvitationFloodProtector");
		FLOOD_PROTECTOR_SAY_ACTION = new FloodProtectorConfig("SayActionFloodProtector");
		FLOOD_PROTECTOR_MOVE_ACTION = new FloodProtectorConfig("MoveActionFloodProtector");
		FLOOD_PROTECTOR_GENERIC_ACTION = new FloodProtectorConfig("GenericActionFloodProtector", true);
		FLOOD_PROTECTOR_MACRO = new FloodProtectorConfig("MacroFloodProtector", true);
		FLOOD_PROTECTOR_POTION = new FloodProtectorConfig("PotionFloodProtector", true);
		
		final PropertiesParser floodProtectConfig = new PropertiesParser(PROTECT_FLOOD_CONFIG_FILE);
		loadFloodProtectorConfigs(floodProtectConfig);
	}
	
	/**
	 * Loads single flood protector configuration.
	 * @param properties PropertiesParser file reader
	 * @param config flood protector configuration instance
	 * @param configString flood protector configuration string that determines for which flood protector configuration should be read
	 * @param defaultInterval default flood protector interval
	 */
	private static void loadFloodProtectorConfig(PropertiesParser properties, FloodProtectorConfig config, String configString, float defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = properties.getFloat(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval);
		config.LOG_FLOODING = properties.getBoolean(StringUtil.concat("FloodProtector", configString, "LogFlooding"), false);
		config.PUNISHMENT_LIMIT = properties.getInt(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), 0);
		config.PUNISHMENT_TYPE = properties.getString(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = properties.getInt(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), 0);
	}
	
	public static void loadProtectedOtherConfig()
	{
		final PropertiesParser protectedOtherConfig = new PropertiesParser(PROTECT_OTHER_CONFIG_FILE);
		CHECK_NAME_ON_LOGIN = protectedOtherConfig.getBoolean("CheckNameOnEnter", true);
		CHECK_SKILLS_ON_ENTER = protectedOtherConfig.getBoolean("CheckSkillsOnEnter", true);
		L2WALKER_PROTECTION = protectedOtherConfig.getBoolean("L2WalkerProtection", false);
		PROTECTED_ENCHANT = protectedOtherConfig.getBoolean("ProtectorEnchant", false);
		ONLY_GM_TELEPORT_FREE = protectedOtherConfig.getBoolean("OnlyGMTeleportFree", false);
		ONLY_GM_ITEMS_FREE = protectedOtherConfig.getBoolean("OnlyGMItemsFree", false);
		BYPASS_VALIDATION = protectedOtherConfig.getBoolean("BypassValidation", true);
		ALLOW_DUALBOX_OLY = protectedOtherConfig.getBoolean("AllowDualBoxInOly", true);
		ALLOW_DUALBOX_EVENT = protectedOtherConfig.getBoolean("AllowDualBoxInEvent", true);
		ALLOWED_BOXES = protectedOtherConfig.getInt("AllowedBoxes", 99);
		ALLOW_DUALBOX = protectedOtherConfig.getBoolean("AllowDualBox", true);
	}
	
	public static void loadPhysicsConfig()
	{
		final PropertiesParser physicsSetting = new PropertiesParser(PHYSICS_CONFIG_FILE);
		ENABLE_CLASS_DAMAGE_SETTINGS = physicsSetting.getBoolean("EnableClassDamageSettings", true);
		ENABLE_CLASS_DAMAGE_SETTINGS_IN_OLY = physicsSetting.getBoolean("EnableClassDamageSettingsInOly", true);
		ENABLE_CLASS_DAMAGE_LOGGER = physicsSetting.getBoolean("EnableClassDamageLogger", false);
		BLOW_ATTACK_FRONT = physicsSetting.getInt("BlowAttackFront", 50);
		BLOW_ATTACK_SIDE = physicsSetting.getInt("BlowAttackSide", 60);
		BLOW_ATTACK_BEHIND = physicsSetting.getInt("BlowAttackBehind", 70);
		BACKSTAB_ATTACK_FRONT = physicsSetting.getInt("BackstabAttackFront", 0);
		BACKSTAB_ATTACK_SIDE = physicsSetting.getInt("BackstabAttackSide", 0);
		BACKSTAB_ATTACK_BEHIND = physicsSetting.getInt("BackstabAttackBehind", 70);
		MAX_PATK_SPEED = physicsSetting.getInt("MaxPAtkSpeed", 1500);
		MAX_MATK_SPEED = physicsSetting.getInt("MaxMAtkSpeed", 1999);
		if (MAX_PATK_SPEED < 1)
		{
			MAX_PATK_SPEED = Integer.MAX_VALUE;
		}
		
		if (MAX_MATK_SPEED < 1)
		{
			MAX_MATK_SPEED = Integer.MAX_VALUE;
		}
		MAX_PCRIT_RATE = physicsSetting.getInt("MaxPCritRate", 500);
		MAX_MCRIT_RATE = physicsSetting.getInt("MaxMCritRate", 300);
		MCRIT_RATE_MUL = physicsSetting.getFloat("McritMulDif", 1f);
		MAGIC_CRITICAL_POWER = physicsSetting.getFloat("MagicCriticalPower", 3f);
		STUN_CHANCE_MODIFIER = physicsSetting.getFloat("StunChanceModifier", 1f);
		BLEED_CHANCE_MODIFIER = physicsSetting.getFloat("BleedChanceModifier", 1f);
		POISON_CHANCE_MODIFIER = physicsSetting.getFloat("PoisonChanceModifier", 1f);
		PARALYZE_CHANCE_MODIFIER = physicsSetting.getFloat("ParalyzeChanceModifier", 1f);
		ROOT_CHANCE_MODIFIER = physicsSetting.getFloat("RootChanceModifier", 1f);
		SLEEP_CHANCE_MODIFIER = physicsSetting.getFloat("SleepChanceModifier", 1f);
		FEAR_CHANCE_MODIFIER = physicsSetting.getFloat("FearChanceModifier", 1f);
		CONFUSION_CHANCE_MODIFIER = physicsSetting.getFloat("ConfusionChanceModifier", 1f);
		DEBUFF_CHANCE_MODIFIER = physicsSetting.getFloat("DebuffChanceModifier", 1f);
		BUFF_CHANCE_MODIFIER = physicsSetting.getFloat("BuffChanceModifier", 1f);
		ALT_MAGES_PHYSICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltPDamageMages", 1f);
		ALT_MAGES_MAGICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltMDamageMages", 1f);
		ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltPDamageFighters", 1f);
		ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltMDamageFighters", 1f);
		ALT_PETS_PHYSICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltPDamagePets", 1f);
		ALT_PETS_MAGICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltMDamagePets", 1f);
		ALT_NPC_PHYSICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltPDamageNpc", 1f);
		ALT_NPC_MAGICAL_DAMAGE_MULTI = physicsSetting.getFloat("AltMDamageNpc", 1f);
		ALT_DAGGER_DMG_VS_HEAVY = physicsSetting.getFloat("DaggerVSHeavy", 2.5f);
		ALT_DAGGER_DMG_VS_ROBE = physicsSetting.getFloat("DaggerVSRobe", 1.8f);
		ALT_DAGGER_DMG_VS_LIGHT = physicsSetting.getFloat("DaggerVSLight", 2f);
		RUN_SPD_BOOST = physicsSetting.getInt("RunSpeedBoost", 0);
		MAX_RUN_SPEED = physicsSetting.getInt("MaxRunSpeed", 250);
		ALLOW_RAID_LETHAL = physicsSetting.getBoolean("AllowLethalOnRaids", false);
		ALLOW_LETHAL_PROTECTION_MOBS = physicsSetting.getBoolean("AllowLethalProtectionMobs", false);
		LETHAL_PROTECTED_MOBS = physicsSetting.getString("LethalProtectedMobs", "");
		LIST_LETHAL_PROTECTED_MOBS = new ArrayList<>();
		for (String id : LETHAL_PROTECTED_MOBS.split(","))
		{
			LIST_LETHAL_PROTECTED_MOBS.add(Integer.parseInt(id));
		}
		SEND_SKILLS_CHANCE_TO_PLAYERS = physicsSetting.getBoolean("SendSkillsChanceToPlayers", false);
		REMOVE_WEAPON_SUBCLASS = physicsSetting.getBoolean("RemoveWeaponSubclass", false);
		REMOVE_CHEST_SUBCLASS = physicsSetting.getBoolean("RemoveChestSubclass", false);
		REMOVE_LEG_SUBCLASS = physicsSetting.getBoolean("RemoveLegSubclass", false);
		DISABLE_BOW_CLASSES_STRING = physicsSetting.getString("DisableBowForClasses", "");
		DISABLE_BOW_CLASSES = new ArrayList<>();
		for (String class_id : DISABLE_BOW_CLASSES_STRING.split(","))
		{
			if (!class_id.equals(""))
			{
				DISABLE_BOW_CLASSES.add(Integer.parseInt(class_id));
			}
		}
		LEAVE_BUFFS_ON_DIE = physicsSetting.getBoolean("LeaveBuffsOnDie", true);
	}
	
	public static void loadgeodataConfig()
	{
		final PropertiesParser geoengineConfig = new PropertiesParser(GEOENGINE_CONFIG_FILE);
		GEODATA_PATH = geoengineConfig.getString("GeoDataPath", "./data/geodata/");
		COORD_SYNCHRONIZE = geoengineConfig.getInt("CoordSynchronize", -1);
		PART_OF_CHARACTER_HEIGHT = geoengineConfig.getInt("PartOfCharacterHeight", 75);
		MAX_OBSTACLE_HEIGHT = geoengineConfig.getInt("MaxObstacleHeight", 32);
		PATHFINDING = geoengineConfig.getBoolean("PathFinding", true);
		PATHFIND_BUFFERS = geoengineConfig.getString("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
		BASE_WEIGHT = geoengineConfig.getInt("BaseWeight", 10);
		DIAGONAL_WEIGHT = geoengineConfig.getInt("DiagonalWeight", 14);
		OBSTACLE_MULTIPLIER = geoengineConfig.getInt("ObstacleMultiplier", 10);
		HEURISTIC_WEIGHT = geoengineConfig.getInt("HeuristicWeight", 20);
		MAX_ITERATIONS = geoengineConfig.getInt("MaxIterations", 3500);
		FALL_DAMAGE = geoengineConfig.getBoolean("FallDamage", false);
		ALLOW_WATER = geoengineConfig.getBoolean("AllowWater", false);
	}
	
	public static void loadBossConfig()
	{
		final PropertiesParser bossConfig = new PropertiesParser(RAIDBOSS_CONFIG_FILE);
		ALT_RAIDS_STATS_BONUS = bossConfig.getBoolean("AltRaidsStatsBonus", true);
		RBLOCKRAGE = bossConfig.getInt("RBlockRage", 5000);
		if ((RBLOCKRAGE > 0) && (RBLOCKRAGE < 100))
		{
			LOGGER.info("ATTENTION: RBlockRage, if enabled (>0), must be >=100");
			LOGGER.info("	-- RBlockRage setted to 100 by default");
			RBLOCKRAGE = 100;
		}
		RBS_SPECIFIC_LOCK_RAGE = new HashMap<>();
		final String RaidBossesSpecificLockRage = bossConfig.getString("RaidBossesSpecificLockRage", "");
		if (!RaidBossesSpecificLockRage.equals(""))
		{
			final String[] lockedBosses = RaidBossesSpecificLockRage.split(";");
			for (String actualBossRage : lockedBosses)
			{
				final String[] bossRage = actualBossRage.split(",");
				int rage = Integer.parseInt(bossRage[1]);
				if ((rage > 0) && (rage < 100))
				{
					LOGGER.info("ATTENTION: RaidBossesSpecificLockRage Value for boss " + bossRage[0] + ", if enabled (>0), must be >=100");
					LOGGER.info("	-- RaidBossesSpecificLockRage Value for boss " + bossRage[0] + " setted to 100 by default");
					rage = 100;
				}
				RBS_SPECIFIC_LOCK_RAGE.put(Integer.parseInt(bossRage[0]), rage);
			}
		}
		PLAYERS_CAN_HEAL_RB = bossConfig.getBoolean("PlayersCanHealRb", true);
		ALLOW_DIRECT_TP_TO_BOSS_ROOM = bossConfig.getBoolean("AllowDirectTeleportToBossRoom", false);
		// Antharas
		ANTHARAS_OLD = bossConfig.getBoolean("AntharasOldScript", true);
		ANTHARAS_CLOSE = bossConfig.getInt("AntharasClose", 1200);
		ANTHARAS_DESPAWN_TIME = bossConfig.getInt("AntharasDespawnTime", 240);
		ANTHARAS_RESP_FIRST = bossConfig.getInt("AntharasRespFirst", 192);
		ANTHARAS_RESP_SECOND = bossConfig.getInt("AntharasRespSecond", 145);
		ANTHARAS_WAIT_TIME = bossConfig.getInt("AntharasWaitTime", 30);
		ANTHARAS_POWER_MULTIPLIER = bossConfig.getFloat("AntharasPowerMultiplier", 1f);
		// Baium
		BAIUM_SLEEP = bossConfig.getInt("BaiumSleep", 1800);
		BAIUM_RESP_FIRST = bossConfig.getInt("BaiumRespFirst", 121);
		BAIUM_RESP_SECOND = bossConfig.getInt("BaiumRespSecond", 8);
		BAIUM_POWER_MULTIPLIER = bossConfig.getFloat("BaiumPowerMultiplier", 1f);
		// Core
		CORE_RESP_MINION = bossConfig.getInt("CoreRespMinion", 60);
		CORE_RESP_FIRST = bossConfig.getInt("CoreRespFirst", 37);
		CORE_RESP_SECOND = bossConfig.getInt("CoreRespSecond", 42);
		CORE_LEVEL = bossConfig.getInt("CoreLevel", 0);
		CORE_RING_CHANCE = bossConfig.getInt("CoreRingChance", 0);
		CORE_POWER_MULTIPLIER = bossConfig.getFloat("CorePowerMultiplier", 1f);
		// Queen Ant
		QA_RESP_NURSE = bossConfig.getInt("QueenAntRespNurse", 60);
		QA_RESP_ROYAL = bossConfig.getInt("QueenAntRespRoyal", 120);
		QA_RESP_FIRST = bossConfig.getInt("QueenAntRespFirst", 19);
		QA_RESP_SECOND = bossConfig.getInt("QueenAntRespSecond", 35);
		QA_LEVEL = bossConfig.getInt("QALevel", 0);
		QA_RING_CHANCE = bossConfig.getInt("QARingChance", 0);
		QA_POWER_MULTIPLIER = bossConfig.getFloat("QueenAntPowerMultiplier", 1f);
		// Zaken
		ZAKEN_RESP_FIRST = bossConfig.getInt("ZakenRespFirst", 60);
		ZAKEN_RESP_SECOND = bossConfig.getInt("ZakenRespSecond", 8);
		ZAKEN_LEVEL = bossConfig.getInt("ZakenLevel", 0);
		ZAKEN_EARRING_CHANCE = bossConfig.getInt("ZakenEarringChance", 0);
		ZAKEN_POWER_MULTIPLIER = bossConfig.getFloat("ZakenPowerMultiplier", 1f);
		// Orfen
		ORFEN_RESP_FIRST = bossConfig.getInt("OrfenRespFirst", 20);
		ORFEN_RESP_SECOND = bossConfig.getInt("OrfenRespSecond", 8);
		ORFEN_LEVEL = bossConfig.getInt("OrfenLevel", 0);
		ORFEN_EARRING_CHANCE = bossConfig.getInt("OrfenEarringChance", 0);
		ORFEN_POWER_MULTIPLIER = bossConfig.getFloat("OrfenPowerMultiplier", 1f);
		// Valakas
		VALAKAS_RESP_FIRST = bossConfig.getInt("ValakasRespFirst", 192);
		VALAKAS_RESP_SECOND = bossConfig.getInt("ValakasRespSecond", 44);
		VALAKAS_WAIT_TIME = bossConfig.getInt("ValakasWaitTime", 30);
		VALAKAS_POWER_MULTIPLIER = bossConfig.getFloat("ValakasPowerMultiplier", 1f);
		VALAKAS_DESPAWN_TIME = bossConfig.getInt("ValakasDespawnTime", 15);
		// Frintezza
		FRINTEZZA_RESP_FIRST = bossConfig.getInt("FrintezzaRespFirst", 48);
		FRINTEZZA_RESP_SECOND = bossConfig.getInt("FrintezzaRespSecond", 8);
		FRINTEZZA_POWER_MULTIPLIER = bossConfig.getFloat("FrintezzaPowerMultiplier", 1f);
		BYPASS_FRINTEZZA_PARTIES_CHECK = bossConfig.getBoolean("BypassPartiesCheck", false);
		FRINTEZZA_MIN_PARTIES = bossConfig.getInt("FrintezzaMinParties", 4);
		FRINTEZZA_MAX_PARTIES = bossConfig.getInt("FrintezzaMaxParties", 5);
		LEVEL_DIFF_MULTIPLIER_MINION = bossConfig.getFloat("LevelDiffMultiplierMinion", 0.5f);
		RAID_INFO_IDS = bossConfig.getString("RaidInfoIDs", "");
		RAID_INFO_IDS_LIST = new ArrayList<>();
		for (String id : RAID_INFO_IDS.split(","))
		{
			RAID_INFO_IDS_LIST.add(Integer.parseInt(id));
		}
		// High Priestess van Halter
		HPH_FIXINTERVALOFHALTER = bossConfig.getInt("FixIntervalOfHalter", 172800);
		if ((HPH_FIXINTERVALOFHALTER < 300) || (HPH_FIXINTERVALOFHALTER > 864000))
		{
			HPH_FIXINTERVALOFHALTER = 172800;
		}
		HPH_FIXINTERVALOFHALTER *= 6000;
		HPH_RANDOMINTERVALOFHALTER = bossConfig.getInt("RandomIntervalOfHalter", 86400);
		if ((HPH_RANDOMINTERVALOFHALTER < 300) || (HPH_RANDOMINTERVALOFHALTER > 864000))
		{
			HPH_RANDOMINTERVALOFHALTER = 86400;
		}
		HPH_RANDOMINTERVALOFHALTER *= 6000;
		HPH_APPTIMEOFHALTER = bossConfig.getInt("AppTimeOfHalter", 20);
		if ((HPH_APPTIMEOFHALTER < 5) || (HPH_APPTIMEOFHALTER > 60))
		{
			HPH_APPTIMEOFHALTER = 20;
		}
		HPH_APPTIMEOFHALTER *= 6000;
		HPH_ACTIVITYTIMEOFHALTER = bossConfig.getInt("ActivityTimeOfHalter", 21600);
		if ((HPH_ACTIVITYTIMEOFHALTER < 7200) || (HPH_ACTIVITYTIMEOFHALTER > 86400))
		{
			HPH_ACTIVITYTIMEOFHALTER = 21600;
		}
		HPH_ACTIVITYTIMEOFHALTER *= 1000;
		HPH_FIGHTTIMEOFHALTER = bossConfig.getInt("FightTimeOfHalter", 7200);
		if ((HPH_FIGHTTIMEOFHALTER < 7200) || (HPH_FIGHTTIMEOFHALTER > 21600))
		{
			HPH_FIGHTTIMEOFHALTER = 7200;
		}
		HPH_FIGHTTIMEOFHALTER *= 6000;
		HPH_CALLROYALGUARDHELPERCOUNT = bossConfig.getInt("CallRoyalGuardHelperCount", 6);
		if ((HPH_CALLROYALGUARDHELPERCOUNT < 1) || (HPH_CALLROYALGUARDHELPERCOUNT > 6))
		{
			HPH_CALLROYALGUARDHELPERCOUNT = 6;
		}
		HPH_CALLROYALGUARDHELPERINTERVAL = bossConfig.getInt("CallRoyalGuardHelperInterval", 10);
		if ((HPH_CALLROYALGUARDHELPERINTERVAL < 1) || (HPH_CALLROYALGUARDHELPERINTERVAL > 60))
		{
			HPH_CALLROYALGUARDHELPERINTERVAL = 10;
		}
		HPH_CALLROYALGUARDHELPERINTERVAL *= 6000;
		HPH_INTERVALOFDOOROFALTER = bossConfig.getInt("IntervalOfDoorOfAlter", 5400);
		if ((HPH_INTERVALOFDOOROFALTER < 60) || (HPH_INTERVALOFDOOROFALTER > 5400))
		{
			HPH_INTERVALOFDOOROFALTER = 5400;
		}
		HPH_INTERVALOFDOOROFALTER *= 6000;
		HPH_TIMEOFLOCKUPDOOROFALTAR = bossConfig.getInt("TimeOfLockUpDoorOfAltar", 180);
		if ((HPH_TIMEOFLOCKUPDOOROFALTAR < 60) || (HPH_TIMEOFLOCKUPDOOROFALTAR > 600))
		{
			HPH_TIMEOFLOCKUPDOOROFALTAR = 180;
		}
		HPH_TIMEOFLOCKUPDOOROFALTAR *= 6000;
	}
	
	public static void loadCharacterConfig()
	{
		final PropertiesParser characterConfig = new PropertiesParser(CHARACTER_CONFIG_FILE);
		AUTO_LOOT = characterConfig.getBoolean("AutoLoot", true);
		AUTO_LOOT_HERBS = characterConfig.getBoolean("AutoLootHerbs", true);
		AUTO_LOOT_BOSS = characterConfig.getBoolean("AutoLootBoss", true);
		AUTO_LEARN_SKILLS = characterConfig.getBoolean("AutoLearnSkills", false);
		AUTO_LEARN_DIVINE_INSPIRATION = characterConfig.getBoolean("AutoLearnDivineInspiration", false);
		LIFE_CRYSTAL_NEEDED = characterConfig.getBoolean("LifeCrystalNeeded", true);
		SP_BOOK_NEEDED = characterConfig.getBoolean("SpBookNeeded", true);
		ES_SP_BOOK_NEEDED = characterConfig.getBoolean("EnchantSkillSpBookNeeded", true);
		DIVINE_SP_BOOK_NEEDED = characterConfig.getBoolean("DivineInspirationSpBookNeeded", true);
		ALT_GAME_SKILL_LEARN = characterConfig.getBoolean("AltGameSkillLearn", false);
		ALLOWED_SUBCLASS = characterConfig.getInt("AllowedSubclass", 3);
		BASE_SUBCLASS_LEVEL = characterConfig.getByte("BaseSubclassLevel", (byte) 40);
		MAX_SUBCLASS_LEVEL = characterConfig.getByte("MaxSubclassLevel", (byte) 81);
		ALT_GAME_SUBCLASS_WITHOUT_QUESTS = characterConfig.getBoolean("AltSubClassWithoutQuests", false);
		ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE = characterConfig.getBoolean("AltRestoreEffectOnSub", false);
		ALT_PARTY_RANGE = characterConfig.getInt("AltPartyRange", 1500);
		ALT_WEIGHT_LIMIT = characterConfig.getDouble("AltWeightLimit", 1);
		ALT_GAME_DELEVEL = characterConfig.getBoolean("Delevel", true);
		ALT_GAME_MAGICFAILURES = characterConfig.getBoolean("MagicFailures", false);
		ALT_GAME_CANCEL_CAST = characterConfig.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || characterConfig.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
		ALT_GAME_CANCEL_BOW = characterConfig.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || characterConfig.getString("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
		ALT_GAME_SHIELD_BLOCKS = characterConfig.getBoolean("AltShieldBlocks", false);
		ALT_PERFECT_SHLD_BLOCK = characterConfig.getInt("AltPerfectShieldBlockRate", 10);
		ALT_GAME_MOB_ATTACK_AI = characterConfig.getBoolean("AltGameMobAttackAI", false);
		ALT_MOB_AGRO_IN_PEACEZONE = characterConfig.getBoolean("AltMobAgroInPeaceZone", true);
		ALT_GAME_FREIGHTS = characterConfig.getBoolean("AltGameFreights", false);
		ALT_GAME_FREIGHT_PRICE = characterConfig.getInt("AltGameFreightPrice", 1000);
		ALT_GAME_EXPONENT_XP = characterConfig.getFloat("AltGameExponentXp", 0f);
		ALT_GAME_EXPONENT_SP = characterConfig.getFloat("AltGameExponentSp", 0f);
		ALT_GAME_TIREDNESS = characterConfig.getBoolean("AltGameTiredness", false);
		ALT_GAME_FREE_TELEPORT = characterConfig.getBoolean("AltFreeTeleporting", false);
		ALT_RECOMMEND = characterConfig.getBoolean("AltRecommend", false);
		ALT_RECOMMENDATIONS_NUMBER = characterConfig.getInt("AltMaxRecommendationNumber", 255);
		MAX_CHARACTERS_NUMBER_PER_ACCOUNT = characterConfig.getInt("CharMaxNumber", 0);
		MAX_LEVEL_NEWBIE = characterConfig.getInt("MaxLevelNewbie", 20);
		MAX_LEVEL_NEWBIE_STATUS = characterConfig.getInt("MaxLevelNewbieStatus", 40);
		DISABLE_TUTORIAL = characterConfig.getBoolean("DisableTutorial", false);
		STARTING_ADENA = characterConfig.getInt("StartingAdena", 100);
		STARTING_AA = characterConfig.getInt("StartingAncientAdena", 0);
		CUSTOM_STARTER_ITEMS_ENABLED = characterConfig.getBoolean("CustomStarterItemsEnabled", false);
		if (CUSTOM_STARTER_ITEMS_ENABLED)
		{
			STARTING_CUSTOM_ITEMS_M.clear();
			String[] propertySplit = characterConfig.getString("StartingCustomItemsMage", "57,0").split(";");
			for (String reward : propertySplit)
			{
				final String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					LOGGER.warning("StartingCustomItemsMage[Config.load()]: invalid config property -> StartingCustomItemsMage \"" + reward + "\"");
				}
				else
				{
					try
					{
						STARTING_CUSTOM_ITEMS_M.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							LOGGER.warning("StartingCustomItemsMage[Config.load()]: invalid config property -> StartingCustomItemsMage \"" + reward + "\"");
						}
					}
				}
			}
			STARTING_CUSTOM_ITEMS_F.clear();
			propertySplit = characterConfig.getString("StartingCustomItemsFighter", "57,0").split(";");
			for (String reward : propertySplit)
			{
				final String[] rewardSplit = reward.split(",");
				if (rewardSplit.length != 2)
				{
					LOGGER.warning("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
				}
				else
				{
					try
					{
						STARTING_CUSTOM_ITEMS_F.add(new int[]
						{
							Integer.parseInt(rewardSplit[0]),
							Integer.parseInt(rewardSplit[1])
						});
					}
					catch (NumberFormatException nfe)
					{
						if (!reward.isEmpty())
						{
							LOGGER.warning("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
						}
					}
				}
			}
		}
		INVENTORY_MAXIMUM_NO_DWARF = characterConfig.getInt("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = characterConfig.getInt("MaximumSlotsForDwarf", 100);
		INVENTORY_MAXIMUM_GM = characterConfig.getInt("MaximumSlotsForGMPlayer", 250);
		MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
		WAREHOUSE_SLOTS_DWARF = characterConfig.getInt("MaximumWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_NO_DWARF = characterConfig.getInt("MaximumWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_CLAN = characterConfig.getInt("MaximumWarehouseSlotsForClan", 150);
		FREIGHT_SLOTS = characterConfig.getInt("MaximumFreightSlots", 20);
		MAX_PVTSTORE_SLOTS_DWARF = characterConfig.getInt("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = characterConfig.getInt("MaxPvtStoreSlotsOther", 4);
		HP_REGEN_MULTIPLIER = characterConfig.getDouble("HpRegenMultiplier", 100) / 100;
		MP_REGEN_MULTIPLIER = characterConfig.getDouble("MpRegenMultiplier", 100) / 100;
		CP_REGEN_MULTIPLIER = characterConfig.getDouble("CpRegenMultiplier", 100) / 100;
		ENABLE_KEYBOARD_MOVEMENT = characterConfig.getBoolean("KeyboardMovement", true);
		UNSTUCK_INTERVAL = characterConfig.getInt("UnstuckInterval", 300);
		PLAYER_SPAWN_PROTECTION = characterConfig.getInt("PlayerSpawnProtection", 0);
		PLAYER_TELEPORT_PROTECTION = characterConfig.getInt("PlayerTeleportProtection", 0);
		PLAYER_FAKEDEATH_UP_PROTECTION = characterConfig.getInt("PlayerFakeDeathUpProtection", 0);
		DEEPBLUE_DROP_RULES = characterConfig.getBoolean("UseDeepBlueDropRules", true);
		PARTY_XP_CUTOFF_METHOD = characterConfig.getString("PartyXpCutoffMethod", "percentage");
		PARTY_XP_CUTOFF_PERCENT = characterConfig.getDouble("PartyXpCutoffPercent", 3);
		PARTY_XP_CUTOFF_LEVEL = characterConfig.getInt("PartyXpCutoffLevel", 30);
		RESPAWN_RESTORE_CP = characterConfig.getDouble("RespawnRestoreCP", 0) / 100;
		RESPAWN_RESTORE_HP = characterConfig.getDouble("RespawnRestoreHP", 70) / 100;
		RESPAWN_RESTORE_MP = characterConfig.getDouble("RespawnRestoreMP", 70) / 100;
		RESPAWN_RANDOM_ENABLED = characterConfig.getBoolean("RespawnRandomInTown", false);
		RESPAWN_RANDOM_MAX_OFFSET = characterConfig.getInt("RespawnRandomMaxOffset", 50);
		PETITIONING_ALLOWED = characterConfig.getBoolean("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = characterConfig.getInt("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = characterConfig.getInt("MaxPetitionsPending", 25);
		DEATH_PENALTY_CHANCE = characterConfig.getInt("DeathPenaltyChance", 20);
		EFFECT_CANCELING = characterConfig.getBoolean("CancelLesserEffect", true);
		STORE_SKILL_COOLTIME = characterConfig.getBoolean("StoreSkillCooltime", true);
		BUFFS_MAX_AMOUNT = characterConfig.getByte("MaxBuffAmount", (byte) 24);
		DEBUFFS_MAX_AMOUNT = characterConfig.getByte("MaxDebuffAmount", (byte) 6);
		ENABLE_MODIFY_SKILL_DURATION = characterConfig.getBoolean("EnableModifySkillDuration", false);
		if (ENABLE_MODIFY_SKILL_DURATION)
		{
			SKILL_DURATION_LIST = new HashMap<>();
			String[] propertySplit;
			propertySplit = characterConfig.getString("SkillDurationList", "").split(";");
			for (String skill : propertySplit)
			{
				final String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
				{
					LOGGER.info("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
				}
				else
				{
					try
					{
						SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.equals(""))
						{
							LOGGER.info("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
						}
					}
				}
			}
		}
		ALLOW_CLASS_MASTERS = characterConfig.getBoolean("AllowClassMasters", false);
		CLASS_MASTER_STRIDER_UPDATE = characterConfig.getBoolean("AllowClassMastersStriderUpdate", false);
		ALLOW_CLASS_MASTERS_FIRST_CLASS = characterConfig.getBoolean("AllowClassMastersFirstClass", true);
		ALLOW_CLASS_MASTERS_SECOND_CLASS = characterConfig.getBoolean("AllowClassMastersSecondClass", true);
		ALLOW_CLASS_MASTERS_THIRD_CLASS = characterConfig.getBoolean("AllowClassMastersThirdClass", true);
		CLASS_MASTER_SETTINGS = new ClassMasterSettings(characterConfig.getString("ConfigClassMaster", ""));
		ALLOW_REMOTE_CLASS_MASTERS = characterConfig.getBoolean("AllowRemoteClassMasters", false);
	}
	
	public static void loadDaemonsConf()
	{
		final PropertiesParser deamonsConfig = new PropertiesParser(DAEMONS_CONFIG_FILE);
		DEADLOCKCHECK_INTIAL_TIME = deamonsConfig.getLong("DeadLockCheck", 0);
		DEADLOCKCHECK_DELAY_TIME = deamonsConfig.getLong("DeadLockDelay", 0);
	}
	
	/**
	 * Loads all Filter Words
	 */
	public static void loadFilter()
	{
		LineNumberReader lnr = null;
		try
		{
			final File file = new File(FILTER_FILE);
			if (!file.exists())
			{
				return;
			}
			
			lnr = new LineNumberReader(new BufferedReader(new FileReader(file)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				FILTER_LIST.add(line.trim());
			}
			LOGGER.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + FILTER_FILE + " File.");
		}
		finally
		{
			if (lnr != null)
			{
				try
				{
					lnr.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void loadHexId()
	{
		final PropertiesParser hexIdConfig = new PropertiesParser(HEXID_FILE);
		SERVER_ID = hexIdConfig.getInt("ServerID", 1);
		HEX_ID = new BigInteger(hexIdConfig.getString("HexID", null), 16).toByteArray();
	}
	
	public static void loadLoginStartConfig()
	{
		final PropertiesParser serverSettings = new PropertiesParser(LOGIN_CONFIG_FILE);
		GAME_SERVER_LOGIN_HOST = serverSettings.getString("LoginHostname", "*");
		GAME_SERVER_LOGIN_PORT = serverSettings.getInt("LoginPort", 9013);
		LOGIN_BIND_ADDRESS = serverSettings.getString("LoginserverHostname", "*");
		PORT_LOGIN = serverSettings.getInt("LoginserverPort", 2106);
		ACCEPT_NEW_GAMESERVER = serverSettings.getBoolean("AcceptNewGameServer", true);
		LOGIN_TRY_BEFORE_BAN = serverSettings.getInt("LoginTryBeforeBan", 10);
		LOGIN_BLOCK_AFTER_BAN = serverSettings.getInt("LoginBlockAfterBan", 600);
		INTERNAL_HOSTNAME = serverSettings.getString("InternalHostname", "localhost");
		EXTERNAL_HOSTNAME = serverSettings.getString("ExternalHostname", "localhost");
		DATABASE_DRIVER = serverSettings.getString("Driver", "org.mariadb.jdbc.Driver");
		DATABASE_URL = serverSettings.getString("URL", "jdbc:mariadb://localhost/l2jdb");
		DATABASE_LOGIN = serverSettings.getString("Login", "root");
		DATABASE_PASSWORD = serverSettings.getString("Password", "");
		DATABASE_MAX_CONNECTIONS = serverSettings.getInt("MaximumDbConnections", 10);
		BACKUP_DATABASE = serverSettings.getBoolean("BackupDatabase", false);
		MYSQL_BIN_PATH = serverSettings.getString("MySqlBinLocation", "C:/xampp/mysql/bin/");
		BACKUP_PATH = serverSettings.getString("BackupPath", "../backup/");
		BACKUP_DAYS = serverSettings.getInt("BackupDays", 30);
		BRUT_AVG_TIME = serverSettings.getInt("BrutAvgTime", 30); // in Seconds
		BRUT_LOGON_ATTEMPTS = serverSettings.getInt("BrutLogonAttempts", 15);
		BRUT_BAN_IP_TIME = serverSettings.getInt("BrutBanIpTime", 900); // in Seconds
		SHOW_LICENCE = serverSettings.getBoolean("ShowLicence", false);
		IP_UPDATE_TIME = serverSettings.getInt("IpUpdateTime", 15);
		FORCE_GGAUTH = serverSettings.getBoolean("ForceGGAuth", false);
		AUTO_CREATE_ACCOUNTS = serverSettings.getBoolean("AutoCreateAccounts", true);
		FLOOD_PROTECTION = serverSettings.getBoolean("EnableFloodProtection", true);
		FAST_CONNECTION_LIMIT = serverSettings.getInt("FastConnectionLimit", 15);
		NORMAL_CONNECTION_TIME = serverSettings.getInt("NormalConnectionTime", 700);
		FAST_CONNECTION_TIME = serverSettings.getInt("FastConnectionTime", 350);
		MAX_CONNECTION_PER_IP = serverSettings.getInt("MaxConnectionPerIP", 50);
		NETWORK_IP_LIST = serverSettings.getString("NetworkList", "");
		SESSION_TTL = serverSettings.getLong("SessionTTL", 25000);
		MAX_LOGINSESSIONS = serverSettings.getInt("MaxSessions", 200);
	}
	
	public static void loadBanFile()
	{
		File file = new File(BANNED_IP_FILE);
		if (!file.exists())
		{
			// old file position
			file = new File(LEGACY_BANNED_IP);
		}
		
		if (file.exists() && file.isFile())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(file);
				LineNumberReader reader = null;
				String line;
				String[] parts;
				try
				{
					reader = new LineNumberReader(new InputStreamReader(fis));
					while ((line = reader.readLine()) != null)
					{
						line = line.trim();
						// check if this line isnt a comment line
						if ((line.length() > 0) && (line.charAt(0) != '#'))
						{
							// split comments if any
							parts = line.split("#", 2);
							
							// discard comments in the line, if any
							line = parts[0];
							parts = line.split(" ");
							
							final String address = parts[0];
							long duration = 0;
							if (parts.length > 1)
							{
								try
								{
									duration = Long.parseLong(parts[1]);
								}
								catch (NumberFormatException e)
								{
									LOGGER.warning("Skipped: Incorrect ban duration (" + parts[1] + ") on (" + file.getName() + "). Line: " + reader.getLineNumber());
									continue;
								}
							}
							
							try
							{
								LoginController.getInstance().addBanForAddress(address, duration);
							}
							catch (UnknownHostException e)
							{
								LOGGER.warning("Skipped: Invalid address (" + parts[0] + ") on (" + file.getName() + "). Line: " + reader.getLineNumber());
							}
						}
					}
				}
				catch (IOException e)
				{
					LOGGER.warning("Error while reading the bans file (" + file.getName() + "). Details: " + e);
				}
				
				LOGGER.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
			}
			catch (FileNotFoundException e)
			{
				LOGGER.warning("Failed to load banned IPs file (" + file.getName() + ") for reading. Reason: " + e);
			}
			finally
			{
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else
		{
			LOGGER.info("IP Bans file (" + file.getName() + ") is missing or is a directory, skipped.");
		}
	}
	
	public static void saveHexid(int serverId, String string)
	{
		saveHexid(serverId, string, HEXID_FILE);
	}
	
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			final Properties hexSetting = new Properties();
			final File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			if (!file.exists())
			{
				try (OutputStream out = new FileOutputStream(file))
				{
					hexSetting.setProperty("ServerID", String.valueOf(serverId));
					hexSetting.setProperty("HexID", hexId);
					hexSetting.store(out, "The HexId to Auth into LoginServer");
					LOGGER.log(Level.INFO, "Gameserver: Generated new HexID file for server id " + serverId + ".");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(StringUtil.concat("Failed to save hex id to ", fileName, " File."));
			LOGGER.warning("Config: " + e.getMessage());
		}
	}
	
	public static void unallocateFilterBuffer()
	{
		LOGGER.info("Cleaning Chat Filter..");
		FILTER_LIST.clear();
	}
	
	/**
	 * Loads flood protector configurations.
	 * @param properties
	 */
	private static void loadFloodProtectorConfigs(PropertiesParser properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", 1);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", 42);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", 42);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", 16);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", 100);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", 20);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", 10);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", 1);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", 10);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", 3);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_UNKNOWN_PACKETS, "UnknownPackets", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_PARTY_INVITATION, "PartyInvitation", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SAY_ACTION, "SayAction", 100);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MOVE_ACTION, "MoveAction", 30);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GENERIC_ACTION, "GenericAction", 5);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MACRO, "Macro", 10);
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_POTION, "Potion", 4);
	}
	
	public static void load(ServerMode serverMode)
	{
		SERVER_MODE = serverMode;
		if (SERVER_MODE == ServerMode.GAME)
		{
			loadHexId();
			
			// Load network
			loadServerConfig();
			
			// Head
			loadRatesConfig();
			loadCharacterConfig();
			loadGeneralConfig();
			load7sConfig();
			loadCHConfig();
			loadElitCHConfig();
			loadOlympConfig();
			loadEnchantConfig();
			loadBossConfig();
			
			// Head functions
			loadCustomServerConfig();
			loadPhysicsConfig();
			loadAccessConfig();
			loadPvpConfig();
			loadCraftConfig();
			
			// Event config
			loadCTFConfig();
			loadDMConfig();
			loadTVTConfig();
			loadTWConfig();
			
			// Protect
			loadFloodConfig();
			loadProtectedOtherConfig();
			
			// Geoengine
			loadgeodataConfig();
			
			// Custom
			loadChampionConfig();
			loadMerchantZeroPriceConfig();
			loadWeddingConfig();
			loadL2jServerConfig();
			loadRebirthConfig();
			loadBankingConfig();
			loadBufferConfig();
			loadPCBPointConfig();
			loadOfflineConfig();
			
			// Other
			loadDaemonsConf();
			
			if (USE_SAY_FILTER)
			{
				loadFilter();
			}
			
			loadTelnetConfig();
		}
		else if (SERVER_MODE == ServerMode.LOGIN)
		{
			loadLoginStartConfig();
			
			loadTelnetConfig();
		}
		else
		{
			LOGGER.severe("Could not Load Config: server mode was not set.");
		}
	}
}
