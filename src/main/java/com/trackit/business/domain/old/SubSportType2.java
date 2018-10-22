package com.trackit.business.domain.old;
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
 
package com.henriquemalheiro.trackit.business.domain.old;

import com.henriquemalheiro.trackit.business.common.Messages;

public enum SubSportType2 {
	GENERIC((short)0,(short)0,"Generic",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   //TREADMILL((short)1,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	  // STREET((short)2,(short)1,"STREET",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	  // TRAIL((short)3,(short)1,"TRAIL",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	  // TRACK((short)4,(short)1,"TRACK",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   //SPIN((short)5,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   //INDOOR_CYCLING((short)6,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	  // ROAD((short)7,(short)1,"ROAD",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	  // MOUNTAIN((short)8,(short)1,"MOUNTAIN",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   DOWNHILL((short)9,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   RECUMBENT((short)10,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   CYCLOCROSS((short)11,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   HAND_CYCLING((short)12,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   TRACK_CYCLING((short)13,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   INDOOR_ROWING((short)14,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   ELLIPTICAL((short)15,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   STAIR_CLIMBING((short)16,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   LAP_SWIMMING((short)17,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   //OPEN_WATER((short)18,(short)1,"OPEN_WATER",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   FLEXIBILITY_TRAINING((short)19,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   STRENGTH_TRAINING((short)20,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   WARM_UP((short)21,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   MATCH((short)22,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   EXERCISE((short)23,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   CHALLENGE((short)24,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   INDOOR_SKIING((short)25,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   CARDIO_TRAINING((short)26,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   INDOOR_WALKING((short)27,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   E_BIKE_FITNESS((short)28,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   BMX((short)29,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   CASUAL_WALKING((short)30,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   SPEED_WALKING((short)31,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   BIKE_TO_RUN_TRANSITION((short)32,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   RUN_TO_BIKE_TRANSITION((short)33,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   SWIM_TO_BIKE_TRANSITION((short)34,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   ATV((short)35,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   MOTOCROSS((short)36,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   BACKCOUNTRY((short)37,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   RESORT((short)38,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   RC_DRONE((short)39,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   WINGSUIT((short)40,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
//	   WHITEWATER((short)41,(short)1,10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	  

	   ROAD_CYCLING((short)7,(short)2,"Road",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   MOUNTAIN_CYCLING((short)8,(short)2,"Mountain",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   TRACK_CYCLING((short)4,(short)2,"Track",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   ROAD_RUNNING((short)7,(short)1,"Road",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   TRAIL_RUNNING((short)3,(short)1,"Trail",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   TRACK_RUNNING((short)4,(short)1,"Track",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   //ROAD_RUNNING((short)7,(short)1,"Road",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   TRACK_ALPINE_SKIING((short)4,(short)13,"Track",10.0,30.0,0.5,(long)5,true,100.0,10.0,(long)5),
	   SLALOM_ALPINE_SKIING((short)42,(short)13,"Slalom",50.0,60.0,2.0,(long)5,true,100.0,10.0,(long)5),
	   GIANT_SLALOM_ALPINE_SKIING((short)43,(short)13,"Giant Slalom",80.0,100.0,2.0,(long)5,true,100.0,10.0,(long)5),
	   SUPER_G_ALPINE_SKIING((short)44,(short)13,"Super-G",140.0,150.0,2.0,(long)5,true,100.0,10.0,(long)5),
	   DOWNHILL_ALPINE_SKIING((short)9,(short)13,"Downhill",150.0,200.0,2.0,(long)5,true,100.0,10.0,(long)5),
	   LEISURE_ALPINE_SKIING((short)45,(short)13,"Leisure  ",30.0,100.0,1.0,(long)5,true,100.0,10.0,(long)5),
	   TRACK_CROSS_COUNTRY_SKIING((short)4,(short)12,"Track",10.0,100.0,1.0,(long)5,true,100.0,10.0,(long)5),
	   BIATHLON_CROSS_COUNTRY_SKIING((short)46,(short)12,"Biathlon",10.0,100.0,1.0,(long)5,true,100.0,10.0,(long)5),
	   TRACK_SNOWBOARDING((short)4,(short)14,"Track",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   MOUNTAIN_SNOWBOARDING((short)8,(short)14,"Mountain",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   ROAD_WALKING((short)7,(short)11,"Road",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   TRAIL_WALKING((short)3,(short)11,"Trail",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   //ROAD_WALKING((short)7,(short)11,"Road",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   TRAIL_HIKING((short)3,(short)17,"Trail",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   //ROAD_HIKING((short)7,(short)17,"Road",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   MOUNTAIN_HIKING((short)8,(short)17,"Mountain",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   OPEN_WATER_SAILING((short)18,(short)23,"Open Water",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   OPEN_WATER_BOATING((short)18,(short)32,"Open Water",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   ALL((short)254,(short)254,"All",10.0,50.0,0.1,(long)5,true,100.0,10.0,(long)5),
	   INVALID((short)255,(short)255,"Invalid",0.0,0.0,0.0,(long)0,false,0.0,0.0,(long)0),
	   ;


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
	
	private SubSportType2(short subSportID){
		this.subSportID = subSportID;
	}
	
	private SubSportType2(short subSportID, short sportID, String name, double defaultAverageSpeed,
			double maximumAverageSpeed, double pauseThresholdSpeed, long defaultPauseDuration, boolean followRoads,
			double joinMaximumWarningDistance, double joinMergeDistanceTolerance, long joinMergeTimeTolerance) {
		this.subSportID = subSportID;
		this.sportID = sportID;
		this.name = name;
		this.defaultAverageSpeed = defaultAverageSpeed;
		this.maximumAverageSpeed = maximumAverageSpeed;
		this.pauseThresholdSpeed = pauseThresholdSpeed;
		this.defaultPauseDuration = defaultPauseDuration;
		this.followRoads = followRoads;
		this.joinMaximumWarningDistance = joinMaximumWarningDistance;
		this.joinMergeDistanceTolerance = joinMergeDistanceTolerance;
		this.joinMergeTimeTolerance = joinMergeTimeTolerance;
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
	
	private static final String[] messageCodes = {"subSportType.generic", "subSportType.treadmill", "subSportType.street",
		"subSportType.trail", "subSportType.track", "subSportType.spin", "subSportType.indoorCycling", "subSportType.road",
		"subSportType.mountain", "subSportType.downhill", "subSportType.recumbent", "subSportType.cyclocross",
		"subSportType.handCycling", "subSportType.trackCycling", "subSportType.indoorRowing", "subSportType.elliptical",
		"subSportType.stairClimbing", "subSportType.lapSwimming", "subSportType.openWater", "subSportType.flexibilityTraining",
		"subSportType.strenghtTraining",
		"subSportType.warmUp",
		"subSportType.match",
		"subSportType.exercise",
		"subSportType.challenge",
		"subSportType.indoorSkiing",
		"subSportType.cardioTraining",
		"subSportType.indoorWalking",
		"subSportType.eBikeFitness",
		"subSportType.bmx",
		"subSportType.casualWalking",
		"subSportType.speedWalking",
		"subSportType.bikeToRunTransition",
		"subSportType.runToBikeTransition",
		"subSportType.swimToBikeTransition",
		"subSportType.atv",
		"subSportType.motocross",
		"subSportType.backcountry",
		"subSportType.resort",
		"subSportType.rcDrone",
		"subSportType.wingsuit",
		"subSportType.whitewater",
		"subSportType.all",
		"subSportType.invalid"};
	
	private final String messageCodes(){
		return name;
	}

	
	public static SubSportType2 lookup(short subSportID) {
		for (SubSportType2 subSport : values()) {
			if (subSport.subSportID == subSportID) {
				return subSport;
			}
		}
		return null;
	}
	
	public static SubSportType2 lookupName(String name, short sportID) {
		for (SubSportType2 subSport : values()) {
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
}*/