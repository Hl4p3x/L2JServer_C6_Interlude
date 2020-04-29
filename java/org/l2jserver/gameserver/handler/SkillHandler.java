
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
package org.l2jserver.gameserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.gameserver.handler.skillhandlers.BalanceLife;
import org.l2jserver.gameserver.handler.skillhandlers.BeastFeed;
import org.l2jserver.gameserver.handler.skillhandlers.Blow;
import org.l2jserver.gameserver.handler.skillhandlers.Charge;
import org.l2jserver.gameserver.handler.skillhandlers.ClanGate;
import org.l2jserver.gameserver.handler.skillhandlers.CombatPointHeal;
import org.l2jserver.gameserver.handler.skillhandlers.Continuous;
import org.l2jserver.gameserver.handler.skillhandlers.CpDam;
import org.l2jserver.gameserver.handler.skillhandlers.Craft;
import org.l2jserver.gameserver.handler.skillhandlers.DeluxeKey;
import org.l2jserver.gameserver.handler.skillhandlers.Disablers;
import org.l2jserver.gameserver.handler.skillhandlers.DrainSoul;
import org.l2jserver.gameserver.handler.skillhandlers.Fishing;
import org.l2jserver.gameserver.handler.skillhandlers.FishingSkill;
import org.l2jserver.gameserver.handler.skillhandlers.GetPlayer;
import org.l2jserver.gameserver.handler.skillhandlers.Harvest;
import org.l2jserver.gameserver.handler.skillhandlers.Heal;
import org.l2jserver.gameserver.handler.skillhandlers.ManaHeal;
import org.l2jserver.gameserver.handler.skillhandlers.Manadam;
import org.l2jserver.gameserver.handler.skillhandlers.Mdam;
import org.l2jserver.gameserver.handler.skillhandlers.Pdam;
import org.l2jserver.gameserver.handler.skillhandlers.Recall;
import org.l2jserver.gameserver.handler.skillhandlers.Resurrect;
import org.l2jserver.gameserver.handler.skillhandlers.SiegeFlag;
import org.l2jserver.gameserver.handler.skillhandlers.Sow;
import org.l2jserver.gameserver.handler.skillhandlers.Spoil;
import org.l2jserver.gameserver.handler.skillhandlers.StrSiegeAssault;
import org.l2jserver.gameserver.handler.skillhandlers.SummonFriend;
import org.l2jserver.gameserver.handler.skillhandlers.SummonTreasureKey;
import org.l2jserver.gameserver.handler.skillhandlers.Sweep;
import org.l2jserver.gameserver.handler.skillhandlers.TakeCastle;
import org.l2jserver.gameserver.handler.skillhandlers.Unlock;
import org.l2jserver.gameserver.handler.skillhandlers.ZakenPlayer;
import org.l2jserver.gameserver.handler.skillhandlers.ZakenSelf;
import org.l2jserver.gameserver.model.Skill.SkillType;

public class SkillHandler
{
	private static final Logger LOGGER = Logger.getLogger(SkillHandler.class.getName());
	
	private final Map<SkillType, ISkillHandler> _datatable;
	
	private SkillHandler()
	{
		_datatable = new HashMap<>();
		registerSkillHandler(new BalanceLife());
		registerSkillHandler(new BeastFeed());
		registerSkillHandler(new Blow());
		registerSkillHandler(new Charge());
		registerSkillHandler(new ClanGate());
		registerSkillHandler(new CombatPointHeal());
		registerSkillHandler(new Continuous());
		registerSkillHandler(new CpDam());
		registerSkillHandler(new Craft());
		registerSkillHandler(new DeluxeKey());
		registerSkillHandler(new Disablers());
		registerSkillHandler(new DrainSoul());
		registerSkillHandler(new Fishing());
		registerSkillHandler(new FishingSkill());
		registerSkillHandler(new GetPlayer());
		registerSkillHandler(new Harvest());
		registerSkillHandler(new Heal());
		registerSkillHandler(new Manadam());
		registerSkillHandler(new ManaHeal());
		registerSkillHandler(new Mdam());
		registerSkillHandler(new Pdam());
		registerSkillHandler(new Recall());
		registerSkillHandler(new Resurrect());
		registerSkillHandler(new SiegeFlag());
		registerSkillHandler(new Sow());
		registerSkillHandler(new Spoil());
		registerSkillHandler(new StrSiegeAssault());
		registerSkillHandler(new SummonFriend());
		registerSkillHandler(new SummonTreasureKey());
		registerSkillHandler(new Sweep());
		registerSkillHandler(new TakeCastle());
		registerSkillHandler(new Unlock());
		registerSkillHandler(new ZakenPlayer());
		registerSkillHandler(new ZakenSelf());
		
		LOGGER.info("SkillHandler: Loaded " + _datatable.size() + " handlers.");
	}
	
	public void registerSkillHandler(ISkillHandler handler)
	{
		final SkillType[] types = handler.getSkillIds();
		for (SkillType t : types)
		{
			_datatable.put(t, handler);
		}
	}
	
	public ISkillHandler getSkillHandler(SkillType skillType)
	{
		return _datatable.get(skillType);
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler INSTANCE = new SkillHandler();
	}
}