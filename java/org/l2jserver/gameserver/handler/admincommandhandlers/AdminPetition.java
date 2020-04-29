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

import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.instancemanager.PetitionManager;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles commands for GMs to respond to petitions.
 * @author Tempy
 */
public class AdminPetition implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_view_petitions",
		"admin_view_petition",
		"admin_accept_petition",
		"admin_reject_petition",
		"admin_reset_petitions"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case "admin_view_petitions":
			{
				PetitionManager.getInstance().sendPendingPetitionList(activeChar);
				return true;
			}
			case "admin_view_petition":
			{
				int petitionId = -1;
				if (st.hasMoreTokens())
				{
					try
					{
						petitionId = Integer.parseInt(st.nextToken());
					}
					catch (Exception e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //admin_view_petition petition_id");
						return false;
					}
				}
				PetitionManager.getInstance().viewPetition(activeChar, petitionId);
				return true;
			}
			case "admin_accept_petition":
			{
				if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.YOU_MAY_ONLY_SUBMIT_ONE_PETITION_ACTIVE_AT_A_TIME);
					return true;
				}
				int petitionId = -1;
				if (st.hasMoreTokens())
				{
					try
					{
						petitionId = Integer.parseInt(st.nextToken());
					}
					catch (Exception e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //admin_accept_petition petition_id");
						return false;
					}
				}
				if (PetitionManager.getInstance().isPetitionInProcess(petitionId))
				{
					activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
					return true;
				}
				if (!PetitionManager.getInstance().acceptPetition(activeChar, petitionId))
				{
					activeChar.sendPacket(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION);
					return false;
				}
				return true;
			}
			case "admin_reject_petition":
			{
				int petitionId = -1;
				if (st.hasMoreTokens())
				{
					try
					{
						petitionId = Integer.parseInt(st.nextToken());
					}
					catch (Exception e)
					{
						BuilderUtil.sendSysMessage(activeChar, "Usage: //admin_reject_petition petition_id");
						return false;
					}
				}
				if (!PetitionManager.getInstance().rejectPetition(activeChar, petitionId))
				{
					activeChar.sendPacket(SystemMessageId.FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER);
					return false;
				}
				return true;
			}
			case "admin_reset_petitions":
			{
				if (PetitionManager.getInstance().isPetitionInProcess())
				{
					activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
					return false;
				}
				PetitionManager.getInstance().clearPendingPetitions();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
