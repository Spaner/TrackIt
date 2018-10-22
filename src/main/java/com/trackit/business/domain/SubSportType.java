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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import com.trackit.business.common.Messages;
import com.trackit.business.database.Database;
import com.trackit.business.utilities.DiacriticalStringComparator;


public enum SubSportType {
	GENERIC_SUB,
	CYCLING_ROAD,
	CYCLING_MOUNTAIN,
	CYCLING_TRACK,
	RUNNING_ROAD,
	RUNNING_TRAIL,
	RUNNING_TRACK,
	SWIMMING_LAP_SWIMMING,				//12335: 2017-06-10
	SWIMMING_OPEN_WATER,				//12335: 2017-06-10
	ALPINE_SKIING_TRACK,
	ALPINE_SKIING_SLALOM,
	ALPINE_SKIING_GIANT_SLALOM,
	ALPINE_SKIING_SUPER_G,
	ALPINE_SKIING_DOWNHILL,
	ALPINE_SKIING_LEISURE,
	ALPINE_SKIING_INDOOR_SKIING,		//2017-06-08: 12335
	CROSS_COUNTRY_SKIING_TRACK,
	CROSS_COUNTRY_SKIING_BIATHLON,
	SNOWBOARDING_DOWNHILL,
	SNOWBOARDING_MOUNTAIN,
	WALKING_ROAD,
	WALKING_TRAIL,
	WALKING_CASUAL,						//2017-06-10: 12335
	WALKING_SPEED,						//2017-06-10: 12335
	HIKING_TRAIL,
	HIKING_MOUNTAIN,
	HIKING_BACKCOUNTRY,					//2017-06-09: 12335
	MOTORCYCLING_TRACK,					//2017-06-09: 12335
	MOTORCYCLING_ATV,					//2017-06-09: 12335
	MOTORCYCLING_MOTOCROSS,				//2017-06-09: 12335
	BOATING_CHALLENGE,					//2017-06-09: 12335
	BOATING_LEISURE,					//2017-06-09: 12335
	DRIVING_TRACK,					 	//2017-06-09: 12335
	DRIVING_ATV,						//2017-06-09: 12335
	DRIVING_LEISURE,					//2017-06-09: 12335
	GOLF_CHALLENGE,						//2017-06-09: 12335
	GOLF_LEISURE,						//2017-06-09: 12335
	SAILING_CHALLENGE,					//2017-06-09: 12335
	SAILING_LEISURE,					//2017-06-09: 12335
	SNOWSHOEING_TRAIL,					//2017-06-09: 12335
	SNOWSHOEING_BACKCOUNTRY,			//2017-06-09: 12335
	KAYAKING_OPEN_WATER,				//2017-06-09: 12335
	KAYAKING_WHITEWATER,				//2017-06-09: 12335
	ALL_SUB,
	INVALID_SUB;


	private short uniqueTableID;
	private short subSportID;
	private short sportID;
	private String name;
	private double defaultAverageSpeed;
	private double maximumAverageSpeed;
	private double pauseThresholdSpeed;
	private long defaultPauseDuration;
	private boolean followRoads;
	private double joinMaximumWarningDistance;
	private double joinMergeDistanceTolerance;
	private long joinMergeTimeTolerance;	
	private double gradeLimit;
	private short  ascentDescentClass;				//12335: 2017-06-07
	
	private SubSportType() {
		this.sportID = Short.parseShort(this.getPropertySportID());
		this.name = this.getPropertyName();
		this.subSportID = Short.parseShort(this.getPropertyID());
		this.defaultAverageSpeed = Double.parseDouble(this.getPropertyDefaultAverageSpeed());
		this.maximumAverageSpeed = Double.parseDouble(this.getPropertyMaximumAllowedSpeed());
		this.pauseThresholdSpeed = Double.parseDouble(this.getPropertyPauseThresholdSpeed());
		this.defaultPauseDuration = Long.parseLong(this.getPropertyDefaultPauseDuration());
		this.followRoads = Boolean.parseBoolean(this.getPropertyFollowRoads());
		this.joinMaximumWarningDistance = Double.parseDouble(this.getPropertyJoinMaximumWarningDistance());
		this.joinMergeDistanceTolerance = Double.parseDouble(this.getPropertyJoinMergeDistanceTolerance());
		this.joinMergeTimeTolerance = Long.parseLong(this.getPropertyJoinMergeTimeTolerance());
		this.gradeLimit = Double.parseDouble(this.getPropertyGradeLimit());
		this.ascentDescentClass = Short.parseShort( this.getPropertyAscentDescentClass()); //12335: 2017-06-07
	}
	
	
	public String getProperty(String keyOrCode) {
        Properties prop = new Properties();
        try {
            prop.load(ClassLoader.getSystemResourceAsStream("sport.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return prop.getProperty(this.name() + "." + keyOrCode);	//2017-06-09: 12335
        // A SubSport may be defined by this enum but sport.properties may hold no properties for the SubSport
        String temp = prop.getProperty(this.name() + "." + keyOrCode);
        if ( temp==null || temp.isEmpty() )
        	// if so, assign it INVALID_SUB properties
        	temp = prop.getProperty( "INVALID_SUB" + "." + keyOrCode);
        return temp;
    }
	
	private String getPropertyAscentDescentClass() {		//12335: 2017-06-07
		String tmp = getProperty( "ASCENT_DESCENT_CLASS");
		if ( tmp == null ) {
			System.out.println( "NO AscentDescent: " + this.name());
			tmp = "0";
		}
		return tmp;
//		return getProperty( "ASCENT_DESCENT_CLASS");
	}
	
	private String getPropertyID() {
        return getProperty("ID");
    }

	private String getPropertyName() {
        return getProperty("NAME");
    }
    
    private String getPropertyDefaultAverageSpeed() {
        return getProperty("DEFAULT_AVERAGE_SPEED");
    }
    
    private String getPropertyMaximumAllowedSpeed() {
        return getProperty("MAXIMUM_ALLOWED_SPEED");
    }
    
    private String getPropertyPauseThresholdSpeed() {
        return getProperty("PAUSE_THRESHOLD_SPEED");
    }
    
    private String getPropertyDefaultPauseDuration() {
        return getProperty("DEFAULT_PAUSE_DURATION");
    }
    
    private String getPropertyFollowRoads() {
        return getProperty("FOLLOW_ROADS");
    }
    
    private String getPropertyJoinMaximumWarningDistance() {
        return getProperty("JOIN_MAXIMUM_WARNING_DISTANCE");
    }
    
    private String getPropertyJoinMergeDistanceTolerance() {
        return getProperty("JOIN_MERGE_DISTANCE_TOLERANCE");
    }
    
    private String getPropertyJoinMergeTimeTolerance() {
        return getProperty("JOIN_MERGE_TIME_TOLERANCE");
    }
    
    private String getPropertyGradeLimit() {
        return getProperty("GRADE_LIMIT");
    }
    
    private String getPropertySportID() {
        return getProperty("SPORT");
    }
	
	
	public short getSubSportID() {
		return subSportID;
	}
	
	public short getSportID(){
		return sportID;
	}
	
	public String getName(){
		return name;
	}
	
	public double getDefaultAverageSpeed(){
		return defaultAverageSpeed;
	}
	
	public double getMaximumAllowedSpeed(){
		return maximumAverageSpeed;
	}
	
	public double getPauseThresholdSpeed(){
		return pauseThresholdSpeed;
	}
	
	public long getDefaultPauseDuration(){
		return defaultPauseDuration;
	}
	
	public boolean getFollowRoads(){
		return followRoads;
	}
	
	public double getJoinMaximumWarningDistance(){
		return joinMaximumWarningDistance;
	}
	
	public double getJoinMergeDistanceTolerance(){
		return joinMergeDistanceTolerance;
	}
	
	public long getJoinMergeTimeTolerance(){
		return joinMergeTimeTolerance;
	}
	
	public double getGradeLimit(){
		return gradeLimit;
	}
	
	public void setFollowRoads(boolean followRoads){
		this.followRoads = followRoads;
	}
	
	public short getAscentDescentClass() {				//12335: 2017-06-07
		return this.ascentDescentClass;
	}
	
	public void setValues( double defaultAverageSpeed, 		  double maximumAverageSpeed, 
			               double pauseThresholdSpeed, 		  long defaultPauseDuration,
			               boolean followRoads, 		  	  double joinMaximumWarningDistance,
			               double joinMergeDistanceTolerance, long joinMergeTimeTolerance, 
			               double gradeLimit,  	 			  short ascentDescentClass){
		this.defaultAverageSpeed = defaultAverageSpeed;
		this.maximumAverageSpeed = maximumAverageSpeed;
		this.pauseThresholdSpeed = pauseThresholdSpeed;
		this.defaultPauseDuration = defaultPauseDuration;
		this.followRoads = followRoads;
		this.joinMaximumWarningDistance = joinMaximumWarningDistance;
		this.joinMergeDistanceTolerance = joinMergeDistanceTolerance;
		this.joinMergeTimeTolerance = joinMergeTimeTolerance;
		this.gradeLimit = gradeLimit;
		this.ascentDescentClass = ascentDescentClass;
	}
	

	
	private final String messageCodes(){
//		return name;							//12335: 2017-04-20
		return "subSportType." + name.substring(0,1).toLowerCase() + name.substring(1).replace( " ", "");
	}

	
	public static SubSportType lookup(short subSportID) {
		for (SubSportType subSport : values()) {
			if (subSport.subSportID == subSportID) {
				return subSport;
			}
		}
		return null;
	}
	
	//12335: 2017-07-15
	public static SubSportType lookup( short sportID, short subSportID) {
		for( SubSportType subSport: values()) {
			if( subSport.getSubSportID() == subSportID && subSport.getSportID() == sportID )
				return subSport;
		}
		return null;
	}
	
//  12335: 2016-06-13: renamed to lookupByName to make the function's purpose clear
//	public static SubSportType lookupName(String name, short sportID) {
	public static SubSportType lookupByName(String name, short sportID) {
		for (SubSportType subSport : values()) {
			String subSportName = subSport.getName();
			if (subSportName.equals(name) && subSport.getSportID() == sportID) {
				return subSport;
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
	public static TreeMap<String, Short> toString( List<Short> subSportIDs) {
		TreeMap<String, Short> sortedLabels = new TreeMap<>( new DiacriticalStringComparator());
		for( short id: subSportIDs)
			sortedLabels.put( lookup( id).toString(), id);
		return sortedLabels;
	}
	
	//12335: 2017-04-22
	public static TreeMap<String, Short> getLabelsAndIds( SportType sport) {
		TreeMap<String, Short> sortedLabelsAndIds = new TreeMap<>( new DiacriticalStringComparator());
		for( short id: Database.getInstance().getSubSportsIds( sport))
			sortedLabelsAndIds.put( lookup(id).toString(), id);
		return sortedLabelsAndIds;		
	}
}