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

import java.util.List;

import org.l2jserver.Config;
import org.l2jserver.gameserver.datatables.SkillTable;
import org.l2jserver.gameserver.datatables.sql.SkillTreeTable;
import org.l2jserver.gameserver.model.EnchantSkillLearn;
import org.l2jserver.gameserver.model.Skill;
import org.l2jserver.gameserver.model.SkillLearn;
import org.l2jserver.gameserver.model.actor.templates.NpcTemplate;
import org.l2jserver.gameserver.model.base.ClassId;
import org.l2jserver.gameserver.network.SystemMessageId;
import org.l2jserver.gameserver.network.serverpackets.ActionFailed;
import org.l2jserver.gameserver.network.serverpackets.AquireSkillList;
import org.l2jserver.gameserver.network.serverpackets.ExEnchantSkillList;
import org.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * The Class FolkInstance.
 */
public class FolkInstance extends NpcInstance
{
	private final List<ClassId> _classesToTeach;
	
	/**
	 * Instantiates a new folk instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public FolkInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_classesToTeach = template.getTeachInfo();
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		player.setLastFolkNPC(this);
		super.onAction(player);
	}
	
	/**
	 * this displays SkillList to the player.
	 * @param player the player
	 * @param classId the class id
	 */
	public void showSkillList(PlayerInstance player, ClassId classId)
	{
		final int npcId = getTemplate().getNpcId();
		if (_classesToTeach == null)
		{
			if (player.isGM())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				final StringBuilder sb = new StringBuilder();
				sb.append("<html><body>");
				sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
				return;
			}
			
			player.sendMessage("Error learning skills, contact the admin.");
			return;
		}
		
		if (!getTemplate().canTeach(classId))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		
		final SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		final AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Usual);
		int counts = 0;
		for (SkillLearn s : skills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if ((sk == null) || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
			{
				continue;
			}
			
			final int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;
			
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}
		
		if (counts == 0)
		{
			final int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);
			if (minlevel > 0)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN));
			}
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * this displays EnchantSkillList to the player.
	 * @param player the player
	 * @param classId the class id
	 */
	public void showEnchantSkillList(PlayerInstance player, ClassId classId)
	{
		final int npcId = getTemplate().getNpcId();
		if (_classesToTeach == null)
		{
			if (player.isGM())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				final StringBuilder sb = new StringBuilder();
				sb.append("<html><body>");
				sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
				return;
			}
			
			player.sendMessage("Error learning skills, contact the admin.");
			return;
		}
		
		if (!getTemplate().canTeach(classId))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		if (player.getClassId().getId() < 88)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		
		final EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
		final ExEnchantSkillList esl = new ExEnchantSkillList();
		int counts = 0;
		for (EnchantSkillLearn s : skills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
			{
				continue;
			}
			counts++;
			esl.addSkill(s.getId(), s.getLevel(), s.getSpCost(), s.getExp());
		}
		if (counts == 0)
		{
			player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final int level = player.getLevel();
			if (level < 74)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addNumber(level);
				player.sendPacket(sm);
			}
			else
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<html><body>");
				sb.append("You've learned all skills for your class.<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(esl);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.startsWith("SkillList"))
		{
			if (Config.ALT_GAME_SKILL_LEARN)
			{
				final String id = command.substring(9).trim();
				if (id.length() != 0)
				{
					player.setSkillLearningClassId(ClassId.getClassId(Integer.parseInt(id)));
					showSkillList(player, ClassId.getClassId(Integer.parseInt(id)));
				}
				else
				{
					boolean ownClass = false;
					for (ClassId cid : _classesToTeach)
					{
						if (cid.equalsOrChildOf(player.getClassId()))
						{
							ownClass = true;
							break;
						}
					}
					
					String text = "<html><body><center>Skill learning:</center><br>";
					if (!ownClass)
					{
						final String mages = player.getClassId().isMage() ? "fighters" : "mages";
						text += "Skills of your class are the easiest to learn.<br>Skills of another class are harder.<br>Skills for another race are even more hard to learn.<br>You can also learn skills of " + mages + ", and they are the hardest to learn!<br><br>";
					}
					
					// make a list of classes
					if (!_classesToTeach.isEmpty())
					{
						int count = 0;
						ClassId classCheck = player.getClassId();
						
						while ((count == 0) && (classCheck != null))
						{
							for (ClassId cid : _classesToTeach)
							{
								if (cid.level() != classCheck.level())
								{
									continue;
								}
								
								if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
								{
									continue;
								}
								
								text += "<a action=\"bypass -h npc_%objectId%_SkillList " + cid.getId() + "\">Learn " + cid + "'s class Skills</a><br>\n";
								count++;
							}
							classCheck = classCheck.getParent();
						}
					}
					else
					{
						text += "No Skills.<br>";
					}
					
					text += "</body></html>";
					insertObjectIdAndShowChatWindow(player, text);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else
			{
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player, player.getClassId());
			}
		}
		else if (command.startsWith("EnchantSkillList"))
		{
			showEnchantSkillList(player, player.getClassId());
		}
		else
		{
			// this class dont know any other commands, let forward the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}
}