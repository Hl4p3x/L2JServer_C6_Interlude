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
package quests.Q505_BloodOffering;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.model.quest.State;
import org.l2jserver.gameserver.util.Util;

import quests.Q635_InTheDimensionalRift.Q635_InTheDimensionalRift;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q505_BloodOffering extends Quest
{
	// NPCs
	// @formatter:off
	private static final int[] TOWN_DAWN = {31078, 31079, 31080, 31081, 31083, 31084, 31082, 31692, 31694, 31997, 31168};
	private static final int[] TOWN_DUSK = {31085, 31086, 31087, 31088, 31090, 31091, 31089, 31693, 31695, 31998, 31169};
	private static final int[] DIM_GK = {31494, 31495, 31496, 31497, 31498, 31499, 31500, 31501, 31502, 31503, 31504, 31505, 31506, 31507};
	private static final int[] GK_ZIGGURAT = {31095, 31096, 31097, 31098, 31099, 31100, 31101, 31102, 31103, 31104, 31105, 31106, 31107, 31108, 31109, 31110, 31114, 31115, 31116, 31117, 31118, 31119, 31120, 31121, 31122, 31123, 31124, 31125};
	private static final int[] FESTIVALGUIDE = {31127, 31128, 31129, 31130, 31131, 31137, 31138, 31139, 31140, 31141};
	private static final int[] FESTIVALWITCH = {31132, 31133, 31134, 31135, 31136, 31142, 31143, 31144, 31145, 31146};
	private static final int[] RIFTPOST = {31488, 31489, 31490, 31491, 31492, 31493};
	// @formatter:on
	
	public Q505_BloodOffering()
	{
		super(505, "Blood Offering");
		
		addStartNpc(TOWN_DAWN);
		addTalkId(TOWN_DAWN);
		addStartNpc(TOWN_DUSK);
		addTalkId(TOWN_DUSK);
		addStartNpc(DIM_GK);
		addTalkId(DIM_GK);
		addStartNpc(GK_ZIGGURAT);
		addTalkId(GK_ZIGGURAT);
		addStartNpc(FESTIVALGUIDE);
		addTalkId(FESTIVALGUIDE);
		addStartNpc(FESTIVALWITCH);
		addTalkId(FESTIVALWITCH);
		addStartNpc(RIFTPOST);
		addTalkId(RIFTPOST);
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final String htmltext = getNoQuestMsg();
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		final QuestState qs2 = player.getQuestState(Q635_InTheDimensionalRift.class.getSimpleName());
		final int npcId = npc.getNpcId();
		if (Util.contains(FESTIVALGUIDE, npcId))
		{
			player.teleToLocation(-114796, -179334, -6752);
			if (qs2 != null)
			{
				qs2.setState(State.STARTED);
				qs2.set("cond", "1");
			}
			qs.playSound("ItemSound.quest_accept");
			qs.exitQuest(true);
			return "guide.htm";
		}
		else if (npcId == 31132)
		{
			player.teleToLocation(-80204, 87056, -5154);
			return "witch.htm";
		}
		else if (npcId == 31133)
		{
			player.teleToLocation(-77198, 87678, -5182);
			return "witch.htm";
		}
		else if (npcId == 31134)
		{
			player.teleToLocation(-76183, 87135, -5179);
			return "witch.htm";
		}
		else if (npcId == 31135)
		{
			player.teleToLocation(-76945, 86602, 5153);
			return "witch.htm";
		}
		else if (npcId == 31136)
		{
			player.teleToLocation(-79970, 85997, -5154);
			return "witch.htm";
		}
		else if (npcId == 31142)
		{
			player.teleToLocation(-79182, 111893, -4898);
			return "witch.htm";
		}
		else if (npcId == 31143)
		{
			player.teleToLocation(-76176, 112505, -4899);
			return "witch.htm";
		}
		else if (npcId == 31144)
		{
			player.teleToLocation(-75198, 111969, -4898);
			return "witch.htm";
		}
		else if (npcId == 31145)
		{
			player.teleToLocation(-75920, 111435, -4900);
			return "witch.htm";
		}
		else if (npcId == 31146)
		{
			player.teleToLocation(-78928, 110825, -4926);
			return "witch.htm";
		}
		else if (Util.contains(RIFTPOST, npcId))
		{
			if (!SevenSigns.getInstance().isSealValidationPeriod())
			{
				final String cabal = SevenSigns.getInstance().getPlayerData(player).getString("cabal");
				if (cabal.equals("dawn"))
				{
					qs.setState(State.STARTED);
					qs.set("cond", "1");
					qs.getPlayer().teleToLocation(-80157, 111344, -4901);
					if (qs2 != null)
					{
						qs2.unset("cond");
					}
					return "riftpost-1.htm";
				}
				if (cabal.equals("dusk"))
				{
					qs.setState(State.STARTED);
					qs.set("cond", "1");
					if (qs2 != null)
					{
						qs2.unset("cond");
					}
					qs.getPlayer().teleToLocation(-81261, 86531, -5157);
					return "riftpost-1.htm";
				}
				return "riftpost-2.htm";
			}
			return "riftpost-2.htm";
		}
		
		return htmltext;
	}
}