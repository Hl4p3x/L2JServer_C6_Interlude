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
package org.l2jserver.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.gameserver.datatables.ItemTable;
import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items with respective id, if num is not specified, assumes 1.
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminCreateItem.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_itemcreate",
		"admin_create_item",
		"admin_mass_create",
		"admin_create_coin"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case "admin_itemcreate":
			{
				AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				return true;
			}
			case "admin_create_item":
			{
				if (st.hasMoreTokens())
				{
					if (st.countTokens() == 2)
					{
						final String id = st.nextToken();
						final String num = st.nextToken();
						int idval = 0;
						int numval = 0;
						try
						{
							idval = Integer.parseInt(id);
							numval = Integer.parseInt(num);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
							return false;
						}
						if ((idval > 0) && (numval > 0))
						{
							createItem(activeChar, idval, numval);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
					else if (st.countTokens() == 1)
					{
						final String id = st.nextToken();
						int idval = 0;
						try
						{
							idval = Integer.parseInt(id);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
							return false;
						}
						if (idval > 0)
						{
							createItem(activeChar, idval, 1);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
				}
				else
				{
					AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
					return true;
				}
				return false;
			}
			case "admin_mass_create":
			{
				if (st.hasMoreTokens())
				{
					if (st.countTokens() == 2)
					{
						final String id = st.nextToken();
						final String num = st.nextToken();
						int idval = 0;
						int numval = 0;
						try
						{
							idval = Integer.parseInt(id);
							numval = Integer.parseInt(num);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
							return false;
						}
						if ((idval > 0) && (numval > 0))
						{
							massCreateItem(activeChar, idval, numval);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
						return false;
					}
					else if (st.countTokens() == 1)
					{
						final String id = st.nextToken();
						int idval = 0;
						try
						{
							idval = Integer.parseInt(id);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
							return false;
						}
						if (idval > 0)
						{
							massCreateItem(activeChar, idval, 1);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
						return false;
					}
				}
				return false;
			}
			case "admin_create_coin":
			{
				try
				{
					final int id = getCoinId(st.nextToken());
					if (id <= 0)
					{
						activeChar.sendMessage("Usage: //create_coin <name> [amount]");
						return false;
					}
					
					createItem(activeChar, id, (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1);
					AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //create_coin <name> [amount]");
				}
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
	
	private static int getCoinId(String name)
	{
		if (name.equalsIgnoreCase("adena"))
		{
			return 57;
		}
		
		if (name.equalsIgnoreCase("ancientadena"))
		{
			return 5575;
		}
		
		if (name.equalsIgnoreCase("festivaladena"))
		{
			return 6673;
		}
		
		return 0;
	}
	
	private void createItem(PlayerInstance activeChar, int id, int num)
	{
		if (num > 20)
		{
			final Item template = ItemTable.getInstance().getTemplate(id);
			if ((template != null) && !template.isStackable())
			{
				BuilderUtil.sendSysMessage(activeChar, "This item does not stack - Creation aborted.");
				return;
			}
		}
		
		PlayerInstance player = null;
		if (activeChar.getTarget() != null)
		{
			if (activeChar.getTarget() instanceof PlayerInstance)
			{
				if (activeChar.getAccessLevel().getLevel() > 70)
				{
					player = (PlayerInstance) activeChar.getTarget();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "You have not right to create item on another player");
					return;
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "You can add an item only to a character.");
				return;
			}
		}
		
		if (player == null)
		{
			activeChar.setTarget(activeChar);
			player = activeChar;
		}
		
		final ItemInstance newItem = player.getInventory().addItem("Admin", id, num, player, null);
		player.sendPacket(new ItemList(player, true));
		if (activeChar.getName().equalsIgnoreCase(player.getName()))
		{
			BuilderUtil.sendSysMessage(activeChar, "You have spawned " + num + " " + newItem.getItemName() + " (" + id + ") in your inventory.");
		}
		else
		{
			BuilderUtil.sendSysMessage(activeChar, "You have spawned " + num + " " + newItem.getItemName() + " (" + id + ") in " + player.getName() + "'s inventory.");
			player.sendMessage("Admin has spawned " + num + " " + newItem.getItemName() + " (" + id + ") in your inventory.");
		}
	}
	
	private void massCreateItem(PlayerInstance activeChar, int id, int num)
	{
		final Item template = ItemTable.getInstance().getTemplate(id);
		if ((template != null) && !template.isStackable())
		{
			BuilderUtil.sendSysMessage(activeChar, "This item does not stack - Creation aborted.");
			return;
		}
		
		int i = 0;
		ItemInstance item = null;
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendMessage("Admin is rewarding all online players.");
			item = player.getInventory().addItem("Admin", id, num, null, null);
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			player.sendPacket(iu);
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
			sm.addItemName(item.getItemId());
			sm.addNumber(num);
			player.sendPacket(sm);
			i++;
		}
		BuilderUtil.sendSysMessage(activeChar, "Mass-created items in the inventory of " + i + " player(s).");
		LOGGER.info("GM " + activeChar.getName() + " mass_created item Id: " + id + " (" + num + ")");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
