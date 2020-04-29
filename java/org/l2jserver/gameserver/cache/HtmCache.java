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
package org.l2jserver.gameserver.cache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.util.Util;

/**
 * @author Layane
 */
public class HtmCache
{
	private static final Logger LOGGER = Logger.getLogger(HtmCache.class.getName());
	
	private final Map<Integer, String> _cache;
	private int _loadedFiles;
	private long _bytesBuffLen;
	
	private HtmCache()
	{
		_cache = new HashMap<>();
		reload();
	}
	
	public void reload()
	{
		reload(Config.DATAPACK_ROOT);
	}
	
	public void reload(File f)
	{
		if (!Config.LAZY_CACHE)
		{
			LOGGER.info("Html cache start...");
			parseDir(f);
			LOGGER.info("Cache[HTML]: " + String.format("%.3f", getMemoryUsage()) + " megabytes on " + _loadedFiles + " files loaded");
		}
		else
		{
			_cache.clear();
			_loadedFiles = 0;
			_bytesBuffLen = 0;
			LOGGER.info("Cache[HTML]: Running lazy cache");
		}
	}
	
	public void reloadPath(File f)
	{
		parseDir(f);
		LOGGER.info("Cache[HTML]: Reloaded specified path.");
	}
	
	public double getMemoryUsage()
	{
		return (float) _bytesBuffLen / 1048576;
	}
	
	public int getLoadedFiles()
	{
		return _loadedFiles;
	}
	
	class HtmFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			if (!file.isDirectory())
			{
				return file.getName().endsWith(".htm") || file.getName().endsWith(".html");
			}
			return true;
		}
	}
	
	private void parseDir(File dir)
	{
		final FileFilter filter = new HtmFilter();
		final File[] files = dir.listFiles(filter);
		for (File file : files)
		{
			if (!file.isDirectory())
			{
				loadFile(file);
			}
			else
			{
				parseDir(file);
			}
		}
	}
	
	public String loadFile(File file)
	{
		final HtmFilter filter = new HtmFilter();
		String content = null;
		if (file.exists() && filter.accept(file) && !file.isDirectory())
		{
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			try
			{
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				final int bytes = bis.available();
				final byte[] raw = new byte[bytes];
				bis.read(raw);
				
				content = new String(raw, StandardCharsets.UTF_8);
				content = content.replaceAll("\r\n", "\n");
				content = content.replaceAll("(?s)<!--.*?-->", ""); // Remove html comments
				
				final String relpath = Util.getRelativePath(Config.DATAPACK_ROOT, file);
				final int hashcode = relpath.hashCode();
				if (Config.CHECK_HTML_ENCODING && !StandardCharsets.US_ASCII.newEncoder().canEncode(content))
				{
					LOGGER.warning("HTML encoding check: File " + relpath + " contains non ASCII content.");
				}
				
				final String oldContent = _cache.get(hashcode);
				if (oldContent == null)
				{
					_bytesBuffLen += bytes;
					_loadedFiles++;
				}
				else
				{
					_bytesBuffLen = (_bytesBuffLen - oldContent.length()) + bytes;
				}
				
				_cache.put(hashcode, content);
			}
			catch (Exception e)
			{
				LOGGER.warning("Problem with htm file " + e);
			}
			finally
			{
				if (bis != null)
				{
					try
					{
						bis.close();
					}
					catch (Exception e1)
					{
						LOGGER.warning("Problem with HtmCache: " + e1.getMessage());
					}
				}
				
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (Exception e1)
					{
						LOGGER.warning("Problem with HtmCache: " + e1.getMessage());
					}
				}
			}
		}
		
		return content;
	}
	
	public String getHtmForce(String path)
	{
		String content = getHtm(path);
		if (content == null)
		{
			content = "<html><body>My text is missing:<br>" + path + "</body></html>";
			LOGGER.warning("Cache[HTML]: Missing HTML page: " + path);
		}
		return content;
	}
	
	public String getHtm(String path)
	{
		String content = _cache.get(path.hashCode());
		if (Config.LAZY_CACHE && (content == null))
		{
			content = loadFile(new File(Config.DATAPACK_ROOT, path));
		}
		return content;
	}
	
	public boolean contains(String path)
	{
		return _cache.containsKey(path.hashCode());
	}
	
	/**
	 * Check if an HTM exists and can be loaded
	 * @param path The path to the HTM
	 * @return
	 */
	public boolean isLoadable(String path)
	{
		final File file = new File(path);
		final HtmFilter filter = new HtmFilter();
		return file.exists() && filter.accept(file) && !file.isDirectory();
	}
	
	public static HtmCache getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HtmCache INSTANCE = new HtmCache();
	}
}
