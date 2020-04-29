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
package org.l2jserver.gameserver.network;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

public class SystemMessageId
{
	private static final Logger LOGGER = Logger.getLogger(SystemMessageId.class.getName());
	
	private static final Map<Integer, SystemMessageId> VALUES = new HashMap<>();
	
	@ClientString(id = 0, message = "You have been disconnected from the server.")
	public static SystemMessageId YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER;
	
	@ClientString(id = 1, message = "The server will be coming down in $s1 second(s). Please find a safe place to log out.")
	public static SystemMessageId THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECOND_S_PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT;
	
	@ClientString(id = 2, message = "$s1 does not exist.")
	public static SystemMessageId S1_DOES_NOT_EXIST;
	
	@ClientString(id = 3, message = "$s1 is not currently logged in.")
	public static SystemMessageId S1_IS_NOT_CURRENTLY_LOGGED_IN;
	
	@ClientString(id = 4, message = "You cannot ask yourself to apply to a clan.")
	public static SystemMessageId YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN;
	
	@ClientString(id = 5, message = "$s1 already exists.")
	public static SystemMessageId S1_ALREADY_EXISTS;
	
	@ClientString(id = 6, message = "$s1 does not exist.")
	public static SystemMessageId S1_DOES_NOT_EXIST_2;
	
	@ClientString(id = 7, message = "You are already a member of $s1.")
	public static SystemMessageId YOU_ARE_ALREADY_A_MEMBER_OF_S1;
	
	@ClientString(id = 8, message = "You are working with another clan.")
	public static SystemMessageId YOU_ARE_WORKING_WITH_ANOTHER_CLAN;
	
	@ClientString(id = 9, message = "$s1 is not a clan leader.")
	public static SystemMessageId S1_IS_NOT_A_CLAN_LEADER;
	
	@ClientString(id = 10, message = "$s1 is working with another clan.")
	public static SystemMessageId S1_IS_WORKING_WITH_ANOTHER_CLAN;
	
	@ClientString(id = 11, message = "There are no applicants for this clan.")
	public static SystemMessageId THERE_ARE_NO_APPLICANTS_FOR_THIS_CLAN;
	
	@ClientString(id = 12, message = "Applicant information is incorrect.")
	public static SystemMessageId APPLICANT_INFORMATION_IS_INCORRECT;
	
	@ClientString(id = 13, message = "Unable to disperse: your clan has requested to participate in a castle siege.")
	public static SystemMessageId UNABLE_TO_DISPERSE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE;
	
	@ClientString(id = 14, message = "Unable to disperse: your clan owns one or more castles or hideouts.")
	public static SystemMessageId UNABLE_TO_DISPERSE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS;
	
	@ClientString(id = 15, message = "You are in siege.")
	public static SystemMessageId YOU_ARE_IN_SIEGE;
	
	@ClientString(id = 16, message = "You are not in siege.")
	public static SystemMessageId YOU_ARE_NOT_IN_SIEGE;
	
	@ClientString(id = 17, message = "The castle siege has begun.")
	public static SystemMessageId THE_CASTLE_SIEGE_HAS_BEGUN;
	
	@ClientString(id = 18, message = "The castle siege has ended.")
	public static SystemMessageId THE_CASTLE_SIEGE_HAS_ENDED;
	
	@ClientString(id = 19, message = "There is a new Lord of the castle!")
	public static SystemMessageId THERE_IS_A_NEW_LORD_OF_THE_CASTLE;
	
	@ClientString(id = 20, message = "The gate is being opened.")
	public static SystemMessageId THE_GATE_IS_BEING_OPENED;
	
	@ClientString(id = 21, message = "The gate is being destroyed.")
	public static SystemMessageId THE_GATE_IS_BEING_DESTROYED;
	
	@ClientString(id = 22, message = "Your target is out of range.")
	public static SystemMessageId YOUR_TARGET_IS_OUT_OF_RANGE;
	
	@ClientString(id = 23, message = "Not enough HP.")
	public static SystemMessageId NOT_ENOUGH_HP;
	
	@ClientString(id = 24, message = "Not enough MP.")
	public static SystemMessageId NOT_ENOUGH_MP;
	
	@ClientString(id = 25, message = "Rejuvenating HP.")
	public static SystemMessageId REJUVENATING_HP;
	
	@ClientString(id = 26, message = "Rejuvenating MP.")
	public static SystemMessageId REJUVENATING_MP;
	
	@ClientString(id = 27, message = "Your casting has been interrupted.")
	public static SystemMessageId YOUR_CASTING_HAS_BEEN_INTERRUPTED;
	
	@ClientString(id = 28, message = "You have obtained $s1 adena.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S1_ADENA;
	
	@ClientString(id = 29, message = "You have obtained $s2 $s1.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S2_S1;
	
	@ClientString(id = 30, message = "You have obtained $s1.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S1;
	
	@ClientString(id = 31, message = "You cannot move while sitting.")
	public static SystemMessageId YOU_CANNOT_MOVE_WHILE_SITTING;
	
	@ClientString(id = 32, message = "You are unable to engage in combat. Please go to the nearest restart point.")
	public static SystemMessageId YOU_ARE_UNABLE_TO_ENGAGE_IN_COMBAT_PLEASE_GO_TO_THE_NEAREST_RESTART_POINT;
	
	@ClientString(id = 33, message = "You cannot move while casting.")
	public static SystemMessageId YOU_CANNOT_MOVE_WHILE_CASTING;
	
	@ClientString(id = 34, message = "Welcome to the World of Lineage II.")
	public static SystemMessageId WELCOME_TO_THE_WORLD_OF_LINEAGE_II;
	
	@ClientString(id = 35, message = "You hit for $s1 damage.")
	public static SystemMessageId YOU_HIT_FOR_S1_DAMAGE;
	
	@ClientString(id = 36, message = "$s1 hit you for $s2 damage.")
	public static SystemMessageId S1_HIT_YOU_FOR_S2_DAMAGE;
	
	@ClientString(id = 37, message = "$s1 hit you for $s2 damage.")
	public static SystemMessageId S1_HIT_YOU_FOR_S2_DAMAGE_2;
	
	@ClientString(id = 38, message = "The TGS2002 event begins!")
	public static SystemMessageId THE_TGS2002_EVENT_BEGINS;
	
	@ClientString(id = 39, message = "The TGS2002 event is over. Thank you very much.")
	public static SystemMessageId THE_TGS2002_EVENT_IS_OVER_THANK_YOU_VERY_MUCH;
	
	@ClientString(id = 40, message = "This is the TGS demo: the character will immediately be restored.")
	public static SystemMessageId THIS_IS_THE_TGS_DEMO_THE_CHARACTER_WILL_IMMEDIATELY_BE_RESTORED;
	
	@ClientString(id = 41, message = "You carefully nock an arrow…")
	public static SystemMessageId YOU_CAREFULLY_NOCK_AN_ARROW;
	
	@ClientString(id = 42, message = "You have avoided $s1's attack.")
	public static SystemMessageId YOU_HAVE_AVOIDED_S1_S_ATTACK;
	
	@ClientString(id = 43, message = "You have missed.")
	public static SystemMessageId YOU_HAVE_MISSED;
	
	@ClientString(id = 44, message = "Critical hit!")
	public static SystemMessageId CRITICAL_HIT;
	
	@ClientString(id = 45, message = "You have earned $s1 experience.")
	public static SystemMessageId YOU_HAVE_EARNED_S1_EXPERIENCE;
	
	@ClientString(id = 46, message = "You use $s1.")
	public static SystemMessageId YOU_USE_S1;
	
	@ClientString(id = 47, message = "You begin to use a(n) $s1.")
	public static SystemMessageId YOU_BEGIN_TO_USE_A_N_S1;
	
	@ClientString(id = 48, message = "$s1 is not available at this time: being prepared for reuse.")
	public static SystemMessageId S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE;
	
	@ClientString(id = 49, message = "You have equipped your $s1.")
	public static SystemMessageId YOU_HAVE_EQUIPPED_YOUR_S1;
	
	@ClientString(id = 50, message = "Your target cannot be found.")
	public static SystemMessageId YOUR_TARGET_CANNOT_BE_FOUND;
	
	@ClientString(id = 51, message = "You cannot use this on yourself.")
	public static SystemMessageId YOU_CANNOT_USE_THIS_ON_YOURSELF;
	
	@ClientString(id = 52, message = "You have earned $s1 adena.")
	public static SystemMessageId YOU_HAVE_EARNED_S1_ADENA;
	
	@ClientString(id = 53, message = "You have earned $s2 $s1(s).")
	public static SystemMessageId YOU_HAVE_EARNED_S2_S1_S;
	
	@ClientString(id = 54, message = "You have earned $s1.")
	public static SystemMessageId YOU_HAVE_EARNED_S1;
	
	@ClientString(id = 55, message = "You have failed to pick up $s1 adena.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA;
	
	@ClientString(id = 56, message = "You have failed to pick up $s1.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_PICK_UP_S1;
	
	@ClientString(id = 57, message = "You have failed to pick up $s2 $s1(s).")
	public static SystemMessageId YOU_HAVE_FAILED_TO_PICK_UP_S2_S1_S;
	
	@ClientString(id = 58, message = "You have failed to earn $s1 adena.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EARN_S1_ADENA;
	
	@ClientString(id = 59, message = "You have failed to earn $s1.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EARN_S1;
	
	@ClientString(id = 60, message = "You have failed to earn $s2 $s1(s).")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EARN_S2_S1_S;
	
	@ClientString(id = 61, message = "Nothing happened.")
	public static SystemMessageId NOTHING_HAPPENED;
	
	@ClientString(id = 62, message = "Your $s1 has been successfully enchanted.")
	public static SystemMessageId YOUR_S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED;
	
	@ClientString(id = 63, message = "Your +$S1 $S2 has been successfully enchanted.")
	public static SystemMessageId YOUR_S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED;
	
	@ClientString(id = 64, message = "The enchantment has failed! Your $s1 has been crystallized.")
	public static SystemMessageId THE_ENCHANTMENT_HAS_FAILED_YOUR_S1_HAS_BEEN_CRYSTALLIZED;
	
	@ClientString(id = 65, message = "The enchantment has failed! Your +$s1 $s2 has been crystallized.")
	public static SystemMessageId THE_ENCHANTMENT_HAS_FAILED_YOUR_S1_S2_HAS_BEEN_CRYSTALLIZED;
	
	@ClientString(id = 66, message = "$s1 has invited you to his/her party. Do you accept the invitation?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_HIS_HER_PARTY_DO_YOU_ACCEPT_THE_INVITATION;
	
	@ClientString(id = 67, message = "$s1 has invited you to the join the clan, $s2. Do you wish to join?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_THE_JOIN_THE_CLAN_S2_DO_YOU_WISH_TO_JOIN;
	
	@ClientString(id = 68, message = "Would you like to withdraw from the $s1 clan? If you leave, you will have to wait at least a day before joining another clan.")
	public static SystemMessageId WOULD_YOU_LIKE_TO_WITHDRAW_FROM_THE_S1_CLAN_IF_YOU_LEAVE_YOU_WILL_HAVE_TO_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN;
	
	@ClientString(id = 69, message = "Would you like to dismiss $s1 from the clan? If you do so, you will have to wait at least a day before accepting a new member.")
	public static SystemMessageId WOULD_YOU_LIKE_TO_DISMISS_S1_FROM_THE_CLAN_IF_YOU_DO_SO_YOU_WILL_HAVE_TO_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER;
	
	@ClientString(id = 70, message = "Do you wish to disperse the clan, $s1?")
	public static SystemMessageId DO_YOU_WISH_TO_DISPERSE_THE_CLAN_S1;
	
	@ClientString(id = 71, message = "How many of your $s1(s) do you wish to discard?")
	public static SystemMessageId HOW_MANY_OF_YOUR_S1_S_DO_YOU_WISH_TO_DISCARD;
	
	@ClientString(id = 72, message = "How many of your $s1(s) do you wish to move?")
	public static SystemMessageId HOW_MANY_OF_YOUR_S1_S_DO_YOU_WISH_TO_MOVE;
	
	@ClientString(id = 73, message = "How many of your $s1(s) do you wish to destroy?")
	public static SystemMessageId HOW_MANY_OF_YOUR_S1_S_DO_YOU_WISH_TO_DESTROY;
	
	@ClientString(id = 74, message = "Do you wish to destroy your $s1?")
	public static SystemMessageId DO_YOU_WISH_TO_DESTROY_YOUR_S1;
	
	@ClientString(id = 75, message = "ID does not exist.")
	public static SystemMessageId ID_DOES_NOT_EXIST;
	
	@ClientString(id = 76, message = "Incorrect password.")
	public static SystemMessageId INCORRECT_PASSWORD;
	
	@ClientString(id = 77, message = "You cannot create another character. Please delete the existing character and try again.")
	public static SystemMessageId YOU_CANNOT_CREATE_ANOTHER_CHARACTER_PLEASE_DELETE_THE_EXISTING_CHARACTER_AND_TRY_AGAIN;
	
	@ClientString(id = 78, message = "Do you wish to delete $s1?")
	public static SystemMessageId DO_YOU_WISH_TO_DELETE_S1;
	
	@ClientString(id = 79, message = "This name already exists.")
	public static SystemMessageId THIS_NAME_ALREADY_EXISTS;
	
	@ClientString(id = 80, message = "Your title cannot exceed 16 characters in length.  Please try again.")
	public static SystemMessageId YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGTH_PLEASE_TRY_AGAIN;
	
	@ClientString(id = 81, message = "Please select your race.")
	public static SystemMessageId PLEASE_SELECT_YOUR_RACE;
	
	@ClientString(id = 82, message = "Please select your occupation.")
	public static SystemMessageId PLEASE_SELECT_YOUR_OCCUPATION;
	
	@ClientString(id = 83, message = "Please select your gender.")
	public static SystemMessageId PLEASE_SELECT_YOUR_GENDER;
	
	@ClientString(id = 84, message = "You may not attack in a peaceful zone.")
	public static SystemMessageId YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE;
	
	@ClientString(id = 85, message = "You may not attack this target in a peaceful zone.")
	public static SystemMessageId YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE;
	
	@ClientString(id = 86, message = "Please enter your ID.")
	public static SystemMessageId PLEASE_ENTER_YOUR_ID;
	
	@ClientString(id = 87, message = "Please enter your password.")
	public static SystemMessageId PLEASE_ENTER_YOUR_PASSWORD;
	
	@ClientString(id = 88, message = "Your protocol version is different, please restart your client and run a full check.")
	public static SystemMessageId YOUR_PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_RESTART_YOUR_CLIENT_AND_RUN_A_FULL_CHECK;
	
	@ClientString(id = 89, message = "Your protocol version is different, please continue.")
	public static SystemMessageId YOUR_PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_CONTINUE;
	
	@ClientString(id = 90, message = "You are unable to connect to the server.")
	public static SystemMessageId YOU_ARE_UNABLE_TO_CONNECT_TO_THE_SERVER;
	
	@ClientString(id = 91, message = "Please select your hairstyle.")
	public static SystemMessageId PLEASE_SELECT_YOUR_HAIRSTYLE;
	
	@ClientString(id = 92, message = "$s1 has worn off.")
	public static SystemMessageId S1_HAS_WORN_OFF;
	
	@ClientString(id = 93, message = "You do not have enough SP for this.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_SP_FOR_THIS;
	
	@ClientString(id = 94, message = "2003 - 2007 Copyright NCsoft Corporation. All Rights Reserved.")
	public static SystemMessageId COPYRIGHT_NCSOFT_CORPORATION_ALL_RIGHTS_RESERVED;
	
	@ClientString(id = 95, message = "You have earned $s1 experience and $s2 SP.")
	public static SystemMessageId YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP;
	
	@ClientString(id = 96, message = "Your level has increased!")
	public static SystemMessageId YOUR_LEVEL_HAS_INCREASED;
	
	@ClientString(id = 97, message = "This item cannot be moved.")
	public static SystemMessageId THIS_ITEM_CANNOT_BE_MOVED;
	
	@ClientString(id = 98, message = "This item cannot be discarded.")
	public static SystemMessageId THIS_ITEM_CANNOT_BE_DISCARDED;
	
	@ClientString(id = 99, message = "This item cannot be traded or sold.")
	public static SystemMessageId THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD;
	
	@ClientString(id = 100, message = "$s1 has requested a trade. Do you wish to continue?")
	public static SystemMessageId S1_HAS_REQUESTED_A_TRADE_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 101, message = "You cannot exit while in combat.")
	public static SystemMessageId YOU_CANNOT_EXIT_WHILE_IN_COMBAT;
	
	@ClientString(id = 102, message = "You cannot restart while in combat.")
	public static SystemMessageId YOU_CANNOT_RESTART_WHILE_IN_COMBAT;
	
	@ClientString(id = 103, message = "This ID is currently logged in.")
	public static SystemMessageId THIS_ID_IS_CURRENTLY_LOGGED_IN;
	
	@ClientString(id = 104, message = "You may not equip items while casting or performing a skill.")
	public static SystemMessageId YOU_MAY_NOT_EQUIP_ITEMS_WHILE_CASTING_OR_PERFORMING_A_SKILL;
	
	@ClientString(id = 105, message = "You have invited $s1 to your party.")
	public static SystemMessageId YOU_HAVE_INVITED_S1_TO_YOUR_PARTY;
	
	@ClientString(id = 106, message = "You have joined $s1's party.")
	public static SystemMessageId YOU_HAVE_JOINED_S1_S_PARTY;
	
	@ClientString(id = 107, message = "$s1 has joined the party.")
	public static SystemMessageId S1_HAS_JOINED_THE_PARTY;
	
	@ClientString(id = 108, message = "$s1 has left the party.")
	public static SystemMessageId S1_HAS_LEFT_THE_PARTY;
	
	@ClientString(id = 109, message = "Invalid target.")
	public static SystemMessageId INVALID_TARGET;
	
	@ClientString(id = 110, message = "The effects of $s1 flow through you.")
	public static SystemMessageId THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU;
	
	@ClientString(id = 111, message = "Your shield defense has succeeded.")
	public static SystemMessageId YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED;
	
	@ClientString(id = 112, message = "You have run out of arrows.")
	public static SystemMessageId YOU_HAVE_RUN_OUT_OF_ARROWS;
	
	@ClientString(id = 113, message = "$s1 cannot be used due to unsuitable terms.")
	public static SystemMessageId S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS;
	
	@ClientString(id = 114, message = "You have entered the shadow of the Mother Tree.")
	public static SystemMessageId YOU_HAVE_ENTERED_THE_SHADOW_OF_THE_MOTHER_TREE;
	
	@ClientString(id = 115, message = "You have left the shadow of the Mother Tree.")
	public static SystemMessageId YOU_HAVE_LEFT_THE_SHADOW_OF_THE_MOTHER_TREE;
	
	@ClientString(id = 116, message = "You have entered a peaceful zone.")
	public static SystemMessageId YOU_HAVE_ENTERED_A_PEACEFUL_ZONE;
	
	@ClientString(id = 117, message = "You have left the peaceful zone.")
	public static SystemMessageId YOU_HAVE_LEFT_THE_PEACEFUL_ZONE;
	
	@ClientString(id = 118, message = "You have requested a trade with $s1.")
	public static SystemMessageId YOU_HAVE_REQUESTED_A_TRADE_WITH_S1;
	
	@ClientString(id = 119, message = "$s1 has denied your request to trade.")
	public static SystemMessageId S1_HAS_DENIED_YOUR_REQUEST_TO_TRADE;
	
	@ClientString(id = 120, message = "You begin trading with $s1.")
	public static SystemMessageId YOU_BEGIN_TRADING_WITH_S1;
	
	@ClientString(id = 121, message = "$s1 has confirmed the trade.")
	public static SystemMessageId S1_HAS_CONFIRMED_THE_TRADE;
	
	@ClientString(id = 122, message = "You may no longer adjust items in the trade because the trade has been confirmed.")
	public static SystemMessageId YOU_MAY_NO_LONGER_ADJUST_ITEMS_IN_THE_TRADE_BECAUSE_THE_TRADE_HAS_BEEN_CONFIRMED;
	
	@ClientString(id = 123, message = "Your trade is successful.")
	public static SystemMessageId YOUR_TRADE_IS_SUCCESSFUL;
	
	@ClientString(id = 124, message = "$s1 has canceled the trade.")
	public static SystemMessageId S1_HAS_CANCELED_THE_TRADE;
	
	@ClientString(id = 125, message = "Do you wish to exit the game?")
	public static SystemMessageId DO_YOU_WISH_TO_EXIT_THE_GAME;
	
	@ClientString(id = 126, message = "Do you wish to exit to the character select screen?")
	public static SystemMessageId DO_YOU_WISH_TO_EXIT_TO_THE_CHARACTER_SELECT_SCREEN;
	
	@ClientString(id = 127, message = "You have been disconnected from the server. Please login again.")
	public static SystemMessageId YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN;
	
	@ClientString(id = 128, message = "Your character creation has failed.")
	public static SystemMessageId YOUR_CHARACTER_CREATION_HAS_FAILED;
	
	@ClientString(id = 129, message = "Your inventory is full.")
	public static SystemMessageId YOUR_INVENTORY_IS_FULL;
	
	@ClientString(id = 130, message = "Your warehouse is full.")
	public static SystemMessageId YOUR_WAREHOUSE_IS_FULL;
	
	@ClientString(id = 131, message = "$s1 has logged in.")
	public static SystemMessageId S1_HAS_LOGGED_IN;
	
	@ClientString(id = 132, message = "$s1 has been added to your friends list.")
	public static SystemMessageId S1_HAS_BEEN_ADDED_TO_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 133, message = "$s1 has been removed from your friends list.")
	public static SystemMessageId S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 134, message = "Please check your friends list again.")
	public static SystemMessageId PLEASE_CHECK_YOUR_FRIENDS_LIST_AGAIN;
	
	@ClientString(id = 135, message = "$s1 did not reply to your invitation; your invite has been canceled.")
	public static SystemMessageId S1_DID_NOT_REPLY_TO_YOUR_INVITATION_YOUR_INVITE_HAS_BEEN_CANCELED;
	
	@ClientString(id = 136, message = "You have not replied to $s1's invitation; the offer has been canceled.")
	public static SystemMessageId YOU_HAVE_NOT_REPLIED_TO_S1_S_INVITATION_THE_OFFER_HAS_BEEN_CANCELED;
	
	@ClientString(id = 137, message = "There are no more items in the shortcut.")
	public static SystemMessageId THERE_ARE_NO_MORE_ITEMS_IN_THE_SHORTCUT;
	
	@ClientString(id = 138, message = "Designate shortcut.")
	public static SystemMessageId DESIGNATE_SHORTCUT;
	
	@ClientString(id = 139, message = "$s1 has resisted your $s2.")
	public static SystemMessageId S1_HAS_RESISTED_YOUR_S2;
	
	@ClientString(id = 140, message = "Your skill was removed due to a lack of MP.")
	public static SystemMessageId YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP;
	
	@ClientString(id = 141, message = "Once the trade is confirmed, the item cannot be moved again.")
	public static SystemMessageId ONCE_THE_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN;
	
	@ClientString(id = 142, message = "You are already trading with someone.")
	public static SystemMessageId YOU_ARE_ALREADY_TRADING_WITH_SOMEONE;
	
	@ClientString(id = 143, message = "$s1 is already trading with another person. Please try again later.")
	public static SystemMessageId S1_IS_ALREADY_TRADING_WITH_ANOTHER_PERSON_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 144, message = "That is the incorrect target.")
	public static SystemMessageId THAT_IS_THE_INCORRECT_TARGET;
	
	@ClientString(id = 145, message = "That player is not online.")
	public static SystemMessageId THAT_PLAYER_IS_NOT_ONLINE;
	
	@ClientString(id = 146, message = "Chatting is now permitted.")
	public static SystemMessageId CHATTING_IS_NOW_PERMITTED;
	
	@ClientString(id = 147, message = "Chatting is currently prohibited.")
	public static SystemMessageId CHATTING_IS_CURRENTLY_PROHIBITED;
	
	@ClientString(id = 148, message = "You cannot use quest items.")
	public static SystemMessageId YOU_CANNOT_USE_QUEST_ITEMS;
	
	@ClientString(id = 149, message = "You cannot pick up or use items while trading.")
	public static SystemMessageId YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING;
	
	@ClientString(id = 150, message = "You cannot discard or destroy an item while trading at a private store.")
	public static SystemMessageId YOU_CANNOT_DISCARD_OR_DESTROY_AN_ITEM_WHILE_TRADING_AT_A_PRIVATE_STORE;
	
	@ClientString(id = 151, message = "That is too far from you to discard.")
	public static SystemMessageId THAT_IS_TOO_FAR_FROM_YOU_TO_DISCARD;
	
	@ClientString(id = 152, message = "You have invited the wrong target.")
	public static SystemMessageId YOU_HAVE_INVITED_THE_WRONG_TARGET;
	
	@ClientString(id = 153, message = "$s1 is busy. Please try again later.")
	public static SystemMessageId S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 154, message = "Only the leader can give out invitations.")
	public static SystemMessageId ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS;
	
	@ClientString(id = 155, message = "The party is full.")
	public static SystemMessageId THE_PARTY_IS_FULL;
	
	@ClientString(id = 156, message = "Drain was only 50 percent successful.")
	public static SystemMessageId DRAIN_WAS_ONLY_50_PERCENT_SUCCESSFUL;
	
	@ClientString(id = 157, message = "You resisted $s1's drain.")
	public static SystemMessageId YOU_RESISTED_S1_S_DRAIN;
	
	@ClientString(id = 158, message = "Your attack has failed.")
	public static SystemMessageId YOUR_ATTACK_HAS_FAILED;
	
	@ClientString(id = 159, message = "You have resisted $s1's magic.")
	public static SystemMessageId YOU_HAVE_RESISTED_S1_S_MAGIC;
	
	@ClientString(id = 160, message = "$s1 is a member of another party and cannot be invited.")
	public static SystemMessageId S1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED;
	
	@ClientString(id = 161, message = "That player is not currently online.")
	public static SystemMessageId THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE;
	
	@ClientString(id = 162, message = "Warehouse is too far.")
	public static SystemMessageId WAREHOUSE_IS_TOO_FAR;
	
	@ClientString(id = 163, message = "You cannot destroy it because the number is incorrect.")
	public static SystemMessageId YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT;
	
	@ClientString(id = 164, message = "Waiting for another reply.")
	public static SystemMessageId WAITING_FOR_ANOTHER_REPLY;
	
	@ClientString(id = 165, message = "You cannot add yourself to your own friend list.")
	public static SystemMessageId YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST;
	
	@ClientString(id = 166, message = "Friend list is not ready yet. Please register again later.")
	public static SystemMessageId FRIEND_LIST_IS_NOT_READY_YET_PLEASE_REGISTER_AGAIN_LATER;
	
	@ClientString(id = 167, message = "$s1 is already on your friend list.")
	public static SystemMessageId S1_IS_ALREADY_ON_YOUR_FRIEND_LIST;
	
	@ClientString(id = 168, message = "$s1 has requested to become friends.")
	public static SystemMessageId S1_HAS_REQUESTED_TO_BECOME_FRIENDS;
	
	@ClientString(id = 169, message = "Accept friendship 0/1 (1 to accept, 0 to deny)")
	public static SystemMessageId ACCEPT_FRIENDSHIP_0_1_1_TO_ACCEPT_0_TO_DENY;
	
	@ClientString(id = 170, message = "The user who requested to become friends is not found in the game.")
	public static SystemMessageId THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME;
	
	@ClientString(id = 171, message = "$s1 is not on your friend list.")
	public static SystemMessageId S1_IS_NOT_ON_YOUR_FRIEND_LIST;
	
	@ClientString(id = 172, message = "You lack the funds needed to pay for this transaction.")
	public static SystemMessageId YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION;
	
	@ClientString(id = 173, message = "You lack the funds needed to pay for this transaction.")
	public static SystemMessageId YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION_2;
	
	@ClientString(id = 174, message = "That person's inventory is full.")
	public static SystemMessageId THAT_PERSON_S_INVENTORY_IS_FULL;
	
	@ClientString(id = 175, message = "That skill has been de-activated as HP was fully recovered.")
	public static SystemMessageId THAT_SKILL_HAS_BEEN_DE_ACTIVATED_AS_HP_WAS_FULLY_RECOVERED;
	
	@ClientString(id = 176, message = "That person is in message refusal mode.")
	public static SystemMessageId THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE;
	
	@ClientString(id = 177, message = "Message refusal mode.")
	public static SystemMessageId MESSAGE_REFUSAL_MODE;
	
	@ClientString(id = 178, message = "Message acceptance mode.")
	public static SystemMessageId MESSAGE_ACCEPTANCE_MODE;
	
	@ClientString(id = 179, message = "You cannot discard those items here.")
	public static SystemMessageId YOU_CANNOT_DISCARD_THOSE_ITEMS_HERE;
	
	@ClientString(id = 180, message = "You have $s1 day(s) left until deletion. Do you wish to cancel this action?")
	public static SystemMessageId YOU_HAVE_S1_DAY_S_LEFT_UNTIL_DELETION_DO_YOU_WISH_TO_CANCEL_THIS_ACTION;
	
	@ClientString(id = 181, message = "Cannot see target.")
	public static SystemMessageId CANNOT_SEE_TARGET;
	
	@ClientString(id = 182, message = "Do you want to quit the current quest?")
	public static SystemMessageId DO_YOU_WANT_TO_QUIT_THE_CURRENT_QUEST;
	
	@ClientString(id = 183, message = "There are too many users on the server. Please try again later.")
	public static SystemMessageId THERE_ARE_TOO_MANY_USERS_ON_THE_SERVER_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 184, message = "Please try again later.")
	public static SystemMessageId PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 185, message = "You must first select a user to invite to your party.")
	public static SystemMessageId YOU_MUST_FIRST_SELECT_A_USER_TO_INVITE_TO_YOUR_PARTY;
	
	@ClientString(id = 186, message = "You must first select a user to invite to your clan.")
	public static SystemMessageId YOU_MUST_FIRST_SELECT_A_USER_TO_INVITE_TO_YOUR_CLAN;
	
	@ClientString(id = 187, message = "Select user to expel.")
	public static SystemMessageId SELECT_USER_TO_EXPEL;
	
	@ClientString(id = 188, message = "Please create your clan name.")
	public static SystemMessageId PLEASE_CREATE_YOUR_CLAN_NAME;
	
	@ClientString(id = 189, message = "Your clan has been created.")
	public static SystemMessageId YOUR_CLAN_HAS_BEEN_CREATED;
	
	@ClientString(id = 190, message = "You have failed to create a clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_CREATE_A_CLAN;
	
	@ClientString(id = 191, message = "Clan member $s1 has been expelled.")
	public static SystemMessageId CLAN_MEMBER_S1_HAS_BEEN_EXPELLED;
	
	@ClientString(id = 192, message = "You have failed to expel $s1 from the clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EXPEL_S1_FROM_THE_CLAN;
	
	@ClientString(id = 193, message = "Clan has dispersed.")
	public static SystemMessageId CLAN_HAS_DISPERSED;
	
	@ClientString(id = 194, message = "You have failed to disperse the clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DISPERSE_THE_CLAN;
	
	@ClientString(id = 195, message = "Entered the clan.")
	public static SystemMessageId ENTERED_THE_CLAN;
	
	@ClientString(id = 196, message = "$s1 declined your clan invitation.")
	public static SystemMessageId S1_DECLINED_YOUR_CLAN_INVITATION;
	
	@ClientString(id = 197, message = "You have withdrawn from the clan.")
	public static SystemMessageId YOU_HAVE_WITHDRAWN_FROM_THE_CLAN;
	
	@ClientString(id = 198, message = "You have failed to withdraw from the $s1 clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_S1_CLAN;
	
	@ClientString(id = 199, message = "You have recently been dismissed from a clan. You are not allowed to join another clan for 24-hours.")
	public static SystemMessageId YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS;
	
	@ClientString(id = 200, message = "You have withdrawn from the party.")
	public static SystemMessageId YOU_HAVE_WITHDRAWN_FROM_THE_PARTY;
	
	@ClientString(id = 201, message = "$s1 was expelled from the party.")
	public static SystemMessageId S1_WAS_EXPELLED_FROM_THE_PARTY;
	
	@ClientString(id = 202, message = "You have been expelled from the party.")
	public static SystemMessageId YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY;
	
	@ClientString(id = 203, message = "The party has dispersed.")
	public static SystemMessageId THE_PARTY_HAS_DISPERSED;
	
	@ClientString(id = 204, message = "Incorrect name. Please try again.")
	public static SystemMessageId INCORRECT_NAME_PLEASE_TRY_AGAIN;
	
	@ClientString(id = 205, message = "Incorrect character name. Please try again.")
	public static SystemMessageId INCORRECT_CHARACTER_NAME_PLEASE_TRY_AGAIN;
	
	@ClientString(id = 206, message = "Please enter the name of the clan you wish to declare war on.")
	public static SystemMessageId PLEASE_ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_DECLARE_WAR_ON;
	
	@ClientString(id = 207, message = "$s2 of the clan $s1 requests declaration of war. Do you accept?")
	public static SystemMessageId S2_OF_THE_CLAN_S1_REQUESTS_DECLARATION_OF_WAR_DO_YOU_ACCEPT;
	
	@ClientString(id = 208, message = "Please include file type when entering file path.")
	public static SystemMessageId PLEASE_INCLUDE_FILE_TYPE_WHEN_ENTERING_FILE_PATH;
	
	@ClientString(id = 209, message = "The size of the image file is inappropriate. Please adjust to 16*12.")
	public static SystemMessageId THE_SIZE_OF_THE_IMAGE_FILE_IS_INAPPROPRIATE_PLEASE_ADJUST_TO_16_12;
	
	@ClientString(id = 210, message = "Cannot find file. Please enter precise path.")
	public static SystemMessageId CANNOT_FIND_FILE_PLEASE_ENTER_PRECISE_PATH;
	
	@ClientString(id = 211, message = "You may only register a 16 x 12 pixel, 256-color BMP.")
	public static SystemMessageId YOU_MAY_ONLY_REGISTER_A_16_X_12_PIXEL_256_COLOR_BMP;
	
	@ClientString(id = 212, message = "You are not a clan member and cannot perform this action.")
	public static SystemMessageId YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION;
	
	@ClientString(id = 213, message = "Not working. Please try again later.")
	public static SystemMessageId NOT_WORKING_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 214, message = "Your title has been changed.")
	public static SystemMessageId YOUR_TITLE_HAS_BEEN_CHANGED;
	
	@ClientString(id = 215, message = "War with the $s1 clan has begun.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_BEGUN;
	
	@ClientString(id = 216, message = "War with the $s1 clan has ended.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_ENDED;
	
	@ClientString(id = 217, message = "You have won the war over the $s1 clan!")
	public static SystemMessageId YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN;
	
	@ClientString(id = 218, message = "You have surrendered to the $s1 clan.")
	public static SystemMessageId YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN;
	
	@ClientString(id = 219, message = "Your clan leader has died. You have been defeated by the $s1 Clan.")
	public static SystemMessageId YOUR_CLAN_LEADER_HAS_DIED_YOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN;
	
	@ClientString(id = 220, message = "You have $s1 minutes left until the clan war ends.")
	public static SystemMessageId YOU_HAVE_S1_MINUTES_LEFT_UNTIL_THE_CLAN_WAR_ENDS;
	
	@ClientString(id = 221, message = "The time limit for the clan war is up. War with the $s1 clan is over.")
	public static SystemMessageId THE_TIME_LIMIT_FOR_THE_CLAN_WAR_IS_UP_WAR_WITH_THE_S1_CLAN_IS_OVER;
	
	@ClientString(id = 222, message = "$s1 has joined the clan.")
	public static SystemMessageId S1_HAS_JOINED_THE_CLAN;
	
	@ClientString(id = 223, message = "$s1 has withdrawn from the clan.")
	public static SystemMessageId S1_HAS_WITHDRAWN_FROM_THE_CLAN;
	
	@ClientString(id = 224, message = "$s1 did not respond: Invitation to the clan has been cancelled.")
	public static SystemMessageId S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 225, message = "You didn't respond to $s1's invitation: joining has been cancelled.")
	public static SystemMessageId YOU_DIDN_T_RESPOND_TO_S1_S_INVITATION_JOINING_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 226, message = "The $s1 clan did not respond: war proclamation has been refused.")
	public static SystemMessageId THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED;
	
	@ClientString(id = 227, message = "Clan war has been refused because you did not respond to $s1 clan's war proclamation.")
	public static SystemMessageId CLAN_WAR_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLAN_S_WAR_PROCLAMATION;
	
	@ClientString(id = 228, message = "Request to end war has been denied.")
	public static SystemMessageId REQUEST_TO_END_WAR_HAS_BEEN_DENIED;
	
	@ClientString(id = 229, message = "You do not meet the criteria ir order to create a clan.")
	public static SystemMessageId YOU_DO_NOT_MEET_THE_CRITERIA_IR_ORDER_TO_CREATE_A_CLAN;
	
	@ClientString(id = 230, message = "You must wait 10 days before creating a new clan.")
	public static SystemMessageId YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN;
	
	@ClientString(id = 231, message = "After a clan member is dismissed from a clan, the clan must wait at least a day before accepting a new member.")
	public static SystemMessageId AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER;
	
	@ClientString(id = 232, message = "After leaving or having been dismissed from a clan, you must wait at least a day before joining another clan.")
	public static SystemMessageId AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN;
	
	@ClientString(id = 233, message = "The Academy/Royal Guard/Order of Knights is full and cannot accept new members at this time.")
	public static SystemMessageId THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME;
	
	@ClientString(id = 234, message = "The target must be a clan member.")
	public static SystemMessageId THE_TARGET_MUST_BE_A_CLAN_MEMBER;
	
	@ClientString(id = 235, message = "You are not authorized to bestow these rights.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_BESTOW_THESE_RIGHTS;
	
	@ClientString(id = 236, message = "Only the clan leader is enabled.")
	public static SystemMessageId ONLY_THE_CLAN_LEADER_IS_ENABLED;
	
	@ClientString(id = 237, message = "The clan leader could not be found.")
	public static SystemMessageId THE_CLAN_LEADER_COULD_NOT_BE_FOUND;
	
	@ClientString(id = 238, message = "Not joined in any clan.")
	public static SystemMessageId NOT_JOINED_IN_ANY_CLAN;
	
	@ClientString(id = 239, message = "The clan leader cannot withdraw.")
	public static SystemMessageId THE_CLAN_LEADER_CANNOT_WITHDRAW;
	
	@ClientString(id = 240, message = "Currently involved in clan war.")
	public static SystemMessageId CURRENTLY_INVOLVED_IN_CLAN_WAR;
	
	@ClientString(id = 241, message = "Leader of the $s1 Clan is not logged in.")
	public static SystemMessageId LEADER_OF_THE_S1_CLAN_IS_NOT_LOGGED_IN;
	
	@ClientString(id = 242, message = "Select target.")
	public static SystemMessageId SELECT_TARGET;
	
	@ClientString(id = 243, message = "You cannot declare war on an allied clan.")
	public static SystemMessageId YOU_CANNOT_DECLARE_WAR_ON_AN_ALLIED_CLAN;
	
	@ClientString(id = 244, message = "You are not allowed to issue this challenge.")
	public static SystemMessageId YOU_ARE_NOT_ALLOWED_TO_ISSUE_THIS_CHALLENGE;
	
	@ClientString(id = 245, message = "5 days has not passed since you were refused war. Do you wish to continue?")
	public static SystemMessageId FIVE_DAYS_HAS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 246, message = "That clan is currently at war.")
	public static SystemMessageId THAT_CLAN_IS_CURRENTLY_AT_WAR;
	
	@ClientString(id = 247, message = "You have already been at war with the $s1 clan: 5 days must pass before you can challenge this clan again.")
	public static SystemMessageId YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_CHALLENGE_THIS_CLAN_AGAIN;
	
	@ClientString(id = 248, message = "You cannot proclaim war: the $s1 clan does not have enough members.")
	public static SystemMessageId YOU_CANNOT_PROCLAIM_WAR_THE_S1_CLAN_DOES_NOT_HAVE_ENOUGH_MEMBERS;
	
	@ClientString(id = 249, message = "Do you wish to surrender to the $s1 clan?")
	public static SystemMessageId DO_YOU_WISH_TO_SURRENDER_TO_THE_S1_CLAN;
	
	@ClientString(id = 250, message = "You have personally surrendered to the $s1 clan. You are no longer participating in this clan war.")
	public static SystemMessageId YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN_YOU_ARE_NO_LONGER_PARTICIPATING_IN_THIS_CLAN_WAR;
	
	@ClientString(id = 251, message = "You cannot proclaim war: you are at war with another clan.")
	public static SystemMessageId YOU_CANNOT_PROCLAIM_WAR_YOU_ARE_AT_WAR_WITH_ANOTHER_CLAN;
	
	@ClientString(id = 252, message = "Enter the name of clan to surrender to.")
	public static SystemMessageId ENTER_THE_NAME_OF_CLAN_TO_SURRENDER_TO;
	
	@ClientString(id = 253, message = "Enter the name of the clan you wish to end the war with.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_END_THE_WAR_WITH;
	
	@ClientString(id = 254, message = "A clan leader cannot personally surrender.")
	public static SystemMessageId A_CLAN_LEADER_CANNOT_PERSONALLY_SURRENDER;
	
	@ClientString(id = 255, message = "The $s1 Clan has requested to end war. Do you agree?")
	public static SystemMessageId THE_S1_CLAN_HAS_REQUESTED_TO_END_WAR_DO_YOU_AGREE;
	
	@ClientString(id = 256, message = "Enter Title")
	public static SystemMessageId ENTER_TITLE;
	
	@ClientString(id = 257, message = "Do you offer the $s1 clan a proposal to end the war?")
	public static SystemMessageId DO_YOU_OFFER_THE_S1_CLAN_A_PROPOSAL_TO_END_THE_WAR;
	
	@ClientString(id = 258, message = "You are not involved in a clan war.")
	public static SystemMessageId YOU_ARE_NOT_INVOLVED_IN_A_CLAN_WAR;
	
	@ClientString(id = 259, message = "Select clan members from list.")
	public static SystemMessageId SELECT_CLAN_MEMBERS_FROM_LIST;
	
	@ClientString(id = 260, message = "Fame level has decreased: 5 days have not passed since you were refused war.")
	public static SystemMessageId FAME_LEVEL_HAS_DECREASED_5_DAYS_HAVE_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR;
	
	@ClientString(id = 261, message = "Clan name is invalid.")
	public static SystemMessageId CLAN_NAME_IS_INVALID;
	
	@ClientString(id = 262, message = "Clan name's length is incorrect.")
	public static SystemMessageId CLAN_NAME_S_LENGTH_IS_INCORRECT;
	
	@ClientString(id = 263, message = "You have already requested the dissolution of your clan.")
	public static SystemMessageId YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN;
	
	@ClientString(id = 264, message = "You cannot dissolve a clan while engaged in a war.")
	public static SystemMessageId YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR;
	
	@ClientString(id = 265, message = "You cannot dissolve a clan during a siege or while protecting a castle.")
	public static SystemMessageId YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE;
	
	@ClientString(id = 266, message = "You cannot dissolve a clan while owning a clan hall or castle.")
	public static SystemMessageId YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_OWNING_A_CLAN_HALL_OR_CASTLE;
	
	@ClientString(id = 267, message = "There are no requests to disperse.")
	public static SystemMessageId THERE_ARE_NO_REQUESTS_TO_DISPERSE;
	
	@ClientString(id = 268, message = "That player already belongs to another clan.")
	public static SystemMessageId THAT_PLAYER_ALREADY_BELONGS_TO_ANOTHER_CLAN;
	
	@ClientString(id = 269, message = "You cannot dismiss yourself.")
	public static SystemMessageId YOU_CANNOT_DISMISS_YOURSELF;
	
	@ClientString(id = 270, message = "You have already surrendered.")
	public static SystemMessageId YOU_HAVE_ALREADY_SURRENDERED;
	
	@ClientString(id = 271, message = "A player can only be granted a title if the clan is level 3 or above.")
	public static SystemMessageId A_PLAYER_CAN_ONLY_BE_GRANTED_A_TITLE_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE;
	
	@ClientString(id = 272, message = "A clan crest can only be registered when the clan's skill level is 3 or above.")
	public static SystemMessageId A_CLAN_CREST_CAN_ONLY_BE_REGISTERED_WHEN_THE_CLAN_S_SKILL_LEVEL_IS_3_OR_ABOVE;
	
	@ClientString(id = 273, message = "A clan war can only be declared when a clan's skill level is 3 or above.")
	public static SystemMessageId A_CLAN_WAR_CAN_ONLY_BE_DECLARED_WHEN_A_CLAN_S_SKILL_LEVEL_IS_3_OR_ABOVE;
	
	@ClientString(id = 274, message = "Your clan's skill level has increased.")
	public static SystemMessageId YOUR_CLAN_S_SKILL_LEVEL_HAS_INCREASED;
	
	@ClientString(id = 275, message = "Clan has failed to increase skill level.")
	public static SystemMessageId CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL;
	
	@ClientString(id = 276, message = "You do not have the necessary materials or prerequisites to learn this skill.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL;
	
	@ClientString(id = 277, message = "You have earned $s1.")
	public static SystemMessageId YOU_HAVE_EARNED_S1_2;
	
	@ClientString(id = 278, message = "You do not have enough SP to learn this skill.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL;
	
	@ClientString(id = 279, message = "You do not have enough adena.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_ADENA;
	
	@ClientString(id = 280, message = "You do not have any items to sell.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ANY_ITEMS_TO_SELL;
	
	@ClientString(id = 281, message = "You do not have enough adena to pay the fee.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_ADENA_TO_PAY_THE_FEE;
	
	@ClientString(id = 282, message = "You have not deposited any items in your warehouse.")
	public static SystemMessageId YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE;
	
	@ClientString(id = 283, message = "You have entered a combat zone.")
	public static SystemMessageId YOU_HAVE_ENTERED_A_COMBAT_ZONE;
	
	@ClientString(id = 284, message = "You have left a combat zone.")
	public static SystemMessageId YOU_HAVE_LEFT_A_COMBAT_ZONE;
	
	@ClientString(id = 285, message = "Clan $s1 has succeeded in engraving the ruler!")
	public static SystemMessageId CLAN_S1_HAS_SUCCEEDED_IN_ENGRAVING_THE_RULER;
	
	@ClientString(id = 286, message = "Your base is being attacked.")
	public static SystemMessageId YOUR_BASE_IS_BEING_ATTACKED;
	
	@ClientString(id = 287, message = "The opposing clan has started to engrave the monument!")
	public static SystemMessageId THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_MONUMENT;
	
	@ClientString(id = 288, message = "The castle gate has been broken down.")
	public static SystemMessageId THE_CASTLE_GATE_HAS_BEEN_BROKEN_DOWN;
	
	@ClientString(id = 289, message = "You cannot build another headquarters since one already exists.")
	public static SystemMessageId YOU_CANNOT_BUILD_ANOTHER_HEADQUARTERS_SINCE_ONE_ALREADY_EXISTS;
	
	@ClientString(id = 290, message = "You cannot set up a base here.")
	public static SystemMessageId YOU_CANNOT_SET_UP_A_BASE_HERE;
	
	@ClientString(id = 291, message = "Clan $s1 is victorious over $s2's castle siege!")
	public static SystemMessageId CLAN_S1_IS_VICTORIOUS_OVER_S2_S_CASTLE_SIEGE;
	
	@ClientString(id = 292, message = "$s1 has announced the castle siege time.")
	public static SystemMessageId S1_HAS_ANNOUNCED_THE_CASTLE_SIEGE_TIME;
	
	@ClientString(id = 293, message = "The registration term for $s1 has ended.")
	public static SystemMessageId THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED;
	
	@ClientString(id = 294, message = "Because your clan is not currently on the offensive in a Clan Hall siege war, it cannot summon its base camp.")
	public static SystemMessageId BECAUSE_YOUR_CLAN_IS_NOT_CURRENTLY_ON_THE_OFFENSIVE_IN_A_CLAN_HALL_SIEGE_WAR_IT_CANNOT_SUMMON_ITS_BASE_CAMP;
	
	@ClientString(id = 295, message = "$s1's siege was canceled because there were no clans that participated.")
	public static SystemMessageId S1_S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED;
	
	@ClientString(id = 296, message = "You received $s1 damage from taking a high fall.")
	public static SystemMessageId YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL;
	
	@ClientString(id = 297, message = "You have taken $s1 damage because you were unable to breathe.")
	public static SystemMessageId YOU_HAVE_TAKEN_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE;
	
	@ClientString(id = 298, message = "You have dropped $s1.")
	public static SystemMessageId YOU_HAVE_DROPPED_S1;
	
	@ClientString(id = 299, message = "$s1 has obtained $s3 $s2.")
	public static SystemMessageId S1_HAS_OBTAINED_S3_S2;
	
	@ClientString(id = 300, message = "$s1 has obtained $s2.")
	public static SystemMessageId S1_HAS_OBTAINED_S2;
	
	@ClientString(id = 301, message = "$s2 $s1 has disappeared.")
	public static SystemMessageId S2_S1_HAS_DISAPPEARED;
	
	@ClientString(id = 302, message = "$s1 has disappeared.")
	public static SystemMessageId S1_HAS_DISAPPEARED;
	
	@ClientString(id = 303, message = "Select item to enchant.")
	public static SystemMessageId SELECT_ITEM_TO_ENCHANT;
	
	@ClientString(id = 304, message = "Clan member $s1 has logged into game.")
	public static SystemMessageId CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME;
	
	@ClientString(id = 305, message = "The player declined to join your party.")
	public static SystemMessageId THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY;
	
	@ClientString(id = 306, message = "You have failed to delete the character.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER;
	
	@ClientString(id = 307, message = "You cannot trade with a warehouse keeper.")
	public static SystemMessageId YOU_CANNOT_TRADE_WITH_A_WAREHOUSE_KEEPER;
	
	@ClientString(id = 308, message = "The player declined your clan invitation.")
	public static SystemMessageId THE_PLAYER_DECLINED_YOUR_CLAN_INVITATION;
	
	@ClientString(id = 309, message = "You have succeeded in expelling the clan member.")
	public static SystemMessageId YOU_HAVE_SUCCEEDED_IN_EXPELLING_THE_CLAN_MEMBER;
	
	@ClientString(id = 310, message = "You have failed to expel the clan member.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EXPEL_THE_CLAN_MEMBER;
	
	@ClientString(id = 311, message = "The clan war declaration has been accepted.")
	public static SystemMessageId THE_CLAN_WAR_DECLARATION_HAS_BEEN_ACCEPTED;
	
	@ClientString(id = 312, message = "The clan war declaration has been refused.")
	public static SystemMessageId THE_CLAN_WAR_DECLARATION_HAS_BEEN_REFUSED;
	
	@ClientString(id = 313, message = "The cease war request has been accepted.")
	public static SystemMessageId THE_CEASE_WAR_REQUEST_HAS_BEEN_ACCEPTED;
	
	@ClientString(id = 314, message = "You have failed to surrender.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_SURRENDER;
	
	@ClientString(id = 315, message = "You have failed to personally surrender.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_PERSONALLY_SURRENDER;
	
	@ClientString(id = 316, message = "You have failed to withdraw from the party.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_PARTY;
	
	@ClientString(id = 317, message = "You have failed to expel the party member.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EXPEL_THE_PARTY_MEMBER;
	
	@ClientString(id = 318, message = "You have failed to disperse the party.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DISPERSE_THE_PARTY;
	
	@ClientString(id = 319, message = "This door cannot be unlocked.")
	public static SystemMessageId THIS_DOOR_CANNOT_BE_UNLOCKED;
	
	@ClientString(id = 320, message = "You have failed to unlock the door.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR;
	
	@ClientString(id = 321, message = "It is not locked.")
	public static SystemMessageId IT_IS_NOT_LOCKED;
	
	@ClientString(id = 322, message = "Please decide on the sales price.")
	public static SystemMessageId PLEASE_DECIDE_ON_THE_SALES_PRICE;
	
	@ClientString(id = 323, message = "Your force has increased to $s1 level.")
	public static SystemMessageId YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL;
	
	@ClientString(id = 324, message = "Your force has reached maximum capacity.")
	public static SystemMessageId YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY;
	
	@ClientString(id = 325, message = "The corpse has already disappeared.")
	public static SystemMessageId THE_CORPSE_HAS_ALREADY_DISAPPEARED;
	
	@ClientString(id = 326, message = "Select target from list.")
	public static SystemMessageId SELECT_TARGET_FROM_LIST;
	
	@ClientString(id = 327, message = "You cannot exceed 80 characters.")
	public static SystemMessageId YOU_CANNOT_EXCEED_80_CHARACTERS;
	
	@ClientString(id = 328, message = "Please input title using less than 128 characters.")
	public static SystemMessageId PLEASE_INPUT_TITLE_USING_LESS_THAN_128_CHARACTERS;
	
	@ClientString(id = 329, message = "Please input contents using less than 3000 characters.")
	public static SystemMessageId PLEASE_INPUT_CONTENTS_USING_LESS_THAN_3000_CHARACTERS;
	
	@ClientString(id = 330, message = "A one-line response may not exceed 128 characters.")
	public static SystemMessageId A_ONE_LINE_RESPONSE_MAY_NOT_EXCEED_128_CHARACTERS;
	
	@ClientString(id = 331, message = "You have acquired $s1 SP.")
	public static SystemMessageId YOU_HAVE_ACQUIRED_S1_SP;
	
	@ClientString(id = 332, message = "Do you want to be restored?")
	public static SystemMessageId DO_YOU_WANT_TO_BE_RESTORED;
	
	@ClientString(id = 333, message = "You have received $s1 damage by Core's barrier.")
	public static SystemMessageId YOU_HAVE_RECEIVED_S1_DAMAGE_BY_CORE_S_BARRIER;
	
	@ClientString(id = 334, message = "Please enter your private store display message.")
	public static SystemMessageId PLEASE_ENTER_YOUR_PRIVATE_STORE_DISPLAY_MESSAGE;
	
	@ClientString(id = 335, message = "$s1 has been aborted.")
	public static SystemMessageId S1_HAS_BEEN_ABORTED;
	
	@ClientString(id = 336, message = "You are attempting to crystalize $s1. Do you wish to continue?")
	public static SystemMessageId YOU_ARE_ATTEMPTING_TO_CRYSTALIZE_S1_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 337, message = "The soulshot you are attempting to use does not match the grade of your equipped weapon.")
	public static SystemMessageId THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON;
	
	@ClientString(id = 338, message = "You do not have enough soulshots for that.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_SOULSHOTS_FOR_THAT;
	
	@ClientString(id = 339, message = "Cannot use soulshots.")
	public static SystemMessageId CANNOT_USE_SOULSHOTS;
	
	@ClientString(id = 340, message = "Your private store is now open for business.")
	public static SystemMessageId YOUR_PRIVATE_STORE_IS_NOW_OPEN_FOR_BUSINESS;
	
	@ClientString(id = 341, message = "You do not have enough materials to perform that action.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION;
	
	@ClientString(id = 342, message = "Power of the spirits enabled.")
	public static SystemMessageId POWER_OF_THE_SPIRITS_ENABLED;
	
	@ClientString(id = 343, message = "Sweeper failed, target not spoiled.")
	public static SystemMessageId SWEEPER_FAILED_TARGET_NOT_SPOILED;
	
	@ClientString(id = 344, message = "Power of the spirits disabled.")
	public static SystemMessageId POWER_OF_THE_SPIRITS_DISABLED;
	
	@ClientString(id = 345, message = "Chat enabled.")
	public static SystemMessageId CHAT_ENABLED;
	
	@ClientString(id = 346, message = "Chat disabled.")
	public static SystemMessageId CHAT_DISABLED;
	
	@ClientString(id = 347, message = "Incorrect item count.")
	public static SystemMessageId INCORRECT_ITEM_COUNT;
	
	@ClientString(id = 348, message = "Incorrect item price.")
	public static SystemMessageId INCORRECT_ITEM_PRICE;
	
	@ClientString(id = 349, message = "Private store already closed.")
	public static SystemMessageId PRIVATE_STORE_ALREADY_CLOSED;
	
	@ClientString(id = 350, message = "Item out of stock.")
	public static SystemMessageId ITEM_OUT_OF_STOCK;
	
	@ClientString(id = 351, message = "Incorrect item count.")
	public static SystemMessageId INCORRECT_ITEM_COUNT_2;
	
	@ClientString(id = 352, message = "Incorrect item.")
	public static SystemMessageId INCORRECT_ITEM;
	
	@ClientString(id = 353, message = "Cannot purchase.")
	public static SystemMessageId CANNOT_PURCHASE;
	
	@ClientString(id = 354, message = "Cancel enchant.")
	public static SystemMessageId CANCEL_ENCHANT;
	
	@ClientString(id = 355, message = "Inappropriate enchant conditions.")
	public static SystemMessageId INAPPROPRIATE_ENCHANT_CONDITIONS;
	
	@ClientString(id = 356, message = "Reject resurrection.")
	public static SystemMessageId REJECT_RESURRECTION;
	
	@ClientString(id = 357, message = "It has already been spoiled.")
	public static SystemMessageId IT_HAS_ALREADY_BEEN_SPOILED;
	
	@ClientString(id = 358, message = "$s1 hour(s) until castle siege conclusion.")
	public static SystemMessageId S1_HOUR_S_UNTIL_CASTLE_SIEGE_CONCLUSION;
	
	@ClientString(id = 359, message = "$s1 minute(s) until castle siege conclusion.")
	public static SystemMessageId S1_MINUTE_S_UNTIL_CASTLE_SIEGE_CONCLUSION;
	
	@ClientString(id = 360, message = "Castle siege $s1 second(s) left!")
	public static SystemMessageId CASTLE_SIEGE_S1_SECOND_S_LEFT;
	
	@ClientString(id = 361, message = "Over-hit!")
	public static SystemMessageId OVER_HIT;
	
	@ClientString(id = 362, message = "You have acquired $s1 bonus experience from a successful over-hit.")
	public static SystemMessageId YOU_HAVE_ACQUIRED_S1_BONUS_EXPERIENCE_FROM_A_SUCCESSFUL_OVER_HIT;
	
	@ClientString(id = 363, message = "Chat available time: $s1 minute.")
	public static SystemMessageId CHAT_AVAILABLE_TIME_S1_MINUTE;
	
	@ClientString(id = 364, message = "Enter user's name to search.")
	public static SystemMessageId ENTER_USER_S_NAME_TO_SEARCH;
	
	@ClientString(id = 365, message = "Are you sure?")
	public static SystemMessageId ARE_YOU_SURE;
	
	@ClientString(id = 366, message = "Please select your hair color.")
	public static SystemMessageId PLEASE_SELECT_YOUR_HAIR_COLOR;
	
	@ClientString(id = 367, message = "You cannot remove that clan character at this time.")
	public static SystemMessageId YOU_CANNOT_REMOVE_THAT_CLAN_CHARACTER_AT_THIS_TIME;
	
	@ClientString(id = 368, message = "Equipped +$s1 $s2.")
	public static SystemMessageId EQUIPPED_S1_S2;
	
	@ClientString(id = 369, message = "You have obtained a +$s1 $s2.")
	public static SystemMessageId YOU_HAVE_OBTAINED_A_S1_S2;
	
	@ClientString(id = 370, message = "Failed to pick up $s1.")
	public static SystemMessageId FAILED_TO_PICK_UP_S1;
	
	@ClientString(id = 371, message = "Acquired +$s1 $s2.")
	public static SystemMessageId ACQUIRED_S1_S2;
	
	@ClientString(id = 372, message = "Failed to earn $s1.")
	public static SystemMessageId FAILED_TO_EARN_S1;
	
	@ClientString(id = 373, message = "You are trying to destroy +$s1 $s2. Do you wish to continue?")
	public static SystemMessageId YOU_ARE_TRYING_TO_DESTROY_S1_S2_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 374, message = "You are attempting to crystalize +$s1 $s2. Do you wish to continue?")
	public static SystemMessageId YOU_ARE_ATTEMPTING_TO_CRYSTALIZE_S1_S2_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 375, message = "You have dropped +$s1 $s2.")
	public static SystemMessageId YOU_HAVE_DROPPED_S1_S2;
	
	@ClientString(id = 376, message = "$s1 has obtained +$s2$s3.")
	public static SystemMessageId S1_HAS_OBTAINED_S2_S3;
	
	@ClientString(id = 377, message = "$S1 $S2 disappeared.")
	public static SystemMessageId S1_S2_DISAPPEARED;
	
	@ClientString(id = 378, message = "$s1 purchased $s2.")
	public static SystemMessageId S1_PURCHASED_S2;
	
	@ClientString(id = 379, message = "$s1 purchased +$s2 $s3.")
	public static SystemMessageId S1_PURCHASED_S2_S3;
	
	@ClientString(id = 380, message = "$s1 purchased $s3 $s2(s).")
	public static SystemMessageId S1_PURCHASED_S3_S2_S;
	
	@ClientString(id = 381, message = "The game client encountered an error and was unable to connect to the petition server.")
	public static SystemMessageId THE_GAME_CLIENT_ENCOUNTERED_AN_ERROR_AND_WAS_UNABLE_TO_CONNECT_TO_THE_PETITION_SERVER;
	
	@ClientString(id = 382, message = "Currently there are no users that have checked out a GM ID.")
	public static SystemMessageId CURRENTLY_THERE_ARE_NO_USERS_THAT_HAVE_CHECKED_OUT_A_GM_ID;
	
	@ClientString(id = 383, message = "Request confirmed to end consultation at petition server.")
	public static SystemMessageId REQUEST_CONFIRMED_TO_END_CONSULTATION_AT_PETITION_SERVER;
	
	@ClientString(id = 384, message = "The client is not logged onto the game server.")
	public static SystemMessageId THE_CLIENT_IS_NOT_LOGGED_ONTO_THE_GAME_SERVER;
	
	@ClientString(id = 385, message = "Request confirmed to begin consultation at petition server.")
	public static SystemMessageId REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION_AT_PETITION_SERVER;
	
	@ClientString(id = 386, message = "The body of your petition must be more than five characters in length.")
	public static SystemMessageId THE_BODY_OF_YOUR_PETITION_MUST_BE_MORE_THAN_FIVE_CHARACTERS_IN_LENGTH;
	
	@ClientString(id = 387, message = "This ends the GM petition consultation. \\n Please take a moment to provide feedback about this service.")
	public static SystemMessageId THIS_ENDS_THE_GM_PETITION_CONSULTATION_N_PLEASE_TAKE_A_MOMENT_TO_PROVIDE_FEEDBACK_ABOUT_THIS_SERVICE;
	
	@ClientString(id = 388, message = "Not under petition consultation.")
	public static SystemMessageId NOT_UNDER_PETITION_CONSULTATION;
	
	@ClientString(id = 389, message = "Your petition application has been accepted. \\n - Receipt No. is $s1.")
	public static SystemMessageId YOUR_PETITION_APPLICATION_HAS_BEEN_ACCEPTED_N_RECEIPT_NO_IS_S1;
	
	@ClientString(id = 390, message = "You may only submit one petition (active) at a time.")
	public static SystemMessageId YOU_MAY_ONLY_SUBMIT_ONE_PETITION_ACTIVE_AT_A_TIME;
	
	@ClientString(id = 391, message = "Receipt No. $s1, petition cancelled.")
	public static SystemMessageId RECEIPT_NO_S1_PETITION_CANCELLED;
	
	@ClientString(id = 392, message = "Under petition advice.")
	public static SystemMessageId UNDER_PETITION_ADVICE;
	
	@ClientString(id = 393, message = "Failed to cancel petition. Please try again later.")
	public static SystemMessageId FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 394, message = "Petition consultation with $s1, under way.")
	public static SystemMessageId PETITION_CONSULTATION_WITH_S1_UNDER_WAY;
	
	@ClientString(id = 395, message = "Ending petition consultation with $s1.")
	public static SystemMessageId ENDING_PETITION_CONSULTATION_WITH_S1;
	
	@ClientString(id = 396, message = "Please login after changing your temporary password.")
	public static SystemMessageId PLEASE_LOGIN_AFTER_CHANGING_YOUR_TEMPORARY_PASSWORD;
	
	@ClientString(id = 397, message = "Not a paid account.")
	public static SystemMessageId NOT_A_PAID_ACCOUNT;
	
	@ClientString(id = 398, message = "There is no time left on this account.")
	public static SystemMessageId THERE_IS_NO_TIME_LEFT_ON_THIS_ACCOUNT;
	
	@ClientString(id = 399, message = "System error.")
	public static SystemMessageId SYSTEM_ERROR;
	
	@ClientString(id = 400, message = "You are attempting to drop $s1. Do you wish to continue?")
	public static SystemMessageId YOU_ARE_ATTEMPTING_TO_DROP_S1_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 401, message = "You currently have too many quests in progress.")
	public static SystemMessageId YOU_CURRENTLY_HAVE_TOO_MANY_QUESTS_IN_PROGRESS;
	
	@ClientString(id = 402, message = "You do not possess the correct ticket to board the boat.")
	public static SystemMessageId YOU_DO_NOT_POSSESS_THE_CORRECT_TICKET_TO_BOARD_THE_BOAT;
	
	@ClientString(id = 403, message = "You have exceeded your out-of-pocket adena limit.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_YOUR_OUT_OF_POCKET_ADENA_LIMIT;
	
	@ClientString(id = 404, message = "Your Create Item level is too low to register this recipe.")
	public static SystemMessageId YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE;
	
	@ClientString(id = 405, message = "The total price of the product is too high.")
	public static SystemMessageId THE_TOTAL_PRICE_OF_THE_PRODUCT_IS_TOO_HIGH;
	
	@ClientString(id = 406, message = "Petition application accepted.")
	public static SystemMessageId PETITION_APPLICATION_ACCEPTED;
	
	@ClientString(id = 407, message = "Petition under process.")
	public static SystemMessageId PETITION_UNDER_PROCESS;
	
	@ClientString(id = 408, message = "Set Period")
	public static SystemMessageId SET_PERIOD;
	
	@ClientString(id = 409, message = "Set Time-$s1: $s2: $s3")
	public static SystemMessageId SET_TIME_S1_S2_S3;
	
	@ClientString(id = 410, message = "Registration Period")
	public static SystemMessageId REGISTRATION_PERIOD;
	
	@ClientString(id = 411, message = "Registration TIme-$s1: $s2: $s3")
	public static SystemMessageId REGISTRATION_TIME_S1_S2_S3;
	
	@ClientString(id = 412, message = "Battle begins in $s1: $s2: $s4")
	public static SystemMessageId BATTLE_BEGINS_IN_S1_S2_S4;
	
	@ClientString(id = 413, message = "Battle ends in $s1: $s2: $s5")
	public static SystemMessageId BATTLE_ENDS_IN_S1_S2_S5;
	
	@ClientString(id = 414, message = "Standby")
	public static SystemMessageId STANDBY;
	
	@ClientString(id = 415, message = "Under Siege")
	public static SystemMessageId UNDER_SIEGE;
	
	@ClientString(id = 416, message = "This item cannot be exchanged.")
	public static SystemMessageId THIS_ITEM_CANNOT_BE_EXCHANGED;
	
	@ClientString(id = 417, message = "$s1 has been disarmed.")
	public static SystemMessageId S1_HAS_BEEN_DISARMED;
	
	@ClientString(id = 418, message = "There is a significant difference between the item's price and its standard price. Please check again.")
	public static SystemMessageId THERE_IS_A_SIGNIFICANT_DIFFERENCE_BETWEEN_THE_ITEM_S_PRICE_AND_ITS_STANDARD_PRICE_PLEASE_CHECK_AGAIN;
	
	@ClientString(id = 419, message = "$s1 minute(s) of usage time left.")
	public static SystemMessageId S1_MINUTE_S_OF_USAGE_TIME_LEFT;
	
	@ClientString(id = 420, message = "Time expired.")
	public static SystemMessageId TIME_EXPIRED;
	
	@ClientString(id = 421, message = "Another person has logged in with the same account.")
	public static SystemMessageId ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT;
	
	@ClientString(id = 422, message = "You have exceeded the weight limit.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT;
	
	@ClientString(id = 423, message = "You have cancelled the enchanting process.")
	public static SystemMessageId YOU_HAVE_CANCELLED_THE_ENCHANTING_PROCESS;
	
	@ClientString(id = 424, message = "Does not fit strengthening conditions of the scroll.")
	public static SystemMessageId DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL;
	
	@ClientString(id = 425, message = "Your Create Item level is too low to register this recipe.")
	public static SystemMessageId YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE_2;
	
	@ClientString(id = 426, message = "Your account has been reported for intentionally not paying the cyber café fees.")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_REPORTED_FOR_INTENTIONALLY_NOT_PAYING_THE_CYBER_CAF_FEES;
	
	@ClientString(id = 427, message = "Please contact us.")
	public static SystemMessageId PLEASE_CONTACT_US;
	
	@ClientString(id = 428, message = "In accordance with company policy, your account has been suspended due to suspicion of illegal use and/or misappropriation of another player's data. Details of the incident(s) in question have been sent to the email address on file with the company. If you are not directly involved with the reported conduct, visit the PlayNC website (http://http://www.plaync.com/us/support/) and go to the Account Appropriation Report Center (Lineage II) to submit an appeal.")
	public static SystemMessageId IN_ACCORDANCE_WITH_COMPANY_POLICY_YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_DUE_TO_SUSPICION_OF_ILLEGAL_USE_AND_OR_MISAPPROPRIATION_OF_ANOTHER_PLAYER_S_DATA_DETAILS_OF_THE_INCIDENT_S_IN_QUESTION_HAVE_BEEN_SENT_TO_THE_EMAIL_ADDRESS_ON_FILE_WITH_THE_COMPANY_IF_YOU_ARE_NOT_DIRECTLY_INVOLVED_WITH_THE_REPORTED_CONDUCT_VISIT_THE_PLAYNC_WEBSITE_HTTP_HTTP_WWW_PLAYNC_COM_US_SUPPORT_AND_GO_TO_THE_ACCOUNT_APPROPRIATION_REPORT_CENTER_LINEAGE_II_TO_SUBMIT_AN_APPEAL;
	
	@ClientString(id = 429, message = "In accordance with company policy, your account has been suspended due to falsely reporting a misappropriation. Submitting an irrelevant report to the Report Center may harm other players. For more information on account suspension, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId IN_ACCORDANCE_WITH_COMPANY_POLICY_YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_DUE_TO_FALSELY_REPORTING_A_MISAPPROPRIATION_SUBMITTING_AN_IRRELEVANT_REPORT_TO_THE_REPORT_CENTER_MAY_HARM_OTHER_PLAYERS_FOR_MORE_INFORMATION_ON_ACCOUNT_SUSPENSION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 430, message = "번역불필요 (Doesn't need to translate.)")
	public static SystemMessageId DOESN_T_NEED_TO_TRANSLATE;
	
	@ClientString(id = 431, message = "Your account has been suspended due to violating the EULA, RoC and/or User Agreement. {Chapter 4, Section 17 of the End User Licence Agreement (Limiting Service Use) : When a user violates the terms of the User Agreement, the company can impose a restriction on the applicable user's account.} For more information on account suspension, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_DUE_TO_VIOLATING_THE_EULA_ROC_AND_OR_USER_AGREEMENT_CHAPTER_4_SECTION_17_OF_THE_END_USER_LICENCE_AGREEMENT_LIMITING_SERVICE_USE_WHEN_A_USER_VIOLATES_THE_TERMS_OF_THE_USER_AGREEMENT_THE_COMPANY_CAN_IMPOSE_A_RESTRICTION_ON_THE_APPLICABLE_USER_S_ACCOUNT_FOR_MORE_INFORMATION_ON_ACCOUNT_SUSPENSION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 432, message = "Your account has been suspended for 7 days (retroactive to the day of disclosure), under Chapter 3, Section 14 of the Lineage II Service Use Agreement, for dealing or attempting to deal items or characters (accounts) within the game in exchange for cash/spots/items of other games. Suspension of your account will automatically expire after 7 days. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_7_DAYS_RETROACTIVE_TO_THE_DAY_OF_DISCLOSURE_UNDER_CHAPTER_3_SECTION_14_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_DEALING_OR_ATTEMPTING_TO_DEAL_ITEMS_OR_CHARACTERS_ACCOUNTS_WITHIN_THE_GAME_IN_EXCHANGE_FOR_CASH_SPOTS_ITEMS_OF_OTHER_GAMES_SUSPENSION_OF_YOUR_ACCOUNT_WILL_AUTOMATICALLY_EXPIRE_AFTER_7_DAYS_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 433, message = "Your account has been suspended, under Chapter 3, Section 14 of the Lineage II Service Use Agreement, for dealing or attempting to deal items or characters (accounts) within the game in exchange for cash/spots/items of other games. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_UNDER_CHAPTER_3_SECTION_14_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_DEALING_OR_ATTEMPTING_TO_DEAL_ITEMS_OR_CHARACTERS_ACCOUNTS_WITHIN_THE_GAME_IN_EXCHANGE_FOR_CASH_SPOTS_ITEMS_OF_OTHER_GAMES_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 434, message = "Your account has been suspended, under Chapter 3, Section 14 of the Lineage II Service Use Agreement, for unethical behavior or fraud. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_UNDER_CHAPTER_3_SECTION_14_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_UNETHICAL_BEHAVIOR_OR_FRAUD_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 435, message = "Your account has been suspended, under Chapter 3 Section 14 of the Lineage II Service Use Agreement, for unethical behavior. For more information, please visit the PlayNC website (http://www.plaync.com/us/support/) and use our Support Center's 1 on 1 inquiry.")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_UNDER_CHAPTER_3_SECTION_14_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_UNETHICAL_BEHAVIOR_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT_AND_USE_OUR_SUPPORT_CENTER_S_1_ON_1_INQUIRY;
	
	@ClientString(id = 436, message = "Your account has been suspended, under Chapter 3, Section 14 of the Lineage II Service Use Agreement, for abusing the game system or exploiting bug(s). Abusing bug(s) may cause critical situations as well as harm the game world's balance. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_UNDER_CHAPTER_3_SECTION_14_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_ABUSING_THE_GAME_SYSTEM_OR_EXPLOITING_BUG_S_ABUSING_BUG_S_MAY_CAUSE_CRITICAL_SITUATIONS_AS_WELL_AS_HARM_THE_GAME_WORLD_S_BALANCE_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 437, message = "Your account has been suspended, under Chapter 3, Section 14 of the Lineage II Service Use Agreement, for using illegal software which has not been authenticated by our company. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_UNDER_CHAPTER_3_SECTION_14_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_USING_ILLEGAL_SOFTWARE_WHICH_HAS_NOT_BEEN_AUTHENTICATED_BY_OUR_COMPANY_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 438, message = "Your account has been suspended, under Chapter 3, Section 14 of the Lineage II Service Use Agreement, for impersonating an official Game Master or staff member. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_UNDER_CHAPTER_3_SECTION_14_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_IMPERSONATING_AN_OFFICIAL_GAME_MASTER_OR_STAFF_MEMBER_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 439, message = "In accordance with the company's User Agreement and Operational Policy this account has been suspended at the account holder's request. If you have any questions regarding your account please contact support at http://support.plaync.com")
	public static SystemMessageId IN_ACCORDANCE_WITH_THE_COMPANY_S_USER_AGREEMENT_AND_OPERATIONAL_POLICY_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_AT_THE_ACCOUNT_HOLDER_S_REQUEST_IF_YOU_HAVE_ANY_QUESTIONS_REGARDING_YOUR_ACCOUNT_PLEASE_CONTACT_SUPPORT_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 440, message = "Because you are registered as a minor, your account has been suspended at the request of your parents or guardian. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId BECAUSE_YOU_ARE_REGISTERED_AS_A_MINOR_YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_AT_THE_REQUEST_OF_YOUR_PARENTS_OR_GUARDIAN_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 441, message = "Per our company's User Agreement, the use of this account has been suspended. If you have any questions regarding your account please contact support at http://support.plaync.com.")
	public static SystemMessageId PER_OUR_COMPANY_S_USER_AGREEMENT_THE_USE_OF_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_IF_YOU_HAVE_ANY_QUESTIONS_REGARDING_YOUR_ACCOUNT_PLEASE_CONTACT_SUPPORT_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 442, message = "Your account has been suspended, under Chapter 2, Section 7 of the Lineage II Service Use Agreement, for misappropriating payment under another player's account. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_UNDER_CHAPTER_2_SECTION_7_OF_THE_LINEAGE_II_SERVICE_USE_AGREEMENT_FOR_MISAPPROPRIATING_PAYMENT_UNDER_ANOTHER_PLAYER_S_ACCOUNT_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 443, message = "The identity of this account has not been veen verified. Therefore, Lineage II service for this account is currently unavailable. To verify your identity, please fax a copy of your social security card, driver's license, passport, medical insurance card, etc. to 02-2186-3282. Include your account, name and contact information. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId THE_IDENTITY_OF_THIS_ACCOUNT_HAS_NOT_BEEN_VEEN_VERIFIED_THEREFORE_LINEAGE_II_SERVICE_FOR_THIS_ACCOUNT_IS_CURRENTLY_UNAVAILABLE_TO_VERIFY_YOUR_IDENTITY_PLEASE_FAX_A_COPY_OF_YOUR_SOCIAL_SECURITY_CARD_DRIVER_S_LICENSE_PASSPORT_MEDICAL_INSURANCE_CARD_ETC_TO_02_2186_3282_INCLUDE_YOUR_ACCOUNT_NAME_AND_CONTACT_INFORMATION_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 444, message = "Since we have received a withdrawal request from the holder of this account access to all applicable accounts has been automatically suspended.")
	public static SystemMessageId SINCE_WE_HAVE_RECEIVED_A_WITHDRAWAL_REQUEST_FROM_THE_HOLDER_OF_THIS_ACCOUNT_ACCESS_TO_ALL_APPLICABLE_ACCOUNTS_HAS_BEEN_AUTOMATICALLY_SUSPENDED;
	
	@ClientString(id = 445, message = "(Reference Number Regarding Membership Withdrawal Request: $s1)")
	public static SystemMessageId REFERENCE_NUMBER_REGARDING_MEMBERSHIP_WITHDRAWAL_REQUEST_S1;
	
	@ClientString(id = 446, message = "For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 447, message = ".")
	public static SystemMessageId EMPTY;
	
	@ClientString(id = 448, message = "System error, please log in again later.")
	public static SystemMessageId SYSTEM_ERROR_PLEASE_LOG_IN_AGAIN_LATER;
	
	@ClientString(id = 449, message = "Password does not match this account.")
	public static SystemMessageId PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT;
	
	@ClientString(id = 450, message = "Confirm your account information and log in again later.")
	public static SystemMessageId CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_LOG_IN_AGAIN_LATER;
	
	@ClientString(id = 451, message = "The password you have entered is incorrect.")
	public static SystemMessageId THE_PASSWORD_YOU_HAVE_ENTERED_IS_INCORRECT;
	
	@ClientString(id = 452, message = "Please confirm your account information and try logging in again.")
	public static SystemMessageId PLEASE_CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_TRY_LOGGING_IN_AGAIN;
	
	@ClientString(id = 453, message = "Your account information is incorrect.")
	public static SystemMessageId YOUR_ACCOUNT_INFORMATION_IS_INCORRECT;
	
	@ClientString(id = 454, message = "For more details, please contact our Support Center at http://support.plaync.com")
	public static SystemMessageId FOR_MORE_DETAILS_PLEASE_CONTACT_OUR_SUPPORT_CENTER_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 455, message = "This account is already in use. Access denied.")
	public static SystemMessageId THIS_ACCOUNT_IS_ALREADY_IN_USE_ACCESS_DENIED;
	
	@ClientString(id = 456, message = "Lineage II game services may be used by individuals 15 years of age or older except for PvP servers, which may only be used by adults 18 years of age and older. (Korea Only)")
	public static SystemMessageId LINEAGE_II_GAME_SERVICES_MAY_BE_USED_BY_INDIVIDUALS_15_YEARS_OF_AGE_OR_OLDER_EXCEPT_FOR_PVP_SERVERS_WHICH_MAY_ONLY_BE_USED_BY_ADULTS_18_YEARS_OF_AGE_AND_OLDER_KOREA_ONLY;
	
	@ClientString(id = 457, message = "Server under maintenance. Please try again later.")
	public static SystemMessageId SERVER_UNDER_MAINTENANCE_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 458, message = "Your usage term has expired.")
	public static SystemMessageId YOUR_USAGE_TERM_HAS_EXPIRED;
	
	@ClientString(id = 459, message = "PlayNC website (http://www.plaync.com/us/support/)")
	public static SystemMessageId PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 460, message = "to reactivate your account.")
	public static SystemMessageId TO_REACTIVATE_YOUR_ACCOUNT;
	
	@ClientString(id = 461, message = "Access failed.")
	public static SystemMessageId ACCESS_FAILED;
	
	@ClientString(id = 462, message = "Please try again later.")
	public static SystemMessageId PLEASE_TRY_AGAIN_LATER_2;
	
	@ClientString(id = 463, message = ".")
	public static SystemMessageId EMPTY_2;
	
	@ClientString(id = 464, message = "This feature is only available alliance leaders.")
	public static SystemMessageId THIS_FEATURE_IS_ONLY_AVAILABLE_ALLIANCE_LEADERS;
	
	@ClientString(id = 465, message = "You are not currently allied with any clans.")
	public static SystemMessageId YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS;
	
	@ClientString(id = 466, message = "You have exceeded the limit.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_LIMIT;
	
	@ClientString(id = 467, message = "You may not accept any clan within a day after expelling another clan.")
	public static SystemMessageId YOU_MAY_NOT_ACCEPT_ANY_CLAN_WITHIN_A_DAY_AFTER_EXPELLING_ANOTHER_CLAN;
	
	@ClientString(id = 468, message = "A clan that has withdrawn or been expelled cannot enter into an alliance within one day of withdrawal or expulsion.")
	public static SystemMessageId A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION;
	
	@ClientString(id = 469, message = "You may not ally with a clan you are currently at war with. That would be diabolical and treacherous.")
	public static SystemMessageId YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_CURRENTLY_AT_WAR_WITH_THAT_WOULD_BE_DIABOLICAL_AND_TREACHEROUS;
	
	@ClientString(id = 470, message = "Only the clan leader may apply for withdrawal from the alliance.")
	public static SystemMessageId ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE;
	
	@ClientString(id = 471, message = "Alliance leaders cannot withdraw.")
	public static SystemMessageId ALLIANCE_LEADERS_CANNOT_WITHDRAW;
	
	@ClientString(id = 472, message = "You cannot expel yourself from the clan.")
	public static SystemMessageId YOU_CANNOT_EXPEL_YOURSELF_FROM_THE_CLAN;
	
	@ClientString(id = 473, message = "Different alliance.")
	public static SystemMessageId DIFFERENT_ALLIANCE;
	
	@ClientString(id = 474, message = "That clan does not exist.")
	public static SystemMessageId THAT_CLAN_DOES_NOT_EXIST;
	
	@ClientString(id = 475, message = "Different alliance.")
	public static SystemMessageId DIFFERENT_ALLIANCE_2;
	
	@ClientString(id = 476, message = "Please adjust the image size to 8x12.")
	public static SystemMessageId PLEASE_ADJUST_THE_IMAGE_SIZE_TO_8X12;
	
	@ClientString(id = 477, message = "No response. Invitation to join an alliance has been cancelled.")
	public static SystemMessageId NO_RESPONSE_INVITATION_TO_JOIN_AN_ALLIANCE_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 478, message = "No response. Your entrance to the alliance has been cancelled.")
	public static SystemMessageId NO_RESPONSE_YOUR_ENTRANCE_TO_THE_ALLIANCE_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 479, message = "$s1 has joined as a friend.")
	public static SystemMessageId S1_HAS_JOINED_AS_A_FRIEND;
	
	@ClientString(id = 480, message = "Please check your friends list.")
	public static SystemMessageId PLEASE_CHECK_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 481, message = "$s1 has been deleted from your friends list.")
	public static SystemMessageId S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 482, message = "You cannot add yourself to your own friend list.")
	public static SystemMessageId YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST_2;
	
	@ClientString(id = 483, message = "This function is inaccessible right now. Please try again later.")
	public static SystemMessageId THIS_FUNCTION_IS_INACCESSIBLE_RIGHT_NOW_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 484, message = "This player is already registered in your friends list.")
	public static SystemMessageId THIS_PLAYER_IS_ALREADY_REGISTERED_IN_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 485, message = "No new friend invitations may be accepted.")
	public static SystemMessageId NO_NEW_FRIEND_INVITATIONS_MAY_BE_ACCEPTED;
	
	@ClientString(id = 486, message = "The following user is not in your friends list.")
	public static SystemMessageId THE_FOLLOWING_USER_IS_NOT_IN_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 487, message = "======<Friends List>======")
	public static SystemMessageId FRIENDS_LIST;
	
	@ClientString(id = 488, message = "$s1 (Currently: Online)")
	public static SystemMessageId S1_CURRENTLY_ONLINE;
	
	@ClientString(id = 489, message = "$s1 (Currently: Offline)")
	public static SystemMessageId S1_CURRENTLY_OFFLINE;
	
	@ClientString(id = 490, message = "========================")
	public static SystemMessageId EMPTY_3;
	
	@ClientString(id = 491, message = "=======<Alliance Information>=======")
	public static SystemMessageId ALLIANCE_INFORMATION;
	
	@ClientString(id = 492, message = "Alliance Name: $s1")
	public static SystemMessageId ALLIANCE_NAME_S1;
	
	@ClientString(id = 493, message = "Connection: $s1 / Total $s2")
	public static SystemMessageId CONNECTION_S1_TOTAL_S2;
	
	@ClientString(id = 494, message = "Alliance Leader: $s2 of $s1")
	public static SystemMessageId ALLIANCE_LEADER_S2_OF_S1;
	
	@ClientString(id = 495, message = "Affiliated clans: Total $s1 clan(s)")
	public static SystemMessageId AFFILIATED_CLANS_TOTAL_S1_CLAN_S;
	
	@ClientString(id = 496, message = "=====<Clan Information>=====")
	public static SystemMessageId CLAN_INFORMATION;
	
	@ClientString(id = 497, message = "Clan Name: $s1")
	public static SystemMessageId CLAN_NAME_S1;
	
	@ClientString(id = 498, message = "Clan Leader: $s1")
	public static SystemMessageId CLAN_LEADER_S1;
	
	@ClientString(id = 499, message = "Clan Level: $s1")
	public static SystemMessageId CLAN_LEVEL_S1;
	
	@ClientString(id = 500, message = "------------------------")
	public static SystemMessageId EMPTY_4;
	
	@ClientString(id = 501, message = "========================")
	public static SystemMessageId EMPTY_5;
	
	@ClientString(id = 502, message = "You already belong to another alliance.")
	public static SystemMessageId YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE;
	
	@ClientString(id = 503, message = "$s1 (Friend) has logged in.")
	public static SystemMessageId S1_FRIEND_HAS_LOGGED_IN;
	
	@ClientString(id = 504, message = "Only clan leaders may create alliances.")
	public static SystemMessageId ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES;
	
	@ClientString(id = 505, message = "You cannot create a new alliance within 10 days after dissolution.")
	public static SystemMessageId YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_10_DAYS_AFTER_DISSOLUTION;
	
	@ClientString(id = 506, message = "Incorrect alliance name. Please try again.")
	public static SystemMessageId INCORRECT_ALLIANCE_NAME_PLEASE_TRY_AGAIN;
	
	@ClientString(id = 507, message = "Incorrect length for an alliance name.")
	public static SystemMessageId INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME;
	
	@ClientString(id = 508, message = "This alliance name already exists.")
	public static SystemMessageId THIS_ALLIANCE_NAME_ALREADY_EXISTS;
	
	@ClientString(id = 509, message = "Cannot accept. clan ally is registered as an enemy during siege battle.")
	public static SystemMessageId CANNOT_ACCEPT_CLAN_ALLY_IS_REGISTERED_AS_AN_ENEMY_DURING_SIEGE_BATTLE;
	
	@ClientString(id = 510, message = "You have invited someone to your alliance.")
	public static SystemMessageId YOU_HAVE_INVITED_SOMEONE_TO_YOUR_ALLIANCE;
	
	@ClientString(id = 511, message = "You must first select a user to invite.")
	public static SystemMessageId YOU_MUST_FIRST_SELECT_A_USER_TO_INVITE;
	
	@ClientString(id = 512, message = "Do you really wish to withdraw from the alliance?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_WITHDRAW_FROM_THE_ALLIANCE;
	
	@ClientString(id = 513, message = "Enter the name of the clan you wish to expel.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_EXPEL;
	
	@ClientString(id = 514, message = "Do you really wish to dissolve the alliance?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_DISSOLVE_THE_ALLIANCE;
	
	@ClientString(id = 515, message = "Enter a file name for the alliance crest.")
	public static SystemMessageId ENTER_A_FILE_NAME_FOR_THE_ALLIANCE_CREST;
	
	@ClientString(id = 516, message = "$s1 has invited you to be their friend.")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_BE_THEIR_FRIEND;
	
	@ClientString(id = 517, message = "You have accepted the alliance.")
	public static SystemMessageId YOU_HAVE_ACCEPTED_THE_ALLIANCE;
	
	@ClientString(id = 518, message = "You have failed to invite a clan into the alliance.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE;
	
	@ClientString(id = 519, message = "You have withdrawn from the alliance.")
	public static SystemMessageId YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE;
	
	@ClientString(id = 520, message = "You have failed to withdraw from the alliance.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE;
	
	@ClientString(id = 521, message = "You have succeeded in expelling a clan.")
	public static SystemMessageId YOU_HAVE_SUCCEEDED_IN_EXPELLING_A_CLAN;
	
	@ClientString(id = 522, message = "You have failed to expel a clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EXPEL_A_CLAN;
	
	@ClientString(id = 523, message = "The alliance has been dissolved.")
	public static SystemMessageId THE_ALLIANCE_HAS_BEEN_DISSOLVED;
	
	@ClientString(id = 524, message = "You have failed to dissolve the alliance.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE;
	
	@ClientString(id = 525, message = "You have succeeded in inviting a friend to your friends list.")
	public static SystemMessageId YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND_TO_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 526, message = "You have failed to add a friend to your friends list.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 527, message = "$s1 leader, $s2, has requested an alliance.")
	public static SystemMessageId S1_LEADER_S2_HAS_REQUESTED_AN_ALLIANCE;
	
	@ClientString(id = 528, message = "Unable to find file at target location.")
	public static SystemMessageId UNABLE_TO_FIND_FILE_AT_TARGET_LOCATION;
	
	@ClientString(id = 529, message = "You may only register an 8 x 12 pixel, 256-color BMP.")
	public static SystemMessageId YOU_MAY_ONLY_REGISTER_AN_8_X_12_PIXEL_256_COLOR_BMP;
	
	@ClientString(id = 530, message = "The Spiritshot does not match the weapon's grade.")
	public static SystemMessageId THE_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE;
	
	@ClientString(id = 531, message = "You do not have enough Spiritshots for that.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOTS_FOR_THAT;
	
	@ClientString(id = 532, message = "You may not use Spiritshots.")
	public static SystemMessageId YOU_MAY_NOT_USE_SPIRITSHOTS;
	
	@ClientString(id = 533, message = "Power of Mana enabled.")
	public static SystemMessageId POWER_OF_MANA_ENABLED;
	
	@ClientString(id = 534, message = "Power of Mana disabled.")
	public static SystemMessageId POWER_OF_MANA_DISABLED;
	
	@ClientString(id = 535, message = "Enter a name for your pet.")
	public static SystemMessageId ENTER_A_NAME_FOR_YOUR_PET;
	
	@ClientString(id = 536, message = "How much adena do you wish to transfer to your Inventory?")
	public static SystemMessageId HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_INVENTORY;
	
	@ClientString(id = 537, message = "How much will you transfer?")
	public static SystemMessageId HOW_MUCH_WILL_YOU_TRANSFER;
	
	@ClientString(id = 538, message = "Your SP has decreased by $s1.")
	public static SystemMessageId YOUR_SP_HAS_DECREASED_BY_S1;
	
	@ClientString(id = 539, message = "Your Experience has decreased by $s1.")
	public static SystemMessageId YOUR_EXPERIENCE_HAS_DECREASED_BY_S1;
	
	@ClientString(id = 540, message = "Clan leaders may not be deleted. Dissolve the clan first and try again.")
	public static SystemMessageId CLAN_LEADERS_MAY_NOT_BE_DELETED_DISSOLVE_THE_CLAN_FIRST_AND_TRY_AGAIN;
	
	@ClientString(id = 541, message = "You may not delete a clan member. Withdraw from the clan first and try again.")
	public static SystemMessageId YOU_MAY_NOT_DELETE_A_CLAN_MEMBER_WITHDRAW_FROM_THE_CLAN_FIRST_AND_TRY_AGAIN;
	
	@ClientString(id = 542, message = "The NPC server is currently down. Pets and servitors cannot be summoned at this time.")
	public static SystemMessageId THE_NPC_SERVER_IS_CURRENTLY_DOWN_PETS_AND_SERVITORS_CANNOT_BE_SUMMONED_AT_THIS_TIME;
	
	@ClientString(id = 543, message = "You already have a pet.")
	public static SystemMessageId YOU_ALREADY_HAVE_A_PET;
	
	@ClientString(id = 544, message = "Your pet cannot carry this item.")
	public static SystemMessageId YOUR_PET_CANNOT_CARRY_THIS_ITEM;
	
	@ClientString(id = 545, message = "Your pet cannot carry any more items. Remove some, then try again.")
	public static SystemMessageId YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS_REMOVE_SOME_THEN_TRY_AGAIN;
	
	@ClientString(id = 546, message = "Unable to place item, your pet is too encumbered.")
	public static SystemMessageId UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED;
	
	@ClientString(id = 547, message = "Summoning your pet…")
	public static SystemMessageId SUMMONING_YOUR_PET;
	
	@ClientString(id = 548, message = "Your pet's name can be up to 8 characters in length.")
	public static SystemMessageId YOUR_PET_S_NAME_CAN_BE_UP_TO_8_CHARACTERS_IN_LENGTH;
	
	@ClientString(id = 549, message = "To create an alliance, your clan must be Level 5 or higher.")
	public static SystemMessageId TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER;
	
	@ClientString(id = 550, message = "You may not create an alliance during the term of dissolution postponement.")
	public static SystemMessageId YOU_MAY_NOT_CREATE_AN_ALLIANCE_DURING_THE_TERM_OF_DISSOLUTION_POSTPONEMENT;
	
	@ClientString(id = 551, message = "You cannot raise your clan level during the term of dispersion postponement.")
	public static SystemMessageId YOU_CANNOT_RAISE_YOUR_CLAN_LEVEL_DURING_THE_TERM_OF_DISPERSION_POSTPONEMENT;
	
	@ClientString(id = 552, message = "During the grace period for dissolving a clan, the registration or deletion of a clan's crest is not allowed.")
	public static SystemMessageId DURING_THE_GRACE_PERIOD_FOR_DISSOLVING_A_CLAN_THE_REGISTRATION_OR_DELETION_OF_A_CLAN_S_CREST_IS_NOT_ALLOWED;
	
	@ClientString(id = 553, message = "The opposing clan has applied for dispersion.")
	public static SystemMessageId THE_OPPOSING_CLAN_HAS_APPLIED_FOR_DISPERSION;
	
	@ClientString(id = 554, message = "You cannot disperse the clans in your alliance.")
	public static SystemMessageId YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE;
	
	@ClientString(id = 555, message = "You cannot move - you are too encumbered.")
	public static SystemMessageId YOU_CANNOT_MOVE_YOU_ARE_TOO_ENCUMBERED;
	
	@ClientString(id = 556, message = "You cannot move in this state.")
	public static SystemMessageId YOU_CANNOT_MOVE_IN_THIS_STATE;
	
	@ClientString(id = 557, message = "Your pet has been summoned and may not be destroyed.")
	public static SystemMessageId YOUR_PET_HAS_BEEN_SUMMONED_AND_MAY_NOT_BE_DESTROYED;
	
	@ClientString(id = 558, message = "Your pet has been summoned and cannot be let go.")
	public static SystemMessageId YOUR_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_LET_GO;
	
	@ClientString(id = 559, message = "You have purchased $s2 from $s1.")
	public static SystemMessageId YOU_HAVE_PURCHASED_S2_FROM_S1;
	
	@ClientString(id = 560, message = "You have purchased +$s2 $s3 from $s1.")
	public static SystemMessageId YOU_HAVE_PURCHASED_S2_S3_FROM_S1;
	
	@ClientString(id = 561, message = "You have purchased $s3 $s2(s) from $s1.")
	public static SystemMessageId YOU_HAVE_PURCHASED_S3_S2_S_FROM_S1;
	
	@ClientString(id = 562, message = "You may not crystallize this item. Your crystallization skill level is too low.")
	public static SystemMessageId YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW;
	
	@ClientString(id = 563, message = "Failed to disable attack target.")
	public static SystemMessageId FAILED_TO_DISABLE_ATTACK_TARGET;
	
	@ClientString(id = 564, message = "Failed to change attack target.")
	public static SystemMessageId FAILED_TO_CHANGE_ATTACK_TARGET;
	
	@ClientString(id = 565, message = "Not enough luck.")
	public static SystemMessageId NOT_ENOUGH_LUCK;
	
	@ClientString(id = 566, message = "Your confusion spell failed.")
	public static SystemMessageId YOUR_CONFUSION_SPELL_FAILED;
	
	@ClientString(id = 567, message = "Your fear spell failed.")
	public static SystemMessageId YOUR_FEAR_SPELL_FAILED;
	
	@ClientString(id = 568, message = "Cubic Summoning failed.")
	public static SystemMessageId CUBIC_SUMMONING_FAILED;
	
	@ClientString(id = 569, message = "Caution -- this item's price greatly differs from non-player run shops. Do you wish to continue?")
	public static SystemMessageId CAUTION_THIS_ITEM_S_PRICE_GREATLY_DIFFERS_FROM_NON_PLAYER_RUN_SHOPS_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 570, message = "How many $s1(s) do you want to purchase?")
	public static SystemMessageId HOW_MANY_S1_S_DO_YOU_WANT_TO_PURCHASE;
	
	@ClientString(id = 571, message = "How many $s1(s) do you want to purchase?")
	public static SystemMessageId HOW_MANY_S1_S_DO_YOU_WANT_TO_PURCHASE_2;
	
	@ClientString(id = 572, message = "Do you wish to join $s1's party? (Item distribution: Finders Keepers)")
	public static SystemMessageId DO_YOU_WISH_TO_JOIN_S1_S_PARTY_ITEM_DISTRIBUTION_FINDERS_KEEPERS;
	
	@ClientString(id = 573, message = "Do you wish to join $s1's party? (Item distribution: Random)")
	public static SystemMessageId DO_YOU_WISH_TO_JOIN_S1_S_PARTY_ITEM_DISTRIBUTION_RANDOM;
	
	@ClientString(id = 574, message = "Pets and Servitors are not available at this time.")
	public static SystemMessageId PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME;
	
	@ClientString(id = 575, message = "How much adena do you wish to transfer to your pet?")
	public static SystemMessageId HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_PET;
	
	@ClientString(id = 576, message = "How much do you wish to transfer?")
	public static SystemMessageId HOW_MUCH_DO_YOU_WISH_TO_TRANSFER;
	
	@ClientString(id = 577, message = "You cannot summon during a trade or while using the private shops.")
	public static SystemMessageId YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS;
	
	@ClientString(id = 578, message = "You cannot summon during combat.")
	public static SystemMessageId YOU_CANNOT_SUMMON_DURING_COMBAT;
	
	@ClientString(id = 579, message = "A pet cannot be sent back during battle.")
	public static SystemMessageId A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE;
	
	@ClientString(id = 580, message = "You may not use multiple pets or servitors at the same time.")
	public static SystemMessageId YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME;
	
	@ClientString(id = 581, message = "There is a space in the name.")
	public static SystemMessageId THERE_IS_A_SPACE_IN_THE_NAME;
	
	@ClientString(id = 582, message = "Inappropriate character name.")
	public static SystemMessageId INAPPROPRIATE_CHARACTER_NAME;
	
	@ClientString(id = 583, message = "Name includes forbidden words.")
	public static SystemMessageId NAME_INCLUDES_FORBIDDEN_WORDS;
	
	@ClientString(id = 584, message = "This is already in use by another pet.")
	public static SystemMessageId THIS_IS_ALREADY_IN_USE_BY_ANOTHER_PET;
	
	@ClientString(id = 585, message = "Please decide on the price.")
	public static SystemMessageId PLEASE_DECIDE_ON_THE_PRICE;
	
	@ClientString(id = 586, message = "Pet items cannot be registered as shortcuts.")
	public static SystemMessageId PET_ITEMS_CANNOT_BE_REGISTERED_AS_SHORTCUTS;
	
	@ClientString(id = 587, message = "Irregular system speed.")
	public static SystemMessageId IRREGULAR_SYSTEM_SPEED;
	
	@ClientString(id = 588, message = "Your pet's inventory is full.")
	public static SystemMessageId YOUR_PET_S_INVENTORY_IS_FULL;
	
	@ClientString(id = 589, message = "A dead pet cannot be sent back.")
	public static SystemMessageId A_DEAD_PET_CANNOT_BE_SENT_BACK;
	
	@ClientString(id = 590, message = "Your pet is motionless and any attempt you make to give it something goes unrecognized.")
	public static SystemMessageId YOUR_PET_IS_MOTIONLESS_AND_ANY_ATTEMPT_YOU_MAKE_TO_GIVE_IT_SOMETHING_GOES_UNRECOGNIZED;
	
	@ClientString(id = 591, message = "An invalid character is included in the pet's name.")
	public static SystemMessageId AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PET_S_NAME;
	
	@ClientString(id = 592, message = "Do you wish to dismiss your pet? Dismissing your pet will cause the pet necklace to disappear.")
	public static SystemMessageId DO_YOU_WISH_TO_DISMISS_YOUR_PET_DISMISSING_YOUR_PET_WILL_CAUSE_THE_PET_NECKLACE_TO_DISAPPEAR;
	
	@ClientString(id = 593, message = "Starving, grumpy and fed up, your pet has left.")
	public static SystemMessageId STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT;
	
	@ClientString(id = 594, message = "You may not restore a hungry pet.")
	public static SystemMessageId YOU_MAY_NOT_RESTORE_A_HUNGRY_PET;
	
	@ClientString(id = 595, message = "Your pet is very hungry.")
	public static SystemMessageId YOUR_PET_IS_VERY_HUNGRY;
	
	@ClientString(id = 596, message = "Your pet ate a little, but is still hungry.")
	public static SystemMessageId YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY;
	
	@ClientString(id = 597, message = "Your pet is very hungry. Please be careful.")
	public static SystemMessageId YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL;
	
	@ClientString(id = 598, message = "You may not chat while you are invisible.")
	public static SystemMessageId YOU_MAY_NOT_CHAT_WHILE_YOU_ARE_INVISIBLE;
	
	@ClientString(id = 599, message = "The GM has an imprtant notice. Chat has been temporarily disabled.")
	public static SystemMessageId THE_GM_HAS_AN_IMPRTANT_NOTICE_CHAT_HAS_BEEN_TEMPORARILY_DISABLED;
	
	@ClientString(id = 600, message = "You may not equip a pet item.")
	public static SystemMessageId YOU_MAY_NOT_EQUIP_A_PET_ITEM;
	
	@ClientString(id = 601, message = "There are $S1 petitions currently on the waiting list.")
	public static SystemMessageId THERE_ARE_S1_PETITIONS_CURRENTLY_ON_THE_WAITING_LIST;
	
	@ClientString(id = 602, message = "The petition system is currently unavailable. Please try again later.")
	public static SystemMessageId THE_PETITION_SYSTEM_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 603, message = "That item cannot be discarded or exchanged.")
	public static SystemMessageId THAT_ITEM_CANNOT_BE_DISCARDED_OR_EXCHANGED;
	
	@ClientString(id = 604, message = "You may not call forth a pet or summoned creature from this location.")
	public static SystemMessageId YOU_MAY_NOT_CALL_FORTH_A_PET_OR_SUMMONED_CREATURE_FROM_THIS_LOCATION;
	
	@ClientString(id = 605, message = "You may register up to 64 people on your list.")
	public static SystemMessageId YOU_MAY_REGISTER_UP_TO_64_PEOPLE_ON_YOUR_LIST;
	
	@ClientString(id = 606, message = "You cannot be registered because the other person has already registered 64 people on his/her list.")
	public static SystemMessageId YOU_CANNOT_BE_REGISTERED_BECAUSE_THE_OTHER_PERSON_HAS_ALREADY_REGISTERED_64_PEOPLE_ON_HIS_HER_LIST;
	
	@ClientString(id = 607, message = "You do not have any further skills to learn. Come back when you have reached Level $s1.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1;
	
	@ClientString(id = 608, message = "$s1 has obtained $s3 $s2 by using Sweeper.")
	public static SystemMessageId S1_HAS_OBTAINED_S3_S2_BY_USING_SWEEPER;
	
	@ClientString(id = 609, message = "$s1 has obtained $s2 by using Sweeper.")
	public static SystemMessageId S1_HAS_OBTAINED_S2_BY_USING_SWEEPER;
	
	@ClientString(id = 610, message = "Your skill has been canceled due to lack of HP.")
	public static SystemMessageId YOUR_SKILL_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_HP;
	
	@ClientString(id = 611, message = "You have succeeded in Confusing the enemy.")
	public static SystemMessageId YOU_HAVE_SUCCEEDED_IN_CONFUSING_THE_ENEMY;
	
	@ClientString(id = 612, message = "The Spoil condition has been activated.")
	public static SystemMessageId THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 613, message = "======<Ignore List>======")
	public static SystemMessageId IGNORE_LIST;
	
	@ClientString(id = 614, message = "$s1 $s2")
	public static SystemMessageId S1_S2;
	
	@ClientString(id = 615, message = "You have failed to register the user to your Ignore List.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST;
	
	@ClientString(id = 616, message = "You have failed to delete the character.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_2;
	
	@ClientString(id = 617, message = "$s1 has been added to your Ignore List.")
	public static SystemMessageId S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST;
	
	@ClientString(id = 618, message = "$s1 has been removed from your Ignore List.")
	public static SystemMessageId S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST;
	
	@ClientString(id = 619, message = "$s1 has placed you on his/her Ignore List.")
	public static SystemMessageId S1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST;
	
	@ClientString(id = 620, message = "$s1 has placed you on his/her Ignore List.")
	public static SystemMessageId S1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST_2;
	
	@ClientString(id = 621, message = "This server is reserved for players in Korea. To play Lineage II, please connect to the server in your region.")
	public static SystemMessageId THIS_SERVER_IS_RESERVED_FOR_PLAYERS_IN_KOREA_TO_PLAY_LINEAGE_II_PLEASE_CONNECT_TO_THE_SERVER_IN_YOUR_REGION;
	
	@ClientString(id = 622, message = "You may not make a declaration of war during an alliance battle.")
	public static SystemMessageId YOU_MAY_NOT_MAKE_A_DECLARATION_OF_WAR_DURING_AN_ALLIANCE_BATTLE;
	
	@ClientString(id = 623, message = "Your opponent has exceeded the number of simultaneous alliance battles allowed.")
	public static SystemMessageId YOUR_OPPONENT_HAS_EXCEEDED_THE_NUMBER_OF_SIMULTANEOUS_ALLIANCE_BATTLES_ALLOWED;
	
	@ClientString(id = 624, message = "$s1 Clan leader is not currently connected to the game server.")
	public static SystemMessageId S1_CLAN_LEADER_IS_NOT_CURRENTLY_CONNECTED_TO_THE_GAME_SERVER;
	
	@ClientString(id = 625, message = "Your request for Alliance Battle truce has been denied.")
	public static SystemMessageId YOUR_REQUEST_FOR_ALLIANCE_BATTLE_TRUCE_HAS_BEEN_DENIED;
	
	@ClientString(id = 626, message = "The $s1 clan did not respond: war proclamation has been refused.")
	public static SystemMessageId THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED_2;
	
	@ClientString(id = 627, message = "Clan battle has been refused because you did not respond to $s1 clan's war proclamation.")
	public static SystemMessageId CLAN_BATTLE_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLAN_S_WAR_PROCLAMATION;
	
	@ClientString(id = 628, message = "You have already been at war with the $s1 clan: 5 days must pass before you can declare war again.")
	public static SystemMessageId YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_DECLARE_WAR_AGAIN;
	
	@ClientString(id = 629, message = "Your opponent has exceeded the number of simultaneous alliance battles allowed.")
	public static SystemMessageId YOUR_OPPONENT_HAS_EXCEEDED_THE_NUMBER_OF_SIMULTANEOUS_ALLIANCE_BATTLES_ALLOWED_2;
	
	@ClientString(id = 630, message = "War with the $s1 clan has begun.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_BEGUN_2;
	
	@ClientString(id = 631, message = "War with the $s1 clan is over.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_IS_OVER;
	
	@ClientString(id = 632, message = "You have won the war over the $s1 clan!")
	public static SystemMessageId YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN_2;
	
	@ClientString(id = 633, message = "You have surrendered to the $s1 clan.")
	public static SystemMessageId YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN_2;
	
	@ClientString(id = 634, message = "Your alliance leader has been slain. You have been defeated by the $s1 clan.")
	public static SystemMessageId YOUR_ALLIANCE_LEADER_HAS_BEEN_SLAIN_YOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN;
	
	@ClientString(id = 635, message = "The time limit for the clan war has been exceeded. War with the $s1 clan is over.")
	public static SystemMessageId THE_TIME_LIMIT_FOR_THE_CLAN_WAR_HAS_BEEN_EXCEEDED_WAR_WITH_THE_S1_CLAN_IS_OVER;
	
	@ClientString(id = 636, message = "You are not involved in a clan war.")
	public static SystemMessageId YOU_ARE_NOT_INVOLVED_IN_A_CLAN_WAR_2;
	
	@ClientString(id = 637, message = "A clan ally has registered itself to the opponent.")
	public static SystemMessageId A_CLAN_ALLY_HAS_REGISTERED_ITSELF_TO_THE_OPPONENT;
	
	@ClientString(id = 638, message = "You have already requested a Siege Battle.")
	public static SystemMessageId YOU_HAVE_ALREADY_REQUESTED_A_SIEGE_BATTLE;
	
	@ClientString(id = 639, message = "Your application has been denied because you have already submitted a request for another Siege Battle.")
	public static SystemMessageId YOUR_APPLICATION_HAS_BEEN_DENIED_BECAUSE_YOU_HAVE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE;
	
	@ClientString(id = 640, message = "You have failed to refuse castle defense aid.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_REFUSE_CASTLE_DEFENSE_AID;
	
	@ClientString(id = 641, message = "You have failed to approve castle defense aid.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_APPROVE_CASTLE_DEFENSE_AID;
	
	@ClientString(id = 642, message = "You are already registered to the attacker side and must cancel your registration before submitting your request.")
	public static SystemMessageId YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST;
	
	@ClientString(id = 643, message = "You have already registered to the defender side and must cancel your registration before submitting your request.")
	public static SystemMessageId YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST;
	
	@ClientString(id = 644, message = "You are not yet registered for the castle siege.")
	public static SystemMessageId YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE;
	
	@ClientString(id = 645, message = "Only clans of level 4 or higher may register for a castle siege.")
	public static SystemMessageId ONLY_CLANS_OF_LEVEL_4_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE;
	
	@ClientString(id = 646, message = "You do not have the authority to modify the castle defender list.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST;
	
	@ClientString(id = 647, message = "You do not have the authority to modify the siege time.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME;
	
	@ClientString(id = 648, message = "No more registrations may be accepted for the attacker side.")
	public static SystemMessageId NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE;
	
	@ClientString(id = 649, message = "No more registrations may be accepted for the defender side.")
	public static SystemMessageId NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE;
	
	@ClientString(id = 650, message = "You may not summon from your current location.")
	public static SystemMessageId YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;
	
	@ClientString(id = 651, message = "Place $s1 in the current location and direction. Do you wish to continue?")
	public static SystemMessageId PLACE_S1_IN_THE_CURRENT_LOCATION_AND_DIRECTION_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 652, message = "The target of the summoned monster is wrong.")
	public static SystemMessageId THE_TARGET_OF_THE_SUMMONED_MONSTER_IS_WRONG;
	
	@ClientString(id = 653, message = "You do not have the authority to position mercenaries.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES;
	
	@ClientString(id = 654, message = "You do not have the authority to cancel mercenary positioning.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING;
	
	@ClientString(id = 655, message = "Mercenaries cannot be positioned here.")
	public static SystemMessageId MERCENARIES_CANNOT_BE_POSITIONED_HERE;
	
	@ClientString(id = 656, message = "This mercenary cannot be positioned anymore.")
	public static SystemMessageId THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE;
	
	@ClientString(id = 657, message = "Positioning cannot be done here because the distance between mercenaries is too short.")
	public static SystemMessageId POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT;
	
	@ClientString(id = 658, message = "This is not a mercenary of a castle that you own and so you cannot cancel its positioning.")
	public static SystemMessageId THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_YOU_CANNOT_CANCEL_ITS_POSITIONING;
	
	@ClientString(id = 659, message = "This is not the time for siege registration and so registrations cannot be accepted or rejected.")
	public static SystemMessageId THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED;
	
	@ClientString(id = 660, message = "This is not the time for siege registration and so registration and cancellation cannot be done.")
	public static SystemMessageId THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE;
	
	@ClientString(id = 661, message = "This character cannot be spoiled.")
	public static SystemMessageId THIS_CHARACTER_CANNOT_BE_SPOILED;
	
	@ClientString(id = 662, message = "The other player is rejecting friend invitations.")
	public static SystemMessageId THE_OTHER_PLAYER_IS_REJECTING_FRIEND_INVITATIONS;
	
	@ClientString(id = 663, message = "The siege time has been declared for $s. It is not possible to change the time after a siege time has been declared. Do you want to continue?")
	public static SystemMessageId THE_SIEGE_TIME_HAS_BEEN_DECLARED_FOR_S_IT_IS_NOT_POSSIBLE_TO_CHANGE_THE_TIME_AFTER_A_SIEGE_TIME_HAS_BEEN_DECLARED_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 664, message = "Please choose a person to receive.")
	public static SystemMessageId PLEASE_CHOOSE_A_PERSON_TO_RECEIVE;
	
	@ClientString(id = 665, message = "$s2 of $s1 alliance is applying for alliance war. Do you want to accept the challenge?")
	public static SystemMessageId S2_OF_S1_ALLIANCE_IS_APPLYING_FOR_ALLIANCE_WAR_DO_YOU_WANT_TO_ACCEPT_THE_CHALLENGE;
	
	@ClientString(id = 666, message = "A request for ceasefire has been received from $s1 alliance. Do you agree?")
	public static SystemMessageId A_REQUEST_FOR_CEASEFIRE_HAS_BEEN_RECEIVED_FROM_S1_ALLIANCE_DO_YOU_AGREE;
	
	@ClientString(id = 667, message = "You are registering on the attacking side of the $s1 siege. Do you want to continue?")
	public static SystemMessageId YOU_ARE_REGISTERING_ON_THE_ATTACKING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 668, message = "You are registering on the defending side of the $s1 siege. Do you want to continue?")
	public static SystemMessageId YOU_ARE_REGISTERING_ON_THE_DEFENDING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 669, message = "You are canceling your application to participate in the $s1 siege battle. Do you want to continue?")
	public static SystemMessageId YOU_ARE_CANCELING_YOUR_APPLICATION_TO_PARTICIPATE_IN_THE_S1_SIEGE_BATTLE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 670, message = "You are refusing the registration of $s1 clan on the defending side. Do you want to continue?")
	public static SystemMessageId YOU_ARE_REFUSING_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 671, message = "You are agreeing to the registration of $s1 clan on the defending side. Do you want to continue?")
	public static SystemMessageId YOU_ARE_AGREEING_TO_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 672, message = "$s1 adena disappeared.")
	public static SystemMessageId S1_ADENA_DISAPPEARED;
	
	@ClientString(id = 673, message = "Only a clan leader whose clan is of level 2 or higher is allowed to participate in a clan hall auction.")
	public static SystemMessageId ONLY_A_CLAN_LEADER_WHOSE_CLAN_IS_OF_LEVEL_2_OR_HIGHER_IS_ALLOWED_TO_PARTICIPATE_IN_A_CLAN_HALL_AUCTION;
	
	@ClientString(id = 674, message = "It has not yet been seven days since canceling an auction.")
	public static SystemMessageId IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION;
	
	@ClientString(id = 675, message = "There are no clan halls up for auction.")
	public static SystemMessageId THERE_ARE_NO_CLAN_HALLS_UP_FOR_AUCTION;
	
	@ClientString(id = 676, message = "Since you have already submitted a bid, you are not allowed to participate in another auction at this time.")
	public static SystemMessageId SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME;
	
	@ClientString(id = 677, message = "Your bid price must be higher than the minimum price that can be bid.")
	public static SystemMessageId YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_THAT_CAN_BE_BID;
	
	@ClientString(id = 678, message = "You have submitted a bid in the auction of $s1.")
	public static SystemMessageId YOU_HAVE_SUBMITTED_A_BID_IN_THE_AUCTION_OF_S1;
	
	@ClientString(id = 679, message = "You have canceled your bid.")
	public static SystemMessageId YOU_HAVE_CANCELED_YOUR_BID;
	
	@ClientString(id = 680, message = "You cannot participate in an auction.")
	public static SystemMessageId YOU_CANNOT_PARTICIPATE_IN_AN_AUCTION;
	
	@ClientString(id = 681, message = "The clan does not own a clan hall.")
	public static SystemMessageId THE_CLAN_DOES_NOT_OWN_A_CLAN_HALL;
	
	@ClientString(id = 682, message = "You are moving to another village. Do you want to continue?")
	public static SystemMessageId YOU_ARE_MOVING_TO_ANOTHER_VILLAGE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 683, message = "There are no priority rights on a sweeper.")
	public static SystemMessageId THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER;
	
	@ClientString(id = 684, message = "You cannot position mercenaries during a siege.")
	public static SystemMessageId YOU_CANNOT_POSITION_MERCENARIES_DURING_A_SIEGE;
	
	@ClientString(id = 685, message = "You cannot apply for clan war with a clan that belongs to the same alliance.")
	public static SystemMessageId YOU_CANNOT_APPLY_FOR_CLAN_WAR_WITH_A_CLAN_THAT_BELONGS_TO_THE_SAME_ALLIANCE;
	
	@ClientString(id = 686, message = "You have received $s1 damage from the fire of magic.")
	public static SystemMessageId YOU_HAVE_RECEIVED_S1_DAMAGE_FROM_THE_FIRE_OF_MAGIC;
	
	@ClientString(id = 687, message = "You cannot move while frozen. Please wait.")
	public static SystemMessageId YOU_CANNOT_MOVE_WHILE_FROZEN_PLEASE_WAIT;
	
	@ClientString(id = 688, message = "The clan that owns the castle is automatically registered on the defending side.")
	public static SystemMessageId THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE;
	
	@ClientString(id = 689, message = "A clan that owns a castle cannot participate in another siege.")
	public static SystemMessageId A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE;
	
	@ClientString(id = 690, message = "You cannot register on the attacking side because you are part of an alliance with the clan that owns the castle.")
	public static SystemMessageId YOU_CANNOT_REGISTER_ON_THE_ATTACKING_SIDE_BECAUSE_YOU_ARE_PART_OF_AN_ALLIANCE_WITH_THE_CLAN_THAT_OWNS_THE_CASTLE;
	
	@ClientString(id = 691, message = "$s1 clan is already a member of $s2 alliance.")
	public static SystemMessageId S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE;
	
	@ClientString(id = 692, message = "The other party is frozen. Please wait a moment.")
	public static SystemMessageId THE_OTHER_PARTY_IS_FROZEN_PLEASE_WAIT_A_MOMENT;
	
	@ClientString(id = 693, message = "The package that arrived is in another warehouse.")
	public static SystemMessageId THE_PACKAGE_THAT_ARRIVED_IS_IN_ANOTHER_WAREHOUSE;
	
	@ClientString(id = 694, message = "No packages have arrived.")
	public static SystemMessageId NO_PACKAGES_HAVE_ARRIVED;
	
	@ClientString(id = 695, message = "You cannot set the name of the pet.")
	public static SystemMessageId YOU_CANNOT_SET_THE_NAME_OF_THE_PET;
	
	@ClientString(id = 696, message = "Your account is restricted for not paying your PC room usage fees.")
	public static SystemMessageId YOUR_ACCOUNT_IS_RESTRICTED_FOR_NOT_PAYING_YOUR_PC_ROOM_USAGE_FEES;
	
	@ClientString(id = 697, message = "The item enchant value is strange.")
	public static SystemMessageId THE_ITEM_ENCHANT_VALUE_IS_STRANGE;
	
	@ClientString(id = 698, message = "The price is different than the same item on the sales list.")
	public static SystemMessageId THE_PRICE_IS_DIFFERENT_THAN_THE_SAME_ITEM_ON_THE_SALES_LIST;
	
	@ClientString(id = 699, message = "Currently not purchasing.")
	public static SystemMessageId CURRENTLY_NOT_PURCHASING;
	
	@ClientString(id = 700, message = "The purchase is complete.")
	public static SystemMessageId THE_PURCHASE_IS_COMPLETE;
	
	@ClientString(id = 701, message = "You do not have enough required items.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS;
	
	@ClientString(id = 702, message = "There are no GMs currently visible in the public list as they may be performing other functions at the moment.")
	public static SystemMessageId THERE_ARE_NO_GMS_CURRENTLY_VISIBLE_IN_THE_PUBLIC_LIST_AS_THEY_MAY_BE_PERFORMING_OTHER_FUNCTIONS_AT_THE_MOMENT;
	
	@ClientString(id = 703, message = "======<GM List>======")
	public static SystemMessageId GM_LIST;
	
	@ClientString(id = 704, message = "GM : $s1")
	public static SystemMessageId GM_S1;
	
	@ClientString(id = 705, message = "You cannot exclude yourself.")
	public static SystemMessageId YOU_CANNOT_EXCLUDE_YOURSELF;
	
	@ClientString(id = 706, message = "You can only register up to 64 names on your exclude list.")
	public static SystemMessageId YOU_CAN_ONLY_REGISTER_UP_TO_64_NAMES_ON_YOUR_EXCLUDE_LIST;
	
	@ClientString(id = 707, message = "You cannot teleport to a village that is in a siege.")
	public static SystemMessageId YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE;
	
	@ClientString(id = 708, message = "You do not have the right to use the castle warehouse.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CASTLE_WAREHOUSE;
	
	@ClientString(id = 709, message = "You do not have the right to use the clan warehouse.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE;
	
	@ClientString(id = 710, message = "Only clans of clan level 1 or higher can use a clan warehouse.")
	public static SystemMessageId ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE;
	
	@ClientString(id = 711, message = "The siege of $s1 has started.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_STARTED;
	
	@ClientString(id = 712, message = "The siege of $s1 has finished.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_FINISHED;
	
	@ClientString(id = 713, message = "$s1/$s2/$s3 $s4:$s5")
	public static SystemMessageId S1_S2_S3_S4_S5;
	
	@ClientString(id = 714, message = "A trap device has been tripped.")
	public static SystemMessageId A_TRAP_DEVICE_HAS_BEEN_TRIPPED;
	
	@ClientString(id = 715, message = "The trap device has been stopped.")
	public static SystemMessageId THE_TRAP_DEVICE_HAS_BEEN_STOPPED;
	
	@ClientString(id = 716, message = "If a base camp does not exist, resurrection is not possible.")
	public static SystemMessageId IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE;
	
	@ClientString(id = 717, message = "The guardian tower has been destroyed and resurrection is not possible.")
	public static SystemMessageId THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE;
	
	@ClientString(id = 718, message = "The castle gates cannot be opened and closed during a siege.")
	public static SystemMessageId THE_CASTLE_GATES_CANNOT_BE_OPENED_AND_CLOSED_DURING_A_SIEGE;
	
	@ClientString(id = 719, message = "You failed at mixing the item.")
	public static SystemMessageId YOU_FAILED_AT_MIXING_THE_ITEM;
	
	@ClientString(id = 720, message = "The purchase price is higher than the amount of money that you have and so you cannot open a personal store.")
	public static SystemMessageId THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE;
	
	@ClientString(id = 721, message = "You cannot create an alliance while participating in a siege.")
	public static SystemMessageId YOU_CANNOT_CREATE_AN_ALLIANCE_WHILE_PARTICIPATING_IN_A_SIEGE;
	
	@ClientString(id = 722, message = "You cannot dissolve an alliance while an affiliated clan is participating in a siege battle.")
	public static SystemMessageId YOU_CANNOT_DISSOLVE_AN_ALLIANCE_WHILE_AN_AFFILIATED_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE;
	
	@ClientString(id = 723, message = "The opposing clan is participating in a siege battle.")
	public static SystemMessageId THE_OPPOSING_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE;
	
	@ClientString(id = 724, message = "You cannot leave while participating in a siege battle.")
	public static SystemMessageId YOU_CANNOT_LEAVE_WHILE_PARTICIPATING_IN_A_SIEGE_BATTLE;
	
	@ClientString(id = 725, message = "You cannot banish a clan from an alliance while the clan is participating in a siege.")
	public static SystemMessageId YOU_CANNOT_BANISH_A_CLAN_FROM_AN_ALLIANCE_WHILE_THE_CLAN_IS_PARTICIPATING_IN_A_SIEGE;
	
	@ClientString(id = 726, message = "The frozen condition has started. Please wait a moment.")
	public static SystemMessageId THE_FROZEN_CONDITION_HAS_STARTED_PLEASE_WAIT_A_MOMENT;
	
	@ClientString(id = 727, message = "The frozen condition was removed.")
	public static SystemMessageId THE_FROZEN_CONDITION_WAS_REMOVED;
	
	@ClientString(id = 728, message = "You cannot apply for dissolution again within seven days after a previous application for dissolution.")
	public static SystemMessageId YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION;
	
	@ClientString(id = 729, message = "That item cannot be discarded.")
	public static SystemMessageId THAT_ITEM_CANNOT_BE_DISCARDED;
	
	@ClientString(id = 730, message = "- You have submitted $s1 petition(s). \\n - You may submit $s2 more petition(s) today.")
	public static SystemMessageId YOU_HAVE_SUBMITTED_S1_PETITION_S_N_YOU_MAY_SUBMIT_S2_MORE_PETITION_S_TODAY;
	
	@ClientString(id = 731, message = "A petition has been received by the GM on behalf of $s1. It is petition #$s2.")
	public static SystemMessageId A_PETITION_HAS_BEEN_RECEIVED_BY_THE_GM_ON_BEHALF_OF_S1_IT_IS_PETITION_S2;
	
	@ClientString(id = 732, message = "$s1 has received a request for a consultation with the GM.")
	public static SystemMessageId S1_HAS_RECEIVED_A_REQUEST_FOR_A_CONSULTATION_WITH_THE_GM;
	
	@ClientString(id = 733, message = "We have received $s1 petitions from you today and that is the maximum that you can submit in one day. You cannot submit any more petitions.")
	public static SystemMessageId WE_HAVE_RECEIVED_S1_PETITIONS_FROM_YOU_TODAY_AND_THAT_IS_THE_MAXIMUM_THAT_YOU_CAN_SUBMIT_IN_ONE_DAY_YOU_CANNOT_SUBMIT_ANY_MORE_PETITIONS;
	
	@ClientString(id = 734, message = "You failed at submitting a petition on behalf of someone else. $s1 already submitted a petition.")
	public static SystemMessageId YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_SOMEONE_ELSE_S1_ALREADY_SUBMITTED_A_PETITION;
	
	@ClientString(id = 735, message = "You failed at submitting a petition on behalf of $s1. The error is #$s2.")
	public static SystemMessageId YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_IS_S2;
	
	@ClientString(id = 736, message = "The petition was canceled. You may submit $s1 more petition(s) today.")
	public static SystemMessageId THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITION_S_TODAY;
	
	@ClientString(id = 737, message = "You failed at submitting a petition on behalf of $s1.")
	public static SystemMessageId YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1;
	
	@ClientString(id = 738, message = "You have not submitted a petition.")
	public static SystemMessageId YOU_HAVE_NOT_SUBMITTED_A_PETITION;
	
	@ClientString(id = 739, message = "You failed at canceling a petition on behalf of $s1. The error code is $s2.")
	public static SystemMessageId YOU_FAILED_AT_CANCELING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_CODE_IS_S2;
	
	@ClientString(id = 740, message = "$s1 participated in a petition chat at the request of the GM.")
	public static SystemMessageId S1_PARTICIPATED_IN_A_PETITION_CHAT_AT_THE_REQUEST_OF_THE_GM;
	
	@ClientString(id = 741, message = "You failed at adding $s1 to the petition chat. A petition has already been submitted.")
	public static SystemMessageId YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_A_PETITION_HAS_ALREADY_BEEN_SUBMITTED;
	
	@ClientString(id = 742, message = "You failed at adding $s1 to the petition chat. The error code is $s2.")
	public static SystemMessageId YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2;
	
	@ClientString(id = 743, message = "$s1 left the petition chat.")
	public static SystemMessageId S1_LEFT_THE_PETITION_CHAT;
	
	@ClientString(id = 744, message = "You failed at removing $s1 from the petition chat. The error code is $s2.")
	public static SystemMessageId YOU_FAILED_AT_REMOVING_S1_FROM_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2;
	
	@ClientString(id = 745, message = "You are currently not in a petition chat.")
	public static SystemMessageId YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT;
	
	@ClientString(id = 746, message = "It is not currently a petition.")
	public static SystemMessageId IT_IS_NOT_CURRENTLY_A_PETITION;
	
	@ClientString(id = 747, message = "If you need help, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId IF_YOU_NEED_HELP_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 748, message = "The distance is too far and so the casting has been stopped.")
	public static SystemMessageId THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED;
	
	@ClientString(id = 749, message = "The effect of $s1 has been removed.")
	public static SystemMessageId THE_EFFECT_OF_S1_HAS_BEEN_REMOVED;
	
	@ClientString(id = 750, message = "There are no other skills to learn.")
	public static SystemMessageId THERE_ARE_NO_OTHER_SKILLS_TO_LEARN;
	
	@ClientString(id = 751, message = "As there is a conflict in the siege relationship with a clan in the alliance, you cannot invite that clan to the alliance.")
	public static SystemMessageId AS_THERE_IS_A_CONFLICT_IN_THE_SIEGE_RELATIONSHIP_WITH_A_CLAN_IN_THE_ALLIANCE_YOU_CANNOT_INVITE_THAT_CLAN_TO_THE_ALLIANCE;
	
	@ClientString(id = 752, message = "That name cannot be used.")
	public static SystemMessageId THAT_NAME_CANNOT_BE_USED;
	
	@ClientString(id = 753, message = "You cannot position mercenaries here.")
	public static SystemMessageId YOU_CANNOT_POSITION_MERCENARIES_HERE;
	
	@ClientString(id = 754, message = "There are $s1 hours and $s2 minutes left in this week's usage time.")
	public static SystemMessageId THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THIS_WEEK_S_USAGE_TIME;
	
	@ClientString(id = 755, message = "There are $s1 minutes left in this week's usage time.")
	public static SystemMessageId THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEK_S_USAGE_TIME;
	
	@ClientString(id = 756, message = "This week's usage time has finished.")
	public static SystemMessageId THIS_WEEK_S_USAGE_TIME_HAS_FINISHED;
	
	@ClientString(id = 757, message = "There are $s1 hours and $s2 minutes left in the fixed use time.")
	public static SystemMessageId THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THE_FIXED_USE_TIME;
	
	@ClientString(id = 758, message = "There are $s1 hour(s) $s2 minute(s) left in this week's play time.")
	public static SystemMessageId THERE_ARE_S1_HOUR_S_S2_MINUTE_S_LEFT_IN_THIS_WEEK_S_PLAY_TIME;
	
	@ClientString(id = 759, message = "There are $s1 minutes left in this week's play time.")
	public static SystemMessageId THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEK_S_PLAY_TIME;
	
	@ClientString(id = 760, message = "$s1 cannot join the clan because one day has not yet passed since he/she left another clan.")
	public static SystemMessageId S1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_HE_SHE_LEFT_ANOTHER_CLAN;
	
	@ClientString(id = 761, message = "$s1 clan cannot join the alliance because one day has not yet passed since it left another alliance.")
	public static SystemMessageId S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_IT_LEFT_ANOTHER_ALLIANCE;
	
	@ClientString(id = 762, message = "$s1 rolled $s2 and $s3's eye came out.")
	public static SystemMessageId S1_ROLLED_S2_AND_S3_S_EYE_CAME_OUT;
	
	@ClientString(id = 763, message = "You failed at sending the package because you are too far from the warehouse.")
	public static SystemMessageId YOU_FAILED_AT_SENDING_THE_PACKAGE_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_WAREHOUSE;
	
	@ClientString(id = 764, message = "You have been playing for an extended period of time. Please consider taking a break.")
	public static SystemMessageId YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK;
	
	@ClientString(id = 765, message = "GameGuard is already running. Please try running it again after rebooting.")
	public static SystemMessageId GAMEGUARD_IS_ALREADY_RUNNING_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING;
	
	@ClientString(id = 766, message = "There is a GameGuard initialization error. Please try running it again after rebooting.")
	public static SystemMessageId THERE_IS_A_GAMEGUARD_INITIALIZATION_ERROR_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING;
	
	@ClientString(id = 767, message = "The GameGuard file is damaged . Please reinstall GameGuard.")
	public static SystemMessageId THE_GAMEGUARD_FILE_IS_DAMAGED_PLEASE_REINSTALL_GAMEGUARD;
	
	@ClientString(id = 768, message = "A Windows system file is damaged. Please reinstall Internet Explorer.")
	public static SystemMessageId A_WINDOWS_SYSTEM_FILE_IS_DAMAGED_PLEASE_REINSTALL_INTERNET_EXPLORER;
	
	@ClientString(id = 769, message = "A hacking tool has been discovered. Please try playing again after closing unnecessary programs.")
	public static SystemMessageId A_HACKING_TOOL_HAS_BEEN_DISCOVERED_PLEASE_TRY_PLAYING_AGAIN_AFTER_CLOSING_UNNECESSARY_PROGRAMS;
	
	@ClientString(id = 770, message = "The GameGuard update was canceled. Please check your network connection status or firewall.")
	public static SystemMessageId THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_CHECK_YOUR_NETWORK_CONNECTION_STATUS_OR_FIREWALL;
	
	@ClientString(id = 771, message = "The GameGuard update was canceled. Please try running it again after doing a virus scan or changing the settings in your PC management program.")
	public static SystemMessageId THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_DOING_A_VIRUS_SCAN_OR_CHANGING_THE_SETTINGS_IN_YOUR_PC_MANAGEMENT_PROGRAM;
	
	@ClientString(id = 772, message = "There was a problem when running GameGuard.")
	public static SystemMessageId THERE_WAS_A_PROBLEM_WHEN_RUNNING_GAMEGUARD;
	
	@ClientString(id = 773, message = "The game or GameGuard files are damaged.")
	public static SystemMessageId THE_GAME_OR_GAMEGUARD_FILES_ARE_DAMAGED;
	
	@ClientString(id = 774, message = "Play time is no longer accumulating.")
	public static SystemMessageId PLAY_TIME_IS_NO_LONGER_ACCUMULATING;
	
	@ClientString(id = 775, message = "From here on, play time will be expended.")
	public static SystemMessageId FROM_HERE_ON_PLAY_TIME_WILL_BE_EXPENDED;
	
	@ClientString(id = 776, message = "The clan hall which was put up for auction has been awarded to $s1 clan.")
	public static SystemMessageId THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN;
	
	@ClientString(id = 777, message = "The clan hall which had been put up for auction was not sold and therefore has been re-listed.")
	public static SystemMessageId THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RE_LISTED;
	
	@ClientString(id = 778, message = "You may not log out from this location.")
	public static SystemMessageId YOU_MAY_NOT_LOG_OUT_FROM_THIS_LOCATION;
	
	@ClientString(id = 779, message = "You may not restart in this location.")
	public static SystemMessageId YOU_MAY_NOT_RESTART_IN_THIS_LOCATION;
	
	@ClientString(id = 780, message = "Observation is only possible during a siege.")
	public static SystemMessageId OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE;
	
	@ClientString(id = 781, message = "Observers cannot participate.")
	public static SystemMessageId OBSERVERS_CANNOT_PARTICIPATE;
	
	@ClientString(id = 782, message = "You may not observe a siege with a pet or servitor summoned.")
	public static SystemMessageId YOU_MAY_NOT_OBSERVE_A_SIEGE_WITH_A_PET_OR_SERVITOR_SUMMONED;
	
	@ClientString(id = 783, message = "Lottery ticket sales have been temporarily suspended.")
	public static SystemMessageId LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED;
	
	@ClientString(id = 784, message = "Tickets for the current lottery are no longer available.")
	public static SystemMessageId TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE;
	
	@ClientString(id = 785, message = "The results of lottery number $s1 have not yet been published.")
	public static SystemMessageId THE_RESULTS_OF_LOTTERY_NUMBER_S1_HAVE_NOT_YET_BEEN_PUBLISHED;
	
	@ClientString(id = 786, message = "Incorrect syntax.")
	public static SystemMessageId INCORRECT_SYNTAX;
	
	@ClientString(id = 787, message = "The tryouts are finished.")
	public static SystemMessageId THE_TRYOUTS_ARE_FINISHED;
	
	@ClientString(id = 788, message = "The finals are finished.")
	public static SystemMessageId THE_FINALS_ARE_FINISHED;
	
	@ClientString(id = 789, message = "The tryouts have begun.")
	public static SystemMessageId THE_TRYOUTS_HAVE_BEGUN;
	
	@ClientString(id = 790, message = "The finals have begun.")
	public static SystemMessageId THE_FINALS_HAVE_BEGUN;
	
	@ClientString(id = 791, message = "The final match is about to begin. Line up!")
	public static SystemMessageId THE_FINAL_MATCH_IS_ABOUT_TO_BEGIN_LINE_UP;
	
	@ClientString(id = 792, message = "The siege of the clan hall is finished.")
	public static SystemMessageId THE_SIEGE_OF_THE_CLAN_HALL_IS_FINISHED;
	
	@ClientString(id = 793, message = "The siege of the clan hall has begun.")
	public static SystemMessageId THE_SIEGE_OF_THE_CLAN_HALL_HAS_BEGUN;
	
	@ClientString(id = 794, message = "You are not authorized to do that.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT;
	
	@ClientString(id = 795, message = "Only clan leaders are authorized to set rights.")
	public static SystemMessageId ONLY_CLAN_LEADERS_ARE_AUTHORIZED_TO_SET_RIGHTS;
	
	@ClientString(id = 796, message = "Your remaining observation time is $s1 minutes.")
	public static SystemMessageId YOUR_REMAINING_OBSERVATION_TIME_IS_S1_MINUTES;
	
	@ClientString(id = 797, message = "You may create up to 24 macros.")
	public static SystemMessageId YOU_MAY_CREATE_UP_TO_24_MACROS;
	
	@ClientString(id = 798, message = "Item registration is irreversible. Do you wish to continue?")
	public static SystemMessageId ITEM_REGISTRATION_IS_IRREVERSIBLE_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 799, message = "The observation time has expired.")
	public static SystemMessageId THE_OBSERVATION_TIME_HAS_EXPIRED;
	
	@ClientString(id = 800, message = "You are too late. The registration period is over.")
	public static SystemMessageId YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER;
	
	@ClientString(id = 801, message = "Registration for the clan hall siege is closed.")
	public static SystemMessageId REGISTRATION_FOR_THE_CLAN_HALL_SIEGE_IS_CLOSED;
	
	@ClientString(id = 802, message = "Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.")
	public static SystemMessageId PETITIONS_ARE_NOT_BEING_ACCEPTED_AT_THIS_TIME_YOU_MAY_SUBMIT_YOUR_PETITION_AFTER_S1_A_M_P_M;
	
	@ClientString(id = 803, message = "Enter the specifics of your petition.")
	public static SystemMessageId ENTER_THE_SPECIFICS_OF_YOUR_PETITION;
	
	@ClientString(id = 804, message = "Select a type.")
	public static SystemMessageId SELECT_A_TYPE;
	
	@ClientString(id = 805, message = "Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.")
	public static SystemMessageId PETITIONS_ARE_NOT_BEING_ACCEPTED_AT_THIS_TIME_YOU_MAY_SUBMIT_YOUR_PETITION_AFTER_S1_A_M_P_M_2;
	
	@ClientString(id = 806, message = "If you are trapped, try typing '/unstuck'.")
	public static SystemMessageId IF_YOU_ARE_TRAPPED_TRY_TYPING_UNSTUCK;
	
	@ClientString(id = 807, message = "This terrain is navigable. Prepare for transport to the nearest village.")
	public static SystemMessageId THIS_TERRAIN_IS_NAVIGABLE_PREPARE_FOR_TRANSPORT_TO_THE_NEAREST_VILLAGE;
	
	@ClientString(id = 808, message = "You are stuck. You may submit a petition by typing '/gm'.")
	public static SystemMessageId YOU_ARE_STUCK_YOU_MAY_SUBMIT_A_PETITION_BY_TYPING_GM;
	
	@ClientString(id = 809, message = "You are stuck. You will be transported to the nearest village in five minutes.")
	public static SystemMessageId YOU_ARE_STUCK_YOU_WILL_BE_TRANSPORTED_TO_THE_NEAREST_VILLAGE_IN_FIVE_MINUTES;
	
	@ClientString(id = 810, message = "Invalid macro. Refer to the Help file for instructions.")
	public static SystemMessageId INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS;
	
	@ClientString(id = 811, message = "You will be moved to ($s1). Do you wish to continue?")
	public static SystemMessageId YOU_WILL_BE_MOVED_TO_S1_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 812, message = "The secret trap has inflicted $s1 damage on you.")
	public static SystemMessageId THE_SECRET_TRAP_HAS_INFLICTED_S1_DAMAGE_ON_YOU;
	
	@ClientString(id = 813, message = "You have been poisoned by a Secret Trap.")
	public static SystemMessageId YOU_HAVE_BEEN_POISONED_BY_A_SECRET_TRAP;
	
	@ClientString(id = 814, message = "Your speed has been decreased by a Secret Trap.")
	public static SystemMessageId YOUR_SPEED_HAS_BEEN_DECREASED_BY_A_SECRET_TRAP;
	
	@ClientString(id = 815, message = "The tryouts are about to begin. Line up!")
	public static SystemMessageId THE_TRYOUTS_ARE_ABOUT_TO_BEGIN_LINE_UP;
	
	@ClientString(id = 816, message = "Tickets are now available for Monster Race $s1!")
	public static SystemMessageId TICKETS_ARE_NOW_AVAILABLE_FOR_MONSTER_RACE_S1;
	
	@ClientString(id = 817, message = "Now selling tickets for Monster Race $s1!")
	public static SystemMessageId NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1;
	
	@ClientString(id = 818, message = "Ticket sales for the Monster Race will end in $s1 minute(s).")
	public static SystemMessageId TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_END_IN_S1_MINUTE_S;
	
	@ClientString(id = 819, message = "Tickets sales are closed for Monster Race $s1. Odds are posted.")
	public static SystemMessageId TICKETS_SALES_ARE_CLOSED_FOR_MONSTER_RACE_S1_ODDS_ARE_POSTED;
	
	@ClientString(id = 820, message = "Monster Race $s2 will begin in $s1 minute(s)!")
	public static SystemMessageId MONSTER_RACE_S2_WILL_BEGIN_IN_S1_MINUTE_S;
	
	@ClientString(id = 821, message = "Monster Race $s1 will begin in 30 seconds!")
	public static SystemMessageId MONSTER_RACE_S1_WILL_BEGIN_IN_30_SECONDS;
	
	@ClientString(id = 822, message = "Monster Race $s1 is about to begin! Countdown in five seconds!")
	public static SystemMessageId MONSTER_RACE_S1_IS_ABOUT_TO_BEGIN_COUNTDOWN_IN_FIVE_SECONDS;
	
	@ClientString(id = 823, message = "The race will begin in $s1 second(s)!")
	public static SystemMessageId THE_RACE_WILL_BEGIN_IN_S1_SECOND_S;
	
	@ClientString(id = 824, message = "They're off!")
	public static SystemMessageId THEY_RE_OFF;
	
	@ClientString(id = 825, message = "Monster Race $s1 is finished!")
	public static SystemMessageId MONSTER_RACE_S1_IS_FINISHED;
	
	@ClientString(id = 826, message = "First prize goes to the player in lane $s1. Second prize goes to the player in lane $s2.")
	public static SystemMessageId FIRST_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S1_SECOND_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S2;
	
	@ClientString(id = 827, message = "You may not impose a block on a GM.")
	public static SystemMessageId YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM;
	
	@ClientString(id = 828, message = "Are you sure you wish to delete the $s1 macro?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WISH_TO_DELETE_THE_S1_MACRO;
	
	@ClientString(id = 829, message = "You cannot recommend yourself.")
	public static SystemMessageId YOU_CANNOT_RECOMMEND_YOURSELF;
	
	@ClientString(id = 830, message = "You have recommended $s1. You are authorized to make $s2 more recommendations.")
	public static SystemMessageId YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS;
	
	@ClientString(id = 831, message = "You have been recommended by $s1.")
	public static SystemMessageId YOU_HAVE_BEEN_RECOMMENDED_BY_S1;
	
	@ClientString(id = 832, message = "That character has already been recommended.")
	public static SystemMessageId THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED;
	
	@ClientString(id = 833, message = "You are not authorized to make further recommendations at this time. You will receive more recommendation credits each day at 1 p.m.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_MAKE_FURTHER_RECOMMENDATIONS_AT_THIS_TIME_YOU_WILL_RECEIVE_MORE_RECOMMENDATION_CREDITS_EACH_DAY_AT_1_P_M;
	
	@ClientString(id = 834, message = "$s1 has rolled $s2.")
	public static SystemMessageId S1_HAS_ROLLED_S2;
	
	@ClientString(id = 835, message = "You may not throw the dice at this time. Try again later.")
	public static SystemMessageId YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER;
	
	@ClientString(id = 836, message = "You have exceeded your inventory volume limit and cannot take this item.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM;
	
	@ClientString(id = 837, message = "Macro descriptions may contain up to 32 characters.")
	public static SystemMessageId MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS;
	
	@ClientString(id = 838, message = "Enter the name of the macro.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_MACRO;
	
	@ClientString(id = 839, message = "That name is already assigned to another macro.")
	public static SystemMessageId THAT_NAME_IS_ALREADY_ASSIGNED_TO_ANOTHER_MACRO;
	
	@ClientString(id = 840, message = "That recipe is already registered.")
	public static SystemMessageId THAT_RECIPE_IS_ALREADY_REGISTERED;
	
	@ClientString(id = 841, message = "No further recipes may be registered.")
	public static SystemMessageId NO_FURTHER_RECIPES_MAY_BE_REGISTERED;
	
	@ClientString(id = 842, message = "You are not authorized to register a recipe.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE;
	
	@ClientString(id = 843, message = "The siege of $s1 is finished.")
	public static SystemMessageId THE_SIEGE_OF_S1_IS_FINISHED;
	
	@ClientString(id = 844, message = "The siege to conquer $s1 has begun.")
	public static SystemMessageId THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN;
	
	@ClientString(id = 845, message = "The deadline to register for the siege of $s1 has passed.")
	public static SystemMessageId THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED;
	
	@ClientString(id = 846, message = "The siege of $s1 has been canceled due to lack of interest.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST;
	
	@ClientString(id = 847, message = "A clan that owns a clan hall may not participate in a clan hall siege.")
	public static SystemMessageId A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE;
	
	@ClientString(id = 848, message = "$s1 has been deleted.")
	public static SystemMessageId S1_HAS_BEEN_DELETED;
	
	@ClientString(id = 849, message = "$s1 cannot be found.")
	public static SystemMessageId S1_CANNOT_BE_FOUND;
	
	@ClientString(id = 850, message = "$s1 already exists.")
	public static SystemMessageId S1_ALREADY_EXISTS_2;
	
	@ClientString(id = 851, message = "$s1 has been added.")
	public static SystemMessageId S1_HAS_BEEN_ADDED;
	
	@ClientString(id = 852, message = "The recipe is incorrect.")
	public static SystemMessageId THE_RECIPE_IS_INCORRECT;
	
	@ClientString(id = 853, message = "You may not alter your recipe book while engaged in manufacturing.")
	public static SystemMessageId YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING;
	
	@ClientString(id = 854, message = "You are missing $s2 $s1 required to create that.")
	public static SystemMessageId YOU_ARE_MISSING_S2_S1_REQUIRED_TO_CREATE_THAT;
	
	@ClientString(id = 855, message = "$s1 clan has defeated $s2.")
	public static SystemMessageId S1_CLAN_HAS_DEFEATED_S2;
	
	@ClientString(id = 856, message = "The siege of $s1 has ended in a draw.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW;
	
	@ClientString(id = 857, message = "$s1 clan has won in the preliminary match of $s2.")
	public static SystemMessageId S1_CLAN_HAS_WON_IN_THE_PRELIMINARY_MATCH_OF_S2;
	
	@ClientString(id = 858, message = "The preliminary match of $s1 has ended in a draw.")
	public static SystemMessageId THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW;
	
	@ClientString(id = 859, message = "Please register a recipe.")
	public static SystemMessageId PLEASE_REGISTER_A_RECIPE;
	
	@ClientString(id = 860, message = "You may not build your headquarters in close proximity to another headquarters.")
	public static SystemMessageId YOU_MAY_NOT_BUILD_YOUR_HEADQUARTERS_IN_CLOSE_PROXIMITY_TO_ANOTHER_HEADQUARTERS;
	
	@ClientString(id = 861, message = "You have exceeded the maximum number of memos.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_MAXIMUM_NUMBER_OF_MEMOS;
	
	@ClientString(id = 862, message = "Odds are not posted until ticket sales have closed.")
	public static SystemMessageId ODDS_ARE_NOT_POSTED_UNTIL_TICKET_SALES_HAVE_CLOSED;
	
	@ClientString(id = 863, message = "You feel the energy of fire.")
	public static SystemMessageId YOU_FEEL_THE_ENERGY_OF_FIRE;
	
	@ClientString(id = 864, message = "You feel the energy of water.")
	public static SystemMessageId YOU_FEEL_THE_ENERGY_OF_WATER;
	
	@ClientString(id = 865, message = "You feel the energy of wind.")
	public static SystemMessageId YOU_FEEL_THE_ENERGY_OF_WIND;
	
	@ClientString(id = 866, message = "You may no longer gather energy.")
	public static SystemMessageId YOU_MAY_NO_LONGER_GATHER_ENERGY;
	
	@ClientString(id = 867, message = "The energy is depleted.")
	public static SystemMessageId THE_ENERGY_IS_DEPLETED;
	
	@ClientString(id = 868, message = "The energy of fire has been delivered.")
	public static SystemMessageId THE_ENERGY_OF_FIRE_HAS_BEEN_DELIVERED;
	
	@ClientString(id = 869, message = "The energy of water has been delivered.")
	public static SystemMessageId THE_ENERGY_OF_WATER_HAS_BEEN_DELIVERED;
	
	@ClientString(id = 870, message = "The energy of wind has been delivered.")
	public static SystemMessageId THE_ENERGY_OF_WIND_HAS_BEEN_DELIVERED;
	
	@ClientString(id = 871, message = "The seed has been sown.")
	public static SystemMessageId THE_SEED_HAS_BEEN_SOWN;
	
	@ClientString(id = 872, message = "This seed may not be sown here.")
	public static SystemMessageId THIS_SEED_MAY_NOT_BE_SOWN_HERE;
	
	@ClientString(id = 873, message = "That character does not exist.")
	public static SystemMessageId THAT_CHARACTER_DOES_NOT_EXIST;
	
	@ClientString(id = 874, message = "The capacity of the warehouse has been exceeded.")
	public static SystemMessageId THE_CAPACITY_OF_THE_WAREHOUSE_HAS_BEEN_EXCEEDED;
	
	@ClientString(id = 875, message = "The transport of the cargo has been canceled.")
	public static SystemMessageId THE_TRANSPORT_OF_THE_CARGO_HAS_BEEN_CANCELED;
	
	@ClientString(id = 876, message = "The cargo was not delivered.")
	public static SystemMessageId THE_CARGO_WAS_NOT_DELIVERED;
	
	@ClientString(id = 877, message = "The symbol has been added.")
	public static SystemMessageId THE_SYMBOL_HAS_BEEN_ADDED;
	
	@ClientString(id = 878, message = "The symbol has been deleted.")
	public static SystemMessageId THE_SYMBOL_HAS_BEEN_DELETED;
	
	@ClientString(id = 879, message = "The manor system is currently under maintenance.")
	public static SystemMessageId THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE;
	
	@ClientString(id = 880, message = "The transaction is complete.")
	public static SystemMessageId THE_TRANSACTION_IS_COMPLETE;
	
	@ClientString(id = 881, message = "There is a discrepancy on the invoice.")
	public static SystemMessageId THERE_IS_A_DISCREPANCY_ON_THE_INVOICE;
	
	@ClientString(id = 882, message = "The seed quantity is incorrect.")
	public static SystemMessageId THE_SEED_QUANTITY_IS_INCORRECT;
	
	@ClientString(id = 883, message = "The seed information is incorrect.")
	public static SystemMessageId THE_SEED_INFORMATION_IS_INCORRECT;
	
	@ClientString(id = 884, message = "The manor information has been updated.")
	public static SystemMessageId THE_MANOR_INFORMATION_HAS_BEEN_UPDATED;
	
	@ClientString(id = 885, message = "The number of crops is incorrect.")
	public static SystemMessageId THE_NUMBER_OF_CROPS_IS_INCORRECT;
	
	@ClientString(id = 886, message = "The crops are priced incorrectly.")
	public static SystemMessageId THE_CROPS_ARE_PRICED_INCORRECTLY;
	
	@ClientString(id = 887, message = "The type is incorrect.")
	public static SystemMessageId THE_TYPE_IS_INCORRECT;
	
	@ClientString(id = 888, message = "No crops can be purchased at this time.")
	public static SystemMessageId NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME;
	
	@ClientString(id = 889, message = "The seed was successfully sown.")
	public static SystemMessageId THE_SEED_WAS_SUCCESSFULLY_SOWN;
	
	@ClientString(id = 890, message = "The seed was not sown.")
	public static SystemMessageId THE_SEED_WAS_NOT_SOWN;
	
	@ClientString(id = 891, message = "You are not authorized to harvest.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_HARVEST;
	
	@ClientString(id = 892, message = "The harvest has failed.")
	public static SystemMessageId THE_HARVEST_HAS_FAILED;
	
	@ClientString(id = 893, message = "The harvest failed because the seed was not sown.")
	public static SystemMessageId THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN;
	
	@ClientString(id = 894, message = "Up to $s1 recipes can be registered.")
	public static SystemMessageId UP_TO_S1_RECIPES_CAN_BE_REGISTERED;
	
	@ClientString(id = 895, message = "No recipes have been registered.")
	public static SystemMessageId NO_RECIPES_HAVE_BEEN_REGISTERED;
	
	@ClientString(id = 896, message = "Quest recipes can not be registered.")
	public static SystemMessageId QUEST_RECIPES_CAN_NOT_BE_REGISTERED;
	
	@ClientString(id = 897, message = "The fee to create the item is incorrect.")
	public static SystemMessageId THE_FEE_TO_CREATE_THE_ITEM_IS_INCORRECT;
	
	@ClientString(id = 898, message = "Only characters of level 10 or above are authorized to make recommendations.")
	public static SystemMessageId ONLY_CHARACTERS_OF_LEVEL_10_OR_ABOVE_ARE_AUTHORIZED_TO_MAKE_RECOMMENDATIONS;
	
	@ClientString(id = 899, message = "The symbol cannot be drawn.")
	public static SystemMessageId THE_SYMBOL_CANNOT_BE_DRAWN;
	
	@ClientString(id = 900, message = "No slot exists to draw the symbol.")
	public static SystemMessageId NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL;
	
	@ClientString(id = 901, message = "The symbol information cannot be found.")
	public static SystemMessageId THE_SYMBOL_INFORMATION_CANNOT_BE_FOUND;
	
	@ClientString(id = 902, message = "The number of items is incorrect.")
	public static SystemMessageId THE_NUMBER_OF_ITEMS_IS_INCORRECT;
	
	@ClientString(id = 903, message = "You may not submit a petition while frozen. Be patient.")
	public static SystemMessageId YOU_MAY_NOT_SUBMIT_A_PETITION_WHILE_FROZEN_BE_PATIENT;
	
	@ClientString(id = 904, message = "Items cannot be discarded while in private store status.")
	public static SystemMessageId ITEMS_CANNOT_BE_DISCARDED_WHILE_IN_PRIVATE_STORE_STATUS;
	
	@ClientString(id = 905, message = "The current score for the Humans is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_HUMANS_IS_S1;
	
	@ClientString(id = 906, message = "The current score for the Elves is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_ELVES_IS_S1;
	
	@ClientString(id = 907, message = "The current score for the Dark Elves is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_DARK_ELVES_IS_S1;
	
	@ClientString(id = 908, message = "The current score for the Orcs is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_ORCS_IS_S1;
	
	@ClientString(id = 909, message = "The current score for the Dwarves is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_DWARVES_IS_S1;
	
	@ClientString(id = 910, message = "Current location : $s1, $s2, $s3 (Near Talking Island Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_TALKING_ISLAND_VILLAGE;
	
	@ClientString(id = 911, message = "Current location : $s1, $s2, $s3 (Near Gludin Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_GLUDIN_VILLAGE;
	
	@ClientString(id = 912, message = "Current location : $s1, $s2, $s3 (Near the Town of Gludio)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GLUDIO;
	
	@ClientString(id = 913, message = "Current location : $s1, $s2, $s3 (Near the Neutral Zone)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_NEUTRAL_ZONE;
	
	@ClientString(id = 914, message = "Current location : $s1, $s2, $s3 (Near the Elven Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_ELVEN_VILLAGE;
	
	@ClientString(id = 915, message = "Current location : $s1, $s2, $s3 (Near the Dark Elf Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_DARK_ELF_VILLAGE;
	
	@ClientString(id = 916, message = "Current location : $s1, $s2, $s3 (Near the Town of Dion)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_DION;
	
	@ClientString(id = 917, message = "Current location : $s1, $s2, $s3 (Near the Floran Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_FLORAN_VILLAGE;
	
	@ClientString(id = 918, message = "Current location : $s1, $s2, $s3 (Near the Town of Giran)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GIRAN;
	
	@ClientString(id = 919, message = "Current location : $s1, $s2, $s3 (Near Giran Harbor)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_GIRAN_HARBOR;
	
	@ClientString(id = 920, message = "Current location : $s1, $s2, $s3 (Near the Orc Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_ORC_VILLAGE;
	
	@ClientString(id = 921, message = "Current location : $s1, $s2, $s3 (Near the Dwarven Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_DWARVEN_VILLAGE;
	
	@ClientString(id = 922, message = "Current location : $s1, $s2, $s3 (Near the Town of Oren)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_OREN;
	
	@ClientString(id = 923, message = "Current location : $s1, $s2, $s3 (Near Hunters Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_HUNTERS_VILLAGE;
	
	@ClientString(id = 924, message = "Current location : $s1, $s2, $s3 (Near Aden Castle Town)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_ADEN_CASTLE_TOWN;
	
	@ClientString(id = 925, message = "Current location : $s1, $s2, $s3 (Near the Coliseum)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_COLISEUM;
	
	@ClientString(id = 926, message = "Current location : $s1, $s2, $s3 (Near Heine)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_HEINE;
	
	@ClientString(id = 927, message = "The current time is $s1:$s2 in the day.")
	public static SystemMessageId THE_CURRENT_TIME_IS_S1_S2_IN_THE_DAY;
	
	@ClientString(id = 928, message = "The current time is $s1:$s2 in the night.")
	public static SystemMessageId THE_CURRENT_TIME_IS_S1_S2_IN_THE_NIGHT;
	
	@ClientString(id = 929, message = "No compensation was given for the farm products.")
	public static SystemMessageId NO_COMPENSATION_WAS_GIVEN_FOR_THE_FARM_PRODUCTS;
	
	@ClientString(id = 930, message = "Lottery tickets are not currently being sold.")
	public static SystemMessageId LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD;
	
	@ClientString(id = 931, message = "The winning lottery ticket number has not yet been announced.")
	public static SystemMessageId THE_WINNING_LOTTERY_TICKET_NUMBER_HAS_NOT_YET_BEEN_ANNOUNCED;
	
	@ClientString(id = 932, message = "You cannot chat locally while observing.")
	public static SystemMessageId YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING;
	
	@ClientString(id = 933, message = "The seed pricing greatly differs from standard seed prices.")
	public static SystemMessageId THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES;
	
	@ClientString(id = 934, message = "It is a deleted recipe.")
	public static SystemMessageId IT_IS_A_DELETED_RECIPE;
	
	@ClientString(id = 935, message = "The amount is not sufficient and so the manor is not in operation.")
	public static SystemMessageId THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION;
	
	@ClientString(id = 936, message = "Use $s1.")
	public static SystemMessageId USE_S1;
	
	@ClientString(id = 937, message = "Currently preparing for private workshop.")
	public static SystemMessageId CURRENTLY_PREPARING_FOR_PRIVATE_WORKSHOP;
	
	@ClientString(id = 938, message = "The community server is currently offline.")
	public static SystemMessageId THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE;
	
	@ClientString(id = 939, message = "You cannot exchange while blocking everything.")
	public static SystemMessageId YOU_CANNOT_EXCHANGE_WHILE_BLOCKING_EVERYTHING;
	
	@ClientString(id = 940, message = "$s1 is blocking everything.")
	public static SystemMessageId S1_IS_BLOCKING_EVERYTHING;
	
	@ClientString(id = 941, message = "Restart at Talking Island Village.")
	public static SystemMessageId RESTART_AT_TALKING_ISLAND_VILLAGE;
	
	@ClientString(id = 942, message = "Restart at Gludin Village.")
	public static SystemMessageId RESTART_AT_GLUDIN_VILLAGE;
	
	@ClientString(id = 943, message = "Restart at the Town of Gludin.")
	public static SystemMessageId RESTART_AT_THE_TOWN_OF_GLUDIN;
	
	@ClientString(id = 944, message = "Restart at the Neutral Zone.")
	public static SystemMessageId RESTART_AT_THE_NEUTRAL_ZONE;
	
	@ClientString(id = 945, message = "Restart at the Elven Village.")
	public static SystemMessageId RESTART_AT_THE_ELVEN_VILLAGE;
	
	@ClientString(id = 946, message = "Restart at the Dark Elf Village.")
	public static SystemMessageId RESTART_AT_THE_DARK_ELF_VILLAGE;
	
	@ClientString(id = 947, message = "Restart at the Town of Dion.")
	public static SystemMessageId RESTART_AT_THE_TOWN_OF_DION;
	
	@ClientString(id = 948, message = "Restart at Floran Village.")
	public static SystemMessageId RESTART_AT_FLORAN_VILLAGE;
	
	@ClientString(id = 949, message = "Restart at the Town of Giran.")
	public static SystemMessageId RESTART_AT_THE_TOWN_OF_GIRAN;
	
	@ClientString(id = 950, message = "Restart at Giran Harbor.")
	public static SystemMessageId RESTART_AT_GIRAN_HARBOR;
	
	@ClientString(id = 951, message = "Restart at the Orc Village.")
	public static SystemMessageId RESTART_AT_THE_ORC_VILLAGE;
	
	@ClientString(id = 952, message = "Restart at the Dwarven Village.")
	public static SystemMessageId RESTART_AT_THE_DWARVEN_VILLAGE;
	
	@ClientString(id = 953, message = "Restart at the Town of Oren.")
	public static SystemMessageId RESTART_AT_THE_TOWN_OF_OREN;
	
	@ClientString(id = 954, message = "Restart at Hunters Village.")
	public static SystemMessageId RESTART_AT_HUNTERS_VILLAGE;
	
	@ClientString(id = 955, message = "Restart at the Town of Aden.")
	public static SystemMessageId RESTART_AT_THE_TOWN_OF_ADEN;
	
	@ClientString(id = 956, message = "Restart at the Coliseum.")
	public static SystemMessageId RESTART_AT_THE_COLISEUM;
	
	@ClientString(id = 957, message = "Restart at Heine.")
	public static SystemMessageId RESTART_AT_HEINE;
	
	@ClientString(id = 958, message = "Items cannot be discarded or destroyed while operating a private store or workshop.")
	public static SystemMessageId ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP;
	
	@ClientString(id = 959, message = "$s1 (*$s2) manufactured successfully.")
	public static SystemMessageId S1_S2_MANUFACTURED_SUCCESSFULLY;
	
	@ClientString(id = 960, message = "$s1 manufacturing failure.")
	public static SystemMessageId S1_MANUFACTURING_FAILURE;
	
	@ClientString(id = 961, message = "You are now blocking everything.")
	public static SystemMessageId YOU_ARE_NOW_BLOCKING_EVERYTHING;
	
	@ClientString(id = 962, message = "You are no longer blocking everything.")
	public static SystemMessageId YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING;
	
	@ClientString(id = 963, message = "Please determine the manufacturing price.")
	public static SystemMessageId PLEASE_DETERMINE_THE_MANUFACTURING_PRICE;
	
	@ClientString(id = 964, message = "Chatting is prohibited for one minute.")
	public static SystemMessageId CHATTING_IS_PROHIBITED_FOR_ONE_MINUTE;
	
	@ClientString(id = 965, message = "The chatting prohibition has been removed.")
	public static SystemMessageId THE_CHATTING_PROHIBITION_HAS_BEEN_REMOVED;
	
	@ClientString(id = 966, message = "Chatting is currently prohibited. If you try to chat before the prohibition is removed, the prohibition time will become even longer.")
	public static SystemMessageId CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_BECOME_EVEN_LONGER;
	
	@ClientString(id = 967, message = "Do you accept the party invitation from $s1? (Item distribution: Random including spoil)")
	public static SystemMessageId DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_RANDOM_INCLUDING_SPOIL;
	
	@ClientString(id = 968, message = "Do you accept the party invitation from $s1? (Item distribution: By turn)")
	public static SystemMessageId DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN;
	
	@ClientString(id = 969, message = "Do you accept the party invitation from $s1? (Item distribution: By turn including spoil)")
	public static SystemMessageId DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN_INCLUDING_SPOIL;
	
	@ClientString(id = 970, message = "$s2's MP has been drained by $s1.")
	public static SystemMessageId S2_S_MP_HAS_BEEN_DRAINED_BY_S1;
	
	@ClientString(id = 971, message = "Petitions cannot exceed 255 characters.")
	public static SystemMessageId PETITIONS_CANNOT_EXCEED_255_CHARACTERS;
	
	@ClientString(id = 972, message = "This pet cannot use this item.")
	public static SystemMessageId THIS_PET_CANNOT_USE_THIS_ITEM;
	
	@ClientString(id = 973, message = "Please input no more than the number you have.")
	public static SystemMessageId PLEASE_INPUT_NO_MORE_THAN_THE_NUMBER_YOU_HAVE;
	
	@ClientString(id = 974, message = "The soul crystal succeeded in absorbing a soul.")
	public static SystemMessageId THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL;
	
	@ClientString(id = 975, message = "The soul crystal was not able to absorb a soul.")
	public static SystemMessageId THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_A_SOUL;
	
	@ClientString(id = 976, message = "The soul crystal broke because it was not able to endure the soul energy.")
	public static SystemMessageId THE_SOUL_CRYSTAL_BROKE_BECAUSE_IT_WAS_NOT_ABLE_TO_ENDURE_THE_SOUL_ENERGY;
	
	@ClientString(id = 977, message = "The soul crystals caused resonation and failed at absorbing a soul.")
	public static SystemMessageId THE_SOUL_CRYSTALS_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL;
	
	@ClientString(id = 978, message = "The soul crystal is refusing to absorb a soul.")
	public static SystemMessageId THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_A_SOUL;
	
	@ClientString(id = 979, message = "The ferry arrived at Talking Island Harbor.")
	public static SystemMessageId THE_FERRY_ARRIVED_AT_TALKING_ISLAND_HARBOR;
	
	@ClientString(id = 980, message = "The ferry will leave for Gludin Harbor after anchoring for ten minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 981, message = "The ferry will leave for Gludin Harbor in five minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_FIVE_MINUTES;
	
	@ClientString(id = 982, message = "The ferry will leave for Gludin Harbor in one minute.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_ONE_MINUTE;
	
	@ClientString(id = 983, message = "Those wishing to ride should make haste to get on.")
	public static SystemMessageId THOSE_WISHING_TO_RIDE_SHOULD_MAKE_HASTE_TO_GET_ON;
	
	@ClientString(id = 984, message = "The ferry will be leaving soon for Gludin Harbor.")
	public static SystemMessageId THE_FERRY_WILL_BE_LEAVING_SOON_FOR_GLUDIN_HARBOR;
	
	@ClientString(id = 985, message = "The ferry is leaving for Gludin Harbor.")
	public static SystemMessageId THE_FERRY_IS_LEAVING_FOR_GLUDIN_HARBOR;
	
	@ClientString(id = 986, message = "The ferry has arrived at Gludin Harbor.")
	public static SystemMessageId THE_FERRY_HAS_ARRIVED_AT_GLUDIN_HARBOR;
	
	@ClientString(id = 987, message = "The ferry will leave for Talking Island Harbor after anchoring for ten minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 988, message = "The ferry will leave for Talking Island Harbor in five minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_FIVE_MINUTES;
	
	@ClientString(id = 989, message = "The ferry will leave for Talking Island Harbor in one minute.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_ONE_MINUTE;
	
	@ClientString(id = 990, message = "The ferry will be leaving soon for Talking Island Harbor.")
	public static SystemMessageId THE_FERRY_WILL_BE_LEAVING_SOON_FOR_TALKING_ISLAND_HARBOR;
	
	@ClientString(id = 991, message = "The ferry is leaving for Talking Island Harbor.")
	public static SystemMessageId THE_FERRY_IS_LEAVING_FOR_TALKING_ISLAND_HARBOR;
	
	@ClientString(id = 992, message = "The ferry has arrived at Giran Harbor.")
	public static SystemMessageId THE_FERRY_HAS_ARRIVED_AT_GIRAN_HARBOR;
	
	@ClientString(id = 993, message = "The ferry will leave for Giran Harbor after anchoring for ten minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 994, message = "The ferry will leave for Giran Harbor in five minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_IN_FIVE_MINUTES;
	
	@ClientString(id = 995, message = "The ferry will leave for Giran Harbor in one minute.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_IN_ONE_MINUTE;
	
	@ClientString(id = 996, message = "The ferry will be leaving soon for Giran Harbor.")
	public static SystemMessageId THE_FERRY_WILL_BE_LEAVING_SOON_FOR_GIRAN_HARBOR;
	
	@ClientString(id = 997, message = "The ferry is leaving for Giran Harbor.")
	public static SystemMessageId THE_FERRY_IS_LEAVING_FOR_GIRAN_HARBOR;
	
	@ClientString(id = 998, message = "The Innadril pleasure boat has arrived. It will anchor for ten minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_HAS_ARRIVED_IT_WILL_ANCHOR_FOR_TEN_MINUTES;
	
	@ClientString(id = 999, message = "The Innadril pleasure boat will leave in five minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_FIVE_MINUTES;
	
	@ClientString(id = 1000, message = "The Innadril pleasure boat will leave in one minute.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_ONE_MINUTE;
	
	@ClientString(id = 1001, message = "The Innadril pleasure boat will be leaving soon.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_BE_LEAVING_SOON;
	
	@ClientString(id = 1002, message = "The Innadril pleasure boat is leaving.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_IS_LEAVING;
	
	@ClientString(id = 1003, message = "Cannot process a monster race ticket.")
	public static SystemMessageId CANNOT_PROCESS_A_MONSTER_RACE_TICKET;
	
	@ClientString(id = 1004, message = "You have registered for a clan hall auction.")
	public static SystemMessageId YOU_HAVE_REGISTERED_FOR_A_CLAN_HALL_AUCTION;
	
	@ClientString(id = 1005, message = "There is not enough adena in the clan hall warehouse.")
	public static SystemMessageId THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE;
	
	@ClientString(id = 1006, message = "You have bid in a clan hall auction.")
	public static SystemMessageId YOU_HAVE_BID_IN_A_CLAN_HALL_AUCTION;
	
	@ClientString(id = 1007, message = "The preliminary match registration of $s1 has finished.")
	public static SystemMessageId THE_PRELIMINARY_MATCH_REGISTRATION_OF_S1_HAS_FINISHED;
	
	@ClientString(id = 1008, message = "A hungry strider cannot be mounted or dismounted.")
	public static SystemMessageId A_HUNGRY_STRIDER_CANNOT_BE_MOUNTED_OR_DISMOUNTED;
	
	@ClientString(id = 1009, message = "A strider cannot be ridden when dead.")
	public static SystemMessageId A_STRIDER_CANNOT_BE_RIDDEN_WHEN_DEAD;
	
	@ClientString(id = 1010, message = "A dead strider cannot be ridden.")
	public static SystemMessageId A_DEAD_STRIDER_CANNOT_BE_RIDDEN;
	
	@ClientString(id = 1011, message = "A strider in battle cannot be ridden.")
	public static SystemMessageId A_STRIDER_IN_BATTLE_CANNOT_BE_RIDDEN;
	
	@ClientString(id = 1012, message = "A strider cannot be ridden while in battle.")
	public static SystemMessageId A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE;
	
	@ClientString(id = 1013, message = "A strider can be ridden only when standing.")
	public static SystemMessageId A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING;
	
	@ClientString(id = 1014, message = "Your pet gained $s1 experience points.")
	public static SystemMessageId YOUR_PET_GAINED_S1_EXPERIENCE_POINTS;
	
	@ClientString(id = 1015, message = "Your pet hit for $s1 damage.")
	public static SystemMessageId YOUR_PET_HIT_FOR_S1_DAMAGE;
	
	@ClientString(id = 1016, message = "Your pet received $s2 damage caused by $s1.")
	public static SystemMessageId YOUR_PET_RECEIVED_S2_DAMAGE_CAUSED_BY_S1;
	
	@ClientString(id = 1017, message = "Pet's critical hit!")
	public static SystemMessageId PET_S_CRITICAL_HIT;
	
	@ClientString(id = 1018, message = "Your pet uses $s1.")
	public static SystemMessageId YOUR_PET_USES_S1;
	
	@ClientString(id = 1019, message = "Your pet uses $s1.")
	public static SystemMessageId YOUR_PET_USES_S1_2;
	
	@ClientString(id = 1020, message = "Your pet picked up $s1.")
	public static SystemMessageId YOUR_PET_PICKED_UP_S1;
	
	@ClientString(id = 1021, message = "Your pet picked up $s2 $s1(s).")
	public static SystemMessageId YOUR_PET_PICKED_UP_S2_S1_S;
	
	@ClientString(id = 1022, message = "Your pet picked up +$s1 $s2.")
	public static SystemMessageId YOUR_PET_PICKED_UP_S1_S2;
	
	@ClientString(id = 1023, message = "Your pet picked up $s1 adena.")
	public static SystemMessageId YOUR_PET_PICKED_UP_S1_ADENA;
	
	@ClientString(id = 1024, message = "Your pet put on $s1.")
	public static SystemMessageId YOUR_PET_PUT_ON_S1;
	
	@ClientString(id = 1025, message = "Your pet took off $s1.")
	public static SystemMessageId YOUR_PET_TOOK_OFF_S1;
	
	@ClientString(id = 1026, message = "The summoned monster gave damage of $s1.")
	public static SystemMessageId THE_SUMMONED_MONSTER_GAVE_DAMAGE_OF_S1;
	
	@ClientString(id = 1027, message = "The summoned monster received damage of $s2 caused by $s1.")
	public static SystemMessageId THE_SUMMONED_MONSTER_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1;
	
	@ClientString(id = 1028, message = "Summoned monster's critical hit!")
	public static SystemMessageId SUMMONED_MONSTER_S_CRITICAL_HIT;
	
	@ClientString(id = 1029, message = "A summoned monster uses $s1.")
	public static SystemMessageId A_SUMMONED_MONSTER_USES_S1;
	
	@ClientString(id = 1030, message = "<Party Information>")
	public static SystemMessageId PARTY_INFORMATION;
	
	@ClientString(id = 1031, message = "Looting method: Finders keepers")
	public static SystemMessageId LOOTING_METHOD_FINDERS_KEEPERS;
	
	@ClientString(id = 1032, message = "Looting method: Random")
	public static SystemMessageId LOOTING_METHOD_RANDOM;
	
	@ClientString(id = 1033, message = "Looting method: Random including spoil")
	public static SystemMessageId LOOTING_METHOD_RANDOM_INCLUDING_SPOIL;
	
	@ClientString(id = 1034, message = "Looting method: By turn")
	public static SystemMessageId LOOTING_METHOD_BY_TURN;
	
	@ClientString(id = 1035, message = "Looting method: By turn including spoil")
	public static SystemMessageId LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL;
	
	@ClientString(id = 1036, message = "You have exceeded the quantity that can be inputted.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED;
	
	@ClientString(id = 1037, message = "$s1 manufactured $s2.")
	public static SystemMessageId S1_MANUFACTURED_S2;
	
	@ClientString(id = 1038, message = "$s1 manufactured $s3 $s2(s).")
	public static SystemMessageId S1_MANUFACTURED_S3_S2_S;
	
	@ClientString(id = 1039, message = "Items left at the clan hall warehouse can only be retrieved by the clan leader. Do you want to continue?")
	public static SystemMessageId ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 1040, message = "Packages sent by freight can be picked up from any Warehouse location. Would you like to continue?")
	public static SystemMessageId PACKAGES_SENT_BY_FREIGHT_CAN_BE_PICKED_UP_FROM_ANY_WAREHOUSE_LOCATION_WOULD_YOU_LIKE_TO_CONTINUE;
	
	@ClientString(id = 1041, message = "The next seed purchase price is $s1 adena.")
	public static SystemMessageId THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA;
	
	@ClientString(id = 1042, message = "The next farm goods purchase price is $s1 adena.")
	public static SystemMessageId THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA;
	
	@ClientString(id = 1043, message = "At the current time, the '/unstuck' command cannot be used. Please send in a petition.")
	public static SystemMessageId AT_THE_CURRENT_TIME_THE_UNSTUCK_COMMAND_CANNOT_BE_USED_PLEASE_SEND_IN_A_PETITION;
	
	@ClientString(id = 1044, message = "Monster race payout information is not available while tickets are being sold.")
	public static SystemMessageId MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD;
	
	@ClientString(id = 1045, message = "Currently, a monster race is not being set up.")
	public static SystemMessageId CURRENTLY_A_MONSTER_RACE_IS_NOT_BEING_SET_UP;
	
	@ClientString(id = 1046, message = "Monster race tickets are no longer available.")
	public static SystemMessageId MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE;
	
	@ClientString(id = 1047, message = "We did not succeed in producing $s1 item.")
	public static SystemMessageId WE_DID_NOT_SUCCEED_IN_PRODUCING_S1_ITEM;
	
	@ClientString(id = 1048, message = "While 'blocking' everything, whispering is not possible.")
	public static SystemMessageId WHILE_BLOCKING_EVERYTHING_WHISPERING_IS_NOT_POSSIBLE;
	
	@ClientString(id = 1049, message = "While 'blocking' everything, it is not possible to send invitations for organizing parties.")
	public static SystemMessageId WHILE_BLOCKING_EVERYTHING_IT_IS_NOT_POSSIBLE_TO_SEND_INVITATIONS_FOR_ORGANIZING_PARTIES;
	
	@ClientString(id = 1050, message = "There are no communities in my clan. Clan communities are allowed for clans with skill levels of 2 and higher.")
	public static SystemMessageId THERE_ARE_NO_COMMUNITIES_IN_MY_CLAN_CLAN_COMMUNITIES_ARE_ALLOWED_FOR_CLANS_WITH_SKILL_LEVELS_OF_2_AND_HIGHER;
	
	@ClientString(id = 1051, message = "Payment for your clan hall has not been made. Please make payment to your clan warehouse by $s1 tomorrow.")
	public static SystemMessageId PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW;
	
	@ClientString(id = 1052, message = "The clan hall fee is one week overdue; therefore the clan hall ownership has been revoked.")
	public static SystemMessageId THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED;
	
	@ClientString(id = 1053, message = "It is not possible to resurrect in battlefields where a siege war is taking place.")
	public static SystemMessageId IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE;
	
	@ClientString(id = 1054, message = "You have entered a mystical land.")
	public static SystemMessageId YOU_HAVE_ENTERED_A_MYSTICAL_LAND;
	
	@ClientString(id = 1055, message = "You have left a mystical land.")
	public static SystemMessageId YOU_HAVE_LEFT_A_MYSTICAL_LAND;
	
	@ClientString(id = 1056, message = "You have exceeded the storage capacity of the castle's vault.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_STORAGE_CAPACITY_OF_THE_CASTLE_S_VAULT;
	
	@ClientString(id = 1057, message = "This command can only be used in the relax server.")
	public static SystemMessageId THIS_COMMAND_CAN_ONLY_BE_USED_IN_THE_RELAX_SERVER;
	
	@ClientString(id = 1058, message = "The sales price for seeds is $s1 adena.")
	public static SystemMessageId THE_SALES_PRICE_FOR_SEEDS_IS_S1_ADENA;
	
	@ClientString(id = 1059, message = "The remaining purchasing amount is $s1 adena.")
	public static SystemMessageId THE_REMAINING_PURCHASING_AMOUNT_IS_S1_ADENA;
	
	@ClientString(id = 1060, message = "The remainder after selling the seeds is $s1.")
	public static SystemMessageId THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1;
	
	@ClientString(id = 1061, message = "The recipe cannot be registered. You do not have the ability to create items.")
	public static SystemMessageId THE_RECIPE_CANNOT_BE_REGISTERED_YOU_DO_NOT_HAVE_THE_ABILITY_TO_CREATE_ITEMS;
	
	@ClientString(id = 1062, message = "Writing something new is possible after level 10.")
	public static SystemMessageId WRITING_SOMETHING_NEW_IS_POSSIBLE_AFTER_LEVEL_10;
	
	@ClientString(id = 1063, message = "The Petition Service is currently unavailable, please try again later; in the interim, if you become trapped or unable to move, please use the '/unstuck' command.")
	public static SystemMessageId THE_PETITION_SERVICE_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER_IN_THE_INTERIM_IF_YOU_BECOME_TRAPPED_OR_UNABLE_TO_MOVE_PLEASE_USE_THE_UNSTUCK_COMMAND;
	
	@ClientString(id = 1064, message = "The equipment, +$s1 $s2, has been removed.")
	public static SystemMessageId THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED;
	
	@ClientString(id = 1065, message = "While operating a private store or workshop, you cannot discard, destroy, or trade an item.")
	public static SystemMessageId WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM;
	
	@ClientString(id = 1066, message = "$s1 HP has been restored.")
	public static SystemMessageId S1_HP_HAS_BEEN_RESTORED;
	
	@ClientString(id = 1067, message = "$s2 HP has been restored by $s1.")
	public static SystemMessageId S2_HP_HAS_BEEN_RESTORED_BY_S1;
	
	@ClientString(id = 1068, message = "$s1 MP has been restored.")
	public static SystemMessageId S1_MP_HAS_BEEN_RESTORED;
	
	@ClientString(id = 1069, message = "$s2 MP has been restored by $s1.")
	public static SystemMessageId S2_MP_HAS_BEEN_RESTORED_BY_S1;
	
	@ClientString(id = 1070, message = "You do not have 'read' permission.")
	public static SystemMessageId YOU_DO_NOT_HAVE_READ_PERMISSION;
	
	@ClientString(id = 1071, message = "You do not have 'write' permission.")
	public static SystemMessageId YOU_DO_NOT_HAVE_WRITE_PERMISSION;
	
	@ClientString(id = 1072, message = "You have obtained a ticket for the Monster Race #$s1 - Single.")
	public static SystemMessageId YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1_SINGLE;
	
	@ClientString(id = 1073, message = "You have obtained a ticket for the Monster Race #$s1 - Single.")
	public static SystemMessageId YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1_SINGLE_2;
	
	@ClientString(id = 1074, message = "You do not meet the age requirement to purchase a Monster Race Ticket.")
	public static SystemMessageId YOU_DO_NOT_MEET_THE_AGE_REQUIREMENT_TO_PURCHASE_A_MONSTER_RACE_TICKET;
	
	@ClientString(id = 1075, message = "The bid amount must be higher than the previous bid.")
	public static SystemMessageId THE_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_PREVIOUS_BID;
	
	@ClientString(id = 1076, message = "The game cannot be terminated at this time.")
	public static SystemMessageId THE_GAME_CANNOT_BE_TERMINATED_AT_THIS_TIME;
	
	@ClientString(id = 1077, message = "A GameGuard Execution error has occurred. Please send the *.erl file(s) located in the GameGuard folder to game@inca.co.kr.")
	public static SystemMessageId A_GAMEGUARD_EXECUTION_ERROR_HAS_OCCURRED_PLEASE_SEND_THE_ERL_FILE_S_LOCATED_IN_THE_GAMEGUARD_FOLDER_TO_GAME_INCA_CO_KR;
	
	@ClientString(id = 1078, message = "When a user's keyboard input exceeds a certain cumulative score a chat ban will be applied. This is done to discourage spamming. Please avoid posting the same message multiple times during a short period.")
	public static SystemMessageId WHEN_A_USER_S_KEYBOARD_INPUT_EXCEEDS_A_CERTAIN_CUMULATIVE_SCORE_A_CHAT_BAN_WILL_BE_APPLIED_THIS_IS_DONE_TO_DISCOURAGE_SPAMMING_PLEASE_AVOID_POSTING_THE_SAME_MESSAGE_MULTIPLE_TIMES_DURING_A_SHORT_PERIOD;
	
	@ClientString(id = 1079, message = "The target is currently banned from chatting.")
	public static SystemMessageId THE_TARGET_IS_CURRENTLY_BANNED_FROM_CHATTING;
	
	@ClientString(id = 1080, message = "Being permanent, are you sure you wish to use the facelift potion - Type A?")
	public static SystemMessageId BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_FACELIFT_POTION_TYPE_A;
	
	@ClientString(id = 1081, message = "Being permanent, are you sure you wish to use the hair dye potion - Type A?")
	public static SystemMessageId BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION_TYPE_A;
	
	@ClientString(id = 1082, message = "Do you wish to use the hair style change potion – Type A? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_A_IT_IS_PERMANENT;
	
	@ClientString(id = 1083, message = "Facelift potion - Type A is being applied.")
	public static SystemMessageId FACELIFT_POTION_TYPE_A_IS_BEING_APPLIED;
	
	@ClientString(id = 1084, message = "Hair dye potion - Type A is being applied.")
	public static SystemMessageId HAIR_DYE_POTION_TYPE_A_IS_BEING_APPLIED;
	
	@ClientString(id = 1085, message = "The hair style change potion - Type A is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_A_IS_BEING_USED;
	
	@ClientString(id = 1086, message = "Your facial appearance has been changed.")
	public static SystemMessageId YOUR_FACIAL_APPEARANCE_HAS_BEEN_CHANGED;
	
	@ClientString(id = 1087, message = "Your hair color has been changed.")
	public static SystemMessageId YOUR_HAIR_COLOR_HAS_BEEN_CHANGED;
	
	@ClientString(id = 1088, message = "Your hair style has been changed.")
	public static SystemMessageId YOUR_HAIR_STYLE_HAS_BEEN_CHANGED;
	
	@ClientString(id = 1089, message = "$s1 has obtained a first anniversary commemorative item.")
	public static SystemMessageId S1_HAS_OBTAINED_A_FIRST_ANNIVERSARY_COMMEMORATIVE_ITEM;
	
	@ClientString(id = 1090, message = "Being permanent, are you sure you wish to use the facelift potion - Type B?")
	public static SystemMessageId BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_FACELIFT_POTION_TYPE_B;
	
	@ClientString(id = 1091, message = "Being permanent, are you sure you wish to use the facelift potion - Type C?")
	public static SystemMessageId BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_FACELIFT_POTION_TYPE_C;
	
	@ClientString(id = 1092, message = "Being permanent, are you sure you wish to use the hair dye potion - Type B?")
	public static SystemMessageId BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION_TYPE_B;
	
	@ClientString(id = 1093, message = "Being permanent, are you sure you wish to use the hair dye potion - Type C?")
	public static SystemMessageId BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION_TYPE_C;
	
	@ClientString(id = 1094, message = "Being permanent, are you sure you wish to use the hair dye potion - Type D?")
	public static SystemMessageId BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION_TYPE_D;
	
	@ClientString(id = 1095, message = "Do you wish to use the hair style change potion – Type B? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_B_IT_IS_PERMANENT;
	
	@ClientString(id = 1096, message = "Do you wish to use the hair style change potion – Type C? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_C_IT_IS_PERMANENT;
	
	@ClientString(id = 1097, message = "Do you wish to use the hair style change potion – Type D? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_D_IT_IS_PERMANENT;
	
	@ClientString(id = 1098, message = "Do you wish to use the hair style change potion – Type E? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_E_IT_IS_PERMANENT;
	
	@ClientString(id = 1099, message = "Do you wish to use the hair style change potion – Type F? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_F_IT_IS_PERMANENT;
	
	@ClientString(id = 1100, message = "Do you wish to use the hair style change potion – Type G? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_G_IT_IS_PERMANENT;
	
	@ClientString(id = 1101, message = "Facelift potion - Type B is being applied.")
	public static SystemMessageId FACELIFT_POTION_TYPE_B_IS_BEING_APPLIED;
	
	@ClientString(id = 1102, message = "Facelift potion - Type C is being applied.")
	public static SystemMessageId FACELIFT_POTION_TYPE_C_IS_BEING_APPLIED;
	
	@ClientString(id = 1103, message = "Hair dye potion - Type B is being applied.")
	public static SystemMessageId HAIR_DYE_POTION_TYPE_B_IS_BEING_APPLIED;
	
	@ClientString(id = 1104, message = "Hair dye potion - Type C is being applied.")
	public static SystemMessageId HAIR_DYE_POTION_TYPE_C_IS_BEING_APPLIED;
	
	@ClientString(id = 1105, message = "Hair dye potion - Type D is being applied.")
	public static SystemMessageId HAIR_DYE_POTION_TYPE_D_IS_BEING_APPLIED;
	
	@ClientString(id = 1106, message = "The hair style change potion - Type B is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_B_IS_BEING_USED;
	
	@ClientString(id = 1107, message = "The hair style change potion - Type C is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_C_IS_BEING_USED;
	
	@ClientString(id = 1108, message = "The hair style change potion - Type D is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_D_IS_BEING_USED;
	
	@ClientString(id = 1109, message = "The hair style change potion - Type E is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_E_IS_BEING_USED;
	
	@ClientString(id = 1110, message = "The hair style change potion - Type F is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_F_IS_BEING_USED;
	
	@ClientString(id = 1111, message = "The hair style change potion - Type G is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_G_IS_BEING_USED;
	
	@ClientString(id = 1112, message = "The prize amount for the winner of Lottery #$s1 is $s2 adena. We have $s3 first prize winners.")
	public static SystemMessageId THE_PRIZE_AMOUNT_FOR_THE_WINNER_OF_LOTTERY_S1_IS_S2_ADENA_WE_HAVE_S3_FIRST_PRIZE_WINNERS;
	
	@ClientString(id = 1113, message = "The prize amount for Lucky Lottery #$s1 is $s2 adena. There was no first prize winner in this drawing, therefore the jackpot will be added to the next drawing.")
	public static SystemMessageId THE_PRIZE_AMOUNT_FOR_LUCKY_LOTTERY_S1_IS_S2_ADENA_THERE_WAS_NO_FIRST_PRIZE_WINNER_IN_THIS_DRAWING_THEREFORE_THE_JACKPOT_WILL_BE_ADDED_TO_THE_NEXT_DRAWING;
	
	@ClientString(id = 1114, message = "Your clan may not register to participate in a siege while under a grace period of the clan's dissolution.")
	public static SystemMessageId YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLAN_S_DISSOLUTION;
	
	@ClientString(id = 1115, message = "Individuals may not surrender during combat.")
	public static SystemMessageId INDIVIDUALS_MAY_NOT_SURRENDER_DURING_COMBAT;
	
	@ClientString(id = 1116, message = "One cannot leave one's clan during combat.")
	public static SystemMessageId ONE_CANNOT_LEAVE_ONE_S_CLAN_DURING_COMBAT;
	
	@ClientString(id = 1117, message = "A clan member may not be dismissed during combat.")
	public static SystemMessageId A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT;
	
	@ClientString(id = 1118, message = "Progress in a quest is possible only when your inventory's weight and volume are less than 80 percent of capacity.")
	public static SystemMessageId PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORY_S_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY;
	
	@ClientString(id = 1119, message = "Quest was automatically canceled when you attempted to settle the accounts of your quest while your inventory exceeded 80 percent of capacity.")
	public static SystemMessageId QUEST_WAS_AUTOMATICALLY_CANCELED_WHEN_YOU_ATTEMPTED_TO_SETTLE_THE_ACCOUNTS_OF_YOUR_QUEST_WHILE_YOUR_INVENTORY_EXCEEDED_80_PERCENT_OF_CAPACITY;
	
	@ClientString(id = 1120, message = "You are still a member of the clan.")
	public static SystemMessageId YOU_ARE_STILL_A_MEMBER_OF_THE_CLAN;
	
	@ClientString(id = 1121, message = "You do not have the right to vote.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_VOTE;
	
	@ClientString(id = 1122, message = "There is no candidate.")
	public static SystemMessageId THERE_IS_NO_CANDIDATE;
	
	@ClientString(id = 1123, message = "Weight and volume limit has been exceeded. That skill is currently unavailable.")
	public static SystemMessageId WEIGHT_AND_VOLUME_LIMIT_HAS_BEEN_EXCEEDED_THAT_SKILL_IS_CURRENTLY_UNAVAILABLE;
	
	@ClientString(id = 1124, message = "A recipe book may not be used while using a skill.")
	public static SystemMessageId A_RECIPE_BOOK_MAY_NOT_BE_USED_WHILE_USING_A_SKILL;
	
	@ClientString(id = 1125, message = "An item may not be created while engaged in trading.")
	public static SystemMessageId AN_ITEM_MAY_NOT_BE_CREATED_WHILE_ENGAGED_IN_TRADING;
	
	@ClientString(id = 1126, message = "You cannot enter a negative number.")
	public static SystemMessageId YOU_CANNOT_ENTER_A_NEGATIVE_NUMBER;
	
	@ClientString(id = 1127, message = "The reward must be less than 10 times the standard price.")
	public static SystemMessageId THE_REWARD_MUST_BE_LESS_THAN_10_TIMES_THE_STANDARD_PRICE;
	
	@ClientString(id = 1128, message = "A private store may not be opened while using a skill.")
	public static SystemMessageId A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL;
	
	@ClientString(id = 1129, message = "This is not allowed while riding a ferry or boat.")
	public static SystemMessageId THIS_IS_NOT_ALLOWED_WHILE_RIDING_A_FERRY_OR_BOAT;
	
	@ClientString(id = 1130, message = "You have given $s1 damage to your target and $s2 damage to the servitor.")
	public static SystemMessageId YOU_HAVE_GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_THE_SERVITOR;
	
	@ClientString(id = 1131, message = "It is now midnight and the effect of $s1 can be felt.")
	public static SystemMessageId IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT;
	
	@ClientString(id = 1132, message = "It is dawn and the effect of $s1 will now disappear.")
	public static SystemMessageId IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR;
	
	@ClientString(id = 1133, message = "Since HP has decreased, the effect of $s1 can be felt.")
	public static SystemMessageId SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT;
	
	@ClientString(id = 1134, message = "Since HP has increased, the effect of $s1 will disappear.")
	public static SystemMessageId SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR;
	
	@ClientString(id = 1135, message = "While you are engaged in combat, you cannot operate a private store or private workshop.")
	public static SystemMessageId WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP;
	
	@ClientString(id = 1136, message = "Since there was an account that used this IP and attempted to log in illegally, this account is not allowed to connect to the game server for $s1 minutes. Please use another game server.")
	public static SystemMessageId SINCE_THERE_WAS_AN_ACCOUNT_THAT_USED_THIS_IP_AND_ATTEMPTED_TO_LOG_IN_ILLEGALLY_THIS_ACCOUNT_IS_NOT_ALLOWED_TO_CONNECT_TO_THE_GAME_SERVER_FOR_S1_MINUTES_PLEASE_USE_ANOTHER_GAME_SERVER;
	
	@ClientString(id = 1137, message = "$s1 harvested $s3 $s2(s).")
	public static SystemMessageId S1_HARVESTED_S3_S2_S;
	
	@ClientString(id = 1138, message = "$s1 harvested $s2(s).")
	public static SystemMessageId S1_HARVESTED_S2_S;
	
	@ClientString(id = 1139, message = "The weight and volume limit of your inventory must not be exceeded.")
	public static SystemMessageId THE_WEIGHT_AND_VOLUME_LIMIT_OF_YOUR_INVENTORY_MUST_NOT_BE_EXCEEDED;
	
	@ClientString(id = 1140, message = "Would you like to open the gate?")
	public static SystemMessageId WOULD_YOU_LIKE_TO_OPEN_THE_GATE;
	
	@ClientString(id = 1141, message = "Would you like to close the gate?")
	public static SystemMessageId WOULD_YOU_LIKE_TO_CLOSE_THE_GATE;
	
	@ClientString(id = 1142, message = "Since $s1 already exists nearby, you cannot summon it again.")
	public static SystemMessageId SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN;
	
	@ClientString(id = 1143, message = "Since you do not have enough items to maintain the servitor's stay, the servitor will disappear.")
	public static SystemMessageId SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITOR_S_STAY_THE_SERVITOR_WILL_DISAPPEAR;
	
	@ClientString(id = 1144, message = "Currently, you don't have anybody to chat with in the game.")
	public static SystemMessageId CURRENTLY_YOU_DON_T_HAVE_ANYBODY_TO_CHAT_WITH_IN_THE_GAME;
	
	@ClientString(id = 1145, message = "$s2 has been created for $s1 after the payment of $s3 adena is received.")
	public static SystemMessageId S2_HAS_BEEN_CREATED_FOR_S1_AFTER_THE_PAYMENT_OF_S3_ADENA_IS_RECEIVED;
	
	@ClientString(id = 1146, message = "$s1 created $s2 after receiving $s3 adena.")
	public static SystemMessageId S1_CREATED_S2_AFTER_RECEIVING_S3_ADENA;
	
	@ClientString(id = 1147, message = "$s2 $s3 have been created for $s1 at the price of $s4 adena.")
	public static SystemMessageId S2_S3_HAVE_BEEN_CREATED_FOR_S1_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1148, message = "$s1 created $s2 $s3 at the price of $s4 adena.")
	public static SystemMessageId S1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1149, message = "The attempt to create $s2 for $s1 at the price of $s3 adena has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_CREATE_S2_FOR_S1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED;
	
	@ClientString(id = 1150, message = "$s1 has failed to create $s2 at the price of $s3 adena.")
	public static SystemMessageId S1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA;
	
	@ClientString(id = 1151, message = "$s2 is sold to $s1 at the price of $s3 adena.")
	public static SystemMessageId S2_IS_SOLD_TO_S1_AT_THE_PRICE_OF_S3_ADENA;
	
	@ClientString(id = 1152, message = "$s2 $s3 have been sold to $s1 for $s4 adena.")
	public static SystemMessageId S2_S3_HAVE_BEEN_SOLD_TO_S1_FOR_S4_ADENA;
	
	@ClientString(id = 1153, message = "$s2 has been purchased from $s1 at the price of $s3 adena.")
	public static SystemMessageId S2_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S3_ADENA;
	
	@ClientString(id = 1154, message = "$s3 $s2 has been purchased from $s1 for $s4 adena.")
	public static SystemMessageId S3_S2_HAS_BEEN_PURCHASED_FROM_S1_FOR_S4_ADENA;
	
	@ClientString(id = 1155, message = "+$s2$s3 has been sold to $s1 at the price of $s4 adena.")
	public static SystemMessageId S2_S3_HAS_BEEN_SOLD_TO_S1_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1156, message = "+$s2$s3 has been purchased from $s1 at the price of $s4 adena.")
	public static SystemMessageId S2_S3_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1157, message = "Trying on state lasts for only 5 seconds. When a character's state changes, it can be cancelled.")
	public static SystemMessageId TRYING_ON_STATE_LASTS_FOR_ONLY_5_SECONDS_WHEN_A_CHARACTER_S_STATE_CHANGES_IT_CAN_BE_CANCELLED;
	
	@ClientString(id = 1158, message = "You cannot dismount from this elevation.")
	public static SystemMessageId YOU_CANNOT_DISMOUNT_FROM_THIS_ELEVATION;
	
	@ClientString(id = 1159, message = "The ferry from Talking Island will arrive at Gludin Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_ARRIVE_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1160, message = "The ferry from Talking Island will be arriving at Gludin Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1161, message = "The ferry from Talking Island will be arriving at Gludin Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1162, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1163, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1164, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1165, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1166, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_20_MINUTES;
	
	@ClientString(id = 1167, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1168, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1169, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1170, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1171, message = "The Innadril pleasure boat will arrive in approximately 20 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_20_MINUTES;
	
	@ClientString(id = 1172, message = "The Innadril pleasure boat will arrive in approximately 15 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1173, message = "The Innadril pleasure boat will arrive in approximately 10 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1174, message = "The Innadril pleasure boat will arrive in approximately 5 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1175, message = "The Innadril pleasure boat will arrive in approximately 1 minute.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1176, message = "This is a quest event period.")
	public static SystemMessageId THIS_IS_A_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1177, message = "This is the seal validation period.")
	public static SystemMessageId THIS_IS_THE_SEAL_VALIDATION_PERIOD;
	
	@ClientString(id = 1178, message = "This seal permits the group that holds it to exclusively enter the dungeon opened by the Seal of Avarice during the seal validation period. It also permits trading with the Merchant of Mammon who appears in special dungeons and permits meetings with Anakim or Lilith in the Disciple's Necropolis.")
	public static SystemMessageId THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_EXCLUSIVELY_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_AVARICE_DURING_THE_SEAL_VALIDATION_PERIOD_IT_ALSO_PERMITS_TRADING_WITH_THE_MERCHANT_OF_MAMMON_WHO_APPEARS_IN_SPECIAL_DUNGEONS_AND_PERMITS_MEETINGS_WITH_ANAKIM_OR_LILITH_IN_THE_DISCIPLE_S_NECROPOLIS;
	
	@ClientString(id = 1179, message = "This seal permits the group that holds it to enter the dungeon opened by the Seal of Gnosis, use the teleportation service offered by the priest in the village, and do business with the Blacksmith of Mammon. The Orator of Revelations appears and casts good magic on the winners, and the Preacher of Doom appears and casts bad magic on the losers.")
	public static SystemMessageId THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_GNOSIS_USE_THE_TELEPORTATION_SERVICE_OFFERED_BY_THE_PRIEST_IN_THE_VILLAGE_AND_DO_BUSINESS_WITH_THE_BLACKSMITH_OF_MAMMON_THE_ORATOR_OF_REVELATIONS_APPEARS_AND_CASTS_GOOD_MAGIC_ON_THE_WINNERS_AND_THE_PREACHER_OF_DOOM_APPEARS_AND_CASTS_BAD_MAGIC_ON_THE_LOSERS;
	
	@ClientString(id = 1180, message = "During the Seal Validation period, the cabal's maximum CP amount increases. In addition, the cabal posessing the seal will benefit from favorable changes in the cost to upgrade castle defense mercenaries, castle gates and walls; basic P. Def. of castle gates and walls; and the limit imposed on the castle tax rate. The use of siege war weapons will also be limited. If the Revolutionary Army of Dusk takes possession of this seal during the castle siege war, only the allies of the clan that owns the castle can come to the aid of the defenders.")
	public static SystemMessageId DURING_THE_SEAL_VALIDATION_PERIOD_THE_CABAL_S_MAXIMUM_CP_AMOUNT_INCREASES_IN_ADDITION_THE_CABAL_POSESSING_THE_SEAL_WILL_BENEFIT_FROM_FAVORABLE_CHANGES_IN_THE_COST_TO_UPGRADE_CASTLE_DEFENSE_MERCENARIES_CASTLE_GATES_AND_WALLS_BASIC_P_DEF_OF_CASTLE_GATES_AND_WALLS_AND_THE_LIMIT_IMPOSED_ON_THE_CASTLE_TAX_RATE_THE_USE_OF_SIEGE_WAR_WEAPONS_WILL_ALSO_BE_LIMITED_IF_THE_REVOLUTIONARY_ARMY_OF_DUSK_TAKES_POSSESSION_OF_THIS_SEAL_DURING_THE_CASTLE_SIEGE_WAR_ONLY_THE_ALLIES_OF_THE_CLAN_THAT_OWNS_THE_CASTLE_CAN_COME_TO_THE_AID_OF_THE_DEFENDERS;
	
	@ClientString(id = 1181, message = "Do you really wish to change the title?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_CHANGE_THE_TITLE;
	
	@ClientString(id = 1182, message = "Are you sure you wish to delete the clan crest?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WISH_TO_DELETE_THE_CLAN_CREST;
	
	@ClientString(id = 1183, message = "This is the initial period.")
	public static SystemMessageId THIS_IS_THE_INITIAL_PERIOD;
	
	@ClientString(id = 1184, message = "This is a period of calculating statistics in the server.")
	public static SystemMessageId THIS_IS_A_PERIOD_OF_CALCULATING_STATISTICS_IN_THE_SERVER;
	
	@ClientString(id = 1185, message = "days left until deletion.")
	public static SystemMessageId DAYS_LEFT_UNTIL_DELETION;
	
	@ClientString(id = 1186, message = "To create a new account, please visit the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId TO_CREATE_A_NEW_ACCOUNT_PLEASE_VISIT_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1187, message = "If you've forgotten your account information or password, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId IF_YOU_VE_FORGOTTEN_YOUR_ACCOUNT_INFORMATION_OR_PASSWORD_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1188, message = "Your selected target can no longer receive a recommendation.")
	public static SystemMessageId YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION;
	
	@ClientString(id = 1189, message = "The temporary alliance of the Castle Attacker team is in effect. It will be dissolved when the Castle Lord is replaced.")
	public static SystemMessageId THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT_IT_WILL_BE_DISSOLVED_WHEN_THE_CASTLE_LORD_IS_REPLACED;
	
	@ClientString(id = 1190, message = "The temporary alliance of the Castle Attacker team has been dissolved.")
	public static SystemMessageId THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED;
	
	@ClientString(id = 1191, message = "The ferry from Gludin Harbor will be arriving at Talking Island in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1192, message = "The ferry from Gludin Harbor will be arriving at Talking Island in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1193, message = "The ferry from Gludin Harbor will be arriving at Talking Island in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1194, message = "A mercenary can be assigned to a position from the beginning of the Seal Validation period until the time when a siege starts.")
	public static SystemMessageId A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS;
	
	@ClientString(id = 1195, message = "This mercenary cannot be assigned to a position by using the Seal of Strife.")
	public static SystemMessageId THIS_MERCENARY_CANNOT_BE_ASSIGNED_TO_A_POSITION_BY_USING_THE_SEAL_OF_STRIFE;
	
	@ClientString(id = 1196, message = "Your force has reached maximum capacity.")
	public static SystemMessageId YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_2;
	
	@ClientString(id = 1197, message = "Summoning a servitor costs $s2 $s1.")
	public static SystemMessageId SUMMONING_A_SERVITOR_COSTS_S2_S1;
	
	@ClientString(id = 1198, message = "The item has been successfully crystallized.")
	public static SystemMessageId THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED;
	
	@ClientString(id = 1199, message = "=======<Clan War Target>=======")
	public static SystemMessageId CLAN_WAR_TARGET;
	
	@ClientString(id = 1200, message = "= $s1 ($s2 Alliance)")
	public static SystemMessageId S1_S2_ALLIANCE;
	
	@ClientString(id = 1201, message = "Please select the quest you wish to abort.")
	public static SystemMessageId PLEASE_SELECT_THE_QUEST_YOU_WISH_TO_ABORT;
	
	@ClientString(id = 1202, message = "= $s1 (No alliance exists)")
	public static SystemMessageId S1_NO_ALLIANCE_EXISTS;
	
	@ClientString(id = 1203, message = "There is no clan war in progress.")
	public static SystemMessageId THERE_IS_NO_CLAN_WAR_IN_PROGRESS;
	
	@ClientString(id = 1204, message = "The screenshot has been saved. ($s1 $s2x$s3)")
	public static SystemMessageId THE_SCREENSHOT_HAS_BEEN_SAVED_S1_S2X_S3;
	
	@ClientString(id = 1205, message = "Your mailbox is full. There is a 100 message limit.")
	public static SystemMessageId YOUR_MAILBOX_IS_FULL_THERE_IS_A_100_MESSAGE_LIMIT;
	
	@ClientString(id = 1206, message = "The memo box is full. There is a 100 memo limit.")
	public static SystemMessageId THE_MEMO_BOX_IS_FULL_THERE_IS_A_100_MEMO_LIMIT;
	
	@ClientString(id = 1207, message = "Please make an entry in the field.")
	public static SystemMessageId PLEASE_MAKE_AN_ENTRY_IN_THE_FIELD;
	
	@ClientString(id = 1208, message = "$s1 died and dropped $s3 $s2.")
	public static SystemMessageId S1_DIED_AND_DROPPED_S3_S2;
	
	@ClientString(id = 1209, message = "Congratulations. Your raid was successful.")
	public static SystemMessageId CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL;
	
	@ClientString(id = 1210, message = "Seven Signs: The quest event period has begun. Visit a Priest of Dawn or Priestess of Dusk to participate in the event.")
	public static SystemMessageId SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_VISIT_A_PRIEST_OF_DAWN_OR_PRIESTESS_OF_DUSK_TO_PARTICIPATE_IN_THE_EVENT;
	
	@ClientString(id = 1211, message = "Seven Signs: The quest event period has ended. The next quest event will start in one week.")
	public static SystemMessageId SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_ENDED_THE_NEXT_QUEST_EVENT_WILL_START_IN_ONE_WEEK;
	
	@ClientString(id = 1212, message = "Seven Signs: The Lords of Dawn have obtained the Seal of Avarice.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_AVARICE;
	
	@ClientString(id = 1213, message = "Seven Signs: The Lords of Dawn have obtained the Seal of Gnosis.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS;
	
	@ClientString(id = 1214, message = "Seven Signs: The Lords of Dawn have obtained the Seal of Strife.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_STRIFE;
	
	@ClientString(id = 1215, message = "Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Avarice.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_AVARICE;
	
	@ClientString(id = 1216, message = "Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Gnosis.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS;
	
	@ClientString(id = 1217, message = "Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Strife.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_STRIFE;
	
	@ClientString(id = 1218, message = "Seven Signs: The Seal Validation period has begun.")
	public static SystemMessageId SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_BEGUN;
	
	@ClientString(id = 1219, message = "Seven Signs: The Seal Validation period has ended.")
	public static SystemMessageId SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_ENDED;
	
	@ClientString(id = 1220, message = "Are you sure you wish to summon it?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WISH_TO_SUMMON_IT;
	
	@ClientString(id = 1221, message = "Do you really wish to return it?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_RETURN_IT;
	
	@ClientString(id = 1222, message = "Current Location: $s1, $s2, $s3 (GM Consultation Service)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_GM_CONSULTATION_SERVICE;
	
	@ClientString(id = 1223, message = "We depart for Talking Island in five minutes.")
	public static SystemMessageId WE_DEPART_FOR_TALKING_ISLAND_IN_FIVE_MINUTES;
	
	@ClientString(id = 1224, message = "We depart for Talking Island in one minute.")
	public static SystemMessageId WE_DEPART_FOR_TALKING_ISLAND_IN_ONE_MINUTE;
	
	@ClientString(id = 1225, message = "All aboard for Talking Island!")
	public static SystemMessageId ALL_ABOARD_FOR_TALKING_ISLAND;
	
	@ClientString(id = 1226, message = "We are now leaving for Talking Island.")
	public static SystemMessageId WE_ARE_NOW_LEAVING_FOR_TALKING_ISLAND;
	
	@ClientString(id = 1227, message = "You have $s1 unread messages.")
	public static SystemMessageId YOU_HAVE_S1_UNREAD_MESSAGES;
	
	@ClientString(id = 1228, message = "$s1 has blocked you. You cannot send mail to $s1 .")
	public static SystemMessageId S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1;
	
	@ClientString(id = 1229, message = "No more messages may be sent at this time. Each account is allowed 10 messages per day.")
	public static SystemMessageId NO_MORE_MESSAGES_MAY_BE_SENT_AT_THIS_TIME_EACH_ACCOUNT_IS_ALLOWED_10_MESSAGES_PER_DAY;
	
	@ClientString(id = 1230, message = "You are limited to five recipients at a time.")
	public static SystemMessageId YOU_ARE_LIMITED_TO_FIVE_RECIPIENTS_AT_A_TIME;
	
	@ClientString(id = 1231, message = "You've sent mail.")
	public static SystemMessageId YOU_VE_SENT_MAIL;
	
	@ClientString(id = 1232, message = "The message was not sent.")
	public static SystemMessageId THE_MESSAGE_WAS_NOT_SENT;
	
	@ClientString(id = 1233, message = "You've got mail.")
	public static SystemMessageId YOU_VE_GOT_MAIL;
	
	@ClientString(id = 1234, message = "The mail has been stored in your temporary mailbox.")
	public static SystemMessageId THE_MAIL_HAS_BEEN_STORED_IN_YOUR_TEMPORARY_MAILBOX;
	
	@ClientString(id = 1235, message = "Do you wish to delete all your friends?")
	public static SystemMessageId DO_YOU_WISH_TO_DELETE_ALL_YOUR_FRIENDS;
	
	@ClientString(id = 1236, message = "Please enter security card number.")
	public static SystemMessageId PLEASE_ENTER_SECURITY_CARD_NUMBER;
	
	@ClientString(id = 1237, message = "Please enter the card number for number $s1.")
	public static SystemMessageId PLEASE_ENTER_THE_CARD_NUMBER_FOR_NUMBER_S1;
	
	@ClientString(id = 1238, message = "Your temporary mailbox is full. No more mail can be stored; you have reached the 10 message limit.")
	public static SystemMessageId YOUR_TEMPORARY_MAILBOX_IS_FULL_NO_MORE_MAIL_CAN_BE_STORED_YOU_HAVE_REACHED_THE_10_MESSAGE_LIMIT;
	
	@ClientString(id = 1239, message = "The keyboard security module has failed to load. Please exit the game and try again.")
	public static SystemMessageId THE_KEYBOARD_SECURITY_MODULE_HAS_FAILED_TO_LOAD_PLEASE_EXIT_THE_GAME_AND_TRY_AGAIN;
	
	@ClientString(id = 1240, message = "Seven Signs: The Revolutionaries of Dusk have won.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_WON;
	
	@ClientString(id = 1241, message = "Seven Signs: The Lords of Dawn have won.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_WON;
	
	@ClientString(id = 1242, message = "Users who have not verified their age may not log in between the hours of 10:00 p.m. and 6:00 a.m.")
	public static SystemMessageId USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_MAY_NOT_LOG_IN_BETWEEN_THE_HOURS_OF_10_00_P_M_AND_6_00_A_M;
	
	@ClientString(id = 1243, message = "The security card number is invalid.")
	public static SystemMessageId THE_SECURITY_CARD_NUMBER_IS_INVALID;
	
	@ClientString(id = 1244, message = "Users who have not verified their age may not log in between the hours of 10:00 p.m. and 6:00 a.m. Logging off now.")
	public static SystemMessageId USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_MAY_NOT_LOG_IN_BETWEEN_THE_HOURS_OF_10_00_P_M_AND_6_00_A_M_LOGGING_OFF_NOW;
	
	@ClientString(id = 1245, message = "You will be logged out in $s1 minutes.")
	public static SystemMessageId YOU_WILL_BE_LOGGED_OUT_IN_S1_MINUTES;
	
	@ClientString(id = 1246, message = "$s1 died and has dropped $s2 adena.")
	public static SystemMessageId S1_DIED_AND_HAS_DROPPED_S2_ADENA;
	
	@ClientString(id = 1247, message = "The corpse is too old. The skill cannot be used.")
	public static SystemMessageId THE_CORPSE_IS_TOO_OLD_THE_SKILL_CANNOT_BE_USED;
	
	@ClientString(id = 1248, message = "You are out of feed. Mount status canceled.")
	public static SystemMessageId YOU_ARE_OUT_OF_FEED_MOUNT_STATUS_CANCELED;
	
	@ClientString(id = 1249, message = "You may only ride a wyvern while you're riding a strider.")
	public static SystemMessageId YOU_MAY_ONLY_RIDE_A_WYVERN_WHILE_YOU_RE_RIDING_A_STRIDER;
	
	@ClientString(id = 1250, message = "Do you really want to surrender? If you surrender during an alliance war, your Exp will drop the same as if you were to die once.")
	public static SystemMessageId DO_YOU_REALLY_WANT_TO_SURRENDER_IF_YOU_SURRENDER_DURING_AN_ALLIANCE_WAR_YOUR_EXP_WILL_DROP_THE_SAME_AS_IF_YOU_WERE_TO_DIE_ONCE;
	
	@ClientString(id = 1251, message = "Are you sure you want to dismiss the alliance? If you use the /allydismiss command, you will not be able to accept another clan to your alliance for one day.")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_DISMISS_THE_ALLIANCE_IF_YOU_USE_THE_ALLYDISMISS_COMMAND_YOU_WILL_NOT_BE_ABLE_TO_ACCEPT_ANOTHER_CLAN_TO_YOUR_ALLIANCE_FOR_ONE_DAY;
	
	@ClientString(id = 1252, message = "Are you sure you want to surrender? Exp penalty will be the same as death.")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH;
	
	@ClientString(id = 1253, message = "Are you sure you want to surrender? Exp penalty will be the same as death and you will not be allowed to participate in clan war.")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH_AND_YOU_WILL_NOT_BE_ALLOWED_TO_PARTICIPATE_IN_CLAN_WAR;
	
	@ClientString(id = 1254, message = "Thank you for submitting feedback.")
	public static SystemMessageId THANK_YOU_FOR_SUBMITTING_FEEDBACK;
	
	@ClientString(id = 1255, message = "GM consultation has begun.")
	public static SystemMessageId GM_CONSULTATION_HAS_BEGUN;
	
	@ClientString(id = 1256, message = "Please write the name after the command.")
	public static SystemMessageId PLEASE_WRITE_THE_NAME_AFTER_THE_COMMAND;
	
	@ClientString(id = 1257, message = "The special skill of a servitor or pet cannot be registered as a macro.")
	public static SystemMessageId THE_SPECIAL_SKILL_OF_A_SERVITOR_OR_PET_CANNOT_BE_REGISTERED_AS_A_MACRO;
	
	@ClientString(id = 1258, message = "$s1 has been crystallized.")
	public static SystemMessageId S1_HAS_BEEN_CRYSTALLIZED;
	
	@ClientString(id = 1259, message = "=======<Alliance Target>=======")
	public static SystemMessageId ALLIANCE_TARGET;
	
	@ClientString(id = 1260, message = "Seven Signs: Preparations have begun for the next quest event.")
	public static SystemMessageId SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT;
	
	@ClientString(id = 1261, message = "Seven Signs: The quest event period has begun. Speak with a Priest of Dawn or Dusk Priestess if you wish to participate in the event.")
	public static SystemMessageId SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT;
	
	@ClientString(id = 1262, message = "Seven Signs: Quest event has ended. Results are being tallied.")
	public static SystemMessageId SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED;
	
	@ClientString(id = 1263, message = "Seven Signs: This is the seal validation period. A new quest event period begins next Monday.")
	public static SystemMessageId SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY;
	
	@ClientString(id = 1264, message = "This soul stone cannot currently absorb souls. Absorption has failed.")
	public static SystemMessageId THIS_SOUL_STONE_CANNOT_CURRENTLY_ABSORB_SOULS_ABSORPTION_HAS_FAILED;
	
	@ClientString(id = 1265, message = "You can't absorb souls without a soul stone.")
	public static SystemMessageId YOU_CAN_T_ABSORB_SOULS_WITHOUT_A_SOUL_STONE;
	
	@ClientString(id = 1266, message = "The exchange has ended.")
	public static SystemMessageId THE_EXCHANGE_HAS_ENDED;
	
	@ClientString(id = 1267, message = "Your contribution score is increased by $s1.")
	public static SystemMessageId YOUR_CONTRIBUTION_SCORE_IS_INCREASED_BY_S1;
	
	@ClientString(id = 1268, message = "Do you wish to add $s1 class as your sub class?")
	public static SystemMessageId DO_YOU_WISH_TO_ADD_S1_CLASS_AS_YOUR_SUB_CLASS;
	
	@ClientString(id = 1269, message = "The new sub class has been added.")
	public static SystemMessageId THE_NEW_SUB_CLASS_HAS_BEEN_ADDED;
	
	@ClientString(id = 1270, message = "The transfer of sub class has been completed.")
	public static SystemMessageId THE_TRANSFER_OF_SUB_CLASS_HAS_BEEN_COMPLETED;
	
	@ClientString(id = 1271, message = "Do you wish to participate? Until the next seal validation period, you are a member of the Lords of Dawn.")
	public static SystemMessageId DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_LORDS_OF_DAWN;
	
	@ClientString(id = 1272, message = "Do you wish to participate? Until the next seal validation period, you are a member of the Revolutionaries of Dusk.")
	public static SystemMessageId DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK;
	
	@ClientString(id = 1273, message = "You will participate in the Seven Signs as a member of the Lords of Dawn.")
	public static SystemMessageId YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN;
	
	@ClientString(id = 1274, message = "You will participate in the Seven Signs as a member of the Revolutionaries of Dusk.")
	public static SystemMessageId YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK;
	
	@ClientString(id = 1275, message = "You've chosen to fight for the Seal of Avarice during this quest event period.")
	public static SystemMessageId YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1276, message = "You've chosen to fight for the Seal of Gnosis during this quest event period.")
	public static SystemMessageId YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1277, message = "You've chosen to fight for the Seal of Strife during this quest event period.")
	public static SystemMessageId YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1278, message = "The NPC server is not operating at this time.")
	public static SystemMessageId THE_NPC_SERVER_IS_NOT_OPERATING_AT_THIS_TIME;
	
	@ClientString(id = 1279, message = "Contribution level has exceeded the limit. You may not continue.")
	public static SystemMessageId CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE;
	
	@ClientString(id = 1280, message = "Magic Critical Hit!")
	public static SystemMessageId MAGIC_CRITICAL_HIT;
	
	@ClientString(id = 1281, message = "Your excellent shield defense was a success!")
	public static SystemMessageId YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS;
	
	@ClientString(id = 1282, message = "Your Karma has been changed to $s1.")
	public static SystemMessageId YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1;
	
	@ClientString(id = 1283, message = "The minimum frame option has been activated.")
	public static SystemMessageId THE_MINIMUM_FRAME_OPTION_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 1284, message = "The minimum frame option has been deactivated.")
	public static SystemMessageId THE_MINIMUM_FRAME_OPTION_HAS_BEEN_DEACTIVATED;
	
	@ClientString(id = 1285, message = "No inventory exists. You cannot purchase an item.")
	public static SystemMessageId NO_INVENTORY_EXISTS_YOU_CANNOT_PURCHASE_AN_ITEM;
	
	@ClientString(id = 1286, message = "(Until next Monday at 6:00 p.m.)")
	public static SystemMessageId UNTIL_NEXT_MONDAY_AT_6_00_P_M;
	
	@ClientString(id = 1287, message = "(Until today at 6:00 p.m.)")
	public static SystemMessageId UNTIL_TODAY_AT_6_00_P_M;
	
	@ClientString(id = 1288, message = "If trends continue, $s1 will win and the seal will belong to:")
	public static SystemMessageId IF_TRENDS_CONTINUE_S1_WILL_WIN_AND_THE_SEAL_WILL_BELONG_TO;
	
	@ClientString(id = 1289, message = "Since the seal was owned during the previous period and 10 percent or more people have voted.")
	public static SystemMessageId SINCE_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_10_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1290, message = "Although the seal was not owned, since 35 percent or more people have voted.")
	public static SystemMessageId ALTHOUGH_THE_SEAL_WAS_NOT_OWNED_SINCE_35_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1291, message = "Although the seal was owned during the previous period, because less than 10 percent of people have voted.")
	public static SystemMessageId ALTHOUGH_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_BECAUSE_LESS_THAN_10_PERCENT_OF_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1292, message = "Since the seal was not owned during the previous period, and since less than 35 percent of people have voted.")
	public static SystemMessageId SINCE_THE_SEAL_WAS_NOT_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_SINCE_LESS_THAN_35_PERCENT_OF_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1293, message = "If current trends continue, it will end in a tie.")
	public static SystemMessageId IF_CURRENT_TRENDS_CONTINUE_IT_WILL_END_IN_A_TIE;
	
	@ClientString(id = 1294, message = "The competition has ended in a tie. Therefore, nobody has been awarded the seal.")
	public static SystemMessageId THE_COMPETITION_HAS_ENDED_IN_A_TIE_THEREFORE_NOBODY_HAS_BEEN_AWARDED_THE_SEAL;
	
	@ClientString(id = 1295, message = "Sub classes may not be created or changed while a skill is in use.")
	public static SystemMessageId SUB_CLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE;
	
	@ClientString(id = 1296, message = "You cannot open a Private Store here.")
	public static SystemMessageId YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE;
	
	@ClientString(id = 1297, message = "You cannot open a Private Workshop here.")
	public static SystemMessageId YOU_CANNOT_OPEN_A_PRIVATE_WORKSHOP_HERE;
	
	@ClientString(id = 1298, message = "Please confirm that you would like to exit the Monster Race Track.")
	public static SystemMessageId PLEASE_CONFIRM_THAT_YOU_WOULD_LIKE_TO_EXIT_THE_MONSTER_RACE_TRACK;
	
	@ClientString(id = 1299, message = "$s1's casting has been interrupted.")
	public static SystemMessageId S1_S_CASTING_HAS_BEEN_INTERRUPTED;
	
	@ClientString(id = 1300, message = "You are no longer trying on equipment.")
	public static SystemMessageId YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT;
	
	@ClientString(id = 1301, message = "Only a Lord of Dawn may use this.")
	public static SystemMessageId ONLY_A_LORD_OF_DAWN_MAY_USE_THIS;
	
	@ClientString(id = 1302, message = "Only a Revolutionary of Dusk may use this.")
	public static SystemMessageId ONLY_A_REVOLUTIONARY_OF_DUSK_MAY_USE_THIS;
	
	@ClientString(id = 1303, message = "This may only be used during the quest event period.")
	public static SystemMessageId THIS_MAY_ONLY_BE_USED_DURING_THE_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1304, message = "The influence of the Seal of Strife has caused all defensive registrations to be canceled, except for an Alliance with a castle owning clan.")
	public static SystemMessageId THE_INFLUENCE_OF_THE_SEAL_OF_STRIFE_HAS_CAUSED_ALL_DEFENSIVE_REGISTRATIONS_TO_BE_CANCELED_EXCEPT_FOR_AN_ALLIANCE_WITH_A_CASTLE_OWNING_CLAN;
	
	@ClientString(id = 1305, message = "Seal Stones may only be transferred during the quest event period.")
	public static SystemMessageId SEAL_STONES_MAY_ONLY_BE_TRANSFERRED_DURING_THE_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1306, message = "You are no longer trying on equipment.")
	public static SystemMessageId YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT_2;
	
	@ClientString(id = 1307, message = "Only during the seal validation period may you settle your account.")
	public static SystemMessageId ONLY_DURING_THE_SEAL_VALIDATION_PERIOD_MAY_YOU_SETTLE_YOUR_ACCOUNT;
	
	@ClientString(id = 1308, message = "Congratulations - You've completed a class transfer!")
	public static SystemMessageId CONGRATULATIONS_YOU_VE_COMPLETED_A_CLASS_TRANSFER;
	
	@ClientString(id = 1309, message = "To use this option, you must have the latest version of MSN Messenger installed on your computer.")
	public static SystemMessageId TO_USE_THIS_OPTION_YOU_MUST_HAVE_THE_LATEST_VERSION_OF_MSN_MESSENGER_INSTALLED_ON_YOUR_COMPUTER;
	
	@ClientString(id = 1310, message = "For full functionality, the latest version of MSN Messenger must be installed on your computer.")
	public static SystemMessageId FOR_FULL_FUNCTIONALITY_THE_LATEST_VERSION_OF_MSN_MESSENGER_MUST_BE_INSTALLED_ON_YOUR_COMPUTER;
	
	@ClientString(id = 1311, message = "Previous versions of MSN Messenger only provide the basic features for in-game MSN Messenger chat. Add/Delete Contacts and other MSN Messenger options are not available.")
	public static SystemMessageId PREVIOUS_VERSIONS_OF_MSN_MESSENGER_ONLY_PROVIDE_THE_BASIC_FEATURES_FOR_IN_GAME_MSN_MESSENGER_CHAT_ADD_DELETE_CONTACTS_AND_OTHER_MSN_MESSENGER_OPTIONS_ARE_NOT_AVAILABLE;
	
	@ClientString(id = 1312, message = "The latest version of MSN Messenger may be obtained from the MSN web site (http://messenger.msn.com).")
	public static SystemMessageId THE_LATEST_VERSION_OF_MSN_MESSENGER_MAY_BE_OBTAINED_FROM_THE_MSN_WEB_SITE_HTTP_MESSENGER_MSN_COM;
	
	@ClientString(id = 1313, message = "$s1, to better server our customers, all chat histories are stored and maintained by Ncsoft. If you do not agree to have your chat records stored, please close the chat window now. For more information regarding this procedure, please visit our home page at www.PlayNC.com. Thank you!")
	public static SystemMessageId S1_TO_BETTER_SERVER_OUR_CUSTOMERS_ALL_CHAT_HISTORIES_ARE_STORED_AND_MAINTAINED_BY_NCSOFT_IF_YOU_DO_NOT_AGREE_TO_HAVE_YOUR_CHAT_RECORDS_STORED_PLEASE_CLOSE_THE_CHAT_WINDOW_NOW_FOR_MORE_INFORMATION_REGARDING_THIS_PROCEDURE_PLEASE_VISIT_OUR_HOME_PAGE_AT_WWW_PLAYNC_COM_THANK_YOU;
	
	@ClientString(id = 1314, message = "Please enter the passport ID of the person you wish to add to your contact list.")
	public static SystemMessageId PLEASE_ENTER_THE_PASSPORT_ID_OF_THE_PERSON_YOU_WISH_TO_ADD_TO_YOUR_CONTACT_LIST;
	
	@ClientString(id = 1315, message = "Deleting a contact will remove that contact from MSN Messenger as well. The contact can still check your online status and will not be blocked from sending you a message.")
	public static SystemMessageId DELETING_A_CONTACT_WILL_REMOVE_THAT_CONTACT_FROM_MSN_MESSENGER_AS_WELL_THE_CONTACT_CAN_STILL_CHECK_YOUR_ONLINE_STATUS_AND_WILL_NOT_BE_BLOCKED_FROM_SENDING_YOU_A_MESSAGE;
	
	@ClientString(id = 1316, message = "The contact will be deleted and blocked from your contact list.")
	public static SystemMessageId THE_CONTACT_WILL_BE_DELETED_AND_BLOCKED_FROM_YOUR_CONTACT_LIST;
	
	@ClientString(id = 1317, message = "Would you like to delete this contact?")
	public static SystemMessageId WOULD_YOU_LIKE_TO_DELETE_THIS_CONTACT;
	
	@ClientString(id = 1318, message = "Please select the contact you want to block or unblock.")
	public static SystemMessageId PLEASE_SELECT_THE_CONTACT_YOU_WANT_TO_BLOCK_OR_UNBLOCK;
	
	@ClientString(id = 1319, message = "Please select the name of the contact you wish to change to another group.")
	public static SystemMessageId PLEASE_SELECT_THE_NAME_OF_THE_CONTACT_YOU_WISH_TO_CHANGE_TO_ANOTHER_GROUP;
	
	@ClientString(id = 1320, message = "After selecting the group you wish to move your contact to, press the OK button.")
	public static SystemMessageId AFTER_SELECTING_THE_GROUP_YOU_WISH_TO_MOVE_YOUR_CONTACT_TO_PRESS_THE_OK_BUTTON;
	
	@ClientString(id = 1321, message = "Enter the name of the group you wish to add.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_GROUP_YOU_WISH_TO_ADD;
	
	@ClientString(id = 1322, message = "Select the group and enter the new name.")
	public static SystemMessageId SELECT_THE_GROUP_AND_ENTER_THE_NEW_NAME;
	
	@ClientString(id = 1323, message = "Select the group you wish to delete and click the OK button.")
	public static SystemMessageId SELECT_THE_GROUP_YOU_WISH_TO_DELETE_AND_CLICK_THE_OK_BUTTON;
	
	@ClientString(id = 1324, message = "Signing in...")
	public static SystemMessageId SIGNING_IN;
	
	@ClientString(id = 1325, message = "You've logged into another computer and have been logged out of the .NET Messenger Service on this computer.")
	public static SystemMessageId YOU_VE_LOGGED_INTO_ANOTHER_COMPUTER_AND_HAVE_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE_ON_THIS_COMPUTER;
	
	@ClientString(id = 1326, message = "$s1:")
	public static SystemMessageId S1;
	
	@ClientString(id = 1327, message = "The following message could not be delivered:")
	public static SystemMessageId THE_FOLLOWING_MESSAGE_COULD_NOT_BE_DELIVERED;
	
	@ClientString(id = 1328, message = "Members of the Revolutionaries of Dusk will not be resurrected.")
	public static SystemMessageId MEMBERS_OF_THE_REVOLUTIONARIES_OF_DUSK_WILL_NOT_BE_RESURRECTED;
	
	@ClientString(id = 1329, message = "You are currently blocked from using the Private Store and Private Workshop.")
	public static SystemMessageId YOU_ARE_CURRENTLY_BLOCKED_FROM_USING_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP;
	
	@ClientString(id = 1330, message = "You may not open a Private Store or Private Workshop for another $s1 minute(s).")
	public static SystemMessageId YOU_MAY_NOT_OPEN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_FOR_ANOTHER_S1_MINUTE_S;
	
	@ClientString(id = 1331, message = "You are no longer blocked from using the Private Store or Private Workshop.")
	public static SystemMessageId YOU_ARE_NO_LONGER_BLOCKED_FROM_USING_THE_PRIVATE_STORE_OR_PRIVATE_WORKSHOP;
	
	@ClientString(id = 1332, message = "Items may not be used after your character or pet dies.")
	public static SystemMessageId ITEMS_MAY_NOT_BE_USED_AFTER_YOUR_CHARACTER_OR_PET_DIES;
	
	@ClientString(id = 1333, message = "The replay file is not accessible. Please verify that the replay.ini file exists in your Lineage 2 directory.")
	public static SystemMessageId THE_REPLAY_FILE_IS_NOT_ACCESSIBLE_PLEASE_VERIFY_THAT_THE_REPLAY_INI_FILE_EXISTS_IN_YOUR_LINEAGE_2_DIRECTORY;
	
	@ClientString(id = 1334, message = "The new camera data has been stored.")
	public static SystemMessageId THE_NEW_CAMERA_DATA_HAS_BEEN_STORED;
	
	@ClientString(id = 1335, message = "The attempt to store the new camera data has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_STORE_THE_NEW_CAMERA_DATA_HAS_FAILED;
	
	@ClientString(id = 1336, message = "The replay file, $s1.$s2 has been corrupted, please check the file.")
	public static SystemMessageId THE_REPLAY_FILE_S1_S2_HAS_BEEN_CORRUPTED_PLEASE_CHECK_THE_FILE;
	
	@ClientString(id = 1337, message = "This will terminate the replay. Do you wish to continue?")
	public static SystemMessageId THIS_WILL_TERMINATE_THE_REPLAY_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 1338, message = "You have exceeded the maximum amount that may be transferred at one time.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_MAXIMUM_AMOUNT_THAT_MAY_BE_TRANSFERRED_AT_ONE_TIME;
	
	@ClientString(id = 1339, message = "Once a macro is assigned to a shortcut, it cannot be run as a macro again.")
	public static SystemMessageId ONCE_A_MACRO_IS_ASSIGNED_TO_A_SHORTCUT_IT_CANNOT_BE_RUN_AS_A_MACRO_AGAIN;
	
	@ClientString(id = 1340, message = "This server cannot be accessed by the coupon you are using.")
	public static SystemMessageId THIS_SERVER_CANNOT_BE_ACCESSED_BY_THE_COUPON_YOU_ARE_USING;
	
	@ClientString(id = 1341, message = "Incorrect name and/or email address.")
	public static SystemMessageId INCORRECT_NAME_AND_OR_EMAIL_ADDRESS;
	
	@ClientString(id = 1342, message = "You are already logged in.")
	public static SystemMessageId YOU_ARE_ALREADY_LOGGED_IN;
	
	@ClientString(id = 1343, message = "Incorrect email address and/or password. Your attempt to log into .NET Messenger Service has failed.")
	public static SystemMessageId INCORRECT_EMAIL_ADDRESS_AND_OR_PASSWORD_YOUR_ATTEMPT_TO_LOG_INTO_NET_MESSENGER_SERVICE_HAS_FAILED;
	
	@ClientString(id = 1344, message = "Your request to log into the .NET Messenger Service has failed. Please verify that you are currently connected to the internet.")
	public static SystemMessageId YOUR_REQUEST_TO_LOG_INTO_THE_NET_MESSENGER_SERVICE_HAS_FAILED_PLEASE_VERIFY_THAT_YOU_ARE_CURRENTLY_CONNECTED_TO_THE_INTERNET;
	
	@ClientString(id = 1345, message = "Click on the OK button after you have selected a contact name.")
	public static SystemMessageId CLICK_ON_THE_OK_BUTTON_AFTER_YOU_HAVE_SELECTED_A_CONTACT_NAME;
	
	@ClientString(id = 1346, message = "You are currently entering a chat message.")
	public static SystemMessageId YOU_ARE_CURRENTLY_ENTERING_A_CHAT_MESSAGE;
	
	@ClientString(id = 1347, message = "The Lineage II messenger could not carry out the task you requested.")
	public static SystemMessageId THE_LINEAGE_II_MESSENGER_COULD_NOT_CARRY_OUT_THE_TASK_YOU_REQUESTED;
	
	@ClientString(id = 1348, message = "$s1 has entered the chat room.")
	public static SystemMessageId S1_HAS_ENTERED_THE_CHAT_ROOM;
	
	@ClientString(id = 1349, message = "$s1 has left the chat room.")
	public static SystemMessageId S1_HAS_LEFT_THE_CHAT_ROOM;
	
	@ClientString(id = 1350, message = "The status will be changed to indicate 'off-line.' All the chat windows currently opened will be closed.")
	public static SystemMessageId THE_STATUS_WILL_BE_CHANGED_TO_INDICATE_OFF_LINE_ALL_THE_CHAT_WINDOWS_CURRENTLY_OPENED_WILL_BE_CLOSED;
	
	@ClientString(id = 1351, message = "Click the Delete button after selecting the contact you wish to remove.")
	public static SystemMessageId CLICK_THE_DELETE_BUTTON_AFTER_SELECTING_THE_CONTACT_YOU_WISH_TO_REMOVE;
	
	@ClientString(id = 1352, message = "You have been added to $s1 ($s2)'s contact list.")
	public static SystemMessageId YOU_HAVE_BEEN_ADDED_TO_S1_S2_S_CONTACT_LIST;
	
	@ClientString(id = 1353, message = "You can set the option to show your status as always being off-line to all of your contacts.")
	public static SystemMessageId YOU_CAN_SET_THE_OPTION_TO_SHOW_YOUR_STATUS_AS_ALWAYS_BEING_OFF_LINE_TO_ALL_OF_YOUR_CONTACTS;
	
	@ClientString(id = 1354, message = "You are not allowed to chat with a contact while a chatting block is imposed.")
	public static SystemMessageId YOU_ARE_NOT_ALLOWED_TO_CHAT_WITH_A_CONTACT_WHILE_A_CHATTING_BLOCK_IS_IMPOSED;
	
	@ClientString(id = 1355, message = "That contact is currently blocked from chatting.")
	public static SystemMessageId THAT_CONTACT_IS_CURRENTLY_BLOCKED_FROM_CHATTING;
	
	@ClientString(id = 1356, message = "That contact is not currently logged in.")
	public static SystemMessageId THAT_CONTACT_IS_NOT_CURRENTLY_LOGGED_IN;
	
	@ClientString(id = 1357, message = "You have been blocked from chatting with that contact.")
	public static SystemMessageId YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT;
	
	@ClientString(id = 1358, message = "You are being logged out...")
	public static SystemMessageId YOU_ARE_BEING_LOGGED_OUT;
	
	@ClientString(id = 1359, message = "$s1 has logged in.")
	public static SystemMessageId S1_HAS_LOGGED_IN_2;
	
	@ClientString(id = 1360, message = "You have received a message from $s1.")
	public static SystemMessageId YOU_HAVE_RECEIVED_A_MESSAGE_FROM_S1;
	
	@ClientString(id = 1361, message = "Due to a system error, you have been logged out of the .NET Messenger Service.")
	public static SystemMessageId DUE_TO_A_SYSTEM_ERROR_YOU_HAVE_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE;
	
	@ClientString(id = 1362, message = "Please select the contact you wish to delete. If you would like to delete a group, click the button next to My Status, and then use the Options menu.")
	public static SystemMessageId PLEASE_SELECT_THE_CONTACT_YOU_WISH_TO_DELETE_IF_YOU_WOULD_LIKE_TO_DELETE_A_GROUP_CLICK_THE_BUTTON_NEXT_TO_MY_STATUS_AND_THEN_USE_THE_OPTIONS_MENU;
	
	@ClientString(id = 1363, message = "Your request to participate in the alliance war has been denied.")
	public static SystemMessageId YOUR_REQUEST_TO_PARTICIPATE_IN_THE_ALLIANCE_WAR_HAS_BEEN_DENIED;
	
	@ClientString(id = 1364, message = "The request for an alliance war has been rejected.")
	public static SystemMessageId THE_REQUEST_FOR_AN_ALLIANCE_WAR_HAS_BEEN_REJECTED;
	
	@ClientString(id = 1365, message = "$s2 of $s1 clan has surrendered as an individual.")
	public static SystemMessageId S2_OF_S1_CLAN_HAS_SURRENDERED_AS_AN_INDIVIDUAL;
	
	@ClientString(id = 1366, message = "In order to delete a group, you must not have any contacts listed under that group. Please transfer your contact(s) to another group before continuing with deletion.")
	public static SystemMessageId IN_ORDER_TO_DELETE_A_GROUP_YOU_MUST_NOT_HAVE_ANY_CONTACTS_LISTED_UNDER_THAT_GROUP_PLEASE_TRANSFER_YOUR_CONTACT_S_TO_ANOTHER_GROUP_BEFORE_CONTINUING_WITH_DELETION;
	
	@ClientString(id = 1367, message = "Only members of the group are allowed to add records.")
	public static SystemMessageId ONLY_MEMBERS_OF_THE_GROUP_ARE_ALLOWED_TO_ADD_RECORDS;
	
	@ClientString(id = 1368, message = "You can not try those items on at the same time.")
	public static SystemMessageId YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME;
	
	@ClientString(id = 1369, message = "You've exceeded the maximum.")
	public static SystemMessageId YOU_VE_EXCEEDED_THE_MAXIMUM;
	
	@ClientString(id = 1370, message = "Your message to $s1 did not reach it's recipient. You cannot send mail to the GM staff.")
	public static SystemMessageId YOUR_MESSAGE_TO_S1_DID_NOT_REACH_IT_S_RECIPIENT_YOU_CANNOT_SEND_MAIL_TO_THE_GM_STAFF;
	
	@ClientString(id = 1371, message = "It has been determined that you're not engaged in normal gameplay and a restriction has been imposed upon you. You may not move for $s1 minutes.")
	public static SystemMessageId IT_HAS_BEEN_DETERMINED_THAT_YOU_RE_NOT_ENGAGED_IN_NORMAL_GAMEPLAY_AND_A_RESTRICTION_HAS_BEEN_IMPOSED_UPON_YOU_YOU_MAY_NOT_MOVE_FOR_S1_MINUTES;
	
	@ClientString(id = 1372, message = "Your punishment will continue for $s1 minutes.")
	public static SystemMessageId YOUR_PUNISHMENT_WILL_CONTINUE_FOR_S1_MINUTES;
	
	@ClientString(id = 1373, message = "$s1 has picked up $s2 that was dropped by a Raid Boss.")
	public static SystemMessageId S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_A_RAID_BOSS;
	
	@ClientString(id = 1374, message = "$s1 has picked up $s3 $s2(s) that was dropped by a Raid Boss.")
	public static SystemMessageId S1_HAS_PICKED_UP_S3_S2_S_THAT_WAS_DROPPED_BY_A_RAID_BOSS;
	
	@ClientString(id = 1375, message = "$s1 has picked up $s2 adena that was dropped by a Raid Boss.")
	public static SystemMessageId S1_HAS_PICKED_UP_S2_ADENA_THAT_WAS_DROPPED_BY_A_RAID_BOSS;
	
	@ClientString(id = 1376, message = "$s1 has picked up $s2 that was dropped by another character.")
	public static SystemMessageId S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER;
	
	@ClientString(id = 1377, message = "$s1 has picked up $s3 $s2(s) that was dropped by another character.")
	public static SystemMessageId S1_HAS_PICKED_UP_S3_S2_S_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER;
	
	@ClientString(id = 1378, message = "$s1 has picked up +$s3$s2 that was dropped by another character.")
	public static SystemMessageId S1_HAS_PICKED_UP_S3_S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER;
	
	@ClientString(id = 1379, message = "$s1 has obtained $s2 adena.")
	public static SystemMessageId S1_HAS_OBTAINED_S2_ADENA;
	
	@ClientString(id = 1380, message = "You can't summon a $s1 while on the battleground.")
	public static SystemMessageId YOU_CAN_T_SUMMON_A_S1_WHILE_ON_THE_BATTLEGROUND;
	
	@ClientString(id = 1381, message = "The party leader has obtained $s2 of $s1.")
	public static SystemMessageId THE_PARTY_LEADER_HAS_OBTAINED_S2_OF_S1;
	
	@ClientString(id = 1382, message = "To fulfill the quest, you must bring the chosen weapon. Are you sure you want to choose this weapon?")
	public static SystemMessageId TO_FULFILL_THE_QUEST_YOU_MUST_BRING_THE_CHOSEN_WEAPON_ARE_YOU_SURE_YOU_WANT_TO_CHOOSE_THIS_WEAPON;
	
	@ClientString(id = 1383, message = "Are you sure you want to exchange?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_EXCHANGE;
	
	@ClientString(id = 1384, message = "$s1 has become the party leader.")
	public static SystemMessageId S1_HAS_BECOME_THE_PARTY_LEADER;
	
	@ClientString(id = 1385, message = "You are not allowed to dismount at this location.")
	public static SystemMessageId YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION;
	
	@ClientString(id = 1386, message = "You are no longer held in place.")
	public static SystemMessageId YOU_ARE_NO_LONGER_HELD_IN_PLACE;
	
	@ClientString(id = 1387, message = "Please select the item you would like to try on.")
	public static SystemMessageId PLEASE_SELECT_THE_ITEM_YOU_WOULD_LIKE_TO_TRY_ON;
	
	@ClientString(id = 1388, message = "A party room has been created.")
	public static SystemMessageId A_PARTY_ROOM_HAS_BEEN_CREATED;
	
	@ClientString(id = 1389, message = "The party room's information has been revised.")
	public static SystemMessageId THE_PARTY_ROOM_S_INFORMATION_HAS_BEEN_REVISED;
	
	@ClientString(id = 1390, message = "You are not allowed to enter the party room.")
	public static SystemMessageId YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM;
	
	@ClientString(id = 1391, message = "You have exited from the party room.")
	public static SystemMessageId YOU_HAVE_EXITED_FROM_THE_PARTY_ROOM;
	
	@ClientString(id = 1392, message = "$s1 has left the party room.")
	public static SystemMessageId S1_HAS_LEFT_THE_PARTY_ROOM;
	
	@ClientString(id = 1393, message = "You have been ousted from the party room.")
	public static SystemMessageId YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM;
	
	@ClientString(id = 1394, message = "$s1 has been ousted from the party room.")
	public static SystemMessageId S1_HAS_BEEN_OUSTED_FROM_THE_PARTY_ROOM;
	
	@ClientString(id = 1395, message = "The party room has been disbanded.")
	public static SystemMessageId THE_PARTY_ROOM_HAS_BEEN_DISBANDED;
	
	@ClientString(id = 1396, message = "The list of party rooms can only be viewed by a person who has not joined a party or who is currently the leader of a party.")
	public static SystemMessageId THE_LIST_OF_PARTY_ROOMS_CAN_ONLY_BE_VIEWED_BY_A_PERSON_WHO_HAS_NOT_JOINED_A_PARTY_OR_WHO_IS_CURRENTLY_THE_LEADER_OF_A_PARTY;
	
	@ClientString(id = 1397, message = "The leader of the party room has changed.")
	public static SystemMessageId THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED;
	
	@ClientString(id = 1398, message = "We are recruiting party members.")
	public static SystemMessageId WE_ARE_RECRUITING_PARTY_MEMBERS;
	
	@ClientString(id = 1399, message = "Only the leader of the party can transfer party leadership to another player.")
	public static SystemMessageId ONLY_THE_LEADER_OF_THE_PARTY_CAN_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_PLAYER;
	
	@ClientString(id = 1400, message = "Please select the person you wish to make the party leader.")
	public static SystemMessageId PLEASE_SELECT_THE_PERSON_YOU_WISH_TO_MAKE_THE_PARTY_LEADER;
	
	@ClientString(id = 1401, message = "Slow down, you are already the party leader.")
	public static SystemMessageId SLOW_DOWN_YOU_ARE_ALREADY_THE_PARTY_LEADER;
	
	@ClientString(id = 1402, message = "You may only transfer party leadership to another member of the party.")
	public static SystemMessageId YOU_MAY_ONLY_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_MEMBER_OF_THE_PARTY;
	
	@ClientString(id = 1403, message = "You have failed to transfer the party leadership.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADERSHIP;
	
	@ClientString(id = 1404, message = "The owner of the private manufacturing store has changed the price for creating this item. Please check the new price before trying again.")
	public static SystemMessageId THE_OWNER_OF_THE_PRIVATE_MANUFACTURING_STORE_HAS_CHANGED_THE_PRICE_FOR_CREATING_THIS_ITEM_PLEASE_CHECK_THE_NEW_PRICE_BEFORE_TRYING_AGAIN;
	
	@ClientString(id = 1405, message = "$s1 CPs have been restored.")
	public static SystemMessageId S1_CPS_HAVE_BEEN_RESTORED;
	
	@ClientString(id = 1406, message = "$s1 restores $s2 CP.")
	public static SystemMessageId S1_RESTORES_S2_CP;
	
	@ClientString(id = 1407, message = "You are using a computer that does not allow you to log in with two accounts at the same time.")
	public static SystemMessageId YOU_ARE_USING_A_COMPUTER_THAT_DOES_NOT_ALLOW_YOU_TO_LOG_IN_WITH_TWO_ACCOUNTS_AT_THE_SAME_TIME;
	
	@ClientString(id = 1408, message = "Your prepaid remaining usage time is $s1 hours and $s2 minutes. You have $s3 paid reservations left.")
	public static SystemMessageId YOUR_PREPAID_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES_YOU_HAVE_S3_PAID_RESERVATIONS_LEFT;
	
	@ClientString(id = 1409, message = "Your prepaid usage time has expired. Your new prepaid reservation will be used. The remaining usage time is $s1 hours and $s2 minutes.")
	public static SystemMessageId YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOUR_NEW_PREPAID_RESERVATION_WILL_BE_USED_THE_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES;
	
	@ClientString(id = 1410, message = "Your prepaid usage time has expired. You do not have any more prepaid reservations left.")
	public static SystemMessageId YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOU_DO_NOT_HAVE_ANY_MORE_PREPAID_RESERVATIONS_LEFT;
	
	@ClientString(id = 1411, message = "The number of your prepaid reservations has changed.")
	public static SystemMessageId THE_NUMBER_OF_YOUR_PREPAID_RESERVATIONS_HAS_CHANGED;
	
	@ClientString(id = 1412, message = "Your prepaid usage time has $s1 minutes left.")
	public static SystemMessageId YOUR_PREPAID_USAGE_TIME_HAS_S1_MINUTES_LEFT;
	
	@ClientString(id = 1413, message = "You do not meet the requirements to enter that party room.")
	public static SystemMessageId YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM;
	
	@ClientString(id = 1414, message = "The width and length should be 100 or more grids and less than 5000 grids respectively.")
	public static SystemMessageId THE_WIDTH_AND_LENGTH_SHOULD_BE_100_OR_MORE_GRIDS_AND_LESS_THAN_5000_GRIDS_RESPECTIVELY;
	
	@ClientString(id = 1415, message = "The command file is not set.")
	public static SystemMessageId THE_COMMAND_FILE_IS_NOT_SET;
	
	@ClientString(id = 1416, message = "The party representative of Team 1 has not been selected.")
	public static SystemMessageId THE_PARTY_REPRESENTATIVE_OF_TEAM_1_HAS_NOT_BEEN_SELECTED;
	
	@ClientString(id = 1417, message = "The party representative of Team 2 has not been selected.")
	public static SystemMessageId THE_PARTY_REPRESENTATIVE_OF_TEAM_2_HAS_NOT_BEEN_SELECTED;
	
	@ClientString(id = 1418, message = "The name of Team 1 has not yet been chosen.")
	public static SystemMessageId THE_NAME_OF_TEAM_1_HAS_NOT_YET_BEEN_CHOSEN;
	
	@ClientString(id = 1419, message = "The name of Team 2 has not yet been chosen.")
	public static SystemMessageId THE_NAME_OF_TEAM_2_HAS_NOT_YET_BEEN_CHOSEN;
	
	@ClientString(id = 1420, message = "The name of Team 1 and the name of Team 2 are identical.")
	public static SystemMessageId THE_NAME_OF_TEAM_1_AND_THE_NAME_OF_TEAM_2_ARE_IDENTICAL;
	
	@ClientString(id = 1421, message = "The race setup file has not been designated.")
	public static SystemMessageId THE_RACE_SETUP_FILE_HAS_NOT_BEEN_DESIGNATED;
	
	@ClientString(id = 1422, message = "Race setup file error - BuffCnt is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_BUFFCNT_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1423, message = "Race setup file error - BuffID$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_BUFFID_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1424, message = "Race setup file error - BuffLv$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_BUFFLV_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1425, message = "Race setup file error - DefaultAllow is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_DEFAULTALLOW_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1426, message = "Race setup file error - ExpSkillCnt is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPSKILLCNT_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1427, message = "Race setup file error - ExpSkillID$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPSKILLID_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1428, message = "Race setup file error - ExpItemCnt is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPITEMCNT_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1429, message = "Race setup file error - ExpItemID$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPITEMID_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1430, message = "Race setup file error - TeleportDelay is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_TELEPORTDELAY_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1431, message = "The race will be stopped temporarily.")
	public static SystemMessageId THE_RACE_WILL_BE_STOPPED_TEMPORARILY;
	
	@ClientString(id = 1432, message = "Your opponent is currently in a petrified state.")
	public static SystemMessageId YOUR_OPPONENT_IS_CURRENTLY_IN_A_PETRIFIED_STATE;
	
	@ClientString(id = 1433, message = "The automatic use of $s1 has been activated.")
	public static SystemMessageId THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 1434, message = "The automatic use of $s1 has been deactivated.")
	public static SystemMessageId THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED;
	
	@ClientString(id = 1435, message = "Due to insufficient $s1, the automatic use function has been deactivated.")
	public static SystemMessageId DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_HAS_BEEN_DEACTIVATED;
	
	@ClientString(id = 1436, message = "Due to insufficient $s1, the automatic use function cannot be activated.")
	public static SystemMessageId DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_CANNOT_BE_ACTIVATED;
	
	@ClientString(id = 1437, message = "Players are no longer allowed to play dice. Dice can no longer be purchased from a village store. However, you can still sell them to any village store.")
	public static SystemMessageId PLAYERS_ARE_NO_LONGER_ALLOWED_TO_PLAY_DICE_DICE_CAN_NO_LONGER_BE_PURCHASED_FROM_A_VILLAGE_STORE_HOWEVER_YOU_CAN_STILL_SELL_THEM_TO_ANY_VILLAGE_STORE;
	
	@ClientString(id = 1438, message = "There is no skill that enables enchant.")
	public static SystemMessageId THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
	
	@ClientString(id = 1439, message = "You do not have all of the items needed to enchant that skill.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL;
	
	@ClientString(id = 1440, message = "Skill enchant was successful! $s1 has been enchanted.")
	public static SystemMessageId SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED;
	
	@ClientString(id = 1441, message = "Skill enchant failed. The skill will be initialized.")
	public static SystemMessageId SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED;
	
	@ClientString(id = 1442, message = "Remaining Time: $s1 second(s)")
	public static SystemMessageId REMAINING_TIME_S1_SECOND_S;
	
	@ClientString(id = 1443, message = "You do not have enough SP to enchant that skill.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL;
	
	@ClientString(id = 1444, message = "You do not have enough experience (Exp) to enchant that skill.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_EXPERIENCE_EXP_TO_ENCHANT_THAT_SKILL;
	
	@ClientString(id = 1445, message = "Your previous subclass will be removed and replaced with the new subclass at level 40. Do you wish to continue?")
	public static SystemMessageId YOUR_PREVIOUS_SUBCLASS_WILL_BE_REMOVED_AND_REPLACED_WITH_THE_NEW_SUBCLASS_AT_LEVEL_40_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 1446, message = "The ferry from $s1 to $s2 has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_S1_TO_S2_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1447, message = "You cannot do that while fishing.")
	public static SystemMessageId YOU_CANNOT_DO_THAT_WHILE_FISHING;
	
	@ClientString(id = 1448, message = "Only fishing skills may be used at this time.")
	public static SystemMessageId ONLY_FISHING_SKILLS_MAY_BE_USED_AT_THIS_TIME;
	
	@ClientString(id = 1449, message = "You've got a bite!")
	public static SystemMessageId YOU_VE_GOT_A_BITE;
	
	@ClientString(id = 1450, message = "That fish is more determined than you are - it spit the hook!")
	public static SystemMessageId THAT_FISH_IS_MORE_DETERMINED_THAN_YOU_ARE_IT_SPIT_THE_HOOK;
	
	@ClientString(id = 1451, message = "Your bait was stolen by that fish!")
	public static SystemMessageId YOUR_BAIT_WAS_STOLEN_BY_THAT_FISH;
	
	@ClientString(id = 1452, message = "Baits have been lost because the fish got away.")
	public static SystemMessageId BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY;
	
	@ClientString(id = 1453, message = "You do not have a fishing pole equipped.")
	public static SystemMessageId YOU_DO_NOT_HAVE_A_FISHING_POLE_EQUIPPED;
	
	@ClientString(id = 1454, message = "You must put bait on your hook before you can fish.")
	public static SystemMessageId YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH;
	
	@ClientString(id = 1455, message = "You cannot fish while under water.")
	public static SystemMessageId YOU_CANNOT_FISH_WHILE_UNDER_WATER;
	
	@ClientString(id = 1456, message = "You cannot fish while riding as a passenger of a boat - it's against the rules.")
	public static SystemMessageId YOU_CANNOT_FISH_WHILE_RIDING_AS_A_PASSENGER_OF_A_BOAT_IT_S_AGAINST_THE_RULES;
	
	@ClientString(id = 1457, message = "You can't fish here.")
	public static SystemMessageId YOU_CAN_T_FISH_HERE;
	
	@ClientString(id = 1458, message = "Your attempt at fishing has been cancelled.")
	public static SystemMessageId YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 1459, message = "You do not have enough bait.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_BAIT;
	
	@ClientString(id = 1460, message = "You reel your line in and stop fishing.")
	public static SystemMessageId YOU_REEL_YOUR_LINE_IN_AND_STOP_FISHING;
	
	@ClientString(id = 1461, message = "You cast your line and start to fish.")
	public static SystemMessageId YOU_CAST_YOUR_LINE_AND_START_TO_FISH;
	
	@ClientString(id = 1462, message = "You may only use the Pumping skill while you are fishing.")
	public static SystemMessageId YOU_MAY_ONLY_USE_THE_PUMPING_SKILL_WHILE_YOU_ARE_FISHING;
	
	@ClientString(id = 1463, message = "You may only use the Reeling skill while you are fishing.")
	public static SystemMessageId YOU_MAY_ONLY_USE_THE_REELING_SKILL_WHILE_YOU_ARE_FISHING;
	
	@ClientString(id = 1464, message = "The fish has resisted your attempt to bring it in.")
	public static SystemMessageId THE_FISH_HAS_RESISTED_YOUR_ATTEMPT_TO_BRING_IT_IN;
	
	@ClientString(id = 1465, message = "Your pumping is successful, causing $s1 damage.")
	public static SystemMessageId YOUR_PUMPING_IS_SUCCESSFUL_CAUSING_S1_DAMAGE;
	
	@ClientString(id = 1466, message = "You failed to do anything with the fish and it regains $s1 HP.")
	public static SystemMessageId YOU_FAILED_TO_DO_ANYTHING_WITH_THE_FISH_AND_IT_REGAINS_S1_HP;
	
	@ClientString(id = 1467, message = "You reel that fish in closer and cause $s1 damage.")
	public static SystemMessageId YOU_REEL_THAT_FISH_IN_CLOSER_AND_CAUSE_S1_DAMAGE;
	
	@ClientString(id = 1468, message = "You failed to reel that fish in further and it regains $s1 HP.")
	public static SystemMessageId YOU_FAILED_TO_REEL_THAT_FISH_IN_FURTHER_AND_IT_REGAINS_S1_HP;
	
	@ClientString(id = 1469, message = "You caught something!")
	public static SystemMessageId YOU_CAUGHT_SOMETHING;
	
	@ClientString(id = 1470, message = "You cannot do that while fishing.")
	public static SystemMessageId YOU_CANNOT_DO_THAT_WHILE_FISHING_2;
	
	@ClientString(id = 1471, message = "You cannot do that while fishing.")
	public static SystemMessageId YOU_CANNOT_DO_THAT_WHILE_FISHING_3;
	
	@ClientString(id = 1472, message = "You look oddly at the fishing pole in disbelief and realize that you can't attack anything with this.")
	public static SystemMessageId YOU_LOOK_ODDLY_AT_THE_FISHING_POLE_IN_DISBELIEF_AND_REALIZE_THAT_YOU_CAN_T_ATTACK_ANYTHING_WITH_THIS;
	
	@ClientString(id = 1473, message = "$s1 is not sufficient.")
	public static SystemMessageId S1_IS_NOT_SUFFICIENT;
	
	@ClientString(id = 1474, message = "$s1 is not available.")
	public static SystemMessageId S1_IS_NOT_AVAILABLE;
	
	@ClientString(id = 1475, message = "Pet has dropped $s1.")
	public static SystemMessageId PET_HAS_DROPPED_S1;
	
	@ClientString(id = 1476, message = "Pet has dropped +$s1$s2.")
	public static SystemMessageId PET_HAS_DROPPED_S1_S2;
	
	@ClientString(id = 1477, message = "Pet has dropped $s2 of $s1.")
	public static SystemMessageId PET_HAS_DROPPED_S2_OF_S1;
	
	@ClientString(id = 1478, message = "You may only register a 64 x 64 pixel, 256-color BMP.")
	public static SystemMessageId YOU_MAY_ONLY_REGISTER_A_64_X_64_PIXEL_256_COLOR_BMP;
	
	@ClientString(id = 1479, message = "That is the wrong grade of soulshot for that fishing pole.")
	public static SystemMessageId THAT_IS_THE_WRONG_GRADE_OF_SOULSHOT_FOR_THAT_FISHING_POLE;
	
	@ClientString(id = 1480, message = "Are you sure you wish to remove yourself from the Grand Olympiad Games waiting list?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WISH_TO_REMOVE_YOURSELF_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST;
	
	@ClientString(id = 1481, message = "You've selected to join a non-class specific game. Continue?")
	public static SystemMessageId YOU_VE_SELECTED_TO_JOIN_A_NON_CLASS_SPECIFIC_GAME_CONTINUE;
	
	@ClientString(id = 1482, message = "You've selected to join a class specific game. Continue?")
	public static SystemMessageId YOU_VE_SELECTED_TO_JOIN_A_CLASS_SPECIFIC_GAME_CONTINUE;
	
	@ClientString(id = 1483, message = "Are you ready to be a Hero?")
	public static SystemMessageId ARE_YOU_READY_TO_BE_A_HERO;
	
	@ClientString(id = 1484, message = "Are you sure this is the Hero weapon you wish to use?")
	public static SystemMessageId ARE_YOU_SURE_THIS_IS_THE_HERO_WEAPON_YOU_WISH_TO_USE;
	
	@ClientString(id = 1485, message = "The ferry from Talking Island to Gludin Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1486, message = "The ferry from Gludin Harbor to Talking Island has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1487, message = "The ferry from Giran Harbor to Talking Island has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1488, message = "The ferry from Talking Island to Giran Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_TO_GIRAN_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1489, message = "Innadril cruise service has been delayed.")
	public static SystemMessageId INNADRIL_CRUISE_SERVICE_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1490, message = "Traded $s2 of crop $s1.")
	public static SystemMessageId TRADED_S2_OF_CROP_S1;
	
	@ClientString(id = 1491, message = "Failed in trading $s2 of crop $s1.")
	public static SystemMessageId FAILED_IN_TRADING_S2_OF_CROP_S1;
	
	@ClientString(id = 1492, message = "You will be moved to the Olympiad Stadium in $s1 second(s).")
	public static SystemMessageId YOU_WILL_BE_MOVED_TO_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S;
	
	@ClientString(id = 1493, message = "Your opponent made haste with their tail between their legs; the match has been cancelled.")
	public static SystemMessageId YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 1494, message = "Your opponent does not meet the requirements to do battle; the match has been cancelled.")
	public static SystemMessageId YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 1495, message = "The Grand Olympiad match will start in $s1 second(s).")
	public static SystemMessageId THE_GRAND_OLYMPIAD_MATCH_WILL_START_IN_S1_SECOND_S;
	
	@ClientString(id = 1496, message = "The match has started, fight!")
	public static SystemMessageId THE_MATCH_HAS_STARTED_FIGHT;
	
	@ClientString(id = 1497, message = "Congratulations $s1, you win the match!")
	public static SystemMessageId CONGRATULATIONS_S1_YOU_WIN_THE_MATCH;
	
	@ClientString(id = 1498, message = "There is no victor; the match ends in a tie.")
	public static SystemMessageId THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE;
	
	@ClientString(id = 1499, message = "You will be moved back to town in $s1 second(s).")
	public static SystemMessageId YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECOND_S;
	
	@ClientString(id = 1500, message = "You cannot participate in the Grand Olympiad Games with a character in their subclass.")
	public static SystemMessageId YOU_CANNOT_PARTICIPATE_IN_THE_GRAND_OLYMPIAD_GAMES_WITH_A_CHARACTER_IN_THEIR_SUBCLASS;
	
	@ClientString(id = 1501, message = "Only Noblesse can participate in the Olympiad.")
	public static SystemMessageId ONLY_NOBLESSE_CAN_PARTICIPATE_IN_THE_OLYMPIAD;
	
	@ClientString(id = 1502, message = "You have already been registered in a waiting list of an event.")
	public static SystemMessageId YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT;
	
	@ClientString(id = 1503, message = "You have been registered in the Grand Olympiad Games waiting list for a class specific match.")
	public static SystemMessageId YOU_HAVE_BEEN_REGISTERED_IN_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH;
	
	@ClientString(id = 1504, message = "You have been registered in the Grand Olympiad Games waiting list for a non-class specific match.")
	public static SystemMessageId YOU_HAVE_BEEN_REGISTERED_IN_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST_FOR_A_NON_CLASS_SPECIFIC_MATCH;
	
	@ClientString(id = 1505, message = "You have been removed from the Grand Olympiad Games waiting list.")
	public static SystemMessageId YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST;
	
	@ClientString(id = 1506, message = "You are not currently registered on any Grand Olympiad Games waiting list.")
	public static SystemMessageId YOU_ARE_NOT_CURRENTLY_REGISTERED_ON_ANY_GRAND_OLYMPIAD_GAMES_WAITING_LIST;
	
	@ClientString(id = 1507, message = "You cannot equip that item in a Grand Olympiad Games match.")
	public static SystemMessageId YOU_CANNOT_EQUIP_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH;
	
	@ClientString(id = 1508, message = "You cannot use that item in a Grand Olympiad Games match.")
	public static SystemMessageId YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH;
	
	@ClientString(id = 1509, message = "You cannot use that skill in a Grand Olympiad Games match.")
	public static SystemMessageId YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_GAMES_MATCH;
	
	@ClientString(id = 1510, message = "$s1 is making an attempt at resurrection. Do you want to continue with this resurrection?")
	public static SystemMessageId S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION;
	
	@ClientString(id = 1511, message = "While a pet is attempting to resurrect, it cannot help in resurrecting its master.")
	public static SystemMessageId WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER;
	
	@ClientString(id = 1512, message = "You cannot resurrect a pet while their owner is being resurrected.")
	public static SystemMessageId YOU_CANNOT_RESURRECT_A_PET_WHILE_THEIR_OWNER_IS_BEING_RESURRECTED;
	
	@ClientString(id = 1513, message = "Resurrection has already been proposed.")
	public static SystemMessageId RESURRECTION_HAS_ALREADY_BEEN_PROPOSED;
	
	@ClientString(id = 1514, message = "You cannot resurrect the owner of a pet while their pet is being resurrected.")
	public static SystemMessageId YOU_CANNOT_RESURRECT_THE_OWNER_OF_A_PET_WHILE_THEIR_PET_IS_BEING_RESURRECTED;
	
	@ClientString(id = 1515, message = "A pet cannot be resurrected while it's owner is in the process of resurrecting.")
	public static SystemMessageId A_PET_CANNOT_BE_RESURRECTED_WHILE_IT_S_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING;
	
	@ClientString(id = 1516, message = "The target is unavailable for seeding.")
	public static SystemMessageId THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING;
	
	@ClientString(id = 1517, message = "Failed in Blessed Enchant. The enchant value of the item became 0.")
	public static SystemMessageId FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0;
	
	@ClientString(id = 1518, message = "You do not meet the required condition to equip that item.")
	public static SystemMessageId YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
	
	@ClientString(id = 1519, message = "Your pet has been killed! Make sure you resurrect your pet within 20 minutes or your pet and all of it's items will disappear forever!")
	public static SystemMessageId YOUR_PET_HAS_BEEN_KILLED_MAKE_SURE_YOU_RESURRECT_YOUR_PET_WITHIN_20_MINUTES_OR_YOUR_PET_AND_ALL_OF_IT_S_ITEMS_WILL_DISAPPEAR_FOREVER;
	
	@ClientString(id = 1520, message = "Servitor passed away.")
	public static SystemMessageId SERVITOR_PASSED_AWAY;
	
	@ClientString(id = 1521, message = "Your servitor has vanished! You'll need to summon a new one.")
	public static SystemMessageId YOUR_SERVITOR_HAS_VANISHED_YOU_LL_NEED_TO_SUMMON_A_NEW_ONE;
	
	@ClientString(id = 1522, message = "Your pet's corpse has decayed!")
	public static SystemMessageId YOUR_PET_S_CORPSE_HAS_DECAYED;
	
	@ClientString(id = 1523, message = "You should release your pet or servitor so that it does not fall off of the boat and drown!")
	public static SystemMessageId YOU_SHOULD_RELEASE_YOUR_PET_OR_SERVITOR_SO_THAT_IT_DOES_NOT_FALL_OFF_OF_THE_BOAT_AND_DROWN;
	
	@ClientString(id = 1524, message = "$s1's pet gained $s2.")
	public static SystemMessageId S1_S_PET_GAINED_S2;
	
	@ClientString(id = 1525, message = "$s1's pet gained $s3 of $s2.")
	public static SystemMessageId S1_S_PET_GAINED_S3_OF_S2;
	
	@ClientString(id = 1526, message = "$s1's pet gained +$s2$s3.")
	public static SystemMessageId S1_S_PET_GAINED_S2_S3;
	
	@ClientString(id = 1527, message = "Your pet was hungry so it ate $s1.")
	public static SystemMessageId YOUR_PET_WAS_HUNGRY_SO_IT_ATE_S1;
	
	@ClientString(id = 1528, message = "You've sent a petition to the GM staff.")
	public static SystemMessageId YOU_VE_SENT_A_PETITION_TO_THE_GM_STAFF;
	
	@ClientString(id = 1529, message = "$s1 has invited you to join a Command Channel. Do you wish to accept?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_JOIN_A_COMMAND_CHANNEL_DO_YOU_WISH_TO_ACCEPT;
	
	@ClientString(id = 1530, message = "Select a target or enter the name.")
	public static SystemMessageId SELECT_A_TARGET_OR_ENTER_THE_NAME;
	
	@ClientString(id = 1531, message = "Enter the name of the clan that you wish to declare war on.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_CLAN_THAT_YOU_WISH_TO_DECLARE_WAR_ON;
	
	@ClientString(id = 1532, message = "Enter the name of the clan that you wish to have a cease-fire with.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_CLAN_THAT_YOU_WISH_TO_HAVE_A_CEASE_FIRE_WITH;
	
	@ClientString(id = 1533, message = "Attention: $s1 picked up $s2.")
	public static SystemMessageId ATTENTION_S1_PICKED_UP_S2;
	
	@ClientString(id = 1534, message = "Attention: $s1 picked up +$s2 $s3.")
	public static SystemMessageId ATTENTION_S1_PICKED_UP_S2_S3;
	
	@ClientString(id = 1535, message = "Attention: $s1's pet picked up $s2.")
	public static SystemMessageId ATTENTION_S1_S_PET_PICKED_UP_S2;
	
	@ClientString(id = 1536, message = "Attention: $s1's pet picked up +$s2 $s3.")
	public static SystemMessageId ATTENTION_S1_S_PET_PICKED_UP_S2_S3;
	
	@ClientString(id = 1537, message = "Current Location: $s1, $s2, $s3 (near Rune Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_RUNE_VILLAGE;
	
	@ClientString(id = 1538, message = "Current Location: $s1, $s2, $s3 (near the Town of Goddard)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GODDARD;
	
	@ClientString(id = 1539, message = "Cargo has arrived at Talking Island Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_TALKING_ISLAND_VILLAGE;
	
	@ClientString(id = 1540, message = "Cargo has arrived at the Dark Elf Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_THE_DARK_ELF_VILLAGE;
	
	@ClientString(id = 1541, message = "Cargo has arrived at Elven Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_ELVEN_VILLAGE;
	
	@ClientString(id = 1542, message = "Cargo has arrived at Orc Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_ORC_VILLAGE;
	
	@ClientString(id = 1543, message = "Cargo has arrived at Dwarven Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_DWARVEN_VILLAGE;
	
	@ClientString(id = 1544, message = "Cargo has arrived at Aden Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_ADEN_CASTLE_TOWN;
	
	@ClientString(id = 1545, message = "Cargo has arrived at the Town of Oren.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_OREN;
	
	@ClientString(id = 1546, message = "Cargo has arrived at Hunters Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_HUNTERS_VILLAGE;
	
	@ClientString(id = 1547, message = "Cargo has arrived at the Town of Dion.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_DION;
	
	@ClientString(id = 1548, message = "Cargo has arrived at Floran Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_FLORAN_VILLAGE;
	
	@ClientString(id = 1549, message = "Cargo has arrived at Gludin Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_GLUDIN_VILLAGE;
	
	@ClientString(id = 1550, message = "Cargo has arrived at the Town of Gludio.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_GLUDIO;
	
	@ClientString(id = 1551, message = "Cargo has arrived at Giran Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_GIRAN_CASTLE_TOWN;
	
	@ClientString(id = 1552, message = "Cargo has arrived at Heine.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_HEINE;
	
	@ClientString(id = 1553, message = "Cargo has arrived at Rune Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_RUNE_VILLAGE;
	
	@ClientString(id = 1554, message = "Cargo has arrived at the Town of Goddard.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_GODDARD;
	
	@ClientString(id = 1555, message = "Do you want to cancel character deletion?")
	public static SystemMessageId DO_YOU_WANT_TO_CANCEL_CHARACTER_DELETION;
	
	@ClientString(id = 1556, message = "Your clan notice has been saved.")
	public static SystemMessageId YOUR_CLAN_NOTICE_HAS_BEEN_SAVED;
	
	@ClientString(id = 1557, message = "Seed price should be more than $s1 and less than $s2.")
	public static SystemMessageId SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1558, message = "The quantity of seed should be more than $s1 and less than $s2.")
	public static SystemMessageId THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1559, message = "Crop price should be more than $s1 and less than $s2.")
	public static SystemMessageId CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1560, message = "The quantity of crop should be more than $s1 and less than $s2 .")
	public static SystemMessageId THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1561, message = "The clan, $s1, has declared a Clan War.")
	public static SystemMessageId THE_CLAN_S1_HAS_DECLARED_A_CLAN_WAR;
	
	@ClientString(id = 1562, message = "A Clan War has been declared against the clan, $s1. If you are killed during the Clan War by members of the opposing clan, you will only lose a quarter of the normal experience from death.")
	public static SystemMessageId A_CLAN_WAR_HAS_BEEN_DECLARED_AGAINST_THE_CLAN_S1_IF_YOU_ARE_KILLED_DURING_THE_CLAN_WAR_BY_MEMBERS_OF_THE_OPPOSING_CLAN_YOU_WILL_ONLY_LOSE_A_QUARTER_OF_THE_NORMAL_EXPERIENCE_FROM_DEATH;
	
	@ClientString(id = 1563, message = "The clan, $s1, cannot declare a Clan War because their clan is less than level three, and or they do not have enough members.")
	public static SystemMessageId THE_CLAN_S1_CANNOT_DECLARE_A_CLAN_WAR_BECAUSE_THEIR_CLAN_IS_LESS_THAN_LEVEL_THREE_AND_OR_THEY_DO_NOT_HAVE_ENOUGH_MEMBERS;
	
	@ClientString(id = 1564, message = "A Clan War can be declared only if the clan is level three or above, and the number of clan members is fifteen or greater.")
	public static SystemMessageId A_CLAN_WAR_CAN_BE_DECLARED_ONLY_IF_THE_CLAN_IS_LEVEL_THREE_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER;
	
	@ClientString(id = 1565, message = "A Clan War cannot be declared against a clan that does not exist!")
	public static SystemMessageId A_CLAN_WAR_CANNOT_BE_DECLARED_AGAINST_A_CLAN_THAT_DOES_NOT_EXIST;
	
	@ClientString(id = 1566, message = "The clan, $s1, has decided to stop the war.")
	public static SystemMessageId THE_CLAN_S1_HAS_DECIDED_TO_STOP_THE_WAR;
	
	@ClientString(id = 1567, message = "The war against $s1 Clan has been stopped.")
	public static SystemMessageId THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED;
	
	@ClientString(id = 1568, message = "The target for declaration is wrong.")
	public static SystemMessageId THE_TARGET_FOR_DECLARATION_IS_WRONG;
	
	@ClientString(id = 1569, message = "A declaration of Clan War against an allied clan can't be made.")
	public static SystemMessageId A_DECLARATION_OF_CLAN_WAR_AGAINST_AN_ALLIED_CLAN_CAN_T_BE_MADE;
	
	@ClientString(id = 1570, message = "A declaration of war against more than 30 Clans can't be made at the same time.")
	public static SystemMessageId A_DECLARATION_OF_WAR_AGAINST_MORE_THAN_30_CLANS_CAN_T_BE_MADE_AT_THE_SAME_TIME;
	
	@ClientString(id = 1571, message = "======<Clans You've Declared War On>======")
	public static SystemMessageId CLANS_YOU_VE_DECLARED_WAR_ON;
	
	@ClientString(id = 1572, message = "======<Clans That Have Declared War On You>======")
	public static SystemMessageId CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU;
	
	@ClientString(id = 1573, message = "There are no clans that your clan has declared war against.")
	public static SystemMessageId THERE_ARE_NO_CLANS_THAT_YOUR_CLAN_HAS_DECLARED_WAR_AGAINST;
	
	@ClientString(id = 1574, message = "All is well. There are no clans that have declared war against your clan.")
	public static SystemMessageId ALL_IS_WELL_THERE_ARE_NO_CLANS_THAT_HAVE_DECLARED_WAR_AGAINST_YOUR_CLAN;
	
	@ClientString(id = 1575, message = "Command Channels can only be formed by a party leader who is also the leader of a level 5 clan.")
	public static SystemMessageId COMMAND_CHANNELS_CAN_ONLY_BE_FORMED_BY_A_PARTY_LEADER_WHO_IS_ALSO_THE_LEADER_OF_A_LEVEL_5_CLAN;
	
	@ClientString(id = 1576, message = "Pet uses the power of spirit.")
	public static SystemMessageId PET_USES_THE_POWER_OF_SPIRIT;
	
	@ClientString(id = 1577, message = "Servitor uses the power of spirit.")
	public static SystemMessageId SERVITOR_USES_THE_POWER_OF_SPIRIT;
	
	@ClientString(id = 1578, message = "Items are not available for a private store or private manufacture.")
	public static SystemMessageId ITEMS_ARE_NOT_AVAILABLE_FOR_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURE;
	
	@ClientString(id = 1579, message = "$s1's pet gained $s2 adena.")
	public static SystemMessageId S1_S_PET_GAINED_S2_ADENA;
	
	@ClientString(id = 1580, message = "The Command Channel has been formed.")
	public static SystemMessageId THE_COMMAND_CHANNEL_HAS_BEEN_FORMED;
	
	@ClientString(id = 1581, message = "The Command Channel has been disbanded.")
	public static SystemMessageId THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED;
	
	@ClientString(id = 1582, message = "You have joined the Command Channel.")
	public static SystemMessageId YOU_HAVE_JOINED_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1583, message = "You were dismissed from the Command Channel.")
	public static SystemMessageId YOU_WERE_DISMISSED_FROM_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1584, message = "$s1's party has been dismissed from the Command Channel.")
	public static SystemMessageId S1_S_PARTY_HAS_BEEN_DISMISSED_FROM_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1585, message = "The Command Channel has been disbanded.")
	public static SystemMessageId THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED_2;
	
	@ClientString(id = 1586, message = "You have quit the Command Channel.")
	public static SystemMessageId YOU_HAVE_QUIT_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1587, message = "$s1's party has left the Command Channel.")
	public static SystemMessageId S1_S_PARTY_HAS_LEFT_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1588, message = "The Command Channel is activated only when there are at least 5 parties participating.")
	public static SystemMessageId THE_COMMAND_CHANNEL_IS_ACTIVATED_ONLY_WHEN_THERE_ARE_AT_LEAST_5_PARTIES_PARTICIPATING;
	
	@ClientString(id = 1589, message = "Command Channel authority has been transferred to $s1.")
	public static SystemMessageId COMMAND_CHANNEL_AUTHORITY_HAS_BEEN_TRANSFERRED_TO_S1;
	
	@ClientString(id = 1590, message = "===<Guild Info (Total Parties: $s1)>===")
	public static SystemMessageId GUILD_INFO_TOTAL_PARTIES_S1;
	
	@ClientString(id = 1591, message = "No user has been invited to the Command Channel.")
	public static SystemMessageId NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1592, message = "You can no longer set up a Command Channel.")
	public static SystemMessageId YOU_CAN_NO_LONGER_SET_UP_A_COMMAND_CHANNEL;
	
	@ClientString(id = 1593, message = "You do not have authority to invite someone to the Command Channel.")
	public static SystemMessageId YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1594, message = "$s1's party is already a member of the Command Channel.")
	public static SystemMessageId S1_S_PARTY_IS_ALREADY_A_MEMBER_OF_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1595, message = "$s1 has succeeded.")
	public static SystemMessageId S1_HAS_SUCCEEDED;
	
	@ClientString(id = 1596, message = "You were hit by $s1!")
	public static SystemMessageId YOU_WERE_HIT_BY_S1;
	
	@ClientString(id = 1597, message = "$s1 has failed.")
	public static SystemMessageId S1_HAS_FAILED;
	
	@ClientString(id = 1598, message = "Soulshots and spiritshots are not available for a dead pet or servitor. Sad, isn't it?")
	public static SystemMessageId SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET_OR_SERVITOR_SAD_ISN_T_IT;
	
	@ClientString(id = 1599, message = "You cannot 'observe' while you are in combat!")
	public static SystemMessageId YOU_CANNOT_OBSERVE_WHILE_YOU_ARE_IN_COMBAT;
	
	@ClientString(id = 1600, message = "Tomorrow's items will ALL be set to 0. Do you wish to continue?")
	public static SystemMessageId TOMORROW_S_ITEMS_WILL_ALL_BE_SET_TO_0_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 1601, message = "Tomorrow's items will all be set to the same value as today's items. Do you wish to continue?")
	public static SystemMessageId TOMORROW_S_ITEMS_WILL_ALL_BE_SET_TO_THE_SAME_VALUE_AS_TODAY_S_ITEMS_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 1602, message = "Only a party leader can access the Command Channel.")
	public static SystemMessageId ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1603, message = "Only channel opener can give All Command.")
	public static SystemMessageId ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND;
	
	@ClientString(id = 1604, message = "While dressed in formal wear, you can't use items that require all skills and casting operations.")
	public static SystemMessageId WHILE_DRESSED_IN_FORMAL_WEAR_YOU_CAN_T_USE_ITEMS_THAT_REQUIRE_ALL_SKILLS_AND_CASTING_OPERATIONS;
	
	@ClientString(id = 1605, message = "* Here, you can buy only seeds of $s1 Manor.")
	public static SystemMessageId HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR;
	
	@ClientString(id = 1606, message = "Congratulations - You've completed the third-class transfer quest!")
	public static SystemMessageId CONGRATULATIONS_YOU_VE_COMPLETED_THE_THIRD_CLASS_TRANSFER_QUEST;
	
	@ClientString(id = 1607, message = "$s1 adena has been withdrawn to pay for purchasing fees.")
	public static SystemMessageId S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES;
	
	@ClientString(id = 1608, message = "Due to insufficient adena you cannot buy another castle.")
	public static SystemMessageId DUE_TO_INSUFFICIENT_ADENA_YOU_CANNOT_BUY_ANOTHER_CASTLE;
	
	@ClientString(id = 1609, message = "War has already been declared against that clan… but I'll make note that you really don't like them.")
	public static SystemMessageId WAR_HAS_ALREADY_BEEN_DECLARED_AGAINST_THAT_CLAN_BUT_I_LL_MAKE_NOTE_THAT_YOU_REALLY_DON_T_LIKE_THEM;
	
	@ClientString(id = 1610, message = "Fool! You cannot declare war against your own clan!")
	public static SystemMessageId FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN;
	
	@ClientString(id = 1611, message = "Party Leader: $s1")
	public static SystemMessageId PARTY_LEADER_S1;
	
	@ClientString(id = 1612, message = "=====<War List>=====")
	public static SystemMessageId WAR_LIST;
	
	@ClientString(id = 1613, message = "There is no clan listed on War List.")
	public static SystemMessageId THERE_IS_NO_CLAN_LISTED_ON_WAR_LIST;
	
	@ClientString(id = 1614, message = "You have joined a channel that was already open.")
	public static SystemMessageId YOU_HAVE_JOINED_A_CHANNEL_THAT_WAS_ALREADY_OPEN;
	
	@ClientString(id = 1615, message = "The number of remaining parties is $s1 until a channel is activated.")
	public static SystemMessageId THE_NUMBER_OF_REMAINING_PARTIES_IS_S1_UNTIL_A_CHANNEL_IS_ACTIVATED;
	
	@ClientString(id = 1616, message = "The Command Channel has been activated.")
	public static SystemMessageId THE_COMMAND_CHANNEL_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 1617, message = "You do not have the authority to use the Command Channel.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1618, message = "The ferry from Rune Harbor to Gludin Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1619, message = "The ferry from Gludin Harbor to Rune Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_TO_RUNE_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1620, message = "Arrived at Rune Harbor.")
	public static SystemMessageId ARRIVED_AT_RUNE_HARBOR;
	
	@ClientString(id = 1621, message = "Departure for Gludin Harbor will take place in five minutes!")
	public static SystemMessageId DEPARTURE_FOR_GLUDIN_HARBOR_WILL_TAKE_PLACE_IN_FIVE_MINUTES;
	
	@ClientString(id = 1622, message = "Departure for Gludin Harbor will take place in one minute!")
	public static SystemMessageId DEPARTURE_FOR_GLUDIN_HARBOR_WILL_TAKE_PLACE_IN_ONE_MINUTE;
	
	@ClientString(id = 1623, message = "Make haste! We will be departing for Gludin Harbor shortly…")
	public static SystemMessageId MAKE_HASTE_WE_WILL_BE_DEPARTING_FOR_GLUDIN_HARBOR_SHORTLY;
	
	@ClientString(id = 1624, message = "We are now departing for Gludin Harbor. Hold on and enjoy the ride!")
	public static SystemMessageId WE_ARE_NOW_DEPARTING_FOR_GLUDIN_HARBOR_HOLD_ON_AND_ENJOY_THE_RIDE;
	
	@ClientString(id = 1625, message = "Departure for Rune Harbor will take place after anchoring for ten minutes.")
	public static SystemMessageId DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 1626, message = "Departure for Rune Harbor will take place in five minutes!")
	public static SystemMessageId DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_IN_FIVE_MINUTES;
	
	@ClientString(id = 1627, message = "Departure for Rune Harbor will take place in one minute!")
	public static SystemMessageId DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_IN_ONE_MINUTE;
	
	@ClientString(id = 1628, message = "Make haste! We will be departing for Gludin Harbor shortly…")
	public static SystemMessageId MAKE_HASTE_WE_WILL_BE_DEPARTING_FOR_GLUDIN_HARBOR_SHORTLY_2;
	
	@ClientString(id = 1629, message = "We are now departing for Rune Harbor. Hold on and enjoy the ride!")
	public static SystemMessageId WE_ARE_NOW_DEPARTING_FOR_RUNE_HARBOR_HOLD_ON_AND_ENJOY_THE_RIDE;
	
	@ClientString(id = 1630, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1631, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1632, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1633, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1634, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1635, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1636, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1637, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1638, message = "You cannot fish while using a recipe book, private manufacture or private store.")
	public static SystemMessageId YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE;
	
	@ClientString(id = 1639, message = "Period $s1 of the Grand Olympiad Games has started!")
	public static SystemMessageId PERIOD_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_STARTED;
	
	@ClientString(id = 1640, message = "Period $s1 of the Grand Olympiad Games has now ended.")
	public static SystemMessageId PERIOD_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_NOW_ENDED;
	
	@ClientString(id = 1641, message = "Sharpen your swords, tighten the stitchings in your armor, and make haste to a Grand Olympiad Manager! Battles in the Grand Olympiad Games are now taking place!")
	public static SystemMessageId SHARPEN_YOUR_SWORDS_TIGHTEN_THE_STITCHINGS_IN_YOUR_ARMOR_AND_MAKE_HASTE_TO_A_GRAND_OLYMPIAD_MANAGER_BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_TAKING_PLACE;
	
	@ClientString(id = 1642, message = "Much carnage has been left for the cleanup crew of the Olympiad Stadium. Battles in the Grand Olympiad Games are now over!")
	public static SystemMessageId MUCH_CARNAGE_HAS_BEEN_LEFT_FOR_THE_CLEANUP_CREW_OF_THE_OLYMPIAD_STADIUM_BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_OVER;
	
	@ClientString(id = 1643, message = "Current Location: $s1, $s2, $s3 (Dimensional Gap)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_DIMENSIONAL_GAP;
	
	@ClientString(id = 1644, message = "none")
	public static SystemMessageId NONE;
	
	@ClientString(id = 1645, message = "none")
	public static SystemMessageId NONE_2;
	
	@ClientString(id = 1646, message = "none")
	public static SystemMessageId NONE_3;
	
	@ClientString(id = 1647, message = "none")
	public static SystemMessageId NONE_4;
	
	@ClientString(id = 1648, message = "none")
	public static SystemMessageId NONE_5;
	
	@ClientString(id = 1649, message = "Play time is now accumulating.")
	public static SystemMessageId PLAY_TIME_IS_NOW_ACCUMULATING;
	
	@ClientString(id = 1650, message = "Due to high server traffic, your login attempt has failed. Please try again soon.")
	public static SystemMessageId DUE_TO_HIGH_SERVER_TRAFFIC_YOUR_LOGIN_ATTEMPT_HAS_FAILED_PLEASE_TRY_AGAIN_SOON;
	
	@ClientString(id = 1651, message = "The Grand Olympiad Games are not currently in progress.")
	public static SystemMessageId THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS;
	
	@ClientString(id = 1652, message = "You are now recording gameplay.")
	public static SystemMessageId YOU_ARE_NOW_RECORDING_GAMEPLAY;
	
	@ClientString(id = 1653, message = "Your recording has been successfully stored. ($s1)")
	public static SystemMessageId YOUR_RECORDING_HAS_BEEN_SUCCESSFULLY_STORED_S1;
	
	@ClientString(id = 1654, message = "The attempt to record the replay file has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_RECORD_THE_REPLAY_FILE_HAS_FAILED;
	
	@ClientString(id = 1655, message = "You caught something smelly and scary, maybe you should throw it back!?")
	public static SystemMessageId YOU_CAUGHT_SOMETHING_SMELLY_AND_SCARY_MAYBE_YOU_SHOULD_THROW_IT_BACK;
	
	@ClientString(id = 1656, message = "You have successfully traded the item with the NPC.")
	public static SystemMessageId YOU_HAVE_SUCCESSFULLY_TRADED_THE_ITEM_WITH_THE_NPC;
	
	@ClientString(id = 1657, message = "$s1 has earned $s2 points in the Grand Olympiad Games.")
	public static SystemMessageId S1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES;
	
	@ClientString(id = 1658, message = "$s1 has lost $s2 points in the Grand Olympiad Games.")
	public static SystemMessageId S1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES;
	
	@ClientString(id = 1659, message = "Current Location: $s1, $s2, $s3 (Cemetery of the Empire).")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_CEMETERY_OF_THE_EMPIRE;
	
	@ClientString(id = 1660, message = "The channel was opened by: $s1")
	public static SystemMessageId THE_CHANNEL_WAS_OPENED_BY_S1;
	
	@ClientString(id = 1661, message = "$s1 has obtained $s3 $s2s.")
	public static SystemMessageId S1_HAS_OBTAINED_S3_S2S;
	
	@ClientString(id = 1662, message = "The fish are no longer biting here because you've caught too many! Try fishing in another location.")
	public static SystemMessageId THE_FISH_ARE_NO_LONGER_BITING_HERE_BECAUSE_YOU_VE_CAUGHT_TOO_MANY_TRY_FISHING_IN_ANOTHER_LOCATION;
	
	@ClientString(id = 1663, message = "The clan crest was successfully registered. Remember, only a clan that owns a clan hall or castle can have their crest displayed.")
	public static SystemMessageId THE_CLAN_CREST_WAS_SUCCESSFULLY_REGISTERED_REMEMBER_ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_CASTLE_CAN_HAVE_THEIR_CREST_DISPLAYED;
	
	@ClientString(id = 1664, message = "The fish is resisting your efforts to haul it in! Look at that bobber go!")
	public static SystemMessageId THE_FISH_IS_RESISTING_YOUR_EFFORTS_TO_HAUL_IT_IN_LOOK_AT_THAT_BOBBER_GO;
	
	@ClientString(id = 1665, message = "You've worn that fish out! It can't even pull the bobber under the water!")
	public static SystemMessageId YOU_VE_WORN_THAT_FISH_OUT_IT_CAN_T_EVEN_PULL_THE_BOBBER_UNDER_THE_WATER;
	
	@ClientString(id = 1666, message = "You have obtained +$s1$s2.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S1_S2;
	
	@ClientString(id = 1667, message = "Lethal Strike!")
	public static SystemMessageId LETHAL_STRIKE;
	
	@ClientString(id = 1668, message = "Your lethal strike was successful!")
	public static SystemMessageId YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL;
	
	@ClientString(id = 1669, message = "There was nothing found inside of that.")
	public static SystemMessageId THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT;
	
	@ClientString(id = 1670, message = "Due to your Reeling and/or Pumping skill being three or more levels higher than your Fishing skill, a 50 damage penalty will be applied.")
	public static SystemMessageId DUE_TO_YOUR_REELING_AND_OR_PUMPING_SKILL_BEING_THREE_OR_MORE_LEVELS_HIGHER_THAN_YOUR_FISHING_SKILL_A_50_DAMAGE_PENALTY_WILL_BE_APPLIED;
	
	@ClientString(id = 1671, message = "Your reeling was successful! (Mastery Penalty:$s1 )")
	public static SystemMessageId YOUR_REELING_WAS_SUCCESSFUL_MASTERY_PENALTY_S1;
	
	@ClientString(id = 1672, message = "Your pumping was successful! (Mastery Penalty:$s1 )")
	public static SystemMessageId YOUR_PUMPING_WAS_SUCCESSFUL_MASTERY_PENALTY_S1;
	
	@ClientString(id = 1673, message = "Your current record for this Grand Olympiad is $s1 match(es), $s2 win(s) and $s3 defeat(s). You have earned $s4 Olympiad Point(s).")
	public static SystemMessageId YOUR_CURRENT_RECORD_FOR_THIS_GRAND_OLYMPIAD_IS_S1_MATCH_ES_S2_WIN_S_AND_S3_DEFEAT_S_YOU_HAVE_EARNED_S4_OLYMPIAD_POINT_S;
	
	@ClientString(id = 1674, message = "This command can only be used by a Noblesse.")
	public static SystemMessageId THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE;
	
	@ClientString(id = 1675, message = "A manor cannot be set up between 6 a.m. and 8 p.m.")
	public static SystemMessageId A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_A_M_AND_8_P_M;
	
	@ClientString(id = 1676, message = "You do not have a servitor or pet and therefore cannot use the automatic-use function.")
	public static SystemMessageId YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATIC_USE_FUNCTION;
	
	@ClientString(id = 1677, message = "A cease-fire during a Clan War can not be called while members of your clan are engaged in battle.")
	public static SystemMessageId A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE;
	
	@ClientString(id = 1678, message = "You have not declared a Clan War against the clan $s1.")
	public static SystemMessageId YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_AGAINST_THE_CLAN_S1;
	
	@ClientString(id = 1679, message = "Only the creator of a channel can issue a global command.")
	public static SystemMessageId ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_ISSUE_A_GLOBAL_COMMAND;
	
	@ClientString(id = 1680, message = "$s1 has declined the channel invitation.")
	public static SystemMessageId S1_HAS_DECLINED_THE_CHANNEL_INVITATION;
	
	@ClientString(id = 1681, message = "Since $s1 did not respond, your channel invitation has failed.")
	public static SystemMessageId SINCE_S1_DID_NOT_RESPOND_YOUR_CHANNEL_INVITATION_HAS_FAILED;
	
	@ClientString(id = 1682, message = "Only the creator of a channel can use the channel dismiss command.")
	public static SystemMessageId ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_USE_THE_CHANNEL_DISMISS_COMMAND;
	
	@ClientString(id = 1683, message = "Only a party leader can choose the option to leave a channel.")
	public static SystemMessageId ONLY_A_PARTY_LEADER_CAN_CHOOSE_THE_OPTION_TO_LEAVE_A_CHANNEL;
	
	@ClientString(id = 1684, message = "A Clan War can not be declared against a clan that is being dissolved.")
	public static SystemMessageId A_CLAN_WAR_CAN_NOT_BE_DECLARED_AGAINST_A_CLAN_THAT_IS_BEING_DISSOLVED;
	
	@ClientString(id = 1685, message = "You are unable to equip this item when your PK count is greater than or equal to one.")
	public static SystemMessageId YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE;
	
	@ClientString(id = 1686, message = "Stones and mortar tumble to the earth - the castle wall has taken damage!")
	public static SystemMessageId STONES_AND_MORTAR_TUMBLE_TO_THE_EARTH_THE_CASTLE_WALL_HAS_TAKEN_DAMAGE;
	
	@ClientString(id = 1687, message = "This area cannot be entered while mounted atop of a Wyvern. You will be dismounted from your Wyvern if you do not leave!")
	public static SystemMessageId THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN_YOU_WILL_BE_DISMOUNTED_FROM_YOUR_WYVERN_IF_YOU_DO_NOT_LEAVE;
	
	@ClientString(id = 1688, message = "You cannot enchant while operating a Private Store or Private Workshop.")
	public static SystemMessageId YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP;
	
	@ClientString(id = 1689, message = "You have already joined the waiting list for a class specific match.")
	public static SystemMessageId YOU_HAVE_ALREADY_JOINED_THE_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH;
	
	@ClientString(id = 1690, message = "You have already joined the waiting list for a non-class specific match.")
	public static SystemMessageId YOU_HAVE_ALREADY_JOINED_THE_WAITING_LIST_FOR_A_NON_CLASS_SPECIFIC_MATCH;
	
	@ClientString(id = 1691, message = "You can't join a Grand Olympiad Game match with that much stuff on you! Reduce your weight to below 80 percent full and request to join again!")
	public static SystemMessageId YOU_CAN_T_JOIN_A_GRAND_OLYMPIAD_GAME_MATCH_WITH_THAT_MUCH_STUFF_ON_YOU_REDUCE_YOUR_WEIGHT_TO_BELOW_80_PERCENT_FULL_AND_REQUEST_TO_JOIN_AGAIN;
	
	@ClientString(id = 1692, message = "You have changed from your main class to a subclass and therefore are removed from the Grand Olympiad Games waiting list.")
	public static SystemMessageId YOU_HAVE_CHANGED_FROM_YOUR_MAIN_CLASS_TO_A_SUBCLASS_AND_THEREFORE_ARE_REMOVED_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST;
	
	@ClientString(id = 1693, message = "You may not observe a Grand Olympiad Games match while you are on the waiting list.")
	public static SystemMessageId YOU_MAY_NOT_OBSERVE_A_GRAND_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST;
	
	@ClientString(id = 1694, message = "Only a clan leader that is a Noblesse can view the Siege War Status window during a siege war.")
	public static SystemMessageId ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR;
	
	@ClientString(id = 1695, message = "You can only use that during a Siege War!")
	public static SystemMessageId YOU_CAN_ONLY_USE_THAT_DURING_A_SIEGE_WAR;
	
	@ClientString(id = 1696, message = "Your accumulated play time is $s1.")
	public static SystemMessageId YOUR_ACCUMULATED_PLAY_TIME_IS_S1;
	
	@ClientString(id = 1697, message = "Your accumulated play time has reached Fatigue level, so you will receive experience or item drops at only 50 percent of the normal rate. For the sake of you physical and emotional health, we encourage you to log out as soon as possible and take a break before returning.")
	public static SystemMessageId YOUR_ACCUMULATED_PLAY_TIME_HAS_REACHED_FATIGUE_LEVEL_SO_YOU_WILL_RECEIVE_EXPERIENCE_OR_ITEM_DROPS_AT_ONLY_50_PERCENT_OF_THE_NORMAL_RATE_FOR_THE_SAKE_OF_YOU_PHYSICAL_AND_EMOTIONAL_HEALTH_WE_ENCOURAGE_YOU_TO_LOG_OUT_AS_SOON_AS_POSSIBLE_AND_TAKE_A_BREAK_BEFORE_RETURNING;
	
	@ClientString(id = 1698, message = "Your accumulated play time has reached Ill-health level, so you will no longer gain experience or item drops. For the sake of your physical and emotional health, please log out as soon as possible and take a break. Once you have been logged out for at least 5 hours, the experience and item drop rate penalties will be removed.")
	public static SystemMessageId YOUR_ACCUMULATED_PLAY_TIME_HAS_REACHED_ILL_HEALTH_LEVEL_SO_YOU_WILL_NO_LONGER_GAIN_EXPERIENCE_OR_ITEM_DROPS_FOR_THE_SAKE_OF_YOUR_PHYSICAL_AND_EMOTIONAL_HEALTH_PLEASE_LOG_OUT_AS_SOON_AS_POSSIBLE_AND_TAKE_A_BREAK_ONCE_YOU_HAVE_BEEN_LOGGED_OUT_FOR_AT_LEAST_5_HOURS_THE_EXPERIENCE_AND_ITEM_DROP_RATE_PENALTIES_WILL_BE_REMOVED;
	
	@ClientString(id = 1699, message = "You cannot dismiss a party member by force.")
	public static SystemMessageId YOU_CANNOT_DISMISS_A_PARTY_MEMBER_BY_FORCE;
	
	@ClientString(id = 1700, message = "You don't have enough spiritshots needed for a pet/servitor.")
	public static SystemMessageId YOU_DON_T_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR;
	
	@ClientString(id = 1701, message = "You don't have enough soulshots needed for a pet/servitor.")
	public static SystemMessageId YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR;
	
	@ClientString(id = 1702, message = "$s1 is using a third party program.")
	public static SystemMessageId S1_IS_USING_A_THIRD_PARTY_PROGRAM;
	
	@ClientString(id = 1703, message = "The previously investigated user is not using a third party program.")
	public static SystemMessageId THE_PREVIOUSLY_INVESTIGATED_USER_IS_NOT_USING_A_THIRD_PARTY_PROGRAM;
	
	@ClientString(id = 1704, message = "Please close the the setup window for your private manufacturing store or private store, and try again.")
	public static SystemMessageId PLEASE_CLOSE_THE_THE_SETUP_WINDOW_FOR_YOUR_PRIVATE_MANUFACTURING_STORE_OR_PRIVATE_STORE_AND_TRY_AGAIN;
	
	@ClientString(id = 1705, message = "PC Bang Points acquisition period. Points acquisition period left $s1 hour.")
	public static SystemMessageId PC_BANG_POINTS_ACQUISITION_PERIOD_POINTS_ACQUISITION_PERIOD_LEFT_S1_HOUR;
	
	@ClientString(id = 1706, message = "PC Bang Points use period. Points use period left $s1 hour.")
	public static SystemMessageId PC_BANG_POINTS_USE_PERIOD_POINTS_USE_PERIOD_LEFT_S1_HOUR;
	
	@ClientString(id = 1707, message = "You acquired $s1 PC Bang Point.")
	public static SystemMessageId YOU_ACQUIRED_S1_PC_BANG_POINT;
	
	@ClientString(id = 1708, message = "Double points! You acquired $s1 PC Bang Point.")
	public static SystemMessageId DOUBLE_POINTS_YOU_ACQUIRED_S1_PC_BANG_POINT;
	
	@ClientString(id = 1709, message = "You are using $s1 point.")
	public static SystemMessageId YOU_ARE_USING_S1_POINT;
	
	@ClientString(id = 1710, message = "You are short of accumulated points.")
	public static SystemMessageId YOU_ARE_SHORT_OF_ACCUMULATED_POINTS;
	
	@ClientString(id = 1711, message = "PC Bang Points use period has expired.")
	public static SystemMessageId PC_BANG_POINTS_USE_PERIOD_HAS_EXPIRED;
	
	@ClientString(id = 1712, message = "The PC Bang Points accumulation period has expired.")
	public static SystemMessageId THE_PC_BANG_POINTS_ACCUMULATION_PERIOD_HAS_EXPIRED;
	
	@ClientString(id = 1713, message = "The games may be delayed due to an insufficient number of players waiting.")
	public static SystemMessageId THE_GAMES_MAY_BE_DELAYED_DUE_TO_AN_INSUFFICIENT_NUMBER_OF_PLAYERS_WAITING;
	
	@ClientString(id = 1714, message = "Current Location: $s1, $s2, $s3 (Near the Town of Schuttgart)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_SCHUTTGART;
	
	@ClientString(id = 1715, message = "This is a Peaceful Zone\\n- PvP is not allowed in this area.")
	public static SystemMessageId THIS_IS_A_PEACEFUL_ZONE_N_PVP_IS_NOT_ALLOWED_IN_THIS_AREA;
	
	@ClientString(id = 1716, message = "Altered Zone")
	public static SystemMessageId ALTERED_ZONE;
	
	@ClientString(id = 1717, message = "Siege War Zone \\n- A siege is currently in progress in this area. \\n If a character dies in this zone, their resurrection ability may be restricted.")
	public static SystemMessageId SIEGE_WAR_ZONE_N_A_SIEGE_IS_CURRENTLY_IN_PROGRESS_IN_THIS_AREA_N_IF_A_CHARACTER_DIES_IN_THIS_ZONE_THEIR_RESURRECTION_ABILITY_MAY_BE_RESTRICTED;
	
	@ClientString(id = 1718, message = "General Field")
	public static SystemMessageId GENERAL_FIELD;
	
	@ClientString(id = 1719, message = "Seven Signs Zone \\n- Although a character's level may increase while in this area, HP and MP \\n will not be regenerated.")
	public static SystemMessageId SEVEN_SIGNS_ZONE_N_ALTHOUGH_A_CHARACTER_S_LEVEL_MAY_INCREASE_WHILE_IN_THIS_AREA_HP_AND_MP_N_WILL_NOT_BE_REGENERATED;
	
	@ClientString(id = 1720, message = "---")
	public static SystemMessageId EMPTY_6;
	
	@ClientString(id = 1721, message = "Combat Zone")
	public static SystemMessageId COMBAT_ZONE;
	
	@ClientString(id = 1722, message = "Please enter the name of the item you wish to search for.")
	public static SystemMessageId PLEASE_ENTER_THE_NAME_OF_THE_ITEM_YOU_WISH_TO_SEARCH_FOR;
	
	@ClientString(id = 1723, message = "Please take a moment to provide feedback about the petition service.")
	public static SystemMessageId PLEASE_TAKE_A_MOMENT_TO_PROVIDE_FEEDBACK_ABOUT_THE_PETITION_SERVICE;
	
	@ClientString(id = 1724, message = "A servitor whom is engaged in battle cannot be de-activated.")
	public static SystemMessageId A_SERVITOR_WHOM_IS_ENGAGED_IN_BATTLE_CANNOT_BE_DE_ACTIVATED;
	
	@ClientString(id = 1725, message = "You have earned $s1 raid point(s).")
	public static SystemMessageId YOU_HAVE_EARNED_S1_RAID_POINT_S;
	
	@ClientString(id = 1726, message = "$s1 has disappeared since its allowed time period has expired.")
	public static SystemMessageId S1_HAS_DISAPPEARED_SINCE_ITS_ALLOWED_TIME_PERIOD_HAS_EXPIRED;
	
	@ClientString(id = 1727, message = "$s1 has invited you to a party room. Would you like to accept the invitation?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_A_PARTY_ROOM_WOULD_YOU_LIKE_TO_ACCEPT_THE_INVITATION;
	
	@ClientString(id = 1728, message = "The recipient of your invitation did not accept the party matching invitation.")
	public static SystemMessageId THE_RECIPIENT_OF_YOUR_INVITATION_DID_NOT_ACCEPT_THE_PARTY_MATCHING_INVITATION;
	
	@ClientString(id = 1729, message = "You cannot join a Command Channel while teleporting.")
	public static SystemMessageId YOU_CANNOT_JOIN_A_COMMAND_CHANNEL_WHILE_TELEPORTING;
	
	@ClientString(id = 1730, message = "To establish a Clan Academy, your clan must be Level 5 or higher.")
	public static SystemMessageId TO_ESTABLISH_A_CLAN_ACADEMY_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER;
	
	@ClientString(id = 1731, message = "Only the clan leader can create a Clan Academy.")
	public static SystemMessageId ONLY_THE_CLAN_LEADER_CAN_CREATE_A_CLAN_ACADEMY;
	
	@ClientString(id = 1732, message = "To create a Clan Academy, a Blood Mark is needed.")
	public static SystemMessageId TO_CREATE_A_CLAN_ACADEMY_A_BLOOD_MARK_IS_NEEDED;
	
	@ClientString(id = 1733, message = "You do not have enough adena to create a Clan Academy.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_ADENA_TO_CREATE_A_CLAN_ACADEMY;
	
	@ClientString(id = 1734, message = "To join a Clan Academy, characters must be Level 40 or below, not belong another clan and not yet completed their 2nd class transfer.")
	public static SystemMessageId TO_JOIN_A_CLAN_ACADEMY_CHARACTERS_MUST_BE_LEVEL_40_OR_BELOW_NOT_BELONG_ANOTHER_CLAN_AND_NOT_YET_COMPLETED_THEIR_2ND_CLASS_TRANSFER;
	
	@ClientString(id = 1735, message = "$s1 does not meet the requirements to join a Clan Academy.")
	public static SystemMessageId S1_DOES_NOT_MEET_THE_REQUIREMENTS_TO_JOIN_A_CLAN_ACADEMY;
	
	@ClientString(id = 1736, message = "The Clan Academy has reached its maximum enrollment.")
	public static SystemMessageId THE_CLAN_ACADEMY_HAS_REACHED_ITS_MAXIMUM_ENROLLMENT;
	
	@ClientString(id = 1737, message = "Your clan has not established a Clan Academy but is eligible to do so.")
	public static SystemMessageId YOUR_CLAN_HAS_NOT_ESTABLISHED_A_CLAN_ACADEMY_BUT_IS_ELIGIBLE_TO_DO_SO;
	
	@ClientString(id = 1738, message = "Your clan has already established a Clan Academy.")
	public static SystemMessageId YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY;
	
	@ClientString(id = 1739, message = "Would you like to create a Clan Academy?")
	public static SystemMessageId WOULD_YOU_LIKE_TO_CREATE_A_CLAN_ACADEMY;
	
	@ClientString(id = 1740, message = "Please enter the name of the Clan Academy.")
	public static SystemMessageId PLEASE_ENTER_THE_NAME_OF_THE_CLAN_ACADEMY;
	
	@ClientString(id = 1741, message = "Congratulations! The $s1's Clan Academy has been created.")
	public static SystemMessageId CONGRATULATIONS_THE_S1_S_CLAN_ACADEMY_HAS_BEEN_CREATED;
	
	@ClientString(id = 1742, message = "A message inviting $s1 to join the Clan Academy is being sent.")
	public static SystemMessageId A_MESSAGE_INVITING_S1_TO_JOIN_THE_CLAN_ACADEMY_IS_BEING_SENT;
	
	@ClientString(id = 1743, message = "To open a Clan Academy, the leader of a Level 5 clan or above must pay XX Proofs of Blood or a certain amount of adena.")
	public static SystemMessageId TO_OPEN_A_CLAN_ACADEMY_THE_LEADER_OF_A_LEVEL_5_CLAN_OR_ABOVE_MUST_PAY_XX_PROOFS_OF_BLOOD_OR_A_CERTAIN_AMOUNT_OF_ADENA;
	
	@ClientString(id = 1744, message = "There was no response to your invitation to join the Clan Academy, so the invitation has been rescinded.")
	public static SystemMessageId THERE_WAS_NO_RESPONSE_TO_YOUR_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_SO_THE_INVITATION_HAS_BEEN_RESCINDED;
	
	@ClientString(id = 1745, message = "The recipient of your invitation to join the Clan Academy has declined.")
	public static SystemMessageId THE_RECIPIENT_OF_YOUR_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_HAS_DECLINED;
	
	@ClientString(id = 1746, message = "You have already joined a Clan Academy.")
	public static SystemMessageId YOU_HAVE_ALREADY_JOINED_A_CLAN_ACADEMY;
	
	@ClientString(id = 1747, message = "$s1 has sent you an invitation to join the Clan Academy belonging to the $s2 clan. Do you accept?")
	public static SystemMessageId S1_HAS_SENT_YOU_AN_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_BELONGING_TO_THE_S2_CLAN_DO_YOU_ACCEPT;
	
	@ClientString(id = 1748, message = "Clan Academy member $s1 has successfully completed the 2nd class transfer and obtained $s2 Clan Reputation points.")
	public static SystemMessageId CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS;
	
	@ClientString(id = 1749, message = "Congratulations! You will now graduate from the Clan Academy and leave your current clan. As a graduate of the academy, you can immediately join a clan as a regular member without being subject to any penalties.")
	public static SystemMessageId CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES;
	
	@ClientString(id = 1750, message = "If you possess $s1, you cannot participate in the Olympiad.")
	public static SystemMessageId IF_YOU_POSSESS_S1_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD;
	
	@ClientString(id = 1751, message = "The Grand Master has given you a commemorative item.")
	public static SystemMessageId THE_GRAND_MASTER_HAS_GIVEN_YOU_A_COMMEMORATIVE_ITEM;
	
	@ClientString(id = 1752, message = "Since the clan has received a graduate of the Clan Academy, it has earned $s1 points toward its reputation score.")
	public static SystemMessageId SINCE_THE_CLAN_HAS_RECEIVED_A_GRADUATE_OF_THE_CLAN_ACADEMY_IT_HAS_EARNED_S1_POINTS_TOWARD_ITS_REPUTATION_SCORE;
	
	@ClientString(id = 1753, message = "The clan leader has decreed that that particular privilege cannot be granted to a Clan Academy member.")
	public static SystemMessageId THE_CLAN_LEADER_HAS_DECREED_THAT_THAT_PARTICULAR_PRIVILEGE_CANNOT_BE_GRANTED_TO_A_CLAN_ACADEMY_MEMBER;
	
	@ClientString(id = 1754, message = "That privilege cannot be granted to a Clan Academy member.")
	public static SystemMessageId THAT_PRIVILEGE_CANNOT_BE_GRANTED_TO_A_CLAN_ACADEMY_MEMBER;
	
	@ClientString(id = 1755, message = "$s2 has been designated as the apprentice of clan member $s1.")
	public static SystemMessageId S2_HAS_BEEN_DESIGNATED_AS_THE_APPRENTICE_OF_CLAN_MEMBER_S1;
	
	@ClientString(id = 1756, message = "Your apprentice, $s1, has logged in.")
	public static SystemMessageId YOUR_APPRENTICE_S1_HAS_LOGGED_IN;
	
	@ClientString(id = 1757, message = "Your apprentice, $s1, has logged out.")
	public static SystemMessageId YOUR_APPRENTICE_S1_HAS_LOGGED_OUT;
	
	@ClientString(id = 1758, message = "Your sponsor, $s1, has logged in.")
	public static SystemMessageId YOUR_SPONSOR_S1_HAS_LOGGED_IN;
	
	@ClientString(id = 1759, message = "Your sponsor, $s1, has logged out.")
	public static SystemMessageId YOUR_SPONSOR_S1_HAS_LOGGED_OUT;
	
	@ClientString(id = 1760, message = "Clan member $s1's title has been changed to $s2.")
	public static SystemMessageId CLAN_MEMBER_S1_S_TITLE_HAS_BEEN_CHANGED_TO_S2;
	
	@ClientString(id = 1761, message = "Clan member $s1's privilege level has been changed to $s2.")
	public static SystemMessageId CLAN_MEMBER_S1_S_PRIVILEGE_LEVEL_HAS_BEEN_CHANGED_TO_S2;
	
	@ClientString(id = 1762, message = "You do not have the right to dismiss an apprentice.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE;
	
	@ClientString(id = 1763, message = "$s2, clan member $s1's apprentice, has been removed.")
	public static SystemMessageId S2_CLAN_MEMBER_S1_S_APPRENTICE_HAS_BEEN_REMOVED;
	
	@ClientString(id = 1764, message = "This item can only be worn by a member of the Clan Academy.")
	public static SystemMessageId THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;
	
	@ClientString(id = 1765, message = "As a graduate of the Clan Academy, you can no longer wear this item.")
	public static SystemMessageId AS_A_GRADUATE_OF_THE_CLAN_ACADEMY_YOU_CAN_NO_LONGER_WEAR_THIS_ITEM;
	
	@ClientString(id = 1766, message = "An application to join the clan has been sent to $s1 in $s2.")
	public static SystemMessageId AN_APPLICATION_TO_JOIN_THE_CLAN_HAS_BEEN_SENT_TO_S1_IN_S2;
	
	@ClientString(id = 1767, message = "An application to join the Clan Academy has been sent to $s1.")
	public static SystemMessageId AN_APPLICATION_TO_JOIN_THE_CLAN_ACADEMY_HAS_BEEN_SENT_TO_S1;
	
	@ClientString(id = 1768, message = "$s1 has invited you to join the Clan Academy of $s2 clan. Would you like to join?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_ACADEMY_OF_S2_CLAN_WOULD_YOU_LIKE_TO_JOIN;
	
	@ClientString(id = 1769, message = "$s1 has sent you an invitation to join the $s3 Order of Knights under the $s2 clan. Would you like to join?")
	public static SystemMessageId S1_HAS_SENT_YOU_AN_INVITATION_TO_JOIN_THE_S3_ORDER_OF_KNIGHTS_UNDER_THE_S2_CLAN_WOULD_YOU_LIKE_TO_JOIN;
	
	@ClientString(id = 1770, message = "The clan's reputation score has dropped below 0. The clan may face certain penalties as a result.")
	public static SystemMessageId THE_CLAN_S_REPUTATION_SCORE_HAS_DROPPED_BELOW_0_THE_CLAN_MAY_FACE_CERTAIN_PENALTIES_AS_A_RESULT;
	
	@ClientString(id = 1771, message = "Now that your clan level is above Level 5, it can accumulate clan reputation points.")
	public static SystemMessageId NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS;
	
	@ClientString(id = 1772, message = "Since your clan was defeated in a siege, $s1 points have been deducted from your clan's reputation score and given to the opposing clan.")
	public static SystemMessageId SINCE_YOUR_CLAN_WAS_DEFEATED_IN_A_SIEGE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_S_REPUTATION_SCORE_AND_GIVEN_TO_THE_OPPOSING_CLAN;
	
	@ClientString(id = 1773, message = "Since your clan emerged victorious from the siege, $s1 points have been added to your clan's reputation score.")
	public static SystemMessageId SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1774, message = "Your clan's newly acquired contested clan hall has added $s1 points to your clan's reputation score.")
	public static SystemMessageId YOUR_CLAN_S_NEWLY_ACQUIRED_CONTESTED_CLAN_HALL_HAS_ADDED_S1_POINTS_TO_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1775, message = "Clan member $s1 was an active member of the highest-ranked party in the Festival of Darkness. $s2 points have been added to your clan's reputation score.")
	public static SystemMessageId CLAN_MEMBER_S1_WAS_AN_ACTIVE_MEMBER_OF_THE_HIGHEST_RANKED_PARTY_IN_THE_FESTIVAL_OF_DARKNESS_S2_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1776, message = "Clan member $s1 was named a hero. $2s points have been added to your clan's reputation score.")
	public static SystemMessageId CLAN_MEMBER_S1_WAS_NAMED_A_HERO_2S_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1777, message = "You have successfully completed a clan quest. $s1 points have been added to your clan's reputation score.")
	public static SystemMessageId YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1778, message = "An opposing clan has captured your clan's contested clan hall. $s1 points have been deducted from your clan's reputation score.")
	public static SystemMessageId AN_OPPOSING_CLAN_HAS_CAPTURED_YOUR_CLAN_S_CONTESTED_CLAN_HALL_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1779, message = "After losing the contested clan hall, 300 points have been deducted from your clan's reputation score.")
	public static SystemMessageId AFTER_LOSING_THE_CONTESTED_CLAN_HALL_300_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1780, message = "Your clan has captured your opponent's contested clan hall. $s1 points have been deducted from your opponent's clan reputation score.")
	public static SystemMessageId YOUR_CLAN_HAS_CAPTURED_YOUR_OPPONENT_S_CONTESTED_CLAN_HALL_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENT_S_CLAN_REPUTATION_SCORE;
	
	@ClientString(id = 1781, message = "Your clan has added $1s points to its clan reputation score.")
	public static SystemMessageId YOUR_CLAN_HAS_ADDED_1S_POINTS_TO_ITS_CLAN_REPUTATION_SCORE;
	
	@ClientString(id = 1782, message = "Your clan member $s1 was killed. $s2 points have been deducted from your clan's reputation score and added to your opponent's clan reputation score.")
	public static SystemMessageId YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_S_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_S_CLAN_REPUTATION_SCORE;
	
	@ClientString(id = 1783, message = "For killing an opposing clan member, $s1 points have been deducted from your opponents' clan reputation score.")
	public static SystemMessageId FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE;
	
	@ClientString(id = 1784, message = "Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score and added to your opponents'.")
	public static SystemMessageId YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_S_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS;
	
	@ClientString(id = 1785, message = "The clan you belong to has been initialized. $s1 points have been deducted from your clan reputation score.")
	public static SystemMessageId THE_CLAN_YOU_BELONG_TO_HAS_BEEN_INITIALIZED_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE;
	
	@ClientString(id = 1786, message = "Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score.")
	public static SystemMessageId YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1787, message = "$s1 points have been deducted from the clan's reputation score.")
	public static SystemMessageId S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION_SCORE;
	
	@ClientString(id = 1788, message = "The clan skill $s1 has been added.")
	public static SystemMessageId THE_CLAN_SKILL_S1_HAS_BEEN_ADDED;
	
	@ClientString(id = 1789, message = "Since the Clan Reputation Score has dropped to 0 or lower, your clan skill(s) will be de-activated.")
	public static SystemMessageId SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILL_S_WILL_BE_DE_ACTIVATED;
	
	@ClientString(id = 1790, message = "The conditions necessary to increase the clan's level have not been met.")
	public static SystemMessageId THE_CONDITIONS_NECESSARY_TO_INCREASE_THE_CLAN_S_LEVEL_HAVE_NOT_BEEN_MET;
	
	@ClientString(id = 1791, message = "The conditions necessary to create a military unit have not been met.")
	public static SystemMessageId THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET;
	
	@ClientString(id = 1792, message = "Please assign a manager for your new Order of Knights.")
	public static SystemMessageId PLEASE_ASSIGN_A_MANAGER_FOR_YOUR_NEW_ORDER_OF_KNIGHTS;
	
	@ClientString(id = 1793, message = "$s1 has been selected as the captain of $s2.")
	public static SystemMessageId S1_HAS_BEEN_SELECTED_AS_THE_CAPTAIN_OF_S2;
	
	@ClientString(id = 1794, message = "The Knights of $s1 have been created.")
	public static SystemMessageId THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED;
	
	@ClientString(id = 1795, message = "The Royal Guard of $s1 have been created.")
	public static SystemMessageId THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED;
	
	@ClientString(id = 1796, message = "Your account has been temporarily suspended because of involvement in account theft or other abnormal game play which has harmed or inconvenienced other players. If you feel that you were not involved with any of these violations, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_TEMPORARILY_SUSPENDED_BECAUSE_OF_INVOLVEMENT_IN_ACCOUNT_THEFT_OR_OTHER_ABNORMAL_GAME_PLAY_WHICH_HAS_HARMED_OR_INCONVENIENCED_OTHER_PLAYERS_IF_YOU_FEEL_THAT_YOU_WERE_NOT_INVOLVED_WITH_ANY_OF_THESE_VIOLATIONS_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1797, message = "$s1 has been promoted to $s2.")
	public static SystemMessageId S1_HAS_BEEN_PROMOTED_TO_S2;
	
	@ClientString(id = 1798, message = "Clan lord privileges have been transferred to $s1.")
	public static SystemMessageId CLAN_LORD_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1;
	
	@ClientString(id = 1799, message = "We are searching for BOT users. Please try again later.")
	public static SystemMessageId WE_ARE_SEARCHING_FOR_BOT_USERS_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 1800, message = "User $s1 has a history of using BOT.")
	public static SystemMessageId USER_S1_HAS_A_HISTORY_OF_USING_BOT;
	
	@ClientString(id = 1801, message = "The attempt to sell has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_SELL_HAS_FAILED;
	
	@ClientString(id = 1802, message = "The attempt to trade has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_TRADE_HAS_FAILED;
	
	@ClientString(id = 1803, message = "The request to participate in the game cannot be made starting from 10 minutes before the end of the game.")
	public static SystemMessageId THE_REQUEST_TO_PARTICIPATE_IN_THE_GAME_CANNOT_BE_MADE_STARTING_FROM_10_MINUTES_BEFORE_THE_END_OF_THE_GAME;
	
	@ClientString(id = 1804, message = "Your account has been suspended for 7 days because an illicit cash/account transaction has been detected. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_7_DAYS_BECAUSE_AN_ILLICIT_CASH_ACCOUNT_TRANSACTION_HAS_BEEN_DETECTED_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1805, message = "Your account has been suspended for 30 days because an illicit cash/account transaction has been detected for the second time. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_30_DAYS_BECAUSE_AN_ILLICIT_CASH_ACCOUNT_TRANSACTION_HAS_BEEN_DETECTED_FOR_THE_SECOND_TIME_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1806, message = "Your account has been permanently suspended because an illicit cash/account transaction has been detected for the third time. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_PERMANENTLY_SUSPENDED_BECAUSE_AN_ILLICIT_CASH_ACCOUNT_TRANSACTION_HAS_BEEN_DETECTED_FOR_THE_THIRD_TIME_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1807, message = "Your account has been suspended for 30 days because of your involvement in an illicit cash transaction. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_30_DAYS_BECAUSE_OF_YOUR_INVOLVEMENT_IN_AN_ILLICIT_CASH_TRANSACTION_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1808, message = "Your account has been permanently suspended because of your involvement in an illicit cash/account transaction. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_PERMANENTLY_SUSPENDED_BECAUSE_OF_YOUR_INVOLVEMENT_IN_AN_ILLICIT_CASH_ACCOUNT_TRANSACTION_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1809, message = "Your account must be verified. For information on verification procedures, please visit the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_MUST_BE_VERIFIED_FOR_INFORMATION_ON_VERIFICATION_PROCEDURES_PLEASE_VISIT_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1810, message = "The refuse invitation state has been activated.")
	public static SystemMessageId THE_REFUSE_INVITATION_STATE_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 1811, message = "The refuse invitation state has been removed.")
	public static SystemMessageId THE_REFUSE_INVITATION_STATE_HAS_BEEN_REMOVED;
	
	@ClientString(id = 1812, message = "Since the refuse invitation state is currently activated, no invitation can be made.")
	public static SystemMessageId SINCE_THE_REFUSE_INVITATION_STATE_IS_CURRENTLY_ACTIVATED_NO_INVITATION_CAN_BE_MADE;
	
	@ClientString(id = 1813, message = "$s1 has $s2 hour(s) of usage time remaining.")
	public static SystemMessageId S1_HAS_S2_HOUR_S_OF_USAGE_TIME_REMAINING;
	
	@ClientString(id = 1814, message = "$s1 has $s2 minute(s) of usage time remaining.")
	public static SystemMessageId S1_HAS_S2_MINUTE_S_OF_USAGE_TIME_REMAINING;
	
	@ClientString(id = 1815, message = "$s2 was dropped in the $s1 region.")
	public static SystemMessageId S2_WAS_DROPPED_IN_THE_S1_REGION;
	
	@ClientString(id = 1816, message = "The owner of $s2 has appeared in the $s1 region.")
	public static SystemMessageId THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION;
	
	@ClientString(id = 1817, message = "$s2's owner has logged into the $s1 region.")
	public static SystemMessageId S2_S_OWNER_HAS_LOGGED_INTO_THE_S1_REGION;
	
	@ClientString(id = 1818, message = "$s1 has disappeared.")
	public static SystemMessageId S1_HAS_DISAPPEARED_2;
	
	@ClientString(id = 1819, message = "An evil is pulsating from $s2 in $s1.")
	public static SystemMessageId AN_EVIL_IS_PULSATING_FROM_S2_IN_S1;
	
	@ClientString(id = 1820, message = "$s1 is currently asleep.")
	public static SystemMessageId S1_IS_CURRENTLY_ASLEEP;
	
	@ClientString(id = 1821, message = "$s2's evil presence is felt in $s1.")
	public static SystemMessageId S2_S_EVIL_PRESENCE_IS_FELT_IN_S1;
	
	@ClientString(id = 1822, message = "$s1 has been sealed.")
	public static SystemMessageId S1_HAS_BEEN_SEALED;
	
	@ClientString(id = 1823, message = "The registration period for a clan hall war has ended.")
	public static SystemMessageId THE_REGISTRATION_PERIOD_FOR_A_CLAN_HALL_WAR_HAS_ENDED;
	
	@ClientString(id = 1824, message = "You have been registered for a clan hall war. Please move to the left side of the clan hall's arena and get ready.")
	public static SystemMessageId YOU_HAVE_BEEN_REGISTERED_FOR_A_CLAN_HALL_WAR_PLEASE_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALL_S_ARENA_AND_GET_READY;
	
	@ClientString(id = 1825, message = "You have failed in your attempt to register for the clan hall war. Please try again.")
	public static SystemMessageId YOU_HAVE_FAILED_IN_YOUR_ATTEMPT_TO_REGISTER_FOR_THE_CLAN_HALL_WAR_PLEASE_TRY_AGAIN;
	
	@ClientString(id = 1826, message = "In $s1 minute(s), the game will begin. All players must hurry and move to the left side of the clan hall's arena.")
	public static SystemMessageId IN_S1_MINUTE_S_THE_GAME_WILL_BEGIN_ALL_PLAYERS_MUST_HURRY_AND_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALL_S_ARENA;
	
	@ClientString(id = 1827, message = "In $s1 minute(s), the game will begin. All players, please enter the arena now.")
	public static SystemMessageId IN_S1_MINUTE_S_THE_GAME_WILL_BEGIN_ALL_PLAYERS_PLEASE_ENTER_THE_ARENA_NOW;
	
	@ClientString(id = 1828, message = "In $s1 second(s), the game will begin.")
	public static SystemMessageId IN_S1_SECOND_S_THE_GAME_WILL_BEGIN;
	
	@ClientString(id = 1829, message = "The Command Channel is full.")
	public static SystemMessageId THE_COMMAND_CHANNEL_IS_FULL;
	
	@ClientString(id = 1830, message = "$s1 is not allowed to use the party room invite command. Please update the waiting list.")
	public static SystemMessageId S1_IS_NOT_ALLOWED_TO_USE_THE_PARTY_ROOM_INVITE_COMMAND_PLEASE_UPDATE_THE_WAITING_LIST;
	
	@ClientString(id = 1831, message = "$s1 does not meet the conditions of the party room. Please update the waiting list.")
	public static SystemMessageId S1_DOES_NOT_MEET_THE_CONDITIONS_OF_THE_PARTY_ROOM_PLEASE_UPDATE_THE_WAITING_LIST;
	
	@ClientString(id = 1832, message = "Only a room leader may invite others to a party room.")
	public static SystemMessageId ONLY_A_ROOM_LEADER_MAY_INVITE_OTHERS_TO_A_PARTY_ROOM;
	
	@ClientString(id = 1833, message = "All of $s1 will be dropped. Would you like to continue?")
	public static SystemMessageId ALL_OF_S1_WILL_BE_DROPPED_WOULD_YOU_LIKE_TO_CONTINUE;
	
	@ClientString(id = 1834, message = "The party room is full. No more characters can be invited in.")
	public static SystemMessageId THE_PARTY_ROOM_IS_FULL_NO_MORE_CHARACTERS_CAN_BE_INVITED_IN;
	
	@ClientString(id = 1835, message = "$s1 is full and cannot accept additional clan members at this time.")
	public static SystemMessageId S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME;
	
	@ClientString(id = 1836, message = "You cannot join a Clan Academy because you have successfully completed your 2nd class transfer.")
	public static SystemMessageId YOU_CANNOT_JOIN_A_CLAN_ACADEMY_BECAUSE_YOU_HAVE_SUCCESSFULLY_COMPLETED_YOUR_2ND_CLASS_TRANSFER;
	
	@ClientString(id = 1837, message = "$s1 has sent you an invitation to join the $s3 Royal Guard under the $s2 clan. Would you like to join?")
	public static SystemMessageId S1_HAS_SENT_YOU_AN_INVITATION_TO_JOIN_THE_S3_ROYAL_GUARD_UNDER_THE_S2_CLAN_WOULD_YOU_LIKE_TO_JOIN;
	
	@ClientString(id = 1838, message = "1. The coupon can be used once per character.")
	public static SystemMessageId ONE_THE_COUPON_CAN_BE_USED_ONCE_PER_CHARACTER;
	
	@ClientString(id = 1839, message = "2. A used serial number may not be used again.")
	public static SystemMessageId TWO_A_USED_SERIAL_NUMBER_MAY_NOT_BE_USED_AGAIN;
	
	@ClientString(id = 1840, message = "3. If you enter the incorrect serial number more than 5 times,\\n you may use it again after a certain amount of time passes.")
	public static SystemMessageId THREE_IF_YOU_ENTER_THE_INCORRECT_SERIAL_NUMBER_MORE_THAN_5_TIMES_N_YOU_MAY_USE_IT_AGAIN_AFTER_A_CERTAIN_AMOUNT_OF_TIME_PASSES;
	
	@ClientString(id = 1841, message = "This clan hall war has been cancelled. Not enough clans have registered.")
	public static SystemMessageId THIS_CLAN_HALL_WAR_HAS_BEEN_CANCELLED_NOT_ENOUGH_CLANS_HAVE_REGISTERED;
	
	@ClientString(id = 1842, message = "$s1 wishes to summon you from $s2. Do you accept?")
	public static SystemMessageId S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT;
	
	@ClientString(id = 1843, message = "$s1 is engaged in combat and cannot be summoned.")
	public static SystemMessageId S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED;
	
	@ClientString(id = 1844, message = "$s1 is dead at the moment and cannot be summoned.")
	public static SystemMessageId S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED;
	
	@ClientString(id = 1845, message = "Hero weapons cannot be destroyed.")
	public static SystemMessageId HERO_WEAPONS_CANNOT_BE_DESTROYED;
	
	@ClientString(id = 1846, message = "You are too far away from the Strider to mount it.")
	public static SystemMessageId YOU_ARE_TOO_FAR_AWAY_FROM_THE_STRIDER_TO_MOUNT_IT;
	
	@ClientString(id = 1847, message = "You caught a fish $s1 in length.")
	public static SystemMessageId YOU_CAUGHT_A_FISH_S1_IN_LENGTH;
	
	@ClientString(id = 1848, message = "Because of the size of fish caught, you will be registered in the ranking.")
	public static SystemMessageId BECAUSE_OF_THE_SIZE_OF_FISH_CAUGHT_YOU_WILL_BE_REGISTERED_IN_THE_RANKING;
	
	@ClientString(id = 1849, message = "All of $s1 will be discarded. Would you like to continue?")
	public static SystemMessageId ALL_OF_S1_WILL_BE_DISCARDED_WOULD_YOU_LIKE_TO_CONTINUE;
	
	@ClientString(id = 1850, message = "The Captain of the Order of Knights cannot be appointed.")
	public static SystemMessageId THE_CAPTAIN_OF_THE_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED;
	
	@ClientString(id = 1851, message = "The Captain of the Royal Guard cannot be appointed.")
	public static SystemMessageId THE_CAPTAIN_OF_THE_ROYAL_GUARD_CANNOT_BE_APPOINTED;
	
	@ClientString(id = 1852, message = "The attempt to acquire the skill has failed because of an insufficient Clan Reputation Score.")
	public static SystemMessageId THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION_SCORE;
	
	@ClientString(id = 1853, message = "Quantity items of the same type cannot be exchanged at the same time.")
	public static SystemMessageId QUANTITY_ITEMS_OF_THE_SAME_TYPE_CANNOT_BE_EXCHANGED_AT_THE_SAME_TIME;
	
	@ClientString(id = 1854, message = "The item was converted successfully.")
	public static SystemMessageId THE_ITEM_WAS_CONVERTED_SUCCESSFULLY;
	
	@ClientString(id = 1855, message = "Another military unit is already using that name. Please enter a different name.")
	public static SystemMessageId ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME;
	
	@ClientString(id = 1856, message = "Since your opponent is now the owner of $s1, the Olympiad has been cancelled.")
	public static SystemMessageId SINCE_YOUR_OPPONENT_IS_NOW_THE_OWNER_OF_S1_THE_OLYMPIAD_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 1857, message = "Since you now own $s1, you cannot participate in the Olympiad.")
	public static SystemMessageId SINCE_YOU_NOW_OWN_S1_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD;
	
	@ClientString(id = 1858, message = "You cannot participate in the Olympiad while dead.")
	public static SystemMessageId YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_WHILE_DEAD;
	
	@ClientString(id = 1859, message = "You have exceeded the quantity that can be moved at one time.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_MOVED_AT_ONE_TIME;
	
	@ClientString(id = 1860, message = "The Clan Reputation Score is too low.")
	public static SystemMessageId THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW;
	
	@ClientString(id = 1861, message = "The clan's crest has been deleted.")
	public static SystemMessageId THE_CLAN_S_CREST_HAS_BEEN_DELETED;
	
	@ClientString(id = 1862, message = "Clan skills will now be activated since the clan's reputation score is 0 or higher.")
	public static SystemMessageId CLAN_SKILLS_WILL_NOW_BE_ACTIVATED_SINCE_THE_CLAN_S_REPUTATION_SCORE_IS_0_OR_HIGHER;
	
	@ClientString(id = 1863, message = "$s1 purchased a clan item, reducing the Clan Reputation by $s2 points.")
	public static SystemMessageId S1_PURCHASED_A_CLAN_ITEM_REDUCING_THE_CLAN_REPUTATION_BY_S2_POINTS;
	
	@ClientString(id = 1864, message = "Your pet/servitor is unresponsive and will not obey any orders.")
	public static SystemMessageId YOUR_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS;
	
	@ClientString(id = 1865, message = "Your pet/servitor is currently in a state of distress.")
	public static SystemMessageId YOUR_PET_SERVITOR_IS_CURRENTLY_IN_A_STATE_OF_DISTRESS;
	
	@ClientString(id = 1866, message = "MP was reduced by $s1.")
	public static SystemMessageId MP_WAS_REDUCED_BY_S1;
	
	@ClientString(id = 1867, message = "Your opponent's MP was reduced by $s1.")
	public static SystemMessageId YOUR_OPPONENT_S_MP_WAS_REDUCED_BY_S1;
	
	@ClientString(id = 1868, message = "You cannot exchange an item while it is being used.")
	public static SystemMessageId YOU_CANNOT_EXCHANGE_AN_ITEM_WHILE_IT_IS_BEING_USED;
	
	@ClientString(id = 1869, message = "$s1's Command Channel has looting rights.")
	public static SystemMessageId S1_S_COMMAND_CHANNEL_HAS_LOOTING_RIGHTS;
	
	@ClientString(id = 1870, message = "A Command Channel with looting rights already exists.")
	public static SystemMessageId A_COMMAND_CHANNEL_WITH_LOOTING_RIGHTS_ALREADY_EXISTS;
	
	@ClientString(id = 1871, message = "Do you want to dismiss $s1 from the clan?")
	public static SystemMessageId DO_YOU_WANT_TO_DISMISS_S1_FROM_THE_CLAN;
	
	@ClientString(id = 1872, message = "You have $s1 hour(s) and $s2 minute(s) left.")
	public static SystemMessageId YOU_HAVE_S1_HOUR_S_AND_S2_MINUTE_S_LEFT;
	
	@ClientString(id = 1873, message = "There are $s1 hour(s) and $s2 minute(s) left in the fixed use time for this PC Café.")
	public static SystemMessageId THERE_ARE_S1_HOUR_S_AND_S2_MINUTE_S_LEFT_IN_THE_FIXED_USE_TIME_FOR_THIS_PC_CAF;
	
	@ClientString(id = 1874, message = "There are $s1 minute(s) left for this individual user.")
	public static SystemMessageId THERE_ARE_S1_MINUTE_S_LEFT_FOR_THIS_INDIVIDUAL_USER;
	
	@ClientString(id = 1875, message = "There are $s1 minute(s) left in the fixed use time for this PC Café.")
	public static SystemMessageId THERE_ARE_S1_MINUTE_S_LEFT_IN_THE_FIXED_USE_TIME_FOR_THIS_PC_CAF;
	
	@ClientString(id = 1876, message = "Do you want to leave $s1 clan?")
	public static SystemMessageId DO_YOU_WANT_TO_LEAVE_S1_CLAN;
	
	@ClientString(id = 1877, message = "The game will end in $s1 minute(s).")
	public static SystemMessageId THE_GAME_WILL_END_IN_S1_MINUTE_S;
	
	@ClientString(id = 1878, message = "The game will end in $s1 second(s).")
	public static SystemMessageId THE_GAME_WILL_END_IN_S1_SECOND_S;
	
	@ClientString(id = 1879, message = "In $s1 minute(s), you will be teleported outside of the game arena.")
	public static SystemMessageId IN_S1_MINUTE_S_YOU_WILL_BE_TELEPORTED_OUTSIDE_OF_THE_GAME_ARENA;
	
	@ClientString(id = 1880, message = "In $s1 second(s), you will be teleported outside of the game arena.")
	public static SystemMessageId IN_S1_SECOND_S_YOU_WILL_BE_TELEPORTED_OUTSIDE_OF_THE_GAME_ARENA;
	
	@ClientString(id = 1881, message = "The preliminary match will begin in $s1 second(s). Prepare yourself.")
	public static SystemMessageId THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECOND_S_PREPARE_YOURSELF;
	
	@ClientString(id = 1882, message = "Characters cannot be created from this server.")
	public static SystemMessageId CHARACTERS_CANNOT_BE_CREATED_FROM_THIS_SERVER;
	
	@ClientString(id = 1883, message = "There are no offerings I own or I made a bid for.")
	public static SystemMessageId THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR;
	
	@ClientString(id = 1884, message = "Enter the PC Room coupon serial number:")
	public static SystemMessageId ENTER_THE_PC_ROOM_COUPON_SERIAL_NUMBER;
	
	@ClientString(id = 1885, message = "This serial number cannot be entered. Please try again in $s1 minute(s).")
	public static SystemMessageId THIS_SERIAL_NUMBER_CANNOT_BE_ENTERED_PLEASE_TRY_AGAIN_IN_S1_MINUTE_S;
	
	@ClientString(id = 1886, message = "This serial number has already been used.")
	public static SystemMessageId THIS_SERIAL_NUMBER_HAS_ALREADY_BEEN_USED;
	
	@ClientString(id = 1887, message = "Invalid serial number. Your attempt to enter the number has failed $s1 time(s). You will be allowed to make $s2 more attempt(s).")
	public static SystemMessageId INVALID_SERIAL_NUMBER_YOUR_ATTEMPT_TO_ENTER_THE_NUMBER_HAS_FAILED_S1_TIME_S_YOU_WILL_BE_ALLOWED_TO_MAKE_S2_MORE_ATTEMPT_S;
	
	@ClientString(id = 1888, message = "Invalid serial number. Your attempt to enter the number has failed 5 times. Please try again in 4 hours.")
	public static SystemMessageId INVALID_SERIAL_NUMBER_YOUR_ATTEMPT_TO_ENTER_THE_NUMBER_HAS_FAILED_5_TIMES_PLEASE_TRY_AGAIN_IN_4_HOURS;
	
	@ClientString(id = 1889, message = "Congratulations. You have received $s1.")
	public static SystemMessageId CONGRATULATIONS_YOU_HAVE_RECEIVED_S1;
	
	@ClientString(id = 1890, message = "Since you have already used this coupon, you may not use this serial number.")
	public static SystemMessageId SINCE_YOU_HAVE_ALREADY_USED_THIS_COUPON_YOU_MAY_NOT_USE_THIS_SERIAL_NUMBER;
	
	@ClientString(id = 1891, message = "You may not use items in a private store or private work shop.")
	public static SystemMessageId YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP;
	
	@ClientString(id = 1892, message = "The replay file for the previous version cannot be played.")
	public static SystemMessageId THE_REPLAY_FILE_FOR_THE_PREVIOUS_VERSION_CANNOT_BE_PLAYED;
	
	@ClientString(id = 1893, message = "This file cannot be replayed.")
	public static SystemMessageId THIS_FILE_CANNOT_BE_REPLAYED;
	
	@ClientString(id = 1894, message = "A sub-class cannot be created or changed while you are over your weight limit.")
	public static SystemMessageId A_SUB_CLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT;
	
	@ClientString(id = 1895, message = "$s1 is in an area which blocks summoning.")
	public static SystemMessageId S1_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
	
	@ClientString(id = 1896, message = "$s1 has already been summoned!")
	public static SystemMessageId S1_HAS_ALREADY_BEEN_SUMMONED;
	
	@ClientString(id = 1897, message = "$s1 is required for summoning.")
	public static SystemMessageId S1_IS_REQUIRED_FOR_SUMMONING;
	
	@ClientString(id = 1898, message = "$s1 is currently trading or operating a private store and cannot be summoned.")
	public static SystemMessageId S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED;
	
	@ClientString(id = 1899, message = "Your target is in an area which blocks summoning.")
	public static SystemMessageId YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;
	
	@ClientString(id = 1900, message = "$s1 has entered the party room.")
	public static SystemMessageId S1_HAS_ENTERED_THE_PARTY_ROOM;
	
	@ClientString(id = 1901, message = "$s1 has invited you to enter the party room.")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_ENTER_THE_PARTY_ROOM;
	
	@ClientString(id = 1902, message = "Incompatible item grade. This item cannot be used.")
	public static SystemMessageId INCOMPATIBLE_ITEM_GRADE_THIS_ITEM_CANNOT_BE_USED;
	
	@ClientString(id = 1903, message = "Those of you who have requested NCOTP should run NCOTP \\n by using your cell phone to get the NCOTP \\n password and enter it within 1 minute.\\n If you have not requested NCOTP, leave this field blank and\\n click the Login button.")
	public static SystemMessageId THOSE_OF_YOU_WHO_HAVE_REQUESTED_NCOTP_SHOULD_RUN_NCOTP_N_BY_USING_YOUR_CELL_PHONE_TO_GET_THE_NCOTP_N_PASSWORD_AND_ENTER_IT_WITHIN_1_MINUTE_N_IF_YOU_HAVE_NOT_REQUESTED_NCOTP_LEAVE_THIS_FIELD_BLANK_AND_N_CLICK_THE_LOGIN_BUTTON;
	
	@ClientString(id = 1904, message = "A sub-class may not be created or changed while a servitor or pet is summoned.")
	public static SystemMessageId A_SUB_CLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED;
	
	@ClientString(id = 1905, message = "$s2 of $s1 will be replaced with $s4 of $s3.")
	public static SystemMessageId S2_OF_S1_WILL_BE_REPLACED_WITH_S4_OF_S3;
	
	@ClientString(id = 1906, message = "Select the combat unit\\n to transfer to.")
	public static SystemMessageId SELECT_THE_COMBAT_UNIT_N_TO_TRANSFER_TO;
	
	@ClientString(id = 1907, message = "Select the the character who will\\n replace the current character.")
	public static SystemMessageId SELECT_THE_THE_CHARACTER_WHO_WILL_N_REPLACE_THE_CURRENT_CHARACTER;
	
	@ClientString(id = 1908, message = "$s1 is in a state which prevents summoning.")
	public static SystemMessageId S1_IS_IN_A_STATE_WHICH_PREVENTS_SUMMONING;
	
	@ClientString(id = 1909, message = "==< List of Clan Academy Graduates During the Past Week >==")
	public static SystemMessageId LIST_OF_CLAN_ACADEMY_GRADUATES_DURING_THE_PAST_WEEK;
	
	@ClientString(id = 1910, message = "Graduates: $s1")
	public static SystemMessageId GRADUATES_S1;
	
	@ClientString(id = 1911, message = "You cannot summon players who are currently participating in the Grand Olympiad.")
	public static SystemMessageId YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD;
	
	@ClientString(id = 1912, message = "Only those requesting NCOTP should make an entry into this field.")
	public static SystemMessageId ONLY_THOSE_REQUESTING_NCOTP_SHOULD_MAKE_AN_ENTRY_INTO_THIS_FIELD;
	
	@ClientString(id = 1913, message = "The remaining recycle time for $s1 is $s2 minute(s).")
	public static SystemMessageId THE_REMAINING_RECYCLE_TIME_FOR_S1_IS_S2_MINUTE_S;
	
	@ClientString(id = 1914, message = "The remaining recycle time for $s1 is $s2 second(s).")
	public static SystemMessageId THE_REMAINING_RECYCLE_TIME_FOR_S1_IS_S2_SECOND_S;
	
	@ClientString(id = 1915, message = "The game will end in $s1 second(s).")
	public static SystemMessageId THE_GAME_WILL_END_IN_S1_SECOND_S_2;
	
	@ClientString(id = 1916, message = "Your Death Penalty is now level $s1.")
	public static SystemMessageId YOUR_DEATH_PENALTY_IS_NOW_LEVEL_S1;
	
	@ClientString(id = 1917, message = "Your Death Penalty has been lifted.")
	public static SystemMessageId YOUR_DEATH_PENALTY_HAS_BEEN_LIFTED;
	
	@ClientString(id = 1918, message = "Your pet is too high level to control.")
	public static SystemMessageId YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL;
	
	@ClientString(id = 1919, message = "The Grand Olympiad registration period has ended.")
	public static SystemMessageId THE_GRAND_OLYMPIAD_REGISTRATION_PERIOD_HAS_ENDED;
	
	@ClientString(id = 1920, message = "Your account is currently inactive because you have not logged into the game for some time. You may reactivate your account by visiting the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_IS_CURRENTLY_INACTIVE_BECAUSE_YOU_HAVE_NOT_LOGGED_INTO_THE_GAME_FOR_SOME_TIME_YOU_MAY_REACTIVATE_YOUR_ACCOUNT_BY_VISITING_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 1921, message = "$s2 hour(s) and $s3 minute(s) have passed since $s1 has killed.")
	public static SystemMessageId S2_HOUR_S_AND_S3_MINUTE_S_HAVE_PASSED_SINCE_S1_HAS_KILLED;
	
	@ClientString(id = 1922, message = "Because $s1 failed to kill for one full day, it has expired.")
	public static SystemMessageId BECAUSE_S1_FAILED_TO_KILL_FOR_ONE_FULL_DAY_IT_HAS_EXPIRED;
	
	@ClientString(id = 1923, message = "Court Magician: The portal has been created!")
	public static SystemMessageId COURT_MAGICIAN_THE_PORTAL_HAS_BEEN_CREATED;
	
	@ClientString(id = 1924, message = "Current Location: $s1, $s2, $s3 (near the Primeval Isle)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_PRIMEVAL_ISLE;
	
	@ClientString(id = 1925, message = "Due to the affects of the Seal of Strife, it is not possible to summon at this time.")
	public static SystemMessageId DUE_TO_THE_AFFECTS_OF_THE_SEAL_OF_STRIFE_IT_IS_NOT_POSSIBLE_TO_SUMMON_AT_THIS_TIME;
	
	@ClientString(id = 1926, message = "There is no opponent to receive your challenge for a duel.")
	public static SystemMessageId THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	@ClientString(id = 1927, message = "$s1 has been challenged to a duel.")
	public static SystemMessageId S1_HAS_BEEN_CHALLENGED_TO_A_DUEL;
	
	@ClientString(id = 1928, message = "$s1's party has been challenged to a duel.")
	public static SystemMessageId S1_S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL;
	
	@ClientString(id = 1929, message = "$s1 has accepted your challenge to a duel. The duel will begin in a few moments.")
	public static SystemMessageId S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	@ClientString(id = 1930, message = "You have accepted $s1's challenge to a duel. The duel will begin in a few moments.")
	public static SystemMessageId YOU_HAVE_ACCEPTED_S1_S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	@ClientString(id = 1931, message = "$s1 has declined your challenge to a duel.")
	public static SystemMessageId S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL;
	
	@ClientString(id = 1932, message = "$s1 has declined your challenge to a duel.")
	public static SystemMessageId S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL_2;
	
	@ClientString(id = 1933, message = "You have accepted $s1's challenge to a party duel. The duel will begin in a few moments.")
	public static SystemMessageId YOU_HAVE_ACCEPTED_S1_S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	@ClientString(id = 1934, message = "$s1 has accepted your challenge to duel against their party. The duel will begin in a few moments.")
	public static SystemMessageId S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS;
	
	@ClientString(id = 1935, message = "$s1 has declined your challenge to a duel.")
	public static SystemMessageId S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL_3;
	
	@ClientString(id = 1936, message = "The opposing party has declined your challenge to a duel.")
	public static SystemMessageId THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL;
	
	@ClientString(id = 1937, message = "Since the person you challenged is not currently in a party, they cannot duel against your party.")
	public static SystemMessageId SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY;
	
	@ClientString(id = 1938, message = "$s1 has challenged you to a duel.")
	public static SystemMessageId S1_HAS_CHALLENGED_YOU_TO_A_DUEL;
	
	@ClientString(id = 1939, message = "$s1's party has challenged your party to a duel.")
	public static SystemMessageId S1_S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL;
	
	@ClientString(id = 1940, message = "You are unable to request a duel at this time.")
	public static SystemMessageId YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME;
	
	@ClientString(id = 1941, message = "This is not a suitable place to challenge anyone or party to a duel.")
	public static SystemMessageId THIS_IS_NOT_A_SUITABLE_PLACE_TO_CHALLENGE_ANYONE_OR_PARTY_TO_A_DUEL;
	
	@ClientString(id = 1942, message = "The opposing party is currently unable to accept a challenge to a duel.")
	public static SystemMessageId THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL;
	
	@ClientString(id = 1943, message = "The opposing party is currently not in a suitable location for a duel.")
	public static SystemMessageId THE_OPPOSING_PARTY_IS_CURRENTLY_NOT_IN_A_SUITABLE_LOCATION_FOR_A_DUEL;
	
	@ClientString(id = 1944, message = "In a moment, you will be transported to the site where the duel will take place.")
	public static SystemMessageId IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE;
	
	@ClientString(id = 1945, message = "The duel will begin in $s1 second(s).")
	public static SystemMessageId THE_DUEL_WILL_BEGIN_IN_S1_SECOND_S;
	
	@ClientString(id = 1946, message = "$s1 has challenged you to a duel. Will you accept?")
	public static SystemMessageId S1_HAS_CHALLENGED_YOU_TO_A_DUEL_WILL_YOU_ACCEPT;
	
	@ClientString(id = 1947, message = "$s1's party has challenged your party to a duel. Will you accept?")
	public static SystemMessageId S1_S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL_WILL_YOU_ACCEPT;
	
	@ClientString(id = 1948, message = "The duel will begin in $s1 second(s).")
	public static SystemMessageId THE_DUEL_WILL_BEGIN_IN_S1_SECOND_S_2;
	
	@ClientString(id = 1949, message = "Let the duel begin!")
	public static SystemMessageId LET_THE_DUEL_BEGIN;
	
	@ClientString(id = 1950, message = "$s1 has won the duel.")
	public static SystemMessageId S1_HAS_WON_THE_DUEL;
	
	@ClientString(id = 1951, message = "$s1's party has won the duel.")
	public static SystemMessageId S1_S_PARTY_HAS_WON_THE_DUEL;
	
	@ClientString(id = 1952, message = "The duel has ended in a tie.")
	public static SystemMessageId THE_DUEL_HAS_ENDED_IN_A_TIE;
	
	@ClientString(id = 1953, message = "Since $s1 was disqualified, $s2 has won.")
	public static SystemMessageId SINCE_S1_WAS_DISQUALIFIED_S2_HAS_WON;
	
	@ClientString(id = 1954, message = "Since $s1's party was disqualified, $s2's party has won.")
	public static SystemMessageId SINCE_S1_S_PARTY_WAS_DISQUALIFIED_S2_S_PARTY_HAS_WON;
	
	@ClientString(id = 1955, message = "Since $s1 withdrew from the duel, $s2 has won.")
	public static SystemMessageId SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON;
	
	@ClientString(id = 1956, message = "Since $s1's party withdrew from the duel, $s2's party has won.")
	public static SystemMessageId SINCE_S1_S_PARTY_WITHDREW_FROM_THE_DUEL_S2_S_PARTY_HAS_WON;
	
	@ClientString(id = 1957, message = "Select the item to be augmented.")
	public static SystemMessageId SELECT_THE_ITEM_TO_BE_AUGMENTED;
	
	@ClientString(id = 1958, message = "Select the catalyst for augmentation.")
	public static SystemMessageId SELECT_THE_CATALYST_FOR_AUGMENTATION;
	
	@ClientString(id = 1959, message = "Requires $s2 $s1.")
	public static SystemMessageId REQUIRES_S2_S1;
	
	@ClientString(id = 1960, message = "This is not a suitable item.")
	public static SystemMessageId THIS_IS_NOT_A_SUITABLE_ITEM;
	
	@ClientString(id = 1961, message = "Gemstone quantity is incorrect.")
	public static SystemMessageId GEMSTONE_QUANTITY_IS_INCORRECT;
	
	@ClientString(id = 1962, message = "The item was successfully augmented!")
	public static SystemMessageId THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED;
	
	@ClientString(id = 1963, message = "Select the item from which you wish to remove augmentation.")
	public static SystemMessageId SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION;
	
	@ClientString(id = 1964, message = "Augmentation removal can only be done on an augmented item.")
	public static SystemMessageId AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM;
	
	@ClientString(id = 1965, message = "Augmentation has been successfully removed from your $s1.")
	public static SystemMessageId AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1;
	
	@ClientString(id = 1966, message = "Only the clan leader may issue commands.")
	public static SystemMessageId ONLY_THE_CLAN_LEADER_MAY_ISSUE_COMMANDS;
	
	@ClientString(id = 1967, message = "The gate is firmly locked. Please try again later.")
	public static SystemMessageId THE_GATE_IS_FIRMLY_LOCKED_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 1968, message = "$s1's owner.")
	public static SystemMessageId S1_S_OWNER;
	
	@ClientString(id = 1969, message = "Area where $s1 appears.")
	public static SystemMessageId AREA_WHERE_S1_APPEARS;
	
	@ClientString(id = 1970, message = "Once an item is augmented, it cannot be augmented again.")
	public static SystemMessageId ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN;
	
	@ClientString(id = 1971, message = "The level of the hardener is too high to be used.")
	public static SystemMessageId THE_LEVEL_OF_THE_HARDENER_IS_TOO_HIGH_TO_BE_USED;
	
	@ClientString(id = 1972, message = "You cannot augment items while a private store or private workshop is in operation.")
	public static SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION;
	
	@ClientString(id = 1973, message = "You cannot augment items while frozen.")
	public static SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN;
	
	@ClientString(id = 1974, message = "You cannot augment items while dead.")
	public static SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD;
	
	@ClientString(id = 1975, message = "You cannot augment items while engaged in trade activities.")
	public static SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_ENGAGED_IN_TRADE_ACTIVITIES;
	
	@ClientString(id = 1976, message = "You cannot augment items while paralyzed.")
	public static SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED;
	
	@ClientString(id = 1977, message = "You cannot augment items while fishing.")
	public static SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING;
	
	@ClientString(id = 1978, message = "You cannot augment items while sitting down.")
	public static SystemMessageId YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN;
	
	@ClientString(id = 1979, message = "$s1's remaining Mana is now 10.")
	public static SystemMessageId S1_S_REMAINING_MANA_IS_NOW_10;
	
	@ClientString(id = 1980, message = "$s1's remaining Mana is now 5.")
	public static SystemMessageId S1_S_REMAINING_MANA_IS_NOW_5;
	
	@ClientString(id = 1981, message = "$s1's remaining Mana is now 1. It will disappear soon.")
	public static SystemMessageId S1_S_REMAINING_MANA_IS_NOW_1_IT_WILL_DISAPPEAR_SOON;
	
	@ClientString(id = 1982, message = "$s1's remaining Mana is now 0, and the item has disappeared.")
	public static SystemMessageId S1_S_REMAINING_MANA_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED;
	
	@ClientString(id = 1983, message = "$s1")
	public static SystemMessageId S1_2;
	
	@ClientString(id = 1984, message = "Press the Augment button to begin.")
	public static SystemMessageId PRESS_THE_AUGMENT_BUTTON_TO_BEGIN;
	
	@ClientString(id = 1985, message = "$s1's drop area ($s2)")
	public static SystemMessageId S1_S_DROP_AREA_S2;
	
	@ClientString(id = 1986, message = "$s1's owner ($s2)")
	public static SystemMessageId S1_S_OWNER_S2;
	
	@ClientString(id = 1987, message = "$s1")
	public static SystemMessageId S1_3;
	
	@ClientString(id = 1988, message = "The ferry has arrived at Primeval Isle.")
	public static SystemMessageId THE_FERRY_HAS_ARRIVED_AT_PRIMEVAL_ISLE;
	
	@ClientString(id = 1989, message = "The ferry will leave for Rune Harbor after anchoring for three minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_RUNE_HARBOR_AFTER_ANCHORING_FOR_THREE_MINUTES;
	
	@ClientString(id = 1990, message = "The ferry is now departing Primeval Isle for Rune Harbor.")
	public static SystemMessageId THE_FERRY_IS_NOW_DEPARTING_PRIMEVAL_ISLE_FOR_RUNE_HARBOR;
	
	@ClientString(id = 1991, message = "The ferry will leave for Primeval Isle after anchoring for three minutes.")
	public static SystemMessageId THE_FERRY_WILL_LEAVE_FOR_PRIMEVAL_ISLE_AFTER_ANCHORING_FOR_THREE_MINUTES;
	
	@ClientString(id = 1992, message = "The ferry is now departing Rune Harbor for Primeval Isle.")
	public static SystemMessageId THE_FERRY_IS_NOW_DEPARTING_RUNE_HARBOR_FOR_PRIMEVAL_ISLE;
	
	@ClientString(id = 1993, message = "The ferry from Primeval Isle to Rune Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_PRIMEVAL_ISLE_TO_RUNE_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1994, message = "The ferry from Rune Harbor to Primeval Isle has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_TO_PRIMEVAL_ISLE_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1995, message = "$s1 channel filtering option")
	public static SystemMessageId S1_CHANNEL_FILTERING_OPTION;
	
	@ClientString(id = 1996, message = "The attack has been blocked.")
	public static SystemMessageId THE_ATTACK_HAS_BEEN_BLOCKED;
	
	@ClientString(id = 1997, message = "$s1 is performing a counter-attack.")
	public static SystemMessageId S1_IS_PERFORMING_A_COUNTER_ATTACK;
	
	@ClientString(id = 1998, message = "You counter-attack $s1's attack.")
	public static SystemMessageId YOU_COUNTER_ATTACK_S1_S_ATTACK;
	
	@ClientString(id = 1999, message = "$s1 dodges the attack.")
	public static SystemMessageId S1_DODGES_THE_ATTACK;
	
	@ClientString(id = 2000, message = "You dodge $s1's attack.")
	public static SystemMessageId YOU_DODGE_S1_S_ATTACK;
	
	@ClientString(id = 2001, message = "Augmentation failed due to inappropriate conditions.")
	public static SystemMessageId AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS;
	
	@ClientString(id = 2002, message = "Trap failed.")
	public static SystemMessageId TRAP_FAILED;
	
	@ClientString(id = 2003, message = "You obtained an ordinary material.")
	public static SystemMessageId YOU_OBTAINED_AN_ORDINARY_MATERIAL;
	
	@ClientString(id = 2004, message = "You obtained a rare material.")
	public static SystemMessageId YOU_OBTAINED_A_RARE_MATERIAL;
	
	@ClientString(id = 2005, message = "You obtained a unique material.")
	public static SystemMessageId YOU_OBTAINED_A_UNIQUE_MATERIAL;
	
	@ClientString(id = 2006, message = "You obtained the only material of this kind.")
	public static SystemMessageId YOU_OBTAINED_THE_ONLY_MATERIAL_OF_THIS_KIND;
	
	@ClientString(id = 2007, message = "Please enter the recipient's name.")
	public static SystemMessageId PLEASE_ENTER_THE_RECIPIENT_S_NAME;
	
	@ClientString(id = 2008, message = "Please enter the text.")
	public static SystemMessageId PLEASE_ENTER_THE_TEXT;
	
	@ClientString(id = 2009, message = "You cannot exceed 1500 characters.")
	public static SystemMessageId YOU_CANNOT_EXCEED_1500_CHARACTERS;
	
	@ClientString(id = 2010, message = "$s2 $s1")
	public static SystemMessageId S2_S1;
	
	@ClientString(id = 2011, message = "The augmented item cannot be discarded.")
	public static SystemMessageId THE_AUGMENTED_ITEM_CANNOT_BE_DISCARDED;
	
	@ClientString(id = 2012, message = "$s1 has been activated.")
	public static SystemMessageId S1_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 2013, message = "Your seed or remaining purchase amount is inadequate.")
	public static SystemMessageId YOUR_SEED_OR_REMAINING_PURCHASE_AMOUNT_IS_INADEQUATE;
	
	@ClientString(id = 2014, message = "You cannot proceed because the manor cannot accept any more crops. All crops have been returned and no adena withdrawn.")
	public static SystemMessageId YOU_CANNOT_PROCEED_BECAUSE_THE_MANOR_CANNOT_ACCEPT_ANY_MORE_CROPS_ALL_CROPS_HAVE_BEEN_RETURNED_AND_NO_ADENA_WITHDRAWN;
	
	@ClientString(id = 2015, message = "A skill is ready to be used again.")
	public static SystemMessageId A_SKILL_IS_READY_TO_BE_USED_AGAIN;
	
	@ClientString(id = 2016, message = "A skill is ready to be used again but its re-use counter time has increased.")
	public static SystemMessageId A_SKILL_IS_READY_TO_BE_USED_AGAIN_BUT_ITS_RE_USE_COUNTER_TIME_HAS_INCREASED;
	
	@ClientString(id = 2017, message = "$s1 cannot duel because $s1 is currently engaged in a private store or manufacture.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
	
	@ClientString(id = 2018, message = "$s1 cannot duel because $s1 is currently fishing.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
	
	@ClientString(id = 2019, message = "$s1 cannot duel because $s1's HP or MP is below 50 percent.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_S_HP_OR_MP_IS_BELOW_50_PERCENT;
	
	@ClientString(id = 2020, message = "$s1 cannot make a challenge to a duel because $s1 is currently in a duel-prohibited area (Peaceful Zone / Seven Signs Zone / Near Water / Restart Prohibited Area).")
	public static SystemMessageId S1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA_PEACEFUL_ZONE_SEVEN_SIGNS_ZONE_NEAR_WATER_RESTART_PROHIBITED_AREA;
	
	@ClientString(id = 2021, message = "$s1 cannot duel because $s1 is currently engaged in battle.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
	
	@ClientString(id = 2022, message = "$s1 cannot duel because $s1 is already engaged in a duel.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
	
	@ClientString(id = 2023, message = "$s1 cannot duel because $s1 is in a chaotic state.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
	
	@ClientString(id = 2024, message = "$s1 cannot duel because $s1 is participating in the Olympiad.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
	
	@ClientString(id = 2025, message = "$s1 cannot duel because $s1 is participating in a clan hall war.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR;
	
	@ClientString(id = 2026, message = "$s1 cannot duel because $s1 is participating in a siege war.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_SIEGE_WAR;
	
	@ClientString(id = 2027, message = "$s1 cannot duel because $s1 is currently riding a boat, wyvern, or strider.")
	public static SystemMessageId S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
	
	@ClientString(id = 2028, message = "$s1 cannot receive a duel challenge because $s1 is too far away.")
	public static SystemMessageId S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY;
	
	@ClientString(id = 2029, message = "You cannot participate in the Olympiad during teleport.")
	public static SystemMessageId YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_DURING_TELEPORT;
	
	@ClientString(id = 2030, message = "You are currently logging in.")
	public static SystemMessageId YOU_ARE_CURRENTLY_LOGGING_IN;
	
	@ClientString(id = 2031, message = "Please wait a moment.")
	public static SystemMessageId PLEASE_WAIT_A_MOMENT;
	
	@ClientString(id = 2032, message = "It is not the right time for purchasing the item.")
	public static SystemMessageId IT_IS_NOT_THE_RIGHT_TIME_FOR_PURCHASING_THE_ITEM;
	
	@ClientString(id = 2033, message = "A sub-class cannot be created or changed because you have exceeded your inventory limit.")
	public static SystemMessageId A_SUB_CLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT;
	
	@ClientString(id = 2034, message = "There are $s1 hours(s) and $s2 minute(s) remaining until the time when the item can be purchased.")
	public static SystemMessageId THERE_ARE_S1_HOURS_S_AND_S2_MINUTE_S_REMAINING_UNTIL_THE_TIME_WHEN_THE_ITEM_CAN_BE_PURCHASED;
	
	@ClientString(id = 2035, message = "There are $s1 minute(s) remaining until the time when the item can be purchased.")
	public static SystemMessageId THERE_ARE_S1_MINUTE_S_REMAINING_UNTIL_THE_TIME_WHEN_THE_ITEM_CAN_BE_PURCHASED;
	
	@ClientString(id = 2036, message = "Unable to invite because the party is locked.")
	public static SystemMessageId UNABLE_TO_INVITE_BECAUSE_THE_PARTY_IS_LOCKED;
	
	@ClientString(id = 2037, message = "Unable to create character. You are unable to create a new character on the selected server. A restriction is in place which restricts users from creating characters on different servers where no previous character exists. Please choose another server.")
	public static SystemMessageId UNABLE_TO_CREATE_CHARACTER_YOU_ARE_UNABLE_TO_CREATE_A_NEW_CHARACTER_ON_THE_SELECTED_SERVER_A_RESTRICTION_IS_IN_PLACE_WHICH_RESTRICTS_USERS_FROM_CREATING_CHARACTERS_ON_DIFFERENT_SERVERS_WHERE_NO_PREVIOUS_CHARACTER_EXISTS_PLEASE_CHOOSE_ANOTHER_SERVER;
	
	@ClientString(id = 2038, message = "This account cannot drop items.")
	public static SystemMessageId THIS_ACCOUNT_CANNOT_DROP_ITEMS;
	
	@ClientString(id = 2039, message = "This account cannot trade items.")
	public static SystemMessageId THIS_ACCOUNT_CANNOT_TRADE_ITEMS;
	
	@ClientString(id = 2040, message = "Cannot trade items with the targeted user.")
	public static SystemMessageId CANNOT_TRADE_ITEMS_WITH_THE_TARGETED_USER;
	
	@ClientString(id = 2041, message = "Cannot open a private store.")
	public static SystemMessageId CANNOT_OPEN_A_PRIVATE_STORE;
	
	@ClientString(id = 2042, message = "This account has been suspended for non-payment based on the cell phone payment agreement.\\n Please submit proof of payment by fax (02-2186-3499) and contact customer service at 1600-0020.")
	public static SystemMessageId THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_NON_PAYMENT_BASED_ON_THE_CELL_PHONE_PAYMENT_AGREEMENT_N_PLEASE_SUBMIT_PROOF_OF_PAYMENT_BY_FAX_02_2186_3499_AND_CONTACT_CUSTOMER_SERVICE_AT_1600_0020;
	
	@ClientString(id = 2043, message = "You have exceeded your inventory volume limit and may not take this quest item. Please make room in your inventory and try again.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_MAY_NOT_TAKE_THIS_QUEST_ITEM_PLEASE_MAKE_ROOM_IN_YOUR_INVENTORY_AND_TRY_AGAIN;
	
	@ClientString(id = 2044, message = "This account cannot set up private manufacture.")
	public static SystemMessageId THIS_ACCOUNT_CANNOT_SET_UP_PRIVATE_MANUFACTURE;
	
	@ClientString(id = 2045, message = "This account cannot use private manufacture.")
	public static SystemMessageId THIS_ACCOUNT_CANNOT_USE_PRIVATE_MANUFACTURE;
	
	@ClientString(id = 2046, message = "This account cannot use private stores.")
	public static SystemMessageId THIS_ACCOUNT_CANNOT_USE_PRIVATE_STORES;
	
	@ClientString(id = 2047, message = "This account cannot use the clan warehouse.")
	public static SystemMessageId THIS_ACCOUNT_CANNOT_USE_THE_CLAN_WAREHOUSE;
	
	@ClientString(id = 2051, message = "You must accept the User Agreement before this account can access Lineage II.\\n Please try again after accepting the agreement on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOU_MUST_ACCEPT_THE_USER_AGREEMENT_BEFORE_THIS_ACCOUNT_CAN_ACCESS_LINEAGE_II_N_PLEASE_TRY_AGAIN_AFTER_ACCEPTING_THE_AGREEMENT_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2052, message = "A guardian's consent is required before this account may be used to play Lineage II.\\nPlease try again after obtaining this consent.")
	public static SystemMessageId A_GUARDIAN_S_CONSENT_IS_REQUIRED_BEFORE_THIS_ACCOUNT_MAY_BE_USED_TO_PLAY_LINEAGE_II_NPLEASE_TRY_AGAIN_AFTER_OBTAINING_THIS_CONSENT;
	
	@ClientString(id = 2053, message = "This account has declined the User Agreement or a withdrawal request is pending for it.\\nPlease try again after canceling this request.")
	public static SystemMessageId THIS_ACCOUNT_HAS_DECLINED_THE_USER_AGREEMENT_OR_A_WITHDRAWAL_REQUEST_IS_PENDING_FOR_IT_NPLEASE_TRY_AGAIN_AFTER_CANCELING_THIS_REQUEST;
	
	@ClientString(id = 2054, message = "This account has been suspended.\\nFor more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId THIS_ACCOUNT_HAS_BEEN_SUSPENDED_NFOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2055, message = "This account has been suspended from all game services.\\nFor more information, please visit the PlayNC website Support Center (http://www.plaync.com/us/support/).")
	public static SystemMessageId THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FROM_ALL_GAME_SERVICES_NFOR_MORE_INFORMATION_PLEASE_VISIT_THE_PLAYNC_WEBSITE_SUPPORT_CENTER_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2056, message = "This account was converted into an integrated account and as a result may not access Lineage II.\\nPlease try again with the integrated account.")
	public static SystemMessageId THIS_ACCOUNT_WAS_CONVERTED_INTO_AN_INTEGRATED_ACCOUNT_AND_AS_A_RESULT_MAY_NOT_ACCESS_LINEAGE_II_NPLEASE_TRY_AGAIN_WITH_THE_INTEGRATED_ACCOUNT;
	
	@ClientString(id = 2057, message = "You have blocked $s1.")
	public static SystemMessageId YOU_HAVE_BLOCKED_S1;
	
	@ClientString(id = 2086, message = "Search user $s2 for third party program use completed in $s1 minute(s).")
	public static SystemMessageId SEARCH_USER_S2_FOR_THIRD_PARTY_PROGRAM_USE_COMPLETED_IN_S1_MINUTE_S;
	
	@ClientString(id = 2091, message = "Your account may only be used after changing your password and quiz. \\n To make the necessary changes, please visit the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_MAY_ONLY_BE_USED_AFTER_CHANGING_YOUR_PASSWORD_AND_QUIZ_N_TO_MAKE_THE_NECESSARY_CHANGES_PLEASE_VISIT_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2092, message = "You cannot bid due to a passed-in price.")
	public static SystemMessageId YOU_CANNOT_BID_DUE_TO_A_PASSED_IN_PRICE;
	
	@ClientString(id = 2093, message = "The passed-in price is $s1 adena. Would you like to return the passed-in price?")
	public static SystemMessageId THE_PASSED_IN_PRICE_IS_S1_ADENA_WOULD_YOU_LIKE_TO_RETURN_THE_PASSED_IN_PRICE;
	
	@ClientString(id = 2094, message = "Another user is purchasing. Please try again later.")
	public static SystemMessageId ANOTHER_USER_IS_PURCHASING_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 2095, message = "The following account cannot use Shout.")
	public static SystemMessageId THE_FOLLOWING_ACCOUNT_CANNOT_USE_SHOUT;
	
	@ClientString(id = 2108, message = "Your account has been suspended for 10 days for use of illegal software and may be permanently suspended. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_10_DAYS_FOR_USE_OF_ILLEGAL_SOFTWARE_AND_MAY_BE_PERMANENTLY_SUSPENDED_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2109, message = "The server has been integrated, and your character, $s1, has been overlapped with another name. Please enter a new name for your character.")
	public static SystemMessageId THE_SERVER_HAS_BEEN_INTEGRATED_AND_YOUR_CHARACTER_S1_HAS_BEEN_OVERLAPPED_WITH_ANOTHER_NAME_PLEASE_ENTER_A_NEW_NAME_FOR_YOUR_CHARACTER;
	
	@ClientString(id = 2110, message = "This character name already exists or is an invalid name. Please enter a new name.")
	public static SystemMessageId THIS_CHARACTER_NAME_ALREADY_EXISTS_OR_IS_AN_INVALID_NAME_PLEASE_ENTER_A_NEW_NAME;
	
	@ClientString(id = 2115, message = "Your account has been suspended for abusing a bug related to the NCcoin. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_ABUSING_A_BUG_RELATED_TO_THE_NCCOIN_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2116, message = "Your account has been suspended for abusing a free NCcoin. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_ABUSING_A_FREE_NCCOIN_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2117, message = "Your account has been suspended for using another person's identification. If you feel this suspension is in error, please verify your identity. For more information, visit the PlayNC website Support Center (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_USING_ANOTHER_PERSON_S_IDENTIFICATION_IF_YOU_FEEL_THIS_SUSPENSION_IS_IN_ERROR_PLEASE_VERIFY_YOUR_IDENTITY_FOR_MORE_INFORMATION_VISIT_THE_PLAYNC_WEBSITE_SUPPORT_CENTER_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2118, message = "Your account has been suspended for misappropriating payment under another player's account. For more information, please visit the PlayNC website's Support Center (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_MISAPPROPRIATING_PAYMENT_UNDER_ANOTHER_PLAYER_S_ACCOUNT_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_PLAYNC_WEBSITE_S_SUPPORT_CENTER_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2119, message = "Your account has been suspended from all game services because illicit account dealing was detected.\\nFor more information, please visit the Support Center at the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FROM_ALL_GAME_SERVICES_BECAUSE_ILLICIT_ACCOUNT_DEALING_WAS_DETECTED_NFOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_AT_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2120, message = "Your account has been suspended for 10 days for use of illegal software and may be permanently suspended. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_10_DAYS_FOR_USE_OF_ILLEGAL_SOFTWARE_AND_MAY_BE_PERMANENTLY_SUSPENDED_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT_2;
	
	@ClientString(id = 2121, message = "Your account has been suspended from all game services for use of illegal software. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FROM_ALL_GAME_SERVICES_FOR_USE_OF_ILLEGAL_SOFTWARE_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2122, message = "Your account has been suspended from all game services for use of illegal software. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FROM_ALL_GAME_SERVICES_FOR_USE_OF_ILLEGAL_SOFTWARE_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT_2;
	
	@ClientString(id = 2123, message = "Your account has been suspended at your own request. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_AT_YOUR_OWN_REQUEST_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2124, message = "The server has been integrated, and your Clan name, $s1, has been overlapped with another name. Please enter the Clan name to be changed.")
	public static SystemMessageId THE_SERVER_HAS_BEEN_INTEGRATED_AND_YOUR_CLAN_NAME_S1_HAS_BEEN_OVERLAPPED_WITH_ANOTHER_NAME_PLEASE_ENTER_THE_CLAN_NAME_TO_BE_CHANGED;
	
	@ClientString(id = 2125, message = "The name already exists or is an invalid name. Please enter the Clan name to be changed.")
	public static SystemMessageId THE_NAME_ALREADY_EXISTS_OR_IS_AN_INVALID_NAME_PLEASE_ENTER_THE_CLAN_NAME_TO_BE_CHANGED;
	
	@ClientString(id = 2126, message = "Your account has been suspended for regularly posting illegal messages. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_REGULARLY_POSTING_ILLEGAL_MESSAGES_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2127, message = "Your account has been suspended after an illegal message was detected. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_AFTER_AN_ILLEGAL_MESSAGE_WAS_DETECTED_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2128, message = "Your account has been suspended from all game services for using the game for commercial purposes. For more information, please visit the Support Center on the PlayNC website (http://www.plaync.com/us/support/).")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_SUSPENDED_FROM_ALL_GAME_SERVICES_FOR_USING_THE_GAME_FOR_COMMERCIAL_PURPOSES_FOR_MORE_INFORMATION_PLEASE_VISIT_THE_SUPPORT_CENTER_ON_THE_PLAYNC_WEBSITE_HTTP_WWW_PLAYNC_COM_US_SUPPORT;
	
	@ClientString(id = 2132, message = "You have entered a common sever.")
	public static SystemMessageId YOU_HAVE_ENTERED_A_COMMON_SEVER;
	
	@ClientString(id = 2133, message = "You have entered an adults-only sever.")
	public static SystemMessageId YOU_HAVE_ENTERED_AN_ADULTS_ONLY_SEVER;
	
	@ClientString(id = 2134, message = "You have entered a server for juveniles.")
	public static SystemMessageId YOU_HAVE_ENTERED_A_SERVER_FOR_JUVENILES;
	
	@ClientString(id = 2135, message = "Because of your Fatigue level, this is not allowed.")
	public static SystemMessageId BECAUSE_OF_YOUR_FATIGUE_LEVEL_THIS_IS_NOT_ALLOWED;
	
	@ClientString(id = 2153, message = "You are currently logged into 10 of your accounts and can no longer access your other accounts.")
	public static SystemMessageId YOU_ARE_CURRENTLY_LOGGED_INTO_10_OF_YOUR_ACCOUNTS_AND_CAN_NO_LONGER_ACCESS_YOUR_OTHER_ACCOUNTS;
	
	static
	{
		buildFastLookupTable();
	}
	
	private static void buildFastLookupTable()
	{
		for (Field field : SystemMessageId.class.getDeclaredFields())
		{
			final int mod = field.getModifiers();
			if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && field.getType().equals(SystemMessageId.class) && field.isAnnotationPresent(ClientString.class))
			{
				try
				{
					final ClientString annotation = field.getAnnotationsByType(ClientString.class)[0];
					final SystemMessageId smId = new SystemMessageId(annotation.id());
					smId.setName(field.getName());
					smId.setParamCount(parseMessageParameters(field.getName()));
					field.set(null, smId);
					VALUES.put(smId.getId(), smId);
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, "SystemMessageId: Failed field access for '" + field.getName() + "'", e);
				}
			}
		}
	}
	
	private static int parseMessageParameters(String name)
	{
		int paramCount = 0;
		char c1;
		char c2;
		for (int i = 0; i < (name.length() - 1); i++)
		{
			c1 = name.charAt(i);
			if ((c1 == 'C') || (c1 == 'S'))
			{
				c2 = name.charAt(i + 1);
				if (Character.isDigit(c2))
				{
					paramCount = Math.max(paramCount, Character.getNumericValue(c2));
					i++;
				}
			}
		}
		return paramCount;
	}
	
	public static SystemMessageId getSystemMessageId(int id)
	{
		final SystemMessageId smi = getSystemMessageIdInternal(id);
		return smi == null ? new SystemMessageId(id) : smi;
	}
	
	private static SystemMessageId getSystemMessageIdInternal(int id)
	{
		return VALUES.get(id);
	}
	
	public static SystemMessageId getSystemMessageId(String name)
	{
		try
		{
			return (SystemMessageId) SystemMessageId.class.getField(name).get(null);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	private final int _id;
	private String _name;
	private byte _params;
	private SystemMessage _staticSystemMessage;
	
	private SystemMessageId(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	private void setName(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getParamCount()
	{
		return _params;
	}
	
	public void setParamCount(int params)
	{
		if (params < 0)
		{
			throw new IllegalArgumentException("Invalid negative param count: " + params);
		}
		
		if (params > 10)
		{
			throw new IllegalArgumentException("Maximum param count exceeded: " + params);
		}
		
		if (params != 0)
		{
			_staticSystemMessage = null;
		}
		
		_params = (byte) params;
	}
	
	public SystemMessage getStaticSystemMessage()
	{
		return _staticSystemMessage;
	}
	
	public void setStaticSystemMessage(SystemMessage sm)
	{
		_staticSystemMessage = sm;
	}
	
	@Override
	public String toString()
	{
		return "SM[" + getId() + ":" + getName() + "]";
	}
}
