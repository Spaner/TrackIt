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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.Utilities;

public class JoiningOperation extends OperationBase implements Operation {
	private boolean addLapMarkers;
	private List<Course> courses;
	private Map<Lap, Pair<Trackpoint, Trackpoint>> lapsInfo;

	public JoiningOperation() {
		super();
		this.options.put(Constants.JoinOperation.ADD_LAP_MARKER, true);
		setUp();
	}
	
	public JoiningOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		addLapMarkers = (Boolean) options.get(Constants.JoinOperation.ADD_LAP_MARKER);
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
		Course newCourse = join(document);
		document.getCourses().clear();
		document.add(newCourse);
	}

	private Course join(GPSDocument document) {
		courses = document.getCourses();
		List<Trackpoint> trackpoints = new ArrayList<Trackpoint>();
		List<CoursePoint> coursePoints = new ArrayList<CoursePoint>();
		lapsInfo = new LinkedHashMap<>();
		Map<CoursePoint, Trackpoint> coursePointsInfo = new LinkedHashMap<>();
		
		for (Course course : courses) {
			storeLapInfo(course);
			storeCoursePointsInfo(coursePointsInfo, course);
			joinTrackpoints(trackpoints, course.getTrackpoints());
		}
		
		Course newCourse = new Course();
		newCourse.setTrackpoints(trackpoints);
		newCourse.setCoursePoints(coursePoints);
		newCourse.setName(courses.get(0).getName());
		
		for (Trackpoint trackpoint : trackpoints) {
			trackpoint.setParent(newCourse);
		}

		addLaps(newCourse);
		addCoursePoints(newCourse, coursePointsInfo);
		newCourse.consolidate(ConsolidationLevel.SUMMARY);
		
		return newCourse;
	}

	private void storeLapInfo(Course course) {
		for (Lap lap : course.getLaps()) {
			lapsInfo.put(lap, Pair.create(lap.getFirstTrackpoint(), lap.getLastTrackpoint()));
		}
	}
	
	private void storeCoursePointsInfo(Map<CoursePoint, Trackpoint> coursePointsInfo, Course course) {
		for (CoursePoint coursePoint : course.getCoursePoints()) {
			List<Trackpoint> trackpoints = course.getTrackpoints(coursePoint.getTime(), coursePoint.getTime());
			if (trackpoints != null && !trackpoints.isEmpty()) {
				coursePointsInfo.put(coursePoint, trackpoints.get(0));
			}
		}
	}
	
	private void addLaps(Course newCourse) {
		for (Lap lap : lapsInfo.keySet()) {
			Pair<Trackpoint, Trackpoint> edges = lapsInfo.get(lap);
			lap.setStartTime(edges.getFirst().getTimestamp());
			lap.setEndTime(edges.getSecond().getTimestamp());
			((CourseLap) lap).setParent(newCourse);
			lap.consolidate(ConsolidationLevel.SUMMARY);
			
			newCourse.add(lap);
		}
		
		if (!addLapMarkers) {
			adjustJoiningLaps(newCourse);
		}
	}
	
	private void adjustJoiningLaps(Course newCourse) {
		for (int i = 0; i < courses.size() - 1; i += 2) {
			Lap rightLap = courses.get(i + 1).getLaps().get(0);

			Lap leftLap = courses.get(i).getLastLap();
			leftLap.setEndTime(rightLap.getEndTime());
			leftLap.consolidate(ConsolidationLevel.SUMMARY);
			
			newCourse.remove(rightLap);
		}
	}

	private void addCoursePoints(Course course, Map<CoursePoint, Trackpoint> coursePointsInfo) {
		for (CoursePoint coursePoint : coursePointsInfo.keySet()) {
			Trackpoint trackpoint = coursePointsInfo.get(coursePoint);
			
			coursePoint.setDistance(trackpoint.getDistance());
			coursePoint.setTime(trackpoint.getTimestamp());
			coursePoint.setAltitude(trackpoint.getAltitude());
			coursePoint.setParent(course);
			
			course.add(coursePoint);
		}
	}

	private void joinTrackpoints(List<Trackpoint> leftTrackpoints, List<Trackpoint> rightTrackpoints) {
		long timeGap = calculateTimeGap(leftTrackpoints, rightTrackpoints);
		double distanceGap = calculateDistanceGap(leftTrackpoints, rightTrackpoints);
		
		int removeIndex = leftTrackpoints.size()-1;
		int rightSize = rightTrackpoints.size();
		
		shiftTrackpoints(rightTrackpoints, timeGap, distanceGap);
		adjustJoiningTrackpoints(leftTrackpoints, rightTrackpoints);
		
		leftTrackpoints.addAll(rightTrackpoints);
		if(rightSize != leftTrackpoints.size()){
			mergeJoiningPoints(leftTrackpoints, removeIndex);
		}
	}
	
	private void mergeJoiningPoints(List<Trackpoint> leftTrackpoints, int index) {
		
		if (!leftTrackpoints.isEmpty()) {
			Trackpoint leftLastTrackpoint = leftTrackpoints.get(index);
			Trackpoint firstRightTrackpoint = leftTrackpoints.get(index+1);
			
			double distance = calculateDistance(leftLastTrackpoint, firstRightTrackpoint);
			
			if(distance < getMinimumDistance()){
				leftTrackpoints.remove(index+1);
			}
		}
	}

	private long calculateTimeGap(List<Trackpoint> leftTrackpoints, List<Trackpoint> rightTrackpoints) {
		final double defaultSpeedMS = 10 * 1000.0 / 3600.0; // meters per second
		long timeGap = 0;
		
		if (!leftTrackpoints.isEmpty() && !rightTrackpoints.isEmpty()) {
			Trackpoint leftLastTrackpoint = leftTrackpoints.get(leftTrackpoints.size() - 1);
			Trackpoint firstRightTrackpoint = rightTrackpoints.get(0);
			
			timeGap = firstRightTrackpoint.getTimestamp().getTime() - leftLastTrackpoint.getTimestamp().getTime();
			
			double distance = calculateDistance(leftLastTrackpoint, firstRightTrackpoint);
			double speed = (leftLastTrackpoint.getSpeed() > 0.0 ? leftLastTrackpoint.getSpeed() : defaultSpeedMS);
			double timeFromPreviousMS = distance / speed * 1000.0;
			
			timeGap -= timeFromPreviousMS;
		}
		
		return timeGap;
	}
	
	private double calculateDistanceGap(List<Trackpoint> leftTrackpoints, List<Trackpoint> rightTrackpoints) {
		double distanceGap = 0;
		
		if (!leftTrackpoints.isEmpty()) {
			Trackpoint leftLastTrackpoint = leftTrackpoints.get(leftTrackpoints.size() - 1);
			Trackpoint firstRightTrackpoint = rightTrackpoints.get(0);
			
			distanceGap = leftLastTrackpoint.getDistance() + calculateDistance(leftLastTrackpoint, firstRightTrackpoint);
		}
		
		return distanceGap;
	}

	private void shiftTrackpoints(List<Trackpoint> trackpoints, long timeGap, double distanceGap) {
		long newTimestamp;
		double newDistance;
		for (Trackpoint trackpoint : trackpoints) {
			newTimestamp = trackpoint.getTimestamp().getTime() - timeGap;
			trackpoint.setTimestamp(new Date(newTimestamp));
			
			newDistance = trackpoint.getDistance() + distanceGap;
			trackpoint.setDistance(newDistance);
		}
	}
	
	private void adjustJoiningTrackpoints(List<Trackpoint> leftTrackpoints, List<Trackpoint> rightTrackpoints) {
		if (!leftTrackpoints.isEmpty() && !rightTrackpoints.isEmpty()) {
			Trackpoint leftLastTrackpoint = leftTrackpoints.get(leftTrackpoints.size() - 1);
			Trackpoint rightFirstTrackpoint = rightTrackpoints.get(0);
			Trackpoint rightSecondTrackpoint = rightTrackpoints.size() > 1 ? rightTrackpoints.get(1) : null;
			
			double distanceFromPrevious = calculateDistance(leftLastTrackpoint, rightFirstTrackpoint);
			double timeFromPrevious = calculateTimeDifference(leftLastTrackpoint, rightFirstTrackpoint);
			double speed = distanceFromPrevious / timeFromPrevious;
			
			rightFirstTrackpoint.setDistanceFromPrevious(distanceFromPrevious);
			rightFirstTrackpoint.setTimeFromPrevious(timeFromPrevious);
			rightFirstTrackpoint.setSpeed(speed);
			
			double grade = (rightSecondTrackpoint != null
					? interpolate(leftLastTrackpoint, rightFirstTrackpoint, rightSecondTrackpoint)
					: rightFirstTrackpoint.getGrade());
			
			rightFirstTrackpoint.setGrade((float) grade);
		}
	}

	private double interpolate(Trackpoint trackpoint1, Trackpoint trackpoint2, Trackpoint trackpoint3) {
		double factor = (trackpoint2.getDistanceFromPrevious()
				/ (trackpoint2.getDistanceFromPrevious() + trackpoint3.getDistanceFromPrevious()));
		
		return trackpoint1.getGrade() + ((trackpoint3.getGrade() - trackpoint1.getGrade()) * factor);
	}

	private Double calculateDistance(Trackpoint leftTrackpoint, Trackpoint firstTrackpoint) {
		return Utilities.getGreatCircleDistance(
				leftTrackpoint.getLatitude(), leftTrackpoint.getLongitude(),
				firstTrackpoint.getLatitude(), firstTrackpoint.getLongitude()) * 1000.0;
	}
	
	private double calculateTimeDifference(Trackpoint leftTrackpoint,
			Trackpoint rightTrackpoint) {
		return (rightTrackpoint.getTimestamp().getTime() - leftTrackpoint.getTimestamp().getTime()) / 1000.0;
	}
	
	private double getMinimumDistance() {
		return TrackIt.getPreferences().getDoublePreference(Constants.PrefsCategories.JOIN, null,
				Constants.JoinPreferences.MINIMUM_DISTANCE, 1.0);
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
