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
package org.l2jserver.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.List;

import org.l2jserver.gameserver.enums.Race;
import org.l2jserver.gameserver.model.StatSet;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * @author mkizub
 */
public class PlayerTemplate extends CreatureTemplate
{
	private final Race _race;
	private final ClassId _classId;
	private final String _className;
	private final int _classBaseLevel;
	private final float _levelHpAdd;
	private final float _levelHpMod;
	private final float _levelCpAdd;
	private final float _levelCpMod;
	private final float _levelMpAdd;
	private final float _levelMpMod;
	private final int _spawnX;
	private final int _spawnY;
	private final int _spawnZ;
	private final List<ItemHolder> _items = new ArrayList<>();
	
	public PlayerTemplate(StatSet set)
	{
		super(set);
		_classId = ClassId.getClassId(set.getInt("id"));
		_race = Enum.valueOf(Race.class, set.getString("race"));
		_className = set.getString("name");
		_spawnX = set.getInt("spawnX");
		_spawnY = set.getInt("spawnY");
		_spawnZ = set.getInt("spawnZ");
		_classBaseLevel = set.getInt("baseLevel");
		_levelHpAdd = set.getFloat("levelHpAdd");
		_levelHpMod = set.getFloat("levelHpMod");
		_levelCpAdd = set.getFloat("levelCpAdd");
		_levelCpMod = set.getFloat("levelCpMod");
		_levelMpAdd = set.getFloat("levelMpAdd");
		_levelMpMod = set.getFloat("levelMpMod");
		String[] item;
		for (String split : set.getString("items").split(";"))
		{
			item = split.split(",");
			_items.add(new ItemHolder(Integer.parseInt(item[0]), Integer.parseInt(item[1])));
		}
	}
	
	public List<ItemHolder> getItems()
	{
		return _items;
	}
	
	public Race getRace()
	{
		return _race;
	}
	
	public ClassId getClassId()
	{
		return _classId;
	}
	
	public String getClassName()
	{
		return _className;
	}
	
	public int getSpawnX()
	{
		return _spawnX;
	}
	
	public int getSpawnY()
	{
		return _spawnY;
	}
	
	public int getSpawnZ()
	{
		return _spawnZ;
	}
	
	public int getClassBaseLevel()
	{
		return _classBaseLevel;
	}
	
	public float getLevelHpAdd()
	{
		return _levelHpAdd;
	}
	
	public float getLevelHpMod()
	{
		return _levelHpMod;
	}
	
	public float getLevelCpAdd()
	{
		return _levelCpAdd;
	}
	
	public float getLevelCpMod()
	{
		return _levelCpMod;
	}
	
	public float getLevelMpAdd()
	{
		return _levelMpAdd;
	}
	
	public float getLevelMpMod()
	{
		return _levelMpMod;
	}
	
	public int getBaseFallSafeHeight(boolean female)
	{
		if ((_classId.getRace() == Race.DARK_ELF) || (_classId.getRace() == Race.ELF))
		{
			return _classId.isMage() ? (female ? 330 : 300) : female ? 380 : 350;
		}
		else if (_classId.getRace() == Race.DWARF)
		{
			return female ? 200 : 180;
		}
		else if (_classId.getRace() == Race.HUMAN)
		{
			return _classId.isMage() ? (female ? 220 : 200) : female ? 270 : 250;
		}
		else if (_classId.getRace() == Race.ORC)
		{
			return _classId.isMage() ? (female ? 280 : 250) : female ? 220 : 200;
		}
		return 400;
	}
	
	public int getFallHeight()
	{
		return 333; // TODO: unhardcode it
	}
}
