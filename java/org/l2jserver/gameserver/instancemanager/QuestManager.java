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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jserver.gameserver.model.quest.Quest;

public class QuestManager
{
	protected static final Logger LOGGER = Logger.getLogger(QuestManager.class.getName());
	
	private static Map<String, Quest> _quests = new HashMap<>();
	private static int _questCount = 0;
	
	private QuestManager()
	{
	}
	
	public boolean reload(String questFolder)
	{
		final Quest q = getQuest(questFolder);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	/**
	 * Reloads a the quest given by questId.<br>
	 * <b>NOTICE: Will only work if the quest name is equal the quest folder name</b>
	 * @param questId The id of the quest to be reloaded
	 * @return true if reload was succesful, false otherwise
	 */
	public boolean reload(int questId)
	{
		final Quest q = getQuest(questId);
		if (q == null)
		{
			return false;
		}
		return q.reload();
	}
	
	public void reloadAllQuests()
	{
		// LOGGER.info("Reloading Server Scripts");
		//// unload all scripts
		// for (Quest quest : _quests.values())
		// {
		// if (quest != null)
		// {
		// quest.unload();
		// }
		// }
		//// now load all scripts
		// final File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
		// ScriptEngineManager.getInstance().executeScriptsList(scripts);
		// getInstance().report();
	}
	
	public void report()
	{
		LOGGER.info("Loaded: " + _questCount + " quest scripts.");
	}
	
	public void save()
	{
		for (Quest q : getQuests().values())
		{
			q.saveGlobalData();
		}
	}
	
	public Quest getQuest(String name)
	{
		return getQuests().get(name);
	}
	
	public Quest getQuest(int questId)
	{
		for (Quest q : getQuests().values())
		{
			if (q.getQuestId() == questId)
			{
				return q;
			}
		}
		return null;
	}
	
	public void addQuest(Quest newQuest)
	{
		if (getQuests().containsKey(newQuest.getName()))
		{
			LOGGER.info("Replaced: " + newQuest.getName() + " with a new version.");
		}
		else if (newQuest.getQuestId() > 0)
		{
			_questCount++;
		}
		
		// Note: HastMap will replace the old value if the key already exists so there is no need to explicitly try to remove the old reference.
		getQuests().put(newQuest.getName(), newQuest);
	}
	
	public Map<String, Quest> getQuests()
	{
		if (_quests == null)
		{
			_quests = new HashMap<>();
		}
		return _quests;
	}
	
	public Iterable<Quest> getAllManagedScripts()
	{
		return _quests.values();
	}
	
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
	
	public String getScriptManagerName()
	{
		return "QuestManager";
	}
	
	public boolean removeQuest(Quest q)
	{
		return _quests.remove(q.getName()) != null;
	}
	
	public void unloadAllQuests()
	{
		LOGGER.info("Unloading Server Quests");
		// unload all scripts
		for (Quest quest : _quests.values())
		{
			if (quest != null)
			{
				quest.unload();
			}
		}
		getInstance().report();
	}
	
	public static QuestManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestManager INSTANCE = new QuestManager();
	}
}
