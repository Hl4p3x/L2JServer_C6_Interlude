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

import org.l2jserver.Config;
import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.instance.BoatInstance;
import org.l2jserver.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jserver.gameserver.model.holders.BoatPathHolder.BoatPoint;

/**
 * @author Mobius
 */
public class BoatData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(BoatData.class.getName());
	
	private final Map<Integer, BoatInstance> _boats = new HashMap<>();
	
	protected BoatData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_boats.clear();
		parseDatapackFile("data/Boats.xml");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		if (!Config.ALLOW_BOAT)
		{
			return;
		}
		
		try
		{
			final StatSet set = new StatSet();
			final Map<Integer, List<BoatPoint>> paths = new HashMap<>();
			final Node n = doc.getFirstChild();
			for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("boat".equalsIgnoreCase(node.getNodeName()))
				{
					final NamedNodeMap attrs = node.getAttributes();
					for (int i = 0; i < attrs.getLength(); i++)
					{
						final Node attr = attrs.item(i);
						set.set(attr.getNodeName(), attr.getNodeValue());
					}
					
					final StatSet npcDat = new StatSet();
					npcDat.set("npcId", set.getInt("id"));
					npcDat.set("level", 0);
					npcDat.set("jClass", "boat");
					npcDat.set("baseSTR", 0);
					npcDat.set("baseCON", 0);
					npcDat.set("baseDEX", 0);
					npcDat.set("baseINT", 0);
					npcDat.set("baseWIT", 0);
					npcDat.set("baseMEN", 0);
					npcDat.set("baseShldDef", 0);
					npcDat.set("baseShldRate", 0);
					npcDat.set("baseAccCombat", 38);
					npcDat.set("baseEvasRate", 38);
					npcDat.set("baseCritRate", 38);
					npcDat.set("collision_radius", 0);
					npcDat.set("collision_height", 0);
					npcDat.set("sex", "male");
					npcDat.set("type", "");
					npcDat.set("baseAtkRange", 0);
					npcDat.set("baseMpMax", 0);
					npcDat.set("baseCpMax", 0);
					npcDat.set("rewardExp", 0);
					npcDat.set("rewardSp", 0);
					npcDat.set("basePAtk", 0);
					npcDat.set("baseMAtk", 0);
					npcDat.set("basePAtkSpd", 0);
					npcDat.set("aggroRange", 0);
					npcDat.set("baseMAtkSpd", 0);
					npcDat.set("rhand", 0);
					npcDat.set("lhand", 0);
					npcDat.set("armor", 0);
					npcDat.set("baseWalkSpd", 0);
					npcDat.set("baseRunSpd", 0);
					npcDat.set("name", set.getString("name"));
					npcDat.set("baseHpMax", 50000);
					npcDat.set("baseHpReg", 3.e-3f);
					npcDat.set("baseMpReg", 3.e-3f);
					npcDat.set("basePDef", 100);
					npcDat.set("baseMDef", 100);
					
					final CreatureTemplate template = new CreatureTemplate(npcDat);
					final BoatInstance boat = new BoatInstance(IdFactory.getNextId(), template);
					boat.getPosition().setHeading(set.getInt("heading"));
					boat.setXYZ(set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"));
					boat.setPathA(set.getInt("pathIdA"), set.getInt("ticketA"), set.getInt("xTeleNoTicketA"), set.getInt("yTeleNoTicketA"), set.getInt("zTeleNoTicketA"), set.getString("announcerA"), set.getString("message10A"), set.getString("message5A"), set.getString("message1A"), set.getString("message0A"), set.getString("messageBeginA"), paths.get(set.getInt("pathIdA")));
					boat.setPathB(set.getInt("pathIdB"), set.getInt("ticketB"), set.getInt("xTeleNoTicketB"), set.getInt("yTeleNoTicketB"), set.getInt("zTeleNoTicketB"), set.getString("announcerB"), set.getString("message10B"), set.getString("message5B"), set.getString("message1B"), set.getString("message0B"), set.getString("messageBeginB"), paths.get(set.getInt("pathIdB")));
					boat.spawn();
					
					_boats.put(boat.getObjectId(), boat);
				}
				else if ("path".equalsIgnoreCase(node.getNodeName()))
				{
					final List<BoatPoint> points = new ArrayList<>();
					for (Node b = node.getFirstChild(); b != null; b = b.getNextSibling())
					{
						if (!"point".equalsIgnoreCase(b.getNodeName()))
						{
							continue;
						}
						
						final NamedNodeMap attrs = b.getAttributes();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							final Node attr = attrs.item(i);
							set.set(attr.getNodeName(), attr.getNodeValue());
						}
						
						final BoatPoint point = new BoatPoint();
						point.speed1 = set.getInt("speed1");
						point.speed2 = set.getInt("speed2");
						point.x = set.getInt("x");
						point.y = set.getInt("y");
						point.z = set.getInt("z");
						point.time = set.getInt("time");
						points.add(point);
					}
					paths.put(Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()), points);
				}
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _boats.size() + " boats.");
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + paths.size() + " paths.");
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Error while reading boat data: " + e);
		}
	}
	
	public BoatInstance getBoat(int boatId)
	{
		return _boats.get(boatId);
	}
	
	public static final BoatData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BoatData INSTANCE = new BoatData();
	}
}
