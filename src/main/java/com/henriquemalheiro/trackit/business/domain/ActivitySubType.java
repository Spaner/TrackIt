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

public enum ActivitySubType {
	GENERIC("Generic"),
	TREADMILL("Treadmill"),
	STREET("Street"),
	TRAIL("Trail"),
	TRACK("Track"),
	SPIN("Spin"),
	INDOOR_CYCLING("Indoor Cycling"),
	ROAD("Road"),
	MOUNTAIN("Mountain"),
	DOWNHILL("Downhill"),
	RECUMBENT("Recumbent"),
	CYCLOCROSS("Cyclocross"),
	HAND_CYCLING("Hand Cycling"),
	TRACK_CYCLING("Track Cycling"),
	INDOOR_ROWING("Indoor Rowing"),
	ELLIPTICAL("Elliptical"),
	STAIR_CLIMBING("Stair Climbing"),
	LAP_SWIMMING("Lap Swimming"),
	OPEN_WATER("Open Water"),
	ALL("All");
	
	private String name;
	
	private ActivitySubType(String name) {
		this.name = name;
	}
	
	private String getName() {
		return name;
	}
	
	private static final String[] messageCodes = {"subActivity.generic", "subActivity.treadmill", "subActivity.street",
		"subActivity.trail", "subActivity.track", "subActivity.spin", "subActivity.indoorCycling", "subActivity.road",
		"subActivity.mountain", "subActivity.downhill", "subActivity.recumbent", "subActivity.cyclocross",
		"subActivity.handCycling", "subActivity.trackCycling", "subActivity.indoorRowing", "subActivity.elliptical",
		"subActivity.stairClimbing", "subActivity.lapSwimming", "subActivity.openWater", "subActivity.all"};
	
	public static ActivitySubType lookup(String subActivityName) {
		for (ActivitySubType subActivity : values()) {
			if (subActivityName.equals(subActivity.getName())) {
				return subActivity;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}