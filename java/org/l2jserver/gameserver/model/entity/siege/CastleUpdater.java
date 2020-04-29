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
package org.l2jserver.gameserver.model.entity.siege;

import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.instancemanager.CastleManager;
import org.l2jserver.gameserver.model.ItemContainer;
import org.l2jserver.gameserver.model.clan.Clan;

/**
 * Thorgrim - 2005 Class managing periodical events with castle
 */
public class CastleUpdater implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(CastleUpdater.class.getName());
	
	private final Clan _clan;
	private int _runCount = 0;
	
	public CastleUpdater(Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}
	
	@Override
	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			final ItemContainer warehouse = _clan.getWarehouse();
			if ((warehouse != null) && (_clan.getHasCastle() > 0))
			{
				final Castle castle = CastleManager.getInstance().getCastleById(_clan.getHasCastle());
				if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS && ((_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE) == 0))
				{
					castle.saveSeedData();
					castle.saveCropData();
				}
				
				_runCount++;
				final CastleUpdater cu = new CastleUpdater(_clan, _runCount);
				ThreadPool.schedule(cu, 3600000);
			}
		}
		catch (Throwable e)
		{
			LOGGER.warning(e.toString());
		}
	}
}
