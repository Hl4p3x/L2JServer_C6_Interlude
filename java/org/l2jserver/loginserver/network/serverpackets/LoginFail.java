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
package org.l2jserver.loginserver.network.serverpackets;

/**
 * Fromat: d d: the failure reason
 */
public class LoginFail extends LoginServerPacket
{
	public enum LoginFailReason
	{
		REASON_SYSTEM_ERROR(0x01),
		REASON_PASS_WRONG(0x02),
		REASON_USER_OR_PASS_WRONG(0x03),
		REASON_ACCESS_FAILED(0x04),
		REASON_ACCOUNT_IN_USE(0x07),
		REASON_SERVER_OVERLOADED(0x0f),
		REASON_SERVER_MAINTENANCE(0x10),
		REASON_TEMP_PASS_EXPIRED(0x11),
		REASON_DUAL_BOX(0x23);
		
		private final int _code;
		
		LoginFailReason(int code)
		{
			_code = code;
		}
		
		public int getCode()
		{
			return _code;
		}
	}
	
	private final LoginFailReason _reason;
	
	public LoginFail(LoginFailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected void write()
	{
		writeC(0x01);
		writeD(_reason.getCode());
	}
}
