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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.concurrent.ThreadPool;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.datatables.sql.PetDataTable;
import org.l2jserver.gameserver.idfactory.IdFactory;
import org.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jserver.gameserver.model.Inventory;
import org.l2jserver.gameserver.model.PetData;
import org.l2jserver.gameserver.model.PetInventory;
import org.l2jserver.gameserver.model.PlayerInventory;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.stat.PetStat;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.entity.olympiad.Olympiad;
import org.l2jserver.gameserver.model.items.Item;
import org.l2jserver.gameserver.model.items.Weapon;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.ItemList;
import org.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jserver.gameserver.network.serverpackets.PetInventoryUpdate;
import org.l2jserver.gameserver.network.serverpackets.PetItemList;
import org.l2jserver.gameserver.network.serverpackets.PetStatusShow;
import org.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import org.l2jserver.gameserver.network.serverpackets.StopMove;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;
import org.l2jserver.gameserver.taskmanager.DecayTaskManager;

public class PetInstance extends Summon
{
	protected static final Logger LOGGER = Logger.getLogger(PetInstance.class.getName());
	
	private static final int FOOD_ITEM_CONSUME_COUNT = 5;
	int _curFed;
	final PetInventory _inventory;
	private final int _controlItemId;
	private boolean _respawned;
	private final boolean _mountable;
	private Future<?> _feedTask;
	protected boolean _feedMode;
	private PetData _data;
	private long _expBeforeDeath = 0;
	
	/**
	 * Gets the pet data.
	 * @return the pet data
	 */
	public PetData getPetData()
	{
		if (_data == null)
		{
			_data = PetDataTable.getInstance().getPetData(getTemplate().getNpcId(), getStat().getLevel());
		}
		return _data;
	}
	
	/**
	 * Sets the pet data.
	 * @param value the new pet data
	 */
	public void setPetData(PetData value)
	{
		_data = value;
	}
	
	/**
	 * Manage Feeding Task.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Feed or kill the pet depending on hunger level</li>
	 * <li>If pet has food in inventory and feed level drops below 55% then consume food from inventory</li>
	 * <li>Send a broadcastStatusUpdate packet for this PetInstance</li>
	 */
	
	class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// if pet is attacking
				if (isAttackingNow())
				{
					// if its not already on battleFeed mode
					if (!_feedMode)
					{
						startFeed(true); // switching to battle feed
					}
					else
					// if its on battleFeed mode
					if (_feedMode)
					{
						startFeed(false); // normal feed
					}
				}
				
				if (_curFed > FOOD_ITEM_CONSUME_COUNT)
				{
					// eat
					setCurrentFed(_curFed - FOOD_ITEM_CONSUME_COUNT);
				}
				else
				{
					// go back to pet control item, or simply said, unsummon it
					setCurrentFed(0);
					stopFeed();
					unSummon(getOwner());
					getOwner().sendMessage("Your pet is too hungry to stay summoned.");
				}
				
				final int foodId = PetDataTable.getFoodItemId(getTemplate().getNpcId());
				if (foodId == 0)
				{
					return;
				}
				
				ItemInstance food = null;
				food = _inventory.getItemByItemId(foodId);
				if ((food != null) && (_curFed < (0.55 * getStat().getMaxFeed())))
				{
					if (destroyItem("Feed", food.getObjectId(), 1, null, false))
					{
						setCurrentFed(_curFed + 100);
						if (getOwner() != null)
						{
							final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_PET_WAS_HUNGRY_SO_IT_ATE_S1);
							sm.addItemName(foodId);
							getOwner().sendPacket(sm);
						}
					}
				}
				
				broadcastStatusUpdate();
			}
			catch (Throwable e)
			{
				LOGGER.info("Pet [#" + getObjectId() + "] a feed task error has occurred: " + e);
			}
		}
	}
	
	/**
	 * Spawn pet.
	 * @param template the template
	 * @param owner the owner
	 * @param control the control
	 * @return the pet instance
	 */
	public static synchronized PetInstance spawnPet(NpcTemplate template, PlayerInstance owner, ItemInstance control)
	{
		if (World.getInstance().getPet(owner.getObjectId()) != null)
		{
			return null; // owner has a pet listed in world
		}
		
		final PetInstance pet = restore(control, template, owner);
		// add the pet instance to world
		if (pet != null)
		{
			// fix pet title
			pet.setTitle(owner.getName());
			World.getInstance().addPet(owner.getObjectId(), pet);
		}
		
		return pet;
	}
	
	/**
	 * Instantiates a new pet instance.
	 * @param objectId the object id
	 * @param template the template
	 * @param owner the owner
	 * @param control the control
	 */
	public PetInstance(int objectId, NpcTemplate template, PlayerInstance owner, ItemInstance control)
	{
		super(objectId, template, owner);
		super.setStat(new PetStat(this));
		
		_controlItemId = control.getObjectId();
		
		// Pet's initial level is supposed to be read from DB
		// Pets start at :
		// Wolf : Level 15
		// Hatcling : Level 35
		// Tested and confirmed on official servers
		// Sin-eaters are defaulted at the owner's level
		if (template.getNpcId() == 12564)
		{
			getStat().setLevel((byte) getOwner().getLevel());
		}
		else
		{
			getStat().setLevel(template.getLevel());
		}
		
		_inventory = new PetInventory(this);
		
		final int npcId = template.getNpcId();
		_mountable = PetDataTable.isMountable(npcId);
	}
	
	@Override
	public PetStat getStat()
	{
		if (!(super.getStat() instanceof PetStat))
		{
			setStat(new PetStat(this));
		}
		return (PetStat) super.getStat();
	}
	
	@Override
	public double getLevelMod()
	{
		return ((100.0 - 11) + getStat().getLevel()) / 100.0;
	}
	
	/**
	 * Checks if is respawned.
	 * @return true, if is respawned
	 */
	public boolean isRespawned()
	{
		return _respawned;
	}
	
	@Override
	public int getSummonType()
	{
		return 2;
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		final boolean isOwner = player.getObjectId() == getOwner().getObjectId();
		final boolean thisIsTarget = (player.getTarget() != null) && (player.getTarget().getObjectId() == getObjectId());
		if (isOwner && thisIsTarget)
		{
			if (player != getOwner())
			{
				// update owner
				updateRefOwner(player);
			}
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		}
	}
	
	@Override
	public int getControlItemId()
	{
		return _controlItemId;
	}
	
	/**
	 * Gets the control item.
	 * @return the control item
	 */
	public ItemInstance getControlItem()
	{
		return getOwner().getInventory().getItemByObjectId(_controlItemId);
	}
	
	/**
	 * Gets the current fed.
	 * @return the current fed
	 */
	public int getCurrentFed()
	{
		return _curFed;
	}
	
	/**
	 * Sets the current fed.
	 * @param num the new current fed
	 */
	public void setCurrentFed(int num)
	{
		_curFed = num > getStat().getMaxFeed() ? getStat().getMaxFeed() : num;
	}
	
	@Override
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	/**
	 * Returns the pet's currently equipped weapon instance (if any).
	 * @return the active weapon instance
	 */
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		for (ItemInstance item : _inventory.getItems())
		{
			if ((item.getItemLocation() == ItemInstance.ItemLocation.PET_EQUIP) && (item.getItem().getBodyPart() == Item.SLOT_R_HAND))
			{
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Returns the pet's currently equipped weapon (if any).
	 * @return the active weapon item
	 */
	@Override
	public Weapon getActiveWeaponItem()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null)
		{
			return null;
		}
		return (Weapon) weapon.getItem();
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}
	
	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the PlayerInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	@Override
	public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage)
	{
		final ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
			{
				getOwner().sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return false;
		}
		
		// Send Pet inventory update packet
		final PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);
		
		if (sendMessage)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			getOwner().sendPacket(sm);
		}
		
		return true;
	}
	
	/**
	 * Destroy item from inventory by using its <b>itemId</b> and send a Server->Client InventoryUpdate packet to the PlayerInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
	{
		final ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
			{
				getOwner().sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return false;
		}
		
		// Send Pet inventory update packet
		final PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);
		
		if (sendMessage)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(itemId);
			getOwner().sendPacket(sm);
		}
		
		return true;
	}
	
	@Override
	protected void doPickupItem(WorldObject object)
	{
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		broadcastPacket(new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()));
		if (!(object instanceof ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			LOGGER.warning("Trying to pickup wrong target." + object);
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ItemInstance target = (ItemInstance) object;
		
		// Herbs
		if ((target.getItemId() > 8599) && (target.getItemId() < 8615))
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
			smsg.addItemName(target.getItemId());
			getOwner().sendPacket(smsg);
			return;
		}
		// Cursed weapons
		if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
			smsg.addItemName(target.getItemId());
			getOwner().sendPacket(smsg);
			return;
		}
		
		synchronized (target)
		{
			if (!target.isVisible())
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
				smsg.addItemName(target.getItemId());
				getOwner().sendPacket(smsg);
				return;
			}
			
			if ((target.getOwnerId() != 0) && (target.getOwnerId() != getOwner().getObjectId()) && !getOwner().isInLooterParty(target.getOwnerId()))
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);
				
				if (target.getItemId() == 57)
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					smsg.addNumber(target.getCount());
					getOwner().sendPacket(smsg);
				}
				else if (target.getCount() > 1)
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S2_S1_S);
					smsg.addItemName(target.getItemId());
					smsg.addNumber(target.getCount());
					getOwner().sendPacket(smsg);
				}
				else
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					smsg.addItemName(target.getItemId());
					getOwner().sendPacket(smsg);
				}
				
				return;
			}
			if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getOwner().getObjectId()) || getOwner().isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}
			
			target.pickupMe(this);
			
			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}
		
		_inventory.addItem("Pickup", target, getOwner(), this);
		// FIXME Just send the updates if possible (old way wasn't working though)
		getOwner().sendPacket(new PetItemList(this));
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		if (getFollowStatus())
		{
			followOwner();
		}
	}
	
	@Override
	public void deleteMe(PlayerInstance owner)
	{
		super.deleteMe(owner);
		destroyControlItem(owner); // this should also delete the pet from the db
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer, true))
		{
			return false;
		}
		stopFeed();
		DecayTaskManager.getInstance().addDecayTask(this, 1200000);
		deathPenalty();
		return true;
	}
	
	@Override
	public void doRevive()
	{
		if (_curFed > (getStat().getMaxFeed() / 10))
		{
			_curFed = getStat().getMaxFeed() / 10;
		}
		
		getOwner().removeReviving();
		
		super.doRevive();
		
		// stopDecay
		DecayTaskManager.getInstance().cancelDecayTask(this);
		startFeed(false);
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		// Restore the pet's lost experience, depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	/**
	 * Transfers item to another inventory.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId the object id
	 * @param count : int Quantity of items to be transfered
	 * @param target the target
	 * @param actor : PlayerInstance Player requesting the item transfer
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public ItemInstance transferItem(String process, int objectId, int count, Inventory target, PlayerInstance actor, WorldObject reference)
	{
		final ItemInstance oldItem = _inventory.getItemByObjectId(objectId);
		final ItemInstance newItem = _inventory.transferItem(process, objectId, count, target, actor, reference);
		if (newItem == null)
		{
			return null;
		}
		
		// Send inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if ((oldItem.getCount() > 0) && (oldItem != newItem))
		{
			petIU.addModifiedItem(oldItem);
		}
		else
		{
			petIU.addRemovedItem(oldItem);
		}
		
		getOwner().sendPacket(petIU);
		
		// Send target update packet
		if (target instanceof PlayerInventory)
		{
			final PlayerInstance targetPlayer = ((PlayerInventory) target).getOwner();
			final InventoryUpdate playerUI = new InventoryUpdate();
			if (newItem.getCount() > count)
			{
				playerUI.addModifiedItem(newItem);
			}
			else
			{
				playerUI.addNewItem(newItem);
			}
			targetPlayer.sendPacket(playerUI);
			
			// Update current load as well
			final StatusUpdate playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			petIU = new PetInventoryUpdate();
			if (newItem.getCount() > count)
			{
				petIU.addRemovedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
		}
		return newItem;
	}
	
	@Override
	public void giveAllToOwner()
	{
		try
		{
			final Inventory petInventory = _inventory;
			final ItemInstance[] items = petInventory.getItems();
			for (ItemInstance item : items)
			{
				final ItemInstance giveit = item;
				if (((giveit.getItem().getWeight() * giveit.getCount()) + getOwner().getInventory().getTotalWeight()) < getOwner().getMaxLoad())
				{
					// If the owner can carry it give it to them
					giveItemToOwner(giveit);
				}
				else
				{
					// If they can't carry it, chuck it on the floor :)
					dropItemHere(giveit);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Give all items error " + e);
		}
	}
	
	/**
	 * Give item to owner.
	 * @param item the item
	 */
	public void giveItemToOwner(ItemInstance item)
	{
		try
		{
			getInventory().transferItem("PetTransfer", item.getObjectId(), item.getCount(), getOwner().getInventory(), getOwner(), this);
			final PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addRemovedItem(item);
			getOwner().sendPacket(petiu);
			getOwner().sendPacket(new ItemList(getOwner(), false));
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while giving item to owner " + e);
		}
	}
	
	/**
	 * Remove the Pet from DB and its associated item from the player inventory.
	 * @param owner The owner from whose invenory we should delete the item
	 */
	public void destroyControlItem(PlayerInstance owner)
	{
		// remove the pet instance from world
		World.getInstance().removePet(owner.getObjectId());
		
		// delete from inventory
		try
		{
			final ItemInstance removedItem = owner.getInventory().destroyItem("PetDestroy", _controlItemId, 1, getOwner(), this);
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(removedItem);
			owner.sendPacket(iu);
			
			final StatusUpdate su = new StatusUpdate(owner.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
			owner.sendPacket(su);
			
			owner.broadcastUserInfo();
			
			final World world = World.getInstance();
			world.removeObject(removedItem);
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while destroying control item " + e);
		}
		
		// pet control item no longer exists, delete the pet from the db
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, _controlItemId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not delete pet " + e);
		}
	}
	
	/**
	 * Drop all items.
	 */
	public void dropAllItems()
	{
		try
		{
			final ItemInstance[] items = _inventory.getItems();
			for (ItemInstance item : items)
			{
				dropItemHere(item);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Pet Drop Error " + e);
		}
	}
	
	/**
	 * Drop item here.
	 * @param dropit the dropit
	 */
	public void dropItemHere(ItemInstance dropit)
	{
		dropItemHere(dropit, false);
	}
	
	/**
	 * Drop item here.
	 * @param dropit the dropit
	 * @param protect the protect
	 */
	public void dropItemHere(ItemInstance dropit, boolean protect)
	{
		dropit = _inventory.dropItem("Drop", dropit.getObjectId(), dropit.getCount(), getOwner(), this);
		if (dropit != null)
		{
			if (protect)
			{
				dropit.getDropProtection().protect(getOwner());
			}
			
			LOGGER.info("Item id to drop: " + dropit.getItemId() + " amount: " + dropit.getCount());
			dropit.dropMe(this, getX(), getY(), getZ() + 100);
		}
	}
	
	/**
	 * Checks if is mountable.
	 * @return Returns the mountable.
	 */
	@Override
	public boolean isMountable()
	{
		return _mountable;
	}
	
	/**
	 * Restore.
	 * @param control the control
	 * @param template the template
	 * @param owner the owner
	 * @return the pet instance
	 */
	private static PetInstance restore(ItemInstance control, NpcTemplate template, PlayerInstance owner)
	{
		PetInstance pet = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (template.getType().equalsIgnoreCase("BabyPet"))
			{
				pet = new BabyPetInstance(IdFactory.getNextId(), template, owner, control);
			}
			else
			{
				pet = new PetInstance(IdFactory.getNextId(), template, owner, control);
			}
			
			final PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, karma, pkkills, fed FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			final ResultSet rset = statement.executeQuery();
			if (!rset.next())
			{
				rset.close();
				statement.close();
				con.close();
				return pet;
			}
			
			pet._respawned = true;
			pet.setName(rset.getString("name"));
			
			pet.getStat().setLevel(rset.getByte("level"));
			pet.getStat().setExp(rset.getLong("exp"));
			pet.getStat().setSp(rset.getInt("sp"));
			
			pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
			pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
			pet.getStatus().setCurrentCp(pet.getMaxCp());
			
			// pet.setKarma(rset.getInt("karma"));
			pet.setPkKills(rset.getInt("pkkills"));
			pet.setCurrentFed(rset.getInt("fed"));
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore pet data " + e);
		}
		
		return pet;
	}
	
	@Override
	public void store()
	{
		if (_controlItemId == 0)
		{
			// this is a summon, not a pet, don't store anything
			return;
		}
		
		String req;
		if (!_respawned)
		{
			req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,karma,pkkills,fed,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
		}
		else
		{
			req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,karma=?,pkkills=?,fed=? WHERE item_obj_id = ?";
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(req);
			statement.setString(1, getName());
			statement.setInt(2, getStat().getLevel());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setDouble(4, getStatus().getCurrentMp());
			statement.setLong(5, getStat().getExp());
			statement.setInt(6, getStat().getSp());
			statement.setInt(7, getKarma());
			statement.setInt(8, getPkKills());
			statement.setInt(9, _curFed);
			statement.setInt(10, _controlItemId);
			statement.executeUpdate();
			statement.close();
			_respawned = true;
		}
		catch (Exception e)
		{
			LOGGER.warning("could not store pet data " + e);
		}
		
		final ItemInstance itemInst = getControlItem();
		if ((itemInst != null) && (itemInst.getEnchantLevel() != getStat().getLevel()))
		{
			itemInst.setEnchantLevel(getStat().getLevel());
			itemInst.updateDatabase();
		}
	}
	
	/**
	 * Stop feed.
	 */
	public synchronized void stopFeed()
	{
		if (_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
		}
	}
	
	/**
	 * Start feed.
	 * @param battleFeed the battle feed
	 */
	public synchronized void startFeed(boolean battleFeed)
	{
		// stop feeding task if its active
		stopFeed();
		if (!isDead())
		{
			int feedTime;
			if (battleFeed)
			{
				_feedMode = true;
				feedTime = _data.getPetFeedBattle();
			}
			else
			{
				_feedMode = false;
				feedTime = _data.getPetFeedNormal();
			}
			// pet feed time must be different than 0. Changing time to bypass divide by 0
			if (feedTime <= 0)
			{
				feedTime = 1;
			}
			
			_feedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 60000 / feedTime, 60000 / feedTime);
		}
	}
	
	@Override
	public synchronized void unSummon(PlayerInstance owner)
	{
		stopFeed();
		stopHpMpRegeneration();
		super.unSummon(owner);
		
		if (!isDead())
		{
			World.getInstance().removePet(owner.getObjectId());
		}
	}
	
	/**
	 * Restore the specified % of experience this PetInstance has lost.
	 * @param restorePercent the restore percent
	 */
	public void restoreExp(double restorePercent)
	{
		if (_expBeforeDeath > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp(Math.round(((_expBeforeDeath - getStat().getExp()) * restorePercent) / 100));
			_expBeforeDeath = 0;
		}
	}
	
	/**
	 * Death penalty.
	 */
	private void deathPenalty()
	{
		// TODO Need Correct Penalty
		final int lvl = getStat().getLevel();
		final double percentLost = (-0.07 * lvl) + 6.5;
		
		// Calculate the Experience loss
		final long lostExp = Math.round(((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost) / 100);
		
		// Get the Experience before applying penalty
		_expBeforeDeath = getStat().getExp();
		
		// Set the new Experience value of the PetInstance
		getStat().addExp(-lostExp);
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		if (getNpcId() == 12564)
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.SINEATER_XP_RATE), addToSp);
		}
		else
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.PET_XP_RATE), addToSp);
		}
	}
	
	@Override
	public long getExpForThisLevel()
	{
		return getStat().getExpForLevel(getStat().getLevel());
	}
	
	@Override
	public long getExpForNextLevel()
	{
		return getStat().getExpForLevel(getStat().getLevel() + 1);
	}
	
	@Override
	public int getLevel()
	{
		return getStat().getLevel();
	}
	
	/**
	 * Gets the max fed.
	 * @return the max fed
	 */
	public int getMaxFed()
	{
		return getStat().getMaxFeed();
	}
	
	@Override
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	@Override
	public int getCriticalHit(Creature target, Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	@Override
	public int getEvasionRate(Creature target)
	{
		return getStat().getEvasionRate(target);
	}
	
	@Override
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	@Override
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	@Override
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	@Override
	public int getMAtk(Creature target, Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	@Override
	public int getMDef(Creature target, Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		return getStat().getPAtk(target);
	}
	
	@Override
	public int getPDef(Creature target)
	{
		return getStat().getPDef(target);
	}
	
	@Override
	public int getSkillLevel(int skillId)
	{
		if ((getSkills() == null) || (getSkills().get(skillId) == null))
		{
			return 0;
		}
		final int lvl = getStat().getLevel();
		return lvl > 70 ? 7 + ((lvl - 70) / 5) : lvl / 10;
	}
	
	/**
	 * Update ref owner.
	 * @param owner the owner
	 */
	public void updateRefOwner(PlayerInstance owner)
	{
		final int oldOwnerId = getOwner().getObjectId();
		setOwner(owner);
		World.getInstance().removePet(oldOwnerId);
		World.getInstance().addPet(oldOwnerId, this);
	}
	
	@Override
	public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss)
		{
			return;
		}
		
		// Prevents the double spam of system messages, if the target is the owning player.
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (pcrit || mcrit)
			{
				getOwner().sendPacket(SystemMessageId.PET_S_CRITICAL_HIT);
			}
			
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_PET_HIT_FOR_S1_DAMAGE);
			sm.addNumber(damage);
			getOwner().sendPacket(sm);
		}
		
		if (getOwner().isInOlympiadMode() && (target instanceof PlayerInstance) && ((PlayerInstance) target).isInOlympiadMode() && (((PlayerInstance) target).getOlympiadGameId() == getOwner().getOlympiadGameId()))
		{
			Olympiad.getInstance().notifyCompetitorDamage(getOwner(), damage, getOwner().getOlympiadGameId());
		}
	}
	
	@Override
	public boolean isPet()
	{
		return true;
	}
}
