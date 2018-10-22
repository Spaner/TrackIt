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

class ConstantSpeedPaceMaker implements PaceMaker {
	private Course course;
	private double targetSpeed;
	private boolean includePauses;
	private Map<String, Object> options;
	
	ConstantSpeedPaceMaker(Course course, Map<String, Object> options) {
		if (!options.containsKey(Constants.SetPaceOperation.SPEED)) {
			throw new IllegalArgumentException();
		}
		
		if (!options.containsKey(Constants.SetPaceOperation.INCLUDE_PAUSES)) {
			throw new IllegalArgumentException("ConstantSpeedPaceMaker: missing include pauses parameter");
		}
		
		this.course = course;
		double speed = (Double) options.get(Constants.SetPaceOperation.SPEED);
		//this.targetSpeed = speed * 1000.0 / 3600.0;
		this.includePauses = (Boolean) options.get(Constants.SetPaceOperation.INCLUDE_PAUSES);
		if(options.get(Constants.SetPaceOperation.WEIGHT) == null){
			this.targetSpeed = speed * 1000.0 / 3600.0;
			options.put(Constants.SetPaceOperation.WEIGHT, 1/targetSpeed);
		}
		else{
			this.targetSpeed = (Double)options.get(Constants.SetPaceOperation.WEIGHT);
			options.put(Constants.SetPaceOperation.WEIGHT, 1/targetSpeed);
		}
		this.options = options;
	}

	@Override
	public void setPace() {
		long currentTimeMS = course.getFirstTrackpoint().getTimestamp().getTime();
		double timeFromPrevious;
		double speed;
		double distanceFromPrevious;
		// 57421
		boolean previousIsInsidePause = course.isInsidePause(course.getFirstTrackpoint().getTimestamp().getTime());
		boolean currentIsInsidePause = previousIsInsidePause;
		if (!includePauses) {
//			new PauseDetectionPicCaseOperation().process(course);		// 12335: 2018-07-16
			new NonReportingPauseDetectionOperation().process( course);
			currentIsInsidePause = course.isInsidePause(currentTimeMS);
		}
		
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			timeFromPrevious = trackpoint.getTimeFromPrevious();
			distanceFromPrevious = trackpoint.getDistanceFromPrevious();
			if (!includePauses) {
				previousIsInsidePause = currentIsInsidePause;
				currentIsInsidePause = course.isInsidePause(trackpoint.getTimestamp().getTime());
				
				if (!previousIsInsidePause || !currentIsInsidePause) {
					timeFromPrevious = options.get(Constants.SetPaceOperation.WEIGHT) == null ? distanceFromPrevious / targetSpeed : timeFromPrevious / targetSpeed;			
				}
			} else {
				timeFromPrevious = options.get(Constants.SetPaceOperation.WEIGHT) == null ? distanceFromPrevious / targetSpeed : timeFromPrevious / targetSpeed;
			}
			currentTimeMS += (long) (timeFromPrevious * 1000);
			speed = (timeFromPrevious > 0 ? trackpoint.getDistanceFromPrevious() / timeFromPrevious : 0.0);
			
			trackpoint.setTimeFromPrevious(timeFromPrevious);
			trackpoint.setTimestamp(new Date(currentTimeMS));
			trackpoint.setSpeed(speed);
		}
	}
	
	protected double getCourseTime() {
		return (includePauses ? course.getElapsedTime() : course.getMovingTime());
	}
	
	protected Course getCourse() {
		return course;
	}
	
	protected double getTargetSpeed() {
		return targetSpeed;
	}
	
	protected boolean isIncludePauses() {
		return includePauses;
	}
}
