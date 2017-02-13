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

import java.util.Objects;

import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class AddLapOperation {
	private Course course;
	private Trackpoint trackpoint;
	
	public AddLapOperation(Course course, Trackpoint trackpoint) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		
		this.course = course;
		this.trackpoint = trackpoint;
	}
	
	public void execute() throws TrackItException {
		addLap();
	}

	private void addLap() throws TrackItException {
		CourseLap lap = getEnclosingLap();
		splitLap(lap);
		consolidateLaps();
	}
	
	private CourseLap getEnclosingLap() throws TrackItException {
		for (Lap lap : course.getLaps()) {
			if (!lap.getStartTime().after(trackpoint.getTimestamp())
					&& !lap.getEndTime().before(trackpoint.getTimestamp())) {
				return (CourseLap) lap;
			}
		}
		throw new TrackItException("AddLapOperation: enclosing lap not found!");
	}

	private void splitLap(CourseLap lap) {
		int lapIndex = course.getLaps().indexOf(lap);
		
		CourseLap newLap = new CourseLap(course);
		newLap.setStartTime(lap.getStartTime());
		newLap.setEndTime(trackpoint.getTimestamp());
		course.getLaps().add(lapIndex, newLap);
		
		lap.setStartTime(trackpoint.getTimestamp());
	}

	private void consolidateLaps() {
		for (Lap lap : course.getLaps()) {
			lap.consolidate(ConsolidationLevel.SUMMARY);
		}
	}
}
