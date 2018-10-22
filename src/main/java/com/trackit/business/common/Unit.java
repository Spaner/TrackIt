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
package com.trackit.business.common;

public enum Unit {
	NONE("NONE"), KILOMETER("KILOMETER"), METER("METER"), SECOND("SECOND"), KILOMETER_PER_HOUR("KILOMETER_PER_HOUR"), DEGREE(
	        "DEGREE"), RPM("RPM"), BPM("BPM"), CALORIES("CALORIES"), DEGREES_CELSIUS("DEGREES_CELSIUS"), DATE_TIME(
	        "DATE_TIME"), PERCENTAGE("PERCENTAGE"), STROKES_PER_LAP("STROKES_PER_LAP"), WATT("WATT"), METERS_PER_SECOND(
	        "METERS_PER_SECOND"), JOULE("JOULE"), VOLT("VOLT"), TRACKPOINT_PER_METER("TRACKPOINT_PER_METER"), MINUTE("MINUTE");

	private String name;

	private Unit(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	private static final String[] messageCodes = { "unit.none", "unit.kilometer", "unit.meter", "unit.second",
	        "unit.kilometerPerHour", "unit.degree", "unit.rpm", "unit.bpm", "unit.calories", "unit.degreesCelsius",
	        "unit.dateTime", "unit.percentage", "unit.strokesPerLap", "unit.watt", "unit.metersPerSecond",
	        "unit.joule", "unit.volt", "unit.trackpointPerMeter", "unit.minute" };

	public static Unit lookup(String unitName) {
		for (Unit unit : values()) {
			if (unitName.equals(unit.getName())) {
				return unit;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}