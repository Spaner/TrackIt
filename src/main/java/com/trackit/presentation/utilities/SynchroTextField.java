/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class SynchroTextField extends JTextField {
	
	private Synchronizer<SynchroTextField> peers = null;
	
	public SynchroTextField() {
		super();
		init();
	}

	public SynchroTextField( String text) {
		super( text);
		init();
	}
	
	public void setText( String text) {
		super.setText( text);
		if ( text == null || text.isEmpty() )
			propagate( null, this);
	}
	
	public void startSynchronizing( SynchroTextField peer) {
		if ( peers == null )
			peers = new Synchronizer<>();		
		peer.peers = peers.add( this, peer);
		peer.setText( getText());
	}
	
	public void stopSynchronizing( SynchroTextField peer) {
		if ( peer != null && peers != null ) {
			peers.remove( peer);
		}
	}
	
	private void init() {
		final SynchroTextField originator = this; 
		super.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setText( getText());
				propagate( getText(), originator);
			}
		});	
	}
	
	private void propagate( String text, SynchroTextField originator) {
		if ( peers != null ) {
			SynchroTextField peer;
			while( (peer = peers.next( this)) != null ) {
				peer.setText( text);
			}
		}
	}
}
