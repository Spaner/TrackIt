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

public enum LapTriggerType {
	MANUAL((short) 0),
	TIME((short) 1),
	DISTANCE((short) 2),
	POSITION_START((short) 3),
	POSITION_LAP((short) 4),
	POSITION_WAYPOINT((short) 5),
	POSITION_MARKED((short) 6),
	SESSION_END((short) 7),
	FITNESS_EQUIPMENT((short) 8);
	
	private short value;
	
	private LapTriggerType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"lapTriggerType.manual", "lapTriggerType.time", "lapTriggerType.distance",
		"lapTriggerType.positionStart", "lapTriggerType.positionLap", "lapTriggerType.positionWaypoint", "lapTriggerType.positionMarked",
		"lapTriggerType.sessionEnd", "lapTriggerType.fitnessEquipment"};
	
	public static LapTriggerType lookup(short value) {
		for (LapTriggerType lapTrigger : values()) {
			if (lapTrigger.value == value) {
				return lapTrigger;
			}
		}
		return null;
	}
	
	public static LapTriggerType lookup(String name) {
		for (LapTriggerType lapTrigger : values()) {
			if (lapTrigger.name().equalsIgnoreCase(name)) {
				return lapTrigger;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}