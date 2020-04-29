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
package org.l2jserver.gameserver.handler.skillhandlers;

/**
 * @author programmos TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
/*
 * public class TakeFort implements ISkillHandler { //private static final Logger LOGGER = Logger.getLogger(TakeFort.class); //private static final SkillType[] SKILL_IDS = { SkillType.TAKEFORT }; public void useSkill(Creature creature, @SuppressWarnings("unused") Skill
 * skill, @SuppressWarnings("unused") WorldObject[] targets) { if (activeChar == null || !(activeChar instanceof PlayerInstance)) return; PlayerInstance player = (PlayerInstance)activeChar; if (player.getClan() == null ) return; Fort fort = FortManager.getInstance().getFort(player); if (fort == null
 * || !checkIfOkToCastFlagDisplay(player, fort, true)) return; try { // if(targets[0] instanceof ArtefactInstance) fort.EndOfSiege(player.getClan()); } catch(Exception e) {} } //public SkillType[] getSkillIds() //{ //return SKILL_IDS; //}
 */
/**
 * Return true if character clan place a flag<br>
 * <br>
 * @param activeChar The Creature of the creature placing the flag
 */
/*
 * public static boolean checkIfOkToCastFlagDisplay(Creature creature, boolean isCheckOnly) { return checkIfOkToCastFlagDisplay(activeChar, FortManager.getInstance().getFort(activeChar), isCheckOnly); } public static boolean checkIfOkToCastFlagDisplay(Creature creature, Fort fort, boolean
 * isCheckOnly) { if (activeChar == null || !(activeChar instanceof PlayerInstance)) return false; SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2ss); PlayerInstance player = (PlayerInstance)activeChar; if (fort == null || fort.getFortId() <= 0)
 * sm.addString("You must be on fort ground to use this skill"); else if (player.getTarget() == null && !(player.getTarget() instanceof ArtefactInstance)) sm.addString("You can only use this skill on an flagpole"); else if (!fort.getSiege().isInProgress())
 * sm.addString("You can only use this skill during a siege."); else if (!Util.checkIfInRange(200, player, player.getTarget(), true)) sm.addString("You are not in range of the flagpole."); else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
 * sm.addString("You must be an attacker to use this skill"); else { if (!isCheckOnly) fort.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to raise flag.", true); return true; } if (!isCheckOnly) { player.sendPacket(sm); } return false; } }
 */
