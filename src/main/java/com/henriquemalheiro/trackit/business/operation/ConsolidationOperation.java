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

/* A operacao de consolidacao ao nivel de recalculo esta partida...
 * 
 * 
 * */

package com.henriquemalheiro.trackit.business.operation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.ColorScheme;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.ActivityLap;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.IntensityType;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.LapTriggerType;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.view.map.provider.bingmaps.elevation.Elevation;

public class ConsolidationOperation extends OperationBase implements Operation {
	private enum MissingDataField {
		LONGITUDE("LONGITUDE"), LATITUDE("LATITUDE"), ALTITUDE("ALTITUDE"), DISTANCE(
				"DISTANCE"), TIMESTAMP("TIMESTAMP"), SPEED("SPEED"), HEART_RATE(
				"HEART_RATE"), POWER("POWER"), CADENCE("CADENCE"), TEMPERATURE(
				"TEMPERATURE");

		private String value;

		private MissingDataField(String value) {
			this.value = value;
		}

		private String getValue() {
			return value;
		}
	}

	@SuppressWarnings("unused")
	// 58406
	private static final double DEFAULT_SPEED_MS = 10 * 1000.0 / 3600.0;
	private ConsolidationLevel level;

	public ConsolidationOperation() {
		super();
		options.put(Constants.ConsolidationOperation.LEVEL,
				ConsolidationLevel.BASIC);
	}

	public ConsolidationOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		level = (ConsolidationLevel) options
				.get(Constants.ConsolidationOperation.LEVEL);
	}

	@Override
	public String getName() {
		return Constants.ConsolidationOperation.NAME;
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for (GPSDocument document : documents) {
			process(document);
		}
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		try {
			if (processActivities) {
				processActivities(document.getActivities());
			}

			if (processCourses) {
				processCourses(document.getCourses());
			}

			if (processWaypoints) {
				processWaypoints(document.getWaypoints());
			}
		} catch (TrackItException e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	private void processActivities(List<Activity> activities)
			throws TrackItException {
		for (Activity activity : activities) {
			process(activity);
		}
	}

	public void process(Activity activity) throws TrackItException {

		logger.debug(String.format("Consolidating activity %s.",
				activity.getName()));
		boolean recalculation = (level == ConsolidationLevel.RECALCULATION);

		if (!hasColorScheme(activity)) {
			setColorScheme(activity);
		}

		processTrackpoints(activity);

		if (!validLapData(activity)) {
			activity.add(createLap(activity));
		}

		if (activity.getLaps().isEmpty()) {
			activity.add(createLap(activity));
		}

		for (Lap currentLap : activity.getLaps()) {
			process(currentLap);
		}

		if (activity.getSessions().isEmpty()) {
			Session session = new Session(activity);
			activity.add(session);
		}

		for (Session session : activity.getSessions()) {
			process(session);
		}

		if (activity.getTotalTimerTime() == null
				|| activity.getStartTime() == null || recalculation) {
			Date startTime = activity.getFirstSession().getStartTime();
			double timerTime = 0.0;

			for (Session session : activity.getSessions()) {
				timerTime += session.getTimerTime();
			}

			if (activity.getTotalTimerTime() == null || recalculation) {
				activity.setTotalTimerTime(timerTime);
			}

			if (activity.getStartTime() == null || recalculation) {
				activity.setStartTime(startTime);
			}
		}
	}

	private void processCourses(List<Course> courses) throws TrackItException {
		boolean unsaved;
		for (Course course : courses) {
			unsaved = course.getUnsavedChanges();
			process(course);
			course.setUnsavedChanges(unsaved);
		}
	}

	public void process(Course course) throws TrackItException {
		logger.debug(String.format("Processing course %s.", course.getName()));

		if (!hasColorScheme(course)) {
			setColorScheme(course);
		}

		processTrackpoints(course);
		processCoursePoints(course);
		processSegments(course);

		if (!validLapData(course) || level == ConsolidationLevel.RECALCULATION) {
			List<Trackpoint> trackpoints = course.getTrackpoints();
			Trackpoint firstTrackpoint = trackpoints.get(0);
			Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);

			Lap lap = new CourseLap(course);

			lap.setTrackpoints(trackpoints);
			lap.setDistance(lastTrackpoint.getDistance());
			lap.setStartTime(firstTrackpoint.getTimestamp());
			lap.setEndTime(lastTrackpoint.getTimestamp());

			double duration = (lastTrackpoint.getTimestamp().getTime() - firstTrackpoint
					.getTimestamp().getTime()) / 1000.0;
			lap.setTimerTime(duration);

			course.getLaps().clear();
			course.add(lap);
		}

		for (Lap lap : course.getLaps()) {
			process(lap);
		}

		course.consolidate(level);
	}

	private boolean validLapData(DocumentItem item) {
		boolean validLapData = true;
		validLapData &= !item.getLaps().isEmpty();

		for (Lap lap : item.getLaps()) {
			validLapData &= (lap.getStartTime() != null);
			validLapData &= (lap.getEndTime() != null);
		}

		return validLapData;
	}

	private void processTrackpoints(DocumentItem item) throws TrackItException {
		List<Trackpoint> trackpoints = item.getTrackpoints();

		if (trackpoints.isEmpty()) {
			return;
		}

		Iterator<Trackpoint> iter = trackpoints.iterator();
		Trackpoint previousTrackpoint = trackpoints.get(0);

		if (previousTrackpoint.getTimestamp() == null) {
			Date initialTimestamp = getRandomTimestamp();
			previousTrackpoint.setTimestamp(initialTimestamp);
		}

		if (previousTrackpoint.getDistance() == null) {
			previousTrackpoint.setDistance(0.0);
		}

		if (previousTrackpoint.getDistanceFromPrevious() == null) {
			previousTrackpoint.setDistanceFromPrevious(0.0);
		}

		if (previousTrackpoint.getTimeFromPrevious() == null) {
			previousTrackpoint.setTimeFromPrevious(0.0);
		}

		if (previousTrackpoint.getSpeed() == null) {
			previousTrackpoint.setSpeed(0.0);
		}

		boolean altitudeDataPresent = (previousTrackpoint.getAltitude() != null);
		boolean positioningDataPresent = (previousTrackpoint.getLongitude() != null && previousTrackpoint
				.getLatitude() != null);

		Trackpoint currentTrackpoint = null;

		while (iter.hasNext()) {
			currentTrackpoint = iter.next();
			Double prevSpeed = previousTrackpoint.getSpeed();
			process(previousTrackpoint, currentTrackpoint, prevSpeed);

			if (currentTrackpoint.getSpeed().isInfinite()) {
				currentTrackpoint.setSpeed(previousTrackpoint.getSpeed());
			}

			altitudeDataPresent &= (currentTrackpoint.getAltitude() != null);
			if (currentTrackpoint.getLongitude() == null
					|| currentTrackpoint.getLatitude() == null) {
				iter.remove();
			}

			previousTrackpoint = currentTrackpoint;
		}

		if (altitudeDataPresent && positioningDataPresent) {
			calculateTrackpointGrades2(trackpoints);
		} else {
			for (Trackpoint trackpoint : trackpoints) {
				trackpoint.setGrade(0.0f);
			}
		}
		// 58406###################################################################################
		/*
		 * int i = 1; while (i < trackpoints.size()) {
		 * updateSpeedfromList(trackpoints, i); i++; }
		 */

		Boolean noSpeedData = true;
		if (item instanceof Activity)
			noSpeedData = ((Activity) item).getNoSpeedInFile();
		if (item instanceof Course)
			noSpeedData = ((Course) item).getNoSpeedInFile();
		if (noSpeedData) {
			double limit = TrackIt.getPreferences().getDoublePreference(
					Constants.PrefsCategories.PAUSE, null,
					Constants.PausePreferences.SPEED_THRESHOLD, 1.5);
			limit = limit / 3.6;
			updateSpeedWithPauseTime(trackpoints, limit);
		}
		// ########################################################################################
	}

	@SuppressWarnings("unused")
	private void processTrackpointsData(List<Trackpoint> trackpoints)
			throws TrackItException {
		Map<String, Boolean> missingData = calculateMissingData(trackpoints);
		processTrackpointsMissingData(trackpoints, missingData);
	}

	private Map<String, Boolean> calculateMissingData(
			List<Trackpoint> trackpoints) {
		Map<String, Integer> frequencies = initializeFrequencies();
		calculateFrequencies(frequencies, trackpoints);

		return calculateMissingData(frequencies, trackpoints.size());
	}

	private Map<String, Integer> initializeFrequencies() {
		Map<String, Integer> frequencies = new LinkedHashMap<String, Integer>();
		for (MissingDataField field : MissingDataField.values()) {
			frequencies.put(field.getValue(), 0);
		}

		return frequencies;
	}

	private void calculateFrequencies(Map<String, Integer> frequencies,
			List<Trackpoint> trackpoints) {
		for (Trackpoint trackpoint : trackpoints) {
			for (String field : frequencies.keySet()) {
				if (trackpoint.get(field) != null) {
					int frequency = frequencies.get(field);
					frequency++;
					frequencies.put(field, frequency);
				}
			}
		}
	}

	private Map<String, Boolean> calculateMissingData(
			Map<String, Integer> frequencies, int numberOfTrackpoints) {
		final double missingDataTreshold = 0.75;

		Map<String, Boolean> missingData = new LinkedHashMap<String, Boolean>();
		for (String field : frequencies.keySet()) {
			double percentage = frequencies.get(field)
					/ (numberOfTrackpoints * 1.0);
			boolean missing = (percentage >= 0 && percentage < missingDataTreshold);
			missingData.put(field, missing);
		}

		return missingData;
	}

	private void processTrackpointsMissingData(List<Trackpoint> trackpoints,
			Map<String, Boolean> missingData) {
		List<Trackpoint> trackpointsToRemove = new ArrayList<>();
		Trackpoint trackpoint;

		Iterator<Trackpoint> iterator = trackpoints.iterator();
		while (iterator.hasNext()) {
			trackpoint = iterator.next();

			for (String field : missingData.keySet()) {
				if (missingData.get(field)) {
					trackpoint.set(field, null);
				} else if (trackpoint.get(field) == null) {
					boolean success = interpolate(field, trackpoints,
							trackpoints.indexOf(trackpoint));
					if (!success) {
						trackpointsToRemove.add(trackpoint);
					}
				}
			}
		}

		trackpoints.removeAll(trackpointsToRemove);
	}

	private boolean interpolate(String field, List<Trackpoint> trackpoints,
			int currentPosition) {
		if (currentPosition == -1) {
			return false;
		}

		Trackpoint previous = getPrevious(field, trackpoints, currentPosition);
		Trackpoint next = getNext(field, trackpoints, currentPosition);

		if (previous == null || next == null) {
			return false;
		}

		switch (field) {
		case "LONGITUDE":
		case "LATITUDE":
		case "SPEED":
		case "ALTITUDE":
			Double dValue = interpolateDoubleValues(field, previous, next);
			trackpoints.get(currentPosition).set(field, dValue);
			break;
		case "POWER":
			long iValue = interpolateIntegerValues(field, previous, next);
			trackpoints.get(currentPosition).set(field, iValue);
			break;
		case "HEART_RATE":
		case "CADENCE":
			short sValue = interpolateShortValues(field, previous, next);
			trackpoints.get(currentPosition).set(field, sValue);
			break;
		case "TEMPERATURE":
			byte bValue = interpolateByteValues(field, previous, next);
			trackpoints.get(currentPosition).set(field, bValue);
		}

		return true;
	}

	private Trackpoint getPrevious(String field, List<Trackpoint> trackpoints,
			int currentPosition) {
		ListIterator<Trackpoint> iterator = trackpoints
				.listIterator(currentPosition);
		Trackpoint trackpoint = null;

		while (iterator.hasPrevious()) {
			trackpoint = iterator.previous();
			if (trackpoint.get(field) != null) {
				return trackpoint;
			}
		}

		return null;
	}

	private Trackpoint getNext(String field, List<Trackpoint> trackpoints,
			int currentPosition) {
		ListIterator<Trackpoint> iterator = trackpoints
				.listIterator(currentPosition);
		Trackpoint trackpoint = null;

		while (iterator.hasNext()) {
			trackpoint = iterator.next();
			if (trackpoint.get(field) != null) {
				return trackpoint;
			}
		}

		return null;
	}

	private double interpolateDoubleValues(String field, Trackpoint previous,
			Trackpoint next) {
		double coordinate1 = (double) previous.get(field);
		double coordinate2 = (double) next.get(field);

		return (coordinate1 + ((coordinate2 - coordinate1) / 2.0));
	}

	@SuppressWarnings("unused")
	private long interpolateLongValues(String field, Trackpoint previous,
			Trackpoint next) {
		long coordinate1 = (long) previous.get(field);
		long coordinate2 = (long) next.get(field);

		return (coordinate1 + ((coordinate2 - coordinate1) / 2));
	}

	private int interpolateIntegerValues(String field, Trackpoint previous,
			Trackpoint next) {
		int coordinate1 = (int) previous.get(field);
		int coordinate2 = (int) next.get(field);

		return (int) (coordinate1 + ((coordinate2 - coordinate1) / 2));
	}

	private short interpolateShortValues(String field, Trackpoint previous,
			Trackpoint next) {
		short coordinate1 = (short) previous.get(field);
		short coordinate2 = (short) next.get(field);

		return (short) (coordinate1 + ((coordinate2 - coordinate1) / 2));
	}

	private byte interpolateByteValues(String field, Trackpoint previous,
			Trackpoint next) {
		byte coordinate1 = (byte) previous.get(field);
		byte coordinate2 = (byte) next.get(field);

		return (byte) (coordinate1 + ((coordinate2 - coordinate1) / 2));
	}

	private Date getRandomTimestamp() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(
				Calendar.YEAR,
				Utilities.getRandomNumber(2000,
						Calendar.getInstance().get(Calendar.YEAR)));
		calendar.set(Calendar.MONTH, Utilities.getRandomNumber(0, 11));
		calendar.set(
				Calendar.DAY_OF_MONTH,
				Utilities.getRandomNumber(1,
						calendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
		calendar.set(Calendar.HOUR, Utilities.getRandomNumber(0, 23));
		calendar.set(Calendar.MINUTE, Utilities.getRandomNumber(0, 59));
		calendar.set(Calendar.SECOND, Utilities.getRandomNumber(0, 59));
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}

	private void processCoursePoints(Course course) {
		for (CoursePoint coursePoint : course.getCoursePoints()) {
			coursePoint.consolidate(level);
		}

		Collections.sort(course.getCoursePoints(),
				new Comparator<CoursePoint>() {
					@Override
					public int compare(CoursePoint coursePoint1,
							CoursePoint coursePoint2) {
						return coursePoint1.getTime().compareTo(
								coursePoint2.getTime());
					}
				});
	}

	private void processSegments(DocumentItem parent) {
		for (TrackSegment segment : parent.getSegments()) {
			segment.consolidate(level);
		}
	}

	private void process(Trackpoint lastTrackpoint,
			Trackpoint currentTrackpoint, Double prevSpeed) {
		if (currentTrackpoint.getDistance() == null
				|| level == ConsolidationLevel.RECALCULATION) {
			updateDistance(lastTrackpoint, currentTrackpoint);
		}
		currentTrackpoint.setDistanceFromPrevious(currentTrackpoint
				.getDistance() - lastTrackpoint.getDistance());

		if (currentTrackpoint.getTimestamp() == null
				|| level == ConsolidationLevel.RECALCULATION) {
			updateTimestamp(lastTrackpoint, currentTrackpoint, prevSpeed);
		}
		currentTrackpoint.setTimeFromPrevious((currentTrackpoint.getTimestamp()
				.getTime() - lastTrackpoint.getTimestamp().getTime()) / 1000.0);

		if (currentTrackpoint.getSpeed() == null
		/* || level == ConsolidationLevel.RECALCULATION */) {
			updateSpeed(lastTrackpoint, currentTrackpoint);
		}

		if (currentTrackpoint.getAltitude() == null) {
			updateAltitude(currentTrackpoint);
		}
	}

	private void updateAltitude(Trackpoint trackpoint) {
		if (trackpoint.getLongitude() == null
				|| trackpoint.getAltitude() != null) {
			return;
		}

		Location location = getLocation(trackpoint);
		try {
			Elevation.fetchElevation(location);
			trackpoint.setAltitude(location.getAltitude());
		} catch (TrackItException e) {
			// ignore: don't set altitude
		}
	}

	private Location getLocation(Trackpoint trackpoint) {
		return new Location(trackpoint.getLongitude(), trackpoint.getLatitude());
	}

	private void updateDistance(Trackpoint lastTrackpoint,
			Trackpoint currentTrackpoint) {
		double distance = calculateDistance(lastTrackpoint, currentTrackpoint);
		currentTrackpoint.setDistance(lastTrackpoint.getDistance() + distance);
	}

	private double calculateDistance(Trackpoint trkpt1, Trackpoint trkpt2) {
		return (trkpt1.getLongitude() != null && trkpt1.getLatitude() != null
				&& trkpt2.getLongitude() != null
				&& trkpt2.getLatitude() != null ? Utilities
				.getGreatCircleDistance(trkpt1.getLatitude(),
						trkpt1.getLongitude(), trkpt2.getLatitude(),
						trkpt2.getLongitude()) * 1000 : 0.0);
	}

	private void updateTimestamp(Trackpoint previousTrackpoint,
			Trackpoint trackpoint, Double prevSpeed) {
		boolean keepTimes = TrackIt
				.getPreferences()
				.getBooleanPreference(
						Constants.PrefsCategories.EDITION,
						null,
						Constants.EditionPreferences.KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL,
						true);
		if (!keepTimes || trackpoint.getTimestamp() == null) {

			/*
			 * double speed; if(previousTrackpoint.getSpeed() != null) speed =
			 * previousTrackpoint.getSpeed() * (1000.0 / 3600.0); else speed =
			 * DEFAULT_SPEED_MS;
			 */
			double timeFromPrevious = trackpoint.getDistanceFromPrevious()
					/ prevSpeed;

			// double timeFromPrevious = trackpoint.getDistanceFromPrevious() /
			// speed;

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(previousTrackpoint.getTimestamp());
			calendar.add(Calendar.SECOND, (int) timeFromPrevious);
			calendar.add(Calendar.MILLISECOND,
					(int) Math.round((timeFromPrevious % 1) / 1000.0));
			trackpoint.setTimestamp(calendar.getTime());
		} else {
			// DO NOTHING???
		}
	}

	private void updateSpeed(Trackpoint lastTrackpoint,
			Trackpoint currentTrackpoint) {
		double speed = 0.0;
		if (currentTrackpoint.getTimeFromPrevious() > 0) {
			speed = currentTrackpoint.getDistanceFromPrevious()
					/ currentTrackpoint.getTimeFromPrevious();
		}
		currentTrackpoint.setSpeed(speed);
	}

	private void calculateTrackpointGrades2(List<Trackpoint> trackpoints)
			throws TrackItException {
		GPSDocument document = new GPSDocument(null);
		Course course = new Course();
		course.setTrackpoints(trackpoints);
		document.add(course);

		new SmoothingOperation().process(document);
	}

	@SuppressWarnings("unused")
	private void calculateTrackpointGrades(List<Trackpoint> trackpoints) {
		if (trackpoints.size() < 3) {
			for (Trackpoint trackpoint : trackpoints) {
				trackpoint.setGrade(0.0f);
			}
			return;
		}

		int segmentSize = calculateSegmentSize(trackpoints);
		double[] resampledGrades = getResampledGrades(trackpoints, segmentSize);

		for (int i = 0; i < trackpoints.size(); i++) {
			Trackpoint trackpoint = trackpoints.get(i);
			int index = trackpoint.getDistance().intValue() / segmentSize;

			double initialDistance = (index) * segmentSize;
			double distance = trackpoint.getDistance() - initialDistance;
			double factor = distance / segmentSize;
			double grade = resampledGrades[index]
					+ (resampledGrades[index + 1] - resampledGrades[index])
					* factor;

			trackpoint.setGrade((float) grade);
		}
	}

	private double[] getResampledGrades(List<Trackpoint> trackpoints,
			int segmentSize) {
		final double distance = getDistance(trackpoints);
		final int numberOfSegments = (int) Math.ceil(distance / segmentSize) + 1;

		double[] resampledGrades = new double[numberOfSegments];
		for (int i = 1; i < numberOfSegments; i++) {
			double currentDistance = i * segmentSize;
			double previousAltitude = getAltitude(trackpoints, currentDistance
					- segmentSize);
			double nextAltitude = getAltitude(trackpoints, currentDistance);
			double grade = (nextAltitude - previousAltitude) / segmentSize
					* 100.0;

			resampledGrades[i] = grade;
		}

		return resampledGrades;
	}

	private double getDistance(List<Trackpoint> trackpoints) {
		Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);
		return lastTrackpoint.getDistance();
	}

	private double getAltitude(List<Trackpoint> trackpoints, double distance) {
		double altitude = trackpoints.get(trackpoints.size() - 1).getAltitude();
		Trackpoint previousTrackpoint = null;
		Trackpoint currentTrackpoint = null;

		for (int i = 1; i < trackpoints.size(); i++) {
			currentTrackpoint = trackpoints.get(i);

			if (currentTrackpoint.getDistance() >= distance) {
				previousTrackpoint = trackpoints.get(i - 1);
				altitude = getAltitude(previousTrackpoint, currentTrackpoint,
						distance);
				break;
			}
		}

		return altitude;
	}

	private double getAltitude(Trackpoint trackpoint1, Trackpoint trackpoint2,
			double distance) {
		double segmentDistance = trackpoint2.getDistance()
				- trackpoint1.getDistance();
		double factor = (distance - trackpoint1.getDistance())
				/ segmentDistance;
		double altitudeDifference = trackpoint2.getAltitude()
				- trackpoint1.getAltitude();
		double altitude = trackpoint1.getAltitude()
				+ (altitudeDifference * factor);

		return altitude;
	}

	private int calculateSegmentSize(List<Trackpoint> trackpoints) {
		double averageTrackpointDistance = calculateAverageTrackpointDistance(trackpoints);
		// 58406
		// logger.info(String.format("Average trackpoint distance: %.2f",
		// averageTrackpointDistance));

		int segmentSize;
		if (averageTrackpointDistance <= 4.0) {
			segmentSize = 16;
		} else if (averageTrackpointDistance <= 8.0) {
			segmentSize = 32;
		} else if (averageTrackpointDistance <= 16.0) {
			segmentSize = 64;
		} else if (averageTrackpointDistance <= 24.0) {
			segmentSize = 96;
		} else if (averageTrackpointDistance <= 32.0) {
			segmentSize = 128;
		} else {
			segmentSize = 256;
		}

		// 58406
		// logger.info(String.format("Segment size: %d", segmentSize));

		return segmentSize;
	}

	private double calculateAverageTrackpointDistance(
			List<Trackpoint> trackpoints) {
		Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);
		return (lastTrackpoint.getDistance() / trackpoints.size());
	}

	private void process(Session session) {
		session.consolidate(level);
	}

	private void process(Lap lap) {
		ColorScheme colorScheme = (ColorScheme) lap.getParent().getAttribute(
				Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME);
		lap.setAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME,
				colorScheme);
		lap.consolidate(level);
	}

	private Lap createLap(Activity activity) {
		List<Trackpoint> trackpoints = activity.getTrackpoints();

		double elapsedTime = 0.0;
		double time = 0.0;
		double distance = 0.0;
		double averageSpeed = 0.0;
		double maximumSpeed = 0.0;
		double averageHeartRate = 0.0;
		short minimumHeartRate = 300;
		short maximumHeartRate = 0;
		double averageCadence = 0;
		short maximumCadence = 0;
		double totalAscent = 0.0;
		double totalDescent = 0.0;

		for (Trackpoint trackpoint : trackpoints) {
			distance += trackpoint.getDistanceFromPrevious();
			elapsedTime += trackpoint.getTimeFromPrevious();
			time += trackpoint.getTimeFromPrevious();
		}

		long heartRateCount = 0;
		long cadenceCount = 0;
		double lastAltitude = trackpoints.get(0).getAltitude();
		for (Trackpoint trackpoint : trackpoints) {
			if (trackpoint.getAltitude() != null) {
				double altitudeDifference = trackpoint.getAltitude()
						- lastAltitude;

				if (altitudeDifference > 0.0) {
					totalAscent += altitudeDifference;
				} else if (altitudeDifference < 0.0) {
					totalDescent += altitudeDifference;
				}

				lastAltitude = trackpoint.getAltitude();
			}

			if (trackpoint.getTimeFromPrevious() > 0) {
				averageSpeed += (trackpoint.getDistanceFromPrevious() / trackpoint
						.getTimeFromPrevious())
						* (trackpoint.getTimeFromPrevious() / time);
			}
			maximumSpeed = (trackpoint.getSpeed() > maximumSpeed ? trackpoint
					.getSpeed() : maximumSpeed);

			if (trackpoint.getHeartRate() != null
					&& trackpoint.getHeartRate() > 0) {
				averageHeartRate += trackpoint.getHeartRate();
				heartRateCount++;
				minimumHeartRate = (short) Math.min(minimumHeartRate,
						trackpoint.getHeartRate());
				maximumHeartRate = (short) Math.max(maximumHeartRate,
						trackpoint.getHeartRate());
			}

			if (trackpoint.getCadence() != null && trackpoint.getCadence() > 0) {
				averageCadence += trackpoint.getCadence();
				cadenceCount++;
				maximumCadence = (short) Math.max(maximumCadence,
						trackpoint.getCadence());
			}
		}
		averageHeartRate /= heartRateCount;
		averageCadence /= cadenceCount;

		Lap lap = new ActivityLap(activity);
		lap.setStartTime(trackpoints.get(0).getTimestamp());
		lap.setEndTime(trackpoints.get(trackpoints.size() - 1).getTimestamp());

		lap.setStartLatitude(trackpoints.get(0).getLatitude());
		lap.setStartLongitude(trackpoints.get(0).getLongitude());
		lap.setStartAltitude(trackpoints.get(0).getAltitude());

		lap.setEndLatitude(trackpoints.get(trackpoints.size() - 1)
				.getLatitude());
		lap.setEndLongitude(trackpoints.get(trackpoints.size() - 1)
				.getLongitude());
		lap.setEndAltitude(trackpoints.get(trackpoints.size() - 1)
				.getAltitude());

		lap.setIntensity(IntensityType.ACTIVE);
		lap.setTrigger(LapTriggerType.MANUAL);

		lap.setDistance(distance);

		lap.setTimerTime(time);
		lap.setElapsedTime(elapsedTime);

		lap.setAverageSpeed(averageSpeed);
		lap.setMaximumSpeed(maximumSpeed);

		lap.setTotalAscent(totalAscent > 0 ? (int) totalAscent : null);
		lap.setTotalDescent(totalDescent < 0 ? (int) -totalDescent : null);

		lap.setAverageHeartRate(averageHeartRate > 0 ? (short) Math
				.round(averageHeartRate) : null);
		lap.setMinimumHeartRate(minimumHeartRate != 300 ? minimumHeartRate
				: null);
		lap.setMaximumHeartRate(maximumHeartRate > 0 ? maximumHeartRate : null);
		lap.setAverageCadence(averageCadence > 0 ? (short) Math
				.round(averageCadence) : null);
		lap.setMaximumCadence(maximumCadence > 0 ? maximumCadence : null);

		lap.setCalories(activity.getSessions().get(0).getCalories());
		lap.setSport(activity.getSessions().get(0).getSport());

		return lap;
	}

	private void setColorScheme(DocumentItem item) {
		ColorScheme colorScheme = ColorScheme.getNextColorScheme();
		item.setAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME,
				colorScheme);
	}

	private boolean hasColorScheme(DocumentItem item) {
		return (item.getAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME) != null);
	}

	private void processWaypoints(List<Waypoint> waypoints) {
	}

	// 58406###################################################################################
	@SuppressWarnings("unused")
	private void updateSpeedfromList(List<Trackpoint> trackpoints, int index) {
		double speed = 0.;

		Trackpoint lastPoint, nextPoint, currentPoint = null;
		currentPoint = trackpoints.get(index);
		if (index > 0 && index < trackpoints.size() - 2) {
			lastPoint = trackpoints.get(index - 1);
			nextPoint = trackpoints.get(index + 1);
			speed = (nextPoint.getDistance() - lastPoint.getDistance())
					/ (nextPoint.getTimeFromPrevious() + currentPoint
							.getTimeFromPrevious());
		}
		if (index == trackpoints.size() - 1) {
			lastPoint = trackpoints.get(index - 1);
			currentPoint = trackpoints.get(index);
			speed = currentPoint.getDistanceFromPrevious()
					/ currentPoint.getTimeFromPrevious();
		}
		currentPoint.setSpeed(speed);
	}

	private void updateSpeedWithPauseTime(List<Trackpoint> trackpoints,
			double pauseLimit) {
		double vm;
		Trackpoint trkp = trackpoints.get(0);
		Trackpoint trkpPlusOne = trackpoints.get(1);
		Trackpoint trkpMinusOne = null;
		double speed;
		double distance = trkp.getDistance();
		double time = trkp.getTimestamp().getTime() / 1000;
		double distancePlusOne = trkpPlusOne.getDistance();
		double timePlusOne = trkpPlusOne.getTimestamp().getTime() / 1000;
		double distanceMinusOne, timeMinusOne;
		vm = (distancePlusOne - distance) / (timePlusOne - time);
		if (vm < pauseLimit) {
			trkp.setSpeed(-1.);
			trkpPlusOne.setSpeed(-1.);
		} else {
			trkp.setSpeed(vm);
			trkpPlusOne.setSpeed(vm);
		}
		int n = trackpoints.size();
		for (int i = 2; i < n; i++) {
			trkp = trackpoints.get(i);
			trkpMinusOne = trackpoints.get(i - 1);
			distance = trkp.getDistance();
			time = trkp.getTimestamp().getTime() / 1000;
			distanceMinusOne = trkpMinusOne.getDistance();
			timeMinusOne = trkpMinusOne.getTimestamp().getTime() / 1000;

			vm = (distance - distanceMinusOne) / (time - timeMinusOne);

			if (vm <= pauseLimit) {
				trkp.setSpeed(vm);
				if (trkpMinusOne.getSpeed() >= 0) {
					trkpMinusOne.setSpeed((trkpMinusOne.getSpeed() + vm) / 2);
				} 
				else {
					trkpMinusOne.setSpeed(vm);
				}
			} else {
				trkp.setSpeed(-1.);
			}
		}
		for (int i = 1; i < n - 1; i++) {
			trkp = trackpoints.get(i);
			trkpPlusOne = trackpoints.get(i + 1);
			trkpMinusOne = trackpoints.get(i - 1);
			distance = trkp.getDistance();
			time = trkp.getTimestamp().getTime() / 1000;
			distancePlusOne = trkpPlusOne.getDistance();
			timePlusOne = trkpPlusOne.getTimestamp().getTime() / 1000;
			distanceMinusOne = trkpMinusOne.getDistance();
			timeMinusOne = trkpMinusOne.getTimestamp().getTime() / 1000;
			if (trkp.getSpeed() < 0) {
				if (time != timeMinusOne && time != timePlusOne) {
					speed = (((time - timeMinusOne) * (distancePlusOne - distance))
							/ (timePlusOne - time) + ((timePlusOne - time) * (distance - distanceMinusOne))
							/ (time - timeMinusOne))
							/ (timePlusOne - timeMinusOne);
					trkp.setSpeed(speed);
				} else {
					if (time == timeMinusOne && time == timePlusOne) {
						trkp.setSpeed(trkpMinusOne.getSpeed());
					} else {
						speed = (distancePlusOne - distanceMinusOne)
								/ (timePlusOne - timeMinusOne);
						trkp.setSpeed(speed);
					}
				}
			}
		}
		trkp = trackpoints.get(0);
		trkpPlusOne = trackpoints.get(1);
		if (trkp.getSpeed() < 0) {
			distance = trkp.getDistance();
			time = trkp.getTimestamp().getTime() / 1000;
			distancePlusOne = trkpPlusOne.getDistance();
			timePlusOne = trkpPlusOne.getTimestamp().getTime() / 1000;
			speed = 2 * (distancePlusOne - distance) / (timePlusOne - time)
					- trkpPlusOne.getSpeed();
			trkp.setSpeed(speed);
		}
		trkp = trackpoints.get(n - 1);
		trkpMinusOne = trackpoints.get(n - 2);
		if (trkp.getSpeed() < 0) {
			distance = trkp.getDistance();
			time = trkp.getTimestamp().getTime() / 1000;
			distanceMinusOne = trkpMinusOne.getDistance();
			timeMinusOne = trkpMinusOne.getTimestamp().getTime() / 1000;
			speed = 2 * (distance - distanceMinusOne) / (time - timeMinusOne)
					- trkpMinusOne.getSpeed();
			trkp.setSpeed(speed);
		}
	}
}
