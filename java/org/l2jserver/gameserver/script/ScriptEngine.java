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
package org.l2jserver.gameserver.script;

import java.util.HashMap;
import java.util.Map;

import org.l2jserver.gameserver.script.faenor.FaenorInterface;

/**
 * @author Luis Arias
 */
public class ScriptEngine
{
	protected EngineInterface _utils = FaenorInterface.getInstance();
	public static final Map<String, ParserFactory> parserFactories = new HashMap<>();
	
	protected static Parser createParser(String name) throws ParserNotCreatedException
	{
		ParserFactory s = parserFactories.get(name);
		if (s == null) // Shape not found.
		{
			try
			{
				Class.forName("org.l2jserver.gameserver.script." + name);
				// By now the static block with no function would have been executed if the shape was found. The shape is expected to have put its factory in the hashtable.
				s = parserFactories.get(name);
				if (s == null) // If the shape factory is not there even now.
				{
					throw new ParserNotCreatedException();
				}
			}
			catch (ClassNotFoundException e)
			{
				// We'll throw an exception to indicate that the shape could not be created.
				throw new ParserNotCreatedException();
			}
		}
		return s.create();
	}
}
