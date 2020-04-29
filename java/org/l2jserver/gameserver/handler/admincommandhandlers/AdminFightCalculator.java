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
package org.l2jserver.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.skills.Formulas;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - GM = turns GM mode on/off
 * @version $Revision: 1.1.2.1 $ $Date: 2005/03/15 21:32:48 $
 */
public class AdminFightCalculator implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fight_calculator",
		"admin_fight_calculator_show",
		"admin_fcs",
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		try
		{
			if (command.startsWith("admin_fight_calculator_show"))
			{
				handleShow(command.substring("admin_fight_calculator_show".length()), activeChar);
			}
			else if (command.startsWith("admin_fcs"))
			{
				handleShow(command.substring("admin_fcs".length()), activeChar);
			}
			else if (command.startsWith("admin_fight_calculator"))
			{
				handleStart(command.substring("admin_fight_calculator".length()), activeChar);
			}
		}
		catch (StringIndexOutOfBoundsException e)
		{
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleStart(String params, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(params);
		int lvl1 = 0;
		int lvl2 = 0;
		int mid1 = 0;
		int mid2 = 0;
		
		while (st.hasMoreTokens())
		{
			final String s = st.nextToken();
			if (s.equals("lvl1"))
			{
				lvl1 = Integer.parseInt(st.nextToken());
				continue;
			}
			
			if (s.equals("lvl2"))
			{
				lvl2 = Integer.parseInt(st.nextToken());
				continue;
			}
			
			if (s.equals("mid1"))
			{
				mid1 = Integer.parseInt(st.nextToken());
				continue;
			}
			
			if (s.equals("mid2"))
			{
				mid2 = Integer.parseInt(st.nextToken());
				continue;
			}
		}
		
		NpcTemplate npc1 = null;
		if (mid1 != 0)
		{
			npc1 = NpcTable.getInstance().getTemplate(mid1);
		}
		
		NpcTemplate npc2 = null;
		if (mid2 != 0)
		{
			npc2 = NpcTable.getInstance().getTemplate(mid2);
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder();
		if ((npc1 != null) && (npc2 != null))
		{
			replyMSG.append("<html><title>Selected mobs to fight</title>");
			replyMSG.append("<body>");
			replyMSG.append("<table>");
			replyMSG.append("<tr><td>First</td><td>Second</td></tr>");
			replyMSG.append("<tr><td>level " + lvl1 + "</td><td>level " + lvl2 + "</td></tr>");
			replyMSG.append("<tr><td>id " + npc1.getNpcId() + "</td><td>id " + npc2.getNpcId() + "</td></tr>");
			replyMSG.append("<tr><td>" + npc1.getName() + "</td><td>" + npc2.getName() + "</td></tr>");
			replyMSG.append("</table>");
			replyMSG.append("<center><br><br><br>");
			replyMSG.append("<button value=\"OK\" action=\"bypass -h admin_fight_calculator_show " + npc1.getNpcId() + " " + npc2.getNpcId() + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</center>");
			replyMSG.append("</body></html>");
		}
		else if ((lvl1 != 0) && (npc1 == null))
		{
			replyMSG.append("<html><title>Select first mob to fight</title>");
			replyMSG.append("<body><table>");
			
			final NpcTemplate[] npcs = NpcTable.getInstance().getAllOfLevel(lvl1);
			for (NpcTemplate n : npcs)
			{
				replyMSG.append("<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 " + lvl1 + " lvl2 " + lvl2 + " mid1 " + n.getNpcId() + " mid2 " + mid2 + "\">" + n.getName() + "</a></td></tr>");
			}
			
			replyMSG.append("</table></body></html>");
		}
		else if ((lvl2 != 0) && (npc2 == null))
		{
			replyMSG.append("<html><title>Select second mob to fight</title>");
			replyMSG.append("<body><table>");
			
			final NpcTemplate[] npcs = NpcTable.getInstance().getAllOfLevel(lvl2);
			for (NpcTemplate n : npcs)
			{
				replyMSG.append("<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 " + lvl1 + " lvl2 " + lvl2 + " mid1 " + mid1 + " mid2 " + n.getNpcId() + "\">" + n.getName() + "</a></td></tr>");
			}
			
			replyMSG.append("</table></body></html>");
		}
		else
		{
			replyMSG.append("<html><title>Select mobs to fight</title>");
			replyMSG.append("<body>");
			replyMSG.append("<table>");
			replyMSG.append("<tr><td>First</td><td>Second</td></tr>");
			replyMSG.append("<tr><td><edit var=\"lvl1\" width=80></td><td><edit var=\"lvl2\" width=80></td></tr>");
			replyMSG.append("</table>");
			replyMSG.append("<center><br><br><br>");
			replyMSG.append("<button value=\"OK\" action=\"bypass -h admin_fight_calculator lvl1 $lvl1 lvl2 $lvl2\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</center>");
			replyMSG.append("</body></html>");
		}
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void handleShow(String params, PlayerInstance activeChar)
	{
		params = params.trim();
		Creature npc1 = null;
		Creature npc2 = null;
		if (params.length() == 0)
		{
			npc1 = activeChar;
			npc2 = (Creature) activeChar.getTarget();
			if (npc2 == null)
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return;
			}
		}
		else
		{
			int mid1 = 0;
			int mid2 = 0;
			
			final StringTokenizer st = new StringTokenizer(params);
			mid1 = Integer.parseInt(st.nextToken());
			mid2 = Integer.parseInt(st.nextToken());
			npc1 = new MonsterInstance(IdFactory.getNextId(), NpcTable.getInstance().getTemplate(mid1));
			npc2 = new MonsterInstance(IdFactory.getNextId(), NpcTable.getInstance().getTemplate(mid2));
		}
		
		int miss1 = 0;
		int miss2 = 0;
		int shld1 = 0;
		int shld2 = 0;
		int crit1 = 0;
		int crit2 = 0;
		int crit3 = 0;
		int crit4 = 0;
		double patk1 = 0;
		double patk2 = 0;
		double pdef1 = 0;
		double pdef2 = 0;
		double dmg1 = 0;
		double dmg2 = 0;
		
		// ATTACK speed in milliseconds
		int sAtk1 = npc1.calculateTimeBetweenAttacks(npc2, null);
		int sAtk2 = npc2.calculateTimeBetweenAttacks(npc1, null);
		
		// number of ATTACK per 100 seconds
		sAtk1 = 100000 / sAtk1;
		sAtk2 = 100000 / sAtk2;
		for (int i = 0; i < 10000; i++)
		{
			final boolean calcMiss1 = Formulas.calcHitMiss(npc1, npc2);
			if (calcMiss1)
			{
				miss1++;
			}
			
			final boolean calcShld1 = Formulas.calcShldUse(npc1, npc2);
			if (calcShld1)
			{
				shld1++;
			}
			
			final boolean calcCrit1 = Formulas.calcCrit(npc1.getCriticalHit(npc2, null));
			if (calcCrit1)
			{
				crit1++;
			}
			
			final boolean calcCrit4 = Formulas.calcCrit(npc1.getMCriticalHit(npc2, null));
			if (calcCrit4)
			{
				crit4++;
			}
			
			double npcPatk1 = npc1.getPAtk(npc2);
			npcPatk1 += Rnd.nextDouble() * npc1.getRandomDamage(npc2);
			patk1 += npcPatk1;
			
			final double npcPdef1 = npc1.getPDef(npc2);
			pdef1 += npcPdef1;
			if (!calcMiss1)
			{
				npc1.setAttackingBodypart();
				
				final double calcDmg1 = Formulas.calcPhysDam(npc1, npc2, null, calcShld1, calcCrit1, false, false);
				dmg1 += calcDmg1;
				npc1.abortAttack();
			}
		}
		
		for (int i = 0; i < 10000; i++)
		{
			final boolean calcMiss2 = Formulas.calcHitMiss(npc2, npc1);
			if (calcMiss2)
			{
				miss2++;
			}
			
			final boolean calcShld2 = Formulas.calcShldUse(npc2, npc1);
			if (calcShld2)
			{
				shld2++;
			}
			
			final boolean calcCrit2 = Formulas.calcCrit(npc2.getCriticalHit(npc1, null));
			if (calcCrit2)
			{
				crit2++;
			}
			
			final boolean calcCrit3 = Formulas.calcCrit(npc2.getMCriticalHit(npc1, null));
			if (calcCrit3)
			{
				crit3++;
			}
			
			double npcPatk2 = npc2.getPAtk(npc1);
			npcPatk2 += Rnd.nextDouble() * npc2.getRandomDamage(npc1);
			patk2 += npcPatk2;
			
			final double npcPdef2 = npc2.getPDef(npc1);
			pdef2 += npcPdef2;
			if (!calcMiss2)
			{
				npc2.setAttackingBodypart();
				
				final double calcDmg2 = Formulas.calcPhysDam(npc2, npc1, null, calcShld2, calcCrit2, false, false);
				dmg2 += calcDmg2;
				npc2.abortAttack();
			}
		}
		
		miss1 /= 100;
		miss2 /= 100;
		shld1 /= 100;
		shld2 /= 100;
		crit1 /= 100;
		crit2 /= 100;
		crit3 /= 100;
		crit4 /= 100;
		patk1 /= 10000;
		patk2 /= 10000;
		pdef1 /= 10000;
		pdef2 /= 10000;
		dmg1 /= 10000;
		dmg2 /= 10000;
		
		// total damage per 100 seconds
		final int tdmg1 = (int) (sAtk1 * dmg1);
		final int tdmg2 = (int) (sAtk2 * dmg2);
		
		// HP restored per 100 seconds
		final double maxHp1 = npc1.getMaxHp();
		final int hp1 = (int) ((Formulas.calcHpRegen(npc1) * 100000) / Formulas.getRegeneratePeriod(npc1));
		final double maxHp2 = npc2.getMaxHp();
		final int hp2 = (int) ((Formulas.calcHpRegen(npc2) * 100000) / Formulas.getRegeneratePeriod(npc2));
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder();
		replyMSG.append("<html><title>Selected mobs to fight</title>");
		replyMSG.append("<body>");
		replyMSG.append("<table>");
		
		if (params.length() == 0)
		{
			replyMSG.append("<tr><td width=140>Parameter</td><td width=70>me</td><td width=70>target</td></tr>");
		}
		else
		{
			replyMSG.append("<tr><td width=140>Parameter</td><td width=70>" + ((NpcTemplate) npc1.getTemplate()).getName() + "</td><td width=70>" + ((NpcTemplate) npc2.getTemplate()).getName() + "</td></tr>");
		}
		
		replyMSG.append("<tr><td>miss</td><td>" + miss1 + "%</td><td>" + miss2 + "%</td></tr>");
		replyMSG.append("<tr><td>shld</td><td>" + shld2 + "%</td><td>" + shld1 + "%</td></tr>");
		replyMSG.append("<tr><td>Physic crit</td><td>" + crit1 + "%</td><td>" + crit2 + "%</td></tr>");
		replyMSG.append("<tr><td>Magic crit</td><td>" + crit4 + "%</td><td>" + crit3 + "%</td></tr>");
		replyMSG.append("<tr><td>pAtk / pDef</td><td>" + (int) patk1 + " / " + (int) pdef1 + "</td><td>" + (int) patk2 + " / " + (int) pdef2 + "</td></tr>");
		replyMSG.append("<tr><td>made hits</td><td>" + sAtk1 + "</td><td>" + sAtk2 + "</td></tr>");
		replyMSG.append("<tr><td>dmg per hit</td><td>" + (int) dmg1 + "</td><td>" + (int) dmg2 + "</td></tr>");
		replyMSG.append("<tr><td>got dmg</td><td>" + tdmg2 + "</td><td>" + tdmg1 + "</td></tr>");
		replyMSG.append("<tr><td>got regen</td><td>" + hp1 + "</td><td>" + hp2 + "</td></tr>");
		replyMSG.append("<tr><td>had HP</td><td>" + (int) maxHp1 + "</td><td>" + (int) maxHp2 + "</td></tr>");
		replyMSG.append("<tr><td>die</td>");
		
		if ((tdmg2 - hp1) > 1)
		{
			replyMSG.append("<td>" + (int) ((100 * maxHp1) / (tdmg2 - hp1)) + " sec</td>");
		}
		else
		{
			replyMSG.append("<td>never</td>");
		}
		
		if ((tdmg1 - hp2) > 1)
		{
			replyMSG.append("<td>" + (int) ((100 * maxHp2) / (tdmg1 - hp2)) + " sec</td>");
		}
		else
		{
			replyMSG.append("<td>never</td>");
		}
		
		replyMSG.append("</tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center><br>");
		
		if (params.length() == 0)
		{
			replyMSG.append("<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		}
		else
		{
			replyMSG.append("<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show " + ((NpcTemplate) npc1.getTemplate()).getNpcId() + " " + ((NpcTemplate) npc2.getTemplate()).getNpcId() + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		}
		
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		
		if (params.length() != 0)
		{
			((MonsterInstance) npc1).deleteMe();
			((MonsterInstance) npc2).deleteMe();
		}
	}
}
