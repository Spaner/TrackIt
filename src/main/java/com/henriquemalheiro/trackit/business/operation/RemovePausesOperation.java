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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class RemovePausesOperation {
	private static Logger logger = Logger.getLogger(RemovePausesOperation.class.getName());
	private Course course;
	private Trackpoint trackpoint;
	private long pausedTime;
	private Double speedThreshold;
	private Double pointSpeed;
	private Map<Lap, Pair<Trackpoint, Trackpoint>> lapInfo;
	private Map<CoursePoint, Trackpoint> coursePointInfo;
	private Double firstTrackpointSpeed;
	private Double secondTrackpointSpeed;

	public RemovePausesOperation(Course course) {
		Objects.requireNonNull(course);
		this.course = course;
		//speedThreshold = Constants.PAUSE_SPEED_THRESHOLD;
		speedThreshold = getSpeedThreshold();
	}
	
	private Double getSpeedThreshold() {
		return TrackIt.getPreferences().getDoublePreference(
				Constants.PrefsCategories.PAUSE, null,
				Constants.PausePreferences.SPEED_THRESHOLD, 0);
	}

	// addPause
	public RemovePausesOperation(Course course, Trackpoint trackpoint, long pausedTime, Double firstTrackpointSpeed, Double secondTrackpointSpeed) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		this.course = course;
		this.trackpoint = trackpoint;
		this.pausedTime = pausedTime;
		this.firstTrackpointSpeed = firstTrackpointSpeed;
		this.secondTrackpointSpeed = secondTrackpointSpeed;
		speedThreshold = getSpeedThreshold();
	}

	// removePause
	public RemovePausesOperation(Course course, Trackpoint trackpoint, Double pointSpeed) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		this.course = course;
		this.trackpoint = trackpoint;
		speedThreshold = getSpeedThreshold();
		this.pointSpeed = pointSpeed;
	}

	public void execute() throws TrackItException {
		storeInfo();
		removePauses();
		updateAndConsolidate();
	}

	private void updateAndConsolidate() {
		updateLapInfo();
		updateCoursePointInfo();
		consolidate();
	}

	private void storeInfo() {
		storeLapInfo();
		storeCoursePointInfo();
	}

	public void executeRemovePause() throws TrackItException {
		storeInfo();
		removePause();
		updateAndConsolidate();
	}

	public void executeAddPause() throws TrackItException {
		storeInfo();
		addPause();
		updateAndConsolidate();
	}

	private void addPause() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		if (trackpoints.isEmpty()) {
			return;
		}
		int currentTrackpointIndex = trackpoints.indexOf(trackpoint);
		Trackpoint endPause = trackpoint.clone();
		if(firstTrackpointSpeed != null && secondTrackpointSpeed != null){
			trackpoint.setSpeed(firstTrackpointSpeed);
			endPause.setSpeed(secondTrackpointSpeed);
		}
		else{
			trackpoint.setSpeed(0.0);
			endPause.setSpeed(0.0);
		}
		Date pausedDate;
		trackpoints.add(currentTrackpointIndex + 1, endPause);
		currentTrackpointIndex += 1;

		Trackpoint previousTrackpoint = trackpoints.get(currentTrackpointIndex);
		Trackpoint currentTrackpoint = null;
		
		for (int i = currentTrackpointIndex; i < trackpoints.size(); i++) {
			currentTrackpoint = trackpoints.get(i);
			long time = currentTrackpoint.getTimestamp().getTime();
			long time2 = pausedTime + time;
			Date testDate = new Date(time);
			Date testDate2 = new Date(pausedTime);
			pausedDate = new Date(pausedTime + currentTrackpoint.getTimestamp().getTime());

			currentTrackpoint.setTimestamp(pausedDate);
			if (i == currentTrackpointIndex) {
				previousTrackpoint = trackpoints.get(currentTrackpointIndex - 1);
			}
			currentTrackpoint.setTimeFromPrevious(calculateTime(previousTrackpoint, currentTrackpoint));
			previousTrackpoint = currentTrackpoint;
		}

	}

	private void removePause() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		if (trackpoints.isEmpty() || trackpoints.size() == 1 || trackpoints.indexOf(trackpoint) == trackpoints.size()-1) {
			return;
		}

		int currentTrackpointIndex = trackpoints.indexOf(trackpoint);
		Trackpoint previousTrkpt = trackpoints.get(currentTrackpointIndex);
		Trackpoint currentTrkpt = trackpoints.get(currentTrackpointIndex+1);
		
		
		double offset = 0.0;
		double timeDiff = 0.0;
		double speed = 0.0;
		//timeDiff = calculateTimeDifference(previousTrkpt, currentTrkpt, offset);
		//speed = calculateSpeed(currentTrkpt, timeDiff);
		if (currentTrkpt.getSpeed()>speedThreshold && previousTrkpt.getSpeed() >speedThreshold){
			return;
		}
		currentTrkpt = null;
		
		boolean pauseRemoved = false;
		
		final String utcTimeZoneCode = "Europe/London";
		Calendar currentTimestamp = Calendar.getInstance();
		currentTimestamp.setTimeZone(TimeZone.getTimeZone(utcTimeZoneCode));
		currentTimestamp.setTime(previousTrkpt.getTimestamp());

		offset = 0.0;
		timeDiff = 0.0;
		double timeFromPrevious = 0.0;
		speed = 0.0;

		for (int i = currentTrackpointIndex+1; i < trackpoints.size(); i++) {
			currentTrkpt = trackpoints.get(i);
			
			timeDiff = calculateTimeDifference(previousTrkpt, currentTrkpt, offset);
			speed = calculateSpeed(currentTrkpt, timeDiff);
			Double speed2 = currentTrkpt.getSpeed();
			Double speed3 = previousTrkpt.getSpeed();

			if (speed2<=speedThreshold && speed3 <= speedThreshold && !pauseRemoved) {
				debugPauseRemoved(previousTrkpt, timeDiff);
				timeFromPrevious = 0.0;
				offset += (timeDiff - timeFromPrevious);
				timeDiff = timeFromPrevious;
				pauseRemoved = true;
				//currentTrkpt.setSpeed(speed);
			}
			
			currentTimestamp.add(Calendar.MILLISECOND, (int) timeDiff);
			currentTrkpt.setTimestamp(currentTimestamp.getTime());
			currentTrkpt.setTimeFromPrevious(calculateTime(previousTrkpt, currentTrkpt));
			previousTrkpt = currentTrkpt;
		}
		trackpoints.remove(currentTrackpointIndex+1);
		if(currentTrackpointIndex > 0){
			offset = 0.0;
			previousTrkpt = trackpoints.get(currentTrackpointIndex-1);
			currentTrkpt = trackpoints.get(currentTrackpointIndex);
			timeDiff = calculateTimeDifference(previousTrkpt, currentTrkpt, offset);
			speed = calculateSpeed(currentTrkpt, timeDiff);
			if(pointSpeed != null){
				currentTrkpt.setSpeed(pointSpeed);
			}
			else{
				currentTrkpt.setSpeed(speed);
			}
			currentTrkpt.setTimeFromPrevious(calculateTime(previousTrkpt, currentTrkpt));
		}
		
	}
	
	private void removePauses() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		for (int i = 0; i < trackpoints.size(); i++) {
			trackpoint = trackpoints.get(i);
			removePause();
			}
	}
	
	
	/*private void removePauses222() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		if (trackpoints.isEmpty()) {
			return;
		}

		Trackpoint previousTrkpt = trackpoints.get(0);
		Trackpoint currentTrkpt = null;

		final String utcTimeZoneCode = "Europe/London";
		Calendar currentTimestamp = Calendar.getInstance();
		currentTimestamp.setTimeZone(TimeZone.getTimeZone(utcTimeZoneCode));
		currentTimestamp.setTime(previousTrkpt.getTimestamp());

		double offset = 0.0;
		double timeDiff = 0.0;
		double timeFromPrevious = 0.0;
		double speed = 0.0;
		int size = trackpoints.size();
		for (int i = 1; i < size; i++) {
			currentTrkpt = trackpoints.get(i);
			timeDiff = calculateTimeDifference(previousTrkpt, currentTrkpt, offset);
			speed = calculateSpeed(currentTrkpt, timeDiff);

			if (speed < speedThreshold) {
				debugPauseRemoved(previousTrkpt, timeDiff);
				timeFromPrevious = 0.0;
				offset += (timeDiff - timeFromPrevious);
				timeDiff = timeFromPrevious;
				trackpoints.remove(i);
				size--;
			}

			currentTimestamp.add(Calendar.MILLISECOND, (int) timeDiff);
			currentTrkpt.setTimestamp(currentTimestamp.getTime());
			currentTrkpt.setTimeFromPrevious(calculateTime(previousTrkpt, currentTrkpt));
			previousTrkpt = currentTrkpt;
		}
		offset = 0.0;
		timeDiff = 0.0;
		timeFromPrevious = 0.0;
		speed = 0.0;
		for (int i = 1; i < size; i++) {
			previousTrkpt = trackpoints.get(i-1);
			currentTrkpt = trackpoints.get(i);
			currentTrkpt.setTimeFromPrevious(calculateTime(previousTrkpt, currentTrkpt));
			timeDiff = calculateTimeDifference(previousTrkpt, currentTrkpt, offset);
			speed = calculateSpeed(currentTrkpt, timeDiff);
			Double speedValue = speed;
			Double speedTest = speed * (3600/1000);
			currentTrkpt.setSpeed(speed);
			
		}
	}*/

	private double calculateTimeDifference(Trackpoint previousTrkpt, Trackpoint currentTrkpt, double offset) {
		return currentTrkpt.getTimestamp().getTime() - (previousTrkpt.getTimestamp().getTime() + offset);
	}

	private double calculateSpeed(Trackpoint currentTrkpt, double timeDiff) {
		return currentTrkpt.getDistanceFromPrevious() / (timeDiff / 1000.0);
	}

	private double calculateTime(Trackpoint previousTrkpt, Trackpoint currentTrkpt) {
		return (currentTrkpt.getTimestamp().getTime() - previousTrkpt.getTimestamp().getTime()) / 1000.0;
	}

	private void debugPauseRemoved(Trackpoint previousTrkpt, double timeDiff) {
		logger.debug(String.format("Pausa de %.1f segundo(s) em %s.", (timeDiff / 1000.0),
				Formatters.getSimpleDateFormatMilis().format(previousTrkpt.getTimestamp())));
	}

	private void storeLapInfo() {
		lapInfo = new HashMap<>();
		for (Lap lap : course.getLaps()) {
			lapInfo.put(lap, Pair.create(lap.getFirstTrackpoint(), lap.getLastTrackpoint()));
		}
	}

	private void updateLapInfo() {
		for (Lap lap : course.getLaps()) {
			lap.setStartTime(lapInfo.get(lap).getFirst().getTimestamp());
			lap.setEndTime(lapInfo.get(lap).getSecond().getTimestamp());
		}
	}

	private void storeCoursePointInfo() {
		coursePointInfo = new HashMap<>();
		for (CoursePoint coursePoint : course.getCoursePoints()) {
			coursePointInfo.put(coursePoint, coursePoint.getTrackpoint());
		}
	}

	private void updateCoursePointInfo() {
		for (CoursePoint coursePoint : course.getCoursePoints()) {
			coursePoint.setTime(coursePointInfo.get(coursePoint).getTimestamp());
			coursePoint.setDistance(coursePointInfo.get(coursePoint).getDistance());
		}
	}

	private void consolidate() {
		for (Lap lap : course.getLaps()) {
			lap.consolidate(ConsolidationLevel.SUMMARY);
		}
		course.consolidate(ConsolidationLevel.SUMMARY);
	}
}
