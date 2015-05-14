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
package com.henriquemalheiro.trackit.presentation.view.map;

public enum MapType {
	MAP("MAP"), SATELLITE("SATELLITE"), HYBRID("HYBRID"), TERRAIN("TERRAIN");
	
	private String mapType;
	
	private static final String[] descriptions = new String[] { "Map", "Satellite", "Hybrid", "Terrain" };
	private static final String[] normalizedNames = new String[] { "map", "satellite", "hybrid", "terrain" };

	private MapType(String mapType) {
		this.mapType = mapType;
	}

	public String getMapType() {
		return mapType;
	}
	
	public String getDescription() {
		return descriptions[this.ordinal()];
	}

	public static String[] getDescriptions() {
		return descriptions;
	}
	
	public String getNormalizedName() {
		return normalizedNames[this.ordinal()];
	}
	
	public static MapType lookup(String mapType) {
		for (int i = 0; i < descriptions.length; i++) {
			if (MapType.values()[i].getMapType().equalsIgnoreCase(mapType)) {
				return MapType.values()[i];
			}
		}
		
		return null;
	}
}
