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

public enum BatteryStatusType {
	NEW((short) 1),
	GOOD((short) 2),
	OK((short) 3),
	LOW((short) 4),
	CRITICAL((short) 5);
	
	private short value;
	
	private BatteryStatusType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"batteryStatus.new", "batteryStatus.good", "batteryStatus.ok",
		"batteryStatus.low", "batteryStatus.critical"};
	
	public static BatteryStatusType lookup(short value) {
		for (BatteryStatusType batteryStatus : values()) {
			if (value == batteryStatus.getValue()) {
				return batteryStatus;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}