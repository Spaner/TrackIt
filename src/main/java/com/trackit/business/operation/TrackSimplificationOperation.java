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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.trackit.business.common.Constants;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.Utilities;

public class TrackSimplificationOperation extends OperationBase implements Operation {
	private static Logger logger = Logger.getLogger(TrackSimplificationOperation.class.getName());
	private int numberOfPoints;
	private boolean removeTrackpoints;

	public TrackSimplificationOperation() {
		super();
		options.put(Constants.TrackSimplificationOperation.NUMBER_OF_POINTS, 5000);
		options.put(Constants.TrackSimplificationOperation.COURSES, new ArrayList<Course>());
		options.put(Constants.TrackSimplificationOperation.REMOVE_TRACKPOINTS, Boolean.FALSE);
	}

	public TrackSimplificationOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		Integer numberOfPoints = (Integer) options.get(Constants.TrackSimplificationOperation.NUMBER_OF_POINTS);
		if (numberOfPoints != null) {
			this.numberOfPoints = numberOfPoints;
		}

		Boolean removeTrackpoints = (Boolean) options.get(Constants.TrackSimplificationOperation.REMOVE_TRACKPOINTS);
		if (removeTrackpoints != null) {
			this.removeTrackpoints = removeTrackpoints;
		}
	}

	@Override
	public String getName() {
		return Constants.TrackSimplificationOperation.NAME;
	}

	@Override
	public void process(GPSDocument gpsDocument) throws TrackItException {
		for (Activity activity : gpsDocument.getActivities()) {
			List<Trackpoint> trackpoints = simplifyTrack(activity.getTrackpoints());
			activity.setTrackpoints(trackpoints);
		}

		for (Course course : gpsDocument.getCourses()) {
			for (Lap lap : course.getLaps()) {
				lap.getFirstTrackpoint().setSticky(true);
				lap.getLastTrackpoint().setSticky(true);
			}
			List<Trackpoint> trackpoints = simplifyTrack(course.getTrackpoints());
			course.setTrackpoints(trackpoints);
		}
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for (GPSDocument document : documents) {
			try {
				process(document);
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	private List<Trackpoint> simplifyTrack(List<Trackpoint> trackpoints) {
		if (numberOfPoints >= trackpoints.size()) {
			return trackpoints;
		} else if (trackpoints.size() <= 2) {
			return trackpoints;
		}

		for (Trackpoint trackpoint : trackpoints) {
			trackpoint.setViewable(true);
		}

		/* Initialize list of cross track distances */
		LinkedList<CrossTrack> crossTrackList = initCrossTrackList(trackpoints);

		/* Compute all cross track distances */
		for (CrossTrack crossTrack : crossTrackList) {
			computeCrossTrackDistance(crossTrack);
		}

		/* Build map from list for performance improvements */
		TreeMap<Double, CrossTrack> map = new TreeMap<Double, CrossTrack>();
		for (CrossTrack crossTrack : crossTrackList) {
			map.put(crossTrack.getDistance(), crossTrack);
		}

		/* while we still have too many trackpoints */
		while (map.keySet().size() > numberOfPoints) {
			CrossTrack firstCrossTrack = map.firstEntry().getValue();

			if (firstCrossTrack.getDistance() == Double.MAX_VALUE) {
				logger.debug(String.format("Exiting... no more candidates to removal. Current number of points %d.",
				        numberOfPoints));
				break;
			}

			if (firstCrossTrack.getTrackpoint().isSticky()) {
				break;
			}

			if (firstCrossTrack.getPrevious() != null) {
				CrossTrack previous = firstCrossTrack.getPrevious();
				previous.setNext(firstCrossTrack.getNext());
				map.remove(previous.getDistance());
				computeCrossTrackDistance(previous);
				map.put(previous.getDistance(), previous);
			}

			if (firstCrossTrack.getNext() != null) {
				CrossTrack next = firstCrossTrack.getNext();
				next.setPrevious(firstCrossTrack.getPrevious());
				map.remove(next.getDistance());
				computeCrossTrackDistance(next);
				map.put(next.getDistance(), next);
			}

			firstCrossTrack.getTrackpoint().setViewable(false);
			map.remove(firstCrossTrack.getDistance());
			firstCrossTrack = null;
		}

		if (removeTrackpoints) {
			removeTrackpoints(trackpoints);
		}

		consolidate(trackpoints);

		return trackpoints;
	}

	private void removeTrackpoints(List<Trackpoint> trackpoints) {
		Iterator<Trackpoint> iter = trackpoints.iterator();
		while (iter.hasNext()) {
			Trackpoint trackpoint = iter.next();
			if (!trackpoint.isViewable()) {
				iter.remove();
			}
		}
	}

	private void consolidate(List<Trackpoint> trackpoints) {
		if (trackpoints.isEmpty()) {
			return;
		}

		trackpoints.get(0).setDistanceFromPrevious(0.0);
		trackpoints.get(0).setTimeFromPrevious(0.0);

		Trackpoint previousTrkpt;
		Trackpoint currentTrkpt;
		double distance;
		double time;
		double speed;

		for (int i = 1; i < trackpoints.size() - 1; i++) {
			previousTrkpt = trackpoints.get(i - 1);
			currentTrkpt = trackpoints.get(i);
			distance = calculateDistance(previousTrkpt, currentTrkpt);
			time = calculateTime(previousTrkpt, currentTrkpt);
			speed = calculateSpeed(currentTrkpt);

			currentTrkpt.setDistanceFromPrevious(distance);
			currentTrkpt.setTimeFromPrevious(time);
			currentTrkpt.setSpeed(speed);
		}
	}

	private double calculateDistance(Trackpoint previousTrkpt, Trackpoint currentTrkpt) {
		return Utilities.getGreatCircleDistance(previousTrkpt.getLatitude(), previousTrkpt.getLongitude(),
		        currentTrkpt.getLatitude(), currentTrkpt.getLongitude()) / 1000.0;
	}

	private double calculateTime(Trackpoint previousTrkpt, Trackpoint currentTrkpt) {
		return (currentTrkpt.getTimestamp().getTime() - previousTrkpt.getTimestamp().getTime()) / 1000.0;
	}

	private double calculateSpeed(Trackpoint currentTrkpt) {
		double speed = 0.0;

		if (currentTrkpt.getDistanceFromPrevious() > 0.0 && currentTrkpt.getTimeFromPrevious() > 0) {
			speed = currentTrkpt.getDistanceFromPrevious() / currentTrkpt.getTimeFromPrevious();
		}

		return speed;
	}

	private LinkedList<CrossTrack> initCrossTrackList(List<Trackpoint> trackpoints) {
		LinkedList<CrossTrack> crossTrackList = new LinkedList<CrossTrack>();
		CrossTrack tempCrossTrack = null;

		for (Trackpoint trackpoint : trackpoints) {
			CrossTrack crossTrack = new CrossTrack();

			crossTrack.setTrackpoint(trackpoint);
			crossTrack.setNext(null);
			crossTrack.setPrevious(tempCrossTrack);

			if (tempCrossTrack != null) {
				tempCrossTrack.setNext(crossTrack);
			}

			tempCrossTrack = crossTrack;

			crossTrackList.add(crossTrack);
		}

		return crossTrackList;
	}

	private void computeCrossTrackDistance(CrossTrack crossTrack) {
		Trackpoint trkpt3 = crossTrack.getTrackpoint();
		Trackpoint trkpt1 = null;
		Trackpoint trkpt2 = null;

		if (crossTrack.getTrackpoint().isSticky()) {
			crossTrack.setDistance(Double.MAX_VALUE);
			return;
		}

		if (crossTrack.getPrevious() == null) {
			crossTrack.setDistance(Double.MAX_VALUE);
			return;
		} else {
			trkpt1 = crossTrack.getPrevious().getTrackpoint();
		}

		if (crossTrack.getNext() == null) {
			crossTrack.setDistance(Double.MAX_VALUE);
			return;
		} else {
			trkpt2 = crossTrack.getNext().getTrackpoint();
		}

		// TODO:
		// if (crossTrack.getPrevious() != null && crossTrack.getNext() != null)
		// {
		// double distance = crossTrack.getTrackpoint().getDistance();
		// double previousTrkptDistance =
		// crossTrack.getPrevious().getTrackpoint().getDistance();
		// double nextTrkptDistance =
		// crossTrack.getNext().getTrackpoint().getDistance();
		//
		// double distanceToPrevious = distance - previousTrkptDistance;
		// double distanceToNext = nextTrkptDistance - distance;
		// double distancePreviousToNext = distanceToPrevious +
		// distanceToNext;

		// if (distancePreviousToNext > 200.0) {
		// crossTrack.setDistance(Double.MAX_VALUE);
		// return;
		// }
		// }

		double grade = (trkpt2.getGrade() != null ? trkpt2.getGrade() : 0.0);
		double crossTrackDistance = Utilities.radToKm(Utilities.lineDistance(trkpt1.getLatitude(),
		        trkpt1.getLongitude(), trkpt2.getLatitude(), trkpt2.getLongitude(), trkpt3.getLatitude(),
		        trkpt3.getLongitude()));
		crossTrackDistance += (grade >= 1.9 ? 0.002 : 0.0);

		crossTrack.setDistance(crossTrackDistance);
	}

	private class CrossTrack {
		private double distance;
		private Trackpoint trackpoint;
		private CrossTrack previous;
		private CrossTrack next;

		public double getDistance() {
			return distance;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		public Trackpoint getTrackpoint() {
			return trackpoint;
		}

		public void setTrackpoint(Trackpoint trackpoint) {
			this.trackpoint = trackpoint;
		}

		public CrossTrack getPrevious() {
			return previous;
		}

		public void setPrevious(CrossTrack previous) {
			this.previous = previous;
		}

		public CrossTrack getNext() {
			return next;
		}

		public void setNext(CrossTrack next) {
			this.next = next;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(distance);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CrossTrack other = (CrossTrack) obj;
			if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
				return false;
			return true;
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
