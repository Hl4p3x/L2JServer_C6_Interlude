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
package org.l2jserver.tools.dbinstaller.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Prevent the underlying input stream to close.
 * @author Joe Cheng, Zoey76
 */
public class CloseShieldedInputStream extends InputStream
{
	private InputStream _in = null;
	
	/**
	 * Instantiates a new close shielded input stream.
	 * @param in the in
	 */
	public CloseShieldedInputStream(InputStream in)
	{
		_in = in;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close()
	{
		_in = null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.read();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b) throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.read(b);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.read(b, off, len);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long n) throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		return _in.skip(n);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void mark(int readlimit)
	{
		if (_in != null)
		{
			_in.mark(readlimit);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean markSupported()
	{
		return (_in != null) && _in.markSupported();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void reset() throws IOException
	{
		if (_in == null)
		{
			throw new IOException("Stream is null!");
		}
		_in.reset();
	}
	
	/**
	 * Gets the underlying stream.
	 * @return the underlying stream
	 */
	public InputStream getUnderlyingStream()
	{
		return _in;
	}
}
