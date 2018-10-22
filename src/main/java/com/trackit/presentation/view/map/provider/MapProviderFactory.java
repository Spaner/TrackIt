/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * Copyright (C) 2018 J M Brisson Lopes
 * 						- uses new (simplified and configurable) map providers
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

import com.trackit.business.common.Location;
import com.trackit.presentation.view.map.MapType;
import com.trackit.presentation.view.map.provider.armysurvey.MilitaryMapsProvider;
import com.trackit.presentation.view.map.provider.bing.BingMapsProvider;
import com.trackit.presentation.view.map.provider.google.GoogleMapsProvider;
import com.trackit.presentation.view.map.provider.here.HereMapsProvider;

public class MapProviderFactory {
	private static MapProviderFactory factory;

	private MapProviderFactory() {
	}

	public static MapProviderFactory getInstance() {
		if (factory == null) {
			factory = new MapProviderFactory();
		}

		return factory;
	}

	public MapProvider getMapProvider(MapProviderType mapProviderType, MapType mapType, Location centerLocation) {
		switch (mapProviderType) {
		case GOOGLE_MAPS:
			return new GoogleMapsProvider(mapType, centerLocation);
		case BING_MAPS:
			return new BingMapsProvider(mapType, centerLocation);
		case HERE_MAPS:
			return new HereMapsProvider(mapType, centerLocation);
		case OPEN_STREET_MAPS:
			return new OpenStreetMapsProvider(mapType, centerLocation);
		case MAP_BOX_MAPS:
			return new MapBoxMapsProvider( mapType, centerLocation);
		case MAP_QUEST_MAPS:
			return new MapQuestMapsProvider(mapType, centerLocation);
		case ARC_GIS_MAPS:
			return new ArcGISMapsProvider(mapType, centerLocation);
		case OPEN_MAP_SURFER_MAPS:		//12335: 2018-05-26
			return new OpenMapSurferMapProvider(mapType, centerLocation);
		case TRANSAS_MAPS:				//12335: 2018-05-29
			return new TransasMapsProvider(mapType, centerLocation);
		case MILITARY_MAPS:
			return new MilitaryMapsProvider(mapType, centerLocation);
		default:
			return new GoogleMapsProvider(mapType, centerLocation);
		}
	}
}
