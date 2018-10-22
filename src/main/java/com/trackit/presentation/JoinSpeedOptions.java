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
package com.trackit.presentation;

import com.trackit.business.common.Messages;

public enum JoinSpeedOptions {
	CONSTANT_DEFAULT("constant default speed"),
	CONSTANT_USER("constant selected speed"),
	FIRST_AVG("first course average moving speed"),
	FIRST_END("first course ending speed"),
	SECOND_AVG("second course average moving speed"),
	SECOND_START("second course start speed"),
	AVG_BOTH_COURSES("average moving speed of both courses"),
	AVG_CONNECTING("average speed of connecting points"),
	KEEP_TIMESTAMPS("keep timestamps"),
	SET_TIME("set time to travel the distance between courses");
	
	private static final String[] messageCodes = {"joinSpeedOptions.constantDefault", 
		"joinSpeedOptions.constantUser", 
		"joinSpeedOptions.firstAvg", 
		"joinSpeedOptions.firstEnd", 
		"joinSpeedOptions.secondAvg",
		"joinSpeedOptions.secondStart",
		"joinSpeedOptions.avgBothCourses",
		"joinSpeedOptions.avgConnecting",
		"joinSpeedOptions.keepTimestamps",
		"joinSpeedOptions.setTime"};
	
	private String joinOptionName;
	
	private JoinSpeedOptions(String joinOptionName) {
		this.joinOptionName = joinOptionName;
	}
	
	public String getJoinSpeedName() {
		return joinOptionName;
	}
	
	public String getDescription() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
	
	public static JoinSpeedOptions lookup(String joinOptionName) {
		for (JoinSpeedOptions joinOption : values()) {
			if (joinOption.getJoinSpeedName().equalsIgnoreCase(joinOptionName)) {
				return joinOption;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
}