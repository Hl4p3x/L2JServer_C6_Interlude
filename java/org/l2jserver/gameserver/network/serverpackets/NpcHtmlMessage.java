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

import java.util.logging.Logger;

import org.l2jserver.Config;
import org.l2jserver.gameserver.cache.HtmCache;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.util.BuilderUtil;

/**
 * The HTML parser in the client knowns these standard and non-standard tags and attributes<br>
 * <li>VOLUMN<br>
 * <li>UNKNOWN<br>
 * <li>UL<br>
 * <li>U<br>
 * <li>TT<br>
 * <li>TR<br>
 * <li>TITLE<br>
 * <li>TEXTCODE<br>
 * <li>TEXTAREA<br>
 * <li>TD<br>
 * <li>TABLE<br>
 * <li>SUP<br>
 * <li>SUB<br>
 * <li>STRIKE<br>
 * <li>SPIN<br>
 * <li>SELECT<br>
 * <li>RIGHT<br>
 * <li>PRE<br>
 * <li>P<br>
 * <li>OPTION<br>
 * <li>OL<br>
 * <li>MULTIEDIT<br>
 * <li>LI<br>
 * <li>LEFT<br>
 * <li>INPUT<br>
 * <li>IMG<br>
 * <li>I<br>
 * <li>HTML<br>
 * <li>H7<br>
 * <li>H6<br>
 * <li>H5<br>
 * <li>H4<br>
 * <li>H3<br>
 * <li>H2<br>
 * <li>H1<br>
 * <li>FONT<br>
 * <li>EXTEND<br>
 * <li>EDIT<br>
 * <li>COMMENT<br>
 * <li>COMBOBOX<br>
 * <li>CENTER<br>
 * <li>BUTTON<br>
 * <li>BR<br>
 * <li>BODY<br>
 * <li>BAR<br>
 * <li>ADDRESS<br>
 * <li>A<br>
 * <li>SEL<br>
 * <li>LIST<br>
 * <li>VAR<br>
 * <li>FORE<br>
 * <li>READONL<br>
 * <li>ROWS<br>
 * <li>VALIGN<br>
 * <li>FIXWIDTH<br>
 * <li>BORDERCOLORLI<br>
 * <li>BORDERCOLORDA<br>
 * <li>BORDERCOLOR<br>
 * <li>BORDER<br>
 * <li>BGCOLOR<br>
 * <li>BACKGROUND<br>
 * <li>ALIGN<br>
 * <li>VALU<br>
 * <li>READONLY<br>
 * <li>MULTIPLE<br>
 * <li>SELECTED<br>
 * <li>TYP<br>
 * <li>TYPE<br>
 * <li>MAXLENGTH<br>
 * <li>CHECKED<br>
 * <li>SRC<br>
 * <li>Y<br>
 * <li>X<br>
 * <li>QUERYDELAY<br>
 * <li>NOSCROLLBAR<br>
 * <li>IMGSRC<br>
 * <li>B<br>
 * <li>FG<br>
 * <li>SIZE<br>
 * <li>FACE<br>
 * <li>COLOR<br>
 * <li>DEFFON<br>
 * <li>DEFFIXEDFONT<br>
 * <li>WIDTH<br>
 * <li>VALUE<br>
 * <li>TOOLTIP<br>
 * <li>NAME<br>
 * <li>MIN<br>
 * <li>MAX<br>
 * <li>HEIGHT<br>
 * <li>DISABLED<br>
 * <li>ALIGN<br>
 * <li>MSG<br>
 * <li>LINK<br>
 * <li>HREF<br>
 * <li>ACTION<br>
 * .
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class NpcHtmlMessage extends GameServerPacket
{
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(NpcHtmlMessage.class.getName());
	
	/** The _npc obj id. */
	private final int _npcObjId;
	
	/** The _html. */
	private String _html;
	
	/** The _html file. */
	private String _file = null;
	
	/** The _validate. */
	private final boolean _validate = true;
	
	/**
	 * Instantiates a new npc html message.
	 * @param npcObjId the npc obj id
	 * @param text the text
	 */
	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		setHtml(text);
	}
	
	/**
	 * Instantiates a new npc html message.
	 * @param npcObjId the npc obj id
	 */
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#runImpl()
	 */
	@Override
	public void runImpl()
	{
		if (Config.BYPASS_VALIDATION && _validate)
		{
			buildBypassCache(getClient().getPlayer());
			buildLinksCache(getClient().getPlayer());
		}
	}
	
	/**
	 * Sets the html.
	 * @param text the new html
	 */
	public void setHtml(String text)
	{
		if (text == null)
		{
			LOGGER.warning("Html is null! this will crash the client!");
			_html = "<html><body></body></html>";
			return;
		}
		
		if (text.length() > 8192)
		{
			LOGGER.warning("Html is too long! this will crash the client!");
			_html = "<html><body>Html was too long,<br>Try to use DB for this action</body></html>";
			return;
		}
		
		_html = text; // html code must not exceed 8192 bytes
	}
	
	/**
	 * Sets the file.
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean setFile(String path)
	{
		final String content = HtmCache.getInstance().getHtm(path);
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			LOGGER.warning("missing html page " + path);
			return false;
		}
		
		_file = path;
		setHtml(content);
		return true;
	}
	
	/**
	 * Replace.
	 * @param pattern the pattern
	 * @param value the value
	 */
	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}
	
	public void replace(String pattern, boolean value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public void replace(String pattern, int value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public void replace(String pattern, long value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public void replace(String pattern, double value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	/**
	 * Builds the bypass cache.
	 * @param player the player
	 */
	private final void buildBypassCache(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		player.clearBypass();
		final int len = _html.length();
		for (int i = 0; i < len; i++)
		{
			int start = _html.indexOf("bypass -h", i);
			final int finish = _html.indexOf("\"", start);
			if ((start < 0) || (finish < 0))
			{
				break;
			}
			
			start += 10;
			i = start;
			final int finish2 = _html.indexOf('$', start);
			if ((finish2 < finish) && (finish2 > 0))
			{
				player.addBypass2(_html.substring(start, finish2));
			}
			else
			{
				player.addBypass(_html.substring(start, finish));
				// LOGGER.warning("["+_html.substring(start, finish)+"]");
			}
		}
	}
	
	/**
	 * Builds the links cache.
	 * @param player the player
	 */
	private final void buildLinksCache(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		player.clearLinks();
		final int len = _html.length();
		for (int i = 0; i < len; i++)
		{
			final int start = _html.indexOf("link", i);
			final int finish = _html.indexOf("\"", start);
			if ((start < 0) || (finish < 0))
			{
				break;
			}
			
			i = start;
			player.addLink(_html.substring(start + 5, finish).trim());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2jserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if ((_file != null) && player.isGM() && Config.GM_DEBUG_HTML_PATHS)
		{
			BuilderUtil.sendHtmlMessage(player, _file.substring(10));
		}
		
		writeC(0x0f);
		writeD(_npcObjId);
		writeS(_html);
		writeD(0x00);
	}
	
	/**
	 * Gets the content.
	 * @return the content
	 */
	public String getContent()
	{
		return _html;
	}
}
