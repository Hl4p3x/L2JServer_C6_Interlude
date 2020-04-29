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
package org.l2jserver.log.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.items.type.EtcItemType;

/**
 * @author Advi
 */
public class ItemFilter implements Filter
{
	// private String _excludeProcess;
	// private String _excludeItemType;
	
	// This is an example how to exclude consuming of shots and arrows from logging
	private static final String EXCLUDE_PROCESS = "Consume";
	private static final Set<EtcItemType> EXCLUDED_ITEM_TYPES = new HashSet<>();
	static
	{
		EXCLUDED_ITEM_TYPES.add(EtcItemType.ARROW);
		EXCLUDED_ITEM_TYPES.add(EtcItemType.SHOT);
	}
	
	@Override
	public boolean isLoggable(LogRecord record)
	{
		if (!"item".equals(record.getLoggerName()))
		{
			return false;
		}
		
		final String[] messageList = record.getMessage().split(":");
		return (messageList.length < 2) || !EXCLUDE_PROCESS.contains(messageList[1]) || !EXCLUDED_ITEM_TYPES.contains(((ItemInstance) record.getParameters()[0]).getItemType());
	}
}
