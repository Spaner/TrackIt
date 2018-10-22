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

package com.trackit.presentation.view.map;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.trackit.TrackIt;
import com.trackit.business.common.Constants;
import com.trackit.presentation.view.map.provider.MapProviderType;

public class MapSelector {
	
	private JComboBox<String> cbMapProvider;
	private JComboBox<String> cbMapType;
	
	private List<MapProviderType> mapProviders = new ArrayList<>();
	private List<MapType> 		  mapTypes     = new ArrayList<>();
	
	private static MapsCatalog mapsCatalog = MapsCatalog.getInstance();

	public MapSelector( String defaultProviderDescription, String defaultTypeDescription) {
		cbMapProvider = new JComboBox<String>();
		cbMapProvider.setMaximumRowCount( 9);
		loadMapProviders();

		cbMapType = new JComboBox<String>();
		cbMapType.putClientProperty("JComponent.sizeVariant", "small");
		cbMapType.setPreferredSize( new Dimension( 140, 22));

		MapProviderType mapProvider = MapProviderType.lookup( defaultProviderDescription);
		cbMapProvider.setSelectedIndex( mapProviders.indexOf( mapProvider));
		loadMapTypes( mapProvider);
		MapType type = MapType.lookupByDescription( defaultTypeDescription);
		int selectedTypeIndex = Math.max( mapTypes.indexOf( type), 0);
		cbMapType.setSelectedIndex( selectedTypeIndex);
		cbMapProvider.putClientProperty("JComponent.sizeVariant", "small");

		cbMapType.setPreferredSize( new Dimension( 140, 22));
	}
	
	public JComboBox<String> mapProviderComboBox() {
		return cbMapProvider;		
	}

	public JComboBox<String> mapTypeComboBox() {
		return cbMapType;		
	}
	
	public MapProviderType handleMapProviderChangeEvent() {
		if ( cbMapProvider.getSelectedIndex() >= 0 ) {
			System.out.println(  "\nMap Provider: " + cbMapProvider.getSelectedIndex());
			MapProviderType selectedMapProvider = mapProviders.get( cbMapProvider.getSelectedIndex());
			MapType currentType = mapTypes.get( cbMapType.getSelectedIndex());
			loadMapTypes( mapProviders.get( cbMapProvider.getSelectedIndex()));
			int currentIndex = mapTypes.indexOf( currentType);
			if ( currentIndex < 0 )
				currentIndex = 0;
			System.out.println( "Provider changed - " + currentType + " " + currentIndex);
			cbMapType.setSelectedIndex( currentIndex);
	        TrackIt.getPreferences().setPreference( Constants.PrefsCategories.MAPS, 
	        		 								null,
	        		 								Constants.MapPreferences.DEFAULT_PROVIDER,
	        		 								selectedMapProvider.getDescription());
			return selectedMapProvider;
		}
		return null;
	}
	
	public MapType handleMapTypeChangeEvent() {
		int selectedIndex = cbMapType.getSelectedIndex();
		if ( selectedIndex >= 0 && selectedIndex < mapTypes.size() ) {
			MapType mapType = mapTypes.get( cbMapType.getSelectedIndex());
	        TrackIt.getPreferences().setPreference(Constants.PrefsCategories.MAPS, null,
	        		Constants.MapPreferences.DEFAULT_MAP_TYPE, mapType.getDescription());
			return mapType;
		}
		return null;
	}
	
	public void setLocale( Locale locale) {
		int selectedProvider = cbMapProvider.getSelectedIndex();
		int selectedType     = cbMapType.getSelectedIndex();
		System.out.println( selectedProvider + "  " + selectedType);
		loadMapProviders();
		loadMapTypes( mapProviders.get( selectedProvider));
		cbMapProvider.setSelectedIndex( selectedProvider);
		cbMapType.setSelectedIndex( selectedType);
	}
	
	private void loadMapProviders() {
		mapProviders.clear();
		cbMapProvider.removeAllItems();
		List<MapProviderType> providersList = mapsCatalog.getActiveMapProvidersList();
		for( MapProviderType mapProvider: MapProviderType.values())
			if ( providersList.contains( mapProvider)) {
				mapProviders.add( mapProvider);
				cbMapProvider.addItem( mapProvider.getMapProviderLabel());
			}
	}
	
	private void loadMapTypes( MapProviderType mapProvider) {
		mapTypes.clear();
		cbMapType.removeAllItems();
		List<MapType> typesList = mapsCatalog.getMapProviderTypesList( mapProvider);
		for( MapType type: MapType.values() )
			if ( typesList.contains( type) ) {
				mapTypes.add( type);
				cbMapType.addItem( type.getMapTypeLabel());
			}
	}

}
