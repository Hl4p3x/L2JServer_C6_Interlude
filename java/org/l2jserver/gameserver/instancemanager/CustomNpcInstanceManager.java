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
package org.l2jserver.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.commons.util.Rnd;

/**
 * control for Custom Npcs that look like players.
 * @version 1.00
 * @author Darki699
 */
public class CustomNpcInstanceManager
{
	private static final Logger LOGGER = Logger.getLogger(CustomNpcInstanceManager.class.getName());
	
	private Map<Integer, customInfo> spawns; // <Object id , info>
	private Map<Integer, customInfo> templates; // <Npc Template Id , info>
	
	/**
	 * Small class to keep the npc poly data... Pretty code =)
	 * @author Darki699
	 */
	public class customInfo
	{
		public String[] stringData = new String[2];
		public int[] integerData = new int[27];
		public boolean[] booleanData = new boolean[8];
	}
	
	/**
	 * Constructor Calls to load the data
	 */
	CustomNpcInstanceManager()
	{
		load();
	}
	
	/**
	 * Flush the old data, and load new data
	 */
	public void reload()
	{
		if (spawns != null)
		{
			spawns.clear();
		}
		if (templates != null)
		{
			templates.clear();
		}
		
		load();
	}
	
	/**
	 * Just load the data for mysql...
	 */
	private final void load()
	{
		if ((spawns == null) || (templates == null))
		{
			spawns = new HashMap<>();
			templates = new HashMap<>();
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			int count = 0;
			final PreparedStatement statement = con.prepareStatement("SELECT spawn,template,name,title,class_id,female,hair_style,hair_color,face,name_color,title_color, noble,hero,pvp,karma,wpn_enchant,right_hand,left_hand,gloves,chest,legs,feet,hair,hair2, pledge,cw_level,clan_id,ally_id,clan_crest,ally_crest,rnd_class,rnd_appearance,rnd_weapon,rnd_armor,max_rnd_enchant FROM npc_to_pc_polymorph");
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				count++;
				final customInfo ci = new customInfo();
				ci.integerData[26] = rset.getInt("spawn");
				ci.integerData[25] = rset.getInt("template");
				try
				{
					ci.stringData[0] = rset.getString("name");
					ci.stringData[1] = rset.getString("title");
					ci.integerData[7] = rset.getInt("class_id");
					
					final int PcSex = rset.getInt("female");
					switch (PcSex)
					{
						case 0:
						{
							ci.booleanData[3] = false;
							break;
						}
						case 1:
						{
							ci.booleanData[3] = true;
							break;
						}
						default:
						{
							ci.booleanData[3] = Rnd.get(100) > 50;
							break;
						}
					}
					
					ci.integerData[19] = rset.getInt("hair_style");
					ci.integerData[20] = rset.getInt("hair_color");
					ci.integerData[21] = rset.getInt("face");
					ci.integerData[22] = rset.getInt("name_color");
					ci.integerData[23] = rset.getInt("title_color");
					ci.booleanData[1] = rset.getInt("noble") > 0;
					ci.booleanData[2] = rset.getInt("hero") > 0;
					ci.booleanData[0] = rset.getInt("pvp") > 0;
					ci.integerData[1] = rset.getInt("karma");
					ci.integerData[8] = rset.getInt("wpn_enchant");
					ci.integerData[11] = rset.getInt("right_hand");
					ci.integerData[12] = rset.getInt("left_hand");
					ci.integerData[13] = rset.getInt("gloves");
					ci.integerData[14] = rset.getInt("chest");
					ci.integerData[15] = rset.getInt("legs");
					ci.integerData[16] = rset.getInt("feet");
					ci.integerData[17] = rset.getInt("hair");
					ci.integerData[18] = rset.getInt("hair2");
					ci.integerData[9] = rset.getInt("pledge");
					ci.integerData[10] = rset.getInt("cw_level");
					ci.integerData[2] = rset.getInt("clan_id");
					ci.integerData[3] = rset.getInt("ally_id");
					ci.integerData[4] = rset.getInt("clan_crest");
					ci.integerData[5] = rset.getInt("ally_crest");
					ci.booleanData[4] = rset.getInt("rnd_class") > 0;
					ci.booleanData[5] = rset.getInt("rnd_appearance") > 0;
					ci.booleanData[6] = rset.getInt("rnd_weapon") > 0;
					ci.booleanData[7] = rset.getInt("rnd_armor") > 0;
					ci.integerData[24] = rset.getInt("max_rnd_enchant");
					// Same object goes in twice:
					if ((ci.integerData[25] != 0) && !templates.containsKey(ci.integerData[25]))
					{
						templates.put(ci.integerData[25], ci);
					}
					if ((ci.integerData[25] == 0) && !spawns.containsKey(ci.integerData[26]))
					{
						spawns.put(ci.integerData[26], ci);
					}
				}
				catch (Throwable t)
				{
					LOGGER.warning("Failed to load Npc Morph data for Object Id: " + ci.integerData[26] + " template: " + ci.integerData[25]);
				}
			}
			statement.close();
			rset.close();
			
			LOGGER.info("CustomNpcInstanceManager: loaded " + count + " NPC to PC polymorphs.");
		}
		catch (Exception e)
		{
		}
	}
	
	/**
	 * Checks if the NpcInstance calling this function has polymorphing data
	 * @param spwnId - NpcInstance's unique Object id
	 * @param npcId - NpcInstance's npc template id
	 * @return
	 */
	public boolean isCustomNpcInstance(int spwnId, int npcId)
	{
		if ((spwnId == 0) || (npcId == 0))
		{
			return false;
		}
		else if (spawns.containsKey(spwnId))
		{
			return true;
		}
		else if (templates.containsKey(npcId))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Return the polymorphing data for this NpcInstance if the data exists
	 * @param spwnId - NpcInstance's unique Object Id
	 * @param npcId - NpcInstance's npc template Id
	 * @return customInfo type data pack, or null if no such data exists.
	 */
	public customInfo getCustomData(int spwnId, int npcId)
	{
		if ((spwnId == 0) || (npcId == 0))
		{
			return null;
		}
		
		// First check individual spawn objects - incase they have different values than their template
		for (customInfo ci : spawns.values())
		{
			if ((ci != null) && (ci.integerData[26] == spwnId))
			{
				return ci;
			}
		}
		
		// Now check if templates contains the morph npc template
		for (customInfo ci : templates.values())
		{
			if ((ci != null) && (ci.integerData[25] == npcId))
			{
				return ci;
			}
		}
		
		return null;
	}
	
	/**
	 * @return all template morphing queue
	 */
	public Map<Integer, customInfo> getAllTemplates()
	{
		return templates;
	}
	
	/**
	 * @return all spawns morphing queue
	 */
	public Map<Integer, customInfo> getAllSpawns()
	{
		return spawns;
	}
	
	/**
	 * Already removed customInfo - Change is saved in the DB <b>NOT IMPLEMENTED YET!</b>
	 * @param ciToRemove
	 */
	public void updateRemoveInDB(customInfo ciToRemove)
	{
	}
	
	public void AddInDB(customInfo ciToAdd)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("REPLACE INTO npc_to_pc_polymorph VALUES spawn,template,name,title,class_id,female,hair_style,hair_color,face,name_color,title_color, noble,hero,pvp,karma,wpn_enchant,right_hand,left_hand,gloves,chest,legs,feet,hair,hair2, pledge,cw_level,clan_id,ally_id,clan_crest,ally_crest,rnd_class,rnd_appearance,rnd_weapon,rnd_armor,max_rnd_enchant FROM npc_to_pc_polymorph");
			final ResultSet rset = statement.executeQuery();
			statement.close();
			
			while (rset.next())
			{
				final customInfo ci = new customInfo();
				ci.integerData[26] = rset.getInt("spawn");
				ci.integerData[25] = rset.getInt("template");
			}
			
			rset.close();
		}
		catch (Throwable t)
		{
			LOGGER.warning("Could not add Npc Morph info into the DB: ");
		}
	}
	
	public static CustomNpcInstanceManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CustomNpcInstanceManager INSTANCE = new CustomNpcInstanceManager();
	}
}
