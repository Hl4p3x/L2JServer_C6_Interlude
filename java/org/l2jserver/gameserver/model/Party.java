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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.instancemanager.DuelManager;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.entity.DimensionalRift;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSignsFestival;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jserver.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.ExCloseMPCC;
import org.l2jserver.gameserver.network.serverpackets.ExOpenMPCC;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.PartyMemberPosition;
import org.l2jserver.gameserver.network.serverpackets.PartySmallWindowAdd;
import org.l2jserver.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jserver.gameserver.network.serverpackets.PartySmallWindowDelete;
import org.l2jserver.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

/**
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class Party
{
	private static final double[] BONUS_EXP_SP =
	{
		1,
		1.30,
		1.39,
		1.50,
		1.54,
		1.58,
		1.63,
		1.67,
		1.71
	};
	
	private static final int PARTY_POSITION_BROADCAST = 10000;
	
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	
	private final List<PlayerInstance> _members;
	private boolean _pendingInvitation = false;
	private long _pendingInviteTimeout;
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemLastLoot = 0;
	
	private CommandChannel _commandChannel = null;
	private DimensionalRift _dr;
	
	private Future<?> _positionBroadcastTask = null;
	protected PartyMemberPosition _positionPacket;
	
	/**
	 * constructor ensures party has always one member - leader
	 * @param leader
	 * @param itemDistribution
	 */
	public Party(PlayerInstance leader, int itemDistribution)
	{
		_members = new ArrayList<>();
		_itemDistribution = itemDistribution;
		_members.add(leader);
		_partyLvl = leader.getLevel();
	}
	
	/**
	 * returns number of party members
	 * @return
	 */
	public int getMemberCount()
	{
		return _members.size();
	}
	
	/**
	 * Check if another player can start invitation process
	 * @return boolean if party waits for invitation respond
	 */
	public boolean getPendingInvitation()
	{
		return _pendingInvitation;
	}
	
	/**
	 * set invitation process flag and store time for expiration happens when: player join party or player decline to join
	 * @param value
	 */
	public void setPendingInvitation(boolean value)
	{
		_pendingInvitation = value;
		_pendingInviteTimeout = GameTimeController.getGameTicks() + (PlayerInstance.REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND);
	}
	
	/**
	 * Check if player invitation is expired
	 * @return boolean if time is expired
	 * @see org.l2jserver.gameserver.model.actor.instance.PlayerInstance#isRequestExpired()
	 */
	public boolean isInvitationRequestExpired()
	{
		return (_pendingInviteTimeout <= GameTimeController.getGameTicks());
	}
	
	/**
	 * returns all party members
	 * @return
	 */
	public List<PlayerInstance> getPartyMembers()
	{
		return _members;
	}
	
	/**
	 * get random member from party
	 * @param itemId
	 * @param target
	 * @return
	 */
	private PlayerInstance getCheckedRandomMember(int itemId, Creature target)
	{
		final List<PlayerInstance> availableMembers = new ArrayList<>();
		for (PlayerInstance member : _members)
		{
			if (member.getInventory().validateCapacityByItemId(itemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, target, member, true))
			{
				availableMembers.add(member);
			}
		}
		
		if (!availableMembers.isEmpty())
		{
			return availableMembers.get(Rnd.get(availableMembers.size()));
		}
		return null;
	}
	
	/**
	 * get next item looter
	 * @param itemId
	 * @param target
	 * @return
	 */
	private PlayerInstance getCheckedNextLooter(int itemId, Creature target)
	{
		for (int i = 0; i < _members.size(); i++)
		{
			if (++_itemLastLoot >= _members.size())
			{
				_itemLastLoot = 0;
			}
			
			PlayerInstance member;
			try
			{
				member = _members.get(_itemLastLoot);
				if (member.getInventory().validateCapacityByItemId(itemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE, target, member, true))
				{
					return member;
				}
			}
			catch (Exception e)
			{
				// continue, take another member if this just logged off
			}
		}
		
		return null;
	}
	
	/**
	 * get next item looter
	 * @param player
	 * @param itemId
	 * @param spoil
	 * @param target
	 * @return
	 */
	private PlayerInstance getActualLooter(PlayerInstance player, int itemId, boolean spoil, Creature target)
	{
		PlayerInstance looter = player;
		
		switch (_itemDistribution)
		{
			case ITEM_RANDOM:
			{
				if (!spoil)
				{
					looter = getCheckedRandomMember(itemId, target);
				}
				break;
			}
			case ITEM_RANDOM_SPOIL:
			{
				looter = getCheckedRandomMember(itemId, target);
				break;
			}
			case ITEM_ORDER:
			{
				if (!spoil)
				{
					looter = getCheckedNextLooter(itemId, target);
				}
				break;
			}
			case ITEM_ORDER_SPOIL:
			{
				looter = getCheckedNextLooter(itemId, target);
				break;
			}
		}
		
		if (looter == null)
		{
			looter = player;
		}
		
		return looter;
	}
	
	/**
	 * true if player is party leader
	 * @param player
	 * @return
	 */
	public boolean isLeader(PlayerInstance player)
	{
		return getLeader().equals(player);
	}
	
	/**
	 * Returns the Object ID for the party leader to be used as a unique identifier of this party
	 * @return int
	 */
	public int getPartyLeaderOID()
	{
		return getLeader().getObjectId();
	}
	
	/**
	 * Broadcasts packet to every party member
	 * @param msg
	 */
	public void broadcastToPartyMembers(GameServerPacket msg)
	{
		for (PlayerInstance member : _members)
		{
			if (member != null)
			{
				member.sendPacket(msg);
			}
		}
	}
	
	public void broadcastToPartyMembersNewLeader()
	{
		for (PlayerInstance member : _members)
		{
			if (member != null)
			{
				member.sendPacket(new PartySmallWindowDeleteAll());
				member.sendPacket(new PartySmallWindowAll(member, this));
				member.broadcastUserInfo();
			}
		}
	}
	
	public void broadcastCSToPartyMembers(CreatureSay msg, PlayerInstance broadcaster)
	{
		for (PlayerInstance member : _members)
		{
			if ((member == null) || (broadcaster == null))
			{
				continue;
			}
			
			final boolean blocked = member.getBlockList().isInBlockList(broadcaster);
			if (!blocked)
			{
				member.sendPacket(msg);
			}
		}
	}
	
	/**
	 * Send a Server->Client packet to all other PlayerInstance of the Party.
	 * @param player
	 * @param msg
	 */
	public void broadcastToPartyMembers(PlayerInstance player, GameServerPacket msg)
	{
		for (PlayerInstance member : _members)
		{
			if ((member != null) && !member.equals(player))
			{
				member.sendPacket(msg);
			}
		}
	}
	
	/**
	 * adds new member to party
	 * @param player
	 */
	public synchronized void addPartyMember(PlayerInstance player)
	{
		if (_members.contains(player))
		{
			return;
		}
		
		// sends new member party window for all members
		player.sendPacket(new PartySmallWindowAll(player, this));
		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_JOINED_S1_S_PARTY);
		msg.addString(getLeader().getName());
		player.sendPacket(msg);
		
		msg = new SystemMessage(SystemMessageId.S1_HAS_JOINED_THE_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player, this));
		
		// add player to party, adjust party level
		_members.add(player);
		if (player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
		}
		
		// update partySpelled
		for (PlayerInstance member : _members)
		{
			if (member != null)
			{
				member.updateEffectIcons(true); // update party icons only
				member.broadcastUserInfo();
			}
		}
		
		if (isInDimensionalRift())
		{
			_dr.partyMemberInvited();
		}
		
		// open the CCInformationwindow
		if (isInCommandChannel())
		{
			player.sendPacket(new ExOpenMPCC());
		}
		
		// activate position task
		if (_positionBroadcastTask == null)
		{
			_positionBroadcastTask = ThreadPool.scheduleAtFixedRate(new PositionBroadcast(), PARTY_POSITION_BROADCAST / 2, PARTY_POSITION_BROADCAST);
		}
	}
	
	/**
	 * Remove player from party Overloaded method that takes player's name as parameter
	 * @param name
	 */
	public void removePartyMember(String name)
	{
		final PlayerInstance player = getPlayerByName(name);
		if (player != null)
		{
			removePartyMember(player);
		}
	}
	
	/**
	 * Remove player from party
	 * @param player
	 */
	public void removePartyMember(PlayerInstance player)
	{
		removePartyMember(player, true);
	}
	
	public synchronized void removePartyMember(PlayerInstance player, boolean sendMessage)
	{
		if (_members.contains(player))
		{
			final boolean isLeader = isLeader(player);
			_members.remove(player);
			recalculatePartyLevel();
			
			if (player.isFestivalParticipant())
			{
				SevenSignsFestival.getInstance().updateParticipants(player, this);
			}
			
			if (player.isInDuel())
			{
				DuelManager.getInstance().onRemoveFromParty(player);
			}
			
			if (sendMessage)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY);
				final SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_LEFT_THE_PARTY);
				msg.addString(player.getName());
				broadcastToPartyMembers(msg);
			}
			
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			if (isInDimensionalRift())
			{
				_dr.partyMemberExited(player);
			}
			
			// Close the CCInfoWindow
			if (isInCommandChannel())
			{
				player.sendPacket(new ExCloseMPCC());
			}
			
			if (isLeader && (_members.size() > 1))
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_THE_PARTY_LEADER);
				msg.addString(getLeader().getName());
				broadcastToPartyMembers(msg);
				broadcastToPartyMembersNewLeader();
			}
			
			if (_members.size() == 1)
			{
				if (isInCommandChannel())
				{
					// delete the whole command channel when the party who opened the channel is disbanded
					if (_commandChannel.getChannelLeader().equals(getLeader()))
					{
						_commandChannel.disbandChannel();
					}
					else
					{
						_commandChannel.removeParty(this);
					}
				}
				
				final PlayerInstance leader = getLeader();
				if (leader != null)
				{
					leader.setParty(null);
					if (leader.isInDuel())
					{
						DuelManager.getInstance().onRemoveFromParty(leader);
					}
				}
				
				if (_positionBroadcastTask != null)
				{
					_positionBroadcastTask.cancel(false);
					_positionBroadcastTask = null;
				}
				_members.clear();
			}
		}
	}
	
	/**
	 * Change party leader (used for string arguments)
	 * @param name
	 */
	
	public void changePartyLeader(String name)
	{
		final PlayerInstance player = getPlayerByName(name);
		if ((player != null) && !player.isInDuel())
		{
			if (_members.contains(player))
			{
				if (isLeader(player))
				{
					player.sendPacket(SystemMessageId.SLOW_DOWN_YOU_ARE_ALREADY_THE_PARTY_LEADER);
				}
				else
				{
					// Swap party members
					PlayerInstance temp;
					final int p1 = _members.indexOf(player);
					temp = getLeader();
					_members.set(0, _members.get(p1));
					_members.set(p1, temp);
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_THE_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembersNewLeader();
					
					if (isInCommandChannel() && temp.equals(_commandChannel.getChannelLeader()))
					{
						_commandChannel.setChannelLeader(getLeader());
						msg = new SystemMessage(SystemMessageId.COMMAND_CHANNEL_AUTHORITY_HAS_BEEN_TRANSFERRED_TO_S1);
						msg.addString(_commandChannel.getChannelLeader().getName());
						_commandChannel.broadcastToChannelMembers(msg);
					}
					
					if (player.isInPartyMatchRoom())
					{
						final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
						room.changeLeader(player);
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_MAY_ONLY_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_MEMBER_OF_THE_PARTY);
			}
		}
	}
	
	/**
	 * finds a player in the party by name
	 * @param name
	 * @return
	 */
	private PlayerInstance getPlayerByName(String name)
	{
		for (PlayerInstance member : _members)
		{
			if (member.getName().equalsIgnoreCase(name))
			{
				return member;
			}
		}
		return null;
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(PlayerInstance player, ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}
		
		final PlayerInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);
		
		// Send messages to other party members about reward
		if (item.getCount() > 1)
		{
			final SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_OBTAINED_S3_S2);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(target, msg);
		}
		else
		{
			final SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_OBTAINED_S2);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(target, msg);
		}
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 * @param spoil
	 * @param target
	 */
	public void distributeItem(PlayerInstance player, Attackable.RewardItem item, boolean spoil, Attackable target)
	{
		if (item == null)
		{
			return;
		}
		
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}
		
		final PlayerInstance looter = getActualLooter(player, item.getItemId(), spoil, target);
		looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);
		
		// Send messages to other aprty members about reward
		if (item.getCount() > 1)
		{
			final SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_HAS_OBTAINED_S3_S2_BY_USING_SWEEPER) : new SystemMessage(SystemMessageId.S1_HAS_OBTAINED_S3_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(looter, msg);
		}
		else
		{
			final SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_HAS_OBTAINED_S2_BY_USING_SWEEPER) : new SystemMessage(SystemMessageId.S1_HAS_OBTAINED_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(looter, msg);
		}
	}
	
	/**
	 * distribute adena to party members
	 * @param player
	 * @param adena
	 * @param target
	 */
	public void distributeAdena(PlayerInstance player, int adena, Creature target)
	{
		// Get all the party members
		final List<PlayerInstance> membersList = _members;
		
		// Check the number of party members that must be rewarded (The party member must be in range to receive its reward)
		final List<PlayerInstance> rewarded = new ArrayList<>();
		for (PlayerInstance member : membersList)
		{
			if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, target, member, true))
			{
				continue;
			}
			rewarded.add(member);
		}
		
		// Avoid null exceptions, if any
		if (rewarded.isEmpty())
		{
			return;
		}
		
		// Now we can actually distribute the adena reward (Total adena split by the number of party members that are in range and must be rewarded)
		final int count = adena / rewarded.size();
		for (PlayerInstance member : rewarded)
		{
			member.addAdena("Party", count, player, true);
		}
	}
	
	/**
	 * Distribute Experience and SP rewards to PlayerInstance Party members in the known area of the last attacker.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the PlayerInstance owner of the SummonInstance (if necessary)</li>
	 * <li>Calculate the Experience and SP reward distribution rate</li>
	 * <li>Add Experience and SP to the PlayerInstance</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T GIVE rewards to PetInstance</b></font><br>
	 * Exception are PetInstances that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain
	 * @param xpReward The Experience reward to distribute
	 * @param spReward The SP reward to distribute
	 * @param rewardedMembers The list of PlayerInstance to reward
	 * @param topLvl
	 */
	public void distributeXpAndSp(long xpReward, int spReward, List<Playable> rewardedMembers, int topLvl)
	{
		SummonInstance summon = null;
		final List<Playable> validMembers = getValidMembers(rewardedMembers, topLvl);
		float penalty;
		double sqLevel;
		double preCalculation;
		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());
		double sqLevelSum = 0;
		for (Playable character : validMembers)
		{
			sqLevelSum += character.getLevel() * character.getLevel();
		}
		
		// Go through the PlayerInstances and PetInstances (not SummonInstances) that must be rewarded
		synchronized (rewardedMembers)
		{
			for (Creature member : rewardedMembers)
			{
				if (member.isDead())
				{
					continue;
				}
				
				penalty = 0;
				
				// The SummonInstance penalty
				if (member.getPet() instanceof SummonInstance)
				{
					summon = (SummonInstance) member.getPet();
					penalty = summon.getExpPenalty();
				}
				
				// Pets that leech xp from the owner (like babypets) do not get rewarded directly
				if (member instanceof PetInstance)
				{
					if (((PetInstance) member).getPetData().getOwnerExpTaken() > 0)
					{
						continue;
					}
					// TODO: This is a temporary fix while correct pet xp in party is figured out
					penalty = (float) 0.85;
				}
				
				// Calculate and add the EXP and SP reward to the member
				if (validMembers.contains(member))
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculation = (sqLevel / sqLevelSum) * (1 - penalty);
					
					// Add the XP/SP points to the requested party member
					if (!member.isDead())
					{
						member.addExpAndSp(Math.round(member.calcStat(Stat.EXPSP_RATE, xpReward * preCalculation, null, null)), (int) member.calcStat(Stat.EXPSP_RATE, spReward * preCalculation, null, null));
					}
				}
				else
				{
					member.addExpAndSp(0, 0);
				}
			}
		}
	}
	
	/**
	 * Calculates and gives final XP and SP rewards to the party member.<br>
	 * This method takes in consideration number of members, members' levels, rewarder's level and bonus modifier for the actual party.
	 * @param member is the Creature to be rewarded
	 * @param xpReward is the total amount of XP to be "splited" and given to the member
	 * @param spReward is the total amount of SP to be "splited" and given to the member
	 * @param penalty is the penalty that must be applied to the XP rewards of the requested member
	 */
	
	/**
	 * refresh party level
	 */
	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		for (PlayerInstance member : _members)
		{
			if (member == null)
			{
				_members.remove(member);
				continue;
			}
			
			if (member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}
		
		_partyLvl = newLevel;
	}
	
	private List<Playable> getValidMembers(List<Playable> members, int topLvl)
	{
		final List<Playable> validMembers = new ArrayList<>();
		
		// Fixed LevelDiff cutoff point
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (Playable member : members)
			{
				if ((topLvl - member.getLevel()) <= Config.PARTY_XP_CUTOFF_LEVEL)
				{
					validMembers.add(member);
				}
			}
		}
		// Fixed MinPercentage cutoff point
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			for (Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}
			
			for (Playable member : members)
			{
				final int sqLevel = member.getLevel() * member.getLevel();
				if ((sqLevel * 100) >= (sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT))
				{
					validMembers.add(member);
				}
			}
		}
		// Automatic cutoff method
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			for (Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}
			
			int i = members.size() - 1;
			if (i < 1)
			{
				return members;
			}
			
			if (i >= BONUS_EXP_SP.length)
			{
				i = BONUS_EXP_SP.length - 1;
			}
			
			for (Playable member : members)
			{
				final int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel >= (sqLevelSum * (1 - (1 / ((1 + BONUS_EXP_SP[i]) - BONUS_EXP_SP[i - 1])))))
				{
					validMembers.add(member);
				}
			}
		}
		return validMembers;
	}
	
	private double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		if (i < 1)
		{
			return 1;
		}
		
		if (i >= BONUS_EXP_SP.length)
		{
			i = BONUS_EXP_SP.length - 1;
		}
		
		return BONUS_EXP_SP[i];
	}
	
	private double getExpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			// not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}
	
	private double getSpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			// not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}
	
	public int getLevel()
	{
		return _partyLvl;
	}
	
	public int getLootDistribution()
	{
		return _itemDistribution;
	}
	
	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}
	
	public CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}
	
	public void setCommandChannel(CommandChannel channel)
	{
		_commandChannel = channel;
	}
	
	public boolean isInDimensionalRift()
	{
		return _dr != null;
	}
	
	public void setDimensionalRift(DimensionalRift dr)
	{
		_dr = dr;
	}
	
	public DimensionalRift getDimensionalRift()
	{
		return _dr;
	}
	
	public PlayerInstance getLeader()
	{
		try
		{
			return _members.get(0);
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}
	
	protected class PositionBroadcast implements Runnable
	{
		@Override
		public void run()
		{
			if (_positionPacket == null)
			{
				_positionPacket = new PartyMemberPosition(Party.this);
			}
			else
			{
				_positionPacket.reuse(Party.this);
			}
			
			broadcastToPartyMembers(_positionPacket);
		}
	}
}
