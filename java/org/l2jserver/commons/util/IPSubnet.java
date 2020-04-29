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
package org.l2jserver.commons.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPSubnet
{
	private final byte[] _addr;
	private final byte[] _mask;
	private final boolean _isIPv4;
	
	public IPSubnet(String input) throws UnknownHostException
	{
		final int idx = input.indexOf('/');
		if (idx > 0)
		{
			_addr = InetAddress.getByName(input.substring(0, idx)).getAddress();
			_mask = getMask(Integer.parseInt(input.substring(idx + 1)), _addr.length);
			_isIPv4 = _addr.length == 4;
			if (!applyMask(_addr))
			{
				throw new UnknownHostException(input);
			}
		}
		else
		{
			_addr = InetAddress.getByName(input).getAddress();
			_mask = getMask(_addr.length * 8, _addr.length); // host, no need to check mask
			_isIPv4 = _addr.length == 4;
		}
	}
	
	public byte[] getAddress()
	{
		return _addr;
	}
	
	private boolean applyMask(byte[] addr)
	{
		// V4 vs V4 or V6 vs V6 checks
		if (_isIPv4 == (addr.length == 4))
		{
			for (int i = 0; i < _addr.length; i++)
			{
				if ((addr[i] & _mask[i]) != _addr[i])
				{
					return false;
				}
			}
		}
		// check for embedded v4 in v6 addr (not done !)
		else if (_isIPv4)
		{
			// my V4 vs V6
			for (int i = 0; i < _addr.length; i++)
			{
				if ((addr[i + 12] & _mask[i]) != _addr[i])
				{
					return false;
				}
			}
		}
		else
		{
			// my V6 vs V4
			for (int i = 0; i < _addr.length; i++)
			{
				if ((addr[i] & _mask[i + 12]) != _addr[i + 12])
				{
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		int size = 0;
		for (byte element : _mask)
		{
			size += Integer.bitCount(element & 0xFF);
		}
		
		try
		{
			return InetAddress.getByAddress(_addr) + "/" + size;
		}
		catch (UnknownHostException e)
		{
			return "Invalid";
		}
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o instanceof IPSubnet)
		{
			return applyMask(((IPSubnet) o).getAddress());
		}
		return (o instanceof InetAddress) && applyMask(((InetAddress) o).getAddress());
	}
	
	private static byte[] getMask(int n, int maxLength) throws UnknownHostException
	{
		if ((n > (maxLength << 3)) || (n < 0))
		{
			throw new UnknownHostException("Invalid netmask: " + n);
		}
		
		final byte[] result = new byte[maxLength];
		for (int i = 0; i < maxLength; i++)
		{
			result[i] = (byte) 0xFF;
		}
		
		for (int i = (maxLength << 3) - 1; i >= n; i--)
		{
			result[i >> 3] = (byte) (result[i >> 3] << 1);
		}
		
		return result;
	}
}