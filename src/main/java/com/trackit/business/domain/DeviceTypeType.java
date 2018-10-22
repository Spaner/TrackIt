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

public enum DeviceTypeType {
	ANTFS((short) 1),
	BIKE_POWER((short) 11),
	ENVIRONMENT_SENSOR_LEGACY((short) 12),
	MULTI_SPORT_SPEED_DISTANCE((short) 15),
	CONTROL((short) 16),
	FITNESS_EQUIPMENT((short) 17),
	BLOOD_PRESSURE((short) 18),
	GEOCACHE_NODE((short) 19),
	LIGHT_ELECTRIC_VEHICLE((short) 20),
	ENV_SENSOR((short) 25),
	WEIGHT_SCALE((short) 119),
	HEART_RATE((short) 120),
	BIKE_SPEED_CADENCE((short) 121),
	BIKE_CADENCE((short) 122),
	BIKE_SPEED((short) 123),
	STRIDE_SPEED_DISTANCE((short) 124);
	
	private short value;
	
	private DeviceTypeType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"deviceType.antfs", "deviceType.bikePower", "deviceType.environmentSensorLegacy",
		"deviceType.multiSportSpeedDistance", "deviceType.control", "deviceType.fitnessEquipment", "deviceType.bloodPressure",
		"deviceType.geocacheNode", "deviceType.lightElectricVehicle", "deviceType.envSensor", "deviceType.weightScale",
		"deviceType.heartRate", "deviceType.bikeSpeedCadence", "deviceType.bikeCadence", "deviceType.bikeSpeed",
		"deviceType.strideSpeedDistance"};
	
	public static DeviceTypeType lookup(short deviceTypeValue) {
		for (DeviceTypeType deviceType : values()) {
			if (deviceTypeValue == deviceType.getValue()) {
				return deviceType;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}