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
package org.l2jserver.gameserver.network.serverpackets;

/**
 * sample 0000: 6d 0c 00 00 00 00 00 00 00 03 00 00 00 f3 03 00 m............... 0010: 00 00 00 00 00 01 00 00 00 f4 03 00 00 00 00 00 ................ 0020: 00 01 00 00 00 10 04 00 00 00 00 00 00 01 00 00 ................ 0030: 00 2c 04 00 00 00 00 00 00 03 00 00 00 99 04 00 .,.............. 0040:
 * 00 00 00 00 00 02 00 00 00 a0 04 00 00 00 00 00 ................ 0050: 00 01 00 00 00 c0 04 00 00 01 00 00 00 01 00 00 ................ 0060: 00 76 00 00 00 01 00 00 00 01 00 00 00 a3 00 00 .v.............. 0070: 00 01 00 00 00 01 00 00 00 c2 00 00 00 01 00 00 ................ 0080: 00 01 00 00
 * 00 d6 00 00 00 01 00 00 00 01 00 00 ................ 0090: 00 f4 00 00 00 format d (ddd)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class SkillList extends GameServerPacket
{
	private Skill[] _skills;
	
	class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		
		Skill(int pId, int pLevel, boolean pPassive)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
		}
	}
	
	public SkillList()
	{
		_skills = new Skill[] {};
	}
	
	public void addSkill(int id, int level, boolean passive)
	{
		final Skill sk = new Skill(id, level, passive);
		if ((_skills == null) || (_skills.length == 0))
		{
			_skills = new Skill[]
			{
				sk
			};
		}
		else
		{
			final Skill[] ns = new Skill[_skills.length + 1];
			boolean added = false;
			int i = 0;
			for (Skill s : _skills)
			{
				if ((sk.id < s.id) && !added)
				{
					ns[i] = sk;
					i++;
					ns[i] = s;
					i++;
					added = true;
				}
				else
				{
					ns[i] = s;
					i++;
				}
			}
			if (!added)
			{
				ns[i] = sk;
			}
			
			_skills = ns;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x58);
		writeD(_skills.length);
		
		for (Skill temp : _skills)
		{
			writeD(temp.passive ? 1 : 0);
			writeD(temp.level);
			writeD(temp.id);
			writeC(0x00); // c5
		}
	}
}
