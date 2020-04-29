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
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jserver.gameserver.model.base.ClassId;

/**
 * @author Mobius
 */
public class PlayerTemplateData implements IXmlReader
{
	private final Map<Integer, PlayerTemplate> _templates = new HashMap<>();
	
	protected PlayerTemplateData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_templates.clear();
		parseDatapackFile("data/stats/playerTemplates.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _templates.size() + " player templates.");
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
			if (!"class".equalsIgnoreCase(node.getNodeName()))
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
			
			set.set("baseHpReg", 1.5);
			set.set("baseMpReg", 0.9);
			set.set("baseWalkSpd", 0);
			set.set("baseShldDef", 0);
			set.set("baseShldRate", 0);
			set.set("baseAtkRange", 40);
			set.set("baseCritRate", set.getInt("baseCritRate") / 10);
			
			final PlayerTemplate template = new PlayerTemplate(set);
			_templates.put(template.getClassId().getId(), template);
		}
	}
	
	public Collection<PlayerTemplate> getAllTemplates()
	{
		return _templates.values();
	}
	
	public PlayerTemplate getTemplate(int classId)
	{
		return _templates.get(classId);
	}
	
	public PlayerTemplate getTemplate(ClassId classId)
	{
		return _templates.get(classId.getId());
	}
	
	public String getClassNameById(int classId)
	{
		return _templates.get(classId).getClassName();
	}
	
	public int getClassIdByName(String className)
	{
		for (PlayerTemplate template : _templates.values())
		{
			if (template.getClassName().equalsIgnoreCase(className))
			{
				return template.getClassId().getId();
			}
		}
		return -1;
	}
	
	public static PlayerTemplateData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerTemplateData INSTANCE = new PlayerTemplateData();
	}
}
