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
package org.l2jserver.commons.util;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

/*
 *  A class to control the maximum number of lines to be stored in a Document
 *
 *  Excess lines can be removed from the start or end of the Document
 *  depending on your requirement.
 *
 *  a) if you append text to the Document, then you would want to remove lines
 *     from the start.
 *  b) if you insert text at the beginning of the Document, then you would
 *     want to remove lines from the end.
 */
public class LimitLinesDocumentListener implements DocumentListener
{
	private int _maximumLines;
	private final boolean _isRemoveFromStart;
	
	/*
	 * Specify the number of lines to be stored in the Document. Extra lines will be removed from the start of the Document.
	 */
	public LimitLinesDocumentListener(int maximumLines)
	{
		this(maximumLines, true);
	}
	
	/*
	 * Specify the number of lines to be stored in the Document. Extra lines will be removed from the start or end of the Document, depending on the boolean value specified.
	 */
	public LimitLinesDocumentListener(int maximumLines, boolean isRemoveFromStart)
	{
		setLimitLines(maximumLines);
		_isRemoveFromStart = isRemoveFromStart;
	}
	
	/*
	 * Return the maximum number of lines to be stored in the Document.
	 */
	public int getLimitLines()
	{
		return _maximumLines;
	}
	
	/*
	 * Set the maximum number of lines to be stored in the Document.
	 */
	public void setLimitLines(int maximumLines)
	{
		if (maximumLines < 1)
		{
			final String message = "Maximum lines must be greater than 0";
			throw new IllegalArgumentException(message);
		}
		
		_maximumLines = maximumLines;
	}
	
	/*
	 * Handle insertion of new text into the Document.
	 */
	@Override
	public void insertUpdate(DocumentEvent e)
	{
		// Changes to the Document can not be done within the listener so we need to add the processing to the end of the EDT.
		SwingUtilities.invokeLater(() -> removeLines(e));
	}
	
	@Override
	public void removeUpdate(DocumentEvent e)
	{
		// Ignore.
	}
	
	@Override
	public void changedUpdate(DocumentEvent e)
	{
		// Ignore.
	}
	
	/*
	 * Remove lines from the Document when necessary.
	 */
	private void removeLines(DocumentEvent e)
	{
		// The root Element of the Document will tell us the total number of line in the Document.
		final Document document = e.getDocument();
		final Element root = document.getDefaultRootElement();
		
		while (root.getElementCount() > _maximumLines)
		{
			if (_isRemoveFromStart)
			{
				removeFromStart(document, root);
			}
			else
			{
				removeFromEnd(document, root);
			}
		}
	}
	
	/*
	 * Remove lines from the start of the Document
	 */
	private void removeFromStart(Document document, Element root)
	{
		final Element line = root.getElement(0);
		final int end = line.getEndOffset();
		
		try
		{
			document.remove(0, end);
		}
		catch (BadLocationException ble)
		{
			System.out.println(ble);
		}
	}
	
	/*
	 * Remove lines from the end of the Document
	 */
	private void removeFromEnd(Document document, Element root)
	{
		// We use start minus 1 to make sure we remove the newline character of the previous line.
		
		final Element line = root.getElement(root.getElementCount() - 1);
		final int start = line.getStartOffset();
		final int end = line.getEndOffset();
		
		try
		{
			document.remove(start - 1, end - start);
		}
		catch (BadLocationException ble)
		{
			System.out.println(ble);
		}
	}
}