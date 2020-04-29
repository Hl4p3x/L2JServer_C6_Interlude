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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.model.SummonItem;

/**
 * This class loads and stores summon items.
 */
public class SummonItemData implements IXmlReader
{
	private final Map<Integer, SummonItem> _items = new HashMap<>();
	
	protected SummonItemData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/SummonItems.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _items.size() + " summon items.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		// First element is never read.
		final Node n = doc.getFirstChild();
		for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (!"item".equalsIgnoreCase(node.getNodeName()))
			{
				continue;
			}
			
			final NamedNodeMap attrs = node.getAttributes();
			final int itemId = Integer.valueOf(attrs.getNamedItem("id").getNodeValue());
			final int npcId = Integer.valueOf(attrs.getNamedItem("npcId").getNodeValue());
			final byte summonType = Byte.valueOf(attrs.getNamedItem("summonType").getNodeValue());
			_items.put(itemId, new SummonItem(itemId, npcId, summonType));
		}
	}
	
	public SummonItem getSummonItem(int itemId)
	{
		return _items.get(itemId);
	}
	
	public int[] getAllItemIds()
	{
		int index = 0;
		final int[] ids = new int[_items.size()];
		for (Integer id : _items.keySet())
		{
			ids[index++] = id;
		}
		return ids;
	}
	
	public static SummonItemData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonItemData INSTANCE = new SummonItemData();
	}
}