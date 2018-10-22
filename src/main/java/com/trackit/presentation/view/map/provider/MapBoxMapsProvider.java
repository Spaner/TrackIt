package com.trackit.presentation.view.map.provider;

import com.trackit.business.common.Location;
import com.trackit.presentation.view.map.MapType;

public class MapBoxMapsProvider extends SphericalMercatorMapProvider implements MapProvider {

	public MapBoxMapsProvider(MapType mapType, Location centerLocation ) {
		super(MapProviderType.MAP_BOX_MAPS, mapType, centerLocation);
		tileFileExtension = "png";
	}	
	
}
