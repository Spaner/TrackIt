/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * Copyright (C) 2018 João Brisson Lopes
 * 					  2018-06-19: Added Hike&Bike, Hiking, Biking, Topographic and Seamap map types
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

import com.trackit.business.common.Messages;

public enum MapType {
	MAP("MAP"),       SATELLITE("SATELLITE"), HYBRID("HYBRID"), TERRAIN("TERRAIN"), TOPOGRAPHIC("TOPOGRAPHIC"),
	HIKING("HIKING"), CYCLING("CYCLING"),       HIKEBIKE("HIKEBIKE"),
	SEAMAP("SEAMAP"), 
	PTARMYSURVEY( "PTARMYSURVEY");
	
	private String mapType;
	
	private static int count = MapType.values().length;
	
	private static final String[] descriptions = new String[] { "Map", "Satellite", "Hybrid", "Terrain",
																"Topographic",
																"Hiking", "Cycling",	"HikeBike",
																"Seamap", "PTArmySurvey" };
	private static final String[] normalizedNames = new String[] { "map", "satellite", "hybrid", "terrain", 
																   "topographic",
																   "hiking", "cycling", "hikeBike",
																   "seamap", "ptArmySurvey" };

	private MapType(String mapType) {
		this.mapType = mapType;
	}

	public String getMapType() {
		return mapType;
	}
	
	public String getDescription() {
		return descriptions[this.ordinal()];
//		return Messages.getMessage( "mapType." + mapType.toLowerCase());
	}

	public static String[] getDescriptions() {
		for( int i=0; i<count; i++)
			descriptions[i] = MapType.values()[i].getDescription();
		return descriptions;
	}
	
	// 2018-05-14
	public String getMapTypeLabel() {
		return Messages.getMessage( "mapType." + getNormalizedName());
	}
	
	public String getNormalizedName() {
// 		2018-05-14: 12335
//		return normalizedNames[this.ordinal()];
		String str = getDescription().replaceAll( "\\s", "");
		char c[] = str.toCharArray();
		c[0] = Character.toLowerCase( c[0]);
		return new String( c);
	}
	
	public static MapType lookup(String mapType) {
		for (int i = 0; i < descriptions.length; i++) {
			if (MapType.values()[i].getMapType().equalsIgnoreCase(mapType)) {
				return MapType.values()[i];
			}
		}
		
		return null;
	}
	
	public static MapType lookupByDescription( String mapTypeDescription) {
		System.out.println( descriptions.length + "  " + mapTypeDescription);
		for( int i=0; i<descriptions.length; i++) {
			System.out.println( descriptions[i]);
			if ( descriptions[i].equalsIgnoreCase( mapTypeDescription) )
				return MapType.values()[i];
		}
		return MapType.MAP;
//		return null;
	}
}
