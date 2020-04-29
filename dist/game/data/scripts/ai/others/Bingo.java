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

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.commons.util.Rnd;

/**
 * Adapted from FirstTeam Interlude
 */
public class Bingo
{
	protected static final String TEMPLATE = "%msg%<br><br>%choices%<br><br>%board%";
	protected static final String TEMPLATE_FINAL = "%msg%<br><br>%board%";
	protected static final String TEMPLATE_BOARD = "For your information, below is your current selection.<br><table border=\"1\" border color=\"white\" width=100><tr><td align=\"center\">%cell1%</td><td align=\"center\">%cell2%</td><td align=\"center\">%cell3%</td></tr><tr><td align=\"center\">%cell4%</td><td align=\"center\">%cell5%</td><td align=\"center\">%cell6%</td></tr><tr><td align=\"center\">%cell7%</td><td align=\"center\">%cell8%</td><td align=\"center\">%cell9%</td></tr></table>";
	protected static final String MSG_AGAIN = "You have already selected that number. Choose your %choicenum% number again.";
	protected static final String MSG_BEGIN = "I've arranged 9 numbers on the panel.<br>Now, select your %choicenum% number.";
	protected static final String MSG_NEXT = "Now, choose your %choicenum% number.";
	protected static final String MSG_ZERO_LINES = "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?";
	protected static final String MSG_THREE_LINES = "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations!";
	protected static final String MSG_LOSE = "Hmm... You didn't make 3 lines. Why don't you try again? The red-colored numbers on the panel are the ones you chose.";
	protected static final String[] NUMBERS =
	{
		"first",
		"second",
		"third",
		"fourth",
		"fifth",
		"final"
	};
	
	private final String _template_choice;
	private final List<Integer> board;
	private final List<Integer> guesses;
	protected int lines;
	
	public Bingo(String templateChoice)
	{
		board = new ArrayList<>();
		guesses = new ArrayList<>();
		_template_choice = templateChoice;
		while (board.size() < 9)
		{
			final int num = Rnd.get(1, 9);
			if (!board.contains(num))
			{
				board.add(num);
			}
		}
	}
	
	public String Select(String s)
	{
		try
		{
			return Select(Integer.parseInt(s));
		}
		catch (Exception E)
		{
			return null;
		}
	}
	
	public String Select(int choise)
	{
		if ((choise < 1) || (choise > 9))
		{
			return null;
		}
		if (guesses.contains(choise))
		{
			return getDialog("You have already selected that number. Choose your %choicenum% number again.");
		}
		guesses.add(choise);
		if (guesses.size() == 6)
		{
			return getFinal();
		}
		return getDialog("");
	}
	
	protected String getBoard()
	{
		if (guesses.isEmpty())
		{
			return "";
		}
		String result = "For your information, below is your current selection.<br><table border=\"1\" border color=\"white\" width=100><tr><td align=\"center\">%cell1%</td><td align=\"center\">%cell2%</td><td align=\"center\">%cell3%</td></tr><tr><td align=\"center\">%cell4%</td><td align=\"center\">%cell5%</td><td align=\"center\">%cell6%</td></tr><tr><td align=\"center\">%cell7%</td><td align=\"center\">%cell8%</td><td align=\"center\">%cell9%</td></tr></table>";
		for (int i = 1; i <= 9; ++i)
		{
			final String cell = "%cell" + i + "%";
			final int num = board.get(i - 1);
			if (guesses.contains(num))
			{
				result = result.replaceFirst(cell, "<font color=\"" + ((guesses.size() == 6) ? "ff0000" : "ffff00") + "\">" + num + "</font>");
			}
			else
			{
				result = result.replaceFirst(cell, "?");
			}
		}
		return result;
	}
	
	public String getDialog(String msg)
	{
		String result = "%msg%<br><br>%choices%<br><br>%board%";
		if (guesses.isEmpty())
		{
			result = result.replaceFirst("%msg%", "I've arranged 9 numbers on the panel.<br>Now, select your %choicenum% number.");
		}
		else
		{
			result = result.replaceFirst("%msg%", "".equalsIgnoreCase(msg) ? "Now, choose your %choicenum% number." : msg);
		}
		result = result.replaceFirst("%choicenum%", Bingo.NUMBERS[guesses.size()]);
		final StringBuilder choices = new StringBuilder();
		for (int i = 1; i <= 9; ++i)
		{
			if (!guesses.contains(i))
			{
				choices.append(_template_choice.replace("%n%", String.valueOf(i)));
			}
		}
		result = result.replaceFirst("%choices%", choices.toString());
		result = result.replaceFirst("%board%", getBoard());
		return result;
	}
	
	protected String getFinal()
	{
		String result = "%msg%<br><br>%board%".replaceFirst("%board%", getBoard());
		calcLines();
		switch (lines)
		{
			case 3:
				result = result.replaceFirst("%msg%", "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations!");
				break;
			case 0:
				result = result.replaceFirst("%msg%", "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?");
				break;
			default:
				result = result.replaceFirst("%msg%", "Hmm... You didn't make 3 lines. Why don't you try again? The red-colored numbers on the panel are the ones you chose.");
				break;
		}
		return result;
	}
	
	public int calcLines()
	{
		lines = 0;
		lines += (checkLine(0, 1, 2) ? 1 : 0);
		lines += (checkLine(3, 4, 5) ? 1 : 0);
		lines += (checkLine(6, 7, 8) ? 1 : 0);
		lines += (checkLine(0, 3, 6) ? 1 : 0);
		lines += (checkLine(1, 4, 7) ? 1 : 0);
		lines += (checkLine(2, 5, 8) ? 1 : 0);
		lines += (checkLine(0, 4, 8) ? 1 : 0);
		return lines += (checkLine(2, 4, 6) ? 1 : 0);
	}
	
	public boolean checkLine(int idx1, int idx2, int idx3)
	{
		return guesses.contains(board.get(idx1)) && guesses.contains(board.get(idx2)) && guesses.contains(board.get(idx3));
	}
}
