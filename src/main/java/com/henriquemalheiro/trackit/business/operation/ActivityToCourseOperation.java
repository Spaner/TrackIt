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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.CourseTrack;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.Track;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class ActivityToCourseOperation extends OperationBase implements Operation {
	private boolean removeActivities;
	private Activity activity;
	private Course course;
	
	public ActivityToCourseOperation() {
		super();
		options.put(Constants.ActivitiesToCoursesOperation.REMOVE_ACTIVITIES, Boolean.FALSE);
		setUp();
	}
	
	public ActivityToCourseOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		removeActivities = (Boolean) options.get(Constants.ActivitiesToCoursesOperation.REMOVE_ACTIVITIES);
	}

	@Override
	public String getName() {
		return Constants.ActivitiesToCoursesOperation.NAME;
	}
	
	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for (GPSDocument document : documents) {
			process(document);
		}
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		Iterator<Activity> iterator = document.getActivities().iterator();
		while (iterator.hasNext()) {
			activity = iterator.next();
			
			convertActivityToCourse(activity);
			document.add(course);
		}
		
		if (removeActivities) {
			document.getActivities().clear();
		}
	}

	private void convertActivityToCourse(Activity activity) throws TrackItException {
		course = new Course();
		course.setParent(activity.getParent());
		
		fillSummaryInfo();
		addTrackpoints();
		addLaps();
		addTracks();
	}

	private void fillSummaryInfo() {
		Session session = activity.getFirstSession();
		
		course.setFilepath(activity.getFilepath());
		
		course.setStartTime(session.getStartTime());
		course.setEndTime(session.getEndTime());
		course.setName(activity.getName());
		course.setSport(session.getSport());
		course.setSubSport(session.getSubSport());
		course.setElapsedTime(session.getElapsedTime());
		course.setTimerTime(session.getTimerTime());
		course.setMovingTime(session.getMovingTime());
		course.setPausedTime(session.getPausedTime());
		course.setDistance(session.getDistance());
		course.setAverageSpeed(session.getAverageSpeed());
		course.setAverageMovingSpeed(session.getAverageMovingSpeed());
		course.setMaximumSpeed(session.getMaximumSpeed());
		course.setAverageHeartRate(session.getAverageHeartRate());
		course.setMinimumHeartRate(session.getMinimumHeartRate());
		course.setMaximumHeartRate(session.getMaximumHeartRate());
		course.setAverageCadence(session.getAverageCadence());
		course.setAverageRunningCadence(session.getAverageRunningCadence());
		course.setMaximumCadence(session.getMaximumCadence());
		course.setMaximumRunningCadence(session.getMaximumRunningCadence());
		course.setAveragePower(session.getAveragePower());
		course.setMaximumPower(session.getMaximumPower());
		course.setCalories(session.getCalories());
		course.setFatCalories(session.getFatCalories());
		course.setAverageTemperature(session.getAverageTemperature());
		course.setMinimumTemperature(session.getMinimumTemperature());
		course.setMaximumTemperature(session.getMaximumTemperature());
		course.setTotalAscent(session.getTotalAscent());
		course.setTotalDescent(session.getTotalDescent());
		course.setAverageAltitude(session.getAverageAltitude());
		course.setMinimumAltitude(session.getMinimumAltitude());
		course.setMaximumAltitude(session.getMaximumAltitude());
		course.setAverageGrade(session.getAverageGrade());
		course.setAveragePositiveGrade(session.getAveragePositiveGrade());
		course.setAverageNegativeGrade(session.getAverageNegativeGrade());
		course.setMaximumPositiveGrade(session.getMaximumPositiveGrade());
		course.setMaximumNegativeGrade(session.getMaximumNegativeGrade());
		course.setAveragePositiveVerticalSpeed(session.getAveragePositiveVerticalSpeed());
		course.setAverageNegativeVerticalSpeed(session.getAverageNegativeVerticalSpeed());
		course.setMaximumPositiveVerticalSpeed(session.getMaximumPositiveVerticalSpeed());
		course.setMaximumNegativeVerticalSpeed(session.getMaximumNegativeVerticalSpeed());
		course.setStartLatitude(session.getStartLongitude());
		course.setStartLongitude(session.getStartLongitude());
		course.setStartAltitude(session.getStartAltitude());
		course.setEndLatitude(session.getEndLatitude());
		course.setEndLongitude(session.getEndLongitude());
		course.setEndAltitude(session.getEndAltitude());
		course.setNortheastLatitude(session.getNortheastLatitude());
		course.setNortheastLongitude(session.getNortheastLongitude());
		course.setSouthwestLatitude(session.getSouthwestLatitude());
		course.setSouthwestLongitude(session.getSouthwestLongitude());
	}
	
	private void addTrackpoints() {
		List<Trackpoint> courseTrackpoints = new ArrayList<Trackpoint>();
		
		for (Trackpoint trackpoint : activity.getTrackpoints()) {
			Trackpoint courseTrackpoint = new Trackpoint(course);
			courseTrackpoint.setTimestamp(trackpoint.getTimestamp());
			courseTrackpoint.setTimeFromPrevious(trackpoint.getTimeFromPrevious());
			courseTrackpoint.setLatitude(trackpoint.getLatitude());
			courseTrackpoint.setLongitude(trackpoint.getLongitude());
			courseTrackpoint.setAltitude(trackpoint.getAltitude());
			courseTrackpoint.setDistance(trackpoint.getDistance());
			courseTrackpoint.setDistanceFromPrevious(trackpoint.getDistanceFromPrevious());
			courseTrackpoint.setSpeed(trackpoint.getSpeed());
			courseTrackpoint.setHeartRate(trackpoint.getHeartRate());
			courseTrackpoint.setCadence(trackpoint.getCadence());
			courseTrackpoint.setPower(trackpoint.getPower());
			courseTrackpoint.setGrade(trackpoint.getGrade());
			courseTrackpoint.setResistance(trackpoint.getResistance());
			courseTrackpoint.setTimeFromCourse(trackpoint.getTimeFromCourse());
			courseTrackpoint.setTemperature(trackpoint.getTemperature());
			courseTrackpoint.setCycles(trackpoint.getCycles());
			courseTrackpoint.setTotalCycles(trackpoint.getTotalCycles());
			courseTrackpoint.setLeftRightBalance(trackpoint.getLeftRightBalance());
			courseTrackpoint.setGpsAccuracy(trackpoint.getGpsAccuracy());
			courseTrackpoint.setVerticalSpeed(trackpoint.getVerticalSpeed());
			courseTrackpoint.setCalories(trackpoint.getCalories());
			courseTrackpoint.setSticky(trackpoint.isSticky());
			courseTrackpoint.setViewable(trackpoint.isViewable());
			
			courseTrackpoints.add(courseTrackpoint);
		}
		
		course.setTrackpoints(courseTrackpoints);
	}

	private void addLaps() {
		for (Lap lap : activity.getLaps()) {
			CourseLap courseLap = new CourseLap(course);
			courseLap.setStartTime(lap.getStartTime());
			courseLap.setEndTime(lap.getEndTime());
			courseLap.setTrigger(lap.getTrigger());
			courseLap.setEvent(lap.getEvent());
			courseLap.setEventType(lap.getEventType());
			courseLap.setEventGroup(lap.getEventGroup());
			courseLap.setStartLatitude(lap.getStartLatitude());
			courseLap.setStartLongitude(lap.getStartLongitude());
			courseLap.setStartAltitude(lap.getStartAltitude());
			courseLap.setEndLatitude(lap.getEndLatitude());
			courseLap.setEndLongitude(lap.getEndLongitude());
			courseLap.setEndAltitude(lap.getEndAltitude());
			courseLap.setSport(lap.getSport());
			courseLap.setSubSport(lap.getSubSport());
			courseLap.setElapsedTime(lap.getElapsedTime());
			courseLap.setTimerTime(lap.getTimerTime());
			courseLap.setMovingTime(lap.getMovingTime());
			courseLap.setPausedTime(lap.getPausedTime());
			courseLap.setDistance(lap.getDistance());
			courseLap.setCycles(lap.getCycles());
			courseLap.setStrides(lap.getStrides());
			courseLap.setCalories(lap.getCalories());
			courseLap.setFatCalories(lap.getCalories());
			courseLap.setAverageSpeed(lap.getAverageSpeed());
			courseLap.setAverageMovingSpeed(lap.getAverageMovingSpeed());
			courseLap.setMaximumSpeed(lap.getMaximumSpeed());
			courseLap.setAverageHeartRate(lap.getAverageHeartRate());
			courseLap.setMinimumHeartRate(lap.getMinimumHeartRate());
			courseLap.setMaximumHeartRate(lap.getMaximumHeartRate());
			courseLap.setAverageCadence(lap.getAverageCadence());
			courseLap.setMaximumCadence(lap.getMaximumCadence());
			courseLap.setAverageRunningCadence(lap.getAverageRunningCadence());
			courseLap.setMaximumRunningCadence(lap.getMaximumRunningCadence());
			courseLap.setAveragePower(lap.getAveragePower());
			courseLap.setMaximumPower(lap.getMaximumPower());
			courseLap.setTotalAscent(lap.getTotalAscent());
			courseLap.setTotalDescent(lap.getTotalDescent());
			courseLap.setIntensity(lap.getIntensity());
			courseLap.setNormalizedPower(lap.getNormalizedPower());
			courseLap.setLeftRightBalance(lap.getLeftRightBalance());
			courseLap.setWork(lap.getWork());
			courseLap.setAverageStrokeDistance(lap.getAverageStrokeDistance());
			courseLap.setSwimStroke(lap.getSwimStroke());
			courseLap.setAverageAltitude(lap.getAverageAltitude());
			courseLap.setMinimumAltitude(lap.getMinimumAltitude());
			courseLap.setMaximumAltitude(lap.getMaximumAltitude());
			courseLap.setAverageGrade(lap.getAverageGrade());
			courseLap.setAveragePositiveGrade(lap.getAveragePositiveGrade());
			courseLap.setAverageNegativeGrade(lap.getAverageNegativeGrade());
			courseLap.setMaximumPositiveGrade(lap.getMaximumPositiveGrade());
			courseLap.setMaximumNegativeGrade(lap.getMaximumNegativeGrade());
			courseLap.setAverageTemperature(lap.getAverageTemperature());
			courseLap.setMinimumTemperature(lap.getMinimumTemperature());
			courseLap.setMaximumTemperature(lap.getMaximumTemperature());
			courseLap.setAveragePositiveVerticalSpeed(lap.getAveragePositiveVerticalSpeed());
			courseLap.setAverageNegativeVerticalSpeed(lap.getAverageNegativeVerticalSpeed());
			courseLap.setMaximumPositiveVerticalSpeed(lap.getMaximumPositiveVerticalSpeed());
			courseLap.setMaximumNegativeVerticalSpeed(lap.getMaximumNegativeVerticalSpeed());
			courseLap.setTimeInSpeedZone(lap.getTimeInSpeedZone());
			courseLap.setTimeInHeartRateZone(lap.getTimeInHeartRateZone());
			courseLap.setTimeInCadenceZone(lap.getTimeInCadenceZone());
			courseLap.setTimeInPowerZone(lap.getTimeInPowerZone());
			courseLap.setGpsAccuracy(lap.getGpsAccuracy());
			courseLap.setFirstLengthIndex(lap.getFirstLengthIndex());
			courseLap.setNumberOfLengths(lap.getNumberOfLengths());
			courseLap.setNumberOfActiveLengths(lap.getNumberOfActiveLengths());
			courseLap.setNotes(lap.getNotes());
			
			course.add(courseLap);
		}
	}
	
	private void addTracks() {
		for (Track track : activity.getTracks()) {
			CourseTrack courseTrack = new CourseTrack(course);
			courseTrack.setStartTime(track.getStartTime());
			courseTrack.setEndTime(track.getEndTime());
			
			course.add(courseTrack);
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
