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

public enum SportType {
	GENERIC((short) 0),
	RUNNING((short) 1),
	CYCLING((short) 2),
	TRANSITION((short) 3),
	FITNESS_EQUIPMENT((short) 4),
	SWIMMING((short) 5),
	BASKETBALL((short) 6),
	SOCCER((short) 7),
	TENNIS((short) 8),
	AMERICAN_FOOTBALL((short) 9),
	TRAINING((short) 10),
	ALL((short) 254);
	
	private short value;
	
	private SportType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"sportType.generic", "sportType.running", "sportType.cycling", "sportType.transition",
		"sportType.fitnessEquipment", "sportType.swimming", "sportType.basketball", "sportType.soccer", "sportType.tennis",
		"sportType.americanFootball", "sportType.training", "sportType.all"};
	
	public static SportType lookup(short value) {
		for (SportType sport : values()) {
			if (sport.value == value) {
				return sport;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}