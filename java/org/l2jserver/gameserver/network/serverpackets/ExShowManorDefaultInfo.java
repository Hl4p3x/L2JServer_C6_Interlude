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

import java.util.List;

import org.l2jserver.gameserver.datatables.xml.ManorSeedData;

/**
 * format(packet 0xFE) ch cd [ddddcdcd] c - id h - sub id c d - size [ d - level d - seed price d - seed level d - crop price c d - reward 1 id c d - reward 2 id ]
 * @author l3x
 */
public class ExShowManorDefaultInfo extends GameServerPacket
{
	private List<Integer> _crops = null;
	
	public ExShowManorDefaultInfo()
	{
		_crops = ManorSeedData.getInstance().getAllCrops();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1E);
		writeC(0);
		writeD(_crops.size());
		for (int cropId : _crops)
		{
			writeD(cropId); // crop Id
			writeD(ManorSeedData.getInstance().getSeedLevelByCrop(cropId)); // level
			writeD(ManorSeedData.getInstance().getSeedBasicPriceByCrop(cropId)); // seed price
			writeD(ManorSeedData.getInstance().getCropBasicPrice(cropId)); // crop price
			writeC(1); // reward 1 Type
			writeD(ManorSeedData.getInstance().getRewardItem(cropId, 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(ManorSeedData.getInstance().getRewardItem(cropId, 2)); // Reward 2 Type Item Id
		}
	}
}
