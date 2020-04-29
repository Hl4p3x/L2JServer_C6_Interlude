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
package org.l2jserver.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.entity.siege.Castle;
import org.l2jserver.gameserver.model.zone.ZoneId;
import org.l2jserver.gameserver.model.zone.ZoneType;

public class CastleTeleportZone extends ZoneType
{
	private final int[] _spawnLoc;
	
	public CastleTeleportZone(int id)
	{
		super(id);
		_spawnLoc = new int[5];
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "castleId":
			{
				final int castleId = Integer.parseInt(value);
				final Castle castle = CastleManager.getInstance().getCastleById(castleId);
				castle.setTeleZone(this);
				break;
			}
			case "spawnMinX":
			{
				_spawnLoc[0] = Integer.parseInt(value);
				break;
			}
			case "spawnMaxX":
			{
				_spawnLoc[1] = Integer.parseInt(value);
				break;
			}
			case "spawnMinY":
			{
				_spawnLoc[2] = Integer.parseInt(value);
				break;
			}
			case "spawnMaxY":
			{
				_spawnLoc[3] = Integer.parseInt(value);
				break;
			}
			case "spawnZ":
			{
				_spawnLoc[4] = Integer.parseInt(value);
				break;
			}
			default:
			{
				super.setParameter(name, value);
				break;
			}
		}
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.OLYMPIAD, true);
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.OLYMPIAD, false);
	}
	
	@Override
	public void onDieInside(Creature l2character)
	{
	}
	
	@Override
	public void onReviveInside(Creature l2character)
	{
	}
	
	public List<Creature> getAllPlayers()
	{
		final List<Creature> players = new ArrayList<>();
		for (Creature creature : getCharactersInside())
		{
			if (creature instanceof PlayerInstance)
			{
				players.add(creature);
			}
		}
		return players;
	}
	
	public void oustAllPlayers()
	{
		for (Creature creature : getCharactersInside())
		{
			if ((creature != null) && (creature instanceof PlayerInstance))
			{
				final PlayerInstance player = (PlayerInstance) creature;
				if (player.isOnline())
				{
					player.teleToLocation(Rnd.get(_spawnLoc[0], _spawnLoc[1]), Rnd.get(_spawnLoc[2], _spawnLoc[3]), _spawnLoc[4]);
				}
			}
		}
	}
	
	public int[] getSpawn()
	{
		return _spawnLoc;
	}
}
