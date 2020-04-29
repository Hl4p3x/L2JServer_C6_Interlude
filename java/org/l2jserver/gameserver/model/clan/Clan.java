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
package org.l2jserver.gameserver.model.clan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.communitybbs.BB.Forum;
import org.l2jserver.gameserver.communitybbs.Manager.ForumsBBSManager;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.instancemanager.CrownManager;
import org.l2jserver.gameserver.instancemanager.SiegeManager;
import org.l2jserver.gameserver.model.ClanWarehouse;
import org.l2jserver.gameserver.model.ItemContainer;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jserver.gameserver.network.serverpackets.PledgeSkillListAdd;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;

public class Clan
{
	private static final Logger LOGGER = Logger.getLogger(Clan.class.getName());
	
	private String _name;
	private int _clanId;
	private ClanMember _leader;
	private final Map<String, ClanMember> _members = new HashMap<>();
	
	private String _allyName;
	private int _allyId = 0;
	private int _level;
	private int _hasCastle;
	private int _hasFort;
	private int _hasHideout;
	private boolean _hasCrest;
	private int _hiredGuards;
	private int _crestId;
	private int _crestLargeId;
	private int _allyCrestId;
	private int _auctionBiddedAt = 0;
	private long _allyPenaltyExpiryTime;
	private int _allyPenaltyType;
	private long _charPenaltyExpiryTime;
	private long _dissolvingExpiryTime;
	// Ally Penalty Types
	/** Clan leaved ally */
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	/** Clan was dismissed from ally */
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	/** Leader clan dismiss clan from ally */
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	/** Leader clan dissolve ally */
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	
	private final ItemContainer _warehouse = new ClanWarehouse(this);
	private final List<Integer> _atWarWith = new ArrayList<>();
	private final List<Integer> _atWarAttackers = new ArrayList<>();
	
	private boolean _hasCrestLarge;
	
	private Forum _forum;
	
	private final List<Skill> _skillList = new ArrayList<>();
	
	// Clan Notice
	private String _notice;
	private boolean _noticeEnabled = false;
	private static final int MAX_NOTICE_LENGTH = 512;
	
	// Clan introduction
	private String _introduction;
	private static final int MAX_INTRODUCTION_LENGTH = 300;
	
	// Clan Privileges
	/** No privilege to manage any clan activity */
	public static final int CP_NOTHING = 0;
	/** Privilege to join clan */
	public static final int CP_CL_JOIN_CLAN = 2;
	/** Privilege to give a title */
	public static final int CP_CL_GIVE_TITLE = 4;
	/** Privilege to view warehouse content */
	public static final int CP_CL_VIEW_WAREHOUSE = 8;
	/** Privilege to manage clan ranks */
	public static final int CP_CL_MANAGE_RANKS = 16;
	public static final int CP_CL_PLEDGE_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	/** Privilege to register clan crest */
	public static final int CP_CL_REGISTER_CREST = 128;
	public static final int CP_CL_MASTER_RIGHTS = 256;
	public static final int CP_CL_MANAGE_LEVELS = 512;
	/** Privilege to open a door */
	public static final int CP_CH_OPEN_DOOR = 1024;
	public static final int CP_CH_OTHER_RIGHTS = 2048;
	public static final int CP_CH_AUCTION = 4096;
	public static final int CP_CH_DISMISS = 8192;
	public static final int CP_CH_SET_FUNCTIONS = 16384;
	public static final int CP_CS_OPEN_DOOR = 32768;
	public static final int CP_CS_MANOR_ADMIN = 65536;
	public static final int CP_CS_MANAGE_SIEGE = 131072;
	public static final int CP_CS_USE_FUNCTIONS = 262144;
	public static final int CP_CS_DISMISS = 524288;
	public static final int CP_CS_TAXES = 1048576;
	public static final int CP_CS_MERCENARIES = 2097152;
	public static final int CP_CS_SET_FUNCTIONS = 4194304;
	/** Privilege to manage all clan activity */
	public static final int CP_ALL = 8388606;
	
	// Sub-unit types
	/** Clan subunit type of Academy */
	public static final int SUBUNIT_ACADEMY = -1;
	/** Clan subunit type of Royal Guard A */
	public static final int SUBUNIT_ROYAL1 = 100;
	/** Clan subunit type of Royal Guard B */
	public static final int SUBUNIT_ROYAL2 = 200;
	/** Clan subunit type of Order of Knights A-1 */
	public static final int SUBUNIT_KNIGHT1 = 1001;
	/** Clan subunit type of Order of Knights A-2 */
	public static final int SUBUNIT_KNIGHT2 = 1002;
	/** Clan subunit type of Order of Knights B-1 */
	public static final int SUBUNIT_KNIGHT3 = 2001;
	/** Clan subunit type of Order of Knights B-2 */
	public static final int SUBUNIT_KNIGHT4 = 2002;
	
	/** Map(Integer, Skill) containing all skills of the Clan */
	protected final Map<Integer, Skill> _skills = new HashMap<>();
	protected final Map<Integer, RankPrivs> _privs = new HashMap<>();
	protected final Map<Integer, SubPledge> _subPledges = new HashMap<>();
	
	private int _reputationScore = 0;
	private int _rank = 0;
	
	/**
	 * Called if a clan is referenced only by id. In this case all other data needs to be fetched from db
	 * @param clanId A valid clan Id to create and restore
	 */
	public Clan(int clanId)
	{
		_clanId = clanId;
		initializePrivs();
		
		try
		{
			restore();
			_warehouse.restore();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error restoring clan \n\t" + this + " " + e);
		}
	}
	
	/**
	 * Called only if a new clan is created
	 * @param clanId A valid clan Id to create
	 * @param clanName A valid clan name
	 */
	public Clan(int clanId, String clanName)
	{
		_clanId = clanId;
		_name = clanName;
		initializePrivs();
	}
	
	/**
	 * @return Returns the clanId.
	 */
	public int getClanId()
	{
		return _clanId;
	}
	
	/**
	 * @param clanId The clanId to set.
	 */
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	/**
	 * @return Returns the leaderId.
	 */
	public int getLeaderId()
	{
		return _leader != null ? _leader.getObjectId() : 0;
	}
	
	/**
	 * @return PledgeMember of clan leader.
	 */
	public ClanMember getLeader()
	{
		return _leader;
	}
	
	public boolean setLeader(ClanMember member)
	{
		if (member == null)
		{
			return false;
		}
		
		final ClanMember oldLeader = _leader;
		_leader = member;
		_members.put(member.getName(), member);
		
		// refresh oldleader and new leader info
		if (oldLeader != null)
		{
			final PlayerInstance exLeader = oldLeader.getPlayerInstance();
			exLeader.setClan(this);
			exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
			exLeader.setClanPrivileges(CP_NOTHING);
			
			exLeader.broadcastUserInfo();
			
			CrownManager.getInstance().checkCrowns(exLeader);
		}
		
		updateClanInDB();
		
		if (member.getPlayerInstance() != null)
		{
			final PlayerInstance newLeader = member.getPlayerInstance();
			newLeader.setClan(this);
			newLeader.setPledgeClass(member.calculatePledgeClass(newLeader));
			newLeader.setClanPrivileges(CP_ALL);
			
			newLeader.broadcastUserInfo();
		}
		
		broadcastClanStatus();
		
		CrownManager.getInstance().checkCrowns(member.getPlayerInstance());
		
		return true;
	}
	
	public void setNewLeader(ClanMember member, PlayerInstance player)
	{
		if (player.isRiding() || player.isFlying())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!_leader.isOnline())
		{
			return;
		}
		
		if (member == null)
		{
			return;
		}
		
		if (!member.isOnline())
		{
			return;
		}
		
		if (setLeader(member))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_LORD_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1);
			sm.addString(member.getName());
			broadcastToOnlineMembers(sm);
		}
	}
	
	/**
	 * @return Returns the leaderName.
	 */
	public String getLeaderName()
	{
		return _leader != null ? _leader.getName() : "";
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	private void addClanMember(ClanMember member)
	{
		_members.put(member.getName(), member);
	}
	
	public void addClanMember(PlayerInstance player)
	{
		final ClanMember member = new ClanMember(this, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
		
		// store in memory
		addClanMember(member);
		member.setPlayerInstance(player);
		player.setClan(this);
		player.setPledgeClass(member.calculatePledgeClass(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new UserInfo(player));
		player.rewardSkills();
	}
	
	public void updateClanMember(PlayerInstance player)
	{
		final ClanMember member = new ClanMember(player);
		addClanMember(member);
	}
	
	public ClanMember getClanMember(String name)
	{
		return _members.get(name);
	}
	
	public ClanMember getClanMember(int objectID)
	{
		for (ClanMember temp : _members.values())
		{
			if (temp.getObjectId() == objectID)
			{
				return temp;
			}
		}
		return null;
	}
	
	public void removeClanMember(String name, long clanJoinExpiryTime)
	{
		final ClanMember exMember = _members.remove(name);
		if (exMember == null)
		{
			LOGGER.warning("Member " + name + " not found in clan while trying to remove");
			return;
		}
		
		final int leadssubpledge = getLeaderSubPledge(name);
		if (leadssubpledge != 0)
		{
			// Sub-unit leader withdraws, position becomes vacant and leader should appoint new via NPC
			getSubPledge(leadssubpledge).setLeaderName("");
			updateSubPledgeInDB(leadssubpledge);
		}
		
		if (exMember.getApprentice() != 0)
		{
			final ClanMember apprentice = getClanMember(exMember.getApprentice());
			if (apprentice != null)
			{
				if (apprentice.getPlayerInstance() != null)
				{
					apprentice.getPlayerInstance().setSponsor(0);
				}
				else
				{
					apprentice.initApprenticeAndSponsor(0, 0);
				}
				
				apprentice.saveApprenticeAndSponsor(0, 0);
			}
		}
		
		if (exMember.getSponsor() != 0)
		{
			final ClanMember sponsor = getClanMember(exMember.getSponsor());
			if (sponsor != null)
			{
				if (sponsor.getPlayerInstance() != null)
				{
					sponsor.getPlayerInstance().setApprentice(0);
				}
				else
				{
					sponsor.initApprenticeAndSponsor(0, 0);
				}
				
				sponsor.saveApprenticeAndSponsor(0, 0);
			}
		}
		
		exMember.saveApprenticeAndSponsor(0, 0);
		if (Config.REMOVE_CASTLE_CIRCLETS)
		{
			CastleManager.getInstance().removeCirclet(exMember, getHasCastle());
		}
		
		if (exMember.isOnline())
		{
			final PlayerInstance player = exMember.getPlayerInstance();
			player.setTitle("");
			player.setApprentice(0);
			player.setSponsor(0);
			
			if (player.isClanLeader())
			{
				SiegeManager.getInstance().removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + (Config.ALT_CLAN_CREATE_DAYS * 86400000)); // 24*60*60*1000 = 86400000
			}
			
			// remove Clan skills from Player
			for (Skill skill : player.getClan().getAllSkills())
			{
				player.removeSkill(skill, false);
			}
			
			player.setClan(null);
			player.setClanJoinExpiryTime(clanJoinExpiryTime);
			player.setPledgeClass(exMember.calculatePledgeClass(player));
			player.broadcastUserInfo();
			// disable clan tab
			player.sendPacket(new PledgeShowMemberListDeleteAll());
		}
		else
		{
			removeMemberInDatabase(exMember, clanJoinExpiryTime, getLeaderName().equalsIgnoreCase(name) ? System.currentTimeMillis() + (Config.ALT_CLAN_CREATE_DAYS * 86400000) : 0);
		}
	}
	
	public ClanMember[] getMembers()
	{
		return _members.values().toArray(new ClanMember[_members.size()]);
	}
	
	public Integer[] getOfflineMembersIds()
	{
		final List<Integer> list = new ArrayList<>();
		for (ClanMember temp : _members.values())
		{
			if ((temp != null) && !temp.isOnline())
			{
				list.add(temp.getObjectId());
			}
		}
		return list.toArray(new Integer[list.size()]);
	}
	
	public int getMembersCount()
	{
		return _members.size();
	}
	
	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;
		for (ClanMember temp : _members.values())
		{
			if (temp.getPledgeType() == subpl)
			{
				result++;
			}
		}
		return result;
	}
	
	public int getMaxNrOfMembers(int pledgetype)
	{
		int limit = 0;
		
		switch (pledgetype)
		{
			case 0:
			{
				switch (_level)
				{
					case 4:
					{
						limit = 40;
						break;
					}
					case 3:
					{
						limit = 30;
						break;
					}
					case 2:
					{
						limit = 20;
						break;
					}
					case 1:
					{
						limit = 15;
						break;
					}
					case 0:
					{
						limit = 10;
						break;
					}
					default:
					{
						limit = 40;
						break;
					}
				}
				break;
			}
			case -1:
			case 100:
			case 200:
			{
				limit = 20;
				break;
			}
			case 1001:
			case 1002:
			case 2001:
			case 2002:
			{
				limit = 10;
				break;
			}
			default:
			{
				break;
			}
		}
		return limit;
	}
	
	public PlayerInstance[] getOnlineMembers()
	{
		final List<PlayerInstance> result = new ArrayList<>();
		for (ClanMember temp : _members.values())
		{
			try
			{
				if (temp.isOnline())
				{
					result.add(temp.getPlayerInstance());
				}
			}
			catch (NullPointerException e)
			{
				LOGGER.warning(e.toString());
			}
		}
		return result.toArray(new PlayerInstance[result.size()]);
	}
	
	public int getAllyId()
	{
		return _allyId;
	}
	
	public String getAllyName()
	{
		return _allyName;
	}
	
	public void setAllyCrestId(int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}
	
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getHasCastle()
	{
		return _hasCastle;
	}
	
	public int getHasFort()
	{
		return _hasFort;
	}
	
	public int getHasHideout()
	{
		return _hasHideout;
	}
	
	/**
	 * @param crestId The id of pledge crest.
	 */
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}
	
	public int getCrestId()
	{
		return _crestId;
	}
	
	public void setCrestLargeId(int crestLargeId)
	{
		_crestLargeId = crestLargeId;
	}
	
	public int getCrestLargeId()
	{
		return _crestLargeId;
	}
	
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	
	public void setHasCastle(int hasCastle)
	{
		_hasCastle = hasCastle;
	}
	
	public void setHasFort(int hasFort)
	{
		_hasFort = hasFort;
	}
	
	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}
	
	public void setLevel(int level)
	{
		_level = level;
		if (Config.ENABLE_COMMUNITY_BOARD && (_level >= 2) && (_forum == null))
		{
			final Forum forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");
			if (forum != null)
			{
				_forum = forum.getChildByName(_name);
				if (_forum == null)
				{
					_forum = ForumsBBSManager.getInstance().createNewForum(_name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), Forum.CLAN, Forum.CLANMEMBERONLY, _clanId);
				}
			}
		}
	}
	
	public boolean isMember(String name)
	{
		return (name != null) && _members.containsKey(name);
	}
	
	public void updateClanInDB()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, _allyId);
			statement.setString(3, _allyName);
			statement.setInt(4, _reputationScore);
			statement.setLong(5, _allyPenaltyExpiryTime);
			statement.setInt(6, _allyPenaltyType);
			statement.setLong(7, _charPenaltyExpiryTime);
			statement.setLong(8, _dissolvingExpiryTime);
			statement.setInt(9, _clanId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("error while saving new clan leader to db " + e);
		}
	}
	
	public void store()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) values (?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _clanId);
			statement.setString(2, _name);
			statement.setInt(3, _level);
			statement.setInt(4, _hasCastle);
			statement.setInt(5, _allyId);
			statement.setString(6, _allyName);
			statement.setInt(7, getLeaderId());
			statement.setInt(8, _crestId);
			statement.setInt(9, _crestLargeId);
			statement.setInt(10, _allyCrestId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("error while saving new clan to db " + e);
		}
	}
	
	private void removeMemberInDatabase(ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?");
			statement.setString(1, "");
			statement.setLong(2, clanJoinExpiryTime);
			statement.setLong(3, clanCreateExpiryTime);
			statement.setInt(4, member.getObjectId());
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("error while removing clan member in db " + e);
		}
	}
	
	private void restore()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			ClanMember member;
			
			final PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,reputation_score,auction_bid_at,ally_penalty_expiry_time,ally_penalty_type,char_penalty_expiry_time,dissolving_expiry_time FROM clan_data where clan_id=?");
			statement.setInt(1, _clanId);
			final ResultSet clanData = statement.executeQuery();
			if (clanData.next())
			{
				setName(clanData.getString("clan_name"));
				setLevel(clanData.getInt("clan_level"));
				setHasCastle(clanData.getInt("hasCastle"));
				setAllyId(clanData.getInt("ally_id"));
				setAllyName(clanData.getString("ally_name"));
				setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
				if (_allyPenaltyExpiryTime < System.currentTimeMillis())
				{
					setAllyPenaltyExpiryTime(0, 0);
				}
				
				setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
				
				if ((_charPenaltyExpiryTime + (Config.ALT_CLAN_JOIN_DAYS * 86400000)) < System.currentTimeMillis()) // 24*60*60*1000 = 86400000
				{
					setCharPenaltyExpiryTime(0);
				}
				
				setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));
				
				setCrestId(clanData.getInt("crest_id"));
				
				if (_crestId != 0)
				{
					setHasCrest(true);
				}
				
				setCrestLargeId(clanData.getInt("crest_large_id"));
				
				if (_crestLargeId != 0)
				{
					setHasCrestLarge(true);
				}
				
				setAllyCrestId(clanData.getInt("ally_crest_id"));
				setReputationScore(clanData.getInt("reputation_score"), false);
				setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
				
				final int leaderId = clanData.getInt("leader_id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge,apprentice,sponsor FROM characters WHERE clanid=?");
				statement2.setInt(1, _clanId);
				final ResultSet clanMembers = statement2.executeQuery();
				
				while (clanMembers.next())
				{
					member = new ClanMember(this, clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"), clanMembers.getInt("subpledge"), clanMembers.getInt("power_grade"), clanMembers.getString("title"));
					if (member.getObjectId() == leaderId)
					{
						setLeader(member);
					}
					else
					{
						addClanMember(member);
					}
					member.initApprenticeAndSponsor(clanMembers.getInt("apprentice"), clanMembers.getInt("sponsor"));
				}
				clanMembers.close();
				statement2.close();
			}
			
			clanData.close();
			statement.close();
			
			restoreSubPledges();
			restoreRankPrivs();
			restoreSkills();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while restoring clan: " + e);
		}
	}
	
	private void storeNotice(String notice, boolean enabled)
	{
		if (notice == null)
		{
			notice = "";
		}
		
		if (notice.length() > MAX_NOTICE_LENGTH)
		{
			notice = notice.substring(0, MAX_NOTICE_LENGTH - 1);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET enabled=?,notice=? WHERE clan_id=?");
			statement.setString(1, (enabled) ? "true" : "false");
			statement.setString(2, notice);
			statement.setInt(3, _clanId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error could not store clan notice: " + e.getMessage() + " " + e);
		}
		
		_notice = notice;
		_noticeEnabled = enabled;
	}
	
	public void setNoticeEnabledAndStore(boolean enabled)
	{
		storeNotice(_notice, enabled);
	}
	
	public void setNoticeAndStore(String notice)
	{
		storeNotice(notice, _noticeEnabled);
	}
	
	public boolean isNoticeEnabled()
	{
		return _noticeEnabled;
	}
	
	public void setNoticeEnabled(boolean enabled)
	{
		_noticeEnabled = enabled;
	}
	
	public String getNotice()
	{
		return (_notice == null) ? "" : _notice;
	}
	
	public void setNotice(String notice)
	{
		_notice = notice;
	}
	
	public String getIntroduction()
	{
		return (_introduction == null) ? "" : _introduction;
	}
	
	public void setIntroduction(String intro, boolean saveOnDb)
	{
		if (saveOnDb)
		{
			if (intro == null)
			{
				intro = "";
			}
			
			if (intro.length() > MAX_INTRODUCTION_LENGTH)
			{
				intro = intro.substring(0, MAX_INTRODUCTION_LENGTH - 1);
			}
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET introduction=? WHERE clan_id=?");
				statement.setString(1, intro);
				statement.setInt(2, _clanId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("Could not store clan introduction: " + e.getMessage());
			}
		}
		
		_introduction = intro;
	}
	
	private void restoreSkills()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, _clanId);
			
			final ResultSet rset = statement.executeQuery();
			
			// Go though the recordset of this SQL query
			while (rset.next())
			{
				final int id = rset.getInt("skill_id");
				final int level = rset.getInt("skill_level");
				
				// Create a Skill object for each record
				final Skill skill = SkillTable.getInstance().getInfo(id, level);
				
				// Add the Skill object to the Clan _skills
				_skills.put(skill.getId(), skill);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore clan skills: " + e);
		}
	}
	
	public Skill[] getAllSkills()
	{
		return _skills.values().toArray(new Skill[_skills.values().size()]);
	}
	
	/**
	 * used to add a skill to skill list of this Pledge
	 * @param newSkill
	 * @return
	 */
	public Skill addSkill(Skill newSkill)
	{
		Skill oldSkill = null;
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
		}
		return oldSkill;
	}
	
	/**
	 * used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db
	 * @param newSkill
	 * @return
	 */
	public Skill addNewSkill(Skill newSkill)
	{
		Skill oldSkill = null;
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			
			PreparedStatement statement;
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				if (oldSkill != null)
				{
					statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
					statement.setInt(1, newSkill.getLevel());
					statement.setInt(2, oldSkill.getId());
					statement.setInt(3, _clanId);
					statement.execute();
					statement.close();
				}
				else
				{
					try
					{
						statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)");
						statement.setInt(1, _clanId);
						statement.setInt(2, newSkill.getId());
						statement.setInt(3, newSkill.getLevel());
						statement.setString(4, newSkill.getName());
						statement.execute();
						statement.close();
					}
					catch (Exception e) // update to avoid miss information
					{
						statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
						statement.setInt(1, newSkill.getLevel());
						statement.setInt(2, newSkill.getId());
						statement.setInt(3, _clanId);
						statement.execute();
						statement.close();
					}
				}
			}
			catch (Exception e2)
			{
				LOGGER.warning("Error could not store char skills: " + e2);
			}
			
			for (ClanMember temp : _members.values())
			{
				try
				{
					if (temp.isOnline() && (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass()))
					{
						temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
						temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
					}
				}
				catch (NullPointerException e)
				{
					LOGGER.warning(e.toString());
				}
			}
		}
		
		return oldSkill;
	}
	
	public void addSkillEffects()
	{
		for (Skill skill : _skills.values())
		{
			for (ClanMember temp : _members.values())
			{
				try
				{
					if (temp.isOnline() && (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass()))
					{
						temp.getPlayerInstance().addSkill(skill, false); // Skill is not saved to player DB
					}
				}
				catch (NullPointerException e)
				{
					LOGGER.warning(e.toString());
				}
			}
		}
	}
	
	public void addSkillEffects(PlayerInstance cm)
	{
		if (cm == null)
		{
			return;
		}
		
		for (Skill skill : _skills.values())
		{
			if (skill.getMinPledgeClass() <= cm.getPledgeClass())
			{
				cm.addSkill(skill, false); // Skill is not saved to player DB
			}
		}
	}
	
	public void broadcastToOnlineAllyMembers(GameServerPacket packet)
	{
		if (_allyId == 0)
		{
			return;
		}
		
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == getAllyId())
			{
				clan.broadcastToOnlineMembers(packet);
			}
		}
	}
	
	public void broadcastToOnlineMembers(GameServerPacket packet)
	{
		for (ClanMember member : _members.values())
		{
			try
			{
				if (member.isOnline())
				{
					member.getPlayerInstance().sendPacket(packet);
				}
			}
			catch (NullPointerException e)
			{
				LOGGER.warning(e.toString());
			}
		}
	}
	
	public void broadcastToOtherOnlineMembers(GameServerPacket packet, PlayerInstance player)
	{
		for (ClanMember member : _members.values())
		{
			try
			{
				if (member.isOnline() && (member.getPlayerInstance() != player))
				{
					member.getPlayerInstance().sendPacket(packet);
				}
			}
			catch (NullPointerException e)
			{
				LOGGER.warning(e.toString());
			}
		}
	}
	
	public boolean hasCrest()
	{
		return _hasCrest;
	}
	
	public boolean hasCrestLarge()
	{
		return _hasCrestLarge;
	}
	
	public void setHasCrest(boolean flag)
	{
		_hasCrest = flag;
	}
	
	public void setHasCrestLarge(boolean flag)
	{
		_hasCrestLarge = flag;
	}
	
	public ItemContainer getWarehouse()
	{
		return _warehouse;
	}
	
	public boolean isAtWarWith(Integer id)
	{
		return _atWarWith.contains(id);
	}
	
	public boolean isAtWarAttacker(Integer id)
	{
		return _atWarAttackers.contains(id);
	}
	
	public void setEnemyClan(Clan clan)
	{
		final Integer id = clan.getClanId();
		_atWarWith.add(id);
	}
	
	public void setEnemyClan(Integer clan)
	{
		_atWarWith.add(clan);
	}
	
	public void setAttackerClan(Clan clan)
	{
		final Integer id = clan.getClanId();
		_atWarAttackers.add(id);
	}
	
	public void setAttackerClan(Integer clan)
	{
		_atWarAttackers.add(clan);
	}
	
	public void deleteEnemyClan(Clan clan)
	{
		final Integer id = clan.getClanId();
		_atWarWith.remove(id);
	}
	
	public void deleteAttackerClan(Clan clan)
	{
		final Integer id = clan.getClanId();
		_atWarAttackers.remove(id);
	}
	
	public int getHiredGuards()
	{
		return _hiredGuards;
	}
	
	public void incrementHiredGuards()
	{
		_hiredGuards++;
	}
	
	public int isAtWar()
	{
		return _atWarWith.isEmpty() ? 0 : 1;
	}
	
	public List<Integer> getWarList()
	{
		return _atWarWith;
	}
	
	public List<Integer> getAttackerList()
	{
		return _atWarAttackers;
	}
	
	public void broadcastClanStatus()
	{
		for (PlayerInstance member : getOnlineMembers())
		{
			member.sendPacket(new PledgeShowMemberListDeleteAll());
			member.sendPacket(new PledgeShowMemberListAll(this, member));
		}
	}
	
	public void removeSkill(int id)
	{
		for (Skill skill : _skillList)
		{
			if (skill.getId() == id)
			{
				_skillList.remove(skill);
				break;
			}
		}
	}
	
	public void removeSkill(Skill deleteSkill)
	{
		_skillList.remove(deleteSkill);
	}
	
	public List<Skill> getSkills()
	{
		return _skillList;
	}
	
	public class SubPledge
	{
		private final int _id;
		private String _subPledgeName;
		private String _leaderName;
		
		public SubPledge(int id, String name, String leaderName)
		{
			_id = id;
			_subPledgeName = name;
			_leaderName = leaderName;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getName()
		{
			return _subPledgeName;
		}
		
		public String getLeaderName()
		{
			return _leaderName;
		}
		
		public void setLeaderName(String leaderName)
		{
			_leaderName = leaderName;
		}
		
		public void setName(String pledgeName)
		{
			_subPledgeName = pledgeName;
		}
	}
	
	public class RankPrivs
	{
		private final int _rankId;
		private final int _party; //
		private int _rankPrivs;
		
		public RankPrivs(int rank, int party, int privs)
		{
			_rankId = rank;
			_party = party;
			_rankPrivs = privs;
		}
		
		public int getRank()
		{
			return _rankId;
		}
		
		public int getParty()
		{
			return _party;
		}
		
		public int getPrivs()
		{
			return _rankPrivs;
		}
		
		public void setPrivs(int privs)
		{
			_rankPrivs = privs;
		}
	}
	
	private void restoreSubPledges()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_name FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, _clanId);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int id = rset.getInt("sub_pledge_id");
				final String name = rset.getString("name");
				final String leaderName = rset.getString("leader_name");
				// Create a SubPledge object for each record
				final SubPledge pledge = new SubPledge(id, name, leaderName);
				_subPledges.put(id, pledge);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore clan sub-units: " + e);
		}
	}
	
	/**
	 * used to retrieve subPledge by type
	 * @param pledgeType
	 * @return
	 */
	public SubPledge getSubPledge(int pledgeType)
	{
		return _subPledges.get(pledgeType);
	}
	
	/**
	 * used to retrieve subPledge by type
	 * @param pledgeName
	 * @return
	 */
	public SubPledge getSubPledge(String pledgeName)
	{
		for (SubPledge sp : _subPledges.values())
		{
			if (sp.getName().equalsIgnoreCase(pledgeName))
			{
				return sp;
			}
		}
		return null;
	}
	
	/**
	 * used to retrieve all subPledges
	 * @return
	 */
	public SubPledge[] getAllSubPledges()
	{
		return _subPledges.values().toArray(new SubPledge[_subPledges.values().size()]);
	}
	
	public SubPledge createSubPledge(PlayerInstance player, int pledgeType, String leaderName, String subPledgeName)
	{
		SubPledge subPledge = null;
		pledgeType = getAvailablePledgeTypes(pledgeType);
		if (pledgeType == 0)
		{
			if (pledgeType == SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendMessage("You can't create any more sub-units of this type");
			}
			return null;
		}
		
		if (_leader.getName().equals(leaderName))
		{
			player.sendMessage("Leader is not correct");
			return null;
		}
		
		// Royal Guard 5000 points per each
		// Order of Knights 10000 points per each
		if ((pledgeType != -1) && (((_reputationScore < 5000) && (pledgeType < SUBUNIT_KNIGHT1)) || ((_reputationScore < 10000) && (pledgeType > SUBUNIT_ROYAL2))))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
			return null;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_name) values (?,?,?,?)");
			statement.setInt(1, _clanId);
			statement.setInt(2, pledgeType);
			statement.setString(3, subPledgeName);
			if (pledgeType != -1)
			{
				statement.setString(4, leaderName);
			}
			else
			{
				statement.setString(4, "");
			}
			
			statement.execute();
			statement.close();
			
			subPledge = new SubPledge(pledgeType, subPledgeName, leaderName);
			_subPledges.put(pledgeType, subPledge);
			
			if (pledgeType != -1)
			{
				setReputationScore(_reputationScore - 2500, true);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("error while saving new sub_clan to db " + e);
		}
		
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(_leader.getClan()));
		broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge));
		return subPledge;
	}
	
	public int getAvailablePledgeTypes(int pledgeType)
	{
		if (_subPledges.get(pledgeType) != null)
		{
			switch (pledgeType)
			{
				case SUBUNIT_ACADEMY:
				{
					return 0;
				}
				case SUBUNIT_ROYAL1:
				{
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				}
				case SUBUNIT_ROYAL2:
				{
					return 0;
				}
				case SUBUNIT_KNIGHT1:
				{
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				}
				case SUBUNIT_KNIGHT2:
				{
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				}
				case SUBUNIT_KNIGHT3:
				{
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				}
				case SUBUNIT_KNIGHT4:
				{
					return 0;
				}
			}
		}
		return pledgeType;
	}
	
	public void updateSubPledgeInDB(int pledgeType)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_subpledges SET leader_name=?, name=? WHERE clan_id=? AND sub_pledge_id=?");
			statement.setString(1, getSubPledge(pledgeType).getLeaderName());
			statement.setString(2, getSubPledge(pledgeType).getName());
			statement.setInt(3, _clanId);
			statement.setInt(4, pledgeType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("error while saving new clan leader to db " + e);
		}
	}
	
	private void restoreRankPrivs()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT privs,`rank`,party FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, _clanId);
			final ResultSet rset = statement.executeQuery();
			
			// Go though the recordset of this SQL query
			while (rset.next())
			{
				final int rank = rset.getInt("rank");
				// int party = rset.getInt("party");
				
				final int privileges = rset.getInt("privs");
				_privs.get(rank).setPrivs(privileges);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore clan privs by rank: " + e);
		}
	}
	
	public void initializePrivs()
	{
		RankPrivs privs;
		for (int i = 1; i < 10; i++)
		{
			privs = new RankPrivs(i, 0, CP_NOTHING);
			_privs.put(i, privs);
		}
	}
	
	public int getRankPrivs(int rank)
	{
		if (_privs.get(rank) != null)
		{
			return _privs.get(rank).getPrivs();
		}
		return CP_NOTHING;
	}
	
	public void setRankPrivs(int rank, int privs)
	{
		if (_privs.get(rank) != null)
		{
			_privs.get(rank).setPrivs(privs);
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("REPLACE INTO clan_privs (clan_id,`rank`,party,privs) VALUES (?,?,?,?)");
				statement.setInt(1, _clanId);
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("Could not store clan privs for rank: " + e);
			}
			
			for (ClanMember cm : getMembers())
			{
				if (cm.isOnline() && (cm.getPowerGrade() == rank) && (cm.getPlayerInstance() != null))
				{
					cm.getPlayerInstance().setClanPrivileges(privs);
					cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
				}
			}
			broadcastClanStatus();
		}
		else
		{
			_privs.put(rank, new RankPrivs(rank, 0, privs));
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("REPLACE INTO clan_privs (clan_id,`rank`,party,privs) VALUES (?,?,?,?)");
				statement.setInt(1, _clanId);
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("Could not create new rank and store clan privs for rank: " + e);
			}
		}
	}
	
	public RankPrivs[] getAllRankPrivs()
	{
		return _privs.values().toArray(new RankPrivs[_privs.values().size()]);
	}
	
	public int getLeaderSubPledge(String name)
	{
		int id = 0;
		for (SubPledge sp : _subPledges.values())
		{
			if (sp.getLeaderName() == null)
			{
				continue;
			}
			
			if (sp.getLeaderName().equals(name))
			{
				id = sp.getId();
			}
		}
		return id;
	}
	
	public void setReputationScore(int value, boolean save)
	{
		if ((_reputationScore >= 0) && (value < 0))
		{
			broadcastToOnlineMembers(new SystemMessage(SystemMessageId.SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILL_S_WILL_BE_DE_ACTIVATED));
			final Skill[] skills = getAllSkills();
			for (ClanMember member : _members.values())
			{
				if (member.isOnline() && (member.getPlayerInstance() != null))
				{
					for (Skill sk : skills)
					{
						member.getPlayerInstance().removeSkill(sk, false);
					}
				}
			}
		}
		else if ((_reputationScore < 0) && (value >= 0))
		{
			broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_SKILLS_WILL_NOW_BE_ACTIVATED_SINCE_THE_CLAN_S_REPUTATION_SCORE_IS_0_OR_HIGHER));
			final Skill[] skills = getAllSkills();
			for (ClanMember member : _members.values())
			{
				if (member.isOnline() && (member.getPlayerInstance() != null))
				{
					for (Skill sk : skills)
					{
						if (sk.getMinPledgeClass() <= member.getPlayerInstance().getPledgeClass())
						{
							member.getPlayerInstance().addSkill(sk, false);
						}
					}
				}
			}
		}
		
		_reputationScore = value;
		if (_reputationScore > 100000000)
		{
			_reputationScore = 100000000;
		}
		if (_reputationScore < -100000000)
		{
			_reputationScore = -100000000;
		}
		
		if (save)
		{
			updateClanInDB();
		}
	}
	
	public int getReputationScore()
	{
		return _reputationScore;
	}
	
	public void setRank(int rank)
	{
		_rank = rank;
	}
	
	public int getRank()
	{
		return _rank;
	}
	
	public int getAuctionBiddedAt()
	{
		return _auctionBiddedAt;
	}
	
	public void setAuctionBiddedAt(int id, boolean storeInDb)
	{
		_auctionBiddedAt = id;
		if (storeInDb)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
				statement.setInt(1, id);
				statement.setInt(2, _clanId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("Could not store auction for clan: " + e);
			}
		}
	}
	
	/**
	 * Checks if player and target meet various conditions to join a clan
	 * @param player
	 * @param target
	 * @param pledgeType
	 * @return
	 */
	public boolean checkClanJoinCondition(PlayerInstance player, PlayerInstance target, int pledgeType)
	{
		if (player == null)
		{
			return false;
		}
		
		if ((player.getClanPrivileges() & CP_CL_JOIN_CLAN) != CP_CL_JOIN_CLAN)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		
		if (target == null)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		
		if (player.getObjectId() == target.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN);
			return false;
		}
		
		if (_charPenaltyExpiryTime > System.currentTimeMillis())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		
		if (target.getClanId() != 0)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_WORKING_WITH_ANOTHER_CLAN);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		
		if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_HE_SHE_LEFT_ANOTHER_CLAN);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		
		if (((target.getLevel() > 40) || (target.getClassId().level() >= 2)) && (pledgeType == -1))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOES_NOT_MEET_THE_REQUIREMENTS_TO_JOIN_A_CLAN_ACADEMY);
			sm.addString(target.getName());
			player.sendPacket(sm);
			player.sendPacket(SystemMessageId.TO_JOIN_A_CLAN_ACADEMY_CHARACTERS_MUST_BE_LEVEL_40_OR_BELOW_NOT_BELONG_ANOTHER_CLAN_AND_NOT_YET_COMPLETED_THEIR_2ND_CLASS_TRANSFER);
			return false;
		}
		
		if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
		{
			if (pledgeType == 0)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME);
				sm.addString(_name);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME);
			}
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if player and target meet various conditions to join a clan
	 * @param player
	 * @param target
	 * @return
	 */
	public boolean checkAllyJoinCondition(PlayerInstance player, PlayerInstance target)
	{
		if (player == null)
		{
			return false;
		}
		
		if ((player.getAllyId() == 0) || !player.isClanLeader() || (player.getClanId() != player.getAllyId()))
		{
			player.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_ALLIANCE_LEADERS);
			return false;
		}
		
		final Clan leaderClan = player.getClan();
		if ((leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis()) && (leaderClan.getAllyPenaltyType() == PENALTY_TYPE_DISMISS_CLAN))
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_ACCEPT_ANY_CLAN_WITHIN_A_DAY_AFTER_EXPELLING_ANOTHER_CLAN);
			return false;
		}
		
		if (target == null)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		
		if (player.getObjectId() == target.getObjectId())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN);
			return false;
		}
		
		if (target.getClan() == null)
		{
			player.sendPacket(SystemMessageId.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
			return false;
		}
		
		if (!target.isClanLeader())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return false;
		}
		
		final Clan targetClan = target.getClan();
		if (target.getAllyId() != 0)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE);
			sm.addString(targetClan.getName());
			sm.addString(targetClan.getAllyName());
			player.sendPacket(sm);
			return false;
		}
		
		if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_LEAVED)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_IT_LEFT_ANOTHER_ALLIANCE);
				sm.addString(target.getClan().getName());
				sm.addString(target.getClan().getAllyName());
				player.sendPacket(sm);
				return false;
			}
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_DISMISSED)
			{
				player.sendPacket(SystemMessageId.A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION);
				return false;
			}
		}
		
		if (player.isInsideZone(ZoneId.SIEGE) && target.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.THE_OPPOSING_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE);
			return false;
		}
		
		if (leaderClan.isAtWarWith(targetClan.getClanId()))
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_CURRENTLY_AT_WAR_WITH_THAT_WOULD_BE_DIABOLICAL_AND_TREACHEROUS);
			return false;
		}
		
		int numOfClansInAlly = 0;
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == player.getAllyId())
			{
				++numOfClansInAlly;
			}
		}
		
		if (numOfClansInAlly >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
			return false;
		}
		
		return true;
	}
	
	public long getAllyPenaltyExpiryTime()
	{
		return _allyPenaltyExpiryTime;
	}
	
	public int getAllyPenaltyType()
	{
		return _allyPenaltyType;
	}
	
	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
	{
		_allyPenaltyExpiryTime = expiryTime;
		_allyPenaltyType = penaltyType;
	}
	
	public long getCharPenaltyExpiryTime()
	{
		return _charPenaltyExpiryTime;
	}
	
	public void setCharPenaltyExpiryTime(long time)
	{
		_charPenaltyExpiryTime = time;
	}
	
	public long getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}
	
	public void setDissolvingExpiryTime(long time)
	{
		_dissolvingExpiryTime = time;
	}
	
	public void createAlly(PlayerInstance player, String allyName)
	{
		if (null == player)
		{
			return;
		}
		
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
			return;
		}
		
		if (_allyId != 0)
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
			return;
		}
		
		if (_level < 5)
		{
			player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return;
		}
		
		if ((_allyPenaltyExpiryTime > System.currentTimeMillis()) && (_allyPenaltyType == PENALTY_TYPE_DISSOLVE_ALLY))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_10_DAYS_AFTER_DISSOLUTION);
			return;
		}
		
		if (_dissolvingExpiryTime > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_AN_ALLIANCE_DURING_THE_TERM_OF_DISSOLUTION_POSTPONEMENT);
			return;
		}
		
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.ALLY_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			LOGGER.info("ERROR: Ally name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher match = pattern.matcher(allyName);
		if (!match.matches())
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_PLEASE_TRY_AGAIN);
			return;
		}
		
		if ((allyName.length() > 16) || (allyName.length() < 2))
		{
			player.sendPacket(SystemMessageId.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
			return;
		}
		
		if (ClanTable.getInstance().isAllyExists(allyName))
		{
			player.sendPacket(SystemMessageId.THIS_ALLIANCE_NAME_ALREADY_EXISTS);
			return;
		}
		
		setAllyId(_clanId);
		setAllyName(allyName.trim());
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();
		
		player.sendPacket(new UserInfo(player));
		
		//
		player.sendMessage("Alliance " + allyName + " has been created.");
	}
	
	public void dissolveAlly(PlayerInstance player)
	{
		if (_allyId == 0)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
			return;
		}
		
		if (!player.isClanLeader() || (_clanId != _allyId))
		{
			player.sendPacket(SystemMessageId.THIS_FEATURE_IS_ONLY_AVAILABLE_ALLIANCE_LEADERS);
			return;
		}
		
		if (player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_AN_ALLIANCE_WHILE_AN_AFFILIATED_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE);
			return;
		}
		
		broadcastToOnlineAllyMembers(new SystemMessage(SystemMessageId.THE_ALLIANCE_HAS_BEEN_DISSOLVED));
		
		final long currentTime = System.currentTimeMillis();
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if ((clan.getAllyId() == getAllyId()) && (clan.getClanId() != getClanId()))
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.setAllyPenaltyExpiryTime(0, 0);
				clan.updateClanInDB();
			}
		}
		
		setAllyId(0);
		setAllyName(null);
		setAllyPenaltyExpiryTime(currentTime + (Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000), PENALTY_TYPE_DISSOLVE_ALLY);
		updateClanInDB();
		
		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false);
	}
	
	public void levelUpClan(PlayerInstance player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (System.currentTimeMillis() < _dissolvingExpiryTime)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_RAISE_YOUR_CLAN_LEVEL_DURING_THE_TERM_OF_DISPERSION_POSTPONEMENT);
			return;
		}
		
		boolean increaseClanLevel = false;
		
		switch (_level)
		{
			case 0:
			{
				// upgrade to 1
				if ((player.getSp() >= 30000) && (player.getAdena() >= 650000) && player.reduceAdena("ClanLvl", 650000, player.getTarget(), true))
				{
					player.setSp(player.getSp() - 30000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
					sp.addNumber(30000);
					player.sendPacket(sp);
					increaseClanLevel = true;
				}
				break;
			}
			case 1:
			{
				// upgrade to 2
				if ((player.getSp() >= 150000) && (player.getAdena() >= 2500000) && player.reduceAdena("ClanLvl", 2500000, player.getTarget(), true))
				{
					player.setSp(player.getSp() - 150000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
					sp.addNumber(150000);
					player.sendPacket(sp);
					increaseClanLevel = true;
				}
				break;
			}
			case 2:
			{
				// upgrade to 3 (itemid 1419 == proof of blood)
				if ((player.getSp() >= 500000) && (player.getInventory().getItemByItemId(1419) != null) && player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false))
				{
					player.setSp(player.getSp() - 500000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
					sp.addNumber(500000);
					player.sendPacket(sp);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
					sm.addItemName(1419);
					sm.addNumber(1);
					player.sendPacket(sm);
					increaseClanLevel = true;
				}
				break;
			}
			case 3:
			{
				// upgrade to 4 (itemid 3874 == proof of alliance)
				if ((player.getSp() >= 1400000) && (player.getInventory().getItemByItemId(3874) != null) && player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false))
				{
					player.setSp(player.getSp() - 1400000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
					sp.addNumber(1400000);
					player.sendPacket(sp);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
					sm.addItemName(3874);
					sm.addNumber(1);
					player.sendPacket(sm);
					increaseClanLevel = true;
				}
				break;
			}
			case 4:
			{
				// upgrade to 5 (itemid 3870 == proof of aspiration)
				if ((player.getSp() >= 3500000) && (player.getInventory().getItemByItemId(3870) != null) && player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false))
				{
					player.setSp(player.getSp() - 3500000);
					final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
					sp.addNumber(3500000);
					player.sendPacket(sp);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
					sm.addItemName(3870);
					sm.addNumber(1);
					player.sendPacket(sm);
					increaseClanLevel = true;
				}
				break;
			}
			case 5:
			{
				if ((_reputationScore >= 10000) && (_members.size() >= 30))
				{
					setReputationScore(_reputationScore - 10000, true);
					final SystemMessage cr = new SystemMessage(SystemMessageId.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION_SCORE);
					cr.addNumber(10000);
					player.sendPacket(cr);
					increaseClanLevel = true;
				}
				break;
			}
			case 6:
			{
				if ((_reputationScore >= 20000) && (_members.size() >= 80))
				{
					setReputationScore(_reputationScore - 20000, true);
					final SystemMessage cr = new SystemMessage(SystemMessageId.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION_SCORE);
					cr.addNumber(20000);
					player.sendPacket(cr);
					increaseClanLevel = true;
				}
				break;
			}
			case 7:
			{
				if ((_reputationScore >= 40000) && (_members.size() >= 120))
				{
					setReputationScore(_reputationScore - 40000, true);
					final SystemMessage cr = new SystemMessage(SystemMessageId.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION_SCORE);
					cr.addNumber(40000);
					player.sendPacket(cr);
					increaseClanLevel = true;
				}
				break;
			}
			default:
			{
				return;
			}
		}
		
		if (!increaseClanLevel)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.THE_CONDITIONS_NECESSARY_TO_INCREASE_THE_CLAN_S_LEVEL_HAVE_NOT_BEEN_MET));
			return;
		}
		
		// the player should know that he has less sp now :p
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		player.sendPacket(new ItemList(player, false));
		changeLevel(_level + 1);
	}
	
	public void changeLevel(int level)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
			statement.setInt(1, level);
			statement.setInt(2, _clanId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not increase clan level:" + e);
		}
		
		setLevel(level);
		
		if (_leader.isOnline())
		{
			final PlayerInstance leader = _leader.getPlayerInstance();
			if (3 < level)
			{
				SiegeManager.getInstance().addSiegeSkills(leader);
			}
			else if (4 > level)
			{
				SiegeManager.getInstance().removeSiegeSkills(leader);
			}
			
			if (4 < level)
			{
				leader.sendPacket(SystemMessageId.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
			}
		}
		
		// notify all the members about it
		broadcastToOnlineMembers(new SystemMessage(SystemMessageId.YOUR_CLAN_S_SKILL_LEVEL_HAS_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}
	
	public void setAllyCrest(int crestId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			setAllyCrestId(crestId);
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?");
			statement.setInt(1, crestId);
			statement.setInt(2, _clanId);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("could not update the ally crest id:" + e.getMessage());
		}
	}
	
	@Override
	public String toString()
	{
		return "Clan [_name=" + _name + ", _clanId=" + _clanId + ", _leader=" + _leader + ", _members=" + _members + ", _allyName=" + _allyName + ", _allyId=" + _allyId + ", _level=" + _level + ", _hasCastle=" + _hasCastle + ", _hasFort=" + _hasFort + ", _hasHideout=" + _hasHideout + ", _hasCrest=" + _hasCrest + ", _hiredGuards=" + _hiredGuards + ", _crestId=" + _crestId + ", _crestLargeId=" + _crestLargeId + ", _allyCrestId=" + _allyCrestId + ", _auctionBiddedAt=" + _auctionBiddedAt + ", _allyPenaltyExpiryTime=" + _allyPenaltyExpiryTime + ", _allyPenaltyType=" + _allyPenaltyType + ", _charPenaltyExpiryTime=" + _charPenaltyExpiryTime + ", _dissolvingExpiryTime=" + _dissolvingExpiryTime + ", _warehouse=" + _warehouse + ", _atWarWith=" + _atWarWith + ", _atWarAttackers=" + _atWarAttackers + ", _hasCrestLarge=" + _hasCrestLarge + ", _forum=" + _forum + ", _skillList=" + _skillList + ", _notice=" + _notice + ", _noticeEnabled=" + _noticeEnabled + ", _skills=" + _skills + ", _privs=" + _privs + ", _subPledges=" + _subPledges + ", _reputationScore=" + _reputationScore + ", _rank=" + _rank + "]";
	}
}