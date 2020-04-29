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
package org.l2jserver.gameserver.model.holders;

import org.l2jserver.Config;

/**
 * @author Mobius
 */
public class SeedDataHolder
{
	private final int _id;
	private final int _level;
	private final int _cropId;
	private final int _matureId;
	private final int _reward1;
	private final int _reward2;
	private final int _castleId;
	private final boolean _isAlternative;
	private final int _seedLimit;
	private final int _cropLimit;
	
	public SeedDataHolder(int id, int level, int crop, int mature, int reward1, int reward2, int castleId, boolean isAlternative, int seedLimit, int cropLimit)
	{
		_id = id;
		_level = level;
		_cropId = crop;
		_matureId = mature;
		_reward1 = reward1;
		_reward2 = reward2;
		_castleId = castleId;
		_isAlternative = isAlternative;
		_seedLimit = seedLimit;
		_cropLimit = cropLimit;
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getCropId()
	{
		return _cropId;
	}
	
	public int getMatureId()
	{
		return _matureId;
	}
	
	public int getReward(int type)
	{
		return type == 1 ? _reward1 : _reward2;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public boolean isAlternative()
	{
		return _isAlternative;
	}
	
	public float getSeedLimit()
	{
		return _seedLimit * Config.RATE_DROP_MANOR;
	}
	
	public float getCropLimit()
	{
		return _cropLimit * Config.RATE_DROP_MANOR;
	}
}
