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
package com.henriquemalheiro.trackit.presentation.view.map.provider;

public enum MapProviderType {
	GOOGLE_MAPS("GOOGLE_MAPS"),
	BING_MAPS("BING_MAPS"),
	HERE_MAPS("HERE_MAPS"),
	OPEN_STREET_MAPS("OPEN_STREET_MAPS"),
	MAP_QUEST_MAPS("MAP_QUEST_MAPS"),
	ARC_GIS_MAPS("ARC_GIS_MAPS"),
	MILITARY_MAPS("MILITARY_MAPS");

	private String mapProviderType;
	
	private static final String[] descriptions = new String[] { "Google Maps", "Bing Maps",
		"Here Maps", "Openstreet Maps", "MapQuest Maps", "ArcGIS Maps", "Military Maps" };

	private MapProviderType(String mapProviderType) {
		this.mapProviderType = mapProviderType;
	}

	public String getMapProviderType() {
		return mapProviderType;
	}
	
	public String getDescription() {
		return descriptions[this.ordinal()];
	}

	public static String[] getDescriptions() {
		return descriptions;
	}
	
	public static MapProviderType lookup(String mapProviderType) {
		for (int i = 0; i < descriptions.length; i++) {
			if (descriptions[i].equalsIgnoreCase(mapProviderType)) {
				return MapProviderType.values()[i];
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
}
