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

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import org.l2jserver.gameserver.model.PageResult;

/**
 * A class containing useful methods for constructing HTML
 * @author NosBit
 */
public class HtmlUtil
{
	public static <T> PageResult createPage(Collection<T> elements, int page, int elementsPerPage, Function<Integer, String> pagerFunction, Function<T, String> bodyFunction)
	{
		return createPage(elements, elements.size(), page, elementsPerPage, pagerFunction, bodyFunction);
	}
	
	public static <T> PageResult createPage(T[] elements, int page, int elementsPerPage, Function<Integer, String> pagerFunction, Function<T, String> bodyFunction)
	{
		return createPage(Arrays.asList(elements), elements.length, page, elementsPerPage, pagerFunction, bodyFunction);
	}
	
	public static <T> PageResult createPage(Iterable<T> elements, int size, int page, int elementsPerPage, Function<Integer, String> pagerFunction, Function<T, String> bodyFunction)
	{
		int pages = size / elementsPerPage;
		if ((elementsPerPage * pages) < size)
		{
			pages++;
		}
		
		final StringBuilder pagerTemplate = new StringBuilder();
		if (pages > 1)
		{
			int breakit = 0;
			for (int i = 0; i < pages; i++)
			{
				pagerTemplate.append(pagerFunction.apply(i));
				breakit++;
				
				if (breakit > 5)
				{
					pagerTemplate.append("</tr><tr>");
					breakit = 0;
				}
			}
		}
		
		if (page >= pages)
		{
			page = pages - 1;
		}
		
		final int start = page > 0 ? elementsPerPage * page : 0;
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (T element : elements)
		{
			if (i++ < start)
			{
				continue;
			}
			
			sb.append(bodyFunction.apply(element));
			
			if (i >= (elementsPerPage + start))
			{
				break;
			}
		}
		return new PageResult(pages, pagerTemplate, sb);
	}
}