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

import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.GMViewPledgeInfo;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * <b>Pledge Manipulation:</b><br>
 * <li>With target in a character without clan:<br>
 * //pledge create clanname
 * <li>With target in a clan leader:<br>
 * //pledge info<br>
 * //pledge dismiss<br>
 * //pledge setlevel level<br>
 * //pledge rep reputation_points
 */
public class AdminPledge implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_pledge"
	};
	
	private enum CommandEnum
	{
		admin_pledge
	}
	
	private enum ActionEnum
	{
		create,
		dismiss,
		info,
		setlevel,
		rep
	}
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_pledge:
			{
				final WorldObject target = activeChar.getTarget();
				PlayerInstance player = null;
				if (target instanceof PlayerInstance)
				{
					player = (PlayerInstance) target;
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
					showMainPage(activeChar);
					return false;
				}
				final String name = player.getName();
				ActionEnum action = null;
				String parameter = null;
				if (st.hasMoreTokens())
				{
					action = ActionEnum.valueOf(st.nextToken()); // create|info|dismiss|setlevel|rep
					if (action == null)
					{
						BuilderUtil.sendSysMessage(activeChar, "Not allowed Action on Clan");
						showMainPage(activeChar);
						return false;
					}
				}
				if ((action != ActionEnum.create) && !player.isClanLeader())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(name));
					showMainPage(activeChar);
					return false;
				}
				if (st.hasMoreTokens())
				{
					parameter = st.nextToken(); // clanname|nothing|nothing|level|rep_points
				}
				if (action != null)
				{
					switch (action)
					{
						case create:
						{
							if ((parameter == null) || (parameter.length() == 0))
							{
								BuilderUtil.sendSysMessage(activeChar, "Please, enter clan name.");
								showMainPage(activeChar);
								return false;
							}
							final long cet = player.getClanCreateExpiryTime();
							player.setClanCreateExpiryTime(0);
							final Clan clan = ClanTable.getInstance().createClan(player, parameter);
							if (clan != null)
							{
								BuilderUtil.sendSysMessage(activeChar, "Clan " + parameter + " created. Leader: " + player.getName());
								return true;
							}
							player.setClanCreateExpiryTime(cet);
							BuilderUtil.sendSysMessage(activeChar, "There was a problem while creating the clan.");
							showMainPage(activeChar);
							return false;
						}
						case dismiss:
						{
							ClanTable.getInstance().destroyClan(player.getClanId());
							final Clan clan = player.getClan();
							if (clan == null)
							{
								BuilderUtil.sendSysMessage(activeChar, "Clan disbanded.");
								return true;
							}
							BuilderUtil.sendSysMessage(activeChar, "There was a problem while destroying the clan.");
							showMainPage(activeChar);
							return false;
						}
						case info:
						{
							activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
							return true;
						}
						case rep:
						{
							if (parameter == null)
							{
								BuilderUtil.sendSysMessage(activeChar, "Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							int points = player.getClan().getReputationScore();
							try
							{
								points = Integer.parseInt(parameter);
							}
							catch (NumberFormatException nfe)
							{
								BuilderUtil.sendSysMessage(activeChar, "Points incorrect.");
								BuilderUtil.sendSysMessage(activeChar, "Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							final Clan clan = player.getClan();
							if (clan.getLevel() < 5)
							{
								BuilderUtil.sendSysMessage(activeChar, "Only clans of level 5 or above may receive reputation points.");
								showMainPage(activeChar);
								return false;
							}
							clan.setReputationScore(clan.getReputationScore() + points, true);
							BuilderUtil.sendSysMessage(activeChar, "You " + (points > 0 ? "add " : "remove ") + Math.abs(points) + " points " + (points > 0 ? "to " : "from ") + clan.getName() + "'s reputation. Their current score is " + clan.getReputationScore());
							return true;
						}
						case setlevel:
						{
							if (parameter == null)
							{
								BuilderUtil.sendSysMessage(activeChar, "Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							int level = player.getClan().getLevel();
							try
							{
								level = Integer.parseInt(parameter);
							}
							catch (NumberFormatException nfe)
							{
								BuilderUtil.sendSysMessage(activeChar, "Level incorrect.");
								BuilderUtil.sendSysMessage(activeChar, "Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							if ((level >= 0) && (level < 9))
							{
								player.getClan().changeLevel(level);
								BuilderUtil.sendSysMessage(activeChar, "You set level " + level + " for clan " + player.getClan().getName());
								return true;
							}
							BuilderUtil.sendSysMessage(activeChar, "Level incorrect.");
							BuilderUtil.sendSysMessage(activeChar, "Usage: //pledge <setlevel|rep> <number>");
							showMainPage(activeChar);
							return false;
						}
						default:
						{
							BuilderUtil.sendSysMessage(activeChar, "Clan Action not allowed...");
							showMainPage(activeChar);
							return false;
						}
					}
				}
				return false;
			}
			default:
			{
				BuilderUtil.sendSysMessage(activeChar, "Clan command not allowed");
				showMainPage(activeChar);
				return false;
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(PlayerInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
	}
}
