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
package ai.others;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.ChestInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Chests extends Quest
{
	// NPCs
	private static final int[] CHESTS =
	{// @formatter:off
		18265,18266,18267,18268,18269,18270,18271,18272,18273,18274,18275,
		18276,18277,18278,18279,18280,18281,18282,18283,18284,18285,18286,
		18287,18288,18289,18290,18291,18292,18293,18294,18295,18296,18297,
		18298,21671,21694,21717,21740,21763,21786,21801,21802,21803,21804,
		21805,21806,21807,21808,21809,21810,21811,21812,21813,21814,21815,
		21816,21817,21818,21819,21820,21821,21822
	}; // @formatter:on
	// Skill
	private static final int SKILL_DELUXE_KEY = 2229;
	// Misc
	private static final int BASE_CHANCE = 100;
	// Percent to decrease base chance when grade of DELUXE key not match.
	private static final int LEVEL_DECREASE = 40;
	// Chance for a chest to actually be a BOX (as opposed to being a mimic).
	private static final int IS_BOX = 40;
	
	private Chests()
	{
		super(-1, "ai/others");
		
		addSkillUseId(CHESTS);
		addAttackId(CHESTS);
	}
	
	@Override
	public String onSkillUse(NpcInstance npc, PlayerInstance caster, Skill skill)
	{
		// if this has already been interacted, no further ai decisions are needed
		// if it's the first interaction, check if this is a box or mimic
		final ChestInstance chest = (ChestInstance) npc;
		if (chest.isInteracted())
		{
			chest.setInteracted();
			if (Rnd.get(100) < IS_BOX)
			{
				// if it's a box, either it will be successfully openned by a proper key, or instantly disappear
				if (skill.getId() == SKILL_DELUXE_KEY)
				{
					// check the chance to open the box
					final int keyLevelNeeded = chest.getLevel() / 10;
					int levelDiff = keyLevelNeeded - skill.getLevel();
					if (levelDiff < 0)
					{
						levelDiff = levelDiff * (-1);
					}
					final int chance = BASE_CHANCE - (levelDiff * LEVEL_DECREASE);
					
					// success, pretend-death with rewards: npc.reduceCurrentHp(99999999, player)
					if (Rnd.get(100) < chance)
					{
						chest.setMustRewardExpSp(false);
						chest.setSpecialDrop();
						chest.reduceCurrentHp(99999999, caster);
						return null;
					}
				}
				// used a skill other than chest-key, or used a chest-key but failed to open: disappear with no rewards
				chest.onDecay();
			}
			else
			{
				final Creature target = chest.getAttackByList().contains(caster.getPet()) ? caster.getPet() : caster;
				chest.setRunning();
				chest.addDamageHate(target, 0, 999);
				chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		return super.onSkillUse(npc, caster, skill);
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final ChestInstance chest = (ChestInstance) npc;
		if (!chest.isInteracted())
		{
			chest.setInteracted();
			if (Rnd.get(100) < IS_BOX)
			{
				chest.onDecay();
			}
			else
			{
				// if this weren't a box, upon interaction start the mimic behaviors...
				// TODO: perhaps a self-buff (skill id 4245) with random chance goes here?
				final Creature target = isPet ? attacker.getPet() : attacker;
				chest.setRunning();
				chest.addDamageHate(target, 0, (damage * 100) / (chest.getLevel() + 7));
				chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new Chests();
	}
}
