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

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.clan.Clan;

/**
 * @author Layane
 */
public class CrestCache
{
	private static final Logger LOGGER = Logger.getLogger(CrestCache.class.getName());
	
	private final Map<Integer, byte[]> _cachePledge = new HashMap<>();
	private final Map<Integer, byte[]> _cachePledgeLarge = new HashMap<>();
	private final Map<Integer, byte[]> _cacheAlly = new HashMap<>();
	private int _loadedFiles;
	private long _bytesBuffLen;
	
	private CrestCache()
	{
		convertOldPedgeFiles();
		reload();
	}
	
	public void reload()
	{
		final FileFilter filter = new BmpFilter();
		final File dir = new File(Config.DATAPACK_ROOT, "data/crests/");
		final File[] files = dir.listFiles(filter);
		byte[] content;
		synchronized (this)
		{
			_loadedFiles = 0;
			_bytesBuffLen = 0;
			_cachePledge.clear();
			_cachePledgeLarge.clear();
			_cacheAlly.clear();
		}
		
		for (File file : files)
		{
			RandomAccessFile f = null;
			synchronized (this)
			{
				try
				{
					f = new RandomAccessFile(file, "r");
					content = new byte[(int) f.length()];
					f.readFully(content);
					
					if (file.getName().startsWith("Crest_Large_"))
					{
						_cachePledgeLarge.put(Integer.parseInt(file.getName().substring(12, file.getName().length() - 4)), content);
					}
					else if (file.getName().startsWith("Crest_"))
					{
						_cachePledge.put(Integer.parseInt(file.getName().substring(6, file.getName().length() - 4)), content);
					}
					else if (file.getName().startsWith("AllyCrest_"))
					{
						_cacheAlly.put(Integer.parseInt(file.getName().substring(10, file.getName().length() - 4)), content);
					}
					
					_loadedFiles++;
					_bytesBuffLen += content.length;
				}
				catch (Exception e)
				{
					LOGGER.warning("problem with crest bmp file " + e);
				}
				finally
				{
					if (f != null)
					{
						try
						{
							f.close();
						}
						catch (Exception e1)
						{
							LOGGER.warning("Problem with CrestCache: " + e1.getMessage());
						}
					}
				}
			}
		}
		
		LOGGER.info("Cache[Crest]: " + String.format("%.3f", getMemoryUsage()) + "MB on " + _loadedFiles + " files loaded.");
	}
	
	public void convertOldPedgeFiles()
	{
		final File dir = new File(Config.DATAPACK_ROOT, "data/crests/");
		final File[] files = dir.listFiles(new OldPledgeFilter());
		if (files == null)
		{
			LOGGER.info("No old crest files found in \"data/crests/\"!!! May be you deleted them?");
			return;
		}
		
		for (File file : files)
		{
			final int clanId = Integer.parseInt(file.getName().substring(7, file.getName().length() - 4));
			
			LOGGER.info("Found old crest file \"" + file.getName() + "\" for clanId " + clanId);
			
			final int newId = IdFactory.getNextId();
			final Clan clan = ClanTable.getInstance().getClan(clanId);
			if (clan != null)
			{
				removeOldPledgeCrest(clan.getCrestId());
				
				file.renameTo(new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + newId + ".bmp"));
				LOGGER.info("Renamed Clan crest to new format: Crest_" + newId + ".bmp");
				
				try (Connection con = DatabaseFactory.getConnection())
				{
					final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
					statement.setInt(1, newId);
					statement.setInt(2, clan.getClanId());
					statement.executeUpdate();
					statement.close();
				}
				catch (SQLException e)
				{
					LOGGER.warning("could not update the crest id:" + e.getMessage());
				}
				
				clan.setCrestId(newId);
				clan.setHasCrest(true);
			}
			else
			{
				LOGGER.info("Clan Id: " + clanId + " does not exist in table.. deleting.");
				file.delete();
			}
		}
	}
	
	public float getMemoryUsage()
	{
		return (float) _bytesBuffLen / 1048576;
	}
	
	public int getLoadedFiles()
	{
		return _loadedFiles;
	}
	
	public byte[] getPledgeCrest(int id)
	{
		return _cachePledge.get(id);
	}
	
	public byte[] getPledgeCrestLarge(int id)
	{
		return _cachePledgeLarge.get(id);
	}
	
	public byte[] getAllyCrest(int id)
	{
		return _cacheAlly.get(id);
	}
	
	public void removePledgeCrest(int id)
	{
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + id + ".bmp");
		_cachePledge.remove(id);
		
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
		}
	}
	
	public void removePledgeCrestLarge(int id)
	{
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + id + ".bmp");
		_cachePledgeLarge.remove(id);
		
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
		}
	}
	
	public void removeOldPledgeCrest(int id)
	{
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Pledge_" + id + ".bmp");
		
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
		}
	}
	
	public void removeAllyCrest(int id)
	{
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + id + ".bmp");
		_cacheAlly.remove(id);
		
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
		}
	}
	
	public boolean savePledgeCrest(int newId, byte[] data)
	{
		boolean output = false;
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cachePledge.put(newId, data);
			
			output = true;
		}
		catch (IOException e)
		{
			LOGGER.warning("Error saving pledge crest" + crestFile + " " + e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					LOGGER.warning("Problem with CrestCache: " + e.getMessage());
				}
			}
		}
		return output;
	}
	
	public boolean savePledgeCrestLarge(int newId, byte[] data)
	{
		boolean output = false;
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Crest_Large_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cachePledgeLarge.put(newId, data);
			
			output = true;
		}
		catch (IOException e)
		{
			LOGGER.warning("Error saving Large pledge crest" + crestFile + " " + e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					LOGGER.warning("Problem with CrestCache: " + e.getMessage());
				}
			}
		}
		return output;
	}
	
	public boolean saveAllyCrest(int newId, byte[] data)
	{
		boolean output = false;
		final File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/AllyCrest_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_cacheAlly.put(newId, data);
			
			output = true;
		}
		catch (IOException e)
		{
			LOGGER.warning("Error saving ally crest" + crestFile + " " + e);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					LOGGER.warning("Problem with CrestCache: " + e.getMessage());
				}
			}
		}
		return output;
	}
	
	class BmpFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			return file.getName().endsWith(".bmp");
		}
	}
	
	class OldPledgeFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			return file.getName().startsWith("Pledge_");
		}
	}
	
	public static CrestCache getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CrestCache INSTANCE = new CrestCache();
	}
}
