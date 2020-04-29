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
package ai.others;

import java.util.ArrayList;

import org.l2jserver.Config;
import org.l2jserver.commons.util.Rnd;
import org.l2jserver.gameserver.ai.CtrlIntention;
import org.l2jserver.gameserver.enums.ChatType;
import org.l2jserver.gameserver.model.actor.Attackable;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.quest.EventType;
import org.l2jserver.gameserver.model.quest.Quest;
import org.l2jserver.gameserver.network.serverpackets.CreatureSay;

public class Transform extends Quest
{
	private final ArrayList<Transformer> _mobs = new ArrayList<>();
	
	private static class Transformer
	{
		private final int _id;
		private final int _idPoly;
		private final int _chance;
		private final int _message;
		
		protected Transformer(int id, int idPoly, int chance, int message)
		{
			_id = id;
			_idPoly = idPoly;
			_chance = chance;
			_message = message;
		}
		
		protected int getId()
		{
			return _id;
		}
		
		protected int getIdPoly()
		{
			return _idPoly;
		}
		
		protected int getChance()
		{
			return _chance;
		}
		
		protected int getMessage()
		{
			return _message;
		}
	}
	
	private static String[] Message =
	{
		"I cannot despise the fellow! I see his sincerity in the duel.",
		"Nows we truly begin!",
		"Fool! Right now is only practice!",
		"Have a look at my true strength.",
		"This time at the last! The end!"
	};
	
	public Transform()
	{
		super(-1, "ai");
		
		_mobs.add(new Transformer(21261, 21262, 1, 5)); // 1st mutation Ol Mahum Transcender
		_mobs.add(new Transformer(21262, 21263, 1, 5)); // 2st mutation Ol Mahum Transcender
		_mobs.add(new Transformer(21263, 21264, 1, 5)); // 3rd mutation Ol Mahum Transcender
		_mobs.add(new Transformer(21258, 21259, 100, 5)); // always mutation on atk Fallen Orc Shaman
		_mobs.add(new Transformer(20835, 21608, 1, 5)); // zaken's seer to zaken's watchman
		_mobs.add(new Transformer(21608, 21609, 1, 5)); // zaken's watchman
		_mobs.add(new Transformer(20832, 21602, 1, 5)); // Zaken's pikeman
		_mobs.add(new Transformer(21602, 21603, 1, 5)); // Zaken's pikeman
		_mobs.add(new Transformer(20833, 21605, 1, 5)); // Zaken's archet
		_mobs.add(new Transformer(21605, 21606, 1, 5)); // Zaken's archet
		_mobs.add(new Transformer(21625, 21623, 1, 5)); // zaken's Elite Guard to zaken's Guard
		_mobs.add(new Transformer(21623, 21624, 1, 5)); // zaken's Guard
		_mobs.add(new Transformer(20842, 21620, 1, 5)); // Musveren
		_mobs.add(new Transformer(21620, 21621, 1, 5)); // Musveren
		_mobs.add(new Transformer(20830, 20859, 100, 0)); //
		_mobs.add(new Transformer(21067, 21068, 100, 0)); //
		_mobs.add(new Transformer(21062, 21063, 100, 0)); // Angels
		_mobs.add(new Transformer(20831, 20860, 100, 0)); //
		_mobs.add(new Transformer(21070, 21071, 100, 0)); //
		
		final int[] mobsKill =
		{
			20830,
			21067,
			21062,
			20831,
			21070
		};
		
		for (int mob : mobsKill)
		{
			addEventId(mob, EventType.ON_KILL);
		}
		
		final int[] mobsAttack =
		{
			21620,
			20842,
			21623,
			21625,
			21605,
			20833,
			21602,
			20832,
			21608,
			20835,
			21258
		};
		
		for (int mob : mobsAttack)
		{
			addEventId(mob, EventType.ON_ATTACK);
		}
	}
	
	@Override
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		for (Transformer monster : _mobs)
		{
			if ((npc.getNpcId() == monster.getId()) && (Rnd.get(100) <= (monster.getChance() * Config.RATE_DROP_QUEST)))
			{
				if (monster.getMessage() != 0)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), Message[Rnd.get(monster.getMessage())]));
				}
				npc.onDecay();
				final Attackable newNpc = (Attackable) addSpawn(monster.getIdPoly(), npc);
				final Creature originalAttacker = isPet ? attacker.getPet() : attacker;
				newNpc.setRunning();
				newNpc.addDamageHate(originalAttacker, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
				
				// NPC Spawn Effect L2OFF
				final NPCSpawnTask spawnEffectTask = new NPCSpawnTask(newNpc, 4000, 800000);
				final Thread effectThread = new Thread(spawnEffectTask);
				effectThread.start();
				
				// Like L2OFF auto target new mob (like an aggression)
				originalAttacker.setTargetTrasformedNpc(newNpc);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance killer, boolean isPet)
	{
		for (Transformer monster : _mobs)
		{
			if (npc.getNpcId() == monster.getId())
			{
				if (monster.getMessage() != 0)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), Message[Rnd.get(monster.getMessage())]));
				}
				final Attackable newNpc = (Attackable) addSpawn(monster.getIdPoly(), npc);
				final Creature originalAttacker = isPet ? killer.getPet() : killer;
				newNpc.setRunning();
				newNpc.addDamageHate(originalAttacker, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	private class NPCSpawnTask implements Runnable
	{
		private final NpcInstance spawn;
		private final long spawnEffectTime;
		private final int spawnAbnormalEffect;
		
		/**
		 * @param spawn
		 * @param spawnEffectTime
		 * @param spawnAbnormalEffect
		 */
		public NPCSpawnTask(NpcInstance spawn, long spawnEffectTime, int spawnAbnormalEffect)
		{
			super();
			this.spawn = spawn;
			this.spawnEffectTime = spawnEffectTime;
			this.spawnAbnormalEffect = Integer.decode("0x" + spawnAbnormalEffect);
		}
		
		@Override
		public void run()
		{
			spawn.startAbnormalEffect(spawnAbnormalEffect);
			
			try
			{
				Thread.sleep(spawnEffectTime);
			}
			catch (InterruptedException e)
			{
			}
			
			spawn.stopAbnormalEffect(spawnAbnormalEffect);
		}
	}
	
	public static void main(String[] args)
	{
		new Transform();
	}
}