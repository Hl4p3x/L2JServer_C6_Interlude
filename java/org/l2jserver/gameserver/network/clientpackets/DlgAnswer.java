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
package org.l2jserver.gameserver.network.clientpackets;

import org.l2jserver.Config;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;

/**
 * @author Dezmond_snz - Packet Format: cddd
 */
public class DlgAnswer extends GameClientPacket
{
	private int _messageId;
	private int _answer;
	private int _requestId;
	
	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_requestId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Long answerTime = getClient().getPlayer().getConfirmDlgRequestTime(_requestId);
		if ((_answer == 1) && (answerTime != null) && (System.currentTimeMillis() > answerTime))
		{
			_answer = 0;
		}
		getClient().getPlayer().removeConfirmDlgRequestTime(_requestId);
		
		if (_messageId == SystemMessageId.S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION.getId())
		{
			player.reviveAnswer(_answer);
		}
		else if (_messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
		{
			player.teleportAnswer(_answer, _requestId);
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
		{
			player.gatesAnswer(_answer, 1);
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
		{
			player.gatesAnswer(_answer, 0);
		}
		else if ((_messageId == 614) && Config.ALLOW_WEDDING)
		{
			player.EngageAnswer(_answer);
		}
		else if (_messageId == SystemMessageId.S1.getId())
		{
			if (player.dialog != null)
			{
				player.dialog.onDlgAnswer(player);
				player.dialog = null;
			}
		}
	}
}