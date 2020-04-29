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
package org.l2jserver.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import org.l2jserver.commons.util.StringUtil;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.communitybbs.CommunityBoard;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.clan.ClanMember;
import org.l2jserver.gameserver.network.SystemMessageId;

public class ClanBBSManager extends BaseBBSManager
{
	protected ClanBBSManager()
	{
	}
	
	public static ClanBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public void parseCmd(String command, PlayerInstance activeChar)
	{
		if (command.equalsIgnoreCase("_bbsclan"))
		{
			if (activeChar.getClan() == null)
			{
				sendClanList(activeChar, 1);
			}
			else
			{
				sendClanDetails(activeChar, activeChar.getClan().getClanId());
			}
		}
		else if (command.startsWith("_bbsclan"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			
			final String clanCommand = st.nextToken();
			if (clanCommand.equalsIgnoreCase("clan"))
			{
				CommunityBoard.getInstance().addBypass(activeChar, "Clan", command);
				sendClanList(activeChar, Integer.parseInt(st.nextToken()));
			}
			else if (clanCommand.equalsIgnoreCase("home"))
			{
				CommunityBoard.getInstance().addBypass(activeChar, "Clan Home", command);
				sendClanDetails(activeChar, Integer.parseInt(st.nextToken()));
			}
			else if (clanCommand.equalsIgnoreCase("mail"))
			{
				CommunityBoard.getInstance().addBypass(activeChar, "Clan Mail", command);
				sendClanMail(activeChar, Integer.parseInt(st.nextToken()));
			}
			else if (clanCommand.equalsIgnoreCase("management"))
			{
				CommunityBoard.getInstance().addBypass(activeChar, "Clan Management", command);
				sendClanManagement(activeChar, Integer.parseInt(st.nextToken()));
			}
			else if (clanCommand.equalsIgnoreCase("notice"))
			{
				CommunityBoard.getInstance().addBypass(activeChar, "Clan Notice", command);
				if (st.hasMoreTokens())
				{
					final String noticeCommand = st.nextToken();
					if (!noticeCommand.isEmpty() && (activeChar.getClan() != null))
					{
						activeChar.getClan().setNoticeEnabledAndStore(Boolean.parseBoolean(noticeCommand));
					}
				}
				sendClanNotice(activeChar, activeChar.getClanId());
			}
		}
		else
		{
			super.parseCmd(command, activeChar);
		}
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, PlayerInstance activeChar)
	{
		if (ar1.equalsIgnoreCase("intro"))
		{
			if (Integer.parseInt(ar2) != activeChar.getClanId())
			{
				return;
			}
			
			final Clan clan = ClanTable.getInstance().getClan(activeChar.getClanId());
			if (clan == null)
			{
				return;
			}
			
			clan.setIntroduction(ar3, true);
			sendClanManagement(activeChar, Integer.parseInt(ar2));
		}
		else if (ar1.equals("notice"))
		{
			activeChar.getClan().setNoticeAndStore(ar4);
			sendClanNotice(activeChar, activeChar.getClanId());
		}
		else if (ar1.equalsIgnoreCase("mail"))
		{
			if (Integer.parseInt(ar2) != activeChar.getClanId())
			{
				return;
			}
			
			final Clan clan = ClanTable.getInstance().getClan(activeChar.getClanId());
			if (clan == null)
			{
				return;
			}
			
			// Retrieve clans members, and store them under a String.
			final StringBuilder membersList = new StringBuilder();
			for (ClanMember player : clan.getMembers())
			{
				if (player != null)
				{
					if (membersList.length() > 0)
					{
						membersList.append(";");
					}
					
					membersList.append(player.getName());
				}
			}
			MailBBSManager.getInstance().sendLetter(membersList.toString(), ar4, ar5, activeChar);
			sendClanDetails(activeChar, activeChar.getClanId());
		}
		else
		{
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, activeChar);
		}
	}
	
	@Override
	protected String getFolder()
	{
		return "clan/";
	}
	
	private void sendClanMail(PlayerInstance activeChar, int clanId)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		if (clan == null)
		{
			return;
		}
		
		if ((activeChar.getClanId() != clanId) || !activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			sendClanList(activeChar, 1);
			return;
		}
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "clan/clanhome-mail.htm");
		content = content.replace("%clanid%", Integer.toString(clanId));
		content = content.replace("%clanName%", clan.getName());
		separateAndSend(content, activeChar);
	}
	
	private void sendClanManagement(PlayerInstance activeChar, int clanId)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		if (clan == null)
		{
			return;
		}
		
		if ((activeChar.getClanId() != clanId) || !activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			sendClanList(activeChar, 1);
			return;
		}
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "clan/clanhome-management.htm");
		content = content.replace("%clanid%", Integer.toString(clan.getClanId()));
		send1001(content, activeChar);
		send1002(activeChar, clan.getIntroduction(), "", "");
	}
	
	private void sendClanNotice(PlayerInstance activeChar, int clanId)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		if ((clan == null) || (activeChar.getClanId() != clanId))
		{
			return;
		}
		
		if (clan.getLevel() < 2)
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_COMMUNITIES_IN_MY_CLAN_CLAN_COMMUNITIES_ARE_ALLOWED_FOR_CLANS_WITH_SKILL_LEVELS_OF_2_AND_HIGHER);
			sendClanList(activeChar, 1);
			return;
		}
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "clan/clanhome-notice.htm");
		content = content.replace("%clanid%", Integer.toString(clan.getClanId()));
		content = content.replace("%enabled%", "[" + String.valueOf(clan.isNoticeEnabled()) + "]");
		content = content.replace("%flag%", String.valueOf(!clan.isNoticeEnabled()));
		send1001(content, activeChar);
		send1002(activeChar, clan.getNotice(), "", "");
	}
	
	private void sendClanList(PlayerInstance activeChar, int index)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "clan/clanlist.htm");
		
		// Player got a clan, show the associated header.
		final StringBuilder sb = new StringBuilder();
		final Clan playerClan = activeChar.getClan();
		if (playerClan != null)
		{
			StringUtil.append(sb, "<table width=610 bgcolor=A7A19A><tr><td width=5></td><td width=605><a action=\"bypass _bbsclan;home;", playerClan.getClanId(), "\">[GO TO MY CLAN]</a></td></tr></table>");
		}
		
		content = content.replace("%homebar%", sb.toString());
		if (index < 1)
		{
			index = 1;
		}
		
		// Cleanup sb.
		sb.setLength(0);
		
		// List of clans.
		int i = 0;
		for (Clan cl : ClanTable.getInstance().getClans())
		{
			if (i > ((index + 1) * 7))
			{
				break;
			}
			
			if (i++ >= ((index - 1) * 7))
			{
				StringUtil.append(sb, "<table width=610><tr><td width=5></td><td width=150 align=center><a action=\"bypass _bbsclan;home;", cl.getClanId(), "\">", cl.getName(), "</a></td><td width=150 align=center>", cl.getLeaderName(), "</td><td width=100 align=center>", cl.getLevel(), "</td><td width=200 align=center>", cl.getMembersCount(), "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>");
			}
		}
		sb.append("<table><tr>");
		
		if (index == 1)
		{
			sb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
		}
		else
		{
			StringUtil.append(sb, "<td><button action=\"_bbsclan;clan;", index - 1, "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		
		int nbp = ClanTable.getInstance().getClans().length / 8;
		if ((nbp * 8) != ClanTable.getInstance().getClans().length)
		{
			nbp++;
		}
		
		for (i = 1; i <= nbp; i++)
		{
			if (i == index)
			{
				StringUtil.append(sb, "<td> ", i, " </td>");
			}
			else
			{
				StringUtil.append(sb, "<td><a action=\"bypass _bbsclan;clan;", i, "\"> ", i, " </a></td>");
			}
		}
		
		if (index == nbp)
		{
			sb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>");
		}
		else
		{
			StringUtil.append(sb, "<td><button action=\"bypass _bbsclan;clan;", index + 1, "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		
		sb.append("</tr></table>");
		
		content = content.replace("%clanlist%", sb.toString());
		separateAndSend(content, activeChar);
	}
	
	private void sendClanDetails(PlayerInstance activeChar, int clanId)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		if (clan == null)
		{
			return;
		}
		
		if (clan.getLevel() < 2)
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_COMMUNITIES_IN_MY_CLAN_CLAN_COMMUNITIES_ARE_ALLOWED_FOR_CLANS_WITH_SKILL_LEVELS_OF_2_AND_HIGHER);
			sendClanList(activeChar, 1);
			return;
		}
		
		// Load different HTM following player case, 3 possibilities : randomer, member, clan leader.
		String content;
		if (activeChar.getClanId() != clanId)
		{
			content = HtmCache.getInstance().getHtm(CB_PATH + "clan/clanhome.htm");
		}
		else if (activeChar.isClanLeader())
		{
			content = HtmCache.getInstance().getHtm(CB_PATH + "clan/clanhome-leader.htm");
		}
		else
		{
			content = HtmCache.getInstance().getHtm(CB_PATH + "clan/clanhome-member.htm");
		}
		
		content = content.replace("%clanid%", Integer.toString(clan.getClanId()));
		content = content.replace("%clanIntro%", clan.getIntroduction());
		content = content.replace("%clanName%", clan.getName());
		content = content.replace("%clanLvL%", Integer.toString(clan.getLevel()));
		content = content.replace("%clanMembers%", Integer.toString(clan.getMembersCount()));
		content = content.replace("%clanLeader%", clan.getLeaderName());
		content = content.replace("%allyName%", (clan.getAllyId() > 0) ? clan.getAllyName() : "");
		separateAndSend(content, activeChar);
	}
	
	private static class SingletonHolder
	{
		protected static final ClanBBSManager INSTANCE = new ClanBBSManager();
	}
}