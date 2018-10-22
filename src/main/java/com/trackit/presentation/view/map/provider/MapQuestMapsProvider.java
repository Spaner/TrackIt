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

import java.util.Locale;

import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.presentation.view.map.MapType;

public class MapQuestMapsProvider extends SphericalMercatorMapProvider implements MapProvider {
	
	private static final String key = "FnlxtGvCcvqYeo3gkFtbItc51MleWzBR";

	public MapQuestMapsProvider(MapType mapType, Location centerLocation ) {
		super(MapProviderType.MAP_QUEST_MAPS, mapType, centerLocation);
		tileFileExtension = "jpg";
	}	
	
//	protected String composeURL( String template, SphericalMercatorMapTile mapTile) {
//		String url = String.format( template, key, 
//									getTileCentralLocationAsString( mapTile.getX(), mapTile.getY()),
//									mapTile.getZoom());
//		return url;
//	}	
}
