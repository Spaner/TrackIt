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

public enum SportType2 {
	GENERIC((short)0,"Generic"),
	   RUNNING((short)1,"Running"),
	   CYCLING((short)2,"Cycling"),
//	   TRANSITION((short)3),
//	   FITNESS_EQUIPMENT((short)4),
//	   SWIMMING((short)5),
//	   BASKETBALL((short)6),
//	   SOCCER((short)7),
//	   TENNIS((short)8),
//	   AMERICAN_FOOTBALL((short)9),
//	   TRAINING((short)10),
	   WALKING((short)11,"Walking"),
	   CROSS_COUNTRY_SKIING((short)12,"Cross Country Skiing"),
	   ALPINE_SKIING((short)13,"Alpine Skiing"),
	   SNOWBOARDING((short)14,"Snowboarding"),
//	   ROWING((short)15),
//	   MOUNTAINEERING((short)16),
	   HIKING((short)17,"Hiking"),
//	   MULTISPORT((short)18),
//	   PADDLING((short)19),
//	   FLYING((short)20),
//	   E_BIKING((short)21),
//	   MOTORCYCLING((short)22),
	   BOATING((short)23,"Boating"),
//	   DRIVING((short)24),
//	   GOLF((short)25),
//	   HANG_GLIDING((short)26),
//	   HORSEBACK_RIDING((short)27),
//	   HUNTING((short)28),
//	   FISHING((short)29),
//	   INLINE_SKATING((short)30),
//	   ROCK_CLIMBING((short)31),
	   SAILING((short)32,"Sailing"),
//	   ICE_SKATING((short)33),
//	   SKY_DIVING((short)34),
//	   SNOWSHOEING((short)35),
//	   SNOWMOBILING((short)36),
//	   STAND_UP_PADDLEBOARDING((short)37),
//	   SURFING((short)38),
//	   WAKEBOARDING((short)39),
//	   WATER_SKIING((short)40),
//	   KAYAKING((short)41),
//	   RAFTING((short)42),
//	   WINDSURFING((short)43),
//	   KITESURFING((short)44),
	   ALL((short)254,"All"),
	   INVALID((short)255,"Invalid"),
	   ;

	public String stuff = "coiso";
	private short sportID;
	private String name;
	
	private SportType2(short sportID, String name) {
		this.sportID = sportID;
		this.name = name;
	}
	
	public short getSportID() {
		return sportID;
	}
	
	public String getName(){
		return name;
	}
	
	private static final String[] messageCodes = {"sportType.generic",
			"sportType.running", 
			"sportType.cycling", 
//			"sportType.transition",
//			"sportType.fitnessEquipment",
//			"sportType.swimming", 
//			"sportType.basketball", 
//			"sportType.soccer", 
//			"sportType.tennis",
//			"sportType.americanFootball", 
//			"sportType.training", 
			"sportType.walking",
			"sportType.crossCountrySkiing",
			"sportType.alpineSkiing",
			"sportType.snowboarding",
//			"sportType.rowing",
//			"sportType.mountaineering",
			"sportType.hiking",
//			"sportType.multisport",
//			"sportType.paddling",
//			"sportType.flying",
//			"sportType.eBiking",
//			"sportType.motorcycling",
			"sportType.boating",
//			"sportType.driving",
//			"sportType.golf",
//			"sportType.hangGliding",
//			"sportType.horsebackRiding",
//			"sportType.hunting",
//			"sportType.fishing",
//			"sportType.inlineSkating",
//			"sportType.rockClimbing",
			"sportType.sailing",
//			"sportType.iceSkating",
//			"sportType.skyDiving",
//			"sportType.snowshoeing",
//			"sportType.snowmibiling",
//			"sportType.standUpPaddleboarding",
//			"sportType.surfing",
//			"sportType.wakeboarding",
//			"sportType.waterSkiing",
//			"sportType.kayaking",
//			"sportType.rafting",
//			"sportType.windsurfing",
//			"sportType.kitesurfing",
			"sportType.all",
			"sportType.invalid"
			};
	private final String messageCodes(){
		return name;
	}
	
	
	public static SportType2 lookup(short sportID) {
		for (SportType2 sport : values()) {
			if (sport.sportID == sportID) {
				return sport;
			}
		}
		return null;
	}
	
	public static SportType2 lookupName(String name) {
		for (SportType2 sport : values()) {
			String sportName = sport.getName();
			if (sportName.equals(name)) {
				return sport;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
	public String toString() {
		return messageCodes();
	}
}*/