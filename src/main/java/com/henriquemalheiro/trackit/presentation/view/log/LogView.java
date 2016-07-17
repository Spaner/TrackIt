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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.business.common.Messages;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

public class LogView extends JPanel {
	private static final long serialVersionUID = 8243867174960605182L;
	private JTextPane logTextPane;
	private SwingAppender logAppender;
	
	public LogView() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		
		logTextPane = new JTextPane();
		DefaultCaret caret = (DefaultCaret) logTextPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		logTextPane.setEditable(false);
		
		logAppender = new SwingAppender();
		logAppender.setTextArea(logTextPane);
		
		JScrollPane logTextPaneScroll = new JScrollPane(logTextPane);
		logTextPaneScroll.setBorder(null);

		add(logTextPaneScroll, BorderLayout.CENTER);
		
		JPanel logOptionsPanel = new JPanel();
		logOptionsPanel.setPreferredSize(new Dimension(600, 32));
		logOptionsPanel.setBackground(Color.WHITE);
		logOptionsPanel.setOpaque(true);
		logOptionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		
		JComboBox<LogLevel> cbLogLevel = new JComboBox<LogLevel>(LogLevel.values());
		cbLogLevel.setSelectedIndex(LogLevel.DEBUG.ordinal());
		cbLogLevel.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent event) {
				JComboBox<LogLevel> cb = (JComboBox<LogLevel>) event.getSource();
		        LogLevel logLevel = (LogLevel) cb.getSelectedItem();
		        
		        ((SwingAppender) Logger.getRootLogger().getAppender("LogViewAppender"))
		        		.setThreshold(Level.toLevel(logLevel.name()));
			}
		});
		logOptionsPanel.add(cbLogLevel);
		
		JButton cmdClear = new JButton(getMessage("logView.cmdClear"));
		cmdClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logTextPane.setText("");
			}
		});
		logOptionsPanel.add(cmdClear);
		
		add(logOptionsPanel, BorderLayout.PAGE_START);
	}
	
	@Override
	public String toString() {
		return Messages.getMessage("view.log.name");
	}
}

enum LogLevel {
	ALL("All"), TRACE("Trace"), DEBUG("Debug"), INFO("Info"), WARN("Warning"), ERROR("Error"),
	FATAL("Fatal Error"), OFF("Off");

	private String name;
	
	private LogLevel(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
};
