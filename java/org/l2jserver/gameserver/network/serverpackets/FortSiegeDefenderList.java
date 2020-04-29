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

//import java.util.Calendar; //signed time related
//

import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.SiegeClan;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.siege.Fort;

/**
 * Populates the Siege Defender List in the SiegeInfo Window<br>
 * <br>
 * packet type id 0xcb<br>
 * format: cddddddd + dSSdddSSd<br>
 * <br>
 * c = 0xcb<br>
 * d = FortID<br>
 * d = unknow (0x00)<br>
 * d = unknow (0x01)<br>
 * d = unknow (0x00)<br>
 * d = Number of Defending Clans?<br>
 * d = Number of Defending Clans<br>
 * { //repeats<br>
 * d = ClanID<br>
 * S = ClanName<br>
 * S = ClanLeaderName<br>
 * d = ClanCrestID<br>
 * d = signed time (seconds)<br>
 * d = Type -> Owner = 0x01 || Waiting = 0x02 || Accepted = 0x03<br>
 * d = AllyID<br>
 * S = AllyName<br>
 * S = AllyLeaderName<br>
 * d = AllyCrestID<br>
 * @author programmos
 */
public class FortSiegeDefenderList extends GameServerPacket
{
	private final Fort _fort;
	
	public FortSiegeDefenderList(Fort fort)
	{
		_fort = fort;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xcb);
		writeD(_fort.getFortId());
		writeD(0x00); // 0
		writeD(0x01); // 1
		writeD(0x00); // 0
		final int size = _fort.getSiege().getDefenderClans().size() + _fort.getSiege().getDefenderWaitingClans().size();
		if (size > 0)
		{
			Clan clan;
			writeD(size);
			writeD(size);
			// Listing the Lord and the approved clans
			for (SiegeClan siegeclan : _fort.getSiege().getDefenderClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not storated by L2J)
				switch (siegeclan.getType())
				{
					case OWNER:
					{
						writeD(0x01); // owner
						break;
					}
					case DEFENDER_PENDING:
					{
						writeD(0x02); // approved
						break;
					}
					case DEFENDER:
					{
						writeD(0x03); // waiting approved
						break;
					}
					default:
					{
						writeD(0x00);
						break;
					}
				}
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
			for (SiegeClan siegeclan : _fort.getSiege().getDefenderWaitingClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not storated by L2J)
				writeD(0x02); // waiting approval
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}
}
