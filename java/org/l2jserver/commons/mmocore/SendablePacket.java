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

/**
 * @author KenM
 * @param <T>
 */
public abstract class SendablePacket<T extends MMOClient<?>>extends AbstractPacket<T>
{
	protected final void putInt(int value)
	{
		_buf.putInt(value);
	}
	
	protected final void putDouble(double value)
	{
		_buf.putDouble(value);
	}
	
	protected final void putFloat(float value)
	{
		_buf.putFloat(value);
	}
	
	/**
	 * Write <b>byte</b> to the buffer.<br>
	 * 8bit integer (00)
	 * @param data
	 */
	protected final void writeC(int data)
	{
		_buf.put((byte) data);
	}
	
	/**
	 * Write <b>double</b> to the buffer.<br>
	 * 64bit double precision float (00 00 00 00 00 00 00 00)
	 * @param value
	 */
	protected final void writeF(double value)
	{
		_buf.putDouble(value);
	}
	
	/**
	 * Write <b>short</b> to the buffer.<br>
	 * 16bit integer (00 00)
	 * @param value
	 */
	protected final void writeH(int value)
	{
		_buf.putShort((short) value);
	}
	
	/**
	 * Write <b>int</b> to the buffer.<br>
	 * 32bit integer (00 00 00 00)
	 * @param value
	 */
	protected final void writeD(int value)
	{
		_buf.putInt(value);
	}
	
	/**
	 * Write <b>long</b> to the buffer.<br>
	 * 64bit integer (00 00 00 00 00 00 00 00)
	 * @param value
	 */
	protected final void writeQ(long value)
	{
		_buf.putLong(value);
	}
	
	/**
	 * Write <b>byte[]</b> to the buffer.<br>
	 * 8bit integer array (00 ...)
	 * @param data
	 */
	protected final void writeB(byte[] data)
	{
		_buf.put(data);
	}
	
	/**
	 * Write <b>String</b> to the buffer.
	 * @param text
	 */
	protected final void writeS(String text)
	{
		if (text != null)
		{
			final int len = text.length();
			for (int i = 0; i < len; i++)
			{
				_buf.putChar(text.charAt(i));
			}
		}
		
		_buf.putChar('\000');
	}
	
	protected abstract void write();
}