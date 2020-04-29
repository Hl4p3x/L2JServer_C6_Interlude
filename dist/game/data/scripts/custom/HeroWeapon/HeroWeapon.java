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
package custom.HeroWeapon;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;
import org.l2jserver.gameserver.util.Util;

public class HeroWeapon extends Quest
{
	private static final int[] WEAPON_IDS =
	{
		6611,
		6612,
		6613,
		6614,
		6615,
		6616,
		6617,
		6618,
		6619,
		6620,
		6621
	};
	
	public HeroWeapon()
	{
		super(-1, "custom");
		
		addStartNpc(31690, 31769, 31770, 31771, 31772, 31773);
		addTalkId(31690, 31769, 31770, 31771, 31772, 31773);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		
		final int weaponId = Integer.parseInt(event);
		if (Util.contains(WEAPON_IDS, weaponId))
		{
			st.giveItems(weaponId, 1);
		}
		
		st.exitQuest(true);
		return null;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			newQuestState(player);
		}
		
		if (st != null)
		{
			if (player.isHero())
			{
				if (hasHeroWeapon(player))
				{
					htmltext = "already_have_weapon.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "weapon_list.htm";
				}
			}
			else
			{
				htmltext = "no_hero.htm";
				st.exitQuest(true);
			}
		}
		
		return htmltext;
	}
	
	private static boolean hasHeroWeapon(PlayerInstance player)
	{
		for (int i : WEAPON_IDS)
		{
			if (player.getInventory().getItemByItemId(i) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new HeroWeapon();
	}
}