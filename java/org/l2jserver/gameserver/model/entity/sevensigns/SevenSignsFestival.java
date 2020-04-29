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
package org.l2jserver.gameserver.model.entity.sevensigns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.sql.SpawnTable;
import org.l2jserver.gameserver.datatables.xml.ExperienceData;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.enums.TeleportWhereType;
import org.l2jserver.gameserver.model.Location;
import org.l2jserver.gameserver.model.Party;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.FestivalMonsterInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.model.spawn.SpawnListener;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.Util;

/**
 * Seven Signs Festival of Darkness Engine TODO: - Archer mobs should target healer characters over other party members. - ADDED 29 Sep: Players that leave a party during the Seven Signs Festival will now take damage and cannot be healed.
 * @author Tempy
 */
public class SevenSignsFestival implements SpawnListener
{
	protected static final Logger LOGGER = Logger.getLogger(SevenSignsFestival.class.getName());
	
	private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)";
	public static final long FESTIVAL_SIGNUP_TIME = Config.ALT_FESTIVAL_CYCLE_LENGTH - Config.ALT_FESTIVAL_LENGTH - 60000;
	private static final int FESTIVAL_MAX_OFFSET_X = 230;
	private static final int FESTIVAL_MAX_OFFSET_Y = 230;
	private static final int FESTIVAL_DEFAULT_RESPAWN = 60; // Specify in seconds!
	public static final int FESTIVAL_COUNT = 5;
	public static final int FESTIVAL_LEVEL_MAX_FOR_31 = 0;
	public static final int FESTIVAL_LEVEL_MAX_FOR_42 = 1;
	public static final int FESTIVAL_LEVEL_MAX_FOR_53 = 2;
	public static final int FESTIVAL_LEVEL_MAX_FOR_64 = 3;
	public static final int FESTIVAL_LEVEL_MAX_FOR_NONE = 4;
	public static final int[] FESTIVAL_LEVEL_SCORES =
	{
		60,
		70,
		100,
		120,
		150
	}; // 500 maximum possible score
	public static final int FESTIVAL_OFFERING_ID = 5901;
	public static final int FESTIVAL_OFFERING_VALUE = 5;
	public static final int[][] FESTIVAL_DAWN_PLAYER_SPAWNS =
	{
		{
			-79187,
			113186,
			-4895,
			0
		}, // 31 and below
		{
			-75918,
			110137,
			-4895,
			0
		}, // 42 and below
		{
			-73835,
			111969,
			-4895,
			0
		}, // 53 and below
		{
			-76170,
			113804,
			-4895,
			0
		}, // 64 and below
		{
			-78927,
			109528,
			-4895,
			0
		}
		// No level limit
	};
	public static final int[][] FESTIVAL_DUSK_PLAYER_SPAWNS =
	{
		{
			-77200,
			88966,
			-5151,
			0
		}, // 31 and below
		{
			-76941,
			85307,
			-5151,
			0
		}, // 42 and below
		{
			-74855,
			87135,
			-5151,
			0
		}, // 53 and below
		{
			-80208,
			88222,
			-5151,
			0
		}, // 64 and below
		{
			-79954,
			84697,
			-5151,
			0
		}
		// No level limit
	};
	protected static final int[][] FESTIVAL_DAWN_WITCH_SPAWNS =
	{
		{
			-79183,
			113052,
			-4891,
			0,
			31132
		}, // 31 and below
		{
			-75916,
			110270,
			-4891,
			0,
			31133
		}, // 42 and below
		{
			-73979,
			111970,
			-4891,
			0,
			31134
		}, // 53 and below
		{
			-76174,
			113663,
			-4891,
			0,
			31135
		}, // 64 and below
		{
			-78930,
			109664,
			-4891,
			0,
			31136
		}
		// No level limit
	};
	protected static final int[][] FESTIVAL_DUSK_WITCH_SPAWNS =
	{
		{
			-77199,
			88830,
			-5147,
			0,
			31142
		}, // 31 and below
		{
			-76942,
			85438,
			-5147,
			0,
			31143
		}, // 42 and below
		{
			-74990,
			87135,
			-5147,
			0,
			31144
		}, // 53 and below
		{
			-80207,
			88222,
			-5147,
			0,
			31145
		}, // 64 and below
		{
			-79952,
			84833,
			-5147,
			0,
			31146
		}
		// No level limit
	};
	protected static final int[][][] FESTIVAL_DAWN_PRIMARY_SPAWNS =
	{
		{
			/* Level 31 and Below - Offering of the Branded */
			{
				-78537,
				113839,
				-4895,
				-1,
				18009
			},
			{
				-78466,
				113852,
				-4895,
				-1,
				18010
			},
			{
				-78509,
				113899,
				-4895,
				-1,
				18010
			},
			
			{
				-78481,
				112557,
				-4895,
				-1,
				18009
			},
			{
				-78559,
				112504,
				-4895,
				-1,
				18010
			},
			{
				-78489,
				112494,
				-4895,
				-1,
				18010
			},
			
			{
				-79803,
				112543,
				-4895,
				-1,
				18012
			},
			{
				-79854,
				112492,
				-4895,
				-1,
				18013
			},
			{
				-79886,
				112557,
				-4895,
				-1,
				18014
			},
			
			{
				-79821,
				113811,
				-4895,
				-1,
				18015
			},
			{
				-79857,
				113896,
				-4895,
				-1,
				18017
			},
			{
				-79878,
				113816,
				-4895,
				-1,
				18018
			},
			
			// Archers and Marksmen \\
			{
				-79190,
				113660,
				-4895,
				-1,
				18011
			},
			{
				-78710,
				113188,
				-4895,
				-1,
				18011
			},
			{
				-79190,
				112730,
				-4895,
				-1,
				18016
			},
			{
				-79656,
				113188,
				-4895,
				-1,
				18016
			}
		},
		{
			/* Level 42 and Below - Apostate Offering */
			{
				-76558,
				110784,
				-4895,
				-1,
				18019
			},
			{
				-76607,
				110815,
				-4895,
				-1,
				18020
			}, // South West
			{
				-76559,
				110820,
				-4895,
				-1,
				18020
			},
			
			{
				-75277,
				110792,
				-4895,
				-1,
				18019
			},
			{
				-75225,
				110801,
				-4895,
				-1,
				18020
			}, // South East
			{
				-75262,
				110832,
				-4895,
				-1,
				18020
			},
			
			{
				-75249,
				109441,
				-4895,
				-1,
				18022
			},
			{
				-75278,
				109495,
				-4895,
				-1,
				18023
			}, // North East
			{
				-75223,
				109489,
				-4895,
				-1,
				18024
			},
			
			{
				-76556,
				109490,
				-4895,
				-1,
				18025
			},
			{
				-76607,
				109469,
				-4895,
				-1,
				18027
			}, // North West
			{
				-76561,
				109450,
				-4895,
				-1,
				18028
			},
			
			// Archers and Marksmen \\
			{
				-76399,
				110144,
				-4895,
				-1,
				18021
			},
			{
				-75912,
				110606,
				-4895,
				-1,
				18021
			},
			{
				-75444,
				110144,
				-4895,
				-1,
				18026
			},
			{
				-75930,
				109665,
				-4895,
				-1,
				18026
			}
		},
		{
			/* Level 53 and Below - Witch's Offering */
			{
				-73184,
				111319,
				-4895,
				-1,
				18029
			},
			{
				-73135,
				111294,
				-4895,
				-1,
				18030
			}, // South West
			{
				-73185,
				111281,
				-4895,
				-1,
				18030
			},
			
			{
				-74477,
				111321,
				-4895,
				-1,
				18029
			},
			{
				-74523,
				111293,
				-4895,
				-1,
				18030
			}, // South East
			{
				-74481,
				111280,
				-4895,
				-1,
				18030
			},
			
			{
				-74489,
				112604,
				-4895,
				-1,
				18032
			},
			{
				-74491,
				112660,
				-4895,
				-1,
				18033
			}, // North East
			{
				-74527,
				112629,
				-4895,
				-1,
				18034
			},
			
			{
				-73197,
				112621,
				-4895,
				-1,
				18035
			},
			{
				-73142,
				112631,
				-4895,
				-1,
				18037
			}, // North West
			{
				-73182,
				112656,
				-4895,
				-1,
				18038
			},
			
			// Archers and Marksmen \\
			{
				-73834,
				112430,
				-4895,
				-1,
				18031
			},
			{
				-74299,
				111959,
				-4895,
				-1,
				18031
			},
			{
				-73841,
				111491,
				-4895,
				-1,
				18036
			},
			{
				-73363,
				111959,
				-4895,
				-1,
				18036
			}
		},
		{
			/* Level 64 and Below - Dark Omen Offering */
			{
				-75543,
				114461,
				-4895,
				-1,
				18039
			},
			{
				-75514,
				114493,
				-4895,
				-1,
				18040
			}, // South West
			{
				-75488,
				114456,
				-4895,
				-1,
				18040
			},
			
			{
				-75521,
				113158,
				-4895,
				-1,
				18039
			},
			{
				-75504,
				113110,
				-4895,
				-1,
				18040
			}, // South East
			{
				-75489,
				113142,
				-4895,
				-1,
				18040
			},
			
			{
				-76809,
				113143,
				-4895,
				-1,
				18042
			},
			{
				-76860,
				113138,
				-4895,
				-1,
				18043
			}, // North East
			{
				-76831,
				113112,
				-4895,
				-1,
				18044
			},
			
			{
				-76831,
				114441,
				-4895,
				-1,
				18045
			},
			{
				-76840,
				114490,
				-4895,
				-1,
				18047
			}, // North West
			{
				-76864,
				114455,
				-4895,
				-1,
				18048
			},
			
			// Archers and Marksmen \\
			{
				-75703,
				113797,
				-4895,
				-1,
				18041
			},
			{
				-76180,
				114263,
				-4895,
				-1,
				18041
			},
			{
				-76639,
				113797,
				-4895,
				-1,
				18046
			},
			{
				-76180,
				113337,
				-4895,
				-1,
				18046
			}
		},
		{
			/* No Level Limit - Offering of Forbidden Path */
			{
				-79576,
				108881,
				-4895,
				-1,
				18049
			},
			{
				-79592,
				108835,
				-4895,
				-1,
				18050
			}, // South West
			{
				-79614,
				108871,
				-4895,
				-1,
				18050
			},
			
			{
				-79586,
				110171,
				-4895,
				-1,
				18049
			},
			{
				-79589,
				110216,
				-4895,
				-1,
				18050
			}, // South East
			{
				-79620,
				110177,
				-4895,
				-1,
				18050
			},
			
			{
				-78825,
				110182,
				-4895,
				-1,
				18052
			},
			{
				-78238,
				110182,
				-4895,
				-1,
				18053
			}, // North East
			{
				-78266,
				110218,
				-4895,
				-1,
				18054
			},
			
			{
				-78275,
				108883,
				-4895,
				-1,
				18055
			},
			{
				-78267,
				108839,
				-4895,
				-1,
				18057
			}, // North West
			{
				-78241,
				108871,
				-4895,
				-1,
				18058
			},
			
			// Archers and Marksmen \\
			{
				-79394,
				109538,
				-4895,
				-1,
				18051
			},
			{
				-78929,
				109992,
				-4895,
				-1,
				18051
			},
			{
				-78454,
				109538,
				-4895,
				-1,
				18056
			},
			{
				-78929,
				109053,
				-4895,
				-1,
				18056
			}
		}
	};
	
	/** The Constant FESTIVAL_DUSK_PRIMARY_SPAWNS. */
	protected static final int[][][] FESTIVAL_DUSK_PRIMARY_SPAWNS =
	{
		{
			/* Level 31 and Below - Offering of the Branded */
			{
				-76542,
				89653,
				-5151,
				-1,
				18009
			},
			{
				-76509,
				89637,
				-5151,
				-1,
				18010
			},
			{
				-76548,
				89614,
				-5151,
				-1,
				18010
			},
			
			{
				-76539,
				88326,
				-5151,
				-1,
				18009
			},
			{
				-76512,
				88289,
				-5151,
				-1,
				18010
			},
			{
				-76546,
				88287,
				-5151,
				-1,
				18010
			},
			
			{
				-77879,
				88308,
				-5151,
				-1,
				18012
			},
			{
				-77886,
				88310,
				-5151,
				-1,
				18013
			},
			{
				-77879,
				88278,
				-5151,
				-1,
				18014
			},
			
			{
				-77857,
				89605,
				-5151,
				-1,
				18015
			},
			{
				-77858,
				89658,
				-5151,
				-1,
				18017
			},
			{
				-77891,
				89633,
				-5151,
				-1,
				18018
			},
			
			// Archers and Marksmen \\
			{
				-76728,
				88962,
				-5151,
				-1,
				18011
			},
			{
				-77194,
				88494,
				-5151,
				-1,
				18011
			},
			{
				-77660,
				88896,
				-5151,
				-1,
				18016
			},
			{
				-77195,
				89438,
				-5151,
				-1,
				18016
			}
		},
		{
			/* Level 42 and Below - Apostate's Offering */
			{
				-77585,
				84650,
				-5151,
				-1,
				18019
			},
			{
				-77628,
				84643,
				-5151,
				-1,
				18020
			},
			{
				-77607,
				84613,
				-5151,
				-1,
				18020
			},
			
			{
				-76603,
				85946,
				-5151,
				-1,
				18019
			},
			{
				-77606,
				85994,
				-5151,
				-1,
				18020
			},
			{
				-77638,
				85959,
				-5151,
				-1,
				18020
			},
			
			{
				-76301,
				85960,
				-5151,
				-1,
				18022
			},
			{
				-76257,
				85972,
				-5151,
				-1,
				18023
			},
			{
				-76286,
				85992,
				-5151,
				-1,
				18024
			},
			
			{
				-76281,
				84667,
				-5151,
				-1,
				18025
			},
			{
				-76291,
				84611,
				-5151,
				-1,
				18027
			},
			{
				-76257,
				84616,
				-5151,
				-1,
				18028
			},
			
			// Archers and Marksmen \\
			{
				-77419,
				85307,
				-5151,
				-1,
				18021
			},
			{
				-76952,
				85768,
				-5151,
				-1,
				18021
			},
			{
				-76477,
				85312,
				-5151,
				-1,
				18026
			},
			{
				-76942,
				84832,
				-5151,
				-1,
				18026
			}
		},
		{
			/* Level 53 and Below - Witch's Offering */
			{
				-74211,
				86494,
				-5151,
				-1,
				18029
			},
			{
				-74200,
				86449,
				-5151,
				-1,
				18030
			},
			{
				-74167,
				86464,
				-5151,
				-1,
				18030
			},
			
			{
				-75495,
				86482,
				-5151,
				-1,
				18029
			},
			{
				-75540,
				86473,
				-5151,
				-1,
				18030
			},
			{
				-75509,
				86445,
				-5151,
				-1,
				18030
			},
			
			{
				-75509,
				87775,
				-5151,
				-1,
				18032
			},
			{
				-75518,
				87826,
				-5151,
				-1,
				18033
			},
			{
				-75542,
				87780,
				-5151,
				-1,
				18034
			},
			
			{
				-74214,
				87789,
				-5151,
				-1,
				18035
			},
			{
				-74169,
				87801,
				-5151,
				-1,
				18037
			},
			{
				-74198,
				87827,
				-5151,
				-1,
				18038
			},
			
			// Archers and Marksmen \\
			{
				-75324,
				87135,
				-5151,
				-1,
				18031
			},
			{
				-74852,
				87606,
				-5151,
				-1,
				18031
			},
			{
				-74388,
				87146,
				-5151,
				-1,
				18036
			},
			{
				-74856,
				86663,
				-5151,
				-1,
				18036
			}
		},
		{
			/* Level 64 and Below - Dark Omen Offering */
			{
				-79560,
				89007,
				-5151,
				-1,
				18039
			},
			{
				-79521,
				89016,
				-5151,
				-1,
				18040
			},
			{
				-79544,
				89047,
				-5151,
				-1,
				18040
			},
			
			{
				-79552,
				87717,
				-5151,
				-1,
				18039
			},
			{
				-79552,
				87673,
				-5151,
				-1,
				18040
			},
			{
				-79510,
				87702,
				-5151,
				-1,
				18040
			},
			
			{
				-80866,
				87719,
				-5151,
				-1,
				18042
			},
			{
				-80897,
				87689,
				-5151,
				-1,
				18043
			},
			{
				-80850,
				87685,
				-5151,
				-1,
				18044
			},
			
			{
				-80848,
				89013,
				-5151,
				-1,
				18045
			},
			{
				-80887,
				89051,
				-5151,
				-1,
				18047
			},
			{
				-80891,
				89004,
				-5151,
				-1,
				18048
			},
			
			// Archers and Marksmen \\
			{
				-80205,
				87895,
				-5151,
				-1,
				18041
			},
			{
				-80674,
				88350,
				-5151,
				-1,
				18041
			},
			{
				-80209,
				88833,
				-5151,
				-1,
				18046
			},
			{
				-79743,
				88364,
				-5151,
				-1,
				18046
			}
		},
		{
			/* No Level Limit - Offering of Forbidden Path */
			{
				-80624,
				84060,
				-5151,
				-1,
				18049
			},
			{
				-80621,
				84007,
				-5151,
				-1,
				18050
			},
			{
				-80590,
				84039,
				-5151,
				-1,
				18050
			},
			
			{
				-80605,
				85349,
				-5151,
				-1,
				18049
			},
			{
				-80639,
				85363,
				-5151,
				-1,
				18050
			},
			{
				-80611,
				85385,
				-5151,
				-1,
				18050
			},
			
			{
				-79311,
				85353,
				-5151,
				-1,
				18052
			},
			{
				-79277,
				85384,
				-5151,
				-1,
				18053
			},
			{
				-79273,
				85539,
				-5151,
				-1,
				18054
			},
			
			{
				-79297,
				84054,
				-5151,
				-1,
				18055
			},
			{
				-79285,
				84006,
				-5151,
				-1,
				18057
			},
			{
				-79260,
				84040,
				-5151,
				-1,
				18058
			},
			
			// Archers and Marksmen \\
			{
				-79945,
				85171,
				-5151,
				-1,
				18051
			},
			{
				-79489,
				84707,
				-5151,
				-1,
				18051
			},
			{
				-79952,
				84222,
				-5151,
				-1,
				18056
			},
			{
				-80423,
				84703,
				-5151,
				-1,
				18056
			}
		}
	};
	
	/** The Constant FESTIVAL_DAWN_SECONDARY_SPAWNS. */
	protected static final int[][][] FESTIVAL_DAWN_SECONDARY_SPAWNS =
	{
		{
			/* 31 and Below */
			{
				-78757,
				112834,
				-4895,
				-1,
				18016
			},
			{
				-78581,
				112834,
				-4895,
				-1,
				18016
			},
			
			{
				-78822,
				112526,
				-4895,
				-1,
				18011
			},
			{
				-78822,
				113702,
				-4895,
				-1,
				18011
			},
			{
				-78822,
				113874,
				-4895,
				-1,
				18011
			},
			
			{
				-79524,
				113546,
				-4895,
				-1,
				18011
			},
			{
				-79693,
				113546,
				-4895,
				-1,
				18011
			},
			{
				-79858,
				113546,
				-4895,
				-1,
				18011
			},
			
			{
				-79545,
				112757,
				-4895,
				-1,
				18016
			},
			{
				-79545,
				112586,
				-4895,
				-1,
				18016
			},
		},
		{
			/* 42 and Below */
			{
				-75565,
				110580,
				-4895,
				-1,
				18026
			},
			{
				-75565,
				110740,
				-4895,
				-1,
				18026
			},
			
			{
				-75577,
				109776,
				-4895,
				-1,
				18021
			},
			{
				-75413,
				109776,
				-4895,
				-1,
				18021
			},
			{
				-75237,
				109776,
				-4895,
				-1,
				18021
			},
			
			{
				-76274,
				109468,
				-4895,
				-1,
				18021
			},
			{
				-76274,
				109635,
				-4895,
				-1,
				18021
			},
			{
				-76274,
				109795,
				-4895,
				-1,
				18021
			},
			
			{
				-76351,
				110500,
				-4895,
				-1,
				18056
			},
			{
				-76528,
				110500,
				-4895,
				-1,
				18056
			},
		},
		{
			/* 53 and Below */
			{
				-74191,
				111527,
				-4895,
				-1,
				18036
			},
			{
				-74191,
				111362,
				-4895,
				-1,
				18036
			},
			
			{
				-73495,
				111611,
				-4895,
				-1,
				18031
			},
			{
				-73327,
				111611,
				-4895,
				-1,
				18031
			},
			{
				-73154,
				111611,
				-4895,
				-1,
				18031
			},
			
			{
				-73473,
				112301,
				-4895,
				-1,
				18031
			},
			{
				-73473,
				112475,
				-4895,
				-1,
				18031
			},
			{
				-73473,
				112649,
				-4895,
				-1,
				18031
			},
			
			{
				-74270,
				112326,
				-4895,
				-1,
				18036
			},
			{
				-74443,
				112326,
				-4895,
				-1,
				18036
			},
		},
		{
			/* 64 and Below */
			{
				-75738,
				113439,
				-4895,
				-1,
				18046
			},
			{
				-75571,
				113439,
				-4895,
				-1,
				18046
			},
			
			{
				-75824,
				114141,
				-4895,
				-1,
				18041
			},
			{
				-75824,
				114309,
				-4895,
				-1,
				18041
			},
			{
				-75824,
				114477,
				-4895,
				-1,
				18041
			},
			
			{
				-76513,
				114158,
				-4895,
				-1,
				18041
			},
			{
				-76683,
				114158,
				-4895,
				-1,
				18041
			},
			{
				-76857,
				114158,
				-4895,
				-1,
				18041
			},
			
			{
				-76535,
				113357,
				-4895,
				-1,
				18056
			},
			{
				-76535,
				113190,
				-4895,
				-1,
				18056
			},
		},
		{
			/* No Level Limit */
			{
				-79350,
				109894,
				-4895,
				-1,
				18056
			},
			{
				-79534,
				109894,
				-4895,
				-1,
				18056
			},
			
			{
				-79285,
				109187,
				-4895,
				-1,
				18051
			},
			{
				-79285,
				109019,
				-4895,
				-1,
				18051
			},
			{
				-79285,
				108860,
				-4895,
				-1,
				18051
			},
			
			{
				-78587,
				109172,
				-4895,
				-1,
				18051
			},
			{
				-78415,
				109172,
				-4895,
				-1,
				18051
			},
			{
				-78249,
				109172,
				-4895,
				-1,
				18051
			},
			
			{
				-78575,
				109961,
				-4895,
				-1,
				18056
			},
			{
				-78575,
				110130,
				-4895,
				-1,
				18056
			},
		}
	};
	
	/** The Constant FESTIVAL_DUSK_SECONDARY_SPAWNS. */
	protected static final int[][][] FESTIVAL_DUSK_SECONDARY_SPAWNS =
	{
		{
			/* 31 and Below */
			{
				-76844,
				89304,
				-5151,
				-1,
				18011
			},
			{
				-76844,
				89479,
				-5151,
				-1,
				18011
			},
			{
				-76844,
				89649,
				-5151,
				-1,
				18011
			},
			
			{
				-77544,
				89326,
				-5151,
				-1,
				18011
			},
			{
				-77716,
				89326,
				-5151,
				-1,
				18011
			},
			{
				-77881,
				89326,
				-5151,
				-1,
				18011
			},
			
			{
				-77561,
				88530,
				-5151,
				-1,
				18016
			},
			{
				-77561,
				88364,
				-5151,
				-1,
				18016
			},
			
			{
				-76762,
				88615,
				-5151,
				-1,
				18016
			},
			{
				-76594,
				88615,
				-5151,
				-1,
				18016
			},
		},
		{
			/* 42 and Below */
			{
				-77307,
				84969,
				-5151,
				-1,
				18021
			},
			{
				-77307,
				84795,
				-5151,
				-1,
				18021
			},
			{
				-77307,
				84623,
				-5151,
				-1,
				18021
			},
			
			{
				-76614,
				84944,
				-5151,
				-1,
				18021
			},
			{
				-76433,
				84944,
				-5151,
				-1,
				18021
			},
			{
				-7626 - 1,
				84944,
				-5151,
				-1,
				18021
			},
			
			{
				-76594,
				85745,
				-5151,
				-1,
				18026
			},
			{
				-76594,
				85910,
				-5151,
				-1,
				18026
			},
			
			{
				-77384,
				85660,
				-5151,
				-1,
				18026
			},
			{
				-77555,
				85660,
				-5151,
				-1,
				18026
			},
		},
		{
			/* 53 and Below */
			{
				-74517,
				86782,
				-5151,
				-1,
				18031
			},
			{
				-74344,
				86782,
				-5151,
				-1,
				18031
			},
			{
				-74185,
				86782,
				-5151,
				-1,
				18031
			},
			
			{
				-74496,
				87464,
				-5151,
				-1,
				18031
			},
			{
				-74496,
				87636,
				-5151,
				-1,
				18031
			},
			{
				-74496,
				87815,
				-5151,
				-1,
				18031
			},
			
			{
				-75298,
				87497,
				-5151,
				-1,
				18036
			},
			{
				-75460,
				87497,
				-5151,
				-1,
				18036
			},
			
			{
				-75219,
				86712,
				-5151,
				-1,
				18036
			},
			{
				-75219,
				86531,
				-5151,
				-1,
				18036
			},
		},
		{
			/* 64 and Below */
			{
				-79851,
				88703,
				-5151,
				-1,
				18041
			},
			{
				-79851,
				88868,
				-5151,
				-1,
				18041
			},
			{
				-79851,
				89040,
				-5151,
				-1,
				18041
			},
			
			{
				-80548,
				88722,
				-5151,
				-1,
				18041
			},
			{
				-80711,
				88722,
				-5151,
				-1,
				18041
			},
			{
				-80883,
				88722,
				-5151,
				-1,
				18041
			},
			
			{
				-80565,
				87916,
				-5151,
				-1,
				18046
			},
			{
				-80565,
				87752,
				-5151,
				-1,
				18046
			},
			
			{
				-79779,
				87996,
				-5151,
				-1,
				18046
			},
			{
				-79613,
				87996,
				-5151,
				-1,
				18046
			},
		},
		{
			/* No Level Limit */
			{
				-79271,
				84330,
				-5151,
				-1,
				18051
			},
			{
				-79448,
				84330,
				-5151,
				-1,
				18051
			},
			{
				-79601,
				84330,
				-5151,
				-1,
				18051
			},
			
			{
				-80311,
				84367,
				-5151,
				-1,
				18051
			},
			{
				-80311,
				84196,
				-5151,
				-1,
				18051
			},
			{
				-80311,
				84015,
				-5151,
				-1,
				18051
			},
			
			{
				-80556,
				85049,
				-5151,
				-1,
				18056
			},
			{
				-80384,
				85049,
				-5151,
				-1,
				18056
			},
			
			{
				-79598,
				85127,
				-5151,
				-1,
				18056
			},
			{
				-79598,
				85303,
				-5151,
				-1,
				18056
			},
		}
	};
	
	/** The Constant FESTIVAL_DAWN_CHEST_SPAWNS. */
	protected static final int[][][] FESTIVAL_DAWN_CHEST_SPAWNS =
	{
		{
			/* Level 31 and Below */
			{
				-78999,
				112957,
				-4927,
				-1,
				18109
			},
			{
				-79153,
				112873,
				-4927,
				-1,
				18109
			},
			{
				-79256,
				112873,
				-4927,
				-1,
				18109
			},
			{
				-79368,
				112957,
				-4927,
				-1,
				18109
			},
			
			{
				-79481,
				113124,
				-4927,
				-1,
				18109
			},
			{
				-79481,
				113275,
				-4927,
				-1,
				18109
			},
			
			{
				-79364,
				113398,
				-4927,
				-1,
				18109
			},
			{
				-79213,
				113500,
				-4927,
				-1,
				18109
			},
			{
				-79099,
				113500,
				-4927,
				-1,
				18109
			},
			{
				-78960,
				113398,
				-4927,
				-1,
				18109
			},
			
			{
				-78882,
				113235,
				-4927,
				-1,
				18109
			},
			{
				-78882,
				113099,
				-4927,
				-1,
				18109
			},
		},
		{
			/* Level 42 and Below */
			{
				-76119,
				110383,
				-4927,
				-1,
				18110
			},
			{
				-75980,
				110442,
				-4927,
				-1,
				18110
			},
			{
				-75848,
				110442,
				-4927,
				-1,
				18110
			},
			{
				-75720,
				110383,
				-4927,
				-1,
				18110
			},
			
			{
				-75625,
				110195,
				-4927,
				-1,
				18110
			},
			{
				-75625,
				110063,
				-4927,
				-1,
				18110
			},
			
			{
				-75722,
				109908,
				-4927,
				-1,
				18110
			},
			{
				-75863,
				109832,
				-4927,
				-1,
				18110
			},
			{
				-75989,
				109832,
				-4927,
				-1,
				18110
			},
			{
				-76130,
				109908,
				-4927,
				-1,
				18110
			},
			
			{
				-76230,
				110079,
				-4927,
				-1,
				18110
			},
			{
				-76230,
				110215,
				-4927,
				-1,
				18110
			},
		},
		{
			/* Level 53 and Below */
			{
				-74055,
				111781,
				-4927,
				-1,
				18111
			},
			{
				-74144,
				111938,
				-4927,
				-1,
				18111
			},
			{
				-74144,
				112075,
				-4927,
				-1,
				18111
			},
			{
				-74055,
				112173,
				-4927,
				-1,
				18111
			},
			
			{
				-73885,
				112289,
				-4927,
				-1,
				18111
			},
			{
				-73756,
				112289,
				-4927,
				-1,
				18111
			},
			
			{
				-73574,
				112141,
				-4927,
				-1,
				18111
			},
			{
				-73511,
				112040,
				-4927,
				-1,
				18111
			},
			{
				-73511,
				111912,
				-4927,
				-1,
				18111
			},
			{
				-73574,
				111772,
				-4927,
				-1,
				18111
			},
			
			{
				-73767,
				111669,
				-4927,
				-1,
				18111
			},
			{
				-73899,
				111669,
				-4927,
				-1,
				18111
			},
		},
		{
			/* Level 64 and Below */
			{
				-76008,
				113566,
				-4927,
				-1,
				18112
			},
			{
				-76159,
				113485,
				-4927,
				-1,
				18112
			},
			{
				-76267,
				113485,
				-4927,
				-1,
				18112
			},
			{
				-76386,
				113566,
				-4927,
				-1,
				18112
			},
			
			{
				-76482,
				113748,
				-4927,
				-1,
				18112
			},
			{
				-76482,
				113885,
				-4927,
				-1,
				18112
			},
			
			{
				-76371,
				114029,
				-4927,
				-1,
				18112
			},
			{
				-76220,
				114118,
				-4927,
				-1,
				18112
			},
			{
				-76092,
				114118,
				-4927,
				-1,
				18112
			},
			{
				-75975,
				114029,
				-4927,
				-1,
				18112
			},
			
			{
				-75861,
				11385 - 1,
				-4927,
				-1,
				18112
			},
			{
				-75861,
				113713,
				-4927,
				-1,
				18112
			},
		},
		{
			/* No Level Limit */
			{
				-79100,
				109782,
				-4927,
				-1,
				18113
			},
			{
				-78962,
				109853,
				-4927,
				-1,
				18113
			},
			{
				-78851,
				109853,
				-4927,
				-1,
				18113
			},
			{
				-78721,
				109782,
				-4927,
				-1,
				18113
			},
			
			{
				-78615,
				109596,
				-4927,
				-1,
				18113
			},
			{
				-78615,
				109453,
				-4927,
				-1,
				18113
			},
			
			{
				-78746,
				109300,
				-4927,
				-1,
				18113
			},
			{
				-78881,
				109203,
				-4927,
				-1,
				18113
			},
			{
				-79027,
				109203,
				-4927,
				-1,
				18113
			},
			{
				-79159,
				109300,
				-4927,
				-1,
				18113
			},
			
			{
				-79240,
				109480,
				-4927,
				-1,
				18113
			},
			{
				-79240,
				109615,
				-4927,
				-1,
				18113
			},
		}
	};
	
	/** The Constant FESTIVAL_DUSK_CHEST_SPAWNS. */
	protected static final int[][][] FESTIVAL_DUSK_CHEST_SPAWNS =
	{
		{
			/* Level 31 and Below */
			{
				-77016,
				88726,
				-5183,
				-1,
				18114
			},
			{
				-77136,
				88646,
				-5183,
				-1,
				18114
			},
			{
				-77247,
				88646,
				-5183,
				-1,
				18114
			},
			{
				-77380,
				88726,
				-5183,
				-1,
				18114
			},
			
			{
				-77512,
				88883,
				-5183,
				-1,
				18114
			},
			{
				-77512,
				89053,
				-5183,
				-1,
				18114
			},
			
			{
				-77378,
				89287,
				-5183,
				-1,
				18114
			},
			{
				-77254,
				89238,
				-5183,
				-1,
				18114
			},
			{
				-77095,
				89238,
				-5183,
				-1,
				18114
			},
			{
				-76996,
				89287,
				-5183,
				-1,
				18114
			},
			
			{
				-76901,
				89025,
				-5183,
				-1,
				18114
			},
			{
				-76901,
				88891,
				-5183,
				-1,
				18114
			},
		},
		{
			/* Level 42 and Below */
			{
				-77128,
				85553,
				-5183,
				-1,
				18115
			},
			{
				-77036,
				85594,
				-5183,
				-1,
				18115
			},
			{
				-76919,
				85594,
				-5183,
				-1,
				18115
			},
			{
				-76755,
				85553,
				-5183,
				-1,
				18115
			},
			
			{
				-76635,
				85392,
				-5183,
				-1,
				18115
			},
			{
				-76635,
				85216,
				-5183,
				-1,
				18115
			},
			
			{
				-76761,
				85025,
				-5183,
				-1,
				18115
			},
			{
				-76908,
				85004,
				-5183,
				-1,
				18115
			},
			{
				-77041,
				85004,
				-5183,
				-1,
				18115
			},
			{
				-77138,
				85025,
				-5183,
				-1,
				18115
			},
			
			{
				-77268,
				85219,
				-5183,
				-1,
				18115
			},
			{
				-77268,
				85410,
				-5183,
				-1,
				18115
			},
		},
		{
			/* Level 53 and Below */
			{
				-75150,
				87303,
				-5183,
				-1,
				18116
			},
			{
				-75150,
				87175,
				-5183,
				-1,
				18116
			},
			{
				-75150,
				87175,
				-5183,
				-1,
				18116
			},
			{
				-75150,
				87303,
				-5183,
				-1,
				18116
			},
			
			{
				-74943,
				87433,
				-5183,
				-1,
				18116
			},
			{
				-74767,
				87433,
				-5183,
				-1,
				18116
			},
			
			{
				-74556,
				87306,
				-5183,
				-1,
				18116
			},
			{
				-74556,
				87184,
				-5183,
				-1,
				18116
			},
			{
				-74556,
				87184,
				-5183,
				-1,
				18116
			},
			{
				-74556,
				87306,
				-5183,
				-1,
				18116
			},
			
			{
				-74757,
				86830,
				-5183,
				-1,
				18116
			},
			{
				-74927,
				86830,
				-5183,
				-1,
				18116
			},
		},
		{
			/* Level 64 and Below */
			{
				-80010,
				88128,
				-5183,
				-1,
				18117
			},
			{
				-80113,
				88066,
				-5183,
				-1,
				18117
			},
			{
				-80220,
				88066,
				-5183,
				-1,
				18117
			},
			{
				-80359,
				88128,
				-5183,
				-1,
				18117
			},
			
			{
				-80467,
				88267,
				-5183,
				-1,
				18117
			},
			{
				-80467,
				88436,
				-5183,
				-1,
				18117
			},
			
			{
				-80381,
				88639,
				-5183,
				-1,
				18117
			},
			{
				-80278,
				88577,
				-5183,
				-1,
				18117
			},
			{
				-80142,
				88577,
				-5183,
				-1,
				18117
			},
			{
				-80028,
				88639,
				-5183,
				-1,
				18117
			},
			
			{
				-79915,
				88466,
				-5183,
				-1,
				18117
			},
			{
				-79915,
				88322,
				-5183,
				-1,
				18117
			},
		},
		{
			/* No Level Limit */
			{
				-80153,
				84947,
				-5183,
				-1,
				18118
			},
			{
				-80003,
				84962,
				-5183,
				-1,
				18118
			},
			{
				-79848,
				84962,
				-5183,
				-1,
				18118
			},
			{
				-79742,
				84947,
				-5183,
				-1,
				18118
			},
			
			{
				-79668,
				84772,
				-5183,
				-1,
				18118
			},
			{
				-79668,
				84619,
				-5183,
				-1,
				18118
			},
			
			{
				-79772,
				84471,
				-5183,
				-1,
				18118
			},
			{
				-79888,
				84414,
				-5183,
				-1,
				18118
			},
			{
				-80023,
				84414,
				-5183,
				-1,
				18118
			},
			{
				-80166,
				84471,
				-5183,
				-1,
				18118
			},
			
			{
				-80253,
				84600,
				-5183,
				-1,
				18118
			},
			{
				-80253,
				84780,
				-5183,
				-1,
				18118
			},
		}
	};
	protected FestivalManager _managerInstance;
	protected ScheduledFuture<?> _managerScheduledTask;
	protected int _signsCycle = SevenSigns.getInstance().getCurrentCycle();
	protected int _festivalCycle;
	protected long _nextFestivalCycleStart;
	protected long _nextFestivalStart;
	protected boolean _festivalInitialized;
	protected boolean _festivalInProgress;
	protected List<Integer> _accumulatedBonuses; // The total bonus available (in Ancient Adena)
	private NpcInstance _dawnChatGuide;
	private NpcInstance _duskChatGuide;
	protected Map<Integer, List<PlayerInstance>> _dawnFestivalParticipants;
	protected Map<Integer, List<PlayerInstance>> _duskFestivalParticipants;
	protected Map<Integer, List<PlayerInstance>> _dawnPreviousParticipants;
	protected Map<Integer, List<PlayerInstance>> _duskPreviousParticipants;
	private Map<Integer, Integer> _dawnFestivalScores;
	private Map<Integer, Integer> _duskFestivalScores;
	private Map<Integer, Map<Integer, StatSet>> _festivalData;
	
	/**
	 * Instantiates a new seven signs festival.
	 */
	public SevenSignsFestival()
	{
		_accumulatedBonuses = new ArrayList<>();
		_dawnFestivalParticipants = new HashMap<>();
		_dawnPreviousParticipants = new HashMap<>();
		_dawnFestivalScores = new HashMap<>();
		_duskFestivalParticipants = new HashMap<>();
		_duskPreviousParticipants = new HashMap<>();
		_duskFestivalScores = new HashMap<>();
		_festivalData = new HashMap<>();
		restoreFestivalData();
		ThreadPool.scheduleAtFixedRate(new RestoreStatus(), 7200000, 10800000);
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			LOGGER.info("SevenSignsFestival: Initialization bypassed due to Seal Validation in effect.");
			return;
		}
		
		Spawn.addSpawnListener(this);
		startFestivalManager();
	}
	
	/**
	 * Returns the associated name (level range) to a given festival ID.
	 * @param festivalID the festival id
	 * @return String festivalName
	 */
	public static final String getFestivalName(int festivalID)
	{
		String festivalName;
		
		switch (festivalID)
		{
			case FESTIVAL_LEVEL_MAX_FOR_31:
			{
				festivalName = "Level 31 or lower";
				break;
			}
			case FESTIVAL_LEVEL_MAX_FOR_42:
			{
				festivalName = "Level 42 or lower";
				break;
			}
			case FESTIVAL_LEVEL_MAX_FOR_53:
			{
				festivalName = "Level 53 or lower";
				break;
			}
			case FESTIVAL_LEVEL_MAX_FOR_64:
			{
				festivalName = "Level 64 or lower";
				break;
			}
			default:
			{
				festivalName = "No Level Limit";
				break;
			}
		}
		return festivalName;
	}
	
	/**
	 * Returns the maximum allowed player level for the given festival type.
	 * @param festivalId the festival id
	 * @return int maxLevel
	 */
	public static final int getMaxLevelForFestival(int festivalId)
	{
		int maxLevel = ExperienceData.getInstance().getMaxLevel() - 1;
		
		switch (festivalId)
		{
			case FESTIVAL_LEVEL_MAX_FOR_31:
			{
				maxLevel = 31;
				break;
			}
			case FESTIVAL_LEVEL_MAX_FOR_42:
			{
				maxLevel = 42;
				break;
			}
			case FESTIVAL_LEVEL_MAX_FOR_53:
			{
				maxLevel = 53;
				break;
			}
			case FESTIVAL_LEVEL_MAX_FOR_64:
			{
				maxLevel = 64;
				break;
			}
		}
		return maxLevel;
	}
	
	/**
	 * Returns true if the monster ID given is of an archer/marksman type.
	 * @param npcId the npc id
	 * @return boolean isArcher
	 */
	protected static final boolean isFestivalArcher(int npcId)
	{
		if ((npcId < 18009) || (npcId > 18108))
		{
			return false;
		}
		
		final int identifier = npcId % 10;
		return (identifier == 4) || (identifier == 9);
	}
	
	/**
	 * Returns true if the monster ID given is a festival chest.
	 * @param npcId the npc id
	 * @return boolean isChest
	 */
	protected static final boolean isFestivalChest(int npcId)
	{
		return (npcId < 18109) || (npcId > 18118);
	}
	
	/**
	 * Primarily used to terminate the Festival Manager, when the Seven Signs period changes.
	 * @return ScheduledFuture festManagerScheduler
	 */
	protected final ScheduledFuture<?> getFestivalManagerSchedule()
	{
		if (_managerScheduledTask == null)
		{
			startFestivalManager();
		}
		return _managerScheduledTask;
	}
	
	/**
	 * Used to start the Festival Manager, if the current period is not Seal Validation.
	 */
	protected void startFestivalManager()
	{
		// Start the Festival Manager for the first time after the server has started at the specified time, then invoke it automatically after every cycle.
		final FestivalManager fm = new FestivalManager();
		setNextFestivalStart(Config.ALT_FESTIVAL_MANAGER_START + FESTIVAL_SIGNUP_TIME);
		_managerScheduledTask = ThreadPool.scheduleAtFixedRate(fm, Config.ALT_FESTIVAL_MANAGER_START, Config.ALT_FESTIVAL_CYCLE_LENGTH);
		
		LOGGER.info("SevenSignsFestival: The first Festival of Darkness cycle begins in " + (Config.ALT_FESTIVAL_MANAGER_START / 60000) + " minute(s).");
	}
	
	/**
	 * Restores saved festival data, basic settings from the properties file and past high score data from the database.
	 */
	protected void restoreFestivalData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT festivalId, cabal, cycle, date, score, members FROM seven_signs_festival");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int festivalCycle = rset.getInt("cycle");
				int festivalId = rset.getInt("festivalId");
				final String cabal = rset.getString("cabal");
				final StatSet festivalDat = new StatSet();
				festivalDat.set("festivalId", festivalId);
				festivalDat.set("cabal", cabal);
				festivalDat.set("cycle", festivalCycle);
				festivalDat.set("date", rset.getString("date"));
				festivalDat.set("score", rset.getInt("score"));
				festivalDat.set("members", rset.getString("members"));
				if (cabal.equals("dawn"))
				{
					festivalId += FESTIVAL_COUNT;
				}
				
				Map<Integer, StatSet> tempData = _festivalData.get(festivalCycle);
				if (tempData == null)
				{
					tempData = new HashMap<>();
				}
				
				tempData.put(festivalId, festivalDat);
				_festivalData.put(festivalCycle, tempData);
			}
			
			rset.close();
			statement.close();
			
			rset = null;
			statement = null;
			String query = "SELECT festival_cycle, ";
			for (int i = 0; i < (FESTIVAL_COUNT - 1); i++)
			{
				query += "accumulated_bonus" + i + ", ";
			}
			query += "accumulated_bonus" + (FESTIVAL_COUNT - 1) + " ";
			query += "FROM seven_signs_status WHERE id=0";
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				_festivalCycle = rset.getInt("festival_cycle");
				for (int i = 0; i < FESTIVAL_COUNT; i++)
				{
					_accumulatedBonuses.add(i, rset.getInt("accumulated_bonus" + i));
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("SevenSignsFestival: Failed to load configuration " + e);
		}
	}
	
	/**
	 * Stores current festival data, basic settings to the properties file and past high score data to the database. If updateSettings = true, then all Seven Signs data is updated in the database.
	 * @param updateSettings the update settings
	 */
	public void saveFestivalData(boolean updateSettings)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = null;
			for (Map<Integer, StatSet> currCycleData : _festivalData.values())
			{
				for (StatSet festivalDat : currCycleData.values())
				{
					final int festivalCycle = festivalDat.getInt("cycle");
					final int festivalId = festivalDat.getInt("festivalId");
					final String cabal = festivalDat.getString("cabal");
					
					// Try to update an existing record.
					statement = con.prepareStatement("UPDATE seven_signs_festival SET date=?, score=?, members=? WHERE cycle=? AND cabal=? AND festivalId=?");
					statement.setLong(1, Long.parseLong(festivalDat.getString("date")));
					statement.setInt(2, festivalDat.getInt("score"));
					statement.setString(3, festivalDat.getString("members"));
					statement.setInt(4, festivalCycle);
					statement.setString(5, cabal);
					statement.setInt(6, festivalId);
					
					// If there was no record to update, assume it doesn't exist and add a new one, otherwise continue with the next record to store.
					if (statement.executeUpdate() > 0)
					{
						statement.close();
						continue;
					}
					
					statement.close();
					
					statement = con.prepareStatement("INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members) VALUES (?,?,?,?,?,?)");
					statement.setInt(1, festivalId);
					statement.setString(2, cabal);
					statement.setInt(3, festivalCycle);
					statement.setLong(4, Long.parseLong(festivalDat.getString("date")));
					statement.setInt(5, festivalDat.getInt("score"));
					statement.setString(6, festivalDat.getString("members"));
					statement.execute();
					statement.close();
				}
			}
			
			// Updates Seven Signs DB data also, so call only if really necessary.
			if (updateSettings)
			{
				SevenSigns.getInstance().saveSevenSignsData(null, true);
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("SevenSignsFestival: Failed to save configuration " + e);
		}
	}
	
	/**
	 * If a clan member is a member of the highest-ranked party in the Festival of Darkness, 100 points are added per member.
	 */
	protected void rewardHighRanked()
	{
		String[] partyMembers;
		StatSet overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_FOR_31);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}
		
		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_FOR_42);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}
		
		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_FOR_53);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}
		
		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_FOR_64);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}
		
		overallData = getOverallHighestScoreData(FESTIVAL_LEVEL_MAX_FOR_NONE);
		if (overallData != null)
		{
			partyMembers = overallData.getString("members").split(",");
			for (String partyMemberName : partyMembers)
			{
				addReputationPointsForPartyMemberClan(partyMemberName);
			}
		}
	}
	
	/**
	 * Adds the reputation points for party member clan.
	 * @param partyMemberName the party member name
	 */
	private void addReputationPointsForPartyMemberClan(String partyMemberName)
	{
		final PlayerInstance player = World.getInstance().getPlayer(partyMemberName);
		if (player != null)
		{
			if (player.getClan() != null)
			{
				player.getClan().setReputationScore(player.getClan().getReputationScore() + 100, true);
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_AN_ACTIVE_MEMBER_OF_THE_HIGHEST_RANKED_PARTY_IN_THE_FESTIVAL_OF_DARKNESS_S2_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE);
				sm.addString(partyMemberName);
				sm.addNumber(100);
				player.getClan().broadcastToOnlineMembers(sm);
			}
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement(GET_CLAN_NAME);
				statement.setString(1, partyMemberName);
				final ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					final String clanName = rset.getString("clan_name");
					if (clanName != null)
					{
						final Clan clan = ClanTable.getInstance().getClanByName(clanName);
						if (clan != null)
						{
							clan.setReputationScore(clan.getReputationScore() + 100, true);
							clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
							final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_AN_ACTIVE_MEMBER_OF_THE_HIGHEST_RANKED_PARTY_IN_THE_FESTIVAL_OF_DARKNESS_S2_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_S_REPUTATION_SCORE);
							sm.addString(partyMemberName);
							sm.addNumber(100);
							clan.broadcastToOnlineMembers(sm);
						}
					}
				}
				
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("could not get clan name of " + partyMemberName + ": " + e);
			}
		}
	}
	
	/**
	 * Used to reset all festival data at the beginning of a new quest event period.
	 * @param updateSettings the update settings
	 */
	protected void resetFestivalData(boolean updateSettings)
	{
		_festivalCycle = 0;
		_signsCycle = SevenSigns.getInstance().getCurrentCycle();
		
		// Set all accumulated bonuses back to 0.
		for (int i = 0; i < FESTIVAL_COUNT; i++)
		{
			_accumulatedBonuses.set(i, 0);
		}
		
		_dawnFestivalParticipants.clear();
		_dawnPreviousParticipants.clear();
		_dawnFestivalScores.clear();
		
		_duskFestivalParticipants.clear();
		_duskPreviousParticipants.clear();
		_duskFestivalScores.clear();
		
		// Set up a new data set for the current cycle of festivals
		final Map<Integer, StatSet> newData = new HashMap<>();
		for (int i = 0; i < (FESTIVAL_COUNT * 2); i++)
		{
			int festivalId = i;
			if (i >= FESTIVAL_COUNT)
			{
				festivalId -= FESTIVAL_COUNT;
			}
			
			// Create a new StatSet with "default" data for Dusk
			final StatSet tempStats = new StatSet();
			tempStats.set("festivalId", festivalId);
			tempStats.set("cycle", _signsCycle);
			tempStats.set("date", "0");
			tempStats.set("score", 0);
			tempStats.set("members", "");
			if (i >= FESTIVAL_COUNT)
			{
				tempStats.set("cabal", SevenSigns.getCabalShortName(SevenSigns.CABAL_DAWN));
			}
			else
			{
				tempStats.set("cabal", SevenSigns.getCabalShortName(SevenSigns.CABAL_DUSK));
			}
			
			newData.put(i, tempStats);
		}
		
		// Add the newly created cycle data to the existing festival data, and subsequently save it to the database.
		_festivalData.put(_signsCycle, newData);
		
		saveFestivalData(updateSettings);
		
		// Remove any unused blood offerings from online players.
		for (PlayerInstance onlinePlayer : World.getInstance().getAllPlayers())
		{
			try
			{
				final ItemInstance bloodOfferings = onlinePlayer.getInventory().getItemByItemId(FESTIVAL_OFFERING_ID);
				if (bloodOfferings != null)
				{
					onlinePlayer.destroyItem("SevenSigns", bloodOfferings, null, false);
				}
			}
			catch (NullPointerException e)
			{
			}
		}
		
		LOGGER.info("SevenSignsFestival: Reinitialized engine for next competition period.");
	}
	
	/**
	 * Gets the current festival cycle.
	 * @return the current festival cycle
	 */
	public int getCurrentFestivalCycle()
	{
		return _festivalCycle;
	}
	
	/**
	 * Checks if is festival initialized.
	 * @return true, if is festival initialized
	 */
	public boolean isFestivalInitialized()
	{
		return _festivalInitialized;
	}
	
	/**
	 * Checks if is festival in progress.
	 * @return true, if is festival in progress
	 */
	public boolean isFestivalInProgress()
	{
		return _festivalInProgress;
	}
	
	/**
	 * Sets the next cycle start.
	 */
	public void setNextCycleStart()
	{
		_nextFestivalCycleStart = System.currentTimeMillis() + Config.ALT_FESTIVAL_CYCLE_LENGTH;
	}
	
	/**
	 * Sets the next festival start.
	 * @param milliFromNow the new next festival start
	 */
	public void setNextFestivalStart(long milliFromNow)
	{
		_nextFestivalStart = System.currentTimeMillis() + milliFromNow;
	}
	
	/**
	 * Gets the mins to next cycle.
	 * @return the mins to next cycle
	 */
	public int getMinsToNextCycle()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			return -1;
		}
		return Math.round((_nextFestivalCycleStart - System.currentTimeMillis()) / 60000);
	}
	
	/**
	 * Gets the mins to next festival.
	 * @return the mins to next festival
	 */
	public int getMinsToNextFestival()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			return -1;
		}
		return Math.round((_nextFestivalStart - System.currentTimeMillis()) / 60000) + 1;
	}
	
	/**
	 * Gets the time to next festival start.
	 * @return the time to next festival start
	 */
	public String getTimeToNextFestivalStart()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			return "<font color=\"FF0000\">This is the Seal Validation period. Festivals will resume next week.</font>";
		}
		return "<font color=\"FF0000\">The next festival will begin in " + getMinsToNextFestival() + " minute(s).</font>";
	}
	
	/**
	 * Returns the current festival ID and oracle ID that the specified player is in, but will return the default of {-1, -1} if the player is not found as a participant.
	 * @param player the player
	 * @return int[] playerFestivalInfo
	 */
	public int[] getFestivalForPlayer(PlayerInstance player)
	{
		final int[] playerFestivalInfo =
		{
			-1,
			-1
		};
		int festivalId = 0;
		
		while (festivalId < FESTIVAL_COUNT)
		{
			List<PlayerInstance> participants = _dawnFestivalParticipants.get(festivalId);
			
			// If there are no participants in this festival, move on to the next.
			if ((participants != null) && participants.contains(player))
			{
				playerFestivalInfo[0] = SevenSigns.CABAL_DAWN;
				playerFestivalInfo[1] = festivalId;
				return playerFestivalInfo;
			}
			
			festivalId++;
			
			participants = _duskFestivalParticipants.get(festivalId);
			if ((participants != null) && participants.contains(player))
			{
				playerFestivalInfo[0] = SevenSigns.CABAL_DUSK;
				playerFestivalInfo[1] = festivalId;
				return playerFestivalInfo;
			}
			
			festivalId++;
		}
		
		// Return default data if the player is not found as a participant.
		return playerFestivalInfo;
	}
	
	/**
	 * Checks if is player participant.
	 * @param player the player
	 * @return true, if is player participant
	 */
	public boolean isPlayerParticipant(PlayerInstance player)
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			return false;
		}
		
		if (_managerInstance == null)
		{
			return false;
		}
		
		for (List<PlayerInstance> participants : _dawnFestivalParticipants.values())
		{
			if (participants.contains(player))
			{
				return true;
			}
		}
		
		for (List<PlayerInstance> participants : _duskFestivalParticipants.values())
		{
			if (participants.contains(player))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gets the participants.
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @return the participants
	 */
	public List<PlayerInstance> getParticipants(int oracle, int festivalId)
	{
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			return _dawnFestivalParticipants.get(festivalId);
		}
		return _duskFestivalParticipants.get(festivalId);
	}
	
	/**
	 * Gets the previous participants.
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @return the previous participants
	 */
	public List<PlayerInstance> getPreviousParticipants(int oracle, int festivalId)
	{
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			return _dawnPreviousParticipants.get(festivalId);
		}
		return _duskPreviousParticipants.get(festivalId);
	}
	
	/**
	 * Sets the participants.
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @param festivalParty the festival party
	 */
	public void setParticipants(int oracle, int festivalId, Party festivalParty)
	{
		List<PlayerInstance> participants = new ArrayList<>();
		if (festivalParty != null)
		{
			participants = festivalParty.getPartyMembers();
		}
		
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			_dawnFestivalParticipants.put(festivalId, participants);
		}
		else
		{
			_duskFestivalParticipants.put(festivalId, participants);
		}
	}
	
	/**
	 * Update participants.
	 * @param player the player
	 * @param festivalParty the festival party
	 */
	public void updateParticipants(PlayerInstance player, Party festivalParty)
	{
		if (!isPlayerParticipant(player))
		{
			return;
		}
		
		final int[] playerFestInfo = getFestivalForPlayer(player);
		final int oracle = playerFestInfo[0];
		final int festivalId = playerFestInfo[1];
		if (festivalId > -1)
		{
			if (_festivalInitialized)
			{
				final DarknessFestival festivalInst = _managerInstance.getFestivalInstance(oracle, festivalId);
				if (festivalParty == null)
				{
					for (PlayerInstance partyMember : getParticipants(oracle, festivalId))
					{
						festivalInst.relocatePlayer(partyMember, true);
					}
				}
				else
				{
					festivalInst.relocatePlayer(player, true);
				}
			}
			
			setParticipants(oracle, festivalId, festivalParty);
		}
	}
	
	/**
	 * Gets the final score.
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @return the final score
	 */
	public int getFinalScore(int oracle, int festivalId)
	{
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			return _dawnFestivalScores.get(festivalId);
		}
		return _duskFestivalScores.get(festivalId);
	}
	
	/**
	 * Gets the highest score.
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @return the highest score
	 */
	public int getHighestScore(int oracle, int festivalId)
	{
		return getHighestScoreData(oracle, festivalId).getInt("score");
	}
	
	/**
	 * Returns a stats set containing the highest score <b>this cycle</b> for the the specified cabal and associated festival ID.
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @return StatSet festivalDat
	 */
	public StatSet getHighestScoreData(int oracle, int festivalId)
	{
		int offsetId = festivalId;
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			offsetId += 5;
		}
		
		// Attempt to retrieve existing score data (if found), otherwise create a new blank data set and display a console warning.
		StatSet currData = null;
		if ((_festivalData != null) && (_festivalData.get(_signsCycle) != null))
		{
			currData = _festivalData.get(_signsCycle).get(offsetId);
		}
		else
		{
			currData = new StatSet();
			currData.set("score", 0);
			currData.set("members", "");
		}
		
		return currData;
	}
	
	/**
	 * Returns a stats set containing the highest ever recorded score data for the specified festival.
	 * @param festivalId the festival id
	 * @return StatSet result
	 */
	public StatSet getOverallHighestScoreData(int festivalId)
	{
		StatSet result = null;
		int highestScore = 0;
		for (Map<Integer, StatSet> currCycleData : _festivalData.values())
		{
			for (StatSet currFestData : currCycleData.values())
			{
				final int currFestID = currFestData.getInt("festivalId");
				final int festivalScore = currFestData.getInt("score");
				if (currFestID != festivalId)
				{
					continue;
				}
				
				if (festivalScore > highestScore)
				{
					highestScore = festivalScore;
					result = currFestData;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Set the final score details for the last participants of the specified festival data. Returns <b>true</b> if the score is higher than that previously recorded <b>this cycle</b>.
	 * @param player the player
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @param offeringScore the offering score
	 * @return boolean isHighestScore
	 */
	public boolean setFinalScore(PlayerInstance player, int oracle, int festivalId, int offeringScore)
	{
		List<String> partyMembers;
		
		final int currDawnHighScore = getHighestScore(SevenSigns.CABAL_DAWN, festivalId);
		final int currDuskHighScore = getHighestScore(SevenSigns.CABAL_DUSK, festivalId);
		int thisCabalHighScore = 0;
		int otherCabalHighScore = 0;
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			thisCabalHighScore = currDawnHighScore;
			otherCabalHighScore = currDuskHighScore;
			_dawnFestivalScores.put(festivalId, offeringScore);
		}
		else
		{
			thisCabalHighScore = currDuskHighScore;
			otherCabalHighScore = currDawnHighScore;
			_duskFestivalScores.put(festivalId, offeringScore);
		}
		
		final StatSet currFestData = getHighestScoreData(oracle, festivalId);
		
		// Check if this is the highest score for this level range so far for the player's cabal.
		if (offeringScore > thisCabalHighScore)
		{
			// If the current score is greater than that for the other cabal, then they already have the points from this festival.
			if (thisCabalHighScore > otherCabalHighScore)
			{
				return false;
			}
			
			partyMembers = new ArrayList<>();
			final List<PlayerInstance> prevParticipants = getPreviousParticipants(oracle, festivalId);
			
			// Record a string list of the party members involved.
			for (PlayerInstance partyMember : prevParticipants)
			{
				try
				{
					partyMembers.add(partyMember.getName());
				}
				catch (NullPointerException e)
				{
				}
			}
			
			// Update the highest scores and party list.
			currFestData.set("date", String.valueOf(System.currentTimeMillis()));
			currFestData.set("score", offeringScore);
			currFestData.set("members", Util.implodeString(partyMembers, ","));
			
			// Only add the score to the cabal's overall if it's higher than the other cabal's score.
			if (offeringScore > otherCabalHighScore)
			{
				final int contribPoints = FESTIVAL_LEVEL_SCORES[festivalId];
				
				// Give this cabal the festival points, while deducting them from the other.
				SevenSigns.getInstance().addFestivalScore(oracle, contribPoints);
				
				LOGGER.info("SevenSignsFestival: This is the highest score overall so far for the " + getFestivalName(festivalId) + " festival!");
			}
			
			saveFestivalData(true);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Gets the accumulated bonus.
	 * @param festivalId the festival id
	 * @return the accumulated bonus
	 */
	public int getAccumulatedBonus(int festivalId)
	{
		return _accumulatedBonuses.get(festivalId);
	}
	
	/**
	 * Gets the total accumulated bonus.
	 * @return the total accumulated bonus
	 */
	public int getTotalAccumulatedBonus()
	{
		int totalAccumBonus = 0;
		for (int accumBonus : _accumulatedBonuses)
		{
			totalAccumBonus += accumBonus;
		}
		return totalAccumBonus;
	}
	
	/**
	 * Adds the accumulated bonus.
	 * @param festivalId the festival id
	 * @param stoneType the stone type
	 * @param stoneAmount the stone amount
	 */
	public void addAccumulatedBonus(int festivalId, int stoneType, int stoneAmount)
	{
		int eachStoneBonus = 0;
		
		switch (stoneType)
		{
			case SevenSigns.SEAL_STONE_BLUE_ID:
			{
				eachStoneBonus = SevenSigns.SEAL_STONE_BLUE_VALUE;
				break;
			}
			case SevenSigns.SEAL_STONE_GREEN_ID:
			{
				eachStoneBonus = SevenSigns.SEAL_STONE_GREEN_VALUE;
				break;
			}
			case SevenSigns.SEAL_STONE_RED_ID:
			{
				eachStoneBonus = SevenSigns.SEAL_STONE_RED_VALUE;
				break;
			}
		}
		
		final int newTotalBonus = _accumulatedBonuses.get(festivalId) + (stoneAmount * eachStoneBonus);
		_accumulatedBonuses.set(festivalId, newTotalBonus);
	}
	
	/**
	 * Calculate and return the proportion of the accumulated bonus for the festival where the player was in the winning party, if the winning party's cabal won the event. The accumulated bonus is then updated, with the player's share deducted.
	 * @param player the player
	 * @return playerBonus (the share of the bonus for the party)
	 */
	public int distribAccumulatedBonus(PlayerInstance player)
	{
		int playerBonus = 0;
		final String playerName = player.getName();
		final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		if (playerCabal != SevenSigns.getInstance().getCabalHighestScore())
		{
			return 0;
		}
		
		if (_festivalData.get(_signsCycle) != null)
		{
			for (StatSet festivalData : _festivalData.get(_signsCycle).values())
			{
				if (festivalData.getString("members").contains(playerName))
				{
					final int festivalId = festivalData.getInt("festivalId");
					final int numPartyMembers = festivalData.getString("members").split(",").length;
					final int totalAccumBonus = _accumulatedBonuses.get(festivalId);
					playerBonus = totalAccumBonus / numPartyMembers;
					_accumulatedBonuses.set(festivalId, totalAccumBonus - playerBonus);
					break;
				}
			}
		}
		return playerBonus;
	}
	
	/**
	 * Used to send a "shout" message to all players currently present in an Oracle. Primarily used for Festival Guide and Witch related speech.
	 * @param senderName the sender name
	 * @param message the message
	 */
	public void sendMessageToAll(String senderName, String message)
	{
		if ((_dawnChatGuide == null) || (_duskChatGuide == null))
		{
			return;
		}
		
		CreatureSay cs = new CreatureSay(_dawnChatGuide.getObjectId(), ChatType.SHOUT, senderName, message);
		_dawnChatGuide.broadcastPacket(cs);
		
		cs = new CreatureSay(_duskChatGuide.getObjectId(), ChatType.SHOUT, senderName, message);
		_duskChatGuide.broadcastPacket(cs);
	}
	
	/**
	 * Basically a wrapper-call to signal to increase the challenge of the specified festival.
	 * @param oracle the oracle
	 * @param festivalId the festival id
	 * @return boolean isChalIncreased
	 */
	public boolean increaseChallenge(int oracle, int festivalId)
	{
		final DarknessFestival festivalInst = _managerInstance.getFestivalInstance(oracle, festivalId);
		return festivalInst.increaseChallenge();
	}
	
	protected class RestoreStatus implements Runnable
	{
		@Override
		public void run()
		{
			// nothing?
		}
	}
	
	/**
	 * Used with the SpawnListener, to update the required "chat guide" instances, for use with announcements in the oracles.
	 * @param npc the npc
	 */
	@Override
	public void npcSpawned(NpcInstance npc)
	{
		if (npc == null)
		{
			return;
		}
		
		final int npcId = npc.getNpcId();
		
		// If the spawned NPC ID matches the ones we need, assign their instances.
		if (npcId == 31127)
		{
			_dawnChatGuide = npc;
		}
		
		if (npcId == 31137)
		{
			_duskChatGuide = npc;
		}
	}
	
	/**
	 * The FestivalManager class is the main runner of all the festivals. It is used for easier integration and management of all running festivals.
	 * @author Tempy
	 */
	private class FestivalManager implements Runnable
	{
		/** The _festival instances. */
		protected Map<Integer, DarknessFestival> _festivalInstances;
		
		/**
		 * Instantiates a new festival manager.
		 */
		public FestivalManager()
		{
			_festivalInstances = new HashMap<>();
			_managerInstance = this;
			
			// Increment the cycle counter.
			_festivalCycle++;
			
			// Set the next start timers.
			setNextCycleStart();
			setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH - FESTIVAL_SIGNUP_TIME);
		}
		
		@Override
		public synchronized void run()
		{
			// The manager shouldn't be running if Seal Validation is in effect.
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				return;
			}
			
			// If the next period is due to start before the end of this festival cycle, then don't run it.
			if (SevenSigns.getInstance().getMilliToPeriodChange() < Config.ALT_FESTIVAL_CYCLE_LENGTH)
			{
				return;
			}
			
			sendMessageToAll("Festival Guide", "The main event will start in " + getMinsToNextFestival() + " minutes. Please register now.");
			
			// Stand by until the allowed signup period has elapsed.
			try
			{
				wait(FESTIVAL_SIGNUP_TIME);
			}
			catch (InterruptedException e)
			{
			}
			
			// Clear past participants, they can no longer register their score if not done so already.
			_dawnPreviousParticipants.clear();
			_duskPreviousParticipants.clear();
			
			// Get rid of random monsters that avoided deletion after last festival
			for (DarknessFestival festivalInst : _festivalInstances.values())
			{
				festivalInst.unspawnMobs();
			}
			
			/* INITIATION */
			// Set the festival timer to 0, as it is just beginning.
			long elapsedTime = 0;
			
			// Create the instances for the festivals in both Oracles, but only if they have participants signed up for them.
			for (int i = 0; i < FESTIVAL_COUNT; i++)
			{
				if (_duskFestivalParticipants.get(i) != null)
				{
					_festivalInstances.put(10 + i, new DarknessFestival(SevenSigns.CABAL_DUSK, i));
				}
				
				if (_dawnFestivalParticipants.get(i) != null)
				{
					_festivalInstances.put(20 + i, new DarknessFestival(SevenSigns.CABAL_DAWN, i));
				}
			}
			
			// Prevent future signups while festival is in progress.
			_festivalInitialized = true;
			setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH);
			sendMessageToAll("Festival Guide", "The main event is now starting.");
			
			// Stand by for a short length of time before starting the festival.
			try
			{
				wait(Config.ALT_FESTIVAL_FIRST_SPAWN);
			}
			catch (InterruptedException e)
			{
			}
			
			elapsedTime = Config.ALT_FESTIVAL_FIRST_SPAWN;
			
			// Participants can now opt to increase the challenge, if desired.
			_festivalInProgress = true;
			
			/* PROPOGATION */
			// Sequentially set all festivals to begin, spawn the Festival Witch and notify participants.
			for (DarknessFestival festivalInst : _festivalInstances.values())
			{
				festivalInst.festivalStart();
				festivalInst.sendMessageToParticipants("The festival is about to begin!");
			}
			
			// After a short time period, move all idle spawns to the center of the arena.
			try
			{
				wait(Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN);
			}
			catch (InterruptedException e)
			{
			}
			
			elapsedTime += Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN;
			for (DarknessFestival festivalInst : _festivalInstances.values())
			{
				festivalInst.moveMonstersToCenter();
			}
			
			// Stand by until the time comes for the second spawn.
			try
			{
				wait(Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM);
			}
			catch (InterruptedException e)
			{
			}
			
			// Spawn an extra set of monsters (archers) on the free platforms with a faster respawn when killed.
			for (DarknessFestival festivalInst : _festivalInstances.values())
			{
				festivalInst.spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN / 2, 2);
				festivalInst.sendMessageToParticipants("The festival will end in " + ((Config.ALT_FESTIVAL_LENGTH - Config.ALT_FESTIVAL_SECOND_SPAWN) / 60000) + " minute(s).");
			}
			
			elapsedTime += Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM;
			
			// After another short time period, again move all idle spawns to the center of the arena.
			try
			{
				wait(Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN);
			}
			catch (InterruptedException e)
			{
			}
			
			for (DarknessFestival festivalInst : _festivalInstances.values())
			{
				festivalInst.moveMonstersToCenter();
			}
			
			elapsedTime += Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN;
			
			// Stand by until the time comes for the chests to be spawned.
			try
			{
				wait(Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM);
			}
			catch (InterruptedException e)
			{
			}
			
			// Spawn the festival chests, which enable the team to gain greater rewards for each chest they kill.
			for (DarknessFestival festivalInst : _festivalInstances.values())
			{
				festivalInst.spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 3);
				festivalInst.sendMessageToParticipants("The chests have spawned! Be quick, the festival will end soon.");
			}
			
			elapsedTime += Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM;
			
			// Stand by and wait until it's time to end the festival.
			try
			{
				wait(Config.ALT_FESTIVAL_LENGTH - elapsedTime);
			}
			catch (InterruptedException e)
			{
			}
			
			// Participants can no longer opt to increase the challenge, as the festival will soon close.
			_festivalInProgress = false;
			
			/* TERMINATION */
			// Sequentially begin the ending sequence for all running festivals.
			for (DarknessFestival festivalInst : _festivalInstances.values())
			{
				festivalInst.festivalEnd();
			}
			
			// Clear the participants list for the next round of signups.
			_dawnFestivalParticipants.clear();
			_duskFestivalParticipants.clear();
			
			// Allow signups for the next festival cycle.
			_festivalInitialized = false;
			sendMessageToAll("Festival Witch", "That will do! I'll move you to the outside soon.");
		}
		
		/**
		 * Returns the running instance of a festival for the given Oracle and festivalID.<br>
		 * A <b>null</b> value is returned if there are no participants in that festival.
		 * @param oracle the oracle
		 * @param festivalId the festival id
		 * @return DarknessFestival festivalInst
		 */
		public DarknessFestival getFestivalInstance(int oracle, int festivalId)
		{
			if (!_festivalInitialized)
			{
				return null;
			}
			
			// Compute the offset if a Dusk instance is required. ID: 0 1 2 3 4 Dusk 1: 10 11 12 13 14 Dawn 2: 20 21 22 23 24
			festivalId += oracle == SevenSigns.CABAL_DUSK ? 10 : 20;
			return _festivalInstances.get(festivalId);
		}
	}
	
	/**
	 * Each running festival is represented by an DarknessFestival class. It contains all the spawn information and data for the running festival. All festivals are managed by the FestivalManager class, which must be initialized first.
	 */
	private class DarknessFestival
	{
		protected final int _cabal;
		protected final int _levelRange;
		protected boolean _challengeIncreased;
		private FestivalSpawn _startLocation;
		private FestivalSpawn _witchSpawn;
		private NpcInstance _witchInst;
		private final List<FestivalMonsterInstance> _npcInsts;
		private List<PlayerInstance> _participants;
		private final Map<PlayerInstance, FestivalSpawn> _originalLocations;
		
		/**
		 * Instantiates a new darkness festival.
		 * @param cabal the cabal
		 * @param levelRange the level range
		 */
		protected DarknessFestival(int cabal, int levelRange)
		{
			_cabal = cabal;
			_levelRange = levelRange;
			_originalLocations = new HashMap<>();
			_npcInsts = new ArrayList<>();
			if (cabal == SevenSigns.CABAL_DAWN)
			{
				_participants = _dawnFestivalParticipants.get(levelRange);
				_witchSpawn = new FestivalSpawn(FESTIVAL_DAWN_WITCH_SPAWNS[levelRange]);
				_startLocation = new FestivalSpawn(FESTIVAL_DAWN_PLAYER_SPAWNS[levelRange]);
			}
			else
			{
				_participants = _duskFestivalParticipants.get(levelRange);
				_witchSpawn = new FestivalSpawn(FESTIVAL_DUSK_WITCH_SPAWNS[levelRange]);
				_startLocation = new FestivalSpawn(FESTIVAL_DUSK_PLAYER_SPAWNS[levelRange]);
			}
			
			// FOR TESTING!
			if (_participants == null)
			{
				_participants = new ArrayList<>();
			}
			
			festivalInit();
		}
		
		/**
		 * Festival init.
		 */
		protected void festivalInit()
		{
			// Teleport all players to arena and notify them.
			if (!_participants.isEmpty())
			{
				try
				{
					for (PlayerInstance participant : _participants)
					{
						_originalLocations.put(participant, new FestivalSpawn(participant.getX(), participant.getY(), participant.getZ(), participant.getHeading()));
						
						// Randomize the spawn point around the specific centerpoint for each player.
						int x = _startLocation._x;
						int y = _startLocation._y;
						if (Rnd.nextBoolean())
						{
							x += Rnd.get(FESTIVAL_MAX_OFFSET_X);
							y += Rnd.get(FESTIVAL_MAX_OFFSET_Y);
						}
						else
						{
							x -= Rnd.get(FESTIVAL_MAX_OFFSET_X);
							y -= Rnd.get(FESTIVAL_MAX_OFFSET_Y);
						}
						
						participant.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						participant.teleToLocation(x, y, _startLocation._z, true);
						
						// Remove all buffs from all participants on entry. Works like the skill Cancel.
						participant.stopAllEffects();
						
						// Remove any stray blood offerings in inventory
						final ItemInstance bloodOfferings = participant.getInventory().getItemByItemId(FESTIVAL_OFFERING_ID);
						if (bloodOfferings != null)
						{
							participant.destroyItem("SevenSigns", bloodOfferings, null, true);
						}
					}
				}
				catch (NullPointerException e)
				{
					// deleteMe handling should teleport party out in case of disconnect
				}
			}
			
			final NpcTemplate witchTemplate = NpcTable.getInstance().getTemplate(_witchSpawn._npcId);
			
			// Spawn the festival witch for this arena
			try
			{
				final Spawn npcSpawn = new Spawn(witchTemplate);
				npcSpawn.setX(_witchSpawn._x);
				npcSpawn.setY(_witchSpawn._y);
				npcSpawn.setZ(_witchSpawn._z);
				npcSpawn.setHeading(_witchSpawn._heading);
				npcSpawn.setAmount(1);
				npcSpawn.setRespawnDelay(1);
				
				// Needed as doSpawn() is required to be called also for the NpcInstance it returns.
				npcSpawn.startRespawn();
				
				SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
				_witchInst = npcSpawn.doSpawn();
			}
			catch (Exception e)
			{
				LOGGER.warning("SevenSignsFestival: Error while spawning Festival Witch ID " + _witchSpawn._npcId + ": " + e);
			}
			
			// Make it appear as though the Witch has apparated there.
			MagicSkillUse msu = new MagicSkillUse(_witchInst, _witchInst, 2003, 1, 1, 0);
			_witchInst.broadcastPacket(msu);
			
			// And another one...:D
			msu = new MagicSkillUse(_witchInst, _witchInst, 2133, 1, 1, 0);
			_witchInst.broadcastPacket(msu);
			
			// Send a message to all participants from the witch.
			sendMessageToParticipants("The festival will begin in 2 minutes.");
		}
		
		/**
		 * Festival start.
		 */
		protected void festivalStart()
		{
			spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 0);
		}
		
		/**
		 * Move monsters to center.
		 */
		protected void moveMonstersToCenter()
		{
			for (FestivalMonsterInstance festivalMob : _npcInsts)
			{
				if (festivalMob.isDead())
				{
					continue;
				}
				
				// Only move monsters that are idle or doing their usual functions.
				final CtrlIntention currIntention = festivalMob.getAI().getIntention();
				if ((currIntention != CtrlIntention.AI_INTENTION_IDLE) && (currIntention != CtrlIntention.AI_INTENTION_ACTIVE))
				{
					continue;
				}
				
				int x = _startLocation._x;
				int y = _startLocation._y;
				
				// Random X and Y coords around the player start location, up to half of the maximum allowed offset are generated to prevent the mobs from all moving to the exact same place.
				if (Rnd.nextBoolean())
				{
					x += Rnd.get(FESTIVAL_MAX_OFFSET_X);
					y += Rnd.get(FESTIVAL_MAX_OFFSET_Y);
				}
				else
				{
					x -= Rnd.get(FESTIVAL_MAX_OFFSET_X);
					y -= Rnd.get(FESTIVAL_MAX_OFFSET_Y);
				}
				
				final Location moveTo = new Location(x, y, _startLocation._z, Rnd.get(65536));
				festivalMob.setRunning();
				festivalMob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, moveTo);
			}
		}
		
		/**
		 * Used to spawn monsters unique to the festival.<br>
		 * Valid SpawnTypes:<br>
		 * 0 - All Primary Monsters (starting monsters)<br>
		 * 1 - Same as 0, but without archers/marksmen. (used for challenge increase)<br>
		 * 2 - Secondary Monsters (archers)<br>
		 * 3 - Festival Chests
		 * @param respawnDelay the respawn delay
		 * @param spawnType the spawn type
		 */
		protected void spawnFestivalMonsters(int respawnDelay, int spawnType)
		{
			int[][] npcSpawns = null;
			
			switch (spawnType)
			{
				case 0:
				case 1:
				{
					npcSpawns = _cabal == SevenSigns.CABAL_DAWN ? FESTIVAL_DAWN_PRIMARY_SPAWNS[_levelRange] : FESTIVAL_DUSK_PRIMARY_SPAWNS[_levelRange];
					break;
				}
				case 2:
				{
					npcSpawns = _cabal == SevenSigns.CABAL_DAWN ? FESTIVAL_DAWN_SECONDARY_SPAWNS[_levelRange] : FESTIVAL_DUSK_SECONDARY_SPAWNS[_levelRange];
					break;
				}
				case 3:
				{
					npcSpawns = _cabal == SevenSigns.CABAL_DAWN ? FESTIVAL_DAWN_CHEST_SPAWNS[_levelRange] : FESTIVAL_DUSK_CHEST_SPAWNS[_levelRange];
					break;
				}
				default:
				{
					return;
				}
			}
			
			for (int[] npcSpawn2 : npcSpawns)
			{
				final FestivalSpawn currSpawn = new FestivalSpawn(npcSpawn2);
				
				// Only spawn archers/marksmen if specified to do so.
				if ((spawnType == 1) && isFestivalArcher(currSpawn._npcId))
				{
					continue;
				}
				
				final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(currSpawn._npcId);
				
				try
				{
					final Spawn npcSpawn = new Spawn(npcTemplate);
					npcSpawn.setX(currSpawn._x);
					npcSpawn.setY(currSpawn._y);
					npcSpawn.setZ(currSpawn._z);
					npcSpawn.setHeading(Rnd.get(65536));
					npcSpawn.setAmount(1);
					npcSpawn.setRespawnDelay(respawnDelay);
					
					// Needed as doSpawn() is required to be called also for the NpcInstance it returns.
					npcSpawn.startRespawn();
					
					SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
					final FestivalMonsterInstance festivalMob = (FestivalMonsterInstance) npcSpawn.doSpawn();
					
					// Set the offering bonus to 2x or 5x the amount per kill,
					// if this spawn is part of an increased challenge or is a festival chest.
					if (spawnType == 1)
					{
						festivalMob.setOfferingBonus(2);
					}
					else if (spawnType == 3)
					{
						festivalMob.setOfferingBonus(5);
					}
					
					_npcInsts.add(festivalMob);
				}
				catch (Exception e)
				{
					LOGGER.warning("SevenSignsFestival: Error while spawning NPC ID " + currSpawn._npcId + ": " + e);
				}
			}
		}
		
		/**
		 * Increase challenge.
		 * @return true, if successful
		 */
		protected boolean increaseChallenge()
		{
			if (_challengeIncreased)
			{
				return false;
			}
			
			// Set this flag to true to make sure that this can only be done once.
			_challengeIncreased = true;
			
			// Spawn more festival monsters, but this time with a twist.
			spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 1);
			return true;
		}
		
		/**
		 * Send message to participants.
		 * @param message the message
		 */
		public void sendMessageToParticipants(String message)
		{
			if (!_participants.isEmpty())
			{
				final CreatureSay cs = new CreatureSay(_witchInst.getObjectId(), ChatType.GENERAL, "Festival Witch", message);
				for (PlayerInstance participant : _participants)
				{
					try
					{
						participant.sendPacket(cs);
					}
					catch (NullPointerException e)
					{
					}
				}
			}
		}
		
		/**
		 * Festival end.
		 */
		protected void festivalEnd()
		{
			if (!_participants.isEmpty())
			{
				for (PlayerInstance participant : _participants)
				{
					try
					{
						relocatePlayer(participant, false);
						participant.sendMessage("The festival has ended. Your party leader must now register your score before the next festival takes place.");
					}
					catch (NullPointerException e)
					{
					}
				}
				
				if (_cabal == SevenSigns.CABAL_DAWN)
				{
					_dawnPreviousParticipants.put(_levelRange, _participants);
				}
				else
				{
					_duskPreviousParticipants.put(_levelRange, _participants);
				}
			}
			_participants = null;
			unspawnMobs();
		}
		
		/**
		 * Unspawn mobs.
		 */
		protected void unspawnMobs()
		{
			// Delete all the NPCs in the current festival arena.
			if (_witchInst != null)
			{
				_witchInst.deleteMe();
			}
			
			if (_npcInsts != null)
			{
				for (FestivalMonsterInstance monsterInst : _npcInsts)
				{
					if (monsterInst != null)
					{
						monsterInst.deleteMe();
					}
				}
			}
		}
		
		/**
		 * Relocate player.
		 * @param participant the participant
		 * @param isRemoving the is removing
		 */
		public void relocatePlayer(PlayerInstance participant, boolean isRemoving)
		{
			try
			{
				final FestivalSpawn origPosition = _originalLocations.get(participant);
				if (isRemoving)
				{
					_originalLocations.remove(participant);
				}
				
				participant.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				participant.teleToLocation(origPosition._x, origPosition._y, origPosition._z, true);
				participant.sendMessage("You have been removed from the festival arena.");
			}
			catch (Exception e)
			{
				// If an exception occurs, just move the player to the nearest town.
				try
				{
					participant.teleToLocation(TeleportWhereType.TOWN);
					participant.sendMessage("You have been removed from the festival arena.");
				}
				catch (NullPointerException e2)
				{
				}
			}
		}
	}
	
	private class FestivalSpawn
	{
		protected final int _x;
		protected final int _y;
		protected final int _z;
		protected final int _heading;
		protected final int _npcId;
		
		/**
		 * Instantiates a new festival spawn.
		 * @param x the x
		 * @param y the y
		 * @param z the z
		 * @param heading the heading
		 */
		protected FestivalSpawn(int x, int y, int z, int heading)
		{
			_x = x;
			_y = y;
			_z = z;
			
			// Generate a random heading if no positive one given.
			_heading = heading < 0 ? Rnd.get(65536) : heading;
			_npcId = -1;
		}
		
		/**
		 * Instantiates a new festival spawn.
		 * @param spawnData the spawn data
		 */
		protected FestivalSpawn(int[] spawnData)
		{
			_x = spawnData[0];
			_y = spawnData[1];
			_z = spawnData[2];
			_heading = spawnData[3] < 0 ? Rnd.get(65536) : spawnData[3];
			if (spawnData.length > 4)
			{
				_npcId = spawnData[4];
			}
			else
			{
				_npcId = -1;
			}
		}
	}
	
	public static SevenSignsFestival getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SevenSignsFestival INSTANCE = new SevenSignsFestival();
	}
}
