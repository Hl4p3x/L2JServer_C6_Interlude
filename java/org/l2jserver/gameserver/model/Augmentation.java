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
package org.l2jserver.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.xml.AugmentationData;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.items.instance.ItemInstance;
import org.l2jserver.gameserver.model.skills.Stat;
import org.l2jserver.gameserver.model.skills.funcs.FuncAdd;
import org.l2jserver.gameserver.model.skills.funcs.LambdaConst;

/**
 * Used to store an augmentation and its bonus
 * @author durgus
 */
public class Augmentation
{
	private static final Logger LOGGER = Logger.getLogger(Augmentation.class.getName());
	
	private final ItemInstance _item;
	private int _effectsId = 0;
	private augmentationStatBonus _bonus = null;
	private Skill _skill = null;
	
	public Augmentation(ItemInstance item, int effects, Skill skill, boolean save)
	{
		_item = item;
		_effectsId = effects;
		_bonus = new augmentationStatBonus(_effectsId);
		_skill = skill;
		
		// write to DB if save is true
		if (save)
		{
			saveAugmentationData();
		}
	}
	
	public Augmentation(ItemInstance item, int effects, int skill, int skillLevel, boolean save)
	{
		this(item, effects, SkillTable.getInstance().getInfo(skill, skillLevel), save);
	}
	
	public class augmentationStatBonus
	{
		private final Stat[] _stats;
		private final float[] _values;
		private boolean _active;
		
		public augmentationStatBonus(int augmentationId)
		{
			_active = false;
			final List<AugmentationData.AugStat> as = AugmentationData.getInstance().getAugStatsById(augmentationId);
			_stats = new Stat[as.size()];
			_values = new float[as.size()];
			int i = 0;
			for (AugmentationData.AugStat aStat : as)
			{
				_stats[i] = aStat.getStat();
				_values[i] = aStat.getValue();
				i++;
			}
		}
		
		public void applyBonus(PlayerInstance player)
		{
			// make sure the bonus are not applyed twice..
			if (_active)
			{
				return;
			}
			
			for (int i = 0; i < _stats.length; i++)
			{
				player.addStatFunc(new FuncAdd(_stats[i], 0x40, this, new LambdaConst(_values[i])));
			}
			
			_active = true;
		}
		
		public void removeBonus(PlayerInstance player)
		{
			// make sure the bonus is not removed twice
			if (!_active)
			{
				return;
			}
			
			player.removeStatsOwner(this);
			
			_active = false;
		}
	}
	
	private void saveAugmentationData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO augmentations (item_id,attributes,skill,level) VALUES (?,?,?,?)");
			statement.setInt(1, _item.getObjectId());
			statement.setInt(2, _effectsId);
			if (_skill != null)
			{
				statement.setInt(3, _skill.getId());
				statement.setInt(4, _skill.getLevel());
			}
			else
			{
				statement.setInt(3, 0);
				statement.setInt(4, 0);
			}
			
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not save augmentation for item: " + _item.getObjectId() + " from DB: " + e);
		}
	}
	
	public void deleteAugmentationData()
	{
		if (!_item.isAugmented())
		{
			return;
		}
		
		// delete the augmentation from the database
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM augmentations WHERE item_id=?");
			statement.setInt(1, _item.getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not delete augmentation for item: " + _item.getObjectId() + " from DB: " + e);
		}
	}
	
	/**
	 * Get the augmentation "id" used in serverpackets.
	 * @return augmentationId
	 */
	public int getAugmentationId()
	{
		return _effectsId;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	/**
	 * Applies the bonus to the player.
	 * @param player
	 */
	public void applyBonus(PlayerInstance player)
	{
		_bonus.applyBonus(player);
		
		// add the skill if any
		if (_skill != null)
		{
			player.addSkill(_skill);
			
			if (_skill.isActive() && (Config.ACTIVE_AUGMENTS_START_REUSE_TIME > 0))
			{
				player.disableSkill(_skill, Config.ACTIVE_AUGMENTS_START_REUSE_TIME);
				player.addTimestamp(_skill, Config.ACTIVE_AUGMENTS_START_REUSE_TIME);
			}
			
			player.sendSkillList();
		}
	}
	
	/**
	 * Removes the augmentation bonus from the player.
	 * @param player
	 */
	public void removeBonus(PlayerInstance player)
	{
		_bonus.removeBonus(player);
		
		// remove the skill if any
		if (_skill != null)
		{
			if (_skill.isPassive())
			{
				player.removeSkill(_skill);
			}
			else
			{
				player.removeSkill(_skill, false);
			}
			
			if ((_skill.isPassive() && Config.DELETE_AUGM_PASSIVE_ON_CHANGE) || (_skill.isActive() && Config.DELETE_AUGM_ACTIVE_ON_CHANGE))
			{
				// Iterate through all effects currently on the character.
				final Effect[] effects = player.getAllEffects();
				for (Effect currenteffect : effects)
				{
					final Skill effectSkill = currenteffect.getSkill();
					if (effectSkill.getId() == _skill.getId())
					{
						player.sendMessage("You feel the power of " + effectSkill.getName() + " leaving yourself.");
						currenteffect.exit(false);
					}
				}
			}
			
			player.sendSkillList();
		}
	}
}
