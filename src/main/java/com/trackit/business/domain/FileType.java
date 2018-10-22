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

public enum FileType {
	DEVICE("Device"),
	SETTINGS("Settings"),
	SPORT("Sport"),
	ACTIVITY("Activity"),
	WORKOUT("Workout"),
	COURSE("Course"),
	SCHEDULES("Schedules"),
	WEIGHT("Weight"),
	TOTALS("Totals"),
	GOALS("Goals"),
	BLOOD_PRESSURE("Blood Pressure"),
	MONITORING("Monitoring"),
	ACTIVITY_SUMMARY("Activity Summary"),
	MONITORING_DAILY("Monitoring Daily");
	
	private String name;
	
	private FileType(String name) {
		this.name = name;
	}
	
	private String getName() {
		return name;
	}
	
	private static final String[] messageCodes = {"fileType.device", "fileType.settings", "fileType.sport", "fileType.activity",
		"fileType.workout", "fileType.course", "fileType.schedules", "fileType.weight", "fileType.totals", "fileType.goals",
		"fileType.bloodPressure", "fileType.monitoring", "fileType.activitySummary", "fileType.monitoringDaily"};
	
	public static FileType lookup(String file) {
		for (FileType fileType : values()) {
			if (file.equals(fileType.getName())) {
				return fileType;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}