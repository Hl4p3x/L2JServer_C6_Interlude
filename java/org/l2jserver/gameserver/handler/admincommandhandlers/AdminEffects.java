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
package org.l2jserver.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.l2jserver.gameserver.handler.IAdminCommandHandler;
import org.l2jserver.gameserver.model.World;
import org.l2jserver.gameserver.model.WorldObject;
import org.l2jserver.gameserver.model.actor.Creature;
import org.l2jserver.gameserver.model.actor.Summon;
import org.l2jserver.gameserver.model.actor.instance.ChestInstance;
import org.l2jserver.gameserver.model.actor.instance.NpcInstance;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.CharInfo;
import org.l2jserver.gameserver.network.serverpackets.Earthquake;
import org.l2jserver.gameserver.network.serverpackets.ExRedSky;
import org.l2jserver.gameserver.network.serverpackets.GameServerPacket;
import org.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jserver.gameserver.network.serverpackets.PlaySound;
import org.l2jserver.gameserver.network.serverpackets.SignsSky;
import org.l2jserver.gameserver.network.serverpackets.SocialAction;
import org.l2jserver.gameserver.network.serverpackets.StopMove;
import org.l2jserver.gameserver.network.serverpackets.SunRise;
import org.l2jserver.gameserver.network.serverpackets.SunSet;
import org.l2jserver.gameserver.network.serverpackets.UserInfo;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands:
 * <li>invis/invisible/vis/visible = makes yourself invisible or visible
 * <li>earthquake = causes an earthquake of a given intensity and duration around you
 * <li>bighead/shrinkhead = changes head size
 * <li>gmspeed = temporary Super Haste effect.
 * <li>para/unpara = paralyze/remove paralysis from target
 * <li>para_all/unpara_all = same as para/unpara, affects the whole world.
 * <li>polyself/unpolyself = makes you look as a specified mob.
 * <li>changename = temporary change name
 * <li>clearteams/setteam_close/setteam = team related commands
 * <li>social = forces an Creature instance to broadcast social action packets.
 * <li>effect = forces an Creature instance to broadcast MSU packets.
 * <li>abnormal = force changes over an Creature instance's abnormal state.
 * <li>play_sound/play_sounds = Music broadcasting related commands
 * <li>atmosphere = sky change related commands.
 */
public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_invis",
		"admin_invis_menu_main",
		"admin_invisible",
		"admin_vis",
		"admin_visible",
		"admin_invis_menu",
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_bighead",
		"admin_shrinkhead",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_polyself",
		"admin_unpolyself",
		"admin_polyself_menu",
		"admin_unpolyself_menu",
		"admin_clearteams",
		"admin_setteam_close",
		"admin_setteam",
		"admin_social",
		"admin_effect",
		"admin_social_menu",
		"admin_effect_menu",
		"admin_abnormal",
		"admin_abnormal_menu",
		"admin_play_sounds",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu",
		"admin_npc_say",
		"admin_debuff"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_invis_menu"))
		{
			if (!activeChar.getAppearance().isInvisible())
			{
				activeChar.getAppearance().setInvisible();
				activeChar.decayMe();
				activeChar.broadcastUserInfo();
				activeChar.spawnMe();
				BuilderUtil.sendSysMessage(activeChar, "Now, you cannot be seen.");
			}
			else
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
				BuilderUtil.sendSysMessage(activeChar, "Now, you can be seen.");
			}
		}
		else if (command.startsWith("admin_invis"))
		{
			activeChar.getAppearance().setInvisible();
			activeChar.decayMe();
			activeChar.broadcastUserInfo();
			activeChar.spawnMe();
			BuilderUtil.sendSysMessage(activeChar, "Now, you cannot be seen.");
		}
		else if (command.startsWith("admin_vis"))
		{
			activeChar.getAppearance().setVisible();
			activeChar.broadcastUserInfo();
			BuilderUtil.sendSysMessage(activeChar, "Now, you can be seen.");
		}
		else if (command.startsWith("admin_earthquake"))
		{
			try
			{
				final String val1 = st.nextToken();
				final int intensity = Integer.parseInt(val1);
				final String val2 = st.nextToken();
				final int duration = Integer.parseInt(val2);
				activeChar.broadcastPacket(new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration));
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Use: //earthquake <intensity> <duration>");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				final String type = st.nextToken();
				final String state = st.nextToken();
				adminAtmosphere(type, state, activeChar);
			}
			catch (Exception ex)
			{
			}
		}
		else if (command.startsWith("admin_npc_say"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				if (activeChar.getSayMode() != null)
				{
					activeChar.setSayMode(null);
					BuilderUtil.sendSysMessage(activeChar, "NpcSay mode off");
				}
				else if ((target != null) && (target instanceof NpcInstance))
				{
					activeChar.setSayMode(target);
					BuilderUtil.sendSysMessage(activeChar, "NpcSay mode on for " + target.getName());
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
					return false;
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Target Npc before. Use: //npc_say");
			}
		}
		else if (command.equals("admin_play_sounds"))
		{
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		}
		else if (command.startsWith("admin_play_sounds"))
		{
			try
			{
				AdminHelpPage.showHelpPage(activeChar, "songs/songs" + command.substring(17) + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.equals("admin_para") || command.equals("admin_para_menu"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch (Exception e)
			{
			}
			try
			{
				final WorldObject target = activeChar.getTarget();
				Creature creature = null;
				if (target instanceof Creature)
				{
					creature = (Creature) target;
					if (type.equals("1"))
					{
						creature.startAbnormalEffect(0x0400);
					}
					else
					{
						creature.startAbnormalEffect(0x0800);
					}
					creature.setParalyzed(true);
					final StopMove sm = new StopMove(creature);
					creature.sendPacket(sm);
					creature.broadcastPacket(sm);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.equals("admin_unpara") || command.equals("admin_unpara_menu"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				Creature creature = null;
				if (target instanceof Creature)
				{
					creature = (Creature) target;
					creature.stopAbnormalEffect((short) 0x0400);
					creature.setParalyzed(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_para_all"))
		{
			try
			{
				for (PlayerInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (!player.isGM())
					{
						player.startAbnormalEffect(0x0400);
						player.setParalyzed(true);
						final StopMove sm = new StopMove(player);
						player.sendPacket(sm);
						player.broadcastPacket(sm);
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unpara_all"))
		{
			try
			{
				for (PlayerInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.stopAbnormalEffect(0x0400);
					player.setParalyzed(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_bighead"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				Creature creature = null;
				if (target instanceof Creature)
				{
					creature = (Creature) target;
					creature.startAbnormalEffect(0x2000);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_shrinkhead"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				Creature creature = null;
				if (target instanceof Creature)
				{
					creature = (Creature) target;
					creature.stopAbnormalEffect((short) 0x2000);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_polyself"))
		{
			try
			{
				final String id = st.nextToken();
				activeChar.getPoly().setPolyInfo("npc", id);
				activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
				activeChar.broadcastPacket(new CharInfo(activeChar));
				activeChar.sendPacket(new UserInfo(activeChar));
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unpolyself"))
		{
			try
			{
				activeChar.getPoly().setPolyInfo(null, "1");
				activeChar.decayMe();
				activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				activeChar.broadcastPacket(new CharInfo(activeChar));
				activeChar.sendPacket(new UserInfo(activeChar));
			}
			catch (Exception e)
			{
			}
		}
		else if (command.equals("admin_clear_teams"))
		{
			try
			{
				for (PlayerInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.setTeam(0);
					player.broadcastUserInfo();
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_setteam_close"))
		{
			try
			{
				final String val = st.nextToken();
				final int teamVal = Integer.parseInt(val);
				for (PlayerInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (activeChar.isInsideRadius(player, 400, false, true))
					{
						player.setTeam(0);
						
						if (teamVal != 0)
						{
							BuilderUtil.sendSysMessage(player, "You have joined team " + teamVal);
						}
						
						player.broadcastUserInfo();
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_setteam"))
		{
			final String val = command.substring(14);
			final int teamVal = Integer.parseInt(val);
			final WorldObject target = activeChar.getTarget();
			PlayerInstance player = null;
			if (target instanceof PlayerInstance)
			{
				player = (PlayerInstance) target;
			}
			else
			{
				return false;
			}
			
			player.setTeam(teamVal);
			
			if (teamVal != 0)
			{
				BuilderUtil.sendSysMessage(player, "You have joined team " + teamVal);
			}
			
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				WorldObject obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					final int social = Integer.parseInt(st.nextToken());
					target = st.nextToken();
					if (target != null)
					{
						final PlayerInstance player = World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performSocial(social, player, activeChar))
							{
								BuilderUtil.sendSysMessage(activeChar, player.getName() + " was affected by your request.");
							}
						}
						else
						{
							try
							{
								final int radius = Integer.parseInt(target);
								for (WorldObject object : activeChar.getKnownList().getKnownObjects().values())
								{
									if (activeChar.isInsideRadius(object, radius, false, false))
									{
										performSocial(social, object, activeChar);
									}
								}
								
								BuilderUtil.sendSysMessage(activeChar, radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								BuilderUtil.sendSysMessage(activeChar, "Incorrect parameter");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					final int social = Integer.parseInt(st.nextToken());
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performSocial(social, obj, activeChar))
					{
						BuilderUtil.sendSysMessage(activeChar, obj.getName() + " was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //social <social_id> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("debuff"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				Creature creature = null;
				if (target instanceof Creature)
				{
					creature = (Creature) target;
					creature.stopAllEffects();
					BuilderUtil.sendSysMessage(activeChar, "Effects has been cleared from " + creature + ".");
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_abnormal"))
		{
			try
			{
				String target = null;
				WorldObject obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					final String parm = st.nextToken();
					final int abnormal = Integer.decode("0x" + parm);
					target = st.nextToken();
					if (target != null)
					{
						final PlayerInstance player = World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performAbnormal(abnormal, player))
							{
								BuilderUtil.sendSysMessage(activeChar, player.getName() + "'s abnormal status was affected by your request.");
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
							}
						}
						else
						{
							try
							{
								final int radius = Integer.parseInt(target);
								for (WorldObject object : activeChar.getKnownList().getKnownObjects().values())
								{
									if (activeChar.isInsideRadius(object, radius, false, false))
									{
										performAbnormal(abnormal, object);
									}
								}
								
								BuilderUtil.sendSysMessage(activeChar, radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								BuilderUtil.sendSysMessage(activeChar, "Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					final int abnormal = Integer.decode("0x" + st.nextToken());
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performAbnormal(abnormal, obj))
					{
						BuilderUtil.sendSysMessage(activeChar, obj.getName() + "'s abnormal status was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //abnormal <abnormal_mask> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				WorldObject obj = activeChar.getTarget();
				int level = 1;
				int hittime = 1;
				final int skill = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken());
				}
				
				if (st.hasMoreTokens())
				{
					hittime = Integer.parseInt(st.nextToken());
				}
				
				if (obj == null)
				{
					obj = activeChar;
				}
				
				if (!(obj instanceof Creature))
				{
					activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				}
				else
				{
					final Creature target = (Creature) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					BuilderUtil.sendSysMessage(activeChar, obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //effect skill [level | level hittime]");
			}
		}
		
		if (command.contains("menu"))
		{
			showMainPage(activeChar, command);
		}
		
		return true;
	}
	
	/**
	 * @param action bitmask that should be applied over target's abnormal
	 * @param target
	 * @return <i>true</i> if target's abnormal state was affected , <i>false</i> otherwise.
	 */
	private boolean performAbnormal(int action, WorldObject target)
	{
		if (target instanceof Creature)
		{
			final Creature creature = (Creature) target;
			if ((creature.getAbnormalEffect() & action) == action)
			{
				creature.stopAbnormalEffect(action);
			}
			else
			{
				creature.startAbnormalEffect(action);
			}
			return true;
		}
		return false;
	}
	
	private boolean performSocial(int action, WorldObject target, PlayerInstance activeChar)
	{
		try
		{
			if (target instanceof Creature)
			{
				if ((target instanceof Summon) || (target instanceof ChestInstance))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					
					return false;
				}
				
				if ((target instanceof NpcInstance) && ((action < 1) || (action > 3)))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					
					return false;
				}
				
				if ((target instanceof PlayerInstance) && ((action < 2) || (action > 16)))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					
					return false;
				}
				
				final Creature creature = (Creature) target;
				creature.broadcastPacket(new SocialAction(target.getObjectId(), action));
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
		}
		return true;
	}
	
	/**
	 * @param type - atmosphere type (signssky,sky)
	 * @param state - atmosphere state(night,day)
	 * @param activeChar
	 */
	private void adminAtmosphere(String type, String state, PlayerInstance activeChar)
	{
		GameServerPacket packet = null;
		
		switch (type)
		{
			case "signsky":
			{
				if (state.equals("dawn"))
				{
					packet = new SignsSky(2);
				}
				else if (state.equals("dusk"))
				{
					packet = new SignsSky(1);
				}
				break;
			}
			case "sky":
			{
				if (state.equals("night"))
				{
					packet = new SunSet();
				}
				else if (state.equals("day"))
				{
					packet = new SunRise();
				}
				else if (state.equals("red"))
				{
					packet = new ExRedSky(10);
				}
				break;
			}
			default:
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
				break;
			}
		}
		
		if (packet != null)
		{
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				player.sendPacket(packet);
			}
		}
	}
	
	private void playAdminSound(PlayerInstance activeChar, String sound)
	{
		final PlaySound snd = new PlaySound(1, sound);
		activeChar.sendPacket(snd);
		activeChar.broadcastPacket(snd);
		BuilderUtil.sendSysMessage(activeChar, "Playing " + sound + ".");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(PlayerInstance activeChar, String command)
	{
		String filename = "effects_menu";
		if (command.contains("menu_main"))
		{
			filename = "main_menu";
		}
		else if (command.contains("abnormal"))
		{
			filename = "abnormal";
		}
		else if (command.contains("social"))
		{
			filename = "social";
		}
		
		AdminHelpPage.showHelpPage(activeChar, filename + ".htm");
	}
}
