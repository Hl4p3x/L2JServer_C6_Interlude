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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jserver.commons.database.DatabaseFactory;
import org.l2jserver.gameserver.model.Macro.MacroCmd;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.network.serverpackets.SendMacroList;

/**
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/02 15:38:41 $
 */
public class MacroList
{
	private static final Logger LOGGER = Logger.getLogger(MacroList.class.getName());
	
	private final PlayerInstance _owner;
	private int _revision;
	private int _macroId;
	private final Map<Integer, Macro> _macroses = new HashMap<>();
	
	public MacroList(PlayerInstance owner)
	{
		_owner = owner;
		_revision = 1;
		_macroId = 1000;
	}
	
	public int getRevision()
	{
		return _revision;
	}
	
	public Macro[] getAllMacroses()
	{
		return _macroses.values().toArray(new Macro[_macroses.size()]);
	}
	
	public Macro getMacro(int id)
	{
		return _macroses.get(id - 1);
	}
	
	public void registerMacro(Macro macro)
	{
		if (macro.id == 0)
		{
			macro.id = _macroId++;
			
			while (_macroses.get(macro.id) != null)
			{
				macro.id = _macroId++;
			}
			
			_macroses.put(macro.id, macro);
			registerMacroInDb(macro);
		}
		else
		{
			final Macro old = _macroses.put(macro.id, macro);
			
			if (old != null)
			{
				deleteMacroFromDb(old);
			}
			
			registerMacroInDb(macro);
		}
		sendUpdate();
	}
	
	public void deleteMacro(int id)
	{
		final Macro toRemove = _macroses.get(id);
		if (toRemove != null)
		{
			deleteMacroFromDb(toRemove);
		}
		
		_macroses.remove(id);
		
		final ShortCut[] allShortCuts = _owner.getAllShortCuts();
		for (ShortCut sc : allShortCuts)
		{
			if ((sc.getId() == id) && (sc.getType() == ShortCut.TYPE_MACRO))
			{
				_owner.deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		sendUpdate();
	}
	
	public void sendUpdate()
	{
		_revision++;
		
		final Macro[] all = getAllMacroses();
		if (all.length == 0)
		{
			_owner.sendPacket(new SendMacroList(_revision, all.length, null));
		}
		else
		{
			for (Macro m : all)
			{
				_owner.sendPacket(new SendMacroList(_revision, all.length, m));
			}
		}
	}
	
	private void registerMacroInDb(Macro macro)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("INSERT INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.setInt(3, macro.icon);
			statement.setString(4, macro.name);
			statement.setString(5, macro.descr);
			statement.setString(6, macro.acronym);
			
			final StringBuilder sb = new StringBuilder();
			for (MacroCmd cmd : macro.commands)
			{
				final StringBuilder cmdSb = new StringBuilder();
				cmdSb.append(cmd.type).append(',');
				cmdSb.append(cmd.d1).append(',');
				cmdSb.append(cmd.d2);
				
				if ((cmd.cmd != null) && (cmd.cmd.length() > 0))
				{
					cmdSb.append(',').append(cmd.cmd);
				}
				
				cmdSb.append(';');
				
				if ((sb.toString().length() + cmdSb.toString().length()) < 255)
				{
					sb.append(cmdSb.toString());
				}
				else
				{
					break;
				}
			}
			
			statement.setString(7, sb.toString());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.info("Player: " + _owner.getName() + " IP:" + _owner.getClient().getConnection().getInetAddress().getHostAddress() + " try to use bug with macros");
			LOGGER.warning("could not store macro: " + e);
		}
	}
	
	/**
	 * @param macro
	 */
	private void deleteMacroFromDb(Macro macro)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not delete macro: " + e);
		}
	}
	
	public void restore()
	{
		_macroses.clear();
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, _owner.getObjectId());
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int id = rset.getInt("id");
				final int icon = rset.getInt("icon");
				final String name = rset.getString("name");
				final String descr = rset.getString("descr");
				final String acronym = rset.getString("acronym");
				final List<MacroCmd> commands = new ArrayList<>();
				final StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
				
				while (st1.hasMoreTokens())
				{
					final StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
					if (st.countTokens() < 3)
					{
						continue;
					}
					
					final int type = Integer.parseInt(st.nextToken());
					final int d1 = Integer.parseInt(st.nextToken());
					final int d2 = Integer.parseInt(st.nextToken());
					String cmd = "";
					if (st.hasMoreTokens())
					{
						cmd = st.nextToken();
					}
					
					final MacroCmd mcmd = new MacroCmd(commands.size(), type, d1, d2, cmd);
					commands.add(mcmd);
				}
				
				final Macro m = new Macro(id, icon, name, descr, acronym, commands.toArray(new MacroCmd[commands.size()]));
				_macroses.put(m.id, m);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not store shortcuts: " + e);
		}
	}
}
