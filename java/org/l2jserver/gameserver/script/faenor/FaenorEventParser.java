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
package org.l2jserver.gameserver.script.faenor;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptContext;

import org.w3c.dom.Node;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.script.DateRange;
import org.l2jserver.gameserver.script.IntList;
import org.l2jserver.gameserver.script.Parser;
import org.l2jserver.gameserver.script.ParserFactory;
import org.l2jserver.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 */
public class FaenorEventParser extends FaenorParser
{
	static Logger _log = Logger.getLogger(FaenorEventParser.class.getName());
	private DateRange _eventDates = null;
	
	@Override
	public void parseScript(Node eventNode, ScriptContext context)
	{
		final String id = attribute(eventNode, "ID");
		_eventDates = DateRange.parse(attribute(eventNode, "Active"), DATE_FORMAT);
		
		final Date currentDate = new Date();
		if (_eventDates.getEndDate().before(currentDate))
		{
			_log.info("Event ID: (" + id + ") has passed... Ignored.");
			return;
		}
		
		if (_eventDates.getStartDate().after(currentDate))
		{
			_log.info("Event ID: (" + id + ") is not active yet... Ignored.");
			ThreadPool.schedule(() -> parseEventDropAndMessage(eventNode), _eventDates.getStartDate().getTime() - currentDate.getTime());
			return;
		}
		
		parseEventDropAndMessage(eventNode);
	}
	
	protected void parseEventDropAndMessage(Node eventNode)
	{
		for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "DropList"))
			{
				parseEventDropList(node);
			}
			else if (isNodeName(node, "Message"))
			{
				parseEventMessage(node);
			}
		}
	}
	
	private void parseEventMessage(Node sysMsg)
	{
		try
		{
			final String type = attribute(sysMsg, "Type");
			final String[] message = attribute(sysMsg, "Msg").split(Config.EOL);
			if (type.equalsIgnoreCase("OnJoin"))
			{
				_bridge.onPlayerLogin(message, _eventDates);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error in event parser: " + e.getMessage());
		}
	}
	
	private void parseEventDropList(Node dropList)
	{
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "AllDrop"))
			{
				parseEventDrop(node);
			}
		}
	}
	
	private void parseEventDrop(Node drop)
	{
		try
		{
			final int[] items = IntList.parse(attribute(drop, "Items"));
			final int[] count = IntList.parse(attribute(drop, "Count"));
			final double chance = getPercent(attribute(drop, "Chance"));
			_bridge.addEventDrop(items, count, chance, _eventDates);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "ERROR(parseEventDrop):" + e.getMessage());
		}
	}
	
	static class FaenorEventParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return (new FaenorEventParser());
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("Event"), new FaenorEventParserFactory());
	}
}
