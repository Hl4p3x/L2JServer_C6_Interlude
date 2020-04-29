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
package custom.ShadowWeapon;

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.model.quest.QuestState;

import village_master.FirstClassChange.FirstClassChange;
import village_master.SecondClassChange.SecondClassChange;

/**
 * @authors: DrLecter (python), Nyaran (java)
 */
public class ShadowWeapon extends Quest
{
	// itemId for shadow weapon coupons, it's not used more than once but increases readability
	private static final int D_COUPON = 8869;
	private static final int C_COUPON = 8870;
	
	public ShadowWeapon()
	{
		super(-1, "custom");
		
		addStartNpc(FirstClassChange.FIRSTCLASSNPCS);
		addTalkId(FirstClassChange.FIRSTCLASSNPCS);
		
		addStartNpc(SecondClassChange.SECONDCLASSNPCS);
		addTalkId(SecondClassChange.SECONDCLASSNPCS);
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		final boolean hasD = st.hasQuestItems(D_COUPON);
		final boolean hasC = st.hasQuestItems(C_COUPON);
		if (hasD || hasC)
		{
			// let's assume character had both c & d-grade coupons, we'll confirm later
			String multisell = "306893003";
			if (!hasD)
			{
				multisell = "306893002";
			}
			else if (!hasC)
			{
				multisell = "306893001";
			}
			
			// finally, return htm with proper multisell value in it.
			htmltext = getHtmlText("exchange.htm").replace("%msid%", multisell);
		}
		else
		{
			htmltext = "exchange-no.htm";
		}
		
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new ShadowWeapon();
	}
}