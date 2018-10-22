package com.trackit.presentation.view.map.provider;

import com.trackit.business.common.Location;
import com.trackit.presentation.view.map.MapType;

public class OpenMapSurferMapProvider extends SphericalMercatorMapProvider implements MapProvider {

	public OpenMapSurferMapProvider(MapType mapType, Location centerLocation ) {
		super(MapProviderType.OPEN_MAP_SURFER_MAPS, mapType, centerLocation);
		tileFileExtension = "png";
	}	
	
//	protected String composeURL( String template, SphericalMercatorMapTile mapTile) {
//		if ( !mapTile.getMapType().equals( mapType.SATELLITE))
//			return String.format( template, mapTile.getX(), mapTile.getY(), mapTile.getZoom());
//		else {
//			long n = (1 << mapTile.getZoom()) - 1 - mapTile.getY();
//			System.out.println( mapTile.getZoom() + "  " + mapTile.getY() + "  " + n);
//			return String.format( template, mapTile.getZoom(), mapTile.getX(), n);
//		}
//	}	

}
