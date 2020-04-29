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

import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import org.l2jserver.gameserver.model.quest.Quest;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Gustav extends Quest
{
	// NPCs
	private static final int GUSTAV = 35410;
	private static final int MESSENGER = 35420;
	// Misc
	private static final Collection<Clan> _clans = ConcurrentHashMap.newKeySet();
	
	private Gustav()
	{
		super(-1, "ai/bosses");
		
		addTalkId(MESSENGER);
		addStartNpc(MESSENGER);
		addAttackId(GUSTAV);
		addKillId(GUSTAV);
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		final Clan playerClan = player.getClan();
		for (Clan clan : _clans)
		{
			if (clan == playerClan)
			{
				return "<html><body>You already registered!</body></html>";
			}
		}
		
		if (DevastatedCastle.getInstance().Conditions(player))
		{
			if (!_clans.contains(playerClan))
			{
				_clans.add(playerClan);
			}
			return "<html><body>You have successful registered on a siege.</body></html>";
		}
		
		return "<html><body>You are not allowed to do that!</body></html>";
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		final Clan playerClan = attacker.getClan();
		if (playerClan != null)
		{
			for (Clan clan : _clans)
			{
				if (clan == playerClan)
				{
					DevastatedCastle.getInstance().addSiegeDamage(clan, damage);
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		DevastatedCastle.getInstance().SiegeFinish();
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Gustav();
	}
}
