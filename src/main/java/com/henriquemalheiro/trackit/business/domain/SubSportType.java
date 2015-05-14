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

import com.henriquemalheiro.trackit.business.common.Messages;

public enum SubSportType {
	GENERIC((short) 0),
	TREADMILL((short) 1),
	STREET((short) 2),
	TRAIL((short) 3),
	TRACK((short) 4),
	SPIN((short) 5),
	INDOOR_CYCLING((short) 6),
	ROAD((short) 7),
	MOUNTAIN((short) 8),
	DOWNHILL((short) 9),
	RECUMBENT((short) 10),
	CYCLOCROSS((short) 11),
	HAND_CYCLING((short) 12),
	TRACK_CYCLING((short) 13),
	INDOOR_ROWING((short) 14),
	ELLIPTICAL((short) 15),
	STAIR_CLIMBING((short) 16),
	LAP_SWIMMING((short) 17),
	OPEN_WATER((short) 18),
	FLEXIBILITY_TRAINING((short) 19),
	STRENGTH_TRAINING((short) 20),
	ALL((short) 254);
	
	private short value;
	
	private SubSportType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"subSportType.generic", "subSportType.treadmill", "subSportType.street",
		"subSportType.trail", "subSportType.track", "subSportType.spin", "subSportType.indoorCycling", "subSportType.road",
		"subSportType.mountain", "subSportType.downhill", "subSportType.recumbent", "subSportType.cyclocross",
		"subSportType.handCycling", "subSportType.trackCycling", "subSportType.indoorRowing", "subSportType.elliptical",
		"subSportType.stairClimbing", "subSportType.lapSwimming", "subSportType.openWater", "subSportType.flexibilityTraining",
		"subSportType.strenghtTraining", "subSportType.all"};
	
	public static SubSportType lookup(short value) {
		for (SubSportType subSport : values()) {
			if (subSport.value == value) {
				return subSport;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}