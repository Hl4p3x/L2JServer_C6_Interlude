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
package org.l2jserver.gameserver.handler.itemhandlers;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.NpcTable;
import org.l2jserver.gameserver.datatables.xml.SummonItemData;
import org.l2jserver.gameserver.handler.IItemHandler;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.SummonItem;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.actor.Playable;
import org.l2jserver.gameserver.model.actor.instance.PetInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance.SkillDat;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.event.CTF;
import org.l2jserver.gameserver.model.entity.event.DM;
import org.l2jserver.gameserver.model.entity.event.TvT;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.spawn.Spawn;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2jserver.gameserver.network.serverpackets.PetInfo;
import org.l2jserver.gameserver.network.serverpackets.Ride;

public class SummonItems implements IItemHandler
{
	private static final int[] ITEM_IDS = SummonItemData.getInstance().getAllItemIds();
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		if (!player.getFloodProtectors().getItemPetSummon().tryPerformAction("summon pet"))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player._inEventTvT && TvT.isStarted() && !Config.TVT_ALLOW_SUMMON)
		{
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		if (player._inEventDM && DM.hasStarted() && !Config.DM_ALLOW_SUMMON)
		{
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		if (player._inEventCTF && CTF.isStarted() && !Config.CTF_ALLOW_SUMMON)
		{
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SITTING);
			return;
		}
		
		if (player.isParalyzed())
		{
			player.sendMessage("You Cannot Use This While You Are Paralyzed");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.inObserverMode())
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		final SummonItem sitem = SummonItemData.getInstance().getSummonItem(item.getItemId());
		if (((player.getPet() != null) || player.isMounted()) && sitem.isPetSummon())
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}
		
		// Like L2OFF you can't summon pet in combat
		if (player.isAttackingNow() || player.isInCombat())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_DURING_COMBAT);
			return;
		}
		
		if (player.isCursedWeaponEquiped() && sitem.isPetSummon())
		{
			player.sendPacket(SystemMessageId.A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE);
			return;
		}
		
		final int npcID = sitem.getNpcId();
		if (npcID == 0)
		{
			return;
		}
		
		final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);
		if (npcTemplate == null)
		{
			return;
		}
		
		switch (sitem.getType())
		{
			case 0: // static summons (like christmas tree)
			{
				try
				{
					final Spawn spawn = new Spawn(npcTemplate);
					spawn.setId(IdFactory.getNextId());
					spawn.setX(player.getX());
					spawn.setY(player.getY());
					spawn.setZ(player.getZ());
					World.getInstance().storeObject(spawn.doSpawn());
					player.destroyItem("Summon", item.getObjectId(), 1, null, false);
					player.sendMessage("Created " + npcTemplate.getName() + " at x: " + spawn.getX() + " y: " + spawn.getY() + " z: " + spawn.getZ());
				}
				catch (Exception e)
				{
					player.sendMessage("Target is not ingame.");
				}
				break;
			}
			case 1: // pet summons
			{
				player.setTarget(player);
				// Skill 2046 used only for animation
				final Skill skill = SkillTable.getInstance().getInfo(2046, 1);
				player.useMagic(skill, true, true);
				player.sendPacket(SystemMessageId.SUMMONING_YOUR_PET);
				ThreadPool.schedule(new PetSummonFinalizer(player, npcTemplate, item), 4800);
				break;
			}
			case 2: // wyvern
			{
				if (!player.disarmWeapons())
				{
					return;
				}
				final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, sitem.getNpcId());
				player.sendPacket(mount);
				player.broadcastPacket(mount);
				player.setMountType(mount.getMountType());
				player.setMountObjectID(item.getObjectId());
			}
		}
	}
	
	static class PetSummonFeedWait implements Runnable
	{
		private final PlayerInstance _player;
		private final PetInstance _petSummon;
		
		PetSummonFeedWait(PlayerInstance player, PetInstance petSummon)
		{
			_player = player;
			_petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
				{
					_petSummon.unSummon(_player);
				}
				else
				{
					_petSummon.startFeed(false);
				}
			}
			catch (Throwable e)
			{
			}
		}
	}
	
	static class PetSummonFinalizer implements Runnable
	{
		private final PlayerInstance _player;
		private final ItemInstance _item;
		private final NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(PlayerInstance player, NpcTemplate npcTemplate, ItemInstance item)
		{
			_player = player;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				final SkillDat skilldat = _player.getCurrentSkill();
				if (!_player.isCastingNow() || ((skilldat != null) && (skilldat.getSkillId() != 2046)))
				{
					return;
				}
				
				_player.sendPacket(new MagicSkillLaunched(_player, 2046, 1));
				
				// check for summon item validity
				if ((_item == null) || (_item.getOwnerId() != _player.getObjectId()) || (_item.getItemLocation() != ItemInstance.ItemLocation.INVENTORY))
				{
					return;
				}
				
				final PetInstance petSummon = PetInstance.spawnPet(_npcTemplate, _player, _item);
				if (petSummon == null)
				{
					return;
				}
				
				petSummon.setTitle(_player.getName());
				
				if (!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}
				
				petSummon.setRunning();
				
				if (!petSummon.isRespawned())
				{
					petSummon.store();
				}
				
				_player.setPet(petSummon);
				
				World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(_player.getX() + 50, _player.getY() + 100, _player.getZ());
				_player.sendPacket(new PetInfo(petSummon));
				petSummon.startFeed(false);
				_item.setEnchantLevel(petSummon.getLevel());
				
				if (petSummon.getCurrentFed() <= 0)
				{
					ThreadPool.schedule(new PetSummonFeedWait(_player, petSummon), 60000);
				}
				else
				{
					petSummon.startFeed(false);
				}
				
				petSummon.setFollowStatus(true);
				petSummon.setShowSummonAnimation(false);
				petSummon.broadcastStatusUpdate();
			}
			catch (Throwable e)
			{
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}