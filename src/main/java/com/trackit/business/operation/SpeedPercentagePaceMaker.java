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
package com.trackit.business.operation;

import java.util.Map;

import com.trackit.business.common.Constants;
import com.trackit.business.domain.Course;

class SpeedPercentagePaceMaker extends SpeedPaceMaker implements PaceMaker {
	SpeedPercentagePaceMaker(Course course, Map<String, Object> options) {
		super(course, getCustomOptions(course, options));
	}

	private static Map<String, Object> getCustomOptions(Course course, Map<String, Object> options) {
		if (!options.containsKey(Constants.SetPaceOperation.PERCENTAGE)) {
			throw new IllegalArgumentException("SpeedPercentagePaceMaker: missing percentage parameter");
		}
		
		if (!options.containsKey(Constants.SetPaceOperation.INCLUDE_PAUSES)) {
			throw new IllegalArgumentException("SpeedPercentagePaceMaker: missing include pauses parameter");
		}
		
		double percentage = (Double) options.get(Constants.SetPaceOperation.PERCENTAGE);
		double targetSpeed = (course.getAverageSpeed() / 1000.0 * 3600.0 * percentage);
		options.put(Constants.SetPaceOperation.SPEED, targetSpeed);
		
		return options;
	}
}
