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
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class RemoveLapOperation {
	private Course course;
	private CourseLap lap;
	
	public RemoveLapOperation(CourseLap lap) {
		Objects.requireNonNull(lap);
		Objects.requireNonNull(lap.getParent());
		
		this.course = lap.getParent();
		this.lap = lap;
	}
	
	public void execute() throws TrackItException {
		validate();
		removeLap();
		consolidateLaps();
	}
	
	private void validate() throws TrackItException {
		if (!course.getLaps().contains(lap)) {
			throw new TrackItException("RemoveLapOperation: lap not found on course!");
		}
		
		if (course.getLaps().size() < 1) {
			throw new TrackItException("RemoveLapOperation: a course must have at least one lap!");
		}
	}

	private void removeLap() throws TrackItException {
		int lapIndex = course.getLaps().indexOf(lap);
		
		if (lapIndex == 0) {
			course.getLaps().get(lapIndex + 1).setStartTime(lap.getStartTime());
		} else {
			course.getLaps().get(lapIndex - 1).setEndTime(lap.getEndTime());
		}
		
		course.getLaps().remove(lap);
	}

	private void consolidateLaps() {
		for (Lap lap : course.getLaps()) {
			lap.consolidate(ConsolidationLevel.SUMMARY);
		}
	}
}
