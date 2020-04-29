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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptContext;

import org.w3c.dom.Node;

import org.l2jserver.Config;
import org.l2jserver.commons.util.file.filter.XMLFilter;
import org.l2jserver.gameserver.script.Parser;
import org.l2jserver.gameserver.script.ParserNotCreatedException;
import org.l2jserver.gameserver.script.ScriptDocument;
import org.l2jserver.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 */
public class FaenorScriptEngine extends ScriptEngine
{
	private static final Logger _log = Logger.getLogger(FaenorScriptEngine.class.getName());
	public static final String PACKAGE_DIRECTORY = "data/faenor/";
	
	protected FaenorScriptEngine()
	{
		final File packDirectory = new File(Config.DATAPACK_ROOT, PACKAGE_DIRECTORY);
		final File[] files = packDirectory.listFiles(new XMLFilter());
		if (files != null)
		{
			for (File file : files)
			{
				try (InputStream in = new FileInputStream(file))
				{
					parseScript(new ScriptDocument(file.getName(), in), null);
				}
				catch (IOException e)
				{
					_log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
	}
	
	public void parseScript(ScriptDocument script, ScriptContext context)
	{
		final Node node = script.getDocument().getFirstChild();
		final String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";
		Parser parser = null;
		try
		{
			parser = createParser(parserClass);
		}
		catch (ParserNotCreatedException e)
		{
			_log.log(Level.WARNING, "ERROR: No parser registered for Script: " + parserClass + ": " + e.getMessage());
		}
		
		if (parser == null)
		{
			_log.warning("Unknown Script Type: " + script.getName());
			return;
		}
		
		try
		{
			parser.parseScript(node, context);
			_log.info(getClass().getSimpleName() + ": Loaded  " + script.getName() + " successfully.");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Script Parsing Failed: " + e.getMessage());
		}
	}
	
	public static FaenorScriptEngine getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FaenorScriptEngine INSTANCE = new FaenorScriptEngine();
	}
}
