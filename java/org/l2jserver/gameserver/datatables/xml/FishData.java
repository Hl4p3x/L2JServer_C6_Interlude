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
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.model.Fish;
import org.l2jserver.gameserver.model.StatSet;

/**
 * This class loads and stores {@link Fish} infos.
 */
public class FishData implements IXmlReader
{
	private final List<Fish> _fish = new ArrayList<>();
	
	protected FishData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/Fish.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _fish.size() + " fish.");
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
			if (!"fish".equalsIgnoreCase(node.getNodeName()))
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
			
			// Feed the list with new data.
			_fish.add(new Fish(set));
		}
	}
	
	/**
	 * Get a random {@link FishData} based on level, type and group.
	 * @param lvl : the fish level to check.
	 * @param type : the fish type to check.
	 * @param group : the fish group to check.
	 * @return a Fish with good criteria.
	 */
	public Fish getFish(int lvl, int type, int group)
	{
		final List<Fish> fish = _fish.stream().filter(f -> (f.getLevel() == lvl) && (f.getType() == type) && (f.getGroup() == group)).collect(Collectors.toList());
		return fish.get(Rnd.get(fish.size()));
	}
	
	public static FishData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FishData INSTANCE = new FishData();
	}
}