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
package quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.instancemanager.QuestManager;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

public class SagasSuperClass extends Quest
{
	public int _qnu;
	public int[] _npc = {};
	public int[] _items = {};
	public int[] _mob = {};
	public int[] _classId = {};
	public int[] _prevClass = {};
	public int[] _x = {};
	public int[] _y = {};
	public int[] _z = {};
	public String[] _text = {};
	private static final Map<NpcInstance, Integer> SPAWN_LIST = new HashMap<>();
	// @formatter:off
	private static final int[][] QUEST_CLASSES =
	{
		{ 0x7f }, { 0x80, 0x81 }, { 0x82 }, { 0x05 }, { 0x14 }, { 0x15 },
		{ 0x02 }, { 0x03 }, { 0x2e }, { 0x30 }, { 0x33 }, { 0x34 }, { 0x08 },
		{ 0x17 }, { 0x24 }, { 0x09 }, { 0x18 }, { 0x25 }, { 0x10 }, { 0x11 },
		{ 0x1e }, { 0x0c }, { 0x1b }, { 0x28 }, { 0x0e }, { 0x1c }, { 0x29 },
		{ 0x0d }, { 0x06 }, { 0x22 }, { 0x21 }, { 0x2b }, { 0x37 }, { 0x39 }
	};
	// @formatter:on
	private static final int[] ARCHON_HALISHA_NORM =
	{
		18212,
		18214,
		18215,
		18216,
		18218
	};
	
	public SagasSuperClass(int id, String descr)
	{
		super(id, descr);
		_qnu = id;
	}
	
	public void registerNPCs()
	{
		addStartNpc(_npc[0]);
		addAttackId(_mob[2]);
		addAttackId(_mob[1]);
		addSkillUseId(_mob[1]);
		addFirstTalkId(_npc[4]);
		for (int npc : _npc)
		{
			addTalkId(npc);
		}
		for (int mobid : _mob)
		{
			addKillId(mobid);
		}
		final int[] questItemIds = _items.clone();
		questItemIds[0] = 0;
		questItemIds[2] = 0; // remove Ice Crystal and Divine Stone of Wisdom
		registerQuestItems(questItemIds);
		for (int archonMinion = 21646; archonMinion < 21652; archonMinion++)
		{
			addKillId(archonMinion);
		}
		for (int element : ARCHON_HALISHA_NORM)
		{
			addKillId(element);
		}
		for (int guardianAngel = 27214; guardianAngel < 27217; guardianAngel++)
		{
			addKillId(guardianAngel);
		}
	}
	
	public void Cast(NpcInstance npc, Creature target, int skillId, int level)
	{
		target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
		target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
	}
	
	public void addSpawn(QuestState st, NpcInstance mob)
	{
		SPAWN_LIST.put(mob, st.getPlayer().getObjectId());
	}
	
	public NpcInstance findSpawn(PlayerInstance player, NpcInstance npc)
	{
		if (SPAWN_LIST.containsKey(npc) && (SPAWN_LIST.get(npc) == player.getObjectId()))
		{
			return npc;
		}
		return null;
	}
	
	public void deleteSpawn(NpcInstance npc)
	{
		if (SPAWN_LIST.containsKey(npc))
		{
			SPAWN_LIST.remove(npc);
			npc.deleteMe();
		}
	}
	
	public QuestState findRightState(NpcInstance npc)
	{
		PlayerInstance player = null;
		QuestState st = null;
		if (SPAWN_LIST.containsKey(npc))
		{
			player = (PlayerInstance) World.getInstance().findObject(SPAWN_LIST.get(npc));
			if (player != null)
			{
				st = player.getQuestState(getName());
			}
		}
		return st;
	}
	
	public void giveHallishaMark(QuestState st2)
	{
		if (st2.getInt("spawned") == 0)
		{
			if (st2.getQuestItemsCount(_items[3]) >= 700)
			{
				st2.takeItems(_items[3], 20);
				final int xx = st2.getPlayer().getX();
				final int yy = st2.getPlayer().getY();
				final int zz = st2.getPlayer().getZ();
				final NpcInstance archon = st2.addSpawn(_mob[1], xx, yy, zz);
				addSpawn(st2, archon);
				st2.set("spawned", "1");
				startQuestTimer("Archon Hellisha has despawned", 600000, archon, st2.getPlayer());
				archon.broadcastNpcSay(_text[13].replace("PLAYERNAME", st2.getPlayer().getName()));
				((Attackable) archon).addDamageHate(st2.getPlayer(), 0, 99999);
				archon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st2.getPlayer(), null);
			}
			else
			{
				st2.giveItems(_items[3], 1);
			}
		}
	}
	
	public QuestState findQuest(PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null)
		{
			if (_qnu != 68)
			{
				if (player.getClassId().getId() == QUEST_CLASSES[_qnu - 67][0])
				{
					return st;
				}
			}
			else
			{
				for (int q = 0; q < 2; q++)
				{
					if (player.getClassId().getId() == QUEST_CLASSES[1][q])
					{
						return st;
					}
				}
			}
		}
		return null;
	}
	
	public int getClassId(PlayerInstance player)
	{
		if (player.getClassId().getId() == 0x81)
		{
			return _classId[1];
		}
		return _classId[0];
	}
	
	public int getPrevClass(PlayerInstance player)
	{
		if (player.getClassId().getId() == 0x81)
		{
			if (_prevClass.length == 1)
			{
				return -1;
			}
			return _prevClass[1];
		}
		return _prevClass[0];
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = "";
		if (st != null)
		{
			if (event.equals("0-011.htm") || event.equals("0-012.htm") || event.equals("0-013.htm") || event.equals("0-014.htm") || event.equals("0-015.htm"))
			{
				htmltext = event;
			}
			else if (event.equals("accept"))
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(_items[10], 1);
				htmltext = "0-03.htm";
			}
			else if (event.equals("0-1"))
			{
				if (player.getLevel() < 76)
				{
					htmltext = "0-02.htm";
					if (st.getState() == State.CREATED)
					{
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "0-05.htm";
				}
			}
			else if (event.equals("0-2"))
			{
				if (player.getLevel() >= 76)
				{
					st.exitQuest(false);
					st.set("cond", "0");
					htmltext = "0-07.htm";
					st.takeItems(_items[10], -1);
					st.rewardExpAndSp(2299404, 0);
					st.giveItems(57, 5000000);
					st.giveItems(6622, 1);
					final int playerClass = getClassId(player);
					final int prevClass = getPrevClass(player);
					player.setClassId(playerClass);
					if (!player.isSubClassActive() && (player.getBaseClass() == prevClass))
					{
						player.setBaseClass(playerClass);
					}
					player.broadcastUserInfo();
					Cast(npc, player, 4339, 1);
					
					final Quest q = QuestManager.getInstance().getQuest("SkillTransfer");
					if (q != null)
					{
						q.startQuestTimer("givePormanders", 1, npc, player);
					}
				}
				else
				{
					st.takeItems(_items[10], -1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "20");
					htmltext = "0-08.htm";
				}
			}
			else if (event.equals("1-3"))
			{
				st.set("cond", "3");
				htmltext = "1-05.htm";
			}
			else if (event.equals("1-4"))
			{
				st.set("cond", "4");
				st.takeItems(_items[0], 1);
				if (_items[11] != 0)
				{
					st.takeItems(_items[11], 1);
				}
				st.giveItems(_items[1], 1);
				htmltext = "1-06.htm";
			}
			else if (event.equals("2-1"))
			{
				st.set("cond", "2");
				htmltext = "2-05.htm";
			}
			else if (event.equals("2-2"))
			{
				st.set("cond", "5");
				st.takeItems(_items[1], 1);
				st.giveItems(_items[4], 1);
				htmltext = "2-06.htm";
			}
			else if (event.equals("3-5"))
			{
				htmltext = "3-07.htm";
			}
			else if (event.equals("3-6"))
			{
				st.set("cond", "11");
				htmltext = "3-02.htm";
			}
			else if (event.equals("3-7"))
			{
				st.set("cond", "12");
				htmltext = "3-03.htm";
			}
			else if (event.equals("3-8"))
			{
				st.set("cond", "13");
				st.takeItems(_items[2], 1);
				st.giveItems(_items[7], 1);
				htmltext = "3-08.htm";
			}
			else if (event.equals("4-1"))
			{
				htmltext = "4-010.htm";
			}
			else if (event.equals("4-2"))
			{
				st.giveItems(_items[9], 1);
				st.set("cond", "18");
				st.playSound("ItemSound.quest_middle");
				htmltext = "4-011.htm";
			}
			else if (event.equals("4-3"))
			{
				st.giveItems(_items[9], 1);
				st.set("cond", "18");
				npc.broadcastNpcSay(_text[13].replace("PLAYERNAME", player.getName()));
				st.set("Quest0", "0");
				cancelQuestTimer("Mob_2 has despawned", npc, player);
				st.playSound("ItemSound.quest_middle");
				deleteSpawn(npc);
				return null;
			}
			else if (event.equals("5-1"))
			{
				st.set("cond", "6");
				st.takeItems(_items[4], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "5-02.htm";
			}
			else if (event.equals("6-1"))
			{
				st.set("cond", "8");
				st.takeItems(_items[5], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "6-03.htm";
			}
			else if (event.equals("7-1"))
			{
				if (st.getInt("spawned") == 1)
				{
					htmltext = "7-03.htm";
				}
				else if (st.getInt("spawned") == 0)
				{
					final NpcInstance mob1 = st.addSpawn(_mob[0], _x[0], _y[0], _z[0]);
					st.set("spawned", "1");
					startQuestTimer("Mob_1 Timer 1", 500, mob1, player);
					startQuestTimer("Mob_1 has despawned", 300000, mob1, player);
					addSpawn(st, mob1);
					htmltext = "7-02.htm";
				}
				else
				{
					htmltext = "7-04.htm";
				}
			}
			else if (event.equals("7-2"))
			{
				st.set("cond", "10");
				st.takeItems(_items[6], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "7-06.htm";
			}
			else if (event.equals("8-1"))
			{
				st.set("cond", "14");
				st.takeItems(_items[7], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "8-02.htm";
			}
			else if (event.equals("9-1"))
			{
				st.set("cond", "17");
				st.takeItems(_items[8], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "9-03.htm";
			}
			else if (event.equals("10-1"))
			{
				if (st.getInt("Quest0") == 0)
				{
					final NpcInstance mob3 = st.addSpawn(_mob[2], _x[1], _y[1], _z[1]);
					final NpcInstance mob2 = st.addSpawn(_npc[4], _x[2], _y[2], _z[2]);
					addSpawn(st, mob3);
					addSpawn(st, mob2);
					st.set("Mob_2", String.valueOf(mob2.getObjectId()));
					st.set("Quest0", "1");
					st.set("Quest1", "45");
					startQuestTimer("Mob_3 Timer 1", 500, mob3, player, true);
					startQuestTimer("Mob_3 has despawned", 59000, mob3, player);
					startQuestTimer("Mob_2 Timer 1", 500, mob2, player);
					startQuestTimer("Mob_2 has despawned", 60000, mob2, player);
					htmltext = "10-02.htm";
				}
				else if (st.getInt("Quest1") == 45)
				{
					htmltext = "10-03.htm";
				}
				else
				{
					htmltext = "10-04.htm";
				}
			}
			else if (event.equals("10-2"))
			{
				st.set("cond", "19");
				st.takeItems(_items[9], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "10-06.htm";
			}
			else if (event.equals("11-9"))
			{
				st.set("cond", "15");
				htmltext = "11-03.htm";
			}
			else if (event.equals("Mob_1 Timer 1"))
			{
				npc.broadcastNpcSay(_text[0].replace("PLAYERNAME", player.getName()));
				return null;
			}
			else if (event.equals("Mob_1 has despawned"))
			{
				npc.broadcastNpcSay(_text[1].replace("PLAYERNAME", player.getName()));
				st.set("spawned", "0");
				deleteSpawn(npc);
				return null;
			}
			else if (event.equals("Archon Hellisha has despawned"))
			{
				npc.broadcastNpcSay(_text[6].replace("PLAYERNAME", player.getName()));
				st.set("spawned", "0");
				deleteSpawn(npc);
				return null;
			}
			else if (event.equals("Mob_3 Timer 1"))
			{
				final NpcInstance mob2 = findSpawn(player, (NpcInstance) World.getInstance().findObject(st.getInt("Mob_2")));
				if (npc.getKnownList().knowsObject(mob2))
				{
					((Attackable) npc).addDamageHate(mob2, 0, 99999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, mob2, null);
					mob2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc, null);
					npc.broadcastNpcSay(_text[14].replace("PLAYERNAME", player.getName()));
					cancelQuestTimer("Mob_3 Timer 1", npc, player);
				}
				return null;
			}
			else if (event.equals("Mob_3 has despawned"))
			{
				npc.broadcastNpcSay(_text[15].replace("PLAYERNAME", player.getName()));
				st.set("Quest0", "2");
				deleteSpawn(npc);
				return null;
			}
			else if (event.equals("Mob_2 Timer 1"))
			{
				npc.broadcastNpcSay(_text[7].replace("PLAYERNAME", player.getName()));
				startQuestTimer("Mob_2 Timer 2", 1500, npc, player);
				if (st.getInt("Quest1") == 45)
				{
					st.set("Quest1", "0");
				}
				return null;
			}
			else if (event.equals("Mob_2 Timer 2"))
			{
				npc.broadcastNpcSay(_text[8].replace("PLAYERNAME", player.getName()));
				startQuestTimer("Mob_2 Timer 3", 10000, npc, player);
				return null;
			}
			else if (event.equals("Mob_2 Timer 3"))
			{
				if (st.getInt("Quest0") == 0)
				{
					startQuestTimer("Mob_2 Timer 3", 13000, npc, player);
					if (Rnd.nextBoolean())
					{
						npc.broadcastNpcSay(_text[9].replace("PLAYERNAME", player.getName()));
					}
					else
					{
						npc.broadcastNpcSay(_text[10].replace("PLAYERNAME", player.getName()));
					}
				}
				return null;
			}
			else if (event.equals("Mob_2 has despawned"))
			{
				st.set("Quest1", String.valueOf(st.getInt("Quest1") + 1));
				if ((st.getInt("Quest0") == 1) || (st.getInt("Quest0") == 2) || (st.getInt("Quest1") > 3))
				{
					st.set("Quest0", "0");
					if (st.getInt("Quest0") == 1)
					{
						npc.broadcastNpcSay(_text[11].replace("PLAYERNAME", player.getName()));
					}
					else
					{
						npc.broadcastNpcSay(_text[12].replace("PLAYERNAME", player.getName()));
					}
					deleteSpawn(npc);
				}
				else
				{
					startQuestTimer("Mob_2 has despawned", 1000, npc, player);
				}
				return null;
			}
		}
		else
		{
			return null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance talker)
	{
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPCs minimum quest requirements.</body></html>";
		final QuestState st = talker.getQuestState(getName());
		if (st != null)
		{
			final int npcId = npc.getNpcId();
			if ((st.getState() == State.COMPLETED) && (npcId == _npc[0]))
			{
				htmltext = "<html><body>You have already completed this quest!</body></html>";
			}
			else if (st.getPlayer().getClassId().getId() == getPrevClass(st.getPlayer()))
			{
				switch (st.getInt("cond"))
				{
					case 0:
					{
						if (npcId == _npc[0])
						{
							htmltext = "0-01.htm";
						}
						break;
					}
					case 1:
					{
						if (npcId == _npc[0])
						{
							htmltext = "0-04.htm";
						}
						else if (npcId == _npc[2])
						{
							htmltext = "2-01.htm";
						}
						break;
					}
					case 2:
					{
						if (npcId == _npc[2])
						{
							htmltext = "2-02.htm";
						}
						else if (npcId == _npc[1])
						{
							htmltext = "1-01.htm";
						}
						break;
					}
					case 3:
					{
						if ((npcId == _npc[1]) && (st.getQuestItemsCount(_items[0]) != 0))
						{
							htmltext = "1-02.htm";
							if ((_items[11] == 0) || (st.getQuestItemsCount(_items[11]) != 0))
							{
								htmltext = "1-03.htm";
							}
						}
						break;
					}
					case 4:
					{
						if (npcId == _npc[1])
						{
							htmltext = "1-04.htm";
						}
						else if (npcId == _npc[2])
						{
							htmltext = "2-03.htm";
						}
						break;
					}
					case 5:
					{
						if (npcId == _npc[2])
						{
							htmltext = "2-04.htm";
						}
						else if (npcId == _npc[5])
						{
							htmltext = "5-01.htm";
						}
						break;
					}
					case 6:
					{
						if (npcId == _npc[5])
						{
							htmltext = "5-03.htm";
						}
						else if (npcId == _npc[6])
						{
							htmltext = "6-01.htm";
						}
						break;
					}
					case 7:
					{
						if (npcId == _npc[6])
						{
							htmltext = "6-02.htm";
						}
						break;
					}
					case 8:
					{
						if (npcId == _npc[6])
						{
							htmltext = "6-04.htm";
						}
						else if (npcId == _npc[7])
						{
							htmltext = "7-01.htm";
						}
						break;
					}
					case 9:
					{
						if (npcId == _npc[7])
						{
							htmltext = "7-05.htm";
						}
						break;
					}
					case 10:
					{
						if (npcId == _npc[7])
						{
							htmltext = "7-07.htm";
						}
						else if (npcId == _npc[3])
						{
							htmltext = "3-01.htm";
						}
						break;
					}
					case 11:
					case 12:
					{
						if (npcId == _npc[3])
						{
							if (st.getQuestItemsCount(_items[2]) > 0)
							{
								htmltext = "3-05.htm";
							}
							else
							{
								htmltext = "3-04.htm";
							}
						}
						break;
					}
					case 13:
					{
						if (npcId == _npc[3])
						{
							htmltext = "3-06.htm";
						}
						else if (npcId == _npc[8])
						{
							htmltext = "8-01.htm";
						}
						break;
					}
					case 14:
					{
						if (npcId == _npc[8])
						{
							htmltext = "8-03.htm";
						}
						else if (npcId == _npc[11])
						{
							htmltext = "11-01.htm";
						}
						break;
					}
					case 15:
					{
						if (npcId == _npc[11])
						{
							htmltext = "11-02.htm";
						}
						else if (npcId == _npc[9])
						{
							htmltext = "9-01.htm";
						}
						break;
					}
					case 16:
					{
						if (npcId == _npc[9])
						{
							htmltext = "9-02.htm";
						}
						break;
					}
					case 17:
					{
						if (npcId == _npc[9])
						{
							htmltext = "9-04.htm";
						}
						else if (npcId == _npc[10])
						{
							htmltext = "10-01.htm";
						}
						break;
					}
					case 18:
					{
						if (npcId == _npc[10])
						{
							htmltext = "10-05.htm";
						}
						break;
					}
					case 19:
					{
						if (npcId == _npc[10])
						{
							htmltext = "10-07.htm";
						}
						else if (npcId == _npc[0])
						{
							htmltext = "0-06.htm";
						}
						break;
					}
					case 20:
					{
						if (npcId == _npc[0])
						{
							if (st.getPlayer().getLevel() >= 76)
							{
								htmltext = "0-09.htm";
								if ((getClassId(st.getPlayer()) < 131) || (getClassId(st.getPlayer()) > 135)) // in Kamael quests, npc wants to chat for a bit before changing class
								{
									st.exitQuest(false);
									st.set("cond", "0");
									st.rewardExpAndSp(2299404, 0);
									st.giveItems(57, 5000000);
									st.giveItems(6622, 1);
									final int playerClass = getClassId(st.getPlayer());
									final int prevClass = getPrevClass(st.getPlayer());
									st.getPlayer().setClassId(playerClass);
									if (!st.getPlayer().isSubClassActive() && (st.getPlayer().getBaseClass() == prevClass))
									{
										st.getPlayer().setBaseClass(playerClass);
									}
									st.getPlayer().broadcastUserInfo();
									Cast(npc, st.getPlayer(), 4339, 1);
									
									final Quest q = QuestManager.getInstance().getQuest("SkillTransfer");
									if (q != null)
									{
										q.startQuestTimer("givePormanders", 1, npc, st.getPlayer());
									}
								}
							}
							else
							{
								htmltext = "0-010.htm";
							}
						}
						break;
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		final int npcId = npc.getNpcId();
		if (st != null)
		{
			final int cond = st.getInt("cond");
			if (npcId == _npc[4])
			{
				if (cond == 17)
				{
					final QuestState st2 = findRightState(npc);
					if (st2 != null)
					{
						player.setLastQuestNpcObject(npc.getObjectId());
						if (st == st2)
						{
							if (st.getInt("Tab") == 1)
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-04.htm";
								}
								else if (st.getInt("Quest0") == 1)
								{
									htmltext = "4-06.htm";
								}
							}
							else
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-01.htm";
								}
								else if (st.getInt("Quest0") == 1)
								{
									htmltext = "4-03.htm";
								}
							}
						}
						else
						{
							if (st.getInt("Tab") == 1)
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-05.htm";
								}
								else if (st.getInt("Quest0") == 1)
								{
									htmltext = "4-07.htm";
								}
							}
							else
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-02.htm";
								}
							}
						}
					}
				}
				else if (cond == 18)
				{
					htmltext = "4-08.htm";
				}
			}
		}
		if (htmltext.equals(""))
		{
			npc.showChatWindow(player);
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance player, int damage, boolean isPet)
	{
		final QuestState st2 = findRightState(npc);
		if (st2 == null)
		{
			return super.onAttack(npc, player, damage, isPet);
		}
		final int cond = st2.getInt("cond");
		final QuestState st = player.getQuestState(getName());
		final int npcId = npc.getNpcId();
		if ((npcId == _mob[2]) && (st == st2) && (cond == 17))
		{
			st.set("Quest0", String.valueOf(st.getInt("Quest0") + 1));
			if (st.getInt("Quest0") == 1)
			{
				npc.broadcastNpcSay(_text[16].replace("PLAYERNAME", player.getName()));
			}
			if (st.getInt("Quest0") > 15)
			{
				st.set("Quest0", "1");
				npc.broadcastNpcSay(_text[17].replace("PLAYERNAME", player.getName()));
				cancelQuestTimer("Mob_3 has despawned", npc, st2.getPlayer());
				st.set("Tab", "1");
				deleteSpawn(npc);
			}
		}
		else if ((npcId == _mob[1]) && (cond == 15) && ((st != st2) || player.isInParty()))
		{
			npc.broadcastNpcSay(_text[5].replace("PLAYERNAME", player.getName()));
			cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
			st2.set("spawned", "0");
			deleteSpawn(npc);
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onSkillUse(NpcInstance npc, PlayerInstance caster, Skill skill)
	{
		if (SPAWN_LIST.containsKey(npc) && (SPAWN_LIST.get(npc) != caster.getObjectId()))
		{
			final PlayerInstance questPlayer = (PlayerInstance) World.getInstance().findObject(SPAWN_LIST.get(npc));
			if (questPlayer == null)
			{
				return null;
			}
			for (WorldObject obj : skill.getTargetList(caster))
			{
				if ((obj == questPlayer) || (obj == npc))
				{
					final QuestState st2 = findRightState(npc);
					if (st2 == null)
					{
						return null;
					}
					npc.broadcastNpcSay(_text[5].replace("PLAYERNAME", caster.getName()));
					cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					deleteSpawn(npc);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(getName());
		for (int Archon_Minion = 21646; Archon_Minion < 21652; Archon_Minion++)
		{
			if (npcId == Archon_Minion)
			{
				final Party party = player.getParty();
				if (party != null)
				{
					final List<QuestState> partyQuestMembers = new ArrayList<>();
					for (PlayerInstance player1 : party.getPartyMembers())
					{
						final QuestState st1 = findQuest(player1);
						if ((st1 != null) && (st1.getInt("cond") == 15))
						{
							partyQuestMembers.add(st1);
						}
					}
					if (!partyQuestMembers.isEmpty())
					{
						final QuestState st2 = partyQuestMembers.get(Rnd.get(partyQuestMembers.size()));
						giveHallishaMark(st2);
					}
				}
				else
				{
					final QuestState st1 = findQuest(player);
					if ((st1 != null) && (st1.getInt("cond") == 15))
					{
						giveHallishaMark(st1);
					}
				}
				return super.onKill(npc, player, isPet);
			}
		}
		
		for (int element : ARCHON_HALISHA_NORM)
		{
			if (npcId == element)
			{
				final QuestState st1 = findQuest(player);
				if ((st1 != null) && (st1.getInt("cond") == 15))
				{
					// This is just a guess....not really sure what it actually says, if anything
					npc.broadcastNpcSay(_text[4].replace("PLAYERNAME", st1.getPlayer().getName()));
					st1.giveItems(_items[8], 1);
					st1.takeItems(_items[3], -1);
					st1.set("cond", "16");
					st1.playSound("ItemSound.quest_middle");
				}
				return super.onKill(npc, player, isPet);
			}
		}
		
		for (int guardianAngel = 27214; guardianAngel < 27217; guardianAngel++)
		{
			if (npcId == guardianAngel)
			{
				final QuestState st1 = findQuest(player);
				if ((st1 != null) && (st1.getInt("cond") == 6))
				{
					if (st1.getInt("kills") < 9)
					{
						st1.set("kills", String.valueOf(st1.getInt("kills") + 1));
					}
					else
					{
						st1.playSound("ItemSound.quest_middle");
						st1.giveItems(_items[5], 1);
						st1.set("cond", "7");
					}
				}
				return super.onKill(npc, player, isPet);
			}
		}
		if ((st != null) && (npcId != _mob[2]))
		{
			final QuestState st2 = findRightState(npc);
			if (st2 == null)
			{
				return super.onKill(npc, player, isPet);
			}
			final int cond = st.getInt("cond");
			if ((npcId == _mob[0]) && (cond == 8))
			{
				if (!player.isInParty() && (st == st2))
				{
					npc.broadcastNpcSay(_text[12].replace("PLAYERNAME", player.getName()));
					st.giveItems(_items[6], 1);
					st.set("cond", "9");
					st.playSound("ItemSound.quest_middle");
				}
				cancelQuestTimer("Mob_1 has despawned", npc, st2.getPlayer());
				st2.set("spawned", "0");
				deleteSpawn(npc);
			}
			else if ((npcId == _mob[1]) && (cond == 15))
			{
				if (!player.isInParty())
				{
					if (st == st2)
					{
						npc.broadcastNpcSay(_text[4].replace("PLAYERNAME", player.getName()));
						st.giveItems(_items[8], 1);
						st.takeItems(_items[3], -1);
						st.set("cond", "16");
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						npc.broadcastNpcSay(_text[5].replace("PLAYERNAME", player.getName()));
					}
				}
				cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
				st2.set("spawned", "0");
				deleteSpawn(npc);
			}
		}
		else
		{
			if (npcId == _mob[0])
			{
				st = findRightState(npc);
				if (st != null)
				{
					cancelQuestTimer("Mob_1 has despawned", npc, st.getPlayer());
					st.set("spawned", "0");
					deleteSpawn(npc);
				}
			}
			else if (npcId == _mob[1])
			{
				st = findRightState(npc);
				if (st != null)
				{
					cancelQuestTimer("Archon Hellisha has despawned", npc, st.getPlayer());
					st.set("spawned", "0");
					deleteSpawn(npc);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
}