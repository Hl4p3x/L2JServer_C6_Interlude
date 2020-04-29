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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.model.ArmorSet;
import org.l2jserver.gameserver.model.StatSet;

/**
 * This class loads and stores {@link ArmorSet}s, the key being the chest item id.
 */
public class ArmorSetData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ArmorSetData.class.getName());
	
	public Map<Integer, ArmorSet> _armorSets = new HashMap<>();
	
	private ArmorSetData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/ArmorSets.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _armorSets.size() + " armor sets.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		// StatsSet used to feed informations. Cleaned on every entry.
		final StatSet set = new StatSet();
		
		// First element is never read.
		final Node n = doc.getFirstChild();
		for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (!"armorset".equalsIgnoreCase(node.getNodeName()))
			{
				continue;
			}
			
			// Parse and feed content.
			final NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++)
			{
				final Node attr = attrs.item(i);
				set.set(attr.getNodeName(), attr.getNodeValue());
			}
			
			// Feed the map with new data.
			final int chestId = set.getInt("chest");
			_armorSets.put(chestId, new ArmorSet(chestId, set.getInt("legs"), set.getInt("head"), set.getInt("gloves"), set.getInt("feet"), set.getInt("skillId"), set.getInt("shield"), set.getInt("shieldSkillId"), set.getInt("enchant6Skill")));
		}
	}
	
	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}
	
	public ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
	
	public static ArmorSetData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ArmorSetData INSTANCE = new ArmorSetData();
	}
}
