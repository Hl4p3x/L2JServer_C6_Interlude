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
package org.l2jserver.gameserver.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This is a class loader for the dynamic extensions used by DynamicExtension class.
 * @version $Revision: $ $Date: $
 * @author galun
 */
public class JarClassLoader extends ClassLoader
{
	private final HashSet<String> _jars = new HashSet<>();
	
	public void addJarFile(String filename)
	{
		_jars.add(filename);
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException
	{
		try
		{
			final byte[] b = loadClassData(name);
			return defineClass(name, b, 0, b.length);
		}
		catch (Exception e)
		{
			throw new ClassNotFoundException(name);
		}
	}
	
	private byte[] loadClassData(String name) throws IOException
	{
		byte[] classData = null;
		for (String jarFile : _jars)
		{
			boolean breakable = false;
			final File file = new File(jarFile);
			ZipFile zipFile = null;
			InputStream is = null;
			DataInputStream zipStream = null;
			
			try
			{
				zipFile = new ZipFile(file);
				
				final String fileName = name.replace('.', '/') + ".class";
				final ZipEntry entry = zipFile.getEntry(fileName);
				if (entry == null)
				{
					continue;
				}
				
				classData = new byte[(int) entry.getSize()];
				is = zipFile.getInputStream(entry);
				zipStream = new DataInputStream(is);
				zipStream.readFully(classData, 0, (int) entry.getSize());
				breakable = true;
			}
			catch (ZipException e2)
			{
				e2.printStackTrace();
			}
			catch (IOException e2)
			{
				e2.printStackTrace();
			}
			finally
			{
				if (zipStream != null)
				{
					try
					{
						zipStream.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			if (breakable)
			{
				break;
			}
		}
		
		if (classData == null)
		{
			throw new IOException("class not found in " + _jars);
		}
		
		return classData;
	}
}
