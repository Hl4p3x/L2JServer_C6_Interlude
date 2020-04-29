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
package org.l2jserver.gameserver.model.quest;

/**
 * @author Mobius
 */
public enum EventType
{
	/** Control the first dialog shown by NPCs when they are clicked (some quests must override the default npc action) */
	NPC_FIRST_TALK(false),
	
	/** onTalk action from start npcs */
	QUEST_START(true),
	
	/** onTalk action from npcs participating in a quest */
	QUEST_TALK(true),
	
	/** Call a faction for support */
	ON_FACTION_CALL(true),
	
	/** on spell finished action when npc finish casting skill */
	ON_SPELL_FINISHED(true),
	
	/** a person came within the Npc/Mob's range */
	ON_AGGRO_RANGE_ENTER(true),
	
	/** onSpawn action triggered when an NPC is spawned or respawned */
	ON_SPAWN(true),
	
	/** NPC or Mob saw a person casting a skill (regardless what the target is) */
	ON_SKILL_USE(true),
	
	/** onKill action triggered when a mob gets killed */
	ON_KILL(true),
	
	/** onAttack action triggered when a mob gets attacked by someone */
	ON_ATTACK(true);
	
	// Control whether this event type is allowed for the same npc template in multiple quests or if the npc must be registered in at most one quest for the specified event.
	private boolean _allowMultipleRegistration;
	
	EventType(boolean allowMultipleRegistration)
	{
		_allowMultipleRegistration = allowMultipleRegistration;
	}
	
	public boolean isMultipleRegistrationAllowed()
	{
		return _allowMultipleRegistration;
	}
}
