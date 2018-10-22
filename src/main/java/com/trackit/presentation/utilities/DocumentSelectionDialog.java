/*
 * This file is part of Track It!.
 * Copyright (C) 2017 João Brisson Lopes
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
package com.trackit.presentation.utilities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import com.trackit.TrackIt;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.domain.GPSDocument;

public class DocumentSelectionDialog extends JDialog implements ActionListener {
	
	public static GPSDocument showCopyToDialog(List<GPSDocument> documents, boolean showNewDocumentOption ) {
		return showDialog( documents, showNewDocumentOption, 
				           Messages.getMessage( "dialog.documentSelection.copyTo") );
	}
	
	public static GPSDocument showMoveToDialog(List<GPSDocument> documents, boolean showNewDocumentOption ) {
		return showDialog( documents, showNewDocumentOption, 
				           Messages.getMessage( "dialog.documentSelection.moveTo") );
	}
	
	private static GPSDocument showDialog( List<GPSDocument> documents, boolean showNewDocumentOption,
										  String actionLabel) {
		List<String> documentNames = new ArrayList<>();
		if ( showNewDocumentOption )
			documentNames.add( Messages.getMessage( "dialog.documentSelection.newDocument"));
		for( GPSDocument document: documents)
			documentNames.add( document.getName());
		dialog = new DocumentSelectionDialog( documentNames, actionLabel);
		selection = dialog.result != -1;
		if ( dialog.result != -1 ) {
			if ( showNewDocumentOption )
				dialog.result--;
			if ( dialog.result >= 0)
				return documents.get( dialog.result);
		}
		return null;
	}
	
	public static boolean newDocumentSelected() {
		return selection;
	}
	
	private static DocumentSelectionDialog dialog;
	private static boolean selection;
	
	private char okChar;
	private char cancelChar;
	private String [] names;
	private String actionLabel;
	private int result;
	private JList list;
	private JRootPane rootPane;
	
	public DocumentSelectionDialog( List<String> names, String actionLabel) {
		this.names = names.toArray( new String[0]);
		this.actionLabel = actionLabel;
		initialise();
	}
	
	private void initialise() {
		
		JLabel headline = new JLabel( actionLabel);
		int maxListWidth = Math.max( 250, headline.getPreferredSize().width);
		
		String buttonText = Messages.getMessage( "trackIt.cmdCancel");
		JButton cancel    = new JButton( buttonText);
		cancelChar        = buttonText.toLowerCase().charAt( 0);
		cancel.setDisplayedMnemonicIndex( 0);
		cancel.addActionListener( this);
		
		buttonText      = Messages.getMessage( "trackIt.cmdOk");
		JButton select  = new JButton( buttonText);
		okChar          = buttonText.toLowerCase().charAt( 0);
		select.setDisplayedMnemonicIndex( 0);
		select.addActionListener( this);
		
		int maxWidth = Math.max( select.getPreferredSize().width, cancel.getPreferredSize().width);
		int maxHeight = Math.max( select.getPreferredSize().height, cancel.getPreferredSize().height);
		select.setPreferredSize( new Dimension( maxWidth, maxHeight));
		cancel.setPreferredSize( new Dimension( maxWidth, maxHeight));
		
		JButton left  = select;
		JButton right = cancel;
		if ( OperatingSystem.isMac() ) {
			left  = cancel;
			right = select;
		}
				
		list = new JList( names);
		list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroller = new JScrollPane( list);
		scroller.setPreferredSize( new Dimension( maxListWidth,  120));
		scroller.setAlignmentX( LEFT_ALIGNMENT);
		setTitle( Messages.getMessage( "dialog.documentSelection.title"));
		
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS));
		headline.setLabelFor( list);
		panel.add( headline);
		panel.add( scroller);
		panel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS));
		buttons.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttons.add( Box.createHorizontalGlue());
		buttons.add( left);
		buttons.add( Box.createRigidArea( new Dimension( 10, 0)));
		buttons.add( right);
		
		Container contentPane = getContentPane();
		contentPane.add( panel, BorderLayout.CENTER);
		contentPane.add( buttons, BorderLayout.PAGE_END);
		
		rootPane = getRootPane();
		rootPane.setDefaultButton( cancel);
		
		AbstractAction eventListener = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				char control = e.getActionCommand().toLowerCase().charAt( 0);
				if ( control == KeyEvent.VK_ESCAPE )
					control = cancelChar;
				else
					if ( control == KeyEvent.VK_ENTER )
						control = okChar;
				finalise( control);
			}
		};

		InputMap inputMap = rootPane.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = rootPane.getActionMap();
		inputMap.put( KeyStroke.getKeyStroke( Character.toString(okChar).toUpperCase().charAt(0), 
                	  InputEvent.SHIFT_DOWN_MASK), 
					  "uppercaseOK");
		actionMap.put( "uppercaseOK", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( okChar), 
					  "lowercaseOK");
		actionMap.put("lowercaseOK", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( Character.toString(cancelChar).toUpperCase().charAt(0),
                	  InputEvent.SHIFT_DOWN_MASK),
					  "uppercaseCancel");
		actionMap.put( "uppercaseCancel", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( cancelChar),
					  "lowercaseCancel");
		actionMap.put( "lowercaseCancel", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER,0), "enter");
		actionMap.put( "enter", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), "escape");
		actionMap.put( "escape", eventListener);

		list.setSelectedIndex( 0);
		
		pack();
		setModal( true);
		setLocationRelativeTo( TrackIt.getApplicationFrame());
		this.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setVisible( true);
	}
	
	public void actionPerformed( ActionEvent e) {
		finalise( e.getActionCommand().toLowerCase().charAt( 0));
	}
	
	private void finalise( char controllingChar) {
		if ( controllingChar == okChar )
			result = list.getSelectedIndex();
		else
			result = -1;
		this.dispose();
	}
	
}
