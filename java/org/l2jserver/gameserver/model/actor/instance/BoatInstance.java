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
package org.l2jserver.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.GameTimeController;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.knownlist.BoatKnownList;
import org.l2jserver.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jserver.gameserver.model.holders.BoatPathHolder;
import org.l2jserver.gameserver.model.holders.BoatPathHolder.BoatPoint;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.OnVehicleCheckLocation;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.VehicleDeparture;
import org.l2jserver.gameserver.network.serverpackets.VehicleInfo;

/**
 * @author Maktakien
 */
public class BoatInstance extends Creature
{
	public float boatSpeed;
	public VehicleDeparture vd = null;
	private int lastx = -1;
	private int lasty = -1;
	private int cycle = 0;
	private int runstate = 0;
	private BoatPathHolder pathA;
	private BoatPathHolder pathB;
	private boolean needOnVehicleCheckLocation = false;
	private final Set<PlayerInstance> passengers = ConcurrentHashMap.newKeySet();
	
	public BoatInstance(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
		super.setKnownList(new BoatKnownList(this));
	}
	
	public void moveToLocation(int x, int y, int z, float speed)
	{
		final int curX = getX();
		final int curY = getY();
		
		// Calculate distance (dx,dy) between current position and destination
		final int dx = x - curX;
		final int dy = y - curY;
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		
		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		
		final double cos = dx / distance;
		final double sin = dy / distance;
		// Create and Init a MoveData object
		final MoveData m = new MoveData();
		
		// Calculate and set the heading of the Creature
		getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		m._heading = 0;
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m._moveStartTime = GameTimeController.getGameTicks();
		
		// Set the Creature _move object to MoveData object
		_move = m;
		
		// Add the Creature to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
	}
	
	public void evtArrived()
	{
		if (runstate != 0)
		{
			final BoatRun bc = new BoatRun(runstate, this);
			ThreadPool.schedule(bc, 10);
			runstate = 0;
		}
	}
	
	public void sendVehicleDeparture(PlayerInstance player)
	{
		if (vd != null)
		{
			player.sendPacket(vd);
		}
	}
	
	public VehicleDeparture getVehicleDeparture()
	{
		return vd;
	}
	
	public void removePassenger(PlayerInstance player)
	{
		try
		{
			passengers.remove(player);
		}
		catch (Exception e)
		{
		}
	}
	
	public void updatePeopleInTheBoat(int x, int y, int z)
	{
		if (passengers != null)
		{
			boolean check = false;
			if ((lastx == -1) || (lasty == -1))
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			else if ((((x - lastx) * (x - lastx)) + ((y - lasty) * (y - lasty))) > 2250000) // 1500 * 1500 = 2250000
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			for (PlayerInstance player : passengers)
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInBoat() && (player.getBoat() == this))
				{
					player.setXYZ(x, y, z);
					player.revalidateZone(false);
				}
				
				if (check && needOnVehicleCheckLocation)
				{
					player.sendPacket(new OnVehicleCheckLocation(this, x, y, z));
				}
			}
		}
	}
	
	private void beginCycle()
	{
		say(10);
		final BoatCaptain bc = new BoatCaptain(1, this);
		ThreadPool.schedule(bc, 300000);
	}
	
	private void begin()
	{
		if (cycle == 1)
		{
			final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if ((knownPlayers != null) && !knownPlayers.isEmpty())
			{
				passengers.clear();
				for (PlayerInstance player : knownPlayers)
				{
					if (player.isInBoat() && (player.getBoat() == this))
					{
						ItemInstance it;
						it = player.getInventory().getItemByItemId(pathA.ticketId);
						if ((it != null) && (it.getCount() >= 1))
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							passengers.add(player);
						}
						else if ((it == null) && (pathA.ticketId == 0))
						{
							passengers.add(player);
						}
						else
						{
							player.teleToLocation(pathA.ntx, pathA.nty, pathA.ntz, false);
						}
					}
				}
			}
			ThreadPool.execute(new BoatRun(0, this));
		}
		else if (cycle == 2)
		{
			final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if ((knownPlayers != null) && !knownPlayers.isEmpty())
			{
				passengers.clear();
				for (PlayerInstance player : knownPlayers)
				{
					if (player.isInBoat() && (player.getBoat() == this))
					{
						ItemInstance it;
						it = player.getInventory().getItemByItemId(pathB.ticketId);
						if ((it != null) && (it.getCount() >= 1))
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							passengers.add(player);
						}
						else if ((it == null) && (pathB.ticketId == 0))
						{
							passengers.add(player);
						}
						else
						{
							player.teleToLocation(pathB.ntx, pathB.nty, pathB.ntz, false);
						}
					}
				}
			}
			ThreadPool.execute(new BoatRun(0, this));
		}
	}
	
	private void say(int id)
	{
		final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		CreatureSay sm;
		PlaySound ps;
		switch (id)
		{
			case 10:
			{
				if (cycle == 1)
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathA.npc, pathA.sysmess10);
				}
				else
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathB.npc, pathB.sysmess10);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", this);
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
					// Mobius: Fixes weird movement at the end of the path.
					player.sendPacket(new VehicleInfo(this));
				}
				break;
			}
			case 5:
			{
				if (cycle == 1)
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathA.npc, pathA.sysmess5);
				}
				else
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathB.npc, pathB.sysmess5);
				}
				ps = new PlaySound(0, "itemsound.ship_5min", this);
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			}
			case 1:
			{
				if (cycle == 1)
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathA.npc, pathA.sysmess1);
				}
				else
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathB.npc, pathB.sysmess1);
				}
				ps = new PlaySound(0, "itemsound.ship_1min", this);
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			}
			case 0:
			{
				if (cycle == 1)
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathA.npc, pathA.sysmess0);
				}
				else
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathB.npc, pathB.sysmess0);
				}
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
				}
				break;
			}
			case -1:
			{
				if (cycle == 1)
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathA.npc, pathA.sysmessb);
				}
				else
				{
					sm = new CreatureSay(0, ChatType.SHOUT, pathB.npc, pathB.sysmessb);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", this);
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
					// Mobius: Fixes weird movement at the end of the path.
					player.sendPacket(new VehicleInfo(this));
				}
				break;
			}
		}
	}
	
	public void spawn()
	{
		cycle = 1;
		beginCycle();
		final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if ((knownPlayers == null) || knownPlayers.isEmpty())
		{
			return;
		}
		final VehicleInfo info = new VehicleInfo(this);
		for (PlayerInstance player : knownPlayers)
		{
			player.sendPacket(info);
		}
	}
	
	public void setPathA(int pathId, int ticketId, int ntx, int nty, int ntz, String announcer, String sysmess10, String sysmess5, String sysmess1, String sysmess0, String sysmessb, List<BoatPoint> path)
	{
		pathA = new BoatPathHolder(pathId, ticketId, ntx, nty, ntz, announcer, sysmess10, sysmess5, sysmess1, sysmess0, sysmessb, path);
	}
	
	public void setPathB(int pathId, int ticketId, int ntx, int nty, int ntz, String announcer, String sysmess10, String sysmess5, String sysmess1, String sysmess0, String sysmessb, List<BoatPoint> path)
	{
		pathB = new BoatPathHolder(pathId, ticketId, ntx, nty, ntz, announcer, sysmess10, sysmess5, sysmess1, sysmess0, sysmessb, path);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		// No effects.
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public int getLevel()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	public boolean isBoat()
	{
		return true;
	}
	
	private class BoatCaptain implements Runnable
	{
		private final int _state;
		private final BoatInstance _boat;
		
		public BoatCaptain(int state, BoatInstance instance)
		{
			_state = state;
			_boat = instance;
		}
		
		@Override
		public void run()
		{
			BoatCaptain bc;
			switch (_state)
			{
				case 1:
				{
					_boat.say(5);
					bc = new BoatCaptain(2, _boat);
					ThreadPool.schedule(bc, 240000);
					break;
				}
				case 2:
				{
					_boat.say(1);
					bc = new BoatCaptain(3, _boat);
					ThreadPool.schedule(bc, 40000);
					break;
				}
				case 3:
				{
					_boat.say(0);
					bc = new BoatCaptain(4, _boat);
					ThreadPool.schedule(bc, 20000);
					break;
				}
				case 4:
				{
					_boat.say(-1);
					_boat.begin();
					break;
				}
			}
		}
	}
	
	private class BoatRun implements Runnable
	{
		private int _state;
		private final BoatInstance _boat;
		
		public BoatRun(int state, BoatInstance instance)
		{
			_state = state;
			_boat = instance;
		}
		
		@Override
		public void run()
		{
			_boat.vd = null;
			_boat.needOnVehicleCheckLocation = false;
			if (_boat.cycle == 1)
			{
				final int time = _boat.pathA.state(_state, _boat);
				if (time > 0)
				{
					_state++;
					final BoatRun bc = new BoatRun(_state, _boat);
					ThreadPool.schedule(bc, time);
				}
				else if (time == 0)
				{
					_boat.cycle = 2;
					_boat.say(10);
					final BoatCaptain bc = new BoatCaptain(1, _boat);
					ThreadPool.schedule(bc, 300000);
				}
				else
				{
					_boat.needOnVehicleCheckLocation = true;
					_state++;
					_boat.runstate = _state;
				}
			}
			else if (_boat.cycle == 2)
			{
				final int time = _boat.pathB.state(_state, _boat);
				if (time > 0)
				{
					_state++;
					final BoatRun bc = new BoatRun(_state, _boat);
					ThreadPool.schedule(bc, time);
				}
				else if (time == 0)
				{
					_boat.cycle = 1;
					_boat.say(10);
					final BoatCaptain bc = new BoatCaptain(1, _boat);
					ThreadPool.schedule(bc, 300000);
				}
				else
				{
					_boat.needOnVehicleCheckLocation = true;
					_state++;
					_boat.runstate = _state;
				}
			}
		}
	}
}
