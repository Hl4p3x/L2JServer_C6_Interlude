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
package org.l2jserver.gameserver.model.base;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.xml.ExperienceData;

/**
 * Character Sub-Class Definition.<br>
 * Used to store key information about a character's sub-class.
 * @author Tempy
 */
public class SubClass
{
	private ClassId _class;
	private long _exp = ExperienceData.getInstance().getExpForLevel(Config.BASE_SUBCLASS_LEVEL);
	private int _sp = 0;
	private int _level = Config.BASE_SUBCLASS_LEVEL;
	private int _classIndex = 1;
	
	public SubClass(int classId, long exp, int sp, byte level, int classIndex)
	{
		_class = ClassId.getClassId(classId);
		_exp = exp;
		_sp = sp;
		_level = level;
		_classIndex = classIndex;
	}
	
	public SubClass(int classId, int classIndex)
	{
		// Used for defining a sub class using default values for XP, SP and player level.
		_class = ClassId.getClassId(classId);
		_classIndex = classIndex;
	}
	
	public SubClass()
	{
		// Used for specifying ALL attributes of a sub class directly, using the preset default values.
	}
	
	public ClassId getClassDefinition()
	{
		return _class;
	}
	
	public int getClassId()
	{
		return _class.getId();
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	public void setClassId(int classId)
	{
		_class = ClassId.getClassId(classId);
	}
	
	public void setExp(long expValue)
	{
		if (expValue > ExperienceData.getInstance().getExpForLevel(Config.MAX_SUBCLASS_LEVEL))
		{
			expValue = ExperienceData.getInstance().getExpForLevel(Config.MAX_SUBCLASS_LEVEL);
		}
		
		_exp = expValue;
	}
	
	public void setSp(int spValue)
	{
		_sp = spValue;
	}
	
	public void setClassIndex(int classIndex)
	{
		_classIndex = classIndex;
	}
	
	public void setLevel(int levelValue)
	{
		if (levelValue > (Config.MAX_SUBCLASS_LEVEL - 1))
		{
			levelValue = Config.MAX_SUBCLASS_LEVEL - 1;
		}
		else if (levelValue < Config.BASE_SUBCLASS_LEVEL)
		{
			levelValue = Config.BASE_SUBCLASS_LEVEL;
		}
		
		_level = levelValue;
	}
	
	public void incLevel()
	{
		if (_level == (Config.MAX_SUBCLASS_LEVEL - 1))
		{
			return;
		}
		_level++;
		setExp(ExperienceData.getInstance().getExpForLevel(getLevel()));
	}
	
	public void decLevel()
	{
		if (_level == Config.BASE_SUBCLASS_LEVEL)
		{
			return;
		}
		_level--;
		setExp(ExperienceData.getInstance().getExpForLevel(getLevel()));
	}
}
