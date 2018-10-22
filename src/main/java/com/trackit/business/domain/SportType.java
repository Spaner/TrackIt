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
package com.trackit.business.domain;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.trackit.business.common.Messages;
import com.trackit.business.database.Database;

public enum SportType {
	GENERIC,
	INVALID,
	RUNNING,
	SWIMMING,				//12335: 2017-06-07
	CYCLING,
	WALKING,
	CROSS_COUNTRY_SKIING,
	ALPINE_SKIING,
	SNOWBOARDING,
	HIKING,
	MOTORCYCLING,			//12335: 2017-06-07
	BOATING,
	DRIVING,				//12335: 2017-06-07
	GOLF,					//12335: 2017-06-07
	SAILING,
	SNOWSHOEING,			//12335: 2017-06-07
	KAYAKING,				//12335: 2017-06-07
	ALL,
	XPTO;

	
	private short sportID;
	private String name;
	
	private SportType() {
		this.sportID = Short.parseShort(this.getPropertyID());
		this.name = this.getPropertyName();
	}
	
	private String getProperty(String keyOrCode) {
        Properties prop = new Properties();
        try {
        	prop.load(ClassLoader.getSystemResourceAsStream("sport.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //2017-06-09: 12335 - A Sport defined by this enum may not have its attributes defined in sport.properties
//        return prop.getProperty(this.name() + "." + keyOrCode);
        String temp = prop.getProperty(this.name() + "." + keyOrCode);
        if ( temp == null )
        	temp = prop.getProperty( "INVALID" + "." + keyOrCode);
        return temp;
   }
	
	private String getPropertyID() {
        return getProperty("ID");
    }

	private String getPropertyName() {
        return getProperty("NAME");
    }

	
	public short getSportID() {
		return sportID;
	}
	
	public String getName(){
		return name;
	}
	

	private final String messageCodes(){
//		return name;						//12335: 2017-04-20
		return "sportType." + name.substring( 0, 1).toLowerCase() + name.substring(1).replace( " ", "");
	}
	
	
	public static SportType lookup(short sportID) {
		for (SportType sport : values()) {
			if (sport.sportID == sportID) {
				return sport;
			}
		}
		return null;
	}
	
//  12335: 2016-06-13: renamed to lookupByName to make the function's purpose clear
//	public static SportType lookupName(String name) {
	public static SportType lookupByName(String name) {
		for (SportType sport : values()) {
			String sportName = sport.getName();
			if (sportName.equals(name)) {
				return sport;
			}
		}
		return null;
	}
	
	@Override

	public String toString() {
//		return messageCodes();				//12335: 2017-04-20
		return Messages.getMessage( messageCodes());
	}
	
	//12335: 2017-04-20
	public static TreeMap<String, Short> toString( List<Short> sportIDs) {
		TreeMap<String, Short> sortedLabels = new TreeMap<>();
		for( short id: sportIDs)
			sortedLabels.put( lookup( id).toString(), id);
		return sortedLabels;
	}

	//12335: 2017-04-22
	public static TreeMap<String, Short> getLabelsAndIds() {
		Database database = Database.getInstance();
		TreeMap<String, Short> sortedLabelsAndIds = new TreeMap<>();
		for( SportType sport: values()) {
			short id = sport.getSportID();
			if ( !database.getSubSportsIds( lookup(id)).isEmpty() )
				sortedLabelsAndIds.put( lookup( id).toString(), id);
		}
		return sortedLabelsAndIds;
	}
	
	//12335: 2018-04-02
	public static String[] getSportsLabels( TreeMap<String, Short> map, boolean isSelect, boolean includeInvalid) {
		List<String> labels = new ArrayList<>( Arrays.asList( getSportsLabels( map)));
		labels.add( 0, (isSelect ? ALL.toString(): GENERIC.toString()));
		if ( includeInvalid )
			labels.add( 0, INVALID.toString());
		return labels.toArray( new String[0]);
	}	
	
	//12335: 2017-04-22
	public static String[] getSportsLabels( TreeMap<String, Short> map) {
		List<String> labels = new ArrayList<>();
//		labels.add( SportType.GENERIC.toString());
		for( String key: map.keySet()) {
			short id = map.get( key);
			if ( id != GENERIC.getSportID() && id != INVALID.getSportID() && id != ALL.getSportID() )
				labels.add( key);
		}
		return labels.toArray( new String[0]);
	}
}