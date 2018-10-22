package com.trackit.presentation.view.map.provider;

import com.trackit.business.common.Location;
import com.trackit.presentation.view.map.MapType;

public class TransasMapsProvider extends SphericalMercatorMapProvider implements MapProvider {

	public TransasMapsProvider( MapType mapType, Location centerLocation ) {
		super(MapProviderType.TRANSAS_MAPS, mapType, centerLocation);
		tileFileExtension = "png";
	}	

}
