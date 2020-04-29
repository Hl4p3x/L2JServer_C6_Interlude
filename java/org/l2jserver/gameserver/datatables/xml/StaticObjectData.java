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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.instance.StaticObjectInstance;
import org.l2jserver.gameserver.network.serverpackets.StaticObject;

/**
 * This class loads, stores and spawns {@link StaticObject}s.
 */
public class StaticObjectData implements IXmlReader
{
	private final Map<Integer, StaticObjectInstance> _objects = new HashMap<>();
	
	protected StaticObjectData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/StaticObjects.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _objects.size() + " static objects.");
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
			if (!"object".equalsIgnoreCase(node.getNodeName()))
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
			
			// Create and spawn the StaticObject instance.
			final StaticObjectInstance obj = new StaticObjectInstance(IdFactory.getNextId());
			obj.setType(set.getInt("type"));
			obj.setStaticObjectId(set.getInt("id"));
			obj.setXYZ(set.getInt("x"), set.getInt("y"), set.getInt("z"));
			obj.setMap(set.getString("texture"), set.getInt("mapX"), set.getInt("mapY"));
			obj.spawnMe();
			
			// Feed the map with new data.
			_objects.put(obj.getObjectId(), obj);
		}
	}
	
	public Collection<StaticObjectInstance> getStaticObjects()
	{
		return _objects.values();
	}
	
	public static StaticObjectData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final StaticObjectData INSTANCE = new StaticObjectData();
	}
}