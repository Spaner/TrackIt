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


public enum SubSportType {
	GENERIC_SUB,
	CYCLING_ROAD,
	CYCLING_MOUNTAIN,
	//CYCLING_TRACK,
	RUNNING_ROAD,
	RUNNING_TRAIL,
	RUNNING_TRACK,
	ALPINE_SKIING_TRACK,
	ALPINE_SKIING_SLALOM,
	ALPINE_SKIING_GIANT_SLALOM,
	ALPINE_SKIING_SUPER_G,
	ALPINE_SKIING_DOWNHILL,
	ALPINE_SKIING_LEISURE,
	CROSS_COUNTRY_SKIING_TRACK,
	CROSS_COUNTRY_SKIING_BIATHLON,
	SNOWBOARDING_DOWNHILL,
	SNOWBOARDING_MOUNTAIN,
	WALKING_ROAD,
	WALKING_TRAIL,
	HIKING_TRAIL,
	//HIKING_MOUNTAIN,
	SAILING_OPEN_WATER,
	//BOATING_OPEN_WATER,
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
	}
	
	
	public String getProperty(String keyOrCode) {
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
	
	public void setValues(double defaultAverageSpeed, double maximumAverageSpeed, double pauseThresholdSpeed, long defaultPauseDuration,
			boolean followRoads, double joinMaximumWarningDistance, double joinMergeDistanceTolerance, long joinMergeTimeTolerance, double gradeLimit){
		this.defaultAverageSpeed = defaultAverageSpeed;
		this.maximumAverageSpeed = maximumAverageSpeed;
		this.pauseThresholdSpeed = pauseThresholdSpeed;
		this.defaultPauseDuration = defaultPauseDuration;
		this.followRoads = followRoads;
		this.joinMaximumWarningDistance = joinMaximumWarningDistance;
		this.joinMergeDistanceTolerance = joinMergeDistanceTolerance;
		this.joinMergeTimeTolerance = joinMergeTimeTolerance;
		this.gradeLimit = gradeLimit;
	}
	

	
	private final String messageCodes(){
		return name;
	}

	
	public static SubSportType lookup(short subSportID) {
		for (SubSportType subSport : values()) {
			if (subSport.subSportID == subSportID) {
				return subSport;
			}
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
		return messageCodes();
	}
}