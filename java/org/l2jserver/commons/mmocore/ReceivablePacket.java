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
package org.l2jserver.commons.mmocore;

import java.nio.ByteBuffer;

/**
 * @author KenM
 * @param <T>
 */
public abstract class ReceivablePacket<T extends MMOClient<?>>extends AbstractPacket<T> implements Runnable
{
	NioNetStringBuffer _sbuf;
	
	protected ReceivablePacket()
	{
	}
	
	protected abstract boolean read();
	
	@Override
	public abstract void run();
	
	/**
	 * Reads <b>byte[]</b> from the buffer.<br>
	 * Reads as many bytes as the length of the array.
	 * @param dst : the byte array which will be filled with the data.
	 */
	protected final void readB(byte[] dst)
	{
		_buf.get(dst);
	}
	
	/**
	 * Reads <b>byte[]</b> from the buffer.<br>
	 * Reads as many bytes as the given length (len). Starts to fill the byte array from the given offset to <b>offset</b> + <b>len</b>.
	 * @param dst : the byte array which will be filled with the data.
	 * @param offset : starts to fill the byte array from the given offset.
	 * @param len : the given length of bytes to be read.
	 */
	protected final void readB(byte[] dst, int offset, int len)
	{
		_buf.get(dst, offset, len);
	}
	
	/**
	 * Reads <b>byte</b> from the buffer.<br>
	 * 8bit integer (00)
	 * @return
	 */
	protected final int readC()
	{
		return _buf.get() & 0xFF;
	}
	
	/**
	 * Reads <b>short</b> from the buffer.<br>
	 * 16bit integer (00 00)
	 * @return
	 */
	protected final int readH()
	{
		return _buf.getShort() & 0xFFFF;
	}
	
	/**
	 * Reads <b>int</b> from the buffer.<br>
	 * 32bit integer (00 00 00 00)
	 * @return
	 */
	protected final int readD()
	{
		return _buf.getInt();
	}
	
	/**
	 * Reads <b>long</b> from the buffer.<br>
	 * 64bit integer (00 00 00 00 00 00 00 00)
	 * @return
	 */
	protected final long readQ()
	{
		return _buf.getLong();
	}
	
	/**
	 * Reads <b>double</b> from the buffer.<br>
	 * 64bit double precision float (00 00 00 00 00 00 00 00)
	 * @return
	 */
	protected final double readF()
	{
		return _buf.getDouble();
	}
	
	/**
	 * Reads <b>String</b> from the buffer.
	 * @return
	 */
	protected final String readS()
	{
		_sbuf.clear();
		
		char ch;
		while ((ch = _buf.getChar()) != 0)
		{
			_sbuf.append(ch);
		}
		
		return _sbuf.toString();
	}
	
	/**
	 * packet forge purpose
	 * @param data
	 * @param client
	 * @param sBuffer
	 */
	public void setBuffers(ByteBuffer data, T client, NioNetStringBuffer sBuffer)
	{
		_buf = data;
		_client = client;
		_sbuf = sBuffer;
	}
}
