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
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.model.NpcWalkerNode;
import org.l2jserver.gameserver.model.StatSet;

/**
 * @author Mobius
 */
public class WalkerRouteData implements IXmlReader
{
	protected static final Logger LOGGER = Logger.getLogger(WalkerRouteData.class.getName());
	
	private final Map<Integer, List<NpcWalkerNode>> _routes = new HashMap<>();
	
	protected WalkerRouteData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_routes.clear();
		parseDatapackFile("data/WalkerRoutes.xml");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			final Node n = doc.getFirstChild();
			for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("route".equalsIgnoreCase(node.getNodeName()))
				{
					final List<NpcWalkerNode> points = new ArrayList<>();
					for (Node b = node.getFirstChild(); b != null; b = b.getNextSibling())
					{
						if (!"point".equalsIgnoreCase(b.getNodeName()))
						{
							continue;
						}
						
						final StatSet set = new StatSet();
						final NamedNodeMap attrs = b.getAttributes();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							final Node attr = attrs.item(i);
							set.set(attr.getNodeName(), attr.getNodeValue());
						}
						
						final NpcWalkerNode route = new NpcWalkerNode();
						route.setMoveX(set.getInt("x"));
						route.setMoveY(set.getInt("y"));
						route.setMoveZ(set.getInt("z"));
						route.setDelay(set.getInt("delay"));
						route.setRunning(set.getBoolean("run"));
						route.setChatText(set.getString("chat", null));
						points.add(route);
					}
					_routes.put(Integer.parseInt(node.getAttributes().getNamedItem("npcId").getNodeValue()), points);
				}
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _routes.size() + " walker routes.");
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Error while reading walker route data: " + e);
		}
	}
	
	public List<NpcWalkerNode> getRouteForNpc(int id)
	{
		return _routes.get(id);
	}
	
	public static WalkerRouteData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final WalkerRouteData INSTANCE = new WalkerRouteData();
	}
}
