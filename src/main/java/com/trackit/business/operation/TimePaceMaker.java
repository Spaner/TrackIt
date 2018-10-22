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

import java.util.Date;
import java.util.Map;

import com.trackit.business.common.Constants;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.Trackpoint;

class TimePaceMaker implements PaceMaker {
	private Course course;
	private double targetTime;
	private boolean includePauses;
	private double weight;
	
	TimePaceMaker(Course course, Map<String, Object> options) {
		if (!options.containsKey(Constants.SetPaceOperation.TIME)) {
			throw new IllegalArgumentException("TimePaceMaker: missing target time parameter");
		}
		
		if (!options.containsKey(Constants.SetPaceOperation.INCLUDE_PAUSES)) {
			throw new IllegalArgumentException("TimePaceMaker: missing include pauses parameter");
		}
		
		this.course = course;
		this.targetTime = (Double) options.get(Constants.SetPaceOperation.TIME);
		this.includePauses = (Boolean) options.get(Constants.SetPaceOperation.INCLUDE_PAUSES);
		if(options.get(Constants.SetPaceOperation.WEIGHT) == null){
			this.weight = targetTime / getCourseTime();
			options.put(Constants.SetPaceOperation.WEIGHT, 1/weight);
		}
		else{
			this.weight = (Double)options.get(Constants.SetPaceOperation.WEIGHT);
			options.put(Constants.SetPaceOperation.WEIGHT, 1/weight);
		}
	}

	@Override
	public void setPace() {
		double targetTimeDiff = calculateTimeDifference();
		long currentTimeMS = course.getFirstTrackpoint().getTimestamp().getTime();
		double timeFromPrevious;
		double weight;
		double speed;
		
		//57421
		boolean previousIsInsidePause = course.isInsidePause(course.getFirstTrackpoint().getTimestamp().getTime());
		boolean currentIsInsidePause = previousIsInsidePause;
		if (!includePauses) {
			new PauseDetectionPicCaseOperation().process(course);
			currentIsInsidePause = course.isInsidePause(currentTimeMS);
		}
		
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			//weight = calculateWeight(trackpoint);
			//timeFromPrevious = trackpoint.getTimeFromPrevious() + (targetTimeDiff * weight);
			//weight = targetTime / getCourseTime();
			timeFromPrevious = trackpoint.getTimeFromPrevious();
			if (!includePauses) {
				previousIsInsidePause = currentIsInsidePause;
				currentIsInsidePause = course.isInsidePause(trackpoint.getTimestamp().getTime());

				if (!previousIsInsidePause || !currentIsInsidePause) {
					timeFromPrevious *= this.weight;
				}
			} else {
				timeFromPrevious *= this.weight;
			}
			currentTimeMS += (long) (timeFromPrevious * 1000);
			speed = (timeFromPrevious > 0 ? trackpoint.getDistanceFromPrevious() / timeFromPrevious : 0.0);
			
			trackpoint.setTimeFromPrevious(timeFromPrevious);
			trackpoint.setTimestamp(new Date(currentTimeMS));
			trackpoint.setSpeed(speed);
		}
	}
	
	private double calculateTimeDifference() {
		return (targetTime - getCourseTime());
	}

	private double calculateWeight(Trackpoint trackpoint) {
		return (trackpoint.getTimeFromPrevious() / getCourseTime());
	}

	private double getCourseTime() {
		return (includePauses ? course.getElapsedTime() : course.getMovingTime());
	}
}
