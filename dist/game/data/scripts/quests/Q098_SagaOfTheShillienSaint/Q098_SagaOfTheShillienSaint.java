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
package quests.Q098_SagaOfTheShillienSaint;

import quests.SagasSuperClass;

/**
 * Saga of the Shillien Saint (98)
 * @author Emperorc
 */
public class Q098_SagaOfTheShillienSaint extends SagasSuperClass
{
	public Q098_SagaOfTheShillienSaint()
	{
		super(98, "Saga of the Shillien Saint");
		_npc = new int[]
		{
			31581,
			31626,
			31588,
			31287,
			31621,
			31646,
			31647,
			31651,
			31654,
			31655,
			31658,
			31287
		};
		_items = new int[]
		{
			7080,
			7525,
			7081,
			7513,
			7296,
			7327,
			7358,
			7389,
			7420,
			7451,
			7090,
			0
		};
		_mob = new int[]
		{
			27270,
			27247,
			27277
		};
		_classId = new int[]
		{
			112
		};
		_prevClass = new int[]
		{
			0x2b
		};
		_x = new int[]
		{
			119518,
			181215,
			181227
		};
		_y = new int[]
		{
			-28658,
			36676,
			36703
		};
		_z = new int[]
		{
			-3811,
			-4812,
			-4816
		};
		_text = new String[]
		{
			"PLAYERNAME! Pursued to here! However, I jumped out of the Banshouren boundaries! You look at the giant as the sign of power!",
			"... Oh ... good! So it was ... let's begin!",
			"I do not have the patience ..! I have been a giant force ...! Cough chatter ah ah ah!",
			"Paying homage to those who disrupt the orderly will be PLAYERNAME's death!",
			"Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ...",
			"Why do you interfere others' battles?",
			"This is a waste of time.. Say goodbye...!",
			"...That is the enemy",
			"...Goodness! PLAYERNAME you are still looking?",
			"PLAYERNAME ... Not just to whom the victory. Only personnel involved in the fighting are eligible to share in the victory.",
			"Your sword is not an ornament. Don't you think, PLAYERNAME?",
			"Goodness! I no longer sense a battle there now.",
			"let...",
			"Only engaged in the battle to bar their choice. Perhaps you should regret.",
			"The human nation was foolish to try and fight a giant's strength.",
			"Must...Retreat... Too...Strong.",
			"PLAYERNAME. Defeat...by...retaining...and...Mo...Hacker",
			"....! Fight...Defeat...It...Fight...Defeat...It..."
		};
		registerNPCs();
	}
}