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
package com.henriquemalheiro.trackit.business.operation;

import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Course;

class TimePercentagePaceMaker extends TimePaceMaker implements PaceMaker {
	TimePercentagePaceMaker(Course course, Map<String, Object> options) {
		super(course, getCustomOptions(course, options));
	}

	private static Map<String, Object> getCustomOptions(Course course, Map<String, Object> options) {
		if (!options.containsKey(Constants.SetPaceOperation.PERCENTAGE)) {
			throw new IllegalArgumentException("TimePercentagePaceMaker: missing percentage parameter");
		}
		
		if (!options.containsKey(Constants.SetPaceOperation.INCLUDE_PAUSES)) {
			throw new IllegalArgumentException("TimePercentagePaceMaker: missing include pauses parameter");
		}
		
		boolean pauses = (Boolean) options.get(Constants.SetPaceOperation.INCLUDE_PAUSES);
		double percentage = (Double) options.get(Constants.SetPaceOperation.PERCENTAGE);
		double targetTime = (getCourseTime(course, pauses)  * percentage);
		options.put(Constants.SetPaceOperation.TIME, targetTime);
		
		return options;
	}
	
	private static double getCourseTime(Course course, boolean pauses) {
		return (pauses ? course.getElapsedTime() : course.getMovingTime());
	}
}
