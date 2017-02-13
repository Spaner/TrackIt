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

public enum MessageType {
	FILE_ID("File Id"),
	CAPABILITIES("Capabilities"),
	DEVICE_SETTINGS("Device Settings"),
	USER_PROFILE("User Profile"),
	HRM_PROFILE("HRM Profile"),
	SDM_PROFILE("SDM Profile"),
	BIKE_PROFILE("Bike Profile"),
	ZONES_TARGET("Zones Target"),
	HR_ZONE("Heart Rate Zone"),
	POWER_ZONE("Power Zone"),
	MET_ZONE("Met Zone"),
	SPORT("Sport"),
	GOAL("Goal"),
	SESSION("Session"),
	LAP("Lap"),
	RECORD("Record"),
	EVENT("Event"),
	DEVICE_INFO("Device Info"),
	WORKOUT("Workout"),
	WORKOUT_STEP("Workout Step"),
	SCHEDULE("Schedule"),
	WEIGHT_SCALE("Weight Scale"),
	COURSE("Course"),
	COURSE_POINT("Course Point"),
	TOTALS("Totals"),
	ACTIVITY("Activity"),
	SOFTWARE("Software"),
	FILE_CAPABILITIES("File Capabilities"),
	MESG_CAPABILITIES("Message Capabilities"),
	FIELD_CAPABILITIES("Field Capabilities"),
	FILE_CREATOR("File Creator"),
	BLOOD_PRESSURE("Blood Pressure"),
	SPEED_ZONE("Speed Zone"),
	MONITORING("Monitoring"),
	HRV("Heart Rate Variability"),
	LENGTH("Length"),
	MONITORING_INFO("Monitoring Info"),
	PAD("Pad"),
	CADENCE_ZONE("Cadence Zone");
	
	private String name;
	
	private MessageType(String name) {
		this.name = name;
	}
	
	private String getName() {
		return name;
	}
	
	private static final String[] messageCodes = {"messageType.fileId", "messageType.capabilities", "messageType.deviceSettings",
		"messageType.userProfile", "messageType.hrmProfile", "messageType.sdmProfile", "messageType.bikeProfile", "messageType.zonesTarget",
		"messageType.heartRateZone", "messageType.powerZone", "messageType.metZone", "messageType.sport", "messageType.goal",
		"messageType.session", "messageType.lap", "messageType.record", "messageType.event", "messageType.deviceInfo", "messageType.workout",
		"messageType.workoutStep", "messageType.schedule", "messageType.weightScale", "messageType.course", "messageType.coursePoint",
		"messageType.totals", "messageType.activity", "messageType.software", "messageType.fileCapabilities", "messageType.messageCapabilities",
		"messageType.fieldCapabilities", "messageType.fileCreator", "messageType.bloodPressure", "messageType.speedZone",
		"messageType.monitoring", "messageType.heartRateVariability", "messageType.length", "messageType.monitoringInfo",
		"messageType.pad", "messageType.cadenceZone"};
	
	public static MessageType lookup(String messageName) {
		for (MessageType message : values()) {
			if (messageName.equals(message.getName())) {
				return message;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}