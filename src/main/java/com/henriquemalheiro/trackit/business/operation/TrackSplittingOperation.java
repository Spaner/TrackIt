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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.ColorScheme;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class TrackSplittingOperation extends OperationBase implements Operation {
	private Course course;
	private Trackpoint trackpoint;

	public TrackSplittingOperation() {
		super();
		setUp();
	}

	public TrackSplittingOperation(Map<String, Object> options) {
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
	public void process(List<GPSDocument> documents) throws TrackItException {
		// Do nothing
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		Course newCourse = splitCourseAtSelectedTrackpoint(document);
		document.add(newCourse);
		document.setChangedTrue();
		consolidate(course, newCourse);
	}

	private Course splitCourseAtSelectedTrackpoint(GPSDocument document) {
		Course newCourse = new Course();
		newCourse.setParent(document);
		// String newCourseName = (course.getName().length() < 10 ?
		// course.getName().concat("_2") : course.getName().substring(0,
		// 9).concat("2"));
		String newCourseName = course.getName().concat(" 2");
		newCourse.setName(newCourseName);

		int startIndex = course.getTrackpoints().indexOf(trackpoint)/* + 1 */;// 58406
		int endIndex = course.getTrackpoints().size();
		List<Trackpoint> copy = new ArrayList<Trackpoint>();
		List<Trackpoint> temp = course.getTrackpoints().subList(startIndex, endIndex);
		for (Trackpoint tp : temp) {
			copy.add(tp.clone());
		}
		int index = 0;
		while (index < copy.size()) {
			copy.get(index).setId(temp.get(index).getId());
			index++;
		}
		Trackpoint tp = copy.get(0);
		tp.setDistanceFromPrevious(0.);
		tp.setTimeFromPrevious(0.);
		newCourse.addTrackpoints(copy);

		startIndex = 0;
		endIndex = course.getTrackpoints().indexOf(trackpoint) + 1;
		consolidateCoursePoints(course, newCourse);
		course.setTrackpoints(course.getTrackpoints().subList(startIndex, endIndex));

		return newCourse;
	}

	private void consolidate(Course oldCourse, Course newCourse) {
		// consolidateCoursePoints(oldCourse, newCourse);
		consolidateTrackpoints(newCourse);
		consolidateSegments(oldCourse, newCourse);
		consolidateLaps(oldCourse, newCourse);
		consolidateCourses(oldCourse, newCourse);
	}

	private void consolidateCoursePoints(Course oldCourse, Course newCourse) {
		Iterator<CoursePoint> iterator = oldCourse.getCoursePoints().iterator();

		while (iterator.hasNext()) {
			CoursePoint coursePoint = iterator.next();

			if (newCourse.getTrackpoints().contains(coursePoint.getTrackpoint())) {
				newCourse.add(coursePoint);
				iterator.remove();
			}
		}
	}

	private void consolidateTrackpoints(Course newCourse) {
		double offset = newCourse.getFirstTrackpoint().getDistance();
		double distance;

		for (Trackpoint trackpoint : newCourse.getTrackpoints()) {
			distance = trackpoint.getDistance() - offset;
			trackpoint.setDistance(distance);
			trackpoint.setParent(newCourse);
		}
	}

	private void consolidateSegments(Course oldCourse, Course newCourse) {
		Iterator<TrackSegment> segmentIterator = oldCourse.getSegments().iterator();

		while (segmentIterator.hasNext()) {
			TrackSegment segment = segmentIterator.next();
			List<Trackpoint> trackpoints = segment.getTrackpoints();

			Trackpoint firstTrackpoint = trackpoints.get(0);
			Trackpoint lastTrackpoint = trackpoints.get(trackpoints.size() - 1);

			if (oldCourse.getTrackpoints().contains(firstTrackpoint)
					&& newCourse.getTrackpoints().contains(lastTrackpoint)) {

				int firstIndex = trackpoints.indexOf(newCourse.getTrackpoints().get(0));
				int lastIndex = trackpoints.size();

				TrackSegment newSegment = new TrackSegment(newCourse);
				newSegment.setTrackpoints(trackpoints.subList(firstIndex, lastIndex));
				newCourse.add(newSegment);

				lastIndex = firstIndex + 1;
				firstIndex = 0;
				segment.setTrackpoints(trackpoints.subList(firstIndex, lastIndex));
			}
		}
	}

	private void consolidateLaps(Course oldCourse, Course newCourse) {
		List<Lap> laps = oldCourse.getLaps();

		Date oldCourseLastTime = oldCourse.getLastTrackpoint().getTimestamp();
		Lap intersectingLap = calculateIntersectingLap(laps, oldCourseLastTime);
		int intersectingLapIndex = laps.indexOf(intersectingLap);

		CourseLap lap = new CourseLap(oldCourse);
		lap.setStartTime(intersectingLap.getStartTime());
		lap.setEndTime(oldCourse.getLastTrackpoint().getTimestamp());
		if (lap.getStartTime() != lap.getEndTime()) {
			laps.add(intersectingLapIndex, lap);
			intersectingLapIndex++;

			intersectingLap.setStartTime(newCourse.getFirstTrackpoint().getTimestamp());
			((CourseLap) intersectingLap).setParent(oldCourse);
			oldCourse.setLaps(new ArrayList<>(laps.subList(0, intersectingLapIndex)));
			newCourse.setLaps(new ArrayList<>(laps.subList(intersectingLapIndex, laps.size())));
		}
		else{
			oldCourse.setLaps(new ArrayList<>(laps.subList(0, intersectingLapIndex)));
			newCourse.setLaps(new ArrayList<>(laps.subList(intersectingLapIndex, laps.size())));
		}
		

		for (Lap oldCourseLap : oldCourse.getLaps()) {
			oldCourseLap.consolidate(ConsolidationLevel.SUMMARY);
		}

		for (Lap newCourseLap : newCourse.getLaps()) {
			((CourseLap) newCourseLap).setParent(newCourse);
			newCourseLap.consolidate(ConsolidationLevel.SUMMARY);
		}
	}

	private Lap calculateIntersectingLap(List<Lap> laps, Date referenceTime) {
		for (Lap lap : laps) {
			if (lap.getEndTime().after(referenceTime)) {
				return lap;
			}
		}
		throw new IllegalStateException("Split operation: intersecting lap not found!");
	}

	private void consolidateCourses(Course oldCourse, Course newCourse) {
		oldCourse.consolidate(ConsolidationLevel.SUMMARY);
		oldCourse.setAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME, ColorScheme.getNextColorScheme());
		newCourse.consolidate(ConsolidationLevel.SUMMARY);
		newCourse.setAttribute(Constants.PAINTING_ATTRIBUTES.COLOR_SCHEME, ColorScheme.getNextColorScheme());
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void undoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
	}

	@Override
	public void redoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub
	}
}
