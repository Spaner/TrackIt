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
package com.henriquemalheiro.trackit.business.domain;

import java.io.FileInputStream;
import java.util.Properties;

public enum SportType {
	GENERIC,
	RUNNING,
	CYCLING,
	WALKING,
	CROSS_COUNTRY_SKIING,
	ALPINE_SKIING,
	SNOWBOARDING,
	HIKING,
	//BOATING,
	SAILING,
	ALL,
	INVALID;

	
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
        return prop.getProperty(this.name() + "." + keyOrCode);
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
		return name;
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
		return messageCodes();
	}
}