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
package org.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.ExCloseMPCC;
import org.l2jserver.gameserver.network.serverpackets.ExOpenMPCC;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author chris_00
 */
public class CommandChannel
{
	private final Collection<Party> _parties;
	private PlayerInstance _commandLeader = null;
	private int _channelLvl;
	
	/**
	 * Creates a New Command Channel and Add the Leaders party to the CC
	 * @param leader
	 */
	public CommandChannel(PlayerInstance leader)
	{
		_commandLeader = leader;
		_parties = ConcurrentHashMap.newKeySet();
		_parties.add(leader.getParty());
		_channelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		leader.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessageId.THE_COMMAND_CHANNEL_HAS_BEEN_FORMED));
		leader.getParty().broadcastToPartyMembers(new ExOpenMPCC());
	}
	
	/**
	 * Adds a Party to the Command Channel
	 * @param party
	 */
	public void addParty(Party party)
	{
		if (party == null)
		{
			return;
		}
		
		_parties.add(party);
		
		if (party.getLevel() > _channelLvl)
		{
			_channelLvl = party.getLevel();
		}
		
		party.setCommandChannel(this);
		party.broadcastToPartyMembers(new SystemMessage(SystemMessageId.YOU_HAVE_JOINED_THE_COMMAND_CHANNEL));
		party.broadcastToPartyMembers(new ExOpenMPCC());
	}
	
	/**
	 * Removes a Party from the Command Channel
	 * @param party
	 */
	public void removeParty(Party party)
	{
		if (party == null)
		{
			return;
		}
		
		_parties.remove(party);
		_channelLvl = 0;
		for (Party pty : _parties)
		{
			if (pty.getLevel() > _channelLvl)
			{
				_channelLvl = pty.getLevel();
			}
		}
		
		party.setCommandChannel(null);
		party.broadcastToPartyMembers(new ExCloseMPCC());
		if (_parties.size() < 2)
		{
			broadcastToChannelMembers(new SystemMessage(SystemMessageId.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED));
			disbandChannel();
		}
	}
	
	/**
	 * disbands the whole Command Channel
	 */
	public void disbandChannel()
	{
		if (_parties != null)
		{
			for (Party party : _parties)
			{
				if (party != null)
				{
					removeParty(party);
				}
			}
			_parties.clear();
		}
	}
	
	/**
	 * @return overall member count of the Command Channel
	 */
	public int getMemberCount()
	{
		int count = 0;
		for (Party party : _parties)
		{
			if (party != null)
			{
				count += party.getMemberCount();
			}
		}
		return count;
	}
	
	/**
	 * Broadcast packet to every channel member
	 * @param gsp
	 */
	public void broadcastToChannelMembers(GameServerPacket gsp)
	{
		if ((_parties != null) && !_parties.isEmpty())
		{
			for (Party party : _parties)
			{
				if (party != null)
				{
					party.broadcastToPartyMembers(gsp);
				}
			}
		}
	}
	
	public void broadcastCSToChannelMembers(CreatureSay gsp, PlayerInstance broadcaster)
	{
		if ((_parties != null) && !_parties.isEmpty())
		{
			for (Party party : _parties)
			{
				if (party != null)
				{
					party.broadcastCSToPartyMembers(gsp, broadcaster);
				}
			}
		}
	}
	
	/**
	 * @return list of Parties in Command Channel
	 */
	public Collection<Party> getParties()
	{
		return _parties;
	}
	
	/**
	 * @return list of all Members in Command Channel
	 */
	public List<PlayerInstance> getMembers()
	{
		final List<PlayerInstance> members = new ArrayList<>();
		for (Party party : _parties)
		{
			members.addAll(party.getPartyMembers());
		}
		return members;
	}
	
	/**
	 * @return Level of CC
	 */
	public int getLevel()
	{
		return _channelLvl;
	}
	
	/**
	 * @param leader the leader of the Command Channel
	 */
	public void setChannelLeader(PlayerInstance leader)
	{
		_commandLeader = leader;
	}
	
	/**
	 * @return the leader of the Command Channel
	 */
	public PlayerInstance getChannelLeader()
	{
		return _commandLeader;
	}
	
	/**
	 * Queen Ant, Core, Orfen, Zaken: MemberCount > 36<br>
	 * Baium: MemberCount > 56<br>
	 * Antharas: MemberCount > 225<br>
	 * Valakas: MemberCount > 99<br>
	 * normal RaidBoss: MemberCount > 18
	 * @param obj
	 * @return true if proper condition for RaidWar
	 */
	public boolean meetRaidWarCondition(WorldObject obj)
	{
		if (!(obj instanceof RaidBossInstance) || !(obj instanceof GrandBossInstance))
		{
			return false;
		}
		
		final int npcId = ((Attackable) obj).getNpcId();
		
		switch (npcId)
		{
			case 29001: // Queen Ant
			case 29006: // Core
			case 29014: // Orfen
			case 29022: // Zaken
			{
				return getMemberCount() > 36;
			}
			case 29020: // Baium
			{
				return getMemberCount() > 56;
			}
			case 29019: // Antharas
			{
				return getMemberCount() > 225;
			}
			case 29028: // Valakas
			{
				return getMemberCount() > 99;
			}
			default: // normal Raidboss
			{
				return getMemberCount() > 18;
			}
		}
	}
}
