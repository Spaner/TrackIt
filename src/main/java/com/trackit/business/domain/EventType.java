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

import com.trackit.business.common.Messages;

public enum EventType {
	TIMER((short) 0),
	WORKOUT((short) 3),
	WORKOUT_STEP((short) 4),
	POWER_DOWN((short) 5),
	POWER_UP((short) 6),
	OFF_COURSE((short) 7),
	SESSION((short) 8),
	LAP((short) 9),
	COURSE_POINT((short) 10),
	BATTERY((short) 11),
	VIRTUAL_PARTNER_PACE((short) 12),
	HR_HIGH_ALERT((short) 13),
	HR_LOW_ALERT((short) 14),
	SPEED_HIGH_ALERT((short) 15),
	SPEED_LOW_ALERT((short) 16),
	CAD_HIGH_ALERT((short) 17),
	CAD_LOW_ALERT((short) 18),
	POWER_HIGH_ALERT((short) 19),
	POWER_LOW_ALERT((short) 20),
	RECOVERY_HR((short) 21),
	BATTERY_LOW((short) 22),
	TIME_DURATION_ALERT((short) 23),
	DISTANCE_DURATION_ALERT((short) 24),
	CALORIE_DURATION_ALERT((short) 25),
	ACTIVITY((short) 26),
	LENGTH((short) 27),
	FITNESS_EQUIPMENT((short) 28);
	
	private short value;
	
	private EventType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"eventType.timer", "eventType.workout", "eventType.workoutStep", "eventType.powerDown",
		"eventType.powerUp", "eventType.offCourse", "eventType.session", "eventType.lap", "eventType.coursePoint", "eventType.battery",
		"eventType.virtualPartnerPace", "eventType.heartRateHighAlert", "eventType.heartRateLowAlert", "eventType.speedHighAlert",
		"eventType.speedLowAlert", "eventType.cadenceHighAlert", "eventType.cadenceLowAlert", "eventType.powerHighAlert",
		"eventType.powerLowAlert", "eventType.recoveryHeartRate", "eventType.batteryLow", "eventType.timeDurationAlert",
		"eventType.distanceDurationAlert", "eventType.calorieDurationAlert", "eventType.activity", "eventType.length",
		"eventType.fitnessEquipment"};
	
	public static EventType lookup(short value) {
		for (EventType event : values()) {
			if (event.value == value) {
				return event;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}