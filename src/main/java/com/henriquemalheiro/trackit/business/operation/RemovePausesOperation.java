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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.log4j.Logger;

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
	private Double speedThreshold;
	private Map<Lap, Pair<Trackpoint, Trackpoint>> lapInfo;
	private Map<CoursePoint, Trackpoint> coursePointInfo;

	public RemovePausesOperation(Course course) {
		Objects.requireNonNull(course);
		this.course = course;
		speedThreshold = Constants.PAUSE_SPEED_THRESHOLD;
	}

	public void execute() throws TrackItException {
		storeLapInfo();
		storeCoursePointInfo();
		removePauses();
		updateLapInfo();
		updateCoursePointInfo();
		consolidate();
	}

	private void removePauses() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		if (trackpoints.isEmpty()) {
			return;
		}

		Trackpoint previousTrkpt = trackpoints.get(0);
		Trackpoint currentTrkpt = null;

		final String utcTimeZoneCode = "UTC";
		Calendar currentTimestamp = Calendar.getInstance();
		currentTimestamp.setTimeZone(TimeZone.getTimeZone(utcTimeZoneCode));
		currentTimestamp.setTime(previousTrkpt.getTimestamp());

		double offset = 0.0;
		double timeDiff = 0.0;
		double timeFromPrevious = 0.0;
		double speed = 0.0;

		for (int i = 1; i < trackpoints.size(); i++) {
			currentTrkpt = trackpoints.get(i);
			timeDiff = calculateTimeDifference(previousTrkpt, currentTrkpt, offset);
			speed = calculateSpeed(currentTrkpt, timeDiff);

			if (speed < speedThreshold) {
				debugPauseRemoved(previousTrkpt, timeDiff);
				timeFromPrevious = 0.0;
				offset += (timeDiff - timeFromPrevious);
				timeDiff = timeFromPrevious;
			}

			currentTimestamp.add(Calendar.MILLISECOND, (int) timeDiff);
			currentTrkpt.setTimestamp(currentTimestamp.getTime());
			currentTrkpt.setTimeFromPrevious(calculateTime(previousTrkpt, currentTrkpt));
			previousTrkpt = currentTrkpt;
		}
	}

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
		logger.debug(String.format("Pausa de %.1f segundo(s) em %s.", (timeDiff / 1000.0), Formatters
		        .getSimpleDateFormatMilis().format(previousTrkpt.getTimestamp())));
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
