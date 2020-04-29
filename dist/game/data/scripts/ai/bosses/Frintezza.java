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
package ai.bosses;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.xml.DoorData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.instancemanager.GrandBossManager;
import org.l2jserver.gameserver.model.CommandChannel;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jserver.gameserver.model.actor.instance.MonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.zone.type.BossZone;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.Earthquake;
import org.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillCanceld;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.NpcInfo;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.SpecialCamera;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Frintezza AI
 * @author Darki699
 * @author SANDMAN L2J_JP(modified)
 * @author JOJO Update by rocknow Updated by L2jOff team Updated by L2jFrozen team<br>
 *         Warn: be careful with adding new spawns {@link #getXFix(int)}
 */
public class Frintezza extends Quest
{
	// @formatter:off
	private static final int[][] _invadeLoc =
	{
		{174102, -76039, -5105},
		{173235, -76884, -5105},
		{175003, -76933, -5105},
		{174196, -76190, -5105},
		{174013, -76120, -5105},
		{173263, -75161, -5105}
	};
	
	private static final int[][] _skill =
	{
		{5015, 1, 5000},
		{5015, 4, 5000},
		{5015, 2, 5000},
		{5015, 5, 5000},
		{5018, 1, 10000},
		{5016, 1, 5000},
		{5015, 3, 5000},
		{5015, 6, 5000},
		{5018, 2, 10000},
		{5019, 1, 10000},
		{5016, 1, 5000}
	};
	
	private static final int[][] _mobLoc =
	{
		{18328, 172894, -76019, -5107, 243},
		{18328, 174095, -77279, -5107, 16216},
		{18328, 174111, -74833, -5107, 49043},
		{18328, 175344, -76042, -5107, 32847},
		{18330, 173489, -76227, -5134, 63565},
		{18330, 173498, -75724, -5107, 58498},
		{18330, 174365, -76745, -5107, 22424},
		{18330, 174570, -75584, -5107, 31968},
		{18330, 174613, -76179, -5107, 31471},
		{18332, 173620, -75981, -5107, 4588},
		{18332, 173630, -76340, -5107, 62454},
		{18332, 173755, -75613, -5107, 57892},
		{18332, 173823, -76688, -5107, 2411},
		{18332, 174000, -75411, -5107, 54718},
		{18332, 174487, -75555, -5107, 33861},
		{18332, 174517, -76471, -5107, 21893},
		{18332, 174576, -76122, -5107, 31176},
		{18332, 174600, -75841, -5134, 35927},
		{18329, 173481, -76043, -5107, 61312},
		{18329, 173539, -75678, -5107, 59524},
		{18329, 173584, -76386, -5107, 3041},
		{18329, 173773, -75420, -5107, 51115},
		{18329, 173777, -76650, -5107, 12588},
		{18329, 174585, -76510, -5107, 21704},
		{18329, 174623, -75571, -5107, 40141},
		{18329, 174744, -76240, -5107, 29202},
		{18329, 174769, -75895, -5107, 29572},
		{18333, 173861, -76011, -5107, 383},
		{18333, 173872, -76461, -5107, 8041},
		{18333, 173898, -75668, -5107, 51856},
		{18333, 174422, -75689, -5107, 42878},
		{18333, 174460, -76355, -5107, 27311},
		{18333, 174483, -76041, -5107, 30947},
		{18331, 173515, -76184, -5107, 6971},
		{18331, 173516, -75790, -5134, 3142},
		{18331, 173696, -76675, -5107, 6757},
		{18331, 173766, -75502, -5134, 60827},
		{18331, 174473, -75321, -5107, 37147},
		{18331, 174493, -76505, -5107, 34503},
		{18331, 174568, -75654, -5134, 41661},
		{18331, 174584, -76263, -5107, 31729},
		{18339, 173892, -81592, -5123, 50849},
		{18339, 173958, -81820, -5123, 7459},
		{18339, 174128, -81805, -5150, 21495},
		{18339, 174245, -81566, -5123, 41760},
		{18334, 173264, -81529, -5072, 1646},
		{18334, 173265, -81656, -5072, 441},
		{18334, 173267, -81889, -5072, 0},
		{18334, 173271, -82015, -5072, 65382},
		{18334, 174867, -81655, -5073, 32537},
		{18334, 174868, -81890, -5073, 32768},
		{18334, 174869, -81485, -5073, 32315},
		{18334, 174871, -82017, -5073, 33007},
		{18335, 173074, -80817, -5107, 8353},
		{18335, 173128, -82702, -5107, 5345},
		{18335, 173181, -82544, -5107, 65135},
		{18335, 173191, -80981, -5107, 6947},
		{18335, 174859, -80889, -5134, 24103},
		{18335, 174924, -82666, -5107, 38710},
		{18335, 174947, -80733, -5107, 22449},
		{18335, 175096, -82724, -5107, 42205},
		{18336, 173435, -80512, -5107, 65215},
		{18336, 173440, -82948, -5107, 417},
		{18336, 173443, -83120, -5107, 1094},
		{18336, 173463, -83064, -5107, 286},
		{18336, 173465, -80453, -5107, 174},
		{18336, 173465, -83006, -5107, 2604},
		{18336, 173468, -82889, -5107, 316},
		{18336, 173469, -80570, -5107, 65353},
		{18336, 173469, -80628, -5107, 166},
		{18336, 173492, -83121, -5107, 394},
		{18336, 173493, -80683, -5107, 0},
		{18336, 173497, -80510, -5134, 417},
		{18336, 173499, -82947, -5107, 0},
		{18336, 173521, -83063, -5107, 316},
		{18336, 173523, -82889, -5107, 128},
		{18336, 173524, -80627, -5134, 65027},
		{18336, 173524, -83007, -5107, 0},
		{18336, 173526, -80452, -5107, 64735},
		{18336, 173527, -80569, -5134, 65062},
		{18336, 174602, -83122, -5107, 33104},
		{18336, 174604, -82949, -5107, 33184},
		{18336, 174609, -80514, -5107, 33234},
		{18336, 174609, -80684, -5107, 32851},
		{18336, 174629, -80627, -5107, 33346},
		{18336, 174632, -80570, -5107, 32896},
		{18336, 174632, -83066, -5107, 32768},
		{18336, 174635, -82893, -5107, 33594},
		{18336, 174636, -80456, -5107, 32065},
		{18336, 174639, -83008, -5107, 33057},
		{18336, 174660, -80512, -5107, 33057},
		{18336, 174661, -83121, -5107, 32768},
		{18336, 174663, -82948, -5107, 32768},
		{18336, 174664, -80685, -5107, 32676},
		{18336, 174687, -83008, -5107, 32520},
		{18336, 174691, -83066, -5107, 32961},
		{18336, 174692, -80455, -5107, 33202},
		{18336, 174692, -80571, -5107, 32768},
		{18336, 174693, -80630, -5107, 32994},
		{18336, 174693, -82889, -5107, 32622},
		{18337, 172837, -82382, -5107, 58363},
		{18337, 172867, -81123, -5107, 64055},
		{18337, 172883, -82495, -5107, 64764},
		{18337, 172916, -81033, -5107, 7099},
		{18337, 172940, -82325, -5107, 58998},
		{18337, 172946, -82435, -5107, 58038},
		{18337, 172971, -81198, -5107, 14768},
		{18337, 172992, -81091, -5107, 9438},
		{18337, 173032, -82365, -5107, 59041},
		{18337, 173064, -81125, -5107, 5827},
		{18337, 175014, -81173, -5107, 26398},
		{18337, 175061, -82374, -5107, 43290},
		{18337, 175096, -81080, -5107, 24719},
		{18337, 175169, -82453, -5107, 37672},
		{18337, 175172, -80972, -5107, 32315},
		{18337, 175174, -82328, -5107, 41760},
		{18337, 175197, -81157, -5107, 27617},
		{18337, 175245, -82547, -5107, 40275},
		{18337, 175249, -81075, -5107, 28435},
		{18337, 175292, -82432, -5107, 42225},
		{18338, 173014, -82628, -5107, 11874},
		{18338, 173033, -80920, -5107, 10425},
		{18338, 173095, -82520, -5107, 49152},
		{18338, 173115, -80986, -5107, 9611},
		{18338, 173144, -80894, -5107, 5345},
		{18338, 173147, -82602, -5107, 51316},
		{18338, 174912, -80825, -5107, 24270},
		{18338, 174935, -80899, -5107, 18061},
		{18338, 175016, -82697, -5107, 39533},
		{18338, 175041, -80834, -5107, 25420},
		{18338, 175071, -82549, -5107, 39163},
		{18338, 175154, -82619, -5107, 36345}
	};
	// @formatter:on
	
	private static final int SCARLET1 = 29046;
	private static final int SCARLET2 = 29047;
	private static final int FRINTEZZA = 29045;
	private static final int GUIDE = 32011;
	private static final int CUBE = 29061;
	
	// Frintezza Status Tracking :
	private static final byte DORMANT = 0; // Frintezza is spawned and no one has entered yet. Entry is unlocked
	private static final byte WAITING = 1; // Frintezza is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	// before he unleashes his attack. Entry is unlocked
	private static final byte FIGHTING = 2; // Frintezza is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 3; // Frintezza has been killed. Entry is locked
	
	private static long _lastAction = 0;
	private static int _angle = 0;
	private static int _locCycle = 0;
	private static int _bomber = 0;
	private static int _checkDie = 0;
	private static int _onCheck = 0;
	private static int _onSong = 0;
	private static int _abnormal = 0;
	private static int _onMorph = 0;
	private static int _scarletX = 0;
	private static int _scarletY = 0;
	private static int _scarletZ = 0;
	private static int _scarletHeading = 0;
	private static int _secondMorph = 0;
	private static int _thirdMorph = 0;
	private static int _killHallAlarmDevice = 0;
	private static int _killDarkChoirPlayer = 0;
	private static int _killDarkChoirCaptain = 0;
	
	private static BossZone _zone;
	private GrandBossInstance _frintezza;
	private GrandBossInstance _weakScarlet;
	private GrandBossInstance _strongScarlet;
	@SuppressWarnings("unused")
	private GrandBossInstance _activeScarlet;
	private MonsterInstance _demon1;
	private MonsterInstance _demon2;
	private MonsterInstance _demon3;
	private MonsterInstance _demon4;
	private MonsterInstance _portrait1;
	private MonsterInstance _portrait2;
	private MonsterInstance _portrait3;
	private MonsterInstance _portrait4;
	private NpcInstance _frintezzaDummy;
	private NpcInstance _overheadDummy;
	private NpcInstance _portraitDummy1;
	private NpcInstance _portraitDummy3;
	private NpcInstance _scarletDummy;
	private final Collection<PlayerInstance> _playersInside = ConcurrentHashMap.newKeySet();
	private final Collection<NpcInstance> _room1Monsters = ConcurrentHashMap.newKeySet();
	private final Collection<NpcInstance> _room2Monsters = ConcurrentHashMap.newKeySet();
	private final Collection<Attackable> _minions = ConcurrentHashMap.newKeySet();
	
	// Boss: Frintezza
	public Frintezza()
	{
		super(-1, "ai/bosses");
		final int[] mob =
		{
			SCARLET1,
			SCARLET2,
			FRINTEZZA,
			18328,
			18329,
			18330,
			18331,
			18332,
			18333,
			18334,
			18335,
			18336,
			18337,
			18338,
			18339,
			29048,
			29049,
			29050,
			29051
		};
		_zone = GrandBossManager.getInstance().getZone(getXFix(174232), getYFix(-88020), getZFix(-5116));
		registerMobs(mob);
		addStartNpc(GUIDE);
		addTalkId(GUIDE);
		addStartNpc(CUBE);
		addTalkId(CUBE);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(FRINTEZZA);
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		if (status == DEAD)
		{
			final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				startQuestTimer("frintezza_unlock", temp, null, null);
			}
			else
			{
				GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
			}
		}
		else if (status != DORMANT)
		{
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
		}
		
		// tempfix for messed door cords
		for (int i = 0; i < 8; i++)
		{
			DoorData.getInstance().getDoor(25150051 + i).setRange(0, 0, 0, 0, 0, 0);
		}
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		long temp = 0;
		if (event.equals("waiting"))
		{
			startQuestTimer("close", 27000, npc, null);
			startQuestTimer("camera_1", 30000, npc, null);
			_zone.broadcastPacket(new Earthquake(getXFix(174232), getYFix(-88020), getZFix(-5116), 45, 27));
		}
		else if (event.equals("room1_spawn"))
		{
			for (int i = 0; i <= 17; i++)
			{
				final NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
				synchronized (_room1Monsters)
				{
					_room1Monsters.add(mob);
				}
			}
		}
		else if (event.equals("room1_spawn2"))
		{
			for (int i = 18; i <= 26; i++)
			{
				final NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
				synchronized (_room1Monsters)
				{
					_room1Monsters.add(mob);
				}
			}
		}
		else if (event.equals("room1_spawn3"))
		{
			for (int i = 27; i <= 32; i++)
			{
				final NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
				synchronized (_room1Monsters)
				{
					_room1Monsters.add(mob);
				}
			}
		}
		else if (event.equals("room1_spawn4"))
		{
			for (int i = 33; i <= 40; i++)
			{
				final NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
				synchronized (_room1Monsters)
				{
					_room1Monsters.add(mob);
				}
			}
		}
		else if (event.equals("room2_spawn"))
		{
			for (int i = 41; i <= 44; i++)
			{
				final NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
				synchronized (_room2Monsters)
				{
					_room2Monsters.add(mob);
				}
			}
		}
		else if (event.equals("room2_spawn2"))
		{
			for (int i = 45; i <= 131; i++)
			{
				final NpcInstance mob = addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0);
				synchronized (_room2Monsters)
				{
					_room2Monsters.add(mob);
				}
			}
		}
		else if (event.equals("room1_del"))
		{
			synchronized (_room1Monsters)
			{
				for (NpcInstance mob : _room1Monsters)
				{
					if (mob != null)
					{
						mob.deleteMe();
					}
				}
				_room1Monsters.clear();
			}
		}
		else if (event.equals("room2_del"))
		{
			synchronized (_room2Monsters)
			{
				for (NpcInstance mob : _room2Monsters)
				{
					if (mob != null)
					{
						mob.deleteMe();
					}
				}
				_room2Monsters.clear();
			}
		}
		else if (event.equals("room3_del"))
		{
			if (_demon1 != null)
			{
				_demon1.deleteMe();
			}
			if (_demon2 != null)
			{
				_demon2.deleteMe();
			}
			if (_demon3 != null)
			{
				_demon3.deleteMe();
			}
			if (_demon4 != null)
			{
				_demon4.deleteMe();
			}
			if (_portrait1 != null)
			{
				_portrait1.deleteMe();
			}
			if (_portrait2 != null)
			{
				_portrait2.deleteMe();
			}
			if (_portrait3 != null)
			{
				_portrait3.deleteMe();
			}
			if (_portrait4 != null)
			{
				_portrait4.deleteMe();
			}
			if (_frintezza != null)
			{
				_frintezza.deleteMe();
			}
			if (_weakScarlet != null)
			{
				_weakScarlet.deleteMe();
			}
			if (_strongScarlet != null)
			{
				_strongScarlet.deleteMe();
			}
			
			_demon1 = null;
			_demon2 = null;
			_demon3 = null;
			_demon4 = null;
			_portrait1 = null;
			_portrait2 = null;
			_portrait3 = null;
			_portrait4 = null;
			_frintezza = null;
			_weakScarlet = null;
			_strongScarlet = null;
			_activeScarlet = null;
		}
		else if (event.equals("clean"))
		{
			_lastAction = 0;
			_locCycle = 0;
			_checkDie = 0;
			_onCheck = 0;
			_abnormal = 0;
			_onMorph = 0;
			_secondMorph = 0;
			_thirdMorph = 0;
			_killHallAlarmDevice = 0;
			_killDarkChoirPlayer = 0;
			_killDarkChoirCaptain = 0;
			_playersInside.clear();
		}
		else if (event.equals("close"))
		{
			for (int i = 25150051; i <= 25150058; i++)
			{
				DoorData.getInstance().getDoor(i).closeMe();
			}
			for (int i = 25150061; i <= 25150070; i++)
			{
				DoorData.getInstance().getDoor(i).closeMe();
			}
			
			DoorData.getInstance().getDoor(25150042).closeMe();
			DoorData.getInstance().getDoor(25150043).closeMe();
			DoorData.getInstance().getDoor(25150045).closeMe();
			DoorData.getInstance().getDoor(25150046).closeMe();
		}
		/*
		 * else if (event.equals("loc_check")) { Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA); if (status == FIGHTING) { if (!_Zone.isInsideZone(npc)) npc.teleToLocation(getXFix(174232),getYFix(-88020),getZFix(-5116)); if (npc.getX() < getXFix(171932) || npc.getX() >
		 * getXFix(176532) || npc.getY() < getYFix(-90320) || npc.getY() > getYFix(-85720) || npc.getZ() < getZFix(-5130)) npc.teleToLocation(getXFix(174232),getYFix(-88020),getZFix(-5116)); } }
		 */
		else if (event.equals("camera_1"))
		{
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, FIGHTING);
			_frintezzaDummy = addSpawn(29052, 174240, -89805, -5022, 16048, false, 0);
			_frintezzaDummy.setInvul(true);
			_frintezzaDummy.setImmobilized(true);
			
			_overheadDummy = addSpawn(29052, 174232, -88020, -5110, 16384, false, 0);
			_overheadDummy.setInvul(true);
			_overheadDummy.setImmobilized(true);
			_overheadDummy.setCollisionHeight(600);
			_zone.broadcastPacket(new NpcInfo(_overheadDummy, null));
			_portraitDummy1 = addSpawn(29052, 172450, -87890, -5100, 16048, false, 0);
			_portraitDummy1.setImmobilized(true);
			_portraitDummy1.setInvul(true);
			
			_portraitDummy3 = addSpawn(29052, 176012, -87890, -5100, 16048, false, 0);
			_portraitDummy3.setImmobilized(true);
			_portraitDummy3.setInvul(true);
			
			_scarletDummy = addSpawn(29053, 174232, -88020, -5110, 16384, false, 0);
			_scarletDummy.setInvul(true);
			_scarletDummy.setImmobilized(true);
			
			startQuestTimer("stop_pc", 0, npc, null);
			startQuestTimer("camera_2", 1000, _overheadDummy, null);
		}
		else if (event.equals("camera_2"))
		{
			_zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 0, 75, -89, 0, 100));
			startQuestTimer("camera_2b", 0, _overheadDummy, null);
		}
		else if (event.equals("camera_2b"))
		{
			_zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 0, 75, -89, 0, 100));
			startQuestTimer("camera_3", 0, _overheadDummy, null);
		}
		else if (event.equals("camera_3"))
		{
			_zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 300, 90, -10, 6500, 7000));
			_frintezza = (GrandBossInstance) addSpawn(FRINTEZZA, 174240, -89805, -5022, 16048, false, 0);
			GrandBossManager.getInstance().addBoss(_frintezza);
			_frintezza.setImmobilized(true);
			_frintezza.setInvul(true);
			_frintezza.disableAllSkills();
			_zone.updateKnownList(_frintezza);
			
			_demon2 = (MonsterInstance) addSpawn(29051, 175876, -88713, -5100, 28205, false, 0);
			_demon2.setImmobilized(true);
			_demon2.disableAllSkills();
			_zone.updateKnownList(_demon2);
			
			_demon3 = (MonsterInstance) addSpawn(29051, 172608, -88702, -5100, 64817, false, 0);
			_demon3.setImmobilized(true);
			_demon3.disableAllSkills();
			_zone.updateKnownList(_demon3);
			
			_demon1 = (MonsterInstance) addSpawn(29050, 175833, -87165, -5100, 35048, false, 0);
			_demon1.setImmobilized(true);
			_demon1.disableAllSkills();
			_zone.updateKnownList(_demon1);
			
			_demon4 = (MonsterInstance) addSpawn(29050, 172634, -87165, -5100, 57730, false, 0);
			_demon4.setImmobilized(true);
			_demon4.disableAllSkills();
			_zone.updateKnownList(_demon4);
			
			startQuestTimer("camera_4", 6500, _overheadDummy, null);
		}
		else if (event.equals("camera_4"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezzaDummy.getObjectId(), 1800, 90, 8, 6500, 7000));
			startQuestTimer("camera_5", 900, _frintezzaDummy, null);
		}
		else if (event.equals("camera_5"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezzaDummy.getObjectId(), 140, 90, 10, 2500, 4500));
			startQuestTimer("camera_5b", 4000, _frintezzaDummy, null);
		}
		else if (event.equals("camera_5b"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 40, 75, -10, 0, 1000));
			startQuestTimer("camera_6", 0, _frintezza, null);
		}
		else if (event.equals("camera_6"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 40, 75, -10, 0, 12000));
			startQuestTimer("camera_7", 1350, _frintezza, null);
		}
		else if (event.equals("camera_7"))
		{
			_zone.broadcastPacket(new SocialAction(_frintezza.getObjectId(), 2));
			startQuestTimer("camera_8", 7000, _frintezza, null);
		}
		else if (event.equals("camera_8"))
		{
			startQuestTimer("camera_9", 1000, _frintezza, null);
			_frintezzaDummy.deleteMe();
			_frintezzaDummy = null;
		}
		else if (event.equals("camera_9"))
		{
			_zone.broadcastPacket(new SocialAction(_demon2.getObjectId(), 1));
			_zone.broadcastPacket(new SocialAction(_demon3.getObjectId(), 1));
			startQuestTimer("camera_9b", 400, _frintezza, null);
		}
		else if (event.equals("camera_9b"))
		{
			_zone.broadcastPacket(new SocialAction(_demon1.getObjectId(), 1));
			_zone.broadcastPacket(new SocialAction(_demon4.getObjectId(), 1));
			for (Creature pc : _zone.getCharactersInside())
			{
				if (pc instanceof PlayerInstance)
				{
					if (pc.getX() < getXFix(174232))
					{
						pc.broadcastPacket(new SpecialCamera(_portraitDummy1.getObjectId(), 1000, 118, 0, 0, 1000));
					}
					else
					{
						pc.broadcastPacket(new SpecialCamera(_portraitDummy3.getObjectId(), 1000, 62, 0, 0, 1000));
					}
				}
			}
			startQuestTimer("camera_9c", 0, _frintezza, null);
		}
		else if (event.equals("camera_9c"))
		{
			for (Creature pc : _zone.getCharactersInside())
			{
				if (pc instanceof PlayerInstance)
				{
					if (pc.getX() < getXFix(174232))
					{
						pc.broadcastPacket(new SpecialCamera(_portraitDummy1.getObjectId(), 1000, 118, 0, 0, 10000));
					}
					else
					{
						pc.broadcastPacket(new SpecialCamera(_portraitDummy3.getObjectId(), 1000, 62, 0, 0, 10000));
					}
				}
			}
			startQuestTimer("camera_10", 2000, _frintezza, null);
		}
		else if (event.equals("camera_10"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 240, 90, 0, 0, 1000));
			startQuestTimer("camera_11", 0, _frintezza, null);
		}
		else if (event.equals("camera_11"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 240, 90, 25, 5500, 10000));
			_zone.broadcastPacket(new SocialAction(_frintezza.getObjectId(), 3));
			_portraitDummy1.deleteMe();
			_portraitDummy3.deleteMe();
			_portraitDummy1 = null;
			_portraitDummy3 = null;
			startQuestTimer("camera_12", 4500, _frintezza, null);
		}
		else if (event.equals("camera_12"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 195, 35, 0, 10000));
			startQuestTimer("camera_13", 700, _frintezza, null);
		}
		else if (event.equals("camera_13"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 195, 35, 0, 10000));
			startQuestTimer("camera_14", 1300, _frintezza, null);
		}
		else if (event.equals("camera_14"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 120, 180, 45, 1500, 10000));
			_zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5006, 1, 34000, 0));
			startQuestTimer("camera_16", 1500, _frintezza, null);
		}
		else if (event.equals("camera_16"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 520, 135, 45, 8000, 10000));
			startQuestTimer("camera_17", 7500, _frintezza, null);
		}
		else if (event.equals("camera_17"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 1500, 110, 25, 10000, 13000));
			startQuestTimer("camera_18", 9500, _frintezza, null);
		}
		else if (event.equals("camera_18"))
		{
			_zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 930, 160, -20, 0, 1000));
			startQuestTimer("camera_18b", 0, _overheadDummy, null);
		}
		else if (event.equals("camera_18b"))
		{
			_zone.broadcastPacket(new SpecialCamera(_overheadDummy.getObjectId(), 600, 180, -25, 0, 10000));
			_zone.broadcastPacket(new MagicSkillUse(_scarletDummy, _overheadDummy, 5004, 1, 5800, 0));
			_weakScarlet = (GrandBossInstance) addSpawn(SCARLET1, 174232, -88020, -5110, 16384, false, 0);
			_weakScarlet.setInvul(true);
			_weakScarlet.setImmobilized(true);
			_weakScarlet.disableAllSkills();
			_zone.updateKnownList(_weakScarlet);
			_activeScarlet = _weakScarlet;
			
			/*
			 * startQuestTimer("camera_19", 2400, _scarletDummy, null); startQuestTimer("camera_19b", 5000, _scarletDummy, null);
			 */
			startQuestTimer("camera_19", 5500, _scarletDummy, null);
			startQuestTimer("camera_19b", 5400, _weakScarlet, null);
		}
		else if (event.equals("camera_19"))
		{
			_weakScarlet.teleToLocation(getXFix(174232), getYFix(-88020), getZFix(-5110));
		}
		else if (event.equals("camera_19b"))
		{
			_zone.broadcastPacket(new SpecialCamera(_scarletDummy.getObjectId(), 800, 180, 10, 1000, 10000));
			startQuestTimer("camera_20", 2100, _scarletDummy, null);
		}
		else if (event.equals("camera_20"))
		{
			_zone.broadcastPacket(new SpecialCamera(_weakScarlet.getObjectId(), 300, 60, 8, 0, 10000));
			startQuestTimer("camera_21", 2000, _weakScarlet, null);
		}
		else if (event.equals("camera_21"))
		{
			_zone.broadcastPacket(new SpecialCamera(_weakScarlet.getObjectId(), 500, 90, 10, 3000, 5000));
			startQuestTimer("camera_22", 3000, _weakScarlet, null);
		}
		else if (event.equals("camera_22"))
		{
			_portrait2 = (MonsterInstance) addSpawn(29049, 175876, -88713, -5000, 28205, false, 0);
			_portrait2.setImmobilized(true);
			_portrait2.disableAllSkills();
			_zone.updateKnownList(_portrait2);
			
			_portrait3 = (MonsterInstance) addSpawn(29049, 172608, -88702, -5000, 64817, false, 0);
			_portrait3.setImmobilized(true);
			_portrait3.disableAllSkills();
			_zone.updateKnownList(_portrait3);
			
			_portrait1 = (MonsterInstance) addSpawn(29048, 175833, -87165, -5000, 35048, false, 0);
			_portrait1.setImmobilized(true);
			_portrait1.disableAllSkills();
			_zone.updateKnownList(_portrait1);
			
			_portrait4 = (MonsterInstance) addSpawn(29048, 172634, -87165, -5000, 57730, false, 0);
			_portrait4.setImmobilized(true);
			_portrait4.disableAllSkills();
			_zone.updateKnownList(_portrait4);
			
			_overheadDummy.deleteMe();
			_scarletDummy.deleteMe();
			_overheadDummy = null;
			_scarletDummy = null;
			startQuestTimer("camera_23", 2000, _weakScarlet, null);
			startQuestTimer("start_pc", 2000, _weakScarlet, null);
			// startQuestTimer("loc_check", 60000, weakScarlet, null, true);
			startQuestTimer("songs_play", 10000 + Rnd.get(10000), _frintezza, null);
			startQuestTimer("skill01", 10000 + Rnd.get(10000), _weakScarlet, null);
		}
		else if (event.equals("camera_23"))
		{
			_demon1.setImmobilized(false);
			_demon2.setImmobilized(false);
			_demon3.setImmobilized(false);
			_demon4.setImmobilized(false);
			_demon1.enableAllSkills();
			_demon2.enableAllSkills();
			_demon3.enableAllSkills();
			_demon4.enableAllSkills();
			_portrait1.setImmobilized(false);
			_portrait2.setImmobilized(false);
			_portrait3.setImmobilized(false);
			_portrait4.setImmobilized(false);
			_portrait1.enableAllSkills();
			_portrait2.enableAllSkills();
			_portrait3.enableAllSkills();
			_portrait4.enableAllSkills();
			_weakScarlet.setInvul(false);
			_weakScarlet.setImmobilized(false);
			_weakScarlet.enableAllSkills();
			_weakScarlet.setRunning();
			
			startQuestTimer("spawn_minion", 20000, _portrait1, null);
			startQuestTimer("spawn_minion", 20000, _portrait2, null);
			startQuestTimer("spawn_minion", 20000, _portrait3, null);
			startQuestTimer("spawn_minion", 20000, _portrait4, null);
		}
		else if (event.equals("stop_pc"))
		{
			for (Creature creature : _zone.getCharactersInside())
			{
				creature.abortAttack();
				creature.abortCast();
				creature.disableAllSkills();
				creature.setTarget(null);
				creature.stopMove(null);
				creature.setImmobilized(true);
				creature.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		else if (event.equals("stop_npc"))
		{
			final int heading = npc.getHeading();
			if (heading < 32768)
			{
				_angle = Math.abs(180 - (int) (heading / 182.044444444));
			}
			else
			{
				_angle = Math.abs(540 - (int) (heading / 182.044444444));
			}
		}
		else if (event.equals("start_pc"))
		{
			for (Creature creature : _zone.getCharactersInside())
			{
				if (creature != _frintezza)
				{
					creature.enableAllSkills();
					creature.setImmobilized(false);
				}
			}
		}
		else if (event.equals("start_npc"))
		{
			npc.setRunning();
			npc.setInvul(false);
		}
		else if (event.equals("morph_end"))
		{
			_onMorph = 0;
		}
		else if (event.equals("morph_01"))
		{
			_zone.broadcastPacket(new SpecialCamera(_weakScarlet.getObjectId(), 250, _angle, 12, 2000, 15000));
			startQuestTimer("morph_02", 3000, _weakScarlet, null);
		}
		else if (event.equals("morph_02"))
		{
			_zone.broadcastPacket(new SocialAction(_weakScarlet.getObjectId(), 1));
			_weakScarlet.setRHandId(7903);
			startQuestTimer("morph_03", 4000, _weakScarlet, null);
		}
		else if (event.equals("morph_03"))
		{
			startQuestTimer("morph_04", 1500, _weakScarlet, null);
		}
		else if (event.equals("morph_04"))
		{
			_zone.broadcastPacket(new SocialAction(_weakScarlet.getObjectId(), 4));
			final Skill skill = SkillTable.getInstance().getInfo(5017, 1);
			if (skill != null)
			{
				skill.getEffects(_weakScarlet, _weakScarlet, false, false, false);
			}
			
			startQuestTimer("morph_end", 6000, _weakScarlet, null);
			startQuestTimer("start_pc", 3000, _weakScarlet, null);
			startQuestTimer("start_npc", 3000, _weakScarlet, null);
			startQuestTimer("songs_play", 10000 + Rnd.get(10000), _frintezza, null);
			startQuestTimer("skill02", 10000 + Rnd.get(10000), _weakScarlet, null);
		}
		else if (event.equals("morph_05a"))
		{
			_zone.broadcastPacket(new SocialAction(_frintezza.getObjectId(), 4));
		}
		else if (event.equals("morph_05"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 250, 120, 15, 0, 1000));
			startQuestTimer("morph_06", 0, _frintezza, null);
		}
		else if (event.equals("morph_06"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 250, 120, 15, 0, 10000));
			
			// cancelQuestTimer("loc_check");
			_scarletX = _weakScarlet.getX();
			_scarletY = _weakScarlet.getY();
			_scarletZ = _weakScarlet.getZ();
			_scarletHeading = _weakScarlet.getHeading();
			_weakScarlet.deleteMe();
			_weakScarlet = null;
			_activeScarlet = null;
			_weakScarlet = (GrandBossInstance) addSpawn(SCARLET1, _scarletX, _scarletY, _scarletZ, _scarletHeading, false, 0);
			_weakScarlet.setInvul(true);
			_weakScarlet.setImmobilized(true);
			_weakScarlet.disableAllSkills();
			_weakScarlet.setRHandId(7903);
			_zone.updateKnownList(_weakScarlet);
			
			startQuestTimer("morph_07", 7000, _frintezza, null);
		}
		else if (event.equals("morph_07"))
		{
			_zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5006, 1, 34000, 0));
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 500, 70, 15, 3000, 10000));
			startQuestTimer("morph_08", 3000, _frintezza, null);
		}
		else if (event.equals("morph_08"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 2500, 90, 12, 6000, 10000));
			startQuestTimer("morph_09", 3000, _frintezza, null);
		}
		else if (event.equals("morph_09"))
		{
			_zone.broadcastPacket(new SpecialCamera(_weakScarlet.getObjectId(), 250, _angle, 12, 0, 1000));
			startQuestTimer("morph_10", 0, _weakScarlet, null);
		}
		else if (event.equals("morph_10"))
		{
			_zone.broadcastPacket(new SpecialCamera(_weakScarlet.getObjectId(), 250, _angle, 12, 0, 10000));
			startQuestTimer("morph_11", 500, _weakScarlet, null);
		}
		else if (event.equals("morph_11"))
		{
			_weakScarlet.doDie(_weakScarlet);
			_zone.broadcastPacket(new SpecialCamera(_weakScarlet.getObjectId(), 450, _angle, 14, 8000, 8000));
			startQuestTimer("morph_12", 6250, _weakScarlet, null);
			startQuestTimer("morph_13", 7200, _weakScarlet, null);
		}
		else if (event.equals("morph_12"))
		{
			_weakScarlet.deleteMe();
			_weakScarlet = null;
		}
		else if (event.equals("morph_13"))
		{
			_strongScarlet = (GrandBossInstance) addSpawn(SCARLET2, reverseXFix(_scarletX), reverseYFix(_scarletY), reverseZFix(_scarletZ), _scarletHeading, false, 0);
			_strongScarlet.setInvul(true);
			_strongScarlet.setImmobilized(true);
			_strongScarlet.disableAllSkills();
			_zone.updateKnownList(_strongScarlet);
			_activeScarlet = _strongScarlet;
			_zone.broadcastPacket(new SpecialCamera(_strongScarlet.getObjectId(), 450, _angle, 12, 500, 14000));
			startQuestTimer("morph_14", 3000, _strongScarlet, null);
			// startQuestTimer("loc_check", 60000, strongScarlet, null, true);
		}
		else if (event.equals("morph_14"))
		{
			startQuestTimer("morph_15", 5100, _strongScarlet, null);
		}
		else if (event.equals("morph_15"))
		{
			_zone.broadcastPacket(new SocialAction(_strongScarlet.getObjectId(), 2));
			final Skill skill = SkillTable.getInstance().getInfo(5017, 1);
			if (skill != null)
			{
				skill.getEffects(_strongScarlet, _strongScarlet, false, false, false);
			}
			
			startQuestTimer("morph_end", 9000, _strongScarlet, null);
			startQuestTimer("start_pc", 6000, _strongScarlet, null);
			startQuestTimer("start_npc", 6000, _strongScarlet, null);
			startQuestTimer("songs_play", 10000 + Rnd.get(10000), _frintezza, null);
			startQuestTimer("skill03", 10000 + Rnd.get(10000), _strongScarlet, null);
		}
		else if (event.equals("morph_16"))
		{
			_zone.broadcastPacket(new SpecialCamera(_strongScarlet.getObjectId(), 300, _angle - 180, 5, 0, 7000));
			startQuestTimer("morph_17", 0, _strongScarlet, null);
		}
		else if (event.equals("morph_17"))
		{
			_zone.broadcastPacket(new SpecialCamera(_strongScarlet.getObjectId(), 200, _angle, 85, 4000, 10000));
			startQuestTimer("morph_17b", 7400, _frintezza, null);
			startQuestTimer("morph_18", 7500, _frintezza, null);
		}
		else if (event.equals("morph_17b"))
		{
			_frintezza.doDie(_frintezza);
		}
		else if (event.equals("morph_18"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 120, 5, 0, 7000));
			startQuestTimer("morph_19", 0, _frintezza, null);
		}
		else if (event.equals("morph_19"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 90, 5, 5000, 15000));
			startQuestTimer("morph_20", 7000, _frintezza, null);
			startQuestTimer("spawn_cubes", 7000, _frintezza, null);
		}
		else if (event.equals("morph_20"))
		{
			_zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 900, 90, 25, 7000, 10000));
			startQuestTimer("start_pc", 7000, _frintezza, null);
		}
		else if (event.equals("songs_play"))
		{
			if ((_frintezza != null) && !_frintezza.isDead() && (_onMorph == 0))
			{
				_onSong = Rnd.get(1, 5);
				if (_onSong == 3)
				{ // to fix skill exception
					_onSong = 2;
				}
				
				String songName = "";
				
				// Name of the songs are custom, named with client side description.
				switch (_onSong)
				{
					case 1:
					{
						songName = "Frintezza's Healing Rhapsody";
						break;
					}
					case 2:
					{
						songName = "Frintezza's Rampaging Opus";
						break;
					}
					case 3:
					{
						songName = "Frintezza's Power Concerto";
						break;
					}
					case 4:
					{
						songName = "Frintezza's Plagued Concerto";
						break;
					}
					case 5:
					{
						songName = "Frintezza's Psycho Symphony";
						break;
					}
					default:
					{
						songName = "Frintezza's Song";
						break;
					}
				}
				
				// Like L2OFF the skill name is printed on screen
				_zone.broadcastPacket(new ExShowScreenMessage(songName, 6000));
				if ((_onSong == 1) && (_thirdMorph == 1) && (_strongScarlet.getCurrentHp() < (_strongScarlet.getMaxHp() * 0.6)) && (Rnd.get(100) < 80))
				{
					_zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, 1, 32000, 0));
					startQuestTimer("songs_effect", 5000, _frintezza, null);
					startQuestTimer("songs_play", 32000 + Rnd.get(10000), _frintezza, null);
				}
				else if ((_onSong == 2) || (_onSong == 3))
				{
					_zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, _onSong, 32000, 0));
					startQuestTimer("songs_effect", 5000, _frintezza, null);
					startQuestTimer("songs_play", 32000 + Rnd.get(10000), _frintezza, null);
				}
				else if ((_onSong == 4) && (_secondMorph == 1))
				{
					_zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, 4, 31000, 0));
					startQuestTimer("songs_effect", 5000, _frintezza, null);
					startQuestTimer("songs_play", 31000 + Rnd.get(10000), _frintezza, null);
				}
				else if ((_onSong == 5) && (_thirdMorph == 1) && (_abnormal == 0))
				{
					_abnormal = 1;
					_zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, 5, 35000, 0));
					startQuestTimer("songs_effect", 5000, _frintezza, null);
					startQuestTimer("songs_play", 35000 + Rnd.get(10000), _frintezza, null);
				}
				else
				{
					startQuestTimer("songs_play", 5000 + Rnd.get(5000), _frintezza, null);
				}
			}
		}
		else if (event.equals("songs_effect"))
		{
			final Skill skill = SkillTable.getInstance().getInfo(5008, _onSong);
			if (skill == null)
			{
				return null;
			}
			
			String songName = "";
			
			// Name of the songs are custom, named with client side description.
			switch (_onSong)
			{
				case 1:
				{
					songName = "Frintezza's Concert Hall Melody";
					break;
				}
				case 2:
				{
					songName = "Frintezza's Rampaging Opus en masse";
					break;
				}
				case 3:
				{
					songName = "Frintezza Power Encore";
					break;
				}
				case 4:
				{
					songName = "Mournful Chorale Prelude";
					break;
				}
				case 5:
				{
					songName = "Hypnotic Mazurka ";
					break;
				}
				default:
				{
					songName = "Frintezza's Song";
					break;
				}
			}
			
			// Like L2OFF the skill name is printed on screen
			_zone.broadcastPacket(new ExShowScreenMessage(songName, 6000));
			if ((_onSong == 1) || (_onSong == 2) || (_onSong == 3))
			{
				/*
				 * TODO: Frintezza songs to be fixed like retail if (frintezza != null && !frintezza.isDead() && activeScarlet != null && !activeScarlet.isDead()) skill.getEffects(frintezza, activeScarlet, false, false, false);
				 */
			}
			else if (_onSong == 4)
			{
				for (Creature creature : _zone.getCharactersInside())
				{
					if ((creature instanceof PlayerInstance) && (Rnd.get(100) < 80))
					{
						skill.getEffects(_frintezza, creature, false, false, false);
						creature.sendPacket(new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU).addSkillName(5008, 4));
					}
				}
			}
			else if (_onSong == 5)
			{
				for (Creature creature : _zone.getCharactersInside())
				{
					if ((creature instanceof PlayerInstance) && (Rnd.get(100) < 70))
					{
						creature.abortAttack();
						creature.abortCast();
						creature.disableAllSkills();
						creature.stopMove(null);
						creature.setImmobilized(true);
						creature.setParalyzed(true);
						creature.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(_frintezza, creature, false, false, false);
						creature.startAbnormalEffect(Creature.ABNORMAL_EFFECT_DANCE_STUNNED);
						creature.sendPacket(new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU).addSkillName(5008, 5));
					}
				}
				startQuestTimer("stop_effect", 25000, _frintezza, null);
			}
		}
		else if (event.equals("stop_effect"))
		{
			for (Creature creature : _zone.getCharactersInside())
			{
				if (creature instanceof PlayerInstance)
				{
					creature.stopAbnormalEffect(Creature.ABNORMAL_EFFECT_DANCE_STUNNED);
					creature.stopAbnormalEffect(Creature.ABNORMAL_EFFECT_FLOATING_ROOT);
					creature.enableAllSkills();
					creature.setImmobilized(false);
					creature.setParalyzed(false);
				}
			}
			_abnormal = 0;
		}
		else if (event.equals("attack_stop"))
		{
			cancelQuestTimer("skill01", npc, null);
			cancelQuestTimer("skill02", npc, null);
			cancelQuestTimer("skill03", npc, null);
			cancelQuestTimer("songs_play", npc, null);
			cancelQuestTimer("songs_effect", npc, null);
			if (_frintezza != null)
			{
				_zone.broadcastPacket(new MagicSkillCanceld(_frintezza.getObjectId()));
			}
		}
		else if (event.equals("check_hp"))
		{
			if (npc.isDead())
			{
				_onMorph = 1;
				_zone.broadcastPacket(new PlaySound(1, "BS01_D", npc));
				startQuestTimer("attack_stop", 0, _frintezza, null);
				startQuestTimer("stop_pc", 0, npc, null);
				startQuestTimer("stop_npc", 0, npc, null);
				startQuestTimer("morph_16", 0, npc, null);
			}
			else
			{
				_checkDie = _checkDie + 10;
				if (_checkDie < 3000)
				{
					startQuestTimer("check_hp", 10, npc, null);
				}
				else
				{
					_onCheck = 0;
					_checkDie = 0;
				}
			}
		}
		else if (event.equals("skill01"))
		{
			if ((_weakScarlet != null) && !_weakScarlet.isDead() && (_secondMorph == 0) && (_thirdMorph == 0) && (_onMorph == 0))
			{
				final int i = Rnd.get(0, 1);
				final Skill skill = SkillTable.getInstance().getInfo(_skill[i][0], _skill[i][1]);
				if (skill != null)
				{
					_weakScarlet.stopMove(null);
					// weakScarlet.setCastingNow(true);
					_weakScarlet.doCast(skill);
				}
				startQuestTimer("skill01", _skill[i][2] + 5000 + Rnd.get(10000), npc, null);
			}
		}
		else if (event.equals("skill02"))
		{
			if ((_weakScarlet != null) && !_weakScarlet.isDead() && (_secondMorph == 1) && (_thirdMorph == 0) && (_onMorph == 0))
			{
				int i = 0;
				if (_abnormal == 0)
				{
					i = Rnd.get(2, 5);
				}
				else
				{
					i = Rnd.get(2, 4);
				}
				
				final Skill skill = SkillTable.getInstance().getInfo(_skill[i][0], _skill[i][1]);
				if (skill != null)
				{
					_weakScarlet.stopMove(null);
					// weakScarlet.setCastingNow(true);
					_weakScarlet.doCast(skill);
				}
				startQuestTimer("skill02", _skill[i][2] + 5000 + Rnd.get(10000), npc, null);
				if (i == 5)
				{
					_abnormal = 1;
					startQuestTimer("float_effect", 4000, _weakScarlet, null);
				}
			}
		}
		else if (event.equals("skill03"))
		{
			if ((_strongScarlet != null) && !_strongScarlet.isDead() && (_secondMorph == 1) && (_thirdMorph == 1) && (_onMorph == 0))
			{
				int i = 0;
				if (_abnormal == 0)
				{
					i = Rnd.get(6, 10);
				}
				else
				{
					i = Rnd.get(6, 9);
				}
				
				final Skill skill = SkillTable.getInstance().getInfo(_skill[i][0], _skill[i][1]);
				if (skill != null)
				{
					_strongScarlet.stopMove(null);
					// strongScarlet.setCastingNow(true);
					_strongScarlet.doCast(skill);
				}
				startQuestTimer("skill03", _skill[i][2] + 5000 + Rnd.get(10000), npc, null);
				if (i == 10)
				{
					_abnormal = 1;
					startQuestTimer("float_effect", 3000, npc, null);
				}
			}
		}
		else if (event.equals("float_effect"))
		{
			if (npc.isCastingNow())
			{
				startQuestTimer("float_effect", 500, npc, null);
			}
			else
			{
				for (Creature creature : _zone.getCharactersInside())
				{
					if ((creature instanceof PlayerInstance) && (creature.getFirstEffect(5016) != null))
					{
						creature.abortAttack();
						creature.abortCast();
						creature.disableAllSkills();
						creature.stopMove(null);
						creature.setImmobilized(true);
						creature.setParalyzed(true);
						creature.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						creature.startAbnormalEffect(Creature.ABNORMAL_EFFECT_FLOATING_ROOT);
					}
				}
				startQuestTimer("stop_effect", 25000, npc, null);
			}
		}
		else if (event.equals("action"))
		{
			_zone.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
		}
		else if (event.equals("bomber"))
		{
			_bomber = 0;
		}
		else if (event.equals("room_final"))
		{
			_zone.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.SHOUT, npc.getName(), "Exceeded his time limit, challenge failed!"));
			_zone.oustAllPlayers();
			
			cancelQuestTimer("waiting", npc, null);
			cancelQuestTimer("frintezza_despawn", null, null);
			startQuestTimer("clean", 1000, npc, null);
			startQuestTimer("close", 1000, npc, null);
			startQuestTimer("room1_del", 1000, npc, null);
			startQuestTimer("room2_del", 1000, npc, null);
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
		}
		else if (event.equals("frintezza_despawn"))
		{
			temp = (System.currentTimeMillis() - _lastAction);
			if (temp > 900000)
			{
				_zone.oustAllPlayers();
				
				cancelQuestTimer("waiting", npc, null);
				// cancelQuestTimer("loc_check");
				cancelQuestTimer("room_final", null, null);
				cancelQuestTimer("spawn_minion", npc, null);
				startQuestTimer("clean", 1000, npc, null);
				startQuestTimer("close", 1000, npc, null);
				startQuestTimer("attack_stop", 1000, npc, null);
				startQuestTimer("room1_del", 1000, npc, null);
				startQuestTimer("room2_del", 1000, npc, null);
				startQuestTimer("room3_del", 1000, npc, null);
				startQuestTimer("minions_despawn", 1000, npc, null);
				GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
				cancelQuestTimer("frintezza_despawn", null, null);
			}
		}
		else if (event.equals("minions_despawn"))
		{
			synchronized (_minions)
			{
				for (Attackable mob : _minions)
				{
					if (mob != null)
					{
						mob.decayMe();
					}
				}
				_minions.clear();
			}
		}
		else if (event.equals("spawn_minion"))
		{
			if ((npc != null) && !npc.isDead() && (_frintezza != null) && !_frintezza.isDead())
			{
				final NpcInstance mob = addSpawn(npc.getNpcId() + 2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				// mob.setIsRaidMinion(true);
				synchronized (_minions)
				{
					_minions.add((Attackable) mob);
				}
				
				startQuestTimer("action", 200, mob, null);
				startQuestTimer("spawn_minion", 18000, npc, null);
			}
		}
		else if (event.equals("spawn_cubes"))
		{
			addSpawn(CUBE, 174232, -88020, -5114, 16384, false, 900000);
		}
		else if (event.equals("frintezza_unlock"))
		{
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DORMANT);
		}
		else if (event.equals("remove_players"))
		{
			_zone.oustAllPlayers();
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		if (npc.getNpcId() == CUBE)
		{
			final int x = 150037 + Rnd.get(500);
			final int y = -57720 + Rnd.get(500);
			player.teleToLocation(x, y, -2976);
			return null;
		}
		
		String htmltext = "";
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		if (status == DEAD)
		{
			htmltext = "<html><body>There is nothing beyond the Magic Force Field. Come back later.<br>(You may not enter because Frintezza is not inside the Imperial Tomb.)</body></html>";
		}
		else if (status == DORMANT)
		{
			boolean partyCheckSuccess = true;
			if (!Config.BYPASS_FRINTEZZA_PARTIES_CHECK)
			{
				if ((!player.isInParty() || !player.getParty().isLeader(player)) || (player.getParty().getCommandChannel() == null) || (player.getParty().getCommandChannel().getChannelLeader() != player))
				{
					htmltext = "<html><body>No reaction. Contact must be initiated by the Command Channel Leader.</body></html>";
					partyCheckSuccess = false;
				}
				else if ((player.getParty().getCommandChannel().getParties().size() < Config.FRINTEZZA_MIN_PARTIES) || (player.getParty().getCommandChannel().getParties().size() > Config.FRINTEZZA_MAX_PARTIES))
				{
					htmltext = "<html><body>Your command channel needs to have at least " + Config.FRINTEZZA_MIN_PARTIES + " parties and a maximum of " + Config.FRINTEZZA_MAX_PARTIES + ".</body></html>";
					partyCheckSuccess = false;
				}
			}
			
			if (partyCheckSuccess)
			{
				if (player.getInventory().getItemByItemId(8073) == null)
				{
					htmltext = "<html><body>You dont have required item.</body></html>";
				}
				else
				{
					player.destroyItemByItemId("Quest", 8073, 1, player, true);
					GrandBossManager.getInstance().setBossStatus(FRINTEZZA, WAITING);
					startQuestTimer("close", 0, npc, null);
					startQuestTimer("room1_spawn", 5000, npc, null);
					startQuestTimer("room_final", 2100000, null, null);
					startQuestTimer("frintezza_despawn", 60000, null, null, true);
					_lastAction = System.currentTimeMillis();
					if (Config.BYPASS_FRINTEZZA_PARTIES_CHECK)
					{
						if (player.getParty() != null)
						{
							final CommandChannel cc = player.getParty().getCommandChannel();
							if (cc != null)
							{
								// teleport all parties into CC
								for (Party party : cc.getParties())
								{
									if (party == null)
									{
										continue;
									}
									
									synchronized (_playersInside)
									{
										for (PlayerInstance member : party.getPartyMembers())
										{
											if ((member == null) || (member.getLevel() < 74))
											{
												continue;
											}
											if (!member.isInsideRadius(npc, 700, false, false))
											{
												continue;
											}
											if (_playersInside.size() > 45)
											{
												member.sendMessage("The number of challenges have been full, so can not enter.");
												break;
											}
											_playersInside.add(member);
											_zone.allowPlayerEntry(member, 300);
											member.teleToLocation(getXFix(_invadeLoc[_locCycle][0]) + Rnd.get(50), getYFix(_invadeLoc[_locCycle][1]) + Rnd.get(50), getZFix(_invadeLoc[_locCycle][2]));
										}
										if (_playersInside.size() > 45)
										{
											break;
										}
									}
									
									_locCycle++;
									if (_locCycle >= 6)
									{
										_locCycle = 1;
									}
								}
							}
							else
							{ // teleport just actual party
								
								final Party party = player.getParty();
								for (PlayerInstance member : party.getPartyMembers())
								{
									if ((member == null) || (member.getLevel() < 74))
									{
										continue;
									}
									if (!member.isInsideRadius(npc, 700, false, false))
									{
										continue;
									}
									
									synchronized (_playersInside)
									{
										if (_playersInside.size() > 45)
										{
											member.sendMessage("The number of challenges have been full, so can not enter.");
											break;
										}
										_playersInside.add(member);
									}
									
									_zone.allowPlayerEntry(member, 300);
									member.teleToLocation(getXFix(_invadeLoc[_locCycle][0]) + Rnd.get(50), getYFix(_invadeLoc[_locCycle][1]) + Rnd.get(50), getZFix(_invadeLoc[_locCycle][2]));
								}
								
								_locCycle++;
								if (_locCycle >= 6)
								{
									_locCycle = 1;
								}
							}
						}
						else if (player.isInsideRadius(npc, 700, false, false))
						{
							synchronized (_playersInside)
							{
								_playersInside.add(player);
							}
							_zone.allowPlayerEntry(player, 300);
							player.teleToLocation(getXFix(_invadeLoc[_locCycle][0]) + Rnd.get(50), getYFix(_invadeLoc[_locCycle][1]) + Rnd.get(50), getZFix(_invadeLoc[_locCycle][2]));
						}
					}
					else
					{
						final CommandChannel cc = player.getParty().getCommandChannel();
						for (Party party : cc.getParties())
						{
							if (party == null)
							{
								continue;
							}
							
							synchronized (_playersInside)
							{
								for (PlayerInstance member : party.getPartyMembers())
								{
									if ((member == null) || (member.getLevel() < 74))
									{
										continue;
									}
									if (!member.isInsideRadius(npc, 700, false, false))
									{
										continue;
									}
									if (_playersInside.size() > 45)
									{
										member.sendMessage("The number of challenges have been full, so can not enter.");
										break;
									}
									_playersInside.add(member);
									_zone.allowPlayerEntry(member, 300);
									member.teleToLocation(getXFix(_invadeLoc[_locCycle][0]) + Rnd.get(50), getYFix(_invadeLoc[_locCycle][1]) + Rnd.get(50), getZFix(_invadeLoc[_locCycle][2]));
								}
								if (_playersInside.size() > 45)
								{
									break;
								}
							}
							
							_locCycle++;
							if (_locCycle >= 6)
							{
								_locCycle = 1;
							}
						}
					}
				}
			}
		}
		else
		{
			htmltext = "<html><body>Someone else is already inside the Magic Force Field. Try again later.</body></html>";
		}
		
		return htmltext;
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		_lastAction = System.currentTimeMillis();
		if (npc.getNpcId() == FRINTEZZA)
		{
			npc.setCurrentHpMp(npc.getMaxHp(), 0);
			return null;
		}
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		if ((npc.getNpcId() == SCARLET1) && (_secondMorph == 0) && (_thirdMorph == 0) && (_onMorph == 0) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.75)) && (status == FIGHTING))
		{
			startQuestTimer("attack_stop", 0, _frintezza, null);
			_secondMorph = 1;
			_onMorph = 1;
			startQuestTimer("stop_pc", 1000, npc, null);
			startQuestTimer("stop_npc", 1000, npc, null);
			startQuestTimer("morph_01", 1100, npc, null);
		}
		else if ((npc.getNpcId() == SCARLET1) && (_secondMorph == 1) && (_thirdMorph == 0) && (_onMorph == 0) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.5)) && (status == FIGHTING))
		{
			startQuestTimer("attack_stop", 0, _frintezza, null);
			_thirdMorph = 1;
			_onMorph = 1;
			startQuestTimer("stop_pc", 2000, npc, null);
			startQuestTimer("stop_npc", 2000, npc, null);
			startQuestTimer("morph_05a", 2000, npc, null);
			startQuestTimer("morph_05", 2100, npc, null);
		}
		else if ((npc.getNpcId() == SCARLET2) && (_secondMorph == 1) && (_thirdMorph == 1) && (_onCheck == 0) && (damage >= npc.getCurrentHp()) && (status == FIGHTING))
		{
			_onCheck = 1;
			startQuestTimer("check_hp", 0, npc, null);
		}
		else if (((npc.getNpcId() == 29050) || (npc.getNpcId() == 29051)) && (_bomber == 0) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.1)) && (Rnd.get(100) < 30))
		{
			_bomber = 1;
			startQuestTimer("bomber", 3000, npc, null);
			
			final Skill skill = SkillTable.getInstance().getInfo(5011, 1);
			if (skill != null)
			{
				// npc.setCastingNow(true);
				npc.doCast(skill);
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		final Integer status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		if (npc.getNpcId() == FRINTEZZA)
		{
			return null;
		}
		else if ((npc.getNpcId() == SCARLET2) && (_onCheck == 0) && (status == FIGHTING))
		{
			_onCheck = 1;
			startQuestTimer("stop_pc", 0, npc, null);
			startQuestTimer("stop_npc", 0, npc, null);
			startQuestTimer("morph_16", 0, npc, null);
		}
		else if ((npc.getNpcId() == SCARLET2) && (_onCheck == 1) && (status == FIGHTING))
		{
			// cancelQuestTimer("loc_check");
			cancelQuestTimer("spawn_minion", npc, null);
			cancelQuestTimer("frintezza_despawn", null, null);
			startQuestTimer("clean", 30000, npc, null);
			startQuestTimer("close", 30000, npc, null);
			startQuestTimer("room3_del", 60000, npc, null);
			startQuestTimer("minions_despawn", 60000, npc, null);
			startQuestTimer("remove_players", 900000, npc, null);
			GrandBossManager.getInstance().setBossStatus(FRINTEZZA, DEAD);
			final long respawnTime = (Config.FRINTEZZA_RESP_FIRST + Rnd.get(Config.FRINTEZZA_RESP_SECOND)) * 3600000;
			startQuestTimer("frintezza_unlock", respawnTime, npc, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatSet info = GrandBossManager.getInstance().getStatSet(FRINTEZZA);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(FRINTEZZA, info);
		}
		else if (npc.getNpcId() == 18328)
		{
			_killHallAlarmDevice++;
			if (_killHallAlarmDevice == 3) // open walls
			{
				for (int i = 25150051; i <= 25150058; i++)
				{
					DoorData.getInstance().getDoor(i).openMe();
				}
			}
			if (_killHallAlarmDevice == 4)
			{
				startQuestTimer("room1_del", 100, npc, null);
				startQuestTimer("room2_spawn", 100, npc, null);
				DoorData.getInstance().getDoor(25150042).openMe();
				DoorData.getInstance().getDoor(25150043).openMe();
			}
			/*
			 * _KillHallAlarmDevice++; if (_KillHallAlarmDevice == 4) { startQuestTimer("room1_del", 100, npc, null); startQuestTimer("room2_spawn", 100, npc, null); DoorTable.getInstance().getDoor(17130042).openMe(); DoorTable.getInstance().getDoor(17130043).openMe(); //
			 * DoorTable.getInstance().getDoor(17130045).openMe(); // DoorTable.getInstance().getDoor(17130046).openMe(); for (int i = 17130051; i <= 17130058; i++) DoorTable.getInstance().getDoor(i).openMe(); }
			 */
		}
		else if (npc.getNpcId() == 18339)
		{
			_killDarkChoirPlayer++;
			if (_killDarkChoirPlayer == 2)
			{
				DoorData.getInstance().getDoor(25150042).closeMe();
				DoorData.getInstance().getDoor(25150043).closeMe();
				DoorData.getInstance().getDoor(25150045).closeMe();
				DoorData.getInstance().getDoor(25150046).closeMe();
				int outside = 0;
				synchronized (_playersInside)
				{
					for (PlayerInstance room2_pc : _playersInside)
					{
						if (_zone.isInsideZone(room2_pc) && (room2_pc.getY() > -86130))
						{
							outside++;
						}
					}
				}
				
				if (outside == 0)
				{
					startQuestTimer("room2_del", 100, npc, null);
					startQuestTimer("waiting", 180000, npc, null);
					cancelQuestTimer("room_final", null, null);
				}
				else
				{
					for (int i = 25150061; i <= 25150070; i++)
					{
						DoorData.getInstance().getDoor(i).openMe();
					}
					
					startQuestTimer("room2_spawn2", 1000, npc, null);
				}
			}
		}
		else if (npc.getNpcId() == 18334)
		{
			_killDarkChoirCaptain++;
			if (_killDarkChoirCaptain == 8)
			{
				startQuestTimer("room2_del", 100, npc, null);
				DoorData.getInstance().getDoor(25150045).openMe();
				DoorData.getInstance().getDoor(25150046).openMe();
				
				startQuestTimer("waiting", 180000, npc, null);
				cancelQuestTimer("room_final", null, null);
			}
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	/**
	 * Override spawn method to correct spawn cords after lair move<br>
	 * TODO: Update cords in script, im just lazy :)
	 */
	@Override
	public NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		// cords fix:
		return super.addSpawn(npcId, getXFix(x), getYFix(y), getZFix(z), heading, randomOffset, despawnDelay);
	}
	
	public int getXFix(int x)
	{
		return x/* - 262016 */;
	}
	
	public int getYFix(int y)
	{
		return y/* - 65278 */;
	}
	
	public int getZFix(int z)
	{
		return z/* - 4065 */;
	}
	
	public int reverseXFix(int x)
	{
		return x/* + 262016 */;
	}
	
	public int reverseYFix(int y)
	{
		return y/* + 65278 */;
	}
	
	public int reverseZFix(int z)
	{
		return z/* + 4065 */;
	}
	
	public static void main(String[] args)
	{
		new Frintezza();
	}
}