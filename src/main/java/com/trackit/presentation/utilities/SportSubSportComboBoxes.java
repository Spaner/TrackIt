/*
 * This file is part of Track It!.
 * Copyright (C) 2017, 2018 João Brisson Lopes
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import com.trackit.business.common.Messages;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;

public class SportSubSportComboBoxes {
	
	private Synchronizer<SportSubSportComboBoxes> peers = null;
	
	private TreeMap<String, Short> 		 	 sportsLabelsAndIds;
	private TreeMap<String, Short> 		 	 subSportsLabelsAndIds;
	private SportType    				 	 currentSport    		= null;
	private SubSportType 				 	 currentSubSport 		= null;
	boolean 							 	 toSelect	 	 		= false;	// 2018-04-02: 12335
	boolean 								 showAllSubSport		= true;		// 2018-04-02: 12335
	private DefaultComboBoxModel<String> 	 sportsModel     		= null;
	private DefaultComboBoxModel<String> 	 subSportsModel 		= null;
	private JComboBox<String> 			 	 sportsComboBox  		= null;
	private JComboBox<String> 			 	 subSportsComboBox 		= null;
	
	private Locale 							 locale                 = Messages.getLocale(); //2018-03-12: 12335			
	
	public SportSubSportComboBoxes( SportType sport, SubSportType subSport) {
		init( sport, subSport, false);
	}
	
	public SportSubSportComboBoxes( SportType sport, SubSportType subSport, boolean toSelect) {
		init( sport, subSport, toSelect);
	}
	
	// 2018-04-02: 12335
	public void showAllSubSport( boolean showAllSubSport) {
		this.showAllSubSport = showAllSubSport;
	}
	
	private void init( SportType sport, SubSportType subSport, boolean toSelect) {
		this.toSelect = toSelect;
		SportType initialSport = sport;
		if ( initialSport == null )
			initialSport = this.toSelect ? SportType.ALL : SportType.GENERIC;
		SubSportType initialSubSport = subSport;
		if ( initialSubSport == null )
			initialSubSport = this.toSelect ? SubSportType.ALL_SUB : SubSportType.GENERIC_SUB;
		setSelectedSport(    initialSport.toString());
		setSelectedSubSport( initialSubSport.toString());
	}
	
	public SportType getSport() {
		return currentSport;
	}
	
	public SubSportType getSubSport() {
		return currentSubSport;
	}
	
	//12335: 2018-03-14
	public SportType translateSportLabel( String label) {
		return SportType.lookup( sportsLabelsAndIds.get( label));
	}
	
	//12335: 2018-03-14
	public SubSportType translateSubSportLabel( String label) {
		return SubSportType.lookup( subSportsLabelsAndIds.get( label));
	}
	
	public JComboBox<String> getSportsComboBox() {
		return sportsComboBox;
	}
	
	public JComboBox<String> getSubSportsComboBox() {
		return subSportsComboBox;
	}
	
	//12335: 2018-03-13
	public void setLocale( Locale locale) {
		if ( this.locale != locale ) {
			this.locale = locale;
			swapLanguage( locale);
			if ( peers != null ) {
				SportSubSportComboBoxes peer;
				while( (peer = peers.next( this)) != null)
					peer.swapLanguage( locale);
			}
			sportsComboBox.setSelectedItem( currentSport.toString());
			subSportsComboBox.setSelectedItem( currentSubSport.toString());
			if ( peers != null ) {
				SportSubSportComboBoxes peer;
				while( (peer = peers.next( this)) != null) {
					peer.sportsComboBox.setSelectedItem( currentSport.toString());
					peer.subSportsComboBox.setSelectedItem( currentSubSport.toString());
				}
			}
		}
	}
	
	//12335: 2017-06-28
	public void startSynchronizing( SportSubSportComboBoxes peer) {
		if ( peers == null )
			peers = new Synchronizer<>();
		peer.peers = peers.add( this, peer);
	}
	
	//12335: 2017-06-28
	public void stopSynchronizing( SportSubSportComboBoxes peer) {
		peers.remove( peer);
	}
		
	public void setSelectedSport() {
		setSelectedSport( sportsComboBox.getSelectedItem().toString());
	}
	
	public void SetSport( SportType sport) {
		if ( sport != null )
			setSelectedSport( sport.toString());
	}
	
	private void setSelectedSport( String label) {
		if ( sportsLabelsAndIds == null ) {
			loadSportsLabelsAndIds();
		}
		SportType newSport = SportType.lookup( sportsLabelsAndIds.get( label));
		if ( newSport != currentSport ) {
			currentSport = newSport;
			sportsComboBox.setSelectedItem( currentSport.toString());
			loadSubSportsLabelsAndIds();
			currentSubSport = SubSportType.lookup( subSportsLabelsAndIds.get( subSportsComboBox.getSelectedItem().toString()));
			subSportsComboBox.setEnabled( subSportsComboBox.getItemCount() > 1 );
		}
	}
	
	//12335: 2018-03-13
	private void swapLanguage( Locale locale) {
		this.locale = locale;
		loadSportsLabelsAndIds();
		sportsComboBox.setModel( sportsModel);
		loadSubSportsLabelsAndIds();
		subSportsComboBox.setModel( subSportsModel);
	}
	
	//12335: 2018-03-13
	private void loadSportsLabelsAndIds() {
		sportsLabelsAndIds = SportType.getLabelsAndIds();
		List<String> sportsLabels = new ArrayList<>(
				Arrays.asList( SportType.getSportsLabels(sportsLabelsAndIds, toSelect, false)));
		sportsModel        = new DefaultComboBoxModel<>( sportsLabels.toArray( new String[0]));	
		if ( sportsComboBox == null ) {
			sportsComboBox     = new JComboBox<>( sportsModel);
			sportsComboBox.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelectedSport();
					propagateSport();
				}
			});
		} else {
			sportsComboBox.setModel( sportsModel);
		}
	}
	
	//12335: 2018-03-13
	private void loadSubSportsLabelsAndIds() {
		subSportsLabelsAndIds = SubSportType.getLabelsAndIds( currentSport);
		List<String> subSportLabels = new ArrayList<String>( subSportsLabelsAndIds.keySet());
		if ( !currentSport.equals( SportType.GENERIC) && !currentSport.equals( SportType.ALL)  &&
																				showAllSubSport ) {
			SubSportType subSportToAdd = toSelect ? SubSportType.ALL_SUB : SubSportType.GENERIC_SUB; 
			subSportsLabelsAndIds.put( subSportToAdd.toString(), subSportToAdd.getSubSportID());
			subSportLabels.add( 0, subSportToAdd.toString());
		}
		subSportsModel = new DefaultComboBoxModel<>( subSportLabels.toArray( new String[0]));
		if ( subSportsComboBox == null ) {
			subSportsComboBox = new JComboBox<>( subSportsModel);
			subSportsComboBox.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setSelectedSubSport();
					propagateSubSport();
				}
			});
		}
		else
			subSportsComboBox.setModel( subSportsModel);
	}
	
	public void setSelectedSubSport() {
		setSelectedSubSport( subSportsComboBox.getSelectedItem().toString());
	}
	
	public void setSubSport( SubSportType subSport) {
		if ( subSport != null )
			setSelectedSport( subSport.toString());
	}
	
	private void setSelectedSubSport( String label) {
		Short target = subSportsLabelsAndIds.get( label);
		if ( target == null )
			target = SubSportType.GENERIC_SUB.getSubSportID();
		SubSportType newSubSport = SubSportType.lookup( target);
//		SubSportType newSubSport = SubSportType.lookup( subSportsLabelsAndIds.get( label));
		if ( newSubSport != null &&  newSubSport != currentSubSport ){
			currentSubSport = newSubSport;
			subSportsComboBox.setSelectedItem( currentSubSport.toString());
		}
	}
	
	//12335: 2017-06-28
	private void propagateSport() {
		if ( peers != null ) {
			SportSubSportComboBoxes comboBoxes;
			String sportName = currentSport.toString();
			while( (comboBoxes = peers.next( this)) != null )
				comboBoxes.setSelectedSport( sportName);
//			String sportName = currentSport.toString();			//12335: 2017-08-10
//			for( SportSubSportComboBoxes comboBoxes: peers)
//				comboBoxes.setSelectedSport( sportName);
			propagateSubSport();
		}
	}
	
	//12335: 2017-06-28
	private void propagateSubSport() {
		if ( peers != null ) {
			String subSportName = currentSubSport.toString();	//12335: 2017-08-10
			SportSubSportComboBoxes comboBoxes;
			while( (comboBoxes = peers.next( this)) != null )
				comboBoxes.setSelectedSubSport( subSportName);
//			for( SportSubSportComboBoxes comboBoxes: peers)
//				comboBoxes.setSelectedSubSport( subSportName);
		}
		
	}
}
