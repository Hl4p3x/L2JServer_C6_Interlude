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
package org.l2jserver.gameserver.ai;

/**
 * This class contains an enum of each possibles evenements that can happen on an AI character.
 */

public enum CtrlEvent
{
	/**
	 * Something has changed, usually a previous step has being completed or maybe was completed, the AI must thing on next action
	 */
	EVT_THINK,
	
	/**
	 * The actor was attacked. This event comes each time a physical or magical attack was done on the actor. NPC may start attack in responce, or ignore this event if they already attack someone, or change target and so on.
	 */
	EVT_ATTACKED,
	
	/** Increase/decrease aggression towards a target, or reduce global aggression if target is null */
	EVT_AGGRESSION,
	
	/** Actor is in stun state */
	EVT_STUNNED,
	
	/** Actor starts/stops sleeping */
	EVT_SLEEPING,
	
	/** Actor is in rooted state (cannot move) */
	EVT_ROOTED,
	
	/**
	 * An event that previous action was completed. The action may be an attempt to physically/magically hit an enemy, or an action that discarded attack attempt has finished.
	 */
	EVT_READY_TO_ACT,
	
	/**
	 * User's command, like using a combat magic or changing weapon, etc. The command is not intended to change final goal
	 */
	EVT_USER_CMD,
	
	/**
	 * The actor arrived to assigned location, or it's a time to modify movement destination (follow, interact, random move and others intentions).
	 */
	EVT_ARRIVED,
	
	/**
	 * The actor arrived to an intermidiate point, and needs revalidate destination. This is sent when follow/move to pawn if destination is far away.
	 */
	EVT_ARRIVED_REVALIDATE,
	
	/** The actor cannot move anymore. */
	EVT_ARRIVED_BLOCKED,
	
	/** Forgets an object (if it's used as attack target, follow target and so on */
	EVT_FORGET_OBJECT,
	
	/**
	 * Attempt to cancel current step execution, but not change the intention. For example, the actor was putted into a stun, so it's current attack or movement has to be canceled. But after the stun state expired, the actor may try to attack again. Another usage for CANCEL is a user's attempt to
	 * cancel a cast/bow attack and so on.
	 */
	EVT_CANCEL,
	
	/** The creature is dead */
	EVT_DEAD,
	
	/** The creature looks like dead */
	EVT_FAKE_DEATH,
	
	/** The creature attack anyone randomly **/
	EVT_CONFUSED,
	
	/** The creature cannot cast spells anymore **/
	EVT_MUTED,
	
	/** The creature flee in randoms directions **/
	EVT_AFFRAID,
	
	/** The creature finish casting **/
	EVT_FINISH_CASTING,
	
	/** The creature betrayed its master */
	EVT_BETRAYED
}
