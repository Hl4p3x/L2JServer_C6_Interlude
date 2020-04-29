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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.script.ScriptContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.l2jserver.gameserver.script.Parser;

/**
 * @author Luis Arias
 */
public abstract class FaenorParser extends Parser
{
	protected static FaenorInterface _bridge = FaenorInterface.getInstance();
	protected final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.US);
	
	public static String attribute(Node node, String attributeName)
	{
		return attribute(node, attributeName, null);
	}
	
	public static String element(Node node, String elementName)
	{
		return element(node, elementName, null);
	}
	
	public static String attribute(Node node, String attributeName, String defaultValue)
	{
		try
		{
			return node.getAttributes().getNamedItem(attributeName).getNodeValue();
		}
		catch (Exception e)
		{
			if (defaultValue != null)
			{
				return defaultValue;
			}
			throw new NullPointerException(e.getMessage());
		}
	}
	
	public static String element(Node parentNode, String elementName, String defaultValue)
	{
		try
		{
			final NodeList list = parentNode.getChildNodes();
			for (int i = 0; i < list.getLength(); i++)
			{
				final Node node = list.item(i);
				if (node.getNodeName().equalsIgnoreCase(elementName))
				{
					return node.getTextContent();
				}
			}
		}
		catch (Exception e)
		{
		}
		if (defaultValue != null)
		{
			return defaultValue;
		}
		throw new NullPointerException();
	}
	
	public static boolean isNodeName(Node node, String name)
	{
		return node.getNodeName().equalsIgnoreCase(name);
	}
	
	public Date getDate(String date) throws ParseException
	{
		return DATE_FORMAT.parse(date);
	}
	
	public static double getPercent(String percent)
	{
		return (Double.parseDouble(percent.split("%")[0]) / 100.0);
	}
	
	protected static int getInt(String number)
	{
		return Integer.parseInt(number);
	}
	
	protected static double getDouble(String number)
	{
		return Double.parseDouble(number);
	}
	
	protected static float getFloat(String number)
	{
		return Float.parseFloat(number);
	}
	
	protected static String getParserName(String name)
	{
		return "faenor.Faenor" + name + "Parser";
	}
	
	@Override
	public abstract void parseScript(Node node, ScriptContext context);
}
