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
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import com.trackit.business.common.Messages;
import com.trackit.business.domain.CircuitType;
import com.trackit.business.domain.DifficultyLevelType;
import com.trackit.business.domain.TrackConditionType;

public class LocalizableComboBox {
	
	public static LocalizableComboBox DifficultyLevelComboBox() {
		return new LocalizableComboBox( ComboBoxType.DIFFICULTY);
	}

	public static LocalizableComboBox TrackConditionComboBox() {
		return new LocalizableComboBox( ComboBoxType.CONDITION);
	}

	public static LocalizableComboBox CircuitTypeComboBox() {
		return new LocalizableComboBox( ComboBoxType.CIRCUIT);
	}

	private JComboBox<String>            comboBox;
	private DefaultComboBoxModel<String> model;
	private ComboBoxType				 dataType;
	HashSet<Object>						 toRemove = new HashSet<>();
	private DifficultyLevelType selectedDifficulty;
	private TrackConditionType  trackCondition;
	private CircuitType			circuitType;
	
	private static enum ComboBoxType {
		DIFFICULTY,
		CONDITION,
		CIRCUIT,
		SPORT,
		SUBSPORT;
	};
	
	public Object getSelectedItem() {
		return retrieveSelected();
	}
	
	public String getSelectedItemString() {
		return translate( getSelectedItem());
	}
	
	public String getSelectedItemStringValue() {
		return String.valueOf( valuOf( getSelectedItem()));
	}
	
	public void removeItem( Object itemToRemove) {
		toRemove.add( itemToRemove);
		comboBox.removeItem( translate( itemToRemove));
	}
	
	public void setSelectedItem( Object itemToSelect) {
		comboBox.setSelectedItem( translate( itemToSelect));
		storeSelected( translate( itemToSelect));
	}
	
	public void setLocale() {
		comboBox.setModel( new DefaultComboBoxModel<>( getLabels()));
		comboBox.setSelectedItem( translate( retrieveSelected()));
	}
	
	protected LocalizableComboBox( ComboBoxType type) {
		dataType = type;
		model    = new DefaultComboBoxModel<>( getLabels());
		comboBox = new JComboBox<>( model);
		comboBox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				storeSelected( comboBox.getSelectedItem().toString());
			}
		});
		storeSelected( comboBox.getSelectedItem().toString());
	}
	
	public JComboBox<String> getComboBox() {
		return comboBox;
	}
	
	private String[] getLabels() {
		String[] labels = null;
		switch ( dataType ) {
		case DIFFICULTY: {
			List<DifficultyLevelType> options = DifficultyLevelType.getList();
			options.removeAll( toRemove);
			labels = new String[ options.size()];
			for( int i=0; i<options.size(); i++)
				labels[i] = options.get( i).toString();
			}
			break;
		case CONDITION: {
			List<TrackConditionType> options = TrackConditionType.getList();
			options.removeAll( toRemove);
			labels = new String[ options.size()];
			for( int i=0; i<options.size(); i++)
				labels[i] = options.get( i).toString();
			}
			break;
		case CIRCUIT: {
			List<CircuitType> options = CircuitType.getList( true);
			options.removeAll( toRemove);
			labels = new String[ options.size()];
			for( int i=0; i<options.size(); i++)
				labels[i] = options.get( i).toString();
			}
			break;
		default:
			break;
		}
		return labels;
	}
	
	private void storeSelected( String selection) {
		switch ( dataType ) {
		case DIFFICULTY:
			selectedDifficulty = DifficultyLevelType.lookup( selection);
			break;
		case CONDITION:
			trackCondition = TrackConditionType.lookup( selection);
			break;
		case CIRCUIT:
			circuitType = CircuitType.lookup( selection);
			break;
//		case SPORT:
//			sport = SportType.lookupByName( selection);
//			break;
//		case SUBSPORT:
//			subSport = SubSportType.lookup( selection);
//			break;
		default:
			break;
		}
	}
	
	private Object retrieveSelected() {
		Object object = null;
		switch ( dataType ) {
		case DIFFICULTY:
			object = selectedDifficulty;
			break;
		case CONDITION:
			object = trackCondition;
			break;
		case CIRCUIT:
			object = circuitType;
			break;
		default:
			break;
		}
		return object;
	}
	
	private String translate( Object object) {
		String answer = "";
		switch ( dataType ) {
		case DIFFICULTY:
			answer = ((DifficultyLevelType) object).toString();
			break;
		case CONDITION:
			answer = ((TrackConditionType) object).toString();
			break;
		case CIRCUIT:
			answer = ((CircuitType) object).toString();
			break;
		default:
			break;
		}
		return answer;
	}
	
	private int valuOf( Object object) {
		int value = 0;
		switch ( dataType ) {
		case DIFFICULTY:
			value = selectedDifficulty.getValue();
			break;
		case CONDITION:
			value = trackCondition.getValue();
			break;
		case CIRCUIT:
			value = circuitType.getValue();
			break;
		default:
			break;
		}
		return value;
	}
}
