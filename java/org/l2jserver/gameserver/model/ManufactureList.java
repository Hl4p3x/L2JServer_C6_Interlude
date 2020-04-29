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
package org.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:33 $
 */
public class ManufactureList
{
	private List<ManufactureItem> _list;
	private boolean _confirmed;
	private String _manufactureStoreName;
	
	public ManufactureList()
	{
		_list = new ArrayList<>();
		_confirmed = false;
	}
	
	public int size()
	{
		return _list.size();
	}
	
	public void setConfirmedTrade(boolean x)
	{
		_confirmed = x;
	}
	
	public boolean hasConfirmed()
	{
		return _confirmed;
	}
	
	/**
	 * @param manufactureStoreName
	 */
	public void setStoreName(String manufactureStoreName)
	{
		_manufactureStoreName = manufactureStoreName;
	}
	
	/**
	 * @return Returns the _manufactureStoreName.
	 */
	public String getStoreName()
	{
		return _manufactureStoreName;
	}
	
	public void add(ManufactureItem item)
	{
		_list.add(item);
	}
	
	public List<ManufactureItem> getList()
	{
		return _list;
	}
	
	public void setList(List<ManufactureItem> list)
	{
		_list = list;
	}
}
