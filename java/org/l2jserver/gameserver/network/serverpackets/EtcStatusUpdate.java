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
package org.l2jserver.gameserver.network.serverpackets;

import org.l2jserver.gameserver.model.Effect;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.skills.effects.EffectCharge;
import org.l2jserver.gameserver.model.zone.ZoneId;

/* Packet format: F3 XX000000 YY000000 ZZ000000 */

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends GameServerPacket
{
	private final PlayerInstance _player;
	private final EffectCharge _effect;
	
	public EtcStatusUpdate(PlayerInstance player)
	{
		_player = player;
		_effect = (EffectCharge) _player.getFirstEffect(Effect.EffectType.CHARGE);
	}
	
	/**
	 * @see org.l2jserver.gameserver.network.serverpackets.GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xF3); // several icons to a separate line (0 = disabled)
		if (_effect != null)
		{
			writeD(_effect.getLevel()); // 1-7 increase force, lvl
		}
		else
		{
			writeD(0x00); // 1-7 increase force, lvl
		}
		writeD(_player.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeD(_player.isInRefusalMode() || _player.isChatBanned() ? 1 : 0); // 1 = block all chat
		// writeD(0x00); // 1 = danger area
		writeD(_player.isInsideZone(ZoneId.DANGER_AREA)/* || _player.isInDangerArea() */ ? 1 : 0); // 1 = danger area
		writeD(Math.min(_player.getExpertisePenalty() + _player.getMasteryPenalty() + _player.getMasteryWeapPenalty(), 1)); // 1 = grade penalty
		writeD(_player.getCharmOfCourage() ? 1 : 0); // 1 = charm of courage (no xp loss in siege..)
		writeD(_player.getDeathPenaltyBuffLevel()); // 1-15 death penalty, lvl (combat ability decreased due to death)
	}
}
