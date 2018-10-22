package com.trackit.presentation.view.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.trackit.presentation.view.map.provider.MapProviderType;
import com.trackit.presentation.view.map.provider.MapRequestTemplate;

public class MapProviderCatalog {
	
	private MapProviderType mapProviderType;
	private String          firstKey  = "";
	private String			secondKey = "";
	
	private HashMap< MapType, List<MapRequestTemplate>> templatesCatalog;

	public MapProviderCatalog( MapProviderType mapProviderType) {
		this.mapProviderType = mapProviderType;
		templatesCatalog     = new HashMap<>();
	}
	
	public MapProviderType getMapProviderType() {
		return mapProviderType;
	}
	
	public void setMainKey( String key) {
		firstKey = key;
	}
	
	public void setSecondaryKey( String key) {
		secondKey = key;
	}
	
	public String getMainKey() {
		return firstKey;
	}
	
	public String getSecondaryKey() {
		return secondKey;
	}
	
	public boolean isEmpty() {
		return templatesCatalog.isEmpty();
	}
	
	public List<MapType> getMapTypesList() {
		return new ArrayList<>( templatesCatalog.keySet());
	}
	
	public void addTemplate( MapRequestTemplate template) {
		List< MapRequestTemplate> list = templatesCatalog.get( template.getMapType());
		if ( list == null ) {
			list = new ArrayList<>();
			templatesCatalog.put( template.getMapType(), list);
		}
		list.add( template);
	}
	
	public List<MapRequestTemplate> getTemplates( MapType mapType) {
		return templatesCatalog.get( mapType);
	}
	
	public int getDefaultZoomLevel( MapType mapType) {
		List<MapRequestTemplate> templates = getTemplates( mapType);
		if ( templates != null ) {
			int zoomLevel = MapRequestTemplate.MAXIMUM_ZOOM_LEVEL;
			for( MapRequestTemplate template: templates)
				zoomLevel = Math.min( template.getDefaultZoomLevel(), zoomLevel);
			return zoomLevel;
		}
		return MapRequestTemplate.DEFAULT_ZOOM_LEVEL;
	}
	
	public int getMaximumZoomLevel( MapType mapType) {
		List<MapRequestTemplate> templates = getTemplates( mapType);
		if ( templates != null ) {
			int zoomLevel = MapRequestTemplate.MINIMUM_ZOOM_LEVEL;
			for( MapRequestTemplate template: templates)
				zoomLevel = Math.max( template.getMaximumZoomLevel(), zoomLevel);
			return zoomLevel;
		}
		return MapRequestTemplate.MAXIMUM_ZOOM_LEVEL;
	}
	
	public int getMinimumZoomLevel( MapType mapType) {
		List<MapRequestTemplate> templates = getTemplates( mapType);
		if ( templates != null ) {
			int zoomLevel = MapRequestTemplate.MAXIMUM_ZOOM_LEVEL;
			for( MapRequestTemplate template: templates)
				zoomLevel = Math.min( template.getMinimumZoomLevel(), zoomLevel);
			return zoomLevel;
		}
		return MapRequestTemplate.MINIMUM_ZOOM_LEVEL;
	}
	
	public int getTileWidth( MapType mapType) {
		List<MapRequestTemplate> templates = getTemplates( mapType);
		if ( templates != null ) {
			return templates.get( 0).getTileWidth();
		}
		return MapRequestTemplate.TILE_WIDTH;
	}

	public int getTileHeight( MapType mapType) {
		List<MapRequestTemplate> templates = getTemplates( mapType);
		if ( templates != null ) {
			return templates.get( 0).getTileHeight();
		}
		return MapRequestTemplate.TILE_HEIGHT;
	}
}
