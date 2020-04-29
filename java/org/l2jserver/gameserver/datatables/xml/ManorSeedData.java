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
package org.l2jserver.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.holders.SeedDataHolder;
import org.l2jserver.gameserver.model.items.Item;

/**
 * This class loads and stores manor seed information.
 * @author l3x, Mobius
 */
public class ManorSeedData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ManorSeedData.class.getName());
	
	private static Map<Integer, SeedDataHolder> _seeds = new ConcurrentHashMap<>();
	
	protected ManorSeedData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_seeds.clear();
		parseDatapackFile("data/ManorSeeds.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _seeds.size() + " seeds.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		// StatSet used to feed informations. Cleaned on every entry.
		final StatSet set = new StatSet();
		
		// First element is never read.
		final Node n = doc.getFirstChild();
		for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("seed".equalsIgnoreCase(node.getNodeName()))
			{
				final NamedNodeMap attrs = node.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					final Node att = attrs.item(i);
					set.set(att.getNodeName(), att.getNodeValue());
				}
				
				final int id = set.getInt("id");
				_seeds.put(id, new SeedDataHolder(id, set.getInt("level"), set.getInt("cropId"), set.getInt("matureId"), set.getInt("reward1"), set.getInt("reward2"), set.getInt("castleId"), set.getBoolean("isAlternative"), set.getInt("seedLimit"), set.getInt("cropLimit")));
			}
		}
	}
	
	public List<Integer> getAllCrops()
	{
		final List<Integer> crops = new ArrayList<>();
		for (SeedDataHolder seed : _seeds.values())
		{
			if (!crops.contains(seed.getCropId()) && (seed.getCropId() != 0))
			{
				crops.add(seed.getCropId());
			}
		}
		return crops;
	}
	
	public int getSeedBasicPrice(int seedId)
	{
		final Item seedItem = ItemTable.getInstance().getTemplate(seedId);
		if (seedItem != null)
		{
			return seedItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getSeedBasicPriceByCrop(int cropId)
	{
		for (SeedDataHolder seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return getSeedBasicPrice(seed.getId());
			}
		}
		return 0;
	}
	
	public int getCropBasicPrice(int cropId)
	{
		final Item cropItem = ItemTable.getInstance().getTemplate(cropId);
		if (cropItem != null)
		{
			return cropItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getMatureCrop(int cropId)
	{
		for (SeedDataHolder seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getMatureId();
			}
		}
		return 0;
	}
	
	/**
	 * Returns price which lord pays to buy one seed
	 * @param seedId
	 * @return seed price
	 */
	public int getSeedBuyPrice(int seedId)
	{
		final int buyPrice = getSeedBasicPrice(seedId) / 10;
		return buyPrice > 0 ? buyPrice : 1;
	}
	
	public int getSeedMinLevel(int seedId)
	{
		final SeedDataHolder seed = _seeds.get(seedId);
		if (seed != null)
		{
			return seed.getLevel() - 5;
		}
		return -1;
	}
	
	public int getSeedMaxLevel(int seedId)
	{
		final SeedDataHolder seed = _seeds.get(seedId);
		if (seed != null)
		{
			return seed.getLevel() + 5;
		}
		return -1;
	}
	
	public int getSeedLevelByCrop(int cropId)
	{
		for (SeedDataHolder seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getLevel();
			}
		}
		return 0;
	}
	
	public int getSeedLevel(int seedId)
	{
		final SeedDataHolder seed = _seeds.get(seedId);
		if (seed != null)
		{
			return seed.getLevel();
		}
		return -1;
	}
	
	public boolean isAlternative(int seedId)
	{
		for (SeedDataHolder seed : _seeds.values())
		{
			if (seed.getId() == seedId)
			{
				return seed.isAlternative();
			}
		}
		return false;
	}
	
	public int getCropType(int seedId)
	{
		final SeedDataHolder seed = _seeds.get(seedId);
		if (seed != null)
		{
			return seed.getCropId();
		}
		return -1;
	}
	
	public synchronized int getRewardItem(int cropId, int type)
	{
		for (SeedDataHolder seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getReward(type); // there can be several seeds with same crop, but reward should be the same for all
			}
		}
		return -1;
	}
	
	public synchronized int getRewardItemBySeed(int seedId, int type)
	{
		final SeedDataHolder seed = _seeds.get(seedId);
		if (seed != null)
		{
			return seed.getReward(type);
		}
		return 0;
	}
	
	/**
	 * Return all crops which can be purchased by given castle
	 * @param castleId
	 * @return
	 */
	public List<Integer> getCropsForCastle(int castleId)
	{
		final List<Integer> crops = new ArrayList<>();
		for (SeedDataHolder seed : _seeds.values())
		{
			if ((seed.getCastleId() == castleId) && !crops.contains(seed.getCropId()))
			{
				crops.add(seed.getCropId());
			}
		}
		return crops;
	}
	
	/**
	 * Return list of seed ids, which belongs to castle with given id
	 * @param castleId - id of the castle
	 * @return seedIds - list of seed ids
	 */
	public List<Integer> getSeedsForCastle(int castleId)
	{
		final List<Integer> seedIds = new ArrayList<>();
		for (SeedDataHolder seed : _seeds.values())
		{
			if ((seed.getCastleId() == castleId) && !seedIds.contains(seed.getId()))
			{
				seedIds.add(seed.getId());
			}
		}
		return seedIds;
	}
	
	/**
	 * Returns castle id where seed can be showed
	 * @param seedId
	 * @return castleId
	 */
	public int getCastleIdForSeed(int seedId)
	{
		final SeedDataHolder seed = _seeds.get(seedId);
		if (seed != null)
		{
			return seed.getCastleId();
		}
		return 0;
	}
	
	public int getSeedSaleLimit(int seedId)
	{
		final SeedDataHolder seed = _seeds.get(seedId);
		if (seed != null)
		{
			return (int) seed.getSeedLimit();
		}
		return 0;
	}
	
	public int getCropPuchaseLimit(int cropId)
	{
		for (SeedDataHolder seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return (int) seed.getCropLimit();
			}
		}
		return 0;
	}
	
	public static ManorSeedData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ManorSeedData INSTANCE = new ManorSeedData();
	}
}
