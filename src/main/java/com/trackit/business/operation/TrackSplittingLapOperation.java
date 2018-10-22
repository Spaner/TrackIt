/*
 * This file is part of Track It!.
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

import java.util.List;
import java.util.Map;

import com.trackit.business.common.Constants;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CourseLap;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;

public class TrackSplittingLapOperation extends OperationBase implements Operation{
	
	private Course course;
	private Trackpoint trackpoint;
	
	public TrackSplittingLapOperation() {
		super();
		setUp();
	}
	
	public TrackSplittingLapOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		course = (Course) options.get(Constants.SplitAtSelectedOperation.COURSE);
		trackpoint = (Trackpoint) options.get(Constants.SplitAtSelectedOperation.TRACKPOINT);
	}

	@Override
	public String getName() {
		return Constants.SplitAtSelectedOperation.NAME;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		splitCourseAtSelectedTrackpoint(document);
		
		consolidate(course);
//		course.setUnsavedTrue();		// 12335 : 2016-10-03
		course.getStatus().setTrackAsChanged();
	}

	@Override
	public void process(List<GPSDocument> document) throws TrackItException {
		// DO nothing
		
	}
	
	private void splitCourseAtSelectedTrackpoint(GPSDocument document) {
		Lap currentLap = null;
		for (Lap lap : course.getLaps()) {
			if (!lap.getStartTime().after(trackpoint.getTimestamp())
					&& !lap.getEndTime().before(trackpoint.getTimestamp())) {
				currentLap = lap;
				break;
			}
		}
		
		int lapIndex = course.getLaps().indexOf(currentLap);
		int startIndex = currentLap.getTrackpoints().indexOf(trackpoint);
		int endIndex = currentLap.getTrackpoints().size();
		CourseLap newLap = new CourseLap(course);
		newLap.setTrackpoints(currentLap.getTrackpoints().subList(startIndex, endIndex));
		endIndex = startIndex + 1;
		startIndex = 0;
		currentLap.setTrackpoints(currentLap.getTrackpoints().subList(startIndex, endIndex));
		course.getLaps().add(lapIndex + 1, newLap);
		
	}
	
	private void consolidate(Course course) {
		double offset = course.getFirstTrackpoint().getDistance();
		double distance;
		
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			distance = trackpoint.getDistance() - offset;
			trackpoint.setDistance(distance);
			trackpoint.setParent(course);
		}
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}

}
