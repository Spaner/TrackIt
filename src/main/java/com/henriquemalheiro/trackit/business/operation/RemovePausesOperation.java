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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.operation.PauseDetectionPicCaseOperation;

public class RemovePausesOperation {
	private static Logger logger = Logger.getLogger(RemovePausesOperation.class.getName());
	private Course course;
	private Trackpoint trackpoint;
	private Trackpoint finalPauseTrackpoint;
	private long pausedTime;
	private Double speedThreshold;
	private Double pointSpeed;
	private Map<Lap, Pair<Trackpoint, Trackpoint>> lapInfo;
	private Map<CoursePoint, Trackpoint> coursePointInfo;
	private Double firstTrackpointSpeed;
	private Double secondTrackpointSpeed;
	private List<Trackpoint> removedPoints;
	private Trackpoint leftEdge;
	private Trackpoint rightEdge;
	private List<PauseInformation> removedPauses;
	private boolean removeAllPauses;
	private double defaultSpeed;
	// private List<Lap> laps;

	private Trackpoint startPausePoint;
	private Trackpoint endPausePoint;
	private double oldPauseDuration;

	public static class PauseInformation {

		private List<Trackpoint> removedPoints;
		private Trackpoint leftEdge;
		private Trackpoint rightEdge;
		private long courseId;
		private long pauseTime;

		public PauseInformation(List<Trackpoint> removedPoints, Trackpoint leftEdge, Trackpoint rightEdge,
				long courseId, long pauseTime) {
			this.removedPoints = removedPoints;
			this.leftEdge = leftEdge;
			this.rightEdge = rightEdge;
			this.courseId = courseId;
			this.pauseTime = pauseTime;
		}

		public List<Trackpoint> getRemovedPoints() {
			return this.removedPoints;
		}

		public Trackpoint getLeftEdge() {
			return this.leftEdge;
		}

		public Trackpoint getRightEdge() {
			return this.rightEdge;
		}

		public long getPauseTime() {
			return this.pauseTime;
		}

		public long getCourseId() {
			return this.courseId;
		}

	}

	public RemovePausesOperation(Course course) {
		Objects.requireNonNull(course);
		this.course = course;
		// speedThreshold = Constants.PAUSE_SPEED_THRESHOLD;
		this.speedThreshold = getSpeedThreshold();
		this.removedPauses = new ArrayList<PauseInformation>();
		this.removedPoints = new ArrayList<Trackpoint>();
		this.removeAllPauses = false;
	}

	private Double getSpeedThreshold() {
		// return
		// TrackIt.getPreferences().getDoublePreference(Constants.PrefsCategories.PAUSE,
		// null,
		// Constants.PausePreferences.SPEED_THRESHOLD, 0);

		SportType sport = course.getSport();
		SubSportType subSport = course.getSubSport();

		double limit = DocumentManager.getInstance().getDatabase().getPauseThresholdSpeed(sport, subSport, false);
		return limit;
	}

	public Double getPointSpeed() {
		return pointSpeed;
	}

	public Trackpoint getFinalPauseTrackpoint() {
		return finalPauseTrackpoint;
	}

	public Trackpoint getLeftEdge() {
		return leftEdge;
	}

	public Trackpoint getRightEdge() {
		return rightEdge;
	}

	public List<Trackpoint> getRemovedTrackpoints() {
		return removedPoints;
	}

	public List<PauseInformation> getRemovedPauses() {
		return removedPauses;
	}

	public long getPausedTime() {
		return pausedTime;
	}

	public double getOldPauseDuration() {
		return oldPauseDuration;
	}

	public RemovePausesOperation() {

	}

	// addPause
	public RemovePausesOperation(Course course, Trackpoint trackpoint, long pausedTime) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		this.course = course;
		this.trackpoint = trackpoint;
		this.pausedTime = pausedTime;
		speedThreshold = getSpeedThreshold();
	}

	// removePause
	public RemovePausesOperation(Course course, Trackpoint trackpoint, Double pointSpeed) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		this.course = course;
		this.trackpoint = trackpoint;
		this.speedThreshold = getSpeedThreshold();
		this.pointSpeed = pointSpeed;
		this.removedPauses = new ArrayList<PauseInformation>();
	}

	public void undoAddPauseSetup(Course course, Trackpoint startPausePoint, Trackpoint endpausePoint, long pausedTime,
			Double previousSpeed) {
		this.course = course;
		this.startPausePoint = startPausePoint;
		this.endPausePoint = endpausePoint;
		this.pausedTime = pausedTime;
		this.pointSpeed = previousSpeed;
	}

	public void undoRemovePauseSetup(Course course, List<Trackpoint> removedTrackpoints, Trackpoint leftPoint,
			Trackpoint rightPoint, long pausedTime) {
		this.course = course;
		this.leftEdge = leftPoint;
		this.rightEdge = rightPoint;
		this.removedPoints = removedTrackpoints;
		this.pausedTime = pausedTime;
		// this.laps = laps;
	}

	public void undoRemoveAllPausesSetup(Course course, List<Trackpoint> removedTrackpoints, Trackpoint leftPoint,
			Trackpoint rightPoint, long pausedTime) {
		this.course = course;
		this.leftEdge = leftPoint;
		this.rightEdge = rightPoint;
		this.removedPoints = removedTrackpoints;
		this.pausedTime = pausedTime;
	}

	public void undoAddPause() {
		boolean setTimes = false;
		List<Trackpoint> newTrackpoints = new ArrayList<Trackpoint>();
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			if (trackpoint.getId() == startPausePoint.getId()) {
				trackpoint.setSpeed(pointSpeed);
			}
			if (!(trackpoint.getId() == endPausePoint.getId())) {

				if (setTimes) {
					long newTimestamp = trackpoint.getTimestamp().getTime() - pausedTime;
					trackpoint.setTimestamp(new Date(newTimestamp));
				}
				newTrackpoints.add(trackpoint);
			} else {
				setTimes = true;
			}

		}
		course.setTrackpoints(newTrackpoints);
	}

	public void undoRemovePause() {
		List<Trackpoint> newTrackpoints = new ArrayList<Trackpoint>();
		boolean setTimes = false;
		// course.setLaps(laps);
		/*Trackpoint last = course.getTrackpoints().get(course.getTrackpoints().size()-1);
		if (last.getId() == leftEdge.getId() && last.getId() == rightEdge.getId()) {
			rightEdge = removedPoints.get(removedPoints.size()-1);
			course.getTrackpoints().add(rightEdge);
		}*/
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			if (trackpoint.getId() == leftEdge.getId()) {
				leftEdge.copyData(trackpoint);

				newTrackpoints.add(trackpoint);
				newTrackpoints.addAll(removedPoints);
				setTimes = true;
			} else if (trackpoint.getId() == rightEdge.getId()) {
				if (setTimes) {
					// long newTimestamp = rightPoint.getTimestamp().getTime() +
					// pausedTime;
					// Date newDate = new Date(newTimestamp);
					// rightPoint.setTimestamp(newDate);
					rightEdge.copyData(trackpoint);

					newTrackpoints.add(trackpoint);
				}

			} else {
				if (setTimes) {
					long newTimestamp = trackpoint.getTimestamp().getTime() + pausedTime;
					trackpoint.setTimestamp(new Date(newTimestamp));
				}
				newTrackpoints.add(trackpoint);
			}
		}
		Trackpoint previousTrackpoint = newTrackpoints.get(0);
		Trackpoint currentTrackpoint = null;
		for (int i = 1; i < newTrackpoints.size(); i++) {
			currentTrackpoint = newTrackpoints.get(i);
			double timeDifference = calculateTimeDifference(previousTrackpoint, currentTrackpoint);
			currentTrackpoint.setTimeFromPrevious(timeDifference);
			previousTrackpoint = currentTrackpoint;
		}
		course.setTrackpoints(newTrackpoints);

	}

	public void undoRemoveAllPauses() {
		List<Trackpoint> newTrackpoints = new ArrayList<Trackpoint>();
		boolean setTimes = false;
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			if (trackpoint.getId() == leftEdge.getId()) {
				newTrackpoints.add(leftEdge);
				newTrackpoints.addAll(removedPoints);
				setTimes = true;
			} else if (trackpoint.getId() == rightEdge.getId()) {
				if (setTimes) {
					long newTimestamp = rightEdge.getTimestamp().getTime() + pausedTime;
					Date newDate = new Date(newTimestamp);
					rightEdge.setTimestamp(newDate);
					newTrackpoints.add(rightEdge);
				}

			} else {
				if (setTimes) {
					long newTimestamp = trackpoint.getTimestamp().getTime() + pausedTime;
					trackpoint.setTimestamp(new Date(newTimestamp));
				}
				newTrackpoints.add(trackpoint);
			}
		}
		Trackpoint previousTrackpoint = newTrackpoints.get(0);
		Trackpoint currentTrackpoint = null;
		for (int i = 1; i < newTrackpoints.size(); i++) {
			currentTrackpoint = newTrackpoints.get(i);
			double timeDifference = calculateTimeDifference(previousTrackpoint, currentTrackpoint);
			currentTrackpoint.setTimeFromPrevious(timeDifference);
			previousTrackpoint = currentTrackpoint;
		}

		course.setTrackpoints(newTrackpoints);
	}

	public void executeUndoAddPause() throws TrackItException

	{
		storeInfo();
		undoAddPause();
		updateAndConsolidate();
	}

	public void executeUndoRemovePause() throws TrackItException

	{
		storeInfo();
		undoRemovePause();
		updateAndConsolidateUndoRemove();
		
	}

	public void executeUndoRemoveAllPauses() throws TrackItException

	{
		storeInfo();
		undoRemoveAllPauses();
		updateAndConsolidateUndoRemove();
	}

	public void execute() throws TrackItException {
		storeInfo();
		removePauses();
		updateAndConsolidate();
	}

	private void updateAndConsolidate() {
		updateLapInfo();
		updateCoursePointInfo();
		// copyLaps();
		consolidate();
	}
	
	private void updateAndConsolidateUndoRemove() {
		updateLapInfo();
		updateCoursePointInfo();
		// copyLaps();
		consolidateRecalc();
	}

	/*
	 * public List<Lap> getLaps(){ return laps; }
	 * 
	 * private void copyLaps(){ laps = new ArrayList<Lap>(); for(Lap lap :
	 * course.getLaps()){ CourseLap courseLap = new CourseLap(course);
	 * courseLap.setStartTime(lap.getStartTime());
	 * courseLap.setEndTime(lap.getEndTime());
	 * courseLap.setTrigger(lap.getTrigger());
	 * courseLap.setEvent(lap.getEvent());
	 * courseLap.setEventType(lap.getEventType());
	 * courseLap.setEventGroup(lap.getEventGroup());
	 * courseLap.setStartLatitude(lap.getStartLatitude());
	 * courseLap.setStartLongitude(lap.getStartLongitude());
	 * courseLap.setStartAltitude(lap.getStartAltitude());
	 * courseLap.setEndLatitude(lap.getEndLatitude());
	 * courseLap.setEndLongitude(lap.getEndLongitude());
	 * courseLap.setEndAltitude(lap.getEndAltitude());
	 * courseLap.setSport(lap.getSport());
	 * courseLap.setSubSport(lap.getSubSport());
	 * courseLap.setElapsedTime(lap.getElapsedTime());
	 * courseLap.setTimerTime(lap.getTimerTime());
	 * courseLap.setMovingTime(lap.getMovingTime());
	 * courseLap.setPausedTime(lap.getPausedTime());
	 * courseLap.setDistance(lap.getDistance());
	 * courseLap.setCycles(lap.getCycles());
	 * courseLap.setStrides(lap.getStrides());
	 * courseLap.setCalories(lap.getCalories());
	 * courseLap.setFatCalories(lap.getCalories());
	 * courseLap.setAverageSpeed(lap.getAverageSpeed());
	 * courseLap.setAverageMovingSpeed(lap.getAverageMovingSpeed());
	 * courseLap.setMaximumSpeed(lap.getMaximumSpeed());
	 * courseLap.setAverageHeartRate(lap.getAverageHeartRate());
	 * courseLap.setMinimumHeartRate(lap.getMinimumHeartRate());
	 * courseLap.setMaximumHeartRate(lap.getMaximumHeartRate());
	 * courseLap.setAverageCadence(lap.getAverageCadence());
	 * courseLap.setMaximumCadence(lap.getMaximumCadence());
	 * courseLap.setAverageRunningCadence(lap.getAverageRunningCadence());
	 * courseLap.setMaximumRunningCadence(lap.getMaximumRunningCadence());
	 * courseLap.setAveragePower(lap.getAveragePower());
	 * courseLap.setMaximumPower(lap.getMaximumPower());
	 * courseLap.setTotalAscent(lap.getTotalAscent());
	 * courseLap.setTotalDescent(lap.getTotalDescent());
	 * courseLap.setIntensity(lap.getIntensity());
	 * courseLap.setNormalizedPower(lap.getNormalizedPower());
	 * courseLap.setLeftRightBalance(lap.getLeftRightBalance());
	 * courseLap.setWork(lap.getWork());
	 * courseLap.setAverageStrokeDistance(lap.getAverageStrokeDistance());
	 * courseLap.setSwimStroke(lap.getSwimStroke());
	 * courseLap.setAverageAltitude(lap.getAverageAltitude());
	 * courseLap.setMinimumAltitude(lap.getMinimumAltitude());
	 * courseLap.setMaximumAltitude(lap.getMaximumAltitude());
	 * courseLap.setAltitudeDifference(lap.getAltitudeDifference());
	 * courseLap.setAverageGrade(lap.getAverageGrade());
	 * courseLap.setAveragePositiveGrade(lap.getAveragePositiveGrade());
	 * courseLap.setAverageNegativeGrade(lap.getAverageNegativeGrade());
	 * courseLap.setMaximumPositiveGrade(lap.getMaximumPositiveGrade());
	 * courseLap.setMaximumNegativeGrade(lap.getMaximumNegativeGrade());
	 * courseLap.setAverageTemperature(lap.getAverageTemperature());
	 * courseLap.setMinimumTemperature(lap.getMinimumTemperature());
	 * courseLap.setMaximumTemperature(lap.getMaximumTemperature());
	 * courseLap.setAveragePositiveVerticalSpeed(lap.
	 * getAveragePositiveVerticalSpeed());
	 * courseLap.setAverageNegativeVerticalSpeed(lap.
	 * getAverageNegativeVerticalSpeed());
	 * courseLap.setMaximumPositiveVerticalSpeed(lap.
	 * getMaximumPositiveVerticalSpeed());
	 * courseLap.setMaximumNegativeVerticalSpeed(lap.
	 * getMaximumNegativeVerticalSpeed());
	 * courseLap.setTimeInSpeedZone(lap.getTimeInSpeedZone());
	 * courseLap.setTimeInHeartRateZone(lap.getTimeInHeartRateZone());
	 * courseLap.setTimeInCadenceZone(lap.getTimeInCadenceZone());
	 * courseLap.setTimeInPowerZone(lap.getTimeInPowerZone());
	 * courseLap.setNortheastLatitude(lap.getNortheastLatitude());
	 * courseLap.setNortheastLongitude(lap.getNortheastLongitude());
	 * courseLap.setSouthwestLatitude(lap.getSouthwestLatitude());
	 * courseLap.setSouthwestLongitude(lap.getSouthwestLongitude());
	 * courseLap.setGpsAccuracy(lap.getGpsAccuracy());
	 * courseLap.setFirstLengthIndex(lap.getFirstLengthIndex());
	 * courseLap.setNumberOfLengths(lap.getNumberOfLengths());
	 * courseLap.setNumberOfActiveLengths(lap.getNumberOfActiveLengths());
	 * courseLap.setNotes(lap.getNotes());
	 * courseLap.setFirstTrack(lap.getFirstTrack());
	 * courseLap.setLastTrack(lap.getLastTrack());
	 * courseLap.setPauses(lap.getPauses()); laps.add(courseLap); } }
	 */

	private void storeInfo() {
		this.defaultSpeed = this.course.getSubSport().getDefaultAverageSpeed();
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

	public void executeChangePauseDuration() throws TrackItException {
		storeInfo();
		changePauseDuration();
		updateAndConsolidate();
	}

	private double calculateDistance(Trackpoint trkpt1, Trackpoint trkpt2) {
		return (trkpt1.getLongitude() != null && trkpt1.getLatitude() != null && trkpt2.getLongitude() != null
				&& trkpt2.getLatitude() != null
						? Utilities.getGreatCircleDistance(trkpt1.getLatitude(), trkpt1.getLongitude(),
								trkpt2.getLatitude(), trkpt2.getLongitude()) * 1000
						: 0.0);
	}

	private double calculateTimeDifference(Trackpoint leftTrackpoint, Trackpoint rightTrackpoint) {
		return (rightTrackpoint.getTimestamp().getTime() - leftTrackpoint.getTimestamp().getTime()) / 1000.0;
	}

	private void addPause() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		if (trackpoints.isEmpty()) {
			return;
		}
		int currentTrackpointIndex = trackpoints.indexOf(trackpoint);
		Trackpoint endPause = trackpoint.clone();
		Double oldSpeed = trackpoint.getSpeed();
		if (firstTrackpointSpeed != null && secondTrackpointSpeed != null) {
			trackpoint.setSpeed(firstTrackpointSpeed);
			endPause.setSpeed(secondTrackpointSpeed);
		} else {
			trackpoint.setSpeed(0.0);
			endPause.setSpeed(0.0);
		}
		Date pausedDate;
		trackpoints.add(currentTrackpointIndex + 1, endPause);
		currentTrackpointIndex += 1;

		Trackpoint previousTrackpoint = trackpoints.get(currentTrackpointIndex);
		Trackpoint currentTrackpoint = null;

		final String utcTimeZoneCode = "UTC";
		Calendar currentTimestamp = Calendar.getInstance();
		currentTimestamp.setTimeZone(TimeZone.getTimeZone(utcTimeZoneCode));

		for (int i = currentTrackpointIndex; i < trackpoints.size(); i++) {
			currentTrackpoint = trackpoints.get(i);
			// long time = currentTrackpoint.getTimestamp().getTime();
			// long time2 = pausedTime + time;
			// Date testDate = new Date(time);
			// Date testDate2 = new Date(pausedTime);
			// pausedDate = new Date(pausedTime +
			// currentTrackpoint.getTimestamp().getTime());
			double date = pausedTime / 1000 / 3600;
			currentTimestamp.setTimeInMillis(currentTrackpoint.getTimestamp().getTime() + pausedTime);
			currentTrackpoint.setTimestamp(currentTimestamp.getTime());

			/*
			 * Date old = new Date(currentTrackpoint.getTimestamp().getTime());
			 * Date newdate = new Date(); newdate.setTime(old.getTime() +
			 * pausedTime); currentTrackpoint.setTimestamp(newdate);
			 */
			// currentTrackpoint.getTimestamp().setTime(currentTrackpoint.getTimestamp().getTime()
			// + pausedTime);
			// currentTrackpoint.setTimestamp(pausedDate);
			if (i == currentTrackpointIndex) {
				previousTrackpoint = trackpoints.get(currentTrackpointIndex - 1);
			}
			currentTrackpoint.setTimeFromPrevious(calculateTime(previousTrackpoint, currentTrackpoint));
			previousTrackpoint = currentTrackpoint;
		}

		this.finalPauseTrackpoint = endPause;
		pointSpeed = oldSpeed;
		course.getPauses().clear();
		new PauseDetectionPicCaseOperation().process(course);
		// undoAddPause(course, trackpoint, endPause, pausedTime, oldSpeed);

	}

	private void changePauseDuration() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		if (trackpoints.isEmpty()) {
			return;
		}
		int currentTrackpointIndex = trackpoints.indexOf(trackpoint);
		Trackpoint lastPauseTrackpoint = trackpoint;
		oldPauseDuration = course.getPause(trackpoint.getTimestamp().getTime()).getDuration() * 1000;
		long pauseDurationDifference = (long) (this.pausedTime - oldPauseDuration);

		for (int i = currentTrackpointIndex; i < trackpoints.size(); i++) {
			if (!course.isInsidePause(trackpoints.get(i).getTimestamp().getTime())) {
				lastPauseTrackpoint.setTimeFromPrevious(
						lastPauseTrackpoint.getTimeFromPrevious() + (pauseDurationDifference / 1000));

				break;
			}
			lastPauseTrackpoint = trackpoints.get(i);
		}
		currentTrackpointIndex = trackpoints.indexOf(lastPauseTrackpoint);

		for (int i = currentTrackpointIndex; i < trackpoints.size(); i++) {
			Trackpoint currentTrackpoint = trackpoints.get(i);
			currentTrackpoint
					.setTimestamp(new Date(currentTrackpoint.getTimestamp().getTime() + pauseDurationDifference));
		}

		course.getPauses().clear();
		new PauseDetectionPicCaseOperation().process(course);
		// undoAddPause(course, trackpoint, endPause, pausedTime, oldSpeed);

	}

	private void removePause() {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		this.removedPoints = new ArrayList<Trackpoint>();
		if (trackpoints.isEmpty() || trackpoints.size() == 1) {
			return;
		}
		if (!removeAllPauses) {
			new PauseDetectionPicCaseOperation().process(course);
		}
		if (!course.isInsidePause(trackpoint.getTimestamp().getTime())) {
			return;
		}
		Pause pause = course.getPause(trackpoint.getTimestamp().getTime());

		long pauseStartTime = pause.getStart().getTime();
		long pauseEndTime = pause.getEnd().getTime();

		List<Trackpoint> newTrackpoints = new ArrayList<Trackpoint>();

		for (int i = 0; i < trackpoints.size(); i++) {
			long trackpointTime = trackpoints.get(i).getTimestamp().getTime();
			if (trackpointTime >= pauseStartTime && trackpointTime <= pauseEndTime) {
				newTrackpoints.add(trackpoints.get(i));
			}
		}

		Trackpoint firstInsidePause = newTrackpoints.get(0);
		Trackpoint lastInsidePause = newTrackpoints.get(newTrackpoints.size() - 1);
		long pausedTime = lastInsidePause.getTimestamp().getTime() - firstInsidePause.getTimestamp().getTime();
		this.pausedTime = pausedTime;
		Trackpoint currentTrackpoint = null;

		newTrackpoints.clear();
		Trackpoint previousTrackpoint = trackpoints.get(0);
		boolean leftPoint = false;
		boolean rightPoint = false;
		for (int i = 0; i < trackpoints.size(); i++) {
			currentTrackpoint = trackpoints.get(i);
			long trackpointTime = currentTrackpoint.getTimestamp().getTime();
			if (trackpointTime >= pauseStartTime && trackpointTime <= pauseEndTime) {
				if (!leftPoint) {
					this.leftEdge = previousTrackpoint.clone();
					this.leftEdge.setId(previousTrackpoint.getId());
					// previousTrackpoint.setSpeed(10.0);
					previousTrackpoint.setSpeed(defaultSpeed);
					leftPoint = true;
				}
				this.removedPoints.add(currentTrackpoint);

			} else {
				if (trackpointTime > pauseEndTime && !rightPoint) {
					this.rightEdge = currentTrackpoint.clone();
					this.rightEdge.setId(currentTrackpoint.getId());
					rightPoint = true;
				}
				newTrackpoints.add(currentTrackpoint);
			}
			previousTrackpoint = currentTrackpoint;
		}

		for (int i = 0; i < newTrackpoints.size(); i++) {
			currentTrackpoint = newTrackpoints.get(i);
			long trackpointTime = currentTrackpoint.getTimestamp().getTime();
			if (trackpointTime > pauseEndTime) {
				Date newTimestamp = new Date(trackpointTime - pausedTime);
				newTrackpoints.get(i).setTimestamp(newTimestamp);
			}
		}

		/*
		 * for (int i = 0; i < newTrackpoints.size(); i++) { currentTrackpoint =
		 * newTrackpoints.get(i); Double distance =
		 * calculateDistance(currentTrackpoint, previousTrackpoint); Double
		 * timeDifference = calculateTimeDifference(previousTrackpoint,
		 * currentTrackpoint); currentTrackpoint.setDistance(distance);
		 * currentTrackpoint.setTimeFromPrevious(timeDifference);
		 * previousTrackpoint = currentTrackpoint; }
		 */
		if (rightEdge == null) {
			rightEdge = newTrackpoints.get(newTrackpoints.size() - 1);
		}
		course.setTrackpoints(newTrackpoints);
		PauseInformation removedPause = new PauseInformation(this.removedPoints, this.leftEdge, this.rightEdge,
				this.course.getId(), this.pausedTime);
		this.removedPauses.add(removedPause);
		if (!removeAllPauses) {
			course.consolidate(ConsolidationLevel.SUMMARY);
			course.getPauses().clear();
			new PauseDetectionPicCaseOperation().process(course);
		}

	}

	// private void removePause() {
	// List<Trackpoint> trackpoints = course.getTrackpoints();
	// if (trackpoints.isEmpty() || trackpoints.size() == 1 ||
	// trackpoints.indexOf(trackpoint) == trackpoints.size()-1) {
	// return;
	// }
	// new PauseDetectionPicCaseOperation().process(course);
	// if(!course.isInsidePause(trackpoint.getTimestamp().getTime())){
	// return;
	// }
	// Pause pause = course.getPause(trackpoint.getTimestamp().getTime());
	// Trackpoint leftEdge = null;
	// Trackpoint rightEdge = null;
	// int leftIndex = 0;
	// int rightIndex = 0;
	//
	// if(pause!=null){
	//
	// Trackpoint current;
	// boolean left = false;
	// for (int i = 0; i < trackpoints.size(); i++){
	// current = trackpoints.get(i);
	// if(current.getTimestamp().getTime()>pause.getStart().getTime() && !left){
	// leftEdge = trackpoints.get(i);
	// leftIndex = i;
	// left = true;
	// }
	// if(current.getTimestamp().getTime()<=pause.getEnd().getTime()){
	// rightEdge = trackpoints.get(i);
	// rightIndex = i;
	// }
	// }
	//
	// }
	//
	// Trackpoint newLeft = new Trackpoint(course);
	// Trackpoint newRight = new Trackpoint(course);
	//
	// trackpoints.add(leftIndex, newLeft);
	// rightIndex++;
	// trackpoints.add(rightIndex+1, newRight);
	// int outerLeftIndex = leftIndex-1;
	// int outerRightIndex = rightIndex;
	// Trackpoint outerLeftEdge = trackpoints.get(outerLeftIndex);
	// Trackpoint outerRightEdge = trackpoints.get(outerRightIndex);
	// try {
	// ConsolidationOperation.processTrackpointsData(trackpoints);
	// newLeft.setDistance(calculateDistance(outerLeftEdge, newLeft) +
	// outerLeftEdge.getDistance());
	// newLeft.setDistanceFromPrevious(newLeft.getDistance()-outerLeftEdge.getDistance());
	// newLeft.setTimestamp(pause.getStart());
	// Double timeFromPrevious = calculateTimeDifference(outerLeftEdge,
	// newLeft);
	// newLeft.setTimeFromPrevious(timeFromPrevious);
	//
	// newRight.setDistance(calculateDistance(outerRightEdge, newRight) +
	// outerRightEdge.getDistance());
	// newRight.setDistanceFromPrevious(newRight.getDistance()-outerRightEdge.getDistance());
	// newRight.setTimestamp(pause.getEnd());
	// timeFromPrevious = calculateTimeDifference(outerRightEdge, newRight);
	// newRight.setTimeFromPrevious(timeFromPrevious);
	// } catch (TrackItException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// Trackpoint previousTrackpoint = trackpoints.get(leftIndex);
	// Trackpoint currentTrackpoint = null;
	//
	// double offset = 0.0;
	// double timeDiff = 0.0;
	// double speed = 0.0;
	//
	// final String utcTimeZoneCode = "UTC";
	// Calendar currentTimestamp = Calendar.getInstance();
	// currentTimestamp.setTimeZone(TimeZone.getTimeZone(utcTimeZoneCode));
	// currentTimestamp.setTime(previousTrackpoint.getTimestamp());
	//
	// offset = 0.0;
	// timeDiff = 0.0;
	// double timeFromPrevious = 0.0;
	// speed = 0.0;
	//
	// List<Trackpoint> newTrackpoints = new ArrayList<Trackpoint>();
	//
	// for(int i = 0; i<=leftIndex; i++){
	// currentTrackpoint = trackpoints.get(i);
	// if(!course.insidePause(pause,
	// currentTrackpoint.getTimestamp().getTime())){
	// newTrackpoints.add(currentTrackpoint);
	// }
	// }
	//
	// for(int i = leftIndex+1; i<trackpoints.size(); i++){
	// currentTrackpoint = trackpoints.get(i);
	// timeDiff = calculateTimeDifference(previousTrackpoint, currentTrackpoint,
	// offset);
	// speed = calculateSpeed(currentTrackpoint, timeDiff);
	// //timeDiff = calculateTimeDifference(previousTrackpoint,
	// currentTrackpoint, offset);
	// if(!course.insidePause(pause,
	// currentTrackpoint.getTimestamp().getTime())){
	// newTrackpoints.add(currentTrackpoint);
	//
	// }
	// else{
	// debugPauseRemoved(previousTrackpoint, timeDiff);
	// timeFromPrevious = 0.0;
	// offset += (timeDiff - timeFromPrevious);
	// timeDiff = timeFromPrevious;
	//
	// }
	// currentTimestamp.add(Calendar.MILLISECOND, (int) timeDiff);
	// currentTrackpoint.setTimestamp(currentTimestamp.getTime());
	// currentTrackpoint.setTimeFromPrevious(calculateTime(previousTrackpoint,
	// currentTrackpoint));
	// previousTrackpoint = currentTrackpoint;
	// /*long newTime = currentTrackpoint.getTimestamp().getTime() -
	// (long)pause.getDuration().doubleValue()*1000;
	// Date newDate = new Date(newTime);
	// currentTrackpoint.setTimestamp(newDate);
	// currentTrackpoint.setTimeFromPrevious(calculateTime(previousTrackpoint,
	// currentTrackpoint));
	// previousTrackpoint = currentTrackpoint;*/
	// }
	// course.setTrackpoints(newTrackpoints);
	//
	// GPSDocument document = new GPSDocument(course.getParent().getFileName());
	// document.add(course);
	// Map<String, Object> options = new HashMap<String, Object>();
	// options.put(Constants.ConsolidationOperation.LEVEL,
	// ConsolidationLevel.SUMMARY);
	// try {
	// new ConsolidationOperation(options).process(document);
	// } catch (TrackItException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	//
	//
	// /*offset = 0.0;
	// if(leftIndex > 0){
	// offset = 0.0;
	// previousTrackpoint = trackpoints.get(leftIndex-1);
	// currentTrackpoint = trackpoints.get(leftIndex);
	// timeDiff = calculateTimeDifference(previousTrackpoint, currentTrackpoint,
	// offset);
	// speed = calculateSpeed(currentTrackpoint, timeDiff);
	// if(pointSpeed != null){
	// currentTrackpoint.setSpeed(pointSpeed);
	// }
	// else{
	// currentTrackpoint.setSpeed(speed);
	// }
	// currentTrackpoint.setTimeFromPrevious(calculateTime(previousTrackpoint,
	// currentTrackpoint));
	// }*/
	//
	//
	//
	// }

	private void removePauses() {
		this.removeAllPauses = true;
		new PauseDetectionPicCaseOperation().process(course);
		Collections.reverse(course.getPauses());
		int size = course.getPauses().size();
		int i = 0;
		boolean reset = false;
		for (Pause pause : course.getPauses()) {
			for (Trackpoint trackpoint : course.getTrackpoints()) {
				if (course.insidePause(pause, trackpoint.getTimestamp().getTime())) {
					this.trackpoint = trackpoint;
					removePause();
					break;
				}
			}
		}
		course.getPauses().clear();
		new PauseDetectionPicCaseOperation().process(course);
		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(course);
		Map<String, Object> options = new HashMap<String, Object>();
		/*
		 * for(Lap lap : course.getLaps()){ lap.setPausedTime(0.0); }
		 * course.setPausedTime(0.0);
		 */
		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
		try {
			new ConsolidationOperation(options).process(document);
		} catch (TrackItException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * while (i < size) { Pause pause = course.getPauses().get(i); reset =
		 * false; for (Trackpoint trackpoint : course.getTrackpoints()) { if
		 * (course.insidePause(pause, trackpoint.getTimestamp().getTime())) {
		 * this.trackpoint = trackpoint; removePause(); new
		 * PauseDetectionPicCaseOperation().process(course); i = 0; size--;
		 * reset = true; break; } } if(!reset){ i++; }
		 * 
		 * }
		 */
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
	
	private void consolidateRecalc() {
		final Map<String, Object> options = new HashMap<String, Object>();
		
		ConsolidationLevel level = ConsolidationLevel.RECALCULATION;
		options.put(Constants.ConsolidationOperation.LEVEL, level);
		DocumentManager.getInstance().consolidate(course, options);
	}
}
