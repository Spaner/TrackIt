/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson Lopes
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
import java.beans.PropertyChangeListener;
import java.util.EventListener;
//import java.util.HashMap;
import java.util.HashSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;

public class SynchroComboBox extends JComboBox<String>{

	private String[]                 items    = null;
	private Synchronizer<SynchroComboBox> peers = null;
	
	public SynchroComboBox() {
		super();
		init( null);
	}
	
	public SynchroComboBox( String[] items) {
		super( items);
		init( items);
	}
	
	public void setItems( String[] items) {
		if ( items != null )
			super.setModel( new DefaultComboBoxModel<>( items));
		propagate( items);
	}
	
	public void startSynchronizing( SynchroComboBox peer) {
		if ( peer != null ) {
			if ( peers == null )
				peers = new Synchronizer<>();
			peer.peers = peers.add( this, peer);
		}
	}
	
	public void stopSynchronizing( SynchroComboBox peer) {
		if ( peer != null)
			peers.remove( peer);
	}
	
	private void init( String[] items) {
		this.items = items;
		super.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				propagate();
			}
		});
	}
	
	private void propagate() {
		if ( peers != null ) {
			String selectedItem = (String) getSelectedItem();
			SynchroComboBox combo;
			while( (combo = peers.next( this)) != null )
				combo.setSelectedItem( selectedItem);
		}
	}
	
	private void propagate( String[] items) {
		if ( items != null  && peers != null ) {
			SynchroComboBox peer;
			while( (peer = peers.next( this)) != null )
				peer.setModel( new DefaultComboBoxModel<>( items));
		}
	}
}
