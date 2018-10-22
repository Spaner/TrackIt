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
import java.util.Locale;

public class EnumComboBox extends SynchroComboBox {
	
	private   HashMap<String, Short> map;
	private   Locale locale;
	protected boolean addAll;
	
	protected EnumComboBox() {
		super();
		this.addAll = true;
	}

	protected EnumComboBox( boolean addAll) {
		super();
		this.addAll = addAll;
	}
	
	public void setItems( HashMap<String, Short> map, String[] labels) {
		if ( map != null ) {
			this.map = map;
			super.setItems(  labels);
		}
	}
	
	public int getSelectedItemIndex() {
		return map.get( super.getSelectedItem());
	}
	
	public String getSelectedItemValueString() {
		if ( map != null )
			return Integer.toString( map.get( getSelectedItem()));
		return null;
	}
	
	public void setLocale( Locale locale) {
		if ( locale != null && ( this.locale == null || !this.locale.equals( locale) ) ) {
			this.locale = locale;
			reloadItems();
		}
	}
	
	protected void reloadItems() { }
	
}
