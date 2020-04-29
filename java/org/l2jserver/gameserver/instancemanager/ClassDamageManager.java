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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.xml.PlayerTemplateData;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Shyla
 */
public class ClassDamageManager
{
	private static final Logger LOGGER = Logger.getLogger(ClassDamageManager.class.getName());
	
	private static final Map<Integer, Double> DAMAGE_TO_MAGE = new HashMap<>();
	private static final Map<Integer, Double> DAMAGE_TO_FIGHTER = new HashMap<>();
	private static final Map<Integer, Double> DAMAGE_BY_MAGE = new HashMap<>();
	private static final Map<Integer, Double> DAMAGE_BY_FIGHTER = new HashMap<>();
	
	private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
	private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();
	
	public static void loadConfig()
	{
		InputStream is = null;
		File file = null;
		try
		{
			final Properties scriptSetting = new Properties();
			file = new File(Config.CLASS_DAMAGE_CONFIG_FILE);
			is = new FileInputStream(file);
			scriptSetting.load(is);
			
			for (Object key : scriptSetting.keySet())
			{
				final String keyString = (String) key;
				final String[] classAndType = keyString.split("__");
				String className = classAndType[0].replace("_", " ");
				if (className.equals("Eva s Saint"))
				{
					className = "Eva's Saint";
				}
				
				final String type = classAndType[1];
				final Integer classId = PlayerTemplateData.getInstance().getClassIdByName(className);
				ID_TO_NAME.put(classId, className);
				NAME_TO_ID.put(className, classId);
				
				if (type.equals("ToFighter"))
				{
					DAMAGE_TO_FIGHTER.put(classId, Double.parseDouble(scriptSetting.getProperty(keyString)));
				}
				else if (type.equals("ToMage"))
				{
					DAMAGE_TO_MAGE.put(classId, Double.parseDouble(scriptSetting.getProperty(keyString)));
				}
				else if (type.equals("ByFighter"))
				{
					DAMAGE_BY_FIGHTER.put(classId, Double.parseDouble(scriptSetting.getProperty(keyString)));
				}
				else if (type.equals("ByMage"))
				{
					DAMAGE_BY_MAGE.put(classId, Double.parseDouble(scriptSetting.getProperty(keyString)));
				}
			}
			
			LOGGER.info("Loaded " + ID_TO_NAME.size() + " class damage configurations.");
		}
		catch (Exception e)
		{
			LOGGER.warning("Problem with ClassDamageManager: " + e.getMessage());
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					LOGGER.warning("Problem with ClassDamageManager: " + e.getMessage());
				}
			}
		}
	}
	
	public static double getClassDamageToMage(int id)
	{
		final Double multiplier = DAMAGE_TO_MAGE.get(id);
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static double getClassDamageToFighter(int id)
	{
		final Double multiplier = DAMAGE_TO_FIGHTER.get(id);
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static double getClassDamageByMage(int id)
	{
		final Double multiplier = DAMAGE_BY_MAGE.get(id);
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static double getClassDamageByFighter(int id)
	{
		final Double multiplier = DAMAGE_BY_FIGHTER.get(id);
		if (multiplier != null)
		{
			return multiplier;
		}
		return 1;
	}
	
	public static int getIdByName(String name)
	{
		final Integer id = NAME_TO_ID.get(name);
		if (id != null)
		{
			return id;
		}
		return 0;
	}
	
	public static String getNameById(int id)
	{
		final String name = ID_TO_NAME.get(id);
		if (name != null)
		{
			return name;
		}
		return "";
	}
	
	/**
	 * return the product between the attackerMultiplier and attackedMultiplier configured into the classDamage.ini
	 * @param attacker
	 * @param attacked
	 * @return output = attackerMulti*attackedMulti
	 */
	public static double getDamageMultiplier(PlayerInstance attacker, PlayerInstance attacked)
	{
		if ((attacker == null) || (attacked == null))
		{
			return 1;
		}
		
		double attackerMulti = 1;
		if (attacked.isMageClass())
		{
			attackerMulti = getClassDamageToMage(attacker.getClassId().getId());
		}
		else
		{
			attackerMulti = getClassDamageToFighter(attacker.getClassId().getId());
		}
		
		double attackedMulti = 1;
		if (attacker.isMageClass())
		{
			attackedMulti = getClassDamageByMage(attacked.getClassId().getId());
		}
		else
		{
			attackedMulti = getClassDamageByFighter(attacked.getClassId().getId());
		}
		
		final double output = attackerMulti * attackedMulti;
		if (Config.ENABLE_CLASS_DAMAGE_LOGGER)
		{
			LOGGER.info("ClassDamageManager -");
			LOGGER.info("ClassDamageManager - Attacker: " + attacker.getName() + " Class: " + getNameById(attacker.getClassId().getId()) + " ClassId: " + attacker.getClassId().getId() + " isMage: " + attacker.isMageClass() + " mult: " + attackerMulti);
			LOGGER.info("ClassDamageManager - Attacked: " + attacked.getName() + " Class: " + getNameById(attacked.getClassId().getId()) + " ClassId: " + attacked.getClassId().getId() + " isMage: " + attacked.isMageClass() + " mult: " + attackedMulti);
			LOGGER.info("ClassDamageManager - FinalMultiplier: " + output);
			LOGGER.info("ClassDamageManager -");
		}
		
		return output;
	}
}
