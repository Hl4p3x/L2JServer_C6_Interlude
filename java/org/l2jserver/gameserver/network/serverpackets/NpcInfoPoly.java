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

import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;

/**
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfoPoly extends GameServerPacket
{
	private Creature _creature;
	private final WorldObject _obj;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private final int _npcId;
	private boolean _isAttackable;
	private final boolean _isSummoned;
	private boolean _isRunning;
	private boolean _isInCombat;
	private boolean _isAlikeDead;
	private int _mAtkSpd;
	private int _pAtkSpd;
	private int _runSpd;
	private int _walkSpd;
	private int _swimRunSpd;
	private int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _rhand;
	private int _lhand;
	private String _name;
	private String _title;
	private int _abnormalEffect;
	NpcTemplate _template;
	private final int _collisionRadius;
	private final int _collisionHeight;
	
	/**
	 * Instantiates a new npc info poly.
	 * @param obj the obj
	 * @param attacker the attacker
	 */
	public NpcInfoPoly(WorldObject obj, Creature attacker)
	{
		_obj = obj;
		_npcId = obj.getPoly().getPolyId();
		_template = NpcTable.getInstance().getTemplate(_npcId);
		_isAttackable = true;
		_rhand = 0;
		_lhand = 0;
		_isSummoned = false;
		_collisionRadius = _template.getCollisionRadius();
		_collisionHeight = _template.getCollisionHeight();
		if (_obj instanceof Creature)
		{
			_creature = (Creature) obj;
			_isAttackable = obj.isAutoAttackable(attacker);
			_rhand = _template.getRhand();
			_lhand = _template.getLhand();
		}
		
		if (_obj instanceof ItemInstance)
		{
			_x = _obj.getX();
			_y = _obj.getY();
			_z = _obj.getZ();
			_heading = 0;
			_mAtkSpd = 100; // yes, an item can be dread as death
			_pAtkSpd = 100;
			_runSpd = 120;
			_walkSpd = 80;
			_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
			_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
			_isRunning = _isInCombat = _isAlikeDead = false;
			_name = "item";
			_title = "polymorphed";
			_abnormalEffect = 0;
		}
		else
		{
			_x = _creature.getX();
			_y = _creature.getY();
			_z = _creature.getZ();
			_heading = _creature.getHeading();
			_mAtkSpd = _creature.getMAtkSpd();
			_pAtkSpd = _creature.getPAtkSpd();
			_runSpd = _creature.getRunSpeed();
			_walkSpd = _creature.getWalkSpeed();
			_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
			_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
			_isRunning = _creature.isRunning();
			_isInCombat = _creature.isInCombat();
			_isAlikeDead = _creature.isAlikeDead();
			_name = _creature.getName();
			_title = _creature.getTitle();
			_abnormalEffect = _creature.getAbnormalEffect();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0x16);
		writeD(_obj.getObjectId());
		writeD(_npcId + 1000000); // npctype id
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/* 0x32 */); // swimspeed
		writeD(_swimWalkSpd/* 0x32 */); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1/* _activeChar.getProperMultiplier() */);
		writeF(1/* _activeChar.getAttackSpeedMultiplier() */);
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(_isRunning ? 1 : 0);
		writeC(_isInCombat ? 1 : 0);
		writeC(_isAlikeDead ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		writeD(0);
		writeD(0);
		writeD(0000); // hmm karma ??
		
		writeH(_abnormalEffect); // C2
		writeH(0x00); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeC(0000); // C2
	}
}
