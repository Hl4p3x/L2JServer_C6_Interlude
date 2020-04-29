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
package ai.areas.HotSprings;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * Hot Springs AI.
 * @author Mobius
 */
public class HotSprings extends Quest
{
	// NPCs
	private static final int BANDERSNATCHLING = 21314;
	private static final int FLAVA = 21316;
	private static final int ATROXSPAWN = 21317;
	private static final int NEPENTHES = 21319;
	private static final int ATROX = 21321;
	private static final int BANDERSNATCH = 21322;
	// Skills
	private static final int RHEUMATISM = 4551;
	private static final int CHOLERA = 4552;
	private static final int FLU = 4553;
	private static final int MALARIA = 4554;
	// Misc
	private static final int DISEASE_CHANCE = 10;
	
	private HotSprings()
	{
		super(-1, "ai/areas");
		
		addAttackId(BANDERSNATCHLING, FLAVA, ATROXSPAWN, NEPENTHES, ATROX, BANDERSNATCH);
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isSummon)
	{
		if (Rnd.get(100) < DISEASE_CHANCE)
		{
			tryToInfect(npc, attacker, MALARIA);
		}
		
		if (Rnd.get(100) < DISEASE_CHANCE)
		{
			switch (npc.getNpcId())
			{
				case BANDERSNATCHLING:
				case ATROX:
				{
					tryToInfect(npc, attacker, RHEUMATISM);
					break;
				}
				case FLAVA:
				case NEPENTHES:
				{
					tryToInfect(npc, attacker, CHOLERA);
					break;
				}
				case ATROXSPAWN:
				case BANDERSNATCH:
				{
					tryToInfect(npc, attacker, FLU);
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	private void tryToInfect(NpcInstance npc, Creature creature, int diseaseId)
	{
		final Effect info = creature.getFirstEffect(diseaseId);
		final int skillLevel = (info == null) ? 1 : (info.getSkill().getLevel() < 10) ? info.getSkill().getLevel() + 1 : 10;
		final Skill skill = SkillTable.getInstance().getInfo(diseaseId, skillLevel);
		if ((skill != null) && !npc.isCastingNow() && !npc.isSkillDisabled(skill))
		{
			npc.setTarget(creature);
			npc.doCast(skill);
		}
	}
	
	public static void main(String[] args)
	{
		new HotSprings();
	}
}