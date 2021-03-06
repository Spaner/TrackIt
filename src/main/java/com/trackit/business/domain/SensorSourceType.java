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

public enum SensorSourceType {
	FOOTPOD,
	BIKE,
	UNDEFINED;
	
	private static final String[] descriptions = {"Footpod", "Bike", "Undefined"};
	
	public String getDescription() {
		return descriptions[this.ordinal()];
	}
	
	public static SensorSourceType lookup(String sensorSourceTypeDescription) {
		for (int i = 0; i < SensorSourceType.values().length; i++) {
			if (SensorSourceType.values()[i].getDescription().equals(sensorSourceTypeDescription)) {
				return SensorSourceType.values()[i];
			}
		}
		return null;
	}
}