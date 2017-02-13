/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * 
 * TrackIt! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Track It! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Track It!. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.henriquemalheiro.trackit.presentation.view.log;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

public class SwingAppender extends WriterAppender {
	private static Logger logger = Logger.getLogger(SwingAppender.class);
	private static JTextPane logTextPane = null;

	/** Set the target JTextPane for the logging information to appear. */
	public void setTextArea(javax.swing.JTextPane logTextPane) {
		SwingAppender.logTextPane = logTextPane;
	}

	@Override
	public void append(LoggingEvent loggingEvent) {
		final String message = this.layout.format(loggingEvent);
		final LoggingEvent currentEvent = loggingEvent;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				StyledDocument styledDocMainLowerText = (StyledDocument) logTextPane.getDocument();
				Style style = styledDocMainLowerText.addStyle("StyledDocument", null);

				StyleConstants.setFontFamily(style, Font.MONOSPACED);
				StyleConstants.setFontSize(style, 11);

				if (currentEvent.getLevel().toString() == "FATAL") {
					StyleConstants.setForeground(style, Color.red);
				} else {
					StyleConstants.setForeground(style, Color.blue);
				}

				try {
					styledDocMainLowerText.insertString(styledDocMainLowerText.getLength(), message, style);
				} catch (BadLocationException e) {
					logger.fatal(e);
				}
			}
		});
	}
}