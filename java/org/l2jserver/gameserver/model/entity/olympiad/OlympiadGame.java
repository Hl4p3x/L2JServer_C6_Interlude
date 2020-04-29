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
package org.l2jserver.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.HeroSkillTable;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.CubicInstance;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.SummonInstance;
import org.l2jserver.gameserver.model.actor.instance.TamedBeastInstance;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad.COMP_TYPE;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jserver.gameserver.network.serverpackets.ExOlympiadMode;
import org.l2jserver.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import org.l2jserver.gameserver.network.serverpackets.ExOlympiadUserInfo;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author GodKratos
 */
class OlympiadGame
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadGame.class.getName());
	protected COMP_TYPE _type;
	protected boolean _aborted;
	protected boolean _gamestarted;
	protected boolean _playerOneDisconnected;
	protected boolean _playerTwoDisconnected;
	protected boolean _playerOneDefaulted;
	protected boolean _playerTwoDefaulted;
	protected String _playerOneName;
	protected String _playerTwoName;
	protected List<Skill> _playerOneSkills = new ArrayList<>();
	protected List<Skill> _playerTwoSkills = new ArrayList<>();
	
	private static final String POINTS = "olympiad_points";
	private static final String COMP_DONE = "competitions_done";
	private static final String COMP_WON = "competitions_won";
	private static final String COMP_LOST = "competitions_lost";
	private static final String COMP_DRAWN = "competitions_drawn";
	protected static boolean _battleStarted;
	
	public int _damageP1 = 0;
	public int _damageP2 = 0;
	
	public PlayerInstance _playerOne;
	public PlayerInstance _playerTwo;
	protected List<PlayerInstance> _players;
	private final int[] _stadiumPort;
	private int x1;
	private int y1;
	private int z1;
	private int x2;
	private int y2;
	private int z2;
	public int _stadiumID;
	
	protected OlympiadGame(int id, COMP_TYPE type, List<PlayerInstance> list)
	{
		_aborted = false;
		_gamestarted = false;
		_stadiumID = id;
		_playerOneDisconnected = false;
		_playerTwoDisconnected = false;
		_type = type;
		_stadiumPort = OlympiadManager.STADIUMS[id].getCoordinates();
		if (list != null)
		{
			_players = list;
			_playerOne = list.get(0);
			_playerTwo = list.get(1);
			
			try
			{
				_playerOneName = _playerOne.getName();
				_playerTwoName = _playerTwo.getName();
				_playerOne.setOlympiadGameId(id);
				_playerTwo.setOlympiadGameId(id);
				_playerOneSkills = new ArrayList<>();
				_playerTwoSkills = new ArrayList<>();
			}
			catch (Exception e)
			{
				_aborted = true;
				clearPlayers();
			}
		}
		else
		{
			_aborted = true;
			clearPlayers();
		}
	}
	
	public boolean isAborted()
	{
		return _aborted;
	}
	
	protected final void clearPlayers()
	{
		_playerOne = null;
		_playerTwo = null;
		_players = null;
		_playerOneName = "";
		_playerTwoName = "";
		_playerOneSkills.clear();
		_playerTwoSkills.clear();
	}
	
	protected void handleDisconnect(PlayerInstance player)
	{
		if (_gamestarted)
		{
			if (player == _playerOne)
			{
				_playerOneDisconnected = true;
			}
			else if (player == _playerTwo)
			{
				_playerTwoDisconnected = true;
			}
		}
	}
	
	protected void removals()
	{
		if (_aborted)
		{
			return;
		}
		
		if ((_playerOne == null) || (_playerTwo == null))
		{
			return;
		}
		if (_playerOneDisconnected || _playerTwoDisconnected)
		{
			return;
		}
		
		for (PlayerInstance player : _players)
		{
			try
			{
				// Remove Clan Skills
				if (player.getClan() != null)
				{
					for (Skill skill : player.getClan().getAllSkills())
					{
						player.removeSkill(skill, false);
					}
				}
				// Abort casting if player casting
				if (player.isCastingNow())
				{
					player.abortCast();
				}
				
				// Force the character to be visible
				player.getAppearance().setVisible();
				
				// Remove Hero Skills
				if (player.isHero())
				{
					for (Skill skill : HeroSkillTable.getHeroSkills())
					{
						player.removeSkill(skill, false);
					}
				}
				
				// Remove Restricted skills
				for (Skill skill : player.getAllSkills())
				{
					if (Config.LIST_OLY_RESTRICTED_SKILLS.contains(skill.getId()))
					{
						if (player.getObjectId() == _playerOne.getObjectId())
						{
							_playerOneSkills.add(skill);
						}
						else
						{
							_playerTwoSkills.add(skill);
						}
						player.removeSkill(skill, false);
					}
				}
				
				// Heal Player fully
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				
				// Remove Buffs
				player.stopAllEffects();
				
				// Remove Summon's Buffs
				if (player.getPet() != null)
				{
					final Summon summon = player.getPet();
					summon.stopAllEffects();
					
					if (summon instanceof PetInstance)
					{
						summon.unSummon(player);
					}
				}
				
				// Remove Tamed Beast
				if (player.getTrainedBeast() != null)
				{
					final TamedBeastInstance traindebeast = player.getTrainedBeast();
					traindebeast.stopAllEffects();
					
					traindebeast.doDespawn();
				}
				
				if (Config.REMOVE_CUBIC_OLYMPIAD)
				{
					if (player.getCubics() != null)
					{
						for (CubicInstance cubic : player.getCubics().values())
						{
							cubic.stopAction();
							player.delCubic(cubic.getId());
						}
						player.getCubics().clear();
					}
				}
				else if (player.getCubics() != null)
				{
					boolean removed = false;
					for (CubicInstance cubic : player.getCubics().values())
					{
						if (cubic.givenByOther())
						{
							cubic.stopAction();
							player.delCubic(cubic.getId());
							removed = true;
						}
					}
					if (removed)
					{
						player.broadcastUserInfo();
					}
				}
				
				// Remove player from his party
				if (player.getParty() != null)
				{
					final Party party = player.getParty();
					party.removePartyMember(player);
				}
				
				player.checkItemRestriction();
				
				// Remove shot automation
				final Map<Integer, Integer> activeSoulShots = player.getAutoSoulShot();
				for (int itemId : activeSoulShots.values())
				{
					player.removeAutoSoulShot(itemId);
					player.sendPacket(new ExAutoSoulShot(itemId, 0));
				}
				
				// Discharge any active shots
				if (player.getActiveWeaponInstance() != null)
				{
					player.getActiveWeaponInstance().setChargedSoulshot(ItemInstance.CHARGED_NONE);
					player.getActiveWeaponInstance().setChargedSpiritshot(ItemInstance.CHARGED_NONE);
				}
				
				// Skill recharge is a Gracia Final feature, but we have it configurable ;)
				if (Config.ALT_OLY_RECHARGE_SKILLS)
				{
					for (Skill skill : player.getAllSkills())
					{
						if (skill.getId() != 1324)
						{
							player.enableSkill(skill);
						}
					}
					
					player.updateEffectIcons();
				}
				
				player.sendSkillList();
			}
			catch (Exception e)
			{
				LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + player.getName() + " removals, an error has been occurred: " + e);
			}
		}
	}
	
	protected boolean portPlayersToArena()
	{
		final boolean _playerOneCrash = ((_playerOne == null) || _playerOneDisconnected);
		final boolean _playerTwoCrash = ((_playerTwo == null) || _playerTwoDisconnected);
		if (_playerOneCrash || _playerTwoCrash || _aborted)
		{
			_playerOne = null;
			_playerTwo = null;
			_aborted = true;
			return false;
		}
		
		if (_playerOne.inObserverMode() || _playerTwo.inObserverMode())
		{
			if (_playerOne.inObserverMode())
			{
				LOGGER.warning("[OLYMPIAD DEBUG] Player one " + _playerOne.getName() + " was on observer mode! Match aborted!");
			}
			
			if (_playerTwo.inObserverMode())
			{
				LOGGER.warning("[OLYMPIAD DEBUG] Player two " + _playerTwo.getName() + " was on observer mode! Match aborted!");
			}
			
			_playerOne.sendMessage("One player on this match is on Observer mode! Match aborted!");
			_playerTwo.sendMessage("One player on this match is on Observer mode! Match aborted!");
			_aborted = true;
			return false;
		}
		
		try
		{
			x1 = _playerOne.getX();
			y1 = _playerOne.getY();
			z1 = _playerOne.getZ();
			x2 = _playerTwo.getX();
			y2 = _playerTwo.getY();
			z2 = _playerTwo.getZ();
			if (_playerOne.isSitting())
			{
				_playerOne.standUp();
			}
			
			if (_playerTwo.isSitting())
			{
				_playerTwo.standUp();
			}
			
			_playerOne.setTarget(null);
			_playerTwo.setTarget(null);
			
			_playerOne.teleToLocation(_stadiumPort[0] + 900, _stadiumPort[1], _stadiumPort[2], false);
			// teleport summon to
			if (_playerOne.getPet() != null)
			{
				final Summon summon = _playerOne.getPet();
				if (summon instanceof SummonInstance)
				{
					summon.teleToLocation(_stadiumPort[0] + 900, _stadiumPort[1], _stadiumPort[2], false);
				}
			}
			_playerTwo.teleToLocation(_stadiumPort[0] - 900, _stadiumPort[1], _stadiumPort[2], false);
			// teleport summon to
			if (_playerTwo.getPet() != null)
			{
				final Summon summon = _playerTwo.getPet();
				if (summon instanceof SummonInstance)
				{
					summon.teleToLocation(_stadiumPort[0] - 900, _stadiumPort[1], _stadiumPort[2], false);
				}
			}
			
			_playerOne.sendPacket(new ExOlympiadMode(2, _playerOne));
			_playerTwo.sendPacket(new ExOlympiadMode(2, _playerTwo));
			_playerOne.setInOlympiadMode(true);
			_playerOne.setOlympiadStart(false);
			_playerOne.setOlympiadSide(1);
			
			_playerTwo.setInOlympiadMode(true);
			_playerTwo.setOlympiadStart(false);
			_playerTwo.setOlympiadSide(2);
		}
		catch (NullPointerException e)
		{
			LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on players portPlayersToArena, an error has been occurred: " + e);
			return false;
		}
		return true;
	}
	
	protected void additions()
	{
		for (PlayerInstance player : _players)
		{
			try
			{
				// Set HP/CP/MP to Max
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				// Wind Walk Buff for Both
				Skill skill;
				SystemMessage sm;
				skill = SkillTable.getInstance().getInfo(1204, 2);
				skill.getEffects(player, player);
				player.broadcastPacket(new MagicSkillUse(player, player, skill.getId(), 2, skill.getHitTime(), 0));
				sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
				sm.addSkillName(1204);
				player.sendPacket(sm);
				if (!player.isMageClass())
				{
					// Haste Buff to Fighters
					skill = SkillTable.getInstance().getInfo(1086, 1);
					skill.getEffects(player, player);
					player.broadcastPacket(new MagicSkillUse(player, player, skill.getId(), 1, skill.getHitTime(), 0));
					sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
					sm.addSkillName(1086);
					player.sendPacket(sm);
				}
				else
				{
					// Acumen Buff to Mages
					skill = SkillTable.getInstance().getInfo(1085, 1);
					skill.getEffects(player, player);
					player.broadcastPacket(new MagicSkillUse(player, player, skill.getId(), 1, skill.getHitTime(), 0));
					sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
					sm.addSkillName(1085);
					player.sendPacket(sm);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + player.getName() + " additions, an error has been occurred: " + e);
			}
		}
	}
	
	protected void sendMessageToPlayers(boolean toBattleBegin, int nsecond)
	{
		SystemMessage sm;
		if (!toBattleBegin)
		{
			sm = new SystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.THE_GRAND_OLYMPIAD_MATCH_WILL_START_IN_S1_SECOND_S);
		}
		
		sm.addNumber(nsecond);
		
		for (PlayerInstance player : _players)
		{
			try
			{
				player.sendPacket(sm);
			}
			catch (Exception e)
			{
				LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + player.getName() + " sendMessage, an error has been occurred: " + e);
			}
		}
	}
	
	protected void portPlayersBack()
	{
		if ((_playerOne != null) && (x1 != 0))
		{
			_playerOne.teleToLocation(x1, y1, z1, true);
		}
		
		if ((_playerTwo != null) && (x2 != 0))
		{
			_playerTwo.teleToLocation(x2, y2, z2, true);
		}
	}
	
	protected void PlayersStatusBack()
	{
		for (PlayerInstance player : _players)
		{
			try
			{
				player.getStatus().startHpMpRegeneration();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				player.setInOlympiadMode(false);
				player.setOlympiadStart(false);
				player.setOlympiadSide(-1);
				player.setOlympiadGameId(-1);
				player.sendPacket(new ExOlympiadMode(0, player));
				
				// Add Clan Skills
				if (player.getClan() != null)
				{
					for (Skill skill : player.getClan().getAllSkills())
					{
						if (skill.getMinPledgeClass() <= player.getPledgeClass())
						{
							player.addSkill(skill, false);
						}
					}
				}
				
				// Add Hero Skills
				if (player.isHero())
				{
					for (Skill skill : HeroSkillTable.getHeroSkills())
					{
						player.addSkill(skill, false);
					}
				}
				
				// Return Restricted Skills
				List<Skill> rskills;
				if (player.getObjectId() == _playerOne.getObjectId())
				{
					rskills = _playerOneSkills;
				}
				else
				{
					rskills = _playerTwoSkills;
				}
				for (Skill skill : rskills)
				{
					player.addSkill(skill, false);
				}
				rskills.clear();
				
				player.sendSkillList();
			}
			catch (Exception e)
			{
				LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + player.getName() + " PlayersStatusBack, an error has been occurred: " + e);
			}
		}
	}
	
	protected boolean haveWinner()
	{
		if (_aborted || (_playerOne == null) || (_playerTwo == null) || _playerOneDisconnected || _playerTwoDisconnected)
		{
			return true;
		}
		
		double playerOneHp = 0;
		
		try
		{
			if (_playerOne.getOlympiadGameId() != -1)
			{
				playerOneHp = _playerOne.getCurrentHp();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + _playerOne.getName() + " haveWinner, an error has been occurred: " + e);
			playerOneHp = 0;
		}
		
		double playerTwoHp = 0;
		try
		{
			if (_playerTwo.getOlympiadGameId() != -1)
			{
				playerTwoHp = _playerTwo.getCurrentHp();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + _playerTwo.getName() + " haveWinner, an error has been occurred: " + e);
			playerTwoHp = 0;
		}
		
		return (playerTwoHp <= 0) || (playerOneHp <= 0);
	}
	
	protected void validateWinner()
	{
		if (_aborted)
		{
			return;
		}
		
		final boolean _pOneCrash = ((_playerOne == null) || _playerOneDisconnected);
		final boolean _pTwoCrash = ((_playerTwo == null) || _playerTwoDisconnected);
		int div;
		int gpReward;
		String classed;
		switch (_type)
		{
			case NON_CLASSED:
			{
				div = 5;
				gpReward = Config.ALT_OLY_NONCLASSED_RITEM_C;
				classed = "no";
				break;
			}
			default:
			{
				div = 3;
				gpReward = Config.ALT_OLY_CLASSED_RITEM_C;
				classed = "yes";
				break;
			}
		}
		
		final StatSet playerOneStat = Olympiad.getNobleStats(_playerOne.getObjectId());
		final StatSet playerTwoStat = Olympiad.getNobleStats(_playerTwo.getObjectId());
		final int playerOnePlayed = playerOneStat.getInt(COMP_DONE);
		final int playerTwoPlayed = playerTwoStat.getInt(COMP_DONE);
		final int playerOneWon = playerOneStat.getInt(COMP_WON);
		final int playerTwoWon = playerTwoStat.getInt(COMP_WON);
		final int playerOneLost = playerOneStat.getInt(COMP_LOST);
		final int playerTwoLost = playerTwoStat.getInt(COMP_LOST);
		final int playerOneDrawn = playerOneStat.getInt(COMP_DRAWN);
		final int playerTwoDrawn = playerTwoStat.getInt(COMP_DRAWN);
		final int playerOnePoints = playerOneStat.getInt(POINTS);
		final int playerTwoPoints = playerTwoStat.getInt(POINTS);
		final int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / div;
		
		// Check for if a player defaulted before battle started
		if (_playerOneDefaulted || _playerTwoDefaulted)
		{
			if (_playerOneDefaulted)
			{
				final int lostPoints = playerOnePoints / 3;
				playerOneStat.set(POINTS, playerOnePoints - lostPoints);
				Olympiad.updateNobleStats(_playerOne.getObjectId(), playerOneStat);
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
				sm.addString(_playerOneName);
				sm.addNumber(lostPoints);
				broadcastMessage(sm, false);
				Olympiad.logResult(_playerOneName, _playerTwoName, 0D, 0D, 0, 0, _playerOneName + " default", lostPoints, classed);
			}
			if (_playerTwoDefaulted)
			{
				final int lostPoints = playerTwoPoints / 3;
				playerTwoStat.set(POINTS, playerTwoPoints - lostPoints);
				Olympiad.updateNobleStats(_playerTwo.getObjectId(), playerTwoStat);
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
				sm.addString(_playerTwoName);
				sm.addNumber(lostPoints);
				broadcastMessage(sm, false);
				Olympiad.logResult(_playerOneName, _playerTwoName, 0D, 0D, 0, 0, _playerTwoName + " default", lostPoints, classed);
			}
			return;
		}
		
		// Create results for players if a player crashed
		if (_pOneCrash || _pTwoCrash)
		{
			if (_pOneCrash && !_pTwoCrash)
			{
				try
				{
					playerOneStat.set(POINTS, playerOnePoints - pointDiff);
					playerOneStat.set(COMP_LOST, playerOneLost + 1);
					Olympiad.logResult(_playerOneName, _playerTwoName, 0D, 0D, 0, 0, _playerOneName + " crash", pointDiff, classed);
					playerTwoStat.set(POINTS, playerTwoPoints + pointDiff);
					playerTwoStat.set(COMP_WON, playerTwoWon + 1);
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.CONGRATULATIONS_S1_YOU_WIN_THE_MATCH);
					final SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
					sm.addString(_playerTwoName);
					broadcastMessage(sm, true);
					sm2.addString(_playerTwoName);
					sm2.addNumber(pointDiff);
					broadcastMessage(sm2, false);
				}
				catch (Exception e)
				{
					LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player crashed evaluation, an error has been occurred: " + e);
				}
			}
			else if (_pTwoCrash && !_pOneCrash)
			{
				try
				{
					playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
					playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
					Olympiad.logResult(_playerOneName, _playerTwoName, 0D, 0D, 0, 0, _playerTwoName + " crash", pointDiff, classed);
					playerOneStat.set(POINTS, playerOnePoints + pointDiff);
					playerOneStat.set(COMP_WON, playerOneWon + 1);
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.CONGRATULATIONS_S1_YOU_WIN_THE_MATCH);
					final SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
					sm.addString(_playerOneName);
					broadcastMessage(sm, true);
					sm2.addString(_playerOneName);
					sm2.addNumber(pointDiff);
					broadcastMessage(sm2, false);
				}
				catch (Exception e)
				{
					LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player crashed evaluation, an error has been occurred: " + e);
				}
			}
			else if (_pOneCrash && _pTwoCrash)
			{
				try
				{
					playerOneStat.set(POINTS, playerOnePoints - pointDiff);
					playerOneStat.set(COMP_LOST, playerOneLost + 1);
					playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
					playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
					Olympiad.logResult(_playerOneName, _playerTwoName, 0D, 0D, 0, 0, "both crash", pointDiff, classed);
				}
				catch (Exception e)
				{
					LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player crashed evaluation, an error has been occurred: " + e);
				}
			}
			playerOneStat.set(COMP_DONE, playerOnePlayed + 1);
			playerTwoStat.set(COMP_DONE, playerTwoPlayed + 1);
			Olympiad.updateNobleStats(_playerOne.getObjectId(), playerOneStat);
			Olympiad.updateNobleStats(_playerTwo.getObjectId(), playerTwoStat);
			return;
		}
		
		double playerOneHp = 0;
		if (!_playerOne.isDead())
		{
			playerOneHp = _playerOne.getCurrentHp() + _playerOne.getCurrentCp();
		}
		
		double playerTwoHp = 0;
		if (!_playerTwo.isDead())
		{
			playerTwoHp = _playerTwo.getCurrentHp() + _playerTwo.getCurrentCp();
		}
		
		SystemMessage sm1 = new SystemMessage(SystemMessageId.CONGRATULATIONS_S1_YOU_WIN_THE_MATCH);
		SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
		SystemMessage sm3 = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
		String winner = "draw";
		if ((_playerOne == null) && (_playerTwo == null))
		{
			playerOneStat.set(COMP_DRAWN, playerOneDrawn + 1);
			playerTwoStat.set(COMP_DRAWN, playerTwoDrawn + 1);
			sm1 = new SystemMessage(SystemMessageId.THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE);
			broadcastMessage(sm1, true);
		}
		else if ((_playerTwo == null) || !_playerTwo.isOnline() || ((playerTwoHp == 0) && (playerOneHp != 0)) || ((_damageP1 > _damageP2) && (playerTwoHp != 0) && (playerOneHp != 0)))
		{
			playerOneStat.set(POINTS, playerOnePoints + pointDiff);
			playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
			playerOneStat.set(COMP_WON, playerOneWon + 1);
			playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
			sm1.addString(_playerOneName);
			broadcastMessage(sm1, true);
			sm2.addString(_playerOneName);
			sm2.addNumber(pointDiff);
			broadcastMessage(sm2, false);
			sm3.addString(_playerTwoName);
			sm3.addNumber(pointDiff);
			broadcastMessage(sm3, false);
			winner = _playerOneName + " won";
			
			try
			{
				final ItemInstance item = _playerOne.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, gpReward, _playerOne, null);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);
				_playerOne.sendPacket(iu);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				sm.addItemName(item.getItemId());
				sm.addNumber(gpReward);
				_playerOne.sendPacket(sm);
			}
			catch (Exception e)
			{
				LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player validateWinner, an error has been occurred: " + e);
			}
		}
		else if ((_playerOne == null) || !_playerOne.isOnline() || ((playerOneHp == 0) && (playerTwoHp != 0)) || ((_damageP2 > _damageP1) && (playerOneHp != 0) && (playerTwoHp != 0)))
		{
			playerTwoStat.set(POINTS, playerTwoPoints + pointDiff);
			playerOneStat.set(POINTS, playerOnePoints - pointDiff);
			playerTwoStat.set(COMP_WON, playerTwoWon + 1);
			playerOneStat.set(COMP_LOST, playerOneLost + 1);
			sm1.addString(_playerTwoName);
			broadcastMessage(sm1, true);
			sm2.addString(_playerTwoName);
			sm2.addNumber(pointDiff);
			broadcastMessage(sm2, false);
			sm3.addString(_playerOneName);
			sm3.addNumber(pointDiff);
			broadcastMessage(sm3, false);
			winner = _playerTwoName + " won";
			
			try
			{
				final ItemInstance item = _playerTwo.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, gpReward, _playerTwo, null);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);
				_playerTwo.sendPacket(iu);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
				sm.addItemName(item.getItemId());
				sm.addNumber(gpReward);
				_playerTwo.sendPacket(sm);
			}
			catch (Exception e)
			{
				LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player validateWinner, an error has been occurred: " + e);
			}
		}
		else
		{
			sm1 = new SystemMessage(SystemMessageId.THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE);
			broadcastMessage(sm1, true);
			final int pointOneDiff = playerOnePoints / 5;
			final int pointTwoDiff = playerTwoPoints / 5;
			playerOneStat.set(POINTS, playerOnePoints - pointOneDiff);
			playerTwoStat.set(POINTS, playerTwoPoints - pointTwoDiff);
			playerOneStat.set(COMP_DRAWN, playerOneDrawn + 1);
			playerTwoStat.set(COMP_DRAWN, playerTwoDrawn + 1);
			sm2 = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
			sm2.addString(_playerOneName);
			sm2.addNumber(pointOneDiff);
			broadcastMessage(sm2, false);
			sm3 = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES);
			sm3.addString(_playerTwoName);
			sm3.addNumber(pointTwoDiff);
			broadcastMessage(sm3, false);
		}
		
		playerOneStat.set(COMP_DONE, playerOnePlayed + 1);
		playerTwoStat.set(COMP_DONE, playerTwoPlayed + 1);
		Olympiad.updateNobleStats(_playerOne.getObjectId(), playerOneStat);
		Olympiad.updateNobleStats(_playerTwo.getObjectId(), playerTwoStat);
		Olympiad.logResult(_playerOneName, _playerTwoName, playerOneHp, playerTwoHp, _damageP1, _damageP2, winner, pointDiff, classed);
		for (int i = 15; i > 5; i -= 5)
		{
			sm1 = new SystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECOND_S);
			sm1.addNumber(i);
			broadcastMessage(sm1, false);
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
			}
		}
		for (int i = 5; i > 0; i--)
		{
			sm1 = new SystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECOND_S);
			sm1.addNumber(i);
			broadcastMessage(sm1, false);
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	protected boolean makeCompetitionStart()
	{
		if (_aborted)
		{
			return false;
		}
		
		broadcastMessage(new SystemMessage(SystemMessageId.THE_MATCH_HAS_STARTED_FIGHT), true);
		for (PlayerInstance player : _players)
		{
			try
			{
				player.setOlympiadStart(true);
			}
			catch (Exception e)
			{
				LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + player.getName() + " makeCompetitionStart, an error has been occurred: " + e);
				_aborted = true;
			}
		}
		
		return !_aborted;
	}
	
	protected void addDamage(PlayerInstance player, int damage)
	{
		if ((_playerOne == null) || (_playerTwo == null))
		{
			return;
		}
		if (player == _playerOne)
		{
			_damageP1 += damage;
		}
		else if (player == _playerTwo)
		{
			_damageP2 += damage;
		}
	}
	
	protected String getTitle()
	{
		String msg = "";
		msg += _playerOneName + " / " + _playerTwoName;
		return msg;
	}
	
	protected PlayerInstance[] getPlayers()
	{
		final PlayerInstance[] players = new PlayerInstance[2];
		if ((_playerOne == null) || (_playerTwo == null))
		{
			return null;
		}
		
		players[0] = _playerOne;
		players[1] = _playerTwo;
		return players;
	}
	
	private void broadcastMessage(SystemMessage sm, boolean toAll)
	{
		try
		{
			_playerOne.sendPacket(sm);
			_playerTwo.sendPacket(sm);
		}
		catch (Exception e)
		{
			LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on players broadcastMessage, an error has been occurred: " + e);
		}
		
		if (toAll && (OlympiadManager.STADIUMS[_stadiumID].getSpectators() != null))
		{
			for (PlayerInstance spec : OlympiadManager.STADIUMS[_stadiumID].getSpectators())
			{
				if (spec != null)
				{
					try
					{
						spec.sendPacket(sm);
					}
					catch (Exception e)
					{
						LOGGER.warning("Olympiad System: Game - " + _stadiumID + " on player " + spec.getName() + " broadcastMessage, an error has been occurred: " + e);
					}
				}
			}
		}
	}
	
	protected void announceGame()
	{
		for (Spawn manager : Olympiad.olymanagers)
		{
			if ((manager != null) && (manager.getLastSpawn() != null))
			{
				final int objId = manager.getLastSpawn().getObjectId();
				final String npcName = manager.getLastSpawn().getName();
				manager.getLastSpawn().broadcastPacket(new CreatureSay(objId, ChatType.SHOUT, npcName, "Olympiad is going to begin in Arena " + (_stadiumID + 1) + " in a moment."));
			}
		}
	}
	
	public void sendPlayersStatus(PlayerInstance spec)
	{
		spec.sendPacket(new ExOlympiadUserInfo(_playerOne, 1));
		spec.sendPacket(new ExOlympiadUserInfo(_playerTwo, 2));
		spec.sendPacket(new ExOlympiadSpelledInfo(_playerOne));
		spec.sendPacket(new ExOlympiadSpelledInfo(_playerTwo));
	}
}

/**
 * @author ascharot
 */
class OlympiadGameTask implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadGameTask.class.getName());
	public OlympiadGame _game = null;
	protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 3 mins
	
	private boolean _terminated = false;
	private boolean _started = false;
	
	public boolean isTerminated()
	{
		return _terminated || _game._aborted;
	}
	
	public boolean isStarted()
	{
		return _started;
	}
	
	public OlympiadGameTask(OlympiadGame game)
	{
		_game = game;
	}
	
	protected boolean checkObserverStatusBug(PlayerInstance player)
	{
		if ((player != null) && player.inObserverMode())
		{
			LOGGER.info("[OLYMPIAD DEBUG] Player " + player.getName() + "is in Observer mode!");
			return true;
		}
		return false;
	}
	
	protected void removeObserverModeBug(PlayerInstance player)
	{
		if ((player == null) || !player.inObserverMode())
		{
			return;
		}
		
		player.setTarget(null);
		// Put the status back to 0 (removing observer mode on olympiad)
		player.sendPacket(new ExOlympiadMode(0, player));
		player.getAppearance().setVisible();
		player.setInvul(false);
		if (player.getAI() != null)
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		player.setObserverMode(false);
		
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			LOGGER.warning(e.toString());
		}
		
		// Put the status back to 2 (fighting mode on olympiad)
		player.sendPacket(new ExOlympiadMode(2, player));
		player.broadcastUserInfo();
		
		LOGGER.info("[OLYMPIAD DEBUG] Player " + player.getName() + "was on observer mode! Status restored!");
	}
	
	protected boolean checkBattleStatus()
	{
		final boolean pOneCrash = ((_game._playerOne == null) || _game._playerOneDisconnected);
		final boolean pTwoCrash = ((_game._playerTwo == null) || _game._playerTwoDisconnected);
		return !pOneCrash && !pTwoCrash && !_game._aborted;
	}
	
	protected boolean checkDefaulted()
	{
		for (int i = 0; i < 2; i++)
		{
			boolean defaulted = false;
			final PlayerInstance player = _game._players.get(i);
			if (player != null)
			{
				player.setOlympiadGameId(_game._stadiumID);
			}
			final PlayerInstance otherPlayer = _game._players.get(i ^ 1);
			SystemMessage sm = null;
			if ((player == null) || !player.isOnline())
			{
				defaulted = true;
			}
			else if (player.isDead())
			{
				sm = new SystemMessage(SystemMessageId.YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_WHILE_DEAD);
				defaulted = true;
			}
			else if (player.isSubClassActive())
			{
				sm = new SystemMessage(SystemMessageId.YOU_HAVE_CHANGED_FROM_YOUR_MAIN_CLASS_TO_A_SUBCLASS_AND_THEREFORE_ARE_REMOVED_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST);
				defaulted = true;
			}
			else if (player.isCursedWeaponEquiped())
			{
				sm = new SystemMessage(SystemMessageId.IF_YOU_POSSESS_S1_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
				sm.addItemName(player.getCursedWeaponEquipedId());
				defaulted = true;
			}
			else if ((player.getInventoryLimit() * 0.8) <= player.getInventory().getSize())
			{
				sm = new SystemMessage(SystemMessageId.YOU_CAN_T_JOIN_A_GRAND_OLYMPIAD_GAME_MATCH_WITH_THAT_MUCH_STUFF_ON_YOU_REDUCE_YOUR_WEIGHT_TO_BELOW_80_PERCENT_FULL_AND_REQUEST_TO_JOIN_AGAIN);
				defaulted = true;
			}
			
			if (defaulted)
			{
				if (player != null)
				{
					player.sendPacket(sm);
				}
				if (otherPlayer != null)
				{
					otherPlayer.sendPacket(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
				}
				if (i == 0)
				{
					_game._playerOneDefaulted = true;
				}
				else
				{
					_game._playerTwoDefaulted = true;
				}
			}
		}
		return _game._playerOneDefaulted || _game._playerTwoDefaulted;
	}
	
	@Override
	public void run()
	{
		_started = true;
		if (_game != null)
		{
			if ((_game._playerOne == null) || (_game._playerTwo == null))
			{
				return;
			}
			
			if (teleportCountdown())
			{
				runGame();
			}
			
			_terminated = true;
			_game.validateWinner();
			_game.PlayersStatusBack();
			
			if (_game._gamestarted)
			{
				_game._gamestarted = false;
				try
				{
					_game.portPlayersBack();
				}
				catch (Exception e)
				{
					LOGGER.warning(e.toString());
				}
			}
			
			if (OlympiadManager.STADIUMS[_game._stadiumID].getSpectators() != null)
			{
				for (PlayerInstance spec : OlympiadManager.STADIUMS[_game._stadiumID].getSpectators())
				{
					spec.leaveOlympiadObserverMode(true);
				}
			}
			
			_game.clearPlayers();
			_game = null;
		}
	}
	
	private boolean runGame()
	{
		_game._gamestarted = true;
		// Checking for opponents and teleporting to arena
		if (checkDefaulted())
		{
			return false;
		}
		_game.portPlayersToArena();
		_game.removals();
		if (Config.ALT_OLY_ANNOUNCE_GAMES)
		{
			_game.announceGame();
		}
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e)
		{
		}
		
		// TODO: This is a workaroud to fix the problem with Observer mode on olympiad on random fight
		// We must find why the player is in observer mode!
		// Try to remove the observer mode bug
		removeObserverModeBug(_game._playerOne);
		removeObserverModeBug(_game._playerTwo);
		
		synchronized (this)
		{
			if (!OlympiadGame._battleStarted)
			{
				OlympiadGame._battleStarted = true;
			}
		}
		
		for (int i = 45; i >= 15; i -= 15)
		{
			_game.sendMessageToPlayers(true, i);
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException e)
			{
			}
			if (i == 15)
			{
				_game.additions();
				_game._damageP1 = 0;
				_game._damageP2 = 0;
				_game.sendMessageToPlayers(true, 10);
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e)
				{
				}
			}
		}
		for (int i = 5; i > 0; i--)
		{
			_game.sendMessageToPlayers(true, i);
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
		}
		
		if (checkObserverStatusBug(_game._playerOne))
		{
			_game._playerOne.sendMessage("One player on this match is on Observer mode! Match aborted!");
			_game._playerTwo.sendMessage("One player on this match is on Observer mode! Match aborted!");
			_game._aborted = true;
			return false;
		}
		
		if (checkObserverStatusBug(_game._playerTwo))
		{
			_game._playerOne.sendMessage("One player on this match is on Observer mode! Match aborted!");
			_game._playerTwo.sendMessage("One player on this match is on Observer mode! Match aborted!");
			_game._aborted = true;
			return false;
		}
		
		if (!checkBattleStatus())
		{
			return false;
		}
		_game._playerOne.sendPacket(new ExOlympiadUserInfo(_game._playerTwo, 1));
		_game._playerTwo.sendPacket(new ExOlympiadUserInfo(_game._playerOne, 1));
		if (OlympiadManager.STADIUMS[_game._stadiumID].getSpectators() != null)
		{
			for (PlayerInstance spec : OlympiadManager.STADIUMS[_game._stadiumID].getSpectators())
			{
				_game.sendPlayersStatus(spec);
			}
		}
		
		if (!_game.makeCompetitionStart())
		{
			return false;
		}
		
		// Wait 3 mins (Battle)
		for (int i = 0; i < BATTLE_PERIOD; i += 2000)
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
			}
			
			// If game haveWinner then stop waiting battle_period and validate winner
			if (_game.haveWinner())
			{
				break;
			}
		}
		
		return checkBattleStatus();
	}
	
	private boolean teleportCountdown()
	{
		// Waiting for teleport to arena
		int k = 1;
		if ((Config.ALT_OLY_TELEPORT_COUNTDOWN % 5) == 0)
		{
			k = 5;
		}
		else if ((Config.ALT_OLY_TELEPORT_COUNTDOWN % 3) == 0)
		{
			k = 3;
		}
		else if ((Config.ALT_OLY_TELEPORT_COUNTDOWN % 2) == 0)
		{
			k = 2;
		}
		
		for (int i = Config.ALT_OLY_TELEPORT_COUNTDOWN; i > k; i -= k)
		{
			switch (i)
			{
				case 120:
				case 60:
				case 30:
				case 15:
				{
					_game.sendMessageToPlayers(false, i);
					break;
				}
			}
			try
			{
				Thread.sleep(k * 1000);
			}
			catch (InterruptedException e)
			{
				return false;
			}
		}
		for (int i = k; i > 0; i--)
		{
			_game.sendMessageToPlayers(false, i);
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				return false;
			}
		}
		return true;
	}
}
