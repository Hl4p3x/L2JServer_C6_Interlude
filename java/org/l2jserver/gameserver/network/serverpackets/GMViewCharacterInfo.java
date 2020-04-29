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

import org.l2jserver.gameserver.model.Inventory;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;

/**
 * TODO Add support for Eval. Score dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSddd rev420 dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhh rev478
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhhddd rev551
 * @version $Revision: 1.2.2.2.2.8 $ $Date: 2005/03/27 15:29:39 $
 */
public class GMViewCharacterInfo extends GameServerPacket
{
	/** The _active char. */
	private final PlayerInstance _player;
	
	/**
	 * Instantiates a new GM view character info.
	 * @param player the player
	 */
	public GMViewCharacterInfo(PlayerInstance player)
	{
		_player = player;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		final float moveMultiplier = _player.getMovementSpeedMultiplier();
		final int _runSpd = (int) (_player.getRunSpeed() / moveMultiplier);
		final int _walkSpd = (int) (_player.getWalkSpeed() / moveMultiplier);
		writeC(0x8f);
		
		writeD(_player.getX());
		writeD(_player.getY());
		writeD(_player.getZ());
		writeD(_player.getHeading());
		writeD(_player.getObjectId());
		writeS(_player.getName());
		writeD(_player.getRace().ordinal());
		writeD(_player.getAppearance().isFemale() ? 1 : 0);
		writeD(_player.getClassId().getId());
		writeD(_player.getLevel());
		writeQ(_player.getExp());
		writeD(_player.getSTR());
		writeD(_player.getDEX());
		writeD(_player.getCON());
		writeD(_player.getINT());
		writeD(_player.getWIT());
		writeD(_player.getMEN());
		writeD(_player.getMaxHp());
		writeD((int) _player.getCurrentHp());
		writeD(_player.getMaxMp());
		writeD((int) _player.getCurrentMp());
		writeD(_player.getSp());
		writeD(_player.getCurrentLoad());
		writeD(_player.getMaxLoad());
		writeD(0x28); // unknown
		
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		
		// c6 new h's
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		// end of c6 new h's
		writeD(_player.getPAtk(null));
		writeD(_player.getPAtkSpd());
		writeD(_player.getPDef(null));
		writeD(_player.getEvasionRate(null));
		writeD(_player.getAccuracy());
		writeD(_player.getCriticalHit(null, null));
		writeD(_player.getMAtk(null, null));
		
		writeD(_player.getMAtkSpd());
		writeD(_player.getPAtkSpd());
		
		writeD(_player.getMDef(null, null));
		
		writeD(_player.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		writeD(_player.getKarma());
		
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd); // swimspeed
		writeD(_walkSpd); // swimspeed
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(moveMultiplier);
		writeF(_player.getAttackSpeedMultiplier()); // 2.9); //
		writeF(_player.getTemplate().getCollisionRadius()); // scale
		writeF(_player.getTemplate().getCollisionHeight()); // y offset ??!? fem dwarf 4033
		writeD(_player.getAppearance().getHairStyle());
		writeD(_player.getAppearance().getHairColor());
		writeD(_player.getAppearance().getFace());
		writeD(_player.isGM() ? 0x01 : 0x00); // builder level
		
		writeS(_player.getTitle());
		writeD(_player.getClanId()); // pledge id
		writeD(_player.getClanCrestId()); // pledge crest id
		writeD(_player.getAllyId()); // ally id
		writeC(_player.getMountType()); // mount type
		writeC(_player.getPrivateStoreType());
		writeC(_player.hasDwarvenCraft() ? 1 : 0);
		writeD(_player.getPkKills());
		writeD(_player.getPvpKills());
		
		writeH(_player.getRecomLeft());
		writeH(_player.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
		writeD(_player.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(_player.getMaxCp());
		writeD((int) _player.getCurrentCp());
		
		writeC(_player.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeC(321);
		
		writeD(_player.getPledgeClass()); // changes the text above CP on Status Window
		
		writeC(_player.isNoble() ? 0x01 : 0x00);
		writeC(_player.isHero() ? 0x01 : 0x00);
		
		writeD(_player.getAppearance().getNameColor());
		writeD(_player.getAppearance().getTitleColor());
	}
}
