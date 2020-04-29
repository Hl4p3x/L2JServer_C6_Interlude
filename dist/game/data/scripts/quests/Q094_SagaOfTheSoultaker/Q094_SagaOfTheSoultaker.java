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
package quests.Q094_SagaOfTheSoultaker;

import quests.SagasSuperClass;

/**
 * Saga of the Soultaker (94)
 * @author Emperorc
 */
public class Q094_SagaOfTheSoultaker extends SagasSuperClass
{
	public Q094_SagaOfTheSoultaker()
	{
		super(94, "Saga of the Soultaker");
		_npc = new int[]
		{
			30832,
			31623,
			31279,
			31279,
			31645,
			31646,
			31648,
			31650,
			31654,
			31655,
			31657,
			31279
		};
		_items = new int[]
		{
			7080,
			7533,
			7081,
			7509,
			7292,
			7323,
			7354,
			7385,
			7416,
			7447,
			7085,
			0
		};
		_mob = new int[]
		{
			27257,
			27243,
			27265
		};
		_classId = new int[]
		{
			95
		};
		_prevClass = new int[]
		{
			0x0d
		};
		_x = new int[]
		{
			191046,
			46066,
			46087
		};
		_y = new int[]
		{
			-40640,
			-36396,
			-36372
		};
		_z = new int[]
		{
			-3042,
			-1685,
			-1685
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