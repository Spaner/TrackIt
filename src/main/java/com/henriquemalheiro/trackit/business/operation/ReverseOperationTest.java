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
/*package com.henriquemalheiro.trackit.business.operation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class ReverseOperationTest extends AbstractUndoableEdit implements Operation {
	
	OperationBase operation = new OperationBase();
	
	public ReverseOperationTest() {
		//super();
		operation.setUp();
	}
	
	public ReverseOperationTest(Map<String, Object> options) {
		this();
		this.operation.options.putAll(options);
		setUp();
	}

	private void setUp() {
	}

	@Override
	public String getName() {
		return Constants.JoinOperation.NAME;
	}
	
	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		// Do nothing
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		Course course = document.getCourses().get(0);
		reverse(course);
	}

	private void reverse(Course course) {
		List<Trackpoint> newTrackpointsList = new ArrayList<>();
		
		ListIterator<Trackpoint> iter = course.getTrackpoints().listIterator(course.getTrackpoints().size());
		Trackpoint trackpoint;
		double timeFromPreviousTemp = 0.0;
		double timeFromPrevious = 0.0;
		double distanceFromPreviousTemp = 0.0;
		double distanceFromPrevious = 0.0;
		
		while (iter.hasPrevious()) {
			trackpoint = iter.previous();
			
			timeFromPreviousTemp = trackpoint.getTimeFromPrevious();
			distanceFromPreviousTemp = trackpoint.getDistanceFromPrevious();
			
			trackpoint.setTimeFromPrevious(timeFromPrevious);
			trackpoint.setDistanceFromPrevious(distanceFromPrevious);
			
			timeFromPrevious = timeFromPreviousTemp;
			distanceFromPrevious = distanceFromPreviousTemp;
			
			newTrackpointsList.add(trackpoint);
		}
		
		course.getTrackpoints().clear();
		course.setTrackpoints(newTrackpointsList);
		
		updateTrackpoints(newTrackpointsList);
	}

	private void updateTrackpoints(List<Trackpoint> newTrackpointsList) {
		if (newTrackpointsList.isEmpty()) {
			return;
		}
		
		long timestamp = newTrackpointsList.get(0).getTimestamp().getTime();
		double distance = 0.0;
		for (Trackpoint trackpoint : newTrackpointsList) {
			timestamp += (trackpoint.getTimeFromPrevious() * 1000);
			distance += trackpoint.getDistanceFromPrevious();
			
			trackpoint.setTimestamp(new Date(timestamp));
			trackpoint.setDistance(distance);
		}
	}

	@Override
	public void start(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgress(int progress, String message) {
		// TODO Auto-generated method stub
		
	}
}*/
