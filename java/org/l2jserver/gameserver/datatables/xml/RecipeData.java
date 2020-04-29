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
package org.l2jserver.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jserver.commons.util.IXmlReader;
import org.l2jserver.gameserver.RecipeController;
import org.l2jserver.gameserver.model.RecipeList;
import org.l2jserver.gameserver.model.actor.instance.RecipeInstance;

public class RecipeData extends RecipeController implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(RecipeData.class.getName());
	
	private final Map<Integer, RecipeList> _lists = new HashMap<>();
	
	protected RecipeData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_lists.clear();
		parseDatapackFile("data/Recipes.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _lists.size() + " recipes.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			List<RecipeInstance> recipePartList = new ArrayList<>();
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("item".equalsIgnoreCase(d.getNodeName()))
				{
					recipePartList.clear();
					NamedNodeMap attrs = d.getAttributes();
					Node att = attrs.getNamedItem("id");
					if (att == null)
					{
						LOGGER.severe(getClass().getSimpleName() + ": Missing id for recipe item, skipping.");
						continue;
					}
					int id = Integer.parseInt(att.getNodeValue());
					att = attrs.getNamedItem("name");
					if (att == null)
					{
						LOGGER.severe(getClass().getSimpleName() + ": Missing name for recipe item id: " + id + ", skipping");
						continue;
					}
					String recipeName = att.getNodeValue();
					int recipeId = -1;
					int level = -1;
					boolean isDwarvenRecipe = true;
					int mpCost = -1;
					int successRate = -1;
					int prodId = -1;
					int count = -1;
					for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
					{
						if ("recipe".equalsIgnoreCase(c.getNodeName()))
						{
							recipeId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
							level = Integer.parseInt(c.getAttributes().getNamedItem("level").getNodeValue());
							isDwarvenRecipe = c.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("dwarven");
						}
						else if ("mpCost".equalsIgnoreCase(c.getNodeName()))
						{
							mpCost = Integer.parseInt(c.getTextContent());
						}
						else if ("successRate".equalsIgnoreCase(c.getNodeName()))
						{
							successRate = Integer.parseInt(c.getTextContent());
						}
						else if ("ingredient".equalsIgnoreCase(c.getNodeName()))
						{
							int ingId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
							int ingCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
							recipePartList.add(new RecipeInstance(ingId, ingCount));
						}
						else if ("production".equalsIgnoreCase(c.getNodeName()))
						{
							prodId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
							count = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
						}
					}
					
					final RecipeList recipeList = new RecipeList(id, level, recipeId, recipeName, successRate, mpCost, prodId, count, isDwarvenRecipe);
					for (RecipeInstance recipePart : recipePartList)
					{
						recipeList.addRecipe(recipePart);
					}
					
					_lists.put(id, recipeList);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Failed loading recipe list", e);
		}
	}
	
	public RecipeList getRecipe(int recipeId)
	{
		return _lists.get(recipeId);
	}
	
	public RecipeList getRecipeByItemId(int itemId)
	{
		for (RecipeList recipe : _lists.values())
		{
			if (recipe.getRecipeId() == itemId)
			{
				return recipe;
			}
		}
		return null;
	}
	
	public int[] getAllItemIds()
	{
		int index = 0;
		final int[] ids = new int[_lists.size()];
		for (RecipeList recipe : _lists.values())
		{
			ids[index++] = recipe.getRecipeId();
		}
		return ids;
	}
	
	public static RecipeData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RecipeData INSTANCE = new RecipeData();
	}
}
