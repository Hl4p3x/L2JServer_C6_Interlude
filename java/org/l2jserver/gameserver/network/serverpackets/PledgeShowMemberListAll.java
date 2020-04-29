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
package org.l2jserver.gameserver.network.serverpackets;

import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.clan.Clan.SubPledge;
import org.l2jserver.gameserver.model.clan.ClanMember;

//
/**
 * sample 0000: 68 b1010000 48 00 61 00 6d 00 62 00 75 00 72 00 67 00 00 00 H.a.m.b.u.r.g... 43 00 61 00 6c 00 61 00 64 00 6f 00 6e 00 00 00 C.a.l.a.d.o.n... 00000000 crestid | not used (nuocnam) 00000000 00000000 00000000 00000000 22000000 00000000 00000000 00000000 ally id 00 00 ally name 00000000
 * ally crrest id 02000000 6c 00 69 00 74 00 68 00 69 00 75 00 6d 00 31 00 00 00 l.i.t.h.i.u.m... 0d000000 level 12000000 class id 00000000 01000000 offline 1=true 00000000 45 00 6c 00 61 00 6e 00 61 00 00 00 E.l.a.n.a... 08000000 19000000 01000000 01000000 00000000 format dSS dddddddddSdd d
 * (Sddddd) dddSS dddddddddSdd d (Sdddddd)
 * @version $Revision: 1.6.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeShowMemberListAll extends GameServerPacket
{
	private final Clan _clan;
	private final PlayerInstance _player;
	private final ClanMember[] _members;
	private int _pledgeType;
	
	// private static final Logger LOGGER = Logger.getLogger(PledgeShowMemberListAll.class);
	
	public PledgeShowMemberListAll(Clan clan, PlayerInstance player)
	{
		_clan = clan;
		_player = player;
		_members = _clan.getMembers();
	}
	
	@Override
	protected final void writeImpl()
	{
		_pledgeType = 0;
		writePledge(0);
		
		final SubPledge[] subPledge = _clan.getAllSubPledges();
		for (SubPledge element : subPledge)
		{
			_player.sendPacket(new PledgeReceiveSubPledgeCreated(element));
		}
		
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_player.sendPacket(new PledgeShowMemberListAdd(m));
		}
		
		// unless this is sent sometimes, the client doesn't recognise the player as the leader
		_player.sendPacket(new UserInfo(_player));
	}
	
	void writePledge(int mainOrSubpledge)
	{
		final int TOP = ClanTable.getInstance().getTopRate(_clan.getClanId());
		writeC(0x53);
		
		writeD(mainOrSubpledge); // c5 main clan 0 or any subpledge 1?
		writeD(_clan.getClanId());
		writeD(_pledgeType); // c5 - possibly pledge type?
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		
		writeD(_clan.getCrestId()); // crest id .. is used again
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(_clan.getHasHideout());
		writeD(TOP);
		writeD(_clan.getReputationScore()); // was activechar lvl
		writeD(0); // 0
		writeD(0); // 0
		
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar());
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		int yellow;
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			if (m.getPledgeType() == -1)
			{
				yellow = m.getSponsor() != 0 ? 1 : 0;
			}
			else if (m.getPlayerInstance() != null)
			{
				yellow = m.getPlayerInstance().isClanLeader() ? 1 : 0;
			}
			else
			{
				yellow = 0;
			}
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			writeD(0);
			writeD(m.getObjectId());
			writeD(m.isOnline() ? 1 : 0);
			writeD(yellow);
		}
	}
}
