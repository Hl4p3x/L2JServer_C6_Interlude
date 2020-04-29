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
package org.l2jserver.gameserver.model.multisell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Armor;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.serverpackets.MultiSellList;

/**
 * Multisell list manager
 * @author programmos
 */
public class Multisell
{
	private static final Logger LOGGER = Logger.getLogger(Multisell.class.getName());
	private final List<MultiSellListContainer> _entries = new ArrayList<>();
	
	public MultiSellListContainer getList(int id)
	{
		synchronized (_entries)
		{
			for (MultiSellListContainer list : _entries)
			{
				if (list.getListId() == id)
				{
					return list;
				}
			}
		}
		
		LOGGER.warning("[L2Multisell] can't find list with id: " + id);
		return null;
	}
	
	private Multisell()
	{
		parseData();
	}
	
	public void reload()
	{
		parseData();
	}
	
	private void parseData()
	{
		_entries.clear();
		parse();
	}
	
	/**
	 * This will generate the multisell list for the items. There exist various parameters in multisells that affect the way they will appear: 1) inventory only: * if true, only show items of the multisell for which the "primary" ingredients are already in the player's inventory. By "primary"
	 * ingredients we mean weapon and armor. * if false, show the entire list. 2) maintain enchantment: presumably, only lists with "inventory only" set to true should sometimes have this as true. This makes no sense otherwise... * If true, then the product will match the enchantment level of the
	 * ingredient. if the player has multiple items that match the ingredient list but the enchantment levels differ, then the entries need to be duplicated to show the products and ingredients for each enchantment level. For example: If the player has a crystal staff +1 and a crystal staff +3 and
	 * goes to exchange it at the mammon, the list should have all exchange possibilities for the +1 staff, followed by all possibilities for the +3 staff. * If false, then any level ingredient will be considered equal and product will always be at +0 3) apply taxes: Uses the "taxIngredient" entry
	 * in order to add a certain amount of adena to the ingredients
	 * @param listId
	 * @param inventoryOnly
	 * @param player
	 * @param taxRate
	 * @return
	 */
	private MultiSellListContainer generateMultiSell(int listId, boolean inventoryOnly, PlayerInstance player, double taxRate)
	{
		final MultiSellListContainer listTemplate = getInstance().getList(listId);
		MultiSellListContainer list = new MultiSellListContainer();
		if (listTemplate == null)
		{
			return list;
		}
		
		list = new MultiSellListContainer();
		list.setListId(listId);
		
		if (inventoryOnly)
		{
			if (player == null)
			{
				return list;
			}
			
			ItemInstance[] items;
			if (listTemplate.getMaintainEnchantment())
			{
				items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false, true);
			}
			else
			{
				items = player.getInventory().getUniqueItems(false, false, false, true);
			}
			
			int enchantLevel;
			for (ItemInstance item : items)
			{
				// Only do the matchup on equipable items that are not currently equipped so for each appropriate item, produce a set of entries for the multisell list.
				if (!item.isWear() && ((item.getItem() instanceof Armor) || (item.getItem() instanceof Weapon)))
				{
					enchantLevel = listTemplate.getMaintainEnchantment() ? item.getEnchantLevel() : 0;
					// loop through the entries to see which ones we wish to include
					for (MultiSellEntry ent : listTemplate.getEntries())
					{
						boolean doInclude = false;
						
						// check ingredients of this entry to see if it's an entry we'd like to include.
						for (MultiSellIngredient ing : ent.getIngredients())
						{
							if (item.getItemId() == ing.getItemId())
							{
								doInclude = true;
								break;
							}
						}
						
						// manipulate the ingredients of the template entry for this particular instance shown
						// i.e: Assign enchant levels and/or apply taxes as needed.
						if (doInclude)
						{
							list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), listTemplate.getMaintainEnchantment(), enchantLevel, taxRate));
						}
					}
				}
			}
		}
		else // this is a list-all type
		{
			// if no taxes are applied, no modifications are needed
			for (MultiSellEntry ent : listTemplate.getEntries())
			{
				list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), false, 0, taxRate));
			}
		}
		
		return list;
	}
	
	// Regarding taxation, the following is the case:
	// a) The taxes come out purely from the adena TaxIngredient
	// b) If the entry has no adena ingredients other than the taxIngredient, the resulting amount of adena is appended to the entry
	// c) If the entry already has adena as an entry, the taxIngredient is used in order to increase the count for the existing adena ingredient
	private MultiSellEntry prepareEntry(MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel, double taxRate)
	{
		final MultiSellEntry newEntry = new MultiSellEntry();
		newEntry.setEntryId((templateEntry.getEntryId() * 100000) + enchantLevel);
		int adenaAmount = 0;
		for (MultiSellIngredient ing : templateEntry.getIngredients())
		{
			// load the ingredient from the template
			final MultiSellIngredient newIngredient = new MultiSellIngredient(ing);
			
			// if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
			if ((ing.getItemId() == 57) && ing.isTaxIngredient())
			{
				if (applyTaxes)
				{
					adenaAmount += (int) Math.round(ing.getItemCount() * taxRate);
				}
				continue; // do not adena yet, as non-taxIngredient adena entries might occur next (order not guaranteed)
			}
			else if (ing.getItemId() == 57) // && !ing.isTaxIngredient()
			{
				adenaAmount += ing.getItemCount();
				continue; // do not adena yet, as taxIngredient adena entries might occur next (order not guaranteed)
			}
			// if it is an armor/weapon, modify the enchantment level appropriately, if necessary
			else if (maintainEnchantment)
			{
				final Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
				if ((tempItem instanceof Armor) || (tempItem instanceof Weapon))
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
				}
			}
			
			// finally, add this ingredient to the entry
			newEntry.addIngredient(newIngredient);
		}
		
		// now add the adena, if any.
		if (adenaAmount > 0)
		{
			newEntry.addIngredient(new MultiSellIngredient(57, adenaAmount, 0, false, false));
		}
		
		// Now modify the enchantment level of products, if necessary
		for (MultiSellIngredient ing : templateEntry.getProducts())
		{
			// load the ingredient from the template
			final MultiSellIngredient newIngredient = new MultiSellIngredient(ing);
			if (maintainEnchantment)
			{
				// if it is an armor/weapon, modify the enchantment level appropriately (note, if maintain enchantment is "false" this modification will result to a +0)
				final Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
				if ((tempItem instanceof Armor) || (tempItem instanceof Weapon))
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
				}
			}
			
			newEntry.addProduct(newIngredient);
		}
		
		return newEntry;
	}
	
	public void SeparateAndSend(int listId, PlayerInstance player, boolean inventoryOnly, double taxRate)
	{
		final MultiSellListContainer list = generateMultiSell(listId, inventoryOnly, player, taxRate);
		MultiSellListContainer temp = new MultiSellListContainer();
		int page = 1;
		temp.setListId(list.getListId());
		
		for (MultiSellEntry e : list.getEntries())
		{
			if (temp.getEntries().size() == 40)
			{
				player.sendPacket(new MultiSellList(temp, page, 0));
				page++;
				temp = new MultiSellListContainer();
				temp.setListId(list.getListId());
			}
			
			temp.addEntry(e);
		}
		
		player.setMultiSellId(listId);
		
		player.sendPacket(new MultiSellList(temp, page, 1));
	}
	
	private void hashFiles(String dirname, List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if (!dir.exists())
		{
			LOGGER.warning("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		
		final File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml"))
			{
				hash.add(f);
			}
		}
	}
	
	private void parse()
	{
		Document doc = null;
		int id = 0;
		
		final List<File> files = new ArrayList<>();
		hashFiles("multisell", files);
		for (File f : files)
		{
			id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
			try
			{
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				LOGGER.warning("Error loading file " + f + " " + e);
			}
			try
			{
				final MultiSellListContainer list = parseDocument(doc);
				list.setListId(id);
				
				updateReferencePrice(list);
				
				_entries.add(list);
			}
			catch (Exception e)
			{
				LOGGER.warning("Error in file " + f + " " + e);
			}
		}
	}
	
	protected MultiSellListContainer parseDocument(Document doc)
	{
		final MultiSellListContainer list = new MultiSellListContainer();
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				Node attribute;
				attribute = n.getAttributes().getNamedItem("applyTaxes");
				if (attribute == null)
				{
					list.setApplyTaxes(false);
				}
				else
				{
					list.setApplyTaxes(Boolean.parseBoolean(attribute.getNodeValue()));
				}
				
				attribute = n.getAttributes().getNamedItem("maintainEnchantment");
				if (attribute == null)
				{
					list.setMaintainEnchantment(false);
				}
				else
				{
					list.setMaintainEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));
				}
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						final MultiSellEntry e = parseEntry(d);
						list.addEntry(e);
					}
				}
			}
			else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				final MultiSellEntry e = parseEntry(n);
				list.addEntry(e);
			}
		}
		
		return list;
	}
	
	protected MultiSellEntry parseEntry(Node n)
	{
		final int entryId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		final Node first = n.getFirstChild();
		final MultiSellEntry entry = new MultiSellEntry();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("ingredient".equalsIgnoreCase(n.getNodeName()))
			{
				Node attribute;
				
				final int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				final int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
				boolean isTaxIngredient = false;
				boolean mantainIngredient = false;
				attribute = n.getAttributes().getNamedItem("isTaxIngredient");
				if (attribute != null)
				{
					isTaxIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}
				
				attribute = n.getAttributes().getNamedItem("mantainIngredient");
				if (attribute != null)
				{
					mantainIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}
				
				final MultiSellIngredient e = new MultiSellIngredient(id, count, isTaxIngredient, mantainIngredient);
				entry.addIngredient(e);
			}
			else if ("production".equalsIgnoreCase(n.getNodeName()))
			{
				final int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				final int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
				if (ItemTable.getInstance().getTemplate(id) == null)
				{
					LOGGER.warning("Multisell: Item " + id + " does not exist.");
				}
				
				int enchant = 0;
				// By Azagthtot support enchantment in multisell
				if (n.getAttributes().getNamedItem("enchant") != null)
				{
					enchant = Integer.parseInt(n.getAttributes().getNamedItem("enchant").getNodeValue());
				}
				final MultiSellIngredient e = new MultiSellIngredient(id, count, enchant, false, false);
				entry.addProduct(e);
			}
		}
		
		entry.setEntryId(entryId);
		
		return entry;
	}
	
	/**
	 * This method checks and update the container to avoid possible items buy/sell duplication. Currently support ADENA check.
	 * @param container
	 */
	private void updateReferencePrice(MultiSellListContainer container)
	{
		for (MultiSellEntry entry : container.getEntries())
		{
			// if ingredient is just 1 and is adena
			if ((entry.getIngredients().size() == 1) && (entry.getIngredients().get(0).getItemId() == 57))
			{
				// the buy price must necessarily higher then total reference item price / 2 that is the default sell price
				int totalProductReferencePrice = 0;
				for (MultiSellIngredient product : entry.getProducts())
				{
					totalProductReferencePrice += (ItemTable.getInstance().getTemplate(product.getItemId()).getReferencePrice() * product.getItemCount());
				}
				
				if (entry.getIngredients().get(0).getItemCount() < (totalProductReferencePrice / 2))
				{
					LOGGER.warning("Multisell " + container.getListId() + " entryId  " + entry.getEntryId() + " has an ADENA price less then total products reference price.. Automatically Updating it..");
					entry.getIngredients().get(0).setItemCount(totalProductReferencePrice);
				}
			}
		}
	}
	
	public static Multisell getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Multisell INSTANCE = new Multisell();
	}
}
