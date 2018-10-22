package com.trackit.presentation.view.map.provider;

import com.trackit.business.common.Location;
import com.trackit.presentation.view.map.MapTile;
import com.trackit.presentation.view.map.MapType;

public class ExtendedSphericalMercatorMapProvider extends SphericalMercatorMapProvider implements MapProvider{
	
	private int minimumZoomLevel;
	private int maximumZoomLevel;
	private int defaultZoomLevel;
	private int tileWidth;
	private int tileHeight;
	
//	protected static MapProviderData mapProviderData = MapProviderData.getInstance();
	
	public ExtendedSphericalMercatorMapProvider( MapType mapType, Location centerLocation) {
		super( mapType, centerLocation);
	}

	@Override
	public byte getMinZoom() {
		return (byte) minimumZoomLevel;
	}

	@Override
	public byte getMaxZoom() {
		return (byte) maximumZoomLevel;
	}

	@Override
	public byte getDefaultZoom() {
		return (byte) defaultZoomLevel;
	}
	
	public boolean isValidZoomLevel( int zoomLevel) {
		return zoomLevel >= minimumZoomLevel && zoomLevel <= maximumZoomLevel;
	}

	@Override
	public int getTileWidth() {
		return tileWidth;
	}

	@Override
	public int getTileHeight() {
		return tileHeight;
	}

	@Override
	public void fetchTileImage(MapTile tile) {
		// TODO Auto-generated method stub

	}

}
