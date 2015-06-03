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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class ReverseOperation extends OperationBase implements Operation {

	public ReverseOperation() {
		super();
		setUp();
	}

	public ReverseOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
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

	public void process(GPSDocument document, String mode)
			throws TrackItException {
		Course course;
		if (mode.equals(Constants.ReverseOperation.NEWRETURN)) {
			course = document.getCourses().get(1);
		} else {
			course = document.getCourses().get(0);
		}
		reverse(course, mode);
	}

	private void reverse(Course course, String mode) {

		List<Trackpoint> newTrackpointsList = new ArrayList<>();

		ListIterator<Trackpoint> iter = course.getTrackpoints().listIterator(
				course.getTrackpoints().size());
		Trackpoint trackpoint;
		double timeFromPreviousTemp = 0.0;
		double timeFromPrevious = 0.0;
		double distanceFromPreviousTemp = 0.0;
		double distanceFromPrevious = 0.0;

		Date startTime = null;
		Date endTime = null;

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
		updateTrackpoints(newTrackpointsList, mode);

		if (mode.equals(Constants.ReverseOperation.NEWRETURN)
				|| mode.equals(Constants.ReverseOperation.RETURN)) {

			List<Lap> newLapList = new ArrayList<>();
			List<Trackpoint> newTrackpointLapList = new ArrayList<>();
			ListIterator<Lap> lapIter = course.getLaps().listIterator(
					course.getLaps().size());
			ListIterator<Trackpoint> lapPointsIter;
			Lap lap;

			while (lapIter.hasPrevious()) {
				lap = lapIter.previous();
				/*
				 * lapPointsIter = lap.getTrackpoints()
				 * .listIterator(lap.getTrackpoints().size()); while
				 * (lapPointsIter.hasPrevious()) { trackpoint =
				 * lapPointsIter.previous();
				 * 
				 * timeFromPreviousTemp = trackpoint.getTimeFromPrevious();
				 * distanceFromPreviousTemp =
				 * trackpoint.getDistanceFromPrevious();
				 * 
				 * trackpoint.setTimeFromPrevious(timeFromPrevious);
				 * trackpoint.setDistanceFromPrevious(distanceFromPrevious);
				 * 
				 * timeFromPrevious = timeFromPreviousTemp; distanceFromPrevious
				 * = distanceFromPreviousTemp;
				 * 
				 * newTrackpointLapList.add(trackpoint); }
				 */
				Date newStartTime = lap.getEndTime();
				Date newEndTime = lap.getStartTime();
				lap.setStartTime(newStartTime);
				lap.setEndTime(newEndTime);
				// lap.getTrackpoints().clear();
				// lap.setTrackpoints(newTrackpointLapList);
				// updateTrackpoints(newTrackpointLapList);

				newLapList.add(lap);

			}
		}

		// course.getLaps().clear();
		// course.setLaps(newLapList);

	}

	private void updateTrackpoints(List<Trackpoint> newTrackpointsList,
			String mode) {
		if (newTrackpointsList.isEmpty()) {
			return;
		}

		long timestamp = 0;
		if (mode.equals(Constants.ReverseOperation.NORMAL)) {
			timestamp = newTrackpointsList.get(newTrackpointsList.size() - 1)
					.getTimestamp().getTime();
		}
		if (mode.equals(Constants.ReverseOperation.NEWRETURN)
				|| mode.equals(Constants.ReverseOperation.RETURN)) {
			timestamp = newTrackpointsList.get(0).getTimestamp().getTime();
		}
		double distance = 0.0;
		for (Trackpoint trackpoint : newTrackpointsList) {
			timestamp += (trackpoint.getTimeFromPrevious() * 1000);
			distance += trackpoint.getDistanceFromPrevious();

			trackpoint.setTimestamp(new Date(timestamp));
			trackpoint.setDistance(distance);
		}
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		Course course = document.getCourses().get(0);
		reverse(course, "stuff");

	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		Course course = document.getCourses().get(0);
		reverse(course, "stuff");

	}

	@Override
	public void undoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub

	}

}
