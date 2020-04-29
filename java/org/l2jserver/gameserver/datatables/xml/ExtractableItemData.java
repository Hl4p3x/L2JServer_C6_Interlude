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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.model.ExtractableItem;
import org.l2jserver.gameserver.model.ExtractableProductItem;
import org.l2jserver.gameserver.model.StatSet;

/**
 * @author Mobius
 */
public class ExtractableItemData implements IXmlReader
{
	private final Map<Integer, ExtractableItem> _items = new HashMap<>();
	
	protected ExtractableItemData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_items.clear();
		parseDatapackFile("data/ExtractableItems.xml");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			int id;
			int amount;
			int production;
			float totalChance;
			float chance;
			final StatSet set = new StatSet();
			final Node n = doc.getFirstChild();
			for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("item".equalsIgnoreCase(node.getNodeName()))
				{
					id = Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
					final List<ExtractableProductItem> extractables = new ArrayList<>();
					for (Node b = node.getFirstChild(); b != null; b = b.getNextSibling())
					{
						if ("extract".equalsIgnoreCase(b.getNodeName()))
						{
							final NamedNodeMap attrs = b.getAttributes();
							for (int i = 0; i < attrs.getLength(); i++)
							{
								final Node attr = attrs.item(i);
								set.set(attr.getNodeName(), attr.getNodeValue());
							}
							
							production = set.getInt("id");
							amount = set.getInt("quantity");
							chance = set.getFloat("chance");
							extractables.add(new ExtractableProductItem(production, amount, chance));
							totalChance = 0;
							for (ExtractableProductItem extractable : extractables)
							{
								totalChance += extractable.getChance();
							}
							if (totalChance > 100)
							{
								LOGGER.info(getClass().getSimpleName() + ": Extractable with id " + id + " has was more than 100% total chance!");
							}
						}
					}
					_items.put(id, new ExtractableItem(id, extractables));
				}
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _items.size() + " extractable items.");
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Error while loading extractable items! " + e);
		}
	}
	
	public ExtractableItem getExtractableItem(int itemId)
	{
		return _items.get(itemId);
	}
	
	public int[] getAllItemIds()
	{
		int index = 0;
		final int[] ids = new int[_items.size()];
		for (ExtractableItem extractable : _items.values())
		{
			ids[index++] = extractable.getItemId();
		}
		return ids;
	}
	
	public static ExtractableItemData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ExtractableItemData INSTANCE = new ExtractableItemData();
	}
}
