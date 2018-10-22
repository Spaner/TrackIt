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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.trackit.business.domain.DifficultyLevelType;;

public class DifficultyLevelComboBox extends EnumComboBox {

	public DifficultyLevelComboBox() {
		super( true);
		reloadItems();
	}
	
	public DifficultyLevelComboBox( boolean addAll) {
		super( addAll);
		reloadItems();
	}
	
	public DifficultyLevelType getSelectedDifficultyLevel() {
		return DifficultyLevelType.lookup( (short) getSelectedItemIndex());
	}
	
	public void setSelectedItem( DifficultyLevelType difficultyLevel) {
		setSelectedItem( difficultyLevel.toString());
	}
	
	public void setLocale( Locale locale) {
		DifficultyLevelType selected = DifficultyLevelType.lookup( (short) getSelectedItemIndex());
		super.setLocale( locale);
		setSelectedItem( selected.toString());
	}
	
	protected void reloadItems() {
		List<DifficultyLevelType> list = DifficultyLevelType.getList( addAll);
		HashMap<String, Short> map = new HashMap<>();
		DifficultyLevelType type;
		String[] labels = new String[ list.size()];
		for( int i=0; i<list.size(); i++) {
			type = list.get( i);
			labels[i] = type.toString();
			map.put( type.toString(), type.getValue());
		}
		setItems( map, labels);
	}
}
