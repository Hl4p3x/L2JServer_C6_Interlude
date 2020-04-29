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
package org.l2jserver.gameserver.model.actor.instance;

import java.util.logging.Logger;

import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.knownlist.NullKnownList;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.ShowTownMap;

/**
 * @author GODSON
 */
public class StaticObjectInstance extends WorldObject
{
	private static final Logger LOGGER = Logger.getLogger(StaticObjectInstance.class.getName());
	
	public static final int INTERACTION_DISTANCE = 150;
	private int _staticObjectId;
	private int _type = -1; // 0 - map signs, 1 - throne , 2 - arena signs
	private int _x;
	private int _y;
	private String _texture;
	
	/**
	 * Gets the static object id.
	 * @return Returns the StaticObjectId.
	 */
	public int getStaticObjectId()
	{
		return _staticObjectId;
	}
	
	/**
	 * Sets the static object id.
	 * @param staticObjectId the new static object id
	 */
	public void setStaticObjectId(int staticObjectId)
	{
		_staticObjectId = staticObjectId;
	}
	
	/**
	 * Instantiates a new static object instance.
	 * @param objectId the object id
	 */
	public StaticObjectInstance(int objectId)
	{
		super(objectId);
		setKnownList(new NullKnownList(this));
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	public int getType()
	{
		return _type;
	}
	
	/**
	 * Sets the type.
	 * @param type the new type
	 */
	public void setType(int type)
	{
		_type = type;
	}
	
	/**
	 * Sets the map.
	 * @param texture the texture
	 * @param x the x
	 * @param y the y
	 */
	public void setMap(String texture, int x, int y)
	{
		_texture = "town_map." + texture;
		_x = x;
		_y = y;
	}
	
	/**
	 * Gets the map x.
	 * @return the map x
	 */
	private int getMapX()
	{
		return _x;
	}
	
	/**
	 * Gets the map y.
	 * @return the map y
	 */
	private int getMapY()
	{
		return _y;
	}
	
	/**
	 * this is called when a player interacts with this NPC.
	 * @param player the player
	 */
	@Override
	public void onAction(PlayerInstance player)
	{
		if (_type < 0)
		{
			LOGGER.info("StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + _staticObjectId);
		}
		// Check if the PlayerInstance already target the NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the PlayerInstance player
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
			// Calculate the distance between the PlayerInstance and the NpcInstance
			if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
			{
				// Notify the PlayerInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				
				// Send a Server->Client packet ActionFailed (target is out of interaction range) to the PlayerInstance player
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (_type == 2)
				{
					final String filename = "data/html/signboard.htm";
					final String content = HtmCache.getInstance().getHtm(filename);
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					if (content == null)
					{
						html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>");
					}
					else
					{
						html.setHtml(content);
					}
					
					player.sendPacket(html);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (_type == 0)
				{
					player.sendPacket(new ShowTownMap(_texture, getMapX(), getMapY()));
				}
				
				// Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
}
