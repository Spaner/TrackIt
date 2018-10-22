package com.trackit.presentation.view.map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.trackit.business.domain.TrackItBaseType;
import com.trackit.presentation.view.map.provider.MapProviderType;
import com.trackit.presentation.view.map.provider.MapRequestTemplate;

public class MapsCatalog {
	
	private static MapsCatalog mapsCatalog;
	
	private static HashMap< MapProviderType, MapProviderCatalog> catalog;
	
	private static final String providersDataFile = "MapProvidersCatalog.def";
	private static final String EMPTY_LINE        = "^\\s*$";
	
	private static Logger logger = Logger.getLogger( TrackItBaseType.class.getName());
	
	static { mapsCatalog = new MapsCatalog(); }
	
	public synchronized static MapsCatalog getInstance() {
		return mapsCatalog;
	}
	
	private MapsCatalog() {
		try {
	        BufferedReader reader = new BufferedReader( new InputStreamReader(
	        							MapsCatalog.class.getResourceAsStream( providersDataFile)));
	        catalog = new HashMap<>();
	        String line;
	        MapProviderType mapProviderType    = null;
	        MapProviderCatalog providerCatalog = null;
	        while( (line = reader.readLine()) != null ) {
	        	line = line.trim();
	        	if ( ! line.matches( EMPTY_LINE) && ! line.startsWith( "!")) {
	        		if ( line.startsWith( "[") ) {
	        			String mapProviderName = line.substring( 1, line.length()-1);
	        			mapProviderType = MapProviderType.lookupByName( mapProviderName);
	        			if ( mapProviderType != null ) {
	        				providerCatalog = catalog.get( mapProviderType);
	        				if ( providerCatalog == null ) {
	        					providerCatalog = new MapProviderCatalog( mapProviderType);
	        					catalog.put( mapProviderType, providerCatalog);
	        				}
	        			}
	        		} else
	        			if ( line.toLowerCase().startsWith( "key") ) {
	        				String key = line.substring( line.indexOf( '=') + 1).trim();
	        				if ( ! key.isEmpty() ) {
	        					if ( providerCatalog.getMainKey().isEmpty() )
	        						providerCatalog.setMainKey( key);
	        					else
	        						providerCatalog.setSecondaryKey( key);
	        				}
	        			} else {
	        				MapRequestTemplate template = new MapRequestTemplate(
	        															line.split( ","), mapProviderType);
	        				providerCatalog.addTemplate( template);
	        			}
	        	}
	        }
	        reader.close();
		} catch (FileNotFoundException e) {
	        logger.error("File not found: MapProvidersData.def.");
	        System.exit(-1);
	    } catch (Exception e) {
	    	logger.error("Error reading MapProvidersData.def file.");
	    	e.printStackTrace();
	    	System.exit(-1);
		}
//		for( MapProviderType mapProvider: catalog.keySet() ) {
//			System.out.println( "\n" + mapProvider.getDescription());
//			MapProviderCatalog providerCatalog = catalog.get( mapProvider);
//			String str = providerCatalog.getMainKey();
//			if ( !str.isEmpty() )
//				System.out.println( "Key = " + str);
//			str = providerCatalog.getSecondaryKey();
//			if ( !str.isEmpty() )
//				System.out.println( "Key = " + str);
//			if ( ! providerCatalog.isEmpty() ) {
//				for( MapType mapType: providerCatalog.getMapTypesList()) {
//					System.out.println( mapType.getDescription());
//					for( MapRequestTemplate template: providerCatalog.getTemplates( mapType)) {
//						System.out.println( template.getUrl());
//					}
//				}
//			}
//		}
//		System.out.println();
	}
	
	public MapProviderCatalog getMapProviderCatalog( MapProviderType mapProviderType) {
		return catalog.get( mapProviderType);
	}
	
	public List<MapRequestTemplate> getMapRequestTemplates( MapProviderType mapProviderType, MapType mapType) {
		MapProviderCatalog mapProviderCatalog = catalog.get( mapProviderType);
		if ( mapProviderCatalog != null )
			return mapProviderCatalog.getTemplates( mapType);
		return null;
	}
	
	public List<MapProviderType> getMapProvidersList() {
		return new ArrayList<MapProviderType>( catalog.keySet());
	}
	
	public List<MapProviderType> getActiveMapProvidersList() {
		List<MapProviderType> mapProviders = getMapProvidersList();
		Iterator<MapProviderType> map = mapProviders.iterator();
		while( map.hasNext() ) {
			MapProviderCatalog providerCatalog = catalog.get( map.next());
			if ( providerCatalog == null || providerCatalog.isEmpty() )
				map.remove();
		}
		return mapProviders;
	}
	
	public List<MapType> getMapProviderTypesList( MapProviderType mapProviderType) {
		MapProviderCatalog mapProviderCatalog = catalog.get( mapProviderType);
		if ( mapProviderCatalog != null )
			return mapProviderCatalog.getMapTypesList();
		return null;
	}
	
}
