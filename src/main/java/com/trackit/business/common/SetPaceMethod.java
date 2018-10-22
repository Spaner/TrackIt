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

import java.util.HashMap;
import java.util.Map;


public enum SetPaceMethod {
	SPEED_PERCENTAGE("SPEED_PERCENTAGE"),
	TIME_PERCENTAGE("TIME_PERCENTAGE"),
	TARGET_SPEED("TARGET_SPEED"),
	TARGET_TIME("TARGET_TIME"),
	CONSTANT_TARGET_SPEED("CONSTANT_TARGET_SPEED"),
	NEW_DATES("NEW_DATES"),
	SMART_PACE("SMART_PACE");
	
	private static Map<String, SetPaceMethod> methodsMap;
	private String name;
	
	static {
		methodsMap = new HashMap<>();
		methodsMap.put(SPEED_PERCENTAGE.getName(), SPEED_PERCENTAGE);
		methodsMap.put(TIME_PERCENTAGE.getName(), TIME_PERCENTAGE);
		methodsMap.put(TARGET_SPEED.getName(), TARGET_SPEED);
		methodsMap.put(TARGET_TIME.getName(), SetPaceMethod.TARGET_TIME);
		methodsMap.put(NEW_DATES.getName(), SetPaceMethod.NEW_DATES);
		methodsMap.put(SMART_PACE.getName(), SMART_PACE);
	}
	
	private SetPaceMethod(String name) {
		this.name = name;
	}
	
	private String getName() {
		return this.name;
	}
	
	private static final String[] messageCodes = {"setPaceMethod.speedPercentage", "setPaceMethod.timePercentage",
		"setPaceMethod.targetSpeed", "setPaceMethod.targetTime", "setPaceMethod.constantTargetSpeed", "setPaceMethod.newDates",
		"setPaceMethod.smartPace" };
	
	public static SetPaceMethod lookup(String methodName) {
		SetPaceMethod method = methodsMap.get(methodName);

		if (method == null) {
			throw new IllegalArgumentException();
		}
		
		return method;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}