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

package com.trackit.presentation.view.map.provider;

import com.trackit.presentation.view.map.MapType;

public class MapRequestTemplate {
	
	public static final int MAXIMUM_ZOOM_LEVEL =  19;
	public static final int MINIMUM_ZOOM_LEVEL =   1;
	public static final int DEFAULT_ZOOM_LEVEL =   4;
	public static final int TILE_WIDTH         = 256;
	public static final int TILE_HEIGHT        = 256;

	private String          url;
	private String          server = "";
	private MapType			mapType;
	private MapProviderType parentType;
	private int             defaultZoom;
	private int             minimumZoom;
	private int             maximumZoom;
	private int				tileWidth;
	private int				tileHeight;
	private int             minServerNumber;
	private int             maxServerNumber;
	private boolean			hasAlternateServers = false;
	
	public MapRequestTemplate( String[] fields, MapProviderType defaultProviderType) {
		// MapType: mandatory
		mapType = MapType.lookup( fields[0].trim());
		// Parent map provider: optional
		String field = fields[1].trim();
		parentType = field.isEmpty() ? defaultProviderType: MapProviderType.lookupByName( field);
		// Tile width
		field = fields[2].trim();
		tileWidth = field.isEmpty() ? TILE_WIDTH: Integer.parseInt( field);
		// Tile width
		field = fields[3].trim();
		tileHeight = field.isEmpty() ? TILE_HEIGHT: Integer.parseInt( field);
		// Minimum zoom
		field = fields[4].trim();
		minimumZoom = field.isEmpty() ? MINIMUM_ZOOM_LEVEL: Integer.parseInt( field);
		// Maximum zoom
		field = fields[5].trim();
		maximumZoom = field.isEmpty() ? MAXIMUM_ZOOM_LEVEL: Integer.parseInt( field);
		// Default zoom
		field = fields[6].trim();
		defaultZoom = field.isEmpty() ? DEFAULT_ZOOM_LEVEL: Integer.parseInt( field);
		// URL
		url = fields[7].trim();
		if ( fields.length > 8 ) {
			// Server name
			server = fields[8].trim();
			// Alternate servers
			if ( fields.length > 9 && !(field = fields[9].trim()).isEmpty() ) {
				String[] split = field.split("-");
				if ( split.length == 2 ) {
					hasAlternateServers = true;
					minServerNumber     = (int) ( split[0].trim().charAt( 0));
					maxServerNumber     = (int) ( split[1].trim().charAt( 0));
				}
			}
		}
	}
	
	public MapProviderType getProviderType() {
		return parentType;
	}
	
	public MapType getMapType() {
		return mapType;
	}
	
	public int getTileWidth() {
		return tileWidth;
	}
	
	public int getTileHeight() {
		return tileHeight;
	}
	
	public int getDefaultZoomLevel() {
		return defaultZoom;
	}
	
	public int getMinimumZoomLevel() {
		return minimumZoom;
	}
	
	public int getMaximumZoomLevel() {
		return maximumZoom;
	}
	
	public String getUrl() {
		if ( ! server.isEmpty() ) {
			String serverTemplate = server;
			if ( hasAlternateServers ) {
				int serverNum = minServerNumber + (int)(Math.random() * ((maxServerNumber - minServerNumber) + 1));
				serverTemplate = String.format( serverTemplate, (char) serverNum);
			}
			return url.replaceFirst( "%s", serverTemplate);
		} else
			return url;
	}

}
